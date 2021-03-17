package com.android.systemui.statusbar.notification.collection.coordinator;

import android.os.UserHandle;
import android.util.ArraySet;
import com.android.systemui.ForegroundServiceController;
import com.android.systemui.appops.AppOpsController;
import com.android.systemui.statusbar.notification.ExpandedNotification;
import com.android.systemui.statusbar.notification.collection.NotifPipeline;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.collection.coordinator.AppOpsCoordinator;
import com.android.systemui.statusbar.notification.collection.listbuilder.pluggable.NotifFilter;
import com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener;
import com.android.systemui.statusbar.notification.collection.notifcollection.NotifLifetimeExtender;
import com.android.systemui.util.Assert;
import com.android.systemui.util.concurrency.DelayableExecutor;
import java.util.HashMap;
import java.util.Map;

public class AppOpsCoordinator implements Coordinator {
    private final AppOpsController mAppOpsController;
    private final NotifLifetimeExtender mForegroundLifetimeExtender = new NotifLifetimeExtender() {
        /* class com.android.systemui.statusbar.notification.collection.coordinator.AppOpsCoordinator.AnonymousClass2 */
        private NotifLifetimeExtender.OnEndLifetimeExtensionCallback mEndCallback;
        private Map<NotificationEntry, Runnable> mEndRunnables = new HashMap();

        @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifLifetimeExtender
        public String getName() {
            return "AppOpsCoordinator";
        }

        @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifLifetimeExtender
        public void setCallback(NotifLifetimeExtender.OnEndLifetimeExtensionCallback onEndLifetimeExtensionCallback) {
            this.mEndCallback = onEndLifetimeExtensionCallback;
        }

        @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifLifetimeExtender
        public boolean shouldExtendLifetime(NotificationEntry notificationEntry, int i) {
            boolean z = false;
            if ((notificationEntry.getSbn().getNotification().flags & 64) == 0) {
                return false;
            }
            long currentTimeMillis = System.currentTimeMillis();
            if (currentTimeMillis - notificationEntry.getSbn().getPostTime() < 5000) {
                z = true;
            }
            if (z && !this.mEndRunnables.containsKey(notificationEntry)) {
                this.mEndRunnables.put(notificationEntry, AppOpsCoordinator.this.mMainExecutor.executeDelayed(new Runnable(notificationEntry) {
                    /* class com.android.systemui.statusbar.notification.collection.coordinator.$$Lambda$AppOpsCoordinator$2$QL7e9QZrONYPm8OKjbOLeImENQ4 */
                    public final /* synthetic */ NotificationEntry f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void run() {
                        AppOpsCoordinator.AnonymousClass2.this.lambda$shouldExtendLifetime$0$AppOpsCoordinator$2(this.f$1);
                    }
                }, 5000 - (currentTimeMillis - notificationEntry.getSbn().getPostTime())));
            }
            return z;
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$shouldExtendLifetime$0 */
        public /* synthetic */ void lambda$shouldExtendLifetime$0$AppOpsCoordinator$2(NotificationEntry notificationEntry) {
            this.mEndRunnables.remove(notificationEntry);
            this.mEndCallback.onEndLifetimeExtension(AppOpsCoordinator.this.mForegroundLifetimeExtender, notificationEntry);
        }

        @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifLifetimeExtender
        public void cancelLifetimeExtension(NotificationEntry notificationEntry) {
            Runnable remove = this.mEndRunnables.remove(notificationEntry);
            if (remove != null) {
                remove.run();
            }
        }
    };
    private final ForegroundServiceController mForegroundServiceController;
    private final DelayableExecutor mMainExecutor;
    private NotifCollectionListener mNotifCollectionListener = new NotifCollectionListener() {
        /* class com.android.systemui.statusbar.notification.collection.coordinator.AppOpsCoordinator.AnonymousClass3 */

        @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener
        public void onEntryAdded(NotificationEntry notificationEntry) {
            tagAppOps(notificationEntry);
        }

        @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener
        public void onEntryUpdated(NotificationEntry notificationEntry) {
            tagAppOps(notificationEntry);
        }

        private void tagAppOps(NotificationEntry notificationEntry) {
            ExpandedNotification sbn = notificationEntry.getSbn();
            ArraySet<Integer> appOps = AppOpsCoordinator.this.mForegroundServiceController.getAppOps(sbn.getUser().getIdentifier(), sbn.getPackageName());
            notificationEntry.mActiveAppOps.clear();
            if (appOps != null) {
                notificationEntry.mActiveAppOps.addAll((ArraySet<? extends Integer>) appOps);
            }
        }
    };
    private final NotifFilter mNotifFilter = new NotifFilter("AppOpsCoordinator") {
        /* class com.android.systemui.statusbar.notification.collection.coordinator.AppOpsCoordinator.AnonymousClass1 */

        @Override // com.android.systemui.statusbar.notification.collection.listbuilder.pluggable.NotifFilter
        public boolean shouldFilterOut(NotificationEntry notificationEntry, long j) {
            String[] stringArray;
            ExpandedNotification sbn = notificationEntry.getSbn();
            if (AppOpsCoordinator.this.mForegroundServiceController.isDisclosureNotification(sbn) && !AppOpsCoordinator.this.mForegroundServiceController.isDisclosureNeededForUser(sbn.getUser().getIdentifier())) {
                return true;
            }
            if (!AppOpsCoordinator.this.mForegroundServiceController.isSystemAlertNotification(sbn) || (stringArray = sbn.getNotification().extras.getStringArray("android.foregroundApps")) == null || stringArray.length < 1 || AppOpsCoordinator.this.mForegroundServiceController.isSystemAlertWarningNeeded(sbn.getUser().getIdentifier(), stringArray[0])) {
                return false;
            }
            return true;
        }
    };
    private NotifPipeline mNotifPipeline;

    public AppOpsCoordinator(ForegroundServiceController foregroundServiceController, AppOpsController appOpsController, DelayableExecutor delayableExecutor) {
        this.mForegroundServiceController = foregroundServiceController;
        this.mAppOpsController = appOpsController;
        this.mMainExecutor = delayableExecutor;
    }

    @Override // com.android.systemui.statusbar.notification.collection.coordinator.Coordinator
    public void attach(NotifPipeline notifPipeline) {
        this.mNotifPipeline = notifPipeline;
        notifPipeline.addNotificationLifetimeExtender(this.mForegroundLifetimeExtender);
        this.mNotifPipeline.addCollectionListener(this.mNotifCollectionListener);
        this.mNotifPipeline.addPreGroupFilter(this.mNotifFilter);
        this.mAppOpsController.addCallback(ForegroundServiceController.APP_OPS, new AppOpsController.Callback() {
            /* class com.android.systemui.statusbar.notification.collection.coordinator.$$Lambda$AppOpsCoordinator$g1CviyimAy6Lv0tcSLFbIg1yos */

            @Override // com.android.systemui.appops.AppOpsController.Callback
            public final void onActiveStateChanged(int i, int i2, String str, boolean z) {
                AppOpsCoordinator.this.onAppOpsChanged(i, i2, str, z);
            }
        });
    }

    /* access modifiers changed from: private */
    public void onAppOpsChanged(int i, int i2, String str, boolean z) {
        this.mMainExecutor.execute(new Runnable(i, i2, str, z) {
            /* class com.android.systemui.statusbar.notification.collection.coordinator.$$Lambda$AppOpsCoordinator$CPW_3xjRRxO_V4fyqQUDt5CLJDM */
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
                AppOpsCoordinator.this.lambda$onAppOpsChanged$0$AppOpsCoordinator(this.f$1, this.f$2, this.f$3, this.f$4);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: handleAppOpsChanged */
    public void lambda$onAppOpsChanged$0(int i, int i2, String str, boolean z) {
        boolean z2;
        Assert.isMainThread();
        ArraySet<String> standardLayoutKeys = this.mForegroundServiceController.getStandardLayoutKeys(UserHandle.getUserId(i2), str);
        if (standardLayoutKeys != null) {
            boolean z3 = false;
            for (int i3 = 0; i3 < standardLayoutKeys.size(); i3++) {
                NotificationEntry findNotificationEntryWithKey = findNotificationEntryWithKey(standardLayoutKeys.valueAt(i3));
                if (findNotificationEntryWithKey != null && i2 == findNotificationEntryWithKey.getSbn().getUid() && str.equals(findNotificationEntryWithKey.getSbn().getPackageName())) {
                    if (z) {
                        z2 = findNotificationEntryWithKey.mActiveAppOps.add(Integer.valueOf(i));
                    } else {
                        z2 = findNotificationEntryWithKey.mActiveAppOps.remove(Integer.valueOf(i));
                    }
                    z3 |= z2;
                }
            }
            if (z3) {
                this.mNotifFilter.invalidateList();
            }
        }
    }

    private NotificationEntry findNotificationEntryWithKey(String str) {
        for (NotificationEntry notificationEntry : this.mNotifPipeline.getAllNotifs()) {
            if (notificationEntry.getKey().equals(str)) {
                return notificationEntry;
            }
        }
        return null;
    }
}
