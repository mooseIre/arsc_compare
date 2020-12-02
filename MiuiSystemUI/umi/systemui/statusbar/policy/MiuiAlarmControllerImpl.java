package com.android.systemui.statusbar.policy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.MiuiSettings;
import android.text.TextUtils;
import com.android.systemui.Dumpable;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;

public class MiuiAlarmControllerImpl extends BroadcastReceiver implements CallbackController, Dumpable {
    private final ArrayList<MiuiAlarmController$MiuiAlarmChangeCallback> mChangeCallbacks = new ArrayList<>();
    private Context mContext;
    private boolean mHasAlarm;

    public MiuiAlarmControllerImpl(Context context) {
        this.mContext = context;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.USER_SWITCHED");
        intentFilter.addAction("android.app.action.NEXT_ALARM_CLOCK_CHANGED");
        intentFilter.addAction("android.intent.action.ALARM_CHANGED");
        context.registerReceiverAsUser(this, UserHandle.ALL, intentFilter, (String) null, (Handler) null);
        updateNextAlarm();
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("MiuiNextAlarmControllerImpl state:");
        printWriter.print("  mHasAlarm=");
        printWriter.println(this.mHasAlarm);
    }

    public void addCallback(MiuiAlarmController$MiuiAlarmChangeCallback miuiAlarmController$MiuiAlarmChangeCallback) {
        this.mChangeCallbacks.add(miuiAlarmController$MiuiAlarmChangeCallback);
        miuiAlarmController$MiuiAlarmChangeCallback.onNextAlarmChanged(this.mHasAlarm);
    }

    public void removeCallback(MiuiAlarmController$MiuiAlarmChangeCallback miuiAlarmController$MiuiAlarmChangeCallback) {
        this.mChangeCallbacks.remove(miuiAlarmController$MiuiAlarmChangeCallback);
    }

    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null) {
            return;
        }
        if (action.equals("android.intent.action.USER_SWITCHED") || action.equals("android.app.action.NEXT_ALARM_CLOCK_CHANGED") || action.equals("android.intent.action.ALARM_CHANGED")) {
            updateNextAlarm();
        }
    }

    private void updateNextAlarm() {
        this.mHasAlarm = !TextUtils.isEmpty(MiuiSettings.System.getStringForUser(this.mContext.getContentResolver(), "next_alarm_clock_formatted", -2));
        fireNextAlarmChanged();
    }

    private void fireNextAlarmChanged() {
        int size = this.mChangeCallbacks.size();
        for (int i = 0; i < size; i++) {
            this.mChangeCallbacks.get(i).onNextAlarmChanged(this.mHasAlarm);
        }
    }
}
