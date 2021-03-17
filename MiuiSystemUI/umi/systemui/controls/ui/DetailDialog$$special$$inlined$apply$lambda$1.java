package com.android.systemui.controls.ui;

import android.view.View;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* access modifiers changed from: package-private */
/* compiled from: DetailDialog.kt */
public final class DetailDialog$$special$$inlined$apply$lambda$1 implements View.OnClickListener {
    final /* synthetic */ DetailDialog this$0;

    DetailDialog$$special$$inlined$apply$lambda$1(DetailDialog detailDialog) {
        this.this$0 = detailDialog;
    }

    public final void onClick(@NotNull View view) {
        Intrinsics.checkParameterIsNotNull(view, "<anonymous parameter 0>");
        this.this$0.dismiss();
    }
}
