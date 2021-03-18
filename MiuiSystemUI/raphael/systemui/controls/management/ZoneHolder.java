package com.android.systemui.controls.management;

import android.view.View;
import android.widget.TextView;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* access modifiers changed from: package-private */
/* compiled from: ControlAdapter.kt */
public final class ZoneHolder extends Holder {
    private final TextView zone;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public ZoneHolder(@NotNull View view) {
        super(view, null);
        Intrinsics.checkParameterIsNotNull(view, "view");
        View view2 = this.itemView;
        if (view2 != null) {
            this.zone = (TextView) view2;
            return;
        }
        throw new TypeCastException("null cannot be cast to non-null type android.widget.TextView");
    }

    @Override // com.android.systemui.controls.management.Holder
    public void bindData(@NotNull ElementWrapper elementWrapper) {
        Intrinsics.checkParameterIsNotNull(elementWrapper, "wrapper");
        this.zone.setText(((ZoneNameWrapper) elementWrapper).getZoneName());
    }
}
