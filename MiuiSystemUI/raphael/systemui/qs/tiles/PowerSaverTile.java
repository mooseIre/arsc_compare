package com.android.systemui.qs.tiles;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.Switch;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0021R$string;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import miui.os.Build;

public class PowerSaverTile extends QSTileImpl<QSTile.BooleanState> {
    private final ContentObserver mBatterySaverObserver = new ContentObserver(this.mHandler) {
        /* class com.android.systemui.qs.tiles.PowerSaverTile.AnonymousClass1 */

        public void onChange(boolean z) {
            PowerSaverTile.this.refreshState();
        }
    };
    private ContentResolver mResolver = this.mContext.getContentResolver();

    @Override // com.android.systemui.plugins.qs.QSTile, com.android.systemui.qs.tileimpl.QSTileImpl
    public int getMetricsCategory() {
        return -1;
    }

    public PowerSaverTile(QSHost qSHost) {
        super(qSHost);
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleSetListening(boolean z) {
        if (z) {
            this.mResolver.registerContentObserver(Settings.System.getUriFor("POWER_SAVE_MODE_OPEN"), false, this.mBatterySaverObserver, -1);
        } else {
            this.mResolver.unregisterContentObserver(this.mBatterySaverObserver);
        }
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public Intent getLongClickIntent() {
        return longClickBatterySaverIntent();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleClick() {
        boolean z = false;
        if (Settings.System.getIntForUser(this.mResolver, "POWER_SAVE_MODE_OPEN", 0, -2) != 0) {
            z = true;
        }
        boolean z2 = !z;
        Bundle bundle = new Bundle();
        bundle.putBoolean("POWER_SAVE_MODE_OPEN", z2);
        this.mResolver.call(maybeAddUserId(Uri.parse("content://com.miui.powercenter.powersaver"), ActivityManager.getCurrentUser()), "changePowerMode", (String) null, bundle);
    }

    @Override // com.android.systemui.plugins.qs.QSTile
    public CharSequence getTileLabel() {
        return this.mContext.getString(C0021R$string.quick_settings_batterysaver_label);
    }

    /* access modifiers changed from: protected */
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        boolean z = false;
        if (Settings.System.getIntForUser(this.mResolver, "POWER_SAVE_MODE_OPEN", 0, -2) != 0) {
            z = true;
        }
        booleanState.value = z;
        booleanState.label = this.mContext.getString(C0021R$string.quick_settings_batterysaver_label);
        if (booleanState.value) {
            booleanState.state = 2;
            booleanState.icon = QSTileImpl.ResourceIcon.get(C0013R$drawable.ic_qs_battery_saver_on);
        } else {
            booleanState.state = 1;
            booleanState.icon = QSTileImpl.ResourceIcon.get(C0013R$drawable.ic_qs_battery_saver_off);
        }
        StringBuilder sb = new StringBuilder();
        sb.append((Object) booleanState.label);
        sb.append(",");
        sb.append(this.mContext.getString(booleanState.value ? C0021R$string.switch_bar_on : C0021R$string.switch_bar_off));
        booleanState.contentDescription = sb.toString();
        booleanState.expandedAccessibilityClassName = Switch.class.getName();
        booleanState.activeBgColor = 1;
    }

    @Override // com.android.systemui.plugins.qs.QSTile, com.android.systemui.qs.tileimpl.QSTileImpl
    public boolean isAvailable() {
        return !Build.IS_TABLET;
    }

    private Intent longClickBatterySaverIntent() {
        ComponentName unflattenFromString = ComponentName.unflattenFromString("com.miui.securitycenter/com.miui.powercenter.savemode.PowerSaveActivity");
        if (unflattenFromString == null) {
            return null;
        }
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setComponent(unflattenFromString);
        intent.setFlags(335544320);
        return intent;
    }

    private Uri maybeAddUserId(Uri uri, int i) {
        if (uri == null) {
            return null;
        }
        if (i == -2 || !"content".equals(uri.getScheme()) || uriHasUserId(uri)) {
            return uri;
        }
        Uri.Builder buildUpon = uri.buildUpon();
        buildUpon.encodedAuthority("" + i + "@" + uri.getEncodedAuthority());
        return buildUpon.build();
    }

    private boolean uriHasUserId(Uri uri) {
        if (uri == null) {
            return false;
        }
        return !TextUtils.isEmpty(uri.getUserInfo());
    }
}
