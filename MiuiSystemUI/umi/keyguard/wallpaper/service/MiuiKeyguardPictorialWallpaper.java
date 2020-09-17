package com.android.keyguard.wallpaper.service;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.ActivityManager;
import android.database.ContentObserver;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.HandlerThread;
import android.provider.MiuiSettings;
import android.provider.Settings;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.util.Size;
import android.view.SurfaceHolder;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.MiuiFastUnlockController;
import com.android.keyguard.MiuiKeyguardUtils;
import com.android.keyguard.utils.DeviceLevelUtils;
import com.android.keyguard.wallpaper.KeyguardWallpaperRenderer;
import com.android.keyguard.wallpaper.KeyguardWallpaperUtils;
import com.android.keyguard.wallpaper.MiuiKeyguardWallpaperController;
import com.android.keyguard.wallpaper.service.BaseKeyguardWallpaperService;
import com.android.keyguard.wallpaper.service.MiuiKeyguardPictorialWallpaper;
import com.android.systemui.Dependency;
import com.android.systemui.glwallpaper.EglHelper;
import com.android.systemui.glwallpaper.GLWallpaperRenderer;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.policy.BatteryController;
import java.io.File;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class MiuiKeyguardPictorialWallpaper extends BaseKeyguardWallpaperService {
    /* access modifiers changed from: private */
    public HandlerThread mWorker;

    public void onCreate() {
        super.onCreate();
        HandlerThread handlerThread = new HandlerThread("MiuiKeyguardPictorialWallpaper");
        this.mWorker = handlerThread;
        handlerThread.start();
    }

    public WallpaperService.Engine onCreateEngine() {
        return new PictorialEngine();
    }

    public void onDestroy() {
        super.onDestroy();
        this.mWorker.quitSafely();
    }

    private class PictorialEngine extends BaseKeyguardWallpaperService.BaseEngine implements GLWallpaperRenderer.SurfaceProxy, StatusBarStateController.StateListener {
        private ValueAnimator mAnimator;
        private BatteryController mBatteryController;
        private StatusBarStateController mController;
        private ContentObserver mDarkModeObserver = new ContentObserver(MiuiKeyguardPictorialWallpaper.this.mWorker.getThreadHandler()) {
            public void onChange(boolean z) {
                boolean access$200 = PictorialEngine.this.mDarkModeUpdated;
                PictorialEngine pictorialEngine = PictorialEngine.this;
                boolean unused = pictorialEngine.mDarkModeUpdated = MiuiKeyguardUtils.isNightMode(pictorialEngine.mContext);
                boolean z2 = MiuiSettings.System.getBoolean(PictorialEngine.this.mContext.getContentResolver(), "darken_wallpaper_under_dark_mode", true);
                if (access$200 != PictorialEngine.this.mDarkModeUpdated && z2) {
                    PictorialEngine.this.mRenderer.updateDarkWallpaperMode(PictorialEngine.this.mDarkModeUpdated);
                    PictorialEngine.this.animateWallpaper(false, true);
                }
            }
        };
        /* access modifiers changed from: private */
        public boolean mDarkModeUpdated;
        private ContentObserver mDarkWallpaperModeObserver = new ContentObserver(MiuiKeyguardPictorialWallpaper.this.mWorker.getThreadHandler()) {
            public void onChange(boolean z) {
                boolean access$100 = PictorialEngine.this.mDarkWallpaperModeUpdated;
                PictorialEngine pictorialEngine = PictorialEngine.this;
                boolean z2 = true;
                boolean unused = pictorialEngine.mDarkWallpaperModeUpdated = MiuiSettings.System.getBoolean(pictorialEngine.mContext.getContentResolver(), "darken_wallpaper_under_dark_mode", true);
                if (access$100 != PictorialEngine.this.mDarkWallpaperModeUpdated) {
                    GLWallpaperRenderer access$300 = PictorialEngine.this.mRenderer;
                    if (!PictorialEngine.this.mDarkWallpaperModeUpdated || !PictorialEngine.this.mDarkModeUpdated) {
                        z2 = false;
                    }
                    access$300.updateDarkWallpaperMode(z2);
                }
            }
        };
        /* access modifiers changed from: private */
        public boolean mDarkWallpaperModeUpdated;
        private EglHelper mEglHelper;
        private final Runnable mFinishRenderingTask = new Runnable() {
            public final void run() {
                MiuiKeyguardPictorialWallpaper.PictorialEngine.this.finishRendering();
            }
        };
        private boolean mIsDozing;
        private KeyguardUpdateMonitor mKeyguardUpdateMonitor = KeyguardUpdateMonitor.getInstance(this.mContext);
        private Drawable mKeyguardWallpaper = null;
        private boolean mKeyguardWallpaperUpdated;
        private final Object mMonitor = new Object();
        private boolean mNeedRedraw;
        private final boolean mNeedTransition = ActivityManager.isHighEndGfx();
        /* access modifiers changed from: private */
        public GLWallpaperRenderer mRenderer;
        private boolean mWaitingForRendering;
        private float mWindowAlpha;

        public PictorialEngine() {
            super(MiuiKeyguardWallpaperController.KeyguardWallpaperType.PICTORIAL);
            StatusBarStateController statusBarStateController = (StatusBarStateController) Dependency.get(StatusBarStateController.class);
            this.mController = statusBarStateController;
            if (statusBarStateController != null) {
                statusBarStateController.addCallback(this);
            }
            this.mBatteryController = (BatteryController) Dependency.get(BatteryController.class);
            this.mEglHelper = new EglHelper();
            this.mRenderer = new KeyguardWallpaperRenderer(this.mContext, this);
            registerContentObserver();
            updateWallpaperDarken();
            this.mRenderer.updateDarkWallpaperMode(this.mDarkWallpaperModeUpdated && this.mDarkModeUpdated);
            this.mRenderer.updateWallpaperAlpha(1.0f);
        }

        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            setFixedSizeAllowed(true);
            updateSurfaceSize();
        }

        private void updateSurfaceSize() {
            SurfaceHolder surfaceHolder = getSurfaceHolder();
            Size reportSurfaceSize = this.mRenderer.reportSurfaceSize();
            surfaceHolder.setFixedSize(Math.max(64, reportSurfaceSize.getWidth()), Math.max(64, reportSurfaceSize.getHeight()));
        }

        public void onDestroy() {
            super.onDestroy();
            StatusBarStateController statusBarStateController = this.mController;
            if (statusBarStateController != null) {
                statusBarStateController.removeCallback(this);
            }
            this.mController = null;
            this.mContext.getContentResolver().unregisterContentObserver(this.mDarkWallpaperModeObserver);
            this.mContext.getContentResolver().unregisterContentObserver(this.mDarkModeObserver);
            MiuiKeyguardPictorialWallpaper.this.mWorker.getThreadHandler().post(new Runnable() {
                public final void run() {
                    MiuiKeyguardPictorialWallpaper.PictorialEngine.this.lambda$onDestroy$0$MiuiKeyguardPictorialWallpaper$PictorialEngine();
                }
            });
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onDestroy$0 */
        public /* synthetic */ void lambda$onDestroy$0$MiuiKeyguardPictorialWallpaper$PictorialEngine() {
            this.mRenderer.finish();
            this.mRenderer = null;
            this.mEglHelper.finish();
            this.mEglHelper = null;
            getSurfaceHolder().getSurface().hwuiDestroy();
        }

        public void onKeyguardWallpaperUpdated(MiuiKeyguardWallpaperController.KeyguardWallpaperType keyguardWallpaperType, boolean z, File file, Drawable drawable) {
            super.onKeyguardWallpaperUpdated(keyguardWallpaperType, z, file, drawable);
            if (this.mWallpaperType == keyguardWallpaperType) {
                Drawable drawable2 = this.mKeyguardWallpaper;
                if (drawable2 == null) {
                    this.mKeyguardWallpaper = drawable;
                } else if (drawable2 != drawable) {
                    this.mKeyguardWallpaper = drawable;
                    updateWallpaperDarken();
                    this.mRenderer.updateDarkWallpaperMode(this.mDarkWallpaperModeUpdated && this.mDarkModeUpdated);
                    this.mRenderer.updateWallpaperAlpha(1.0f);
                    if (this.mWakingUp) {
                        long j = (!this.mKeyguardShowing || !this.mNeedTransition) ? 0 : 300;
                        MiuiKeyguardPictorialWallpaper.this.mWorker.getThreadHandler().post(new Runnable(j) {
                            public final /* synthetic */ long f$1;

                            {
                                this.f$1 = r2;
                            }

                            public final void run() {
                                MiuiKeyguardPictorialWallpaper.PictorialEngine.this.lambda$onKeyguardWallpaperUpdated$1$MiuiKeyguardPictorialWallpaper$PictorialEngine(this.f$1);
                            }
                        });
                        MiuiKeyguardPictorialWallpaper.this.mWorker.getThreadHandler().postDelayed(new Runnable(j) {
                            public final /* synthetic */ long f$1;

                            {
                                this.f$1 = r2;
                            }

                            public final void run() {
                                MiuiKeyguardPictorialWallpaper.PictorialEngine.this.lambda$onKeyguardWallpaperUpdated$2$MiuiKeyguardPictorialWallpaper$PictorialEngine(this.f$1);
                            }
                        }, j);
                        return;
                    }
                    this.mKeyguardWallpaperUpdated = true;
                }
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onKeyguardWallpaperUpdated$1 */
        public /* synthetic */ void lambda$onKeyguardWallpaperUpdated$1$MiuiKeyguardPictorialWallpaper$PictorialEngine(long j) {
            this.mRenderer.updateAmbientMode(true, j);
            this.mKeyguardWallpaperUpdated = true;
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onKeyguardWallpaperUpdated$2 */
        public /* synthetic */ void lambda$onKeyguardWallpaperUpdated$2$MiuiKeyguardPictorialWallpaper$PictorialEngine(long j) {
            this.mRenderer.updateAmbientMode(false, j);
        }

        private void registerContentObserver() {
            this.mDarkModeUpdated = MiuiKeyguardUtils.isNightMode(this.mContext);
            this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("ui_night_mode"), false, this.mDarkModeObserver, -1);
            this.mDarkWallpaperModeUpdated = MiuiSettings.System.getBoolean(this.mContext.getContentResolver(), "darken_wallpaper_under_dark_mode", true);
            this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("darken_wallpaper_under_dark_mode"), false, this.mDarkWallpaperModeObserver, -1);
        }

        private void updateWallpaperDarken() {
            if (KeyguardWallpaperUtils.checkNeedDarkenWallpaper(this.mContext)) {
                this.mRenderer.updateDarken(true);
            } else {
                this.mRenderer.updateDarken(false);
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onOffsetsChanged$3 */
        public /* synthetic */ void lambda$onOffsetsChanged$3$MiuiKeyguardPictorialWallpaper$PictorialEngine(float f, float f2) {
            this.mRenderer.updateOffsets(f, f2);
        }

        public void onOffsetsChanged(float f, float f2, float f3, float f4, int i, int i2) {
            MiuiKeyguardPictorialWallpaper.this.mWorker.getThreadHandler().post(new Runnable(f, f2) {
                public final /* synthetic */ float f$1;
                public final /* synthetic */ float f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                public final void run() {
                    MiuiKeyguardPictorialWallpaper.PictorialEngine.this.lambda$onOffsetsChanged$3$MiuiKeyguardPictorialWallpaper$PictorialEngine(this.f$1, this.f$2);
                }
            });
        }

        /* JADX WARNING: Missing exception handler attribute for start block: B:13:0x0021 */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private void waitForBackgroundRendering() {
            /*
                r7 = this;
                java.lang.Object r0 = r7.mMonitor
                monitor-enter(r0)
                r1 = 0
                r2 = 1
                r7.mWaitingForRendering = r2     // Catch:{ InterruptedException -> 0x0021, all -> 0x0024 }
                r3 = r2
            L_0x0008:
                boolean r4 = r7.mWaitingForRendering     // Catch:{ InterruptedException -> 0x0021, all -> 0x0024 }
                if (r4 == 0) goto L_0x0021
                java.lang.Object r4 = r7.mMonitor     // Catch:{ InterruptedException -> 0x0021, all -> 0x0024 }
                r5 = 100
                r4.wait(r5)     // Catch:{ InterruptedException -> 0x0021, all -> 0x0024 }
                boolean r4 = r7.mWaitingForRendering     // Catch:{ InterruptedException -> 0x0021, all -> 0x0024 }
                r5 = 5
                if (r3 >= r5) goto L_0x001a
                r5 = r2
                goto L_0x001b
            L_0x001a:
                r5 = r1
            L_0x001b:
                r4 = r4 & r5
                r7.mWaitingForRendering = r4     // Catch:{ InterruptedException -> 0x0021, all -> 0x0024 }
                int r3 = r3 + 1
                goto L_0x0008
            L_0x0021:
                r7.mWaitingForRendering = r1     // Catch:{ all -> 0x002a }
                goto L_0x0028
            L_0x0024:
                r2 = move-exception
                r7.mWaitingForRendering = r1     // Catch:{ all -> 0x002a }
                throw r2     // Catch:{ all -> 0x002a }
            L_0x0028:
                monitor-exit(r0)     // Catch:{ all -> 0x002a }
                return
            L_0x002a:
                r7 = move-exception
                monitor-exit(r0)     // Catch:{ all -> 0x002a }
                throw r7
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.keyguard.wallpaper.service.MiuiKeyguardPictorialWallpaper.PictorialEngine.waitForBackgroundRendering():void");
        }

        public void onWallpaperAnimationUpdated(boolean z) {
            animateWallpaper(false, z && this.mNeedTransition);
        }

        /* access modifiers changed from: protected */
        public void scheduleUpdateSurface() {
            float f;
            float f2;
            float f3 = this.mWallpaperAnimValue;
            float f4 = 1.0f;
            if (f3 > 0.0f) {
                f2 = (float) (1.0d - ((double) Math.min(1.0f, f3 / 0.6f)));
                f = (float) Math.pow((double) (1.0f - Math.abs(Math.max(0.0f, Math.abs(this.mWallpaperAnimValue) - 0.6f) / 0.4f)), 2.0d);
                f4 = (float) (1.0d - Math.pow((double) this.mWallpaperAnimValue, 2.0d));
            } else {
                f = (float) (1.0d - Math.pow((double) (-f3), 3.0d));
                f2 = 1.0f;
            }
            rendererWallpaper(f4, f2, f);
        }

        /* access modifiers changed from: protected */
        public void onKeyguardShowingChanged(boolean z) {
            if (z) {
                ValueAnimator valueAnimator = this.mAnimator;
                if (valueAnimator != null && valueAnimator.isRunning()) {
                    this.mAnimator.cancel();
                }
                updateSurfaceAttrs(1.0f);
            } else if (!KeyguardWallpaperUtils.isSupportWallpaperBlur() || DeviceLevelUtils.isLowEndDevice()) {
                updateSurfaceAttrs(0.0f);
            } else if (!this.mKeyguardUpdateMonitor.getKeyguardGoingAway()) {
                updateSurfaceAttrs(0.0f);
            }
        }

        public void onDozingChanged(boolean z) {
            this.mIsDozing = z;
            updateSurfaceAttrs(this.mWindowAlpha);
        }

        /* access modifiers changed from: protected */
        public void onKeyguardGoingAway() {
            if (KeyguardWallpaperUtils.isSupportWallpaperBlur() && KeyguardWallpaperUtils.hasKeyguardWallpaperEffects(this.mContext) && !DeviceLevelUtils.isLowEndDevice() && !this.mBatteryController.isPowerSave()) {
                if (this.mKeyguardUpdateMonitor.isFingerprintWakeUnlock() || ((MiuiFastUnlockController) Dependency.get(MiuiFastUnlockController.class)).isFastUnlock()) {
                    updateSurfaceAttrs(0.0f);
                    return;
                }
                ValueAnimator ofFloat = ValueAnimator.ofFloat(new float[]{this.mWallpaperAnimValue, 1.0f});
                this.mAnimator = ofFloat;
                ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        PictorialEngine.this.mWallpaperAnimValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                        PictorialEngine.this.scheduleUpdateSurface();
                    }
                });
                this.mAnimator.addListener(new AnimatorListenerAdapter() {
                    public void onAnimationEnd(Animator animator) {
                        PictorialEngine.this.updateSurfaceAttrs(0.0f);
                    }
                });
                this.mAnimator.setInterpolator(new DecelerateInterpolator());
                this.mAnimator.setDuration(500);
                this.mAnimator.start();
            }
        }

        private void rendererWallpaper(float f, float f2, float f3) {
            this.mBlurRatio = f3;
            updateSurfaceAttrs(f);
            MiuiKeyguardPictorialWallpaper.this.mWorker.getThreadHandler().post(new Runnable() {
                public final void run() {
                    MiuiKeyguardPictorialWallpaper.PictorialEngine.this.lambda$rendererWallpaper$4$MiuiKeyguardPictorialWallpaper$PictorialEngine();
                }
            });
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$rendererWallpaper$4 */
        public /* synthetic */ void lambda$rendererWallpaper$4$MiuiKeyguardPictorialWallpaper$PictorialEngine() {
            preRender();
            requestRender();
            postRender();
        }

        /* access modifiers changed from: private */
        public void updateSurfaceAttrs(float f) {
            Boolean bool = Boolean.TRUE;
            WindowManager.LayoutParams layoutParams = this.mLayoutParams;
            if (layoutParams != null && BaseKeyguardWallpaperService.ENGINE_UPDATE_SURFACE != null) {
                boolean z = (layoutParams.alpha == f && layoutParams.blurRatio == this.mBlurRatio && layoutParams.windowAnimations == 0) ? false : true;
                updateBlurCurrent(this.mBlurRatio);
                this.mWindowAlpha = f;
                if (this.mIsDozing) {
                    f = 0.0f;
                }
                boolean z2 = this.mWindowAlpha != f;
                WindowManager.LayoutParams layoutParams2 = this.mLayoutParams;
                layoutParams2.alpha = f;
                layoutParams2.windowAnimations = 0;
                if (z || z2) {
                    try {
                        BaseKeyguardWallpaperService.ENGINE_UPDATE_SURFACE.invoke(this, new Object[]{bool, Boolean.FALSE, bool});
                    } catch (Exception e) {
                        Log.e("MiuiKeyguardPictorialWallpaper", "error in updateSurfaceAttrs", e);
                    }
                }
            }
        }

        /* access modifiers changed from: protected */
        public void onStartedGoingToSleep(int i) {
            animateWallpaper(true, this.mKeyguardShowing && this.mNeedTransition);
        }

        /* access modifiers changed from: private */
        public void animateWallpaper(boolean z, boolean z2) {
            long j = z2 ? 800 : 0;
            MiuiKeyguardPictorialWallpaper.this.mWorker.getThreadHandler().post(new Runnable(z, j) {
                public final /* synthetic */ boolean f$1;
                public final /* synthetic */ long f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                public final void run() {
                    MiuiKeyguardPictorialWallpaper.PictorialEngine.this.lambda$animateWallpaper$5$MiuiKeyguardPictorialWallpaper$PictorialEngine(this.f$1, this.f$2);
                }
            });
            if (z && j == 0) {
                waitForBackgroundRendering();
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$animateWallpaper$5 */
        public /* synthetic */ void lambda$animateWallpaper$5$MiuiKeyguardPictorialWallpaper$PictorialEngine(boolean z, long j) {
            this.mRenderer.updateAmbientMode(z, j);
            this.mRenderer.updateWallpaperAlpha(1.0f);
        }

        public void onSurfaceCreated(SurfaceHolder surfaceHolder) {
            super.onSurfaceCreated(surfaceHolder);
            MiuiKeyguardPictorialWallpaper.this.mWorker.getThreadHandler().post(new Runnable(surfaceHolder) {
                public final /* synthetic */ SurfaceHolder f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    MiuiKeyguardPictorialWallpaper.PictorialEngine.this.lambda$onSurfaceCreated$6$MiuiKeyguardPictorialWallpaper$PictorialEngine(this.f$1);
                }
            });
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onSurfaceCreated$6 */
        public /* synthetic */ void lambda$onSurfaceCreated$6$MiuiKeyguardPictorialWallpaper$PictorialEngine(SurfaceHolder surfaceHolder) {
            this.mEglHelper.init(surfaceHolder);
            this.mRenderer.onSurfaceCreated();
        }

        public void onSurfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {
            super.onSurfaceChanged(surfaceHolder, i, i2, i3);
            MiuiKeyguardPictorialWallpaper.this.mWorker.getThreadHandler().post(new Runnable(i2, i3) {
                public final /* synthetic */ int f$1;
                public final /* synthetic */ int f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                public final void run() {
                    MiuiKeyguardPictorialWallpaper.PictorialEngine.this.lambda$onSurfaceChanged$7$MiuiKeyguardPictorialWallpaper$PictorialEngine(this.f$1, this.f$2);
                }
            });
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onSurfaceChanged$7 */
        public /* synthetic */ void lambda$onSurfaceChanged$7$MiuiKeyguardPictorialWallpaper$PictorialEngine(int i, int i2) {
            this.mRenderer.onSurfaceChanged(i, i2);
            this.mNeedRedraw = true;
        }

        public void onSurfaceRedrawNeeded(SurfaceHolder surfaceHolder) {
            super.onSurfaceRedrawNeeded(surfaceHolder);
            MiuiKeyguardPictorialWallpaper.this.mWorker.getThreadHandler().post(new Runnable() {
                public final void run() {
                    MiuiKeyguardPictorialWallpaper.PictorialEngine.this.lambda$onSurfaceRedrawNeeded$8$MiuiKeyguardPictorialWallpaper$PictorialEngine();
                }
            });
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onSurfaceRedrawNeeded$8 */
        public /* synthetic */ void lambda$onSurfaceRedrawNeeded$8$MiuiKeyguardPictorialWallpaper$PictorialEngine() {
            if (this.mNeedRedraw) {
                preRender();
                this.mRenderer.updateWallpaperAlpha(1.0f);
                requestRender();
                postRender();
                this.mNeedRedraw = false;
            }
        }

        public void onStatePostChange() {
            if (this.mController.getState() == 0) {
                MiuiKeyguardPictorialWallpaper.this.mWorker.getThreadHandler().post(new Runnable() {
                    public final void run() {
                        MiuiKeyguardPictorialWallpaper.PictorialEngine.this.scheduleFinishRendering();
                    }
                });
            }
        }

        public void preRender() {
            preRenderInternal();
        }

        /* JADX WARNING: Removed duplicated region for block: B:23:0x0068  */
        /* JADX WARNING: Removed duplicated region for block: B:27:? A[RETURN, SYNTHETIC] */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private void preRenderInternal() {
            /*
                r7 = this;
                android.view.SurfaceHolder r0 = r7.getSurfaceHolder()
                android.graphics.Rect r0 = r0.getSurfaceFrame()
                r7.cancelFinishRenderingTask()
                com.android.systemui.glwallpaper.EglHelper r1 = r7.mEglHelper
                boolean r1 = r1.hasEglContext()
                java.lang.String r2 = "MiuiKeyguardPictorialWallpaper"
                r3 = 1
                r4 = 0
                if (r1 != 0) goto L_0x002c
                com.android.systemui.glwallpaper.EglHelper r1 = r7.mEglHelper
                r1.destroyEglSurface()
                com.android.systemui.glwallpaper.EglHelper r1 = r7.mEglHelper
                boolean r1 = r1.createEglContext()
                if (r1 != 0) goto L_0x002a
                java.lang.String r1 = "recreate egl context failed!"
                android.util.Log.w(r2, r1)
                goto L_0x002c
            L_0x002a:
                r1 = r3
                goto L_0x002d
            L_0x002c:
                r1 = r4
            L_0x002d:
                com.android.systemui.glwallpaper.EglHelper r5 = r7.mEglHelper
                boolean r5 = r5.hasEglContext()
                if (r5 == 0) goto L_0x004e
                com.android.systemui.glwallpaper.EglHelper r5 = r7.mEglHelper
                boolean r5 = r5.hasEglSurface()
                if (r5 != 0) goto L_0x004e
                com.android.systemui.glwallpaper.EglHelper r5 = r7.mEglHelper
                android.view.SurfaceHolder r6 = r7.getSurfaceHolder()
                boolean r5 = r5.createEglSurface(r6)
                if (r5 != 0) goto L_0x004e
                java.lang.String r5 = "recreate egl surface failed!"
                android.util.Log.w(r2, r5)
            L_0x004e:
                if (r1 != 0) goto L_0x0056
                boolean r1 = r7.mKeyguardWallpaperUpdated
                if (r1 == 0) goto L_0x0055
                goto L_0x0056
            L_0x0055:
                r3 = r4
            L_0x0056:
                if (r3 == 0) goto L_0x007c
                com.android.systemui.glwallpaper.EglHelper r1 = r7.mEglHelper
                boolean r1 = r1.hasEglContext()
                if (r1 == 0) goto L_0x007c
                com.android.systemui.glwallpaper.EglHelper r1 = r7.mEglHelper
                boolean r1 = r1.hasEglSurface()
                if (r1 == 0) goto L_0x007c
                r7.mKeyguardWallpaperUpdated = r4
                com.android.systemui.glwallpaper.GLWallpaperRenderer r1 = r7.mRenderer
                r1.onSurfaceCreated()
                com.android.systemui.glwallpaper.GLWallpaperRenderer r7 = r7.mRenderer
                int r1 = r0.width()
                int r0 = r0.height()
                r7.onSurfaceChanged(r1, r0)
            L_0x007c:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.keyguard.wallpaper.service.MiuiKeyguardPictorialWallpaper.PictorialEngine.preRenderInternal():void");
        }

        public void requestRender() {
            requestRenderInternal();
        }

        private void requestRenderInternal() {
            Rect surfaceFrame = getSurfaceHolder().getSurfaceFrame();
            if (this.mEglHelper.hasEglContext() && this.mEglHelper.hasEglSurface() && surfaceFrame.width() > 0 && surfaceFrame.height() > 0) {
                this.mRenderer.onDrawFrame();
                if (!this.mEglHelper.swapBuffer()) {
                    Log.d("MiuiKeyguardPictorialWallpaper", "drawFrame failed!");
                    return;
                }
                return;
            }
            Log.d("MiuiKeyguardPictorialWallpaper", "requestRender: not ready, has context=" + this.mEglHelper.hasEglContext() + ", has surface=" + this.mEglHelper.hasEglSurface() + ", frame=" + surfaceFrame);
        }

        public void postRender() {
            notifyWaitingThread();
            scheduleFinishRendering();
        }

        /* JADX WARNING: Can't wrap try/catch for region: R(5:2|3|(3:5|6|7)|8|9) */
        /* JADX WARNING: Missing exception handler attribute for start block: B:8:0x000f */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private void notifyWaitingThread() {
            /*
                r2 = this;
                java.lang.Object r0 = r2.mMonitor
                monitor-enter(r0)
                boolean r1 = r2.mWaitingForRendering     // Catch:{ all -> 0x0011 }
                if (r1 == 0) goto L_0x000f
                r1 = 0
                r2.mWaitingForRendering = r1     // Catch:{ IllegalMonitorStateException -> 0x000f }
                java.lang.Object r2 = r2.mMonitor     // Catch:{ IllegalMonitorStateException -> 0x000f }
                r2.notify()     // Catch:{ IllegalMonitorStateException -> 0x000f }
            L_0x000f:
                monitor-exit(r0)     // Catch:{ all -> 0x0011 }
                return
            L_0x0011:
                r2 = move-exception
                monitor-exit(r0)     // Catch:{ all -> 0x0011 }
                throw r2
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.keyguard.wallpaper.service.MiuiKeyguardPictorialWallpaper.PictorialEngine.notifyWaitingThread():void");
        }

        private void cancelFinishRenderingTask() {
            MiuiKeyguardPictorialWallpaper.this.mWorker.getThreadHandler().removeCallbacks(this.mFinishRenderingTask);
        }

        /* access modifiers changed from: private */
        public void scheduleFinishRendering() {
            cancelFinishRenderingTask();
            MiuiKeyguardPictorialWallpaper.this.mWorker.getThreadHandler().postDelayed(this.mFinishRenderingTask, 1000);
        }

        /* access modifiers changed from: private */
        public void finishRendering() {
            EglHelper eglHelper = this.mEglHelper;
            if (eglHelper != null) {
                eglHelper.destroyEglSurface();
                if (!needPreserveEglContext()) {
                    this.mEglHelper.destroyEglContext();
                }
            }
        }

        private boolean needPreserveEglContext() {
            StatusBarStateController statusBarStateController;
            if (!this.mNeedTransition || (statusBarStateController = this.mController) == null || statusBarStateController.getState() != 1) {
                return false;
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public void dump(String str, FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
            super.dump(str, fileDescriptor, printWriter, strArr);
            printWriter.print(str);
            printWriter.print("Engine=");
            printWriter.println(this);
            boolean isHighEndGfx = ActivityManager.isHighEndGfx();
            printWriter.print(str);
            printWriter.print("isHighEndGfx=");
            printWriter.println(isHighEndGfx);
            printWriter.print(str);
            printWriter.print("StatusBarState=");
            StatusBarStateController statusBarStateController = this.mController;
            Object obj = "null";
            printWriter.println(statusBarStateController != null ? Integer.valueOf(statusBarStateController.getState()) : obj);
            printWriter.print(str);
            printWriter.print("valid surface=");
            printWriter.println((getSurfaceHolder() == null || getSurfaceHolder().getSurface() == null) ? obj : Boolean.valueOf(getSurfaceHolder().getSurface().isValid()));
            printWriter.print(str);
            printWriter.print("surface frame=");
            if (getSurfaceHolder() != null) {
                obj = getSurfaceHolder().getSurfaceFrame();
            }
            printWriter.println(obj);
            this.mEglHelper.dump(str, fileDescriptor, printWriter, strArr);
            this.mRenderer.dump(str, fileDescriptor, printWriter, strArr);
        }
    }
}
