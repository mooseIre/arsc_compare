package com.android.systemui.classifier.brightline;

import android.app.ActivityManager;
import android.hardware.biometrics.BiometricSourceType;
import android.net.Uri;
import android.util.Log;
import android.view.MotionEvent;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.util.IndentingPrintWriter;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.systemui.dock.DockManager;
import com.android.systemui.plugins.FalsingManager;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.StatusBarState;
import com.android.systemui.util.DeviceConfigProxy;
import com.android.systemui.util.sensors.ProximitySensor;
import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.StringJoiner;
import java.util.function.Consumer;

public class BrightLineFalsingManager implements FalsingManager {
    static final boolean DEBUG = Log.isLoggable("FalsingManager", 3);
    private static final Queue<String> RECENT_INFO_LOG = new ArrayDeque(41);
    private static final Queue<DebugSwipeRecord> RECENT_SWIPES = new ArrayDeque(21);
    private final List<FalsingClassifier> mClassifiers;
    private final FalsingDataProvider mDataProvider;
    private final DockManager mDockManager;
    private int mIsFalseTouchCalls;
    /* access modifiers changed from: private */
    public boolean mJustUnlockedWithFace;
    private final KeyguardUpdateMonitorCallback mKeyguardUpdateCallback = new KeyguardUpdateMonitorCallback() {
        public void onBiometricAuthenticated(int i, BiometricSourceType biometricSourceType, boolean z) {
            if (i == KeyguardUpdateMonitor.getCurrentUser() && biometricSourceType == BiometricSourceType.FACE) {
                boolean unused = BrightLineFalsingManager.this.mJustUnlockedWithFace = true;
            }
        }
    };
    private final KeyguardUpdateMonitor mKeyguardUpdateMonitor;
    private MetricsLogger mMetricsLogger;
    private boolean mPreviousResult = false;
    private final ProximitySensor mProximitySensor;
    private boolean mScreenOn;
    private ProximitySensor.ProximitySensorListener mSensorEventListener = new ProximitySensor.ProximitySensorListener() {
        public final void onSensorEvent(ProximitySensor.ProximityEvent proximityEvent) {
            BrightLineFalsingManager.this.onProximityEvent(proximityEvent);
        }
    };
    private boolean mSessionStarted;
    private boolean mShowingAod;
    /* access modifiers changed from: private */
    public int mState;
    private final StatusBarStateController mStatusBarStateController;
    private StatusBarStateController.StateListener mStatusBarStateListener = new StatusBarStateController.StateListener() {
        public void onStateChanged(int i) {
            BrightLineFalsingManager.logDebug("StatusBarState=" + StatusBarState.toShortString(i));
            int unused = BrightLineFalsingManager.this.mState = i;
            BrightLineFalsingManager.this.updateSessionActive();
        }
    };

    public boolean isClassifierEnabled() {
        return true;
    }

    public boolean isReportingEnabled() {
        return false;
    }

    public boolean isUnlockingDisabled() {
        return false;
    }

    public void onAffordanceSwipingAborted() {
    }

    public void onCameraHintStarted() {
    }

    public void onCameraOn() {
    }

    public void onExpansionFromPulseStopped() {
    }

    public void onLeftAffordanceHintStarted() {
    }

    public void onLeftAffordanceOn() {
    }

    public void onNotificationActive() {
    }

    public void onNotificationDismissed() {
    }

    public void onNotificationDoubleTap(boolean z, float f, float f2) {
    }

    public void onNotificationStopDismissing() {
    }

    public void onNotificatonStopDraggingDown() {
    }

    public void onTrackingStopped() {
    }

    public void onUnlockHintStarted() {
    }

    public Uri reportRejectedTouch() {
        return null;
    }

    public void setNotificationExpanded() {
    }

    public boolean shouldEnforceBouncer() {
        return false;
    }

