package com.android.systemui.statusbar.notification.collection.coalescer;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: CoalescedEvent.kt */
public final class CoalescedEvent {
    @Nullable
    private EventBatch batch;
    @NotNull
    private final String key;
    private int position;
    @NotNull
    private NotificationListenerService.Ranking ranking;
    @NotNull
    private StatusBarNotification sbn;

    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof CoalescedEvent)) {
            return false;
        }
        CoalescedEvent coalescedEvent = (CoalescedEvent) obj;
        return Intrinsics.areEqual(this.key, coalescedEvent.key) && this.position == coalescedEvent.position && Intrinsics.areEqual(this.sbn, coalescedEvent.sbn) && Intrinsics.areEqual(this.ranking, coalescedEvent.ranking) && Intrinsics.areEqual(this.batch, coalescedEvent.batch);
    }

    public int hashCode() {
        String str = this.key;
        int i = 0;
        int hashCode = (((str != null ? str.hashCode() : 0) * 31) + Integer.hashCode(this.position)) * 31;
        StatusBarNotification statusBarNotification = this.sbn;
        int hashCode2 = (hashCode + (statusBarNotification != null ? statusBarNotification.hashCode() : 0)) * 31;
        NotificationListenerService.Ranking ranking2 = this.ranking;
        int hashCode3 = (hashCode2 + (ranking2 != null ? ranking2.hashCode() : 0)) * 31;
        EventBatch eventBatch = this.batch;
        if (eventBatch != null) {
            i = eventBatch.hashCode();
        }
        return hashCode3 + i;
    }

    public CoalescedEvent(@NotNull String str, int i, @NotNull StatusBarNotification statusBarNotification, @NotNull NotificationListenerService.Ranking ranking2, @Nullable EventBatch eventBatch) {
        Intrinsics.checkParameterIsNotNull(str, "key");
        Intrinsics.checkParameterIsNotNull(statusBarNotification, "sbn");
        Intrinsics.checkParameterIsNotNull(ranking2, "ranking");
        this.key = str;
        this.position = i;
        this.sbn = statusBarNotification;
        this.ranking = ranking2;
        this.batch = eventBatch;
    }

    @NotNull
    public final String getKey() {
        return this.key;
    }

    public final int getPosition() {
        return this.position;
    }

    @NotNull
    public final StatusBarNotification getSbn() {
        return this.sbn;
    }

    @NotNull
    public final NotificationListenerService.Ranking getRanking() {
        return this.ranking;
    }

    public final void setRanking(@NotNull NotificationListenerService.Ranking ranking2) {
        Intrinsics.checkParameterIsNotNull(ranking2, "<set-?>");
        this.ranking = ranking2;
    }

    @Nullable
    public final EventBatch getBatch() {
        return this.batch;
    }

    public final void setBatch(@Nullable EventBatch eventBatch) {
        this.batch = eventBatch;
    }

    @NotNull
    public String toString() {
        return "CoalescedEvent(key=" + this.key + ')';
    }
}
