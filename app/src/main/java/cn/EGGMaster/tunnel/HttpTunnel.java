package cn.EGGMaster.tunnel;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.EGGMaster.core.Configer;

import static android.text.TextUtils.isEmpty;

public class HttpTunnel extends Tunnel {

    private String host;
    private String port;
    private String path;
    private String method;
    private String version;

    private StringBuffer header = new StringBuffer();

    private final String METHOD_GET = "GET";
    private final String METHOD_POST = "POST";

    public HttpTunnel(InetSocketAddress serverAddress, Selector selector) throws Exception {
        super(serverAddress, selector);
    }

    @Override
    protected boolean write(ByteBuffer buffer, boolean copyRemainData) throws Exception {
        ByteBuffer m_Buffer = HeaderProcess(buffer);
        int bytesSent;
        while (m_Buffer.hasRemaining()) {
            bytesSent = m_InnerChannel.write(m_Buffer);
            if (bytesSent == 0) {
                break;//不能再发送了，终止循环
            }
        }

        if (m_Buffer.hasRemaining()) {//数据没有发送完毕
            if (copyRemainData) {//拷贝剩余数据，然后侦听写入事件，待可写入时写入。
                //拷贝剩余数据
                if (m_SendRemainBuffer == null) {
                    m_SendRemainBuffer = ByteBuffer.allocate(m_Buffer.capacity());
                }
                m_SendRemainBuffer.clear();
                m_SendRemainBuffer.put(m_Buffer);
                m_SendRemainBuffer.flip();
                m_InnerChannel.register(m_Selector, SelectionKey.OP_WRITE, this);//注册写事件
            }
            return false;
        } else {//发送完毕了
            return true;
        }
    }

    @Override
    protected void onConnected(ByteBuffer buffer) throws Exception {
        onTunnelEstablished();
    }

    @Override
    protected void afterReceived(ByteBuffer buffer) throws Exception {
    }

    @Override
    protected boolean isTunnelEstablished() {
        return true;
    }

    @Override
    protected void onDispose() {
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
        if (str.toLowerCase(Locale.ENGLISH).startsWith("host")) {
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
        header.insert(0, Configer.http_first
                .replaceAll("\\[M\\]", method)
                .replaceAll("\\[V\\]", version)
                .replaceAll("\\[H\\]", host)
                .replaceAll("\\[U\\]", path)
        ).append("\r\n");
    }

}
