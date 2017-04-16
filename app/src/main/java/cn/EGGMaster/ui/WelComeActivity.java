package cn.EGGMaster.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.widget.Toast;

import cn.EGGMaster.R;
import cn.EGGMaster.util.ActivityUserUtils;

public class WelComeActivity extends Activity {

    public static Activity instance;

    @Override
    public void onCreate(Bundle savedInstanceState) {
         /*
        4.0后在获取网页源码时需加这个，不然就报错android.os.NetworkOnMainThreadException
         */
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads().detectDiskWrites().detectNetwork()
                .penaltyLog().build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects().detectLeakedClosableObjects()
                .penaltyLog().penaltyDeath().build());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome);
        instance = this;
        String ban = ActivityUserUtils.getVersionName(this);
        String ba = "1";
        int b = 2;
        try {
            b = Integer.parseInt(ba);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        if (b == 0) {
            new AlertDialog.Builder(this)
                    .setTitle("提示")
                    .setMessage("当前版本已需要更新，请联系应用提供商更新！")
                    .setPositiveButton(R.string.btn_ok, null)
                    .show();
        } else if (b == 1) {
            Start();
        } else {
            Toast.makeText(this, "网络错误！", Toast.LENGTH_SHORT).show();
        }
    }

    public void Start() {
        new Thread() {
            public void run() {
                try {
                    Thread.sleep(2500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent();
                intent.setClass(WelComeActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                /*if (LoginActivity.check_login()) {
                    Intent intent = new Intent();
                    intent.setClass(WelComeActivity.this, MainActivity1.class);
                    startActivity(intent);
                    finish();
                } else {
                    Intent intent = new Intent();
                    intent.setClass(WelComeActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }*/
            }
        }.start();
    }
}
