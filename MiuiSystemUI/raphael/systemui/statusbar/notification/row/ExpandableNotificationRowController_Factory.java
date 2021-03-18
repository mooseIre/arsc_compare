package com.android.systemui.statusbar.notification.row;

import com.android.systemui.plugins.FalsingManager;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.shared.plugins.PluginManager;
import com.android.systemui.statusbar.NotificationMediaManager;
import com.android.systemui.statusbar.notification.logging.NotificationLogger;
import com.android.systemui.statusbar.notification.people.PeopleNotificationIdentifier;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import com.android.systemui.statusbar.phone.KeyguardBypassController;
import com.android.systemui.statusbar.phone.NotificationGroupManager;
import com.android.systemui.statusbar.policy.HeadsUpManager;
import com.android.systemui.util.time.SystemClock;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class ExpandableNotificationRowController_Factory implements Factory<ExpandableNotificationRowController> {
    private final Provider<ActivatableNotificationViewController> activatableNotificationViewControllerProvider;
    private final Provider<Boolean> allowLongPressProvider;
    private final Provider<String> appNameProvider;
    private final Provider<SystemClock> clockProvider;
    private final Provider<FalsingManager> falsingManagerProvider;
    private final Provider<HeadsUpManager> headsUpManagerProvider;
    private final Provider<KeyguardBypassController> keyguardBypassControllerProvider;
    private final Provider<NotificationMediaManager> mediaManagerProvider;
    private final Provider<NotificationGroupManager> notificationGroupManagerProvider;
    private final Provider<NotificationGutsManager> notificationGutsManagerProvider;
    private final Provider<String> notificationKeyProvider;
    private final Provider<NotificationLogger> notificationLoggerProvider;
    private final Provider<Runnable> onDismissRunnableProvider;
    private final Provider<ExpandableNotificationRow.OnExpandClickListener> onExpandClickListenerProvider;
    private final Provider<PeopleNotificationIdentifier> peopleNotificationIdentifierProvider;
    private final Provider<PluginManager> pluginManagerProvider;
    private final Provider<RowContentBindStage> rowContentBindStageProvider;
    private final Provider<StatusBarStateController> statusBarStateControllerProvider;
    private final Provider<ExpandableNotificationRow> viewProvider;

    public ExpandableNotificationRowController_Factory(Provider<ExpandableNotificationRow> provider, Provider<ActivatableNotificationViewController> provider2, Provider<NotificationMediaManager> provider3, Provider<PluginManager> provider4, Provider<SystemClock> provider5, Provider<String> provider6, Provider<String> provider7, Provider<KeyguardBypassController> provider8, Provider<NotificationGroupManager> provider9, Provider<RowContentBindStage> provider10, Provider<NotificationLogger> provider11, Provider<HeadsUpManager> provider12, Provider<ExpandableNotificationRow.OnExpandClickListener> provider13, Provider<StatusBarStateController> provider14, Provider<NotificationGutsManager> provider15, Provider<Boolean> provider16, Provider<Runnable> provider17, Provider<FalsingManager> provider18, Provider<PeopleNotificationIdentifier> provider19) {
        this.viewProvider = provider;
        this.activatableNotificationViewControllerProvider = provider2;
        this.mediaManagerProvider = provider3;
        this.pluginManagerProvider = provider4;
        this.clockProvider = provider5;
        this.appNameProvider = provider6;
        this.notificationKeyProvider = provider7;
        this.keyguardBypassControllerProvider = provider8;
        this.notificationGroupManagerProvider = provider9;
        this.rowContentBindStageProvider = provider10;
        this.notificationLoggerProvider = provider11;
        this.headsUpManagerProvider = provider12;
        this.onExpandClickListenerProvider = provider13;
        this.statusBarStateControllerProvider = provider14;
        this.notificationGutsManagerProvider = provider15;
        this.allowLongPressProvider = provider16;
        this.onDismissRunnableProvider = provider17;
        this.falsingManagerProvider = provider18;
        this.peopleNotificationIdentifierProvider = provider19;
    }

    @Override // javax.inject.Provider
    public ExpandableNotificationRowController get() {
        return provideInstance(this.viewProvider, this.activatableNotificationViewControllerProvider, this.mediaManagerProvider, this.pluginManagerProvider, this.clockProvider, this.appNameProvider, this.notificationKeyProvider, this.keyguardBypassControllerProvider, this.notificationGroupManagerProvider, this.rowContentBindStageProvider, this.notificationLoggerProvider, this.headsUpManagerProvider, this.onExpandClickListenerProvider, this.statusBarStateControllerProvider, this.notificationGutsManagerProvider, this.allowLongPressProvider, this.onDismissRunnableProvider, this.falsingManagerProvider, this.peopleNotificationIdentifierProvider);
    }

    public static ExpandableNotificationRowController provideInstance(Provider<ExpandableNotificationRow> provider, Provider<ActivatableNotificationViewController> provider2, Provider<NotificationMediaManager> provider3, Provider<PluginManager> provider4, Provider<SystemClock> provider5, Provider<String> provider6, Provider<String> provider7, Provider<KeyguardBypassController> provider8, Provider<NotificationGroupManager> provider9, Provider<RowContentBindStage> provider10, Provider<NotificationLogger> provider11, Provider<HeadsUpManager> provider12, Provider<ExpandableNotificationRow.OnExpandClickListener> provider13, Provider<StatusBarStateController> provider14, Provider<NotificationGutsManager> provider15, Provider<Boolean> provider16, Provider<Runnable> provider17, Provider<FalsingManager> provider18, Provider<PeopleNotificationIdentifier> provider19) {
        return new ExpandableNotificationRowController(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get(), provider6.get(), provider7.get(), provider8.get(), provider9.get(), provider10.get(), provider11.get(), provider12.get(), provider13.get(), provider14.get(), provider15.get(), provider16.get().booleanValue(), provider17.get(), provider18.get(), provider19.get());
    }

    public static ExpandableNotificationRowController_Factory create(Provider<ExpandableNotificationRow> provider, Provider<ActivatableNotificationViewController> provider2, Provider<NotificationMediaManager> provider3, Provider<PluginManager> provider4, Provider<SystemClock> provider5, Provider<String> provider6, Provider<String> provider7, Provider<KeyguardBypassController> provider8, Provider<NotificationGroupManager> provider9, Provider<RowContentBindStage> provider10, Provider<NotificationLogger> provider11, Provider<HeadsUpManager> provider12, Provider<ExpandableNotificationRow.OnExpandClickListener> provider13, Provider<StatusBarStateController> provider14, Provider<NotificationGutsManager> provider15, Provider<Boolean> provider16, Provider<Runnable> provider17, Provider<FalsingManager> provider18, Provider<PeopleNotificationIdentifier> provider19) {
        return new ExpandableNotificationRowController_Factory(provider, provider2, provider3, provider4, provider5, provider6, provider7, provider8, provider9, provider10, provider11, provider12, provider13, provider14, provider15, provider16, provider17, provider18, provider19);
    }
}
