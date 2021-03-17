package com.android.systemui.settings;

public interface ToggleSlider {

    public interface Listener {
        void onChanged(ToggleSlider toggleSlider, boolean z, boolean z2, int i, boolean z3);

        void onInit(ToggleSlider toggleSlider);

        void onStart(int i) {
        }

        void onStop(int i) {
        }
    }

    int getValue();

    boolean isChecked() {
        return false;
    }

    void setMax(int i);

    void setOnChangedListener(Listener listener);

    void setValue(int i);
}
