package com.android.systemui.miui.statusbar.policy;

import android.app.AlertDialog;
import android.app.MiuiThemeHelper;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbManagerCompat;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import com.android.systemui.Constants;
import com.android.systemui.miui.statusbar.ExpandedNotification;
import com.android.systemui.plugins.R;
import com.miui.systemui.annotation.Inject;
import miui.util.ResourceMapper;

public class UsbNotificationController {
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.intent.action.BATTERY_CHANGED".equals(action)) {
                int access$400 = UsbNotificationController.this.mPlugType;
                int unused = UsbNotificationController.this.mPlugType = intent.getIntExtra("plugged", 0);
                boolean z = true;
                boolean z2 = UsbNotificationController.this.mPlugType == 2;
                if (access$400 != 2) {
                    z = false;
                }
                if (z2 != z) {
                    UsbNotificationController.this.refreshWhenUsbConnectChanged(z2);
                }
            } else if ("android.hardware.usb.action.USB_STATE".equals(action)) {
                UsbNotificationController.this.refreshWhenUsbConnectChanged(intent.getBooleanExtra("connected", false));
            }
        }
    };
    private int mCdInstallNotificationId;
    private int mChargingNotificationId;
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public boolean mDisableUsbBySim;
    private final ContentObserver mDisableUsbObserver = new ContentObserver(this.mHandler) {
        public void onChange(boolean z) {
            boolean z2 = Constants.SUPPORT_DISABLE_USB_BY_SIM;
            UsbNotificationController usbNotificationController = UsbNotificationController.this;
            boolean z3 = true;
            boolean unused = usbNotificationController.mDisableUsbBySim = Settings.System.getInt(usbNotificationController.mContext.getContentResolver(), "disable_usb_by_sim", z2 ? 1 : 0) != 0;
            if (!Constants.SUPPORT_DISABLE_USB_BY_SIM && UsbNotificationController.this.mDisableUsbBySim) {
                Log.d("UsbNotificationController", "not support disable usb by sim!");
                boolean unused2 = UsbNotificationController.this.mDisableUsbBySim = false;
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
    /* access modifiers changed from: private */
    public boolean mIsDialogShowing;
    private boolean mIsScreenshotMode;
    private int mMtpNotificationId;
    /* access modifiers changed from: private */
    public int mPlugType = 0;
    private int mPtpNotificationId;
    /* access modifiers changed from: private */
    public AlertDialog mUsbAlert;
    private UsbManager mUsbManager;

    public UsbNotificationController(@Inject Context context) {
        this.mContext = context;
        this.mIsScreenshotMode = MiuiThemeHelper.isScreenshotMode();
        this.mUsbManager = (UsbManager) this.mContext.getSystemService("usb");
        this.mPtpNotificationId = ResourceMapper.resolveReference(this.mContext.getResources(), 286130222);
        this.mMtpNotificationId = ResourceMapper.resolveReference(this.mContext.getResources(), 286130221);
        this.mCdInstallNotificationId = ResourceMapper.resolveReference(this.mContext.getResources(), 286130220);
        this.mEnableUsbModeSelection = this.mContext.getResources().getBoolean(285474845);
        this.mChargingNotificationId = this.mContext.getResources().getIdentifier("usb_charging_notification_title", "string", "com.mediatek");
        if (this.mChargingNotificationId == 0) {
            this.mChargingNotificationId = this.mContext.getResources().getIdentifier("usb_charging_notification_title", "string", "android");
        }
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("disable_usb_by_sim"), false, this.mDisableUsbObserver);
        this.mDisableUsbObserver.onChange(false);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.BATTERY_CHANGED");
        intentFilter.addAction("android.hardware.usb.action.USB_STATE");
        this.mContext.registerReceiver(this.mBroadcastReceiver, intentFilter);
    }

    /* access modifiers changed from: private */
    public void refreshWhenUsbConnectChanged(boolean z) {
        if (Constants.SUPPORT_DISABLE_USB_BY_SIM && z && this.mDisableUsbBySim && !this.mIsDialogShowing) {
            this.mIsDialogShowing = true;
            AlertDialog.Builder builder = new AlertDialog.Builder(this.mContext, R.style.Theme_Dialog_Alert);
            builder.setCancelable(true);
            builder.setTitle(R.string.activate_usb_title);
            builder.setMessage(R.string.activate_usb_message);
            builder.setIconAttribute(16843605);
            builder.setPositiveButton(17039370, (DialogInterface.OnClickListener) null);
            this.mUsbAlert = builder.create();
            this.mUsbAlert.getWindow().setType(2003);
            this.mUsbAlert.setOnDismissListener(new DialogInterface.OnDismissListener() {
                public void onDismiss(DialogInterface dialogInterface) {
                    boolean unused = UsbNotificationController.this.mIsDialogShowing = false;
                }
            });
            this.mUsbAlert.show();
            UsbManagerCompat.setCurrentFunction(this.mUsbManager, "none", false);
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
