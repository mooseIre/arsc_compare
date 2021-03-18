package com.android.systemui.controls.ui;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.Icon;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.StateListDrawable;
import android.service.controls.Control;
import android.service.controls.actions.ControlAction;
import android.service.controls.templates.ControlTemplate;
import android.service.controls.templates.RangeTemplate;
import android.service.controls.templates.StatelessTemplate;
import android.service.controls.templates.TemperatureControlTemplate;
import android.service.controls.templates.ToggleRangeTemplate;
import android.service.controls.templates.ToggleTemplate;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.internal.graphics.ColorUtils;
import com.android.systemui.C0011R$color;
import com.android.systemui.C0014R$fraction;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0021R$string;
import com.android.systemui.Interpolators;
import com.android.systemui.controls.controller.ControlsController;
import com.android.systemui.controls.ui.RenderInfo;
import com.android.systemui.util.concurrency.DelayableExecutor;
import java.util.List;
import java.util.Set;
import kotlin.TypeCastException;
import kotlin.Unit;
import kotlin.collections.CollectionsKt__CollectionsKt;
import kotlin.collections.SetsKt__SetsKt;
import kotlin.jvm.JvmClassMappingKt;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Ref$IntRef;
import kotlin.jvm.internal.Reflection;
import kotlin.reflect.KClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: ControlViewHolder.kt */
public final class ControlViewHolder {
    private static final int[] ATTR_DISABLED = {-16842910};
    private static final int[] ATTR_ENABLED = {16842910};
    public static final Companion Companion = new Companion(null);
    private static final Set<Integer> FORCE_PANEL_DEVICES = SetsKt__SetsKt.setOf((Object[]) new Integer[]{49, 50});
    private final GradientDrawable baseLayer;
    @Nullable
    private Behavior behavior;
    @NotNull
    private final ClipDrawable clipLayer;
    @NotNull
    private final Context context;
    @NotNull
    private final ControlActionCoordinator controlActionCoordinator;
    @NotNull
    private final ControlsController controlsController;
    @NotNull
    public ControlWithState cws;
    @NotNull
    private final ImageView icon;
    private boolean isLoading;
    @Nullable
    private ControlAction lastAction;
    private Dialog lastChallengeDialog;
    @NotNull
    private final ViewGroup layout;
    private CharSequence nextStatusText = "";
    private final Function0<Unit> onDialogCancel;
    private ValueAnimator stateAnimator;
    private final TextView status;
    private Animator statusAnimator;
    @NotNull
    private final TextView subtitle;
    @NotNull
    private final TextView title;
    private final float toggleBackgroundIntensity;
    @NotNull
    private final DelayableExecutor uiExecutor;
    private boolean userInteractionInProgress;
    @Nullable
    private Dialog visibleDialog;

