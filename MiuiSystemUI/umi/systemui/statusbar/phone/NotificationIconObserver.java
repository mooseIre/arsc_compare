package com.android.systemui.statusbar.phone;

import android.app.MiuiStatusBarManager;
import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.Dependency;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.settings.CurrentUserTracker;
import com.miui.systemui.SettingsManager;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class NotificationIconObserver {
    public final ArrayList<WeakReference<Callback>> mCallbacks = new ArrayList<>();
    private Context mContext;
    private int mCurrentUserId;
    public Handler mMainHandler;
    private volatile boolean mShowNotificationIcons;

    public interface Callback {
        void onNotificationIconChanged(boolean z);
    }

    public NotificationIconObserver(Context context, Handler handler) {
        this.mContext = context;
        this.mMainHandler = handler;
        new CurrentUserTracker((BroadcastDispatcher) Dependency.get(BroadcastDispatcher.class)) {
            /* class com.android.systemui.statusbar.phone.NotificationIconObserver.AnonymousClass1 */

            @Override // com.android.systemui.settings.CurrentUserTracker
            public void onUserSwitched(int i) {
                NotificationIconObserver.this.updateUserIdAndSetting();
                NotificationIconObserver.this.fireNotificationIconsChanged();
            }
        }.startTracking();
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("status_bar_show_notification_icon"), false, new ContentObserver(this.mMainHandler) {
            /* class com.android.systemui.statusbar.phone.NotificationIconObserver.AnonymousClass2 */

            public void onChange(boolean z) {
                NotificationIconObserver.this.mShowNotificationIcons = !((SettingsManager) Dependency.get(SettingsManager.class)).getMiuiOptimizationEnabled() || MiuiStatusBarManager.isShowNotificationIconForUser(NotificationIconObserver.this.mContext, NotificationIconObserver.this.mCurrentUserId);
                Log.d("NotificationIconObserver", "onChange: show = " + NotificationIconObserver.this.mShowNotificationIcons);
                NotificationIconObserver.this.fireNotificationIconsChanged();
            }
        }, -1);
        updateUserIdAndSetting();
    }

    /* access modifiers changed from: protected */
    public void fireNotificationIconsChanged() {
        boolean z = this.mShowNotificationIcons;
        for (int size = this.mCallbacks.size() - 1; size >= 0; size--) {
            Callback callback = this.mCallbacks.get(size).get();
            if (callback != null) {
                callback.onNotificationIconChanged(z);
            }
        }
    }

    public void addCallback(Callback callback) {
        if (callback != null) {
            for (int i = 0; i < this.mCallbacks.size(); i++) {
                if (this.mCallbacks.get(i).get() == callback) {
                    return;
                }
            }
            this.mCallbacks.add(new WeakReference<>(callback));
            callback.onNotificationIconChanged(this.mShowNotificationIcons);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateUserIdAndSetting() {
        boolean z;
        this.mCurrentUserId = KeyguardUpdateMonitor.getCurrentUser();
        if (!((SettingsManager) Dependency.get(SettingsManager.class)).getMiuiOptimizationEnabled()) {
            z = true;
        } else {
            z = MiuiStatusBarManager.isShowNotificationIconForUser(this.mContext, this.mCurrentUserId);
        }
        this.mShowNotificationIcons = z;
        Log.d("NotificationIconObserver", "updateUserIdAndSetting: mCurrentUserId = " + this.mCurrentUserId + " mShowNotificationIcons = " + this.mShowNotificationIcons);
    }
}
