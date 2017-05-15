package cn.EGGMaster.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Fan on 2017/4/4.
 */

public class Utils {
    private static final String INDEX = StringCode.getStr(StaticVal.INDEX);

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
            HttpURLConnection conn = (HttpURLConnection) realUrl.openConnection();
            conn.setRequestMethod("POST");// 提交模式
            conn.setConnectTimeout(5000);//连接超时 单位毫秒
            conn.setReadTimeout(10000);//读取超时 单位毫秒
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
            //e.printStackTrace();
        } finally {
            try {
                if (in != null)
                    in.close();
                if (out != null)
                    out.close();
            } catch (Exception e) {
                //
            }
        }
        return result;
    }
}
