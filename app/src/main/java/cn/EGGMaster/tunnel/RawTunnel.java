package cn.EGGMaster.tunnel;

import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class RawTunnel extends Tunnel {

    public RawTunnel(SocketChannel innerChannel, Selector selector) {
        super(innerChannel, selector);
    }

    @Override
    protected boolean isTunnelEstablished() {
        return true;
    }

}
