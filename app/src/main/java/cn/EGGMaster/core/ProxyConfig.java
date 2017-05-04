package cn.EGGMaster.core;

import cn.EGGMaster.tcpip.CommonMethods;


public class ProxyConfig {
    public static final ProxyConfig Instance = new ProxyConfig();
    public static String AppInstallID;
    public static String AppVersion;
    private final static int FAKE_NETWORK_MASK = CommonMethods.ipStringToInt("255.255.0.0");
    public final static int FAKE_NETWORK_IP = CommonMethods.ipStringToInt("10.231.0.0");

    /*HashMap<String, Boolean> m_DomainMap;

    public ProxyConfig() {
        m_DomainMap = new HashMap<String, Boolean>();
    }*/


    public static boolean isFakeIP(int ip) {
        return (ip & ProxyConfig.FAKE_NETWORK_MASK) == ProxyConfig.FAKE_NETWORK_IP;
    }

    /*private Boolean getDomainState(String domain) {
        domain = domain.toLowerCase();
        while (domain.length() > 0) {
            Boolean stateBoolean = m_DomainMap.get(domain);
            if (stateBoolean != null) {
                return stateBoolean;
            } else {
                int start = domain.indexOf('.') + 1;
                if (start > 0 && start < domain.length()) {
                    domain = domain.substring(start);
                } else {
                    return null;
                }
            }
        }
        return null;
    }*/

    public boolean needProxy(String host, int ip) {
        /*if (host != null) {
            Boolean stateBoolean = getDomainState(host);
            if (stateBoolean != null) {
                return stateBoolean;
            }
        }*/
        return isFakeIP(ip);
    }

}