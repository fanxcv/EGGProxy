package cn.wostore.auth;

public class WoJNIUtil {
    static {
        System.loadLibrary("XLibs");
    }

    public static String a( String str, String str2, String str3, String str4, String str5, String str6) {
        String str7 = "";
        try {
            return getD(str4, str3, str5, str6, str, str2);
        } catch (Exception e) {
            e.printStackTrace();
            return str7;
        }
    }
    public static native String getD(String str, String str2, String str3, String str4, String str5, String str6);

}
