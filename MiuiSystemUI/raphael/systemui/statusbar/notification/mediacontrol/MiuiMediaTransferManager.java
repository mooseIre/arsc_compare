package com.android.systemui.statusbar.notification.mediacontrol;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.MediaRoute2Info;
import android.media.MediaRouter;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import codeinjection.CodeInjection;
import com.android.settingslib.bluetooth.BluetoothCallback;
import com.android.settingslib.bluetooth.CachedBluetoothDevice;
import com.android.settingslib.bluetooth.LocalBluetoothManager;
import com.android.settingslib.media.InfoMediaManager;
import com.android.settingslib.media.LocalMediaManager;
import com.android.settingslib.media.MediaDevice;
import com.android.settingslib.media.PhoneMediaDevice;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0015R$id;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.miui.systemui.util.ReflectUtil;
import java.util.ArrayList;
import java.util.List;

public class MiuiMediaTransferManager implements BluetoothCallback {
    private final ActivityStarter mActivityStarter = ((ActivityStarter) Dependency.get(ActivityStarter.class));
    private ConfigurationController.ConfigurationListener mConfigurationListener = new ConfigurationController.ConfigurationListener() {
        /* class com.android.systemui.statusbar.notification.mediacontrol.MiuiMediaTransferManager.AnonymousClass1 */

        @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
        public void onConfigChanged(Configuration configuration) {
            MiuiMediaTransferManager.this.refreshPhoneRouteName();
        }

        @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
        public void onLocaleListChanged() {
            MiuiMediaTransferManager.this.refreshPhoneRouteName();
        }
    };
    private String mCurRouteName = CodeInjection.MD5;
    private final LocalMediaManager mLocalMediaManager;
    private final MediaRouter.SimpleCallback mMediaCallback = new MediaRouter.SimpleCallback() {
        /* class com.android.systemui.statusbar.notification.mediacontrol.MiuiMediaTransferManager.AnonymousClass4 */

        public void onRouteSelected(MediaRouter mediaRouter, int i, MediaRouter.RouteInfo routeInfo) {
            MiuiMediaTransferManager.this.updateCurrentDevice(routeInfo.getName().toString());
        }
    };
    private final LocalMediaManager.DeviceCallback mMediaDeviceCallback = new LocalMediaManager.DeviceCallback() {
        /* class com.android.systemui.statusbar.notification.mediacontrol.MiuiMediaTransferManager.AnonymousClass3 */

        @Override // com.android.settingslib.media.LocalMediaManager.DeviceCallback
        public void onDeviceListUpdate(List<MediaDevice> list) {
            MiuiMediaTransferManager.this.updatePhoneDevice((MiuiMediaTransferManager) list);
            MediaDevice currentConnectedDevice = MiuiMediaTransferManager.this.mLocalMediaManager.getCurrentConnectedDevice();
            if (currentConnectedDevice == null) {
                return;
            }
            if (currentConnectedDevice instanceof PhoneMediaDevice) {
                MiuiMediaTransferManager miuiMediaTransferManager = MiuiMediaTransferManager.this;
                miuiMediaTransferManager.updateCurrentDevice(miuiMediaTransferManager.mPhoneRouteName);
                return;
            }
            MiuiMediaTransferManager.this.updateCurrentDevice(currentConnectedDevice.getName());
        }
    };
    private final MediaRouter mMediaRouter;
    private final View.OnClickListener mOnClickHandler = new View.OnClickListener() {
        /* class com.android.systemui.statusbar.notification.mediacontrol.MiuiMediaTransferManager.AnonymousClass2 */

        public void onClick(View view) {
            handleMediaTransfer();
        }

        private void handleMediaTransfer() {
            MiuiMediaTransferManager.this.mActivityStarter.startActivity(new Intent().setAction("miui.bluetooth.mible.MiuiAudioRelayActivity"), false, true, 268435456);
        }
    };
    private String mPhoneRouteName = CodeInjection.MD5;
    private List<ImageView> mViews = new ArrayList();
    private boolean sHasTransferComponent;

    @Override // com.android.settingslib.bluetooth.BluetoothCallback
    public void onActiveDeviceChanged(CachedBluetoothDevice cachedBluetoothDevice, int i) {
        updateCurrentDevice(cachedBluetoothDevice == null ? this.mPhoneRouteName : cachedBluetoothDevice.getName());
    }

    public MiuiMediaTransferManager(Context context) {
        LocalBluetoothManager localBluetoothManager = (LocalBluetoothManager) Dependency.get(LocalBluetoothManager.class);
        this.mLocalMediaManager = new LocalMediaManager(context, localBluetoothManager, new InfoMediaManager(context, context.getPackageName(), null, localBluetoothManager), null);
        this.mMediaRouter = (MediaRouter) context.getSystemService("media_router");
        checkForTransferComponent(context);
        if (this.sHasTransferComponent) {
            ((ConfigurationController) Dependency.get(ConfigurationController.class)).addCallback(this.mConfigurationListener);
        }
    }

