package com.android.systemui.assist;

import android.content.Context;
import android.os.Handler;
import android.view.accessibility.AccessibilityManager;
import com.android.internal.app.AssistUtils;
import com.android.systemui.assist.AssistHandleBehaviorController;
import com.android.systemui.dump.DumpManager;
import com.android.systemui.statusbar.phone.NavigationModeController;
import dagger.internal.DoubleCheck;
import dagger.internal.Factory;
import java.util.Map;
import javax.inject.Provider;

public final class AssistHandleBehaviorController_Factory implements Factory<AssistHandleBehaviorController> {
    private final Provider<AccessibilityManager> a11yManagerProvider;
    private final Provider<AssistHandleViewController> assistHandleViewControllerProvider;
    private final Provider<AssistUtils> assistUtilsProvider;
    private final Provider<Map<AssistHandleBehavior, AssistHandleBehaviorController.BehaviorController>> behaviorMapProvider;
    private final Provider<Context> contextProvider;
    private final Provider<DeviceConfigHelper> deviceConfigHelperProvider;
    private final Provider<DumpManager> dumpManagerProvider;
    private final Provider<Handler> handlerProvider;
    private final Provider<NavigationModeController> navigationModeControllerProvider;

    public AssistHandleBehaviorController_Factory(Provider<Context> provider, Provider<AssistUtils> provider2, Provider<Handler> provider3, Provider<AssistHandleViewController> provider4, Provider<DeviceConfigHelper> provider5, Provider<Map<AssistHandleBehavior, AssistHandleBehaviorController.BehaviorController>> provider6, Provider<NavigationModeController> provider7, Provider<AccessibilityManager> provider8, Provider<DumpManager> provider9) {
        this.contextProvider = provider;
        this.assistUtilsProvider = provider2;
        this.handlerProvider = provider3;
        this.assistHandleViewControllerProvider = provider4;
        this.deviceConfigHelperProvider = provider5;
        this.behaviorMapProvider = provider6;
        this.navigationModeControllerProvider = provider7;
        this.a11yManagerProvider = provider8;
        this.dumpManagerProvider = provider9;
    }

    @Override // javax.inject.Provider
    public AssistHandleBehaviorController get() {
        return provideInstance(this.contextProvider, this.assistUtilsProvider, this.handlerProvider, this.assistHandleViewControllerProvider, this.deviceConfigHelperProvider, this.behaviorMapProvider, this.navigationModeControllerProvider, this.a11yManagerProvider, this.dumpManagerProvider);
    }

    public static AssistHandleBehaviorController provideInstance(Provider<Context> provider, Provider<AssistUtils> provider2, Provider<Handler> provider3, Provider<AssistHandleViewController> provider4, Provider<DeviceConfigHelper> provider5, Provider<Map<AssistHandleBehavior, AssistHandleBehaviorController.BehaviorController>> provider6, Provider<NavigationModeController> provider7, Provider<AccessibilityManager> provider8, Provider<DumpManager> provider9) {
        return new AssistHandleBehaviorController(provider.get(), provider2.get(), provider3.get(), provider4, provider5.get(), provider6.get(), provider7.get(), DoubleCheck.lazy(provider8), provider9.get());
    }

    public static AssistHandleBehaviorController_Factory create(Provider<Context> provider, Provider<AssistUtils> provider2, Provider<Handler> provider3, Provider<AssistHandleViewController> provider4, Provider<DeviceConfigHelper> provider5, Provider<Map<AssistHandleBehavior, AssistHandleBehaviorController.BehaviorController>> provider6, Provider<NavigationModeController> provider7, Provider<AccessibilityManager> provider8, Provider<DumpManager> provider9) {
        return new AssistHandleBehaviorController_Factory(provider, provider2, provider3, provider4, provider5, provider6, provider7, provider8, provider9);
    }
}
