package com.android.systemui.media;

import android.content.ComponentName;
import android.content.pm.ComponentInfo;
import android.content.pm.ResolveInfo;
import java.util.List;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: MediaResumeListener.kt */
final class MediaResumeListener$onMediaDataLoaded$1 implements Runnable {
    final /* synthetic */ List $inf;
    final /* synthetic */ String $key;
    final /* synthetic */ MediaResumeListener this$0;

    MediaResumeListener$onMediaDataLoaded$1(MediaResumeListener mediaResumeListener, String str, List list) {
        this.this$0 = mediaResumeListener;
        this.$key = str;
        this.$inf = list;
    }

    public final void run() {
        MediaResumeListener mediaResumeListener = this.this$0;
        String str = this.$key;
        List list = this.$inf;
        if (list != null) {
            Object obj = list.get(0);
            Intrinsics.checkExpressionValueIsNotNull(obj, "inf!!.get(0)");
            ComponentInfo componentInfo = ((ResolveInfo) obj).getComponentInfo();
            Intrinsics.checkExpressionValueIsNotNull(componentInfo, "inf!!.get(0).componentInfo");
            ComponentName componentName = componentInfo.getComponentName();
            Intrinsics.checkExpressionValueIsNotNull(componentName, "inf!!.get(0).componentInfo.componentName");
            mediaResumeListener.tryUpdateResumptionList(str, componentName);
            return;
        }
        Intrinsics.throwNpe();
        throw null;
    }
}
