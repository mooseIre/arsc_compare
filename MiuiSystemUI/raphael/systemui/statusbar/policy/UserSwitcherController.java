package com.android.systemui.statusbar.policy;

import android.app.ActivityManager;
import android.app.ActivityManagerCompat;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationCompat;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.UserInfo;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserHandleCompat;
import android.os.UserManager;
import android.os.UserManagerCompat;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.UserIcons;
import com.android.internal.util.UserIconsCompat;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtilsHelper;
import com.android.systemui.Dependency;
import com.android.systemui.GuestResumeSessionReceiver;
import com.android.systemui.SystemUI;
import com.android.systemui.SystemUISecondaryUserService;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.R;
import com.android.systemui.plugins.qs.DetailAdapter;
import com.android.systemui.qs.tiles.UserDetailView;
import com.android.systemui.statusbar.phone.SystemUIDialog;
import com.android.systemui.statusbar.policy.KeyguardMonitor;
import com.android.systemui.util.NotificationChannels;
import com.android.systemui.util.Utils;
import com.xiaomi.stat.c.b;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class UserSwitcherController {
    private final ActivityStarter mActivityStarter;
    private final ArrayList<WeakReference<BaseUserAdapter>> mAdapters = new ArrayList<>();
    private Dialog mAddUserDialog;
    /* access modifiers changed from: private */
    public boolean mAddUsersWhenLocked;
    private final KeyguardMonitor.Callback mCallback = new KeyguardMonitor.Callback() {
        public void onKeyguardShowingChanged() {
            if (!UserSwitcherController.this.mKeyguardMonitor.isShowing()) {
                UserSwitcherController.this.mHandler.post(new Runnable() {
                    public void run() {
                        UserSwitcherController.this.notifyAdapters();
                    }
                });
            } else {
                UserSwitcherController.this.notifyAdapters();
            }
        }
    };
    protected final Context mContext;
    /* access modifiers changed from: private */
    public Dialog mExitGuestDialog;
    private SparseBooleanArray mForcePictureLoadForUserId = new SparseBooleanArray(2);
    private final GuestResumeSessionReceiver mGuestResumeSessionReceiver = new GuestResumeSessionReceiver();
    protected final Handler mHandler;
    /* access modifiers changed from: private */
    public final KeyguardMonitor mKeyguardMonitor;
    /* access modifiers changed from: private */
    public int mLastNonGuestUser = 0;
    /* access modifiers changed from: private */
    public boolean mPauseRefreshUsers;
    private final PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        private int mCallState;

        public void onCallStateChanged(int i, String str) {
            if (this.mCallState != i) {
                this.mCallState = i;
                int currentUser = KeyguardUpdateMonitor.getCurrentUser();
                UserInfo userInfo = UserSwitcherController.this.mUserManager.getUserInfo(currentUser);
                if (userInfo != null && userInfo.isGuest()) {
                    UserSwitcherController.this.showGuestNotification(currentUser);
                }
                UserSwitcherController.this.refreshUsers(-10000);
            }
        }
    };
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("com.android.systemui.REMOVE_GUEST".equals(intent.getAction())) {
                int currentUser = ActivityManager.getCurrentUser();
                UserInfo userInfo = UserSwitcherController.this.mUserManager.getUserInfo(currentUser);
                if (userInfo != null && userInfo.isGuest()) {
                    UserSwitcherController.this.showExitGuestDialog(currentUser);
                    return;
                }
                return;
            }
            boolean z = false;
            int i = -10000;
            if ("com.android.systemui.LOGOUT_USER".equals(intent.getAction())) {
                UserSwitcherController.this.logoutCurrentUser();
            } else if ("android.intent.action.USER_SWITCHED".equals(intent.getAction())) {
                if (UserSwitcherController.this.mExitGuestDialog != null && UserSwitcherController.this.mExitGuestDialog.isShowing()) {
                    UserSwitcherController.this.mExitGuestDialog.cancel();
                    Dialog unused = UserSwitcherController.this.mExitGuestDialog = null;
                }
                int intExtra = intent.getIntExtra("android.intent.extra.user_handle", -1);
                UserInfo userInfo2 = UserSwitcherController.this.mUserManager.getUserInfo(intExtra);
                int size = UserSwitcherController.this.mUsers.size();
                int i2 = 0;
                while (i2 < size) {
                    UserRecord userRecord = (UserRecord) UserSwitcherController.this.mUsers.get(i2);
                    UserInfo userInfo3 = userRecord.info;
                    if (userInfo3 != null) {
                        boolean z2 = userInfo3.id == intExtra;
                        if (userRecord.isCurrent != z2) {
                            UserSwitcherController.this.mUsers.set(i2, userRecord.copyWithIsCurrent(z2));
                        }
                        if (z2 && !userRecord.isGuest) {
                            int unused2 = UserSwitcherController.this.mLastNonGuestUser = userRecord.info.id;
                        }
                        if ((userInfo2 == null || !userInfo2.isAdmin()) && userRecord.isRestricted) {
                            UserSwitcherController.this.mUsers.remove(i2);
                            i2--;
                        }
                    }
                    i2++;
                }
                UserSwitcherController.this.notifyAdapters();
                if (UserSwitcherController.this.mSecondaryUser != -10000) {
                    context.stopServiceAsUser(UserSwitcherController.this.mSecondaryUserServiceIntent, UserHandleCompat.of(UserSwitcherController.this.mSecondaryUser));
                    int unused3 = UserSwitcherController.this.mSecondaryUser = -10000;
                }
                if (!(userInfo2 == null || userInfo2.id == 0)) {
                    context.startServiceAsUser(UserSwitcherController.this.mSecondaryUserServiceIntent, UserHandleCompat.of(userInfo2.id));
                    int unused4 = UserSwitcherController.this.mSecondaryUser = userInfo2.id;
                }
                if (UserManagerCompat.isSplitSystemUser() && userInfo2 != null && !userInfo2.isGuest() && userInfo2.id != 0) {
                    showLogoutNotification(intExtra);
                }
                if (userInfo2 != null && userInfo2.isGuest()) {
                    UserSwitcherController.this.showGuestNotification(intExtra);
                }
                z = true;
            } else if ("android.intent.action.USER_INFO_CHANGED".equals(intent.getAction())) {
                i = intent.getIntExtra("android.intent.extra.user_handle", -10000);
            } else if ("android.intent.action.USER_UNLOCKED".equals(intent.getAction()) && intent.getIntExtra("android.intent.extra.user_handle", -10000) != 0) {
                return;
            }
            UserSwitcherController.this.refreshUsers(i);
            if (z) {
                UserSwitcherController.this.mUnpauseRefreshUsers.run();
            }
        }

        private void showLogoutNotification(int i) {
            PendingIntent broadcastAsUser = PendingIntent.getBroadcastAsUser(UserSwitcherController.this.mContext, 0, new Intent("com.android.systemui.LOGOUT_USER"), 0, UserHandleCompat.SYSTEM);
            Notification.Builder addAction = NotificationCompat.newBuilder(UserSwitcherController.this.mContext, NotificationChannels.GENERAL).setVisibility(-1).setSmallIcon(R.drawable.ic_person).setContentTitle(UserSwitcherController.this.mContext.getString(R.string.user_logout_notification_title)).setContentText(UserSwitcherController.this.mContext.getString(R.string.user_logout_notification_text)).setContentIntent(broadcastAsUser).setOngoing(true).setShowWhen(false).addAction(R.drawable.ic_delete, UserSwitcherController.this.mContext.getString(R.string.user_logout_notification_action), broadcastAsUser);
            SystemUI.overrideNotificationAppName(UserSwitcherController.this.mContext, addAction);
            NotificationManager.from(UserSwitcherController.this.mContext).notifyAsUser("logout_user", b.k, addAction.build(), new UserHandle(i));
        }
    };
    private boolean mResumeUserOnGuestLogout = true;
    /* access modifiers changed from: private */
    public int mSecondaryUser = -10000;
    /* access modifiers changed from: private */
    public Intent mSecondaryUserServiceIntent;
    private final ContentObserver mSettingsObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean z) {
            UserSwitcherController userSwitcherController = UserSwitcherController.this;
            boolean z2 = false;
            boolean unused = userSwitcherController.mSimpleUserSwitcher = Settings.Global.getInt(userSwitcherController.mContext.getContentResolver(), "lockscreenSimpleUserSwitcher", 0) != 0;
            UserSwitcherController userSwitcherController2 = UserSwitcherController.this;
            if (Settings.Global.getInt(userSwitcherController2.mContext.getContentResolver(), "add_users_when_locked", 0) != 0) {
                z2 = true;
            }
            boolean unused2 = userSwitcherController2.mAddUsersWhenLocked = z2;
            UserSwitcherController.this.refreshUsers(-10000);
        }
    };
    /* access modifiers changed from: private */
    public boolean mSimpleUserSwitcher;
    /* access modifiers changed from: private */
    public final Runnable mUnpauseRefreshUsers = new Runnable() {
        public void run() {
            UserSwitcherController.this.mHandler.removeCallbacks(this);
            boolean unused = UserSwitcherController.this.mPauseRefreshUsers = false;
            UserSwitcherController.this.refreshUsers(-10000);
        }
    };
    protected final UserManager mUserManager;
    /* access modifiers changed from: private */
    public ArrayList<UserRecord> mUsers = new ArrayList<>();
    public final DetailAdapter userDetailAdapter = new DetailAdapter() {
        private final Intent USER_SETTINGS_INTENT = new Intent("android.settings.USER_SETTINGS");

        public int getMetricsCategory() {
            return 125;
        }

        public boolean getToggleEnabled() {
            return true;
        }

        public Boolean getToggleState() {
            return null;
        }

        public boolean hasHeader() {
            return true;
        }

        public void setToggleState(boolean z) {
        }

        public CharSequence getTitle() {
            return UserSwitcherController.this.mContext.getString(R.string.quick_settings_user_title);
        }

        public View createDetailView(Context context, View view, ViewGroup viewGroup) {
            UserDetailView userDetailView;
            if (!(view instanceof UserDetailView)) {
                userDetailView = UserDetailView.inflate(context, viewGroup, false);
                userDetailView.createAndSetAdapter(UserSwitcherController.this);
            } else {
                userDetailView = (UserDetailView) view;
            }
            userDetailView.refreshAdapter();
            return userDetailView;
        }

        public Intent getSettingsIntent() {
            return this.USER_SETTINGS_INTENT;
        }
    };

    public UserSwitcherController(Context context, KeyguardMonitor keyguardMonitor, Handler handler, ActivityStarter activityStarter) {
        this.mContext = context;
        this.mGuestResumeSessionReceiver.register(context);
        this.mKeyguardMonitor = keyguardMonitor;
        this.mHandler = handler;
        this.mActivityStarter = activityStarter;
        this.mUserManager = UserManager.get(context);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.USER_ADDED");
        intentFilter.addAction("android.intent.action.USER_REMOVED");
        intentFilter.addAction("android.intent.action.USER_INFO_CHANGED");
        intentFilter.addAction("android.intent.action.USER_SWITCHED");
        intentFilter.addAction("android.intent.action.USER_STOPPED");
        intentFilter.addAction("android.intent.action.USER_UNLOCKED");
        this.mContext.registerReceiverAsUser(this.mReceiver, UserHandleCompat.SYSTEM, intentFilter, (String) null, (Handler) null);
        this.mSecondaryUserServiceIntent = new Intent(context, SystemUISecondaryUserService.class);
        IntentFilter intentFilter2 = new IntentFilter();
        intentFilter2.addAction("com.android.systemui.REMOVE_GUEST");
        intentFilter2.addAction("com.android.systemui.LOGOUT_USER");
        this.mContext.registerReceiverAsUser(this.mReceiver, UserHandleCompat.SYSTEM, intentFilter2, "com.android.systemui.permission.SELF", (Handler) null);
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("lockscreenSimpleUserSwitcher"), true, this.mSettingsObserver);
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("add_users_when_locked"), true, this.mSettingsObserver);
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("allow_user_switching_when_system_user_locked"), true, this.mSettingsObserver);
        this.mSettingsObserver.onChange(false);
        keyguardMonitor.addCallback(this.mCallback);
        listenForCallState();
        refreshUsers(-10000);
    }

    /* access modifiers changed from: private */
    public void refreshUsers(int i) {
        UserInfo userInfo;
        if (i != -10000) {
            this.mForcePictureLoadForUserId.put(i, true);
        }
        if (!this.mPauseRefreshUsers) {
            boolean z = this.mForcePictureLoadForUserId.get(-1);
            SparseArray sparseArray = new SparseArray(this.mUsers.size());
            int size = this.mUsers.size();
            for (int i2 = 0; i2 < size; i2++) {
                UserRecord userRecord = this.mUsers.get(i2);
                if (!(userRecord == null || userRecord.picture == null || (userInfo = userRecord.info) == null || z || this.mForcePictureLoadForUserId.get(userInfo.id))) {
                    sparseArray.put(userRecord.info.id, userRecord.picture);
                }
            }
            this.mForcePictureLoadForUserId.clear();
            final boolean z2 = this.mAddUsersWhenLocked;
            new AsyncTask<SparseArray<Bitmap>, Void, ArrayList<UserRecord>>() {
                /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r10v3, resolved type: java.lang.Object} */
                /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v2, resolved type: android.content.pm.UserInfo} */
                /* access modifiers changed from: protected */
                /* JADX WARNING: Multi-variable type inference failed */
                /* Code decompiled incorrectly, please refer to instructions dump. */
                public java.util.ArrayList<com.android.systemui.statusbar.policy.UserSwitcherController.UserRecord> doInBackground(android.util.SparseArray<android.graphics.Bitmap>... r21) {
                    /*
                        r20 = this;
                        r0 = r20
                        r1 = 0
                        r2 = r21[r1]
                        com.android.systemui.statusbar.policy.UserSwitcherController r3 = com.android.systemui.statusbar.policy.UserSwitcherController.this
                        android.os.UserManager r3 = r3.mUserManager
                        r4 = 1
                        java.util.List r3 = r3.getUsers(r4)
                        r5 = 0
                        if (r3 != 0) goto L_0x0012
                        return r5
                    L_0x0012:
                        java.util.ArrayList r6 = new java.util.ArrayList
                        int r7 = r3.size()
                        r6.<init>(r7)
                        int r7 = android.app.ActivityManager.getCurrentUser()
                        com.android.systemui.statusbar.policy.UserSwitcherController r8 = com.android.systemui.statusbar.policy.UserSwitcherController.this
                        android.os.UserManager r8 = r8.mUserManager
                        boolean r8 = android.os.UserManagerCompat.canSwitchUsers(r8)
                        java.util.Iterator r3 = r3.iterator()
                        r9 = r5
                    L_0x002c:
                        boolean r10 = r3.hasNext()
                        if (r10 == 0) goto L_0x00be
                        java.lang.Object r10 = r3.next()
                        r12 = r10
                        android.content.pm.UserInfo r12 = (android.content.pm.UserInfo) r12
                        int r10 = r12.id
                        if (r7 != r10) goto L_0x003f
                        r15 = r4
                        goto L_0x0040
                    L_0x003f:
                        r15 = r1
                    L_0x0040:
                        if (r15 == 0) goto L_0x0043
                        r5 = r12
                    L_0x0043:
                        if (r8 != 0) goto L_0x004b
                        if (r15 == 0) goto L_0x0048
                        goto L_0x004b
                    L_0x0048:
                        r18 = r1
                        goto L_0x004d
                    L_0x004b:
                        r18 = r4
                    L_0x004d:
                        boolean r10 = r12.isEnabled()
                        if (r10 == 0) goto L_0x00bb
                        boolean r10 = r12.isGuest()
                        if (r10 == 0) goto L_0x006d
                        com.android.systemui.statusbar.policy.UserSwitcherController$UserRecord r17 = new com.android.systemui.statusbar.policy.UserSwitcherController$UserRecord
                        r11 = 0
                        r13 = 1
                        r14 = 0
                        r16 = 0
                        r9 = r17
                        r10 = r12
                        r12 = r13
                        r13 = r15
                        r15 = r16
                        r16 = r8
                        r9.<init>(r10, r11, r12, r13, r14, r15, r16)
                        goto L_0x00bb
                    L_0x006d:
                        boolean r10 = android.content.pm.UserInfoCompat.supportsSwitchToByUser(r12)
                        if (r10 == 0) goto L_0x00bb
                        int r10 = r12.id
                        java.lang.Object r10 = r2.get(r10)
                        android.graphics.Bitmap r10 = (android.graphics.Bitmap) r10
                        if (r10 != 0) goto L_0x009c
                        com.android.systemui.statusbar.policy.UserSwitcherController r10 = com.android.systemui.statusbar.policy.UserSwitcherController.this
                        android.os.UserManager r10 = r10.mUserManager
                        int r11 = r12.id
                        android.graphics.Bitmap r10 = r10.getUserIcon(r11)
                        if (r10 == 0) goto L_0x009c
                        com.android.systemui.statusbar.policy.UserSwitcherController r11 = com.android.systemui.statusbar.policy.UserSwitcherController.this
                        android.content.Context r11 = r11.mContext
                        android.content.res.Resources r11 = r11.getResources()
                        r13 = 2131165892(0x7f0702c4, float:1.7946014E38)
                        int r11 = r11.getDimensionPixelSize(r13)
                        android.graphics.Bitmap r10 = android.graphics.Bitmap.createScaledBitmap(r10, r11, r11, r4)
                    L_0x009c:
                        r13 = r10
                        if (r15 == 0) goto L_0x00a1
                        r10 = r1
                        goto L_0x00a5
                    L_0x00a1:
                        int r10 = r6.size()
                    L_0x00a5:
                        com.android.systemui.statusbar.policy.UserSwitcherController$UserRecord r14 = new com.android.systemui.statusbar.policy.UserSwitcherController$UserRecord
                        r16 = 0
                        r17 = 0
                        r19 = 0
                        r11 = r14
                        r1 = r14
                        r14 = r16
                        r16 = r17
                        r17 = r19
                        r11.<init>(r12, r13, r14, r15, r16, r17, r18)
                        r6.add(r10, r1)
                    L_0x00bb:
                        r1 = 0
                        goto L_0x002c
                    L_0x00be:
                        com.android.systemui.statusbar.policy.UserSwitcherController r1 = com.android.systemui.statusbar.policy.UserSwitcherController.this
                        android.os.UserManager r1 = r1.mUserManager
                        android.os.UserHandle r2 = android.os.UserHandleCompat.SYSTEM
                        java.lang.String r3 = "no_add_user"
                        boolean r1 = android.os.UserManagerCompat.hasBaseUserRestriction(r1, r3, r2)
                        r1 = r1 ^ r4
                        if (r5 == 0) goto L_0x00db
                        boolean r2 = r5.isAdmin()
                        if (r2 != 0) goto L_0x00d7
                        int r2 = r5.id
                        if (r2 != 0) goto L_0x00db
                    L_0x00d7:
                        if (r1 == 0) goto L_0x00db
                        r2 = r4
                        goto L_0x00dc
                    L_0x00db:
                        r2 = 0
                    L_0x00dc:
                        if (r1 == 0) goto L_0x00e4
                        boolean r1 = r9
                        if (r1 == 0) goto L_0x00e4
                        r1 = r4
                        goto L_0x00e5
                    L_0x00e4:
                        r1 = 0
                    L_0x00e5:
                        if (r2 != 0) goto L_0x00e9
                        if (r1 == 0) goto L_0x00ed
                    L_0x00e9:
                        if (r9 != 0) goto L_0x00ed
                        r3 = r4
                        goto L_0x00ee
                    L_0x00ed:
                        r3 = 0
                    L_0x00ee:
                        if (r2 != 0) goto L_0x00f2
                        if (r1 == 0) goto L_0x00fe
                    L_0x00f2:
                        com.android.systemui.statusbar.policy.UserSwitcherController r1 = com.android.systemui.statusbar.policy.UserSwitcherController.this
                        android.os.UserManager r1 = r1.mUserManager
                        boolean r1 = r1.canAddMoreUsers()
                        if (r1 == 0) goto L_0x00fe
                        r1 = r4
                        goto L_0x00ff
                    L_0x00fe:
                        r1 = 0
                    L_0x00ff:
                        boolean r2 = r9
                        r2 = r2 ^ r4
                        com.android.systemui.statusbar.policy.UserSwitcherController r4 = com.android.systemui.statusbar.policy.UserSwitcherController.this
                        boolean r4 = r4.mSimpleUserSwitcher
                        if (r4 != 0) goto L_0x0132
                        if (r9 != 0) goto L_0x0125
                        if (r3 == 0) goto L_0x0132
                        com.android.systemui.statusbar.policy.UserSwitcherController$UserRecord r3 = new com.android.systemui.statusbar.policy.UserSwitcherController$UserRecord
                        r10 = 0
                        r11 = 0
                        r12 = 1
                        r13 = 0
                        r14 = 0
                        r9 = r3
                        r15 = r2
                        r16 = r8
                        r9.<init>(r10, r11, r12, r13, r14, r15, r16)
                        com.android.systemui.statusbar.policy.UserSwitcherController r4 = com.android.systemui.statusbar.policy.UserSwitcherController.this
                        r4.checkIfAddUserDisallowedByAdminOnly(r3)
                        r6.add(r3)
                        goto L_0x0132
                    L_0x0125:
                        boolean r3 = r9.isCurrent
                        if (r3 == 0) goto L_0x012b
                        r3 = 0
                        goto L_0x012f
                    L_0x012b:
                        int r3 = r6.size()
                    L_0x012f:
                        r6.add(r3, r9)
                    L_0x0132:
                        com.android.systemui.statusbar.policy.UserSwitcherController r3 = com.android.systemui.statusbar.policy.UserSwitcherController.this
                        boolean r3 = r3.mSimpleUserSwitcher
                        if (r3 != 0) goto L_0x0152
                        if (r1 == 0) goto L_0x0152
                        com.android.systemui.statusbar.policy.UserSwitcherController$UserRecord r1 = new com.android.systemui.statusbar.policy.UserSwitcherController$UserRecord
                        r10 = 0
                        r11 = 0
                        r12 = 0
                        r13 = 0
                        r14 = 1
                        r9 = r1
                        r15 = r2
                        r16 = r8
                        r9.<init>(r10, r11, r12, r13, r14, r15, r16)
                        com.android.systemui.statusbar.policy.UserSwitcherController r0 = com.android.systemui.statusbar.policy.UserSwitcherController.this
                        r0.checkIfAddUserDisallowedByAdminOnly(r1)
                        r6.add(r1)
                    L_0x0152:
                        return r6
                    */
                    throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.policy.UserSwitcherController.AnonymousClass1.doInBackground(android.util.SparseArray[]):java.util.ArrayList");
                }

                /* access modifiers changed from: protected */
                public void onPostExecute(ArrayList<UserRecord> arrayList) {
                    if (arrayList != null) {
                        ArrayList unused = UserSwitcherController.this.mUsers = arrayList;
                        UserSwitcherController.this.notifyAdapters();
                    }
                }
            }.execute(new SparseArray[]{sparseArray});
        }
    }

    private void pauseRefreshUsers() {
        if (!this.mPauseRefreshUsers) {
            this.mHandler.postDelayed(this.mUnpauseRefreshUsers, 3000);
            this.mPauseRefreshUsers = true;
        }
    }

    /* access modifiers changed from: private */
    public void notifyAdapters() {
        for (int size = this.mAdapters.size() - 1; size >= 0; size--) {
            BaseUserAdapter baseUserAdapter = (BaseUserAdapter) this.mAdapters.get(size).get();
            if (baseUserAdapter != null) {
                baseUserAdapter.notifyDataSetChanged();
            } else {
                this.mAdapters.remove(size);
            }
        }
    }

    public boolean isSimpleUserSwitcher() {
        return this.mSimpleUserSwitcher;
    }

    public boolean useFullscreenUserSwitcher() {
        int i = Settings.System.getInt(this.mContext.getContentResolver(), "enable_fullscreen_user_switcher", -1);
        if (i != -1) {
            return i != 0;
        }
        return this.mContext.getResources().getBoolean(R.bool.config_enableFullscreenUserSwitcher);
    }

    public void logoutCurrentUser() {
        if (ActivityManager.getCurrentUser() != 0) {
            pauseRefreshUsers();
            ActivityManagerCompat.logoutCurrentUser();
        }
    }

    public void switchTo(UserRecord userRecord) {
        int i;
        if (userRecord.isGuest && userRecord.info == null) {
            UserManager userManager = this.mUserManager;
            Context context = this.mContext;
            UserInfo createGuest = userManager.createGuest(context, context.getString(R.string.guest_nickname));
            if (createGuest != null) {
                i = createGuest.id;
            } else {
                return;
            }
        } else if (userRecord.isAddUser) {
            showAddUserDialog();
            return;
        } else {
            i = userRecord.info.id;
        }
        if (ActivityManager.getCurrentUser() != i) {
            switchToUserId(i);
        } else if (userRecord.isGuest) {
            showExitGuestDialog(i);
        }
    }

    /* access modifiers changed from: protected */
    public void switchToUserId(int i) {
        try {
            pauseRefreshUsers();
            ActivityManagerCompat.getService().switchUser(i);
        } catch (RemoteException e) {
            Log.e("UserSwitcherController", "Couldn't switch user.", e);
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:4:0x0008, code lost:
        r0 = r2.mUserManager.getUserInfo((r0 = r2.mLastNonGuestUser));
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void showExitGuestDialog(int r3) {
        /*
            r2 = this;
            boolean r0 = r2.mResumeUserOnGuestLogout
            if (r0 == 0) goto L_0x001f
            int r0 = r2.mLastNonGuestUser
            if (r0 == 0) goto L_0x001f
            android.os.UserManager r1 = r2.mUserManager
            android.content.pm.UserInfo r0 = r1.getUserInfo(r0)
            if (r0 == 0) goto L_0x001f
            boolean r1 = r0.isEnabled()
            if (r1 == 0) goto L_0x001f
            boolean r1 = android.content.pm.UserInfoCompat.supportsSwitchToByUser(r0)
            if (r1 == 0) goto L_0x001f
            int r0 = r0.id
            goto L_0x0020
        L_0x001f:
            r0 = 0
        L_0x0020:
            r2.showExitGuestDialog(r3, r0)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.policy.UserSwitcherController.showExitGuestDialog(int):void");
    }

    /* access modifiers changed from: protected */
    public void showExitGuestDialog(int i, int i2) {
        Dialog dialog = this.mExitGuestDialog;
        if (dialog != null && dialog.isShowing()) {
            this.mExitGuestDialog.cancel();
        }
        this.mExitGuestDialog = new ExitGuestDialog(this.mContext, i, i2);
        this.mExitGuestDialog.show();
    }

    public void showAddUserDialog() {
        Dialog dialog = this.mAddUserDialog;
        if (dialog != null && dialog.isShowing()) {
            this.mAddUserDialog.cancel();
        }
        this.mAddUserDialog = new AddUserDialog(this.mContext);
        this.mAddUserDialog.show();
    }

    /* access modifiers changed from: protected */
    public void exitGuest(int i, int i2) {
        switchToUserId(i2);
        this.mUserManager.removeUser(i);
    }

    private void listenForCallState() {
        TelephonyManager.from(this.mContext).listen(this.mPhoneStateListener, 32);
    }

    /* access modifiers changed from: private */
    public void showGuestNotification(int i) {
        PendingIntent broadcastAsUser = UserManagerCompat.canSwitchUsers(this.mUserManager) ? PendingIntent.getBroadcastAsUser(this.mContext, 0, new Intent("com.android.systemui.REMOVE_GUEST"), 0, UserHandleCompat.SYSTEM) : null;
        Notification.Builder addAction = NotificationCompat.newBuilder(this.mContext, NotificationChannels.GENERAL).setVisibility(-1).setSmallIcon(R.drawable.ic_person).setContentTitle(this.mContext.getString(R.string.guest_notification_title)).setContentText(this.mContext.getString(R.string.guest_notification_text)).setContentIntent(broadcastAsUser).setShowWhen(false).addAction(R.drawable.ic_delete, this.mContext.getString(R.string.guest_notification_remove_action), broadcastAsUser);
        SystemUI.overrideNotificationAppName(this.mContext, addAction);
        NotificationManager.from(this.mContext).notifyAsUser("remove_guest", b.j, addAction.build(), new UserHandle(i));
    }

    public String getCurrentUserName(Context context) {
        UserRecord userRecord;
        UserInfo userInfo;
        if (this.mUsers.isEmpty() || (userRecord = this.mUsers.get(0)) == null || (userInfo = userRecord.info) == null) {
            return null;
        }
        if (userRecord.isGuest) {
            return context.getString(R.string.guest_nickname);
        }
        return userInfo.name;
    }

    public void onDensityOrFontScaleChanged() {
        refreshUsers(-1);
    }

    @VisibleForTesting
    public void addAdapter(WeakReference<BaseUserAdapter> weakReference) {
        this.mAdapters.add(weakReference);
    }

    @VisibleForTesting
    public ArrayList<UserRecord> getUsers() {
        return this.mUsers;
    }

    public static abstract class BaseUserAdapter extends BaseAdapter {
        final UserSwitcherController mController;
        private final KeyguardMonitor mKeyguardMonitor = ((KeyguardMonitor) Dependency.get(KeyguardMonitor.class));

        public long getItemId(int i) {
            return (long) i;
        }

        protected BaseUserAdapter(UserSwitcherController userSwitcherController) {
            this.mController = userSwitcherController;
            userSwitcherController.addAdapter(new WeakReference(this));
        }

        public int getUserCount() {
            if (!(this.mKeyguardMonitor.isShowing() && this.mKeyguardMonitor.isSecure() && !this.mKeyguardMonitor.canSkipBouncer())) {
                return this.mController.getUsers().size();
            }
            int size = this.mController.getUsers().size();
            int i = 0;
            for (int i2 = 0; i2 < size; i2++) {
                if (!this.mController.getUsers().get(i2).isGuest) {
                    if (this.mController.getUsers().get(i2).isRestricted) {
                        break;
                    }
                    i++;
                }
            }
            return i;
        }

        public int getCount() {
            int i = 0;
            if (!(this.mKeyguardMonitor.isShowing() && this.mKeyguardMonitor.isSecure() && !this.mKeyguardMonitor.canSkipBouncer())) {
                return this.mController.getUsers().size();
            }
            int size = this.mController.getUsers().size();
            int i2 = 0;
            while (i < size && !this.mController.getUsers().get(i).isRestricted) {
                i2++;
                i++;
            }
            return i2;
        }

        public UserRecord getItem(int i) {
            return this.mController.getUsers().get(i);
        }

        public void switchTo(UserRecord userRecord) {
            this.mController.switchTo(userRecord);
        }

        public String getName(Context context, UserRecord userRecord) {
            if (userRecord.isGuest) {
                if (userRecord.isCurrent) {
                    return context.getString(R.string.guest_exit_guest);
                }
                return context.getString(userRecord.info == null ? R.string.guest_new_guest : R.string.guest_nickname);
            } else if (userRecord.isAddUser) {
                return context.getString(R.string.user_add_user);
            } else {
                return userRecord.info.name;
            }
        }

        public Drawable getDrawable(Context context, UserRecord userRecord) {
            if (userRecord.isAddUser) {
                return context.getDrawable(R.drawable.ic_add_circle_qs);
            }
            Drawable defaultUserIcon = UserIconsCompat.getDefaultUserIcon(context.getResources(), userRecord.resolveId(), false);
            if (userRecord.isGuest) {
                defaultUserIcon.setColorFilter(Utils.getColorAttr(context, 16842800), PorterDuff.Mode.SRC_IN);
            }
            return defaultUserIcon;
        }

        public void refresh() {
            this.mController.refreshUsers(-10000);
        }
    }

    /* access modifiers changed from: private */
    public void checkIfAddUserDisallowedByAdminOnly(UserRecord userRecord) {
        RestrictedLockUtils.EnforcedAdmin checkIfRestrictionEnforced = RestrictedLockUtilsHelper.checkIfRestrictionEnforced(this.mContext, "no_add_user", KeyguardUpdateMonitor.getCurrentUser());
        if (checkIfRestrictionEnforced == null || RestrictedLockUtilsHelper.hasBaseUserRestriction(this.mContext, "no_add_user", KeyguardUpdateMonitor.getCurrentUser())) {
            userRecord.isDisabledByAdmin = false;
            userRecord.enforcedAdmin = null;
            return;
        }
        userRecord.isDisabledByAdmin = true;
        userRecord.enforcedAdmin = checkIfRestrictionEnforced;
    }

    public void startActivity(Intent intent) {
        this.mActivityStarter.startActivity(intent, true);
    }

    public static final class UserRecord {
        public RestrictedLockUtils.EnforcedAdmin enforcedAdmin;
        public final UserInfo info;
        public final boolean isAddUser;
        public final boolean isCurrent;
        public boolean isDisabledByAdmin;
        public final boolean isGuest;
        public final boolean isRestricted;
        public boolean isSwitchToEnabled;
        public final Bitmap picture;

        public UserRecord(UserInfo userInfo, Bitmap bitmap, boolean z, boolean z2, boolean z3, boolean z4, boolean z5) {
            this.info = userInfo;
            this.picture = bitmap;
            this.isGuest = z;
            this.isCurrent = z2;
            this.isAddUser = z3;
            this.isRestricted = z4;
            this.isSwitchToEnabled = z5;
        }

        public UserRecord copyWithIsCurrent(boolean z) {
            return new UserRecord(this.info, this.picture, this.isGuest, z, this.isAddUser, this.isRestricted, this.isSwitchToEnabled);
        }

        public int resolveId() {
            UserInfo userInfo;
            if (this.isGuest || (userInfo = this.info) == null) {
                return -10000;
            }
            return userInfo.id;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("UserRecord(");
            if (this.info != null) {
                sb.append("name=\"");
                sb.append(this.info.name);
                sb.append("\" id=");
                sb.append(this.info.id);
            } else if (this.isGuest) {
                sb.append("<add guest placeholder>");
            } else if (this.isAddUser) {
                sb.append("<add user placeholder>");
            }
            if (this.isGuest) {
                sb.append(" <isGuest>");
            }
            if (this.isAddUser) {
                sb.append(" <isAddUser>");
            }
            if (this.isCurrent) {
                sb.append(" <isCurrent>");
            }
            if (this.picture != null) {
                sb.append(" <hasPicture>");
            }
            if (this.isRestricted) {
                sb.append(" <isRestricted>");
            }
            if (this.isDisabledByAdmin) {
                sb.append(" <isDisabledByAdmin>");
                sb.append(" enforcedAdmin=");
                sb.append(this.enforcedAdmin);
            }
            if (this.isSwitchToEnabled) {
                sb.append(" <isSwitchToEnabled>");
            }
            sb.append(')');
            return sb.toString();
        }
    }

    private final class ExitGuestDialog extends SystemUIDialog implements DialogInterface.OnClickListener {
        private final int mGuestId;
        private final int mTargetId;

        public ExitGuestDialog(Context context, int i, int i2) {
            super(context);
            setTitle(R.string.guest_exit_guest_dialog_title);
            setMessage(context.getString(R.string.guest_exit_guest_dialog_message));
            setButton(-2, context.getString(17039360), this);
            setButton(-1, context.getString(R.string.guest_exit_guest_dialog_remove), this);
            setCanceledOnTouchOutside(false);
            this.mGuestId = i;
            this.mTargetId = i2;
        }

        public void onClick(DialogInterface dialogInterface, int i) {
            if (i == -2) {
                cancel();
                return;
            }
            dismiss();
            UserSwitcherController.this.exitGuest(this.mGuestId, this.mTargetId);
        }
    }

    private final class AddUserDialog extends SystemUIDialog implements DialogInterface.OnClickListener {
        public AddUserDialog(Context context) {
            super(context);
            setTitle(R.string.user_add_user_title);
            setMessage(context.getString(R.string.user_add_user_message_short));
            setButton(-2, context.getString(17039360), this);
            setButton(-1, context.getString(17039370), this);
        }

        public void onClick(DialogInterface dialogInterface, int i) {
            if (i == -2) {
                cancel();
                return;
            }
            dismiss();
            if (!ActivityManager.isUserAMonkey()) {
                UserSwitcherController userSwitcherController = UserSwitcherController.this;
                UserInfo createUser = userSwitcherController.mUserManager.createUser(userSwitcherController.mContext.getString(R.string.user_new_user_name), 0);
                if (createUser != null) {
                    int i2 = createUser.id;
                    UserSwitcherController.this.mUserManager.setUserIcon(i2, UserIcons.convertToBitmap(UserIconsCompat.getDefaultUserIcon(UserSwitcherController.this.mContext.getResources(), i2, false)));
                    UserSwitcherController.this.switchToUserId(i2);
                }
            }
        }
    }
}
