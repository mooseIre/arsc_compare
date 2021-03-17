package com.android.systemui.statusbar.phone;

public interface PanelExpansionListener {
    void onPanelExpansionChanged(float f, boolean z);

    default void onQsExpansionChanged(float f) {
    }
}
