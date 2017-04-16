package cn.EGGMaster.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;

import java.util.UUID;

/**
 * Created by Fan on 2017/4/4.
 */

public class ActivityUserUtils extends Activity {
    public static final boolean IS_DEBUG = true;
    private static String appInstallID = null;
    private static String versionName = null;

    /**
     * toast提醒
     */
    /*public static void ToastNotice(Context context, Object msg) {
        if (msg instanceof String) {
            Toast.makeText(context, (String) msg, Toast.LENGTH_SHORT).show();
        } else if (msg instanceof Integer) {
            Toast.makeText(context, (int) msg, Toast.LENGTH_SHORT).show();
        } else {
            if (IS_DEBUG) {
                Toast.makeText(context, "未知的提示错误： " + msg.toString(), Toast.LENGTH_SHORT).show();
            }
        }
    }*/

    /**
     * 获取软件版本号
     */
    public static String getVersionName(Context context) {
        if (versionName == null || versionName.isEmpty()) {
            PackageManager packageManager = context.getPackageManager();
            if (packageManager == null) {
                return null;
            }
            try {
                versionName = packageManager.getPackageInfo(context.getPackageName(), 0).versionName;
            } catch (PackageManager.NameNotFoundException e) {
                return null;
            }
        }
        return versionName;
    }


    /**
     * 获取安装ID
     */
    public static String getAppInstallID(Context context) {
        if (appInstallID == null || appInstallID.isEmpty()) {
            SharedPreferences preferences = context.getSharedPreferences("EggInfo", MODE_PRIVATE);
            appInstallID = preferences.getString("AppInstallID", null);
            if (appInstallID == null || appInstallID.isEmpty()) {
                appInstallID = UUID.randomUUID().toString();
                Editor editor = preferences.edit();
                editor.putString("AppInstallID", appInstallID);
                editor.commit();
            }
        }
        return appInstallID;
    }

    /**
     * 字符串判空
     */
    /*public static boolean isEmpty(String str) {
        if (str != null && !str.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }*/

    /**
     * 文件读取
     */
    /*public static boolean writeFile(String m_FileName, String text) {
        try {
            FileOutputStream fos = new FileOutputStream(m_FileName);
            fos.write(text.getBytes());
            fos.close();
        } catch (IOException e) {
        }
        return false;
    }*/

    /**
     * 文件读取
     */
    /*public static String ReadFile(String m_FileName) {
        try {
            FileInputStream fis = new FileInputStream(m_FileName);
            InputStreamReader isr = new InputStreamReader(fis);
            StringBuffer sb = new StringBuffer();
            char c[] = new char[8192];
            int i = -1;
            while ((i = isr.read(c)) != -1) {
                sb.append(c, 0, i);
            }
            isr.close();
            fis.close();
            return sb.toString();
        } catch (IOException e) {
            //e.printStackTrace();
        }
        return null;
    }*/

    /**
     * 文件删除
     */
    /*public static boolean deleteFileUtil(String filePath) {
        File file = new File(filePath);
        if (file.exists() && file.isFile()) {
            return file.delete();
        }
        return false;
    }*/

    /**
     * 复制资源文件
     */
    /*public static boolean copyRawFile(Context context, int fileId, String filePath) {
        try {
            deleteFileUtil(filePath);
            OutputStream os = new FileOutputStream(filePath);
            InputStream is = context.getResources().openRawResource(fileId);
//          InputStream is = context.getAssets().open(assetFile);
            byte[] buffer = new byte[4096];
            int length;
            while ((length = is.read(buffer)) != -1) {
                os.write(buffer, 0, length);
            }
            os.flush();
            is.close();
            os.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }*/


    /**
     * 检查文件属性
     */
    /*public static boolean CheckFile(String path, boolean canRead) {
        try {
            if (path == null || path.isEmpty())
                return false;
            File file = new File(path);
            if (!file.exists()) {
                return false;
            }
            if (canRead && !file.canRead()) {
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }*/

    /**
     * 创建文件
     */
    /*public static boolean createFile(String path, boolean isDir) {
        try {
            File file = new File(path);
            if (!file.exists()) {
                if (isDir) {
                    return file.mkdirs();
                } else {
                    return file.createNewFile();
                }
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }*/

    /**
     * 执行命令行
     */
    /*public static String execShell(String str, boolean callback) {
        try {
            Process exec = Runtime.getRuntime().exec(str);
            if (callback) {
                String line;
                StringBuffer sb = new StringBuffer();
                BufferedReader br = new BufferedReader(new InputStreamReader(exec.getInputStream()));
                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\r\n");
                }
                return sb.toString();
            } else {
                return "OK";
            }
        } catch (IOException e) {
            return null;
        }
    }*/
}
