package com.android.systemui.controlcenter.policy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.Slog;
import com.android.keyguard.charge.ChargeUtils;
import com.android.systemui.C0016R$integer;
import com.android.systemui.C0021R$string;
import com.android.systemui.statusbar.policy.FlashlightController;
import java.io.File;

public class MiuiFlashlightHelper {
    public static final String[] FLASH_DEVICES = {"/sys/class/leds/flashlight/brightness", "/sys/class/leds/spotlight/brightness"};
    private Handler mBgHandler;
    private final Context mContext;
    private String mFlashDevice;
    private FlashlightController mFlashlightController;
    private boolean mForceOff;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        /* class com.android.systemui.controlcenter.policy.MiuiFlashlightHelper.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            boolean z;
            String action = intent.getAction();
            if ("miui.intent.action.TOGGLE_TORCH".equals(action)) {
                if (MiuiFlashlightHelper.this.mForceOff) {
                    MiuiFlashlightHelper.this.showToast();
                    return;
                }
                boolean booleanExtra = intent.getBooleanExtra("miui.intent.extra.IS_TOGGLE", false);
                if (booleanExtra) {
                    z = !MiuiFlashlightHelper.this.mFlashlightController.isEnabled();
                } else {
                    z = intent.getBooleanExtra("miui.intent.extra.IS_ENABLE", false);
                }
                Slog.d("FlashlightController", String.format("onReceive: isToggle=%b, newState=%b, from=%s", Boolean.valueOf(booleanExtra), Boolean.valueOf(z), intent.getSender()));
                MiuiFlashlightHelper.this.mFlashlightController.setFlashlight(z);
            } else if ("action_temp_state_change".equals(action)) {
                boolean z2 = intent.getIntExtra("temp_state", 0) == 1;
                MiuiFlashlightHelper.this.mForceOff = z2;
                if (z2 && MiuiFlashlightHelper.this.mFlashlightController.isEnabled()) {
                    Slog.d("FlashlightController", String.format("onReceive: forceOff=%b, state=%b, from=%s", Boolean.valueOf(z2), Boolean.FALSE, intent.getSender()));
                    MiuiFlashlightHelper.this.mFlashlightController.setFlashlight(false);
                    MiuiFlashlightHelper.this.showToast();
                }
            }
        }
    };
    private Runnable mStatusDetecting;
    private int mValueOn;
    private String mWaringToastString;

    public MiuiFlashlightHelper(Context context) {
        this.mContext = context;
    }

    public void setFlashlightController(FlashlightController flashlightController) {
        this.mFlashlightController = flashlightController;
        this.mWaringToastString = this.mContext.getResources().getString(C0021R$string.torch_high_temperature_warning);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void showToast() {
        ChargeUtils.showSystemOverlayToast(this.mContext, this.mWaringToastString, 1);
    }

    public synchronized void ensureHandler(Handler handler) {
        if (this.mBgHandler == null) {
            this.mBgHandler = handler;
        }
    }

    public boolean isForceOff() {
        return this.mForceOff;
    }

    public void tryInitCamera() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("miui.intent.action.TOGGLE_TORCH");
        intentFilter.addAction("action_temp_state_change");
        intentFilter.setPriority(-1000);
        this.mContext.registerReceiverAsUser(this.mReceiver, UserHandle.ALL, intentFilter, null, null);
    }

    public void initMiuiFlash() {
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

    public boolean setMiuiFlashlight(boolean z) {
        Log.d("FlashlightController", "setMiuiFlashlight:" + z);
        return setMiuiFlashModeInternal(z);
    }

    private synchronized boolean setMiuiFlashModeInternal(final boolean z) {
        if (!this.mFlashlightController.hasFlashlight()) {
            return false;
        }
        if (TextUtils.isEmpty(this.mFlashDevice)) {
            Slog.d("FlashlightController", "setFlashModeInternal: no device node");
            return false;
        }
        try {
            if (this.mStatusDetecting == null) {
                this.mStatusDetecting = new Runnable() {
                    /* class com.android.systemui.controlcenter.policy.MiuiFlashlightHelper.AnonymousClass2 */

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
                            com.android.systemui.controlcenter.policy.MiuiFlashlightHelper r4 = com.android.systemui.controlcenter.policy.MiuiFlashlightHelper.this     // Catch:{ Exception -> 0x0025, all -> 0x0023 }
                            java.lang.String r4 = com.android.systemui.controlcenter.policy.MiuiFlashlightHelper.access$300(r4)     // Catch:{ Exception -> 0x0025, all -> 0x0023 }
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
                            com.android.systemui.controlcenter.policy.MiuiFlashlightHelper r6 = com.android.systemui.controlcenter.policy.MiuiFlashlightHelper.this
                            r6.setMiuiFlashlight(r0)
                            goto L_0x0056
                        L_0x0040:
                            java.lang.String r0 = "setFlashModeInternal: in runnable, post delay StatusDetectingRunnable"
                            android.util.Slog.d(r2, r0)
                            com.android.systemui.controlcenter.policy.MiuiFlashlightHelper r0 = com.android.systemui.controlcenter.policy.MiuiFlashlightHelper.this
                            android.os.Handler r0 = com.android.systemui.controlcenter.policy.MiuiFlashlightHelper.access$500(r0)
                            com.android.systemui.controlcenter.policy.MiuiFlashlightHelper r6 = com.android.systemui.controlcenter.policy.MiuiFlashlightHelper.this
                            java.lang.Runnable r6 = com.android.systemui.controlcenter.policy.MiuiFlashlightHelper.access$400(r6)
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
                        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.controlcenter.policy.MiuiFlashlightHelper.AnonymousClass2.run():void");
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
                /* class com.android.systemui.controlcenter.policy.MiuiFlashlightHelper.AnonymousClass3 */

                /* JADX WARNING: Removed duplicated region for block: B:25:0x0079  */
                /* JADX WARNING: Removed duplicated region for block: B:29:0x0084 A[SYNTHETIC, Splitter:B:29:0x0084] */
                /* JADX WARNING: Removed duplicated region for block: B:35:? A[RETURN, SYNTHETIC] */
                /* Code decompiled incorrectly, please refer to instructions dump. */
                public void run() {
                    /*
                    // Method dump skipped, instructions count: 142
                    */
                    throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.controlcenter.policy.MiuiFlashlightHelper.AnonymousClass3.run():void");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public void setTorchState(boolean z) {
        Slog.d("FlashlightController", "setTorchState: enabled: " + z);
        Settings.Global.putInt(this.mContext.getContentResolver(), "torch_state", z ? 1 : 0);
    }
}
