package com.android.systemui.statusbar.policy;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.Vibrator;
import android.provider.MiuiSettings;
import android.provider.Settings;
import android.service.notification.ZenModeConfig;
import android.text.format.DateFormat;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.Dumpable;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.qs.GlobalSetting;
import com.android.systemui.settings.CurrentUserTracker;
import com.android.systemui.statusbar.policy.ZenModeController;
import com.android.systemui.util.Utils;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Consumer;
import miui.util.AudioManagerHelper;

public class ZenModeControllerImpl extends CurrentUserTracker implements ZenModeController, Dumpable {
    private final AlarmManager mAlarmManager;
    private final ArrayList<ZenModeController.Callback> mCallbacks = new ArrayList<>();
    private final Object mCallbacksLock = new Object();
    private ZenModeConfig mConfig;
    private final GlobalSetting mConfigSetting;
    private NotificationManager.Policy mConsolidatedNotificationPolicy;
    private final Context mContext;
    private volatile boolean mLastRingerMode;
    private volatile boolean mLastZenMode;
    private final GlobalSetting mModeSetting;
    private final NotificationManager mNoMan;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        /* class com.android.systemui.statusbar.policy.ZenModeControllerImpl.AnonymousClass5 */

        public void onReceive(Context context, Intent intent) {
            if ("android.app.action.NEXT_ALARM_CLOCK_CHANGED".equals(intent.getAction())) {
                ZenModeControllerImpl.this.fireNextAlarmChanged();
            }
            if ("android.os.action.ACTION_EFFECTS_SUPPRESSOR_CHANGED".equals(intent.getAction())) {
                ZenModeControllerImpl.this.fireEffectsSuppressorChanged();
            }
        }
    };
    private boolean mRegistered;
    private volatile boolean mRingerMode;
    private final BroadcastReceiver mRingerReceiver = new BroadcastReceiver() {
        /* class com.android.systemui.statusbar.policy.ZenModeControllerImpl.AnonymousClass6 */

        public void onReceive(Context context, Intent intent) {
            if ("android.media.RINGER_MODE_CHANGED".equals(intent.getAction())) {
                ZenModeControllerImpl zenModeControllerImpl = ZenModeControllerImpl.this;
                zenModeControllerImpl.mRingerMode = MiuiSettings.SoundMode.isSilenceModeOn(zenModeControllerImpl.mContext);
                ZenModeControllerImpl zenModeControllerImpl2 = ZenModeControllerImpl.this;
                zenModeControllerImpl2.mVibrateEnable = AudioManagerHelper.isVibrateEnabled(zenModeControllerImpl2.mContext);
                Log.d("ZenModeController", "onReceive: RINGER_MODE_CHANGED_ACTION, mVibrateEnable = " + ZenModeControllerImpl.this.mVibrateEnable + " mRingerMode = " + ZenModeControllerImpl.this.mRingerMode);
                ZenModeControllerImpl.this.onZenOrRingerModeMayChanged();
            }
        }
    };
    private GlobalSetting mRingerSetting;
    private final SetupObserver mSetupObserver;
    private int mUserId;
    private final UserManager mUserManager;
    private volatile boolean mVibrateEnable;
    private ContentObserver mVibrateEnableObserver;
    private volatile boolean mZenMode;
    private long mZenUpdateTime;

    static {
        Log.isLoggable("ZenModeController", 3);
    }

    public ZenModeControllerImpl(Context context, Handler handler, BroadcastDispatcher broadcastDispatcher) {
        super(broadcastDispatcher);
        this.mContext = context;
        this.mModeSetting = new GlobalSetting(this.mContext, handler, "zen_mode") {
            /* class com.android.systemui.statusbar.policy.ZenModeControllerImpl.AnonymousClass1 */

            /* access modifiers changed from: protected */
            @Override // com.android.systemui.qs.GlobalSetting
            public void handleValueChanged(int i) {
                ZenModeControllerImpl zenModeControllerImpl = ZenModeControllerImpl.this;
                zenModeControllerImpl.mVibrateEnable = AudioManagerHelper.isVibrateEnabled(zenModeControllerImpl.mContext);
                ZenModeControllerImpl zenModeControllerImpl2 = ZenModeControllerImpl.this;
                zenModeControllerImpl2.mZenMode = MiuiSettings.SoundMode.isZenModeOn(zenModeControllerImpl2.mContext);
                ZenModeControllerImpl.this.onZenOrRingerModeMayChanged();
            }
        };
        AnonymousClass2 r0 = new GlobalSetting(this.mContext, handler, "mode_ringer") {
            /* class com.android.systemui.statusbar.policy.ZenModeControllerImpl.AnonymousClass2 */

            /* access modifiers changed from: protected */
            @Override // com.android.systemui.qs.GlobalSetting
            public void handleValueChanged(int i) {
                ZenModeControllerImpl zenModeControllerImpl = ZenModeControllerImpl.this;
                zenModeControllerImpl.mVibrateEnable = AudioManagerHelper.isVibrateEnabled(zenModeControllerImpl.mContext);
                ZenModeControllerImpl zenModeControllerImpl2 = ZenModeControllerImpl.this;
                zenModeControllerImpl2.mRingerMode = MiuiSettings.SoundMode.isSilenceModeOn(zenModeControllerImpl2.mContext);
                ZenModeControllerImpl.this.onZenOrRingerModeMayChanged();
            }
        };
        this.mRingerSetting = r0;
        r0.setListening(true);
        this.mRingerSetting.onChange(false);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.media.RINGER_MODE_CHANGED");
        broadcastDispatcher.registerReceiverWithHandler(this.mRingerReceiver, intentFilter, handler);
        this.mVibrateEnableObserver = new ContentObserver(handler) {
            /* class com.android.systemui.statusbar.policy.ZenModeControllerImpl.AnonymousClass3 */

            public void onChange(boolean z) {
                ZenModeControllerImpl zenModeControllerImpl = ZenModeControllerImpl.this;
                zenModeControllerImpl.mVibrateEnable = AudioManagerHelper.isVibrateEnabled(zenModeControllerImpl.mContext);
                ZenModeControllerImpl.this.fireVibrateChanged();
            }
        };
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("vibrate_in_silent"), false, this.mVibrateEnableObserver, -1);
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("vibrate_in_normal"), false, this.mVibrateEnableObserver, -1);
        this.mVibrateEnable = AudioManagerHelper.isVibrateEnabled(this.mContext);
        this.mConfigSetting = new GlobalSetting(this.mContext, handler, "zen_mode_config_etag") {
            /* class com.android.systemui.statusbar.policy.ZenModeControllerImpl.AnonymousClass4 */

            /* access modifiers changed from: protected */
            @Override // com.android.systemui.qs.GlobalSetting
            public void handleValueChanged(int i) {
                ZenModeControllerImpl.this.updateZenModeConfig();
            }
        };
        this.mNoMan = (NotificationManager) context.getSystemService("notification");
        this.mModeSetting.setListening(true);
        updateZenMode(this.mModeSetting.getValue());
        this.mConfigSetting.setListening(true);
        updateZenModeConfig();
        updateConsolidatedNotificationPolicy();
        this.mAlarmManager = (AlarmManager) context.getSystemService("alarm");
        SetupObserver setupObserver = new SetupObserver(handler);
        this.mSetupObserver = setupObserver;
        setupObserver.register();
        this.mUserManager = (UserManager) context.getSystemService(UserManager.class);
        startTracking();
    }

    @Override // com.android.systemui.statusbar.policy.ZenModeController
    public boolean isVolumeRestricted() {
        return this.mUserManager.hasUserRestriction("no_adjust_volume", new UserHandle(this.mUserId));
    }

    @Override // com.android.systemui.statusbar.policy.ZenModeController
    public boolean areNotificationsHiddenInShade() {
        if (!this.mZenMode || (this.mConsolidatedNotificationPolicy.suppressedVisualEffects & 256) == 0) {
            return false;
        }
        return true;
    }

    public void addCallback(ZenModeController.Callback callback) {
        synchronized (this.mCallbacksLock) {
            this.mCallbacks.add(callback);
        }
    }

    public void removeCallback(ZenModeController.Callback callback) {
        synchronized (this.mCallbacksLock) {
            this.mCallbacks.remove(callback);
        }
    }

    @Override // com.android.systemui.statusbar.policy.ZenModeController
    public int getZen() {
        return this.mModeSetting.getValue();
    }

    @Override // com.android.systemui.statusbar.policy.ZenModeController
    public void setZen(int i, Uri uri, String str) {
        this.mNoMan.setZenMode(i, uri, str);
    }

    @Override // com.android.systemui.statusbar.policy.ZenModeController
    public boolean isZenAvailable() {
        return this.mSetupObserver.isDeviceProvisioned() && this.mSetupObserver.isUserSetup();
    }

    @Override // com.android.systemui.statusbar.policy.ZenModeController
    public ZenModeConfig.ZenRule getManualRule() {
        ZenModeConfig zenModeConfig = this.mConfig;
        if (zenModeConfig == null) {
            return null;
        }
        return zenModeConfig.manualRule;
    }

    @Override // com.android.systemui.statusbar.policy.ZenModeController
    public ZenModeConfig getConfig() {
        return this.mConfig;
    }

    @Override // com.android.systemui.statusbar.policy.ZenModeController
    public NotificationManager.Policy getConsolidatedPolicy() {
        return this.mConsolidatedNotificationPolicy;
    }

    @Override // com.android.systemui.statusbar.policy.ZenModeController
    public long getNextAlarm() {
        AlarmManager.AlarmClockInfo nextAlarmClock = this.mAlarmManager.getNextAlarmClock(this.mUserId);
        if (nextAlarmClock != null) {
            return nextAlarmClock.getTriggerTime();
        }
        return 0;
    }

    @Override // com.android.systemui.settings.CurrentUserTracker
    public void onUserSwitched(int i) {
        this.mUserId = i;
        if (this.mRegistered) {
            this.mContext.unregisterReceiver(this.mReceiver);
        }
        IntentFilter intentFilter = new IntentFilter("android.app.action.NEXT_ALARM_CLOCK_CHANGED");
        intentFilter.addAction("android.os.action.ACTION_EFFECTS_SUPPRESSOR_CHANGED");
        this.mContext.registerReceiverAsUser(this.mReceiver, new UserHandle(this.mUserId), intentFilter, null, null);
        this.mRegistered = true;
        this.mSetupObserver.register();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void fireNextAlarmChanged() {
        synchronized (this.mCallbacksLock) {
            Utils.safeForeach(this.mCallbacks, $$Lambda$ZenModeControllerImpl$6_S_aAoRd9fsiJr9D0TIwCJGb6M.INSTANCE);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void fireEffectsSuppressorChanged() {
        synchronized (this.mCallbacksLock) {
            Utils.safeForeach(this.mCallbacks, $$Lambda$ZenModeControllerImpl$SV0AVEr3ZD6I5F0ZOAtC6EOynk.INSTANCE);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void fireZenAvailableChanged(boolean z) {
        synchronized (this.mCallbacksLock) {
            Utils.safeForeach(this.mCallbacks, new Consumer(z) {
                /* class com.android.systemui.statusbar.policy.$$Lambda$ZenModeControllerImpl$SZ6Og1sK4NAnerjv0COJMr2bCU */
                public final /* synthetic */ boolean f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    ((ZenModeController.Callback) obj).onZenAvailableChanged(this.f$0);
                }
            });
        }
    }

    private void fireManualRuleChanged(ZenModeConfig.ZenRule zenRule) {
        synchronized (this.mCallbacksLock) {
            Utils.safeForeach(this.mCallbacks, new Consumer(zenRule) {
                /* class com.android.systemui.statusbar.policy.$$Lambda$ZenModeControllerImpl$8iaDxlkHjmysoUP7KwjUaBzkBiQ */
                public final /* synthetic */ ZenModeConfig.ZenRule f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    ((ZenModeController.Callback) obj).onManualRuleChanged(this.f$0);
                }
            });
        }
    }

    private void fireConsolidatedPolicyChanged(NotificationManager.Policy policy) {
        synchronized (this.mCallbacksLock) {
            Utils.safeForeach(this.mCallbacks, new Consumer(policy) {
                /* class com.android.systemui.statusbar.policy.$$Lambda$ZenModeControllerImpl$8ESweSQi2XbEG_Qu7VUYzDq1Zcs */
                public final /* synthetic */ NotificationManager.Policy f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    ((ZenModeController.Callback) obj).onConsolidatedPolicyChanged(this.f$0);
                }
            });
        }
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public void fireConfigChanged(ZenModeConfig zenModeConfig) {
        synchronized (this.mCallbacksLock) {
            Utils.safeForeach(this.mCallbacks, new Consumer(zenModeConfig) {
                /* class com.android.systemui.statusbar.policy.$$Lambda$ZenModeControllerImpl$idmtZJFosRgAGQLYktOBo_UGp5E */
                public final /* synthetic */ ZenModeConfig f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    ((ZenModeController.Callback) obj).onConfigChanged(this.f$0);
                }
            });
        }
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public void updateZenMode(int i) {
        this.mZenMode = MiuiSettings.SoundMode.isZenModeOn(this.mContext);
        onZenOrRingerModeMayChanged();
        this.mZenUpdateTime = System.currentTimeMillis();
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public void updateConsolidatedNotificationPolicy() {
        NotificationManager.Policy consolidatedNotificationPolicy = this.mNoMan.getConsolidatedNotificationPolicy();
        if (!Objects.equals(consolidatedNotificationPolicy, this.mConsolidatedNotificationPolicy)) {
            this.mConsolidatedNotificationPolicy = consolidatedNotificationPolicy;
            fireConsolidatedPolicyChanged(consolidatedNotificationPolicy);
        }
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public void updateZenModeConfig() {
        ZenModeConfig zenModeConfig = this.mNoMan.getZenModeConfig();
        if (!Objects.equals(zenModeConfig, this.mConfig)) {
            ZenModeConfig zenModeConfig2 = this.mConfig;
            ZenModeConfig.ZenRule zenRule = null;
            ZenModeConfig.ZenRule zenRule2 = zenModeConfig2 != null ? zenModeConfig2.manualRule : null;
            this.mConfig = zenModeConfig;
            this.mZenUpdateTime = System.currentTimeMillis();
            fireConfigChanged(zenModeConfig);
            if (zenModeConfig != null) {
                zenRule = zenModeConfig.manualRule;
            }
            if (!Objects.equals(zenRule2, zenRule)) {
                fireManualRuleChanged(zenRule);
            }
            NotificationManager.Policy consolidatedNotificationPolicy = this.mNoMan.getConsolidatedNotificationPolicy();
            if (!Objects.equals(consolidatedNotificationPolicy, this.mConsolidatedNotificationPolicy)) {
                this.mConsolidatedNotificationPolicy = consolidatedNotificationPolicy;
                fireConsolidatedPolicyChanged(consolidatedNotificationPolicy);
            }
        }
    }

    @Override // com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("ZenModeControllerImpl:");
        printWriter.println("  mZenMode=" + this.mZenMode);
        printWriter.println("  mVibrateEnable= " + this.mVibrateEnable);
        printWriter.println("  mRingerMode=" + this.mRingerMode);
        printWriter.println("  mConfig=" + this.mConfig);
        printWriter.println("  mConsolidatedNotificationPolicy=" + this.mConsolidatedNotificationPolicy);
        printWriter.println("  mZenUpdateTime=" + ((Object) DateFormat.format("MM-dd HH:mm:ss", this.mZenUpdateTime)));
    }

    /* access modifiers changed from: private */
    public final class SetupObserver extends ContentObserver {
        private boolean mRegistered;
        private final ContentResolver mResolver;

        public SetupObserver(Handler handler) {
            super(handler);
            this.mResolver = ZenModeControllerImpl.this.mContext.getContentResolver();
        }

        public boolean isUserSetup() {
            return Settings.Secure.getIntForUser(this.mResolver, "user_setup_complete", 0, ZenModeControllerImpl.this.mUserId) != 0;
        }

        public boolean isDeviceProvisioned() {
            return Settings.Global.getInt(this.mResolver, "device_provisioned", 0) != 0;
        }

        public void register() {
            if (this.mRegistered) {
                this.mResolver.unregisterContentObserver(this);
            }
            this.mResolver.registerContentObserver(Settings.Global.getUriFor("device_provisioned"), false, this);
            this.mResolver.registerContentObserver(Settings.Secure.getUriFor("user_setup_complete"), false, this, ZenModeControllerImpl.this.mUserId);
            this.mRegistered = true;
            ZenModeControllerImpl zenModeControllerImpl = ZenModeControllerImpl.this;
            zenModeControllerImpl.fireZenAvailableChanged(zenModeControllerImpl.isZenAvailable());
        }

        public void onChange(boolean z, Uri uri) {
            if (Settings.Global.getUriFor("device_provisioned").equals(uri) || Settings.Secure.getUriFor("user_setup_complete").equals(uri)) {
                ZenModeControllerImpl zenModeControllerImpl = ZenModeControllerImpl.this;
                zenModeControllerImpl.fireZenAvailableChanged(zenModeControllerImpl.isZenAvailable());
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onZenOrRingerModeMayChanged() {
        Log.d("ZenModeController", "onZenOrRingerModeMayChanged: mZenMode = " + this.mZenMode + ", mRingerMode =" + this.mRingerMode);
        if (this.mLastZenMode != this.mZenMode || this.mLastRingerMode != this.mRingerMode) {
            this.mLastZenMode = this.mZenMode;
            this.mLastRingerMode = this.mRingerMode;
            fireZenOrRingerChanged();
        }
    }

    /* access modifiers changed from: protected */
    public void fireVibrateChanged() {
        Utils.safeForeach(this.mCallbacks, new Consumer(this.mVibrateEnable) {
            /* class com.android.systemui.statusbar.policy.$$Lambda$ZenModeControllerImpl$uKt_3WXZ4L13263SobuGkOac7Uw */
            public final /* synthetic */ boolean f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                ((ZenModeController.Callback) obj).onVibrateChanged(this.f$0);
            }
        });
    }

    @Override // com.android.systemui.statusbar.policy.ZenModeController
    public boolean isVibrateOn() {
        return this.mVibrateEnable;
    }

    @Override // com.android.systemui.statusbar.policy.ZenModeController
    public boolean isZenModeOn() {
        return this.mZenMode;
    }

    @Override // com.android.systemui.statusbar.policy.ZenModeController
    public boolean isRingerModeOn() {
        return this.mRingerMode;
    }

    private void fireZenOrRingerChanged() {
        Utils.safeForeach(this.mCallbacks, new Consumer(this.mZenMode, this.mRingerMode) {
            /* class com.android.systemui.statusbar.policy.$$Lambda$ZenModeControllerImpl$CowTOyLJxNXw1gez1pZVPvO14fs */
            public final /* synthetic */ boolean f$0;
            public final /* synthetic */ boolean f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                ((ZenModeController.Callback) obj).onZenOrRingerChanged(this.f$0, this.f$1);
            }
        });
    }

    @Override // com.android.systemui.statusbar.policy.ZenModeController
    public void toggleSilent() {
        AudioManagerHelper.toggleSilent(this.mContext, 4);
    }

    @Override // com.android.systemui.statusbar.policy.ZenModeController
    public void toggleVibrate() {
        AudioManagerHelper.toggleVibrateSetting(this.mContext);
    }

    @Override // com.android.systemui.statusbar.policy.ZenModeController
    public boolean isVibratorAvailable() {
        return ((Vibrator) this.mContext.getSystemService("vibrator")).hasVibrator();
    }
}
