package com.android.systemui.util.leak;

import java.util.Collection;
import java.util.function.Predicate;

/* renamed from: com.android.systemui.util.leak.-$$Lambda$LeakDetector$pWx7s0HACocvPZyQWmuD0rk2VO8  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$LeakDetector$pWx7s0HACocvPZyQWmuD0rk2VO8 implements Predicate {
    public static final /* synthetic */ $$Lambda$LeakDetector$pWx7s0HACocvPZyQWmuD0rk2VO8 INSTANCE = new $$Lambda$LeakDetector$pWx7s0HACocvPZyQWmuD0rk2VO8();

    private /* synthetic */ $$Lambda$LeakDetector$pWx7s0HACocvPZyQWmuD0rk2VO8() {
    }

    @Override // java.util.function.Predicate
    public final boolean test(Object obj) {
        return LeakDetector.lambda$dump$0((Collection) obj);
    }
}
