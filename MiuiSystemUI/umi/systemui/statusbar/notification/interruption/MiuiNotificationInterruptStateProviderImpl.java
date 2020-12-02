package com.android.systemui.statusbar.notification.interruption;

import android.content.ContentResolver;
import android.hardware.display.AmbientDisplayConfiguration;
import android.os.Handler;
import android.os.PowerManager;
import android.service.dreams.IDreamManager;
import android.util.Log;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.notification.ExpandedNotification;
import com.android.systemui.statusbar.notification.NotificationFilter;
import com.android.systemui.statusbar.notification.NotificationUtil;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.phone.StatusBarKeyguardViewManager;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.statusbar.policy.HeadsUpManager;
import com.android.systemui.statusbar.policy.ZenModeController;
import com.miui.systemui.SettingsManager;

public class MiuiNotificationInterruptStateProviderImpl extends NotificationInterruptStateProviderImpl implements CommandQueue.Callbacks {
    private boolean mDisableFloatNotification;
    private final SettingsManager mSettingsManager;
    private final StatusBarKeyguardViewManager mStatusBarKeyguardViewManager;
    private final StatusBarStateController mStatusBarStateController;
    private final ZenModeController mZenModeController;

    public MiuiNotificationInterruptStateProviderImpl(ContentResolver contentResolver, PowerManager powerManager, IDreamManager iDreamManager, AmbientDisplayConfiguration ambientDisplayConfiguration, NotificationFilter notificationFilter, BatteryController batteryController, StatusBarStateController statusBarStateController, HeadsUpManager headsUpManager, Handler handler, ZenModeController zenModeController, SettingsManager settingsManager, CommandQueue commandQueue, StatusBarKeyguardViewManager statusBarKeyguardViewManager) {
        super(contentResolver, powerManager, iDreamManager, ambientDisplayConfiguration, notificationFilter, batteryController, statusBarStateController, headsUpManager, handler);
        this.mStatusBarStateController = statusBarStateController;
        this.mZenModeController = zenModeController;
        this.mSettingsManager = settingsManager;
        commandQueue.addCallback((CommandQueue.Callbacks) this);
        this.mStatusBarKeyguardViewManager = statusBarKeyguardViewManager;
    }

    public void disable(int i, int i2, int i3, boolean z) {
        this.mDisableFloatNotification = (i2 & 1024) != 0;
    }

    public boolean shouldBubbleUp(NotificationEntry notificationEntry) {
        return super.shouldBubbleUp(notificationEntry);
    }

    public boolean shouldHeadsUp(NotificationEntry notificationEntry) {
        if (super.shouldHeadsUp(notificationEntry)) {
            return shouldPeek(notificationEntry);
        }
        return false;
    }

    public boolean shouldPeek(NotificationEntry notificationEntry) {
        ExpandedNotification sbn = notificationEntry.getSbn();
        if (sbn.getNotification().fullScreenIntent != null && NotificationUtil.isInCallNotification(sbn)) {
            boolean z = !this.mStatusBarKeyguardViewManager.isShowing();
            Log.d("InterruptionStateProvider", "in call notification should peek: " + z);
            return z;
        } else if (this.mDisableFloatNotification) {
            Log.d("InterruptionStateProvider", "no peek disable float notification " + notificationEntry.getKey());
            return false;
        } else if (this.mZenModeController.isZenModeOn() && !notificationEntry.getSbn().isFloatWhenDnd()) {
            Log.d("InterruptionStateProvider", "no peek in dnd mode " + notificationEntry.getKey());
            return false;
        } else if (this.mSettingsManager.getMiuiMirrorDndModeEnabled()) {
            Log.d("InterruptionStateProvider", "no peek in mirror dnd mode " + notificationEntry.getKey());
            return false;
        } else if (!notificationEntry.getSbn().canFloat()) {
            Log.d("InterruptionStateProvider", "no peek require miui permission " + notificationEntry.getKey());
            return false;
        } else if (!NotificationUtil.hasProgressbar(notificationEntry.getSbn())) {
            return true;
        } else {
            Log.d("InterruptionStateProvider", "no peek has progress " + notificationEntry.getKey());
            return false;
        }
    }

    public boolean shouldLaunchFullScreenIntentWhenAdded(NotificationEntry notificationEntry) {
        if (notificationEntry.getSbn().getNotification().fullScreenIntent == null) {
            return false;
        }
        if (this.mStatusBarStateController.getState() == 1) {
            return true;
        }
        if (!super.shouldHeadsUp(notificationEntry)) {
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
}
