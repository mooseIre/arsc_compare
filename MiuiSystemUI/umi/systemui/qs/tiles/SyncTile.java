package com.android.systemui.qs.tiles;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SyncStatusObserver;
import android.os.UserHandle;
import android.util.Log;
import android.widget.Switch;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0021R$string;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import java.lang.reflect.Method;

public class SyncTile extends QSTileImpl<QSTile.BooleanState> {
    private int mCurrentUserId = 0;
    private Object mStatusChangeListenerHandle;
    private SyncStatusObserver mSyncStatusObserver = new SyncStatusObserver() {
        /* class com.android.systemui.qs.tiles.$$Lambda$SyncTile$Mq7aSAdFJyMzn6kU6KbMoUjE6sM */

        public final void onStatusChanged(int i) {
            SyncTile.this.lambda$new$0$SyncTile(i);
        }
    };

    @Override // com.android.systemui.plugins.qs.QSTile, com.android.systemui.qs.tileimpl.QSTileImpl
    public int getMetricsCategory() {
        return -1;
    }

    @Override // com.android.systemui.plugins.qs.QSTile, com.android.systemui.qs.tileimpl.QSTileImpl
    public boolean isAvailable() {
        return false;
    }

    public SyncTile(QSHost qSHost) {
        super(qSHost);
        this.mCurrentUserId = "com.android.systemui".equals(this.mContext.getApplicationInfo().packageName) ? ActivityManager.getCurrentUser() : UserHandle.myUserId();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleDestroy() {
        super.handleDestroy();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleSetListening(boolean z) {
        if (z) {
            this.mStatusChangeListenerHandle = ContentResolver.addStatusChangeListener(Integer.MAX_VALUE, this.mSyncStatusObserver);
        } else {
            ContentResolver.removeStatusChangeListener(this.mStatusChangeListenerHandle);
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleUserSwitch(int i) {
        this.mCurrentUserId = i;
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public Intent getLongClickIntent() {
        return longClickVibrateIntent();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleClick() {
        try {
            Method method = ContentResolver.class.getMethod("setMasterSyncAutomaticallyAsUser", Boolean.TYPE, Integer.TYPE);
            Object[] objArr = new Object[2];
            objArr[0] = Boolean.valueOf(!isSyncOn());
            objArr[1] = Integer.valueOf(this.mCurrentUserId);
            method.invoke(null, objArr);
        } catch (Exception unused) {
            Log.i(this.TAG, "setMasterSyncAutomaticallyAsUser not found.");
            ContentResolver.setMasterSyncAutomatically(!ContentResolver.getMasterSyncAutomatically());
        }
    }

    private boolean isSyncOn() {
        try {
            return ((Boolean) ContentResolver.class.getMethod("getMasterSyncAutomaticallyAsUser", Integer.TYPE).invoke(null, Integer.valueOf(this.mCurrentUserId))).booleanValue();
        } catch (Exception unused) {
            Log.i(this.TAG, "getMasterSyncAutomaticallyAsUser not found.");
            return ContentResolver.getMasterSyncAutomatically();
        }
    }

    @Override // com.android.systemui.plugins.qs.QSTile
    public CharSequence getTileLabel() {
        return this.mContext.getString(C0021R$string.quick_settings_sync_label);
    }

    /* access modifiers changed from: protected */
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        booleanState.value = isSyncOn();
        booleanState.label = this.mContext.getString(C0021R$string.quick_settings_sync_label);
        if (booleanState.value) {
            booleanState.state = 2;
            booleanState.icon = QSTileImpl.ResourceIcon.get(C0013R$drawable.ic_qs_sync_on);
        } else {
            booleanState.state = 1;
            booleanState.icon = QSTileImpl.ResourceIcon.get(C0013R$drawable.ic_qs_sync_off);
        }
        StringBuilder sb = new StringBuilder();
        sb.append((Object) booleanState.label);
        sb.append(",");
        sb.append(this.mContext.getString(booleanState.value ? C0021R$string.switch_bar_on : C0021R$string.switch_bar_off));
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

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$0 */
    public /* synthetic */ void lambda$new$0$SyncTile(int i) {
        refreshState();
    }
}
