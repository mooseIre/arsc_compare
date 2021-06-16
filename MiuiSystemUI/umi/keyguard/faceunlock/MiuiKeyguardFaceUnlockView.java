package com.android.keyguard.faceunlock;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.hardware.biometrics.BiometricSourceType;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.vectordrawable.graphics.drawable.Animatable2Compat$AnimationCallback;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;
import com.android.keyguard.Ease$Sine;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.MiuiKeyguardUpdateMonitorCallback;
import com.android.keyguard.faceunlock.MiuiKeyguardFaceUnlockView;
import com.android.keyguard.injector.KeyguardUpdateMonitorInjector;
import com.android.keyguard.wallpaper.IMiuiKeyguardWallpaperController;
import com.android.systemui.C0007R$animator;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.Dependency;
import com.android.systemui.keyguard.WakefulnessLifecycle;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.miui.systemui.DeviceConfig;
import com.miui.systemui.util.HapticFeedBackImpl;
import com.miui.systemui.util.MiuiAnimationUtils;

public class MiuiKeyguardFaceUnlockView extends ConstraintLayout {
    private final Handler mAnimationHandler;
    private final Context mContext;
    private final Runnable mDelayedHide;
    private ImageView mFaceIV;
    private AnimatorSet mFaceUnlockAnimation;
    private boolean mFaceUnlockAnimationRuning;
    private final View.OnClickListener mFaceUnlockClickListener;
    private final MiuiFaceUnlockManager mFaceUnlockManager;
    private boolean mIsKeyguardFaceUnlockView;
    private final MiuiKeyguardUpdateMonitorCallback mKeyguardUpdateMonitorCallback;
    private boolean mLightClock;
    private boolean mLockScreenMagazinePreViewVisibility;
    private final PowerManager mPowerManager;
    private ImageView mRingIV;
    public StatusBarStateController.StateListener mStatusBarStateListener;
    private boolean mSuccessAniRunning;
    private final Animatable2Compat$AnimationCallback mSuccessAnimationListener;
    private AnimatedVectorDrawableCompat mSuccessfulAnimation;
    private final KeyguardUpdateMonitor mUpdateMonitor;
    private boolean mWaitWakeupAimation;
    protected final WakefulnessLifecycle.Observer mWakefulnessObserver;
    private final IMiuiKeyguardWallpaperController.IWallpaperChangeCallback mWallpaperChangeCallback;

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$0 */
    public /* synthetic */ void lambda$new$0$MiuiKeyguardFaceUnlockView(boolean z) {
        this.mLightClock = z;
        updateFaceUnlockIconStatus();
    }

    public MiuiKeyguardFaceUnlockView(Context context) {
        this(context, null);
    }

