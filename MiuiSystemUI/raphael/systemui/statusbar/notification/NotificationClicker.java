package com.android.systemui.statusbar.notification;

import android.app.Notification;
import android.app.PendingIntent;
import android.os.SystemClock;
import android.service.notification.StatusBarNotification;
import android.service.notification.StatusBarNotificationCompat;
import android.view.View;
import com.android.systemui.DejankUtils;
import com.android.systemui.Dependency;
import com.android.systemui.Logger;
import com.android.systemui.bubbles.BubbleController;
import com.android.systemui.miui.statusbar.ExpandedNotification;
import com.android.systemui.miui.statusbar.analytics.NotificationStat;
import com.android.systemui.statusbar.ExpandableNotificationRow;
import com.android.systemui.statusbar.phone.ShadeController;
import com.android.systemui.statusbar.policy.HeadsUpManager;

public class NotificationClicker implements View.OnClickListener {
    private final BubbleController mBubbleController;
    private final HeadsUpManager mHeadsUpManager;
    private final NotificationActivityStarter mNotificationActivityStarter;
    private final ShadeController mShadeController;

    public NotificationClicker(ShadeController shadeController, BubbleController bubbleController, NotificationActivityStarter notificationActivityStarter, HeadsUpManager headsUpManager) {
        this.mShadeController = shadeController;
        this.mBubbleController = bubbleController;
        this.mNotificationActivityStarter = notificationActivityStarter;
        this.mHeadsUpManager = headsUpManager;
    }

    public void onClick(View view) {
        if (!(view instanceof ExpandableNotificationRow)) {
            Logger.fullW("NotificationClicker", "NotificationClicker called on a view that is not a notification row.");
            return;
        }
        this.mShadeController.wakeUpIfDozing(SystemClock.uptimeMillis(), view, "NOTIFICATION_CLICK");
        final ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) view;
        ExpandedNotification statusBarNotification = expandableNotificationRow.getStatusBarNotification();
        if (statusBarNotification == null) {
            Logger.fullW("NotificationClicker", "NotificationClicker called on an unclickable notification,");
        } else if (expandableNotificationRow.getMenu() == null || !expandableNotificationRow.getMenu().isMenuVisible()) {
            Notification notification = statusBarNotification.getNotification();
            PendingIntent pendingIntent = notification.contentIntent;
            if (pendingIntent == null) {
                pendingIntent = notification.fullScreenIntent;
            }
            String key = statusBarNotification.getKey();
            HeadsUpManager headsUpManager = this.mHeadsUpManager;
            ((NotificationStat) Dependency.get(NotificationStat.class)).onClick(statusBarNotification, headsUpManager != null && headsUpManager.isHeadsUp(key), this.mShadeController.isKeyguardShowing(), this.mShadeController.indexOfEntry(expandableNotificationRow.getEntry()));
            if (expandableNotificationRow.isSummaryWithChildren() && !expandableNotificationRow.isGroupExpanded() && (StatusBarNotificationCompat.isAutoGroupSummary(statusBarNotification) || pendingIntent == null)) {
                expandableNotificationRow.getExpandClickListener().onClick(expandableNotificationRow);
            } else if (pendingIntent == null) {
                Logger.fullI("NotificationClicker", "click notification, no intent, key=" + statusBarNotification.getKey());
            } else {
                expandableNotificationRow.setJustClicked(true);
                DejankUtils.postAfterTraversal(new Runnable() {
                    public void run() {
                        expandableNotificationRow.setJustClicked(false);
                    }
                });
                if (!expandableNotificationRow.getEntry().isBubble()) {
                    this.mBubbleController.collapseStack();
                }
                this.mNotificationActivityStarter.onNotificationClicked(statusBarNotification, expandableNotificationRow);
            }
        } else {
            expandableNotificationRow.animateTranslateNotification(0.0f);
        }
    }

    public void register(ExpandableNotificationRow expandableNotificationRow, StatusBarNotification statusBarNotification) {
        expandableNotificationRow.setOnClickListener(this);
    }
}
