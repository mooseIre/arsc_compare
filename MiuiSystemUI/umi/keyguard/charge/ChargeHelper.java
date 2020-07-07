package com.android.keyguard.charge;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.Settings;
import com.google.android.collect.Lists;
import java.util.ArrayList;
import java.util.Iterator;

public class ChargeHelper {
    private static volatile ChargeHelper sInstance;
    private final BroadcastReceiver mExtremePowerReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            boolean unused = ChargeHelper.this.mIsExtremePowerSaveMode = intent.getBooleanExtra("EXTREME_POWER_SAVE_MODE_OPEN", false);
            Iterator it = ChargeHelper.this.mExtremePowerSaveModeChangeCallbacks.iterator();
            while (it.hasNext()) {
                ((ExtremePowerSaveModeChangeCallback) it.next()).onModeChange();
            }
        }
    };
    /* access modifiers changed from: private */
    public ArrayList<ExtremePowerSaveModeChangeCallback> mExtremePowerSaveModeChangeCallbacks = Lists.newArrayList();
    /* access modifiers changed from: private */
    public boolean mIsExtremePowerSaveMode;

    public interface ExtremePowerSaveModeChangeCallback {
        void onModeChange();
    }

    public static ChargeHelper getInstance(Context context) {
        if (sInstance == null) {
            synchronized (ChargeHelper.class) {
                if (sInstance == null) {
                    sInstance = new ChargeHelper(context);
                }
            }
        }
        return sInstance;
    }

    private ChargeHelper(Context context) {
        this.mIsExtremePowerSaveMode = Settings.Secure.getInt(context.getContentResolver(), "EXTREME_POWER_MODE_ENABLE", 0) == 1;
        context.registerReceiverAsUser(this.mExtremePowerReceiver, UserHandle.CURRENT, new IntentFilter("miui.intent.action.EXTREME_POWER_SAVE_MODE_CHANGED"), (String) null, (Handler) null);
    }

    public boolean isExtremePowerModeEnabled(Context context) {
        return this.mIsExtremePowerSaveMode;
    }
}
