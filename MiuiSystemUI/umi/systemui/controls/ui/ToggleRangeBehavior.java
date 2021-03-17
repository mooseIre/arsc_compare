package com.android.systemui.controls.ui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.service.controls.Control;
import android.service.controls.templates.ControlTemplate;
import android.service.controls.templates.RangeTemplate;
import android.service.controls.templates.TemperatureControlTemplate;
import android.service.controls.templates.ToggleRangeTemplate;
import android.util.Log;
import android.util.MathUtils;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0015R$id;
import com.android.systemui.Interpolators;
import java.util.Arrays;
import java.util.IllegalFormatException;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.StringCompanionObject;
import org.jetbrains.annotations.NotNull;

/* compiled from: ToggleRangeBehavior.kt */
public final class ToggleRangeBehavior implements Behavior {
    @NotNull
    public Drawable clipLayer;
    private int colorOffset;
    @NotNull
    public Context context;
    @NotNull
    public Control control;
    @NotNull
    private String currentRangeValue = "";
    @NotNull
    private CharSequence currentStatusText = "";
    @NotNull
    public ControlViewHolder cvh;
    private boolean isChecked;
    private boolean isToggleable;
    private ValueAnimator rangeAnimator;
    @NotNull
    public RangeTemplate rangeTemplate;
    @NotNull
    public String templateId;

    @NotNull
    public final Drawable getClipLayer() {
        Drawable drawable = this.clipLayer;
        if (drawable != null) {
            return drawable;
        }
        Intrinsics.throwUninitializedPropertyAccessException("clipLayer");
        throw null;
    }

    @NotNull
    public final String getTemplateId() {
        String str = this.templateId;
        if (str != null) {
            return str;
        }
        Intrinsics.throwUninitializedPropertyAccessException("templateId");
        throw null;
    }

    @NotNull
    public final ControlViewHolder getCvh() {
        ControlViewHolder controlViewHolder = this.cvh;
        if (controlViewHolder != null) {
            return controlViewHolder;
        }
        Intrinsics.throwUninitializedPropertyAccessException("cvh");
        throw null;
    }

    @NotNull
    public final RangeTemplate getRangeTemplate() {
        RangeTemplate rangeTemplate2 = this.rangeTemplate;
        if (rangeTemplate2 != null) {
            return rangeTemplate2;
        }
        Intrinsics.throwUninitializedPropertyAccessException("rangeTemplate");
        throw null;
    }

    public final boolean isChecked() {
        return this.isChecked;
    }

    public final boolean isToggleable() {
        return this.isToggleable;
    }

    @Override // com.android.systemui.controls.ui.Behavior
    public void initialize(@NotNull ControlViewHolder controlViewHolder) {
        Intrinsics.checkParameterIsNotNull(controlViewHolder, "cvh");
        this.cvh = controlViewHolder;
        this.context = controlViewHolder.getContext();
        ToggleRangeGestureListener toggleRangeGestureListener = new ToggleRangeGestureListener(this, controlViewHolder.getLayout());
        Context context2 = this.context;
        if (context2 != null) {
            controlViewHolder.getLayout().setOnTouchListener(new ToggleRangeBehavior$initialize$1(this, new GestureDetector(context2, toggleRangeGestureListener), toggleRangeGestureListener));
            return;
        }
        Intrinsics.throwUninitializedPropertyAccessException("context");
        throw null;
    }

    private final void setup(ToggleRangeTemplate toggleRangeTemplate) {
        RangeTemplate range = toggleRangeTemplate.getRange();
        Intrinsics.checkExpressionValueIsNotNull(range, "template.getRange()");
        this.rangeTemplate = range;
        this.isToggleable = true;
        this.isChecked = toggleRangeTemplate.isChecked();
    }

    private final void setup(RangeTemplate rangeTemplate2) {
        this.rangeTemplate = rangeTemplate2;
        if (rangeTemplate2 != null) {
            float currentValue = rangeTemplate2.getCurrentValue();
            RangeTemplate rangeTemplate3 = this.rangeTemplate;
            if (rangeTemplate3 != null) {
                this.isChecked = currentValue != rangeTemplate3.getMinValue();
            } else {
                Intrinsics.throwUninitializedPropertyAccessException("rangeTemplate");
                throw null;
            }
        } else {
            Intrinsics.throwUninitializedPropertyAccessException("rangeTemplate");
            throw null;
        }
    }

