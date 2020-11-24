package com.android.systemui.statusbar.notification.row;

import androidx.collection.ArraySet;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class NotifInflationErrorManager {
    Set<NotificationEntry> mErroredNotifs = new ArraySet();
    List<NotifInflationErrorListener> mListeners = new ArrayList();

    public interface NotifInflationErrorListener {
        void onNotifInflationError(NotificationEntry notificationEntry, Exception exc);

        void onNotifInflationErrorCleared(NotificationEntry notificationEntry) {
        }
    }

    public void setInflationError(NotificationEntry notificationEntry, Exception exc) {
        this.mErroredNotifs.add(notificationEntry);
        for (int i = 0; i < this.mListeners.size(); i++) {
            this.mListeners.get(i).onNotifInflationError(notificationEntry, exc);
        }
    }

    public void clearInflationError(NotificationEntry notificationEntry) {
        if (this.mErroredNotifs.contains(notificationEntry)) {
            this.mErroredNotifs.remove(notificationEntry);
            for (int i = 0; i < this.mListeners.size(); i++) {
                this.mListeners.get(i).onNotifInflationErrorCleared(notificationEntry);
            }
        }
    }

    public void addInflationErrorListener(NotifInflationErrorListener notifInflationErrorListener) {
        this.mListeners.add(notifInflationErrorListener);
    }
}
