package com.android.systemui.statusbar.notification.collection.notifcollection;

import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: NotifEvent.kt */
public final class InitEntryEvent extends NotifEvent {
    @NotNull
    private final NotificationEntry entry;

    public boolean equals(@Nullable Object obj) {
        if (this != obj) {
            return (obj instanceof InitEntryEvent) && Intrinsics.areEqual(this.entry, ((InitEntryEvent) obj).entry);
        }
        return true;
    }

    public int hashCode() {
        NotificationEntry notificationEntry = this.entry;
        if (notificationEntry != null) {
            return notificationEntry.hashCode();
        }
        return 0;
    }

    @NotNull
    public String toString() {
        return "InitEntryEvent(entry=" + this.entry + ")";
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public InitEntryEvent(@NotNull NotificationEntry notificationEntry) {
        super(null);
        Intrinsics.checkParameterIsNotNull(notificationEntry, "entry");
        this.entry = notificationEntry;
    }

    @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifEvent
    public void dispatchToListener(@NotNull NotifCollectionListener notifCollectionListener) {
        Intrinsics.checkParameterIsNotNull(notifCollectionListener, "listener");
        notifCollectionListener.onEntryInit(this.entry);
    }
}
