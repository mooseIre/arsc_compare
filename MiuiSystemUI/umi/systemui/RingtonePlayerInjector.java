package com.android.systemui;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.UserHandle;
import android.util.Log;
import java.io.File;

public class RingtonePlayerInjector {
    private static final Uri INCALL_NOTIFICATION_URI = Uri.parse("file:///system/media/audio/ui/InCallNotification.ogg");
    private static final Uri Q_INCALL_NOTIFICATION_URI = Uri.parse("file:///product/media/audio/ui/InCallNotification.ogg");
    private static boolean sPlayInCallNotification;

    public static void init(Context context) {
        sPlayInCallNotification = context.getResources().getBoolean(C0010R$bool.play_incall_notification);
        Log.d("MiuiRingtonePlayer", "RingtonePlayer sPlayInCallNotification=" + sPlayInCallNotification);
    }

    public static Uri fallbackInCallNotification(Uri uri) {
        if (!isInCallNotification(uri)) {
            return uri;
        }
        if (!sPlayInCallNotification) {
            return null;
        }
        return INCALL_NOTIFICATION_URI;
    }

    private static boolean isInCallNotification(Uri uri) {
        return Q_INCALL_NOTIFICATION_URI.equals(uri);
    }

    public static UserHandle fallbackUserHandle(UserHandle userHandle) {
        return 999 == userHandle.getIdentifier() ? UserHandle.SYSTEM : userHandle;
    }

    public static Uri fallbackNotificationUri(Uri uri, AudioAttributes audioAttributes) {
        if (uri == null) {
            return uri;
        }
        if (audioAttributes.getUsage() == 5) {
            String scheme = uri.getScheme();
            if ((scheme == null || scheme.equals("file")) && !new File(uri.getPath()).exists()) {
                return RingtoneManager.getDefaultUri(2);
            }
            return uri;
        } else if (audioAttributes.getUsage() != 6) {
            return uri;
        } else {
            String scheme2 = uri.getScheme();
            return ((scheme2 == null || scheme2.equals("file")) && !new File(uri.getPath()).exists()) ? RingtoneManager.getDefaultUri(1) : uri;
        }
    }
}
