package com.android.systemui.statusbar.notification.collection.coalescer;

import java.util.ArrayList;
import java.util.List;

public class EventBatch {
    Runnable mCancelShortTimeout;
    final long mCreatedTimestamp;
    final String mGroupKey;
    final List<CoalescedEvent> mMembers = new ArrayList();

    EventBatch(long j, String str) {
        this.mCreatedTimestamp = j;
        this.mGroupKey = str;
    }
}
