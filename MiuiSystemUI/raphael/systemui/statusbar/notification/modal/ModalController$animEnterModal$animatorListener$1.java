package com.android.systemui.statusbar.notification.modal;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.notification.analytics.NotificationStat;
import com.android.systemui.statusbar.notification.modal.ModalController;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: ModalController.kt */
public final class ModalController$animEnterModal$animatorListener$1 extends AnimatorListenerAdapter {
    final /* synthetic */ ExpandableNotificationRow $row;
    final /* synthetic */ ModalController this$0;

    ModalController$animEnterModal$animatorListener$1(ModalController modalController, ExpandableNotificationRow expandableNotificationRow) {
        this.this$0 = modalController;
        this.$row = expandableNotificationRow;
    }

    public void onAnimationEnd(@NotNull Animator animator) {
        Intrinsics.checkParameterIsNotNull(animator, "animation");
        ((NotificationStat) Dependency.get(NotificationStat.class)).onOpenMenu(this.$row.getEntry());
        this.this$0.isAnimating = false;
        for (ModalController.OnModalChangeListener onModalChangeListener : this.this$0.onModalChangeListeners) {
            onModalChangeListener.onChange(true);
        }
    }
}
