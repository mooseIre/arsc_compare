package com.android.systemui.qs.tiles;

import android.content.ComponentName;
import android.content.Intent;
import android.os.SystemProperties;
import android.util.Log;
import android.widget.Switch;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0021R$string;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.statusbar.policy.DriveModeController;
import miui.os.Build;
import miui.securityspace.CrossUserUtils;

public class DriveModeTile extends QSTileImpl<QSTile.BooleanState> {
    private static final String[] BLACK_LIST = {"camellia", "camellian", "lime"};
    private final DriveModeController mDriveModeController;

    @Override // com.android.systemui.plugins.qs.QSTile, com.android.systemui.qs.tileimpl.QSTileImpl
    public int getMetricsCategory() {
        return -1;
    }

    public DriveModeTile(QSHost qSHost, DriveModeController driveModeController) {
        super(qSHost);
        this.mDriveModeController = driveModeController;
        driveModeController.observe(this, new DriveModeController.DriveModeListener() {
            /* class com.android.systemui.qs.tiles.$$Lambda$d51C4tMnNLo3Eer3I5Y2mzfmMk */

            @Override // com.android.systemui.statusbar.policy.DriveModeController.DriveModeListener
            public final void onDriveModeChanged() {
                DriveModeTile.this.refreshState();
            }
        });
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleDestroy() {
        super.handleDestroy();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    @Override // com.android.systemui.plugins.qs.QSTile, com.android.systemui.qs.tileimpl.QSTileImpl
    public boolean isAvailable() {
        return !isInBlackList() && !Build.IS_INTERNATIONAL_BUILD && !Build.IS_TABLET && CrossUserUtils.getCurrentUserId() == 0;
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public Intent getLongClickIntent() {
        if (!this.mDriveModeController.isDriveModeAvailable()) {
            return getMiuiLabSettingsIntent();
        }
        if (!this.mDriveModeController.isMiuiLabDriveModeOn()) {
            return getMiuiLabSettingsIntent();
        }
        return longClickDriveModeIntent();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleClick() {
        if (!this.mDriveModeController.isDriveModeAvailable()) {
            transitionMiuiLabSettings();
        } else if (!this.mDriveModeController.isMiuiLabDriveModeOn()) {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.xiaomi.drivemode", "com.xiaomi.drivemode.MiuiLabDriveModeActivity"));
            intent.addFlags(268435456);
            intent.putExtra("EXTRA_START_MODE", true);
            postStartActivityDismissingKeyguard(intent, 0);
        } else if (!((QSTile.BooleanState) this.mState).value) {
            startDriveModeActivity();
        } else {
            this.mDriveModeController.setDriveModeEnabled(false);
            this.mHost.collapsePanels();
        }
    }

    @Override // com.android.systemui.plugins.qs.QSTile
    public CharSequence getTileLabel() {
        return this.mContext.getString(C0021R$string.quick_settings_drivemode_label);
    }

    /* access modifiers changed from: protected */
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        Log.d("SystemUI.DriveMode", "drive mode handleUpdateState");
        booleanState.value = this.mDriveModeController.isDriveModeEnabled();
        booleanState.label = this.mContext.getString(C0021R$string.quick_settings_drivemode_label);
        if (booleanState.value) {
            booleanState.state = 2;
            booleanState.icon = QSTileImpl.ResourceIcon.get(C0013R$drawable.ic_qs_drive_enabled);
        } else {
            booleanState.state = 1;
            booleanState.icon = QSTileImpl.ResourceIcon.get(C0013R$drawable.ic_qs_drive_disabled);
        }
        StringBuilder sb = new StringBuilder();
        sb.append((Object) booleanState.label);
        sb.append(",");
        sb.append(this.mContext.getString(booleanState.value ? C0021R$string.switch_bar_on : C0021R$string.switch_bar_off));
        booleanState.contentDescription = sb.toString();
        booleanState.expandedAccessibilityClassName = Switch.class.getName();
    }

    private void startDriveModeActivity() {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.xiaomi.drivemode", "com.xiaomi.drivemode.UserGuideActivity"));
        intent.addFlags(268435456);
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

    private void transitionMiuiLabSettings() {
        postStartActivityDismissingKeyguard(getMiuiLabSettingsIntent(), 0);
    }

    public boolean isInBlackList() {
        try {
            String str = SystemProperties.get("ro.product.vendor.name", "null");
            for (String str2 : BLACK_LIST) {
                if (str.equals(str2)) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(this.TAG, "Exception: " + e);
        }
        return false;
    }
}
