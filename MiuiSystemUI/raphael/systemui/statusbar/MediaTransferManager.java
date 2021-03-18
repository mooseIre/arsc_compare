package com.android.systemui.statusbar;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.util.FeatureFlagUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.settingslib.bluetooth.LocalBluetoothManager;
import com.android.settingslib.media.InfoMediaManager;
import com.android.settingslib.media.LocalMediaManager;
import com.android.settingslib.media.MediaDevice;
import com.android.settingslib.widget.AdaptiveIcon;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import java.util.ArrayList;
import java.util.List;

public class MediaTransferManager {
    private final ActivityStarter mActivityStarter;
    private final Context mContext;
    private MediaDevice mDevice;
    private LocalMediaManager mLocalMediaManager;
    private final LocalMediaManager.DeviceCallback mMediaDeviceCallback = new LocalMediaManager.DeviceCallback() {
        /* class com.android.systemui.statusbar.MediaTransferManager.AnonymousClass2 */

        @Override // com.android.settingslib.media.LocalMediaManager.DeviceCallback
        public void onDeviceListUpdate(List<MediaDevice> list) {
            MediaDevice currentConnectedDevice = MediaTransferManager.this.mLocalMediaManager.getCurrentConnectedDevice();
            if (MediaTransferManager.this.mDevice == null || !MediaTransferManager.this.mDevice.equals(currentConnectedDevice)) {
                MediaTransferManager.this.mDevice = currentConnectedDevice;
                MediaTransferManager.this.updateAllChips();
            }
        }

        @Override // com.android.settingslib.media.LocalMediaManager.DeviceCallback
        public void onSelectedDeviceStateChanged(MediaDevice mediaDevice, int i) {
            if (MediaTransferManager.this.mDevice == null || !MediaTransferManager.this.mDevice.equals(mediaDevice)) {
                MediaTransferManager.this.mDevice = mediaDevice;
                MediaTransferManager.this.updateAllChips();
            }
        }
    };
    private final View.OnClickListener mOnClickHandler = new View.OnClickListener() {
        /* class com.android.systemui.statusbar.MediaTransferManager.AnonymousClass1 */

        public void onClick(View view) {
            if (handleMediaTransfer(view)) {
            }
        }

        private boolean handleMediaTransfer(View view) {
            if (view.findViewById(16909167) == null) {
                return false;
            }
            MediaTransferManager.this.mActivityStarter.startActivity(new Intent().setAction("com.android.settings.panel.action.MEDIA_OUTPUT").putExtra("com.android.settings.panel.extra.PACKAGE_NAME", MediaTransferManager.this.getRowForParent(view.getParent()).getEntry().getSbn().getPackageName()), false, true, 268468224);
            return true;
        }
    };
    private List<View> mViews = new ArrayList();

    public MediaTransferManager(Context context) {
        this.mContext = context;
        this.mActivityStarter = (ActivityStarter) Dependency.get(ActivityStarter.class);
        LocalBluetoothManager localBluetoothManager = (LocalBluetoothManager) Dependency.get(LocalBluetoothManager.class);
        this.mLocalMediaManager = new LocalMediaManager(this.mContext, localBluetoothManager, new InfoMediaManager(this.mContext, null, null, localBluetoothManager), null);
    }

    public void setRemoved(View view) {
        if (FeatureFlagUtils.isEnabled(this.mContext, "settings_seamless_transfer") && this.mLocalMediaManager != null && view != null) {
            View findViewById = view.findViewById(16909167);
            if (!this.mViews.remove(findViewById)) {
                Log.e("MediaTransferManager", "Tried to remove unknown view " + findViewById);
            } else if (this.mViews.size() == 0) {
                this.mLocalMediaManager.unregisterCallback(this.mMediaDeviceCallback);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private ExpandableNotificationRow getRowForParent(ViewParent viewParent) {
        while (viewParent != null) {
            if (viewParent instanceof ExpandableNotificationRow) {
                return (ExpandableNotificationRow) viewParent;
            }
            viewParent = viewParent.getParent();
        }
        return null;
    }

    public void applyMediaTransferView(ViewGroup viewGroup, NotificationEntry notificationEntry) {
        View findViewById;
        if (FeatureFlagUtils.isEnabled(this.mContext, "settings_seamless_transfer") && this.mLocalMediaManager != null && viewGroup != null && (findViewById = viewGroup.findViewById(16909167)) != null) {
            findViewById.setVisibility(0);
            findViewById.setOnClickListener(this.mOnClickHandler);
            if (!this.mViews.contains(findViewById)) {
                this.mViews.add(findViewById);
                if (this.mViews.size() == 1) {
                    this.mLocalMediaManager.registerCallback(this.mMediaDeviceCallback);
                }
            }
            this.mLocalMediaManager.startScan();
            this.mDevice = this.mLocalMediaManager.getCurrentConnectedDevice();
            updateChip(findViewById);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateAllChips() {
        for (View view : this.mViews) {
            updateChip(view);
        }
    }

    private void updateChip(View view) {
        ExpandableNotificationRow rowForParent = getRowForParent(view.getParent());
        int originalIconColor = rowForParent.getNotificationHeader().getOriginalIconColor();
        ColorStateList valueOf = ColorStateList.valueOf(originalIconColor);
        int currentBackgroundTint = rowForParent.getCurrentBackgroundTint();
        GradientDrawable gradientDrawable = (GradientDrawable) ((RippleDrawable) ((LinearLayout) view).getBackground()).getDrawable(0);
        gradientDrawable.setStroke(2, originalIconColor);
        gradientDrawable.setColor(currentBackgroundTint);
        ImageView imageView = (ImageView) view.findViewById(16909168);
        TextView textView = (TextView) view.findViewById(16909169);
        textView.setTextColor(valueOf);
        MediaDevice mediaDevice = this.mDevice;
        if (mediaDevice != null) {
            Drawable icon = mediaDevice.getIcon();
            imageView.setVisibility(0);
            imageView.setImageTintList(valueOf);
            if (icon instanceof AdaptiveIcon) {
                AdaptiveIcon adaptiveIcon = (AdaptiveIcon) icon;
                adaptiveIcon.setBackgroundColor(currentBackgroundTint);
                imageView.setImageDrawable(adaptiveIcon);
            } else {
                imageView.setImageDrawable(icon);
            }
            textView.setText(this.mDevice.getName());
            return;
        }
        imageView.setVisibility(8);
        textView.setText(17040178);
    }
}
