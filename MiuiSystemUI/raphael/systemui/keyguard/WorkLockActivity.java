package com.android.systemui.keyguard;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManagerCompat;
import android.app.ActivityOptions;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;
import android.view.View;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.plugins.R;

public class WorkLockActivity extends Activity {
    private KeyguardManager mKgm;
    private final BroadcastReceiver mLockEventReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            int targetUserId = WorkLockActivity.this.getTargetUserId();
            if (intent.getIntExtra("android.intent.extra.user_handle", targetUserId) == targetUserId && !WorkLockActivity.this.getKeyguardManager().isDeviceLocked(targetUserId)) {
                WorkLockActivity.this.finish();
            }
        }
    };

    public void onBackPressed() {
    }

    public void setTaskDescription(ActivityManager.TaskDescription taskDescription) {
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        registerReceiverAsUser(this.mLockEventReceiver, UserHandle.ALL, new IntentFilter("android.intent.action.DEVICE_LOCKED_CHANGED"), (String) null, (Handler) null);
        if (!getKeyguardManager().isDeviceLocked(getTargetUserId())) {
            finish();
            return;
        }
        setOverlayWithDecorCaptionEnabled(true);
        View view = new View(this);
        view.setContentDescription(getString(R.string.accessibility_desc_work_lock));
        view.setBackgroundColor(getPrimaryColor());
        setContentView(view);
    }

    public void onWindowFocusChanged(boolean z) {
        if (z) {
            showConfirmCredentialActivity();
        }
    }

    public void onDestroy() {
        unregisterReceiver(this.mLockEventReceiver);
        super.onDestroy();
    }

    private void showConfirmCredentialActivity() {
        Intent createConfirmDeviceCredentialIntent;
        if (!isFinishing() && getKeyguardManager().isDeviceLocked(getTargetUserId()) && (createConfirmDeviceCredentialIntent = getKeyguardManager().createConfirmDeviceCredentialIntent((CharSequence) null, (CharSequence) null, getTargetUserId())) != null) {
            ActivityOptions makeBasic = ActivityOptions.makeBasic();
            makeBasic.setLaunchTaskId(getTaskId());
            createConfirmDeviceCredentialIntent.putExtra("android.intent.extra.INTENT", PendingIntent.getActivity(this, -1, getIntent(), 1409286144, makeBasic.toBundle()).getIntentSender());
            try {
                ActivityManagerCompat.getService().startConfirmDeviceCredentialIntent(createConfirmDeviceCredentialIntent, getChallengeOptions().toBundle());
            } catch (RemoteException e) {
                Log.e("WorkLockActivity", "Failed to start confirm credential intent", e);
            }
        }
    }

    private ActivityOptions getChallengeOptions() {
        if (!isInMultiWindowMode()) {
            return ActivityOptions.makeBasic();
        }
        View decorView = getWindow().getDecorView();
        return ActivityOptions.makeScaleUpAnimation(decorView, 0, 0, decorView.getWidth(), decorView.getHeight());
    }

    /* access modifiers changed from: private */
    public KeyguardManager getKeyguardManager() {
        if (this.mKgm == null) {
            this.mKgm = (KeyguardManager) getSystemService("keyguard");
        }
        return this.mKgm;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public final int getTargetUserId() {
        return getIntent().getIntExtra("android.intent.extra.USER_ID", UserHandle.myUserId());
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public final int getPrimaryColor() {
        ActivityManager.TaskDescription taskDescription = (ActivityManager.TaskDescription) getIntent().getExtra("com.android.systemui.keyguard.extra.TASK_DESCRIPTION");
        if (taskDescription == null || Color.alpha(taskDescription.getPrimaryColor()) != 255) {
            return ((DevicePolicyManager) getSystemService("device_policy")).getOrganizationColorForUser(getTargetUserId());
        }
        return taskDescription.getPrimaryColor();
    }
}
