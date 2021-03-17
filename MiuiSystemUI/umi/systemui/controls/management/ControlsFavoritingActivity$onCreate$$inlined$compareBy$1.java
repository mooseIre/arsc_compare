package com.android.systemui.controls.management;

import java.util.Comparator;

/* compiled from: Comparisons.kt */
public final class ControlsFavoritingActivity$onCreate$$inlined$compareBy$1<T> implements Comparator<T> {
    final /* synthetic */ Comparator $comparator;

    public ControlsFavoritingActivity$onCreate$$inlined$compareBy$1(Comparator comparator) {
        this.$comparator = comparator;
    }

    public final int compare(T t, T t2) {
        return this.$comparator.compare(((StructureContainer) t).getStructureName(), ((StructureContainer) t2).getStructureName());
    }
}
