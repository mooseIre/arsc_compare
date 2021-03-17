package com.android.systemui.pip.phone;

import com.android.systemui.pip.phone.PipMenuActivityController;
import java.util.function.Consumer;

/* renamed from: com.android.systemui.pip.phone.-$$Lambda$ab7bqy0BtiE8EwwZ2rb49JCCbFA  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ab7bqy0BtiE8EwwZ2rb49JCCbFA implements Consumer {
    public static final /* synthetic */ $$Lambda$ab7bqy0BtiE8EwwZ2rb49JCCbFA INSTANCE = new $$Lambda$ab7bqy0BtiE8EwwZ2rb49JCCbFA();

    private /* synthetic */ $$Lambda$ab7bqy0BtiE8EwwZ2rb49JCCbFA() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((PipMenuActivityController.Listener) obj).onPipShowMenu();
    }
}
