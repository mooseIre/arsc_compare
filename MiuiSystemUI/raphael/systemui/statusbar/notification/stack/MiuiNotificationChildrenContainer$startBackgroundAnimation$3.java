package com.android.systemui.statusbar.notification.stack;

import android.animation.Animator;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import com.android.systemui.statusbar.notification.row.NotificationBackgroundView;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: MiuiNotificationChildrenContainer.kt */
public final class MiuiNotificationChildrenContainer$startBackgroundAnimation$3 implements Animator.AnimatorListener {
    final /* synthetic */ NotificationBackgroundView $firstChildBackground;
    final /* synthetic */ NotificationBackgroundView $summaryBackground;
    final /* synthetic */ MiuiNotificationChildrenContainer this$0;

    public void onAnimationRepeat(@NotNull Animator animator) {
        Intrinsics.checkParameterIsNotNull(animator, "animator");
    }

    MiuiNotificationChildrenContainer$startBackgroundAnimation$3(MiuiNotificationChildrenContainer miuiNotificationChildrenContainer, NotificationBackgroundView notificationBackgroundView, NotificationBackgroundView notificationBackgroundView2) {
        this.this$0 = miuiNotificationChildrenContainer;
        this.$summaryBackground = notificationBackgroundView;
        this.$firstChildBackground = notificationBackgroundView2;
    }

    public void onAnimationStart(@NotNull Animator animator) {
        Intrinsics.checkParameterIsNotNull(animator, "animator");
        this.$summaryBackground.setVisibility(0);
        this.$summaryBackground.setAlpha(1.0f);
        this.$firstChildBackground.setVisibility(8);
    }

    public void onAnimationEnd(@NotNull Animator animator) {
        Intrinsics.checkParameterIsNotNull(animator, "animator");
        this.$summaryBackground.setVisibility(0);
        this.$summaryBackground.setAlpha(1.0f);
        NotificationBackgroundView notificationBackgroundView = this.$summaryBackground;
        ExpandableNotificationRow expandableNotificationRow = this.this$0.mContainingNotification;
        Intrinsics.checkExpressionValueIsNotNull(expandableNotificationRow, "mContainingNotification");
        notificationBackgroundView.setActualHeight(expandableNotificationRow.getActualHeight());
        this.this$0.setGroupBackgroundAnimating(false);
    }

    public void onAnimationCancel(@NotNull Animator animator) {
        Intrinsics.checkParameterIsNotNull(animator, "animator");
        this.this$0.setGroupBackgroundAnimating(false);
    }
}
