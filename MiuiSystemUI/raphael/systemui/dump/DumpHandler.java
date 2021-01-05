package com.android.systemui.dump;

import android.content.Context;
import android.os.SystemClock;
import android.os.Trace;
import com.android.systemui.C0008R$array;
import com.android.systemui.C0021R$string;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: DumpHandler.kt */
public final class DumpHandler {
    private final Context context;
    private final DumpManager dumpManager;
    private final LogBufferEulogizer logBufferEulogizer;

    public DumpHandler(@NotNull Context context2, @NotNull DumpManager dumpManager2, @NotNull LogBufferEulogizer logBufferEulogizer2) {
        Intrinsics.checkParameterIsNotNull(context2, "context");
        Intrinsics.checkParameterIsNotNull(dumpManager2, "dumpManager");
        Intrinsics.checkParameterIsNotNull(logBufferEulogizer2, "logBufferEulogizer");
        this.context = context2;
        this.dumpManager = dumpManager2;
        this.logBufferEulogizer = logBufferEulogizer2;
    }

    public final void dump(@NotNull FileDescriptor fileDescriptor, @NotNull PrintWriter printWriter, @NotNull String[] strArr) {
        Intrinsics.checkParameterIsNotNull(fileDescriptor, "fd");
        Intrinsics.checkParameterIsNotNull(printWriter, "pw");
        Intrinsics.checkParameterIsNotNull(strArr, "args");
        Trace.beginSection("DumpManager#dump()");
        long uptimeMillis = SystemClock.uptimeMillis();
        try {
            ParsedArgs parseArgs = parseArgs(strArr);
            String dumpPriority = parseArgs.getDumpPriority();
            if (dumpPriority != null) {
                int hashCode = dumpPriority.hashCode();
                if (hashCode != -1986416409) {
                    if (hashCode == -1560189025 && dumpPriority.equals("CRITICAL")) {
                        dumpCritical(fileDescriptor, printWriter, parseArgs);
                        printWriter.println();
                        printWriter.println("Dump took " + (SystemClock.uptimeMillis() - uptimeMillis) + "ms");
                        Trace.endSection();
                    }
                } else if (dumpPriority.equals("NORMAL")) {
                    dumpNormal(printWriter, parseArgs);
                    printWriter.println();
                    printWriter.println("Dump took " + (SystemClock.uptimeMillis() - uptimeMillis) + "ms");
                    Trace.endSection();
                }
            }
            dumpParameterized(fileDescriptor, printWriter, parseArgs);
            printWriter.println();
            printWriter.println("Dump took " + (SystemClock.uptimeMillis() - uptimeMillis) + "ms");
            Trace.endSection();
        } catch (ArgParseException e) {
            printWriter.println(e.getMessage());
        }
    }

    private final void dumpParameterized(FileDescriptor fileDescriptor, PrintWriter printWriter, ParsedArgs parsedArgs) {
        String command = parsedArgs.getCommand();
        if (command != null) {
            switch (command.hashCode()) {
                case -1354792126:
                    if (command.equals("config")) {
                        dumpConfig(printWriter);
                        return;
                    }
                    break;
                case -1353714459:
                    if (command.equals("dumpables")) {
                        dumpDumpables(fileDescriptor, printWriter, parsedArgs);
                        return;
                    }
                    break;
                case -1045369428:
                    if (command.equals("bugreport-normal")) {
                        dumpNormal(printWriter, parsedArgs);
                        return;
                    }
                    break;
                case 3198785:
                    if (command.equals("help")) {
                        dumpHelp(printWriter);
                        return;
                    }
                    break;
                case 227996723:
                    if (command.equals("buffers")) {
                        dumpBuffers(printWriter, parsedArgs);
                        return;
                    }
                    break;
                case 842828580:
                    if (command.equals("bugreport-critical")) {
                        dumpCritical(fileDescriptor, printWriter, parsedArgs);
                        return;
                    }
                    break;
            }
        }
        dumpTargets(parsedArgs.getNonFlagArgs(), fileDescriptor, printWriter, parsedArgs);
    }

    private final void dumpCritical(FileDescriptor fileDescriptor, PrintWriter printWriter, ParsedArgs parsedArgs) {
        this.dumpManager.dumpDumpables(fileDescriptor, printWriter, parsedArgs.getRawArgs());
        dumpConfig(printWriter);
    }

    private final void dumpNormal(PrintWriter printWriter, ParsedArgs parsedArgs) {
        this.dumpManager.dumpBuffers(printWriter, parsedArgs.getTailLength());
        this.logBufferEulogizer.readEulogyIfPresent(printWriter);
    }

