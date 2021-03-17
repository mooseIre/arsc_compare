package com.android.systemui.plugins;

import android.content.Context;

public interface Plugin {
    int getVersion() {
        return -1;
    }

    void onCreate(Context context, Context context2) {
    }

    void onDestroy() {
    }
}
