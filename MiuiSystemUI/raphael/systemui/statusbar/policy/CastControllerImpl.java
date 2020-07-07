package com.android.systemui.statusbar.policy;

import android.content.Context;
import android.media.MediaRouter;
import android.media.projection.MediaProjectionInfo;
import android.media.projection.MediaProjectionManager;
import android.os.Handler;
import android.util.ArrayMap;
import android.util.Log;
import com.android.systemui.statusbar.policy.CastController;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;
import java.util.UUID;

public class CastControllerImpl implements CastController {
    /* access modifiers changed from: private */
    public static final boolean DEBUG = Log.isLoggable("CastController", 3);
    private boolean mCallbackRegistered;
    private final ArrayList<CastController.Callback> mCallbacks = new ArrayList<>();
    private final Context mContext;
    private boolean mDiscovering;
    private final Object mDiscoveringLock = new Object();
    private final MediaRouter.SimpleCallback mMediaCallback = new MediaRouter.SimpleCallback() {
        public void onRouteAdded(MediaRouter mediaRouter, MediaRouter.RouteInfo routeInfo) {
            if (CastControllerImpl.DEBUG) {
                Log.d("CastController", "onRouteAdded: " + CastControllerImpl.routeToString(routeInfo));
            }
            CastControllerImpl.this.updateRemoteDisplays();
        }

        public void onRouteChanged(MediaRouter mediaRouter, MediaRouter.RouteInfo routeInfo) {
            if (CastControllerImpl.DEBUG) {
                Log.d("CastController", "onRouteChanged: " + CastControllerImpl.routeToString(routeInfo));
            }
            CastControllerImpl.this.updateRemoteDisplays();
        }

        public void onRouteRemoved(MediaRouter mediaRouter, MediaRouter.RouteInfo routeInfo) {
            if (CastControllerImpl.DEBUG) {
                Log.d("CastController", "onRouteRemoved: " + CastControllerImpl.routeToString(routeInfo));
            }
            CastControllerImpl.this.updateRemoteDisplays();
        }

        public void onRouteSelected(MediaRouter mediaRouter, int i, MediaRouter.RouteInfo routeInfo) {
            if (CastControllerImpl.DEBUG) {
                Log.d("CastController", "onRouteSelected(" + i + "): " + CastControllerImpl.routeToString(routeInfo));
            }
            CastControllerImpl.this.updateRemoteDisplays();
        }

        public void onRouteUnselected(MediaRouter mediaRouter, int i, MediaRouter.RouteInfo routeInfo) {
            if (CastControllerImpl.DEBUG) {
                Log.d("CastController", "onRouteUnselected(" + i + "): " + CastControllerImpl.routeToString(routeInfo));
            }
            CastControllerImpl.this.updateRemoteDisplays();
        }
    };
    private final MediaRouter mMediaRouter;
    private MediaProjectionInfo mProjection;
    private final MediaProjectionManager.Callback mProjectionCallback = new MediaProjectionManager.Callback() {
        public void onStart(MediaProjectionInfo mediaProjectionInfo) {
            CastControllerImpl.this.setProjection(mediaProjectionInfo, true);
        }

        public void onStop(MediaProjectionInfo mediaProjectionInfo) {
            CastControllerImpl.this.setProjection(mediaProjectionInfo, false);
        }
    };
    private final Object mProjectionLock = new Object();
    private final MediaProjectionManager mProjectionManager;
    private final ArrayMap<String, MediaRouter.RouteInfo> mRoutes = new ArrayMap<>();

