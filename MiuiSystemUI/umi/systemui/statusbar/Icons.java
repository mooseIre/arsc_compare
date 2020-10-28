package com.android.systemui.statusbar;

import android.util.SparseArray;
import com.android.systemui.plugins.R;

public class Icons {
    private static SparseArray<Integer> sMapping;
    private static SparseArray<Integer> sQSIconMapping;
    private static SparseArray<Integer> sSignalHalfMapping;

    public static int get(Integer num, boolean z) {
        if (sMapping == null) {
            SparseArray<Integer> sparseArray = new SparseArray<>();
            sMapping = sparseArray;
            sparseArray.put(R.drawable.stat_sys_alarm, Integer.valueOf(R.drawable.stat_sys_alarm_darkmode));
            sMapping.put(R.drawable.stat_sys_data_bluetooth, Integer.valueOf(R.drawable.stat_sys_data_bluetooth_darkmode));
            sMapping.put(R.drawable.stat_sys_data_bluetooth_connected, Integer.valueOf(R.drawable.stat_sys_data_bluetooth_connected_darkmode));
            sMapping.put(R.drawable.stat_sys_data_bluetooth_in, Integer.valueOf(R.drawable.stat_sys_data_bluetooth_in_darkmode));
            sMapping.put(R.drawable.stat_sys_data_bluetooth_inout, Integer.valueOf(R.drawable.stat_sys_data_bluetooth_inout_darkmode));
            sMapping.put(R.drawable.stat_sys_data_bluetooth_out, Integer.valueOf(R.drawable.stat_sys_data_bluetooth_out_darkmode));
            sMapping.put(R.drawable.stat_sys_data_connected_roam, Integer.valueOf(R.drawable.stat_sys_data_connected_roam_darkmode));
            sMapping.put(R.drawable.stat_sys_speech_hd, Integer.valueOf(R.drawable.stat_sys_speech_hd_darkmode));
            sMapping.put(R.drawable.stat_sys_vowifi, Integer.valueOf(R.drawable.stat_sys_vowifi_darkmode));
            sMapping.put(R.drawable.stat_sys_gps_acquiring_anim, Integer.valueOf(R.drawable.stat_sys_gps_acquiring_anim_darkmode));
            sMapping.put(R.drawable.stat_sys_dual_gps_acquiring_anim, Integer.valueOf(R.drawable.stat_sys_dual_gps_acquiring_anim_darkmode));
            sMapping.put(R.drawable.stat_sys_gps_on, Integer.valueOf(R.drawable.stat_sys_gps_on_darkmode));
            sMapping.put(R.drawable.stat_sys_gps_acquiring, Integer.valueOf(R.drawable.stat_sys_gps_acquiring_darkmode));
            sMapping.put(R.drawable.stat_sys_dual_gps_on, Integer.valueOf(R.drawable.stat_sys_dual_gps_on_darkmode));
            sMapping.put(R.drawable.stat_sys_dual_gps_acquiring, Integer.valueOf(R.drawable.stat_sys_dual_gps_acquiring_darkmode));
            sMapping.put(R.drawable.stat_sys_headset, Integer.valueOf(R.drawable.stat_sys_headset_darkmode));
            sMapping.put(R.drawable.stat_sys_headset_without_mic, Integer.valueOf(R.drawable.stat_sys_headset_without_mic_darkmode));
            sMapping.put(R.drawable.stat_sys_micphone, Integer.valueOf(R.drawable.stat_sys_micphone_dark));
            sMapping.put(R.drawable.stat_sys_no_sim, Integer.valueOf(R.drawable.stat_sys_no_sim_darkmode));
            sMapping.put(R.drawable.stat_sys_ringer_silent, Integer.valueOf(R.drawable.stat_sys_ringer_silent_darkmode));
            sMapping.put(R.drawable.stat_sys_ringer_vibrate, Integer.valueOf(R.drawable.stat_sys_ringer_vibrate_darkmode));
            sMapping.put(R.drawable.stat_sys_signal_null, Integer.valueOf(R.drawable.stat_sys_signal_null_darkmode));
            sMapping.put(R.drawable.stat_sys_signal_0, Integer.valueOf(R.drawable.stat_sys_signal_0_darkmode));
            sMapping.put(R.drawable.stat_sys_signal_1, Integer.valueOf(R.drawable.stat_sys_signal_1_darkmode));
            sMapping.put(R.drawable.stat_sys_signal_2, Integer.valueOf(R.drawable.stat_sys_signal_2_darkmode));
            sMapping.put(R.drawable.stat_sys_signal_3, Integer.valueOf(R.drawable.stat_sys_signal_3_darkmode));
            sMapping.put(R.drawable.stat_sys_signal_4, Integer.valueOf(R.drawable.stat_sys_signal_4_darkmode));
            sMapping.put(R.drawable.stat_sys_signal_5, Integer.valueOf(R.drawable.stat_sys_signal_5_darkmode));
            sMapping.put(R.drawable.stat_sys_signal_flightmode, Integer.valueOf(R.drawable.stat_sys_signal_flightmode_darkmode));
            sMapping.put(R.drawable.stat_sys_signal_in_left, Integer.valueOf(R.drawable.stat_sys_signal_in_left_darkmode));
            sMapping.put(R.drawable.stat_sys_signal_inout_left, Integer.valueOf(R.drawable.stat_sys_signal_inout_left_darkmode));
            sMapping.put(R.drawable.stat_sys_signal_out_left, Integer.valueOf(R.drawable.stat_sys_signal_out_left_darkmode));
            sMapping.put(R.drawable.stat_sys_signal_data_left, Integer.valueOf(R.drawable.stat_sys_signal_data_left_darkmode));
            sMapping.put(R.drawable.stat_sys_sync_active, Integer.valueOf(R.drawable.stat_sys_sync_active_darkmode));
            sMapping.put(R.drawable.stat_sys_sync_error, Integer.valueOf(R.drawable.stat_sys_sync_error_darkmode));
            sMapping.put(R.drawable.stat_sys_bluetooth_handsfree_battery, Integer.valueOf(R.drawable.stat_sys_bluetooth_handsfree_battery_darkmode));
            sMapping.put(R.drawable.stat_sys_wifi_signal_null, Integer.valueOf(R.drawable.stat_sys_wifi_signal_null_darkmode));
            sMapping.put(R.drawable.stat_sys_wifi_signal_0, Integer.valueOf(R.drawable.stat_sys_wifi_signal_0_darkmode));
            sMapping.put(R.drawable.stat_sys_wifi_signal_1, Integer.valueOf(R.drawable.stat_sys_wifi_signal_1_darkmode));
            sMapping.put(R.drawable.stat_sys_wifi_signal_2, Integer.valueOf(R.drawable.stat_sys_wifi_signal_2_darkmode));
            sMapping.put(R.drawable.stat_sys_wifi_signal_3, Integer.valueOf(R.drawable.stat_sys_wifi_signal_3_darkmode));
            sMapping.put(R.drawable.stat_sys_wifi_signal_4, Integer.valueOf(R.drawable.stat_sys_wifi_signal_4_darkmode));
            sMapping.put(R.drawable.stat_sys_wifi_ap, Integer.valueOf(R.drawable.stat_sys_wifi_ap_darkmode));
            sMapping.put(R.drawable.stat_sys_wifi_inout, Integer.valueOf(R.drawable.stat_sys_wifi_inout_darkmode));
            sMapping.put(R.drawable.stat_sys_wifi_in, Integer.valueOf(R.drawable.stat_sys_wifi_in_darkmode));
            sMapping.put(R.drawable.stat_sys_wifi_out, Integer.valueOf(R.drawable.stat_sys_wifi_out_darkmode));
            sMapping.put(R.drawable.stat_sys_wifi_ap_on, Integer.valueOf(R.drawable.stat_sys_wifi_ap_on_darkmode));
            sMapping.put(R.drawable.stat_sys_ethernet, Integer.valueOf(R.drawable.stat_sys_ethernet_darkmode));
            sMapping.put(R.drawable.stat_sys_ethernet_fully, Integer.valueOf(R.drawable.stat_sys_ethernet_fully_darkmode));
            sMapping.put(R.drawable.stat_sys_warning, Integer.valueOf(R.drawable.stat_sys_warning_darkmode));
            sMapping.put(R.drawable.stat_sys_vpn, Integer.valueOf(R.drawable.stat_sys_vpn_darkmode));
            sMapping.put(R.drawable.stat_sys_data_connected_roam_small, Integer.valueOf(R.drawable.stat_sys_data_connected_roam_small_darkmode));
            sMapping.put(R.drawable.stat_sys_speakerphone, Integer.valueOf(R.drawable.stat_sys_speakerphone_darkmode));
            sMapping.put(R.drawable.stat_sys_call_record, Integer.valueOf(R.drawable.stat_sys_call_record_darkmode));
            sMapping.put(R.drawable.stat_sys_roaming_cdma_0, Integer.valueOf(R.drawable.stat_sys_roaming_cdma_0_darkmode));
            sMapping.put(R.drawable.stat_sys_signal_0_half, Integer.valueOf(R.drawable.stat_sys_signal_0_half_darkmode));
            sMapping.put(R.drawable.stat_sys_signal_1_half, Integer.valueOf(R.drawable.stat_sys_signal_1_half_darkmode));
            sMapping.put(R.drawable.stat_sys_signal_2_half, Integer.valueOf(R.drawable.stat_sys_signal_2_half_darkmode));
            sMapping.put(R.drawable.stat_sys_signal_3_half, Integer.valueOf(R.drawable.stat_sys_signal_3_half_darkmode));
            sMapping.put(R.drawable.stat_sys_signal_4_half, Integer.valueOf(R.drawable.stat_sys_signal_4_half_darkmode));
            sMapping.put(R.drawable.stat_sys_signal_5_half, Integer.valueOf(R.drawable.stat_sys_signal_5_half_darkmode));
            sMapping.put(R.drawable.stat_sys_signal_dual_in, Integer.valueOf(R.drawable.stat_sys_signal_dual_in_darkmode));
            sMapping.put(R.drawable.stat_sys_signal_dual_inout, Integer.valueOf(R.drawable.stat_sys_signal_dual_inout_darkmode));
            sMapping.put(R.drawable.stat_sys_signal_dual_out, Integer.valueOf(R.drawable.stat_sys_signal_dual_out_darkmode));
            sMapping.put(R.drawable.stat_sys_signal_dual_data, Integer.valueOf(R.drawable.stat_sys_signal_dual_data_darkmode));
            sMapping.put(R.drawable.stat_notify_more, Integer.valueOf(R.drawable.stat_notify_more_darkmode));
            sMapping.put(R.drawable.stat_sys_speakerphone, Integer.valueOf(R.drawable.stat_sys_speakerphone_darkmode));
            sMapping.put(R.drawable.stat_notify_call_mute, Integer.valueOf(R.drawable.stat_notify_call_mute_darkmode));
            sMapping.put(R.drawable.stat_sys_quiet_mode, Integer.valueOf(R.drawable.stat_sys_quiet_mode_darkmode));
            sMapping.put(R.drawable.stat_sys_usb_share, Integer.valueOf(R.drawable.stat_sys_usb_share_darkmode));
            sMapping.put(R.drawable.stat_sys_missed_call, Integer.valueOf(R.drawable.stat_sys_missed_call_darkmode));
            sMapping.put(R.drawable.stat_sys_battery_charging, Integer.valueOf(R.drawable.stat_sys_battery_charging_darkmode));
            sMapping.put(R.drawable.stat_sys_quick_charging, Integer.valueOf(R.drawable.stat_sys_quick_charging_darkmode));
            sMapping.put(R.drawable.stat_sys_signal_4g_lte, Integer.valueOf(R.drawable.stat_sys_signal_4g_lte_darkmode));
            sMapping.put(R.drawable.signal_5g_off, Integer.valueOf(R.drawable.signal_5g_off_darkmode));
            sMapping.put(R.drawable.signal_5g_on, Integer.valueOf(R.drawable.signal_5g_on_darkmode));
            sMapping.put(R.drawable.battery_meter_charging, Integer.valueOf(R.drawable.battery_meter_charging_dark));
            sMapping.put(R.drawable.battery_meter_quick_charging, Integer.valueOf(R.drawable.battery_meter_quick_charging_dark));
            sMapping.put(R.drawable.stat_sys_sos, Integer.valueOf(R.drawable.stat_sys_sos_darkmode));
            sMapping.put(R.drawable.stat_sys_managed_profile_status, Integer.valueOf(R.drawable.stat_sys_managed_profile_status_darkmode));
            sMapping.put(R.drawable.stat_sys_managed_profile_status_off, Integer.valueOf(R.drawable.stat_sys_managed_profile_status_off_darkmode));
            sMapping.put(R.drawable.stat_sys_managed_profile_xspace_user, Integer.valueOf(R.drawable.stat_sys_managed_profile_xspace_user_darkmode));
            sMapping.put(R.drawable.stat_sys_managed_profile_not_owner_user, Integer.valueOf(R.drawable.stat_sys_managed_profile_not_owner_user_darkmode));
            sMapping.put(R.drawable.sim1, Integer.valueOf(R.drawable.sim1_darkmode));
            sMapping.put(R.drawable.sim2, Integer.valueOf(R.drawable.sim2_darkmode));
            sMapping.put(R.drawable.stat_sys_signal_upgrade, Integer.valueOf(R.drawable.stat_sys_signal_upgrade_darkmode));
            sMapping.put(R.drawable.stat_sys_signal_volte, Integer.valueOf(R.drawable.stat_sys_signal_volte_darkmode));
            sMapping.put(R.drawable.stat_sys_volte_no_service, Integer.valueOf(R.drawable.stat_sys_volte_no_service_darkmode));
            sMapping.put(R.drawable.stat_sys_signal_hd_notch, Integer.valueOf(R.drawable.stat_sys_signal_hd_notch_darkmode));
            sMapping.put(R.drawable.stat_sys_signal_hd_big, Integer.valueOf(R.drawable.stat_sys_signal_hd_big_darkmode));
            sMapping.put(R.drawable.stat_sys_signal_null_half, Integer.valueOf(R.drawable.stat_sys_signal_null_half_darkmode));
            sMapping.put(R.drawable.stat_sys_battery_charging, Integer.valueOf(R.drawable.stat_sys_battery_charging_darkmode));
            sMapping.put(R.drawable.stat_sys_vowifi_call, Integer.valueOf(R.drawable.stat_sys_vowifi_call_darkmode));
            sMapping.put(R.drawable.stat_sys_signal_volte_4g, Integer.valueOf(R.drawable.stat_sys_signal_volte_4g_darkmode));
            sMapping.put(R.drawable.stat_sys_vowifi_wifi, Integer.valueOf(R.drawable.stat_sys_vowifi_wifi_darkmode));
            sMapping.put(R.drawable.stat_sys_signal_volte_no_frame, Integer.valueOf(R.drawable.stat_sys_signal_volte_no_frame_darkmode));
            sMapping.put(R.drawable.stat_sys_signal_volte_hd_voice, Integer.valueOf(R.drawable.stat_sys_signal_volte_hd_voice_darkmode));
            sMapping.put(R.drawable.ble_unlock_statusbar_icon_unverified, Integer.valueOf(R.drawable.ble_unlock_statusbar_icon_unverified_dark));
            sMapping.put(R.drawable.ble_unlock_statusbar_icon_verified_near, Integer.valueOf(R.drawable.ble_unlock_statusbar_icon_verified_near_dark));
            sMapping.put(R.drawable.ble_unlock_statusbar_icon_verified_far, Integer.valueOf(R.drawable.ble_unlock_statusbar_icon_verified_far_dark));
            sMapping.put(R.drawable.stat_sys_slave_wifi_signal_0, Integer.valueOf(R.drawable.stat_sys_slave_wifi_signal_0_darkmode));
            sMapping.put(R.drawable.stat_sys_slave_wifi_signal_1, Integer.valueOf(R.drawable.stat_sys_slave_wifi_signal_1_darkmode));
            sMapping.put(R.drawable.stat_sys_slave_wifi_signal_2, Integer.valueOf(R.drawable.stat_sys_slave_wifi_signal_2_darkmode));
            sMapping.put(R.drawable.stat_sys_slave_wifi_signal_3, Integer.valueOf(R.drawable.stat_sys_slave_wifi_signal_3_darkmode));
            sMapping.put(R.drawable.stat_sys_slave_wifi_signal_4, Integer.valueOf(R.drawable.stat_sys_slave_wifi_signal_4_darkmode));
        }
        Integer num2 = z ? sMapping.get(num.intValue()) : num;
        if (num2 != null) {
            num = num2;
        }
        return num.intValue();
    }

