package com.android.systemui.qs.tiles;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;
import android.widget.Switch;
import com.android.systemui.plugins.R;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.GlobalSetting;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.statusbar.Icons;

public class AirplaneModeTile extends QSTileImpl<QSTile.BooleanState> {
    private boolean mListening;
    private final GlobalSetting mSetting = new GlobalSetting(this.mContext, this.mHandler, "airplane_mode_on") {
        /* access modifiers changed from: protected */
        public void handleValueChanged(int i) {
            String access$000 = AirplaneModeTile.this.TAG;
            Log.d(access$000, "handleValueChanged: value = " + i);
            int unused = AirplaneModeTile.this.mTargetValue = i;
            AirplaneModeTile.this.handleRefreshState(Integer.valueOf(i));
        }
    };
    /* access modifiers changed from: private */
    public int mTargetValue = this.mSetting.getValue();

    public int getMetricsCategory() {
        return R.styleable.AppCompatTheme_tooltipForegroundColor;
    }

    public AirplaneModeTile(QSHost qSHost) {
        super(qSHost);
    }

    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    public void handleClick() {
        int value = this.mSetting.getValue();
        if (value != this.mTargetValue) {
            String str = this.TAG;
            Log.d(str, "handleClick: mTargetValue = " + this.mTargetValue + ", value = " + value);
            return;
        }
        boolean z = true;
        if (value == 1) {
            z = false;
        }
        setEnabled(z);
        refreshState();
    }

    private void setEnabled(boolean z) {
        ((ConnectivityManager) this.mContext.getSystemService("connectivity")).setAirplaneMode(z);
    }

    public Intent getLongClickIntent() {
        return new Intent("android.settings.AIRPLANE_MODE_SETTINGS");
    }

    public CharSequence getTileLabel() {
        return this.mContext.getString(R.string.airplane_mode);
    }

    /* access modifiers changed from: protected */
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        boolean z = false;
        int i = 1;
        boolean z2 = (obj instanceof Integer ? ((Integer) obj).intValue() : this.mSetting.getValue()) == 1;
        booleanState.value = z2;
        if (!z2 && this.mTargetValue == 1) {
            z = true;
        }
        booleanState.withAnimation = z;
        booleanState.label = this.mContext.getString(R.string.airplane_mode);
        if (z2) {
            booleanState.icon = QSTileImpl.ResourceIcon.get(Icons.getQSIcons(Integer.valueOf(R.drawable.ic_signal_airplane_enable), this.mInControlCenter));
        } else {
            booleanState.icon = QSTileImpl.ResourceIcon.get(Icons.getQSIcons(Integer.valueOf(R.drawable.ic_signal_airplane_disable), this.mInControlCenter));
        }
        if (z2) {
            i = 2;
        }
        booleanState.state = i;
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
            return this.mContext.getString(R.string.accessibility_quick_settings_airplane_changed_on);
        }
        return this.mContext.getString(R.string.accessibility_quick_settings_airplane_changed_off);
    }

    public void handleSetListening(boolean z) {
        if (this.mListening != z) {
            this.mListening = z;
            this.mTargetValue = this.mSetting.getValue();
            this.mSetting.setListening(z);
        }
    }
}
