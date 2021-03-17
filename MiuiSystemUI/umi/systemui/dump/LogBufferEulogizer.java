package com.android.systemui.dump;

import com.android.systemui.util.io.Files;
import com.android.systemui.util.time.SystemClock;
import java.nio.file.Path;
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
            long r6 = com.android.systemui.dump.LogBufferEulogizerKt.MIN_WRITE_GAP
            long r8 = com.android.systemui.dump.LogBufferEulogizerKt.MAX_AGE_TO_DUMP
            r1 = r10
            r2 = r12
            r3 = r13
            r4 = r14
            r1.<init>(r2, r3, r4, r5, r6, r8)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.dump.LogBufferEulogizer.<init>(android.content.Context, com.android.systemui.dump.DumpManager, com.android.systemui.util.time.SystemClock, com.android.systemui.util.io.Files):void");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x00a5, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:?, code lost:
        kotlin.io.CloseableKt.closeFinally(r7, r14);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x00a9, code lost:
        throw r2;
     */
    @org.jetbrains.annotations.NotNull
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final <T extends java.lang.Exception> T record(@org.jetbrains.annotations.NotNull T r15) {
        /*
            r14 = this;
            java.lang.String r0 = "ms"
            java.lang.String r1 = "Buffer eulogy took "
            java.lang.String r2 = "reason"
            kotlin.jvm.internal.Intrinsics.checkParameterIsNotNull(r15, r2)
            com.android.systemui.util.time.SystemClock r2 = r14.systemClock
            long r2 = r2.uptimeMillis()
            java.lang.String r4 = "BufferEulogizer"
            java.lang.String r5 = "Performing emergency dump of log buffers"
            android.util.Log.i(r4, r5)
            java.nio.file.Path r5 = r14.logPath
            long r5 = r14.getMillisSinceLastWrite(r5)
            long r7 = r14.minWriteGap
            int r7 = (r5 > r7 ? 1 : (r5 == r7 ? 0 : -1))
            if (r7 >= 0) goto L_0x003c
            java.lang.StringBuilder r14 = new java.lang.StringBuilder
            r14.<init>()
            java.lang.String r0 = "Cannot dump logs, last write was only "
            r14.append(r0)
            r14.append(r5)
            java.lang.String r0 = " ms ago"
            r14.append(r0)
            java.lang.String r14 = r14.toString()
            android.util.Log.w(r4, r14)
            return r15
        L_0x003c:
            r5 = 0
            com.android.systemui.util.io.Files r7 = r14.files     // Catch:{ Exception -> 0x00aa }
            java.nio.file.Path r8 = r14.logPath     // Catch:{ Exception -> 0x00aa }
            r9 = 2
            java.nio.file.OpenOption[] r9 = new java.nio.file.OpenOption[r9]     // Catch:{ Exception -> 0x00aa }
            java.nio.file.StandardOpenOption r10 = java.nio.file.StandardOpenOption.CREATE     // Catch:{ Exception -> 0x00aa }
            r11 = 0
            r9[r11] = r10     // Catch:{ Exception -> 0x00aa }
            r10 = 1
            java.nio.file.StandardOpenOption r12 = java.nio.file.StandardOpenOption.TRUNCATE_EXISTING     // Catch:{ Exception -> 0x00aa }
            r9[r10] = r12     // Catch:{ Exception -> 0x00aa }
            java.io.BufferedWriter r7 = r7.newBufferedWriter(r8, r9)     // Catch:{ Exception -> 0x00aa }
            r8 = 0
            java.io.PrintWriter r9 = new java.io.PrintWriter     // Catch:{ all -> 0x00a3 }
            r9.<init>(r7)     // Catch:{ all -> 0x00a3 }
            java.text.SimpleDateFormat r10 = com.android.systemui.dump.LogBufferEulogizerKt.DATE_FORMAT     // Catch:{ all -> 0x00a3 }
            com.android.systemui.util.time.SystemClock r12 = r14.systemClock     // Catch:{ all -> 0x00a3 }
            long r12 = r12.currentTimeMillis()     // Catch:{ all -> 0x00a3 }
            java.lang.Long r12 = java.lang.Long.valueOf(r12)     // Catch:{ all -> 0x00a3 }
            java.lang.String r10 = r10.format(r12)     // Catch:{ all -> 0x00a3 }
            r9.println(r10)     // Catch:{ all -> 0x00a3 }
            r9.println()     // Catch:{ all -> 0x00a3 }
            java.lang.String r10 = "Dump triggered by exception:"
            r9.println(r10)     // Catch:{ all -> 0x00a3 }
            r15.printStackTrace(r9)     // Catch:{ all -> 0x00a3 }
            com.android.systemui.dump.DumpManager r10 = r14.dumpManager     // Catch:{ all -> 0x00a3 }
            r10.dumpBuffers(r9, r11)     // Catch:{ all -> 0x00a3 }
            com.android.systemui.util.time.SystemClock r14 = r14.systemClock     // Catch:{ all -> 0x00a3 }
            long r5 = r14.uptimeMillis()     // Catch:{ all -> 0x00a3 }
            long r5 = r5 - r2
            r9.println()     // Catch:{ all -> 0x00a3 }
            java.lang.StringBuilder r14 = new java.lang.StringBuilder     // Catch:{ all -> 0x00a3 }
            r14.<init>()     // Catch:{ all -> 0x00a3 }
            r14.append(r1)     // Catch:{ all -> 0x00a3 }
            r14.append(r5)     // Catch:{ all -> 0x00a3 }
            r14.append(r0)     // Catch:{ all -> 0x00a3 }
            java.lang.String r14 = r14.toString()     // Catch:{ all -> 0x00a3 }
            r9.println(r14)     // Catch:{ all -> 0x00a3 }
            kotlin.Unit r14 = kotlin.Unit.INSTANCE     // Catch:{ all -> 0x00a3 }
            kotlin.io.CloseableKt.closeFinally(r7, r8)     // Catch:{ Exception -> 0x00aa }
            goto L_0x00b0
        L_0x00a3:
            r14 = move-exception
            throw r14     // Catch:{ all -> 0x00a5 }
        L_0x00a5:
            r2 = move-exception
            kotlin.io.CloseableKt.closeFinally(r7, r14)     // Catch:{ Exception -> 0x00aa }
            throw r2     // Catch:{ Exception -> 0x00aa }
        L_0x00aa:
            r14 = move-exception
            java.lang.String r2 = "Exception while attempting to dump buffers, bailing"
            android.util.Log.e(r4, r2, r14)
        L_0x00b0:
            java.lang.StringBuilder r14 = new java.lang.StringBuilder
            r14.<init>()
            r14.append(r1)
            r14.append(r5)
            r14.append(r0)
            java.lang.String r14 = r14.toString()
            android.util.Log.i(r4, r14)
            return r15
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.dump.LogBufferEulogizer.record(java.lang.Exception):java.lang.Exception");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0059, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:?, code lost:
        kotlin.jdk7.AutoCloseableKt.closeFinally(r5, r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x005d, code lost:
        throw r1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final void readEulogyIfPresent(@org.jetbrains.annotations.NotNull java.io.PrintWriter r6) {
        /*
            r5 = this;
            java.lang.String r0 = "BufferEulogizer"
            java.lang.String r1 = "pw"
            kotlin.jvm.internal.Intrinsics.checkParameterIsNotNull(r6, r1)
            java.nio.file.Path r1 = r5.logPath     // Catch:{ IOException -> 0x0064, UncheckedIOException -> 0x005e }
            long r1 = r5.getMillisSinceLastWrite(r1)     // Catch:{ IOException -> 0x0064, UncheckedIOException -> 0x005e }
            long r3 = r5.maxLogAgeToDump     // Catch:{ IOException -> 0x0064, UncheckedIOException -> 0x005e }
            int r3 = (r1 > r3 ? 1 : (r1 == r3 ? 0 : -1))
            if (r3 <= 0) goto L_0x0035
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ IOException -> 0x0064, UncheckedIOException -> 0x005e }
            r5.<init>()     // Catch:{ IOException -> 0x0064, UncheckedIOException -> 0x005e }
            java.lang.String r6 = "Not eulogizing buffers; they are "
            r5.append(r6)     // Catch:{ IOException -> 0x0064, UncheckedIOException -> 0x005e }
            java.util.concurrent.TimeUnit r6 = java.util.concurrent.TimeUnit.HOURS     // Catch:{ IOException -> 0x0064, UncheckedIOException -> 0x005e }
            java.util.concurrent.TimeUnit r3 = java.util.concurrent.TimeUnit.MILLISECONDS     // Catch:{ IOException -> 0x0064, UncheckedIOException -> 0x005e }
            long r1 = r6.convert(r1, r3)     // Catch:{ IOException -> 0x0064, UncheckedIOException -> 0x005e }
            r5.append(r1)     // Catch:{ IOException -> 0x0064, UncheckedIOException -> 0x005e }
            java.lang.String r6 = " hours old"
            r5.append(r6)     // Catch:{ IOException -> 0x0064, UncheckedIOException -> 0x005e }
            java.lang.String r5 = r5.toString()     // Catch:{ IOException -> 0x0064, UncheckedIOException -> 0x005e }
            android.util.Log.i(r0, r5)     // Catch:{ IOException -> 0x0064, UncheckedIOException -> 0x005e }
            return
        L_0x0035:
            com.android.systemui.util.io.Files r1 = r5.files     // Catch:{ IOException -> 0x0064, UncheckedIOException -> 0x005e }
            java.nio.file.Path r5 = r5.logPath     // Catch:{ IOException -> 0x0064, UncheckedIOException -> 0x005e }
            java.util.stream.Stream r5 = r1.lines(r5)     // Catch:{ IOException -> 0x0064, UncheckedIOException -> 0x005e }
            r1 = 0
            r6.println()     // Catch:{ all -> 0x0057 }
            r6.println()     // Catch:{ all -> 0x0057 }
            java.lang.String r2 = "=============== BUFFERS FROM MOST RECENT CRASH ==============="
            r6.println(r2)     // Catch:{ all -> 0x0057 }
            com.android.systemui.dump.LogBufferEulogizer$readEulogyIfPresent$$inlined$use$lambda$1 r2 = new com.android.systemui.dump.LogBufferEulogizer$readEulogyIfPresent$$inlined$use$lambda$1     // Catch:{ all -> 0x0057 }
            r2.<init>(r6)     // Catch:{ all -> 0x0057 }
            r5.forEach(r2)     // Catch:{ all -> 0x0057 }
            kotlin.Unit r6 = kotlin.Unit.INSTANCE     // Catch:{ all -> 0x0057 }
            kotlin.jdk7.AutoCloseableKt.closeFinally(r5, r1)     // Catch:{ IOException -> 0x0064, UncheckedIOException -> 0x005e }
            goto L_0x0064
        L_0x0057:
            r6 = move-exception
            throw r6     // Catch:{ all -> 0x0059 }
        L_0x0059:
            r1 = move-exception
            kotlin.jdk7.AutoCloseableKt.closeFinally(r5, r6)     // Catch:{ IOException -> 0x0064, UncheckedIOException -> 0x005e }
            throw r1     // Catch:{ IOException -> 0x0064, UncheckedIOException -> 0x005e }
        L_0x005e:
            r5 = move-exception
            java.lang.String r6 = "UncheckedIOException while dumping the core"
            android.util.Log.e(r0, r6, r5)
        L_0x0064:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.dump.LogBufferEulogizer.readEulogyIfPresent(java.io.PrintWriter):void");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:6:0x0015, code lost:
        r3 = r4.lastModifiedTime();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private final long getMillisSinceLastWrite(java.nio.file.Path r4) {
        /*
            r3 = this;
            com.android.systemui.util.io.Files r0 = r3.files     // Catch:{ IOException -> 0x000c }
            java.lang.Class<java.nio.file.attribute.BasicFileAttributes> r1 = java.nio.file.attribute.BasicFileAttributes.class
            r2 = 0
            java.nio.file.LinkOption[] r2 = new java.nio.file.LinkOption[r2]     // Catch:{ IOException -> 0x000c }
            java.nio.file.attribute.BasicFileAttributes r4 = r0.readAttributes(r4, r1, r2)     // Catch:{ IOException -> 0x000c }
            goto L_0x000d
        L_0x000c:
            r4 = 0
        L_0x000d:
            com.android.systemui.util.time.SystemClock r3 = r3.systemClock
            long r0 = r3.currentTimeMillis()
            if (r4 == 0) goto L_0x0020
            java.nio.file.attribute.FileTime r3 = r4.lastModifiedTime()
            if (r3 == 0) goto L_0x0020
            long r3 = r3.toMillis()
            goto L_0x0022
        L_0x0020:
            r3 = 0
        L_0x0022:
            long r0 = r0 - r3
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.dump.LogBufferEulogizer.getMillisSinceLastWrite(java.nio.file.Path):long");
    }
}
