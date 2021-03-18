package com.android.keyguard.charge.container;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Property;
import android.util.Slog;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.charge.ChargeUtils;
import com.android.keyguard.charge.MiuiChargeManager;
import com.android.keyguard.charge.OrientationEventListenerWrapper;
import com.android.keyguard.charge.view.IChargeAnimationListener;
import com.android.keyguard.charge.view.MiuiChargePercentCountView;
import com.android.keyguard.injector.KeyguardUpdateMonitorInjector;
import com.android.systemui.C0010R$bool;
import com.android.systemui.Dependency;
import miui.maml.animation.interpolater.QuartEaseOutInterpolater;

public class MiuiChargeAnimationView extends FrameLayout {
    private IChargeAnimationListener animationListener;
    private ViewGroup itemContainer;
    private MiuiChargeContainerView mChargeContainerView;
    private MiuiChargeIconView mChargeIconView;
    private MiuiChargeLogoView mChargeLogoView;
    private MiuiChargePercentCountView mChargePercentView;
    private final Configuration mConfiguration;
    private AnimatorSet mDismissAnimatorSet;
    private String mDismissReason;
    private final Runnable mDismissRunnable;
    private final Handler mHandler;
    private int mIconPaddingTop;
    private boolean mIsFoldChargeVideo;
    private OrientationEventListenerWrapper mOrientationListener;
    private ViewGroup mParentContainer;
    private final Interpolator mQuartOutInterpolator;
    private Point mScreenSize;
    private boolean mShowChargingInNonLockscreen;
    private AnimatorSet mShowingAnimatorSet;
    private boolean mStartingDismissAnim;
    private final boolean mSupportWaveChargeAnimation;
    private Runnable mTimeoutDismissJob;
    private KeyguardUpdateMonitorInjector mUpdateMonitorInjector;
    private WindowManager mWindowManager;
    private int mWireState;

    public MiuiChargeAnimationView(Context context) {
        this(context, null);
    }

