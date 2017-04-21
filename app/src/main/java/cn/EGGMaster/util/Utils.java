package cn.EGGMaster.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;

import static cn.EGGMaster.util.StaticVal.INDEX;

/**
 * Created by Fan on 2017/4/4.
 */

public class Utils {

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
     * POST请求数据
     */
    public static String sendPost(String url, String... param) {
        String result = "";
        PrintWriter out = null;
        BufferedReader in = null;
        StringBuffer strs = new StringBuffer();
        try {
            URL realUrl = new URL(INDEX + url);
            // 打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
            // 设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            //处理参数
            for (String s : param) {
                String[] vals = s.split("=", 2);
                strs.append(vals[0]).append("=").append(StringCode.getInstance().encrypt(vals[1])).append("&");
            }
            // 获取URLConnection对象对应的输出流
            out = new PrintWriter(conn.getOutputStream());
            // 发送请求参数
            out.print(strs.toString());
            out.flush();
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return result;
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
