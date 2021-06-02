package com.android.systemui.controls.ui;

import android.content.ComponentName;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.ArrayMap;
import android.util.SparseArray;
import com.android.systemui.C0013R$drawable;
import kotlin.Pair;
import kotlin.collections.MapsKt;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;

public final class RenderInfo {
    public static final Companion Companion = new Companion(null);
    private static final ArrayMap<ComponentName, Drawable> appIconMap = new ArrayMap<>();
    private static final SparseArray<Drawable> iconMap = new SparseArray<>();
    private final int enabledBackground;
    private final int foreground;
    private final Drawable icon;

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof RenderInfo)) {
            return false;
        }
        RenderInfo renderInfo = (RenderInfo) obj;
        return Intrinsics.areEqual(this.icon, renderInfo.icon) && this.foreground == renderInfo.foreground && this.enabledBackground == renderInfo.enabledBackground;
    }

    public int hashCode() {
        Drawable drawable = this.icon;
        return ((((drawable != null ? drawable.hashCode() : 0) * 31) + Integer.hashCode(this.foreground)) * 31) + Integer.hashCode(this.enabledBackground);
    }

    public String toString() {
        return "RenderInfo(icon=" + this.icon + ", foreground=" + this.foreground + ", enabledBackground=" + this.enabledBackground + ")";
    }

    public RenderInfo(Drawable drawable, int i, int i2) {
        Intrinsics.checkParameterIsNotNull(drawable, "icon");
        this.icon = drawable;
        this.foreground = i;
        this.enabledBackground = i2;
    }

    public final Drawable getIcon() {
        return this.icon;
    }

    public final int getForeground() {
        return this.foreground;
    }

    public final int getEnabledBackground() {
        return this.enabledBackground;
    }

    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        public static /* synthetic */ RenderInfo lookup$default(Companion companion, Context context, ComponentName componentName, int i, int i2, int i3, Object obj) {
            if ((i3 & 8) != 0) {
                i2 = 0;
            }
            return companion.lookup(context, componentName, i, i2);
        }

        public final RenderInfo lookup(Context context, ComponentName componentName, int i, int i2) {
            Drawable drawable;
            Intrinsics.checkParameterIsNotNull(context, "context");
            Intrinsics.checkParameterIsNotNull(componentName, "componentName");
            if (i2 > 0) {
                i = (i * 1000) + i2;
            }
            Pair pair = (Pair) MapsKt.getValue(RenderInfoKt.deviceColorMap, Integer.valueOf(i));
            int intValue = ((Number) pair.component1()).intValue();
            int intValue2 = ((Number) pair.component2()).intValue();
            int intValue3 = ((Number) MapsKt.getValue(RenderInfoKt.deviceIconMap, Integer.valueOf(i))).intValue();
            if (intValue3 == -1) {
                drawable = (Drawable) RenderInfo.appIconMap.get(componentName);
                if (drawable == null) {
                    drawable = context.getResources().getDrawable(C0013R$drawable.ic_device_unknown_on, null);
                    RenderInfo.appIconMap.put(componentName, drawable);
                }
            } else {
                Drawable drawable2 = (Drawable) RenderInfo.iconMap.get(intValue3);
                if (drawable2 == null) {
                    drawable2 = context.getResources().getDrawable(intValue3, null);
                    RenderInfo.iconMap.put(intValue3, drawable2);
                }
                drawable = drawable2;
            }
            if (drawable != null) {
                Drawable newDrawable = drawable.getConstantState().newDrawable(context.getResources());
                Intrinsics.checkExpressionValueIsNotNull(newDrawable, "icon!!.constantState.new…awable(context.resources)");
                return new RenderInfo(newDrawable, intValue, intValue2);
            }
            Intrinsics.throwNpe();
            throw null;
        }

        public final void registerComponentIcon(ComponentName componentName, Drawable drawable) {
            Intrinsics.checkParameterIsNotNull(componentName, "componentName");
            Intrinsics.checkParameterIsNotNull(drawable, "icon");
            RenderInfo.appIconMap.put(componentName, drawable);
        }

        public final void clearCache() {
            RenderInfo.iconMap.clear();
            RenderInfo.appIconMap.clear();
        }
    }
}
