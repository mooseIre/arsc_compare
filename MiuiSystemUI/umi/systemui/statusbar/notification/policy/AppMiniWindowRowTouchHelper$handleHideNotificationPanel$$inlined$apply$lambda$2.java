package com.android.systemui.statusbar.notification.policy;

import android.animation.AnimatorListenerAdapter;

/* compiled from: AppMiniWindowRowTouchHelper.kt */
public final class AppMiniWindowRowTouchHelper$handleHideNotificationPanel$$inlined$apply$lambda$2 extends AnimatorListenerAdapter {
    final /* synthetic */ AppMiniWindowRowTouchHelper this$0;

    AppMiniWindowRowTouchHelper$handleHideNotificationPanel$$inlined$apply$lambda$2(AppMiniWindowRowTouchHelper appMiniWindowRowTouchHelper) {
        this.this$0 = appMiniWindowRowTouchHelper;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:2:0x0016, code lost:
        r2 = r2.getEntry();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onAnimationEnd(@org.jetbrains.annotations.Nullable android.animation.Animator r2) {
        /*
            r1 = this;
            com.android.systemui.statusbar.notification.policy.AppMiniWindowRowTouchHelper r2 = r1.this$0
            r2.onMiniWindowReset()
            com.android.systemui.statusbar.notification.policy.AppMiniWindowRowTouchHelper r2 = r1.this$0
            com.android.systemui.statusbar.notification.policy.AppMiniWindowRowTouchCallback r2 = r2.mTouchCallback
            r2.onMiniWindowAppLaunched()
            com.android.systemui.statusbar.notification.policy.AppMiniWindowRowTouchHelper r2 = r1.this$0
            com.android.systemui.statusbar.notification.row.MiuiExpandableNotificationRow r2 = r2.mPickedMiniWindowChild
            if (r2 == 0) goto L_0x0021
            com.android.systemui.statusbar.notification.collection.NotificationEntry r2 = r2.getEntry()
            if (r2 == 0) goto L_0x0021
            com.android.systemui.statusbar.notification.ExpandedNotification r2 = r2.getSbn()
            goto L_0x0022
        L_0x0021:
            r2 = 0
        L_0x0022:
            if (r2 == 0) goto L_0x002e
            com.android.systemui.statusbar.notification.policy.AppMiniWindowRowTouchHelper r1 = r1.this$0
            com.android.systemui.statusbar.notification.NotificationEntryManager r1 = r1.mNotificationEntryManager
            r0 = 1
            r1.performRemoveNotification(r2, r0)
        L_0x002e:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.notification.policy.AppMiniWindowRowTouchHelper$handleHideNotificationPanel$$inlined$apply$lambda$2.onAnimationEnd(android.animation.Animator):void");
    }
}
