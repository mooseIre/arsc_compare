package com.android.systemui.dnd;

import android.app.Notification;
import android.app.NotificationCompat;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.UserHandle;
import android.widget.RemoteViews;
import com.android.systemui.Dependency;
import com.android.systemui.Util;
import com.android.systemui.keyguard.ScreenLifecycle;
import com.android.systemui.miui.statusbar.ExpandedNotification;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.notification.MiuiNotificationCompat;
import com.android.systemui.util.NotificationChannels;

public class DndNotificationWarnings {
    private final Context mContext;
    /* access modifiers changed from: private */
    public boolean mIsClearedAll;
    /* access modifiers changed from: private */
    public boolean mIsDNDEnabled;
    /* access modifiers changed from: private */
    public boolean mIsManuallyCleared;
    private final NotificationManager mNoMan;
    private final ScreenLifecycle.Observer mScreenObserver = new ScreenLifecycle.Observer() {
        public void onScreenTurningOff() {
        }

        public void onScreenTurningOn() {
        }

        public void onScreenTurnedOn() {
            boolean unused = DndNotificationWarnings.this.mIsClearedAll = false;
        }

        public void onScreenTurnedOff() {
            if (!DndNotificationWarnings.this.mIsManuallyCleared && DndNotificationWarnings.this.mIsClearedAll && DndNotificationWarnings.this.mIsDNDEnabled) {
                DndNotificationWarnings.this.handleDndStateChanged();
            }
            boolean unused = DndNotificationWarnings.this.mIsClearedAll = false;
        }
    };

    public DndNotificationWarnings(Context context) {
        this.mContext = context;
        this.mNoMan = (NotificationManager) context.getSystemService(NotificationManager.class);
        ((ScreenLifecycle) Dependency.get(ScreenLifecycle.class)).addObserver(this.mScreenObserver);
    }

    public void markClearNotification(ExpandedNotification expandedNotification) {
        if (expandedNotification.isSystemWarnings() && expandedNotification.getId() == R.string.dnd_notification_warnings_title) {
            this.mIsManuallyCleared = true;
        }
    }

    public void markClearAllNotifications() {
        this.mIsClearedAll = true;
    }

    public void setDNDEnabled(boolean z) {
        if (this.mIsDNDEnabled != z) {
            this.mIsDNDEnabled = z;
            handleDndStateChanged();
        }
    }

    /* access modifiers changed from: private */
    public void handleDndStateChanged() {
        if (this.mIsDNDEnabled) {
            showNotification();
        } else {
            cancelNotification();
        }
        this.mIsManuallyCleared = false;
    }

    private void showNotification() {
        Notification.Builder builder = new Notification.Builder(this.mContext);
        NotificationCompat.setChannelId(builder, NotificationChannels.DND);
        builder.setSmallIcon(17303603).setAutoCancel(false).setGroup(NotificationChannels.DND).setVisibility(1).setCustomContentView(new RemoteViews(this.mContext.getPackageName(), R.layout.dnd_notification)).setContentIntent(PendingIntent.getActivity(this.mContext, 0, Util.getSilentModeIntent(), 0));
        Notification build = builder.build();
        build.priority = 2;
        MiuiNotificationCompat.setTargetPkg(build, "android");
        MiuiNotificationCompat.setEnableKeyguard(build, true);
        MiuiNotificationCompat.setEnableFloat(build, false);
        MiuiNotificationCompat.setOnlyShowKeyguard(build, true);
        MiuiNotificationCompat.setCustomHeight(build, true);
        MiuiNotificationCompat.setSystemWarnings(build, true);
        this.mNoMan.notifyAsUser((String) null, R.string.dnd_notification_warnings_title, build, UserHandle.CURRENT);
    }

    private void cancelNotification() {
        this.mNoMan.cancelAsUser((String) null, R.string.dnd_notification_warnings_title, UserHandle.CURRENT);
    }
}
