package com.android.systemui.keyguard;

import android.app.ActivityManager;
import android.app.ActivityManagerCompat;
import android.app.ActivityOptions;
import android.app.IActivityManager;
import android.app.ProfilerInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.recents.misc.SystemServicesProxy;

public class WorkLockActivityController {
    private final Context mContext;
    private final IActivityManager mIam;
    private final SystemServicesProxy.TaskStackListener mLockListener;
    private final SystemServicesProxy mSsp;

    public WorkLockActivityController(Context context) {
        this(context, SystemServicesProxy.getInstance(context), ActivityManagerCompat.getService());
    }

    @VisibleForTesting
    WorkLockActivityController(Context context, SystemServicesProxy systemServicesProxy, IActivityManager iActivityManager) {
        this.mLockListener = new SystemServicesProxy.TaskStackListener() {
            public void onTaskProfileLocked(int i, int i2) {
                WorkLockActivityController.this.startWorkChallengeInTask(i, i2);
            }
        };
        this.mContext = context;
        this.mSsp = systemServicesProxy;
        this.mIam = iActivityManager;
        this.mSsp.registerTaskStackListener(this.mLockListener);
    }

    /* access modifiers changed from: private */
    public void startWorkChallengeInTask(int i, int i2) {
        Intent addFlags = new Intent("android.app.action.CONFIRM_DEVICE_CREDENTIAL_WITH_USER").setComponent(new ComponentName(this.mContext, WorkLockActivity.class)).putExtra("android.intent.extra.USER_ID", i2).addFlags(67239936);
        ActivityOptions makeBasic = ActivityOptions.makeBasic();
        makeBasic.setLaunchTaskId(i);
        makeBasic.setTaskOverlay(true, false);
        if (!ActivityManager.isStartResultSuccessful(startActivityAsUser(addFlags, makeBasic.toBundle(), -2))) {
            this.mSsp.removeTask(i, true);
        }
    }

    private int startActivityAsUser(Intent intent, Bundle bundle, int i) {
        try {
            Intent intent2 = intent;
            return this.mIam.startActivityAsUser(this.mContext.getIApplicationThread(), this.mContext.getBasePackageName(), intent, intent.resolveTypeIfNeeded(this.mContext.getContentResolver()), (IBinder) null, (String) null, 0, 268435456, (ProfilerInfo) null, bundle, i);
        } catch (RemoteException | Exception unused) {
            return -96;
        }
    }
}
