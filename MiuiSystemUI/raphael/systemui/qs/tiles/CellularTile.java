package com.android.systemui.qs.tiles;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import com.android.internal.logging.MetricsLogger;
import com.android.settingslib.net.DataUsageController;
import com.android.systemui.Constants;
import com.android.systemui.Dependency;
import com.android.systemui.SystemUI;
import com.android.systemui.Util;
import com.android.systemui.VirtualSimUtils;
import com.android.systemui.plugins.R;
import com.android.systemui.plugins.qs.DetailAdapter;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSDetailItems;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.statusbar.CallStateController;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.policy.NetworkController;
import java.util.ArrayList;
import java.util.List;
import miui.app.AlertDialog;
import miui.securityspace.CrossUserUtils;
import miui.telephony.SubscriptionInfo;
import miui.telephony.SubscriptionManager;
import miui.telephony.TelephonyManager;

public class CellularTile extends QSTileImpl<QSTile.BooleanState> {
    private static final boolean DETAIL_ADAPTER_ENABLED = (!Constants.IS_CUST_SINGLE_SIM);
    private final NetworkController mController = ((NetworkController) Dependency.get(NetworkController.class));
    /* access modifiers changed from: private */
    public final DataUsageController mDataController = this.mController.getMobileDataController();
    /* access modifiers changed from: private */
    public final CellularDetailAdapter mDetailAdapter = createDetailAdapter();
    /* access modifiers changed from: private */
    public boolean mOpening;
    private final CellSignalCallback mSignalCallback = new CellSignalCallback();
    /* access modifiers changed from: private */
    public List<SubscriptionInfo> mSimInfoRecordList;

    public int getMetricsCategory() {
        return 115;
    }

