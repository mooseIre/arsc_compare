package com.android.systemui.statusbar.policy;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.HandlerExecutor;
import android.os.UserHandle;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.statusbar.policy.NextAlarmController;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;

public class NextAlarmControllerImpl extends BroadcastReceiver implements NextAlarmController {
    private AlarmManager mAlarmManager;
    private final ArrayList<NextAlarmController.NextAlarmChangeCallback> mChangeCallbacks = new ArrayList<>();
    private Handler mMainHandler;
    private volatile AlarmManager.AlarmClockInfo mNextAlarm;

    public NextAlarmControllerImpl(Context context, BroadcastDispatcher broadcastDispatcher, Handler handler, Handler handler2) {
        this.mAlarmManager = (AlarmManager) context.getSystemService("alarm");
        this.mMainHandler = handler;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.USER_SWITCHED");
        intentFilter.addAction("android.app.action.NEXT_ALARM_CLOCK_CHANGED");
        HandlerExecutor handlerExecutor = new HandlerExecutor(handler2);
        broadcastDispatcher.registerReceiver(this, intentFilter, handlerExecutor, UserHandle.ALL);
        handlerExecutor.execute(new Runnable() {
            /* class com.android.systemui.statusbar.policy.NextAlarmControllerImpl.AnonymousClass1 */

            public void run() {
                NextAlarmControllerImpl.this.updateNextAlarm();
            }
        });
    }

    @Override // com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("NextAlarmController state:");
        printWriter.print("  mNextAlarm=");
        printWriter.println(this.mNextAlarm);
    }

    public void addCallback(NextAlarmController.NextAlarmChangeCallback nextAlarmChangeCallback) {
        this.mChangeCallbacks.add(nextAlarmChangeCallback);
        nextAlarmChangeCallback.onNextAlarmChanged(this.mNextAlarm);
    }

    public void removeCallback(NextAlarmController.NextAlarmChangeCallback nextAlarmChangeCallback) {
        this.mChangeCallbacks.remove(nextAlarmChangeCallback);
    }

    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals("android.intent.action.USER_SWITCHED") || action.equals("android.app.action.NEXT_ALARM_CLOCK_CHANGED")) {
            updateNextAlarm();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateNextAlarm() {
        this.mNextAlarm = this.mAlarmManager.getNextAlarmClock(-2);
        this.mMainHandler.post(new Runnable() {
            /* class com.android.systemui.statusbar.policy.NextAlarmControllerImpl.AnonymousClass2 */

            public void run() {
                NextAlarmControllerImpl.this.fireNextAlarmChanged();
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void fireNextAlarmChanged() {
        AlarmManager.AlarmClockInfo alarmClockInfo = this.mNextAlarm;
        int size = this.mChangeCallbacks.size();
        for (int i = 0; i < size; i++) {
            this.mChangeCallbacks.get(i).onNextAlarmChanged(alarmClockInfo);
        }
    }

    @Override // com.android.systemui.statusbar.policy.NextAlarmController
    public boolean hasAlarm() {
        return this.mNextAlarm != null;
    }
}
