package com.android.systemui.miui.statusbar.phone.rank;

import com.android.systemui.SystemUICompat;
import com.android.systemui.miui.statusbar.ExpandedNotification;
import com.android.systemui.statusbar.NotificationData;
import com.android.systemui.statusbar.policy.HeadsUpManager;
import java.util.HashMap;

public class RankUtil {
    private static HashMap<String, Boolean> sHighPriorityMap = new HashMap<>();
    private static long sLastSortTime = System.currentTimeMillis();

    public static int compareHeadsUp(NotificationData.Entry entry, NotificationData.Entry entry2, HeadsUpManager headsUpManager) {
        boolean isHeadsUp = entry.row.isHeadsUp();
        if (isHeadsUp != entry2.row.isHeadsUp()) {
            return isHeadsUp ? -1 : 1;
        }
        if (isHeadsUp) {
            return headsUpManager.compare(entry, entry2);
        }
        return 0;
    }

    public static int compareSystemWarnings(NotificationData.Entry entry, NotificationData.Entry entry2) {
        boolean isSystemWarnings = entry.notification.isSystemWarnings();
        if (isSystemWarnings != entry2.notification.isSystemWarnings()) {
            return isSystemWarnings ? -1 : 1;
        }
        return 0;
    }

    public static int compareShowingAtTail(NotificationData.Entry entry, NotificationData.Entry entry2) {
        boolean isShowingAtTail = entry.notification.isShowingAtTail();
        boolean isShowingAtTail2 = entry2.notification.isShowingAtTail();
        if (isShowingAtTail != isShowingAtTail2) {
            return isShowingAtTail2 ? -1 : 1;
        }
        return 0;
    }

    public static void updateHighPriorityMap(String str, int i) {
        try {
            sHighPriorityMap.put(str, Boolean.valueOf(SystemUICompat.isHighPriority(str, i)));
        } catch (Exception unused) {
            sHighPriorityMap.remove(str);
        }
    }

    public static int compareMedia(NotificationData.Entry entry, NotificationData.Entry entry2, int i, int i2, String str) {
        boolean z = entry.key.equals(str) && i > 1;
        if (z == (entry2.key.equals(str) && i2 > 1)) {
            return 0;
        }
        if (z) {
            return -1;
        }
        return 1;
    }

    public static int comparePrioritizedMax(NotificationData.Entry entry, NotificationData.Entry entry2, int i, int i2) {
        boolean isPrioritizedMaxImportanceNotification = isPrioritizedMaxImportanceNotification(entry, i);
        if (isPrioritizedMaxImportanceNotification != isPrioritizedMaxImportanceNotification(entry2, i2)) {
            return isPrioritizedMaxImportanceNotification ? -1 : 1;
        }
        return 0;
    }

    private static boolean isPrioritizedMaxImportanceNotification(NotificationData.Entry entry, int i) {
        if (!entry.notification.isPrioritizedApp() || (i < 4 && entry.notification.getNotification().priority < 1)) {
            return false;
        }
        return true;
    }

    public static int compareWhen(ExpandedNotification expandedNotification, ExpandedNotification expandedNotification2) {
        return Long.compare(getPostTime(expandedNotification2), getPostTime(expandedNotification));
    }

    private static long getPostTime(ExpandedNotification expandedNotification) {
        long j = expandedNotification.getNotification().when;
        if (j == 0 || j > expandedNotification.getPostTime()) {
            return expandedNotification.getPostTime();
        }
        return j;
    }
}
