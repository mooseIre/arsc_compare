package com.android.systemui.media;

import android.app.PendingIntent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.media.session.MediaSession;
import java.util.List;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;

public final class MediaData {
    private final List<MediaAction> actions;
    private final List<Integer> actionsToShowInCompact;
    private boolean active;
    private final String app;
    private final Drawable appIcon;
    private final CharSequence artist;
    private final Icon artwork;
    private final int backgroundColor;
    private final PendingIntent clickIntent;
    private final MediaDeviceData device;
    private boolean hasCheckedForResume;
    private final boolean initialized;
    private final String notificationKey;
    private final String packageName;
    private Runnable resumeAction;
    private boolean resumption;
    private final CharSequence song;
    private final MediaSession.Token token;
    private final int userId;

    public static /* synthetic */ MediaData copy$default(MediaData mediaData, int i, boolean z, int i2, String str, Drawable drawable, CharSequence charSequence, CharSequence charSequence2, Icon icon, List list, List list2, String str2, MediaSession.Token token2, PendingIntent pendingIntent, MediaDeviceData mediaDeviceData, boolean z2, Runnable runnable, boolean z3, String str3, boolean z4, int i3, Object obj) {
        return mediaData.copy((i3 & 1) != 0 ? mediaData.userId : i, (i3 & 2) != 0 ? mediaData.initialized : z, (i3 & 4) != 0 ? mediaData.backgroundColor : i2, (i3 & 8) != 0 ? mediaData.app : str, (i3 & 16) != 0 ? mediaData.appIcon : drawable, (i3 & 32) != 0 ? mediaData.artist : charSequence, (i3 & 64) != 0 ? mediaData.song : charSequence2, (i3 & 128) != 0 ? mediaData.artwork : icon, (i3 & 256) != 0 ? mediaData.actions : list, (i3 & 512) != 0 ? mediaData.actionsToShowInCompact : list2, (i3 & 1024) != 0 ? mediaData.packageName : str2, (i3 & 2048) != 0 ? mediaData.token : token2, (i3 & 4096) != 0 ? mediaData.clickIntent : pendingIntent, (i3 & 8192) != 0 ? mediaData.device : mediaDeviceData, (i3 & 16384) != 0 ? mediaData.active : z2, (i3 & 32768) != 0 ? mediaData.resumeAction : runnable, (i3 & 65536) != 0 ? mediaData.resumption : z3, (i3 & 131072) != 0 ? mediaData.notificationKey : str3, (i3 & 262144) != 0 ? mediaData.hasCheckedForResume : z4);
    }

