package com.android.systemui.wm;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Region;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.util.MergedConfiguration;
import android.util.Slog;
import android.util.SparseArray;
import android.view.DisplayCutout;
import android.view.DragEvent;
import android.view.IScrollCaptureController;
import android.view.IWindow;
import android.view.IWindowManager;
import android.view.IWindowSessionCallback;
import android.view.InsetsSourceControl;
import android.view.InsetsState;
import android.view.SurfaceControl;
import android.view.SurfaceControlViewHost;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowlessWindowManager;
import com.android.internal.os.IResultReceiver;
import com.android.systemui.wm.DisplayController;
import java.util.HashMap;

public class SystemWindows {
    Context mContext;
    DisplayController mDisplayController;
    private final DisplayController.OnDisplaysChangedListener mDisplayListener;
    private final SparseArray<PerDisplay> mPerDisplay = new SparseArray<>();
    final HashMap<View, SurfaceControlViewHost> mViewRoots = new HashMap<>();
    IWindowManager mWmService;

    public SystemWindows(Context context, DisplayController displayController, IWindowManager iWindowManager) {
        AnonymousClass1 r0 = new DisplayController.OnDisplaysChangedListener() {
            /* class com.android.systemui.wm.SystemWindows.AnonymousClass1 */

            @Override // com.android.systemui.wm.DisplayController.OnDisplaysChangedListener
            public void onDisplayAdded(int i) {
            }

            @Override // com.android.systemui.wm.DisplayController.OnDisplaysChangedListener
            public void onDisplayRemoved(int i) {
            }

            @Override // com.android.systemui.wm.DisplayController.OnDisplaysChangedListener
            public void onDisplayConfigurationChanged(int i, Configuration configuration) {
                PerDisplay perDisplay = (PerDisplay) SystemWindows.this.mPerDisplay.get(i);
                if (perDisplay != null) {
                    perDisplay.updateConfiguration(configuration);
                }
            }
        };
        this.mDisplayListener = r0;
        this.mContext = context;
        this.mWmService = iWindowManager;
        this.mDisplayController = displayController;
        displayController.addDisplayWindowListener(r0);
        try {
            iWindowManager.openSession(new IWindowSessionCallback.Stub(this) {
                /* class com.android.systemui.wm.SystemWindows.AnonymousClass2 */

                public void onAnimatorScaleChanged(float f) {
                }
            });
        } catch (RemoteException e) {
            Slog.e("SystemWindows", "Unable to create layer", e);
        }
    }

    public void addView(View view, WindowManager.LayoutParams layoutParams, int i, int i2) {
        PerDisplay perDisplay = this.mPerDisplay.get(i);
        if (perDisplay == null) {
            perDisplay = new PerDisplay(i);
            this.mPerDisplay.put(i, perDisplay);
        }
        perDisplay.addView(view, layoutParams, i2);
    }

    public void removeView(View view) {
        this.mViewRoots.remove(view).release();
    }

    public void updateViewLayout(View view, ViewGroup.LayoutParams layoutParams) {
        SurfaceControlViewHost surfaceControlViewHost = this.mViewRoots.get(view);
        if (surfaceControlViewHost != null && (layoutParams instanceof WindowManager.LayoutParams)) {
            view.setLayoutParams(layoutParams);
            surfaceControlViewHost.relayout((WindowManager.LayoutParams) layoutParams);
        }
    }

    public void setTouchableRegion(View view, Region region) {
        SurfaceControlViewHost surfaceControlViewHost = this.mViewRoots.get(view);
        if (surfaceControlViewHost != null) {
            SysUiWindowManager windowlessWM = surfaceControlViewHost.getWindowlessWM();
            if (windowlessWM instanceof SysUiWindowManager) {
                windowlessWM.setTouchableRegionForWindow(view, region);
            }
        }
    }

    public SurfaceControl getViewSurface(View view) {
        for (int i = 0; i < this.mPerDisplay.size(); i++) {
            for (int i2 = 0; i2 < this.mPerDisplay.valueAt(i).mWwms.size(); i2++) {
                SurfaceControl surfaceControlForWindow = ((SysUiWindowManager) this.mPerDisplay.valueAt(i).mWwms.valueAt(i2)).getSurfaceControlForWindow(view);
                if (surfaceControlForWindow != null) {
                    return surfaceControlForWindow;
                }
            }
        }
        return null;
    }

    /* access modifiers changed from: private */
    public class PerDisplay {
        final int mDisplayId;
        private final SparseArray<SysUiWindowManager> mWwms = new SparseArray<>();

        PerDisplay(int i) {
            this.mDisplayId = i;
        }

        public void addView(View view, WindowManager.LayoutParams layoutParams, int i) {
            SysUiWindowManager addRoot = addRoot(i);
            if (addRoot == null) {
                Slog.e("SystemWindows", "Unable to create systemui root");
                return;
            }
            SurfaceControlViewHost surfaceControlViewHost = new SurfaceControlViewHost(SystemWindows.this.mContext, SystemWindows.this.mDisplayController.getDisplay(this.mDisplayId), addRoot, true);
            layoutParams.flags |= 16777216;
            surfaceControlViewHost.setView(view, layoutParams);
            SystemWindows.this.mViewRoots.put(view, surfaceControlViewHost);
            try {
                SystemWindows.this.mWmService.setShellRootAccessibilityWindow(this.mDisplayId, i, surfaceControlViewHost.getWindowToken());
            } catch (RemoteException e) {
                Slog.e("SystemWindows", "Error setting accessibility window for " + this.mDisplayId + ":" + i, e);
            }
        }

