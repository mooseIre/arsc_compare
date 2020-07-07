package com.android.systemui.media;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.media.projection.IMediaProjection;
import android.media.projection.IMediaProjectionManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.text.BidiFormatter;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Window;
import com.android.systemui.plugins.R;
import miui.app.AlertDialog;

public class MediaProjectionPermissionActivity extends Activity implements DialogInterface.OnClickListener, DialogInterface.OnCancelListener {
    private AlertDialog mDialog;
    private String mPackageName;
    private IMediaProjectionManager mService;
    private int mUid;

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.mPackageName = getCallingPackage();
        this.mService = IMediaProjectionManager.Stub.asInterface(ServiceManager.getService("media_projection"));
        if (this.mPackageName == null) {
            finish();
            return;
        }
        PackageManager packageManager = getPackageManager();
        try {
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(this.mPackageName, 0);
            int i = applicationInfo.uid;
            this.mUid = i;
            try {
                if (this.mService.hasProjectionPermission(i, this.mPackageName)) {
                    setResult(-1, getMediaProjectionIntent(this.mUid, this.mPackageName, false));
                    finish();
                    return;
                }
                TextPaint textPaint = new TextPaint();
                textPaint.setTextSize(42.0f);
                String charSequence = applicationInfo.loadLabel(packageManager).toString();
                int length = charSequence.length();
                int i2 = 0;
                while (true) {
                    if (i2 >= length) {
                        break;
                    }
                    int codePointAt = charSequence.codePointAt(i2);
                    int type = Character.getType(codePointAt);
                    if (type == 13 || type == 15 || type == 14) {
                        charSequence = charSequence.substring(0, i2) + "…";
                    } else {
                        i2 += Character.charCount(codePointAt);
                    }
                }
                charSequence = charSequence.substring(0, i2) + "…";
                if (charSequence.isEmpty()) {
                    charSequence = this.mPackageName;
                }
                String unicodeWrap = BidiFormatter.getInstance().unicodeWrap(TextUtils.ellipsize(charSequence, textPaint, 500.0f, TextUtils.TruncateAt.END).toString());
                String string = getString(R.string.media_projection_dialog_text, new Object[]{unicodeWrap});
                SpannableString spannableString = new SpannableString(string);
                int indexOf = string.indexOf(unicodeWrap);
                if (indexOf >= 0) {
                    spannableString.setSpan(new StyleSpan(1), indexOf, unicodeWrap.length() + indexOf, 0);
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Theme_Dialog_Alert);
                builder.setIcon(applicationInfo.loadIcon(packageManager));
                builder.setMessage((CharSequence) spannableString);
                builder.setCheckBox(false, getString(R.string.media_projection_remember_text));
                builder.setPositiveButton((int) R.string.media_projection_action_text, (DialogInterface.OnClickListener) this);
                builder.setNegativeButton(17039360, (DialogInterface.OnClickListener) this);
                builder.setOnCancelListener(this);
                AlertDialog create = builder.create();
                this.mDialog = create;
                create.create();
                this.mDialog.getButton(-1).setFilterTouchesWhenObscured(true);
                Window window = this.mDialog.getWindow();
                window.setType(2003);
                window.addPrivateFlags(524288);
                this.mDialog.show();
            } catch (RemoteException e) {
                Log.e("MediaProjectionPermissionActivity", "Error checking projection permissions", e);
                finish();
            }
        } catch (PackageManager.NameNotFoundException e2) {
            Log.e("MediaProjectionPermissionActivity", "unable to look up package name", e2);
            finish();
        }
    }

    /* access modifiers changed from: protected */
    public void onDestroy() {
        super.onDestroy();
        AlertDialog alertDialog = this.mDialog;
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0025, code lost:
        if (r3 == null) goto L_0x003a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0035, code lost:
        if (r3 != null) goto L_0x0037;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0037, code lost:
        r3.dismiss();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x003a, code lost:
        finish();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x003d, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onClick(android.content.DialogInterface r3, int r4) {
        /*
            r2 = this;
            r3 = -1
            if (r4 != r3) goto L_0x0033
            int r4 = r2.mUid     // Catch:{ RemoteException -> 0x0017 }
            java.lang.String r0 = r2.mPackageName     // Catch:{ RemoteException -> 0x0017 }
            miui.app.AlertDialog r1 = r2.mDialog     // Catch:{ RemoteException -> 0x0017 }
            boolean r1 = r1.isChecked()     // Catch:{ RemoteException -> 0x0017 }
            android.content.Intent r4 = r2.getMediaProjectionIntent(r4, r0, r1)     // Catch:{ RemoteException -> 0x0017 }
            r2.setResult(r3, r4)     // Catch:{ RemoteException -> 0x0017 }
            goto L_0x0033
        L_0x0015:
            r3 = move-exception
            goto L_0x0028
        L_0x0017:
            r3 = move-exception
            java.lang.String r4 = "MediaProjectionPermissionActivity"
            java.lang.String r0 = "Error granting projection permission"
            android.util.Log.e(r4, r0, r3)     // Catch:{ all -> 0x0015 }
            r3 = 0
            r2.setResult(r3)     // Catch:{ all -> 0x0015 }
            miui.app.AlertDialog r3 = r2.mDialog
            if (r3 == 0) goto L_0x003a
            goto L_0x0037
        L_0x0028:
            miui.app.AlertDialog r4 = r2.mDialog
            if (r4 == 0) goto L_0x002f
            r4.dismiss()
        L_0x002f:
            r2.finish()
            throw r3
        L_0x0033:
            miui.app.AlertDialog r3 = r2.mDialog
            if (r3 == 0) goto L_0x003a
        L_0x0037:
            r3.dismiss()
        L_0x003a:
            r2.finish()
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.media.MediaProjectionPermissionActivity.onClick(android.content.DialogInterface, int):void");
    }

    private Intent getMediaProjectionIntent(int i, String str, boolean z) throws RemoteException {
        IMediaProjection createProjection = this.mService.createProjection(i, str, 0, z);
        Intent intent = new Intent();
        intent.putExtra("android.media.projection.extra.EXTRA_MEDIA_PROJECTION", createProjection.asBinder());
        return intent;
    }

    public void onCancel(DialogInterface dialogInterface) {
        finish();
    }
}
