package com.android.systemui.statusbar.phone;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.ActivityManager;
import android.app.ActivityTaskManager;
import android.app.IActivityTaskManager;
import android.app.StatusBarManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.display.DisplayManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.os.UserHandle;
import android.provider.DeviceConfig;
import android.provider.Settings;
import android.telecom.TelecomManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.InsetsState;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityManager;
import com.android.internal.accessibility.dialog.AccessibilityButtonChooserActivity;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.UiEventLogger;
import com.android.internal.util.LatencyTracker;
import com.android.internal.view.AppearanceRegion;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0017R$layout;
import com.android.systemui.C0021R$string;
import com.android.systemui.accessibility.SystemActions;
import com.android.systemui.assist.AssistHandleViewController;
import com.android.systemui.assist.AssistManager;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.fragments.FragmentHostManager;
import com.android.systemui.model.SysUiState;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.recents.OverviewProxyService;
import com.android.systemui.recents.Recents;
import com.android.systemui.shared.system.ActivityManagerWrapper;
import com.android.systemui.shared.system.QuickStepContract;
import com.android.systemui.stackdivider.Divider;
import com.android.systemui.statusbar.AutoHideUiElement;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.NotificationRemoteInputManager;
import com.android.systemui.statusbar.phone.ContextualButton;
import com.android.systemui.statusbar.phone.NavigationBarTransitions;
import com.android.systemui.statusbar.phone.NavigationBarView;
import com.android.systemui.statusbar.phone.NavigationModeController;
import com.android.systemui.statusbar.policy.AccessibilityManagerWrapper;
import com.android.systemui.statusbar.policy.DeviceProvisionedController;
import com.android.systemui.statusbar.policy.KeyButtonView;
import com.android.systemui.util.LifecycleFragment;
import dagger.Lazy;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;

public class NavigationBarFragment extends LifecycleFragment implements CommandQueue.Callbacks, NavigationModeController.ModeChangedListener, DisplayManager.DisplayListener {
    private final AccessibilityManager.AccessibilityServicesStateChangeListener mAccessibilityListener;
    private AccessibilityManager mAccessibilityManager;
    private final AccessibilityManagerWrapper mAccessibilityManagerWrapper;
    private int mAppearance;
    private final ContentObserver mAssistContentObserver;
    private AssistHandleViewController mAssistHandlerViewController;
    protected final AssistManager mAssistManager;
    private boolean mAssistantAvailable;
    private final Runnable mAutoDim;
    private AutoHideController mAutoHideController;
    private final AutoHideUiElement mAutoHideUiElement;
    private final BroadcastDispatcher mBroadcastDispatcher;
    private final BroadcastReceiver mBroadcastReceiver;
    private final CommandQueue mCommandQueue;
    private ContentResolver mContentResolver;
    private int mCurrentRotation;
    private final DeviceProvisionedController mDeviceProvisionedController;
    private int mDisabledFlags1;
    private int mDisabledFlags2;
    public int mDisplayId;
    private final Divider mDivider;
    private boolean mForceNavBarHandleOpaque;
    private final Handler mHandler;
    public boolean mHomeBlockedThisTouch;
    private boolean mIsOnDefaultDisplay;
    private final KeyOrderObserver mKeyOrderObserver;
    private long mLastLockToAppLongPress;
    private int mLayoutDirection;
    private LightBarController mLightBarController;
    private Locale mLocale;
    private final MetricsLogger mMetricsLogger;
    private int mNavBarMode;
    private int mNavigationBarMode;
    protected NavigationBarView mNavigationBarView = null;
    private int mNavigationBarWindowState;
    private int mNavigationIconHints;
    private final NavigationModeController mNavigationModeController;
    private final NotificationRemoteInputManager mNotificationRemoteInputManager;
    private final DeviceConfig.OnPropertiesChangedListener mOnPropertiesChangedListener;
    private QuickswitchOrientedNavHandle mOrientationHandle;
    private ViewTreeObserver.OnGlobalLayoutListener mOrientationHandleGlobalLayoutListener;
    private NavigationBarTransitions.DarkIntensityListener mOrientationHandleIntensityListener;
    private WindowManager.LayoutParams mOrientationParams;
    private final OverviewProxyService.OverviewProxyListener mOverviewProxyListener;
    private OverviewProxyService mOverviewProxyService;
    private final Optional<Recents> mRecentsOptional;
    private final ContextualButton.ContextButtonListener mRotationButtonListener;
    private final Consumer<Integer> mRotationWatcher;
    private boolean mShowOrientedHandleForImmersiveMode;
    private int mStartingQuickSwitchRotation;
    private final Lazy<StatusBar> mStatusBarLazy;
    private final StatusBarStateController mStatusBarStateController;
    private SysUiState mSysUiFlagsContainer;
    private final SystemActions mSystemActions;
    private boolean mTransientShown;
    private UiEventLogger mUiEventLogger;
    private WindowManager mWindowManager;

    private static int barMode(boolean z, int i) {
        if (z) {
            return 1;
        }
        if ((i & 6) == 6) {
            return 3;
        }
        if ((i & 4) != 0) {
            return 6;
        }
        return (i & 2) != 0 ? 4 : 0;
    }

    private int deltaRotation(int i, int i2) {
        int i3 = i2 - i;
        return i3 < 0 ? i3 + 4 : i3;
    }

