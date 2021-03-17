package com.android.systemui.media;

import android.animation.ValueAnimator;
import android.graphics.Rect;

/* access modifiers changed from: package-private */
/* compiled from: MediaHierarchyManager.kt */
public final class MediaHierarchyManager$$special$$inlined$apply$lambda$1 implements ValueAnimator.AnimatorUpdateListener {
    final /* synthetic */ ValueAnimator $this_apply;
    final /* synthetic */ MediaHierarchyManager this$0;

    MediaHierarchyManager$$special$$inlined$apply$lambda$1(ValueAnimator valueAnimator, MediaHierarchyManager mediaHierarchyManager) {
        this.$this_apply = valueAnimator;
        this.this$0 = mediaHierarchyManager;
    }

    public final void onAnimationUpdate(ValueAnimator valueAnimator) {
        this.this$0.updateTargetState();
        MediaHierarchyManager mediaHierarchyManager = this.this$0;
        Rect unused = mediaHierarchyManager.interpolateBounds(mediaHierarchyManager.animationStartBounds, this.this$0.targetBounds, this.$this_apply.getAnimatedFraction(), this.this$0.currentBounds);
        MediaHierarchyManager mediaHierarchyManager2 = this.this$0;
        MediaHierarchyManager.applyState$default(mediaHierarchyManager2, mediaHierarchyManager2.currentBounds, false, 2, null);
    }
}
