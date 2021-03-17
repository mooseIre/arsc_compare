package com.android.systemui.controls.management;

import com.android.systemui.controls.TooltipManager;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Lambda;

/* compiled from: ControlsFavoritingActivity.kt */
final class ControlsFavoritingActivity$bindViews$$inlined$apply$lambda$1 extends Lambda implements Function1<Integer, Unit> {
    final /* synthetic */ ControlsFavoritingActivity this$0;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    ControlsFavoritingActivity$bindViews$$inlined$apply$lambda$1(ControlsFavoritingActivity controlsFavoritingActivity) {
        super(1);
        this.this$0 = controlsFavoritingActivity;
    }

    /* Return type fixed from 'java.lang.Object' to match base method */
    /* JADX DEBUG: Method arguments types fixed to match base method, original types: [java.lang.Object] */
    @Override // kotlin.jvm.functions.Function1
    public /* bridge */ /* synthetic */ Unit invoke(Integer num) {
        invoke(num.intValue());
        return Unit.INSTANCE;
    }

    public final void invoke(int i) {
        TooltipManager access$getMTooltipManager$p;
        if (i != 0 && (access$getMTooltipManager$p = ControlsFavoritingActivity.access$getMTooltipManager$p(this.this$0)) != null) {
            access$getMTooltipManager$p.hide(true);
        }
    }
}
