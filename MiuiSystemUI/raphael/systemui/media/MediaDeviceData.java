package com.android.systemui.media;

import android.graphics.drawable.Drawable;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: MediaData.kt */
public final class MediaDeviceData {
    private final boolean enabled;
    @Nullable
    private final Drawable icon;
    @Nullable
    private final String name;

    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MediaDeviceData)) {
            return false;
        }
        MediaDeviceData mediaDeviceData = (MediaDeviceData) obj;
        return this.enabled == mediaDeviceData.enabled && Intrinsics.areEqual(this.icon, mediaDeviceData.icon) && Intrinsics.areEqual(this.name, mediaDeviceData.name);
    }

    public int hashCode() {
        boolean z = this.enabled;
        if (z) {
            z = true;
        }
        int i = z ? 1 : 0;
        int i2 = z ? 1 : 0;
        int i3 = z ? 1 : 0;
        int i4 = i * 31;
        Drawable drawable = this.icon;
        int i5 = 0;
        int hashCode = (i4 + (drawable != null ? drawable.hashCode() : 0)) * 31;
        String str = this.name;
        if (str != null) {
            i5 = str.hashCode();
        }
        return hashCode + i5;
    }

    @NotNull
    public String toString() {
        return "MediaDeviceData(enabled=" + this.enabled + ", icon=" + this.icon + ", name=" + this.name + ")";
    }

    public MediaDeviceData(boolean z, @Nullable Drawable drawable, @Nullable String str) {
        this.enabled = z;
        this.icon = drawable;
        this.name = str;
    }

    public final boolean getEnabled() {
        return this.enabled;
    }

    @Nullable
    public final Drawable getIcon() {
        return this.icon;
    }

    @Nullable
    public final String getName() {
        return this.name;
    }
}
