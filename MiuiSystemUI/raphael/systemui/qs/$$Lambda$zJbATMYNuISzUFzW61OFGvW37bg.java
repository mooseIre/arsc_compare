package com.android.systemui.qs;

import com.android.systemui.statusbar.phone.StatusBar;
import java.util.function.Function;

/* renamed from: com.android.systemui.qs.-$$Lambda$zJbATMYNuISzUFzW61OFGvW37bg  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$zJbATMYNuISzUFzW61OFGvW37bg implements Function {
    public static final /* synthetic */ $$Lambda$zJbATMYNuISzUFzW61OFGvW37bg INSTANCE = new $$Lambda$zJbATMYNuISzUFzW61OFGvW37bg();

    private /* synthetic */ $$Lambda$zJbATMYNuISzUFzW61OFGvW37bg() {
    }

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        return Boolean.valueOf(((StatusBar) obj).isQSFullyCollapsed());
    }
}
