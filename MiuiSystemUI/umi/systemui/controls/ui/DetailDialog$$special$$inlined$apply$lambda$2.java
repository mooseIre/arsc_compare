package com.android.systemui.controls.ui;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* access modifiers changed from: package-private */
/* compiled from: DetailDialog.kt */
public final class DetailDialog$$special$$inlined$apply$lambda$2 implements View.OnClickListener {
    final /* synthetic */ ImageView $this_apply;
    final /* synthetic */ DetailDialog this$0;

    DetailDialog$$special$$inlined$apply$lambda$2(ImageView imageView, DetailDialog detailDialog) {
        this.$this_apply = imageView;
        this.this$0 = detailDialog;
    }

    public final void onClick(@NotNull View view) {
        Intrinsics.checkParameterIsNotNull(view, "v");
        this.this$0.dismiss();
        this.$this_apply.getContext().sendBroadcast(new Intent("android.intent.action.CLOSE_SYSTEM_DIALOGS"));
        view.getContext().startActivity(this.this$0.getIntent());
    }
}
