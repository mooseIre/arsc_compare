package com.android.systemui.statusbar.policy;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import com.android.systemui.Dependency;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.power.EnhancedEstimates;

public class MiuiBatteryControllerImpl extends BatteryControllerImpl {
    ContentObserver mBatteryExtremeSaveModeChangeObserver = new ContentObserver(new Handler()) {
        /* class com.android.systemui.statusbar.policy.MiuiBatteryControllerImpl.AnonymousClass3 */

        public void onChange(boolean z) {
            MiuiBatteryControllerImpl miuiBatteryControllerImpl = MiuiBatteryControllerImpl.this;
            boolean z2 = false;
            if (Settings.System.getIntForUser(miuiBatteryControllerImpl.mContext.getContentResolver(), "EXTREME_POWER_MODE_ENABLE", 0, -2) == 1) {
                z2 = true;
            }
            miuiBatteryControllerImpl.mIsExtremePowerSaveMode = z2;
            MiuiBatteryControllerImpl.this.fireExtremePowerSaveChanged();
        }
    };
    ContentObserver mBatterySaveModeChangeObserver = new ContentObserver(new Handler()) {
        /* class com.android.systemui.statusbar.policy.MiuiBatteryControllerImpl.AnonymousClass2 */

        public void onChange(boolean z) {
            MiuiBatteryControllerImpl miuiBatteryControllerImpl = MiuiBatteryControllerImpl.this;
            boolean z2 = false;
            if (Settings.System.getIntForUser(miuiBatteryControllerImpl.mContext.getContentResolver(), "POWER_SAVE_MODE_OPEN", 0, -2) == 1) {
                z2 = true;
            }
            miuiBatteryControllerImpl.mIsPowerSaveMode = z2;
            MiuiBatteryControllerImpl.this.firePowerSaverChanged();
        }
    };
    ContentObserver mBatteryStyleChangeObserver = new ContentObserver(new Handler()) {
        /* class com.android.systemui.statusbar.policy.MiuiBatteryControllerImpl.AnonymousClass1 */

        public void onChange(boolean z) {
            MiuiBatteryControllerImpl miuiBatteryControllerImpl = MiuiBatteryControllerImpl.this;
            miuiBatteryControllerImpl.mBatteryStyle = Settings.System.getIntForUser(miuiBatteryControllerImpl.mContext.getContentResolver(), "battery_indicator_style", 1, -2);
            synchronized (MiuiBatteryControllerImpl.this.mChangeCallbacks) {
                int size = MiuiBatteryControllerImpl.this.mChangeCallbacks.size();
                for (int i = 0; i < size; i++) {
                    MiuiBatteryControllerImpl.this.mChangeCallbacks.get(i).onBatteryStyleChanged(MiuiBatteryControllerImpl.this.mBatteryStyle);
                }
            }
        }
    };

    public MiuiBatteryControllerImpl(Context context, EnhancedEstimates enhancedEstimates, PowerManager powerManager, BroadcastDispatcher broadcastDispatcher, Handler handler, Handler handler2) {
        super(context, enhancedEstimates, powerManager, broadcastDispatcher, handler, handler2);
        ((DemoModeController) Dependency.get(DemoModeController.class)).addCallback(this);
    }

    @Override // com.android.systemui.statusbar.policy.BatteryController, com.android.systemui.statusbar.policy.BatteryControllerImpl
    public void init() {
        registerReceiver();
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("battery_indicator_style"), false, this.mBatteryStyleChangeObserver, -1);
        this.mBatteryStyleChangeObserver.onChange(false);
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("POWER_SAVE_MODE_OPEN"), false, this.mBatterySaveModeChangeObserver, -1);
        this.mBatterySaveModeChangeObserver.onChange(false);
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("EXTREME_POWER_MODE_ENABLE"), false, this.mBatteryExtremeSaveModeChangeObserver, -1);
        this.mBatteryExtremeSaveModeChangeObserver.onChange(false);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void firePowerSaverChanged() {
        synchronized (this.mChangeCallbacks) {
            int size = this.mChangeCallbacks.size();
            for (int i = 0; i < size; i++) {
                this.mChangeCallbacks.get(i).onPowerSaveChanged(this.mIsPowerSaveMode);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void fireExtremePowerSaveChanged() {
        synchronized (this.mChangeCallbacks) {
            int size = this.mChangeCallbacks.size();
            for (int i = 0; i < size; i++) {
                this.mChangeCallbacks.get(i).onExtremePowerSaveChanged(this.mDemoMode ? false : this.mIsExtremePowerSaveMode);
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.policy.BatteryControllerImpl
    public void updateSecondSpace() {
        this.mBatteryStyleChangeObserver.onChange(false);
        this.mBatterySaveModeChangeObserver.onChange(false);
        this.mBatteryExtremeSaveModeChangeObserver.onChange(false);
    }

    @Override // com.android.systemui.DemoMode, com.android.systemui.statusbar.policy.BatteryControllerImpl
    public void dispatchDemoCommand(String str, Bundle bundle) {
        if (!this.mDemoMode && str.equals("enter")) {
            this.mDemoMode = true;
            fireDemoModeChanged(str, bundle);
        } else if (this.mDemoMode && str.equals("exit")) {
            this.mDemoMode = false;
            fireDemoModeChanged(str, bundle);
        }
    }

    private void fireDemoModeChanged(String str, Bundle bundle) {
        int size = this.mChangeCallbacks.size();
        for (int i = 0; i < size; i++) {
            this.mChangeCallbacks.get(i).dispatchDemoCommand(str, bundle);
        }
    }
}
