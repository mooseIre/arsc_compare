package com.android.systemui.dagger;

import android.view.Choreographer;
import dagger.internal.Factory;
import dagger.internal.Preconditions;

public final class DependencyProvider_ProvidesChoreographerFactory implements Factory<Choreographer> {
    private final DependencyProvider module;

    public DependencyProvider_ProvidesChoreographerFactory(DependencyProvider dependencyProvider) {
        this.module = dependencyProvider;
    }

    @Override // javax.inject.Provider
    public Choreographer get() {
        return provideInstance(this.module);
    }

    public static Choreographer provideInstance(DependencyProvider dependencyProvider) {
        return proxyProvidesChoreographer(dependencyProvider);
    }

    public static DependencyProvider_ProvidesChoreographerFactory create(DependencyProvider dependencyProvider) {
        return new DependencyProvider_ProvidesChoreographerFactory(dependencyProvider);
    }

    public static Choreographer proxyProvidesChoreographer(DependencyProvider dependencyProvider) {
        Choreographer providesChoreographer = dependencyProvider.providesChoreographer();
        Preconditions.checkNotNull(providesChoreographer, "Cannot return null from a non-@Nullable @Provides method");
        return providesChoreographer;
    }
}
