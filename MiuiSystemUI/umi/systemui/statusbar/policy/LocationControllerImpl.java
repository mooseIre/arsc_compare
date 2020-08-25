package com.android.systemui.statusbar.policy;

import android.app.AppOpsManager;
import android.app.NotificationManager;
import android.app.StatusBarManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.UserHandle;
import android.os.UserHandleCompat;
import android.os.UserManager;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.qs.tiles.TilesHelper;
import com.android.systemui.statusbar.policy.LocationController;
import com.android.systemui.statusbar.policy.LocationControllerImpl;
import com.android.systemui.util.Utils;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class LocationControllerImpl extends BroadcastReceiver implements LocationController {
    private static final int[] mHighPowerRequestAppOpArray = {42};
    private AppOpsManager mAppOpsManager;
    /* access modifiers changed from: private */
    public boolean mAreActiveLocationRequests;
    private Context mContext;
    private final H mHandler = new H();
    private NotificationManager mNotificationManager;
    /* access modifiers changed from: private */
    public ArrayList<LocationController.LocationChangeCallback> mSettingsChangeCallbacks = new ArrayList<>();

    public LocationControllerImpl(Context context, Looper looper) {
        this.mContext = context;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.location.GPS_ENABLED_CHANGE");
        intentFilter.addAction("android.location.GPS_FIX_CHANGE");
        intentFilter.addAction("android.location.HIGH_POWER_REQUEST_CHANGE");
        intentFilter.addAction("android.location.MODE_CHANGED");
        context.registerReceiverAsUser(this, UserHandle.ALL, intentFilter, (String) null, new Handler(looper));
        this.mAppOpsManager = (AppOpsManager) context.getSystemService("appops");
        StatusBarManager statusBarManager = (StatusBarManager) context.getSystemService("statusbar");
        NotificationManager notificationManager = (NotificationManager) context.getSystemService("notification");
        this.mNotificationManager = notificationManager;
        notificationManager.cancelAsUser((String) null, 252119, UserHandle.CURRENT);
        updateActiveLocationRequests();
    }

    public void addCallback(LocationController.LocationChangeCallback locationChangeCallback) {
        this.mSettingsChangeCallbacks.add(locationChangeCallback);
        this.mHandler.sendEmptyMessage(1);
    }

    public void removeCallback(LocationController.LocationChangeCallback locationChangeCallback) {
        this.mSettingsChangeCallbacks.remove(locationChangeCallback);
    }

    public boolean setLocationEnabled(boolean z) {
        int currentUser = KeyguardUpdateMonitor.getCurrentUser();
        if (isUserLocationRestricted(currentUser)) {
            return false;
        }
        return TilesHelper.updateLocationEnabled(this.mContext, z, currentUser);
    }

    public boolean isLocationEnabled() {
        return TilesHelper.isLocationEnabled(this.mContext, KeyguardUpdateMonitor.getCurrentUser());
    }

    public boolean isLocationActive() {
        return this.mAreActiveLocationRequests;
    }

    private boolean isUserLocationRestricted(int i) {
        return ((UserManager) this.mContext.getSystemService("user")).hasUserRestriction("no_share_location", UserHandleCompat.of(i));
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

    private void updateLocationStatus(Intent intent) {
        Message obtainMessage = this.mHandler.obtainMessage();
        obtainMessage.what = 3;
        obtainMessage.obj = intent;
        this.mHandler.sendMessage(obtainMessage);
    }

    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if ("android.location.HIGH_POWER_REQUEST_CHANGE".equals(action)) {
            updateActiveLocationRequests();
        } else if ("android.location.MODE_CHANGED".equals(action)) {
            this.mHandler.sendEmptyMessage(1);
        } else if ("android.location.GPS_ENABLED_CHANGE".equals(action) || "android.location.GPS_FIX_CHANGE".equals(action)) {
            updateLocationStatus(intent);
            updateGpsNotification(intent);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:32:0x00b1  */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x00b5  */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x00c2  */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x00c6  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void updateGpsNotification(android.content.Intent r15) {
        /*
            r14 = this;
            java.lang.String r0 = r15.getAction()
            java.lang.String r1 = "enabled"
            r2 = 0
            boolean r1 = r15.getBooleanExtra(r1, r2)
            java.lang.String r3 = "android.location.GPS_FIX_CHANGE"
            boolean r3 = r0.equals(r3)
            r4 = 1
            if (r3 == 0) goto L_0x001e
            if (r1 == 0) goto L_0x001e
            r0 = 285671442(0x11070012, float:1.0649644E-28)
            r1 = 2131821379(0x7f110343, float:1.92755E38)
            r3 = r2
            goto L_0x003c
        L_0x001e:
            java.lang.String r3 = "android.location.GPS_ENABLED_CHANGE"
            boolean r0 = r0.equals(r3)
            if (r0 == 0) goto L_0x002d
            if (r1 != 0) goto L_0x002d
            r0 = r2
            r1 = r0
            r3 = r1
            r4 = r3
            goto L_0x003c
        L_0x002d:
            boolean r0 = com.android.systemui.Constants.SUPPORT_DUAL_GPS
            if (r0 == 0) goto L_0x0035
            r0 = 2131233627(0x7f080b5b, float:1.8083397E38)
            goto L_0x0038
        L_0x0035:
            r0 = 2131233637(0x7f080b65, float:1.8083417E38)
        L_0x0038:
            r1 = 2131821380(0x7f110344, float:1.9275502E38)
            r3 = r4
        L_0x003c:
            r5 = 252119(0x3d8d7, float:3.53294E-40)
            r6 = 0
            if (r4 == 0) goto L_0x00f0
            java.lang.String r4 = "android.intent.extra.PACKAGES"
            java.lang.String r15 = r15.getStringExtra(r4)
            boolean r4 = android.text.TextUtils.isEmpty(r15)
            if (r4 == 0) goto L_0x004f
            return
        L_0x004f:
            android.content.Context r4 = r14.mContext
            android.content.pm.PackageManager r4 = r4.getPackageManager()
            android.content.pm.ApplicationInfo r7 = r4.getApplicationInfo(r15, r2)     // Catch:{ NameNotFoundException -> 0x0072 }
            java.lang.CharSequence r4 = r7.loadLabel(r4)     // Catch:{ NameNotFoundException -> 0x0072 }
            java.lang.Class<com.android.systemui.miui.AppIconsManager> r7 = com.android.systemui.miui.AppIconsManager.class
            java.lang.Object r7 = com.android.systemui.Dependency.get(r7)     // Catch:{ NameNotFoundException -> 0x0073 }
            com.android.systemui.miui.AppIconsManager r7 = (com.android.systemui.miui.AppIconsManager) r7     // Catch:{ NameNotFoundException -> 0x0073 }
            android.content.Context r8 = r14.mContext     // Catch:{ NameNotFoundException -> 0x0073 }
            android.graphics.Bitmap r7 = r7.getAppIconBitmap(r8, r15)     // Catch:{ NameNotFoundException -> 0x0073 }
            if (r7 == 0) goto L_0x0073
            android.graphics.drawable.Icon r7 = android.graphics.drawable.Icon.createWithBitmap(r7)     // Catch:{ NameNotFoundException -> 0x0073 }
            goto L_0x0074
        L_0x0072:
            r4 = r6
        L_0x0073:
            r7 = r6
        L_0x0074:
            android.content.Intent r10 = new android.content.Intent
            java.lang.String r8 = "package"
            android.net.Uri r8 = android.net.Uri.fromParts(r8, r15, r6)
            java.lang.String r9 = "android.settings.APPLICATION_DETAILS_SETTINGS"
            r10.<init>(r9, r8)
            r8 = 268435456(0x10000000, float:2.5243549E-29)
            r10.setFlags(r8)
            android.content.Context r8 = r14.mContext
            r9 = 0
            r11 = 0
            r12 = 0
            android.os.UserHandle r13 = android.os.UserHandle.CURRENT
            android.app.PendingIntent r8 = android.app.PendingIntent.getActivityAsUser(r8, r9, r10, r11, r12, r13)
            android.content.Context r9 = r14.mContext
            java.lang.String r10 = com.android.systemui.util.NotificationChannels.LOCATION
            android.app.Notification$Builder r9 = android.app.NotificationCompat.newBuilder(r9, r10)
            android.content.Context r10 = r14.mContext
            java.lang.CharSequence r1 = r10.getText(r1)
            android.app.Notification$Builder r1 = r9.setContentTitle(r1)
            android.app.Notification$Builder r1 = r1.setContentText(r4)
            android.app.Notification$Builder r1 = r1.setOngoing(r3)
            android.app.Notification$Builder r1 = r1.setContentIntent(r8)
            if (r7 != 0) goto L_0x00b5
            r1.setSmallIcon(r0)
            goto L_0x00b8
        L_0x00b5:
            r1.setSmallIcon(r7)
        L_0x00b8:
            android.app.Notification r0 = r1.build()
            boolean r3 = android.text.TextUtils.isEmpty(r15)
            if (r3 != 0) goto L_0x00c6
            com.android.systemui.statusbar.notification.MiuiNotificationCompat.setTargetPkg(r0, r15)
            goto L_0x00de
        L_0x00c6:
            android.content.Context r15 = r14.mContext
            android.content.res.Resources r15 = r15.getResources()
            boolean r3 = com.android.systemui.Constants.SUPPORT_DUAL_GPS
            if (r3 == 0) goto L_0x00d4
            r3 = 2131233282(0x7f080a02, float:1.8082697E38)
            goto L_0x00d7
        L_0x00d4:
            r3 = 2131233284(0x7f080a04, float:1.8082701E38)
        L_0x00d7:
            android.graphics.Bitmap r15 = android.graphics.BitmapFactory.decodeResource(r15, r3)
            r1.setLargeIcon(r15)
        L_0x00de:
            com.android.systemui.statusbar.notification.MiuiNotificationCompat.setEnableFloat(r0, r2)
            com.android.systemui.statusbar.notification.MiuiNotificationCompat.setEnableKeyguard(r0, r2)
            r0.tickerView = r6
            r0.tickerText = r6
            android.app.NotificationManager r14 = r14.mNotificationManager
            android.os.UserHandle r15 = android.os.UserHandle.CURRENT
            r14.notifyAsUser(r6, r5, r0, r15)
            goto L_0x00f7
        L_0x00f0:
            android.app.NotificationManager r14 = r14.mNotificationManager
            android.os.UserHandle r15 = android.os.UserHandle.CURRENT
            r14.cancelAsUser(r6, r5, r15)
        L_0x00f7:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.policy.LocationControllerImpl.updateGpsNotification(android.content.Intent):void");
    }

    private final class H extends Handler {
        private H() {
        }

        public void handleMessage(Message message) {
            int i = message.what;
            if (i == 1) {
                locationSettingsChanged();
            } else if (i == 2) {
                locationActiveChanged();
            } else if (i == 3) {
                locationStatusChanged((Intent) message.obj);
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$locationActiveChanged$0 */
        public /* synthetic */ void lambda$locationActiveChanged$0$LocationControllerImpl$H(LocationController.LocationChangeCallback locationChangeCallback) {
            locationChangeCallback.onLocationActiveChanged(LocationControllerImpl.this.mAreActiveLocationRequests);
        }

        private void locationActiveChanged() {
            Utils.safeForeach(LocationControllerImpl.this.mSettingsChangeCallbacks, new Consumer() {
                public final void accept(Object obj) {
                    LocationControllerImpl.H.this.lambda$locationActiveChanged$0$LocationControllerImpl$H((LocationController.LocationChangeCallback) obj);
                }
            });
        }

        private void locationSettingsChanged() {
            Utils.safeForeach(LocationControllerImpl.this.mSettingsChangeCallbacks, new Consumer(LocationControllerImpl.this.isLocationEnabled()) {
                public final /* synthetic */ boolean f$0;

                {
                    this.f$0 = r1;
                }

                public final void accept(Object obj) {
                    ((LocationController.LocationChangeCallback) obj).onLocationSettingsChanged(this.f$0);
                }
            });
        }

        private void locationStatusChanged(Intent intent) {
            Utils.safeForeach(LocationControllerImpl.this.mSettingsChangeCallbacks, new Consumer(intent) {
                public final /* synthetic */ Intent f$0;

                {
                    this.f$0 = r1;
                }

                public final void accept(Object obj) {
                    ((LocationController.LocationChangeCallback) obj).onLocationStatusChanged(this.f$0);
                }
            });
        }
    }
}
