package com.android.systemui.statusbar.notification.interruption;

import android.content.ContentResolver;
import android.content.Context;
import android.hardware.display.AmbientDisplayConfiguration;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.service.dreams.IDreamManager;
import android.util.Log;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.notification.ExpandedNotification;
import com.android.systemui.statusbar.notification.InCallUtils;
import com.android.systemui.statusbar.notification.MiuiNotificationCompat;
import com.android.systemui.statusbar.notification.NotificationFilter;
import com.android.systemui.statusbar.notification.NotificationSettingsManager;
import com.android.systemui.statusbar.notification.NotificationUtil;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.phone.StatusBarKeyguardViewManager;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.statusbar.policy.DeviceProvisionedController;
import com.android.systemui.statusbar.policy.HeadsUpManager;
import com.android.systemui.statusbar.policy.ZenModeController;
import com.miui.systemui.BuildConfig;
import com.miui.systemui.SettingsManager;
import com.miui.systemui.util.CommonUtil;

public class MiuiNotificationInterruptStateProviderImpl extends NotificationInterruptStateProviderImpl implements CommandQueue.Callbacks {
    private final Context mContext;
    private final DeviceProvisionedController mDeviceProvisionedController;
    private boolean mDisableFloatNotification;
    private boolean mIsStatusBarHidden;
    private final SettingsManager mSettingsManager;
    private boolean mSoftInputVisible;
    private final StatusBarKeyguardViewManager mStatusBarKeyguardViewManager;
    private final StatusBarStateController mStatusBarStateController;
    private final ZenModeController mZenModeController;

