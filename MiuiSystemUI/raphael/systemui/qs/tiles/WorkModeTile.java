package com.android.systemui.qs.tiles;

import android.content.Intent;
import android.widget.Switch;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0021R$string;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.statusbar.phone.ManagedProfileController;
import miuix.os.Build;

public class WorkModeTile extends QSTileImpl<QSTile.BooleanState> implements ManagedProfileController.Callback {
    private final QSTile.Icon mIcon = QSTileImpl.ResourceIcon.get(C0013R$drawable.stat_sys_managed_profile_status);
    private final ManagedProfileController mProfileController;

    @Override // com.android.systemui.plugins.qs.QSTile, com.android.systemui.qs.tileimpl.QSTileImpl
    public int getMetricsCategory() {
        return 257;
    }

    public WorkModeTile(QSHost qSHost, ManagedProfileController managedProfileController) {
        super(qSHost);
        this.mProfileController = managedProfileController;
        managedProfileController.observe(getLifecycle(), this);
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public Intent getLongClickIntent() {
        return new Intent("android.settings.MANAGED_PROFILE_SETTINGS");
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleClick() {
        this.mProfileController.setWorkModeEnabled(!((QSTile.BooleanState) this.mState).value);
    }

    @Override // com.android.systemui.plugins.qs.QSTile, com.android.systemui.qs.tileimpl.QSTileImpl
    public boolean isAvailable() {
        return Build.IS_INTERNATIONAL_BUILD && this.mProfileController.hasActiveProfile();
    }

    @Override // com.android.systemui.statusbar.phone.ManagedProfileController.Callback
    public void onManagedProfileChanged() {
        refreshState(Boolean.valueOf(this.mProfileController.isWorkModeEnabled()));
    }

    @Override // com.android.systemui.statusbar.phone.ManagedProfileController.Callback
    public void onManagedProfileRemoved() {
        this.mHost.removeTile(getTileSpec());
        this.mHost.unmarkTileAsAutoAdded(getTileSpec());
    }

    @Override // com.android.systemui.plugins.qs.QSTile
    public CharSequence getTileLabel() {
        return this.mContext.getString(C0021R$string.quick_settings_work_mode_label);
    }

    /* access modifiers changed from: protected */
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        if (!isAvailable()) {
            onManagedProfileRemoved();
        }
        if (booleanState.slash == null) {
            booleanState.slash = new QSTile.SlashState();
        }
        if (obj instanceof Boolean) {
            booleanState.value = ((Boolean) obj).booleanValue();
        } else {
            booleanState.value = this.mProfileController.isWorkModeEnabled();
        }
        booleanState.icon = this.mIcon;
        int i = 1;
        if (booleanState.value) {
            booleanState.slash.isSlashed = false;
        } else {
            booleanState.slash.isSlashed = true;
        }
        String string = this.mContext.getString(C0021R$string.quick_settings_work_mode_label);
        booleanState.label = string;
        booleanState.contentDescription = string;
        booleanState.expandedAccessibilityClassName = Switch.class.getName();
        if (booleanState.value) {
            i = 2;
        }
        booleanState.state = i;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public String composeChangeAnnouncement() {
        if (((QSTile.BooleanState) this.mState).value) {
            return this.mContext.getString(C0021R$string.accessibility_quick_settings_work_mode_changed_on);
        }
        return this.mContext.getString(C0021R$string.accessibility_quick_settings_work_mode_changed_off);
    }
}
