package com.android.systemui.pip.phone;

import com.android.systemui.pip.phone.PipMenuActivityController;
import java.util.function.Consumer;

/* renamed from: com.android.systemui.pip.phone.-$$Lambda$zhx89MCRVbbUuwAz2vBzNfzR3hg  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$zhx89MCRVbbUuwAz2vBzNfzR3hg implements Consumer {
    public static final /* synthetic */ $$Lambda$zhx89MCRVbbUuwAz2vBzNfzR3hg INSTANCE = new $$Lambda$zhx89MCRVbbUuwAz2vBzNfzR3hg();

    private /* synthetic */ $$Lambda$zhx89MCRVbbUuwAz2vBzNfzR3hg() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((PipMenuActivityController.Listener) obj).onPipDismiss();
    }
}
