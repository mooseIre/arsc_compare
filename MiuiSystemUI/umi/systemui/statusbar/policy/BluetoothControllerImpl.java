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
import android.os.UserHandleCompat;
import android.os.UserManager;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Log;
import com.android.settingslib.bluetooth.BluetoothCallback;
import com.android.settingslib.bluetooth.CachedBluetoothDevice;
import com.android.settingslib.bluetooth.LocalBluetoothManager;
import com.android.settingslib.bluetooth.LocalBluetoothProfileManager;
import com.android.systemui.Dependency;
import com.android.systemui.SystemUICompat;
import com.android.systemui.statusbar.policy.BluetoothController;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class BluetoothControllerImpl implements BluetoothController, BluetoothCallback, CachedBluetoothDevice.Callback, LocalBluetoothProfileManager.ServiceListener {
    private final Handler mBgHandler;
    private Collection<CachedBluetoothDevice> mCachedDevices;
    private Map<String, CachedDeviceState> mCachedStates;
    private final List<CachedBluetoothDevice> mConnectedDevices = new ArrayList();
    private int mConnectionState = 0;
    private final Context mContext;
    private final int mCurrentUser;
    /* access modifiers changed from: private */
    public boolean mEnabled;
    /* access modifiers changed from: private */
    public final H mHandler = new H(Looper.getMainLooper());
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("com.android.bluetooth.opp.BLUETOOTH_OPP_INBOUND_START".equals(action) || "com.android.bluetooth.opp.BLUETOOTH_OPP_INBOUND_END".equals(action) || "com.android.bluetooth.opp.BLUETOOTH_OPP_OUTBOUND_START".equals(action) || "com.android.bluetooth.opp.BLUETOOTH_OPP_OUTBOUND_END".equals(action)) {
                Message obtainMessage = BluetoothControllerImpl.this.mHandler.obtainMessage();
                obtainMessage.what = 5;
                obtainMessage.obj = action;
                BluetoothControllerImpl.this.mHandler.sendMessage(obtainMessage);
            } else if ("android.intent.action.BLUETOOTH_HANDSFREE_BATTERY_CHANGED".equals(action)) {
                BluetoothControllerImpl.this.onDeviceAttributesChanged();
            }
        }
    };
    private CachedBluetoothDevice mLastActiveDevice;
    /* access modifiers changed from: private */
    public final LocalBluetoothManager mLocalBluetoothManager;
    private int mState = 10;
    private final UserManager mUserManager;

    public void onActiveDeviceChanged(CachedBluetoothDevice cachedBluetoothDevice, int i) {
    }

    public void onAudioModeChanged() {
    }

    public void onConnectionStateChanged(CachedBluetoothDevice cachedBluetoothDevice, int i) {
    }

    public void onDeviceAdded(CachedBluetoothDevice cachedBluetoothDevice) {
    }

    public void onDeviceDeleted(CachedBluetoothDevice cachedBluetoothDevice) {
    }

    public void onScanningStateChanged(boolean z) {
    }

    public void onServiceDisconnected() {
    }

    static {
        Log.isLoggable("BluetoothController", 3);
    }

    public BluetoothControllerImpl(Context context, Looper looper) {
        this.mContext = context;
        this.mCachedDevices = new ArraySet();
        this.mCachedStates = new HashMap();
        this.mLocalBluetoothManager = (LocalBluetoothManager) Dependency.get(LocalBluetoothManager.class);
        this.mBgHandler = new BH(looper);
        LocalBluetoothManager localBluetoothManager = this.mLocalBluetoothManager;
        if (localBluetoothManager != null) {
            BluetoothControllerHelper.setReceiverHandler(localBluetoothManager.getEventManager(), this.mBgHandler);
            this.mLocalBluetoothManager.getEventManager().registerCallback(this);
            BluetoothControllerHelper.addServiceListener(this.mLocalBluetoothManager.getProfileManager(), this);
            this.mBgHandler.post(new Runnable() {
                public void run() {
                    BluetoothControllerImpl bluetoothControllerImpl = BluetoothControllerImpl.this;
                    bluetoothControllerImpl.onBluetoothStateChanged(bluetoothControllerImpl.mLocalBluetoothManager.getBluetoothAdapter().getBluetoothState());
                }
            });
        }
        this.mUserManager = (UserManager) context.getSystemService("user");
        this.mCurrentUser = ActivityManager.getCurrentUser();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.android.bluetooth.opp.BLUETOOTH_OPP_INBOUND_START");
        intentFilter.addAction("com.android.bluetooth.opp.BLUETOOTH_OPP_INBOUND_END");
        intentFilter.addAction("com.android.bluetooth.opp.BLUETOOTH_OPP_OUTBOUND_START");
        intentFilter.addAction("com.android.bluetooth.opp.BLUETOOTH_OPP_OUTBOUND_END");
        intentFilter.addAction("android.bluetooth.headset.profile.action.CONNECTION_STATE_CHANGED");
        intentFilter.addAction("android.intent.action.BLUETOOTH_HANDSFREE_BATTERY_CHANGED");
        context.registerReceiverAsUser(this.mIntentReceiver, UserHandle.ALL, intentFilter, (String) null, this.mBgHandler);
    }

    private void addPairedDevices() {
        Log.d("BluetoothController", "addPairedDevices");
        int connectionState = this.mLocalBluetoothManager.getBluetoothAdapter().getConnectionState();
        Collection<CachedBluetoothDevice> cachedDevicesCopy = this.mLocalBluetoothManager.getCachedDeviceManager().getCachedDevicesCopy();
        CachedBluetoothDevice cachedBluetoothDevice = null;
        this.mLastActiveDevice = null;
        for (CachedBluetoothDevice next : cachedDevicesCopy) {
            int bondState = next.getBondState();
            if (bondState != 10) {
                addCachedDevice(next);
                CachedDeviceState cachedState = getCachedState(next);
                cachedState.setBondState(bondState);
                int maxConnectionState = next.getMaxConnectionState();
                if (maxConnectionState > connectionState) {
                    connectionState = maxConnectionState;
                }
                cachedState.setConnectionState(maxConnectionState);
                if (SystemUICompat.isDeviceActive(next)) {
                    Log.d("BluetoothController", "addPairedDevices: last active device: " + next.getName());
                    this.mLastActiveDevice = next;
                } else if (next.isConnected()) {
                    Log.d("BluetoothController", "addPairedDevices: last connected device: " + next.getName());
                    cachedBluetoothDevice = next;
                }
                cachedState.setSummary(SystemUICompat.getConnectionSummary(this.mContext, next));
            }
        }
        if (this.mLastActiveDevice == null && cachedBluetoothDevice != null) {
            Log.d("BluetoothController", "addPairedDevices: find last connected device: " + cachedBluetoothDevice.getName());
            this.mLastActiveDevice = cachedBluetoothDevice;
        }
        if (this.mLastActiveDevice == null && connectionState == 2) {
            connectionState = 0;
        }
        if (connectionState != this.mConnectionState) {
            this.mConnectionState = connectionState;
            this.mHandler.removeMessages(2);
            this.mHandler.sendEmptyMessage(2);
        }
    }

    private void removeAllDevices() {
        Log.d("BluetoothController", "removeAllDevices");
        synchronized (this.mCachedDevices) {
            for (CachedBluetoothDevice unregisterCallback : this.mCachedDevices) {
                unregisterCallback.unregisterCallback(this);
            }
            this.mCachedDevices.clear();
            this.mCachedStates.clear();
        }
    }

    public void addCallback(BluetoothController.Callback callback) {
        this.mHandler.obtainMessage(3, callback).sendToTarget();
        this.mHandler.removeMessages(2);
        this.mHandler.sendEmptyMessage(2);
    }

    public void removeCallback(BluetoothController.Callback callback) {
        this.mHandler.obtainMessage(4, callback).sendToTarget();
    }

    private void addCachedDevice(CachedBluetoothDevice cachedBluetoothDevice) {
        synchronized (this.mCachedDevices) {
            if (this.mCachedDevices.add(cachedBluetoothDevice)) {
                cachedBluetoothDevice.registerCallback(this);
            }
        }
    }

    private void removeCachedDevice(CachedBluetoothDevice cachedBluetoothDevice) {
        synchronized (this.mCachedDevices) {
            if (this.mCachedDevices.remove(cachedBluetoothDevice)) {
                cachedBluetoothDevice.unregisterCallback(this);
            }
        }
    }

    public boolean canConfigBluetooth() {
        return !this.mUserManager.hasUserRestriction("no_config_bluetooth", UserHandleCompat.of(this.mCurrentUser));
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

    public String getLastDeviceName() {
        CachedBluetoothDevice cachedBluetoothDevice = this.mLastActiveDevice;
        if (cachedBluetoothDevice != null) {
            return cachedBluetoothDevice.getName();
        }
        return null;
    }

    public Collection<CachedBluetoothDevice> getCachedDevicesCopy() {
        ArrayList arrayList;
        synchronized (this.mCachedDevices) {
            arrayList = (this.mCachedDevices == null || this.mCachedDevices.isEmpty()) ? null : new ArrayList(this.mCachedDevices);
        }
        return arrayList;
    }

    public void onBluetoothStateChanged(int i) {
        Log.d("BluetoothController", "onBluetoothStateChanged: bluetoothState: " + i);
        this.mState = i;
        switch (i) {
            case 10:
                this.mEnabled = false;
                break;
            case 11:
                removeAllDevices();
                this.mEnabled = true;
                break;
            case 12:
                this.mEnabled = true;
                addPairedDevices();
                break;
            case 13:
                this.mEnabled = false;
                removeAllDevices();
                this.mLastActiveDevice = null;
                this.mConnectionState = 0;
                break;
        }
        this.mHandler.removeMessages(2);
        this.mHandler.sendEmptyMessage(2);
    }

    public void onDeviceBondStateChanged(CachedBluetoothDevice cachedBluetoothDevice, int i) {
        Log.d("BluetoothController", "onDeviceBondStateChanged");
        if (i == 10) {
            Log.d("BluetoothController", "onDeviceBondStateChanged: " + cachedBluetoothDevice.getDevice() + ": bond none");
            removeCachedDevice(cachedBluetoothDevice);
            this.mCachedStates.remove(cachedBluetoothDevice.getDevice().getAddress());
            onDeviceAttributesChanged();
        } else if (i == 12) {
            Log.d("BluetoothController", "onDeviceBondStateChanged: " + cachedBluetoothDevice.getDevice() + ": bonded");
            addCachedDevice(cachedBluetoothDevice);
            getCachedState(cachedBluetoothDevice).setBondState(i);
        }
    }

    public void onServiceConnected() {
        updateConnected();
        this.mHandler.sendEmptyMessage(1);
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
    }

    public void onDeviceAttributesChanged() {
        Log.d("BluetoothController", "onDeviceAttributesChanged");
        this.mBgHandler.removeMessages(1);
        this.mBgHandler.sendEmptyMessage(1);
    }

    public void handleDeviceAttributesChanged() {
        Log.d("BluetoothController", "handleDeviceAttributesChanged");
        updateConnectionState();
        updateSummary();
    }

    private void updateConnectionState() {
        CachedBluetoothDevice cachedBluetoothDevice;
        int i;
        CachedBluetoothDevice cachedBluetoothDevice2;
        Log.d("BluetoothController", "updateConnectionState");
        synchronized (this.mCachedDevices) {
            cachedBluetoothDevice = null;
            i = 0;
            cachedBluetoothDevice2 = null;
            for (CachedBluetoothDevice next : this.mCachedDevices) {
                int maxConnectionState = next.getMaxConnectionState();
                if (maxConnectionState > i) {
                    i = maxConnectionState;
                }
                if (SystemUICompat.isDeviceActive(next)) {
                    Log.d("BluetoothController", "updateConnectionState: last active device: " + next.getName());
                    cachedBluetoothDevice = next;
                } else if (next.isConnected()) {
                    Log.d("BluetoothController", "updateConnectionState: last connected device: " + next.getName());
                    cachedBluetoothDevice2 = next;
                }
                getCachedState(next).setConnectionState(maxConnectionState);
            }
        }
        if (cachedBluetoothDevice == null && cachedBluetoothDevice2 != null) {
            Log.d("BluetoothController", "updateConnectionState: find last connected device: " + cachedBluetoothDevice2.getName());
            cachedBluetoothDevice = cachedBluetoothDevice2;
        }
        if (this.mConnectionState != i || this.mLastActiveDevice != cachedBluetoothDevice) {
            this.mConnectionState = i;
            this.mLastActiveDevice = cachedBluetoothDevice;
            this.mHandler.removeMessages(2);
            this.mHandler.sendEmptyMessage(2);
            Log.d("BluetoothController", "updateConnectionState: " + this.mConnectionState);
        }
    }

    private void updateSummary() {
        synchronized (this.mCachedDevices) {
            for (CachedBluetoothDevice next : this.mCachedDevices) {
                getCachedState(next).setSummary(SystemUICompat.getConnectionSummary(this.mContext, next));
            }
        }
    }

    public CachedDeviceState getCachedState(CachedBluetoothDevice cachedBluetoothDevice) {
        CachedDeviceState cachedDeviceState;
        synchronized (this.mCachedDevices) {
            String address = cachedBluetoothDevice.getDevice().getAddress();
            cachedDeviceState = this.mCachedStates.get(address);
            if (cachedDeviceState == null) {
                cachedDeviceState = new CachedDeviceState(this.mHandler);
                this.mCachedStates.put(address, cachedDeviceState);
            }
        }
        return cachedDeviceState;
    }

    public int getMaxConnectionState(CachedBluetoothDevice cachedBluetoothDevice) {
        return getCachedState(cachedBluetoothDevice).mMaxConnectionState;
    }

    public String getSummary(CachedBluetoothDevice cachedBluetoothDevice) {
        return getCachedState(cachedBluetoothDevice).mSummary;
    }

    private static class CachedDeviceState {
        private int mBondState;
        /* access modifiers changed from: private */
        public int mMaxConnectionState;
        /* access modifiers changed from: private */
        public String mSummary;
        private final Handler mUiHandler;

        private CachedDeviceState(Handler handler) {
            this.mBondState = 10;
            this.mMaxConnectionState = 0;
            this.mSummary = "";
            this.mUiHandler = handler;
        }

        public void setBondState(int i) {
            if (this.mBondState != i) {
                this.mBondState = i;
                this.mUiHandler.removeMessages(1);
                this.mUiHandler.sendEmptyMessage(1);
            }
        }

        public void setConnectionState(int i) {
            if (this.mMaxConnectionState != i) {
                this.mMaxConnectionState = i;
                this.mUiHandler.removeMessages(2);
                this.mUiHandler.sendEmptyMessage(2);
            }
        }

        public void setSummary(String str) {
            if (!TextUtils.equals(this.mSummary, str)) {
                this.mSummary = str;
                this.mUiHandler.removeMessages(2);
                this.mUiHandler.sendEmptyMessage(2);
            }
        }
    }

    private final class BH extends Handler {
        public BH(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message message) {
            if (message.what == 1) {
                BluetoothControllerImpl.this.handleDeviceAttributesChanged();
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
                Log.d("BluetoothController", "fireStateChange");
                fireStateChange();
            } else if (i == 3) {
                this.mCallbacks.add((BluetoothController.Callback) message.obj);
            } else if (i == 4) {
                this.mCallbacks.remove((BluetoothController.Callback) message.obj);
            } else if (i == 5) {
                fireInoutStateChange((String) message.obj);
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

        private void fireInoutStateChange(String str) {
            Iterator<BluetoothController.Callback> it = this.mCallbacks.iterator();
            while (it.hasNext()) {
                fireInoutStateChange(it.next(), str);
            }
        }

        private void fireStateChange(BluetoothController.Callback callback) {
            callback.onBluetoothStateChange(BluetoothControllerImpl.this.mEnabled);
        }

        private void fireInoutStateChange(BluetoothController.Callback callback, String str) {
            callback.onBluetoothInoutStateChange(str);
        }
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("BluetoothController state:");
        printWriter.print("  mLocalBluetoothManager=");
        printWriter.println(this.mLocalBluetoothManager);
        if (this.mLocalBluetoothManager != null) {
            printWriter.print("  mEnabled=");
            printWriter.println(this.mEnabled);
            printWriter.print("  mState=");
            printWriter.println(this.mState);
            printWriter.print("  mConnectionState=");
            printWriter.println(stateToString(this.mConnectionState));
            printWriter.print("  mLastActiveDevice=");
            printWriter.println(this.mLastActiveDevice);
            printWriter.print("  mCallbacks.size=");
            printWriter.println(this.mHandler.mCallbacks.size());
            printWriter.println("  Bluetooth Devices:");
            synchronized (this.mCachedDevices) {
                for (CachedBluetoothDevice deviceString : this.mCachedDevices) {
                    printWriter.println("    " + getDeviceString(deviceString));
                }
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
}
