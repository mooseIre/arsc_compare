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
import android.util.Property;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import com.android.systemui.plugins.R;

public class WaveRapidChargeView extends RapidChargeView {
    /* access modifiers changed from: private */
    public boolean mIsPercentViewShown;
    private ValueAnimator mPercentViewAnimator;
    /* access modifiers changed from: private */
    public boolean mPercentViewAnimatorCanceled;
    private float mTouchX;
    private WaveView mWaveView;

    public WaveRapidChargeView(Context context) {
        this(context, (AttributeSet) null);
    }

    public WaveRapidChargeView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public WaveRapidChargeView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mIsPercentViewShown = false;
        setComponentTransparent(true);
    }

    /* access modifiers changed from: protected */
    public void addChildView() {
        WaveView waveView = new WaveView(this.mContext);
        this.mWaveView = waveView;
        waveView.setWaveViewWidth(2088);
        this.mWaveView.setWaveViewHeight(2250);
        ViewGroup viewGroup = this.mContentContainer;
        if (viewGroup != null) {
            viewGroup.setBackgroundColor(-16777216);
            this.mContentContainer.addView(this.mWaveView, new FrameLayout.LayoutParams(-1, -1));
        }
    }

    /* access modifiers changed from: protected */
    public void init(Context context) {
        super.init(context);
        this.mCenterAnchorView.setTranslationX(299.0f);
        this.mPercentCountView.setTranslationX(299.0f);
        this.mPercentCountView.setTextSize(265, R.styleable.AppCompatTheme_textAppearanceListItemSecondary, R.styleable.AppCompatTheme_textAppearanceListItemSecondary);
        this.mStateTip.setTranslationX(299.0f);
        this.mGtChargeAniView.setTranslationX(299.0f);
        this.mRapidIcon.setTranslationX(299.0f);
        this.mSuperRapidIcon.setTranslationX(299.0f);
    }

    public void setProgress(int i) {
        super.setProgress(i);
        this.mWaveView.setProgress(i);
    }

    private WindowManager.LayoutParams getWaveWindowParam() {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(-1, -1, 2060, 83952904, -3);
        layoutParams.windowAnimations = 0;
        layoutParams.screenOrientation = 1;
        layoutParams.extraFlags = 32768;
        layoutParams.setTitle("rapid_charge");
        layoutParams.x = 0;
        layoutParams.y = 0;
        layoutParams.width = 2088;
        layoutParams.height = 2250;
        return layoutParams;
    }

    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        this.mTouchX = motionEvent.getX();
        return super.onInterceptTouchEvent(motionEvent);
    }

    public boolean shouldDismiss() {
        float f = this.mTouchX;
        return f > 60.0f && f < 1020.0f;
    }

    /* access modifiers changed from: protected */
    public void zoomLargeOnChildView() {
        this.mWaveView.startAnim();
        this.mPercentCountView.setTextSize(265, R.styleable.AppCompatTheme_textAppearanceListItemSecondary, R.styleable.AppCompatTheme_textAppearanceListItemSecondary);
        ValueAnimator ofFloat = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
        this.mPercentViewAnimator = ofFloat;
        ofFloat.setInterpolator(this.mQuartOutInterpolator);
        this.mPercentViewAnimator.setDuration(800);
        this.mPercentViewAnimator.setStartDelay(1000);
        this.mPercentViewAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                WaveRapidChargeView.this.mPercentCountView.setAlpha(valueAnimator.getAnimatedFraction());
            }
        });
        this.mPercentViewAnimator.addListener(new Animator.AnimatorListener() {
            public void onAnimationRepeat(Animator animator) {
            }

            public void onAnimationStart(Animator animator) {
                boolean unused = WaveRapidChargeView.this.mPercentViewAnimatorCanceled = false;
            }

            public void onAnimationEnd(Animator animator) {
                if (!WaveRapidChargeView.this.mPercentViewAnimatorCanceled) {
                    boolean unused = WaveRapidChargeView.this.mIsPercentViewShown = true;
                    WaveRapidChargeView.this.startContentSwitchAnimation();
                }
            }

            public void onAnimationCancel(Animator animator) {
                boolean unused = WaveRapidChargeView.this.mPercentViewAnimatorCanceled = true;
            }
        });
        this.mPercentViewAnimator.start();
    }

    /* access modifiers changed from: protected */
    public boolean isPercentViewShown() {
        return this.mIsPercentViewShown;
    }

    /* access modifiers changed from: protected */
    public void initAnimator() {
        super.initAnimator();
        ValueAnimator ofInt = ValueAnimator.ofInt(new int[]{0, 1});
        ofInt.setInterpolator(this.mQuartOutInterpolator);
        ofInt.setDuration(800);
        ofInt.addListener(this);
        ofInt.addUpdateListener(this);
        AnimatorSet animatorSet = new AnimatorSet();
        this.mEnterAnimatorSet = animatorSet;
        animatorSet.play(ofInt);
    }

    /* access modifiers changed from: protected */
    public void setViewState() {
        super.setViewState();
        this.mPercentCountView.setAlpha(0.0f);
        this.mIsPercentViewShown = false;
    }

    /* access modifiers changed from: protected */
    public void hideSystemUI() {
        setSystemUiVisibility(4864);
    }

    /* access modifiers changed from: protected */
    public void disableTouch(boolean z) {
        if (isAttachedToWindow()) {
            WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) getLayoutParams();
            if (z) {
                layoutParams.flags = layoutParams.flags | 16 | 8;
            } else {
                layoutParams.flags = layoutParams.flags & -17 & -9;
            }
            this.mWindowManager.updateViewLayout(this, layoutParams);
        }
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
                Log.i("WaveRapidChargeView", "addToWindow: " + str);
                setComponentTransparent(true);
                this.mWindowManager.addView(this, getWaveWindowParam());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /* access modifiers changed from: protected */
    public void dismissView() {
        if (this.mIsScreenOn) {
            removeFromWindow("dismiss");
        }
    }

    public void removeFromWindow(String str) {
        this.mWindowShouldAdd = false;
        if (isAttachedToWindow()) {
            try {
                Log.i("WaveRapidChargeView", "removeFromWindow: " + str);
                this.mWindowManager.removeViewImmediate(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void startDismiss(String str) {
        Property property = FrameLayout.ALPHA;
        super.startDismiss(str);
        this.mPercentCountView.setAlpha(0.0f);
        this.mIsPercentViewShown = false;
        ObjectAnimator duration = ObjectAnimator.ofPropertyValuesHolder(this, new PropertyValuesHolder[]{PropertyValuesHolder.ofFloat(property, new float[]{getAlpha(), 0.0f})}).setDuration(600);
        PropertyValuesHolder ofFloat = PropertyValuesHolder.ofFloat(property, new float[]{this.mContentContainer.getAlpha(), 0.0f});
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
        this.mWaveView.stopAnim();
        ValueAnimator valueAnimator = this.mPercentViewAnimator;
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
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

    /* access modifiers changed from: protected */
    public void updateSizeForScreenSizeChange() {
        super.updateSizeForScreenSizeChange();
        Point point = this.mScreenSize;
        float min = (((float) Math.min(point.x, point.y)) * 1.0f) / 1080.0f;
        this.mChargeNumberTranslateSmall = (int) (-100.0f * min);
        this.mChargeNumberTranslateInit = (int) (-40.0f * min);
        this.mIconPaddingTop = (int) (min * 305.0f);
    }
}
