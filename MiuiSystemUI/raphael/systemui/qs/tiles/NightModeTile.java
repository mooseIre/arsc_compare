package com.android.systemui.qs.tiles;

import android.app.AlertDialog;
import android.app.UiModeManager;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.ContentObserver;
import android.provider.Settings;
import android.util.Log;
import android.widget.Switch;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.Constants;
import com.android.systemui.Prefs;
import com.android.systemui.plugins.R;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.statusbar.Icons;

public class NightModeTile extends QSTileImpl<QSTile.BooleanState> {
    private ContentObserver mNightModeObserver = new ContentObserver(this.mHandler) {
        public void onChange(boolean z) {
            Log.d(NightModeTile.this.TAG, "night mode changed.");
            NightModeTile.this.refreshState();
        }
    };
    private final ContentResolver mResolver = this.mContext.getContentResolver();
    private boolean mShowAlert;
    private final UiModeManager mUiModeManager = ((UiModeManager) this.mContext.getSystemService("uimode"));

    public int getMetricsCategory() {
        return -1;
    }

    public boolean hideCustomizerAfterClick() {
        return true;
    }

    public NightModeTile(QSHost qSHost) {
        super(qSHost);
        boolean z = true;
        this.mShowAlert = true;
        this.mShowAlert = (!Constants.IS_OLED_SCREEN || !Prefs.getBoolean(this.mContext, "QsShowNightAlert", true)) ? false : z;
    }

    public void handleClick() {
        String str = this.TAG;
        StringBuilder sb = new StringBuilder();
        sb.append("handleClick: from: ");
        sb.append(((QSTile.BooleanState) this.mState).value);
        sb.append(", to: ");
        int i = 1;
        sb.append(!((QSTile.BooleanState) this.mState).value);
        Log.d(str, sb.toString());
        boolean z = !((QSTile.BooleanState) this.mState).value;
        refreshState(Boolean.valueOf(z));
        UiModeManager uiModeManager = this.mUiModeManager;
        if (z) {
            i = 2;
        }
        uiModeManager.setNightMode(i);
        if (z && this.mShowAlert) {
            Prefs.putBoolean(this.mContext, "QsShowNightAlert", false);
            this.mShowAlert = false;
            this.mUiHandler.post(new ShowAlertRunnable());
        }
    }

    public void handleSetListening(boolean z) {
        if (z) {
            this.mResolver.registerContentObserver(Settings.Secure.getUriFor("ui_night_mode"), false, this.mNightModeObserver, -1);
        } else {
            this.mResolver.unregisterContentObserver(this.mNightModeObserver);
        }
    }

    /* access modifiers changed from: protected */
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        booleanState.label = this.mContext.getString(R.string.quick_settings_nightmode_label);
        if (obj instanceof Boolean) {
            boolean booleanValue = ((Boolean) obj).booleanValue();
            if (booleanValue != booleanState.value) {
                booleanState.value = booleanValue;
            } else {
                return;
            }
        } else {
            booleanState.value = this.mUiModeManager.getNightMode() == 2;
        }
        if (booleanState.value) {
            booleanState.icon = QSTileImpl.ResourceIcon.get(Icons.getQSIcons(Integer.valueOf(R.drawable.ic_qs_night_mode_on), this.mInControlCenter));
            booleanState.state = 2;
        } else {
            booleanState.icon = QSTileImpl.ResourceIcon.get(Icons.getQSIcons(Integer.valueOf(R.drawable.ic_qs_night_mode_off), this.mInControlCenter));
            booleanState.state = 1;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(booleanState.label);
        sb.append(",");
        sb.append(this.mContext.getString(booleanState.value ? R.string.switch_bar_on : R.string.switch_bar_off));
        booleanState.contentDescription = sb.toString();
        booleanState.expandedAccessibilityClassName = Switch.class.getName();
    }

    /* access modifiers changed from: protected */
    public void handleDestroy() {
        super.handleDestroy();
    }

    public boolean isAvailable() {
        return KeyguardUpdateMonitor.getCurrentUser() == 0 && this.mContext.getResources().getBoolean(R.bool.config_support_night_mode);
    }

    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    public CharSequence getTileLabel() {
        return this.mContext.getString(R.string.quick_settings_nightmode_label);
    }

    public Intent getLongClickIntent() {
        return new Intent("android.settings.DISPLAY_SETTINGS");
    }

    protected class ShowAlertRunnable implements Runnable {
        protected ShowAlertRunnable() {
        }

        public void run() {
            AlertDialog create = new AlertDialog.Builder(NightModeTile.this.mContext, R.style.Theme_Dialog_Alert).setMessage(R.string.qs_open_night_mode_alert_summary).setPositiveButton(17039370, (DialogInterface.OnClickListener) null).create();
            create.getWindow().setType(2010);
            create.getWindow().addPrivateFlags(16);
            create.show();
        }
    }
}
