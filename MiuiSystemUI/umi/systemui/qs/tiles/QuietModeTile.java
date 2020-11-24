package com.android.systemui.qs.tiles;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.widget.Switch;
import androidx.appcompat.R$styleable;
import com.android.systemui.C0010R$drawable;
import com.android.systemui.C0018R$string;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.statusbar.policy.ZenModeController;

public class QuietModeTile extends QSTileImpl<QSTile.BooleanState> implements ZenModeController.Callback {
    private final ZenModeController mZenModeController;

    public int getMetricsCategory() {
        return R$styleable.AppCompatTheme_windowActionBarOverlay;
    }

    public QuietModeTile(QSHost qSHost, ZenModeController zenModeController) {
        super(qSHost);
        this.mZenModeController = zenModeController;
        zenModeController.observe(getLifecycle(), this);
    }

    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    public Intent getLongClickIntent() {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setComponent(ComponentName.unflattenFromString("com.android.settings/com.android.settings.Settings$SoundSettingsActivity"));
        intent.setFlags(335544320);
        return intent;
    }

    /* access modifiers changed from: protected */
    public void handleClick() {
        if (this.mZenModeController.isZenAvailable()) {
            ZenModeController zenModeController = this.mZenModeController;
            zenModeController.setZen(zenModeController.isZenModeOn() ^ true ? 1 : 0, (Uri) null, this.TAG);
        }
    }

    public CharSequence getTileLabel() {
        return this.mContext.getString(C0018R$string.quick_settings_quietmode_label);
    }

    /* access modifiers changed from: protected */
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        booleanState.value = this.mZenModeController.isZenModeOn();
        booleanState.label = this.mContext.getString(C0018R$string.quick_settings_quietmode_label);
        if (booleanState.value) {
            booleanState.state = 2;
            booleanState.icon = QSTileImpl.ResourceIcon.get(C0010R$drawable.ic_qs_dnd_on);
        } else {
            booleanState.state = 1;
            booleanState.icon = QSTileImpl.ResourceIcon.get(C0010R$drawable.ic_qs_dnd_off);
        }
        StringBuilder sb = new StringBuilder();
        sb.append(booleanState.label);
        sb.append(",");
        sb.append(this.mContext.getString(booleanState.value ? C0018R$string.switch_bar_on : C0018R$string.switch_bar_off));
        booleanState.contentDescription = sb.toString();
        booleanState.expandedAccessibilityClassName = Switch.class.getName();
    }

    public boolean isAvailable() {
        return this.mZenModeController.isZenAvailable();
    }

    public void onZenOrRingerChanged(boolean z, boolean z2) {
        refreshState(Boolean.valueOf(z));
    }
}
