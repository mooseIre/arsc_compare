package com.android.systemui.controls.management;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.service.controls.Control;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0017R$layout;
import com.android.systemui.C0021R$string;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.controls.controller.ControlInfo;
import com.android.systemui.controls.controller.ControlsController;
import com.android.systemui.controls.ui.RenderInfo;
import com.android.systemui.statusbar.phone.SystemUIDialog;
import com.android.systemui.util.LifecycleActivity;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: ControlsRequestDialog.kt */
public class ControlsRequestDialog extends LifecycleActivity implements DialogInterface.OnClickListener, DialogInterface.OnCancelListener {
    private final BroadcastDispatcher broadcastDispatcher;
    private final ControlsRequestDialog$callback$1 callback = new ControlsRequestDialog$callback$1();
    private Control control;
    private ComponentName controlComponent;
    private final ControlsController controller;
    private final ControlsListingController controlsListingController;
    private final ControlsRequestDialog$currentUserTracker$1 currentUserTracker = new ControlsRequestDialog$currentUserTracker$1(this, this.broadcastDispatcher);
    private Dialog dialog;

    public ControlsRequestDialog(@NotNull ControlsController controlsController, @NotNull BroadcastDispatcher broadcastDispatcher2, @NotNull ControlsListingController controlsListingController2) {
        Intrinsics.checkParameterIsNotNull(controlsController, "controller");
        Intrinsics.checkParameterIsNotNull(broadcastDispatcher2, "broadcastDispatcher");
        Intrinsics.checkParameterIsNotNull(controlsListingController2, "controlsListingController");
        this.controller = controlsController;
        this.broadcastDispatcher = broadcastDispatcher2;
        this.controlsListingController = controlsListingController2;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.util.LifecycleActivity
    public void onCreate(@Nullable Bundle bundle) {
        super.onCreate(bundle);
        if (!this.controller.getAvailable()) {
            Log.w("ControlsRequestDialog", "Quick Controls not available for this user ");
            finish();
        }
        this.currentUserTracker.startTracking();
        this.controlsListingController.addCallback(this.callback);
        int intExtra = getIntent().getIntExtra("android.intent.extra.USER_ID", -10000);
        int currentUserId = this.controller.getCurrentUserId();
        if (intExtra != currentUserId) {
            Log.w("ControlsRequestDialog", "Current user (" + currentUserId + ") different from request user (" + intExtra + ')');
            finish();
        }
        ComponentName componentName = (ComponentName) getIntent().getParcelableExtra("android.intent.extra.COMPONENT_NAME");
        if (componentName != null) {
            this.controlComponent = componentName;
            Control parcelableExtra = getIntent().getParcelableExtra("android.service.controls.extra.CONTROL");
            if (parcelableExtra != null) {
                this.control = parcelableExtra;
                return;
            }
            Log.e("ControlsRequestDialog", "Request did not contain control");
            finish();
            return;
        }
        Log.e("ControlsRequestDialog", "Request did not contain componentName");
        finish();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.util.LifecycleActivity
    public void onResume() {
        super.onResume();
        CharSequence verifyComponentAndGetLabel = verifyComponentAndGetLabel();
        if (verifyComponentAndGetLabel == null) {
            StringBuilder sb = new StringBuilder();
            sb.append("The component specified (");
            ComponentName componentName = this.controlComponent;
            if (componentName != null) {
                sb.append(componentName.flattenToString());
                sb.append(' ');
                sb.append("is not a valid ControlsProviderService");
                Log.e("ControlsRequestDialog", sb.toString());
                finish();
                return;
            }
            Intrinsics.throwUninitializedPropertyAccessException("controlComponent");
            throw null;
        }
        if (isCurrentFavorite()) {
            StringBuilder sb2 = new StringBuilder();
            sb2.append("The control ");
            Control control2 = this.control;
            if (control2 != null) {
                sb2.append(control2.getTitle());
                sb2.append(" is already a favorite");
                Log.w("ControlsRequestDialog", sb2.toString());
                finish();
            } else {
                Intrinsics.throwUninitializedPropertyAccessException("control");
                throw null;
            }
        }
        Dialog createDialog = createDialog(verifyComponentAndGetLabel);
        this.dialog = createDialog;
        if (createDialog != null) {
            createDialog.show();
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.util.LifecycleActivity
    public void onDestroy() {
        Dialog dialog2 = this.dialog;
        if (dialog2 != null) {
            dialog2.dismiss();
        }
        this.currentUserTracker.stopTracking();
        this.controlsListingController.removeCallback(this.callback);
        super.onDestroy();
    }

    private final CharSequence verifyComponentAndGetLabel() {
        ControlsListingController controlsListingController2 = this.controlsListingController;
        ComponentName componentName = this.controlComponent;
        if (componentName != null) {
            return controlsListingController2.getAppLabel(componentName);
        }
        Intrinsics.throwUninitializedPropertyAccessException("controlComponent");
        throw null;
    }

    /* JADX WARNING: Removed duplicated region for block: B:29:0x0065 A[SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private final boolean isCurrentFavorite() {
        /*
        // Method dump skipped, instructions count: 108
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.controls.management.ControlsRequestDialog.isCurrentFavorite():boolean");
    }

    @NotNull
    public final Dialog createDialog(@NotNull CharSequence charSequence) {
        Intrinsics.checkParameterIsNotNull(charSequence, "label");
        RenderInfo.Companion companion = RenderInfo.Companion;
        ComponentName componentName = this.controlComponent;
        if (componentName != null) {
            Control control2 = this.control;
            if (control2 != null) {
                RenderInfo lookup$default = RenderInfo.Companion.lookup$default(companion, this, componentName, control2.getDeviceType(), 0, 8, null);
                View inflate = LayoutInflater.from(this).inflate(C0017R$layout.controls_dialog, (ViewGroup) null);
                ImageView imageView = (ImageView) inflate.requireViewById(C0015R$id.icon);
                imageView.setImageDrawable(lookup$default.getIcon());
                Context context = imageView.getContext();
                Intrinsics.checkExpressionValueIsNotNull(context, "context");
                Resources resources = context.getResources();
                int foreground = lookup$default.getForeground();
                Context context2 = imageView.getContext();
                Intrinsics.checkExpressionValueIsNotNull(context2, "context");
                imageView.setImageTintList(resources.getColorStateList(foreground, context2.getTheme()));
                View requireViewById = inflate.requireViewById(C0015R$id.title);
                Intrinsics.checkExpressionValueIsNotNull(requireViewById, "requireViewById<TextView>(R.id.title)");
                TextView textView = (TextView) requireViewById;
                Control control3 = this.control;
                if (control3 != null) {
                    textView.setText(control3.getTitle());
                    View requireViewById2 = inflate.requireViewById(C0015R$id.subtitle);
                    Intrinsics.checkExpressionValueIsNotNull(requireViewById2, "requireViewById<TextView>(R.id.subtitle)");
                    TextView textView2 = (TextView) requireViewById2;
                    Control control4 = this.control;
                    if (control4 != null) {
                        textView2.setText(control4.getSubtitle());
                        View requireViewById3 = inflate.requireViewById(C0015R$id.control);
                        Intrinsics.checkExpressionValueIsNotNull(requireViewById3, "requireViewById<View>(R.id.control)");
                        requireViewById3.setElevation(inflate.getResources().getFloat(C0012R$dimen.control_card_elevation));
                        AlertDialog create = new AlertDialog.Builder(this).setTitle(getString(C0021R$string.controls_dialog_title)).setMessage(getString(C0021R$string.controls_dialog_message, new Object[]{charSequence})).setPositiveButton(C0021R$string.controls_dialog_ok, this).setNegativeButton(17039360, this).setOnCancelListener(this).setView(inflate).create();
                        SystemUIDialog.registerDismissListener(create);
                        create.setCanceledOnTouchOutside(true);
                        Intrinsics.checkExpressionValueIsNotNull(create, "dialog");
                        return create;
                    }
                    Intrinsics.throwUninitializedPropertyAccessException("control");
                    throw null;
                }
                Intrinsics.throwUninitializedPropertyAccessException("control");
                throw null;
            }
            Intrinsics.throwUninitializedPropertyAccessException("control");
            throw null;
        }
        Intrinsics.throwUninitializedPropertyAccessException("controlComponent");
        throw null;
    }

    public void onCancel(@Nullable DialogInterface dialogInterface) {
        finish();
    }

    public void onClick(@Nullable DialogInterface dialogInterface, int i) {
        if (i == -1) {
            ControlsController controlsController = this.controller;
            ComponentName componentName = this.controlComponent;
            if (componentName != null) {
                Control control2 = this.control;
                if (control2 != null) {
                    CharSequence structure = control2.getStructure();
                    if (structure == null) {
                        structure = "";
                    }
                    Control control3 = this.control;
                    if (control3 != null) {
                        String controlId = control3.getControlId();
                        Intrinsics.checkExpressionValueIsNotNull(controlId, "control.controlId");
                        Control control4 = this.control;
                        if (control4 != null) {
                            CharSequence title = control4.getTitle();
                            Intrinsics.checkExpressionValueIsNotNull(title, "control.title");
                            Control control5 = this.control;
                            if (control5 != null) {
                                CharSequence subtitle = control5.getSubtitle();
                                Intrinsics.checkExpressionValueIsNotNull(subtitle, "control.subtitle");
                                Control control6 = this.control;
                                if (control6 != null) {
                                    controlsController.addFavorite(componentName, structure, new ControlInfo(controlId, title, subtitle, control6.getDeviceType()));
                                } else {
                                    Intrinsics.throwUninitializedPropertyAccessException("control");
                                    throw null;
                                }
                            } else {
                                Intrinsics.throwUninitializedPropertyAccessException("control");
                                throw null;
                            }
                        } else {
                            Intrinsics.throwUninitializedPropertyAccessException("control");
                            throw null;
                        }
                    } else {
                        Intrinsics.throwUninitializedPropertyAccessException("control");
                        throw null;
                    }
                } else {
                    Intrinsics.throwUninitializedPropertyAccessException("control");
                    throw null;
                }
            } else {
                Intrinsics.throwUninitializedPropertyAccessException("controlComponent");
                throw null;
            }
        }
        finish();
    }
}
