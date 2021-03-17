package com.android.systemui.controls.ui;

import android.view.View;
import android.widget.AdapterView;
import com.android.systemui.globalactions.GlobalActionsPopupMenu;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: ControlsUiControllerImpl.kt */
public final class ControlsUiControllerImpl$createDropDown$3$onClick$$inlined$apply$lambda$1 implements AdapterView.OnItemClickListener {
    final /* synthetic */ GlobalActionsPopupMenu $this_apply;
    final /* synthetic */ ControlsUiControllerImpl$createDropDown$3 this$0;

    ControlsUiControllerImpl$createDropDown$3$onClick$$inlined$apply$lambda$1(GlobalActionsPopupMenu globalActionsPopupMenu, ControlsUiControllerImpl$createDropDown$3 controlsUiControllerImpl$createDropDown$3) {
        this.$this_apply = globalActionsPopupMenu;
        this.this$0 = controlsUiControllerImpl$createDropDown$3;
    }

    @Override // android.widget.AdapterView.OnItemClickListener
    public void onItemClick(@NotNull AdapterView<?> adapterView, @NotNull View view, int i, long j) {
        Intrinsics.checkParameterIsNotNull(adapterView, "parent");
        Intrinsics.checkParameterIsNotNull(view, "view");
        Object itemAtPosition = adapterView.getItemAtPosition(i);
        if (itemAtPosition != null) {
            this.this$0.this$0.switchAppOrStructure((SelectionItem) itemAtPosition);
            this.$this_apply.dismiss();
            return;
        }
        throw new TypeCastException("null cannot be cast to non-null type com.android.systemui.controls.ui.SelectionItem");
    }
}
