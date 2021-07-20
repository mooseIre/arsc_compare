package com.android.systemui.controls.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.res.Resources;
import android.service.controls.actions.BooleanAction;
import android.service.controls.actions.CommandAction;
import android.service.controls.actions.ControlAction;
import android.service.controls.actions.FloatAction;
import android.service.controls.actions.ModeAction;
import android.util.Log;
import android.view.Window;
import android.widget.EditText;
import com.android.systemui.C0017R$layout;
import com.android.systemui.C0021R$string;
import kotlin.Pair;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: ChallengeDialogs.kt */
public final class ChallengeDialogs {
    public static final ChallengeDialogs INSTANCE = new ChallengeDialogs();

    private ChallengeDialogs() {
    }

    @Nullable
    public final Dialog createPinDialog(@NotNull ControlViewHolder controlViewHolder, boolean z, boolean z2, @NotNull Function0<Unit> function0) {
        Pair pair;
        Intrinsics.checkParameterIsNotNull(controlViewHolder, "cvh");
        Intrinsics.checkParameterIsNotNull(function0, "onCancel");
        ControlAction lastAction = controlViewHolder.getLastAction();
        if (lastAction == null) {
            Log.e("ControlsUiController", "PIN Dialog attempted but no last action is set. Will not show");
            return null;
        }
        Resources resources = controlViewHolder.getContext().getResources();
        if (z2) {
            pair = new Pair(resources.getString(C0021R$string.controls_pin_wrong), Integer.valueOf(C0021R$string.controls_pin_instructions_retry));
        } else {
            pair = new Pair(resources.getString(C0021R$string.controls_pin_verify, controlViewHolder.getTitle().getText()), Integer.valueOf(C0021R$string.controls_pin_instructions));
        }
        String str = (String) pair.component1();
        int intValue = ((Number) pair.component2()).intValue();
        AlertDialog.Builder builder = new AlertDialog.Builder(controlViewHolder.getContext(), 16974545);
        builder.setTitle(str);
        builder.setView(C0017R$layout.controls_dialog_pin);
        builder.setPositiveButton(17039370, new ChallengeDialogs$createPinDialog$$inlined$apply$lambda$1(str, controlViewHolder, lastAction, function0));
        builder.setNegativeButton(17039360, new ChallengeDialogs$createPinDialog$$inlined$apply$lambda$2(str, controlViewHolder, lastAction, function0));
        AlertDialog create = builder.create();
        Window window = create.getWindow();
        window.setType(2020);
        window.setSoftInputMode(4);
        create.setOnShowListener(new ChallengeDialogs$createPinDialog$$inlined$apply$lambda$3(create, intValue, z));
        return create;
    }

    @Nullable
    public final Dialog createConfirmationDialog(@NotNull ControlViewHolder controlViewHolder, @NotNull Function0<Unit> function0) {
        Intrinsics.checkParameterIsNotNull(controlViewHolder, "cvh");
        Intrinsics.checkParameterIsNotNull(function0, "onCancel");
        ControlAction lastAction = controlViewHolder.getLastAction();
        if (lastAction == null) {
            Log.e("ControlsUiController", "Confirmation Dialog attempted but no last action is set. Will not show");
            return null;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(controlViewHolder.getContext(), 16974545);
        builder.setTitle(controlViewHolder.getContext().getResources().getString(C0021R$string.controls_confirmation_message, controlViewHolder.getTitle().getText()));
        builder.setPositiveButton(17039370, new ChallengeDialogs$createConfirmationDialog$$inlined$apply$lambda$1(controlViewHolder, lastAction, function0));
        builder.setNegativeButton(17039360, new ChallengeDialogs$createConfirmationDialog$$inlined$apply$lambda$2(controlViewHolder, lastAction, function0));
        AlertDialog create = builder.create();
        create.getWindow().setType(2020);
        return create;
    }

    /* access modifiers changed from: private */
    public final void setInputType(EditText editText, boolean z) {
        if (z) {
            editText.setInputType(129);
        } else {
            editText.setInputType(18);
        }
    }

    /* access modifiers changed from: private */
    public final ControlAction addChallengeValue(ControlAction controlAction, String str) {
        String templateId = controlAction.getTemplateId();
        if (controlAction instanceof BooleanAction) {
            return new BooleanAction(templateId, ((BooleanAction) controlAction).getNewState(), str);
        }
        if (controlAction instanceof FloatAction) {
            return new FloatAction(templateId, ((FloatAction) controlAction).getNewValue(), str);
        }
        if (controlAction instanceof CommandAction) {
            return new CommandAction(templateId, str);
        }
        if (controlAction instanceof ModeAction) {
            return new ModeAction(templateId, ((ModeAction) controlAction).getNewMode(), str);
        }
        throw new IllegalStateException("'action' is not a known type: " + controlAction);
    }
}
