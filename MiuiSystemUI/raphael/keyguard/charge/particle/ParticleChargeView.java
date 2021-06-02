package com.android.keyguard.charge.particle;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.android.keyguard.charge.container.IChargeView;
import com.android.systemui.C0013R$drawable;

public class ParticleChargeView extends IChargeView {
    private ObjectAnimator animatorRingGlow;
    private int mParticleItemContainerTranslationY;
    private ParticleRenderView mParticleRenderView;
    private ImageView mRingGlow;
    private int mRingRowMarginTop;

    public ParticleChargeView(Context context) {
        this(context, null);
    }

    public ParticleChargeView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public ParticleChargeView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mParticleItemContainerTranslationY = 457;
        this.mRingRowMarginTop = 469;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.charge.container.IChargeView
    public void addChildView() {
        super.addChildView();
        this.mParticleRenderView = new ParticleRenderView(this.mContext);
        ImageView imageView = new ImageView(this.mContext);
        this.mRingGlow = imageView;
        imageView.setImageDrawable(this.mContext.getDrawable(C0013R$drawable.charge_animation_particle_ring_glow));
        this.mRingGlow.setAlpha(0.0f);
        ViewGroup viewGroup = this.mContentContainer;
        if (viewGroup != null) {
            viewGroup.setBackgroundColor(-16777216);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(-2, -2);
            layoutParams.addRule(14);
            layoutParams.setMargins(0, this.mRingRowMarginTop, 0, 0);
            this.mContentContainer.addView(this.mRingGlow, layoutParams);
            this.mContentContainer.addView(this.mParticleRenderView, new ViewGroup.LayoutParams(-1, -1));
        }
        ParticleRenderView particleRenderView = this.mParticleRenderView;
        Point point = this.mScreenSize;
        particleRenderView.updateSizeForScreenSizeChange(point.x, point.y);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.charge.container.IChargeView
    public void startAnimationOnChildView() {
        super.startAnimationOnChildView();
        this.mRingGlow.setAlpha(0.0f);
        this.mParticleRenderView.startAnimation(this.mChargeLevel * 10);
        ObjectAnimator objectAnimator = this.animatorRingGlow;
        if (objectAnimator != null && objectAnimator.isStarted()) {
            this.animatorRingGlow.cancel();
        }
        ImageView imageView = this.mRingGlow;
        ObjectAnimator ofFloat = ObjectAnimator.ofFloat(imageView, "alpha", imageView.getAlpha(), 0.5f);
        this.animatorRingGlow = ofFloat;
        ofFloat.setInterpolator(new AccelerateDecelerateInterpolator());
        this.animatorRingGlow.setDuration(1000L);
        this.animatorRingGlow.setStartDelay(1300);
        this.animatorRingGlow.start();
    }

    @Override // com.android.keyguard.charge.container.IChargeView
    public void onAnimationUpdate(ValueAnimator valueAnimator) {
        super.onAnimationUpdate(valueAnimator);
        this.mContentContainer.setAlpha(valueAnimator.getAnimatedFraction());
    }

    @Override // com.android.keyguard.charge.container.IChargeView
    public void setProgress(int i) {
        this.mParticleRenderView.updateProgress(i * 10);
    }

    @Override // com.android.keyguard.charge.container.IChargeView
    public void startDismiss(String str) {
        super.startDismiss(str);
        ObjectAnimator objectAnimator = this.animatorRingGlow;
        if (objectAnimator != null && objectAnimator.isStarted()) {
            this.animatorRingGlow.cancel();
        }
        this.mRingGlow.setAlpha(0.0f);
        this.mParticleRenderView.reset();
        ViewGroup viewGroup = this.mContentContainer;
        ObjectAnimator ofFloat = ObjectAnimator.ofFloat(viewGroup, "alpha", viewGroup.getAlpha(), 0.0f);
        ofFloat.setDuration(600L);
        this.mDismissAnimatorSet.setInterpolator(new DecelerateInterpolator());
        this.mDismissAnimatorSet.playTogether(ofFloat);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.charge.container.IChargeView
    public void hideSystemUI() {
        setSystemUiVisibility(4864);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.charge.container.IChargeView
    public void initAnimator() {
        super.initAnimator();
        ValueAnimator ofInt = ValueAnimator.ofInt(0, 1);
        ofInt.setInterpolator(this.mQuartOutInterpolator);
        ofInt.setDuration(800L);
        ofInt.addUpdateListener(this);
        AnimatorSet animatorSet = new AnimatorSet();
        this.mEnterAnimatorSet = animatorSet;
        animatorSet.play(ofInt);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.charge.container.IChargeView
    public void updateSizeForScreenSizeChange() {
        super.updateSizeForScreenSizeChange();
        ParticleRenderView particleRenderView = this.mParticleRenderView;
        if (particleRenderView != null) {
            particleRenderView.initTargets();
            ParticleRenderView particleRenderView2 = this.mParticleRenderView;
            Point point = this.mScreenSize;
            particleRenderView2.updateSizeForScreenSizeChange(point.x, point.y);
        }
        this.mWindowManager.getDefaultDisplay().getRealSize(this.mScreenSize);
        Point point2 = this.mScreenSize;
        float min = (((float) Math.min(point2.x, point2.y)) * 1.0f) / 1080.0f;
        this.mParticleItemContainerTranslationY = (int) (457.0f * min);
        this.mRingRowMarginTop = (int) (min * 469.0f);
        ImageView imageView = this.mRingGlow;
        if (imageView != null) {
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) imageView.getLayoutParams();
            layoutParams.setMargins(0, this.mRingRowMarginTop, 0, 0);
            this.mRingGlow.setLayoutParams(layoutParams);
            this.mRingGlow.requestLayout();
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.charge.container.IChargeView
    public float getVideoTranslationY() {
        Point point = this.mScreenSize;
        int min = (int) (((((float) Math.min(point.x, point.y)) * 1.0f) / 1080.0f) * 457.0f);
        this.mParticleItemContainerTranslationY = min;
        return (float) min;
    }
}
