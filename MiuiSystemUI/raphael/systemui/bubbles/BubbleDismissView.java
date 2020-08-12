package com.android.systemui.bubbles;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;
import com.android.systemui.plugins.R;

public class BubbleDismissView extends FrameLayout {
    private View mDismissCircle;
    private View mDismissGradient = new FrameLayout(this.mContext);
    private ImageView mDismissIcon;
    private LinearLayout mDismissTarget;
    private SpringAnimation mDismissTargetAlphaSpring;
    private SpringAnimation mDismissTargetVerticalSpring;
    private TextView mDismissText;

    public BubbleDismissView(Context context) {
        super(context);
        setVisibility(8);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(-1, -1);
        layoutParams.gravity = 80;
        this.mDismissGradient.setLayoutParams(layoutParams);
        Drawable drawable = this.mContext.getResources().getDrawable(R.drawable.pip_dismiss_scrim);
        drawable.setAlpha(216);
        this.mDismissGradient.setBackground(drawable);
        this.mDismissGradient.setVisibility(8);
        addView(this.mDismissGradient);
        LayoutInflater.from(context).inflate(R.layout.bubble_dismiss_target, this, true);
        this.mDismissTarget = (LinearLayout) findViewById(R.id.bubble_dismiss_icon_container);
        this.mDismissIcon = (ImageView) findViewById(R.id.bubble_dismiss_close_icon);
        this.mDismissText = (TextView) findViewById(R.id.bubble_dismiss_text);
        this.mDismissCircle = findViewById(R.id.bubble_dismiss_circle);
        AccelerateDecelerateInterpolator accelerateDecelerateInterpolator = new AccelerateDecelerateInterpolator();
        this.mDismissGradient.animate().setDuration(150).setInterpolator(accelerateDecelerateInterpolator);
        this.mDismissText.animate().setDuration(150).setInterpolator(accelerateDecelerateInterpolator);
        this.mDismissIcon.animate().setDuration(150).setInterpolator(accelerateDecelerateInterpolator);
        this.mDismissCircle.animate().setDuration(75).setInterpolator(accelerateDecelerateInterpolator);
        SpringAnimation springAnimation = new SpringAnimation(this.mDismissTarget, DynamicAnimation.ALPHA);
        SpringForce springForce = new SpringForce();
        springForce.setStiffness(200.0f);
        springForce.setDampingRatio(0.75f);
        springAnimation.setSpring(springForce);
        this.mDismissTargetAlphaSpring = springAnimation;
        SpringAnimation springAnimation2 = new SpringAnimation(this.mDismissTarget, DynamicAnimation.TRANSLATION_Y);
        SpringForce springForce2 = new SpringForce();
        springForce2.setStiffness(1500.0f);
        springForce2.setDampingRatio(0.75f);
        springAnimation2.setSpring(springForce2);
        this.mDismissTargetVerticalSpring = springAnimation2;
        this.mDismissTargetAlphaSpring.addEndListener(new DynamicAnimation.OnAnimationEndListener() {
            public final void onAnimationEnd(DynamicAnimation dynamicAnimation, boolean z, float f, float f2) {
                BubbleDismissView.this.lambda$new$0$BubbleDismissView(dynamicAnimation, z, f, f2);
            }
        });
    }

    public /* synthetic */ void lambda$new$0$BubbleDismissView(DynamicAnimation dynamicAnimation, boolean z, float f, float f2) {
        if (f < 0.5f) {
            setVisibility(8);
        }
    }

    /* access modifiers changed from: package-private */
    public void springIn() {
        setVisibility(0);
        this.mDismissTarget.setAlpha(0.0f);
        this.mDismissTargetAlphaSpring.animateToFinalPosition(1.0f);
        LinearLayout linearLayout = this.mDismissTarget;
        linearLayout.setTranslationY(((float) linearLayout.getHeight()) / 2.0f);
        this.mDismissTargetVerticalSpring.animateToFinalPosition(0.0f);
        this.mDismissGradient.setVisibility(0);
        this.mDismissGradient.animate().alpha(1.0f);
        this.mDismissIcon.setAlpha(1.0f);
        this.mDismissIcon.setScaleX(1.0f);
        this.mDismissIcon.setScaleY(1.0f);
        this.mDismissIcon.setTranslationX(0.0f);
        this.mDismissText.setAlpha(1.0f);
        this.mDismissText.setTranslationX(0.0f);
    }

    /* access modifiers changed from: package-private */
    public void springOut() {
        this.mDismissTargetAlphaSpring.animateToFinalPosition(0.0f);
        this.mDismissTargetVerticalSpring.animateToFinalPosition(((float) this.mDismissTarget.getHeight()) / 2.0f);
        this.mDismissGradient.animate().alpha(0.0f).withEndAction(new Runnable() {
            public final void run() {
                BubbleDismissView.this.lambda$springOut$1$BubbleDismissView();
            }
        });
        this.mDismissCircle.animate().alpha(0.0f).scaleX(1.2f).scaleY(1.2f);
    }

    public /* synthetic */ void lambda$springOut$1$BubbleDismissView() {
        this.mDismissGradient.setVisibility(8);
    }

    /* access modifiers changed from: package-private */
    public void animateEncircleCenterWithX(boolean z) {
        float f = 0.0f;
        float f2 = z ? ((float) (-this.mDismissIcon.getWidth())) / 4.0f : 0.0f;
        float width = z ? ((((float) this.mDismissTarget.getWidth()) / 2.0f) - (((float) this.mDismissIcon.getWidth()) / 2.0f)) - ((float) this.mDismissIcon.getLeft()) : 0.0f;
        this.mDismissText.animate().alpha(z ? 0.0f : 1.0f).translationX(f2);
        this.mDismissIcon.animate().setDuration(150).translationX(width);
        this.mDismissGradient.animate().alpha(z ? 0.0f : 1.0f);
        if (z) {
            this.mDismissCircle.setAlpha(0.0f);
            this.mDismissCircle.setScaleX(1.2f);
            this.mDismissCircle.setScaleY(1.2f);
        }
        ViewPropertyAnimator scaleX = this.mDismissCircle.animate().alpha(z ? 1.0f : 0.0f).scaleX(z ? 1.0f : 0.0f);
        if (z) {
            f = 1.0f;
        }
        scaleX.scaleY(f);
    }

    /* access modifiers changed from: package-private */
    public void animateEncirclingCircleDisappearance() {
        this.mDismissIcon.animate().setDuration(50).scaleX(0.9f).scaleY(0.9f).alpha(0.0f);
        this.mDismissCircle.animate().scaleX(0.9f).scaleY(0.9f).alpha(0.0f);
    }

    /* access modifiers changed from: package-private */
    public float getDismissTargetCenterY() {
        return ((float) (getTop() + this.mDismissTarget.getTop())) + (((float) this.mDismissTarget.getHeight()) / 2.0f);
    }

    /* access modifiers changed from: package-private */
    public View getDismissTarget() {
        return this.mDismissTarget;
    }
}
