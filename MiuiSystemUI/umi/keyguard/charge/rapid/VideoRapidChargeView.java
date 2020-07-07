package com.android.keyguard.charge.rapid;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import com.android.keyguard.charge.ChargeUtils;
import com.android.systemui.plugins.R;

public class VideoRapidChargeView extends RapidChargeView {
    private ChargeVideoView mVideoView;

    public VideoRapidChargeView(Context context) {
        this(context, (AttributeSet) null);
    }

    public VideoRapidChargeView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public VideoRapidChargeView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        setComponentTransparent(true);
    }

    /* access modifiers changed from: protected */
    public void init(Context context) {
        super.init(context);
        this.mContentContainer.setBackgroundColor(-16777216);
    }

    /* access modifiers changed from: protected */
    public void addChildView() {
        this.mVideoView = new ChargeVideoView(this.mContext);
        this.mVideoView.setChargeUri("android.resource://" + this.mContext.getPackageName() + "/" + R.raw.wired_charge_video);
        this.mVideoView.setRapidChargeUri("android.resource://" + this.mContext.getPackageName() + "/" + R.raw.wired_quick_charge_video);
        if (this.mContentContainer != null) {
            Point point = this.mScreenSize;
            this.mContentContainer.setTranslationY((float) ((Math.max(point.x, point.y) - 2340) / 2));
            ViewGroup viewGroup = this.mContentContainer;
            ChargeVideoView chargeVideoView = this.mVideoView;
            viewGroup.addView(chargeVideoView, chargeVideoView.getVideoLayoutParams());
        }
    }

    /* access modifiers changed from: protected */
    public RelativeLayout.LayoutParams getContainerLayoutParams() {
        return new RelativeLayout.LayoutParams(1080, 2340);
    }

    /* access modifiers changed from: protected */
    public void zoomLargeOnChildView() {
        if (this.mChargeState == 0) {
            this.mVideoView.addChargeView();
        } else {
            this.mVideoView.addRapidChargeView();
        }
    }

    /* access modifiers changed from: protected */
    public void initAnimator() {
        super.initAnimator();
        ValueAnimator ofInt = ValueAnimator.ofInt(new int[]{0, 1});
        ofInt.setInterpolator(this.mQuartOutInterpolator);
        ofInt.setDuration(800);
        ofInt.addListener(this);
        ofInt.addUpdateListener(this);
        this.mEnterAnimatorSet = new AnimatorSet();
        this.mEnterAnimatorSet.play(ofInt);
    }

    public void onAnimationUpdate(ValueAnimator valueAnimator) {
        super.onAnimationUpdate(valueAnimator);
        float animatedFraction = valueAnimator.getAnimatedFraction();
        this.mContentContainer.setScaleX(1.0f);
        this.mContentContainer.setScaleY(1.0f);
        this.mContentContainer.setAlpha(animatedFraction);
    }

    public void addToWindow(String str) {
        this.mWindowShouldAdd = true;
        if (!isAttachedToWindow() && getParent() == null) {
            try {
                Log.i("VideoRapidChargeView", "addToWindow: " + str);
                setComponentTransparent(true);
                ChargeUtils.getParentView(this.mContext).addView(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void removeFromWindow(String str) {
        this.mWindowShouldAdd = false;
        if (isAttachedToWindow()) {
            try {
                Log.i("VideoRapidChargeView", "removeFromWindow: " + str);
                ChargeUtils.getParentView(this.mContext).removeView(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /* access modifiers changed from: protected */
    public void showRapidChargeAnim() {
        this.mVideoView.switchToRapidChargeAnim();
    }

    /* access modifiers changed from: protected */
    public void showNormalChargeAnim() {
        this.mVideoView.switchToNormalChargeAnim();
    }

    public void startDismiss(String str) {
        super.startDismiss(str);
        ObjectAnimator duration = ObjectAnimator.ofPropertyValuesHolder(this, new PropertyValuesHolder[]{PropertyValuesHolder.ofFloat(FrameLayout.ALPHA, new float[]{getAlpha(), 0.0f})}).setDuration(600);
        PropertyValuesHolder ofFloat = PropertyValuesHolder.ofFloat(FrameLayout.ALPHA, new float[]{this.mContentContainer.getAlpha(), 0.0f});
        ObjectAnimator duration2 = ObjectAnimator.ofPropertyValuesHolder(this.mContentContainer, new PropertyValuesHolder[]{ofFloat}).setDuration(600);
        this.mDismissAnimatorSet.setInterpolator(this.mQuartOutInterpolator);
        this.mDismissAnimatorSet.playTogether(new Animator[]{duration2});
        if (!"dismiss_for_timeout".equals(str)) {
            this.mDismissAnimatorSet.play(duration).with(duration2);
        }
        this.mDismissAnimatorSet.start();
    }

    /* access modifiers changed from: protected */
    public void stopChildAnimation() {
        this.mVideoView.stopAnimation();
        this.mVideoView.removeChargeView();
        this.mVideoView.removeRapidChargeView();
        this.mStateTip.setAlpha(0.0f);
        this.mStateTip.setTranslationY(0.0f);
        this.mGtChargeAniView.setViewInitState();
        this.mGtChargeAniView.setVisibility(8);
        this.mRapidIcon.setScaleY(0.0f);
        this.mRapidIcon.setScaleX(0.0f);
        this.mRapidIcon.setAlpha(0.0f);
        this.mSuperRapidIcon.setScaleY(0.0f);
        this.mSuperRapidIcon.setScaleX(0.0f);
        this.mSuperRapidIcon.setAlpha(0.0f);
    }

    /* access modifiers changed from: protected */
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
