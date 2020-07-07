package com.android.systemui.miui.volume;

import android.content.Context;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.MiuiSettings;
import com.android.systemui.Util;

public class VolumeUtil {
    public static void setSilenceMode(Context context, int i, Uri uri) {
        MiuiSettings.SilenceMode.setSilenceMode(context, i, uri);
        if (context.getResources().getBoolean(R$bool.miui_config_enableRingerRelieveSound) && i == 0) {
            Util.playRingtoneAsync(context, RingtoneManager.getActualDefaultRingtoneUri(context, 2), 5);
        }
    }
}