    public static int getSignalHalfId(Integer num) {
        if (sSignalHalfMapping == null) {
            SparseArray<Integer> sparseArray = new SparseArray<>();
            sSignalHalfMapping = sparseArray;
            sparseArray.put(R.drawable.stat_sys_signal_0, Integer.valueOf(R.drawable.stat_sys_signal_0_half));
            sSignalHalfMapping.put(R.drawable.stat_sys_signal_1, Integer.valueOf(R.drawable.stat_sys_signal_1_half));
            sSignalHalfMapping.put(R.drawable.stat_sys_signal_2, Integer.valueOf(R.drawable.stat_sys_signal_2_half));
            sSignalHalfMapping.put(R.drawable.stat_sys_signal_3, Integer.valueOf(R.drawable.stat_sys_signal_3_half));
            sSignalHalfMapping.put(R.drawable.stat_sys_signal_4, Integer.valueOf(R.drawable.stat_sys_signal_4_half));
            sSignalHalfMapping.put(R.drawable.stat_sys_signal_5, Integer.valueOf(R.drawable.stat_sys_signal_5_half));
            sSignalHalfMapping.put(R.drawable.stat_sys_signal_null, Integer.valueOf(R.drawable.stat_sys_signal_null_half));
        }
        Integer num2 = sSignalHalfMapping.get(num.intValue());
        if (num2 != null) {
            num = num2;
        }
        return num.intValue();
    }

