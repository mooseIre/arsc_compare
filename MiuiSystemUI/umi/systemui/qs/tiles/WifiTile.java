package com.android.systemui.qs.tiles;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import androidx.appcompat.R$styleable;
import com.android.internal.logging.MetricsLogger;
import com.android.settingslib.wifi.AccessPoint;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0021R$string;
import com.android.systemui.Dependency;
import com.android.systemui.controlcenter.policy.SlaveWifiHelper;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.qs.DetailAdapter;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.MiuiQSDetailItems;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.qs.tiles.WifiTile;
import com.android.systemui.statusbar.policy.NetworkController;
import com.miui.systemui.util.HapticFeedBackImpl;
import java.util.List;

public class WifiTile extends QSTileImpl<QSTile.SignalState> {
    /* access modifiers changed from: private */
    public static final Intent WIFI_SETTINGS = new Intent("android.settings.WIFI_SETTINGS");
    private boolean mConnected;
    protected final NetworkController mController;
    /* access modifiers changed from: private */
    public final WifiDetailAdapter mDetailAdapter;
    protected final WifiSignalCallback mSignalCallback = new WifiSignalCallback();
    /* access modifiers changed from: private */
    public SlaveWifiHelper mSlaveWifiHelper;
    private final QSTile.SignalState mStateBeforeClick = newTileState();
    /* access modifiers changed from: private */
    public boolean mTransientEnabling;
    /* access modifiers changed from: private */
    public final NetworkController.AccessPointController mWifiController;
    /* access modifiers changed from: private */
    public boolean mWifiEnabled;

    public int getMetricsCategory() {
        return R$styleable.AppCompatTheme_windowNoTitle;
    }

    public WifiTile(QSHost qSHost, NetworkController networkController, ActivityStarter activityStarter, SlaveWifiHelper slaveWifiHelper) {
        super(qSHost);
        this.mController = networkController;
        this.mWifiController = networkController.getAccessPointController();
        this.mDetailAdapter = (WifiDetailAdapter) createDetailAdapter();
        this.mController.observe(getLifecycle(), this.mSignalCallback);
        this.mSlaveWifiHelper = slaveWifiHelper;
    }

    public QSTile.SignalState newTileState() {
        return new QSTile.SignalState();
    }

