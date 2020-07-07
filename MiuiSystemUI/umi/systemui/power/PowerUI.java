package com.android.systemui.power;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.Dependency;
import com.android.systemui.HapticFeedBackImpl;
import com.android.systemui.SystemUI;
import com.xiaomi.stat.MiStat;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class PowerUI extends SystemUI {
    private int mBatteryLevel = 100;
    private int mBatteryStatus = 1;
    private final Handler mHandler = new Handler();
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.BATTERY_CHANGED".equals(intent.getAction())) {
                PowerUI.this.handleBatteryChanged(intent);
            }
        }
    };
    private int mInvalidCharger = 0;
    private int mPlugType = 0;

    public void start() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.BATTERY_CHANGED");
        this.mContext.registerReceiver(this.mIntentReceiver, intentFilter, (String) null, this.mHandler);
    }

    /* access modifiers changed from: private */
    public void handleBatteryChanged(Intent intent) {
        int i = this.mPlugType;
        this.mBatteryLevel = intent.getIntExtra(MiStat.Param.LEVEL, 100);
        boolean z = true;
        this.mBatteryStatus = intent.getIntExtra(MiStat.Param.STATUS, 1);
        this.mPlugType = intent.getIntExtra("plugged", 1);
        this.mInvalidCharger = intent.getIntExtra("invalid_charger", 0);
        if (this.mPlugType == 0) {
            z = false;
        }
        if (this.mPlugType == i) {
            return;
        }
        if (!z || !KeyguardUpdateMonitor.getInstance(this.mContext).isShowingChargeAnimationWindow()) {
            ((HapticFeedBackImpl) Dependency.get(HapticFeedBackImpl.class)).getHapticFeedbackUtil().performHapticFeedback(0, false);
        }
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println(PowerUI.class.getSimpleName() + " state:");
        printWriter.println("  mBatteryLevel:" + this.mBatteryLevel);
        printWriter.println("  mBatteryStatus:" + this.mBatteryStatus);
        printWriter.println("  mPlugType:" + this.mPlugType);
        printWriter.println("  mInvalidCharger:" + this.mInvalidCharger);
    }
}
