package cn.wostore.auth;

public class WoJNIUtil {
    static {
        System.loadLibrary("XLibs");
    }

    public static native String getD(String str, String str2, String str3, String str4, String str5, String str6);

}
