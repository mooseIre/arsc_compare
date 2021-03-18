package com.android.systemui.media;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: MediaDataManager.kt */
public final class MediaDataManager$appChangeReceiver$1 extends BroadcastReceiver {
    final /* synthetic */ MediaDataManager this$0;

    /* JADX WARN: Incorrect args count in method signature: ()V */
    MediaDataManager$appChangeReceiver$1(MediaDataManager mediaDataManager) {
        this.this$0 = mediaDataManager;
    }

    public void onReceive(@NotNull Context context, @NotNull Intent intent) {
        String[] stringArrayExtra;
        String encodedSchemeSpecificPart;
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(intent, "intent");
        String action = intent.getAction();
        if (action != null) {
            int hashCode = action.hashCode();
            if (hashCode != -1001645458) {
                if (hashCode != -757780528) {
                    if (hashCode != 525384130 || !action.equals("android.intent.action.PACKAGE_REMOVED")) {
                        return;
                    }
                } else if (!action.equals("android.intent.action.PACKAGE_RESTARTED")) {
                    return;
                }
                Uri data = intent.getData();
                if (!(data == null || (encodedSchemeSpecificPart = data.getEncodedSchemeSpecificPart()) == null)) {
                    this.this$0.removeAllForPackage(encodedSchemeSpecificPart);
                }
            } else if (action.equals("android.intent.action.PACKAGES_SUSPENDED") && (stringArrayExtra = intent.getStringArrayExtra("android.intent.extra.changed_package_list")) != null) {
                for (String str : stringArrayExtra) {
                    MediaDataManager mediaDataManager = this.this$0;
                    Intrinsics.checkExpressionValueIsNotNull(str, "it");
                    mediaDataManager.removeAllForPackage(str);
                }
            }
        }
    }
}
