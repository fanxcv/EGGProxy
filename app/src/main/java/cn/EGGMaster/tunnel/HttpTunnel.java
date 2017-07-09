package cn.EGGMaster.tunnel;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

import cn.EGGMaster.core.Configer;
import cn.EGGMaster.util.JniUtils;
import cn.EGGMaster.util.Utils;

import static android.text.TextUtils.isEmpty;
import static cn.EGGMaster.core.Configer.U_H_S;
import static cn.EGGMaster.util.Utils.log;

public class HttpTunnel extends Tunnel {

    public HttpTunnel(InetSocketAddress serverAddress, Selector selector) throws Exception {
        super(serverAddress, selector);
    }

    @Override
    protected boolean write(ByteBuffer buffer, boolean copyRemainData) throws Exception {
        ByteBuffer m_Buffer = HeaderProcess(buffer);
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

    private ByteBuffer HeaderProcess(ByteBuffer buffer) {
        try {
            String request;
            Charset charset = Charset.forName("UTF-8");
            CharsetDecoder decoder = charset.newDecoder();
            CharBuffer charBuffer = decoder.decode(buffer.asReadOnlyBuffer());
            request = charBuffer.toString();
            if (!isEmpty(request)) {
                String[] headers = JniUtils.getHttpHeader(request);
                String format = Configer.http_first;
                StringBuilder header = new StringBuilder();
                if (U_H_S) {
                    String url = "http://" + headers[1] + headers[0];
                    if (headers[1].endsWith(":80"))
                        url = "http://" + headers[1].substring(0, headers[1].length() - 3) + headers[0];
                    String time = String.valueOf(System.currentTimeMillis()).substring(0, 10) + "000";
                    format = format.replaceAll("\\[K\\]", Utils.getKey(url, time))
                            .replaceAll("\\[T\\]", time);
                }
                header.append(format.replaceAll("\\[M\\]", headers[2])
                        .replaceAll("\\[V\\]", headers[3])
                        .replaceAll("\\[H\\]", headers[1])
                        .replaceAll("\\[U\\]", headers[0]))
                        .append(headers[4]).append("\r\n");
                log(header.toString());
                return ByteBuffer.wrap(header.toString().getBytes());
            }
        } catch (Exception e) {
            return buffer;
        }
        return buffer;
    }

}
