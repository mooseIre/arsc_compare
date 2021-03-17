package com.android.systemui.statusbar.notification.collection;

import java.util.Comparator;
import org.jetbrains.annotations.NotNull;

/* compiled from: NotificationRankingManagerEx.kt */
public final class NotificationRankingManagerExKt {
    @NotNull
    private static final Comparator<NotificationEntry> miuiRankingComparator = NotificationRankingManagerExKt$miuiRankingComparator$1.INSTANCE;

    @NotNull
    public static final Comparator<NotificationEntry> getMiuiRankingComparator() {
        return miuiRankingComparator;
    }
}
