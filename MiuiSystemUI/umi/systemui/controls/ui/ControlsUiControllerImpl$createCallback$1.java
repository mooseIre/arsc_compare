package com.android.systemui.controls.ui;

import android.content.ComponentName;
import android.graphics.drawable.Drawable;
import com.android.systemui.controls.ControlsServiceInfo;
import com.android.systemui.controls.management.ControlsListingController;
import java.util.ArrayList;
import java.util.List;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: ControlsUiControllerImpl.kt */
public final class ControlsUiControllerImpl$createCallback$1 implements ControlsListingController.ControlsListingCallback {
    final /* synthetic */ Function1 $onResult;
    final /* synthetic */ ControlsUiControllerImpl this$0;

    ControlsUiControllerImpl$createCallback$1(ControlsUiControllerImpl controlsUiControllerImpl, Function1 function1) {
        this.this$0 = controlsUiControllerImpl;
        this.$onResult = function1;
    }

    public void onServicesUpdated(@NotNull List<ControlsServiceInfo> list) {
        Intrinsics.checkParameterIsNotNull(list, "serviceInfos");
        ArrayList arrayList = new ArrayList(CollectionsKt__IterablesKt.collectionSizeOrDefault(list, 10));
        for (ControlsServiceInfo controlsServiceInfo : list) {
            CharSequence loadLabel = controlsServiceInfo.loadLabel();
            Intrinsics.checkExpressionValueIsNotNull(loadLabel, "it.loadLabel()");
            Drawable loadIcon = controlsServiceInfo.loadIcon();
            Intrinsics.checkExpressionValueIsNotNull(loadIcon, "it.loadIcon()");
            ComponentName componentName = controlsServiceInfo.componentName;
            Intrinsics.checkExpressionValueIsNotNull(componentName, "it.componentName");
            arrayList.add(new SelectionItem(loadLabel, "", loadIcon, componentName));
        }
        this.this$0.getUiExecutor().execute(new ControlsUiControllerImpl$createCallback$1$onServicesUpdated$1(this, arrayList));
    }
}
