package com.android.keyguard.faceunlock;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.biometrics.BiometricSourceType;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.android.keyguard.Ease$Sine;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.MiuiKeyguardUpdateMonitorCallback;
import com.android.keyguard.faceunlock.MiuiKeyguardFaceUnlockView;
import com.android.keyguard.injector.KeyguardUpdateMonitorInjector;
import com.android.keyguard.utils.MiuiKeyguardUtils;
import com.android.keyguard.wallpaper.IMiuiKeyguardWallpaperController;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.Dependency;
import com.android.systemui.keyguard.WakefulnessLifecycle;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.miui.systemui.DeviceConfig;
import com.miui.systemui.util.HapticFeedBackImpl;
import com.miui.systemui.util.MiuiAnimationUtils;

public class MiuiKeyguardFaceUnlockView extends LinearLayout {
    private Handler mAnimationHandler;
    private Context mContext;
    private final Runnable mDelayedHide;
    private boolean mFaceUnlockAnimationRuning;
    private View.OnClickListener mFaceUnlockClickListener;
    private MiuiFaceUnlockManager mFaceUnlockManager;
    private boolean mIsKeyguardFaceUnlockView;
    private MiuiKeyguardUpdateMonitorCallback mKeyguardUpdateMonitorCallback;
    private boolean mLightClock;
    private boolean mLockScreenMagazinePreViewVisibility;
    private final PowerManager mPowerManager;
    public StatusBarStateController.StateListener mStatusBarStateListener;
    private KeyguardUpdateMonitor mUpdateMonitor;
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
                if (biometricSourceType != BiometricSourceType.FACE) {
                    return;
                }
                if (i == 9) {
                    MiuiKeyguardFaceUnlockView.this.stopShakeHeadAnimation();
                    MiuiKeyguardFaceUnlockView.this.updateFaceUnlockIconStatus();
                } else if (i == 10002) {
                    MiuiKeyguardFaceUnlockView.this.stopShakeHeadAnimation();
                    MiuiKeyguardFaceUnlockView.this.updateFaceUnlockIconStatus();
                }
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
            /* class com.android.keyguard.faceunlock.MiuiKeyguardFaceUnlockView.AnonymousClass4 */

            public void run() {
                MiuiKeyguardFaceUnlockView.this.mFaceUnlockAnimationRuning = false;
            }
        };
        this.mStatusBarStateListener = new StatusBarStateController.StateListener() {
            /* class com.android.keyguard.faceunlock.MiuiKeyguardFaceUnlockView.AnonymousClass5 */

            @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
            public void onStateChanged(int i) {
                MiuiKeyguardFaceUnlockView.this.updateFaceUnlockIconStatus();
            }
        };
        this.mContext = context;
        this.mUpdateMonitor = (KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class);
        this.mFaceUnlockManager = (MiuiFaceUnlockManager) Dependency.get(MiuiFaceUnlockManager.class);
        this.mPowerManager = (PowerManager) this.mContext.getSystemService("power");
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
        if (!this.mFaceUnlockAnimationRuning) {
            this.mFaceUnlockAnimationRuning = true;
            AnimationDrawable animationDrawable = new AnimationDrawable();
            for (int i = 1; i <= 30; i++) {
                String str = (this.mUpdateMonitor.isBouncerShowing() || !this.mLightClock) ? "face_unlock_error" : "face_unlock_black_error";
                animationDrawable.addFrame(getResources().getDrawable(this.mContext.getResources().getIdentifier(str + i, "drawable", this.mContext.getPackageName())), 16);
            }
            setBackground(animationDrawable);
            animationDrawable.setOneShot(true);
            animationDrawable.start();
            this.mAnimationHandler.postDelayed(this.mDelayedHide, 1480);
        }
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
        AnimationDrawable animationDrawable = new AnimationDrawable();
        for (int i = 1; i <= 20; i++) {
            String str = (this.mUpdateMonitor.isBouncerShowing() || !this.mLightClock) ? "face_unlock_success" : "face_unlock_black_success";
            animationDrawable.addFrame(getResources().getDrawable(this.mContext.getResources().getIdentifier(str + i, "drawable", this.mContext.getPackageName())), 16);
        }
        animationDrawable.setOneShot(true);
        setBackground(animationDrawable);
        animationDrawable.start();
    }

    public void setVisibility(int i) {
        if (i == 0 && getVisibility() != i && this.mWaitWakeupAimation) {
            startAnimation(MiuiAnimationUtils.INSTANCE.generalWakeupScaleAnimation());
            this.mWaitWakeupAimation = false;
        }
        super.setVisibility(i);
    }

    public void updateFaceUnlockIconStatus() {
        if (MiuiFaceUnlockUtils.isHardwareDetected(this.mContext)) {
            if (!shouldFaceUnlockViewExecuteAnimation() || !shouldShowFaceUnlockImage()) {
                setVisibility(4);
            } else {
                setVisibility(0);
            }
            boolean isFaceUnlock = ((KeyguardUpdateMonitorInjector) Dependency.get(KeyguardUpdateMonitorInjector.class)).isFaceUnlock();
            if (this.mUpdateMonitor.isBouncerShowing() || !this.mLightClock) {
                setBackground(getResources().getDrawable(isFaceUnlock ? C0013R$drawable.face_unlock_success20 : C0013R$drawable.face_unlock_error1));
            } else {
                setBackground(getResources().getDrawable(isFaceUnlock ? C0013R$drawable.face_unlock_black_success20 : C0013R$drawable.face_unlock_black_error1));
            }
        }
    }

    private boolean shouldShowFaceUnlockImage() {
        boolean z = this.mFaceUnlockManager.isFaceAuthEnabled() && !this.mUpdateMonitor.userNeedsStrongAuth() && ((KeyguardUpdateMonitorInjector) Dependency.get(KeyguardUpdateMonitorInjector.class)).isKeyguardShowing() && !this.mFaceUnlockManager.isFaceTemporarilyLockout() && !this.mUpdateMonitor.isSimPinSecure() && !this.mFaceUnlockManager.isDisableLockScreenFaceUnlockAnim();
        boolean isKeyguardOccluded = ((KeyguardUpdateMonitorInjector) Dependency.get(KeyguardUpdateMonitorInjector.class)).isKeyguardOccluded();
        if (this.mUpdateMonitor.isBouncerShowing()) {
            if (z) {
                return !isKeyguardOccluded || !MiuiKeyguardUtils.isTopActivityCameraApp(this.mContext);
            }
            return false;
        } else if (!z || isKeyguardOccluded || MiuiFaceUnlockUtils.isSupportLiftingCamera(this.mContext)) {
            return false;
        } else {
            return (this.mUpdateMonitor.isFaceDetectionRunning() || ((KeyguardUpdateMonitorInjector) Dependency.get(KeyguardUpdateMonitorInjector.class)).isFaceUnlock()) && !this.mLockScreenMagazinePreViewVisibility && this.mUpdateMonitor.getStatusBarState() == 1;
        }
    }
}
