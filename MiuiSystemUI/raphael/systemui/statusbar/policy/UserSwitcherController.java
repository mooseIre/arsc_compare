package com.android.systemui.statusbar.policy;

import android.app.ActivityManager;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.UserInfo;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import androidx.appcompat.R$styleable;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.logging.UiEventLogger;
import com.android.internal.util.UserIcons;
import com.android.settingslib.R$string;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtilsInternal;
import com.android.systemui.C0010R$bool;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0021R$string;
import com.android.systemui.DejankUtils;
import com.android.systemui.Dependency;
import com.android.systemui.Dumpable;
import com.android.systemui.GuestResumeSessionReceiver;
import com.android.systemui.SystemUISecondaryUserService;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.qs.DetailAdapter;
import com.android.systemui.qs.QSUserSwitcherEvent;
import com.android.systemui.qs.tiles.UserDetailView;
import com.android.systemui.statusbar.phone.SystemUIDialog;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.miui.systemui.SettingsObserver;
import com.miui.systemui.content.UserContextWrapper;
import com.miui.systemui.util.MiuiTextUtils;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
import org.jetbrains.annotations.Nullable;

public class UserSwitcherController implements Dumpable {
    /* access modifiers changed from: private */
    public static int sKidSpaceUser;
    private static int sMaintenanceModeId;
    /* access modifiers changed from: private */
    public static int sSecondUser;
    private final ActivityStarter mActivityStarter;
    private final ArrayList<WeakReference<BaseUserAdapter>> mAdapters = new ArrayList<>();
    private Dialog mAddUserDialog;
    /* access modifiers changed from: private */
    public boolean mAddUsersWhenLocked;
    private final BroadcastDispatcher mBroadcastDispatcher;
    private final KeyguardStateController.Callback mCallback = new KeyguardStateController.Callback() {
        public void onKeyguardShowingChanged() {
            if (!UserSwitcherController.this.mKeyguardStateController.isShowing()) {
                UserSwitcherController userSwitcherController = UserSwitcherController.this;
                userSwitcherController.mHandler.post(new Runnable() {
                    public final void run() {
                        UserSwitcherController.this.notifyAdapters();
                    }
                });
                return;
            }
            UserSwitcherController.this.notifyAdapters();
        }
    };
    protected final Context mContext;
    /* access modifiers changed from: private */
    public Dialog mExitGuestDialog;
    private SparseBooleanArray mForcePictureLoadForUserId = new SparseBooleanArray(2);
    private final GuestResumeSessionReceiver mGuestResumeSessionReceiver = new GuestResumeSessionReceiver();
    protected final Handler mHandler;
    /* access modifiers changed from: private */
    public final KeyguardStateController mKeyguardStateController;
    /* access modifiers changed from: private */
    public int mLastNonGuestUser = 0;
    /* access modifiers changed from: private */
    public boolean mPauseRefreshUsers;
    private final PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        private int mCallState;

