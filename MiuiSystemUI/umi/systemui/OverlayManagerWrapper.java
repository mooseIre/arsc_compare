package com.android.systemui;

import android.content.om.IOverlayManager;
import android.os.RemoteException;
import android.os.ServiceManager;

public class OverlayManagerWrapper {
    private final IOverlayManager mOverlayManager;

    public OverlayManagerWrapper(IOverlayManager iOverlayManager) {
        this.mOverlayManager = iOverlayManager;
    }

    public OverlayManagerWrapper() {
        this(IOverlayManager.Stub.asInterface(ServiceManager.getService("overlay")));
    }

    public OverlayInfo getOverlayInfo(String str, int i) {
        IOverlayManager iOverlayManager = this.mOverlayManager;
        if (iOverlayManager == null) {
            return null;
        }
        try {
            android.content.om.OverlayInfo overlayInfo = iOverlayManager.getOverlayInfo(str, i);
            if (overlayInfo == null) {
                return null;
            }
            return new OverlayInfo(overlayInfo);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean setEnabled(String str, boolean z, int i) {
        IOverlayManager iOverlayManager = this.mOverlayManager;
        if (iOverlayManager == null) {
            return false;
        }
        try {
            return iOverlayManager.setEnabled(str, z, i);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static class OverlayInfo {
        private final boolean mEnabled;

        public OverlayInfo(android.content.om.OverlayInfo overlayInfo) {
            this.mEnabled = overlayInfo.isEnabled();
            String str = overlayInfo.packageName;
        }

        public boolean isEnabled() {
            return this.mEnabled;
        }
    }
}
