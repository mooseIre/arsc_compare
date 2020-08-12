package com.android.systemui.statusbar;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.statusbar.NotificationData;
import com.xiaomi.stat.d;
import miui.view.MiuiHapticFeedbackConstants;

public class MediaTransferManager {
    private static final boolean MIUI_MEDIA_SEAMLESS_ENABLED = (Build.VERSION.SDK_INT > 28);
    private static boolean sComponentChecked;
    private static boolean sHasTransferComponent;
    /* access modifiers changed from: private */
    public final ActivityStarter mActivityStarter;
    private final Context mContext;
    private final View.OnClickListener mOnClickHandler = new View.OnClickListener() {
        public void onClick(View view) {
            if (handleMediaTransfer(view)) {
            }
        }

        private boolean handleMediaTransfer(View view) {
            int identifier = view.getContext().getResources().getIdentifier("media_seamless", d.h, "android");
            if (identifier == 0 || view.findViewById(identifier) == null) {
                return false;
            }
            MediaTransferManager.this.mActivityStarter.startActivity(new Intent().setAction("miui.bluetooth.mible.MiuiAudioRelayActivity"), false, true, MiuiHapticFeedbackConstants.FLAG_MIUI_HAPTIC_TAP_NORMAL);
            return true;
        }
    };

    public MediaTransferManager(Context context) {
        this.mContext = context;
        this.mActivityStarter = (ActivityStarter) Dependency.get(ActivityStarter.class);
    }

    private void checkForTransferComponent() {
        sComponentChecked = true;
        if (this.mContext.getPackageManager().resolveActivity(new Intent("miui.bluetooth.mible.MiuiAudioRelayActivity"), 0) != null) {
            sHasTransferComponent = true;
        }
    }

    public void applyMediaTransferView(ViewGroup viewGroup, NotificationData.Entry entry) {
        int identifier;
        View findViewById;
        if (MIUI_MEDIA_SEAMLESS_ENABLED) {
            if (!sComponentChecked) {
                checkForTransferComponent();
            }
            if ((!sComponentChecked || sHasTransferComponent) && (identifier = viewGroup.getContext().getResources().getIdentifier("media_seamless", d.h, "android")) != 0 && (findViewById = viewGroup.findViewById(identifier)) != null) {
                if (!(findViewById instanceof ImageView) || ((ImageView) findViewById).getDrawable() != null) {
                    findViewById.setVisibility(0);
                    findViewById.setOnClickListener(this.mOnClickHandler);
                }
            }
        }
    }
}
