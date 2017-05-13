package cn.EGGMaster.core;

import android.app.PendingIntent;
import android.content.Intent;
import android.net.VpnService;
import android.os.Handler;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.EGGMaster.R;
import cn.EGGMaster.dns.DnsPacket;
import cn.EGGMaster.tcpip.CommonMethods;
import cn.EGGMaster.tcpip.IPHeader;
import cn.EGGMaster.tcpip.TCPHeader;
import cn.EGGMaster.tcpip.UDPHeader;
import cn.EGGMaster.ui.MainActivity;


public class LocalVpnService extends VpnService implements Runnable {

    public static LocalVpnService Instance;
    public static boolean IsRunning = false;

    private static int LOCAL_IP;
    private static ConcurrentHashMap<onStatusChangedListener, Object> m_OnStatusChangedListeners = new ConcurrentHashMap<>();

    private Thread m_VPNThread;
    private ParcelFileDescriptor m_VPNInterface;
    private TcpProxyServer m_TcpProxyServer;
    private DnsProxy m_DnsProxy;
    private FileOutputStream m_VPNOutputStream;

    private byte[] m_Packet;
    private IPHeader m_IPHeader;
    private TCPHeader m_TCPHeader;
    private UDPHeader m_UDPHeader;
    private ByteBuffer m_DNSBuffer;
    private Handler m_Handler;
    //private long m_SentBytes;
    //private long m_ReceivedBytes;

    public LocalVpnService() {
        m_Handler = new Handler();
        m_Packet = new byte[8192];
        m_IPHeader = new IPHeader(m_Packet, 0);
        m_TCPHeader = new TCPHeader(m_Packet, 20);
        m_UDPHeader = new UDPHeader(m_Packet, 20);
        m_DNSBuffer = ((ByteBuffer) ByteBuffer.wrap(m_Packet).position(28)).slice();
        Instance = this;
    }

    @Override
    public void onCreate() {
        try {
            m_TcpProxyServer = new TcpProxyServer(1088);
            m_TcpProxyServer.start();
            writeLog("TCP服务已启动");

            m_DnsProxy = new DnsProxy();
            m_DnsProxy.start();
            writeLog("DNS服务已启动");
        } catch (Exception e) {
            writeLog("核心服务启动失败");
        }
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        IsRunning = true;
        // Start a new session by creating a new thread.
        m_VPNThread = new Thread(this, "VPNServiceThread");
        m_VPNThread.start();
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        String action = intent.getAction();
        if (action.equals(VpnService.SERVICE_INTERFACE)) {
            return super.onBind(intent);
        }
        return null;
    }

    public static void addOnStatusChangedListener(onStatusChangedListener listener) {
        if (!m_OnStatusChangedListeners.containsKey(listener)) {
            m_OnStatusChangedListeners.put(listener, 1);
        }
    }

    public static void removeOnStatusChangedListener(onStatusChangedListener listener) {
        if (m_OnStatusChangedListeners.containsKey(listener)) {
            m_OnStatusChangedListeners.remove(listener);
        }
    }

    private void onStatusChanged(final String status, final boolean isRunning) {
        m_Handler.post(new Runnable() {
            @Override
            public void run() {
                for (Map.Entry<onStatusChangedListener, Object> entry : m_OnStatusChangedListeners.entrySet()) {
                    entry.getKey().onStatusChanged(status, isRunning);
                }
            }
        });
    }

    public void writeLog(final String format, Object... args) {
        final String logString = String.format(format, args);
        m_Handler.post(new Runnable() {
            @Override
            public void run() {
                for (Map.Entry<onStatusChangedListener, Object> entry : m_OnStatusChangedListeners.entrySet()) {
                    entry.getKey().onLogReceived(logString);
                }
            }
        });
    }

