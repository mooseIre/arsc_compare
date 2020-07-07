package com.android.systemui.recents;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ActivityManager;
import android.app.ActivityManagerCompat;
import android.app.ActivityOptions;
import android.app.KeyguardManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextCompat;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.UserHandle;
import android.provider.MiuiSettings;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Log;
import android.util.Slog;
import android.view.IWindowManager;
import android.view.IWindowManagerCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;
import android.widget.Toast;
import androidx.preference.PreferenceManager;
import com.android.internal.os.BackgroundThread;
import com.android.internal.policy.DockedDividerUtils;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.Application;
import com.android.systemui.Constants;
import com.android.systemui.Dependency;
import com.android.systemui.HapticFeedBackImpl;
import com.android.systemui.Util;
import com.android.systemui.assist.AssistManager;
import com.android.systemui.fsgesture.GestureStubView;
import com.android.systemui.miui.ActivityObserver;
import com.android.systemui.miui.statusbar.CloudDataHelper;
import com.android.systemui.plugins.R;
import com.android.systemui.recents.events.RecentsEventBus;
import com.android.systemui.recents.events.activity.ActivitySetDummyTranslucentEvent;
import com.android.systemui.recents.events.activity.AnimFirstTaskViewAlphaEvent;
import com.android.systemui.recents.events.activity.DockedFirstAnimationFrameEvent;
import com.android.systemui.recents.events.activity.DockedTopTaskEvent;
import com.android.systemui.recents.events.activity.EnterRecentsWindowFirstAnimationFrameEvent;
import com.android.systemui.recents.events.activity.FsGestureEnterRecentsCompleteEvent;
import com.android.systemui.recents.events.activity.FsGestureEnterRecentsEvent;
import com.android.systemui.recents.events.activity.FsGestureEnterRecentsZoomEvent;
import com.android.systemui.recents.events.activity.FsGestureLaunchTargetTaskViewRectEvent;
import com.android.systemui.recents.events.activity.FsGesturePreloadRecentsEvent;
import com.android.systemui.recents.events.activity.FsGestureShowFirstCardEvent;
import com.android.systemui.recents.events.activity.FsGestureShowStateEvent;
import com.android.systemui.recents.events.activity.FsGestureSlideInEvent;
import com.android.systemui.recents.events.activity.FsGestureSlideOutEvent;
import com.android.systemui.recents.events.activity.HideRecentsEvent;
import com.android.systemui.recents.events.activity.IterateRecentsEvent;
import com.android.systemui.recents.events.activity.LaunchNextTaskRequestEvent;
import com.android.systemui.recents.events.activity.MultiWindowStateChangedEvent;
import com.android.systemui.recents.events.activity.RecentsActivityStartingEvent;
import com.android.systemui.recents.events.activity.RotationChangedEvent;
import com.android.systemui.recents.events.activity.ThumbnailBlurPkgsChangedEvent;
import com.android.systemui.recents.events.activity.ToggleRecentsEvent;
import com.android.systemui.recents.events.activity.UndockingTaskEvent;
import com.android.systemui.recents.events.component.HideNavStubForBackWindow;
import com.android.systemui.recents.events.component.RecentsVisibilityChangedEvent;
import com.android.systemui.recents.events.ui.CleanInRecentsEvents;
import com.android.systemui.recents.events.ui.DismissAllTaskViewsEndEvent;
import com.android.systemui.recents.events.ui.DismissAllTaskViewsEvent;
import com.android.systemui.recents.events.ui.DraggingInRecentsEndedEvent;
import com.android.systemui.recents.events.ui.DraggingInRecentsEvent;
import com.android.systemui.recents.events.ui.RecentsDrawnEvent;
import com.android.systemui.recents.misc.DozeTrigger;
import com.android.systemui.recents.misc.ForegroundThread;
import com.android.systemui.recents.misc.ProcessManagerHelper;
import com.android.systemui.recents.misc.RecentsPushEventHelper;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.recents.misc.Utilities;
import com.android.systemui.recents.model.MutableBoolean;
import com.android.systemui.recents.model.RecentsTaskLoadPlan;
import com.android.systemui.recents.model.RecentsTaskLoader;
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.model.TaskStack;
import com.android.systemui.recents.views.TaskStackLayoutAlgorithm;
import com.android.systemui.recents.views.TaskStackView;
import com.android.systemui.recents.views.TaskViewHeader;
import com.android.systemui.recents.views.TaskViewTransform;
import com.android.systemui.stackdivider.Divider;
import com.android.systemui.statusbar.phone.NavStubView;
import com.android.systemui.statusbar.phone.StatusBar;
import com.miui.internal.transition.IMiuiGestureControlHelper;
import com.miui.internal.transition.MiuiAppTransitionAnimationSpec;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import miui.process.ProcessConfig;
import miui.process.ProcessManager;
import miui.securityspace.CrossUserUtils;
import miui.util.HardwareInfo;
import miui.widget.CircleProgressBar;