    private void checkForTransferComponent(Context context) {
        if (context.getPackageManager().resolveActivity(new Intent("miui.bluetooth.mible.MiuiAudioRelayActivity"), 0) != null) {
            this.sHasTransferComponent = true;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void refreshPhoneRouteName() {
        this.mPhoneRouteName = CodeInjection.MD5;
        if (!this.mViews.isEmpty()) {
            this.mLocalMediaManager.startScan();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateCurrentDevice(String str) {
        checkLocalMediaManager();
        this.mCurRouteName = str;
        updateAllChips();
    }

    private boolean checkLocalMediaManager() {
        if (TextUtils.isEmpty(this.mPhoneRouteName)) {
            return false;
        }
        this.mLocalMediaManager.stopScan();
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Removed duplicated region for block: B:6:0x0011  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void updatePhoneDevice(java.util.List<com.android.settingslib.media.MediaDevice> r2) {
        /*
            r1 = this;
            boolean r0 = r1.checkLocalMediaManager()
            if (r0 == 0) goto L_0x0007
            return
        L_0x0007:
            java.util.Iterator r2 = r2.iterator()
        L_0x000b:
            boolean r0 = r2.hasNext()
            if (r0 == 0) goto L_0x001d
            java.lang.Object r0 = r2.next()
            com.android.settingslib.media.MediaDevice r0 = (com.android.settingslib.media.MediaDevice) r0
            boolean r0 = r1.updatePhoneDevice(r0)
            if (r0 == 0) goto L_0x000b
        L_0x001d:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.notification.mediacontrol.MiuiMediaTransferManager.updatePhoneDevice(java.util.List):void");
    }

    private boolean updatePhoneDevice(MediaDevice mediaDevice) {
        if (!(mediaDevice instanceof PhoneMediaDevice)) {
            return false;
        }
        try {
            MediaRoute2Info mediaRoute2Info = (MediaRoute2Info) ReflectUtil.getObjectFieldAndSuper(mediaDevice, "mRouteInfo", MediaRoute2Info.class);
            if (mediaRoute2Info == null) {
                return false;
            }
            this.mPhoneRouteName = mediaRoute2Info.getName().toString();
            this.mLocalMediaManager.stopScan();
            return true;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            Log.e("MiuiMediaTransferManager", "Can't find phone name!");
            return false;
        }
    }

    public void setRemoved(View view) {
        if (this.mLocalMediaManager != null && view != null) {
            ImageView imageView = (ImageView) view.findViewById(C0015R$id.media_seamless_image);
            if (!this.mViews.remove(imageView)) {
                Log.e("MiuiMediaTransferManager", "Tried to remove unknown view " + imageView);
            } else if (this.mViews.size() == 0) {
                ((LocalBluetoothManager) Dependency.get(LocalBluetoothManager.class)).getEventManager().unregisterCallback(this);
                this.mLocalMediaManager.unregisterCallback(this.mMediaDeviceCallback);
                this.mMediaRouter.removeCallback(this.mMediaCallback);
            }
        }
    }

    public void applyMediaTransferView(ViewGroup viewGroup) {
        if (this.mLocalMediaManager != null && viewGroup != null && this.sHasTransferComponent) {
            ImageView imageView = (ImageView) viewGroup.findViewById(C0015R$id.media_seamless_image);
            if (imageView == null) {
                Log.e("MiuiMediaTransferManager", "There is no {ImageView @media_seamless_image} in root");
                return;
            }
            imageView.setVisibility(0);
            imageView.setOnClickListener(this.mOnClickHandler);
            if (!this.mViews.contains(imageView)) {
                this.mViews.add(imageView);
                if (this.mViews.size() == 1) {
                    ((LocalBluetoothManager) Dependency.get(LocalBluetoothManager.class)).getEventManager().registerCallback(this);
                    this.mLocalMediaManager.registerCallback(this.mMediaDeviceCallback);
                    this.mMediaRouter.addCallback(8388615, this.mMediaCallback, 2);
                }
            }
            if (TextUtils.isEmpty(this.mPhoneRouteName)) {
                this.mLocalMediaManager.startScan();
            } else {
                updateChip(imageView);
            }
        }
    }

    private void updateAllChips() {
        for (ImageView imageView : this.mViews) {
            updateChip(imageView);
        }
    }

    private void updateChip(ImageView imageView) {
        if (!TextUtils.equals(this.mPhoneRouteName, this.mCurRouteName)) {
            imageView.setImageResource(C0013R$drawable.ic_media_seamless_others);
            imageView.setContentDescription(this.mCurRouteName);
            return;
        }
        imageView.setImageResource(C0013R$drawable.ic_media_seamless);
        imageView.setContentDescription(this.mPhoneRouteName);
    }
}
