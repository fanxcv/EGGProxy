package cn.EGGMaster.core;

import java.util.HashMap;

import cn.EGGMaster.tcpip.CommonMethods;


public class ProxyConfig {
    public static final ProxyConfig Instance = new ProxyConfig();
    public static String AppInstallID;
    public static String AppVersion;
    private final static int FAKE_NETWORK_MASK = CommonMethods.ipStringToInt("255.255.0.0");
    public final static int FAKE_NETWORK_IP = CommonMethods.ipStringToInt("10.231.0.0");

    //ArrayList<Config> m_ProxyList;
    HashMap<String, Boolean> m_DomainMap;

    //String m_session_name;

    //Timer m_Timer;

   /* public class IPAddress {
        public final String Address;
        public final int PrefixLength;

        public IPAddress(String address, int prefixLength) {
            this.Address = address;
            this.PrefixLength = prefixLength;
        }*/

        /*public IPAddress(String ipAddresString) {
            String[] arrStrings = ipAddresString.split("/");
            String address = arrStrings[0];
            int prefixLength = 32;
            if (arrStrings.length > 1) {
                prefixLength = Integer.parseInt(arrStrings[1]);
            }
            this.Address = address;
            this.PrefixLength = prefixLength;
        }*/
   // }

    public ProxyConfig() {
        //m_ProxyList = new ArrayList<Config>();
        m_DomainMap = new HashMap<String, Boolean>();

        //m_Timer = new Timer();
        //m_Timer.schedule(m_Task, 120000, 120000);//每两分钟刷新一次。
    }

    /*TimerTask m_Task = new TimerTask() {
        @Override
        public void run() {
            refreshProxyServer();//定时更新dns缓存
        }

        //定时更新dns缓存
        void refreshProxyServer() {
            try {
                for (int i = 0; i < m_ProxyList.size(); i++) {
                    try {
                        Config config = m_ProxyList.get(0);
                        InetAddress address = InetAddress.getByName(config.ServerAddress.getHostName());
                        if (address != null && !address.equals(config.ServerAddress.getAddress())) {
                            config.ServerAddress = new InetSocketAddress(address, config.ServerAddress.getPort());
                        }
                    } catch (Exception e) {
                    }
                }
            } catch (Exception e) {

            }
        }
    };*/


    public static boolean isFakeIP(int ip) {
        return (ip & ProxyConfig.FAKE_NETWORK_MASK) == ProxyConfig.FAKE_NETWORK_IP;
    }

    /*public Config getDefaultProxy() {
        if (m_ProxyList.size() > 0) {
            return m_ProxyList.get(0);
        } else {
            return null;
        }
    }*/

    public int getDnsTTL() {
        return 30;
    }

    public String getSessionName() {
        /*if (m_session_name == null) {
            m_session_name = getDefaultProxy().ServerAddress.getHostName();
        }
        return m_session_name;*/
        return "127.0.0.1";
    }

    private Boolean getDomainState(String domain) {
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
    }

    public boolean needProxy(String host, int ip) {
        if (host != null) {
            Boolean stateBoolean = getDomainState(host);
            if (stateBoolean != null) {
                return stateBoolean;
            }
        }
        return isFakeIP(ip);
    }

    //public void loadFromUrl() throws Exception {
        /*String[] lines = new String[]{};

        //m_ProxyList.clear();
        m_DomainMap.clear();

        for (String line : lines) {
            String[] items = line.split("\\s+");

            String tagString = items[0].toLowerCase(Locale.ENGLISH).trim();
            try {
                if (!tagString.startsWith("#")) {
                    if (tagString.equals("not_need")) {
                        addDomainToHashMap(items, 1, false);
                    }
                }
            } catch (Exception e) {
                throw new Exception(String.format("SmartProxy config file parse error: line:%d, tag:%s, error:%s", tagString, e));
            }
        }*/

        //查找默认代理。
        /*if (m_ProxyList.size() == 0) {
            tryAddProxy(lines);
        }*/
    //}

    /*private void tryAddProxy(String[] lines) {
        for (String line : lines) {
            Pattern p = Pattern.compile("proxy\\s+([^:]+):(\\d+)", Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(line);
            while (m.find()) {
                HttpConnectConfig config = new HttpConnectConfig();
                config.ServerAddress = new InetSocketAddress(m.group(1), Integer.parseInt(m.group(2)));
                if (!m_ProxyList.contains(config)) {
                    m_ProxyList.add(config);
                    m_DomainMap.put(config.ServerAddress.getHostName(), false);
                }
            }
        }
    }*/

    /*private void addDomainToHashMap(String[] items, int offset, Boolean state) {
        for (int i = offset; i < items.length; i++) {
            String domainString = items[i].toLowerCase().trim();
            if (domainString.charAt(0) == '.') {
                domainString = domainString.substring(1);
            }
            m_DomainMap.put(domainString, state);
        }
    }*/

}