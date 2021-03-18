package com.android.systemui.shortcut;

import android.content.Context;
import com.android.systemui.recents.Recents;
import com.android.systemui.stackdivider.Divider;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class ShortcutKeyDispatcher_Factory implements Factory<ShortcutKeyDispatcher> {
    private final Provider<Context> contextProvider;
    private final Provider<Divider> dividerProvider;
    private final Provider<Recents> recentsProvider;

    public ShortcutKeyDispatcher_Factory(Provider<Context> provider, Provider<Divider> provider2, Provider<Recents> provider3) {
        this.contextProvider = provider;
        this.dividerProvider = provider2;
        this.recentsProvider = provider3;
    }

    @Override // javax.inject.Provider
    public ShortcutKeyDispatcher get() {
        return provideInstance(this.contextProvider, this.dividerProvider, this.recentsProvider);
    }

    public static ShortcutKeyDispatcher provideInstance(Provider<Context> provider, Provider<Divider> provider2, Provider<Recents> provider3) {
        return new ShortcutKeyDispatcher(provider.get(), provider2.get(), provider3.get());
    }

    public static ShortcutKeyDispatcher_Factory create(Provider<Context> provider, Provider<Divider> provider2, Provider<Recents> provider3) {
        return new ShortcutKeyDispatcher_Factory(provider, provider2, provider3);
    }
}
