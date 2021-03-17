package com.android.systemui.statusbar.phone;

import com.android.systemui.plugins.ActivityStarter;

public interface KeyguardDismissHandler {
    void executeWhenUnlocked(ActivityStarter.OnDismissAction onDismissAction, boolean z);
}
