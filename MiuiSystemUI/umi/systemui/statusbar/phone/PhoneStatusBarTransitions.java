package com.android.systemui.statusbar.phone;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;
import com.android.systemui.plugins.R;

public final class PhoneStatusBarTransitions extends BarTransitions {
    private View mBattery;
    private View mClock;
    private Animator mCurrentAnimation;
    private final float mIconAlphaWhenOpaque;
    private View mLeftSide;
    private View mSignalCluster;
    private View mStatusIcons;
    private final PhoneStatusBarView mView;

    private boolean isOpaque(int i) {
        return (i == 1 || i == 2 || i == 4 || i == 6) ? false : true;
    }

    public PhoneStatusBarTransitions(PhoneStatusBarView phoneStatusBarView) {
        super(phoneStatusBarView, R.drawable.status_background, R.color.system_status_bar_background_opaque);
        this.mView = phoneStatusBarView;
        this.mIconAlphaWhenOpaque = phoneStatusBarView.getContext().getResources().getFraction(R.dimen.status_bar_icon_drawing_alpha, 1, 1);
    }

    public void init() {
        this.mLeftSide = this.mView.findViewById(R.id.notification_icon_area);
        this.mStatusIcons = this.mView.findViewById(R.id.statusIcons);
        this.mSignalCluster = this.mView.findViewById(R.id.signal_cluster);
        this.mBattery = this.mView.findViewById(R.id.battery);
        this.mClock = this.mView.findViewById(R.id.clock);
        applyModeBackground(-1, getMode(), false);
        applyMode(getMode(), false);
    }

    public ObjectAnimator animateTransitionTo(View view, float f) {
        return ObjectAnimator.ofFloat(view, "alpha", new float[]{view.getAlpha(), f});
    }

    private float getNonBatteryClockAlphaFor(int i) {
        if (isLightsOut(i)) {
            return 0.0f;
        }
        if (!isOpaque(i)) {
            return 1.0f;
        }
        return this.mIconAlphaWhenOpaque;
    }

    private float getBatteryClockAlpha(int i) {
        if (isLightsOut(i)) {
            return 0.5f;
        }
        return getNonBatteryClockAlphaFor(i);
    }

    /* access modifiers changed from: protected */
    public void onTransition(int i, int i2, boolean z) {
        super.onTransition(i, i2, z);
        applyMode(i2, z);
    }

    private void applyMode(int i, boolean z) {
        if (this.mLeftSide != null) {
            float nonBatteryClockAlphaFor = getNonBatteryClockAlphaFor(i);
            float batteryClockAlpha = getBatteryClockAlpha(i);
            Animator animator = this.mCurrentAnimation;
            if (animator != null) {
                animator.cancel();
            }
            if (z) {
                AnimatorSet animatorSet = new AnimatorSet();
                animatorSet.playTogether(new Animator[]{animateTransitionTo(this.mLeftSide, nonBatteryClockAlphaFor), animateTransitionTo(this.mStatusIcons, nonBatteryClockAlphaFor), animateTransitionTo(this.mSignalCluster, nonBatteryClockAlphaFor), animateTransitionTo(this.mBattery, batteryClockAlpha), animateTransitionTo(this.mClock, batteryClockAlpha)});
                if (isLightsOut(i)) {
                    animatorSet.setDuration(750);
                }
                animatorSet.start();
                this.mCurrentAnimation = animatorSet;
                return;
            }
            this.mLeftSide.setAlpha(nonBatteryClockAlphaFor);
            this.mStatusIcons.setAlpha(nonBatteryClockAlphaFor);
            this.mSignalCluster.setAlpha(nonBatteryClockAlphaFor);
            this.mBattery.setAlpha(batteryClockAlpha);
            this.mClock.setAlpha(batteryClockAlpha);
        }
    }
}
