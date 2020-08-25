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
import com.android.systemui.Dependency;
import com.android.systemui.SystemUICompat;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.R;
import com.android.systemui.plugins.qs.DetailAdapter;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSDetailItems;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.statusbar.Icons;
import com.android.systemui.statusbar.policy.BluetoothController;
import java.util.ArrayList;
import java.util.Collection;

public class BluetoothTile extends QSTileImpl<QSTile.BooleanState> {
    /* access modifiers changed from: private */
    public static final Intent BLUETOOTH_SETTINGS = new Intent("android.settings.BLUETOOTH_SETTINGS");
    private final BluetoothController.Callback mCallback = new BluetoothController.Callback() {
        public void onBluetoothInoutStateChange(String str) {
        }

        public void onBluetoothStatePhoneChange() {
        }

        public void onBluetoothStateChange(final boolean z) {
            String access$400 = BluetoothTile.this.TAG;
            Log.d(access$400, "onBluetoothStateChange: enabled = " + z);
            BluetoothTile.this.mHandler.post(new Runnable() {
                public void run() {
                    boolean unused = BluetoothTile.this.mTargetEnable = z;
                    BluetoothTile.this.refreshState();
                }
            });
        }

        public void onBluetoothDevicesChanged() {
            if (BluetoothTile.this.isShowingDetail()) {
                BluetoothTile.this.mDetailAdapter.updateItems();
            }
        }
    };
    private boolean mConnnected;
    /* access modifiers changed from: private */
    public final BluetoothController mController = ((BluetoothController) Dependency.get(BluetoothController.class));
    /* access modifiers changed from: private */
    public final BluetoothDetailAdapter mDetailAdapter;
    /* access modifiers changed from: private */
    public boolean mTargetEnable;

    public int getMetricsCategory() {
        return R.styleable.AppCompatTheme_toolbarStyle;
    }

    public BluetoothTile(QSHost qSHost) {
        super(qSHost);
        ActivityStarter activityStarter = (ActivityStarter) Dependency.get(ActivityStarter.class);
        this.mDetailAdapter = (BluetoothDetailAdapter) createDetailAdapter();
        this.mHandler.post(new Runnable() {
            public void run() {
                BluetoothTile bluetoothTile = BluetoothTile.this;
                boolean unused = bluetoothTile.mTargetEnable = bluetoothTile.mController.isBluetoothEnabled();
            }
        });
    }

    public DetailAdapter getDetailAdapter() {
        return this.mDetailAdapter;
    }

    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    public void handleSetListening(boolean z) {
        if (z) {
            this.mController.addCallback(this.mCallback);
        } else {
            this.mController.removeCallback(this.mCallback);
        }
    }

    /* access modifiers changed from: protected */
    public void handleClick() {
        String str = this.TAG;
        StringBuilder sb = new StringBuilder();
        sb.append("handleClick: from: ");
        sb.append(this.mTargetEnable);
        sb.append(", to: ");
        sb.append(!this.mTargetEnable);
        Log.d(str, sb.toString());
        if (!this.mTargetEnable ? this.mController.getBluetoothState() == 10 : this.mController.getBluetoothState() == 12) {
            boolean z = !this.mTargetEnable;
            this.mTargetEnable = z;
            this.mController.setBluetoothEnabled(z);
            refreshState();
            return;
        }
        Log.d(this.TAG, "handleClick: bluetooth not ready");
    }

    public Intent getLongClickIntent() {
        return new Intent("android.settings.BLUETOOTH_SETTINGS");
    }

    /* access modifiers changed from: protected */
    public void handleSecondaryClick() {
        String str = this.TAG;
        Log.d(str, "handleSecondaryClick: canConfigBluetooth = " + this.mController.canConfigBluetooth());
        if (!this.mController.canConfigBluetooth()) {
            postStartActivityDismissingKeyguard(new Intent("android.settings.BLUETOOTH_SETTINGS"), 0);
            return;
        }
        this.mTargetEnable = true;
        this.mController.setBluetoothEnabled(true);
        showDetail(true);
        refreshState();
    }

    public CharSequence getTileLabel() {
        return this.mContext.getString(R.string.quick_settings_bluetooth_label);
    }

