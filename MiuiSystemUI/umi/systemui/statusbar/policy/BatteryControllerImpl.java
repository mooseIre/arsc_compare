package com.android.systemui.statusbar.policy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import com.android.systemui.statusbar.policy.BatteryController;
import com.xiaomi.stat.MiStat;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;

public class BatteryControllerImpl extends BroadcastReceiver implements BatteryController {
    ContentObserver mBatteryExtremeSaveModeChangeObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean z) {
            BatteryControllerImpl batteryControllerImpl = BatteryControllerImpl.this;
            boolean z2 = false;
            if (Settings.Secure.getIntForUser(batteryControllerImpl.mContext.getContentResolver(), "EXTREME_POWER_MODE_ENABLE", 0, -2) == 1) {
                z2 = true;
            }
            batteryControllerImpl.mIsExtremePowerSaveMode = z2;
            BatteryControllerImpl.this.fireExtremePowerSaveChanged();
        }
    };
    ContentObserver mBatterySaveModeChangeObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean z) {
            BatteryControllerImpl batteryControllerImpl = BatteryControllerImpl.this;
            boolean z2 = false;
            if (Settings.System.getIntForUser(batteryControllerImpl.mContext.getContentResolver(), "POWER_SAVE_MODE_OPEN", 0, -2) == 1) {
                z2 = true;
            }
            batteryControllerImpl.mIsPowerSaveMode = z2;
            BatteryControllerImpl.this.firePowerSaveChanged();
        }
    };
    /* access modifiers changed from: private */
    public int mBatteryStyle = 1;
    ContentObserver mBatteryStyleChangeObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean z) {
            BatteryControllerImpl batteryControllerImpl = BatteryControllerImpl.this;
            int unused = batteryControllerImpl.mBatteryStyle = Settings.System.getIntForUser(batteryControllerImpl.mContext.getContentResolver(), "battery_indicator_style", 1, -2);
            synchronized (BatteryControllerImpl.this.mChangeCallbacks) {
                int size = BatteryControllerImpl.this.mChangeCallbacks.size();
                for (int i = 0; i < size; i++) {
                    ((BatteryController.BatteryStateChangeCallback) BatteryControllerImpl.this.mChangeCallbacks.get(i)).onBatteryStyleChanged(BatteryControllerImpl.this.mBatteryStyle);
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public final ArrayList<BatteryController.BatteryStateChangeCallback> mChangeCallbacks = new ArrayList<>();
    protected boolean mCharged;
    protected boolean mCharging;
    /* access modifiers changed from: private */
    public final Context mContext;
    private boolean mDemoMode;
    /* access modifiers changed from: private */
    public final Handler mHandler;
    private boolean mHasReceivedBattery = false;
    protected boolean mIsExtremePowerSaveMode;
    protected boolean mIsPowerSaveMode;
    protected int mLevel;
    protected boolean mPluggedIn;
    /* access modifiers changed from: private */
    public boolean mTestmode = false;

    static {
        Log.isLoggable("BatteryController", 3);
    }

    public BatteryControllerImpl(Context context) {
        this.mContext = context;
        this.mHandler = new Handler();
        PowerManager powerManager = (PowerManager) context.getSystemService("power");
        registerReceiver();
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("battery_indicator_style"), false, this.mBatteryStyleChangeObserver, -1);
        this.mBatteryStyleChangeObserver.onChange(false);
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("POWER_SAVE_MODE_OPEN"), false, this.mBatterySaveModeChangeObserver, -1);
        this.mBatterySaveModeChangeObserver.onChange(false);
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("EXTREME_POWER_MODE_ENABLE"), false, this.mBatteryExtremeSaveModeChangeObserver, -1);
        this.mBatteryExtremeSaveModeChangeObserver.onChange(false);
    }

    private void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.BATTERY_CHANGED");
        intentFilter.addAction("android.intent.action.USER_SWITCHED");
        intentFilter.addAction("com.android.systemui.BATTERY_LEVEL_TEST");
        this.mContext.registerReceiver(this, intentFilter);
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("BatteryController state:");
        printWriter.print("  mLevel=");
        printWriter.println(this.mLevel);
        printWriter.print("  mPluggedIn=");
        printWriter.println(this.mPluggedIn);
        printWriter.print("  mCharging=");
        printWriter.println(this.mCharging);
        printWriter.print("  mCharged=");
        printWriter.println(this.mCharged);
        printWriter.print("  mPowerSave=");
        printWriter.println(this.mIsPowerSaveMode);
        printWriter.print("  mExtremePowerSave=");
        printWriter.println(this.mIsExtremePowerSaveMode);
    }

    public void addCallback(BatteryController.BatteryStateChangeCallback batteryStateChangeCallback) {
        synchronized (this.mChangeCallbacks) {
            this.mChangeCallbacks.add(batteryStateChangeCallback);
        }
        batteryStateChangeCallback.onBatteryStyleChanged(this.mBatteryStyle);
        batteryStateChangeCallback.onPowerSaveChanged(this.mIsPowerSaveMode);
        batteryStateChangeCallback.onExtremePowerSaveChanged(this.mIsExtremePowerSaveMode);
        if (this.mHasReceivedBattery) {
            batteryStateChangeCallback.onBatteryLevelChanged(this.mLevel, this.mPluggedIn, this.mCharging);
        }
    }

    public void removeCallback(BatteryController.BatteryStateChangeCallback batteryStateChangeCallback) {
        synchronized (this.mChangeCallbacks) {
            this.mChangeCallbacks.remove(batteryStateChangeCallback);
        }
    }

    public void onReceive(final Context context, Intent intent) {
        String action = intent.getAction();
        boolean z = true;
        if (action.equals("android.intent.action.BATTERY_CHANGED")) {
            if (!this.mTestmode || intent.getBooleanExtra("testmode", false)) {
                this.mHasReceivedBattery = true;
                this.mLevel = intent.getIntExtra(MiStat.Param.LEVEL, 0);
                this.mPluggedIn = intent.getIntExtra("plugged", 0) != 0;
                int intExtra = intent.getIntExtra(MiStat.Param.STATUS, 1);
                boolean z2 = intExtra == 5;
                this.mCharged = z2;
                if ((!z2 && intExtra != 2) || !this.mPluggedIn) {
                    z = false;
                }
                this.mCharging = z;
                fireBatteryLevelChanged();
            }
        } else if ("android.intent.action.USER_SWITCHED".equals(action)) {
            this.mBatteryStyleChangeObserver.onChange(false);
            this.mBatterySaveModeChangeObserver.onChange(false);
            this.mBatteryExtremeSaveModeChangeObserver.onChange(false);
        } else if (action.equals("com.android.systemui.BATTERY_LEVEL_TEST")) {
            this.mTestmode = true;
            this.mHandler.post(new Runnable() {
                int curLevel = 0;
                Intent dummy;
                int incr = 1;
                int saveLevel;
                boolean savePlugged;

                {
                    BatteryControllerImpl batteryControllerImpl = BatteryControllerImpl.this;
                    this.saveLevel = batteryControllerImpl.mLevel;
                    this.savePlugged = batteryControllerImpl.mPluggedIn;
                    this.dummy = new Intent("android.intent.action.BATTERY_CHANGED");
                }

                public void run() {
                    int i = this.curLevel;
                    int i2 = 0;
                    if (i < 0) {
                        boolean unused = BatteryControllerImpl.this.mTestmode = false;
                        this.dummy.putExtra(MiStat.Param.LEVEL, this.saveLevel);
                        this.dummy.putExtra("plugged", this.savePlugged);
                        this.dummy.putExtra("testmode", false);
                    } else {
                        this.dummy.putExtra(MiStat.Param.LEVEL, i);
                        Intent intent = this.dummy;
                        if (this.incr > 0) {
                            i2 = 1;
                        }
                        intent.putExtra("plugged", i2);
                        this.dummy.putExtra("testmode", true);
                    }
                    context.sendBroadcast(this.dummy);
                    if (BatteryControllerImpl.this.mTestmode) {
                        int i3 = this.curLevel;
                        int i4 = this.incr;
                        int i5 = i3 + i4;
                        this.curLevel = i5;
                        if (i5 == 100) {
                            this.incr = i4 * -1;
                        }
                        BatteryControllerImpl.this.mHandler.postDelayed(this, 200);
                    }
                }
            });
        }
    }

    public boolean isPowerSave() {
        return this.mIsPowerSaveMode;
    }

    public boolean isExtremePowerSave() {
        return this.mIsExtremePowerSaveMode;
    }

    /* access modifiers changed from: protected */
    public void fireBatteryLevelChanged() {
        synchronized (this.mChangeCallbacks) {
            int size = this.mChangeCallbacks.size();
            for (int i = 0; i < size; i++) {
                this.mChangeCallbacks.get(i).onBatteryLevelChanged(this.mLevel, this.mPluggedIn, this.mCharging);
            }
        }
    }

    /* access modifiers changed from: private */
    public void firePowerSaveChanged() {
        synchronized (this.mChangeCallbacks) {
            int size = this.mChangeCallbacks.size();
            for (int i = 0; i < size; i++) {
                this.mChangeCallbacks.get(i).onPowerSaveChanged(this.mDemoMode ? false : this.mIsPowerSaveMode);
            }
        }
    }

    /* access modifiers changed from: private */
    public void fireExtremePowerSaveChanged() {
        synchronized (this.mChangeCallbacks) {
            int size = this.mChangeCallbacks.size();
            for (int i = 0; i < size; i++) {
                this.mChangeCallbacks.get(i).onExtremePowerSaveChanged(this.mDemoMode ? false : this.mIsExtremePowerSaveMode);
            }
        }
    }

    private void fireDemoModeChanged(String str, Bundle bundle) {
        int size = this.mChangeCallbacks.size();
        for (int i = 0; i < size; i++) {
            this.mChangeCallbacks.get(i).dispatchDemoCommand(str, bundle);
        }
    }

    public void dispatchDemoCommand(String str, Bundle bundle) {
        if (!this.mDemoMode && str.equals("enter")) {
            this.mDemoMode = true;
            this.mContext.unregisterReceiver(this);
            this.mLevel = 100;
            this.mPluggedIn = false;
            this.mCharging = false;
            this.mBatterySaveModeChangeObserver.onChange(false);
            this.mBatteryExtremeSaveModeChangeObserver.onChange(false);
            fireBatteryLevelChanged();
            fireDemoModeChanged(str, bundle);
        } else if (this.mDemoMode && str.equals("exit")) {
            this.mDemoMode = false;
            registerReceiver();
            this.mBatteryStyleChangeObserver.onChange(false);
            this.mBatterySaveModeChangeObserver.onChange(false);
            this.mBatteryExtremeSaveModeChangeObserver.onChange(false);
            fireDemoModeChanged(str, bundle);
        } else if (this.mDemoMode && str.equals("battery")) {
            String string = bundle.getString(MiStat.Param.LEVEL);
            String string2 = bundle.getString("plugged");
            if (string != null) {
                this.mLevel = Math.min(Math.max(Integer.parseInt(string), 0), 100);
            }
            if (string2 != null) {
                this.mPluggedIn = Boolean.parseBoolean(string2);
            }
            fireBatteryLevelChanged();
        }
    }
}
