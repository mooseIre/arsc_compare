package com.android.systemui.media;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.View;
import org.jetbrains.annotations.Nullable;

/* compiled from: MediaHierarchyManager.kt */
public final class MediaHierarchyManager$$special$$inlined$apply$lambda$2 extends AnimatorListenerAdapter {
    private boolean cancelled;
    final /* synthetic */ MediaHierarchyManager this$0;

    MediaHierarchyManager$$special$$inlined$apply$lambda$2(MediaHierarchyManager mediaHierarchyManager) {
        this.this$0 = mediaHierarchyManager;
    }

    public void onAnimationCancel(@Nullable Animator animator) {
        this.cancelled = true;
        this.this$0.animationPending = false;
        View view = this.this$0.rootView;
        if (view != null) {
            view.removeCallbacks(this.this$0.startAnimation);
        }
    }

    public void onAnimationEnd(@Nullable Animator animator) {
        if (!this.cancelled) {
            this.this$0.applyTargetStateIfNotAnimating();
        }
    }

    public void onAnimationStart(@Nullable Animator animator) {
        this.cancelled = false;
        this.this$0.animationPending = false;
    }
}
