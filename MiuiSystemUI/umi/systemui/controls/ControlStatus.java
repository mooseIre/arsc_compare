package com.android.systemui.controls;

import android.content.ComponentName;
import android.graphics.drawable.Icon;
import android.service.controls.Control;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: ControlStatus.kt */
public final class ControlStatus implements ControlInterface {
    @NotNull
    private final ComponentName component;
    @NotNull
    private final Control control;
    private boolean favorite;
    private final boolean removed;

    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ControlStatus)) {
            return false;
        }
        ControlStatus controlStatus = (ControlStatus) obj;
        return Intrinsics.areEqual((Object) this.control, (Object) controlStatus.control) && Intrinsics.areEqual((Object) getComponent(), (Object) controlStatus.getComponent()) && getFavorite() == controlStatus.getFavorite() && getRemoved() == controlStatus.getRemoved();
    }

    public int hashCode() {
        Control control2 = this.control;
        int i = 0;
        int hashCode = (control2 != null ? control2.hashCode() : 0) * 31;
        ComponentName component2 = getComponent();
        if (component2 != null) {
            i = component2.hashCode();
        }
        int i2 = (hashCode + i) * 31;
        boolean favorite2 = getFavorite();
        boolean z = true;
        if (favorite2) {
            favorite2 = true;
        }
        int i3 = (i2 + (favorite2 ? 1 : 0)) * 31;
        boolean removed2 = getRemoved();
        if (!removed2) {
            z = removed2;
        }
        return i3 + (z ? 1 : 0);
    }

    @NotNull
    public String toString() {
        return "ControlStatus(control=" + this.control + ", component=" + getComponent() + ", favorite=" + getFavorite() + ", removed=" + getRemoved() + ")";
    }

    public ControlStatus(@NotNull Control control2, @NotNull ComponentName componentName, boolean z, boolean z2) {
        Intrinsics.checkParameterIsNotNull(control2, "control");
        Intrinsics.checkParameterIsNotNull(componentName, "component");
        this.control = control2;
        this.component = componentName;
        this.favorite = z;
        this.removed = z2;
    }

    @NotNull
    public final Control getControl() {
        return this.control;
    }

    @NotNull
    public ComponentName getComponent() {
        return this.component;
    }

    public boolean getFavorite() {
        return this.favorite;
    }

    public void setFavorite(boolean z) {
        this.favorite = z;
    }

    /* JADX INFO: this call moved to the top of the method (can break code semantics) */
    public /* synthetic */ ControlStatus(Control control2, ComponentName componentName, boolean z, boolean z2, int i, DefaultConstructorMarker defaultConstructorMarker) {
        this(control2, componentName, z, (i & 8) != 0 ? false : z2);
    }

    public boolean getRemoved() {
        return this.removed;
    }

    @NotNull
    public String getControlId() {
        String controlId = this.control.getControlId();
        Intrinsics.checkExpressionValueIsNotNull(controlId, "control.controlId");
        return controlId;
    }

    @NotNull
    public CharSequence getTitle() {
        CharSequence title = this.control.getTitle();
        Intrinsics.checkExpressionValueIsNotNull(title, "control.title");
        return title;
    }

    @NotNull
    public CharSequence getSubtitle() {
        CharSequence subtitle = this.control.getSubtitle();
        Intrinsics.checkExpressionValueIsNotNull(subtitle, "control.subtitle");
        return subtitle;
    }

    @Nullable
    public Icon getCustomIcon() {
        return this.control.getCustomIcon();
    }

    public int getDeviceType() {
        return this.control.getDeviceType();
    }
}
