package com.android.systemui.statusbar.notification.policy;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* access modifiers changed from: package-private */
/* compiled from: AppMiniWindowManager.kt */
public final class WindowForegroundListener {
    @NotNull
    private final Function0<Unit> callback;
    @NotNull
    private final String packageName;

    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof WindowForegroundListener)) {
            return false;
        }
        WindowForegroundListener windowForegroundListener = (WindowForegroundListener) obj;
        return Intrinsics.areEqual(this.packageName, windowForegroundListener.packageName) && Intrinsics.areEqual(this.callback, windowForegroundListener.callback);
    }

    public int hashCode() {
        String str = this.packageName;
        int i = 0;
        int hashCode = (str != null ? str.hashCode() : 0) * 31;
        Function0<Unit> function0 = this.callback;
        if (function0 != null) {
            i = function0.hashCode();
        }
        return hashCode + i;
    }

    @NotNull
    public String toString() {
        return "WindowForegroundListener(packageName=" + this.packageName + ", callback=" + this.callback + ")";
    }

    public WindowForegroundListener(@NotNull String str, @NotNull Function0<Unit> function0) {
        Intrinsics.checkParameterIsNotNull(str, "packageName");
        Intrinsics.checkParameterIsNotNull(function0, "callback");
        this.packageName = str;
        this.callback = function0;
    }

    @NotNull
    public final Function0<Unit> getCallback() {
        return this.callback;
    }

    @NotNull
    public final String getPackageName() {
        return this.packageName;
    }
}
