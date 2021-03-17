package com.android.systemui.settings;

public interface ToggleSlider {

    public interface Listener {
        void onChanged(ToggleSlider toggleSlider, boolean z, boolean z2, int i, boolean z3);

        void onInit(ToggleSlider toggleSlider);

        default void onStart(int i) {
        }

        default void onStop(int i) {
        }
    }

    int getValue();

    default boolean isChecked() {
        return false;
    }

    void setMax(int i);

    void setOnChangedListener(Listener listener);

    void setValue(int i);
}
