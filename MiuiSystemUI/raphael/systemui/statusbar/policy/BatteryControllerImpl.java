package com.android.systemui.statusbar.policy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.settingslib.fuelgauge.BatterySaverUtils;
import com.android.settingslib.fuelgauge.Estimate;
import com.android.settingslib.utils.PowerUtil;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.power.EnhancedEstimates;
import com.android.systemui.statusbar.policy.BatteryController;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;

public class BatteryControllerImpl extends BroadcastReceiver implements BatteryController {
    private static final boolean DEBUG = Log.isLoggable("BatteryController", 3);
    private boolean mAodPowerSave;
    protected int mBatteryStyle = 1;
    private final Handler mBgHandler;
    private final BroadcastDispatcher mBroadcastDispatcher;
    protected final ArrayList<BatteryController.BatteryStateChangeCallback> mChangeCallbacks = new ArrayList<>();
    private boolean mCharged;
    protected boolean mCharging;
    protected final Context mContext;
    protected boolean mDemoMode;
    private Estimate mEstimate;
    private final EnhancedEstimates mEstimates;
    private final ArrayList<BatteryController.EstimateFetchCompletion> mFetchCallbacks = new ArrayList<>();
    private boolean mFetchingEstimate = false;
    @VisibleForTesting
    boolean mHasReceivedBattery = false;
    protected boolean mIsExtremePowerSaveMode;
    protected boolean mIsPowerSaveMode;
    protected int mLevel;
    private final Handler mMainHandler;
    protected boolean mPluggedIn;
    private final PowerManager mPowerManager;
    protected boolean mPowerSave;
    private boolean mTestmode = false;

    /* access modifiers changed from: protected */
    public void updateSecondSpace() {
    }

    @VisibleForTesting
    public BatteryControllerImpl(Context context, EnhancedEstimates enhancedEstimates, PowerManager powerManager, BroadcastDispatcher broadcastDispatcher, Handler handler, Handler handler2) {
        this.mContext = context;
        this.mMainHandler = handler;
        this.mBgHandler = handler2;
        this.mPowerManager = powerManager;
        this.mEstimates = enhancedEstimates;
        this.mBroadcastDispatcher = broadcastDispatcher;
    }

