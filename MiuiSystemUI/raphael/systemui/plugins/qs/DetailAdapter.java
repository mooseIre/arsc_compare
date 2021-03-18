package com.android.systemui.plugins.qs;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import com.android.internal.logging.UiEventLogger;
import com.android.systemui.plugins.annotations.ProvidesInterface;

@ProvidesInterface(version = 1)
public interface DetailAdapter {
    public static final UiEventLogger.UiEventEnum INVALID = $$Lambda$DetailAdapter$MIOR0XPfHL3Q0lRMWDu_BAc5K4g.INSTANCE;
    public static final int VERSION = 1;

    static /* synthetic */ default int lambda$static$0() {
        return 0;
    }

    View createDetailView(Context context, View view, ViewGroup viewGroup);

    default int getContainerHeight() {
        return -1;
    }

    int getMetricsCategory();

    Intent getSettingsIntent();

    CharSequence getTitle();

    default boolean getToggleEnabled() {
        return true;
    }

    Boolean getToggleState();

    default boolean hasHeader() {
        return true;
    }

    void setToggleState(boolean z);

    default UiEventLogger.UiEventEnum openDetailEvent() {
        return INVALID;
    }

    default UiEventLogger.UiEventEnum closeDetailEvent() {
        return INVALID;
    }

    default UiEventLogger.UiEventEnum moreSettingsEvent() {
        return INVALID;
    }
}
