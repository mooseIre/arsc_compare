package com.android.systemui.recents.events.ui;

import android.util.MutableInt;
import com.android.systemui.recents.events.RecentsEventBus;

public class StackViewScrolledEvent extends RecentsEventBus.ReusableEvent {
    public final MutableInt yMovement = new MutableInt(0);

    public void updateY(int i) {
        this.yMovement.value = i;
    }
}
