package com.android.systemui.qs.tiles;

import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Switch;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0021R$string;
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
    private long mMillisUntilFinished = 0;

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public Intent getLongClickIntent() {
        return null;
    }

    @Override // com.android.systemui.plugins.qs.QSTile, com.android.systemui.qs.tileimpl.QSTileImpl
    public int getMetricsCategory() {
        return 0;
    }

    public ScreenRecordTile(QSHost qSHost, RecordingController recordingController, KeyguardDismissUtil keyguardDismissUtil) {
        super(qSHost);
        Callback callback = new Callback();
        this.mCallback = callback;
        this.mController = recordingController;
        recordingController.observe(this, callback);
        this.mKeyguardDismissUtil = keyguardDismissUtil;
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public QSTile.BooleanState newTileState() {
        QSTile.BooleanState booleanState = new QSTile.BooleanState();
        booleanState.label = this.mContext.getString(C0021R$string.quick_settings_screen_record_label);
        booleanState.handlesLongClick = false;
        return booleanState;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleClick() {
        if (this.mController.isStarting()) {
            cancelCountdown();
        } else if (this.mController.isRecording()) {
            stopRecording();
        } else {
            this.mUiHandler.post(new Runnable() {
                /* class com.android.systemui.qs.tiles.$$Lambda$ScreenRecordTile$mAnfMZKBPW0VK4FrRQ7ZNGHi_A */

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
        booleanState.label = this.mContext.getString(C0021R$string.quick_settings_screen_record_label);
        booleanState.icon = QSTileImpl.ResourceIcon.get(C0013R$drawable.ic_screenrecord);
        if (isRecording) {
            booleanState.secondaryLabel = this.mContext.getString(C0021R$string.quick_settings_screen_record_stop);
        } else if (isStarting) {
            booleanState.secondaryLabel = String.format("%d...", Integer.valueOf((int) Math.floorDiv(this.mMillisUntilFinished + 500, 1000)));
        } else {
            booleanState.secondaryLabel = this.mContext.getString(C0021R$string.quick_settings_screen_record_start);
        }
        if (TextUtils.isEmpty(booleanState.secondaryLabel)) {
            charSequence = booleanState.label;
        } else {
            charSequence = TextUtils.concat(booleanState.label, ", ", booleanState.secondaryLabel);
        }
        booleanState.contentDescription = charSequence;
        booleanState.expandedAccessibilityClassName = Switch.class.getName();
    }

    @Override // com.android.systemui.plugins.qs.QSTile
    public CharSequence getTileLabel() {
        return this.mContext.getString(C0021R$string.quick_settings_screen_record_label);
    }

    /* access modifiers changed from: private */
    /* renamed from: showPrompt */
    public void lambda$handleClick$0() {
        getHost().collapsePanels();
        this.mKeyguardDismissUtil.executeWhenUnlocked(new ActivityStarter.OnDismissAction(this.mController.getPromptIntent()) {
            /* class com.android.systemui.qs.tiles.$$Lambda$ScreenRecordTile$qbmpsZNn23bn5rqlkcpltHLJl4w */
            public final /* synthetic */ Intent f$1;

            {
                this.f$1 = r2;
            }

            @Override // com.android.systemui.plugins.ActivityStarter.OnDismissAction
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

        @Override // com.android.systemui.screenrecord.RecordingController.RecordingStateChangeCallback
        public void onCountdown(long j) {
            ScreenRecordTile.this.mMillisUntilFinished = j;
            ScreenRecordTile.this.refreshState();
        }

        @Override // com.android.systemui.screenrecord.RecordingController.RecordingStateChangeCallback
        public void onCountdownEnd() {
            ScreenRecordTile.this.refreshState();
        }

        @Override // com.android.systemui.screenrecord.RecordingController.RecordingStateChangeCallback
        public void onRecordingStart() {
            ScreenRecordTile.this.refreshState();
        }

        @Override // com.android.systemui.screenrecord.RecordingController.RecordingStateChangeCallback
        public void onRecordingEnd() {
            ScreenRecordTile.this.refreshState();
        }
    }
}
