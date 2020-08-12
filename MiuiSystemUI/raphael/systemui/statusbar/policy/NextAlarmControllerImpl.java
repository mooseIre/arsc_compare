package com.android.systemui.statusbar.policy;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.UserHandle;
import com.android.systemui.statusbar.policy.NextAlarmController;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;

public class NextAlarmControllerImpl extends BroadcastReceiver implements NextAlarmController {
    private AlarmManager mAlarmManager;
    private final ArrayList<NextAlarmController.NextAlarmChangeCallback> mChangeCallbacks = new ArrayList<>();
    private boolean mHasSystemAlarm;
    private boolean mHasThirdPartyAlarm;

    public NextAlarmControllerImpl(Context context) {
        this.mAlarmManager = (AlarmManager) context.getSystemService("alarm");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.ALARM_CHANGED");
        context.registerReceiverAsUser(this, UserHandle.ALL, intentFilter, (String) null, (Handler) null);
        fireAlarmChanged();
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("NextAlarmController state:");
        printWriter.print("  mHasNextAlarm=");
        printWriter.println(this.mHasSystemAlarm || this.mHasThirdPartyAlarm);
    }

    public void addCallback(NextAlarmController.NextAlarmChangeCallback nextAlarmChangeCallback) {
        this.mChangeCallbacks.add(nextAlarmChangeCallback);
        nextAlarmChangeCallback.onNextAlarmChanged(this.mHasSystemAlarm || this.mHasThirdPartyAlarm);
    }

    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.ALARM_CHANGED")) {
            boolean booleanExtra = intent.getBooleanExtra("alarmSet", false);
            if (intent.getBooleanExtra("alarmSystem", false)) {
                this.mHasSystemAlarm = booleanExtra;
            } else {
                this.mHasThirdPartyAlarm = booleanExtra;
            }
            fireAlarmChanged();
        }
    }

    public boolean hasAlarm() {
        return this.mHasSystemAlarm || this.mHasThirdPartyAlarm;
    }

    private void fireAlarmChanged() {
        int size = this.mChangeCallbacks.size();
        for (int i = 0; i < size; i++) {
            this.mChangeCallbacks.get(i).onNextAlarmChanged(this.mHasSystemAlarm || this.mHasThirdPartyAlarm);
        }
    }
}
