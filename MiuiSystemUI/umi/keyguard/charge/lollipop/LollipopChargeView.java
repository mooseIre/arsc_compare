package com.android.keyguard.charge.lollipop;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Property;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.android.keyguard.charge.container.IChargeView;
import com.android.systemui.C0013R$drawable;

public class LollipopChargeView extends IChargeView {
    private Drawable mBottomLightDrawable;
    private int mBottomLightHeight;
    private ImageView mBottomLightImage;
    private int mBottomLightWidth;
    private LollipopWirelessAnimationView mCircleRapidView;
    private FireworksView mFireworksView;
    private ObjectAnimator mInnerCircleAnimator;
    private Drawable mInnerCircleDrawable;
    private int mInnerCircleSize;
    private ImageView mInnerCircleView;
    private int mInnerParticleCircleSize;
    private Drawable mInnerParticleDrawable;
    private OutlineView mOutlineView;
    private ObjectAnimator mParticleCircleAnimator;
    private ImageView mParticleCircleView;

    public LollipopChargeView(Context context) {
        this(context, null);
    }

    public LollipopChargeView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public LollipopChargeView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        init(context);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.charge.container.IChargeView
    public void init(Context context) {
        Property property = View.ROTATION;
        super.init(context);
        this.mInnerCircleDrawable = context.getDrawable(C0013R$drawable.charge_animation_wired_rotate_circle_icon);
        this.mInnerParticleDrawable = context.getDrawable(C0013R$drawable.charge_animation_particle_circle_icon);
        this.mBottomLightDrawable = context.getDrawable(C0013R$drawable.charge_animation_bottom_light_icon);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(-2, -2);
        layoutParams.gravity = 81;
        OutlineView outlineView = new OutlineView(context);
        this.mOutlineView = outlineView;
        addView(outlineView, layoutParams);
        FrameLayout.LayoutParams layoutParams2 = new FrameLayout.LayoutParams(-2, -2);
        layoutParams2.gravity = 81;
        FireworksView fireworksView = new FireworksView(context);
        this.mFireworksView = fireworksView;
        addView(fireworksView, layoutParams2);
        ImageView imageView = new ImageView(context);
        this.mInnerCircleView = imageView;
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        this.mInnerCircleView.setImageDrawable(this.mInnerCircleDrawable);
        int i = this.mInnerCircleSize;
        FrameLayout.LayoutParams layoutParams3 = new FrameLayout.LayoutParams(i, i);
        layoutParams3.gravity = 17;
        this.mInnerCircleView.setLayoutParams(layoutParams3);
        addView(this.mInnerCircleView);
        ObjectAnimator ofFloat = ObjectAnimator.ofFloat(this.mInnerCircleView, property, 0.0f, 360.0f);
        this.mInnerCircleAnimator = ofFloat;
        ofFloat.setInterpolator(new LinearInterpolator());
        this.mInnerCircleAnimator.setRepeatCount(-1);
        this.mInnerCircleAnimator.setDuration(6000L);
        ImageView imageView2 = new ImageView(context);
        this.mParticleCircleView = imageView2;
        imageView2.setScaleType(ImageView.ScaleType.FIT_XY);
        this.mParticleCircleView.setImageDrawable(this.mInnerParticleDrawable);
        int i2 = this.mInnerParticleCircleSize;
        FrameLayout.LayoutParams layoutParams4 = new FrameLayout.LayoutParams(i2, i2);
        layoutParams4.gravity = 17;
        this.mParticleCircleView.setLayoutParams(layoutParams4);
        addView(this.mParticleCircleView);
        ObjectAnimator ofFloat2 = ObjectAnimator.ofFloat(this.mParticleCircleView, property, 0.0f, 360.0f);
        this.mParticleCircleAnimator = ofFloat2;
        ofFloat2.setInterpolator(new LinearInterpolator());
        this.mParticleCircleAnimator.setRepeatCount(-1);
        this.mParticleCircleAnimator.setDuration(1000L);
        FrameLayout.LayoutParams layoutParams5 = new FrameLayout.LayoutParams(this.mBottomLightWidth, this.mBottomLightHeight);
        layoutParams5.gravity = 81;
        ImageView imageView3 = new ImageView(context);
        this.mBottomLightImage = imageView3;
        imageView3.setImageDrawable(this.mBottomLightDrawable);
        addView(this.mBottomLightImage, layoutParams5);
        FrameLayout.LayoutParams layoutParams6 = new FrameLayout.LayoutParams(-2, -2);
        layoutParams6.gravity = 17;
        LollipopWirelessAnimationView lollipopWirelessAnimationView = new LollipopWirelessAnimationView(context);
        this.mCircleRapidView = lollipopWirelessAnimationView;
        addView(lollipopWirelessAnimationView, layoutParams6);
        setComponentTransparent(true);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.charge.container.IChargeView
    public void startAnimationOnChildView() {
        Log.d("LollipopChargeView", "startAnimationOnChildView: " + this.mWireState);
        int i = this.mWireState;
        if (i == 11) {
            startWiredAnimation();
        } else if (i == 10) {
            startWirelessAnimation();
        }
    }

    private void startWiredAnimation() {
        this.mFireworksView.start();
        this.mInnerCircleAnimator.start();
        this.mParticleCircleAnimator.start();
    }

    private void startWirelessAnimation() {
        this.mCircleRapidView.startAnimation();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.charge.container.IChargeView
    public void initAnimator() {
        super.initAnimator();
        ValueAnimator ofInt = ValueAnimator.ofInt(0, 1);
        ofInt.setInterpolator(this.mQuartOutInterpolator);
        ofInt.setDuration(800L);
        ofInt.addUpdateListener(this);
        this.mEnterAnimatorSet = new AnimatorSet();
        Log.i("LollipopChargeView", "initAnimator mWireState=" + this.mWireState);
        int i = this.mWireState;
        if (i == 11) {
            PropertyValuesHolder ofFloat = PropertyValuesHolder.ofFloat(FrameLayout.ALPHA, 1.0f, 0.0f);
            this.mEnterAnimatorSet.play(ObjectAnimator.ofPropertyValuesHolder(this.mBottomLightImage, ofFloat).setDuration(1000L)).after(ofInt);
        } else if (i == 10) {
            this.mEnterAnimatorSet.play(ofInt);
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.charge.container.IChargeView
    public void setViewState() {
        super.setViewState();
        Log.i("LollipopChargeView", "setViewState mWireState=" + this.mWireState);
        this.mInnerCircleView.setAlpha(0.0f);
        this.mInnerCircleView.setScaleX(0.0f);
        this.mInnerCircleView.setScaleY(0.0f);
        this.mParticleCircleView.setAlpha(0.0f);
        this.mParticleCircleView.setScaleX(0.0f);
        this.mParticleCircleView.setScaleY(0.0f);
        this.mFireworksView.setAlpha(0.0f);
        this.mOutlineView.setAlpha(0.0f);
        this.mBottomLightImage.setAlpha(0.0f);
        this.mCircleRapidView.setScaleX(0.0f);
        this.mCircleRapidView.setScaleY(0.0f);
        this.mCircleRapidView.setAlpha(0.0f);
    }

    @Override // com.android.keyguard.charge.container.IChargeView
    public void onAnimationUpdate(ValueAnimator valueAnimator) {
        super.onAnimationUpdate(valueAnimator);
        float animatedFraction = valueAnimator.getAnimatedFraction();
        this.mContentContainer.setScaleX(animatedFraction);
        this.mContentContainer.setScaleY(animatedFraction);
        this.mContentContainer.setAlpha(animatedFraction);
        int i = this.mWireState;
        if (i == 11) {
            this.mInnerCircleView.setScaleX(animatedFraction);
            this.mInnerCircleView.setScaleY(animatedFraction);
            this.mInnerCircleView.setAlpha(animatedFraction);
            this.mParticleCircleView.setScaleX(animatedFraction);
            this.mParticleCircleView.setScaleY(animatedFraction);
            this.mParticleCircleView.setAlpha(animatedFraction);
            this.mFireworksView.setAlpha(animatedFraction);
            this.mOutlineView.setAlpha(animatedFraction);
            this.mBottomLightImage.setAlpha(animatedFraction);
        } else if (i == 10) {
            this.mCircleRapidView.setScaleX(animatedFraction);
            this.mCircleRapidView.setScaleY(animatedFraction);
            this.mCircleRapidView.setAlpha(animatedFraction);
        }
    }

    @Override // com.android.keyguard.charge.container.IChargeView
    public void startDismiss(String str) {
        Property property = FrameLayout.SCALE_Y;
        Property property2 = FrameLayout.SCALE_X;
        Property property3 = FrameLayout.ALPHA;
        super.startDismiss(str);
        ObjectAnimator duration = ObjectAnimator.ofPropertyValuesHolder(this, PropertyValuesHolder.ofFloat(property3, getAlpha(), 0.0f)).setDuration(600L);
        PropertyValuesHolder ofFloat = PropertyValuesHolder.ofFloat(property3, this.mContentContainer.getAlpha(), 0.0f);
        PropertyValuesHolder ofFloat2 = PropertyValuesHolder.ofFloat(property2, this.mContentContainer.getScaleX(), 0.0f);
        PropertyValuesHolder ofFloat3 = PropertyValuesHolder.ofFloat(property, this.mContentContainer.getScaleY(), 0.0f);
        ObjectAnimator duration2 = ObjectAnimator.ofPropertyValuesHolder(this.mContentContainer, ofFloat, ofFloat2, ofFloat3).setDuration(600L);
        Log.i("LollipopChargeView", ",startDismiss=" + str + " ,mWireState=" + this.mWireState);
        int i = this.mWireState;
        if (i == 11) {
            PropertyValuesHolder ofFloat4 = PropertyValuesHolder.ofFloat(property3, this.mOutlineView.getAlpha(), 0.0f);
            ObjectAnimator duration3 = ObjectAnimator.ofPropertyValuesHolder(this.mOutlineView, ofFloat4).setDuration(600L);
            PropertyValuesHolder ofFloat5 = PropertyValuesHolder.ofFloat(property3, this.mInnerCircleView.getAlpha(), 0.0f);
            PropertyValuesHolder ofFloat6 = PropertyValuesHolder.ofFloat(property2, this.mInnerCircleView.getScaleX(), 0.0f);
            PropertyValuesHolder ofFloat7 = PropertyValuesHolder.ofFloat(property, this.mInnerCircleView.getScaleY(), 0.0f);
            ObjectAnimator duration4 = ObjectAnimator.ofPropertyValuesHolder(this.mInnerCircleView, ofFloat5, ofFloat6, ofFloat7).setDuration(600L);
            PropertyValuesHolder ofFloat8 = PropertyValuesHolder.ofFloat(property3, this.mParticleCircleView.getAlpha(), 0.0f);
            PropertyValuesHolder ofFloat9 = PropertyValuesHolder.ofFloat(property2, this.mParticleCircleView.getScaleX(), 0.0f);
            PropertyValuesHolder ofFloat10 = PropertyValuesHolder.ofFloat(property, this.mParticleCircleView.getScaleY(), 0.0f);
            ObjectAnimator duration5 = ObjectAnimator.ofPropertyValuesHolder(this.mParticleCircleView, ofFloat8, ofFloat9, ofFloat10).setDuration(600L);
            PropertyValuesHolder ofFloat11 = PropertyValuesHolder.ofFloat(property3, this.mFireworksView.getAlpha(), 0.0f);
            ObjectAnimator duration6 = ObjectAnimator.ofPropertyValuesHolder(this.mFireworksView, ofFloat11).setDuration(600L);
            PropertyValuesHolder ofFloat12 = PropertyValuesHolder.ofFloat(property3, this.mBottomLightImage.getAlpha(), 0.0f);
            ObjectAnimator duration7 = ObjectAnimator.ofPropertyValuesHolder(this.mBottomLightImage, ofFloat12).setDuration(600L);
            this.mDismissAnimatorSet.playTogether(duration2, duration3, duration4, duration5, duration6, duration7);
        } else if (i == 10) {
            PropertyValuesHolder ofFloat13 = PropertyValuesHolder.ofFloat(property3, this.mCircleRapidView.getAlpha(), 0.0f);
            PropertyValuesHolder ofFloat14 = PropertyValuesHolder.ofFloat(property2, this.mCircleRapidView.getScaleX(), 0.0f);
            PropertyValuesHolder ofFloat15 = PropertyValuesHolder.ofFloat(property, this.mCircleRapidView.getScaleY(), 0.0f);
            ObjectAnimator duration8 = ObjectAnimator.ofPropertyValuesHolder(this.mCircleRapidView, ofFloat13, ofFloat14, ofFloat15).setDuration(600L);
            this.mDismissAnimatorSet.playTogether(duration2, duration8);
        }
        this.mDismissAnimatorSet.setInterpolator(this.mQuartOutInterpolator);
        if (!"dismiss_for_timeout".equals(str)) {
            this.mDismissAnimatorSet.play(duration).with(duration2);
        }
        this.mDismissAnimatorSet.start();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.charge.container.IChargeView
    public void stopChildAnimation() {
        Log.d("LollipopChargeView", "stopChildAnimation: " + this.mWireState);
        int i = this.mWireState;
        if (i == 11) {
            this.mFireworksView.stop();
            this.mInnerCircleAnimator.cancel();
            this.mParticleCircleAnimator.cancel();
        } else if (i == 10) {
            this.mCircleRapidView.stopAnimation();
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.charge.container.IChargeView
    public void setComponentTransparent(boolean z) {
        super.setComponentTransparent(z);
        Log.d("LollipopChargeView", "setComponentTransparent: " + z + " mWireState=" + this.mWireState);
        if (z) {
            setAlpha(0.0f);
            this.mInnerCircleView.setAlpha(0.0f);
            this.mParticleCircleView.setAlpha(0.0f);
            this.mFireworksView.setAlpha(0.0f);
            this.mOutlineView.setAlpha(0.0f);
            this.mContentContainer.setAlpha(0.0f);
            this.mBottomLightImage.setAlpha(0.0f);
            this.mCircleRapidView.setAlpha(0.0f);
            return;
        }
        setAlpha(1.0f);
        int i = this.mWireState;
        if (i == 11) {
            this.mInnerCircleView.setAlpha(1.0f);
            this.mParticleCircleView.setAlpha(1.0f);
            this.mFireworksView.setAlpha(1.0f);
            this.mOutlineView.setAlpha(1.0f);
            this.mContentContainer.setAlpha(1.0f);
            this.mBottomLightImage.setAlpha(1.0f);
            this.mCircleRapidView.setAlpha(0.0f);
        } else if (i == 10) {
            this.mInnerCircleView.setAlpha(0.0f);
            this.mParticleCircleView.setAlpha(0.0f);
            this.mFireworksView.setAlpha(0.0f);
            this.mOutlineView.setAlpha(0.0f);
            this.mBottomLightImage.setAlpha(0.0f);
            this.mContentContainer.setAlpha(1.0f);
            this.mCircleRapidView.setAlpha(1.0f);
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.charge.container.IChargeView
    public void updateSizeForScreenSizeChange() {
        super.updateSizeForScreenSizeChange();
        Point point = this.mScreenSize;
        float min = (((float) Math.min(point.x, point.y)) * 1.0f) / 1080.0f;
        this.mInnerCircleSize = (int) (662.0f * min);
        this.mInnerParticleCircleSize = (int) (612.0f * min);
        Drawable drawable = this.mBottomLightDrawable;
        if (drawable != null) {
            this.mBottomLightWidth = (int) (((float) drawable.getIntrinsicWidth()) * min);
            this.mBottomLightHeight = (int) (min * ((float) this.mBottomLightDrawable.getIntrinsicHeight()));
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.charge.container.IChargeView
    public void updateLayoutParamForScreenSizeChange() {
        super.updateLayoutParamForScreenSizeChange();
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) this.mInnerCircleView.getLayoutParams();
        int i = this.mInnerCircleSize;
        layoutParams.width = i;
        layoutParams.height = i;
        FrameLayout.LayoutParams layoutParams2 = (FrameLayout.LayoutParams) this.mParticleCircleView.getLayoutParams();
        int i2 = this.mInnerParticleCircleSize;
        layoutParams2.width = i2;
        layoutParams2.height = i2;
        FrameLayout.LayoutParams layoutParams3 = (FrameLayout.LayoutParams) this.mBottomLightImage.getLayoutParams();
        layoutParams3.width = this.mBottomLightWidth;
        layoutParams3.height = this.mBottomLightHeight;
    }
}
