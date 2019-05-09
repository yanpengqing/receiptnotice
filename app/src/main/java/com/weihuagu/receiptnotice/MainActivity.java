package com.weihuagu.receiptnotice;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.github.pedrovgs.lynx.LynxActivity;
import com.github.pedrovgs.lynx.LynxConfig;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AsyncResponse {

    private static final String TAG = "MainActivity";
    private Toolbar myToolbar;
    private Button btnsetposturl;
    private FloatingActionButton btnshowlog;
    private EditText posturl;
    private SharedPreferences sp;
    private EditText mEdtToken;
    private Button btnSubmit;
    public  Boolean mIsRunning = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        PreferenceUtil preferenceUtil = new PreferenceUtil(this);
        String token = preferenceUtil.getToken();
        if (!TextUtils.isEmpty(token)){
            mIsRunning = true ;
            mEdtToken.setText(token);
        }
//        boolean messageServiceAlive = serviceAlive(NLService.class.getName());
//        Log.d("NLService","messageServiceAlive="+messageServiceAlive);
    }

    private void initView() {
        sp = getSharedPreferences("url", Context.MODE_PRIVATE);
        myToolbar = findViewById(R.id.my_toolbar);
        btnSubmit = findViewById(R.id.btn_submit);
        btnSubmit.setOnClickListener(this);
        setSupportActionBar(myToolbar);
        mEdtToken = ((TextInputLayout) findViewById(R.id.edt_act_main_token)).getEditText();
        btnsetposturl = findViewById(R.id.btnsetposturl);
        btnsetposturl.setOnClickListener(this);
        btnshowlog = findViewById(R.id.floatingshowlog);
        btnshowlog.setOnClickListener(this);
        posturl = findViewById(R.id.posturl);
        if (getPostUrl() != null)
            posturl.setText(getPostUrl());
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean isAuthor = isNotificationServiceEnable();
        if (!isAuthor) {
            //直接跳转通知授权界面
            //android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS是API 22才加入到Settings里，这里直接写死
            startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
        }
    }
    private boolean serviceAlive(String serviceName) {
        boolean isWork = false;
        ActivityManager myAM = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> myList = myAM.getRunningServices(100);
        if (myList.size() <= 0) {
            return false;
        }
        for (int i = 0; i < myList.size(); i++) {
            String mName = myList.get(i).service.getClassName().toString();
            if (mName.equals(serviceName)) {
                isWork = true;
                break;
            }
        }
        return isWork;
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * 是否已授权
     *
     * @return
     */
    private boolean isNotificationServiceEnable() {
        return NotificationManagerCompat.getEnabledListenerPackages(this).contains(getPackageName());
    }
    private boolean changeStatus() {
        mIsRunning = !mIsRunning;
        btnSubmit.setText(mIsRunning ? "重新配置" : "确认配置");
        if (!mIsRunning){
            //停止服务

        }
        return mIsRunning;
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_submit:
                if (mEdtToken.length() != 8) {
                    Toast.makeText(this, "唯一码只能为八位数字或字符！", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.equals(mEdtToken.getText().toString().trim(),new PreferenceUtil(this).getToken())){
                    return;
                }
//                if (!changeStatus()) {
//                    return;
//                }
                PreferenceUtil preferenceUtil = new PreferenceUtil(this);
                preferenceUtil.setToken(mEdtToken.getText().toString().trim());
                PostTask  task = new PostTask();
                String tasknum=RandomUtil.getRandomTaskNum();
                task.setRandomTaskNum(tasknum);
                task.setOnAsyncResponse(this);
                Map<String, String> tmpmap=new HashMap<>();
                tmpmap.put("url",getPostUrl()+Constants.URL_BIND);
                tmpmap.put("token",preferenceUtil.getToken());
                task.execute(tmpmap);

//        addStatusBar();

                break;
            case R.id.btnsetposturl:
                posturl.setHint(null);
                setPostUrl();
                break;
            case R.id.floatingshowlog:
                showLog();
                break;
        }
    }

    private void setPostUrl() {
        SharedPreferences.Editor edit = sp.edit();
        //通过editor对象写入数据
        edit.putString("posturl", posturl.getText().toString());
        //提交数据存入到xml文件中
        edit.apply();
        Toast.makeText(getApplicationContext(), "已经设置posturl为：" + posturl.getText().toString(),
                Toast.LENGTH_SHORT).show();
    }

    private String getPostUrl() {
        String posturlpath;
        posturlpath = sp.getString("posturl", "");
        if (posturlpath == null)
            return null;
        else
            return posturlpath;
    }


    private void showLog() {
        //startActivity(new Intent(this, LogActivity.class));
        openLynxActivity();
    }

    private void openLynxActivity() {
        LynxConfig lynxConfig = new LynxConfig();
        lynxConfig.setMaxNumberOfTracesToShow(4000)
                .setFilter("NLService");

        Intent lynxActivityIntent = LynxActivity.getIntent(this, lynxConfig);
        startActivity(lynxActivityIntent);
    }

    private void openSettingActivity() {
        Intent intent = new Intent(MainActivity.this, PreferenceActivity.class);
        startActivity(intent);
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // TODO Auto-generated method stub
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.main, menu);
//        return true;
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
                openSettingActivity();
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }
    /**
     * 在状态栏添加图标
     */
    private void addStatusBar() {
        NotificationManager manager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancelAll();

        PendingIntent pi = PendingIntent.getActivity(this, 0, getIntent(), 0);
        Notification noti = new Notification.Builder(this)
                .setTicker("程序启动成功")
                .setContentTitle("看到我，说明我在后台正常运行")
                .setContentText("始于：" + new SimpleDateFormat("MM-dd HH:mm:ss").format(new Date()))
                .setSmallIcon(R.mipmap.ic_launcher)//设置图标
                .setDefaults(Notification.DEFAULT_SOUND)//设置声音
                .setContentIntent(pi)//点击之后的页面
                .build();

        manager.notify(17952, noti);
    }

    @Override
    public void onDataReceivedSuccess(String[] returnstr) {
        LogUtil.postResultLog(returnstr[0],returnstr[1],returnstr[2]);
    }

    @Override
    public void onDataReceivedFailed(String[] returnstr) {
        LogUtil.postResultLog(returnstr[0],returnstr[1],returnstr[2]);
    }
}
