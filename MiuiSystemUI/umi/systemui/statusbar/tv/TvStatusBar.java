package com.android.systemui.statusbar.tv;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import com.android.internal.statusbar.IStatusBarService;
import com.android.systemui.SystemUI;
import com.android.systemui.assist.AssistManager;
import com.android.systemui.statusbar.CommandQueue;
import dagger.Lazy;

public class TvStatusBar extends SystemUI implements CommandQueue.Callbacks {
    private final Lazy<AssistManager> mAssistManagerLazy;
    private final CommandQueue mCommandQueue;

    public TvStatusBar(Context context, CommandQueue commandQueue, Lazy<AssistManager> lazy) {
        super(context);
        this.mCommandQueue = commandQueue;
        this.mAssistManagerLazy = lazy;
    }

    @Override // com.android.systemui.SystemUI
    public void start() {
        IStatusBarService asInterface = IStatusBarService.Stub.asInterface(ServiceManager.getService("statusbar"));
        this.mCommandQueue.addCallback((CommandQueue.Callbacks) this);
        try {
            asInterface.registerStatusBar(this.mCommandQueue);
        } catch (RemoteException unused) {
        }
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void animateExpandNotificationsPanel() {
        startSystemActivity(new Intent("com.android.tv.action.OPEN_NOTIFICATIONS_PANEL"));
    }

    private void startSystemActivity(Intent intent) {
        ActivityInfo activityInfo;
        ResolveInfo resolveActivity = this.mContext.getPackageManager().resolveActivity(intent, 1048576);
        if (resolveActivity != null && (activityInfo = resolveActivity.activityInfo) != null) {
            intent.setPackage(activityInfo.packageName);
            this.mContext.startActivityAsUser(intent, UserHandle.CURRENT);
        }
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void startAssist(Bundle bundle) {
        this.mAssistManagerLazy.get().startAssist(bundle);
    }
}
