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
import static cn.EGGMaster.util.Utils.sendPost;

/**
 * Created by Fan on 2017/4/20.
 */

public class DataUtils {

    private static final Gson gson = new Gson();

    private static final Type type = new TypeToken<Map<String, String>>() {
    }.getType();

    public static Map<String, String> app;
    public static Map<String, String> user;
    public static Map<String, String> admin;

    public static String APP_KEY = null;

    public static String appInstallID = null;
    public static String versionName = null;
    public static String phoneNumber = null;
    public static String phoneIMEI = null;

    public static void intLocalData(Context context) {
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
            }
            if (isEmpty(phoneNumber)) {
                phoneNumber = tm.getLine1Number();
            }
        } catch (PackageManager.NameNotFoundException e) {
        }
    }

    public static void initWebData() {
        String result;
        if (admin == null || admin.size() == 0) {
            result = sendPost("getAdminInfo", "key=" + APP_KEY);
            admin = gson.fromJson(result, type);
        }
        if (app == null || app.size() == 0) {
            result = sendPost("getAppInfo", "id=" + admin.get("id"));
            app = gson.fromJson(result, type);
        }
        if (user == null || user.size() == 0) {
            result = sendPost("getUserInfo", "name=" + phoneIMEI, "pass=" + appInstallID,
                    "remark=" + phoneNumber, "u_id=" + admin.get("id"));
            user = gson.fromJson(result, type);
            if (StaticVal.IS_DEBUG)
                for (final Map.Entry<String, String> entry : user.entrySet()) {
                    System.out.println("user-->" + entry.getKey() + " : " + entry.getValue());
                }
        }
    }
}