    public MiuiKeyguardFaceUnlockView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mAnimationHandler = new Handler();
        this.mLightClock = false;
        this.mSuccessAniRunning = false;
        this.mWakefulnessObserver = new WakefulnessLifecycle.Observer() {
            /* class com.android.keyguard.faceunlock.MiuiKeyguardFaceUnlockView.AnonymousClass1 */

            @Override // com.android.systemui.keyguard.WakefulnessLifecycle.Observer
            public void onStartedWakingUp() {
                if (MiuiKeyguardFaceUnlockView.this.getVisibility() == 0) {
                    MiuiKeyguardFaceUnlockView.this.mWaitWakeupAimation = false;
                    MiuiKeyguardFaceUnlockView.this.startAnimation(MiuiAnimationUtils.INSTANCE.generalWakeupScaleAnimation());
                    return;
                }
                MiuiKeyguardFaceUnlockView.this.mWaitWakeupAimation = true;
                MiuiKeyguardFaceUnlockView.this.mAnimationHandler.postDelayed(new Runnable() {
                    /* class com.android.keyguard.faceunlock.$$Lambda$MiuiKeyguardFaceUnlockView$1$FkutTao3E51GbzhxS5jcPRULBYE */

                    public final void run() {
                        MiuiKeyguardFaceUnlockView.AnonymousClass1.this.lambda$onStartedWakingUp$0$MiuiKeyguardFaceUnlockView$1();
                    }
                }, 200);
            }

            /* access modifiers changed from: private */
            /* renamed from: lambda$onStartedWakingUp$0 */
            public /* synthetic */ void lambda$onStartedWakingUp$0$MiuiKeyguardFaceUnlockView$1() {
                MiuiKeyguardFaceUnlockView.this.mWaitWakeupAimation = false;
            }

            @Override // com.android.systemui.keyguard.WakefulnessLifecycle.Observer
            public void onFinishedGoingToSleep() {
                MiuiKeyguardFaceUnlockView.this.updateFaceUnlockIconStatus();
            }
        };
        this.mKeyguardUpdateMonitorCallback = new MiuiKeyguardUpdateMonitorCallback() {
            /* class com.android.keyguard.faceunlock.MiuiKeyguardFaceUnlockView.AnonymousClass2 */

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onBiometricHelp(int i, String str, BiometricSourceType biometricSourceType) {
                if (biometricSourceType != BiometricSourceType.FACE) {
                    return;
                }
                if (i == 10001) {
                    MiuiKeyguardFaceUnlockView.this.updateFaceUnlockIconStatus();
                } else if (MiuiKeyguardFaceUnlockView.this.shouldFaceUnlockViewExecuteAnimation()) {
                    MiuiKeyguardFaceUnlockView.this.startFaceUnlockAnimation();
                }
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onBiometricAuthenticated(int i, BiometricSourceType biometricSourceType, boolean z) {
                if (MiuiKeyguardFaceUnlockView.this.shouldFaceUnlockViewExecuteAnimation() && MiuiKeyguardFaceUnlockView.this.mFaceUnlockManager.isStayScreenWhenFaceUnlockSuccess() && !MiuiKeyguardFaceUnlockView.this.mUpdateMonitor.isBouncerShowing() && biometricSourceType == BiometricSourceType.FACE) {
                    ((HapticFeedBackImpl) Dependency.get(HapticFeedBackImpl.class)).getHapticFeedbackUtil().performHapticFeedback("mesh_light", false);
                    MiuiKeyguardFaceUnlockView.this.startFaceUnlockSuccessAnimation();
                }
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onBiometricAuthFailed(BiometricSourceType biometricSourceType) {
                MiuiKeyguardFaceUnlockView.this.stopShakeHeadAnimation();
                MiuiKeyguardFaceUnlockView.this.updateFaceUnlockIconStatus();
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onBiometricError(int i, String str, BiometricSourceType biometricSourceType) {
                MiuiKeyguardFaceUnlockView.this.stopShakeHeadAnimation();
                MiuiKeyguardFaceUnlockView.this.updateFaceUnlockIconStatus();
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onKeyguardBouncerChanged(boolean z) {
                MiuiKeyguardFaceUnlockView.this.updateFaceUnlockIconStatus();
            }

            @Override // com.android.keyguard.MiuiKeyguardUpdateMonitorCallback
            public void onLockScreenMagazinePreViewVisibilityChanged(boolean z) {
                MiuiKeyguardFaceUnlockView.this.mLockScreenMagazinePreViewVisibility = z;
                MiuiKeyguardFaceUnlockView.this.updateFaceUnlockIconStatus();
                if (z) {
                    MiuiKeyguardFaceUnlockView.this.mUpdateMonitor.cancelFaceAuth();
                }
            }
        };
        this.mWallpaperChangeCallback = new IMiuiKeyguardWallpaperController.IWallpaperChangeCallback() {
            /* class com.android.keyguard.faceunlock.$$Lambda$MiuiKeyguardFaceUnlockView$PKkkbV34Hst5MW8ZhqXILmcqlTE */

            @Override // com.android.keyguard.wallpaper.IMiuiKeyguardWallpaperController.IWallpaperChangeCallback
            public final void onWallpaperChange(boolean z) {
                MiuiKeyguardFaceUnlockView.this.lambda$new$0$MiuiKeyguardFaceUnlockView(z);
            }
        };
        this.mFaceUnlockClickListener = new View.OnClickListener() {
            /* class com.android.keyguard.faceunlock.MiuiKeyguardFaceUnlockView.AnonymousClass3 */

            public void onClick(View view) {
                if (!((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).isFaceDetectionRunning()) {
                    ObjectAnimator ofFloat = ObjectAnimator.ofFloat(MiuiKeyguardFaceUnlockView.this, "scaleX", 1.0f, 1.2f, 0.9f, 1.0f);
                    ObjectAnimator ofFloat2 = ObjectAnimator.ofFloat(MiuiKeyguardFaceUnlockView.this, "scaleY", 1.0f, 1.2f, 0.9f, 1.0f);
                    AnimatorSet animatorSet = new AnimatorSet();
                    animatorSet.setInterpolator(Ease$Sine.easeInOut);
                    animatorSet.setDuration(400L);
                    animatorSet.playTogether(ofFloat, ofFloat2);
                    animatorSet.addListener(new AnimatorListenerAdapter() {
                        /* class com.android.keyguard.faceunlock.MiuiKeyguardFaceUnlockView.AnonymousClass3.AnonymousClass1 */

                        public void onAnimationEnd(Animator animator) {
                            MiuiKeyguardFaceUnlockView.this.mUpdateMonitor.requestFaceAuth(2);
                            MiuiKeyguardFaceUnlockView.this.mPowerManager.userActivity(SystemClock.uptimeMillis(), false);
                        }
                    });
                    animatorSet.start();
                }
            }
        };
        this.mDelayedHide = new Runnable() {
            /* class com.android.keyguard.faceunlock.MiuiKeyguardFaceUnlockView.AnonymousClass5 */

            public void run() {
                MiuiKeyguardFaceUnlockView.this.mFaceUnlockAnimationRuning = false;
            }
        };
        this.mSuccessAnimationListener = new Animatable2Compat$AnimationCallback() {
            /* class com.android.keyguard.faceunlock.MiuiKeyguardFaceUnlockView.AnonymousClass6 */

            @Override // androidx.vectordrawable.graphics.drawable.Animatable2Compat$AnimationCallback
            public void onAnimationStart(Drawable drawable) {
                super.onAnimationStart(drawable);
                MiuiKeyguardFaceUnlockView.this.mSuccessAniRunning = true;
            }

            @Override // androidx.vectordrawable.graphics.drawable.Animatable2Compat$AnimationCallback
            public void onAnimationEnd(Drawable drawable) {
                super.onAnimationEnd(drawable);
                MiuiKeyguardFaceUnlockView.this.mSuccessAniRunning = false;
            }
        };
        this.mStatusBarStateListener = new StatusBarStateController.StateListener() {
            /* class com.android.keyguard.faceunlock.MiuiKeyguardFaceUnlockView.AnonymousClass7 */

            @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
            public void onStateChanged(int i) {
                MiuiKeyguardFaceUnlockView.this.updateFaceUnlockIconStatus();
            }
        };
        this.mContext = context;
        this.mUpdateMonitor = (KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class);
        this.mFaceUnlockManager = (MiuiFaceUnlockManager) Dependency.get(MiuiFaceUnlockManager.class);
        this.mPowerManager = (PowerManager) this.mContext.getSystemService("power");
        initView();
    }

    private void initView() {
        ImageView imageView = new ImageView(getContext());
        this.mFaceIV = imageView;
        imageView.setImageResource(C0013R$drawable.face_unlock_face);
        int id = getId();
        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(-2, -2);
        layoutParams.startToStart = id;
        layoutParams.endToEnd = id;
        layoutParams.topToTop = id;
        layoutParams.bottomToBottom = id;
        addView(this.mFaceIV, layoutParams);
        ImageView imageView2 = new ImageView(getContext());
        this.mRingIV = imageView2;
        imageView2.setAlpha(0.0f);
        this.mRingIV.setImageResource(C0013R$drawable.face_unlock_ring);
        addView(this.mRingIV, layoutParams);
        int dimensionPixelOffset = this.mContext.getResources().getDimensionPixelOffset(C0012R$dimen.miui_face_unlock_view_padding);
        setPadding(dimensionPixelOffset, 0, dimensionPixelOffset, 0);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        setOnClickListener(this.mFaceUnlockClickListener);
        updateFaceUnlockViewForNotch();
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mUpdateMonitor.registerCallback(this.mKeyguardUpdateMonitorCallback);
        this.mFaceUnlockManager.addFaceUnlockView(this);
        ((WakefulnessLifecycle) Dependency.get(WakefulnessLifecycle.class)).addObserver(this.mWakefulnessObserver);
        ((IMiuiKeyguardWallpaperController) Dependency.get(IMiuiKeyguardWallpaperController.class)).registerWallpaperChangeCallback(this.mWallpaperChangeCallback);
        ((StatusBarStateController) Dependency.get(StatusBarStateController.class)).addCallback(this.mStatusBarStateListener);
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mUpdateMonitor.removeCallback(this.mKeyguardUpdateMonitorCallback);
        this.mFaceUnlockManager.removeFaceUnlockView(this);
        AnimatedVectorDrawableCompat animatedVectorDrawableCompat = this.mSuccessfulAnimation;
        if (animatedVectorDrawableCompat != null) {
            animatedVectorDrawableCompat.unregisterAnimationCallback(this.mSuccessAnimationListener);
        }
        ((WakefulnessLifecycle) Dependency.get(WakefulnessLifecycle.class)).removeObserver(this.mWakefulnessObserver);
        ((IMiuiKeyguardWallpaperController) Dependency.get(IMiuiKeyguardWallpaperController.class)).unregisterWallpaperChangeCallback(this.mWallpaperChangeCallback);
        ((StatusBarStateController) Dependency.get(StatusBarStateController.class)).removeCallback(this.mStatusBarStateListener);
    }

    private void updateFaceUnlockViewForNotch() {
        int i;
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) getLayoutParams();
        Resources resources = this.mContext.getResources();
        if (DeviceConfig.IS_NOTCH) {
            i = C0012R$dimen.miui_face_unlock_view_notch_top;
        } else {
            i = C0012R$dimen.miui_face_unlock_view_top;
        }
        marginLayoutParams.topMargin = resources.getDimensionPixelSize(i);
        setLayoutParams(marginLayoutParams);
    }

    public void setKeyguardFaceUnlockView(boolean z) {
        this.mIsKeyguardFaceUnlockView = z;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean shouldFaceUnlockViewExecuteAnimation() {
        return (!this.mUpdateMonitor.isBouncerShowing() && this.mIsKeyguardFaceUnlockView) || (this.mUpdateMonitor.isBouncerShowing() && !this.mIsKeyguardFaceUnlockView);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startFaceUnlockAnimation() {
        if (shouldFaceUnlockViewExecuteAnimation() && !this.mFaceUnlockAnimationRuning) {
            boolean z = !this.mUpdateMonitor.isBouncerShowing() && this.mLightClock;
            this.mFaceIV.setImageResource(z ? C0013R$drawable.face_unlock_face_black : C0013R$drawable.face_unlock_face);
            this.mRingIV.setImageResource(z ? C0013R$drawable.face_unlock_ring_black : C0013R$drawable.face_unlock_ring);
            this.mFaceUnlockAnimationRuning = true;
            AnimatorSet faceUnlockAnimation = getFaceUnlockAnimation();
            this.mFaceUnlockAnimation = faceUnlockAnimation;
            faceUnlockAnimation.addListener(new AnimatorListenerAdapter() {
                /* class com.android.keyguard.faceunlock.MiuiKeyguardFaceUnlockView.AnonymousClass4 */

                public void onAnimationStart(Animator animator) {
                    super.onAnimationStart(animator);
                    MiuiKeyguardFaceUnlockView.this.mRingIV.setAlpha(1.0f);
                }
            });
            this.mFaceUnlockAnimation.start();
            this.mAnimationHandler.postDelayed(this.mDelayedHide, 1480);
        }
    }

    private AnimatorSet getFaceUnlockAnimation() {
        AnimatorSet animatorSet = (AnimatorSet) AnimatorInflater.loadAnimator(getContext(), C0007R$animator.keyguard_face_unlock_error_face_rotate);
        AnimatorSet animatorSet2 = (AnimatorSet) AnimatorInflater.loadAnimator(getContext(), C0007R$animator.keyguard_face_unlock_error_ring_rotate);
        animatorSet.setTarget(this.mFaceIV);
        animatorSet2.setTarget(this.mRingIV);
        AnimatorSet animatorSet3 = new AnimatorSet();
        animatorSet3.playTogether(animatorSet, animatorSet2);
        return animatorSet3;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void stopShakeHeadAnimation() {
        this.mAnimationHandler.removeCallbacks(this.mDelayedHide);
        this.mFaceUnlockAnimationRuning = false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startFaceUnlockSuccessAnimation() {
        int i;
        AnimatorSet animatorSet = this.mFaceUnlockAnimation;
        if (animatorSet != null && animatorSet.isRunning()) {
            this.mFaceUnlockAnimation.cancel();
        }
        this.mRingIV.setAlpha(0.0f);
        this.mRingIV.setRotationY(0.0f);
        this.mRingIV.setTranslationX(0.0f);
        this.mFaceIV.setRotationY(0.0f);
        this.mFaceIV.setTranslationX(0.0f);
        if (this.mUpdateMonitor.isBouncerShowing() || !this.mLightClock) {
            i = C0013R$drawable.face_unlock_success_ani;
        } else {
            i = C0013R$drawable.face_unlock_black_success_ani;
        }
        AnimatedVectorDrawableCompat create = AnimatedVectorDrawableCompat.create(getContext(), i);
        this.mSuccessfulAnimation = create;
        create.registerAnimationCallback(this.mSuccessAnimationListener);
        this.mFaceIV.setImageDrawable(this.mSuccessfulAnimation);
        this.mSuccessfulAnimation.start();
    }

    public void setVisibility(int i) {
        if (i == 0 && getVisibility() != i && this.mWaitWakeupAimation) {
            startAnimation(MiuiAnimationUtils.INSTANCE.generalWakeupScaleAnimation());
            this.mWaitWakeupAimation = false;
        }
        super.setVisibility(i);
    }

    public void updateFaceUnlockIconStatus() {
        int i;
        int i2;
        if (MiuiFaceUnlockUtils.isHardwareDetected(this.mContext)) {
            if (!shouldFaceUnlockViewExecuteAnimation() || !this.mFaceUnlockManager.shouldShowFaceUnlockRetryMessageInBouncer()) {
                clearAnimation();
                setVisibility(4);
            } else {
                setVisibility(0);
            }
            if (!this.mSuccessAniRunning) {
                this.mRingIV.setAlpha(0.0f);
                boolean isFaceUnlock = ((KeyguardUpdateMonitorInjector) Dependency.get(KeyguardUpdateMonitorInjector.class)).isFaceUnlock();
                if (this.mUpdateMonitor.isBouncerShowing() || !this.mLightClock) {
                    ImageView imageView = this.mFaceIV;
                    Resources resources = getResources();
                    if (isFaceUnlock) {
                        i = C0013R$drawable.face_unlock_success;
                    } else {
                        i = C0013R$drawable.face_unlock_error;
                    }
                    imageView.setImageDrawable(resources.getDrawable(i));
                    return;
                }
                ImageView imageView2 = this.mFaceIV;
                Resources resources2 = getResources();
                if (isFaceUnlock) {
                    i2 = C0013R$drawable.face_unlock_black_success;
                } else {
                    i2 = C0013R$drawable.face_unlock_black_error;
                }
                imageView2.setImageDrawable(resources2.getDrawable(i2));
            }
        }
    }
}