    public CastControllerImpl(Context context) {
        this.mContext = context;
        this.mMediaRouter = (MediaRouter) context.getSystemService("media_router");
        this.mProjectionManager = (MediaProjectionManager) context.getSystemService("media_projection");
        this.mProjection = this.mProjectionManager.getActiveProjectionInfo();
        this.mProjectionManager.addCallback(this.mProjectionCallback, new Handler());
        if (DEBUG) {
            Log.d("CastController", "new CastController()");
        }
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("CastController state:");
        printWriter.print("  mDiscovering=");
        printWriter.println(this.mDiscovering);
        printWriter.print("  mCallbackRegistered=");
        printWriter.println(this.mCallbackRegistered);
        printWriter.print("  mCallbacks.size=");
        printWriter.println(this.mCallbacks.size());
        printWriter.print("  mRoutes.size=");
        printWriter.println(this.mRoutes.size());
        for (int i = 0; i < this.mRoutes.size(); i++) {
            printWriter.print("    ");
            printWriter.println(routeToString(this.mRoutes.valueAt(i)));
        }
        printWriter.print("  mProjection=");
        printWriter.println(this.mProjection);
    }

    /* access modifiers changed from: private */
    public void setProjection(MediaProjectionInfo mediaProjectionInfo, boolean z) {
        boolean z2;
        MediaProjectionInfo mediaProjectionInfo2 = this.mProjection;
        synchronized (this.mProjectionLock) {
            boolean equals = Objects.equals(mediaProjectionInfo, this.mProjection);
            z2 = true;
            if (z && !equals) {
                this.mProjection = mediaProjectionInfo;
            } else if (z || !equals) {
                z2 = false;
            } else {
                this.mProjection = null;
            }
        }
        if (z2) {
            if (DEBUG) {
                Log.d("CastController", "setProjection: " + mediaProjectionInfo2 + " -> " + this.mProjection);
            }
            fireOnCastDevicesChanged();
        }
    }

    /* access modifiers changed from: private */
    public void updateRemoteDisplays() {
        synchronized (this.mRoutes) {
            this.mRoutes.clear();
            int routeCount = this.mMediaRouter.getRouteCount();
            for (int i = 0; i < routeCount; i++) {
                MediaRouter.RouteInfo routeAt = this.mMediaRouter.getRouteAt(i);
                if (routeAt.isEnabled()) {
                    if (routeAt.matchesTypes(4)) {
                        ensureTagExists(routeAt);
                        this.mRoutes.put(routeAt.getTag().toString(), routeAt);
                    }
                }
            }
            MediaRouter.RouteInfo selectedRoute = this.mMediaRouter.getSelectedRoute(4);
            if (selectedRoute != null && !selectedRoute.isDefault()) {
                ensureTagExists(selectedRoute);
                this.mRoutes.put(selectedRoute.getTag().toString(), selectedRoute);
            }
        }
        fireOnCastDevicesChanged();
    }

    private void ensureTagExists(MediaRouter.RouteInfo routeInfo) {
        if (routeInfo.getTag() == null) {
            routeInfo.setTag(UUID.randomUUID().toString());
        }
    }

    private void fireOnCastDevicesChanged() {
        Iterator<CastController.Callback> it = this.mCallbacks.iterator();
        while (it.hasNext()) {
            fireOnCastDevicesChanged(it.next());
        }
    }

    private void fireOnCastDevicesChanged(CastController.Callback callback) {
        callback.onCastDevicesChanged();
    }

    /* access modifiers changed from: private */
    public static String routeToString(MediaRouter.RouteInfo routeInfo) {
        if (routeInfo == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(routeInfo.getName());
        sb.append('/');
        sb.append(routeInfo.getDescription());
        sb.append('@');
        sb.append(routeInfo.getDeviceAddress());
        sb.append(",status=");
        sb.append(routeInfo.getStatus());
        if (routeInfo.isDefault()) {
            sb.append(",default");
        }
        if (routeInfo.isEnabled()) {
            sb.append(",enabled");
        }
        if (routeInfo.isConnecting()) {
            sb.append(",connecting");
        }
        if (routeInfo.isSelected()) {
            sb.append(",selected");
        }
        sb.append(",id=");
        sb.append(routeInfo.getTag());
        return sb.toString();
    }
}
