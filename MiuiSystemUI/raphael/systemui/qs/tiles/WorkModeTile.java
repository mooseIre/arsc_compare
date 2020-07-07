package com.android.systemui.qs.tiles;

import android.content.Intent;
import android.widget.Switch;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.R;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.statusbar.phone.ManagedProfileController;
import miui.os.Build;

public class WorkModeTile extends QSTileImpl<QSTile.BooleanState> implements ManagedProfileController.Callback {
    private QSTile.Icon mDisable = QSTileImpl.ResourceIcon.get(R.drawable.ic_qs_workmode_disable);
    private QSTile.Icon mEnable = QSTileImpl.ResourceIcon.get(R.drawable.ic_qs_workmode_enable);
    private final ManagedProfileController mProfileController = ((ManagedProfileController) Dependency.get(ManagedProfileController.class));
    private QSTile.Icon mUnavailable = QSTileImpl.ResourceIcon.get(R.drawable.ic_qs_workmode_unavailable);

    public int getMetricsCategory() {
        return 257;
    }

    public WorkModeTile(QSHost qSHost) {
        super(qSHost);
    }

    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    public void handleSetListening(boolean z) {
        if (z) {
            this.mProfileController.addCallback(this);
        } else {
            this.mProfileController.removeCallback(this);
        }
    }

    public Intent getLongClickIntent() {
        return new Intent("android.settings.MANAGED_PROFILE_SETTINGS");
    }

    public void handleClick() {
        this.mProfileController.setWorkModeEnabled(!((QSTile.BooleanState) this.mState).value);
    }

    public boolean isAvailable() {
        return Build.IS_INTERNATIONAL_BUILD && this.mProfileController.hasActiveProfile();
    }

    public void onManagedProfileChanged() {
        refreshState(Boolean.valueOf(this.mProfileController.isWorkModeEnabled()));
    }

    public void onManagedProfileRemoved() {
        this.mHost.removeTile(getTileSpec());
    }

    public CharSequence getTileLabel() {
        return this.mContext.getString(R.string.quick_settings_work_mode_label);
    }

    /* access modifiers changed from: protected */
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        if (obj instanceof Boolean) {
            booleanState.value = ((Boolean) obj).booleanValue();
        } else {
            booleanState.value = this.mProfileController.isWorkModeEnabled();
        }
        booleanState.label = this.mContext.getString(R.string.quick_settings_work_mode_label);
        if (booleanState.value) {
            booleanState.contentDescription = this.mContext.getString(R.string.accessibility_quick_settings_work_mode_on);
        } else {
            booleanState.contentDescription = this.mContext.getString(R.string.accessibility_quick_settings_work_mode_off);
        }
        if (!isAvailable()) {
            booleanState.icon = this.mUnavailable;
            booleanState.state = 0;
        } else {
            booleanState.icon = booleanState.value ? this.mEnable : this.mDisable;
            booleanState.state = booleanState.value ? 2 : 1;
        }
        booleanState.expandedAccessibilityClassName = Switch.class.getName();
    }

    /* access modifiers changed from: protected */
    public String composeChangeAnnouncement() {
        if (((QSTile.BooleanState) this.mState).value) {
            return this.mContext.getString(R.string.accessibility_quick_settings_work_mode_changed_on);
        }
        return this.mContext.getString(R.string.accessibility_quick_settings_work_mode_changed_off);
    }
}
