package com.android.systemui.statusbar;

import android.content.Context;
import android.database.ContentObserver;
import android.media.AudioAttributes;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;

public class VibratorHelper {
    private static final AudioAttributes STATUS_BAR_VIBRATION_ATTRIBUTES = new AudioAttributes.Builder().setContentType(4).setUsage(13).build();
    private final Context mContext;
    private boolean mHapticFeedbackEnabled;
    private final ContentObserver mVibrationObserver = new ContentObserver(Handler.getMain()) {
        /* class com.android.systemui.statusbar.VibratorHelper.AnonymousClass1 */

        public void onChange(boolean z) {
            VibratorHelper.this.updateHapticFeedBackEnabled();
        }
    };
    private final Vibrator mVibrator;

    public VibratorHelper(Context context) {
        this.mContext = context;
        this.mVibrator = (Vibrator) context.getSystemService(Vibrator.class);
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("haptic_feedback_enabled"), true, this.mVibrationObserver);
        this.mVibrationObserver.onChange(false);
    }

    public void vibrate(int i) {
        if (this.mHapticFeedbackEnabled) {
            AsyncTask.execute(new Runnable(i) {
                /* class com.android.systemui.statusbar.$$Lambda$VibratorHelper$aLryVlYLKeF6vrqCqBn9qjn6bQ */
                public final /* synthetic */ int f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    VibratorHelper.this.lambda$vibrate$0$VibratorHelper(this.f$1);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$vibrate$0 */
    public /* synthetic */ void lambda$vibrate$0$VibratorHelper(int i) {
        this.mVibrator.vibrate(VibrationEffect.get(i, false), STATUS_BAR_VIBRATION_ATTRIBUTES);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateHapticFeedBackEnabled() {
        boolean z = false;
        if (Settings.System.getIntForUser(this.mContext.getContentResolver(), "haptic_feedback_enabled", 0, -2) != 0) {
            z = true;
        }
        this.mHapticFeedbackEnabled = z;
    }
}
