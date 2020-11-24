package com.android.systemui.statusbar;

import androidx.dynamicanimation.animation.FloatPropertyCompat;
import com.android.systemui.statusbar.NotificationShadeDepthController;
import org.jetbrains.annotations.Nullable;

/* compiled from: NotificationShadeDepthController.kt */
public final class NotificationShadeDepthController$DepthAnimation$springAnimation$1 extends FloatPropertyCompat<NotificationShadeDepthController.DepthAnimation> {
    final /* synthetic */ NotificationShadeDepthController.DepthAnimation this$0;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    NotificationShadeDepthController$DepthAnimation$springAnimation$1(NotificationShadeDepthController.DepthAnimation depthAnimation, String str) {
        super(str);
        this.this$0 = depthAnimation;
    }

    public void setValue(@Nullable NotificationShadeDepthController.DepthAnimation depthAnimation, float f) {
        this.this$0.setRadius((int) f);
        NotificationShadeDepthController.DepthAnimation depthAnimation2 = this.this$0;
        NotificationShadeDepthController.this.scheduleUpdate(depthAnimation2.view);
    }

    public float getValue(@Nullable NotificationShadeDepthController.DepthAnimation depthAnimation) {
        return (float) this.this$0.getRadius();
    }
}
