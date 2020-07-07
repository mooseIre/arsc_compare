package com.android.systemui.settings;

public interface ToggleSlider {

    public interface Listener {
        void onChanged(ToggleSlider toggleSlider, boolean z, int i, boolean z2);

        void onInit(ToggleSlider toggleSlider);

        void onStart(int i);

        void onStop(int i);
    }

    int getValue();

    void setMax(int i);

    void setOnChangedListener(Listener listener);

    void setValue(int i);
}
