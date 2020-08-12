package com.android.systemui.screenshot;

import android.net.Uri;
import com.android.systemui.screenshot.GlobalScreenshot;

/* compiled from: GlobalScreenshot */
class NotifyMediaStoreData {
    public GlobalScreenshot.ScreenshotFinishCallback finishCallback;
    public Runnable finisher;
    public int height;
    public String imageFileName;
    public String imageFilePath;
    public boolean isPending;
    public boolean isRunned;
    public Uri outUri;
    public boolean saveFinished;
    public long takenTime;
    public int width;

    NotifyMediaStoreData() {
    }
}
