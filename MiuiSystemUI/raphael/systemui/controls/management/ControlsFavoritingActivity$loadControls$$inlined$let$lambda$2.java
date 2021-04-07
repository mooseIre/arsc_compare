package com.android.systemui.controls.management;

import java.util.function.Consumer;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* access modifiers changed from: package-private */
/* compiled from: ControlsFavoritingActivity.kt */
public final class ControlsFavoritingActivity$loadControls$$inlined$let$lambda$2<T> implements Consumer<Runnable> {
    final /* synthetic */ ControlsFavoritingActivity this$0;

    ControlsFavoritingActivity$loadControls$$inlined$let$lambda$2(ControlsFavoritingActivity controlsFavoritingActivity) {
        this.this$0 = controlsFavoritingActivity;
    }

    public final void accept(@NotNull Runnable runnable) {
        Intrinsics.checkParameterIsNotNull(runnable, "runnable");
        this.this$0.cancelLoadRunnable = runnable;
    }
}
