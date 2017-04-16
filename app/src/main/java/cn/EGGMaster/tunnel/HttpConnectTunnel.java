package cn.EGGMaster.tunnel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;

import cn.EGGMaster.core.Configer;
import cn.EGGMaster.tcpip.CommonMethods;
import cn.EGGMaster.util.ActivityUserUtils;

public class HttpConnectTunnel extends Tunnel {

    private boolean m_TunnelEstablished;

    public HttpConnectTunnel(InetSocketAddress serverAddress, Selector selector) throws IOException {
        super(serverAddress, selector);
    }

    @Override
    protected void afterReceived(ByteBuffer byteBuffer) throws Exception {
        if (!this.m_TunnelEstablished) {
            if (new String(byteBuffer.array(), byteBuffer.position(), 12).matches("^HTTP/1.[01] 200$")) {
                byteBuffer.limit(byteBuffer.position());
                this.m_TunnelEstablished = true;
                super.onTunnelEstablished();
                return;
            } else {
                if (ActivityUserUtils.IS_DEBUG)
                    throw new Exception(String.format("Proxy server responsed an error: %s", new Object[]{new String(byteBuffer.array(), byteBuffer.position(), 12)}));
            }
        }
    }

    @Override
    protected void beforeSend(ByteBuffer buffer) throws Exception {
    }

    @Override
    protected boolean isTunnelEstablished() {
        return this.m_TunnelEstablished;
    }

    @Override
    protected void onConnected(ByteBuffer byteBuffer) throws Exception {
        String format = Configer.instance.https_first
                .replaceAll("\\[M\\]", "CONNECT")
                .replaceAll("\\[V\\]", "HTTP/1.1")
                //.replaceAll("\\[H\\]", this.m_DestAddress.getHostName())
                .replaceAll("\\[H\\]",
                        CommonMethods.ipBytesToString(this.m_DestAddress.getAddress().getAddress())
                        + ":" + this.m_DestAddress.getPort()
                )
                .replaceAll("\\[U\\]", "/")
                + "Proxy-Connection: keep-alive\r\n"
                + "\r\n";
        byteBuffer.clear();
        byteBuffer.put(format.getBytes());
        byteBuffer.flip();
        if (write(byteBuffer, true)) {
            beginReceive();
        }
    }

    @Override
    protected void onDispose() {
    }

}
