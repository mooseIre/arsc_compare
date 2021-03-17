package com.android.systemui.keyguard;

import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.ActivityTaskManager;
import android.app.IActivityTaskManager;
import android.app.ProfilerInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.shared.system.ActivityManagerWrapper;
import com.android.systemui.shared.system.TaskStackChangeListener;

public class WorkLockActivityController {
    private static final String TAG = "WorkLockActivityController";
    private final Context mContext;
    private final IActivityTaskManager mIatm;
    private final TaskStackChangeListener mLockListener;

    public WorkLockActivityController(Context context) {
        this(context, ActivityManagerWrapper.getInstance(), ActivityTaskManager.getService());
    }

    @VisibleForTesting
    WorkLockActivityController(Context context, ActivityManagerWrapper activityManagerWrapper, IActivityTaskManager iActivityTaskManager) {
        AnonymousClass1 r0 = new TaskStackChangeListener() {
            /* class com.android.systemui.keyguard.WorkLockActivityController.AnonymousClass1 */

            @Override // com.android.systemui.shared.system.TaskStackChangeListener
            public void onTaskProfileLocked(int i, int i2) {
                WorkLockActivityController.this.startWorkChallengeInTask(i, i2);
            }
        };
        this.mLockListener = r0;
        this.mContext = context;
        this.mIatm = iActivityTaskManager;
        activityManagerWrapper.registerTaskStackListener(r0);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startWorkChallengeInTask(int i, int i2) {
        ActivityManager.TaskDescription taskDescription;
        try {
            taskDescription = this.mIatm.getTaskDescription(i);
        } catch (RemoteException unused) {
            String str = TAG;
            Log.w(str, "Failed to get description for task=" + i);
            taskDescription = null;
        }
        Intent addFlags = new Intent("android.app.action.CONFIRM_DEVICE_CREDENTIAL_WITH_USER").setComponent(new ComponentName(this.mContext, WorkLockActivity.class)).putExtra("android.intent.extra.USER_ID", i2).putExtra("com.android.systemui.keyguard.extra.TASK_DESCRIPTION", taskDescription).addFlags(67239936);
        ActivityOptions makeBasic = ActivityOptions.makeBasic();
        makeBasic.setLaunchTaskId(i);
        makeBasic.setTaskOverlay(true, false);
        if (!ActivityManager.isStartResultSuccessful(startActivityAsUser(addFlags, makeBasic.toBundle(), -2))) {
            try {
                this.mIatm.removeTask(i);
            } catch (RemoteException unused2) {
                String str2 = TAG;
                Log.w(str2, "Failed to get description for task=" + i);
            }
        }
    }

    private int startActivityAsUser(Intent intent, Bundle bundle, int i) {
        try {
            return this.mIatm.startActivityAsUser(this.mContext.getIApplicationThread(), this.mContext.getBasePackageName(), this.mContext.getAttributionTag(), intent, intent.resolveTypeIfNeeded(this.mContext.getContentResolver()), (IBinder) null, (String) null, 0, 268435456, (ProfilerInfo) null, bundle, i);
        } catch (RemoteException | Exception unused) {
            return -96;
        }
    }
}
