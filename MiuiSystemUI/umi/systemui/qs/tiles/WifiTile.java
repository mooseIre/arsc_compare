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
import com.android.systemui.C0010R$drawable;
import com.android.systemui.C0018R$string;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.qs.DetailAdapter;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.MiuiQSDetailItems;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
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

    public WifiTile(QSHost qSHost, NetworkController networkController, ActivityStarter activityStarter) {
        super(qSHost);
        this.mController = networkController;
        this.mWifiController = networkController.getAccessPointController();
        this.mDetailAdapter = (WifiDetailAdapter) createDetailAdapter();
        this.mController.observe(getLifecycle(), this.mSignalCallback);
    }

    public QSTile.SignalState newTileState() {
        return new QSTile.SignalState();
    }

    public void setDetailListening(boolean z) {
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
        return this.mContext.getString(C0018R$string.quick_settings_wifi_label);
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
            signalState.label = resources.getString(C0018R$string.quick_settings_wifi_label);
        } else if (!signalState.value) {
            signalState.label = resources.getString(C0018R$string.quick_settings_wifi_label);
        } else if (z3) {
            signalState.label = removeDoubleQuotes(callbackInfo.ssid);
        } else if (z4) {
            signalState.label = resources.getString(C0018R$string.quick_settings_wifi_label);
        } else {
            signalState.label = resources.getString(C0018R$string.quick_settings_wifi_label);
        }
        stringBuffer.append(this.mContext.getString(C0018R$string.quick_settings_wifi_label));
        stringBuffer.append(",");
        stringBuffer.append(this.mContext.getString(signalState.value ? C0018R$string.switch_bar_on : C0018R$string.switch_bar_off));
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
            i = C0010R$drawable.ic_qs_wifi_on;
        } else {
            i = C0010R$drawable.ic_qs_wifi_off;
        }
        signalState.icon = QSTileImpl.ResourceIcon.get(i);
        signalState.contentDescription = stringBuffer.toString();
        signalState.dualLabelContentDescription = resources.getString(C0018R$string.accessibility_quick_settings_open_settings, new Object[]{getTileLabel()});
        signalState.expandedAccessibilityClassName = Switch.class.getName();
    }

    /* access modifiers changed from: protected */
    public boolean shouldAnnouncementBeDelayed() {
        return this.mStateBeforeClick.value == ((QSTile.SignalState) this.mState).value;
    }

    /* access modifiers changed from: protected */
    public String composeChangeAnnouncement() {
        if (((QSTile.SignalState) this.mState).value) {
            return this.mContext.getString(C0018R$string.accessibility_quick_settings_wifi_changed_on);
        }
        return this.mContext.getString(C0018R$string.accessibility_quick_settings_wifi_changed_off);
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
            return WifiTile.this.mContext.getString(C0018R$string.quick_settings_wifi_label);
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
            updateItems();
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
                    WifiTile.this.mWifiController.connect(accessPoint);
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
        /* JADX WARNING: Removed duplicated region for block: B:13:0x002c  */
        /* JADX WARNING: Removed duplicated region for block: B:15:0x003b  */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void updateItems() {
            /*
                r6 = this;
                com.android.systemui.qs.MiuiQSDetailItems r0 = r6.mItems
                if (r0 != 0) goto L_0x0005
                return
            L_0x0005:
                com.android.settingslib.wifi.AccessPoint[] r0 = r6.mAccessPoints
                r1 = 0
                if (r0 == 0) goto L_0x000d
                int r0 = r0.length
                if (r0 > 0) goto L_0x0017
            L_0x000d:
                com.android.systemui.qs.tiles.WifiTile r0 = com.android.systemui.qs.tiles.WifiTile.this
                com.android.systemui.qs.tiles.WifiTile$WifiSignalCallback r2 = r0.mSignalCallback
                com.android.systemui.qs.tiles.WifiTile$CallbackInfo r2 = r2.mInfo
                boolean r2 = r2.enabled
                if (r2 != 0) goto L_0x001d
            L_0x0017:
                com.android.systemui.qs.tiles.WifiTile r0 = com.android.systemui.qs.tiles.WifiTile.this
                r0.fireScanStateChanged(r1)
                goto L_0x0021
            L_0x001d:
                r2 = 1
                r0.fireScanStateChanged(r2)
            L_0x0021:
                com.android.systemui.qs.tiles.WifiTile r0 = com.android.systemui.qs.tiles.WifiTile.this
                com.android.systemui.qs.tiles.WifiTile$WifiSignalCallback r0 = r0.mSignalCallback
                com.android.systemui.qs.tiles.WifiTile$CallbackInfo r0 = r0.mInfo
                boolean r0 = r0.enabled
                r2 = 0
                if (r0 != 0) goto L_0x003b
                com.android.systemui.qs.MiuiQSDetailItems r0 = r6.mItems
                int r1 = com.android.systemui.C0010R$drawable.ic_qs_wifi_detail_empty
                int r3 = com.android.systemui.C0018R$string.wifi_is_off
                r0.setEmptyState(r1, r3)
                com.android.systemui.qs.MiuiQSDetailItems r6 = r6.mItems
                r6.setItems(r2)
                return
            L_0x003b:
                com.android.systemui.qs.MiuiQSDetailItems r0 = r6.mItems
                int r3 = com.android.systemui.C0010R$drawable.ic_qs_wifi_detail_empty
                int r4 = com.android.systemui.C0018R$string.quick_settings_wifi_detail_empty_text
                r0.setEmptyState(r3, r4)
                com.android.settingslib.wifi.AccessPoint[] r0 = r6.mAccessPoints
                if (r0 == 0) goto L_0x009b
                int r0 = r0.length
                com.android.systemui.qs.MiuiQSDetailItems$Item[] r0 = new com.android.systemui.qs.MiuiQSDetailItems.Item[r0]
            L_0x004b:
                com.android.settingslib.wifi.AccessPoint[] r3 = r6.mAccessPoints
                int r4 = r3.length
                if (r1 >= r4) goto L_0x009a
                r3 = r3[r1]
                com.android.systemui.qs.MiuiQSDetailItems r4 = r6.mItems
                com.android.systemui.qs.MiuiQSDetailItems$Item r4 = r4.acquireItem()
                com.android.systemui.qs.tiles.WifiTile r5 = com.android.systemui.qs.tiles.WifiTile.this
                com.android.systemui.statusbar.policy.NetworkController$AccessPointController r5 = r5.mWifiController
                int r5 = r5.getIcon(r3)
                r4.icon = r5
                r4.tag = r3
                boolean r5 = r3.isActive()
                r4.selected = r5
                java.lang.CharSequence r5 = r3.getSsid()
                r4.line1 = r5
                boolean r5 = r3.isActive()
                if (r5 == 0) goto L_0x007d
                java.lang.String r5 = r3.getSummary()
                goto L_0x007e
            L_0x007d:
                r5 = r2
            L_0x007e:
                r4.line2 = r5
                boolean r5 = r4.selected
                if (r5 == 0) goto L_0x0089
                int r3 = com.android.systemui.C0010R$drawable.ic_qs_detail_item_selected
                r4.icon2 = r3
                goto L_0x0095
            L_0x0089:
                int r3 = r3.getSecurity()
                if (r3 == 0) goto L_0x0092
                int r3 = com.android.systemui.C0010R$drawable.ic_qs_wifi_lock
                goto L_0x0093
            L_0x0092:
                r3 = -1
            L_0x0093:
                r4.icon2 = r3
            L_0x0095:
                r0[r1] = r4
                int r1 = r1 + 1
                goto L_0x004b
            L_0x009a:
                r2 = r0
            L_0x009b:
                com.android.systemui.qs.MiuiQSDetailItems r6 = r6.mItems
                r6.setItems(r2)
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.qs.tiles.WifiTile.WifiDetailAdapter.updateItems():void");
        }
    }
}