    private final boolean setupTemplate(ControlTemplate controlTemplate) {
        if (controlTemplate instanceof ToggleRangeTemplate) {
            setup((ToggleRangeTemplate) controlTemplate);
            return true;
        } else if (controlTemplate instanceof RangeTemplate) {
            setup((RangeTemplate) controlTemplate);
            return true;
        } else if (controlTemplate instanceof TemperatureControlTemplate) {
            ControlTemplate template = ((TemperatureControlTemplate) controlTemplate).getTemplate();
            Intrinsics.checkExpressionValueIsNotNull(template, "template.getTemplate()");
            return setupTemplate(template);
        } else {
            Log.e("ControlsUiController", "Unsupported template type: " + controlTemplate);
            return false;
        }
    }

    @Override // com.android.systemui.controls.ui.Behavior
    public void bind(@NotNull ControlWithState controlWithState, int i) {
        Intrinsics.checkParameterIsNotNull(controlWithState, "cws");
        Control control2 = controlWithState.getControl();
        if (control2 != null) {
            this.control = control2;
            this.colorOffset = i;
            if (control2 != null) {
                CharSequence statusText = control2.getStatusText();
                Intrinsics.checkExpressionValueIsNotNull(statusText, "control.getStatusText()");
                this.currentStatusText = statusText;
                ControlViewHolder controlViewHolder = this.cvh;
                if (controlViewHolder != null) {
                    controlViewHolder.getLayout().setOnLongClickListener(null);
                    ControlViewHolder controlViewHolder2 = this.cvh;
                    if (controlViewHolder2 != null) {
                        Drawable background = controlViewHolder2.getLayout().getBackground();
                        if (background != null) {
                            Drawable findDrawableByLayerId = ((LayerDrawable) background).findDrawableByLayerId(C0015R$id.clip_layer);
                            Intrinsics.checkExpressionValueIsNotNull(findDrawableByLayerId, "ld.findDrawableByLayerId(R.id.clip_layer)");
                            this.clipLayer = findDrawableByLayerId;
                            Control control3 = this.control;
                            if (control3 != null) {
                                ControlTemplate controlTemplate = control3.getControlTemplate();
                                Intrinsics.checkExpressionValueIsNotNull(controlTemplate, "template");
                                if (setupTemplate(controlTemplate)) {
                                    String templateId2 = controlTemplate.getTemplateId();
                                    Intrinsics.checkExpressionValueIsNotNull(templateId2, "template.getTemplateId()");
                                    this.templateId = templateId2;
                                    RangeTemplate rangeTemplate2 = this.rangeTemplate;
                                    if (rangeTemplate2 != null) {
                                        updateRange(rangeToLevelValue(rangeTemplate2.getCurrentValue()), this.isChecked, false);
                                        ControlViewHolder controlViewHolder3 = this.cvh;
                                        if (controlViewHolder3 != null) {
                                            ControlViewHolder.applyRenderInfo$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core$default(controlViewHolder3, this.isChecked, i, false, 4, null);
                                            ControlViewHolder controlViewHolder4 = this.cvh;
                                            if (controlViewHolder4 != null) {
                                                controlViewHolder4.getLayout().setAccessibilityDelegate(new ToggleRangeBehavior$bind$1(this));
                                            } else {
                                                Intrinsics.throwUninitializedPropertyAccessException("cvh");
                                                throw null;
                                            }
                                        } else {
                                            Intrinsics.throwUninitializedPropertyAccessException("cvh");
                                            throw null;
                                        }
                                    } else {
                                        Intrinsics.throwUninitializedPropertyAccessException("rangeTemplate");
                                        throw null;
                                    }
                                }
                            } else {
                                Intrinsics.throwUninitializedPropertyAccessException("control");
                                throw null;
                            }
                        } else {
                            throw new TypeCastException("null cannot be cast to non-null type android.graphics.drawable.LayerDrawable");
                        }
                    } else {
                        Intrinsics.throwUninitializedPropertyAccessException("cvh");
                        throw null;
                    }
                } else {
                    Intrinsics.throwUninitializedPropertyAccessException("cvh");
                    throw null;
                }
            } else {
                Intrinsics.throwUninitializedPropertyAccessException("control");
                throw null;
            }
        } else {
            Intrinsics.throwNpe();
            throw null;
        }
    }

