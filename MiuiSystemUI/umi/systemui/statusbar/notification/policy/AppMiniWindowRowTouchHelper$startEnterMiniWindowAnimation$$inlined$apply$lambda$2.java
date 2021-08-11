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
        MiuiExpandableNotificationRow miuiExpandableNotificationRow = this.this$0.mPickedMiniWindowChild;
        if (miuiExpandableNotificationRow != null) {
            miuiExpandableNotificationRow.setForceDisableBlur(false);
        }
    }

    public void onAnimationCancel(@Nullable Animator animator) {
        MiuiExpandableNotificationRow miuiExpandableNotificationRow = this.this$0.mPickedMiniWindowChild;
        if (miuiExpandableNotificationRow != null) {
            miuiExpandableNotificationRow.setForceDisableBlur(false);
        }
    }

    public void onAnimationStart(@Nullable Animator animator) {
        MiuiExpandableNotificationRow miuiExpandableNotificationRow = this.this$0.mPickedMiniWindowChild;
        if (miuiExpandableNotificationRow != null) {
            miuiExpandableNotificationRow.setForceDisableBlur(true);
        }
    }
}
