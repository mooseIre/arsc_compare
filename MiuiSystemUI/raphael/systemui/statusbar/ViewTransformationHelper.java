package com.android.systemui.statusbar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import com.android.systemui.C0015R$id;
import com.android.systemui.Interpolators;
import com.android.systemui.statusbar.notification.TransformState;
import java.util.Stack;

public class ViewTransformationHelper implements TransformableView, TransformState.TransformInfo {
    private static final int TAG_CONTAINS_TRANSFORMED_VIEW = C0015R$id.contains_transformed_view;
    private ArrayMap<Integer, CustomTransformation> mCustomTransformations = new ArrayMap<>();
    private ArraySet<Integer> mKeysTransformingToSimilar = new ArraySet<>();
    private ArrayMap<Integer, View> mTransformedViews = new ArrayMap<>();
    private ValueAnimator mViewTransformationAnimation;

    public static abstract class CustomTransformation {
        public boolean customTransformTarget(TransformState transformState, TransformState transformState2) {
            return false;
        }

        public Interpolator getCustomInterpolator(int i, boolean z) {
            return null;
        }

        public boolean initTransformation(TransformState transformState, TransformState transformState2) {
            return false;
        }

        public abstract boolean transformFrom(TransformState transformState, TransformableView transformableView, float f);

        public abstract boolean transformTo(TransformState transformState, TransformableView transformableView, float f);
    }

    public void addTransformedView(int i, View view) {
        this.mTransformedViews.put(Integer.valueOf(i), view);
    }

    public void addTransformedView(View view) {
        int id = view.getId();
        if (id != -1) {
            addTransformedView(id, view);
            return;
        }
        throw new IllegalArgumentException("View argument does not have a valid id");
    }

    public void addViewTransformingToSimilar(int i, View view) {
        addTransformedView(i, view);
        this.mKeysTransformingToSimilar.add(Integer.valueOf(i));
    }

    public void addViewTransformingToSimilar(View view) {
        int id = view.getId();
        if (id != -1) {
            addViewTransformingToSimilar(id, view);
            return;
        }
        throw new IllegalArgumentException("View argument does not have a valid id");
    }

    public void reset() {
        this.mTransformedViews.clear();
        this.mKeysTransformingToSimilar.clear();
    }

    public void setCustomTransformation(CustomTransformation customTransformation, int i) {
        this.mCustomTransformations.put(Integer.valueOf(i), customTransformation);
    }

    @Override // com.android.systemui.statusbar.TransformableView
    public TransformState getCurrentState(int i) {
        View view = this.mTransformedViews.get(Integer.valueOf(i));
        if (view == null || view.getVisibility() == 8) {
            return null;
        }
        TransformState createFrom = TransformState.createFrom(view, this);
        if (this.mKeysTransformingToSimilar.contains(Integer.valueOf(i))) {
            createFrom.setIsSameAsAnyView(true);
        }
        return createFrom;
    }

