package com.android.systemui.statusbar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import com.android.systemui.Interpolators;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.notification.TransformState;
import java.util.Stack;

public class ViewTransformationHelper implements TransformableView {
    private ArrayMap<Integer, CustomTransformation> mCustomTransformations = new ArrayMap<>();
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

    public void reset() {
        this.mTransformedViews.clear();
    }

    public void setCustomTransformation(CustomTransformation customTransformation, int i) {
        this.mCustomTransformations.put(Integer.valueOf(i), customTransformation);
    }

    public TransformState getCurrentState(int i) {
        View view = this.mTransformedViews.get(Integer.valueOf(i));
        if (view == null || view.getVisibility() == 8) {
            return null;
        }
        return TransformState.createFrom(view);
    }

    public void transformTo(final TransformableView transformableView, final Runnable runnable) {
        ValueAnimator valueAnimator = this.mViewTransformationAnimation;
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
        this.mViewTransformationAnimation = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
        this.mViewTransformationAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                ViewTransformationHelper.this.transformTo(transformableView, valueAnimator.getAnimatedFraction());
            }
        });
        this.mViewTransformationAnimation.setInterpolator(Interpolators.LINEAR);
        this.mViewTransformationAnimation.setDuration(360);
        this.mViewTransformationAnimation.addListener(new AnimatorListenerAdapter() {
            public boolean mCancelled;

            public void onAnimationEnd(Animator animator) {
                if (!this.mCancelled) {
                    Runnable runnable = runnable;
                    if (runnable != null) {
                        runnable.run();
                    }
                    ViewTransformationHelper.this.setVisible(false);
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

    public void transformTo(TransformableView transformableView, float f) {
        for (Integer next : this.mTransformedViews.keySet()) {
            TransformState currentState = getCurrentState(next.intValue());
            if (currentState != null) {
                CustomTransformation customTransformation = this.mCustomTransformations.get(next);
                if (customTransformation == null || !customTransformation.transformTo(currentState, transformableView, f)) {
                    TransformState currentState2 = transformableView.getCurrentState(next.intValue());
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

    public void transformFrom(final TransformableView transformableView) {
        ValueAnimator valueAnimator = this.mViewTransformationAnimation;
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
        this.mViewTransformationAnimation = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
        this.mViewTransformationAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                ViewTransformationHelper.this.transformFrom(transformableView, valueAnimator.getAnimatedFraction());
            }
        });
        this.mViewTransformationAnimation.addListener(new AnimatorListenerAdapter() {
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
        this.mViewTransformationAnimation.setDuration(360);
        this.mViewTransformationAnimation.start();
    }

    public void transformFrom(TransformableView transformableView, float f) {
        for (Integer next : this.mTransformedViews.keySet()) {
            TransformState currentState = getCurrentState(next.intValue());
            if (currentState != null) {
                CustomTransformation customTransformation = this.mCustomTransformations.get(next);
                if (customTransformation == null || !customTransformation.transformFrom(currentState, transformableView, f)) {
                    TransformState currentState2 = transformableView.getCurrentState(next.intValue());
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

    public void setVisible(boolean z) {
        ValueAnimator valueAnimator = this.mViewTransformationAnimation;
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
        for (Integer intValue : this.mTransformedViews.keySet()) {
            TransformState currentState = getCurrentState(intValue.intValue());
            if (currentState != null) {
                currentState.setVisible(z, false);
                currentState.recycle();
            }
        }
    }

    /* access modifiers changed from: private */
    public void abortTransformations() {
        for (Integer intValue : this.mTransformedViews.keySet()) {
            TransformState currentState = getCurrentState(intValue.intValue());
            if (currentState != null) {
                currentState.abortTransformation();
                currentState.recycle();
            }
        }
    }

    public void addRemainingTransformTypes(View view) {
        int id;
        int size = this.mTransformedViews.size();
        for (int i = 0; i < size; i++) {
            View valueAt = this.mTransformedViews.valueAt(i);
            while (valueAt != null && valueAt != view.getParent()) {
                valueAt.setTag(R.id.contains_transformed_view, true);
                valueAt = (View) valueAt.getParent();
            }
        }
        Stack stack = new Stack();
        stack.push(view);
        while (!stack.isEmpty()) {
            View view2 = (View) stack.pop();
            if (((Boolean) view2.getTag(R.id.contains_transformed_view)) != null || (id = view2.getId()) == -1) {
                view2.setTag(R.id.contains_transformed_view, (Object) null);
                if ((view2 instanceof ViewGroup) && !this.mTransformedViews.containsValue(view2)) {
                    ViewGroup viewGroup = (ViewGroup) view2;
                    for (int i2 = 0; i2 < viewGroup.getChildCount(); i2++) {
                        stack.push(viewGroup.getChildAt(i2));
                    }
                }
            } else {
                addTransformedView(id, view2);
            }
        }
    }

    public void resetTransformedView(View view) {
        TransformState createFrom = TransformState.createFrom(view);
        createFrom.setVisible(true, true);
        createFrom.recycle();
    }

    public ArraySet<View> getAllTransformingViews() {
        return new ArraySet<>(this.mTransformedViews.values());
    }
}
