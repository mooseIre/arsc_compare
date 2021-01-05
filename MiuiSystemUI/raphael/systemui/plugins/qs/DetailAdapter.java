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

    static /* synthetic */ int lambda$static$0() {
        return 0;
    }

    View createDetailView(Context context, View view, ViewGroup viewGroup);

    int getContainerHeight() {
        return -1;
    }

    int getMetricsCategory();

    Intent getSettingsIntent();

    CharSequence getTitle();

    boolean getToggleEnabled() {
        return true;
    }

    Boolean getToggleState();

    boolean hasHeader() {
        return true;
    }

    void setToggleState(boolean z);

    UiEventLogger.UiEventEnum openDetailEvent() {
        return INVALID;
    }

    UiEventLogger.UiEventEnum closeDetailEvent() {
        return INVALID;
    }

    UiEventLogger.UiEventEnum moreSettingsEvent() {
        return INVALID;
    }
}
