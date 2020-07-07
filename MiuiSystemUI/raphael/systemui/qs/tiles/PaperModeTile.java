package com.android.systemui.qs.tiles;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Switch;
import com.android.systemui.Constants;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.R;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.statusbar.Icons;
import com.android.systemui.statusbar.policy.PaperModeController;

public class PaperModeTile extends QSTileImpl<QSTile.BooleanState> implements PaperModeController.PaperModeListener {
    private final PaperModeController mPaperModeController = ((PaperModeController) Dependency.get(PaperModeController.class));

    public int getMetricsCategory() {
        return -1;
    }

    public PaperModeTile(QSHost qSHost) {
        super(qSHost);
    }

    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

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

    public Intent getLongClickIntent() {
        return longClickPaperModeIntent();
    }

    public CharSequence getTileLabel() {
        return this.mContext.getString(R.string.quick_settings_papermode_label);
    }

    /* access modifiers changed from: protected */
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        booleanState.label = this.mContext.getString(R.string.quick_settings_papermode_label);
        boolean isAvailable = this.mPaperModeController.isAvailable();
        int i = R.string.switch_bar_off;
        Integer valueOf = Integer.valueOf(R.drawable.ic_qs_paper_mode_off);
        if (!isAvailable) {
            booleanState.icon = QSTileImpl.ResourceIcon.get(Icons.getQSIcons(valueOf, this.mInControlCenter));
            booleanState.contentDescription = booleanState.label + "," + this.mContext.getString(R.string.switch_bar_off);
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
            booleanState.icon = QSTileImpl.ResourceIcon.get(Icons.getQSIcons(Integer.valueOf(R.drawable.ic_qs_paper_mode_on), this.mInControlCenter));
            booleanState.state = 2;
        } else {
            booleanState.icon = QSTileImpl.ResourceIcon.get(Icons.getQSIcons(valueOf, this.mInControlCenter));
            booleanState.state = 1;
        }
        booleanState.activeBgColor = 1;
        StringBuilder sb = new StringBuilder();
        sb.append(booleanState.label);
        sb.append(",");
        Context context = this.mContext;
        if (booleanState.value) {
            i = R.string.switch_bar_on;
        }
        sb.append(context.getString(i));
        booleanState.contentDescription = sb.toString();
        booleanState.expandedAccessibilityClassName = Switch.class.getName();
    }

    public boolean isAvailable() {
        return Constants.SUPPORT_SCREEN_PAPER_MODE;
    }

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

    public void onPaperModeChanged(boolean z) {
        refreshState(Boolean.valueOf(z));
    }

    public void onPaperModeAvailabilityChanged(boolean z) {
        refreshState();
    }
}
