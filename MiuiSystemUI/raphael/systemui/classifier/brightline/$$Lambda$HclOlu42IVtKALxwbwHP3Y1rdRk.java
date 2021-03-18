package com.android.systemui.classifier.brightline;

import java.util.function.Consumer;

/* renamed from: com.android.systemui.classifier.brightline.-$$Lambda$HclOlu42IVtKALxwbwHP3Y1rdRk  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$HclOlu42IVtKALxwbwHP3Y1rdRk implements Consumer {
    public static final /* synthetic */ $$Lambda$HclOlu42IVtKALxwbwHP3Y1rdRk INSTANCE = new $$Lambda$HclOlu42IVtKALxwbwHP3Y1rdRk();

    private /* synthetic */ $$Lambda$HclOlu42IVtKALxwbwHP3Y1rdRk() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((FalsingClassifier) obj).onSessionStarted();
    }
}
