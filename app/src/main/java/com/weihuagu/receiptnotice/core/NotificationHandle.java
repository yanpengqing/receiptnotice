package com.weihuagu.receiptnotice.core;

import android.app.Notification;
import android.os.Bundle;
import android.text.TextUtils;

import com.weihuagu.receiptnotice.utils.log.YLog;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public abstract class NotificationHandle {
    protected String pkgtype;
    protected Notification notification;
    protected Bundle extras;
    protected String title;
    protected String content;
    protected String notitime;
    protected IDoPost postpush;
    protected long when;

    public NotificationHandle(String pkgtype, Notification notification, IDoPost postpush) {
        this.pkgtype = pkgtype;
        this.notification = notification;
        this.postpush = postpush;

        this.extras = notification.extras;
        // 获取通知标题
        title = extras.getString(Notification.EXTRA_TITLE, "");
        // 获取通知内容
        if ("com.android.mms".equals(pkgtype)||"com.android.messaging".equals(pkgtype)){
            content = notification.tickerText.toString();
        }else {
            content = extras.getString(Notification.EXTRA_TEXT, "");
        }
        YLog.d("短信内容：" + content);
        when = notification.when;
        Date date = new Date(when);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        notitime = format.format(date);


    }


    public abstract void handleNotification();

    protected String extractMoney(String content) {
        Pattern pattern = Pattern.compile("(收款|向你付款|收入|收入人民币)(([1-9]{1}\\d*)|([0]{1}))(\\.(\\d){0,2})?元");
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            String tmp = matcher.group();
            Pattern patternnum = Pattern.compile("(([1-9]{1}\\d*)|([0]{1}))(\\.(\\d){0,2})?");
            Matcher matchernum = patternnum.matcher(tmp);
            if (matchernum.find())
                return matchernum.group();
            return null;
        } else
            return null;


    }

}

