package com.android.systemui.bubbles;

import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: BubbleDataRepository.kt */
public final class ShortcutKey {
    @NotNull
    private final String pkg;
    private final int userId;

    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ShortcutKey)) {
            return false;
        }
        ShortcutKey shortcutKey = (ShortcutKey) obj;
        return this.userId == shortcutKey.userId && Intrinsics.areEqual(this.pkg, shortcutKey.pkg);
    }

    public int hashCode() {
        int hashCode = Integer.hashCode(this.userId) * 31;
        String str = this.pkg;
        return hashCode + (str != null ? str.hashCode() : 0);
    }

    @NotNull
    public String toString() {
        return "ShortcutKey(userId=" + this.userId + ", pkg=" + this.pkg + ")";
    }

    public ShortcutKey(int i, @NotNull String str) {
        Intrinsics.checkParameterIsNotNull(str, "pkg");
        this.userId = i;
        this.pkg = str;
    }

    @NotNull
    public final String getPkg() {
        return this.pkg;
    }

    public final int getUserId() {
        return this.userId;
    }
}
