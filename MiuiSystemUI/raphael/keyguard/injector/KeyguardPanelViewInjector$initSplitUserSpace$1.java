package com.android.keyguard.injector;

import android.app.ActivityManager;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import com.android.systemui.statusbar.phone.PanelView;

/* access modifiers changed from: package-private */
/* compiled from: KeyguardPanelViewInjector.kt */
public final class KeyguardPanelViewInjector$initSplitUserSpace$1 implements View.OnClickListener {
    final /* synthetic */ KeyguardPanelViewInjector this$0;

    KeyguardPanelViewInjector$initSplitUserSpace$1(KeyguardPanelViewInjector keyguardPanelViewInjector) {
        this.this$0 = keyguardPanelViewInjector;
    }

    public final void onClick(View view) {
        if (!KeyguardPanelViewInjector.access$getMUserContextController$p(this.this$0).isOwnerUser()) {
            try {
                ActivityManager.getService().switchUser(0);
                KeyguardPanelViewInjector.access$getMSwitchSystemUserEntrance$p(this.this$0).setVisibility(8);
            } catch (RemoteException e) {
                Log.e(PanelView.TAG, "switchUser failed", e.getCause());
            }
        }
    }
}
