package com.android.systemui.statusbar.notification.stack;

import android.service.notification.NotificationListenerService;
import java.util.Comparator;
import kotlin.comparisons.ComparisonsKt__ComparisonsKt;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: Comparisons.kt */
public final class ForegroundServiceSectionController$$special$$inlined$sortedBy$1<T> implements Comparator<T> {
    @Override // java.util.Comparator
    public final int compare(T t, T t2) {
        NotificationListenerService.Ranking ranking = t.getRanking();
        Intrinsics.checkExpressionValueIsNotNull(ranking, "it.ranking");
        Integer valueOf = Integer.valueOf(ranking.getRank());
        NotificationListenerService.Ranking ranking2 = t2.getRanking();
        Intrinsics.checkExpressionValueIsNotNull(ranking2, "it.ranking");
        return ComparisonsKt__ComparisonsKt.compareValues(valueOf, Integer.valueOf(ranking2.getRank()));
    }
}
