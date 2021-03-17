package com.android.systemui.controls.management;

import com.android.systemui.controls.ControlsServiceInfo;
import com.android.systemui.controls.management.ControlsListingController;
import java.util.List;
import java.util.concurrent.Executor;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: AppAdapter.kt */
public final class AppAdapter$callback$1 implements ControlsListingController.ControlsListingCallback {
    final /* synthetic */ Executor $backgroundExecutor;
    final /* synthetic */ Executor $uiExecutor;
    final /* synthetic */ AppAdapter this$0;

    AppAdapter$callback$1(AppAdapter appAdapter, Executor executor, Executor executor2) {
        this.this$0 = appAdapter;
        this.$backgroundExecutor = executor;
        this.$uiExecutor = executor2;
    }

    @Override // com.android.systemui.controls.management.ControlsListingController.ControlsListingCallback
    public void onServicesUpdated(@NotNull List<ControlsServiceInfo> list) {
        Intrinsics.checkParameterIsNotNull(list, "serviceInfos");
        this.$backgroundExecutor.execute(new AppAdapter$callback$1$onServicesUpdated$1(this, list));
    }
}
