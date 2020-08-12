package com.android.systemui.statusbar.policy;

import android.content.Context;
import android.graphics.Rect;
import android.widget.ImageView;
import com.android.systemui.statusbar.phone.LightBarTransitionsController;

public interface DarkIconDispatcher {
    public static final int[] sTmpInt2 = new int[2];
    public static final Rect sTmpRect = new Rect();

    public interface DarkReceiver {
        void onDarkChanged(Rect rect, float f, int i);
    }

    void addDarkReceiver(ImageView imageView);

    void addDarkReceiver(DarkReceiver darkReceiver);

    void applyDark(Object obj);

    int getLightTintColor();

    LightBarTransitionsController getTransitionsController();

    void removeDarkReceiver(ImageView imageView);

    void removeDarkReceiver(DarkReceiver darkReceiver);

    void setIconsDarkArea(Rect rect);

    void updateResource(Context context);

    boolean useTint();
}
