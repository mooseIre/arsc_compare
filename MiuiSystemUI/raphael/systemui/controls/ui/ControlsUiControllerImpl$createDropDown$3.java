package com.android.systemui.controls.ui;

import android.view.View;
import android.view.ViewGroup;
import com.android.systemui.globalactions.GlobalActionsPopupMenu;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Ref$ObjectRef;
import org.jetbrains.annotations.NotNull;

/* compiled from: ControlsUiControllerImpl.kt */
public final class ControlsUiControllerImpl$createDropDown$3 implements View.OnClickListener {
    final /* synthetic */ Ref$ObjectRef $adapter;
    final /* synthetic */ ViewGroup $anchor;
    final /* synthetic */ ControlsUiControllerImpl this$0;

    ControlsUiControllerImpl$createDropDown$3(ControlsUiControllerImpl controlsUiControllerImpl, ViewGroup viewGroup, Ref$ObjectRef ref$ObjectRef) {
        this.this$0 = controlsUiControllerImpl;
        this.$anchor = viewGroup;
        this.$adapter = ref$ObjectRef;
    }

    public void onClick(@NotNull View view) {
        Intrinsics.checkParameterIsNotNull(view, "v");
        ControlsUiControllerImpl controlsUiControllerImpl = this.this$0;
        GlobalActionsPopupMenu globalActionsPopupMenu = new GlobalActionsPopupMenu(this.this$0.popupThemedContext, true);
        globalActionsPopupMenu.setAnchorView(this.$anchor);
        globalActionsPopupMenu.setAdapter(this.$adapter.element);
        globalActionsPopupMenu.setOnItemClickListener(new ControlsUiControllerImpl$createDropDown$3$onClick$$inlined$apply$lambda$1(globalActionsPopupMenu, this));
        globalActionsPopupMenu.show();
        controlsUiControllerImpl.popup = globalActionsPopupMenu;
    }
}
