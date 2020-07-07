package com.android.systemui.qs.tiles;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;
import android.widget.Switch;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.R;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.statusbar.Icons;
import com.android.systemui.statusbar.policy.SilentModeObserverController;
import miui.util.AudioManagerHelper;

public class VibrateTile extends QSTileImpl<QSTile.BooleanState> implements SilentModeObserverController.SilentModeListener {
    private final ContentResolver mResolver = this.mContext.getContentResolver();
    private final SilentModeObserverController mSilentModeObserverController = ((SilentModeObserverController) Dependency.get(SilentModeObserverController.class));
    private ContentObserver mVibrateEnableObserver = new ContentObserver(this.mHandler) {
        public void onChange(boolean z) {
            VibrateTile.this.refreshState();
        }
    };

    public int getMetricsCategory() {
        return -1;
    }

    public VibrateTile(QSHost qSHost) {
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
            this.mResolver.registerContentObserver(Settings.System.getUriFor("vibrate_in_silent"), false, this.mVibrateEnableObserver, -1);
            this.mResolver.registerContentObserver(Settings.System.getUriFor("vibrate_in_normal"), false, this.mVibrateEnableObserver, -1);
            this.mSilentModeObserverController.addCallback(this);
            return;
        }
        this.mResolver.unregisterContentObserver(this.mVibrateEnableObserver);
        this.mSilentModeObserverController.removeCallback(this);
    }

    public Intent getLongClickIntent() {
        return longClickVibrateIntent();
    }

    /* access modifiers changed from: protected */
    public void handleClick() {
        String str = this.TAG;
        StringBuilder sb = new StringBuilder();
        sb.append("handleClick: from: ");
        sb.append(((QSTile.BooleanState) this.mState).value);
        sb.append(", to: ");
        sb.append(!((QSTile.BooleanState) this.mState).value);
        Log.d(str, sb.toString());
        AudioManagerHelper.toggleVibrateSetting(this.mContext);
    }

    public CharSequence getTileLabel() {
        return this.mContext.getString(R.string.quick_settings_vibrate_label);
    }

    /* access modifiers changed from: protected */
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        booleanState.value = AudioManagerHelper.isVibrateEnabled(this.mContext);
        booleanState.label = this.mContext.getString(R.string.quick_settings_vibrate_label);
        if (booleanState.value) {
            booleanState.state = 2;
            booleanState.icon = QSTileImpl.ResourceIcon.get(Icons.getQSIcons(Integer.valueOf(R.drawable.ic_qs_vibrate_on), this.mInControlCenter));
        } else {
            booleanState.state = 1;
            booleanState.icon = QSTileImpl.ResourceIcon.get(Icons.getQSIcons(Integer.valueOf(R.drawable.ic_qs_vibrate_off), this.mInControlCenter));
        }
        StringBuilder sb = new StringBuilder();
        sb.append(booleanState.label);
        sb.append(",");
        sb.append(this.mContext.getString(booleanState.value ? R.string.switch_bar_on : R.string.switch_bar_off));
        booleanState.contentDescription = sb.toString();
        booleanState.expandedAccessibilityClassName = Switch.class.getName();
    }

    public boolean isAvailable() {
        return ((Vibrator) this.mContext.getSystemService("vibrator")).hasVibrator();
    }

    private Intent longClickVibrateIntent() {
        ComponentName unflattenFromString = ComponentName.unflattenFromString("com.android.settings/com.android.settings.Settings$SoundSettingsActivity");
        if (unflattenFromString == null) {
            return null;
        }
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setComponent(unflattenFromString);
        intent.setFlags(335544320);
        return intent;
    }

    public void onSilentModeChanged(boolean z) {
        refreshState();
    }
}