        public void onCallStateChanged(int i, String str) {
            if (this.mCallState != i) {
                this.mCallState = i;
                UserSwitcherController.this.refreshUsers(-10000);
            }
        }
    };
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            boolean z = true;
            int i = -10000;
            if ("android.intent.action.USER_SWITCHED".equals(intent.getAction())) {
                if (UserSwitcherController.this.mExitGuestDialog != null && UserSwitcherController.this.mExitGuestDialog.isShowing()) {
                    UserSwitcherController.this.mExitGuestDialog.cancel();
                    Dialog unused = UserSwitcherController.this.mExitGuestDialog = null;
                }
                int intExtra = intent.getIntExtra("android.intent.extra.user_handle", -1);
                UserSwitcherController.this.mUserContextWrapper.setUserId(intExtra);
                UserInfo userInfo = UserSwitcherController.this.mUserManager.getUserInfo(intExtra);
                int size = UserSwitcherController.this.mUsers.size();
                int i2 = 0;
                while (i2 < size) {
                    UserRecord userRecord = (UserRecord) UserSwitcherController.this.mUsers.get(i2);
                    UserInfo userInfo2 = userRecord.info;
                    if (userInfo2 != null) {
                        boolean z2 = userInfo2.id == intExtra;
                        if (userRecord.isCurrent != z2) {
                            UserSwitcherController.this.mUsers.set(i2, userRecord.copyWithIsCurrent(z2));
                        }
                        if (z2 && !userRecord.isGuest) {
                            int unused2 = UserSwitcherController.this.mLastNonGuestUser = userRecord.info.id;
                        }
                        if ((userInfo == null || !userInfo.isAdmin()) && userRecord.isRestricted) {
                            UserSwitcherController.this.mUsers.remove(i2);
                            i2--;
                        }
                    }
                    i2++;
                }
                UserSwitcherController.this.notifyAdapters();
                if (UserSwitcherController.this.mSecondaryUser != -10000) {
                    context.stopServiceAsUser(UserSwitcherController.this.mSecondaryUserServiceIntent, UserHandle.of(UserSwitcherController.this.mSecondaryUser));
                    int unused3 = UserSwitcherController.this.mSecondaryUser = -10000;
                }
                if (!(userInfo == null || userInfo.id == 0)) {
                    context.startServiceAsUser(UserSwitcherController.this.mSecondaryUserServiceIntent, UserHandle.of(userInfo.id));
                    int unused4 = UserSwitcherController.this.mSecondaryUser = userInfo.id;
                }
            } else {
                if ("android.intent.action.USER_INFO_CHANGED".equals(intent.getAction())) {
                    i = intent.getIntExtra("android.intent.extra.user_handle", -10000);
                } else if ("android.intent.action.USER_UNLOCKED".equals(intent.getAction()) && intent.getIntExtra("android.intent.extra.user_handle", -10000) != 0) {
                    return;
                }
                z = false;
            }
            UserSwitcherController.this.refreshUsers(i);
            if (z) {
                UserSwitcherController.this.mUnpauseRefreshUsers.run();
            }
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
            boolean unused = userSwitcherController.mSimpleUserSwitcher = userSwitcherController.shouldUseSimpleUserSwitcher();
            UserSwitcherController userSwitcherController2 = UserSwitcherController.this;
            boolean z2 = false;
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
    public final UiEventLogger mUiEventLogger;
    /* access modifiers changed from: private */
    public final Runnable mUnpauseRefreshUsers = new Runnable() {
        public void run() {
            UserSwitcherController.this.mHandler.removeCallbacks(this);
            boolean unused = UserSwitcherController.this.mPauseRefreshUsers = false;
            UserSwitcherController.this.refreshUsers(-10000);
        }
    };
    /* access modifiers changed from: private */
    public UserContextWrapper mUserContextWrapper;
    private SettingsObserver.Callback mUserIdObserverCallback = new SettingsObserver.Callback(this) {
        public void onContentChanged(@Nullable String str, @Nullable String str2) {
            if ("second_user_id".equals(str)) {
                int unused = UserSwitcherController.sSecondUser = MiuiTextUtils.parseInt(str2, -1);
            } else if ("kid_user_id".equals(str)) {
                int unused2 = UserSwitcherController.sKidSpaceUser = MiuiTextUtils.parseInt(str2, -1);
            }
        }
    };
    private SettingsObserver mUserIdSettingsObserver;
    protected final UserManager mUserManager;
    /* access modifiers changed from: private */
    public ArrayList<UserRecord> mUsers = new ArrayList<>();
    public final DetailAdapter userDetailAdapter = new DetailAdapter() {
        private final Intent USER_SETTINGS_INTENT = new Intent("android.settings.USER_SETTINGS");

        public int getMetricsCategory() {
            return R$styleable.AppCompatTheme_windowMinWidthMinor;
        }

        public Boolean getToggleState() {
            return null;
        }

        public void setToggleState(boolean z) {
        }

        public CharSequence getTitle() {
            return UserSwitcherController.this.mContext.getString(C0021R$string.quick_settings_user_title);
        }

        public View createDetailView(Context context, View view, ViewGroup viewGroup) {
            UserDetailView userDetailView;
            if (!(view instanceof UserDetailView)) {
                userDetailView = UserDetailView.inflate(context, viewGroup, false);
                UserSwitcherController userSwitcherController = UserSwitcherController.this;
                userDetailView.createAndSetAdapter(userSwitcherController, userSwitcherController.mUiEventLogger);
            } else {
                userDetailView = (UserDetailView) view;
            }
            userDetailView.refreshAdapter();
            return userDetailView;
        }

        public Intent getSettingsIntent() {
            return this.USER_SETTINGS_INTENT;
        }

        public UiEventLogger.UiEventEnum openDetailEvent() {
            return QSUserSwitcherEvent.QS_USER_DETAIL_OPEN;
        }

        public UiEventLogger.UiEventEnum closeDetailEvent() {
            return QSUserSwitcherEvent.QS_USER_DETAIL_CLOSE;
        }

        public UiEventLogger.UiEventEnum moreSettingsEvent() {
            return QSUserSwitcherEvent.QS_USER_MORE_SETTINGS;
        }
    };

    static {
        try {
            Class<?> cls = Class.forName("android.os.UserHandle");
            sMaintenanceModeId = cls.getField("MAINTENANCE_MODE_ID").getInt(cls);
        } catch (Exception unused) {
            sMaintenanceModeId = -10000;
            Log.e("UserSwitcherController", "reflect failed when get maintenance_mode_id");
        }
    }

    public static synchronized int getMaintenanceModeId() {
        int i;
        synchronized (UserSwitcherController.class) {
            i = sMaintenanceModeId;
        }
        return i;
    }

    public int getCurrentUserId() {
        return this.mUserContextWrapper.getCurrentUserId();
    }

    public boolean isOwnerUser() {
        return this.mUserContextWrapper.isOwnerUser();
    }

    public Context getContextForUser() {
        return this.mUserContextWrapper.getContextForUser();
    }

    public static synchronized int getSecondUser() {
        int i;
        synchronized (UserSwitcherController.class) {
            i = sSecondUser;
        }
        return i;
    }

    public static synchronized int getKidSpaceUser() {
        int i;
        synchronized (UserSwitcherController.class) {
            i = sKidSpaceUser;
        }
        return i;
    }

    public UserSwitcherController(Context context, KeyguardStateController keyguardStateController, Handler handler, ActivityStarter activityStarter, BroadcastDispatcher broadcastDispatcher, UiEventLogger uiEventLogger) {
        this.mUserContextWrapper = new UserContextWrapper(context, ActivityManager.getCurrentUser());
        this.mContext = context;
        this.mBroadcastDispatcher = broadcastDispatcher;
        this.mUiEventLogger = uiEventLogger;
        if (!UserManager.isGuestUserEphemeral()) {
            this.mGuestResumeSessionReceiver.register(this.mBroadcastDispatcher);
        }
        this.mKeyguardStateController = keyguardStateController;
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
        this.mBroadcastDispatcher.registerReceiver(this.mReceiver, intentFilter, (Executor) null, UserHandle.SYSTEM);
        this.mSimpleUserSwitcher = shouldUseSimpleUserSwitcher();
        this.mSecondaryUserServiceIntent = new Intent(context, SystemUISecondaryUserService.class);
        this.mContext.registerReceiverAsUser(this.mReceiver, UserHandle.SYSTEM, new IntentFilter(), "com.android.systemui.permission.SELF", (Handler) null);
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("lockscreenSimpleUserSwitcher"), true, this.mSettingsObserver);
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("add_users_when_locked"), true, this.mSettingsObserver);
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("allow_user_switching_when_system_user_locked"), true, this.mSettingsObserver);
        this.mSettingsObserver.onChange(false);
        keyguardStateController.addCallback(this.mCallback);
        listenForCallState();
        SettingsObserver settingsObserver = (SettingsObserver) Dependency.get(SettingsObserver.class);
        this.mUserIdSettingsObserver = settingsObserver;
        settingsObserver.addCallbackForSingleUser(this.mUserIdObserverCallback, 1, 0, "second_user_id", "kid_user_id");
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
                        int r9 = android.app.ActivityManager.getCurrentUser()
                        android.os.UserHandle r9 = android.os.UserHandle.of(r9)
                        int r8 = r8.getUserSwitchability(r9)
                        if (r8 != 0) goto L_0x0033
                        r8 = r4
                        goto L_0x0034
                    L_0x0033:
                        r8 = r1
                    L_0x0034:
                        java.util.Iterator r3 = r3.iterator()
                        r9 = r5
                    L_0x0039:
                        boolean r10 = r3.hasNext()
                        if (r10 == 0) goto L_0x00be
                        java.lang.Object r10 = r3.next()
                        r12 = r10
                        android.content.pm.UserInfo r12 = (android.content.pm.UserInfo) r12
                        int r10 = r12.id
                        if (r7 != r10) goto L_0x004c
                        r15 = r4
                        goto L_0x004d
                    L_0x004c:
                        r15 = r1
                    L_0x004d:
                        if (r15 == 0) goto L_0x0052
                        r19 = r12
                        goto L_0x0054
                    L_0x0052:
                        r19 = r9
                    L_0x0054:
                        if (r8 != 0) goto L_0x005c
                        if (r15 == 0) goto L_0x0059
                        goto L_0x005c
                    L_0x0059:
                        r18 = r1
                        goto L_0x005e
                    L_0x005c:
                        r18 = r4
                    L_0x005e:
                        boolean r9 = r12.isEnabled()
                        if (r9 == 0) goto L_0x00ba
                        boolean r9 = r12.isGuest()
                        if (r9 == 0) goto L_0x007d
                        com.android.systemui.statusbar.policy.UserSwitcherController$UserRecord r5 = new com.android.systemui.statusbar.policy.UserSwitcherController$UserRecord
                        r11 = 0
                        r13 = 1
                        r14 = 0
                        r16 = 0
                        r9 = r5
                        r10 = r12
                        r12 = r13
                        r13 = r15
                        r15 = r16
                        r16 = r8
                        r9.<init>(r10, r11, r12, r13, r14, r15, r16)
                        goto L_0x00ba
                    L_0x007d:
                        boolean r9 = r12.supportsSwitchToByUser()
                        if (r9 == 0) goto L_0x00ba
                        int r9 = r12.id
                        java.lang.Object r9 = r2.get(r9)
                        android.graphics.Bitmap r9 = (android.graphics.Bitmap) r9
                        if (r9 != 0) goto L_0x00ab
                        com.android.systemui.statusbar.policy.UserSwitcherController r9 = com.android.systemui.statusbar.policy.UserSwitcherController.this
                        android.os.UserManager r9 = r9.mUserManager
                        int r10 = r12.id
                        android.graphics.Bitmap r9 = r9.getUserIcon(r10)
                        if (r9 == 0) goto L_0x00ab
                        com.android.systemui.statusbar.policy.UserSwitcherController r10 = com.android.systemui.statusbar.policy.UserSwitcherController.this
                        android.content.Context r10 = r10.mContext
                        android.content.res.Resources r10 = r10.getResources()
                        int r11 = com.android.systemui.C0012R$dimen.max_avatar_size
                        int r10 = r10.getDimensionPixelSize(r11)
                        android.graphics.Bitmap r9 = android.graphics.Bitmap.createScaledBitmap(r9, r10, r10, r4)
                    L_0x00ab:
                        r13 = r9
                        com.android.systemui.statusbar.policy.UserSwitcherController$UserRecord r9 = new com.android.systemui.statusbar.policy.UserSwitcherController$UserRecord
                        r14 = 0
                        r16 = 0
                        r17 = 0
                        r11 = r9
                        r11.<init>(r12, r13, r14, r15, r16, r17, r18)
                        r6.add(r9)
                    L_0x00ba:
                        r9 = r19
                        goto L_0x0039
                    L_0x00be:
                        int r2 = r6.size()
                        if (r2 > r4) goto L_0x00c6
                        if (r5 == 0) goto L_0x00cf
                    L_0x00c6:
                        com.android.systemui.statusbar.policy.UserSwitcherController r2 = com.android.systemui.statusbar.policy.UserSwitcherController.this
                        android.content.Context r2 = r2.mContext
                        java.lang.String r3 = "HasSeenMultiUser"
                        com.android.systemui.Prefs.putBoolean(r2, r3, r4)
                    L_0x00cf:
                        com.android.systemui.statusbar.policy.UserSwitcherController r2 = com.android.systemui.statusbar.policy.UserSwitcherController.this
                        android.os.UserManager r2 = r2.mUserManager
                        android.os.UserHandle r3 = android.os.UserHandle.SYSTEM
                        java.lang.String r7 = "no_add_user"
                        boolean r2 = r2.hasBaseUserRestriction(r7, r3)
                        r2 = r2 ^ r4
                        if (r9 == 0) goto L_0x00ec
                        boolean r3 = r9.isAdmin()
                        if (r3 != 0) goto L_0x00e8
                        int r3 = r9.id
                        if (r3 != 0) goto L_0x00ec
                    L_0x00e8:
                        if (r2 == 0) goto L_0x00ec
                        r3 = r4
                        goto L_0x00ed
                    L_0x00ec:
                        r3 = r1
                    L_0x00ed:
                        if (r2 == 0) goto L_0x00f5
                        boolean r2 = r9
                        if (r2 == 0) goto L_0x00f5
                        r2 = r4
                        goto L_0x00f6
                    L_0x00f5:
                        r2 = r1
                    L_0x00f6:
                        if (r3 != 0) goto L_0x00fa
                        if (r2 == 0) goto L_0x00fe
                    L_0x00fa:
                        if (r5 != 0) goto L_0x00fe
                        r7 = r4
                        goto L_0x00ff
                    L_0x00fe:
                        r7 = r1
                    L_0x00ff:
                        if (r3 != 0) goto L_0x0103
                        if (r2 == 0) goto L_0x010e
                    L_0x0103:
                        com.android.systemui.statusbar.policy.UserSwitcherController r2 = com.android.systemui.statusbar.policy.UserSwitcherController.this
                        android.os.UserManager r2 = r2.mUserManager
                        boolean r2 = r2.canAddMoreUsers()
                        if (r2 == 0) goto L_0x010e
                        r1 = r4
                    L_0x010e:
                        boolean r2 = r9
                        r2 = r2 ^ r4
                        if (r5 != 0) goto L_0x012c
                        if (r7 == 0) goto L_0x012f
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
                        goto L_0x012f
                    L_0x012c:
                        r6.add(r5)
                    L_0x012f:
                        if (r1 == 0) goto L_0x0147
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
                    L_0x0147:
                        return r6
                    */
                    throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.policy.UserSwitcherController.AnonymousClass2.doInBackground(android.util.SparseArray[]):java.util.ArrayList");
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
        int intValue = ((Integer) DejankUtils.whitelistIpcs(new Supplier() {
            public final Object get() {
                return UserSwitcherController.this.lambda$useFullscreenUserSwitcher$0$UserSwitcherController();
            }
        })).intValue();
        if (intValue != -1) {
            return intValue != 0;
        }
        return this.mContext.getResources().getBoolean(C0010R$bool.config_enableFullscreenUserSwitcher);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$useFullscreenUserSwitcher$0 */
    public /* synthetic */ Integer lambda$useFullscreenUserSwitcher$0$UserSwitcherController() {
        return Integer.valueOf(Settings.System.getInt(this.mContext.getContentResolver(), "enable_fullscreen_user_switcher", -1));
    }

    public void switchTo(UserRecord userRecord) {
        int i;
        UserInfo userInfo;
        if (userRecord.isGuest && userRecord.info == null) {
            try {
                UserInfo createGuest = this.mUserManager.createGuest(this.mContext, this.mContext.getString(R$string.guest_nickname));
                if (createGuest != null) {
                    i = createGuest.id;
                } else {
                    return;
                }
            } catch (UserManager.UserOperationException e) {
                Log.e("UserSwitcherController", "Couldn't create guest user", e);
                return;
            }
        } else if (userRecord.isAddUser) {
            showAddUserDialog();
            return;
        } else {
            i = userRecord.info.id;
        }
        int currentUser = ActivityManager.getCurrentUser();
        if (currentUser == i) {
            if (userRecord.isGuest) {
                showExitGuestDialog(i);
            }
        } else if (!UserManager.isGuestUserEphemeral() || (userInfo = this.mUserManager.getUserInfo(currentUser)) == null || !userInfo.isGuest()) {
            switchToUserId(i);
        } else {
            showExitGuestDialog(currentUser, userRecord.resolveId());
        }
    }

    /* access modifiers changed from: protected */
    public void switchToUserId(int i) {
        try {
            pauseRefreshUsers();
            ActivityManager.getService().switchUser(i);
        } catch (RemoteException e) {
            Log.e("UserSwitcherController", "Couldn't switch user.", e);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:4:0x0008, code lost:
        r0 = r2.mUserManager.getUserInfo((r0 = r2.mLastNonGuestUser));
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void showExitGuestDialog(int r3) {
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
            boolean r1 = r0.supportsSwitchToByUser()
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
        ExitGuestDialog exitGuestDialog = new ExitGuestDialog(this.mContext, i, i2);
        this.mExitGuestDialog = exitGuestDialog;
        exitGuestDialog.show();
    }

    public void showAddUserDialog() {
        Dialog dialog = this.mAddUserDialog;
        if (dialog != null && dialog.isShowing()) {
            this.mAddUserDialog.cancel();
        }
        AddUserDialog addUserDialog = new AddUserDialog(this.mContext);
        this.mAddUserDialog = addUserDialog;
        addUserDialog.show();
    }

    /* access modifiers changed from: protected */
    public void exitGuest(int i, int i2) {
        switchToUserId(i2);
        this.mUserManager.removeUser(i);
    }

    private void listenForCallState() {
        TelephonyManager telephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        if (telephonyManager != null) {
            telephonyManager.listen(this.mPhoneStateListener, 32);
        }
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("UserSwitcherController state:");
        printWriter.println("  mLastNonGuestUser=" + this.mLastNonGuestUser);
        printWriter.print("  mUsers.size=");
        printWriter.println(this.mUsers.size());
        for (int i = 0; i < this.mUsers.size(); i++) {
            printWriter.print("    ");
            printWriter.println(this.mUsers.get(i).toString());
        }
        printWriter.println("mSimpleUserSwitcher=" + this.mSimpleUserSwitcher);
    }

    public String getCurrentUserName(Context context) {
        UserRecord userRecord;
        UserInfo userInfo;
        if (this.mUsers.isEmpty() || (userRecord = this.mUsers.get(0)) == null || (userInfo = userRecord.info) == null) {
            return null;
        }
        if (userRecord.isGuest) {
            return context.getString(R$string.guest_nickname);
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
        private final KeyguardStateController mKeyguardStateController;

        public long getItemId(int i) {
            return (long) i;
        }

        protected BaseUserAdapter(UserSwitcherController userSwitcherController) {
            this.mController = userSwitcherController;
            this.mKeyguardStateController = userSwitcherController.mKeyguardStateController;
            userSwitcherController.addAdapter(new WeakReference(this));
        }

        public int getUserCount() {
            if (!(this.mKeyguardStateController.isShowing() && this.mKeyguardStateController.isMethodSecure() && !this.mKeyguardStateController.canDismissLockScreen())) {
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
            if (!(this.mKeyguardStateController.isShowing() && this.mKeyguardStateController.isMethodSecure() && !this.mKeyguardStateController.canDismissLockScreen())) {
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
            int i;
            if (userRecord.isGuest) {
                if (userRecord.isCurrent) {
                    return context.getString(R$string.guest_exit_guest);
                }
                if (userRecord.info == null) {
                    i = R$string.guest_new_guest;
                } else {
                    i = R$string.guest_nickname;
                }
                return context.getString(i);
            } else if (userRecord.isAddUser) {
                return context.getString(C0021R$string.user_add_user);
            } else {
                return userRecord.info.name;
            }
        }

        protected static ColorFilter getDisabledUserAvatarColorFilter() {
            ColorMatrix colorMatrix = new ColorMatrix();
            colorMatrix.setSaturation(0.0f);
            return new ColorMatrixColorFilter(colorMatrix);
        }

        protected static Drawable getIconDrawable(Context context, UserRecord userRecord) {
            int i;
            if (userRecord.isAddUser) {
                i = C0013R$drawable.ic_add_circle;
            } else if (userRecord.isGuest) {
                i = C0013R$drawable.ic_avatar_guest_user;
            } else {
                i = C0013R$drawable.ic_avatar_user;
            }
            return context.getDrawable(i);
        }

        public void refresh() {
            this.mController.refreshUsers(-10000);
        }
    }

    /* access modifiers changed from: private */
    public void checkIfAddUserDisallowedByAdminOnly(UserRecord userRecord) {
        RestrictedLockUtils.EnforcedAdmin checkIfRestrictionEnforced = RestrictedLockUtilsInternal.checkIfRestrictionEnforced(this.mContext, "no_add_user", ActivityManager.getCurrentUser());
        if (checkIfRestrictionEnforced == null || RestrictedLockUtilsInternal.hasBaseUserRestriction(this.mContext, "no_add_user", ActivityManager.getCurrentUser())) {
            userRecord.isDisabledByAdmin = false;
            userRecord.enforcedAdmin = null;
            return;
        }
        userRecord.isDisabledByAdmin = true;
        userRecord.enforcedAdmin = checkIfRestrictionEnforced;
    }

    /* access modifiers changed from: private */
    public boolean shouldUseSimpleUserSwitcher() {
        return Settings.Global.getInt(this.mContext.getContentResolver(), "lockscreenSimpleUserSwitcher", this.mContext.getResources().getBoolean(17891459) ? 1 : 0) != 0;
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
            setTitle(C0021R$string.guest_exit_guest_dialog_title);
            setMessage(context.getString(C0021R$string.guest_exit_guest_dialog_message));
            setButton(-2, context.getString(17039360), this);
            setButton(-1, context.getString(C0021R$string.guest_exit_guest_dialog_remove), this);
            SystemUIDialog.setWindowOnTop(this);
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
            setTitle(C0021R$string.user_add_user_title);
            setMessage(context.getString(C0021R$string.user_add_user_message_short));
            setButton(-2, context.getString(17039360), this);
            setButton(-1, context.getString(17039370), this);
            SystemUIDialog.setWindowOnTop(this);
        }

        public void onClick(DialogInterface dialogInterface, int i) {
            if (i == -2) {
                cancel();
                return;
            }
            dismiss();
            if (!ActivityManager.isUserAMonkey()) {
                UserSwitcherController userSwitcherController = UserSwitcherController.this;
                UserInfo createUser = userSwitcherController.mUserManager.createUser(userSwitcherController.mContext.getString(C0021R$string.user_new_user_name), 0);
                if (createUser != null) {
                    int i2 = createUser.id;
                    UserSwitcherController.this.mUserManager.setUserIcon(i2, UserIcons.convertToBitmap(UserIcons.getDefaultUserIcon(UserSwitcherController.this.mContext.getResources(), i2, false)));
                    UserSwitcherController.this.switchToUserId(i2);
                }
            }
        }
    }
}
