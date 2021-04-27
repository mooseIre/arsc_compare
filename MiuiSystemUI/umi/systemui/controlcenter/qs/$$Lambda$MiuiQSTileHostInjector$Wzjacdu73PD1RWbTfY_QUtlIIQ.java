package com.android.systemui.controlcenter.qs;

import com.android.systemui.plugins.qs.QSTile;
import java.util.function.Consumer;

/* renamed from: com.android.systemui.controlcenter.qs.-$$Lambda$MiuiQSTileHostInjector$Wzjacdu73PD1RWbTfY_QU-tlIIQ  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$MiuiQSTileHostInjector$Wzjacdu73PD1RWbTfY_QUtlIIQ implements Consumer {
    public static final /* synthetic */ $$Lambda$MiuiQSTileHostInjector$Wzjacdu73PD1RWbTfY_QUtlIIQ INSTANCE = new $$Lambda$MiuiQSTileHostInjector$Wzjacdu73PD1RWbTfY_QUtlIIQ();

    private /* synthetic */ $$Lambda$MiuiQSTileHostInjector$Wzjacdu73PD1RWbTfY_QUtlIIQ() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((QSTile) obj).refreshState();
    }
}
