package com.android.systemui.controls.ui;

import android.view.View;
import android.widget.ImageView;
import com.android.systemui.globalactions.GlobalActionsPopupMenu;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Ref$ObjectRef;
import org.jetbrains.annotations.NotNull;

/* compiled from: ControlsUiControllerImpl.kt */
public final class ControlsUiControllerImpl$createMenu$1 implements View.OnClickListener {
    final /* synthetic */ Ref$ObjectRef $adapter;
    final /* synthetic */ ImageView $anchor;
    final /* synthetic */ ControlsUiControllerImpl this$0;

    ControlsUiControllerImpl$createMenu$1(ControlsUiControllerImpl controlsUiControllerImpl, ImageView imageView, Ref$ObjectRef ref$ObjectRef) {
        this.this$0 = controlsUiControllerImpl;
        this.$anchor = imageView;
        this.$adapter = ref$ObjectRef;
    }

    public void onClick(@NotNull View view) {
        Intrinsics.checkParameterIsNotNull(view, "v");
        ControlsUiControllerImpl controlsUiControllerImpl = this.this$0;
        GlobalActionsPopupMenu globalActionsPopupMenu = new GlobalActionsPopupMenu(this.this$0.popupThemedContext, false);
        globalActionsPopupMenu.setAnchorView(this.$anchor);
        globalActionsPopupMenu.setAdapter(this.$adapter.element);
        globalActionsPopupMenu.setOnItemClickListener(new ControlsUiControllerImpl$createMenu$1$onClick$$inlined$apply$lambda$1(globalActionsPopupMenu, this));
        globalActionsPopupMenu.show();
        controlsUiControllerImpl.popup = globalActionsPopupMenu;
    }
}
