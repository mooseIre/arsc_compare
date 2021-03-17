package com.android.systemui.statusbar.notification.collection.coordinator;

import com.android.systemui.dump.DumpManager;
import com.android.systemui.statusbar.FeatureFlags;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class NotifCoordinators_Factory implements Factory<NotifCoordinators> {
    private final Provider<AppOpsCoordinator> appOpsCoordinatorProvider;
    private final Provider<BubbleCoordinator> bubbleCoordinatorProvider;
    private final Provider<ConversationCoordinator> conversationCoordinatorProvider;
    private final Provider<DeviceProvisionedCoordinator> deviceProvisionedCoordinatorProvider;
    private final Provider<DumpManager> dumpManagerProvider;
    private final Provider<FeatureFlags> featureFlagsProvider;
    private final Provider<HeadsUpCoordinator> headsUpCoordinatorProvider;
    private final Provider<HideNotifsForOtherUsersCoordinator> hideNotifsForOtherUsersCoordinatorProvider;
    private final Provider<KeyguardCoordinator> keyguardCoordinatorProvider;
    private final Provider<MediaCoordinator> mediaCoordinatorProvider;
    private final Provider<PreparationCoordinator> preparationCoordinatorProvider;
    private final Provider<RankingCoordinator> rankingCoordinatorProvider;

    public NotifCoordinators_Factory(Provider<DumpManager> provider, Provider<FeatureFlags> provider2, Provider<HideNotifsForOtherUsersCoordinator> provider3, Provider<KeyguardCoordinator> provider4, Provider<RankingCoordinator> provider5, Provider<AppOpsCoordinator> provider6, Provider<DeviceProvisionedCoordinator> provider7, Provider<BubbleCoordinator> provider8, Provider<HeadsUpCoordinator> provider9, Provider<ConversationCoordinator> provider10, Provider<PreparationCoordinator> provider11, Provider<MediaCoordinator> provider12) {
        this.dumpManagerProvider = provider;
        this.featureFlagsProvider = provider2;
        this.hideNotifsForOtherUsersCoordinatorProvider = provider3;
        this.keyguardCoordinatorProvider = provider4;
        this.rankingCoordinatorProvider = provider5;
        this.appOpsCoordinatorProvider = provider6;
        this.deviceProvisionedCoordinatorProvider = provider7;
        this.bubbleCoordinatorProvider = provider8;
        this.headsUpCoordinatorProvider = provider9;
        this.conversationCoordinatorProvider = provider10;
        this.preparationCoordinatorProvider = provider11;
        this.mediaCoordinatorProvider = provider12;
    }

    @Override // javax.inject.Provider
    public NotifCoordinators get() {
        return provideInstance(this.dumpManagerProvider, this.featureFlagsProvider, this.hideNotifsForOtherUsersCoordinatorProvider, this.keyguardCoordinatorProvider, this.rankingCoordinatorProvider, this.appOpsCoordinatorProvider, this.deviceProvisionedCoordinatorProvider, this.bubbleCoordinatorProvider, this.headsUpCoordinatorProvider, this.conversationCoordinatorProvider, this.preparationCoordinatorProvider, this.mediaCoordinatorProvider);
    }

    public static NotifCoordinators provideInstance(Provider<DumpManager> provider, Provider<FeatureFlags> provider2, Provider<HideNotifsForOtherUsersCoordinator> provider3, Provider<KeyguardCoordinator> provider4, Provider<RankingCoordinator> provider5, Provider<AppOpsCoordinator> provider6, Provider<DeviceProvisionedCoordinator> provider7, Provider<BubbleCoordinator> provider8, Provider<HeadsUpCoordinator> provider9, Provider<ConversationCoordinator> provider10, Provider<PreparationCoordinator> provider11, Provider<MediaCoordinator> provider12) {
        return new NotifCoordinators(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get(), provider6.get(), provider7.get(), provider8.get(), provider9.get(), provider10.get(), provider11.get(), provider12.get());
    }

    public static NotifCoordinators_Factory create(Provider<DumpManager> provider, Provider<FeatureFlags> provider2, Provider<HideNotifsForOtherUsersCoordinator> provider3, Provider<KeyguardCoordinator> provider4, Provider<RankingCoordinator> provider5, Provider<AppOpsCoordinator> provider6, Provider<DeviceProvisionedCoordinator> provider7, Provider<BubbleCoordinator> provider8, Provider<HeadsUpCoordinator> provider9, Provider<ConversationCoordinator> provider10, Provider<PreparationCoordinator> provider11, Provider<MediaCoordinator> provider12) {
        return new NotifCoordinators_Factory(provider, provider2, provider3, provider4, provider5, provider6, provider7, provider8, provider9, provider10, provider11, provider12);
    }
}
