package com.android.systemui.statusbar.notification.people;

import android.content.Context;
import android.content.pm.LauncherApps;
import android.content.pm.PackageManager;
import android.os.UserManager;
import com.android.systemui.statusbar.NotificationListener;
import com.android.systemui.statusbar.NotificationLockscreenUserManager;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import dagger.internal.Factory;
import java.util.concurrent.Executor;
import javax.inject.Provider;

public final class PeopleHubDataSourceImpl_Factory implements Factory<PeopleHubDataSourceImpl> {
    private final Provider<Executor> bgExecutorProvider;
    private final Provider<Context> contextProvider;
    private final Provider<NotificationPersonExtractor> extractorProvider;
    private final Provider<LauncherApps> launcherAppsProvider;
    private final Provider<Executor> mainExecutorProvider;
    private final Provider<NotificationLockscreenUserManager> notifLockscreenUserMgrProvider;
    private final Provider<NotificationEntryManager> notificationEntryManagerProvider;
    private final Provider<NotificationListener> notificationListenerProvider;
    private final Provider<PackageManager> packageManagerProvider;
    private final Provider<PeopleNotificationIdentifier> peopleNotificationIdentifierProvider;
    private final Provider<UserManager> userManagerProvider;

    public PeopleHubDataSourceImpl_Factory(Provider<NotificationEntryManager> provider, Provider<NotificationPersonExtractor> provider2, Provider<UserManager> provider3, Provider<LauncherApps> provider4, Provider<PackageManager> provider5, Provider<Context> provider6, Provider<NotificationListener> provider7, Provider<Executor> provider8, Provider<Executor> provider9, Provider<NotificationLockscreenUserManager> provider10, Provider<PeopleNotificationIdentifier> provider11) {
        this.notificationEntryManagerProvider = provider;
        this.extractorProvider = provider2;
        this.userManagerProvider = provider3;
        this.launcherAppsProvider = provider4;
        this.packageManagerProvider = provider5;
        this.contextProvider = provider6;
        this.notificationListenerProvider = provider7;
        this.bgExecutorProvider = provider8;
        this.mainExecutorProvider = provider9;
        this.notifLockscreenUserMgrProvider = provider10;
        this.peopleNotificationIdentifierProvider = provider11;
    }

    @Override // javax.inject.Provider
    public PeopleHubDataSourceImpl get() {
        return provideInstance(this.notificationEntryManagerProvider, this.extractorProvider, this.userManagerProvider, this.launcherAppsProvider, this.packageManagerProvider, this.contextProvider, this.notificationListenerProvider, this.bgExecutorProvider, this.mainExecutorProvider, this.notifLockscreenUserMgrProvider, this.peopleNotificationIdentifierProvider);
    }

    public static PeopleHubDataSourceImpl provideInstance(Provider<NotificationEntryManager> provider, Provider<NotificationPersonExtractor> provider2, Provider<UserManager> provider3, Provider<LauncherApps> provider4, Provider<PackageManager> provider5, Provider<Context> provider6, Provider<NotificationListener> provider7, Provider<Executor> provider8, Provider<Executor> provider9, Provider<NotificationLockscreenUserManager> provider10, Provider<PeopleNotificationIdentifier> provider11) {
        return new PeopleHubDataSourceImpl(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get(), provider6.get(), provider7.get(), provider8.get(), provider9.get(), provider10.get(), provider11.get());
    }

    public static PeopleHubDataSourceImpl_Factory create(Provider<NotificationEntryManager> provider, Provider<NotificationPersonExtractor> provider2, Provider<UserManager> provider3, Provider<LauncherApps> provider4, Provider<PackageManager> provider5, Provider<Context> provider6, Provider<NotificationListener> provider7, Provider<Executor> provider8, Provider<Executor> provider9, Provider<NotificationLockscreenUserManager> provider10, Provider<PeopleNotificationIdentifier> provider11) {
        return new PeopleHubDataSourceImpl_Factory(provider, provider2, provider3, provider4, provider5, provider6, provider7, provider8, provider9, provider10, provider11);
    }
}