    public final MediaData copy(int i, boolean z, int i2, String str, Drawable drawable, CharSequence charSequence, CharSequence charSequence2, Icon icon, List<MediaAction> list, List<Integer> list2, String str2, MediaSession.Token token2, PendingIntent pendingIntent, MediaDeviceData mediaDeviceData, boolean z2, Runnable runnable, boolean z3, String str3, boolean z4) {
        Intrinsics.checkParameterIsNotNull(list, "actions");
        Intrinsics.checkParameterIsNotNull(list2, "actionsToShowInCompact");
        Intrinsics.checkParameterIsNotNull(str2, "packageName");
        return new MediaData(i, z, i2, str, drawable, charSequence, charSequence2, icon, list, list2, str2, token2, pendingIntent, mediaDeviceData, z2, runnable, z3, str3, z4);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MediaData)) {
            return false;
        }
        MediaData mediaData = (MediaData) obj;
        return this.userId == mediaData.userId && this.initialized == mediaData.initialized && this.backgroundColor == mediaData.backgroundColor && Intrinsics.areEqual(this.app, mediaData.app) && Intrinsics.areEqual(this.appIcon, mediaData.appIcon) && Intrinsics.areEqual(this.artist, mediaData.artist) && Intrinsics.areEqual(this.song, mediaData.song) && Intrinsics.areEqual(this.artwork, mediaData.artwork) && Intrinsics.areEqual(this.actions, mediaData.actions) && Intrinsics.areEqual(this.actionsToShowInCompact, mediaData.actionsToShowInCompact) && Intrinsics.areEqual(this.packageName, mediaData.packageName) && Intrinsics.areEqual(this.token, mediaData.token) && Intrinsics.areEqual(this.clickIntent, mediaData.clickIntent) && Intrinsics.areEqual(this.device, mediaData.device) && this.active == mediaData.active && Intrinsics.areEqual(this.resumeAction, mediaData.resumeAction) && this.resumption == mediaData.resumption && Intrinsics.areEqual(this.notificationKey, mediaData.notificationKey) && this.hasCheckedForResume == mediaData.hasCheckedForResume;
    }

    public int hashCode() {
        int hashCode = Integer.hashCode(this.userId) * 31;
        boolean z = this.initialized;
        int i = 1;
        if (z) {
            z = true;
        }
        int i2 = z ? 1 : 0;
        int i3 = z ? 1 : 0;
        int i4 = z ? 1 : 0;
        int hashCode2 = (((hashCode + i2) * 31) + Integer.hashCode(this.backgroundColor)) * 31;
        String str = this.app;
        int i5 = 0;
        int hashCode3 = (hashCode2 + (str != null ? str.hashCode() : 0)) * 31;
        Drawable drawable = this.appIcon;
        int hashCode4 = (hashCode3 + (drawable != null ? drawable.hashCode() : 0)) * 31;
        CharSequence charSequence = this.artist;
        int hashCode5 = (hashCode4 + (charSequence != null ? charSequence.hashCode() : 0)) * 31;
        CharSequence charSequence2 = this.song;
        int hashCode6 = (hashCode5 + (charSequence2 != null ? charSequence2.hashCode() : 0)) * 31;
        Icon icon = this.artwork;
        int hashCode7 = (hashCode6 + (icon != null ? icon.hashCode() : 0)) * 31;
        List<MediaAction> list = this.actions;
        int hashCode8 = (hashCode7 + (list != null ? list.hashCode() : 0)) * 31;
        List<Integer> list2 = this.actionsToShowInCompact;
        int hashCode9 = (hashCode8 + (list2 != null ? list2.hashCode() : 0)) * 31;
        String str2 = this.packageName;
        int hashCode10 = (hashCode9 + (str2 != null ? str2.hashCode() : 0)) * 31;
        MediaSession.Token token2 = this.token;
        int hashCode11 = (hashCode10 + (token2 != null ? token2.hashCode() : 0)) * 31;
        PendingIntent pendingIntent = this.clickIntent;
        int hashCode12 = (hashCode11 + (pendingIntent != null ? pendingIntent.hashCode() : 0)) * 31;
        MediaDeviceData mediaDeviceData = this.device;
        int hashCode13 = (hashCode12 + (mediaDeviceData != null ? mediaDeviceData.hashCode() : 0)) * 31;
        boolean z2 = this.active;
        if (z2) {
            z2 = true;
        }
        int i6 = z2 ? 1 : 0;
        int i7 = z2 ? 1 : 0;
        int i8 = z2 ? 1 : 0;
        int i9 = (hashCode13 + i6) * 31;
        Runnable runnable = this.resumeAction;
        int hashCode14 = (i9 + (runnable != null ? runnable.hashCode() : 0)) * 31;
        boolean z3 = this.resumption;
        if (z3) {
            z3 = true;
        }
        int i10 = z3 ? 1 : 0;
        int i11 = z3 ? 1 : 0;
        int i12 = z3 ? 1 : 0;
        int i13 = (hashCode14 + i10) * 31;
        String str3 = this.notificationKey;
        if (str3 != null) {
            i5 = str3.hashCode();
        }
        int i14 = (i13 + i5) * 31;
        boolean z4 = this.hasCheckedForResume;
        if (!z4) {
            i = z4 ? 1 : 0;
        }
        return i14 + i;
    }

    public String toString() {
        return "MediaData(userId=" + this.userId + ", initialized=" + this.initialized + ", backgroundColor=" + this.backgroundColor + ", app=" + this.app + ", appIcon=" + this.appIcon + ", artist=" + this.artist + ", song=" + this.song + ", artwork=" + this.artwork + ", actions=" + this.actions + ", actionsToShowInCompact=" + this.actionsToShowInCompact + ", packageName=" + this.packageName + ", token=" + this.token + ", clickIntent=" + this.clickIntent + ", device=" + this.device + ", active=" + this.active + ", resumeAction=" + this.resumeAction + ", resumption=" + this.resumption + ", notificationKey=" + this.notificationKey + ", hasCheckedForResume=" + this.hasCheckedForResume + ")";
    }

    public MediaData(int i, boolean z, int i2, String str, Drawable drawable, CharSequence charSequence, CharSequence charSequence2, Icon icon, List<MediaAction> list, List<Integer> list2, String str2, MediaSession.Token token2, PendingIntent pendingIntent, MediaDeviceData mediaDeviceData, boolean z2, Runnable runnable, boolean z3, String str3, boolean z4) {
        Intrinsics.checkParameterIsNotNull(list, "actions");
        Intrinsics.checkParameterIsNotNull(list2, "actionsToShowInCompact");
        Intrinsics.checkParameterIsNotNull(str2, "packageName");
        this.userId = i;
        this.initialized = z;
        this.backgroundColor = i2;
        this.app = str;
        this.appIcon = drawable;
        this.artist = charSequence;
        this.song = charSequence2;
        this.artwork = icon;
        this.actions = list;
        this.actionsToShowInCompact = list2;
        this.packageName = str2;
        this.token = token2;
        this.clickIntent = pendingIntent;
        this.device = mediaDeviceData;
        this.active = z2;
        this.resumeAction = runnable;
        this.resumption = z3;
        this.notificationKey = str3;
        this.hasCheckedForResume = z4;
    }

    public final int getUserId() {
        return this.userId;
    }

    public final int getBackgroundColor() {
        return this.backgroundColor;
    }

    public final String getApp() {
        return this.app;
    }

    public final Drawable getAppIcon() {
        return this.appIcon;
    }

    public final CharSequence getArtist() {
        return this.artist;
    }

    public final CharSequence getSong() {
        return this.song;
    }

    public final Icon getArtwork() {
        return this.artwork;
    }

    public final List<MediaAction> getActions() {
        return this.actions;
    }

    public final List<Integer> getActionsToShowInCompact() {
        return this.actionsToShowInCompact;
    }

    public final String getPackageName() {
        return this.packageName;
    }

    public final MediaSession.Token getToken() {
        return this.token;
    }

    public final PendingIntent getClickIntent() {
        return this.clickIntent;
    }

    public final MediaDeviceData getDevice() {
        return this.device;
    }

    public final boolean getActive() {
        return this.active;
    }

    public final void setActive(boolean z) {
        this.active = z;
    }

    public final Runnable getResumeAction() {
        return this.resumeAction;
    }

    public final void setResumeAction(Runnable runnable) {
        this.resumeAction = runnable;
    }

    public final boolean getResumption() {
        return this.resumption;
    }

    /* JADX INFO: this call moved to the top of the method (can break code semantics) */
    public /* synthetic */ MediaData(int i, boolean z, int i2, String str, Drawable drawable, CharSequence charSequence, CharSequence charSequence2, Icon icon, List list, List list2, String str2, MediaSession.Token token2, PendingIntent pendingIntent, MediaDeviceData mediaDeviceData, boolean z2, Runnable runnable, boolean z3, String str3, boolean z4, int i3, DefaultConstructorMarker defaultConstructorMarker) {
        this(i, (i3 & 2) != 0 ? false : z, i2, str, drawable, charSequence, charSequence2, icon, list, list2, str2, token2, pendingIntent, mediaDeviceData, z2, runnable, (i3 & 65536) != 0 ? false : z3, (i3 & 131072) != 0 ? null : str3, (i3 & 262144) != 0 ? false : z4);
    }

    public final boolean getHasCheckedForResume() {
        return this.hasCheckedForResume;
    }

    public final void setHasCheckedForResume(boolean z) {
        this.hasCheckedForResume = z;
    }
}
