package com.android.systemui.qs.tiles;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.UiModeManager;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Log;
import android.widget.Switch;
import com.android.systemui.C0007R$bool;
import com.android.systemui.C0010R$drawable;
import com.android.systemui.C0018R$string;
import com.android.systemui.C0019R$style;
import com.android.systemui.Prefs;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;

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
        this.mShowAlert = (!"oled".equals(SystemProperties.get("ro.display.type")) || !Prefs.getBoolean(this.mContext, "QsShowNightAlert", true)) ? false : z;
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
        booleanState.label = this.mContext.getString(C0018R$string.quick_settings_nightmode_label);
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
            booleanState.icon = QSTileImpl.ResourceIcon.get(C0010R$drawable.ic_qs_night_mode_on);
            booleanState.state = 2;
        } else {
            booleanState.icon = QSTileImpl.ResourceIcon.get(C0010R$drawable.ic_qs_night_mode_off);
            booleanState.state = 1;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(booleanState.label);
        sb.append(",");
        sb.append(this.mContext.getString(booleanState.value ? C0018R$string.switch_bar_on : C0018R$string.switch_bar_off));
        booleanState.contentDescription = sb.toString();
        booleanState.expandedAccessibilityClassName = Switch.class.getName();
    }

    /* access modifiers changed from: protected */
    public void handleDestroy() {
        super.handleDestroy();
    }

    public boolean isAvailable() {
        return ActivityManager.getCurrentUser() == 0 && this.mContext.getResources().getBoolean(C0007R$bool.config_support_night_mode);
    }

    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    public CharSequence getTileLabel() {
        return this.mContext.getString(C0018R$string.quick_settings_nightmode_label);
    }

    public Intent getLongClickIntent() {
        return new Intent("android.settings.DISPLAY_SETTINGS");
    }

    protected class ShowAlertRunnable implements Runnable {
        protected ShowAlertRunnable() {
        }

        public void run() {
            AlertDialog create = new AlertDialog.Builder(NightModeTile.this.mContext, C0019R$style.Theme_Dialog_Alert).setMessage(C0018R$string.qs_open_night_mode_alert_summary).setPositiveButton(17039370, (DialogInterface.OnClickListener) null).create();
            create.getWindow().setType(2010);
            create.show();
        }
    }
}
