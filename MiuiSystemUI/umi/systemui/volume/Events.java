package com.android.systemui.volume;

import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.UiEventLogger;
import com.android.internal.logging.UiEventLoggerImpl;
import com.android.systemui.plugins.VolumeDialogController;

public class Events {
    public static final String[] DISMISS_REASONS = {"unknown", "touch_outside", "volume_controller", "timeout", "screen_off", "settings_clicked", "done_clicked", "a11y_stream_changed", "output_chooser", "usb_temperature_below_threshold"};
    private static final String[] EVENT_TAGS = {"show_dialog", "dismiss_dialog", "active_stream_changed", "expand", "key", "collection_started", "collection_stopped", "icon_click", "settings_click", "touch_level_changed", "level_changed", "internal_ringer_mode_changed", "external_ringer_mode_changed", "zen_mode_changed", "suppressor_changed", "mute_changed", "touch_level_done", "zen_mode_config_changed", "ringer_toggle", "show_usb_overheat_alarm", "dismiss_usb_overheat_alarm", "odi_captions_click", "odi_captions_tooltip_click"};
    public static final String[] SHOW_REASONS = {"unknown", "volume_changed", "remote_volume_changed", "usb_temperature_above_threshold"};
    private static final String TAG = Util.logTag(Events.class);
    public static Callback sCallback;
    @VisibleForTesting
    static MetricsLogger sLegacyLogger = new MetricsLogger();
    @VisibleForTesting
    static UiEventLogger sUiEventLogger = new UiEventLoggerImpl();

    public interface Callback {
        void writeEvent(long j, int i, Object[] objArr);

        void writeState(long j, VolumeDialogController.State state);
    }

    private static String ringerModeToString(int i) {
        return i != 0 ? i != 1 ? i != 2 ? "unknown" : "normal" : "vibrate" : "silent";
    }

    private static String zenModeToString(int i) {
        return i != 0 ? i != 1 ? i != 2 ? i != 3 ? "unknown" : "alarms" : "no_interruptions" : "important_interruptions" : "off";
    }

    @VisibleForTesting
    public enum VolumeDialogOpenEvent implements UiEventLogger.UiEventEnum {
        INVALID(0),
        VOLUME_DIALOG_SHOW_VOLUME_CHANGED(128),
        VOLUME_DIALOG_SHOW_REMOTE_VOLUME_CHANGED(129),
        VOLUME_DIALOG_SHOW_USB_TEMP_ALARM_CHANGED(130);
        
        private final int mId;

        private VolumeDialogOpenEvent(int i) {
            this.mId = i;
        }

        public int getId() {
            return this.mId;
        }

        static VolumeDialogOpenEvent fromReasons(int i) {
            if (i == 1) {
                return VOLUME_DIALOG_SHOW_VOLUME_CHANGED;
            }
            if (i == 2) {
                return VOLUME_DIALOG_SHOW_REMOTE_VOLUME_CHANGED;
            }
            if (i != 3) {
                return INVALID;
            }
            return VOLUME_DIALOG_SHOW_USB_TEMP_ALARM_CHANGED;
        }
    }

    @VisibleForTesting
    public enum VolumeDialogCloseEvent implements UiEventLogger.UiEventEnum {
        INVALID(0),
        VOLUME_DIALOG_DISMISS_TOUCH_OUTSIDE(134),
        VOLUME_DIALOG_DISMISS_SYSTEM(135),
        VOLUME_DIALOG_DISMISS_TIMEOUT(136),
        VOLUME_DIALOG_DISMISS_SCREEN_OFF(137),
        VOLUME_DIALOG_DISMISS_SETTINGS(138),
        VOLUME_DIALOG_DISMISS_STREAM_GONE(140),
        VOLUME_DIALOG_DISMISS_USB_TEMP_ALARM_CHANGED(142);
        
        private final int mId;

        private VolumeDialogCloseEvent(int i) {
            this.mId = i;
        }

        public int getId() {
            return this.mId;
        }

        static VolumeDialogCloseEvent fromReason(int i) {
            if (i == 1) {
                return VOLUME_DIALOG_DISMISS_TOUCH_OUTSIDE;
            }
            if (i == 2) {
                return VOLUME_DIALOG_DISMISS_SYSTEM;
            }
            if (i == 3) {
                return VOLUME_DIALOG_DISMISS_TIMEOUT;
            }
            if (i == 4) {
                return VOLUME_DIALOG_DISMISS_SCREEN_OFF;
            }
            if (i == 5) {
                return VOLUME_DIALOG_DISMISS_SETTINGS;
            }
            if (i == 7) {
                return VOLUME_DIALOG_DISMISS_STREAM_GONE;
            }
            if (i != 9) {
                return INVALID;
            }
            return VOLUME_DIALOG_DISMISS_USB_TEMP_ALARM_CHANGED;
        }
    }

