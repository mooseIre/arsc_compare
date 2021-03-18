package com.android.keyguard.charge.wave;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Property;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.android.keyguard.charge.container.IChargeView;

public class WaveChargeView extends IChargeView {
    private WaveView mWaveView;

    public WaveChargeView(Context context) {
        this(context, null);
    }

    public WaveChargeView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public WaveChargeView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        setComponentTransparent(true);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.charge.container.IChargeView
    public void addChildView() {
        WaveView waveView = new WaveView(this.mContext);
        this.mWaveView = waveView;
        waveView.setWaveViewWidth(this.mScreenSize.x);
        this.mWaveView.setWaveViewHeight(this.mScreenSize.y);
        ViewGroup viewGroup = this.mContentContainer;
        if (viewGroup != null) {
            viewGroup.setBackgroundColor(-16777216);
            this.mContentContainer.addView(this.mWaveView, new FrameLayout.LayoutParams(-1, -1));
        }
    }

    @Override // com.android.keyguard.charge.container.IChargeView
    public void setProgress(int i) {
        super.setProgress(i);
        this.mWaveView.setProgress(i);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.charge.container.IChargeView
    public void startAnimationOnChildView() {
        Log.d("WaveChargeView", "startAnimationOnChildView: " + this.mWireState);
        this.mWaveView.startAnim();
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
    public void hideSystemUI() {
        setSystemUiVisibility(4864);
    }

    @Override // com.android.keyguard.charge.container.IChargeView
    public void onAnimationUpdate(ValueAnimator valueAnimator) {
        super.onAnimationUpdate(valueAnimator);
        float animatedFraction = valueAnimator.getAnimatedFraction();
        this.mContentContainer.setScaleX(1.0f);
        this.mContentContainer.setScaleY(1.0f);
        this.mContentContainer.setAlpha(animatedFraction);
    }

    @Override // com.android.keyguard.charge.container.IChargeView
    public void startDismiss(String str) {
        Property property = FrameLayout.ALPHA;
        super.startDismiss(str);
        ObjectAnimator duration = ObjectAnimator.ofPropertyValuesHolder(this, PropertyValuesHolder.ofFloat(property, getAlpha(), 0.0f)).setDuration(600L);
        PropertyValuesHolder ofFloat = PropertyValuesHolder.ofFloat(property, this.mContentContainer.getAlpha(), 0.0f);
        ObjectAnimator duration2 = ObjectAnimator.ofPropertyValuesHolder(this.mContentContainer, ofFloat).setDuration(600L);
        this.mDismissAnimatorSet.setInterpolator(this.mQuartOutInterpolator);
        this.mDismissAnimatorSet.playTogether(duration2);
        if (!"dismiss_for_timeout".equals(str)) {
            this.mDismissAnimatorSet.play(duration).with(duration2);
        }
        this.mDismissAnimatorSet.start();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.charge.container.IChargeView
    public void stopChildAnimation() {
        this.mWaveView.stopAnim();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.charge.container.IChargeView
    public void setComponentTransparent(boolean z) {
        super.setComponentTransparent(z);
        if (z) {
            setAlpha(0.0f);
            this.mContentContainer.setAlpha(0.0f);
            return;
        }
        setAlpha(1.0f);
        this.mContentContainer.setAlpha(1.0f);
    }
}
