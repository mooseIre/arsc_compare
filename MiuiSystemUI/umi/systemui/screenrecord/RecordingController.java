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
import java.util.concurrent.Executor;

public class RecordingController implements CallbackController<RecordingStateChangeCallback> {
    /* access modifiers changed from: private */
    public BroadcastDispatcher mBroadcastDispatcher;
    private CountDownTimer mCountDownTimer = null;
    /* access modifiers changed from: private */
    public boolean mIsRecording;
    /* access modifiers changed from: private */
    public boolean mIsStarting;
    /* access modifiers changed from: private */
    public ArrayList<RecordingStateChangeCallback> mListeners = new ArrayList<>();
    /* access modifiers changed from: private */
    public PendingIntent mStopIntent;
    @VisibleForTesting
    protected final BroadcastReceiver mUserChangeReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (RecordingController.this.mStopIntent != null) {
                RecordingController.this.stopRecording();
            }
        }
    };

    public interface RecordingStateChangeCallback {
        void onCountdown(long j) {
        }

        void onCountdownEnd() {
        }

        void onRecordingEnd() {
        }

        void onRecordingStart() {
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

    public void startCountdown(long j, long j2, PendingIntent pendingIntent, PendingIntent pendingIntent2) {
        this.mIsStarting = true;
        this.mStopIntent = pendingIntent2;
        final PendingIntent pendingIntent3 = pendingIntent;
        AnonymousClass2 r1 = new CountDownTimer(j, j2) {
            public void onTick(long j) {
                Iterator it = RecordingController.this.mListeners.iterator();
                while (it.hasNext()) {
                    ((RecordingStateChangeCallback) it.next()).onCountdown(j);
                }
            }

            public void onFinish() {
                boolean unused = RecordingController.this.mIsStarting = false;
                boolean unused2 = RecordingController.this.mIsRecording = true;
                Iterator it = RecordingController.this.mListeners.iterator();
                while (it.hasNext()) {
                    ((RecordingStateChangeCallback) it.next()).onCountdownEnd();
                }
                try {
                    pendingIntent3.send();
                    RecordingController.this.mBroadcastDispatcher.registerReceiver(RecordingController.this.mUserChangeReceiver, new IntentFilter("android.intent.action.USER_SWITCHED"), (Executor) null, UserHandle.ALL);
                    Log.d("RecordingController", "sent start intent");
                } catch (PendingIntent.CanceledException e) {
                    Log.e("RecordingController", "Pending intent was cancelled: " + e.getMessage());
                }
            }
        };
        this.mCountDownTimer = r1;
        r1.start();
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
