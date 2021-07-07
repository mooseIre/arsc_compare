package com.android.systemui.statusbar.notification;

import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.unimportant.FoldTool;
import java.util.Map;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Lambda;
import org.jetbrains.annotations.NotNull;

/* compiled from: MiuiNotificationEntryManager.kt */
final class MiuiNotificationEntryManager$shouldShow$1 extends Lambda implements Function1<Map.Entry<? extends String, ? extends NotificationEntry>, Boolean> {
    final /* synthetic */ int $userId;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    MiuiNotificationEntryManager$shouldShow$1(int i) {
        super(1);
        this.$userId = i;
    }

    /* Return type fixed from 'java.lang.Object' to match base method */
    /* JADX DEBUG: Method arguments types fixed to match base method, original types: [java.lang.Object] */
    @Override // kotlin.jvm.functions.Function1
    public /* bridge */ /* synthetic */ Boolean invoke(Map.Entry<? extends String, ? extends NotificationEntry> entry) {
        return Boolean.valueOf(invoke((Map.Entry<String, NotificationEntry>) entry));
    }

    public final boolean invoke(@NotNull Map.Entry<String, NotificationEntry> entry) {
        Intrinsics.checkParameterIsNotNull(entry, "it");
        FoldTool foldTool = FoldTool.INSTANCE;
        NotificationEntry value = entry.getValue();
        Intrinsics.checkExpressionValueIsNotNull(value, "it.value");
        return foldTool.isSameUser(value.getSbn(), this.$userId);
    }
}
