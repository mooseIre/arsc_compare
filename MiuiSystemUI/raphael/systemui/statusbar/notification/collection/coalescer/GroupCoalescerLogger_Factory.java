package com.android.systemui.statusbar.notification.collection.coalescer;

import com.android.systemui.log.LogBuffer;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class GroupCoalescerLogger_Factory implements Factory<GroupCoalescerLogger> {
    private final Provider<LogBuffer> bufferProvider;

    public GroupCoalescerLogger_Factory(Provider<LogBuffer> provider) {
        this.bufferProvider = provider;
    }

    @Override // javax.inject.Provider
    public GroupCoalescerLogger get() {
        return provideInstance(this.bufferProvider);
    }

    public static GroupCoalescerLogger provideInstance(Provider<LogBuffer> provider) {
        return new GroupCoalescerLogger(provider.get());
    }

    public static GroupCoalescerLogger_Factory create(Provider<LogBuffer> provider) {
        return new GroupCoalescerLogger_Factory(provider);
    }
}
