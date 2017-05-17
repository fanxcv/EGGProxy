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
import static cn.EGGMaster.util.StaticVal.METHOD_GET;
import static cn.EGGMaster.util.StaticVal.METHOD_POST;

public class HttpTunnel extends Tunnel {

    private String host;
    private String path;
    private String method;
    private String version;

    private StringBuffer header;

    public HttpTunnel(InetSocketAddress serverAddress, Selector selector, String remoteHost) throws Exception {
        super(serverAddress, selector);
        this.host = remoteHost != null ? remoteHost : null;
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
    protected boolean isTunnelEstablished() {
        return true;
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
        String request = getString(buffer);
        if (!isEmpty(request) && getMethod(request)) {
            String[] herders = request.split("\\r\\n");
            header = new StringBuffer();
            addHeaderMethod(herders[0]);
            for (int i = 1; i < herders.length; i++) {
                addHeaderString(herders[i]);
            }
            buildHeader();
            return ByteBuffer.wrap(header.toString().getBytes());
        }
        return buffer;
    }

    private boolean getMethod(String str) {
        str = str.substring(0, 10).trim();
        if (str.startsWith(METHOD_GET)) {
            method = METHOD_GET;
            return true;
        } else if (str.startsWith(METHOD_POST)) {
            method = METHOD_POST;
            return true;
        }
        return false;
    }

    private void addHeaderMethod(String str) {
        if (!isEmpty(method)) {
            Pattern p = Pattern.compile("([a-zA-Z ]*?://)?([^/]*)(.*) (HTTP/.*)$");
            Matcher m = p.matcher(str);
            if (m.find()) {
                path = m.group(3).trim();
                version = m.group(4).trim();
            }
        }
    }

    private void addHeaderString(String str) {
        int i;
        if ((i = str.indexOf(':')) >= 0) {
            String head = str.substring(0, i).trim().toLowerCase(Locale.ENGLISH);
            if (host == null) {
                int j = str.length();
                if ("x-online-host".equals(head)) {
                    host = str.substring(++i, j);
                } else if ("host".equals(head)) {
                    host = str.substring(++i, j);
                }
            }
            String header = "'|" + head + "|'";
            if (Configer.http_del.contains(header)) {
                return;
            }
        }
        header.append(str.trim()).append("\r\n");
    }

    private void buildHeader() {
        header.insert(0, Configer.http_first
                .replaceAll("\\[M\\]", method)
                .replaceAll("\\[V\\]", version)
                .replaceAll("\\[H\\]", host)
                .replaceAll("\\[U\\]", path)
        ).append("\r\n");
    }

}
