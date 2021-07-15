package com.android.systemui;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import codeinjection.CodeInjection;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.miui.systemui.EventTracker;
import com.miui.systemui.events.CodeBlueEvent;
import kotlin.jvm.internal.Intrinsics;

public final class CodeBlueService {
    private final Looper bgLooper;
    private final Context context;
    private final NotificationEntryManager entryManager;
    private final EventTracker eventTracker;
    private String latestNotificationPkgName;
    private long latestNotificationTimeMillis;

    public CodeBlueService(Context context2, Looper looper, NotificationEntryManager notificationEntryManager, EventTracker eventTracker2) {
        Intrinsics.checkParameterIsNotNull(context2, "context");
        Intrinsics.checkParameterIsNotNull(looper, "bgLooper");
        Intrinsics.checkParameterIsNotNull(notificationEntryManager, "entryManager");
        Intrinsics.checkParameterIsNotNull(eventTracker2, "eventTracker");
        this.context = context2;
        this.bgLooper = looper;
        this.entryManager = notificationEntryManager;
        this.eventTracker = eventTracker2;
    }

    public final Context getContext() {
        return this.context;
    }

    public final String getLatestNotificationPkgName() {
        return this.latestNotificationPkgName;
    }

    public final void setLatestNotificationPkgName(String str) {
        this.latestNotificationPkgName = str;
    }

    public final long getLatestNotificationTimeMillis() {
        return this.latestNotificationTimeMillis;
    }

    public final void setLatestNotificationTimeMillis(long j) {
        this.latestNotificationTimeMillis = j;
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

    /* access modifiers changed from: public */
    private final void trackCodeBlueEvent() {
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
