package com.android.systemui.screenrecord;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Switch;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0017R$layout;
import com.android.systemui.C0021R$string;
import com.android.systemui.settings.CurrentUserContextTracker;
import java.util.ArrayList;
import java.util.List;

public class ScreenRecordDialog extends Activity {
    private Switch mAudioSwitch;
    private final RecordingController mController;
    private final CurrentUserContextTracker mCurrentUserContextTracker;
    private List<ScreenRecordingAudioSource> mModes;
    private Spinner mOptions;
    private Switch mTapsSwitch;

    public ScreenRecordDialog(RecordingController recordingController, CurrentUserContextTracker currentUserContextTracker) {
        this.mController = recordingController;
        this.mCurrentUserContextTracker = currentUserContextTracker;
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Window window = getWindow();
        window.getDecorView();
        window.setLayout(-1, -2);
        window.addPrivateFlags(16);
        window.setGravity(48);
        setTitle(C0021R$string.screenrecord_name);
        setContentView(C0017R$layout.screen_record_dialog);
        ((Button) findViewById(C0015R$id.button_cancel)).setOnClickListener(new View.OnClickListener() {
            /* class com.android.systemui.screenrecord.$$Lambda$ScreenRecordDialog$UwuybAZfzEbqKArO9WeoPnEStk */

            public final void onClick(View view) {
                ScreenRecordDialog.this.lambda$onCreate$0$ScreenRecordDialog(view);
            }
        });
        ((Button) findViewById(C0015R$id.button_start)).setOnClickListener(new View.OnClickListener() {
            /* class com.android.systemui.screenrecord.$$Lambda$ScreenRecordDialog$PtlgQ6bdLH8Q6JnpPzk4xxbDTtg */

            public final void onClick(View view) {
                ScreenRecordDialog.this.lambda$onCreate$1$ScreenRecordDialog(view);
            }
        });
        ArrayList arrayList = new ArrayList();
        this.mModes = arrayList;
        arrayList.add(ScreenRecordingAudioSource.MIC);
        this.mModes.add(ScreenRecordingAudioSource.INTERNAL);
        this.mModes.add(ScreenRecordingAudioSource.MIC_AND_INTERNAL);
        this.mAudioSwitch = (Switch) findViewById(C0015R$id.screenrecord_audio_switch);
        this.mTapsSwitch = (Switch) findViewById(C0015R$id.screenrecord_taps_switch);
        this.mOptions = (Spinner) findViewById(C0015R$id.screen_recording_options);
        ScreenRecordingAdapter screenRecordingAdapter = new ScreenRecordingAdapter(getApplicationContext(), 17367049, this.mModes);
        screenRecordingAdapter.setDropDownViewResource(17367049);
        this.mOptions.setAdapter((SpinnerAdapter) screenRecordingAdapter);
        this.mOptions.setOnItemClickListenerInt(new AdapterView.OnItemClickListener() {
            /* class com.android.systemui.screenrecord.$$Lambda$ScreenRecordDialog$cUVjQAzT3j1yIYL9zw8455dx4I */

            @Override // android.widget.AdapterView.OnItemClickListener
            public final void onItemClick(AdapterView adapterView, View view, int i, long j) {
                ScreenRecordDialog.this.lambda$onCreate$2$ScreenRecordDialog(adapterView, view, i, j);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onCreate$0 */
    public /* synthetic */ void lambda$onCreate$0$ScreenRecordDialog(View view) {
        finish();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onCreate$1 */
    public /* synthetic */ void lambda$onCreate$1$ScreenRecordDialog(View view) {
        requestScreenCapture();
        finish();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onCreate$2 */
    public /* synthetic */ void lambda$onCreate$2$ScreenRecordDialog(AdapterView adapterView, View view, int i, long j) {
        this.mAudioSwitch.setChecked(true);
    }

    private void requestScreenCapture() {
        ScreenRecordingAudioSource screenRecordingAudioSource;
        Context currentUserContext = this.mCurrentUserContextTracker.getCurrentUserContext();
        boolean isChecked = this.mTapsSwitch.isChecked();
        if (this.mAudioSwitch.isChecked()) {
            screenRecordingAudioSource = (ScreenRecordingAudioSource) this.mOptions.getSelectedItem();
        } else {
            screenRecordingAudioSource = ScreenRecordingAudioSource.NONE;
        }
        this.mController.startCountdown(3000, 1000, PendingIntent.getForegroundService(currentUserContext, 2, RecordingService.getStartIntent(currentUserContext, -1, screenRecordingAudioSource.ordinal(), isChecked), 201326592), PendingIntent.getService(currentUserContext, 2, RecordingService.getStopIntent(currentUserContext), 201326592));
    }
}
