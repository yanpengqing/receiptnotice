package com.weihuagu.receiptnotice;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.github.pedrovgs.lynx.LynxActivity;
import com.github.pedrovgs.lynx.LynxConfig;
import com.weihuagu.receiptnotice.core.AsyncResponse;
import com.weihuagu.receiptnotice.core.Constants;
import com.weihuagu.receiptnotice.core.PostTask;
import com.weihuagu.receiptnotice.utils.LogUtil;
import com.weihuagu.receiptnotice.utils.PreferenceUtil;
import com.weihuagu.receiptnotice.utils.RandomUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AsyncResponse {

    private static final String TAG = "MainActivity";
    private Toolbar myToolbar;
    private Button btnsetposturl;
    private RelativeLayout mRlFloat;
    private RelativeLayout mRlright;
    private FloatingActionButton btnshowlog;
    private EditText posturl;
    private SharedPreferences sp;
    private EditText mEdtToken;
    private Button btnSubmit;
    public  Boolean mIsRunning = false;
    final static int COUNTS = 5;//点击次数
    final static long DURATION = 1100;//规定有效时间
    long[] mHits = new long[COUNTS];
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
        boolean showlog = preferenceUtil.getBoolean("showlog", false);
        btnshowlog.setVisibility(showlog?View.VISIBLE:View.GONE);
//        boolean messageServiceAlive = serviceAlive(NLService.class.getName());
//        Log.d("NLService","messageServiceAlive="+messageServiceAlive);
    }

    private void initView() {
        sp = getSharedPreferences("url", Context.MODE_PRIVATE);
        myToolbar = findViewById(R.id.my_toolbar);
        mRlFloat = findViewById(R.id.rl_floatingshowlog);
        mRlright = findViewById(R.id.rl_right);
        mRlFloat.setOnClickListener(this);
        mRlright.setOnClickListener(this);
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
                    Toast.makeText(this, "唯一码只能为八位数字或字符！", Toast.LENGTH_SHORT).show();
                    return;
                }
//                if (!changeStatus()) {
//                    return;
//                }
                PreferenceUtil preferenceUtil = new PreferenceUtil(this);
                preferenceUtil.setToken(mEdtToken.getText().toString().trim());
                PostTask task = new PostTask();
                String tasknum= RandomUtil.getRandomTaskNum();
                task.setRandomTaskNum(tasknum);
                task.setOnAsyncResponse(this);
                Map<String, String> tmpmap=new HashMap<>();
                tmpmap.put("url",getPostUrl()+Constants.URL_BIND);
                tmpmap.put("token",preferenceUtil.getToken());
                task.execute(tmpmap);

//        addStatusBar();

                break;
            case R.id.rl_floatingshowlog:
            case R.id.rl_right:
                exitAfterMany();
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
    /**
     * 连续点击多次退出
     */
    private void exitAfterMany() {
        /**
         * 实现双击方法
         * src 拷贝的源数组
         * srcPos 从源数组的那个位置开始拷贝.
         * dst 目标数组
         * dstPos 从目标数组的那个位子开始写数据
         * length 拷贝的元素的个数
         */
        System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
        //实现左移，然后最后一个位置更新距离开机的时间，如果最后一个时间和最开始时间小于DURATION，即连续5次点击
        mHits[mHits.length - 1] = SystemClock.uptimeMillis();//System.currentTimeMillis()
        if ((mHits[mHits.length - 1] - mHits[0] <= DURATION)) {
            btnshowlog.setVisibility(btnshowlog.getVisibility()==View.VISIBLE?View.GONE:View.VISIBLE);
            PreferenceUtil util = new PreferenceUtil(this);
            util.putBoolean("showlog",btnshowlog.getVisibility()==View.VISIBLE?true:false);
        }
    }

}
