package com.android.systemui.plugins.qs;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import com.android.systemui.plugins.annotations.ProvidesInterface;

@ProvidesInterface(version = 1)
public interface DetailAdapter {
    public static final int VERSION = 1;

    View createDetailView(Context context, View view, ViewGroup viewGroup);

    int getContainerHeight() {
        return -1;
    }

    int getMetricsCategory();

    Intent getSettingsIntent();

    CharSequence getTitle();

    boolean getToggleEnabled();

    Boolean getToggleState();

    boolean hasHeader();

    boolean hasSwitch() {
        return true;
    }

    void setToggleState(boolean z);
}
