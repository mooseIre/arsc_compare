package com.android.systemui.controlcenter.qs;

import com.android.systemui.plugins.qs.QSTile;
import java.util.function.Consumer;

/* renamed from: com.android.systemui.controlcenter.qs.-$$Lambda$MiuiQSTileHostInjector$TvxZmf6huBGafjkUs3QMGNRueDw  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$MiuiQSTileHostInjector$TvxZmf6huBGafjkUs3QMGNRueDw implements Consumer {
    public static final /* synthetic */ $$Lambda$MiuiQSTileHostInjector$TvxZmf6huBGafjkUs3QMGNRueDw INSTANCE = new $$Lambda$MiuiQSTileHostInjector$TvxZmf6huBGafjkUs3QMGNRueDw();

    private /* synthetic */ $$Lambda$MiuiQSTileHostInjector$TvxZmf6huBGafjkUs3QMGNRueDw() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((QSTile) obj).destroy();
    }
}
