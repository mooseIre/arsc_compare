package com.android.systemui.pip;

import android.content.Context;
import com.android.systemui.stackdivider.Divider;
import com.android.systemui.wm.DisplayController;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class PipTaskOrganizer_Factory implements Factory<PipTaskOrganizer> {
    private final Provider<PipBoundsHandler> boundsHandlerProvider;
    private final Provider<Context> contextProvider;
    private final Provider<DisplayController> displayControllerProvider;
    private final Provider<Divider> dividerProvider;
    private final Provider<PipAnimationController> pipAnimationControllerProvider;
    private final Provider<PipUiEventLogger> pipUiEventLoggerProvider;
    private final Provider<PipSurfaceTransactionHelper> surfaceTransactionHelperProvider;

    public PipTaskOrganizer_Factory(Provider<Context> provider, Provider<PipBoundsHandler> provider2, Provider<PipSurfaceTransactionHelper> provider3, Provider<Divider> provider4, Provider<DisplayController> provider5, Provider<PipAnimationController> provider6, Provider<PipUiEventLogger> provider7) {
        this.contextProvider = provider;
        this.boundsHandlerProvider = provider2;
        this.surfaceTransactionHelperProvider = provider3;
        this.dividerProvider = provider4;
        this.displayControllerProvider = provider5;
        this.pipAnimationControllerProvider = provider6;
        this.pipUiEventLoggerProvider = provider7;
    }

    @Override // javax.inject.Provider
    public PipTaskOrganizer get() {
        return provideInstance(this.contextProvider, this.boundsHandlerProvider, this.surfaceTransactionHelperProvider, this.dividerProvider, this.displayControllerProvider, this.pipAnimationControllerProvider, this.pipUiEventLoggerProvider);
    }

    public static PipTaskOrganizer provideInstance(Provider<Context> provider, Provider<PipBoundsHandler> provider2, Provider<PipSurfaceTransactionHelper> provider3, Provider<Divider> provider4, Provider<DisplayController> provider5, Provider<PipAnimationController> provider6, Provider<PipUiEventLogger> provider7) {
        return new PipTaskOrganizer(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get(), provider6.get(), provider7.get());
    }

    public static PipTaskOrganizer_Factory create(Provider<Context> provider, Provider<PipBoundsHandler> provider2, Provider<PipSurfaceTransactionHelper> provider3, Provider<Divider> provider4, Provider<DisplayController> provider5, Provider<PipAnimationController> provider6, Provider<PipUiEventLogger> provider7) {
        return new PipTaskOrganizer_Factory(provider, provider2, provider3, provider4, provider5, provider6, provider7);
    }
}
