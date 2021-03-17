package com.android.systemui.statusbar.phone;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.plugins.FalsingManager;
import com.android.systemui.statusbar.FlingAnimationUtils;
import com.android.systemui.statusbar.KeyguardAffordanceView;

public class KeyguardAffordanceHelper {
    private final Callback mCallback;
    private final Context mContext;
    private KeyguardAffordanceView mLeftIcon;
    private KeyguardAffordanceView mRightIcon;
    private Animator mSwipeAnimator;
    private boolean mSwipingInProgress;
    private View mTargetedView;
    private int mTouchTargetSize;

    public interface Callback {
        KeyguardAffordanceView getLeftIcon();

        KeyguardAffordanceView getRightIcon();
    }

    public abstract void animateHideLeftRightIcon();

    public abstract void launchAffordance(boolean z, boolean z2);

    public abstract void onConfigurationChanged();

    public abstract void onRtlPropertiesChanged();

    public abstract boolean onTouchEvent(MotionEvent motionEvent);

    public abstract void reset(boolean z);

    public abstract void updatePreviews();

    public KeyguardAffordanceHelper(Callback callback, Context context, FalsingManager falsingManager) {
        new AnimatorListenerAdapter() {
            /* class com.android.systemui.statusbar.phone.KeyguardAffordanceHelper.AnonymousClass1 */

            public void onAnimationEnd(Animator animator) {
                KeyguardAffordanceHelper.this.mSwipeAnimator = null;
                KeyguardAffordanceHelper.this.mSwipingInProgress = false;
                KeyguardAffordanceHelper.this.mTargetedView = null;
            }
        };
        this.mContext = context;
        this.mCallback = callback;
        initIcons();
        KeyguardAffordanceView keyguardAffordanceView = this.mLeftIcon;
        updateIcon(keyguardAffordanceView, 0.0f, keyguardAffordanceView.getRestingAlpha(), false, false, true, false);
        KeyguardAffordanceView keyguardAffordanceView2 = this.mRightIcon;
        updateIcon(keyguardAffordanceView2, 0.0f, keyguardAffordanceView2.getRestingAlpha(), false, false, true, false);
        initDimens();
    }

    private void initDimens() {
        ViewConfiguration viewConfiguration = ViewConfiguration.get(this.mContext);
        viewConfiguration.getScaledPagingTouchSlop();
        viewConfiguration.getScaledMinimumFlingVelocity();
        this.mContext.getResources().getDimensionPixelSize(C0012R$dimen.keyguard_min_swipe_amount);
        this.mContext.getResources().getDimensionPixelSize(C0012R$dimen.keyguard_affordance_min_background_radius);
        this.mTouchTargetSize = this.mContext.getResources().getDimensionPixelSize(C0012R$dimen.keyguard_affordance_touch_target_size);
        this.mContext.getResources().getDimensionPixelSize(C0012R$dimen.hint_grow_amount_sideways);
        new FlingAnimationUtils(this.mContext.getResources().getDisplayMetrics(), 0.4f);
    }

    private void initIcons() {
        this.mLeftIcon = this.mCallback.getLeftIcon();
        this.mRightIcon = this.mCallback.getRightIcon();
        updatePreviews();
    }

    public boolean isOnAffordanceIcon(float f, float f2) {
        return isOnIcon(this.mLeftIcon, f, f2) || isOnIcon(this.mRightIcon, f, f2);
    }

    private boolean isOnIcon(View view, float f, float f2) {
        return Math.hypot((double) (f - (view.getX() + (((float) view.getWidth()) / 2.0f))), (double) (f2 - (view.getY() + (((float) view.getHeight()) / 2.0f)))) <= ((double) (this.mTouchTargetSize / 2));
    }

    private void updateIcon(KeyguardAffordanceView keyguardAffordanceView, float f, float f2, boolean z, boolean z2, boolean z3, boolean z4) {
        if (keyguardAffordanceView.getVisibility() == 0 || z3) {
            if (z4) {
                keyguardAffordanceView.setCircleRadiusWithoutAnimation(f);
            } else {
                keyguardAffordanceView.setCircleRadius(f, z2);
            }
            updateIconAlpha(keyguardAffordanceView, f2, z);
        }
    }

    private void updateIconAlpha(KeyguardAffordanceView keyguardAffordanceView, float f, boolean z) {
        float scale = getScale(f, keyguardAffordanceView);
        keyguardAffordanceView.setImageAlpha(Math.min(1.0f, f), z);
        keyguardAffordanceView.setImageScale(scale, z);
    }

    private float getScale(float f, KeyguardAffordanceView keyguardAffordanceView) {
        return Math.min(((f / keyguardAffordanceView.getRestingAlpha()) * 0.2f) + 0.8f, 1.5f);
    }

    public boolean isSwipingInProgress() {
        return this.mSwipingInProgress;
    }
}