    public MiuiChargeAnimationView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public MiuiChargeAnimationView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mHandler = new Handler();
        this.mQuartOutInterpolator = new QuartEaseOutInterpolater();
        this.mConfiguration = new Configuration();
        this.mIsFoldChargeVideo = false;
        this.mSupportWaveChargeAnimation = ChargeUtils.supportWaveChargeAnimation();
        this.mTimeoutDismissJob = new Runnable() {
            /* class com.android.keyguard.charge.container.MiuiChargeAnimationView.AnonymousClass6 */

            public void run() {
                MiuiChargeAnimationView.this.startDismiss("dismiss_for_timeout");
            }
        };
        this.mDismissRunnable = new Runnable() {
            /* class com.android.keyguard.charge.container.MiuiChargeAnimationView.AnonymousClass7 */

            public void run() {
                MiuiChargeAnimationView.this.stopChildAnimation();
                MiuiChargeAnimationView.this.setComponentTransparent(true);
                MiuiChargeAnimationView.this.dismissView();
                MiuiChargeAnimationView.this.onDismissAnimationDismiss();
            }
        };
        init(context);
    }

    /* access modifiers changed from: protected */
    public void init(Context context) {
        this.mWindowManager = (WindowManager) context.getSystemService("window");
        this.mIsFoldChargeVideo = context.getResources().getBoolean(C0010R$bool.config_folding_charge_video);
        this.mScreenSize = new Point();
        updateSizeForScreenSizeChange();
        RelativeLayout relativeLayout = new RelativeLayout(context);
        this.mParentContainer = relativeLayout;
        relativeLayout.setBackgroundColor(Color.argb(242, 0, 0, 0));
        new RelativeLayout.LayoutParams(-1, -1).addRule(13);
        this.mChargeContainerView = new MiuiChargeContainerView(context);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(-1, -1);
        layoutParams.addRule(13);
        this.mParentContainer.addView(this.mChargeContainerView, layoutParams);
        this.itemContainer = new RelativeLayout(context);
        RelativeLayout.LayoutParams layoutParams2 = new RelativeLayout.LayoutParams(-2, -2);
        layoutParams2.addRule(13);
        this.mChargePercentView = new MiuiChargePercentCountView(context);
        RelativeLayout.LayoutParams layoutParams3 = new RelativeLayout.LayoutParams(-2, -2);
        layoutParams3.addRule(13);
        this.itemContainer.addView(this.mChargePercentView, layoutParams3);
        this.mChargeLogoView = new MiuiChargeLogoView(context);
        RelativeLayout.LayoutParams layoutParams4 = new RelativeLayout.LayoutParams(-2, -2);
        layoutParams4.addRule(13);
        this.itemContainer.addView(this.mChargeLogoView, layoutParams4);
        this.mChargeIconView = new MiuiChargeIconView(context);
        RelativeLayout.LayoutParams layoutParams5 = new RelativeLayout.LayoutParams(-2, -2);
        layoutParams5.addRule(13);
        this.mChargeIconView.setPadding(0, this.mIconPaddingTop, 0, 0);
        this.itemContainer.addView(this.mChargeIconView, layoutParams5);
        this.mParentContainer.addView(this.itemContainer, layoutParams2);
        addView(this.mParentContainer, getContainerLayoutParams());
        if (ChargeUtils.supportWaveChargeAnimation()) {
            this.itemContainer.setTranslationX(299.0f);
        }
        if (!this.mIsFoldChargeVideo) {
            this.itemContainer.setTranslationY(this.mChargeContainerView.getVideoTranslationY());
        }
        setElevation(30.0f);
        this.mUpdateMonitorInjector = (KeyguardUpdateMonitorInjector) Dependency.get(KeyguardUpdateMonitorInjector.class);
        this.mOrientationListener = new OrientationEventListenerWrapper(context) {
            /* class com.android.keyguard.charge.container.MiuiChargeAnimationView.AnonymousClass1 */

            @Override // com.android.keyguard.charge.OrientationEventListenerWrapper
            public void onOrientationChanged(int i) {
                MiuiChargeAnimationView.this.updateOrientation(i);
            }
        };
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateOrientation(int i) {
        if (this.mWireState != 11 && !this.mSupportWaveChargeAnimation) {
            Slog.i("MiuiChargeAnimationView", "onOrientationChanged: " + i);
            if (i > 45 && i < 135) {
                this.itemContainer.setRotation(270.0f);
                this.mChargeContainerView.setRotation(270.0f);
            } else if (i <= 225 || i >= 315) {
                this.itemContainer.setRotation(0.0f);
                this.mChargeContainerView.setRotation(0.0f);
            } else {
                this.itemContainer.setRotation(90.0f);
                this.mChargeContainerView.setRotation(90.0f);
            }
        }
    }

    /* access modifiers changed from: protected */
    public RelativeLayout.LayoutParams getContainerLayoutParams() {
        return new RelativeLayout.LayoutParams(-1, -1);
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        if ((this.mConfiguration.updateFrom(configuration) & 2048) != 0) {
            checkScreenSize();
        }
    }

    private void checkScreenSize() {
        this.mWindowManager.getDefaultDisplay().getRealSize(this.mScreenSize);
        updateSizeForScreenSizeChange();
        updateLayoutParamForScreenSizeChange();
    }

    /* access modifiers changed from: protected */
    public void updateSizeForScreenSizeChange() {
        Point point = this.mScreenSize;
        float f = 1.0f;
        float min = (((float) Math.min(point.x, point.y)) * 1.0f) / 1080.0f;
        if (this.mIsFoldChargeVideo) {
            if (min <= 1.0f) {
                f = min;
            }
            min = f;
        }
        this.mIconPaddingTop = (int) (min * 275.0f);
    }

    /* access modifiers changed from: protected */
    public void updateLayoutParamForScreenSizeChange() {
        this.mChargeIconView.setPadding(0, this.mIconPaddingTop, 0, 0);
    }

    public void setChargeAnimationListener(IChargeAnimationListener iChargeAnimationListener) {
        this.animationListener = iChargeAnimationListener;
    }

    /* access modifiers changed from: protected */
    public void setComponentTransparent(boolean z) {
        if (z) {
            setAlpha(0.0f);
        } else {
            setAlpha(1.0f);
        }
    }

    public void setProgress(int i) {
        this.mChargePercentView.setProgress(i);
        this.mChargeContainerView.setProgress(i);
    }

    public void startValueAnimation(float f, float f2) {
        this.mChargePercentView.startValueAnimation(f, f2);
        this.mHandler.removeCallbacks(this.mTimeoutDismissJob);
        this.mHandler.postDelayed(this.mTimeoutDismissJob, this.mShowChargingInNonLockscreen ? 5800 : 9400);
    }

    public void startChargeAnimation(boolean z, boolean z2) {
        AnimatorSet animatorSet;
        Log.d("MiuiChargeAnimationView", "startChargeAnimation: mInitScreenOn " + z + ", clickShow=" + z2);
        if (this.mStartingDismissAnim && (animatorSet = this.mDismissAnimatorSet) != null) {
            animatorSet.cancel();
        }
        setComponentTransparent(false);
        int i = ChargeUtils.sBatteryStatus.wireState;
        this.mWireState = i;
        if (i != 10 || this.mSupportWaveChargeAnimation) {
            this.itemContainer.setRotation(0.0f);
            this.mChargeContainerView.setRotation(0.0f);
        } else {
            enableOrientation();
        }
        ValueAnimator ofInt = ValueAnimator.ofInt(0, 1);
        ofInt.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.android.keyguard.charge.container.MiuiChargeAnimationView.AnonymousClass2 */

            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                MiuiChargeAnimationView.this.mParentContainer.setAlpha(valueAnimator.getAnimatedFraction());
            }
        });
        ValueAnimator ofInt2 = ValueAnimator.ofInt(0, 1);
        ofInt2.setStartDelay((long) ChargeUtils.getWaveTextDelayTime());
        ofInt2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.android.keyguard.charge.container.MiuiChargeAnimationView.AnonymousClass3 */

            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                MiuiChargeAnimationView.this.itemContainer.setAlpha(valueAnimator.getAnimatedFraction());
            }
        });
        AnimatorSet animatorSet2 = new AnimatorSet();
        this.mShowingAnimatorSet = animatorSet2;
        animatorSet2.setDuration(800L);
        this.mShowingAnimatorSet.setInterpolator(this.mQuartOutInterpolator);
        this.mShowingAnimatorSet.addListener(new AnimatorListenerAdapter() {
            /* class com.android.keyguard.charge.container.MiuiChargeAnimationView.AnonymousClass4 */

            public void onAnimationStart(Animator animator) {
                super.onAnimationStart(animator);
                MiuiChargeAnimationView.this.onChargeAnimationStart();
            }
        });
        this.mShowingAnimatorSet.playTogether(ofInt, ofInt2);
        this.mShowingAnimatorSet.start();
        this.mChargeContainerView.startContainerAnimation(z);
        this.mChargePercentView.startPercentViewAnimation(z2);
        this.mChargeLogoView.startLogoAnimation(z2);
        this.mChargeIconView.startLightningAnimation();
    }

    public void switchChargeItemViewAnimation(boolean z, int i) {
        Log.d("MiuiChargeAnimationView", "switchChargeItemViewAnimation: , clickShow=" + z + " chargeSpeed=" + i);
        this.mChargeContainerView.switchContainerViewAnimation(i);
        this.mChargePercentView.switchPercentViewAnimation(i);
        this.mChargeLogoView.switchLogoAnimation(i);
        this.mChargeIconView.switchLightningAnimation(i);
    }

    public void startDismiss(final String str) {
        Property property = FrameLayout.ALPHA;
        if (TextUtils.equals(str, "dismiss_for_timeout")) {
            ((KeyguardUpdateMonitorInjector) Dependency.get(KeyguardUpdateMonitorInjector.class)).handleChargeAnimationShowingChanged(false);
        }
        if (!this.mStartingDismissAnim) {
            AnimatorSet animatorSet = this.mShowingAnimatorSet;
            if (animatorSet != null && animatorSet.isStarted()) {
                this.mShowingAnimatorSet.cancel();
            }
            disableOrientation();
            Log.i("MiuiChargeAnimationView", "startDismiss: reason: " + str);
            this.mDismissReason = str;
            this.mHandler.removeCallbacks(this.mTimeoutDismissJob);
            this.mHandler.removeCallbacks(this.mDismissRunnable);
            PropertyValuesHolder ofFloat = PropertyValuesHolder.ofFloat(property, this.mParentContainer.getAlpha(), 0.0f);
            PropertyValuesHolder ofFloat2 = PropertyValuesHolder.ofFloat(FrameLayout.SCALE_X, this.itemContainer.getScaleX(), 0.0f);
            PropertyValuesHolder ofFloat3 = PropertyValuesHolder.ofFloat(FrameLayout.SCALE_Y, this.itemContainer.getScaleY(), 0.0f);
            ObjectAnimator ofPropertyValuesHolder = ObjectAnimator.ofPropertyValuesHolder(this.mParentContainer, ofFloat);
            PropertyValuesHolder ofFloat4 = PropertyValuesHolder.ofFloat(property, this.itemContainer.getAlpha(), 0.0f);
            ObjectAnimator ofPropertyValuesHolder2 = ObjectAnimator.ofPropertyValuesHolder(this.itemContainer, ofFloat2, ofFloat3, ofFloat4);
            AnimatorSet animatorSet2 = new AnimatorSet();
            this.mDismissAnimatorSet = animatorSet2;
            animatorSet2.setDuration(600L);
            this.mDismissAnimatorSet.setInterpolator(this.mQuartOutInterpolator);
            this.mDismissAnimatorSet.addListener(new Animator.AnimatorListener() {
                /* class com.android.keyguard.charge.container.MiuiChargeAnimationView.AnonymousClass5 */

                public void onAnimationRepeat(Animator animator) {
                }

                public void onAnimationStart(Animator animator) {
                    MiuiChargeAnimationView.this.mChargeContainerView.startDismiss(str);
                }

                public void onAnimationEnd(Animator animator) {
                    if (MiuiChargeAnimationView.this.mStartingDismissAnim) {
                        MiuiChargeAnimationView.this.onDismissAnimationEnd();
                        MiuiChargeAnimationView.this.itemContainer.setAlpha(0.0f);
                    }
                    MiuiChargeAnimationView.this.itemContainer.setScaleX(1.0f);
                    MiuiChargeAnimationView.this.itemContainer.setScaleY(1.0f);
                    MiuiChargeAnimationView.this.mStartingDismissAnim = false;
                }

                public void onAnimationCancel(Animator animator) {
                    MiuiChargeAnimationView.this.onDismissAnimationCancel();
                }
            });
            if (!this.mUpdateMonitorInjector.isKeyguardShowing() || !TextUtils.equals(str, "dismiss_for_timeout")) {
                this.mDismissAnimatorSet.playTogether(ofPropertyValuesHolder, ofPropertyValuesHolder2);
            } else {
                this.mDismissAnimatorSet.play(ofPropertyValuesHolder2);
            }
            this.mDismissAnimatorSet.start();
            this.mStartingDismissAnim = true;
        }
    }

    public void onChargeAnimationStart() {
        IChargeAnimationListener iChargeAnimationListener = this.animationListener;
        if (iChargeAnimationListener != null) {
            iChargeAnimationListener.onChargeAnimationStart(this.mWireState);
        }
        this.mHandler.removeCallbacks(this.mTimeoutDismissJob);
        this.mHandler.postDelayed(this.mTimeoutDismissJob, (long) (getAnimationDuration() - 600));
    }

    public void onDismissAnimationEnd() {
        this.mStartingDismissAnim = false;
        IChargeAnimationListener iChargeAnimationListener = this.animationListener;
        if (iChargeAnimationListener != null) {
            iChargeAnimationListener.onChargeAnimationEnd(this.mWireState, this.mDismissReason);
        }
        if (this.itemContainer.getRotation() > 0.0f || this.mChargeContainerView.getRotation() > 0.0f) {
            this.itemContainer.setRotation(0.0f);
            this.mChargeContainerView.setRotation(0.0f);
        }
        this.mHandler.post(this.mDismissRunnable);
    }

    public void onDismissAnimationCancel() {
        this.mStartingDismissAnim = false;
        IChargeAnimationListener iChargeAnimationListener = this.animationListener;
        if (iChargeAnimationListener != null) {
            iChargeAnimationListener.onChargeAnimationEnd(this.mWireState, this.mDismissReason);
        }
        this.mHandler.removeCallbacks(this.mDismissRunnable);
    }

    public void onDismissAnimationDismiss() {
        IChargeAnimationListener iChargeAnimationListener = this.animationListener;
        if (iChargeAnimationListener != null) {
            iChargeAnimationListener.onChargeAnimationDismiss(this.mWireState, this.mDismissReason);
        }
    }

    /* access modifiers changed from: protected */
    public void dismissView() {
        removeChargeView("dismiss");
    }

    /* access modifiers changed from: protected */
    public void stopChildAnimation() {
        this.mChargePercentView.stopValueAnimation();
    }

    private WindowManager.LayoutParams getWindowParam() {
        this.mWindowManager.getDefaultDisplay().getRealSize(this.mScreenSize);
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(-1, -1, 2026, 92275712, -3);
        layoutParams.windowAnimations = 0;
        layoutParams.systemUiVisibility = 4864;
        layoutParams.screenOrientation = 1;
        layoutParams.extraFlags = 32768;
        layoutParams.layoutInDisplayCutoutMode = 3;
        layoutParams.setTitle("charge_animation_view");
        return layoutParams;
    }

    private ViewGroup.LayoutParams getParentViewParams() {
        return new ViewGroup.LayoutParams(-1, -1);
    }

    public void addChargeView(String str, boolean z) {
        if (!isAttachedToWindow() && getParent() == null) {
            this.mShowChargingInNonLockscreen = z;
            try {
                Log.d("MiuiChargeAnimationView", "addToWindow: reason " + str);
                setComponentTransparent(true);
                if (this.mShowChargingInNonLockscreen) {
                    this.mWindowManager.addView(this, getWindowParam());
                } else {
                    ChargeUtils.getParentView().addView(this, getParentViewParams());
                }
            } catch (Exception e) {
                Log.d("MiuiChargeAnimationView", "addToWindow: Exception " + e);
                e.printStackTrace();
            }
        }
    }

    public void removeChargeView(String str) {
        if (isAttachedToWindow()) {
            Log.d("MiuiChargeAnimationView", "removeFromWindow: " + str);
            try {
                if (this.mShowChargingInNonLockscreen) {
                    this.mWindowManager.removeViewImmediate(this);
                } else {
                    ChargeUtils.getParentView().removeView(this);
                }
                WindowManagerGlobal.getInstance().trimMemory(20);
            } catch (Exception e) {
                Log.e("MiuiChargeAnimationView", "remove from window exception:", e);
            }
        }
    }

    public int getAnimationDuration() {
        if (!((MiuiChargeManager) Dependency.get(MiuiChargeManager.class)).isUsbCharging() || ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).getStrongAuthTracker().hasUserAuthenticatedSinceBoot()) {
            return this.mShowChargingInNonLockscreen ? 6400 : 20000;
        }
        return 5000;
    }

    public boolean dispatchKeyEvent(KeyEvent keyEvent) {
        startDismiss("dismiss_for_key_event");
        return false;
    }

    private void enableOrientation() {
        OrientationEventListenerWrapper orientationEventListenerWrapper = this.mOrientationListener;
        if (orientationEventListenerWrapper != null && orientationEventListenerWrapper.canDetectOrientation()) {
            Slog.i("MiuiChargeAnimationView", "enable orientation sensor");
            this.mOrientationListener.enable();
        }
    }

    private void disableOrientation() {
        OrientationEventListenerWrapper orientationEventListenerWrapper = this.mOrientationListener;
        if (orientationEventListenerWrapper != null && orientationEventListenerWrapper.canDetectOrientation()) {
            Slog.i("MiuiChargeAnimationView", "disable orientation sensor");
            this.mOrientationListener.disable();
        }
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        disableOrientation();
    }
}
