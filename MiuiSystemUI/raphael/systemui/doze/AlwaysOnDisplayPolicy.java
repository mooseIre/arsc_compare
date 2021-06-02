package com.android.systemui.doze;

import android.content.Context;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;
import android.util.KeyValueListParser;
import android.util.Log;
import com.android.systemui.C0008R$array;

public class AlwaysOnDisplayPolicy {
    public int[] dimmingScrimArray;
    private final Context mContext;
    private final KeyValueListParser mParser = new KeyValueListParser(',');
    private SettingsObserver mSettingsObserver;
    public long proxCooldownPeriodMs;
    public long proxCooldownTriggerMs;
    public long proxScreenOffDelayMs;
    public int[] screenBrightnessArray;
    public long wallpaperFadeOutDuration;
    public long wallpaperVisibilityDuration;

    public AlwaysOnDisplayPolicy(Context context) {
        Context applicationContext = context.getApplicationContext();
        this.mContext = applicationContext;
        SettingsObserver settingsObserver = new SettingsObserver(applicationContext.getMainThreadHandler());
        this.mSettingsObserver = settingsObserver;
        settingsObserver.observe();
    }

    private final class SettingsObserver extends ContentObserver {
        private final Uri ALWAYS_ON_DISPLAY_CONSTANTS_URI = Settings.Global.getUriFor("always_on_display_constants");

        SettingsObserver(Handler handler) {
            super(handler);
        }

        /* access modifiers changed from: package-private */
        public void observe() {
            AlwaysOnDisplayPolicy.this.mContext.getContentResolver().registerContentObserver(this.ALWAYS_ON_DISPLAY_CONSTANTS_URI, false, this, -1);
            update(null);
        }

        public void onChange(boolean z, Uri uri) {
            update(uri);
        }

        public void update(Uri uri) {
            if (uri == null || this.ALWAYS_ON_DISPLAY_CONSTANTS_URI.equals(uri)) {
                Resources resources = AlwaysOnDisplayPolicy.this.mContext.getResources();
                try {
                    AlwaysOnDisplayPolicy.this.mParser.setString(Settings.Global.getString(AlwaysOnDisplayPolicy.this.mContext.getContentResolver(), "always_on_display_constants"));
                } catch (IllegalArgumentException unused) {
                    Log.e("AlwaysOnDisplayPolicy", "Bad AOD constants");
                }
                AlwaysOnDisplayPolicy alwaysOnDisplayPolicy = AlwaysOnDisplayPolicy.this;
                alwaysOnDisplayPolicy.proxScreenOffDelayMs = alwaysOnDisplayPolicy.mParser.getLong("prox_screen_off_delay", 10000);
                AlwaysOnDisplayPolicy alwaysOnDisplayPolicy2 = AlwaysOnDisplayPolicy.this;
                alwaysOnDisplayPolicy2.proxCooldownTriggerMs = alwaysOnDisplayPolicy2.mParser.getLong("prox_cooldown_trigger", 2000);
                AlwaysOnDisplayPolicy alwaysOnDisplayPolicy3 = AlwaysOnDisplayPolicy.this;
                alwaysOnDisplayPolicy3.proxCooldownPeriodMs = alwaysOnDisplayPolicy3.mParser.getLong("prox_cooldown_period", 5000);
                AlwaysOnDisplayPolicy alwaysOnDisplayPolicy4 = AlwaysOnDisplayPolicy.this;
                alwaysOnDisplayPolicy4.wallpaperFadeOutDuration = alwaysOnDisplayPolicy4.mParser.getLong("wallpaper_fade_out_duration", 400);
                AlwaysOnDisplayPolicy alwaysOnDisplayPolicy5 = AlwaysOnDisplayPolicy.this;
                alwaysOnDisplayPolicy5.wallpaperVisibilityDuration = alwaysOnDisplayPolicy5.mParser.getLong("wallpaper_visibility_timeout", 60000);
                AlwaysOnDisplayPolicy alwaysOnDisplayPolicy6 = AlwaysOnDisplayPolicy.this;
                alwaysOnDisplayPolicy6.screenBrightnessArray = alwaysOnDisplayPolicy6.mParser.getIntArray("screen_brightness_array", resources.getIntArray(C0008R$array.config_doze_brightness_sensor_to_brightness));
                AlwaysOnDisplayPolicy alwaysOnDisplayPolicy7 = AlwaysOnDisplayPolicy.this;
                alwaysOnDisplayPolicy7.dimmingScrimArray = alwaysOnDisplayPolicy7.mParser.getIntArray("dimming_scrim_array", resources.getIntArray(C0008R$array.config_doze_brightness_sensor_to_scrim_opacity));
            }
        }
    }
}
