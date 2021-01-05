package com.android.keyguard.injector;

import android.os.SystemClock;
import com.android.keyguard.analytics.AnalyticsHelper;
import com.android.systemui.statusbar.phone.DoubleTapHelper;

/* compiled from: KeyguardPanelViewInjector.kt */
final class KeyguardPanelViewInjector$init$2 implements DoubleTapHelper.DoubleTapListener {
    final /* synthetic */ KeyguardPanelViewInjector this$0;

    KeyguardPanelViewInjector$init$2(KeyguardPanelViewInjector keyguardPanelViewInjector) {
        this.this$0 = keyguardPanelViewInjector;
    }

    public final boolean onDoubleTap() {
        KeyguardPanelViewInjector.access$getMPowerManager$p(this.this$0).goToSleep(SystemClock.uptimeMillis());
        AnalyticsHelper.getInstance(this.this$0.getMContext()).record("action_double_click_sleep");
        return true;
    }
}
