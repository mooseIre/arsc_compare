package com.android.systemui.controls.ui;

import android.app.ActivityView;
import android.content.Intent;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: DetailDialog.kt */
public final class DetailDialog$stateCallback$1 extends ActivityView.StateCallback {
    final /* synthetic */ DetailDialog this$0;

    public void onActivityViewDestroyed(@NotNull ActivityView activityView) {
        Intrinsics.checkParameterIsNotNull(activityView, "view");
    }

    /* JADX WARN: Incorrect args count in method signature: ()V */
    DetailDialog$stateCallback$1(DetailDialog detailDialog) {
        this.this$0 = detailDialog;
    }

    public void onActivityViewReady(@NotNull ActivityView activityView) {
        Intrinsics.checkParameterIsNotNull(activityView, "view");
        Intent intent = new Intent(this.this$0.getIntent());
        intent.putExtra("controls.DISPLAY_IN_PANEL", true);
        intent.addFlags(524288);
        intent.addFlags(134217728);
        activityView.startActivity(intent);
    }

    public void onTaskRemovalStarted(int i) {
        this.this$0.dismiss();
    }
}
