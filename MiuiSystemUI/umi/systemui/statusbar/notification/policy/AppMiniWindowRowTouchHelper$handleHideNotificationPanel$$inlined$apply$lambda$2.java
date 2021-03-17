package com.android.systemui.statusbar.notification.policy;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import com.android.systemui.statusbar.notification.ExpandedNotification;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.row.MiuiExpandableNotificationRow;
import org.jetbrains.annotations.Nullable;

/* compiled from: AppMiniWindowRowTouchHelper.kt */
public final class AppMiniWindowRowTouchHelper$handleHideNotificationPanel$$inlined$apply$lambda$2 extends AnimatorListenerAdapter {
    final /* synthetic */ AppMiniWindowRowTouchHelper this$0;

    AppMiniWindowRowTouchHelper$handleHideNotificationPanel$$inlined$apply$lambda$2(AppMiniWindowRowTouchHelper appMiniWindowRowTouchHelper) {
        this.this$0 = appMiniWindowRowTouchHelper;
    }

    public void onAnimationEnd(@Nullable Animator animator) {
        NotificationEntry entry;
        this.this$0.onMiniWindowReset();
        this.this$0.mTouchCallback.onMiniWindowAppLaunched();
        MiuiExpandableNotificationRow miuiExpandableNotificationRow = this.this$0.mPickedMiniWindowChild;
        ExpandedNotification sbn = (miuiExpandableNotificationRow == null || (entry = miuiExpandableNotificationRow.getEntry()) == null) ? null : entry.getSbn();
        if (sbn != null) {
            this.this$0.mNotificationEntryManager.performRemoveNotification(sbn, 1);
        }
    }
}
