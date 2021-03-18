package com.android.systemui.assist;

import com.android.systemui.assist.AssistHandleBehaviorController;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import java.util.Map;
import javax.inject.Provider;

public final class AssistModule_ProvideAssistHandleBehaviorControllerMapFactory implements Factory<Map<AssistHandleBehavior, AssistHandleBehaviorController.BehaviorController>> {
    private final Provider<AssistHandleLikeHomeBehavior> likeHomeBehaviorProvider;
    private final Provider<AssistHandleOffBehavior> offBehaviorProvider;
    private final Provider<AssistHandleReminderExpBehavior> reminderExpBehaviorProvider;

    public AssistModule_ProvideAssistHandleBehaviorControllerMapFactory(Provider<AssistHandleOffBehavior> provider, Provider<AssistHandleLikeHomeBehavior> provider2, Provider<AssistHandleReminderExpBehavior> provider3) {
        this.offBehaviorProvider = provider;
        this.likeHomeBehaviorProvider = provider2;
        this.reminderExpBehaviorProvider = provider3;
    }

    @Override // javax.inject.Provider
    public Map<AssistHandleBehavior, AssistHandleBehaviorController.BehaviorController> get() {
        return provideInstance(this.offBehaviorProvider, this.likeHomeBehaviorProvider, this.reminderExpBehaviorProvider);
    }

    public static Map<AssistHandleBehavior, AssistHandleBehaviorController.BehaviorController> provideInstance(Provider<AssistHandleOffBehavior> provider, Provider<AssistHandleLikeHomeBehavior> provider2, Provider<AssistHandleReminderExpBehavior> provider3) {
        return proxyProvideAssistHandleBehaviorControllerMap(provider.get(), provider2.get(), provider3.get());
    }

    public static AssistModule_ProvideAssistHandleBehaviorControllerMapFactory create(Provider<AssistHandleOffBehavior> provider, Provider<AssistHandleLikeHomeBehavior> provider2, Provider<AssistHandleReminderExpBehavior> provider3) {
        return new AssistModule_ProvideAssistHandleBehaviorControllerMapFactory(provider, provider2, provider3);
    }

    public static Map<AssistHandleBehavior, AssistHandleBehaviorController.BehaviorController> proxyProvideAssistHandleBehaviorControllerMap(Object obj, Object obj2, Object obj3) {
        Map<AssistHandleBehavior, AssistHandleBehaviorController.BehaviorController> provideAssistHandleBehaviorControllerMap = AssistModule.provideAssistHandleBehaviorControllerMap((AssistHandleOffBehavior) obj, (AssistHandleLikeHomeBehavior) obj2, (AssistHandleReminderExpBehavior) obj3);
        Preconditions.checkNotNull(provideAssistHandleBehaviorControllerMap, "Cannot return null from a non-@Nullable @Provides method");
        return provideAssistHandleBehaviorControllerMap;
    }
}
