package com.android.systemui.bubbles;

import android.graphics.Bitmap;
import android.graphics.Path;
import android.view.View;

/* access modifiers changed from: package-private */
public interface BubbleViewProvider {
    Bitmap getBadgedImage();

    int getDisplayId();

    int getDotColor();

    Path getDotPath();

    BubbleExpandedView getExpandedView();

    View getIconView();

    String getKey();

    void logUIEvent(int i, int i2, float f, float f2, int i3);

    void setContentVisibility(boolean z);

    boolean showDot();
}