    public final void beginUpdateRange() {
        ControlViewHolder controlViewHolder = this.cvh;
        if (controlViewHolder != null) {
            controlViewHolder.setUserInteractionInProgress(true);
            ControlViewHolder controlViewHolder2 = this.cvh;
            if (controlViewHolder2 != null) {
                Context context2 = this.context;
                if (context2 != null) {
                    controlViewHolder2.setStatusTextSize((float) context2.getResources().getDimensionPixelSize(C0012R$dimen.control_status_expanded));
                } else {
                    Intrinsics.throwUninitializedPropertyAccessException("context");
                    throw null;
                }
            } else {
                Intrinsics.throwUninitializedPropertyAccessException("cvh");
                throw null;
            }
        } else {
            Intrinsics.throwUninitializedPropertyAccessException("cvh");
            throw null;
        }
    }

    public final void updateRange(int i, boolean z, boolean z2) {
        int max = Math.max(0, Math.min(10000, i));
        Drawable drawable = this.clipLayer;
        if (drawable != null) {
            if (drawable.getLevel() == 0 && max > 0) {
                ControlViewHolder controlViewHolder = this.cvh;
                if (controlViewHolder != null) {
                    controlViewHolder.applyRenderInfo$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core(z, this.colorOffset, false);
                } else {
                    Intrinsics.throwUninitializedPropertyAccessException("cvh");
                    throw null;
                }
            }
            ValueAnimator valueAnimator = this.rangeAnimator;
            if (valueAnimator != null) {
                valueAnimator.cancel();
            }
            if (z2) {
                boolean z3 = max == 0 || max == 10000;
                Drawable drawable2 = this.clipLayer;
                if (drawable2 == null) {
                    Intrinsics.throwUninitializedPropertyAccessException("clipLayer");
                    throw null;
                } else if (drawable2.getLevel() != max) {
                    ControlViewHolder controlViewHolder2 = this.cvh;
                    if (controlViewHolder2 != null) {
                        controlViewHolder2.getControlActionCoordinator().drag(z3);
                        Drawable drawable3 = this.clipLayer;
                        if (drawable3 != null) {
                            drawable3.setLevel(max);
                        } else {
                            Intrinsics.throwUninitializedPropertyAccessException("clipLayer");
                            throw null;
                        }
                    } else {
                        Intrinsics.throwUninitializedPropertyAccessException("cvh");
                        throw null;
                    }
                }
            } else {
                Drawable drawable4 = this.clipLayer;
                if (drawable4 == null) {
                    Intrinsics.throwUninitializedPropertyAccessException("clipLayer");
                    throw null;
                } else if (max != drawable4.getLevel()) {
                    int[] iArr = new int[2];
                    ControlViewHolder controlViewHolder3 = this.cvh;
                    if (controlViewHolder3 != null) {
                        iArr[0] = controlViewHolder3.getClipLayer().getLevel();
                        iArr[1] = max;
                        ValueAnimator ofInt = ValueAnimator.ofInt(iArr);
                        ofInt.addUpdateListener(new ToggleRangeBehavior$updateRange$$inlined$apply$lambda$1(this));
                        ofInt.addListener(new ToggleRangeBehavior$updateRange$$inlined$apply$lambda$2(this));
                        ofInt.setDuration(700L);
                        ofInt.setInterpolator(Interpolators.CONTROL_STATE);
                        ofInt.start();
                        this.rangeAnimator = ofInt;
                    } else {
                        Intrinsics.throwUninitializedPropertyAccessException("cvh");
                        throw null;
                    }
                }
            }
            if (z) {
                float levelToRangeValue = levelToRangeValue(max);
                RangeTemplate rangeTemplate2 = this.rangeTemplate;
                if (rangeTemplate2 != null) {
                    String format = format(rangeTemplate2.getFormatString().toString(), "%.1f", levelToRangeValue);
                    this.currentRangeValue = format;
                    if (z2) {
                        ControlViewHolder controlViewHolder4 = this.cvh;
                        if (controlViewHolder4 != null) {
                            controlViewHolder4.setStatusText(format, true);
                        } else {
                            Intrinsics.throwUninitializedPropertyAccessException("cvh");
                            throw null;
                        }
                    } else {
                        ControlViewHolder controlViewHolder5 = this.cvh;
                        if (controlViewHolder5 != null) {
                            ControlViewHolder.setStatusText$default(controlViewHolder5, this.currentStatusText + ' ' + this.currentRangeValue, false, 2, null);
                            return;
                        }
                        Intrinsics.throwUninitializedPropertyAccessException("cvh");
                        throw null;
                    }
                } else {
                    Intrinsics.throwUninitializedPropertyAccessException("rangeTemplate");
                    throw null;
                }
            } else {
                ControlViewHolder controlViewHolder6 = this.cvh;
                if (controlViewHolder6 != null) {
                    ControlViewHolder.setStatusText$default(controlViewHolder6, this.currentStatusText, false, 2, null);
                } else {
                    Intrinsics.throwUninitializedPropertyAccessException("cvh");
                    throw null;
                }
            }
        } else {
            Intrinsics.throwUninitializedPropertyAccessException("clipLayer");
            throw null;
        }
    }