    /* access modifiers changed from: protected */
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        String str = this.TAG;
        Log.d(str, "handleUpdateState: mTargetEnable = " + this.mTargetEnable + "mController.getBluetoothState() = " + this.mController.getBluetoothState());
        if (isShowingDetail()) {
            fireToggleStateChanged(this.mTargetEnable);
            this.mUiHandler.post(new Runnable() {
                public void run() {
                    BluetoothTile.this.mDetailAdapter.updateItems();
                }
            });
        }
        booleanState.dualTarget = true;
        booleanState.value = this.mTargetEnable && this.mController.getBluetoothState() == 12;
        booleanState.label = this.mContext.getString(R.string.quick_settings_bluetooth_label);
        this.mConnnected = false;
        if (!booleanState.value) {
            booleanState.contentDescription = booleanState.label + "," + this.mContext.getString(R.string.switch_bar_off);
        } else if (this.mController.isBluetoothConnected()) {
            this.mConnnected = true;
            booleanState.contentDescription = this.mContext.getString(R.string.accessibility_bluetooth_name, new Object[]{booleanState.label});
            booleanState.label = this.mController.getLastDeviceName();
        } else if (this.mController.isBluetoothConnecting()) {
            booleanState.contentDescription = this.mContext.getString(R.string.accessibility_quick_settings_bluetooth_connecting);
        } else {
            booleanState.contentDescription = booleanState.label + "," + this.mContext.getString(R.string.switch_bar_on) + "," + this.mContext.getString(R.string.accessibility_not_connected);
        }
        if (isShowingDetail()) {
            this.mDetailAdapter.setItemsVisible(booleanState.value);
        }
        booleanState.state = booleanState.value ? 2 : 1;
        booleanState.withAnimation = this.mTargetEnable && this.mController.getBluetoothState() != 12;
        booleanState.icon = QSTileImpl.ResourceIcon.get(Icons.getQSIcons(Integer.valueOf(booleanState.value ? R.drawable.ic_qs_bluetooth_on : R.drawable.ic_qs_bluetooth_off), this.mInControlCenter));
        booleanState.dualLabelContentDescription = this.mContext.getResources().getString(R.string.accessibility_quick_settings_open_settings, new Object[]{getTileLabel()});
        booleanState.expandedAccessibilityClassName = Switch.class.getName();
    }

    /* access modifiers changed from: protected */
    public String composeChangeAnnouncement() {
        if (this.mTargetEnable) {
            return this.mContext.getString(R.string.accessibility_quick_settings_bluetooth_changed_on);
        }
        return this.mContext.getString(R.string.accessibility_quick_settings_bluetooth_changed_off);
    }

    public boolean isAvailable() {
        return this.mController.isBluetoothSupported();
    }

    public boolean isConnected() {
        return this.mConnnected;
    }

    /* access modifiers changed from: protected */
    public DetailAdapter createDetailAdapter() {
        return new BluetoothDetailAdapter();
    }

    public Boolean getTargetEnable() {
        return Boolean.valueOf(this.mTargetEnable);
    }

    protected class BluetoothDetailAdapter implements DetailAdapter, QSDetailItems.Callback {
        private QSDetailItems mItems;

        public int getMetricsCategory() {
            return 150;
        }

        public boolean hasHeader() {
            return true;
        }

        protected BluetoothDetailAdapter() {
        }

        public CharSequence getTitle() {
            return BluetoothTile.this.mContext.getString(R.string.quick_settings_bluetooth_label);
        }

        public Boolean getToggleState() {
            return Boolean.valueOf(BluetoothTile.this.mTargetEnable);
        }

        public boolean getToggleEnabled() {
            if (BluetoothTile.this.mTargetEnable) {
                if (BluetoothTile.this.mController.getBluetoothState() == 12) {
                    return true;
                }
            } else if (BluetoothTile.this.mController.getBluetoothState() == 10) {
                return true;
            }
            return false;
        }

        public Intent getSettingsIntent() {
            return BluetoothTile.BLUETOOTH_SETTINGS;
        }

        public void setToggleState(final boolean z) {
            String access$900 = BluetoothTile.this.TAG;
            Log.d(access$900, "setToggleState: state = " + z);
            MetricsLogger.action(BluetoothTile.this.mContext, 154, z);
            BluetoothTile.this.mHandler.post(new Runnable() {
                public void run() {
                    boolean unused = BluetoothTile.this.mTargetEnable = z;
                    BluetoothTile.this.mController.setBluetoothEnabled(z);
                    BluetoothTile.this.refreshState();
                }
            });
        }

        public View createDetailView(Context context, View view, ViewGroup viewGroup) {
            QSDetailItems convertOrInflate = QSDetailItems.convertOrInflate(context, view, viewGroup);
            this.mItems = convertOrInflate;
            convertOrInflate.setTagSuffix("Bluetooth");
            this.mItems.setCallback(this);
            if (BluetoothTile.this.isShowingDetail()) {
                updateItems();
            }
            return this.mItems;
        }

        public void setItemsVisible(boolean z) {
            QSDetailItems qSDetailItems = this.mItems;
            if (qSDetailItems != null) {
                qSDetailItems.setItemsVisible(z);
            }
        }

        /* access modifiers changed from: private */
        public void updateItems() {
            if (this.mItems != null) {
                if (BluetoothTile.this.mController.isBluetoothEnabled()) {
                    ArrayList arrayList = new ArrayList();
                    Collection<CachedBluetoothDevice> cachedDevicesCopy = BluetoothTile.this.mController.getCachedDevicesCopy();
                    if (cachedDevicesCopy != null) {
                        int i = 0;
                        int i2 = 0;
                        for (CachedBluetoothDevice next : cachedDevicesCopy) {
                            QSDetailItems.Item acquireItem = this.mItems.acquireItem();
                            acquireItem.icon = R.drawable.ic_qs_bluetooth_on;
                            acquireItem.line1 = next.getName();
                            acquireItem.line2 = BluetoothTile.this.mController.getSummary(next);
                            acquireItem.tag = next;
                            BluetoothClass btClass = next.getBtClass();
                            if (btClass != null) {
                                if (btClass.doesClassMatch(0) || btClass.doesClassMatch(1)) {
                                    acquireItem.icon = R.drawable.ic_qs_bluetooth_device_headset;
                                } else {
                                    int majorDeviceClass = btClass.getMajorDeviceClass();
                                    if (majorDeviceClass == 0) {
                                        acquireItem.icon = R.drawable.ic_qs_bluetooth_device_misc;
                                    } else if (majorDeviceClass == 256) {
                                        acquireItem.icon = R.drawable.ic_qs_bluetooth_device_laptop;
                                    } else if (majorDeviceClass == 512) {
                                        acquireItem.icon = R.drawable.ic_qs_bluetooth_device_cellphone;
                                    } else if (majorDeviceClass == 768) {
                                        acquireItem.icon = R.drawable.ic_qs_bluetooth_device_network;
                                    } else if (majorDeviceClass != 1536) {
                                        acquireItem.icon = R.drawable.ic_qs_bluetooth_device_common;
                                    } else {
                                        acquireItem.icon = R.drawable.ic_qs_bluetooth_device_imaging;
                                    }
                                }
                            }
                            int maxConnectionState = BluetoothTile.this.mController.getMaxConnectionState(next);
                            if (maxConnectionState == 2) {
                                acquireItem.icon2 = R.drawable.ic_qs_detail_item_selected;
                                acquireItem.canDisconnect = true;
                                acquireItem.selected = true;
                                arrayList.add(i, acquireItem);
                                i++;
                            } else if (maxConnectionState == 1) {
                                acquireItem.icon2 = R.drawable.ic_qs_bluetooth_connecting;
                                arrayList.add(i, acquireItem);
                            } else {
                                arrayList.add(acquireItem);
                            }
                            i2++;
                            if (i2 == 20) {
                                break;
                            }
                        }
                    }
                    if (arrayList.size() == 0) {
                        this.mItems.setEmptyState(R.drawable.ic_qs_bluetooth_detail_empty, R.string.quick_settings_bluetooth_detail_empty_text);
                    }
                    this.mItems.setItems((QSDetailItems.Item[]) arrayList.toArray(new QSDetailItems.Item[arrayList.size()]));
                    return;
                }
                this.mItems.setEmptyState(R.drawable.ic_qs_bluetooth_detail_empty, R.string.bt_is_off);
                this.mItems.setItems((QSDetailItems.Item[]) null);
            }
        }

        public void onDetailItemClick(QSDetailItems.Item item) {
            Object obj;
            CachedBluetoothDevice cachedBluetoothDevice;
            if (item != null && (obj = item.tag) != null && (cachedBluetoothDevice = (CachedBluetoothDevice) obj) != null) {
                if (cachedBluetoothDevice.isConnected()) {
                    SystemUICompat.setDeviceActive(cachedBluetoothDevice);
                } else {
                    BluetoothTile.this.mController.connect(cachedBluetoothDevice);
                }
                this.mItems.setItemClicked(true);
            }
        }

        public void onDetailItemDisconnect(QSDetailItems.Item item) {
            Object obj;
            CachedBluetoothDevice cachedBluetoothDevice;
            if (item != null && (obj = item.tag) != null && (cachedBluetoothDevice = (CachedBluetoothDevice) obj) != null) {
                BluetoothTile.this.mController.disconnect(cachedBluetoothDevice);
            }
        }

        public int getContainerHeight() {
            if (BluetoothTile.this.mContext.getResources().getConfiguration().orientation == 1) {
                return BluetoothTile.this.mContext.getResources().getDimensionPixelSize(R.dimen.qs_bluetooth_detail_height);
            }
            return -1;
        }
    }
}