    public MiuiNotificationInterruptStateProviderImpl(Context context, ContentResolver contentResolver, PowerManager powerManager, IDreamManager iDreamManager, AmbientDisplayConfiguration ambientDisplayConfiguration, NotificationFilter notificationFilter, BatteryController batteryController, StatusBarStateController statusBarStateController, HeadsUpManager headsUpManager, Handler handler, ZenModeController zenModeController, SettingsManager settingsManager, CommandQueue commandQueue, StatusBarKeyguardViewManager statusBarKeyguardViewManager, DeviceProvisionedController deviceProvisionedController) {
        super(contentResolver, powerManager, iDreamManager, ambientDisplayConfiguration, notificationFilter, batteryController, statusBarStateController, headsUpManager, handler, context);
        this.mContext = context;
        this.mStatusBarStateController = statusBarStateController;
        this.mZenModeController = zenModeController;
        this.mSettingsManager = settingsManager;
        commandQueue.addCallback((CommandQueue.Callbacks) this);
        this.mStatusBarKeyguardViewManager = statusBarKeyguardViewManager;
        this.mDeviceProvisionedController = deviceProvisionedController;
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void disable(int i, int i2, int i3, boolean z) {
        boolean z2 = true;
        this.mDisableFloatNotification = (i2 & 1024) != 0;
        if ((i2 & 256) == 0) {
            z2 = false;
        }
        this.mIsStatusBarHidden = z2;
    }

    @Override // com.android.systemui.statusbar.notification.interruption.NotificationInterruptStateProviderImpl, com.android.systemui.statusbar.notification.interruption.NotificationInterruptStateProvider
    public boolean shouldBubbleUp(NotificationEntry notificationEntry) {
        return super.shouldBubbleUp(notificationEntry);
    }

    @Override // com.android.systemui.statusbar.notification.interruption.NotificationInterruptStateProviderImpl, com.android.systemui.statusbar.notification.interruption.NotificationInterruptStateProvider
    public boolean shouldHeadsUp(NotificationEntry notificationEntry) {
        if (super.shouldHeadsUp(notificationEntry)) {
            return shouldPeek(notificationEntry);
        }
        return false;
    }

    public boolean shouldPeek(NotificationEntry notificationEntry) {
        return shouldPeek(notificationEntry, notificationEntry.getSbn());
    }

    private boolean shouldPeek(NotificationEntry notificationEntry, ExpandedNotification expandedNotification) {
        boolean z = false;
        if (!this.mDeviceProvisionedController.isDeviceProvisioned() || this.mStatusBarKeyguardViewManager.isShowing() || ((InCallUtils.isInCallNotificationHeadsUp(this.mHeadsUpManager.getTopEntry()) && !InCallUtils.isInCallNotification(expandedNotification)) || (InCallUtils.isInCallScreenShowing() && !InCallUtils.isInCallNotificationHasVideoCall(expandedNotification)))) {
            Log.d("InterruptionStateProvider", "no peek: miui smart intercept: " + expandedNotification.getKey());
            return false;
        } else if (expandedNotification.getNotification().fullScreenIntent != null) {
            if (NotificationUtil.isInCallNotification(expandedNotification)) {
                if (!this.mStatusBarKeyguardViewManager.isShowing() && !((StatusBar) Dependency.get(StatusBar.class)).isPanelExpanded()) {
                    z = true;
                }
                Log.d("InterruptionStateProvider", "in call notification should peek: " + z);
                return z;
            } else if (BuildConfig.IS_INTERNATIONAL) {
                if (!this.mStatusBarKeyguardViewManager.isShowing() || this.mStatusBarKeyguardViewManager.isOccluded()) {
                    return true;
                }
                return false;
            } else if (this.mIsStatusBarHidden || this.mSoftInputVisible || this.mDisableFloatNotification || this.mZenModeController.isZenModeOn() || ((NotificationSettingsManager) Dependency.get(NotificationSettingsManager.class)).shouldPeekWhenAppShowing(CommonUtil.getTopActivityPkg(this.mContext, true))) {
                if (!expandedNotification.isClearable()) {
                    MiuiNotificationCompat.setFloatTime(expandedNotification.getNotification(), Integer.MAX_VALUE);
                }
                Log.d("InterruptionStateProvider", "peek: miui smart suspension: " + expandedNotification.getKey());
                return true;
            } else {
                Log.d("InterruptionStateProvider", "no peek: has fullscreen intent: " + expandedNotification.getKey());
                return false;
            }
        } else if (this.mDisableFloatNotification) {
            Log.d("InterruptionStateProvider", "no peek disable float notification " + notificationEntry.getKey());
            return false;
        } else if (this.mZenModeController.isZenModeOn() && !expandedNotification.isFloatWhenDnd()) {
            Log.d("InterruptionStateProvider", "no peek in dnd mode " + notificationEntry.getKey());
            return false;
        } else if (this.mSettingsManager.getMiuiMirrorDndModeEnabled()) {
            Log.d("InterruptionStateProvider", "no peek in mirror dnd mode " + notificationEntry.getKey());
            return false;
        } else if (!expandedNotification.canFloat()) {
            Log.d("InterruptionStateProvider", "no peek require miui permission " + notificationEntry.getKey());
            return false;
        } else if (NotificationUtil.hasProgressbar(expandedNotification)) {
            Log.d("InterruptionStateProvider", "no peek has progress " + notificationEntry.getKey());
            return false;
        } else if (!((StatusBar) Dependency.get(StatusBar.class)).isPanelExpanded()) {
            return true;
        } else {
            Log.d("InterruptionStateProvider", "No peeking: status bar panel expanded  " + expandedNotification.getKey());
            return false;
        }
    }

    @Override // com.android.systemui.statusbar.notification.interruption.NotificationInterruptStateProviderImpl, com.android.systemui.statusbar.notification.interruption.NotificationInterruptStateProvider
    public boolean shouldLaunchFullScreenIntentWhenAdded(NotificationEntry notificationEntry) {
        if (notificationEntry.getSbn().getNotification().fullScreenIntent == null) {
            return false;
        }
        if (this.mStatusBarStateController.getState() == 1) {
            return true;
        }
        if (!shouldHeadsUp(notificationEntry)) {
            return shouldSend(notificationEntry);
        }
        return false;
    }

    private boolean shouldSend(NotificationEntry notificationEntry) {
        if (this.mZenModeController.isZenModeOn()) {
            Log.d("InterruptionStateProvider", "no send suppressed by DND " + notificationEntry.getKey());
            return false;
        } else if (!this.mSettingsManager.getMiuiMirrorDndModeEnabled()) {
            return true;
        } else {
            Log.d("InterruptionStateProvider", "no send suppressed by Mirror DND " + notificationEntry.getKey());
            return false;
        }
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void setImeWindowStatus(int i, IBinder iBinder, int i2, int i3, boolean z) {
        this.mSoftInputVisible = (i2 & 2) != 0;
    }
}