    private final String format(String str, String str2, float f) {
        try {
            StringCompanionObject stringCompanionObject = StringCompanionObject.INSTANCE;
            String format = String.format(str, Arrays.copyOf(new Object[]{Float.valueOf(findNearestStep(f))}, 1));
            Intrinsics.checkExpressionValueIsNotNull(format, "java.lang.String.format(format, *args)");
            return format;
        } catch (IllegalFormatException e) {
            Log.w("ControlsUiController", "Illegal format in range template", e);
            if (Intrinsics.areEqual(str2, "")) {
                return "";
            }
            return format(str2, "", f);
        }
    }

    /* access modifiers changed from: private */
    public final float levelToRangeValue(int i) {
        RangeTemplate rangeTemplate2 = this.rangeTemplate;
        if (rangeTemplate2 != null) {
            float minValue = rangeTemplate2.getMinValue();
            RangeTemplate rangeTemplate3 = this.rangeTemplate;
            if (rangeTemplate3 != null) {
                return MathUtils.constrainedMap(minValue, rangeTemplate3.getMaxValue(), (float) 0, (float) 10000, (float) i);
            }
            Intrinsics.throwUninitializedPropertyAccessException("rangeTemplate");
            throw null;
        }
        Intrinsics.throwUninitializedPropertyAccessException("rangeTemplate");
        throw null;
    }

    /* access modifiers changed from: private */
    public final int rangeToLevelValue(float f) {
        float f2 = (float) 0;
        float f3 = (float) 10000;
        RangeTemplate rangeTemplate2 = this.rangeTemplate;
        if (rangeTemplate2 != null) {
            float minValue = rangeTemplate2.getMinValue();
            RangeTemplate rangeTemplate3 = this.rangeTemplate;
            if (rangeTemplate3 != null) {
                return (int) MathUtils.constrainedMap(f2, f3, minValue, rangeTemplate3.getMaxValue(), f);
            }
            Intrinsics.throwUninitializedPropertyAccessException("rangeTemplate");
            throw null;
        }
        Intrinsics.throwUninitializedPropertyAccessException("rangeTemplate");
        throw null;
    }

    public final void endUpdateRange() {
        ControlViewHolder controlViewHolder = this.cvh;
        if (controlViewHolder != null) {
            Context context2 = this.context;
            if (context2 != null) {
                controlViewHolder.setStatusTextSize((float) context2.getResources().getDimensionPixelSize(C0012R$dimen.control_status_normal));
                ControlViewHolder controlViewHolder2 = this.cvh;
                if (controlViewHolder2 != null) {
                    controlViewHolder2.setStatusText(this.currentStatusText + ' ' + this.currentRangeValue, true);
                    ControlViewHolder controlViewHolder3 = this.cvh;
                    if (controlViewHolder3 != null) {
                        ControlActionCoordinator controlActionCoordinator = controlViewHolder3.getControlActionCoordinator();
                        ControlViewHolder controlViewHolder4 = this.cvh;
                        if (controlViewHolder4 != null) {
                            RangeTemplate rangeTemplate2 = this.rangeTemplate;
                            if (rangeTemplate2 != null) {
                                String templateId2 = rangeTemplate2.getTemplateId();
                                Intrinsics.checkExpressionValueIsNotNull(templateId2, "rangeTemplate.getTemplateId()");
                                Drawable drawable = this.clipLayer;
                                if (drawable != null) {
                                    controlActionCoordinator.setValue(controlViewHolder4, templateId2, findNearestStep(levelToRangeValue(drawable.getLevel())));
                                    ControlViewHolder controlViewHolder5 = this.cvh;
                                    if (controlViewHolder5 != null) {
                                        controlViewHolder5.setUserInteractionInProgress(false);
                                    } else {
                                        Intrinsics.throwUninitializedPropertyAccessException("cvh");
                                        throw null;
                                    }
                                } else {
                                    Intrinsics.throwUninitializedPropertyAccessException("clipLayer");
                                    throw null;
                                }
                            } else {
                                Intrinsics.throwUninitializedPropertyAccessException("rangeTemplate");
                                throw null;
                            }
                        } else {
                            Intrinsics.throwUninitializedPropertyAccessException("cvh");
                            throw null;
                        }
                    } else {
                        Intrinsics.throwUninitializedPropertyAccessException("cvh");
                        throw null;
                    }
                } else {
                    Intrinsics.throwUninitializedPropertyAccessException("cvh");
                    throw null;
                }
            } else {
                Intrinsics.throwUninitializedPropertyAccessException("context");
                throw null;
            }
        } else {
            Intrinsics.throwUninitializedPropertyAccessException("cvh");
            throw null;
        }
    }

