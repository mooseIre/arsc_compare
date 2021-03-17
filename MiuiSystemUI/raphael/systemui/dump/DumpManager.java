package com.android.systemui.dump;

import android.util.ArrayMap;
import com.android.systemui.Dumpable;
import com.android.systemui.log.LogBuffer;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Map;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: DumpManager.kt */
public final class DumpManager {
    private final Map<String, RegisteredDumpable<LogBuffer>> buffers = new ArrayMap();
    private final Map<String, RegisteredDumpable<Dumpable>> dumpables = new ArrayMap();

    public final synchronized void registerDumpable(@NotNull String str, @NotNull Dumpable dumpable) {
        Intrinsics.checkParameterIsNotNull(str, "name");
        Intrinsics.checkParameterIsNotNull(dumpable, "module");
        if (canAssignToNameLocked(str, dumpable)) {
            this.dumpables.put(str, new RegisteredDumpable(str, dumpable));
        } else {
            throw new IllegalArgumentException('\'' + str + "' is already registered");
        }
    }

    public final synchronized void unregisterDumpable(@NotNull String str) {
        Intrinsics.checkParameterIsNotNull(str, "name");
        this.dumpables.remove(str);
    }

    public final synchronized void registerBuffer(@NotNull String str, @NotNull LogBuffer logBuffer) {
        Intrinsics.checkParameterIsNotNull(str, "name");
        Intrinsics.checkParameterIsNotNull(logBuffer, "buffer");
        if (canAssignToNameLocked(str, logBuffer)) {
            this.buffers.put(str, new RegisteredDumpable(str, logBuffer));
        } else {
            throw new IllegalArgumentException('\'' + str + "' is already registered");
        }
    }

    public final synchronized void dumpTarget(@NotNull String str, @NotNull FileDescriptor fileDescriptor, @NotNull PrintWriter printWriter, @NotNull String[] strArr, int i) {
        Intrinsics.checkParameterIsNotNull(str, "target");
        Intrinsics.checkParameterIsNotNull(fileDescriptor, "fd");
        Intrinsics.checkParameterIsNotNull(printWriter, "pw");
        Intrinsics.checkParameterIsNotNull(strArr, "args");
        for (RegisteredDumpable next : this.dumpables.values()) {
            if (StringsKt__StringsJVMKt.endsWith$default(next.getName(), str, false, 2, (Object) null)) {
                dumpDumpable(next, fileDescriptor, printWriter, strArr);
                return;
            }
        }
        for (RegisteredDumpable next2 : this.buffers.values()) {
            if (StringsKt__StringsJVMKt.endsWith$default(next2.getName(), str, false, 2, (Object) null)) {
                dumpBuffer(next2, printWriter, i);
                return;
            }
        }
    }

    public final synchronized void dumpDumpables(@NotNull FileDescriptor fileDescriptor, @NotNull PrintWriter printWriter, @NotNull String[] strArr) {
        Intrinsics.checkParameterIsNotNull(fileDescriptor, "fd");
        Intrinsics.checkParameterIsNotNull(printWriter, "pw");
        Intrinsics.checkParameterIsNotNull(strArr, "args");
        for (RegisteredDumpable<Dumpable> dumpDumpable : this.dumpables.values()) {
            dumpDumpable(dumpDumpable, fileDescriptor, printWriter, strArr);
        }
    }

    public final synchronized void listDumpables(@NotNull PrintWriter printWriter) {
        Intrinsics.checkParameterIsNotNull(printWriter, "pw");
        for (RegisteredDumpable<Dumpable> name : this.dumpables.values()) {
            printWriter.println(name.getName());
        }
    }

    public final synchronized void dumpBuffers(@NotNull PrintWriter printWriter, int i) {
        Intrinsics.checkParameterIsNotNull(printWriter, "pw");
        for (RegisteredDumpable<LogBuffer> dumpBuffer : this.buffers.values()) {
            dumpBuffer(dumpBuffer, printWriter, i);
        }
    }

    public final synchronized void listBuffers(@NotNull PrintWriter printWriter) {
        Intrinsics.checkParameterIsNotNull(printWriter, "pw");
        for (RegisteredDumpable<LogBuffer> name : this.buffers.values()) {
            printWriter.println(name.getName());
        }
    }

    public final synchronized void freezeBuffers() {
        for (RegisteredDumpable<LogBuffer> dumpable : this.buffers.values()) {
            ((LogBuffer) dumpable.getDumpable()).freeze();
        }
    }

    public final synchronized void unfreezeBuffers() {
        for (RegisteredDumpable<LogBuffer> dumpable : this.buffers.values()) {
            ((LogBuffer) dumpable.getDumpable()).unfreeze();
        }
    }

    private final void dumpDumpable(RegisteredDumpable<Dumpable> registeredDumpable, FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println();
        printWriter.println(registeredDumpable.getName() + ':');
        printWriter.println("----------------------------------------------------------------------------");
        registeredDumpable.getDumpable().dump(fileDescriptor, printWriter, strArr);
    }

    private final void dumpBuffer(RegisteredDumpable<LogBuffer> registeredDumpable, PrintWriter printWriter, int i) {
        printWriter.println();
        printWriter.println();
        printWriter.println("BUFFER " + registeredDumpable.getName() + ':');
        printWriter.println("============================================================================");
        registeredDumpable.getDumpable().dump(printWriter, i);
    }

    private final boolean canAssignToNameLocked(String str, Object obj) {
        Object obj2;
        RegisteredDumpable registeredDumpable = this.dumpables.get(str);
        if (registeredDumpable == null || (obj2 = (Dumpable) registeredDumpable.getDumpable()) == null) {
            RegisteredDumpable registeredDumpable2 = this.buffers.get(str);
            obj2 = registeredDumpable2 != null ? (LogBuffer) registeredDumpable2.getDumpable() : null;
        }
        return obj2 == null || Intrinsics.areEqual(obj, obj2);
    }
}
