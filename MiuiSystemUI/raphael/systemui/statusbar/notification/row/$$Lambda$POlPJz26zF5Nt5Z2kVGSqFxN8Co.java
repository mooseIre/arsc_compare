package com.android.systemui.statusbar.notification.row;

import android.os.CancellationSignal;
import java.util.function.Consumer;

/* renamed from: com.android.systemui.statusbar.notification.row.-$$Lambda$POlPJz26zF5Nt5Z2kVGSqFxN8Co  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$POlPJz26zF5Nt5Z2kVGSqFxN8Co implements Consumer {
    public static final /* synthetic */ $$Lambda$POlPJz26zF5Nt5Z2kVGSqFxN8Co INSTANCE = new $$Lambda$POlPJz26zF5Nt5Z2kVGSqFxN8Co();

    private /* synthetic */ $$Lambda$POlPJz26zF5Nt5Z2kVGSqFxN8Co() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((CancellationSignal) obj).cancel();
    }
}
