package com.android.systemui.bubbles;

import android.content.Context;
import android.provider.Settings;

public class BubbleDebugConfig {
    static boolean forceShowUserEducation(Context context) {
        return Settings.Secure.getInt(context.getContentResolver(), "force_show_bubbles_user_education", 0) != 0;
    }
}
