package com.android.systemui.statusbar.notification.collection.coordinator;

import android.content.pm.UserInfo;
import android.util.SparseArray;
import com.android.systemui.statusbar.NotificationLockscreenUserManager;
import com.android.systemui.statusbar.notification.collection.NotifPipeline;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.collection.listbuilder.pluggable.NotifFilter;

public class HideNotifsForOtherUsersCoordinator implements Coordinator {
    /* access modifiers changed from: private */
    public final NotifFilter mFilter = new NotifFilter("NotCurrentUserFilter") {
        public boolean shouldFilterOut(NotificationEntry notificationEntry, long j) {
            return !HideNotifsForOtherUsersCoordinator.this.mLockscreenUserManager.isCurrentProfile(notificationEntry.getSbn().getUser().getIdentifier());
        }
    };
    /* access modifiers changed from: private */
    public final NotificationLockscreenUserManager mLockscreenUserManager;
    /* access modifiers changed from: private */
    public final SharedCoordinatorLogger mLogger;
    private final NotificationLockscreenUserManager.UserChangedListener mUserChangedListener = new NotificationLockscreenUserManager.UserChangedListener() {
        public void onCurrentProfilesChanged(SparseArray<UserInfo> sparseArray) {
            HideNotifsForOtherUsersCoordinator.this.mLogger.logUserOrProfileChanged(HideNotifsForOtherUsersCoordinator.this.mLockscreenUserManager.getCurrentUserId(), HideNotifsForOtherUsersCoordinator.this.profileIdsToStr(sparseArray));
            HideNotifsForOtherUsersCoordinator.this.mFilter.invalidateList();
        }
    };

    public HideNotifsForOtherUsersCoordinator(NotificationLockscreenUserManager notificationLockscreenUserManager, SharedCoordinatorLogger sharedCoordinatorLogger) {
        this.mLockscreenUserManager = notificationLockscreenUserManager;
        this.mLogger = sharedCoordinatorLogger;
    }

    public void attach(NotifPipeline notifPipeline) {
        notifPipeline.addPreGroupFilter(this.mFilter);
        this.mLockscreenUserManager.addUserChangedListener(this.mUserChangedListener);
    }

    /* access modifiers changed from: private */
    public String profileIdsToStr(SparseArray<UserInfo> sparseArray) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (int i = 0; i < sparseArray.size(); i++) {
            sb.append(sparseArray.keyAt(i));
            if (i < sparseArray.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("}");
        return sb.toString();
    }
}
