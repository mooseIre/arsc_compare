package com.android.systemui.shared.system;

import android.os.RemoteException;
import android.view.IPinnedStackListener;
import android.view.WindowManagerGlobal;

public class WindowManagerWrapper {
    private static final WindowManagerWrapper sInstance = new WindowManagerWrapper();
    private PinnedStackListenerForwarder mPinnedStackListenerForwarder = new PinnedStackListenerForwarder();

    public static WindowManagerWrapper getInstance() {
        return sInstance;
    }

    public void addPinnedStackListener(IPinnedStackListener iPinnedStackListener) throws RemoteException {
        this.mPinnedStackListenerForwarder.addListener(iPinnedStackListener);
        WindowManagerGlobal.getWindowManagerService().registerPinnedStackListener(0, this.mPinnedStackListenerForwarder);
    }
}
