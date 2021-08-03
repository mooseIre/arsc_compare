package com.android.systemui.statusbar.notification.policy;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.Rect;
import com.android.systemui.statusbar.notification.row.MiuiExpandableNotificationRow;
import org.jetbrains.annotations.Nullable;

/* compiled from: AppMiniWindowRowTouchHelper.kt */
public final class AppMiniWindowRowTouchHelper$startEnterMiniWindowAnimation$$inlined$apply$lambda$2 extends AnimatorListenerAdapter {
    final /* synthetic */ AppMiniWindowRowTouchHelper this$0;

    AppMiniWindowRowTouchHelper$startEnterMiniWindowAnimation$$inlined$apply$lambda$2(AppMiniWindowRowTouchHelper appMiniWindowRowTouchHelper, Rect rect, Rect rect2) {
        this.this$0 = appMiniWindowRowTouchHelper;
    }

    public void onAnimationEnd(@Nullable Animator animator) {
        MiuiExpandableNotificationRow access$getMPickedMiniWindowChild$p = AppMiniWindowRowTouchHelper.access$getMPickedMiniWindowChild$p(this.this$0);
        if (access$getMPickedMiniWindowChild$p != null) {
            access$getMPickedMiniWindowChild$p.setForceDisableBlur(false);
        }
    }

    public void onAnimationCancel(@Nullable Animator animator) {
        MiuiExpandableNotificationRow access$getMPickedMiniWindowChild$p = AppMiniWindowRowTouchHelper.access$getMPickedMiniWindowChild$p(this.this$0);
        if (access$getMPickedMiniWindowChild$p != null) {
            access$getMPickedMiniWindowChild$p.setForceDisableBlur(false);
        }
    }

    public void onAnimationStart(@Nullable Animator animator) {
        MiuiExpandableNotificationRow access$getMPickedMiniWindowChild$p = AppMiniWindowRowTouchHelper.access$getMPickedMiniWindowChild$p(this.this$0);
        if (access$getMPickedMiniWindowChild$p != null) {
            access$getMPickedMiniWindowChild$p.setForceDisableBlur(true);
        }
    }
}