    public final float findNearestStep(float f) {
        RangeTemplate rangeTemplate2 = this.rangeTemplate;
        if (rangeTemplate2 != null) {
            float minValue = rangeTemplate2.getMinValue();
            float f2 = 1000.0f;
            while (true) {
                RangeTemplate rangeTemplate3 = this.rangeTemplate;
                if (rangeTemplate3 == null) {
                    Intrinsics.throwUninitializedPropertyAccessException("rangeTemplate");
                    throw null;
                } else if (minValue <= rangeTemplate3.getMaxValue()) {
                    float abs = Math.abs(f - minValue);
                    if (abs < f2) {
                        RangeTemplate rangeTemplate4 = this.rangeTemplate;
                        if (rangeTemplate4 != null) {
                            minValue += rangeTemplate4.getStepValue();
                            f2 = abs;
                        } else {
                            Intrinsics.throwUninitializedPropertyAccessException("rangeTemplate");
                            throw null;
                        }
                    } else {
                        RangeTemplate rangeTemplate5 = this.rangeTemplate;
                        if (rangeTemplate5 != null) {
                            return minValue - rangeTemplate5.getStepValue();
                        }
                        Intrinsics.throwUninitializedPropertyAccessException("rangeTemplate");
                        throw null;
                    }
                } else {
                    RangeTemplate rangeTemplate6 = this.rangeTemplate;
                    if (rangeTemplate6 != null) {
                        return rangeTemplate6.getMaxValue();
                    }
                    Intrinsics.throwUninitializedPropertyAccessException("rangeTemplate");
                    throw null;
                }
            }
        } else {
            Intrinsics.throwUninitializedPropertyAccessException("rangeTemplate");
            throw null;
        }
    }

    /* compiled from: ToggleRangeBehavior.kt */
    public final class ToggleRangeGestureListener extends GestureDetector.SimpleOnGestureListener {
        private boolean isDragging;
        final /* synthetic */ ToggleRangeBehavior this$0;
        @NotNull
        private final View v;

        public boolean onDown(@NotNull MotionEvent motionEvent) {
            Intrinsics.checkParameterIsNotNull(motionEvent, "e");
            return true;
        }

        public ToggleRangeGestureListener(@NotNull ToggleRangeBehavior toggleRangeBehavior, View view) {
            Intrinsics.checkParameterIsNotNull(view, "v");
            this.this$0 = toggleRangeBehavior;
            this.v = view;
        }

        public final boolean isDragging() {
            return this.isDragging;
        }

        public final void setDragging(boolean z) {
            this.isDragging = z;
        }

        public void onLongPress(@NotNull MotionEvent motionEvent) {
            Intrinsics.checkParameterIsNotNull(motionEvent, "e");
            if (!this.isDragging) {
                this.this$0.getCvh().getControlActionCoordinator().longPress(this.this$0.getCvh());
            }
        }

        public boolean onScroll(@NotNull MotionEvent motionEvent, @NotNull MotionEvent motionEvent2, float f, float f2) {
            Intrinsics.checkParameterIsNotNull(motionEvent, "e1");
            Intrinsics.checkParameterIsNotNull(motionEvent2, "e2");
            if (!this.isDragging) {
                this.v.getParent().requestDisallowInterceptTouchEvent(true);
                this.this$0.beginUpdateRange();
                this.isDragging = true;
            }
            ToggleRangeBehavior toggleRangeBehavior = this.this$0;
            toggleRangeBehavior.updateRange(toggleRangeBehavior.getClipLayer().getLevel() + ((int) (((float) 10000) * ((-f) / ((float) this.v.getWidth())))), true, true);
            return true;
        }

        public boolean onSingleTapUp(@NotNull MotionEvent motionEvent) {
            Intrinsics.checkParameterIsNotNull(motionEvent, "e");
            if (!this.this$0.isToggleable()) {
                return false;
            }
            this.this$0.getCvh().getControlActionCoordinator().toggle(this.this$0.getCvh(), this.this$0.getTemplateId(), this.this$0.isChecked());
            return true;
        }
    }
}
