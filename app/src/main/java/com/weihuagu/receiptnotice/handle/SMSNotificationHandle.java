package com.weihuagu.receiptnotice.handle;

import android.app.Notification;
import android.text.TextUtils;

import com.weihuagu.receiptnotice.core.IDoPost;
import com.weihuagu.receiptnotice.core.NotificationHandle;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SMSNotificationHandle extends NotificationHandle {
    public SMSNotificationHandle(String pkgtype, Notification notification, IDoPost postpush) {
        super(pkgtype, notification, postpush);
    }

    public void handleNotification() {
        if (title.contains("兴业银行") || content.contains("兴业银行")) {
            Map<String, String> postmap = new HashMap<String, String>();
//            postmap.put("type","unionpay");
            postmap.put("time", when + "");
//            postmap.put("title",title);
            postmap.put("money", extractMoney(content));
            postmap.put("type", "2");
//            postmap.put("content",content);
            postpush.doPost(postmap);
            return;
        }
        if (title.contains("农业银行") || title.contains("95599") && (content.contains("中国农业银行") && content.contains("尾号")
                && content.contains("交易人民币")&& content.contains("余额"))) {
            try {
                String value = extractMoney(content);
                if (TextUtils.isEmpty(value)) {
                    return;
                }
                Map<String, String> postmap = new HashMap<String, String>();
//            postmap.put("type","unionpay");
                postmap.put("time", when + "");
                postmap.put("last_no", getMidText(content, "尾号", "账户"));  //尾号
                postmap.put("money", value);
                if (content.contains("支付宝")) {
                    postmap.put("pay_way", "2");
                } else if (content.contains("银联")) {
                    postmap.put("pay_way", "1");
                }
                postpush.doBank(postmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
    }

    public String getMidText(String text, String begin, String end) {
        try {
            int b = text.indexOf(begin) + begin.length();
            int e = text.indexOf(end, b);
            return text.substring(b, e);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    protected String extractMoney(String content) {
        Pattern pattern = Pattern.compile("(收款|向你付款|收入|交易人民币)(([1-9]{1}\\d*)|([0]{1}))(\\.(\\d){0,2})?");
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
