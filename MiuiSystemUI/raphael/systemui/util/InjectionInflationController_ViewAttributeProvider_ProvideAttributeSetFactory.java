package com.android.systemui.util;

import android.util.AttributeSet;
import com.android.systemui.util.InjectionInflationController;
import dagger.internal.Factory;
import dagger.internal.Preconditions;

public final class InjectionInflationController_ViewAttributeProvider_ProvideAttributeSetFactory implements Factory<AttributeSet> {
    public static AttributeSet proxyProvideAttributeSet(InjectionInflationController.ViewAttributeProvider viewAttributeProvider) {
        AttributeSet provideAttributeSet = viewAttributeProvider.provideAttributeSet();
        Preconditions.checkNotNull(provideAttributeSet, "Cannot return null from a non-@Nullable @Provides method");
        return provideAttributeSet;
    }
}
