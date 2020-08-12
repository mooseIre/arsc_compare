package com.android.systemui.statusbar.policy;

import android.app.ActivityManagerCompat;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.UserInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.UserManagerCompat;
import android.util.Log;
import com.android.internal.util.UserIconsCompat;
import com.android.settingslib.drawable.UserIconDrawable;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.policy.UserInfoController;
import java.util.ArrayList;
import java.util.Iterator;

public class UserInfoControllerImpl implements UserInfoController {
    private final ArrayList<UserInfoController.OnUserInfoChangedListener> mCallbacks = new ArrayList<>();
    /* access modifiers changed from: private */
    public final Context mContext;
    private final BroadcastReceiver mProfileReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.provider.Contacts.PROFILE_CHANGED".equals(action) || "android.intent.action.USER_INFO_CHANGED".equals(action)) {
                try {
                    if (intent.getIntExtra("android.intent.extra.user_handle", getSendingUserId()) == ActivityManagerCompat.getService().getCurrentUser().id) {
                        UserInfoControllerImpl.this.reloadUserInfo();
                    }
                } catch (RemoteException e) {
                    Log.e("UserInfoController", "Couldn't get current user id for profile change", e);
                }
            }
        }
    };
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.USER_SWITCHED".equals(intent.getAction())) {
                UserInfoControllerImpl.this.reloadUserInfo();
            }
        }
    };
    /* access modifiers changed from: private */
    public String mUserAccount;
    /* access modifiers changed from: private */
    public Drawable mUserDrawable;
    /* access modifiers changed from: private */
    public AsyncTask<Void, Void, UserInfoQueryResult> mUserInfoTask;
    /* access modifiers changed from: private */
    public String mUserName;

    public UserInfoControllerImpl(Context context) {
        this.mContext = context;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.USER_SWITCHED");
        this.mContext.registerReceiver(this.mReceiver, intentFilter);
        IntentFilter intentFilter2 = new IntentFilter();
        intentFilter2.addAction("android.provider.Contacts.PROFILE_CHANGED");
        intentFilter2.addAction("android.intent.action.USER_INFO_CHANGED");
        this.mContext.registerReceiverAsUser(this.mProfileReceiver, UserHandle.ALL, intentFilter2, (String) null, (Handler) null);
    }

    public void addCallback(UserInfoController.OnUserInfoChangedListener onUserInfoChangedListener) {
        this.mCallbacks.add(onUserInfoChangedListener);
        onUserInfoChangedListener.onUserInfoChanged(this.mUserName, this.mUserDrawable, this.mUserAccount);
    }

    public void removeCallback(UserInfoController.OnUserInfoChangedListener onUserInfoChangedListener) {
        this.mCallbacks.remove(onUserInfoChangedListener);
    }

    public void reloadUserInfo() {
        AsyncTask<Void, Void, UserInfoQueryResult> asyncTask = this.mUserInfoTask;
        if (asyncTask != null) {
            asyncTask.cancel(false);
            this.mUserInfoTask = null;
        }
        try {
            queryForUserInformation();
        } catch (Exception e) {
            Log.e("UserInfoController", "Couldn't query user info", e);
        }
    }

    private void queryForUserInformation() {
        try {
            UserInfo currentUser = ActivityManagerCompat.getService().getCurrentUser();
            this.mContext.createPackageContextAsUser("android", 0, new UserHandle(currentUser.id));
            final int i = currentUser.id;
            final boolean isGuest = currentUser.isGuest();
            final String str = currentUser.name;
            Resources resources = this.mContext.getResources();
            final int max = Math.max(resources.getDimensionPixelSize(R.dimen.multi_user_avatar_expanded_size), resources.getDimensionPixelSize(R.dimen.multi_user_avatar_keyguard_size));
            this.mUserInfoTask = new AsyncTask<Void, Void, UserInfoQueryResult>() {
                /* access modifiers changed from: protected */
                public UserInfoQueryResult doInBackground(Void... voidArr) {
                    UserIconDrawable userIconDrawable;
                    UserManager userManager = UserManager.get(UserInfoControllerImpl.this.mContext);
                    String str = str;
                    Bitmap userIcon = userManager.getUserIcon(i);
                    if (userIcon != null) {
                        UserIconDrawable userIconDrawable2 = new UserIconDrawable(max);
                        userIconDrawable2.setIcon(userIcon);
                        userIconDrawable2.setBadgeIfManagedUser(UserInfoControllerImpl.this.mContext, i);
                        userIconDrawable2.bake();
                        userIconDrawable = userIconDrawable2;
                    } else {
                        userIconDrawable = UserIconsCompat.getDefaultUserIcon(UserInfoControllerImpl.this.mContext.getResources(), isGuest ? -10000 : i, true);
                    }
                    int size = userManager.getUsers().size();
                    return new UserInfoQueryResult(str, userIconDrawable, UserManagerCompat.getUserAccount(userManager, i));
                }

                /* access modifiers changed from: protected */
                public void onPostExecute(UserInfoQueryResult userInfoQueryResult) {
                    String unused = UserInfoControllerImpl.this.mUserName = userInfoQueryResult.getName();
                    Drawable unused2 = UserInfoControllerImpl.this.mUserDrawable = userInfoQueryResult.getAvatar();
                    String unused3 = UserInfoControllerImpl.this.mUserAccount = userInfoQueryResult.getUserAccount();
                    AsyncTask unused4 = UserInfoControllerImpl.this.mUserInfoTask = null;
                    UserInfoControllerImpl.this.notifyChanged();
                }
            };
            this.mUserInfoTask.execute(new Void[0]);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("UserInfoController", "Couldn't create user context", e);
            throw new RuntimeException(e);
        } catch (RemoteException e2) {
            Log.e("UserInfoController", "Couldn't get user info", e2);
            throw new RuntimeException(e2);
        }
    }

    /* access modifiers changed from: private */
    public void notifyChanged() {
        Iterator<UserInfoController.OnUserInfoChangedListener> it = this.mCallbacks.iterator();
        while (it.hasNext()) {
            it.next().onUserInfoChanged(this.mUserName, this.mUserDrawable, this.mUserAccount);
        }
    }

    public void onDensityOrFontScaleChanged() {
        reloadUserInfo();
    }

    private static class UserInfoQueryResult {
        private Drawable mAvatar;
        private String mName;
        private String mUserAccount;

        public UserInfoQueryResult(String str, Drawable drawable, String str2) {
            this.mName = str;
            this.mAvatar = drawable;
            this.mUserAccount = str2;
        }

        public String getName() {
            return this.mName;
        }

        public Drawable getAvatar() {
            return this.mAvatar;
        }

        public String getUserAccount() {
            return this.mUserAccount;
        }
    }
}
