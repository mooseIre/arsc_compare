package com.android.systemui.screenshot;

import android.app.Notification;
import android.content.ComponentName;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.UserHandle;
import android.util.Log;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ScreenshotNotificationSmartActionsProvider {

    protected enum ScreenshotOp {
        OP_UNKNOWN,
        RETRIEVE_SMART_ACTIONS,
        REQUEST_SMART_ACTIONS,
        WAIT_FOR_SMART_ACTIONS
    }

    protected enum ScreenshotOpStatus {
        OP_STATUS_UNKNOWN,
        SUCCESS,
        ERROR,
        TIMEOUT
    }

    public CompletableFuture<List<Notification.Action>> getActions(String str, Uri uri, Bitmap bitmap, ComponentName componentName, UserHandle userHandle) {
        Log.d("ScreenshotActions", "Returning empty smart action list.");
        return CompletableFuture.completedFuture(Collections.emptyList());
    }

    public void notifyOp(String str, ScreenshotOp screenshotOp, ScreenshotOpStatus screenshotOpStatus, long j) {
        Log.d("ScreenshotActions", "Return without notify.");
    }

    public void notifyAction(String str, String str2, boolean z) {
        Log.d("ScreenshotActions", "Return without notify.");
    }
}
