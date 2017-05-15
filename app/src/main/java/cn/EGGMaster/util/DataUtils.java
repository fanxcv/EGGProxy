package cn.EGGMaster.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.telephony.TelephonyManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.TELEPHONY_SERVICE;
import static android.text.TextUtils.isEmpty;

/**
 * Created by Fan on 2017/4/20.
 */

public class DataUtils extends Utils {

    private static final BlockingQueue<ByteBuffer> byteBufferPool = new ArrayBlockingQueue<>(1024);
    private static final BlockingQueue<ByteBuffer> ConnBufferPool = new ArrayBlockingQueue<>(1024);

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

    public static void initBufferPool(int num) {
        ByteBuffer byteBuffer, connBuffer;
        for (int i = 0; i < num; i++) {
            byteBuffer = ByteBuffer.allocate(8192);
            byteBufferPool.offer(byteBuffer);
            connBuffer = ByteBuffer.allocate(1536);
            ConnBufferPool.offer(connBuffer);
        }
    }

    public static ByteBuffer getByteBuffer() {
        try {
            if (!byteBufferPool.isEmpty()) return byteBufferPool.take();
        } catch (Exception e) {
            //
        }
        return ByteBuffer.allocate(8192);
    }

    public static void setByteBuffer(ByteBuffer buffer) {
        buffer.clear();
        byteBufferPool.offer(buffer);
    }

    public static ByteBuffer getConnBuffer() {
        try {
            if (!ConnBufferPool.isEmpty()) return ConnBufferPool.take();
        } catch (Exception e) {
            //
        }
        return ByteBuffer.allocate(1536);
    }

    public static void setConnBuffer(ByteBuffer buffer) {
        buffer.clear();
        ConnBufferPool.offer(buffer);
    }

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
                    editor.apply();
                }
            }
            if (isEmpty(phoneIMEI)) {
                phoneIMEI = tm.getDeviceId();
                if (isEmpty(phoneIMEI) || "null".equals(phoneIMEI)) {
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
        return map == null || map.size() == 0;
    }
}
