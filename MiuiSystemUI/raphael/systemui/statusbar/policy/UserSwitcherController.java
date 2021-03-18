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
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0021R$string;
import com.android.systemui.DejankUtils;
import com.android.systemui.Dependency;
import com.android.systemui.Dumpable;
import com.android.systemui.GuestResumeSessionReceiver;
import com.android.systemui.Prefs;
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
import java.util.List;
import java.util.function.Supplier;
import org.jetbrains.annotations.Nullable;

public class UserSwitcherController implements Dumpable {
    private static int sKidSpaceUser;
    private static int sMaintenanceModeId;
    private static int sSecondUser;
    private final ActivityStarter mActivityStarter;
    private final ArrayList<WeakReference<BaseUserAdapter>> mAdapters = new ArrayList<>();
    private Dialog mAddUserDialog;
    private boolean mAddUsersWhenLocked;
    private final BroadcastDispatcher mBroadcastDispatcher;
    private final KeyguardStateController.Callback mCallback = new KeyguardStateController.Callback() {
        /* class com.android.systemui.statusbar.policy.UserSwitcherController.AnonymousClass8 */

        @Override // com.android.systemui.statusbar.policy.KeyguardStateController.Callback
        public void onKeyguardShowingChanged() {
            if (!UserSwitcherController.this.mKeyguardStateController.isShowing()) {
                UserSwitcherController userSwitcherController = UserSwitcherController.this;
                userSwitcherController.mHandler.post(new Runnable() {
                    /* class com.android.systemui.statusbar.policy.$$Lambda$UserSwitcherController$8$A3r6icx46POmpjCAt7rArXBXF0c */

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
    private Dialog mExitGuestDialog;
    private SparseBooleanArray mForcePictureLoadForUserId = new SparseBooleanArray(2);
    private final GuestResumeSessionReceiver mGuestResumeSessionReceiver = new GuestResumeSessionReceiver();
    protected final Handler mHandler;
    private final KeyguardStateController mKeyguardStateController;
    private int mLastNonGuestUser = 0;
    private boolean mPauseRefreshUsers;
    private final PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        /* class com.android.systemui.statusbar.policy.UserSwitcherController.AnonymousClass3 */
        private int mCallState;

        public void onCallStateChanged(int i, String str) {
            if (this.mCallState != i) {
                this.mCallState = i;
                UserSwitcherController.this.refreshUsers(-10000);
            }
        }
    };
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        /* class com.android.systemui.statusbar.policy.UserSwitcherController.AnonymousClass4 */

        public void onReceive(Context context, Intent intent) {
            boolean z = true;
            int i = -10000;
            if ("android.intent.action.USER_SWITCHED".equals(intent.getAction())) {
                if (UserSwitcherController.this.mExitGuestDialog != null && UserSwitcherController.this.mExitGuestDialog.isShowing()) {
                    UserSwitcherController.this.mExitGuestDialog.cancel();
                    UserSwitcherController.this.mExitGuestDialog = null;
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
                            UserSwitcherController.this.mLastNonGuestUser = userRecord.info.id;
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
                    UserSwitcherController.this.mSecondaryUser = -10000;
                }
                if (!(userInfo == null || userInfo.id == 0)) {
                    context.startServiceAsUser(UserSwitcherController.this.mSecondaryUserServiceIntent, UserHandle.of(userInfo.id));
                    UserSwitcherController.this.mSecondaryUser = userInfo.id;
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
    private int mSecondaryUser = -10000;
    private Intent mSecondaryUserServiceIntent;
    private final ContentObserver mSettingsObserver = new ContentObserver(new Handler()) {
        /* class com.android.systemui.statusbar.policy.UserSwitcherController.AnonymousClass6 */

        public void onChange(boolean z) {
            UserSwitcherController userSwitcherController = UserSwitcherController.this;
            userSwitcherController.mSimpleUserSwitcher = userSwitcherController.shouldUseSimpleUserSwitcher();
            UserSwitcherController userSwitcherController2 = UserSwitcherController.this;
            boolean z2 = false;
            if (Settings.Global.getInt(userSwitcherController2.mContext.getContentResolver(), "add_users_when_locked", 0) != 0) {
                z2 = true;
            }
            userSwitcherController2.mAddUsersWhenLocked = z2;
            UserSwitcherController.this.refreshUsers(-10000);
        }
    };
    private boolean mSimpleUserSwitcher;
    private final UiEventLogger mUiEventLogger;
    private final Runnable mUnpauseRefreshUsers = new Runnable() {
        /* class com.android.systemui.statusbar.policy.UserSwitcherController.AnonymousClass5 */

        public void run() {
            UserSwitcherController.this.mHandler.removeCallbacks(this);
            UserSwitcherController.this.mPauseRefreshUsers = false;
            UserSwitcherController.this.refreshUsers(-10000);
        }
    };
    private UserContextWrapper mUserContextWrapper;
    private SettingsObserver.Callback mUserIdObserverCallback = new SettingsObserver.Callback(this) {
        /* class com.android.systemui.statusbar.policy.UserSwitcherController.AnonymousClass1 */

        @Override // com.miui.systemui.SettingsObserver.Callback
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
    private ArrayList<UserRecord> mUsers = new ArrayList<>();
    public final DetailAdapter userDetailAdapter = new DetailAdapter() {
        /* class com.android.systemui.statusbar.policy.UserSwitcherController.AnonymousClass7 */
        private final Intent USER_SETTINGS_INTENT = new Intent("android.settings.USER_SETTINGS");

        @Override // com.android.systemui.plugins.qs.DetailAdapter
        public int getMetricsCategory() {
            return R$styleable.AppCompatTheme_windowMinWidthMinor;
        }

        @Override // com.android.systemui.plugins.qs.DetailAdapter
        public Boolean getToggleState() {
            return null;
        }

        @Override // com.android.systemui.plugins.qs.DetailAdapter
        public void setToggleState(boolean z) {
        }

        @Override // com.android.systemui.plugins.qs.DetailAdapter
        public CharSequence getTitle() {
            return UserSwitcherController.this.mContext.getString(C0021R$string.quick_settings_user_title);
        }

        @Override // com.android.systemui.plugins.qs.DetailAdapter
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

        @Override // com.android.systemui.plugins.qs.DetailAdapter
        public Intent getSettingsIntent() {
            return this.USER_SETTINGS_INTENT;
        }

        @Override // com.android.systemui.plugins.qs.DetailAdapter
        public UiEventLogger.UiEventEnum openDetailEvent() {
            return QSUserSwitcherEvent.QS_USER_DETAIL_OPEN;
        }

        @Override // com.android.systemui.plugins.qs.DetailAdapter
        public UiEventLogger.UiEventEnum closeDetailEvent() {
            return QSUserSwitcherEvent.QS_USER_DETAIL_CLOSE;
        }

        @Override // com.android.systemui.plugins.qs.DetailAdapter
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
        this.mBroadcastDispatcher.registerReceiver(this.mReceiver, intentFilter, null, UserHandle.SYSTEM);
        this.mSimpleUserSwitcher = shouldUseSimpleUserSwitcher();
        this.mSecondaryUserServiceIntent = new Intent(context, SystemUISecondaryUserService.class);
        this.mContext.registerReceiverAsUser(this.mReceiver, UserHandle.SYSTEM, new IntentFilter(), "com.android.systemui.permission.SELF", null);
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("lockscreenSimpleUserSwitcher"), true, this.mSettingsObserver);
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("add_users_when_locked"), true, this.mSettingsObserver);
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("allow_user_switching_when_system_user_locked"), true, this.mSettingsObserver);
        this.mSettingsObserver.onChange(false);
        keyguardStateController.addCallback(this.mCallback);
        listenForCallState();
        SettingsObserver settingsObserver = (SettingsObserver) Dependency.get(SettingsObserver.class);
        this.mUserIdSettingsObserver = settingsObserver;
        settingsObserver.addCallbackForUser(this.mUserIdObserverCallback, 1, 0, "second_user_id", "kid_user_id");
        refreshUsers(-10000);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void refreshUsers(int i) {
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
                /* class com.android.systemui.statusbar.policy.UserSwitcherController.AnonymousClass2 */

                /* access modifiers changed from: protected */
                public ArrayList<UserRecord> doInBackground(SparseArray<Bitmap>... sparseArrayArr) {
                    boolean z = false;
                    SparseArray<Bitmap> sparseArray = sparseArrayArr[0];
                    List<UserInfo> users = UserSwitcherController.this.mUserManager.getUsers(true);
                    UserRecord userRecord = null;
                    if (users == null) {
                        return null;
                    }
                    ArrayList<UserRecord> arrayList = new ArrayList<>(users.size());
                    int currentUser = ActivityManager.getCurrentUser();
                    boolean z2 = UserSwitcherController.this.mUserManager.getUserSwitchability(UserHandle.of(ActivityManager.getCurrentUser())) == 0;
                    UserInfo userInfo = null;
                    for (UserInfo userInfo2 : users) {
                        boolean z3 = currentUser == userInfo2.id;
                        UserInfo userInfo3 = z3 ? userInfo2 : userInfo;
                        boolean z4 = z2 || z3;
                        if (userInfo2.isEnabled()) {
                            if (userInfo2.isGuest()) {
                                userRecord = new UserRecord(userInfo2, null, true, z3, false, false, z2);
                            } else if (userInfo2.supportsSwitchToByUser()) {
                                Bitmap bitmap = sparseArray.get(userInfo2.id);
                                if (bitmap == null && (bitmap = UserSwitcherController.this.mUserManager.getUserIcon(userInfo2.id)) != null) {
                                    int dimensionPixelSize = UserSwitcherController.this.mContext.getResources().getDimensionPixelSize(C0012R$dimen.max_avatar_size);
                                    bitmap = Bitmap.createScaledBitmap(bitmap, dimensionPixelSize, dimensionPixelSize, true);
                                }
                                arrayList.add(new UserRecord(userInfo2, bitmap, false, z3, false, false, z4));
                            }
                        }
                        userInfo = userInfo3;
                    }
                    if (arrayList.size() > 1 || userRecord != null) {
                        Prefs.putBoolean(UserSwitcherController.this.mContext, "HasSeenMultiUser", true);
                    }
                    boolean z5 = !UserSwitcherController.this.mUserManager.hasBaseUserRestriction("no_add_user", UserHandle.SYSTEM);
                    boolean z6 = userInfo != null && (userInfo.isAdmin() || userInfo.id == 0) && z5;
                    boolean z7 = z5 && z2;
                    boolean z8 = (z6 || z7) && userRecord == null;
                    if ((z6 || z7) && UserSwitcherController.this.mUserManager.canAddMoreUsers()) {
                        z = true;
                    }
                    boolean z9 = !z2;
                    if (userRecord != null) {
                        arrayList.add(userRecord);
                    } else if (z8) {
                        UserRecord userRecord2 = new UserRecord(null, null, true, false, false, z9, z2);
                        UserSwitcherController.this.checkIfAddUserDisallowedByAdminOnly(userRecord2);
                        arrayList.add(userRecord2);
                    }
                    if (z) {
                        UserRecord userRecord3 = new UserRecord(null, null, false, false, true, z9, z2);
                        UserSwitcherController.this.checkIfAddUserDisallowedByAdminOnly(userRecord3);
                        arrayList.add(userRecord3);
                    }
                    return arrayList;
                }

                /* access modifiers changed from: protected */
                public void onPostExecute(ArrayList<UserRecord> arrayList) {
                    if (arrayList != null) {
                        UserSwitcherController.this.mUsers = arrayList;
                        UserSwitcherController.this.notifyAdapters();
                    }
                }
            }.execute(sparseArray);
        }
    }

    private void pauseRefreshUsers() {
        if (!this.mPauseRefreshUsers) {
            this.mHandler.postDelayed(this.mUnpauseRefreshUsers, 3000);
            this.mPauseRefreshUsers = true;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    public void notifyAdapters() {
        for (int size = this.mAdapters.size() - 1; size >= 0; size--) {
            BaseUserAdapter baseUserAdapter = this.mAdapters.get(size).get();
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
            /* class com.android.systemui.statusbar.policy.$$Lambda$UserSwitcherController$gJeQLk7uUPWe8l2LAzLToqBeJo */

            @Override // java.util.function.Supplier
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

    private void showExitGuestDialog(int i) {
        int i2;
        UserInfo userInfo;
        showExitGuestDialog(i, (!this.mResumeUserOnGuestLogout || (i2 = this.mLastNonGuestUser) == 0 || (userInfo = this.mUserManager.getUserInfo(i2)) == null || !userInfo.isEnabled() || !userInfo.supportsSwitchToByUser()) ? 0 : userInfo.id);
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

    @Override // com.android.systemui.Dumpable
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
            userSwitcherController.addAdapter(new WeakReference<>(this));
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
    /* access modifiers changed from: public */
    private void checkIfAddUserDisallowedByAdminOnly(UserRecord userRecord) {
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
    /* access modifiers changed from: public */
    private boolean shouldUseSimpleUserSwitcher() {
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

    /* access modifiers changed from: private */
    public final class ExitGuestDialog extends SystemUIDialog implements DialogInterface.OnClickListener {
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

    /* access modifiers changed from: private */
    public final class AddUserDialog extends SystemUIDialog implements DialogInterface.OnClickListener {
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
