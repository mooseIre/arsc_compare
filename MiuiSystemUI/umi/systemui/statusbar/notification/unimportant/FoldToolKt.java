package com.android.systemui.statusbar.notification.unimportant;

import com.android.systemui.statusbar.notification.NotificationUtil;
import java.util.List;
import kotlin.TypeCastException;
import kotlin.collections.CollectionsKt___CollectionsKt;
import kotlin.jvm.internal.Intrinsics;

public final class FoldToolKt {
    public static final boolean isXmsfNotificationChannel(String str, String str2) {
        String str3;
        if (!NotificationUtil.isXmsfChannel(str, str2)) {
            return false;
        }
        String str4 = "mipush|" + str + "|pre";
        if (str2 != null) {
            int length = str4.length();
            if (str2 != null) {
                str3 = str2.substring(length);
                Intrinsics.checkExpressionValueIsNotNull(str3, "(this as java.lang.String).substring(startIndex)");
            } else {
                throw new TypeCastException("null cannot be cast to non-null type java.lang.String");
            }
        } else {
            str3 = null;
        }
        List<String> xmsfNotificationChannel = FoldCloudDataHelper.INSTANCE.getXmsfNotificationChannel();
        if (xmsfNotificationChannel != null) {
            return CollectionsKt___CollectionsKt.contains(xmsfNotificationChannel, str3);
        }
        return false;
    }

    public static final boolean isLocalWhitelist(String str) {
        List<String> localWhitelist = FoldCloudDataHelper.INSTANCE.getLocalWhitelist();
        if (localWhitelist != null) {
            return CollectionsKt___CollectionsKt.contains(localWhitelist, str);
        }
        return false;
    }
}
