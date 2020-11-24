package com.android.systemui.controls.management;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: ControlsModel.kt */
public final class DividerWrapper extends ElementWrapper {
    private boolean showDivider;
    private boolean showNone;

    public DividerWrapper() {
        this(false, false, 3, (DefaultConstructorMarker) null);
    }

    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof DividerWrapper)) {
            return false;
        }
        DividerWrapper dividerWrapper = (DividerWrapper) obj;
        return this.showNone == dividerWrapper.showNone && this.showDivider == dividerWrapper.showDivider;
    }

    public int hashCode() {
        boolean z = this.showNone;
        boolean z2 = true;
        if (z) {
            z = true;
        }
        int i = (z ? 1 : 0) * true;
        boolean z3 = this.showDivider;
        if (!z3) {
            z2 = z3;
        }
        return i + (z2 ? 1 : 0);
    }

    @NotNull
    public String toString() {
        return "DividerWrapper(showNone=" + this.showNone + ", showDivider=" + this.showDivider + ")";
    }

    public final boolean getShowNone() {
        return this.showNone;
    }

    public final void setShowNone(boolean z) {
        this.showNone = z;
    }

    /* JADX INFO: this call moved to the top of the method (can break code semantics) */
    public /* synthetic */ DividerWrapper(boolean z, boolean z2, int i, DefaultConstructorMarker defaultConstructorMarker) {
        this((i & 1) != 0 ? false : z, (i & 2) != 0 ? false : z2);
    }

    public final boolean getShowDivider() {
        return this.showDivider;
    }

    public final void setShowDivider(boolean z) {
        this.showDivider = z;
    }

    public DividerWrapper(boolean z, boolean z2) {
        super((DefaultConstructorMarker) null);
        this.showNone = z;
        this.showDivider = z2;
    }
}
