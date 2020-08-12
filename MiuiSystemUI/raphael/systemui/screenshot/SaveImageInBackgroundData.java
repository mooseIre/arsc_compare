package com.android.systemui.screenshot;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;

/* compiled from: GlobalScreenshot */
class SaveImageInBackgroundData {
    boolean canLongScreenshot;
    Context context;
    Runnable finisher;
    int iconSize;
    Bitmap image;
    boolean orientationLandscape;
    int result;
    View screenLongShotView;
    GlobalScreenshotDisplay screenshotDisplay;

    SaveImageInBackgroundData() {
    }
}
