package com.android.systemui.qs.tiles;

import android.content.Context;
import android.content.Intent;
import android.widget.Switch;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.R;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.statusbar.Icons;
import com.android.systemui.statusbar.policy.RotationLockController;

public class RotationLockTile extends QSTileImpl<QSTile.BooleanState> {
    private final RotationLockController.RotationLockControllerCallback mCallback = new RotationLockController.RotationLockControllerCallback() {
        public void onRotationLockStateChanged(boolean z, boolean z2) {
            RotationLockTile.this.refreshState(Boolean.valueOf(z));
        }
    };
    private final RotationLockController mController = ((RotationLockController) Dependency.get(RotationLockController.class));

    public int getMetricsCategory() {
        return 123;
    }

    public RotationLockTile(QSHost qSHost) {
        super(qSHost);
    }

    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    public void handleSetListening(boolean z) {
        RotationLockController rotationLockController = this.mController;
        if (rotationLockController != null) {
            if (z) {
                rotationLockController.addCallback(this.mCallback);
            } else {
                rotationLockController.removeCallback(this.mCallback);
            }
        }
    }

    public Intent getLongClickIntent() {
        return new Intent("android.settings.DISPLAY_SETTINGS");
    }

    /* access modifiers changed from: protected */
    public void handleClick() {
        RotationLockController rotationLockController = this.mController;
        if (rotationLockController != null) {
            boolean z = !((QSTile.BooleanState) this.mState).value;
            rotationLockController.setRotationLocked(z);
            refreshState(Boolean.valueOf(z));
        }
    }

    public CharSequence getTileLabel() {
        return ((QSTile.BooleanState) getState()).label;
    }

    /* access modifiers changed from: protected */
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        RotationLockController rotationLockController = this.mController;
        if (rotationLockController != null) {
            boolean isRotationLocked = rotationLockController.isRotationLocked();
            booleanState.value = isRotationLocked;
            booleanState.label = this.mContext.getString(286130553);
            booleanState.icon = QSTileImpl.ResourceIcon.get(Icons.getQSIcons(Integer.valueOf(!isRotationLocked ? R.drawable.ic_qs_auto_rotate_enabled : R.drawable.ic_qs_auto_rotate_disabled), this.mInControlCenter));
            booleanState.contentDescription = getAccessibilityString(isRotationLocked);
            booleanState.expandedAccessibilityClassName = Switch.class.getName();
            booleanState.state = booleanState.value ? 2 : 1;
        }
    }

    public static boolean isCurrentOrientationLockPortrait(RotationLockController rotationLockController, Context context) {
        int rotationLockOrientation = rotationLockController.getRotationLockOrientation();
        if (rotationLockOrientation == 0) {
            if (context.getResources().getConfiguration().orientation != 2) {
                return true;
            }
            return false;
        } else if (rotationLockOrientation != 2) {
            return true;
        } else {
            return false;
        }
    }

    private String getAccessibilityString(boolean z) {
        String str;
        if (!z) {
            return this.mContext.getString(R.string.accessibility_quick_settings_rotation);
        }
        StringBuilder sb = new StringBuilder();
        Context context = this.mContext;
        Object[] objArr = new Object[1];
        if (isCurrentOrientationLockPortrait(this.mController, context)) {
            str = this.mContext.getString(R.string.quick_settings_rotation_locked_portrait_label);
        } else {
            str = this.mContext.getString(R.string.quick_settings_rotation_locked_landscape_label);
        }
        objArr[0] = str;
        sb.append(context.getString(R.string.accessibility_quick_settings_rotation_value, objArr));
        sb.append(",");
        sb.append(this.mContext.getString(R.string.accessibility_quick_settings_rotation));
        return sb.toString();
    }

    /* access modifiers changed from: protected */
    public String composeChangeAnnouncement() {
        return getAccessibilityString(((QSTile.BooleanState) this.mState).value);
    }
}
