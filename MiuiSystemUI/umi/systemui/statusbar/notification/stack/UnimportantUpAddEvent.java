package com.android.systemui.statusbar.notification.stack;

import com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: MiuiNotificationAnimationExtensions.kt */
public final class UnimportantUpAddEvent extends NotificationStackScrollLayout.AnimationEvent {
    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public UnimportantUpAddEvent(@NotNull NotificationStackScrollLayout.AnimationEvent animationEvent) {
        super(animationEvent.mChangingView, 23, animationEvent.length, animationEvent.filter);
        Intrinsics.checkParameterIsNotNull(animationEvent, "event");
        this.filter.animateY = true;
    }
}
