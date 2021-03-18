package com.android.systemui.qs.tiles;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.Switch;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0021R$string;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import miui.util.FeatureParser;

public class PowerModeTile extends QSTileImpl<QSTile.BooleanState> {
    private final ContentObserver mPowerModeObserver = new ContentObserver(this.mHandler) {
        /* class com.android.systemui.qs.tiles.PowerModeTile.AnonymousClass1 */

        public void onChange(boolean z) {
            PowerModeTile.this.refreshState();
        }
    };
    private ContentResolver mResolver = this.mContext.getContentResolver();

    @Override // com.android.systemui.plugins.qs.QSTile, com.android.systemui.qs.tileimpl.QSTileImpl
    public int getMetricsCategory() {
        return -1;
    }

    public PowerModeTile(QSHost qSHost) {
        super(qSHost);
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
            this.mResolver.registerContentObserver(Settings.System.getUriFor("power_mode"), false, this.mPowerModeObserver, -1);
        } else {
            this.mResolver.unregisterContentObserver(this.mPowerModeObserver);
        }
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public Intent getLongClickIntent() {
        return longClickPowerModeIntent();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleClick() {
        String stringForUser = Settings.System.getStringForUser(this.mContext.getContentResolver(), "power_mode", -2);
        String str = "middle";
        if (TextUtils.isEmpty(stringForUser)) {
            stringForUser = str;
        }
        if (!"high".equals(stringForUser)) {
            str = "high";
        }
        SystemProperties.set("persist.sys.aries.power_profile", str);
        Settings.System.putStringForUser(this.mResolver, "power_mode", str, ActivityManager.getCurrentUser());
        this.mContext.sendBroadcastAsUser(new Intent("miui.intent.action.POWER_MODE_CHANGE"), UserHandle.CURRENT);
    }

    @Override // com.android.systemui.plugins.qs.QSTile
    public CharSequence getTileLabel() {
        return this.mContext.getString(C0021R$string.quick_settings_powermode_label);
    }

    /* access modifiers changed from: protected */
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        String stringForUser = Settings.System.getStringForUser(this.mContext.getContentResolver(), "power_mode", -2);
        if (TextUtils.isEmpty(stringForUser)) {
            stringForUser = "middle";
        }
        booleanState.value = "high".equals(stringForUser);
        booleanState.label = this.mContext.getString(C0021R$string.quick_settings_powermode_label);
        if (booleanState.value) {
            booleanState.state = 2;
            booleanState.icon = QSTileImpl.ResourceIcon.get(C0013R$drawable.ic_qs_power_high_on);
        } else {
            booleanState.state = 1;
            booleanState.icon = QSTileImpl.ResourceIcon.get(C0013R$drawable.ic_qs_power_high_off);
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
        return FeatureParser.getBoolean("support_power_mode", false);
    }

    private Intent longClickPowerModeIntent() {
        ComponentName unflattenFromString = ComponentName.unflattenFromString("com.android.settings/com.android.settings.Settings$BatterySettingsActivity");
        if (unflattenFromString == null) {
            return null;
        }
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setComponent(unflattenFromString);
        intent.setFlags(335544320);
        return intent;
    }
}
