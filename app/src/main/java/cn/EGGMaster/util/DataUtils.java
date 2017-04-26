package cn.EGGMaster.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.telephony.TelephonyManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.UUID;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.TELEPHONY_SERVICE;
import static android.text.TextUtils.isEmpty;

/**
 * Created by Fan on 2017/4/20.
 */

public class DataUtils extends Utils {

    public static final Gson gson = new Gson();

    public static final Type TYPE = new TypeToken<Map<String, String>>() {
    }.getType();

    public static String webVersion = null;

    public static Map<String, String> app;
    public static Map<String, String> user;
    public static Map<String, String> admin;

    public static String APP_KEY = null;

    public static String appInstallID = null;
    public static String versionName = null;
    public static String phoneNumber = null;
    public static String phoneIMEI = null;

    public static boolean initLocalData(Context context) {
        try {
            SharedPreferences preferences = context.getSharedPreferences("EggInfo", MODE_PRIVATE);
            TelephonyManager tm = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
            PackageManager packageManager = context.getPackageManager();
            if (isEmpty(versionName)) {
                versionName = packageManager.getPackageInfo(context.getPackageName(), 0).versionName;
            }
            if (isEmpty(appInstallID)) {
                appInstallID = preferences.getString("AppInstallID", null);
                if (isEmpty(appInstallID)) {
                    appInstallID = UUID.randomUUID().toString();
                    Editor editor = preferences.edit();
                    editor.putString("AppInstallID", appInstallID);
                    editor.commit();
                }
            }
            if (isEmpty(phoneIMEI)) {
                phoneIMEI = tm.getDeviceId();
                if (isEmpty(phoneIMEI) || "null".equalsIgnoreCase(phoneIMEI)) {
                    return false;
                }
            }
            if (isEmpty(phoneNumber)) {
                phoneNumber = tm.getLine1Number();
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static void initWebData() {
        if (isNullMap(admin) || isNullMap(app) || isNullMap(user)) {
            String result = sendPost("getWebInfo", "name=" + phoneIMEI, "pass=" + appInstallID,
                    "remark=" + phoneNumber, "key=" + APP_KEY);
            Map<String, String> list = gson.fromJson(StringCode.getInstance().decrypt(result), TYPE);
            app = gson.fromJson(list.get("app"), TYPE);
            user = gson.fromJson(list.get("user"), TYPE);
            admin = gson.fromJson(list.get("admin"), TYPE);
        }
    }

    private static boolean isNullMap(Map<String, String> map) {
        if (map == null || map.size() == 0)
            return true;
        return false;
    }
}
