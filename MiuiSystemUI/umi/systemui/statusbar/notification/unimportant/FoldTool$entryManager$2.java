package com.android.systemui.statusbar.notification.unimportant;

import com.android.systemui.Dependency;
import com.android.systemui.statusbar.notification.MiuiNotificationEntryManager;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import kotlin.TypeCastException;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.Lambda;
import org.jetbrains.annotations.NotNull;

/* compiled from: FoldTool.kt */
final class FoldTool$entryManager$2 extends Lambda implements Function0<MiuiNotificationEntryManager> {
    public static final FoldTool$entryManager$2 INSTANCE = new FoldTool$entryManager$2();

    FoldTool$entryManager$2() {
        super(0);
    }

    @Override // kotlin.jvm.functions.Function0
    @NotNull
    public final MiuiNotificationEntryManager invoke() {
        Object obj = Dependency.get(NotificationEntryManager.class);
        if (obj != null) {
            return (MiuiNotificationEntryManager) obj;
        }
        throw new TypeCastException("null cannot be cast to non-null type com.android.systemui.statusbar.notification.MiuiNotificationEntryManager");
    }
}
