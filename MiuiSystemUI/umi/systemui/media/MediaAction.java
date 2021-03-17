package com.android.systemui.media;

import android.app.Notification;
import android.graphics.drawable.Drawable;
import kotlin.jvm.internal.DefaultConstructorMarker;
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
    @Nullable
    private final Notification.Action notificationAction;

    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MediaAction)) {
            return false;
        }
        MediaAction mediaAction = (MediaAction) obj;
        return Intrinsics.areEqual(this.drawable, mediaAction.drawable) && Intrinsics.areEqual(this.action, mediaAction.action) && Intrinsics.areEqual(this.contentDescription, mediaAction.contentDescription) && Intrinsics.areEqual(this.notificationAction, mediaAction.notificationAction);
    }

    public int hashCode() {
        Drawable drawable2 = this.drawable;
        int i = 0;
        int hashCode = (drawable2 != null ? drawable2.hashCode() : 0) * 31;
        Runnable runnable = this.action;
        int hashCode2 = (hashCode + (runnable != null ? runnable.hashCode() : 0)) * 31;
        CharSequence charSequence = this.contentDescription;
        int hashCode3 = (hashCode2 + (charSequence != null ? charSequence.hashCode() : 0)) * 31;
        Notification.Action action2 = this.notificationAction;
        if (action2 != null) {
            i = action2.hashCode();
        }
        return hashCode3 + i;
    }

    @NotNull
    public String toString() {
        return "MediaAction(drawable=" + this.drawable + ", action=" + this.action + ", contentDescription=" + this.contentDescription + ", notificationAction=" + this.notificationAction + ")";
    }

    public MediaAction(@Nullable Drawable drawable2, @Nullable Runnable runnable, @Nullable CharSequence charSequence, @Nullable Notification.Action action2) {
        this.drawable = drawable2;
        this.action = runnable;
        this.contentDescription = charSequence;
        this.notificationAction = action2;
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

    /* JADX INFO: this call moved to the top of the method (can break code semantics) */
    public /* synthetic */ MediaAction(Drawable drawable2, Runnable runnable, CharSequence charSequence, Notification.Action action2, int i, DefaultConstructorMarker defaultConstructorMarker) {
        this(drawable2, runnable, charSequence, (i & 8) != 0 ? null : action2);
    }

    @Nullable
    public final Notification.Action getNotificationAction() {
        return this.notificationAction;
    }
}
