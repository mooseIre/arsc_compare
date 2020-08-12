package com.android.systemui.statusbar.phone;

import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.MiuiSettings;
import android.provider.Settings;
import android.util.Log;
import android.widget.ImageView;
import com.android.systemui.OverlayManagerWrapper;

public class ElderlyNavigationBarSettingObserver extends ContentObserver {
    private ContentResolver mContentResolver;
    private int mCurrentUserId = ActivityManager.getCurrentUser();
    private NavigationBarView mNavigationBarView;
    private OverlayManagerWrapper mOverlayManager = new OverlayManagerWrapper();

    public ElderlyNavigationBarSettingObserver(Context context, Handler handler, NavigationBarView navigationBarView) {
        super(handler);
        this.mContentResolver = context.getContentResolver();
        this.mNavigationBarView = navigationBarView;
    }

    public void setCurrentUserId(int i) {
        this.mCurrentUserId = i;
        onChange(false);
    }

    public void register() {
        this.mContentResolver.registerContentObserver(Settings.System.getUriFor("elderly_mode"), false, this, -1);
    }

    public void unregister() {
        this.mContentResolver.unregisterContentObserver(this);
    }

    public void onChange(boolean z) {
        super.onChange(z);
        boolean z2 = MiuiSettings.Global.getBoolean(this.mContentResolver, "force_fsg_nav_bar");
        boolean booleanForUser = MiuiSettings.System.getBooleanForUser(this.mContentResolver, "elderly_mode", false, this.mCurrentUserId);
        Log.d("ElderlyNavigationBarSettingObserver", "isScreenButtonHidden ".concat(String.valueOf(z2)));
        Log.d("ElderlyNavigationBarSettingObserver", "isElderlyMode ".concat(String.valueOf(booleanForUser)));
        boolean z3 = !z2 && booleanForUser;
        boolean isOverlay = isOverlay(this.mCurrentUserId);
        Log.d("ElderlyNavigationBarSettingObserver", "needOverlay ".concat(String.valueOf(z3)));
        Log.d("ElderlyNavigationBarSettingObserver", "isOverlay ".concat(String.valueOf(isOverlay)));
        if (z3 != isOverlay) {
            try {
                Log.d("ElderlyNavigationBarSettingObserver", "needOverlay != isOverlay");
                this.mOverlayManager.setEnabled("com.android.systemui.navigation.bar.overlay", z3, this.mCurrentUserId);
            } catch (Exception e) {
                Log.d("ElderlyNavigationBarSettingObserver", "Can't apply overlay for user " + this.mCurrentUserId, e);
                this.mNavigationBarView.updateImageViewScaleType(ImageView.ScaleType.CENTER);
            }
        }
        if (!(this.mCurrentUserId == 0 || z3 == isOverlay(0))) {
            try {
                Log.d("ElderlyNavigationBarSettingObserver", "mCurrentUserId != UserHandle.USER_OWNER");
                this.mOverlayManager.setEnabled("com.android.systemui.navigation.bar.overlay", z3, 0);
            } catch (Exception e2) {
                Log.d("ElderlyNavigationBarSettingObserver", "Can't apply overlay for user owner", e2);
                this.mNavigationBarView.updateImageViewScaleType(ImageView.ScaleType.CENTER);
            }
        }
        if (z3) {
            this.mNavigationBarView.updateImageViewScaleType(ImageView.ScaleType.CENTER_CROP);
        } else {
            this.mNavigationBarView.updateImageViewScaleType(ImageView.ScaleType.CENTER);
        }
    }

    private boolean isOverlay(int i) {
        OverlayManagerWrapper.OverlayInfo overlayInfo;
        try {
            overlayInfo = this.mOverlayManager.getOverlayInfo("com.android.systemui.navigation.bar.overlay", i);
        } catch (Exception e) {
            Log.d("ElderlyNavigationBarSettingObserver", "Can't get overlay info for user " + i, e);
            overlayInfo = null;
        }
        return overlayInfo != null && overlayInfo.isEnabled();
    }
}
