package com.android.systemui.recents;

import com.android.systemui.stackdivider.Divider;
import java.util.function.Consumer;

/* renamed from: com.android.systemui.recents.-$$Lambda$xuXEcdh0HmTmuN4e7qU9mBkM36M  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$xuXEcdh0HmTmuN4e7qU9mBkM36M implements Consumer {
    public static final /* synthetic */ $$Lambda$xuXEcdh0HmTmuN4e7qU9mBkM36M INSTANCE = new $$Lambda$xuXEcdh0HmTmuN4e7qU9mBkM36M();

    private /* synthetic */ $$Lambda$xuXEcdh0HmTmuN4e7qU9mBkM36M() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((Divider) obj).onDockedFirstAnimationFrame();
    }
}
