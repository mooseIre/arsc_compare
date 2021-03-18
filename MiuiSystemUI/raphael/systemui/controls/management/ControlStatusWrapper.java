package com.android.systemui.controls.management;

import android.content.ComponentName;
import android.graphics.drawable.Icon;
import com.android.systemui.controls.ControlInterface;
import com.android.systemui.controls.ControlStatus;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: ControlsModel.kt */
public final class ControlStatusWrapper extends ElementWrapper implements ControlInterface {
    @NotNull
    private final ControlStatus controlStatus;

    public boolean equals(@Nullable Object obj) {
        if (this != obj) {
            return (obj instanceof ControlStatusWrapper) && Intrinsics.areEqual(this.controlStatus, ((ControlStatusWrapper) obj).controlStatus);
        }
        return true;
    }

    @Override // com.android.systemui.controls.ControlInterface
    @NotNull
    public ComponentName getComponent() {
        return this.controlStatus.getComponent();
    }

    @Override // com.android.systemui.controls.ControlInterface
    @NotNull
    public String getControlId() {
        return this.controlStatus.getControlId();
    }

    @Override // com.android.systemui.controls.ControlInterface
    @Nullable
    public Icon getCustomIcon() {
        return this.controlStatus.getCustomIcon();
    }

    @Override // com.android.systemui.controls.ControlInterface
    public int getDeviceType() {
        return this.controlStatus.getDeviceType();
    }

    @Override // com.android.systemui.controls.ControlInterface
    public boolean getFavorite() {
        return this.controlStatus.getFavorite();
    }

    @Override // com.android.systemui.controls.ControlInterface
    public boolean getRemoved() {
        return this.controlStatus.getRemoved();
    }

    @Override // com.android.systemui.controls.ControlInterface
    @NotNull
    public CharSequence getSubtitle() {
        return this.controlStatus.getSubtitle();
    }

    @Override // com.android.systemui.controls.ControlInterface
    @NotNull
    public CharSequence getTitle() {
        return this.controlStatus.getTitle();
    }

    public int hashCode() {
        ControlStatus controlStatus2 = this.controlStatus;
        if (controlStatus2 != null) {
            return controlStatus2.hashCode();
        }
        return 0;
    }

    @NotNull
    public String toString() {
        return "ControlStatusWrapper(controlStatus=" + this.controlStatus + ")";
    }

    @NotNull
    public final ControlStatus getControlStatus() {
        return this.controlStatus;
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public ControlStatusWrapper(@NotNull ControlStatus controlStatus2) {
        super(null);
        Intrinsics.checkParameterIsNotNull(controlStatus2, "controlStatus");
        this.controlStatus = controlStatus2;
    }
}
