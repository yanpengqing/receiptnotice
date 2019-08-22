package com.weihuagu.receiptnotice.service;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v4.content.LocalBroadcastManager;

import com.weihuagu.receiptnotice.core.AsyncResponse;
import com.weihuagu.receiptnotice.core.Constants;
import com.weihuagu.receiptnotice.core.IDoPost;
import com.weihuagu.receiptnotice.core.PostTask;
import com.weihuagu.receiptnotice.handle.AlipayNotificationHandle;
import com.weihuagu.receiptnotice.handle.BankCibmbNotificationHandle;
import com.weihuagu.receiptnotice.handle.CashbarNotificationHandle;
import com.weihuagu.receiptnotice.handle.IcbcelifeNotificationHandle;
import com.weihuagu.receiptnotice.handle.MipushNotificationHandle;
import com.weihuagu.receiptnotice.handle.SMSNotificationHandle;
import com.weihuagu.receiptnotice.handle.UnionpayNotificationHandle;
import com.weihuagu.receiptnotice.handle.WechatNotificationHandle;
import com.weihuagu.receiptnotice.handle.XposedmoduleNotificationHandle;
import com.weihuagu.receiptnotice.utils.EncryptFactory;
import com.weihuagu.receiptnotice.utils.Encrypter;
import com.weihuagu.receiptnotice.utils.LogUtil;
import com.weihuagu.receiptnotice.utils.PreferenceUtil;
import com.weihuagu.receiptnotice.utils.RandomUtil;
import com.weihuagu.receiptnotice.utils.log.YLog;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class NLService extends NotificationListenerService implements AsyncResponse, IDoPost {
    private String TAG = "NLService";
    private String posturl = null;
    private Context context = null;
    private static final int MIN_CLICK_DELAY_TIME = 1000;
    private static long lastClickTime;

    private String getPostUrl() {
        SharedPreferences sp = getSharedPreferences("url", 0);
        this.posturl = sp.getString("posturl", null);
        if (posturl == null)
            return null;
        else
            return posturl;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        //        super.onNotificationPosted(sbn);
        //这里只是获取了包名和通知提示信息，其他数据可根据需求取，注意空指针就行
//        if (!isFastClick()){
//            return;
//        }
        YLog.d("接受到通知消息");
        YLog.d("posturl:" + getPostUrl());
        if (getPostUrl() == null)
            return;

        Notification notification = sbn.getNotification();
        String pkg = sbn.getPackageName();
        if (notification == null) {
            return;
        }

        Bundle extras = notification.extras;
        if (extras == null)
            return;

        YLog.d("-----------------");

        //mipush
        if ("com.xiaomi.xmsf".equals(pkg)) {
            if (getNotiTitle(extras).contains("支付宝")) {
                //printNotify(getNotitime(notification),getNotiTitle(extras),getNotiContent(extras));
                new MipushNotificationHandle("com.xiaomi.xmsf", notification, this).handleNotification();

            }

        }
        //支付宝
        if ("com.eg.android.AlipayGphone".equals(pkg)) {

            new AlipayNotificationHandle("com.eg.android.AlipayGphone", notification, this).handleNotification();

        }
        //应用管理GCM代收
        if ("android".equals(pkg)) {

            new XposedmoduleNotificationHandle("github.tornaco.xposedmoduletest", notification, this).handleNotification();
        }
        //微信
        if ("com.tencent.mm".equals(pkg)) {

            new WechatNotificationHandle("com.tencent.mm", notification, this).handleNotification();

        }
        //收钱吧
        if ("com.wosai.cashbar".equals(pkg)) {
            new CashbarNotificationHandle("com.wosai.cashbar", notification, this).handleNotification();
        }
        //云闪付
        if ("com.unionpay".equals(pkg)) {
            new UnionpayNotificationHandle("com.unionpay", notification, this).handleNotification();
        }
        //短信
        if ("com.android.mms".equals(pkg) || "com.android.messaging".equals(pkg)) {
            new SMSNotificationHandle("com.android.mms", notification, this).handleNotification();
            removeNotification(sbn);

        }
        //兴业银行app
        if ("com.cib.cibmb".equals(pkg)) {
            new BankCibmbNotificationHandle("com.cib.cibmb", notification, this).handleNotification();
        }
        //工银商户之家
        if ("com.icbc.biz.elife".equals(pkg)) {
            new IcbcelifeNotificationHandle("com.icbc.biz.elife", notification, this).handleNotification();
        }
        YLog.d("这是检测之外的其它通知");
        YLog.d("包名是" + pkg);
        printNotify(getNotitime(notification), getNotiTitle(extras), getNotiContent(extras));

        YLog.d("**********************");


    }

    public void removeNotification(StatusBarNotification sbn) {
        if (Build.VERSION.SDK_INT >= 21)
            cancelNotification(sbn.getKey());
        else
            cancelNotification(sbn.getPackageName(), sbn.getTag(), sbn.getId());
        YLog.d("receiptnotice移除了包名为" + sbn.getPackageName() + "的通知");

    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        if (Build.VERSION.SDK_INT > 19)
            super.onNotificationRemoved(sbn);
    }

    private void sendBroadcast(String msg) {
        Intent intent = new Intent(getPackageName());
        intent.putExtra("text", msg);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private String getNotitime(Notification notification) {

        long when = notification.when;
        Date date = new Date(when);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String notitime = format.format(date);
        return notitime;

    }

    private String getNotiTitle(Bundle extras) {
        String title = null;
        // 获取通知标题
        title = extras.getString(Notification.EXTRA_TITLE, "");
        return title;
    }

    private String getNotiContent(Bundle extras) {
        String content = null;
        // 获取通知内容
        content = extras.getString(Notification.EXTRA_TEXT, "");
        return content;
    }

    private void printNotify(String notitime, String title, String content) {
        YLog.d(notitime);
        YLog.d(title);
        YLog.d(content);
    }


    public void doPost(Map<String, String> params) {
        if (this.posturl == null)
            return;
        PreferenceUtil preference = new PreferenceUtil(getBaseContext());
        Map<String, String> tmpmap = params;
        Map<String, String> postmap = null;
        String tasknum = RandomUtil.getRandomTaskNum();
        YLog.d("开始准备进行post");
        PostTask mtask = new PostTask();
        mtask.setRandomTaskNum(tasknum);
        mtask.setOnAsyncResponse(this);

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getPostUrl() + Constants.URL_CALLBACK)
                .append("&token=")
                .append(new PreferenceUtil(this).getToken())
                .append("&time=")
                .append(tmpmap.get("time"))
                .append("&type=")
                .append(tmpmap.get("type"))
                .append("&money=")
                .append(tmpmap.get("money"));
//                tmpmap.put("encrypt","0");
        tmpmap.put("url", stringBuilder.toString());
        tmpmap.put("token", new PreferenceUtil(this).getToken());
        String deviceid = preference.getDeviceid();
//                tmpmap.put("deviceid",(!deviceid.equals("")? deviceid:DeviceInfoUtil.getUniquePsuedoID()));

        if (preference.isEncrypt()) {
            String encrypt_type = preference.getEncryptMethod();
            if (encrypt_type != null) {
                String key = preference.getPasswd();
                EncryptFactory encryptfactory = new EncryptFactory(key);
                YLog.d("加密方法" + encrypt_type);
                YLog.d("加密秘钥" + key);
                Encrypter encrypter = encryptfactory.getEncrypter(encrypt_type);
                if (encrypter != null && key != null) {
                    postmap = encrypter.transferMapValue(tmpmap);
                    postmap.put("url", this.posturl);
                }

            }
        }

//                Map<String, String> recordmap=tmpmap;
//                recordmap.remove("encrypt");
        LogUtil.postRecordLog(tasknum, tmpmap.toString());


        if (postmap != null)
            mtask.execute(postmap);
        else
            mtask.execute(tmpmap);

    }

    /**
     * 银行卡短信监听，目前农业银行
     *
     * @param params
     */
    @Override
    public void doBank(Map<String, String> params) {
        String tasknum = RandomUtil.getRandomTaskNum();
        YLog.d("开始准备进行post");
        PostTask mtask = new PostTask();
        mtask.setRandomTaskNum(tasknum);
        mtask.setOnAsyncResponse(this);

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getPostUrl() + Constants.URL_FXWX_BACK)
                .append("&token=")
                .append(new PreferenceUtil(this).getToken())
                .append("&time=")
                .append(params.get("time"))
//                .append("&last_no=")
//                .append(params.get("last_no"))
//                .append("&client_id=")
//                .append(DeviceInfoUtil.getDeviceId(getApplicationContext()))
//                .append("&type=")
//                .append(params.get("type"))
                .append("&pay_way=")
                .append(params.get("pay_way"))
                .append("&money=")
                .append(params.get("money"));
//                tmpmap.put("encrypt","0");
        params.put("url", stringBuilder.toString());
        params.put("token", new PreferenceUtil(this).getToken());
        LogUtil.postRecordLog(tasknum, params.toString());
        mtask.execute(params);
    }


    @Override
    public void onDataReceivedSuccess(String[] returnstr) {
        YLog.d("Post Receive-returned post string");
        YLog.d(returnstr[2]);
        LogUtil.postResultLog(returnstr[0], returnstr[1], returnstr[2]);

    }

    @Override
    public void onDataReceivedFailed(String[] returnstr) {
        // TODO Auto-generated method stub
        YLog.d("Post Receive-post error");
        LogUtil.postResultLog(returnstr[0], returnstr[1], returnstr[2]);

    }

    public static boolean isFastClick() {
        boolean flag = false;
        long curClickTime = System.currentTimeMillis();
        if ((curClickTime - lastClickTime) >= MIN_CLICK_DELAY_TIME) {
            flag = true;
        }
        lastClickTime = curClickTime;
        return flag;
    }
}
