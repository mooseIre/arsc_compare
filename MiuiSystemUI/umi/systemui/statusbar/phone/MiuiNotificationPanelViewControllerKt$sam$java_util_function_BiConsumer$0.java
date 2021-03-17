package com.android.systemui.statusbar.phone;

import java.util.function.BiConsumer;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: MiuiNotificationPanelViewController.kt */
final class MiuiNotificationPanelViewControllerKt$sam$java_util_function_BiConsumer$0 implements BiConsumer {
    private final /* synthetic */ Function2 function;

    MiuiNotificationPanelViewControllerKt$sam$java_util_function_BiConsumer$0(Function2 function2) {
        this.function = function2;
    }

    public final /* synthetic */ void accept(Object obj, Object obj2) {
        Intrinsics.checkExpressionValueIsNotNull(this.function.invoke(obj, obj2), "invoke(...)");
    }
}
