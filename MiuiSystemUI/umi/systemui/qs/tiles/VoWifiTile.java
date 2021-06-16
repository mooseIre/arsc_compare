package com.android.systemui.qs.tiles;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.SystemProperties;
import android.provider.MiuiSettings;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;
import codeinjection.CodeInjection;
import com.android.ims.ImsManager;
import com.android.internal.util.ArrayUtils;
import com.android.systemui.C0010R$bool;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0021R$string;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import java.util.List;
import miui.securityspace.CrossUserUtils;
import miui.telephony.SubscriptionInfo;
import miui.telephony.SubscriptionManager;

public class VoWifiTile extends QSTileImpl<QSTile.BooleanState> implements SubscriptionManager.OnSubscriptionsChangedListener {
    private static final boolean IS_CUST_SINGLE_SIM;
    private final ContentResolver mResolver;
    private List<SubscriptionInfo> mSimInfoRecordList;
    private int mSlotId;
    private ContentObserver mVoWifiEnableObserver = new ContentObserver(this.mHandler) {
        /* class com.android.systemui.qs.tiles.VoWifiTile.AnonymousClass1 */

        public void onChange(boolean z) {
            Log.d(((QSTileImpl) VoWifiTile.this).TAG, "onChange");
            VoWifiTile.this.refreshState();
        }
    };

    @Override // com.android.systemui.plugins.qs.QSTile, com.android.systemui.qs.tileimpl.QSTileImpl
    public int getMetricsCategory() {
        return 0;
    }

    static {
        boolean z = false;
        if (SystemProperties.getInt("ro.miui.singlesim", 0) == 1) {
            z = true;
        }
        IS_CUST_SINGLE_SIM = z;
    }

