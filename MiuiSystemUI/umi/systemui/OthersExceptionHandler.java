package com.android.systemui;

import android.content.Context;
import com.miui.systemui.util.MiuiThemeUtils;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: ExceptionHandler.kt */
public final class OthersExceptionHandler {
    public static final OthersExceptionHandler INSTANCE = new OthersExceptionHandler();

    private OthersExceptionHandler() {
    }

    public final void tryFixCrash(@NotNull Context context, @Nullable String str) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        if (!MiuiThemeUtils.isDefaultLockScreenTheme()) {
            ThemeExceptionHandler.INSTANCE.tryFixCrash(context, str);
        }
    }
}
