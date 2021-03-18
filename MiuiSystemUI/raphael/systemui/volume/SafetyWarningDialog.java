package com.android.systemui.volume;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.media.AudioManager;
import android.util.Log;
import android.view.KeyEvent;
import com.android.systemui.statusbar.phone.SystemUIDialog;

public abstract class SafetyWarningDialog extends SystemUIDialog implements DialogInterface.OnDismissListener, DialogInterface.OnClickListener {
    private static final String TAG = Util.logTag(SafetyWarningDialog.class);
    private final AudioManager mAudioManager;
    private final Context mContext;
    private boolean mDisableOnVolumeUp;
    private boolean mNewVolumeUp;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        /* class com.android.systemui.volume.SafetyWarningDialog.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.CLOSE_SYSTEM_DIALOGS".equals(intent.getAction())) {
                if (D.BUG) {
                    Log.d(SafetyWarningDialog.TAG, "Received ACTION_CLOSE_SYSTEM_DIALOGS");
                }
                SafetyWarningDialog.this.cancel();
                SafetyWarningDialog.this.cleanUp();
            }
        }
    };
    private long mShowTime;

    /* access modifiers changed from: protected */
    public abstract void cleanUp();

    public SafetyWarningDialog(Context context, AudioManager audioManager) {
        super(context);
        this.mContext = context;
        this.mAudioManager = audioManager;
        try {
            this.mDisableOnVolumeUp = context.getResources().getBoolean(17891515);
        } catch (Resources.NotFoundException unused) {
            this.mDisableOnVolumeUp = true;
        }
        getWindow().setType(2010);
        setShowForAllUsers(true);
        setMessage(this.mContext.getString(17041277));
        setButton(-1, this.mContext.getString(17039379), this);
        setButton(-2, this.mContext.getString(17039369), (DialogInterface.OnClickListener) null);
        setOnDismissListener(this);
        context.registerReceiver(this.mReceiver, new IntentFilter("android.intent.action.CLOSE_SYSTEM_DIALOGS"));
    }

    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        if (this.mDisableOnVolumeUp && i == 24 && keyEvent.getRepeatCount() == 0) {
            this.mNewVolumeUp = true;
        }
        return super.onKeyDown(i, keyEvent);
    }

    public boolean onKeyUp(int i, KeyEvent keyEvent) {
        if (i == 24 && this.mNewVolumeUp && System.currentTimeMillis() - this.mShowTime > 1000) {
            if (D.BUG) {
                Log.d(TAG, "Confirmed warning via VOLUME_UP");
            }
            this.mAudioManager.disableSafeMediaVolume();
            dismiss();
        }
        return super.onKeyUp(i, keyEvent);
    }

    public void onClick(DialogInterface dialogInterface, int i) {
        this.mAudioManager.disableSafeMediaVolume();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.SystemUIDialog
    public void onStart() {
        super.onStart();
        this.mShowTime = System.currentTimeMillis();
    }

    public void onDismiss(DialogInterface dialogInterface) {
        this.mContext.unregisterReceiver(this.mReceiver);
        cleanUp();
    }
}
