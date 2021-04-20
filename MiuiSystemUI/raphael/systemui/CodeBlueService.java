package com.android.systemui;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import codeinjection.CodeInjection;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.miui.systemui.EventTracker;
import com.miui.systemui.events.CodeBlueEvent;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: CodeBlueService.kt */
public final class CodeBlueService {
    @NotNull
    private final Looper bgLooper;
    @NotNull
    private final Context context;
    @NotNull
    private final NotificationEntryManager entryManager;
    @NotNull
    private final EventTracker eventTracker;
    @Nullable
    private String latestNotificationPkgName;

    public CodeBlueService(@NotNull Context context2, @NotNull Looper looper, @NotNull NotificationEntryManager notificationEntryManager, @NotNull EventTracker eventTracker2) {
        Intrinsics.checkParameterIsNotNull(context2, "context");
        Intrinsics.checkParameterIsNotNull(looper, "bgLooper");
        Intrinsics.checkParameterIsNotNull(notificationEntryManager, "entryManager");
        Intrinsics.checkParameterIsNotNull(eventTracker2, "eventTracker");
        this.context = context2;
        this.bgLooper = looper;
        this.entryManager = notificationEntryManager;
        this.eventTracker = eventTracker2;
    }

    @NotNull
    public final Context getContext() {
        return this.context;
    }

    @Nullable
    public final String getLatestNotificationPkgName() {
        return this.latestNotificationPkgName;
    }

    public final void setLatestNotificationPkgName(@Nullable String str) {
        this.latestNotificationPkgName = str;
    }

    public final void start() {
        addNotifCollectionListener();
        trackCodeBlue();
    }

    private final void addNotifCollectionListener() {
        this.entryManager.addCollectionListener(new CodeBlueService$addNotifCollectionListener$1(this));
    }

    private final void trackCodeBlue() {
        new Handler(this.bgLooper).post(new CodeBlueService$trackCodeBlue$1(this));
    }

    /* access modifiers changed from: private */
    public final void trackCodeBlueEvent() {
        EventTracker eventTracker2 = this.eventTracker;
        int crashCount = CodeBlueConfig.Companion.getCrashCount(this.context);
        String exceptionHandler = CodeBlueConfig.Companion.getExceptionHandler(this.context);
        String str = CodeInjection.MD5;
        if (exceptionHandler == null) {
            exceptionHandler = str;
        }
        String exceptionClues = CodeBlueConfig.Companion.getExceptionClues(this.context);
        if (exceptionClues != null) {
            str = exceptionClues;
        }
        eventTracker2.track(new CodeBlueEvent(crashCount, exceptionHandler, str));
    }
}
