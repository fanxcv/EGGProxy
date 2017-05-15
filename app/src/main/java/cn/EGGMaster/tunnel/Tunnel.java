package cn.EGGMaster.tunnel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import cn.EGGMaster.core.LocalVpnService;
import cn.EGGMaster.util.DataUtils;

public abstract class Tunnel {

    protected abstract boolean isTunnelEstablished();

    Selector m_Selector;
    Tunnel m_BrotherTunnel;
    SocketChannel m_InnerChannel;
    ByteBuffer m_SendRemainBuffer;
    InetSocketAddress m_DestAddress;

    private boolean m_Disposed;
    private InetSocketAddress m_ServerIP;

    public Tunnel(SocketChannel innerChannel, Selector selector) {
        this.m_InnerChannel = innerChannel;
        this.m_Selector = selector;
    }

    public Tunnel(InetSocketAddress serverAddress, Selector selector) throws IOException {
        SocketChannel innerChannel = SocketChannel.open();
        innerChannel.configureBlocking(false);
        this.m_InnerChannel = innerChannel;
        this.m_ServerIP = serverAddress;
        this.m_Selector = selector;
    }

    public void setBrotherTunnel(Tunnel brotherTunnel) {
        m_BrotherTunnel = brotherTunnel;
    }

    public void connect(InetSocketAddress destAddress) throws Exception {
        if (LocalVpnService.Instance.protect(m_InnerChannel.socket())) {//保护socket不走vpn
            m_InnerChannel.register(m_Selector, SelectionKey.OP_CONNECT, this);//注册连接事件
            m_InnerChannel.connect(m_ServerIP);//连接目标
            m_DestAddress = destAddress;
        }
    }

    void beginReceive() throws Exception {
        if (m_InnerChannel.isBlocking()) {
            m_InnerChannel.configureBlocking(false);
        }
        m_InnerChannel.register(m_Selector, SelectionKey.OP_READ, this);//注册读事件
    }


    protected boolean write(ByteBuffer buffer, boolean copyRemainData) throws Exception {
        while (buffer.hasRemaining()) {
            if (m_InnerChannel.write(buffer) == 0) {
                break;//不能再发送了，终止循环
            }
        }

        if (buffer.hasRemaining()) {//数据没有发送完毕
            if (copyRemainData) {//拷贝剩余数据，然后侦听写入事件，待可写入时写入。
                //拷贝剩余数据
                if (m_SendRemainBuffer == null) {
                    m_SendRemainBuffer = ByteBuffer.allocate(buffer.capacity());
                }
                m_SendRemainBuffer.clear();
                m_SendRemainBuffer.put(buffer);
                m_SendRemainBuffer.flip();
                m_InnerChannel.register(m_Selector, SelectionKey.OP_WRITE, this);//注册写事件
            }
            return false;
        } else {//发送完毕了
            return true;
        }
    }

    void onTunnelEstablished() throws Exception {
        this.beginReceive();//开始接收数据
        m_BrotherTunnel.beginReceive();//兄弟也开始收数据吧
    }

    public void onConnectable() {
        try {
            if (m_InnerChannel.finishConnect()) {//连接成功
                onTunnelEstablished();
            } else {//连接失败
                this.dispose();
            }
        } catch (Exception e) {
            this.dispose();
        }
    }

    public void onReadable(SelectionKey key) {
        ByteBuffer buffer = DataUtils.getByteBuffer();
        try {
            int bytesRead = m_InnerChannel.read(buffer);
            if (bytesRead > 0) {
                buffer.flip();
                if (buffer.hasRemaining()) {//将读到的数据，转发给兄弟。
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

    public void onWritable(SelectionKey key) {
        try {
            //this.beforeSend(m_SendRemainBuffer);//发送之前，先让子类处理，例如做加密等。
            if (this.write(m_SendRemainBuffer, false)) {//如果剩余数据已经发送完毕
                key.cancel();//取消写事件。
                if (isTunnelEstablished()) {
                    m_BrotherTunnel.beginReceive();//这边数据发送完毕，通知兄弟可以收数据了。
                } else {
                    this.beginReceive();//开始接收代理服务器响应数据
                }
            }
        } catch (Exception e) {
            this.dispose();
        }
    }

    public void dispose() {
        disposeInternal(true);
    }

    private void disposeInternal(boolean disposeBrother) {
        if (!m_Disposed) {
            try {
                m_InnerChannel.close();
            } catch (Exception e) {
                //
            }

            if (m_BrotherTunnel != null && disposeBrother) {
                m_BrotherTunnel.disposeInternal(false);//把兄弟的资源也释放了。
            }

            m_InnerChannel = null;
            m_SendRemainBuffer = null;
            m_Selector = null;
            m_BrotherTunnel = null;
            m_Disposed = true;
        }
    }

}