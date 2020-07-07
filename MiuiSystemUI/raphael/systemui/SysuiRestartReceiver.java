package com.android.systemui;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Process;

public class SysuiRestartReceiver extends BroadcastReceiver {
    public static String ACTION = "com.android.systemui.action.RESTART";

    public void onReceive(Context context, Intent intent) {
        if (ACTION.equals(intent.getAction())) {
            NotificationManager.from(context).cancel(intent.getData().toString().substring(10), 6);
            Process.killProcess(Process.myPid());
        }
    }
}
