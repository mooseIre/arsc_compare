package com.android.systemui;

import android.content.Context;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.SparseArray;
import java.util.Arrays;

public class ForegroundServiceControllerImpl implements ForegroundServiceController {
    private final Object mMutex = new Object();
    private final SparseArray<UserServices> mUserServices = new SparseArray<>();

    public ForegroundServiceControllerImpl(Context context) {
    }

    public boolean isDungeonNeededForUser(int i) {
        synchronized (this.mMutex) {
            UserServices userServices = this.mUserServices.get(i);
            if (userServices == null) {
                return false;
            }
            boolean isDungeonNeeded = userServices.isDungeonNeeded();
            return isDungeonNeeded;
        }
    }

    public void addNotification(StatusBarNotification statusBarNotification, int i) {
        updateNotification(statusBarNotification, i);
    }

    public boolean removeNotification(StatusBarNotification statusBarNotification) {
        synchronized (this.mMutex) {
            UserServices userServices = this.mUserServices.get(statusBarNotification.getUserId());
            if (userServices == null) {
                return false;
            }
            if (isDungeonNotification(statusBarNotification)) {
                userServices.setRunningServices((String[]) null);
                return true;
            }
            boolean removeNotification = userServices.removeNotification(statusBarNotification.getPackageName(), statusBarNotification.getKey());
            return removeNotification;
        }
    }

    public void updateNotification(StatusBarNotification statusBarNotification, int i) {
        synchronized (this.mMutex) {
            UserServices userServices = this.mUserServices.get(statusBarNotification.getUserId());
            if (userServices == null) {
                userServices = new UserServices();
                this.mUserServices.put(statusBarNotification.getUserId(), userServices);
            }
            if (isDungeonNotification(statusBarNotification)) {
                Bundle bundle = statusBarNotification.getNotification().extras;
                if (bundle != null) {
                    userServices.setRunningServices(bundle.getStringArray("android.foregroundApps"));
                }
            } else {
                userServices.removeNotification(statusBarNotification.getPackageName(), statusBarNotification.getKey());
                if ((statusBarNotification.getNotification().flags & 64) != 0 && i > 1) {
                    userServices.addNotification(statusBarNotification.getPackageName(), statusBarNotification.getKey());
                }
            }
        }
    }

    public boolean isDungeonNotification(StatusBarNotification statusBarNotification) {
        return statusBarNotification.getId() == 40 && statusBarNotification.getTag() == null && statusBarNotification.getPackageName().equals("android");
    }

    private static class UserServices {
        private ArrayMap<String, ArraySet<String>> mNotifications;
        private String[] mRunning;

        private UserServices() {
            this.mRunning = null;
            this.mNotifications = new ArrayMap<>(1);
        }

        public void setRunningServices(String[] strArr) {
            this.mRunning = strArr != null ? (String[]) Arrays.copyOf(strArr, strArr.length) : null;
        }

        public void addNotification(String str, String str2) {
            if (this.mNotifications.get(str) == null) {
                this.mNotifications.put(str, new ArraySet());
            }
            this.mNotifications.get(str).add(str2);
        }

        public boolean removeNotification(String str, String str2) {
            ArraySet arraySet = this.mNotifications.get(str);
            if (arraySet == null) {
                return false;
            }
            boolean remove = arraySet.remove(str2);
            if (arraySet.size() == 0) {
                this.mNotifications.remove(str);
            }
            return remove;
        }

        public boolean isDungeonNeeded() {
            String[] strArr = this.mRunning;
            if (strArr != null) {
                for (String str : strArr) {
                    ArraySet arraySet = this.mNotifications.get(str);
                    if (arraySet == null || arraySet.size() == 0) {
                        return true;
                    }
                }
            }
            return false;
        }
    }
}
