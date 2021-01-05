package com.android.systemui.statusbar.notification;

import android.app.NotificationChannel;
import android.service.notification.NotificationListenerService;
import com.android.systemui.log.LogBuffer;
import com.android.systemui.log.LogLevel;
import com.android.systemui.log.LogMessageImpl;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: NotificationClickerLogger.kt */
public final class NotificationClickerLogger {
    private final LogBuffer buffer;

    public NotificationClickerLogger(@NotNull LogBuffer logBuffer) {
        Intrinsics.checkParameterIsNotNull(logBuffer, "buffer");
        this.buffer = logBuffer;
    }

    public final void logOnClick(@NotNull NotificationEntry notificationEntry) {
        Intrinsics.checkParameterIsNotNull(notificationEntry, "entry");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.DEBUG;
        NotificationClickerLogger$logOnClick$2 notificationClickerLogger$logOnClick$2 = NotificationClickerLogger$logOnClick$2.INSTANCE;
        if (!logBuffer.getFrozen()) {
            LogMessageImpl obtain = logBuffer.obtain("NotificationClicker", logLevel, notificationClickerLogger$logOnClick$2);
            obtain.setStr1(notificationEntry.getKey());
            NotificationListenerService.Ranking ranking = notificationEntry.getRanking();
            Intrinsics.checkExpressionValueIsNotNull(ranking, "entry.ranking");
            NotificationChannel channel = ranking.getChannel();
            Intrinsics.checkExpressionValueIsNotNull(channel, "entry.ranking.channel");
            obtain.setStr2(channel.getId());
            logBuffer.push(obtain);
        }
    }

    public final void logMenuVisible(@NotNull NotificationEntry notificationEntry) {
        Intrinsics.checkParameterIsNotNull(notificationEntry, "entry");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.DEBUG;
        NotificationClickerLogger$logMenuVisible$2 notificationClickerLogger$logMenuVisible$2 = NotificationClickerLogger$logMenuVisible$2.INSTANCE;
        if (!logBuffer.getFrozen()) {
            LogMessageImpl obtain = logBuffer.obtain("NotificationClicker", logLevel, notificationClickerLogger$logMenuVisible$2);
            obtain.setStr1(notificationEntry.getKey());
            logBuffer.push(obtain);
        }
    }

    public final void logParentMenuVisible(@NotNull NotificationEntry notificationEntry) {
        Intrinsics.checkParameterIsNotNull(notificationEntry, "entry");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.DEBUG;
        NotificationClickerLogger$logParentMenuVisible$2 notificationClickerLogger$logParentMenuVisible$2 = NotificationClickerLogger$logParentMenuVisible$2.INSTANCE;
        if (!logBuffer.getFrozen()) {
            LogMessageImpl obtain = logBuffer.obtain("NotificationClicker", logLevel, notificationClickerLogger$logParentMenuVisible$2);
            obtain.setStr1(notificationEntry.getKey());
            logBuffer.push(obtain);
        }
    }

    public final void logChildrenExpanded(@NotNull NotificationEntry notificationEntry) {
        Intrinsics.checkParameterIsNotNull(notificationEntry, "entry");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.DEBUG;
        NotificationClickerLogger$logChildrenExpanded$2 notificationClickerLogger$logChildrenExpanded$2 = NotificationClickerLogger$logChildrenExpanded$2.INSTANCE;
        if (!logBuffer.getFrozen()) {
            LogMessageImpl obtain = logBuffer.obtain("NotificationClicker", logLevel, notificationClickerLogger$logChildrenExpanded$2);
            obtain.setStr1(notificationEntry.getKey());
            logBuffer.push(obtain);
        }
    }

    public final void logGutsExposed(@NotNull NotificationEntry notificationEntry) {
        Intrinsics.checkParameterIsNotNull(notificationEntry, "entry");
        LogBuffer logBuffer = this.buffer;
        LogLevel logLevel = LogLevel.DEBUG;
        NotificationClickerLogger$logGutsExposed$2 notificationClickerLogger$logGutsExposed$2 = NotificationClickerLogger$logGutsExposed$2.INSTANCE;
        if (!logBuffer.getFrozen()) {
            LogMessageImpl obtain = logBuffer.obtain("NotificationClicker", logLevel, notificationClickerLogger$logGutsExposed$2);
            obtain.setStr1(notificationEntry.getKey());
            logBuffer.push(obtain);
        }
    }
}
