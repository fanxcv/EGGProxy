package cn.EGGMaster.tunnel;

import android.annotation.SuppressLint;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.EGGMaster.core.Configer;
import cn.EGGMaster.core.LocalVpnService;

import static android.text.TextUtils.isEmpty;

public abstract class Tunnel {

    final static ByteBuffer GL_BUFFER = ByteBuffer.allocate(8192);
    public static long SessionCount;

    protected abstract void onConnected(ByteBuffer buffer) throws Exception;

    protected abstract boolean isTunnelEstablished();

    protected abstract void beforeSend(ByteBuffer buffer) throws Exception;

    protected abstract void afterReceived(ByteBuffer buffer) throws Exception;

    protected abstract void onDispose();

    private String host;
    private String port;
    private String path;
    private String method;
    private String version;

    private StringBuffer header = new StringBuffer();

    private final String METHOD_GET = "GET";
    private final String METHOD_POST = "POST";

    private boolean isServer = false;
    private SocketChannel m_InnerChannel;
    private ByteBuffer m_SendRemainBuffer;
    private Selector m_Selector;
    private Tunnel m_BrotherTunnel;
    private boolean m_Disposed;
    private InetSocketAddress m_ServerEP;
    protected InetSocketAddress m_DestAddress;

    public Tunnel(SocketChannel innerChannel, Selector selector) {
        this.m_InnerChannel = innerChannel;
        this.m_Selector = selector;
        SessionCount++;
    }

    public Tunnel(InetSocketAddress serverAddress, Selector selector) throws IOException {
        SocketChannel innerChannel = SocketChannel.open();
        innerChannel.configureBlocking(false);
        this.m_InnerChannel = innerChannel;
        this.m_Selector = selector;
        this.m_ServerEP = serverAddress;
        SessionCount++;
    }

    public void setBrotherTunnel(Tunnel brotherTunnel, boolean isServer) {
        m_BrotherTunnel = brotherTunnel;
        this.isServer = isServer;
    }

    public void connect(InetSocketAddress destAddress) throws Exception {
        if (LocalVpnService.Instance.protect(m_InnerChannel.socket())) {//保护socket不走vpn
            m_DestAddress = destAddress;
            m_InnerChannel.register(m_Selector, SelectionKey.OP_CONNECT, this);//注册连接事件
            m_InnerChannel.connect(m_ServerEP);//连接目标
        } else {
            throw new Exception("VPN protect socket failed.");
        }
    }

    protected void beginReceive() throws Exception {
        if (m_InnerChannel.isBlocking()) {
            m_InnerChannel.configureBlocking(false);
        }
        m_InnerChannel.register(m_Selector, SelectionKey.OP_READ, this);//注册读事件
    }


