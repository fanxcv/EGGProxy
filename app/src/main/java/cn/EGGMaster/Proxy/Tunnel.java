package cn.EGGMaster.Proxy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import cn.EGGMaster.core.LocalVpnService;
import cn.EGGMaster.util.DataUtils;

import static cn.EGGMaster.core.TcpProxyServer.m_Selector;

/**
 * Created by Administrator on 2017/5/11 0011.
 */

public class Tunnel {

    Tunnel other_Socket;
    SocketChannel socket;
    ByteBuffer m_SendRemainBuffer;
    InetSocketAddress m_DestAddress;

    private InetSocketAddress m_ServerAddress;

    public Tunnel(SocketChannel innerChannel) {
        this.socket = innerChannel;
    }

    public Tunnel(InetSocketAddress serverAddress) throws IOException {
        SocketChannel innerChannel = SocketChannel.open();
        innerChannel.configureBlocking(false);
        this.m_ServerAddress = serverAddress;
        this.socket = innerChannel;
    }

    public void setBrotherTunnel(Tunnel brotherTunnel) {
        other_Socket = brotherTunnel;
    }

    public void connect(InetSocketAddress destAddress) throws Exception {
        if (LocalVpnService.Instance.protect(socket.socket())) {//保护socket不走vpn
            socket.register(m_Selector, SelectionKey.OP_CONNECT, this);//注册连接事件
            socket.connect(m_ServerAddress);//连接目标
        }
        this.m_DestAddress = destAddress;
    }

    boolean write(ByteBuffer buffer, boolean copyRemainData) throws Exception {
        int bytesSent;
        while (buffer.hasRemaining()) {
            bytesSent = socket.write(buffer);
            if (bytesSent == 0) {
                break;//不能再发送了，终止循环
            }
        }

        if (buffer.hasRemaining()) {//数据没有发送完毕
            if (copyRemainData) {//拷贝剩余数据，然后侦听写入事件，待可写入时写入。
                //拷贝剩余数据
                if (m_SendRemainBuffer == null) {
                    m_SendRemainBuffer = DataUtils.getByteBuffer();
                }
                //m_SendRemainBuffer.clear();
                m_SendRemainBuffer.put(buffer);
                m_SendRemainBuffer.flip();
                socket.register(m_Selector, SelectionKey.OP_WRITE);//注册写事件
            }
            return false;
        } else {//发送完毕了
            return true;
        }
    }

    public void onReadable(SelectionKey key) {
        try {
            ByteBuffer buffer = DataUtils.getByteBuffer();
            //buffer.clear();
            int bytesRead = socket.read(buffer);
            if (bytesRead > 0) {
                buffer.flip();
                if (buffer.hasRemaining()) {//将读到的数据，转发给兄弟。
                    if (!other_Socket.write(buffer, true)) {
                        key.cancel();//兄弟吃不消，就取消读取事件。
                    } else {
                        other_Socket.beginReceive();
                    }
                }
            } else if (bytesRead < 0) {
                this.dispose();//连接已关闭，释放资源。
            }
        } catch (Exception e) {
            //e.printStackTrace();
            this.dispose();
        }
    }

    public void onWritable(SelectionKey key) {
        try {
            if (this.write(m_SendRemainBuffer, false)) {//如果剩余数据已经发送完毕
                key.cancel();//取消写事件。
                this.beginReceive();//这边数据发送完毕，通知兄弟可以收数据了。
            }
        } catch (Exception e) {
            this.dispose();
        }
    }

    public void onConnectable() {
        try {
            if (socket.finishConnect()) {//连接成功
                other_Socket.beginReceive();
            } else {//连接失败
                this.dispose();
            }
        } catch (Exception e) {
            this.dispose();
        }
    }

    void beginReceive() throws Exception {
        if (socket.isBlocking()) {
            socket.configureBlocking(false);
        }
        socket.register(m_Selector, SelectionKey.OP_READ, this);//注册读事件
    }

    public void dispose() {
        try {
            socket.close();
            socket = null;

            if (m_SendRemainBuffer != null) {
                DataUtils.setByteBuffer(m_SendRemainBuffer);
            }

            if (other_Socket != null) {
                other_Socket.dispose();
                other_Socket = null;//把兄弟的资源也释放了。
            }
        } catch (Exception e) {
        }
    }
}
