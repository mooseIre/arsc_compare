package com.android.systemui.qs.tiles;

import android.content.ComponentName;
import android.content.Intent;
import android.util.Log;
import android.widget.Switch;
import androidx.appcompat.R$styleable;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0021R$string;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.GlobalSetting;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.statusbar.policy.HotspotController;
import miui.securityspace.CrossUserUtils;

public class MiuiHotspotTile extends QSTileImpl<QSTile.BooleanState> {
    static final Intent TETHER_SETTINGS = new Intent().setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings$TetherSettingsActivity"));
    private final GlobalSetting mAirplaneMode;
    private final Callback mHotspotCallback = new Callback();
    private final HotspotController mHotspotController;
    private boolean mIsAirplaneMode;
    private boolean mListening;

    @Override // com.android.systemui.plugins.qs.QSTile, com.android.systemui.qs.tileimpl.QSTileImpl
    public int getMetricsCategory() {
        return R$styleable.AppCompatTheme_windowFixedHeightMajor;
    }

    public MiuiHotspotTile(QSHost qSHost, HotspotController hotspotController) {
        super(qSHost);
        this.mHotspotController = hotspotController;
        hotspotController.observe(getLifecycle(), this.mHotspotCallback);
        this.mAirplaneMode = new GlobalSetting(this.mContext, this.mHandler, "airplane_mode_on") {
            /* class com.android.systemui.qs.tiles.MiuiHotspotTile.AnonymousClass1 */

            /* access modifiers changed from: protected */
            @Override // com.android.systemui.qs.GlobalSetting
            public void handleValueChanged(int i) {
                MiuiHotspotTile.this.refreshState();
            }
        };
    }

    @Override // com.android.systemui.plugins.qs.QSTile, com.android.systemui.qs.tileimpl.QSTileImpl
    public boolean isAvailable() {
        return this.mHotspotController.isHotspotSupported() && CrossUserUtils.getCurrentUserId() == 0;
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
        if (this.mListening != z) {
            this.mListening = z;
            this.mAirplaneMode.setListening(z);
        }
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public Intent getLongClickIntent() {
        return new Intent(TETHER_SETTINGS);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleClick() {
        Object obj;
        boolean z = ((QSTile.BooleanState) this.mState).value;
        if ((z || this.mAirplaneMode.getValue() == 0) && this.mHotspotController.isHotspotReady()) {
            String str = this.TAG;
            StringBuilder sb = new StringBuilder();
            sb.append("handleClick: from: mState.value: ");
            sb.append(((QSTile.BooleanState) this.mState).value);
            sb.append(", to: ");
            sb.append(!((QSTile.BooleanState) this.mState).value);
            Log.d(str, sb.toString());
            if (z) {
                obj = null;
            } else {
                obj = QSTileImpl.ARG_SHOW_TRANSIENT_ENABLING;
            }
            refreshState(obj);
            this.mHotspotController.setHotspotEnabled(!z);
        }
    }

    @Override // com.android.systemui.plugins.qs.QSTile
    public CharSequence getTileLabel() {
        return this.mContext.getString(C0021R$string.quick_settings_hotspot_label);
    }

    /* access modifiers changed from: protected */
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        QSTile.Icon icon;
        int i = 1;
        boolean z = obj == QSTileImpl.ARG_SHOW_TRANSIENT_ENABLING;
        checkIfRestrictionEnforcedByAdminOnly(booleanState, "no_config_tethering");
        booleanState.value = z || this.mHotspotController.isHotspotEnabled();
        booleanState.label = this.mContext.getString(C0021R$string.quick_settings_hotspot_label);
        if (!booleanState.value) {
            icon = QSTileImpl.ResourceIcon.get(C0013R$drawable.ic_qs_hotspot_disabled);
        } else if (booleanState.isTransient) {
            icon = QSTileImpl.ResourceIcon.get(C0013R$drawable.ic_qs_hotspot_enabled);
        } else {
            icon = QSTileImpl.ResourceIcon.get(C0013R$drawable.ic_qs_hotspot_enabled);
        }
        booleanState.icon = icon;
        boolean z2 = this.mIsAirplaneMode;
        this.mIsAirplaneMode = this.mAirplaneMode.getValue() != 0;
        boolean isHotspotTransient = this.mHotspotController.isHotspotTransient();
        booleanState.isTransient = isHotspotTransient;
        if (isHotspotTransient) {
            booleanState.icon = QSTileImpl.ResourceIcon.get(C0013R$drawable.ic_qs_hotspot_enabled);
        } else if (this.mIsAirplaneMode) {
            booleanState.icon = QSTileImpl.ResourceIcon.get(C0013R$drawable.ic_qs_hotspot_disabled);
        } else if (z2) {
            booleanState.icon = QSTileImpl.ResourceIcon.get(C0013R$drawable.ic_qs_hotspot_disabled);
        }
        if (this.mIsAirplaneMode) {
            i = 0;
        } else if (booleanState.value || booleanState.isTransient) {
            i = 2;
        }
        booleanState.state = i;
        StringBuilder sb = new StringBuilder();
        sb.append((Object) booleanState.label);
        sb.append(",");
        sb.append(this.mContext.getString(booleanState.value ? C0021R$string.switch_bar_on : C0021R$string.switch_bar_off));
        booleanState.contentDescription = sb.toString();
        booleanState.expandedAccessibilityClassName = Switch.class.getName();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public String composeChangeAnnouncement() {
        if (((QSTile.BooleanState) this.mState).value) {
            return this.mContext.getString(C0021R$string.accessibility_quick_settings_hotspot_changed_on);
        }
        return this.mContext.getString(C0021R$string.accessibility_quick_settings_hotspot_changed_off);
    }

    private final class Callback implements HotspotController.Callback {
        private Callback() {
        }

        @Override // com.android.systemui.statusbar.policy.HotspotController.Callback
        public void onHotspotChanged(boolean z, int i) {
            MiuiHotspotTile.this.refreshState(Boolean.valueOf(z));
        }
    }
}
