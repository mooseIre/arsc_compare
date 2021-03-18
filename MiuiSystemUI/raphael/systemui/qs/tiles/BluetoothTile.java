package com.android.systemui.qs.tiles;

import android.bluetooth.BluetoothClass;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import com.android.internal.logging.MetricsLogger;
import com.android.settingslib.bluetooth.CachedBluetoothDevice;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0021R$string;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.qs.DetailAdapter;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.MiuiQSDetailItems;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.statusbar.policy.BluetoothController;
import java.util.ArrayList;
import java.util.Collection;

public class BluetoothTile extends QSTileImpl<QSTile.BooleanState> {
    private static final Intent BLUETOOTH_SETTINGS = new Intent("android.settings.BLUETOOTH_SETTINGS");
    private final ActivityStarter mActivityStarter;
    private final BluetoothController.Callback mCallback = new BluetoothController.Callback() {
        /* class com.android.systemui.qs.tiles.BluetoothTile.AnonymousClass1 */

        @Override // com.android.systemui.statusbar.policy.BluetoothController.Callback
        public void onBluetoothStateChange(boolean z) {
            BluetoothTile.this.refreshState();
            if (BluetoothTile.this.isShowingDetail()) {
                BluetoothTile.this.mDetailAdapter.updateItems();
                BluetoothTile bluetoothTile = BluetoothTile.this;
                bluetoothTile.fireToggleStateChanged(bluetoothTile.mDetailAdapter.getToggleState().booleanValue());
            }
        }

        @Override // com.android.systemui.statusbar.policy.BluetoothController.Callback
        public void onBluetoothDevicesChanged() {
            BluetoothTile.this.refreshState();
            if (BluetoothTile.this.isShowingDetail()) {
                BluetoothTile.this.mDetailAdapter.updateItems();
            }
        }
    };
    private final BluetoothController mController;
    private final BluetoothDetailAdapter mDetailAdapter;

    @Override // com.android.systemui.plugins.qs.QSTile, com.android.systemui.qs.tileimpl.QSTileImpl
    public int getMetricsCategory() {
        return 113;
    }

    public BluetoothTile(QSHost qSHost, BluetoothController bluetoothController, ActivityStarter activityStarter) {
        super(qSHost);
        this.mController = bluetoothController;
        this.mActivityStarter = activityStarter;
        this.mDetailAdapter = (BluetoothDetailAdapter) createDetailAdapter();
        this.mController.observe(getLifecycle(), this.mCallback);
    }

