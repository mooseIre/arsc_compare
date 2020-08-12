package com.android.systemui;

import miui.external.ApplicationDelegate;

public class Application extends miui.external.Application {
    public ApplicationDelegate onCreateApplicationDelegate() {
        return new SystemUIApplication();
    }

    public SystemUIApplication getSystemUIApplication() {
        return (SystemUIApplication) getApplicationDelegate();
    }
}
