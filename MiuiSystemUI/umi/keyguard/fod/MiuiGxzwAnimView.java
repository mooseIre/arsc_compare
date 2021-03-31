package com.android.keyguard.fod;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.display.DisplayManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Vibrator;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.keyguard.MiuiFastUnlockController;
import com.android.keyguard.fod.MiuiGxzwAnimManager;
import com.android.keyguard.fod.MiuiGxzwAnimView;
import com.android.keyguard.fod.MiuiGxzwFrameAnimation;
import com.android.keyguard.utils.MiuiKeyguardUtils;
import com.android.keyguard.wallpaper.IMiuiKeyguardWallpaperController;
import com.android.keyguard.wallpaper.MiuiKeyguardWallpaperControllerImpl;
import com.android.systemui.C0021R$string;
import com.android.systemui.Dependency;
import com.miui.systemui.util.HapticFeedBackImpl;
import java.util.Objects;
import miui.util.HapticFeedbackUtil;
import miui.view.animation.QuarticEaseOutInterpolator;
import miuix.animation.Folme;
import miuix.animation.IVisibleStyle;

/* access modifiers changed from: package-private */
public class MiuiGxzwAnimView {
    private final Handler mMainHandler;
    private MiuiGxzwAnimViewInternal mMiuiGxzwAnimView;

