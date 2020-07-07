package com.android.systemui.miui.statusbar.analytics;

public class Analytics$Event {
    protected String mEventName;

    public Analytics$Event(String str) {
        this.mEventName = str;
    }

    public String getEventName() {
        return this.mEventName;
    }
}
