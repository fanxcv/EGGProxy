package cn.EGGMaster.util;

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

}
