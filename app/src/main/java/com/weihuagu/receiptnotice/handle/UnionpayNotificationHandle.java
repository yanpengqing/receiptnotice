package com.weihuagu.receiptnotice.handle;

import android.app.Notification;

import com.weihuagu.receiptnotice.core.IDoPost;
import com.weihuagu.receiptnotice.core.NotificationHandle;

import java.util.HashMap;
import java.util.Map;

public class UnionpayNotificationHandle  extends NotificationHandle {
    public UnionpayNotificationHandle(String pkgtype, Notification notification, IDoPost postpush){
        super(pkgtype,notification,postpush);
    }

    public void handleNotification(){
        if(title.contains("消息推送")&&content.contains("云闪付收款")){
            Map<String,String> postmap=new HashMap<String,String>();
//            postmap.put("type","unionpay");
            postmap.put("time",when + "");
//            postmap.put("title",title);
            postmap.put("money",extractMoney(content));
//            postmap.put("content",content);
            postpush.doPost(postmap);
            return ;
        }



    }
}
