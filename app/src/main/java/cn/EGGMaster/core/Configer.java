package cn.EGGMaster.core;

import java.io.IOException;
import java.io.StringReader;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

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

        Properties prof = new Properties();
        try {
            prof.load(new StringReader(conf));
        } catch (IOException e) {
            return false;
        }

        Enumeration<?> enumeration = prof.propertyNames();
        while (enumeration.hasMoreElements()){
            String key = ((String) enumeration.nextElement()).trim();
            String val = prof.getProperty(key,"").trim();
/*
            if (val.endsWith(";"))
                val = val.substring(0,val.length()-1);
            if (val.startsWith("\""))
                val = val.substring(1,val.length()-2);*/
           /* if (val.endsWith("\""))
                val = val.substring(0,val.length()-1);*/

            if (val.length()>0 && key.length()>0){
                switch (key.toLowerCase()){
                    case "mode":
                        mode = formatString(val);
                        break;
                    case "http_ip":
                        http_ip = formatString(val);
                        break;
                    case "http_port":
                        http_port = formatString(val);
                        break;
                    case "http_del":
                        http_del = formatString(val).split(",");
                        break;
                    case "http_first":
                        http_first = genericFirstLine(formatString(val));
                        break;
                    case "https_ip":
                        https_ip = formatString(val);
                        break;
                    case "https_port":
                        https_port = formatString(val);
                        break;
                    case "https_first":
                        https_first = genericFirstLine(formatString(val));
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
        if (isEmpty(http_first) || isEmpty(https_first))
            return false;
        return true;
    }

    private String formatString(String str) {
        if (isEmpty(str))
            return "";
        /*str = str.trim();
        if (!isEmpty(str)) {
            String regex = "\"?([^\"]*)\"?;?$";
            Pattern patter = Pattern.compile(regex);
            Matcher matcher = patter.matcher(str);
            if (matcher.find()) {
                str = matcher.group(1);
            }
        }*/

        if (str.endsWith(";"))
            str = str.substring(0,str.length()-1);
        if (str.startsWith("\""))
            str = str.substring(1,str.length());
        if (str.endsWith("\""))
            str = str.substring(0,str.length()-1);
        return isEmpty(str) ? "" : str;
    }

    private String genericFirstLine(String str)
    {
        return str.replaceAll("\\[version\\]", "\\[V\\]")
            .replaceAll("\\[method\\]", "\\[M\\]")
            .replaceAll("\\[host\\]", "\\[H\\]")
            .replaceAll("\\[uri\\]", "\\[U\\]");
//        Properties方式读取配置时不需要处理\r\n；它们在读取时不会被转义
//            .replaceAll("\\\\r", "\r")
//            .replaceAll("\\\\n", "\n")
//            .replaceAll("\\\\t", "\t");
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
