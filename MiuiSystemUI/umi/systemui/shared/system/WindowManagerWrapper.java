package com.android.systemui.shared.system;

import android.os.RemoteException;
import android.view.WindowManagerGlobal;
import com.android.systemui.shared.system.PinnedStackListenerForwarder;

public class WindowManagerWrapper {
    private static final WindowManagerWrapper sInstance = new WindowManagerWrapper();
    private PinnedStackListenerForwarder mPinnedStackListenerForwarder = new PinnedStackListenerForwarder();

    public static WindowManagerWrapper getInstance() {
        return sInstance;
    }

    public void addPinnedStackListener(PinnedStackListenerForwarder.PinnedStackListener pinnedStackListener) throws RemoteException {
        this.mPinnedStackListenerForwarder.addListener(pinnedStackListener);
        WindowManagerGlobal.getWindowManagerService().registerPinnedStackListener(0, this.mPinnedStackListenerForwarder);
    }
}
