package com.android.systemui;

import com.miui.systemui.MemoryMonitor;
import com.miui.systemui.ViewLeakMonitor;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: PerformanceTools.kt */
public final class PerformanceTools {
    private final MemoryMonitor memoryMonitor;
    private final ViewLeakMonitor viewLeakMonitor;

    public PerformanceTools(@NotNull ViewLeakMonitor viewLeakMonitor2, @NotNull MemoryMonitor memoryMonitor2) {
        Intrinsics.checkParameterIsNotNull(viewLeakMonitor2, "viewLeakMonitor");
        Intrinsics.checkParameterIsNotNull(memoryMonitor2, "memoryMonitor");
        this.viewLeakMonitor = viewLeakMonitor2;
        this.memoryMonitor = memoryMonitor2;
    }

    public final void start() {
        this.viewLeakMonitor.start();
        this.memoryMonitor.start();
    }
}
