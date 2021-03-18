package com.android.systemui.controls.ui;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import com.android.systemui.globalactions.GlobalActionsPopupMenu;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: ControlsUiControllerImpl.kt */
public final class ControlsUiControllerImpl$createMenu$1$onClick$$inlined$apply$lambda$1 implements AdapterView.OnItemClickListener {
    final /* synthetic */ GlobalActionsPopupMenu $this_apply;
    final /* synthetic */ ControlsUiControllerImpl$createMenu$1 this$0;

    ControlsUiControllerImpl$createMenu$1$onClick$$inlined$apply$lambda$1(GlobalActionsPopupMenu globalActionsPopupMenu, ControlsUiControllerImpl$createMenu$1 controlsUiControllerImpl$createMenu$1) {
        this.$this_apply = globalActionsPopupMenu;
        this.this$0 = controlsUiControllerImpl$createMenu$1;
    }

    @Override // android.widget.AdapterView.OnItemClickListener
    public void onItemClick(@NotNull AdapterView<?> adapterView, @NotNull View view, int i, long j) {
        Intrinsics.checkParameterIsNotNull(adapterView, "parent");
        Intrinsics.checkParameterIsNotNull(view, "view");
        if (i == 0) {
            ControlsUiControllerImpl controlsUiControllerImpl = this.this$0.this$0;
            Context context = view.getContext();
            Intrinsics.checkExpressionValueIsNotNull(context, "view.context");
            controlsUiControllerImpl.startFavoritingActivity(context, this.this$0.this$0.selectedStructure);
        } else if (i == 1) {
            ControlsUiControllerImpl controlsUiControllerImpl2 = this.this$0.this$0;
            Context context2 = view.getContext();
            Intrinsics.checkExpressionValueIsNotNull(context2, "view.context");
            controlsUiControllerImpl2.startEditingActivity(context2, this.this$0.this$0.selectedStructure);
        }
        this.$this_apply.dismiss();
    }
}
