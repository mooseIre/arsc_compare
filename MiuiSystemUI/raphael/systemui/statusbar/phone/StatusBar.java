package com.android.systemui.statusbar.phone;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.ActivityManager;
import android.app.ActivityManagerCompat;
import android.app.ActivityOptions;
import android.app.ActivityOptionsCompat;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.IApplicationThread;
import android.app.INotificationManager;
import android.app.KeyguardManager;
import android.app.KeyguardManagerCompat;
import android.app.MiuiStatusBarManager;
import android.app.Notification;
import android.app.NotificationChannelCompat;
import android.app.NotificationCompat;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProfilerInfo;
import android.app.RemoteInput;
import android.app.StatusBarManager;
import android.app.WallpaperInfo;
import android.app.WallpaperInfoCompat;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextCompat;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager;
import android.content.pm.UserInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.display.DisplayManager;
import android.media.AudioAttributes;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.metrics.LogMaker;
import android.miui.Shell;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.Vibrator;
import android.provider.MiuiSettings;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.service.notification.StatusBarNotificationCompat;
import android.service.vr.IVrManagerCompat;
import android.telephony.PhoneStateListener;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.DisplayMetrics;
import android.util.EventLog;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.Display;
import android.view.DisplayInfo;
import android.view.IWindowManager;
import android.view.IWindowManagerCompat;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.RemoteAnimationAdapter;
import android.view.ThreadedRenderer;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewPropertyAnimator;
import android.view.ViewStub;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;
import android.view.accessibility.AccessibilityManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.SpringInterpolator;
import android.widget.AbstractOnClickHandler;
import android.widget.DateTimeView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.MetricsLoggerCompat;
import com.android.internal.os.SomeArgs;
import com.android.internal.statusbar.IStatusBarService;
import com.android.internal.statusbar.NotificationVisibilityCompat;
import com.android.internal.statusbar.StatusBarIcon;
import com.android.internal.statusbar.StatusBarServiceCompat;
import com.android.internal.telephony.Call;
import com.android.internal.util.NotificationMessagingUtil;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.KeyguardSensorManager;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.keyguard.MiuiKeyguardUtils;
import com.android.keyguard.ViewMediatorCallback;
import com.android.keyguard.analytics.AnalyticsHelper;
import com.android.keyguard.clock.KeyguardClockContainer;
import com.android.keyguard.faceunlock.FaceUnlockCallback;
import com.android.keyguard.faceunlock.FaceUnlockController;
import com.android.keyguard.faceunlock.FaceUnlockManager;
import com.android.keyguard.fod.MiuiGxzwManager;
import com.android.keyguard.wallpaper.KeyguardWallpaperUtils;
import com.android.keyguard.wallpaper.MiuiKeyguardWallpaperController;
import com.android.systemui.ActivityStarterDelegate;
import com.android.systemui.Constants;
import com.android.systemui.CustomizedUtils;
import com.android.systemui.DemoMode;
import com.android.systemui.Dependency;
import com.android.systemui.DisplayCutoutCompat;
import com.android.systemui.EventLogTags;
import com.android.systemui.ForegroundServiceController;
import com.android.systemui.HapticFeedBackImpl;
import com.android.systemui.Interpolators;
import com.android.systemui.OverlayManagerWrapper;
import com.android.systemui.Prefs;
import com.android.systemui.RecentsComponent;
import com.android.systemui.SwipeHelper;
import com.android.systemui.SystemUI;
import com.android.systemui.SystemUICompat;
import com.android.systemui.SystemUIFactory;
import com.android.systemui.UiOffloadThread;
import com.android.systemui.Util;
import com.android.systemui.analytics.SettingsJobSchedulerService;
import com.android.systemui.assist.AssistManager;
import com.android.systemui.bubbles.BubbleController;
import com.android.systemui.classifier.FalsingLog;
import com.android.systemui.classifier.FalsingManager;
import com.android.systemui.content.pm.PackageManagerCompat;
import com.android.systemui.dnd.DndNotificationWarnings;
import com.android.systemui.doze.AodHost;
import com.android.systemui.doze.DozeLog;
import com.android.systemui.events.ScreenOffEvent;
import com.android.systemui.events.ScreenOnEvent;
import com.android.systemui.fragments.FragmentHostManager;
import com.android.systemui.fragments.PluginFragmentListener;
import com.android.systemui.keyguard.KeyguardViewMediator;
import com.android.systemui.keyguard.ScreenLifecycle;
import com.android.systemui.miui.AppIconsManager;
import com.android.systemui.miui.ToastOverlayManager;
import com.android.systemui.miui.controls.ControlsPluginManager;
import com.android.systemui.miui.policy.NotificationsMonitor;
import com.android.systemui.miui.statusbar.CloudDataHelper;
import com.android.systemui.miui.statusbar.ExpandedNotification;
import com.android.systemui.miui.statusbar.InCallUtils;
import com.android.systemui.miui.statusbar.analytics.NotificationStat;
import com.android.systemui.miui.statusbar.notification.HeadsUpAnimatedStubView;
import com.android.systemui.miui.statusbar.notification.NotificationSettingsHelper;
import com.android.systemui.miui.statusbar.notification.NotificationSettingsManager;
import com.android.systemui.miui.statusbar.notification.NotificationUtil;
import com.android.systemui.miui.statusbar.phone.ControlPanelWindowManager;
import com.android.systemui.miui.statusbar.phone.MiuiStatusBarPromptController;
import com.android.systemui.miui.statusbar.phone.applock.AppLockHelper;
import com.android.systemui.miui.statusbar.phone.rank.RankUtil;
import com.android.systemui.miui.statusbar.policy.AppMiniWindowManager;
import com.android.systemui.miui.statusbar.policy.ControlPanelController;
import com.android.systemui.miui.statusbar.policy.SuperSaveModeController;
import com.android.systemui.miui.statusbar.policy.UsbNotificationController;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.R;
import com.android.systemui.plugins.qs.QS;
import com.android.systemui.plugins.statusbar.NotificationMenuRowPlugin;
import com.android.systemui.plugins.statusbar.NotificationSwipeActionHelper;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.qs.QSFragment;
import com.android.systemui.qs.QSPanel;
import com.android.systemui.qs.QSTileHost;
import com.android.systemui.qs.QuickQSPanel;
import com.android.systemui.qs.QuickStatusBarHeader;
import com.android.systemui.recents.OverviewProxyService;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.ScreenPinningRequest;
import com.android.systemui.recents.events.RecentsEventBus;
import com.android.systemui.recents.events.activity.AppTransitionFinishedEvent;
import com.android.systemui.recents.events.activity.MultiWindowStateChangedEvent;
import com.android.systemui.recents.events.activity.UseFsGestureVersionThreeChangedEvent;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.stackdivider.Divider;
import com.android.systemui.statistic.ScenarioConstants;
import com.android.systemui.statistic.ScenarioTrackUtil;
import com.android.systemui.statusbar.ActivatableNotificationView;
import com.android.systemui.statusbar.BackDropView;
import com.android.systemui.statusbar.CallStateController;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.DismissView;
import com.android.systemui.statusbar.DragDownHelper;
import com.android.systemui.statusbar.EmptyShadeView;
import com.android.systemui.statusbar.ExpandableNotificationRow;
import com.android.systemui.statusbar.GestureRecorder;
import com.android.systemui.statusbar.KeyboardShortcuts;
import com.android.systemui.statusbar.KeyguardIndicationController;
import com.android.systemui.statusbar.KeyguardNotificationHelper;
import com.android.systemui.statusbar.NotificationAggregate;
import com.android.systemui.statusbar.NotificationData;
import com.android.systemui.statusbar.NotificationGuts;
import com.android.systemui.statusbar.NotificationInfo;
import com.android.systemui.statusbar.NotificationLogger;
import com.android.systemui.statusbar.NotificationPresenter;
import com.android.systemui.statusbar.NotificationProvider;
import com.android.systemui.statusbar.NotificationShelf;
import com.android.systemui.statusbar.NotificationSnooze;
import com.android.systemui.statusbar.RemoteInputController;
import com.android.systemui.statusbar.ScrimView;
import com.android.systemui.statusbar.SignalClusterView;
import com.android.systemui.statusbar.SysuiStatusBarStateController;
import com.android.systemui.statusbar.notification.InCallNotificationView;
import com.android.systemui.statusbar.notification.InflationException;
import com.android.systemui.statusbar.notification.MiuiActivityLaunchAnimator;
import com.android.systemui.statusbar.notification.MiuiNotificationCompat;
import com.android.systemui.statusbar.notification.NotificationActivityStarter;
import com.android.systemui.statusbar.notification.NotificationClicker;
import com.android.systemui.statusbar.notification.NotificationInflater;
import com.android.systemui.statusbar.notification.RowInflaterTask;
import com.android.systemui.statusbar.notification.VisualStabilityManager;
import com.android.systemui.statusbar.phone.NotificationGroupManager;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.phone.StatusBarTypeController;
import com.android.systemui.statusbar.phone.UnlockMethodCache;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.statusbar.policy.BrightnessMirrorController;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.statusbar.policy.DarkIconDispatcher;
import com.android.systemui.statusbar.policy.DemoModeController;
import com.android.systemui.statusbar.policy.DeviceProvisionedController;
import com.android.systemui.statusbar.policy.HeadsUpManager;
import com.android.systemui.statusbar.policy.KeyguardMonitor;
import com.android.systemui.statusbar.policy.KeyguardMonitorImpl;
import com.android.systemui.statusbar.policy.KeyguardUserSwitcher;
import com.android.systemui.statusbar.policy.NetworkController;
import com.android.systemui.statusbar.policy.OnHeadsUpChangedListener;
import com.android.systemui.statusbar.policy.PreviewInflater;
import com.android.systemui.statusbar.policy.RemoteInputView;
import com.android.systemui.statusbar.policy.SilentModeObserverController;
import com.android.systemui.statusbar.policy.TelephonyIcons;
import com.android.systemui.statusbar.policy.UserInfoController;
import com.android.systemui.statusbar.policy.UserInfoControllerImpl;
import com.android.systemui.statusbar.policy.UserSwitcherController;
import com.android.systemui.statusbar.stack.NotificationStackScrollLayout;
import com.android.systemui.util.NotificationChannels;
import com.android.systemui.util.Utils;
import com.android.systemui.util.leak.LeakDetector;
import com.android.systemui.volume.VolumeComponent;
import com.miui.aod.IMiuiAodCallback;
import com.miui.aod.IMiuiAodService;
import com.miui.systemui.annotation.Inject;
import com.xiaomi.stat.MiStat;
import com.xiaomi.stat.c.b;
import com.xiaomi.stat.c.c;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;
import miui.app.ToggleManager;
import miui.content.res.IconCustomizer;
import miui.hardware.display.DisplayFeatureManager;
import miui.os.Build;
import miui.security.SecurityManager;
import miui.telephony.TelephonyManager;
import miui.telephony.TelephonyManagerEx;
import miui.util.CustomizeUtil;

public class StatusBar extends SystemUI implements DemoMode, DragDownHelper.DragDownCallback, ActivityStarter, UnlockMethodCache.OnUnlockMethodChangedListener, OnHeadsUpChangedListener, VisualStabilityManager.Callback, CommandQueue.Callbacks, SilentModeObserverController.SilentModeListener, MiuiActivityLaunchAnimator.Callback, ExpandableNotificationRow.ExpansionLogger, NotificationData.Environment, ShadeController, NotificationInflater.InflationCallback, InCallNotificationView.InCallCallback, NotificationPresenter {
    public static final Interpolator ALPHA_IN = Interpolators.ALPHA_IN;
    public static final Interpolator ALPHA_OUT = Interpolators.ALPHA_OUT;
    private static final Intent APP_NOTIFICATION_PREFS_CATEGORY_INTENT = new Intent("android.intent.action.MAIN").addCategory("android.intent.category.NOTIFICATION_PREFERENCES");
    public static final boolean CHATTY;
    public static final boolean DEBUG = Constants.DEBUG;
    public static final boolean DEBUG_GESTURES;
    public static final boolean DEBUG_MEDIA;
    public static final boolean DEBUG_MEDIA_FAKE_ARTWORK;
    public static final boolean DEBUG_WINDOW_STATE;
    public static final boolean ENABLE_CHILD_NOTIFICATIONS;
    private static boolean ENABLE_LOCK_SCREEN_ALLOW_REMOTE_INPUT = false;
    public static final boolean ENABLE_REMOTE_INPUT = SystemProperties.getBoolean("debug.enable_remote_input", true);
    /* access modifiers changed from: private */
    public static String EXTRA_APP_UID = "app_uid";
    /* access modifiers changed from: private */
    public static String EXTRA_HIGH_PRIORITY_SETTING = "high_priority_setting";
    public static final boolean FORCE_REMOTE_INPUT_HISTORY = SystemProperties.getBoolean("debug.force_remoteinput_history", false);
    private static final boolean FREEFORM_WINDOW_MANAGEMENT;
    private static final boolean ONLY_CORE_APPS;
    public static final boolean SPEW;
    private static final AudioAttributes VIBRATION_ATTRIBUTES = new AudioAttributes.Builder().setContentType(4).setUsage(13).build();
    public static boolean sBootCompleted = false;
    public static boolean sGameMode = false;
    /* access modifiers changed from: private */
    public int[] TRANSIENT_TYPES;
    private final String TYPE_FROM_STATUS_BAR_EXPANSION = "typefrom_status_bar_expansion";
    private ContentObserver mAODObserver;
    int[] mAbsPos = new int[2];
    private ContentObserver mAccessControlLockObserver;
    protected AccessibilityManager mAccessibilityManager;
    private MiuiActivityLaunchAnimator mActivityLaunchAnimator;
    Runnable mAddNavigationBarRunnable;
    private final BroadcastReceiver mAllUsersReceiver;
    protected boolean mAllowLockscreenRemoteInput;
    private final Runnable mAnimateCollapsePanels;
    /* access modifiers changed from: private */
    public boolean mAnimateWakeup;
    /* access modifiers changed from: private */
    public AodCallback mAodCallback;
    /* access modifiers changed from: private */
    public boolean mAodEnable;
    /* access modifiers changed from: private */
    public IMiuiAodService mAodService;
    /* access modifiers changed from: private */
    public boolean mAodServiceBinded;
    /* access modifiers changed from: private */
    public boolean mAodUsingSuperWallpaperStyle;
    protected AppMiniWindowManager mAppMiniWindowManager;
    @Inject
    protected AssistManager mAssistManager;
    private final Runnable mAutohide;
    private boolean mAutohideSuspended;
    protected BackDropView mBackdrop;
    protected ImageView mBackdropBack;
    protected ImageView mBackdropFront;
    protected IStatusBarService mBarService;
    private final BroadcastReceiver mBaseBroadcastReceiver;
    @Inject
    private BatteryController mBatteryController;
    /* access modifiers changed from: private */
    public int mBatteryLevel;
    /* access modifiers changed from: private */
    public Handler mBgHandler;
    private HandlerThread mBgThread;
    protected boolean mBouncerShowing;
    BrightnessMirrorController mBrightnessMirrorController;
    private BroadcastReceiver mBroadcastReceiver;
    protected BubbleController mBubbleController;
    private final BubbleController.BubbleExpandListener mBubbleExpandListener;
    /* access modifiers changed from: private */
    public long mCallBaseTime;
    /* access modifiers changed from: private */
    public String mCallState;
    private long[] mCameraLaunchGestureVibePattern;
    private final Runnable mCancelDisableTouch;
    /* access modifiers changed from: private */
    public final Runnable mCheckBarModes;
    private final ContentObserver mCloudDataObserver;
    protected CommandQueue mCommandQueue;
    private ConfigurationController.ConfigurationListener mConfigurationListener;
    protected Context mContextForUser;
    Point mCurrentDisplaySize = new Point();
    protected final SparseArray<UserInfo> mCurrentProfiles;
    protected int mCurrentUserId;
    private final DemoModeController.DemoModeCallback mDemoCallback;
    boolean mDemoMode;
    protected boolean mDeviceInteractive;
    protected DevicePolicyManager mDevicePolicyManager;
    /* access modifiers changed from: private */
    @Inject
    public DeviceProvisionedController mDeviceProvisionedController;
    private final DeviceProvisionedController.DeviceProvisionedListener mDeviceProvisionedListener;
    private boolean mDisableFloatNotification;
    protected boolean mDisableNotificationAlerts;
    int mDisabled1 = 0;
    int mDisabled2 = 0;
    protected DismissView mDismissView;
    protected Display mDisplay;
    DisplayMetrics mDisplayMetrics = new DisplayMetrics();
    private DndNotificationWarnings mDndWarnings;
    private Divider.DockedStackExistsChangedListener mDockedStackExistsChangedListener;
    protected DozeScrimController mDozeScrimController;
    private DozeServiceHost mDozeServiceHost;
    private boolean mDozing;
    Runnable mDozingChanged;
    /* access modifiers changed from: private */
    public boolean mDozingRequested;
    private ExpandableNotificationRow mDraggedDownRow;
    private LinearLayout mDriveModeBg;
    protected EmptyShadeView mEmptyShadeView;
    private final BroadcastReceiver mEnableNotificationsReceiver;
    View mExpandedContents;
    boolean mExpandedVisible;
    private FaceUnlockCallback mFaceUnlockCallback;
    protected FaceUnlockController mFaceUnlockController;
    protected FaceUnlockManager mFaceUnlockManager;
    /* access modifiers changed from: private */
    public ValueAnimator mFadeKeyguardAimator;
    private BroadcastReceiver mFakeArtworkReceiver;
    /* access modifiers changed from: private */
    public FalsingManager mFalsingManager;
    protected FingerprintUnlockController mFingerprintUnlockController;
    private final ContentObserver mFoldImportanceObserver;
    /* access modifiers changed from: private */
    public boolean mForceBlack;
    private ContentObserver mForceBlackObserver;
    private ForegroundServiceController mForegroundServiceController;
    private ContentObserver mFullScreenGestureListener;
    /* access modifiers changed from: private */
    public boolean mGameHandsFreeMode;
    private ContentObserver mGameHandsFreeObserver;
    private ContentObserver mGameModeObserver;
    private final GestureRecorder mGestureRec;
    private PowerManager.WakeLock mGestureWakeLock;
    private final View.OnClickListener mGoToLockedShadeListener;
    protected NotificationGroupManager mGroupManager;
    /* access modifiers changed from: private */
    public NotificationMenuRowPlugin.MenuItem mGutsMenuItem;
    protected H mHandler;
    private boolean mHasAnswerCall;
    private boolean mHasBubbleAnswerCall;
    private boolean mHasClearAllNotifications;
    protected QuickStatusBarHeader mHeader;
    private HeadsUpAnimatedStubView mHeadsUpAnimatedStub;
    protected ArraySet<NotificationData.Entry> mHeadsUpEntriesToRemoveOnSwitch;
    protected HeadsUpManager mHeadsUpManager;
    protected boolean mHeadsUpTicker;
    private boolean mHideAmPmForNotification;
    protected Runnable mHideBackdropFront;
    /* access modifiers changed from: private */
    public boolean mHideGestureLine;
    private ContentObserver mHideGestureLineObserver;
    @Inject
    protected StatusBarIconController mIconController;
    PhoneStatusBarPolicy mIconPolicy;
    private boolean mInPinnedMode;
    /* access modifiers changed from: private */
    public final DisplayInfo mInfo;
    private int mInteractingWindows;
    private BroadcastReceiver mInternalBroadcastReceiver;
    private boolean mIsDNDEnabled;
    /* access modifiers changed from: private */
    public boolean mIsFsgMode;
    /* access modifiers changed from: private */
    public boolean mIsInDriveMode;
    /* access modifiers changed from: private */
    public boolean mIsInDriveModeMask;
    /* access modifiers changed from: private */
    public boolean mIsKeyguard;
    private boolean mIsRemoved;
    private boolean mIsStatusBarHidden;
    private boolean mIsUseFsGestureVersionThree;
    private boolean mKeptOnKeyguard;
    KeyguardBottomAreaView mKeyguardBottomArea;
    KeyguardClockContainer mKeyguardClock;
    protected boolean mKeyguardFadingAway;
    protected long mKeyguardFadingAwayDelay;
    protected long mKeyguardFadingAwayDuration;
    private boolean mKeyguardGoingAway;
    KeyguardIndicationController mKeyguardIndicationController;
    protected KeyguardManager mKeyguardManager;
    private KeyguardMonitorImpl mKeyguardMonitor;
    private int mKeyguardNotifications;
    private boolean mKeyguardRequested;
    protected KeyguardStatusBarView mKeyguardStatusBar;
    private KeyguardUserSwitcher mKeyguardUserSwitcher;
    private ViewMediatorCallback mKeyguardViewMediatorCallback;
    protected ArraySet<String> mKeysKeptForRemoteInput;
    private long mLastAbortTransientTime;
    /* access modifiers changed from: private */
    public int mLastCameraLaunchSource;
    private int mLastDispatchedSystemUiVisibility = -1;
    private final Rect mLastDockedStackBounds = new Rect();
    private final Rect mLastFullscreenStackBounds = new Rect();
    private int mLastLoggedStateFingerprint;
    private NotificationListenerService.RankingMap mLatestRankingMap;
    private boolean mLaunchCameraOnFinishedGoingToSleep;
    private boolean mLaunchCameraOnScreenTurningOn;
    private Runnable mLaunchTransitionEndRunnable;
    protected boolean mLaunchTransitionFadingAway;
    protected int mLayoutDirection;
    boolean mLeaveOpenOnKeyguardHide;
    LightBarController mLightBarController;
    private Locale mLocale;
    private LockPatternUtils mLockPatternUtils;
    private LockscreenGestureLogger mLockscreenGestureLogger;
    private final SparseBooleanArray mLockscreenPublicMode;
    private final ContentObserver mLockscreenSettingsObserver;
    protected LockscreenWallpaper mLockscreenWallpaper;
    /* access modifiers changed from: private */
    public int mLogicalHeight;
    /* access modifiers changed from: private */
    public int mLogicalWidth;
    int mMaxAllowedKeyguardNotifications;
    private int mMaxKeyguardNotifications;
    private MediaController mMediaController;
    private MediaController.Callback mMediaListener;
    /* access modifiers changed from: private */
    public MediaMetadata mMediaMetadata;
    private String mMediaNotificationKey;
    private MediaSessionManager mMediaSessionManager;
    private NotificationMessagingUtil mMessagingUtil;
    private final MetricsLogger mMetricsLogger;
    /* access modifiers changed from: private */
    public boolean mMirrorDndEnable;
    private final ContentObserver mMirrorDndObserver;
    private ContentObserver mMiuiOptimizationObserver;
    private BroadcastReceiver mMiuiRemoteOperationReceiver;
    /* access modifiers changed from: private */
    public MiuiStatusBarPromptController mMiuiStatusBarPrompt;
    /* access modifiers changed from: private */
    public SharedPreferences mMiuiUpdateVersionSharedPreferences;
    int mNaturalBarHeight = -1;
    /* access modifiers changed from: private */
    public boolean mNavigationBarLoaded;
    private int mNavigationBarMode;
    /* access modifiers changed from: private */
    public NavigationBarView mNavigationBarView;
    private ContentObserver mNavigationBarWindowLoadedObserver;
    /* access modifiers changed from: private */
    public int mNavigationBarYPostion;
    SparseArray<NavigationBarView> mNavigationBars;
    @Inject
    private NetworkController mNetworkController;
    private boolean mNoAnimationOnNextBarModeChange;
    private boolean mNoIconsSetGone;
    /* access modifiers changed from: private */
    public int mNotchRotation;
    private NotificationActivityStarter mNotificationActivityStarter;
    private NotificationClicker mNotificationClicker;
    protected NotificationData mNotificationData;
    /* access modifiers changed from: private */
    public NotificationGuts mNotificationGutsExposed;
    /* access modifiers changed from: private */
    public NotificationIconAreaController mNotificationIconAreaController;
    private final NotificationListenerService mNotificationListener;
    @Inject
    protected NotificationLogger mNotificationLogger;
    protected NotificationPanelView mNotificationPanel;
    protected NotificationShelf mNotificationShelf;
    /* access modifiers changed from: private */
    public final ContentObserver mNotificationStyleObserver;
    /* access modifiers changed from: private */
    public View mNotifications;
    Runnable mNotifyKeycodeGoto;
    private OLEDScreenHelper mOLEDScreenHelper;
    private final NotificationStackScrollLayout.OnChildLocationsChangedListener mOnChildLocationsChangedListener;
    private RemoteViews.OnClickHandler mOnClickHandler;
    private int mOrientation;
    private OverlayManagerWrapper mOverlayManager;
    private boolean mPanelExpanded;
    private HashMap<String, NotificationData.Entry> mPendingNotifications;
    private View mPendingRemoteInputView;
    /* access modifiers changed from: private */
    public View mPendingWorkRemoteInputView;
    private PhoneStateListener mPhoneStateListener;
    int mPixelFormat;
    ArrayList<Runnable> mPostCollapseRunnables = new ArrayList<>();
    protected PowerManager mPowerManager;
    /* access modifiers changed from: private */
    public Configuration mPreviousConfig;
    /* access modifiers changed from: private */
    public QSFragment mQSFragment;
    protected QSPanel mQSPanel;
    /* access modifiers changed from: private */
    public QSTileHost mQSTileHost;
    Object mQueueLock = new Object();
    protected QuickQSPanel mQuickQSPanel;
    private boolean mQuietModeEnable;
    protected RecentsComponent mRecents;
    private View.OnClickListener mRecentsClickListener;
    private boolean mReinflateNotificationsOnUserSwitched;
    protected RemoteInputController mRemoteInputController;
    protected ArraySet<NotificationData.Entry> mRemoteInputEntriesToRemoveOnCollapse;
    private View mReportRejectedTouch;
    /* access modifiers changed from: private */
    public ContentResolver mResolver;
    /* access modifiers changed from: private */
    public boolean mScreenButtonDisabled;
    private ContentObserver mScreenButtonStateObserver;
    private ScreenPinningRequest mScreenPinningRequest;
    private boolean mScreenTurningOn;
    protected ScrimController mScrimController;
    private boolean mScrimSrcModeEnabled;
    protected SecurityManager mSecurityManager;
    protected final ContentObserver mSettingsObserver;
    private boolean mShouldDisableFsgMode;
    private boolean mShouldPopup;
    protected boolean mShowLockscreenNotifications;
    /* access modifiers changed from: private */
    public final ContentObserver mShowNotificationIconObserver;
    /* access modifiers changed from: private */
    public boolean mShowNotifications;
    @Inject
    private SilentModeObserverController mSilentModeObserverController;
    private ContentObserver mSliderStatusObserver;
    private boolean mSoftInputVisible;
    protected PorterDuffXfermode mSrcOverXferMode;
    protected PorterDuffXfermode mSrcXferMode;
    protected NotificationStackScrollLayout mStackScroller;
    Runnable mStartTracing;
    protected boolean mStartedGoingToSleep;
    protected int mState;
    protected CollapsedStatusBarFragment mStatusBarFragment;
    protected StatusBarKeyguardViewManager mStatusBarKeyguardViewManager;
    /* access modifiers changed from: private */
    public int mStatusBarMode;
    private final SysuiStatusBarStateController mStatusBarStateController;
    private LogMaker mStatusBarStateLog;
    protected PhoneStatusBarView mStatusBarView;
    protected StatusBarWindowView mStatusBarWindow;
    protected StatusBarWindowManager mStatusBarWindowManager;
    private int mStatusBarWindowState = 0;
    Runnable mStopTracing;
    private SuperSaveModeController.SuperSaveModeChangeListener mSuperSaveModeChangeListener;
    /* access modifiers changed from: private */
    public boolean mSuperSaveModeOn;
    /* access modifiers changed from: private */
    public boolean mSupportsAmbientMode;
    int mSystemUiVisibility = 0;
    private TelephonyManager mTelephonyManager;
    private HashMap<ExpandableNotificationRow, List<ExpandableNotificationRow>> mTmpChildOrderMap;
    private final int[] mTmpInt2;
    private final Rect mTmpRect = new Rect();
    private BroadcastReceiver mToggleBroadcastReceiver;
    /* access modifiers changed from: private */
    public ToggleManager mToggleManager;
    boolean mTracking;
    int mTrackingPosition;
    private final UiOffloadThread mUiOffloadThread;
    protected UnlockMethodCache mUnlockMethodCache;
    private KeyguardUpdateMonitorCallback mUpdateCallback;
    private KeyguardUpdateMonitor mUpdateMonitor;
    /* access modifiers changed from: private */
    public Runnable mUpdateStausBarPaddingRunnable;
    protected boolean mUseHeadsUp;
    private ContentObserver mUserExperienceObserver;
    private final ContentObserver mUserFoldObserver;
    /* access modifiers changed from: private */
    public UserManager mUserManager;
    /* access modifiers changed from: private */
    public boolean mUserSetup;
    private DeviceProvisionedController.DeviceProvisionedListener mUserSetupObserver;
    @Inject
    private UserSwitcherController mUserSwitcherController;
    /* access modifiers changed from: private */
    public final SparseBooleanArray mUsersAllowingNotifications;
    /* access modifiers changed from: private */
    public final SparseBooleanArray mUsersAllowingPrivateNotifications;
    private Vibrator mVibrator;
    protected boolean mVisible;
    private boolean mVisibleToUser;
    protected VisualStabilityManager mVisualStabilityManager;
    private BroadcastReceiver mVoipPhoneStateReceiver;
    VolumeComponent mVolumeComponent;
    protected boolean mVrMode;
    private boolean mWaitingForKeyguardExit;
    private boolean mWakeUpComingFromTouch;
    private PointF mWakeUpTouchLocation;
    /* access modifiers changed from: private */
    public boolean mWakeupForNotification;
    private final ContentObserver mWakeupForNotificationObserver;
    private final BroadcastReceiver mWallpaperChangedReceiver;
    protected WindowManager mWindowManager;
    protected IWindowManager mWindowManagerService;
    protected int mZenMode;
    private INotificationManager sService;
    /* access modifiers changed from: private */
    public ServiceConnection serviceConnection;
    private View statusBarFragmentContainer;

    private int barMode(int i, int i2, int i3, int i4) {
        int i5 = i4 | 1;
        if ((i2 & i) != 0) {
            return 1;
        }
        if ((i & i5) == i5) {
            return 6;
        }
        if ((i & i4) != 0) {
            return 4;
        }
        if ((i & i3) != 0) {
            return 2;
        }
        return (i & 1) != 0 ? 3 : 0;
    }

    private static int getLoggingFingerprint(int i, boolean z, boolean z2, boolean z3, boolean z4, boolean z5) {
        return (i & 255) | ((z ? 1 : 0) << true) | ((z2 ? 1 : 0) << true) | ((z3 ? 1 : 0) << true) | ((z4 ? 1 : 0) << true) | ((z5 ? 1 : 0) << true);
    }

    /* access modifiers changed from: private */
    public boolean isPlaybackActive(int i) {
        return (i == 1 || i == 7 || i == 0) ? false : true;
    }

    /* access modifiers changed from: private */
    public boolean isSameRotation(int i, int i2) {
        if ((i == 0 || i == 2) && i2 == 1) {
            return true;
        }
        return (i == 1 || i == 3) && i2 == 2;
    }

    private int navigationBarMode(int i, int i2, int i3, int i4) {
        if ((i & i2) != 0) {
            return 1;
        }
        if ((i & i3) != 0) {
            return 2;
        }
        if ((i & i4) != 0) {
            return 4;
        }
        return (i & 1) != 0 ? 3 : 0;
    }

    private boolean verifyHeadsUpInflateFlags(int i) {
        return i == -1 || (i & 16) == 0;
    }

    public void appTransitionPending(boolean z) {
    }

    public void appTransitionStarting(long j, long j2, boolean z) {
    }

    public void handleShowGlobalActionsMenu() {
    }

    public void hideFingerprintDialog() {
    }

    public void hideRecentApps(boolean z, boolean z2) {
    }

    public void onFingerprintAuthenticated() {
    }

    public void onFingerprintError(String str) {
    }

    public void onFingerprintHelp(String str) {
    }

    public void onHeadsUpUnPinned(ExpandableNotificationRow expandableNotificationRow) {
    }

    public void removeIcon(String str) {
    }

    public void setIcon(String str, StatusBarIcon statusBarIcon) {
    }

    public void showFingerprintDialog(SomeArgs someArgs) {
    }

    public void showPictureInPictureMenu() {
    }

    public void toggleRecentApps() {
    }

    public StatusBar() {
        this.mGestureRec = DEBUG_GESTURES ? new GestureRecorder("/sdcard/statusbar_gestures.dat") : null;
        this.mMetricsLogger = (MetricsLogger) Dependency.get(MetricsLogger.class);
        this.mUserSetup = false;
        this.mUserSetupObserver = new DeviceProvisionedController.DeviceProvisionedListener() {
            public void onDeviceProvisionedChanged() {
            }

            public void onUserSwitched() {
                onUserSetupChanged();
            }

            public void onUserSetupChanged() {
                boolean isUserSetup = StatusBar.this.mDeviceProvisionedController.isUserSetup(StatusBar.this.mDeviceProvisionedController.getCurrentUser());
                Log.d("StatusBar", String.format("User setup changed: userSetup= %s mUserSetup=%s", new Object[]{Boolean.valueOf(isUserSetup), Boolean.valueOf(StatusBar.this.mUserSetup)}));
                if (isUserSetup != StatusBar.this.mUserSetup) {
                    boolean unused = StatusBar.this.mUserSetup = isUserSetup;
                    if (!StatusBar.this.mUserSetup) {
                        StatusBar statusBar = StatusBar.this;
                        if (statusBar.mStatusBarView != null) {
                            statusBar.animateCollapseQuickSettings();
                        }
                    }
                    StatusBar statusBar2 = StatusBar.this;
                    KeyguardBottomAreaView keyguardBottomAreaView = statusBar2.mKeyguardBottomArea;
                    if (keyguardBottomAreaView != null) {
                        keyguardBottomAreaView.setUserSetupComplete(statusBar2.mUserSetup);
                    }
                    StatusBar.this.updateQsExpansionEnabled();
                }
            }
        };
        this.mHandler = createHandler();
        this.mUserExperienceObserver = new ContentObserver(this.mHandler) {
            public void onChange(boolean z) {
                boolean z2 = Build.IS_DEVELOPMENT_VERSION;
                int intForUser = Settings.Secure.getIntForUser(StatusBar.this.mContext.getContentResolver(), "upload_log_pref", z2 ? 1 : 0, StatusBar.this.mCurrentUserId);
                boolean z3 = true;
                if (intForUser != 1) {
                    z3 = false;
                }
                Util.setUserExperienceProgramEnabled(z3);
            }
        };
        this.mForceBlack = false;
        this.mForceBlackObserver = new ContentObserver(this.mHandler) {
            public void onChange(boolean z) {
                StatusBar statusBar = StatusBar.this;
                boolean unused = statusBar.mForceBlack = MiuiSettings.Global.getBoolean(statusBar.mContext.getContentResolver(), "force_black");
                StatusBar statusBar2 = StatusBar.this;
                LightBarController lightBarController = statusBar2.mLightBarController;
                boolean z2 = true;
                if (!statusBar2.mForceBlack || StatusBar.this.mContext.getResources().getConfiguration().orientation != 1) {
                    z2 = false;
                }
                lightBarController.setForceBlack(z2);
                StatusBar statusBar3 = StatusBar.this;
                statusBar3.mHandler.post(statusBar3.mCheckBarModes);
                StatusBar statusBar4 = StatusBar.this;
                statusBar4.mNotificationPanel.setForceBlack(statusBar4.mForceBlack);
            }
        };
        this.mOrientation = 1;
        this.mUiOffloadThread = (UiOffloadThread) Dependency.get(UiOffloadThread.class);
        this.TRANSIENT_TYPES = new int[]{0, 1};
        this.mAutohide = new Runnable() {
            public void run() {
                if (Build.VERSION.SDK_INT > 29) {
                    StatusBar statusBar = StatusBar.this;
                    if ((statusBar.mSystemUiVisibility & 201326592) != 0) {
                        statusBar.clearTransient(statusBar.TRANSIENT_TYPES);
                        return;
                    }
                    return;
                }
                StatusBar statusBar2 = StatusBar.this;
                int i = (~statusBar2.getTransientMask()) & statusBar2.mSystemUiVisibility;
                StatusBar statusBar3 = StatusBar.this;
                if (statusBar3.mSystemUiVisibility != i) {
                    statusBar3.notifyUiVisibilityChanged(i);
                }
            }
        };
        this.mCancelDisableTouch = new Runnable() {
            public void run() {
                StatusBar.this.cancelDisableTouch();
            }
        };
        this.mSrcXferMode = new PorterDuffXfermode(PorterDuff.Mode.SRC);
        this.mSrcOverXferMode = new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER);
        this.mMediaListener = new MediaController.Callback() {
            public void onPlaybackStateChanged(PlaybackState playbackState) {
                super.onPlaybackStateChanged(playbackState);
                if (StatusBar.DEBUG_MEDIA) {
                    Log.v("StatusBar", "DEBUG_MEDIA: onPlaybackStateChanged: " + playbackState);
                }
                if (playbackState != null && !StatusBar.this.isPlaybackActive(playbackState.getState())) {
                    StatusBar.this.clearCurrentMediaNotification();
                    StatusBar.this.updateMediaMetaData(true, true);
                }
            }

            public void onMetadataChanged(MediaMetadata mediaMetadata) {
                super.onMetadataChanged(mediaMetadata);
                if (StatusBar.DEBUG_MEDIA) {
                    Log.v("StatusBar", "DEBUG_MEDIA: onMetadataChanged: " + mediaMetadata);
                }
                MediaMetadata unused = StatusBar.this.mMediaMetadata = mediaMetadata;
                StatusBar.this.updateMediaMetaData(true, true);
            }
        };
        this.mOnChildLocationsChangedListener = new NotificationStackScrollLayout.OnChildLocationsChangedListener() {
        };
        this.mTmpInt2 = new int[2];
        this.mLockscreenGestureLogger = new LockscreenGestureLogger();
        this.mPendingNotifications = new HashMap<>();
        this.mGoToLockedShadeListener = new View.OnClickListener() {
            public void onClick(View view) {
                StatusBar statusBar = StatusBar.this;
                if (statusBar.mState == 1) {
                    statusBar.wakeUpIfDozing(SystemClock.uptimeMillis(), view, "SHADE_CLICK");
                    StatusBar.this.goToLockedShade((View) null);
                }
            }
        };
        this.mTmpChildOrderMap = new HashMap<>();
        this.mStatusBarStateController = (SysuiStatusBarStateController) Dependency.get(StatusBarStateController.class);
        this.mUpdateCallback = new KeyguardUpdateMonitorCallback() {
            public void onDreamingStateChanged(boolean z) {
                if (z) {
                    StatusBar.this.maybeEscalateHeadsUp();
                }
            }
        };
        this.mFaceUnlockCallback = new FaceUnlockCallback() {
            public void onFaceAuthenticated() {
                if (StatusBar.this.mFaceUnlockManager.isShowMessageWhenFaceUnlockSuccess()) {
                    StatusBar.this.updatePublicMode();
                    boolean access$1400 = StatusBar.this.isAnyProfilePublicMode();
                    StatusBar statusBar = StatusBar.this;
                    statusBar.mStackScroller.setHideSensitive(NotificationUtil.hideNotificationsForFaceUnlock(statusBar.mContext) || access$1400, true);
                    StatusBar.this.updateNotificationViewsOnly();
                }
            }
        };
        this.mNavigationBars = new SparseArray<>();
        this.mIsStatusBarHidden = false;
        this.mGameModeObserver = new ContentObserver(this.mHandler) {
            public void onChange(boolean z) {
                boolean z2 = true;
                if (Settings.Secure.getIntForUser(StatusBar.this.mContext.getContentResolver(), "gb_notification", 0, KeyguardUpdateMonitor.getCurrentUser()) != 1) {
                    z2 = false;
                }
                StatusBar.sGameMode = z2;
            }
        };
        this.mGameHandsFreeObserver = new ContentObserver(this.mHandler) {
            public void onChange(boolean z) {
                StatusBar statusBar = StatusBar.this;
                boolean z2 = true;
                if (Settings.Secure.getIntForUser(statusBar.mContext.getContentResolver(), "gb_handsfree", 0, KeyguardUpdateMonitor.getCurrentUser()) != 1) {
                    z2 = false;
                }
                boolean unused = statusBar.mGameHandsFreeMode = z2;
            }
        };
        this.mNotchRotation = -1;
        this.mLogicalWidth = -1;
        this.mLogicalHeight = -1;
        this.mInfo = new DisplayInfo();
        this.mBubbleExpandListener = new BubbleController.BubbleExpandListener() {
            public final void onBubbleExpandChanged(boolean z, String str) {
                StatusBar.this.lambda$new$0$StatusBar(z, str);
            }
        };
        this.mDockedStackExistsChangedListener = new Divider.DockedStackExistsChangedListener() {
            public void onDockedStackMinimizedChanged(boolean z) {
                if (!Recents.getSystemServices().hasDockedTask() || !z) {
                    StatusBar.this.mMiuiStatusBarPrompt.clearState("legacy_multi");
                } else {
                    StatusBar.this.mMiuiStatusBarPrompt.setState("legacy_multi", (MiuiStatusBarPromptController.State) null, 1);
                }
            }
        };
        this.mRecentsClickListener = new View.OnClickListener() {
            public void onClick(View view) {
                StatusBar.this.toggleRecentApps();
            }
        };
        this.mToggleBroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if ("com.miui.app.ExtraStatusBarManager.TRIGGER_TOGGLE_SCREEN_BUTTONS".equals(action)) {
                    StatusBar.this.mToggleManager.performToggle(20);
                } else if ("com.miui.app.ExtraStatusBarManager.TRIGGER_TOGGLE_LOCK".equals(action)) {
                    StatusBar.this.mToggleManager.performToggle(10);
                } else if ("com.miui.app.ExtraStatusBarManager.action_TRIGGER_TOGGLE".equals(action)) {
                    StatusBar.this.mToggleManager.performToggle(intent.getIntExtra("com.miui.app.ExtraStatusBarManager.extra_TOGGLE_ID", -1));
                }
            }
        };
        this.mUpdateStausBarPaddingRunnable = new Runnable() {
            public void run() {
                StatusBar.this.updateStatusBarPading();
            }
        };
        this.mHideBackdropFront = new Runnable() {
            public void run() {
                if (StatusBar.DEBUG_MEDIA) {
                    Log.v("StatusBar", "DEBUG_MEDIA: removing fade layer");
                }
                StatusBar.this.mBackdropFront.setVisibility(4);
                StatusBar.this.mBackdropFront.animate().cancel();
                StatusBar.this.mBackdropFront.setImageDrawable((Drawable) null);
            }
        };
        this.mAnimateCollapsePanels = new Runnable() {
            public void run() {
                StatusBar.this.animateCollapsePanels();
            }
        };
        this.mCheckBarModes = new Runnable() {
            public void run() {
                StatusBar.this.checkBarModes();
            }
        };
        this.mLastAbortTransientTime = 0;
        this.mBroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (StatusBar.DEBUG) {
                    Log.v("StatusBar", "onReceive: " + intent);
                }
                String action = intent.getAction();
                if ("android.intent.action.CLOSE_SYSTEM_DIALOGS".equals(action)) {
                    KeyboardShortcuts.dismiss();
                    StatusBar.this.showReturnToInCallScreenButtonIfNeed();
                    StatusBar.this.mHeadsUpManager.removeHeadsUpNotification();
                    RemoteInputController remoteInputController = StatusBar.this.mRemoteInputController;
                    if (remoteInputController != null) {
                        remoteInputController.closeRemoteInputs();
                    }
                    if (StatusBar.this.mBubbleController.isStackExpanded()) {
                        StatusBar.this.mBubbleController.collapseStack();
                    }
                    if (StatusBar.this.isCurrentProfile(getSendingUserId())) {
                        int i = 0;
                        String stringExtra = intent.getStringExtra("reason");
                        if (stringExtra != null && stringExtra.equals("recentapps")) {
                            i = 2;
                        }
                        if (stringExtra != null && stringExtra.equals("homekey")) {
                            ((NotificationStat) Dependency.get(NotificationStat.class)).onHomePressed();
                        }
                        StatusBar.this.animateCollapsePanels(i);
                    }
                } else if ("android.app.action.SHOW_DEVICE_MONITORING_DIALOG".equals(action)) {
                    StatusBar.this.mQSPanel.showDeviceMonitoringDialog();
                }
            }
        };
        this.mInternalBroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if ("android.intent.action.LEAVE_INCALL_SCREEN_DURING_CALL".equals(action)) {
                    if (StatusBar.this.getCallState() != 0) {
                        String unused = StatusBar.this.mCallState = intent.getStringExtra("call_state");
                        long unused2 = StatusBar.this.mCallBaseTime = intent.getLongExtra("base_time", 0);
                        if (!InCallUtils.isInCallNotificationHeadsUp(StatusBar.this.mHeadsUpManager.getTopEntry())) {
                            StatusBar statusBar = StatusBar.this;
                            statusBar.showReturnToInCallScreenButton(statusBar.mCallState, StatusBar.this.mCallBaseTime);
                        }
                    }
                    if (InCallUtils.isInCallScreenShowing(StatusBar.this.mContext) || InCallUtils.isCallScreenShowing(StatusBar.this.mContext)) {
                        StatusBar.this.mMiuiStatusBarPrompt.makeReturnToInCallScreenButtonGone();
                    }
                } else if ("android.intent.action.ENTER_INCALL_SCREEN_DURING_CALL".equals(action)) {
                    StatusBar.this.hideReturnToInCallScreenButton();
                    String unused3 = StatusBar.this.mCallState = "";
                } else if ("miui.intent.action.MIUI_REGION_CHANGED".equals(action)) {
                    TelephonyIcons.updateDataTypeMiuiRegion(StatusBar.this.mContext, SystemProperties.get("ro.miui.mcc", ""));
                    QuickStatusBarHeader quickStatusBarHeader = StatusBar.this.mHeader;
                    if (quickStatusBarHeader != null) {
                        quickStatusBarHeader.regionChanged();
                    }
                    if (StatusBar.this.mQSTileHost != null && StatusBar.this.mContext.getResources().getBoolean(R.bool.config_hideWirelessPowerTile)) {
                        StatusBar.this.mQSTileHost.updateTilesAvailable();
                    }
                } else if ("com.miui.app.ExtraStatusBarManager.action_enter_drive_mode".equals(action)) {
                    String stringExtra = intent.getStringExtra("EXTRA_STATE");
                    if (stringExtra == null) {
                        stringExtra = "drivemode_standby";
                    }
                    if (stringExtra.equals("drivemode_standby")) {
                        boolean unused4 = StatusBar.this.mIsInDriveModeMask = true;
                    } else if (stringExtra.equals("drivemode_idle")) {
                        boolean unused5 = StatusBar.this.mIsInDriveModeMask = false;
                    }
                    boolean unused6 = StatusBar.this.mIsInDriveMode = true;
                    StatusBar.this.mMiuiStatusBarPrompt.showReturnToDriveModeView(true, StatusBar.this.mIsInDriveModeMask);
                    StatusBar.this.updateDriveMode();
                } else if ("com.miui.app.ExtraStatusBarManager.action_leave_drive_mode".equals(action)) {
                    boolean unused7 = StatusBar.this.mIsInDriveMode = false;
                    StatusBar.this.mMiuiStatusBarPrompt.showReturnToDriveModeView(false, false);
                    StatusBar.this.updateDriveMode();
                } else if ("com.miui.app.ExtraStatusBarManager.action_refresh_notification".equals(action)) {
                    String stringExtra2 = intent.getStringExtra("app_packageName");
                    String stringExtra3 = intent.getStringExtra("messageId");
                    intent.getStringExtra("change_importance");
                    String stringExtra4 = intent.getStringExtra("channel_id");
                    if (intent.getBooleanExtra("com.miui.app.ExtraStatusBarManager.extra_forbid_notification", false)) {
                        StatusBar.this.filterPackageNotifications(stringExtra2);
                        if (!TextUtils.equals(intent.getSender(), StatusBar.this.mContext.getPackageName())) {
                            ((NotificationStat) Dependency.get(NotificationStat.class)).onBlock(stringExtra2, stringExtra4, stringExtra3);
                        }
                    } else if (intent.getBooleanExtra(StatusBar.EXTRA_HIGH_PRIORITY_SETTING, false)) {
                        int intExtra = intent.getIntExtra(StatusBar.EXTRA_APP_UID, -1);
                        Log.d("StatusBar", "update high priority: pkg=" + stringExtra2 + ", uid=" + intExtra);
                        if (intExtra >= 0) {
                            RankUtil.updateHighPriorityMap(stringExtra2, intExtra);
                            StatusBar.this.mHandler.post(new Runnable() {
                                public void run() {
                                    StatusBar.this.updateNotifications();
                                }
                            });
                        }
                    } else if (!TextUtils.isEmpty(stringExtra2)) {
                        AppMessage appMessage = new AppMessage();
                        appMessage.pkgName = stringExtra2;
                        appMessage.className = "";
                        appMessage.userId = 0;
                        appMessage.num = 0;
                        StatusBar.this.mBgHandler.obtainMessage(b.n, appMessage).sendToTarget();
                    }
                } else if ("com.miui.app.ExtraStatusBarManager.action_remove_keyguard_notification".equals(action)) {
                    int intExtra2 = intent.getIntExtra("com.miui.app.ExtraStatusBarManager.extra_notification_key", 0);
                    int intExtra3 = intent.getIntExtra("com.miui.app.ExtraStatusBarManager.extra_notification_click", 0);
                    if (intExtra2 == 0) {
                        Log.d("StatusBar", "keyCode == 0 CLEAR_KEYGUARD_NOTIFICATION");
                        ((KeyguardNotificationHelper) Dependency.get(KeyguardNotificationHelper.class)).clear();
                        return;
                    }
                    ((KeyguardNotificationHelper) Dependency.get(KeyguardNotificationHelper.class)).remove(intExtra2, (String) null);
                    for (NotificationData.Entry next : StatusBar.this.mNotificationData.getActiveNotifications()) {
                        if (intExtra2 == next.key.hashCode()) {
                            ExpandedNotification expandedNotification = next.notification;
                            Log.d("StatusBar", "keycode=" + intExtra2 + ";click=" + intExtra3 + ";pkg=" + expandedNotification.getPackageName() + ";id=" + expandedNotification.getId());
                            if (intExtra3 == 1) {
                                next.row.callOnClick();
                            } else {
                                StatusBar.this.onNotificationClear(expandedNotification);
                            }
                        }
                    }
                }
            }
        };
        this.mDemoCallback = new DemoModeController.DemoModeCallback() {
            public void onDemoModeChanged(String str, Bundle bundle) {
                StatusBar.this.dispatchDemoCommand(str, bundle);
            }
        };
        this.mFakeArtworkReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (StatusBar.DEBUG) {
                    Log.v("StatusBar", "onReceive: " + intent);
                }
                if ("fake_artwork".equals(intent.getAction()) && StatusBar.DEBUG_MEDIA_FAKE_ARTWORK) {
                    StatusBar.this.updateMediaMetaData(true, true);
                }
            }
        };
        this.mMiuiRemoteOperationReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                String stringExtra = intent.getStringExtra("operation");
                ControlPanelController controlPanelController = (ControlPanelController) Dependency.get(ControlPanelController.class);
                boolean isUseControlCenter = controlPanelController.isUseControlCenter();
                if (!"action_panels_operation".equals(action)) {
                    return;
                }
                if ("reverse_notifications_panel".equals(stringExtra)) {
                    if (StatusBar.this.isQSFullyCollapsed()) {
                        StatusBar.this.animateExpandNotificationsPanel();
                    } else {
                        StatusBar.this.animateCollapsePanels();
                    }
                } else if (!"reverse_quick_settings_panel".equals(stringExtra)) {
                } else {
                    if (isUseControlCenter) {
                        if (controlPanelController.isQSFullyCollapsed()) {
                            controlPanelController.openPanel();
                        } else {
                            controlPanelController.collapsePanel(true);
                        }
                    } else if (StatusBar.this.isQSFullyCollapsed()) {
                        StatusBar.this.animateExpandSettingsPanel((String) null);
                    } else {
                        StatusBar.this.animateCollapsePanels();
                    }
                }
            }
        };
        this.mStartTracing = new Runnable() {
            public void run() {
                StatusBar.this.vibrate();
                SystemClock.sleep(250);
                Log.d("StatusBar", "startTracing");
                Debug.startMethodTracing("/data/statusbar-traces/trace");
                StatusBar statusBar = StatusBar.this;
                statusBar.mHandler.postDelayed(statusBar.mStopTracing, 10000);
            }
        };
        this.mStopTracing = new Runnable() {
            public void run() {
                Debug.stopMethodTracing();
                Log.d("StatusBar", "stopTracing");
                StatusBar.this.vibrate();
            }
        };
        this.mFadeKeyguardAimator = null;
        this.mAodCallback = new AodCallback();
        this.mAodServiceBinded = false;
        this.mAnimateWakeup = false;
        this.mNotifyKeycodeGoto = new Runnable() {
            public void run() {
                if (MiuiKeyguardUtils.isGxzwSensor()) {
                    MiuiGxzwManager.getInstance().notifyKeycodeGoto();
                }
            }
        };
        this.mDozingChanged = new Runnable() {
            public void run() {
                StatusBar.this.updateDozing();
            }
        };
        this.serviceConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                IMiuiAodService unused = StatusBar.this.mAodService = IMiuiAodService.Stub.asInterface(iBinder);
                if (StatusBar.this.mAodService != null) {
                    try {
                        StatusBar.this.mAodService.registerCallback(StatusBar.this.mAodCallback);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                Log.i("StatusBar", "连接Service 成功");
            }

            public void onServiceDisconnected(ComponentName componentName) {
                StatusBar.this.disconnectAodService();
                Log.e("StatusBar", "连接Service 失败");
                StatusBar.this.startAndBindAodService();
            }
        };
        this.mGroupManager = new NotificationGroupManager();
        this.mVisualStabilityManager = new VisualStabilityManager();
        this.mCurrentUserId = 0;
        this.mCurrentProfiles = new SparseArray<>();
        this.mLayoutDirection = -1;
        this.mHeadsUpEntriesToRemoveOnSwitch = new ArraySet<>();
        this.mRemoteInputEntriesToRemoveOnCollapse = new ArraySet<>();
        this.mKeysKeptForRemoteInput = new ArraySet<>();
        this.mUseHeadsUp = false;
        this.mHeadsUpTicker = false;
        this.mDisableNotificationAlerts = false;
        this.mLockscreenPublicMode = new SparseBooleanArray();
        this.mUsersAllowingPrivateNotifications = new SparseBooleanArray();
        this.mUsersAllowingNotifications = new SparseBooleanArray();
        this.mSuperSaveModeOn = false;
        this.mSuperSaveModeChangeListener = new SuperSaveModeController.SuperSaveModeChangeListener() {
            public void onSuperSaveModeChange(boolean z) {
                if (StatusBar.this.mSuperSaveModeOn != z) {
                    boolean unused = StatusBar.this.mSuperSaveModeOn = z;
                }
            }
        };
        this.mWallpaperChangedReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                WallpaperInfo wallpaperInfo = KeyguardWallpaperUtils.getWallpaperInfo();
                int identifier = StatusBar.this.mContext.getResources().getIdentifier("config_dozeSupportsAodWallpaper", "bool", "android");
                boolean z = false;
                boolean z2 = identifier > 0 ? StatusBar.this.mContext.getResources().getBoolean(identifier) : false;
                StatusBar statusBar = StatusBar.this;
                if (z2 && WallpaperInfoCompat.supportsAmbientMode(wallpaperInfo)) {
                    z = true;
                }
                boolean unused = statusBar.mSupportsAmbientMode = z;
                ((MiuiKeyguardWallpaperController) Dependency.get(MiuiKeyguardWallpaperController.class)).setWallpaperSupportsAmbientMode(StatusBar.this.mSupportsAmbientMode);
                StatusBar statusBar2 = StatusBar.this;
                statusBar2.mScrimController.setWallpaperSupportsAmbientMode(statusBar2.mSupportsAmbientMode);
                StatusBar.this.updateDozeAfterScreenOff();
                Log.d("StatusBar", "deviceSupportsAodWallpaper:" + z2 + " supportsAmbientMode:" + StatusBar.this.mSupportsAmbientMode);
            }
        };
        this.mDeviceProvisionedListener = new DeviceProvisionedController.DeviceProvisionedListener() {
            public void onUserSetupChanged() {
            }

            public void onDeviceProvisionedChanged() {
                StatusBar.this.updateNotifications();
                StatusBar.this.updateNotificationsOnDensityOrFontScaleChanged();
                if (StatusBar.this.isDeviceProvisioned() && !StatusBar.this.mMiuiUpdateVersionSharedPreferences.getBoolean("deviceProvisionUpdateTiles", false)) {
                    if (((ControlPanelController) Dependency.get(ControlPanelController.class)).isUseControlCenter()) {
                        ((ControlPanelController) Dependency.get(ControlPanelController.class)).resetTiles();
                    } else if (StatusBar.this.mQSTileHost != null) {
                        StatusBar.this.mQSTileHost.resetTiles();
                    }
                    SharedPreferences.Editor edit = StatusBar.this.mMiuiUpdateVersionSharedPreferences.edit();
                    edit.putBoolean("deviceProvisionUpdateTiles", true);
                    edit.apply();
                }
            }

            public void onUserSwitched() {
                onUserSetupChanged();
            }
        };
        this.mMiuiOptimizationObserver = new ContentObserver(this.mHandler) {
            public void onChange(boolean z) {
                boolean z2 = true;
                if (Settings.Secure.getIntForUser(StatusBar.this.mContext.getContentResolver(), "miui_optimization", 1, KeyguardUpdateMonitor.getCurrentUser()) != 0) {
                    z2 = false;
                }
                Util.setMiuiOptimizationDisabled(z2);
                if (StatusBar.this.mNotifications != null) {
                    StatusBar.this.mShowNotificationIconObserver.onChange(false);
                }
                StatusBar.this.mNotificationStyleObserver.onChange(false);
            }
        };
        this.mSettingsObserver = new ContentObserver(this.mHandler) {
            public void onChange(boolean z) {
                StatusBar.this.setZenMode(Settings.Global.getInt(StatusBar.this.mContext.getContentResolver(), "zen_mode", 0));
                StatusBar.this.updateLockscreenNotificationSetting();
            }
        };
        this.mShowNotificationIconObserver = new ContentObserver(this.mHandler) {
            public void onChange(boolean z) {
                boolean z2;
                StatusBar statusBar = StatusBar.this;
                if (Util.showCtsSpecifiedColor()) {
                    z2 = true;
                } else {
                    StatusBar statusBar2 = StatusBar.this;
                    z2 = MiuiStatusBarManager.isShowNotificationIconForUser(statusBar2.mContext, statusBar2.mCurrentUserId);
                }
                boolean unused = statusBar.mShowNotifications = z2;
                StatusBar.this.mNotificationIconAreaController.setShowNotificationIcon(StatusBar.this.mShowNotifications);
                StatusBar.this.updateNotifications();
                StatusBar.this.updateNotificationIconsLayout();
            }
        };
        this.mWakeupForNotification = true;
        this.mWakeupForNotificationObserver = new ContentObserver(this.mHandler) {
            public void onChange(boolean z) {
                StatusBar statusBar = StatusBar.this;
                boolean unused = statusBar.mWakeupForNotification = MiuiKeyguardUtils.isWakeupForNotification(statusBar.mContext.getContentResolver());
            }
        };
        this.mLockscreenSettingsObserver = new ContentObserver(this.mHandler) {
            public void onChange(boolean z) {
                StatusBar.this.mUsersAllowingPrivateNotifications.clear();
                StatusBar.this.mUsersAllowingNotifications.clear();
                StatusBar.this.updateLockscreenNotificationSetting();
                StatusBar.this.updateNotifications();
            }
        };
        this.mNotificationStyleObserver = new ContentObserver(this.mHandler) {
            public void onChange(boolean z) {
                if (NotificationUtil.isNotificationStyleChanged(Settings.System.getIntForUser(StatusBar.this.mContext.getContentResolver(), "status_bar_notification_style", (Constants.IS_INTERNATIONAL || Util.isMiuiOptimizationDisabled()) ? 1 : 0, StatusBar.this.mCurrentUserId))) {
                    StatusBar.this.updateNotificationsOnDensityOrFontScaleChanged();
                    StatusBar.this.mNotificationIconAreaController.updateNotificationIcons(StatusBar.this.mNotificationData);
                }
            }
        };
        this.mUserFoldObserver = new ContentObserver(this.mHandler) {
            public void onChange(boolean z) {
                StatusBar.this.updateNotificationsOnDensityOrFontScaleChanged();
            }
        };
        this.mFoldImportanceObserver = new ContentObserver(this.mHandler) {
            public void onChange(boolean z, Uri uri) {
                try {
                    NotificationSettingsHelper.setFoldImportance(StatusBar.this.mContextForUser, uri.getQueryParameter("package"), Integer.parseInt(uri.getQueryParameter("foldImportance")));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        this.mCloudDataObserver = new ContentObserver(this.mHandler) {
            public void onChange(boolean z) {
                CloudDataHelper.updateAll(StatusBar.this.mContext);
            }
        };
        this.mScreenButtonStateObserver = new ContentObserver(this.mHandler) {
            public void onChange(boolean z) {
                StatusBar statusBar = StatusBar.this;
                boolean z2 = false;
                if (Settings.Secure.getIntForUser(statusBar.mResolver, "screen_buttons_state", 0, StatusBar.this.mCurrentUserId) != 0) {
                    z2 = true;
                }
                boolean unused = statusBar.mScreenButtonDisabled = z2;
                StatusBar.this.processScreenBtnDisableNotification();
            }
        };
        this.mSliderStatusObserver = new ContentObserver(this.mHandler) {
            public void onChange(boolean z) {
                NotificationPanelView notificationPanelView;
                if (Settings.System.getIntForUser(StatusBar.this.mContext.getContentResolver(), "sc_status", 1, 0) == 0 && (notificationPanelView = StatusBar.this.mNotificationPanel) != null) {
                    if (notificationPanelView.isTracking() || !StatusBar.this.mNotificationPanel.isFullyCollapsed()) {
                        StatusBar.this.mNotificationPanel.stopTrackingAndCollapsed();
                        StatusBar.this.mStatusBarWindow.cancelCurrentTouch();
                    }
                }
            }
        };
        this.mNavigationBarWindowLoadedObserver = new ContentObserver(this.mHandler) {
            public void onChange(boolean z) {
                StatusBar statusBar = StatusBar.this;
                boolean unused = statusBar.mNavigationBarLoaded = MiuiSettings.System.getBooleanForUser(statusBar.mContext.getContentResolver(), "navigation_bar_window_loaded", false, KeyguardUpdateMonitor.getCurrentUser());
                StatusBar statusBar2 = StatusBar.this;
                statusBar2.mHandler.removeCallbacks(statusBar2.mAddNavigationBarRunnable);
                if (!StatusBar.this.mNavigationBarLoaded) {
                    StatusBar statusBar3 = StatusBar.this;
                    statusBar3.mHandler.post(statusBar3.mAddNavigationBarRunnable);
                }
            }
        };
        this.mAccessControlLockObserver = new ContentObserver(this.mHandler) {
            public void onChange(boolean z) {
                AppLockHelper.clearACLockEnabledAsUser();
            }
        };
        this.mAODObserver = new ContentObserver(this.mHandler) {
            public void onChange(boolean z) {
                StatusBar statusBar = StatusBar.this;
                boolean unused = statusBar.mAodEnable = MiuiKeyguardUtils.isAodEnable(statusBar.mContext);
                StatusBar statusBar2 = StatusBar.this;
                boolean unused2 = statusBar2.mAodUsingSuperWallpaperStyle = MiuiKeyguardUtils.isAodUsingSuperWallpaperStyle(statusBar2.mContext);
                StatusBar.this.updateDozeAfterScreenOff();
            }
        };
        this.mMirrorDndObserver = new ContentObserver(this.mHandler) {
            public void onChange(boolean z) {
                StatusBar statusBar = StatusBar.this;
                boolean z2 = false;
                if (Settings.Secure.getIntForUser(statusBar.mContext.getContentResolver(), "miui_mirror_dnd_mode", 0, StatusBar.this.mCurrentUserId) != 0) {
                    z2 = true;
                }
                boolean unused = statusBar.mMirrorDndEnable = z2;
                Log.w("StatusBar", "Miui Mirror dnd mode, " + StatusBar.this.mMirrorDndEnable);
            }
        };
        this.mAddNavigationBarRunnable = new Runnable() {
            public void run() {
                StatusBar.this.addNavigationBar();
            }
        };
        this.mOnClickHandler = new AbstractOnClickHandler() {
            public boolean onClickHandler(final View view, final PendingIntent pendingIntent, final Intent intent) {
                StatusBar.this.wakeUpIfDozing(SystemClock.uptimeMillis(), view, "NOTIFICATION_CLICK");
                if (handleRemoteInput(view, pendingIntent, intent)) {
                    return true;
                }
                if (StatusBar.DEBUG) {
                    Log.v("StatusBar", "Notification click handler invoked for intent: " + pendingIntent);
                }
                logActionClick(view);
                try {
                    ActivityManagerCompat.getService().resumeAppSwitches();
                } catch (RemoteException unused) {
                }
                if (!pendingIntent.isActivity()) {
                    return superOnClickHandler(view, pendingIntent, intent);
                }
                StatusBar.this.dismissKeyguardThenExecute(new ActivityStarter.OnDismissAction() {
                    public boolean onDismiss() {
                        try {
                            ActivityManagerCompat.getService().resumeAppSwitches();
                        } catch (RemoteException unused) {
                        }
                        boolean access$9900 = AnonymousClass92.this.superOnClickHandler(view, pendingIntent, intent);
                        if (access$9900) {
                            StatusBar.this.animateCollapsePanels(2, true);
                            StatusBar.this.visibilityChanged(false);
                            StatusBar.this.mAssistManager.hideAssist();
                        }
                        return access$9900;
                    }
                }, PreviewInflater.wouldLaunchResolverActivity(StatusBar.this.mContext, pendingIntent.getIntent(), StatusBar.this.mCurrentUserId));
                return true;
            }

            private void logActionClick(View view) {
                Integer actionIndex = StatusBarServiceCompat.getActionIndex(view);
                if (Build.VERSION.SDK_INT <= 28 || actionIndex != null) {
                    ViewParent parent = view.getParent();
                    ExpandedNotification notificationForParent = getNotificationForParent(parent);
                    if (notificationForParent == null) {
                        Log.w("StatusBar", "Couldn't determine notification for click.");
                        return;
                    }
                    String key = notificationForParent.getKey();
                    int indexOfChild = (view.getId() != 16908671 || parent == null || !(parent instanceof ViewGroup)) ? -1 : ((ViewGroup) parent).indexOfChild(view);
                    try {
                        int rank = StatusBar.this.mNotificationData.getRank(key);
                        int size = StatusBar.this.mNotificationData.getActiveNotifications().size();
                        StatusBarServiceCompat.onNotificationActionClick(StatusBar.this.mBarService, key, indexOfChild, actionIndex != null ? notificationForParent.getNotification().actions[actionIndex.intValue()] : null, NotificationVisibilityCompat.obtain(key, rank, size, true), false);
                    } catch (Exception unused) {
                    }
                } else {
                    Log.e("StatusBar", "Couldn't retrieve the actionIndex from the clicked button");
                }
            }

            private ExpandedNotification getNotificationForParent(ViewParent viewParent) {
                while (viewParent != null) {
                    if (viewParent instanceof ExpandableNotificationRow) {
                        return ((ExpandableNotificationRow) viewParent).getStatusBarNotification();
                    }
                    viewParent = viewParent.getParent();
                }
                return null;
            }

            /* access modifiers changed from: private */
            public boolean superOnClickHandler(View view, PendingIntent pendingIntent, Intent intent) {
                return super.onClickHandler(view, pendingIntent, intent, 1);
            }

            private boolean handleRemoteInput(View view, PendingIntent pendingIntent, Intent intent) {
                RemoteInputView remoteInputView;
                Object tag = view.getTag(16909297);
                ExpandableNotificationRow expandableNotificationRow = null;
                RemoteInput[] remoteInputArr = tag instanceof RemoteInput[] ? (RemoteInput[]) tag : null;
                if (remoteInputArr == null) {
                    return false;
                }
                RemoteInput remoteInput = null;
                for (RemoteInput remoteInput2 : remoteInputArr) {
                    if (remoteInput2.getAllowFreeFormInput()) {
                        remoteInput = remoteInput2;
                    }
                }
                if (remoteInput == null) {
                    return false;
                }
                ViewParent parent = view.getParent();
                while (true) {
                    if (parent == null) {
                        remoteInputView = null;
                        break;
                    }
                    if (parent instanceof View) {
                        View view2 = (View) parent;
                        if (view2.isRootNamespace()) {
                            remoteInputView = findRemoteInputView(view2);
                            break;
                        }
                    }
                    parent = parent.getParent();
                }
                while (true) {
                    if (parent == null) {
                        break;
                    } else if (parent instanceof ExpandableNotificationRow) {
                        expandableNotificationRow = (ExpandableNotificationRow) parent;
                        break;
                    } else {
                        parent = parent.getParent();
                    }
                }
                if (expandableNotificationRow == null) {
                    return false;
                }
                expandableNotificationRow.setUserExpanded(true);
                if (!StatusBar.this.mAllowLockscreenRemoteInput) {
                    int identifier = pendingIntent.getCreatorUserHandle().getIdentifier();
                    if (StatusBar.this.isLockscreenPublicMode(identifier)) {
                        StatusBar.this.onLockedRemoteInput(expandableNotificationRow, view);
                        return true;
                    } else if (StatusBar.this.mUserManager.getUserInfo(identifier).isManagedProfile() && StatusBar.this.mKeyguardManager.isDeviceLocked(identifier)) {
                        StatusBar.this.onLockedWorkRemoteInput(identifier, expandableNotificationRow, view);
                        return true;
                    }
                }
                if (remoteInputView == null) {
                    remoteInputView = findRemoteInputView(expandableNotificationRow.getPrivateLayout().getExpandedChild());
                    if (remoteInputView == null) {
                        return false;
                    }
                    if (!expandableNotificationRow.getPrivateLayout().getExpandedChild().isShown()) {
                        StatusBar.this.onMakeExpandedVisibleForRemoteInput(expandableNotificationRow, view);
                        return true;
                    }
                }
                int width = view.getWidth();
                if (view instanceof TextView) {
                    TextView textView = (TextView) view;
                    if (textView.getLayout() != null) {
                        width = Math.min(width, ((int) textView.getLayout().getLineWidth(0)) + textView.getCompoundPaddingLeft() + textView.getCompoundPaddingRight());
                    }
                }
                int left = view.getLeft() + (width / 2);
                int top = view.getTop() + (view.getHeight() / 2);
                int width2 = remoteInputView.getWidth();
                int height = remoteInputView.getHeight() - top;
                int i = width2 - left;
                remoteInputView.setRevealParameters(left, top, Math.max(Math.max(left + top, left + height), Math.max(i + top, i + height)));
                remoteInputView.setPendingIntent(pendingIntent);
                remoteInputView.setRemoteInput(remoteInputArr, remoteInput);
                remoteInputView.focusAnimated();
                return true;
            }

            private RemoteInputView findRemoteInputView(View view) {
                if (view == null) {
                    return null;
                }
                return (RemoteInputView) view.findViewWithTag(RemoteInputView.VIEW_TAG);
            }
        };
        this.mBaseBroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if ("android.intent.action.USER_SWITCHED".equals(action)) {
                    StatusBar.this.mCurrentUserId = intent.getIntExtra("android.intent.extra.user_handle", -1);
                    StatusBar statusBar = StatusBar.this;
                    statusBar.mContextForUser = statusBar.getContextForUser(statusBar.mCurrentUserId);
                    StatusBar.this.updateCurrentProfilesCache();
                    Log.v("StatusBar", "userId " + StatusBar.this.mCurrentUserId + " is in the house");
                    StatusBar.this.updateLockscreenNotificationSetting();
                    StatusBar statusBar2 = StatusBar.this;
                    statusBar2.userSwitched(statusBar2.mCurrentUserId);
                    StatusBar.this.mToggleManager.updateAllToggles(StatusBar.this.mCurrentUserId);
                } else if ("android.intent.action.USER_ADDED".equals(action)) {
                    StatusBar.this.updateCurrentProfilesCache();
                } else if ("android.intent.action.USER_PRESENT".equals(action)) {
                    List<ActivityManager.RecentTaskInfo> list = null;
                    try {
                        list = ActivityManagerCompat.getRecentTasks(1, 5, StatusBar.this.mCurrentUserId);
                    } catch (RemoteException unused) {
                    }
                    if (list != null && list.size() > 0) {
                        UserInfo userInfo = StatusBar.this.mUserManager.getUserInfo(list.get(0).userId);
                        if (!(userInfo == null || !userInfo.isManagedProfile() || userInfo.id == 999)) {
                            Toast makeText = Toast.makeText(StatusBar.this.mContext, R.string.managed_profile_foreground_toast, 0);
                            if (makeText.getView() != null) {
                                TextView textView = (TextView) makeText.getView().findViewById(16908299);
                                textView.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.stat_sys_managed_profile_status, 0, 0, 0);
                                textView.setCompoundDrawablePadding(StatusBar.this.mContext.getResources().getDimensionPixelSize(R.dimen.managed_profile_toast_padding));
                            }
                            makeText.show();
                        }
                        if (userInfo != null) {
                            StatusBar.this.mIconPolicy.profileChanged(userInfo.getUserHandle().getIdentifier());
                        }
                    }
                } else if ("com.android.systemui.statusbar.banner_action_cancel".equals(action) || "com.android.systemui.statusbar.banner_action_setup".equals(action)) {
                    ((NotificationManager) StatusBar.this.mContext.getSystemService("notification")).cancel(5);
                    Settings.Secure.putInt(StatusBar.this.mContext.getContentResolver(), "show_note_about_notification_hiding", 0);
                    if ("com.android.systemui.statusbar.banner_action_setup".equals(action)) {
                        StatusBar.this.animateCollapsePanels(2, true);
                        StatusBar.this.mContext.startActivity(new Intent("android.settings.ACTION_APP_NOTIFICATION_REDACTION").addFlags(268435456));
                    }
                } else if ("com.android.systemui.statusbar.work_challenge_unlocked_notification_action".equals(action)) {
                    IntentSender intentSender = (IntentSender) intent.getParcelableExtra("android.intent.extra.INTENT");
                    String stringExtra = intent.getStringExtra("android.intent.extra.INDEX");
                    if (intentSender != null) {
                        try {
                            StatusBar.this.mContext.startIntentSender(intentSender, (Intent) null, 0, 0, 0);
                        } catch (IntentSender.SendIntentException unused2) {
                        }
                    }
                    if (stringExtra != null) {
                        try {
                            StatusBarServiceCompat.onNotificationClick(StatusBar.this.mBarService, stringExtra, NotificationVisibilityCompat.obtain(stringExtra, StatusBar.this.mNotificationData.getRank(stringExtra), StatusBar.this.mNotificationData.getActiveNotifications().size(), true));
                        } catch (Exception unused3) {
                        }
                    }
                }
            }
        };
        this.mAllUsersReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                int intExtra = intent.getIntExtra("android.intent.extra.user_handle", -10000);
                if ("android.app.action.DEVICE_POLICY_MANAGER_STATE_CHANGED".equals(action) && StatusBar.this.isCurrentProfile(getSendingUserId())) {
                    StatusBar.this.mUsersAllowingPrivateNotifications.clear();
                    StatusBar.this.updateLockscreenNotificationSetting();
                    StatusBar.this.updateNotifications();
                } else if ("android.intent.action.DEVICE_LOCKED_CHANGED".equals(action)) {
                    StatusBar statusBar = StatusBar.this;
                    if (intExtra != statusBar.mCurrentUserId && statusBar.isCurrentProfile(intExtra)) {
                        StatusBar.this.onWorkChallengeChanged();
                    }
                } else if ("android.intent.action.USER_UNLOCKED".equals(action)) {
                    ((OverviewProxyService) Dependency.get(OverviewProxyService.class)).startConnectionToCurrentUser();
                } else if ("android.intent.action.APPLICATION_MESSAGE_QUERY".equals(action)) {
                    boolean booleanExtra = intent.getBooleanExtra("com.miui.extra_update_request_first_time", false);
                    Log.d("StatusBar", "recevie broadbcast ACTION_APPLICATION_MESSAGE_QUERY, requestFirstTime=" + booleanExtra);
                    if (booleanExtra) {
                        new ArrayList(StatusBar.this.mNotificationData.getActiveNotifications()).stream().filter(new Predicate(ConcurrentHashMap.newKeySet()) {
                            private final /* synthetic */ Set f$0;

                            {
                                this.f$0 = r1;
                            }

                            public final boolean test(Object obj) {
                                return this.f$0.add(((NotificationData.Entry) obj).notification.getPackageName());
                            }
                        }).forEach(new Consumer() {
                            public final void accept(Object obj) {
                                StatusBar.AnonymousClass94.this.lambda$onReceive$1$StatusBar$94((NotificationData.Entry) obj);
                            }
                        });
                    }
                }
            }

            public /* synthetic */ void lambda$onReceive$1$StatusBar$94(NotificationData.Entry entry) {
                StatusBar.this.updateAppBadgeNum(entry.notification);
            }
        };
        this.mEnableNotificationsReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (!checkSender(intent.getSender())) {
                    Log.d("StatusBar", "enable notifications receiver: invalid sender");
                    return;
                }
                String stringExtra = intent.getStringExtra("pkg");
                boolean booleanExtra = intent.getBooleanExtra("enabled", true);
                if (checkParams(stringExtra)) {
                    Log.d("StatusBar", "enable notifications receiver: pkg=" + stringExtra + ", enabled=" + booleanExtra);
                    NotificationSettingsHelper.setNotificationsEnabledForPackage(context, stringExtra, booleanExtra);
                }
            }

            private boolean checkSender(String str) {
                return TextUtils.equals("com.android.systemui", str) || TextUtils.equals(c.a, str);
            }

            private boolean checkParams(String str) {
                if (!TextUtils.isEmpty(str)) {
                    return true;
                }
                Log.d("StatusBar", "enable notifications receiver: empty pkg");
                return false;
            }
        };
        this.mNotificationListener = new NotificationListenerService() {
            public void onListenerConnected() {
                Log.d("StatusBar", "onListenerConnected");
                final StatusBarNotification[] activeNotifications = getActiveNotifications();
                if (activeNotifications == null) {
                    Log.w("StatusBar", "onListenerConnected unable to get active notifications.");
                    return;
                }
                final NotificationListenerService.RankingMap currentRanking = getCurrentRanking();
                StatusBar.this.mHandler.post(new Runnable() {
                    public void run() {
                        for (StatusBarNotification statusBarNotification : activeNotifications) {
                            MiuiNotificationCompat.setEnableFloat(statusBarNotification.getNotification(), false);
                            boolean unused = StatusBar.this.handleNotification(statusBarNotification, currentRanking, false);
                        }
                    }
                });
            }

            public void onNotificationPosted(final StatusBarNotification statusBarNotification, final NotificationListenerService.RankingMap rankingMap) {
                if (statusBarNotification != null) {
                    StatusBar.this.mHandler.post(new Runnable() {
                        public void run() {
                            StatusBar.this.processForRemoteInput(statusBarNotification.getNotification());
                            String key = statusBarNotification.getKey();
                            StatusBar.this.mKeysKeptForRemoteInput.remove(key);
                            boolean z = StatusBar.this.mNotificationData.get(key) != null;
                            if (StatusBar.ENABLE_CHILD_NOTIFICATIONS || !StatusBar.this.mGroupManager.isChildInGroupWithSummary(statusBarNotification)) {
                                boolean unused = StatusBar.this.handleNotification(statusBarNotification, rankingMap, z);
                                return;
                            }
                            if (StatusBar.DEBUG) {
                                Log.d("StatusBar", "Ignoring group child due to existing summary: " + statusBarNotification);
                            }
                            if (z) {
                                StatusBar.this.removeNotification(key, rankingMap);
                            } else {
                                StatusBar.this.mNotificationData.updateRanking(rankingMap);
                            }
                        }
                    });
                }
            }

            public void onNotificationRemoved(StatusBarNotification statusBarNotification, NotificationListenerService.RankingMap rankingMap) {
                onNotificationRemoved(statusBarNotification, rankingMap, 0);
            }

            public void onNotificationRemoved(StatusBarNotification statusBarNotification, final NotificationListenerService.RankingMap rankingMap, final int i) {
                Slog.i("StatusBar", "onNotificationRemoved key=" + statusBarNotification.getKey() + " reason=" + i);
                if (statusBarNotification != null) {
                    final String key = statusBarNotification.getKey();
                    StatusBar.this.mHandler.post(new Runnable() {
                        public void run() {
                            StatusBar.this.removeNotification(key, rankingMap, i);
                        }
                    });
                }
            }

            public void onNotificationRankingUpdate(NotificationListenerService.RankingMap rankingMap) {
                if (rankingMap != null) {
                    StatusBar.this.mHandler.removeMessages(b.e);
                    SomeArgs obtain = SomeArgs.obtain();
                    obtain.arg1 = rankingMap;
                    obtain.arg2 = Long.valueOf(SystemClock.uptimeMillis());
                    H h = StatusBar.this.mHandler;
                    h.sendMessageDelayed(h.obtainMessage(b.e, obtain), 200);
                }
            }
        };
    }

    static {
        boolean z;
        boolean z2;
        boolean z3 = true;
        if (Build.VERSION.SDK_INT > 23) {
            z3 = SystemProperties.getBoolean("debug.child_notifs", true);
        } else if (!miui.os.Build.IS_DEBUGGABLE || !SystemProperties.getBoolean("debug.child_notifs", false)) {
            z3 = false;
        }
        ENABLE_CHILD_NOTIFICATIONS = z3;
        boolean z4 = DEBUG;
        SPEW = z4;
        DEBUG_GESTURES = z4;
        DEBUG_MEDIA = z4;
        DEBUG_MEDIA_FAKE_ARTWORK = z4;
        DEBUG_WINDOW_STATE = z4;
        CHATTY = z4;
        try {
            IPackageManager asInterface = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
            z = asInterface.isOnlyCoreApps();
            z2 = PackageManagerCompat.hasSystemFeature(asInterface, "android.software.freeform_window_management", 0);
        } catch (RemoteException unused) {
            z2 = false;
            z = false;
        }
        ONLY_CORE_APPS = z;
        FREEFORM_WINDOW_MANAGEMENT = z2;
    }

    public void onSilentModeChanged(boolean z) {
        this.mQuietModeEnable = z;
        if (MiuiSettings.SilenceMode.isSupported) {
            this.mIsDNDEnabled = MiuiSettings.SilenceMode.isDNDEnabled(this.mContext);
            this.mDndWarnings.setDNDEnabled(this.mIsDNDEnabled);
            this.mIconPolicy.updateSilentModeIcon();
            this.mShouldPopup = MiuiSettings.SilenceMode.showNotification(this.mContext);
            return;
        }
        this.mIconPolicy.setQuietMode(z);
    }

    public /* synthetic */ void lambda$new$0$StatusBar(boolean z, String str) {
        updateNotifications();
    }

    public void start() {
        this.mContext.setTheme(R.style.Theme);
        this.mBgHandler = createBgHandler();
        this.mResolver = this.mContext.getContentResolver();
        RecentsEventBus.getDefault().register(this);
        this.mOLEDScreenHelper = new OLEDScreenHelper(this.mContext);
        this.mDndWarnings = new DndNotificationWarnings(this.mContext);
        this.mBubbleController = (BubbleController) Dependency.get(BubbleController.class);
        this.mBubbleController.setStatusBar(this);
        this.mBubbleController.setExpandListener(this.mBubbleExpandListener);
        this.mMiuiUpdateVersionSharedPreferences = this.mContext.getSharedPreferences("deviceProvisionUpdateTiles", 0);
        this.mNoIconsSetGone = this.mContext.getResources().getBoolean(R.bool.hide_notification_icons_if_empty);
        this.mHideAmPmForNotification = this.mContext.getResources().getBoolean(R.bool.hide_am_pm_if_show_notification_icons);
        this.mKeptOnKeyguard = this.mContext.getResources().getBoolean(R.bool.kept_notifications_on_keyguard);
        this.mKeyguardMonitor = (KeyguardMonitorImpl) Dependency.get(KeyguardMonitor.class);
        this.mSecurityManager = (SecurityManager) this.mContext.getSystemService("security");
        this.mMiuiStatusBarPrompt = (MiuiStatusBarPromptController) Dependency.get(MiuiStatusBarPromptController.class);
        this.mForegroundServiceController = (ForegroundServiceController) Dependency.get(ForegroundServiceController.class);
        this.mWindowManager = (WindowManager) this.mContext.getSystemService("window");
        this.mDisplay = this.mWindowManager.getDefaultDisplay();
        updateDisplaySize();
        this.mScrimSrcModeEnabled = this.mContext.getResources().getBoolean(R.bool.config_status_bar_scrim_behind_use_src);
        this.mTelephonyManager = TelephonyManager.getDefault();
        this.mCurrentUserId = ActivityManager.getCurrentUser();
        this.mOverlayManager = new OverlayManagerWrapper();
        this.mToggleManager = ToggleManager.createInstance(this.mContext, this.mCurrentUserId);
        this.mBarService = IStatusBarService.Stub.asInterface(ServiceManager.getService("statusbar"));
        DateTimeView.setReceiverHandler((Handler) Dependency.get(Dependency.TIME_TICK_HANDLER));
        putComponent(StatusBar.class, this);
        this.mWindowManagerService = WindowManagerGlobal.getWindowManagerService();
        this.mDevicePolicyManager = (DevicePolicyManager) this.mContext.getSystemService("device_policy");
        ((SuperSaveModeController) Dependency.get(SuperSaveModeController.class)).addCallback(this.mSuperSaveModeChangeListener);
        this.mNotificationData = new NotificationData(this);
        this.mMessagingUtil = new NotificationMessagingUtil(this.mContext);
        this.mAccessibilityManager = (AccessibilityManager) this.mContext.getSystemService("accessibility");
        this.mPowerManager = (PowerManager) this.mContext.getSystemService("power");
        this.mDeviceProvisionedController.addCallback(this.mDeviceProvisionedListener);
        registerContentObserver();
        this.mRecents = (RecentsComponent) getComponent(Recents.class);
        this.mLocale = this.mContext.getResources().getConfiguration().locale;
        this.mLayoutDirection = TextUtils.getLayoutDirectionFromLocale(this.mLocale);
        this.mUserManager = (UserManager) this.mContext.getSystemService("user");
        this.mKeyguardManager = (KeyguardManager) this.mContext.getSystemService("keyguard");
        this.mLockPatternUtils = new LockPatternUtils(this.mContext);
        this.mCommandQueue = (CommandQueue) getComponent(CommandQueue.class);
        this.mCommandQueue.addCallbacks(this);
        int[] iArr = new int[9];
        ArrayList arrayList = new ArrayList();
        ArrayList arrayList2 = new ArrayList();
        ArrayList arrayList3 = new ArrayList();
        Rect rect = new Rect();
        Rect rect2 = new Rect();
        try {
            SystemUICompat.registerStatusBar(this.mBarService, this.mCommandQueue, arrayList2, arrayList3, iArr, arrayList, rect, rect2);
        } catch (RemoteException unused) {
        }
        createAndAddWindows();
        this.mContext.registerReceiverAsUser(this.mWallpaperChangedReceiver, UserHandle.ALL, new IntentFilter("android.intent.action.WALLPAPER_CHANGED"), (String) null, (Handler) null);
        this.mWallpaperChangedReceiver.onReceive(this.mContext, (Intent) null);
        setUpPresenter();
        this.mSettingsObserver.onChange(false);
        this.mCommandQueue.disable(iArr[0], iArr[6], false);
        ArrayList arrayList4 = arrayList3;
        ArrayList arrayList5 = arrayList2;
        int[] iArr2 = iArr;
        setSystemUiVisibility(iArr[1], iArr[7], iArr[8], -1, rect, rect2);
        topAppWindowChanged(iArr2[2] != 0);
        setImeWindowStatus((IBinder) arrayList.get(0), iArr2[3], iArr2[4], iArr2[5] != 0);
        int size = arrayList5.size();
        for (int i = 0; i < size; i++) {
            this.mCommandQueue.setIcon((String) arrayList5.get(i), (StatusBarIcon) arrayList4.get(i));
        }
        try {
            this.mNotificationListener.registerAsSystemService(this.mContext, new ComponentName(this.mContext.getPackageName(), getClass().getCanonicalName()), -1);
        } catch (RemoteException e) {
            Log.e("StatusBar", "Unable to register notification listener", e);
        }
        if (DEBUG) {
            Log.d("StatusBar", String.format("init: icons=%d disabled=0x%08x lights=0x%08x menu=0x%08x imeButton=0x%08x", new Object[]{Integer.valueOf(arrayList4.size()), Integer.valueOf(iArr2[0]), Integer.valueOf(iArr2[1]), Integer.valueOf(iArr2[2]), Integer.valueOf(iArr2[3])}));
        }
        this.mContextForUser = getContextForUser(this.mCurrentUserId);
        this.mPreviousConfig = new Configuration(this.mContext.getResources().getConfiguration());
        setHeadsUpUser(this.mCurrentUserId);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("miui.intent.action.systemui.ENABLE_NOTIFICATIONS");
        this.mContext.registerReceiver(this.mEnableNotificationsReceiver, intentFilter);
        IntentFilter intentFilter2 = new IntentFilter();
        intentFilter2.addAction("android.intent.action.USER_SWITCHED");
        intentFilter2.addAction("android.intent.action.USER_ADDED");
        intentFilter2.addAction("android.intent.action.USER_PRESENT");
        this.mContext.registerReceiver(this.mBaseBroadcastReceiver, intentFilter2);
        IntentFilter intentFilter3 = new IntentFilter();
        intentFilter3.addAction("com.android.systemui.statusbar.work_challenge_unlocked_notification_action");
        intentFilter3.addAction("com.android.systemui.statusbar.banner_action_cancel");
        intentFilter3.addAction("com.android.systemui.statusbar.banner_action_setup");
        this.mContext.registerReceiver(this.mBaseBroadcastReceiver, intentFilter3, "com.android.systemui.permission.SELF", (Handler) null);
        IntentFilter intentFilter4 = new IntentFilter();
        intentFilter4.addAction("android.app.action.DEVICE_POLICY_MANAGER_STATE_CHANGED");
        intentFilter4.addAction("android.intent.action.DEVICE_LOCKED_CHANGED");
        intentFilter4.addAction("android.intent.action.USER_UNLOCKED");
        intentFilter4.addAction("android.intent.action.APPLICATION_MESSAGE_QUERY");
        this.mContext.registerReceiverAsUser(this.mAllUsersReceiver, UserHandle.ALL, intentFilter4, (String) null, (Handler) null);
        updateCurrentProfilesCache();
        IVrManagerCompat.registerListener(new IVrManagerCompat.IVrManagerCompatCallbacks() {
            public void onVrStateChanged(boolean z) {
                StatusBar.this.mVrMode = z;
            }
        });
        this.mMediaSessionManager = (MediaSessionManager) this.mContext.getSystemService("media_session");
        updateHeadsUpSetting();
        this.mIconPolicy = new PhoneStatusBarPolicy(this.mContext, this.mIconController);
        this.mGameHandsFreeObserver.onChange(false);
        this.mGameModeObserver.onChange(false);
        this.mNotificationStyleObserver.onChange(false);
        this.mCloudDataObserver.onChange(false);
        this.mScreenButtonStateObserver.onChange(false);
        this.mWakeupForNotificationObserver.onChange(false);
        this.mUserExperienceObserver.onChange(false);
        this.mMiuiOptimizationObserver.onChange(false);
        this.mAODObserver.onChange(false);
        this.mMirrorDndObserver.onChange(false);
        this.mSilentModeObserverController.addCallback(this);
        this.mUnlockMethodCache = UnlockMethodCache.getInstance(this.mContext);
        this.mUnlockMethodCache.addListener(this);
        startKeyguard();
        this.mUpdateMonitor = KeyguardUpdateMonitor.getInstance(this.mContext);
        this.mUpdateMonitor.registerCallback(this.mUpdateCallback);
        this.mFaceUnlockManager = FaceUnlockManager.getInstance();
        this.mFaceUnlockManager.registerFaceUnlockCallback(this.mFaceUnlockCallback);
        if (Constants.SUPPORT_AOD) {
            this.mDozeServiceHost = new DozeServiceHost();
            Dependency.setHost(this.mDozeServiceHost);
        }
        startAndBindAodService();
        registerDeviceProvsionedObserverForAodIfNeeded();
        this.mScreenPinningRequest = new ScreenPinningRequest(this.mContext);
        this.mFalsingManager = FalsingManager.getInstance(this.mContext);
        ((ActivityStarterDelegate) Dependency.get(ActivityStarterDelegate.class)).setActivityStarterImpl(this);
        this.mConfigurationListener = new ConfigurationController.ConfigurationListener() {
            public void onConfigChanged(Configuration configuration) {
                StatusBar.this.onConfigurationChanged(configuration);
            }

            public void onDensityOrFontScaleChanged() {
                StatusBar.this.onDensityOrFontScaleChanged();
            }
        };
        ((ConfigurationController) Dependency.get(ConfigurationController.class)).addCallback(this.mConfigurationListener);
        Settings.Global.putInt(this.mResolver, "hide_nav_bar", 0);
        Settings.Global.putInt(this.mResolver, "can_nav_bar_hide", 0);
        Settings.Global.putInt(this.mResolver, "force_immersive_nav_bar", 0);
        Settings.Global.putInt(this.mResolver, "sysui_powerui_enabled", 0);
        if (Build.VERSION.SDK_INT > 28) {
            Settings.Secure.putIntForUser(this.mResolver, "charging_sounds_enabled", 0, 0);
            Settings.Secure.putIntForUser(this.mResolver, "charging_sounds_enabled", 0, 10);
        } else {
            Settings.Global.putInt(this.mResolver, "charging_sounds_enabled", 0);
        }
        Settings.Global.putInt(this.mResolver, "music_in_white_list", 0);
        Settings.System.putInt(this.mResolver, "sysui_tuner_demo_on", 0);
        Settings.Secure.putInt(this.mResolver, "in_call_notification_enabled", this.mContext.getResources().getBoolean(R.bool.play_incall_notification) ? 1 : 0);
        MiuiSettings.System.putBoolean(this.mResolver, "navigation_bar_window_loaded", false);
        if (CustomizeUtil.HAS_NOTCH) {
            this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("force_black"), false, this.mForceBlackObserver, -1);
            this.mForceBlackObserver.onChange(false);
        }
        ((Divider) getComponent(Divider.class)).registerDockedStackExistsChangedListener(this.mDockedStackExistsChangedListener);
        final DisplayManager displayManager = (DisplayManager) this.mContext.getSystemService("display");
        displayManager.registerDisplayListener(new DisplayManager.DisplayListener() {
            public void onDisplayRemoved(int i) {
                if (Build.VERSION.SDK_INT >= 29 && i != 0) {
                    StatusBar.this.removeNavigationBar(i);
                }
            }

            public void onDisplayChanged(int i) {
                StatusBar statusBar = StatusBar.this;
                statusBar.mDisplay.getDisplayInfo(statusBar.mInfo);
                if (StatusBar.DEBUG) {
                    Log.d("StatusBar", "onDisplayChanged " + StatusBar.this.mInfo);
                }
                if (CustomizeUtil.HAS_NOTCH) {
                    int i2 = StatusBar.this.mInfo.rotation;
                    int i3 = StatusBar.this.mInfo.logicalHeight;
                    int i4 = StatusBar.this.mInfo.logicalWidth;
                    int abs = Math.abs(StatusBar.this.mNotchRotation - i2);
                    if (abs == 1 || abs == 3) {
                        int unused = StatusBar.this.mNotchRotation = i2;
                        int unused2 = StatusBar.this.mLogicalWidth = i4;
                        int unused3 = StatusBar.this.mLogicalHeight = i3;
                        if (StatusBar.this.mPreviousConfig != null) {
                            StatusBar statusBar2 = StatusBar.this;
                            if (statusBar2.isSameRotation(i2, statusBar2.mPreviousConfig.orientation)) {
                                StatusBar statusBar3 = StatusBar.this;
                                statusBar3.mHandler.removeCallbacks(statusBar3.mUpdateStausBarPaddingRunnable);
                                StatusBar statusBar4 = StatusBar.this;
                                statusBar4.mHandler.post(statusBar4.mUpdateStausBarPaddingRunnable);
                            }
                        }
                    } else if (StatusBar.this.mNotchRotation != i2 || StatusBar.this.mLogicalWidth != i4 || StatusBar.this.mLogicalHeight != i3) {
                        int unused4 = StatusBar.this.mNotchRotation = i2;
                        int unused5 = StatusBar.this.mLogicalWidth = i4;
                        int unused6 = StatusBar.this.mLogicalHeight = i3;
                        StatusBar statusBar5 = StatusBar.this;
                        statusBar5.mHandler.removeCallbacks(statusBar5.mUpdateStausBarPaddingRunnable);
                        StatusBar statusBar6 = StatusBar.this;
                        statusBar6.mHandler.post(statusBar6.mUpdateStausBarPaddingRunnable);
                    }
                }
            }

            public void onDisplayAdded(int i) {
                if (Build.VERSION.SDK_INT >= 29 && i != 0) {
                    StatusBar.this.createNavigationBar(displayManager.getDisplay(i));
                }
            }
        }, this.mBgHandler);
        this.mMiuiStatusBarPrompt.dealWithRecordState();
        SettingsJobSchedulerService.schedule(this.mContext);
        ((ToastOverlayManager) Dependency.get(ToastOverlayManager.class)).setup(this.mContext, getStatusBarWindow());
        ((OverviewProxyService) Dependency.get(OverviewProxyService.class)).startConnectionToCurrentUser();
        ((ControlsPluginManager) Dependency.get(ControlsPluginManager.class)).addControlsPluginListener();
    }

    public final void onBusEvent(MultiWindowStateChangedEvent multiWindowStateChangedEvent) {
        if (!multiWindowStateChangedEvent.inMultiWindow) {
            this.mDockedStackExistsChangedListener.onDockedStackMinimizedChanged(false);
        }
    }

    public final void onBusEvent(ScreenOffEvent screenOffEvent) {
        notifyHeadsUpScreenOff();
        if (this.mBubbleController.isStackExpanded()) {
            this.mBubbleController.collapseStack();
        }
        finishBarAnimations();
        resetUserExpandedStates();
        if (!TextUtils.isEmpty(this.mCallState)) {
            this.mMiuiStatusBarPrompt.makeReturnToInCallScreenButtonGone();
        }
        this.mOLEDScreenHelper.stop(false);
        if (isAodUsingSuperWallpaper()) {
            this.mNotificationPanel.animate().cancel();
            this.mNotificationPanel.animate().setListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animator) {
                    StatusBar.this.mNotificationPanel.setVisibility(4);
                }
            }).alpha(0.0f).setDuration(500).start();
        }
    }

    public final void onBusEvent(ScreenOnEvent screenOnEvent) {
        if (isAodUsingSuperWallpaper()) {
            this.mNotificationPanel.animate().cancel();
            this.mNotificationPanel.animate().setListener(new AnimatorListenerAdapter() {
                public void onAnimationStart(Animator animator) {
                    StatusBar.this.mNotificationPanel.setVisibility(0);
                }
            }).alpha(1.0f).setDuration(500).start();
        }
        if (!TextUtils.isEmpty(this.mCallState)) {
            if (InCallUtils.isInCallScreenShowing(this.mContext) || InCallUtils.isCallScreenShowing(this.mContext)) {
                this.mCallState = "";
                hideReturnToInCallScreenButton();
            } else {
                this.mMiuiStatusBarPrompt.makeReturnToInCallScreenButtonVisible();
            }
        }
        this.mOLEDScreenHelper.start(true);
    }

    private void changeNavBarViewState() {
        if (this.mIsFsgMode) {
            Log.d("StatusBar", "NOTICE: full screen gesture function close");
            if (isHideGestureLine()) {
                Log.d("StatusBar", "NOTICE: full screen gesture line hide");
                removeNavBarView();
            } else {
                Log.d("StatusBar", "NOTICE: full screen gesture line show");
                addNavigationBar();
                NavigationBarView navigationBarView = this.mNavigationBarView;
                if (navigationBarView != null) {
                    navigationBarView.getCurrentView().setVisibility(8);
                    this.mNavigationBarView.getNavigationHandle().setVisibility(0);
                }
            }
        } else {
            Log.d("StatusBar", "NOTICE: full screen gesture function open");
            addNavigationBar();
            NavigationBarView navigationBarView2 = this.mNavigationBarView;
            if (navigationBarView2 != null) {
                navigationBarView2.getCurrentView().setVisibility(0);
                this.mNavigationBarView.getNavigationHandle().setVisibility(8);
            }
        }
        NavigationBarView navigationBarView3 = this.mNavigationBarView;
        if (navigationBarView3 != null) {
            navigationBarView3.updateNotTouchable();
            this.mNavigationBarView.updateBackgroundColor();
        }
        updateStatusBarPading();
    }

    public final void onBusEvent(UseFsGestureVersionThreeChangedEvent useFsGestureVersionThreeChangedEvent) {
        this.mIsUseFsGestureVersionThree = useFsGestureVersionThreeChangedEvent.mUseFsGestureVersionThree;
        onFsGestureStateChange();
    }

    public boolean isHideGestureLine() {
        return this.mHideGestureLine || !this.mIsUseFsGestureVersionThree;
    }

    public boolean isFullScreenGestureMode() {
        return this.mIsFsgMode;
    }

    /* access modifiers changed from: package-private */
    public void createNavigationBar(Display display) {
        if (display != null && (display.getFlags() & 4) == 0) {
            int displayId = display.getDisplayId();
            try {
                if (IWindowManagerCompat.hasNavigationBar(this.mWindowManagerService, displayId)) {
                    if (!(displayId == 0)) {
                        Context createDisplayContext = this.mContext.createDisplayContext(display);
                        NavigationBarView navigationBarView = (NavigationBarView) View.inflate(createDisplayContext, R.layout.navigation_bar, (ViewGroup) null);
                        this.mNavigationBars.append(displayId, navigationBarView);
                        ((WindowManager) createDisplayContext.getSystemService(WindowManager.class)).addView(navigationBarView, getNavigationBarLayoutParams());
                    }
                }
            } catch (RemoteException unused) {
                Log.w("StatusBar", "Cannot get WindowManager.");
            }
        }
    }

    /* access modifiers changed from: private */
    public void removeNavigationBar(int i) {
        NavigationBarView navigationBarView = this.mNavigationBars.get(i);
        if (navigationBarView != null) {
            WindowManagerGlobal.getInstance().removeView(navigationBarView, true);
            this.mNavigationBars.remove(i);
        }
    }

    private void removeNavBarView() {
        NavigationBarView navigationBarView = this.mNavigationBarView;
        if (navigationBarView != null && !this.mIsRemoved) {
            this.mIsRemoved = true;
            try {
                this.mWindowManager.removeViewImmediate(navigationBarView);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private WindowManager.LayoutParams getNavigationBarLayoutParams() {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(-1, -1, 2019, 8650856, -3);
        if (ActivityManager.isHighEndGfx()) {
            layoutParams.flags |= 16777216;
        }
        layoutParams.setTitle("NavigationBar");
        layoutParams.windowAnimations = 0;
        return layoutParams;
    }

    private void repositionNavigationBar() {
        NavigationBarView navigationBarView = this.mNavigationBarView;
        if (navigationBarView != null && navigationBarView.isAttachedToWindow()) {
            if (!this.mIsFsgMode || isHideGestureLine()) {
                prepareNavigationBarView();
                WindowManager windowManager = this.mWindowManager;
                NavigationBarView navigationBarView2 = this.mNavigationBarView;
                windowManager.updateViewLayout(navigationBarView2, navigationBarView2.getLayoutParams());
            }
        }
    }

    private void prepareNavigationBarView() {
        this.mNavigationBarView.reorient();
        this.mNavigationBarView.getRecentsButton().setOnClickListener(this.mRecentsClickListener);
    }

    /* access modifiers changed from: private */
    public void addNavigationBar() {
        if (DEBUG) {
            Log.v("StatusBar", "addNavigationBar: about to add " + this.mNavigationBarView);
        }
        if (this.mNavigationBarView != null && !this.mNavigationBarLoaded) {
            if ((!this.mIsFsgMode || !isHideGestureLine()) && this.mIsRemoved) {
                prepareNavigationBarView();
                this.mNavigationBarView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    public void onGlobalLayout() {
                        int[] iArr = new int[2];
                        StatusBar.this.mNavigationBarView.getLocationOnScreen(iArr);
                        int unused = StatusBar.this.mNavigationBarYPostion = iArr[1];
                    }
                });
                if (!this.mNavigationBarView.isAttachedToWindow()) {
                    try {
                        this.mNavigationBarView.setLayoutDirection(2);
                        this.mWindowManager.addView(this.mNavigationBarView, getNavigationBarLayoutParams());
                        this.mNavigationBarLoaded = true;
                        MiuiSettings.System.putBoolean(this.mContext.getContentResolver(), "navigation_bar_window_loaded", true);
                        this.mIsRemoved = false;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public int getNavigationBarYPosition() {
        return this.mNavigationBarYPostion;
    }

    /* access modifiers changed from: protected */
    public void onBootCompleted() {
        super.onBootCompleted();
        sBootCompleted = true;
        Log.d("StatusBar", "boot complete");
    }

    /* access modifiers changed from: private */
    public Context getContextForUser(int i) {
        Context context = this.mContext;
        if (i < 0) {
            return context;
        }
        try {
            return context.createPackageContextAsUser(context.getPackageName(), 4, new UserHandle(i));
        } catch (PackageManager.NameNotFoundException unused) {
            return context;
        }
    }

    private void registerContentObserver() {
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("zen_mode"), false, this.mSettingsObserver);
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("lock_screen_show_notifications"), false, this.mLockscreenSettingsObserver, -1);
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("status_bar_show_notification_icon"), false, this.mShowNotificationIconObserver, -1);
        if (ENABLE_LOCK_SCREEN_ALLOW_REMOTE_INPUT) {
            this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("lock_screen_allow_remote_input"), false, this.mSettingsObserver, -1);
        }
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("lock_screen_allow_private_notifications"), true, this.mLockscreenSettingsObserver, -1);
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("gb_notification"), false, this.mGameModeObserver, -1);
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("gb_handsfree"), false, this.mGameHandsFreeObserver, -1);
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("miui_optimization"), false, this.mMiuiOptimizationObserver, -1);
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("status_bar_notification_style"), false, this.mNotificationStyleObserver, -1);
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("user_fold"), false, this.mUserFoldObserver, -1);
        this.mContext.getContentResolver().registerContentObserver(NotificationProvider.URI_FOLD_IMPORTANCE, false, this.mFoldImportanceObserver, -1);
        this.mContext.getContentResolver().registerContentObserver(CloudDataHelper.URI_CLOUD_ALL_DATA_NOTIFY, false, this.mCloudDataObserver, -1);
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("wakeup_for_keyguard_notification"), false, this.mWakeupForNotificationObserver, -1);
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("screen_buttons_state"), false, this.mScreenButtonStateObserver, -1);
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("sc_status"), false, this.mSliderStatusObserver, -1);
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("upload_log_pref"), false, this.mUserExperienceObserver, -1);
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("navigation_bar_window_loaded"), false, this.mNavigationBarWindowLoadedObserver);
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("access_control_lock_enabled"), false, this.mAccessControlLockObserver, -1);
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor(MiuiKeyguardUtils.AOD_MODE), false, this.mAODObserver, -1);
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("aod_using_super_wallpaper"), false, this.mAODObserver, -1);
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("miui_mirror_dnd_mode"), false, this.mMirrorDndObserver, -1);
    }

    /* access modifiers changed from: protected */
    public void makeStatusBarView() {
        Context context = this.mContext;
        updateDisplaySize();
        updateResources(false);
        CustomizedUtils.setCustomized(this.mContext);
        TelephonyIcons.initDataTypeName(context);
        inflateStatusBarWindow(context);
        this.mStatusBarWindow.setService(this);
        this.mStatusBarWindow.setOnTouchListener(getStatusBarWindowTouchListener());
        this.mNotificationPanel = (NotificationPanelView) this.mStatusBarWindow.findViewById(R.id.notification_panel);
        this.mStackScroller = (NotificationStackScrollLayout) this.mStatusBarWindow.findViewById(R.id.notification_stack_scroller);
        this.mNotificationLogger.setUp(this, this.mNotificationData, this.mStackScroller);
        this.mNotificationPanel.setStatusBar(this);
        this.mNotificationPanel.setGroupManager(this.mGroupManager);
        this.mKeyguardStatusBar = (KeyguardStatusBarView) this.mStatusBarWindow.findViewById(R.id.keyguard_header);
        this.mNotificationIconAreaController = SystemUIFactory.getInstance().createNotificationIconAreaController(context, this);
        inflateShelf();
        this.mNotificationIconAreaController.setupShelf(this.mNotificationShelf);
        ((DarkIconDispatcher) Dependency.get(DarkIconDispatcher.class)).addDarkReceiver((DarkIconDispatcher.DarkReceiver) this.mNotificationIconAreaController);
        createStatusBarFragment();
        if (!ActivityManager.isHighEndGfx()) {
            this.mStatusBarWindow.setBackground((Drawable) null);
            this.mNotificationPanel.setBackground(new FastColorDrawable(context.getColor(17170443)));
        }
        this.mHeadsUpManager = new HeadsUpManager(context, this.mStatusBarWindow, this.mGroupManager, this);
        ((ConfigurationController) Dependency.get(ConfigurationController.class)).addCallback(this.mHeadsUpManager);
        this.mHeadsUpManager.addListener(this);
        this.mHeadsUpManager.addListener(this.mNotificationPanel);
        this.mHeadsUpManager.addListener(this.mGroupManager);
        this.mHeadsUpManager.addListener(this.mVisualStabilityManager);
        this.mNotificationPanel.setHeadsUpManager(this.mHeadsUpManager);
        this.mAppMiniWindowManager = new AppMiniWindowManager(context, this);
        this.mAppMiniWindowManager.setHeadsUpManager(this.mHeadsUpManager);
        this.mNotificationPanel.setAppMiniWindowManager(this.mAppMiniWindowManager);
        this.mNotificationData.setHeadsUpManager(this.mHeadsUpManager);
        this.mGroupManager.setHeadsUpManager(this.mHeadsUpManager);
        this.mHeadsUpManager.setVisualStabilityManager(this.mVisualStabilityManager);
        this.mIsFsgMode = MiuiSettings.Global.getBoolean(this.mContext.getContentResolver(), "force_fsg_nav_bar");
        RecentsComponent recentsComponent = this.mRecents;
        if (recentsComponent != null) {
            this.mIsUseFsGestureVersionThree = ((Recents) recentsComponent).useFsGestureVersionThree();
        }
        this.mHideGestureLine = Settings.Global.getInt(this.mContext.getContentResolver(), "hide_gesture_line", 0) != 0;
        this.mIsRemoved = true;
        try {
            final boolean hasNavigationBar = IWindowManagerCompat.hasNavigationBar(this.mWindowManagerService, ContextCompat.getDisplayId(this.mContext));
            if (DEBUG) {
                Log.v("StatusBar", "hasNavigationBar=" + hasNavigationBar);
            }
            if (hasNavigationBar) {
                createNavigationBar();
            }
            if (hasNavigationBar || Constants.SUPPORT_LAB_GESTURE) {
                this.mFullScreenGestureListener = new ContentObserver(this.mHandler) {
                    public void onChange(boolean z) {
                        StatusBar statusBar = StatusBar.this;
                        boolean unused = statusBar.mIsFsgMode = MiuiSettings.Global.getBoolean(statusBar.mContext.getContentResolver(), "force_fsg_nav_bar");
                        if (StatusBar.this.mNavigationBarView != null && !StatusBar.this.mIsFsgMode) {
                            StatusBar.this.mNavigationBarView.updateElderlyMode();
                        }
                        StatusBar.this.onFsGestureStateChange();
                        if (!hasNavigationBar) {
                            StatusBar.this.processScreenBtnDisableNotification();
                        }
                    }
                };
                this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("force_fsg_nav_bar"), false, this.mFullScreenGestureListener);
                this.mFullScreenGestureListener.onChange(false);
                if (Build.VERSION.SDK_INT >= 29) {
                    this.mHideGestureLineObserver = new ContentObserver(this.mHandler) {
                        public void onChange(boolean z) {
                            StatusBar statusBar = StatusBar.this;
                            boolean z2 = false;
                            if (Settings.Global.getInt(statusBar.mContext.getContentResolver(), "hide_gesture_line", 0) != 0) {
                                z2 = true;
                            }
                            boolean unused = statusBar.mHideGestureLine = z2;
                            StatusBar.this.onFsGestureStateChange();
                        }
                    };
                    this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("hide_gesture_line"), false, this.mHideGestureLineObserver);
                    this.mHideGestureLineObserver.onChange(false);
                }
            }
            if (hasNavigationBar) {
                addNavigationBar();
            }
        } catch (RemoteException unused) {
        }
        this.mPixelFormat = -1;
        this.mStackScroller.setLongPressListener(getNotificationLongClicker());
        this.mStackScroller.setMenuPressListener(getNotificationMenuClicker());
        this.mStackScroller.setStatusBar(this);
        this.mStackScroller.setGroupManager(this.mGroupManager);
        this.mStackScroller.setHeadsUpManager(this.mHeadsUpManager);
        this.mGroupManager.setOnGroupChangeListener(this.mStackScroller);
        this.mVisualStabilityManager.setVisibilityLocationProvider(this.mStackScroller);
        inflateEmptyShadeView();
        inflateDismissView();
        inflateHeadsUpStubView();
        this.mExpandedContents = this.mStackScroller;
        this.mBackdrop = (BackDropView) this.mStatusBarWindow.findViewById(R.id.backdrop);
        this.mBackdropFront = (ImageView) this.mBackdrop.findViewById(R.id.backdrop_front);
        this.mBackdropBack = (ImageView) this.mBackdrop.findViewById(R.id.backdrop_back);
        this.mKeyguardClock = (KeyguardClockContainer) this.mStatusBarWindow.findViewById(R.id.keyguard_clock_view);
        this.mKeyguardBottomArea = (KeyguardBottomAreaView) this.mStatusBarWindow.findViewById(R.id.keyguard_bottom_area);
        this.mKeyguardIndicationController = SystemUIFactory.getInstance().createKeyguardIndicationController(this.mContext, this.mNotificationPanel);
        this.mNotificationPanel.setKeyguardIndicationController(this.mKeyguardIndicationController);
        this.mKeyguardBottomArea.setKeyguardIndicationController(this.mKeyguardIndicationController);
        setAreThereNotifications();
        this.mBatteryController.addCallback(new BatteryController.BatteryStateChangeCallback() {
            public void onBatteryStyleChanged(int i) {
            }

            public void onPowerSaveChanged(boolean z) {
                StatusBar statusBar = StatusBar.this;
                statusBar.mHandler.post(statusBar.mCheckBarModes);
            }

            public void onExtremePowerSaveChanged(boolean z) {
                StatusBar statusBar = StatusBar.this;
                statusBar.mHandler.post(statusBar.mCheckBarModes);
            }

            public void onBatteryLevelChanged(int i, boolean z, boolean z2) {
                int unused = StatusBar.this.mBatteryLevel = i;
            }
        });
        this.mLightBarController = (LightBarController) Dependency.get(LightBarController.class);
        this.mScrimController = SystemUIFactory.getInstance().createScrimController(this.mLightBarController, (ScrimView) this.mStatusBarWindow.findViewById(R.id.scrim_behind), (ScrimView) this.mStatusBarWindow.findViewById(R.id.scrim_in_front), this.mStatusBarWindow.findViewById(R.id.heads_up_scrim), this.mLockscreenWallpaper);
        if (this.mScrimSrcModeEnabled) {
            AnonymousClass24 r1 = new Runnable() {
                public void run() {
                    boolean z = StatusBar.this.mBackdrop.getVisibility() != 0;
                    StatusBar.this.mScrimController.setDrawBehindAsSrc(z);
                    StatusBar.this.mStackScroller.setDrawBackgroundAsSrc(z);
                }
            };
            this.mBackdrop.setOnVisibilityChangedRunnable(r1);
            r1.run();
        }
        this.mHeadsUpManager.addListener(this.mScrimController);
        this.mStackScroller.setScrimController(this.mScrimController);
        this.mDozeScrimController = new DozeScrimController(this.mScrimController, context);
        ((MiuiKeyguardWallpaperController) Dependency.get(MiuiKeyguardWallpaperController.class)).setWallpaperScrim(this.mStatusBarWindow.findViewById(R.id.wallpaper_scrim));
        this.mVolumeComponent = (VolumeComponent) getComponent(VolumeComponent.class);
        this.mKeyguardBottomArea.setStatusBar(this);
        this.mKeyguardBottomArea.setUserSetupComplete(this.mUserSetup);
        if (UserManager.get(this.mContext).isUserSwitcherEnabled()) {
            createUserSwitcher();
        }
        View findViewById = this.mStatusBarWindow.findViewById(R.id.qs_frame);
        if (findViewById != null) {
            FragmentHostManager fragmentHostManager = FragmentHostManager.get(findViewById);
            fragmentHostManager.getFragmentManager().beginTransaction().replace(R.id.qs_frame, new QSFragment(), QS.TAG).commit();
            new PluginFragmentListener(findViewById, QS.TAG, QSFragment.class, QS.class).startListening();
            this.mQSTileHost = SystemUIFactory.getInstance().createQSTileHost(this.mContext, this, this.mIconController);
            this.mQSTileHost.init();
            this.mBrightnessMirrorController = new BrightnessMirrorController(this.mStatusBarWindow);
            fragmentHostManager.addTagListener(QS.TAG, new FragmentHostManager.FragmentListener() {
                public void onFragmentViewDestroyed(String str, Fragment fragment) {
                }

                public void onFragmentViewCreated(String str, Fragment fragment) {
                    QS qs = (QS) fragment;
                    if (qs instanceof QSFragment) {
                        QSFragment unused = StatusBar.this.mQSFragment = (QSFragment) qs;
                        StatusBar.this.mQSFragment.setHost(StatusBar.this.mQSTileHost);
                        StatusBar.this.mQSFragment.setBrightnessMirror(StatusBar.this.mBrightnessMirrorController);
                        StatusBar statusBar = StatusBar.this;
                        statusBar.mQSPanel = statusBar.mQSFragment.getQSPanel();
                        StatusBar statusBar2 = StatusBar.this;
                        statusBar2.mQuickQSPanel = statusBar2.mQSFragment.getQuickQSPanel();
                        StatusBar statusBar3 = StatusBar.this;
                        statusBar3.mHeader = (QuickStatusBarHeader) statusBar3.mQSFragment.getHeader();
                        StatusBar.this.mHeader.themeChanged();
                        StatusBar.this.mHeader.regionChanged();
                    }
                }
            });
        }
        this.mReportRejectedTouch = this.mStatusBarWindow.findViewById(R.id.report_rejected_touch);
        if (this.mReportRejectedTouch != null) {
            updateReportRejectedTouchVisibility();
            this.mReportRejectedTouch.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    Uri reportRejectedTouch = StatusBar.this.mFalsingManager.reportRejectedTouch();
                    if (reportRejectedTouch != null) {
                        StringWriter stringWriter = new StringWriter();
                        stringWriter.write("Build info: ");
                        stringWriter.write(SystemProperties.get("ro.build.description"));
                        stringWriter.write("\nSerial number: ");
                        stringWriter.write(SystemProperties.get("ro.serialno"));
                        stringWriter.write("\n");
                        PrintWriter printWriter = new PrintWriter(stringWriter);
                        FalsingLog.dump(printWriter);
                        printWriter.flush();
                        StatusBar.this.startActivityDismissingKeyguard(Intent.createChooser(new Intent("android.intent.action.SEND").setType("*/*").putExtra("android.intent.extra.SUBJECT", "Rejected touch report").putExtra("android.intent.extra.STREAM", reportRejectedTouch).putExtra("android.intent.extra.TEXT", stringWriter.toString()), "Share rejected touch report").addFlags(268435456), true, true);
                    }
                }
            });
        }
        PowerManager powerManager = (PowerManager) this.mContext.getSystemService("power");
        if (!powerManager.isScreenOn()) {
            onBusEvent(new ScreenOffEvent());
        }
        this.mGestureWakeLock = powerManager.newWakeLock(10, "GestureWakeLock");
        this.mVibrator = (Vibrator) this.mContext.getSystemService(Vibrator.class);
        int[] intArray = this.mContext.getResources().getIntArray(R.array.config_cameraLaunchGestureVibePattern);
        this.mCameraLaunchGestureVibePattern = new long[intArray.length];
        for (int i = 0; i < intArray.length; i++) {
            this.mCameraLaunchGestureVibePattern[i] = (long) intArray[i];
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.CLOSE_SYSTEM_DIALOGS");
        intentFilter.addAction("android.app.action.SHOW_DEVICE_MONITORING_DIALOG");
        context.registerReceiverAsUser(this.mBroadcastReceiver, UserHandle.ALL, intentFilter, (String) null, (Handler) null);
        IntentFilter intentFilter2 = new IntentFilter();
        intentFilter2.addAction("android.intent.action.LEAVE_INCALL_SCREEN_DURING_CALL");
        intentFilter2.addAction("android.intent.action.ENTER_INCALL_SCREEN_DURING_CALL");
        intentFilter2.addAction("miui.intent.action.MIUI_REGION_CHANGED");
        intentFilter2.addAction("com.miui.app.ExtraStatusBarManager.action_enter_drive_mode");
        intentFilter2.addAction("com.miui.app.ExtraStatusBarManager.action_leave_drive_mode");
        intentFilter2.addAction("com.miui.app.ExtraStatusBarManager.action_refresh_notification");
        intentFilter2.addAction("com.miui.app.ExtraStatusBarManager.action_remove_keyguard_notification");
        context.registerReceiverAsUser(this.mInternalBroadcastReceiver, UserHandle.ALL, intentFilter2, "miui.permission.USE_INTERNAL_GENERAL_API", this.mHandler);
        if (DEBUG_MEDIA_FAKE_ARTWORK) {
            IntentFilter intentFilter3 = new IntentFilter();
            intentFilter3.addAction("fake_artwork");
            context.registerReceiverAsUser(this.mFakeArtworkReceiver, UserHandle.ALL, intentFilter3, "android.permission.DUMP", (Handler) null);
        }
        IntentFilter intentFilter4 = new IntentFilter();
        intentFilter4.addAction("action_panels_operation");
        context.registerReceiverAsUser(this.mMiuiRemoteOperationReceiver, UserHandle.ALL, intentFilter4, "android.permission.EXPAND_STATUS_BAR", (Handler) null);
        ((DemoModeController) Dependency.get(DemoModeController.class)).addCallback(this.mDemoCallback);
        IntentFilter intentFilter5 = new IntentFilter();
        intentFilter5.addAction("com.miui.app.ExtraStatusBarManager.TRIGGER_TOGGLE_SCREEN_BUTTONS");
        intentFilter5.addAction("com.miui.app.ExtraStatusBarManager.TRIGGER_TOGGLE_LOCK");
        intentFilter5.addAction("com.miui.app.ExtraStatusBarManager.action_TRIGGER_TOGGLE");
        this.mContext.registerReceiverAsUser(this.mToggleBroadcastReceiver, UserHandle.ALL, intentFilter5, "com.android.SystemUI.permission.TIGGER_TOGGLE", this.mHandler);
        this.mDeviceProvisionedController.addCallback(this.mUserSetupObserver);
        this.mUserSetupObserver.onUserSetupChanged();
        ThreadedRenderer.overrideProperty("disableProfileBars", "true");
        ThreadedRenderer.overrideProperty("ambientRatio", String.valueOf(1.5f));
    }

    private void setUpPresenter() {
        this.mActivityLaunchAnimator = new MiuiActivityLaunchAnimator(this.mStatusBarWindow, this, this.mNotificationPanel, this.mHeadsUpAnimatedStub);
        Context context = this.mContext;
        AssistManager assistManager = this.mAssistManager;
        NotificationData notificationData = this.mNotificationData;
        LockPatternUtils lockPatternUtils = this.mLockPatternUtils;
        HeadsUpManager headsUpManager = this.mHeadsUpManager;
        KeyguardManager keyguardManager = this.mKeyguardManager;
        NotificationGroupManager notificationGroupManager = this.mGroupManager;
        H h = this.mHandler;
        Handler handler = this.mBgHandler;
        IStatusBarService iStatusBarService = this.mBarService;
        BubbleController bubbleController = this.mBubbleController;
        BubbleController bubbleController2 = bubbleController;
        MiuiNotificationActivityStarter miuiNotificationActivityStarter = r0;
        MiuiNotificationActivityStarter miuiNotificationActivityStarter2 = new MiuiNotificationActivityStarter(context, assistManager, this, notificationData, lockPatternUtils, headsUpManager, keyguardManager, notificationGroupManager, this, h, handler, this, iStatusBarService, bubbleController2, this.mActivityLaunchAnimator);
        this.mNotificationActivityStarter = miuiNotificationActivityStarter;
        this.mNotificationClicker = new NotificationClicker(this, this.mBubbleController, this.mNotificationActivityStarter, this.mHeadsUpManager);
    }

    public void updateQSTileHost(boolean z) {
        if (z) {
            this.mQSTileHost.destroy();
            return;
        }
        this.mQSTileHost = SystemUIFactory.getInstance().createQSTileHost(this.mContext, this, this.mIconController);
        this.mQSTileHost.init();
        QSFragment qSFragment = this.mQSFragment;
        if (qSFragment != null) {
            qSFragment.setHost(this.mQSTileHost);
        }
    }

    /* access modifiers changed from: private */
    public void onFragmentUpdate(Fragment fragment) {
        this.mStatusBarFragment = (CollapsedStatusBarFragment) fragment;
        this.mStatusBarFragment.initNotificationIconArea(this.mNotificationIconAreaController);
        this.mStatusBarFragment.updatePromptLayout();
        PhoneStatusBarView phoneStatusBarView = this.mStatusBarView;
        if (phoneStatusBarView != null) {
            phoneStatusBarView.clearPrompt();
        }
        this.mStatusBarView = (PhoneStatusBarView) fragment.getView();
        this.mNotifications = this.mStatusBarView.findViewById(R.id.notification_icon_area);
        this.mDriveModeBg = (LinearLayout) this.mStatusBarView.findViewById(R.id.drivemodebg);
        this.mStatusBarView.setBar(this);
        this.mStatusBarView.setPrompt(((StatusBarTypeController) Dependency.get(StatusBarTypeController.class)).getCutoutType().name());
        this.mStatusBarView.setPanel(this.mNotificationPanel);
        this.mStatusBarView.setScrimController(this.mScrimController);
        this.mMiuiStatusBarPrompt.setHandler(this.mStatusBarView.getHandler());
        this.mMiuiStatusBarPrompt.showReturnToDriveModeView(this.mMiuiStatusBarPrompt.isShowingState("legacy_drive"), this.mIsInDriveModeMask);
        if (!TextUtils.isEmpty(this.mCallState)) {
            showReturnToInCallScreenButton(this.mCallState, this.mCallBaseTime);
        }
        if ("legacy_recorder".equals(this.mMiuiStatusBarPrompt.getStatusBarModeState())) {
            this.mMiuiStatusBarPrompt.forceRefreshRecorder();
        }
        this.mStatusBarWindow.setStatusBarView(this.mStatusBarView);
        this.mStatusBarWindow.setKeyguardStatusBarView(this.mKeyguardStatusBar);
        setAreThereNotifications();
        checkBarModes();
        this.mShowNotificationIconObserver.onChange(false);
        updateDriveMode();
        updateStatusBarPading();
        this.mOLEDScreenHelper.setStatusBarView(this.mStatusBarView);
    }

    private void createStatusBarFragment() {
        this.statusBarFragmentContainer = this.mStatusBarWindow.findViewById(R.id.status_bar_container);
        switchStatusBarFragment(((StatusBarTypeController) Dependency.get(StatusBarTypeController.class)).getCutoutType());
        ((StatusBarTypeController) Dependency.get(StatusBarTypeController.class)).addCallback(new StatusBarTypeController.StatusBarTypeChangeListener() {
            public void onCutoutTypeChanged() {
                if (StatusBar.this.mNotificationIconAreaController != null) {
                    StatusBar.this.mNotificationIconAreaController.release();
                    ((DarkIconDispatcher) Dependency.get(DarkIconDispatcher.class)).removeDarkReceiver((DarkIconDispatcher.DarkReceiver) StatusBar.this.mNotificationIconAreaController);
                }
                StatusBar statusBar = StatusBar.this;
                SystemUIFactory instance = SystemUIFactory.getInstance();
                StatusBar statusBar2 = StatusBar.this;
                NotificationIconAreaController unused = statusBar.mNotificationIconAreaController = instance.createNotificationIconAreaController(statusBar2.mContext, statusBar2);
                StatusBar.this.mNotificationIconAreaController.setupShelf(StatusBar.this.mNotificationShelf);
                ((DarkIconDispatcher) Dependency.get(DarkIconDispatcher.class)).addDarkReceiver((DarkIconDispatcher.DarkReceiver) StatusBar.this.mNotificationIconAreaController);
                StatusBar.this.switchStatusBarFragment(((StatusBarTypeController) Dependency.get(StatusBarTypeController.class)).getCutoutType());
            }
        });
    }

    /* access modifiers changed from: private */
    public void switchStatusBarFragment(StatusBarTypeController.CutoutType cutoutType) {
        String fragmentTagByType = getFragmentTagByType(cutoutType);
        Fragment findFragmentByTag = FragmentHostManager.get(this.statusBarFragmentContainer, true).getFragmentManager().findFragmentByTag(fragmentTagByType);
        Log.d("StatusBar", "switchStatusBarFragment cutouttype: " + cutoutType + " targetFragment: " + findFragmentByTag);
        if (findFragmentByTag == null) {
            AnonymousClass28 r1 = new FragmentHostManager.FragmentListener() {
                public void onFragmentViewDestroyed(String str, Fragment fragment) {
                }

                public void onFragmentViewCreated(String str, Fragment fragment) {
                    if (((CollapsedStatusBarFragment) fragment).getCutoutType() == ((StatusBarTypeController) Dependency.get(StatusBarTypeController.class)).getCutoutType()) {
                        StatusBar.this.onFragmentUpdate(fragment);
                    }
                }
            };
            CollapsedStatusBarFragment newInstance = CollapsedStatusBarFragment.newInstance(cutoutType);
            FragmentHostManager fragmentHostManager = FragmentHostManager.get(this.statusBarFragmentContainer, true);
            fragmentHostManager.addTagListener(fragmentTagByType, r1);
            fragmentHostManager.getFragmentManager().beginTransaction().add(R.id.status_bar_container, newInstance, fragmentTagByType).commit();
        } else {
            onFragmentUpdate(findFragmentByTag);
        }
        updateFragmentsVisibility();
    }

    private void updateFragmentsVisibility() {
        StatusBarTypeController.CutoutType cutoutType = ((StatusBarTypeController) Dependency.get(StatusBarTypeController.class)).getCutoutType();
        FragmentManager fragmentManager = FragmentHostManager.get(this.statusBarFragmentContainer, true).getFragmentManager();
        FragmentTransaction beginTransaction = fragmentManager.beginTransaction();
        Log.d("StatusBar", "updateFragmentsVisibility cutouttype: " + cutoutType);
        for (StatusBarTypeController.CutoutType cutoutType2 : StatusBarTypeController.CutoutType.values()) {
            Fragment findFragmentByTag = fragmentManager.findFragmentByTag(getFragmentTagByType(cutoutType2));
            if (findFragmentByTag != null) {
                if (cutoutType2 == cutoutType) {
                    beginTransaction.show(findFragmentByTag);
                } else {
                    beginTransaction.hide(findFragmentByTag);
                }
            }
        }
        beginTransaction.commit();
    }

    private String getFragmentTagByType(StatusBarTypeController.CutoutType cutoutType) {
        return "CollapsedStatusBarFragment" + cutoutType.name();
    }

    /* access modifiers changed from: protected */
    public void createNavigationBar() {
        removeNavBarView();
        this.mNavigationBarView = (NavigationBarView) View.inflate(this.mContext, R.layout.navigation_bar, (ViewGroup) null);
        this.mNavigationBarView.disableChangeBg(CustomizeUtil.forceLayoutHideNavigation(Util.getTopActivityPkg(this.mContext, true)));
        this.mNavigationBarView.setDisabledFlags(this.mDisabled1);
        this.mNavigationBarView.setBar(this);
        this.mOLEDScreenHelper.setNavigationBarView(this.mNavigationBarView);
    }

    /* access modifiers changed from: protected */
    public View.OnTouchListener getStatusBarWindowTouchListener() {
        return new View.OnTouchListener() {
            public boolean onTouch(View view, MotionEvent motionEvent) {
                StatusBar.this.checkUserAutohide(view, motionEvent);
                StatusBar.this.checkRemoteInputOutside(motionEvent);
                if (motionEvent.getAction() == 0) {
                    StatusBar statusBar = StatusBar.this;
                    if (statusBar.mExpandedVisible && !statusBar.mNotificationPanel.isQsDetailShowing()) {
                        StatusBar.this.animateCollapsePanels();
                    }
                }
                return StatusBar.this.mStatusBarWindow.onTouchEvent(motionEvent);
            }
        };
    }

    private void inflateShelf() {
        this.mNotificationShelf = (NotificationShelf) LayoutInflater.from(this.mContext).inflate(R.layout.status_bar_notification_shelf, this.mStackScroller, false);
        this.mNotificationShelf.setOnActivatedListener(this);
        this.mStackScroller.setShelf(this.mNotificationShelf);
        this.mNotificationShelf.setOnClickListener(this.mGoToLockedShadeListener);
        this.mNotificationShelf.setStatusBarState(this.mState);
        this.mNotificationShelf.setViewType(1);
    }

    /* access modifiers changed from: protected */
    public void onDensityOrFontScaleChanged() {
        if (!KeyguardUpdateMonitor.getInstance(this.mContext).isSwitchingUser()) {
            updateNotificationsOnDensityOrFontScaleChanged();
        } else {
            this.mReinflateNotificationsOnUserSwitched = true;
        }
        this.mScrimController.onDensityOrFontScaleChanged();
        PhoneStatusBarView phoneStatusBarView = this.mStatusBarView;
        if (phoneStatusBarView != null) {
            phoneStatusBarView.onDensityOrFontScaleChanged();
        }
        BrightnessMirrorController brightnessMirrorController = this.mBrightnessMirrorController;
        if (brightnessMirrorController != null) {
            brightnessMirrorController.onDensityOrFontScaleChanged();
        }
        inflateSignalClusters();
        this.mNotificationIconAreaController.onDensityOrFontScaleChanged(this.mContext);
        inflateDismissView();
        updateClearAll();
        inflateEmptyShadeView();
        updateEmptyShadeView();
        this.mStatusBarKeyguardViewManager.onDensityOrFontScaleChanged();
        ((UserInfoControllerImpl) Dependency.get(UserInfoController.class)).onDensityOrFontScaleChanged();
        ((UserSwitcherController) Dependency.get(UserSwitcherController.class)).onDensityOrFontScaleChanged();
        KeyguardUserSwitcher keyguardUserSwitcher = this.mKeyguardUserSwitcher;
        if (keyguardUserSwitcher != null) {
            keyguardUserSwitcher.onDensityOrFontScaleChanged();
        }
        MiuiStatusBarPromptController miuiStatusBarPromptController = this.mMiuiStatusBarPrompt;
        if (miuiStatusBarPromptController != null) {
            miuiStatusBarPromptController.updateViews();
        }
    }

    /* access modifiers changed from: private */
    public void updateNotificationsOnDensityOrFontScaleChanged() {
        updateNotificationsOnDensityOrFontScaleChanged(this.mNotificationData.getAllEntries());
    }

    private void updateNotificationsOnDensityOrFontScaleChanged(List<NotificationData.Entry> list) {
        for (int i = 0; i < list.size(); i++) {
            NotificationData.Entry entry = list.get(i);
            try {
                entry.updateIcons(this.mContext, entry.notification);
            } catch (InflationException unused) {
                Log.d("StatusBar", "updateIcons failed key=" + entry.key);
            }
            MiuiNotificationCompat.setEnableFloat(entry.notification.getNotification(), false);
            boolean z = this.mNotificationGutsExposed != null && entry.row.getGuts() == this.mNotificationGutsExposed;
            entry.row.onDensityOrFontScaleChanged();
            if (z) {
                this.mNotificationGutsExposed = entry.row.getGuts();
                this.mNotificationGutsExposed.setExposed(true, false);
                bindGuts(entry.row, this.mGutsMenuItem);
            }
        }
    }

    private void inflateSignalClusters() {
        reinflateSignalCluster(this.mKeyguardStatusBar);
    }

    public static SignalClusterView reinflateSignalCluster(View view) {
        Context context = view.getContext();
        SignalClusterView signalClusterView = (SignalClusterView) view.findViewById(R.id.signal_cluster);
        if (signalClusterView == null) {
            return null;
        }
        ViewParent parent = signalClusterView.getParent();
        if (!(parent instanceof ViewGroup)) {
            return signalClusterView;
        }
        ViewGroup viewGroup = (ViewGroup) parent;
        int indexOfChild = viewGroup.indexOfChild(signalClusterView);
        viewGroup.removeView(signalClusterView);
        SignalClusterView signalClusterView2 = (SignalClusterView) LayoutInflater.from(context).inflate(R.layout.signal_cluster_view, viewGroup, false);
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) viewGroup.getLayoutParams();
        marginLayoutParams.setMarginsRelative(context.getResources().getDimensionPixelSize(R.dimen.signal_cluster_margin_start), 0, 0, 0);
        signalClusterView2.setLayoutParams(marginLayoutParams);
        viewGroup.addView(signalClusterView2, indexOfChild);
        return signalClusterView2;
    }

    private void inflateEmptyShadeView() {
        this.mEmptyShadeView = (EmptyShadeView) LayoutInflater.from(this.mContext).inflate(R.layout.status_bar_no_notifications, this.mStackScroller, false);
        this.mEmptyShadeView.setViewType(2);
        this.mStackScroller.setEmptyShadeView(this.mEmptyShadeView);
    }

    private void inflateDismissView() {
        this.mDismissView = (DismissView) LayoutInflater.from(this.mContext).inflate(R.layout.status_bar_notification_dismiss_all, (ViewGroup) this.mNotificationPanel.findViewById(R.id.notification_container_parent), false);
        this.mDismissView.setDrawables(R.drawable.notifications_clear_all, R.drawable.btn_clear_all);
        this.mDismissView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                MetricsLogger.action(StatusBar.this.mContext, 148);
                ScenarioTrackUtil.beginScenario(ScenarioConstants.SCENARIO_CLEAR_ALL_NOTI);
                StatusBar.this.clearAllNotifications();
                ((HapticFeedBackImpl) Dependency.get(HapticFeedBackImpl.class)).clearAllNotifications();
            }
        });
        this.mNotificationPanel.setDismissView(this.mDismissView);
        this.mDismissView.setAccessibilityTraversalAfter(R.id.notification_stack_scroller);
        this.mStackScroller.setAccessibilityTraversalBefore(R.id.dismiss_view);
    }

    private void inflateHeadsUpStubView() {
        HeadsUpAnimatedStubView headsUpAnimatedStubView = (HeadsUpAnimatedStubView) this.mStatusBarWindow.findViewById(R.id.heads_up_animated_stub);
        headsUpAnimatedStubView.setHeadsUpManager(this.mHeadsUpManager);
        headsUpAnimatedStubView.setHeadsHiddenListener(this.mStackScroller);
        this.mHeadsUpAnimatedStub = headsUpAnimatedStubView;
        this.mAppMiniWindowManager.setHeadsUpStubView(headsUpAnimatedStubView);
    }

    /* access modifiers changed from: protected */
    public void createUserSwitcher() {
        this.mKeyguardUserSwitcher = new KeyguardUserSwitcher(this.mContext, (ViewStub) this.mStatusBarWindow.findViewById(R.id.keyguard_user_switcher), this.mKeyguardStatusBar, this.mNotificationPanel);
    }

    /* access modifiers changed from: protected */
    public void inflateStatusBarWindow(Context context) {
        this.mStatusBarWindow = (StatusBarWindowView) View.inflate(context, R.layout.super_status_bar, (ViewGroup) null);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0077, code lost:
        if (r12.mTmpRect.height() > 0) goto L_0x0079;
     */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x0082  */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x00bf A[SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void clearAllNotifications() {
        /*
            r12 = this;
            r0 = 1
            r12.mHasClearAllNotifications = r0
            r1 = 0
            r12.updateAppBadgeNum(r1)
            com.android.systemui.dnd.DndNotificationWarnings r1 = r12.mDndWarnings
            r1.markClearAllNotifications()
            com.android.systemui.statusbar.NotificationData r1 = r12.mNotificationData
            java.util.List r1 = r1.getClearableNotifications()
            java.lang.Class<com.android.systemui.miui.statusbar.analytics.NotificationStat> r2 = com.android.systemui.miui.statusbar.analytics.NotificationStat.class
            java.lang.Object r2 = com.android.systemui.Dependency.get(r2)
            com.android.systemui.miui.statusbar.analytics.NotificationStat r2 = (com.android.systemui.miui.statusbar.analytics.NotificationStat) r2
            com.android.systemui.statusbar.stack.NotificationStackScrollLayout r3 = r12.mStackScroller
            boolean r3 = r3.canScrollDown()
            r4 = 0
            r2.onRemoveAll(r4, r1, r3)
            com.android.systemui.statusbar.stack.NotificationStackScrollLayout r1 = r12.mStackScroller
            int r1 = r1.getChildCount()
            java.util.ArrayList r2 = new java.util.ArrayList
            r2.<init>(r1)
            java.util.ArrayList r3 = new java.util.ArrayList
            r3.<init>(r1)
            r5 = r4
        L_0x0035:
            if (r5 >= r1) goto L_0x00c3
            com.android.systemui.statusbar.stack.NotificationStackScrollLayout r6 = r12.mStackScroller
            android.view.View r6 = r6.getChildAt(r5)
            boolean r7 = r6 instanceof com.android.systemui.statusbar.ExpandableNotificationRow
            if (r7 == 0) goto L_0x00bf
            r7 = r6
            com.android.systemui.statusbar.ExpandableNotificationRow r7 = (com.android.systemui.statusbar.ExpandableNotificationRow) r7
            android.graphics.Rect r8 = r12.mTmpRect
            boolean r8 = r6.getClipBounds(r8)
            com.android.systemui.statusbar.stack.NotificationStackScrollLayout r9 = r12.mStackScroller
            boolean r9 = r9.canChildBeDismissed(r6)
            if (r9 == 0) goto L_0x0069
            r3.add(r7)
            int r9 = r6.getVisibility()
            if (r9 != 0) goto L_0x007b
            if (r8 == 0) goto L_0x0065
            android.graphics.Rect r8 = r12.mTmpRect
            int r8 = r8.height()
            if (r8 <= 0) goto L_0x007b
        L_0x0065:
            r2.add(r6)
            goto L_0x0079
        L_0x0069:
            int r6 = r6.getVisibility()
            if (r6 != 0) goto L_0x007b
            if (r8 == 0) goto L_0x0079
            android.graphics.Rect r6 = r12.mTmpRect
            int r6 = r6.height()
            if (r6 <= 0) goto L_0x007b
        L_0x0079:
            r6 = r0
            goto L_0x007c
        L_0x007b:
            r6 = r4
        L_0x007c:
            java.util.List r8 = r7.getNotificationChildren()
            if (r8 == 0) goto L_0x00bf
            java.util.Iterator r8 = r8.iterator()
        L_0x0086:
            boolean r9 = r8.hasNext()
            if (r9 == 0) goto L_0x00bf
            java.lang.Object r9 = r8.next()
            com.android.systemui.statusbar.ExpandableNotificationRow r9 = (com.android.systemui.statusbar.ExpandableNotificationRow) r9
            r3.add(r9)
            if (r6 == 0) goto L_0x0086
            boolean r10 = r7.areChildrenExpanded()
            if (r10 == 0) goto L_0x0086
            com.android.systemui.statusbar.stack.NotificationStackScrollLayout r10 = r12.mStackScroller
            boolean r10 = r10.canChildBeDismissed(r9)
            if (r10 == 0) goto L_0x0086
            android.graphics.Rect r10 = r12.mTmpRect
            boolean r10 = r9.getClipBounds(r10)
            int r11 = r9.getVisibility()
            if (r11 != 0) goto L_0x0086
            if (r10 == 0) goto L_0x00bb
            android.graphics.Rect r10 = r12.mTmpRect
            int r10 = r10.height()
            if (r10 <= 0) goto L_0x0086
        L_0x00bb:
            r2.add(r9)
            goto L_0x0086
        L_0x00bf:
            int r5 = r5 + 1
            goto L_0x0035
        L_0x00c3:
            boolean r0 = r3.isEmpty()
            if (r0 == 0) goto L_0x00cc
            r12.animateCollapsePanels()
        L_0x00cc:
            com.android.systemui.statusbar.phone.StatusBar$32 r0 = new com.android.systemui.statusbar.phone.StatusBar$32
            r0.<init>(r3)
            r12.addPostCollapseAction(r0)
            r12.performDismissAllAnimations(r2)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.phone.StatusBar.clearAllNotifications():void");
    }

    private void performDismissAllAnimations(ArrayList<View> arrayList) {
        AnonymousClass33 r0 = new Runnable() {
            public void run() {
                StatusBar.this.mDismissView.animatorStart(new AnimatorListenerAdapter() {
                    public void onAnimationEnd(Animator animator) {
                        StatusBar.this.animateCollapsePanels();
                    }
                });
            }
        };
        this.mStackScroller.setDismissAllInProgress(true);
        ArrayList arrayList2 = new ArrayList();
        Iterator<View> it = arrayList.iterator();
        while (it.hasNext()) {
            View next = it.next();
            if ((next instanceof ExpandableNotificationRow) && this.mStackScroller.isInUserVisibleArea((ExpandableNotificationRow) next)) {
                arrayList2.add(next);
            }
        }
        if (arrayList2.size() == 0 && arrayList.size() > 0) {
            arrayList2.add(arrayList.get(0));
        }
        Log.i("StatusBar", String.format("ignored %d rows when dismiss all", new Object[]{Integer.valueOf(arrayList.size() - arrayList2.size())}));
        this.mStackScroller.dispatchDismissAllToChild(arrayList2, r0);
    }

    /* access modifiers changed from: protected */
    public void setZenMode(int i) {
        if (isDeviceProvisioned()) {
            this.mZenMode = i;
            updateNotifications();
        }
    }

    /* access modifiers changed from: protected */
    public void startKeyguard() {
        Trace.beginSection("StatusBar#startKeyguard");
        KeyguardViewMediator keyguardViewMediator = (KeyguardViewMediator) getComponent(KeyguardViewMediator.class);
        Context context = this.mContext;
        this.mFingerprintUnlockController = new FingerprintUnlockController(context, this.mDozeScrimController, keyguardViewMediator, this.mScrimController, this, UnlockMethodCache.getInstance(context));
        this.mFaceUnlockController = new FaceUnlockController(this.mContext, keyguardViewMediator, this);
        this.mStatusBarKeyguardViewManager = keyguardViewMediator.registerStatusBar(this, getBouncerContainer(), this.mScrimController, this.mFingerprintUnlockController, this.mFaceUnlockController);
        this.mKeyguardIndicationController.setStatusBarKeyguardViewManager(this.mStatusBarKeyguardViewManager);
        this.mKeyguardIndicationController.setUserInfoController((UserInfoController) Dependency.get(UserInfoController.class));
        this.mFingerprintUnlockController.setStatusBarKeyguardViewManager(this.mStatusBarKeyguardViewManager);
        this.mFaceUnlockController.setStatusBarKeyguardViewManager(this.mStatusBarKeyguardViewManager);
        this.mRemoteInputController.addCallback(this.mStatusBarKeyguardViewManager);
        this.mRemoteInputController.addCallback(new RemoteInputController.Callback() {
            public void onRemoteInputActive(boolean z) {
            }

            public void onRemoteInputSent(final NotificationData.Entry entry) {
                if (StatusBar.FORCE_REMOTE_INPUT_HISTORY && StatusBar.this.mKeysKeptForRemoteInput.contains(entry.key)) {
                    StatusBar.this.removeNotification(entry.key, (NotificationListenerService.RankingMap) null);
                } else if (StatusBar.this.mRemoteInputEntriesToRemoveOnCollapse.contains(entry)) {
                    StatusBar.this.mHandler.postDelayed(new Runnable() {
                        public void run() {
                            if (StatusBar.this.mRemoteInputEntriesToRemoveOnCollapse.remove(entry)) {
                                StatusBar.this.removeNotification(entry.key, (NotificationListenerService.RankingMap) null);
                            }
                        }
                    }, 200);
                }
            }
        });
        this.mKeyguardViewMediatorCallback = keyguardViewMediator.getViewMediatorCallback();
        this.mLightBarController.setFingerprintUnlockController(this.mFingerprintUnlockController);
        Trace.endSection();
    }

    /* access modifiers changed from: protected */
    public View getStatusBarView() {
        return this.mStatusBarView;
    }

    public StatusBarWindowView getStatusBarWindow() {
        return this.mStatusBarWindow;
    }

    /* access modifiers changed from: protected */
    public ViewGroup getBouncerContainer() {
        return this.mStatusBarWindow;
    }

    public int getStatusBarHeight() {
        if (this.mNaturalBarHeight < 0) {
            this.mNaturalBarHeight = this.mContext.getResources().getDimensionPixelSize(17105439);
        }
        return this.mNaturalBarHeight;
    }

    public boolean collapsePanel() {
        if (this.mNotificationPanel.isFullyCollapsed()) {
            return false;
        }
        animateCollapsePanels(2, true, true);
        visibilityChanged(false);
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean toggleSplitScreenMode(int i, int i2) {
        if (this.mRecents == null) {
            return false;
        }
        Divider divider = (Divider) getComponent(Divider.class);
        if (divider == null || !divider.inSplitMode()) {
            return this.mRecents.dockTopTask(-1, 0, (Rect) null, i);
        }
        if (divider != null && divider.isMinimized() && !divider.isHomeStackResizable()) {
            return false;
        }
        if (divider != null) {
            divider.onUndockingTask(true);
        }
        if (i2 != -1) {
            MetricsLogger.action(this.mContext, i2);
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public void awakenDreams() {
        SystemServicesProxy.getInstance(this.mContext).awakenDreamsAsync();
    }

    private void postWakeUpForNotification(NotificationData.Entry entry) {
        if (!this.mHandler.hasMessages(b.f, entry)) {
            this.mHandler.sendMessageDelayed(Message.obtain(this.mHandler, b.f, entry), 500);
        }
    }

    /* access modifiers changed from: private */
    public void wakeUpForNotification(NotificationData.Entry entry) {
        if (this.mNotificationData.getActiveNotifications().contains(entry) && this.mWakeupForNotification && entry.notification.isClearable() && !NotificationUtil.hasProgressbar(entry.notification) && !entry.isMediaNotification() && shouldShowOnKeyguard(entry)) {
            if ((!this.mDeviceInteractive || this.mDozing) && !this.mIsDNDEnabled) {
                Slog.i("StatusBar", "wake up for notification, pkg:" + entry.notification.getPackageName());
                if (!KeyguardUpdateMonitor.getInstance(this.mContext).isPsensorDisabled()) {
                    KeyguardSensorManager.getInstance(this.mContext).registerProximitySensor(new KeyguardSensorManager.ProximitySensorChangeCallback() {
                        public void onChange(boolean z) {
                            if (!z) {
                                StatusBar.this.wakeUpForNotificationInternal();
                            } else {
                                Log.e("miui_keyguard", "not wake up for notification because in suspect mode");
                            }
                        }
                    });
                } else if (!MiuiKeyguardUtils.isNonUI()) {
                    wakeUpForNotificationInternal();
                } else {
                    AnalyticsHelper.getInstance(this.mContext).record("screen_not_on_in_nonui_mode");
                    Log.e("miui_keyguard", "not wake up for notification in nonui mode");
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void wakeUpForNotificationInternal() {
        this.mPowerManager.wakeUp(SystemClock.uptimeMillis(), "com.android.systemui:NOTIFICATION");
        this.mFaceUnlockManager.setWakeupByNotification(true);
        AnalyticsHelper.getInstance(this.mContext).setWakeupWay("screen_on_by_notification");
    }

    public void addNotification(ExpandedNotification expandedNotification, NotificationListenerService.RankingMap rankingMap) throws InflationException {
        String key = expandedNotification.getKey();
        if (!filterNotification(expandedNotification)) {
            this.mHasClearAllNotifications = false;
            this.mNotificationData.updateRanking(rankingMap);
            NotificationData.Entry createNotificationViews = createNotificationViews(expandedNotification);
            createNotificationViews.hideSensitiveByAppLock = isHideSensitiveByAppLock(expandedNotification);
            createNotificationViews.needUpdateBadgeNum = NotificationUtil.needStatBadgeNum(expandedNotification);
            createNotificationViews.canShowBaged = NotificationSettingsHelper.canShowBadge(this.mContextForUser, expandedNotification.getPackageName());
            if (Constants.SUPPORT_FPS_DYNAMIC_ACCOMMODATION) {
                dynamicFPSAccommodation(createNotificationViews);
            }
            boolean shouldPeek = shouldPeek(createNotificationViews);
            expandedNotification.notePeek(shouldPeek);
            if (!shouldPeek && expandedNotification.getNotification().fullScreenIntent != null) {
                if (shouldSuppressFullScreenIntent(key)) {
                    Log.d("StatusBar", "No Fullscreen intent: suppressed by DND: " + key);
                } else if (createNotificationViews.notification.getImportance() < 4) {
                    Log.d("StatusBar", "No Fullscreen intent: not important enough: " + key);
                } else {
                    if (NotificationUtil.isInCallUINotification(expandedNotification)) {
                        awakenDreams();
                    }
                    expandedNotification.noteFullscreen(true);
                    try {
                        EventLog.writeEvent(36002, key);
                        expandedNotification.getNotification().fullScreenIntent.send();
                        createNotificationViews.notifyFullScreenIntentLaunched();
                        MetricsLogger.count(this.mContext, "note_fullscreen", 1);
                    } catch (PendingIntent.CanceledException e) {
                        Log.e("StatusBar", "throw exception when sending full screen intent" + e);
                    }
                }
            }
            abortExistingInflation(key);
            this.mForegroundServiceController.addNotification(expandedNotification, createNotificationViews.notification.getImportance());
            this.mPendingNotifications.put(key, createNotificationViews);
            postWakeUpForNotification(createNotificationViews);
            ((NotificationsMonitor) Dependency.get(NotificationsMonitor.class)).notifyNotificationAdded(expandedNotification);
            ((BubbleController) Dependency.get(BubbleController.class)).onPendingEntryAdded(createNotificationViews);
            if (InCallUtils.isInCallNotification(expandedNotification)) {
                ((ControlPanelController) Dependency.get(ControlPanelController.class)).collapseControlCenter(true);
            }
        }
    }

    private boolean isHideSensitiveByAppLock(ExpandedNotification expandedNotification) {
        return AppLockHelper.shouldShowPublicNotificationByAppLock(this.mContext, this.mSecurityManager, expandedNotification.getPackageName(), AppLockHelper.getCurrentUserIdIfNeeded(expandedNotification.getUserId(), this.mCurrentUserId));
    }

    private boolean filterNotification(ExpandedNotification expandedNotification) {
        boolean z;
        String packageName = expandedNotification.getPackageName();
        String channelId = expandedNotification.getNotification().getChannelId();
        if ((!expandedNotification.isSubstituteNotification() || !NotificationSettingsHelper.isNotificationsBanned(this.mContext, packageName)) && (((expandedNotification.getNotification().flags & 64) == 0 || !((NotificationSettingsManager) Dependency.get(NotificationSettingsManager.class)).hideForegroundNotification(packageName, channelId)) && (((expandedNotification.getNotification().flags & 2) == 0 || expandedNotification.getId() != 0 || !TextUtils.equals("android", expandedNotification.getBasePkg()) || !((NotificationSettingsManager) Dependency.get(NotificationSettingsManager.class)).hideAlertWindowNotification(expandedNotification.getTag())) && !((UsbNotificationController) Dependency.get(UsbNotificationController.class)).needDisableUsbNotification(expandedNotification) && ((expandedNotification.getNotification().flags & 268435456) == 0 || (!packageName.equalsIgnoreCase("com.mediatek.selfregister") && !packageName.equalsIgnoreCase("com.mediatek.deviceregister")))))) {
            z = false;
        } else {
            z = true;
        }
        if (z) {
            onNotificationClear(expandedNotification);
        }
        if (z) {
            Log.d("StatusBar", String.format("filter Notification key=%s", new Object[]{expandedNotification.getKey()}));
        }
        return z;
    }

    private void abortExistingInflation(String str) {
        if (this.mPendingNotifications.containsKey(str)) {
            this.mPendingNotifications.get(str).abortTask();
            this.mPendingNotifications.remove(str);
        }
        NotificationData.Entry entry = this.mNotificationData.get(str);
        if (entry != null) {
            entry.abortTask();
        }
    }

    private void addEntry(NotificationData.Entry entry) {
        if (shouldPeek(entry)) {
            this.mHeadsUpManager.showNotification(entry);
            setNotificationShown(entry.notification);
        }
        addNotificationViews(entry);
        setAreThereNotifications();
    }

    public void handleInflationException(StatusBarNotification statusBarNotification, Exception exc) {
        handleNotificationError(statusBarNotification, exc.getMessage());
    }

    public void onAsyncInflationFinished(NotificationData.Entry entry) {
        this.mPendingNotifications.remove(entry.key);
        boolean z = this.mNotificationData.get(entry.key) == null;
        if (z && !entry.row.isRemoved()) {
            ((BubbleController) Dependency.get(BubbleController.class)).onEntryInflated(entry, 0);
            addEntry(entry);
            if (!this.mMirrorDndEnable) {
                this.mBgHandler.obtainMessage(b.m, entry.notification.getKey()).sendToTarget();
            }
        } else if (!z && entry.row.hasLowPriorityStateUpdated()) {
            this.mVisualStabilityManager.onLowPriorityUpdated(entry);
            updateNotificationShade();
        }
        entry.row.setLowPriorityStateUpdated(false);
        if (entry.needUpdateBadgeNum) {
            updateAppBadgeNum(entry.notification);
        }
        if (!z) {
            updateHeadsUp(entry.key, entry, shouldPeek(entry), alertAgain(entry, entry.notification.getNotification()));
        }
        if (!needUpdateNotificationProvider(entry)) {
            return;
        }
        if (z) {
            ((KeyguardNotificationHelper) Dependency.get(KeyguardNotificationHelper.class)).add(entry);
        } else {
            ((KeyguardNotificationHelper) Dependency.get(KeyguardNotificationHelper.class)).update(entry);
        }
    }

    private boolean needUpdateNotificationProvider(NotificationData.Entry entry) {
        if (isKeyguardShowing() && shouldShowOnKeyguard(entry) && !entry.isMediaNotification() && !entry.isCustomViewNotification()) {
            return true;
        }
        return false;
    }

    public void updateStatusBarPading() {
        int i;
        int i2;
        if (this.mStatusBarView != null) {
            if (CustomizeUtil.HAS_NOTCH) {
                int rotation = this.mDisplay.getRotation();
                if (rotation == 1) {
                    i2 = DisplayCutoutCompat.getSafeInsetLeft(this, this.mInfo);
                    i = 0;
                } else if (rotation == 3) {
                    i = DisplayCutoutCompat.getSafeInsetRight(this, this.mInfo);
                    i2 = 0;
                }
                this.mStatusBarView.setPadding(i2, 0, i, 0);
            }
            i2 = 0;
            i = 0;
            this.mStatusBarView.setPadding(i2, 0, i, 0);
        }
    }

    private boolean shouldSuppressFullScreenIntent(String str) {
        if (isDeviceInVrMode() || isVrMode()) {
            return true;
        }
        if (this.mPowerManager.isInteractive()) {
            return this.mNotificationData.shouldSuppressScreenOn(str);
        }
        return this.mNotificationData.shouldSuppressScreenOff(str);
    }

    /* access modifiers changed from: protected */
    public void updateNotificationRanking(NotificationListenerService.RankingMap rankingMap) {
        Log.d("StatusBar", "updateNotificationRanking");
        this.mNotificationData.updateRanking(rankingMap);
        updateNotificationViewsOnly();
    }

    /* access modifiers changed from: private */
    public void updateNotificationRankingDelayed(NotificationListenerService.RankingMap rankingMap, long j) {
        Log.d("StatusBar", "updateNotificationRankingDelayed messageReceiveTime=" + j);
        if (this.mNotificationData.updateRankingDelayed(rankingMap, j)) {
            updateNotificationViewsOnly();
            if (rankingMap != null) {
                ((BubbleController) Dependency.get(BubbleController.class)).onNotificationRankingUpdated(rankingMap);
            }
        }
    }

    public void removeNotification(String str, NotificationListenerService.RankingMap rankingMap) {
        removeNotification(str, rankingMap, 0);
    }

    public void removeNotification(String str, NotificationListenerService.RankingMap rankingMap, int i) {
        if (this.mAppMiniWindowManager.isStartingActivity(str)) {
            Slog.i("StatusBar", "cannot removeNotification key=" + str);
            return;
        }
        removeNotification(str, rankingMap, true, i);
    }

    public void removeNotification(String str, NotificationListenerService.RankingMap rankingMap, boolean z, int i) {
        ExpandableNotificationRow expandableNotificationRow;
        ExpandableNotificationRow expandableNotificationRow2;
        CharSequence[] charSequenceArr;
        if (!((BubbleController) Dependency.get(BubbleController.class)).onNotificationRemoveRequested(str, i)) {
            abortExistingInflation(str);
            boolean z2 = false;
            boolean z3 = this.mHeadsUpManager.isHeadsUp(str) ? !this.mHeadsUpManager.removeNotification(str, true) : false;
            if (str.equals(this.mMediaNotificationKey)) {
                clearCurrentMediaNotification();
                updateMediaMetaData(true, true);
            }
            String str2 = null;
            if (FORCE_REMOTE_INPUT_HISTORY && this.mRemoteInputController.isSpinning(str)) {
                NotificationData.Entry entry = this.mNotificationData.get(str);
                ExpandedNotification expandedNotification = entry.notification;
                Notification.Builder recoverBuilder = NotificationCompat.recoverBuilder(this.mContext, expandedNotification.getNotification().clone());
                CharSequence[] charSequenceArray = expandedNotification.getNotification().extras.getCharSequenceArray("android.remoteInputHistory");
                if (charSequenceArray == null) {
                    charSequenceArr = new CharSequence[1];
                } else {
                    CharSequence[] charSequenceArr2 = new CharSequence[(charSequenceArray.length + 1)];
                    int i2 = 0;
                    while (i2 < charSequenceArray.length) {
                        int i3 = i2 + 1;
                        charSequenceArr2[i3] = charSequenceArray[i2];
                        i2 = i3;
                    }
                    charSequenceArr = charSequenceArr2;
                }
                charSequenceArr[0] = String.valueOf(entry.remoteInputText);
                NotificationCompat.setRemoteInputHistory(recoverBuilder, charSequenceArr);
                Notification build = recoverBuilder.build();
                build.contentView = expandedNotification.getNotification().contentView;
                build.bigContentView = expandedNotification.getNotification().bigContentView;
                build.headsUpContentView = expandedNotification.getNotification().headsUpContentView;
                boolean handleNotification = handleNotification(expandedNotification, (NotificationListenerService.RankingMap) null, true);
                if (!handleNotification) {
                    z3 = false;
                }
                if (handleNotification) {
                    this.mKeysKeptForRemoteInput.add(entry.key);
                    return;
                }
            }
            if (z3) {
                this.mLatestRankingMap = rankingMap;
                this.mHeadsUpEntriesToRemoveOnSwitch.add(this.mHeadsUpManager.getEntry(str));
                return;
            }
            NotificationData.Entry entry2 = this.mNotificationData.get(str);
            if (entry2 == null || !this.mRemoteInputController.isRemoteInputActive(entry2) || (expandableNotificationRow2 = entry2.row) == null || expandableNotificationRow2.isDismissed()) {
                if (entry2 != null) {
                    this.mForegroundServiceController.removeNotification(entry2.notification);
                }
                if (!(entry2 == null || (expandableNotificationRow = entry2.row) == null)) {
                    expandableNotificationRow.setRemoved();
                    this.mStackScroller.cleanUpViewState(entry2.row);
                }
                handleGroupSummaryRemoved(str, rankingMap);
                ExpandedNotification removeNotificationViews = removeNotificationViews(str, rankingMap, z);
                Log.d("StatusBar", "removeNotification " + removeNotificationViews);
                if (removeNotificationViews != null) {
                    if (!this.mHasClearAllNotifications) {
                        updateAppBadgeNum(removeNotificationViews);
                    }
                    hasActiveNotifications();
                }
                setAreThereNotifications();
                if (entry2 != null && ((KeyguardNotificationHelper) Dependency.get(KeyguardNotificationHelper.class)).needReadd(entry2)) {
                    z2 = true;
                }
                if (z2 || isKeyguardShowing()) {
                    if (entry2 != null) {
                        str2 = entry2.notification.getPackageName();
                    }
                    ((KeyguardNotificationHelper) Dependency.get(KeyguardNotificationHelper.class)).remove(str, str2);
                    return;
                }
                return;
            }
            this.mLatestRankingMap = rankingMap;
            this.mRemoteInputEntriesToRemoveOnCollapse.add(entry2);
        }
    }

    private void handleGroupSummaryRemoved(String str, NotificationListenerService.RankingMap rankingMap) {
        ExpandableNotificationRow expandableNotificationRow;
        NotificationData.Entry entry = this.mNotificationData.get(str);
        if (entry != null && (expandableNotificationRow = entry.row) != null && expandableNotificationRow.isSummaryWithChildren()) {
            if (StatusBarNotificationCompat.getOverrideGroupKey(entry.notification) == null || entry.row.isDismissed()) {
                List<ExpandableNotificationRow> notificationChildren = entry.row.getNotificationChildren();
                ArrayList arrayList = new ArrayList();
                for (int i = 0; i < notificationChildren.size(); i++) {
                    ExpandableNotificationRow expandableNotificationRow2 = notificationChildren.get(i);
                    if ((expandableNotificationRow2.getStatusBarNotification().getNotification().flags & 64) == 0) {
                        arrayList.add(expandableNotificationRow2);
                        expandableNotificationRow2.setKeepInParent(true);
                        expandableNotificationRow2.setRemoved();
                    }
                }
            }
        }
    }

    public void performRemoveNotification(ExpandedNotification expandedNotification) {
        NotificationData.Entry entry = this.mNotificationData.get(expandedNotification.getKey());
        if (this.mRemoteInputController.isRemoteInputActive(entry)) {
            this.mRemoteInputController.removeRemoteInput(entry, (Object) null);
        }
        onNotificationClear(expandedNotification);
        if (FORCE_REMOTE_INPUT_HISTORY && this.mKeysKeptForRemoteInput.contains(expandedNotification.getKey())) {
            this.mKeysKeptForRemoteInput.remove(expandedNotification.getKey());
        }
        removeNotification(expandedNotification.getKey(), (NotificationListenerService.RankingMap) null, 2);
        if (this.mState == 1) {
            this.mDndWarnings.markClearNotification(expandedNotification);
        }
    }

    /* access modifiers changed from: private */
    public void updateNotificationShade() {
        if (this.mStackScroller != null) {
            if (isCollapsing()) {
                addPostCollapseAction(new Runnable() {
                    public void run() {
                        StatusBar.this.updateNotificationShade();
                    }
                });
                return;
            }
            ArrayList<NotificationData.Entry> activeNotifications = this.mNotificationData.getActiveNotifications();
            ArrayList arrayList = new ArrayList(activeNotifications.size());
            int size = activeNotifications.size();
            boolean z = false;
            int i = 0;
            while (true) {
                boolean z2 = true;
                if (i >= size) {
                    break;
                }
                NotificationData.Entry entry = activeNotifications.get(i);
                if (!entry.row.isDismissed() && !entry.row.isRemoved()) {
                    int userId = entry.notification.getUserId();
                    boolean isLockscreenPublicMode = isLockscreenPublicMode(this.mCurrentUserId);
                    boolean z3 = (isLockscreenPublicMode || isLockscreenPublicMode(userId)) && needsRedaction(entry);
                    if (entry.hideSensitive != z3) {
                        entry.hideSensitive = z3;
                        if (isKeyguardShowing() && shouldShowOnKeyguard(entry)) {
                            ((KeyguardNotificationHelper) Dependency.get(KeyguardNotificationHelper.class)).update(entry);
                        }
                    }
                    entry.row.setSensitive(z3 || entry.hideSensitiveByAppLock, isLockscreenPublicMode && !userAllowsPrivateNotificationsInPublic(this.mCurrentUserId));
                    entry.row.setNeedsRedaction(false);
                    ExpandedNotification statusBarNotification = entry.row.getStatusBarNotification();
                    ExpandableNotificationRow groupSummary = this.mGroupManager.getGroupSummary((StatusBarNotification) statusBarNotification);
                    if (this.mGroupManager.isChildInGroupWithSummary(statusBarNotification)) {
                        List list = this.mTmpChildOrderMap.get(groupSummary);
                        if (list == null) {
                            list = new ArrayList();
                            this.mTmpChildOrderMap.put(groupSummary, list);
                        }
                        list.add(entry.row);
                    } else {
                        if (this.mState != 0 || !entry.notification.isOnlyShowKeyguard()) {
                            z2 = false;
                        }
                        if (!z2) {
                            arrayList.add(entry.row);
                        }
                    }
                }
                i++;
            }
            ArrayList arrayList2 = new ArrayList();
            for (int i2 = 0; i2 < this.mStackScroller.getChildCount(); i2++) {
                View childAt = this.mStackScroller.getChildAt(i2);
                if (!arrayList.contains(childAt) && (childAt instanceof ExpandableNotificationRow)) {
                    arrayList2.add((ExpandableNotificationRow) childAt);
                }
            }
            Iterator it = arrayList2.iterator();
            while (it.hasNext()) {
                ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) it.next();
                if (this.mGroupManager.isChildInGroupWithSummary(expandableNotificationRow.getStatusBarNotification())) {
                    this.mStackScroller.setChildTransferInProgress(true);
                }
                if (expandableNotificationRow.isSummaryWithChildren() && expandableNotificationRow.isGroupExpanded()) {
                    expandableNotificationRow.getExpandClickListener().onClick(expandableNotificationRow);
                }
                this.mStackScroller.removeView(expandableNotificationRow);
                this.mStackScroller.setChildTransferInProgress(false);
            }
            removeNotificationChildren();
            for (int i3 = 0; i3 < arrayList.size(); i3++) {
                View view = (View) arrayList.get(i3);
                if (view.getParent() == null) {
                    this.mVisualStabilityManager.notifyViewAddition(view);
                    this.mStackScroller.addView(view);
                }
            }
            addNotificationChildrenAndSort();
            int i4 = 0;
            for (int i5 = 0; i5 < this.mStackScroller.getChildCount(); i5++) {
                View childAt2 = this.mStackScroller.getChildAt(i5);
                if (childAt2 instanceof ExpandableNotificationRow) {
                    ExpandableNotificationRow expandableNotificationRow2 = (ExpandableNotificationRow) arrayList.get(i4);
                    if (childAt2 != expandableNotificationRow2) {
                        if (this.mVisualStabilityManager.canReorderNotification(expandableNotificationRow2)) {
                            this.mStackScroller.changeViewPosition(expandableNotificationRow2, i5);
                        } else {
                            this.mVisualStabilityManager.addReorderingAllowedCallback(this);
                        }
                    }
                    i4++;
                }
            }
            ArrayList arrayList3 = new ArrayList();
            for (int i6 = 0; i6 < this.mStackScroller.getChildCount(); i6++) {
                View childAt3 = this.mStackScroller.getChildAt(i6);
                if ((childAt3 instanceof ExpandableNotificationRow) && childAt3.getVisibility() != 8) {
                    arrayList3.add((ExpandableNotificationRow) childAt3);
                }
            }
            int i7 = 0;
            while (i7 < arrayList3.size()) {
                ((ExpandableNotificationRow) arrayList3.get(i7)).setIsFirstRow(i7 == 0);
                i7++;
            }
            this.mVisualStabilityManager.onReorderingFinished();
            this.mTmpChildOrderMap.clear();
            updateRowStates();
            changeViewPosition();
            updateSpeedBumpIndex();
            updateClearAll();
            updateEmptyShadeView();
            this.mKeyguardClock.updateClockView(hasNotificationOnKeyguard(), this.mState == 1);
            updateQsExpansionEnabled();
            this.mNotificationIconAreaController.updateNotificationIcons(this.mNotificationData);
            if (this.mNotifications != null && isNoIconsSetGone()) {
                this.mNotifications.setVisibility(this.mNotificationIconAreaController.getNotificationIconsVisibility());
            }
            if (this.mStatusBarFragment != null && hideAmPmForNotification()) {
                CollapsedStatusBarFragment collapsedStatusBarFragment = this.mStatusBarFragment;
                if (this.mNotificationIconAreaController.getNotificationIconsVisibility() == 0) {
                    z = true;
                }
                collapsedStatusBarFragment.refreshClockAmPm(z);
            }
        }
    }

    public boolean hasNotificationOnKeyguard() {
        return !this.mNotificationPanel.isNoVisibleNotifications();
    }

    private void changeViewPosition() {
        NotificationStackScrollLayout notificationStackScrollLayout = this.mStackScroller;
        notificationStackScrollLayout.changeViewPosition(this.mEmptyShadeView, notificationStackScrollLayout.getChildCount() - 1);
        NotificationStackScrollLayout notificationStackScrollLayout2 = this.mStackScroller;
        notificationStackScrollLayout2.changeViewPosition(this.mNotificationShelf, notificationStackScrollLayout2.getChildCount() - 2);
    }

    private boolean needsRedaction(NotificationData.Entry entry) {
        boolean z = (userAllowsPrivateNotificationsInPublic(this.mCurrentUserId) ^ true) || (userAllowsPrivateNotificationsInPublic(entry.notification.getUserId()) ^ true) || NotificationUtil.hideNotificationsForFaceUnlock(this.mContext);
        boolean z2 = entry.notification.getNotification().visibility == 0;
        if (packageHasVisibilityOverride(entry.notification.getKey())) {
            return true;
        }
        if (!z2 || !z) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0026, code lost:
        if (ONLY_CORE_APPS == false) goto L_0x002a;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void updateQsExpansionEnabled() {
        /*
            r4 = this;
            com.android.systemui.statusbar.phone.NotificationPanelView r0 = r4.mNotificationPanel
            boolean r1 = r4.isDeviceProvisioned()
            r2 = 1
            if (r1 == 0) goto L_0x0029
            boolean r1 = r4.mUserSetup
            if (r1 != 0) goto L_0x0017
            com.android.systemui.statusbar.policy.UserSwitcherController r1 = r4.mUserSwitcherController
            if (r1 == 0) goto L_0x0017
            boolean r1 = r1.isSimpleUserSwitcher()
            if (r1 != 0) goto L_0x0029
        L_0x0017:
            int r1 = r4.mDisabled2
            r3 = r1 & 4
            if (r3 != 0) goto L_0x0029
            r1 = r1 & r2
            if (r1 != 0) goto L_0x0029
            boolean r4 = r4.mDozing
            if (r4 != 0) goto L_0x0029
            boolean r4 = ONLY_CORE_APPS
            if (r4 != 0) goto L_0x0029
            goto L_0x002a
        L_0x0029:
            r2 = 0
        L_0x002a:
            r0.setQsExpansionEnabled(r2)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.phone.StatusBar.updateQsExpansionEnabled():void");
    }

    private void addNotificationChildrenAndSort() {
        boolean z = false;
        for (int i = 0; i < this.mStackScroller.getChildCount(); i++) {
            View childAt = this.mStackScroller.getChildAt(i);
            if (childAt instanceof ExpandableNotificationRow) {
                ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) childAt;
                List<ExpandableNotificationRow> notificationChildren = expandableNotificationRow.getNotificationChildren();
                List list = this.mTmpChildOrderMap.get(expandableNotificationRow);
                int i2 = 0;
                while (list != null && i2 < list.size()) {
                    ExpandableNotificationRow expandableNotificationRow2 = (ExpandableNotificationRow) list.get(i2);
                    if (notificationChildren == null || !notificationChildren.contains(expandableNotificationRow2)) {
                        if (expandableNotificationRow2.getParent() != null) {
                            Log.wtf("StatusBar", "trying to add a notification child that already has a parent. class:" + expandableNotificationRow2.getParent().getClass() + "\n child: " + expandableNotificationRow2);
                            ((ViewGroup) expandableNotificationRow2.getParent()).removeView(expandableNotificationRow2);
                        }
                        this.mVisualStabilityManager.notifyViewAddition(expandableNotificationRow2);
                        expandableNotificationRow.addChildNotification(expandableNotificationRow2, i2);
                        this.mStackScroller.notifyGroupChildAdded(expandableNotificationRow2);
                    }
                    i2++;
                }
                z |= expandableNotificationRow.applyChildOrder(list, this.mVisualStabilityManager, this);
            }
        }
        if (z) {
            this.mStackScroller.generateChildOrderChangedEvent();
        }
    }

    private void removeNotificationChildren() {
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < this.mStackScroller.getChildCount(); i++) {
            View childAt = this.mStackScroller.getChildAt(i);
            if (childAt instanceof ExpandableNotificationRow) {
                ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) childAt;
                List<ExpandableNotificationRow> notificationChildren = expandableNotificationRow.getNotificationChildren();
                List list = this.mTmpChildOrderMap.get(expandableNotificationRow);
                if (notificationChildren != null) {
                    arrayList.clear();
                    for (ExpandableNotificationRow next : notificationChildren) {
                        if ((list == null || !list.contains(next)) && !next.keepInParent()) {
                            arrayList.add(next);
                        }
                    }
                    Iterator it = arrayList.iterator();
                    while (it.hasNext()) {
                        ExpandableNotificationRow expandableNotificationRow2 = (ExpandableNotificationRow) it.next();
                        expandableNotificationRow.removeChildNotification(expandableNotificationRow2);
                        if (this.mNotificationData.get(expandableNotificationRow2.getStatusBarNotification().getKey()) == null) {
                            this.mStackScroller.notifyGroupChildRemoved(expandableNotificationRow2, expandableNotificationRow.getChildrenContainer());
                        }
                    }
                }
            }
        }
    }

    public void addQsTile(ComponentName componentName) {
        this.mQSPanel.getHost().addTile(componentName);
    }

    public void remQsTile(ComponentName componentName) {
        this.mQSPanel.getHost().removeTile(componentName);
    }

    public void clickTile(ComponentName componentName) {
        this.mQSPanel.clickTile(componentName);
    }

    private boolean packageHasVisibilityOverride(String str) {
        return this.mNotificationData.getVisibilityOverride(str) == 0;
    }

    public void updateClearAll() {
        this.mNotificationPanel.tryUpdateDismissView(this.mState == 0 && this.mExpandedVisible && !this.mHeadsUpManager.hasPinnedHeadsUp() && !this.mHeadsUpManager.isHeadsUpGoingAway() && hasActiveClearableNotifications());
    }

    private boolean hasActiveClearableNotifications() {
        int childCount = this.mStackScroller.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = this.mStackScroller.getChildAt(i);
            if (childAt.getVisibility() != 8 && (childAt instanceof ExpandableNotificationRow) && ((ExpandableNotificationRow) childAt).canViewBeDismissed()) {
                return true;
            }
        }
        return false;
    }

    private void updateEmptyShadeView() {
        boolean z = true;
        if (this.mState == 1 || this.mNotificationData.getActiveNotifications().size() != 0) {
            z = false;
        }
        this.mNotificationPanel.showEmptyShadeView(z);
    }

    private void updateSpeedBumpIndex() {
        int childCount = this.mStackScroller.getChildCount();
        boolean z = false;
        int i = 0;
        int i2 = 0;
        for (int i3 = 0; i3 < childCount; i3++) {
            View childAt = this.mStackScroller.getChildAt(i3);
            if (childAt.getVisibility() != 8 && (childAt instanceof ExpandableNotificationRow)) {
                i2++;
                if (!this.mNotificationData.isAmbient(((ExpandableNotificationRow) childAt).getStatusBarNotification().getKey())) {
                    i = i2;
                }
            }
        }
        if (i == childCount) {
            z = true;
        }
        this.mStackScroller.updateSpeedBumpIndex(i, z);
    }

    public static boolean isTopLevelChild(NotificationData.Entry entry) {
        return entry.row.getParent() instanceof NotificationStackScrollLayout;
    }

    /* access modifiers changed from: private */
    public void updateNotificationViewsOnly() {
        updateNotificationShade();
    }

    public void updateNotifications() {
        this.mNotificationData.filterAndSort();
        updateNotificationViewsOnly();
    }

    public void requestNotificationUpdate() {
        updateNotifications();
    }

    /* access modifiers changed from: protected */
    public void setAreThereNotifications() {
        AnonymousClass38 r0;
        boolean z = true;
        if (SPEW) {
            Log.d("StatusBar", "setAreThereNotifications: N=" + this.mNotificationData.getActiveNotifications().size() + " any=" + hasActiveNotifications() + " clearable=" + (hasActiveNotifications() && hasActiveClearableNotifications()));
        }
        PhoneStatusBarView phoneStatusBarView = this.mStatusBarView;
        if (phoneStatusBarView != null) {
            final View findViewById = phoneStatusBarView.findViewById(R.id.notification_lights_out);
            boolean z2 = hasActiveNotifications() && !areLightsOn();
            if (findViewById.getAlpha() != 1.0f) {
                z = false;
            }
            if (z2 != z) {
                float f = 0.0f;
                if (z2) {
                    findViewById.setAlpha(0.0f);
                    findViewById.setVisibility(0);
                }
                ViewPropertyAnimator animate = findViewById.animate();
                if (z2) {
                    f = 1.0f;
                }
                ViewPropertyAnimator interpolator = animate.alpha(f).setDuration(z2 ? 750 : 250).setInterpolator(new AccelerateInterpolator(2.0f));
                if (z2) {
                    r0 = null;
                } else {
                    r0 = new AnimatorListenerAdapter() {
                        public void onAnimationEnd(Animator animator) {
                            findViewById.setVisibility(8);
                        }
                    };
                }
                interpolator.setListener(r0).start();
            }
        }
        findAndUpdateMediaNotifications();
    }

    public void findAndUpdateMediaNotifications() {
        boolean z;
        NotificationData.Entry entry;
        MediaController mediaController;
        MediaSession.Token token;
        synchronized (this.mNotificationData) {
            ArrayList<NotificationData.Entry> activeNotifications = this.mNotificationData.getActiveNotifications();
            int size = activeNotifications.size();
            z = false;
            int i = 0;
            while (true) {
                if (i >= size) {
                    entry = null;
                    mediaController = null;
                    break;
                }
                entry = activeNotifications.get(i);
                if (entry.isMediaNotification() && (token = (MediaSession.Token) entry.notification.getNotification().extras.getParcelable("android.mediaSession")) != null) {
                    mediaController = new MediaController(this.mContext, token);
                    if (3 == getMediaControllerPlaybackState(mediaController)) {
                        if (DEBUG_MEDIA) {
                            Log.v("StatusBar", "DEBUG_MEDIA: found mediastyle controller matching " + entry.notification.getKey());
                        }
                    }
                }
                i++;
            }
            if (entry == null && this.mMediaSessionManager != null) {
                for (MediaController mediaController2 : this.mMediaSessionManager.getActiveSessionsForUser((ComponentName) null, -1)) {
                    if (3 == getMediaControllerPlaybackState(mediaController2)) {
                        String packageName = mediaController2.getPackageName();
                        int i2 = 0;
                        while (true) {
                            if (i2 >= size) {
                                break;
                            }
                            NotificationData.Entry entry2 = activeNotifications.get(i2);
                            if (entry2.notification.isSubstituteNotification() || !entry2.notification.getPackageName().equals(packageName)) {
                                i2++;
                            } else {
                                if (DEBUG_MEDIA) {
                                    Log.v("StatusBar", "DEBUG_MEDIA: found controller matching " + entry2.notification.getKey());
                                }
                                mediaController = mediaController2;
                                entry = entry2;
                            }
                        }
                    }
                }
            }
            if (mediaController != null && !sameSessions(this.mMediaController, mediaController)) {
                clearCurrentMediaNotification();
                this.mMediaController = mediaController;
                this.mMediaController.registerCallback(this.mMediaListener);
                this.mMediaMetadata = this.mMediaController.getMetadata();
                if (DEBUG_MEDIA) {
                    Log.v("StatusBar", "DEBUG_MEDIA: insert listener, receive metadata: " + this.mMediaMetadata);
                }
                if (entry != null) {
                    this.mMediaNotificationKey = entry.notification.getKey();
                    if (DEBUG_MEDIA) {
                        Log.v("StatusBar", "DEBUG_MEDIA: Found new media notification: key=" + this.mMediaNotificationKey + " controller=" + this.mMediaController);
                    }
                }
                z = true;
            }
        }
        if (z) {
            updateNotifications();
        }
        updateMediaMetaData(z, true);
    }

    private int getMediaControllerPlaybackState(MediaController mediaController) {
        PlaybackState playbackState;
        if (mediaController == null || (playbackState = mediaController.getPlaybackState()) == null) {
            return 0;
        }
        return playbackState.getState();
    }

    /* access modifiers changed from: private */
    public void clearCurrentMediaNotification() {
        this.mMediaNotificationKey = null;
        this.mMediaMetadata = null;
        if (this.mMediaController != null) {
            if (DEBUG_MEDIA) {
                Log.v("StatusBar", "DEBUG_MEDIA: Disconnecting from old controller: " + this.mMediaController.getPackageName());
            }
            this.mMediaController.unregisterCallback(this.mMediaListener);
        }
        this.mMediaController = null;
    }

    private boolean sameSessions(MediaController mediaController, MediaController mediaController2) {
        if (mediaController == mediaController2) {
            return true;
        }
        if (mediaController == null) {
            return false;
        }
        return mediaController.controlsSameSession(mediaController2);
    }

    public void updateMediaMetaData(boolean z, boolean z2) {
        Trace.beginSection("StatusBar#updateMediaMetaData");
        BackDropView backDropView = this.mBackdrop;
        if (backDropView == null) {
            Trace.endSection();
        } else if (this.mLaunchTransitionFadingAway) {
            backDropView.setVisibility(4);
            Trace.endSection();
        } else {
            if (DEBUG_MEDIA) {
                Log.v("StatusBar", "DEBUG_MEDIA: updating album art for notification " + this.mMediaNotificationKey + " metadata=" + this.mMediaMetadata + " metaDataChanged=" + z + " state=" + this.mState);
            }
            MediaMetadata mediaMetadata = this.mMediaMetadata;
            StatusBarKeyguardViewManager statusBarKeyguardViewManager = this.mStatusBarKeyguardViewManager;
            boolean z3 = statusBarKeyguardViewManager != null && statusBarKeyguardViewManager.isOccluded();
            if (DEBUG_MEDIA_FAKE_ARTWORK && this.mState != 0 && this.mFingerprintUnlockController.getMode() != 2 && !z3) {
                if (this.mBackdrop.getVisibility() != 0) {
                    this.mBackdrop.setVisibility(0);
                    if (z2) {
                        this.mBackdrop.setAlpha(0.002f);
                        this.mBackdrop.animate().alpha(1.0f);
                    } else {
                        this.mBackdrop.animate().cancel();
                        this.mBackdrop.setAlpha(1.0f);
                    }
                    this.mStatusBarWindowManager.setBackdropShowing(true);
                    if (DEBUG_MEDIA) {
                        Log.v("StatusBar", "DEBUG_MEDIA: Fading in album artwork");
                    }
                    z = true;
                }
                if (z) {
                    if (this.mBackdropBack.getDrawable() != null) {
                        this.mBackdropFront.setImageDrawable(this.mBackdropBack.getDrawable().getConstantState().newDrawable(this.mBackdropFront.getResources()).mutate());
                        if (this.mScrimSrcModeEnabled) {
                            this.mBackdropFront.getDrawable().mutate().setXfermode(this.mSrcOverXferMode);
                        }
                        this.mBackdropFront.setAlpha(1.0f);
                        this.mBackdropFront.setVisibility(0);
                    } else {
                        this.mBackdropFront.setVisibility(4);
                    }
                    if (DEBUG_MEDIA_FAKE_ARTWORK) {
                        int random = -16777216 | ((int) (Math.random() * 1.6777215E7d));
                        Log.v("StatusBar", String.format("DEBUG_MEDIA: setting new color: 0x%08x", new Object[]{Integer.valueOf(random)}));
                        this.mBackdropBack.setBackgroundColor(-1);
                        this.mBackdropBack.setImageDrawable(new ColorDrawable(random));
                    } else {
                        this.mBackdropBack.setImageDrawable((Drawable) null);
                    }
                    if (this.mScrimSrcModeEnabled) {
                        this.mBackdropBack.getDrawable().mutate().setXfermode(this.mSrcXferMode);
                    }
                    if (this.mBackdropFront.getVisibility() == 0) {
                        if (DEBUG_MEDIA) {
                            Log.v("StatusBar", "DEBUG_MEDIA: Crossfading album artwork from " + this.mBackdropFront.getDrawable() + " to " + this.mBackdropBack.getDrawable());
                        }
                        this.mBackdropFront.animate().setDuration(250).alpha(0.0f).withEndAction(this.mHideBackdropFront);
                    }
                }
            } else if (this.mBackdrop.getVisibility() != 8) {
                if (DEBUG_MEDIA) {
                    Log.v("StatusBar", "DEBUG_MEDIA: Fading out album artwork");
                }
                if (this.mFingerprintUnlockController.getMode() == 2 || z3) {
                    this.mBackdrop.setVisibility(8);
                    this.mBackdropBack.setImageDrawable((Drawable) null);
                    this.mStatusBarWindowManager.setBackdropShowing(false);
                } else {
                    this.mStatusBarWindowManager.setBackdropShowing(false);
                    this.mBackdrop.animate().alpha(0.002f).setInterpolator(Interpolators.ACCELERATE_DECELERATE).setDuration(300).setStartDelay(0).withEndAction(new Runnable() {
                        public void run() {
                            StatusBar.this.mBackdrop.setVisibility(8);
                            StatusBar.this.mBackdropFront.animate().cancel();
                            StatusBar.this.mBackdropBack.setImageDrawable((Drawable) null);
                            StatusBar statusBar = StatusBar.this;
                            statusBar.mHandler.post(statusBar.mHideBackdropFront);
                        }
                    });
                    if (this.mKeyguardFadingAway) {
                        this.mBackdrop.animate().setDuration(this.mKeyguardFadingAwayDuration / 2).setStartDelay(this.mKeyguardFadingAwayDelay).setInterpolator(Interpolators.LINEAR).start();
                    }
                }
            }
            Trace.endSection();
        }
    }

    private void updateReportRejectedTouchVisibility() {
        View view = this.mReportRejectedTouch;
        if (view != null) {
            view.setVisibility((this.mState != 1 || !this.mFalsingManager.isReportingEnabled()) ? 4 : 0);
        }
    }

    public void setImeWindowStatus(IBinder iBinder, int i, int i2, boolean z) {
        this.mSoftInputVisible = (i & 2) != 0;
    }

    public void disable(int i, int i2, boolean z) {
        NavigationBarView navigationBarView;
        int i3 = i;
        int i4 = i2;
        int i5 = this.mStatusBarWindowState;
        EventLog.writeEvent(30099, i3);
        int i6 = this.mDisabled1;
        int i7 = i3 ^ i6;
        this.mDisabled1 = i3;
        int i8 = this.mDisabled2;
        int i9 = i4 ^ i8;
        this.mDisabled2 = i4;
        if (DEBUG) {
            Log.d("StatusBar", String.format("disable1: 0x%08x -> 0x%08x (diff1: 0x%08x)", new Object[]{Integer.valueOf(i6), Integer.valueOf(i), Integer.valueOf(i7)}));
            Log.d("StatusBar", String.format("disable2: 0x%08x -> 0x%08x (diff2: 0x%08x)", new Object[]{Integer.valueOf(i8), Integer.valueOf(i2), Integer.valueOf(i9)}));
        }
        StringBuilder sb = new StringBuilder();
        sb.append("disable<");
        int i10 = i3 & 65536;
        sb.append(i10 != 0 ? 'E' : 'e');
        int i11 = 65536 & i7;
        sb.append(i11 != 0 ? '!' : ' ');
        char c = 'I';
        sb.append((i3 & 131072) != 0 ? 'I' : 'i');
        sb.append((131072 & i7) != 0 ? '!' : ' ');
        int i12 = i3 & 262144;
        sb.append(i12 != 0 ? 'A' : 'a');
        int i13 = i7 & 262144;
        sb.append(i13 != 0 ? '!' : ' ');
        sb.append((i3 & 1048576) != 0 ? 'S' : 's');
        sb.append((i7 & 1048576) != 0 ? '!' : ' ');
        sb.append((4194304 & i3) != 0 ? 'B' : 'b');
        sb.append((4194304 & i7) != 0 ? '!' : ' ');
        sb.append((2097152 & i3) != 0 ? 'H' : 'h');
        sb.append((2097152 & i7) != 0 ? '!' : ' ');
        int i14 = 16777216 & i3;
        sb.append(i14 != 0 ? 'R' : 'r');
        int i15 = 16777216 & i7;
        sb.append(i15 != 0 ? '!' : ' ');
        sb.append((8388608 & i3) != 0 ? 'C' : 'c');
        sb.append((8388608 & i7) != 0 ? '!' : ' ');
        sb.append((33554432 & i3) != 0 ? 'S' : 's');
        sb.append((33554432 & i7) != 0 ? '!' : ' ');
        sb.append("> disable2<");
        sb.append((i4 & 1) != 0 ? 'Q' : 'q');
        int i16 = i9 & 1;
        sb.append(i16 != 0 ? '!' : ' ');
        if ((i4 & 2) == 0) {
            c = 'i';
        }
        sb.append(c);
        sb.append((i9 & 2) != 0 ? '!' : ' ');
        int i17 = i4 & 4;
        sb.append(i17 != 0 ? 'N' : 'n');
        int i18 = i9 & 4;
        int i19 = i17;
        sb.append(i18 != 0 ? '!' : ' ');
        sb.append((i4 & 8) != 0 ? 'G' : 'g');
        sb.append((i9 & 8) != 0 ? '!' : ' ');
        sb.append((i4 & 16) != 0 ? 'R' : 'r');
        sb.append((i9 & 16) != 0 ? '!' : ' ');
        sb.append('>');
        Log.d("StatusBar", sb.toString());
        if (!(i11 == 0 || i10 == 0)) {
            animateCollapsePanels();
        }
        if (!(i15 == 0 || i14 == 0)) {
            this.mHandler.removeMessages(1020);
            this.mHandler.sendEmptyMessage(1020);
        }
        if (!((56623616 & i7) == 0 || (navigationBarView = this.mNavigationBarView) == null)) {
            navigationBarView.setDisabledFlags(i3);
        }
        if (i13 != 0) {
            this.mDisableNotificationAlerts = i12 != 0;
            updateHeadsUpSetting();
        }
        if (i16 != 0) {
            updateQsExpansionEnabled();
        }
        if (i18 != 0) {
            updateQsExpansionEnabled();
            if (i19 != 0) {
                animateCollapsePanels();
            }
        }
        this.mIsStatusBarHidden = (this.mDisabled1 & 256) != 0;
        if ((i7 & 256) != 0) {
            boolean z2 = this.mIsStatusBarHidden;
            if (this.mIsFsgMode) {
                Intent intent = new Intent();
                intent.setAction("com.android.systemui.fullscreen.statechange");
                intent.putExtra("isEnter", z2);
                this.mContext.sendBroadcast(intent);
            }
        }
        if ((i7 & 1024) != 0) {
            this.mDisableFloatNotification = (this.mDisabled1 & 1024) != 0;
        }
    }

    public int getFlagDisable1() {
        return this.mDisabled1;
    }

    public boolean isUseHeadsUp() {
        return this.mUseHeadsUp;
    }

    private void updateHeadsUpSetting() {
        boolean z = this.mUseHeadsUp;
        this.mUseHeadsUp = !this.mDisableNotificationAlerts;
        StringBuilder sb = new StringBuilder();
        sb.append("heads up is ");
        sb.append(this.mUseHeadsUp ? "enabled" : "disabled");
        Log.d("StatusBar", sb.toString());
        boolean z2 = this.mUseHeadsUp;
        if (z != z2 && !z2) {
            Log.d("StatusBar", "dismissing any existing heads up notification on disable event");
            this.mHeadsUpManager.releaseAllImmediately();
        }
    }

    public void recomputeDisableFlags(boolean z) {
        this.mCommandQueue.recomputeDisableFlags(z);
    }

    /* access modifiers changed from: protected */
    public H createHandler() {
        return new H();
    }

    private W createBgHandler() {
        this.mBgThread = new HandlerThread("StatusBar", 10);
        this.mBgThread.start();
        return new W(this.mBgThread.getLooper());
    }

    public void startActivity(Intent intent, boolean z, boolean z2, int i) {
        startActivityDismissingKeyguard(intent, z, z2, i);
    }

    public void startActivity(Intent intent, boolean z) {
        startActivityDismissingKeyguard(intent, false, z);
    }

    public void startActivity(Intent intent, boolean z, boolean z2) {
        startActivityDismissingKeyguard(intent, z, z2);
    }

    public void startActivity(Intent intent, boolean z, ActivityStarter.Callback callback) {
        startActivityDismissingKeyguard(intent, false, z, callback, 0);
    }

    public void setQsExpanded(boolean z) {
        this.mStatusBarWindowManager.setQsExpanded(z);
        this.mKeyguardClock.setImportantForAccessibility(z ? 4 : 0);
    }

    public boolean isGoingToNotificationShade() {
        return this.mLeaveOpenOnKeyguardHide;
    }

    public boolean isWakeUpComingFromTouch() {
        return this.mWakeUpComingFromTouch;
    }

    public boolean isFalsingThresholdNeeded() {
        return getBarState() == 1;
    }

    public boolean isDozing() {
        return this.mDozing;
    }

    public String getCurrentMediaNotificationKey() {
        return this.mMediaNotificationKey;
    }

    public boolean isScrimSrcModeEnabled() {
        return this.mScrimSrcModeEnabled;
    }

    public void onKeyguardViewManagerStatesUpdated() {
        logStateToEventlog();
    }

    public void onUnlockMethodStateChanged() {
        logStateToEventlog();
    }

    public void onHeadsUpPinnedModeChanged(boolean z) {
        this.mInPinnedMode = z;
        if (z) {
            this.mStatusBarWindowManager.setHeadsUpShowing(true);
            this.mStatusBarWindowManager.setForceStatusBarVisible(true);
            if (this.mNotificationPanel.isFullyCollapsed()) {
                this.mNotificationPanel.requestLayout();
                this.mStatusBarWindowManager.setForceWindowCollapsed(true);
                this.mNotificationPanel.post(new Runnable() {
                    public void run() {
                        StatusBar.this.mStatusBarWindowManager.setForceWindowCollapsed(false);
                    }
                });
            }
            updateFsgState();
        } else if (!this.mNotificationPanel.isFullyCollapsed() || this.mNotificationPanel.isTracking()) {
            this.mStatusBarWindowManager.setHeadsUpShowing(false);
            updateFsgState();
        } else {
            this.mHeadsUpManager.setHeadsUpGoingAway(true);
            this.mStackScroller.runAfterAnimationFinished(new Runnable() {
                public void run() {
                    if (!StatusBar.this.mHeadsUpManager.hasPinnedHeadsUp()) {
                        StatusBar.this.mStatusBarWindowManager.setHeadsUpShowing(false);
                        StatusBar.this.mHeadsUpManager.setHeadsUpGoingAway(false);
                    }
                    StatusBar.this.removeRemoteInputEntriesKeptUntilCollapsed();
                }
            });
        }
    }

    public void onHeadsUpPinned(ExpandableNotificationRow expandableNotificationRow) {
        dismissVolumeDialog();
    }

    public void onHeadsUpStateChanged(NotificationData.Entry entry, boolean z) {
        if (z || !this.mHeadsUpEntriesToRemoveOnSwitch.contains(entry)) {
            updateNotificationRanking((NotificationListenerService.RankingMap) null);
        } else {
            removeNotification(entry.key, this.mLatestRankingMap);
            this.mHeadsUpEntriesToRemoveOnSwitch.remove(entry);
            if (this.mHeadsUpEntriesToRemoveOnSwitch.isEmpty()) {
                this.mLatestRankingMap = null;
            }
        }
        if (!z) {
            sendExitFloatingIntent(entry.notification);
        }
    }

    private void sendExitFloatingIntent(ExpandedNotification expandedNotification) {
        if (expandedNotification != null && MiuiNotificationCompat.getExitFloatingIntent(expandedNotification.getNotification()) != null) {
            try {
                Log.d("StatusBar", "Notification has exitFloatingIntent; sending exitFloatingIntent");
                MiuiNotificationCompat.getExitFloatingIntent(expandedNotification.getNotification()).send();
            } catch (PendingIntent.CanceledException e) {
                Log.e("StatusBar", "floating intent send occur exception", e);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void updateHeadsUp(String str, NotificationData.Entry entry, boolean z, boolean z2) {
        boolean isHeadsUp = isHeadsUp(str);
        Log.d("StatusBar", String.format("updateHeadsUp wasHeadsUp=%b shouldPeek=%b alertAgain=%b", new Object[]{Boolean.valueOf(isHeadsUp), Boolean.valueOf(z), Boolean.valueOf(z2)}));
        if (isHeadsUp) {
            if (!z) {
                this.mHeadsUpManager.removeNotification(str, false);
            } else {
                this.mHeadsUpManager.updateNotification(entry, z2);
            }
        } else if (z && z2 && verifyHeadsUpInflateFlags(entry.row.getReInflateFlags())) {
            this.mHeadsUpManager.showNotification(entry);
        }
    }

    /* access modifiers changed from: protected */
    public void setHeadsUpUser(int i) {
        HeadsUpManager headsUpManager = this.mHeadsUpManager;
        if (headsUpManager != null) {
            headsUpManager.setUser(i);
        }
    }

    public boolean isHeadsUpPinned() {
        return this.mHeadsUpManager.hasPinnedHeadsUp();
    }

    public boolean isHeadsUp(String str) {
        return this.mHeadsUpManager.isHeadsUp(str);
    }

    public boolean isHeadsUp() {
        return this.mHeadsUpManager.isHeadsUp();
    }

    public boolean isSnoozedPackage(StatusBarNotification statusBarNotification) {
        return this.mHeadsUpManager.isSnoozed(statusBarNotification.getPackageName());
    }

    public boolean isKeyguardCurrentlySecure() {
        return !this.mUnlockMethodCache.canSkipBouncer();
    }

    public void setPanelExpanded(boolean z) {
        this.mPanelExpanded = z;
        this.mStatusBarWindowManager.setPanelExpanded(z);
        this.mVisualStabilityManager.setPanelExpanded(z);
        if (z && getBarState() != 1) {
            if (DEBUG) {
                Log.v("StatusBar", "clearing notification effects from setPanelExpanded");
            }
            clearNotificationEffects();
        }
        if (!z) {
            removeRemoteInputEntriesKeptUntilCollapsed();
        }
    }

    /* access modifiers changed from: private */
    public void removeRemoteInputEntriesKeptUntilCollapsed() {
        for (int i = 0; i < this.mRemoteInputEntriesToRemoveOnCollapse.size(); i++) {
            NotificationData.Entry valueAt = this.mRemoteInputEntriesToRemoveOnCollapse.valueAt(i);
            this.mRemoteInputController.removeRemoteInput(valueAt, (Object) null);
            removeNotification(valueAt.key, this.mLatestRankingMap);
        }
        this.mRemoteInputEntriesToRemoveOnCollapse.clear();
    }

    public void onScreenTurnedOff() {
        this.mFalsingManager.onScreenOff();
        this.mStatusBarWindowManager.setNotTouchable(false);
    }

    public NotificationStackScrollLayout getNotificationScrollLayout() {
        return this.mStackScroller;
    }

    public boolean isPulsing() {
        return this.mDozeScrimController.isPulsing();
    }

    public void onReorderingAllowed() {
        updateNotifications();
    }

    public boolean isLaunchTransitionFadingAway() {
        return this.mLaunchTransitionFadingAway;
    }

    public boolean hideStatusBarIconsWhenExpanded() {
        return this.mNotificationPanel.hideStatusBarIconsWhenExpanded();
    }

    protected class H extends Handler {
        protected H() {
        }

        public void handleMessage(Message message) {
            int i = message.what;
            if (i == 1026) {
                StatusBar.this.toggleKeyboardShortcuts(message.arg1);
            } else if (i != 1027) {
                switch (i) {
                    case 1000:
                        StatusBar.this.animateExpandNotificationsPanel();
                        return;
                    case b.a /*1001*/:
                        StatusBar.this.animateCollapsePanels();
                        return;
                    case b.b /*1002*/:
                        StatusBar.this.animateExpandSettingsPanel((String) message.obj);
                        return;
                    case b.c /*1003*/:
                        StatusBar.this.onLaunchTransitionTimeout();
                        return;
                    case b.d /*1004*/:
                        StatusBar.this.onUpdateFsgState();
                        return;
                    case b.e /*1005*/:
                        SomeArgs someArgs = (SomeArgs) message.obj;
                        StatusBar.this.updateNotificationRankingDelayed((NotificationListenerService.RankingMap) someArgs.arg1, ((Long) someArgs.arg2).longValue());
                        return;
                    case b.f /*1006*/:
                        StatusBar.this.wakeUpForNotification((NotificationData.Entry) message.obj);
                        return;
                    default:
                        return;
                }
            } else {
                StatusBar.this.dismissKeyboardShortcuts();
            }
        }
    }

    private final class W extends Handler {
        W(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message message) {
            super.handleMessage(message);
            int i = message.what;
            if (i == 2001) {
                StatusBar.this.beep((String) message.obj);
            } else if (i == 2002) {
                StatusBar.this.updateMessage((AppMessage) message.obj);
            }
        }
    }

    /* access modifiers changed from: private */
    public void updateMessage(AppMessage appMessage) {
        updateAppBadgeNum(appMessage.pkgName, appMessage.className, appMessage.num, appMessage.userId, false);
    }

    /* access modifiers changed from: private */
    public void beep(String str) {
        if (this.sService == null) {
            this.sService = INotificationManager.Stub.asInterface(ServiceManager.getService("notification"));
        }
        try {
            this.sService.getClass().getMethod("buzzBeepBlinkForNotification", new Class[]{String.class}).invoke(this.sService, new Object[]{str});
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e2) {
            e2.printStackTrace();
        } catch (IllegalAccessException e3) {
            e3.printStackTrace();
        }
    }

    public void maybeEscalateHeadsUp() {
        for (HeadsUpManager.HeadsUpEntry next : this.mHeadsUpManager.getAllEntries()) {
            ExpandedNotification expandedNotification = next.entry.notification;
            Notification notification = expandedNotification.getNotification();
            if (notification.fullScreenIntent != null) {
                Log.d("StatusBar", "converting a heads up to fullScreen");
                try {
                    EventLog.writeEvent(36003, expandedNotification.getKey());
                    notification.fullScreenIntent.send();
                    next.entry.notifyFullScreenIntentLaunched();
                } catch (PendingIntent.CanceledException e) {
                    Log.e("StatusBar", "throw exception when sending full screen intent", e);
                }
            }
        }
        this.mHeadsUpManager.releaseAllImmediately();
    }

    public void handleSystemNavigationKey(int i) {
        if (SPEW) {
            Log.d("StatusBar", "handleSystemNavigationKey: " + i);
        }
        if (panelsEnabled() && this.mKeyguardMonitor.isDeviceInteractive()) {
            if ((this.mKeyguardMonitor.isShowing() && !this.mKeyguardMonitor.isOccluded()) || !this.mUserSetup) {
                return;
            }
            if (280 == i) {
                MetricsLogger.action(this.mContext, 493);
                this.mNotificationPanel.collapse(false, 1.0f);
            } else if (281 == i) {
                MetricsLogger.action(this.mContext, 494);
                if (this.mNotificationPanel.isFullyCollapsed()) {
                    this.mNotificationPanel.expand(true);
                    MetricsLogger.count(this.mContext, "panel_open", 1);
                } else if (!this.mNotificationPanel.isInSettings() && !this.mNotificationPanel.isExpanding()) {
                    this.mNotificationPanel.flingSettings(0.0f, true);
                    MetricsLogger.count(this.mContext, "panel_open_qs", 1);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean panelsEnabled() {
        if ((!this.mSuperSaveModeOn || !((ControlPanelController) Dependency.get(ControlPanelController.class)).isUseControlCenter()) && (this.mDisabled1 & 65536) == 0 && (this.mDisabled2 & 4) == 0 && !ONLY_CORE_APPS) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void makeExpandedVisible(boolean z) {
        if (SPEW) {
            Log.d("StatusBar", "makeExpandedVisible mExpandedVisible=" + this.mExpandedVisible);
        }
        if (z || (!this.mExpandedVisible && panelsEnabled())) {
            this.mExpandedVisible = true;
            this.mStatusBarWindowManager.setPanelVisible(true);
            visibilityChanged(true);
            updateFsgState();
            this.mWaitingForKeyguardExit = false;
            recomputeDisableFlags(!z);
            setInteracting(1, true);
        }
    }

    public void animateCollapsePanels() {
        animateCollapsePanels(0);
    }

    public void postAnimateCollapsePanels() {
        this.mHandler.post(this.mAnimateCollapsePanels);
    }

    public void postAnimateForceCollapsePanels() {
        this.mHandler.post(new Runnable() {
            public void run() {
                StatusBar.this.animateCollapsePanels(0, true);
            }
        });
    }

    public void animateCollapsePanels(int i) {
        animateCollapsePanels(i, false, false, 1.0f);
    }

    public void animateCollapsePanels(int i, boolean z) {
        animateCollapsePanels(i, z, false, 1.0f);
    }

    public void animateCollapsePanels(int i, boolean z, boolean z2) {
        animateCollapsePanels(i, z, z2, 1.0f);
    }

    public void animateCollapsePanels(int i, boolean z, boolean z2, float f) {
        if (z || this.mState == 0) {
            if (SPEW) {
                Log.d("StatusBar", "animateCollapse(): mExpandedVisible=" + this.mExpandedVisible + " flags=" + i);
            }
            if ((i & 2) == 0 && !this.mHandler.hasMessages(1020)) {
                this.mHandler.removeMessages(1020);
                this.mHandler.sendEmptyMessage(1020);
            }
            if (this.mStatusBarWindow == null || !this.mNotificationPanel.canPanelBeCollapsed()) {
                this.mBubbleController.collapseStack();
                return;
            }
            if (!isKeyguardShowing()) {
                ((NotificationStat) Dependency.get(NotificationStat.class)).onPanelCollapsed(false, false, this.mStackScroller.getNotGoneNotifications());
            }
            this.mStatusBarWindowManager.setStatusBarFocusable(false);
            this.mStatusBarWindow.cancelExpandHelper();
            PhoneStatusBarView phoneStatusBarView = this.mStatusBarView;
            if (phoneStatusBarView != null) {
                phoneStatusBarView.collapsePanel(true, z2, f);
                return;
            }
            return;
        }
        runPostCollapseRunnables();
    }

    public boolean canPanelBeCollapsed() {
        return this.mNotificationPanel.canPanelBeCollapsed();
    }

    /* access modifiers changed from: private */
    public void runPostCollapseRunnables() {
        ArrayList arrayList = new ArrayList(this.mPostCollapseRunnables);
        this.mPostCollapseRunnables.clear();
        int size = arrayList.size();
        for (int i = 0; i < size; i++) {
            ((Runnable) arrayList.get(i)).run();
        }
        this.mStatusBarKeyguardViewManager.readyForKeyguardDone();
    }

    public void dispatchControlPanelTouchEvent(MotionEvent motionEvent) {
        if (getStatusBarWindow() != null && getStatusBarWindow().mControllerPanel != null && getStatusBarWindow().mControllerPanel.panelEnabled()) {
            getStatusBarWindow().mControllerPanel.dispatchTouchEvent(motionEvent);
            int action = motionEvent.getAction();
            if (action == 0) {
                ((ControlPanelWindowManager) Dependency.get(ControlPanelWindowManager.class)).setNotTouchable(true);
            } else if (action == 1 || action == 3) {
                ((ControlPanelWindowManager) Dependency.get(ControlPanelWindowManager.class)).setNotTouchable(false);
            }
        }
    }

    public void dispatchNotificationsPanelTouchEvent(MotionEvent motionEvent) {
        if (panelsEnabled()) {
            this.mNotificationPanel.dispatchTouchEvent(motionEvent);
            int action = motionEvent.getAction();
            if (action == 0) {
                this.mStatusBarWindowManager.setNotTouchable(true);
            } else if (action == 1 || action == 3) {
                this.mStatusBarWindowManager.setNotTouchable(false);
            }
        }
    }

    public void animateExpandNotificationsPanel() {
        if (SPEW) {
            Log.d("StatusBar", "animateExpand: mExpandedVisible=" + this.mExpandedVisible);
        }
        if (panelsEnabled()) {
            this.mNotificationPanel.expand(true);
        }
    }

    public void animateExpandSettingsPanel(String str) {
        if (SPEW) {
            Log.d("StatusBar", "animateExpand: mExpandedVisible=" + this.mExpandedVisible);
        }
        if (panelsEnabled() && this.mUserSetup && !((ControlPanelController) Dependency.get(ControlPanelController.class)).isUseControlCenter()) {
            if (str != null) {
                this.mQSPanel.openDetails(str);
            }
            this.mNotificationPanel.expandWithQs();
        }
    }

    public void animateCollapseQuickSettings() {
        if (this.mState == 0) {
            this.mStatusBarView.collapsePanel(true, false, 1.0f);
        }
    }

    /* access modifiers changed from: package-private */
    public void makeExpandedInvisible() {
        if (SPEW) {
            Log.d("StatusBar", "makeExpandedInvisible: mExpandedVisible=" + this.mExpandedVisible);
        }
        if (this.mExpandedVisible && this.mStatusBarWindow != null) {
            ((NotificationStat) Dependency.get(NotificationStat.class)).onPanelCollapsed(false, true, this.mStackScroller.getNotGoneNotifications());
            HeadsUpAnimatedStubView headsUpAnimatedStubView = this.mHeadsUpAnimatedStub;
            if (headsUpAnimatedStubView != null) {
                headsUpAnimatedStubView.setAnimationRunning(false);
            }
            PhoneStatusBarView phoneStatusBarView = this.mStatusBarView;
            if (phoneStatusBarView != null) {
                phoneStatusBarView.collapsePanel(false, false, 1.0f);
            }
            this.mNotificationPanel.closeQs();
            this.mExpandedVisible = false;
            visibilityChanged(false);
            updateFsgState();
            this.mStatusBarWindowManager.setPanelVisible(false);
            this.mStatusBarWindowManager.setForceStatusBarVisible(false);
            closeAndSaveGuts(true, true, true, -1, -1, true);
            runPostCollapseRunnables();
            setInteracting(1, false);
            showBouncerIfKeyguard();
            recomputeDisableFlags(this.mNotificationPanel.hideStatusBarIconsWhenExpanded());
            if (!isKeyguardShowing()) {
                WindowManagerGlobal.getInstance().trimMemory(20);
            }
        }
    }

    public boolean interceptTouchEvent(MotionEvent motionEvent) {
        if (DEBUG_GESTURES && motionEvent.getActionMasked() != 2) {
            EventLog.writeEvent(36000, new Object[]{Integer.valueOf(motionEvent.getActionMasked()), Integer.valueOf((int) motionEvent.getX()), Integer.valueOf((int) motionEvent.getY()), Integer.valueOf(this.mDisabled1), Integer.valueOf(this.mDisabled2)});
        }
        if (SPEW) {
            Log.d("StatusBar", "Touch: rawY=" + motionEvent.getRawY() + " event=" + motionEvent + " mDisabled1=" + this.mDisabled1 + " mDisabled2=" + this.mDisabled2 + " mTracking=" + this.mTracking);
        } else if (CHATTY && motionEvent.getAction() != 2) {
            Log.d("StatusBar", String.format("panel: %s at (%f, %f) mDisabled1=0x%08x mDisabled2=0x%08x", new Object[]{MotionEvent.actionToString(motionEvent.getAction()), Float.valueOf(motionEvent.getRawX()), Float.valueOf(motionEvent.getRawY()), Integer.valueOf(this.mDisabled1), Integer.valueOf(this.mDisabled2)}));
        }
        if (DEBUG_GESTURES) {
            this.mGestureRec.add(motionEvent);
        }
        if (this.mStatusBarWindowState == 0) {
            if (!(motionEvent.getAction() == 1 || motionEvent.getAction() == 3) || this.mExpandedVisible) {
                setInteracting(1, true);
            } else {
                setInteracting(1, false);
            }
        }
        return false;
    }

    public GestureRecorder getGestureRecorder() {
        return this.mGestureRec;
    }

    public FingerprintUnlockController getFingerprintUnlockController() {
        return this.mFingerprintUnlockController;
    }

    public void setWindowState(int i, int i2) {
        PhoneStatusBarView phoneStatusBarView;
        boolean z = i2 == 0;
        if (!(this.mStatusBarWindow == null || i != 1 || this.mStatusBarWindowState == i2)) {
            this.mStatusBarWindowState = i2;
            if (DEBUG_WINDOW_STATE) {
                Log.d("StatusBar", "Status bar " + StatusBarManager.windowStateToString(i2));
            }
            if (!z && this.mState == 0 && (phoneStatusBarView = this.mStatusBarView) != null) {
                phoneStatusBarView.collapsePanel(false, false, 1.0f);
            }
        }
        NavigationBarView navigationBarView = this.mNavigationBarView;
        if (navigationBarView != null) {
            navigationBarView.setWindowState(i, i2);
        }
        updateSystemUiStateFlags();
    }

    public void updateSystemUiStateFlags() {
        if (Dependency.get(OverviewProxyService.class) != null) {
            ((OverviewProxyService) Dependency.get(OverviewProxyService.class)).setSystemUiStateFlag(1048576, !isStatusBarVisible());
        }
    }

    public boolean isStatusBarVisible() {
        return this.mStatusBarWindowState == 0;
    }

    public void setSystemUiVisibility(int i, int i2, int i3, int i4, Rect rect, Rect rect2) {
        int i5 = i4;
        int i6 = this.mSystemUiVisibility;
        int i7 = ((~i5) & i6) | (i & i5);
        int i8 = i7 ^ i6;
        boolean z = false;
        if (DEBUG) {
            Log.d("StatusBar", String.format("setSystemUiVisibility vis=%s mask=%s oldVal=%s newVal=%s diff=%s", new Object[]{Integer.toHexString(i), Integer.toHexString(i4), Integer.toHexString(i6), Integer.toHexString(i7), Integer.toHexString(i8)}));
        }
        if (i8 != 0) {
            this.mSystemUiVisibility = i7;
            if ((i8 & 1) != 0) {
                setAreThereNotifications();
            }
            if ((268435456 & i) != 0) {
                this.mSystemUiVisibility &= -268435457;
                this.mNoAnimationOnNextBarModeChange = true;
            }
            int computeNavigationBarMode = this.mNavigationBarView == null ? -1 : computeNavigationBarMode(i6, i7, 134217728, Integer.MIN_VALUE, 32768);
            NavigationBarView navigationBarView = this.mNavigationBarView;
            if (navigationBarView != null && navigationBarView.isForceImmersive() && (i7 & 2) != 0 && (computeNavigationBarMode == 0 || computeNavigationBarMode == 3)) {
                computeNavigationBarMode = 1;
            }
            int computeStatusBarMode = computeStatusBarMode(i6, i7);
            boolean z2 = computeNavigationBarMode != -1;
            if (computeStatusBarMode != -1) {
                z = true;
            }
            if (z && computeStatusBarMode != this.mStatusBarMode) {
                this.mStatusBarMode = computeStatusBarMode;
                checkBarModes();
                this.mOLEDScreenHelper.onStatusBarModeChanged(this.mStatusBarMode);
            }
            if (z2 && computeNavigationBarMode != this.mNavigationBarMode) {
                this.mNavigationBarMode = computeNavigationBarMode;
                NotificationStackScrollLayout notificationStackScrollLayout = this.mStackScroller;
                if (notificationStackScrollLayout != null) {
                    notificationStackScrollLayout.setLastNavigationBarMode(this.mNavigationBarMode);
                }
                NavigationBarView navigationBarView2 = this.mNavigationBarView;
                if (navigationBarView2 != null) {
                    navigationBarView2.getBarTransitions().transitionTo(computeNavigationBarMode, true);
                    this.mNavigationBarView.setDisabledFlags(this.mDisabled1, true);
                }
            }
            if (z || z2) {
                if (this.mStatusBarMode == 1 || this.mNavigationBarMode == 1) {
                    scheduleAutohide();
                } else {
                    cancelAutohide();
                }
            }
            if ((536870912 & i) != 0) {
                this.mSystemUiVisibility &= -536870913;
            }
            if (Build.VERSION.SDK_INT > 29 && (this.mSystemUiVisibility & 201326592) != 0) {
                cancelDisableTouch();
            }
            notifyUiVisibilityChanged(this.mSystemUiVisibility);
        }
        final int i9 = Build.VERSION.SDK_INT == 23 ? i : i2;
        StatusBarWindowView statusBarWindowView = this.mStatusBarWindow;
        final int i10 = i3;
        final int i11 = i4;
        final Rect rect3 = rect;
        final Rect rect4 = rect2;
        final boolean z3 = z;
        statusBarWindowView.post(new Runnable() {
            public void run() {
                StatusBar statusBar = StatusBar.this;
                statusBar.mLightBarController.onSystemUiVisibilityChanged(i9, i10, i11, rect3, rect4, z3, statusBar.mStatusBarMode);
            }
        });
    }

    private int computeNavigationBarMode(int i, int i2, int i3, int i4, int i5) {
        int navigationBarMode = navigationBarMode(i, i3, i4, i5);
        int navigationBarMode2 = navigationBarMode(i2, i3, i4, i5);
        if (navigationBarMode == navigationBarMode2) {
            return -1;
        }
        return navigationBarMode2;
    }

    public int getNavigationBarMode() {
        return this.mNavigationBarMode;
    }

    /* access modifiers changed from: protected */
    public int computeStatusBarMode(int i, int i2) {
        return computeBarMode(i, i2, 67108864, 1073741824, 8);
    }

    /* access modifiers changed from: protected */
    public BarTransitions getStatusBarTransitions() {
        return this.mStatusBarView.getBarTransitions();
    }

    /* access modifiers changed from: protected */
    public int computeBarMode(int i, int i2, int i3, int i4, int i5) {
        int barMode = barMode(i, i3, i4, i5);
        int barMode2 = barMode(i2, i3, i4, i5);
        if (barMode == barMode2) {
            return -1;
        }
        return barMode2;
    }

    /* access modifiers changed from: package-private */
    public void checkBarModes() {
        if (this.mStatusBarView != null) {
            checkBarMode(this.mStatusBarMode, this.mStatusBarWindowState, getStatusBarTransitions());
        }
        this.mNoAnimationOnNextBarModeChange = false;
    }

    /* access modifiers changed from: package-private */
    public void checkBarMode(int i, int i2, BarTransitions barTransitions) {
        boolean z = !this.mNoAnimationOnNextBarModeChange && this.mDeviceInteractive && i2 != 2 && !(this.mBatteryController.isPowerSave() || this.mBatteryController.isExtremePowerSave());
        if (this.mForceBlack && !this.mExpandedVisible && this.mContext.getResources().getConfiguration().orientation == 1) {
            i = 0;
        }
        barTransitions.transitionTo(i, z);
    }

    private void finishBarAnimations() {
        PhoneStatusBarView phoneStatusBarView = this.mStatusBarView;
        if (phoneStatusBarView != null) {
            phoneStatusBarView.getBarTransitions().finishAnimations();
        }
    }

    public void setInteracting(int i, boolean z) {
        int i2;
        boolean z2 = true;
        if (((this.mInteractingWindows & i) != 0) == z) {
            z2 = false;
        }
        if (z) {
            i2 = this.mInteractingWindows | i;
        } else {
            i2 = this.mInteractingWindows & (~i);
        }
        this.mInteractingWindows = i2;
        if (this.mInteractingWindows != 0) {
            suspendAutohide();
        } else {
            resumeSuspendedAutohide();
        }
        if (z2 && z && i == 2) {
            dismissVolumeDialog();
        }
        checkBarModes();
    }

    private void dismissVolumeDialog() {
        VolumeComponent volumeComponent = this.mVolumeComponent;
        if (volumeComponent != null) {
            volumeComponent.dismissNow();
        }
    }

    private void resumeSuspendedAutohide() {
        if (this.mAutohideSuspended) {
            scheduleAutohide();
            this.mHandler.postDelayed(this.mCheckBarModes, 500);
        }
    }

    private void suspendAutohide() {
        this.mHandler.removeCallbacks(this.mAutohide);
        this.mHandler.removeCallbacks(this.mCheckBarModes);
        this.mAutohideSuspended = (this.mSystemUiVisibility & 201326592) != 0;
    }

    private void cancelAutohide() {
        this.mAutohideSuspended = false;
        this.mHandler.removeCallbacks(this.mAutohide);
    }

    private void scheduleAutohide() {
        cancelAutohide();
        this.mHandler.postDelayed(this.mAutohide, 2250);
    }

    private void scheduleDisableTouch() {
        StatusBarWindowManager statusBarWindowManager = this.mStatusBarWindowManager;
        if (statusBarWindowManager != null) {
            statusBarWindowManager.setNotTouchable(true);
            StatusBarWindowView statusBarWindowView = this.mStatusBarWindow;
            if (statusBarWindowView != null) {
                statusBarWindowView.setNotTouchable(true);
            }
            this.mHandler.postDelayed(this.mCancelDisableTouch, 350);
        }
    }

    /* access modifiers changed from: private */
    public void cancelDisableTouch() {
        this.mHandler.removeCallbacks(this.mCancelDisableTouch);
        StatusBarWindowManager statusBarWindowManager = this.mStatusBarWindowManager;
        if (statusBarWindowManager != null) {
            statusBarWindowManager.setNotTouchable(false);
        }
        StatusBarWindowView statusBarWindowView = this.mStatusBarWindow;
        if (statusBarWindowView != null) {
            statusBarWindowView.setNotTouchable(false);
        }
    }

    /* access modifiers changed from: package-private */
    public void checkUserAutohide(View view, MotionEvent motionEvent) {
        if ((this.mSystemUiVisibility & 201326592) != 0 && motionEvent.getAction() == 4 && motionEvent.getX() == 0.0f && motionEvent.getY() == 0.0f && !this.mRemoteInputController.isRemoteInputActive()) {
            userAutohide();
        }
    }

    /* access modifiers changed from: private */
    public void checkRemoteInputOutside(MotionEvent motionEvent) {
        if (motionEvent.getAction() == 4 && motionEvent.getX() == 0.0f && motionEvent.getY() == 0.0f && this.mRemoteInputController.isRemoteInputActive()) {
            this.mRemoteInputController.closeRemoteInputs();
        }
    }

    private void userAutohide() {
        cancelAutohide();
        this.mHandler.postDelayed(this.mAutohide, 350);
    }

    private boolean areLightsOn() {
        return (this.mSystemUiVisibility & 1) == 0;
    }

    public void setLightsOn(boolean z) {
        Log.v("StatusBar", "setLightsOn(" + z + ")");
        if (z) {
            setSystemUiVisibility(0, 0, 0, 1, this.mLastFullscreenStackBounds, this.mLastDockedStackBounds);
            return;
        }
        setSystemUiVisibility(1, 0, 0, 1, this.mLastFullscreenStackBounds, this.mLastDockedStackBounds);
    }

    /* access modifiers changed from: private */
    public void notifyUiVisibilityChanged(int i) {
        try {
            if (this.mLastDispatchedSystemUiVisibility != i) {
                IWindowManagerCompat.statusBarVisibilityChanged(this.mWindowManagerService, ContextCompat.getDisplayId(this.mContext), i);
                this.mLastDispatchedSystemUiVisibility = i;
            }
        } catch (RemoteException unused) {
        }
    }

    /* access modifiers changed from: private */
    public void clearTransient(int[] iArr) {
        scheduleDisableTouch();
        try {
            this.mCommandQueue.abortTransient(ContextCompat.getDisplayId(this.mContext), iArr);
        } catch (RemoteException e) {
            Log.e("StatusBar", "clearTransient: " + e);
        }
    }

    /* access modifiers changed from: private */
    public int getTransientMask() {
        int i = this.mStatusBarView != null ? 67108864 : 0;
        return this.mNavigationBarView != null ? i | 134217728 : i;
    }

    public void topAppWindowChanged(boolean z) {
        if (SPEW) {
            StringBuilder sb = new StringBuilder();
            sb.append(z ? "showing" : "hiding");
            sb.append(" the MENU button");
            Log.d("StatusBar", sb.toString());
        }
        if (z) {
            setLightsOn(true);
        }
    }

    public static String viewInfo(View view) {
        return "[(" + view.getLeft() + "," + view.getTop() + ")(" + view.getRight() + "," + view.getBottom() + ") " + view.getWidth() + "x" + view.getHeight() + "]";
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        synchronized (this.mQueueLock) {
            printWriter.println("Current Status Bar state:");
            printWriter.println("  mExpandedVisible=" + this.mExpandedVisible + ", mTrackingPosition=" + this.mTrackingPosition);
            StringBuilder sb = new StringBuilder();
            sb.append("  mTracking=");
            sb.append(this.mTracking);
            printWriter.println(sb.toString());
            printWriter.println("  mDisplayMetrics=" + this.mDisplayMetrics);
            printWriter.println("  mStackScroller: " + viewInfo(this.mStackScroller) + " scroll " + this.mStackScroller.getScrollX() + "," + this.mStackScroller.getScrollY());
        }
        printWriter.print("  mPendingNotifications=");
        if (this.mPendingNotifications.size() == 0) {
            printWriter.println("null");
        } else {
            for (NotificationData.Entry entry : this.mPendingNotifications.values()) {
                printWriter.println(entry.notification);
            }
        }
        printWriter.print("  mInteractingWindows=");
        printWriter.println(this.mInteractingWindows);
        printWriter.println("  mSystemUiVisibility=" + Integer.toHexString(this.mSystemUiVisibility));
        printWriter.println(String.format("  disable1=0x%08x disable2=0x%08x", new Object[]{Integer.valueOf(this.mDisabled1), Integer.valueOf(this.mDisabled2)}));
        printWriter.print("  mStatusBarWindowState=");
        printWriter.println(StatusBarManager.windowStateToString(this.mStatusBarWindowState));
        printWriter.print("  mStatusBarMode=");
        printWriter.println(BarTransitions.modeToString(this.mStatusBarMode));
        printWriter.print("  mWakeupForNotification=");
        printWriter.println(this.mWakeupForNotification);
        printWriter.print("  mDozing=");
        printWriter.println(this.mDozing);
        printWriter.print("  mZenMode=");
        printWriter.println(Settings.Global.zenModeToString(this.mZenMode));
        printWriter.print("  mUseHeadsUp=");
        printWriter.println(this.mUseHeadsUp);
        PhoneStatusBarView phoneStatusBarView = this.mStatusBarView;
        if (phoneStatusBarView != null) {
            dumpBarTransitions(printWriter, "mStatusBarView", phoneStatusBarView.getBarTransitions());
        }
        printWriter.print("  mMediaSessionManager=");
        printWriter.println(this.mMediaSessionManager);
        printWriter.print("  mMediaNotificationKey=");
        printWriter.println(this.mMediaNotificationKey);
        printWriter.print("  mMediaController=");
        printWriter.print(this.mMediaController);
        if (this.mMediaController != null) {
            printWriter.print(" state=" + this.mMediaController.getPlaybackState());
        }
        printWriter.println();
        printWriter.print("  mMediaMetadata=");
        printWriter.print(this.mMediaMetadata);
        if (this.mMediaMetadata != null) {
            printWriter.print(" title=" + this.mMediaMetadata.getText("android.media.metadata.TITLE"));
        }
        printWriter.println();
        this.mOLEDScreenHelper.dump(fileDescriptor, printWriter, strArr);
        printWriter.println("  Panels: ");
        if (this.mNotificationPanel != null) {
            printWriter.println("    mNotificationPanel=" + this.mNotificationPanel + " params=" + this.mNotificationPanel.getLayoutParams().debug(""));
            printWriter.print("      ");
            this.mNotificationPanel.dump(fileDescriptor, printWriter, strArr);
        }
        DozeLog.dump(printWriter);
        synchronized (this.mNotificationData) {
            this.mNotificationData.dump(printWriter, "  ");
        }
        if (DEBUG_GESTURES) {
            printWriter.print("  status bar gestures: ");
            this.mGestureRec.dump(fileDescriptor, printWriter, strArr);
        }
        AppMiniWindowManager appMiniWindowManager = this.mAppMiniWindowManager;
        if (appMiniWindowManager != null) {
            appMiniWindowManager.dump(fileDescriptor, printWriter, strArr);
        }
        HeadsUpManager headsUpManager = this.mHeadsUpManager;
        if (headsUpManager != null) {
            headsUpManager.dump(fileDescriptor, printWriter, strArr);
        } else {
            printWriter.println("  mHeadsUpManager: null");
        }
        NotificationGroupManager notificationGroupManager = this.mGroupManager;
        if (notificationGroupManager != null) {
            notificationGroupManager.dump(fileDescriptor, printWriter, strArr);
        } else {
            printWriter.println("  mGroupManager: null");
        }
        if (KeyguardUpdateMonitor.getInstance(this.mContext) != null) {
            KeyguardUpdateMonitor.getInstance(this.mContext).dump(fileDescriptor, printWriter, strArr);
        }
        FalsingManager.getInstance(this.mContext).dump(printWriter);
        FalsingLog.dump(printWriter);
        printWriter.println("SharedPreferences:");
        for (Map.Entry next : Prefs.getAll(this.mContext).entrySet()) {
            printWriter.print("  ");
            printWriter.print((String) next.getKey());
            printWriter.print("=");
            printWriter.println(next.getValue());
        }
        CloudDataHelper.dump(this.mContext, printWriter);
        printWriter.println("AppNotificationSettings:");
        for (Map.Entry next2 : this.mContext.getSharedPreferences("app_notification", 4).getAll().entrySet()) {
            printWriter.print("  ");
            printWriter.print((String) next2.getKey());
            printWriter.print("=");
            printWriter.println(next2.getValue());
        }
        printWriter.print("  mNavigationBarView=");
        NavigationBarView navigationBarView = this.mNavigationBarView;
        if (navigationBarView == null) {
            printWriter.println("null");
        } else {
            navigationBarView.dump(fileDescriptor, printWriter, strArr);
        }
        printWriter.println(" mStatusBarFragment:");
        CollapsedStatusBarFragment collapsedStatusBarFragment = this.mStatusBarFragment;
        if (collapsedStatusBarFragment != null) {
            collapsedStatusBarFragment.dump(printWriter);
        }
    }

    static void dumpBarTransitions(PrintWriter printWriter, String str, BarTransitions barTransitions) {
        printWriter.print("  ");
        printWriter.print(str);
        printWriter.print(".BarTransitions.mMode=");
        printWriter.println(BarTransitions.modeToString(barTransitions.getMode()));
    }

    public void createAndAddWindows() {
        addStatusBarWindow();
    }

    private void addStatusBarWindow() {
        makeStatusBarView();
        this.mStatusBarWindowManager = (StatusBarWindowManager) Dependency.get(StatusBarWindowManager.class);
        this.mRemoteInputController = new RemoteInputController(this.mHeadsUpManager);
        this.mStatusBarWindowManager.add(this.mStatusBarWindow, getStatusBarHeight());
    }

    /* access modifiers changed from: package-private */
    public void updateDisplaySize() {
        this.mDisplay.getMetrics(this.mDisplayMetrics);
        this.mDisplay.getSize(this.mCurrentDisplaySize);
        if (DEBUG_GESTURES) {
            this.mGestureRec.tag("display", String.format("%dx%d", new Object[]{Integer.valueOf(this.mDisplayMetrics.widthPixels), Integer.valueOf(this.mDisplayMetrics.heightPixels)}));
        }
    }

    /* access modifiers changed from: package-private */
    public float getDisplayDensity() {
        return this.mDisplayMetrics.density;
    }

    public void startActivityDismissingKeyguard(Intent intent, boolean z, boolean z2, int i) {
        startActivityDismissingKeyguard(intent, z, z2, (ActivityStarter.Callback) null, i);
    }

    public void startActivityDismissingKeyguard(Intent intent, boolean z, boolean z2) {
        startActivityDismissingKeyguard(intent, z, z2, (ActivityStarter.Callback) null, 0);
    }

    public void startActivityDismissingKeyguard(final Intent intent, boolean z, boolean z2, final ActivityStarter.Callback callback, final int i) {
        if (!z || isDeviceProvisioned()) {
            executeRunnableDismissingKeyguard(new Runnable() {
                public void run() {
                    int i;
                    StatusBar.this.mAssistManager.hideAssist();
                    intent.setFlags(335544320);
                    intent.addFlags(i);
                    ActivityOptions activityOptions = new ActivityOptions(StatusBar.getActivityOptions());
                    if (intent == KeyguardBottomAreaView.INSECURE_CAMERA_INTENT) {
                        ActivityOptionsCompat.setRotationAnimationHint(activityOptions, 3);
                    }
                    try {
                        i = ActivityManagerCompat.getService().startActivityAsUser((IApplicationThread) null, StatusBar.this.mContext.getBasePackageName(), intent, intent.resolveTypeIfNeeded(StatusBar.this.mContext.getContentResolver()), (IBinder) null, (String) null, 0, 268435456, (ProfilerInfo) null, activityOptions.toBundle(), UserHandle.CURRENT.getIdentifier());
                    } catch (RemoteException e) {
                        Log.w("StatusBar", "Unable to start activity", e);
                        i = -96;
                    }
                    ActivityStarter.Callback callback = callback;
                    if (callback != null) {
                        callback.onActivityStarted(i);
                    }
                }
            }, new Runnable() {
                public void run() {
                    ActivityStarter.Callback callback = callback;
                    if (callback != null) {
                        callback.onActivityStarted(-96);
                    }
                }
            }, z2, PreviewInflater.wouldLaunchResolverActivity(this.mContext, intent, this.mCurrentUserId), true);
        }
    }

    public void readyForKeyguardDone() {
        this.mStatusBarKeyguardViewManager.readyForKeyguardDone();
    }

    public void executeRunnableDismissingKeyguard(final Runnable runnable, Runnable runnable2, final boolean z, boolean z2, final boolean z3) {
        dismissKeyguardThenExecute(new ActivityStarter.OnDismissAction() {
            public boolean onDismiss() {
                if (runnable != null) {
                    if (!StatusBar.this.isKeyguardShowing() || !StatusBar.this.mStatusBarKeyguardViewManager.isOccluded()) {
                        AsyncTask.execute(runnable);
                    } else {
                        StatusBar.this.mStatusBarKeyguardViewManager.addAfterKeyguardGoneRunnable(runnable);
                    }
                }
                if (z) {
                    StatusBar statusBar = StatusBar.this;
                    if (statusBar.mExpandedVisible) {
                        statusBar.animateCollapsePanels(2, true, true);
                    } else {
                        statusBar.mHandler.post(new Runnable() {
                            public void run() {
                                StatusBar.this.runPostCollapseRunnables();
                            }
                        });
                    }
                } else if (StatusBar.this.isInLaunchTransition() && StatusBar.this.mNotificationPanel.isLaunchTransitionFinished()) {
                    StatusBar.this.mHandler.post(new Runnable() {
                        public void run() {
                            StatusBar.this.mStatusBarKeyguardViewManager.readyForKeyguardDone();
                        }
                    });
                }
                return z3;
            }
        }, runnable2, z2);
    }

    class AppMessage {
        CharSequence className;
        int num;
        String pkgName;
        int userId;

        AppMessage() {
        }
    }

    public void resetUserExpandedStates() {
        ArrayList<NotificationData.Entry> activeNotifications = this.mNotificationData.getActiveNotifications();
        int size = activeNotifications.size();
        for (int i = 0; i < size; i++) {
            ExpandableNotificationRow expandableNotificationRow = activeNotifications.get(i).row;
            if (expandableNotificationRow != null) {
                expandableNotificationRow.resetUserExpansion();
            }
        }
    }

    public void dismissKeyguardThenExecute(ActivityStarter.OnDismissAction onDismissAction, boolean z) {
        dismissKeyguardThenExecute(onDismissAction, (Runnable) null, z);
    }

    public void dismissKeyguardThenExecute(ActivityStarter.OnDismissAction onDismissAction, Runnable runnable, boolean z) {
        if (isKeyguardShowing()) {
            this.mStatusBarKeyguardViewManager.dismissWithAction(onDismissAction, runnable, z);
        } else {
            onDismissAction.onDismiss();
        }
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        if (DEBUG) {
            Log.v("StatusBar", "configuration changed: " + this.mContext.getResources().getConfiguration());
        }
        if (this.mPreviousConfig == null) {
            this.mPreviousConfig = new Configuration(configuration);
        }
        int updateFrom = this.mPreviousConfig.updateFrom(configuration);
        boolean isThemeResourcesChanged = Util.isThemeResourcesChanged(updateFrom, configuration.extraConfig.themeChangedFlags);
        boolean z = true;
        boolean z2 = (updateFrom & 4) != 0;
        updateResources(isThemeResourcesChanged);
        updateDisplaySize();
        updateRowStates();
        if (CustomizeUtil.HAS_NOTCH) {
            this.mForceBlackObserver.onChange(false);
        }
        this.mScreenPinningRequest.onConfigurationChanged();
        int i = this.mOrientation;
        int i2 = configuration.orientation;
        if (i != i2) {
            this.mOrientation = i2;
            NotificationStackScrollLayout notificationStackScrollLayout = this.mStackScroller;
            if (notificationStackScrollLayout != null) {
                notificationStackScrollLayout.disallowMeasureChildren(true);
            }
        }
        if (isThemeResourcesChanged) {
            ((DarkIconDispatcher) Dependency.get(DarkIconDispatcher.class)).updateResource(this.mContext);
            IconCustomizer.clearCache();
            ((AppIconsManager) Dependency.get(AppIconsManager.class)).onDensityOrFontScaleChanged();
            updateNotificationsOnDensityOrFontScaleChanged();
            inflateDismissView();
            QuickStatusBarHeader quickStatusBarHeader = this.mHeader;
            if (quickStatusBarHeader != null) {
                quickStatusBarHeader.themeChanged();
            }
        } else if (z2) {
            updateNotificationsOnDensityOrFontScaleChanged();
        }
        repositionNavigationBar();
        this.mOLEDScreenHelper.onConfigurationChanged();
        CollapsedStatusBarFragment collapsedStatusBarFragment = this.mStatusBarFragment;
        if (collapsedStatusBarFragment != null) {
            collapsedStatusBarFragment.onConfigurationChanged();
        }
        if ((updateFrom & 4096) == 0) {
            z = false;
        }
        if (z) {
            removeNavBarView();
            changeNavBarViewState();
        }
    }

    public void userSwitched(int i) {
        ((ControlPanelController) Dependency.get(ControlPanelController.class)).onUserSwitched();
        setHeadsUpUser(i);
        animateCollapsePanels();
        updatePublicMode();
        this.mNotificationData.filterAndSort();
        if (this.mReinflateNotificationsOnUserSwitched) {
            updateNotificationsOnDensityOrFontScaleChanged();
            this.mReinflateNotificationsOnUserSwitched = false;
        }
        this.mMiuiOptimizationObserver.onChange(false);
        this.mWakeupForNotificationObserver.onChange(false);
        this.mNotificationStyleObserver.onChange(false);
        this.mUserFoldObserver.onChange(false);
        this.mAODObserver.onChange(false);
        updateNotificationShade();
        clearCurrentMediaNotification();
        setLockscreenUser(i);
        Intent intent = new Intent("android.intent.action.APPLICATION_MESSAGE_QUERY");
        intent.putExtra("com.miui.extra_update_request_first_time", true);
        this.mContext.sendBroadcast(intent);
        disconnectAodService();
        startAndBindAodService();
    }

    public void setLockscreenUser(int i) {
        LockscreenWallpaper lockscreenWallpaper = this.mLockscreenWallpaper;
        if (lockscreenWallpaper == null) {
            this.mScrimController.setCurrentUser(i);
            updateMediaMetaData(true, false);
            this.mWallpaperChangedReceiver.onReceive(this.mContext, (Intent) null);
            return;
        }
        lockscreenWallpaper.setCurrentUser(i);
        throw null;
    }

    /* access modifiers changed from: package-private */
    public void updateResources(boolean z) {
        if (z) {
            this.mContext.getTheme().rebase();
        }
        loadDimens(this.mContext.getResources());
        NotificationPanelView notificationPanelView = this.mNotificationPanel;
        if (notificationPanelView != null) {
            notificationPanelView.updateResources(z);
        }
        QSPanel qSPanel = this.mQSPanel;
        if (qSPanel != null) {
            qSPanel.updateResources(z);
        }
        QuickQSPanel quickQSPanel = this.mQuickQSPanel;
        if (quickQSPanel != null) {
            quickQSPanel.updateResources(z);
        }
        BrightnessMirrorController brightnessMirrorController = this.mBrightnessMirrorController;
        if (brightnessMirrorController != null) {
            brightnessMirrorController.updateResources();
        }
        this.mMiuiStatusBarPrompt.updateTouchRegion();
    }

    /* access modifiers changed from: protected */
    public void loadDimens(Resources resources) {
        int i;
        int i2 = this.mNaturalBarHeight;
        this.mNaturalBarHeight = resources.getDimensionPixelSize(R.dimen.status_bar_height);
        StatusBarWindowManager statusBarWindowManager = this.mStatusBarWindowManager;
        if (!(statusBarWindowManager == null || (i = this.mNaturalBarHeight) == i2)) {
            statusBarWindowManager.setBarHeight(i);
        }
        updateStatusBarPading();
        KeyguardStatusBarView keyguardStatusBarView = this.mKeyguardStatusBar;
        if (!(keyguardStatusBarView == null || keyguardStatusBarView.getLayoutParams() == null)) {
            this.mKeyguardStatusBar.getLayoutParams().height = this.mNaturalBarHeight;
        }
        this.mMaxAllowedKeyguardNotifications = resources.getInteger(R.integer.keyguard_max_notification_count);
        if (DEBUG) {
            Log.v("StatusBar", "defineSlots");
        }
    }

    /* access modifiers changed from: protected */
    public void handleVisibleToUserChanged(boolean z) {
        if (z) {
            handleVisibleToUserChangedImpl(z);
            this.mNotificationLogger.startNotificationLogging();
            return;
        }
        this.mNotificationLogger.stopNotificationLogging();
        handleVisibleToUserChangedImpl(z);
    }

    private void handleVisibleToUserChangedImpl(boolean z) {
        if (z) {
            try {
                boolean hasPinnedHeadsUp = this.mHeadsUpManager.hasPinnedHeadsUp();
                int i = 1;
                boolean z2 = !isPanelFullyCollapsed() && (this.mState == 0 || this.mState == 2);
                int size = this.mNotificationData.getActiveNotifications().size();
                if (!hasPinnedHeadsUp || !isPanelFullyCollapsed()) {
                    i = size;
                }
                this.mBarService.onPanelRevealed(z2, i);
            } catch (RemoteException unused) {
            }
        } else {
            this.mBarService.onPanelHidden();
        }
    }

    public void onKeyguardOccludedChanged(boolean z) {
        this.mNotificationPanel.onKeyguardOccludedChanged(z);
    }

    private void logStateToEventlog() {
        boolean isKeyguardShowing = isKeyguardShowing();
        boolean isOccluded = this.mStatusBarKeyguardViewManager.isOccluded();
        boolean isBouncerShowing = this.mStatusBarKeyguardViewManager.isBouncerShowing();
        boolean isMethodSecure = this.mUnlockMethodCache.isMethodSecure();
        boolean canSkipBouncer = this.mUnlockMethodCache.canSkipBouncer();
        int loggingFingerprint = getLoggingFingerprint(this.mState, isKeyguardShowing, isOccluded, isBouncerShowing, isMethodSecure, canSkipBouncer);
        if (loggingFingerprint != this.mLastLoggedStateFingerprint) {
            if (this.mStatusBarStateLog == null) {
                this.mStatusBarStateLog = new LogMaker(0);
            }
            MetricsLoggerCompat.write(this.mContext, this.mMetricsLogger, this.mStatusBarStateLog.setCategory(isBouncerShowing ? 197 : 196).setType(isKeyguardShowing ? 1 : 2).setSubtype(isMethodSecure ? 1 : 0));
            EventLogTags.writeSysuiStatusBarState(this.mState, isKeyguardShowing ? 1 : 0, isOccluded ? 1 : 0, isBouncerShowing ? 1 : 0, isMethodSecure ? 1 : 0, canSkipBouncer ? 1 : 0);
            this.mLastLoggedStateFingerprint = loggingFingerprint;
        }
    }

    /* access modifiers changed from: package-private */
    public void vibrate() {
        ((Vibrator) this.mContext.getSystemService("vibrator")).vibrate(250, VIBRATION_ATTRIBUTES);
    }

    public void collapsePanels() {
        makeExpandedInvisible();
    }

    public void postQSRunnableDismissingKeyguard(final Runnable runnable) {
        this.mHandler.post(new Runnable() {
            public void run() {
                StatusBar.this.executeRunnableDismissingKeyguard(new Runnable() {
                    public void run() {
                        AnonymousClass58 r1 = AnonymousClass58.this;
                        StatusBar.this.mHandler.post(runnable);
                    }
                }, (Runnable) null, false, false, false);
            }
        });
    }

    public void postStartActivityDismissingKeyguard(final PendingIntent pendingIntent) {
        this.mHandler.post(new Runnable() {
            public void run() {
                StatusBar.this.startPendingIntentDismissingKeyguard(pendingIntent);
            }
        });
    }

    public void postStartActivityDismissingKeyguard(final Intent intent, int i) {
        this.mHandler.postDelayed(new Runnable() {
            public void run() {
                StatusBar.this.handleStartActivityDismissingKeyguard(intent, true);
            }
        }, (long) i);
    }

    /* access modifiers changed from: private */
    public void handleStartActivityDismissingKeyguard(Intent intent, boolean z) {
        if (intent != null) {
            startActivityDismissingKeyguard(intent, z, true);
        }
    }

    private static class FastColorDrawable extends Drawable {
        private final int mColor;

        public int getOpacity() {
            return -1;
        }

        public void setAlpha(int i) {
        }

        public void setBounds(int i, int i2, int i3, int i4) {
        }

        public void setBounds(Rect rect) {
        }

        public void setColorFilter(ColorFilter colorFilter) {
        }

        public FastColorDrawable(int i) {
            this.mColor = i | -16777216;
        }

        public void draw(Canvas canvas) {
            canvas.drawColor(this.mColor, PorterDuff.Mode.SRC);
        }
    }

    public void dispatchDemoCommand(String str, Bundle bundle) {
        PhoneStatusBarView phoneStatusBarView;
        View view;
        VolumeComponent volumeComponent;
        int i = 0;
        if (str.equals("enter")) {
            this.mDemoMode = true;
        } else if (str.equals("exit")) {
            this.mDemoMode = false;
            checkBarModes();
        } else if (!this.mDemoMode) {
            dispatchDemoCommand("enter", new Bundle());
        }
        boolean z = str.equals("enter") || str.equals("exit");
        if ((z || str.equals("volume")) && (volumeComponent = this.mVolumeComponent) != null) {
            volumeComponent.dispatchDemoCommand(str, bundle);
        }
        if (z || str.equals("clock")) {
            dispatchDemoCommandToView(str, bundle, R.id.clock);
        }
        if (z || str.equals("battery")) {
            this.mBatteryController.dispatchDemoCommand(str, bundle);
        }
        if (z || str.equals(MiStat.Param.STATUS)) {
            this.mIconController.dispatchDemoCommand(str, bundle);
        }
        if (this.mNetworkController != null && (z || str.equals("network"))) {
            this.mNetworkController.dispatchDemoCommand(str, bundle);
        }
        if (z || str.equals("notifications")) {
            PhoneStatusBarView phoneStatusBarView2 = this.mStatusBarView;
            if (phoneStatusBarView2 == null) {
                view = null;
            } else {
                view = phoneStatusBarView2.findViewById(R.id.notification_icon_area);
            }
            if (view != null) {
                view.setVisibility(this.mDemoMode ? 4 : 0);
            }
        }
        if (str.equals("bars")) {
            String string = bundle.getString("mode");
            if (!"opaque".equals(string)) {
                if ("translucent".equals(string)) {
                    i = 2;
                } else if ("semi-transparent".equals(string)) {
                    i = 1;
                } else if ("transparent".equals(string)) {
                    i = 4;
                } else {
                    i = "warning".equals(string) ? 5 : -1;
                }
            }
            if (i != -1 && (phoneStatusBarView = this.mStatusBarView) != null) {
                phoneStatusBarView.getBarTransitions().transitionTo(i, true);
            }
        }
    }

    private void dispatchDemoCommandToView(String str, Bundle bundle, int i) {
        PhoneStatusBarView phoneStatusBarView = this.mStatusBarView;
        if (phoneStatusBarView != null) {
            View findViewById = phoneStatusBarView.findViewById(i);
            if (findViewById instanceof DemoMode) {
                ((DemoMode) findViewById).dispatchDemoCommand(str, bundle);
            }
        }
    }

    public int getBarState() {
        return this.mState;
    }

    public boolean isPresenterFullyCollapsed() {
        return isPanelFullyCollapsed();
    }

    public boolean isPanelFullyCollapsed() {
        return this.mNotificationPanel.isFullyCollapsed();
    }

    public boolean isQSFullyCollapsed() {
        return this.mNotificationPanel.isQSFullyCollapsed();
    }

    public void showKeyguard() {
        this.mKeyguardRequested = true;
        updateIsKeyguard();
    }

    public boolean hideKeyguard() {
        this.mKeyguardRequested = false;
        return updateIsKeyguard();
    }

    public void setKeyguardTransparent() {
        if (this.mState == 1) {
            this.mStatusBarWindowManager.setKeygaurdTransparent(true);
            IMiuiAodService iMiuiAodService = this.mAodService;
            if (iMiuiAodService != null) {
                try {
                    iMiuiAodService.onKeyguardTransparent();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean updateIsKeyguard() {
        boolean z = true;
        boolean z2 = this.mDozingRequested && !this.mDeviceInteractive;
        if (!this.mKeyguardRequested && !z2) {
            z = false;
        }
        if (!z) {
            return hideKeyguardImpl();
        }
        showKeyguardImpl();
        return false;
    }

    public void showKeyguardImpl() {
        this.mIsKeyguard = true;
        if (this.mLaunchTransitionFadingAway) {
            this.mNotificationPanel.animate().cancel();
            onLaunchTransitionFadingEnded();
        }
        ValueAnimator valueAnimator = this.mFadeKeyguardAimator;
        if (valueAnimator != null) {
            valueAnimator.cancel();
            this.mFadeKeyguardAimator = null;
            this.mNotificationPanel.setAlpha(1.0f);
            this.mNotificationPanel.setScaleX(1.0f);
            this.mNotificationPanel.setScaleY(1.0f);
            finishKeyguardFadingAway();
            this.mFingerprintUnlockController.finishKeyguardFadingAway();
            this.mStatusBarWindowManager.setKeyguardFadingAway(false);
        }
        this.mHandler.removeMessages(b.c);
        UserSwitcherController userSwitcherController = this.mUserSwitcherController;
        if (userSwitcherController == null || !userSwitcherController.useFullscreenUserSwitcher()) {
            setBarState(1);
        } else {
            setBarState(3);
        }
        updateKeyguardState(false, false);
        int i = this.mState;
        if (i == 1) {
            instantExpandNotificationsPanel();
        } else if (i == 3) {
            instantCollapseNotificationPanel();
        }
        this.mLeaveOpenOnKeyguardHide = false;
        ExpandableNotificationRow expandableNotificationRow = this.mDraggedDownRow;
        if (expandableNotificationRow != null) {
            expandableNotificationRow.setUserLocked(false);
            this.mDraggedDownRow.notifyHeightChanged(false);
            this.mDraggedDownRow = null;
        }
        this.mPendingRemoteInputView = null;
        this.mAssistManager.onLockscreenShown();
    }

    /* access modifiers changed from: private */
    public void onLaunchTransitionFadingEnded() {
        this.mNotificationPanel.setAlpha(1.0f);
        this.mNotificationPanel.onAffordanceLaunchEnded();
        releaseGestureWakeLock();
        runLaunchTransitionEndRunnable();
        this.mLaunchTransitionFadingAway = false;
        this.mScrimController.forceHideScrims(false);
        updateMediaMetaData(true, true);
    }

    public boolean isCollapsing() {
        return this.mNotificationPanel.isCollapsing() || this.mActivityLaunchAnimator.isAnimationPending() || this.mActivityLaunchAnimator.isAnimationRunning();
    }

    public void addPostCollapseAction(Runnable runnable) {
        this.mPostCollapseRunnables.add(runnable);
    }

    public void onKeyguardDone() {
        ((KeyguardNotificationHelper) Dependency.get(KeyguardNotificationHelper.class)).clear();
    }

    public boolean isInLaunchTransition() {
        return this.mNotificationPanel.isLaunchTransitionRunning() || this.mNotificationPanel.isLaunchTransitionFinished();
    }

    public void fadeKeyguardAfterLaunchTransition(final Runnable runnable, Runnable runnable2) {
        this.mHandler.removeMessages(b.c);
        this.mLaunchTransitionEndRunnable = runnable2;
        AnonymousClass61 r4 = new Runnable() {
            public void run() {
                StatusBar.this.mLaunchTransitionFadingAway = true;
                Runnable runnable = runnable;
                if (runnable != null) {
                    runnable.run();
                }
                StatusBar.this.mScrimController.forceHideScrims(true);
                StatusBar.this.updateMediaMetaData(false, true);
                StatusBar.this.mNotificationPanel.setAlpha(1.0f);
                StatusBar.this.mStackScroller.setParentNotFullyVisible(true);
                StatusBar.this.mNotificationPanel.animate().alpha(0.0f).setStartDelay(100).setDuration(300).withLayer().withEndAction(new Runnable() {
                    public void run() {
                        StatusBar.this.onLaunchTransitionFadingEnded();
                    }
                });
                StatusBar.this.mCommandQueue.appTransitionStarting(SystemClock.uptimeMillis(), 500, true);
            }
        };
        if (this.mNotificationPanel.isLaunchTransitionRunning()) {
            this.mNotificationPanel.setLaunchTransitionEndRunnable(r4);
        } else {
            r4.run();
        }
    }

    public void fadeKeyguardWhilePulsing() {
        this.mNotificationPanel.notifyStartFading();
        this.mNotificationPanel.animate().alpha(0.0f).setStartDelay(0).setDuration(96).setInterpolator(ScrimController.KEYGUARD_FADE_OUT_INTERPOLATOR).start();
    }

    public void fadeKeyguardWhenUnlockByFingerprint(final Runnable runnable) {
        ValueAnimator valueAnimator = this.mFadeKeyguardAimator;
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
        this.mNotificationPanel.notifyStartFading();
        this.mFadeKeyguardAimator = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
        this.mFadeKeyguardAimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                StatusBar.this.lambda$fadeKeyguardWhenUnlockByFingerprint$1$StatusBar(valueAnimator);
            }
        });
        this.mFadeKeyguardAimator.setDuration(350);
        this.mFadeKeyguardAimator.setInterpolator(new SpringInterpolator(0.9f, 0.8571f));
        this.mFadeKeyguardAimator.addListener(new Animator.AnimatorListener() {
            private boolean cancel = false;

            public void onAnimationRepeat(Animator animator) {
            }

            public void onAnimationStart(Animator animator) {
            }

            public void onAnimationEnd(Animator animator) {
                Runnable runnable;
                ValueAnimator unused = StatusBar.this.mFadeKeyguardAimator = null;
                if (!this.cancel && (runnable = runnable) != null) {
                    runnable.run();
                }
                StatusBar.this.mNotificationPanel.setScaleX(1.0f);
                StatusBar.this.mNotificationPanel.setScaleY(1.0f);
            }

            public void onAnimationCancel(Animator animator) {
                this.cancel = true;
            }
        });
        this.mFadeKeyguardAimator.start();
    }

    public /* synthetic */ void lambda$fadeKeyguardWhenUnlockByFingerprint$1$StatusBar(ValueAnimator valueAnimator) {
        float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
        float f = 1.0f - (0.1f * floatValue);
        this.mNotificationPanel.setScaleX(f);
        this.mNotificationPanel.setScaleY(f);
        this.mNotificationPanel.setAlpha(1.0f - floatValue);
    }

    public void startLaunchTransitionTimeout() {
        this.mHandler.sendEmptyMessageDelayed(b.c, 5000);
    }

    /* access modifiers changed from: private */
    public void onLaunchTransitionTimeout() {
        Log.w("StatusBar", "Launch transition: Timeout!");
        this.mNotificationPanel.onAffordanceLaunchEnded();
        releaseGestureWakeLock();
        this.mNotificationPanel.resetViews();
    }

    private void runLaunchTransitionEndRunnable() {
        Runnable runnable = this.mLaunchTransitionEndRunnable;
        if (runnable != null) {
            this.mLaunchTransitionEndRunnable = null;
            runnable.run();
        }
    }

    public boolean hideKeyguardImpl() {
        View view;
        this.mIsKeyguard = false;
        Trace.beginSection("StatusBar#hideKeyguard");
        boolean z = this.mLeaveOpenOnKeyguardHide;
        setBarState(0);
        if (this.mLeaveOpenOnKeyguardHide) {
            this.mLeaveOpenOnKeyguardHide = false;
            this.mNotificationPanel.animateToFullShade(calculateGoingToFullShadeDelay());
            this.mDismissView.setVisibility(4);
            ExpandableNotificationRow expandableNotificationRow = this.mDraggedDownRow;
            if (expandableNotificationRow != null) {
                expandableNotificationRow.setUserLocked(false);
                this.mDraggedDownRow = null;
            }
            view = this.mPendingRemoteInputView;
            this.mPendingRemoteInputView = null;
        } else {
            if (!this.mNotificationPanel.isCollapsing()) {
                instantCollapseNotificationPanel();
            }
            view = null;
        }
        updateKeyguardState(z, false);
        if (view != null && view.isAttachedToWindow()) {
            view.callOnClick();
        }
        QSPanel qSPanel = this.mQSPanel;
        if (qSPanel != null) {
            qSPanel.refreshAllTiles();
        }
        this.mHandler.removeMessages(b.c);
        releaseGestureWakeLock();
        this.mNotificationPanel.onAffordanceLaunchEnded();
        this.mNotificationPanel.animate().cancel();
        this.mNotificationPanel.setAlpha(1.0f);
        Trace.endSection();
        return z;
    }

    private void releaseGestureWakeLock() {
        if (this.mGestureWakeLock.isHeld()) {
            this.mGestureWakeLock.release();
        }
    }

    public long calculateGoingToFullShadeDelay() {
        return this.mKeyguardFadingAwayDelay + this.mKeyguardFadingAwayDuration;
    }

    public void keyguardGoingAway() {
        this.mKeyguardGoingAway = true;
        this.mKeyguardMonitor.notifyKeyguardGoingAway(true);
        this.mCommandQueue.appTransitionPending(true);
    }

    public void setKeyguardFadingAway(long j, long j2, long j3) {
        long j4 = j2;
        long j5 = j3;
        boolean z = true;
        this.mKeyguardFadingAway = true;
        this.mKeyguardFadingAwayDelay = j4;
        this.mKeyguardFadingAwayDuration = j5;
        this.mWaitingForKeyguardExit = false;
        this.mCommandQueue.appTransitionStarting((j + j5) - 500, 500, true);
        if (j5 <= 0) {
            z = false;
        }
        recomputeDisableFlags(z);
        this.mCommandQueue.appTransitionStarting(j - 500, 500, true);
        this.mKeyguardMonitor.notifyKeyguardFadingAway(j4, j5);
    }

    public boolean isKeyguardFadingAway() {
        return this.mKeyguardFadingAway;
    }

    public void finishKeyguardFadingAway() {
        this.mKeyguardFadingAway = false;
        this.mKeyguardGoingAway = false;
        this.mKeyguardMonitor.notifyKeyguardDoneFading();
    }

    public void stopWaitingForKeyguardExit() {
        this.mWaitingForKeyguardExit = false;
    }

    /* access modifiers changed from: private */
    public void updatePublicMode() {
        boolean z;
        boolean isKeyguardShowing = isKeyguardShowing();
        boolean z2 = isKeyguardShowing && this.mStatusBarKeyguardViewManager.isSecure(this.mCurrentUserId);
        Log.d("StatusBar", "updatePublicMode() showingKeyguard=" + isKeyguardShowing + ",devicePublic=" + z2);
        for (int size = this.mCurrentProfiles.size() - 1; size >= 0; size--) {
            int i = this.mCurrentProfiles.valueAt(size).id;
            if (z2 || i == this.mCurrentUserId || !this.mLockPatternUtils.isSeparateProfileChallengeEnabled(i) || !this.mStatusBarKeyguardViewManager.isSecure(i)) {
                z = z2;
            } else {
                z = this.mKeyguardManager.isDeviceLocked(i);
                Log.d("StatusBar", "updatePublicMode() isProfilePublic=" + z);
            }
            setLockscreenPublicMode(z, i);
        }
    }

    /* access modifiers changed from: protected */
    public void updateKeyguardState(boolean z, boolean z2) {
        Trace.beginSection("StatusBar#updateKeyguardState");
        boolean z3 = true;
        if (this.mState == 1) {
            this.mKeyguardIndicationController.setVisible(true);
            this.mNotificationPanel.resetViews();
            KeyguardUserSwitcher keyguardUserSwitcher = this.mKeyguardUserSwitcher;
            if (keyguardUserSwitcher != null) {
                keyguardUserSwitcher.setKeyguard(true, z2);
            }
            PhoneStatusBarView phoneStatusBarView = this.mStatusBarView;
            if (phoneStatusBarView != null) {
                phoneStatusBarView.removePendingHideExpandedRunnables();
            }
        } else {
            this.mKeyguardIndicationController.setVisible(false);
            KeyguardUserSwitcher keyguardUserSwitcher2 = this.mKeyguardUserSwitcher;
            if (keyguardUserSwitcher2 != null) {
                keyguardUserSwitcher2.setKeyguard(false, z || this.mState == 2 || z2);
            }
        }
        int i = this.mState;
        if (i == 1 || i == 2) {
            this.mScrimController.setKeyguardShowing(true);
        } else {
            this.mScrimController.setKeyguardShowing(false);
        }
        this.mNotificationPanel.setBarState(this.mState, this.mKeyguardFadingAway, z);
        updateDozingState();
        updatePublicMode();
        updateStackScrollerState(z, z2);
        updateNotifications();
        checkBarModes();
        if (this.mState == 1) {
            z3 = false;
        }
        updateMediaMetaData(false, z3);
        this.mKeyguardMonitor.notifyKeyguardState(isKeyguardShowing(), this.mUnlockMethodCache.isMethodSecure(), this.mStatusBarKeyguardViewManager.isOccluded());
        Trace.endSection();
    }

    private void updateDozingState() {
        Trace.beginSection("StatusBar#updateDozingState");
        boolean z = !this.mDozing && this.mAnimateWakeup;
        this.mNotificationPanel.setDozing(this.mDozing, z);
        this.mStackScroller.setDark(false, z, this.mWakeUpTouchLocation);
        this.mScrimController.setDozing(this.mDozing);
        this.mKeyguardIndicationController.setDozing(this.mDozing);
        this.mNotificationPanel.setDark(false, z);
        updateQsExpansionEnabled();
        this.mDozeScrimController.setDozing(this.mDozing, z);
        updateRowStates();
        Trace.endSection();
    }

    public void updateStackScrollerState(boolean z, boolean z2) {
        if (this.mStackScroller != null) {
            boolean z3 = true;
            boolean z4 = this.mState == 1;
            boolean isAnyProfilePublicMode = isAnyProfilePublicMode();
            Log.d("StatusBar", "updateStackScrollerState() publicMode=" + isAnyProfilePublicMode + ",isKeyguardShowing=" + isKeyguardShowing());
            NotificationStackScrollLayout notificationStackScrollLayout = this.mStackScroller;
            if (!NotificationUtil.hideNotificationsForFaceUnlock(this.mContext) && !isAnyProfilePublicMode) {
                z3 = false;
            }
            notificationStackScrollLayout.setHideSensitive(z3, z);
            this.mStackScroller.setDimmed(z4, z2);
            this.mStackScroller.setExpandingEnabled(NotificationUtil.isExpandingEnabled(z4));
            ActivatableNotificationView activatedChild = this.mStackScroller.getActivatedChild();
            this.mStackScroller.setActivatedChild((ActivatableNotificationView) null);
            if (activatedChild != null) {
                activatedChild.makeInactive(false);
            }
        }
    }

    public void userActivity() {
        if (this.mState == 1) {
            this.mKeyguardViewMediatorCallback.userActivity();
        }
    }

    public boolean interceptMediaKey(KeyEvent keyEvent) {
        if (this.mState != 1 || !this.mStatusBarKeyguardViewManager.interceptMediaKey(keyEvent)) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean shouldUnlockOnMenuPressed() {
        return this.mDeviceInteractive && this.mState != 0 && this.mStatusBarKeyguardViewManager.shouldDismissOnMenuPressed();
    }

    public boolean onMenuPressed() {
        if (!shouldUnlockOnMenuPressed()) {
            return false;
        }
        animateCollapsePanels(2, true);
        return true;
    }

    public void endAffordanceLaunch() {
        releaseGestureWakeLock();
        this.mNotificationPanel.onAffordanceLaunchEnded();
    }

    public void closeQs() {
        if (this.mNotificationPanel != null) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    StatusBar.this.mNotificationPanel.animateCloseQs();
                }
            });
        }
    }

    public boolean onBackPressed() {
        if (this.mStatusBarKeyguardViewManager.onBackPressed()) {
            return true;
        }
        if (this.mNotificationPanel.isQsExpanded()) {
            if (this.mNotificationPanel.isQsDetailShowing()) {
                this.mNotificationPanel.closeQsDetail();
            } else {
                this.mNotificationPanel.animateCloseQs();
            }
            return true;
        } else if (!this.mNotificationPanel.isInCenterScreen()) {
            this.mNotificationPanel.resetViews();
            return true;
        } else {
            int i = this.mState;
            if (i == 1 || i == 2) {
                KeyguardUserSwitcher keyguardUserSwitcher = this.mKeyguardUserSwitcher;
                if (keyguardUserSwitcher == null || !keyguardUserSwitcher.hideIfNotSimple(true)) {
                    return false;
                }
                return true;
            }
            ((NotificationStat) Dependency.get(NotificationStat.class)).onBackPressed();
            if (this.mNotificationPanel.canPanelBeCollapsed()) {
                animateCollapsePanels();
            } else {
                this.mBubbleController.performBackPressIfNeeded();
            }
            animateCollapsePanels();
            return true;
        }
    }

    public boolean onSpacePressed() {
        if (!this.mDeviceInteractive || this.mState == 0) {
            return false;
        }
        animateCollapsePanels(2, true);
        return true;
    }

    public void showBouncerIfKeyguard() {
        int i = this.mState;
        if (i == 1 || i == 2) {
            showBouncer();
        }
    }

    /* access modifiers changed from: protected */
    public void showBouncer() {
        this.mWaitingForKeyguardExit = isKeyguardShowing();
        this.mStatusBarKeyguardViewManager.dismiss();
    }

    public void instantExpandNotificationsPanel() {
        makeExpandedVisible(true);
        this.mNotificationPanel.expand(false);
    }

    private void instantCollapseNotificationPanel() {
        this.mNotificationPanel.instantCollapse();
    }

    public void onActivated(ActivatableNotificationView activatableNotificationView) {
        this.mLockscreenGestureLogger.write(this.mContext, 192, 0, 0);
        this.mKeyguardIndicationController.showTransientIndication((int) R.string.notification_tap_again);
        ActivatableNotificationView activatedChild = this.mStackScroller.getActivatedChild();
        if (activatedChild != null) {
            activatedChild.makeInactive(true);
        }
        this.mStackScroller.setActivatedChild(activatableNotificationView);
    }

    public void setBarState(int i) {
        if (!(!DEBUG || i == 0 || i == 1)) {
            Slog.w("StatusBar", "setBarState: illegal state, state = " + i, new Throwable());
        }
        int i2 = this.mState;
        if (i != i2 && i2 == 2) {
            ((NotificationStat) Dependency.get(NotificationStat.class)).onPanelCollapsed(false, false, this.mStackScroller.getNotGoneNotifications());
        }
        if (i != this.mState && this.mVisible && (i == 2 || (i == 0 && isGoingToNotificationShade()))) {
            clearNotificationEffects();
        }
        if (i == 1) {
            removeRemoteInputEntriesKeptUntilCollapsed();
            maybeEscalateHeadsUp();
            HeadsUpAnimatedStubView headsUpAnimatedStubView = this.mHeadsUpAnimatedStub;
            if (headsUpAnimatedStubView != null) {
                headsUpAnimatedStubView.setAnimationRunning(false);
            }
        } else if (this.mIsInDriveMode) {
            this.mMiuiStatusBarPrompt.setState("legacy_drive", (MiuiStatusBarPromptController.State) null, 1);
        }
        this.mState = i;
        this.mStatusBarStateController.setState(i);
        updateDriveMode();
        this.mGroupManager.setStatusBarState(i);
        this.mHeadsUpManager.setStatusBarState(i);
        this.mFalsingManager.setStatusBarState(i);
        this.mStatusBarWindowManager.setStatusBarState(i);
        this.mStackScroller.setStatusBarState(i);
        updateReportRejectedTouchVisibility();
        updateDozing();
        this.mNotificationShelf.setStatusBarState(i);
        ((BubbleController) Dependency.get(BubbleController.class)).setStatusBarState(this.mState);
    }

    public void onActivationReset(ActivatableNotificationView activatableNotificationView) {
        if (activatableNotificationView == this.mStackScroller.getActivatedChild()) {
            this.mKeyguardIndicationController.hideTransientIndication();
            this.mStackScroller.setActivatedChild((ActivatableNotificationView) null);
        }
    }

    public void onTrackingStarted() {
        runPostCollapseRunnables();
    }

    public void onExpandingFinished() {
        if (!isKeyguardShowing() && !this.mNotificationPanel.isFullyCollapsed()) {
            ((NotificationStat) Dependency.get(NotificationStat.class)).onPanelExpanded(false, true, this.mStackScroller.getNotGoneNotifications());
        }
    }

    public void onClosingFinished() {
        if (!isKeyguardShowing()) {
            ((NotificationStat) Dependency.get(NotificationStat.class)).onPanelCollapsed(false, true, this.mStackScroller.getNotGoneNotifications());
        }
        runPostCollapseRunnables();
        if (!isPanelFullyCollapsed()) {
            this.mStatusBarWindowManager.setStatusBarFocusable(true);
        }
    }

    public void onTrackingStopped(boolean z) {
        int i = this.mState;
        if ((i == 1 || i == 2) && !z && !this.mUnlockMethodCache.canSkipBouncer()) {
            AnalyticsHelper.getInstance(this.mContext).recordKeyguardAction("action_vertical_sweep");
            if (MiuiKeyguardUtils.isGxzwSensor()) {
                MiuiGxzwManager.getInstance().setDimissFodInBouncer(true);
            }
            this.mFaceUnlockManager.startFaceUnlock(1);
            showBouncerIfKeyguard();
        }
    }

    /* access modifiers changed from: protected */
    public int getMaxKeyguardNotifications(boolean z) {
        if (!z) {
            return this.mMaxKeyguardNotifications;
        }
        this.mKeyguardNotifications = this.mNotificationPanel.computeMaxKeyguardNotifications(this.mMaxAllowedKeyguardNotifications);
        this.mMaxKeyguardNotifications = Math.max(1, this.mKeyguardNotifications);
        return this.mMaxKeyguardNotifications;
    }

    public int getMaxKeyguardNotifications() {
        return getMaxKeyguardNotifications(false);
    }

    public int getKeyguardNotifications() {
        return this.mKeyguardNotifications;
    }

    public NavigationBarView getNavigationBarView() {
        return this.mNavigationBarView;
    }

    public boolean onDraggedDown(View view, int i) {
        if (this.mState != 1 || !hasActiveNotifications() || (isDozing() && !isPulsing())) {
            return false;
        }
        this.mLockscreenGestureLogger.write(this.mContext, 187, (int) (((float) i) / this.mDisplayMetrics.density), 0);
        goToLockedShade(view);
        if (view instanceof ExpandableNotificationRow) {
            ((ExpandableNotificationRow) view).onExpandedByGesture(true);
        }
        return true;
    }

    public void onDragDownReset() {
        this.mStackScroller.setDimmed(true, true);
        this.mStackScroller.resetScrollPosition();
        this.mStackScroller.resetCheckSnoozeLeavebehind();
    }

    public void onCrossedThreshold(boolean z) {
        this.mStackScroller.setDimmed(!z, true);
    }

    public void onTouchSlopExceeded() {
        this.mStackScroller.removeLongPressCallback();
        this.mStackScroller.checkSnoozeLeavebehind();
    }

    public void setEmptyDragAmount(float f) {
        this.mNotificationPanel.setEmptyDragAmount(f);
    }

    public void goToLockedShade(View view) {
        ExpandableNotificationRow expandableNotificationRow;
        if ((this.mDisabled2 & 4) == 0 && MiuiStatusBarManager.isExpandableUnderKeyguard(this.mContext)) {
            int i = this.mCurrentUserId;
            if (view instanceof ExpandableNotificationRow) {
                expandableNotificationRow = (ExpandableNotificationRow) view;
                expandableNotificationRow.setUserExpanded(true, true);
                expandableNotificationRow.setGroupExpansionChanging(true);
                if (expandableNotificationRow.getStatusBarNotification() != null) {
                    i = expandableNotificationRow.getStatusBarNotification().getUserId();
                }
            } else {
                expandableNotificationRow = null;
            }
            boolean z = !userAllowsPrivateNotificationsInPublic(this.mCurrentUserId) || !this.mShowLockscreenNotifications || this.mFalsingManager.shouldEnforceBouncer();
            if (!isLockscreenPublicMode(i) || !z) {
                this.mNotificationPanel.animateToFullShade(0);
                this.mDismissView.setVisibility(4);
                setBarState(2);
                updateKeyguardState(false, false);
                return;
            }
            this.mLeaveOpenOnKeyguardHide = true;
            showBouncerIfKeyguard();
            this.mDraggedDownRow = expandableNotificationRow;
            this.mPendingRemoteInputView = null;
        }
    }

    public void onLockedNotificationImportanceChange(ActivityStarter.OnDismissAction onDismissAction) {
        this.mLeaveOpenOnKeyguardHide = true;
        dismissKeyguardThenExecute(onDismissAction, true);
    }

    /* access modifiers changed from: protected */
    public void onLockedRemoteInput(ExpandableNotificationRow expandableNotificationRow, View view) {
        this.mLeaveOpenOnKeyguardHide = true;
        showBouncer();
        this.mPendingRemoteInputView = view;
    }

    /* access modifiers changed from: protected */
    public void onMakeExpandedVisibleForRemoteInput(ExpandableNotificationRow expandableNotificationRow, final View view) {
        if (isKeyguardShowing()) {
            onLockedRemoteInput(expandableNotificationRow, view);
            return;
        }
        expandableNotificationRow.setUserExpanded(true);
        expandableNotificationRow.getPrivateLayout().setOnExpandedVisibleListener(new Runnable() {
            public void run() {
                view.performClick();
            }
        });
    }

    public boolean startWorkChallengeIfNecessary(int i, IntentSender intentSender, String str) {
        this.mPendingWorkRemoteInputView = null;
        Intent createConfirmDeviceCredentialIntent = KeyguardManagerCompat.createConfirmDeviceCredentialIntent(this.mKeyguardManager, (CharSequence) null, (CharSequence) null, i);
        if (createConfirmDeviceCredentialIntent == null) {
            return false;
        }
        Intent intent = new Intent("com.android.systemui.statusbar.work_challenge_unlocked_notification_action");
        intent.putExtra("android.intent.extra.INTENT", intentSender);
        intent.putExtra("android.intent.extra.INDEX", str);
        intent.setPackage(this.mContext.getPackageName());
        createConfirmDeviceCredentialIntent.putExtra("android.intent.extra.INTENT", PendingIntent.getBroadcast(this.mContext, 0, intent, 1409286144).getIntentSender());
        try {
            ActivityManagerCompat.startConfirmDeviceCredentialIntent(createConfirmDeviceCredentialIntent, (Bundle) null);
            return true;
        } catch (RemoteException unused) {
            return true;
        }
    }

    /* access modifiers changed from: protected */
    public void onLockedWorkRemoteInput(int i, ExpandableNotificationRow expandableNotificationRow, View view) {
        animateCollapsePanels();
        startWorkChallengeIfNecessary(i, (IntentSender) null, (String) null);
        this.mPendingWorkRemoteInputView = view;
    }

    /* access modifiers changed from: private */
    public boolean isAnyProfilePublicMode() {
        for (int size = this.mCurrentProfiles.size() - 1; size >= 0; size--) {
            if (isLockscreenPublicMode(this.mCurrentProfiles.valueAt(size).id)) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public void onWorkChallengeChanged() {
        updatePublicMode();
        updateNotifications();
        if (this.mPendingWorkRemoteInputView != null && !isAnyProfilePublicMode()) {
            final AnonymousClass65 r0 = new Runnable() {
                public void run() {
                    View access$6500 = StatusBar.this.mPendingWorkRemoteInputView;
                    if (access$6500 != null) {
                        ViewParent parent = access$6500.getParent();
                        while (!(parent instanceof ExpandableNotificationRow)) {
                            if (parent != null) {
                                parent = parent.getParent();
                            } else {
                                return;
                            }
                        }
                        final ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) parent;
                        ViewParent parent2 = expandableNotificationRow.getParent();
                        if (parent2 instanceof NotificationStackScrollLayout) {
                            final NotificationStackScrollLayout notificationStackScrollLayout = (NotificationStackScrollLayout) parent2;
                            expandableNotificationRow.makeActionsVisibile();
                            expandableNotificationRow.post(new Runnable() {
                                public void run() {
                                    AnonymousClass1 r0 = new Runnable() {
                                        public void run() {
                                            StatusBar.this.mPendingWorkRemoteInputView.callOnClick();
                                            View unused = StatusBar.this.mPendingWorkRemoteInputView = null;
                                            notificationStackScrollLayout.setFinishScrollingCallback((Runnable) null);
                                        }
                                    };
                                    if (notificationStackScrollLayout.scrollTo(expandableNotificationRow)) {
                                        notificationStackScrollLayout.setFinishScrollingCallback(r0);
                                    } else {
                                        r0.run();
                                    }
                                }
                            });
                        }
                    }
                }
            };
            this.mNotificationPanel.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                public void onGlobalLayout() {
                    if (StatusBar.this.mNotificationPanel.mStatusBar.getStatusBarWindow().getHeight() != StatusBar.this.mNotificationPanel.mStatusBar.getStatusBarHeight()) {
                        StatusBar.this.mNotificationPanel.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        StatusBar.this.mNotificationPanel.post(r0);
                    }
                }
            });
            instantExpandNotificationsPanel();
        }
    }

    public void onExpandClicked(NotificationData.Entry entry, boolean z) {
        this.mHeadsUpManager.setExpanded(entry, z);
    }

    public void goToKeyguard() {
        if (this.mState == 2) {
            this.mStackScroller.onGoToKeyguard();
            setBarState(1);
            updateKeyguardState(false, true);
        }
    }

    public long getKeyguardFadingAwayDelay() {
        return this.mKeyguardFadingAwayDelay;
    }

    public long getKeyguardFadingAwayDuration() {
        return this.mKeyguardFadingAwayDuration;
    }

    public void setBouncerShowing(boolean z) {
        this.mBouncerShowing = z;
        PhoneStatusBarView phoneStatusBarView = this.mStatusBarView;
        if (phoneStatusBarView != null) {
            phoneStatusBarView.setBouncerShowing(z);
        }
        recomputeDisableFlags(true);
    }

    public void onStartedGoingToSleep() {
        this.mStartedGoingToSleep = true;
        this.mNotificationPanel.onStartedGoingToSleep();
    }

    public void onFinishedGoingToSleep() {
        this.mNotificationPanel.onAffordanceLaunchEnded();
        releaseGestureWakeLock();
        this.mLaunchCameraOnScreenTurningOn = false;
        this.mStartedGoingToSleep = false;
        this.mDeviceInteractive = false;
        this.mWakeUpComingFromTouch = false;
        this.mWakeUpTouchLocation = null;
        this.mStackScroller.setAnimationsEnabled(false);
        this.mVisualStabilityManager.setScreenOn(false);
        updateVisibleToUser();
        this.mNotificationPanel.setTouchDisabled(true);
        this.mStatusBarWindow.cancelCurrentTouch();
        if (this.mLaunchCameraOnFinishedGoingToSleep) {
            this.mLaunchCameraOnFinishedGoingToSleep = false;
            this.mHandler.post(new Runnable() {
                public void run() {
                    StatusBar statusBar = StatusBar.this;
                    statusBar.onCameraLaunchGestureDetected(statusBar.mLastCameraLaunchSource);
                }
            });
        }
        updateIsKeyguard();
    }

    public void onStartedWakingUp() {
        this.mDeviceInteractive = true;
        this.mStackScroller.setAnimationsEnabled(true);
        this.mVisualStabilityManager.setScreenOn(true);
        this.mNotificationPanel.setTouchDisabled(false);
        this.mNotificationPanel.onStartedWakingUp();
        updateVisibleToUser();
        updateIsKeyguard();
    }

    public void onScreenTurningOn() {
        this.mScreenTurningOn = true;
        this.mFalsingManager.onScreenTurningOn();
        if (this.mLaunchCameraOnScreenTurningOn) {
            this.mNotificationPanel.launchCamera(false, this.mLastCameraLaunchSource);
            this.mLaunchCameraOnScreenTurningOn = false;
        }
    }

    private void vibrateForCameraGesture() {
        this.mVibrator.vibrate(this.mCameraLaunchGestureVibePattern, -1);
    }

    public void onScreenTurnedOn() {
        this.mScreenTurningOn = false;
        this.mDozeScrimController.onScreenTurnedOn();
    }

    public void showScreenPinningRequest(int i) {
        if (!this.mKeyguardMonitor.isShowing()) {
            showScreenPinningRequest(i, true);
        }
    }

    public void showScreenPinningRequest(int i, boolean z) {
        this.mScreenPinningRequest.showPrompt(i, z);
    }

    public boolean hasActiveNotifications() {
        return !this.mNotificationData.getActiveNotifications().isEmpty();
    }

    public void wakeUpIfDozing(long j, View view, String str) {
        if (this.mDozing) {
            ((PowerManager) this.mContext.getSystemService("power")).wakeUp(j, "com.android.systemui:" + str);
            this.mWakeUpComingFromTouch = true;
            view.getLocationInWindow(this.mTmpInt2);
            this.mWakeUpTouchLocation = new PointF((float) (this.mTmpInt2[0] + (view.getWidth() / 2)), (float) (this.mTmpInt2[1] + (view.getHeight() / 2)));
            this.mStatusBarKeyguardViewManager.notifyDeviceWakeUpRequested();
            this.mFalsingManager.onScreenOnFromTouch();
        }
    }

    public void appTransitionCancelled() {
        RecentsEventBus.getDefault().send(new AppTransitionFinishedEvent());
    }

    public void appTransitionFinished() {
        RecentsEventBus.getDefault().send(new AppTransitionFinishedEvent());
    }

    public void onCameraLaunchGestureDetected(int i) {
        this.mLastCameraLaunchSource = i;
        if (this.mStartedGoingToSleep) {
            this.mLaunchCameraOnFinishedGoingToSleep = true;
            return;
        }
        if (this.mNotificationPanel.canCameraGestureBeLaunched(isKeyguardShowing() && this.mExpandedVisible)) {
            if (!this.mDeviceInteractive) {
                ((PowerManager) this.mContext.getSystemService(PowerManager.class)).wakeUp(SystemClock.uptimeMillis(), "com.android.systemui:CAMERA_GESTURE");
                this.mStatusBarKeyguardViewManager.notifyDeviceWakeUpRequested();
            }
            vibrateForCameraGesture();
            if (!isKeyguardShowing()) {
                startActivity(KeyguardBottomAreaView.INSECURE_CAMERA_INTENT, true);
                return;
            }
            if (!this.mDeviceInteractive) {
                this.mScrimController.dontAnimateBouncerChangesUntilNextFrame();
                this.mGestureWakeLock.acquire(6000);
            }
            if (this.mScreenTurningOn || this.mStatusBarKeyguardViewManager.isScreenTurnedOn()) {
                this.mNotificationPanel.launchCamera(this.mDeviceInteractive, i);
            } else {
                this.mLaunchCameraOnScreenTurningOn = true;
            }
        }
    }

    public void notifyFpAuthModeChanged() {
        updateDozing();
    }

    /* access modifiers changed from: private */
    public void updateDozing() {
        Trace.beginSection("StatusBar#updateDozing");
        boolean z = false;
        this.mDozing = this.mDozingRequested || this.mFingerprintUnlockController.getMode() == 2;
        if (this.mFingerprintUnlockController.getMode() == 1) {
            this.mDozing = false;
        }
        this.mStatusBarStateController.setIsDozing(this.mDozing);
        Log.i("StatusBar", "updateDozing: mDozing:" + this.mDozing);
        KeyguardViewMediator keyguardViewMediator = (KeyguardViewMediator) getComponent(KeyguardViewMediator.class);
        if (this.mDozing && MiuiKeyguardUtils.isAodEnable(this.mContext) && Util.isMiuiOptimizationDisabled()) {
            z = true;
        }
        keyguardViewMediator.setAodShowing(z);
        this.mStatusBarWindowManager.setDozing(this.mDozing);
        this.mStatusBarKeyguardViewManager.setDozing(this.mDozing);
        updateDozingState();
        Trace.endSection();
    }

    public int indexOfEntry(NotificationData.Entry entry) {
        return this.mNotificationData.indexOf(entry);
    }

    public boolean isKeyguardShowing() {
        return this.mStatusBarKeyguardViewManager.isShowing();
    }

    public void suppressAmbientDisplay(boolean z) {
        Log.w("StatusBar", "suppressAmbientDisplay: " + z);
        DozeServiceHost dozeServiceHost = this.mDozeServiceHost;
        if (dozeServiceHost != null) {
            dozeServiceHost.sendCommand("suppressAmbientDisplay", z ? 1 : 0, (Bundle) null);
        }
    }

    private class AodCallback extends IMiuiAodCallback.Stub {
        public void onDozeStateChanged(int i) {
        }

        private AodCallback() {
        }

        public void setAnimateWakeup(boolean z) {
            boolean unused = StatusBar.this.mAnimateWakeup = z;
        }

        public void onDozingRequested(boolean z) {
            Log.i("StatusBar", "onDozingRequested: " + z);
            boolean unused = StatusBar.this.mDozingRequested = z;
            StatusBar statusBar = StatusBar.this;
            statusBar.mHandler.removeCallbacks(statusBar.mDozingChanged);
            StatusBar statusBar2 = StatusBar.this;
            statusBar2.mHandler.postAtFrontOfQueue(statusBar2.mDozingChanged);
        }

        public void onExtendPulse() {
            StatusBar.this.mDozeScrimController.extendPulse();
        }

        public void notifyKeycodeGoto() {
            StatusBar statusBar = StatusBar.this;
            statusBar.mHandler.postAtFrontOfQueue(statusBar.mNotifyKeycodeGoto);
        }
    }

    private void registerDeviceProvsionedObserverForAodIfNeeded() {
        if (Constants.SUPPORT_AOD) {
            try {
                this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("device_provisioned"), false, new ContentObserver(new Handler()) {
                    public void onChange(boolean z) {
                        super.onChange(z);
                        ContentResolver contentResolver = StatusBar.this.mContext.getContentResolver();
                        boolean z2 = false;
                        if (Settings.Global.getInt(contentResolver, "device_provisioned", 0) != 0) {
                            z2 = true;
                        }
                        if (z2) {
                            Settings.Global.putInt(contentResolver, "new_device_after_support_notification_animation", 1);
                        }
                        StatusBar statusBar = StatusBar.this;
                        boolean unused = statusBar.mWakeupForNotification = MiuiKeyguardUtils.isWakeupForNotification(statusBar.mContext.getContentResolver());
                    }
                });
            } catch (Exception e) {
                Log.d("StatusBar", "registerContentObserver DEVICE_PROVISIONED failed", e);
            }
        }
    }

    /* access modifiers changed from: private */
    public void startAndBindAodService() {
        if (Constants.SUPPORT_AOD) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    Intent intent = new Intent("com.miui.aod.MiuiAodService");
                    intent.setPackage("com.miui.aod");
                    StatusBar statusBar = StatusBar.this;
                    boolean unused = statusBar.mAodServiceBinded = statusBar.mContext.bindServiceAsUser(intent, statusBar.serviceConnection, 1, UserHandle.CURRENT);
                    Log.d("StatusBar", "is service connected: " + StatusBar.this.mAodServiceBinded);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    public void disconnectAodService() {
        IMiuiAodService iMiuiAodService = this.mAodService;
        if (iMiuiAodService != null) {
            try {
                iMiuiAodService.unregisterCallback();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            this.mAodService = null;
            this.mContext.unbindService(this.serviceConnection);
        }
        AodCallback aodCallback = this.mAodCallback;
        if (aodCallback != null) {
            aodCallback.onDozingRequested(false);
        }
    }

    /* access modifiers changed from: private */
    public void checkAodService() {
        if (this.mAodService == null) {
            startAndBindAodService();
        }
    }

    public final class DozeServiceHost implements AodHost {
        public DozeServiceHost() {
        }

        public void fireAodState(boolean z) {
            StatusBar.this.checkAodService();
            if (StatusBar.this.mAodService != null) {
                try {
                    StatusBar.this.mAodService.fireAodState(z);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        public void onGxzwIconChanged(boolean z) {
            Shell.setRuntimeSharedValue("KEYGUARD_GXZW_ICON_SHOWN", z ? 0 : 1);
            StatusBar.this.checkAodService();
            if (StatusBar.this.mAodService != null) {
                try {
                    StatusBar.this.mAodService.onGxzwIconChanged(z);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        public void fireFingerprintPressed(boolean z) {
            StatusBar.this.checkAodService();
            if (StatusBar.this.mAodService != null) {
                try {
                    StatusBar.this.mAodService.fireFingerprintPressed(z);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        public void stopDozing() {
            StatusBar.this.checkAodService();
            if (StatusBar.this.mAodService != null) {
                try {
                    StatusBar.this.mAodService.stopDozing();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        public void onSimPinSecureChanged(boolean z) {
            StatusBar.this.checkAodService();
            if (StatusBar.this.mAodService != null) {
                try {
                    StatusBar.this.mAodService.onSimPinSecureChanged(z);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        public void sendCommand(String str, int i, Bundle bundle) {
            StatusBar.this.checkAodService();
            if (StatusBar.this.mAodService != null) {
                try {
                    StatusBar.this.mAodService.sendCommand(str, i, bundle);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void updateDozeAfterScreenOff() {
        if (Build.VERSION.SDK_INT >= 29) {
            if (!this.mSupportsAmbientMode || !this.mAodEnable || !this.mAodUsingSuperWallpaperStyle) {
                this.mPowerManager.setDozeAfterScreenOff(true);
            } else {
                this.mPowerManager.setDozeAfterScreenOff(false);
            }
        }
    }

    public boolean isDeviceProvisioned() {
        return this.mDeviceProvisionedController.isDeviceProvisioned();
    }

    public boolean isSuperSaveModeOn() {
        return this.mSuperSaveModeOn;
    }

    public void onLaunchAnimationCancelled() {
        if (!isCollapsing()) {
            onClosingFinished();
        }
    }

    public void onExpandAnimationFinished(boolean z) {
        if (!isCollapsing()) {
            onClosingFinished();
        }
        if (z) {
            instantCollapseNotificationPanel();
        }
    }

    public void onExpandAnimationTimedOut() {
        MiuiActivityLaunchAnimator miuiActivityLaunchAnimator;
        if (!isPresenterFullyCollapsed() || isCollapsing() || (miuiActivityLaunchAnimator = this.mActivityLaunchAnimator) == null || miuiActivityLaunchAnimator.isLaunchForActivity()) {
            collapsePanel();
        } else {
            onClosingFinished();
        }
    }

    public boolean areLaunchAnimationsEnabled() {
        return this.mState == 0;
    }

    public boolean isDeviceInVrMode() {
        return this.mVrMode;
    }

    /* access modifiers changed from: private */
    public void processScreenBtnDisableNotification() {
        NotificationManager notificationManager = (NotificationManager) this.mContext.getSystemService("notification");
        if (!this.mScreenButtonDisabled || this.mIsFsgMode) {
            notificationManager.cancelAsUser((String) null, R.drawable.screen_button_notification_icon, new UserHandle(this.mCurrentUserId));
            return;
        }
        Notification build = NotificationCompat.newBuilder(this.mContext, NotificationChannels.SCREENBUTTON).setWhen(System.currentTimeMillis()).setShowWhen(true).setOngoing(true).setSmallIcon(R.drawable.screen_button_notification_icon).setContentTitle(this.mContext.getString(R.string.screen_button_notification_title)).setContentText(this.mContext.getString(286130250)).setContentIntent(PendingIntent.getBroadcast(this.mContext, 0, new Intent("com.miui.app.ExtraStatusBarManager.TRIGGER_TOGGLE_SCREEN_BUTTONS"), 0)).build();
        MiuiNotificationCompat.setTargetPkg(build, "android");
        notificationManager.notifyAsUser((String) null, R.drawable.screen_button_notification_icon, build, new UserHandle(this.mCurrentUserId));
    }

    /* access modifiers changed from: private */
    public void onFsGestureStateChange() {
        updateCompositionSampling();
        changeNavBarViewState();
        updateOverlayManager();
    }

    private void updateCompositionSampling() {
        if (this.mNavigationBarView != null) {
            if (!this.mIsFsgMode || isHideGestureLine()) {
                this.mNavigationBarView.stopCompositionSampling();
            } else {
                this.mNavigationBarView.startCompositionSampling();
            }
        }
    }

    private void updateOverlayManager() {
        boolean z = !isHideGestureLine() && this.mIsFsgMode;
        if (z != isOverlay(this.mCurrentUserId)) {
            try {
                this.mOverlayManager.setEnabled("com.android.systemui.gesture.line.overlay", z, this.mCurrentUserId);
            } catch (Exception e) {
                Log.w("StatusBar", "Can't apply overlay for user " + this.mCurrentUserId, e);
            }
        }
        if (this.mCurrentUserId != 0 && z != isOverlay(0)) {
            try {
                this.mOverlayManager.setEnabled("com.android.systemui.gesture.line.overlay", z, 0);
            } catch (Exception e2) {
                Log.w("StatusBar", "Can't apply overlay for user owner", e2);
            }
        }
    }

    private boolean isOverlay(int i) {
        OverlayManagerWrapper.OverlayInfo overlayInfo;
        try {
            overlayInfo = this.mOverlayManager.getOverlayInfo("com.android.systemui.gesture.line.overlay", i);
        } catch (Exception e) {
            Log.w("StatusBar", "Can't get overlay info for user " + i, e);
            overlayInfo = null;
        }
        return overlayInfo != null && overlayInfo.isEnabled();
    }

    public boolean isAodUsingSuperWallpaper() {
        return this.mAodEnable && this.mAodUsingSuperWallpaperStyle;
    }

    /* access modifiers changed from: private */
    public boolean handleNotification(StatusBarNotification statusBarNotification, NotificationListenerService.RankingMap rankingMap, boolean z) {
        boolean hasSmallIcon = NotificationUtil.hasSmallIcon(statusBarNotification.getNotification());
        boolean z2 = StatusBarNotificationCompat.isAutoGroupSummary(statusBarNotification) && ((NotificationSettingsManager) Dependency.get(NotificationSettingsManager.class)).disableAutoGroupSummary(statusBarNotification.getPackageName());
        if (!hasSmallIcon || z2) {
            Log.d("StatusBar", "do not process notification. key=" + statusBarNotification.getKey());
            return false;
        }
        ExpandedNotification expandedNotification = new ExpandedNotification(this.mContextForUser, statusBarNotification);
        expandedNotification.setImportance(this.mNotificationData.getImportance(expandedNotification, rankingMap));
        if (!NotificationUtil.isMediaNotification(expandedNotification)) {
            MiuiNotificationCompat.disableColorized(statusBarNotification.getNotification());
        }
        try {
            Log.d("StatusBar", "onNotificationPosted key=" + statusBarNotification.getKey() + " isUpdate=" + z);
            if (z) {
                updateNotification(expandedNotification, rankingMap);
                return true;
            }
            addNotification(expandedNotification, rankingMap);
            return true;
        } catch (InflationException e) {
            handleInflationException(statusBarNotification, e);
            return false;
        }
    }

    /* access modifiers changed from: private */
    public void updateCurrentProfilesCache() {
        synchronized (this.mCurrentProfiles) {
            this.mCurrentProfiles.clear();
            if (this.mUserManager != null) {
                for (UserInfo userInfo : this.mUserManager.getProfiles(this.mCurrentUserId)) {
                    this.mCurrentProfiles.put(userInfo.id, userInfo);
                }
            }
        }
    }

    public boolean isNotificationForCurrentProfiles(StatusBarNotification statusBarNotification) {
        int i = this.mCurrentUserId;
        int userId = statusBarNotification.getUserId();
        if (DEBUG) {
            Log.v("StatusBar", String.format("%s: current userid: %d, notification userid: %d", new Object[]{statusBarNotification, Integer.valueOf(i), Integer.valueOf(userId)}));
        }
        return isCurrentProfile(userId);
    }

    /* access modifiers changed from: protected */
    public void setNotificationShown(StatusBarNotification statusBarNotification) {
        setNotificationsShown(new String[]{statusBarNotification.getKey()});
    }

    public void setNotificationsShown(String[] strArr) {
        try {
            this.mNotificationListener.setNotificationsShown(strArr);
        } catch (RuntimeException e) {
            Log.d("StatusBar", "failed setNotificationsShown: ", e);
        }
    }

    /* access modifiers changed from: protected */
    public boolean isCurrentProfile(int i) {
        boolean z;
        synchronized (this.mCurrentProfiles) {
            if (i != -1) {
                try {
                    if (this.mCurrentProfiles.get(i) == null) {
                        z = false;
                    }
                } catch (Throwable th) {
                    throw th;
                }
            }
            z = true;
        }
        return z;
    }

    public NotificationData getNotificationData() {
        return this.mNotificationData;
    }

    public NotificationGroupManager getGroupManager() {
        return this.mGroupManager;
    }

    public IStatusBarService getBarService() {
        return this.mBarService;
    }

    public void setNotificationSnoozed(StatusBarNotification statusBarNotification, NotificationSwipeActionHelper.SnoozeOption snoozeOption) {
        if (snoozeOption.criterion != null) {
            NotificationCompat.snoozeNotification(this.mNotificationListener, statusBarNotification.getKey(), snoozeOption.criterion.getId());
        } else {
            NotificationCompat.snoozeNotification(this.mNotificationListener, statusBarNotification.getKey(), (long) (snoozeOption.snoozeForMinutes * 60 * 1000));
        }
    }

    private void bindGuts(ExpandableNotificationRow expandableNotificationRow, NotificationMenuRowPlugin.MenuItem menuItem) {
        final ExpandableNotificationRow expandableNotificationRow2 = expandableNotificationRow;
        expandableNotificationRow.inflateGuts();
        expandableNotificationRow.setGutsView(menuItem);
        final ExpandedNotification statusBarNotification = expandableNotificationRow.getStatusBarNotification();
        expandableNotificationRow2.setTag(statusBarNotification.getPackageName());
        final NotificationGuts guts = expandableNotificationRow.getGuts();
        guts.setClosedListener(new NotificationGuts.OnGutsClosedListener() {
            public void onGutsClosed(NotificationGuts notificationGuts) {
                if (!notificationGuts.willBeRemoved() && !expandableNotificationRow2.isRemoved()) {
                    StatusBar statusBar = StatusBar.this;
                    statusBar.mStackScroller.onHeightChanged(expandableNotificationRow2, !statusBar.isPanelFullyCollapsed());
                }
                if (StatusBar.this.mNotificationGutsExposed == notificationGuts) {
                    NotificationGuts unused = StatusBar.this.mNotificationGutsExposed = null;
                    NotificationMenuRowPlugin.MenuItem unused2 = StatusBar.this.mGutsMenuItem = null;
                }
                expandableNotificationRow2.resetTranslation();
            }

            public void onGutsCloseAnimationEnd() {
                expandableNotificationRow2.resetTranslation();
            }
        });
        View gutsView = menuItem.getGutsView();
        if (gutsView instanceof NotificationSnooze) {
            NotificationSnooze notificationSnooze = (NotificationSnooze) gutsView;
            notificationSnooze.setSnoozeListener(this.mStackScroller.getSwipeActionHelper());
            notificationSnooze.setStatusBarNotification(statusBarNotification);
            notificationSnooze.setSnoozeOptions(expandableNotificationRow.getEntry().snoozeCriteria);
            guts.setHeightChangedListener(new NotificationGuts.OnHeightChangedListener(expandableNotificationRow2) {
                private final /* synthetic */ ExpandableNotificationRow f$1;

                {
                    this.f$1 = r2;
                }

                public final void onHeightChanged(NotificationGuts notificationGuts) {
                    StatusBar.this.lambda$bindGuts$2$StatusBar(this.f$1, notificationGuts);
                }
            });
        }
        if (gutsView instanceof NotificationAggregate) {
            ((NotificationAggregate) gutsView).bindNotification(statusBarNotification, new NotificationAggregate.ClickListener() {
                public void onClickConfirm(View view) {
                    StatusBar.this.saveAndCloseNotificationMenu(expandableNotificationRow2, guts, view);
                    StatusBar.this.saveFiler(statusBarNotification);
                    ((NotificationStat) Dependency.get(NotificationStat.class)).handleNotiSetConfigEvent(statusBarNotification);
                }

                public void onClickCancel(View view) {
                    StatusBar.this.saveAndCloseNotificationMenu(expandableNotificationRow2, guts, view);
                }
            });
        }
        if (gutsView instanceof NotificationInfo) {
            NotificationInfo notificationInfo = (NotificationInfo) gutsView;
            final UserHandle user = statusBarNotification.getUser();
            INotificationManager asInterface = INotificationManager.Stub.asInterface(ServiceManager.getService("notification"));
            String packageName = statusBarNotification.getPackageName();
            ArraySet arraySet = new ArraySet();
            arraySet.add(expandableNotificationRow.getEntry().channel);
            if (expandableNotificationRow.isSummaryWithChildren()) {
                List<ExpandableNotificationRow> notificationChildren = expandableNotificationRow.getNotificationChildren();
                int size = notificationChildren.size();
                for (int i = 0; i < size; i++) {
                    ExpandableNotificationRow expandableNotificationRow3 = notificationChildren.get(i);
                    NotificationChannelCompat notificationChannelCompat = expandableNotificationRow3.getEntry().channel;
                    ExpandedNotification statusBarNotification2 = expandableNotificationRow3.getStatusBarNotification();
                    if (statusBarNotification2.getUser().equals(user) && statusBarNotification2.getPackageName().equals(packageName)) {
                        arraySet.add(notificationChannelCompat);
                    }
                }
            }
            int importance = expandableNotificationRow.getEntry().channel.getImportance();
            int indexOf = this.mNotificationData.indexOf(expandableNotificationRow.getEntry());
            ArrayList arrayList = new ArrayList(arraySet);
            final ExpandableNotificationRow expandableNotificationRow4 = expandableNotificationRow;
            final NotificationMenuRowPlugin.MenuItem menuItem2 = menuItem;
            notificationInfo.bindNotification(asInterface, arrayList, importance, statusBarNotification, indexOf, new NotificationInfo.ClickListener() {
                public void onClickSettings(View view) {
                    StatusBar.this.saveAndCloseNotificationMenu(expandableNotificationRow4, guts, view);
                    StatusBar.this.onClickMenuSettings(expandableNotificationRow4, menuItem2, false);
                }

                public void onClickDone(View view) {
                    StatusBar.this.saveAndCloseNotificationMenu(expandableNotificationRow4, guts, view);
                }

                public void onClickCheckSave(Runnable runnable) {
                    int i;
                    if (!StatusBar.this.isLockscreenPublicMode(user.getIdentifier()) || !((i = StatusBar.this.mState) == 1 || i == 2)) {
                        runnable.run();
                    } else {
                        StatusBar.this.onLockedNotificationImportanceChange(new ActivityStarter.OnDismissAction(runnable) {
                            private final /* synthetic */ Runnable f$0;

                            {
                                this.f$0 = r1;
                            }

                            public final boolean onDismiss() {
                                return this.f$0.run();
                            }
                        });
                    }
                }
            });
        }
    }

    public /* synthetic */ void lambda$bindGuts$2$StatusBar(ExpandableNotificationRow expandableNotificationRow, NotificationGuts notificationGuts) {
        this.mStackScroller.onHeightChanged(expandableNotificationRow, expandableNotificationRow.isShown());
    }

    /* access modifiers changed from: private */
    public void saveAndCloseNotificationMenu(ExpandableNotificationRow expandableNotificationRow, NotificationGuts notificationGuts, View view) {
        notificationGuts.resetFalsingCheck();
        int[] iArr = new int[2];
        int[] iArr2 = new int[2];
        expandableNotificationRow.getLocationOnScreen(iArr);
        view.getLocationOnScreen(iArr2);
        closeAndSaveGuts(false, false, true, (iArr2[0] - iArr[0]) + (view.getWidth() / 2), (iArr2[1] - iArr[1]) + (view.getHeight() / 2), true);
    }

    /* access modifiers changed from: protected */
    public SwipeHelper.LongPressListener getNotificationLongClicker() {
        return new SwipeHelper.LongPressListener() {
            public boolean onLongPress(View view, int i, int i2, NotificationMenuRowPlugin.MenuItem menuItem) {
                if (!(view instanceof ExpandableNotificationRow)) {
                    return false;
                }
                if (view.getWindowToken() == null) {
                    Log.e("StatusBar", "Trying to show notification guts, but not attached to window");
                    return false;
                }
                ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) view;
                ((NotificationStat) Dependency.get(NotificationStat.class)).logNotificationLongPress(expandableNotificationRow.getEntry().key);
                if (expandableNotificationRow.isDark()) {
                    return false;
                }
                if (expandableNotificationRow.isExpandable() && !NotificationUtil.isExpandingEnabled(StatusBar.this.isKeyguardShowing())) {
                    expandableNotificationRow.getExpandClickListener().onClick(expandableNotificationRow);
                    return true;
                } else if (expandableNotificationRow.isPinned() || !NotificationUtil.isExpandingEnabled(StatusBar.this.isKeyguardShowing())) {
                    return false;
                } else {
                    return StatusBar.this.updateGutsState(expandableNotificationRow, i, i2, menuItem);
                }
            }
        };
    }

    /* access modifiers changed from: protected */
    public SwipeHelper.MenuPressListener getNotificationMenuClicker() {
        return new SwipeHelper.MenuPressListener() {
            public boolean onMenuPress(View view, int i, int i2, NotificationMenuRowPlugin.MenuItem menuItem) {
                return StatusBar.this.updateGutsState((ExpandableNotificationRow) view, i, i2, menuItem);
            }
        };
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x0094  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onClickMenuSettings(com.android.systemui.statusbar.ExpandableNotificationRow r8, com.android.systemui.plugins.statusbar.NotificationMenuRowPlugin.MenuItem r9, boolean r10) {
        /*
            r7 = this;
            android.view.View r9 = r9.getGutsView()
            boolean r9 = r9 instanceof com.android.systemui.statusbar.NotificationInfo
            if (r9 == 0) goto L_0x00aa
            com.android.systemui.statusbar.NotificationData$Entry r9 = r8.getEntry()
            com.android.systemui.miui.statusbar.ExpandedNotification r9 = r9.notification
            java.lang.String r10 = r9.getPackageName()
            java.lang.String r0 = com.android.systemui.miui.statusbar.notification.NotificationUtil.getMessageId(r9)
            android.content.Context r1 = r7.mContext
            android.content.pm.PackageManager r1 = r1.getPackageManager()
            boolean r2 = com.android.systemui.miui.statusbar.notification.NotificationUtil.isHybrid(r9)
            r3 = 1
            r4 = 0
            if (r2 == 0) goto L_0x0091
            android.content.Intent r2 = new android.content.Intent
            android.content.Intent r5 = APP_NOTIFICATION_PREFS_CATEGORY_INTENT
            r2.<init>(r5)
            android.content.Intent r2 = r2.setPackage(r10)
            java.util.List r1 = r1.queryIntentActivities(r2, r4)
            int r5 = r1.size()
            if (r5 <= 0) goto L_0x0091
            java.lang.Object r1 = r1.get(r4)
            android.content.pm.ResolveInfo r1 = (android.content.pm.ResolveInfo) r1
            android.content.pm.ActivityInfo r1 = r1.activityInfo
            java.lang.String r5 = r1.packageName
            java.lang.String r1 = r1.name
            r2.setClassName(r5, r1)
            r1 = 32768(0x8000, float:4.5918E-41)
            r2.addFlags(r1)
            r1 = 268435456(0x10000000, float:2.5243549E-29)
            r2.addFlags(r1)
            java.lang.String r1 = ""
            java.lang.String r5 = "appName"
            r2.putExtra(r5, r1)
            java.lang.String r5 = "packageName"
            r2.putExtra(r5, r10)
            int r5 = r9.getAppUid()
            int r5 = android.os.UserHandle.getUserId(r5)
            java.lang.String r6 = "userId"
            r2.putExtra(r6, r5)
            java.lang.String r5 = "messageId"
            r2.putExtra(r5, r0)
            java.lang.String r5 = "notificationId"
            r2.putExtra(r5, r1)
            java.lang.String r1 = com.android.systemui.miui.statusbar.notification.NotificationUtil.getCategory(r9)
            java.lang.String r5 = "miui.category"
            r2.putExtra(r5, r1)
            android.content.Context r1 = r7.mContext     // Catch:{ ActivityNotFoundException -> 0x0089 }
            android.os.UserHandle r5 = android.os.UserHandle.CURRENT     // Catch:{ ActivityNotFoundException -> 0x0089 }
            r1.startActivityAsUser(r2, r5)     // Catch:{ ActivityNotFoundException -> 0x0089 }
            r1 = r3
            goto L_0x0092
        L_0x0089:
            r1 = move-exception
            java.lang.String r2 = "StatusBar"
            java.lang.String r5 = "Failed startActivityAsUser() "
            android.util.Log.e(r2, r5, r1)
        L_0x0091:
            r1 = r4
        L_0x0092:
            if (r1 != 0) goto L_0x00a7
            android.content.Context r1 = r7.mContext
            com.android.systemui.statusbar.NotificationData$Entry r8 = r8.getEntry()
            com.android.systemui.miui.statusbar.ExpandedNotification r8 = r8.notification
            java.lang.String r8 = r8.getAppName()
            int r9 = r9.getAppUid()
            com.android.systemui.miui.statusbar.notification.NotificationSettingsHelper.startAppNotificationSettings(r1, r10, r8, r9, r0)
        L_0x00a7:
            r7.animateCollapsePanels(r4, r3)
        L_0x00aa:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.phone.StatusBar.onClickMenuSettings(com.android.systemui.statusbar.ExpandableNotificationRow, com.android.systemui.plugins.statusbar.NotificationMenuRowPlugin$MenuItem, boolean):void");
    }

    /* access modifiers changed from: private */
    public boolean updateGutsState(ExpandableNotificationRow expandableNotificationRow, int i, int i2, NotificationMenuRowPlugin.MenuItem menuItem) {
        if (expandableNotificationRow.areGutsExposed()) {
            closeAndSaveGuts(false, false, true, -1, -1, true);
            return false;
        }
        bindGuts(expandableNotificationRow, menuItem);
        NotificationGuts guts = expandableNotificationRow.getGuts();
        if (guts == null) {
            return false;
        }
        MetricsLogger.action(this.mContext, 204);
        guts.setVisibility(4);
        final ExpandableNotificationRow expandableNotificationRow2 = expandableNotificationRow;
        final NotificationGuts notificationGuts = guts;
        final int i3 = i;
        final int i4 = i2;
        final NotificationMenuRowPlugin.MenuItem menuItem2 = menuItem;
        guts.post(new Runnable() {
            public void run() {
                if (expandableNotificationRow2.getWindowToken() == null) {
                    Log.e("StatusBar", "Trying to show notification guts, but not attached to window");
                    return;
                }
                StatusBar.this.closeAndSaveGuts(true, true, true, -1, -1, false);
                StatusBar statusBar = StatusBar.this;
                notificationGuts.openControls(i3, i4, statusBar.mState == 1 && !statusBar.mAccessibilityManager.isTouchExplorationEnabled(), menuItem2);
                expandableNotificationRow2.closeRemoteInput();
                StatusBar.this.mStackScroller.onHeightChanged(expandableNotificationRow2, true);
                NotificationGuts unused = StatusBar.this.mNotificationGutsExposed = notificationGuts;
                NotificationMenuRowPlugin.MenuItem unused2 = StatusBar.this.mGutsMenuItem = menuItem2;
            }
        });
        return true;
    }

    /* access modifiers changed from: private */
    public void saveFiler(ExpandedNotification expandedNotification) {
        String targetPackageName = expandedNotification.getTargetPackageName();
        NotificationSettingsHelper.setFoldImportance(this.mContextForUser, targetPackageName, -1);
        this.mContext.getContentResolver().notifyChange(NotificationProvider.URI_FOLD_IMPORTANCE.buildUpon().appendQueryParameter("package", targetPackageName).appendQueryParameter("foldImportance", "-1").build(), this.mFoldImportanceObserver, true, this.mCurrentUserId);
    }

    /* access modifiers changed from: private */
    public void updateAppBadgeNum(ExpandedNotification expandedNotification) {
        int i;
        CharSequence charSequence;
        String str;
        if (expandedNotification != null) {
            int i2 = 0;
            int identifier = expandedNotification.getUser().getIdentifier();
            String packageName = expandedNotification.getPackageName();
            CharSequence messageClassName = NotificationUtil.getMessageClassName(expandedNotification);
            boolean canShowBadge = NotificationSettingsHelper.canShowBadge(this.mContextForUser, expandedNotification.getPackageName());
            if (canShowBadge) {
                List<NotificationData.Entry> pkgNotifications = this.mNotificationData.getPkgNotifications(packageName);
                if (NotificationUtil.isMissedCallNotification(expandedNotification)) {
                    for (NotificationData.Entry next : pkgNotifications) {
                        if (NotificationUtil.isMissedCallNotification(next.notification) && needStatBadgeNum(next, expandedNotification)) {
                            i2 += next.notification.getMessageCount();
                        }
                    }
                    str = "com.android.contacts";
                    i = i2;
                    charSequence = ".activities.TwelveKeyDialer";
                    updateAppBadgeNum(str, charSequence, i, identifier, canShowBadge);
                    return;
                }
                for (NotificationData.Entry next2 : pkgNotifications) {
                    if (next2.notification.getPackageName().equals(packageName) && TextUtils.equals(NotificationUtil.getMessageClassName(next2.notification), messageClassName) && needStatBadgeNum(next2, expandedNotification)) {
                        i2 += next2.notification.getMessageCount();
                    }
                }
            }
            i = i2;
            str = packageName;
            charSequence = messageClassName;
            updateAppBadgeNum(str, charSequence, i, identifier, canShowBadge);
            return;
        }
        updateAppBadgeNum((String) null, (CharSequence) null, 0, 0, false);
    }

    private boolean needStatBadgeNum(NotificationData.Entry entry, ExpandedNotification expandedNotification) {
        return UserHandle.isSameUser(entry.notification.getUid(), expandedNotification.getUid()) && NotificationUtil.needStatBadgeNum(entry) && !entry.isMediaNotification() && !this.mGroupManager.isSummaryHasChildren(entry.notification);
    }

    private void updateAppBadgeNum(String str, CharSequence charSequence, int i, int i2, boolean z) {
        String str2;
        if (str == null) {
            str2 = "";
        } else {
            str2 = str + "/" + charSequence;
        }
        Intent intent = new Intent("android.intent.action.APPLICATION_MESSAGE_UPDATE");
        intent.putExtra("android.intent.extra.update_application_message_text", i > 0 ? String.valueOf(i) : null);
        intent.putExtra("android.intent.extra.update_application_component_name", str2);
        intent.putExtra("userId", i2);
        intent.putExtra("targetPkg", charSequence);
        intent.putExtra("miui.intent.extra.application_show_corner", z);
        intent.setPackage("com.miui.home");
        Log.d("StatusBar", "update app badge num: " + str2 + ",num=" + i + ",isAllowed=" + z + ",userId=" + i2);
        if (i2 == -1) {
            this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        } else {
            this.mContext.sendBroadcastAsUser(intent, UserHandle.CURRENT);
        }
    }

    /* access modifiers changed from: private */
    public void filterPackageNotifications(String str) {
        for (NotificationData.Entry entry : this.mNotificationData.getPkgNotifications(str)) {
            filterNotification(entry.notification);
        }
    }

    public NotificationGuts getExposedGuts() {
        return this.mNotificationGutsExposed;
    }

    public void closeAndSaveGuts(boolean z, boolean z2, boolean z3, int i, int i2, boolean z4) {
        NotificationGuts notificationGuts = this.mNotificationGutsExposed;
        if (notificationGuts != null) {
            notificationGuts.closeControls(z, z3, i, i2, z2);
        }
        if (z4) {
            this.mStackScroller.resetExposedMenuView(false, true);
        }
    }

    public void toggleSplitScreen() {
        toggleSplitScreenMode(-1, -1);
    }

    public void preloadRecentApps() {
        this.mHandler.removeMessages(1022);
        this.mHandler.sendEmptyMessage(1022);
    }

    public void cancelPreloadRecentApps() {
        this.mHandler.removeMessages(1023);
        this.mHandler.sendEmptyMessage(1023);
    }

    public void dismissKeyboardShortcutsMenu() {
        this.mHandler.removeMessages(1027);
        this.mHandler.sendEmptyMessage(1027);
    }

    public void toggleKeyboardShortcutsMenu(int i) {
        this.mHandler.removeMessages(1026);
        this.mHandler.obtainMessage(1026, i, 0).sendToTarget();
    }

    /* access modifiers changed from: protected */
    public void toggleKeyboardShortcuts(int i) {
        KeyboardShortcuts.toggle(this.mContext, i);
    }

    /* access modifiers changed from: protected */
    public void dismissKeyboardShortcuts() {
        KeyboardShortcuts.dismiss();
    }

    public void setLockscreenPublicMode(boolean z, int i) {
        this.mLockscreenPublicMode.put(i, z);
    }

    public boolean isLockscreenPublicMode(int i) {
        if (i == -1) {
            return this.mLockscreenPublicMode.get(this.mCurrentUserId, false);
        }
        return this.mLockscreenPublicMode.get(i, false);
    }

    public boolean userAllowsNotificationsInPublic(int i) {
        boolean z = true;
        if (isCurrentProfile(i) && i != this.mCurrentUserId) {
            return true;
        }
        if (this.mUsersAllowingNotifications.indexOfKey(i) >= 0) {
            return this.mUsersAllowingNotifications.get(i);
        }
        boolean z2 = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "lock_screen_show_notifications", 0, i) != 0;
        boolean adminAllowsKeyguardFeature = adminAllowsKeyguardFeature(i, 4);
        if (!z2 || !adminAllowsKeyguardFeature) {
            z = false;
        }
        this.mUsersAllowingNotifications.append(i, z);
        return z;
    }

    public boolean userAllowsPrivateNotificationsInPublic(int i) {
        boolean z = true;
        if (i == -1) {
            return true;
        }
        if (this.mUsersAllowingPrivateNotifications.indexOfKey(i) >= 0) {
            return this.mUsersAllowingPrivateNotifications.get(i);
        }
        boolean z2 = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "lock_screen_allow_private_notifications", 0, i) != 0;
        boolean adminAllowsKeyguardFeature = adminAllowsKeyguardFeature(i, 8);
        if (!z2 || !adminAllowsKeyguardFeature) {
            z = false;
        }
        this.mUsersAllowingPrivateNotifications.append(i, z);
        return z;
    }

    private boolean adminAllowsKeyguardFeature(int i, int i2) {
        if (i == -1 || (this.mDevicePolicyManager.getKeyguardDisabledFeatures((ComponentName) null, i) & i2) == 0) {
            return true;
        }
        return false;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:4:0x000c, code lost:
        r0 = r1.mCurrentUserId;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean shouldHideNotifications(int r2) {
        /*
            r1 = this;
            boolean r0 = r1.isLockscreenPublicMode(r2)
            if (r0 == 0) goto L_0x000c
            boolean r0 = r1.userAllowsNotificationsInPublic(r2)
            if (r0 == 0) goto L_0x0016
        L_0x000c:
            int r0 = r1.mCurrentUserId
            if (r2 == r0) goto L_0x0018
            boolean r1 = r1.shouldHideNotifications((int) r0)
            if (r1 == 0) goto L_0x0018
        L_0x0016:
            r1 = 1
            goto L_0x0019
        L_0x0018:
            r1 = 0
        L_0x0019:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.phone.StatusBar.shouldHideNotifications(int):boolean");
    }

    public boolean shouldHideNotifications(String str) {
        return isLockscreenPublicMode(this.mCurrentUserId) && this.mNotificationData.getVisibilityOverride(str) == -1;
    }

    public boolean isSecurelyLocked(int i) {
        return isLockscreenPublicMode(i);
    }

    public void onNotificationClear(ExpandedNotification expandedNotification) {
        this.mNotificationData.performRemoveNotification(expandedNotification);
    }

    public void onPanelLaidOut() {
        if (this.mState == 1 && getMaxKeyguardNotifications(false) != getMaxKeyguardNotifications(true)) {
            updateRowStates();
        }
    }

    /* access modifiers changed from: protected */
    public void inflateViews(final NotificationData.Entry entry, ViewGroup viewGroup) {
        entry.mIsShowMiniWindowBar = showMiniWindowBar(entry.notification);
        final ExpandedNotification expandedNotification = entry.notification;
        if (entry.row != null) {
            entry.reset();
            updateNotification(entry, expandedNotification, entry.row);
            return;
        }
        new RowInflaterTask().inflate(this.mContext, viewGroup, entry, new RowInflaterTask.RowInflationFinishedListener() {
            public void onInflationFinished(ExpandableNotificationRow expandableNotificationRow) {
                StatusBar.this.bindRow(entry, expandableNotificationRow);
                StatusBar.this.updateNotification(entry, expandedNotification, expandableNotificationRow);
            }
        });
    }

    private boolean showMiniWindowBar(ExpandedNotification expandedNotification) {
        return this.mState == 0 && this.mAppMiniWindowManager.canNotificationSlide(this.mContext, expandedNotification);
    }

    /* access modifiers changed from: private */
    public void bindRow(NotificationData.Entry entry, final ExpandableNotificationRow expandableNotificationRow) {
        expandableNotificationRow.setExpansionLogger(this, entry.notification.getKey());
        expandableNotificationRow.setGroupManager(this.mGroupManager);
        expandableNotificationRow.setHeadsUpManager(this.mHeadsUpManager);
        expandableNotificationRow.setRemoteInputController(this.mRemoteInputController);
        expandableNotificationRow.setOnExpandClickListener(this);
        expandableNotificationRow.setRemoteViewClickHandler(this.mOnClickHandler);
        expandableNotificationRow.setInflationCallback(this);
        expandableNotificationRow.setInCallCallback(this);
        expandableNotificationRow.setAppName(entry.notification.getAppName());
        expandableNotificationRow.setOnDismissRunnable(new Runnable() {
            public void run() {
                ((NotificationStat) Dependency.get(NotificationStat.class)).onRemove(expandableNotificationRow, StatusBar.this.mNotificationData.indexOf(expandableNotificationRow.getEntry()), StatusBar.this.mHeadsUpManager.isHeadsUp(expandableNotificationRow.getEntry().key), StatusBar.this.mIsKeyguard);
                StatusBar.this.performRemoveNotification(expandableNotificationRow.getStatusBarNotification());
            }
        });
        expandableNotificationRow.setDescendantFocusability(393216);
        if (ENABLE_REMOTE_INPUT) {
            expandableNotificationRow.setDescendantFocusability(131072);
        }
    }

    /* access modifiers changed from: private */
    public void updateNotification(NotificationData.Entry entry, ExpandedNotification expandedNotification, ExpandableNotificationRow expandableNotificationRow) {
        boolean z = false;
        expandableNotificationRow.setNeedsRedaction(false);
        boolean z2 = this.mNotificationData.isAmbient(expandedNotification.getKey()) && !expandedNotification.getNotification().isGroupSummary();
        boolean z3 = this.mNotificationData.get(entry.key) != null;
        boolean isLowPriority = expandableNotificationRow.isLowPriority();
        expandableNotificationRow.setIsLowPriority(false);
        expandableNotificationRow.setLowPriorityStateUpdated(z3 && isLowPriority != z2);
        this.mNotificationClicker.register(expandableNotificationRow, expandedNotification);
        entry.targetSdk = entry.notification.getTargetSdk();
        int i = entry.targetSdk;
        expandableNotificationRow.setLegacy(i >= 9 && i < 21);
        entry.autoRedacted = entry.notification.getNotification().publicVersion == null;
        entry.row = expandableNotificationRow;
        entry.row.setOnActivatedListener(this);
        boolean isImportantMessaging = this.mMessagingUtil.isImportantMessaging(expandedNotification, entry.notification.getImportance());
        boolean z4 = isImportantMessaging && this.mPanelExpanded;
        if ((isImportantMessaging && NotificationUtil.showGoogleStyle()) || shouldUseIncreaedColleapsedHeight(expandedNotification)) {
            z = true;
        }
        expandableNotificationRow.setUseIncreasedCollapsedHeight(z);
        expandableNotificationRow.setUseIncreasedHeadsUpHeight(z4);
        expandableNotificationRow.updateNotification(entry);
    }

    private boolean shouldUseIncreaedColleapsedHeight(StatusBarNotification statusBarNotification) {
        return !statusBarNotification.getNotification().extras.containsKey("android.template") && statusBarNotification.getNotification().extras.containsKey("android.progress");
    }

    /* access modifiers changed from: private */
    public void processForRemoteInput(Notification notification) {
        Bundle bundle;
        RemoteInput[] remoteInputs;
        if (ENABLE_REMOTE_INPUT && (bundle = notification.extras) != null && bundle.containsKey("android.wearable.EXTENSIONS")) {
            Notification.Action[] actionArr = notification.actions;
            if (actionArr == null || actionArr.length == 0) {
                List<Notification.Action> actions = new Notification.WearableExtender(notification).getActions();
                int size = actions.size();
                Notification.Action action = null;
                for (int i = 0; i < size; i++) {
                    Notification.Action action2 = actions.get(i);
                    if (!(action2 == null || (remoteInputs = action2.getRemoteInputs()) == null)) {
                        int length = remoteInputs.length;
                        int i2 = 0;
                        while (true) {
                            if (i2 >= length) {
                                break;
                            } else if (remoteInputs[i2].getAllowFreeFormInput()) {
                                action = action2;
                                break;
                            } else {
                                i2++;
                            }
                        }
                        if (action != null) {
                            break;
                        }
                    }
                }
                if (action != null) {
                    Notification.Builder recoverBuilder = NotificationCompat.recoverBuilder(this.mContext, notification);
                    recoverBuilder.setActions(new Notification.Action[]{action});
                    recoverBuilder.build();
                }
            }
        }
    }

    public void startPendingIntentDismissingKeyguard(final PendingIntent pendingIntent) {
        if (isDeviceProvisioned()) {
            dismissKeyguardThenExecute(new ActivityStarter.OnDismissAction() {
                public boolean onDismiss() {
                    new Thread() {
                        /* JADX WARNING: Can't wrap try/catch for region: R(6:0|1|2|3|6|(2:8|9)(1:10)) */
                        /* JADX WARNING: Code restructure failed: missing block: B:4:0x0019, code lost:
                            r0 = move-exception;
                         */
                        /* JADX WARNING: Code restructure failed: missing block: B:5:0x001a, code lost:
                            android.util.Log.w("StatusBar", "Sending intent failed: " + r0);
                         */
                        /* JADX WARNING: Failed to process nested try/catch */
                        /* JADX WARNING: Missing exception handler attribute for start block: B:2:0x0007 */
                        /* JADX WARNING: Removed duplicated region for block: B:10:? A[RETURN, SYNTHETIC] */
                        /* JADX WARNING: Removed duplicated region for block: B:8:0x003a  */
                        /* Code decompiled incorrectly, please refer to instructions dump. */
                        public void run() {
                            /*
                                r9 = this;
                                android.app.IActivityManager r0 = android.app.ActivityManagerCompat.getService()     // Catch:{ RemoteException -> 0x0007 }
                                r0.resumeAppSwitches()     // Catch:{ RemoteException -> 0x0007 }
                            L_0x0007:
                                com.android.systemui.statusbar.phone.StatusBar$105 r0 = com.android.systemui.statusbar.phone.StatusBar.AnonymousClass105.this     // Catch:{ CanceledException -> 0x0019 }
                                android.app.PendingIntent r1 = r4     // Catch:{ CanceledException -> 0x0019 }
                                r2 = 0
                                r3 = 0
                                r4 = 0
                                r5 = 0
                                r6 = 0
                                r7 = 0
                                android.os.Bundle r8 = com.android.systemui.statusbar.phone.StatusBar.getActivityOptions()     // Catch:{ CanceledException -> 0x0019 }
                                r1.send(r2, r3, r4, r5, r6, r7, r8)     // Catch:{ CanceledException -> 0x0019 }
                                goto L_0x0030
                            L_0x0019:
                                r0 = move-exception
                                java.lang.StringBuilder r1 = new java.lang.StringBuilder
                                r1.<init>()
                                java.lang.String r2 = "Sending intent failed: "
                                r1.append(r2)
                                r1.append(r0)
                                java.lang.String r0 = r1.toString()
                                java.lang.String r1 = "StatusBar"
                                android.util.Log.w(r1, r0)
                            L_0x0030:
                                com.android.systemui.statusbar.phone.StatusBar$105 r0 = com.android.systemui.statusbar.phone.StatusBar.AnonymousClass105.this
                                android.app.PendingIntent r0 = r4
                                boolean r0 = r0.isActivity()
                                if (r0 == 0) goto L_0x0043
                                com.android.systemui.statusbar.phone.StatusBar$105 r9 = com.android.systemui.statusbar.phone.StatusBar.AnonymousClass105.this
                                com.android.systemui.statusbar.phone.StatusBar r9 = com.android.systemui.statusbar.phone.StatusBar.this
                                com.android.systemui.assist.AssistManager r9 = r9.mAssistManager
                                r9.hideAssist()
                            L_0x0043:
                                return
                            */
                            throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.phone.StatusBar.AnonymousClass105.AnonymousClass1.run():void");
                        }
                    }.start();
                    StatusBar.this.animateCollapsePanels(2, true, true);
                    StatusBar.this.visibilityChanged(false);
                    return true;
                }
            }, pendingIntent.isActivity() && PreviewInflater.wouldLaunchResolverActivity(this.mContext, pendingIntent.getIntent(), this.mCurrentUserId));
        }
    }

    public static Bundle getActivityOptions(RemoteAnimationAdapter remoteAnimationAdapter) {
        ActivityOptions activityOptions;
        if (remoteAnimationAdapter != null) {
            activityOptions = ActivityOptions.makeRemoteAnimation(remoteAnimationAdapter);
        } else {
            activityOptions = ActivityOptions.makeBasic();
        }
        ActivityOptionsCompat.setLaunchStackId(activityOptions, 1, 4, -1);
        return activityOptions.toBundle();
    }

    public static Bundle getActivityOptions() {
        return getActivityOptions((RemoteAnimationAdapter) null);
    }

    public void visibilityChanged(boolean z) {
        if (this.mVisible != z) {
            this.mVisible = z;
            if (!z) {
                closeAndSaveGuts(true, true, true, -1, -1, true);
            }
        }
        updateVisibleToUser();
    }

    /* access modifiers changed from: protected */
    public void updateVisibleToUser() {
        boolean z = this.mVisibleToUser;
        this.mVisibleToUser = this.mVisible && this.mDeviceInteractive;
        boolean z2 = this.mVisibleToUser;
        if (z != z2) {
            handleVisibleToUserChanged(z2);
        }
    }

    private void updateFsgState() {
        this.mHandler.removeMessages(b.d);
        this.mHandler.sendEmptyMessageDelayed(b.d, 10);
    }

    /* access modifiers changed from: private */
    public void onUpdateFsgState() {
        boolean z = this.mExpandedVisible && !this.mInPinnedMode;
        if (this.mIsFsgMode && !this.mIsKeyguard && z != this.mShouldDisableFsgMode) {
            Utils.updateFsgState(this.mContext, "typefrom_status_bar_expansion", z);
        }
        this.mShouldDisableFsgMode = z;
    }

    public void clearNotificationEffects() {
        this.mUiOffloadThread.submit(new Runnable() {
            public void run() {
                try {
                    StatusBar.this.mBarService.clearNotificationEffects();
                } catch (RemoteException unused) {
                }
            }
        });
    }

    /* access modifiers changed from: package-private */
    public void handleNotificationError(StatusBarNotification statusBarNotification, String str) {
        removeNotification(statusBarNotification.getKey(), (NotificationListenerService.RankingMap) null);
        try {
            this.mBarService.onNotificationError(statusBarNotification.getPackageName(), statusBarNotification.getTag(), statusBarNotification.getId(), statusBarNotification.getUid(), statusBarNotification.getInitialPid(), str, statusBarNotification.getUserId());
        } catch (RemoteException unused) {
        }
    }

    /* access modifiers changed from: protected */
    public ExpandedNotification removeNotificationViews(String str, NotificationListenerService.RankingMap rankingMap, boolean z) {
        NotificationData.Entry remove = this.mNotificationData.remove(str, rankingMap, z);
        if (remove == null) {
            Log.w("StatusBar", "removeNotification for unknown key: " + str);
            return null;
        }
        if (z) {
            updateNotifications();
        }
        ((LeakDetector) Dependency.get(LeakDetector.class)).trackGarbage(remove);
        return remove.notification;
    }

    /* access modifiers changed from: protected */
    public NotificationData.Entry createNotificationViews(ExpandedNotification expandedNotification) throws InflationException {
        if (DEBUG) {
            Log.d("StatusBar", "createNotificationViews(notification=" + expandedNotification);
        }
        NotificationData.Entry entry = new NotificationData.Entry(expandedNotification);
        entry.canBubble = this.mNotificationData.canBubble(entry.key);
        ((LeakDetector) Dependency.get(LeakDetector.class)).trackInstance(entry);
        entry.createIcons(this.mContext, expandedNotification);
        inflateViews(entry, this.mStackScroller);
        return entry;
    }

    /* access modifiers changed from: protected */
    public void addNotificationViews(NotificationData.Entry entry) {
        if (entry != null) {
            this.mNotificationData.add(entry);
            ((NotificationsMonitor) Dependency.get(NotificationsMonitor.class)).notifyNotificationArrived(entry.notification);
            ((NotificationStat) Dependency.get(NotificationStat.class)).onArrive(entry.notification);
            updateNotifications();
        }
    }

    /* access modifiers changed from: protected */
    public void updateRowStates() {
        int childCount = this.mStackScroller.getChildCount();
        boolean z = false;
        boolean z2 = this.mState == 1;
        this.mStackScroller.setMaxDisplayedNotifications((this.mStatusBarFragment == null || !z2) ? -1 : getMaxKeyguardNotifications(true));
        Stack stack = new Stack();
        for (int i = childCount - 1; i >= 0; i--) {
            View childAt = this.mStackScroller.getChildAt(i);
            if (childAt instanceof ExpandableNotificationRow) {
                stack.push((ExpandableNotificationRow) childAt);
            }
        }
        int i2 = 0;
        while (!stack.isEmpty()) {
            ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) stack.pop();
            NotificationData.Entry entry = expandableNotificationRow.getEntry();
            boolean isChildInGroupWithSummary = this.mGroupManager.isChildInGroupWithSummary(entry.notification);
            if (z2) {
                expandableNotificationRow.setOnKeyguard(true);
            } else {
                expandableNotificationRow.setOnKeyguard(false);
                expandableNotificationRow.setSystemExpanded(i2 == 0 && !isChildInGroupWithSummary);
            }
            entry.row.setShowAmbient(false);
            int userId = entry.notification.getUserId();
            boolean z3 = this.mGroupManager.isSummaryOfSuppressedGroup(entry.notification) && !entry.row.isRemoved();
            if (!isKeyguardShowing()) {
                entry.notification.setHasShownAfterUnlock(true);
            }
            boolean z4 = shouldShowOnKeyguard(entry) || expandableNotificationRow.isChildInGroup();
            if (z3 || ((isLockscreenPublicMode(userId) && !this.mShowLockscreenNotifications) || (this.mState != 0 && !z4))) {
                entry.row.setVisibility(8);
            } else {
                boolean z5 = entry.row.getVisibility() == 8;
                if (z5) {
                    entry.row.setVisibility(0);
                }
                if (!isChildInGroupWithSummary && !entry.row.isRemoved()) {
                    if (z5) {
                        this.mStackScroller.generateAddAnimation(entry.row, !z4);
                    }
                    i2++;
                }
            }
            if (expandableNotificationRow.isSummaryWithChildren()) {
                List<ExpandableNotificationRow> notificationChildren = expandableNotificationRow.getNotificationChildren();
                for (int size = notificationChildren.size() - 1; size >= 0; size--) {
                    stack.push(notificationChildren.get(size));
                }
            }
        }
        NotificationPanelView notificationPanelView = this.mNotificationPanel;
        if (i2 == 0) {
            z = true;
        }
        notificationPanelView.setNoVisibleNotifications(z);
    }

    public boolean shouldShowOnKeyguard(NotificationData.Entry entry) {
        return this.mShowLockscreenNotifications && !this.mNotificationData.isAmbient(entry.notification.getKey()) && isEnableKeyguard(entry);
    }

    private boolean isEnableKeyguard(NotificationData.Entry entry) {
        ExpandedNotification expandedNotification = entry.notification;
        if (entry.isMediaNotification() || entry.key.equals(this.mMediaNotificationKey) || NotificationUtil.isCts(expandedNotification)) {
            return true;
        }
        if (!expandedNotification.canShowOnKeyguard()) {
            return false;
        }
        if (this.mKeptOnKeyguard || this.mState != 1 || !expandedNotification.hasShownAfterUnlock()) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public void setShowLockscreenNotifications(boolean z) {
        this.mShowLockscreenNotifications = z;
    }

    /* access modifiers changed from: protected */
    public void setLockScreenAllowRemoteInput(boolean z) {
        this.mAllowLockscreenRemoteInput = z;
    }

    /* access modifiers changed from: private */
    public void updateLockscreenNotificationSetting() {
        boolean z = false;
        boolean z2 = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "lock_screen_show_notifications", 1, this.mCurrentUserId) != 0;
        int keyguardDisabledFeatures = this.mDevicePolicyManager.getKeyguardDisabledFeatures((ComponentName) null, this.mCurrentUserId);
        setShowLockscreenNotifications(z2 && ((keyguardDisabledFeatures & 4) == 0));
        if (ENABLE_LOCK_SCREEN_ALLOW_REMOTE_INPUT) {
            boolean z3 = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "lock_screen_allow_remote_input", 0, this.mCurrentUserId) != 0;
            boolean z4 = (keyguardDisabledFeatures & 64) == 0;
            if (z3 && z4) {
                z = true;
            }
            setLockScreenAllowRemoteInput(z);
            return;
        }
        setLockScreenAllowRemoteInput(false);
    }

    public void updateNotification(ExpandedNotification expandedNotification, NotificationListenerService.RankingMap rankingMap) throws InflationException {
        if (!filterNotification(expandedNotification)) {
            String key = expandedNotification.getKey();
            if (!this.mMirrorDndEnable) {
                this.mBgHandler.obtainMessage(b.m, key).sendToTarget();
            }
            abortExistingInflation(key);
            NotificationData.Entry entry = this.mNotificationData.get(key);
            if (entry != null) {
                entry.hideSensitiveByAppLock = isHideSensitiveByAppLock(expandedNotification);
                this.mHeadsUpEntriesToRemoveOnSwitch.remove(entry);
                this.mRemoteInputEntriesToRemoveOnCollapse.remove(entry);
                this.mNotificationData.updateRanking(rankingMap);
                ExpandedNotification expandedNotification2 = entry.notification;
                if (!NotificationUtil.hasProgressbar(expandedNotification)) {
                    Log.d("StatusBar", "updateNotification old=" + expandedNotification2);
                }
                entry.notification = expandedNotification;
                entry.needUpdateBadgeNum = NotificationUtil.needRestatBadgeNum(expandedNotification, expandedNotification2);
                boolean z = entry.canShowBaged;
                entry.canShowBaged = NotificationSettingsHelper.canShowBadge(this.mContextForUser, expandedNotification.getPackageName());
                if (!z && entry.canShowBaged) {
                    entry.needUpdateBadgeNum = true;
                }
                if (!TextUtils.equals(expandedNotification2.getTargetPackageName(), expandedNotification.getTargetPackageName())) {
                    updateAppBadgeNum(expandedNotification2);
                }
                this.mGroupManager.onEntryUpdated(entry, expandedNotification2);
                ((BubbleController) Dependency.get(BubbleController.class)).onPreEntryUpdated(entry);
                entry.updateIcons(this.mContext, expandedNotification);
                inflateViews(entry, this.mStackScroller);
                this.mForegroundServiceController.updateNotification(expandedNotification, entry.notification.getImportance());
                updateNotifications();
                if (!expandedNotification.isClearable()) {
                    this.mStackScroller.snapViewIfNeeded(entry.row);
                }
                allowGroupShowOnKeyguardAgain(expandedNotification);
                if (DEBUG) {
                    boolean isNotificationForCurrentProfiles = isNotificationForCurrentProfiles(expandedNotification);
                    StringBuilder sb = new StringBuilder();
                    sb.append("notification is ");
                    sb.append(isNotificationForCurrentProfiles ? "" : "not ");
                    sb.append("for you");
                    Log.d("StatusBar", sb.toString());
                }
                setAreThereNotifications();
                postWakeUpForNotification(entry);
                ((NotificationsMonitor) Dependency.get(NotificationsMonitor.class)).notifyNotificationUpdated(expandedNotification);
            }
        }
    }

    private void allowGroupShowOnKeyguardAgain(ExpandedNotification expandedNotification) {
        ExpandableNotificationRow groupSummary;
        if (this.mGroupManager.isChildInGroupWithSummary(expandedNotification) && (groupSummary = this.mGroupManager.getGroupSummary((StatusBarNotification) expandedNotification)) != null && groupSummary.getEntry() != null) {
            NotificationData.Entry entry = groupSummary.getEntry();
            entry.notification.setHasShownAfterUnlock(false);
            NotificationGroupManager.NotificationGroup notificationGroup = this.mGroupManager.getNotificationGroup(entry.notification.getGroupKey());
            if (notificationGroup != null) {
                Iterator<NotificationData.Entry> it = notificationGroup.children.iterator();
                while (it.hasNext()) {
                    it.next().notification.setHasShownAfterUnlock(false);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void notifyHeadsUpScreenOff() {
        maybeEscalateHeadsUp();
    }

    private boolean alertAgain(NotificationData.Entry entry, Notification notification) {
        return entry == null || !entry.hasInterrupted() || (notification.flags & 8) == 0;
    }

    /* access modifiers changed from: protected */
    public boolean shouldPeek(NotificationData.Entry entry) {
        return shouldPeek(entry, entry.notification);
    }

    /* access modifiers changed from: protected */
    public boolean shouldPeek(NotificationData.Entry entry, ExpandedNotification expandedNotification) {
        if (!this.mUseHeadsUp || isDeviceInVrMode()) {
            Log.d("StatusBar", "No peeking: no huns or vr mode");
            return false;
        } else if (this.mNotificationData.shouldFilterOut(expandedNotification)) {
            Log.d("StatusBar", "No peeking: filtered notification: " + expandedNotification.getKey());
            return false;
        } else {
            if (!(((ScreenLifecycle) Dependency.get(ScreenLifecycle.class)).isScreenOn() && !SystemServicesProxy.getInstance(this.mContext).isDreaming()) && !isDozing()) {
                Log.d("StatusBar", "No peeking: not in use: " + expandedNotification.getKey());
                return false;
            } else if (this.mNotificationData.shouldSuppressScreenOn(expandedNotification.getKey())) {
                Log.d("StatusBar", "No peeking: suppressed by DND: " + expandedNotification.getKey());
                return false;
            } else if (entry.hasJustLaunchedFullScreenIntent()) {
                Log.d("StatusBar", "No peeking: recent fullscreen: " + expandedNotification.getKey());
                return false;
            } else if (isSnoozedPackage(expandedNotification)) {
                Log.d("StatusBar", "No peeking: snoozed package: " + expandedNotification.getKey());
                return false;
            } else if (entry.notification.getImportance() < 4 && !InCallUtils.isInCallNotification(expandedNotification)) {
                Log.d("StatusBar", "No peeking: unimportant notification: " + expandedNotification.getKey());
                return false;
            } else if (this.mExpandedVisible && !this.mHeadsUpManager.hasPinnedHeadsUp() && !this.mHeadsUpManager.isHeadsUpGoingAway()) {
                Log.d("StatusBar", "No peeking: status bar expanded: " + expandedNotification.getKey());
                return false;
            } else if (!panelsEnabled()) {
                Log.d("StatusBar", "No peeking: disabled panel : " + expandedNotification.getKey());
                return false;
            } else {
                if (expandedNotification.getNotification().fullScreenIntent != null) {
                    if (this.mAccessibilityManager.isTouchExplorationEnabled()) {
                        Log.d("StatusBar", "No peeking: accessible fullscreen: " + expandedNotification.getKey());
                        return false;
                    } else if (InCallUtils.isInCallNotification(expandedNotification)) {
                        if (Settings.Global.getInt(this.mContext.getContentResolver(), "com.xiaomi.system.devicelock.locked", 0) != 0) {
                            Log.d("StatusBar", "No peeking: device locked: " + expandedNotification.getKey());
                            return false;
                        }
                        boolean z = !this.mStatusBarKeyguardViewManager.isShowing();
                        Log.d("StatusBar", "in call notification should peek: " + z);
                        return z;
                    }
                }
                if (StatusBarNotificationCompat.isGroup(expandedNotification) && NotificationCompat.suppressAlertingDueToGrouping(expandedNotification.getNotification())) {
                    Log.d("StatusBar", "No peeking: suppressed due to group alert behavior: " + expandedNotification.getKey());
                    return false;
                } else if (!StatusBarNotificationCompat.isAutoGroupSummary(expandedNotification)) {
                    return enableFloatNotification(expandedNotification);
                } else {
                    Log.d("StatusBar", "No peeking: auto group summary: " + expandedNotification.getKey());
                    return false;
                }
            }
        }
    }

    private boolean enableFloatNotification(ExpandedNotification expandedNotification) {
        if (!isDeviceProvisioned() || isKeyguardShowing() || ((InCallUtils.isInCallNotificationHeadsUp(this.mHeadsUpManager.getTopEntry()) && !InCallUtils.isInCallNotification(expandedNotification)) || ((InCallUtils.isInCallScreenShowing(this.mContext) && !InCallUtils.isInCallNotificationHasVideoCall(expandedNotification)) || isLowStorageMode() || isVrMode()))) {
            Log.d("StatusBar", "No peeking: miui smart intercept: " + expandedNotification.getKey());
            return false;
        }
        boolean z = MiuiSettings.SilenceMode.isSupported ? this.mShouldPopup : this.mQuietModeEnable;
        if (expandedNotification.getNotification().fullScreenIntent != null) {
            if (Constants.IS_INTERNATIONAL) {
                if (!this.mStatusBarKeyguardViewManager.isShowing() || this.mStatusBarKeyguardViewManager.isOccluded()) {
                    return true;
                }
                return false;
            } else if (this.mIsStatusBarHidden || this.mSoftInputVisible || this.mDisableFloatNotification || z || ((NotificationSettingsManager) Dependency.get(NotificationSettingsManager.class)).shouldPeekWhenAppShowing(Util.getTopActivityPkg(this.mContext, true))) {
                if (!expandedNotification.isClearable()) {
                    MiuiNotificationCompat.setFloatTime(expandedNotification.getNotification(), Integer.MAX_VALUE);
                }
                Log.d("StatusBar", "peeking: miui smart suspension: " + expandedNotification.getKey());
                return true;
            } else {
                Log.d("StatusBar", "No peeking: has fullscreen intent: " + expandedNotification.getKey());
                return false;
            }
        } else if (this.mDisableFloatNotification) {
            Log.d("StatusBar", "No peeking: disable float notification: " + expandedNotification.getKey());
            return false;
        } else if (this.mMirrorDndEnable) {
            Log.d("StatusBar", "No peeking: mirror dnd mode");
            return false;
        } else if (InCallUtils.isInCallScreenShowing(this.mContext) && InCallUtils.isInCallNotificationHasVideoCall(expandedNotification)) {
            Log.d("StatusBar", "peeking: video in call notification: " + expandedNotification.getKey());
            return true;
        } else if (expandedNotification.canFloat() && !NotificationUtil.hasProgressbar(expandedNotification) && (!z || expandedNotification.isFloatWhenDnd())) {
            Log.d("StatusBar", "peeking: miui permission allows: " + expandedNotification.getKey());
            return true;
        } else if (!this.mQuietModeEnable || !InCallUtils.isInCallNotification(expandedNotification) || getCallState() != 1) {
            Log.d("StatusBar", "No peeking: " + expandedNotification.getKey());
            return false;
        } else {
            Log.d("StatusBar", "peeking: in call notification: " + expandedNotification.getKey());
            return true;
        }
    }

    public boolean shouldSuppressPeek(String str) {
        return this.mNotificationData.shouldSuppressScreenOn(str);
    }

    private boolean isVrMode() {
        return 1 == Settings.System.getInt(this.mContext.getContentResolver(), "vr_mode", 0);
    }

    private boolean isLowStorageMode() {
        return SystemProperties.getBoolean("sys.is_mem_low", false);
    }

    public boolean isBouncerShowing() {
        return this.mBouncerShowing;
    }

    public void logNotificationExpansion(final String str, final boolean z, final boolean z2) {
        NotificationData.Entry entry = this.mNotificationData.get(str);
        if (!(entry == null || entry.notification == null)) {
            ((NotificationStat) Dependency.get(NotificationStat.class)).onExpansionChanged(entry.notification, z, z2);
        }
        this.mUiOffloadThread.submit(new Runnable() {
            public void run() {
                try {
                    StatusBarServiceCompat.onNotificationExpansionChanged(StatusBar.this.mBarService, str, z, z2, -1);
                } catch (RemoteException unused) {
                }
            }
        });
    }

    public boolean isKeyguardSecure() {
        StatusBarKeyguardViewManager statusBarKeyguardViewManager = this.mStatusBarKeyguardViewManager;
        if (statusBarKeyguardViewManager != null) {
            return statusBarKeyguardViewManager.isSecure();
        }
        Slog.w("StatusBar", "isKeyguardSecure() called before startKeyguard(), returning false", new Throwable());
        return false;
    }

    public void showAssistDisclosure() {
        AssistManager assistManager = this.mAssistManager;
        if (assistManager != null) {
            assistManager.showDisclosure();
        }
    }

    public void startAssist(Bundle bundle) {
        if (this.mAssistManager != null && (getFlagDisable1() & 33554432) == 0) {
            this.mAssistManager.startAssist(bundle);
        }
    }

    public void onInCallNotificationShow() {
        if (this.mPhoneStateListener == null) {
            this.mPhoneStateListener = new PhoneStateListener() {
                public void onCallStateChanged(int i, String str) {
                    StatusBar.this.onCallStateChanged(i);
                }
            };
            this.mTelephonyManager.listen(this.mPhoneStateListener, 32);
        }
        if (this.mVoipPhoneStateReceiver == null) {
            this.mVoipPhoneStateReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    StatusBar.this.onCallStateChanged(intent.getIntExtra("state", 0));
                }
            };
            this.mContext.registerReceiverAsUser(this.mVoipPhoneStateReceiver, UserHandle.ALL, new IntentFilter("com.miui.voip.action.CALL_STATE_CHANGED"), (String) null, (Handler) null);
        }
        Settings.System.putIntForUser(this.mContext.getContentResolver(), "status_bar_in_call_notification_floating", 1, -2);
    }

    /* access modifiers changed from: private */
    public void onCallStateChanged(int i) {
        if (i == 0) {
            PhoneStateListener phoneStateListener = this.mPhoneStateListener;
            if (phoneStateListener != null) {
                this.mTelephonyManager.listen(phoneStateListener, 0);
                this.mPhoneStateListener = null;
            }
            BroadcastReceiver broadcastReceiver = this.mVoipPhoneStateReceiver;
            if (broadcastReceiver != null) {
                this.mContext.unregisterReceiver(broadcastReceiver);
                this.mVoipPhoneStateReceiver = null;
            }
            this.mHasAnswerCall = false;
            this.mHasBubbleAnswerCall = false;
        }
        if (i != 2) {
            return;
        }
        if (this.mGameHandsFreeMode || this.mHasBubbleAnswerCall) {
            this.mHeadsUpManager.removeHeadsUpNotification();
        } else if (!this.mHasAnswerCall) {
            onExitCall();
        }
    }

    /* access modifiers changed from: private */
    public int getCallState() {
        return ((CallStateController) Dependency.get(CallStateController.class)).getCallState();
    }

    public void onInCallNotificationHide() {
        Settings.System.putIntForUser(this.mContext.getContentResolver(), "status_bar_in_call_notification_floating", 0, KeyguardUpdateMonitor.getCurrentUser());
    }

    public void onAnswerCall() {
        Log.d("StatusBar", "on answer call");
        this.mHasAnswerCall = true;
        if (getCallState() != 0) {
            TelephonyManagerEx.getDefault().answerRingingCall();
        }
        Bundle bundle = new Bundle();
        bundle.putBoolean("systemUI.answer", true);
        if (!this.mGameHandsFreeMode) {
            InCallUtils.goInCallScreen(this.mContext, bundle);
        }
        this.mHeadsUpManager.removeHeadsUpNotification();
    }

    public void onBubbleAnswerCall() {
        Log.d("StatusBar", "on bubble answer");
        this.mHasBubbleAnswerCall = true;
        this.mHeadsUpManager.removeHeadsUpNotification();
    }

    public void onEndCall() {
        Log.d("StatusBar", "on end call");
        if (getCallState() != 0) {
            TelephonyManagerEx.getDefault().endCall();
        }
        this.mHeadsUpManager.removeHeadsUpNotification();
    }

    public void onExitCall() {
        Log.d("StatusBar", "on exit call");
        InCallUtils.goInCallScreen(this.mContext);
        this.mHeadsUpManager.removeHeadsUpNotification();
    }

    public void showReturnToInCallScreenButtonIfNeed() {
        if (!TelephonyManager.isGoogleCsp() && InCallUtils.isInCallNotificationHeadsUp(this.mHeadsUpManager.getTopEntry()) && 1 == getCallState()) {
            showReturnToInCallScreenButton(Call.State.INCOMING.toString(), 0);
            TelephonyManagerEx.getDefault().silenceRinger();
        }
    }

    public void showReturnToInCallScreenButton(String str, long j) {
        Log.d("StatusBar", "show return to in call screen button");
        this.mMiuiStatusBarPrompt.showReturnToInCallScreenButton(str, j);
    }

    public void hideReturnToInCallScreenButton() {
        Log.d("StatusBar", "hide return to in call screen button");
        this.mMiuiStatusBarPrompt.hideReturnToInCallScreenButton();
    }

    /* access modifiers changed from: package-private */
    public void resumeSuspendedNavBarAutohide() {
        resumeSuspendedAutohide();
    }

    /* access modifiers changed from: package-private */
    public void suspendNavBarAutohide() {
        suspendAutohide();
    }

    /* access modifiers changed from: private */
    public void updateNotificationIconsLayout() {
        int i = 0;
        boolean z = true;
        boolean z2 = this.mState == 1;
        boolean isShowingState = this.mMiuiStatusBarPrompt.isShowingState("legacy_drive");
        if (!this.mShowNotifications || ((isShowingState && !z2) || this.mDemoMode)) {
            z = false;
        }
        View view = this.mNotifications;
        if (view != null) {
            if (!z) {
                i = 4;
            }
            view.setVisibility(i);
            if (isNoIconsSetGone() && this.mNotificationIconAreaController.getNotificationIconsVisibility() == 8) {
                this.mNotifications.setVisibility(8);
            }
        }
    }

    private boolean isNoIconsSetGone() {
        return this.mNoIconsSetGone && ((StatusBarTypeController) Dependency.get(StatusBarTypeController.class)).hasCutout();
    }

    private boolean hideAmPmForNotification() {
        return this.mHideAmPmForNotification && ((StatusBarTypeController) Dependency.get(StatusBarTypeController.class)).hasCutout();
    }

    /* access modifiers changed from: private */
    public void updateDriveMode() {
        boolean z = false;
        boolean z2 = this.mState == 1;
        boolean equals = "legacy_drive".equals(this.mMiuiStatusBarPrompt.calculateTopTag());
        updateNotificationIconsLayout();
        LinearLayout linearLayout = this.mDriveModeBg;
        if (linearLayout != null) {
            linearLayout.setVisibility((((StatusBarTypeController) Dependency.get(StatusBarTypeController.class)).hasCutout() || !equals || z2) ? 8 : 0);
        }
        this.mLightBarController.setDriveMode(!((StatusBarTypeController) Dependency.get(StatusBarTypeController.class)).hasCutout() && !z2 && equals);
        CollapsedStatusBarFragment collapsedStatusBarFragment = this.mStatusBarFragment;
        if (collapsedStatusBarFragment != null) {
            collapsedStatusBarFragment.updateInDriveMode(!z2 && equals);
        }
        MiuiStatusBarPromptController miuiStatusBarPromptController = this.mMiuiStatusBarPrompt;
        boolean z3 = !z2 && equals;
        if (!z2 && this.mIsInDriveModeMask) {
            z = true;
        }
        miuiStatusBarPromptController.showReturnToDriveModeView(z3, z);
    }

    public void showRecentApps(boolean z, boolean z2) {
        this.mRecents.showRecentApps(z, z2);
    }

    public boolean shouldHideNotificationIcons() {
        return this.mMiuiStatusBarPrompt.isShowingState("legacy_multi");
    }

    public void setStatus(int i, String str, Bundle bundle) {
        this.mMiuiStatusBarPrompt.setStatus(i, str, bundle);
    }

    public void refreshClockVisibility(boolean z) {
        CollapsedStatusBarFragment collapsedStatusBarFragment = this.mStatusBarFragment;
        if (collapsedStatusBarFragment != null) {
            collapsedStatusBarFragment.refreshClockVisibility(false, z, false, true);
        }
    }

    private void dynamicFPSAccommodation(NotificationData.Entry entry) {
        boolean z = false;
        if ((((ScreenLifecycle) Dependency.get(ScreenLifecycle.class)).isScreenOn() && !SystemServicesProxy.getInstance(this.mContext).isDreaming()) && entry.notification.getImportance() >= 3 && (this.mExpandedVisible || (isKeyguardShowing() && shouldShowOnKeyguard(entry)))) {
            z = true;
        }
        if (z) {
            DisplayFeatureManager.getInstance().setScreenEffect(24, 255, 256);
        }
    }
}
