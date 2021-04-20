package com.android.systemui;

import com.android.systemui.dump.DumpManager;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import kotlin.jvm.internal.Intrinsics;
import miui.os.Build;
import miui.systemui.performance.BinderMonitor;
import miui.systemui.performance.EvilMethodMonitor;
import miui.systemui.performance.MemoryMonitor;
import miui.systemui.performance.ViewLeakMonitor;
import org.jetbrains.annotations.NotNull;

/* compiled from: PerformanceTools.kt */
public final class PerformanceTools implements Dumpable {
    private final BinderMonitor binderMonitor;
    private final EvilMethodMonitor evilMethodMonitor;
    private final MemoryMonitor memoryMonitor;
    private final ViewLeakMonitor viewLeakMonitor;

    public PerformanceTools(@NotNull ViewLeakMonitor viewLeakMonitor2, @NotNull MemoryMonitor memoryMonitor2, @NotNull EvilMethodMonitor evilMethodMonitor2, @NotNull BinderMonitor binderMonitor2, @NotNull DumpManager dumpManager) {
        Intrinsics.checkParameterIsNotNull(viewLeakMonitor2, "viewLeakMonitor");
        Intrinsics.checkParameterIsNotNull(memoryMonitor2, "memoryMonitor");
        Intrinsics.checkParameterIsNotNull(evilMethodMonitor2, "evilMethodMonitor");
        Intrinsics.checkParameterIsNotNull(binderMonitor2, "binderMonitor");
        Intrinsics.checkParameterIsNotNull(dumpManager, "dumpManager");
        this.viewLeakMonitor = viewLeakMonitor2;
        this.memoryMonitor = memoryMonitor2;
        this.evilMethodMonitor = evilMethodMonitor2;
        this.binderMonitor = binderMonitor2;
        dumpManager.registerDumpable("PerformanceTools", this);
    }

    public final void start() {
        if (Build.IS_DEBUGGABLE) {
            this.viewLeakMonitor.start();
            this.memoryMonitor.start();
            this.evilMethodMonitor.start();
            this.binderMonitor.start();
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
        this.viewLeakMonitor.dump(printWriter);
        this.memoryMonitor.dump(printWriter);
        this.evilMethodMonitor.dump(printWriter);
        this.binderMonitor.dump(printWriter);
    }
}
