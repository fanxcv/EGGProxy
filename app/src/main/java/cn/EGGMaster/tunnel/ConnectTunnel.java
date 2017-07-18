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
