package com.android.systemui.qs.tiles;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.SystemProperties;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.Toast;
import androidx.appcompat.R$styleable;
import com.android.internal.logging.MetricsLogger;
import com.android.settingslib.net.DataUsageController;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0021R$string;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.qs.DetailAdapter;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.MiuiQSDetailItems;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.statusbar.policy.CallStateControllerImpl;
import com.android.systemui.statusbar.policy.NetworkController;
import com.miui.systemui.util.VirtualSimUtils;
import java.util.ArrayList;
import java.util.List;
import miui.app.AlertDialog;
import miui.securityspace.CrossUserUtils;
import miui.telephony.SubscriptionInfo;
import miui.telephony.SubscriptionManager;
import miui.telephony.TelephonyManager;

public class MiuiCellularTile extends QSTileImpl<QSTile.BooleanState> {
    private static final boolean DETAIL_ADAPTER_ENABLED;
    private final NetworkController mController;
    /* access modifiers changed from: private */
    public final DataUsageController mDataController;
    /* access modifiers changed from: private */
    public final CellularDetailAdapter mDetailAdapter;
    private final CellSignalCallback mSignalCallback = new CellSignalCallback();
    /* access modifiers changed from: private */
    public List<SubscriptionInfo> mSimInfoRecordList;

    static /* synthetic */ void lambda$showConfirmDialog$0(DialogInterface dialogInterface, int i) {
    }

    public int getMetricsCategory() {
        return 115;
    }

    static {
        boolean z = false;
        if (SystemProperties.getInt("ro.miui.singlesim", 0) != 1) {
            z = true;
        }
        DETAIL_ADAPTER_ENABLED = z;
    }

    public MiuiCellularTile(QSHost qSHost, NetworkController networkController) {
        super(qSHost);
        this.mController = networkController;
        this.mDataController = networkController.getMobileDataController();
        this.mDetailAdapter = createDetailAdapter();
        this.mController.observe(getLifecycle(), this.mSignalCallback);
    }

    public QSTile.SignalState newTileState() {
        return new QSTile.SignalState();
    }

    public DetailAdapter getDetailAdapter() {
        return this.mDetailAdapter;
    }

    /* access modifiers changed from: protected */
    public CellularDetailAdapter createDetailAdapter() {
        return new CellularDetailAdapter();
    }

    public Intent getLongClickIntent() {
        if (!((QSTile.BooleanState) this.mState).disabledByPolicy) {
            return longClickDataIntent();
        }
        Intent intent = new Intent("android.settings.SETTINGS");
        intent.setFlags(335544320);
        return intent;
    }

    public void click() {
        if (!this.mDataController.isMobileDataSupported()) {
            return;
        }
        if (TelephonyManager.isCustForKrOps()) {
            showConfirmDialog(((QSTile.BooleanState) this.mState).state == 2);
        } else {
            super.click();
        }
    }

    /* access modifiers changed from: protected */
    public void handleClick() {
        if (this.mDataController.isMobileDataSupported()) {
            String str = this.TAG;
            StringBuilder sb = new StringBuilder();
            sb.append("handleClick: from: ");
            sb.append(((QSTile.BooleanState) this.mState).value);
            sb.append(", to: ");
            sb.append(!((QSTile.BooleanState) this.mState).value);
            Log.d(str, sb.toString());
            this.mDataController.setMobileDataEnabled(!((QSTile.BooleanState) this.mState).value);
        }
    }

    /* access modifiers changed from: protected */
    public void handleSecondaryClick() {
        if (((QSTile.BooleanState) this.mState).dualTarget) {
            showDetail(true);
            this.mDetailAdapter.updateItems();
            if (!((QSTile.BooleanState) this.mState).value) {
                this.mDataController.setMobileDataEnabled(true);
            }
        }
    }

    public CharSequence getTileLabel() {
        return this.mContext.getString(C0021R$string.quick_settings_cellular_detail_title);
    }

