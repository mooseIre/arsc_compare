package com.android.systemui.statusbar.notification.mediacontrol;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.ActivityStarter;

public class MediaTransferForMediaControlManager {
    private static final boolean MIUI_MEDIA_SEAMLESS_ENABLED = (Build.VERSION.SDK_INT > 28);
    private static boolean sComponentChecked;
    private static boolean sHasTransferComponent;
    /* access modifiers changed from: private */
    public final ActivityStarter mActivityStarter;
    private final Context mContext;
    private final View.OnClickListener mOnClickHandler = new View.OnClickListener() {
        public void onClick(View view) {
            handleMediaTransfer();
        }

        private void handleMediaTransfer() {
            MediaTransferForMediaControlManager.this.mActivityStarter.startActivity(new Intent().setAction("miui.bluetooth.mible.MiuiAudioRelayActivity"), false, true, 268435456);
        }
    };

    public MediaTransferForMediaControlManager(Context context) {
        this.mContext = context;
        this.mActivityStarter = (ActivityStarter) Dependency.get(ActivityStarter.class);
    }

    private void checkForTransferComponent() {
        sComponentChecked = true;
        if (this.mContext.getPackageManager().resolveActivity(new Intent("miui.bluetooth.mible.MiuiAudioRelayActivity"), 0) != null) {
            sHasTransferComponent = true;
        }
    }

    public void applyMediaTransferView(ViewGroup viewGroup) {
        if (MIUI_MEDIA_SEAMLESS_ENABLED) {
            if (!sComponentChecked) {
                checkForTransferComponent();
            }
            if (!sComponentChecked || sHasTransferComponent) {
                viewGroup.setVisibility(0);
                viewGroup.setOnClickListener(this.mOnClickHandler);
            }
        }
    }
}
