package com.android.systemui.qs.tiles;

import android.app.ActivityManager;
import android.content.Intent;
import android.widget.Switch;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.R;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.statusbar.Icons;
import com.android.systemui.statusbar.policy.FlashlightController;

public class FlashlightTile extends QSTileImpl<QSTile.BooleanState> implements FlashlightController.FlashlightListener {
    private final FlashlightController mFlashlightController = ((FlashlightController) Dependency.get(FlashlightController.class));

    public int getMetricsCategory() {
        return 119;
    }

    /* access modifiers changed from: protected */
    public void handleLongClick() {
    }

    /* access modifiers changed from: protected */
    public void handleUserSwitch(int i) {
    }

    public FlashlightTile(QSHost qSHost) {
        super(qSHost);
    }

    /* access modifiers changed from: protected */
    public void handleDestroy() {
        super.handleDestroy();
    }

    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    public void handleSetListening(boolean z) {
        if (z) {
            this.mFlashlightController.addCallback(this);
        } else {
            this.mFlashlightController.removeCallback(this);
        }
    }

    public Intent getLongClickIntent() {
        return new Intent("android.media.action.STILL_IMAGE_CAMERA");
    }

    public boolean isAvailable() {
        return this.mFlashlightController.hasFlashlight();
    }

    /* access modifiers changed from: protected */
    public void handleClick() {
        if (!ActivityManager.isUserAMonkey()) {
            this.mFlashlightController.setFlashlight(!((QSTile.BooleanState) this.mState).value);
            refreshState();
        }
    }

    public CharSequence getTileLabel() {
        return this.mContext.getString(R.string.quick_settings_flashlight_label);
    }

    /* access modifiers changed from: protected */
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        booleanState.label = this.mContext.getString(R.string.quick_settings_flashlight_label);
        if (!this.mFlashlightController.isAvailable()) {
            booleanState.icon = QSTileImpl.ResourceIcon.get(Icons.getQSIcons(Integer.valueOf(R.drawable.ic_qs_flashlight_unavailable), this.mInControlCenter));
            booleanState.contentDescription = this.mContext.getString(R.string.accessibility_quick_settings_flashlight_unavailable);
            booleanState.state = 0;
            return;
        }
        if (obj instanceof Boolean) {
            boolean booleanValue = ((Boolean) obj).booleanValue();
            if (booleanValue != booleanState.value) {
                booleanState.value = booleanValue;
            } else {
                return;
            }
        } else {
            booleanState.value = this.mFlashlightController.isEnabled();
        }
        booleanState.state = booleanState.value ? 2 : 1;
        booleanState.activeBgColor = 1;
        booleanState.icon = QSTileImpl.ResourceIcon.get(Icons.getQSIcons(Integer.valueOf(booleanState.value ? R.drawable.ic_qs_flashlight_enabled : R.drawable.ic_qs_flashlight_disabled), this.mInControlCenter));
        StringBuilder sb = new StringBuilder();
        sb.append(booleanState.label);
        sb.append(",");
        sb.append(this.mContext.getString(booleanState.value ? R.string.switch_bar_on : R.string.switch_bar_off));
        booleanState.contentDescription = sb.toString();
        booleanState.expandedAccessibilityClassName = Switch.class.getName();
    }

    /* access modifiers changed from: protected */
    public String composeChangeAnnouncement() {
        if (((QSTile.BooleanState) this.mState).value) {
            return this.mContext.getString(R.string.accessibility_quick_settings_flashlight_changed_on);
        }
        return this.mContext.getString(R.string.accessibility_quick_settings_flashlight_changed_off);
    }

    public void onFlashlightChanged(boolean z) {
        refreshState(Boolean.valueOf(z));
    }

    public void onFlashlightError() {
        refreshState(false);
    }

    public void onFlashlightAvailabilityChanged(boolean z) {
        refreshState();
    }
}
