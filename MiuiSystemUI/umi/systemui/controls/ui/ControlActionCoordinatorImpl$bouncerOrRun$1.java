package com.android.systemui.controls.ui;

import android.util.Log;
import com.android.systemui.controls.ui.ControlActionCoordinatorImpl;
import com.android.systemui.plugins.ActivityStarter;
import kotlin.jvm.internal.Ref$BooleanRef;

/* compiled from: ControlActionCoordinatorImpl.kt */
final class ControlActionCoordinatorImpl$bouncerOrRun$1 implements ActivityStarter.OnDismissAction {
    final /* synthetic */ ControlActionCoordinatorImpl.Action $action;
    final /* synthetic */ Ref$BooleanRef $closeGlobalActions;
    final /* synthetic */ ControlActionCoordinatorImpl this$0;

    ControlActionCoordinatorImpl$bouncerOrRun$1(ControlActionCoordinatorImpl controlActionCoordinatorImpl, Ref$BooleanRef ref$BooleanRef, ControlActionCoordinatorImpl.Action action) {
        this.this$0 = controlActionCoordinatorImpl;
        this.$closeGlobalActions = ref$BooleanRef;
        this.$action = action;
    }

    public final boolean onDismiss() {
        Log.d("ControlsUiController", "Device unlocked, invoking controls action");
        if (this.$closeGlobalActions.element) {
            this.this$0.globalActionsComponent.handleShowGlobalActionsMenu();
            return true;
        }
        this.$action.invoke();
        return true;
    }
}
