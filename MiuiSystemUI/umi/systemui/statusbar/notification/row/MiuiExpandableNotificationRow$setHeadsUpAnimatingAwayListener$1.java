package com.android.systemui.statusbar.notification.row;

import java.util.function.Consumer;

/* compiled from: MiuiExpandableNotificationRow.kt */
final class MiuiExpandableNotificationRow$setHeadsUpAnimatingAwayListener$1<T> implements Consumer<Boolean> {
    final /* synthetic */ Consumer $listener;
    final /* synthetic */ MiuiExpandableNotificationRow this$0;

    MiuiExpandableNotificationRow$setHeadsUpAnimatingAwayListener$1(MiuiExpandableNotificationRow miuiExpandableNotificationRow, Consumer consumer) {
        this.this$0 = miuiExpandableNotificationRow;
        this.$listener = consumer;
    }

    public final void accept(Boolean bool) {
        this.$listener.accept(bool);
        this.this$0.updateBackgroundBg();
    }
}
