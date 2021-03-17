package com.android.systemui.statusbar.notification.policy;

import com.android.systemui.statusbar.notification.row.MiuiExpandableNotificationRow;
import java.util.function.Consumer;

/* access modifiers changed from: package-private */
/* compiled from: AppMiniWindowManager.kt */
public final class AppMiniWindowManager$updateAllHeadsUpMiniBars$4<T> implements Consumer<MiuiExpandableNotificationRow> {
    public static final AppMiniWindowManager$updateAllHeadsUpMiniBars$4 INSTANCE = new AppMiniWindowManager$updateAllHeadsUpMiniBars$4();

    AppMiniWindowManager$updateAllHeadsUpMiniBars$4() {
    }

    public final void accept(MiuiExpandableNotificationRow miuiExpandableNotificationRow) {
        miuiExpandableNotificationRow.updateMiniWindowBar();
    }
}
