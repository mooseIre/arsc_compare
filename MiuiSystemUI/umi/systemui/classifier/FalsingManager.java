package com.android.systemui.classifier;

import android.content.Context;
import android.database.ContentObserver;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.MotionEvent;
import android.view.accessibility.AccessibilityManager;
import com.android.systemui.Dependency;
import com.android.systemui.UiOffloadThread;
import com.android.systemui.analytics.DataCollector;
import com.android.systemui.statusbar.StatusBarState;
import java.io.PrintWriter;

public class FalsingManager implements SensorEventListener {
    private static final int[] CLASSIFIER_SENSORS = {8};
    private static final int[] COLLECTOR_SENSORS = {1, 4, 8, 5, 11};
    private static FalsingManager sInstance = null;
    private final AccessibilityManager mAccessibilityManager;
    private boolean mBouncerOn = false;
    private final Context mContext;
    private final DataCollector mDataCollector;
    private boolean mEnforceBouncer = false;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private boolean mHasActionDown;
    private final HumanInteractionClassifier mHumanInteractionClassifier;
    private Runnable mPendingWtf;
    private boolean mScreenOn;
    /* access modifiers changed from: private */
    public final SensorManager mSensorManager;
    private boolean mSessionActive = false;
    protected final ContentObserver mSettingsObserver = new ContentObserver(this.mHandler) {
        public void onChange(boolean z) {
            FalsingManager.this.updateConfiguration();
        }
    };
    private int mState = 0;
    private int mStatusBarHeight;
    private final UiOffloadThread mUiOffloadThread;

