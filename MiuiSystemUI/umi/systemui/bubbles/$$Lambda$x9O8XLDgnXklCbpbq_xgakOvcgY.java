package com.android.systemui.bubbles;

import java.util.function.ToLongFunction;

/* renamed from: com.android.systemui.bubbles.-$$Lambda$x9O8XLDgnXklCbpbq_xgakOvcgY  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$x9O8XLDgnXklCbpbq_xgakOvcgY implements ToLongFunction {
    public static final /* synthetic */ $$Lambda$x9O8XLDgnXklCbpbq_xgakOvcgY INSTANCE = new $$Lambda$x9O8XLDgnXklCbpbq_xgakOvcgY();

    private /* synthetic */ $$Lambda$x9O8XLDgnXklCbpbq_xgakOvcgY() {
    }

    @Override // java.util.function.ToLongFunction
    public final long applyAsLong(Object obj) {
        return ((Bubble) obj).getLastActivity();
    }
}