    @Override // com.android.systemui.statusbar.TransformableView
    public void transformTo(final TransformableView transformableView, final Runnable runnable) {
        ValueAnimator valueAnimator = this.mViewTransformationAnimation;
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
        ValueAnimator ofFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
        this.mViewTransformationAnimation = ofFloat;
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.android.systemui.statusbar.ViewTransformationHelper.AnonymousClass1 */

            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                ViewTransformationHelper.this.transformTo(transformableView, valueAnimator.getAnimatedFraction());
            }
        });
        this.mViewTransformationAnimation.setInterpolator(Interpolators.LINEAR);
        this.mViewTransformationAnimation.setDuration(300L);
        this.mViewTransformationAnimation.addListener(new AnimatorListenerAdapter() {
            /* class com.android.systemui.statusbar.ViewTransformationHelper.AnonymousClass2 */
            public boolean mCancelled;

            public void onAnimationEnd(Animator animator) {
                if (!this.mCancelled) {
                    Runnable runnable = runnable;
                    if (runnable != null) {
                        runnable.run();
                    }
                    ViewTransformationHelper.this.setVisible(false);
                    ViewTransformationHelper.this.mViewTransformationAnimation = null;
                    return;
                }
                ViewTransformationHelper.this.abortTransformations();
            }

            public void onAnimationCancel(Animator animator) {
                this.mCancelled = true;
            }
        });
        this.mViewTransformationAnimation.start();
    }

    @Override // com.android.systemui.statusbar.TransformableView
    public void transformTo(TransformableView transformableView, float f) {
        for (Integer num : this.mTransformedViews.keySet()) {
            TransformState currentState = getCurrentState(num.intValue());
            if (currentState != null) {
                CustomTransformation customTransformation = this.mCustomTransformations.get(num);
                if (customTransformation == null || !customTransformation.transformTo(currentState, transformableView, f)) {
                    TransformState currentState2 = transformableView.getCurrentState(num.intValue());
                    if (currentState2 != null) {
                        currentState.transformViewTo(currentState2, f);
                        currentState2.recycle();
                    } else {
                        currentState.disappear(f, transformableView);
                    }
                    currentState.recycle();
                } else {
                    currentState.recycle();
                }
            }
        }
    }

    @Override // com.android.systemui.statusbar.TransformableView
    public void transformFrom(final TransformableView transformableView) {
        ValueAnimator valueAnimator = this.mViewTransformationAnimation;
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
        ValueAnimator ofFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
        this.mViewTransformationAnimation = ofFloat;
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.android.systemui.statusbar.ViewTransformationHelper.AnonymousClass3 */

            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                ViewTransformationHelper.this.transformFrom(transformableView, valueAnimator.getAnimatedFraction());
            }
        });
        this.mViewTransformationAnimation.addListener(new AnimatorListenerAdapter() {
            /* class com.android.systemui.statusbar.ViewTransformationHelper.AnonymousClass4 */
            public boolean mCancelled;

            public void onAnimationEnd(Animator animator) {
                if (!this.mCancelled) {
                    ViewTransformationHelper.this.setVisible(true);
                } else {
                    ViewTransformationHelper.this.abortTransformations();
                }
            }

            public void onAnimationCancel(Animator animator) {
                this.mCancelled = true;
            }
        });
        this.mViewTransformationAnimation.setInterpolator(Interpolators.LINEAR);
        this.mViewTransformationAnimation.setDuration(300L);
        this.mViewTransformationAnimation.start();
    }

    @Override // com.android.systemui.statusbar.TransformableView
    public void transformFrom(TransformableView transformableView, float f) {
        for (Integer num : this.mTransformedViews.keySet()) {
            TransformState currentState = getCurrentState(num.intValue());
            if (currentState != null) {
                CustomTransformation customTransformation = this.mCustomTransformations.get(num);
                if (customTransformation == null || !customTransformation.transformFrom(currentState, transformableView, f)) {
                    TransformState currentState2 = transformableView.getCurrentState(num.intValue());
                    if (currentState2 != null) {
                        currentState.transformViewFrom(currentState2, f);
                        currentState2.recycle();
                    } else {
                        currentState.appear(f, transformableView);
                    }
                    currentState.recycle();
                } else {
                    currentState.recycle();
                }
            }
        }
    }

    @Override // com.android.systemui.statusbar.TransformableView
    public void setVisible(boolean z) {
        ValueAnimator valueAnimator = this.mViewTransformationAnimation;
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
        for (Integer num : this.mTransformedViews.keySet()) {
            TransformState currentState = getCurrentState(num.intValue());
            if (currentState != null) {
                currentState.setVisible(z, false);
                currentState.recycle();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void abortTransformations() {
        for (Integer num : this.mTransformedViews.keySet()) {
            TransformState currentState = getCurrentState(num.intValue());
            if (currentState != null) {
                currentState.abortTransformation();
                currentState.recycle();
            }
        }
    }

    public void addRemainingTransformTypes(View view) {
        int id;
        int i = TAG_CONTAINS_TRANSFORMED_VIEW;
        int size = this.mTransformedViews.size();
        for (int i2 = 0; i2 < size; i2++) {
            Object valueAt = this.mTransformedViews.valueAt(i2);
            while (true) {
                View view2 = (View) valueAt;
                if (view2 == view.getParent()) {
                    break;
                }
                view2.setTag(i, Boolean.TRUE);
                valueAt = view2.getParent();
            }
        }
        Stack stack = new Stack();
        stack.push(view);
        while (!stack.isEmpty()) {
            View view3 = (View) stack.pop();
            if (((Boolean) view3.getTag(i)) != null || (id = view3.getId()) == -1) {
                view3.setTag(i, null);
                if ((view3 instanceof ViewGroup) && !this.mTransformedViews.containsValue(view3)) {
                    ViewGroup viewGroup = (ViewGroup) view3;
                    for (int i3 = 0; i3 < viewGroup.getChildCount(); i3++) {
                        stack.push(viewGroup.getChildAt(i3));
                    }
                }
            } else {
                addTransformedView(id, view3);
            }
        }
    }

    public void resetTransformedView(View view) {
        TransformState createFrom = TransformState.createFrom(view, this);
        createFrom.setVisible(true, true);
        createFrom.recycle();
    }

    public ArraySet<View> getAllTransformingViews() {
        return new ArraySet<>(this.mTransformedViews.values());
    }

    @Override // com.android.systemui.statusbar.notification.TransformState.TransformInfo
    public boolean isAnimating() {
        ValueAnimator valueAnimator = this.mViewTransformationAnimation;
        return valueAnimator != null && valueAnimator.isRunning();
    }
}