    public static int getQSIcons(Integer num, boolean z) {
        if (!z) {
            return num.intValue();
        }
        if (sQSIconMapping == null) {
            SparseArray<Integer> sparseArray = new SparseArray<>();
            sQSIconMapping = sparseArray;
            sparseArray.put(R.drawable.ic_signal_airplane_enable, Integer.valueOf(R.drawable.ic_cc_qs_airplane_on));
            sQSIconMapping.put(R.drawable.ic_signal_airplane_disable, Integer.valueOf(R.drawable.ic_cc_qs_airplane_off));
            sQSIconMapping.put(R.drawable.ic_qs_brightness_auto, Integer.valueOf(R.drawable.ic_cc_qs_auto_brightness_on));
            sQSIconMapping.put(R.drawable.ic_qs_brightness_manual, Integer.valueOf(R.drawable.ic_cc_qs_auto_brightness_off));
            sQSIconMapping.put(R.drawable.ic_qs_bluetooth_on, Integer.valueOf(R.drawable.ic_cc_qs_bluetooth_on));
            sQSIconMapping.put(R.drawable.ic_qs_bluetooth_off, Integer.valueOf(R.drawable.ic_cc_qs_bluetooth_off));
            sQSIconMapping.put(R.drawable.ic_qs_data_disabled, Integer.valueOf(R.drawable.ic_cc_qs_cellular_off));
            sQSIconMapping.put(R.drawable.ic_qs_data_on, Integer.valueOf(R.drawable.ic_cc_qs_cellular_on));
            sQSIconMapping.put(R.drawable.ic_qs_data_off, Integer.valueOf(R.drawable.ic_cc_qs_cellular_off));
            sQSIconMapping.put(R.drawable.ic_qs_drive_enabled, Integer.valueOf(R.drawable.ic_cc_qs_drive_mode_on));
            sQSIconMapping.put(R.drawable.ic_qs_drive_disabled, Integer.valueOf(R.drawable.ic_cc_qs_drive_mode_off));
            sQSIconMapping.put(R.drawable.ic_qs_drive_enabled, Integer.valueOf(R.drawable.ic_cc_qs_drive_mode_on));
            sQSIconMapping.put(R.drawable.ic_qs_drive_disabled, Integer.valueOf(R.drawable.ic_cc_qs_drive_mode_off));
            sQSIconMapping.put(R.drawable.ic_qs_flashlight_unavailable, Integer.valueOf(R.drawable.ic_cc_qs_flashlight_off));
            sQSIconMapping.put(R.drawable.ic_qs_flashlight_enabled, Integer.valueOf(R.drawable.ic_cc_qs_flashlight_on));
            sQSIconMapping.put(R.drawable.ic_qs_flashlight_disabled, Integer.valueOf(R.drawable.ic_cc_qs_flashlight_off));
            sQSIconMapping.put(R.drawable.ic_qs_dual_location_enabled, Integer.valueOf(R.drawable.ic_cc_qs_gps_on));
            sQSIconMapping.put(R.drawable.ic_qs_dual_location_disabled, Integer.valueOf(R.drawable.ic_cc_qs_gps_off));
            sQSIconMapping.put(R.drawable.ic_signal_location_enable, Integer.valueOf(R.drawable.ic_cc_qs_gps_on));
            sQSIconMapping.put(R.drawable.ic_signal_location_disable, Integer.valueOf(R.drawable.ic_cc_qs_gps_off));
            sQSIconMapping.put(R.drawable.ic_qs_hotspot_enabled, Integer.valueOf(R.drawable.ic_cc_qs_hotspot_on));
            sQSIconMapping.put(R.drawable.ic_qs_hotspot_disabled, Integer.valueOf(R.drawable.ic_cc_qs_hotspot_off));
            sQSIconMapping.put(R.drawable.ic_signal_location_enable, Integer.valueOf(R.drawable.ic_cc_qs_location_on));
            sQSIconMapping.put(R.drawable.ic_signal_location_disable, Integer.valueOf(R.drawable.ic_cc_qs_location_off));
            sQSIconMapping.put(R.drawable.ic_qs_mute_on, Integer.valueOf(R.drawable.ic_cc_qs_mute_on));
            sQSIconMapping.put(R.drawable.ic_qs_mute_off, Integer.valueOf(R.drawable.ic_cc_qs_mute_off));
            sQSIconMapping.put(R.drawable.ic_qs_nfc_enabled, Integer.valueOf(R.drawable.ic_cc_qs_nfc_on));
            sQSIconMapping.put(R.drawable.ic_qs_nfc_disabled, Integer.valueOf(R.drawable.ic_cc_qs_nfc_off));
            sQSIconMapping.put(R.drawable.ic_qs_night_mode_on, Integer.valueOf(R.drawable.ic_cc_qs_night_mode_on));
            sQSIconMapping.put(R.drawable.ic_qs_night_mode_off, Integer.valueOf(R.drawable.ic_cc_qs_night_mode_off));
            sQSIconMapping.put(R.drawable.ic_qs_paper_mode_on, Integer.valueOf(R.drawable.ic_cc_qs_paper_mode_on));
            sQSIconMapping.put(R.drawable.ic_qs_paper_mode_off, Integer.valueOf(R.drawable.ic_cc_qs_paper_mode_off));
            sQSIconMapping.put(R.drawable.ic_qs_battery_saver_on, Integer.valueOf(R.drawable.ic_cc_qs_power_save_on));
            sQSIconMapping.put(R.drawable.ic_qs_battery_saver_off, Integer.valueOf(R.drawable.ic_cc_qs_power_save_off));
            sQSIconMapping.put(R.drawable.ic_qs_dnd_on, Integer.valueOf(R.drawable.ic_cc_qs_quiet_on));
            sQSIconMapping.put(R.drawable.ic_qs_dnd_off, Integer.valueOf(R.drawable.ic_cc_qs_quiet_off));
            sQSIconMapping.put(R.drawable.ic_qs_auto_rotate_enabled, Integer.valueOf(R.drawable.ic_cc_qs_rotation_lock_on));
            sQSIconMapping.put(R.drawable.ic_qs_auto_rotate_disabled, Integer.valueOf(R.drawable.ic_cc_qs_rotation_lock_off));
            sQSIconMapping.put(R.drawable.ic_qs_screenlock, Integer.valueOf(R.drawable.ic_cc_qs_screen_lock_off));
            sQSIconMapping.put(R.drawable.ic_qs_screenshot, Integer.valueOf(R.drawable.ic_cc_qs_screen_shot_off));
            sQSIconMapping.put(R.drawable.ic_qs_sync_on, Integer.valueOf(R.drawable.ic_cc_qs_sync_on));
            sQSIconMapping.put(R.drawable.ic_qs_sync_off, Integer.valueOf(R.drawable.ic_cc_qs_sync_off));
            sQSIconMapping.put(R.drawable.ic_qs_vibrate_on, Integer.valueOf(R.drawable.ic_cc_qs_vibrate_on));
            sQSIconMapping.put(R.drawable.ic_qs_vibrate_off, Integer.valueOf(R.drawable.ic_cc_qs_vibrate_off));
            sQSIconMapping.put(R.drawable.ic_qs_wifi_on, Integer.valueOf(R.drawable.ic_cc_qs_wifi_on));
            sQSIconMapping.put(R.drawable.ic_qs_wifi_off, Integer.valueOf(R.drawable.ic_cc_qs_wifi_off));
        }
        Integer num2 = sQSIconMapping.get(num.intValue());
        if (num2 != null) {
            num = num2;
        }
        return num.intValue();
    }
}
