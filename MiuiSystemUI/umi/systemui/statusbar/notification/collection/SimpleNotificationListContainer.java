package com.android.systemui.statusbar.notification.collection;

import android.view.View;
import android.view.ViewGroup;
import com.android.systemui.statusbar.notification.stack.NotificationListItem;
import org.jetbrains.annotations.NotNull;

/* compiled from: SimpleNotificationListContainer.kt */
public interface SimpleNotificationListContainer {
    void addListItem(@NotNull NotificationListItem notificationListItem);

    void generateChildOrderChangedEvent();

    @NotNull
    View getContainerChildAt(int i);

    int getContainerChildCount();

    void notifyGroupChildAdded(@NotNull View view);

    void notifyGroupChildRemoved(@NotNull View view, @NotNull ViewGroup viewGroup);

    void removeListItem(@NotNull NotificationListItem notificationListItem);

    void setChildTransferInProgress(boolean z);
}
