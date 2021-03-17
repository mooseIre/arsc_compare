package com.android.systemui.usb;

import android.app.Activity;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.PermissionChecker;
import android.content.pm.ResolveInfo;
import android.hardware.usb.IUsbManager;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController;
import com.android.systemui.C0021R$string;

public class UsbConfirmActivity extends AlertActivity implements DialogInterface.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private UsbAccessory mAccessory;
    private CheckBox mAlwaysUse;
    private TextView mClearDefaultHint;
    private UsbDevice mDevice;
    private UsbDisconnectedReceiver mDisconnectedReceiver;
    private ResolveInfo mResolveInfo;

    /* JADX DEBUG: Multi-variable search result rejected for r8v0, resolved type: com.android.systemui.usb.UsbConfirmActivity */
    /* JADX WARN: Multi-variable type inference failed */
    public void onCreate(Bundle bundle) {
        boolean z;
        int i;
        UsbConfirmActivity.super.onCreate(bundle);
        Intent intent = getIntent();
        this.mDevice = (UsbDevice) intent.getParcelableExtra("device");
        this.mAccessory = (UsbAccessory) intent.getParcelableExtra("accessory");
        this.mResolveInfo = (ResolveInfo) intent.getParcelableExtra("rinfo");
        String stringExtra = intent.getStringExtra("android.hardware.usb.extra.PACKAGE");
        String charSequence = this.mResolveInfo.loadLabel(getPackageManager()).toString();
        AlertController.AlertParams alertParams = ((AlertActivity) this).mAlertParams;
        alertParams.mTitle = charSequence;
        if (this.mDevice == null) {
            alertParams.mMessage = getString(C0021R$string.usb_accessory_confirm_prompt, new Object[]{charSequence, this.mAccessory.getDescription()});
            this.mDisconnectedReceiver = new UsbDisconnectedReceiver((Activity) this, this.mAccessory);
            z = false;
        } else {
            z = this.mDevice.getHasAudioCapture() && !(PermissionChecker.checkPermissionForPreflight(this, "android.permission.RECORD_AUDIO", -1, intent.getIntExtra("android.intent.extra.UID", -1), stringExtra) == 0);
            if (z) {
                i = C0021R$string.usb_device_confirm_prompt_warn;
            } else {
                i = C0021R$string.usb_device_confirm_prompt;
            }
            alertParams.mMessage = getString(i, new Object[]{charSequence, this.mDevice.getProductName()});
            this.mDisconnectedReceiver = new UsbDisconnectedReceiver((Activity) this, this.mDevice);
        }
        alertParams.mPositiveButtonText = getString(17039370);
        alertParams.mNegativeButtonText = getString(17039360);
        alertParams.mPositiveButtonListener = this;
        alertParams.mNegativeButtonListener = this;
        if (!z) {
            View inflate = ((LayoutInflater) getSystemService("layout_inflater")).inflate(17367092, (ViewGroup) null);
            alertParams.mView = inflate;
            CheckBox checkBox = (CheckBox) inflate.findViewById(16908753);
            this.mAlwaysUse = checkBox;
            UsbDevice usbDevice = this.mDevice;
            if (usbDevice == null) {
                checkBox.setText(getString(C0021R$string.always_use_accessory, new Object[]{charSequence, this.mAccessory.getDescription()}));
            } else {
                checkBox.setText(getString(C0021R$string.always_use_device, new Object[]{charSequence, usbDevice.getProductName()}));
            }
            this.mAlwaysUse.setOnCheckedChangeListener(this);
            TextView textView = (TextView) alertParams.mView.findViewById(16908848);
            this.mClearDefaultHint = textView;
            textView.setVisibility(8);
        }
        setupAlert();
    }

    /* access modifiers changed from: protected */
    public void onDestroy() {
        UsbDisconnectedReceiver usbDisconnectedReceiver = this.mDisconnectedReceiver;
        if (usbDisconnectedReceiver != null) {
            unregisterReceiver(usbDisconnectedReceiver);
        }
        UsbConfirmActivity.super.onDestroy();
    }

    public void onClick(DialogInterface dialogInterface, int i) {
        Intent intent;
        if (i == -1) {
            try {
                IUsbManager asInterface = IUsbManager.Stub.asInterface(ServiceManager.getService("usb"));
                int i2 = this.mResolveInfo.activityInfo.applicationInfo.uid;
                int myUserId = UserHandle.myUserId();
                boolean isChecked = this.mAlwaysUse != null ? this.mAlwaysUse.isChecked() : false;
                Intent intent2 = null;
                if (this.mDevice != null) {
                    intent = new Intent("android.hardware.usb.action.USB_DEVICE_ATTACHED");
                    intent.putExtra("device", this.mDevice);
                    asInterface.grantDevicePermission(this.mDevice, i2);
                    if (isChecked) {
                        asInterface.setDevicePackage(this.mDevice, this.mResolveInfo.activityInfo.packageName, myUserId);
                    } else {
                        asInterface.setDevicePackage(this.mDevice, (String) null, myUserId);
                    }
                } else {
                    if (this.mAccessory != null) {
                        intent = new Intent("android.hardware.usb.action.USB_ACCESSORY_ATTACHED");
                        intent.putExtra("accessory", this.mAccessory);
                        asInterface.grantAccessoryPermission(this.mAccessory, i2);
                        if (isChecked) {
                            asInterface.setAccessoryPackage(this.mAccessory, this.mResolveInfo.activityInfo.packageName, myUserId);
                        } else {
                            asInterface.setAccessoryPackage(this.mAccessory, (String) null, myUserId);
                        }
                    }
                    intent2.addFlags(268435456);
                    intent2.setComponent(new ComponentName(this.mResolveInfo.activityInfo.packageName, this.mResolveInfo.activityInfo.name));
                    startActivityAsUser(intent2, new UserHandle(myUserId));
                }
                intent2 = intent;
                intent2.addFlags(268435456);
                intent2.setComponent(new ComponentName(this.mResolveInfo.activityInfo.packageName, this.mResolveInfo.activityInfo.name));
                startActivityAsUser(intent2, new UserHandle(myUserId));
            } catch (Exception e) {
                Log.e("UsbConfirmActivity", "Unable to start activity", e);
            }
        }
        finish();
    }

    public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
        TextView textView = this.mClearDefaultHint;
        if (textView != null) {
            if (z) {
                textView.setVisibility(0);
            } else {
                textView.setVisibility(8);
            }
        }
    }
}