    public CellularTile(QSHost qSHost) {
        super(qSHost);
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

    public void handleSetListening(boolean z) {
        if (z) {
            this.mController.addCallback(this.mSignalCallback);
        } else {
            this.mController.removeCallback(this.mSignalCallback);
        }
    }

    public Intent getLongClickIntent() {
        return longClickDataIntent();
    }

    public void click() {
        if (!this.mController.isMobileDataSupported()) {
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
        if (this.mController.isMobileDataSupported()) {
            boolean z = ((QSTile.BooleanState) this.mState).value;
            String str = this.TAG;
            StringBuilder sb = new StringBuilder();
            sb.append("handleClick: from: ");
            sb.append(z);
            sb.append(", to: ");
            sb.append(!z);
            Log.d(str, sb.toString());
            if (this.mOpening) {
                String str2 = this.TAG;
                Log.d(str2, "handleClick: opening, DataController.isMobileDataEnabled() = " + this.mSignalCallback.mInfo.enabled + ", not ready");
                return;
            }
            if (!z) {
                this.mOpening = true;
            }
            this.mDataController.setMobileDataEnabled(!z);
            refreshState();
        }
    }

    /* access modifiers changed from: protected */
    public void handleSecondaryClick() {
        TState tstate = this.mState;
        if (((QSTile.BooleanState) tstate).dualTarget) {
            if (!((QSTile.BooleanState) tstate).value) {
                Log.d(this.TAG, "handleSecondaryClick");
                this.mOpening = true;
                this.mDataController.setMobileDataEnabled(true);
                refreshState();
            }
            showDetail(true);
            this.mDetailAdapter.updateItems();
        }
    }

    public CharSequence getTileLabel() {
        return this.mContext.getString(R.string.quick_settings_cellular_detail_title);
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0035, code lost:
        r1 = r6.mSimInfoRecordList;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void handleUpdateState(com.android.systemui.plugins.qs.QSTile.BooleanState r7, java.lang.Object r8) {
        /*
            r6 = this;
            com.android.systemui.qs.tiles.CellularTile$CallbackInfo r8 = (com.android.systemui.qs.tiles.CellularTile.CallbackInfo) r8
            if (r8 != 0) goto L_0x000a
            com.android.systemui.qs.tiles.CellularTile$CellSignalCallback r8 = r6.mSignalCallback
            com.android.systemui.qs.tiles.CellularTile$CallbackInfo r8 = r8.mInfo
        L_0x000a:
            android.content.Context r0 = r6.mContext
            android.content.res.Resources r0 = r0.getResources()
            r1 = 2131821671(0x7f110467, float:1.9276092E38)
            java.lang.String r1 = r0.getString(r1)
            r7.label = r1
            com.android.systemui.statusbar.policy.NetworkController r1 = r6.mController
            boolean r1 = r1.isMobileDataSupported()
            r2 = 0
            if (r1 == 0) goto L_0x0096
            boolean r3 = r8.airplaneModeEnabled
            if (r3 == 0) goto L_0x0028
            goto L_0x0096
        L_0x0028:
            com.android.settingslib.net.DataUsageController r1 = r6.mDataController
            boolean r1 = r1.isMobileDataEnabled()
            r7.value = r1
            boolean r1 = DETAIL_ADAPTER_ENABLED
            r3 = 1
            if (r1 == 0) goto L_0x0041
            java.util.List<miui.telephony.SubscriptionInfo> r1 = r6.mSimInfoRecordList
            if (r1 == 0) goto L_0x0041
            int r1 = r1.size()
            if (r1 <= r3) goto L_0x0041
            r1 = r3
            goto L_0x0042
        L_0x0041:
            r1 = r2
        L_0x0042:
            r7.dualTarget = r1
            TState r1 = r6.mState
            com.android.systemui.plugins.qs.QSTile$BooleanState r1 = (com.android.systemui.plugins.qs.QSTile.BooleanState) r1
            boolean r1 = r1.value
            boolean r4 = r7.value
            if (r1 == r4) goto L_0x0066
            java.lang.String r1 = r6.TAG
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r5 = "handleUpdateState: isMobileDataEnabled: "
            r4.append(r5)
            boolean r5 = r7.value
            r4.append(r5)
            java.lang.String r4 = r4.toString()
            android.util.Log.d(r1, r4)
        L_0x0066:
            boolean r1 = r7.value
            if (r1 == 0) goto L_0x006c
            r1 = 2
            goto L_0x006d
        L_0x006c:
            r1 = r3
        L_0x006d:
            r7.state = r1
            boolean r1 = r6.mOpening
            if (r1 == 0) goto L_0x0078
            boolean r1 = r7.value
            if (r1 != 0) goto L_0x0078
            r2 = r3
        L_0x0078:
            r7.withAnimation = r2
            boolean r1 = r7.value
            if (r1 == 0) goto L_0x0082
            r1 = 2131232470(0x7f0806d6, float:1.808105E38)
            goto L_0x0085
        L_0x0082:
            r1 = 2131232469(0x7f0806d5, float:1.8081048E38)
        L_0x0085:
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)
            boolean r2 = r6.mInControlCenter
            int r1 = com.android.systemui.statusbar.Icons.getQSIcons(r1, r2)
            com.android.systemui.plugins.qs.QSTile$Icon r1 = com.android.systemui.qs.tileimpl.QSTileImpl.ResourceIcon.get(r1)
            r7.icon = r1
            goto L_0x00d1
        L_0x0096:
            java.lang.String r3 = r6.TAG
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r5 = "handleUpdateState: airplaneModeEnabled: "
            r4.append(r5)
            boolean r5 = r8.airplaneModeEnabled
            r4.append(r5)
            java.lang.String r5 = ", isMobileDataSupported: "
            r4.append(r5)
            r4.append(r1)
            java.lang.String r1 = r4.toString()
            android.util.Log.d(r3, r1)
            r7.dualTarget = r2
            r7.value = r2
            r7.state = r2
            r7.withAnimation = r2
            r1 = 2131232468(0x7f0806d4, float:1.8081046E38)
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)
            boolean r2 = r6.mInControlCenter
            int r1 = com.android.systemui.statusbar.Icons.getQSIcons(r1, r2)
            com.android.systemui.plugins.qs.QSTile$Icon r1 = com.android.systemui.qs.tileimpl.QSTileImpl.ResourceIcon.get(r1)
            r7.icon = r1
        L_0x00d1:
            boolean r1 = r7.dualTarget
            if (r1 == 0) goto L_0x0106
            java.util.List<miui.telephony.SubscriptionInfo> r1 = r6.mSimInfoRecordList
            if (r1 == 0) goto L_0x0106
            int r2 = r8.defaultDataSlot
            if (r2 < 0) goto L_0x0106
            int r1 = r1.size()
            if (r2 >= r1) goto L_0x0106
            java.util.List<miui.telephony.SubscriptionInfo> r1 = r6.mSimInfoRecordList
            int r2 = r8.defaultDataSlot
            java.lang.Object r1 = r1.get(r2)
            miui.telephony.SubscriptionInfo r1 = (miui.telephony.SubscriptionInfo) r1
            android.content.Context r2 = r6.mContext
            int r3 = r1.getSlotId()
            boolean r2 = com.android.systemui.VirtualSimUtils.isVirtualSim(r2, r3)
            if (r2 == 0) goto L_0x0100
            android.content.Context r1 = r6.mContext
            java.lang.String r1 = com.android.systemui.VirtualSimUtils.getVirtualSimCarrierName(r1)
            goto L_0x0104
        L_0x0100:
            java.lang.CharSequence r1 = r1.getDisplayName()
        L_0x0104:
            r7.label = r1
        L_0x0106:
            boolean r1 = r8.enabled
            if (r1 == 0) goto L_0x0111
            int r1 = r8.mobileSignalIconId
            if (r1 <= 0) goto L_0x0111
            java.lang.String r0 = r8.signalContentDescription
            goto L_0x0118
        L_0x0111:
            r1 = 2131820686(0x7f11008e, float:1.9274094E38)
            java.lang.String r0 = r0.getString(r1)
        L_0x0118:
            boolean r8 = r8.noSim
            r1 = 2131822316(0x7f1106ec, float:1.92774E38)
            r2 = 2131822315(0x7f1106eb, float:1.9277398E38)
            java.lang.String r3 = ","
            if (r8 == 0) goto L_0x0147
            java.lang.StringBuilder r8 = new java.lang.StringBuilder
            r8.<init>()
            java.lang.CharSequence r0 = r7.label
            r8.append(r0)
            r8.append(r3)
            android.content.Context r6 = r6.mContext
            boolean r0 = r7.value
            if (r0 == 0) goto L_0x0138
            goto L_0x0139
        L_0x0138:
            r1 = r2
        L_0x0139:
            java.lang.String r6 = r6.getString(r1)
            r8.append(r6)
            java.lang.String r6 = r8.toString()
            r7.contentDescription = r6
            goto L_0x016f
        L_0x0147:
            java.lang.StringBuilder r8 = new java.lang.StringBuilder
            r8.<init>()
            java.lang.CharSequence r4 = r7.label
            r8.append(r4)
            r8.append(r3)
            android.content.Context r6 = r6.mContext
            boolean r4 = r7.value
            if (r4 == 0) goto L_0x015b
            goto L_0x015c
        L_0x015b:
            r1 = r2
        L_0x015c:
            java.lang.String r6 = r6.getString(r1)
            r8.append(r6)
            r8.append(r3)
            r8.append(r0)
            java.lang.String r6 = r8.toString()
            r7.contentDescription = r6
        L_0x016f:
            java.lang.Class<android.widget.Switch> r6 = android.widget.Switch.class
            java.lang.String r6 = r6.getName()
            r7.expandedAccessibilityClassName = r6
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.qs.tiles.CellularTile.handleUpdateState(com.android.systemui.plugins.qs.QSTile$BooleanState, java.lang.Object):void");
    }

    public boolean isAvailable() {
        return this.mController.hasMobileDataFeature();
    }

    private void showConfirmDialog(boolean z) {
        ((CommandQueue) SystemUI.getComponent(this.mContext, CommandQueue.class)).animateCollapsePanels();
        AlertDialog.Builder builder = new AlertDialog.Builder(this.mContext);
        builder.setCancelable(false);
        builder.setTitle((CharSequence) this.mContext.getResources().getString(R.string.quick_settings_cellular_detail_title));
        builder.setMessage(z ? R.string.quick_settings_cellular_detail_dialog_message_turnoff : R.string.quick_settings_cellular_detail_dialog_message_turnon);
        builder.setNegativeButton((int) R.string.quick_settings_cellular_detail_dialog_negative_button_cancel, (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        builder.setPositiveButton(z ? R.string.quick_settings_cellular_detail_dialog_positive_button_turnoff : R.string.quick_settings_cellular_detail_dialog_positive_button_ok, (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                CellularTile.super.click();
            }
        });
        AlertDialog create = builder.create();
        create.getWindow().setType(2010);
        create.getWindow().addPrivateFlags(16);
        create.show();
    }

    private static final class CallbackInfo {
        boolean activityIn;
        boolean activityOut;
        boolean airplaneModeEnabled;
        String dataContentDescription;
        int dataTypeIconId;
        int defaultDataSlot;
        boolean enabled;
        String enabledDesc;
        boolean isDataTypeIconWide;
        int mobileSignalIconId;
        boolean noSim;
        boolean roaming;
        String signalContentDescription;

        private CallbackInfo() {
            this.defaultDataSlot = -1;
        }
    }

    private class CellSignalCallback implements NetworkController.SignalCallback {
        /* access modifiers changed from: private */
        public final CallbackInfo mInfo;

        public void setEthernetIndicators(NetworkController.IconState iconState) {
        }

        public void setIsImsRegisted(int i, boolean z) {
        }

        public void setMobileDataEnabled(boolean z) {
        }

        public void setNetworkNameVoice(int i, String str) {
        }

        public void setSlaveWifiIndicators(boolean z, NetworkController.IconState iconState, NetworkController.IconState iconState2) {
        }

        public void setSpeechHd(int i, boolean z) {
        }

        public void setVolteNoService(int i, boolean z) {
        }

        public void setVowifi(int i, boolean z) {
        }

        public void setWifiIndicators(boolean z, NetworkController.IconState iconState, NetworkController.IconState iconState2, boolean z2, boolean z3, String str, boolean z4) {
        }

        private CellSignalCallback() {
            this.mInfo = new CallbackInfo();
        }

        public void setMobileDataIndicators(NetworkController.IconState iconState, NetworkController.IconState iconState2, int i, int i2, boolean z, boolean z2, int i3, int i4, int i5, String str, String str2, boolean z3, int i6, boolean z4) {
            if (iconState2 != null) {
                boolean z5 = false;
                if (CellularTile.this.mOpening && iconState2.visible) {
                    boolean unused = CellularTile.this.mOpening = false;
                }
                CallbackInfo callbackInfo = this.mInfo;
                callbackInfo.enabled = iconState2.visible;
                callbackInfo.mobileSignalIconId = iconState2.icon;
                callbackInfo.signalContentDescription = iconState2.contentDescription;
                callbackInfo.dataTypeIconId = i2;
                callbackInfo.dataContentDescription = str;
                callbackInfo.activityIn = z;
                callbackInfo.activityOut = z2;
                callbackInfo.enabledDesc = str2;
                if (i2 != 0 && z3) {
                    z5 = true;
                }
                callbackInfo.isDataTypeIconWide = z5;
                CallbackInfo callbackInfo2 = this.mInfo;
                callbackInfo2.roaming = z4;
                CellularTile.this.refreshState(callbackInfo2);
                if (CellularTile.this.isShowingDetail()) {
                    CellularTile.this.fireToggleStateChanged(this.mInfo.enabled);
                }
            }
        }

        public void setNoSims(boolean z) {
            CallbackInfo callbackInfo = this.mInfo;
            callbackInfo.noSim = z;
            if (callbackInfo.noSim) {
                callbackInfo.mobileSignalIconId = 0;
                callbackInfo.dataTypeIconId = 0;
                callbackInfo.enabled = true;
                callbackInfo.enabledDesc = CellularTile.this.mContext.getString(R.string.keyguard_missing_sim_message_short);
                CallbackInfo callbackInfo2 = this.mInfo;
                callbackInfo2.signalContentDescription = callbackInfo2.enabledDesc;
            }
            CellularTile.this.refreshState(this.mInfo);
        }

        public void setIsAirplaneMode(NetworkController.IconState iconState) {
            CallbackInfo callbackInfo = this.mInfo;
            callbackInfo.airplaneModeEnabled = iconState.visible;
            CellularTile.this.refreshState(callbackInfo);
        }

        public void setSubs(List<SubscriptionInfo> list) {
            List unused = CellularTile.this.mSimInfoRecordList = SubscriptionManager.getDefault().getSubscriptionInfoList();
            CellularTile.this.refreshState();
            if (CellularTile.this.isShowingDetail()) {
                CellularTile.this.mDetailAdapter.updateItems();
            }
        }

        public void setIsDefaultDataSim(int i, boolean z) {
            if (z) {
                CellularTile.this.mDetailAdapter.setDefaultDataSlot(i);
                boolean z2 = this.mInfo.defaultDataSlot != i;
                CallbackInfo callbackInfo = this.mInfo;
                callbackInfo.defaultDataSlot = i;
                if (z2) {
                    CellularTile.this.refreshState(callbackInfo);
                }
            }
        }
    }

    private final class CellularDetailAdapter implements DetailAdapter, QSDetailItems.Callback {
        private final int[] SIM_SLOT_DISABLED_ICON;
        private final int[] SIM_SLOT_ICON;
        private int mDefaultDataSlot;
        private QSDetailItems mItems;

        public int getMetricsCategory() {
            return 117;
        }

        public boolean hasHeader() {
            return true;
        }

        public void onDetailItemDisconnect(QSDetailItems.Item item) {
        }

        private CellularDetailAdapter() {
            this.SIM_SLOT_ICON = new int[]{R.drawable.ic_qs_sim_card1, R.drawable.ic_qs_sim_card2};
            this.SIM_SLOT_DISABLED_ICON = new int[]{R.drawable.ic_qs_sim_card1_disable, R.drawable.ic_qs_sim_card2_disable};
        }

        public CharSequence getTitle() {
            return CellularTile.this.mContext.getString(R.string.quick_settings_cellular_detail_title);
        }

        public Boolean getToggleState() {
            return Boolean.valueOf(((QSTile.BooleanState) CellularTile.this.mState).value);
        }

        public boolean getToggleEnabled() {
            return !CellularTile.this.mOpening;
        }

        public Intent getSettingsIntent() {
            return CellularTile.longClickDataIntent();
        }

        public void setToggleState(boolean z) {
            MetricsLogger.action(CellularTile.this.mContext, 155, z);
            CellularTile.this.mDataController.setMobileDataEnabled(z);
            if (!z) {
                CellularTile.this.showDetail(false);
                return;
            }
            boolean unused = CellularTile.this.mOpening = true;
            CellularTile.this.fireToggleStateChanged(z);
        }

        public View createDetailView(Context context, View view, ViewGroup viewGroup) {
            this.mItems = QSDetailItems.convertOrInflate(context, view, viewGroup);
            this.mItems.setTagSuffix("Cellular");
            this.mItems.setCallback(this);
            if (CellularTile.this.isShowingDetail()) {
                updateItems();
            }
            return this.mItems;
        }

        /* access modifiers changed from: private */
        public void setDefaultDataSlot(int i) {
            boolean z = this.mDefaultDataSlot != i;
            this.mDefaultDataSlot = i;
            if (z && CellularTile.this.isShowingDetail()) {
                updateItems();
            }
        }

        /* access modifiers changed from: private */
        public void updateItems() {
            if (this.mItems != null) {
                int size = CellularTile.this.mSimInfoRecordList != null ? CellularTile.this.mSimInfoRecordList.size() : 0;
                if (size > 0) {
                    ArrayList arrayList = new ArrayList();
                    for (int i = 0; i < size; i++) {
                        SubscriptionInfo subscriptionInfo = (SubscriptionInfo) CellularTile.this.mSimInfoRecordList.get(i);
                        if (subscriptionInfo != null) {
                            arrayList.add(generateItem(subscriptionInfo, i));
                        }
                    }
                    this.mItems.setItems((QSDetailItems.Item[]) arrayList.toArray(new QSDetailItems.Item[0]));
                    return;
                }
                this.mItems.setItems((QSDetailItems.Item[]) null);
            }
        }

        private QSDetailItems.Item generateItem(SubscriptionInfo subscriptionInfo, int i) {
            QSDetailItems.Item acquireItem = this.mItems.acquireItem();
            boolean z = true;
            if (subscriptionInfo.isActivated()) {
                int[] iArr = this.SIM_SLOT_ICON;
                if (i < iArr.length) {
                    acquireItem.icon = iArr[i];
                }
                acquireItem.line1 = VirtualSimUtils.isVirtualSim(CellularTile.this.mContext, subscriptionInfo.getSlotId()) ? VirtualSimUtils.getVirtualSimCarrierName(CellularTile.this.mContext) : subscriptionInfo.getDisplayName();
                acquireItem.activated = true;
            } else {
                if (i < this.SIM_SLOT_ICON.length) {
                    acquireItem.icon = this.SIM_SLOT_DISABLED_ICON[i];
                }
                acquireItem.line1 = subscriptionInfo.getDisplayName() + CellularTile.this.mContext.getResources().getString(R.string.quick_settings_sim_disabled);
                acquireItem.activated = false;
            }
            if (this.mDefaultDataSlot != i) {
                z = false;
            }
            acquireItem.selected = z;
            acquireItem.icon2 = acquireItem.selected ? R.drawable.ic_qs_detail_item_selected : -1;
            acquireItem.line2 = subscriptionInfo.getDisplayNumber();
            acquireItem.tag = Integer.valueOf(i);
            return acquireItem;
        }

        public void onDetailItemClick(QSDetailItems.Item item) {
            int intValue;
            if (this.mItems != null) {
                if (((CallStateController) Dependency.get(CallStateController.class)).getCallState() != 0) {
                    Util.showSystemOverlayToast(CellularTile.this.mContext, (int) R.string.quick_settings_cellular_detail_unable_change, 0);
                    return;
                }
                Object obj = item.tag;
                if (obj != null && this.mDefaultDataSlot != (intValue = ((Integer) obj).intValue())) {
                    SubscriptionManager.getDefault().setDefaultDataSlotId(intValue);
                }
            }
        }

        public int getContainerHeight() {
            return CellularTile.this.mContext.getResources().getConfiguration().orientation == 1 ? -2 : -1;
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
}
