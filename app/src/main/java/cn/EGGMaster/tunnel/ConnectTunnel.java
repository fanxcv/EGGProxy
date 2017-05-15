package cn.EGGMaster.tunnel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

import cn.EGGMaster.core.Configer;
import cn.EGGMaster.tcpip.CommonMethods;
import cn.EGGMaster.util.DataUtils;

public class ConnectTunnel extends Tunnel {

    private String remoteHost;
    private boolean m_TunnelEstablished;

    public ConnectTunnel(InetSocketAddress serverAddress, Selector selector, String remoteHost) throws IOException {
        super(serverAddress, selector);
        this.remoteHost = remoteHost != null ? remoteHost :
                CommonMethods.ipBytesToString(m_DestAddress.getAddress().getAddress()) + ":" + m_DestAddress.getPort();
    }

    @Override
    public void onReadable(SelectionKey key) {
        ByteBuffer buffer = DataUtils.getByteBuffer();
        try {
            int bytesRead = m_InnerChannel.read(buffer);
            if (bytesRead > 0) {
                buffer.flip();
                afterReceived(buffer);//先让子类处理，例如解密数据。
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

    private void afterReceived(ByteBuffer byteBuffer) throws Exception {
        if (!this.m_TunnelEstablished) {
            if (new String(byteBuffer.array(), byteBuffer.position(), 12).matches("^HTTP/1.[01] 200$")) {
                byteBuffer.limit(byteBuffer.position());
                this.m_TunnelEstablished = true;
                super.onTunnelEstablished();
            }
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
                onConnected(DataUtils.getConnBuffer());//通知子类TCP已连接，子类可以根据协议实现握手等。
            } else {//连接失败
                this.dispose();
            }
        } catch (Exception e) {
            this.dispose();
        }
    }

    private void onConnected(ByteBuffer byteBuffer) throws Exception {
        String format = Configer.https_first
                .replaceAll("\\[V\\]", "HTTP/1.0")
                .replaceAll("\\[M\\]", "CONNECT")
                .replaceAll("\\[U\\]", "/")
                .replaceAll("\\[H\\]", remoteHost + ":" + m_DestAddress.getPort())
                + "\r\n";
        byteBuffer.put(format.getBytes());
        byteBuffer.flip();
        if (write(byteBuffer, true)) {
            beginReceive();
        }
        DataUtils.setConnBuffer(byteBuffer);
    }

}
