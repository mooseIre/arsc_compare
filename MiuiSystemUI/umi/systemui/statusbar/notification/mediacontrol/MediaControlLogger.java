package com.android.systemui.statusbar.notification.mediacontrol;

import android.app.PendingIntent;
import com.android.systemui.log.LogBuffer;
import com.android.systemui.log.LogLevel;
import com.android.systemui.log.LogMessageImpl;
import com.android.systemui.media.MediaData;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: MediaControlLogger.kt */
public final class MediaControlLogger {
    private final String TAG = "MediaControlLogger";
    private final LogBuffer buffer;

    public MediaControlLogger(@NotNull LogBuffer logBuffer) {
        Intrinsics.checkParameterIsNotNull(logBuffer, "buffer");
        this.buffer = logBuffer;
    }

    public final void logDataNotCurrentUser(@NotNull MediaData mediaData) {
        Intrinsics.checkParameterIsNotNull(mediaData, "data");
        LogBuffer logBuffer = this.buffer;
        String str = this.TAG;
        LogLevel logLevel = LogLevel.DEBUG;
        MediaControlLogger$logDataNotCurrentUser$2 mediaControlLogger$logDataNotCurrentUser$2 = MediaControlLogger$logDataNotCurrentUser$2.INSTANCE;
        if (!logBuffer.getFrozen()) {
            LogMessageImpl obtain = logBuffer.obtain(str, logLevel, mediaControlLogger$logDataNotCurrentUser$2);
            obtain.setStr1(String.valueOf(mediaData.getNotificationKey()));
            obtain.setStr2(String.valueOf(mediaData.getUserId()));
            logBuffer.push(obtain);
        }
    }

    public final void logMediaHostVisibilityChanged(boolean z, int i) {
        LogBuffer logBuffer = this.buffer;
        String str = this.TAG;
        LogLevel logLevel = LogLevel.DEBUG;
        MediaControlLogger$logMediaHostVisibilityChanged$2 mediaControlLogger$logMediaHostVisibilityChanged$2 = MediaControlLogger$logMediaHostVisibilityChanged$2.INSTANCE;
        if (!logBuffer.getFrozen()) {
            LogMessageImpl obtain = logBuffer.obtain(str, logLevel, mediaControlLogger$logMediaHostVisibilityChanged$2);
            obtain.setBool1(z);
            obtain.setInt1(i);
            logBuffer.push(obtain);
        }
    }

    public final void logMediaAction(@NotNull String str) {
        Intrinsics.checkParameterIsNotNull(str, "allActions");
        LogBuffer logBuffer = this.buffer;
        String str2 = this.TAG;
        LogLevel logLevel = LogLevel.DEBUG;
        MediaControlLogger$logMediaAction$2 mediaControlLogger$logMediaAction$2 = MediaControlLogger$logMediaAction$2.INSTANCE;
        if (!logBuffer.getFrozen()) {
            LogMessageImpl obtain = logBuffer.obtain(str2, logLevel, mediaControlLogger$logMediaAction$2);
            obtain.setStr1(str);
            logBuffer.push(obtain);
        }
    }

    public final void logMediaActionClicked(@NotNull PendingIntent pendingIntent) {
        Intrinsics.checkParameterIsNotNull(pendingIntent, "pendingIntent");
        LogBuffer logBuffer = this.buffer;
        String str = this.TAG;
        LogLevel logLevel = LogLevel.DEBUG;
        MediaControlLogger$logMediaActionClicked$2 mediaControlLogger$logMediaActionClicked$2 = MediaControlLogger$logMediaActionClicked$2.INSTANCE;
        if (!logBuffer.getFrozen()) {
            LogMessageImpl obtain = logBuffer.obtain(str, logLevel, mediaControlLogger$logMediaActionClicked$2);
            obtain.setStr1(String.valueOf(pendingIntent));
            logBuffer.push(obtain);
        }
    }

    public final void logMediaActionClicked(@NotNull String str) {
        Intrinsics.checkParameterIsNotNull(str, "string");
        LogBuffer logBuffer = this.buffer;
        String str2 = this.TAG;
        LogLevel logLevel = LogLevel.DEBUG;
        MediaControlLogger$logMediaActionClicked$4 mediaControlLogger$logMediaActionClicked$4 = MediaControlLogger$logMediaActionClicked$4.INSTANCE;
        if (!logBuffer.getFrozen()) {
            LogMessageImpl obtain = logBuffer.obtain(str2, logLevel, mediaControlLogger$logMediaActionClicked$4);
            obtain.setStr1(str);
            logBuffer.push(obtain);
        }
    }
}
