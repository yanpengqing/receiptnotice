/*
 * Created By WeihuaGu (email:weihuagu_work@163.com)
 * Copyright (c) 2017
 * All right reserved.
 */

package com.weihuagu.receiptnotice.utils;

import android.util.Log;

import com.tao.admin.loglib.Logger;
import com.weihuagu.receiptnotice.BuildConfig;

public class LogUtil {
    public static String TAG = "NLService";
    public static String DEBUGTAG = "NLDebugService";
    public static String EXCEPTIONTAG = "NLExceptionService";

    public static void infoLog(String info) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, info);
        }
    }

    public static void debugLog(String info) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, info);
        }
    }

    public static void debugLogWithDeveloper(String info) {
        if (BuildConfig.DEBUG) {
            Log.d(DEBUGTAG, info);
        }
    }

    public static void debugLogWithJava(String info) {
        System.out.println(DEBUGTAG + ":" + info);
    }

    public static void postRecordLog(String tasknum, String post) {
        if (BuildConfig.DEBUG) {
            Logger.i("*********************************");
            Logger.i("开始推送", "随机序列号:" + tasknum);
            Logger.i(post);
        }
    }

    public static void postResultLog(String tasknum, String result, String returnstr) {
        if (BuildConfig.DEBUG) {
            Logger.i("推送结果", "随机序列号:" + tasknum);
            Logger.i("推送结果", result);
            Logger.i("返回内容", returnstr);
            Logger.i("------------------------------------------");
        }
    }
}
