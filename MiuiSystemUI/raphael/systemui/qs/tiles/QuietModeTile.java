package com.android.systemui.qs.tiles;

import android.content.Intent;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.provider.MiuiSettings;
import android.provider.Settings;
import android.widget.Switch;
import com.android.systemui.Dependency;
import com.android.systemui.Util;
import com.android.systemui.miui.volume.VolumeUtil;
import com.android.systemui.plugins.R;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.statusbar.Icons;
import com.android.systemui.statusbar.policy.SilentModeObserverController;

public class QuietModeTile extends QSTileImpl<QSTile.BooleanState> implements SilentModeObserverController.SilentModeListener {
    private ContentObserver mContentObserver = new ContentObserver(this.mHandler) {
        public void onChange(boolean z) {
            QuietModeTile.this.refreshState();
        }

        public void onChange(boolean z, Uri uri) {
            QuietModeTile.this.refreshState();
        }
    };
    private final SilentModeObserverController mSilentModeObserverController = ((SilentModeObserverController) Dependency.get(SilentModeObserverController.class));

    public int getMetricsCategory() {
        return -1;
    }

    public QuietModeTile(QSHost qSHost) {
        super(qSHost);
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
            this.mSilentModeObserverController.addCallback(this);
            this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("mute_music_at_silent"), false, this.mContentObserver, -1);
            return;
        }
        this.mContext.getContentResolver().unregisterContentObserver(this.mContentObserver);
        this.mSilentModeObserverController.removeCallback(this);
    }

    public Intent getLongClickIntent() {
        return Util.getSilentModeIntent();
    }

    /* access modifiers changed from: protected */
    public void handleClick() {
        int i = 1;
        if (MiuiSettings.SilenceMode.isSupported) {
            if (VolumeUtil.getZenMode(this.mContext) == 1) {
                i = 0;
            }
            VolumeUtil.setSilenceMode(this.mContext, i, (Uri) null);
            return;
        }
        MiuiSettings.AntiSpam.setQuietMode(this.mContext, !((QSTile.BooleanState) this.mState).value);
    }

    public CharSequence getTileLabel() {
        return this.mContext.getString(R.string.quick_settings_quietmode_label);
    }

    /* access modifiers changed from: protected */
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        boolean z;
        int zenMode = VolumeUtil.getZenMode(this.mContext);
        if (MiuiSettings.SilenceMode.isSupported) {
            z = zenMode == 1;
        } else {
            z = MiuiSettings.AntiSpam.isQuietModeEnable(this.mContext);
        }
        booleanState.value = z;
        booleanState.label = this.mContext.getString(R.string.quick_settings_quietmode_label);
        if (booleanState.value) {
            booleanState.state = 2;
            booleanState.icon = QSTileImpl.ResourceIcon.get(Icons.getQSIcons(Integer.valueOf(R.drawable.ic_qs_dnd_on), this.mInControlCenter));
        } else {
            booleanState.state = 1;
            booleanState.icon = QSTileImpl.ResourceIcon.get(Icons.getQSIcons(Integer.valueOf(R.drawable.ic_qs_dnd_off), this.mInControlCenter));
        }
        StringBuilder sb = new StringBuilder();
        sb.append(booleanState.label);
        sb.append(",");
        sb.append(this.mContext.getString(booleanState.value ? R.string.switch_bar_on : R.string.switch_bar_off));
        booleanState.contentDescription = sb.toString();
        booleanState.expandedAccessibilityClassName = Switch.class.getName();
    }

    public boolean isAvailable() {
        return ((ConnectivityManager) this.mContext.getSystemService("connectivity")).isNetworkSupported(0);
    }

    public void onSilentModeChanged(boolean z) {
        refreshState();
    }
}