    public BrightLineFalsingManager(FalsingDataProvider falsingDataProvider, KeyguardUpdateMonitor keyguardUpdateMonitor, ProximitySensor proximitySensor, DeviceConfigProxy deviceConfigProxy, DockManager dockManager, StatusBarStateController statusBarStateController) {
        this.mKeyguardUpdateMonitor = keyguardUpdateMonitor;
        this.mDataProvider = falsingDataProvider;
        this.mProximitySensor = proximitySensor;
        this.mDockManager = dockManager;
        this.mStatusBarStateController = statusBarStateController;
        keyguardUpdateMonitor.registerCallback(this.mKeyguardUpdateCallback);
        this.mStatusBarStateController.addCallback(this.mStatusBarStateListener);
        this.mState = this.mStatusBarStateController.getState();
        this.mMetricsLogger = new MetricsLogger();
        this.mClassifiers = new ArrayList();
        DistanceClassifier distanceClassifier = new DistanceClassifier(this.mDataProvider, deviceConfigProxy);
        ProximityClassifier proximityClassifier = new ProximityClassifier(distanceClassifier, this.mDataProvider, deviceConfigProxy);
        this.mClassifiers.add(new PointerCountClassifier(this.mDataProvider));
        this.mClassifiers.add(new TypeClassifier(this.mDataProvider));
        this.mClassifiers.add(new DiagonalClassifier(this.mDataProvider, deviceConfigProxy));
        this.mClassifiers.add(distanceClassifier);
        this.mClassifiers.add(proximityClassifier);
        this.mClassifiers.add(new ZigZagClassifier(this.mDataProvider, deviceConfigProxy));
    }

    private void registerSensors() {
        this.mProximitySensor.register(this.mSensorEventListener);
    }

    private void unregisterSensors() {
        this.mProximitySensor.unregister(this.mSensorEventListener);
    }

    private void sessionStart() {
        if (!this.mSessionStarted && shouldSessionBeActive()) {
            logDebug("Starting Session");
            this.mSessionStarted = true;
            this.mJustUnlockedWithFace = false;
            registerSensors();
            this.mClassifiers.forEach($$Lambda$HclOlu42IVtKALxwbwHP3Y1rdRk.INSTANCE);
        }
    }

    private void sessionEnd() {
        if (this.mSessionStarted) {
            logDebug("Ending Session");
            this.mSessionStarted = false;
            unregisterSensors();
            this.mDataProvider.onSessionEnd();
            this.mClassifiers.forEach($$Lambda$47wU6WxQ76Gt_ecwypSCrFl04Q.INSTANCE);
            int i = this.mIsFalseTouchCalls;
            if (i != 0) {
                this.mMetricsLogger.histogram("falsing_failure_after_attempts", i);
                this.mIsFalseTouchCalls = 0;
            }
        }
    }

    /* access modifiers changed from: private */
    public void updateSessionActive() {
        if (shouldSessionBeActive()) {
            sessionStart();
        } else {
            sessionEnd();
        }
    }

    private boolean shouldSessionBeActive() {
        return this.mScreenOn && this.mState == 1 && !this.mShowingAod;
    }

    private void updateInteractionType(int i) {
        logDebug("InteractionType: " + i);
        this.mDataProvider.setInteractionType(i);
    }

