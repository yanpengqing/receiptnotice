package com.weihuagu.receiptnotice;

import android.app.ActivityManager;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;

import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class JobWakeUpService extends JobService {
    private int JobWakeUpId = 1;
    private JobScheduler mJobScheduler;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //开启轮寻
//        JobInfo.Builder mJobBulider = new JobInfo.Builder(
//                JobWakeUpId,new ComponentName(this,JobWakeUpService.class));
//        //设置轮寻时间
//        mJobBulider.setPeriodic(2000);
        mJobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
//        mJobScheduler.schedule(mJobBulider.build());
        startJobScheduler(startId);

        return START_STICKY;
    }
    public void startJobScheduler( int JOB_ID) {
        int id = JOB_ID;
        mJobScheduler.cancel(id);
        JobInfo.Builder builder = new JobInfo.Builder(id, new ComponentName(this, JobWakeUpService.class));
        if (Build.VERSION.SDK_INT >= 24) {
            builder.setMinimumLatency(JobInfo.DEFAULT_INITIAL_BACKOFF_MILLIS); //执行的最小延迟时间
            builder.setOverrideDeadline(JobInfo.DEFAULT_INITIAL_BACKOFF_MILLIS);  //执行的最长延时时间
            builder.setMinimumLatency(JobInfo.DEFAULT_INITIAL_BACKOFF_MILLIS);
            builder.setBackoffCriteria(JobInfo.DEFAULT_INITIAL_BACKOFF_MILLIS, JobInfo.BACKOFF_POLICY_LINEAR);//线性重试方案
        } else {
            builder.setPeriodic(5000);
        }
        builder.setPersisted(true);  // 设置设备重启时，执行该任务
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
        builder.setRequiresCharging(true); // 当插入充电器，执行该任务
        JobInfo info = builder.build();
        mJobScheduler.schedule(info); //开始定时执行该系统任务
    }
    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        //开启定时任务 定时轮寻 判断应用Service是否被杀死
        //如果被杀死则重启Service
        boolean messageServiceAlive = serviceAlive(NLService.class.getName());
        if(!messageServiceAlive){
            startService(new Intent(this,NLService.class));
        }

        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {

        return false;
    }

    /**
     * 判断某个服务是否正在运行的方法
     * @param serviceName
     *            是包名+服务的类名（例如：net.loonggg.testbackstage.TestService）
     * @return true代表正在运行，false代表服务没有正在运行
     */
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

}