    public void onDisplayAdded(int i) {
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void onDisplayRemoved(int i) {
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void setImeWindowStatus(int i, IBinder iBinder, int i2, int i3, boolean z) {
    }

    @VisibleForTesting
    public enum NavBarActionEvent implements UiEventLogger.UiEventEnum {
        NAVBAR_ASSIST_LONGPRESS(550);
        
        private final int mId;

        private NavBarActionEvent(int i) {
            this.mId = i;
        }

        public int getId() {
            return this.mId;
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$0 */
    public /* synthetic */ void lambda$new$0$NavigationBarFragment(ContextualButton contextualButton, boolean z) {
        if (z) {
            this.mAutoHideController.touchAutoHide();
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$1 */
    public /* synthetic */ void lambda$new$1$NavigationBarFragment() {
        getBarTransitions().setAutoDim(true);
    }

    public NavigationBarFragment(AccessibilityManagerWrapper accessibilityManagerWrapper, DeviceProvisionedController deviceProvisionedController, MetricsLogger metricsLogger, AssistManager assistManager, OverviewProxyService overviewProxyService, NavigationModeController navigationModeController, StatusBarStateController statusBarStateController, SysUiState sysUiState, BroadcastDispatcher broadcastDispatcher, CommandQueue commandQueue, Divider divider, Optional<Recents> optional, Lazy<StatusBar> lazy, ShadeController shadeController, NotificationRemoteInputManager notificationRemoteInputManager, SystemActions systemActions, Handler handler, UiEventLogger uiEventLogger) {
        boolean z = false;
        this.mNavigationBarWindowState = 0;
        this.mNavigationIconHints = 0;
        this.mNavBarMode = 0;
        this.mKeyOrderObserver = new KeyOrderObserver();
        this.mStartingQuickSwitchRotation = -1;
        this.mAutoHideUiElement = new AutoHideUiElement() {
            /* class com.android.systemui.statusbar.phone.NavigationBarFragment.AnonymousClass1 */

            @Override // com.android.systemui.statusbar.AutoHideUiElement
            public void synchronizeState() {
                NavigationBarFragment.this.checkNavBarModes();
            }

            @Override // com.android.systemui.statusbar.AutoHideUiElement
            public boolean shouldHideOnTouch() {
                return !NavigationBarFragment.this.mNotificationRemoteInputManager.getController().isRemoteInputActive();
            }

            @Override // com.android.systemui.statusbar.AutoHideUiElement
            public boolean isVisible() {
                return NavigationBarFragment.this.isTransientShown();
            }

            @Override // com.android.systemui.statusbar.AutoHideUiElement
            public void hide() {
                NavigationBarFragment.this.clearTransient();
            }
        };
        this.mOverviewProxyListener = new OverviewProxyService.OverviewProxyListener() {
            /* class com.android.systemui.statusbar.phone.NavigationBarFragment.AnonymousClass2 */

            @Override // com.android.systemui.recents.OverviewProxyService.OverviewProxyListener
            public void onConnectionChanged(boolean z) {
                NavigationBarFragment.this.mNavigationBarView.updateStates();
                NavigationBarFragment.this.updateScreenPinningGestures();
                if (z) {
                    NavigationBarFragment navigationBarFragment = NavigationBarFragment.this;
                    navigationBarFragment.sendAssistantAvailability(navigationBarFragment.mAssistantAvailable);
                }
            }

            @Override // com.android.systemui.recents.OverviewProxyService.OverviewProxyListener
            public void onQuickSwitchToNewTask(int i) {
                NavigationBarFragment.this.mStartingQuickSwitchRotation = i;
                if (i == -1) {
                    NavigationBarFragment.this.mShowOrientedHandleForImmersiveMode = false;
                }
                NavigationBarFragment.this.orientSecondaryHomeHandle();
            }

            @Override // com.android.systemui.recents.OverviewProxyService.OverviewProxyListener
            public void startAssistant(Bundle bundle) {
                NavigationBarFragment.this.mAssistManager.startAssist(bundle);
            }

            /* JADX WARNING: Removed duplicated region for block: B:18:? A[RETURN, SYNTHETIC] */
            /* JADX WARNING: Removed duplicated region for block: B:9:0x0035 A[ADDED_TO_REGION] */
            @Override // com.android.systemui.recents.OverviewProxyService.OverviewProxyListener
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void onNavBarButtonAlphaChanged(float r4, boolean r5) {
                /*
                    r3 = this;
                    com.android.systemui.statusbar.phone.NavigationBarFragment r0 = com.android.systemui.statusbar.phone.NavigationBarFragment.this
                    int r0 = com.android.systemui.statusbar.phone.NavigationBarFragment.access$1000(r0)
                    boolean r0 = com.android.systemui.shared.system.QuickStepContract.isSwipeUpMode(r0)
                    r1 = 0
                    if (r0 == 0) goto L_0x0016
                    com.android.systemui.statusbar.phone.NavigationBarFragment r3 = com.android.systemui.statusbar.phone.NavigationBarFragment.this
                    com.android.systemui.statusbar.phone.NavigationBarView r3 = r3.mNavigationBarView
                    com.android.systemui.statusbar.phone.ButtonDispatcher r3 = r3.getBackButton()
                    goto L_0x0032
                L_0x0016:
                    com.android.systemui.statusbar.phone.NavigationBarFragment r0 = com.android.systemui.statusbar.phone.NavigationBarFragment.this
                    int r0 = com.android.systemui.statusbar.phone.NavigationBarFragment.access$1000(r0)
                    boolean r0 = com.android.systemui.shared.system.QuickStepContract.isGesturalMode(r0)
                    if (r0 == 0) goto L_0x0031
                    com.android.systemui.statusbar.phone.NavigationBarFragment r0 = com.android.systemui.statusbar.phone.NavigationBarFragment.this
                    boolean r0 = com.android.systemui.statusbar.phone.NavigationBarFragment.access$1100(r0)
                    com.android.systemui.statusbar.phone.NavigationBarFragment r3 = com.android.systemui.statusbar.phone.NavigationBarFragment.this
                    com.android.systemui.statusbar.phone.NavigationBarView r3 = r3.mNavigationBarView
                    com.android.systemui.statusbar.phone.ButtonDispatcher r3 = r3.getHomeHandle()
                    goto L_0x0033
                L_0x0031:
                    r3 = 0
                L_0x0032:
                    r0 = r1
                L_0x0033:
                    if (r3 == 0) goto L_0x0048
                    if (r0 != 0) goto L_0x003e
                    r2 = 0
                    int r2 = (r4 > r2 ? 1 : (r4 == r2 ? 0 : -1))
                    if (r2 <= 0) goto L_0x003d
                    goto L_0x003e
                L_0x003d:
                    r1 = 4
                L_0x003e:
                    r3.setVisibility(r1)
                    if (r0 == 0) goto L_0x0045
                    r4 = 1065353216(0x3f800000, float:1.0)
                L_0x0045:
                    r3.setAlpha(r4, r5)
                L_0x0048:
                    return
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.phone.NavigationBarFragment.AnonymousClass2.onNavBarButtonAlphaChanged(float, boolean):void");
            }

            @Override // com.android.systemui.recents.OverviewProxyService.OverviewProxyListener
            public void onOverviewShown(boolean z) {
                NavigationBarFragment.this.mNavigationBarView.getRotationButtonController().setSkipOverrideUserLockPrefsOnce();
            }

            @Override // com.android.systemui.recents.OverviewProxyService.OverviewProxyListener
            public void onToggleRecentApps() {
                NavigationBarFragment.this.mNavigationBarView.getRotationButtonController().setSkipOverrideUserLockPrefsOnce();
            }
        };
        this.mOrientationHandleIntensityListener = new NavigationBarTransitions.DarkIntensityListener() {
            /* class com.android.systemui.statusbar.phone.NavigationBarFragment.AnonymousClass3 */

            @Override // com.android.systemui.statusbar.phone.NavigationBarTransitions.DarkIntensityListener
            public void onDarkIntensity(float f) {
                NavigationBarFragment.this.mOrientationHandle.setDarkIntensity(f);
            }
        };
        this.mRotationButtonListener = new ContextualButton.ContextButtonListener() {
            /* class com.android.systemui.statusbar.phone.$$Lambda$NavigationBarFragment$xnm4oWC06iZWqzBbKv8ubGVFU */

            @Override // com.android.systemui.statusbar.phone.ContextualButton.ContextButtonListener
            public final void onVisibilityChanged(ContextualButton contextualButton, boolean z) {
                NavigationBarFragment.this.lambda$new$0$NavigationBarFragment(contextualButton, z);
            }
        };
        this.mAutoDim = new Runnable() {
            /* class com.android.systemui.statusbar.phone.$$Lambda$NavigationBarFragment$Wf_FUQzkbSdMD9hXKJaXOD_rVSY */

            public final void run() {
                NavigationBarFragment.this.lambda$new$1$NavigationBarFragment();
            }
        };
        this.mAssistContentObserver = new ContentObserver(new Handler(Looper.getMainLooper())) {
            /* class com.android.systemui.statusbar.phone.NavigationBarFragment.AnonymousClass4 */

            public void onChange(boolean z, Uri uri) {
                boolean z2 = NavigationBarFragment.this.mAssistManager.getAssistInfoForUser(-2) != null;
                if (NavigationBarFragment.this.mAssistantAvailable != z2) {
                    NavigationBarFragment.this.sendAssistantAvailability(z2);
                    NavigationBarFragment.this.mAssistantAvailable = z2;
                }
            }
        };
        this.mOnPropertiesChangedListener = new DeviceConfig.OnPropertiesChangedListener() {
            /* class com.android.systemui.statusbar.phone.NavigationBarFragment.AnonymousClass5 */

            public void onPropertiesChanged(DeviceConfig.Properties properties) {
                if (properties.getKeyset().contains("nav_bar_handle_force_opaque")) {
                    NavigationBarFragment.this.mForceNavBarHandleOpaque = properties.getBoolean("nav_bar_handle_force_opaque", true);
                }
            }
        };
        this.mAccessibilityListener = new AccessibilityManager.AccessibilityServicesStateChangeListener() {
            /* class com.android.systemui.statusbar.phone.$$Lambda$NavigationBarFragment$dxES00kAyC8r2RmY9FwTYgUhoj8 */

            public final void onAccessibilityServicesStateChanged(AccessibilityManager accessibilityManager) {
                NavigationBarFragment.this.updateAccessibilityServicesState(accessibilityManager);
            }
        };
        this.mRotationWatcher = new Consumer() {
            /* class com.android.systemui.statusbar.phone.$$Lambda$NavigationBarFragment$ivU7sfkPnx7kuUAYwcAC75X5OE */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                NavigationBarFragment.this.lambda$new$5$NavigationBarFragment((Integer) obj);
            }
        };
        this.mBroadcastReceiver = new BroadcastReceiver() {
            /* class com.android.systemui.statusbar.phone.NavigationBarFragment.AnonymousClass6 */

            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if ("android.intent.action.SCREEN_OFF".equals(action) || "android.intent.action.SCREEN_ON".equals(action)) {
                    NavigationBarFragment.this.notifyNavigationBarScreenOn();
                    NavigationBarFragment.this.mNavigationBarView.onScreenStateChanged("android.intent.action.SCREEN_ON".equals(action));
                }
                if ("android.intent.action.USER_SWITCHED".equals(action)) {
                    NavigationBarFragment navigationBarFragment = NavigationBarFragment.this;
                    navigationBarFragment.updateAccessibilityServicesState(navigationBarFragment.mAccessibilityManager);
                }
            }
        };
        this.mAccessibilityManagerWrapper = accessibilityManagerWrapper;
        this.mDeviceProvisionedController = deviceProvisionedController;
        this.mStatusBarStateController = statusBarStateController;
        this.mMetricsLogger = metricsLogger;
        this.mAssistManager = assistManager;
        this.mSysUiFlagsContainer = sysUiState;
        this.mStatusBarLazy = lazy;
        this.mNotificationRemoteInputManager = notificationRemoteInputManager;
        this.mAssistantAvailable = assistManager.getAssistInfoForUser(-2) != null ? true : z;
        this.mOverviewProxyService = overviewProxyService;
        this.mNavigationModeController = navigationModeController;
        this.mNavBarMode = navigationModeController.addListener(this);
        this.mBroadcastDispatcher = broadcastDispatcher;
        this.mCommandQueue = commandQueue;
        this.mDivider = divider;
        this.mRecentsOptional = optional;
        this.mSystemActions = systemActions;
        this.mHandler = handler;
        this.mUiEventLogger = uiEventLogger;
    }

    @Override // com.android.systemui.util.LifecycleFragment
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.mCommandQueue.observe(getLifecycle(), this);
        this.mWindowManager = (WindowManager) getContext().getSystemService(WindowManager.class);
        this.mAccessibilityManager = (AccessibilityManager) getContext().getSystemService(AccessibilityManager.class);
        ContentResolver contentResolver = getContext().getContentResolver();
        this.mContentResolver = contentResolver;
        contentResolver.registerContentObserver(Settings.Secure.getUriFor("assistant"), false, this.mAssistContentObserver, -1);
        if (bundle != null) {
            this.mDisabledFlags1 = bundle.getInt("disabled_state", 0);
            this.mDisabledFlags2 = bundle.getInt("disabled2_state", 0);
            this.mAppearance = bundle.getInt("appearance", 0);
            this.mTransientShown = bundle.getBoolean("transient_state", false);
        }
        this.mAccessibilityManagerWrapper.addCallback(this.mAccessibilityListener);
        this.mCommandQueue.recomputeDisableFlags(this.mDisplayId, false);
        this.mForceNavBarHandleOpaque = DeviceConfig.getBoolean("systemui", "nav_bar_handle_force_opaque", true);
        Handler handler = this.mHandler;
        Objects.requireNonNull(handler);
        DeviceConfig.addOnPropertiesChangedListener("systemui", new Executor(handler) {
            /* class com.android.systemui.statusbar.phone.$$Lambda$LfzJt661qZfn2w6SYHFbD3aMy0 */
            public final /* synthetic */ Handler f$0;

            {
                this.f$0 = r1;
            }

            public final void execute(Runnable runnable) {
                this.f$0.post(runnable);
            }
        }, this.mOnPropertiesChangedListener);
    }

    @Override // com.android.systemui.util.LifecycleFragment
    public void onDestroy() {
        super.onDestroy();
        this.mNavigationModeController.removeListener(this);
        this.mAccessibilityManagerWrapper.removeCallback(this.mAccessibilityListener);
        this.mContentResolver.unregisterContentObserver(this.mAssistContentObserver);
        DeviceConfig.removeOnPropertiesChangedListener(this.mOnPropertiesChangedListener);
    }

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        return layoutInflater.inflate(C0017R$layout.navigation_bar, viewGroup, false);
    }

    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        this.mNavigationBarView = (NavigationBarView) view;
        Display display = view.getDisplay();
        if (display != null) {
            int displayId = display.getDisplayId();
            this.mDisplayId = displayId;
            this.mIsOnDefaultDisplay = displayId == 0;
        }
        this.mNavigationBarView.setComponents(this.mStatusBarLazy.get().getPanelController());
        this.mNavigationBarView.setDisabledFlags(this.mDisabledFlags1);
        this.mNavigationBarView.setOnVerticalChangedListener(new NavigationBarView.OnVerticalChangedListener() {
            /* class com.android.systemui.statusbar.phone.$$Lambda$NavigationBarFragment$eFJm5m1txtISSi8Cx3m3pc8Nvjw */

            @Override // com.android.systemui.statusbar.phone.NavigationBarView.OnVerticalChangedListener
            public final void onVerticalChanged(boolean z) {
                NavigationBarFragment.this.onVerticalChanged(z);
            }
        });
        this.mNavigationBarView.setOnTouchListener(new View.OnTouchListener() {
            /* class com.android.systemui.statusbar.phone.$$Lambda$NavigationBarFragment$X9JO9eLzlFoQkYf8XrZGl2EMsk */

            public final boolean onTouch(View view, MotionEvent motionEvent) {
                return NavigationBarFragment.this.onNavigationTouch(view, motionEvent);
            }
        });
        if (bundle != null) {
            this.mNavigationBarView.getLightTransitionsController().restoreState(bundle);
        }
        this.mNavigationBarView.setNavigationIconHints(this.mNavigationIconHints);
        this.mNavigationBarView.setWindowVisible(isNavBarWindowVisible());
        prepareNavigationBarView();
        checkNavBarModes();
        IntentFilter intentFilter = new IntentFilter("android.intent.action.SCREEN_OFF");
        intentFilter.addAction("android.intent.action.SCREEN_ON");
        intentFilter.addAction("android.intent.action.USER_SWITCHED");
        this.mBroadcastDispatcher.registerReceiverWithHandler(this.mBroadcastReceiver, intentFilter, Handler.getMain(), UserHandle.ALL);
        notifyNavigationBarScreenOn();
        this.mOverviewProxyService.addCallback(this.mOverviewProxyListener);
        updateSystemUiStateFlags(-1);
        if (this.mIsOnDefaultDisplay) {
            this.mNavigationBarView.getRotateSuggestionButton().setListener(this.mRotationButtonListener);
            RotationButtonController rotationButtonController = this.mNavigationBarView.getRotationButtonController();
            rotationButtonController.addRotationCallback(this.mRotationWatcher);
            if (display != null && rotationButtonController.isRotationLocked()) {
                rotationButtonController.setRotationLockedAtAngle(display.getRotation());
            }
        } else {
            this.mDisabledFlags2 |= 16;
        }
        setDisabled2Flags(this.mDisabledFlags2);
        if (this.mIsOnDefaultDisplay) {
            this.mAssistHandlerViewController = new AssistHandleViewController(this.mHandler, this.mNavigationBarView);
            getBarTransitions().addDarkIntensityListener(this.mAssistHandlerViewController);
        }
        initSecondaryHomeHandleForRotation();
        this.mKeyOrderObserver.register(this.mContentResolver, new Function0() {
            /* class com.android.systemui.statusbar.phone.$$Lambda$S4iNug6JoJ5hSF76R3aOJlWU4g */

            @Override // kotlin.jvm.functions.Function0
            public final Object invoke() {
                return NavigationBarFragment.this.reversetKeyOrder();
            }
        });
    }

    public Unit reversetKeyOrder() {
        NavigationBarView navigationBarView = this.mNavigationBarView;
        if (navigationBarView != null && navigationBarView.getVisibility() == 0) {
            this.mNavigationBarView.reverseOrder();
        }
        return Unit.INSTANCE;
    }

    public void onDestroyView() {
        super.onDestroyView();
        NavigationBarView navigationBarView = this.mNavigationBarView;
        if (navigationBarView != null) {
            if (this.mIsOnDefaultDisplay) {
                navigationBarView.getBarTransitions().removeDarkIntensityListener(this.mAssistHandlerViewController);
                this.mAssistHandlerViewController = null;
            }
            this.mNavigationBarView.getBarTransitions().destroy();
            this.mNavigationBarView.getLightTransitionsController().destroy(getContext());
        }
        this.mOverviewProxyService.removeCallback(this.mOverviewProxyListener);
        this.mBroadcastDispatcher.unregisterReceiver(this.mBroadcastReceiver);
        if (this.mOrientationHandle != null) {
            resetSecondaryHandle();
            ((DisplayManager) getContext().getSystemService(DisplayManager.class)).unregisterDisplayListener(this);
            getBarTransitions().removeDarkIntensityListener(this.mOrientationHandleIntensityListener);
            this.mWindowManager.removeView(this.mOrientationHandle);
            this.mOrientationHandle.getViewTreeObserver().removeOnGlobalLayoutListener(this.mOrientationHandleGlobalLayoutListener);
        }
        this.mKeyOrderObserver.unregister(this.mContentResolver);
        NavigationBarView navigationBarView2 = this.mNavigationBarView;
        if (navigationBarView2 != null) {
            navigationBarView2.onViewDestroyed();
        }
    }

    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putInt("disabled_state", this.mDisabledFlags1);
        bundle.putInt("disabled2_state", this.mDisabledFlags2);
        bundle.putInt("appearance", this.mAppearance);
        bundle.putBoolean("transient_state", this.mTransientShown);
        NavigationBarView navigationBarView = this.mNavigationBarView;
        if (navigationBarView != null) {
            navigationBarView.getLightTransitionsController().saveState(bundle);
        }
    }

    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        if (getContext() != null) {
            Locale locale = getContext().getResources().getConfiguration().locale;
            int layoutDirectionFromLocale = TextUtils.getLayoutDirectionFromLocale(locale);
            if (!locale.equals(this.mLocale) || layoutDirectionFromLocale != this.mLayoutDirection) {
                this.mLocale = locale;
                this.mLayoutDirection = layoutDirectionFromLocale;
                refreshLayout(layoutDirectionFromLocale);
            }
            repositionNavigationBar();
        }
    }