    private FalsingManager(Context context) {
        this.mContext = context;
        this.mSensorManager = (SensorManager) context.getSystemService(SensorManager.class);
        this.mAccessibilityManager = (AccessibilityManager) context.getSystemService(AccessibilityManager.class);
        this.mDataCollector = DataCollector.getInstance(this.mContext);
        this.mHumanInteractionClassifier = HumanInteractionClassifier.getInstance(this.mContext);
        this.mUiOffloadThread = (UiOffloadThread) Dependency.get(UiOffloadThread.class);
        this.mScreenOn = ((PowerManager) context.getSystemService(PowerManager.class)).isInteractive();
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("falsing_manager_enforce_bouncer"), false, this.mSettingsObserver, -1);
        updateConfiguration();
    }

    public static FalsingManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new FalsingManager(context);
        }
        return sInstance;
    }

    /* access modifiers changed from: private */
    public void updateConfiguration() {
        this.mStatusBarHeight = this.mContext.getResources().getDimensionPixelSize(17105519);
        boolean z = false;
        if (Settings.Secure.getInt(this.mContext.getContentResolver(), "falsing_manager_enforce_bouncer", 0) != 0) {
            z = true;
        }
        this.mEnforceBouncer = z;
    }

    private boolean shouldSessionBeActive() {
        boolean z = FalsingLog.ENABLED;
        return isEnabled() && this.mScreenOn && this.mState == 1;
    }

    private boolean sessionEntrypoint() {
        if (this.mSessionActive || !shouldSessionBeActive()) {
            return false;
        }
        onSessionStart();
        return true;
    }

    private void sessionExitpoint(boolean z) {
        if (!this.mSessionActive) {
            return;
        }
        if (z || !shouldSessionBeActive()) {
            this.mSessionActive = false;
            this.mUiOffloadThread.submit(new Runnable() {
                public void run() {
                    FalsingManager.this.mSensorManager.unregisterListener(FalsingManager.this);
                }
            });
        }
    }

    private void onSessionStart() {
        if (FalsingLog.ENABLED) {
            FalsingLog.i("onSessionStart", "classifierEnabled=" + isClassiferEnabled());
            clearPendingWtf();
        }
        this.mBouncerOn = false;
        this.mSessionActive = true;
        if (this.mHumanInteractionClassifier.isEnabled()) {
            registerSensors(CLASSIFIER_SENSORS);
        }
        if (this.mDataCollector.isEnabledFull()) {
            registerSensors(COLLECTOR_SENSORS);
        }
    }

    private void registerSensors(int[] iArr) {
        for (int defaultSensor : iArr) {
            final Sensor defaultSensor2 = this.mSensorManager.getDefaultSensor(defaultSensor);
            if (defaultSensor2 != null) {
                this.mUiOffloadThread.submit(new Runnable() {
                    public void run() {
                        FalsingManager.this.mSensorManager.registerListener(FalsingManager.this, defaultSensor2, 1);
                    }
                });
            }
        }
    }

    public boolean isClassiferEnabled() {
        return this.mHumanInteractionClassifier.isEnabled();
    }

    private boolean isEnabled() {
        return this.mHumanInteractionClassifier.isEnabled() || this.mDataCollector.isEnabled();
    }

    public boolean isFalseTouch() {
        if (this.mAccessibilityManager.isTouchExplorationEnabled()) {
            return false;
        }
        return this.mHumanInteractionClassifier.isFalseTouch();
    }

    private void clearPendingWtf() {
        Runnable runnable = this.mPendingWtf;
        if (runnable != null) {
            this.mHandler.removeCallbacks(runnable);
            this.mPendingWtf = null;
        }
    }

    public synchronized void onSensorChanged(SensorEvent sensorEvent) {
        this.mDataCollector.onSensorChanged(sensorEvent);
        this.mHumanInteractionClassifier.onSensorChanged(sensorEvent);
    }

    public void onAccuracyChanged(Sensor sensor, int i) {
        this.mDataCollector.onAccuracyChanged(sensor, i);
    }

    public boolean shouldEnforceBouncer() {
        return this.mEnforceBouncer;
    }

    public void setStatusBarState(int i) {
        if (FalsingLog.ENABLED) {
            FalsingLog.i("setStatusBarState", "from=" + StatusBarState.toShortString(this.mState) + " to=" + StatusBarState.toShortString(i));
        }
        this.mState = i;
        if (shouldSessionBeActive()) {
            sessionEntrypoint();
        } else {
            sessionExitpoint(false);
        }
    }

    public void onScreenTurningOn() {
        if (FalsingLog.ENABLED) {
            FalsingLog.i("onScreenTurningOn", "from=" + (this.mScreenOn ? 1 : 0));
            clearPendingWtf();
        }
        this.mScreenOn = true;
        if (sessionEntrypoint()) {
            this.mDataCollector.onScreenTurningOn();
        }
    }

    public void onScreenOnFromTouch() {
        if (FalsingLog.ENABLED) {
            FalsingLog.i("onScreenOnFromTouch", "from=" + (this.mScreenOn ? 1 : 0));
        }
        this.mScreenOn = true;
        if (sessionEntrypoint()) {
            this.mDataCollector.onScreenOnFromTouch();
        }
    }

    public void onScreenOff() {
        if (FalsingLog.ENABLED) {
            FalsingLog.i("onScreenOff", "from=" + (this.mScreenOn ? 1 : 0));
        }
        this.mDataCollector.onScreenOff();
        this.mScreenOn = false;
        sessionExitpoint(false);
    }

    public void onSucccessfulUnlock() {
        if (FalsingLog.ENABLED) {
            FalsingLog.i("onSucccessfulUnlock", "");
        }
        this.mDataCollector.onSucccessfulUnlock();
    }

    public void onBouncerShown() {
        if (FalsingLog.ENABLED) {
            FalsingLog.i("onBouncerShown", "from=" + (this.mBouncerOn ? 1 : 0));
        }
        if (!this.mBouncerOn) {
            this.mBouncerOn = true;
            this.mDataCollector.onBouncerShown();
        }
    }

    public void onBouncerHidden() {
        if (FalsingLog.ENABLED) {
            FalsingLog.i("onBouncerHidden", "from=" + (this.mBouncerOn ? 1 : 0));
        }
        if (this.mBouncerOn) {
            this.mBouncerOn = false;
            this.mDataCollector.onBouncerHidden();
        }
    }

    public void onQsDown() {
        if (FalsingLog.ENABLED) {
            FalsingLog.i("onQsDown", "");
        }
        this.mHumanInteractionClassifier.setType(0);
        this.mDataCollector.onQsDown();
    }

    public void setQsExpanded(boolean z) {
        this.mDataCollector.setQsExpanded(z);
    }

    public void onTrackingStarted() {
        if (FalsingLog.ENABLED) {
            FalsingLog.i("onTrackingStarted", "");
        }
        this.mHumanInteractionClassifier.setType(4);
        this.mDataCollector.onTrackingStarted();
    }

    public void onTrackingStopped() {
        this.mDataCollector.onTrackingStopped();
    }

    public void onNotificationActive() {
        this.mDataCollector.onNotificationActive();
    }

    public void onNotificationDoubleTap(boolean z, float f, float f2) {
        if (FalsingLog.ENABLED) {
            FalsingLog.i("onNotificationDoubleTap", "accepted=" + z + " dx=" + f + " dy=" + f2 + " (px)");
        }
        this.mDataCollector.onNotificationDoubleTap();
    }

    public void setNotificationExpanded() {
        this.mDataCollector.setNotificationExpanded();
    }

    public void onNotificatonStartDraggingDown() {
        if (FalsingLog.ENABLED) {
            FalsingLog.i("onNotificatonStartDraggingDown", "");
        }
        this.mHumanInteractionClassifier.setType(2);
        this.mDataCollector.onNotificatonStartDraggingDown();
    }

    public void onNotificatonStopDraggingDown() {
        this.mDataCollector.onNotificatonStopDraggingDown();
    }

    public void onNotificationDismissed() {
        this.mDataCollector.onNotificationDismissed();
    }

    public void onNotificatonStartDismissing() {
        if (FalsingLog.ENABLED) {
            FalsingLog.i("onNotificatonStartDismissing", "");
        }
        this.mHumanInteractionClassifier.setType(1);
        this.mDataCollector.onNotificatonStartDismissing();
    }

    public void onNotificatonStopDismissing() {
        this.mDataCollector.onNotificatonStopDismissing();
    }

    public void onCameraOn() {
        this.mDataCollector.onCameraOn();
    }

    public void onLeftAffordanceOn() {
        this.mDataCollector.onLeftAffordanceOn();
    }

    public void onAffordanceSwipingAborted() {
        this.mDataCollector.onAffordanceSwipingAborted();
    }

    public void onTouchEvent(MotionEvent motionEvent, int i, int i2) {
        if (this.mSessionActive && !this.mBouncerOn) {
            this.mDataCollector.onTouchEvent(motionEvent, i, i2);
            this.mHumanInteractionClassifier.onTouchEvent(motionEvent);
        }
        if (FalsingLog.ENABLED && this.mState == 0) {
            boolean z = motionEvent.getActionMasked() == 0;
            boolean z2 = motionEvent.getActionMasked() == 1;
            boolean z3 = motionEvent.getActionMasked() == 3;
            if (z && motionEvent.getY() < ((float) (this.mStatusBarHeight * 2))) {
                this.mHasActionDown = true;
            } else if ((z2 || z3) && this.mHasActionDown) {
                this.mHasActionDown = false;
            } else {
                return;
            }
            FalsingLog.w("onTouchEvent", String.format("action=%d x=%.1f y=%.1f", new Object[]{Integer.valueOf(motionEvent.getActionMasked()), Float.valueOf(motionEvent.getX()), Float.valueOf(motionEvent.getY())}));
        }
    }

    public void onPanelEvent(boolean z) {
        if (this.mState == 0) {
            FalsingLog.w("onPanelEvent", z ? "expand" : "collapsed");
        }
    }

    public void dump(PrintWriter printWriter) {
        printWriter.println("FALSING MANAGER");
        printWriter.print("classifierEnabled=");
        printWriter.println(isClassiferEnabled() ? 1 : 0);
        printWriter.print("mSessionActive=");
        printWriter.println(this.mSessionActive ? 1 : 0);
        printWriter.print("mBouncerOn=");
        printWriter.println(this.mSessionActive ? 1 : 0);
        printWriter.print("mState=");
        printWriter.println(StatusBarState.toShortString(this.mState));
        printWriter.print("mScreenOn=");
        printWriter.println(this.mScreenOn ? 1 : 0);
        printWriter.println();
    }

    public Uri reportRejectedTouch() {
        if (this.mDataCollector.isEnabled()) {
            return this.mDataCollector.reportRejectedTouch();
        }
        return null;
    }

    public boolean isReportingEnabled() {
        return this.mDataCollector.isReportingEnabled();
    }
}
