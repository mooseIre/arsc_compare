package com.android.systemui;

import android.app.WallpaperManager;
import android.content.ContextCompat;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.view.IWindowManager;
import android.view.IWindowManagerCompat;
import android.view.View;
import android.view.ViewGroup;
import com.android.systemui.miui.ActivityObserver;
import com.android.systemui.miui.PackageEventReceiver;
import com.android.systemui.statusbar.phone.StatusBarWindowManager;
import com.android.systemui.vendor.FsGesturePolicy;
import com.android.systemui.vendor.HeadsetPolicy;
import com.android.systemui.vendor.OrientationPolicy;

public class VendorServices extends SystemUI implements ActivityObserver.ActivityObserverCallback, PackageEventReceiver {
    private FsGesturePolicy mFsGesturePolicy;
    private String mLauncherPackage;
    private boolean mSupportsWallpaperZoom;
    /* access modifiers changed from: private */
    public WallpaperManager mWallpaperManager;

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
        this.mWallpaperManager = (WallpaperManager) this.mContext.getSystemService(WallpaperManager.class);
        ((ActivityObserver) Dependency.get(ActivityObserver.class)).addCallback(this);
        onLauncherSupportsZoomChanged(false);
        this.mLauncherPackage = ((ActivityObserver) Dependency.get(ActivityObserver.class)).getLauncherPackage();
        evaluateLauncherSupportsZoom();
    }

    public void onLauncherPackageChanged(String str) {
        this.mLauncherPackage = str;
        evaluateLauncherSupportsZoom();
    }

    public void onPackageAdded(int i, String str, boolean z) {
        if (str.equals(this.mLauncherPackage)) {
            evaluateLauncherSupportsZoom();
        }
    }

    private void evaluateLauncherSupportsZoom() {
        if (Build.VERSION.SDK_INT >= 30) {
            boolean supportsZoomOut = supportsZoomOut(this.mLauncherPackage);
            if (this.mSupportsWallpaperZoom != supportsZoomOut) {
                onLauncherSupportsZoomChanged(supportsZoomOut);
            }
            this.mSupportsWallpaperZoom = supportsZoomOut;
            Log.i("VendorServices", "launcher changed, supports wallpaper zoom: " + this.mSupportsWallpaperZoom);
        }
    }

    private void onLauncherSupportsZoomChanged(final boolean z) {
        final ViewGroup statusBarView = ((StatusBarWindowManager) Dependency.get(StatusBarWindowManager.class)).getStatusBarView();
        if (statusBarView.isAttachedToWindow()) {
            statusBarView.getWindowToken();
        } else {
            statusBarView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                public void onViewDetachedFromWindow(View view) {
                }

                public void onViewAttachedToWindow(View view) {
                    statusBarView.removeOnAttachStateChangeListener(this);
                    WallpaperManager unused = VendorServices.this.mWallpaperManager;
                    statusBarView.getWindowToken();
                    boolean z = z;
                }
            });
        }
    }

    private boolean supportsZoomOut(String str) {
        PackageInfo packageInfo;
        ApplicationInfo applicationInfo;
        Bundle bundle;
        try {
            packageInfo = this.mContext.getPackageManager().getPackageInfo(str, 128);
        } catch (Exception unused) {
            packageInfo = null;
        }
        if (packageInfo == null || (applicationInfo = packageInfo.applicationInfo) == null || (bundle = applicationInfo.metaData) == null) {
            return false;
        }
        return bundle.getBoolean("miui.supports_wallpaper_zoom_out", false);
    }
}
