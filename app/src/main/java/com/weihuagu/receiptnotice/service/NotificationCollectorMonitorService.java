package com.weihuagu.receiptnotice.service;

import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.weihuagu.receiptnotice.MainApplication;
import com.weihuagu.receiptnotice.SSLSocketFactoryCompat;
import com.weihuagu.receiptnotice.beans.DeviceBean;
import com.weihuagu.receiptnotice.core.AsyncResponse;
import com.weihuagu.receiptnotice.core.Constants;
import com.weihuagu.receiptnotice.core.PostTask;
import com.weihuagu.receiptnotice.utils.DeviceInfoUtil;
import com.weihuagu.receiptnotice.utils.LogUtil;
import com.weihuagu.receiptnotice.utils.PreferenceUtil;
import com.weihuagu.receiptnotice.utils.RandomUtil;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.net.ssl.SSLSocketFactory;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.TlsVersion;

public class NotificationCollectorMonitorService extends Service implements AsyncResponse {
    private static final String TAG = "NotifiCollectorMonitor";
    private Timer timer = null;
    private String echointerval = null;
    private TimerTask echotimertask = null;
    private Socket mSocket;
    private String posturl = null;

    @Override
    public void onCreate() {
        super.onCreate();
        ensureCollectorRunning();
//        startEchoTimer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private boolean echoServerBySocketio(String echourl, final String echojson) {
        mSocket = EchoSocket.getInstance(echourl);
        mSocket.connect();
        mSocket.emit("echo", echojson);
        mSocket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                LogUtil.infoLog("socket disconnected,try start echo in 5 secend");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                echoServer();
            }
        }).on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {

            }
        }).on(Socket.EVENT_MESSAGE, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                //收到消息

            }
        });
        return true;
    }

    private String getDefaultEchoInterval() {
        if (Build.VERSION.SDK_INT >= 22)
             return "30";
        else
             return "30";
    }

    private void startEchoTimer() {
        PreferenceUtil preference = new PreferenceUtil(getBaseContext());
        String interval = preference.getEchoInterval();
        this.echointerval = (!interval.equals("") ? interval : getDefaultEchoInterval());
        this.echotimertask = returnEchoTimerTask();
        this.timer = new Timer();
        int intervalmilliseconds = Integer.parseInt(this.echointerval) * 1000;
        LogUtil.infoLog("now socketio timer milliseconds:" + intervalmilliseconds);
        timer.schedule(echotimertask, 5 * 1000, intervalmilliseconds);
    }

    private TimerTask returnEchoTimerTask() {
        return new TimerTask() {
            @Override
            public void run() {
                if (!isIntervalMatchPreference()) {
                    //重新设置时间间隔
                    restartEchoTimer();
                    return;
                }
                LogUtil.debugLog("once socketio timer task run");
                if (getPostUrl() != null && (!TextUtils.isEmpty(new PreferenceUtil(MainApplication.getApp()).getToken()))) {
                    getServer();
//                    echoServer();
                }
//                if (!flag)
//                    LogUtil.debugLog("socketio timer task not have a server");
            }
        };
    }

    private void getServer() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getPostUrl() + Constants.URL_SOCKET)
                .append("&token=")
                .append(new PreferenceUtil(NotificationCollectorMonitorService.this).getToken());
        Map<String, String> tmpmap = new HashMap<>();
        tmpmap.put("url", stringBuilder.toString());
        PostTask mtask = new PostTask();
        mtask.setOnAsyncResponse(this);
        mtask.setRandomTaskNum( RandomUtil.getRandomTaskNum());
        mtask.execute(tmpmap);
    }

    private void restartEchoTimer() {
        if (this.timer != null) {
            this.timer.cancel();
            this.timer = null;
        }
        if (echotimertask != null) {
            echotimertask.cancel();
            echotimertask = null;
        }
        LogUtil.debugLog("restart echo timer task");
        startEchoTimer();
    }

    private boolean isIntervalMatchPreference() {
        PreferenceUtil preference = new PreferenceUtil(getBaseContext());
        String interval = preference.getEchoInterval();
        if (interval.equals(""))
            return true;
        if (interval.equals(this.echointerval))
            return true;
        return false;
    }

    private boolean echoServer() {
        PreferenceUtil preference = new PreferenceUtil(getBaseContext());
        Gson gson = new Gson();
        if (true) {
            Date date = new Date(System.currentTimeMillis());
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String time = format.format(date);
            DeviceBean device = new DeviceBean();
            String deviceid = preference.getDeviceid();
            deviceid = (!deviceid.equals("") ? deviceid : DeviceInfoUtil.getUniquePsuedoID());
            device.setToken(new PreferenceUtil(this).getToken());
            device.setConnectedtime(System.currentTimeMillis()+"");
            LogUtil.debugLog("start connect socketio");
//            if (mSocket == null) {
                echoServerBySocketio(getPostUrl() + Constants.URL_SOCKET, gson.toJson(device));
//            } else {
//                mSocket.emit("echo", gson.toJson(device));
//            }
            LogUtil.debugLog(gson.toJson(device));
            return true;
        } else
            return false;

    }

    private void ensureCollectorRunning() {
        ComponentName collectorComponent = new ComponentName(this, /*NotificationListenerService Inheritance*/ NLService.class);
        Log.v(TAG, "ensureCollectorRunning collectorComponent: " + collectorComponent);
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        boolean collectorRunning = false;
        List<ActivityManager.RunningServiceInfo> runningServices = manager.getRunningServices(Integer.MAX_VALUE);
        if (runningServices == null) {
            Log.w(TAG, "ensureCollectorRunning() runningServices is NULL");
            return;
        }
        for (ActivityManager.RunningServiceInfo service : runningServices) {
            if (service.service.equals(collectorComponent)) {
                Log.w(TAG, "ensureCollectorRunning service - pid: " + service.pid + ", currentPID: " + Process.myPid() + ", clientPackage: " + service.clientPackage + ", clientCount: " + service.clientCount
                        + ", clientLabel: " + ((service.clientLabel == 0) ? "0" : "(" + getResources().getString(service.clientLabel) + ")"));
                if (service.pid == Process.myPid() /*&& service.clientCount > 0 && !TextUtils.isEmpty(service.clientPackage)*/) {
                    collectorRunning = true;
                }
            }
        }
        if (collectorRunning) {
            LogUtil.infoLog("ensureCollectorRunning: collector is running");
            return;
        }
        LogUtil.infoLog("ensureCollectorRunning: collector not running, reviving...");
        toggleNotificationListenerService();
    }

    private void toggleNotificationListenerService() {
        LogUtil.infoLog("toggleNotificationListenerService() called");
        ComponentName thisComponent = new ComponentName(this, /*getClass()*/ NLService.class);
        PackageManager pm = getPackageManager();
        pm.setComponentEnabledSetting(thisComponent, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        pm.setComponentEnabledSetting(thisComponent, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDataReceivedSuccess(String[] returnstr) {
        LogUtil.infoLog(returnstr[0]+returnstr[1]+returnstr[2]);
    }

    @Override
    public void onDataReceivedFailed(String[] returnstr) {
        LogUtil.infoLog(returnstr[0]+returnstr[1]+returnstr[2]);
    }

    public static class EchoSocket {
        private static Socket instance1 = null;
        private static Socket instance2 = null;
        private static Socket instance3 = null;
        private static final int maxCount = 3;

        private EchoSocket() {
        }

        public static Socket getThisInstance(int i) {
            if (i == 1)
                return EchoSocket.instance1;
            if (i == 2)
                return EchoSocket.instance2;
            if (i == 3)
                return EchoSocket.instance3;
            else
                return null;
        }

        public static Socket getInstance(String socketserverurl) {
            Random random = new Random();
            int current = random.nextInt(maxCount) + 1;
            if (getThisInstance(current) == null) {
                synchronized (EchoSocket.class) {
                    if (current == 1)
                        instance1 = getIOSocket(socketserverurl);
                    if (current == 2)
                        instance2 = getIOSocket(socketserverurl);

                    if (current == 3)
                        instance3 = getIOSocket(socketserverurl);

                }
            }
            return getThisInstance(current);
        }

        public static Socket getIOSocket(String socketserverurl) {
            try {
                if (Build.VERSION.SDK_INT >= 22) {
                    return IO.socket(socketserverurl);
                } else {
                    SSLSocketFactory factory = new SSLSocketFactoryCompat();
                    ConnectionSpec cs = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                            .tlsVersions(TlsVersion.TLS_1_2)
                            .build();
                    List<ConnectionSpec> specs = new ArrayList<>();
                    specs.add(cs);
                    specs.add(ConnectionSpec.COMPATIBLE_TLS);
                    specs.add(ConnectionSpec.CLEARTEXT);
                    OkHttpClient client = new OkHttpClient.Builder()
                            .sslSocketFactory(factory)
                            .connectionSpecs(specs)
                            .build();
                    IO.setDefaultOkHttpWebSocketFactory(client);
                    IO.setDefaultOkHttpCallFactory(client);
                    // set as an option
                    IO.Options opts = new IO.Options();
                    opts.callFactory = client;
                    opts.webSocketFactory = client;
                    return IO.socket(socketserverurl, opts);
                }
            } catch (URISyntaxException e) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                LogUtil.debugLog(sw.toString());
                return null;
            } catch (KeyManagementException e) {
                e.printStackTrace();
                return null;
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                return null;
            }


        }
    }

    private String getPostUrl() {
        SharedPreferences sp = getSharedPreferences("url", 0);
        this.posturl = sp.getString("posturl", null);
        if (posturl == null)
            return null;
        else
            return posturl;
    }
}
