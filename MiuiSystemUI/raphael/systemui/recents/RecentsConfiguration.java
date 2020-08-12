package com.android.systemui.recents;

import android.content.Context;
import android.content.res.Resources;
import com.android.systemui.plugins.R;
import com.android.systemui.recents.misc.SystemServicesProxy;

public class RecentsConfiguration {
    public static boolean sCanMultiWindow = false;
    public boolean fakeShadows;
    public final boolean isLargeScreen;
    public final boolean isXLargeScreen;
    public RecentsActivityLaunchState mLaunchState = new RecentsActivityLaunchState();
    public final int smallestWidth;
    public int svelteLevel;

    public RecentsConfiguration(Context context) {
        SystemServicesProxy systemServices = Recents.getSystemServices();
        Resources resources = context.getApplicationContext().getResources();
        this.fakeShadows = resources.getBoolean(R.bool.config_recents_fake_shadows);
        this.svelteLevel = resources.getInteger(R.integer.recents_svelte_level);
        float f = context.getResources().getDisplayMetrics().density;
        this.smallestWidth = systemServices.getDeviceSmallestWidth();
        boolean z = true;
        this.isLargeScreen = this.smallestWidth >= ((int) (600.0f * f));
        this.isXLargeScreen = this.smallestWidth < ((int) (f * 720.0f)) ? false : z;
    }

    public RecentsActivityLaunchState getLaunchState() {
        return this.mLaunchState;
    }
}
