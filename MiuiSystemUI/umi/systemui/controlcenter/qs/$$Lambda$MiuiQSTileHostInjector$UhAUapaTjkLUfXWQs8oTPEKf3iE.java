package com.android.systemui.controlcenter.qs;

import com.android.systemui.plugins.qs.QSTile;
import java.util.function.Consumer;

/* renamed from: com.android.systemui.controlcenter.qs.-$$Lambda$MiuiQSTileHostInjector$UhAUapaTjkLUfXWQs8oTPEKf3iE  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$MiuiQSTileHostInjector$UhAUapaTjkLUfXWQs8oTPEKf3iE implements Consumer {
    public static final /* synthetic */ $$Lambda$MiuiQSTileHostInjector$UhAUapaTjkLUfXWQs8oTPEKf3iE INSTANCE = new $$Lambda$MiuiQSTileHostInjector$UhAUapaTjkLUfXWQs8oTPEKf3iE();

    private /* synthetic */ $$Lambda$MiuiQSTileHostInjector$UhAUapaTjkLUfXWQs8oTPEKf3iE() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((QSTile) obj).refreshState();
    }
}
