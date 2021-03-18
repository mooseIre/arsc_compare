package com.android.systemui.assist;

import android.content.Context;
import com.android.internal.app.AssistUtils;
import com.android.internal.logging.UiEventLogger;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class AssistLogger_Factory implements Factory<AssistLogger> {
    private final Provider<AssistHandleBehaviorController> assistHandleBehaviorControllerProvider;
    private final Provider<AssistUtils> assistUtilsProvider;
    private final Provider<Context> contextProvider;
    private final Provider<PhoneStateMonitor> phoneStateMonitorProvider;
    private final Provider<UiEventLogger> uiEventLoggerProvider;

    public AssistLogger_Factory(Provider<Context> provider, Provider<UiEventLogger> provider2, Provider<AssistUtils> provider3, Provider<PhoneStateMonitor> provider4, Provider<AssistHandleBehaviorController> provider5) {
        this.contextProvider = provider;
        this.uiEventLoggerProvider = provider2;
        this.assistUtilsProvider = provider3;
        this.phoneStateMonitorProvider = provider4;
        this.assistHandleBehaviorControllerProvider = provider5;
    }

    @Override // javax.inject.Provider
    public AssistLogger get() {
        return provideInstance(this.contextProvider, this.uiEventLoggerProvider, this.assistUtilsProvider, this.phoneStateMonitorProvider, this.assistHandleBehaviorControllerProvider);
    }

    public static AssistLogger provideInstance(Provider<Context> provider, Provider<UiEventLogger> provider2, Provider<AssistUtils> provider3, Provider<PhoneStateMonitor> provider4, Provider<AssistHandleBehaviorController> provider5) {
        return new AssistLogger(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get());
    }

    public static AssistLogger_Factory create(Provider<Context> provider, Provider<UiEventLogger> provider2, Provider<AssistUtils> provider3, Provider<PhoneStateMonitor> provider4, Provider<AssistHandleBehaviorController> provider5) {
        return new AssistLogger_Factory(provider, provider2, provider3, provider4, provider5);
    }
}
