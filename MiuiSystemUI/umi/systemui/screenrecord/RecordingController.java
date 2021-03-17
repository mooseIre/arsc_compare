package com.android.systemui.screenrecord;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.CountDownTimer;
import android.os.UserHandle;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.statusbar.policy.CallbackController;
import java.util.ArrayList;
import java.util.Iterator;

public class RecordingController implements CallbackController<RecordingStateChangeCallback> {
    private BroadcastDispatcher mBroadcastDispatcher;
    private CountDownTimer mCountDownTimer = null;
    private boolean mIsRecording;
    private boolean mIsStarting;
    private ArrayList<RecordingStateChangeCallback> mListeners = new ArrayList<>();
    private PendingIntent mStopIntent;
    @VisibleForTesting
    protected final BroadcastReceiver mUserChangeReceiver = new BroadcastReceiver() {
        /* class com.android.systemui.screenrecord.RecordingController.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            if (RecordingController.this.mStopIntent != null) {
                RecordingController.this.stopRecording();
            }
        }
    };

    public interface RecordingStateChangeCallback {
        default void onCountdown(long j) {
        }

        default void onCountdownEnd() {
        }

        default void onRecordingEnd() {
        }

        default void onRecordingStart() {
        }
    }

    public RecordingController(BroadcastDispatcher broadcastDispatcher) {
        this.mBroadcastDispatcher = broadcastDispatcher;
    }

    public Intent getPromptIntent() {
        ComponentName componentName = new ComponentName("com.android.systemui", "com.android.systemui.screenrecord.ScreenRecordDialog");
        Intent intent = new Intent();
        intent.setComponent(componentName);
        intent.setFlags(268435456);
        return intent;
    }

    public void startCountdown(long j, long j2, final PendingIntent pendingIntent, PendingIntent pendingIntent2) {
        this.mIsStarting = true;
        this.mStopIntent = pendingIntent2;
        AnonymousClass2 r14 = new CountDownTimer(j, j2) {
            /* class com.android.systemui.screenrecord.RecordingController.AnonymousClass2 */

            public void onTick(long j) {
                Iterator it = RecordingController.this.mListeners.iterator();
                while (it.hasNext()) {
                    ((RecordingStateChangeCallback) it.next()).onCountdown(j);
                }
            }

            public void onFinish() {
                RecordingController.this.mIsStarting = false;
                RecordingController.this.mIsRecording = true;
                Iterator it = RecordingController.this.mListeners.iterator();
                while (it.hasNext()) {
                    ((RecordingStateChangeCallback) it.next()).onCountdownEnd();
                }
                try {
                    pendingIntent.send();
                    RecordingController.this.mBroadcastDispatcher.registerReceiver(RecordingController.this.mUserChangeReceiver, new IntentFilter("android.intent.action.USER_SWITCHED"), null, UserHandle.ALL);
                    Log.d("RecordingController", "sent start intent");
                } catch (PendingIntent.CanceledException e) {
                    Log.e("RecordingController", "Pending intent was cancelled: " + e.getMessage());
                }
            }
        };
        this.mCountDownTimer = r14;
        r14.start();
    }

    public void cancelCountdown() {
        CountDownTimer countDownTimer = this.mCountDownTimer;
        if (countDownTimer != null) {
            countDownTimer.cancel();
        } else {
            Log.e("RecordingController", "Timer was null");
        }
        this.mIsStarting = false;
        Iterator<RecordingStateChangeCallback> it = this.mListeners.iterator();
        while (it.hasNext()) {
            it.next().onCountdownEnd();
        }
    }

    public boolean isStarting() {
        return this.mIsStarting;
    }

    public synchronized boolean isRecording() {
        return this.mIsRecording;
    }

    public void stopRecording() {
        try {
            if (this.mStopIntent != null) {
                this.mStopIntent.send();
            } else {
                Log.e("RecordingController", "Stop intent was null");
            }
            updateState(false);
        } catch (PendingIntent.CanceledException e) {
            Log.e("RecordingController", "Error stopping: " + e.getMessage());
        }
        this.mBroadcastDispatcher.unregisterReceiver(this.mUserChangeReceiver);
    }

    public synchronized void updateState(boolean z) {
        this.mIsRecording = z;
        Iterator<RecordingStateChangeCallback> it = this.mListeners.iterator();
        while (it.hasNext()) {
            RecordingStateChangeCallback next = it.next();
            if (z) {
                next.onRecordingStart();
            } else {
                next.onRecordingEnd();
            }
        }
    }

    public void addCallback(RecordingStateChangeCallback recordingStateChangeCallback) {
        this.mListeners.add(recordingStateChangeCallback);
    }

    public void removeCallback(RecordingStateChangeCallback recordingStateChangeCallback) {
        this.mListeners.remove(recordingStateChangeCallback);
    }
}
