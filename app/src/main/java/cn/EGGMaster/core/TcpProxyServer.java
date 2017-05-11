package cn.EGGMaster.core;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import cn.EGGMaster.Proxy.Tunnel;

public class TcpProxyServer implements Runnable {

    boolean Stopped;
    short Port = 1088;

    public static Selector m_Selector;
    private ServerSocketChannel m_ServerSocketChannel;
    private Thread m_ServerThread;

    TcpProxyServer() throws IOException {
        m_Selector = Selector.open();
        m_ServerSocketChannel = ServerSocketChannel.open();
        m_ServerSocketChannel.configureBlocking(false);
        m_ServerSocketChannel.socket().bind(new InetSocketAddress(Port));
        m_ServerSocketChannel.register(m_Selector, SelectionKey.OP_ACCEPT);
    }

    private InetSocketAddress getDestAddress(SocketChannel localChannel) {
        short portKey = (short) localChannel.socket().getPort();
        NatSession session = NatSessionManager.getSession(portKey);
        if (session != null) {
            if (ProxyConfig.Instance.needProxy(session.RemoteHost, session.RemoteIP)) {
                return InetSocketAddress.createUnresolved(session.RemoteHost, session.RemotePort & 0xFFFF);
            } else {
                return new InetSocketAddress(localChannel.socket().getInetAddress(), session.RemotePort & 0xFFFF);
            }
        }
        return null;
    }

    void start() {
        m_ServerThread = new Thread(this);
        m_ServerThread.setName("TcpProxyServerThread");
        m_ServerThread.start();
    }

    void stop() {
        this.Stopped = true;
        if (m_Selector != null) {
            try {
                m_Selector.close();
                m_Selector = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (m_ServerSocketChannel != null) {
            try {
                m_ServerSocketChannel.close();
                m_ServerSocketChannel = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                m_Selector.select();
                Iterator<SelectionKey> keyIterator = m_Selector.selectedKeys().iterator();
                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    if (key.isValid()) {
                        try {
                            if (key.isReadable()) {
                                ((Tunnel) key.attachment()).onReadable(key);
                            } else if (key.isWritable()) {
                                ((Tunnel) key.attachment()).onWritable(key);
                            } else if (key.isConnectable()) {
                                ((Tunnel) key.attachment()).onConnectable();
                            } else if (key.isAcceptable()) {
                                onAccepted();
                            }
                        } catch (Exception e) {
                        }
                    }
                    keyIterator.remove();

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.stop();
        }
    }


    private void onAccepted() {
        Tunnel localTunnel = null;
        try {
            SocketChannel localChannel = m_ServerSocketChannel.accept();
            localChannel.configureBlocking(false);
            localTunnel = TunnelFactory.wrap(localChannel);

            InetSocketAddress destAddress = getDestAddress(localChannel);
            if (destAddress != null) {
                Tunnel remoteTunnel = TunnelFactory.createTunnelByConfig(destAddress, destAddress.getPort() != 80);
                remoteTunnel.setBrotherTunnel(localTunnel);//关联兄弟
                localTunnel.setBrotherTunnel(remoteTunnel);//关联兄弟
                remoteTunnel.connect(destAddress);//开始连接
            } else {
                localTunnel.dispose();
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (localTunnel != null) {
                localTunnel.dispose();
            }
        }
    }

}