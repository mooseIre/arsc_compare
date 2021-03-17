package com.android.systemui.statusbar.policy;

import com.android.systemui.statusbar.policy.KeyguardStateController;
import java.util.function.Consumer;

/* renamed from: com.android.systemui.statusbar.policy.-$$Lambda$6-7ujqA_9Wm5PTpKC6v1UcUnDTY  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$67ujqA_9Wm5PTpKC6v1UcUnDTY implements Consumer {
    public static final /* synthetic */ $$Lambda$67ujqA_9Wm5PTpKC6v1UcUnDTY INSTANCE = new $$Lambda$67ujqA_9Wm5PTpKC6v1UcUnDTY();

    private /* synthetic */ $$Lambda$67ujqA_9Wm5PTpKC6v1UcUnDTY() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((KeyguardStateController.Callback) obj).onUnlockedChanged();
    }
}
