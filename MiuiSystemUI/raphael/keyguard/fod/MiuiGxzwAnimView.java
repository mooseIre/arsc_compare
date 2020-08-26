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
import com.android.keyguard.MiuiKeyguardUtils;
import com.android.keyguard.fod.MiuiGxzwAnimManager;
import com.android.keyguard.fod.MiuiGxzwAnimView;
import com.android.keyguard.fod.MiuiGxzwFrameAnimation;
import com.android.systemui.Dependency;
import com.android.systemui.HapticFeedBackImpl;
import com.android.systemui.plugins.R;
import java.util.Objects;
import miui.animation.Folme;
import miui.animation.IVisibleStyle;
import miui.animation.base.AnimConfig;
import miui.animation.listener.TransitionListener;
import miui.view.animation.QuarticEaseOutInterpolator;

class MiuiGxzwAnimView {
    private final Handler mMainHandler;
    private MiuiGxzwAnimViewInternal mMiuiGxzwAnimView;

    public MiuiGxzwAnimView(Context context) {
        Handler handler = new Handler();
        HandlerThread handlerThread = new HandlerThread("MiuiGxzwAnimView");
        handlerThread.start();
        this.mMainHandler = new Handler(handlerThread.getLooper());
        this.mMainHandler.post(new Runnable(context, handler) {
            private final /* synthetic */ Context f$1;
            private final /* synthetic */ Handler f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                MiuiGxzwAnimView.this.lambda$new$0$MiuiGxzwAnimView(this.f$1, this.f$2);
            }
        });
    }

    public /* synthetic */ void lambda$new$0$MiuiGxzwAnimView(Context context, Handler handler) {
        this.mMiuiGxzwAnimView = new MiuiGxzwAnimViewInternal(context, handler);
    }

    public /* synthetic */ void lambda$show$1$MiuiGxzwAnimView(boolean z) {
        this.mMiuiGxzwAnimView.show(z);
    }

    public void show(boolean z) {
        this.mMainHandler.post(new Runnable(z) {
            private final /* synthetic */ boolean f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                MiuiGxzwAnimView.this.lambda$show$1$MiuiGxzwAnimView(this.f$1);
            }
        });
    }

    public void dismiss(boolean z) {
        this.mMainHandler.post(new Runnable(z) {
            private final /* synthetic */ boolean f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                MiuiGxzwAnimView.this.lambda$dismiss$2$MiuiGxzwAnimView(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$dismiss$2$MiuiGxzwAnimView(boolean z) {
        this.mMiuiGxzwAnimView.dismiss(z);
    }

    public void startDozing() {
        Handler handler = this.mMainHandler;
        MiuiGxzwAnimViewInternal miuiGxzwAnimViewInternal = this.mMiuiGxzwAnimView;
        Objects.requireNonNull(miuiGxzwAnimViewInternal);
        handler.post(new Runnable() {
            public final void run() {
                MiuiGxzwAnimView.MiuiGxzwAnimViewInternal.this.startDozing();
            }
        });
    }

    public void stopDozing() {
        Handler handler = this.mMainHandler;
        MiuiGxzwAnimViewInternal miuiGxzwAnimViewInternal = this.mMiuiGxzwAnimView;
        Objects.requireNonNull(miuiGxzwAnimViewInternal);
        handler.post(new Runnable() {
            public final void run() {
                MiuiGxzwAnimView.MiuiGxzwAnimViewInternal.this.stopDozing();
            }
        });
    }

    public /* synthetic */ void lambda$setEnrolling$5$MiuiGxzwAnimView(boolean z) {
        this.mMiuiGxzwAnimView.setEnrolling(z);
    }

    public void setEnrolling(boolean z) {
        this.mMainHandler.post(new Runnable(z) {
            private final /* synthetic */ boolean f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                MiuiGxzwAnimView.this.lambda$setEnrolling$5$MiuiGxzwAnimView(this.f$1);
            }
        });
    }

    public void startRecognizingAnim() {
        Handler handler = this.mMainHandler;
        MiuiGxzwAnimViewInternal miuiGxzwAnimViewInternal = this.mMiuiGxzwAnimView;
        Objects.requireNonNull(miuiGxzwAnimViewInternal);
        handler.post(new Runnable() {
            public final void run() {
                MiuiGxzwAnimView.MiuiGxzwAnimViewInternal.this.startRecognizingAnim();
            }
        });
    }

    public void startFalseAnim() {
        Handler handler = this.mMainHandler;
        MiuiGxzwAnimViewInternal miuiGxzwAnimViewInternal = this.mMiuiGxzwAnimView;
        Objects.requireNonNull(miuiGxzwAnimViewInternal);
        handler.post(new Runnable() {
            public final void run() {
                MiuiGxzwAnimView.MiuiGxzwAnimViewInternal.this.startFalseAnim();
            }
        });
    }

    public void startBackAnim() {
        Handler handler = this.mMainHandler;
        MiuiGxzwAnimViewInternal miuiGxzwAnimViewInternal = this.mMiuiGxzwAnimView;
        Objects.requireNonNull(miuiGxzwAnimViewInternal);
        handler.post(new Runnable() {
            public final void run() {
                MiuiGxzwAnimView.MiuiGxzwAnimViewInternal.this.startBackAnim();
            }
        });
    }

    public void stopAnim() {
        Handler handler = this.mMainHandler;
        MiuiGxzwAnimViewInternal miuiGxzwAnimViewInternal = this.mMiuiGxzwAnimView;
        Objects.requireNonNull(miuiGxzwAnimViewInternal);
        handler.post(new Runnable() {
            public final void run() {
                MiuiGxzwAnimView.MiuiGxzwAnimViewInternal.this.stopAnim();
            }
        });
    }

    public void stopTip() {
        Handler handler = this.mMainHandler;
        MiuiGxzwAnimViewInternal miuiGxzwAnimViewInternal = this.mMiuiGxzwAnimView;
        Objects.requireNonNull(miuiGxzwAnimViewInternal);
        handler.post(new Runnable() {
            public final void run() {
                MiuiGxzwAnimView.MiuiGxzwAnimViewInternal.this.stopTip();
            }
        });
    }

    public void showMorePress() {
        Handler handler = this.mMainHandler;
        MiuiGxzwAnimViewInternal miuiGxzwAnimViewInternal = this.mMiuiGxzwAnimView;
        Objects.requireNonNull(miuiGxzwAnimViewInternal);
        handler.post(new Runnable() {
            public final void run() {
                MiuiGxzwAnimView.MiuiGxzwAnimViewInternal.this.showMorePress();
            }
        });
    }

    public void drawFingerprintIcon(boolean z) {
        this.mMainHandler.post(new Runnable(z) {
            private final /* synthetic */ boolean f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                MiuiGxzwAnimView.this.lambda$drawFingerprintIcon$12$MiuiGxzwAnimView(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$drawFingerprintIcon$12$MiuiGxzwAnimView(boolean z) {
        this.mMiuiGxzwAnimView.drawFingerprintIcon(z);
    }

    public /* synthetic */ void lambda$setTranslate$13$MiuiGxzwAnimView(int i, int i2) {
        this.mMiuiGxzwAnimView.setTranslate(i, i2);
    }

    public void setTranslate(int i, int i2) {
        this.mMainHandler.post(new Runnable(i, i2) {
            private final /* synthetic */ int f$1;
            private final /* synthetic */ int f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                MiuiGxzwAnimView.this.lambda$setTranslate$13$MiuiGxzwAnimView(this.f$1, this.f$2);
            }
        });
    }

    public /* synthetic */ void lambda$setCollecting$14$MiuiGxzwAnimView(boolean z) {
        this.mMiuiGxzwAnimView.setCollecting(z);
    }

    public void setCollecting(boolean z) {
        this.mMainHandler.post(new Runnable(z) {
            private final /* synthetic */ boolean f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                MiuiGxzwAnimView.this.lambda$setCollecting$14$MiuiGxzwAnimView(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$setGxzwTransparent$15$MiuiGxzwAnimView(boolean z) {
        this.mMiuiGxzwAnimView.setGxzwTransparent(z);
    }

    public void setGxzwTransparent(boolean z) {
        this.mMainHandler.post(new Runnable(z) {
            private final /* synthetic */ boolean f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                MiuiGxzwAnimView.this.lambda$setGxzwTransparent$15$MiuiGxzwAnimView(this.f$1);
            }
        });
    }

    public void performSuccessFeedback() {
        Handler handler = this.mMainHandler;
        MiuiGxzwAnimViewInternal miuiGxzwAnimViewInternal = this.mMiuiGxzwAnimView;
        Objects.requireNonNull(miuiGxzwAnimViewInternal);
        handler.post(new Runnable() {
            public final void run() {
                MiuiGxzwAnimView.MiuiGxzwAnimViewInternal.this.performSuccessFeedback();
            }
        });
    }

    public void cancelAnimFeedback(Context context) {
        if (MiuiKeyguardUtils.SUPPORT_LINEAR_MOTOR_VIBRATE) {
            Vibrator vibrator = (Vibrator) context.getSystemService("vibrator");
            if (vibrator != null) {
                vibrator.cancel();
            }
            this.mMainHandler.post(new Runnable() {
                public final void run() {
                    MiuiGxzwAnimView.this.lambda$cancelAnimFeedback$17$MiuiGxzwAnimView();
                }
            });
        }
    }

    public /* synthetic */ void lambda$cancelAnimFeedback$17$MiuiGxzwAnimView() {
        boolean unused = this.mMiuiGxzwAnimView.mAnimFeedback = false;
    }

    public void disableLockScreenFodAnim(boolean z) {
        this.mMainHandler.post(new Runnable(z) {
            private final /* synthetic */ boolean f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                MiuiGxzwAnimView.this.lambda$disableLockScreenFodAnim$18$MiuiGxzwAnimView(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$disableLockScreenFodAnim$18$MiuiGxzwAnimView(boolean z) {
        this.mMiuiGxzwAnimView.disableLockScreenFodAnim(z);
    }

    public /* synthetic */ void lambda$setVisibility$19$MiuiGxzwAnimView(int i) {
        this.mMiuiGxzwAnimView.setVisibility(i);
    }

    public void setVisibility(int i) {
        this.mMainHandler.post(new Runnable(i) {
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                MiuiGxzwAnimView.this.lambda$setVisibility$19$MiuiGxzwAnimView(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$setVisibilityAnim$20$MiuiGxzwAnimView(int i) {
        this.mMiuiGxzwAnimView.setVisibilityAnim(i);
    }

    public void setVisibilityAnim(int i) {
        this.mMainHandler.post(new Runnable(i) {
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                MiuiGxzwAnimView.this.lambda$setVisibilityAnim$20$MiuiGxzwAnimView(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$onKeyguardAuthen$22$MiuiGxzwAnimView(boolean z) {
        this.mMiuiGxzwAnimView.onKeyguardAuthen(z);
    }

    public void onKeyguardAuthen(boolean z) {
        this.mMainHandler.post(new Runnable(z) {
            private final /* synthetic */ boolean f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                MiuiGxzwAnimView.this.lambda$onKeyguardAuthen$22$MiuiGxzwAnimView(this.f$1);
            }
        });
    }

    private static class MiuiGxzwAnimViewInternal extends GxzwNoRotateFrameLayout implements DisplayManager.DisplayListener, TextureView.SurfaceTextureListener {
        private float mAlpha;
        /* access modifiers changed from: private */
        public ValueAnimator mAlphaAnimator;
        /* access modifiers changed from: private */
        public boolean mAnimFeedback;
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
        /* access modifiers changed from: private */
        public final Handler mMainHandler;
        private MiuiGxzwAnimManager mMiuiGxzwAnimManager;
        private MiuiGxzwFrameAnimation mMiuiGxzwFrameAnimation;
        private MiuiGxzwTipView mMiuiGxzwTipView;
        /* access modifiers changed from: private */
        public Runnable mReleaseDrawWackLockRunnable;
        private Runnable mRemoveRunnable = new Runnable() {
            public final void run() {
                MiuiGxzwAnimView.MiuiGxzwAnimViewInternal.this.removeViewFromWindow();
            }
        };
        private boolean mShouldShowBackAnim = false;
        private boolean mSurfaceCreate = false;
        private final Handler mSystemUIHandler;
        private TextureView mTextureView;
        private final KeyguardUpdateMonitor.WallpaperChangeCallback mWallpaperChangeCallback;

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
                public final void run() {
                    MiuiGxzwManager.this.releaseDrawWackLock();
                }
            };
            this.mAlpha = 1.0f;
            this.mKeyguardUpdateMonitorCallback = new KeyguardUpdateMonitorCallback() {
                public void onKeyguardBouncerChanged(boolean z) {
                    MiuiGxzwAnimViewInternal.this.mMainHandler.post(
                    /*  JADX ERROR: Method code generation error
                        jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x0019: INVOKE  
                          (wrap: android.os.Handler : 0x0010: INVOKE  (r1v1 android.os.Handler) = 
                          (wrap: com.android.keyguard.fod.MiuiGxzwAnimView$MiuiGxzwAnimViewInternal : 0x000e: IGET  (r1v0 com.android.keyguard.fod.MiuiGxzwAnimView$MiuiGxzwAnimViewInternal) = 
                          (r3v0 'this' com.android.keyguard.fod.MiuiGxzwAnimView$MiuiGxzwAnimViewInternal$1 A[THIS])
                         com.android.keyguard.fod.MiuiGxzwAnimView.MiuiGxzwAnimViewInternal.1.this$0 com.android.keyguard.fod.MiuiGxzwAnimView$MiuiGxzwAnimViewInternal)
                         com.android.keyguard.fod.MiuiGxzwAnimView.MiuiGxzwAnimViewInternal.access$000(com.android.keyguard.fod.MiuiGxzwAnimView$MiuiGxzwAnimViewInternal):android.os.Handler type: STATIC)
                          (wrap: com.android.keyguard.fod.-$$Lambda$MiuiGxzwAnimView$MiuiGxzwAnimViewInternal$1$i4db7Cf5H8H4M7ryC89z0IUwbhE : 0x0016: CONSTRUCTOR  (r2v0 com.android.keyguard.fod.-$$Lambda$MiuiGxzwAnimView$MiuiGxzwAnimViewInternal$1$i4db7Cf5H8H4M7ryC89z0IUwbhE) = 
                          (r3v0 'this' com.android.keyguard.fod.MiuiGxzwAnimView$MiuiGxzwAnimViewInternal$1 A[THIS])
                          (r4v0 'z' boolean)
                          (wrap: boolean : 0x000a: INVOKE  (r0v3 boolean) = 
                          (wrap: com.android.keyguard.KeyguardUpdateMonitor : 0x0006: INVOKE  (r0v2 com.android.keyguard.KeyguardUpdateMonitor) = 
                          (wrap: android.content.Context : 0x0002: INVOKE  (r0v1 android.content.Context) = 
                          (wrap: com.android.keyguard.fod.MiuiGxzwAnimView$MiuiGxzwAnimViewInternal : 0x0000: IGET  (r0v0 com.android.keyguard.fod.MiuiGxzwAnimView$MiuiGxzwAnimViewInternal) = 
                          (r3v0 'this' com.android.keyguard.fod.MiuiGxzwAnimView$MiuiGxzwAnimViewInternal$1 A[THIS])
                         com.android.keyguard.fod.MiuiGxzwAnimView.MiuiGxzwAnimViewInternal.1.this$0 com.android.keyguard.fod.MiuiGxzwAnimView$MiuiGxzwAnimViewInternal)
                         android.widget.FrameLayout.getContext():android.content.Context type: VIRTUAL)
                         com.android.keyguard.KeyguardUpdateMonitor.getInstance(android.content.Context):com.android.keyguard.KeyguardUpdateMonitor type: STATIC)
                         com.android.keyguard.KeyguardUpdateMonitor.isDeviceInteractive():boolean type: VIRTUAL)
                         call: com.android.keyguard.fod.-$$Lambda$MiuiGxzwAnimView$MiuiGxzwAnimViewInternal$1$i4db7Cf5H8H4M7ryC89z0IUwbhE.<init>(com.android.keyguard.fod.MiuiGxzwAnimView$MiuiGxzwAnimViewInternal$1, boolean, boolean):void type: CONSTRUCTOR)
                         android.os.Handler.post(java.lang.Runnable):boolean type: VIRTUAL in method: com.android.keyguard.fod.MiuiGxzwAnimView.MiuiGxzwAnimViewInternal.1.onKeyguardBouncerChanged(boolean):void, dex: classes.dex
                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:256)
                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:221)
                        	at jadx.core.codegen.RegionGen.makeSimpleBlock(RegionGen.java:109)
                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:55)
                        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                        	at jadx.core.codegen.MethodGen.addRegionInsns(MethodGen.java:211)
                        	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:204)
                        	at jadx.core.codegen.ClassGen.addMethodCode(ClassGen.java:318)
                        	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:271)
                        	at jadx.core.codegen.ClassGen.lambda$addInnerClsAndMethods$2(ClassGen.java:240)
                        	at java.base/java.util.stream.ForEachOps$ForEachOp$OfRef.accept(ForEachOps.java:183)
                        	at java.base/java.util.ArrayList.forEach(ArrayList.java:1541)
                        	at java.base/java.util.stream.SortedOps$RefSortingSink.end(SortedOps.java:395)
                        	at java.base/java.util.stream.Sink$ChainedReference.end(Sink.java:258)
                        	at java.base/java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:485)
                        	at java.base/java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:474)
                        	at java.base/java.util.stream.ForEachOps$ForEachOp.evaluateSequential(ForEachOps.java:150)
                        	at java.base/java.util.stream.ForEachOps$ForEachOp$OfRef.evaluateSequential(ForEachOps.java:173)
                        	at java.base/java.util.stream.AbstractPipeline.evaluate(AbstractPipeline.java:234)
                        	at java.base/java.util.stream.ReferencePipeline.forEach(ReferencePipeline.java:497)
                        	at jadx.core.codegen.ClassGen.addInnerClsAndMethods(ClassGen.java:236)
                        	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:227)
                        	at jadx.core.codegen.InsnGen.inlineAnonymousConstructor(InsnGen.java:676)
                        	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:607)
                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:364)
                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:231)
                        	at jadx.core.codegen.InsnGen.addWrappedArg(InsnGen.java:123)
                        	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:107)
                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:429)
                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:250)
                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:221)
                        	at jadx.core.codegen.RegionGen.makeSimpleBlock(RegionGen.java:109)
                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:55)
                        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                        	at jadx.core.codegen.MethodGen.addRegionInsns(MethodGen.java:211)
                        	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:204)
                        	at jadx.core.codegen.ClassGen.addMethodCode(ClassGen.java:318)
                        	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:271)
                        	at jadx.core.codegen.ClassGen.lambda$addInnerClsAndMethods$2(ClassGen.java:240)
                        	at java.base/java.util.stream.ForEachOps$ForEachOp$OfRef.accept(ForEachOps.java:183)
                        	at java.base/java.util.ArrayList.forEach(ArrayList.java:1541)
                        	at java.base/java.util.stream.SortedOps$RefSortingSink.end(SortedOps.java:395)
                        	at java.base/java.util.stream.Sink$ChainedReference.end(Sink.java:258)
                        	at java.base/java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:485)
                        	at java.base/java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:474)
                        	at java.base/java.util.stream.ForEachOps$ForEachOp.evaluateSequential(ForEachOps.java:150)
                        	at java.base/java.util.stream.ForEachOps$ForEachOp$OfRef.evaluateSequential(ForEachOps.java:173)
                        	at java.base/java.util.stream.AbstractPipeline.evaluate(AbstractPipeline.java:234)
                        	at java.base/java.util.stream.ReferencePipeline.forEach(ReferencePipeline.java:497)
                        	at jadx.core.codegen.ClassGen.addInnerClsAndMethods(ClassGen.java:236)
                        	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:227)
                        	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:112)
                        	at jadx.core.codegen.ClassGen.addInnerClass(ClassGen.java:249)
                        	at jadx.core.codegen.ClassGen.lambda$addInnerClsAndMethods$2(ClassGen.java:238)
                        	at java.base/java.util.stream.ForEachOps$ForEachOp$OfRef.accept(ForEachOps.java:183)
                        	at java.base/java.util.ArrayList.forEach(ArrayList.java:1541)
                        	at java.base/java.util.stream.SortedOps$RefSortingSink.end(SortedOps.java:395)
                        	at java.base/java.util.stream.Sink$ChainedReference.end(Sink.java:258)
                        	at java.base/java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:485)
                        	at java.base/java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:474)
                        	at java.base/java.util.stream.ForEachOps$ForEachOp.evaluateSequential(ForEachOps.java:150)
                        	at java.base/java.util.stream.ForEachOps$ForEachOp$OfRef.evaluateSequential(ForEachOps.java:173)
                        	at java.base/java.util.stream.AbstractPipeline.evaluate(AbstractPipeline.java:234)
                        	at java.base/java.util.stream.ReferencePipeline.forEach(ReferencePipeline.java:497)
                        	at jadx.core.codegen.ClassGen.addInnerClsAndMethods(ClassGen.java:236)
                        	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:227)
                        	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:112)
                        	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:78)
                        	at jadx.core.codegen.CodeGen.wrapCodeGen(CodeGen.java:44)
                        	at jadx.core.codegen.CodeGen.generateJavaCode(CodeGen.java:33)
                        	at jadx.core.codegen.CodeGen.generate(CodeGen.java:21)
                        	at jadx.core.ProcessClass.generateCode(ProcessClass.java:61)
                        	at jadx.core.dex.nodes.ClassNode.decompile(ClassNode.java:273)
                        Caused by: jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x0016: CONSTRUCTOR  (r2v0 com.android.keyguard.fod.-$$Lambda$MiuiGxzwAnimView$MiuiGxzwAnimViewInternal$1$i4db7Cf5H8H4M7ryC89z0IUwbhE) = 
                          (r3v0 'this' com.android.keyguard.fod.MiuiGxzwAnimView$MiuiGxzwAnimViewInternal$1 A[THIS])
                          (r4v0 'z' boolean)
                          (wrap: boolean : 0x000a: INVOKE  (r0v3 boolean) = 
                          (wrap: com.android.keyguard.KeyguardUpdateMonitor : 0x0006: INVOKE  (r0v2 com.android.keyguard.KeyguardUpdateMonitor) = 
                          (wrap: android.content.Context : 0x0002: INVOKE  (r0v1 android.content.Context) = 
                          (wrap: com.android.keyguard.fod.MiuiGxzwAnimView$MiuiGxzwAnimViewInternal : 0x0000: IGET  (r0v0 com.android.keyguard.fod.MiuiGxzwAnimView$MiuiGxzwAnimViewInternal) = 
                          (r3v0 'this' com.android.keyguard.fod.MiuiGxzwAnimView$MiuiGxzwAnimViewInternal$1 A[THIS])
                         com.android.keyguard.fod.MiuiGxzwAnimView.MiuiGxzwAnimViewInternal.1.this$0 com.android.keyguard.fod.MiuiGxzwAnimView$MiuiGxzwAnimViewInternal)
                         android.widget.FrameLayout.getContext():android.content.Context type: VIRTUAL)
                         com.android.keyguard.KeyguardUpdateMonitor.getInstance(android.content.Context):com.android.keyguard.KeyguardUpdateMonitor type: STATIC)
                         com.android.keyguard.KeyguardUpdateMonitor.isDeviceInteractive():boolean type: VIRTUAL)
                         call: com.android.keyguard.fod.-$$Lambda$MiuiGxzwAnimView$MiuiGxzwAnimViewInternal$1$i4db7Cf5H8H4M7ryC89z0IUwbhE.<init>(com.android.keyguard.fod.MiuiGxzwAnimView$MiuiGxzwAnimViewInternal$1, boolean, boolean):void type: CONSTRUCTOR in method: com.android.keyguard.fod.MiuiGxzwAnimView.MiuiGxzwAnimViewInternal.1.onKeyguardBouncerChanged(boolean):void, dex: classes.dex
                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:256)
                        	at jadx.core.codegen.InsnGen.addWrappedArg(InsnGen.java:123)
                        	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:107)
                        	at jadx.core.codegen.InsnGen.generateMethodArguments(InsnGen.java:787)
                        	at jadx.core.codegen.InsnGen.makeInvoke(InsnGen.java:728)
                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:368)
                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:250)
                        	... 74 more
                        Caused by: jadx.core.utils.exceptions.JadxRuntimeException: Expected class to be processed at this point, class: com.android.keyguard.fod.-$$Lambda$MiuiGxzwAnimView$MiuiGxzwAnimViewInternal$1$i4db7Cf5H8H4M7ryC89z0IUwbhE, state: NOT_LOADED
                        	at jadx.core.dex.nodes.ClassNode.ensureProcessed(ClassNode.java:260)
                        	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:606)
                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:364)
                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:231)
                        	... 80 more
                        */
                    /*
                        this = this;
                        com.android.keyguard.fod.MiuiGxzwAnimView$MiuiGxzwAnimViewInternal r0 = com.android.keyguard.fod.MiuiGxzwAnimView.MiuiGxzwAnimViewInternal.this
                        android.content.Context r0 = r0.getContext()
                        com.android.keyguard.KeyguardUpdateMonitor r0 = com.android.keyguard.KeyguardUpdateMonitor.getInstance(r0)
                        boolean r0 = r0.isDeviceInteractive()
                        com.android.keyguard.fod.MiuiGxzwAnimView$MiuiGxzwAnimViewInternal r1 = com.android.keyguard.fod.MiuiGxzwAnimView.MiuiGxzwAnimViewInternal.this
                        android.os.Handler r1 = r1.mMainHandler
                        com.android.keyguard.fod.-$$Lambda$MiuiGxzwAnimView$MiuiGxzwAnimViewInternal$1$i4db7Cf5H8H4M7ryC89z0IUwbhE r2 = new com.android.keyguard.fod.-$$Lambda$MiuiGxzwAnimView$MiuiGxzwAnimViewInternal$1$i4db7Cf5H8H4M7ryC89z0IUwbhE
                        r2.<init>(r3, r4, r0)
                        r1.post(r2)
                        return
                    */
                    throw new UnsupportedOperationException("Method not decompiled: com.android.keyguard.fod.MiuiGxzwAnimView.MiuiGxzwAnimViewInternal.AnonymousClass1.onKeyguardBouncerChanged(boolean):void");
                }

                public /* synthetic */ void lambda$onKeyguardBouncerChanged$0$MiuiGxzwAnimView$MiuiGxzwAnimViewInternal$1(boolean z, boolean z2) {
                    MiuiGxzwAnimViewInternal.this.onKeyguardBouncerChanged(z, z2);
                }
            };
            this.mWallpaperChangeCallback = new KeyguardUpdateMonitor.WallpaperChangeCallback() {
                public void onWallpaperChange(boolean z) {
                    KeyguardUpdateMonitor.getInstance(MiuiGxzwAnimViewInternal.this.getContext());
                    MiuiGxzwAnimViewInternal.this.mMainHandler.post(
                    /*  JADX ERROR: Method code generation error
                        jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x001e: INVOKE  
                          (wrap: android.os.Handler : 0x0015: INVOKE  (r0v1 android.os.Handler) = 
                          (wrap: com.android.keyguard.fod.MiuiGxzwAnimView$MiuiGxzwAnimViewInternal : 0x0013: IGET  (r0v0 com.android.keyguard.fod.MiuiGxzwAnimView$MiuiGxzwAnimViewInternal) = 
                          (r2v0 'this' com.android.keyguard.fod.MiuiGxzwAnimView$MiuiGxzwAnimViewInternal$2 A[THIS])
                         com.android.keyguard.fod.MiuiGxzwAnimView.MiuiGxzwAnimViewInternal.2.this$0 com.android.keyguard.fod.MiuiGxzwAnimView$MiuiGxzwAnimViewInternal)
                         com.android.keyguard.fod.MiuiGxzwAnimView.MiuiGxzwAnimViewInternal.access$000(com.android.keyguard.fod.MiuiGxzwAnimView$MiuiGxzwAnimViewInternal):android.os.Handler type: STATIC)
                          (wrap: com.android.keyguard.fod.-$$Lambda$MiuiGxzwAnimView$MiuiGxzwAnimViewInternal$2$rSJ0ssoNyg2LzR8PzqB-LE_kQpc : 0x001b: CONSTRUCTOR  (r1v0 com.android.keyguard.fod.-$$Lambda$MiuiGxzwAnimView$MiuiGxzwAnimViewInternal$2$rSJ0ssoNyg2LzR8PzqB-LE_kQpc) = 
                          (r2v0 'this' com.android.keyguard.fod.MiuiGxzwAnimView$MiuiGxzwAnimViewInternal$2 A[THIS])
                          (wrap: boolean : 0x000f: INVOKE  (r3v5 boolean) = 
                          (wrap: android.content.Context : 0x000b: INVOKE  (r3v4 android.content.Context) = 
                          (wrap: com.android.keyguard.fod.MiuiGxzwAnimView$MiuiGxzwAnimViewInternal : 0x0009: IGET  (r3v3 com.android.keyguard.fod.MiuiGxzwAnimView$MiuiGxzwAnimViewInternal) = 
                          (r2v0 'this' com.android.keyguard.fod.MiuiGxzwAnimView$MiuiGxzwAnimViewInternal$2 A[THIS])
                         com.android.keyguard.fod.MiuiGxzwAnimView.MiuiGxzwAnimViewInternal.2.this$0 com.android.keyguard.fod.MiuiGxzwAnimView$MiuiGxzwAnimViewInternal)
                         android.widget.FrameLayout.getContext():android.content.Context type: VIRTUAL)
                         com.android.keyguard.KeyguardUpdateMonitor.isWallpaperColorLight(android.content.Context):boolean type: STATIC)
                         call: com.android.keyguard.fod.-$$Lambda$MiuiGxzwAnimView$MiuiGxzwAnimViewInternal$2$rSJ0ssoNyg2LzR8PzqB-LE_kQpc.<init>(com.android.keyguard.fod.MiuiGxzwAnimView$MiuiGxzwAnimViewInternal$2, boolean):void type: CONSTRUCTOR)
                         android.os.Handler.post(java.lang.Runnable):boolean type: VIRTUAL in method: com.android.keyguard.fod.MiuiGxzwAnimView.MiuiGxzwAnimViewInternal.2.onWallpaperChange(boolean):void, dex: classes.dex
                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:256)
                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:221)
                        	at jadx.core.codegen.RegionGen.makeSimpleBlock(RegionGen.java:109)
                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:55)
                        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                        	at jadx.core.codegen.MethodGen.addRegionInsns(MethodGen.java:211)
                        	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:204)
                        	at jadx.core.codegen.ClassGen.addMethodCode(ClassGen.java:318)
                        	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:271)
                        	at jadx.core.codegen.ClassGen.lambda$addInnerClsAndMethods$2(ClassGen.java:240)
                        	at java.base/java.util.stream.ForEachOps$ForEachOp$OfRef.accept(ForEachOps.java:183)
                        	at java.base/java.util.ArrayList.forEach(ArrayList.java:1541)
                        	at java.base/java.util.stream.SortedOps$RefSortingSink.end(SortedOps.java:395)
                        	at java.base/java.util.stream.Sink$ChainedReference.end(Sink.java:258)
                        	at java.base/java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:485)
                        	at java.base/java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:474)
                        	at java.base/java.util.stream.ForEachOps$ForEachOp.evaluateSequential(ForEachOps.java:150)
                        	at java.base/java.util.stream.ForEachOps$ForEachOp$OfRef.evaluateSequential(ForEachOps.java:173)
                        	at java.base/java.util.stream.AbstractPipeline.evaluate(AbstractPipeline.java:234)
                        	at java.base/java.util.stream.ReferencePipeline.forEach(ReferencePipeline.java:497)
                        	at jadx.core.codegen.ClassGen.addInnerClsAndMethods(ClassGen.java:236)
                        	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:227)
                        	at jadx.core.codegen.InsnGen.inlineAnonymousConstructor(InsnGen.java:676)
                        	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:607)
                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:364)
                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:231)
                        	at jadx.core.codegen.InsnGen.addWrappedArg(InsnGen.java:123)
                        	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:107)
                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:429)
                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:250)
                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:221)
                        	at jadx.core.codegen.RegionGen.makeSimpleBlock(RegionGen.java:109)
                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:55)
                        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                        	at jadx.core.codegen.MethodGen.addRegionInsns(MethodGen.java:211)
                        	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:204)
                        	at jadx.core.codegen.ClassGen.addMethodCode(ClassGen.java:318)
                        	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:271)
                        	at jadx.core.codegen.ClassGen.lambda$addInnerClsAndMethods$2(ClassGen.java:240)
                        	at java.base/java.util.stream.ForEachOps$ForEachOp$OfRef.accept(ForEachOps.java:183)
                        	at java.base/java.util.ArrayList.forEach(ArrayList.java:1541)
                        	at java.base/java.util.stream.SortedOps$RefSortingSink.end(SortedOps.java:395)
                        	at java.base/java.util.stream.Sink$ChainedReference.end(Sink.java:258)
                        	at java.base/java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:485)
                        	at java.base/java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:474)
                        	at java.base/java.util.stream.ForEachOps$ForEachOp.evaluateSequential(ForEachOps.java:150)
                        	at java.base/java.util.stream.ForEachOps$ForEachOp$OfRef.evaluateSequential(ForEachOps.java:173)
                        	at java.base/java.util.stream.AbstractPipeline.evaluate(AbstractPipeline.java:234)
                        	at java.base/java.util.stream.ReferencePipeline.forEach(ReferencePipeline.java:497)
                        	at jadx.core.codegen.ClassGen.addInnerClsAndMethods(ClassGen.java:236)
                        	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:227)
                        	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:112)
                        	at jadx.core.codegen.ClassGen.addInnerClass(ClassGen.java:249)
                        	at jadx.core.codegen.ClassGen.lambda$addInnerClsAndMethods$2(ClassGen.java:238)
                        	at java.base/java.util.stream.ForEachOps$ForEachOp$OfRef.accept(ForEachOps.java:183)
                        	at java.base/java.util.ArrayList.forEach(ArrayList.java:1541)
                        	at java.base/java.util.stream.SortedOps$RefSortingSink.end(SortedOps.java:395)
                        	at java.base/java.util.stream.Sink$ChainedReference.end(Sink.java:258)
                        	at java.base/java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:485)
                        	at java.base/java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:474)
                        	at java.base/java.util.stream.ForEachOps$ForEachOp.evaluateSequential(ForEachOps.java:150)
                        	at java.base/java.util.stream.ForEachOps$ForEachOp$OfRef.evaluateSequential(ForEachOps.java:173)
                        	at java.base/java.util.stream.AbstractPipeline.evaluate(AbstractPipeline.java:234)
                        	at java.base/java.util.stream.ReferencePipeline.forEach(ReferencePipeline.java:497)
                        	at jadx.core.codegen.ClassGen.addInnerClsAndMethods(ClassGen.java:236)
                        	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:227)
                        	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:112)
                        	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:78)
                        	at jadx.core.codegen.CodeGen.wrapCodeGen(CodeGen.java:44)
                        	at jadx.core.codegen.CodeGen.generateJavaCode(CodeGen.java:33)
                        	at jadx.core.codegen.CodeGen.generate(CodeGen.java:21)
                        	at jadx.core.ProcessClass.generateCode(ProcessClass.java:61)
                        	at jadx.core.dex.nodes.ClassNode.decompile(ClassNode.java:273)
                        Caused by: jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x001b: CONSTRUCTOR  (r1v0 com.android.keyguard.fod.-$$Lambda$MiuiGxzwAnimView$MiuiGxzwAnimViewInternal$2$rSJ0ssoNyg2LzR8PzqB-LE_kQpc) = 
                          (r2v0 'this' com.android.keyguard.fod.MiuiGxzwAnimView$MiuiGxzwAnimViewInternal$2 A[THIS])
                          (wrap: boolean : 0x000f: INVOKE  (r3v5 boolean) = 
                          (wrap: android.content.Context : 0x000b: INVOKE  (r3v4 android.content.Context) = 
                          (wrap: com.android.keyguard.fod.MiuiGxzwAnimView$MiuiGxzwAnimViewInternal : 0x0009: IGET  (r3v3 com.android.keyguard.fod.MiuiGxzwAnimView$MiuiGxzwAnimViewInternal) = 
                          (r2v0 'this' com.android.keyguard.fod.MiuiGxzwAnimView$MiuiGxzwAnimViewInternal$2 A[THIS])
                         com.android.keyguard.fod.MiuiGxzwAnimView.MiuiGxzwAnimViewInternal.2.this$0 com.android.keyguard.fod.MiuiGxzwAnimView$MiuiGxzwAnimViewInternal)
                         android.widget.FrameLayout.getContext():android.content.Context type: VIRTUAL)
                         com.android.keyguard.KeyguardUpdateMonitor.isWallpaperColorLight(android.content.Context):boolean type: STATIC)
                         call: com.android.keyguard.fod.-$$Lambda$MiuiGxzwAnimView$MiuiGxzwAnimViewInternal$2$rSJ0ssoNyg2LzR8PzqB-LE_kQpc.<init>(com.android.keyguard.fod.MiuiGxzwAnimView$MiuiGxzwAnimViewInternal$2, boolean):void type: CONSTRUCTOR in method: com.android.keyguard.fod.MiuiGxzwAnimView.MiuiGxzwAnimViewInternal.2.onWallpaperChange(boolean):void, dex: classes.dex
                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:256)
                        	at jadx.core.codegen.InsnGen.addWrappedArg(InsnGen.java:123)
                        	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:107)
                        	at jadx.core.codegen.InsnGen.generateMethodArguments(InsnGen.java:787)
                        	at jadx.core.codegen.InsnGen.makeInvoke(InsnGen.java:728)
                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:368)
                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:250)
                        	... 74 more
                        Caused by: jadx.core.utils.exceptions.JadxRuntimeException: Expected class to be processed at this point, class: com.android.keyguard.fod.-$$Lambda$MiuiGxzwAnimView$MiuiGxzwAnimViewInternal$2$rSJ0ssoNyg2LzR8PzqB-LE_kQpc, state: NOT_LOADED
                        	at jadx.core.dex.nodes.ClassNode.ensureProcessed(ClassNode.java:260)
                        	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:606)
                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:364)
                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:231)
                        	... 80 more
                        */
                    /*
                        this = this;
                        com.android.keyguard.fod.MiuiGxzwAnimView$MiuiGxzwAnimViewInternal r3 = com.android.keyguard.fod.MiuiGxzwAnimView.MiuiGxzwAnimViewInternal.this
                        android.content.Context r3 = r3.getContext()
                        com.android.keyguard.KeyguardUpdateMonitor.getInstance(r3)
                        com.android.keyguard.fod.MiuiGxzwAnimView$MiuiGxzwAnimViewInternal r3 = com.android.keyguard.fod.MiuiGxzwAnimView.MiuiGxzwAnimViewInternal.this
                        android.content.Context r3 = r3.getContext()
                        boolean r3 = com.android.keyguard.KeyguardUpdateMonitor.isWallpaperColorLight(r3)
                        com.android.keyguard.fod.MiuiGxzwAnimView$MiuiGxzwAnimViewInternal r0 = com.android.keyguard.fod.MiuiGxzwAnimView.MiuiGxzwAnimViewInternal.this
                        android.os.Handler r0 = r0.mMainHandler
                        com.android.keyguard.fod.-$$Lambda$MiuiGxzwAnimView$MiuiGxzwAnimViewInternal$2$rSJ0ssoNyg2LzR8PzqB-LE_kQpc r1 = new com.android.keyguard.fod.-$$Lambda$MiuiGxzwAnimView$MiuiGxzwAnimViewInternal$2$rSJ0ssoNyg2LzR8PzqB-LE_kQpc
                        r1.<init>(r2, r3)
                        r0.post(r1)
                        return
                    */
                    throw new UnsupportedOperationException("Method not decompiled: com.android.keyguard.fod.MiuiGxzwAnimView.MiuiGxzwAnimViewInternal.AnonymousClass2.onWallpaperChange(boolean):void");
                }

                public /* synthetic */ void lambda$onWallpaperChange$0$MiuiGxzwAnimView$MiuiGxzwAnimViewInternal$2(boolean z) {
                    MiuiGxzwAnimViewInternal.this.onWallpaperChange(z);
                }
            };
            this.mMainHandler = new Handler();
            this.mSystemUIHandler = handler;
            initView();
        }

        private void initView() {
            this.mRegion = caculateRegion();
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(-1, -1);
            this.mTextureView = new TextureView(getContext());
            this.mTextureView.setSurfaceTextureListener(this);
            this.mMiuiGxzwFrameAnimation = new MiuiGxzwFrameAnimation(this.mTextureView);
            addView(this.mTextureView, layoutParams);
            this.mMiuiGxzwTipView = new MiuiGxzwTipView(getContext());
            addView(this.mMiuiGxzwTipView, new FrameLayout.LayoutParams(-1, -1));
            setSystemUiVisibility(4868);
            this.mMiuiGxzwFrameAnimation.setMode(1);
            this.mDisplayManager = (DisplayManager) getContext().getSystemService("display");
            this.mDisplayManager.registerDisplayListener(this, this.mMainHandler);
            this.mMiuiGxzwAnimManager = new MiuiGxzwAnimManager(getContext(), this.mMiuiGxzwFrameAnimation);
            this.mLayoutParams = new WindowManager.LayoutParams(this.mRegion.width(), this.mRegion.height(), 2015, 16778776, -2);
            WindowManager.LayoutParams layoutParams2 = this.mLayoutParams;
            layoutParams2.layoutInDisplayCutoutMode = 1;
            layoutParams2.privateFlags |= 16;
            layoutParams2.privateFlags |= MiuiGxzwUtils.PRIVATE_FLAG_IS_HBM_OVERLAY;
            layoutParams2.gravity = 51;
            Rect rect = this.mRegion;
            layoutParams2.x = rect.left;
            layoutParams2.y = rect.top;
            layoutParams2.setTitle("gxzw_anim");
        }

        public void onKeyguardAuthen(boolean z) {
            super.onKeyguardAuthen(z);
            if (this.mShowing && this.mKeyguardAuthen) {
                drawFingerprintIcon(this.mDozing);
            }
            this.mMiuiGxzwAnimManager.onKeyguardAuthen(z);
        }

        /* access modifiers changed from: protected */
        public Rect caculateRegion() {
            int i = MiuiGxzwUtils.GXZW_ICON_X - ((MiuiGxzwUtils.GXZW_ANIM_WIDTH - MiuiGxzwUtils.GXZW_ICON_WIDTH) / 2);
            int i2 = MiuiGxzwUtils.GXZW_ICON_Y - ((MiuiGxzwUtils.GXZW_ANIM_HEIGHT - MiuiGxzwUtils.GXZW_ICON_HEIGHT) / 2);
            return new Rect(i, i2, MiuiGxzwUtils.GXZW_ANIM_WIDTH + i, MiuiGxzwUtils.GXZW_ANIM_HEIGHT + i2);
        }

        /* access modifiers changed from: protected */
        public WindowManager.LayoutParams generateLayoutParams() {
            return this.mLayoutParams;
        }

        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i2) {
            Log.i("MiuiGxzwAnimView", "onSurfaceTextureAvailable");
            this.mSurfaceCreate = true;
            if (!this.mKeyguardAuthen || !this.mShowing || !this.mDozing) {
                this.mShouldShowBackAnim = false;
                this.mMiuiGxzwTipView.stopTipAnim();
                drawFingerprintIcon(this.mDozing);
                return;
            }
            updateDozingIconAnim();
        }

        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            Log.i("MiuiGxzwAnimView", "onSurfaceTextureDestroyed");
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
            this.mAlpha = f;
            if (isAttachedToWindow()) {
                WindowManager.LayoutParams layoutParams = this.mLayoutParams;
                layoutParams.alpha = this.mAlpha;
                this.mWindowManager.updateViewLayout(this, layoutParams);
            }
        }

        /* access modifiers changed from: protected */
        public void onAttachedToWindow() {
            super.onAttachedToWindow();
            if (Float.compare(this.mAlpha, this.mLayoutParams.alpha) != 0) {
                WindowManager.LayoutParams layoutParams = this.mLayoutParams;
                layoutParams.alpha = this.mAlpha;
                this.mWindowManager.updateViewLayout(this, layoutParams);
            }
        }

        /* access modifiers changed from: private */
        public void show(boolean z) {
            if (!this.mShowing) {
                super.show();
                ValueAnimator valueAnimator = this.mAlphaAnimator;
                if (valueAnimator != null && valueAnimator.isRunning()) {
                    this.mAlphaAnimator.cancel();
                }
                this.mLightIcon = z;
                this.mMiuiGxzwAnimManager.setLightIcon(this.mLightIcon);
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
                public final void run() {
                    MiuiGxzwAnimView.MiuiGxzwAnimViewInternal.this.lambda$registerCallback$0$MiuiGxzwAnimView$MiuiGxzwAnimViewInternal();
                }
            });
        }

        public /* synthetic */ void lambda$registerCallback$0$MiuiGxzwAnimView$MiuiGxzwAnimViewInternal() {
            KeyguardUpdateMonitor instance = KeyguardUpdateMonitor.getInstance(getContext());
            instance.registerWallpaperChangeCallback(this.mWallpaperChangeCallback);
            instance.registerCallback(this.mKeyguardUpdateMonitorCallback);
            this.mKeyguardUpdateMonitorCallback.onKeyguardBouncerChanged(instance.isBouncerShowing());
        }

        /* access modifiers changed from: private */
        public void dismiss(boolean z) {
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
                public final void run() {
                    MiuiGxzwAnimView.MiuiGxzwAnimViewInternal.this.lambda$unregisterCallback$1$MiuiGxzwAnimView$MiuiGxzwAnimViewInternal();
                }
            });
        }

        public /* synthetic */ void lambda$unregisterCallback$1$MiuiGxzwAnimView$MiuiGxzwAnimViewInternal() {
            KeyguardUpdateMonitor instance = KeyguardUpdateMonitor.getInstance(getContext());
            instance.unregisterWallpaperChangeCallback(this.mWallpaperChangeCallback);
            instance.removeCallback(this.mKeyguardUpdateMonitorCallback);
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
        public void removeAnimView() {
            this.mMiuiGxzwFrameAnimation.stopAnimation();
            this.mMiuiGxzwFrameAnimation.clean();
            this.mMainHandler.removeCallbacks(this.mRemoveRunnable);
            this.mMainHandler.postDelayed(this.mRemoveRunnable, 10000);
            setVisibility(8);
        }

        /* access modifiers changed from: private */
        public void startDozing() {
            this.mDozing = true;
            stopAnim();
            if (KeyguardUpdateMonitor.getInstance(getContext()).isAodUsingSuperWallpaper()) {
                this.mDozingIconAnimDone = true;
            }
            drawFingerprintIcon(this.mDozing);
            updateDozingIconAnim();
        }

        /* access modifiers changed from: private */
        public void stopDozing() {
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
        public void setEnrolling(boolean z) {
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
        public void startRecognizingAnim() {
            Log.i("MiuiGxzwAnimView", "startRecognizingAnim");
            this.mShouldShowBackAnim = true;
            startAnim(this.mMiuiGxzwAnimManager.getRecognizingAnimArgs(this.mDozing));
            this.mMiuiGxzwTipView.stopTipAnim();
        }

        /* access modifiers changed from: private */
        public void startFalseAnim() {
            Log.i("MiuiGxzwAnimView", "startFalseAnim");
            boolean z = true;
            this.mShouldShowBackAnim = true;
            startAnim(this.mMiuiGxzwAnimManager.getFalseAnimArgs(this.mDozing));
            if (this.mDozing || !this.mLightWallpaperGxzw) {
                z = false;
            }
            startTipAnim(z, getContext().getString(R.string.gxzw_try_again), (float) this.mMiuiGxzwAnimManager.getFalseTipTranslationY(getContext()));
        }

        /* access modifiers changed from: private */
        public void startBackAnim() {
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
        public void stopAnim() {
            this.mShouldShowBackAnim = false;
            this.mMiuiGxzwFrameAnimation.stopAnimation();
            this.mMiuiGxzwTipView.stopTipAnim();
        }

        /* access modifiers changed from: private */
        public void stopTip() {
            this.mMiuiGxzwTipView.stopTipAnim();
        }

        /* access modifiers changed from: private */
        public void showMorePress() {
            startTipAnim(!this.mDozing && this.mLightWallpaperGxzw, getContext().getString(R.string.gxzw_press_harder), 260.0f);
        }

        /* access modifiers changed from: private */
        public void drawFingerprintIcon(boolean z) {
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
        public void setTranslate(int i, int i2) {
            this.mMiuiGxzwAnimManager.setTranslate(i, i2);
            this.mMiuiGxzwTipView.setTranslate(i, i2);
        }

        /* access modifiers changed from: private */
        public void setCollecting(boolean z) {
            this.mCollecting = z;
        }

        /* access modifiers changed from: private */
        public void setGxzwTransparent(boolean z) {
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
        public void onWallpaperChange(boolean z) {
            boolean z2 = this.mLightWallpaperGxzw;
            this.mLightWallpaperGxzw = z;
            this.mMiuiGxzwAnimManager.setLightWallpaperGxzw(this.mLightWallpaperGxzw);
            if (z2 != z && !this.mDozing && this.mShowing) {
                this.mShouldShowBackAnim = false;
                this.mMiuiGxzwTipView.stopTipAnim();
                drawFingerprintIcon(this.mDozing);
            }
        }

        /* access modifiers changed from: private */
        public void onKeyguardBouncerChanged(boolean z, boolean z2) {
            this.mBouncer = z;
            this.mMiuiGxzwAnimManager.setBouncer(this.mBouncer);
            if (z2) {
                drawFingerprintIcon(this.mDozing);
            }
        }

        /* access modifiers changed from: private */
        public void performSuccessFeedback() {
            if (MiuiKeyguardUtils.SUPPORT_LINEAR_MOTOR_VIBRATE) {
                Vibrator vibrator = (Vibrator) getContext().getSystemService("vibrator");
                if (this.mAnimFeedback && vibrator != null) {
                    vibrator.cancel();
                }
                this.mSystemUIHandler.post($$Lambda$MiuiGxzwAnimView$MiuiGxzwAnimViewInternal$UaxnELxzuDnXdfJaaePdm_842UE.INSTANCE);
                this.mAnimFeedback = false;
            }
        }

        /* access modifiers changed from: private */
        public void cancelAnimFeedback() {
            if (MiuiKeyguardUtils.SUPPORT_LINEAR_MOTOR_VIBRATE && this.mAnimFeedback) {
                Vibrator vibrator = (Vibrator) getContext().getSystemService("vibrator");
                if (vibrator != null) {
                    vibrator.cancel();
                }
                this.mAnimFeedback = false;
            }
        }

        /* access modifiers changed from: private */
        public void performAnimFeedback() {
            if (MiuiKeyguardUtils.SUPPORT_LINEAR_MOTOR_VIBRATE) {
                this.mSystemUIHandler.post(new Runnable(this.mMiuiGxzwAnimManager.getFodMotionRtpId()) {
                    private final /* synthetic */ int f$0;

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
        public void disableLockScreenFodAnim(boolean z) {
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

        public void setVisibilityAnim(final int i) {
            IVisibleStyle visible = Folme.useAt(this.mTextureView).visible();
            visible.cancel();
            if (i == 0) {
                setVisibility(i);
                visible.show(new AnimConfig());
                return;
            }
            AnimConfig animConfig = new AnimConfig();
            animConfig.addListeners(new TransitionListener() {
                public void onComplete(Object obj) {
                    super.onComplete(obj);
                    MiuiGxzwAnimViewInternal.this.mMainHandler.post(
                    /*  JADX ERROR: Method code generation error
                        jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x0010: INVOKE  
                          (wrap: android.os.Handler : 0x0005: INVOKE  (r3v2 android.os.Handler) = 
                          (wrap: com.android.keyguard.fod.MiuiGxzwAnimView$MiuiGxzwAnimViewInternal : 0x0003: IGET  (r3v1 com.android.keyguard.fod.MiuiGxzwAnimView$MiuiGxzwAnimViewInternal) = 
                          (r2v0 'this' com.android.keyguard.fod.MiuiGxzwAnimView$MiuiGxzwAnimViewInternal$3 A[THIS])
                         com.android.keyguard.fod.MiuiGxzwAnimView.MiuiGxzwAnimViewInternal.3.this$0 com.android.keyguard.fod.MiuiGxzwAnimView$MiuiGxzwAnimViewInternal)
                         com.android.keyguard.fod.MiuiGxzwAnimView.MiuiGxzwAnimViewInternal.access$000(com.android.keyguard.fod.MiuiGxzwAnimView$MiuiGxzwAnimViewInternal):android.os.Handler type: STATIC)
                          (wrap: com.android.keyguard.fod.-$$Lambda$MiuiGxzwAnimView$MiuiGxzwAnimViewInternal$3$QI9DuZt4H7nHgje39UAvZ5uzXqc : 0x000d: CONSTRUCTOR  (r1v0 com.android.keyguard.fod.-$$Lambda$MiuiGxzwAnimView$MiuiGxzwAnimViewInternal$3$QI9DuZt4H7nHgje39UAvZ5uzXqc) = 
                          (r2v0 'this' com.android.keyguard.fod.MiuiGxzwAnimView$MiuiGxzwAnimViewInternal$3 A[THIS])
                          (wrap: int : 0x0009: IGET  (r0v0 int) = 
                          (r2v0 'this' com.android.keyguard.fod.MiuiGxzwAnimView$MiuiGxzwAnimViewInternal$3 A[THIS])
                         com.android.keyguard.fod.MiuiGxzwAnimView.MiuiGxzwAnimViewInternal.3.val$visibility int)
                         call: com.android.keyguard.fod.-$$Lambda$MiuiGxzwAnimView$MiuiGxzwAnimViewInternal$3$QI9DuZt4H7nHgje39UAvZ5uzXqc.<init>(com.android.keyguard.fod.MiuiGxzwAnimView$MiuiGxzwAnimViewInternal$3, int):void type: CONSTRUCTOR)
                         android.os.Handler.post(java.lang.Runnable):boolean type: VIRTUAL in method: com.android.keyguard.fod.MiuiGxzwAnimView.MiuiGxzwAnimViewInternal.3.onComplete(java.lang.Object):void, dex: classes.dex
                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:256)
                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:221)
                        	at jadx.core.codegen.RegionGen.makeSimpleBlock(RegionGen.java:109)
                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:55)
                        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                        	at jadx.core.codegen.MethodGen.addRegionInsns(MethodGen.java:211)
                        	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:204)
                        	at jadx.core.codegen.ClassGen.addMethodCode(ClassGen.java:318)
                        	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:271)
                        	at jadx.core.codegen.ClassGen.lambda$addInnerClsAndMethods$2(ClassGen.java:240)
                        	at java.base/java.util.stream.ForEachOps$ForEachOp$OfRef.accept(ForEachOps.java:183)
                        	at java.base/java.util.ArrayList.forEach(ArrayList.java:1541)
                        	at java.base/java.util.stream.SortedOps$RefSortingSink.end(SortedOps.java:395)
                        	at java.base/java.util.stream.Sink$ChainedReference.end(Sink.java:258)
                        	at java.base/java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:485)
                        	at java.base/java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:474)
                        	at java.base/java.util.stream.ForEachOps$ForEachOp.evaluateSequential(ForEachOps.java:150)
                        	at java.base/java.util.stream.ForEachOps$ForEachOp$OfRef.evaluateSequential(ForEachOps.java:173)
                        	at java.base/java.util.stream.AbstractPipeline.evaluate(AbstractPipeline.java:234)
                        	at java.base/java.util.stream.ReferencePipeline.forEach(ReferencePipeline.java:497)
                        	at jadx.core.codegen.ClassGen.addInnerClsAndMethods(ClassGen.java:236)
                        	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:227)
                        	at jadx.core.codegen.InsnGen.inlineAnonymousConstructor(InsnGen.java:676)
                        	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:607)
                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:364)
                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:231)
                        	at jadx.core.codegen.InsnGen.addWrappedArg(InsnGen.java:123)
                        	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:107)
                        	at jadx.core.codegen.InsnGen.processVarArg(InsnGen.java:871)
                        	at jadx.core.codegen.InsnGen.generateMethodArguments(InsnGen.java:784)
                        	at jadx.core.codegen.InsnGen.makeInvoke(InsnGen.java:728)
                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:368)
                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:250)
                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:221)
                        	at jadx.core.codegen.RegionGen.makeSimpleBlock(RegionGen.java:109)
                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:55)
                        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                        	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                        	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                        	at jadx.core.codegen.MethodGen.addRegionInsns(MethodGen.java:211)
                        	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:204)
                        	at jadx.core.codegen.ClassGen.addMethodCode(ClassGen.java:318)
                        	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:271)
                        	at jadx.core.codegen.ClassGen.lambda$addInnerClsAndMethods$2(ClassGen.java:240)
                        	at java.base/java.util.stream.ForEachOps$ForEachOp$OfRef.accept(ForEachOps.java:183)
                        	at java.base/java.util.ArrayList.forEach(ArrayList.java:1541)
                        	at java.base/java.util.stream.SortedOps$RefSortingSink.end(SortedOps.java:395)
                        	at java.base/java.util.stream.Sink$ChainedReference.end(Sink.java:258)
                        	at java.base/java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:485)
                        	at java.base/java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:474)
                        	at java.base/java.util.stream.ForEachOps$ForEachOp.evaluateSequential(ForEachOps.java:150)
                        	at java.base/java.util.stream.ForEachOps$ForEachOp$OfRef.evaluateSequential(ForEachOps.java:173)
                        	at java.base/java.util.stream.AbstractPipeline.evaluate(AbstractPipeline.java:234)
                        	at java.base/java.util.stream.ReferencePipeline.forEach(ReferencePipeline.java:497)
                        	at jadx.core.codegen.ClassGen.addInnerClsAndMethods(ClassGen.java:236)
                        	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:227)
                        	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:112)
                        	at jadx.core.codegen.ClassGen.addInnerClass(ClassGen.java:249)
                        	at jadx.core.codegen.ClassGen.lambda$addInnerClsAndMethods$2(ClassGen.java:238)
                        	at java.base/java.util.stream.ForEachOps$ForEachOp$OfRef.accept(ForEachOps.java:183)
                        	at java.base/java.util.ArrayList.forEach(ArrayList.java:1541)
                        	at java.base/java.util.stream.SortedOps$RefSortingSink.end(SortedOps.java:395)
                        	at java.base/java.util.stream.Sink$ChainedReference.end(Sink.java:258)
                        	at java.base/java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:485)
                        	at java.base/java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:474)
                        	at java.base/java.util.stream.ForEachOps$ForEachOp.evaluateSequential(ForEachOps.java:150)
                        	at java.base/java.util.stream.ForEachOps$ForEachOp$OfRef.evaluateSequential(ForEachOps.java:173)
                        	at java.base/java.util.stream.AbstractPipeline.evaluate(AbstractPipeline.java:234)
                        	at java.base/java.util.stream.ReferencePipeline.forEach(ReferencePipeline.java:497)
                        	at jadx.core.codegen.ClassGen.addInnerClsAndMethods(ClassGen.java:236)
                        	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:227)
                        	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:112)
                        	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:78)
                        	at jadx.core.codegen.CodeGen.wrapCodeGen(CodeGen.java:44)
                        	at jadx.core.codegen.CodeGen.generateJavaCode(CodeGen.java:33)
                        	at jadx.core.codegen.CodeGen.generate(CodeGen.java:21)
                        	at jadx.core.ProcessClass.generateCode(ProcessClass.java:61)
                        	at jadx.core.dex.nodes.ClassNode.decompile(ClassNode.java:273)
                        Caused by: jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x000d: CONSTRUCTOR  (r1v0 com.android.keyguard.fod.-$$Lambda$MiuiGxzwAnimView$MiuiGxzwAnimViewInternal$3$QI9DuZt4H7nHgje39UAvZ5uzXqc) = 
                          (r2v0 'this' com.android.keyguard.fod.MiuiGxzwAnimView$MiuiGxzwAnimViewInternal$3 A[THIS])
                          (wrap: int : 0x0009: IGET  (r0v0 int) = 
                          (r2v0 'this' com.android.keyguard.fod.MiuiGxzwAnimView$MiuiGxzwAnimViewInternal$3 A[THIS])
                         com.android.keyguard.fod.MiuiGxzwAnimView.MiuiGxzwAnimViewInternal.3.val$visibility int)
                         call: com.android.keyguard.fod.-$$Lambda$MiuiGxzwAnimView$MiuiGxzwAnimViewInternal$3$QI9DuZt4H7nHgje39UAvZ5uzXqc.<init>(com.android.keyguard.fod.MiuiGxzwAnimView$MiuiGxzwAnimViewInternal$3, int):void type: CONSTRUCTOR in method: com.android.keyguard.fod.MiuiGxzwAnimView.MiuiGxzwAnimViewInternal.3.onComplete(java.lang.Object):void, dex: classes.dex
                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:256)
                        	at jadx.core.codegen.InsnGen.addWrappedArg(InsnGen.java:123)
                        	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:107)
                        	at jadx.core.codegen.InsnGen.generateMethodArguments(InsnGen.java:787)
                        	at jadx.core.codegen.InsnGen.makeInvoke(InsnGen.java:728)
                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:368)
                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:250)
                        	... 81 more
                        Caused by: jadx.core.utils.exceptions.JadxRuntimeException: Expected class to be processed at this point, class: com.android.keyguard.fod.-$$Lambda$MiuiGxzwAnimView$MiuiGxzwAnimViewInternal$3$QI9DuZt4H7nHgje39UAvZ5uzXqc, state: NOT_LOADED
                        	at jadx.core.dex.nodes.ClassNode.ensureProcessed(ClassNode.java:260)
                        	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:606)
                        	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:364)
                        	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:231)
                        	... 87 more
                        */
                    /*
                        this = this;
                        super.onComplete(r3)
                        com.android.keyguard.fod.MiuiGxzwAnimView$MiuiGxzwAnimViewInternal r3 = com.android.keyguard.fod.MiuiGxzwAnimView.MiuiGxzwAnimViewInternal.this
                        android.os.Handler r3 = r3.mMainHandler
                        int r0 = r7
                        com.android.keyguard.fod.-$$Lambda$MiuiGxzwAnimView$MiuiGxzwAnimViewInternal$3$QI9DuZt4H7nHgje39UAvZ5uzXqc r1 = new com.android.keyguard.fod.-$$Lambda$MiuiGxzwAnimView$MiuiGxzwAnimViewInternal$3$QI9DuZt4H7nHgje39UAvZ5uzXqc
                        r1.<init>(r2, r0)
                        r3.post(r1)
                        return
                    */
                    throw new UnsupportedOperationException("Method not decompiled: com.android.keyguard.fod.MiuiGxzwAnimView.MiuiGxzwAnimViewInternal.AnonymousClass3.onComplete(java.lang.Object):void");
                }

                public /* synthetic */ void lambda$onComplete$0$MiuiGxzwAnimView$MiuiGxzwAnimViewInternal$3(int i) {
                    MiuiGxzwAnimViewInternal.this.setVisibility(i);
                }
            });
            visible.hide(animConfig);
        }

        private void startFadeAniamtion() {
            ValueAnimator valueAnimator = this.mAlphaAnimator;
            if (valueAnimator != null && valueAnimator.isRunning()) {
                this.mAlphaAnimator.cancel();
            }
            new ObjectAnimator();
            this.mAlphaAnimator = ObjectAnimator.ofFloat(this, View.ALPHA, new float[]{1.0f, 0.0f});
            this.mAlphaAnimator.setDuration(300);
            this.mAlphaAnimator.setInterpolator(new QuarticEaseOutInterpolator());
            this.mAlphaAnimator.addListener(new Animator.AnimatorListener() {
                private boolean cancel = false;

                public void onAnimationRepeat(Animator animator) {
                }

                public void onAnimationStart(Animator animator) {
                }

                public void onAnimationEnd(Animator animator) {
                    ValueAnimator unused = MiuiGxzwAnimViewInternal.this.mAlphaAnimator = null;
                    if (!this.cancel) {
                        MiuiGxzwAnimViewInternal.this.removeAnimView();
                    }
                }

                public void onAnimationCancel(Animator animator) {
                    ValueAnimator unused = MiuiGxzwAnimViewInternal.this.mAlphaAnimator = null;
                    this.cancel = true;
                }
            });
            this.mAlphaAnimator.start();
        }

        private class AnimationListener implements MiuiGxzwFrameAnimation.FrameAnimationListener {
            private final MiuiGxzwAnimManager.MiuiGxzwAnimArgs mArgs;

            private AnimationListener(MiuiGxzwAnimManager.MiuiGxzwAnimArgs miuiGxzwAnimArgs) {
                this.mArgs = miuiGxzwAnimArgs;
            }

            public void onStart() {
                Log.i("MiuiGxzwAnimView", "onStart");
                if (this.mArgs.aod) {
                    MiuiGxzwManager.getInstance().requestDrawWackLock(120000);
                }
                if (this.mArgs.feedback) {
                    MiuiGxzwAnimViewInternal.this.performAnimFeedback();
                }
            }

            public void onInterrupt() {
                Log.i("MiuiGxzwAnimView", "onInterrupt");
                if (this.mArgs.aod) {
                    MiuiGxzwAnimViewInternal.this.mMainHandler.postDelayed(MiuiGxzwAnimViewInternal.this.mReleaseDrawWackLockRunnable, 300);
                }
                if (this.mArgs.feedback) {
                    MiuiGxzwAnimViewInternal.this.cancelAnimFeedback();
                }
            }

            public void onFinish() {
                Log.i("MiuiGxzwAnimView", "onFinish");
                if (this.mArgs.aod) {
                    MiuiGxzwAnimViewInternal.this.mMainHandler.postDelayed(MiuiGxzwAnimViewInternal.this.mReleaseDrawWackLockRunnable, 300);
                }
                if (this.mArgs.feedback) {
                    MiuiGxzwAnimViewInternal.this.cancelAnimFeedback();
                }
            }

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
