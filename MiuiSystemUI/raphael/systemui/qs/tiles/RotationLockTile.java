package com.android.systemui.qs.tiles;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.widget.Switch;
import androidx.appcompat.R$styleable;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0021R$string;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.statusbar.policy.RotationLockController;

public class RotationLockTile extends QSTileImpl<QSTile.BooleanState> {
    private final RotationLockController.RotationLockControllerCallback mCallback = new RotationLockController.RotationLockControllerCallback() {
        /* class com.android.systemui.qs.tiles.RotationLockTile.AnonymousClass1 */

        @Override // com.android.systemui.statusbar.policy.RotationLockController.RotationLockControllerCallback
        public void onRotationLockStateChanged(boolean z, boolean z2) {
            RotationLockTile.this.refreshState(Boolean.valueOf(z));
        }
    };
    private final RotationLockController mController;

    @Override // com.android.systemui.plugins.qs.QSTile, com.android.systemui.qs.tileimpl.QSTileImpl
    public int getMetricsCategory() {
        return R$styleable.AppCompatTheme_windowFixedWidthMinor;
    }

    public RotationLockTile(QSHost qSHost, RotationLockController rotationLockController) {
        super(qSHost);
        this.mController = rotationLockController;
        rotationLockController.observe(getLifecycle(), this.mCallback);
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public Intent getLongClickIntent() {
        return new Intent("android.settings.DISPLAY_SETTINGS");
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleClick() {
        boolean z = !((QSTile.BooleanState) this.mState).value;
        this.mController.setRotationLockedAtAngle(z, -1);
        refreshState(Boolean.valueOf(z));
    }

    @Override // com.android.systemui.plugins.qs.QSTile
    public CharSequence getTileLabel() {
        return ((QSTile.BooleanState) getState()).label;
    }

    /* access modifiers changed from: protected */
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        int i;
        boolean isRotationLocked = this.mController.isRotationLocked();
        booleanState.value = isRotationLocked;
        booleanState.label = this.mContext.getString(C0021R$string.quick_settings_rotationlock_label);
        if (!isRotationLocked) {
            i = C0013R$drawable.ic_qs_auto_rotate_enabled;
        } else {
            i = C0013R$drawable.ic_qs_auto_rotate_disabled;
        }
        booleanState.icon = QSTileImpl.ResourceIcon.get(i);
        booleanState.contentDescription = getAccessibilityString(isRotationLocked);
        booleanState.expandedAccessibilityClassName = Switch.class.getName();
        booleanState.state = booleanState.value ? 2 : 1;
    }

    public static boolean isCurrentOrientationLockPortrait(RotationLockController rotationLockController, Resources resources) {
        int rotationLockOrientation = rotationLockController.getRotationLockOrientation();
        return rotationLockOrientation == 0 ? resources.getConfiguration().orientation != 2 : rotationLockOrientation != 2;
    }

    private String getAccessibilityString(boolean z) {
        String str;
        if (!z) {
            return this.mContext.getString(C0021R$string.accessibility_quick_settings_rotation);
        }
        StringBuilder sb = new StringBuilder();
        Context context = this.mContext;
        int i = C0021R$string.accessibility_quick_settings_rotation_value;
        Object[] objArr = new Object[1];
        if (isCurrentOrientationLockPortrait(this.mController, context.getResources())) {
            str = this.mContext.getString(C0021R$string.quick_settings_rotation_locked_portrait_label);
        } else {
            str = this.mContext.getString(C0021R$string.quick_settings_rotation_locked_landscape_label);
        }
        objArr[0] = str;
        sb.append(context.getString(i, objArr));
        sb.append(",");
        sb.append(this.mContext.getString(C0021R$string.accessibility_quick_settings_rotation));
        return sb.toString();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public String composeChangeAnnouncement() {
        return getAccessibilityString(((QSTile.BooleanState) this.mState).value);
    }
}
