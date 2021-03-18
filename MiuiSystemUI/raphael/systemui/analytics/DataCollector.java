package com.android.systemui.analytics;

import android.content.Context;
import android.database.ContentObserver;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.MotionEvent;
import android.widget.Toast;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.FalsingPlugin;
import com.android.systemui.plugins.PluginListener;
import com.android.systemui.shared.plugins.PluginManager;
import com.google.protobuf.nano.MessageNano;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class DataCollector implements SensorEventListener {
    private static DataCollector sInstance;
    private boolean mAllowReportRejectedTouch = false;
    private boolean mCollectBadTouches = false;
    private final Context mContext;
    private boolean mCornerSwiping = false;
    private SensorLoggerSession mCurrentSession = null;
    private boolean mDisableUnlocking = false;
    private boolean mEnableCollector = false;
    private FalsingPlugin mFalsingPlugin = null;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final PluginListener mPluginListener = new PluginListener<FalsingPlugin>() {
        /* class com.android.systemui.analytics.DataCollector.AnonymousClass2 */

        public void onPluginConnected(FalsingPlugin falsingPlugin, Context context) {
            DataCollector.this.mFalsingPlugin = falsingPlugin;
        }

        public void onPluginDisconnected(FalsingPlugin falsingPlugin) {
            DataCollector.this.mFalsingPlugin = null;
        }
    };
    protected final ContentObserver mSettingsObserver = new ContentObserver(this.mHandler) {
        /* class com.android.systemui.analytics.DataCollector.AnonymousClass1 */

        public void onChange(boolean z) {
            DataCollector.this.updateConfiguration();
        }
    };
    private boolean mTrackingStarted = false;

    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    public void onExpansionFromPulseStopped() {
    }

    public void onStartExpandingFromPulse() {
    }

    private DataCollector(Context context) {
        this.mContext = context;
        context.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("data_collector_enable"), false, this.mSettingsObserver, -1);
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("data_collector_collect_bad_touches"), false, this.mSettingsObserver, -1);
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("data_collector_allow_rejected_touch_reports"), false, this.mSettingsObserver, -1);
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("data_collector_disable_unlocking"), false, this.mSettingsObserver, -1);
        updateConfiguration();
        ((PluginManager) Dependency.get(PluginManager.class)).addPluginListener(this.mPluginListener, FalsingPlugin.class);
    }

    public static DataCollector getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new DataCollector(context);
        }
        return sInstance;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateConfiguration() {
        boolean z = true;
        boolean z2 = Build.IS_DEBUGGABLE && Settings.Secure.getInt(this.mContext.getContentResolver(), "data_collector_enable", 0) != 0;
        this.mEnableCollector = z2;
        this.mCollectBadTouches = z2 && Settings.Secure.getInt(this.mContext.getContentResolver(), "data_collector_collect_bad_touches", 0) != 0;
        this.mAllowReportRejectedTouch = Build.IS_DEBUGGABLE && Settings.Secure.getInt(this.mContext.getContentResolver(), "data_collector_allow_rejected_touch_reports", 0) != 0;
        if (!this.mEnableCollector || !Build.IS_DEBUGGABLE || Settings.Secure.getInt(this.mContext.getContentResolver(), "data_collector_disable_unlocking", 0) == 0) {
            z = false;
        }
        this.mDisableUnlocking = z;
    }

    private boolean sessionEntrypoint() {
        if (!isEnabled() || this.mCurrentSession != null) {
            return false;
        }
        onSessionStart();
        return true;
    }

    private void sessionExitpoint(int i) {
        if (this.mCurrentSession != null) {
            onSessionEnd(i);
        }
    }

    private void onSessionStart() {
        this.mCornerSwiping = false;
        this.mTrackingStarted = false;
        this.mCurrentSession = new SensorLoggerSession(System.currentTimeMillis(), System.nanoTime());
    }

    private void onSessionEnd(int i) {
        SensorLoggerSession sensorLoggerSession = this.mCurrentSession;
        this.mCurrentSession = null;
        if (this.mEnableCollector || this.mDisableUnlocking) {
            sensorLoggerSession.end(System.currentTimeMillis(), i);
            queueSession(sensorLoggerSession);
        }
    }

    public Uri reportRejectedTouch() {
        SensorLoggerSession sensorLoggerSession = this.mCurrentSession;
        if (sensorLoggerSession == null) {
            Toast.makeText(this.mContext, "Generating rejected touch report failed: session timed out.", 1).show();
            return null;
        }
        sensorLoggerSession.setType(4);
        sensorLoggerSession.end(System.currentTimeMillis(), 1);
        byte[] byteArray = MessageNano.toByteArray(sensorLoggerSession.toProto());
        File file = new File(this.mContext.getExternalCacheDir(), "rejected_touch_reports");
        file.mkdir();
        File file2 = new File(file, "rejected_touch_report_" + System.currentTimeMillis());
        try {
            new FileOutputStream(file2).write(byteArray);
            return Uri.fromFile(file2);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void queueSession(final SensorLoggerSession sensorLoggerSession) {
        AsyncTask.execute(new Runnable() {
            /* class com.android.systemui.analytics.DataCollector.AnonymousClass3 */

            public void run() {
                byte[] byteArray = MessageNano.toByteArray(sensorLoggerSession.toProto());
                boolean z = true;
                if (DataCollector.this.mFalsingPlugin != null) {
                    FalsingPlugin falsingPlugin = DataCollector.this.mFalsingPlugin;
                    if (sensorLoggerSession.getResult() != 1) {
                        z = false;
                    }
                    falsingPlugin.dataCollected(z, byteArray);
                    return;
                }
                String absolutePath = DataCollector.this.mContext.getFilesDir().getAbsolutePath();
                if (sensorLoggerSession.getResult() != 1) {
                    if (DataCollector.this.mDisableUnlocking || DataCollector.this.mCollectBadTouches) {
                        absolutePath = absolutePath + "/bad_touches";
                    } else {
                        return;
                    }
                } else if (!DataCollector.this.mDisableUnlocking) {
                    absolutePath = absolutePath + "/good_touches";
                }
                File file = new File(absolutePath);
                file.mkdir();
                try {
                    new FileOutputStream(new File(file, "trace_" + System.currentTimeMillis())).write(byteArray);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public synchronized void onSensorChanged(SensorEvent sensorEvent) {
        if (isEnabled() && this.mCurrentSession != null) {
            this.mCurrentSession.addSensorEvent(sensorEvent, System.nanoTime());
        }
    }

    public boolean isEnabled() {
        return this.mEnableCollector || this.mAllowReportRejectedTouch || this.mDisableUnlocking;
    }

    public boolean isUnlockingDisabled() {
        return this.mDisableUnlocking;
    }

    public boolean isEnabledFull() {
        return this.mEnableCollector;
    }

    public void onScreenTurningOn() {
        if (sessionEntrypoint()) {
            addEvent(0);
        }
    }

    public void onScreenOnFromTouch() {
        if (sessionEntrypoint()) {
            addEvent(1);
        }
    }

    public void onScreenOff() {
        addEvent(2);
        sessionExitpoint(0);
    }

    public void onSucccessfulUnlock() {
        addEvent(3);
        sessionExitpoint(1);
    }

    public void onBouncerShown() {
        addEvent(4);
    }

    public void onBouncerHidden() {
        addEvent(5);
    }

    public void onQsDown() {
        addEvent(6);
    }

    public void setQsExpanded(boolean z) {
        if (z) {
            addEvent(7);
        } else {
            addEvent(8);
        }
    }

    public void onTrackingStarted() {
        this.mTrackingStarted = true;
        addEvent(9);
    }

    public void onTrackingStopped() {
        if (this.mTrackingStarted) {
            this.mTrackingStarted = false;
            addEvent(10);
        }
    }

    public void onNotificationActive() {
        addEvent(11);
    }

    public void onNotificationDoubleTap() {
        addEvent(13);
    }

    public void setNotificationExpanded() {
        addEvent(14);
    }

    public void onNotificatonStartDraggingDown() {
        addEvent(16);
    }

    public void onNotificatonStopDraggingDown() {
        addEvent(17);
    }

    public void onNotificationDismissed() {
        addEvent(18);
    }

    public void onNotificatonStartDismissing() {
        addEvent(19);
    }

    public void onNotificatonStopDismissing() {
        addEvent(20);
    }

    public void onCameraOn() {
        addEvent(24);
    }

    public void onLeftAffordanceOn() {
        addEvent(25);
    }

    public void onAffordanceSwipingStarted(boolean z) {
        this.mCornerSwiping = true;
        if (z) {
            addEvent(21);
        } else {
            addEvent(22);
        }
    }

    public void onAffordanceSwipingAborted() {
        if (this.mCornerSwiping) {
            this.mCornerSwiping = false;
            addEvent(23);
        }
    }

    public void onUnlockHintStarted() {
        addEvent(26);
    }

    public void onCameraHintStarted() {
        addEvent(27);
    }

    public void onLeftAffordanceHintStarted() {
        addEvent(28);
    }

    public void onTouchEvent(MotionEvent motionEvent, int i, int i2) {
        SensorLoggerSession sensorLoggerSession = this.mCurrentSession;
        if (sensorLoggerSession != null) {
            sensorLoggerSession.addMotionEvent(motionEvent);
            this.mCurrentSession.setTouchArea(i, i2);
        }
    }

    private void addEvent(int i) {
        SensorLoggerSession sensorLoggerSession;
        if (isEnabled() && (sensorLoggerSession = this.mCurrentSession) != null) {
            sensorLoggerSession.addPhoneEvent(i, System.nanoTime());
        }
    }

    public boolean isReportingEnabled() {
        return this.mAllowReportRejectedTouch;
    }

    public void onFalsingSessionStarted() {
        sessionEntrypoint();
    }
}
