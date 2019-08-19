package com.weihuagu.receiptnotice;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.tao.admin.loglib.IConfig;
import com.tao.admin.loglib.TLogApplication;
import com.weihuagu.receiptnotice.service.JobWakeUpService;
import com.weihuagu.receiptnotice.service.NotificationCollectorMonitorService;
import com.weihuagu.receiptnotice.utils.log.XLogConfig;
import com.weihuagu.receiptnotice.utils.log.YLog;

public class MainApplication extends Application {
    private static Context mContenx;
    @Override
    public void onCreate() {
        super.onCreate();
        mContenx = this;
        YLog.init(new XLogConfig().setShowThreadInfo(false).setDebug(BuildConfig.DEBUG));
        startNotificationService();
        initLogConfig();
    }
      public static Context getApp(){
        return  mContenx;
    }
    private void initLogConfig() {
        TLogApplication.initialize(this);
        IConfig.getInstance().isShowLog(BuildConfig.DEBUG)//是否在logcat中打印log,默认不打印
                .isWriteLog(false)//是否在文件中记录，默认不记录
                .tag("NLService");//logcat 日志过滤tag
    }

    private void startNotificationService() {
        startService(new Intent(this, NotificationCollectorMonitorService.class));
        if (Build.VERSION.SDK_INT >= 21) {
            startService(new Intent(this, JobWakeUpService.class));
        }
    }

}
