package com.android.systemui.qs.tiles;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SyncStatusObserver;
import android.os.UserHandle;
import android.util.Log;
import android.widget.Switch;
import com.android.systemui.plugins.R;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.statusbar.Icons;
import java.lang.reflect.Method;

public class SyncTile extends QSTileImpl<QSTile.BooleanState> {
    private int mCurrentUserId = 0;
    private Object mStatusChangeListenerHandle;
    private SyncStatusObserver mSyncStatusObserver = new SyncStatusObserver() {
        public void onStatusChanged(int i) {
            SyncTile.this.refreshState();
        }
    };

    public int getMetricsCategory() {
        return -1;
    }

    public SyncTile(QSHost qSHost) {
        super(qSHost);
        this.mCurrentUserId = "com.android.systemui".equals(this.mContext.getApplicationInfo().packageName) ? ActivityManager.getCurrentUser() : UserHandle.myUserId();
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
            Class<ContentResolver> cls = ContentResolver.class;
            try {
                this.mStatusChangeListenerHandle = cls.getMethod("addStatusChangeListenerAsUser", new Class[]{Integer.TYPE, SyncStatusObserver.class, Integer.TYPE}).invoke((Object) null, new Object[]{Integer.MAX_VALUE, this.mSyncStatusObserver, Integer.valueOf(this.mCurrentUserId)});
            } catch (Exception unused) {
                this.mStatusChangeListenerHandle = ContentResolver.addStatusChangeListener(Integer.MAX_VALUE, this.mSyncStatusObserver);
            }
        } else {
            ContentResolver.removeStatusChangeListener(this.mStatusChangeListenerHandle);
        }
    }

    /* access modifiers changed from: protected */
    public void handleUserSwitch(int i) {
        this.mCurrentUserId = i;
    }

    public Intent getLongClickIntent() {
        return longClickVibrateIntent();
    }

    /* access modifiers changed from: protected */
    public void handleClick() {
        try {
            Method method = ContentResolver.class.getMethod("setMasterSyncAutomaticallyAsUser", new Class[]{Boolean.TYPE, Integer.TYPE});
            Object[] objArr = new Object[2];
            objArr[0] = Boolean.valueOf(!isSyncOn());
            objArr[1] = Integer.valueOf(this.mCurrentUserId);
            method.invoke((Object) null, objArr);
        } catch (Exception unused) {
            Log.i(this.TAG, "setMasterSyncAutomaticallyAsUser not found.");
            ContentResolver.setMasterSyncAutomatically(!ContentResolver.getMasterSyncAutomatically());
        }
    }

    private boolean isSyncOn() {
        Class<ContentResolver> cls = ContentResolver.class;
        try {
            return ((Boolean) cls.getMethod("getMasterSyncAutomaticallyAsUser", new Class[]{Integer.TYPE}).invoke((Object) null, new Object[]{Integer.valueOf(this.mCurrentUserId)})).booleanValue();
        } catch (Exception unused) {
            Log.i(this.TAG, "getMasterSyncAutomaticallyAsUser not found.");
            return ContentResolver.getMasterSyncAutomatically();
        }
    }

    public CharSequence getTileLabel() {
        return this.mContext.getString(R.string.quick_settings_sync_label);
    }

    /* access modifiers changed from: protected */
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        booleanState.value = isSyncOn();
        booleanState.label = this.mContext.getString(R.string.quick_settings_sync_label);
        if (booleanState.value) {
            booleanState.state = 2;
            booleanState.icon = QSTileImpl.ResourceIcon.get(Icons.getQSIcons(Integer.valueOf(R.drawable.ic_qs_sync_on), this.mInControlCenter));
        } else {
            booleanState.state = 1;
            booleanState.icon = QSTileImpl.ResourceIcon.get(Icons.getQSIcons(Integer.valueOf(R.drawable.ic_qs_sync_off), this.mInControlCenter));
        }
        StringBuilder sb = new StringBuilder();
        sb.append(booleanState.label);
        sb.append(",");
        sb.append(this.mContext.getString(booleanState.value ? R.string.switch_bar_on : R.string.switch_bar_off));
        booleanState.contentDescription = sb.toString();
        booleanState.expandedAccessibilityClassName = Switch.class.getName();
    }

    private Intent longClickVibrateIntent() {
        ComponentName unflattenFromString = ComponentName.unflattenFromString("com.android.settings/com.android.settings.Settings$ManageAccountsSettingsActivity");
        if (unflattenFromString == null) {
            return null;
        }
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setComponent(unflattenFromString);
        intent.setFlags(335544320);
        return intent;
    }
}
