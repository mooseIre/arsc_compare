package com.android.systemui.statusbar.notification.stack;

import com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout;
import com.miui.systemui.animation.PhysicBasedInterpolator;
import kotlin.jvm.internal.DefaultConstructorMarker;
import org.jetbrains.annotations.NotNull;

/* compiled from: MiuiNotificationAnimationExtensions.kt */
public final class HeadsUpPositionEvent extends NotificationStackScrollLayout.AnimationEvent {
    public static final Companion Companion = new Companion(null);
    @NotNull
    private static final PhysicBasedInterpolator INTERPOLATOR;

    /* compiled from: MiuiNotificationAnimationExtensions.kt */
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        @NotNull
        public final PhysicBasedInterpolator getINTERPOLATOR() {
            return HeadsUpPositionEvent.INTERPOLATOR;
        }
    }

    /* JADX WARNING: Illegal instructions before constructor call */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public HeadsUpPositionEvent(@org.jetbrains.annotations.NotNull com.android.systemui.statusbar.notification.row.ExpandableView r8) {
        /*
            r7 = this;
            java.lang.String r0 = "view"
            kotlin.jvm.internal.Intrinsics.checkParameterIsNotNull(r8, r0)
            com.android.systemui.statusbar.notification.stack.AnimationFilter r6 = new com.android.systemui.statusbar.notification.stack.AnimationFilter
            r6.<init>()
            r6.animateY(r8)
            r3 = 18
            r4 = 250(0xfa, double:1.235E-321)
            r1 = r7
            r2 = r8
            r1.<init>(r2, r3, r4, r6)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.notification.stack.HeadsUpPositionEvent.<init>(com.android.systemui.statusbar.notification.row.ExpandableView):void");
    }

    static {
        PhysicBasedInterpolator.Builder builder = new PhysicBasedInterpolator.Builder();
        builder.setDamping(0.85f);
        builder.setResponse(0.67f);
        INTERPOLATOR = builder.build();
    }
}
