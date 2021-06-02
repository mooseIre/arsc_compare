package com.android.systemui.controlcenter.utils;

import android.util.SparseArray;
import com.android.systemui.C0013R$drawable;

public class MiuiQSIcons {
    private static SparseArray<Integer> sQSIconMapping;

    public static int getQSIcons(Integer num, boolean z) {
        if (!z) {
            return num.intValue();
        }
        if (sQSIconMapping == null) {
            SparseArray<Integer> sparseArray = new SparseArray<>();
            sQSIconMapping = sparseArray;
            sparseArray.put(C0013R$drawable.ic_signal_airplane_enable, Integer.valueOf(C0013R$drawable.ic_cc_qs_airplane_on));
            sQSIconMapping.put(C0013R$drawable.ic_signal_airplane_disable, Integer.valueOf(C0013R$drawable.ic_cc_qs_airplane_off));
            sQSIconMapping.put(C0013R$drawable.ic_qs_brightness_auto, Integer.valueOf(C0013R$drawable.ic_cc_qs_auto_brightness_on));
            sQSIconMapping.put(C0013R$drawable.ic_qs_brightness_manual, Integer.valueOf(C0013R$drawable.ic_cc_qs_auto_brightness_off));
            sQSIconMapping.put(C0013R$drawable.ic_qs_bluetooth_on, Integer.valueOf(C0013R$drawable.ic_cc_qs_bluetooth_on));
            sQSIconMapping.put(C0013R$drawable.ic_qs_bluetooth_off, Integer.valueOf(C0013R$drawable.ic_cc_qs_bluetooth_off));
            sQSIconMapping.put(C0013R$drawable.ic_qs_data_disabled, Integer.valueOf(C0013R$drawable.ic_cc_qs_cellular_off));
            sQSIconMapping.put(C0013R$drawable.ic_qs_data_on, Integer.valueOf(C0013R$drawable.ic_cc_qs_cellular_on));
            sQSIconMapping.put(C0013R$drawable.ic_qs_data_off, Integer.valueOf(C0013R$drawable.ic_cc_qs_cellular_off));
            sQSIconMapping.put(C0013R$drawable.ic_qs_drive_enabled, Integer.valueOf(C0013R$drawable.ic_cc_qs_drive_mode_on));
            sQSIconMapping.put(C0013R$drawable.ic_qs_drive_disabled, Integer.valueOf(C0013R$drawable.ic_cc_qs_drive_mode_off));
            sQSIconMapping.put(C0013R$drawable.ic_qs_flashlight_unavailable, Integer.valueOf(C0013R$drawable.ic_cc_qs_flashlight_off));
            sQSIconMapping.put(C0013R$drawable.ic_qs_flashlight_enabled, Integer.valueOf(C0013R$drawable.ic_cc_qs_flashlight_on));
            sQSIconMapping.put(C0013R$drawable.ic_qs_flashlight_disabled, Integer.valueOf(C0013R$drawable.ic_cc_qs_flashlight_off));
            sQSIconMapping.put(C0013R$drawable.ic_qs_dual_location_enabled, Integer.valueOf(C0013R$drawable.ic_cc_qs_gps_on));
            sQSIconMapping.put(C0013R$drawable.ic_qs_dual_location_disabled, Integer.valueOf(C0013R$drawable.ic_cc_qs_gps_off));
            sQSIconMapping.put(C0013R$drawable.ic_qs_hotspot_enabled, Integer.valueOf(C0013R$drawable.ic_cc_qs_hotspot_on));
            sQSIconMapping.put(C0013R$drawable.ic_qs_hotspot_disabled, Integer.valueOf(C0013R$drawable.ic_cc_qs_hotspot_off));
            sQSIconMapping.put(C0013R$drawable.ic_signal_location_enable, Integer.valueOf(C0013R$drawable.ic_cc_qs_location_on));
            sQSIconMapping.put(C0013R$drawable.ic_signal_location_disable, Integer.valueOf(C0013R$drawable.ic_cc_qs_location_off));
            sQSIconMapping.put(C0013R$drawable.ic_qs_mute_on, Integer.valueOf(C0013R$drawable.ic_cc_qs_mute_on));
            sQSIconMapping.put(C0013R$drawable.ic_qs_mute_off, Integer.valueOf(C0013R$drawable.ic_cc_qs_mute_off));
            sQSIconMapping.put(C0013R$drawable.ic_qs_nfc_enabled, Integer.valueOf(C0013R$drawable.ic_cc_qs_nfc_on));
            sQSIconMapping.put(C0013R$drawable.ic_qs_nfc_disabled, Integer.valueOf(C0013R$drawable.ic_cc_qs_nfc_off));
            sQSIconMapping.put(C0013R$drawable.ic_qs_night_mode_on, Integer.valueOf(C0013R$drawable.ic_cc_qs_night_mode_on));
            sQSIconMapping.put(C0013R$drawable.ic_qs_night_mode_off, Integer.valueOf(C0013R$drawable.ic_cc_qs_night_mode_off));
            sQSIconMapping.put(C0013R$drawable.ic_qs_paper_mode_on, Integer.valueOf(C0013R$drawable.ic_cc_qs_paper_mode_on));
            sQSIconMapping.put(C0013R$drawable.ic_qs_paper_mode_off, Integer.valueOf(C0013R$drawable.ic_cc_qs_paper_mode_off));
            sQSIconMapping.put(C0013R$drawable.ic_qs_battery_saver_on, Integer.valueOf(C0013R$drawable.ic_cc_qs_power_save_on));
            sQSIconMapping.put(C0013R$drawable.ic_qs_battery_saver_off, Integer.valueOf(C0013R$drawable.ic_cc_qs_power_save_off));
            sQSIconMapping.put(C0013R$drawable.ic_qs_dnd_on, Integer.valueOf(C0013R$drawable.ic_cc_qs_quiet_on));
            sQSIconMapping.put(C0013R$drawable.ic_qs_dnd_off, Integer.valueOf(C0013R$drawable.ic_cc_qs_quiet_off));
            sQSIconMapping.put(C0013R$drawable.ic_qs_auto_rotate_enabled, Integer.valueOf(C0013R$drawable.ic_cc_qs_rotation_lock_on));
            sQSIconMapping.put(C0013R$drawable.ic_qs_auto_rotate_disabled, Integer.valueOf(C0013R$drawable.ic_cc_qs_rotation_lock_off));
            sQSIconMapping.put(C0013R$drawable.ic_qs_screenlock, Integer.valueOf(C0013R$drawable.ic_cc_qs_screen_lock_off));
            sQSIconMapping.put(C0013R$drawable.ic_qs_screenshot, Integer.valueOf(C0013R$drawable.ic_cc_qs_screen_shot_off));
            sQSIconMapping.put(C0013R$drawable.ic_qs_sync_on, Integer.valueOf(C0013R$drawable.ic_cc_qs_sync_on));
            sQSIconMapping.put(C0013R$drawable.ic_qs_sync_off, Integer.valueOf(C0013R$drawable.ic_cc_qs_sync_off));
            sQSIconMapping.put(C0013R$drawable.ic_qs_vibrate_on, Integer.valueOf(C0013R$drawable.ic_cc_qs_vibrate_on));
            sQSIconMapping.put(C0013R$drawable.ic_qs_vibrate_off, Integer.valueOf(C0013R$drawable.ic_cc_qs_vibrate_off));
            sQSIconMapping.put(C0013R$drawable.ic_qs_wifi_on, Integer.valueOf(C0013R$drawable.ic_cc_qs_wifi_on));
            sQSIconMapping.put(C0013R$drawable.ic_qs_wifi_off, Integer.valueOf(C0013R$drawable.ic_cc_qs_wifi_off));
        }
        Integer num2 = sQSIconMapping.get(num.intValue());
        if (num2 != null) {
            num = num2;
        }
        return num.intValue();
    }
}
