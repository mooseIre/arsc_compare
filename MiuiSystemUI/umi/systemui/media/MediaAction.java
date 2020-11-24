package com.android.systemui.media;

import android.graphics.drawable.Drawable;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: MediaData.kt */
public final class MediaAction {
    @Nullable
    private final Runnable action;
    @Nullable
    private final CharSequence contentDescription;
    @Nullable
    private final Drawable drawable;

    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MediaAction)) {
            return false;
        }
        MediaAction mediaAction = (MediaAction) obj;
        return Intrinsics.areEqual((Object) this.drawable, (Object) mediaAction.drawable) && Intrinsics.areEqual((Object) this.action, (Object) mediaAction.action) && Intrinsics.areEqual((Object) this.contentDescription, (Object) mediaAction.contentDescription);
    }

    public int hashCode() {
        Drawable drawable2 = this.drawable;
        int i = 0;
        int hashCode = (drawable2 != null ? drawable2.hashCode() : 0) * 31;
        Runnable runnable = this.action;
        int hashCode2 = (hashCode + (runnable != null ? runnable.hashCode() : 0)) * 31;
        CharSequence charSequence = this.contentDescription;
        if (charSequence != null) {
            i = charSequence.hashCode();
        }
        return hashCode2 + i;
    }

    @NotNull
    public String toString() {
        return "MediaAction(drawable=" + this.drawable + ", action=" + this.action + ", contentDescription=" + this.contentDescription + ")";
    }

    public MediaAction(@Nullable Drawable drawable2, @Nullable Runnable runnable, @Nullable CharSequence charSequence) {
        this.drawable = drawable2;
        this.action = runnable;
        this.contentDescription = charSequence;
    }

    @Nullable
    public final Drawable getDrawable() {
        return this.drawable;
    }

    @Nullable
    public final Runnable getAction() {
        return this.action;
    }

    @Nullable
    public final CharSequence getContentDescription() {
        return this.contentDescription;
    }
}
