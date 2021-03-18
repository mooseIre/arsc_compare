package com.android.systemui.statusbar.policy;

import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.StatusBarManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.UserHandle;
import android.os.UserManager;
import com.android.settingslib.Utils;
import com.android.systemui.BootCompleteCache;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.statusbar.policy.LocationController;
import com.android.systemui.statusbar.policy.LocationControllerImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class LocationControllerImpl extends BroadcastReceiver implements LocationController {
    private static final int[] mHighPowerRequestAppOpArray = {42};
    private AppOpsManager mAppOpsManager;
    private boolean mAreActiveLocationRequests;
    private BootCompleteCache mBootCompleteCache;
    private BroadcastDispatcher mBroadcastDispatcher;
    private Context mContext;
    private final H mHandler;

    public LocationControllerImpl(Context context, Looper looper, Looper looper2, BroadcastDispatcher broadcastDispatcher, BootCompleteCache bootCompleteCache) {
        this.mContext = context;
        this.mBroadcastDispatcher = broadcastDispatcher;
        this.mBootCompleteCache = bootCompleteCache;
        this.mHandler = new H(looper);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.location.HIGH_POWER_REQUEST_CHANGE");
        intentFilter.addAction("android.location.MODE_CHANGED");
        intentFilter.addAction("android.location.GPS_ENABLED_CHANGE");
        intentFilter.addAction("android.location.GPS_FIX_CHANGE");
        this.mBroadcastDispatcher.registerReceiverWithHandler(this, intentFilter, new Handler(looper2), UserHandle.ALL);
        this.mAppOpsManager = (AppOpsManager) context.getSystemService("appops");
        StatusBarManager statusBarManager = (StatusBarManager) context.getSystemService("statusbar");
        updateActiveLocationRequests();
    }

    public void addCallback(LocationController.LocationChangeCallback locationChangeCallback) {
        this.mHandler.obtainMessage(3, locationChangeCallback).sendToTarget();
        this.mHandler.sendEmptyMessage(1);
    }

    public void removeCallback(LocationController.LocationChangeCallback locationChangeCallback) {
        this.mHandler.obtainMessage(4, locationChangeCallback).sendToTarget();
    }

    @Override // com.android.systemui.statusbar.policy.LocationController
    public boolean setLocationEnabled(boolean z) {
        int currentUser = ActivityManager.getCurrentUser();
        if (isUserLocationRestricted(currentUser)) {
            return false;
        }
        Utils.updateLocationEnabled(this.mContext, z, currentUser, 2);
        return true;
    }

    @Override // com.android.systemui.statusbar.policy.LocationController
    public boolean isLocationEnabled() {
        return this.mBootCompleteCache.isBootComplete() && ((LocationManager) this.mContext.getSystemService("location")).isLocationEnabledForUser(UserHandle.of(ActivityManager.getCurrentUser()));
    }

    @Override // com.android.systemui.statusbar.policy.LocationController
    public boolean isLocationActive() {
        return this.mAreActiveLocationRequests;
    }

    private boolean isUserLocationRestricted(int i) {
        return ((UserManager) this.mContext.getSystemService("user")).hasUserRestriction("no_share_location", UserHandle.of(i));
    }

    /* access modifiers changed from: protected */
    public boolean areActiveHighPowerLocationRequests() {
        List packagesForOps = this.mAppOpsManager.getPackagesForOps(mHighPowerRequestAppOpArray);
        if (packagesForOps != null) {
            int size = packagesForOps.size();
            for (int i = 0; i < size; i++) {
                List ops = ((AppOpsManager.PackageOps) packagesForOps.get(i)).getOps();
                if (ops != null) {
                    int size2 = ops.size();
                    for (int i2 = 0; i2 < size2; i2++) {
                        AppOpsManager.OpEntry opEntry = (AppOpsManager.OpEntry) ops.get(i2);
                        if (opEntry.getOp() == 42 && opEntry.isRunning()) {
                            return true;
                        }
                    }
                    continue;
                }
            }
        }
        return false;
    }

    private void updateActiveLocationRequests() {
        boolean z = this.mAreActiveLocationRequests;
        boolean areActiveHighPowerLocationRequests = areActiveHighPowerLocationRequests();
        this.mAreActiveLocationRequests = areActiveHighPowerLocationRequests;
        if (areActiveHighPowerLocationRequests != z) {
            this.mHandler.sendEmptyMessage(2);
        }
    }

    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if ("android.location.HIGH_POWER_REQUEST_CHANGE".equals(action)) {
            updateActiveLocationRequests();
        } else if ("android.location.MODE_CHANGED".equals(action)) {
            this.mHandler.sendEmptyMessage(1);
        } else if ("android.location.GPS_ENABLED_CHANGE".equals(action) || "android.location.GPS_FIX_CHANGE".equals(action)) {
            updateLocationStatus(intent);
        }
    }

    /* access modifiers changed from: private */
    public final class H extends Handler {
        private ArrayList<LocationController.LocationChangeCallback> mSettingsChangeCallbacks = new ArrayList<>();

        H(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message message) {
            int i = message.what;
            if (i == 1) {
                locationSettingsChanged();
            } else if (i == 2) {
                locationActiveChanged();
            } else if (i == 3) {
                this.mSettingsChangeCallbacks.add((LocationController.LocationChangeCallback) message.obj);
            } else if (i == 4) {
                this.mSettingsChangeCallbacks.remove((LocationController.LocationChangeCallback) message.obj);
            } else if (i == 100) {
                locationStatusChanged((Intent) message.obj);
            }
        }

        private void locationActiveChanged() {
            com.android.systemui.util.Utils.safeForeach(this.mSettingsChangeCallbacks, new Consumer() {
                /* class com.android.systemui.statusbar.policy.$$Lambda$LocationControllerImpl$H$vKTe7eMzgWgCJvXCt8UIIkFyg78 */

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    LocationControllerImpl.H.this.lambda$locationActiveChanged$0$LocationControllerImpl$H((LocationController.LocationChangeCallback) obj);
                }
            });
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$locationActiveChanged$0 */
        public /* synthetic */ void lambda$locationActiveChanged$0$LocationControllerImpl$H(LocationController.LocationChangeCallback locationChangeCallback) {
            locationChangeCallback.onLocationActiveChanged(LocationControllerImpl.this.mAreActiveLocationRequests);
        }

        private void locationSettingsChanged() {
            com.android.systemui.util.Utils.safeForeach(this.mSettingsChangeCallbacks, new Consumer(LocationControllerImpl.this.isLocationEnabled()) {
                /* class com.android.systemui.statusbar.policy.$$Lambda$LocationControllerImpl$H$xXVOboFsQOHoRYEFzvZuIOYh0 */
                public final /* synthetic */ boolean f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    ((LocationController.LocationChangeCallback) obj).onLocationSettingsChanged(this.f$0);
                }
            });
        }

        private void locationStatusChanged(Intent intent) {
            com.android.systemui.util.Utils.safeForeach(this.mSettingsChangeCallbacks, new Consumer(intent) {
                /* class com.android.systemui.statusbar.policy.$$Lambda$LocationControllerImpl$H$30hp0_d_kTB_SAwrFdJsBzk0umU */
                public final /* synthetic */ Intent f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    ((LocationController.LocationChangeCallback) obj).onLocationStatusChanged(this.f$0);
                }
            });
        }
    }

    /* access modifiers changed from: protected */
    public void updateLocationStatus(Intent intent) {
        Message obtainMessage = this.mHandler.obtainMessage();
        obtainMessage.what = 100;
        obtainMessage.obj = intent;
        this.mHandler.sendMessage(obtainMessage);
    }
}
