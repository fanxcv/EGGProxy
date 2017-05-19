package cn.EGGMaster.tunnel;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

import cn.EGGMaster.util.JniUtils;

import static android.text.TextUtils.isEmpty;
import static cn.EGGMaster.util.StaticVal.METHOD_GET;
import static cn.EGGMaster.util.StaticVal.METHOD_POST;

public class HttpTunnel extends Tunnel {

    public HttpTunnel(InetSocketAddress serverAddress, Selector selector) throws Exception {
        super(serverAddress, selector);
    }

    @Override
    protected boolean write(ByteBuffer buffer, boolean copyRemainData) throws Exception {
        ByteBuffer m_Buffer = headerProcess(buffer);
        int bytesSent;
        while (m_Buffer.hasRemaining()) {
            bytesSent = m_InnerChannel.write(m_Buffer);
            if (bytesSent == 0) {
                break;//不能再发送了，终止循环
            }
        }

        if (m_Buffer.hasRemaining()) {//数据没有发送完毕
            if (copyRemainData) {//拷贝剩余数据，然后侦听写入事件，待可写入时写入。
                //拷贝剩余数据
                if (m_SendRemainBuffer == null) {
                    m_SendRemainBuffer = ByteBuffer.allocate(m_Buffer.capacity());
                }
                m_SendRemainBuffer.clear();
                m_SendRemainBuffer.put(m_Buffer);
                m_SendRemainBuffer.flip();
                m_InnerChannel.register(m_Selector, SelectionKey.OP_WRITE, this);//注册写事件
            }
            return false;
        } else {//发送完毕了
            return true;
        }
    }

    @Override
    protected boolean isTunnelEstablished() {
        return true;
    }

    private String getString(ByteBuffer buffer) {
        Charset charset;
        CharBuffer charBuffer;
        CharsetDecoder decoder;
        try {
            charset = Charset.forName("UTF-8");
            decoder = charset.newDecoder();
            charBuffer = decoder.decode(buffer.asReadOnlyBuffer());
            return charBuffer.toString();
        } catch (Exception ex) {
            return "";
        }
    }

    private ByteBuffer headerProcess(ByteBuffer buffer) {
        String request = getString(buffer);
        if (!isEmpty(request) && getMethod(request.substring(0, 10).trim())) {
            return ByteBuffer.wrap(JniUtils.getHttpHeader(request).getBytes());
        }
        return buffer;
    }

    private boolean getMethod(String str) {
        return str.startsWith(METHOD_GET) || str.startsWith(METHOD_POST);
    }
}
