package com.android.systemui.controls;

import android.content.ComponentName;
import android.graphics.drawable.Icon;
import android.service.controls.Control;
import kotlin.jvm.internal.DefaultConstructorMarker;
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
        return Intrinsics.areEqual(this.control, controlStatus.control) && Intrinsics.areEqual(getComponent(), controlStatus.getComponent()) && getFavorite() == controlStatus.getFavorite() && getRemoved() == controlStatus.getRemoved();
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
        int i3 = 1;
        if (favorite2) {
            favorite2 = true;
        }
        int i4 = favorite2 ? 1 : 0;
        int i5 = favorite2 ? 1 : 0;
        int i6 = favorite2 ? 1 : 0;
        int i7 = (i2 + i4) * 31;
        boolean removed2 = getRemoved();
        if (!removed2) {
            i3 = removed2;
        }
        return i7 + i3;
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

    @Override // com.android.systemui.controls.ControlInterface
    @NotNull
    public ComponentName getComponent() {
        return this.component;
    }

    @Override // com.android.systemui.controls.ControlInterface
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

    @Override // com.android.systemui.controls.ControlInterface
    public boolean getRemoved() {
        return this.removed;
    }

    @Override // com.android.systemui.controls.ControlInterface
    @NotNull
    public String getControlId() {
        String controlId = this.control.getControlId();
        Intrinsics.checkExpressionValueIsNotNull(controlId, "control.controlId");
        return controlId;
    }

    @Override // com.android.systemui.controls.ControlInterface
    @NotNull
    public CharSequence getTitle() {
        CharSequence title = this.control.getTitle();
        Intrinsics.checkExpressionValueIsNotNull(title, "control.title");
        return title;
    }

    @Override // com.android.systemui.controls.ControlInterface
    @NotNull
    public CharSequence getSubtitle() {
        CharSequence subtitle = this.control.getSubtitle();
        Intrinsics.checkExpressionValueIsNotNull(subtitle, "control.subtitle");
        return subtitle;
    }

    @Override // com.android.systemui.controls.ControlInterface
    @Nullable
    public Icon getCustomIcon() {
        return this.control.getCustomIcon();
    }

    @Override // com.android.systemui.controls.ControlInterface
    public int getDeviceType() {
        return this.control.getDeviceType();
    }
}
