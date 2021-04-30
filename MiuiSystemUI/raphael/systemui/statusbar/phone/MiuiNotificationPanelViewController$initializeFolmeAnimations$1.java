package com.android.systemui.statusbar.phone;

import miuix.animation.listener.TransitionListener;
import miuix.animation.property.FloatProperty;
import org.jetbrains.annotations.Nullable;

/* compiled from: MiuiNotificationPanelViewController.kt */
public final class MiuiNotificationPanelViewController$initializeFolmeAnimations$1 extends TransitionListener {
    final /* synthetic */ MiuiNotificationPanelViewController this$0;

    /* JADX WARN: Incorrect args count in method signature: ()V */
    MiuiNotificationPanelViewController$initializeFolmeAnimations$1(MiuiNotificationPanelViewController miuiNotificationPanelViewController) {
        this.this$0 = miuiNotificationPanelViewController;
    }

    @Override // miuix.animation.listener.TransitionListener
    public void onUpdate(@Nullable Object obj, @Nullable FloatProperty<?> floatProperty, float f, float f2, boolean z) {
        this.this$0.setMSpringLength(f);
    }
}
