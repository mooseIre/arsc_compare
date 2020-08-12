package com.android.systemui.statusbar.policy;

import android.app.ActivityManager;
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
import android.provider.Settings;
import android.service.notification.Condition;
import android.service.notification.IConditionListener;
import android.service.notification.ZenModeConfig;
import android.util.Log;
import android.util.Slog;
import com.android.systemui.qs.GlobalSetting;
import com.android.systemui.settings.CurrentUserTracker;
import com.android.systemui.statusbar.policy.ZenModeController;
import com.android.systemui.util.Utils;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.function.Consumer;

public class ZenModeControllerImpl extends CurrentUserTracker implements ZenModeController {
    /* access modifiers changed from: private */
    public static final boolean DEBUG = Log.isLoggable("ZenModeController", 3);
    private final AlarmManager mAlarmManager;
    private final ArrayList<ZenModeController.Callback> mCallbacks = new ArrayList<>();
    private final LinkedHashMap<Uri, Condition> mConditions = new LinkedHashMap<>();
    private ZenModeConfig mConfig;
    private final GlobalSetting mConfigSetting;
    /* access modifiers changed from: private */
    public final Context mContext;
    private final IConditionListener mListener = new IConditionListener.Stub() {
        public void onConditionsReceived(Condition[] conditionArr) {
            int i;
            if (ZenModeControllerImpl.DEBUG) {
                StringBuilder sb = new StringBuilder();
                sb.append("onConditionsReceived ");
                if (conditionArr == null) {
                    i = 0;
                } else {
                    i = conditionArr.length;
                }
                sb.append(i);
                sb.append(" mRequesting=");
                sb.append(ZenModeControllerImpl.this.mRequesting);
                Slog.d("ZenModeController", sb.toString());
            }
            if (ZenModeControllerImpl.this.mRequesting) {
                ZenModeControllerImpl.this.updateConditions(conditionArr);
            }
        }
    };
    private final GlobalSetting mModeSetting;
    private final NotificationManager mNoMan;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
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
    /* access modifiers changed from: private */
    public boolean mRequesting;
    private final SetupObserver mSetupObserver;
    /* access modifiers changed from: private */
    public int mUserId;
    private final UserManager mUserManager;

    public ZenModeControllerImpl(Context context, Handler handler) {
        super(context);
        this.mContext = context;
        this.mModeSetting = new GlobalSetting(this.mContext, handler, "zen_mode") {
            /* access modifiers changed from: protected */
            public void handleValueChanged(int i) {
                ZenModeControllerImpl.this.fireZenChanged(i);
            }
        };
        this.mConfigSetting = new GlobalSetting(this.mContext, handler, "zen_mode_config_etag") {
            /* access modifiers changed from: protected */
            public void handleValueChanged(int i) {
                ZenModeControllerImpl.this.updateZenModeConfig();
            }
        };
        this.mNoMan = (NotificationManager) context.getSystemService("notification");
        this.mConfig = this.mNoMan.getZenModeConfig();
        this.mModeSetting.setListening(true);
        this.mConfigSetting.setListening(true);
        this.mAlarmManager = (AlarmManager) context.getSystemService("alarm");
        this.mSetupObserver = new SetupObserver(handler);
        this.mSetupObserver.register();
        this.mUserManager = (UserManager) context.getSystemService(UserManager.class);
        startTracking();
    }

    public void addCallback(ZenModeController.Callback callback) {
        this.mCallbacks.add(callback);
    }

    public int getZen() {
        return this.mModeSetting.getValue();
    }

    public boolean isZenAvailable() {
        return this.mSetupObserver.isDeviceProvisioned() && this.mSetupObserver.isUserSetup();
    }

    public ZenModeConfig getConfig() {
        return this.mConfig;
    }

    public void onUserSwitched(int i) {
        this.mUserId = i;
        if (this.mRegistered) {
            this.mContext.unregisterReceiver(this.mReceiver);
        }
        IntentFilter intentFilter = new IntentFilter("android.app.action.NEXT_ALARM_CLOCK_CHANGED");
        intentFilter.addAction("android.os.action.ACTION_EFFECTS_SUPPRESSOR_CHANGED");
        this.mContext.registerReceiverAsUser(this.mReceiver, new UserHandle(this.mUserId), intentFilter, (String) null, (Handler) null);
        this.mRegistered = true;
        this.mSetupObserver.register();
    }