    private void initSecondaryHomeHandleForRotation() {
        if (canShowSecondaryHandle()) {
            ((DisplayManager) getContext().getSystemService(DisplayManager.class)).registerDisplayListener(this, new Handler(Looper.getMainLooper()));
            this.mOrientationHandle = new QuickswitchOrientedNavHandle(getContext());
            getBarTransitions().addDarkIntensityListener(this.mOrientationHandleIntensityListener);
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(0, 0, 2024, 545259816, -3);
            this.mOrientationParams = layoutParams;
            layoutParams.setTitle("SecondaryHomeHandle" + getContext().getDisplayId());
            WindowManager.LayoutParams layoutParams2 = this.mOrientationParams;
            layoutParams2.privateFlags = layoutParams2.privateFlags | 64;
            this.mWindowManager.addView(this.mOrientationHandle, layoutParams2);
            this.mOrientationHandle.setVisibility(8);
            this.mOrientationParams.setFitInsetsTypes(0);
            this.mOrientationHandleGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
                /* class com.android.systemui.statusbar.phone.$$Lambda$NavigationBarFragment$Q9R6TLtnrREWz6y8b5TLw_HN9LI */

                public final void onGlobalLayout() {
                    NavigationBarFragment.this.lambda$initSecondaryHomeHandleForRotation$2$NavigationBarFragment();
                }
            };
            this.mOrientationHandle.getViewTreeObserver().addOnGlobalLayoutListener(this.mOrientationHandleGlobalLayoutListener);
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$initSecondaryHomeHandleForRotation$2 */
    public /* synthetic */ void lambda$initSecondaryHomeHandleForRotation$2$NavigationBarFragment() {
        if (this.mStartingQuickSwitchRotation != -1) {
            RectF computeHomeHandleBounds = this.mOrientationHandle.computeHomeHandleBounds();
            this.mOrientationHandle.mapRectFromViewToScreenCoords(computeHomeHandleBounds, true);
            Rect rect = new Rect();
            computeHomeHandleBounds.roundOut(rect);
            this.mNavigationBarView.setOrientedHandleSamplingRegion(rect);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x008d  */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x0090  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void orientSecondaryHomeHandle() {
        /*
        // Method dump skipped, instructions count: 180
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.phone.NavigationBarFragment.orientSecondaryHomeHandle():void");
    }

    private void resetSecondaryHandle() {
        QuickswitchOrientedNavHandle quickswitchOrientedNavHandle = this.mOrientationHandle;
        if (quickswitchOrientedNavHandle != null) {
            quickswitchOrientedNavHandle.setVisibility(8);
        }
        NavigationBarView navigationBarView = this.mNavigationBarView;
        if (navigationBarView != null) {
            navigationBarView.setVisibility(0);
            this.mNavigationBarView.setOrientedHandleSamplingRegion(null);
        }
    }

    public void dump(String str, FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        if (this.mNavigationBarView != null) {
            printWriter.print("  mNavigationBarWindowState=");
            printWriter.println(StatusBarManager.windowStateToString(this.mNavigationBarWindowState));
            printWriter.print("  mNavigationBarMode=");
            printWriter.println(BarTransitions.modeToString(this.mNavigationBarMode));
            StatusBar.dumpBarTransitions(printWriter, "mNavigationBarView", this.mNavigationBarView.getBarTransitions());
        }
        printWriter.print("  mStartingQuickSwitchRotation=" + this.mStartingQuickSwitchRotation);
        printWriter.print("  mCurrentRotation=" + this.mCurrentRotation);
        printWriter.print("  mNavigationBarView=");
        NavigationBarView navigationBarView = this.mNavigationBarView;
        if (navigationBarView == null) {
            printWriter.println("null");
        } else {
            navigationBarView.dump(fileDescriptor, printWriter, strArr);
        }
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void setWindowState(int i, int i2, int i3) {
        if (i == this.mDisplayId && i2 == 2 && this.mNavigationBarWindowState != i3) {
            this.mNavigationBarWindowState = i3;
            updateSystemUiStateFlags(-1);
            this.mShowOrientedHandleForImmersiveMode = i3 == 2;
            if (!(this.mOrientationHandle == null || this.mStartingQuickSwitchRotation == -1)) {
                orientSecondaryHomeHandle();
            }
            NavigationBarView navigationBarView = this.mNavigationBarView;
            if (navigationBarView != null) {
                navigationBarView.setWindowVisible(isNavBarWindowVisible());
            }
        }
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void onRotationProposal(int i, boolean z) {
        int rotation = this.mNavigationBarView.getDisplay().getRotation();
        boolean hasDisable2RotateSuggestionFlag = RotationButtonController.hasDisable2RotateSuggestionFlag(this.mDisabledFlags2);
        RotationButtonController rotationButtonController = this.mNavigationBarView.getRotationButtonController();
        rotationButtonController.getRotationButton();
        if (!hasDisable2RotateSuggestionFlag) {
            rotationButtonController.onRotationProposal(i, rotation, z);
        }
    }

    public void restoreAppearanceAndTransientState() {
        int barMode = barMode(this.mTransientShown, this.mAppearance);
        this.mNavigationBarMode = barMode;
        checkNavBarModes();
        this.mAutoHideController.touchAutoHide();
        this.mLightBarController.onNavigationBarAppearanceChanged(this.mAppearance, true, barMode, false);
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void onSystemBarAppearanceChanged(int i, int i2, AppearanceRegion[] appearanceRegionArr, boolean z) {
        if (i == this.mDisplayId) {
            boolean z2 = false;
            if (this.mAppearance != i2) {
                this.mAppearance = i2;
                if (getView() != null) {
                    z2 = updateBarMode(barMode(this.mTransientShown, i2));
                } else {
                    return;
                }
            }
            this.mLightBarController.onNavigationBarAppearanceChanged(i2, z2, this.mNavigationBarMode, z);
        }
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void showTransient(int i, int[] iArr) {
        if (i == this.mDisplayId && InsetsState.containsType(iArr, 1) && !this.mTransientShown) {
            this.mTransientShown = true;
            handleTransientChanged();
        }
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void abortTransient(int i, int[] iArr) {
        if (i == this.mDisplayId && InsetsState.containsType(iArr, 1)) {
            clearTransient();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void clearTransient() {
        if (this.mTransientShown) {
            this.mTransientShown = false;
            handleTransientChanged();
        }
    }

    private void handleTransientChanged() {
        if (getView() != null) {
            NavigationBarView navigationBarView = this.mNavigationBarView;
            if (navigationBarView != null) {
                navigationBarView.onTransientStateChanged(this.mTransientShown);
            }
            int barMode = barMode(this.mTransientShown, this.mAppearance);
            if (updateBarMode(barMode)) {
                this.mLightBarController.onNavigationBarModeChanged(barMode);
            }
        }
    }

    private boolean updateBarMode(int i) {
        int i2 = this.mNavigationBarMode;
        if (i2 == i) {
            return false;
        }
        if (i2 == 0 || i2 == 6) {
            this.mNavigationBarView.hideRecentsOnboarding();
        }
        this.mNavigationBarMode = i;
        checkNavBarModes();
        this.mAutoHideController.touchAutoHide();
        return true;
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void disable(int i, int i2, int i3, boolean z) {
        int i4;
        if (i == this.mDisplayId) {
            this.mLightBarController.disable(i2);
            int i5 = 56623104 & i2;
            if (i5 != this.mDisabledFlags1) {
                this.mDisabledFlags1 = i5;
                NavigationBarView navigationBarView = this.mNavigationBarView;
                if (navigationBarView != null) {
                    navigationBarView.setDisabledFlags(i2);
                }
                updateScreenPinningGestures();
            }
            if (this.mIsOnDefaultDisplay && (i4 = i3 & 16) != this.mDisabledFlags2) {
                this.mDisabledFlags2 = i4;
                setDisabled2Flags(i4);
            }
        }
    }

    private void setDisabled2Flags(int i) {
        NavigationBarView navigationBarView = this.mNavigationBarView;
        if (navigationBarView != null) {
            navigationBarView.getRotationButtonController().onDisable2FlagChanged(i);
        }
    }

    private void refreshLayout(int i) {
        NavigationBarView navigationBarView = this.mNavigationBarView;
        if (navigationBarView != null) {
            navigationBarView.setLayoutDirection(i);
        }
    }

    private boolean shouldDisableNavbarGestures() {
        return !this.mDeviceProvisionedController.isDeviceProvisioned() || (this.mDisabledFlags1 & 33554432) != 0;
    }

    private void repositionNavigationBar() {
        NavigationBarView navigationBarView = this.mNavigationBarView;
        if (navigationBarView != null && navigationBarView.isAttachedToWindow()) {
            prepareNavigationBarView();
            this.mWindowManager.updateViewLayout((View) this.mNavigationBarView.getParent(), ((View) this.mNavigationBarView.getParent()).getLayoutParams());
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateScreenPinningGestures() {
        NavigationBarView navigationBarView = this.mNavigationBarView;
        if (navigationBarView != null) {
            boolean isRecentsButtonVisible = navigationBarView.isRecentsButtonVisible();
            ButtonDispatcher backButton = this.mNavigationBarView.getBackButton();
            if (isRecentsButtonVisible) {
                backButton.setOnLongClickListener(new View.OnLongClickListener() {
                    /* class com.android.systemui.statusbar.phone.$$Lambda$NavigationBarFragment$dtGeJfWz2E4_XAoQgX8peIw4kU8 */

                    public final boolean onLongClick(View view) {
                        return NavigationBarFragment.this.onLongPressBackRecents(view);
                    }
                });
            } else {
                backButton.setOnLongClickListener(new View.OnLongClickListener() {
                    /* class com.android.systemui.statusbar.phone.$$Lambda$NavigationBarFragment$oZtQ9jE1OTI8AtitIxsN6ETT4sc */

                    public final boolean onLongClick(View view) {
                        return NavigationBarFragment.this.onLongPressBackHome(view);
                    }
                });
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyNavigationBarScreenOn() {
        this.mNavigationBarView.updateNavButtonIcons();
    }

    private void prepareNavigationBarView() {
        this.mNavigationBarView.reorient();
        ButtonDispatcher recentsButton = this.mNavigationBarView.getRecentsButton();
        recentsButton.setOnClickListener(new View.OnClickListener() {
            /* class com.android.systemui.statusbar.phone.$$Lambda$NavigationBarFragment$0mmLLxBq7RxotphHQB_RtYb4SpQ */

            public final void onClick(View view) {
                NavigationBarFragment.this.onRecentsClick(view);
            }
        });
        recentsButton.setOnTouchListener(new View.OnTouchListener() {
            /* class com.android.systemui.statusbar.phone.$$Lambda$NavigationBarFragment$VEqqEZFjg0f3lWOW2BJ66Oo_2aE */

            public final boolean onTouch(View view, MotionEvent motionEvent) {
                return NavigationBarFragment.this.onRecentsTouch(view, motionEvent);
            }
        });
        recentsButton.setLongClickable(true);
        recentsButton.setOnLongClickListener(new View.OnLongClickListener() {
            /* class com.android.systemui.statusbar.phone.$$Lambda$NavigationBarFragment$dtGeJfWz2E4_XAoQgX8peIw4kU8 */

            public final boolean onLongClick(View view) {
                return NavigationBarFragment.this.onLongPressBackRecents(view);
            }
        });
        this.mNavigationBarView.getBackButton().setLongClickable(true);
        this.mNavigationBarView.getHomeButton().setOnTouchListener(new View.OnTouchListener() {
            /* class com.android.systemui.statusbar.phone.$$Lambda$NavigationBarFragment$y_1OHmWTpLl8uCcO3A0Am620g94 */

            public final boolean onTouch(View view, MotionEvent motionEvent) {
                return NavigationBarFragment.this.onHomeTouch(view, motionEvent);
            }
        });
        ButtonDispatcher accessibilityButton = this.mNavigationBarView.getAccessibilityButton();
        accessibilityButton.setOnClickListener(new View.OnClickListener() {
            /* class com.android.systemui.statusbar.phone.$$Lambda$NavigationBarFragment$Ylizyb5K7ZQr77j1Ehc8SUjcI6E */

            public final void onClick(View view) {
                NavigationBarFragment.this.onAccessibilityClick(view);
            }
        });
        accessibilityButton.setOnLongClickListener(new View.OnLongClickListener() {
            /* class com.android.systemui.statusbar.phone.$$Lambda$NavigationBarFragment$RtBTLxltRKo37YrTKiaCXCxwRDg */

            public final boolean onLongClick(View view) {
                return NavigationBarFragment.this.onAccessibilityLongClick(view);
            }
        });
        updateAccessibilityServicesState(this.mAccessibilityManager);
        updateScreenPinningGestures();
    }

    /* access modifiers changed from: private */
    public boolean onHomeTouch(View view, MotionEvent motionEvent) {
        if (this.mHomeBlockedThisTouch && motionEvent.getActionMasked() != 0) {
            return true;
        }
        int action = motionEvent.getAction();
        if (action == 0) {
            this.mHomeBlockedThisTouch = false;
            TelecomManager telecomManager = (TelecomManager) getContext().getSystemService(TelecomManager.class);
            if (telecomManager != null && telecomManager.isRinging() && this.mStatusBarLazy.get().isKeyguardShowing()) {
                Log.i("NavigationBar", "Ignoring HOME; there's a ringing incoming call. No heads up");
                this.mHomeBlockedThisTouch = true;
                return true;
            }
        } else if (action == 1 || action == 3) {
            this.mStatusBarLazy.get().awakenDreams();
        }
        return false;
    }

    /* access modifiers changed from: private */
    public void onVerticalChanged(boolean z) {
        this.mStatusBarLazy.get().setQsScrimEnabled(!z);
    }

    /* access modifiers changed from: private */
    public boolean onNavigationTouch(View view, MotionEvent motionEvent) {
        this.mAutoHideController.checkUserAutoHide(motionEvent);
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean onHomeLongClick(View view) {
        if (!this.mNavigationBarView.isRecentsButtonVisible() && ActivityManagerWrapper.getInstance().isScreenPinningActive()) {
            return onLongPressBackHome(view);
        }
        if (shouldDisableNavbarGestures()) {
            return false;
        }
        this.mMetricsLogger.action(239);
        this.mUiEventLogger.log(NavBarActionEvent.NAVBAR_ASSIST_LONGPRESS);
        Bundle bundle = new Bundle();
        bundle.putInt("invocation_type", 5);
        this.mAssistManager.startAssist(bundle);
        this.mStatusBarLazy.get().awakenDreams();
        NavigationBarView navigationBarView = this.mNavigationBarView;
        if (navigationBarView == null) {
            return true;
        }
        navigationBarView.abortCurrentGesture();
        return true;
    }

    /* access modifiers changed from: private */
    public boolean onRecentsTouch(View view, MotionEvent motionEvent) {
        int action = motionEvent.getAction() & 255;
        if (action == 0) {
            this.mCommandQueue.preloadRecentApps();
            return false;
        } else if (action == 3) {
            this.mCommandQueue.cancelPreloadRecentApps();
            return false;
        } else if (action != 1 || view.isPressed()) {
            return false;
        } else {
            this.mCommandQueue.cancelPreloadRecentApps();
            return false;
        }
    }

    /* access modifiers changed from: private */
    public void onRecentsClick(View view) {
        if (LatencyTracker.isEnabled(getContext())) {
            LatencyTracker.getInstance(getContext()).onActionStart(1);
        }
        this.mStatusBarLazy.get().awakenDreams();
        this.mCommandQueue.toggleRecentApps();
    }

    /* access modifiers changed from: private */
    public boolean onLongPressBackHome(View view) {
        return onLongPressNavigationButtons(view, C0015R$id.back, C0015R$id.home);
    }

    /* access modifiers changed from: private */
    public boolean onLongPressBackRecents(View view) {
        return onLongPressNavigationButtons(view, C0015R$id.back, C0015R$id.recent_apps);
    }

    private boolean onLongPressNavigationButtons(View view, int i, int i2) {
        boolean z;
        ButtonDispatcher buttonDispatcher;
        try {
            IActivityTaskManager service = ActivityTaskManager.getService();
            boolean isTouchExplorationEnabled = this.mAccessibilityManager.isTouchExplorationEnabled();
            boolean isInLockTaskMode = service.isInLockTaskMode();
            if (isInLockTaskMode && !isTouchExplorationEnabled) {
                long currentTimeMillis = System.currentTimeMillis();
                if (currentTimeMillis - this.mLastLockToAppLongPress < 200) {
                    service.stopSystemLockTaskMode();
                    this.mNavigationBarView.updateNavButtonIcons();
                    return true;
                }
                if (view.getId() == i) {
                    if (i2 == C0015R$id.recent_apps) {
                        buttonDispatcher = this.mNavigationBarView.getRecentsButton();
                    } else {
                        buttonDispatcher = this.mNavigationBarView.getHomeButton();
                    }
                    if (!buttonDispatcher.getCurrentView().isPressed()) {
                        z = true;
                        this.mLastLockToAppLongPress = currentTimeMillis;
                    }
                }
                z = false;
                this.mLastLockToAppLongPress = currentTimeMillis;
            } else if (view.getId() == i) {
                z = true;
            } else if (isTouchExplorationEnabled && isInLockTaskMode) {
                service.stopSystemLockTaskMode();
                this.mNavigationBarView.updateNavButtonIcons();
                return true;
            } else if (view.getId() != i2) {
                z = false;
            } else if (i2 == C0015R$id.recent_apps) {
                return onLongPressRecents();
            } else {
                return onHomeLongClick(this.mNavigationBarView.getHomeButton().getCurrentView());
            }
            if (z) {
                KeyButtonView keyButtonView = (KeyButtonView) view;
                keyButtonView.sendEvent(0, 128);
                keyButtonView.sendAccessibilityEvent(2);
                return true;
            }
        } catch (RemoteException e) {
            Log.d("NavigationBar", "Unable to reach activity manager", e);
        }
        return false;
    }

    private boolean onLongPressRecents() {
        if (this.mRecentsOptional.isPresent() || !ActivityTaskManager.supportsMultiWindow(getContext()) || !this.mDivider.getView().getSnapAlgorithm().isSplitScreenFeasible() || ActivityManager.isLowRamDeviceStatic() || this.mOverviewProxyService.getProxy() != null) {
            return false;
        }
        return this.mStatusBarLazy.get().toggleSplitScreenMode(271, 286);
    }

    /* access modifiers changed from: private */
    public void onAccessibilityClick(View view) {
        Display display = view.getDisplay();
        this.mAccessibilityManager.notifyAccessibilityButtonClicked(display != null ? display.getDisplayId() : 0);
    }

    /* access modifiers changed from: private */
    public boolean onAccessibilityLongClick(View view) {
        Intent intent = new Intent("com.android.internal.intent.action.CHOOSE_ACCESSIBILITY_BUTTON");
        intent.addFlags(268468224);
        intent.setClassName("android", AccessibilityButtonChooserActivity.class.getName());
        view.getContext().startActivityAsUser(intent, UserHandle.CURRENT);
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    public void updateAccessibilityServicesState(AccessibilityManager accessibilityManager) {
        boolean z = true;
        int a11yButtonState = getA11yButtonState(new boolean[1]);
        boolean z2 = (a11yButtonState & 16) != 0;
        if ((a11yButtonState & 32) == 0) {
            z = false;
        }
        this.mNavigationBarView.setAccessibilityButtonState(z2, z);
        updateSystemUiStateFlags(a11yButtonState);
    }

    public void updateSystemUiStateFlags(int i) {
        if (i < 0) {
            i = getA11yButtonState(null);
        }
        boolean z = false;
        boolean z2 = (i & 16) != 0;
        if ((i & 32) != 0) {
            z = true;
        }
        SysUiState sysUiState = this.mSysUiFlagsContainer;
        sysUiState.setFlag(16, z2);
        sysUiState.setFlag(32, z);
        sysUiState.setFlag(2, true ^ isNavBarWindowVisible());
        sysUiState.commitUpdate(this.mDisplayId);
        registerAction(z2, 11);
        registerAction(z, 12);
    }

    private void registerAction(boolean z, int i) {
        if (z) {
            this.mSystemActions.register(i);
        } else {
            this.mSystemActions.unregister(i);
        }
    }

    public int getA11yButtonState(boolean[] zArr) {
        int i;
        List<AccessibilityServiceInfo> enabledAccessibilityServiceList = this.mAccessibilityManager.getEnabledAccessibilityServiceList(-1);
        int i2 = 0;
        int size = this.mAccessibilityManager.getAccessibilityShortcutTargets(0).size();
        int size2 = enabledAccessibilityServiceList.size() - 1;
        boolean z = false;
        while (true) {
            i = 16;
            if (size2 < 0) {
                break;
            }
            int i3 = enabledAccessibilityServiceList.get(size2).feedbackType;
            if (!(i3 == 0 || i3 == 16)) {
                z = true;
            }
            size2--;
        }
        if (zArr != null) {
            zArr[0] = z;
        }
        if (size < 1) {
            i = 0;
        }
        if (size >= 2) {
            i2 = 32;
        }
        return i | i2;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendAssistantAvailability(boolean z) {
        if (this.mOverviewProxyService.getProxy() != null) {
            try {
                this.mOverviewProxyService.getProxy().onAssistantAvailable(z && QuickStepContract.isGesturalMode(this.mNavBarMode));
            } catch (RemoteException unused) {
                Log.w("NavigationBar", "Unable to send assistant availability data to launcher");
            }
        }
    }

    public void touchAutoDim() {
        getBarTransitions().setAutoDim(false);
        this.mHandler.removeCallbacks(this.mAutoDim);
        int state = this.mStatusBarStateController.getState();
        if (state != 1 && state != 2) {
            this.mHandler.postDelayed(this.mAutoDim, 2250);
        }
    }

    public void setLightBarController(LightBarController lightBarController) {
        this.mLightBarController = lightBarController;
        lightBarController.setNavigationBar(this.mNavigationBarView.getLightTransitionsController());
    }

    public void setAutoHideController(AutoHideController autoHideController) {
        this.mAutoHideController = autoHideController;
        if (autoHideController != null) {
            autoHideController.setNavigationBar(this.mAutoHideUiElement);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isTransientShown() {
        return this.mTransientShown;
    }

    public boolean isNavBarWindowVisible() {
        return this.mNavigationBarWindowState == 0;
    }

    public void checkNavBarModes() {
        this.mNavigationBarView.getBarTransitions().transitionTo(this.mNavigationBarMode, this.mStatusBarLazy.get().isDeviceInteractive() && this.mNavigationBarWindowState != 2);
    }

    @Override // com.android.systemui.statusbar.phone.NavigationModeController.ModeChangedListener
    public void onNavigationModeChanged(int i) {
        this.mNavBarMode = i;
        updateScreenPinningGestures();
        if (!canShowSecondaryHandle()) {
            resetSecondaryHandle();
        }
        if (ActivityManagerWrapper.getInstance().getCurrentUserId() != 0) {
            this.mHandler.post(new Runnable() {
                /* class com.android.systemui.statusbar.phone.$$Lambda$NavigationBarFragment$nsAI5AC9ZGrYUeFXDe0isYp7EU */

                public final void run() {
                    NavigationBarFragment.this.lambda$onNavigationModeChanged$3$NavigationBarFragment();
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onNavigationModeChanged$3 */
    public /* synthetic */ void lambda$onNavigationModeChanged$3$NavigationBarFragment() {
        FragmentHostManager.get(this.mNavigationBarView).reloadFragments();
    }

    public void disableAnimationsDuringHide(long j) {
        this.mNavigationBarView.setLayoutTransitionsEnabled(false);
        this.mNavigationBarView.postDelayed(new Runnable() {
            /* class com.android.systemui.statusbar.phone.$$Lambda$NavigationBarFragment$AE0O6kF8ojkF6VqrpuJnRwniTec */

            public final void run() {
                NavigationBarFragment.this.lambda$disableAnimationsDuringHide$4$NavigationBarFragment();
            }
        }, j + 448);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$disableAnimationsDuringHide$4 */
    public /* synthetic */ void lambda$disableAnimationsDuringHide$4$NavigationBarFragment() {
        this.mNavigationBarView.setLayoutTransitionsEnabled(true);
    }

    public AssistHandleViewController getAssistHandlerViewController() {
        return this.mAssistHandlerViewController;
    }

    public void transitionTo(int i, boolean z) {
        getBarTransitions().transitionTo(i, z);
    }

    public NavigationBarTransitions getBarTransitions() {
        return this.mNavigationBarView.getBarTransitions();
    }

    public void finishBarAnimations() {
        this.mNavigationBarView.getBarTransitions().finishAnimations();
    }

    public void onDisplayChanged(int i) {
        int rotation;
        if (canShowSecondaryHandle() && (rotation = getContext().getResources().getConfiguration().windowConfiguration.getRotation()) != this.mCurrentRotation) {
            this.mCurrentRotation = rotation;
            orientSecondaryHomeHandle();
        }
    }

    private boolean canShowSecondaryHandle() {
        return this.mNavBarMode == 2;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$5 */
    public /* synthetic */ void lambda$new$5$NavigationBarFragment(Integer num) {
        NavigationBarView navigationBarView = this.mNavigationBarView;
        if (navigationBarView != null && navigationBarView.needsReorient(num.intValue())) {
            repositionNavigationBar();
        }
    }

    public static View create(Context context, final FragmentHostManager.FragmentListener fragmentListener) {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(-1, -1, 2019, 545521768, -3);
        layoutParams.token = new Binder();
        layoutParams.setTitle("NavigationBar" + context.getDisplayId());
        layoutParams.accessibilityTitle = context.getString(C0021R$string.nav_bar);
        layoutParams.windowAnimations = 0;
        layoutParams.privateFlags = layoutParams.privateFlags | 16777216;
        final View inflate = LayoutInflater.from(context).inflate(C0017R$layout.navigation_bar_window, (ViewGroup) null);
        if (inflate == null) {
            return null;
        }
        inflate.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            /* class com.android.systemui.statusbar.phone.NavigationBarFragment.AnonymousClass7 */

            public void onViewAttachedToWindow(View view) {
                FragmentHostManager fragmentHostManager = FragmentHostManager.get(view);
                fragmentHostManager.getFragmentManager().beginTransaction().replace(C0015R$id.navigation_bar_frame, NavigationBarFragment.this, "NavigationBar").commit();
                fragmentHostManager.addTagListener("NavigationBar", fragmentListener);
            }

            public void onViewDetachedFromWindow(View view) {
                FragmentHostManager.removeAndDestroy(view);
                inflate.removeOnAttachStateChangeListener(this);
            }
        });
        ((WindowManager) context.getSystemService(WindowManager.class)).addView(inflate, layoutParams);
        return inflate;
    }

    /* access modifiers changed from: package-private */
    public int getNavigationIconHints() {
        return this.mNavigationIconHints;
    }
}
