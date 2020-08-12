package com.android.systemui.qs.tiles;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.provider.Settings;
import android.util.Log;
import android.widget.Switch;
import com.android.systemui.Constants;
import com.android.systemui.plugins.R;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.statusbar.Icons;
import miui.os.Build;
import miui.securityspace.CrossUserUtils;
import miui.view.MiuiHapticFeedbackConstants;

public class DriveModeTile extends QSTileImpl<QSTile.BooleanState> {
    public static final boolean IS_MIUI_LITE_VERSION = Build.IS_MIUI_LITE_VERSION;
    private ContentObserver mDriveModeObserver = new ContentObserver(this.mHandler) {
        public void onChange(boolean z) {
            Log.d("SystemUI.DriveMode", "drive mode change detected");
            DriveModeTile.this.refreshState();
        }
    };
    private boolean mMiuiLabDriveModeOn;
    private final ContentResolver mResolver = this.mContext.getContentResolver();

    public int getMetricsCategory() {
        return -1;
    }

    public DriveModeTile(QSHost qSHost) {
        super(qSHost);
        this.mMiuiLabDriveModeOn = -1 != Settings.System.getIntForUser(this.mResolver, "drive_mode_drive_mode", -1, -2);
    }

    /* access modifiers changed from: protected */
    public void handleDestroy() {
        super.handleDestroy();
    }

    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    public boolean isAvailable() {
        return !IS_MIUI_LITE_VERSION && !Constants.IS_INTERNATIONAL && !Constants.IS_TABLET && CrossUserUtils.getCurrentUserId() == 0;
    }

    public void handleSetListening(boolean z) {
        if (z) {
            this.mResolver.registerContentObserver(Settings.System.getUriFor("drive_mode_drive_mode"), false, this.mDriveModeObserver, -1);
        } else {
            this.mResolver.unregisterContentObserver(this.mDriveModeObserver);
        }
    }

    public Intent getLongClickIntent() {
        if (!this.mHost.isDriveModeInstalled()) {
            return getMiuiLabSettingsIntent();
        }
        if (!this.mMiuiLabDriveModeOn) {
            this.mMiuiLabDriveModeOn = -1 != Settings.System.getIntForUser(this.mResolver, "drive_mode_drive_mode", -1, -2);
            if (!this.mMiuiLabDriveModeOn) {
                return getMiuiLabSettingsIntent();
            }
        }
        return longClickDriveModeIntent();
    }

    /* access modifiers changed from: protected */
    public void handleClick() {
        if (!this.mMiuiLabDriveModeOn) {
            this.mMiuiLabDriveModeOn = -1 != Settings.System.getIntForUser(this.mResolver, "drive_mode_drive_mode", -1, -2);
        }
        if (!this.mHost.isDriveModeInstalled()) {
            transitionMiuiLabSettings();
        } else if (!this.mMiuiLabDriveModeOn) {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.xiaomi.drivemode", "com.xiaomi.drivemode.MiuiLabDriveModeActivity"));
            intent.addFlags(MiuiHapticFeedbackConstants.FLAG_MIUI_HAPTIC_TAP_NORMAL);
            intent.putExtra("EXTRA_START_MODE", true);
            postStartActivityDismissingKeyguard(intent, 0);
        } else if (!((QSTile.BooleanState) this.mState).value) {
            startDriveModeActivity();
        } else {
            Settings.System.putIntForUser(this.mResolver, "drive_mode_drive_mode", 0, -2);
            this.mHost.collapsePanels();
        }
    }

    public CharSequence getTileLabel() {
        return this.mContext.getString(R.string.quick_settings_drivemode_label);
    }

    /* access modifiers changed from: protected */
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        Log.d("SystemUI.DriveMode", "drive mode handleUpdateState");
        boolean z = false;
        if (Settings.System.getIntForUser(this.mResolver, "drive_mode_drive_mode", 0, -2) > 0) {
            z = true;
        }
        booleanState.value = z;
        booleanState.label = this.mContext.getString(R.string.quick_settings_drivemode_label);
        if (booleanState.value) {
            booleanState.state = 2;
            booleanState.icon = QSTileImpl.ResourceIcon.get(Icons.getQSIcons(Integer.valueOf(R.drawable.ic_qs_drive_enabled), this.mInControlCenter));
        } else {
            booleanState.state = 1;
            booleanState.icon = QSTileImpl.ResourceIcon.get(Icons.getQSIcons(Integer.valueOf(R.drawable.ic_qs_drive_disabled), this.mInControlCenter));
        }
        StringBuilder sb = new StringBuilder();
        sb.append(booleanState.label);
        sb.append(",");
        sb.append(this.mContext.getString(booleanState.value ? R.string.switch_bar_on : R.string.switch_bar_off));
        booleanState.contentDescription = sb.toString();
        booleanState.expandedAccessibilityClassName = Switch.class.getName();
    }

    private void startDriveModeActivity() {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.xiaomi.drivemode", "com.xiaomi.drivemode.UserGuideActivity"));
        intent.addFlags(MiuiHapticFeedbackConstants.FLAG_MIUI_HAPTIC_TAP_NORMAL);
        intent.putExtra("EXTRA_START_MODE", true);
        postStartActivityDismissingKeyguard(intent, 0);
    }

    private Intent longClickDriveModeIntent() {
        ComponentName unflattenFromString = ComponentName.unflattenFromString("com.xiaomi.drivemode/.DriveModeSettingsActivity");
        if (unflattenFromString == null) {
            return null;
        }
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setComponent(unflattenFromString);
        intent.setFlags(335544320);
        return intent;
    }

    private Intent getMiuiLabSettingsIntent() {
        Intent intent = new Intent();
        intent.setFlags(335544320);
        intent.setAction("android.intent.action.MAIN");
        intent.putExtra(":android:show_fragment", "com.android.settings.MiuiLabSettings");
        intent.setClassName("com.android.settings", "com.android.settings.SubSettings");
        return intent;
    }

    public static void leaveDriveMode(Context context) {
        Settings.System.putIntForUser(context.getContentResolver(), "drive_mode_drive_mode", -1, -2);
        Intent intent = new Intent();
        intent.setAction("com.miui.app.ExtraStatusBarManager.action_leave_drive_mode");
        context.sendBroadcast(intent);
    }

    private void transitionMiuiLabSettings() {
        postStartActivityDismissingKeyguard(getMiuiLabSettingsIntent(), 0);
    }
}
