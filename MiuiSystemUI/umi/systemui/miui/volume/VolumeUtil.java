package com.android.systemui.miui.volume;

import android.content.Context;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.MiuiSettings;
import com.android.systemui.Util;

public class VolumeUtil {
    public static void setSilenceMode(Context context, int i, Uri uri) {
        if (i == 1) {
            MiuiSettings.SoundMode.setSilenceModeOn(context, false);
            MiuiSettings.SoundMode.setZenModeOn(context, true, "miui_manual");
        } else if (i != 4) {
            MiuiSettings.SoundMode.setSilenceModeOn(context, false);
            MiuiSettings.SoundMode.setZenModeOn(context, false, "miui_manual");
        } else {
            MiuiSettings.SoundMode.setSilenceModeOn(context, true);
            MiuiSettings.SoundMode.setZenModeOn(context, false, "miui_manual");
        }
        if (context.getResources().getBoolean(R$bool.miui_config_enableRingerRelieveSound) && i == 0) {
            Util.playRingtoneAsync(context, RingtoneManager.getActualDefaultRingtoneUri(context, 2), 5);
        }
    }

    public static int getZenMode(Context context) {
        boolean isZenModeOn = MiuiSettings.SoundMode.isZenModeOn(context);
        if (MiuiSettings.SoundMode.isSilenceModeOn(context)) {
            return 4;
        }
        return isZenModeOn ? 1 : 0;
    }

    public static void setZenModeForDuration(Context context, int i, int i2) {
        MiuiSettings.SoundMode.setZenModeForDuration(context, i2, "miui_manual");
    }
}