public abstract class BaseRecentsImpl {
    /* access modifiers changed from: private */
    public static String CLOUD_DATA_MODULE_NAME_THUMBNAIL_BLUR = "RecentsThumbnailBlur";
    /* access modifiers changed from: private */
    public static String PREF_KEY_INVALID_THUMBNAIL_BLUR_PKG_WHITE_SET = "invalid_thumbnail_blur_pkgs_white_set";
    private static Toast mScreeningToast = null;
    public static int mTaskBarHeight = 0;
    protected static RecentsTaskLoadPlan sInstanceLoadPlan = null;
    public static boolean sOneKeyCleaning = false;
    private boolean isShowing = false;
    private ActivityObserver.ActivityObserverCallback mActivityStateObserver = new ActivityObserver.ActivityObserverCallback() {
        public void activityResumed(Intent intent) {
            if (intent != null && intent.getComponent() != null) {
                BaseRecentsImpl.this.onResumed(intent.getComponent().getClassName());
            }
        }
    };
    private ContentObserver mAppSwitchAnimChangeListener = new ContentObserver(new Handler(Looper.getMainLooper())) {
        public void onChange(boolean z) {
            boolean z2 = false;
            if (Settings.Global.getInt(BaseRecentsImpl.this.mContext.getContentResolver(), "show_gesture_appswitch_feature", 0) == 0) {
                z2 = true;
            }
            if (BaseRecentsImpl.this.mGestureStubLeft != null) {
                BaseRecentsImpl.this.mGestureStubLeft.disableQuickSwitch(z2);
            }
            if (BaseRecentsImpl.this.mGestureStubRight != null) {
                BaseRecentsImpl.this.mGestureStubRight.disableQuickSwitch(z2);
            }
        }
    };
    private ContentObserver mAssistContentObserver;
    /* access modifiers changed from: private */
    public AssistManager mAssistManager;
    private ContentObserver mCastModeObserver;
    private ContentObserver mCloudDataObserver = new ContentObserver(this.mHandler) {
        public void onChange(boolean z) {
            if (BaseRecentsImpl.this.mHasNavigationBar || Constants.SUPPORT_LAB_GESTURE) {
                BaseRecentsImpl.this.readCloudDataForFsg();
            }
            BaseRecentsImpl.this.readCloudDataForThumbnailBlur();
        }
    };
    protected Context mContext;
    /* access modifiers changed from: private */
    public boolean mDisabledByDriveMode;
    boolean mDraggingInRecents;
    private ContentObserver mDriveModeObserver = new ContentObserver((Handler) Dependency.get(Dependency.MAIN_HANDLER)) {
        public void onChange(boolean z) {
            BaseRecentsImpl baseRecentsImpl = BaseRecentsImpl.this;
            boolean z2 = false;
            if (Settings.System.getInt(baseRecentsImpl.mContext.getContentResolver(), "drive_mode_drive_mode", 0) == 1) {
                z2 = true;
            }
            boolean unused = baseRecentsImpl.mDisabledByDriveMode = z2;
            BaseRecentsImpl.this.updateFsgWindowState();
        }
    };
    protected TaskStackView mDummyStackView;
    private ContentObserver mElderlyModeObserver = new ContentObserver(new Handler(Looper.getMainLooper())) {
        public void onChange(boolean z) {
            if (!BaseRecentsImpl.this.mIsInAnotherPro) {
                boolean z2 = MiuiSettings.System.getBoolean(BaseRecentsImpl.this.mContext.getContentResolver(), "elderly_mode", false);
                boolean z3 = MiuiSettings.Global.getBoolean(BaseRecentsImpl.this.mContext.getContentResolver(), "force_fsg_nav_bar");
                if (z2 && z3) {
                    MiuiSettings.Global.putBoolean(BaseRecentsImpl.this.mContext.getContentResolver(), "force_fsg_nav_bar", false);
                    BaseRecentsImpl.this.updateFsgWindowState();
                }
            }
        }
    };
    DozeTrigger mFastAltTabTrigger = new DozeTrigger(225, new Runnable() {
        public void run() {
            BaseRecentsImpl baseRecentsImpl = BaseRecentsImpl.this;
            baseRecentsImpl.showRecents(baseRecentsImpl.mTriggeredFromAltTab, false, true, false, false, -1, false);
        }
    });
    private ContentObserver mForceImmersiveNavBarListener = new ContentObserver(new Handler(Looper.getMainLooper())) {
        public void onChange(boolean z) {
            if (!BaseRecentsImpl.this.mIsInAnotherPro) {
                BaseRecentsImpl.this.updateFsgWindowState();
            }
        }
    };
    /* access modifiers changed from: private */
    public int mFsgBackState = 0;
    private BroadcastReceiver mFsgReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("com.android.systemui.fsgesture".equals(intent.getAction())) {
                BaseRecentsImpl.this.updateFsgWindowVisibilityState(intent.getBooleanExtra("isEnter", false), intent.getStringExtra("typeFrom"));
            } else if ("android.intent.action.USER_SWITCHED".equals(intent.getAction())) {
                Log.d("RecentsImpl", "registerMiuiGestureControlHelper: user switched.");
                BaseRecentsImpl.this.registerMiuiGestureControlHelper();
            }
        }
    };
    /* access modifiers changed from: private */
    public long mGestureAnimationStartTime;
    private IMiuiGestureControlHelper mGestureControlHelper;
    /* access modifiers changed from: private */
    public GestureStubView mGestureStubLeft;
    /* access modifiers changed from: private */
    public GestureStubView mGestureStubRight;
    protected Handler mHandler;
    /* access modifiers changed from: private */
    public boolean mHasNavigationBar;
    private boolean mHasRegistedInput = false;
    TaskViewHeader mHeaderBar;
    final Object mHeaderBarLock = new Object();
    /* access modifiers changed from: private */
    public String mHotZoneChangeActListStr = "";
    /* access modifiers changed from: private */
    public final IWindowManager mIWindowManager;
    /* access modifiers changed from: private */
    public int mInputMethodHeight;
    private BroadcastReceiver mInputMethodVisibleHeightChangeReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("miui.intent.action.INPUT_METHOD_VISIBLE_HEIGHT_CHANGED".equals(intent.getAction()) && BaseRecentsImpl.this.mGestureStubLeft != null && BaseRecentsImpl.this.mGestureStubRight != null && !"lithium".equals(Build.DEVICE)) {
                int intExtra = intent.getIntExtra("miui.intent.extra.input_method_visible_height", -1);
                if (BaseRecentsImpl.this.mInputMethodHeight <= 0 || intExtra <= 0) {
                    int unused = BaseRecentsImpl.this.mInputMethodHeight = intExtra;
                    if (intExtra != -1 && BaseRecentsImpl.this.mGestureStubLeft.getVisibility() == 0) {
                        if (BaseRecentsImpl.this.mInputMethodHeight > 0) {
                            BaseRecentsImpl.this.sendChangeBackGestureSizeIsNeeded();
                        } else if (BaseRecentsImpl.this.mInputMethodHeight == 0 && BaseRecentsImpl.this.mFsgBackState != 2) {
                            BaseRecentsImpl.this.sendResetBackGestureSizeIsNeeded();
                        }
                    }
                }
            }
        }
    };
    public Set<String> mInvalidThumbnailBlurPkgWhiteSet = new HashSet();
    /* access modifiers changed from: private */
    public Boolean mIsChangedScreeningPkgLockState;
    private boolean mIsHideRecentsViewByFsGesture = false;
    /* access modifiers changed from: private */
    public boolean mIsInAnotherPro = false;
    private boolean mIsSizeReset;
    /* access modifiers changed from: private */
    public boolean mIsStartRecent = false;
    /* access modifiers changed from: private */
    public boolean mIsSuperPowerMode;
    protected KeyguardManager mKM;
    private String mLastResumedClassName;
    protected long mLastToggleTime;
    boolean mLaunchedWhileDocking;
    private String[] mLocalCtrlActs = {"com.android.systemui.fsgesture.HomeDemoAct", "com.android.systemui.fsgesture.DemoFinishAct", "com.android.systemui.fsgesture.DrawerDemoAct", "com.android.systemui.fsgesture.FsGestureBackDemoActivity", "com.android.provision.activities.CongratulationActivity"};
    int mNavBarHeight;
    int mNavBarWidth;
    /* access modifiers changed from: private */
    public NavStubView mNavStubView;
    /* access modifiers changed from: private */
    public String mNoBackActListStr = "";
    /* access modifiers changed from: private */
    public String mNoBackAndHomeActListStr = "";
    /* access modifiers changed from: private */
    public String mNoHomeActListStr = "";
    private Runnable mReadCloudForThumbnailBlurRunnable = new Runnable() {
        public void run() {
            final List cloudDataList = MiuiSettings.SettingsCloudData.getCloudDataList(BaseRecentsImpl.this.mContext.getContentResolver(), BaseRecentsImpl.CLOUD_DATA_MODULE_NAME_THUMBNAIL_BLUR);
            BaseRecentsImpl.this.mHandler.post(new Runnable() {
                public void run() {
                    List<MiuiSettings.SettingsCloudData.CloudData> list = cloudDataList;
                    if (list != null) {
                        for (MiuiSettings.SettingsCloudData.CloudData string : list) {
                            String string2 = string.getString("pkg", "");
                            if (!TextUtils.isEmpty(string2) && !BaseRecentsImpl.this.mThumbnailBlurPkgWhiteSet.contains(string2)) {
                                BaseRecentsImpl.this.mThumbnailBlurPkgWhiteSet.add(string2);
                            }
                        }
                    }
                    BaseRecentsImpl.this.updateThumbnailBlurSettings();
                }
            });
        }
    };
    private Runnable mReadCloudRunnable = new Runnable() {
        public void run() {
            final String cloudDataString = MiuiSettings.SettingsCloudData.getCloudDataString(BaseRecentsImpl.this.mContext.getContentResolver(), "ykrq", "no_back_gesture_only", "");
            final String cloudDataString2 = MiuiSettings.SettingsCloudData.getCloudDataString(BaseRecentsImpl.this.mContext.getContentResolver(), "ykrq", "no_home_gesture_only", "");
            final String cloudDataString3 = MiuiSettings.SettingsCloudData.getCloudDataString(BaseRecentsImpl.this.mContext.getContentResolver(), "ykrq", "no_back_and_home", "");
            final String cloudDataString4 = MiuiSettings.SettingsCloudData.getCloudDataString(BaseRecentsImpl.this.mContext.getContentResolver(), "ykrq", "hot_zone_change", "");
            BaseRecentsImpl.this.mHandler.post(new Runnable() {
                public void run() {
                    String unused = BaseRecentsImpl.this.mNoBackActListStr = cloudDataString;
                    if (TextUtils.isEmpty(BaseRecentsImpl.this.mNoBackActListStr)) {
                        String unused2 = BaseRecentsImpl.this.mNoBackActListStr = "com.miui.home.launcher.Launcher:com.miui.personalassistant.fake.FakeStartActivity:com.miui.personalassistant.fake.FakeEndActivity:com.miui.superpower.SuperPowerLauncherActivity";
                    }
                    String unused3 = BaseRecentsImpl.this.mNoHomeActListStr = cloudDataString2;
                    String unused4 = BaseRecentsImpl.this.mNoBackAndHomeActListStr = cloudDataString3;
                    String unused5 = BaseRecentsImpl.this.mHotZoneChangeActListStr = cloudDataString4;
                    BaseRecentsImpl.access$884(BaseRecentsImpl.this, "com.android.systemui.fsgesture.DemoIntroduceAct:com.android.systemui.sliderpanel.SliderPanelActivity:com.miui.miservice.main.update.UpdateGuideActivity:com.miui.miservice.main.update.UpdateDetailActivity:com.xiaomi.market.ui.OtaRecommendActivity:com.miui.miservice.main.update.UpdateEndActivity:com.miui.superpower.SuperPowerProgressActivity");
                }
            });
        }
    };
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.USER_SWITCHED".equals(intent.getAction())) {
                if (BaseRecentsImpl.this.mHasNavigationBar || Constants.SUPPORT_LAB_GESTURE) {
                    if (UserHandle.getUserId(Process.myUid()) == intent.getIntExtra("android.intent.extra.user_handle", -1)) {
                        boolean unused = BaseRecentsImpl.this.mIsInAnotherPro = false;
                        if (MiuiSettings.Global.getBoolean(BaseRecentsImpl.this.mContext.getContentResolver(), "force_fsg_nav_bar") && !BaseRecentsImpl.this.mDisabledByDriveMode) {
                            try {
                                if (BaseRecentsImpl.this.mNavStubView == null) {
                                    Log.d("RecentsImpl", "navstubview will be added: mReceiver Intent.ACTION_USER_SWITCHED userid: " + UserHandle.getUserId(Process.myUid()));
                                    BaseRecentsImpl.this.createAndAddNavStubView();
                                }
                                BaseRecentsImpl.this.addBackStubWindow();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        boolean unused2 = BaseRecentsImpl.this.mIsInAnotherPro = true;
                        try {
                            if (BaseRecentsImpl.this.mNavStubView != null) {
                                Log.d("RecentsImpl", "navstubview will be removed: mReceiver Intent.ACTION_USER_SWITCHED userid: " + UserHandle.getUserId(Process.myUid()));
                                BaseRecentsImpl.this.mWindowManager.removeView(BaseRecentsImpl.this.mNavStubView);
                                NavStubView unused3 = BaseRecentsImpl.this.mNavStubView = null;
                            }
                            BaseRecentsImpl.this.clearBackStubWindow();
                        } catch (Exception e2) {
                            e2.printStackTrace();
                        }
                    }
                }
            } else if ("android.intent.action.USER_PRESENT".equals(intent.getAction())) {
                BaseRecentsImpl.this.adaptToTopActivity();
            }
        }
    };
    RecentsReceiver mRecentsReceiver = new RecentsReceiver();
    /* access modifiers changed from: private */
    public boolean mRecentsVisible;
    /* access modifiers changed from: private */
    public int mScreenWidth;
    /* access modifiers changed from: private */
    public String mScreeningPkg;
    int mStatusBarHeight;
    private ContentObserver mSuperSavePowerObserver = new ContentObserver(this.mHandler) {
        public void onChange(boolean z) {
            BaseRecentsImpl baseRecentsImpl = BaseRecentsImpl.this;
            boolean unused = baseRecentsImpl.mIsSuperPowerMode = MiuiSettings.System.isSuperSaveModeOpen(baseRecentsImpl.mContext, UserHandle.myUserId());
            if (BaseRecentsImpl.this.mNavStubView != null) {
                BaseRecentsImpl.this.mNavStubView.setIsSuperPowerMode(BaseRecentsImpl.this.mIsSuperPowerMode);
            }
        }
    };
    Rect mTaskStackBounds = new Rect();
    SystemServicesProxy.TaskStackListener mTaskStackListener;
    protected Bitmap mThumbTransitionBitmapCache;
    private ContentObserver mThumbnailBlurObserver;
    public Set<String> mThumbnailBlurPkgWhiteSet = new HashSet();
    public HashSet<String> mThumbnailBlurSet = new HashSet<>();
    TaskViewTransform mTmpTransform = new TaskViewTransform();
    protected boolean mTriggeredFromAltTab;
    /* access modifiers changed from: private */
    public final WindowManager mWindowManager;

    public void cancelPreloadingRecents() {
    }

    static /* synthetic */ String access$884(BaseRecentsImpl baseRecentsImpl, Object obj) {
        String str = baseRecentsImpl.mNoBackAndHomeActListStr + obj;
        baseRecentsImpl.mNoBackAndHomeActListStr = str;
        return str;
    }

    public BaseRecentsImpl(Context context) {
        this.mContext = context;
        this.mHandler = new H();
        this.mKM = (KeyguardManager) context.getSystemService("keyguard");
        ForegroundThread.get();
        SystemServicesProxy systemServices = Recents.getSystemServices();
        this.mTaskStackListener = systemServices.getTaskStackListener();
        systemServices.registerTaskStackListener(this.mTaskStackListener);
        LayoutInflater from = LayoutInflater.from(this.mContext);
        this.mDummyStackView = new TaskStackView(this.mContext);
        this.mHeaderBar = (TaskViewHeader) from.inflate(R.layout.recents_task_view_header, (ViewGroup) null, false);
        reloadResources();
        systemServices.registerMiuiTaskResizeList(this.mContext);
        this.mWindowManager = (WindowManager) this.mContext.getSystemService("window");
        this.mIWindowManager = IWindowManager.Stub.asInterface(ServiceManager.getService("window"));
        this.mAssistManager = (AssistManager) Dependency.get(AssistManager.class);
        addFsgGestureWindow();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.android.systemui.taskmanager.Clear");
        this.mContext.registerReceiver(this.mRecentsReceiver, intentFilter);
        registerMiuiGestureControlHelper();
        Log.e("RecentsImpl", "BaseRecentsImpl init registerMiuiGestureControlHelper");
        initThumbnailBlur();
        registerThumbnailBlurResolver();
        this.mContext.getContentResolver().registerContentObserver(CloudDataHelper.URI_CLOUD_ALL_DATA_NOTIFY, false, this.mCloudDataObserver);
        registerScreeningModeObserver();
        Settings.Secure.putInt(this.mContext.getContentResolver(), "systemui_fsgesture_support_superpower", 1);
    }

    public void release() {
        Log.e("RecentsImpl", "BaseRecentsImpl release");
        try {
            if (this.mNavStubView != null) {
                this.mWindowManager.removeView(this.mNavStubView);
                this.mNavStubView = null;
            }
            clearBackStubWindow();
        } catch (Exception e) {
            e.printStackTrace();
        }
        unregisterMiuiGestureControlHelper();
        unRegisterInputMethodVisibleHeightReceiver();
        unregisterReceiverSafely(this.mRecentsReceiver);
        unregisterContentObserverSafely(this.mThumbnailBlurObserver);
        unregisterContentObserverSafely(this.mCloudDataObserver);
        unregisterContentObserverSafely(this.mCastModeObserver);
        unregisterContentObserverSafely(this.mAssistContentObserver);
        unregisterContentObserverSafely(this.mForceImmersiveNavBarListener);
        unregisterContentObserverSafely(this.mElderlyModeObserver);
        unregisterContentObserverSafely(this.mDriveModeObserver);
        unregisterContentObserverSafely(this.mAppSwitchAnimChangeListener);
        unregisterReceiverSafely(this.mReceiver);
        unregisterReceiverSafely(this.mFsgReceiver);
        unregisterContentObserverSafely(this.mSuperSavePowerObserver);
    }

    private void unregisterContentObserverSafely(ContentObserver contentObserver) {
        try {
            this.mContext.getContentResolver().unregisterContentObserver(contentObserver);
        } catch (Exception unused) {
        }
    }

    private void unregisterReceiverSafely(BroadcastReceiver broadcastReceiver) {
        try {
            this.mContext.unregisterReceiver(broadcastReceiver);
        } catch (Exception unused) {
        }
    }

    private void initThumbnailBlur() {
        this.mThumbnailBlurPkgWhiteSet.clear();
        this.mThumbnailBlurPkgWhiteSet.addAll(Arrays.asList(this.mContext.getResources().getStringArray(R.array.recents_thumbnail_blur_white_pkgs)));
        this.mInvalidThumbnailBlurPkgWhiteSet.clear();
        this.mInvalidThumbnailBlurPkgWhiteSet = PreferenceManager.getDefaultSharedPreferences(this.mContext.getApplicationContext()).getStringSet(PREF_KEY_INVALID_THUMBNAIL_BLUR_PKG_WHITE_SET, this.mInvalidThumbnailBlurPkgWhiteSet);
        readCloudDataForThumbnailBlur();
    }

    /* access modifiers changed from: private */
    public void readCloudDataForThumbnailBlur() {
        BackgroundThread.getHandler().removeCallbacks(this.mReadCloudForThumbnailBlurRunnable);
        BackgroundThread.getHandler().post(this.mReadCloudForThumbnailBlurRunnable);
    }

    /* access modifiers changed from: private */
    public void updateThumbnailBlurSettings() {
        this.mThumbnailBlurSet = Utilities.convertStringToSet(MiuiSettings.System.getString(this.mContext.getContentResolver(), "miui_recents_privacy_thumbnail_blur", ""));
        for (String next : this.mThumbnailBlurPkgWhiteSet) {
            if (!this.mInvalidThumbnailBlurPkgWhiteSet.contains(next) && !this.mThumbnailBlurSet.contains(next)) {
                this.mThumbnailBlurSet.add(next);
            }
        }
        MiuiSettings.System.putString(this.mContext.getContentResolver(), "miui_recents_privacy_thumbnail_blur", Utilities.convertSetToString(this.mThumbnailBlurSet));
    }

    private void registerThumbnailBlurResolver() {
        if (this.mThumbnailBlurObserver == null) {
            this.mThumbnailBlurObserver = new ContentObserver(this.mHandler) {
                public void onChange(boolean z) {
                    HashSet<String> convertStringToSet = Utilities.convertStringToSet(MiuiSettings.System.getStringForUser(BaseRecentsImpl.this.mContext.getContentResolver(), "miui_recents_privacy_thumbnail_blur", KeyguardUpdateMonitor.getCurrentUser()));
                    RecentsEventBus.getDefault().send(new ThumbnailBlurPkgsChangedEvent(convertStringToSet));
                    Iterator<String> it = convertStringToSet.iterator();
                    while (it.hasNext()) {
                        String next = it.next();
                        if (BaseRecentsImpl.this.mThumbnailBlurSet.contains(next)) {
                            BaseRecentsImpl.this.mThumbnailBlurSet.remove(next);
                        } else {
                            BaseRecentsImpl.this.mInvalidThumbnailBlurPkgWhiteSet.add(next);
                        }
                    }
                    if (BaseRecentsImpl.this.mThumbnailBlurSet.size() > 0) {
                        Iterator<String> it2 = BaseRecentsImpl.this.mThumbnailBlurSet.iterator();
                        while (it2.hasNext()) {
                            BaseRecentsImpl.this.mInvalidThumbnailBlurPkgWhiteSet.add(it2.next());
                        }
                    }
                    PreferenceManager.getDefaultSharedPreferences(BaseRecentsImpl.this.mContext.getApplicationContext()).edit().putStringSet(BaseRecentsImpl.PREF_KEY_INVALID_THUMBNAIL_BLUR_PKG_WHITE_SET, BaseRecentsImpl.this.mInvalidThumbnailBlurPkgWhiteSet).apply();
                    BaseRecentsImpl.this.mThumbnailBlurSet = convertStringToSet;
                }
            };
        }
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("miui_recents_privacy_thumbnail_blur"), false, this.mThumbnailBlurObserver, -1);
    }

    private void registerScreeningModeObserver() {
        if (this.mCastModeObserver == null) {
            this.mCastModeObserver = new ContentObserver(this.mHandler) {
                public void onChange(boolean z) {
                    try {
                        int i = Settings.Secure.getInt(BaseRecentsImpl.this.mContext.getContentResolver(), "cast_mode", 0);
                        if (i == 1) {
                            String unused = BaseRecentsImpl.this.mScreeningPkg = Settings.Secure.getString(BaseRecentsImpl.this.mContext.getContentResolver(), "cast_mode_package");
                            if (!ProcessManager.isLockedApplication(BaseRecentsImpl.this.mScreeningPkg, UserHandle.getUserId(Process.myUid()))) {
                                Boolean unused2 = BaseRecentsImpl.this.mIsChangedScreeningPkgLockState = true;
                                ProcessManagerHelper.updateApplicationLockedState(BaseRecentsImpl.this.mScreeningPkg, UserHandle.getUserId(Process.myUid()), true);
                                return;
                            }
                            Boolean unused3 = BaseRecentsImpl.this.mIsChangedScreeningPkgLockState = false;
                        } else if (i == 0) {
                            if (BaseRecentsImpl.this.mIsChangedScreeningPkgLockState.booleanValue()) {
                                ProcessManagerHelper.updateApplicationLockedState(BaseRecentsImpl.this.mScreeningPkg, UserHandle.getUserId(Process.myUid()), false);
                            }
                            String unused4 = BaseRecentsImpl.this.mScreeningPkg = null;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
        }
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("cast_mode"), false, this.mCastModeObserver);
        this.mCastModeObserver.onChange(false);
    }

    private void registerAssistObserver() {
        if (this.mAssistContentObserver == null) {
            this.mAssistContentObserver = new ContentObserver(new Handler(Looper.getMainLooper())) {
                public void onChange(boolean z) {
                    boolean isSupportGoogleAssist = BaseRecentsImpl.this.mAssistManager.isSupportGoogleAssist(-2);
                    if (BaseRecentsImpl.this.mNavStubView != null) {
                        BaseRecentsImpl.this.mNavStubView.setIsAssistantAvailable(isSupportGoogleAssist);
                    }
                }
            };
        }
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("assistant"), false, this.mAssistContentObserver);
        this.mAssistContentObserver.onChange(false);
    }

    private void registerInputMethodVisibleHeightReceiver() {
        if (!this.mHasRegistedInput) {
            this.mHasRegistedInput = true;
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("miui.intent.action.INPUT_METHOD_VISIBLE_HEIGHT_CHANGED");
            this.mContext.registerReceiverAsUser(this.mInputMethodVisibleHeightChangeReceiver, UserHandle.ALL, intentFilter, "miui.permission.USE_INTERNAL_GENERAL_API", (Handler) null);
        }
    }

    private void unRegisterInputMethodVisibleHeightReceiver() {
        if (this.mHasRegistedInput) {
            this.mHasRegistedInput = false;
            this.mContext.unregisterReceiver(this.mInputMethodVisibleHeightChangeReceiver);
        }
    }

    public void unregisterMiuiGestureControlHelper() {
        try {
            Log.e("RecentsImpl", "BaseRecentsImpl unregisterMiuiGestureControlHelper");
            Method method = WindowManagerGlobal.getWindowManagerService().getClass().getMethod("unregisterMiuiGestureControlHelper", new Class[0]);
            if (method != null) {
                method.invoke(WindowManagerGlobal.getWindowManagerService(), new Object[0]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void registerMiuiGestureControlHelper() {
        if (miui.os.UserHandle.myUserId() != CrossUserUtils.getCurrentUserId()) {
            Log.w("RecentsImpl", "registerMiuiGestureControlHelper failed: userId is wrong.");
            return;
        }
        if (this.mGestureControlHelper == null) {
            this.mGestureControlHelper = new IMiuiGestureControlHelper.Stub() {
                public MiuiAppTransitionAnimationSpec getSpec(String str, int i) throws RemoteException {
                    return new MiuiAppTransitionAnimationSpec((Bitmap) null, new Rect());
                }

                public void notifyGestureStartRecents() {
                    Log.d("RecentsImpl", "notifyGestureStartRecents");
                    BaseRecentsImpl.this.mHandler.removeMessages(100);
                    BaseRecentsImpl.this.mHandler.sendMessage(BaseRecentsImpl.this.mHandler.obtainMessage(100));
                    BaseRecentsImpl.this.mHandler.removeMessages(R.styleable.AppCompatTheme_textAppearanceListItem);
                    BaseRecentsImpl.this.mHandler.sendMessage(BaseRecentsImpl.this.mHandler.obtainMessage(R.styleable.AppCompatTheme_textAppearanceListItem));
                    BaseRecentsImpl.this.mHandler.removeMessages(R.styleable.AppCompatTheme_textAppearanceSearchResultTitle);
                    BaseRecentsImpl.this.mHandler.sendMessageDelayed(BaseRecentsImpl.this.mHandler.obtainMessage(R.styleable.AppCompatTheme_textAppearanceSearchResultTitle), 500);
                }

                public void notifyGestureAnimationStart() {
                    long j;
                    Log.d("RecentsImpl", "notifyGestureAnimationStart");
                    BaseRecentsImpl.this.mHandler.removeMessages(R.styleable.AppCompatTheme_textAppearancePopupMenuHeader);
                    Message obtainMessage = BaseRecentsImpl.this.mHandler.obtainMessage(R.styleable.AppCompatTheme_textAppearancePopupMenuHeader);
                    try {
                        j = (long) ((BaseRecentsImpl.this.mIWindowManager.getAnimationScale(2) * 300.0f) - 17.0f);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                        j = 287;
                    }
                    BaseRecentsImpl.this.mHandler.sendMessageDelayed(obtainMessage, j);
                    long unused = BaseRecentsImpl.this.mGestureAnimationStartTime = System.currentTimeMillis();
                    BaseRecentsImpl.this.mHandler.removeMessages(R.styleable.AppCompatTheme_textAppearanceSearchResultSubtitle);
                    BaseRecentsImpl.this.mHandler.sendMessage(BaseRecentsImpl.this.mHandler.obtainMessage(R.styleable.AppCompatTheme_textAppearanceSearchResultSubtitle));
                    BaseRecentsImpl.this.mHandler.removeMessages(R.styleable.AppCompatTheme_textAppearanceSmallPopupMenu);
                    BaseRecentsImpl.this.mHandler.sendMessageDelayed(BaseRecentsImpl.this.mHandler.obtainMessage(R.styleable.AppCompatTheme_textAppearanceSmallPopupMenu), 500);
                }

                public void notifyGestureAnimationCancel() {
                    Log.d("RecentsImpl", "notifyGestureAnimationCancel");
                    BaseRecentsImpl.this.mHandler.removeMessages(R.styleable.AppCompatTheme_textAppearanceListItemSmall);
                    BaseRecentsImpl.this.mHandler.sendMessage(BaseRecentsImpl.this.mHandler.obtainMessage(R.styleable.AppCompatTheme_textAppearanceListItemSmall));
                }

                public void notifyGestureAnimationEnd() {
                    Log.d("RecentsImpl", "notifyGestureAnimationEnd");
                    BaseRecentsImpl.this.mHandler.removeMessages(R.styleable.AppCompatTheme_textAppearanceListItemSecondary);
                    BaseRecentsImpl.this.mHandler.sendMessage(BaseRecentsImpl.this.mHandler.obtainMessage(R.styleable.AppCompatTheme_textAppearanceListItemSecondary));
                }
            };
        }
        try {
            Method method = WindowManagerGlobal.getWindowManagerService().getClass().getMethod("registerMiuiGestureControlHelper", new Class[]{IMiuiGestureControlHelper.class});
            Method method2 = WindowManagerGlobal.getWindowManagerService().getClass().getMethod("unregisterMiuiGestureControlHelper", new Class[0]);
            if (method != null && method2 != null) {
                method2.invoke(WindowManagerGlobal.getWindowManagerService(), new Object[0]);
                method.invoke(WindowManagerGlobal.getWindowManagerService(), new Object[]{this.mGestureControlHelper});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addFsgGestureWindow() {
        boolean z = true;
        this.mHasNavigationBar = true;
        try {
            this.mHasNavigationBar = IWindowManagerCompat.hasNavigationBar(this.mIWindowManager, ContextCompat.getDisplayId(this.mContext));
        } catch (RemoteException unused) {
        }
        if (this.mHasNavigationBar || Constants.SUPPORT_LAB_GESTURE) {
            Settings.Global.putInt(this.mContext.getContentResolver(), "systemui_fsg_version", 10);
            this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("force_fsg_nav_bar"), false, this.mForceImmersiveNavBarListener, -1);
            this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("drive_mode_drive_mode"), false, this.mDriveModeObserver);
            this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("show_gesture_appswitch_feature"), false, this.mAppSwitchAnimChangeListener, -1);
            this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("elderly_mode"), false, this.mElderlyModeObserver, -1);
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.intent.action.USER_SWITCHED");
            intentFilter.addAction("android.intent.action.USER_PRESENT");
            this.mContext.registerReceiverAsUser(this.mReceiver, UserHandle.ALL, intentFilter, (String) null, (Handler) null);
            IntentFilter intentFilter2 = new IntentFilter();
            intentFilter2.addAction("com.android.systemui.fsgesture");
            intentFilter2.addAction("android.intent.action.USER_SWITCHED");
            this.mContext.registerReceiverAsUser(this.mFsgReceiver, UserHandle.ALL, intentFilter2, "miui.permission.USE_INTERNAL_GENERAL_API", (Handler) null);
            ((ActivityObserver) Dependency.get(ActivityObserver.class)).addCallback(this.mActivityStateObserver);
            readCloudDataForFsg();
            boolean z2 = MiuiSettings.Global.getBoolean(this.mContext.getContentResolver(), "force_fsg_nav_bar");
            this.mDisabledByDriveMode = Settings.System.getInt(this.mContext.getContentResolver(), "drive_mode_drive_mode", 0) == 1;
            if (UserHandle.myUserId() == CrossUserUtils.getCurrentUserId()) {
                z = false;
            }
            this.mIsInAnotherPro = z;
            if (z2 && !this.mDisabledByDriveMode && !this.mIsInAnotherPro) {
                Log.d("RecentsImpl", "navstubview will be added: addFsgGestureWindow");
                createAndAddNavStubView();
            }
        }
    }

    /* access modifiers changed from: private */
    public void readCloudDataForFsg() {
        BackgroundThread.getHandler().removeCallbacks(this.mReadCloudRunnable);
        BackgroundThread.getHandler().post(this.mReadCloudRunnable);
    }

    public void onResumed(final String str) {
        this.mLastResumedClassName = str;
        if (this.mNavStubView != null && !this.mKM.isKeyguardLocked() && miui.os.UserHandle.myUserId() == CrossUserUtils.getCurrentUserId()) {
            String[] strArr = this.mLocalCtrlActs;
            int length = strArr.length;
            int i = 0;
            while (i < length) {
                if (!TextUtils.equals(strArr[i], str)) {
                    i++;
                } else {
                    return;
                }
            }
            if (!"com.miui.home.launcher.Launcher:com.miui.personalassistant.fake.FakeStartActivity:com.miui.personalassistant.fake.FakeEndActivity:com.miui.superpower.SuperPowerLauncherActivity".contains(str)) {
                if (this.mNoBackActListStr.contains(str)) {
                    this.mHandler.post(new Runnable() {
                        public void run() {
                            BaseRecentsImpl.this.hideBackStubWindow();
                            if (BaseRecentsImpl.this.mNavStubView != null) {
                                BaseRecentsImpl.this.mNavStubView.setVisibility(0);
                            }
                        }
                    });
                } else if (this.mNoHomeActListStr.contains(str)) {
                    this.mHandler.post(new Runnable() {
                        public void run() {
                            if (BaseRecentsImpl.this.mNavStubView != null) {
                                BaseRecentsImpl.this.mNavStubView.setVisibility(8);
                                Log.d("RecentsImpl", "resume nohome nstub gone : " + str);
                            }
                            BaseRecentsImpl.this.showBackStubWindow();
                        }
                    });
                } else if (this.mNoBackAndHomeActListStr.contains(str)) {
                    this.mHandler.post(new Runnable() {
                        public void run() {
                            if (BaseRecentsImpl.this.mNavStubView != null) {
                                BaseRecentsImpl.this.mNavStubView.setVisibility(8);
                                Log.d("RecentsImpl", "resume nobackhome nstub gone : " + str);
                            }
                            BaseRecentsImpl.this.hideBackStubWindow();
                        }
                    });
                } else {
                    this.mHandler.post(new Runnable() {
                        public void run() {
                            if (BaseRecentsImpl.this.mNavStubView != null) {
                                BaseRecentsImpl.this.mNavStubView.setVisibility(0);
                            }
                            BaseRecentsImpl.this.showBackStubWindow();
                        }
                    });
                }
                if ("lithium".equals(Build.DEVICE)) {
                    return;
                }
                if (this.mHotZoneChangeActListStr.contains(str)) {
                    this.mFsgBackState = 2;
                    sendChangeBackGestureSizeIsNeeded();
                    return;
                }
                this.mFsgBackState = 1;
                sendResetBackGestureSizeIsNeeded();
            }
        }
    }

    /* access modifiers changed from: private */
    public void showBackStubWindow() {
        showBackStubWindow(-1);
    }

    private void showBackStubWindow(int i) {
        if ((this.mHasNavigationBar || Constants.SUPPORT_LAB_GESTURE) && !this.mDisabledByDriveMode) {
            boolean z = MiuiSettings.Global.getBoolean(this.mContext.getContentResolver(), "force_fsg_nav_bar");
            if (this.mGestureStubLeft == null && z) {
                initGestureStub(i);
            }
            if (z) {
                this.mGestureStubLeft.showGestureStub();
                this.mGestureStubRight.showGestureStub();
                this.isShowing = true;
                registerInputMethodVisibleHeightReceiver();
                return;
            }
            hideBackStubWindow();
        }
    }

    private void initGestureStub(int i) {
        this.mGestureStubLeft = new GestureStubView(this.mContext);
        setDefaultProperty(this.mGestureStubLeft, 0);
        this.mGestureStubRight = new GestureStubView(this.mContext);
        setDefaultProperty(this.mGestureStubRight, 1);
        adaptToTopActivity();
    }

    private void setDefaultProperty(GestureStubView gestureStubView, int i) {
        boolean z = false;
        if (Settings.Global.getInt(this.mContext.getContentResolver(), "show_gesture_appswitch_feature", 0) != 0) {
            z = true;
        }
        gestureStubView.disableQuickSwitch(!z);
        gestureStubView.enableGestureBackAnimation(true);
        gestureStubView.setGestureStubPosition(i);
        gestureStubView.adaptAndRender();
    }

    /* access modifiers changed from: private */
    public void sendChangeBackGestureSizeIsNeeded() {
        if (!this.mIsSizeReset) {
            this.mIsSizeReset = true;
            this.mHandler.removeMessages(2777);
            this.mHandler.removeMessages(2877);
            this.mHandler.sendMessage(this.mHandler.obtainMessage(2777));
        }
    }

    /* access modifiers changed from: private */
    public void sendResetBackGestureSizeIsNeeded() {
        if (this.mIsSizeReset) {
            this.mIsSizeReset = false;
            this.mHandler.removeMessages(2777);
            this.mHandler.removeMessages(2877);
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(2877), 300);
        }
    }

    /* access modifiers changed from: private */
    public void adaptToTopActivity() {
        ComponentName topActivity = Util.getTopActivity(this.mContext);
        if (topActivity != null) {
            onResumed(topActivity.getClassName());
        }
    }

    /* access modifiers changed from: private */
    public void hideBackStubWindow() {
        GestureStubView gestureStubView = this.mGestureStubLeft;
        if (gestureStubView != null) {
            gestureStubView.hideGestureStubDelay();
        }
        GestureStubView gestureStubView2 = this.mGestureStubRight;
        if (gestureStubView2 != null) {
            gestureStubView2.hideGestureStubDelay();
        }
        this.isShowing = false;
        unRegisterInputMethodVisibleHeightReceiver();
    }

    /* access modifiers changed from: private */
    public void clearBackStubWindow() {
        try {
            if (this.mGestureStubLeft != null) {
                this.mGestureStubLeft.clearGestureStub();
                this.mGestureStubLeft = null;
            }
            if (this.mGestureStubRight != null) {
                this.mGestureStubRight.clearGestureStub();
                this.mGestureStubRight = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.isShowing = false;
        unRegisterInputMethodVisibleHeightReceiver();
    }

    /* access modifiers changed from: private */
    public void updateFsgWindowState() {
        if (!this.mHasNavigationBar && !Constants.SUPPORT_LAB_GESTURE) {
            return;
        }
        if (!MiuiSettings.Global.getBoolean(this.mContext.getContentResolver(), "force_fsg_nav_bar") || this.mDisabledByDriveMode) {
            try {
                if (this.mNavStubView != null) {
                    Log.d("RecentsImpl", "navstubview will be removed: updateFsgWindowState");
                    this.mWindowManager.removeView(this.mNavStubView);
                    this.mNavStubView = null;
                }
                clearBackStubWindow();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                if (this.mNavStubView == null) {
                    Log.d("RecentsImpl", "navstubview will be added: updateFsgWindowState");
                    createAndAddNavStubView();
                }
                addBackStubWindow();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }

    /* access modifiers changed from: private */
    public void createAndAddNavStubView() {
        this.mNavStubView = new NavStubView(this.mContext);
        WindowManager windowManager = this.mWindowManager;
        NavStubView navStubView = this.mNavStubView;
        windowManager.addView(navStubView, navStubView.getWindowParam(navStubView.getHotSpaceHeight()));
        if (UserHandle.myUserId() == 0) {
            this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("power_supersave_mode_open"), false, this.mSuperSavePowerObserver, UserHandle.myUserId());
            this.mSuperSavePowerObserver.onChange(false);
            registerAssistObserver();
        }
    }

    /* access modifiers changed from: private */
    public void addBackStubWindow() {
        if (this.mGestureStubLeft == null) {
            initGestureStub(-1);
        }
        this.mGestureStubLeft.showGestureStub();
        this.mGestureStubRight.showGestureStub();
        this.isShowing = true;
    }

    public void onBootCompleted() {
        RecentsTaskLoader taskLoader = Recents.getTaskLoader();
        RecentsTaskLoadPlan createLoadPlan = taskLoader.createLoadPlan(this.mContext);
        taskLoader.preloadTasks(createLoadPlan, -1, false);
        RecentsTaskLoadPlan.Options options = new RecentsTaskLoadPlan.Options();
        options.numVisibleTasks = taskLoader.getIconCacheSize();
        options.numVisibleTaskThumbnails = taskLoader.getThumbnailCacheSize();
        options.onlyLoadForCache = true;
        taskLoader.loadTasks(this.mContext, createLoadPlan, options);
    }

    public void onConfigurationChanged() {
        reloadResources();
        this.mDummyStackView.reloadOnConfigurationChange();
        this.mHeaderBar.onConfigurationChanged();
    }

    public void onVisibilityChanged(Context context, boolean z) {
        Recents.getSystemServices().setRecentsVisibility(context, z);
    }

    public void onStartScreenPinning(Context context, int i) {
        StatusBar statusBar = (StatusBar) ((Application) context.getApplicationContext()).getSystemUIApplication().getComponent(StatusBar.class);
        if (statusBar != null) {
            statusBar.showScreenPinningRequest(i, false);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x0044 A[Catch:{ ActivityNotFoundException -> 0x006a }] */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x004b A[Catch:{ ActivityNotFoundException -> 0x006a }] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void showRecents(boolean r7, boolean r8, boolean r9, boolean r10, boolean r11, int r12, boolean r13) {
        /*
            r6 = this;
            r6.mTriggeredFromAltTab = r7
            r6.mDraggingInRecents = r8
            r6.mLaunchedWhileDocking = r10
            com.android.systemui.recents.misc.DozeTrigger r0 = r6.mFastAltTabTrigger
            boolean r0 = r0.isAsleep()
            if (r0 == 0) goto L_0x0014
            com.android.systemui.recents.misc.DozeTrigger r7 = r6.mFastAltTabTrigger
            r7.stopDozing()
            goto L_0x002d
        L_0x0014:
            com.android.systemui.recents.misc.DozeTrigger r0 = r6.mFastAltTabTrigger
            boolean r0 = r0.isDozing()
            if (r0 == 0) goto L_0x0025
            if (r7 != 0) goto L_0x001f
            return
        L_0x001f:
            com.android.systemui.recents.misc.DozeTrigger r7 = r6.mFastAltTabTrigger
            r7.stopDozing()
            goto L_0x002d
        L_0x0025:
            if (r7 == 0) goto L_0x002d
            com.android.systemui.recents.misc.DozeTrigger r6 = r6.mFastAltTabTrigger
            r6.startDozing()
            return
        L_0x002d:
            com.android.systemui.recents.misc.SystemServicesProxy r7 = com.android.systemui.recents.Recents.getSystemServices()     // Catch:{ ActivityNotFoundException -> 0x006a }
            r0 = 0
            r1 = 1
            if (r10 != 0) goto L_0x003a
            if (r8 == 0) goto L_0x0038
            goto L_0x003a
        L_0x0038:
            r8 = r0
            goto L_0x003b
        L_0x003a:
            r8 = r1
        L_0x003b:
            com.android.systemui.recents.model.MutableBoolean r10 = new com.android.systemui.recents.model.MutableBoolean     // Catch:{ ActivityNotFoundException -> 0x006a }
            r10.<init>(r8)     // Catch:{ ActivityNotFoundException -> 0x006a }
            boolean r2 = sOneKeyCleaning     // Catch:{ ActivityNotFoundException -> 0x006a }
            if (r2 == 0) goto L_0x004b
            r7 = 2131822086(0x7f110606, float:1.9276933E38)
            r6.showToast(r7)     // Catch:{ ActivityNotFoundException -> 0x006a }
            return
        L_0x004b:
            if (r8 != 0) goto L_0x0053
            boolean r8 = r7.isRecentsActivityVisible(r10)     // Catch:{ ActivityNotFoundException -> 0x006a }
            if (r8 != 0) goto L_0x0072
        L_0x0053:
            android.app.ActivityManager$RunningTaskInfo r7 = r7.getRunningTask()     // Catch:{ ActivityNotFoundException -> 0x006a }
            boolean r8 = r10.value     // Catch:{ ActivityNotFoundException -> 0x006a }
            if (r8 != 0) goto L_0x0060
            if (r11 == 0) goto L_0x005e
            goto L_0x0060
        L_0x005e:
            r2 = r0
            goto L_0x0061
        L_0x0060:
            r2 = r1
        L_0x0061:
            r0 = r6
            r1 = r7
            r3 = r9
            r4 = r12
            r5 = r13
            r0.startRecentsActivity(r1, r2, r3, r4, r5)     // Catch:{ ActivityNotFoundException -> 0x006a }
            goto L_0x0072
        L_0x006a:
            r6 = move-exception
            java.lang.String r7 = "RecentsImpl"
            java.lang.String r8 = "Failed to launch RecentsActivity"
            android.util.Log.e(r7, r8, r6)
        L_0x0072:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.recents.BaseRecentsImpl.showRecents(boolean, boolean, boolean, boolean, boolean, int, boolean):void");
    }

    public void hideRecents(boolean z, boolean z2, boolean z3) {
        if (!z || !this.mFastAltTabTrigger.isDozing()) {
            if (z2) {
                RecentsPushEventHelper.sendHideRecentsEvent("clickHomeKey");
            }
            RecentsEventBus.getDefault().post(new HideRecentsEvent(z, z2, z3));
            return;
        }
        showNextTask();
        this.mFastAltTabTrigger.stopDozing();
    }

    public void toggleRecents(int i) {
        if (!this.mFastAltTabTrigger.isDozing()) {
            this.mDraggingInRecents = false;
            this.mLaunchedWhileDocking = false;
            this.mTriggeredFromAltTab = false;
            try {
                SystemServicesProxy systemServices = Recents.getSystemServices();
                MutableBoolean mutableBoolean = new MutableBoolean(true);
                long elapsedRealtime = SystemClock.elapsedRealtime() - this.mLastToggleTime;
                if (systemServices.isRecentsActivityVisible(mutableBoolean)) {
                    RecentsDebugFlags debugFlags = Recents.getDebugFlags();
                    if (!Recents.getConfiguration().getLaunchState().launchedWithAltTab) {
                        debugFlags.isPagingEnabled();
                        if (((long) ViewConfiguration.getDoubleTapMinTime()) < elapsedRealtime && elapsedRealtime < ((long) ViewConfiguration.getDoubleTapTimeout())) {
                            RecentsEventBus.getDefault().post(new LaunchNextTaskRequestEvent());
                            RecentsPushEventHelper.sendSwitchAppEvent("doubleTap", 1);
                        } else if (debugFlags.isPagingEnabled()) {
                            RecentsEventBus.getDefault().post(new IterateRecentsEvent());
                        } else {
                            RecentsEventBus.getDefault().post(new ToggleRecentsEvent());
                            RecentsPushEventHelper.sendHideRecentsEvent("clickRecentsKey");
                        }
                        this.mLastToggleTime = SystemClock.elapsedRealtime();
                    } else if (elapsedRealtime >= 350) {
                        RecentsEventBus.getDefault().post(new ToggleRecentsEvent());
                        RecentsPushEventHelper.sendHideRecentsEvent("clickAltTabKey");
                        this.mLastToggleTime = SystemClock.elapsedRealtime();
                    }
                } else if (elapsedRealtime >= 350) {
                    if (sOneKeyCleaning) {
                        showToast(R.string.recent_task_cleaning);
                        return;
                    }
                    startRecentsActivity(systemServices.getRunningTask(), mutableBoolean.value, true, i, false);
                    systemServices.sendCloseSystemWindows("recentapps");
                    this.mLastToggleTime = SystemClock.elapsedRealtime();
                }
            } catch (ActivityNotFoundException e) {
                Log.e("RecentsImpl", "Failed to launch RecentsActivity", e);
            }
        }
    }

    private void showToast(int i) {
        Toast.makeText(this.mContext, i, 1).show();
    }

    public void preloadRecents() {
        SystemServicesProxy systemServices = Recents.getSystemServices();
        MutableBoolean mutableBoolean = new MutableBoolean(true);
        if (!systemServices.isRecentsActivityVisible(mutableBoolean)) {
            ActivityManager.RunningTaskInfo runningTask = systemServices.getRunningTask();
            int i = runningTask != null ? runningTask.id : 0;
            RecentsTaskLoader taskLoader = Recents.getTaskLoader();
            sInstanceLoadPlan = taskLoader.createLoadPlan(this.mContext);
            sInstanceLoadPlan.preloadRawTasks(!mutableBoolean.value);
            taskLoader.preloadTasks(sInstanceLoadPlan, i, !mutableBoolean.value);
            TaskStack taskStack = sInstanceLoadPlan.getTaskStack();
            if (taskStack.getTaskCount() > 0) {
                preloadIcon(i);
                updateHeaderBarLayout(taskStack, (Rect) null);
            }
        }
    }

    public void onDraggingInRecents(float f) {
        RecentsEventBus.getDefault().sendOntoMainThread(new DraggingInRecentsEvent(f));
    }

    public void onDraggingInRecentsEnded(float f) {
        RecentsEventBus.getDefault().sendOntoMainThread(new DraggingInRecentsEndedEvent(f));
    }

    public boolean isScreeningPkg(String str) {
        return !TextUtils.isEmpty(this.mScreeningPkg) && this.mScreeningPkg.equals(str);
    }

    public static boolean toastForbidDockedWhenScreening(Context context) {
        if (Settings.Secure.getInt(context.getContentResolver(), "cast_mode", 0) != 1) {
            return false;
        }
        Toast toast = mScreeningToast;
        if (toast != null) {
            toast.setText(R.string.recents_forbid_dock_when_screening_toast_text);
            mScreeningToast.setDuration(0);
        } else {
            mScreeningToast = Toast.makeText(context, R.string.recents_forbid_dock_when_screening_toast_text, 0);
        }
        mScreeningToast.show();
        return true;
    }

    public void showNextTask() {
        ActivityManager.RunningTaskInfo runningTask;
        Task task;
        ActivityOptions activityOptions;
        SystemServicesProxy systemServices = Recents.getSystemServices();
        RecentsTaskLoader taskLoader = Recents.getTaskLoader();
        RecentsTaskLoadPlan createLoadPlan = taskLoader.createLoadPlan(this.mContext);
        int i = 0;
        taskLoader.preloadTasks(createLoadPlan, -1, false);
        TaskStack taskStack = createLoadPlan.getTaskStack();
        if (taskStack != null && taskStack.getTaskCount() != 0 && (runningTask = systemServices.getRunningTask()) != null) {
            boolean isHomeOrRecentsStack = SystemServicesProxy.isHomeOrRecentsStack(ActivityManagerCompat.getRunningTaskStackId(runningTask), runningTask);
            ArrayList<Task> stackTasks = taskStack.getStackTasks();
            int size = stackTasks.size();
            while (true) {
                task = null;
                if (i >= size - 1) {
                    activityOptions = null;
                    break;
                }
                Task task2 = stackTasks.get(i);
                if (isHomeOrRecentsStack) {
                    task = stackTasks.get(i + 1);
                    activityOptions = ActivityOptions.makeCustomAnimation(this.mContext, R.anim.recents_launch_next_affiliated_task_target, R.anim.recents_fast_toggle_app_home_exit);
                    break;
                } else if (task2.key.id == runningTask.id) {
                    task = stackTasks.get(i + 1);
                    activityOptions = ActivityOptions.makeCustomAnimation(this.mContext, R.anim.recents_launch_prev_affiliated_task_target, R.anim.recents_launch_prev_affiliated_task_source);
                    break;
                } else {
                    i++;
                }
            }
            if (task == null) {
                systemServices.startInPlaceAnimationOnFrontMostApplication(ActivityOptions.makeCustomInPlaceAnimation(this.mContext, R.anim.recents_launch_prev_affiliated_task_bounce));
            } else {
                systemServices.startActivityFromRecents(this.mContext, task.key, task.title, activityOptions);
            }
        }
    }

    public void dockTopTask(int i, int i2, int i3, Rect rect) {
        boolean z = true;
        if (!Utilities.supportsMultiWindow()) {
            Toast.makeText(this.mContext, R.string.recent_cannot_dock, 1).show();
        } else if (Recents.getSystemServices().moveTaskToDockedStack(i, i3, rect)) {
            RecentsEventBus.getDefault().send(new DockedTopTaskEvent(i2, rect));
            if (i2 != 0) {
                z = false;
            }
            showRecents(false, z, false, true, false, -1, false);
        }
    }

    public static RecentsTaskLoadPlan consumeInstanceLoadPlan() {
        RecentsTaskLoadPlan recentsTaskLoadPlan = sInstanceLoadPlan;
        sInstanceLoadPlan = null;
        return recentsTaskLoadPlan;
    }

    private void reloadResources() {
        Resources resources = this.mContext.getResources();
        this.mStatusBarHeight = resources.getDimensionPixelSize(17105478);
        this.mNavBarHeight = resources.getDimensionPixelSize(17105307);
        this.mNavBarWidth = resources.getDimensionPixelSize(17105312);
        mTaskBarHeight = TaskStackLayoutAlgorithm.getDimensionForDevice(this.mContext, R.dimen.recents_task_view_header_height, R.dimen.recents_task_view_header_height, R.dimen.recents_task_view_header_height, R.dimen.recents_task_view_header_height_tablet_land, R.dimen.recents_task_view_header_height, R.dimen.recents_task_view_header_height_tablet_land);
    }

    private void updateHeaderBarLayout(TaskStack taskStack, Rect rect) {
        Rect rect2;
        SystemServicesProxy systemServices = Recents.getSystemServices();
        Rect displayRect = systemServices.getDisplayRect();
        Rect rect3 = new Rect();
        systemServices.getStableInsets(rect3);
        if (rect != null) {
            rect2 = new Rect(rect);
        } else {
            rect2 = systemServices.getWindowRect();
        }
        if (systemServices.hasDockedTask()) {
            rect2.bottom -= rect3.bottom;
            rect3.bottom = 0;
        }
        calculateWindowStableInsets(rect3, rect2);
        rect2.offsetTo(0, 0);
        TaskStackLayoutAlgorithm stackAlgorithm = this.mDummyStackView.getStackAlgorithm();
        stackAlgorithm.setSystemInsets(rect3);
        if (taskStack != null) {
            stackAlgorithm.getTaskStackBounds(displayRect, rect2, rect3.top, rect3.left, rect3.right, this.mTaskStackBounds);
            stackAlgorithm.reset();
            stackAlgorithm.initialize(displayRect, rect2, this.mTaskStackBounds, TaskStackLayoutAlgorithm.StackState.getStackStateForStack(taskStack));
            this.mDummyStackView.setTasks(taskStack, false);
            Rect untransformedTaskViewBounds = stackAlgorithm.getUntransformedTaskViewBounds();
            if (!untransformedTaskViewBounds.isEmpty()) {
                int width = untransformedTaskViewBounds.width();
                synchronized (this.mHeaderBarLock) {
                    if (!(this.mHeaderBar.getMeasuredWidth() == width && this.mHeaderBar.getMeasuredHeight() == mTaskBarHeight)) {
                        this.mHeaderBar.measure(View.MeasureSpec.makeMeasureSpec(width, 1073741824), View.MeasureSpec.makeMeasureSpec(mTaskBarHeight, 1073741824));
                    }
                    this.mHeaderBar.layout(0, 0, width, mTaskBarHeight);
                }
                Bitmap bitmap = this.mThumbTransitionBitmapCache;
                if (bitmap == null || bitmap.getWidth() != width || this.mThumbTransitionBitmapCache.getHeight() != mTaskBarHeight) {
                    this.mThumbTransitionBitmapCache = Bitmap.createBitmap(width, mTaskBarHeight, Bitmap.Config.ARGB_8888);
                }
            }
        }
    }

    private void calculateWindowStableInsets(Rect rect, Rect rect2) {
        Rect rect3 = new Rect(Recents.getSystemServices().getDisplayRect());
        inset(rect3, rect);
        Rect rect4 = new Rect(rect2);
        rect4.intersect(rect3);
        rect.left = rect4.left - rect2.left;
        rect.top = rect4.top - rect2.top;
        rect.right = rect2.right - rect4.right;
        rect.bottom = rect2.bottom - rect4.bottom;
    }

    private void inset(Rect rect, Rect rect2) {
        rect.left += rect2.left;
        rect.top += rect2.top;
        rect.right -= rect2.right;
        rect.bottom -= rect2.bottom;
    }

    private void preloadIcon(int i) {
        RecentsTaskLoadPlan.Options options = new RecentsTaskLoadPlan.Options();
        options.runningTaskId = i;
        options.loadThumbnails = false;
        options.onlyLoadForCache = true;
        Recents.getTaskLoader().loadTasks(this.mContext, sInstanceLoadPlan, options);
    }

    /* access modifiers changed from: protected */
    public ActivityOptions getUnknownTransitionActivityOptions() {
        return ActivityOptions.makeCustomAnimation(this.mContext, R.anim.recents_from_unknown_enter, R.anim.recents_from_unknown_exit, this.mHandler, (ActivityOptions.OnAnimationStartedListener) null);
    }

    /* access modifiers changed from: protected */
    public ActivityOptions getHomeTransitionActivityOptions() {
        return ActivityOptions.makeCustomAnimation(this.mContext, R.anim.recents_from_launcher_enter, R.anim.recents_from_launcher_exit, this.mHandler, (ActivityOptions.OnAnimationStartedListener) null);
    }

    /* access modifiers changed from: package-private */
    public ActivityOptions getThumbnailTransitionActivityOptions(ActivityManager.RunningTaskInfo runningTaskInfo, TaskStackView taskStackView, Rect rect) {
        TaskViewTransform thumbnailTransitionTransform = getThumbnailTransitionTransform(taskStackView, new Task(), rect);
        if (this.mThumbTransitionBitmapCache == null) {
            return getUnknownTransitionActivityOptions();
        }
        RectF rectF = thumbnailTransitionTransform.rect;
        rectF.top += (float) mTaskBarHeight;
        return ActivityOptions.makeThumbnailAspectScaleDownAnimation(this.mDummyStackView, this.mThumbTransitionBitmapCache, (int) rectF.left, (int) rectF.top, (int) rectF.width(), (int) rectF.height(), this.mHandler, new ActivityOptions.OnAnimationStartedListener() {
            public void onAnimationStarted() {
                RecentsEventBus.getDefault().post(new EnterRecentsWindowFirstAnimationFrameEvent());
            }
        });
    }

    /* access modifiers changed from: package-private */
    public TaskViewTransform getThumbnailTransitionTransform(TaskStackView taskStackView, Task task, Rect rect) {
        TaskStack stack = taskStackView.getStack();
        Task launchTarget = stack.getLaunchTarget();
        if (launchTarget != null) {
            task.copyFrom(launchTarget);
        } else {
            launchTarget = stack.getStackFrontMostTask(true);
            if (launchTarget == null) {
                return null;
            }
            task.copyFrom(launchTarget);
        }
        Task task2 = launchTarget;
        taskStackView.updateLayoutAlgorithm(true);
        taskStackView.updateToInitialState();
        taskStackView.getStackAlgorithm().getStackTransformScreenCoordinates(task2, taskStackView.getScroller().getStackScroll(), this.mTmpTransform, (TaskViewTransform) null, rect);
        return this.mTmpTransform;
    }

    private RectF getLaunchTargetTaskViewRect(ActivityManager.RunningTaskInfo runningTaskInfo, TaskStackView taskStackView, Rect rect, Task task) {
        RectF rectF = getThumbnailTransitionTransform(taskStackView, task, rect).rect;
        rectF.top += (float) mTaskBarHeight;
        return rectF;
    }

    /* access modifiers changed from: protected */
    public void startRecentsActivity(ActivityManager.RunningTaskInfo runningTaskInfo, boolean z, boolean z2, int i, boolean z3) {
        ActivityOptions activityOptions;
        ActivityManager.RunningTaskInfo runningTaskInfo2 = runningTaskInfo;
        boolean z4 = z3;
        RecentsTaskLoader taskLoader = Recents.getTaskLoader();
        RecentsActivityLaunchState launchState = Recents.getConfiguration().getLaunchState();
        StringBuilder sb = new StringBuilder();
        sb.append("startRecentsActivity runningTask: ");
        sb.append(runningTaskInfo2 != null ? runningTaskInfo2.baseActivity.toString() : "null");
        Log.d("RecentsImpl", sb.toString());
        int i2 = (this.mLaunchedWhileDocking || runningTaskInfo2 == null) ? -1 : runningTaskInfo2.id;
        if (this.mLaunchedWhileDocking || this.mTriggeredFromAltTab || sInstanceLoadPlan == null) {
            sInstanceLoadPlan = taskLoader.createLoadPlan(this.mContext);
        }
        if (this.mLaunchedWhileDocking || this.mTriggeredFromAltTab || !sInstanceLoadPlan.hasTasks()) {
            taskLoader.preloadTasks(sInstanceLoadPlan, i2, !z);
        }
        TaskStack taskStack = sInstanceLoadPlan.getTaskStack();
        boolean z5 = taskStack.getTaskCount() > 0;
        boolean z6 = runningTaskInfo2 != null && !z && z5;
        launchState.launchedFromHome = !z6 && !this.mLaunchedWhileDocking;
        launchState.launchedFromApp = z6 || this.mLaunchedWhileDocking;
        launchState.launchedViaDockGesture = this.mLaunchedWhileDocking;
        launchState.launchedViaDragGesture = this.mDraggingInRecents;
        launchState.launchedToTaskId = i2;
        launchState.launchedWithAltTab = this.mTriggeredFromAltTab;
        launchState.launchedViaFsGesture = z4;
        preloadIcon(i2);
        Rect windowRectOverride = getWindowRectOverride(i);
        updateHeaderBarLayout(taskStack, windowRectOverride);
        TaskStackLayoutAlgorithm.VisibilityReport computeStackVisibilityReport = this.mDummyStackView.computeStackVisibilityReport();
        launchState.launchedNumVisibleTasks = computeStackVisibilityReport.numVisibleTasks;
        launchState.launchedNumVisibleThumbnails = computeStackVisibilityReport.numVisibleThumbnails;
        RecentsEventBus.getDefault().send(new ActivitySetDummyTranslucentEvent(launchState.launchedViaFsGesture && launchState.launchedFromHome));
        if (!z2 || z4) {
            if (z) {
                startRecentsActivity(ActivityOptions.makeCustomAnimation(this.mContext, -1, -1));
            } else {
                TaskViewTransform thumbnailTransitionTransform = getThumbnailTransitionTransform(this.mDummyStackView, new Task(), windowRectOverride);
                if (thumbnailTransitionTransform != null) {
                    RectF rectF = thumbnailTransitionTransform.rect;
                    rectF.top += (float) mTaskBarHeight;
                    try {
                        Method method = ActivityOptions.class.getMethod("makeTaskLaunchBehindWithCoordinates", new Class[]{Context.class, View.class, Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE});
                        if (method != null) {
                            startRecentsActivity((ActivityOptions) method.invoke(ActivityOptions.class, new Object[]{this.mContext, this.mDummyStackView, Integer.valueOf((int) rectF.left), Integer.valueOf((int) rectF.top), Integer.valueOf((int) rectF.width()), Integer.valueOf((int) rectF.height()), -1, -1}));
                        }
                    } catch (Exception unused) {
                        Log.e("RecentsImpl", "makeTaskLaunchBehindWithCoordinates method not found");
                        startRecentsActivity(ActivityOptions.makeCustomAnimation(this.mContext, -1, -1));
                    }
                } else {
                    startRecentsActivity(ActivityOptions.makeCustomAnimation(this.mContext, -1, -1));
                }
            }
            if (z5) {
                Task task = new Task();
                RecentsEventBus.getDefault().send(new FsGestureLaunchTargetTaskViewRectEvent(getLaunchTargetTaskViewRect(runningTaskInfo2, this.mDummyStackView, windowRectOverride, task), task));
                return;
            }
            return;
        }
        if (z6) {
            activityOptions = getThumbnailTransitionActivityOptions(runningTaskInfo2, this.mDummyStackView, windowRectOverride);
        } else if (z5) {
            activityOptions = getHomeTransitionActivityOptions();
        } else {
            activityOptions = getUnknownTransitionActivityOptions();
        }
        startRecentsActivity(activityOptions);
        this.mLastToggleTime = SystemClock.elapsedRealtime();
    }

    private Rect getWindowRectOverride(int i) {
        if (i == -1) {
            return null;
        }
        Rect rect = new Rect();
        Rect displayRect = Recents.getSystemServices().getDisplayRect();
        DockedDividerUtils.calculateBoundsForPosition(i, 4, rect, displayRect.width(), displayRect.height(), Recents.getSystemServices().getDockedDividerSize(this.mContext));
        return rect;
    }

    private void startRecentsActivity(ActivityOptions activityOptions) {
        Intent intent = new Intent();
        intent.setClassName("com.android.systemui", "com.android.systemui.recents.RecentsActivity");
        intent.setFlags(277364736);
        if (activityOptions != null) {
            try {
                this.mContext.startActivityAsUser(intent, activityOptions.toBundle(), UserHandle.CURRENT);
            } catch (IllegalStateException e) {
                Log.e("RecentsImpl", "startRecentsActivity", e);
                return;
            }
        } else {
            this.mContext.startActivityAsUser(intent, UserHandle.CURRENT);
        }
        RecentsEventBus.getDefault().send(new RecentsActivityStartingEvent());
    }

    public final void onBusEvent(DismissAllTaskViewsEvent dismissAllTaskViewsEvent) {
        sOneKeyCleaning = true;
    }

    public final void onBusEvent(DismissAllTaskViewsEndEvent dismissAllTaskViewsEndEvent) {
        sOneKeyCleaning = false;
    }

    public final void onBusEvent(FsGestureShowStateEvent fsGestureShowStateEvent) {
        updateFsgWindowVisibilityState(fsGestureShowStateEvent.isEnter, fsGestureShowStateEvent.typeFrom);
    }

    public final void onBusEvent(RecentsVisibilityChangedEvent recentsVisibilityChangedEvent) {
        this.mRecentsVisible = recentsVisibilityChangedEvent.visible;
    }

    /* access modifiers changed from: private */
    public void updateFsgWindowVisibilityState(boolean z, String str) {
        if (this.mNavStubView != null && !this.mIsInAnotherPro && MiuiSettings.Global.getBoolean(this.mContext.getContentResolver(), "force_fsg_nav_bar")) {
            if (z) {
                char c = 65535;
                switch (str.hashCode()) {
                    case -1025688671:
                        if (str.equals("typefrom_keyguard")) {
                            c = 1;
                            break;
                        }
                        break;
                    case -863436742:
                        if (str.equals("typefrom_provision")) {
                            c = 2;
                            break;
                        }
                        break;
                    case 413913473:
                        if (str.equals("typefrom_status_bar_expansion")) {
                            c = 3;
                            break;
                        }
                        break;
                    case 1076218718:
                        if (str.equals("typefrom_demo")) {
                            c = 0;
                            break;
                        }
                        break;
                    case 1076347482:
                        if (str.equals("typefrom_home")) {
                            c = 4;
                            break;
                        }
                        break;
                }
                if (c == 0 || c == 1) {
                    this.mNavStubView.setVisibility(8);
                    Log.d("RecentsImpl", "resume demo nstub gone");
                    hideBackStubWindow();
                } else if (c == 2 || c == 3) {
                    Log.d("RecentsImpl", "resume statusbar nstub gone");
                    this.mNavStubView.setVisibility(8);
                    showBackStubWindow();
                } else if (this.mIsSuperPowerMode) {
                    ComponentName topActivity = Util.getTopActivity(this.mContext);
                    if (topActivity != null && "com.miui.home.launcher.Launcher:com.miui.personalassistant.fake.FakeStartActivity:com.miui.personalassistant.fake.FakeEndActivity:com.miui.superpower.SuperPowerLauncherActivity".contains(topActivity.getClassName())) {
                        this.mNavStubView.setVisibility(8);
                        showBackStubWindow();
                    }
                } else {
                    this.mNavStubView.setVisibility(0);
                    showBackStubWindow();
                }
            } else if ("typefrom_keyguard".equals(str) && this.mKM.isKeyguardLocked() && isAllowUpdateFsgStateFromKeyguard(this.mLastResumedClassName)) {
                this.mNavStubView.setVisibility(0);
                showBackStubWindow();
            } else if ("typefrom_home".equals(str)) {
                ComponentName topActivity2 = Util.getTopActivity(this.mContext);
                if (topActivity2 != null && "com.miui.home.launcher.Launcher:com.miui.personalassistant.fake.FakeStartActivity:com.miui.personalassistant.fake.FakeEndActivity:com.miui.superpower.SuperPowerLauncherActivity".contains(topActivity2.getClassName())) {
                    if (this.mIsSuperPowerMode) {
                        this.mNavStubView.setVisibility(8);
                        hideBackStubWindow();
                        return;
                    }
                    this.mNavStubView.setVisibility(0);
                    hideBackStubWindow();
                }
            } else {
                adaptToTopActivity();
            }
        }
    }

    private boolean isAllowUpdateFsgStateFromKeyguard(String str) {
        if (TextUtils.isEmpty(str)) {
            return true;
        }
        return !"com.mfashiongallery.emag.morning.MorningGreetActivity:com.android.deskclock.activity.AlarmAlertFullScreenActivity".contains(str);
    }

    public final void onBusEvent(FsGestureEnterRecentsEvent fsGestureEnterRecentsEvent) {
        this.mIsHideRecentsViewByFsGesture = true;
        showRecents(false, false, false, false, false, -1, true);
    }

    public final void onBusEvent(FsGestureShowFirstCardEvent fsGestureShowFirstCardEvent) {
        this.mIsHideRecentsViewByFsGesture = false;
    }

    public final void onBusEvent(FsGesturePreloadRecentsEvent fsGesturePreloadRecentsEvent) {
        preloadRecents();
    }

    public final void onBusEvent(HideNavStubForBackWindow hideNavStubForBackWindow) {
        hideBackStubWindow();
    }

    public final void onBusEvent(RotationChangedEvent rotationChangedEvent) {
        this.mDummyStackView.onBusEvent(rotationChangedEvent);
    }

    public final void onBusEvent(RecentsActivityStartingEvent recentsActivityStartingEvent) {
        Divider divider = (Divider) getComponent(Divider.class);
        if (divider != null) {
            divider.onRecentsActivityStarting();
        }
    }

    public final void onBusEvent(DockedTopTaskEvent dockedTopTaskEvent) {
        Divider divider = (Divider) getComponent(Divider.class);
        if (divider != null) {
            divider.onDockedTopTask(dockedTopTaskEvent.dragMode, dockedTopTaskEvent.initialRect);
        }
    }

    public final void onBusEvent(DockedFirstAnimationFrameEvent dockedFirstAnimationFrameEvent) {
        Divider divider;
        SystemServicesProxy systemServices = Recents.getSystemServices();
        if (systemServices.isSystemUser(systemServices.getProcessUser()) && (divider = (Divider) getComponent(Divider.class)) != null) {
            divider.onDockedFirstAnimationFrame();
        }
    }

    public final void onBusEvent(RecentsDrawnEvent recentsDrawnEvent) {
        Divider divider = (Divider) getComponent(Divider.class);
        if (divider != null) {
            divider.onRecentsDrawn();
        }
    }

    public final void onBusEvent(UndockingTaskEvent undockingTaskEvent) {
        Divider divider = (Divider) getComponent(Divider.class);
        if (divider != null) {
            divider.onUndockingTask(false);
        }
    }

    public final void onBusEvent(MultiWindowStateChangedEvent multiWindowStateChangedEvent) {
        Divider divider = (Divider) getComponent(Divider.class);
        if (divider != null) {
            divider.onMultiWindowStateChanged(multiWindowStateChangedEvent.inMultiWindow);
        }
    }

    private <T> T getComponent(Class<T> cls) {
        return ((Application) this.mContext.getApplicationContext()).getSystemUIApplication().getComponent(cls);
    }

    public boolean getIsHideRecentsViewByFsGesture() {
        return this.mIsHideRecentsViewByFsGesture;
    }

    private class H extends Handler {
        private H() {
        }

        public void handleMessage(Message message) {
            int i = message.what;
            if (i == 2577) {
                BaseRecentsImpl.this.showBackStubWindow();
            } else if (i == 2677) {
                BaseRecentsImpl.this.hideBackStubWindow();
            } else if (i != 2777) {
                if (i != 2877) {
                    switch (i) {
                        case R.styleable.AppCompatTheme_textAppearanceLargePopupMenu:
                            RecentsEventBus.getDefault().post(new FsGesturePreloadRecentsEvent());
                            RecentsEventBus.getDefault().send(new FsGestureEnterRecentsEvent());
                            if (BaseRecentsImpl.this.mNavStubView != null) {
                                BaseRecentsImpl.this.mNavStubView.postDelayed(new Runnable() {
                                    public void run() {
                                        if (Constants.IS_SUPPORT_LINEAR_MOTOR_VIBRATE) {
                                            ((HapticFeedBackImpl) Dependency.get(HapticFeedBackImpl.class)).getHapticFeedbackUtil().performHapticFeedback("hold", false);
                                        } else if (BaseRecentsImpl.this.mNavStubView != null) {
                                            BaseRecentsImpl.this.mNavStubView.performHapticFeedback(1);
                                        }
                                    }
                                }, 100);
                                return;
                            }
                            return;
                        case R.styleable.AppCompatTheme_textAppearanceListItem:
                            Log.d("RecentsImpl", "handleMessage: MSG_START_RECENTS_ANIAMTION mRecentsVisible = " + BaseRecentsImpl.this.mRecentsVisible);
                            if (BaseRecentsImpl.this.mRecentsVisible) {
                                if (BaseRecentsImpl.this.mNavStubView != null) {
                                    RecentsEventBus.getDefault().send(new FsGestureSlideInEvent(BaseRecentsImpl.this.mNavStubView.getCurrentX(), BaseRecentsImpl.this.mNavStubView.getCurrentY()));
                                } else {
                                    RecentsEventBus.getDefault().send(new FsGestureSlideInEvent());
                                }
                                boolean unused = BaseRecentsImpl.this.mIsStartRecent = true;
                                return;
                            }
                            removeMessages(R.styleable.AppCompatTheme_textAppearanceListItem);
                            sendMessageDelayed(obtainMessage(R.styleable.AppCompatTheme_textAppearanceListItem), 20);
                            return;
                        case R.styleable.AppCompatTheme_textAppearanceListItemSecondary:
                            RecentsEventBus.getDefault().send(new FsGestureEnterRecentsCompleteEvent(true));
                            RecentsEventBus.getDefault().send(new ActivitySetDummyTranslucentEvent(false));
                            removeStartRecentsAnimMsg();
                            removeZoomRecentsMsg();
                            boolean unused2 = BaseRecentsImpl.this.mIsStartRecent = false;
                            return;
                        case R.styleable.AppCompatTheme_textAppearanceListItemSmall:
                            RecentsEventBus.getDefault().send(new FsGestureSlideOutEvent());
                            BaseRecentsImpl.this.showBackStubWindow();
                            boolean unused3 = BaseRecentsImpl.this.mIsStartRecent = false;
                            removeStartRecentsAnimMsg();
                            removeZoomRecentsMsg();
                            return;
                        case R.styleable.AppCompatTheme_textAppearancePopupMenuHeader:
                            RecentsEventBus.getDefault().send(new FsGestureShowFirstCardEvent());
                            RecentsEventBus.getDefault().send(new AnimFirstTaskViewAlphaEvent(1.0f, false));
                            return;
                        case R.styleable.AppCompatTheme_textAppearanceSearchResultSubtitle:
                            Log.d("RecentsImpl", "handleMessage: MSG_ZOOM_RECENT_VIEW mRecentsVisible = " + BaseRecentsImpl.this.mRecentsVisible + " mIsStartRecent = " + BaseRecentsImpl.this.mIsStartRecent);
                            if (!BaseRecentsImpl.this.mRecentsVisible || !BaseRecentsImpl.this.mIsStartRecent) {
                                removeMessages(R.styleable.AppCompatTheme_textAppearanceSearchResultSubtitle);
                                sendMessageDelayed(obtainMessage(R.styleable.AppCompatTheme_textAppearanceSearchResultSubtitle), 20);
                                return;
                            }
                            RecentsEventBus.getDefault().send(new FsGestureEnterRecentsZoomEvent(System.currentTimeMillis() - BaseRecentsImpl.this.mGestureAnimationStartTime, (Runnable) null));
                            removeStartRecentsAnimMsg();
                            return;
                        case R.styleable.AppCompatTheme_textAppearanceSearchResultTitle:
                            removeMessages(R.styleable.AppCompatTheme_textAppearanceListItem);
                            return;
                        case R.styleable.AppCompatTheme_textAppearanceSmallPopupMenu:
                            removeMessages(R.styleable.AppCompatTheme_textAppearanceSearchResultSubtitle);
                            return;
                        default:
                            return;
                    }
                } else if (BaseRecentsImpl.this.mGestureStubLeft != null && BaseRecentsImpl.this.mGestureStubRight != null) {
                    int i2 = BaseRecentsImpl.this.mScreenWidth != 720 ? 54 : 40;
                    BaseRecentsImpl.this.mGestureStubLeft.setSize(i2);
                    BaseRecentsImpl.this.mGestureStubRight.setSize(i2);
                }
            } else if (BaseRecentsImpl.this.mGestureStubLeft != null && BaseRecentsImpl.this.mGestureStubRight != null) {
                BaseRecentsImpl.this.mGestureStubLeft.setSize(30);
                BaseRecentsImpl.this.mGestureStubRight.setSize(30);
            }
        }

        private void removeStartRecentsAnimMsg() {
            removeMessages(R.styleable.AppCompatTheme_textAppearanceListItem);
            removeMessages(R.styleable.AppCompatTheme_textAppearanceSearchResultTitle);
        }

        private void removeZoomRecentsMsg() {
            removeMessages(R.styleable.AppCompatTheme_textAppearanceSearchResultSubtitle);
            removeMessages(R.styleable.AppCompatTheme_textAppearanceSmallPopupMenu);
        }
    }

    public class RecentsReceiver extends BroadcastReceiver {
        private final List<String> pkgsAllowCallClear = Arrays.asList(new String[]{"com.miui.home", "com.miui.securitycenter", "com.miui.touchassistant", "com.android.snapshot", "com.android.keyguard", "com.android.systemui", "com.mi.android.globallauncher", "com.xiaomi.mihomemanager", "com.miui.voiceassist", "com.xiaomi.gameboosterglobal"});

        public RecentsReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if ("com.android.systemui.taskmanager.Clear".equals(intent.getAction())) {
                String sender = intent.getSender();
                Slog.d("RecentsReceiver", "onReceive: senderName=" + sender);
                if (this.pkgsAllowCallClear.contains(sender)) {
                    boolean booleanExtra = intent.getBooleanExtra("show_toast", false);
                    ArrayList<String> stringArrayListExtra = intent.getStringArrayListExtra("protected_pkgnames");
                    int intExtra = intent.getIntExtra("clean_type", -1);
                    if (Recents.getSystemServices().isRecentsActivityVisible()) {
                        RecentsEventBus.getDefault().post(new CleanInRecentsEvents());
                    } else {
                        removeAllTask(booleanExtra, stringArrayListExtra, intExtra);
                    }
                } else {
                    Slog.d("RecentsReceiver", sender + " is not allow to call clear");
                }
            }
        }

        public void removeAllTask(boolean z, List<String> list, int i) {
            long j;
            if (z) {
                j = HardwareInfo.getFreeMemory() / 1024;
                Log.d("RecentsReceiver", "freeMemoryAtFirst:" + HardwareInfo.getFreeMemory());
            } else {
                j = 0;
            }
            List<ActivityManager.RecentTaskInfo> recentTasks = SystemServicesProxy.getInstance(BaseRecentsImpl.this.mContext).getRecentTasks(ActivityManager.getMaxRecentTasksStatic(), -2, false, new ArraySet());
            ArrayList arrayList = new ArrayList();
            if (list != null) {
                arrayList.addAll(list);
            }
            try {
                if (Recents.getSystemServices().hasDockedTask()) {
                    ActivityManager.StackInfo stackInfo = ActivityManagerCompat.getStackInfo(3, 3, 0);
                    ComponentName componentName = stackInfo.topActivity;
                    if (componentName != null && stackInfo.visible) {
                        arrayList.add(componentName.getPackageName());
                    }
                    ActivityManager.StackInfo stackInfo2 = ActivityManagerCompat.getStackInfo(1, 1, 0);
                    ComponentName componentName2 = stackInfo2.topActivity;
                    if (componentName2 != null && stackInfo2.visible) {
                        arrayList.add(componentName2.getPackageName());
                    }
                }
            } catch (Exception e) {
                Log.e("RecentsReceiver", "getProtectedTaskPkg", e);
            }
            doClear(arrayList, i, recentTasks);
            if (z) {
                showCleanEndMsg(j);
            }
        }

        private void doClear(final List<String> list, final int i, final List<ActivityManager.RecentTaskInfo> list2) {
            BackgroundThread.getHandler().post(new Runnable() {
                public void run() {
                    ProcessConfig processConfig;
                    if (i == 0) {
                        processConfig = new ProcessConfig(4);
                        processConfig.setWhiteList(list);
                    } else {
                        processConfig = new ProcessConfig(1);
                    }
                    ArrayList arrayList = new ArrayList();
                    for (ActivityManager.RecentTaskInfo recentTaskInfo : list2) {
                        arrayList.add(Integer.valueOf(recentTaskInfo.persistentId));
                    }
                    processConfig.setRemoveTaskNeeded(true);
                    processConfig.setRemovingTaskIdList(arrayList);
                    ProcessManager.kill(processConfig);
                }
            });
        }

        private void showCleanEndMsg(long j) {
            CircleProgressBar circleProgressBar = new CircleProgressBar(BaseRecentsImpl.this.mContext);
            circleProgressBar.setDrawablesForLevels(new int[]{R.drawable.clean_tip_bg}, new int[]{R.drawable.clean_tip_fg}, (int[]) null);
            circleProgressBar.setMax((int) (HardwareInfo.getTotalPhysicalMemory() / 1024));
            circleProgressBar.setProgress((int) ((HardwareInfo.getTotalPhysicalMemory() / 1024) - j));
            Log.d("RecentsReceiver", "totalPhysicalMemory:" + HardwareInfo.getTotalPhysicalMemory());
            final WindowManager windowManager = (WindowManager) BaseRecentsImpl.this.mContext.getSystemService("window");
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(-2, -2, 2006, 0, 1);
            layoutParams.gravity = 81;
            layoutParams.y = BaseRecentsImpl.this.mContext.getResources().getDimensionPixelSize(R.dimen.clean_toast_bottom_margin);
            layoutParams.windowAnimations = R.style.Animation_CleanTip;
            layoutParams.privateFlags = 16;
            windowManager.addView(circleProgressBar, layoutParams);
            final CircleProgressBar circleProgressBar2 = circleProgressBar;
            final long j2 = j;
            circleProgressBar.postDelayed(new Runnable() {
                public void run() {
                    circleProgressBar2.setProgressByAnimator(0, new AnimatorListenerAdapter() {
                        public void onAnimationEnd(Animator animator) {
                            final long freeMemory = (long) ((int) (HardwareInfo.getFreeMemory() / 1024));
                            Log.d("RecentsReceiver", "freeMemoryAtLast:" + HardwareInfo.getFreeMemory());
                            circleProgressBar2.setProgressByAnimator((int) ((HardwareInfo.getTotalPhysicalMemory() / 1024) - freeMemory), new AnimatorListenerAdapter() {
                                public void onAnimationEnd(Animator animator) {
                                    AnonymousClass2 r6 = AnonymousClass2.this;
                                    windowManager.removeView(circleProgressBar2);
                                    AnonymousClass2 r62 = AnonymousClass2.this;
                                    Toast makeText = Toast.makeText(BaseRecentsImpl.this.mContext, RecentsActivity.getToastMsg(BaseRecentsImpl.this.mContext, j2, freeMemory), 1);
                                    makeText.setType(2006);
                                    makeText.getWindowParams().privateFlags |= 16;
                                    makeText.show();
                                }
                            });
                        }
                    });
                }
            }, 250);
        }
    }
}
