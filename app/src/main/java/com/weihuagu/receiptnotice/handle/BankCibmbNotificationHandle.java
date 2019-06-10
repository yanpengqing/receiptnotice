package com.weihuagu.receiptnotice.handle;

import android.app.Notification;

import com.weihuagu.receiptnotice.core.IDoPost;
import com.weihuagu.receiptnotice.core.NotificationHandle;

import java.util.HashMap;
import java.util.Map;

public class BankCibmbNotificationHandle extends NotificationHandle {
    public BankCibmbNotificationHandle(String pkgtype, Notification notification, IDoPost postpush){
        super(pkgtype,notification,postpush);
    }

    public void handleNotification(){
        if(title.contains("精灵信使")&&content.contains("收入")){
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