        /* access modifiers changed from: package-private */
        public SysUiWindowManager addRoot(int i) {
            SurfaceControl surfaceControl;
            SysUiWindowManager sysUiWindowManager = this.mWwms.get(i);
            if (sysUiWindowManager != null) {
                return sysUiWindowManager;
            }
            ContainerWindow containerWindow = new ContainerWindow(SystemWindows.this);
            try {
                surfaceControl = SystemWindows.this.mWmService.addShellRoot(this.mDisplayId, containerWindow, i);
            } catch (RemoteException unused) {
                surfaceControl = null;
            }
            if (surfaceControl == null) {
                Slog.e("SystemWindows", "Unable to get root surfacecontrol for systemui");
                return null;
            }
            SysUiWindowManager sysUiWindowManager2 = new SysUiWindowManager(this.mDisplayId, SystemWindows.this.mDisplayController.getDisplayContext(this.mDisplayId), surfaceControl, containerWindow);
            this.mWwms.put(i, sysUiWindowManager2);
            return sysUiWindowManager2;
        }

        /* access modifiers changed from: package-private */
        public void updateConfiguration(Configuration configuration) {
            for (int i = 0; i < this.mWwms.size(); i++) {
                this.mWwms.valueAt(i).updateConfiguration(configuration);
            }
        }
    }

    public class SysUiWindowManager extends WindowlessWindowManager {
        final int mDisplayId;

        public SysUiWindowManager(int i, Context context, SurfaceControl surfaceControl, ContainerWindow containerWindow) {
            super(context.getResources().getConfiguration(), surfaceControl, (IBinder) null);
            this.mDisplayId = i;
        }

        public int relayout(IWindow iWindow, int i, WindowManager.LayoutParams layoutParams, int i2, int i3, int i4, int i5, long j, Rect rect, Rect rect2, Rect rect3, Rect rect4, Rect rect5, DisplayCutout.ParcelableWrapper parcelableWrapper, MergedConfiguration mergedConfiguration, SurfaceControl surfaceControl, InsetsState insetsState, InsetsSourceControl[] insetsSourceControlArr, Point point, SurfaceControl surfaceControl2) {
            int relayout = SystemWindows.super.relayout(iWindow, i, layoutParams, i2, i3, i4, i5, j, rect, rect2, rect3, rect4, rect5, parcelableWrapper, mergedConfiguration, surfaceControl, insetsState, insetsSourceControlArr, point, surfaceControl2);
            if (relayout != 0) {
                return relayout;
            }
            rect5.set(SystemWindows.this.mDisplayController.getDisplayLayout(this.mDisplayId).stableInsets());
            return 0;
        }

        /* access modifiers changed from: package-private */
        public void updateConfiguration(Configuration configuration) {
            setConfiguration(configuration);
        }

        /* access modifiers changed from: package-private */
        public SurfaceControl getSurfaceControlForWindow(View view) {
            return getSurfaceControl(view);
        }

        /* access modifiers changed from: package-private */
        public void setTouchableRegionForWindow(View view, Region region) {
            IBinder windowToken = view.getWindowToken();
            if (windowToken != null) {
                setTouchRegion(windowToken, region);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public class ContainerWindow extends IWindow.Stub {
        public void closeSystemDialogs(String str) {
        }

        public void dispatchAppVisibility(boolean z) {
        }

        public void dispatchDragEvent(DragEvent dragEvent) {
        }

        public void dispatchGetNewSurface() {
        }

        public void dispatchPointerCaptureChanged(boolean z) {
        }

        public void dispatchSystemUiVisibilityChanged(int i, int i2, int i3, int i4) {
        }

        public void dispatchWallpaperCommand(String str, int i, int i2, int i3, Bundle bundle, boolean z) {
        }

        public void dispatchWallpaperOffsets(float f, float f2, float f3, float f4, float f5, boolean z) {
        }

        public void dispatchWindowShown() {
        }

        public void executeCommand(String str, String str2, ParcelFileDescriptor parcelFileDescriptor) {
        }

        public void hideInsets(int i, boolean z) {
        }

        public void insetsChanged(InsetsState insetsState) {
        }

        public void insetsControlChanged(InsetsState insetsState, InsetsSourceControl[] insetsSourceControlArr) {
        }

        public void locationInParentDisplayChanged(Point point) {
        }

        public void moved(int i, int i2) {
        }

        public void notifyCastMode(boolean z) {
        }

        public void notifyProjectionMode(boolean z) {
        }

        public void notifyRotationChanged(boolean z) {
        }

        public void requestAppKeyboardShortcuts(IResultReceiver iResultReceiver, int i) {
        }

        public void resized(Rect rect, Rect rect2, Rect rect3, Rect rect4, boolean z, MergedConfiguration mergedConfiguration, Rect rect5, boolean z2, boolean z3, int i, DisplayCutout.ParcelableWrapper parcelableWrapper) {
        }

        public void showInsets(int i, boolean z) {
        }

        public void updatePointerIcon(float f, float f2) {
        }

        public void windowFocusChanged(boolean z, boolean z2) {
        }

        ContainerWindow(SystemWindows systemWindows) {
        }

        public void requestScrollCapture(IScrollCaptureController iScrollCaptureController) {
            try {
                iScrollCaptureController.onClientUnavailable();
            } catch (RemoteException unused) {
            }
        }
    }
}
