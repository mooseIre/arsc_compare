package com.android.systemui.media;

import kotlin.jvm.internal.DefaultConstructorMarker;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: MediaViewController.kt */
final class CacheKey {
    private float expansion;
    private int heightMeasureSpec;
    private int widthMeasureSpec;

    public CacheKey() {
        this(0, 0, 0.0f, 7, null);
    }

    public static /* synthetic */ CacheKey copy$default(CacheKey cacheKey, int i, int i2, float f, int i3, Object obj) {
        if ((i3 & 1) != 0) {
            i = cacheKey.widthMeasureSpec;
        }
        if ((i3 & 2) != 0) {
            i2 = cacheKey.heightMeasureSpec;
        }
        if ((i3 & 4) != 0) {
            f = cacheKey.expansion;
        }
        return cacheKey.copy(i, i2, f);
    }

    @NotNull
    public final CacheKey copy(int i, int i2, float f) {
        return new CacheKey(i, i2, f);
    }

    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof CacheKey)) {
            return false;
        }
        CacheKey cacheKey = (CacheKey) obj;
        return this.widthMeasureSpec == cacheKey.widthMeasureSpec && this.heightMeasureSpec == cacheKey.heightMeasureSpec && Float.compare(this.expansion, cacheKey.expansion) == 0;
    }

    public int hashCode() {
        return (((Integer.hashCode(this.widthMeasureSpec) * 31) + Integer.hashCode(this.heightMeasureSpec)) * 31) + Float.hashCode(this.expansion);
    }

    @NotNull
    public String toString() {
        return "CacheKey(widthMeasureSpec=" + this.widthMeasureSpec + ", heightMeasureSpec=" + this.heightMeasureSpec + ", expansion=" + this.expansion + ")";
    }

    public CacheKey(int i, int i2, float f) {
        this.widthMeasureSpec = i;
        this.heightMeasureSpec = i2;
        this.expansion = f;
    }

    public final void setWidthMeasureSpec(int i) {
        this.widthMeasureSpec = i;
    }

    public final void setHeightMeasureSpec(int i) {
        this.heightMeasureSpec = i;
    }

    /* JADX INFO: this call moved to the top of the method (can break code semantics) */
    public /* synthetic */ CacheKey(int i, int i2, float f, int i3, DefaultConstructorMarker defaultConstructorMarker) {
        this((i3 & 1) != 0 ? -1 : i, (i3 & 2) != 0 ? -1 : i2, (i3 & 4) != 0 ? 0.0f : f);
    }

    public final void setExpansion(float f) {
        this.expansion = f;
    }
}
