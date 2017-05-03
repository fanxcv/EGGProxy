package cn.EGGMaster.tunnel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;

import cn.EGGMaster.core.Configer;
import cn.EGGMaster.tcpip.CommonMethods;
import cn.EGGMaster.util.StaticVal;

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
                if (StaticVal.IS_DEBUG)
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
                .replaceAll("\\[V\\]", "HTTP/1.0")
                .replaceAll("\\[M\\]", "CONNECT")
                .replaceAll("\\[U\\]", "/")
                .replaceAll("\\[H\\]",
                        CommonMethods.ipBytesToString(this.m_DestAddress.getAddress().getAddress())
                                + ":" + this.m_DestAddress.getPort()
                )
                + "\r\n\r\n";
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
