package com.android.systemui;

import android.animation.ArgbEvaluator;
import android.content.Context;
import android.view.ContextThemeWrapper;
import com.android.settingslib.Utils;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: DualToneHandler.kt */
public final class DualToneHandler {
    private Color darkColor;
    private Color lightColor;

    public DualToneHandler(@NotNull Context context) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        setColorsFromContext(context);
    }

    public final void setColorsFromContext(@NotNull Context context) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(context, Utils.getThemeAttr(context, C0009R$attr.darkIconTheme));
        ContextThemeWrapper contextThemeWrapper2 = new ContextThemeWrapper(context, Utils.getThemeAttr(context, C0009R$attr.lightIconTheme));
        this.darkColor = new Color(Utils.getColorAttrDefaultColor(contextThemeWrapper, C0009R$attr.singleToneColor), Utils.getColorAttrDefaultColor(contextThemeWrapper, C0009R$attr.backgroundColor), Utils.getColorAttrDefaultColor(contextThemeWrapper, C0009R$attr.fillColor));
        this.lightColor = new Color(Utils.getColorAttrDefaultColor(contextThemeWrapper2, C0009R$attr.singleToneColor), Utils.getColorAttrDefaultColor(contextThemeWrapper2, C0009R$attr.backgroundColor), Utils.getColorAttrDefaultColor(contextThemeWrapper2, C0009R$attr.fillColor));
    }

    private final int getColorForDarkIntensity(float f, int i, int i2) {
        Object evaluate = ArgbEvaluator.getInstance().evaluate(f, Integer.valueOf(i), Integer.valueOf(i2));
        if (evaluate != null) {
            return ((Integer) evaluate).intValue();
        }
        throw new TypeCastException("null cannot be cast to non-null type kotlin.Int");
    }

    public final int getSingleColor(float f) {
        Color color = this.lightColor;
        if (color != null) {
            int single = color.getSingle();
            Color color2 = this.darkColor;
            if (color2 != null) {
                return getColorForDarkIntensity(f, single, color2.getSingle());
            }
            Intrinsics.throwUninitializedPropertyAccessException("darkColor");
            throw null;
        }
        Intrinsics.throwUninitializedPropertyAccessException("lightColor");
        throw null;
    }

    public final int getBackgroundColor(float f) {
        Color color = this.lightColor;
        if (color != null) {
            int background = color.getBackground();
            Color color2 = this.darkColor;
            if (color2 != null) {
                return getColorForDarkIntensity(f, background, color2.getBackground());
            }
            Intrinsics.throwUninitializedPropertyAccessException("darkColor");
            throw null;
        }
        Intrinsics.throwUninitializedPropertyAccessException("lightColor");
        throw null;
    }

    public final int getFillColor(float f) {
        Color color = this.lightColor;
        if (color != null) {
            int fill = color.getFill();
            Color color2 = this.darkColor;
            if (color2 != null) {
                return getColorForDarkIntensity(f, fill, color2.getFill());
            }
            Intrinsics.throwUninitializedPropertyAccessException("darkColor");
            throw null;
        }
        Intrinsics.throwUninitializedPropertyAccessException("lightColor");
        throw null;
    }

    /* access modifiers changed from: private */
    /* compiled from: DualToneHandler.kt */
    public static final class Color {
        private final int background;
        private final int fill;
        private final int single;

        public boolean equals(@Nullable Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Color)) {
                return false;
            }
            Color color = (Color) obj;
            return this.single == color.single && this.background == color.background && this.fill == color.fill;
        }

        public int hashCode() {
            return (((Integer.hashCode(this.single) * 31) + Integer.hashCode(this.background)) * 31) + Integer.hashCode(this.fill);
        }

        @NotNull
        public String toString() {
            return "Color(single=" + this.single + ", background=" + this.background + ", fill=" + this.fill + ")";
        }

        public Color(int i, int i2, int i3) {
            this.single = i;
            this.background = i2;
            this.fill = i3;
        }

        public final int getBackground() {
            return this.background;
        }

        public final int getFill() {
            return this.fill;
        }

        public final int getSingle() {
            return this.single;
        }
    }
}
