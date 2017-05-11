package cn.EGGMaster.core;

import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

import cn.EGGMaster.Proxy.ConnectTunnel;
import cn.EGGMaster.Proxy.HttpTunnel;
import cn.EGGMaster.Proxy.Tunnel;

import static cn.EGGMaster.core.Configer.allHttps;
import static cn.EGGMaster.core.Configer.httpAddress;
import static cn.EGGMaster.core.Configer.httpsAddress;
import static cn.EGGMaster.core.Configer.isNet;

public class TunnelFactory {

    public static Tunnel wrap(SocketChannel channel) {
        return new Tunnel(channel);
    }

    public static Tunnel createTunnelByConfig(InetSocketAddress destAddress, boolean isHttps) throws Exception {
        if (isNoProxy(destAddress.toString())) {
            return new Tunnel(destAddress);
        } else if (isHttps || allHttps) {
            return new ConnectTunnel(httpsAddress);
        } else if (isNet) {
            return new HttpTunnel(destAddress);
        } else {
            return new HttpTunnel(httpAddress);
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