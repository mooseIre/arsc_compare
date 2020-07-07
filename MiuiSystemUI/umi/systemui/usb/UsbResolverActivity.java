package com.android.systemui.usb;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.pm.ResolveInfoCompat;
import android.hardware.usb.IUsbManager;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.os.UserHandleCompat;
import android.util.Log;
import android.widget.CheckBox;
import com.android.internal.app.IntentForwarderActivity;
import com.android.internal.app.ResolverActivity;
import com.android.internal.app.chooser.TargetInfo;
import com.android.systemui.plugins.R;
import java.util.ArrayList;
import java.util.Iterator;

public class UsbResolverActivity extends ResolverActivity {
    private UsbAccessory mAccessory;
    private UsbDevice mDevice;
    private UsbDisconnectedReceiver mDisconnectedReceiver;
    private ResolveInfo mForwardResolveInfo;
    private Intent mOtherProfileIntent;

    /* JADX WARNING: type inference failed for: r11v0, types: [android.content.Context, com.android.internal.app.ResolverActivity, com.android.systemui.usb.UsbResolverActivity, android.app.Activity] */
    /* access modifiers changed from: protected */
    public void onCreate(Bundle bundle) {
        Intent intent = getIntent();
        Parcelable parcelableExtra = intent.getParcelableExtra("android.intent.extra.INTENT");
        if (!(parcelableExtra instanceof Intent)) {
            Log.w("UsbResolverActivity", "Target is not an intent: " + parcelableExtra);
            finish();
            return;
        }
        Intent intent2 = (Intent) parcelableExtra;
        ArrayList arrayList = new ArrayList(intent.getParcelableArrayListExtra("rlist"));
        ArrayList arrayList2 = new ArrayList(arrayList.size());
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            arrayList2.add((ResolveInfo) ((Parcelable) it.next()));
        }
        ArrayList arrayList3 = new ArrayList();
        this.mForwardResolveInfo = null;
        Iterator it2 = arrayList2.iterator();
        while (it2.hasNext()) {
            ResolveInfo resolveInfo = (ResolveInfo) it2.next();
            if (ResolveInfoCompat.getComponentInfo(resolveInfo).name.equals(IntentForwarderActivity.FORWARD_INTENT_TO_MANAGED_PROFILE)) {
                this.mForwardResolveInfo = resolveInfo;
            } else if (UserHandle.getUserId(resolveInfo.activityInfo.applicationInfo.uid) != UserHandle.myUserId()) {
                it2.remove();
                arrayList3.add(resolveInfo);
            }
        }
        UsbDevice usbDevice = (UsbDevice) intent2.getParcelableExtra("device");
        this.mDevice = usbDevice;
        if (usbDevice != null) {
            this.mDisconnectedReceiver = new UsbDisconnectedReceiver((Activity) this, this.mDevice);
        } else {
            UsbAccessory usbAccessory = (UsbAccessory) intent2.getParcelableExtra("accessory");
            this.mAccessory = usbAccessory;
            if (usbAccessory == null) {
                Log.e("UsbResolverActivity", "no device or accessory");
                finish();
                return;
            }
            this.mDisconnectedReceiver = new UsbDisconnectedReceiver((Activity) this, this.mAccessory);
        }
        if (this.mForwardResolveInfo != null) {
            if (arrayList3.size() > 1) {
                Intent intent3 = new Intent(intent);
                this.mOtherProfileIntent = intent3;
                intent3.putParcelableArrayListExtra("rlist", arrayList3);
            } else {
                Intent intent4 = new Intent(this, UsbConfirmActivity.class);
                this.mOtherProfileIntent = intent4;
                intent4.putExtra("rinfo", (Parcelable) arrayList3.get(0));
                UsbDevice usbDevice2 = this.mDevice;
                if (usbDevice2 != null) {
                    this.mOtherProfileIntent.putExtra("device", usbDevice2);
                }
                UsbAccessory usbAccessory2 = this.mAccessory;
                if (usbAccessory2 != null) {
                    this.mOtherProfileIntent.putExtra("accessory", usbAccessory2);
                }
            }
        }
        UsbResolverActivity.super.onCreate(bundle, intent2, getResources().getText(17039811), (Intent[]) null, arrayList2, true);
        CheckBox checkBox = (CheckBox) findViewById(16908752);
        if (checkBox == null) {
            return;
        }
        if (this.mDevice == null) {
            checkBox.setText(R.string.always_use_accessory);
        } else {
            checkBox.setText(R.string.always_use_device);
        }
    }

    /* access modifiers changed from: protected */
    public void onDestroy() {
        UsbDisconnectedReceiver usbDisconnectedReceiver = this.mDisconnectedReceiver;
        if (usbDisconnectedReceiver != null) {
            unregisterReceiver(usbDisconnectedReceiver);
        }
        UsbResolverActivity.super.onDestroy();
    }

    /* JADX WARNING: type inference failed for: r8v0, types: [com.android.internal.app.ResolverActivity, com.android.systemui.usb.UsbResolverActivity, android.app.Activity] */
    /* access modifiers changed from: protected */
    public boolean onTargetSelected(TargetInfo targetInfo, boolean z) {
        ResolveInfo resolveInfo = targetInfo.getResolveInfo();
        ResolveInfo resolveInfo2 = this.mForwardResolveInfo;
        if (resolveInfo == resolveInfo2) {
            startActivityAsUser(this.mOtherProfileIntent, (Bundle) null, UserHandleCompat.of(resolveInfo2.targetUserId));
            return true;
        }
        try {
            IUsbManager asInterface = IUsbManager.Stub.asInterface(ServiceManager.getService("usb"));
            int i = resolveInfo.activityInfo.applicationInfo.uid;
            int myUserId = UserHandle.myUserId();
            if (this.mDevice != null) {
                asInterface.grantDevicePermission(this.mDevice, i);
                if (z) {
                    asInterface.setDevicePackage(this.mDevice, resolveInfo.activityInfo.packageName, myUserId);
                } else {
                    asInterface.setDevicePackage(this.mDevice, (String) null, myUserId);
                }
            } else if (this.mAccessory != null) {
                asInterface.grantAccessoryPermission(this.mAccessory, i);
                if (z) {
                    asInterface.setAccessoryPackage(this.mAccessory, resolveInfo.activityInfo.packageName, myUserId);
                } else {
                    asInterface.setAccessoryPackage(this.mAccessory, (String) null, myUserId);
                }
            }
            try {
                targetInfo.startAsUser(this, (Bundle) null, UserHandleCompat.of(myUserId));
            } catch (ActivityNotFoundException e) {
                Log.e("UsbResolverActivity", "startActivity failed", e);
            }
        } catch (RemoteException e2) {
            Log.e("UsbResolverActivity", "onIntentSelected failed", e2);
        }
        return true;
    }
}
