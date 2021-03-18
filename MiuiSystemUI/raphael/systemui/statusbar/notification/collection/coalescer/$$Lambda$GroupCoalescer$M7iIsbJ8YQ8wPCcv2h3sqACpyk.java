package com.android.systemui.statusbar.notification.collection.coalescer;

import java.util.Comparator;

/* renamed from: com.android.systemui.statusbar.notification.collection.coalescer.-$$Lambda$GroupCoalescer$M7iIsb-J8YQ8wPCcv2h3sqACpyk  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$GroupCoalescer$M7iIsbJ8YQ8wPCcv2h3sqACpyk implements Comparator {
    public static final /* synthetic */ $$Lambda$GroupCoalescer$M7iIsbJ8YQ8wPCcv2h3sqACpyk INSTANCE = new $$Lambda$GroupCoalescer$M7iIsbJ8YQ8wPCcv2h3sqACpyk();

    private /* synthetic */ $$Lambda$GroupCoalescer$M7iIsbJ8YQ8wPCcv2h3sqACpyk() {
    }

    @Override // java.util.Comparator
    public final int compare(Object obj, Object obj2) {
        return GroupCoalescer.lambda$new$1((CoalescedEvent) obj, (CoalescedEvent) obj2);
    }
}
