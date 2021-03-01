package com.android.systemui.qs.tiles;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.ContentObserver;
import android.provider.MiuiSettings;
import android.provider.Settings;
import android.view.ViewConfiguration;
import android.widget.Switch;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0021R$string;
import com.android.systemui.C0022R$style;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;

public class ScreenButtonTile extends QSTileImpl<QSTile.BooleanState> {
    protected boolean mHasButtons;
    private final ContentObserver mScreenButtonStateObserver;

    public Intent getLongClickIntent() {
        return null;
    }

    public int getMetricsCategory() {
        return -1;
    }

    /* access modifiers changed from: protected */
    public void handleLongClick() {
    }

    public ScreenButtonTile(QSHost qSHost) {
        super(qSHost);
        this.mHasButtons = true;
        this.mScreenButtonStateObserver = new ContentObserver(this.mHandler) {
            public void onChange(boolean z) {
                ScreenButtonTile.this.refreshState();
            }
        };
        this.mHasButtons = ViewConfiguration.get(this.mContext).hasPermanentMenuKey();
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
            this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("screen_buttons_state"), false, this.mScreenButtonStateObserver, -1);
            this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("force_fsg_nav_bar"), false, this.mScreenButtonStateObserver);
            return;
        }
        this.mContext.getContentResolver().unregisterContentObserver(this.mScreenButtonStateObserver);
    }

    public boolean isAvailable() {
        return this.mHasButtons;
    }

    /* access modifiers changed from: protected */
    public void handleClick() {
        ContentResolver contentResolver = this.mContext.getContentResolver();
        boolean z = Settings.Secure.getIntForUser(contentResolver, "screen_buttons_state", 0, -2) != 0;
        int intForUser = Settings.Secure.getIntForUser(contentResolver, "screen_buttons_has_been_disabled", 0, -2);
        if (intForUser == 0) {
            Settings.Secure.putIntForUser(contentResolver, "screen_buttons_has_been_disabled", 1, -2);
        }
        Settings.Secure.putIntForUser(contentResolver, "screen_buttons_state", z ^ true ? 1 : 0, -2);
        this.mUiHandler.post(new ClickRunnable(intForUser, !z));
    }

    public CharSequence getTileLabel() {
        return this.mContext.getString(C0021R$string.quick_settings_screenbutton_label);
    }

    /* access modifiers changed from: protected */
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        boolean z = false;
        if (MiuiSettings.Global.getBoolean(this.mContext.getContentResolver(), "force_fsg_nav_bar")) {
            booleanState.value = false;
            booleanState.state = 0;
            booleanState.icon = QSTileImpl.ResourceIcon.get(C0013R$drawable.ic_qs_screen_button_unavailable);
        } else {
            if (Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "screen_buttons_state", 0, -2) != 0) {
                z = true;
            }
            booleanState.value = z;
            if (z) {
                booleanState.state = 2;
                booleanState.icon = QSTileImpl.ResourceIcon.get(C0013R$drawable.ic_qs_screen_button_enabled);
            } else {
                booleanState.state = 1;
                booleanState.icon = QSTileImpl.ResourceIcon.get(C0013R$drawable.ic_qs_screen_button_disabled);
            }
        }
        booleanState.label = this.mContext.getString(C0021R$string.quick_settings_screenbutton_label);
        StringBuilder sb = new StringBuilder();
        sb.append(booleanState.label);
        sb.append(",");
        sb.append(this.mContext.getString(booleanState.value ? C0021R$string.switch_bar_on : C0021R$string.switch_bar_off));
        booleanState.contentDescription = sb.toString();
        booleanState.expandedAccessibilityClassName = Switch.class.getName();
    }

    protected class ClickRunnable implements Runnable {
        private int value;

        ClickRunnable(int i, boolean z) {
            this.value = i;
        }

        public void run() {
            if (this.value == 0) {
                AlertDialog create = new AlertDialog.Builder(ScreenButtonTile.this.mContext, C0022R$style.Theme_Dialog_Alert).setMessage(286195789).setPositiveButton(17039370, (DialogInterface.OnClickListener) null).create();
                create.getWindow().setType(2010);
                create.show();
            }
        }
    }
}
