package com.android.systemui.media;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: MediaResumeListener.kt */
public final class MediaResumeListener$userChangeReceiver$1 extends BroadcastReceiver {
    final /* synthetic */ MediaResumeListener this$0;

    /* JADX WARN: Incorrect args count in method signature: ()V */
    MediaResumeListener$userChangeReceiver$1(MediaResumeListener mediaResumeListener) {
        this.this$0 = mediaResumeListener;
    }

    public void onReceive(@NotNull Context context, @NotNull Intent intent) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(intent, "intent");
        if (Intrinsics.areEqual("android.intent.action.USER_UNLOCKED", intent.getAction())) {
            this.this$0.loadMediaResumptionControls();
        } else if (Intrinsics.areEqual("android.intent.action.USER_SWITCHED", intent.getAction())) {
            this.this$0.currentUserId = intent.getIntExtra("android.intent.extra.user_handle", -1);
            this.this$0.loadSavedComponents();
        }
    }
}