    public VoWifiTile(QSHost qSHost, int i) {
        super(qSHost);
        this.mSlotId = i;
        this.mResolver = this.mContext.getContentResolver();
        this.mSimInfoRecordList = SubscriptionManager.getDefault().getSubscriptionInfoList();
        String str = this.TAG;
        Log.d(str, "VoWifiTile init: " + i);
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    public boolean isOrangePolandSim() {
        String simOperatorNumericForPhone = ((TelephonyManager) this.mContext.getSystemService("phone")).getSimOperatorNumericForPhone(this.mSlotId);
        String str = this.TAG;
        Log.d(str, "simNumeric = " + simOperatorNumericForPhone);
        return "26003".equals(simOperatorNumericForPhone);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleClick() {
        TelecomManager telecomManager = (TelecomManager) this.mContext.getSystemService("telecom");
        if (this.mContext.getResources().getBoolean(C0010R$bool.switch_vowifi_during_call) || !telecomManager.isInCall()) {
            ImsManager instance = ImsManager.getInstance(this.mContext, this.mSlotId);
            boolean isWfcEnabledByUser = instance.isWfcEnabledByUser();
            String str = this.TAG;
            Log.d(str, "setWfcSetting: " + this.mSlotId + " enable: " + instance.isWfcEnabledByUser());
            if (!MiuiSettings.Global.getBoolean(this.mResolver, "wifi_call_available_dialog_showed")) {
                if (!isOrangePolandSim()) {
                    instance.setWfcSetting(!isWfcEnabledByUser);
                    MiuiSettings.Global.putBoolean(this.mResolver, "wifi_call_available_dialog_showed", true);
                }
                maybeShowVoWifiFirstDialog(isWfcEnabledByUser);
                return;
            }
            instance.setWfcSetting(!isWfcEnabledByUser);
            return;
        }
        Toast.makeText(this.mContext, C0021R$string.msim_set_sub_not_supported_phone_in_call, 0).show();
    }

    private void maybeShowVoWifiFirstDialog(boolean z) {
        if (this.mContext.getResources().getBoolean(C0010R$bool.show_vowifi_first_dialog) && !z) {
            this.mHost.collapsePanels();
            Intent intent = new Intent("android.intent.action.MAIN");
            intent.setComponent(ComponentName.unflattenFromString("com.android.phone/.MiuiErrorDialogActivity"));
            intent.putExtra("dialog_type", 10);
            intent.putExtra("wfc_state", z);
            intent.putExtra("phone_id", this.mSlotId);
            intent.setFlags(268435456);
            this.mContext.startActivity(intent);
        }
    }

    @Override // com.android.systemui.plugins.qs.QSTile, com.android.systemui.qs.tileimpl.QSTileImpl
    public boolean isAvailable() {
        if (!android.telephony.SubscriptionManager.getResourcesForSubId(this.mContext, getSubId()).getBoolean(C0010R$bool.show_vowifi_qs)) {
            Log.d(this.TAG, "show vowifi false");
            return false;
        } else if (!isWfcEnabledByPlatform(this.mSlotId)) {
            Log.d(this.TAG, "isWfcEnabledByPlatform false");
            return false;
        } else {
            List subscriptionInfoList = SubscriptionManager.getDefault().getSubscriptionInfoList();
            if (!ArrayUtils.isEmpty(subscriptionInfoList) && (subscriptionInfoList.size() != 1 || ((SubscriptionInfo) subscriptionInfoList.get(0)).getSlotId() == this.mSlotId)) {
                return true;
            }
            String str = this.TAG;
            Log.d(str, "subinfos : " + subscriptionInfoList + " mSlotId: " + this.mSlotId);
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        int i;
        booleanState.label = getTileLabel();
        int size = ArrayUtils.size(this.mSimInfoRecordList);
        if (size == 0) {
            booleanState.value = false;
            booleanState.state = 0;
            booleanState.icon = QSTileImpl.ResourceIcon.get(C0013R$drawable.ic_qs_vowifi_unavailable);
            return;
        }
        boolean isWfcEnabled = isWfcEnabled(this.mSlotId);
        booleanState.value = isWfcEnabled;
        booleanState.state = isWfcEnabled ? 2 : 1;
        if (size == 1) {
            if (booleanState.value) {
                i = C0013R$drawable.ic_qs_vowifi_on;
            } else {
                i = C0013R$drawable.ic_qs_vowifi_off;
            }
            booleanState.icon = QSTileImpl.ResourceIcon.get(i);
            return;
        }
        int i2 = this.mSlotId == 0 ? C0013R$drawable.ic_qs_vowifi_sim1_on : C0013R$drawable.ic_qs_vowifi_sim2_on;
        int i3 = this.mSlotId == 0 ? C0013R$drawable.ic_qs_vowifi_sim1_off : C0013R$drawable.ic_qs_vowifi_sim2_off;
        if (!booleanState.value) {
            i2 = i3;
        }
        booleanState.icon = QSTileImpl.ResourceIcon.get(i2);
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public Intent getLongClickIntent() {
        if (CrossUserUtils.getCurrentUserId() != 0) {
            return null;
        }
        ComponentName unflattenFromString = ComponentName.unflattenFromString(IS_CUST_SINGLE_SIM ? "com.android.phone/.settings.MobileNetworkSettings" : "com.android.phone/.settings.MultiSimInfoEditorActivity");
        if (unflattenFromString == null) {
            return null;
        }
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setComponent(unflattenFromString);
        intent.putExtra(":miui:starting_window_label", CodeInjection.MD5);
        Bundle bundle = new Bundle();
        if (!IS_CUST_SINGLE_SIM) {
            SubscriptionManager.putSlotId(bundle, this.mSlotId);
            intent.putExtras(bundle);
        }
        intent.setFlags(335544320);
        return intent;
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleSetListening(boolean z) {
        if (z) {
            this.mResolver.registerContentObserver(android.telephony.SubscriptionManager.CONTENT_URI, true, this.mVoWifiEnableObserver);
            SubscriptionManager.getDefault().addOnSubscriptionsChangedListener(this);
            return;
        }
        this.mResolver.unregisterContentObserver(this.mVoWifiEnableObserver);
        SubscriptionManager.getDefault().removeOnSubscriptionsChangedListener(this);
    }

    public void onSubscriptionsChanged() {
        this.mSimInfoRecordList = SubscriptionManager.getDefault().getSubscriptionInfoList();
        refreshState();
    }

    @Override // com.android.systemui.plugins.qs.QSTile
    public CharSequence getTileLabel() {
        int i = C0021R$string.quick_settings_vowifi_label;
        List<SubscriptionInfo> list = this.mSimInfoRecordList;
        if (list != null && list.size() > 1) {
            i = this.mSlotId == 0 ? C0021R$string.quick_settings_vowifi_sim1_label : C0021R$string.quick_settings_vowifi_sim2_label;
        }
        return this.mContext.getString(i);
    }

    public boolean isWfcEnabled(int i) {
        return ImsManager.getInstance(this.mContext, i).isWfcEnabledByUser();
    }

    public boolean isWfcEnabledByPlatform(int i) {
        return ImsManager.getInstance(this.mContext, i).isWfcEnabledByPlatform();
    }

    private int getSubId() {
        int[] subId = android.telephony.SubscriptionManager.getSubId(this.mSlotId);
        if (subId == null || subId.length < 1) {
            return -1;
        }
        return subId[0];
    }
}
