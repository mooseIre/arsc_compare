package com.android.systemui.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: RingerModeTrackerImpl.kt */
public final class RingerModeLiveData$receiver$1 extends BroadcastReceiver {
    final /* synthetic */ RingerModeLiveData this$0;

    /* JADX WARN: Incorrect args count in method signature: ()V */
    RingerModeLiveData$receiver$1(RingerModeLiveData ringerModeLiveData) {
        this.this$0 = ringerModeLiveData;
    }

    public void onReceive(@NotNull Context context, @NotNull Intent intent) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(intent, "intent");
        RingerModeLiveData.access$setInitialSticky$p(this.this$0, isInitialStickyBroadcast());
        this.this$0.postValue(Integer.valueOf(intent.getIntExtra("android.media.EXTRA_RINGER_MODE", -1)));
    }
}