    public int getCurrentUser() {
        return ActivityManager.getCurrentUser();
    }

    /* access modifiers changed from: private */
    public void fireNextAlarmChanged() {
        Utils.safeForeach(this.mCallbacks, $$Lambda$ZenModeControllerImpl$6_S_aAoRd9fsiJr9D0TIwCJGb6M.INSTANCE);
    }

    /* access modifiers changed from: private */
    public void fireEffectsSuppressorChanged() {
        Utils.safeForeach(this.mCallbacks, $$Lambda$ZenModeControllerImpl$SV0AVEr3ZD6I5F0ZOAtC6EOynk.INSTANCE);
    }

    /* access modifiers changed from: private */
    public void fireZenChanged(int i) {
        Utils.safeForeach(this.mCallbacks, new Consumer(i) {
            private final /* synthetic */ int f$0;

            {
                this.f$0 = r1;
            }

            public final void accept(Object obj) {
                ((ZenModeController.Callback) obj).onZenChanged(this.f$0);
            }
        });
    }

    /* access modifiers changed from: private */
    public void fireZenAvailableChanged(boolean z) {
        Utils.safeForeach(this.mCallbacks, new Consumer(z) {
            private final /* synthetic */ boolean f$0;

            {
                this.f$0 = r1;
            }

            public final void accept(Object obj) {
                ((ZenModeController.Callback) obj).onZenAvailableChanged(this.f$0);
            }
        });
    }

    private void fireConditionsChanged(Condition[] conditionArr) {
        Utils.safeForeach(this.mCallbacks, new Consumer(conditionArr) {
            private final /* synthetic */ Condition[] f$0;

            {
                this.f$0 = r1;
            }

            public final void accept(Object obj) {
                ((ZenModeController.Callback) obj).onConditionsChanged(this.f$0);
            }
        });
    }

    private void fireManualRuleChanged(ZenModeConfig.ZenRule zenRule) {
        Utils.safeForeach(this.mCallbacks, new Consumer(zenRule) {
            private final /* synthetic */ ZenModeConfig.ZenRule f$0;

            {
                this.f$0 = r1;
            }

            public final void accept(Object obj) {
                ((ZenModeController.Callback) obj).onManualRuleChanged(this.f$0);
            }
        });
    }

    /* access modifiers changed from: protected */
    public void fireConfigChanged(ZenModeConfig zenModeConfig) {
        Utils.safeForeach(this.mCallbacks, new Consumer(zenModeConfig) {
            private final /* synthetic */ ZenModeConfig f$0;

            {
                this.f$0 = r1;
            }

            public final void accept(Object obj) {
                ((ZenModeController.Callback) obj).onConfigChanged(this.f$0);
            }
        });
    }

    /* access modifiers changed from: private */
    public void updateConditions(Condition[] conditionArr) {
        if (conditionArr != null && conditionArr.length != 0) {
            for (Condition condition : conditionArr) {
                if ((condition.flags & 1) != 0) {
                    this.mConditions.put(condition.id, condition);
                }
            }
            fireConditionsChanged((Condition[]) this.mConditions.values().toArray(new Condition[this.mConditions.values().size()]));
        }
    }

    /* access modifiers changed from: private */
    public void updateZenModeConfig() {
        ZenModeConfig zenModeConfig = this.mNoMan.getZenModeConfig();
        if (!Objects.equals(zenModeConfig, this.mConfig)) {
            ZenModeConfig zenModeConfig2 = this.mConfig;
            ZenModeConfig.ZenRule zenRule = null;
            ZenModeConfig.ZenRule zenRule2 = zenModeConfig2 != null ? zenModeConfig2.manualRule : null;
            this.mConfig = zenModeConfig;
            fireConfigChanged(zenModeConfig);
            if (zenModeConfig != null) {
                zenRule = zenModeConfig.manualRule;
            }
            if (!Objects.equals(zenRule2, zenRule)) {
                fireManualRuleChanged(zenRule);
            }
        }
    }

    private final class SetupObserver extends ContentObserver {
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
}
