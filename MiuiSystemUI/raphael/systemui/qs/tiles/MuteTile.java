package com.android.systemui.qs.tiles;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.UserHandle;
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
import miui.util.AudioManagerHelper;

public class MuteTile extends QSTileImpl<QSTile.BooleanState> implements SilentModeObserverController.SilentModeListener {
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.media.RINGER_MODE_CHANGED".equals(intent.getAction())) {
                MuteTile.this.refreshState();
            }
        }
    };
    private ContentObserver mContentObserver = new ContentObserver(this.mHandler) {
        public void onChange(boolean z) {
            MuteTile.this.refreshState();
        }

        public void onChange(boolean z, Uri uri) {
            MuteTile.this.refreshState();
        }
    };
    private final SilentModeObserverController mSilentModeObserverController = ((SilentModeObserverController) Dependency.get(SilentModeObserverController.class));

    public int getMetricsCategory() {
        return -1;
    }

    public MuteTile(QSHost qSHost) {
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
            UserHandle userHandle = UserHandle.ALL;
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.media.RINGER_MODE_CHANGED");
            this.mContext.registerReceiverAsUser(this.mBroadcastReceiver, userHandle, intentFilter, (String) null, (Handler) null);
            this.mSilentModeObserverController.addCallback(this);
            this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("mute_music_at_silent"), false, this.mContentObserver, -1);
            return;
        }
        this.mContext.getContentResolver().unregisterContentObserver(this.mContentObserver);
        this.mSilentModeObserverController.removeCallback(this);
        this.mContext.unregisterReceiver(this.mBroadcastReceiver);
    }

    public Intent getLongClickIntent() {
        return Util.getSilentModeIntent();
    }

    /* access modifiers changed from: protected */
    public void handleClick() {
        Object obj;
        int i = 4;
        if (MiuiSettings.SilenceMode.isSupported) {
            boolean z = VolumeUtil.getZenMode(this.mContext) != 4;
            if (z) {
                obj = null;
            } else {
                obj = QSTileImpl.ARG_SHOW_TRANSIENT_ENABLING;
            }
            refreshState(obj);
            Context context = this.mContext;
            if (!z) {
                i = 0;
            }
            VolumeUtil.setSilenceMode(context, i, (Uri) null);
            return;
        }
        AudioManagerHelper.toggleSilent(this.mContext, 4);
    }

    public CharSequence getTileLabel() {
        return this.mContext.getString(R.string.quick_settings_mute_label);
    }

    /* access modifiers changed from: protected */
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        boolean z = false;
        boolean z2 = obj == QSTileImpl.ARG_SHOW_TRANSIENT_ENABLING;
        int zenMode = VolumeUtil.getZenMode(this.mContext);
        if (!z2 && !MiuiSettings.SilenceMode.isSupported) {
            z = AudioManagerHelper.isSilentEnabled(this.mContext);
        } else if (zenMode == 4) {
            z = true;
        }
        booleanState.value = z;
        booleanState.label = this.mContext.getString(R.string.quick_settings_mute_label);
        if (booleanState.value) {
            booleanState.state = 2;
            booleanState.icon = QSTileImpl.ResourceIcon.get(Icons.getQSIcons(Integer.valueOf(R.drawable.ic_qs_mute_on), this.mInControlCenter));
        } else {
            booleanState.state = 1;
            booleanState.icon = QSTileImpl.ResourceIcon.get(Icons.getQSIcons(Integer.valueOf(R.drawable.ic_qs_mute_off), this.mInControlCenter));
        }
        StringBuilder sb = new StringBuilder();
        sb.append(booleanState.label);
        sb.append(",");
        sb.append(this.mContext.getString(booleanState.value ? R.string.switch_bar_on : R.string.switch_bar_off));
        booleanState.contentDescription = sb.toString();
        booleanState.expandedAccessibilityClassName = Switch.class.getName();
    }

    public void onSilentModeChanged(boolean z) {
        refreshState();
    }
}
