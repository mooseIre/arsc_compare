package com.android.systemui;

import android.content.Context;
import com.android.systemui.plugins.NotificationListenerController;
import com.android.systemui.statusbar.NotificationListener;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: ExceptionHandler.kt */
public final class NotificationExceptionHandler {
    public static final NotificationExceptionHandler INSTANCE = new NotificationExceptionHandler();

    private NotificationExceptionHandler() {
    }

    public final void tryFixCrash(@NotNull Context context, @Nullable String str) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        if (!CodeBlueConfig.Companion.isFirstTimeFixCrash(context)) {
            str = null;
        }
        ((NotificationListener) Dependency.get(NotificationListener.class)).onPluginConnected(createFixPatch(str), context);
    }

    private final NotificationListenerController createFixPatch(String str) {
        return new NotificationExceptionHandler$createFixPatch$1(str == null || str.length() == 0, str);
    }
}
