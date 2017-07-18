package cn.EGGMaster.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Map;

import cn.EGGMaster.R;
import cn.EGGMaster.core.Configer;
import cn.EGGMaster.core.LocalVpnService;
import cn.EGGMaster.util.StaticVal;
import cn.EGGMaster.util.StringCode;
import cn.EGGMaster.util.Utils;

import static android.text.TextUtils.isEmpty;
import static cn.EGGMaster.util.DataUtils.TYPE;
import static cn.EGGMaster.util.DataUtils.admin;
import static cn.EGGMaster.util.DataUtils.app;
import static cn.EGGMaster.util.DataUtils.appInstallID;
import static cn.EGGMaster.util.DataUtils.gson;
import static cn.EGGMaster.util.DataUtils.initBufferPool;
import static cn.EGGMaster.util.DataUtils.phoneIMEI;
import static cn.EGGMaster.util.DataUtils.user;
import static cn.EGGMaster.util.DataUtils.versionName;
import static cn.EGGMaster.util.InetUtils.getLocalHost;
import static cn.EGGMaster.util.Utils.sendPost;

public class MainActivity extends Activity implements
        OnCheckedChangeListener,
        LocalVpnService.onStatusChangedListener {

    private static String GL_HISTORY_LOGS;

    private static final int START_VPN_SERVICE_REQUEST_CODE = 1985;

    private TextView info;
    private Switch switchProxy;
    private TextView textViewLog;
    private ScrollView scrollViewLog;
    private Calendar mCalendar;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scrollViewLog = (ScrollView) findViewById(R.id.scrollViewLog);
        textViewLog = (TextView) findViewById(R.id.textViewLog);
        TextView explain = (TextView) findViewById(R.id.explain);
        info = (TextView) findViewById(R.id.info);

        textViewLog.setText(GL_HISTORY_LOGS);
        scrollViewLog.fullScroll(ScrollView.FOCUS_DOWN);

        preferences = getSharedPreferences("EggInfo", MODE_PRIVATE);

        explain.setText(getText(R.string.explain) + StringCode.getInstance().decrypt(app.get("AppExplain")) + "");

        changeView();

        initBufferPool(4);
        mCalendar = Calendar.getInstance();
        LocalVpnService.addOnStatusChangedListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        changeView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        changeView();
    }

    @Override
    @SuppressLint("DefaultLocale")
    public void onLogReceived(String logString) {
        mCalendar.setTimeInMillis(System.currentTimeMillis());
        logString = String.format("[%1$02d:%2$02d:%3$02d] %4$s\n",
                mCalendar.get(Calendar.HOUR_OF_DAY),
                mCalendar.get(Calendar.MINUTE),
                mCalendar.get(Calendar.SECOND),
                logString);
        if (StaticVal.IS_DEBUG)
            System.out.println(logString);

        if (textViewLog.getLineCount() > 200) {
            textViewLog.setText("");
        }
        textViewLog.append(logString);
        scrollViewLog.fullScroll(ScrollView.FOCUS_DOWN);
        GL_HISTORY_LOGS = textViewLog.getText() == null ? "" : textViewLog.getText().toString();
    }

    @Override
    public void onStatusChanged(String status, Boolean isRunning) {
        switchProxy.setEnabled(true);
        switchProxy.setChecked(isRunning);
        if (status != null) {
            onLogReceived(status);
            Toast.makeText(this, status, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (!isEmpty(user.get("time")) && !"timeError".equals(user.get("time"))) {
            if (Double.parseDouble(user.get("time")) > 0) {
                if (LocalVpnService.IsRunning != isChecked) {
                    switchProxy.setEnabled(false);
                    if (isChecked) {
                        Intent intent = LocalVpnService.prepare(this);
                        if (intent == null) {
                            startVPNService();
                            addIconToStatusbar();
                        } else {
                            startActivityForResult(intent, START_VPN_SERVICE_REQUEST_CODE);
                            addIconToStatusbar();
                        }
                    } else {
                        LocalVpnService.IsRunning = false;
                    }
                }
            } else {
                switchProxy.setChecked(false);
                Toast.makeText(this, "请先充值", Toast.LENGTH_SHORT).show();
            }
        } else {
            switchProxy.setChecked(false);
            Toast.makeText(this, "账号归属错误，请通过充值重置账号状态", Toast.LENGTH_SHORT).show();
        }
    }

    private void startVPNService() {
        textViewLog.setText("");
        GL_HISTORY_LOGS = null;
        onLogReceived("软件版本：" + versionName);
        String id = preferences.getString("lineId", null);
        if (isEmpty(id)) {
            onLogReceived("你还未选择线路，请先选择线路");
            runFalse();
            return;
        }
        String result = Utils.sendPost("getLine", "id=" + id, "name=" + phoneIMEI, "pass=" + appInstallID);
        Map<String, String> line = gson.fromJson(StringCode.getInstance().decrypt(result), TYPE);
        if (line != null && Configer.instance.readConf(line.get("value"), line.get("type") + "")) {
            onLogReceived("核心启动成功");
        } else {
            onLogReceived("核心加载配置文件失败，请稍后重试");
            runFalse();
            return;
        }
        onLogReceived("VPN启动中...");

        startService(new Intent(this, LocalVpnService.class));
    }

    private void runFalse() {
        switchProxy.post(new Runnable() {
            @Override
            public void run() {
                switchProxy.setChecked(false);
                switchProxy.setEnabled(true);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == START_VPN_SERVICE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                startVPNService();
            } else {
                switchProxy.setChecked(false);
                switchProxy.setEnabled(true);
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_actions, menu);

        MenuItem menuItem = menu.findItem(R.id.menu_item_switch);
        if (menuItem == null) {
            return false;
        }

        switchProxy = (Switch) menuItem.getActionView();
        if (switchProxy == null) {
            return false;
        }

        switchProxy.setChecked(LocalVpnService.IsRunning);
        switchProxy.setOnCheckedChangeListener(this);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_list:
                Intent lineIntent = new Intent(this, ListActivity.class);
                startActivity(lineIntent);
                return true;
            case R.id.menu_item_buy:
                String buyUrl = app.get("buyUrl");
                if (isEmpty(buyUrl)) {
                    Toast.makeText(this, "木有购买地址呀，亲", Toast.LENGTH_LONG).show();
                } else {
                    if (!buyUrl.startsWith("http://") && !buyUrl.startsWith("https://"))
                        buyUrl = "http://" + buyUrl;
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(buyUrl)));
                }
                return true;
            case R.id.menu_item_use:
                if (isEmpty(user.get("time")) || "timeError".equals(user.get("time"))) {
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.info_title)
                            .setMessage(R.string.use_code_info)
                            .setPositiveButton(R.string.btn_ok, new OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    useCode(1);
                                }
                            })
                            .setNegativeButton(R.string.btn_cancel, null)
                            .show();
                    return true;
                }
                useCode(0);
                return true;
            case R.id.menu_item_exit:
                if (!LocalVpnService.IsRunning) {
                    finish();
                    return true;
                }
                new AlertDialog.Builder(this)
                        .setTitle(R.string.menu_item_exit)
                        .setMessage(R.string.exit_confirm_info)
                        .setPositiveButton(R.string.btn_ok, new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                LocalVpnService.IsRunning = false;
                                LocalVpnService.Instance.dispose();
                                stopService(new Intent(MainActivity.this, LocalVpnService.class));
                                System.runFinalization();
                                System.exit(0);
                            }
                        })
                        .setNegativeButton(R.string.btn_cancel, null)
                        .show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * @param type 是否重置账号，0否，1是
     */
    private void useCode(final int type) {
        final EditText editText = new EditText(this);
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
        editText.setHint(getString(R.string.code_use));

        new AlertDialog.Builder(this)
                .setTitle(R.string.menu_item_use)
                .setView(editText)
                .setPositiveButton(R.string.btn_ok, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (editText.getText() == null) {
                            return;
                        }
                        String code = editText.getText().toString().trim();
                        if (!isEmpty(code) && code.length() >= 6 && code.length() <= 10) {
                            String result = sendPost("setCodeValue", "name=" + phoneIMEI, "code=" + code,
                                    "id=" + admin.get("id"), "type=" + type);
                            try {
                                Map<String, String> list = gson.fromJson(StringCode.getInstance().decrypt(result), TYPE);
                                user.put("due_time", list.get("due_time"));
                                user.put("time", list.get("time"));
                                changeView();
                            } catch (Exception e) {
                                onLogReceived("充值失败");
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "充值码错误", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    @Override
    protected void onDestroy() {
        LocalVpnService.removeOnStatusChangedListener(this);
        super.onDestroy();
    }

    private void changeView() {
        String sb = "本机地址：" + getLocalHost() + "\r\n" +
                "当前线路：" + (isEmpty(preferences.getString("lineName", null)) ? "未选择" : preferences.getString("lineName", null)) + "\r\n" +
                "到期时间：" + user.get("due_time") + "\r\n" +
                "剩余时间：" + ("timeError".equals(user.get("time")) ? "账号归属错误" : (user.get("time")) + "天");
        info.setText(sb);
    }

    //如果没有从状态栏中删除ICON，且继续调用addIconToStatusbar,则不会有任何变化.如果将notification中的resId设置不同的图标，则会显示不同的图标
    private void addIconToStatusbar() {
        //long[] vibrate = new long[]{0, 500, 1000, 1500};
        Intent mainIntent = new Intent(this, MainActivity.class);
        PendingIntent pending = PendingIntent.getActivity(this, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder builder = new Notification.Builder(MainActivity.this);

        builder.setSmallIcon(R.drawable.ic_launcher)// 设置图标
                .setWhen(System.currentTimeMillis())// 设置通知来到的时间
                .setContentTitle(getString(R.string.app_name))// 设置通知的标题
                .setContentText(getString(R.string.app_name) + "已连接")// 设置通知的内容
                .setTicker(getString(R.string.app_name) + "正在运行。。。")// 状态栏上显示
                .setContentIntent(pending)//设置点击事件
                //.setVibrate(vibrate)//设置震动
                .setOngoing(true);
        Notification notification = builder.build();

        notification.flags = Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;

        notificationManager.notify(R.drawable.ic_launcher, notification);
    }

}
