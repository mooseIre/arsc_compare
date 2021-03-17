package com.android.systemui.tv;

import android.content.Context;
import com.android.systemui.SystemUIFactory;
import com.android.systemui.dagger.SystemUIRootComponent;
import com.android.systemui.tv.TvSystemUIRootComponent;

public class TvSystemUIFactory extends SystemUIFactory {
    /* access modifiers changed from: protected */
    @Override // com.android.systemui.SystemUIFactory
    public SystemUIRootComponent buildSystemUIRootComponent(Context context) {
        TvSystemUIRootComponent.Builder builder = DaggerTvSystemUIRootComponent.builder();
        builder.context(context);
        return builder.build();
    }
}