    private final void dumpDumpables(FileDescriptor fileDescriptor, PrintWriter printWriter, ParsedArgs parsedArgs) {
        if (parsedArgs.getListOnly()) {
            this.dumpManager.listDumpables(printWriter);
        } else {
            this.dumpManager.dumpDumpables(fileDescriptor, printWriter, parsedArgs.getRawArgs());
        }
    }

    private final void dumpBuffers(PrintWriter printWriter, ParsedArgs parsedArgs) {
        if (parsedArgs.getListOnly()) {
            this.dumpManager.listBuffers(printWriter);
        } else {
            this.dumpManager.dumpBuffers(printWriter, parsedArgs.getTailLength());
        }
    }

    private final void dumpTargets(List<String> list, FileDescriptor fileDescriptor, PrintWriter printWriter, ParsedArgs parsedArgs) {
        if (!list.isEmpty()) {
            for (String dumpTarget : list) {
                this.dumpManager.dumpTarget(dumpTarget, fileDescriptor, printWriter, parsedArgs.getRawArgs(), parsedArgs.getTailLength());
            }
        } else if (parsedArgs.getListOnly()) {
            printWriter.println("Dumpables:");
            this.dumpManager.listDumpables(printWriter);
            printWriter.println();
            printWriter.println("Buffers:");
            this.dumpManager.listBuffers(printWriter);
        } else {
            printWriter.println("Nothing to dump :(");
        }
    }

    private final void dumpConfig(PrintWriter printWriter) {
        printWriter.println("SystemUiServiceComponents configuration:");
        printWriter.print("vendor component: ");
        printWriter.println(this.context.getResources().getString(C0021R$string.config_systemUIVendorServiceComponent));
        dumpServiceList(printWriter, "global", C0008R$array.config_systemUIServiceComponents);
        dumpServiceList(printWriter, "per-user", C0008R$array.config_systemUIServiceComponentsPerUser);
    }

    private final void dumpServiceList(PrintWriter printWriter, String str, int i) {
        String[] stringArray = this.context.getResources().getStringArray(i);
        printWriter.print(str);
        printWriter.print(": ");
        if (stringArray == null) {
            printWriter.println("N/A");
            return;
        }
        printWriter.print(stringArray.length);
        printWriter.println(" services");
        int length = stringArray.length;
        for (int i2 = 0; i2 < length; i2++) {
            printWriter.print("  ");
            printWriter.print(i2);
            printWriter.print(": ");
            printWriter.println(stringArray[i2]);
        }
    }

