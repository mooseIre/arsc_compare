package com.android.systemui.dump;

import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* access modifiers changed from: package-private */
/* compiled from: DumpManager.kt */
public final class RegisteredDumpable<T> {
    private final T dumpable;
    @NotNull
    private final String name;

    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof RegisteredDumpable)) {
            return false;
        }
        RegisteredDumpable registeredDumpable = (RegisteredDumpable) obj;
        return Intrinsics.areEqual(this.name, registeredDumpable.name) && Intrinsics.areEqual(this.dumpable, registeredDumpable.dumpable);
    }

    public int hashCode() {
        String str = this.name;
        int i = 0;
        int hashCode = (str != null ? str.hashCode() : 0) * 31;
        T t = this.dumpable;
        if (t != null) {
            i = t.hashCode();
        }
        return hashCode + i;
    }

    @NotNull
    public String toString() {
        return "RegisteredDumpable(name=" + this.name + ", dumpable=" + ((Object) this.dumpable) + ")";
    }

    public RegisteredDumpable(@NotNull String str, T t) {
        Intrinsics.checkParameterIsNotNull(str, "name");
        this.name = str;
        this.dumpable = t;
    }

    @NotNull
    public final String getName() {
        return this.name;
    }

    public final T getDumpable() {
        return this.dumpable;
    }
}
