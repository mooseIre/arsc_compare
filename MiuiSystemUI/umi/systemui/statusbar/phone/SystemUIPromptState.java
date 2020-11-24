package com.android.systemui.statusbar.phone;

import android.widget.RemoteViews;

public class SystemUIPromptState {
    public RemoteViews mMiniStateViews;
    public int mPriority;

    public SystemUIPromptState(String str, RemoteViews remoteViews, RemoteViews remoteViews2, int i) {
        this.mMiniStateViews = remoteViews2;
        this.mPriority = Math.max(Math.min(i, 3), 0);
    }
}