    public boolean isFalseTouch() {
        if (!this.mDataProvider.isDirty()) {
            return this.mPreviousResult;
        }
        this.mPreviousResult = !ActivityManager.isRunningInUserTestHarness() && !this.mJustUnlockedWithFace && !this.mDockManager.isDocked() && this.mClassifiers.stream().anyMatch(
        /*  JADX ERROR: Method code generation error
            jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x0031: IPUT  
              (wrap: boolean : ?: TERNARY(r0v3 boolean) = (((wrap: boolean : 0x000b: INVOKE  (r0v2 boolean) =  android.app.ActivityManager.isRunningInUserTestHarness():boolean type: STATIC) == false && (wrap: boolean : 0x0011: IGET  (r0v15 boolean) = 
              (r6v0 'this' com.android.systemui.classifier.brightline.BrightLineFalsingManager A[THIS])
             com.android.systemui.classifier.brightline.BrightLineFalsingManager.mJustUnlockedWithFace boolean) == false && (wrap: boolean : 0x0017: INVOKE  (r0v17 boolean) = 
              (wrap: com.android.systemui.dock.DockManager : 0x0015: IGET  (r0v16 com.android.systemui.dock.DockManager) = 
              (r6v0 'this' com.android.systemui.classifier.brightline.BrightLineFalsingManager A[THIS])
             com.android.systemui.classifier.brightline.BrightLineFalsingManager.mDockManager com.android.systemui.dock.DockManager)
             com.android.systemui.dock.DockManager.isDocked():boolean type: INTERFACE) == false && (wrap: boolean : 0x0028: INVOKE  (r0v20 boolean) = 
              (wrap: java.util.stream.Stream : 0x001f: INVOKE  (r0v19 java.util.stream.Stream) = 
              (wrap: java.util.List<com.android.systemui.classifier.brightline.FalsingClassifier> : 0x001d: IGET  (r0v18 java.util.List<com.android.systemui.classifier.brightline.FalsingClassifier>) = 
              (r6v0 'this' com.android.systemui.classifier.brightline.BrightLineFalsingManager A[THIS])
             com.android.systemui.classifier.brightline.BrightLineFalsingManager.mClassifiers java.util.List)
             java.util.List.stream():java.util.stream.Stream type: INTERFACE)
              (wrap: com.android.systemui.classifier.brightline.-$$Lambda$BrightLineFalsingManager$a2Ll-_HVGMZ_iA7riIG6wQYElYM : 0x0025: CONSTRUCTOR  (r1v4 com.android.systemui.classifier.brightline.-$$Lambda$BrightLineFalsingManager$a2Ll-_HVGMZ_iA7riIG6wQYElYM) = 
              (r6v0 'this' com.android.systemui.classifier.brightline.BrightLineFalsingManager A[THIS])
             call: com.android.systemui.classifier.brightline.-$$Lambda$BrightLineFalsingManager$a2Ll-_HVGMZ_iA7riIG6wQYElYM.<init>(com.android.systemui.classifier.brightline.BrightLineFalsingManager):void type: CONSTRUCTOR)
             java.util.stream.Stream.anyMatch(java.util.function.Predicate):boolean type: INTERFACE) == true)) ? true : false)
              (r6v0 'this' com.android.systemui.classifier.brightline.BrightLineFalsingManager A[THIS])
             com.android.systemui.classifier.brightline.BrightLineFalsingManager.mPreviousResult boolean in method: com.android.systemui.classifier.brightline.BrightLineFalsingManager.isFalseTouch():boolean, dex: classes.dex
            	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:256)
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
            	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:78)
            	at jadx.core.codegen.CodeGen.wrapCodeGen(CodeGen.java:44)
            	at jadx.core.codegen.CodeGen.generateJavaCode(CodeGen.java:33)
            	at jadx.core.codegen.CodeGen.generate(CodeGen.java:21)
            	at jadx.core.ProcessClass.generateCode(ProcessClass.java:61)
            	at jadx.core.dex.nodes.ClassNode.decompile(ClassNode.java:273)
            Caused by: jadx.core.utils.exceptions.CodegenException: Error generate insn: ?: TERNARY(r0v3 boolean) = (((wrap: boolean : 0x000b: INVOKE  (r0v2 boolean) =  android.app.ActivityManager.isRunningInUserTestHarness():boolean type: STATIC) == false && (wrap: boolean : 0x0011: IGET  (r0v15 boolean) = 
              (r6v0 'this' com.android.systemui.classifier.brightline.BrightLineFalsingManager A[THIS])
             com.android.systemui.classifier.brightline.BrightLineFalsingManager.mJustUnlockedWithFace boolean) == false && (wrap: boolean : 0x0017: INVOKE  (r0v17 boolean) = 
              (wrap: com.android.systemui.dock.DockManager : 0x0015: IGET  (r0v16 com.android.systemui.dock.DockManager) = 
              (r6v0 'this' com.android.systemui.classifier.brightline.BrightLineFalsingManager A[THIS])
             com.android.systemui.classifier.brightline.BrightLineFalsingManager.mDockManager com.android.systemui.dock.DockManager)
             com.android.systemui.dock.DockManager.isDocked():boolean type: INTERFACE) == false && (wrap: boolean : 0x0028: INVOKE  (r0v20 boolean) = 
              (wrap: java.util.stream.Stream : 0x001f: INVOKE  (r0v19 java.util.stream.Stream) = 
              (wrap: java.util.List<com.android.systemui.classifier.brightline.FalsingClassifier> : 0x001d: IGET  (r0v18 java.util.List<com.android.systemui.classifier.brightline.FalsingClassifier>) = 
              (r6v0 'this' com.android.systemui.classifier.brightline.BrightLineFalsingManager A[THIS])
             com.android.systemui.classifier.brightline.BrightLineFalsingManager.mClassifiers java.util.List)
             java.util.List.stream():java.util.stream.Stream type: INTERFACE)
              (wrap: com.android.systemui.classifier.brightline.-$$Lambda$BrightLineFalsingManager$a2Ll-_HVGMZ_iA7riIG6wQYElYM : 0x0025: CONSTRUCTOR  (r1v4 com.android.systemui.classifier.brightline.-$$Lambda$BrightLineFalsingManager$a2Ll-_HVGMZ_iA7riIG6wQYElYM) = 
              (r6v0 'this' com.android.systemui.classifier.brightline.BrightLineFalsingManager A[THIS])
             call: com.android.systemui.classifier.brightline.-$$Lambda$BrightLineFalsingManager$a2Ll-_HVGMZ_iA7riIG6wQYElYM.<init>(com.android.systemui.classifier.brightline.BrightLineFalsingManager):void type: CONSTRUCTOR)
             java.util.stream.Stream.anyMatch(java.util.function.Predicate):boolean type: INTERFACE) == true)) ? true : false in method: com.android.systemui.classifier.brightline.BrightLineFalsingManager.isFalseTouch():boolean, dex: classes.dex
            	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:256)
            	at jadx.core.codegen.InsnGen.addWrappedArg(InsnGen.java:123)
            	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:107)
            	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:429)
            	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:250)
            	... 33 more
            Caused by: jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x0028: INVOKE  (r0v20 boolean) = 
              (wrap: java.util.stream.Stream : 0x001f: INVOKE  (r0v19 java.util.stream.Stream) = 
              (wrap: java.util.List<com.android.systemui.classifier.brightline.FalsingClassifier> : 0x001d: IGET  (r0v18 java.util.List<com.android.systemui.classifier.brightline.FalsingClassifier>) = 
              (r6v0 'this' com.android.systemui.classifier.brightline.BrightLineFalsingManager A[THIS])
             com.android.systemui.classifier.brightline.BrightLineFalsingManager.mClassifiers java.util.List)
             java.util.List.stream():java.util.stream.Stream type: INTERFACE)
              (wrap: com.android.systemui.classifier.brightline.-$$Lambda$BrightLineFalsingManager$a2Ll-_HVGMZ_iA7riIG6wQYElYM : 0x0025: CONSTRUCTOR  (r1v4 com.android.systemui.classifier.brightline.-$$Lambda$BrightLineFalsingManager$a2Ll-_HVGMZ_iA7riIG6wQYElYM) = 
              (r6v0 'this' com.android.systemui.classifier.brightline.BrightLineFalsingManager A[THIS])
             call: com.android.systemui.classifier.brightline.-$$Lambda$BrightLineFalsingManager$a2Ll-_HVGMZ_iA7riIG6wQYElYM.<init>(com.android.systemui.classifier.brightline.BrightLineFalsingManager):void type: CONSTRUCTOR)
             java.util.stream.Stream.anyMatch(java.util.function.Predicate):boolean type: INTERFACE in method: com.android.systemui.classifier.brightline.BrightLineFalsingManager.isFalseTouch():boolean, dex: classes.dex
            	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:256)
            	at jadx.core.codegen.InsnGen.addWrappedArg(InsnGen.java:123)
            	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:107)
            	at jadx.core.codegen.ConditionGen.wrap(ConditionGen.java:95)
            	at jadx.core.codegen.ConditionGen.addCompare(ConditionGen.java:117)
            	at jadx.core.codegen.ConditionGen.add(ConditionGen.java:57)
            	at jadx.core.codegen.ConditionGen.wrap(ConditionGen.java:84)
            	at jadx.core.codegen.ConditionGen.addAndOr(ConditionGen.java:151)
            	at jadx.core.codegen.ConditionGen.add(ConditionGen.java:70)
            	at jadx.core.codegen.ConditionGen.add(ConditionGen.java:46)
            	at jadx.core.codegen.InsnGen.makeTernary(InsnGen.java:948)
            	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:476)
            	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:231)
            	... 37 more
            Caused by: jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x0025: CONSTRUCTOR  (r1v4 com.android.systemui.classifier.brightline.-$$Lambda$BrightLineFalsingManager$a2Ll-_HVGMZ_iA7riIG6wQYElYM) = 
              (r6v0 'this' com.android.systemui.classifier.brightline.BrightLineFalsingManager A[THIS])
             call: com.android.systemui.classifier.brightline.-$$Lambda$BrightLineFalsingManager$a2Ll-_HVGMZ_iA7riIG6wQYElYM.<init>(com.android.systemui.classifier.brightline.BrightLineFalsingManager):void type: CONSTRUCTOR in method: com.android.systemui.classifier.brightline.BrightLineFalsingManager.isFalseTouch():boolean, dex: classes.dex
            	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:256)
            	at jadx.core.codegen.InsnGen.addWrappedArg(InsnGen.java:123)
            	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:107)
            	at jadx.core.codegen.InsnGen.generateMethodArguments(InsnGen.java:787)
            	at jadx.core.codegen.InsnGen.makeInvoke(InsnGen.java:728)
            	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:368)
            	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:231)
            	... 49 more
            Caused by: jadx.core.utils.exceptions.JadxRuntimeException: Expected class to be processed at this point, class: com.android.systemui.classifier.brightline.-$$Lambda$BrightLineFalsingManager$a2Ll-_HVGMZ_iA7riIG6wQYElYM, state: NOT_LOADED
            	at jadx.core.dex.nodes.ClassNode.ensureProcessed(ClassNode.java:260)
            	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:606)
            	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:364)
            	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:231)
            	... 55 more
            */
        /*
            this = this;
            com.android.systemui.classifier.brightline.FalsingDataProvider r0 = r6.mDataProvider
            boolean r0 = r0.isDirty()
            if (r0 != 0) goto L_0x000b
            boolean r6 = r6.mPreviousResult
            return r6
        L_0x000b:
            boolean r0 = android.app.ActivityManager.isRunningInUserTestHarness()
            if (r0 != 0) goto L_0x0030
            boolean r0 = r6.mJustUnlockedWithFace
            if (r0 != 0) goto L_0x0030
            com.android.systemui.dock.DockManager r0 = r6.mDockManager
            boolean r0 = r0.isDocked()
            if (r0 != 0) goto L_0x0030
            java.util.List<com.android.systemui.classifier.brightline.FalsingClassifier> r0 = r6.mClassifiers
            java.util.stream.Stream r0 = r0.stream()
            com.android.systemui.classifier.brightline.-$$Lambda$BrightLineFalsingManager$a2Ll-_HVGMZ_iA7riIG6wQYElYM r1 = new com.android.systemui.classifier.brightline.-$$Lambda$BrightLineFalsingManager$a2Ll-_HVGMZ_iA7riIG6wQYElYM
            r1.<init>(r6)
            boolean r0 = r0.anyMatch(r1)
            if (r0 == 0) goto L_0x0030
            r0 = 1
            goto L_0x0031
        L_0x0030:
            r0 = 0
        L_0x0031:
            r6.mPreviousResult = r0
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r1 = "Is false touch? "
            r0.append(r1)
            boolean r1 = r6.mPreviousResult
            r0.append(r1)
            java.lang.String r0 = r0.toString()
            logDebug(r0)
            boolean r0 = android.os.Build.IS_ENG
            if (r0 != 0) goto L_0x0051
            boolean r0 = android.os.Build.IS_USERDEBUG
            if (r0 == 0) goto L_0x0090
        L_0x0051:
            java.util.Queue<com.android.systemui.classifier.brightline.BrightLineFalsingManager$DebugSwipeRecord> r0 = RECENT_SWIPES
            com.android.systemui.classifier.brightline.BrightLineFalsingManager$DebugSwipeRecord r1 = new com.android.systemui.classifier.brightline.BrightLineFalsingManager$DebugSwipeRecord
            boolean r2 = r6.mPreviousResult
            com.android.systemui.classifier.brightline.FalsingDataProvider r3 = r6.mDataProvider
            int r3 = r3.getInteractionType()
            com.android.systemui.classifier.brightline.FalsingDataProvider r4 = r6.mDataProvider
            java.util.List r4 = r4.getRecentMotionEvents()
            java.util.stream.Stream r4 = r4.stream()
            com.android.systemui.classifier.brightline.-$$Lambda$BrightLineFalsingManager$CaQ6cuS9SHkQ1By76SF5W8vub7I r5 = com.android.systemui.classifier.brightline.$$Lambda$BrightLineFalsingManager$CaQ6cuS9SHkQ1By76SF5W8vub7I.INSTANCE
            java.util.stream.Stream r4 = r4.map(r5)
            java.util.stream.Collector r5 = java.util.stream.Collectors.toList()
            java.lang.Object r4 = r4.collect(r5)
            java.util.List r4 = (java.util.List) r4
            r1.<init>(r2, r3, r4)
            r0.add(r1)
        L_0x007d:
            java.util.Queue<com.android.systemui.classifier.brightline.BrightLineFalsingManager$DebugSwipeRecord> r0 = RECENT_SWIPES
            int r0 = r0.size()
            r1 = 40
            if (r0 <= r1) goto L_0x0090
            java.util.Queue<com.android.systemui.classifier.brightline.BrightLineFalsingManager$DebugSwipeRecord> r0 = RECENT_SWIPES
            java.lang.Object r0 = r0.remove()
            com.android.systemui.classifier.brightline.BrightLineFalsingManager$DebugSwipeRecord r0 = (com.android.systemui.classifier.brightline.BrightLineFalsingManager.DebugSwipeRecord) r0
            goto L_0x007d
        L_0x0090:
            boolean r6 = r6.mPreviousResult
            return r6
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.classifier.brightline.BrightLineFalsingManager.isFalseTouch():boolean");
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$isFalseTouch$0 */
    public /* synthetic */ boolean lambda$isFalseTouch$0$BrightLineFalsingManager(FalsingClassifier falsingClassifier) {
        boolean isFalseTouch = falsingClassifier.isFalseTouch();
        if (isFalseTouch) {
            logInfo(String.format((Locale) null, "{classifier=%s, interactionType=%d}", new Object[]{falsingClassifier.getClass().getName(), Integer.valueOf(this.mDataProvider.getInteractionType())}));
            String reason = falsingClassifier.getReason();
            if (reason != null) {
                logInfo(reason);
            }
        } else {
            logDebug(falsingClassifier.getClass().getName() + ": false");
        }
        return isFalseTouch;
    }

    static /* synthetic */ XYDt lambda$isFalseTouch$1(MotionEvent motionEvent) {
        return new XYDt((int) motionEvent.getX(), (int) motionEvent.getY(), (int) (motionEvent.getEventTime() - motionEvent.getDownTime()));
    }

    public void onTouchEvent(MotionEvent motionEvent, int i, int i2) {
        this.mDataProvider.onMotionEvent(motionEvent);
        this.mClassifiers.forEach(new Consumer(motionEvent) {
            public final /* synthetic */ MotionEvent f$0;

            {
                this.f$0 = r1;
            }

            public final void accept(Object obj) {
                ((FalsingClassifier) obj).onTouchEvent(this.f$0);
            }
        });
    }

    /* access modifiers changed from: private */
    public void onProximityEvent(ProximitySensor.ProximityEvent proximityEvent) {
        this.mClassifiers.forEach(new Consumer() {
            public final void accept(Object obj) {
                ((FalsingClassifier) obj).onProximityEvent(ProximitySensor.ProximityEvent.this);
            }
        });
    }

    public void onSuccessfulUnlock() {
        int i = this.mIsFalseTouchCalls;
        if (i != 0) {
            this.mMetricsLogger.histogram("falsing_success_after_attempts", i);
            this.mIsFalseTouchCalls = 0;
        }
        sessionEnd();
    }

    public void setShowingAod(boolean z) {
        this.mShowingAod = z;
        updateSessionActive();
    }

    public void onNotificatonStartDraggingDown() {
        updateInteractionType(2);
    }

    public void onQsDown() {
        updateInteractionType(0);
    }

    public void setQsExpanded(boolean z) {
        if (z) {
            unregisterSensors();
        } else if (this.mSessionStarted) {
            registerSensors();
        }
    }

    public void onTrackingStarted(boolean z) {
        updateInteractionType(z ? 8 : 4);
    }

    public void onAffordanceSwipingStarted(boolean z) {
        updateInteractionType(z ? 6 : 5);
    }

    public void onStartExpandingFromPulse() {
        updateInteractionType(9);
    }

    public void onScreenOnFromTouch() {
        onScreenTurningOn();
    }

    public void onScreenTurningOn() {
        this.mScreenOn = true;
        updateSessionActive();
    }

    public void onScreenOff() {
        this.mScreenOn = false;
        updateSessionActive();
    }

    public void onNotificationStartDismissing() {
        updateInteractionType(1);
    }

    public void onBouncerShown() {
        unregisterSensors();
    }

    public void onBouncerHidden() {
        if (this.mSessionStarted) {
            registerSensors();
        }
    }

    public void dump(PrintWriter printWriter) {
        IndentingPrintWriter indentingPrintWriter = new IndentingPrintWriter(printWriter, "  ");
        indentingPrintWriter.println("BRIGHTLINE FALSING MANAGER");
        indentingPrintWriter.print("classifierEnabled=");
        indentingPrintWriter.println(isClassifierEnabled() ? 1 : 0);
        indentingPrintWriter.print("mJustUnlockedWithFace=");
        indentingPrintWriter.println(this.mJustUnlockedWithFace ? 1 : 0);
        indentingPrintWriter.print("isDocked=");
        indentingPrintWriter.println(this.mDockManager.isDocked() ? 1 : 0);
        indentingPrintWriter.print("width=");
        indentingPrintWriter.println(this.mDataProvider.getWidthPixels());
        indentingPrintWriter.print("height=");
        indentingPrintWriter.println(this.mDataProvider.getHeightPixels());
        indentingPrintWriter.println();
        if (RECENT_SWIPES.size() != 0) {
            indentingPrintWriter.println("Recent swipes:");
            indentingPrintWriter.increaseIndent();
            for (DebugSwipeRecord string : RECENT_SWIPES) {
                indentingPrintWriter.println(string.getString());
                indentingPrintWriter.println();
            }
            indentingPrintWriter.decreaseIndent();
        } else {
            indentingPrintWriter.println("No recent swipes");
        }
        indentingPrintWriter.println();
        indentingPrintWriter.println("Recent falsing info:");
        indentingPrintWriter.increaseIndent();
        for (String println : RECENT_INFO_LOG) {
            indentingPrintWriter.println(println);
        }
        indentingPrintWriter.println();
    }

    public void cleanup() {
        unregisterSensors();
        this.mKeyguardUpdateMonitor.removeCallback(this.mKeyguardUpdateCallback);
        this.mStatusBarStateController.removeCallback(this.mStatusBarStateListener);
    }

    static void logDebug(String str) {
        logDebug(str, (Throwable) null);
    }

    static void logDebug(String str, Throwable th) {
        if (DEBUG) {
            Log.d("FalsingManager", str, th);
        }
    }

    static void logInfo(String str) {
        Log.i("FalsingManager", str);
        RECENT_INFO_LOG.add(str);
        while (RECENT_INFO_LOG.size() > 40) {
            RECENT_INFO_LOG.remove();
        }
    }

    private static class DebugSwipeRecord {
        private final int mInteractionType;
        private final boolean mIsFalse;
        private final List<XYDt> mRecentMotionEvents;

        DebugSwipeRecord(boolean z, int i, List<XYDt> list) {
            this.mIsFalse = z;
            this.mInteractionType = i;
            this.mRecentMotionEvents = list;
        }

        /* access modifiers changed from: package-private */
        public String getString() {
            StringJoiner stringJoiner = new StringJoiner(",");
            stringJoiner.add(Integer.toString(1)).add(this.mIsFalse ? "1" : "0").add(Integer.toString(this.mInteractionType));
            for (XYDt xYDt : this.mRecentMotionEvents) {
                stringJoiner.add(xYDt.toString());
            }
            return stringJoiner.toString();
        }
    }

    private static class XYDt {
        private final int mDT;
        private final int mX;
        private final int mY;

        XYDt(int i, int i2, int i3) {
            this.mX = i;
            this.mY = i2;
            this.mDT = i3;
        }

        public String toString() {
            return this.mX + "," + this.mY + "," + this.mDT;
        }
    }
}
