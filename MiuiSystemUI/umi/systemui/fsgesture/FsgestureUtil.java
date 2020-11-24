package com.android.systemui.fsgesture;

import android.view.View;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: FsgestureUtil.kt */
public final class FsgestureUtil {
    public static final FsgestureUtil INSTANCE = new FsgestureUtil();

    private FsgestureUtil() {
    }

    public final void hideSystemBars(@NotNull View view) {
        Intrinsics.checkParameterIsNotNull(view, "decorView");
        view.setSystemUiVisibility(12038);
    }

    public final void wholeHideSystemBars(@NotNull View view) {
        Intrinsics.checkParameterIsNotNull(view, "decorView");
        view.setSystemUiVisibility(16134);
    }
}
