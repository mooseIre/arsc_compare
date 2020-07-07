package com.android.systemui.partialscreenshot;

import android.net.Uri;
import com.android.systemui.partialscreenshot.PartialScreenshot;

public class PartialNotifyMediaStoreData {
    public PartialScreenshot.PartialScreenshotFinishCallback finishCallback;
    public int height;
    public String imageFileName;
    public String imageFilePath;
    public boolean isPending;
    public boolean isRunned;
    public Uri outUri;
    public boolean saveFinished;
    public long takenTime;
    public int width;
}
