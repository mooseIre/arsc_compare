package com.android.systemui.util;

import android.content.IntentFilter;
import android.os.UserHandle;
import androidx.lifecycle.MutableLiveData;
import com.android.systemui.broadcast.BroadcastDispatcher;
import java.util.concurrent.Executor;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: RingerModeTrackerImpl.kt */
public final class RingerModeLiveData extends MutableLiveData<Integer> {
    private final BroadcastDispatcher broadcastDispatcher;
    private final Executor executor;
    private final IntentFilter filter;
    private final Function0<Integer> getter;
    private boolean initialSticky;
    private final RingerModeLiveData$receiver$1 receiver = new RingerModeLiveData$receiver$1(this);

    public RingerModeLiveData(@NotNull BroadcastDispatcher broadcastDispatcher2, @NotNull Executor executor2, @NotNull String str, @NotNull Function0<Integer> function0) {
        Intrinsics.checkParameterIsNotNull(broadcastDispatcher2, "broadcastDispatcher");
        Intrinsics.checkParameterIsNotNull(executor2, "executor");
        Intrinsics.checkParameterIsNotNull(str, "intent");
        Intrinsics.checkParameterIsNotNull(function0, "getter");
        this.broadcastDispatcher = broadcastDispatcher2;
        this.executor = executor2;
        this.getter = function0;
        this.filter = new IntentFilter(str);
    }

    public final boolean getInitialSticky() {
        return this.initialSticky;
    }

    @Override // androidx.lifecycle.LiveData
    @NotNull
    public Integer getValue() {
        Integer num = (Integer) super.getValue();
        return Integer.valueOf(num != null ? num.intValue() : -1);
    }

    /* access modifiers changed from: protected */
    @Override // androidx.lifecycle.LiveData
    public void onActive() {
        super.onActive();
        BroadcastDispatcher broadcastDispatcher2 = this.broadcastDispatcher;
        RingerModeLiveData$receiver$1 ringerModeLiveData$receiver$1 = this.receiver;
        IntentFilter intentFilter = this.filter;
        Executor executor2 = this.executor;
        UserHandle userHandle = UserHandle.ALL;
        Intrinsics.checkExpressionValueIsNotNull(userHandle, "UserHandle.ALL");
        broadcastDispatcher2.registerReceiver(ringerModeLiveData$receiver$1, intentFilter, executor2, userHandle);
        this.executor.execute(new RingerModeLiveData$onActive$1(this));
    }

    /* access modifiers changed from: protected */
    @Override // androidx.lifecycle.LiveData
    public void onInactive() {
        super.onInactive();
        this.broadcastDispatcher.unregisterReceiver(this.receiver);
    }
}
