package com.weihuagu.receiptnotice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootCompleteReceiver extends BroadcastReceiver {

    private static final String ACTION = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ACTION)) {
            Log.i("BootCompleteReceiver", "收到广播了");
            Intent mIntent = new Intent(context, NLService.class);
            context.startService(mIntent);
            Log.i("BootCompleteReceiver", "2222222222222");
        }
    }
}
