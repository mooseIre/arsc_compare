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
import com.android.keyguard.charge.ChargeUtils;
import com.android.systemui.C0016R$integer;
import com.android.systemui.C0021R$string;
import com.android.systemui.controlcenter.utils.Constants;
import com.android.systemui.statusbar.policy.FlashlightController;
import java.io.File;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import miui.os.Build;

public class MiuiFlashlightControllerImpl implements FlashlightController {
    private static final boolean DEBUG = Log.isLoggable("FlashlightController", 3);
    public static final String[] FLASH_DEVICES = {"/sys/class/leds/flashlight/brightness", "/sys/class/leds/spotlight/brightness"};
    private Handler mBgHandler;
    private String mCameraId;
    private final CameraManager mCameraManager;
    private final Context mContext;
    private String mFlashDevice;
    private boolean mFlashlightEnabled;
    private boolean mForceOff;
    private Handler mHandler;
    private final ArrayList<WeakReference<FlashlightController.FlashlightListener>> mListeners = new ArrayList<>(1);
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        /* class com.android.systemui.statusbar.policy.MiuiFlashlightControllerImpl.AnonymousClass2 */

        public void onReceive(Context context, Intent intent) {
            boolean z;
            String action = intent.getAction();
            if ("miui.intent.action.TOGGLE_TORCH".equals(action)) {
                boolean booleanExtra = intent.getBooleanExtra("miui.intent.extra.IS_TOGGLE", false);
                if (booleanExtra) {
                    z = !MiuiFlashlightControllerImpl.this.mFlashlightEnabled;
                } else {
                    z = intent.getBooleanExtra("miui.intent.extra.IS_ENABLE", false);
                }
                Slog.d("FlashlightController", String.format("onReceive: isToggle=%b, newState=%b, from=%s", Boolean.valueOf(booleanExtra), Boolean.valueOf(z), intent.getSender()));
                MiuiFlashlightControllerImpl.this.setFlashlight(z);
            } else if ("action_temp_state_change".equals(action)) {
                boolean z2 = intent.getIntExtra("temp_state", 0) == 1;
                if (z2 && MiuiFlashlightControllerImpl.this.mFlashlightEnabled) {
                    Slog.d("FlashlightController", String.format("onReceive: forceOff=%b, state=%b, from=%s", Boolean.valueOf(z2), Boolean.FALSE, intent.getSender()));
                    MiuiFlashlightControllerImpl.this.setFlashlight(false);
                    MiuiFlashlightControllerImpl.this.postShowToast();
                }
                if (MiuiFlashlightControllerImpl.this.mForceOff != z2) {
                    MiuiFlashlightControllerImpl.this.mForceOff = z2;
                    MiuiFlashlightControllerImpl miuiFlashlightControllerImpl = MiuiFlashlightControllerImpl.this;
                    miuiFlashlightControllerImpl.dispatchAvailabilityChanged(miuiFlashlightControllerImpl.isAvailable());
                }
            }
        }
    };
    private Runnable mStatusDetecting;
    private boolean mTorchAvailable;
    private final CameraManager.TorchCallback mTorchCallback = new CameraManager.TorchCallback() {
        /* class com.android.systemui.statusbar.policy.MiuiFlashlightControllerImpl.AnonymousClass6 */

        public void onTorchModeUnavailable(String str) {
            if (TextUtils.equals(str, MiuiFlashlightControllerImpl.this.mCameraId)) {
                Slog.d("FlashlightController", "TorchCallback: onTorchModeUnavailable");
                setCameraAvailable(false);
            }
        }

        public void onTorchModeChanged(String str, boolean z) {
            if (TextUtils.equals(str, MiuiFlashlightControllerImpl.this.mCameraId)) {
                Slog.d("FlashlightController", "TorchCallback: onTorchModeChanged: enabled: " + z);
                setCameraAvailable(true);
                setTorchMode(z);
            }
        }

        private void setCameraAvailable(boolean z) {
            boolean z2;
            synchronized (MiuiFlashlightControllerImpl.this) {
                z2 = MiuiFlashlightControllerImpl.this.mTorchAvailable != z;
                MiuiFlashlightControllerImpl.this.mTorchAvailable = z;
            }
            if (z2) {
                if (MiuiFlashlightControllerImpl.DEBUG) {
                    Log.d("FlashlightController", "setCameraAvailable: dispatchAvailabilityChanged(" + z + ")");
                }
                MiuiFlashlightControllerImpl.this.dispatchAvailabilityChanged(z);
            }
        }

        private void setTorchMode(boolean z) {
            boolean z2;
            synchronized (MiuiFlashlightControllerImpl.this) {
                z2 = MiuiFlashlightControllerImpl.this.mFlashlightEnabled != z;
                MiuiFlashlightControllerImpl.this.mFlashlightEnabled = z;
            }
            if (z2) {
                if (MiuiFlashlightControllerImpl.DEBUG) {
                    Log.d("FlashlightController", "setCameraAvailable: dispatchModeChanged(" + z + ")");
                }
                MiuiFlashlightControllerImpl.this.dispatchModeChanged(z);
            }
        }
    };
    private int mValueOn;
    private String mWaringToastString;

    public MiuiFlashlightControllerImpl(Context context) {
        this.mContext = context;
        this.mCameraManager = (CameraManager) context.getSystemService("camera");
        this.mWaringToastString = this.mContext.getResources().getString(C0021R$string.torch_high_temperature_warning);
        ensureHandler();
        this.mBgHandler.post(new Runnable() {
            /* class com.android.systemui.statusbar.policy.MiuiFlashlightControllerImpl.AnonymousClass1 */

            public void run() {
                MiuiFlashlightControllerImpl.this.initFlash();
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initFlash() {
        if (Constants.SUPPORT_ANDROID_FLASHLIGHT) {
            initCameraFlash();
        } else {
            initMiuiFlash();
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("miui.intent.action.TOGGLE_TORCH");
        intentFilter.addAction("action_temp_state_change");
        intentFilter.setPriority(-1000);
        this.mContext.registerReceiverAsUser(this.mReceiver, UserHandle.ALL, intentFilter, null, this.mBgHandler);
    }

    private void initCameraFlash() {
        try {
            String cameraId = getCameraId();
            this.mCameraId = cameraId;
            if (cameraId != null) {
                Slog.d("FlashlightController", "initCameraFlash: register torch callback");
                this.mCameraManager.registerTorchCallback(this.mTorchCallback, this.mBgHandler);
            }
        } catch (Throwable th) {
            Log.e("FlashlightController", "Couldn't initialize.", th);
        }
    }

    private void initMiuiFlash() {
        Resources resources = this.mContext.getResources();
        this.mValueOn = resources.getInteger(C0016R$integer.flash_on_value);
        this.mFlashDevice = resources.getString(C0021R$string.flash_device);
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

    @Override // com.android.systemui.statusbar.policy.FlashlightController
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
                    /* class com.android.systemui.statusbar.policy.MiuiFlashlightControllerImpl.AnonymousClass3 */

                    public void run() {
                        MiuiFlashlightControllerImpl.this.setNormalFlashlight(z);
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
    /* access modifiers changed from: public */
    private void setNormalFlashlight(boolean z) {
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
    /* access modifiers changed from: public */
    private void setMiuiFlashlight(boolean z) {
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
                        /* class com.android.systemui.statusbar.policy.MiuiFlashlightControllerImpl.AnonymousClass4 */

                        /* JADX WARNING: Removed duplicated region for block: B:19:0x002e  */
                        /* JADX WARNING: Removed duplicated region for block: B:22:0x0035  */
                        /* JADX WARNING: Removed duplicated region for block: B:23:0x0040  */
                        /* JADX WARNING: Removed duplicated region for block: B:27:0x005b A[SYNTHETIC, Splitter:B:27:0x005b] */
                        /* Code decompiled incorrectly, please refer to instructions dump. */
                        public void run() {
                            /*
                                r6 = this;
                                r0 = 0
                                r1 = 1
                                r2 = 0
                                java.io.FileReader r3 = new java.io.FileReader     // Catch:{ Exception -> 0x0025, all -> 0x0023 }
                                com.android.systemui.statusbar.policy.MiuiFlashlightControllerImpl r4 = com.android.systemui.statusbar.policy.MiuiFlashlightControllerImpl.this     // Catch:{ Exception -> 0x0025, all -> 0x0023 }
                                java.lang.String r4 = com.android.systemui.statusbar.policy.MiuiFlashlightControllerImpl.access$600(r4)     // Catch:{ Exception -> 0x0025, all -> 0x0023 }
                                r3.<init>(r4)     // Catch:{ Exception -> 0x0025, all -> 0x0023 }
                                int r2 = r3.read()     // Catch:{ Exception -> 0x0021 }
                                r4 = 48
                                if (r2 != r4) goto L_0x0017
                                goto L_0x0018
                            L_0x0017:
                                r1 = r0
                            L_0x0018:
                                r3.close()     // Catch:{ IOException -> 0x001c }
                                goto L_0x0031
                            L_0x001c:
                                r2 = move-exception
                                r2.printStackTrace()
                                goto L_0x0031
                            L_0x0021:
                                r2 = move-exception
                                goto L_0x0029
                            L_0x0023:
                                r6 = move-exception
                                goto L_0x0059
                            L_0x0025:
                                r3 = move-exception
                                r5 = r3
                                r3 = r2
                                r2 = r5
                            L_0x0029:
                                r2.printStackTrace()     // Catch:{ all -> 0x0057 }
                                if (r3 == 0) goto L_0x0031
                                r3.close()
                            L_0x0031:
                                java.lang.String r2 = "FlashlightController"
                                if (r1 == 0) goto L_0x0040
                                java.lang.String r1 = "setFlashModeInternal: StatusDetectingRunnable: state change"
                                android.util.Slog.d(r2, r1)
                                com.android.systemui.statusbar.policy.MiuiFlashlightControllerImpl r6 = com.android.systemui.statusbar.policy.MiuiFlashlightControllerImpl.this
                                com.android.systemui.statusbar.policy.MiuiFlashlightControllerImpl.access$700(r6, r0)
                                goto L_0x0056
                            L_0x0040:
                                java.lang.String r0 = "setFlashModeInternal: in runnable, post delay StatusDetectingRunnable"
                                android.util.Slog.d(r2, r0)
                                com.android.systemui.statusbar.policy.MiuiFlashlightControllerImpl r0 = com.android.systemui.statusbar.policy.MiuiFlashlightControllerImpl.this
                                android.os.Handler r0 = com.android.systemui.statusbar.policy.MiuiFlashlightControllerImpl.access$900(r0)
                                com.android.systemui.statusbar.policy.MiuiFlashlightControllerImpl r6 = com.android.systemui.statusbar.policy.MiuiFlashlightControllerImpl.this
                                java.lang.Runnable r6 = com.android.systemui.statusbar.policy.MiuiFlashlightControllerImpl.access$800(r6)
                                r1 = 1000(0x3e8, double:4.94E-321)
                                r0.postDelayed(r6, r1)
                            L_0x0056:
                                return
                            L_0x0057:
                                r6 = move-exception
                                r2 = r3
                            L_0x0059:
                                if (r2 == 0) goto L_0x0063
                                r2.close()     // Catch:{ IOException -> 0x005f }
                                goto L_0x0063
                            L_0x005f:
                                r0 = move-exception
                                r0.printStackTrace()
                            L_0x0063:
                                throw r6
                            */
                            throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.policy.MiuiFlashlightControllerImpl.AnonymousClass4.run():void");
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
                    /* class com.android.systemui.statusbar.policy.MiuiFlashlightControllerImpl.AnonymousClass5 */

                    /* JADX WARNING: Removed duplicated region for block: B:25:0x0079  */
                    /* JADX WARNING: Removed duplicated region for block: B:29:0x0084 A[SYNTHETIC, Splitter:B:29:0x0084] */
                    /* JADX WARNING: Removed duplicated region for block: B:35:? A[RETURN, SYNTHETIC] */
                    /* Code decompiled incorrectly, please refer to instructions dump. */
                    public void run() {
                        /*
                        // Method dump skipped, instructions count: 142
                        */
                        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.policy.MiuiFlashlightControllerImpl.AnonymousClass5.run():void");
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }
    }

    @Override // com.android.systemui.statusbar.policy.FlashlightController
    public boolean hasFlashlight() {
        return Build.hasCameraFlash(this.mContext);
    }

    @Override // com.android.systemui.statusbar.policy.FlashlightController
    public synchronized boolean isEnabled() {
        return this.mFlashlightEnabled;
    }

    @Override // com.android.systemui.statusbar.policy.FlashlightController
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
            this.mListeners.add(new WeakReference<>(flashlightListener));
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
        String[] cameraIdList = this.mCameraManager.getCameraIdList();
        for (String str : cameraIdList) {
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
    /* access modifiers changed from: public */
    private void dispatchModeChanged(boolean z) {
        setTorchState(z);
        dispatchListeners(1, z);
    }

    private void dispatchError() {
        dispatchListeners(1, false);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dispatchAvailabilityChanged(boolean z) {
        dispatchListeners(2, z);
    }

    private void dispatchListeners(int i, boolean z) {
        synchronized (this.mListeners) {
            int size = this.mListeners.size();
            boolean z2 = false;
            for (int i2 = 0; i2 < size; i2++) {
                FlashlightController.FlashlightListener flashlightListener = this.mListeners.get(i2).get();
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
                cleanUpListenersLocked(null);
            }
        }
    }

    private void cleanUpListenersLocked(FlashlightController.FlashlightListener flashlightListener) {
        for (int size = this.mListeners.size() - 1; size >= 0; size--) {
            FlashlightController.FlashlightListener flashlightListener2 = this.mListeners.get(size).get();
            if (flashlightListener2 == null || flashlightListener2 == flashlightListener) {
                this.mListeners.remove(size);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void postShowToast() {
        this.mHandler.post(new Runnable() {
            /* class com.android.systemui.statusbar.policy.MiuiFlashlightControllerImpl.AnonymousClass7 */

            public void run() {
                ChargeUtils.showSystemOverlayToast(MiuiFlashlightControllerImpl.this.mContext, MiuiFlashlightControllerImpl.this.mWaringToastString, 1);
            }
        });
    }

    @Override // com.android.systemui.Dumpable
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
