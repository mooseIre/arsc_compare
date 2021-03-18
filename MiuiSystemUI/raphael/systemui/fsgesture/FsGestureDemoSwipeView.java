package com.android.systemui.fsgesture;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0017R$layout;

public class FsGestureDemoSwipeView extends FrameLayout {
    AnimatorSet finalAnimatorSet;
    ObjectAnimator hidingAnimator;
    private int mDisplayHeight;
    private int mDisplayWidth;
    private float mFinalTranslate;
    ObjectAnimator movingAnimator;
    ObjectAnimator scaleAnimator;
    ObjectAnimator showingAnimator;

    public FsGestureDemoSwipeView(Context context) {
        this(context, null);
    }

    public FsGestureDemoSwipeView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public FsGestureDemoSwipeView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public FsGestureDemoSwipeView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(C0017R$layout.fs_gesture_swipe_view, this);
        setAlpha(0.0f);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager) getContext().getSystemService("window")).getDefaultDisplay().getRealMetrics(displayMetrics);
        this.mDisplayWidth = displayMetrics.widthPixels;
        this.mDisplayHeight = displayMetrics.heightPixels;
        this.mFinalTranslate = getResources().getDimension(C0012R$dimen.fsgesture_swipe_final_translateX);
    }

    /* access modifiers changed from: package-private */
    public void prepare(int i) {
        setAlpha(0.0f);
        setVisibility(0);
        switch (i) {
            case 0:
                setTranslationY(getResources().getDimension(C0012R$dimen.fsgesture_swipe_translateY));
                setTranslationX((float) ((-getWidth()) / 2));
                return;
            case 1:
                setTranslationY(getResources().getDimension(C0012R$dimen.fsgesture_swipe_translateY));
                setTranslationX((float) (this.mDisplayWidth - (getWidth() / 2)));
                return;
            case 2:
            case 4:
            case 5:
            case 6:
                setTranslationX((float) ((this.mDisplayWidth / 2) - (getLeft() + (getWidth() / 2))));
                setTranslationY((float) (this.mDisplayHeight - (getHeight() / 2)));
                return;
            case 3:
                setTranslationY(getResources().getDimension(C0012R$dimen.fsgesture_swipe_drawer_translateY));
                setTranslationX((float) ((-getWidth()) / 2));
                return;
            default:
                return;
        }
    }

    /* access modifiers changed from: package-private */
    public void startAnimation(int i) {
        createShowingAnimator(i);
        createMovingAnimator(i);
        createScaleAnimator(i);
        createHidingAnimator(i);
        createFinalAnimSet(i);
        this.finalAnimatorSet.start();
    }

    private void createFinalAnimSet(final int i) {
        if (this.finalAnimatorSet == null) {
            AnimatorSet animatorSet = new AnimatorSet();
            this.finalAnimatorSet = animatorSet;
            if (i != 4) {
                animatorSet.playSequentially(this.showingAnimator, this.movingAnimator, this.hidingAnimator);
            } else {
                animatorSet.playSequentially(this.showingAnimator, this.movingAnimator, this.scaleAnimator, this.hidingAnimator);
            }
            this.finalAnimatorSet.addListener(new AnimatorListenerAdapter() {
                /* class com.android.systemui.fsgesture.FsGestureDemoSwipeView.AnonymousClass1 */

                public void onAnimationEnd(Animator animator) {
                    super.onAnimationEnd(animator);
                    switch (i) {
                        case 0:
                        case 3:
                            FsGestureDemoSwipeView.this.setTranslationX((float) ((-FsGestureDemoSwipeView.this.getWidth()) / 2));
                            break;
                        case 1:
                            FsGestureDemoSwipeView.this.setTranslationX((float) (FsGestureDemoSwipeView.this.mDisplayWidth - (FsGestureDemoSwipeView.this.getWidth() / 2)));
                            break;
                        case 2:
                        case 4:
                            FsGestureDemoSwipeView.this.setTranslationY((float) (FsGestureDemoSwipeView.this.mDisplayHeight - (FsGestureDemoSwipeView.this.getHeight() / 2)));
                            break;
                        case 5:
                        case 6:
                            FsGestureDemoSwipeView.this.setTranslationX((float) ((FsGestureDemoSwipeView.this.mDisplayWidth / 2) - (FsGestureDemoSwipeView.this.getLeft() + (FsGestureDemoSwipeView.this.getWidth() / 2))));
                            FsGestureDemoSwipeView.this.setTranslationY((float) (FsGestureDemoSwipeView.this.mDisplayHeight - (FsGestureDemoSwipeView.this.getHeight() / 2)));
                            break;
                    }
                    FsGestureDemoSwipeView.this.finalAnimatorSet.setStartDelay(1500);
                    FsGestureDemoSwipeView.this.finalAnimatorSet.start();
                }
            });
        }
    }

    private void createScaleAnimator(int i) {
        if (this.scaleAnimator == null) {
            ObjectAnimator ofPropertyValuesHolder = ObjectAnimator.ofPropertyValuesHolder(this, PropertyValuesHolder.ofFloat("scaleX", 1.0f, 1.2f), PropertyValuesHolder.ofFloat("scaleY", 1.0f, 1.2f));
            this.scaleAnimator = ofPropertyValuesHolder;
            ofPropertyValuesHolder.setDuration(1000L);
        }
    }

    private void createShowingAnimator(int i) {
        if (this.showingAnimator == null) {
            ObjectAnimator ofPropertyValuesHolder = ObjectAnimator.ofPropertyValuesHolder(this, PropertyValuesHolder.ofFloat("scaleX", 1.2f, 1.0f), PropertyValuesHolder.ofFloat("scaleY", 1.2f, 1.0f), PropertyValuesHolder.ofFloat("alpha", 0.0f, 1.0f));
            this.showingAnimator = ofPropertyValuesHolder;
            ofPropertyValuesHolder.setDuration(200L);
            this.showingAnimator.setStartDelay(300);
        }
    }

    private void createHidingAnimator(int i) {
        if (this.hidingAnimator == null) {
            if (i != 4) {
                ObjectAnimator ofFloat = ObjectAnimator.ofFloat(this, "alpha", 1.0f, 0.0f);
                this.hidingAnimator = ofFloat;
                ofFloat.setDuration(300L);
                return;
            }
            ObjectAnimator ofPropertyValuesHolder = ObjectAnimator.ofPropertyValuesHolder(this, PropertyValuesHolder.ofFloat("scaleX", 1.2f, 1.5f), PropertyValuesHolder.ofFloat("scaleY", 1.2f, 1.5f), PropertyValuesHolder.ofFloat("alpha", 1.0f, 0.0f));
            this.hidingAnimator = ofPropertyValuesHolder;
            ofPropertyValuesHolder.setDuration(100L);
        }
    }

    private void createMovingAnimator(int i) {
        if (this.movingAnimator == null) {
            switch (i) {
                case 0:
                case 3:
                    this.movingAnimator = ObjectAnimator.ofFloat(this, "translationX", (float) ((-getWidth()) / 2), this.mFinalTranslate - ((float) (getWidth() / 2)));
                    break;
                case 1:
                    this.movingAnimator = ObjectAnimator.ofFloat(this, "translationX", (float) (this.mDisplayWidth - (getWidth() / 2)), ((float) this.mDisplayWidth) - this.mFinalTranslate);
                    break;
                case 2:
                case 4:
                    this.movingAnimator = ObjectAnimator.ofFloat(this, "translationY", (float) (this.mDisplayHeight - (getHeight() / 2)), (float) (this.mDisplayHeight - 1000));
                    break;
                case 5:
                    float left = (float) ((this.mDisplayWidth / 2) - ((getLeft() + getWidth()) / 2));
                    this.movingAnimator = ObjectAnimator.ofFloat(this, "translationX", left, this.mFinalTranslate + left);
                    break;
                case 6:
                    int left2 = (this.mDisplayWidth / 2) - ((getLeft() + getWidth()) / 2);
                    Path path = new Path();
                    float f = (float) left2;
                    float height = (float) (this.mDisplayHeight - (getHeight() / 2));
                    path.moveTo(f, height);
                    path.lineTo(f, height - (this.mFinalTranslate / 2.0f));
                    float f2 = this.mFinalTranslate;
                    path.lineTo(f + f2, height - (f2 / 2.0f));
                    this.movingAnimator = ObjectAnimator.ofFloat(this, "translationX", "translationY", path);
                    break;
            }
            this.movingAnimator.setStartDelay(1000);
            if (i == 6) {
                this.movingAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
                this.movingAnimator.setDuration(1000L);
                return;
            }
            this.movingAnimator.setInterpolator(new DecelerateInterpolator(1.5f));
            this.movingAnimator.setDuration(500L);
        }
    }

    /* access modifiers changed from: package-private */
    public void cancelAnimation() {
        setVisibility(8);
        AnimatorSet animatorSet = this.finalAnimatorSet;
        if (animatorSet != null) {
            animatorSet.cancel();
            this.finalAnimatorSet.removeAllListeners();
            this.finalAnimatorSet = null;
        }
    }
}
