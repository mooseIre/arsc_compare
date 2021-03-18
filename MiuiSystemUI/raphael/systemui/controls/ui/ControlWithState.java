package com.android.systemui.controls.ui;

import android.content.ComponentName;
import android.service.controls.Control;
import com.android.systemui.controls.controller.ControlInfo;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: ControlWithState.kt */
public final class ControlWithState {
    @NotNull
    private final ControlInfo ci;
    @NotNull
    private final ComponentName componentName;
    @Nullable
    private final Control control;

    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ControlWithState)) {
            return false;
        }
        ControlWithState controlWithState = (ControlWithState) obj;
        return Intrinsics.areEqual(this.componentName, controlWithState.componentName) && Intrinsics.areEqual(this.ci, controlWithState.ci) && Intrinsics.areEqual(this.control, controlWithState.control);
    }

    public int hashCode() {
        ComponentName componentName2 = this.componentName;
        int i = 0;
        int hashCode = (componentName2 != null ? componentName2.hashCode() : 0) * 31;
        ControlInfo controlInfo = this.ci;
        int hashCode2 = (hashCode + (controlInfo != null ? controlInfo.hashCode() : 0)) * 31;
        Control control2 = this.control;
        if (control2 != null) {
            i = control2.hashCode();
        }
        return hashCode2 + i;
    }

    @NotNull
    public String toString() {
        return "ControlWithState(componentName=" + this.componentName + ", ci=" + this.ci + ", control=" + this.control + ")";
    }

    public ControlWithState(@NotNull ComponentName componentName2, @NotNull ControlInfo controlInfo, @Nullable Control control2) {
        Intrinsics.checkParameterIsNotNull(componentName2, "componentName");
        Intrinsics.checkParameterIsNotNull(controlInfo, "ci");
        this.componentName = componentName2;
        this.ci = controlInfo;
        this.control = control2;
    }

    @NotNull
    public final ComponentName getComponentName() {
        return this.componentName;
    }

    @NotNull
    public final ControlInfo getCi() {
        return this.ci;
    }

    @Nullable
    public final Control getControl() {
        return this.control;
    }
}
