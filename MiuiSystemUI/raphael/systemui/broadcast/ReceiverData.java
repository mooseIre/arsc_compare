package com.android.systemui.broadcast;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.os.UserHandle;
import java.util.concurrent.Executor;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: BroadcastDispatcher.kt */
public final class ReceiverData {
    @NotNull
    private final Executor executor;
    @NotNull
    private final IntentFilter filter;
    @NotNull
    private final BroadcastReceiver receiver;
    @NotNull
    private final UserHandle user;

    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ReceiverData)) {
            return false;
        }
        ReceiverData receiverData = (ReceiverData) obj;
        return Intrinsics.areEqual(this.receiver, receiverData.receiver) && Intrinsics.areEqual(this.filter, receiverData.filter) && Intrinsics.areEqual(this.executor, receiverData.executor) && Intrinsics.areEqual(this.user, receiverData.user);
    }

    public int hashCode() {
        BroadcastReceiver broadcastReceiver = this.receiver;
        int i = 0;
        int hashCode = (broadcastReceiver != null ? broadcastReceiver.hashCode() : 0) * 31;
        IntentFilter intentFilter = this.filter;
        int hashCode2 = (hashCode + (intentFilter != null ? intentFilter.hashCode() : 0)) * 31;
        Executor executor2 = this.executor;
        int hashCode3 = (hashCode2 + (executor2 != null ? executor2.hashCode() : 0)) * 31;
        UserHandle userHandle = this.user;
        if (userHandle != null) {
            i = userHandle.hashCode();
        }
        return hashCode3 + i;
    }

    @NotNull
    public String toString() {
        return "ReceiverData(receiver=" + this.receiver + ", filter=" + this.filter + ", executor=" + this.executor + ", user=" + this.user + ")";
    }

    public ReceiverData(@NotNull BroadcastReceiver broadcastReceiver, @NotNull IntentFilter intentFilter, @NotNull Executor executor2, @NotNull UserHandle userHandle) {
        Intrinsics.checkParameterIsNotNull(broadcastReceiver, "receiver");
        Intrinsics.checkParameterIsNotNull(intentFilter, "filter");
        Intrinsics.checkParameterIsNotNull(executor2, "executor");
        Intrinsics.checkParameterIsNotNull(userHandle, "user");
        this.receiver = broadcastReceiver;
        this.filter = intentFilter;
        this.executor = executor2;
        this.user = userHandle;
    }

    @NotNull
    public final BroadcastReceiver getReceiver() {
        return this.receiver;
    }

    @NotNull
    public final IntentFilter getFilter() {
        return this.filter;
    }

    @NotNull
    public final Executor getExecutor() {
        return this.executor;
    }

    @NotNull
    public final UserHandle getUser() {
        return this.user;
    }
}
