package cn.EGGMaster.core;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.text.TextUtils.isEmpty;

public class Configer {

    private String mode = "";

    private String http_ip;
    private String http_port;
    public static String[] http_del;
    public static String http_first;

    private String https_ip;
    private String https_port;
    public static String https_first;

    protected static boolean isNet = false;

    protected static InetSocketAddress httpAddress;
    protected static InetSocketAddress httpsAddress;

    private Set<String> noProxyList = new HashSet<String>();

    private Configer() {
    }

    public String[] getNoProxyList() {
        if (noProxyList != null && noProxyList.size() != 0)
            return noProxyList.toArray(new String[noProxyList.size()]);
        return null;
    }

    public static final Configer instance = new Configer();

    /**
     * 读取配置文件生成对象
     */
    public boolean readConf(String conf) {
        if (isEmpty(conf)) {
            return false;
        }
        String[] lines = conf.split(";");
        for (String line : lines) {
            if (!line.startsWith("#")) {
                String[] params = line.split("=");
                if (params.length == 2)
                    switch (params[0].toLowerCase(Locale.ENGLISH).trim()) {
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
                            http_del = formatString(params[1]).split(",");
                            break;
                        case "http_first":
                            http_first = formatString(params[1])
                                    .replaceAll("\\[version\\]", "\\[V\\]")
                                    .replaceAll("\\[method\\]", "\\[M\\]")
                                    .replaceAll("\\[host\\]", "\\[H\\]")
                                    .replaceAll("\\[uri\\]", "\\[U\\]")
                                    .replaceAll("\\\\r", "\r")
                                    .replaceAll("\\\\n", "\n")
                                    .replaceAll("\\\\t", "\t");
                            break;
                        case "https_ip":
                            https_ip = formatString(params[1]);
                            break;
                        case "https_port":
                            https_port = formatString(params[1]);
                            break;
                        case "https_first":
                            https_first = formatString(params[1])
                                    .replaceAll("\\[version\\]", "\\[V\\]")
                                    .replaceAll("\\[method\\]", "\\[M\\]")
                                    .replaceAll("\\[host\\]", "\\[H\\]")
                                    .replaceAll("\\[uri\\]", "\\[U\\]")
                                    .replaceAll("\\\\r", "\r")
                                    .replaceAll("\\\\n", "\n")
                                    .replaceAll("\\\\t", "\t");
                            break;
                    }
            }
        }

        if ("net".equals(mode))
            isNet = true;

        noProxyList.add("127.0.");
        noProxyList.add("192.168.");
        if (!isEmpty(http_ip)) {
            noProxyList.add(http_ip);
            httpAddress = new InetSocketAddress(http_ip, Integer.parseInt(http_port));
        }
        if (!isEmpty(https_ip)) {
            noProxyList.add(https_ip);
            httpsAddress = new InetSocketAddress(https_ip, Integer.parseInt(https_port));
        }
        if (isEmpty(http_first) && isEmpty(https_first))
            return false;
        return true;
    }

    private String formatString(String str) {
        if (isEmpty(str))
            return "";
        str = str.trim();
        if (!isEmpty(str)) {
            String regex = "\"?([^\"]*)\"?;?$";
            Pattern patter = Pattern.compile(regex);
            Matcher matcher = patter.matcher(str);
            if (matcher.find()) {
                str = matcher.group(1);
            }
        }
        return str == null ? "" : str;
    }

    @Override
    public String toString() {
        return "\r\nmode='" + mode + '\'' +
                "\r\nhttp_ip='" + http_ip + '\'' +
                "\r\nhttp_port='" + http_port + '\'' +
                "\r\nhttp_del=" + Arrays.toString(http_del) +
                "\r\nhttp_first='" + http_first + '\'' +
                "\r\nhttps_ip='" + https_ip + '\'' +
                "\r\nhttps_port='" + https_port + '\'' +
                "\r\nhttps_first='" + https_first + '\'' +
                "\r\nnoProxyList=" + Arrays.toString(getNoProxyList());
    }
}
