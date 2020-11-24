package com.android.systemui.qs.tiles;

import android.content.ComponentName;
import android.content.Intent;
import android.util.Log;
import android.widget.Switch;
import androidx.lifecycle.LifecycleOwner;
import com.android.systemui.C0010R$drawable;
import com.android.systemui.C0018R$string;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.statusbar.policy.DriveModeController;
import miui.os.Build;
import miui.securityspace.CrossUserUtils;

public class DriveModeTile extends QSTileImpl<QSTile.BooleanState> {
    private final DriveModeController mDriveModeController;

    public int getMetricsCategory() {
        return -1;
    }

    public DriveModeTile(QSHost qSHost, DriveModeController driveModeController) {
        super(qSHost);
        this.mDriveModeController = driveModeController;
        driveModeController.observe((LifecycleOwner) this, new DriveModeController.DriveModeListener() {
            public final void onDriveModeChanged() {
                DriveModeTile.this.refreshState();
            }
        });
    }

    /* access modifiers changed from: protected */
    public void handleDestroy() {
        super.handleDestroy();
    }

    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    public boolean isAvailable() {
        return !Build.IS_INTERNATIONAL_BUILD && !Build.IS_TABLET && CrossUserUtils.getCurrentUserId() == 0;
    }

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

    public CharSequence getTileLabel() {
        return this.mContext.getString(C0018R$string.quick_settings_drivemode_label);
    }

    /* access modifiers changed from: protected */
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        Log.d("SystemUI.DriveMode", "drive mode handleUpdateState");
        booleanState.value = this.mDriveModeController.isDriveModeEnabled();
        booleanState.label = this.mContext.getString(C0018R$string.quick_settings_drivemode_label);
        if (booleanState.value) {
            booleanState.state = 2;
            booleanState.icon = QSTileImpl.ResourceIcon.get(C0010R$drawable.ic_qs_drive_enabled);
        } else {
            booleanState.state = 1;
            booleanState.icon = QSTileImpl.ResourceIcon.get(C0010R$drawable.ic_qs_drive_disabled);
        }
        StringBuilder sb = new StringBuilder();
        sb.append(booleanState.label);
        sb.append(",");
        sb.append(this.mContext.getString(booleanState.value ? C0018R$string.switch_bar_on : C0018R$string.switch_bar_off));
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
}
