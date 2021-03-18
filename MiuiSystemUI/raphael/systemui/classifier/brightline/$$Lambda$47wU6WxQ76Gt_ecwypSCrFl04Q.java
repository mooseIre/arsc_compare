package com.android.systemui.classifier.brightline;

import java.util.function.Consumer;

/* renamed from: com.android.systemui.classifier.brightline.-$$Lambda$47wU6WxQ-76Gt_ecwypSCrFl04Q  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$47wU6WxQ76Gt_ecwypSCrFl04Q implements Consumer {
    public static final /* synthetic */ $$Lambda$47wU6WxQ76Gt_ecwypSCrFl04Q INSTANCE = new $$Lambda$47wU6WxQ76Gt_ecwypSCrFl04Q();

    private /* synthetic */ $$Lambda$47wU6WxQ76Gt_ecwypSCrFl04Q() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((FalsingClassifier) obj).onSessionEnded();
    }
}
