package com.android.systemui.statusbar.notification.policy;

import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import java.util.function.Function;

/* renamed from: com.android.systemui.statusbar.notification.policy.-$$Lambda$kpoFpoZoiVX8t3fHvYFACoEdWls  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$kpoFpoZoiVX8t3fHvYFACoEdWls implements Function {
    public static final /* synthetic */ $$Lambda$kpoFpoZoiVX8t3fHvYFACoEdWls INSTANCE = new $$Lambda$kpoFpoZoiVX8t3fHvYFACoEdWls();

    private /* synthetic */ $$Lambda$kpoFpoZoiVX8t3fHvYFACoEdWls() {
    }

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        return ((NotificationEntry) obj).getKey();
    }
}
