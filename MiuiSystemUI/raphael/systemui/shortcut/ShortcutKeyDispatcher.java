package com.android.systemui.shortcut;

import android.content.Context;
import android.os.RemoteException;
import android.view.IWindowManager;
import android.view.WindowManagerGlobal;
import com.android.systemui.SystemUI;
import com.android.systemui.recents.Recents;
import com.android.systemui.shortcut.ShortcutKeyServiceProxy;
import com.android.systemui.stackdivider.Divider;
import com.android.systemui.stackdivider.DividerSnapAlgorithm;
import com.android.systemui.stackdivider.DividerView;

public class ShortcutKeyDispatcher extends SystemUI implements ShortcutKeyServiceProxy.Callbacks {
    private final Divider mDivider;
    private final Recents mRecents;
    private ShortcutKeyServiceProxy mShortcutKeyServiceProxy = new ShortcutKeyServiceProxy(this);
    private IWindowManager mWindowManagerService = WindowManagerGlobal.getWindowManagerService();

    public ShortcutKeyDispatcher(Context context, Divider divider, Recents recents) {
        super(context);
        this.mDivider = divider;
        this.mRecents = recents;
    }

    public void registerShortcutKey(long j) {
        try {
            this.mWindowManagerService.registerShortcutKey(j, this.mShortcutKeyServiceProxy);
        } catch (RemoteException unused) {
        }
    }

    @Override // com.android.systemui.shortcut.ShortcutKeyServiceProxy.Callbacks
    public void onShortcutKeyPressed(long j) {
        int i = this.mContext.getResources().getConfiguration().orientation;
        if ((j == 281474976710727L || j == 281474976710728L) && i == 2) {
            handleDockKey(j);
        }
    }

    @Override // com.android.systemui.SystemUI
    public void start() {
        registerShortcutKey(281474976710727L);
        registerShortcutKey(281474976710728L);
    }

    private void handleDockKey(long j) {
        DividerSnapAlgorithm.SnapTarget snapTarget;
        Divider divider = this.mDivider;
        int i = 0;
        if (divider == null || !divider.isDividerVisible()) {
            Recents recents = this.mRecents;
            if (j != 281474976710727L) {
                i = 1;
            }
            recents.splitPrimaryTask(i, null, -1);
            return;
        }
        DividerView view = this.mDivider.getView();
        DividerSnapAlgorithm snapAlgorithm = view.getSnapAlgorithm();
        DividerSnapAlgorithm.SnapTarget calculateNonDismissingSnapTarget = snapAlgorithm.calculateNonDismissingSnapTarget(view.getCurrentPosition());
        if (j == 281474976710727L) {
            snapTarget = snapAlgorithm.getPreviousTarget(calculateNonDismissingSnapTarget);
        } else {
            snapTarget = snapAlgorithm.getNextTarget(calculateNonDismissingSnapTarget);
        }
        view.startDragging(true, false);
        view.stopDragging(snapTarget.position, 0.0f, false, true);
    }
}
