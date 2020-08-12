package com.android.systemui.plugins;

import android.content.Context;
import com.android.systemui.plugins.annotations.DependsOn;
import com.android.systemui.plugins.annotations.ProvidesInterface;

@DependsOn(target = FalsingManager.class)
@ProvidesInterface(action = "com.android.systemui.action.FALSING_PLUGIN", version = 2)
public interface FalsingPlugin extends Plugin {
    public static final String ACTION = "com.android.systemui.action.FALSING_PLUGIN";
    public static final int VERSION = 2;

    void dataCollected(boolean z, byte[] bArr) {
    }

    FalsingManager getFalsingManager(Context context) {
        return null;
    }
}
