package com.android.systemui.statusbar.policy;

import com.android.systemui.statusbar.policy.ExtensionControllerImpl;
import java.util.function.ToIntFunction;

/* renamed from: com.android.systemui.statusbar.policy.-$$Lambda$LO8p3lRLZXpohPDzojcJ_BVuMnk  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$LO8p3lRLZXpohPDzojcJ_BVuMnk implements ToIntFunction {
    public static final /* synthetic */ $$Lambda$LO8p3lRLZXpohPDzojcJ_BVuMnk INSTANCE = new $$Lambda$LO8p3lRLZXpohPDzojcJ_BVuMnk();

    private /* synthetic */ $$Lambda$LO8p3lRLZXpohPDzojcJ_BVuMnk() {
    }

    @Override // java.util.function.ToIntFunction
    public final int applyAsInt(Object obj) {
        return ((ExtensionControllerImpl.Item) obj).sortOrder();
    }
}
