package cn.EGGMaster.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.TextUtils;
import android.widget.Toast;

import cn.EGGMaster.R;
import cn.EGGMaster.core.LocalVpnService;
import cn.EGGMaster.util.DataUtils;

import static cn.EGGMaster.util.Utils.sendPost;

public class WelComeActivity extends Activity {

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

        if (DataUtils.APP_KEY == null)
            DataUtils.APP_KEY = getString(R.string.key);
        if (!DataUtils.initLocalData(this)) {
            Toast.makeText(this, "本地数据加载错误！请检查软件权限是否打开", Toast.LENGTH_LONG).show();
            return;
        }
        if (LocalVpnService.IsRunning)
            start();

        if (TextUtils.isEmpty(DataUtils.webVersion))
            DataUtils.webVersion = sendPost("getVersion", "version=" + DataUtils.versionName);
        if ("0".equals(DataUtils.webVersion)) {
            new AlertDialog.Builder(this)
                    .setTitle("提示")
                    .setMessage("当前版本需要更新，请联系应用提供商更新！")
                    .setPositiveButton(R.string.btn_ok, null)
                    .show();
        } else if ("1".equals(DataUtils.webVersion)) {
            getInfo();
        } else {
            Toast.makeText(this, "网络错误！", Toast.LENGTH_SHORT).show();
        }
    }

    private void getInfo() {
        DataUtils.initWebData();
        start();
    }

    private void start() {
        Intent intent = new Intent();
        intent.setClass(WelComeActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
