package com.android.systemui.statusbar.phone;

import android.animation.AnimatorListenerAdapter;

/* compiled from: MiuiHeadsUpTouchHelper.kt */
public final class MiuiHeadsUpTouchHelper$handleHideNotificationPanel$$inlined$apply$lambda$2 extends AnimatorListenerAdapter {
    final /* synthetic */ MiuiHeadsUpTouchHelper this$0;

    MiuiHeadsUpTouchHelper$handleHideNotificationPanel$$inlined$apply$lambda$2(MiuiHeadsUpTouchHelper miuiHeadsUpTouchHelper) {
        this.this$0 = miuiHeadsUpTouchHelper;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:2:0x0008, code lost:
        r3 = r3.getEntry();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onAnimationEnd(@org.jetbrains.annotations.Nullable android.animation.Animator r3) {
        /*
            r2 = this;
            com.android.systemui.statusbar.phone.MiuiHeadsUpTouchHelper r3 = r2.this$0
            com.android.systemui.statusbar.notification.row.MiuiExpandableNotificationRow r3 = r3.mPickedMiniWindowChild
            if (r3 == 0) goto L_0x0013
            com.android.systemui.statusbar.notification.collection.NotificationEntry r3 = r3.getEntry()
            if (r3 == 0) goto L_0x0013
            com.android.systemui.statusbar.notification.ExpandedNotification r3 = r3.getSbn()
            goto L_0x0014
        L_0x0013:
            r3 = 0
        L_0x0014:
            com.android.systemui.statusbar.phone.MiuiHeadsUpTouchHelper r0 = r2.this$0
            r0.onMiniWindowReset()
            com.android.systemui.statusbar.phone.MiuiHeadsUpTouchHelper r0 = r2.this$0
            com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout r0 = r0.mStackScrollLayout
            r1 = 0
            r0.setHeadsUpGoingAwayAnimationsAllowed(r1)
            com.android.systemui.statusbar.phone.MiuiHeadsUpTouchHelper r0 = r2.this$0
            com.android.systemui.statusbar.phone.HeadsUpManagerPhone r0 = r0.mHeadsUpManager
            r0.releaseAllImmediately()
            com.android.systemui.statusbar.phone.MiuiHeadsUpTouchHelper r0 = r2.this$0
            com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout r0 = r0.mStackScrollLayout
            r1 = 1
            r0.setHeadsUpGoingAwayAnimationsAllowed(r1)
            if (r3 == 0) goto L_0x0041
            com.android.systemui.statusbar.phone.MiuiHeadsUpTouchHelper r2 = r2.this$0
            com.android.systemui.statusbar.notification.NotificationEntryManager r2 = r2.mNotificationEntryManager
            r2.performRemoveNotification(r3, r1)
        L_0x0041:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.phone.MiuiHeadsUpTouchHelper$handleHideNotificationPanel$$inlined$apply$lambda$2.onAnimationEnd(android.animation.Animator):void");
    }
}
