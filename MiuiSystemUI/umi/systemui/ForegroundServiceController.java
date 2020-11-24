package com.android.systemui;

import android.os.Handler;
import android.os.UserHandle;
import android.service.notification.StatusBarNotification;
import android.util.ArraySet;
import android.util.SparseArray;
import com.android.systemui.appops.AppOpsController;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.util.Assert;

public class ForegroundServiceController {
    public static final int[] APP_OPS = {26, 24, 27, 0, 1};
    private final NotificationEntryManager mEntryManager;
    private final Handler mMainHandler;
    private final Object mMutex = new Object();
    private final SparseArray<ForegroundServicesUserState> mUserServices = new SparseArray<>();

    interface UserStateUpdateCallback {
        boolean updateUserState(ForegroundServicesUserState foregroundServicesUserState);
    }

    public ForegroundServiceController(NotificationEntryManager notificationEntryManager, AppOpsController appOpsController, Handler handler) {
        this.mEntryManager = notificationEntryManager;
        this.mMainHandler = handler;
        appOpsController.addCallback(APP_OPS, new AppOpsController.Callback() {
            public final void onActiveStateChanged(int i, int i2, String str, boolean z) {
                ForegroundServiceController.this.lambda$new$1$ForegroundServiceController(i, i2, str, z);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$1 */
    public /* synthetic */ void lambda$new$1$ForegroundServiceController(int i, int i2, String str, boolean z) {
        this.mMainHandler.post(new Runnable(i, i2, str, z) {
            public final /* synthetic */ int f$1;
            public final /* synthetic */ int f$2;
            public final /* synthetic */ String f$3;
            public final /* synthetic */ boolean f$4;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
            }

            public final void run() {
                ForegroundServiceController.this.lambda$new$0$ForegroundServiceController(this.f$1, this.f$2, this.f$3, this.f$4);
            }
        });
    }

    public boolean isDisclosureNeededForUser(int i) {
        synchronized (this.mMutex) {
            ForegroundServicesUserState foregroundServicesUserState = this.mUserServices.get(i);
            if (foregroundServicesUserState == null) {
                return false;
            }
            boolean isDisclosureNeeded = foregroundServicesUserState.isDisclosureNeeded();
            return isDisclosureNeeded;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0018, code lost:
        return r2;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isSystemAlertWarningNeeded(int r2, java.lang.String r3) {
        /*
            r1 = this;
            java.lang.Object r0 = r1.mMutex
            monitor-enter(r0)
            android.util.SparseArray<com.android.systemui.ForegroundServicesUserState> r1 = r1.mUserServices     // Catch:{ all -> 0x0019 }
            java.lang.Object r1 = r1.get(r2)     // Catch:{ all -> 0x0019 }
            com.android.systemui.ForegroundServicesUserState r1 = (com.android.systemui.ForegroundServicesUserState) r1     // Catch:{ all -> 0x0019 }
            r2 = 0
            if (r1 != 0) goto L_0x0010
            monitor-exit(r0)     // Catch:{ all -> 0x0019 }
            return r2
        L_0x0010:
            android.util.ArraySet r1 = r1.getStandardLayoutKeys(r3)     // Catch:{ all -> 0x0019 }
            if (r1 != 0) goto L_0x0017
            r2 = 1
        L_0x0017:
            monitor-exit(r0)     // Catch:{ all -> 0x0019 }
            return r2
        L_0x0019:
            r1 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x0019 }
            throw r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.ForegroundServiceController.isSystemAlertWarningNeeded(int, java.lang.String):boolean");
    }

    public ArraySet<String> getStandardLayoutKeys(int i, String str) {
        synchronized (this.mMutex) {
            ForegroundServicesUserState foregroundServicesUserState = this.mUserServices.get(i);
            if (foregroundServicesUserState == null) {
                return null;
            }
            ArraySet<String> standardLayoutKeys = foregroundServicesUserState.getStandardLayoutKeys(str);
            return standardLayoutKeys;
        }
    }

    public ArraySet<Integer> getAppOps(int i, String str) {
        synchronized (this.mMutex) {
            ForegroundServicesUserState foregroundServicesUserState = this.mUserServices.get(i);
            if (foregroundServicesUserState == null) {
                return null;
            }
            ArraySet<Integer> features = foregroundServicesUserState.getFeatures(str);
            return features;
        }
    }

    /* access modifiers changed from: package-private */
    /* renamed from: onAppOpChanged */
    public void lambda$new$0(int i, int i2, String str, boolean z) {
        boolean remove;
        Assert.isMainThread();
        int userId = UserHandle.getUserId(i2);
        synchronized (this.mMutex) {
            ForegroundServicesUserState foregroundServicesUserState = this.mUserServices.get(userId);
            if (foregroundServicesUserState == null) {
                foregroundServicesUserState = new ForegroundServicesUserState();
                this.mUserServices.put(userId, foregroundServicesUserState);
            }
            if (z) {
                foregroundServicesUserState.addOp(str, i);
            } else {
                foregroundServicesUserState.removeOp(str, i);
            }
        }
        ArraySet<String> standardLayoutKeys = getStandardLayoutKeys(userId, str);
        if (standardLayoutKeys != null) {
            boolean z2 = false;
            for (String pendingOrActiveNotif : standardLayoutKeys) {
                NotificationEntry pendingOrActiveNotif2 = this.mEntryManager.getPendingOrActiveNotif(pendingOrActiveNotif);
                if (pendingOrActiveNotif2 != null && i2 == pendingOrActiveNotif2.getSbn().getUid() && str.equals(pendingOrActiveNotif2.getSbn().getPackageName())) {
                    synchronized (pendingOrActiveNotif2.mActiveAppOps) {
                        if (z) {
                            remove = pendingOrActiveNotif2.mActiveAppOps.add(Integer.valueOf(i));
                        } else {
                            remove = pendingOrActiveNotif2.mActiveAppOps.remove(Integer.valueOf(i));
                        }
                        z2 |= remove;
                    }
                }
            }
            if (z2) {
                NotificationEntryManager notificationEntryManager = this.mEntryManager;
                notificationEntryManager.updateNotifications("appOpChanged pkg=" + str);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean updateUserState(int i, UserStateUpdateCallback userStateUpdateCallback, boolean z) {
        synchronized (this.mMutex) {
            ForegroundServicesUserState foregroundServicesUserState = this.mUserServices.get(i);
            if (foregroundServicesUserState == null) {
                if (!z) {
                    return false;
                }
                foregroundServicesUserState = new ForegroundServicesUserState();
                this.mUserServices.put(i, foregroundServicesUserState);
            }
            boolean updateUserState = userStateUpdateCallback.updateUserState(foregroundServicesUserState);
            return updateUserState;
        }
    }

    public boolean isDisclosureNotification(StatusBarNotification statusBarNotification) {
        return statusBarNotification.getId() == 40 && statusBarNotification.getTag() == null && statusBarNotification.getPackageName().equals("android");
    }

    public boolean isSystemAlertNotification(StatusBarNotification statusBarNotification) {
        return statusBarNotification.getPackageName().equals("android") && statusBarNotification.getTag() != null && statusBarNotification.getTag().contains("AlertWindowNotification");
    }
}
