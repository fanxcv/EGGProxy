package cn.EGGMaster.core;

import java.net.InetSocketAddress;

import cn.EGGMaster.tcpip.CommonMethods;
import cn.EGGMaster.util.JniUtils;

import static android.text.TextUtils.isEmpty;

public class Configer {

    private String mode = "wap";

    private String http_ip;
    private String http_port;
    private String http_del;
    private String http_first;

    private String https_ip;
    private String https_port;
    private String https_first;

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
        String[] lines = conf.split("\\n");
        switch (type) {
            case "0":
                for (String line : lines) {
                    String[] params = line.split("=", 2);
                    switch (params[0].toLowerCase().trim()) {
                        case "mode":
                            mode = formatString(params[1]);
                            break;
                        case "http_ip":
                            http_ip = formatString(params[1]);
                            break;
                        case "http_port":
                            http_port = formatString(params[1]);
                            break;
                        case "http_del":
                            http_del = formatString(params[1]);
                            break;
                        case "http_first":
                            http_first = genericFirstLine(formatString(params[1]));
                            break;
                        case "https_ip":
                            https_ip = formatString(params[1]);
                            break;
                        case "https_port":
                            https_port = formatString(params[1]);
                            break;
                        case "https_first":
                            https_first = genericFirstLine(formatString(params[1]));
                            break;
                    }
                }
                break;
            case "1":
                for (String line : lines) {
                    line = line.trim();
                    if (line.contains("httpip")) {
                        String[] params = line.split("=", 2);
                        if (!"null".equals(params[1])) {
                            if (line.contains(":")) {
                                String[] param = params[1].split(":", 2);
                                http_ip = formatString(param[0]);
                                http_port = formatString(param[1]);
                            } else {
                                http_ip = formatString(params[1]);
                                http_port = "80";
                            }
                        } else
                            mode = "net";
                    } else if (line.contains("httpsip")) {
                        String[] params = line.split("=", 2);
                        if (!"null".equals(params[1]))
                            if (line.contains(":")) {
                                String[] param = params[1].split(":", 2);
                                https_ip = formatString(param[0]);
                                https_port = formatString(param[1]);
                            } else {
                                https_ip = formatString(params[1]);
                                https_port = "80";
                            }
                    } else if (line.contains("[MTD]")) {
                        http_first = genericFirstLine(formatString(line + "\\r\\n"));
                    } else if (line.contains("CONNECT")) {
                        https_first = genericFirstLine(formatString(line + "\\r\\n"));
                    }
                }
                http_del = "Host,X-Online-Host";
                break;
            default:
                return false;
        }
        if ("net".equals(mode))
            isNet = true;
        else if ("wap_https".equals(mode))
            allHttps = true;

        if (!isEmpty(http_ip)) {
            httpAddress = new InetSocketAddress(http_ip, Integer.parseInt(http_port));
        }
        if (!isEmpty(https_ip)) {
            httpsAddress = new InetSocketAddress(https_ip, Integer.parseInt(https_port));
        }
        return JniUtils.setVal(http_first, https_first, http_del) && !(isEmpty(http_first) || isEmpty(https_first));
    }

    private String formatString(String str) {
        str = str.trim();
        if (isEmpty(str))
            return "";
        if (str.endsWith(";"))
            str = str.substring(0, str.length() - 1);
        if (str.startsWith("\""))
            str = str.substring(1, str.length());
        if (str.endsWith("\""))
            str = str.substring(0, str.length() - 1);
        return isEmpty(str) ? "" : str;
    }

    private String genericFirstLine(String str) {
        return str.replaceAll("\\[version\\]", "\\[V\\]")
                .replaceAll("\\[method\\]", "\\[M\\]")
                .replaceAll("\\[host\\]", "\\[H\\]")
                .replaceAll("\\[uri\\]", "\\[U\\]")
                .replaceAll("\\[MTD\\]", "\\[M\\]")
                .replaceAll("\\[Rr\\]", "\r")
                .replaceAll("\\[Nn\\]", "\n")
                .replaceAll("\\[Tt\\]", "\t")
                .replaceAll("\\\\r", "\r")
                .replaceAll("\\\\n", "\n")
                .replaceAll("\\\\t", "\t");
    }

}
