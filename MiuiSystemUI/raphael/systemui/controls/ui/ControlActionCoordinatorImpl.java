package com.android.systemui.controls.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.service.controls.Control;
import com.android.systemui.globalactions.GlobalActionsComponent;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.android.systemui.util.concurrency.DelayableExecutor;
import java.util.LinkedHashSet;
import java.util.Set;
import kotlin.TypeCastException;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Ref$BooleanRef;
import org.jetbrains.annotations.NotNull;

/* compiled from: ControlActionCoordinatorImpl.kt */
public final class ControlActionCoordinatorImpl implements ControlActionCoordinator {
    private Set<String> actionsInProgress;
    private final ActivityStarter activityStarter;
    private final DelayableExecutor bgExecutor;
    private final Context context;
    private Dialog dialog;
    private final GlobalActionsComponent globalActionsComponent;
    private final KeyguardStateController keyguardStateController;
    private Action pendingAction;
    private final DelayableExecutor uiExecutor;
    private final Vibrator vibrator;

    public ControlActionCoordinatorImpl(@NotNull Context context2, @NotNull DelayableExecutor delayableExecutor, @NotNull DelayableExecutor delayableExecutor2, @NotNull ActivityStarter activityStarter2, @NotNull KeyguardStateController keyguardStateController2, @NotNull GlobalActionsComponent globalActionsComponent2) {
        Intrinsics.checkParameterIsNotNull(context2, "context");
        Intrinsics.checkParameterIsNotNull(delayableExecutor, "bgExecutor");
        Intrinsics.checkParameterIsNotNull(delayableExecutor2, "uiExecutor");
        Intrinsics.checkParameterIsNotNull(activityStarter2, "activityStarter");
        Intrinsics.checkParameterIsNotNull(keyguardStateController2, "keyguardStateController");
        Intrinsics.checkParameterIsNotNull(globalActionsComponent2, "globalActionsComponent");
        this.context = context2;
        this.bgExecutor = delayableExecutor;
        this.uiExecutor = delayableExecutor2;
        this.activityStarter = activityStarter2;
        this.keyguardStateController = keyguardStateController2;
        this.globalActionsComponent = globalActionsComponent2;
        Object systemService = context2.getSystemService("vibrator");
        if (systemService != null) {
            this.vibrator = (Vibrator) systemService;
            this.actionsInProgress = new LinkedHashSet();
            return;
        }
        throw new TypeCastException("null cannot be cast to non-null type android.os.Vibrator");
    }

    public static final /* synthetic */ GlobalActionsComponent access$getGlobalActionsComponent$p(ControlActionCoordinatorImpl controlActionCoordinatorImpl) {
        return controlActionCoordinatorImpl.globalActionsComponent;
    }

    @Override // com.android.systemui.controls.ui.ControlActionCoordinator
    public void closeDialogs() {
        Dialog dialog2 = this.dialog;
        if (dialog2 != null) {
            dialog2.dismiss();
        }
        this.dialog = null;
    }

    @Override // com.android.systemui.controls.ui.ControlActionCoordinator
    public void toggle(@NotNull ControlViewHolder controlViewHolder, @NotNull String str, boolean z) {
        Intrinsics.checkParameterIsNotNull(controlViewHolder, "cvh");
        Intrinsics.checkParameterIsNotNull(str, "templateId");
        bouncerOrRun(new Action(this, controlViewHolder.getCws().getCi().getControlId(), new ControlActionCoordinatorImpl$toggle$1(controlViewHolder, str, z), true));
    }

    @Override // com.android.systemui.controls.ui.ControlActionCoordinator
    public void touch(@NotNull ControlViewHolder controlViewHolder, @NotNull String str, @NotNull Control control) {
        Intrinsics.checkParameterIsNotNull(controlViewHolder, "cvh");
        Intrinsics.checkParameterIsNotNull(str, "templateId");
        Intrinsics.checkParameterIsNotNull(control, "control");
        bouncerOrRun(new Action(this, controlViewHolder.getCws().getCi().getControlId(), new ControlActionCoordinatorImpl$touch$1(this, controlViewHolder, control, str), controlViewHolder.usePanel()));
    }

