package com.android.systemui.qs.tiles;

import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Switch;
import androidx.lifecycle.LifecycleOwner;
import com.android.systemui.C0010R$drawable;
import com.android.systemui.C0018R$string;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.screenrecord.RecordingController;
import com.android.systemui.statusbar.phone.KeyguardDismissUtil;

public class ScreenRecordTile extends QSTileImpl<QSTile.BooleanState> implements RecordingController.RecordingStateChangeCallback {
    private Callback mCallback;
    private RecordingController mController;
    private KeyguardDismissUtil mKeyguardDismissUtil;
    /* access modifiers changed from: private */
    public long mMillisUntilFinished = 0;

    public Intent getLongClickIntent() {
        return null;
    }

    public int getMetricsCategory() {
        return 0;
    }

    public ScreenRecordTile(QSHost qSHost, RecordingController recordingController, KeyguardDismissUtil keyguardDismissUtil) {
        super(qSHost);
        Callback callback = new Callback();
        this.mCallback = callback;
        this.mController = recordingController;
        recordingController.observe((LifecycleOwner) this, callback);
        this.mKeyguardDismissUtil = keyguardDismissUtil;
    }

    public QSTile.BooleanState newTileState() {
        QSTile.BooleanState booleanState = new QSTile.BooleanState();
        booleanState.label = this.mContext.getString(C0018R$string.quick_settings_screen_record_label);
        booleanState.handlesLongClick = false;
        return booleanState;
    }

    /* access modifiers changed from: protected */
    public void handleClick() {
        if (this.mController.isStarting()) {
            cancelCountdown();
        } else if (this.mController.isRecording()) {
            stopRecording();
        } else {
            this.mUiHandler.post(new Runnable() {
                public final void run() {
                    ScreenRecordTile.this.lambda$handleClick$0$ScreenRecordTile();
                }
            });
        }
        refreshState();
    }

    /* access modifiers changed from: protected */
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        CharSequence charSequence;
        boolean isStarting = this.mController.isStarting();
        boolean isRecording = this.mController.isRecording();
        booleanState.value = isRecording || isStarting;
        booleanState.state = (isRecording || isStarting) ? 2 : 1;
        booleanState.label = this.mContext.getString(C0018R$string.quick_settings_screen_record_label);
        booleanState.icon = QSTileImpl.ResourceIcon.get(C0010R$drawable.ic_screenrecord);
        if (isRecording) {
            booleanState.secondaryLabel = this.mContext.getString(C0018R$string.quick_settings_screen_record_stop);
        } else if (isStarting) {
            booleanState.secondaryLabel = String.format("%d...", new Object[]{Integer.valueOf((int) Math.floorDiv(this.mMillisUntilFinished + 500, 1000))});
        } else {
            booleanState.secondaryLabel = this.mContext.getString(C0018R$string.quick_settings_screen_record_start);
        }
        if (TextUtils.isEmpty(booleanState.secondaryLabel)) {
            charSequence = booleanState.label;
        } else {
            charSequence = TextUtils.concat(new CharSequence[]{booleanState.label, ", ", booleanState.secondaryLabel});
        }
        booleanState.contentDescription = charSequence;
        booleanState.expandedAccessibilityClassName = Switch.class.getName();
    }

    public CharSequence getTileLabel() {
        return this.mContext.getString(C0018R$string.quick_settings_screen_record_label);
    }

    /* access modifiers changed from: private */
    /* renamed from: showPrompt */
    public void lambda$handleClick$0() {
        getHost().collapsePanels();
        this.mKeyguardDismissUtil.executeWhenUnlocked(new ActivityStarter.OnDismissAction(this.mController.getPromptIntent()) {
            public final /* synthetic */ Intent f$1;

            {
                this.f$1 = r2;
            }

            public final boolean onDismiss() {
                return ScreenRecordTile.this.lambda$showPrompt$1$ScreenRecordTile(this.f$1);
            }
        }, false);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$showPrompt$1 */
    public /* synthetic */ boolean lambda$showPrompt$1$ScreenRecordTile(Intent intent) {
        this.mContext.startActivity(intent);
        return false;
    }

    private void cancelCountdown() {
        Log.d("ScreenRecordTile", "Cancelling countdown");
        this.mController.cancelCountdown();
    }

    private void stopRecording() {
        this.mController.stopRecording();
    }

    private final class Callback implements RecordingController.RecordingStateChangeCallback {
        private Callback() {
        }

        public void onCountdown(long j) {
            long unused = ScreenRecordTile.this.mMillisUntilFinished = j;
            ScreenRecordTile.this.refreshState();
        }

        public void onCountdownEnd() {
            ScreenRecordTile.this.refreshState();
        }

        public void onRecordingStart() {
            ScreenRecordTile.this.refreshState();
        }

        public void onRecordingEnd() {
            ScreenRecordTile.this.refreshState();
        }
    }
}
