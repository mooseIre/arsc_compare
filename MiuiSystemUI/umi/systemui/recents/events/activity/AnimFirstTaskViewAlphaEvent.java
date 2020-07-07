package com.android.systemui.recents.events.activity;

import com.android.systemui.recents.events.RecentsEventBus;

public class AnimFirstTaskViewAlphaEvent extends RecentsEventBus.Event {
    public final float mAlpha;
    public final boolean mKeepAlphaWhenRelayout;
    public final boolean mWithAnim;

    public AnimFirstTaskViewAlphaEvent(float f, boolean z) {
        this.mAlpha = f;
        this.mWithAnim = z;
        this.mKeepAlphaWhenRelayout = false;
    }

    public AnimFirstTaskViewAlphaEvent(float f, boolean z, boolean z2) {
        this.mAlpha = f;
        this.mWithAnim = z;
        this.mKeepAlphaWhenRelayout = z2;
    }

    public String description() {
        return "mAlpha=" + this.mAlpha + " mWithAnim=" + this.mWithAnim + " mKeepAlphaWhenRelayout=" + this.mKeepAlphaWhenRelayout;
    }
}
