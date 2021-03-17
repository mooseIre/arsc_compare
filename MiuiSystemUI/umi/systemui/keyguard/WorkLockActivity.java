package com.android.systemui.keyguard;

import android.app.Activity;
import android.app.ActivityManager;
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
import android.os.UserHandle;
import android.view.View;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.C0021R$string;
import com.android.systemui.broadcast.BroadcastDispatcher;

public class WorkLockActivity extends Activity {
    private final BroadcastDispatcher mBroadcastDispatcher;
    private KeyguardManager mKgm;
    private final BroadcastReceiver mLockEventReceiver = new BroadcastReceiver() {
        /* class com.android.systemui.keyguard.WorkLockActivity.AnonymousClass1 */

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

    public WorkLockActivity(BroadcastDispatcher broadcastDispatcher) {
        this.mBroadcastDispatcher = broadcastDispatcher;
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.mBroadcastDispatcher.registerReceiver(this.mLockEventReceiver, new IntentFilter("android.intent.action.DEVICE_LOCKED_CHANGED"), null, UserHandle.ALL);
        if (!getKeyguardManager().isDeviceLocked(getTargetUserId())) {
            finish();
            return;
        }
        setOverlayWithDecorCaptionEnabled(true);
        View view = new View(this);
        view.setContentDescription(getString(C0021R$string.accessibility_desc_work_lock));
        view.setBackgroundColor(getPrimaryColor());
        setContentView(view);
    }

    public void onWindowFocusChanged(boolean z) {
        if (z) {
            showConfirmCredentialActivity();
        }
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public void unregisterBroadcastReceiver() {
        this.mBroadcastDispatcher.unregisterReceiver(this.mLockEventReceiver);
    }

    public void onDestroy() {
        unregisterBroadcastReceiver();
        super.onDestroy();
    }

    private void showConfirmCredentialActivity() {
        Intent createConfirmDeviceCredentialIntent;
        if (!isFinishing() && getKeyguardManager().isDeviceLocked(getTargetUserId()) && (createConfirmDeviceCredentialIntent = getKeyguardManager().createConfirmDeviceCredentialIntent(null, null, getTargetUserId(), true)) != null) {
            ActivityOptions makeBasic = ActivityOptions.makeBasic();
            makeBasic.setLaunchTaskId(getTaskId());
            PendingIntent activity = PendingIntent.getActivity(this, -1, getIntent(), 1409286144, makeBasic.toBundle());
            if (activity != null) {
                createConfirmDeviceCredentialIntent.putExtra("android.intent.extra.INTENT", activity.getIntentSender());
            }
            ActivityOptions makeBasic2 = ActivityOptions.makeBasic();
            makeBasic2.setLaunchTaskId(getTaskId());
            makeBasic2.setTaskOverlay(true, true);
            startActivityForResult(createConfirmDeviceCredentialIntent, 1, makeBasic2.toBundle());
        }
    }

    /* access modifiers changed from: protected */
    public void onActivityResult(int i, int i2, Intent intent) {
        if (i == 1 && i2 != -1) {
            goToHomeScreen();
        }
    }

    private void goToHomeScreen() {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        intent.setFlags(268435456);
        startActivity(intent);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private KeyguardManager getKeyguardManager() {
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
