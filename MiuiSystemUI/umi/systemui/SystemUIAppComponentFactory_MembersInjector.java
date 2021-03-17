package com.android.systemui;

import com.android.systemui.dagger.ContextComponentHelper;

public final class SystemUIAppComponentFactory_MembersInjector {
    public static void injectMComponentHelper(SystemUIAppComponentFactory systemUIAppComponentFactory, ContextComponentHelper contextComponentHelper) {
        systemUIAppComponentFactory.mComponentHelper = contextComponentHelper;
    }
}
