package com.android.systemui;

import dagger.internal.Factory;

public final class TransactionPool_Factory implements Factory<TransactionPool> {
    private static final TransactionPool_Factory INSTANCE = new TransactionPool_Factory();

    @Override // javax.inject.Provider
    public TransactionPool get() {
        return provideInstance();
    }

    public static TransactionPool provideInstance() {
        return new TransactionPool();
    }

    public static TransactionPool_Factory create() {
        return INSTANCE;
    }
}
