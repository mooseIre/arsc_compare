package com.android.systemui.media;

import android.app.Notification;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.media.session.MediaSession;
import android.service.notification.StatusBarNotification;
import java.util.List;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Ref$ObjectRef;

/* access modifiers changed from: package-private */
/* compiled from: MediaDataManager.kt */
public final class MediaDataManager$loadMediaDataInBg$1 implements Runnable {
    final /* synthetic */ List $actionIcons;
    final /* synthetic */ List $actionsToShowCollapsed;
    final /* synthetic */ String $app;
    final /* synthetic */ Icon $artWorkIcon;
    final /* synthetic */ Ref$ObjectRef $artist;
    final /* synthetic */ int $bgColor;
    final /* synthetic */ String $key;
    final /* synthetic */ Notification $notif;
    final /* synthetic */ String $oldKey;
    final /* synthetic */ StatusBarNotification $sbn;
    final /* synthetic */ Drawable $smallIconDrawable;
    final /* synthetic */ Ref$ObjectRef $song;
    final /* synthetic */ MediaSession.Token $token;
    final /* synthetic */ MediaDataManager this$0;

    MediaDataManager$loadMediaDataInBg$1(MediaDataManager mediaDataManager, String str, String str2, StatusBarNotification statusBarNotification, int i, String str3, Drawable drawable, Ref$ObjectRef ref$ObjectRef, Ref$ObjectRef ref$ObjectRef2, Icon icon, List list, List list2, MediaSession.Token token, Notification notification) {
        this.this$0 = mediaDataManager;
        this.$key = str;
        this.$oldKey = str2;
        this.$sbn = statusBarNotification;
        this.$bgColor = i;
        this.$app = str3;
        this.$smallIconDrawable = drawable;
        this.$artist = ref$ObjectRef;
        this.$song = ref$ObjectRef2;
        this.$artWorkIcon = icon;
        this.$actionIcons = list;
        this.$actionsToShowCollapsed = list2;
        this.$token = token;
        this.$notif = notification;
    }

    public final void run() {
        MediaData mediaData = (MediaData) this.this$0.mediaEntries.get(this.$key);
        Runnable resumeAction = mediaData != null ? mediaData.getResumeAction() : null;
        MediaData mediaData2 = (MediaData) this.this$0.mediaEntries.get(this.$key);
        boolean z = mediaData2 != null && mediaData2.getHasCheckedForResume();
        MediaData mediaData3 = (MediaData) this.this$0.mediaEntries.get(this.$key);
        boolean active = mediaData3 != null ? mediaData3.getActive() : true;
        MediaDataManager mediaDataManager = this.this$0;
        String str = this.$key;
        String str2 = this.$oldKey;
        Icon icon = this.$artWorkIcon;
        List list = this.$actionIcons;
        List list2 = this.$actionsToShowCollapsed;
        String packageName = this.$sbn.getPackageName();
        Intrinsics.checkExpressionValueIsNotNull(packageName, "sbn.packageName");
        mediaDataManager.onMediaDataLoaded(str, str2, new MediaData(this.$sbn.getNormalizedUserId(), true, this.$bgColor, this.$app, this.$smallIconDrawable, this.$artist.element, this.$song.element, icon, list, list2, packageName, this.$token, this.$notif.contentIntent, null, active, resumeAction, false, this.$key, z, 65536, null));
    }
}
