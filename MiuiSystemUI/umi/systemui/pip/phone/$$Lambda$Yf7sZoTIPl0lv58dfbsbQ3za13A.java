package com.android.systemui.pip.phone;

import com.android.systemui.pip.phone.PipMenuActivityController;
import java.util.function.Consumer;

/* renamed from: com.android.systemui.pip.phone.-$$Lambda$Yf7sZoTIPl0lv58dfbsbQ3za13A  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$Yf7sZoTIPl0lv58dfbsbQ3za13A implements Consumer {
    public static final /* synthetic */ $$Lambda$Yf7sZoTIPl0lv58dfbsbQ3za13A INSTANCE = new $$Lambda$Yf7sZoTIPl0lv58dfbsbQ3za13A();

    private /* synthetic */ $$Lambda$Yf7sZoTIPl0lv58dfbsbQ3za13A() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((PipMenuActivityController.Listener) obj).onPipExpand();
    }
}
