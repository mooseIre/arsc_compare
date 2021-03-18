package com.android.systemui.statusbar.notification.row;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.drawable.GradientDrawable;
import android.widget.ImageView;
import org.jetbrains.annotations.Nullable;

/* compiled from: PriorityOnboardingDialogController.kt */
public final class PriorityOnboardingDialogController$initDialog$1$3 extends AnimatorListenerAdapter {
    final /* synthetic */ int $baseSize;
    final /* synthetic */ GradientDrawable $bg;
    final /* synthetic */ int $bgSize;
    final /* synthetic */ ImageView $conversationIconBadgeBg;

    PriorityOnboardingDialogController$initDialog$1$3(GradientDrawable gradientDrawable, int i, ImageView imageView, int i2) {
        this.$bg = gradientDrawable;
        this.$baseSize = i;
        this.$conversationIconBadgeBg = imageView;
        this.$bgSize = i2;
    }

    public void onAnimationStart(@Nullable Animator animator) {
        GradientDrawable gradientDrawable = this.$bg;
        int i = this.$baseSize;
        gradientDrawable.setSize(i, i);
        this.$conversationIconBadgeBg.invalidate();
    }

    public void onAnimationEnd(@Nullable Animator animator) {
        GradientDrawable gradientDrawable = this.$bg;
        int i = this.$bgSize;
        gradientDrawable.setSize(i, i);
        this.$conversationIconBadgeBg.invalidate();
    }
}
