package com.android.systemui.statusbar.notification.policy;

import android.app.AlertDialog;
import android.app.MiuiThemeHelper;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import com.android.systemui.C0021R$string;
import com.android.systemui.C0022R$style;
import com.android.systemui.statusbar.notification.ExpandedNotification;
import com.miui.systemui.BuildConfig;
import miui.telephony.TelephonyManager;
import miui.util.ResourceMapper;

public class UsbNotificationController {
    public static final boolean SUPPORT_DISABLE_USB_BY_SIM = (BuildConfig.IS_CM_CUSTOMIZATION_TEST || BuildConfig.IS_CM_CUSTOMIZATION);
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        /* class com.android.systemui.statusbar.notification.policy.UsbNotificationController.AnonymousClass2 */

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            boolean z = false;
            if ("android.intent.action.BATTERY_CHANGED".equals(action)) {
                int i = UsbNotificationController.this.mPlugType;
                UsbNotificationController.this.mPlugType = intent.getIntExtra("plugged", 0);
                boolean z2 = UsbNotificationController.this.mPlugType == 2;
                if (i == 2) {
                    z = true;
                }
                if (z2 != z) {
                    UsbNotificationController.this.refreshWhenUsbConnectChanged(z2);
                }
            } else if ("android.hardware.usb.action.USB_STATE".equals(action)) {
                UsbNotificationController.this.refreshWhenUsbConnectChanged(intent.getBooleanExtra("connected", false));
            } else if ("android.intent.action.SIM_STATE_CHANGED".equals(action) && UsbNotificationController.SUPPORT_DISABLE_USB_BY_SIM) {
                int phoneCount = TelephonyManager.getDefault().getPhoneCount();
                int i2 = 0;
                for (int i3 = 0; i3 < phoneCount; i3++) {
                    if (TelephonyManager.getDefault().hasIccCard(i3)) {
                        i2++;
                    }
                }
                if (i2 > 0) {
                    Log.d("UsbNotificationController", "has sim");
                    Settings.System.putInt(UsbNotificationController.this.mContext.getContentResolver(), "disable_usb_by_sim", 0);
                }
            }
        }
    };
    private int mCdInstallNotificationId;
    private int mChargingNotificationId;
    private Context mContext;
    private boolean mDisableUsbBySim;
    private final ContentObserver mDisableUsbObserver = new ContentObserver(this.mHandler) {
        /* class com.android.systemui.statusbar.notification.policy.UsbNotificationController.AnonymousClass1 */

        public void onChange(boolean z) {
            boolean z2 = UsbNotificationController.SUPPORT_DISABLE_USB_BY_SIM;
            UsbNotificationController usbNotificationController = UsbNotificationController.this;
            boolean z3 = true;
            usbNotificationController.mDisableUsbBySim = Settings.System.getInt(usbNotificationController.mContext.getContentResolver(), "disable_usb_by_sim", z2 ? 1 : 0) != 0;
            if (!UsbNotificationController.SUPPORT_DISABLE_USB_BY_SIM && UsbNotificationController.this.mDisableUsbBySim) {
                Log.d("UsbNotificationController", "not support disable usb by sim!");
                UsbNotificationController.this.mDisableUsbBySim = false;
                Settings.System.putInt(UsbNotificationController.this.mContext.getContentResolver(), "disable_usb_by_sim", 0);
            }
            if (!UsbNotificationController.this.mDisableUsbBySim) {
                if (UsbNotificationController.this.mIsDialogShowing && UsbNotificationController.this.mUsbAlert != null) {
                    UsbNotificationController.this.mUsbAlert.dismiss();
                }
                UsbNotificationController usbNotificationController2 = UsbNotificationController.this;
                if (usbNotificationController2.mPlugType != 2) {
                    z3 = false;
                }
                usbNotificationController2.refreshWhenUsbConnectChanged(z3);
            }
        }
    };
    private boolean mEnableUsbModeSelection;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private boolean mIsDialogShowing;
    private boolean mIsScreenshotMode;
    private int mMtpNotificationId;
    private int mPlugType = 0;
    private int mPtpNotificationId;
    private AlertDialog mUsbAlert;
    private UsbManager mUsbManager;

    public UsbNotificationController(Context context) {
        this.mContext = context;
        this.mUsbManager = (UsbManager) context.getSystemService("usb");
        this.mPtpNotificationId = ResourceMapper.resolveReference(this.mContext.getResources(), 286195758);
        this.mMtpNotificationId = ResourceMapper.resolveReference(this.mContext.getResources(), 286195757);
        this.mCdInstallNotificationId = ResourceMapper.resolveReference(this.mContext.getResources(), 286195756);
        int identifier = this.mContext.getResources().getIdentifier("usb_charging_notification_title", "string", "com.mediatek");
        this.mChargingNotificationId = identifier;
        if (identifier == 0) {
            this.mChargingNotificationId = this.mContext.getResources().getIdentifier("usb_charging_notification_title", "string", "android");
        }
        this.mIsScreenshotMode = MiuiThemeHelper.isScreenshotMode();
        this.mEnableUsbModeSelection = this.mContext.getResources().getBoolean(285474847);
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("disable_usb_by_sim"), false, this.mDisableUsbObserver);
        this.mDisableUsbObserver.onChange(false);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.BATTERY_CHANGED");
        intentFilter.addAction("android.hardware.usb.action.USB_STATE");
        if (SUPPORT_DISABLE_USB_BY_SIM) {
            intentFilter.addAction("android.intent.action.SIM_STATE_CHANGED");
        }
        this.mContext.registerReceiver(this.mBroadcastReceiver, intentFilter);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void refreshWhenUsbConnectChanged(boolean z) {
        if (SUPPORT_DISABLE_USB_BY_SIM && z && this.mDisableUsbBySim && !this.mIsDialogShowing) {
            this.mIsDialogShowing = true;
            AlertDialog.Builder builder = new AlertDialog.Builder(this.mContext, C0022R$style.Theme_Dialog_Alert);
            builder.setCancelable(true);
            builder.setTitle(C0021R$string.activate_usb_title);
            builder.setMessage(C0021R$string.activate_usb_message);
            builder.setIconAttribute(16843605);
            builder.setPositiveButton(17039370, (DialogInterface.OnClickListener) null);
            AlertDialog create = builder.create();
            this.mUsbAlert = create;
            create.getWindow().setType(2003);
            this.mUsbAlert.setOnDismissListener(new DialogInterface.OnDismissListener() {
                /* class com.android.systemui.statusbar.notification.policy.UsbNotificationController.AnonymousClass3 */

                public void onDismiss(DialogInterface dialogInterface) {
                    UsbNotificationController.this.mIsDialogShowing = false;
                }
            });
            this.mUsbAlert.show();
            this.mUsbManager.setCurrentFunction("none", false);
            Settings.Global.putInt(this.mContext.getContentResolver(), "adb_enabled", 0);
        }
    }

    public boolean needDisableUsbNotification(ExpandedNotification expandedNotification) {
        return (this.mDisableUsbBySim || this.mIsScreenshotMode) && isUsbNotification(expandedNotification);
    }

    public boolean isUsbNotification(ExpandedNotification expandedNotification) {
        return isMtpSwitcherNotification(expandedNotification) || isUsbModeNotification(expandedNotification) || isChargingNotification(expandedNotification) || isUsbHeadsetNotification(expandedNotification);
    }

    private boolean isChargingNotification(ExpandedNotification expandedNotification) {
        int id = expandedNotification.getId();
        return "android".equals(expandedNotification.getPackageName()) && (id == this.mChargingNotificationId || id == 32);
    }

    private boolean isUsbHeadsetNotification(ExpandedNotification expandedNotification) {
        int id = expandedNotification.getId();
        return "android".equals(expandedNotification.getPackageName()) && (id == 1397122662 || id == 1397262472);
    }

    private boolean isMtpSwitcherNotification(ExpandedNotification expandedNotification) {
        int id = expandedNotification.getId();
        return "android".equals(expandedNotification.getPackageName()) && (id == this.mPtpNotificationId || id == this.mMtpNotificationId || id == this.mCdInstallNotificationId);
    }

    private boolean isUsbModeNotification(ExpandedNotification expandedNotification) {
        int id = expandedNotification.getId();
        return this.mEnableUsbModeSelection && "com.android.systemui".equals(expandedNotification.getPackageName()) && (id == 1397773634 || id == 1397772886 || id == 1396986699 || id == 1397575510);
    }
}
