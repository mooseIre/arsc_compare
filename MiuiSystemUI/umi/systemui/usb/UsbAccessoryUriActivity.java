package com.android.systemui.usb;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.usb.UsbAccessory;
import android.net.Uri;
import android.os.Bundle;
import android.os.UserHandle;
import android.util.Log;
import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController;
import com.android.systemui.C0021R$string;

public class UsbAccessoryUriActivity extends AlertActivity implements DialogInterface.OnClickListener {
    private UsbAccessory mAccessory;
    private Uri mUri;

    public void onCreate(Bundle bundle) {
        Uri uri;
        UsbAccessoryUriActivity.super.onCreate(bundle);
        Intent intent = getIntent();
        this.mAccessory = (UsbAccessory) intent.getParcelableExtra("accessory");
        String stringExtra = intent.getStringExtra("uri");
        if (stringExtra == null) {
            uri = null;
        } else {
            uri = Uri.parse(stringExtra);
        }
        this.mUri = uri;
        if (uri == null) {
            Log.e("UsbAccessoryUriActivity", "could not parse Uri " + stringExtra);
            finish();
            return;
        }
        String scheme = uri.getScheme();
        if ("http".equals(scheme) || "https".equals(scheme)) {
            AlertController.AlertParams alertParams = ((AlertActivity) this).mAlertParams;
            String description = this.mAccessory.getDescription();
            alertParams.mTitle = description;
            if (description == null || description.length() == 0) {
                alertParams.mTitle = getString(C0021R$string.title_usb_accessory);
            }
            alertParams.mMessage = getString(C0021R$string.usb_accessory_uri_prompt, new Object[]{this.mUri});
            alertParams.mPositiveButtonText = getString(C0021R$string.label_view);
            alertParams.mNegativeButtonText = getString(17039360);
            alertParams.mPositiveButtonListener = this;
            alertParams.mNegativeButtonListener = this;
            setupAlert();
            return;
        }
        Log.e("UsbAccessoryUriActivity", "Uri not http or https: " + this.mUri);
        finish();
    }

    public void onClick(DialogInterface dialogInterface, int i) {
        if (i == -1) {
            Intent intent = new Intent("android.intent.action.VIEW", this.mUri);
            intent.addCategory("android.intent.category.BROWSABLE");
            intent.addFlags(268435456);
            try {
                startActivityAsUser(intent, UserHandle.CURRENT);
            } catch (ActivityNotFoundException unused) {
                Log.e("UsbAccessoryUriActivity", "startActivity failed for " + this.mUri);
            }
        }
        finish();
    }
}