    /* access modifiers changed from: protected */
    public void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.BATTERY_CHANGED");
        intentFilter.addAction("android.os.action.POWER_SAVE_MODE_CHANGED");
        intentFilter.addAction("android.intent.action.USER_SWITCHED");
        intentFilter.addAction("com.android.systemui.BATTERY_LEVEL_TEST");
        this.mBroadcastDispatcher.registerReceiver(this, intentFilter);
    }

    @Override // com.android.systemui.statusbar.policy.BatteryController
    public void init() {
        Intent registerReceiver;
        registerReceiver();
        if (!this.mHasReceivedBattery && (registerReceiver = this.mContext.registerReceiver(null, new IntentFilter("android.intent.action.BATTERY_CHANGED"))) != null && !this.mHasReceivedBattery) {
            onReceive(this.mContext, registerReceiver);
        }
        updatePowerSave();
        updateEstimate();
    }

    @Override // com.android.systemui.Dumpable
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
        printWriter.println(this.mPowerSave);
    }

    @Override // com.android.systemui.statusbar.policy.BatteryController
    public void setPowerSaveMode(boolean z) {
        BatterySaverUtils.setPowerSaveMode(this.mContext, z, true);
    }

    public void addCallback(BatteryController.BatteryStateChangeCallback batteryStateChangeCallback) {
        synchronized (this.mChangeCallbacks) {
            this.mChangeCallbacks.add(batteryStateChangeCallback);
        }
        batteryStateChangeCallback.onExtremePowerSaveChanged(this.mIsExtremePowerSaveMode);
        batteryStateChangeCallback.onBatteryStyleChanged(this.mBatteryStyle);
        batteryStateChangeCallback.onPowerSaveChanged(this.mIsPowerSaveMode);
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
        Log.d("BatteryController", "onReceive: Intent = " + intent + " action = " + action);
        boolean z = true;
        if (action.equals("android.intent.action.BATTERY_CHANGED")) {
            if (!this.mTestmode || intent.getBooleanExtra("testmode", false)) {
                this.mHasReceivedBattery = true;
                this.mLevel = (int) ((((float) intent.getIntExtra("level", 0)) * 100.0f) / ((float) intent.getIntExtra("scale", 100)));
                this.mPluggedIn = intent.getIntExtra("plugged", 0) != 0;
                int intExtra = intent.getIntExtra("status", 1);
                boolean z2 = intExtra == 5;
                this.mCharged = z2;
                if (!z2 && intExtra != 2) {
                    z = false;
                }
                this.mCharging = z;
                fireBatteryLevelChanged();
            }
        } else if (action.equals("android.os.action.POWER_SAVE_MODE_CHANGED")) {
            updatePowerSave();
        } else if (action.equals("com.android.systemui.BATTERY_LEVEL_TEST")) {
            this.mTestmode = true;
            this.mMainHandler.post(new Runnable() {
                /* class com.android.systemui.statusbar.policy.BatteryControllerImpl.AnonymousClass1 */
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
                        BatteryControllerImpl.this.mTestmode = false;
                        this.dummy.putExtra("level", this.saveLevel);
                        this.dummy.putExtra("plugged", this.savePlugged);
                        this.dummy.putExtra("testmode", false);
                    } else {
                        this.dummy.putExtra("level", i);
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
                        BatteryControllerImpl.this.mMainHandler.postDelayed(this, 200);
                    }
                }
            });
        } else if ("android.intent.action.USER_SWITCHED".equals(action)) {
            updateSecondSpace();
        }
    }

    @Override // com.android.systemui.statusbar.policy.BatteryController
    public boolean isPowerSave() {
        return this.mPowerSave;
    }

    @Override // com.android.systemui.statusbar.policy.BatteryController
    public boolean isAodPowerSave() {
        return this.mAodPowerSave;
    }

    @Override // com.android.systemui.statusbar.policy.BatteryController
    public void getEstimatedTimeRemainingString(BatteryController.EstimateFetchCompletion estimateFetchCompletion) {
        synchronized (this.mFetchCallbacks) {
            this.mFetchCallbacks.add(estimateFetchCompletion);
        }
        updateEstimateInBackground();
    }

    private String generateTimeRemainingString() {
        synchronized (this.mFetchCallbacks) {
            if (this.mEstimate == null) {
                return null;
            }
            return PowerUtil.getBatteryRemainingShortStringFormatted(this.mContext, this.mEstimate.getEstimateMillis());
        }
    }

    private void updateEstimateInBackground() {
        if (!this.mFetchingEstimate) {
            this.mFetchingEstimate = true;
            this.mBgHandler.post(new Runnable() {
                /* class com.android.systemui.statusbar.policy.$$Lambda$BatteryControllerImpl$Q2m5_jQFbUIrN5x5MkihyCoos8 */

                public final void run() {
                    BatteryControllerImpl.this.lambda$updateEstimateInBackground$0$BatteryControllerImpl();
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$updateEstimateInBackground$0 */
    public /* synthetic */ void lambda$updateEstimateInBackground$0$BatteryControllerImpl() {
        synchronized (this.mFetchCallbacks) {
            this.mEstimate = null;
            if (this.mEstimates.isHybridNotificationEnabled()) {
                updateEstimate();
            }
        }
        this.mFetchingEstimate = false;
        this.mMainHandler.post(new Runnable() {
            /* class com.android.systemui.statusbar.policy.$$Lambda$BatteryControllerImpl$xVvPxv9usTpbGvWx3jH4_VH1nvI */

            public final void run() {
                BatteryControllerImpl.lambda$xVvPxv9usTpbGvWx3jH4_VH1nvI(BatteryControllerImpl.this);
            }
        });
    }

    /* access modifiers changed from: private */
    public void notifyEstimateFetchCallbacks() {
        synchronized (this.mFetchCallbacks) {
            String generateTimeRemainingString = generateTimeRemainingString();
            Iterator<BatteryController.EstimateFetchCompletion> it = this.mFetchCallbacks.iterator();
            while (it.hasNext()) {
                it.next().onBatteryRemainingEstimateRetrieved(generateTimeRemainingString);
            }
            this.mFetchCallbacks.clear();
        }
    }

    private void updateEstimate() {
        Estimate cachedEstimateIfAvailable = Estimate.getCachedEstimateIfAvailable(this.mContext);
        this.mEstimate = cachedEstimateIfAvailable;
        if (cachedEstimateIfAvailable == null) {
            Estimate estimate = this.mEstimates.getEstimate();
            this.mEstimate = estimate;
            if (estimate != null) {
                Estimate.storeCachedEstimate(this.mContext, estimate);
            }
        }
    }

    private void updatePowerSave() {
        setPowerSave(this.mPowerManager.isPowerSaveMode());
    }

    private void setPowerSave(boolean z) {
        if (z != this.mPowerSave) {
            this.mPowerSave = z;
            this.mAodPowerSave = this.mPowerManager.getPowerSaveState(14).batterySaverEnabled;
            if (DEBUG) {
                StringBuilder sb = new StringBuilder();
                sb.append("Power save is ");
                sb.append(this.mPowerSave ? "on" : "off");
                Log.d("BatteryController", sb.toString());
            }
            firePowerSaveChanged();
        }
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

    private void firePowerSaveChanged() {
        synchronized (this.mChangeCallbacks) {
            for (int i = 0; i < this.mChangeCallbacks.size(); i++) {
            }
        }
    }

    @Override // com.android.systemui.DemoMode
    public void dispatchDemoCommand(String str, Bundle bundle) {
        if (!this.mDemoMode && str.equals("enter")) {
            this.mDemoMode = true;
            this.mBroadcastDispatcher.unregisterReceiver(this);
        } else if (this.mDemoMode && str.equals("exit")) {
            this.mDemoMode = false;
            registerReceiver();
            updatePowerSave();
        } else if (this.mDemoMode && str.equals("battery")) {
            String string = bundle.getString("level");
            String string2 = bundle.getString("plugged");
            String string3 = bundle.getString("powersave");
            if (string != null) {
                this.mLevel = Math.min(Math.max(Integer.parseInt(string), 0), 100);
            }
            if (string2 != null) {
                this.mPluggedIn = Boolean.parseBoolean(string2);
            }
            if (string3 != null) {
                this.mPowerSave = string3.equals("true");
                firePowerSaveChanged();
            }
            fireBatteryLevelChanged();
        }
    }
}
