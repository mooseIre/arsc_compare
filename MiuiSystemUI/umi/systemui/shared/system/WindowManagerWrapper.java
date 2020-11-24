package com.android.systemui.shared.system;

import android.os.RemoteException;
import android.util.Log;
import android.view.SurfaceControl;
import android.view.WindowManagerGlobal;
import com.android.systemui.shared.system.PinnedStackListenerForwarder;

public class WindowManagerWrapper {
    private static final WindowManagerWrapper sInstance = new WindowManagerWrapper();
    private PinnedStackListenerForwarder mPinnedStackListenerForwarder = new PinnedStackListenerForwarder();

    public static WindowManagerWrapper getInstance() {
        return sInstance;
    }

    public void setNavBarVirtualKeyHapticFeedbackEnabled(boolean z) {
        try {
            WindowManagerGlobal.getWindowManagerService().setNavBarVirtualKeyHapticFeedbackEnabled(z);
        } catch (RemoteException e) {
            Log.w("WindowManagerWrapper", "Failed to enable or disable navigation bar button haptics: ", e);
        }
    }

    public void setPipVisibility(boolean z) {
        try {
            WindowManagerGlobal.getWindowManagerService().setPipVisibility(z);
        } catch (RemoteException e) {
            Log.e("WindowManagerWrapper", "Unable to reach window manager", e);
        }
    }

    public boolean hasSoftNavigationBar(int i) {
        try {
            return WindowManagerGlobal.getWindowManagerService().hasNavigationBar(i);
        } catch (RemoteException unused) {
            return false;
        }
    }

    public int getNavBarPosition(int i) {
        try {
            return WindowManagerGlobal.getWindowManagerService().getNavBarPosition(i);
        } catch (RemoteException unused) {
            Log.w("WindowManagerWrapper", "Failed to get nav bar position");
            return -1;
        }
    }

    public void addPinnedStackListener(PinnedStackListenerForwarder.PinnedStackListener pinnedStackListener) throws RemoteException {
        this.mPinnedStackListenerForwarder.addListener(pinnedStackListener);
        WindowManagerGlobal.getWindowManagerService().registerPinnedStackListener(0, this.mPinnedStackListenerForwarder);
    }

    public SurfaceControl mirrorDisplay(int i) {
        try {
            SurfaceControl surfaceControl = new SurfaceControl();
            WindowManagerGlobal.getWindowManagerService().mirrorDisplay(i, surfaceControl);
            return surfaceControl;
        } catch (RemoteException e) {
            Log.e("WindowManagerWrapper", "Unable to reach window manager", e);
            return null;
        }
    }
}
