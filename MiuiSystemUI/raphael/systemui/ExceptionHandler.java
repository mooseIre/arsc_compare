package com.android.systemui;

import android.content.Context;
import android.util.Log;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: ExceptionHandler.kt */
public enum ExceptionHandler {
    Notification,
    Others;
    
    public static final Companion Companion = new Companion(null);

    /* compiled from: ExceptionHandler.kt */
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        public final void tryFixCrash(@NotNull Context context, @Nullable String str, @Nullable String str2) {
            Intrinsics.checkParameterIsNotNull(context, "context");
            Log.d("CodeBlue", "tryFixCrash handler=" + str + " clues=" + str2);
            if (Intrinsics.areEqual(str, ExceptionHandler.Notification.name())) {
                NotificationExceptionHandler.INSTANCE.tryFixCrash(context, str2);
            } else if (Intrinsics.areEqual(str, ExceptionHandler.Others.name())) {
                OthersExceptionHandler.INSTANCE.tryFixCrash(context, str2);
            }
        }
    }
}
