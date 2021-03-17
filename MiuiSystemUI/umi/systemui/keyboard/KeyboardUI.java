package com.android.systemui.keyboard;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.hardware.input.InputManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Pair;
import android.widget.Toast;
import com.android.settingslib.bluetooth.BluetoothCallback;
import com.android.settingslib.bluetooth.BluetoothUtils;
import com.android.settingslib.bluetooth.CachedBluetoothDevice;
import com.android.settingslib.bluetooth.CachedBluetoothDeviceManager;
import com.android.settingslib.bluetooth.LocalBluetoothAdapter;
import com.android.settingslib.bluetooth.LocalBluetoothManager;
import com.android.systemui.C0021R$string;
import com.android.systemui.Dependency;
import com.android.systemui.SystemUI;
import com.android.systemui.plugins.DarkIconDispatcher;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

public class KeyboardUI extends SystemUI implements InputManager.OnTabletModeChangedListener {
    private boolean mBootCompleted;
    private long mBootCompletedTime;
    private CachedBluetoothDeviceManager mCachedDeviceManager;
    protected volatile Context mContext;
    private BluetoothDialog mDialog;
    private boolean mEnabled;
    private volatile KeyboardHandler mHandler;
    private int mInTabletMode = -1;
    private String mKeyboardName;
    private LocalBluetoothAdapter mLocalBluetoothAdapter;
    private int mScanAttempt = 0;
    private ScanCallback mScanCallback;
    private int mState;
    private volatile KeyboardUIHandler mUIHandler;

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.SystemUI
    public void onConfigurationChanged(Configuration configuration) {
    }

    public KeyboardUI(Context context) {
        super(context);
    }

    @Override // com.android.systemui.SystemUI
    public void start() {
        this.mContext = super.mContext;
        HandlerThread handlerThread = new HandlerThread("Keyboard", 10);
        handlerThread.start();
        this.mHandler = new KeyboardHandler(handlerThread.getLooper());
        this.mHandler.sendEmptyMessage(0);
    }

    @Override // com.android.systemui.SystemUI, com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("KeyboardUI:");
        printWriter.println("  mEnabled=" + this.mEnabled);
        printWriter.println("  mBootCompleted=" + this.mEnabled);
        printWriter.println("  mBootCompletedTime=" + this.mBootCompletedTime);
        printWriter.println("  mKeyboardName=" + this.mKeyboardName);
        printWriter.println("  mInTabletMode=" + this.mInTabletMode);
        printWriter.println("  mState=" + stateToString(this.mState));
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.SystemUI
    public void onBootCompleted() {
        this.mHandler.sendEmptyMessage(1);
    }

