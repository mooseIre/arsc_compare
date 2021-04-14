package com.android.systemui.statusbar.notification;

import android.app.Notification;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.view.View;
import com.android.systemui.DejankUtils;
import com.android.systemui.Dependency;
import com.android.systemui.bubbles.BubbleController;
import com.android.systemui.statusbar.notification.analytics.NotificationStat;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import com.android.systemui.statusbar.notification.row.NotificationClickerInjector;
import com.android.systemui.statusbar.phone.StatusBar;
import java.util.Optional;
import java.util.function.Consumer;

public final class NotificationClicker implements View.OnClickListener {
    private final BubbleController mBubbleController;
    private final NotificationClickerLogger mLogger;
    private final NotificationActivityStarter mNotificationActivityStarter;
    private final Optional<StatusBar> mStatusBar;

    private NotificationClicker(BubbleController bubbleController, NotificationClickerLogger notificationClickerLogger, Optional<StatusBar> optional, NotificationActivityStarter notificationActivityStarter) {
        this.mBubbleController = bubbleController;
        this.mLogger = notificationClickerLogger;
        this.mStatusBar = optional;
        this.mNotificationActivityStarter = notificationActivityStarter;
    }

    public void onClick(View view) {
        if (!(view instanceof ExpandableNotificationRow)) {
            Log.e("NotificationClicker", "NotificationClicker called on a view that is not a notification row.");
            return;
        }
        this.mStatusBar.ifPresent(new Consumer(view) {
            /* class com.android.systemui.statusbar.notification.$$Lambda$NotificationClicker$yfbp07NzZ_fwFSXR0B2xHX63zDM */
            public final /* synthetic */ View f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                NotificationClicker.lambda$onClick$0(this.f$0, (StatusBar) obj);
            }
        });
        ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) view;
        NotificationEntry entry = expandableNotificationRow.getEntry();
        this.mLogger.logOnClick(entry);
        if (isMenuVisible(expandableNotificationRow)) {
            this.mLogger.logMenuVisible(entry);
            expandableNotificationRow.animateTranslateNotification(0.0f);
        } else if (expandableNotificationRow.isChildInGroup() && isMenuVisible(expandableNotificationRow.getNotificationParent())) {
            this.mLogger.logParentMenuVisible(entry);
            expandableNotificationRow.getNotificationParent().animateTranslateNotification(0.0f);
        } else if (expandableNotificationRow.isSummaryWithChildren() && expandableNotificationRow.areChildrenExpanded()) {
            this.mLogger.logChildrenExpanded(entry);
        } else if (expandableNotificationRow.areGutsExposed()) {
            this.mLogger.logGutsExposed(entry);
        } else if (!NotificationClickerInjector.INSTANCE.onClick(expandableNotificationRow)) {
            expandableNotificationRow.setJustClicked(true);
            DejankUtils.postAfterTraversal(new Runnable() {
                /* class com.android.systemui.statusbar.notification.$$Lambda$NotificationClicker$CH899qDTz3sQ4q5eBHDPoFomAGA */

                public final void run() {
                    NotificationClicker.lambda$onClick$1(ExpandableNotificationRow.this);
                }
            });
            if (!expandableNotificationRow.getEntry().isBubble()) {
                this.mBubbleController.collapseStack();
            }
            this.mNotificationActivityStarter.onNotificationClicked(entry.getSbn(), expandableNotificationRow);
            ((NotificationStat) Dependency.get(NotificationStat.class)).onClick(entry);
        }
    }

    private boolean isMenuVisible(ExpandableNotificationRow expandableNotificationRow) {
        return expandableNotificationRow.getProvider() != null && expandableNotificationRow.getProvider().isMenuVisible();
    }

    public void register(ExpandableNotificationRow expandableNotificationRow, StatusBarNotification statusBarNotification) {
        Notification notification = statusBarNotification.getNotification();
        if (notification.contentIntent != null || notification.fullScreenIntent != null || expandableNotificationRow.getEntry().isBubble() || statusBarNotification.isGroup()) {
            expandableNotificationRow.setOnClickListener(this);
        } else {
            expandableNotificationRow.setOnClickListener(null);
        }
    }

    public static class Builder {
        private final BubbleController mBubbleController;
        private final NotificationClickerLogger mLogger;

        public Builder(BubbleController bubbleController, NotificationClickerLogger notificationClickerLogger) {
            this.mBubbleController = bubbleController;
            this.mLogger = notificationClickerLogger;
        }

        public NotificationClicker build(Optional<StatusBar> optional, NotificationActivityStarter notificationActivityStarter) {
            return new NotificationClicker(this.mBubbleController, this.mLogger, optional, notificationActivityStarter);
        }
    }
}
