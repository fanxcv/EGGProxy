package cn.EGGMaster.tunnel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

import cn.EGGMaster.core.Configer;
import cn.EGGMaster.tcpip.CommonMethods;
import cn.EGGMaster.util.DataUtils;
import cn.EGGMaster.util.Utils;

import static cn.EGGMaster.core.Configer.U_S_S;
import static cn.EGGMaster.util.Utils.log;

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
                onConnected(DataUtils.getByteBuffer());
            } else {//连接失败
                this.dispose();
            }
        } catch (Exception e) {
            this.dispose();
        }
    }

    private void onConnected(ByteBuffer byteBuffer) throws Exception {
        remoteHost = remoteHost.contains(":") ? remoteHost : remoteHost + ":" + m_DestAddress.getPort();
        String format = Configer.https_first;
        if (U_S_S) {
            String url = "https://" + remoteHost + "/";
            if (remoteHost.endsWith(":443"))
                url = "https://" + remoteHost.substring(0, remoteHost.length() - 4) + "/";
            String time = String.valueOf(System.currentTimeMillis()).substring(0, 10) + "000";
            format = format.replaceAll("\\[K\\]", Utils.getKey(url, time))
                    .replaceAll("\\[T\\]", time);
        }
        format = format.replaceAll("\\[V\\]", "HTTP/1.0")
                .replaceAll("\\[M\\]", "CONNECT")
                .replaceAll("\\[U\\]", "/")
                .replaceAll("\\[H\\]", remoteHost)
                + "\r\n";
        log(format);
        byteBuffer.clear();
        byteBuffer.put(format.getBytes());
        byteBuffer.flip();
        if (write(byteBuffer, true)) {
            beginReceive();
        }
        DataUtils.setByteBuffer(byteBuffer);
    }

}
