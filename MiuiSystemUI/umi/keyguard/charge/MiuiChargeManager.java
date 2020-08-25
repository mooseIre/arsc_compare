package com.android.keyguard.charge;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Slog;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.Constants;
import com.android.systemui.Dumpable;
import com.miui.systemui.annotation.Inject;
import com.xiaomi.stat.MiStat;
import com.xiaomi.stat.c.b;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class MiuiChargeManager implements Dumpable {
    /* access modifiers changed from: private */
    public static String KEY_QUICK_CHARGE = "quick_charge";
    /* access modifiers changed from: private */
    public static String METHOD_GET_POWER_SUPPLY_INFO = "getPowerSupplyInfo";
    /* access modifiers changed from: private */
    public static String PROVIDER_POWER_CENTER = "content://com.miui.powercenter.provider";
    /* access modifiers changed from: private */
    public BatteryStatus mBatteryStatus;
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public Handler mHandler = new Handler();
    /* access modifiers changed from: private */
    public boolean mIsChargeLevelAnimationRunning;
    Runnable mNotUpdateLevelRunnable = new Runnable() {
        public void run() {
            boolean unused = MiuiChargeManager.this.mNotUpdateLevelWhenBatteryChange = false;
            if ((MiuiChargeManager.this.mRealLevel < MiuiChargeManager.this.mBatteryStatus.level && !MiuiChargeManager.this.mBatteryStatus.isCharging()) || MiuiChargeManager.this.mRealLevel > MiuiChargeManager.this.mBatteryStatus.level) {
                MiuiChargeManager.this.mBatteryStatus.level = MiuiChargeManager.this.mRealLevel;
                MiuiChargeManager.this.notifyBatteryStatusChanged();
            }
        }
    };
    /* access modifiers changed from: private */
    public boolean mNotUpdateLevelWhenBatteryChange;
    /* access modifiers changed from: private */
    public int mRealLevel;
    /* access modifiers changed from: private */
    public Runnable mUpdateChargingFromPowerCenterRunnable = new Runnable() {
        public void run() {
            MiuiChargeManager.this.getChargingStatusFromPowerCenter();
        }
    };
    /* access modifiers changed from: private */
    public int mWiredChargeType;
    /* access modifiers changed from: private */
    public int mWirelessChargeType;

    /* access modifiers changed from: private */
    public int checkWireState(int i, int i2) {
        boolean z = false;
        boolean z2 = i == 4;
        if (i == 1 || i == 2) {
            z = true;
        }
        if (i2 != 2 && i2 != 5 && i2 != 4) {
            return -1;
        }
        if (z2) {
            return 10;
        }
        return z ? 11 : -1;
    }

    private int formatBatteryLevel(int i) {
        if (i < 0) {
            return 0;
        }
        if (i > 100) {
            return 100;
        }
        return i;
    }

    public MiuiChargeManager(@Inject Context context) {
        this.mContext = context;
        this.mBatteryStatus = new BatteryStatus(1, 0, 0, 0, 0, -1);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.BATTERY_CHANGED");
        intentFilter.addAction("miui.intent.action.ACTION_QUICK_CHARGE_TYPE");
        intentFilter.addAction("miui.intent.action.ACTION_WIRELESS_TX_TYPE");
        intentFilter.setPriority(b.a);
        context.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if ("android.intent.action.BATTERY_CHANGED".equals(intent.getAction())) {
                    boolean z = true;
                    int intExtra = intent.getIntExtra(MiStat.Param.STATUS, 1);
                    int intExtra2 = intent.getIntExtra("plugged", 0);
                    int intExtra3 = intent.getIntExtra(MiStat.Param.LEVEL, 0);
                    int unused = MiuiChargeManager.this.mRealLevel = intExtra3;
                    int access$100 = MiuiChargeManager.this.checkWireState(intExtra2, intExtra);
                    if (!BatteryStatus.isPluggedIn(MiuiChargeManager.this.mBatteryStatus.plugged) && BatteryStatus.isPluggedIn(intExtra2)) {
                        boolean unused2 = MiuiChargeManager.this.mIsChargeLevelAnimationRunning = false;
                        boolean unused3 = MiuiChargeManager.this.mNotUpdateLevelWhenBatteryChange = false;
                    }
                    if (!Constants.SUPPORT_BROADCAST_QUICK_CHARGE) {
                        if (!BatteryStatus.isPluggedIn(intExtra2) || !BatteryStatus.isChargingOrFull(intExtra)) {
                            z = false;
                        }
                        if (!z) {
                            MiuiChargeManager.this.mBatteryStatus.chargeSpeed = 0;
                            MiuiChargeManager.this.mBatteryStatus.chargeDeviceType = -1;
                            MiuiChargeManager.this.mHandler.removeCallbacks(MiuiChargeManager.this.mUpdateChargingFromPowerCenterRunnable);
                        }
                        boolean isCharging = MiuiChargeManager.this.mBatteryStatus.isCharging();
                        if (z && !isCharging) {
                            MiuiChargeManager.this.mHandler.removeCallbacks(MiuiChargeManager.this.mUpdateChargingFromPowerCenterRunnable);
                            MiuiChargeManager.this.mHandler.postDelayed(MiuiChargeManager.this.mUpdateChargingFromPowerCenterRunnable, 1000);
                            MiuiChargeManager.this.mHandler.postDelayed(MiuiChargeManager.this.mUpdateChargingFromPowerCenterRunnable, 3000);
                            MiuiChargeManager.this.mHandler.postDelayed(MiuiChargeManager.this.mUpdateChargingFromPowerCenterRunnable, 20000);
                            MiuiChargeManager.this.mHandler.postDelayed(MiuiChargeManager.this.mUpdateChargingFromPowerCenterRunnable, 120000);
                        }
                    }
                    boolean access$700 = MiuiChargeManager.this.isBatteryStatusChanged(intExtra3, intExtra2, intExtra);
                    MiuiChargeManager.this.mBatteryStatus.plugged = intExtra2;
                    MiuiChargeManager.this.mBatteryStatus.wireState = access$100;
                    MiuiChargeManager.this.mBatteryStatus.status = intExtra;
                    if (access$700) {
                        BatteryStatus access$200 = MiuiChargeManager.this.mBatteryStatus;
                        MiuiChargeManager miuiChargeManager = MiuiChargeManager.this;
                        access$200.chargeDeviceType = miuiChargeManager.getCurrentChargeDeviceType(miuiChargeManager.mBatteryStatus.wireState, MiuiChargeManager.this.mBatteryStatus.chargeDeviceType);
                        BatteryStatus access$2002 = MiuiChargeManager.this.mBatteryStatus;
                        MiuiChargeManager miuiChargeManager2 = MiuiChargeManager.this;
                        access$2002.chargeSpeed = miuiChargeManager2.getChargeSpeed(miuiChargeManager2.mBatteryStatus.wireState, MiuiChargeManager.this.mBatteryStatus.chargeDeviceType);
                        MiuiChargeManager.this.notifyBatteryStatusChanged();
                    }
                } else if ("miui.intent.action.ACTION_QUICK_CHARGE_TYPE".equals(intent.getAction()) && Constants.SUPPORT_BROADCAST_QUICK_CHARGE) {
                    int unused4 = MiuiChargeManager.this.mWiredChargeType = intent.getIntExtra("miui.intent.extra.quick_charge_type", -1);
                    if (MiuiChargeManager.this.mBatteryStatus.wireState == 11) {
                        MiuiChargeManager miuiChargeManager3 = MiuiChargeManager.this;
                        miuiChargeManager3.onChargeDeviceTypeChanged(miuiChargeManager3.mWiredChargeType);
                    }
                } else if ("miui.intent.action.ACTION_WIRELESS_TX_TYPE".equals(intent.getAction())) {
                    int unused5 = MiuiChargeManager.this.mWirelessChargeType = intent.getIntExtra("miui.intent.extra.wireless_tx_type", -1);
                    if (MiuiChargeManager.this.mBatteryStatus.wireState == 10) {
                        MiuiChargeManager miuiChargeManager4 = MiuiChargeManager.this;
                        miuiChargeManager4.onChargeDeviceTypeChanged(miuiChargeManager4.mWirelessChargeType);
                    }
                }
            }
        }, intentFilter);
    }

    /* access modifiers changed from: private */
    public boolean isBatteryStatusChanged(int i, int i2, int i3) {
        if (i == this.mBatteryStatus.level || ((this.mIsChargeLevelAnimationRunning || this.mNotUpdateLevelWhenBatteryChange) && i != 100)) {
            BatteryStatus batteryStatus = this.mBatteryStatus;
            if (i2 == batteryStatus.plugged && i3 == batteryStatus.status) {
                return false;
            }
            return true;
        }
        this.mBatteryStatus.level = i;
        return true;
    }

    /* access modifiers changed from: private */
    public void onChargeDeviceTypeChanged(int i) {
        BatteryStatus batteryStatus = this.mBatteryStatus;
        if (batteryStatus != null && i >= 0) {
            int chargeSpeed = getChargeSpeed(batteryStatus.wireState, i);
            BatteryStatus batteryStatus2 = this.mBatteryStatus;
            batteryStatus2.chargeSpeed = chargeSpeed;
            int currentChargeDeviceType = getCurrentChargeDeviceType(batteryStatus2.wireState, i);
            BatteryStatus batteryStatus3 = this.mBatteryStatus;
            if (currentChargeDeviceType != batteryStatus3.chargeDeviceType) {
                batteryStatus3.chargeDeviceType = i;
                notifyBatteryStatusChanged();
            }
        }
    }

    public void setIsChargeLevelAnimationRunning(boolean z) {
        if (!this.mIsChargeLevelAnimationRunning && z) {
            this.mHandler.removeCallbacks(this.mNotUpdateLevelRunnable);
        }
        if (this.mIsChargeLevelAnimationRunning && !z) {
            this.mNotUpdateLevelWhenBatteryChange = true;
            this.mHandler.removeCallbacks(this.mNotUpdateLevelRunnable);
            this.mHandler.postDelayed(this.mNotUpdateLevelRunnable, 3000);
        }
        this.mIsChargeLevelAnimationRunning = z;
    }

    public void updateBattery(int i) {
        BatteryStatus batteryStatus = this.mBatteryStatus;
        if (batteryStatus != null) {
            batteryStatus.level = i;
            notifyBatteryStatusChanged();
        }
    }

    /* access modifiers changed from: private */
    public int getCurrentChargeDeviceType(int i, int i2) {
        if (!Constants.SUPPORT_BROADCAST_QUICK_CHARGE) {
            return i2;
        }
        if (i == 10) {
            return this.mWirelessChargeType;
        }
        if (i == 11) {
            return this.mWiredChargeType;
        }
        return -1;
    }

    /* access modifiers changed from: private */
    public int getChargeSpeed(int i, int i2) {
        if (i != 10) {
            if (i == 11) {
                if (!ChargeUtils.isStrongSuperRapidCharge(i2)) {
                    if (ChargeUtils.isSuperRapidCharge(i2)) {
                        return 2;
                    }
                    if (ChargeUtils.isRapidCharge(i2)) {
                        return 1;
                    }
                }
            }
            return 0;
        } else if (!ChargeUtils.isWirelessStrongSuperRapidCharge(i2)) {
            if (ChargeUtils.isWirelessSuperRapidCharge(i2)) {
                return 2;
            }
            return 0;
        }
        return 3;
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("MiuiChargeManager state:");
        printWriter.print("  isChargeAnimationDisabled =");
        printWriter.println(ChargeUtils.isChargeAnimationDisabled());
        if (this.mBatteryStatus != null) {
            printWriter.print("  mLevel =");
            printWriter.println(this.mBatteryStatus.level);
            printWriter.print("  mWireState =");
            printWriter.println(this.mBatteryStatus.wireState);
            printWriter.print("  mChargeSpeed =");
            printWriter.println(this.mBatteryStatus.chargeSpeed);
        }
    }

    /* access modifiers changed from: private */
    public void getChargingStatusFromPowerCenter() {
        new AsyncTask<Void, Void, Boolean>() {
            /* access modifiers changed from: protected */
            public Boolean doInBackground(Void... voidArr) {
                boolean z;
                try {
                    z = MiuiChargeManager.this.mContext.getContentResolver().call(Uri.parse(MiuiChargeManager.PROVIDER_POWER_CENTER), MiuiChargeManager.METHOD_GET_POWER_SUPPLY_INFO, (String) null, (Bundle) null).getBoolean(MiuiChargeManager.KEY_QUICK_CHARGE);
                } catch (Exception unused) {
                    Slog.e("MiuiChargeManager", "cannot find the path getPowerSupplyInfo of content://com.miui.powercenter.provider");
                    z = false;
                }
                return Boolean.valueOf(z);
            }

            /* access modifiers changed from: protected */
            public void onPostExecute(Boolean bool) {
                if (bool.booleanValue()) {
                    MiuiChargeManager.this.mHandler.removeCallbacks(MiuiChargeManager.this.mUpdateChargingFromPowerCenterRunnable);
                    if (!MiuiChargeManager.this.isSuperQuickCharging() && !MiuiChargeManager.this.isQuickCharging()) {
                        MiuiChargeManager.this.mBatteryStatus.chargeSpeed = 1;
                        MiuiChargeManager.this.onChargeDeviceTypeChanged(1);
                    }
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
    }

    public boolean isQuickCharging() {
        BatteryStatus batteryStatus = this.mBatteryStatus;
        return batteryStatus != null && batteryStatus.chargeSpeed == 1;
    }

    public boolean isSuperQuickCharging() {
        BatteryStatus batteryStatus = this.mBatteryStatus;
        return batteryStatus != null && batteryStatus.chargeSpeed == 2;
    }

    public boolean isStrongSuperQuickCharging() {
        BatteryStatus batteryStatus = this.mBatteryStatus;
        return batteryStatus != null && batteryStatus.chargeSpeed == 3;
    }

    public boolean isUsbCharging() {
        BatteryStatus batteryStatus = this.mBatteryStatus;
        return batteryStatus != null && batteryStatus.isUsbPluggedIn();
    }

    /* access modifiers changed from: private */
    public void notifyBatteryStatusChanged() {
        if (this.mBatteryStatus != null) {
            Slog.i("MiuiChargeManager", "notifyBatteryStatusChanged:  status: " + this.mBatteryStatus.status + " isPlugged: " + this.mBatteryStatus.plugged + " level: " + this.mBatteryStatus.level + " wireState: " + this.mBatteryStatus.wireState + " chargeSpeed: " + this.mBatteryStatus.chargeSpeed + " mWiredChargeType: " + this.mWiredChargeType + " mWirelessChargeType: " + this.mWirelessChargeType + " chargeDeviceType: " + this.mBatteryStatus.chargeDeviceType);
            BatteryStatus batteryStatus = this.mBatteryStatus;
            int i = batteryStatus.status;
            int i2 = batteryStatus.plugged;
            int formatBatteryLevel = formatBatteryLevel(batteryStatus.level);
            BatteryStatus batteryStatus2 = this.mBatteryStatus;
            KeyguardUpdateMonitor.getInstance(this.mContext).onBatteryStatusChange(new BatteryStatus(i, i2, formatBatteryLevel, batteryStatus2.wireState, batteryStatus2.chargeSpeed, batteryStatus2.chargeDeviceType));
        }
    }
}