    @VisibleForTesting
    public enum VolumeDialogEvent implements UiEventLogger.UiEventEnum {
        INVALID(0),
        VOLUME_DIALOG_SETTINGS_CLICK(143),
        VOLUME_DIALOG_EXPAND_DETAILS(144),
        VOLUME_DIALOG_COLLAPSE_DETAILS(145),
        VOLUME_DIALOG_ACTIVE_STREAM_CHANGED(146),
        VOLUME_DIALOG_MUTE_STREAM(147),
        VOLUME_DIALOG_UNMUTE_STREAM(148),
        VOLUME_DIALOG_TO_VIBRATE_STREAM(149),
        VOLUME_DIALOG_SLIDER(150),
        VOLUME_DIALOG_SLIDER_TO_ZERO(151),
        VOLUME_KEY_TO_ZERO(152),
        VOLUME_KEY(153),
        RINGER_MODE_SILENT(154),
        RINGER_MODE_VIBRATE(155),
        RINGER_MODE_NORMAL(334),
        USB_OVERHEAT_ALARM(160),
        USB_OVERHEAT_ALARM_DISMISSED(161);
        
        private final int mId;

        private VolumeDialogEvent(int i) {
            this.mId = i;
        }

        public int getId() {
            return this.mId;
        }

        static VolumeDialogEvent fromIconState(int i) {
            if (i == 1) {
                return VOLUME_DIALOG_UNMUTE_STREAM;
            }
            if (i == 2) {
                return VOLUME_DIALOG_MUTE_STREAM;
            }
            if (i != 3) {
                return INVALID;
            }
            return VOLUME_DIALOG_TO_VIBRATE_STREAM;
        }

        static VolumeDialogEvent fromSliderLevel(int i) {
            return i == 0 ? VOLUME_DIALOG_SLIDER_TO_ZERO : VOLUME_DIALOG_SLIDER;
        }

        static VolumeDialogEvent fromKeyLevel(int i) {
            return i == 0 ? VOLUME_KEY_TO_ZERO : VOLUME_KEY;
        }

        static VolumeDialogEvent fromRingerMode(int i) {
            if (i == 0) {
                return RINGER_MODE_SILENT;
            }
            if (i == 1) {
                return RINGER_MODE_VIBRATE;
            }
            if (i != 2) {
                return INVALID;
            }
            return RINGER_MODE_NORMAL;
        }
    }

    @VisibleForTesting
    public enum ZenModeEvent implements UiEventLogger.UiEventEnum {
        INVALID(0),
        ZEN_MODE_OFF(335),
        ZEN_MODE_IMPORTANT_ONLY(157),
        ZEN_MODE_ALARMS_ONLY(158),
        ZEN_MODE_NO_INTERRUPTIONS(159);
        
        private final int mId;

        private ZenModeEvent(int i) {
            this.mId = i;
        }

        public int getId() {
            return this.mId;
        }

        static ZenModeEvent fromZenMode(int i) {
            if (i == 0) {
                return ZEN_MODE_OFF;
            }
            if (i == 1) {
                return ZEN_MODE_IMPORTANT_ONLY;
            }
            if (i == 2) {
                return ZEN_MODE_NO_INTERRUPTIONS;
            }
            if (i != 3) {
                return INVALID;
            }
            return ZEN_MODE_ALARMS_ONLY;
        }
    }

    public static void writeEvent(int i, Object... objArr) {
        long currentTimeMillis = System.currentTimeMillis();
        Log.i(TAG, logEvent(i, objArr));
        Callback callback = sCallback;
        if (callback != null) {
            callback.writeEvent(currentTimeMillis, i, objArr);
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x013e  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static java.lang.String logEvent(int r7, java.lang.Object... r8) {
        /*
        // Method dump skipped, instructions count: 694
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.volume.Events.logEvent(int, java.lang.Object[]):java.lang.String");
    }

    public static void writeState(long j, VolumeDialogController.State state) {
        Callback callback = sCallback;
        if (callback != null) {
            callback.writeState(j, state);
        }
    }

    private static String iconStateToString(int i) {
        if (i == 1) {
            return "unmute";
        }
        if (i == 2) {
            return "mute";
        }
        if (i == 3) {
            return "vibrate";
        }
        return "unknown_state_" + i;
    }
}
