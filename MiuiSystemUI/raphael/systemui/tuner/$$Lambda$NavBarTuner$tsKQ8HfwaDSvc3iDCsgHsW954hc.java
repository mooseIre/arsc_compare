package com.android.systemui.tuner;

import com.android.systemui.Dependency;
import com.android.systemui.tuner.TunerService;
import java.util.function.Consumer;

/* renamed from: com.android.systemui.tuner.-$$Lambda$NavBarTuner$tsKQ8HfwaDSvc3iDCsgHsW954hc  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$NavBarTuner$tsKQ8HfwaDSvc3iDCsgHsW954hc implements Consumer {
    public static final /* synthetic */ $$Lambda$NavBarTuner$tsKQ8HfwaDSvc3iDCsgHsW954hc INSTANCE = new $$Lambda$NavBarTuner$tsKQ8HfwaDSvc3iDCsgHsW954hc();

    private /* synthetic */ $$Lambda$NavBarTuner$tsKQ8HfwaDSvc3iDCsgHsW954hc() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((TunerService) Dependency.get(TunerService.class)).removeTunable((TunerService.Tunable) obj);
    }
}