    public void sendUDPPacket(IPHeader ipHeader, UDPHeader udpHeader) {
        try {
            CommonMethods.ComputeUDPChecksum(ipHeader, udpHeader);
            this.m_VPNOutputStream.write(ipHeader.m_Data, ipHeader.m_Offset, ipHeader.getTotalLength());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void run() {
        try {
            waitUntilPreapred();

            runVPN();
        } catch (Exception e) {
            //e.printStackTrace();
        }

        dispose();
    }

    private void runVPN() throws Exception {
        this.m_VPNInterface = establishVPN();
        this.m_VPNOutputStream = new FileOutputStream(m_VPNInterface.getFileDescriptor());
        try (FileInputStream in = new FileInputStream(m_VPNInterface.getFileDescriptor())) {
            while (IsRunning) {
                boolean idle = true;
                int size = in.read(m_Packet);
                if (size > 0) {
                    if (m_DnsProxy.Stopped || m_TcpProxyServer.Stopped) {
                        in.close();
                    }
                    try {
                        onIPPacketReceived(m_IPHeader, size);
                        idle = false;
                    } catch (IOException ex) {
                        //
                    }
                }
                if (idle) {
                    Thread.sleep(100);
                }
            }
        }
    }

    void onIPPacketReceived(IPHeader ipHeader, int size) throws IOException {
        switch (ipHeader.getProtocol()) {
            case IPHeader.TCP:
                TCPHeader tcpHeader = m_TCPHeader;
                tcpHeader.m_Offset = ipHeader.getHeaderLength();
                if (ipHeader.getSourceIP() == LOCAL_IP) {
                    if (tcpHeader.getSourcePort() == m_TcpProxyServer.Port) {
                        NatSession session = NatSessionManager.getSession(tcpHeader.getDestinationPort());
                        if (session != null) {
                            ipHeader.setSourceIP(ipHeader.getDestinationIP());
                            tcpHeader.setSourcePort(session.RemotePort);
                            ipHeader.setDestinationIP(LOCAL_IP);

                            CommonMethods.ComputeTCPChecksum(ipHeader, tcpHeader);
                            m_VPNOutputStream.write(ipHeader.m_Data, ipHeader.m_Offset, size);
                            //m_ReceivedBytes += size;
                        }
                    } else {
                        int portKey = tcpHeader.getSourcePort();
                        NatSession session = NatSessionManager.getSession(portKey);
                        if (session == null || session.RemoteIP != ipHeader.getDestinationIP() || session.RemotePort != tcpHeader.getDestinationPort()) {
                            session = NatSessionManager.createSession(portKey, ipHeader.getDestinationIP(), tcpHeader.getDestinationPort());
                        }

                        session.LastNanoTime = System.nanoTime();
                        session.PacketSent++;

                        int tcpDataSize = ipHeader.getDataLength() - tcpHeader.getHeaderLength();
                        if (session.PacketSent == 2 && tcpDataSize == 0) {
                            return;
                        }

                        if (session.BytesSent == 0 && tcpDataSize > 10) {
                            int dataOffset = tcpHeader.m_Offset + tcpHeader.getHeaderLength();
                            String host = HttpHostHeaderParser.parseHost(tcpHeader.m_Data, dataOffset, tcpDataSize);
                            if (host != null) {
                                session.RemoteHost = host;
                            }
                        }

                        ipHeader.setSourceIP(ipHeader.getDestinationIP());
                        ipHeader.setDestinationIP(LOCAL_IP);
                        tcpHeader.setDestinationPort(m_TcpProxyServer.Port);

                        CommonMethods.ComputeTCPChecksum(ipHeader, tcpHeader);
                        m_VPNOutputStream.write(ipHeader.m_Data, ipHeader.m_Offset, size);
                        session.BytesSent += tcpDataSize;
                        //m_SentBytes += size;
                    }
                }
                break;
            case IPHeader.UDP:
                UDPHeader udpHeader = m_UDPHeader;
                udpHeader.m_Offset = ipHeader.getHeaderLength();
                if (ipHeader.getSourceIP() == LOCAL_IP && udpHeader.getDestinationPort() == 53) {
                    m_DNSBuffer.clear();
                    m_DNSBuffer.limit(ipHeader.getDataLength() - 8);
                    DnsPacket dnsPacket = DnsPacket.FromBytes(m_DNSBuffer);
                    if (dnsPacket != null && dnsPacket.Header.QuestionCount > 0) {
                        m_DnsProxy.onDnsRequestReceived(ipHeader, udpHeader, dnsPacket);
                    }
                }
                break;
        }
    }

    private void waitUntilPreapred() {
        while (prepare(this) != null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // Ignore
            }
        }
    }

    private ParcelFileDescriptor establishVPN() throws Exception {
        NatSessionManager.clearAllSessions();

        Builder builder = new Builder();
        builder.setMtu(1500);
        builder.setSession("EGGProxyVpn");
        builder.addAddress("26.26.26.2", 32);
        builder.addDnsServer("119.29.29.29");
        builder.addDnsServer("114.114.115.115");

        for (int i = 1; i < 26; i++) {
            builder.addRoute(i + ".0.0.0", 8);
        }
        for (int i = 27; i < 127; i++) {
            builder.addRoute(i + ".0.0.0", 8);
        }
        for (int i = 128; i < 192; i++) {
            builder.addRoute(i + ".0.0.0", 8);
        }
        for (int i = 193; i < 256; i++) {
            builder.addRoute(i + ".0.0.0", 8);
        }
        for (int i = 1; i < 26; i++) {
            builder.addRoute("26." + i + ".0.0", 16);
        }
        for (int i = 27; i < 256; i++) {
            builder.addRoute("26." + i + ".0.0", 16);
        }
        for (int i = 1; i < 168; i++) {
            builder.addRoute("192." + i + ".0.0", 16);
        }
        for (int i = 169; i < 256; i++) {
            builder.addRoute("192." + i + ".0.0", 16);
        }

        LOCAL_IP = CommonMethods.ipStringToInt("26.26.26.2");

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            PackageManager packageManager = getPackageManager();
//            List<PackageInfo> list = packageManager.getInstalledPackages(0);
//            HashSet<String> packageSet = new HashSet<>();
//
//            for (int i = 0; i < list.size(); i++) {
//                PackageInfo info = list.get(i);
//                packageSet.add(info.packageName);
//            }
//
//            for (String name : getResources().getStringArray(R.array.bypass_package_name)) {
//                if (packageSet.contains(name)) {
//                    builder.addDisallowedApplication(name);
//                }
//            }
//        }

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        builder.setConfigureIntent(pendingIntent);

        ParcelFileDescriptor pfdDescriptor = builder.establish();
        onStatusChanged("VPN" + getString(R.string.vpn_connected_status), true);
        return pfdDescriptor;
    }

    public synchronized void dispose() {

        onStatusChanged("VPN" + getString(R.string.vpn_disconnected_status), false);

        IsRunning = false;

        try {
            if (m_VPNInterface != null) {
                m_VPNInterface.close();
                m_VPNInterface = null;
            }
        } catch (Exception e) {
            // ignore
        }

        try {
            if (m_VPNOutputStream != null) {
                m_VPNOutputStream.close();
                m_VPNOutputStream = null;
            }
        } catch (Exception e) {
            // ignore
        }

        if (m_VPNThread != null) {
            m_VPNThread.interrupt();
            m_VPNThread = null;
        }
    }

    @Override
    public void onDestroy() {
        if (IsRunning) dispose();
        try {
            // ֹͣTcpServer
            if (m_TcpProxyServer != null) {
                m_TcpProxyServer.stop();
                m_TcpProxyServer = null;
            }
        } catch (Exception e) {
            // ignore
        }
        try {
            // DnsProxy
            if (m_DnsProxy != null) {
                m_DnsProxy.stop();
                m_DnsProxy = null;
            }
        } catch (Exception e) {
            // ignore
        }
        super.onDestroy();
    }

    public interface onStatusChangedListener {
        void onStatusChanged(String status, Boolean isRunning);

        void onLogReceived(String logString);
    }

}
