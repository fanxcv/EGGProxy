package cn.EGGMaster.util;

import android.content.Context;

public class JniUtils {

    static {
        System.loadLibrary("Core");
    }

    public static native boolean setVal(String http_first, String https_first, String http_del);

    public static native String getConfString(int type);

    public static native String getHost(String header);

    public static native String getHttpHeader(String header);

    public static native String getCoonHeader(String host, String port);

    public static native String initCore(Context context);

}
