package com.android.systemui.statusbar.policy;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.Log;
import androidx.constraintlayout.widget.R$styleable;
import com.android.settingslib.bluetooth.BluetoothCallback;
import com.android.settingslib.bluetooth.CachedBluetoothDevice;
import com.android.settingslib.bluetooth.LocalBluetoothManager;
import com.android.settingslib.bluetooth.LocalBluetoothProfile;
import com.android.settingslib.bluetooth.LocalBluetoothProfileManager;
import com.android.systemui.statusbar.policy.BluetoothController;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.WeakHashMap;

public class BluetoothControllerImpl implements BluetoothController, BluetoothCallback, CachedBluetoothDevice.Callback, LocalBluetoothProfileManager.ServiceListener {
    private static final boolean DEBUG = Log.isLoggable("BluetoothController", 3);
    private boolean mAudioProfileOnly;
    private final Handler mBgHandler;
    private final WeakHashMap<CachedBluetoothDevice, ActuallyCachedState> mCachedState = new WeakHashMap<>();
    private final List<CachedBluetoothDevice> mConnectedDevices = new ArrayList();
    private int mConnectionState = 0;
    private final int mCurrentUser;
    /* access modifiers changed from: private */
    public boolean mEnabled;
    /* access modifiers changed from: private */
    public final H mHandler;
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d("BluetoothController", "onReceive: action = " + action);
            if ("com.android.bluetooth.opp.BLUETOOTH_OPP_INBOUND_START".equals(action) || "com.android.bluetooth.opp.BLUETOOTH_OPP_INBOUND_END".equals(action) || "com.android.bluetooth.opp.BLUETOOTH_OPP_OUTBOUND_START".equals(action) || "com.android.bluetooth.opp.BLUETOOTH_OPP_OUTBOUND_END".equals(action)) {
                Message obtainMessage = BluetoothControllerImpl.this.mHandler.obtainMessage();
                obtainMessage.what = 100;
                obtainMessage.obj = action;
                BluetoothControllerImpl.this.mHandler.sendMessage(obtainMessage);
            } else if ("android.intent.action.BLUETOOTH_HANDSFREE_BATTERY_CHANGED".equals(action)) {
                Message obtainMessage2 = BluetoothControllerImpl.this.mHandler.obtainMessage();
                obtainMessage2.what = R$styleable.Constraint_layout_goneMarginRight;
                obtainMessage2.obj = intent;
                BluetoothControllerImpl.this.mHandler.sendMessage(obtainMessage2);
                BluetoothControllerImpl.this.onDeviceAttributesChanged();
            }
        }
    };
    private boolean mIsActive;
    private CachedBluetoothDevice mLastActiveDevice;
    private final LocalBluetoothManager mLocalBluetoothManager;
    private int mState = 10;
    private final UserManager mUserManager;

    public void onServiceDisconnected() {
    }

    public BluetoothControllerImpl(Context context, Looper looper, Looper looper2, LocalBluetoothManager localBluetoothManager) {
        this.mLocalBluetoothManager = localBluetoothManager;
        this.mBgHandler = new Handler(looper);
        this.mHandler = new H(looper2);
        LocalBluetoothManager localBluetoothManager2 = this.mLocalBluetoothManager;
        if (localBluetoothManager2 != null) {
            localBluetoothManager2.getEventManager().registerCallback(this);
            this.mLocalBluetoothManager.getProfileManager().addServiceListener(this);
            onBluetoothStateChanged(this.mLocalBluetoothManager.getBluetoothAdapter().getBluetoothState());
        }
        this.mUserManager = (UserManager) context.getSystemService("user");
        this.mCurrentUser = ActivityManager.getCurrentUser();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.android.bluetooth.opp.BLUETOOTH_OPP_INBOUND_START");
        intentFilter.addAction("com.android.bluetooth.opp.BLUETOOTH_OPP_INBOUND_END");
        intentFilter.addAction("com.android.bluetooth.opp.BLUETOOTH_OPP_OUTBOUND_START");
        intentFilter.addAction("com.android.bluetooth.opp.BLUETOOTH_OPP_OUTBOUND_END");
        intentFilter.addAction("android.intent.action.BLUETOOTH_HANDSFREE_BATTERY_CHANGED");
        context.registerReceiverAsUser(this.mIntentReceiver, UserHandle.ALL, intentFilter, (String) null, this.mBgHandler);
    }

    public boolean canConfigBluetooth() {
        return !this.mUserManager.hasUserRestriction("no_config_bluetooth", UserHandle.of(this.mCurrentUser)) && !this.mUserManager.hasUserRestriction("no_bluetooth", UserHandle.of(this.mCurrentUser));
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("BluetoothController state:");
        printWriter.print("  mLocalBluetoothManager=");
        printWriter.println(this.mLocalBluetoothManager);
        if (this.mLocalBluetoothManager != null) {
            printWriter.print("  mEnabled=");
            printWriter.println(this.mEnabled);
            printWriter.print("  mConnectionState=");
            printWriter.println(stateToString(this.mConnectionState));
            printWriter.print("  mAudioProfileOnly=");
            printWriter.println(this.mAudioProfileOnly);
            printWriter.print("  mIsActive=");
            printWriter.println(this.mIsActive);
            printWriter.print("  mConnectedDevices=");
            printWriter.println(this.mConnectedDevices);
            printWriter.print("  mCallbacks.size=");
            printWriter.println(this.mHandler.mCallbacks.size());
            printWriter.println("  Bluetooth Devices:");
            for (CachedBluetoothDevice deviceString : getDevices()) {
                printWriter.println("    " + getDeviceString(deviceString));
            }
        }
    }

    private static String stateToString(int i) {
        if (i == 0) {
            return "DISCONNECTED";
        }
        if (i == 1) {
            return "CONNECTING";
        }
        if (i == 2) {
            return "CONNECTED";
        }
        if (i == 3) {
            return "DISCONNECTING";
        }
        return "UNKNOWN(" + i + ")";
    }

    private String getDeviceString(CachedBluetoothDevice cachedBluetoothDevice) {
        return cachedBluetoothDevice.getName() + " " + cachedBluetoothDevice.getBondState() + " " + cachedBluetoothDevice.isConnected();
    }

    public int getMaxConnectionState(CachedBluetoothDevice cachedBluetoothDevice) {
        return getCachedState(cachedBluetoothDevice).mMaxConnectionState;
    }

    public void addCallback(BluetoothController.Callback callback) {
        this.mHandler.obtainMessage(3, callback).sendToTarget();
        this.mHandler.sendEmptyMessage(2);
    }

    public void removeCallback(BluetoothController.Callback callback) {
        this.mHandler.obtainMessage(4, callback).sendToTarget();
    }

    public boolean isBluetoothEnabled() {
        return this.mEnabled;
    }

    public int getBluetoothState() {
        return this.mState;
    }

    public boolean isBluetoothConnected() {
        return this.mConnectionState == 2;
    }

    public boolean isBluetoothConnecting() {
        return this.mConnectionState == 1;
    }

    public void setBluetoothEnabled(boolean z) {
        LocalBluetoothManager localBluetoothManager = this.mLocalBluetoothManager;
        if (localBluetoothManager != null) {
            localBluetoothManager.getBluetoothAdapter().setBluetoothEnabled(z);
        }
    }

    public boolean isBluetoothSupported() {
        return this.mLocalBluetoothManager != null;
    }

    public void connect(CachedBluetoothDevice cachedBluetoothDevice) {
        if (this.mLocalBluetoothManager != null && cachedBluetoothDevice != null) {
            cachedBluetoothDevice.connect(true);
        }
    }

    public void disconnect(CachedBluetoothDevice cachedBluetoothDevice) {
        if (this.mLocalBluetoothManager != null && cachedBluetoothDevice != null) {
            cachedBluetoothDevice.disconnect();
        }
    }

    public Collection<CachedBluetoothDevice> getDevices() {
        LocalBluetoothManager localBluetoothManager = this.mLocalBluetoothManager;
        if (localBluetoothManager != null) {
            return localBluetoothManager.getCachedDeviceManager().getCachedDevicesCopy();
        }
        return null;
    }

    private void updateConnected() {
        int connectionState = this.mLocalBluetoothManager.getBluetoothAdapter().getConnectionState();
        this.mConnectedDevices.clear();
        for (CachedBluetoothDevice next : getDevices()) {
            int maxConnectionState = next.getMaxConnectionState();
            if (maxConnectionState > connectionState) {
                connectionState = maxConnectionState;
            }
            if (next.isConnected()) {
                this.mConnectedDevices.add(next);
            }
        }
        if (this.mConnectedDevices.isEmpty() && connectionState == 2) {
            connectionState = 0;
        }
        if (connectionState != this.mConnectionState) {
            this.mConnectionState = connectionState;
            this.mHandler.sendEmptyMessage(2);
        }
        updateAudioProfile();
    }

    private void updateActive() {
        boolean z = false;
        for (CachedBluetoothDevice next : getDevices()) {
            boolean z2 = true;
            if (!next.isActiveDevice(1) && !next.isActiveDevice(2) && !next.isActiveDevice(21)) {
                z2 = false;
            }
            z |= z2;
        }
        if (this.mIsActive != z) {
            this.mIsActive = z;
            this.mHandler.sendEmptyMessage(2);
        }
    }

    private void updateAudioProfile() {
        boolean z = false;
        boolean z2 = false;
        boolean z3 = false;
        for (CachedBluetoothDevice next : getDevices()) {
            for (LocalBluetoothProfile next2 : next.getProfiles()) {
                int profileId = next2.getProfileId();
                boolean isConnectedProfile = next.isConnectedProfile(next2);
                if (profileId == 1 || profileId == 2 || profileId == 21) {
                    z2 |= isConnectedProfile;
                } else {
                    z3 |= isConnectedProfile;
                }
            }
        }
        if (z2 && !z3) {
            z = true;
        }
        if (z != this.mAudioProfileOnly) {
            this.mAudioProfileOnly = z;
            this.mHandler.sendEmptyMessage(2);
        }
    }

    public void onBluetoothStateChanged(int i) {
        if (DEBUG) {
            Log.d("BluetoothController", "BluetoothStateChanged=" + stateToString(i));
        }
        this.mEnabled = i == 12 || i == 11;
        this.mState = i;
        updateConnected();
        this.mHandler.sendEmptyMessage(2);
    }

    public void onDeviceAdded(CachedBluetoothDevice cachedBluetoothDevice) {
        if (DEBUG) {
            Log.d("BluetoothController", "DeviceAdded=" + cachedBluetoothDevice.getAddress());
        }
        cachedBluetoothDevice.registerCallback(this);
        updateConnected();
        this.mHandler.sendEmptyMessage(1);
    }

    public void onDeviceDeleted(CachedBluetoothDevice cachedBluetoothDevice) {
        if (DEBUG) {
            Log.d("BluetoothController", "DeviceDeleted=" + cachedBluetoothDevice.getAddress());
        }
        this.mCachedState.remove(cachedBluetoothDevice);
        updateConnected();
        this.mHandler.sendEmptyMessage(1);
    }

    public void onDeviceBondStateChanged(CachedBluetoothDevice cachedBluetoothDevice, int i) {
        if (DEBUG) {
            Log.d("BluetoothController", "DeviceBondStateChanged=" + cachedBluetoothDevice.getAddress());
        }
        this.mCachedState.remove(cachedBluetoothDevice);
        updateConnected();
        this.mHandler.sendEmptyMessage(1);
    }

    public void onDeviceAttributesChanged() {
        if (DEBUG) {
            Log.d("BluetoothController", "DeviceAttributesChanged");
        }
        updateConnected();
        this.mHandler.sendEmptyMessage(1);
    }

    public void onConnectionStateChanged(CachedBluetoothDevice cachedBluetoothDevice, int i) {
        if (DEBUG) {
            Log.d("BluetoothController", "ConnectionStateChanged=" + cachedBluetoothDevice.getAddress() + " " + stateToString(i));
        }
        this.mCachedState.remove(cachedBluetoothDevice);
        updateConnected();
        this.mHandler.sendEmptyMessage(2);
    }

    public void onProfileConnectionStateChanged(CachedBluetoothDevice cachedBluetoothDevice, int i, int i2) {
        if (DEBUG) {
            Log.d("BluetoothController", "ProfileConnectionStateChanged=" + cachedBluetoothDevice.getAddress() + " " + stateToString(i) + " profileId=" + i2);
        }
        this.mCachedState.remove(cachedBluetoothDevice);
        updateConnected();
        this.mHandler.sendEmptyMessage(2);
    }

    public void onActiveDeviceChanged(CachedBluetoothDevice cachedBluetoothDevice, int i) {
        if (DEBUG) {
            Log.d("BluetoothController", "ActiveDeviceChanged=" + cachedBluetoothDevice.getAddress() + " profileId=" + i);
        }
        updateActive();
        if (!this.mIsActive || cachedBluetoothDevice == null) {
            this.mLastActiveDevice = null;
        } else {
            this.mLastActiveDevice = cachedBluetoothDevice;
        }
        this.mHandler.sendEmptyMessage(2);
    }

    public void onAclConnectionStateChanged(CachedBluetoothDevice cachedBluetoothDevice, int i) {
        if (DEBUG) {
            Log.d("BluetoothController", "ACLConnectionStateChanged=" + cachedBluetoothDevice.getAddress() + " " + stateToString(i));
        }
        this.mLastActiveDevice = null;
        this.mCachedState.remove(cachedBluetoothDevice);
        updateConnected();
        this.mHandler.sendEmptyMessage(2);
    }

    private ActuallyCachedState getCachedState(CachedBluetoothDevice cachedBluetoothDevice) {
        ActuallyCachedState actuallyCachedState = this.mCachedState.get(cachedBluetoothDevice);
        if (actuallyCachedState != null) {
            return actuallyCachedState;
        }
        ActuallyCachedState actuallyCachedState2 = new ActuallyCachedState(cachedBluetoothDevice, this.mHandler);
        this.mBgHandler.post(actuallyCachedState2);
        this.mCachedState.put(cachedBluetoothDevice, actuallyCachedState2);
        return actuallyCachedState2;
    }

    public void onServiceConnected() {
        updateConnected();
        this.mHandler.sendEmptyMessage(1);
    }

    private static class ActuallyCachedState implements Runnable {
        private final WeakReference<CachedBluetoothDevice> mDevice;
        /* access modifiers changed from: private */
        public int mMaxConnectionState;
        private final Handler mUiHandler;

        private ActuallyCachedState(CachedBluetoothDevice cachedBluetoothDevice, Handler handler) {
            this.mMaxConnectionState = 0;
            this.mDevice = new WeakReference<>(cachedBluetoothDevice);
            this.mUiHandler = handler;
        }

        public void run() {
            CachedBluetoothDevice cachedBluetoothDevice = (CachedBluetoothDevice) this.mDevice.get();
            if (cachedBluetoothDevice != null) {
                cachedBluetoothDevice.getBondState();
                this.mMaxConnectionState = cachedBluetoothDevice.getMaxConnectionState();
                this.mUiHandler.removeMessages(1);
                this.mUiHandler.sendEmptyMessage(1);
            }
        }
    }

    private final class H extends Handler {
        /* access modifiers changed from: private */
        public final ArrayList<BluetoothController.Callback> mCallbacks = new ArrayList<>();

        public H(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message message) {
            int i = message.what;
            if (i == 1) {
                firePairedDevicesChanged();
            } else if (i == 2) {
                fireStateChange();
            } else if (i == 3) {
                this.mCallbacks.add((BluetoothController.Callback) message.obj);
            } else if (i == 4) {
                this.mCallbacks.remove((BluetoothController.Callback) message.obj);
            } else if (i == 100) {
                fireInoutStateChange((String) message.obj);
            } else if (i == 101) {
                fireHandsreeBatteryStateChange((Intent) message.obj);
            }
        }

        private void firePairedDevicesChanged() {
            Iterator<BluetoothController.Callback> it = this.mCallbacks.iterator();
            while (it.hasNext()) {
                it.next().onBluetoothDevicesChanged();
            }
        }

        private void fireStateChange() {
            Iterator<BluetoothController.Callback> it = this.mCallbacks.iterator();
            while (it.hasNext()) {
                fireStateChange(it.next());
            }
        }

        private void fireStateChange(BluetoothController.Callback callback) {
            callback.onBluetoothStateChange(BluetoothControllerImpl.this.mEnabled);
        }

        private void fireInoutStateChange(String str) {
            Iterator<BluetoothController.Callback> it = this.mCallbacks.iterator();
            while (it.hasNext()) {
                it.next().onBluetoothInoutStateChange(str);
            }
        }

        private void fireHandsreeBatteryStateChange(Intent intent) {
            Iterator<BluetoothController.Callback> it = this.mCallbacks.iterator();
            while (it.hasNext()) {
                it.next().onBluetoothBatteryChange(intent);
            }
        }
    }

    public boolean isBluetoothReady() {
        int i = this.mState;
        return i == 12 || i == 10 || i == 15;
    }

    public String getLastDeviceName() {
        if (this.mConnectedDevices.isEmpty()) {
            return "";
        }
        CachedBluetoothDevice cachedBluetoothDevice = this.mLastActiveDevice;
        if (cachedBluetoothDevice != null) {
            return cachedBluetoothDevice.getName();
        }
        CachedBluetoothDevice cachedBluetoothDevice2 = this.mConnectedDevices.get(0);
        if (cachedBluetoothDevice2 == null) {
            return "";
        }
        return cachedBluetoothDevice2.getName();
    }
}
