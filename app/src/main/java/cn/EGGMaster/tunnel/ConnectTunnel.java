package cn.EGGMaster.tunnel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

import cn.EGGMaster.tcpip.CommonMethods;
import cn.EGGMaster.util.DataUtils;
import cn.EGGMaster.util.JniUtils;

public class ConnectTunnel extends Tunnel {

    private String remoteHost;
    private boolean m_TunnelEstablished;

    public ConnectTunnel(InetSocketAddress serverAddress, Selector selector, String remoteHost) throws IOException {
        super(serverAddress, selector);
        this.remoteHost = remoteHost != null ? remoteHost : CommonMethods.ipBytesToString(m_DestAddress.getAddress().getAddress());
    }

//    @Override
//    protected boolean write(ByteBuffer buffer, boolean copyRemainData) throws Exception {
//        ByteBuffer m_Buffer = headerProcess(buffer);
//        int bytesSent;
//        while (m_Buffer.hasRemaining()) {
//            bytesSent = m_InnerChannel.write(m_Buffer);
//            if (bytesSent == 0) {
//                break;//不能再发送了，终止循环
//            }
//        }
//
//        if (m_Buffer.hasRemaining()) {//数据没有发送完毕
//            if (copyRemainData) {//拷贝剩余数据，然后侦听写入事件，待可写入时写入。
//                //拷贝剩余数据
//                if (m_SendRemainBuffer == null) {
//                    m_SendRemainBuffer = ByteBuffer.allocate(m_Buffer.capacity());
//                }
//                m_SendRemainBuffer.clear();
//                m_SendRemainBuffer.put(m_Buffer);
//                m_SendRemainBuffer.flip();
//                m_InnerChannel.register(m_Selector, SelectionKey.OP_WRITE, this);//注册写事件
//            }
//            return false;
//        } else {//发送完毕了
//            return true;
//        }
//    }
//
//    private ByteBuffer headerProcess(ByteBuffer buffer) {
//        String request;
//        try {
//            Charset charset = Charset.forName("UTF-8");
//            CharsetDecoder decoder = charset.newDecoder();
//            CharBuffer charBuffer = decoder.decode(buffer.asReadOnlyBuffer());
//            request = charBuffer.toString();
//            System.out.println("这是https里的请求：" + request);
//            if (!isEmpty(request)) {
//                String str = request.substring(0, 10).trim();
//                if (str.startsWith(METHOD_GET) || str.startsWith(METHOD_POST))
//                    return ByteBuffer.wrap(JniUtils.getHttpHeader(request).getBytes());
//            }
//        } catch (Exception ex) {
//            return buffer;
//        }
//        return buffer;
//    }

    @Override
    public void onReadable(SelectionKey key) {
        ByteBuffer buffer = DataUtils.getByteBuffer();
        try {
            int bytesRead = m_InnerChannel.read(buffer);
            if (bytesRead > 0) {
                buffer.flip();
                if (!this.m_TunnelEstablished) {
                    if (new String(buffer.array(), buffer.position(), 12).matches("^HTTP/1.[01] 200$")) {
                        buffer.limit(buffer.position());
                        this.m_TunnelEstablished = true;
                        super.onTunnelEstablished();
                    }
                }
                if (isTunnelEstablished() && buffer.hasRemaining()) {//将读到的数据，转发给兄弟。
                    //m_BrotherTunnel.beforeSend(buffer);//发送之前，先让子类处理，例如做加密等。
                    if (!m_BrotherTunnel.write(buffer, true)) {
                        key.cancel();//兄弟吃不消，就取消读取事件。
                    }
                }
            } else if (bytesRead < 0) {
                this.dispose();//连接已关闭，释放资源。
            }
        } catch (Exception e) {
            //e.printStackTrace();
            this.dispose();
        } finally {
            DataUtils.setByteBuffer(buffer);
        }
    }

    @Override
    protected boolean isTunnelEstablished() {
        return this.m_TunnelEstablished;
    }

    @Override
    public void onConnectable() {
        try {
            if (m_InnerChannel.finishConnect()) {//连接成功
                if (write(ByteBuffer.wrap(JniUtils.getCoonHeader(remoteHost, String.valueOf(m_DestAddress.getPort())).getBytes()), true)) {
                    beginReceive();
                }
            } else {//连接失败
                this.dispose();
            }
        } catch (Exception e) {
            this.dispose();
        }
    }

}
