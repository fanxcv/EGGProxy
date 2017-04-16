package cn.EGGMaster.core;

import java.net.InetSocketAddress;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import cn.EGGMaster.tunnel.RawTunnel;
import cn.EGGMaster.tunnel.Tunnel;
import cn.EGGMaster.tunnel.HttpConnectTunnel;

import static cn.EGGMaster.core.Configer.httpAddress;
import static cn.EGGMaster.core.Configer.httpsAddress;
import static cn.EGGMaster.core.Configer.isNet;

public class TunnelFactory {

    public static Tunnel wrap(SocketChannel channel, Selector selector) {
        return new RawTunnel(channel, selector);
    }

    public static Tunnel createTunnelByConfig(InetSocketAddress destAddress, Selector selector, boolean isHttps) throws Exception {
        if (isNoProxy(destAddress.toString())) {
            return new RawTunnel(destAddress, selector);
        } else if (isHttps) {
            return new HttpConnectTunnel(httpsAddress, selector);
        } else if (isNet) {
            return new RawTunnel(destAddress, selector);
        } else {
            return new RawTunnel(httpAddress, selector);
        }
    }

    private static boolean isNoProxy(String addr) {
        //放行ip
        for (String str : Configer.instance.getNoProxyList()) {
            if (addr.contains(str)) {
                return true;
            }
        }
        return false;
    }
}