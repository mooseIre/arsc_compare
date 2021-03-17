package com.android.keyguard.charge.container;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import com.android.keyguard.charge.ChargeUtils;
import com.android.keyguard.charge.MiuiBatteryStatus;
import com.android.systemui.C0010R$bool;
import miui.maml.animation.interpolater.QuartEaseOutInterpolater;

public class IChargeView extends FrameLayout implements ValueAnimator.AnimatorUpdateListener {
    protected int mChargeSpeed;
    protected ViewGroup mContentContainer;
    protected Context mContext;
    protected AnimatorSet mDismissAnimatorSet;
    protected AnimatorSet mEnterAnimatorSet;
    private boolean mInitScreenOn;
    protected boolean mIsFoldChargeVideo;
    protected Interpolator mQuartOutInterpolator;
    protected Point mScreenSize;
    private boolean mStartingDismissAnim;
    protected WindowManager mWindowManager;
    protected int mWireState;

    /* access modifiers changed from: protected */
    public void addChildView() {
    }

    /* access modifiers changed from: protected */
    public float getVideoTranslationY() {
        return 0.0f;
    }

    /* access modifiers changed from: protected */
    public void hideSystemUI() {
    }

    /* access modifiers changed from: protected */
    public void initAnimator() {
    }

    /* access modifiers changed from: protected */
    public void setComponentTransparent(boolean z) {
    }

    public void setProgress(int i) {
    }

    /* access modifiers changed from: protected */
    public void setViewState() {
    }

    /* access modifiers changed from: protected */
    public void startAnimationOnChildView() {
    }

    /* access modifiers changed from: protected */
    public void stopChildAnimation() {
    }

    public void switchContainerViewAnimation(int i) {
    }

    /* access modifiers changed from: protected */
    public void updateLayoutParamForScreenSizeChange() {
    }

    /* access modifiers changed from: protected */
    public void updateSizeForScreenSizeChange() {
    }

    public IChargeView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public IChargeView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mQuartOutInterpolator = new QuartEaseOutInterpolater();
        this.mIsFoldChargeVideo = false;
        init(context);
    }

    /* access modifiers changed from: protected */
    public void init(Context context) {
        this.mIsFoldChargeVideo = context.getResources().getBoolean(C0010R$bool.config_folding_charge_video);
        this.mContext = context;
        this.mWindowManager = (WindowManager) context.getSystemService("window");
        this.mScreenSize = new Point();
        this.mWindowManager.getDefaultDisplay().getRealSize(this.mScreenSize);
        updateSizeForScreenSizeChange();
        this.mChargeSpeed = 0;
        hideSystemUI();
        this.mContentContainer = new RelativeLayout(context);
        new RelativeLayout.LayoutParams(-1, -1).addRule(13);
        addChildView();
        addView(this.mContentContainer, getContainerLayoutParams());
    }

    private RelativeLayout.LayoutParams getContainerLayoutParams() {
        return new RelativeLayout.LayoutParams(-1, -1);
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    public void startAnimation(boolean z) {
        Log.d("IChargeView", "startAnimation: mInitScreenOn " + z);
        this.mInitScreenOn = z;
        MiuiBatteryStatus miuiBatteryStatus = ChargeUtils.sBatteryStatus;
        this.mWireState = miuiBatteryStatus.wireState;
        this.mChargeSpeed = miuiBatteryStatus.chargeSpeed;
        AnimatorSet animatorSet = this.mDismissAnimatorSet;
        if (animatorSet != null && this.mStartingDismissAnim) {
            animatorSet.cancel();
        }
        this.mStartingDismissAnim = false;
        hideSystemUI();
        setAlpha(this.mInitScreenOn ? 0.0f : 1.0f);
        setViewState();
        setVisibility(0);
        requestFocus();
        initAnimator();
        if (this.mEnterAnimatorSet.isStarted()) {
            this.mEnterAnimatorSet.cancel();
        }
        this.mEnterAnimatorSet.start();
        setComponentTransparent(false);
        startAnimationOnChildView();
    }

    public void startDismiss(String str) {
        if (!this.mStartingDismissAnim) {
            AnimatorSet animatorSet = this.mEnterAnimatorSet;
            if (animatorSet != null) {
                animatorSet.cancel();
            }
            Log.i("IChargeView", "startDismiss: reason: " + str);
            AnimatorSet animatorSet2 = new AnimatorSet();
            this.mDismissAnimatorSet = animatorSet2;
            animatorSet2.addListener(new Animator.AnimatorListener() {
                /* class com.android.keyguard.charge.container.IChargeView.AnonymousClass1 */

                public void onAnimationRepeat(Animator animator) {
                }

                public void onAnimationStart(Animator animator) {
                }

                public void onAnimationEnd(Animator animator) {
                    if (IChargeView.this.mStartingDismissAnim) {
                        IChargeView.this.dismiss();
                    }
                    IChargeView.this.mStartingDismissAnim = false;
                }

                public void onAnimationCancel(Animator animator) {
                    IChargeView.this.mStartingDismissAnim = false;
                }
            });
            this.mStartingDismissAnim = true;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dismiss() {
        stopChildAnimation();
        setComponentTransparent(true);
    }

    public void onAnimationUpdate(ValueAnimator valueAnimator) {
        float animatedFraction = valueAnimator.getAnimatedFraction();
        if (!this.mInitScreenOn) {
            animatedFraction = 1.0f;
        }
        setAlpha(animatedFraction);
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        checkScreenSize();
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        checkScreenSize();
    }

    private void checkScreenSize() {
        Point point = new Point();
        this.mWindowManager.getDefaultDisplay().getRealSize(point);
        if (!this.mScreenSize.equals(point.x, point.y)) {
            this.mScreenSize.set(point.x, point.y);
            updateSizeForScreenSizeChange();
            updateLayoutParamForScreenSizeChange();
            requestLayout();
        }
    }
}
