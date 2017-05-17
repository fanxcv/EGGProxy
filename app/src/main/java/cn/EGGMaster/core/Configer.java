package cn.EGGMaster.core;

import java.net.InetSocketAddress;

import cn.EGGMaster.tcpip.CommonMethods;
import cn.EGGMaster.util.JniUtils;
import cn.EGGMaster.util.StaticVal;

import static android.text.TextUtils.isEmpty;

public class Configer {

    public static String http_del;
    public static String http_first;
    public static String https_first;

    static boolean isNet = false;
    static boolean allHttps = false;

    static InetSocketAddress httpAddress;
    static InetSocketAddress httpsAddress;

    public static final Configer instance = new Configer();

    private final static int FAKE_NETWORK_MASK = CommonMethods.ipStringToInt("255.255.0.0");
    final static int FAKE_NETWORK_IP = CommonMethods.ipStringToInt("26.25.0.0");

    private Configer() {
    }

    static boolean isFakeIP(int ip) {
        return (ip & FAKE_NETWORK_MASK) == FAKE_NETWORK_IP;
    }

    boolean needProxy(int ip) {
        return isFakeIP(ip);
    }

    /**
     * 读取配置文件生成对象
     */
    public boolean readConf(String conf, String type) {
        if (isEmpty(conf)) {
            return false;
        }

        JniUtils.loadConf(conf, Integer.parseInt(type));
        http_del = JniUtils.getConfString(100133);
        http_first = JniUtils.getConfString(100131);
        https_first = JniUtils.getConfString(100132);

        if (!isEmpty(JniUtils.getConfString(StaticVal.HTTP_IP))) {
            httpAddress = new InetSocketAddress(JniUtils.getConfString(StaticVal.HTTP_IP), Integer.parseInt(JniUtils.getConfString(StaticVal.HTTP_PORT)));
        }
        if (!isEmpty(JniUtils.getConfString(StaticVal.HTTPS_IP))) {
            httpsAddress = new InetSocketAddress(JniUtils.getConfString(StaticVal.HTTPS_IP), Integer.parseInt(JniUtils.getConfString(StaticVal.HTTPS_PORT)));
        }

//                for (String line : lines) {
//                    line = line.trim();
//                    if (line.contains("httpip")) {
//                        String[] params = line.split("=", 2);
//                        if (!"null".equals(params[1])) {
//                            if (line.contains(":")) {
//                                String[] param = params[1].split(":", 2);
//                                http_ip = formatString(param[0]);
//                                http_port = formatString(param[1]);
//                            } else {
//                                http_ip = formatString(params[1]);
//                                http_port = "80";
//                            }
//                        } else
//                            mode = "net";
//                    } else if (line.contains("httpsip")) {
//                        String[] params = line.split("=", 2);
//                        if (!"null".equals(params[1]))
//                            if (line.contains(":")) {
//                                String[] param = params[1].split(":", 2);
//                                https_ip = formatString(param[0]);
//                                https_port = formatString(param[1]);
//                            } else {
//                                https_ip = formatString(params[1]);
//                                https_port = "80";
//                            }
//                    } else if (line.contains("[MTD]")) {
//                        http_first = genericFirstLine(formatString(line + "\\r\\n"));
//                    } else if (line.contains("CONNECT")) {
//                        https_first = genericFirstLine(formatString(line + "\\r\\n"));
//                    }
//                }
//                http_dels = new String[]{"Host", "X-Online-Host"};
        return !(isEmpty(http_first) || isEmpty(https_first));
    }

//    @Override
//    public String toString() {
//        return "\r\nmode='" + mode + '\'' +
//                "\r\nhttp_ip='" + http_ip + '\'' +
//                "\r\nhttp_port='" + http_port + '\'' +
//                "\r\nhttp_del=" + Arrays.toString(http_del) +
//                "\r\nhttp_first='" + http_first + '\'' +
//                "\r\nhttps_ip='" + https_ip + '\'' +
//                "\r\nhttps_port='" + https_port + '\'' +
//                "\r\nhttps_first='" + https_first + '\'' +
//                "\r\nnoProxyList=" + Arrays.toString(getNoProxyList());
//    }
}
