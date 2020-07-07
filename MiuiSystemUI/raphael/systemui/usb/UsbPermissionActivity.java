package com.android.systemui.usb;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.hardware.usb.IUsbManager;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbDevice;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController;
import com.android.systemui.plugins.R;

public class UsbPermissionActivity extends AlertActivity implements DialogInterface.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private UsbAccessory mAccessory;
    private CheckBox mAlwaysUse;
    private TextView mClearDefaultHint;
    private UsbDevice mDevice;
    private UsbDisconnectedReceiver mDisconnectedReceiver;
    private String mPackageName;
    private PendingIntent mPendingIntent;
    private boolean mPermissionGranted;
    private int mUid;

    /* JADX WARNING: type inference failed for: r6v0, types: [android.content.DialogInterface$OnClickListener, com.android.internal.app.AlertActivity, com.android.systemui.usb.UsbPermissionActivity, android.widget.CompoundButton$OnCheckedChangeListener, android.app.Activity] */
    public void onCreate(Bundle bundle) {
        UsbPermissionActivity.super.onCreate(bundle);
        Intent intent = getIntent();
        this.mDevice = (UsbDevice) intent.getParcelableExtra("device");
        this.mAccessory = (UsbAccessory) intent.getParcelableExtra("accessory");
        this.mPendingIntent = (PendingIntent) intent.getParcelableExtra("android.intent.extra.INTENT");
        this.mUid = intent.getIntExtra("android.intent.extra.UID", -1);
        this.mPackageName = intent.getStringExtra(Build.VERSION.SDK_INT < 29 ? "package" : "android.hardware.usb.extra.PACKAGE");
        boolean booleanExtra = intent.getBooleanExtra("android.hardware.usb.extra.CAN_BE_DEFAULT", false);
        PackageManager packageManager = getPackageManager();
        try {
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(this.mPackageName, 0);
            String charSequence = applicationInfo.loadLabel(packageManager).toString();
            AlertController.AlertParams alertParams = this.mAlertParams;
            alertParams.mIcon = applicationInfo.loadIcon(packageManager);
            alertParams.mTitle = charSequence;
            if (this.mDevice == null) {
                alertParams.mMessage = getString(R.string.usb_accessory_permission_prompt, new Object[]{charSequence});
                this.mDisconnectedReceiver = new UsbDisconnectedReceiver((Activity) this, this.mAccessory);
            } else {
                alertParams.mMessage = getString(R.string.usb_device_permission_prompt, new Object[]{charSequence});
                this.mDisconnectedReceiver = new UsbDisconnectedReceiver((Activity) this, this.mDevice);
            }
            alertParams.mPositiveButtonText = getString(17039370);
            alertParams.mNegativeButtonText = getString(17039360);
            alertParams.mPositiveButtonListener = this;
            alertParams.mNegativeButtonListener = this;
            if (Build.VERSION.SDK_INT < 29 || (booleanExtra && !(this.mDevice == null && this.mAccessory == null))) {
                alertParams.mView = ((LayoutInflater) getSystemService("layout_inflater")).inflate(17367090, (ViewGroup) null);
                this.mAlwaysUse = (CheckBox) alertParams.mView.findViewById(16908724);
                if (this.mDevice == null) {
                    this.mAlwaysUse.setText(R.string.always_use_accessory);
                } else {
                    this.mAlwaysUse.setText(R.string.always_use_device);
                }
                this.mAlwaysUse.setOnCheckedChangeListener(this);
                this.mClearDefaultHint = (TextView) alertParams.mView.findViewById(16908819);
                this.mClearDefaultHint.setVisibility(8);
            }
            setupAlert();
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("UsbPermissionActivity", "unable to look up package name", e);
            finish();
        }
    }

    /* JADX WARNING: type inference failed for: r6v0, types: [android.content.Context, com.android.internal.app.AlertActivity, com.android.systemui.usb.UsbPermissionActivity] */
    public void onDestroy() {
        IUsbManager asInterface = IUsbManager.Stub.asInterface(ServiceManager.getService("usb"));
        Intent intent = new Intent();
        try {
            if (this.mDevice != null) {
                intent.putExtra("device", this.mDevice);
                if (this.mPermissionGranted) {
                    asInterface.grantDevicePermission(this.mDevice, this.mUid);
                    if (this.mAlwaysUse != null && this.mAlwaysUse.isChecked()) {
                        asInterface.setDevicePackage(this.mDevice, this.mPackageName, UserHandle.getUserId(this.mUid));
                    }
                }
            }
            if (this.mAccessory != null) {
                intent.putExtra("accessory", this.mAccessory);
                if (this.mPermissionGranted) {
                    asInterface.grantAccessoryPermission(this.mAccessory, this.mUid);
                    if (this.mAlwaysUse != null && this.mAlwaysUse.isChecked()) {
                        asInterface.setAccessoryPackage(this.mAccessory, this.mPackageName, UserHandle.getUserId(this.mUid));
                    }
                }
            }
            intent.putExtra("permission", this.mPermissionGranted);
            this.mPendingIntent.send(this, 0, intent);
        } catch (PendingIntent.CanceledException unused) {
            Log.w("UsbPermissionActivity", "PendingIntent was cancelled");
        } catch (RemoteException e) {
            Log.e("UsbPermissionActivity", "IUsbService connection failed", e);
        }
        UsbDisconnectedReceiver usbDisconnectedReceiver = this.mDisconnectedReceiver;
        if (usbDisconnectedReceiver != null) {
            unregisterReceiver(usbDisconnectedReceiver);
        }
        UsbPermissionActivity.super.onDestroy();
    }

    public void onClick(DialogInterface dialogInterface, int i) {
        if (i == -1) {
            this.mPermissionGranted = true;
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
