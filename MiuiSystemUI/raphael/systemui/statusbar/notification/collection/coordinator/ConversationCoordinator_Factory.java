package com.android.systemui.statusbar.notification.collection.coordinator;

import dagger.internal.Factory;

public final class ConversationCoordinator_Factory implements Factory<ConversationCoordinator> {
    private static final ConversationCoordinator_Factory INSTANCE = new ConversationCoordinator_Factory();

    @Override // javax.inject.Provider
    public ConversationCoordinator get() {
        return provideInstance();
    }

    public static ConversationCoordinator provideInstance() {
        return new ConversationCoordinator();
    }

    public static ConversationCoordinator_Factory create() {
        return INSTANCE;
    }
}
