package com.android.systemui.statusbar.notification.logging;

import com.android.internal.statusbar.NotificationVisibility;
import java.util.function.Function;

/* renamed from: com.android.systemui.statusbar.notification.logging.-$$Lambda$NotificationLogger$JO0enstrXhdro9FeK3TlyP_3L6E  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$NotificationLogger$JO0enstrXhdro9FeK3TlyP_3L6E implements Function {
    public static final /* synthetic */ $$Lambda$NotificationLogger$JO0enstrXhdro9FeK3TlyP_3L6E INSTANCE = new $$Lambda$NotificationLogger$JO0enstrXhdro9FeK3TlyP_3L6E();

    private /* synthetic */ $$Lambda$NotificationLogger$JO0enstrXhdro9FeK3TlyP_3L6E() {
    }

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        return ((NotificationVisibility) obj).key;
    }
}
