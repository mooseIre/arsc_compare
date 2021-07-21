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
        AppMiniWindowRowTouchHelper.access$onMiniWindowReset(this.this$0);
        AppMiniWindowRowTouchHelper.access$getMTouchCallback$p(this.this$0).onMiniWindowAppLaunched();
        MiuiExpandableNotificationRow access$getMPickedMiniWindowChild$p = AppMiniWindowRowTouchHelper.access$getMPickedMiniWindowChild$p(this.this$0);
        ExpandedNotification sbn = (access$getMPickedMiniWindowChild$p == null || (entry = access$getMPickedMiniWindowChild$p.getEntry()) == null) ? null : entry.getSbn();
        if (sbn != null) {
            AppMiniWindowRowTouchHelper.access$getMNotificationEntryManager$p(this.this$0).performRemoveNotification(sbn, 1);
        }
    }
}
