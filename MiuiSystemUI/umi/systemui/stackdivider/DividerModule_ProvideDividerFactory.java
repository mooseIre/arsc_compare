package com.android.systemui.stackdivider;

import android.content.Context;
import android.os.Handler;
import com.android.systemui.TransactionPool;
import com.android.systemui.recents.Recents;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.android.systemui.wm.DisplayController;
import com.android.systemui.wm.DisplayImeController;
import com.android.systemui.wm.SystemWindows;
import dagger.Lazy;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import java.util.Optional;
import javax.inject.Provider;

public final class DividerModule_ProvideDividerFactory implements Factory<Divider> {
    private final Provider<Context> contextProvider;
    private final Provider<DisplayController> displayControllerProvider;
    private final Provider<Handler> handlerProvider;
    private final Provider<DisplayImeController> imeControllerProvider;
    private final Provider<KeyguardStateController> keyguardStateControllerProvider;
    private final Provider<Optional<Lazy<Recents>>> recentsOptionalLazyProvider;
    private final Provider<SystemWindows> systemWindowsProvider;
    private final Provider<TransactionPool> transactionPoolProvider;

    public DividerModule_ProvideDividerFactory(Provider<Context> provider, Provider<Optional<Lazy<Recents>>> provider2, Provider<DisplayController> provider3, Provider<SystemWindows> provider4, Provider<DisplayImeController> provider5, Provider<Handler> provider6, Provider<KeyguardStateController> provider7, Provider<TransactionPool> provider8) {
        this.contextProvider = provider;
        this.recentsOptionalLazyProvider = provider2;
        this.displayControllerProvider = provider3;
        this.systemWindowsProvider = provider4;
        this.imeControllerProvider = provider5;
        this.handlerProvider = provider6;
        this.keyguardStateControllerProvider = provider7;
        this.transactionPoolProvider = provider8;
    }

    @Override // javax.inject.Provider
    public Divider get() {
        return provideInstance(this.contextProvider, this.recentsOptionalLazyProvider, this.displayControllerProvider, this.systemWindowsProvider, this.imeControllerProvider, this.handlerProvider, this.keyguardStateControllerProvider, this.transactionPoolProvider);
    }

    public static Divider provideInstance(Provider<Context> provider, Provider<Optional<Lazy<Recents>>> provider2, Provider<DisplayController> provider3, Provider<SystemWindows> provider4, Provider<DisplayImeController> provider5, Provider<Handler> provider6, Provider<KeyguardStateController> provider7, Provider<TransactionPool> provider8) {
        return proxyProvideDivider(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get(), provider6.get(), provider7.get(), provider8.get());
    }

    public static DividerModule_ProvideDividerFactory create(Provider<Context> provider, Provider<Optional<Lazy<Recents>>> provider2, Provider<DisplayController> provider3, Provider<SystemWindows> provider4, Provider<DisplayImeController> provider5, Provider<Handler> provider6, Provider<KeyguardStateController> provider7, Provider<TransactionPool> provider8) {
        return new DividerModule_ProvideDividerFactory(provider, provider2, provider3, provider4, provider5, provider6, provider7, provider8);
    }

    public static Divider proxyProvideDivider(Context context, Optional<Lazy<Recents>> optional, DisplayController displayController, SystemWindows systemWindows, DisplayImeController displayImeController, Handler handler, KeyguardStateController keyguardStateController, TransactionPool transactionPool) {
        Divider provideDivider = DividerModule.provideDivider(context, optional, displayController, systemWindows, displayImeController, handler, keyguardStateController, transactionPool);
        Preconditions.checkNotNull(provideDivider, "Cannot return null from a non-@Nullable @Provides method");
        return provideDivider;
    }
}
