package com.android.systemui.controls.ui;

import java.util.Comparator;

/* compiled from: Comparisons.kt */
public final class ControlsUiControllerImpl$$special$$inlined$compareBy$1<T> implements Comparator<T> {
    final /* synthetic */ Comparator $comparator;

    public ControlsUiControllerImpl$$special$$inlined$compareBy$1(Comparator comparator) {
        this.$comparator = comparator;
    }

    public final int compare(T t, T t2) {
        return this.$comparator.compare(((SelectionItem) t).getTitle(), ((SelectionItem) t2).getTitle());
    }
}
