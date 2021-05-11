package com.android.keyguard.fod;

import com.android.systemui.Dependency;
import com.android.systemui.statusbar.phone.StatusBar;

/* renamed from: com.android.keyguard.fod.-$$Lambda$MiuiGxzwManager$CwiMqWSzmARmfUO94Rp4D6BxC4E  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$MiuiGxzwManager$CwiMqWSzmARmfUO94Rp4D6BxC4E implements Runnable {
    public static final /* synthetic */ $$Lambda$MiuiGxzwManager$CwiMqWSzmARmfUO94Rp4D6BxC4E INSTANCE = new $$Lambda$MiuiGxzwManager$CwiMqWSzmARmfUO94Rp4D6BxC4E();

    private /* synthetic */ $$Lambda$MiuiGxzwManager$CwiMqWSzmARmfUO94Rp4D6BxC4E() {
    }

    public final void run() {
        ((StatusBar) Dependency.get(StatusBar.class)).collapsePanels();
    }
}
