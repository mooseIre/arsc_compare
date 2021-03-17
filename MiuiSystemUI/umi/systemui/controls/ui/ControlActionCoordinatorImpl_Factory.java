package com.android.systemui.controls.ui;

import android.content.Context;
import com.android.systemui.globalactions.GlobalActionsComponent;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.android.systemui.util.concurrency.DelayableExecutor;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class ControlActionCoordinatorImpl_Factory implements Factory<ControlActionCoordinatorImpl> {
    private final Provider<ActivityStarter> activityStarterProvider;
    private final Provider<DelayableExecutor> bgExecutorProvider;
    private final Provider<Context> contextProvider;
    private final Provider<GlobalActionsComponent> globalActionsComponentProvider;
    private final Provider<KeyguardStateController> keyguardStateControllerProvider;
    private final Provider<DelayableExecutor> uiExecutorProvider;

    public ControlActionCoordinatorImpl_Factory(Provider<Context> provider, Provider<DelayableExecutor> provider2, Provider<DelayableExecutor> provider3, Provider<ActivityStarter> provider4, Provider<KeyguardStateController> provider5, Provider<GlobalActionsComponent> provider6) {
        this.contextProvider = provider;
        this.bgExecutorProvider = provider2;
        this.uiExecutorProvider = provider3;
        this.activityStarterProvider = provider4;
        this.keyguardStateControllerProvider = provider5;
        this.globalActionsComponentProvider = provider6;
    }

    @Override // javax.inject.Provider
    public ControlActionCoordinatorImpl get() {
        return provideInstance(this.contextProvider, this.bgExecutorProvider, this.uiExecutorProvider, this.activityStarterProvider, this.keyguardStateControllerProvider, this.globalActionsComponentProvider);
    }

    public static ControlActionCoordinatorImpl provideInstance(Provider<Context> provider, Provider<DelayableExecutor> provider2, Provider<DelayableExecutor> provider3, Provider<ActivityStarter> provider4, Provider<KeyguardStateController> provider5, Provider<GlobalActionsComponent> provider6) {
        return new ControlActionCoordinatorImpl(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get(), provider6.get());
    }

    public static ControlActionCoordinatorImpl_Factory create(Provider<Context> provider, Provider<DelayableExecutor> provider2, Provider<DelayableExecutor> provider3, Provider<ActivityStarter> provider4, Provider<KeyguardStateController> provider5, Provider<GlobalActionsComponent> provider6) {
        return new ControlActionCoordinatorImpl_Factory(provider, provider2, provider3, provider4, provider5, provider6);
    }
}
