package com.android.systemui.controls.management;

import java.util.Comparator;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: Comparisons.kt */
public final class AppAdapter$callback$1$onServicesUpdated$1$$special$$inlined$compareBy$1<T> implements Comparator<T> {
    final /* synthetic */ Comparator $comparator;

    public AppAdapter$callback$1$onServicesUpdated$1$$special$$inlined$compareBy$1(Comparator comparator) {
        this.$comparator = comparator;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r1v1, resolved type: java.util.Comparator */
    /* JADX WARN: Multi-variable type inference failed */
    @Override // java.util.Comparator
    public final int compare(T t, T t2) {
        Comparator comparator = this.$comparator;
        CharSequence loadLabel = t.loadLabel();
        Intrinsics.checkExpressionValueIsNotNull(loadLabel, "it.loadLabel()");
        CharSequence loadLabel2 = t2.loadLabel();
        Intrinsics.checkExpressionValueIsNotNull(loadLabel2, "it.loadLabel()");
        return comparator.compare(loadLabel, loadLabel2);
    }
}
