package com.android.systemui.recents.events.activity;

import com.android.systemui.recents.events.RecentsEventBus;
import java.util.HashSet;

public class ThumbnailBlurPkgsChangedEvent extends RecentsEventBus.Event {
    public final HashSet<String> mThumbnailBlurPkgSet;

    public ThumbnailBlurPkgsChangedEvent(HashSet hashSet) {
        this.mThumbnailBlurPkgSet = hashSet;
    }
}
