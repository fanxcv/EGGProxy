package cn.EGGMaster.Proxy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

import cn.EGGMaster.core.Configer;
import cn.EGGMaster.tcpip.CommonMethods;
import cn.EGGMaster.util.DataUtils;

public class ConnectTunnel extends Tunnel {

    public ConnectTunnel(InetSocketAddress serverAddress) throws IOException {
        super(serverAddress);
    }

    @Override
    public void onReadable(SelectionKey key) {
        try {
            ByteBuffer buffer = DataUtils.getByteBuffer();
            //buffer.clear();
            int bytesRead = socket.read(buffer);
            if (bytesRead > 0) {
                buffer.flip();
                if (new String(buffer.array(), buffer.position(), 12).matches("^HTTP/1.[01] 200$")) {
                    other_Socket.beginReceive();
                } else {
                    return;
                }
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

    @Override
    public void onConnectable() {
        try {
            if (socket.finishConnect()) {//连接成功
                String format = Configer.instance.https_first
                        .replaceAll("\\[V\\]", "HTTP/1.0")
                        .replaceAll("\\[M\\]", "CONNECT")
                        .replaceAll("\\[U\\]", "/")
                        .replaceAll("\\[H\\]", CommonMethods.ipBytesToString(m_DestAddress.getAddress().getAddress())
                                + ":" + m_DestAddress.getPort())
                        + "\r\n";
                ByteBuffer byteBuffer = DataUtils.getByteBuffer();
                //byteBuffer.clear();
                byteBuffer.put(format.getBytes());
                byteBuffer.flip();
                if (write(byteBuffer, true)) {
                    DataUtils.setByteBuffer(byteBuffer);
                    beginReceive();
                }
            } else {//连接失败
                this.dispose();
            }
        } catch (Exception e) {
            this.dispose();
        }
    }
}