    @Override // com.android.systemui.controls.ui.ControlActionCoordinator
    public void drag(boolean z) {
        if (z) {
            vibrate(Vibrations.INSTANCE.getRangeEdgeEffect());
        } else {
            vibrate(Vibrations.INSTANCE.getRangeMiddleEffect());
        }
    }

    @Override // com.android.systemui.controls.ui.ControlActionCoordinator
    public void setValue(@NotNull ControlViewHolder controlViewHolder, @NotNull String str, float f) {
        Intrinsics.checkParameterIsNotNull(controlViewHolder, "cvh");
        Intrinsics.checkParameterIsNotNull(str, "templateId");
        bouncerOrRun(new Action(this, controlViewHolder.getCws().getCi().getControlId(), new ControlActionCoordinatorImpl$setValue$1(controlViewHolder, str, f), true));
    }

    @Override // com.android.systemui.controls.ui.ControlActionCoordinator
    public void longPress(@NotNull ControlViewHolder controlViewHolder) {
        Intrinsics.checkParameterIsNotNull(controlViewHolder, "cvh");
        bouncerOrRun(new Action(this, controlViewHolder.getCws().getCi().getControlId(), new ControlActionCoordinatorImpl$longPress$1(this, controlViewHolder), false));
    }

    @Override // com.android.systemui.controls.ui.ControlActionCoordinator
    public void runPendingAction(@NotNull String str) {
        Intrinsics.checkParameterIsNotNull(str, "controlId");
        Action action = this.pendingAction;
        if (Intrinsics.areEqual(action != null ? action.getControlId() : null, str)) {
            Action action2 = this.pendingAction;
            if (action2 != null) {
                action2.invoke();
            }
            this.pendingAction = null;
        }
    }

    @Override // com.android.systemui.controls.ui.ControlActionCoordinator
    public void enableActionOnTouch(@NotNull String str) {
        Intrinsics.checkParameterIsNotNull(str, "controlId");
        this.actionsInProgress.remove(str);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private final boolean shouldRunAction(String str) {
        if (!this.actionsInProgress.add(str)) {
            return false;
        }
        this.uiExecutor.executeDelayed(new ControlActionCoordinatorImpl$shouldRunAction$1(this, str), 3000);
        return true;
    }

    private final void bouncerOrRun(Action action) {
        if (this.keyguardStateController.isShowing()) {
            Ref$BooleanRef ref$BooleanRef = new Ref$BooleanRef();
            boolean z = !this.keyguardStateController.isUnlocked();
            ref$BooleanRef.element = z;
            if (z) {
                this.context.sendBroadcast(new Intent("android.intent.action.CLOSE_SYSTEM_DIALOGS"));
                this.pendingAction = action;
            }
            this.activityStarter.dismissKeyguardThenExecute(new ControlActionCoordinatorImpl$bouncerOrRun$1(this, ref$BooleanRef, action), new ControlActionCoordinatorImpl$bouncerOrRun$2(this), true);
            return;
        }
        action.invoke();
    }

    private final void vibrate(VibrationEffect vibrationEffect) {
        this.bgExecutor.execute(new ControlActionCoordinatorImpl$vibrate$1(this, vibrationEffect));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private final void showDialog(ControlViewHolder controlViewHolder, Intent intent) {
        this.bgExecutor.execute(new ControlActionCoordinatorImpl$showDialog$1(this, controlViewHolder, intent));
    }

    /* compiled from: ControlActionCoordinatorImpl.kt */
    public final class Action {
        private final boolean blockable;
        @NotNull
        private final String controlId;
        @NotNull
        private final Function0<Unit> f;
        final /* synthetic */ ControlActionCoordinatorImpl this$0;

        public Action(@NotNull ControlActionCoordinatorImpl controlActionCoordinatorImpl, @NotNull String str, Function0<Unit> function0, boolean z) {
            Intrinsics.checkParameterIsNotNull(str, "controlId");
            Intrinsics.checkParameterIsNotNull(function0, "f");
            this.this$0 = controlActionCoordinatorImpl;
            this.controlId = str;
            this.f = function0;
            this.blockable = z;
        }

        @NotNull
        public final String getControlId() {
            return this.controlId;
        }

        public final void invoke() {
            if (!this.blockable || this.this$0.shouldRunAction(this.controlId)) {
                this.f.invoke();
            }
        }
    }
}
