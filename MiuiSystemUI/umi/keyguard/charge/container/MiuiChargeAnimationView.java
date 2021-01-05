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
import android.hardware.input.InputManager;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Property;
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
import com.android.keyguard.charge.view.IChargeAnimationListener;
import com.android.keyguard.charge.view.MiuiChargePercentCountView;
import com.android.keyguard.injector.KeyguardUpdateMonitorInjector;
import com.android.systemui.C0010R$bool;
import com.android.systemui.Dependency;
import miui.maml.animation.interpolater.QuartEaseOutInterpolater;

public class MiuiChargeAnimationView extends FrameLayout {
    private IChargeAnimationListener animationListener;
    /* access modifiers changed from: private */
    public ViewGroup itemContainer;
    /* access modifiers changed from: private */
    public MiuiChargeContainerView mChargeContainerView;
    private MiuiChargeIconView mChargeIconView;
    private MiuiChargeLogoView mChargeLogoView;
    private MiuiChargePercentCountView mChargePercentView;
    private Configuration mConfiguration;
    private String mDismissReason;
    private final Runnable mDismissRunnable;
    private Handler mHandler;
    private int mIconPaddingTop;
    private boolean mIsFoldChargeVideo;
    /* access modifiers changed from: private */
    public ViewGroup mParentContainer;
    private Interpolator mQuartOutInterpolator;
    private Point mScreenSize;
    private boolean mShowChargingInNonLockscreen;
    private boolean mStartingDismissAnim;
    private Runnable mTimeoutDismissJob;
    private WindowManager mWindowManager;
    private int mWireState;

    public MiuiChargeAnimationView(Context context) {
        this(context, (AttributeSet) null);
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
        this.mTimeoutDismissJob = new Runnable() {
            public void run() {
                MiuiChargeAnimationView.this.startDismiss("dismiss_for_timeout");
            }
        };
        this.mDismissRunnable = new Runnable() {
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
        Point point = new Point();
        this.mWindowManager.getDefaultDisplay().getRealSize(point);
        if (!this.mScreenSize.equals(point.x, point.y)) {
            this.mScreenSize.set(point.x, point.y);
            updateSizeForScreenSizeChange();
            updateLayoutParamForScreenSizeChange();
        }
    }

    /* access modifiers changed from: protected */
    public void updateSizeForScreenSizeChange() {
        this.mWindowManager.getDefaultDisplay().getRealSize(this.mScreenSize);
        Point point = this.mScreenSize;
        this.mIconPaddingTop = (int) (((((float) Math.min(point.x, point.y)) * 1.0f) / 1080.0f) * 275.0f);
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
        Log.d("MiuiChargeAnimationView", "startChargeAnimation: mInitScreenOn " + z + ", clickShow=" + z2);
        setComponentTransparent(false);
        this.mWireState = ChargeUtils.sBatteryStatus.wireState;
        ValueAnimator ofInt = ValueAnimator.ofInt(new int[]{0, 1});
        ofInt.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                MiuiChargeAnimationView.this.mParentContainer.setAlpha(valueAnimator.getAnimatedFraction());
            }
        });
        ValueAnimator ofInt2 = ValueAnimator.ofInt(new int[]{0, 1});
        ofInt2.setStartDelay((long) ChargeUtils.getWaveTextDelayTime());
        ofInt2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                MiuiChargeAnimationView.this.itemContainer.setAlpha(valueAnimator.getAnimatedFraction());
            }
        });
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(800);
        animatorSet.setInterpolator(this.mQuartOutInterpolator);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            public void onAnimationStart(Animator animator) {
                super.onAnimationStart(animator);
                MiuiChargeAnimationView.this.onChargeAnimationStart();
            }
        });
        animatorSet.playTogether(new Animator[]{ofInt, ofInt2});
        animatorSet.start();
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
        if (str != "dismiss_for_timeout") {
            ((KeyguardUpdateMonitorInjector) Dependency.get(KeyguardUpdateMonitorInjector.class)).handleChargeAnimationShowingChanged(false);
        }
        if (!this.mStartingDismissAnim) {
            Log.i("MiuiChargeAnimationView", "startDismiss: reason: " + str);
            this.mDismissReason = str;
            this.mHandler.removeCallbacks(this.mTimeoutDismissJob);
            this.mHandler.removeCallbacks(this.mDismissRunnable);
            PropertyValuesHolder ofFloat = PropertyValuesHolder.ofFloat(property, new float[]{this.mParentContainer.getAlpha(), 0.0f});
            PropertyValuesHolder ofFloat2 = PropertyValuesHolder.ofFloat(FrameLayout.SCALE_X, new float[]{this.itemContainer.getScaleX(), 0.0f});
            PropertyValuesHolder ofFloat3 = PropertyValuesHolder.ofFloat(FrameLayout.SCALE_Y, new float[]{this.itemContainer.getScaleY(), 0.0f});
            ObjectAnimator ofPropertyValuesHolder = ObjectAnimator.ofPropertyValuesHolder(this.mParentContainer, new PropertyValuesHolder[]{ofFloat});
            PropertyValuesHolder ofFloat4 = PropertyValuesHolder.ofFloat(property, new float[]{this.itemContainer.getAlpha(), 0.0f});
            ObjectAnimator ofPropertyValuesHolder2 = ObjectAnimator.ofPropertyValuesHolder(this.itemContainer, new PropertyValuesHolder[]{ofFloat2, ofFloat3, ofFloat4});
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.setDuration(600);
            animatorSet.setInterpolator(this.mQuartOutInterpolator);
            animatorSet.addListener(new Animator.AnimatorListener() {
                public void onAnimationRepeat(Animator animator) {
                }

                public void onAnimationStart(Animator animator) {
                    MiuiChargeAnimationView.this.mChargeContainerView.startDismiss(str);
                }

                public void onAnimationEnd(Animator animator) {
                    MiuiChargeAnimationView.this.onDismissAnimationEnd();
                    MiuiChargeAnimationView.this.itemContainer.setScaleX(1.0f);
                    MiuiChargeAnimationView.this.itemContainer.setScaleY(1.0f);
                    MiuiChargeAnimationView.this.itemContainer.setAlpha(0.0f);
                }

                public void onAnimationCancel(Animator animator) {
                    MiuiChargeAnimationView.this.onDismissAnimationCancel();
                }
            });
            animatorSet.playTogether(new Animator[]{ofPropertyValuesHolder, ofPropertyValuesHolder2});
            animatorSet.start();
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

    public void addChargeView(String str, boolean z) {
        if (!isAttachedToWindow() && getParent() == null) {
            this.mShowChargingInNonLockscreen = z;
            try {
                Log.d("MiuiChargeAnimationView", "addToWindow: reason " + str);
                setComponentTransparent(true);
                if (this.mShowChargingInNonLockscreen) {
                    this.mWindowManager.addView(this, getWindowParam());
                } else {
                    ChargeUtils.getParentView().addView(this);
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
        InputManager.getInstance().injectInputEvent(keyEvent, 0);
        return false;
    }
}
