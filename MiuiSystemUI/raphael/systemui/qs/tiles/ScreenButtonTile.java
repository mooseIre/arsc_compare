package com.android.systemui.qs.tiles;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContextCompat;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.RemoteException;
import android.provider.MiuiSettings;
import android.provider.Settings;
import android.view.IWindowManager;
import android.view.IWindowManagerCompat;
import android.view.WindowManagerGlobal;
import android.widget.Switch;
import com.android.systemui.Util;
import com.android.systemui.plugins.R;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;

public class ScreenButtonTile extends QSTileImpl<QSTile.BooleanState> {
    protected boolean mHasButtons = true;
    private final ContentObserver mScreenButtonStateObserver = new ContentObserver(this.mHandler) {
        public void onChange(boolean z) {
            ScreenButtonTile.this.refreshState();
        }
    };
    protected IWindowManager mWindowManagerService = WindowManagerGlobal.getWindowManagerService();

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
        boolean z = true;
        try {
            if (IWindowManagerCompat.hasNavigationBar(this.mWindowManagerService, ContextCompat.getDisplayId(this.mContext))) {
                z = false;
            }
            this.mHasButtons = z;
        } catch (RemoteException unused) {
        }
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
        return this.mContext.getString(R.string.quick_settings_screenbutton_label);
    }

    /* access modifiers changed from: protected */
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        boolean z = false;
        if (MiuiSettings.Global.getBoolean(this.mContext.getContentResolver(), "force_fsg_nav_bar")) {
            booleanState.value = false;
            booleanState.state = 0;
            booleanState.icon = QSTileImpl.ResourceIcon.get(R.drawable.ic_qs_screen_button_unavailable);
        } else {
            if (Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "screen_buttons_state", 0, -2) != 0) {
                z = true;
            }
            booleanState.value = z;
            if (booleanState.value) {
                booleanState.state = 2;
                booleanState.icon = QSTileImpl.ResourceIcon.get(R.drawable.ic_qs_screen_button_enabled);
            } else {
                booleanState.state = 1;
                booleanState.icon = QSTileImpl.ResourceIcon.get(R.drawable.ic_qs_screen_button_disabled);
            }
        }
        booleanState.label = this.mContext.getString(R.string.quick_settings_screenbutton_label);
        StringBuilder sb = new StringBuilder();
        sb.append(booleanState.label);
        sb.append(",");
        sb.append(this.mContext.getString(booleanState.value ? R.string.switch_bar_on : R.string.switch_bar_off));
        booleanState.contentDescription = sb.toString();
        booleanState.expandedAccessibilityClassName = Switch.class.getName();
    }

    protected class ClickRunnable implements Runnable {
        private boolean disabled;
        private int value;

        ClickRunnable(int i, boolean z) {
            this.value = i;
            this.disabled = z;
        }

        public void run() {
            if (this.value == 0) {
                AlertDialog create = new AlertDialog.Builder(ScreenButtonTile.this.mContext, R.style.Theme_Dialog_Alert).setMessage(286130245).setPositiveButton(17039370, (DialogInterface.OnClickListener) null).create();
                create.getWindow().setType(2010);
                create.getWindow().addPrivateFlags(16);
                create.show();
                return;
            }
            Util.showSystemOverlayToast(ScreenButtonTile.this.mContext, this.disabled ? R.string.auto_disable_screenbuttons_disable_toast_text : R.string.auto_disable_screenbuttons_enable_toast_text, 0);
        }
    }
}
