package com.android.systemui.statusbar.notification.unimportant;

import android.content.Context;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class FoldNotifController_Factory implements Factory<FoldNotifController> {
    private final Provider<Context> contextProvider;

    public FoldNotifController_Factory(Provider<Context> provider) {
        this.contextProvider = provider;
    }

    @Override // javax.inject.Provider
    public FoldNotifController get() {
        return provideInstance(this.contextProvider);
    }

    public static FoldNotifController provideInstance(Provider<Context> provider) {
        return new FoldNotifController(provider.get());
    }

    public static FoldNotifController_Factory create(Provider<Context> provider) {
        return new FoldNotifController_Factory(provider);
    }
}
