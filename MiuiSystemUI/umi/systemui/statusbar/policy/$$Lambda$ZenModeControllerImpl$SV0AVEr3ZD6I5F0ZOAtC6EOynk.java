package com.android.systemui.statusbar.policy;

import com.android.systemui.statusbar.policy.ZenModeController;
import java.util.function.Consumer;

/* renamed from: com.android.systemui.statusbar.policy.-$$Lambda$ZenModeControllerImpl$SV0AVEr3ZD6I5F0ZOAtC6EOyn-k  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ZenModeControllerImpl$SV0AVEr3ZD6I5F0ZOAtC6EOynk implements Consumer {
    public static final /* synthetic */ $$Lambda$ZenModeControllerImpl$SV0AVEr3ZD6I5F0ZOAtC6EOynk INSTANCE = new $$Lambda$ZenModeControllerImpl$SV0AVEr3ZD6I5F0ZOAtC6EOynk();

    private /* synthetic */ $$Lambda$ZenModeControllerImpl$SV0AVEr3ZD6I5F0ZOAtC6EOynk() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((ZenModeController.Callback) obj).onEffectsSupressorChanged();
    }
}