    public void onTabletModeChanged(long j, boolean z) {
        if ((z && this.mInTabletMode != 1) || (!z && this.mInTabletMode != 0)) {
            this.mInTabletMode = z ? 1 : 0;
            processKeyboardState();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void init() {
        LocalBluetoothManager localBluetoothManager;
        Context context = this.mContext;
        String string = context.getString(17039953);
        this.mKeyboardName = string;
        if (!TextUtils.isEmpty(string) && (localBluetoothManager = (LocalBluetoothManager) Dependency.get(LocalBluetoothManager.class)) != null) {
            this.mEnabled = true;
            this.mCachedDeviceManager = localBluetoothManager.getCachedDeviceManager();
            this.mLocalBluetoothAdapter = localBluetoothManager.getBluetoothAdapter();
            localBluetoothManager.getProfileManager();
            localBluetoothManager.getEventManager().registerCallback(new BluetoothCallbackHandler());
            BluetoothUtils.setErrorListener(new BluetoothErrorListener());
            InputManager inputManager = (InputManager) context.getSystemService(InputManager.class);
            inputManager.registerOnTabletModeChangedListener(this, this.mHandler);
            this.mInTabletMode = inputManager.isInTabletMode();
            processKeyboardState();
            this.mUIHandler = new KeyboardUIHandler();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void processKeyboardState() {
        this.mHandler.removeMessages(2);
        if (!this.mEnabled) {
            this.mState = -1;
        } else if (!this.mBootCompleted) {
            this.mState = 1;
        } else if (this.mInTabletMode != 0) {
            int i = this.mState;
            if (i == 3) {
                stopScanning();
            } else if (i == 4) {
                this.mUIHandler.sendEmptyMessage(9);
            }
            this.mState = 2;
        } else {
            int state = this.mLocalBluetoothAdapter.getState();
            if ((state == 11 || state == 12) && this.mState == 4) {
                this.mUIHandler.sendEmptyMessage(9);
            }
            if (state == 11) {
                this.mState = 4;
            } else if (state != 12) {
                this.mState = 4;
                showBluetoothDialog();
            } else {
                CachedBluetoothDevice pairedKeyboard = getPairedKeyboard();
                int i2 = this.mState;
                if (i2 == 2 || i2 == 4) {
                    if (pairedKeyboard != null) {
                        this.mState = 6;
                        pairedKeyboard.connect(false);
                        return;
                    }
                    this.mCachedDeviceManager.clearNonBondedDevices();
                }
                CachedBluetoothDevice discoveredKeyboard = getDiscoveredKeyboard();
                if (discoveredKeyboard != null) {
                    this.mState = 5;
                    discoveredKeyboard.startPairing();
                    return;
                }
                this.mState = 3;
                startScanning();
            }
        }
    }

    public void onBootCompletedInternal() {
        this.mBootCompleted = true;
        this.mBootCompletedTime = SystemClock.uptimeMillis();
        if (this.mState == 1) {
            processKeyboardState();
        }
    }

    private void showBluetoothDialog() {
        if (isUserSetupComplete()) {
            long uptimeMillis = SystemClock.uptimeMillis();
            long j = this.mBootCompletedTime + 10000;
            if (j < uptimeMillis) {
                this.mUIHandler.sendEmptyMessage(8);
            } else {
                this.mHandler.sendEmptyMessageAtTime(2, j);
            }
        } else {
            this.mLocalBluetoothAdapter.enable();
        }
    }

    private boolean isUserSetupComplete() {
        if (Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "user_setup_complete", 0, -2) != 0) {
            return true;
        }
        return false;
    }

    private CachedBluetoothDevice getPairedKeyboard() {
        for (BluetoothDevice bluetoothDevice : this.mLocalBluetoothAdapter.getBondedDevices()) {
            if (this.mKeyboardName.equals(bluetoothDevice.getName())) {
                return getCachedBluetoothDevice(bluetoothDevice);
            }
        }
        return null;
    }

    private CachedBluetoothDevice getDiscoveredKeyboard() {
        for (CachedBluetoothDevice cachedBluetoothDevice : this.mCachedDeviceManager.getCachedDevicesCopy()) {
            if (cachedBluetoothDevice.getName().equals(this.mKeyboardName)) {
                return cachedBluetoothDevice;
            }
        }
        return null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private CachedBluetoothDevice getCachedBluetoothDevice(BluetoothDevice bluetoothDevice) {
        CachedBluetoothDevice findDevice = this.mCachedDeviceManager.findDevice(bluetoothDevice);
        return findDevice == null ? this.mCachedDeviceManager.addDevice(bluetoothDevice) : findDevice;
    }

    private void startScanning() {
        BluetoothLeScanner bluetoothLeScanner = this.mLocalBluetoothAdapter.getBluetoothLeScanner();
        ScanFilter build = new ScanFilter.Builder().setDeviceName(this.mKeyboardName).build();
        ScanSettings build2 = new ScanSettings.Builder().setCallbackType(1).setNumOfMatches(1).setScanMode(2).setReportDelay(0).build();
        this.mScanCallback = new KeyboardScanCallback();
        bluetoothLeScanner.startScan(Arrays.asList(build), build2, this.mScanCallback);
        KeyboardHandler keyboardHandler = this.mHandler;
        int i = this.mScanAttempt + 1;
        this.mScanAttempt = i;
        this.mHandler.sendMessageDelayed(keyboardHandler.obtainMessage(10, i, 0), 30000);
    }

    private void stopScanning() {
        if (this.mScanCallback != null) {
            BluetoothLeScanner bluetoothLeScanner = this.mLocalBluetoothAdapter.getBluetoothLeScanner();
            if (bluetoothLeScanner != null) {
                bluetoothLeScanner.stopScan(this.mScanCallback);
            }
            this.mScanCallback = null;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void bleAbortScanInternal(int i) {
        if (this.mState == 3 && i == this.mScanAttempt) {
            stopScanning();
            this.mState = 9;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onDeviceAddedInternal(CachedBluetoothDevice cachedBluetoothDevice) {
        if (this.mState == 3 && cachedBluetoothDevice.getName().equals(this.mKeyboardName)) {
            stopScanning();
            cachedBluetoothDevice.startPairing();
            this.mState = 5;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onBluetoothStateChangedInternal(int i) {
        if (i == 12 && this.mState == 4) {
            processKeyboardState();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onDeviceBondStateChangedInternal(CachedBluetoothDevice cachedBluetoothDevice, int i) {
        if (this.mState == 5 && cachedBluetoothDevice.getName().equals(this.mKeyboardName)) {
            if (i == 12) {
                this.mState = 6;
            } else if (i == 10) {
                this.mState = 7;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onBleScanFailedInternal() {
        this.mScanCallback = null;
        if (this.mState == 3) {
            this.mState = 9;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onShowErrorInternal(Context context, String str, int i) {
        int i2 = this.mState;
        if ((i2 == 5 || i2 == 7) && this.mKeyboardName.equals(str)) {
            Toast.makeText(context, context.getString(i, str), 0).show();
        }
    }

    /* access modifiers changed from: private */
    public final class KeyboardUIHandler extends Handler {
        public KeyboardUIHandler() {
            super(Looper.getMainLooper(), null, true);
        }

        public void handleMessage(Message message) {
            int i = message.what;
            if (i != 8) {
                if (i == 9 && KeyboardUI.this.mDialog != null) {
                    KeyboardUI.this.mDialog.dismiss();
                }
            } else if (KeyboardUI.this.mDialog == null) {
                BluetoothDialogClickListener bluetoothDialogClickListener = new BluetoothDialogClickListener();
                BluetoothDialogDismissListener bluetoothDialogDismissListener = new BluetoothDialogDismissListener();
                KeyboardUI.this.mDialog = new BluetoothDialog(KeyboardUI.this.mContext);
                KeyboardUI.this.mDialog.setTitle(C0021R$string.enable_bluetooth_title);
                KeyboardUI.this.mDialog.setMessage(C0021R$string.enable_bluetooth_message);
                KeyboardUI.this.mDialog.setPositiveButton(C0021R$string.enable_bluetooth_confirmation_ok, bluetoothDialogClickListener);
                KeyboardUI.this.mDialog.setNegativeButton(17039360, bluetoothDialogClickListener);
                KeyboardUI.this.mDialog.setOnDismissListener(bluetoothDialogDismissListener);
                KeyboardUI.this.mDialog.show();
            }
        }
    }

    /* access modifiers changed from: private */
    public final class KeyboardHandler extends Handler {
        public KeyboardHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message message) {
            switch (message.what) {
                case 0:
                    KeyboardUI.this.init();
                    return;
                case 1:
                    KeyboardUI.this.onBootCompletedInternal();
                    return;
                case 2:
                    KeyboardUI.this.processKeyboardState();
                    return;
                case 3:
                    boolean z = true;
                    if (message.arg1 != 1) {
                        z = false;
                    }
                    if (z) {
                        KeyboardUI.this.mLocalBluetoothAdapter.enable();
                        return;
                    } else {
                        KeyboardUI.this.mState = 8;
                        return;
                    }
                case 4:
                    KeyboardUI.this.onBluetoothStateChangedInternal(message.arg1);
                    return;
                case 5:
                    int i = message.arg1;
                    KeyboardUI.this.onDeviceBondStateChangedInternal((CachedBluetoothDevice) message.obj, i);
                    return;
                case 6:
                    KeyboardUI.this.onDeviceAddedInternal(KeyboardUI.this.getCachedBluetoothDevice((BluetoothDevice) message.obj));
                    return;
                case 7:
                    KeyboardUI.this.onBleScanFailedInternal();
                    return;
                case 8:
                case 9:
                default:
                    return;
                case 10:
                    KeyboardUI.this.bleAbortScanInternal(message.arg1);
                    return;
                case 11:
                    Pair pair = (Pair) message.obj;
                    KeyboardUI.this.onShowErrorInternal((Context) pair.first, (String) pair.second, message.arg1);
                    return;
            }
        }
    }

    private final class BluetoothDialogClickListener implements DialogInterface.OnClickListener {
        private BluetoothDialogClickListener() {
        }

        public void onClick(DialogInterface dialogInterface, int i) {
            KeyboardUI.this.mHandler.obtainMessage(3, -1 == i ? 1 : 0, 0).sendToTarget();
            KeyboardUI.this.mDialog = null;
        }
    }

    private final class BluetoothDialogDismissListener implements DialogInterface.OnDismissListener {
        private BluetoothDialogDismissListener() {
        }

        public void onDismiss(DialogInterface dialogInterface) {
            KeyboardUI.this.mDialog = null;
        }
    }

    /* access modifiers changed from: private */
    public final class KeyboardScanCallback extends ScanCallback {
        private KeyboardScanCallback() {
        }

        private boolean isDeviceDiscoverable(ScanResult scanResult) {
            return (scanResult.getScanRecord().getAdvertiseFlags() & 3) != 0;
        }

        @Override // android.bluetooth.le.ScanCallback
        public void onBatchScanResults(List<ScanResult> list) {
            BluetoothDevice bluetoothDevice = null;
            int i = Integer.MIN_VALUE;
            for (ScanResult scanResult : list) {
                if (isDeviceDiscoverable(scanResult) && scanResult.getRssi() > i) {
                    bluetoothDevice = scanResult.getDevice();
                    i = scanResult.getRssi();
                }
            }
            if (bluetoothDevice != null) {
                KeyboardUI.this.mHandler.obtainMessage(6, bluetoothDevice).sendToTarget();
            }
        }

        public void onScanFailed(int i) {
            KeyboardUI.this.mHandler.obtainMessage(7).sendToTarget();
        }

        public void onScanResult(int i, ScanResult scanResult) {
            if (isDeviceDiscoverable(scanResult)) {
                KeyboardUI.this.mHandler.obtainMessage(6, scanResult.getDevice()).sendToTarget();
            }
        }
    }

    /* access modifiers changed from: private */
    public final class BluetoothCallbackHandler implements BluetoothCallback {
        private BluetoothCallbackHandler() {
        }

        @Override // com.android.settingslib.bluetooth.BluetoothCallback
        public void onBluetoothStateChanged(int i) {
            KeyboardUI.this.mHandler.obtainMessage(4, i, 0).sendToTarget();
        }

        @Override // com.android.settingslib.bluetooth.BluetoothCallback
        public void onDeviceBondStateChanged(CachedBluetoothDevice cachedBluetoothDevice, int i) {
            KeyboardUI.this.mHandler.obtainMessage(5, i, 0, cachedBluetoothDevice).sendToTarget();
        }
    }

    /* access modifiers changed from: private */
    public final class BluetoothErrorListener implements BluetoothUtils.ErrorListener {
        private BluetoothErrorListener() {
        }

        @Override // com.android.settingslib.bluetooth.BluetoothUtils.ErrorListener
        public void onShowError(Context context, String str, int i) {
            KeyboardUI.this.mHandler.obtainMessage(11, i, 0, new Pair(context, str)).sendToTarget();
        }
    }

    private static String stateToString(int i) {
        switch (i) {
            case DarkIconDispatcher.DEFAULT_ICON_TINT /*{ENCODED_INT: -1}*/:
                return "STATE_NOT_ENABLED";
            case 0:
            default:
                return "STATE_UNKNOWN (" + i + ")";
            case 1:
                return "STATE_WAITING_FOR_BOOT_COMPLETED";
            case 2:
                return "STATE_WAITING_FOR_TABLET_MODE_EXIT";
            case 3:
                return "STATE_WAITING_FOR_DEVICE_DISCOVERY";
            case 4:
                return "STATE_WAITING_FOR_BLUETOOTH";
            case 5:
                return "STATE_PAIRING";
            case 6:
                return "STATE_PAIRED";
            case 7:
                return "STATE_PAIRING_FAILED";
            case 8:
                return "STATE_USER_CANCELLED";
            case 9:
                return "STATE_DEVICE_NOT_FOUND";
        }
    }
}
