package com.android.systemui.statusbar.policy;

import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.settings.CurrentUserTracker;
import com.android.systemui.statusbar.policy.DeviceProvisionedController;
import java.util.ArrayList;

public class DeviceProvisionedControllerImpl extends CurrentUserTracker implements DeviceProvisionedController {
    protected static final String TAG = "DeviceProvisionedControllerImpl";
    private final ContentResolver mContentResolver;
    private final Uri mDeviceProvisionedUri;
    protected final ArrayList<DeviceProvisionedController.DeviceProvisionedListener> mListeners = new ArrayList<>();
    protected final ContentObserver mSettingsObserver;
    private final Uri mUserSetupUri;

    public DeviceProvisionedControllerImpl(Context context, Handler handler, BroadcastDispatcher broadcastDispatcher) {
        super(broadcastDispatcher);
        this.mContentResolver = context.getContentResolver();
        this.mDeviceProvisionedUri = Settings.Global.getUriFor("device_provisioned");
        this.mUserSetupUri = Settings.Secure.getUriFor("user_setup_complete");
        this.mSettingsObserver = new ContentObserver(handler) {
            /* class com.android.systemui.statusbar.policy.DeviceProvisionedControllerImpl.AnonymousClass1 */

            public void onChange(boolean z, Uri uri, int i) {
                String str = DeviceProvisionedControllerImpl.TAG;
                Log.d(str, "Setting change: " + uri);
                if (DeviceProvisionedControllerImpl.this.mUserSetupUri.equals(uri)) {
                    DeviceProvisionedControllerImpl.this.notifySetupChanged();
                } else {
                    DeviceProvisionedControllerImpl.this.notifyProvisionedChanged();
                }
            }
        };
    }

    @Override // com.android.systemui.statusbar.policy.DeviceProvisionedController
    public boolean isDeviceProvisioned() {
        return Settings.Global.getInt(this.mContentResolver, "device_provisioned", 0) != 0;
    }

    @Override // com.android.systemui.statusbar.policy.DeviceProvisionedController
    public boolean isUserSetup(int i) {
        return Settings.Secure.getIntForUser(this.mContentResolver, "user_setup_complete", 0, i) != 0;
    }

    @Override // com.android.systemui.statusbar.policy.DeviceProvisionedController
    public int getCurrentUser() {
        return ActivityManager.getCurrentUser();
    }

    public void addCallback(DeviceProvisionedController.DeviceProvisionedListener deviceProvisionedListener) {
        this.mListeners.add(deviceProvisionedListener);
        if (this.mListeners.size() == 1) {
            startListening(getCurrentUser());
        }
        deviceProvisionedListener.onUserSetupChanged();
        deviceProvisionedListener.onDeviceProvisionedChanged();
    }

    public void removeCallback(DeviceProvisionedController.DeviceProvisionedListener deviceProvisionedListener) {
        this.mListeners.remove(deviceProvisionedListener);
        if (this.mListeners.size() == 0) {
            stopListening();
        }
    }

    /* access modifiers changed from: protected */
    public void startListening(int i) {
        this.mContentResolver.registerContentObserver(this.mDeviceProvisionedUri, true, this.mSettingsObserver, 0);
        this.mContentResolver.registerContentObserver(this.mUserSetupUri, true, this.mSettingsObserver, i);
        startTracking();
    }

    /* access modifiers changed from: protected */
    public void stopListening() {
        stopTracking();
        this.mContentResolver.unregisterContentObserver(this.mSettingsObserver);
    }

    @Override // com.android.systemui.settings.CurrentUserTracker
    public void onUserSwitched(int i) {
        this.mContentResolver.unregisterContentObserver(this.mSettingsObserver);
        this.mContentResolver.registerContentObserver(this.mDeviceProvisionedUri, true, this.mSettingsObserver, 0);
        this.mContentResolver.registerContentObserver(this.mUserSetupUri, true, this.mSettingsObserver, i);
        notifyUserChanged();
    }

    private void notifyUserChanged() {
        for (int size = this.mListeners.size() - 1; size >= 0; size--) {
            this.mListeners.get(size).onUserSwitched();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifySetupChanged() {
        for (int size = this.mListeners.size() - 1; size >= 0; size--) {
            this.mListeners.get(size).onUserSetupChanged();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyProvisionedChanged() {
        for (int size = this.mListeners.size() - 1; size >= 0; size--) {
            this.mListeners.get(size).onDeviceProvisionedChanged();
        }
    }
}
