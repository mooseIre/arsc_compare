package com.android.systemui.controls.controller;

import android.service.controls.Control;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: ControlInfo.kt */
public final class ControlInfo {
    public static final Companion Companion = new Companion(null);
    @NotNull
    private final String controlId;
    @NotNull
    private final CharSequence controlSubtitle;
    @NotNull
    private final CharSequence controlTitle;
    private final int deviceType;

    public static /* synthetic */ ControlInfo copy$default(ControlInfo controlInfo, String str, CharSequence charSequence, CharSequence charSequence2, int i, int i2, Object obj) {
        if ((i2 & 1) != 0) {
            str = controlInfo.controlId;
        }
        if ((i2 & 2) != 0) {
            charSequence = controlInfo.controlTitle;
        }
        if ((i2 & 4) != 0) {
            charSequence2 = controlInfo.controlSubtitle;
        }
        if ((i2 & 8) != 0) {
            i = controlInfo.deviceType;
        }
        return controlInfo.copy(str, charSequence, charSequence2, i);
    }

    @NotNull
    public final ControlInfo copy(@NotNull String str, @NotNull CharSequence charSequence, @NotNull CharSequence charSequence2, int i) {
        Intrinsics.checkParameterIsNotNull(str, "controlId");
        Intrinsics.checkParameterIsNotNull(charSequence, "controlTitle");
        Intrinsics.checkParameterIsNotNull(charSequence2, "controlSubtitle");
        return new ControlInfo(str, charSequence, charSequence2, i);
    }

    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ControlInfo)) {
            return false;
        }
        ControlInfo controlInfo = (ControlInfo) obj;
        return Intrinsics.areEqual(this.controlId, controlInfo.controlId) && Intrinsics.areEqual(this.controlTitle, controlInfo.controlTitle) && Intrinsics.areEqual(this.controlSubtitle, controlInfo.controlSubtitle) && this.deviceType == controlInfo.deviceType;
    }

    public int hashCode() {
        String str = this.controlId;
        int i = 0;
        int hashCode = (str != null ? str.hashCode() : 0) * 31;
        CharSequence charSequence = this.controlTitle;
        int hashCode2 = (hashCode + (charSequence != null ? charSequence.hashCode() : 0)) * 31;
        CharSequence charSequence2 = this.controlSubtitle;
        if (charSequence2 != null) {
            i = charSequence2.hashCode();
        }
        return ((hashCode2 + i) * 31) + Integer.hashCode(this.deviceType);
    }

    public ControlInfo(@NotNull String str, @NotNull CharSequence charSequence, @NotNull CharSequence charSequence2, int i) {
        Intrinsics.checkParameterIsNotNull(str, "controlId");
        Intrinsics.checkParameterIsNotNull(charSequence, "controlTitle");
        Intrinsics.checkParameterIsNotNull(charSequence2, "controlSubtitle");
        this.controlId = str;
        this.controlTitle = charSequence;
        this.controlSubtitle = charSequence2;
        this.deviceType = i;
    }

    @NotNull
    public final String getControlId() {
        return this.controlId;
    }

    @NotNull
    public final CharSequence getControlTitle() {
        return this.controlTitle;
    }

    @NotNull
    public final CharSequence getControlSubtitle() {
        return this.controlSubtitle;
    }

    public final int getDeviceType() {
        return this.deviceType;
    }

    /* compiled from: ControlInfo.kt */
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        @NotNull
        public final ControlInfo fromControl(@NotNull Control control) {
            Intrinsics.checkParameterIsNotNull(control, "control");
            String controlId = control.getControlId();
            Intrinsics.checkExpressionValueIsNotNull(controlId, "control.controlId");
            CharSequence title = control.getTitle();
            Intrinsics.checkExpressionValueIsNotNull(title, "control.title");
            CharSequence subtitle = control.getSubtitle();
            Intrinsics.checkExpressionValueIsNotNull(subtitle, "control.subtitle");
            return new ControlInfo(controlId, title, subtitle, control.getDeviceType());
        }
    }

    @NotNull
    public String toString() {
        return ':' + this.controlId + ':' + this.controlTitle + ':' + this.deviceType;
    }
}
