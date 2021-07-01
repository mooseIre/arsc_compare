package com.android.systemui.statusbar.phone;

import com.android.systemui.statusbar.notification.NotificationUtil;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import java.util.Map;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Lambda;
import org.jetbrains.annotations.NotNull;

/* access modifiers changed from: package-private */
/* compiled from: NotificationGroupManagerInjector.kt */
public final class NotificationGroupManagerInjectorKt$hasFoldChild$1 extends Lambda implements Function1<Map.Entry<? extends String, ? extends NotificationEntry>, Boolean> {
    public static final NotificationGroupManagerInjectorKt$hasFoldChild$1 INSTANCE = new NotificationGroupManagerInjectorKt$hasFoldChild$1();

    NotificationGroupManagerInjectorKt$hasFoldChild$1() {
        super(1);
    }

    /* Return type fixed from 'java.lang.Object' to match base method */
    /* JADX DEBUG: Method arguments types fixed to match base method, original types: [java.lang.Object] */
    @Override // kotlin.jvm.functions.Function1
    public /* bridge */ /* synthetic */ Boolean invoke(Map.Entry<? extends String, ? extends NotificationEntry> entry) {
        return Boolean.valueOf(invoke((Map.Entry<String, NotificationEntry>) entry));
    }

    public final boolean invoke(@NotNull Map.Entry<String, NotificationEntry> entry) {
        Intrinsics.checkParameterIsNotNull(entry, "it");
        NotificationEntry value = entry.getValue();
        Intrinsics.checkExpressionValueIsNotNull(value, "it.value");
        return NotificationUtil.isFold(value.getSbn());
    }
}
