package com.android.systemui.controls.ui;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.service.controls.Control;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Lambda;

/* access modifiers changed from: package-private */
/* compiled from: ControlViewHolder.kt */
public final class ControlViewHolder$applyRenderInfo$1 extends Lambda implements Function0<Unit> {
    final /* synthetic */ Control $control;
    final /* synthetic */ boolean $enabled;
    final /* synthetic */ ColorStateList $fg;
    final /* synthetic */ CharSequence $newText;
    final /* synthetic */ RenderInfo $ri;
    final /* synthetic */ ControlViewHolder this$0;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    ControlViewHolder$applyRenderInfo$1(ControlViewHolder controlViewHolder, boolean z, CharSequence charSequence, RenderInfo renderInfo, ColorStateList colorStateList, Control control) {
        super(0);
        this.this$0 = controlViewHolder;
        this.$enabled = z;
        this.$newText = charSequence;
        this.$ri = renderInfo;
        this.$fg = colorStateList;
        this.$control = control;
    }

    @Override // kotlin.jvm.functions.Function0
    public final void invoke() {
        ControlViewHolder controlViewHolder = this.this$0;
        boolean z = this.$enabled;
        CharSequence charSequence = this.$newText;
        Drawable icon = this.$ri.getIcon();
        ColorStateList colorStateList = this.$fg;
        Intrinsics.checkExpressionValueIsNotNull(colorStateList, "fg");
        controlViewHolder.updateStatusRow(z, charSequence, icon, colorStateList, this.$control);
    }
}