    public MiuiGxzwAnimView(Context context) {
        Handler handler = new Handler();
        HandlerThread handlerThread = new HandlerThread("MiuiGxzwAnimView");
        handlerThread.start();
        Handler handler2 = new Handler(handlerThread.getLooper());
        this.mMainHandler = handler2;
        handler2.post(new Runnable(context, handler) {
            /* class com.android.keyguard.fod.$$Lambda$MiuiGxzwAnimView$aXeisBCHTDNR6SyEX0m0n3vkBA */
            public final /* synthetic */ Context f$1;
            public final /* synthetic */ Handler f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                MiuiGxzwAnimView.this.lambda$new$0$MiuiGxzwAnimView(this.f$1, this.f$2);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$0 */
    public /* synthetic */ void lambda$new$0$MiuiGxzwAnimView(Context context, Handler handler) {
        this.mMiuiGxzwAnimView = new MiuiGxzwAnimViewInternal(context, handler);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$show$1 */
    public /* synthetic */ void lambda$show$1$MiuiGxzwAnimView(boolean z) {
        this.mMiuiGxzwAnimView.show(z);
    }

    public void show(boolean z) {
        this.mMainHandler.post(new Runnable(z) {
            /* class com.android.keyguard.fod.$$Lambda$MiuiGxzwAnimView$HUEAJWeZmzUSq9L_AHPdXyZu3Go */
            public final /* synthetic */ boolean f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                MiuiGxzwAnimView.this.lambda$show$1$MiuiGxzwAnimView(this.f$1);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$dismiss$2 */
    public /* synthetic */ void lambda$dismiss$2$MiuiGxzwAnimView(boolean z) {
        this.mMiuiGxzwAnimView.dismiss(z);
    }

    public void dismiss(boolean z) {
        this.mMainHandler.post(new Runnable(z) {
            /* class com.android.keyguard.fod.$$Lambda$MiuiGxzwAnimView$JVRWBxPvEKEnqYbttiZwG97Fg */
            public final /* synthetic */ boolean f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                MiuiGxzwAnimView.this.lambda$dismiss$2$MiuiGxzwAnimView(this.f$1);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$startDozing$3 */
    public /* synthetic */ void lambda$startDozing$3$MiuiGxzwAnimView() {
        this.mMiuiGxzwAnimView.startDozing();
    }

    public void startDozing() {
        this.mMainHandler.post(new Runnable() {
            /* class com.android.keyguard.fod.$$Lambda$MiuiGxzwAnimView$tJD_rEuQUfx59aJ77anRHyb6pRA */

            public final void run() {
                MiuiGxzwAnimView.this.lambda$startDozing$3$MiuiGxzwAnimView();
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$stopDozing$4 */
    public /* synthetic */ void lambda$stopDozing$4$MiuiGxzwAnimView() {
        this.mMiuiGxzwAnimView.stopDozing();
    }

    public void stopDozing() {
        this.mMainHandler.post(new Runnable() {
            /* class com.android.keyguard.fod.$$Lambda$MiuiGxzwAnimView$S8kQNrx8YwPpTIUH_JqMj26N2eg */

            public final void run() {
                MiuiGxzwAnimView.this.lambda$stopDozing$4$MiuiGxzwAnimView();
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$setEnrolling$5 */
    public /* synthetic */ void lambda$setEnrolling$5$MiuiGxzwAnimView(boolean z) {
        this.mMiuiGxzwAnimView.setEnrolling(z);
    }

    public void setEnrolling(boolean z) {
        this.mMainHandler.post(new Runnable(z) {
            /* class com.android.keyguard.fod.$$Lambda$MiuiGxzwAnimView$SFuYwLOLMqIVpFmVLeJSQ4nFlI */
            public final /* synthetic */ boolean f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                MiuiGxzwAnimView.this.lambda$setEnrolling$5$MiuiGxzwAnimView(this.f$1);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$startRecognizingAnim$6 */
    public /* synthetic */ void lambda$startRecognizingAnim$6$MiuiGxzwAnimView() {
        this.mMiuiGxzwAnimView.startRecognizingAnim();
    }

    public void startRecognizingAnim() {
        this.mMainHandler.post(new Runnable() {
            /* class com.android.keyguard.fod.$$Lambda$MiuiGxzwAnimView$ZcQ9f_vjcN4bL6kBNGwJPJaKMQ */

            public final void run() {
                MiuiGxzwAnimView.this.lambda$startRecognizingAnim$6$MiuiGxzwAnimView();
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$startFalseAnim$7 */
    public /* synthetic */ void lambda$startFalseAnim$7$MiuiGxzwAnimView() {
        this.mMiuiGxzwAnimView.startFalseAnim();
    }

    public void startFalseAnim() {
        this.mMainHandler.post(new Runnable() {
            /* class com.android.keyguard.fod.$$Lambda$MiuiGxzwAnimView$SkuochNBnrhitPblnPijHIJjxXU */

            public final void run() {
                MiuiGxzwAnimView.this.lambda$startFalseAnim$7$MiuiGxzwAnimView();
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$startBackAnim$8 */
    public /* synthetic */ void lambda$startBackAnim$8$MiuiGxzwAnimView() {
        this.mMiuiGxzwAnimView.startBackAnim();
    }

    public void startBackAnim() {
        this.mMainHandler.post(new Runnable() {
            /* class com.android.keyguard.fod.$$Lambda$MiuiGxzwAnimView$mBsIS_TiHc3rMGkDHJMzIKQgNE */

            public final void run() {
                MiuiGxzwAnimView.this.lambda$startBackAnim$8$MiuiGxzwAnimView();
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$stopAnim$9 */
    public /* synthetic */ void lambda$stopAnim$9$MiuiGxzwAnimView() {
        this.mMiuiGxzwAnimView.stopAnim();
    }

    public void stopAnim() {
        this.mMainHandler.post(new Runnable() {
            /* class com.android.keyguard.fod.$$Lambda$MiuiGxzwAnimView$Mlpj5MhpuKcJHHqlivv86LEM2s4 */

            public final void run() {
                MiuiGxzwAnimView.this.lambda$stopAnim$9$MiuiGxzwAnimView();
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$stopTip$10 */
    public /* synthetic */ void lambda$stopTip$10$MiuiGxzwAnimView() {
        this.mMiuiGxzwAnimView.stopTip();
    }

    public void stopTip() {
        this.mMainHandler.post(new Runnable() {
            /* class com.android.keyguard.fod.$$Lambda$MiuiGxzwAnimView$xCvwrdPhr5YGYfKw8mOmhluc4e8 */

            public final void run() {
                MiuiGxzwAnimView.this.lambda$stopTip$10$MiuiGxzwAnimView();
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$drawFingerprintIcon$11 */
    public /* synthetic */ void lambda$drawFingerprintIcon$11$MiuiGxzwAnimView(boolean z) {
        this.mMiuiGxzwAnimView.drawFingerprintIcon(z);
    }

    public void drawFingerprintIcon(boolean z) {
        this.mMainHandler.post(new Runnable(z) {
            /* class com.android.keyguard.fod.$$Lambda$MiuiGxzwAnimView$8GbkLKmOyr0q5Azi7nKABRDhL8 */
            public final /* synthetic */ boolean f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                MiuiGxzwAnimView.this.lambda$drawFingerprintIcon$11$MiuiGxzwAnimView(this.f$1);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$setTranslate$12 */
    public /* synthetic */ void lambda$setTranslate$12$MiuiGxzwAnimView(int i, int i2) {
        this.mMiuiGxzwAnimView.setTranslate(i, i2);
    }

    public void setTranslate(int i, int i2) {
        this.mMainHandler.post(new Runnable(i, i2) {
            /* class com.android.keyguard.fod.$$Lambda$MiuiGxzwAnimView$HffTgF3MDzIpT9LeNDcUJVpAHM */
            public final /* synthetic */ int f$1;
            public final /* synthetic */ int f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                MiuiGxzwAnimView.this.lambda$setTranslate$12$MiuiGxzwAnimView(this.f$1, this.f$2);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$setCollecting$13 */
    public /* synthetic */ void lambda$setCollecting$13$MiuiGxzwAnimView(boolean z) {
        this.mMiuiGxzwAnimView.setCollecting(z);
    }

    public void setCollecting(boolean z) {
        this.mMainHandler.post(new Runnable(z) {
            /* class com.android.keyguard.fod.$$Lambda$MiuiGxzwAnimView$CjwijGKOCWG9IxwsD_mNImhKFm4 */
            public final /* synthetic */ boolean f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                MiuiGxzwAnimView.this.lambda$setCollecting$13$MiuiGxzwAnimView(this.f$1);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$setGxzwTransparent$14 */
    public /* synthetic */ void lambda$setGxzwTransparent$14$MiuiGxzwAnimView(boolean z) {
        this.mMiuiGxzwAnimView.setGxzwTransparent(z);
    }

    public void setGxzwTransparent(boolean z) {
        this.mMainHandler.post(new Runnable(z) {
            /* class com.android.keyguard.fod.$$Lambda$MiuiGxzwAnimView$ztsoFdNDydykbN1I2k9IHmRW2G8 */
            public final /* synthetic */ boolean f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                MiuiGxzwAnimView.this.lambda$setGxzwTransparent$14$MiuiGxzwAnimView(this.f$1);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$performSuccessFeedback$15 */
    public /* synthetic */ void lambda$performSuccessFeedback$15$MiuiGxzwAnimView() {
        this.mMiuiGxzwAnimView.performSuccessFeedback();
    }

    public void performSuccessFeedback() {
        this.mMainHandler.post(new Runnable() {
            /* class com.android.keyguard.fod.$$Lambda$MiuiGxzwAnimView$IjNSgT7oEk_TJGUv72Lctn0QVbs */

            public final void run() {
                MiuiGxzwAnimView.this.lambda$performSuccessFeedback$15$MiuiGxzwAnimView();
            }
        });
    }

    public void performFailFeedback() {
        Handler handler = this.mMainHandler;
        MiuiGxzwAnimViewInternal miuiGxzwAnimViewInternal = this.mMiuiGxzwAnimView;
        Objects.requireNonNull(miuiGxzwAnimViewInternal);
        handler.post(new Runnable() {
            /* class com.android.keyguard.fod.$$Lambda$MiuiGxzwAnimView$wVxK2Igqpk2MJf1ojfR53SJtkzk */

            public final void run() {
                MiuiGxzwAnimView.MiuiGxzwAnimViewInternal.this.performFailFeedback();
            }
        });
    }

    public void cancelAnimFeedback(Context context) {
        if (HapticFeedbackUtil.isSupportLinearMotorVibrate()) {
            Vibrator vibrator = (Vibrator) context.getSystemService("vibrator");
            if (vibrator != null) {
                vibrator.cancel();
            }
            this.mMainHandler.post(new Runnable() {
                /* class com.android.keyguard.fod.$$Lambda$MiuiGxzwAnimView$lTI8Kcn0S4V1PCKuYg_CqtsvucM */

                public final void run() {
                    MiuiGxzwAnimView.this.lambda$cancelAnimFeedback$17$MiuiGxzwAnimView();
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$cancelAnimFeedback$17 */
    public /* synthetic */ void lambda$cancelAnimFeedback$17$MiuiGxzwAnimView() {
        this.mMiuiGxzwAnimView.mAnimFeedback = false;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$disableLockScreenFodAnim$18 */
    public /* synthetic */ void lambda$disableLockScreenFodAnim$18$MiuiGxzwAnimView(boolean z) {
        this.mMiuiGxzwAnimView.disableLockScreenFodAnim(z);
    }

    public void disableLockScreenFodAnim(boolean z) {
        this.mMainHandler.post(new Runnable(z) {
            /* class com.android.keyguard.fod.$$Lambda$MiuiGxzwAnimView$1WfcVw9ngG5xvyTMiJxtrfoDvY */
            public final /* synthetic */ boolean f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                MiuiGxzwAnimView.this.lambda$disableLockScreenFodAnim$18$MiuiGxzwAnimView(this.f$1);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$setVisibility$19 */
    public /* synthetic */ void lambda$setVisibility$19$MiuiGxzwAnimView(int i) {
        this.mMiuiGxzwAnimView.setVisibility(i);
    }

    public void setVisibility(int i) {
        this.mMainHandler.post(new Runnable(i) {
            /* class com.android.keyguard.fod.$$Lambda$MiuiGxzwAnimView$JuEdjOyOa4PX7kCTwLqzcbcII */
            public final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                MiuiGxzwAnimView.this.lambda$setVisibility$19$MiuiGxzwAnimView(this.f$1);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onKeyguardAuthen$21 */
    public /* synthetic */ void lambda$onKeyguardAuthen$21$MiuiGxzwAnimView(boolean z) {
        this.mMiuiGxzwAnimView.onKeyguardAuthen(z);
    }

    public void onKeyguardAuthen(boolean z) {
        this.mMainHandler.post(new Runnable(z) {
            /* class com.android.keyguard.fod.$$Lambda$MiuiGxzwAnimView$C8szOdc6O2Gqo6zQxHKSN2hrU7I */
            public final /* synthetic */ boolean f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                MiuiGxzwAnimView.this.lambda$onKeyguardAuthen$21$MiuiGxzwAnimView(this.f$1);
            }
        });
    }

    /* access modifiers changed from: private */
    public static class MiuiGxzwAnimViewInternal extends GxzwNoRotateFrameLayout implements DisplayManager.DisplayListener, TextureView.SurfaceTextureListener {
        private float mAlpha;
        private ValueAnimator mAlphaAnimator;
        private boolean mAnimFeedback;
        private boolean mBouncer;
        private boolean mCollecting = false;
        private boolean mDisableLockScreenFodAnim = false;
        private DisplayManager mDisplayManager;
        private int mDisplayState = 2;
        private boolean mDozeScreenOn = false;
        private boolean mDozing = false;
        private boolean mDozingIconAnimDone = false;
        private boolean mGxzwTransparent = true;
        private final KeyguardUpdateMonitorCallback mKeyguardUpdateMonitorCallback;
        private WindowManager.LayoutParams mLayoutParams;
        private boolean mLightIcon = false;
        private boolean mLightWallpaperGxzw;
        private final Handler mMainHandler;
        private MiuiGxzwAnimManager mMiuiGxzwAnimManager;
        private MiuiGxzwFrameAnimation mMiuiGxzwFrameAnimation;
        private MiuiGxzwTipView mMiuiGxzwTipView;
        private Runnable mReleaseDrawWackLockRunnable;
        private Runnable mRemoveRunnable = new Runnable() {
            /* class com.android.keyguard.fod.$$Lambda$vKZhyrUnSkkNYgArv0zEHjoFg */

            public final void run() {
                MiuiGxzwAnimView.MiuiGxzwAnimViewInternal.this.removeViewFromWindow();
            }
        };
        private boolean mShouldShowBackAnim = false;
        private boolean mSurfaceCreate = false;
        private volatile MiuiGxzwFrameAnimation.SurfaceTextureState mSurfaceTextureState;
        private MiuiGxzwFrameAnimation.ISurfaceTextureStateHelper mSurfaceTextureStateHelper;
        private final Handler mSystemUIHandler;
        private TextureView mTextureView;
        private final IMiuiKeyguardWallpaperController.IWallpaperChangeCallback mWallpaperChangeCallback;

        public void onDisplayAdded(int i) {
        }

        public void onDisplayRemoved(int i) {
        }

        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i2) {
        }

        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        }

        public MiuiGxzwAnimViewInternal(Context context, Handler handler) {
            super(context);
            MiuiGxzwManager instance = MiuiGxzwManager.getInstance();
            Objects.requireNonNull(instance);
            this.mReleaseDrawWackLockRunnable = new Runnable() {
                /* class com.android.keyguard.fod.$$Lambda$6DEIs9lWsN0lnJmBY718KrIV_Jw */

                public final void run() {
                    MiuiGxzwManager.this.releaseDrawWackLock();
                }
            };
            this.mAlpha = 1.0f;
            this.mSurfaceTextureState = MiuiGxzwFrameAnimation.SurfaceTextureState.Unknown;
            this.mSurfaceTextureStateHelper = new MiuiGxzwFrameAnimation.ISurfaceTextureStateHelper() {
                /* class com.android.keyguard.fod.MiuiGxzwAnimView.MiuiGxzwAnimViewInternal.AnonymousClass1 */

                @Override // com.android.keyguard.fod.MiuiGxzwFrameAnimation.ISurfaceTextureStateHelper
                public MiuiGxzwFrameAnimation.SurfaceTextureState getState() {
                    return MiuiGxzwAnimViewInternal.this.mSurfaceTextureState;
                }
            };
            this.mKeyguardUpdateMonitorCallback = new KeyguardUpdateMonitorCallback() {
                /* class com.android.keyguard.fod.MiuiGxzwAnimView.MiuiGxzwAnimViewInternal.AnonymousClass2 */

                @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
                public void onKeyguardBouncerChanged(boolean z) {
                    MiuiGxzwAnimViewInternal.this.mMainHandler.post(new Runnable(z, ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).isDeviceInteractive()) {
                        /* class com.android.keyguard.fod.$$Lambda$MiuiGxzwAnimView$MiuiGxzwAnimViewInternal$2$wEHG0WNHKQjSUWfRobd3_5_U0 */
                        public final /* synthetic */ boolean f$1;
                        public final /* synthetic */ boolean f$2;

                        {
                            this.f$1 = r2;
                            this.f$2 = r3;
                        }

                        public final void run() {
                            MiuiGxzwAnimView.MiuiGxzwAnimViewInternal.AnonymousClass2.this.lambda$onKeyguardBouncerChanged$0$MiuiGxzwAnimView$MiuiGxzwAnimViewInternal$2(this.f$1, this.f$2);
                        }
                    });
                }

                /* access modifiers changed from: private */
                /* renamed from: lambda$onKeyguardBouncerChanged$0 */
                public /* synthetic */ void lambda$onKeyguardBouncerChanged$0$MiuiGxzwAnimView$MiuiGxzwAnimViewInternal$2(boolean z, boolean z2) {
                    MiuiGxzwAnimViewInternal.this.onKeyguardBouncerChanged(z, z2);
                }
            };
            this.mWallpaperChangeCallback = new IMiuiKeyguardWallpaperController.IWallpaperChangeCallback() {
                /* class com.android.keyguard.fod.MiuiGxzwAnimView.MiuiGxzwAnimViewInternal.AnonymousClass3 */

                /* access modifiers changed from: private */
                /* renamed from: lambda$onWallpaperChange$0 */
                public /* synthetic */ void lambda$onWallpaperChange$0$MiuiGxzwAnimView$MiuiGxzwAnimViewInternal$3(boolean z) {
                    MiuiGxzwAnimViewInternal.this.onWallpaperChange(z);
                }

                @Override // com.android.keyguard.wallpaper.IMiuiKeyguardWallpaperController.IWallpaperChangeCallback
                public void onWallpaperChange(boolean z) {
                    MiuiGxzwAnimViewInternal.this.mMainHandler.post(new Runnable(z) {
                        /* class com.android.keyguard.fod.$$Lambda$MiuiGxzwAnimView$MiuiGxzwAnimViewInternal$3$oTy3NenDOLxgllUFEnSyUU0WC5s */
                        public final /* synthetic */ boolean f$1;

                        {
                            this.f$1 = r2;
                        }

                        public final void run() {
                            MiuiGxzwAnimView.MiuiGxzwAnimViewInternal.AnonymousClass3.this.lambda$onWallpaperChange$0$MiuiGxzwAnimView$MiuiGxzwAnimViewInternal$3(this.f$1);
                        }
                    });
                }
            };
            this.mMainHandler = new Handler();
            this.mSystemUIHandler = handler;
            initView();
        }

        private void initView() {
            this.mRegion = caculateRegion();
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(-1, -1);
            TextureView textureView = new TextureView(getContext());
            this.mTextureView = textureView;
            textureView.setSurfaceTextureListener(this);
            this.mMiuiGxzwFrameAnimation = new MiuiGxzwFrameAnimation(this.mTextureView, this.mSurfaceTextureStateHelper);
            addView(this.mTextureView, layoutParams);
            this.mMiuiGxzwTipView = new MiuiGxzwTipView(getContext());
            addView(this.mMiuiGxzwTipView, new FrameLayout.LayoutParams(-1, -1));
            setSystemUiVisibility(4868);
            this.mMiuiGxzwFrameAnimation.setMode(1);
            DisplayManager displayManager = (DisplayManager) getContext().getSystemService("display");
            this.mDisplayManager = displayManager;
            displayManager.registerDisplayListener(this, this.mMainHandler);
            this.mMiuiGxzwAnimManager = new MiuiGxzwAnimManager(getContext(), this.mMiuiGxzwFrameAnimation);
            WindowManager.LayoutParams layoutParams2 = new WindowManager.LayoutParams(this.mRegion.width(), this.mRegion.height(), 2015, 16778776, -2);
            this.mLayoutParams = layoutParams2;
            layoutParams2.layoutInDisplayCutoutMode = 1;
            int i = layoutParams2.privateFlags | 16;
            layoutParams2.privateFlags = i;
            layoutParams2.privateFlags = i | MiuiGxzwUtils.PRIVATE_FLAG_IS_HBM_OVERLAY;
            layoutParams2.gravity = 51;
            Rect rect = this.mRegion;
            layoutParams2.x = rect.left;
            layoutParams2.y = rect.top;
            layoutParams2.setTitle("gxzw_anim");
        }

        @Override // com.android.keyguard.fod.GxzwNoRotateFrameLayout
        public void onKeyguardAuthen(boolean z) {
            super.onKeyguardAuthen(z);
            if (this.mShowing && this.mKeyguardAuthen) {
                drawFingerprintIcon(this.mDozing);
            }
            this.mMiuiGxzwAnimManager.onKeyguardAuthen(z);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.keyguard.fod.GxzwNoRotateFrameLayout
        public Rect caculateRegion() {
            int i = MiuiGxzwUtils.GXZW_ICON_X - ((MiuiGxzwUtils.GXZW_ANIM_WIDTH - MiuiGxzwUtils.GXZW_ICON_WIDTH) / 2);
            int i2 = MiuiGxzwUtils.GXZW_ICON_Y - ((MiuiGxzwUtils.GXZW_ANIM_HEIGHT - MiuiGxzwUtils.GXZW_ICON_HEIGHT) / 2);
            return new Rect(i, i2, MiuiGxzwUtils.GXZW_ANIM_WIDTH + i, MiuiGxzwUtils.GXZW_ANIM_HEIGHT + i2);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.keyguard.fod.GxzwWindowFrameLayout
        public WindowManager.LayoutParams generateLayoutParams() {
            return this.mLayoutParams;
        }

        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i2) {
            Log.i("MiuiGxzwAnimView", "onSurfaceTextureAvailable");
            this.mSurfaceTextureState = MiuiGxzwFrameAnimation.SurfaceTextureState.Available;
            this.mSurfaceCreate = true;
            drawFingerprintIcon(this.mDozing);
            if (!this.mKeyguardAuthen || !this.mShowing || !this.mDozing) {
                this.mShouldShowBackAnim = false;
                this.mMiuiGxzwTipView.stopTipAnim();
                return;
            }
            updateDozingIconAnim();
        }

        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            Log.i("MiuiGxzwAnimView", "onSurfaceTextureDestroyed");
            this.mSurfaceTextureState = MiuiGxzwFrameAnimation.SurfaceTextureState.Destroyed;
            this.mSurfaceCreate = false;
            this.mMiuiGxzwFrameAnimation.stopAnimation();
            return true;
        }

        public void onDisplayChanged(int i) {
            if (i == 0) {
                int state = this.mDisplayManager.getDisplay(i).getState();
                int i2 = this.mDisplayState;
                Log.d("MiuiGxzwAnimView", "onDisplayChanged: oldState = " + i2 + ", newState = " + state);
                this.mDisplayState = state;
                if (this.mDozing && state != i2 && state != 1) {
                    this.mDozeScreenOn = true;
                    if (this.mKeyguardAuthen && this.mShowing) {
                        updateDozingIconAnim();
                    }
                }
            }
        }

        public void setAlpha(float f) {
            super.setAlpha(f);
            this.mAlpha = f;
            if (!isAttachedToWindow()) {
                return;
            }
            if (f == 1.0f || f == 0.0f) {
                WindowManager.LayoutParams layoutParams = this.mLayoutParams;
                layoutParams.alpha = this.mAlpha;
                this.mWindowManager.updateViewLayout(this, layoutParams);
            }
        }

        /* access modifiers changed from: protected */
        @Override // com.android.keyguard.fod.GxzwWindowFrameLayout
        public void onAttachedToWindow() {
            super.onAttachedToWindow();
            if (Float.compare(this.mAlpha, this.mLayoutParams.alpha) != 0) {
                WindowManager.LayoutParams layoutParams = this.mLayoutParams;
                layoutParams.alpha = this.mAlpha;
                this.mWindowManager.updateViewLayout(this, layoutParams);
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void show(boolean z) {
            if (!this.mShowing) {
                super.show();
                ValueAnimator valueAnimator = this.mAlphaAnimator;
                if (valueAnimator != null && valueAnimator.isRunning()) {
                    this.mAlphaAnimator.cancel();
                }
                this.mLightIcon = z;
                this.mMiuiGxzwAnimManager.setLightIcon(z);
                registerCallback();
                if (this.mGxzwTransparent) {
                    setAlpha(0.0f);
                } else {
                    setAlpha(1.0f);
                }
                addAnimView();
                this.mMiuiGxzwTipView.setVisibility(0);
            }
        }

        private void registerCallback() {
            this.mSystemUIHandler.post(new Runnable() {
                /* class com.android.keyguard.fod.$$Lambda$MiuiGxzwAnimView$MiuiGxzwAnimViewInternal$WF6NVuIvcd6P6NaONaKtG7HDPV8 */

                public final void run() {
                    MiuiGxzwAnimView.MiuiGxzwAnimViewInternal.this.lambda$registerCallback$0$MiuiGxzwAnimView$MiuiGxzwAnimViewInternal();
                }
            });
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$registerCallback$0 */
        public /* synthetic */ void lambda$registerCallback$0$MiuiGxzwAnimView$MiuiGxzwAnimViewInternal() {
            KeyguardUpdateMonitor keyguardUpdateMonitor = (KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class);
            ((IMiuiKeyguardWallpaperController) Dependency.get(IMiuiKeyguardWallpaperController.class)).registerWallpaperChangeCallback(this.mWallpaperChangeCallback);
            keyguardUpdateMonitor.registerCallback(this.mKeyguardUpdateMonitorCallback);
            this.mKeyguardUpdateMonitorCallback.onKeyguardBouncerChanged(keyguardUpdateMonitor.isBouncerShowing());
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void dismiss(boolean z) {
            if (this.mShowing) {
                super.dismiss();
                unregisterCallback();
                this.mMiuiGxzwTipView.setVisibility(8);
                if (!this.mKeyguardAuthen || !((MiuiFastUnlockController) Dependency.get(MiuiFastUnlockController.class)).isFastUnlock() || !z) {
                    removeAnimView();
                } else {
                    startFadeAniamtion();
                }
            }
        }

        private void unregisterCallback() {
            this.mSystemUIHandler.post(new Runnable() {
                /* class com.android.keyguard.fod.$$Lambda$MiuiGxzwAnimView$MiuiGxzwAnimViewInternal$1C2yzKqc6KaPtF2XnOdfCdc4 */

                public final void run() {
                    MiuiGxzwAnimView.MiuiGxzwAnimViewInternal.this.lambda$unregisterCallback$1$MiuiGxzwAnimView$MiuiGxzwAnimViewInternal();
                }
            });
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$unregisterCallback$1 */
        public /* synthetic */ void lambda$unregisterCallback$1$MiuiGxzwAnimView$MiuiGxzwAnimViewInternal() {
            ((IMiuiKeyguardWallpaperController) Dependency.get(IMiuiKeyguardWallpaperController.class)).unregisterWallpaperChangeCallback(this.mWallpaperChangeCallback);
            ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).removeCallback(this.mKeyguardUpdateMonitorCallback);
        }

        private void addAnimView() {
            IVisibleStyle visible = Folme.useAt(this.mTextureView).visible();
            visible.cancel();
            visible.setShow();
            this.mMainHandler.removeCallbacks(this.mRemoveRunnable);
            addViewToWindow();
            if (isAttachedToWindow()) {
                this.mWindowManager.updateViewLayout(this, this.mLayoutParams);
                drawFingerprintIcon(this.mDozing);
            }
            setVisibility(0);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void removeAnimView() {
            this.mMiuiGxzwFrameAnimation.stopAnimation();
            this.mMiuiGxzwFrameAnimation.clean();
            this.mMainHandler.removeCallbacks(this.mRemoveRunnable);
            this.mMainHandler.postDelayed(this.mRemoveRunnable, 10000);
            setVisibility(8);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void startDozing() {
            this.mDozing = true;
            stopAnim();
            if (((MiuiKeyguardWallpaperControllerImpl) Dependency.get(MiuiKeyguardWallpaperControllerImpl.class)).isAodUsingSuperWallpaper()) {
                this.mDozingIconAnimDone = true;
            }
            drawFingerprintIcon(this.mDozing);
            updateDozingIconAnim();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void stopDozing() {
            this.mDozeScreenOn = false;
            this.mDozingIconAnimDone = false;
            this.mDozing = false;
            this.mShouldShowBackAnim = false;
            this.mMiuiGxzwTipView.stopTipAnim();
            if (!MiuiGxzwManager.getInstance().isUnlockByGxzw()) {
                drawFingerprintIcon(false);
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setEnrolling(boolean z) {
            this.mMiuiGxzwAnimManager.setEnrolling(z);
            if (this.mShowing) {
                drawFingerprintIcon(this.mDozing);
            }
        }

        private void startIconAnim(boolean z) {
            Log.i("MiuiGxzwAnimView", "startIconAnim");
            this.mShouldShowBackAnim = false;
            startAnim(this.mMiuiGxzwAnimManager.getIconAnimArgs(z));
            this.mMiuiGxzwTipView.stopTipAnim();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void startRecognizingAnim() {
            Log.i("MiuiGxzwAnimView", "startRecognizingAnim");
            this.mShouldShowBackAnim = true;
            startAnim(this.mMiuiGxzwAnimManager.getRecognizingAnimArgs(this.mDozing));
            this.mMiuiGxzwTipView.stopTipAnim();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void startFalseAnim() {
            Log.i("MiuiGxzwAnimView", "startFalseAnim");
            boolean z = true;
            this.mShouldShowBackAnim = true;
            startAnim(this.mMiuiGxzwAnimManager.getFalseAnimArgs(this.mDozing));
            if (this.mDozing || !this.mLightWallpaperGxzw) {
                z = false;
            }
            startTipAnim(z, getContext().getString(C0021R$string.gxzw_try_again), (float) this.mMiuiGxzwAnimManager.getFalseTipTranslationY(getContext()));
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void startBackAnim() {
            Log.i("MiuiGxzwAnimView", "startBackAnim: mShouldShowBackAnim = " + this.mShouldShowBackAnim);
            if (this.mShouldShowBackAnim) {
                this.mShouldShowBackAnim = false;
                startAnim(this.mMiuiGxzwAnimManager.getBackAnimArgs(this.mDozing));
                this.mMiuiGxzwTipView.stopTipAnim();
                return;
            }
            this.mMiuiGxzwTipView.stopTipAnim();
            drawFingerprintIcon(this.mDozing);
        }

        private void startAnim(MiuiGxzwAnimManager.MiuiGxzwAnimArgs miuiGxzwAnimArgs) {
            int[] iArr;
            if (!isDisableAnimAndTip() && (iArr = miuiGxzwAnimArgs.res) != null && iArr.length > 0) {
                this.mMiuiGxzwFrameAnimation.setMode(miuiGxzwAnimArgs.repeat ? 2 : 1);
                this.mMiuiGxzwFrameAnimation.setFrameInterval(miuiGxzwAnimArgs.frameInterval);
                this.mMiuiGxzwFrameAnimation.startAnimation(iArr, miuiGxzwAnimArgs.startPosition, miuiGxzwAnimArgs.backgroundRes, miuiGxzwAnimArgs.backgroundFrame, new AnimationListener(miuiGxzwAnimArgs), miuiGxzwAnimArgs.customerDrawBitmap, miuiGxzwAnimArgs.translateX, miuiGxzwAnimArgs.translateY);
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void stopAnim() {
            this.mShouldShowBackAnim = false;
            this.mMiuiGxzwFrameAnimation.stopAnimation();
            this.mMiuiGxzwTipView.stopTipAnim();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void stopTip() {
            this.mMiuiGxzwTipView.stopTipAnim();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void drawFingerprintIcon(boolean z) {
            if (this.mSurfaceCreate) {
                if (this.mDozing && !this.mDozingIconAnimDone) {
                    this.mMiuiGxzwFrameAnimation.clean();
                } else if (!MiuiGxzwUtils.isLargeFod() || !this.mKeyguardAuthen) {
                    this.mMiuiGxzwFrameAnimation.draw(this.mMiuiGxzwAnimManager.getFingerIconResource(z), false, 1.0f);
                    if (z) {
                        MiuiGxzwManager.getInstance().requestDrawWackLock(300);
                    }
                } else {
                    startIconAnim(this.mDozing);
                }
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setTranslate(int i, int i2) {
            this.mMiuiGxzwAnimManager.setTranslate(i, i2);
            this.mMiuiGxzwTipView.setTranslate(i, i2);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setCollecting(boolean z) {
            this.mCollecting = z;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setGxzwTransparent(boolean z) {
            if (this.mGxzwTransparent != z) {
                this.mGxzwTransparent = z;
                setAlpha(z ? 0.0f : 1.0f);
                if (!MiuiGxzwUtils.isLargeFod()) {
                    return;
                }
                if (z) {
                    stopAnim();
                } else {
                    startIconAnim(this.mDozing);
                }
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void onWallpaperChange(boolean z) {
            boolean z2 = this.mLightWallpaperGxzw;
            this.mLightWallpaperGxzw = z;
            this.mMiuiGxzwAnimManager.setLightWallpaperGxzw(z);
            if (z2 != z && !this.mDozing && this.mShowing) {
                this.mShouldShowBackAnim = false;
                this.mMiuiGxzwTipView.stopTipAnim();
                drawFingerprintIcon(this.mDozing);
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void onKeyguardBouncerChanged(boolean z, boolean z2) {
            this.mBouncer = z;
            this.mMiuiGxzwAnimManager.setBouncer(z);
            if (z2) {
                drawFingerprintIcon(this.mDozing);
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void performSuccessFeedback() {
            if (MiuiKeyguardUtils.SUPPORT_LINEAR_MOTOR_VIBRATE) {
                Vibrator vibrator = (Vibrator) getContext().getSystemService("vibrator");
                if (this.mAnimFeedback && vibrator != null) {
                    vibrator.cancel();
                }
                if (((HapticFeedBackImpl) Dependency.get(HapticFeedBackImpl.class)).isSupportExtHapticFeedback(166)) {
                    this.mSystemUIHandler.post($$Lambda$MiuiGxzwAnimView$MiuiGxzwAnimViewInternal$UaxnELxzuDnXdfJaaePdm_842UE.INSTANCE);
                } else {
                    this.mSystemUIHandler.post($$Lambda$MiuiGxzwAnimView$MiuiGxzwAnimViewInternal$pU93HFtcJ915rU4l2vSbn9uDT5U.INSTANCE);
                }
                this.mAnimFeedback = false;
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        public void performFailFeedback() {
            if (((HapticFeedBackImpl) Dependency.get(HapticFeedBackImpl.class)).isSupportExtHapticFeedback(165)) {
                Vibrator vibrator = (Vibrator) getContext().getSystemService("vibrator");
                if (this.mAnimFeedback && vibrator != null) {
                    vibrator.cancel();
                }
                this.mSystemUIHandler.post($$Lambda$MiuiGxzwAnimView$MiuiGxzwAnimViewInternal$EofAXSl4oW5sLBWNfhgzhOWxbNU.INSTANCE);
                this.mAnimFeedback = false;
                return;
            }
            MiuiGxzwUtils.vibrateNormal(getContext());
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void cancelAnimFeedback() {
            if (MiuiKeyguardUtils.SUPPORT_LINEAR_MOTOR_VIBRATE && this.mAnimFeedback) {
                Vibrator vibrator = (Vibrator) getContext().getSystemService("vibrator");
                if (vibrator != null) {
                    vibrator.cancel();
                }
                this.mAnimFeedback = false;
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void performAnimFeedback() {
            if (MiuiKeyguardUtils.SUPPORT_LINEAR_MOTOR_VIBRATE) {
                this.mSystemUIHandler.post(new Runnable(this.mMiuiGxzwAnimManager.getFodMotionRtpId()) {
                    /* class com.android.keyguard.fod.$$Lambda$MiuiGxzwAnimView$MiuiGxzwAnimViewInternal$qePVMtNEKUrZWbPwNh9ru8B55U */
                    public final /* synthetic */ int f$0;

                    {
                        this.f$0 = r1;
                    }

                    public final void run() {
                        ((HapticFeedBackImpl) Dependency.get(HapticFeedBackImpl.class)).extHapticFeedback(this.f$0, false, 0);
                    }
                });
                this.mAnimFeedback = true;
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void disableLockScreenFodAnim(boolean z) {
            this.mDisableLockScreenFodAnim = z;
        }

        private void updateDozingIconAnim() {
            if (this.mDozing && this.mSurfaceCreate && this.mDozeScreenOn) {
                if (!this.mDozingIconAnimDone && MiuiGxzwUtils.isFodAodShowEnable(getContext()) && !this.mCollecting) {
                    startIconAnim(true);
                }
                this.mDozingIconAnimDone = true;
            }
        }

        private void startFadeAniamtion() {
            ValueAnimator valueAnimator = this.mAlphaAnimator;
            if (valueAnimator != null && valueAnimator.isRunning()) {
                this.mAlphaAnimator.cancel();
            }
            new ObjectAnimator();
            ObjectAnimator ofFloat = ObjectAnimator.ofFloat(this, View.ALPHA, 1.0f, 0.0f);
            this.mAlphaAnimator = ofFloat;
            ofFloat.setDuration(300L);
            this.mAlphaAnimator.setInterpolator(new QuarticEaseOutInterpolator());
            this.mAlphaAnimator.addListener(new Animator.AnimatorListener() {
                /* class com.android.keyguard.fod.MiuiGxzwAnimView.MiuiGxzwAnimViewInternal.AnonymousClass4 */
                private boolean cancel = false;

                public void onAnimationRepeat(Animator animator) {
                }

                public void onAnimationStart(Animator animator) {
                }

                public void onAnimationEnd(Animator animator) {
                    MiuiGxzwAnimViewInternal.this.mAlphaAnimator = null;
                    if (!this.cancel) {
                        MiuiGxzwAnimViewInternal.this.removeAnimView();
                    }
                }

                public void onAnimationCancel(Animator animator) {
                    MiuiGxzwAnimViewInternal.this.mAlphaAnimator = null;
                    this.cancel = true;
                }
            });
            this.mAlphaAnimator.start();
        }

        /* access modifiers changed from: private */
        public class AnimationListener implements MiuiGxzwFrameAnimation.FrameAnimationListener {
            private final MiuiGxzwAnimManager.MiuiGxzwAnimArgs mArgs;

            private AnimationListener(MiuiGxzwAnimManager.MiuiGxzwAnimArgs miuiGxzwAnimArgs) {
                this.mArgs = miuiGxzwAnimArgs;
            }

            @Override // com.android.keyguard.fod.MiuiGxzwFrameAnimation.FrameAnimationListener
            public void onStart() {
                Log.i("MiuiGxzwAnimView", "onStart");
                if (this.mArgs.aod) {
                    MiuiGxzwManager.getInstance().requestDrawWackLock(120000);
                }
                if (this.mArgs.feedback) {
                    MiuiGxzwAnimViewInternal.this.performAnimFeedback();
                }
            }

            @Override // com.android.keyguard.fod.MiuiGxzwFrameAnimation.FrameAnimationListener
            public void onInterrupt() {
                Log.i("MiuiGxzwAnimView", "onInterrupt");
                if (this.mArgs.aod) {
                    MiuiGxzwAnimViewInternal.this.mMainHandler.postDelayed(MiuiGxzwAnimViewInternal.this.mReleaseDrawWackLockRunnable, 300);
                }
                if (this.mArgs.feedback) {
                    MiuiGxzwAnimViewInternal.this.cancelAnimFeedback();
                }
            }

            @Override // com.android.keyguard.fod.MiuiGxzwFrameAnimation.FrameAnimationListener
            public void onFinish() {
                Log.i("MiuiGxzwAnimView", "onFinish");
                if (this.mArgs.aod) {
                    MiuiGxzwAnimViewInternal.this.mMainHandler.postDelayed(MiuiGxzwAnimViewInternal.this.mReleaseDrawWackLockRunnable, 300);
                }
                if (this.mArgs.feedback) {
                    MiuiGxzwAnimViewInternal.this.cancelAnimFeedback();
                }
            }

            @Override // com.android.keyguard.fod.MiuiGxzwFrameAnimation.FrameAnimationListener
            public void onRepeat() {
                if (this.mArgs.feedback && MiuiGxzwAnimViewInternal.this.mAnimFeedback) {
                    MiuiGxzwAnimViewInternal.this.performAnimFeedback();
                }
            }
        }

        private void startTipAnim(boolean z, String str, float f) {
            if (!isDisableAnimAndTip()) {
                this.mMiuiGxzwTipView.startTipAnim(z, str, f);
            }
        }

        private boolean isDisableAnimAndTip() {
            return this.mDisableLockScreenFodAnim && !this.mDozing && this.mKeyguardAuthen && !this.mBouncer;
        }
    }
}
