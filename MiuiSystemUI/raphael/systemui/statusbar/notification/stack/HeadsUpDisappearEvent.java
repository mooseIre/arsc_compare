package com.android.systemui.statusbar.notification.stack;

import com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: MiuiNotificationAnimationExtensions.kt */
public final class HeadsUpDisappearEvent extends NotificationStackScrollLayout.AnimationEvent {
    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public HeadsUpDisappearEvent(@NotNull NotificationStackScrollLayout.AnimationEvent animationEvent) {
        super(animationEvent.mChangingView, 22, 150, animationEvent.filter);
        Intrinsics.checkParameterIsNotNull(animationEvent, "event");
        this.headsUpFromBottom = animationEvent.headsUpFromBottom;
    }
}
