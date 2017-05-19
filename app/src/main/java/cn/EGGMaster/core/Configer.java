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

        boolean res = JniUtils.loadConf(conf, Integer.parseInt(type));

        isNet = JniUtils.getConfBoolean(StaticVal.ISNET);
        allHttps = JniUtils.getConfBoolean(StaticVal.ALLHTTPS);

        if (!isEmpty(JniUtils.getConfString(StaticVal.HTTP_IP))) {
            httpAddress = new InetSocketAddress(JniUtils.getConfString(StaticVal.HTTP_IP), Integer.parseInt(JniUtils.getConfString(StaticVal.HTTP_PORT)));
        }
        if (!isEmpty(JniUtils.getConfString(StaticVal.HTTPS_IP))) {
            httpsAddress = new InetSocketAddress(JniUtils.getConfString(StaticVal.HTTPS_IP), Integer.parseInt(JniUtils.getConfString(StaticVal.HTTPS_PORT)));
        }
        return res;
    }
}