    private final void dumpHelp(PrintWriter printWriter) {
        printWriter.println("Let <invocation> be:");
        printWriter.println("$ adb shell dumpsys activity service com.android.systemui/.SystemUIService");
        printWriter.println();
        printWriter.println("Most common usage:");
        printWriter.println("$ <invocation> <targets>");
        printWriter.println("$ <invocation> NotifLog");
        printWriter.println("$ <invocation> StatusBar FalsingManager BootCompleteCacheImpl");
        printWriter.println("etc.");
        printWriter.println();
        printWriter.println("Special commands:");
        printWriter.println("$ <invocation> dumpables");
        printWriter.println("$ <invocation> buffers");
        printWriter.println("$ <invocation> bugreport-critical");
        printWriter.println("$ <invocation> bugreport-normal");
        printWriter.println();
        printWriter.println("Targets can be listed:");
        printWriter.println("$ <invocation> --list");
        printWriter.println("$ <invocation> dumpables --list");
        printWriter.println("$ <invocation> buffers --list");
        printWriter.println();
        printWriter.println("Show only the most recent N lines of buffers");
        printWriter.println("$ <invocation> NotifLog --tail 30");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0067, code lost:
        r1.setTailLength(((java.lang.Number) readArgument(r9, r2, com.android.systemui.dump.DumpHandler$parseArgs$2.INSTANCE)).intValue());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x007f, code lost:
        r1.setListOnly(true);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x008b, code lost:
        r1.setCommand("help");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x00a8, code lost:
        throw new com.android.systemui.dump.ArgParseException("Unknown flag: " + r2);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private final com.android.systemui.dump.ParsedArgs parseArgs(java.lang.String[] r9) {
        /*
            r8 = this;
            java.util.List r0 = kotlin.collections.ArraysKt___ArraysKt.toMutableList((T[]) r9)
            com.android.systemui.dump.ParsedArgs r1 = new com.android.systemui.dump.ParsedArgs
            r1.<init>(r9, r0)
            java.util.Iterator r9 = r0.iterator()
        L_0x000d:
            boolean r2 = r9.hasNext()
            r3 = 1
            r4 = 0
            if (r2 == 0) goto L_0x00a9
            java.lang.Object r2 = r9.next()
            java.lang.String r2 = (java.lang.String) r2
            r5 = 2
            r6 = 0
            java.lang.String r7 = "-"
            boolean r4 = kotlin.text.StringsKt__StringsJVMKt.startsWith$default(r2, r7, r4, r5, r6)
            if (r4 == 0) goto L_0x000d
            r9.remove()
            int r4 = r2.hashCode()
            switch(r4) {
                case 1499: goto L_0x0083;
                case 1503: goto L_0x0077;
                case 1511: goto L_0x005f;
                case 1056887741: goto L_0x004b;
                case 1333069025: goto L_0x0042;
                case 1333192254: goto L_0x0039;
                case 1333422576: goto L_0x0030;
                default: goto L_0x002f;
            }
        L_0x002f:
            goto L_0x0092
        L_0x0030:
            java.lang.String r3 = "--tail"
            boolean r3 = r2.equals(r3)
            if (r3 == 0) goto L_0x0092
            goto L_0x0067
        L_0x0039:
            java.lang.String r4 = "--list"
            boolean r4 = r2.equals(r4)
            if (r4 == 0) goto L_0x0092
            goto L_0x007f
        L_0x0042:
            java.lang.String r3 = "--help"
            boolean r3 = r2.equals(r3)
            if (r3 == 0) goto L_0x0092
            goto L_0x008b
        L_0x004b:
            java.lang.String r3 = "--dump-priority"
            boolean r4 = r2.equals(r3)
            if (r4 == 0) goto L_0x0092
            com.android.systemui.dump.DumpHandler$parseArgs$1 r2 = com.android.systemui.dump.DumpHandler$parseArgs$1.INSTANCE
            java.lang.Object r2 = r8.readArgument(r9, r3, r2)
            java.lang.String r2 = (java.lang.String) r2
            r1.setDumpPriority(r2)
            goto L_0x000d
        L_0x005f:
            java.lang.String r3 = "-t"
            boolean r3 = r2.equals(r3)
            if (r3 == 0) goto L_0x0092
        L_0x0067:
            com.android.systemui.dump.DumpHandler$parseArgs$2 r3 = com.android.systemui.dump.DumpHandler$parseArgs$2.INSTANCE
            java.lang.Object r2 = r8.readArgument(r9, r2, r3)
            java.lang.Number r2 = (java.lang.Number) r2
            int r2 = r2.intValue()
            r1.setTailLength(r2)
            goto L_0x000d
        L_0x0077:
            java.lang.String r4 = "-l"
            boolean r4 = r2.equals(r4)
            if (r4 == 0) goto L_0x0092
        L_0x007f:
            r1.setListOnly(r3)
            goto L_0x000d
        L_0x0083:
            java.lang.String r3 = "-h"
            boolean r3 = r2.equals(r3)
            if (r3 == 0) goto L_0x0092
        L_0x008b:
            java.lang.String r2 = "help"
            r1.setCommand(r2)
            goto L_0x000d
        L_0x0092:
            com.android.systemui.dump.ArgParseException r8 = new com.android.systemui.dump.ArgParseException
            java.lang.StringBuilder r9 = new java.lang.StringBuilder
            r9.<init>()
            java.lang.String r0 = "Unknown flag: "
            r9.append(r0)
            r9.append(r2)
            java.lang.String r9 = r9.toString()
            r8.<init>(r9)
            throw r8
        L_0x00a9:
            java.lang.String r8 = r1.getCommand()
            if (r8 != 0) goto L_0x00cd
            boolean r8 = r0.isEmpty()
            r8 = r8 ^ r3
            if (r8 == 0) goto L_0x00cd
            java.lang.String[] r8 = com.android.systemui.dump.DumpHandlerKt.COMMANDS
            java.lang.Object r9 = r0.get(r4)
            boolean r8 = kotlin.collections.ArraysKt___ArraysKt.contains(r8, r9)
            if (r8 == 0) goto L_0x00cd
            java.lang.Object r8 = r0.remove(r4)
            java.lang.String r8 = (java.lang.String) r8
            r1.setCommand(r8)
        L_0x00cd:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.dump.DumpHandler.parseArgs(java.lang.String[]):com.android.systemui.dump.ParsedArgs");
    }

    private final <T> T readArgument(Iterator<String> it, String str, Function1<? super String, ? extends T> function1) {
        if (it.hasNext()) {
            String next = it.next();
            try {
                T invoke = function1.invoke(next);
                it.remove();
                return invoke;
            } catch (Exception unused) {
                throw new ArgParseException("Invalid argument '" + next + "' for flag " + str);
            }
        } else {
            throw new ArgParseException("Missing argument for " + str);
        }
    }
}
