package com.android.systemui.controls.ui;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import java.util.List;
import kotlin.jvm.internal.Intrinsics;

/* access modifiers changed from: package-private */
/* compiled from: ControlActionCoordinatorImpl.kt */
public final class ControlActionCoordinatorImpl$showDialog$1 implements Runnable {
    final /* synthetic */ ControlViewHolder $cvh;
    final /* synthetic */ Intent $intent;
    final /* synthetic */ ControlActionCoordinatorImpl this$0;

    ControlActionCoordinatorImpl$showDialog$1(ControlActionCoordinatorImpl controlActionCoordinatorImpl, ControlViewHolder controlViewHolder, Intent intent) {
        this.this$0 = controlActionCoordinatorImpl;
        this.$cvh = controlViewHolder;
        this.$intent = intent;
    }

    public final void run() {
        final List<ResolveInfo> queryIntentActivities = this.$cvh.getContext().getPackageManager().queryIntentActivities(this.$intent, 65536);
        Intrinsics.checkExpressionValueIsNotNull(queryIntentActivities, "cvh.context.packageManag…EFAULT_ONLY\n            )");
        this.this$0.uiExecutor.execute(new Runnable(this) {
            /* class com.android.systemui.controls.ui.ControlActionCoordinatorImpl$showDialog$1.AnonymousClass1 */
            final /* synthetic */ ControlActionCoordinatorImpl$showDialog$1 this$0;

            {
                this.this$0 = r1;
            }

            public final void run() {
                if (!queryIntentActivities.isEmpty()) {
                    ControlActionCoordinatorImpl controlActionCoordinatorImpl = this.this$0.this$0;
                    ControlActionCoordinatorImpl$showDialog$1 controlActionCoordinatorImpl$showDialog$1 = this.this$0;
                    DetailDialog detailDialog = new DetailDialog(controlActionCoordinatorImpl$showDialog$1.$cvh, controlActionCoordinatorImpl$showDialog$1.$intent);
                    detailDialog.setOnDismissListener(new ControlActionCoordinatorImpl$showDialog$1$1$$special$$inlined$also$lambda$1(this));
                    detailDialog.show();
                    controlActionCoordinatorImpl.dialog = detailDialog;
                    return;
                }
                this.this$0.$cvh.setErrorStatus();
            }
        });
    }
}
