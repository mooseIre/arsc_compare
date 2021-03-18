package com.android.systemui.statusbar.policy;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;
import com.android.systemui.statusbar.policy.UserInfoController;
import java.util.ArrayList;

public class UserInfoControllerImpl implements UserInfoController {
    private final ArrayList<UserInfoController.OnUserInfoChangedListener> mCallbacks = new ArrayList<>();
    private final Context mContext;
    private final BroadcastReceiver mProfileReceiver = new BroadcastReceiver() {
        /* class com.android.systemui.statusbar.policy.UserInfoControllerImpl.AnonymousClass2 */

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.provider.Contacts.PROFILE_CHANGED".equals(action) || "android.intent.action.USER_INFO_CHANGED".equals(action)) {
                try {
                    if (intent.getIntExtra("android.intent.extra.user_handle", getSendingUserId()) == ActivityManager.getService().getCurrentUser().id) {
                        UserInfoControllerImpl.this.reloadUserInfo();
                    }
                } catch (RemoteException e) {
                    Log.e("UserInfoController", "Couldn't get current user id for profile change", e);
                }
            }
        }
    };
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        /* class com.android.systemui.statusbar.policy.UserInfoControllerImpl.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.USER_SWITCHED".equals(intent.getAction())) {
                UserInfoControllerImpl.this.reloadUserInfo();
            }
        }
    };
    private String mUserAccount;
    private Drawable mUserDrawable;
    private AsyncTask<Void, Void, Object> mUserInfoTask;
    private String mUserName;

    public UserInfoControllerImpl(Context context) {
        this.mContext = context;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.USER_SWITCHED");
        this.mContext.registerReceiver(this.mReceiver, intentFilter);
        IntentFilter intentFilter2 = new IntentFilter();
        intentFilter2.addAction("android.provider.Contacts.PROFILE_CHANGED");
        intentFilter2.addAction("android.intent.action.USER_INFO_CHANGED");
        this.mContext.registerReceiverAsUser(this.mProfileReceiver, UserHandle.ALL, intentFilter2, null, null);
    }

    public void addCallback(UserInfoController.OnUserInfoChangedListener onUserInfoChangedListener) {
        this.mCallbacks.add(onUserInfoChangedListener);
        onUserInfoChangedListener.onUserInfoChanged(this.mUserName, this.mUserDrawable, this.mUserAccount);
    }

    public void removeCallback(UserInfoController.OnUserInfoChangedListener onUserInfoChangedListener) {
        this.mCallbacks.remove(onUserInfoChangedListener);
    }

    @Override // com.android.systemui.statusbar.policy.UserInfoController
    public void reloadUserInfo() {
        AsyncTask<Void, Void, Object> asyncTask = this.mUserInfoTask;
        if (asyncTask != null) {
            asyncTask.cancel(false);
            this.mUserInfoTask = null;
        }
    }

    public void onDensityOrFontScaleChanged() {
        reloadUserInfo();
    }
}
