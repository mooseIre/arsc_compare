package com.android.systemui.usb;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbDevice;

class UsbDisconnectedReceiver extends BroadcastReceiver {
    private UsbAccessory mAccessory;
    private final Activity mActivity;
    private UsbDevice mDevice;

    public UsbDisconnectedReceiver(Activity activity, UsbDevice usbDevice) {
        this.mActivity = activity;
        this.mDevice = usbDevice;
        activity.registerReceiver(this, new IntentFilter("android.hardware.usb.action.USB_DEVICE_DETACHED"));
    }

    public UsbDisconnectedReceiver(Activity activity, UsbAccessory usbAccessory) {
        this.mActivity = activity;
        this.mAccessory = usbAccessory;
        activity.registerReceiver(this, new IntentFilter("android.hardware.usb.action.USB_ACCESSORY_DETACHED"));
    }

    public void onReceive(Context context, Intent intent) {
        UsbAccessory usbAccessory;
        String action = intent.getAction();
        if ("android.hardware.usb.action.USB_DEVICE_DETACHED".equals(action)) {
            UsbDevice usbDevice = (UsbDevice) intent.getParcelableExtra("device");
            if (usbDevice != null && usbDevice.equals(this.mDevice)) {
                this.mActivity.finish();
            }
        } else if ("android.hardware.usb.action.USB_ACCESSORY_DETACHED".equals(action) && (usbAccessory = (UsbAccessory) intent.getParcelableExtra("accessory")) != null && usbAccessory.equals(this.mAccessory)) {
            this.mActivity.finish();
        }
    }
}
