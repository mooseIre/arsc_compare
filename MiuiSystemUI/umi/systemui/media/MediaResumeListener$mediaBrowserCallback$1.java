package com.android.systemui.media;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.media.MediaDescription;
import android.media.session.MediaSession;
import android.util.Log;
import com.android.systemui.media.ResumeMediaBrowser;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: MediaResumeListener.kt */
public final class MediaResumeListener$mediaBrowserCallback$1 extends ResumeMediaBrowser.Callback {
    final /* synthetic */ MediaResumeListener this$0;

    /* JADX WARN: Incorrect args count in method signature: ()V */
    MediaResumeListener$mediaBrowserCallback$1(MediaResumeListener mediaResumeListener) {
        this.this$0 = mediaResumeListener;
    }

    @Override // com.android.systemui.media.ResumeMediaBrowser.Callback
    public void addTrack(@NotNull MediaDescription mediaDescription, @NotNull ComponentName componentName, @NotNull ResumeMediaBrowser resumeMediaBrowser) {
        Intrinsics.checkParameterIsNotNull(mediaDescription, "desc");
        Intrinsics.checkParameterIsNotNull(componentName, "component");
        Intrinsics.checkParameterIsNotNull(resumeMediaBrowser, "browser");
        MediaSession.Token token = resumeMediaBrowser.getToken();
        PendingIntent appIntent = resumeMediaBrowser.getAppIntent();
        PackageManager packageManager = this.this$0.context.getPackageManager();
        String packageName = componentName.getPackageName();
        Intrinsics.checkExpressionValueIsNotNull(packageName, "component.packageName");
        Runnable runnable = this.this$0.getResumeAction(componentName);
        try {
            CharSequence applicationLabel = packageManager.getApplicationLabel(packageManager.getApplicationInfo(componentName.getPackageName(), 0));
            Intrinsics.checkExpressionValueIsNotNull(applicationLabel, "pm.getApplicationLabel(\n…omponent.packageName, 0))");
            packageName = applicationLabel;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("MediaResumeListener", "Error getting package information", e);
        }
        Log.d("MediaResumeListener", "Adding resume controls " + mediaDescription);
        MediaDataManager access$getMediaDataManager$p = MediaResumeListener.access$getMediaDataManager$p(this.this$0);
        int i = this.this$0.currentUserId;
        Intrinsics.checkExpressionValueIsNotNull(token, "token");
        String obj = packageName.toString();
        Intrinsics.checkExpressionValueIsNotNull(appIntent, "appIntent");
        String packageName2 = componentName.getPackageName();
        Intrinsics.checkExpressionValueIsNotNull(packageName2, "component.packageName");
        access$getMediaDataManager$p.addResumptionControls(i, mediaDescription, runnable, token, obj, appIntent, packageName2);
    }
}