    /* access modifiers changed from: protected */
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        String str;
        List<SubscriptionInfo> list;
        int i;
        CharSequence charSequence;
        int i2;
        List<SubscriptionInfo> list2;
        CallbackInfo callbackInfo = (CallbackInfo) obj;
        if (callbackInfo == null) {
            callbackInfo = this.mSignalCallback.mInfo;
        }
        checkIfRestrictionEnforcedByAdminOnly(booleanState, "no_config_mobile_networks");
        Resources resources = this.mContext.getResources();
        booleanState.label = resources.getString(C0021R$string.mobile_data);
        boolean z = false;
        if (!this.mDataController.isMobileDataSupported() || callbackInfo.airplaneModeEnabled) {
            if (((QSTile.BooleanState) this.mState).state != 0) {
                Log.d(this.TAG, "handleUpdateState: airplaneModeEnabled: " + callbackInfo.airplaneModeEnabled + ", isMobileDataSupported: " + this.mDataController.isMobileDataSupported());
            }
            booleanState.dualTarget = false;
            booleanState.value = false;
            booleanState.state = 0;
            booleanState.icon = QSTileImpl.ResourceIcon.get(C0013R$drawable.ic_qs_data_disabled);
        } else {
            booleanState.value = this.mDataController.isMobileDataEnabled();
            if (DETAIL_ADAPTER_ENABLED && (list2 = this.mSimInfoRecordList) != null && list2.size() > 1) {
                z = true;
            }
            booleanState.dualTarget = z;
            if (((QSTile.BooleanState) this.mState).value != booleanState.value) {
                Log.d(this.TAG, "handleUpdateState: isMobileDataEnabled: " + booleanState.value);
            }
            booleanState.state = booleanState.value ? 2 : 1;
            if (booleanState.value) {
                i2 = C0013R$drawable.ic_qs_data_on;
            } else {
                i2 = C0013R$drawable.ic_qs_data_off;
            }
            booleanState.icon = QSTileImpl.ResourceIcon.get(i2);
        }
        if (booleanState.value && isShowingDetail()) {
            this.mDetailAdapter.setMobileDataEnabled(true);
        }
        if (booleanState.dualTarget && (list = this.mSimInfoRecordList) != null && (i = callbackInfo.defaultDataSlot) >= 0 && i < list.size()) {
            SubscriptionInfo subscriptionInfo = this.mSimInfoRecordList.get(callbackInfo.defaultDataSlot);
            if (VirtualSimUtils.isVirtualSim(this.mContext, subscriptionInfo.getSlotId())) {
                charSequence = VirtualSimUtils.getVirtualSimCarrierName(this.mContext);
            } else {
                charSequence = subscriptionInfo.getDisplayName();
            }
            booleanState.label = charSequence;
        }
        if (!callbackInfo.enabled || callbackInfo.mobileSignalIconId <= 0) {
            str = resources.getString(C0021R$string.accessibility_no_signal);
        } else {
            str = callbackInfo.signalContentDescription.toString();
        }
        if (callbackInfo.noSim) {
            StringBuilder sb = new StringBuilder();
            sb.append(booleanState.label);
            sb.append(",");
            sb.append(this.mContext.getString(booleanState.value ? C0021R$string.switch_bar_on : C0021R$string.switch_bar_off));
            booleanState.contentDescription = sb.toString();
        } else {
            StringBuilder sb2 = new StringBuilder();
            sb2.append(booleanState.label);
            sb2.append(",");
            sb2.append(this.mContext.getString(booleanState.value ? C0021R$string.switch_bar_on : C0021R$string.switch_bar_off));
            sb2.append(",");
            sb2.append(str);
            booleanState.contentDescription = sb2.toString();
        }
        booleanState.expandedAccessibilityClassName = Switch.class.getName();
    }

    public boolean isAvailable() {
        return this.mController.hasMobileDataFeature();
    }

    private static final class CallbackInfo {
        boolean activityIn;
        boolean activityOut;
        boolean airplaneModeEnabled;
        CharSequence dataContentDescription;
        int dataTypeIconId;
        int defaultDataSlot;
        boolean enabled;
        CharSequence enabledDesc;
        boolean isDataTypeIconWide;
        int mobileSignalIconId;
        boolean noSim;
        boolean roaming;
        CharSequence signalContentDescription;

        private CallbackInfo() {
            this.defaultDataSlot = -1;
        }
    }

    private final class CellSignalCallback implements NetworkController.SignalCallback {
        /* access modifiers changed from: private */
        public final CallbackInfo mInfo;

        public void setEthernetIndicators(NetworkController.IconState iconState) {
        }

        private CellSignalCallback() {
            this.mInfo = new CallbackInfo();
        }

        public void setMobileDataIndicators(NetworkController.IconState iconState, NetworkController.IconState iconState2, int i, int i2, boolean z, boolean z2, int i3, CharSequence charSequence, CharSequence charSequence2, CharSequence charSequence3, boolean z3, int i4, boolean z4) {
            if (iconState2 != null) {
                CallbackInfo callbackInfo = this.mInfo;
                callbackInfo.enabled = iconState2.visible;
                callbackInfo.mobileSignalIconId = iconState2.icon;
                callbackInfo.signalContentDescription = iconState2.contentDescription;
                callbackInfo.dataTypeIconId = i2;
                if (charSequence3 == null) {
                    charSequence2 = null;
                }
                callbackInfo.dataContentDescription = charSequence2;
                CallbackInfo callbackInfo2 = this.mInfo;
                callbackInfo2.activityIn = z;
                callbackInfo2.activityOut = z2;
                callbackInfo2.enabledDesc = charSequence3;
                callbackInfo2.isDataTypeIconWide = i2 != 0 && z3;
                CallbackInfo callbackInfo3 = this.mInfo;
                callbackInfo3.roaming = z4;
                MiuiCellularTile.this.refreshState(callbackInfo3);
            }
        }

        public void setNoSims(boolean z, boolean z2) {
            CallbackInfo callbackInfo = this.mInfo;
            callbackInfo.noSim = z;
            if (z) {
                callbackInfo.mobileSignalIconId = 0;
                callbackInfo.dataTypeIconId = 0;
                callbackInfo.enabled = true;
                callbackInfo.enabledDesc = MiuiCellularTile.this.mContext.getString(C0021R$string.keyguard_missing_sim_message_short);
                CallbackInfo callbackInfo2 = this.mInfo;
                callbackInfo2.signalContentDescription = callbackInfo2.enabledDesc;
            }
            MiuiCellularTile.this.refreshState(this.mInfo);
        }

        public void setIsAirplaneMode(NetworkController.IconState iconState) {
            CallbackInfo callbackInfo = this.mInfo;
            callbackInfo.airplaneModeEnabled = iconState.visible;
            MiuiCellularTile.this.refreshState(callbackInfo);
        }

        public void setMobileDataEnabled(boolean z) {
            MiuiCellularTile.this.mDetailAdapter.setMobileDataEnabled(z);
            MiuiCellularTile.this.refreshState(this.mInfo);
            if (!z && MiuiCellularTile.this.isShowingDetail()) {
                MiuiCellularTile.this.showDetail(false);
            }
        }

        public void setSubs(List<android.telephony.SubscriptionInfo> list) {
            List unused = MiuiCellularTile.this.mSimInfoRecordList = SubscriptionManager.getDefault().getSubscriptionInfoList();
            MiuiCellularTile.this.refreshState(this.mInfo);
            if (!MiuiCellularTile.this.isShowingDetail()) {
                return;
            }
            if (list == null || list.size() < 2) {
                MiuiCellularTile.this.showDetail(false);
            } else {
                MiuiCellularTile.this.mDetailAdapter.updateItems();
            }
        }

        public void setIsDefaultDataSim(int i, boolean z) {
            if (z) {
                MiuiCellularTile.this.mDetailAdapter.setDefaultDataSlot(i);
                boolean z2 = this.mInfo.defaultDataSlot != i;
                CallbackInfo callbackInfo = this.mInfo;
                callbackInfo.defaultDataSlot = i;
                if (z2) {
                    MiuiCellularTile.this.refreshState(callbackInfo);
                }
            }
        }
    }

    private final class CellularDetailAdapter implements DetailAdapter, MiuiQSDetailItems.Callback {
        private final int[] SIM_SLOT_DISABLED_ICON;
        private final int[] SIM_SLOT_ICON;
        private int mDefaultDataSlot;
        private MiuiQSDetailItems mItems;

        public int getMetricsCategory() {
            return R$styleable.AppCompatTheme_windowActionBar;
        }

        public boolean getToggleEnabled() {
            return true;
        }

        public boolean hasHeader() {
            return true;
        }

        public void onDetailItemDisconnect(MiuiQSDetailItems.Item item) {
        }

        private CellularDetailAdapter() {
            this.SIM_SLOT_ICON = new int[]{C0013R$drawable.ic_qs_sim_card1, C0013R$drawable.ic_qs_sim_card2};
            this.SIM_SLOT_DISABLED_ICON = new int[]{C0013R$drawable.ic_qs_sim_card1_disable, C0013R$drawable.ic_qs_sim_card2_disable};
        }

        public CharSequence getTitle() {
            return MiuiCellularTile.this.mContext.getString(C0021R$string.quick_settings_cellular_detail_title);
        }

        public Boolean getToggleState() {
            return Boolean.valueOf(((QSTile.BooleanState) MiuiCellularTile.this.mState).value);
        }

        public Intent getSettingsIntent() {
            return MiuiCellularTile.longClickDataIntent();
        }

        public void setToggleState(boolean z) {
            MetricsLogger.action(MiuiCellularTile.this.mContext, 155, z);
            MiuiCellularTile.this.mDataController.setMobileDataEnabled(z);
        }

        public View createDetailView(Context context, View view, ViewGroup viewGroup) {
            MiuiQSDetailItems convertOrInflate = MiuiQSDetailItems.convertOrInflate(context, view, viewGroup);
            this.mItems = convertOrInflate;
            convertOrInflate.setTagSuffix("Cellular");
            this.mItems.setCallback(this);
            if (MiuiCellularTile.this.isShowingDetail()) {
                updateItems();
            }
            return this.mItems;
        }

        /* access modifiers changed from: private */
        public void setDefaultDataSlot(int i) {
            boolean z = this.mDefaultDataSlot != i;
            this.mDefaultDataSlot = i;
            if (z && MiuiCellularTile.this.isShowingDetail()) {
                updateItems();
            }
        }

        /* access modifiers changed from: private */
        public void updateItems() {
            if (this.mItems != null) {
                int size = MiuiCellularTile.this.mSimInfoRecordList != null ? MiuiCellularTile.this.mSimInfoRecordList.size() : 0;
                if (size > 0) {
                    ArrayList arrayList = new ArrayList();
                    for (int i = 0; i < size; i++) {
                        SubscriptionInfo subscriptionInfo = (SubscriptionInfo) MiuiCellularTile.this.mSimInfoRecordList.get(i);
                        if (subscriptionInfo != null) {
                            arrayList.add(generateItem(subscriptionInfo, i));
                        }
                    }
                    this.mItems.setItems((MiuiQSDetailItems.Item[]) arrayList.toArray(new MiuiQSDetailItems.Item[0]));
                    return;
                }
                this.mItems.setItems((MiuiQSDetailItems.Item[]) null);
            }
        }

        private MiuiQSDetailItems.Item generateItem(SubscriptionInfo subscriptionInfo, int i) {
            MiuiQSDetailItems.Item acquireItem = this.mItems.acquireItem();
            boolean z = true;
            if (subscriptionInfo.isActivated()) {
                int[] iArr = this.SIM_SLOT_ICON;
                if (i < iArr.length) {
                    acquireItem.icon = iArr[i];
                }
                acquireItem.line1 = VirtualSimUtils.isVirtualSim(MiuiCellularTile.this.mContext, subscriptionInfo.getSlotId()) ? VirtualSimUtils.getVirtualSimCarrierName(MiuiCellularTile.this.mContext) : subscriptionInfo.getDisplayName();
                acquireItem.activated = true;
            } else {
                if (i < this.SIM_SLOT_ICON.length) {
                    acquireItem.icon = this.SIM_SLOT_DISABLED_ICON[i];
                }
                acquireItem.line1 = subscriptionInfo.getDisplayName() + MiuiCellularTile.this.mContext.getResources().getString(C0021R$string.quick_settings_sim_disabled);
                acquireItem.activated = false;
            }
            if (this.mDefaultDataSlot != i) {
                z = false;
            }
            acquireItem.selected = z;
            acquireItem.icon2 = z ? C0013R$drawable.ic_qs_detail_item_selected : -1;
            acquireItem.line2 = subscriptionInfo.getDisplayNumber();
            acquireItem.tag = Integer.valueOf(i);
            return acquireItem;
        }

        public void setMobileDataEnabled(boolean z) {
            MiuiCellularTile.this.fireToggleStateChanged(z);
        }

        public void onDetailItemClick(MiuiQSDetailItems.Item item) {
            int intValue;
            if (this.mItems != null) {
                if (((CallStateControllerImpl) Dependency.get(CallStateControllerImpl.class)).getCallState() != 0) {
                    Toast.makeText(MiuiCellularTile.this.mContext, C0021R$string.quick_settings_cellular_detail_unable_change, 0).show();
                    return;
                }
                Object obj = item.tag;
                if (obj != null && this.mDefaultDataSlot != (intValue = ((Integer) obj).intValue())) {
                    SubscriptionManager.getDefault().setDefaultDataSlotId(intValue);
                }
            }
        }

        public int getContainerHeight() {
            return MiuiCellularTile.this.mContext.getResources().getConfiguration().orientation == 1 ? -2 : -1;
        }
    }

    static Intent longClickDataIntent() {
        ComponentName unflattenFromString;
        if (CrossUserUtils.getCurrentUserId() != 0 || (unflattenFromString = ComponentName.unflattenFromString("com.android.phone/.settings.MobileNetworkSettings")) == null) {
            return null;
        }
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setComponent(unflattenFromString);
        intent.putExtra(":miui:starting_window_label", "");
        intent.setFlags(335544320);
        return intent;
    }

    private void showConfirmDialog(boolean z) {
        int i;
        int i2;
        this.mHost.collapsePanels();
        AlertDialog.Builder builder = new AlertDialog.Builder(this.mContext, 8);
        builder.setCancelable(false);
        builder.setTitle(C0021R$string.quick_settings_cellular_detail_title);
        if (z) {
            i = C0021R$string.quick_settings_cellular_detail_dialog_message_turnoff;
        } else {
            i = C0021R$string.quick_settings_cellular_detail_dialog_message_turnon;
        }
        builder.setMessage(i);
        builder.setNegativeButton(C0021R$string.quick_settings_cellular_detail_dialog_negative_button_cancel, $$Lambda$MiuiCellularTile$xuv9jHuPCYNuIuI1rQzDEiBE9fU.INSTANCE);
        if (z) {
            i2 = C0021R$string.quick_settings_cellular_detail_dialog_positive_button_turnoff;
        } else {
            i2 = C0021R$string.quick_settings_cellular_detail_dialog_positive_button_ok;
        }
        builder.setPositiveButton(i2, new DialogInterface.OnClickListener() {
            public final void onClick(DialogInterface dialogInterface, int i) {
                MiuiCellularTile.this.lambda$showConfirmDialog$1$MiuiCellularTile(dialogInterface, i);
            }
        });
        AlertDialog create = builder.create();
        create.getWindow().setType(2010);
        create.getWindow().addSystemFlags(16);
        create.show();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$showConfirmDialog$1 */
    public /* synthetic */ void lambda$showConfirmDialog$1$MiuiCellularTile(DialogInterface dialogInterface, int i) {
        super.click();
    }
}
