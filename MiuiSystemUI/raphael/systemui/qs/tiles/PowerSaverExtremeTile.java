package com.android.systemui.qs.tiles;

import android.app.ActivityManager;
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
import miui.securityspace.CrossUserUtils;
import miui.util.FeatureParser;

public class PowerSaverExtremeTile extends QSTileImpl<QSTile.BooleanState> {
    private static final boolean SUPPORT_EXTREME_BATTERY_SAVER = FeatureParser.getBoolean("support_extreme_battery_saver", false);
    private final ContentObserver mObserver = new ContentObserver(this.mHandler) {
        /* class com.android.systemui.qs.tiles.PowerSaverExtremeTile.AnonymousClass1 */

        public void onChange(boolean z) {
            PowerSaverExtremeTile.this.refreshState();
        }
    };
    private ContentResolver mResolver = this.mContext.getContentResolver();

    @Override // com.android.systemui.plugins.qs.QSTile, com.android.systemui.qs.tileimpl.QSTileImpl
    public int getMetricsCategory() {
        return -1;
    }

    public PowerSaverExtremeTile(QSHost qSHost) {
        super(qSHost);
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleSetListening(boolean z) {
        if (z) {
            this.mResolver.registerContentObserver(Settings.Secure.getUriFor("EXTREME_POWER_MODE_ENABLE"), false, this.mObserver, -1);
        } else {
            this.mResolver.unregisterContentObserver(this.mObserver);
        }
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public Intent getLongClickIntent() {
        Intent intent = new Intent("miui.intent.action.EXTREME_POWER_ENTRY_ACTIVITY");
        intent.setFlags(335544320);
        return intent;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleClick() {
        boolean z = false;
        if (Settings.Secure.getIntForUser(this.mResolver, "EXTREME_POWER_MODE_ENABLE", 0, -2) != 0) {
            z = true;
        }
        Bundle bundle = new Bundle();
        bundle.putString("SOURCE", "systemui");
        bundle.putBoolean("EXTREME_POWER_SAVE_MODE_OPEN", !z);
        this.mResolver.call(maybeAddUserId(Uri.parse("content://com.miui.powerkeeper.configure"), ActivityManager.getCurrentUser()), "changeExtremePowerMode", (String) null, bundle);
    }

    @Override // com.android.systemui.plugins.qs.QSTile
    public CharSequence getTileLabel() {
        return this.mContext.getString(C0021R$string.quick_settings_extreme_batterysaver_label);
    }

    /* access modifiers changed from: protected */
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        boolean z = false;
        if (Settings.Secure.getIntForUser(this.mResolver, "EXTREME_POWER_MODE_ENABLE", 0, -2) != 0) {
            z = true;
        }
        booleanState.value = z;
        booleanState.label = this.mContext.getString(C0021R$string.quick_settings_extreme_batterysaver_label);
        if (booleanState.value) {
            booleanState.state = 2;
            booleanState.icon = QSTileImpl.ResourceIcon.get(C0013R$drawable.ic_qs_extreme_battery_saver_on);
        } else {
            booleanState.state = 1;
            booleanState.icon = QSTileImpl.ResourceIcon.get(C0013R$drawable.ic_qs_extreme_battery_saver_off);
        }
        StringBuilder sb = new StringBuilder();
        sb.append((Object) booleanState.label);
        sb.append(",");
        sb.append(this.mContext.getString(booleanState.value ? C0021R$string.switch_bar_on : C0021R$string.switch_bar_off));
        booleanState.contentDescription = sb.toString();
        booleanState.expandedAccessibilityClassName = Switch.class.getName();
    }

    @Override // com.android.systemui.plugins.qs.QSTile, com.android.systemui.qs.tileimpl.QSTileImpl
    public boolean isAvailable() {
        return Build.IS_INTERNATIONAL_BUILD && SUPPORT_EXTREME_BATTERY_SAVER && !Build.IS_TABLET && CrossUserUtils.getCurrentUserId() == 0;
    }

    private Uri maybeAddUserId(Uri uri, int i) {
        if (uri == null) {
            return null;
        }
        if (i == -2 || !"content".equals(uri.getScheme()) || uriHasUserId(uri)) {
            return uri;
        }
        Uri.Builder buildUpon = uri.buildUpon();
        buildUpon.encodedAuthority(Integer.toString(i) + "@" + uri.getEncodedAuthority());
        return buildUpon.build();
    }

    private boolean uriHasUserId(Uri uri) {
        if (uri == null) {
            return false;
        }
        return !TextUtils.isEmpty(uri.getUserInfo());
    }
}
