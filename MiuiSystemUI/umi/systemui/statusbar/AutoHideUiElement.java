package com.android.systemui.statusbar;

public interface AutoHideUiElement {
    void hide();

    boolean isVisible();

    boolean shouldHideOnTouch() {
        return true;
    }

    void synchronizeState();
}
