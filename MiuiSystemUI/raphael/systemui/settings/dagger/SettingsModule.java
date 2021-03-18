package com.android.systemui.settings.dagger;

import android.content.Context;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.settings.CurrentUserContextTracker;

public interface SettingsModule {
    static default CurrentUserContextTracker provideCurrentUserContextTracker(Context context, BroadcastDispatcher broadcastDispatcher) {
        CurrentUserContextTracker currentUserContextTracker = new CurrentUserContextTracker(context, broadcastDispatcher);
        currentUserContextTracker.initialize();
        return currentUserContextTracker;
    }
}
