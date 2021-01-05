package com.android.systemui.controls.ui;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import com.android.systemui.controls.ui.ToggleRangeBehavior;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: ToggleRangeBehavior.kt */
final class ToggleRangeBehavior$initialize$1 implements View.OnTouchListener {
    final /* synthetic */ GestureDetector $gestureDetector;
    final /* synthetic */ ToggleRangeBehavior.ToggleRangeGestureListener $gestureListener;
    final /* synthetic */ ToggleRangeBehavior this$0;

    ToggleRangeBehavior$initialize$1(ToggleRangeBehavior toggleRangeBehavior, GestureDetector gestureDetector, ToggleRangeBehavior.ToggleRangeGestureListener toggleRangeGestureListener) {
        this.this$0 = toggleRangeBehavior;
        this.$gestureDetector = gestureDetector;
        this.$gestureListener = toggleRangeGestureListener;
    }

    public final boolean onTouch(@NotNull View view, @NotNull MotionEvent motionEvent) {
        Intrinsics.checkParameterIsNotNull(view, "v");
        Intrinsics.checkParameterIsNotNull(motionEvent, "e");
        if (!this.$gestureDetector.onTouchEvent(motionEvent) && motionEvent.getAction() == 1 && this.$gestureListener.isDragging()) {
            view.getParent().requestDisallowInterceptTouchEvent(false);
            this.$gestureListener.setDragging(false);
            this.this$0.endUpdateRange();
        }
        return false;
    }
}
