package cn.EGGMaster.core;

import java.net.InetSocketAddress;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import cn.EGGMaster.tunnel.ConnectTunnel;
import cn.EGGMaster.tunnel.HttpTunnel;
import cn.EGGMaster.tunnel.RawTunnel;
import cn.EGGMaster.tunnel.Tunnel;

import static cn.EGGMaster.core.Configer.allHttps;
import static cn.EGGMaster.core.Configer.httpAddress;
import static cn.EGGMaster.core.Configer.httpsAddress;
import static cn.EGGMaster.core.Configer.isNet;

class TunnelFactory {

    static Tunnel wrap(SocketChannel channel, Selector selector) {
        return new RawTunnel(channel, selector);
    }

    static Tunnel createTunnelByConfig(InetSocketAddress destAddress, Selector selector, String remoteHost, boolean isHttps) throws Exception {
        /*if (Configer.noProxyListString.contains(destAddress.toString())) {
            return new RawTunnel(destAddress, selector);
        } else */
        if (isHttps || allHttps) {
            return new ConnectTunnel(httpsAddress, selector, remoteHost);
        } else if (isNet) {
            return new HttpTunnel(destAddress, selector, remoteHost);
        } else {
            return new HttpTunnel(httpAddress, selector, remoteHost);
        }
    }
}