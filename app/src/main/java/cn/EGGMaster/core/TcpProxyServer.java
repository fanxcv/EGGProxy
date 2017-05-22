package cn.EGGMaster.core;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import cn.EGGMaster.tunnel.ConnectTunnel;
import cn.EGGMaster.tunnel.HttpTunnel;
import cn.EGGMaster.tunnel.Tunnel;

import static cn.EGGMaster.core.Configer.allHttps;
import static cn.EGGMaster.core.Configer.httpAddress;
import static cn.EGGMaster.core.Configer.httpsAddress;
import static cn.EGGMaster.core.Configer.isNet;


class TcpProxyServer implements Runnable {

    boolean Stopped;
    short Port = 1088;

    private Selector m_Selector;
    private ServerSocketChannel m_ServerSocketChannel;

    TcpProxyServer() throws IOException {
        m_Selector = Selector.open();
        m_ServerSocketChannel = ServerSocketChannel.open();
        m_ServerSocketChannel.configureBlocking(false);
        m_ServerSocketChannel.socket().bind(new InetSocketAddress(Port));
        m_ServerSocketChannel.register(m_Selector, SelectionKey.OP_ACCEPT);
    }

    synchronized void start() {
        Thread m_ServerThread = new Thread(this);
        m_ServerThread.setName("TcpProxyServerThread");
        m_ServerThread.start();
    }

    synchronized void stop() {
        this.Stopped = true;
        if (m_Selector != null) {
            try {
                m_Selector.close();
            } catch (Exception e) {
                //
            } finally {
                m_Selector = null;
            }
        }

        if (m_ServerSocketChannel != null) {
            try {
                m_ServerSocketChannel.close();
            } catch (Exception e) {
                //
            } finally {
                m_ServerSocketChannel = null;
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
                            //
                        }
                    }
                    keyIterator.remove();
                }
            }
        } catch (Exception e) {
            //
        } finally {
            this.stop();
        }
    }

    private NatSession getNatSession(SocketChannel localChannel) {
        return NatSessionManager.getSession((short) localChannel.socket().getPort());
    }

    private InetSocketAddress getDestAddress(NatSession session, SocketChannel localChannel) {
        if (session != null) {
            if (Configer.instance.needProxy(session.RemoteIP)) {
                return InetSocketAddress.createUnresolved(session.RemoteHost, session.RemotePort & 0xFFFF);
            } else {
                return new InetSocketAddress(localChannel.socket().getInetAddress(), session.RemotePort & 0xFFFF);
            }
        }
        return null;
    }

    private void onAccepted() {
        Tunnel localTunnel = null;
        try {
            SocketChannel localChannel = m_ServerSocketChannel.accept();
            localTunnel = new Tunnel(localChannel, m_Selector);

            NatSession session = getNatSession(localChannel);
            InetSocketAddress destAddress = getDestAddress(session, localChannel);
            if (destAddress != null) {
                Tunnel remoteTunnel;
                if (destAddress.getPort() != 80 || allHttps) {
                    remoteTunnel = new ConnectTunnel(httpsAddress, m_Selector, session.RemoteHost);
                } else if (isNet) {
                    remoteTunnel = new HttpTunnel(destAddress, m_Selector);
                } else {
                    remoteTunnel = new HttpTunnel(httpAddress, m_Selector);
                }
                remoteTunnel.setBrotherTunnel(localTunnel);
                localTunnel.setBrotherTunnel(remoteTunnel);
                remoteTunnel.connect(destAddress);
            } else {
                localTunnel.dispose();
            }
        } catch (Exception e) {
            //e.printStackTrace();
            if (localTunnel != null) {
                localTunnel.dispose();
            }
        }
    }

}
