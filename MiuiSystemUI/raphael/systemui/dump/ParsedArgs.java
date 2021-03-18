package com.android.systemui.dump;

import java.util.List;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* access modifiers changed from: package-private */
/* compiled from: DumpHandler.kt */
public final class ParsedArgs {
    @Nullable
    private String command;
    @Nullable
    private String dumpPriority;
    private boolean listOnly;
    @NotNull
    private final List<String> nonFlagArgs;
    @NotNull
    private final String[] rawArgs;
    private int tailLength;

    public ParsedArgs(@NotNull String[] strArr, @NotNull List<String> list) {
        Intrinsics.checkParameterIsNotNull(strArr, "rawArgs");
        Intrinsics.checkParameterIsNotNull(list, "nonFlagArgs");
        this.rawArgs = strArr;
        this.nonFlagArgs = list;
    }

    @NotNull
    public final String[] getRawArgs() {
        return this.rawArgs;
    }

    @NotNull
    public final List<String> getNonFlagArgs() {
        return this.nonFlagArgs;
    }

    @Nullable
    public final String getDumpPriority() {
        return this.dumpPriority;
    }

    public final void setDumpPriority(@Nullable String str) {
        this.dumpPriority = str;
    }

    public final int getTailLength() {
        return this.tailLength;
    }

    public final void setTailLength(int i) {
        this.tailLength = i;
    }

    @Nullable
    public final String getCommand() {
        return this.command;
    }

    public final void setCommand(@Nullable String str) {
        this.command = str;
    }

    public final boolean getListOnly() {
        return this.listOnly;
    }

    public final void setListOnly(boolean z) {
        this.listOnly = z;
    }
}
