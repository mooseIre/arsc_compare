package com.android.systemui.stackdivider;

import com.android.systemui.recents.Recents;
import dagger.Lazy;
import java.util.function.Consumer;

/* renamed from: com.android.systemui.stackdivider.-$$Lambda$Divider$kUReJvdE1s1BPD9HklZ-GjPX7dM  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$Divider$kUReJvdE1s1BPD9HklZGjPX7dM implements Consumer {
    public static final /* synthetic */ $$Lambda$Divider$kUReJvdE1s1BPD9HklZGjPX7dM INSTANCE = new $$Lambda$Divider$kUReJvdE1s1BPD9HklZGjPX7dM();

    private /* synthetic */ $$Lambda$Divider$kUReJvdE1s1BPD9HklZGjPX7dM() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((Recents) ((Lazy) obj).get()).growRecents();
    }
}
