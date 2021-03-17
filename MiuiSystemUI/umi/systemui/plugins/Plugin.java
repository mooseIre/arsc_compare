package com.android.systemui.plugins;

import android.content.Context;

public interface Plugin {
    default int getVersion() {
        return -1;
    }

    default void onCreate(Context context, Context context2) {
    }

    default void onDestroy() {
    }
}