    public ControlViewHolder(@NotNull ViewGroup viewGroup, @NotNull ControlsController controlsController2, @NotNull DelayableExecutor delayableExecutor, @NotNull DelayableExecutor delayableExecutor2, @NotNull ControlActionCoordinator controlActionCoordinator2) {
        Intrinsics.checkParameterIsNotNull(viewGroup, "layout");
        Intrinsics.checkParameterIsNotNull(controlsController2, "controlsController");
        Intrinsics.checkParameterIsNotNull(delayableExecutor, "uiExecutor");
        Intrinsics.checkParameterIsNotNull(delayableExecutor2, "bgExecutor");
        Intrinsics.checkParameterIsNotNull(controlActionCoordinator2, "controlActionCoordinator");
        this.layout = viewGroup;
        this.controlsController = controlsController2;
        this.uiExecutor = delayableExecutor;
        this.controlActionCoordinator = controlActionCoordinator2;
        Context context2 = viewGroup.getContext();
        Intrinsics.checkExpressionValueIsNotNull(context2, "layout.context");
        this.toggleBackgroundIntensity = context2.getResources().getFraction(C0014R$fraction.controls_toggle_bg_intensity, 1, 1);
        View requireViewById = this.layout.requireViewById(C0015R$id.icon);
        Intrinsics.checkExpressionValueIsNotNull(requireViewById, "layout.requireViewById(R.id.icon)");
        this.icon = (ImageView) requireViewById;
        View requireViewById2 = this.layout.requireViewById(C0015R$id.status);
        Intrinsics.checkExpressionValueIsNotNull(requireViewById2, "layout.requireViewById(R.id.status)");
        this.status = (TextView) requireViewById2;
        View requireViewById3 = this.layout.requireViewById(C0015R$id.title);
        Intrinsics.checkExpressionValueIsNotNull(requireViewById3, "layout.requireViewById(R.id.title)");
        this.title = (TextView) requireViewById3;
        View requireViewById4 = this.layout.requireViewById(C0015R$id.subtitle);
        Intrinsics.checkExpressionValueIsNotNull(requireViewById4, "layout.requireViewById(R.id.subtitle)");
        this.subtitle = (TextView) requireViewById4;
        Context context3 = this.layout.getContext();
        Intrinsics.checkExpressionValueIsNotNull(context3, "layout.getContext()");
        this.context = context3;
        this.onDialogCancel = new ControlViewHolder$onDialogCancel$1(this);
        Drawable background = this.layout.getBackground();
        if (background != null) {
            LayerDrawable layerDrawable = (LayerDrawable) background;
            layerDrawable.mutate();
            Drawable findDrawableByLayerId = layerDrawable.findDrawableByLayerId(C0015R$id.clip_layer);
            if (findDrawableByLayerId != null) {
                ClipDrawable clipDrawable = (ClipDrawable) findDrawableByLayerId;
                this.clipLayer = clipDrawable;
                clipDrawable.setAlpha(0);
                Drawable findDrawableByLayerId2 = layerDrawable.findDrawableByLayerId(C0015R$id.background);
                if (findDrawableByLayerId2 != null) {
                    this.baseLayer = (GradientDrawable) findDrawableByLayerId2;
                    this.status.setSelected(true);
                    return;
                }
                throw new TypeCastException("null cannot be cast to non-null type android.graphics.drawable.GradientDrawable");
            }
            throw new TypeCastException("null cannot be cast to non-null type android.graphics.drawable.ClipDrawable");
        }
        throw new TypeCastException("null cannot be cast to non-null type android.graphics.drawable.LayerDrawable");
    }

    @NotNull
    public final ViewGroup getLayout() {
        return this.layout;
    }

    @NotNull
    public final DelayableExecutor getUiExecutor() {
        return this.uiExecutor;
    }

    @NotNull
    public final ControlActionCoordinator getControlActionCoordinator() {
        return this.controlActionCoordinator;
    }

