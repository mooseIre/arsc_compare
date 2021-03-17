package com.android.systemui.controls.management;

import java.util.Comparator;

/* compiled from: Comparisons.kt */
public final class ControlsFavoritingActivity$onCreate$$inlined$compareBy$1<T> implements Comparator<T> {
    final /* synthetic */ Comparator $comparator;

    public ControlsFavoritingActivity$onCreate$$inlined$compareBy$1(Comparator comparator) {
        this.$comparator = comparator;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r0v1, resolved type: java.util.Comparator */
    /* JADX WARN: Multi-variable type inference failed */
    @Override // java.util.Comparator
    public final int compare(T t, T t2) {
        return this.$comparator.compare(t.getStructureName(), t2.getStructureName());
    }
}
