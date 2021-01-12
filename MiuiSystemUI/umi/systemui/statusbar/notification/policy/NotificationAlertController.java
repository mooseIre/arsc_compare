package com.android.systemui.statusbar.notification.policy;

import android.app.INotificationManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.SystemClock;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.faceunlock.MiuiFaceUnlockManager;
import com.android.keyguard.injector.KeyguardSensorInjector;
import com.android.keyguard.utils.MiuiKeyguardUtils;
import com.android.systemui.Dependency;
import com.android.systemui.keyguard.ScreenLifecycle;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.NotificationLockscreenUserManager;
import com.android.systemui.statusbar.notification.NotificationEntryListener;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.NotificationUtil;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.phone.NotificationGroupManager;
import com.android.systemui.statusbar.phone.StatusBarKeyguardViewManager;
import com.android.systemui.statusbar.policy.ZenModeController;
import com.miui.systemui.DebugConfig;
import com.miui.systemui.SettingsManager;
import java.util.ArrayList;

public class NotificationAlertController {
    /* access modifiers changed from: private */
    public static final boolean DEBUG = DebugConfig.DEBUG_NOTIFICATION;
    /* access modifiers changed from: private */
    public int mBarState;
    private Handler mBgHandler = new Handler((Looper) Dependency.get(Dependency.BG_LOOPER));
    private Context mContext;
    private NotificationEntryManager mEntryManager;
    private NotificationGroupManager mGroupManager;
    private INotificationManager mNm;
    private NotificationLockscreenUserManager mNotificationLockscreenUserManager;
    private SettingsManager mSettingsManager;
    private StatusBarKeyguardViewManager mStatusBarKeyguardManager;
    private StatusBarStateController mStatusBarStateController;
    private ZenModeController mZenModeController;

    public NotificationAlertController(Context context, INotificationManager iNotificationManager, NotificationEntryManager notificationEntryManager, NotificationGroupManager notificationGroupManager, StatusBarStateController statusBarStateController, ScreenLifecycle screenLifecycle, ZenModeController zenModeController, SettingsManager settingsManager, NotificationLockscreenUserManager notificationLockscreenUserManager, StatusBarKeyguardViewManager statusBarKeyguardViewManager) {
        this.mContext = context;
        this.mNm = iNotificationManager;
        this.mEntryManager = notificationEntryManager;
        this.mGroupManager = notificationGroupManager;
        this.mStatusBarStateController = statusBarStateController;
        this.mZenModeController = zenModeController;
        this.mSettingsManager = settingsManager;
        this.mNotificationLockscreenUserManager = notificationLockscreenUserManager;
        this.mStatusBarKeyguardManager = statusBarKeyguardViewManager;
    }

    public void start() {
        this.mEntryManager.addNotificationEntryListener(new NotificationEntryListener() {
            public void onNotificationAdded(NotificationEntry notificationEntry) {
                if (NotificationAlertController.DEBUG) {
                    Log.d("NotificationAlertController", "onNotificationAdded " + notificationEntry.getKey());
                }
                NotificationAlertController.this.buzzBeepBlink(notificationEntry.getKey());
                NotificationAlertController.this.wakeUpIfNeeded(notificationEntry);
                NotificationAlertController.this.markGroupSummaryChildrenUnShown(notificationEntry);
            }

            public void onPostEntryUpdated(NotificationEntry notificationEntry) {
                if (NotificationAlertController.DEBUG) {
                    Log.d("NotificationAlertController", "onPostEntryUpdated " + notificationEntry.getKey());
                }
                NotificationAlertController.this.buzzBeepBlink(notificationEntry.getKey());
                NotificationAlertController.this.wakeUpIfNeeded(notificationEntry);
            }
        });
        this.mStatusBarStateController.addCallback(new StatusBarStateController.StateListener() {
            public void onStateChanged(int i) {
                if (NotificationAlertController.this.mBarState == 1 && i == 0 && !NotificationAlertController.this.isShowingKeyguard()) {
                    NotificationAlertController.this.markVisibleNotificationsShown();
                }
                int unused = NotificationAlertController.this.mBarState = i;
            }
        });
    }

