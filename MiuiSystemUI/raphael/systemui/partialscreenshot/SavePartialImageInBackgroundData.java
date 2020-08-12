package com.android.systemui.partialscreenshot;

import android.content.Context;
import android.graphics.Bitmap;

public class SavePartialImageInBackgroundData {
    Context context;
    Runnable finisher;
    Bitmap image;
    int result;
}
