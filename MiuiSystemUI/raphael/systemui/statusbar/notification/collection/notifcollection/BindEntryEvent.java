package com.android.systemui.statusbar.notification.collection.notifcollection;

import android.service.notification.StatusBarNotification;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: NotifEvent.kt */
public final class BindEntryEvent extends NotifEvent {
    @NotNull
    private final NotificationEntry entry;
    @NotNull
    private final StatusBarNotification sbn;

    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof BindEntryEvent)) {
            return false;
        }
        BindEntryEvent bindEntryEvent = (BindEntryEvent) obj;
        return Intrinsics.areEqual(this.entry, bindEntryEvent.entry) && Intrinsics.areEqual(this.sbn, bindEntryEvent.sbn);
    }

    public int hashCode() {
        NotificationEntry notificationEntry = this.entry;
        int i = 0;
        int hashCode = (notificationEntry != null ? notificationEntry.hashCode() : 0) * 31;
        StatusBarNotification statusBarNotification = this.sbn;
        if (statusBarNotification != null) {
            i = statusBarNotification.hashCode();
        }
        return hashCode + i;
    }

    @NotNull
    public String toString() {
        return "BindEntryEvent(entry=" + this.entry + ", sbn=" + this.sbn + ")";
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public BindEntryEvent(@NotNull NotificationEntry notificationEntry, @NotNull StatusBarNotification statusBarNotification) {
        super(null);
        Intrinsics.checkParameterIsNotNull(notificationEntry, "entry");
        Intrinsics.checkParameterIsNotNull(statusBarNotification, "sbn");
        this.entry = notificationEntry;
        this.sbn = statusBarNotification;
    }

    @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifEvent
    public void dispatchToListener(@NotNull NotifCollectionListener notifCollectionListener) {
        Intrinsics.checkParameterIsNotNull(notifCollectionListener, "listener");
        notifCollectionListener.onEntryBind(this.entry, this.sbn);
    }
}
