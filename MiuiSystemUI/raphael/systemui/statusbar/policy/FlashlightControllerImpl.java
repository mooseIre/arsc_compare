package com.android.systemui.statusbar.policy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.Slog;
import com.android.systemui.Constants;
import com.android.systemui.Util;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.policy.FlashlightController;
import java.io.File;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import miui.os.Build;

public class FlashlightControllerImpl implements FlashlightController {
    /* access modifiers changed from: private */
    public static final boolean DEBUG = Log.isLoggable("FlashlightController", 3);
    public static final String[] FLASH_DEVICES = {"/sys/class/leds/flashlight/brightness", "/sys/class/leds/spotlight/brightness"};
    /* access modifiers changed from: private */
    public Handler mBgHandler;
    /* access modifiers changed from: private */
    public String mCameraId;
    private final CameraManager mCameraManager;
    /* access modifiers changed from: private */
    public final Context mContext;
    /* access modifiers changed from: private */
    public String mFlashDevice;
    /* access modifiers changed from: private */
    public boolean mFlashlightEnabled;
    /* access modifiers changed from: private */
    public boolean mForceOff;
    private Handler mHandler;
    private final ArrayList<WeakReference<FlashlightController.FlashlightListener>> mListeners = new ArrayList<>(1);
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            boolean z;
            String action = intent.getAction();
            if ("miui.intent.action.TOGGLE_TORCH".equals(action)) {
                boolean booleanExtra = intent.getBooleanExtra("miui.intent.extra.IS_TOGGLE", false);
                if (booleanExtra) {
                    z = !FlashlightControllerImpl.this.mFlashlightEnabled;
                } else {
                    z = intent.getBooleanExtra("miui.intent.extra.IS_ENABLE", false);
                }
                Slog.d("FlashlightController", String.format("onReceive: isToggle=%b, newState=%b, from=%s", new Object[]{Boolean.valueOf(booleanExtra), Boolean.valueOf(z), intent.getSender()}));
                FlashlightControllerImpl.this.setFlashlight(z);
            } else if ("action_temp_state_change".equals(action)) {
                boolean z2 = intent.getIntExtra("temp_state", 0) == 1;
                if (z2 && FlashlightControllerImpl.this.mFlashlightEnabled) {
                    Slog.d("FlashlightController", String.format("onReceive: forceOff=%b, state=%b, from=%s", new Object[]{Boolean.valueOf(z2), false, intent.getSender()}));
                    FlashlightControllerImpl.this.setFlashlight(false);
                    FlashlightControllerImpl.this.postShowToast();
                }
                if (FlashlightControllerImpl.this.mForceOff != z2) {
                    boolean unused = FlashlightControllerImpl.this.mForceOff = z2;
                    FlashlightControllerImpl flashlightControllerImpl = FlashlightControllerImpl.this;
                    flashlightControllerImpl.dispatchAvailabilityChanged(flashlightControllerImpl.isAvailable());
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public Runnable mStatusDetecting;
    /* access modifiers changed from: private */
    public boolean mTorchAvailable;
    private final CameraManager.TorchCallback mTorchCallback = new CameraManager.TorchCallback() {
        public void onTorchModeUnavailable(String str) {
            if (TextUtils.equals(str, FlashlightControllerImpl.this.mCameraId)) {
                Slog.d("FlashlightController", "TorchCallback: onTorchModeUnavailable");
                setCameraAvailable(false);
            }
        }

        public void onTorchModeChanged(String str, boolean z) {
            if (TextUtils.equals(str, FlashlightControllerImpl.this.mCameraId)) {
                Slog.d("FlashlightController", "TorchCallback: onTorchModeChanged: enabled: " + z);
                setCameraAvailable(true);
                setTorchMode(z);
            }
        }

        private void setCameraAvailable(boolean z) {
            boolean z2;
            synchronized (FlashlightControllerImpl.this) {
                z2 = FlashlightControllerImpl.this.mTorchAvailable != z;
                boolean unused = FlashlightControllerImpl.this.mTorchAvailable = z;
            }
            if (z2) {
                if (FlashlightControllerImpl.DEBUG) {
                    Log.d("FlashlightController", "setCameraAvailable: dispatchAvailabilityChanged(" + z + ")");
                }
                FlashlightControllerImpl.this.dispatchAvailabilityChanged(z);
            }
        }

        private void setTorchMode(boolean z) {
            boolean z2;
            synchronized (FlashlightControllerImpl.this) {
                z2 = FlashlightControllerImpl.this.mFlashlightEnabled != z;
                boolean unused = FlashlightControllerImpl.this.mFlashlightEnabled = z;
            }
            if (z2) {
                if (FlashlightControllerImpl.DEBUG) {
                    Log.d("FlashlightController", "setCameraAvailable: dispatchModeChanged(" + z + ")");
                }
                FlashlightControllerImpl.this.dispatchModeChanged(z);
            }
        }
    };
    /* access modifiers changed from: private */
    public int mValueOn;
    /* access modifiers changed from: private */
    public String mWaringToastString;

    public FlashlightControllerImpl(Context context) {
        this.mContext = context;
        this.mCameraManager = (CameraManager) this.mContext.getSystemService("camera");
        this.mWaringToastString = this.mContext.getResources().getString(R.string.torch_high_temperature_warning);
        ensureHandler();
        this.mBgHandler.post(new Runnable() {
            public void run() {
                FlashlightControllerImpl.this.initFlash();
            }
        });
    }

    /* access modifiers changed from: private */
    public void initFlash() {
        if (Constants.SUPPORT_ANDROID_FLASHLIGHT) {
            initCameraFlash();
        } else {
            initMiuiFlash();
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("miui.intent.action.TOGGLE_TORCH");
        intentFilter.addAction("action_temp_state_change");
        intentFilter.setPriority(-1000);
        this.mContext.registerReceiverAsUser(this.mReceiver, UserHandle.ALL, intentFilter, (String) null, this.mBgHandler);
    }

    private void initCameraFlash() {
        try {
            this.mCameraId = getCameraId();
            if (this.mCameraId != null) {
                Slog.d("FlashlightController", "initCameraFlash: register torch callback");
                this.mCameraManager.registerTorchCallback(this.mTorchCallback, this.mBgHandler);
            }
        } catch (Throwable th) {
            Log.e("FlashlightController", "Couldn't initialize.", th);
        }
    }

    private void initMiuiFlash() {
        Resources resources = this.mContext.getResources();
        this.mValueOn = resources.getInteger(R.integer.flash_on_value);
        this.mFlashDevice = resources.getString(R.string.flash_device);
        int i = 0;
        while (!new File(this.mFlashDevice).exists()) {
            String[] strArr = FLASH_DEVICES;
            if (i == strArr.length) {
                this.mFlashDevice = null;
                return;
            }
            this.mFlashDevice = strArr[i];
            i++;
        }
    }

    public void setFlashlight(final boolean z) {
        if (this.mForceOff) {
            Slog.d("FlashlightController", "setFlashlight: force off state");
            postShowToast();
        } else if (Constants.SUPPORT_ANDROID_FLASHLIGHT) {
            int i = 0;
            while (this.mCameraId == null && i < 2) {
                initCameraFlash();
                i++;
            }
            if (this.mCameraId != null) {
                this.mBgHandler.post(new Runnable() {
                    public void run() {
                        FlashlightControllerImpl.this.setNormalFlashlight(z);
                    }
                });
                return;
            }
            Slog.d("FlashlightController", "setFlashlight: enabled: " + z + ", could not initialize cameraId");
        } else {
            setMiuiFlashlight(z);
        }
    }

    /* access modifiers changed from: private */
    public void setNormalFlashlight(boolean z) {
        boolean z2;
        synchronized (this) {
            if (!this.mTorchAvailable) {
                Slog.d("FlashlightController", "setNormalFlashlight: enabled: " + z + ", torchAvailable: " + this.mTorchAvailable);
                return;
            }
            z2 = false;
            if (this.mFlashlightEnabled != z) {
                this.mFlashlightEnabled = z;
                try {
                    this.mCameraManager.setTorchMode(this.mCameraId, z);
                } catch (CameraAccessException e) {
                    Log.e("FlashlightController", "Couldn't set torch mode", e);
                    this.mFlashlightEnabled = false;
                    z2 = true;
                }
            }
        }
        if (z2) {
            dispatchError();
        } else {
            dispatchModeChanged(z);
        }
    }

    /* access modifiers changed from: private */
    public void setMiuiFlashlight(boolean z) {
        if (setMiuiFlashModeInternal(z)) {
            this.mFlashlightEnabled = z;
            dispatchModeChanged(z);
        }
    }

    private synchronized boolean setMiuiFlashModeInternal(final boolean z) {
        if (!hasFlashlight()) {
            Slog.d("FlashlightController", "setFlashModeInternal: no flashlight");
            return false;
        } else if (TextUtils.isEmpty(this.mFlashDevice)) {
            Slog.d("FlashlightController", "setFlashModeInternal: no device node");
            return false;
        } else {
            try {
                if (this.mStatusDetecting == null) {
                    this.mStatusDetecting = new Runnable() {
                        /* JADX WARNING: Removed duplicated region for block: B:21:0x0031 A[SYNTHETIC, Splitter:B:21:0x0031] */
                        /* JADX WARNING: Removed duplicated region for block: B:25:0x0038  */
                        /* JADX WARNING: Removed duplicated region for block: B:26:0x0043  */
                        /* JADX WARNING: Removed duplicated region for block: B:28:0x005c A[SYNTHETIC, Splitter:B:28:0x005c] */
                        /* Code decompiled incorrectly, please refer to instructions dump. */
                        public void run() {
                            /*
                                r6 = this;
                                r0 = 0
                                r1 = 1
                                r2 = 0
                                java.io.FileReader r3 = new java.io.FileReader     // Catch:{ Exception -> 0x002b }
                                com.android.systemui.statusbar.policy.FlashlightControllerImpl r4 = com.android.systemui.statusbar.policy.FlashlightControllerImpl.this     // Catch:{ Exception -> 0x002b }
                                java.lang.String r4 = r4.mFlashDevice     // Catch:{ Exception -> 0x002b }
                                r3.<init>(r4)     // Catch:{ Exception -> 0x002b }
                                int r2 = r3.read()     // Catch:{ Exception -> 0x0024, all -> 0x0021 }
                                r4 = 48
                                if (r2 != r4) goto L_0x0017
                                goto L_0x0018
                            L_0x0017:
                                r1 = r0
                            L_0x0018:
                                r3.close()     // Catch:{ IOException -> 0x001c }
                                goto L_0x0034
                            L_0x001c:
                                r2 = move-exception
                                r2.printStackTrace()
                                goto L_0x0034
                            L_0x0021:
                                r6 = move-exception
                                r2 = r3
                                goto L_0x005a
                            L_0x0024:
                                r2 = move-exception
                                r5 = r3
                                r3 = r2
                                r2 = r5
                                goto L_0x002c
                            L_0x0029:
                                r6 = move-exception
                                goto L_0x005a
                            L_0x002b:
                                r3 = move-exception
                            L_0x002c:
                                r3.printStackTrace()     // Catch:{ all -> 0x0029 }
                                if (r2 == 0) goto L_0x0034
                                r2.close()     // Catch:{ IOException -> 0x001c }
                            L_0x0034:
                                java.lang.String r2 = "FlashlightController"
                                if (r1 == 0) goto L_0x0043
                                java.lang.String r1 = "setFlashModeInternal: StatusDetectingRunnable: state change"
                                android.util.Slog.d(r2, r1)
                                com.android.systemui.statusbar.policy.FlashlightControllerImpl r6 = com.android.systemui.statusbar.policy.FlashlightControllerImpl.this
                                r6.setMiuiFlashlight(r0)
                                goto L_0x0059
                            L_0x0043:
                                java.lang.String r0 = "setFlashModeInternal: in runnable, post delay StatusDetectingRunnable"
                                android.util.Slog.d(r2, r0)
                                com.android.systemui.statusbar.policy.FlashlightControllerImpl r0 = com.android.systemui.statusbar.policy.FlashlightControllerImpl.this
                                android.os.Handler r0 = r0.mBgHandler
                                com.android.systemui.statusbar.policy.FlashlightControllerImpl r6 = com.android.systemui.statusbar.policy.FlashlightControllerImpl.this
                                java.lang.Runnable r6 = r6.mStatusDetecting
                                r1 = 1000(0x3e8, double:4.94E-321)
                                r0.postDelayed(r6, r1)
                            L_0x0059:
                                return
                            L_0x005a:
                                if (r2 == 0) goto L_0x0064
                                r2.close()     // Catch:{ IOException -> 0x0060 }
                                goto L_0x0064
                            L_0x0060:
                                r0 = move-exception
                                r0.printStackTrace()
                            L_0x0064:
                                throw r6
                            */
                            throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.policy.FlashlightControllerImpl.AnonymousClass4.run():void");
                        }
                    };
                }
                if (z) {
                    Slog.d("FlashlightController", "setFlashModeInternal: post delay StatusDetectingRunnable");
                    this.mBgHandler.postDelayed(this.mStatusDetecting, 1000);
                } else {
                    Slog.d("FlashlightController", "setFlashModeInternal: remove StatusDetectingRunnable");
                    this.mBgHandler.removeCallbacks(this.mStatusDetecting);
                }
                this.mBgHandler.post(new Runnable() {
                    /* JADX WARNING: Removed duplicated region for block: B:25:0x0079 A[SYNTHETIC, Splitter:B:25:0x0079] */
                    /* JADX WARNING: Removed duplicated region for block: B:30:0x0084 A[SYNTHETIC, Splitter:B:30:0x0084] */
                    /* JADX WARNING: Removed duplicated region for block: B:36:? A[RETURN, SYNTHETIC] */
                    /* Code decompiled incorrectly, please refer to instructions dump. */
                    public void run() {
                        /*
                            r5 = this;
                            java.lang.String r0 = "FlashlightController"
                            com.android.systemui.statusbar.policy.FlashlightControllerImpl r1 = com.android.systemui.statusbar.policy.FlashlightControllerImpl.this
                            java.lang.String r1 = r1.mFlashDevice
                            boolean r2 = r5
                            r3 = 0
                            if (r2 == 0) goto L_0x0014
                            com.android.systemui.statusbar.policy.FlashlightControllerImpl r2 = com.android.systemui.statusbar.policy.FlashlightControllerImpl.this
                            int r2 = r2.mValueOn
                            goto L_0x0015
                        L_0x0014:
                            r2 = r3
                        L_0x0015:
                            java.lang.String r2 = java.lang.String.valueOf(r2)
                            boolean r1 = android.miui.Shell.write(r1, r2)
                            if (r1 != 0) goto L_0x008d
                            r1 = 0
                            java.io.FileWriter r2 = new java.io.FileWriter     // Catch:{ Exception -> 0x005e }
                            com.android.systemui.statusbar.policy.FlashlightControllerImpl r4 = com.android.systemui.statusbar.policy.FlashlightControllerImpl.this     // Catch:{ Exception -> 0x005e }
                            java.lang.String r4 = r4.mFlashDevice     // Catch:{ Exception -> 0x005e }
                            r2.<init>(r4)     // Catch:{ Exception -> 0x005e }
                            java.lang.StringBuilder r1 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0058, all -> 0x0056 }
                            r1.<init>()     // Catch:{ Exception -> 0x0058, all -> 0x0056 }
                            java.lang.String r4 = "setFlashModeInternal: file writer write: "
                            r1.append(r4)     // Catch:{ Exception -> 0x0058, all -> 0x0056 }
                            boolean r4 = r5     // Catch:{ Exception -> 0x0058, all -> 0x0056 }
                            r1.append(r4)     // Catch:{ Exception -> 0x0058, all -> 0x0056 }
                            java.lang.String r1 = r1.toString()     // Catch:{ Exception -> 0x0058, all -> 0x0056 }
                            android.util.Slog.d(r0, r1)     // Catch:{ Exception -> 0x0058, all -> 0x0056 }
                            boolean r1 = r5     // Catch:{ Exception -> 0x0058, all -> 0x0056 }
                            if (r1 == 0) goto L_0x004b
                            com.android.systemui.statusbar.policy.FlashlightControllerImpl r5 = com.android.systemui.statusbar.policy.FlashlightControllerImpl.this     // Catch:{ Exception -> 0x0058, all -> 0x0056 }
                            int r3 = r5.mValueOn     // Catch:{ Exception -> 0x0058, all -> 0x0056 }
                        L_0x004b:
                            java.lang.String r5 = java.lang.String.valueOf(r3)     // Catch:{ Exception -> 0x0058, all -> 0x0056 }
                            r2.write(r5)     // Catch:{ Exception -> 0x0058, all -> 0x0056 }
                            r2.close()     // Catch:{ IOException -> 0x007d }
                            goto L_0x008d
                        L_0x0056:
                            r5 = move-exception
                            goto L_0x0082
                        L_0x0058:
                            r5 = move-exception
                            r1 = r2
                            goto L_0x005f
                        L_0x005b:
                            r5 = move-exception
                            r2 = r1
                            goto L_0x0082
                        L_0x005e:
                            r5 = move-exception
                        L_0x005f:
                            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ all -> 0x005b }
                            r2.<init>()     // Catch:{ all -> 0x005b }
                            java.lang.String r3 = "FileWriter write failed!"
                            r2.append(r3)     // Catch:{ all -> 0x005b }
                            java.lang.String r5 = r5.getMessage()     // Catch:{ all -> 0x005b }
                            r2.append(r5)     // Catch:{ all -> 0x005b }
                            java.lang.String r5 = r2.toString()     // Catch:{ all -> 0x005b }
                            android.util.Log.w(r0, r5)     // Catch:{ all -> 0x005b }
                            if (r1 == 0) goto L_0x008d
                            r1.close()     // Catch:{ IOException -> 0x007d }
                            goto L_0x008d
                        L_0x007d:
                            r5 = move-exception
                            r5.printStackTrace()
                            goto L_0x008d
                        L_0x0082:
                            if (r2 == 0) goto L_0x008c
                            r2.close()     // Catch:{ IOException -> 0x0088 }
                            goto L_0x008c
                        L_0x0088:
                            r0 = move-exception
                            r0.printStackTrace()
                        L_0x008c:
                            throw r5
                        L_0x008d:
                            return
                        */
                        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.policy.FlashlightControllerImpl.AnonymousClass5.run():void");
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }
    }

    public boolean hasFlashlight() {
        return Build.hasCameraFlash(this.mContext);
    }

    public synchronized boolean isEnabled() {
        return this.mFlashlightEnabled;
    }

    public synchronized boolean isAvailable() {
        boolean z;
        z = true;
        boolean z2 = Constants.SUPPORT_ANDROID_FLASHLIGHT ? this.mTorchAvailable : true;
        if (this.mForceOff || !z2) {
            z = false;
        }
        return z;
    }

    public void addCallback(FlashlightController.FlashlightListener flashlightListener) {
        synchronized (this.mListeners) {
            if (Constants.SUPPORT_ANDROID_FLASHLIGHT && this.mCameraId == null) {
                initCameraFlash();
            }
            cleanUpListenersLocked(flashlightListener);
            this.mListeners.add(new WeakReference(flashlightListener));
            flashlightListener.onFlashlightAvailabilityChanged(isAvailable());
            flashlightListener.onFlashlightChanged(this.mFlashlightEnabled);
        }
    }

    public void removeCallback(FlashlightController.FlashlightListener flashlightListener) {
        synchronized (this.mListeners) {
            cleanUpListenersLocked(flashlightListener);
        }
    }

    private synchronized void ensureHandler() {
        if (this.mHandler == null) {
            this.mHandler = new Handler();
        }
        if (this.mBgHandler == null) {
            HandlerThread handlerThread = new HandlerThread("FlashlightController", 10);
            handlerThread.start();
            this.mBgHandler = new Handler(handlerThread.getLooper());
        }
        if (this.mHandler == null) {
            this.mHandler = new Handler(Looper.getMainLooper());
        }
    }

    private String getCameraId() throws CameraAccessException {
        for (String str : this.mCameraManager.getCameraIdList()) {
            CameraCharacteristics cameraCharacteristics = this.mCameraManager.getCameraCharacteristics(str);
            Boolean bool = (Boolean) cameraCharacteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
            Integer num = (Integer) cameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
            if (bool != null && bool.booleanValue() && num != null && num.intValue() == 1) {
                return str;
            }
        }
        return null;
    }

    private void setTorchState(boolean z) {
        Slog.d("FlashlightController", "setTorchState: enabled: " + z);
        Settings.Global.putInt(this.mContext.getContentResolver(), "torch_state", z ? 1 : 0);
    }

    /* access modifiers changed from: private */
    public void dispatchModeChanged(boolean z) {
        setTorchState(z);
        dispatchListeners(1, z);
    }

    private void dispatchError() {
        dispatchListeners(1, false);
    }

    /* access modifiers changed from: private */
    public void dispatchAvailabilityChanged(boolean z) {
        dispatchListeners(2, z);
    }

    private void dispatchListeners(int i, boolean z) {
        synchronized (this.mListeners) {
            int size = this.mListeners.size();
            boolean z2 = false;
            for (int i2 = 0; i2 < size; i2++) {
                FlashlightController.FlashlightListener flashlightListener = (FlashlightController.FlashlightListener) this.mListeners.get(i2).get();
                if (flashlightListener == null) {
                    z2 = true;
                } else if (i == 0) {
                    flashlightListener.onFlashlightError();
                } else if (i == 1) {
                    flashlightListener.onFlashlightChanged(z);
                } else if (i == 2) {
                    flashlightListener.onFlashlightAvailabilityChanged(z);
                }
            }
            if (z2) {
                cleanUpListenersLocked((FlashlightController.FlashlightListener) null);
            }
        }
    }

    private void cleanUpListenersLocked(FlashlightController.FlashlightListener flashlightListener) {
        for (int size = this.mListeners.size() - 1; size >= 0; size--) {
            FlashlightController.FlashlightListener flashlightListener2 = (FlashlightController.FlashlightListener) this.mListeners.get(size).get();
            if (flashlightListener2 == null || flashlightListener2 == flashlightListener) {
                this.mListeners.remove(size);
            }
        }
    }

    /* access modifiers changed from: private */
    public void postShowToast() {
        this.mHandler.post(new Runnable() {
            public void run() {
                Util.showSystemOverlayToast(FlashlightControllerImpl.this.mContext, FlashlightControllerImpl.this.mWaringToastString, 1);
            }
        });
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("FlashlightController state:");
        printWriter.print("  mCameraId=");
        printWriter.println(this.mCameraId);
        printWriter.print("  mFlashlightEnabled=");
        printWriter.println(this.mFlashlightEnabled);
        printWriter.print("  isSupportAndroidFlashlight=");
        printWriter.println(Constants.SUPPORT_ANDROID_FLASHLIGHT);
        printWriter.print("  isAvailable=");
        printWriter.println(isAvailable());
    }
}
