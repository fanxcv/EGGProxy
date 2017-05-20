package cn.EGGMaster.core;

import java.net.InetSocketAddress;

import cn.EGGMaster.tcpip.CommonMethods;
import cn.EGGMaster.util.JniUtils;
import cn.EGGMaster.util.StaticVal;

import static android.text.TextUtils.isEmpty;

public class Configer {

    static boolean isNet = false;
    static boolean allHttps = false;

    static InetSocketAddress httpAddress;
    static InetSocketAddress httpsAddress;

    public static final Configer instance = new Configer();

    final static int FAKE_NETWORK_IP = CommonMethods.ipStringToInt("26.25.0.0");
    private final static int FAKE_NETWORK_MASK = CommonMethods.ipStringToInt("255.255.0.0");

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

        boolean res = JniUtils.loadConf(conf, Integer.parseInt(type));

        isNet = JniUtils.getConfBoolean(StaticVal.ISNET);
        allHttps = JniUtils.getConfBoolean(StaticVal.ALLHTTPS);

        String http_ip = JniUtils.getConfString(StaticVal.HTTP_IP);
        String https_ip = JniUtils.getConfString(StaticVal.HTTPS_IP);
        String http_port = JniUtils.getConfString(StaticVal.HTTP_PORT);
        String https_port = JniUtils.getConfString(StaticVal.HTTPS_PORT);
        if (!isEmpty(http_ip)) {
            httpAddress = new InetSocketAddress(http_ip, Integer.parseInt(http_port));
        }
        if (!isEmpty(https_ip)) {
            httpsAddress = new InetSocketAddress(https_ip, Integer.parseInt(https_port));
        }
        return res;
    }
}
