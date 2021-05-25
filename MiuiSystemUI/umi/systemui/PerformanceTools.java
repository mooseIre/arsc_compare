package com.android.systemui;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import codeinjection.CodeInjection;
import com.android.systemui.dump.DumpManager;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.List;
import java.util.Locale;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;
import kotlin.text.StringsKt__StringsJVMKt;
import kotlin.text.StringsKt__StringsKt;
import miui.os.Build;
import miui.systemui.performance.BinderMonitor;
import miui.systemui.performance.EvilMethodMonitor;
import miui.systemui.performance.FrameMonitor;
import miui.systemui.performance.MemoryMonitor;
import miui.systemui.performance.MessageMonitor;
import miui.systemui.performance.ViewLeakMonitor;
import org.jetbrains.annotations.NotNull;

/* compiled from: PerformanceTools.kt */
public final class PerformanceTools implements Dumpable {
    private final BinderMonitor binderMonitor;
    private final Context context;
    private final EvilMethodMonitor evilMethodMonitor;
    private final FrameMonitor frameMonitor;
    private final MemoryMonitor memoryMonitor;
    private final MessageMonitor messageMonitor;
    private final ViewLeakMonitor viewLeakMonitor;

    public PerformanceTools(@NotNull Context context2, @NotNull BinderMonitor binderMonitor2, @NotNull EvilMethodMonitor evilMethodMonitor2, @NotNull FrameMonitor frameMonitor2, @NotNull MemoryMonitor memoryMonitor2, @NotNull MessageMonitor messageMonitor2, @NotNull ViewLeakMonitor viewLeakMonitor2, @NotNull DumpManager dumpManager) {
        Intrinsics.checkParameterIsNotNull(context2, "context");
        Intrinsics.checkParameterIsNotNull(binderMonitor2, "binderMonitor");
        Intrinsics.checkParameterIsNotNull(evilMethodMonitor2, "evilMethodMonitor");
        Intrinsics.checkParameterIsNotNull(frameMonitor2, "frameMonitor");
        Intrinsics.checkParameterIsNotNull(memoryMonitor2, "memoryMonitor");
        Intrinsics.checkParameterIsNotNull(messageMonitor2, "messageMonitor");
        Intrinsics.checkParameterIsNotNull(viewLeakMonitor2, "viewLeakMonitor");
        Intrinsics.checkParameterIsNotNull(dumpManager, "dumpManager");
        this.context = context2;
        this.binderMonitor = binderMonitor2;
        this.evilMethodMonitor = evilMethodMonitor2;
        this.frameMonitor = frameMonitor2;
        this.memoryMonitor = memoryMonitor2;
        this.messageMonitor = messageMonitor2;
        this.viewLeakMonitor = viewLeakMonitor2;
        if (Build.IS_DEBUGGABLE) {
            context2.getContentResolver().registerContentObserver(Settings.Global.getUriFor("sysui.performance"), false, new ContentObserver(this, new Handler(Looper.getMainLooper())) {
                /* class com.android.systemui.PerformanceTools.AnonymousClass1 */
                final /* synthetic */ PerformanceTools this$0;

                {
                    this.this$0 = r1;
                }

                public void onChange(boolean z) {
                    this.this$0.execCommand();
                }
            });
        }
        dumpManager.registerDumpable("PerformanceTools", this);
    }

    public final void start() {
        Log.d("PerformanceTools", "IS_DEBUGGABLE=" + Build.IS_DEBUGGABLE);
        if (Build.IS_DEBUGGABLE) {
            execCommand();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private final void execCommand() {
        String string = Settings.Global.getString(this.context.getContentResolver(), "sysui.performance");
        Log.d("PerformanceTools", "onCommand " + string);
        try {
            Intrinsics.checkExpressionValueIsNotNull(string, "cmd");
            Locale locale = Locale.ROOT;
            Intrinsics.checkExpressionValueIsNotNull(locale, "Locale.ROOT");
            if (string != null) {
                String lowerCase = string.toLowerCase(locale);
                Intrinsics.checkExpressionValueIsNotNull(lowerCase, "(this as java.lang.String).toLowerCase(locale)");
                List<String> list = StringsKt__StringsKt.split$default(lowerCase, new String[]{"="}, false, 0, 6, null);
                if (StringsKt__StringsJVMKt.startsWith$default(string, "binder", false, 2, null)) {
                    this.binderMonitor.onCommand(list);
                } else if (StringsKt__StringsJVMKt.startsWith$default(string, "evilmethod", false, 2, null)) {
                    this.evilMethodMonitor.onCommand(list);
                } else if (StringsKt__StringsJVMKt.startsWith$default(string, "frame", false, 2, null)) {
                    this.frameMonitor.onCommand(list);
                } else if (StringsKt__StringsJVMKt.startsWith$default(string, "memory", false, 2, null)) {
                    this.memoryMonitor.onCommand(list);
                } else if (StringsKt__StringsJVMKt.startsWith$default(string, "viewleak", false, 2, null)) {
                    this.viewLeakMonitor.onCommand(list);
                }
            } else {
                throw new TypeCastException("null cannot be cast to non-null type java.lang.String");
            }
        } catch (Exception e) {
            Log.e("PerformanceTools", CodeInjection.MD5, e);
        }
    }

    public final void doDailyTask() {
        if (Build.IS_DEBUGGABLE) {
            this.evilMethodMonitor.trimTrace();
        }
    }

    @Override // com.android.systemui.Dumpable
    public void dump(@NotNull FileDescriptor fileDescriptor, @NotNull PrintWriter printWriter, @NotNull String[] strArr) {
        Intrinsics.checkParameterIsNotNull(fileDescriptor, "fd");
        Intrinsics.checkParameterIsNotNull(printWriter, "pw");
        Intrinsics.checkParameterIsNotNull(strArr, "args");
        this.binderMonitor.dump(printWriter);
        this.evilMethodMonitor.dump(printWriter);
        this.frameMonitor.dump(printWriter);
        this.memoryMonitor.dump(printWriter);
        this.messageMonitor.dump(printWriter);
        this.viewLeakMonitor.dump(printWriter);
    }
}