    /* access modifiers changed from: private */
    public void buzzBeepBlink(String str) {
        if (!this.mSettingsManager.getMiuiMirrorDndModeEnabled()) {
            this.mBgHandler.post(new Runnable(str) {
                public final /* synthetic */ String f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    NotificationAlertController.this.lambda$buzzBeepBlink$0$NotificationAlertController(this.f$1);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: invokeBuzzBeepBlink */
    public void lambda$buzzBeepBlink$0(String str) {
        try {
            this.mNm.getClass().getMethod("buzzBeepBlinkForNotification", new Class[]{String.class}).invoke(this.mNm, new Object[]{str});
        } catch (Exception e) {
            Log.e("NotificationAlertController", "beep " + str, e);
        }
    }

    /* access modifiers changed from: private */
    public boolean isShowingKeyguard() {
        return this.mStatusBarKeyguardManager.isShowing();
    }

    /* access modifiers changed from: private */
    public void markVisibleNotificationsShown() {
        this.mEntryManager.getVisibleNotifications().forEach($$Lambda$NotificationAlertController$cnPeo2J1MJMxlulxtwNq2qNkNE.INSTANCE);
    }

    /* access modifiers changed from: private */
    public void markGroupSummaryChildrenUnShown(NotificationEntry notificationEntry) {
        NotificationEntry groupSummary = this.mGroupManager.getGroupSummary((StatusBarNotification) notificationEntry.getSbn());
        if (groupSummary != null) {
            groupSummary.getSbn().setHasShownAfterUnlock(false);
            ArrayList<NotificationEntry> children = this.mGroupManager.getChildren(groupSummary.getSbn());
            if (children != null) {
                children.forEach($$Lambda$NotificationAlertController$d0Iv3YbZq03Jnp0MstxoxZC7XHw.INSTANCE);
            }
        }
    }

    /* access modifiers changed from: private */
    public void wakeUpIfNeeded(NotificationEntry notificationEntry) {
        if (this.mSettingsManager.getWakeupForNotification() && this.mNotificationLockscreenUserManager.shouldShowLockscreenNotifications() && this.mNotificationLockscreenUserManager.shouldShowOnKeyguard(notificationEntry) && !this.mZenModeController.isZenModeOn() && !NotificationUtil.isMediaNotification(notificationEntry.getSbn()) && !notificationEntry.getSbn().getNotification().hasMediaSession() && notificationEntry.getSbn().isClearable() && !NotificationUtil.hasProgressbar(notificationEntry.getSbn()) && !((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).isDeviceInteractive()) {
            if (DEBUG) {
                Log.d("NotificationAlertController", "wakeUpForNotification " + notificationEntry.getKey());
            }
            wakeUpForNotification(notificationEntry);
        }
    }

    private void wakeUpForNotification(NotificationEntry notificationEntry) {
        KeyguardSensorInjector keyguardSensorInjector = (KeyguardSensorInjector) Dependency.get(KeyguardSensorInjector.class);
        if (!keyguardSensorInjector.isProximitySensorDisabled()) {
            keyguardSensorInjector.registerProximitySensor(new KeyguardSensorInjector.ProximitySensorChangeCallback() {
                public final void onChange(boolean z) {
                    NotificationAlertController.this.lambda$wakeUpForNotification$3$NotificationAlertController(z);
                }
            });
        } else if (!MiuiKeyguardUtils.isNonUI()) {
            wakeUpForNotificationInternal();
        } else {
            Log.e("miui_keyguard", "not wake up for notification in nonui mode");
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$wakeUpForNotification$3 */
    public /* synthetic */ void lambda$wakeUpForNotification$3$NotificationAlertController(boolean z) {
        if (!z) {
            wakeUpForNotificationInternal();
        } else {
            Log.e("miui_keyguard", "not wake up for notification because in suspect mode");
        }
    }

    private void wakeUpForNotificationInternal() {
        ((PowerManager) this.mContext.getSystemService("power")).wakeUp(SystemClock.uptimeMillis(), "com.android.systemui:NOTIFICATION");
        ((MiuiFaceUnlockManager) Dependency.get(MiuiFaceUnlockManager.class)).setWakeupByNotification(true);
    }
}
