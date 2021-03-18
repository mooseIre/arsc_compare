package com.android.systemui.statusbar.notification.row;

import java.util.Comparator;
import kotlin.comparisons.ComparisonsKt__ComparisonsKt;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: Comparisons.kt */
public final class ChannelEditorDialogController$getDisplayableChannels$$inlined$compareBy$1<T> implements Comparator<T> {
    @Override // java.util.Comparator
    public final int compare(T t, T t2) {
        String str;
        String str2;
        T t3 = t;
        Intrinsics.checkExpressionValueIsNotNull(t3, "it");
        CharSequence name = t3.getName();
        if (name == null || (str = name.toString()) == null) {
            str = t3.getId();
        }
        T t4 = t2;
        Intrinsics.checkExpressionValueIsNotNull(t4, "it");
        CharSequence name2 = t4.getName();
        if (name2 == null || (str2 = name2.toString()) == null) {
            str2 = t4.getId();
        }
        return ComparisonsKt__ComparisonsKt.compareValues(str, str2);
    }
}
