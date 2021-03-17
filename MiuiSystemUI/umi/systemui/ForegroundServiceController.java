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
            /* class com.android.systemui.$$Lambda$ForegroundServiceController$6VuUZsronrAWhzH49_rmYg_sL9o */

            @Override // com.android.systemui.appops.AppOpsController.Callback
            public final void onActiveStateChanged(int i, int i2, String str, boolean z) {
                ForegroundServiceController.this.lambda$new$1$ForegroundServiceController(i, i2, str, z);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$1 */
    public /* synthetic */ void lambda$new$1$ForegroundServiceController(int i, int i2, String str, boolean z) {
        this.mMainHandler.post(new Runnable(i, i2, str, z) {
            /* class com.android.systemui.$$Lambda$ForegroundServiceController$wst_lR_MjTzsfZPULZ8dMJBIEr4 */
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
            return foregroundServicesUserState.isDisclosureNeeded();
        }
    }

    public boolean isSystemAlertWarningNeeded(int i, String str) {
        synchronized (this.mMutex) {
            ForegroundServicesUserState foregroundServicesUserState = this.mUserServices.get(i);
            boolean z = false;
            if (foregroundServicesUserState == null) {
                return false;
            }
            if (foregroundServicesUserState.getStandardLayoutKeys(str) == null) {
                z = true;
            }
            return z;
        }
    }

    public ArraySet<String> getStandardLayoutKeys(int i, String str) {
        synchronized (this.mMutex) {
            ForegroundServicesUserState foregroundServicesUserState = this.mUserServices.get(i);
            if (foregroundServicesUserState == null) {
                return null;
            }
            return foregroundServicesUserState.getStandardLayoutKeys(str);
        }
    }

    public ArraySet<Integer> getAppOps(int i, String str) {
        synchronized (this.mMutex) {
            ForegroundServicesUserState foregroundServicesUserState = this.mUserServices.get(i);
            if (foregroundServicesUserState == null) {
                return null;
            }
            return foregroundServicesUserState.getFeatures(str);
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
            for (String str2 : standardLayoutKeys) {
                NotificationEntry pendingOrActiveNotif = this.mEntryManager.getPendingOrActiveNotif(str2);
                if (pendingOrActiveNotif != null && i2 == pendingOrActiveNotif.getSbn().getUid() && str.equals(pendingOrActiveNotif.getSbn().getPackageName())) {
                    synchronized (pendingOrActiveNotif.mActiveAppOps) {
                        if (z) {
                            remove = pendingOrActiveNotif.mActiveAppOps.add(Integer.valueOf(i));
                        } else {
                            remove = pendingOrActiveNotif.mActiveAppOps.remove(Integer.valueOf(i));
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
            return userStateUpdateCallback.updateUserState(foregroundServicesUserState);
        }
    }

    public boolean isDisclosureNotification(StatusBarNotification statusBarNotification) {
        return statusBarNotification.getId() == 40 && statusBarNotification.getTag() == null && statusBarNotification.getPackageName().equals("android");
    }

    public boolean isSystemAlertNotification(StatusBarNotification statusBarNotification) {
        return statusBarNotification.getPackageName().equals("android") && statusBarNotification.getTag() != null && statusBarNotification.getTag().contains("AlertWindowNotification");
    }
}
