package com.android.systemui;

import android.app.ActivityManager;
import android.content.Context;
import android.database.ContentObserver;
import android.graphics.Rect;
import android.os.HandlerThread;
import android.provider.MiuiSettings;
import android.provider.Settings;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.util.Size;
import android.view.SurfaceHolder;
import com.android.internal.annotations.VisibleForTesting;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.keyguard.MiuiFastUnlockController;
import com.android.keyguard.MiuiKeyguardUtils;
import com.android.keyguard.wallpaper.KeyguardWallpaperUtils;
import com.android.systemui.ImageWallpaper;
import com.android.systemui.glwallpaper.EglHelper;
import com.android.systemui.glwallpaper.GLWallpaperRenderer;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.statusbar.policy.EncryptionHelper;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class ImageWallpaper extends WallpaperService {
    /* access modifiers changed from: private */
    public static final String TAG = "ImageWallpaper";
    /* access modifiers changed from: private */
    public HandlerThread mWorker;

    public void onCreate() {
        super.onCreate();
        this.mWorker = new HandlerThread(TAG);
        this.mWorker.start();
    }

    public WallpaperService.Engine onCreateEngine() {
        if (EncryptionHelper.systemNotReady()) {
            return new WallpaperService.Engine(this);
        }
        return new GLEngine(this);
    }

    public void onDestroy() {
        super.onDestroy();
        this.mWorker.quitSafely();
        this.mWorker = null;
    }

    class GLEngine extends WallpaperService.Engine implements GLWallpaperRenderer.SurfaceProxy, StatusBarStateController.StateListener {
        @VisibleForTesting
        static final int MIN_SURFACE_HEIGHT = 64;
        @VisibleForTesting
        static final int MIN_SURFACE_WIDTH = 64;
        /* access modifiers changed from: private */
        public BatteryController mBatteryController;
        /* access modifiers changed from: private */
        public Context mContext;
        private StatusBarStateController mController;
        private ContentObserver mDarkModeObserver = new ContentObserver(ImageWallpaper.this.mWorker.getThreadHandler()) {
            public void onChange(boolean z) {
                boolean access$600 = GLEngine.this.mDarkModeUpdated;
                GLEngine gLEngine = GLEngine.this;
                boolean unused = gLEngine.mDarkModeUpdated = MiuiKeyguardUtils.isNightMode(gLEngine.mContext);
                boolean z2 = MiuiSettings.System.getBoolean(GLEngine.this.mContext.getContentResolver(), "darken_wallpaper_under_dark_mode", true);
                if (access$600 != GLEngine.this.mDarkModeUpdated && z2) {
                    GLEngine.this.rendererWallpaper();
                }
            }
        };
        /* access modifiers changed from: private */
        public boolean mDarkModeUpdated;
        private ContentObserver mDarkWallpaperModeObserver = new ContentObserver(ImageWallpaper.this.mWorker.getThreadHandler()) {
            public void onChange(boolean z) {
                boolean access$300 = GLEngine.this.mDarkWallpaperModeUpdated;
                GLEngine gLEngine = GLEngine.this;
                boolean unused = gLEngine.mDarkWallpaperModeUpdated = MiuiSettings.System.getBoolean(gLEngine.mContext.getContentResolver(), "darken_wallpaper_under_dark_mode", true);
                if (access$300 != GLEngine.this.mDarkWallpaperModeUpdated) {
                    GLEngine.this.rendererWallpaper();
                }
            }
        };
        /* access modifiers changed from: private */
        public boolean mDarkWallpaperModeUpdated;
        private EglHelper mEglHelper;
        private final Runnable mFinishRenderingTask = new Runnable() {
            public final void run() {
                ImageWallpaper.GLEngine.this.finishRendering();
            }
        };
        private final KeyguardUpdateMonitorCallback mKeyguardCallback;
        private final Object mMonitor = new Object();
        private boolean mNeedRedraw;
        private final boolean mNeedTransition = ActivityManager.isHighEndGfx();
        /* access modifiers changed from: private */
        public GLWallpaperRenderer mRenderer;
        private boolean mWaitingForRendering;

        GLEngine(Context context) {
            super(ImageWallpaper.this);
            this.mContext = context;
            registerContentObserver();
            this.mController = (StatusBarStateController) Dependency.get(StatusBarStateController.class);
            StatusBarStateController statusBarStateController = this.mController;
            if (statusBarStateController != null) {
                statusBarStateController.addCallback(this);
            }
            this.mEglHelper = new EglHelper();
            this.mRenderer = new HomeWallpaperRenderer(context, this);
            updateWallpaperDarken();
            this.mRenderer.updateDarkWallpaperMode(this.mDarkWallpaperModeUpdated && this.mDarkModeUpdated);
            this.mBatteryController = (BatteryController) Dependency.get(BatteryController.class);
            this.mKeyguardCallback = new KeyguardUpdateMonitorCallback(ImageWallpaper.this) {
                public void onKeyguardGoingAway() {
                    if (KeyguardWallpaperUtils.isSupportWallpaperBlur() && !GLEngine.this.mBatteryController.isPowerSave() && !((MiuiFastUnlockController) Dependency.get(MiuiFastUnlockController.class)).isFastUnlock()) {
                        ImageWallpaper.this.mWorker.getThreadHandler().post(
                        /*  JADX ERROR: Method code generation error
                            jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x0032: INVOKE  
                              (wrap: android.os.Handler : 0x0029: INVOKE  (r0v11 android.os.Handler) = 
                              (wrap: android.os.HandlerThread : 0x0025: INVOKE  (r0v10 android.os.HandlerThread) = 
                              (wrap: com.android.systemui.ImageWallpaper : 0x0023: IGET  (r0v9 com.android.systemui.ImageWallpaper) = 
                              (wrap: com.android.systemui.ImageWallpaper$GLEngine : 0x0021: IGET  (r0v8 com.android.systemui.ImageWallpaper$GLEngine) = 
                              (r2v0 'this' com.android.systemui.ImageWallpaper$GLEngine$1 A[THIS])
                             com.android.systemui.ImageWallpaper.GLEngine.1.this$1 com.android.systemui.ImageWallpaper$GLEngine)
                             com.android.systemui.ImageWallpaper.GLEngine.this$0 com.android.systemui.ImageWallpaper)
                             com.android.systemui.ImageWallpaper.access$100(com.android.systemui.ImageWallpaper):android.os.HandlerThread type: STATIC)
                             android.os.HandlerThread.getThreadHandler():android.os.Handler type: VIRTUAL)
                              (wrap: com.android.systemui.-$$Lambda$ImageWallpaper$GLEngine$1$fKBrTAP0A5mQK-jCeW566832sso : 0x002f: CONSTRUCTOR  (r1v0 com.android.systemui.-$$Lambda$ImageWallpaper$GLEngine$1$fKBrTAP0A5mQK-jCeW566832sso) = 
                              (r2v0 'this' com.android.systemui.ImageWallpaper$GLEngine$1 A[THIS])
                             call: com.android.systemui.-$$Lambda$ImageWallpaper$GLEngine$1$fKBrTAP0A5mQK-jCeW566832sso.<init>(com.android.systemui.ImageWallpaper$GLEngine$1):void type: CONSTRUCTOR)
                             android.os.Handler.post(java.lang.Runnable):boolean type: VIRTUAL in method: com.android.systemui.ImageWallpaper.GLEngine.1.onKeyguardGoingAway():void, dex: classes.dex
                            	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:256)
                            	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:221)
                            	at jadx.core.codegen.RegionGen.makeSimpleBlock(RegionGen.java:109)
                            	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:55)
                            	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                            	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                            	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:98)
                            	at jadx.core.codegen.RegionGen.makeIf(RegionGen.java:142)
                            	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:62)
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
                            	at java.base/java.util.ArrayList.forEach(ArrayList.java:1540)
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
                            	at java.base/java.util.ArrayList.forEach(ArrayList.java:1540)
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
                            	at java.base/java.util.ArrayList.forEach(ArrayList.java:1540)
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
                            Caused by: jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x002f: CONSTRUCTOR  (r1v0 com.android.systemui.-$$Lambda$ImageWallpaper$GLEngine$1$fKBrTAP0A5mQK-jCeW566832sso) = 
                              (r2v0 'this' com.android.systemui.ImageWallpaper$GLEngine$1 A[THIS])
                             call: com.android.systemui.-$$Lambda$ImageWallpaper$GLEngine$1$fKBrTAP0A5mQK-jCeW566832sso.<init>(com.android.systemui.ImageWallpaper$GLEngine$1):void type: CONSTRUCTOR in method: com.android.systemui.ImageWallpaper.GLEngine.1.onKeyguardGoingAway():void, dex: classes.dex
                            	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:256)
                            	at jadx.core.codegen.InsnGen.addWrappedArg(InsnGen.java:123)
                            	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:107)
                            	at jadx.core.codegen.InsnGen.generateMethodArguments(InsnGen.java:787)
                            	at jadx.core.codegen.InsnGen.makeInvoke(InsnGen.java:728)
                            	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:368)
                            	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:250)
                            	... 81 more
                            Caused by: jadx.core.utils.exceptions.JadxRuntimeException: Expected class to be processed at this point, class: com.android.systemui.-$$Lambda$ImageWallpaper$GLEngine$1$fKBrTAP0A5mQK-jCeW566832sso, state: NOT_LOADED
                            	at jadx.core.dex.nodes.ClassNode.ensureProcessed(ClassNode.java:260)
                            	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:606)
                            	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:364)
                            	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:231)
                            	... 87 more
                            */
                        /*
                            this = this;
                            boolean r0 = com.android.keyguard.wallpaper.KeyguardWallpaperUtils.isSupportWallpaperBlur()
                            if (r0 == 0) goto L_0x0035
                            com.android.systemui.ImageWallpaper$GLEngine r0 = com.android.systemui.ImageWallpaper.GLEngine.this
                            com.android.systemui.statusbar.policy.BatteryController r0 = r0.mBatteryController
                            boolean r0 = r0.isPowerSave()
                            if (r0 != 0) goto L_0x0035
                            java.lang.Class<com.android.keyguard.MiuiFastUnlockController> r0 = com.android.keyguard.MiuiFastUnlockController.class
                            java.lang.Object r0 = com.android.systemui.Dependency.get(r0)
                            com.android.keyguard.MiuiFastUnlockController r0 = (com.android.keyguard.MiuiFastUnlockController) r0
                            boolean r0 = r0.isFastUnlock()
                            if (r0 == 0) goto L_0x0021
                            goto L_0x0035
                        L_0x0021:
                            com.android.systemui.ImageWallpaper$GLEngine r0 = com.android.systemui.ImageWallpaper.GLEngine.this
                            com.android.systemui.ImageWallpaper r0 = com.android.systemui.ImageWallpaper.this
                            android.os.HandlerThread r0 = r0.mWorker
                            android.os.Handler r0 = r0.getThreadHandler()
                            com.android.systemui.-$$Lambda$ImageWallpaper$GLEngine$1$fKBrTAP0A5mQK-jCeW566832sso r1 = new com.android.systemui.-$$Lambda$ImageWallpaper$GLEngine$1$fKBrTAP0A5mQK-jCeW566832sso
                            r1.<init>(r2)
                            r0.post(r1)
                        L_0x0035:
                            return
                        */
                        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.ImageWallpaper.GLEngine.AnonymousClass1.onKeyguardGoingAway():void");
                    }

                    public /* synthetic */ void lambda$onKeyguardGoingAway$0$ImageWallpaper$GLEngine$1() {
                        GLEngine.this.preRender();
                        GLEngine.this.mRenderer.startUnlockAnim(false, 800);
                        GLEngine.this.requestRender();
                        GLEngine.this.postRender();
                    }
                };
            }

            private void registerContentObserver() {
                this.mDarkModeUpdated = MiuiKeyguardUtils.isNightMode(this.mContext);
                this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("ui_night_mode"), false, this.mDarkModeObserver, -1);
                this.mDarkWallpaperModeUpdated = MiuiSettings.System.getBoolean(this.mContext.getContentResolver(), "darken_wallpaper_under_dark_mode", true);
                this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("darken_wallpaper_under_dark_mode"), false, this.mDarkWallpaperModeObserver, -1);
            }

            /* access modifiers changed from: private */
            public void rendererWallpaper() {
                preRender();
                this.mRenderer.updateDarkWallpaperMode(this.mDarkWallpaperModeUpdated && this.mDarkModeUpdated);
                requestRender();
                postRender();
            }

            private void updateWallpaperDarken() {
                if (KeyguardUpdateMonitor.getInstance(this.mContext).needDarkenWallpaper()) {
                    this.mRenderer.updateDarken(true);
                } else {
                    this.mRenderer.updateDarken(false);
                }
            }

            public void onCreate(SurfaceHolder surfaceHolder) {
                setFixedSizeAllowed(true);
                setOffsetNotificationsEnabled(true);
                updateSurfaceSize();
                KeyguardUpdateMonitor.getInstance(this.mContext).registerCallback(this.mKeyguardCallback);
            }

            private void updateSurfaceSize() {
                SurfaceHolder surfaceHolder = getSurfaceHolder();
                Size reportSurfaceSize = this.mRenderer.reportSurfaceSize();
                surfaceHolder.setFixedSize(Math.max(64, reportSurfaceSize.getWidth()), Math.max(64, reportSurfaceSize.getHeight()));
            }

            public /* synthetic */ void lambda$onOffsetsChanged$0$ImageWallpaper$GLEngine(float f, float f2) {
                this.mRenderer.updateOffsets(f, f2);
            }

            public void onOffsetsChanged(float f, float f2, float f3, float f4, int i, int i2) {
                ImageWallpaper.this.mWorker.getThreadHandler().post(new Runnable(f, f2) {
                    private final /* synthetic */ float f$1;
                    private final /* synthetic */ float f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    public final void run() {
                        ImageWallpaper.GLEngine.this.lambda$onOffsetsChanged$0$ImageWallpaper$GLEngine(this.f$1, this.f$2);
                    }
                });
            }

            public void onAmbientModeChanged(boolean z, long j) {
                if (this.mNeedTransition) {
                    ImageWallpaper.this.mWorker.getThreadHandler().post(new Runnable(z, j) {
                        private final /* synthetic */ boolean f$1;
                        private final /* synthetic */ long f$2;

                        {
                            this.f$1 = r2;
                            this.f$2 = r3;
                        }

                        public final void run() {
                            ImageWallpaper.GLEngine.this.lambda$onAmbientModeChanged$2$ImageWallpaper$GLEngine(this.f$1, this.f$2);
                        }
                    });
                    if (z && j == 0) {
                        waitForBackgroundRendering();
                    }
                }
            }

            public /* synthetic */ void lambda$onAmbientModeChanged$2$ImageWallpaper$GLEngine(boolean z, long j) {
                this.mRenderer.updateAmbientMode(z, j);
            }

            /* JADX WARNING: Missing exception handler attribute for start block: B:13:0x0022 */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            private void waitForBackgroundRendering() {
                /*
                    r7 = this;
                    java.lang.Object r0 = r7.mMonitor
                    monitor-enter(r0)
                    r1 = 0
                    r2 = 1
                    r7.mWaitingForRendering = r2     // Catch:{ InterruptedException -> 0x0022, all -> 0x0025 }
                    r3 = r2
                L_0x0008:
                    boolean r4 = r7.mWaitingForRendering     // Catch:{ InterruptedException -> 0x0022, all -> 0x0025 }
                    if (r4 == 0) goto L_0x0022
                    java.lang.Object r4 = r7.mMonitor     // Catch:{ InterruptedException -> 0x0022, all -> 0x0025 }
                    r5 = 100
                    r4.wait(r5)     // Catch:{ InterruptedException -> 0x0022, all -> 0x0025 }
                    boolean r4 = r7.mWaitingForRendering     // Catch:{ InterruptedException -> 0x0022, all -> 0x0025 }
                    r5 = 10
                    if (r3 >= r5) goto L_0x001b
                    r5 = r2
                    goto L_0x001c
                L_0x001b:
                    r5 = r1
                L_0x001c:
                    r4 = r4 & r5
                    r7.mWaitingForRendering = r4     // Catch:{ InterruptedException -> 0x0022, all -> 0x0025 }
                    int r3 = r3 + 1
                    goto L_0x0008
                L_0x0022:
                    r7.mWaitingForRendering = r1     // Catch:{ all -> 0x002b }
                    goto L_0x0029
                L_0x0025:
                    r2 = move-exception
                    r7.mWaitingForRendering = r1     // Catch:{ all -> 0x002b }
                    throw r2     // Catch:{ all -> 0x002b }
                L_0x0029:
                    monitor-exit(r0)     // Catch:{ all -> 0x002b }
                    return
                L_0x002b:
                    r7 = move-exception
                    monitor-exit(r0)     // Catch:{ all -> 0x002b }
                    throw r7
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.ImageWallpaper.GLEngine.waitForBackgroundRendering():void");
            }

            public void onDestroy() {
                StatusBarStateController statusBarStateController = this.mController;
                if (statusBarStateController != null) {
                    statusBarStateController.removeCallback(this);
                }
                this.mController = null;
                this.mContext.getContentResolver().unregisterContentObserver(this.mDarkModeObserver);
                this.mContext.getContentResolver().unregisterContentObserver(this.mDarkWallpaperModeObserver);
                KeyguardUpdateMonitor.getInstance(this.mContext).removeCallback(this.mKeyguardCallback);
                ImageWallpaper.this.mWorker.getThreadHandler().post(new Runnable() {
                    public final void run() {
                        ImageWallpaper.GLEngine.this.lambda$onDestroy$3$ImageWallpaper$GLEngine();
                    }
                });
            }

            public /* synthetic */ void lambda$onDestroy$3$ImageWallpaper$GLEngine() {
                this.mRenderer.finish();
                this.mRenderer = null;
                this.mEglHelper.finish();
                this.mEglHelper = null;
                getSurfaceHolder().getSurface().hwuiDestroy();
            }

            public void onSurfaceCreated(SurfaceHolder surfaceHolder) {
                ImageWallpaper.this.mWorker.getThreadHandler().post(new Runnable(surfaceHolder) {
                    private final /* synthetic */ SurfaceHolder f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void run() {
                        ImageWallpaper.GLEngine.this.lambda$onSurfaceCreated$4$ImageWallpaper$GLEngine(this.f$1);
                    }
                });
            }

            public /* synthetic */ void lambda$onSurfaceCreated$4$ImageWallpaper$GLEngine(SurfaceHolder surfaceHolder) {
                this.mEglHelper.init(surfaceHolder);
                this.mRenderer.onSurfaceCreated();
            }

            public void onSurfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {
                ImageWallpaper.this.mWorker.getThreadHandler().post(new Runnable(i2, i3) {
                    private final /* synthetic */ int f$1;
                    private final /* synthetic */ int f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    public final void run() {
                        ImageWallpaper.GLEngine.this.lambda$onSurfaceChanged$5$ImageWallpaper$GLEngine(this.f$1, this.f$2);
                    }
                });
            }

            public /* synthetic */ void lambda$onSurfaceChanged$5$ImageWallpaper$GLEngine(int i, int i2) {
                this.mRenderer.onSurfaceChanged(i, i2);
                this.mNeedRedraw = true;
            }

            public void onSurfaceRedrawNeeded(SurfaceHolder surfaceHolder) {
                ImageWallpaper.this.mWorker.getThreadHandler().post(new Runnable() {
                    public final void run() {
                        ImageWallpaper.GLEngine.this.lambda$onSurfaceRedrawNeeded$6$ImageWallpaper$GLEngine();
                    }
                });
            }

            public /* synthetic */ void lambda$onSurfaceRedrawNeeded$6$ImageWallpaper$GLEngine() {
                if (this.mNeedRedraw) {
                    preRender();
                    requestRender();
                    postRender();
                    this.mNeedRedraw = false;
                }
            }

            public void onStatePostChange() {
                if (this.mController.getState() == 0) {
                    ImageWallpaper.this.mWorker.getThreadHandler().post(new Runnable() {
                        public final void run() {
                            ImageWallpaper.GLEngine.this.scheduleFinishRendering();
                        }
                    });
                }
            }

            public void preRender() {
                preRenderInternal();
            }

            private void preRenderInternal() {
                boolean z;
                Rect surfaceFrame = getSurfaceHolder().getSurfaceFrame();
                cancelFinishRenderingTask();
                if (!this.mEglHelper.hasEglContext()) {
                    this.mEglHelper.destroyEglSurface();
                    if (!this.mEglHelper.createEglContext()) {
                        Log.w(ImageWallpaper.TAG, "recreate egl context failed!");
                    } else {
                        z = true;
                        if (this.mEglHelper.hasEglContext() && !this.mEglHelper.hasEglSurface() && !this.mEglHelper.createEglSurface(getSurfaceHolder())) {
                            Log.w(ImageWallpaper.TAG, "recreate egl surface failed!");
                        }
                        if (!this.mEglHelper.hasEglContext() && !this.mEglHelper.hasEglSurface() && z) {
                            this.mRenderer.onSurfaceCreated();
                            this.mRenderer.onSurfaceChanged(surfaceFrame.width(), surfaceFrame.height());
                            return;
                        }
                    }
                }
                z = false;
                Log.w(ImageWallpaper.TAG, "recreate egl surface failed!");
                if (!this.mEglHelper.hasEglContext() && !this.mEglHelper.hasEglSurface()) {
                }
            }

            public void requestRender() {
                requestRenderInternal();
            }

            private void requestRenderInternal() {
                Rect surfaceFrame = getSurfaceHolder().getSurfaceFrame();
                if (this.mEglHelper.hasEglContext() && this.mEglHelper.hasEglSurface() && surfaceFrame.width() > 0 && surfaceFrame.height() > 0) {
                    this.mRenderer.onDrawFrame();
                    if (!this.mEglHelper.swapBuffer()) {
                        Log.e(ImageWallpaper.TAG, "drawFrame failed!");
                        return;
                    }
                    return;
                }
                String access$700 = ImageWallpaper.TAG;
                Log.e(access$700, "requestRender: not ready, has context=" + this.mEglHelper.hasEglContext() + ", has surface=" + this.mEglHelper.hasEglSurface() + ", frame=" + surfaceFrame);
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
                throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.ImageWallpaper.GLEngine.notifyWaitingThread():void");
            }

            private void cancelFinishRenderingTask() {
                ImageWallpaper.this.mWorker.getThreadHandler().removeCallbacks(this.mFinishRenderingTask);
            }

            /* access modifiers changed from: private */
            public void scheduleFinishRendering() {
                cancelFinishRenderingTask();
                ImageWallpaper.this.mWorker.getThreadHandler().postDelayed(this.mFinishRenderingTask, 1000);
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
                printWriter.print("mNeedTransition=");
                printWriter.println(this.mNeedTransition);
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
