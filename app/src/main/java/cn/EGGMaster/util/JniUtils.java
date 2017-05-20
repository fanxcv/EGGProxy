package cn.EGGMaster.util;

import android.content.Context;

/**
 * Created by Administrator on 2017/5/17 0017.
 */

public class JniUtils {

    static {
        System.loadLibrary("Core");
    }

    public static native boolean loadConf(String conf, int type);

    public static native String getConfString(int type);

    public static native boolean getConfBoolean(int type);

    public static native String getHost(String header);

    public static native String getHttpHeader(String header);

    public static native String getCoonHeader(String host);

    public static native String initCore(Utils utils, Context context);

}
