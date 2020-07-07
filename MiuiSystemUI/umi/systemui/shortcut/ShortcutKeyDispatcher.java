package com.android.systemui.shortcut;

import android.app.ActivityManager;
import android.app.ActivityManagerCompat;
import android.os.RemoteException;
import android.util.ArraySet;
import android.util.Log;
import android.view.IWindowManager;
import android.view.WindowManagerGlobal;
import com.android.systemui.SystemUI;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.shortcut.ShortcutKeyServiceProxy;
import com.android.systemui.stackdivider.Divider;
import com.android.systemui.stackdivider.DividerSnapAlgorithm;
import com.android.systemui.stackdivider.DividerView;
import java.util.List;

public class ShortcutKeyDispatcher extends SystemUI implements ShortcutKeyServiceProxy.Callbacks {
    private ShortcutKeyServiceProxy mShortcutKeyServiceProxy = new ShortcutKeyServiceProxy(this);
    private IWindowManager mWindowManagerService = WindowManagerGlobal.getWindowManagerService();

    public ShortcutKeyDispatcher() {
        ActivityManagerCompat.getService();
    }

    public void registerShortcutKey(long j) {
        try {
            this.mWindowManagerService.registerShortcutKey(j, this.mShortcutKeyServiceProxy);
        } catch (RemoteException unused) {
        }
    }

    public void onShortcutKeyPressed(long j) {
        int i = this.mContext.getResources().getConfiguration().orientation;
        if ((j == 281474976710727L || j == 281474976710728L) && i == 2) {
            handleDockKey(j);
        }
    }

    public void start() {
        registerShortcutKey(281474976710727L);
        registerShortcutKey(281474976710728L);
    }

    private void handleDockKey(long j) {
        DividerSnapAlgorithm.SnapTarget snapTarget;
        try {
            if (this.mWindowManagerService.getDockedStackSide() == -1) {
                Recents recents = (Recents) getComponent(Recents.class);
                int i = j == 281474976710727L ? 0 : 1;
                List<ActivityManager.RecentTaskInfo> recentTasks = SystemServicesProxy.getInstance(this.mContext).getRecentTasks(1, -2, false, new ArraySet());
                recents.showRecentApps(false, false);
                if (!recentTasks.isEmpty()) {
                    SystemServicesProxy.getInstance(this.mContext).startTaskInDockedMode(recentTasks.get(0).id, i);
                    return;
                }
                return;
            }
            DividerView view = ((Divider) getComponent(Divider.class)).getView();
            DividerSnapAlgorithm snapAlgorithm = view.getSnapAlgorithm();
            DividerSnapAlgorithm.SnapTarget calculateNonDismissingSnapTarget = snapAlgorithm.calculateNonDismissingSnapTarget(view.getCurrentPosition());
            if (j == 281474976710727L) {
                snapTarget = snapAlgorithm.getPreviousTarget(calculateNonDismissingSnapTarget);
            } else {
                snapTarget = snapAlgorithm.getNextTarget(calculateNonDismissingSnapTarget);
            }
            view.startDragging(true, false);
            view.stopDragging(snapTarget.position, 0.0f, false, true);
        } catch (RemoteException unused) {
            Log.e("ShortcutKeyDispatcher", "handleDockKey() failed.");
        }
    }
}
