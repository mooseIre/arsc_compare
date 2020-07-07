package com.android.systemui;

import android.content.ContextCompat;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.view.IWindowManager;
import android.view.IWindowManagerCompat;
import com.android.systemui.vendor.FsGesturePolicy;
import com.android.systemui.vendor.HeadsetPolicy;
import com.android.systemui.vendor.OrientationPolicy;

public class VendorServices extends SystemUI {
    private FsGesturePolicy mFsGesturePolicy;

    public void start() {
        boolean z;
        new HeadsetPolicy(this.mContext);
        try {
            z = IWindowManagerCompat.hasNavigationBar(IWindowManager.Stub.asInterface(ServiceManager.getService("window")), ContextCompat.getDisplayId(this.mContext));
        } catch (RemoteException unused) {
            z = false;
        }
        if (z) {
            new OrientationPolicy(this.mContext);
            this.mFsGesturePolicy = new FsGesturePolicy(this.mContext);
            this.mFsGesturePolicy.start();
        }
    }
}
