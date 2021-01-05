package com.android.systemui.statusbar.notification.stack;

import com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: MiuiNotificationAnimationExtensions.kt */
public final class HeadsUpAppearEvent extends NotificationStackScrollLayout.AnimationEvent {
    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public HeadsUpAppearEvent(@NotNull NotificationStackScrollLayout.AnimationEvent animationEvent) {
        super(animationEvent.mChangingView, 21, (long) 550, animationEvent.filter);
        Intrinsics.checkParameterIsNotNull(animationEvent, "event");
        this.headsUpFromBottom = animationEvent.headsUpFromBottom;
    }
}
