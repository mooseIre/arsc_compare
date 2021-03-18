package com.android.systemui.dump;

import com.android.systemui.util.io.Files;
import com.android.systemui.util.time.SystemClock;
import java.io.IOException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: LogBufferEulogizer.kt */
public final class LogBufferEulogizer {
    private final DumpManager dumpManager;
    private final Files files;
    private final Path logPath;
    private final long maxLogAgeToDump;
    private final long minWriteGap;
    private final SystemClock systemClock;

    public LogBufferEulogizer(@NotNull DumpManager dumpManager2, @NotNull SystemClock systemClock2, @NotNull Files files2, @NotNull Path path, long j, long j2) {
        Intrinsics.checkParameterIsNotNull(dumpManager2, "dumpManager");
        Intrinsics.checkParameterIsNotNull(systemClock2, "systemClock");
        Intrinsics.checkParameterIsNotNull(files2, "files");
        Intrinsics.checkParameterIsNotNull(path, "logPath");
        this.dumpManager = dumpManager2;
        this.systemClock = systemClock2;
        this.files = files2;
        this.logPath = path;
        this.minWriteGap = j;
        this.maxLogAgeToDump = j2;
    }

    /* JADX WARNING: Illegal instructions before constructor call */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public LogBufferEulogizer(@org.jetbrains.annotations.NotNull android.content.Context r11, @org.jetbrains.annotations.NotNull com.android.systemui.dump.DumpManager r12, @org.jetbrains.annotations.NotNull com.android.systemui.util.time.SystemClock r13, @org.jetbrains.annotations.NotNull com.android.systemui.util.io.Files r14) {
        /*
            r10 = this;
            java.lang.String r0 = "context"
            kotlin.jvm.internal.Intrinsics.checkParameterIsNotNull(r11, r0)
            java.lang.String r0 = "dumpManager"
            kotlin.jvm.internal.Intrinsics.checkParameterIsNotNull(r12, r0)
            java.lang.String r0 = "systemClock"
            kotlin.jvm.internal.Intrinsics.checkParameterIsNotNull(r13, r0)
            java.lang.String r0 = "files"
            kotlin.jvm.internal.Intrinsics.checkParameterIsNotNull(r14, r0)
            java.io.File r11 = r11.getFilesDir()
            java.nio.file.Path r11 = r11.toPath()
            java.lang.String r11 = r11.toString()
            java.lang.String r0 = "log_buffers.txt"
            java.lang.String[] r0 = new java.lang.String[]{r0}
            java.nio.file.Path r5 = java.nio.file.Paths.get(r11, r0)
            java.lang.String r11 = "Paths.get(context.filesD…ing(), \"log_buffers.txt\")"
            kotlin.jvm.internal.Intrinsics.checkExpressionValueIsNotNull(r5, r11)
            long r6 = com.android.systemui.dump.LogBufferEulogizerKt.access$getMIN_WRITE_GAP$p()
            long r8 = com.android.systemui.dump.LogBufferEulogizerKt.access$getMAX_AGE_TO_DUMP$p()
            r1 = r10
            r2 = r12
            r3 = r13
            r4 = r14
            r1.<init>(r2, r3, r4, r5, r6, r8)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.dump.LogBufferEulogizer.<init>(android.content.Context, com.android.systemui.dump.DumpManager, com.android.systemui.util.time.SystemClock, com.android.systemui.util.io.Files):void");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x00a5, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x00a6, code lost:
        kotlin.io.CloseableKt.closeFinally(r7, r14);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x00a9, code lost:
        throw r2;
     */
    @org.jetbrains.annotations.NotNull
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final <T extends java.lang.Exception> T record(@org.jetbrains.annotations.NotNull T r15) {
        /*
        // Method dump skipped, instructions count: 198
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.dump.LogBufferEulogizer.record(java.lang.Exception):java.lang.Exception");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0059, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x005a, code lost:
        kotlin.jdk7.AutoCloseableKt.closeFinally(r5, r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x005d, code lost:
        throw r1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final void readEulogyIfPresent(@org.jetbrains.annotations.NotNull java.io.PrintWriter r6) {
        /*
        // Method dump skipped, instructions count: 101
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.dump.LogBufferEulogizer.readEulogyIfPresent(java.io.PrintWriter):void");
    }

    private final long getMillisSinceLastWrite(Path path) {
        BasicFileAttributes basicFileAttributes;
        FileTime lastModifiedTime;
        try {
            basicFileAttributes = this.files.readAttributes(path, BasicFileAttributes.class, new LinkOption[0]);
        } catch (IOException unused) {
            basicFileAttributes = null;
        }
        return this.systemClock.currentTimeMillis() - ((basicFileAttributes == null || (lastModifiedTime = basicFileAttributes.lastModifiedTime()) == null) ? 0 : lastModifiedTime.toMillis());
    }
}
