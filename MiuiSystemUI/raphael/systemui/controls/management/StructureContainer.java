package com.android.systemui.controls.management;

import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: ControlsFavoritingActivity.kt */
public final class StructureContainer {
    @NotNull
    private final ControlsModel model;
    @NotNull
    private final CharSequence structureName;

    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof StructureContainer)) {
            return false;
        }
        StructureContainer structureContainer = (StructureContainer) obj;
        return Intrinsics.areEqual((Object) this.structureName, (Object) structureContainer.structureName) && Intrinsics.areEqual((Object) this.model, (Object) structureContainer.model);
    }

    public int hashCode() {
        CharSequence charSequence = this.structureName;
        int i = 0;
        int hashCode = (charSequence != null ? charSequence.hashCode() : 0) * 31;
        ControlsModel controlsModel = this.model;
        if (controlsModel != null) {
            i = controlsModel.hashCode();
        }
        return hashCode + i;
    }

    @NotNull
    public String toString() {
        return "StructureContainer(structureName=" + this.structureName + ", model=" + this.model + ")";
    }

    public StructureContainer(@NotNull CharSequence charSequence, @NotNull ControlsModel controlsModel) {
        Intrinsics.checkParameterIsNotNull(charSequence, "structureName");
        Intrinsics.checkParameterIsNotNull(controlsModel, "model");
        this.structureName = charSequence;
        this.model = controlsModel;
    }

    @NotNull
    public final ControlsModel getModel() {
        return this.model;
    }

    @NotNull
    public final CharSequence getStructureName() {
        return this.structureName;
    }
}
