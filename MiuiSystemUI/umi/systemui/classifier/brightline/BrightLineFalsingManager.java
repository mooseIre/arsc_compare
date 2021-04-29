package com.android.systemui.classifier.brightline;

import android.app.ActivityManager;
import android.hardware.biometrics.BiometricSourceType;
import android.net.Uri;
import android.os.Build;
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
import java.util.Queue;
import java.util.StringJoiner;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class BrightLineFalsingManager implements FalsingManager {
    static final boolean DEBUG = Log.isLoggable("FalsingManager", 3);
    private static final Queue<String> RECENT_INFO_LOG = new ArrayDeque(41);
    private static final Queue<DebugSwipeRecord> RECENT_SWIPES = new ArrayDeque(21);
    private final List<FalsingClassifier> mClassifiers;
    private final FalsingDataProvider mDataProvider;
    private final DockManager mDockManager;
    private int mIsFalseTouchCalls;
    private boolean mJustUnlockedWithFace;
    private final KeyguardUpdateMonitorCallback mKeyguardUpdateCallback = new KeyguardUpdateMonitorCallback() {
        /* class com.android.systemui.classifier.brightline.BrightLineFalsingManager.AnonymousClass1 */

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onBiometricAuthenticated(int i, BiometricSourceType biometricSourceType, boolean z) {
            if (i == KeyguardUpdateMonitor.getCurrentUser() && biometricSourceType == BiometricSourceType.FACE) {
                BrightLineFalsingManager.this.mJustUnlockedWithFace = true;
            }
        }
    };
    private final KeyguardUpdateMonitor mKeyguardUpdateMonitor;
    private MetricsLogger mMetricsLogger;
    private boolean mPreviousResult = false;
    private final ProximitySensor mProximitySensor;
    private boolean mScreenOn;
    private ProximitySensor.ProximitySensorListener mSensorEventListener = new ProximitySensor.ProximitySensorListener() {
        /* class com.android.systemui.classifier.brightline.$$Lambda$BrightLineFalsingManager$DCb2WK5QgVL78Az07qEbZU0x84o */

        @Override // com.android.systemui.util.sensors.ProximitySensor.ProximitySensorListener
        public final void onSensorEvent(ProximitySensor.ProximityEvent proximityEvent) {
            BrightLineFalsingManager.this.onProximityEvent(proximityEvent);
        }
    };
    private boolean mSessionStarted;
    private boolean mShowingAod;
    private int mState;
    private final StatusBarStateController mStatusBarStateController;
    private StatusBarStateController.StateListener mStatusBarStateListener = new StatusBarStateController.StateListener() {
        /* class com.android.systemui.classifier.brightline.BrightLineFalsingManager.AnonymousClass2 */

        @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
        public void onStateChanged(int i) {
            BrightLineFalsingManager.logDebug("StatusBarState=" + StatusBarState.toShortString(i));
            BrightLineFalsingManager.this.mState = i;
            BrightLineFalsingManager.this.updateSessionActive();
        }
    };

    @Override // com.android.systemui.plugins.FalsingManager
    public boolean isClassifierEnabled() {
        return true;
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public boolean isReportingEnabled() {
        return false;
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public boolean isUnlockingDisabled() {
        return false;
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onAffordanceSwipingAborted() {
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onCameraHintStarted() {
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onCameraOn() {
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onExpansionFromPulseStopped() {
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onLeftAffordanceHintStarted() {
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onLeftAffordanceOn() {
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onNotificationActive() {
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onNotificationDismissed() {
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onNotificationDoubleTap(boolean z, float f, float f2) {
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onNotificationStopDismissing() {
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onNotificatonStopDraggingDown() {
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onTrackingStopped() {
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onUnlockHintStarted() {
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public Uri reportRejectedTouch() {
        return null;
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void setNotificationExpanded() {
    }

    @Override // com.android.systemui.plugins.FalsingManager
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
    /* access modifiers changed from: public */
    private void updateSessionActive() {
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

    @Override // com.android.systemui.plugins.FalsingManager
    public boolean isFalseTouch() {
        if (!this.mDataProvider.isDirty()) {
            return this.mPreviousResult;
        }
        this.mPreviousResult = !ActivityManager.isRunningInUserTestHarness() && !this.mJustUnlockedWithFace && !this.mDockManager.isDocked() && this.mClassifiers.stream().anyMatch(new Predicate() {
            /* class com.android.systemui.classifier.brightline.$$Lambda$BrightLineFalsingManager$a2Ll_HVGMZ_iA7riIG6wQYElYM */

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return BrightLineFalsingManager.this.lambda$isFalseTouch$0$BrightLineFalsingManager((FalsingClassifier) obj);
            }
        });
        logDebug("Is false touch? " + this.mPreviousResult);
        if (Build.IS_ENG || Build.IS_USERDEBUG) {
            RECENT_SWIPES.add(new DebugSwipeRecord(this.mPreviousResult, this.mDataProvider.getInteractionType(), (List) this.mDataProvider.getRecentMotionEvents().stream().map($$Lambda$BrightLineFalsingManager$CaQ6cuS9SHkQ1By76SF5W8vub7I.INSTANCE).collect(Collectors.toList())));
            while (RECENT_SWIPES.size() > 40) {
                RECENT_SWIPES.remove();
            }
        }
        return this.mPreviousResult;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$isFalseTouch$0 */
    public /* synthetic */ boolean lambda$isFalseTouch$0$BrightLineFalsingManager(FalsingClassifier falsingClassifier) {
        boolean isFalseTouch = falsingClassifier.isFalseTouch();
        if (isFalseTouch) {
            logInfo(String.format(null, "{classifier=%s, interactionType=%d}", falsingClassifier.getClass().getName(), Integer.valueOf(this.mDataProvider.getInteractionType())));
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

    @Override // com.android.systemui.plugins.FalsingManager
    public void onTouchEvent(MotionEvent motionEvent, int i, int i2) {
        this.mDataProvider.onMotionEvent(motionEvent);
        this.mClassifiers.forEach(new Consumer(motionEvent) {
            /* class com.android.systemui.classifier.brightline.$$Lambda$BrightLineFalsingManager$dqBtGf6PUXlUGyEertsddqo7Kg */
            public final /* synthetic */ MotionEvent f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                ((FalsingClassifier) obj).onTouchEvent(this.f$0);
            }
        });
    }

    /* access modifiers changed from: private */
    public void onProximityEvent(ProximitySensor.ProximityEvent proximityEvent) {
        this.mClassifiers.forEach(new Consumer() {
            /* class com.android.systemui.classifier.brightline.$$Lambda$BrightLineFalsingManager$_d89p1tVOz6Jf4LOgqm74DRgw1s */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                ((FalsingClassifier) obj).onProximityEvent(ProximitySensor.ProximityEvent.this);
            }
        });
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onSuccessfulUnlock() {
        int i = this.mIsFalseTouchCalls;
        if (i != 0) {
            this.mMetricsLogger.histogram("falsing_success_after_attempts", i);
            this.mIsFalseTouchCalls = 0;
        }
        sessionEnd();
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void setShowingAod(boolean z) {
        this.mShowingAod = z;
        updateSessionActive();
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onNotificatonStartDraggingDown() {
        updateInteractionType(2);
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onQsDown() {
        updateInteractionType(0);
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void setQsExpanded(boolean z) {
        if (z) {
            unregisterSensors();
        } else if (this.mSessionStarted) {
            registerSensors();
        }
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onTrackingStarted(boolean z) {
        updateInteractionType(z ? 8 : 4);
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onAffordanceSwipingStarted(boolean z) {
        updateInteractionType(z ? 6 : 5);
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onStartExpandingFromPulse() {
        updateInteractionType(9);
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onScreenOnFromTouch() {
        onScreenTurningOn();
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onScreenTurningOn() {
        this.mScreenOn = true;
        updateSessionActive();
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onScreenOff() {
        this.mScreenOn = false;
        updateSessionActive();
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onNotificationStartDismissing() {
        updateInteractionType(1);
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onBouncerShown() {
        unregisterSensors();
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onBouncerHidden() {
        if (this.mSessionStarted) {
            registerSensors();
        }
    }

    @Override // com.android.systemui.plugins.FalsingManager
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
            for (DebugSwipeRecord debugSwipeRecord : RECENT_SWIPES) {
                indentingPrintWriter.println(debugSwipeRecord.getString());
                indentingPrintWriter.println();
            }
            indentingPrintWriter.decreaseIndent();
        } else {
            indentingPrintWriter.println("No recent swipes");
        }
        indentingPrintWriter.println();
        indentingPrintWriter.println("Recent falsing info:");
        indentingPrintWriter.increaseIndent();
        for (String str : RECENT_INFO_LOG) {
            indentingPrintWriter.println(str);
        }
        indentingPrintWriter.println();
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void cleanup() {
        unregisterSensors();
        this.mKeyguardUpdateMonitor.removeCallback(this.mKeyguardUpdateCallback);
        this.mStatusBarStateController.removeCallback(this.mStatusBarStateListener);
    }

    static void logDebug(String str) {
        logDebug(str, null);
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

    /* access modifiers changed from: private */
    public static class XYDt {
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