    protected boolean write(ByteBuffer buffer, boolean copyRemainData) throws Exception {
        int bytesSent;
        while (buffer.hasRemaining()) {
            bytesSent = m_InnerChannel.write(buffer);
            if (bytesSent == 0) {
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

    protected void onTunnelEstablished() throws Exception {
        this.beginReceive();//开始接收数据
        m_BrotherTunnel.beginReceive();//兄弟也开始收数据吧
    }

    @SuppressLint("DefaultLocale")
    public void onConnectable() {
        try {
            if (m_InnerChannel.finishConnect()) {//连接成功
                onConnected(GL_BUFFER);//通知子类TCP已连接，子类可以根据协议实现握手等。
            } else {//连接失败
                this.dispose();
            }
        } catch (Exception e) {
            this.dispose();
        }
    }

    public void onReadable(SelectionKey key) {
        try {
            ByteBuffer buffer = GL_BUFFER;
            buffer.clear();
            int bytesRead = m_InnerChannel.read(buffer);
            if (bytesRead > 0) {
                buffer.flip();
                afterReceived(buffer);//先让子类处理，例如解密数据。
                if (isTunnelEstablished() && buffer.hasRemaining()) {//将读到的数据，转发给兄弟。
                    //m_BrotherTunnel.beforeSend(buffer);//发送之前，先让子类处理，例如做加密等。
                    if (!m_BrotherTunnel.write(HeaderProcess(buffer), true)) {
                        key.cancel();//兄弟吃不消，就取消读取事件。
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

    void disposeInternal(boolean disposeBrother) {
        if (m_Disposed) {
            return;
        } else {
            try {
                m_InnerChannel.close();
            } catch (Exception e) {
            }

            if (m_BrotherTunnel != null && disposeBrother) {
                m_BrotherTunnel.disposeInternal(false);//把兄弟的资源也释放了。
            }

            m_InnerChannel = null;
            m_SendRemainBuffer = null;
            m_Selector = null;
            m_BrotherTunnel = null;
            m_Disposed = true;
            SessionCount--;

            onDispose();
        }
    }

    private String getString(ByteBuffer buffer) {
        Charset charset;
        CharBuffer charBuffer;
        CharsetDecoder decoder;
        try {
            charset = Charset.forName("UTF-8");
            decoder = charset.newDecoder();
            charBuffer = decoder.decode(buffer.asReadOnlyBuffer());
            return charBuffer.toString();
        } catch (Exception ex) {
            return "";
        }
    }

    private ByteBuffer HeaderProcess(ByteBuffer buffer) {
        if (!isServer) {
            return buffer;
        }
        /*byte b;
        String s;
        ByteBuffer bc = ByteBuffer.allocate(8192);
        for (int i = 1; buffer.position() < buffer.limit(); i++) {
            bc.clear();
            for (; buffer.position() < buffer.limit(); ) {
                if ((b = buffer.get()) == 10) {
                    bc.flip();
                    break;
                }
                bc.put(b);
            }
            s = new String(bc.array(), 0, bc.limit());
            if (s.equals("\r")) {
                if (Utils.IS_DEBUG)
                    System.out.printf("到http请求末尾了，再见了，buffer一共长：%d;我读到了：%d\r\n", buffer.limit(), buffer.position());
                break;
            }
            if (isEmpty(s)) {
                if (Utils.IS_DEBUG)
                    System.out.println("字段为空，我要跳过了");
                continue;
            }
            if (Utils.IS_DEBUG)
                System.out.printf("原请求字段：Id:%d;内容：%s\r\n", i, s);
            if (i == 1 && (s.startsWith(METHOD_GET) || s.startsWith(METHOD_POST))) {
                header = new StringBuffer();
                addHeaderMethod(s);
            } else if (i == 1) {
                buffer.rewind();
                return buffer;
            }
            if (i > 1) {
                addHeaderString(s);
            }
        }
        buildHeader();
        if (Utils.IS_DEBUG)
            System.out.println("新的请求： " + header.toString());
        newByteBuffer.clear();
        newByteBuffer.put(ByteBuffer.wrap(header.toString().getBytes()));
        if (Utils.IS_DEBUG)
            System.out.printf("newByteBuffer的第一次位置： %d\r\n", newByteBuffer.position());
        if (buffer.position() < buffer.limit()) {
            newByteBuffer.put(buffer.array(), buffer.position(), buffer.limit() - buffer.position());
        }
        if (Utils.IS_DEBUG) {
            System.out.printf("newByteBuffer的第二次位置： %d\r\n", newByteBuffer.position());
            System.out.printf("buffer的长度： %d\r\n", buffer.position());
        }
        newByteBuffer.flip();
        if (Utils.IS_DEBUG) {
            System.out.printf("newByteBuffer的长度： %d\r\n", newByteBuffer.limit());
        }
        return newByteBuffer;*/

        String request = getString(buffer).trim();
        if (!isEmpty(request) && (request.startsWith(METHOD_GET) || request.startsWith(METHOD_POST))) {
            header.setLength(0);
            String[] herders = request.split("\\r\\n");
            addHeaderMethod(herders[0]);
            if (!isEmpty(method)) {
                for (int i = 1; i < herders.length; i++) {
                    addHeaderString(herders[i]);
                }
                buildHeader();
                return ByteBuffer.wrap(header.toString().getBytes());
            }
        }
        return buffer;
    }

    private void addHeaderMethod(String str) {
        str = str.trim();
        if (str.startsWith(METHOD_GET)) {
            method = METHOD_GET;
        } else if (str.startsWith(METHOD_POST)) {
            method = METHOD_POST;
        }
        if (!isEmpty(method)) {
            Pattern p = Pattern.compile("([a-zA-Z ]*?://)?([^/]*)(.*) (HTTP/.*)$");
            Matcher m = p.matcher(str);
            if (m.find()) {
                path = m.group(3);
                version = m.group(4);
            }
        }
    }

    private void addHeaderString(String str) {
        str = str.trim();
        if (str.startsWith("Host") || str.startsWith("host")) {
            String[] hosts = str.split(":");
            host = hosts[1].trim();
            port = hosts.length == 3 ? hosts[2] : "80";
        }
        if (str.indexOf(':') != -1) {
            String head = str.substring(0, str.indexOf(':'));
            for (final String s : Configer.http_del) {
                if (s.trim().equalsIgnoreCase(head)) {
                    return;
                }
            }
        }
        header.append(str).append("\r\n");
    }

    private void buildHeader() {
        if (port != null && !"80".equals(port)) {
            host = host + ":" + port;
        }
        header.insert(0,
                Configer.http_first.replaceAll("\\[M\\]", method)
                        .replaceAll("\\[V\\]", version)
                        .replaceAll("\\[H\\]", host)
                        .replaceAll("\\[U\\]", path)
        ).append("\r\n");
    }

}