    /* compiled from: ControlViewHolder.kt */
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        @NotNull
        public final KClass<? extends Behavior> findBehaviorClass(int i, @NotNull ControlTemplate controlTemplate, int i2) {
            Intrinsics.checkParameterIsNotNull(controlTemplate, "template");
            if (i != 1) {
                return Reflection.getOrCreateKotlinClass(StatusBehavior.class);
            }
            if (i2 == 50) {
                return Reflection.getOrCreateKotlinClass(TouchBehavior.class);
            }
            if (Intrinsics.areEqual(controlTemplate, ControlTemplate.NO_TEMPLATE)) {
                return Reflection.getOrCreateKotlinClass(TouchBehavior.class);
            }
            if (controlTemplate instanceof ToggleTemplate) {
                return Reflection.getOrCreateKotlinClass(ToggleBehavior.class);
            }
            if (controlTemplate instanceof StatelessTemplate) {
                return Reflection.getOrCreateKotlinClass(TouchBehavior.class);
            }
            if (controlTemplate instanceof ToggleRangeTemplate) {
                return Reflection.getOrCreateKotlinClass(ToggleRangeBehavior.class);
            }
            if (controlTemplate instanceof RangeTemplate) {
                return Reflection.getOrCreateKotlinClass(ToggleRangeBehavior.class);
            }
            if (controlTemplate instanceof TemperatureControlTemplate) {
                return Reflection.getOrCreateKotlinClass(TemperatureControlBehavior.class);
            }
            return Reflection.getOrCreateKotlinClass(DefaultBehavior.class);
        }
    }

    @NotNull
    public final TextView getTitle() {
        return this.title;
    }

    @NotNull
    public final Context getContext() {
        return this.context;
    }

    @NotNull
    public final ClipDrawable getClipLayer() {
        return this.clipLayer;
    }

    @NotNull
    public final ControlWithState getCws() {
        ControlWithState controlWithState = this.cws;
        if (controlWithState != null) {
            return controlWithState;
        }
        Intrinsics.throwUninitializedPropertyAccessException("cws");
        throw null;
    }

    @Nullable
    public final ControlAction getLastAction() {
        return this.lastAction;
    }

    public final void setLoading(boolean z) {
        this.isLoading = z;
    }

    public final void setVisibleDialog(@Nullable Dialog dialog) {
        this.visibleDialog = dialog;
    }

    public final int getDeviceType() {
        ControlWithState controlWithState = this.cws;
        if (controlWithState != null) {
            Control control = controlWithState.getControl();
            if (control != null) {
                return control.getDeviceType();
            }
            ControlWithState controlWithState2 = this.cws;
            if (controlWithState2 != null) {
                return controlWithState2.getCi().getDeviceType();
            }
            Intrinsics.throwUninitializedPropertyAccessException("cws");
            throw null;
        }
        Intrinsics.throwUninitializedPropertyAccessException("cws");
        throw null;
    }

    public final int getControlStatus() {
        ControlWithState controlWithState = this.cws;
        if (controlWithState != null) {
            Control control = controlWithState.getControl();
            if (control != null) {
                return control.getStatus();
            }
            return 0;
        }
        Intrinsics.throwUninitializedPropertyAccessException("cws");
        throw null;
    }

    @NotNull
    public final ControlTemplate getControlTemplate() {
        ControlTemplate controlTemplate;
        ControlWithState controlWithState = this.cws;
        if (controlWithState != null) {
            Control control = controlWithState.getControl();
            if (control != null && (controlTemplate = control.getControlTemplate()) != null) {
                return controlTemplate;
            }
            ControlTemplate controlTemplate2 = ControlTemplate.NO_TEMPLATE;
            Intrinsics.checkExpressionValueIsNotNull(controlTemplate2, "ControlTemplate.NO_TEMPLATE");
            return controlTemplate2;
        }
        Intrinsics.throwUninitializedPropertyAccessException("cws");
        throw null;
    }

    public final void setUserInteractionInProgress(boolean z) {
        this.userInteractionInProgress = z;
    }

    public final void bindData(@NotNull ControlWithState controlWithState) {
        Intrinsics.checkParameterIsNotNull(controlWithState, "cws");
        if (!this.userInteractionInProgress) {
            this.cws = controlWithState;
            if (getControlStatus() == 0 || getControlStatus() == 2) {
                this.title.setText(controlWithState.getCi().getControlTitle());
                this.subtitle.setText(controlWithState.getCi().getControlSubtitle());
            } else {
                Control control = controlWithState.getControl();
                if (control != null) {
                    this.title.setText(control.getTitle());
                    this.subtitle.setText(control.getSubtitle());
                }
            }
            if (controlWithState.getControl() != null) {
                this.layout.setClickable(true);
                this.layout.setOnLongClickListener(new ControlViewHolder$bindData$$inlined$let$lambda$1(this, controlWithState));
                this.controlActionCoordinator.runPendingAction(controlWithState.getCi().getControlId());
            }
            this.isLoading = false;
            this.behavior = bindBehavior$default(this, this.behavior, Companion.findBehaviorClass(getControlStatus(), getControlTemplate(), getDeviceType()), 0, 4, null);
            updateContentDescription();
        }
    }

    public final void actionResponse(int i) {
        ControlActionCoordinator controlActionCoordinator2 = this.controlActionCoordinator;
        ControlWithState controlWithState = this.cws;
        if (controlWithState != null) {
            controlActionCoordinator2.enableActionOnTouch(controlWithState.getCi().getControlId());
            boolean z = this.lastChallengeDialog != null;
            if (i == 0) {
                this.lastChallengeDialog = null;
                setErrorStatus();
            } else if (i == 1) {
                this.lastChallengeDialog = null;
            } else if (i == 2) {
                this.lastChallengeDialog = null;
                setErrorStatus();
            } else if (i == 3) {
                Dialog createConfirmationDialog = ChallengeDialogs.INSTANCE.createConfirmationDialog(this, this.onDialogCancel);
                this.lastChallengeDialog = createConfirmationDialog;
                if (createConfirmationDialog != null) {
                    createConfirmationDialog.show();
                }
            } else if (i == 4) {
                Dialog createPinDialog = ChallengeDialogs.INSTANCE.createPinDialog(this, false, z, this.onDialogCancel);
                this.lastChallengeDialog = createPinDialog;
                if (createPinDialog != null) {
                    createPinDialog.show();
                }
            } else if (i == 5) {
                Dialog createPinDialog2 = ChallengeDialogs.INSTANCE.createPinDialog(this, true, z, this.onDialogCancel);
                this.lastChallengeDialog = createPinDialog2;
                if (createPinDialog2 != null) {
                    createPinDialog2.show();
                }
            }
        } else {
            Intrinsics.throwUninitializedPropertyAccessException("cws");
            throw null;
        }
    }

    public final void dismiss() {
        Dialog dialog = this.lastChallengeDialog;
        if (dialog != null) {
            dialog.dismiss();
        }
        this.lastChallengeDialog = null;
        Dialog dialog2 = this.visibleDialog;
        if (dialog2 != null) {
            dialog2.dismiss();
        }
        this.visibleDialog = null;
    }

    public final void setErrorStatus() {
        animateStatusChange(true, new ControlViewHolder$setErrorStatus$1(this, this.context.getResources().getString(C0021R$string.controls_error_failed)));
    }

    private final void updateContentDescription() {
        ViewGroup viewGroup = this.layout;
        StringBuilder sb = new StringBuilder();
        sb.append(this.title.getText());
        sb.append(' ');
        sb.append(this.subtitle.getText());
        sb.append(' ');
        sb.append(this.status.getText());
        viewGroup.setContentDescription(sb.toString());
    }

    public final void action(@NotNull ControlAction controlAction) {
        Intrinsics.checkParameterIsNotNull(controlAction, "action");
        this.lastAction = controlAction;
        ControlsController controlsController2 = this.controlsController;
        ControlWithState controlWithState = this.cws;
        if (controlWithState != null) {
            ComponentName componentName = controlWithState.getComponentName();
            ControlWithState controlWithState2 = this.cws;
            if (controlWithState2 != null) {
                controlsController2.action(componentName, controlWithState2.getCi(), controlAction);
            } else {
                Intrinsics.throwUninitializedPropertyAccessException("cws");
                throw null;
            }
        } else {
            Intrinsics.throwUninitializedPropertyAccessException("cws");
            throw null;
        }
    }

    public final boolean usePanel() {
        return FORCE_PANEL_DEVICES.contains(Integer.valueOf(getDeviceType())) || Intrinsics.areEqual(getControlTemplate(), ControlTemplate.NO_TEMPLATE);
    }

    public static /* synthetic */ Behavior bindBehavior$default(ControlViewHolder controlViewHolder, Behavior behavior2, KClass kClass, int i, int i2, Object obj) {
        if ((i2 & 4) != 0) {
            i = 0;
        }
        return controlViewHolder.bindBehavior(behavior2, kClass, i);
    }

    @NotNull
    public final Behavior bindBehavior(@Nullable Behavior behavior2, @NotNull KClass<? extends Behavior> kClass, int i) {
        Intrinsics.checkParameterIsNotNull(kClass, "clazz");
        if (behavior2 == null || (!Intrinsics.areEqual(Reflection.getOrCreateKotlinClass(behavior2.getClass()), kClass))) {
            behavior2 = (Behavior) JvmClassMappingKt.getJavaClass(kClass).newInstance();
            behavior2.initialize(this);
            this.layout.setAccessibilityDelegate(null);
        }
        ControlWithState controlWithState = this.cws;
        if (controlWithState != null) {
            behavior2.bind(controlWithState, i);
            Intrinsics.checkExpressionValueIsNotNull(behavior2, "behavior.also {\n        …nd(cws, offset)\n        }");
            return behavior2;
        }
        Intrinsics.throwUninitializedPropertyAccessException("cws");
        throw null;
    }

    public static /* synthetic */ void applyRenderInfo$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core$default(ControlViewHolder controlViewHolder, boolean z, int i, boolean z2, int i2, Object obj) {
        if ((i2 & 4) != 0) {
            z2 = true;
        }
        controlViewHolder.applyRenderInfo$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core(z, i, z2);
    }

    public final void applyRenderInfo$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core(boolean z, int i, boolean z2) {
        int i2;
        if (getControlStatus() == 1 || getControlStatus() == 0) {
            i2 = getDeviceType();
        } else {
            i2 = -1000;
        }
        RenderInfo.Companion companion = RenderInfo.Companion;
        Context context2 = this.context;
        ControlWithState controlWithState = this.cws;
        if (controlWithState != null) {
            RenderInfo lookup = companion.lookup(context2, controlWithState.getComponentName(), i2, i);
            ColorStateList colorStateList = this.context.getResources().getColorStateList(lookup.getForeground(), this.context.getTheme());
            CharSequence charSequence = this.nextStatusText;
            ControlWithState controlWithState2 = this.cws;
            if (controlWithState2 != null) {
                Control control = controlWithState2.getControl();
                boolean z3 = Intrinsics.areEqual(charSequence, this.status.getText()) ? false : z2;
                animateStatusChange(z3, new ControlViewHolder$applyRenderInfo$1(this, z, charSequence, lookup, colorStateList, control));
                animateBackgroundChange(z3, z, lookup.getEnabledBackground());
                return;
            }
            Intrinsics.throwUninitializedPropertyAccessException("cws");
            throw null;
        }
        Intrinsics.throwUninitializedPropertyAccessException("cws");
        throw null;
    }

    public final void setStatusTextSize(float f) {
        this.status.setTextSize(0, f);
    }

    public static /* synthetic */ void setStatusText$default(ControlViewHolder controlViewHolder, CharSequence charSequence, boolean z, int i, Object obj) {
        if ((i & 2) != 0) {
            z = false;
        }
        controlViewHolder.setStatusText(charSequence, z);
    }

    public final void setStatusText(@NotNull CharSequence charSequence, boolean z) {
        Intrinsics.checkParameterIsNotNull(charSequence, "text");
        if (z) {
            this.status.setAlpha(1.0f);
            this.status.setText(charSequence);
            updateContentDescription();
        }
        this.nextStatusText = charSequence;
    }

    private final void animateBackgroundChange(boolean z, boolean z2, int i) {
        List list;
        int i2;
        ColorStateList customColor;
        int color = this.context.getResources().getColor(C0011R$color.control_default_background, this.context.getTheme());
        Ref$IntRef ref$IntRef = new Ref$IntRef();
        Ref$IntRef ref$IntRef2 = new Ref$IntRef();
        if (z2) {
            ControlWithState controlWithState = this.cws;
            if (controlWithState != null) {
                Control control = controlWithState.getControl();
                if (control == null || (customColor = control.getCustomColor()) == null) {
                    i2 = this.context.getResources().getColor(i, this.context.getTheme());
                } else {
                    i2 = customColor.getColorForState(new int[]{16842910}, customColor.getDefaultColor());
                }
                list = CollectionsKt__CollectionsKt.listOf((Object[]) new Integer[]{Integer.valueOf(i2), 255});
            } else {
                Intrinsics.throwUninitializedPropertyAccessException("cws");
                throw null;
            }
        } else {
            list = CollectionsKt__CollectionsKt.listOf((Object[]) new Integer[]{Integer.valueOf(this.context.getResources().getColor(C0011R$color.control_default_background, this.context.getTheme())), 0});
        }
        ref$IntRef.element = ((Number) list.get(0)).intValue();
        ref$IntRef2.element = ((Number) list.get(1)).intValue();
        Drawable drawable = this.clipLayer.getDrawable();
        if (drawable != null) {
            GradientDrawable gradientDrawable = (GradientDrawable) drawable;
            int blendARGB = this.behavior instanceof ToggleRangeBehavior ? ColorUtils.blendARGB(color, ref$IntRef.element, this.toggleBackgroundIntensity) : color;
            ValueAnimator valueAnimator = this.stateAnimator;
            if (valueAnimator != null) {
                valueAnimator.cancel();
            }
            if (z) {
                ColorStateList color2 = gradientDrawable.getColor();
                int defaultColor = color2 != null ? color2.getDefaultColor() : ref$IntRef.element;
                ColorStateList color3 = this.baseLayer.getColor();
                int defaultColor2 = color3 != null ? color3.getDefaultColor() : blendARGB;
                float alpha = this.layout.getAlpha();
                ValueAnimator ofInt = ValueAnimator.ofInt(this.clipLayer.getAlpha(), ref$IntRef2.element);
                ofInt.addUpdateListener(new ControlViewHolder$animateBackgroundChange$$inlined$apply$lambda$1(gradientDrawable, defaultColor, defaultColor2, blendARGB, alpha, this, color, ref$IntRef, z, ref$IntRef2));
                ofInt.addListener(new ControlViewHolder$animateBackgroundChange$$inlined$apply$lambda$2(gradientDrawable, defaultColor, defaultColor2, blendARGB, alpha, this, color, ref$IntRef, z, ref$IntRef2));
                ofInt.setDuration(700L);
                ofInt.setInterpolator(Interpolators.CONTROL_STATE);
                ofInt.start();
                this.stateAnimator = ofInt;
                return;
            }
            gradientDrawable.setAlpha(ref$IntRef2.element);
            gradientDrawable.setColor(ref$IntRef.element);
            this.baseLayer.setColor(blendARGB);
            this.layout.setAlpha(1.0f);
            return;
        }
        throw new TypeCastException("null cannot be cast to non-null type android.graphics.drawable.GradientDrawable");
    }

    private final void animateStatusChange(boolean z, Function0<Unit> function0) {
        Animator animator = this.statusAnimator;
        if (animator != null) {
            animator.cancel();
        }
        if (!z) {
            function0.invoke();
        } else if (this.isLoading) {
            function0.invoke();
            ObjectAnimator ofFloat = ObjectAnimator.ofFloat(this.status, "alpha", 0.45f);
            ofFloat.setRepeatMode(2);
            ofFloat.setRepeatCount(-1);
            ofFloat.setDuration(500L);
            ofFloat.setInterpolator(Interpolators.LINEAR);
            ofFloat.setStartDelay(900);
            ofFloat.start();
            this.statusAnimator = ofFloat;
        } else {
            ObjectAnimator ofFloat2 = ObjectAnimator.ofFloat(this.status, "alpha", 0.0f);
            ofFloat2.setDuration(200L);
            ofFloat2.setInterpolator(Interpolators.LINEAR);
            ofFloat2.addListener(new ControlViewHolder$animateStatusChange$$inlined$apply$lambda$1(function0));
            ObjectAnimator ofFloat3 = ObjectAnimator.ofFloat(this.status, "alpha", 1.0f);
            ofFloat3.setDuration(200L);
            ofFloat3.setInterpolator(Interpolators.LINEAR);
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playSequentially(ofFloat2, ofFloat3);
            animatorSet.addListener(new ControlViewHolder$animateStatusChange$$inlined$apply$lambda$2(this, ofFloat2, ofFloat3));
            animatorSet.start();
            this.statusAnimator = animatorSet;
        }
    }

    /* access modifiers changed from: private */
    public final void updateStatusRow(boolean z, CharSequence charSequence, Drawable drawable, ColorStateList colorStateList, Control control) {
        Icon customIcon;
        setEnabled(z);
        this.status.setText(charSequence);
        updateContentDescription();
        this.status.setTextColor(colorStateList);
        if (control == null || (customIcon = control.getCustomIcon()) == null) {
            if (drawable instanceof StateListDrawable) {
                if (this.icon.getDrawable() == null || !(this.icon.getDrawable() instanceof StateListDrawable)) {
                    this.icon.setImageDrawable(drawable);
                }
                this.icon.setImageState(z ? ATTR_ENABLED : ATTR_DISABLED, true);
            } else {
                this.icon.setImageDrawable(drawable);
            }
            if (getDeviceType() != 52) {
                this.icon.setImageTintList(colorStateList);
                return;
            }
            return;
        }
        if (this.icon.getImageTintList() != null) {
            this.icon.setImageTintList(null);
        }
        this.icon.setImageIcon(customIcon);
    }

    private final void setEnabled(boolean z) {
        this.status.setEnabled(z);
        this.icon.setEnabled(z);
    }
}