    public void setDetailListening(boolean z) {
        this.mHandler.post(new Runnable(z) {
            public final /* synthetic */ boolean f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                WifiTile.this.lambda$setDetailListening$0$WifiTile(this.f$1);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$setDetailListening$0 */
    public /* synthetic */ void lambda$setDetailListening$0$WifiTile(boolean z) {
        if (z) {
            this.mWifiController.addAccessPointCallback(this.mDetailAdapter);
        } else {
            this.mWifiController.removeAccessPointCallback(this.mDetailAdapter);
        }
    }

    public DetailAdapter getDetailAdapter() {
        return this.mDetailAdapter;
    }

    /* access modifiers changed from: protected */
    public DetailAdapter createDetailAdapter() {
        return new WifiDetailAdapter();
    }

    public Intent getLongClickIntent() {
        return WIFI_SETTINGS;
    }

    /* access modifiers changed from: protected */
    public void handleClick() {
        TState tstate = this.mState;
        if (((QSTile.SignalState) tstate).isTransient) {
            Log.d(this.TAG, "handleClick: not ready, ignore");
            return;
        }
        ((QSTile.SignalState) tstate).copyTo(this.mStateBeforeClick);
        boolean z = !((QSTile.SignalState) this.mState).value;
        this.mWifiEnabled = z;
        this.mController.setWifiEnabled(z);
        refreshState(this.mWifiEnabled ? QSTileImpl.ARG_SHOW_TRANSIENT_ENABLING : null);
    }

    /* access modifiers changed from: protected */
    public void handleSecondaryClick() {
        if (!this.mWifiController.canConfigWifi()) {
            postStartActivityDismissingKeyguard(new Intent("android.settings.WIFI_SETTINGS"), 0);
            return;
        }
        showDetail(true);
        ((HapticFeedBackImpl) Dependency.get(HapticFeedBackImpl.class)).hapticFeedback("popup_normal", false);
        if (!((QSTile.SignalState) this.mState).value) {
            this.mController.setWifiEnabled(true);
        }
        refreshState();
        this.mDetailAdapter.updateItems();
    }

    public CharSequence getTileLabel() {
        return this.mContext.getString(C0021R$string.quick_settings_wifi_label);
    }

    /* access modifiers changed from: protected */
    public void handleUpdateState(QSTile.SignalState signalState, Object obj) {
        int i;
        if (QSTileImpl.DEBUG) {
            Log.d(this.TAG, "handleUpdateState arg=" + obj);
        }
        CallbackInfo callbackInfo = this.mSignalCallback.mInfo;
        Object obj2 = QSTileImpl.ARG_SHOW_TRANSIENT_ENABLING;
        boolean z = callbackInfo.isTransient && this.mWifiEnabled;
        boolean z2 = callbackInfo.isTransient && !this.mWifiEnabled;
        signalState.value = (this.mWifiEnabled && callbackInfo.enabled && !callbackInfo.isTransient) || (!this.mWifiEnabled && (callbackInfo.enabled || callbackInfo.isTransient));
        signalState.isTransient = this.mTransientEnabling || callbackInfo.isTransient;
        boolean z3 = callbackInfo.enabled && callbackInfo.wifiSignalIconId > 0 && callbackInfo.ssid != null;
        boolean z4 = callbackInfo.wifiSignalIconId > 0 && callbackInfo.ssid == null;
        this.mConnected = z3;
        int i2 = 2;
        signalState.state = 2;
        signalState.dualTarget = true;
        if (!z && !z2) {
            if ((signalState.value != callbackInfo.enabled) && isShowingDetail()) {
                fireToggleStateChanged(callbackInfo.enabled);
            }
            signalState.value = callbackInfo.enabled;
        }
        if (isShowingDetail()) {
            this.mDetailAdapter.setItemsVisible(callbackInfo.enabled);
        }
        StringBuffer stringBuffer = new StringBuffer();
        Resources resources = this.mContext.getResources();
        if (signalState.isTransient) {
            signalState.label = resources.getString(C0021R$string.quick_settings_wifi_label);
        } else if (!signalState.value) {
            signalState.label = resources.getString(C0021R$string.quick_settings_wifi_label);
        } else if (z3) {
            signalState.label = removeDoubleQuotes(callbackInfo.ssid);
        } else if (z4) {
            signalState.label = resources.getString(C0021R$string.quick_settings_wifi_label);
        } else {
            signalState.label = resources.getString(C0021R$string.quick_settings_wifi_label);
        }
        stringBuffer.append(this.mContext.getString(C0021R$string.quick_settings_wifi_label));
        stringBuffer.append(",");
        stringBuffer.append(this.mContext.getString(signalState.value ? C0021R$string.switch_bar_on : C0021R$string.switch_bar_off));
        stringBuffer.append(",");
        if (signalState.value && z3) {
            stringBuffer.append(callbackInfo.wifiSignalContentDescription);
            stringBuffer.append(",");
            stringBuffer.append(removeDoubleQuotes(callbackInfo.ssid));
        }
        if (!signalState.value) {
            i2 = 1;
        }
        signalState.state = i2;
        if (signalState.value) {
            i = C0013R$drawable.ic_qs_wifi_on;
        } else {
            i = C0013R$drawable.ic_qs_wifi_off;
        }
        signalState.icon = QSTileImpl.ResourceIcon.get(i);
        signalState.contentDescription = stringBuffer.toString();
        signalState.dualLabelContentDescription = resources.getString(C0021R$string.accessibility_quick_settings_open_settings, new Object[]{getTileLabel()});
        signalState.expandedAccessibilityClassName = Switch.class.getName();
    }

    /* access modifiers changed from: protected */
    public boolean shouldAnnouncementBeDelayed() {
        return this.mStateBeforeClick.value == ((QSTile.SignalState) this.mState).value;
    }

    /* access modifiers changed from: protected */
    public String composeChangeAnnouncement() {
        if (((QSTile.SignalState) this.mState).value) {
            return this.mContext.getString(C0021R$string.accessibility_quick_settings_wifi_changed_on);
        }
        return this.mContext.getString(C0021R$string.accessibility_quick_settings_wifi_changed_off);
    }

    public boolean isAvailable() {
        return this.mContext.getPackageManager().hasSystemFeature("android.hardware.wifi");
    }

    private static String removeDoubleQuotes(String str) {
        if (str == null) {
            return null;
        }
        int length = str.length();
        if (length <= 1 || str.charAt(0) != '\"') {
            return str;
        }
        int i = length - 1;
        return str.charAt(i) == '\"' ? str.substring(1, i) : str;
    }

    public boolean isConnected() {
        return this.mConnected;
    }

    protected static final class CallbackInfo {
        boolean activityIn;
        boolean activityOut;
        boolean connected;
        boolean enabled;
        boolean isTransient;
        String ssid;
        String wifiSignalContentDescription;
        int wifiSignalIconId;

        protected CallbackInfo() {
        }

        public String toString() {
            return "CallbackInfo[" + "enabled=" + this.enabled + ",connected=" + this.connected + ",wifiSignalIconId=" + this.wifiSignalIconId + ",ssid=" + this.ssid + ",activityIn=" + this.activityIn + ",activityOut=" + this.activityOut + ",wifiSignalContentDescription=" + this.wifiSignalContentDescription + ",isTransient=" + this.isTransient + ']';
        }

        public boolean isChanged(boolean z, NetworkController.IconState iconState, boolean z2, boolean z3, String str, boolean z4) {
            boolean z5;
            this.activityIn = z2;
            this.activityOut = z3;
            if (this.enabled != z) {
                Log.d("WifiTile", "isChanged: enabled from: " + this.enabled + ", to: " + z);
                this.enabled = z;
                z5 = true;
            } else {
                z5 = false;
            }
            if (this.connected != iconState.visible) {
                Log.d("WifiTile", "isChanged: connected from: " + this.connected + ", to: " + iconState.visible);
                this.connected = iconState.visible;
                z5 = true;
            }
            if (this.isTransient != z4) {
                Log.d("WifiTile", "isChanged: isTransient from: " + this.isTransient + ", to: " + z4);
                this.isTransient = z4;
                z5 = true;
            }
            int i = this.wifiSignalIconId;
            int i2 = iconState.icon;
            if (i != i2) {
                this.wifiSignalIconId = i2;
                z5 = true;
            }
            if (!TextUtils.equals(this.ssid, str)) {
                this.ssid = str;
                z5 = true;
            }
            if (TextUtils.equals(this.wifiSignalContentDescription, iconState.contentDescription)) {
                return z5;
            }
            this.wifiSignalContentDescription = iconState.contentDescription;
            return true;
        }
    }

    protected final class WifiSignalCallback implements NetworkController.SignalCallback {
        final CallbackInfo mInfo = new CallbackInfo();

        protected WifiSignalCallback() {
        }

        public void setWifiIndicators(boolean z, NetworkController.IconState iconState, NetworkController.IconState iconState2, boolean z2, boolean z3, String str, boolean z4, String str2) {
            if (QSTileImpl.DEBUG) {
                String access$200 = WifiTile.this.TAG;
                Log.d(access$200, "onWifiSignalChanged enabled=" + z);
            }
            boolean unused = WifiTile.this.mWifiEnabled = z;
            if (this.mInfo.isChanged(z, iconState2, z2, z3, str, z4)) {
                if (WifiTile.this.isShowingDetail()) {
                    WifiTile.this.mDetailAdapter.updateItems();
                    WifiTile wifiTile = WifiTile.this;
                    wifiTile.fireToggleStateChanged(wifiTile.mWifiEnabled);
                }
                if (!WifiTile.this.mTransientEnabling || !z4) {
                    WifiTile.this.refreshState();
                } else {
                    Log.d(WifiTile.this.TAG, "setWifiIndicators: ignore when enabling state is not ready");
                }
            } else if (QSTileImpl.DEBUG) {
                Log.d(WifiTile.this.TAG, "setWifiIndicators: ignore in/out info change");
            }
        }
    }

    protected class WifiDetailAdapter implements DetailAdapter, NetworkController.AccessPointController.AccessPointCallback, MiuiQSDetailItems.Callback {
        private AccessPoint[] mAccessPoints;
        private MiuiQSDetailItems mItems;

        public int getMetricsCategory() {
            return 152;
        }

        public boolean getToggleEnabled() {
            return true;
        }

        public boolean hasHeader() {
            return true;
        }

        public void onDetailItemDisconnect(MiuiQSDetailItems.Item item) {
        }

        protected WifiDetailAdapter() {
        }

        public CharSequence getTitle() {
            return WifiTile.this.mContext.getString(C0021R$string.quick_settings_wifi_label);
        }

        public Intent getSettingsIntent() {
            return WifiTile.WIFI_SETTINGS;
        }

        public Boolean getToggleState() {
            return Boolean.valueOf(((QSTile.SignalState) WifiTile.this.mState).value);
        }

        public void setToggleState(boolean z) {
            if (QSTileImpl.DEBUG) {
                String access$1400 = WifiTile.this.TAG;
                Log.d(access$1400, "setToggleState " + z);
            }
            MetricsLogger.action(WifiTile.this.mContext, 153, z);
            WifiTile.this.mController.setWifiEnabled(z);
        }

        public View createDetailView(Context context, View view, ViewGroup viewGroup) {
            if (QSTileImpl.DEBUG) {
                String access$1700 = WifiTile.this.TAG;
                StringBuilder sb = new StringBuilder();
                sb.append("createDetailView convertView=");
                sb.append(view != null);
                Log.d(access$1700, sb.toString());
            }
            this.mAccessPoints = null;
            MiuiQSDetailItems convertOrInflate = MiuiQSDetailItems.convertOrInflate(context, view, viewGroup);
            this.mItems = convertOrInflate;
            convertOrInflate.setTagSuffix("Wifi");
            this.mItems.setCallback(this);
            WifiTile.this.mWifiController.scanForAccessPoints();
            setItemsVisible(((QSTile.SignalState) WifiTile.this.mState).value);
            return this.mItems;
        }

        public void onAccessPointsChanged(List<AccessPoint> list) {
            this.mAccessPoints = (AccessPoint[]) list.toArray(new AccessPoint[list.size()]);
            filterUnreachableAPs();
            WifiTile.this.mHandler.post(new Runnable() {
                public final void run() {
                    WifiTile.WifiDetailAdapter.this.updateItems();
                }
            });
        }

        private void filterUnreachableAPs() {
            int i = 0;
            for (AccessPoint isReachable : this.mAccessPoints) {
                if (isReachable.isReachable()) {
                    i++;
                }
            }
            AccessPoint[] accessPointArr = this.mAccessPoints;
            if (i != accessPointArr.length) {
                this.mAccessPoints = new AccessPoint[i];
                int i2 = 0;
                for (AccessPoint accessPoint : accessPointArr) {
                    if (accessPoint.isReachable()) {
                        this.mAccessPoints[i2] = accessPoint;
                        i2++;
                    }
                }
            }
        }

        public void onSettingsActivityTriggered(Intent intent) {
            WifiTile.this.postStartActivityDismissingKeyguard(intent, 0);
        }

        public void onDetailItemClick(MiuiQSDetailItems.Item item) {
            Object obj;
            if (item != null && (obj = item.tag) != null) {
                AccessPoint accessPoint = (AccessPoint) obj;
                if (!accessPoint.isActive()) {
                    WifiTile.this.mSlaveWifiHelper.connect(WifiTile.this.mContext, accessPoint, WifiTile.this.mWifiController);
                    if (!accessPoint.isSaved() || accessPoint.getSecurity() == 0) {
                        WifiTile.this.mHost.collapsePanels();
                        WifiTile.this.showDetail(false);
                    }
                    ((HapticFeedBackImpl) Dependency.get(HapticFeedBackImpl.class)).meshNormal();
                }
                this.mItems.setItemClicked(true);
            }
        }

        public void setItemsVisible(boolean z) {
            MiuiQSDetailItems miuiQSDetailItems = this.mItems;
            if (miuiQSDetailItems != null) {
                miuiQSDetailItems.setItemsVisible(z);
            }
        }

        /* access modifiers changed from: private */
        /* JADX WARNING: Removed duplicated region for block: B:13:0x002b  */
        /* JADX WARNING: Removed duplicated region for block: B:15:0x003b  */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void updateItems() {
            /*
                r11 = this;
                com.android.systemui.qs.MiuiQSDetailItems r0 = r11.mItems
                if (r0 != 0) goto L_0x0005
                return
            L_0x0005:
                com.android.settingslib.wifi.AccessPoint[] r0 = r11.mAccessPoints
                r1 = 0
                if (r0 == 0) goto L_0x000d
                int r2 = r0.length
                if (r2 > 0) goto L_0x0017
            L_0x000d:
                com.android.systemui.qs.tiles.WifiTile r2 = com.android.systemui.qs.tiles.WifiTile.this
                com.android.systemui.qs.tiles.WifiTile$WifiSignalCallback r3 = r2.mSignalCallback
                com.android.systemui.qs.tiles.WifiTile$CallbackInfo r3 = r3.mInfo
                boolean r3 = r3.enabled
                if (r3 != 0) goto L_0x001d
            L_0x0017:
                com.android.systemui.qs.tiles.WifiTile r2 = com.android.systemui.qs.tiles.WifiTile.this
                r2.fireScanStateChanged(r1)
                goto L_0x0021
            L_0x001d:
                r3 = 1
                r2.fireScanStateChanged(r3)
            L_0x0021:
                com.android.systemui.qs.tiles.WifiTile r2 = com.android.systemui.qs.tiles.WifiTile.this
                com.android.systemui.qs.tiles.WifiTile$WifiSignalCallback r2 = r2.mSignalCallback
                com.android.systemui.qs.tiles.WifiTile$CallbackInfo r2 = r2.mInfo
                boolean r2 = r2.enabled
                if (r2 != 0) goto L_0x003b
                com.android.systemui.qs.MiuiQSDetailItems r0 = r11.mItems
                int r1 = com.android.systemui.C0013R$drawable.ic_qs_wifi_detail_empty
                int r2 = com.android.systemui.C0021R$string.wifi_is_off
                r0.setEmptyState(r1, r2)
                com.android.systemui.qs.MiuiQSDetailItems r11 = r11.mItems
                r0 = 0
                r11.setItems(r0)
                return
            L_0x003b:
                com.android.systemui.qs.MiuiQSDetailItems r2 = r11.mItems
                int r3 = com.android.systemui.C0013R$drawable.ic_qs_wifi_detail_empty
                int r4 = com.android.systemui.C0021R$string.quick_settings_wifi_detail_empty_text
                r2.setEmptyState(r3, r4)
                java.util.ArrayList r2 = new java.util.ArrayList
                r2.<init>()
                if (r0 == 0) goto L_0x006c
                r3 = r1
            L_0x004c:
                int r4 = r0.length
                if (r3 >= r4) goto L_0x006c
                r10 = r0[r3]
                com.android.systemui.qs.tiles.WifiTile r4 = com.android.systemui.qs.tiles.WifiTile.this
                com.android.systemui.controlcenter.policy.SlaveWifiHelper r5 = r4.mSlaveWifiHelper
                com.android.systemui.qs.tiles.WifiTile r4 = com.android.systemui.qs.tiles.WifiTile.this
                android.content.Context r6 = r4.mContext
                com.android.systemui.qs.tiles.WifiTile r4 = com.android.systemui.qs.tiles.WifiTile.this
                com.android.systemui.statusbar.policy.NetworkController$AccessPointController r7 = r4.mWifiController
                com.android.systemui.qs.MiuiQSDetailItems r8 = r11.mItems
                r9 = r2
                r5.updateItem(r6, r7, r8, r9, r10)
                int r3 = r3 + 1
                goto L_0x004c
            L_0x006c:
                com.android.systemui.qs.MiuiQSDetailItems r11 = r11.mItems
                com.android.systemui.qs.MiuiQSDetailItems$Item[] r0 = new com.android.systemui.qs.MiuiQSDetailItems.Item[r1]
                java.lang.Object[] r0 = r2.toArray(r0)
                com.android.systemui.qs.MiuiQSDetailItems$Item[] r0 = (com.android.systemui.qs.MiuiQSDetailItems.Item[]) r0
                r11.setItems(r0)
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.qs.tiles.WifiTile.WifiDetailAdapter.updateItems():void");
        }
    }
}
