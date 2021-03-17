package com.android.systemui.media;

import android.content.Context;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import com.android.systemui.Gefingerpoken;
import com.android.systemui.util.animation.PhysicsAnimatorKt;
import kotlin.TypeCastException;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: MediaScrollView.kt */
public class MediaScrollView extends HorizontalScrollView {
    private float animationTargetX;
    @NotNull
    private ViewGroup contentContainer;
    @Nullable
    private Gefingerpoken touchListener;

    public MediaScrollView(@NotNull Context context) {
        this(context, null, 0, 6, null);
    }

    public MediaScrollView(@NotNull Context context, @Nullable AttributeSet attributeSet) {
        this(context, attributeSet, 0, 4, null);
    }

    /* JADX INFO: this call moved to the top of the method (can break code semantics) */
    public /* synthetic */ MediaScrollView(Context context, AttributeSet attributeSet, int i, int i2, DefaultConstructorMarker defaultConstructorMarker) {
        this(context, (i2 & 2) != 0 ? null : attributeSet, (i2 & 4) != 0 ? 0 : i);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public MediaScrollView(@NotNull Context context, @Nullable AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        Intrinsics.checkParameterIsNotNull(context, "context");
    }

    @NotNull
    public final ViewGroup getContentContainer() {
        ViewGroup viewGroup = this.contentContainer;
        if (viewGroup != null) {
            return viewGroup;
        }
        Intrinsics.throwUninitializedPropertyAccessException("contentContainer");
        throw null;
    }

    public final void setTouchListener(@Nullable Gefingerpoken gefingerpoken) {
        this.touchListener = gefingerpoken;
    }

    public final void setAnimationTargetX(float f) {
        this.animationTargetX = f;
    }

    public final float getContentTranslation() {
        ViewGroup viewGroup = this.contentContainer;
        if (viewGroup == null) {
            Intrinsics.throwUninitializedPropertyAccessException("contentContainer");
            throw null;
        } else if (PhysicsAnimatorKt.getPhysicsAnimator(viewGroup).isRunning()) {
            return this.animationTargetX;
        } else {
            ViewGroup viewGroup2 = this.contentContainer;
            if (viewGroup2 != null) {
                return viewGroup2.getTranslationX();
            }
            Intrinsics.throwUninitializedPropertyAccessException("contentContainer");
            throw null;
        }
    }

    private final int transformScrollX(int i) {
        if (!isLayoutRtl()) {
            return i;
        }
        ViewGroup viewGroup = this.contentContainer;
        if (viewGroup != null) {
            return (viewGroup.getWidth() - getWidth()) - i;
        }
        Intrinsics.throwUninitializedPropertyAccessException("contentContainer");
        throw null;
    }

    public final int getRelativeScrollX() {
        return transformScrollX(getScrollX());
    }

    public final void setRelativeScrollX(int i) {
        setScrollX(transformScrollX(i));
    }

    public void scrollTo(int i, int i2) {
        if (((HorizontalScrollView) this).mScrollX != i || ((HorizontalScrollView) this).mScrollY != i2) {
            int i3 = ((HorizontalScrollView) this).mScrollX;
            int i4 = ((HorizontalScrollView) this).mScrollY;
            ((HorizontalScrollView) this).mScrollX = i;
            ((HorizontalScrollView) this).mScrollY = i2;
            invalidateParentCaches();
            onScrollChanged(((HorizontalScrollView) this).mScrollX, ((HorizontalScrollView) this).mScrollY, i3, i4);
            if (!awakenScrollBars()) {
                postInvalidateOnAnimation();
            }
        }
    }

    public boolean onInterceptTouchEvent(@Nullable MotionEvent motionEvent) {
        Gefingerpoken gefingerpoken = this.touchListener;
        boolean onInterceptTouchEvent = gefingerpoken != null ? gefingerpoken.onInterceptTouchEvent(motionEvent) : false;
        if (super.onInterceptTouchEvent(motionEvent) || onInterceptTouchEvent) {
            return true;
        }
        return false;
    }

    public boolean onTouchEvent(@Nullable MotionEvent motionEvent) {
        Gefingerpoken gefingerpoken = this.touchListener;
        boolean onTouchEvent = gefingerpoken != null ? gefingerpoken.onTouchEvent(motionEvent) : false;
        if (super.onTouchEvent(motionEvent) || onTouchEvent) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        View childAt = getChildAt(0);
        if (childAt != null) {
            this.contentContainer = (ViewGroup) childAt;
            return;
        }
        throw new TypeCastException("null cannot be cast to non-null type android.view.ViewGroup");
    }

    /* access modifiers changed from: protected */
    public boolean overScrollBy(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, boolean z) {
        if (getContentTranslation() != 0.0f) {
            return false;
        }
        return super.overScrollBy(i, i2, i3, i4, i5, i6, i7, i8, z);
    }

    public final void cancelCurrentScroll() {
        long uptimeMillis = SystemClock.uptimeMillis();
        MotionEvent obtain = MotionEvent.obtain(uptimeMillis, uptimeMillis, 3, 0.0f, 0.0f, 0);
        Intrinsics.checkExpressionValueIsNotNull(obtain, "event");
        obtain.setSource(4098);
        super.onTouchEvent(obtain);
        obtain.recycle();
    }
}
