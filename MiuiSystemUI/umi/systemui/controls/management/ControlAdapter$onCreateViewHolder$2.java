package com.android.systemui.controls.management;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Lambda;
import org.jetbrains.annotations.NotNull;

/* compiled from: ControlAdapter.kt */
final class ControlAdapter$onCreateViewHolder$2 extends Lambda implements Function2<String, Boolean, Unit> {
    final /* synthetic */ ControlAdapter this$0;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    ControlAdapter$onCreateViewHolder$2(ControlAdapter controlAdapter) {
        super(2);
        this.this$0 = controlAdapter;
    }

    /* Return type fixed from 'java.lang.Object' to match base method */
    /* JADX DEBUG: Method arguments types fixed to match base method, original types: [java.lang.Object, java.lang.Object] */
    @Override // kotlin.jvm.functions.Function2
    public /* bridge */ /* synthetic */ Unit invoke(String str, Boolean bool) {
        invoke(str, bool.booleanValue());
        return Unit.INSTANCE;
    }

    public final void invoke(@NotNull String str, boolean z) {
        Intrinsics.checkParameterIsNotNull(str, "id");
        ControlsModel access$getModel$p = ControlAdapter.access$getModel$p(this.this$0);
        if (access$getModel$p != null) {
            access$getModel$p.changeFavoriteStatus(str, z);
        }
    }
}
