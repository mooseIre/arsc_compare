package com.android.systemui.statusbar.policy;

import com.android.systemui.statusbar.policy.KeyguardStateController;
import java.util.function.Consumer;

/* renamed from: com.android.systemui.statusbar.policy.-$$Lambda$AiBS48IhN8ohBVmcN3CRVHIBtQ8  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$AiBS48IhN8ohBVmcN3CRVHIBtQ8 implements Consumer {
    public static final /* synthetic */ $$Lambda$AiBS48IhN8ohBVmcN3CRVHIBtQ8 INSTANCE = new $$Lambda$AiBS48IhN8ohBVmcN3CRVHIBtQ8();

    private /* synthetic */ $$Lambda$AiBS48IhN8ohBVmcN3CRVHIBtQ8() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((KeyguardStateController.Callback) obj).onKeyguardShowingChanged();
    }
}
