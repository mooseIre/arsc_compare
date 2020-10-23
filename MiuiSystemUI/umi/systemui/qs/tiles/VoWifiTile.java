package com.android.systemui.qs.tiles;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Bundle;
import android.provider.MiuiSettings;
import android.telecom.TelecomManager;
import android.util.Log;
import android.widget.Toast;
import com.android.ims.ImsManager;
import com.android.internal.util.ArrayUtils;
import com.android.systemui.Constants;
import com.android.systemui.plugins.R;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import java.util.List;
import miui.securityspace.CrossUserUtils;
import miui.telephony.SubscriptionInfo;
import miui.telephony.SubscriptionManager;

public class VoWifiTile extends QSTileImpl<QSTile.BooleanState> implements SubscriptionManager.OnSubscriptionsChangedListener {
    private final ContentResolver mResolver;
    private List<SubscriptionInfo> mSimInfoRecordList;
    private int mSlotId;
    private ContentObserver mVoWifiEnableObserver = new ContentObserver(this.mHandler) {
        public void onChange(boolean z) {
            Log.d(VoWifiTile.this.TAG, "onChange");
            VoWifiTile.this.refreshState();
        }
    };

    public int getMetricsCategory() {
        return 0;
    }

    public VoWifiTile(QSHost qSHost, int i) {
        super(qSHost);
        this.mSlotId = i;
        this.mResolver = this.mContext.getContentResolver();
        this.mSimInfoRecordList = SubscriptionManager.getDefault().getSubscriptionInfoList();
        String str = this.TAG;
        Log.d(str, "VoWifiTile init: " + i);
    }

    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    /* access modifiers changed from: protected */
    public void handleClick() {
        TelecomManager telecomManager = (TelecomManager) this.mContext.getSystemService("telecom");
        if (this.mContext.getResources().getBoolean(R.bool.switch_vowifi_during_call) || !telecomManager.isInCall()) {
            ImsManager instance = ImsManager.getInstance(this.mContext, this.mSlotId);
            boolean isWfcEnabledByUser = instance.isWfcEnabledByUser();
            instance.setWfcSetting(!isWfcEnabledByUser);
            String str = this.TAG;
            Log.d(str, "setWfcSetting: " + this.mSlotId + " enable: " + instance.isWfcEnabledByUser());
            maybeShowVoWifiFirstDialog(isWfcEnabledByUser);
            return;
        }
        Toast.makeText(this.mContext, R.string.msim_set_sub_not_supported_phone_in_call, 0).show();
    }

    private void maybeShowVoWifiFirstDialog(boolean z) {
        if (this.mContext.getResources().getBoolean(R.bool.show_vowifi_first_dialog) && !z && !MiuiSettings.Global.getBoolean(this.mResolver, "wifi_call_available_dialog_showed")) {
            this.mHost.collapsePanels();
            Intent intent = new Intent("android.intent.action.MAIN");
            intent.setComponent(ComponentName.unflattenFromString("com.android.phone/.MiuiErrorDialogActivity"));
            intent.putExtra("dialog_type", 10);
            intent.setFlags(268435456);
            this.mContext.startActivity(intent);
            MiuiSettings.Global.putBoolean(this.mResolver, "wifi_call_available_dialog_showed", true);
        }
    }

    public boolean isAvailable() {
        if (!android.telephony.SubscriptionManager.getResourcesForSubId(this.mContext, getSubId()).getBoolean(R.bool.show_vowifi_qs)) {
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
        booleanState.label = getTileLabel();
        int size = ArrayUtils.size(this.mSimInfoRecordList);
        if (size == 0) {
            booleanState.value = false;
            booleanState.state = 0;
            booleanState.icon = QSTileImpl.ResourceIcon.get(R.drawable.ic_qs_vowifi_unavailable);
            return;
        }
        boolean isWfcEnabled = isWfcEnabled(this.mSlotId);
        booleanState.value = isWfcEnabled;
        booleanState.state = isWfcEnabled ? 2 : 1;
        if (size == 1) {
            booleanState.icon = QSTileImpl.ResourceIcon.get(booleanState.value ? R.drawable.ic_qs_vowifi_on : R.drawable.ic_qs_vowifi_off);
            return;
        }
        int i = this.mSlotId == 0 ? R.drawable.ic_qs_vowifi_sim1_on : R.drawable.ic_qs_vowifi_sim2_on;
        int i2 = this.mSlotId == 0 ? R.drawable.ic_qs_vowifi_sim1_off : R.drawable.ic_qs_vowifi_sim2_off;
        if (!booleanState.value) {
            i = i2;
        }
        booleanState.icon = QSTileImpl.ResourceIcon.get(i);
    }

    public Intent getLongClickIntent() {
        if (CrossUserUtils.getCurrentUserId() != 0) {
            return null;
        }
        ComponentName unflattenFromString = ComponentName.unflattenFromString(Constants.IS_CUST_SINGLE_SIM ? "com.android.phone/.settings.MobileNetworkSettings" : "com.android.phone/.settings.MultiSimInfoEditorActivity");
        if (unflattenFromString == null) {
            return null;
        }
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setComponent(unflattenFromString);
        intent.putExtra(":miui:starting_window_label", "");
        Bundle bundle = new Bundle();
        if (!Constants.IS_CUST_SINGLE_SIM) {
            SubscriptionManager.putSlotId(bundle, this.mSlotId);
            intent.putExtras(bundle);
        }
        intent.setFlags(335544320);
        return intent;
    }

    /* access modifiers changed from: protected */
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

    public CharSequence getTileLabel() {
        List<SubscriptionInfo> list = this.mSimInfoRecordList;
        return this.mContext.getString((list == null || list.size() <= 1) ? R.string.quick_settings_vowifi_label : this.mSlotId == 0 ? R.string.quick_settings_vowifi_sim1_label : R.string.quick_settings_vowifi_sim2_label);
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
