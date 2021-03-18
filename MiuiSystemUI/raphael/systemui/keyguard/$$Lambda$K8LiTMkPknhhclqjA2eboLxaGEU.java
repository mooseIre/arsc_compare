package com.android.systemui.keyguard;

import com.android.systemui.keyguard.ScreenLifecycle;
import java.util.function.Consumer;

/* renamed from: com.android.systemui.keyguard.-$$Lambda$K8LiTMkPknhhclqjA2eboLxaGEU  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$K8LiTMkPknhhclqjA2eboLxaGEU implements Consumer {
    public static final /* synthetic */ $$Lambda$K8LiTMkPknhhclqjA2eboLxaGEU INSTANCE = new $$Lambda$K8LiTMkPknhhclqjA2eboLxaGEU();

    private /* synthetic */ $$Lambda$K8LiTMkPknhhclqjA2eboLxaGEU() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((ScreenLifecycle.Observer) obj).onScreenTurnedOff();
    }
}