    @Override // com.android.systemui.plugins.qs.QSTile, com.android.systemui.qs.tileimpl.QSTileImpl
    public DetailAdapter getDetailAdapter() {
        return this.mDetailAdapter;
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleClick() {
        Object obj;
        if (this.mController.isBluetoothReady()) {
            TState tstate = this.mState;
            if (!((QSTile.BooleanState) tstate).isTransient) {
                boolean z = ((QSTile.BooleanState) tstate).value;
                if (z) {
                    obj = null;
                } else {
                    obj = QSTileImpl.ARG_SHOW_TRANSIENT_ENABLING;
                }
                refreshState(obj);
                this.mController.setBluetoothEnabled(!z);
                return;
            }
        }
        Log.d(this.TAG, "handleClick: bluetooth not ready");
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public Intent getLongClickIntent() {
        return new Intent("android.settings.BLUETOOTH_SETTINGS");
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleSecondaryClick() {
        if (!this.mController.canConfigBluetooth()) {
            this.mActivityStarter.postStartActivityDismissingKeyguard(new Intent("android.settings.BLUETOOTH_SETTINGS"), 0);
            return;
        }
        showDetail(true);
        if (!((QSTile.BooleanState) this.mState).value) {
            this.mController.setBluetoothEnabled(true);
        }
    }

    @Override // com.android.systemui.plugins.qs.QSTile
    public CharSequence getTileLabel() {
        return this.mContext.getString(C0021R$string.quick_settings_bluetooth_label);
    }

    public boolean isConnected() {
        return this.mController.isBluetoothConnected();
    }

    /* access modifiers changed from: protected */
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        int i;
        boolean z = obj == QSTileImpl.ARG_SHOW_TRANSIENT_ENABLING;
        boolean z2 = z || this.mController.isBluetoothEnabled();
        boolean isBluetoothConnected = this.mController.isBluetoothConnected();
        this.mController.isBluetoothConnecting();
        booleanState.isTransient = z || this.mController.getBluetoothState() == 11;
        booleanState.dualTarget = true;
        booleanState.value = z2;
        booleanState.label = this.mContext.getString(C0021R$string.quick_settings_bluetooth_label);
        if (booleanState.value && isShowingDetail()) {
            this.mDetailAdapter.setToggleState(true);
        }
        if (!z2) {
            booleanState.contentDescription = ((Object) booleanState.label) + "," + this.mContext.getString(C0021R$string.switch_bar_off);
        } else if (isBluetoothConnected) {
            booleanState.contentDescription = this.mContext.getString(C0021R$string.accessibility_bluetooth_name, booleanState.label);
            booleanState.label = this.mController.getLastDeviceName();
        } else if (booleanState.isTransient) {
            booleanState.contentDescription = this.mContext.getString(C0021R$string.accessibility_quick_settings_bluetooth_connecting);
        } else {
            booleanState.contentDescription = ((Object) booleanState.label) + "," + this.mContext.getString(C0021R$string.switch_bar_on) + "," + this.mContext.getString(C0021R$string.accessibility_not_connected);
        }
        booleanState.state = booleanState.value ? 2 : 1;
        if (booleanState.value) {
            i = C0013R$drawable.ic_qs_bluetooth_on;
        } else {
            i = C0013R$drawable.ic_qs_bluetooth_off;
        }
        booleanState.icon = QSTileImpl.ResourceIcon.get(i);
        booleanState.dualLabelContentDescription = this.mContext.getResources().getString(C0021R$string.accessibility_quick_settings_open_settings, getTileLabel());
        booleanState.expandedAccessibilityClassName = Switch.class.getName();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public String composeChangeAnnouncement() {
        if (((QSTile.BooleanState) this.mState).value) {
            return this.mContext.getString(C0021R$string.accessibility_quick_settings_bluetooth_changed_on);
        }
        return this.mContext.getString(C0021R$string.accessibility_quick_settings_bluetooth_changed_off);
    }

    @Override // com.android.systemui.plugins.qs.QSTile, com.android.systemui.qs.tileimpl.QSTileImpl
    public boolean isAvailable() {
        return this.mController.isBluetoothSupported();
    }

    /* access modifiers changed from: protected */
    public DetailAdapter createDetailAdapter() {
        return new BluetoothDetailAdapter();
    }

    /* access modifiers changed from: protected */
    public class BluetoothDetailAdapter implements DetailAdapter, MiuiQSDetailItems.Callback {
        private MiuiQSDetailItems mItems;

        @Override // com.android.systemui.plugins.qs.DetailAdapter
        public int getMetricsCategory() {
            return 150;
        }

        protected BluetoothDetailAdapter() {
        }

        @Override // com.android.systemui.plugins.qs.DetailAdapter
        public CharSequence getTitle() {
            return ((QSTileImpl) BluetoothTile.this).mContext.getString(C0021R$string.quick_settings_bluetooth_label);
        }

        @Override // com.android.systemui.plugins.qs.DetailAdapter
        public Boolean getToggleState() {
            return Boolean.valueOf(((QSTile.BooleanState) ((QSTileImpl) BluetoothTile.this).mState).value);
        }

        @Override // com.android.systemui.plugins.qs.DetailAdapter
        public boolean getToggleEnabled() {
            return BluetoothTile.this.mController.getBluetoothState() == 10 || BluetoothTile.this.mController.getBluetoothState() == 12;
        }

        @Override // com.android.systemui.plugins.qs.DetailAdapter
        public Intent getSettingsIntent() {
            return BluetoothTile.BLUETOOTH_SETTINGS;
        }

        @Override // com.android.systemui.plugins.qs.DetailAdapter
        public void setToggleState(boolean z) {
            MetricsLogger.action(((QSTileImpl) BluetoothTile.this).mContext, 154, z);
            BluetoothTile.this.mController.setBluetoothEnabled(z);
        }

        @Override // com.android.systemui.plugins.qs.DetailAdapter
        public View createDetailView(Context context, View view, ViewGroup viewGroup) {
            MiuiQSDetailItems convertOrInflate = MiuiQSDetailItems.convertOrInflate(context, view, viewGroup);
            this.mItems = convertOrInflate;
            convertOrInflate.setTagSuffix("Bluetooth");
            this.mItems.setCallback(this);
            if (BluetoothTile.this.isShowingDetail()) {
                updateItems();
            }
            return this.mItems;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void updateItems() {
            if (this.mItems != null) {
                if (BluetoothTile.this.mController.isBluetoothEnabled()) {
                    ArrayList arrayList = new ArrayList();
                    Collection<CachedBluetoothDevice> devices = BluetoothTile.this.mController.getDevices();
                    if (devices != null) {
                        int i = 0;
                        int i2 = 0;
                        for (CachedBluetoothDevice cachedBluetoothDevice : devices) {
                            if (BluetoothTile.this.mController.getBondState(cachedBluetoothDevice) != 10 && !BluetoothTile.this.mController.isBleAudioDevice(((QSTileImpl) BluetoothTile.this).mContext, cachedBluetoothDevice)) {
                                MiuiQSDetailItems.Item item = new MiuiQSDetailItems.Item();
                                item.icon = C0013R$drawable.ic_qs_bluetooth_on;
                                item.line1 = cachedBluetoothDevice.getName();
                                item.line2 = cachedBluetoothDevice.getConnectionSummary();
                                item.tag = cachedBluetoothDevice;
                                BluetoothClass btClass = cachedBluetoothDevice.getBtClass();
                                if (btClass != null) {
                                    if (btClass.doesClassMatch(0) || btClass.doesClassMatch(1)) {
                                        item.icon = C0013R$drawable.ic_qs_bluetooth_device_headset;
                                    } else {
                                        int majorDeviceClass = btClass.getMajorDeviceClass();
                                        if (majorDeviceClass == 0) {
                                            item.icon = C0013R$drawable.ic_qs_bluetooth_device_misc;
                                        } else if (majorDeviceClass == 256) {
                                            item.icon = C0013R$drawable.ic_qs_bluetooth_device_laptop;
                                        } else if (majorDeviceClass == 512) {
                                            item.icon = C0013R$drawable.ic_qs_bluetooth_device_cellphone;
                                        } else if (majorDeviceClass == 768) {
                                            item.icon = C0013R$drawable.ic_qs_bluetooth_device_network;
                                        } else if (majorDeviceClass != 1536) {
                                            item.icon = C0013R$drawable.ic_qs_bluetooth_device_common;
                                        } else {
                                            item.icon = C0013R$drawable.ic_qs_bluetooth_device_imaging;
                                        }
                                    }
                                }
                                int maxConnectionState = BluetoothTile.this.mController.getMaxConnectionState(cachedBluetoothDevice);
                                if (maxConnectionState == 2) {
                                    item.icon2 = C0013R$drawable.ic_qs_detail_item_selected;
                                    item.canDisconnect = true;
                                    item.selected = true;
                                    arrayList.add(i, item);
                                    i++;
                                } else if (maxConnectionState == 1) {
                                    item.icon2 = C0013R$drawable.ic_qs_bluetooth_connecting;
                                    arrayList.add(i, item);
                                } else {
                                    arrayList.add(item);
                                }
                                i2++;
                                if (i2 == 20) {
                                    break;
                                }
                            }
                        }
                    }
                    if (arrayList.size() == 0) {
                        this.mItems.setEmptyState(C0013R$drawable.ic_miui_qs_bluetooth_detail_empty, C0021R$string.quick_settings_bluetooth_detail_empty_text);
                    }
                    this.mItems.setItems((MiuiQSDetailItems.Item[]) arrayList.toArray(new MiuiQSDetailItems.Item[arrayList.size()]));
                    return;
                }
                this.mItems.setEmptyState(C0013R$drawable.ic_miui_qs_bluetooth_detail_empty, C0021R$string.bt_is_off);
                this.mItems.setItems(null);
            }
        }

        @Override // com.android.systemui.qs.MiuiQSDetailItems.Callback
        public void onDetailItemClick(MiuiQSDetailItems.Item item) {
            Object obj;
            CachedBluetoothDevice cachedBluetoothDevice;
            if (item != null && (obj = item.tag) != null && (cachedBluetoothDevice = (CachedBluetoothDevice) obj) != null) {
                if (cachedBluetoothDevice.isConnected()) {
                    setDeviceActive(cachedBluetoothDevice);
                } else {
                    BluetoothTile.this.mController.connect(cachedBluetoothDevice);
                }
                this.mItems.setItemClicked(true);
            }
        }

        public boolean setDeviceActive(CachedBluetoothDevice cachedBluetoothDevice) {
            if (cachedBluetoothDevice == null || cachedBluetoothDevice.isActiveDevice(2) || cachedBluetoothDevice.isActiveDevice(1) || cachedBluetoothDevice.isActiveDevice(21)) {
                return false;
            }
            return cachedBluetoothDevice.setActive();
        }

        @Override // com.android.systemui.qs.MiuiQSDetailItems.Callback
        public void onDetailItemDisconnect(MiuiQSDetailItems.Item item) {
            Object obj;
            CachedBluetoothDevice cachedBluetoothDevice;
            if (item != null && (obj = item.tag) != null && (cachedBluetoothDevice = (CachedBluetoothDevice) obj) != null) {
                BluetoothTile.this.mController.disconnect(cachedBluetoothDevice);
            }
        }

        @Override // com.android.systemui.plugins.qs.DetailAdapter
        public int getContainerHeight() {
            if (((QSTileImpl) BluetoothTile.this).mContext.getResources().getConfiguration().orientation == 1) {
                return ((QSTileImpl) BluetoothTile.this).mContext.getResources().getDimensionPixelSize(C0012R$dimen.qs_bluetooth_detail_height);
            }
            return -1;
        }
    }
}
