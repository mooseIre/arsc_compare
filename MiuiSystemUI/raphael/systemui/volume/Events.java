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

    /* JADX WARNING: Code restructure failed: missing block: B:25:0x012a, code lost:
        r0.append(ringerModeToString(r8[0].intValue()));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x013c, code lost:
        if (r8.length <= 1) goto L_0x026c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x013e, code lost:
        r0.append(android.media.AudioSystem.streamToString(r8[0].intValue()));
        r0.append(' ');
        r0.append(r8[1]);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x0270, code lost:
        return r0.toString();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static java.lang.String logEvent(int r7, java.lang.Object... r8) {
        /*
            java.lang.String[] r0 = EVENT_TAGS
            int r0 = r0.length
            if (r7 < r0) goto L_0x0008
            java.lang.String r7 = ""
            return r7
        L_0x0008:
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            java.lang.String r1 = "writeEvent "
            r0.<init>(r1)
            java.lang.String[] r1 = EVENT_TAGS
            r1 = r1[r7]
            r0.append(r1)
            if (r8 == 0) goto L_0x0271
            int r1 = r8.length
            if (r1 != 0) goto L_0x001d
            goto L_0x0271
        L_0x001d:
            java.lang.String r1 = " "
            r0.append(r1)
            r1 = 1457(0x5b1, float:2.042E-42)
            r2 = 207(0xcf, float:2.9E-43)
            java.lang.String r3 = " keyguard="
            r4 = 32
            r5 = 0
            r6 = 1
            switch(r7) {
                case 0: goto L_0x0233;
                case 1: goto L_0x0211;
                case 2: goto L_0x01ef;
                case 3: goto L_0x01cb;
                case 4: goto L_0x0195;
                case 5: goto L_0x002f;
                case 6: goto L_0x002f;
                case 7: goto L_0x0157;
                case 8: goto L_0x002f;
                case 9: goto L_0x013b;
                case 10: goto L_0x013b;
                case 11: goto L_0x012a;
                case 12: goto L_0x011b;
                case 13: goto L_0x00fd;
                case 14: goto L_0x00eb;
                case 15: goto L_0x013b;
                case 16: goto L_0x00cb;
                case 17: goto L_0x002f;
                case 18: goto L_0x00a2;
                case 19: goto L_0x006d;
                case 20: goto L_0x0038;
                default: goto L_0x002f;
            }
        L_0x002f:
            java.util.List r7 = java.util.Arrays.asList(r8)
            r0.append(r7)
            goto L_0x026c
        L_0x0038:
            com.android.internal.logging.MetricsLogger r7 = sLegacyLogger
            r7.hidden(r1)
            com.android.internal.logging.UiEventLogger r7 = sUiEventLogger
            com.android.systemui.volume.Events$VolumeDialogEvent r1 = com.android.systemui.volume.Events.VolumeDialogEvent.USB_OVERHEAT_ALARM_DISMISSED
            r7.log(r1)
            int r7 = r8.length
            if (r7 <= r6) goto L_0x026c
            r7 = r8[r6]
            java.lang.Boolean r7 = (java.lang.Boolean) r7
            com.android.internal.logging.MetricsLogger r1 = sLegacyLogger
            boolean r2 = r7.booleanValue()
            java.lang.String r4 = "dismiss_usb_overheat_alarm"
            r1.histogram(r4, r2)
            r8 = r8[r5]
            java.lang.Integer r8 = (java.lang.Integer) r8
            java.lang.String[] r1 = DISMISS_REASONS
            int r8 = r8.intValue()
            r8 = r1[r8]
            r0.append(r8)
            r0.append(r3)
            r0.append(r7)
            goto L_0x026c
        L_0x006d:
            com.android.internal.logging.MetricsLogger r7 = sLegacyLogger
            r7.visible(r1)
            com.android.internal.logging.UiEventLogger r7 = sUiEventLogger
            com.android.systemui.volume.Events$VolumeDialogEvent r1 = com.android.systemui.volume.Events.VolumeDialogEvent.USB_OVERHEAT_ALARM
            r7.log(r1)
            int r7 = r8.length
            if (r7 <= r6) goto L_0x026c
            r7 = r8[r6]
            java.lang.Boolean r7 = (java.lang.Boolean) r7
            com.android.internal.logging.MetricsLogger r1 = sLegacyLogger
            boolean r2 = r7.booleanValue()
            java.lang.String r4 = "show_usb_overheat_alarm"
            r1.histogram(r4, r2)
            r8 = r8[r5]
            java.lang.Integer r8 = (java.lang.Integer) r8
            java.lang.String[] r1 = SHOW_REASONS
            int r8 = r8.intValue()
            r8 = r1[r8]
            r0.append(r8)
            r0.append(r3)
            r0.append(r7)
            goto L_0x026c
        L_0x00a2:
            r7 = r8[r5]
            java.lang.Integer r7 = (java.lang.Integer) r7
            com.android.internal.logging.MetricsLogger r8 = sLegacyLogger
            r1 = 1385(0x569, float:1.941E-42)
            int r2 = r7.intValue()
            r8.action(r1, r2)
            com.android.internal.logging.UiEventLogger r8 = sUiEventLogger
            int r1 = r7.intValue()
            com.android.systemui.volume.Events$VolumeDialogEvent r1 = com.android.systemui.volume.Events.VolumeDialogEvent.fromRingerMode(r1)
            r8.log(r1)
            int r7 = r7.intValue()
            java.lang.String r7 = ringerModeToString(r7)
            r0.append(r7)
            goto L_0x026c
        L_0x00cb:
            int r7 = r8.length
            if (r7 <= r6) goto L_0x013b
            r7 = r8[r6]
            java.lang.Integer r7 = (java.lang.Integer) r7
            com.android.internal.logging.MetricsLogger r1 = sLegacyLogger
            r2 = 209(0xd1, float:2.93E-43)
            int r3 = r7.intValue()
            r1.action(r2, r3)
            com.android.internal.logging.UiEventLogger r1 = sUiEventLogger
            int r7 = r7.intValue()
            com.android.systemui.volume.Events$VolumeDialogEvent r7 = com.android.systemui.volume.Events.VolumeDialogEvent.fromSliderLevel(r7)
            r1.log(r7)
            goto L_0x013b
        L_0x00eb:
            int r7 = r8.length
            if (r7 <= r6) goto L_0x026c
            r7 = r8[r5]
            r0.append(r7)
            r0.append(r4)
            r7 = r8[r6]
            r0.append(r7)
            goto L_0x026c
        L_0x00fd:
            r7 = r8[r5]
            java.lang.Integer r7 = (java.lang.Integer) r7
            int r8 = r7.intValue()
            java.lang.String r8 = zenModeToString(r8)
            r0.append(r8)
            com.android.internal.logging.UiEventLogger r8 = sUiEventLogger
            int r7 = r7.intValue()
            com.android.systemui.volume.Events$ZenModeEvent r7 = com.android.systemui.volume.Events.ZenModeEvent.fromZenMode(r7)
            r8.log(r7)
            goto L_0x026c
        L_0x011b:
            r7 = r8[r5]
            java.lang.Integer r7 = (java.lang.Integer) r7
            com.android.internal.logging.MetricsLogger r1 = sLegacyLogger
            r2 = 213(0xd5, float:2.98E-43)
            int r7 = r7.intValue()
            r1.action(r2, r7)
        L_0x012a:
            r7 = r8[r5]
            java.lang.Integer r7 = (java.lang.Integer) r7
            int r7 = r7.intValue()
            java.lang.String r7 = ringerModeToString(r7)
            r0.append(r7)
            goto L_0x026c
        L_0x013b:
            int r7 = r8.length
            if (r7 <= r6) goto L_0x026c
            r7 = r8[r5]
            java.lang.Integer r7 = (java.lang.Integer) r7
            int r7 = r7.intValue()
            java.lang.String r7 = android.media.AudioSystem.streamToString(r7)
            r0.append(r7)
            r0.append(r4)
            r7 = r8[r6]
            r0.append(r7)
            goto L_0x026c
        L_0x0157:
            int r7 = r8.length
            if (r7 <= r6) goto L_0x026c
            r7 = r8[r5]
            java.lang.Integer r7 = (java.lang.Integer) r7
            com.android.internal.logging.MetricsLogger r1 = sLegacyLogger
            r2 = 212(0xd4, float:2.97E-43)
            int r3 = r7.intValue()
            r1.action(r2, r3)
            r8 = r8[r6]
            java.lang.Integer r8 = (java.lang.Integer) r8
            com.android.internal.logging.UiEventLogger r1 = sUiEventLogger
            int r2 = r8.intValue()
            com.android.systemui.volume.Events$VolumeDialogEvent r2 = com.android.systemui.volume.Events.VolumeDialogEvent.fromIconState(r2)
            r1.log(r2)
            int r7 = r7.intValue()
            java.lang.String r7 = android.media.AudioSystem.streamToString(r7)
            r0.append(r7)
            r0.append(r4)
            int r7 = r8.intValue()
            java.lang.String r7 = iconStateToString(r7)
            r0.append(r7)
            goto L_0x026c
        L_0x0195:
            int r7 = r8.length
            if (r7 <= r6) goto L_0x026c
            r7 = r8[r5]
            java.lang.Integer r7 = (java.lang.Integer) r7
            com.android.internal.logging.MetricsLogger r1 = sLegacyLogger
            r2 = 211(0xd3, float:2.96E-43)
            int r3 = r7.intValue()
            r1.action(r2, r3)
            r8 = r8[r6]
            java.lang.Integer r8 = (java.lang.Integer) r8
            com.android.internal.logging.UiEventLogger r1 = sUiEventLogger
            int r2 = r8.intValue()
            com.android.systemui.volume.Events$VolumeDialogEvent r2 = com.android.systemui.volume.Events.VolumeDialogEvent.fromKeyLevel(r2)
            r1.log(r2)
            int r7 = r7.intValue()
            java.lang.String r7 = android.media.AudioSystem.streamToString(r7)
            r0.append(r7)
            r0.append(r4)
            r0.append(r8)
            goto L_0x026c
        L_0x01cb:
            r7 = r8[r5]
            java.lang.Boolean r7 = (java.lang.Boolean) r7
            com.android.internal.logging.MetricsLogger r8 = sLegacyLogger
            r1 = 208(0xd0, float:2.91E-43)
            boolean r2 = r7.booleanValue()
            r8.visibility(r1, r2)
            com.android.internal.logging.UiEventLogger r8 = sUiEventLogger
            boolean r1 = r7.booleanValue()
            if (r1 == 0) goto L_0x01e5
            com.android.systemui.volume.Events$VolumeDialogEvent r1 = com.android.systemui.volume.Events.VolumeDialogEvent.VOLUME_DIALOG_EXPAND_DETAILS
            goto L_0x01e7
        L_0x01e5:
            com.android.systemui.volume.Events$VolumeDialogEvent r1 = com.android.systemui.volume.Events.VolumeDialogEvent.VOLUME_DIALOG_COLLAPSE_DETAILS
        L_0x01e7:
            r8.log(r1)
            r0.append(r7)
            goto L_0x026c
        L_0x01ef:
            r7 = r8[r5]
            java.lang.Integer r7 = (java.lang.Integer) r7
            com.android.internal.logging.MetricsLogger r8 = sLegacyLogger
            r1 = 210(0xd2, float:2.94E-43)
            int r2 = r7.intValue()
            r8.action(r1, r2)
            com.android.internal.logging.UiEventLogger r8 = sUiEventLogger
            com.android.systemui.volume.Events$VolumeDialogEvent r1 = com.android.systemui.volume.Events.VolumeDialogEvent.VOLUME_DIALOG_ACTIVE_STREAM_CHANGED
            r8.log(r1)
            int r7 = r7.intValue()
            java.lang.String r7 = android.media.AudioSystem.streamToString(r7)
            r0.append(r7)
            goto L_0x026c
        L_0x0211:
            com.android.internal.logging.MetricsLogger r7 = sLegacyLogger
            r7.hidden(r2)
            r7 = r8[r5]
            java.lang.Integer r7 = (java.lang.Integer) r7
            com.android.internal.logging.UiEventLogger r8 = sUiEventLogger
            int r1 = r7.intValue()
            com.android.systemui.volume.Events$VolumeDialogCloseEvent r1 = com.android.systemui.volume.Events.VolumeDialogCloseEvent.fromReason(r1)
            r8.log(r1)
            java.lang.String[] r8 = DISMISS_REASONS
            int r7 = r7.intValue()
            r7 = r8[r7]
            r0.append(r7)
            goto L_0x026c
        L_0x0233:
            com.android.internal.logging.MetricsLogger r7 = sLegacyLogger
            r7.visible(r2)
            int r7 = r8.length
            if (r7 <= r6) goto L_0x026c
            r7 = r8[r5]
            java.lang.Integer r7 = (java.lang.Integer) r7
            r8 = r8[r6]
            java.lang.Boolean r8 = (java.lang.Boolean) r8
            com.android.internal.logging.MetricsLogger r1 = sLegacyLogger
            boolean r2 = r8.booleanValue()
            java.lang.String r4 = "volume_from_keyguard"
            r1.histogram(r4, r2)
            com.android.internal.logging.UiEventLogger r1 = sUiEventLogger
            int r2 = r7.intValue()
            com.android.systemui.volume.Events$VolumeDialogOpenEvent r2 = com.android.systemui.volume.Events.VolumeDialogOpenEvent.fromReasons(r2)
            r1.log(r2)
            java.lang.String[] r1 = SHOW_REASONS
            int r7 = r7.intValue()
            r7 = r1[r7]
            r0.append(r7)
            r0.append(r3)
            r0.append(r8)
        L_0x026c:
            java.lang.String r7 = r0.toString()
            return r7
        L_0x0271:
            r8 = 8
            if (r7 != r8) goto L_0x0283
            com.android.internal.logging.MetricsLogger r7 = sLegacyLogger
            r8 = 1386(0x56a, float:1.942E-42)
            r7.action(r8)
            com.android.internal.logging.UiEventLogger r7 = sUiEventLogger
            com.android.systemui.volume.Events$VolumeDialogEvent r8 = com.android.systemui.volume.Events.VolumeDialogEvent.VOLUME_DIALOG_SETTINGS_CLICK
            r7.log(r8)
        L_0x0283:
            java.lang.String r7 = r0.toString()
            return r7
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
