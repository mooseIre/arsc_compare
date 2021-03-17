package com.android.systemui.statusbar.phone;

import com.android.systemui.Dumpable;
import com.android.systemui.plugins.DarkIconDispatcher;

public interface SysuiDarkIconDispatcher extends DarkIconDispatcher, Dumpable {
    LightBarTransitionsController getTransitionsController();
}
