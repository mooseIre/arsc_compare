package com.android.systemui.util.animation;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import com.android.systemui.statusbar.CrossFadeHelper;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: TransitionLayout.kt */
public final class TransitionLayout extends ConstraintLayout {
    private final Rect boundsRect;
    private TransitionViewState currentState;
    private int desiredMeasureHeight;
    private int desiredMeasureWidth;
    private boolean measureAsConstraint;
    private final Set<Integer> originalGoneChildrenSet;
    private final Map<Integer, Float> originalViewAlphas;
    private final TransitionLayout$preDrawApplicator$1 preDrawApplicator;
    private boolean updateScheduled;

    public TransitionLayout(@NotNull Context context) {
        this(context, null, 0, 6, null);
    }

    public TransitionLayout(@NotNull Context context, @Nullable AttributeSet attributeSet) {
        this(context, attributeSet, 0, 4, null);
    }

    /* JADX INFO: this call moved to the top of the method (can break code semantics) */
    public /* synthetic */ TransitionLayout(Context context, AttributeSet attributeSet, int i, int i2, DefaultConstructorMarker defaultConstructorMarker) {
        this(context, (i2 & 2) != 0 ? null : attributeSet, (i2 & 4) != 0 ? 0 : i);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public TransitionLayout(@NotNull Context context, @Nullable AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        Intrinsics.checkParameterIsNotNull(context, "context");
        this.boundsRect = new Rect();
        this.originalGoneChildrenSet = new LinkedHashSet();
        this.originalViewAlphas = new LinkedHashMap();
        this.currentState = new TransitionViewState();
        new TransitionViewState();
        this.preDrawApplicator = new TransitionLayout$preDrawApplicator$1(this);
    }

    public final void setMeasureState(@NotNull TransitionViewState transitionViewState) {
        Intrinsics.checkParameterIsNotNull(transitionViewState, "value");
        int width = transitionViewState.getWidth();
        int height = transitionViewState.getHeight();
        if (width != this.desiredMeasureWidth || height != this.desiredMeasureHeight) {
            this.desiredMeasureWidth = width;
            this.desiredMeasureHeight = height;
            if (isInLayout()) {
                forceLayout();
            } else {
                requestLayout();
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = getChildAt(i);
            Intrinsics.checkExpressionValueIsNotNull(childAt, "child");
            if (childAt.getId() == -1) {
                childAt.setId(i);
            }
            if (childAt.getVisibility() == 8) {
                this.originalGoneChildrenSet.add(Integer.valueOf(childAt.getId()));
            }
            this.originalViewAlphas.put(Integer.valueOf(childAt.getId()), Float.valueOf(childAt.getAlpha()));
        }
    }

    /* access modifiers changed from: private */
    public final void applyCurrentState() {
        int childCount = getChildCount();
        int i = (int) this.currentState.getContentTranslation().x;
        int i2 = (int) this.currentState.getContentTranslation().y;
        for (int i3 = 0; i3 < childCount; i3++) {
            View childAt = getChildAt(i3);
            Map<Integer, WidgetState> widgetStates = this.currentState.getWidgetStates();
            Intrinsics.checkExpressionValueIsNotNull(childAt, "child");
            WidgetState widgetState = widgetStates.get(Integer.valueOf(childAt.getId()));
            if (widgetState != null) {
                Integer valueOf = (!(childAt instanceof TextView) || widgetState.getWidth() >= widgetState.getMeasureWidth()) ? null : Integer.valueOf(((TextView) childAt).getLayout().getParagraphDirection(0) == -1 ? widgetState.getMeasureWidth() - widgetState.getWidth() : 0);
                if (!(childAt.getMeasuredWidth() == widgetState.getMeasureWidth() && childAt.getMeasuredHeight() == widgetState.getMeasureHeight())) {
                    childAt.measure(View.MeasureSpec.makeMeasureSpec(widgetState.getMeasureWidth(), 1073741824), View.MeasureSpec.makeMeasureSpec(widgetState.getMeasureHeight(), 1073741824));
                    childAt.layout(0, 0, childAt.getMeasuredWidth(), childAt.getMeasuredHeight());
                }
                int intValue = valueOf != null ? valueOf.intValue() : 0;
                int x = (((int) widgetState.getX()) + i) - intValue;
                int y = ((int) widgetState.getY()) + i2;
                boolean z = valueOf != null;
                childAt.setLeftTopRightBottom(x, y, (z ? widgetState.getMeasureWidth() : widgetState.getWidth()) + x, (z ? widgetState.getMeasureHeight() : widgetState.getHeight()) + y);
                childAt.setScaleX(widgetState.getScale());
                childAt.setScaleY(widgetState.getScale());
                Rect clipBounds = childAt.getClipBounds();
                if (clipBounds == null) {
                    clipBounds = new Rect();
                }
                clipBounds.set(intValue, 0, widgetState.getWidth() + intValue, widgetState.getHeight());
                childAt.setClipBounds(clipBounds);
                CrossFadeHelper.fadeIn(childAt, widgetState.getAlpha());
                childAt.setVisibility((widgetState.getGone() || widgetState.getAlpha() == 0.0f) ? 4 : 0);
            }
        }
        updateBounds();
        setTranslationX(this.currentState.getTranslation().x);
        setTranslationY(this.currentState.getTranslation().y);
        CrossFadeHelper.fadeIn(this, this.currentState.getAlpha());
    }

    private final void applyCurrentStateOnPredraw() {
        if (!this.updateScheduled) {
            this.updateScheduled = true;
            getViewTreeObserver().addOnPreDrawListener(this.preDrawApplicator);
        }
    }

    /* access modifiers changed from: protected */
    @Override // androidx.constraintlayout.widget.ConstraintLayout
    public void onMeasure(int i, int i2) {
        if (this.measureAsConstraint) {
            super.onMeasure(i, i2);
            return;
        }
        int childCount = getChildCount();
        for (int i3 = 0; i3 < childCount; i3++) {
            View childAt = getChildAt(i3);
            Map<Integer, WidgetState> widgetStates = this.currentState.getWidgetStates();
            Intrinsics.checkExpressionValueIsNotNull(childAt, "child");
            WidgetState widgetState = widgetStates.get(Integer.valueOf(childAt.getId()));
            if (widgetState != null) {
                childAt.measure(View.MeasureSpec.makeMeasureSpec(widgetState.getMeasureWidth(), 1073741824), View.MeasureSpec.makeMeasureSpec(widgetState.getMeasureHeight(), 1073741824));
            }
        }
        setMeasuredDimension(this.desiredMeasureWidth, this.desiredMeasureHeight);
    }

    /* access modifiers changed from: protected */
    @Override // androidx.constraintlayout.widget.ConstraintLayout
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        if (this.measureAsConstraint) {
            super.onLayout(z, getLeft(), getTop(), getRight(), getBottom());
            return;
        }
        int childCount = getChildCount();
        for (int i5 = 0; i5 < childCount; i5++) {
            View childAt = getChildAt(i5);
            Intrinsics.checkExpressionValueIsNotNull(childAt, "child");
            childAt.layout(0, 0, childAt.getMeasuredWidth(), childAt.getMeasuredHeight());
        }
        applyCurrentState();
    }

    /* access modifiers changed from: protected */
    @Override // androidx.constraintlayout.widget.ConstraintLayout
    public void dispatchDraw(@Nullable Canvas canvas) {
        if (canvas != null) {
            canvas.save();
        }
        if (canvas != null) {
            canvas.clipRect(this.boundsRect);
        }
        super.dispatchDraw(canvas);
        if (canvas != null) {
            canvas.restore();
        }
    }

    private final void updateBounds() {
        int left = getLeft();
        int top = getTop();
        setLeftTopRightBottom(left, top, this.currentState.getWidth() + left, this.currentState.getHeight() + top);
        this.boundsRect.set(0, 0, getWidth(), getHeight());
    }

    @NotNull
    public final TransitionViewState calculateViewState(@NotNull MeasurementInput measurementInput, @NotNull ConstraintSet constraintSet, @Nullable TransitionViewState transitionViewState) {
        Intrinsics.checkParameterIsNotNull(measurementInput, "input");
        Intrinsics.checkParameterIsNotNull(constraintSet, "constraintSet");
        if (transitionViewState == null) {
            transitionViewState = new TransitionViewState();
        }
        applySetToFullLayout(constraintSet);
        int measuredHeight = getMeasuredHeight();
        int measuredWidth = getMeasuredWidth();
        this.measureAsConstraint = true;
        measure(measurementInput.getWidthMeasureSpec(), measurementInput.getHeightMeasureSpec());
        int left = getLeft();
        int top = getTop();
        layout(left, top, getMeasuredWidth() + left, getMeasuredHeight() + top);
        this.measureAsConstraint = false;
        transitionViewState.initFromLayout(this);
        ensureViewsNotGone();
        setMeasuredDimension(measuredWidth, measuredHeight);
        applyCurrentStateOnPredraw();
        return transitionViewState;
    }

    private final void applySetToFullLayout(ConstraintSet constraintSet) {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = getChildAt(i);
            Set<Integer> set = this.originalGoneChildrenSet;
            Intrinsics.checkExpressionValueIsNotNull(childAt, "child");
            if (set.contains(Integer.valueOf(childAt.getId()))) {
                childAt.setVisibility(8);
            }
            Float f = this.originalViewAlphas.get(Integer.valueOf(childAt.getId()));
            childAt.setAlpha(f != null ? f.floatValue() : 1.0f);
        }
        constraintSet.applyTo(this);
    }

    private final void ensureViewsNotGone() {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = getChildAt(i);
            Map<Integer, WidgetState> widgetStates = this.currentState.getWidgetStates();
            Intrinsics.checkExpressionValueIsNotNull(childAt, "child");
            WidgetState widgetState = widgetStates.get(Integer.valueOf(childAt.getId()));
            childAt.setVisibility((widgetState == null || widgetState.getGone()) ? 4 : 0);
        }
    }

    public final void setState(@NotNull TransitionViewState transitionViewState) {
        Intrinsics.checkParameterIsNotNull(transitionViewState, "state");
        this.currentState = transitionViewState;
        applyCurrentState();
    }
}
