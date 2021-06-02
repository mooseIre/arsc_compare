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
import kotlin.collections.ArraysKt___ArraysKt;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.text.StringsKt__StringsJVMKt;
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
            for (String str : list) {
                this.dumpManager.dumpTarget(str, fileDescriptor, printWriter, parsedArgs.getRawArgs(), parsedArgs.getTailLength());
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

    private final ParsedArgs parseArgs(String[] strArr) {
        List list = ArraysKt___ArraysKt.toMutableList(strArr);
        ParsedArgs parsedArgs = new ParsedArgs(strArr, list);
        Iterator<String> it = list.iterator();
        while (it.hasNext()) {
            String next = it.next();
            if (StringsKt__StringsJVMKt.startsWith$default(next, "-", false, 2, null)) {
                it.remove();
                switch (next.hashCode()) {
                    case 1499:
                        if (!next.equals("-h")) {
                            throw new ArgParseException("Unknown flag: " + next);
                        }
                        parsedArgs.setCommand("help");
                        break;
                    case 1503:
                        if (!next.equals("-l")) {
                            throw new ArgParseException("Unknown flag: " + next);
                        }
                        parsedArgs.setListOnly(true);
                        break;
                    case 1511:
                        if (!next.equals("-t")) {
                            throw new ArgParseException("Unknown flag: " + next);
                        }
                        parsedArgs.setTailLength(((Number) readArgument(it, next, DumpHandler$parseArgs$2.INSTANCE)).intValue());
                        break;
                    case 1056887741:
                        if (next.equals("--dump-priority")) {
                            parsedArgs.setDumpPriority((String) readArgument(it, "--dump-priority", DumpHandler$parseArgs$1.INSTANCE));
                            break;
                        } else {
                            throw new ArgParseException("Unknown flag: " + next);
                        }
                    case 1333069025:
                        if (!next.equals("--help")) {
                            throw new ArgParseException("Unknown flag: " + next);
                        }
                        parsedArgs.setCommand("help");
                        break;
                    case 1333192254:
                        if (!next.equals("--list")) {
                            throw new ArgParseException("Unknown flag: " + next);
                        }
                        parsedArgs.setListOnly(true);
                        break;
                    case 1333422576:
                        if (!next.equals("--tail")) {
                            throw new ArgParseException("Unknown flag: " + next);
                        }
                        parsedArgs.setTailLength(((Number) readArgument(it, next, DumpHandler$parseArgs$2.INSTANCE)).intValue());
                        break;
                    default:
                        throw new ArgParseException("Unknown flag: " + next);
                }
            }
        }
        if (parsedArgs.getCommand() == null && (!list.isEmpty()) && (ArraysKt___ArraysKt.contains(DumpHandlerKt.access$getCOMMANDS$p(), list.get(0)))) {
            parsedArgs.setCommand((String) list.remove(0));
        }
        return parsedArgs;
    }

    private final <T> T readArgument(Iterator<String> it, String str, Function1<? super String, ? extends T> function1) {
        if (it.hasNext()) {
            String next = it.next();
            try {
                T t = (T) function1.invoke(next);
                it.remove();
                return t;
            } catch (Exception unused) {
                throw new ArgParseException("Invalid argument '" + next + "' for flag " + str);
            }
        } else {
            throw new ArgParseException("Missing argument for " + str);
        }
    }
}
