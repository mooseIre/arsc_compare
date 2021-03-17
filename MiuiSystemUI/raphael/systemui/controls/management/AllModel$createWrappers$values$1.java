package com.android.systemui.controls.management;

import com.android.systemui.controls.ControlStatus;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Lambda;
import org.jetbrains.annotations.NotNull;

/* compiled from: AllModel.kt */
final class AllModel$createWrappers$values$1 extends Lambda implements Function1<ControlStatus, ControlStatusWrapper> {
    public static final AllModel$createWrappers$values$1 INSTANCE = new AllModel$createWrappers$values$1();

    AllModel$createWrappers$values$1() {
        super(1);
    }

    @NotNull
    public final ControlStatusWrapper invoke(@NotNull ControlStatus controlStatus) {
        Intrinsics.checkParameterIsNotNull(controlStatus, "it");
        return new ControlStatusWrapper(controlStatus);
    }
}
