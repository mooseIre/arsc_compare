package com.android.systemui.qs.tiles;

import android.content.ComponentName;
import android.content.Intent;
import android.util.Log;
import android.widget.Switch;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0021R$string;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.statusbar.policy.PaperModeController;
import miui.util.FeatureParser;

public class PaperModeTile extends QSTileImpl<QSTile.BooleanState> implements PaperModeController.PaperModeListener {
    private final PaperModeController mPaperModeController;

    @Override // com.android.systemui.plugins.qs.QSTile, com.android.systemui.qs.tileimpl.QSTileImpl
    public int getMetricsCategory() {
        return -1;
    }

    public PaperModeTile(QSHost qSHost, PaperModeController paperModeController) {
        super(qSHost);
        this.mPaperModeController = paperModeController;
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleClick() {
        String str = this.TAG;
        StringBuilder sb = new StringBuilder();
        sb.append("handleClick: from: ");
        sb.append(((QSTile.BooleanState) this.mState).value);
        sb.append(", to: ");
        sb.append(!((QSTile.BooleanState) this.mState).value);
        Log.d(str, sb.toString());
        boolean z = !((QSTile.BooleanState) this.mState).value;
        refreshState(Boolean.valueOf(z));
        this.mPaperModeController.setEnabled(z);
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public Intent getLongClickIntent() {
        return longClickPaperModeIntent();
    }

    @Override // com.android.systemui.plugins.qs.QSTile
    public CharSequence getTileLabel() {
        return this.mContext.getString(C0021R$string.quick_settings_papermode_label);
    }

    /* access modifiers changed from: protected */
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        booleanState.label = this.mContext.getString(C0021R$string.quick_settings_papermode_label);
        if (!this.mPaperModeController.isAvailable()) {
            booleanState.icon = QSTileImpl.ResourceIcon.get(C0013R$drawable.ic_qs_paper_mode_off);
            booleanState.contentDescription = ((Object) booleanState.label) + "," + this.mContext.getString(C0021R$string.switch_bar_off);
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
            booleanState.value = this.mPaperModeController.isEnabled();
        }
        if (booleanState.value) {
            booleanState.icon = QSTileImpl.ResourceIcon.get(C0013R$drawable.ic_qs_paper_mode_on);
            booleanState.state = 2;
        } else {
            booleanState.icon = QSTileImpl.ResourceIcon.get(C0013R$drawable.ic_qs_paper_mode_off);
            booleanState.state = 1;
        }
        StringBuilder sb = new StringBuilder();
        sb.append((Object) booleanState.label);
        sb.append(",");
        sb.append(this.mContext.getString(booleanState.value ? C0021R$string.switch_bar_on : C0021R$string.switch_bar_off));
        booleanState.contentDescription = sb.toString();
        booleanState.expandedAccessibilityClassName = Switch.class.getName();
        booleanState.activeBgColor = 1;
    }

    @Override // com.android.systemui.plugins.qs.QSTile, com.android.systemui.qs.tileimpl.QSTileImpl
    public boolean isAvailable() {
        return FeatureParser.getBoolean("support_screen_paper_mode", false);
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleSetListening(boolean z) {
        if (z) {
            this.mPaperModeController.addCallback(this);
        } else {
            this.mPaperModeController.removeCallback(this);
        }
    }

    private Intent longClickPaperModeIntent() {
        ComponentName unflattenFromString = ComponentName.unflattenFromString("com.android.settings/.display.ScreenPaperModeActivity");
        if (unflattenFromString == null) {
            return null;
        }
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setComponent(unflattenFromString);
        intent.setFlags(335544320);
        return intent;
    }

    @Override // com.android.systemui.statusbar.policy.PaperModeController.PaperModeListener
    public void onPaperModeChanged(boolean z) {
        refreshState(Boolean.valueOf(z));
    }

    @Override // com.android.systemui.statusbar.policy.PaperModeController.PaperModeListener
    public void onPaperModeAvailabilityChanged(boolean z) {
        refreshState();
    }
}
