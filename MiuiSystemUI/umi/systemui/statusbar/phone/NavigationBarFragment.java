package com.android.systemui.statusbar.phone;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.ActivityManager;
import android.app.ActivityTaskManager;
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
import com.android.systemui.C0012R$id;
import com.android.systemui.C0014R$layout;
import com.android.systemui.C0018R$string;
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
    /* access modifiers changed from: private */
    public AccessibilityManager mAccessibilityManager;
    private final AccessibilityManagerWrapper mAccessibilityManagerWrapper;
    private int mAppearance;
    private final ContentObserver mAssistContentObserver;
    private AssistHandleViewController mAssistHandlerViewController;
    protected final AssistManager mAssistManager;
    /* access modifiers changed from: private */
    public boolean mAssistantAvailable;
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
    /* access modifiers changed from: private */
    public boolean mForceNavBarHandleOpaque;
    private final Handler mHandler;
    public boolean mHomeBlockedThisTouch;
    private boolean mIsOnDefaultDisplay;
    private final KeyOrderObserver mKeyOrderObserver;
    private long mLastLockToAppLongPress;
    private int mLayoutDirection;
    private LightBarController mLightBarController;
    private Locale mLocale;
    private final MetricsLogger mMetricsLogger;
    /* access modifiers changed from: private */
    public int mNavBarMode;
    private int mNavigationBarMode;
    protected NavigationBarView mNavigationBarView = null;
    private int mNavigationBarWindowState;
    private int mNavigationIconHints;
    private final NavigationModeController mNavigationModeController;
    /* access modifiers changed from: private */
    public final NotificationRemoteInputManager mNotificationRemoteInputManager;
    private final DeviceConfig.OnPropertiesChangedListener mOnPropertiesChangedListener;
    /* access modifiers changed from: private */
    public QuickswitchOrientedNavHandle mOrientationHandle;
    private ViewTreeObserver.OnGlobalLayoutListener mOrientationHandleGlobalLayoutListener;
    private NavigationBarTransitions.DarkIntensityListener mOrientationHandleIntensityListener;
    private WindowManager.LayoutParams mOrientationParams;
    private final OverviewProxyService.OverviewProxyListener mOverviewProxyListener;
    private OverviewProxyService mOverviewProxyService;
    private final Optional<Recents> mRecentsOptional;
    private final ContextualButton.ContextButtonListener mRotationButtonListener;
    private final Consumer<Integer> mRotationWatcher;
    /* access modifiers changed from: private */
    public boolean mShowOrientedHandleForImmersiveMode;
    /* access modifiers changed from: private */
    public int mStartingQuickSwitchRotation;
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

    public void onDisplayRemoved(int i) {
    }

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
        NavigationModeController navigationModeController2 = navigationModeController;
        boolean z = false;
        this.mNavigationBarWindowState = 0;
        this.mNavigationIconHints = 0;
        this.mNavBarMode = 0;
        this.mKeyOrderObserver = new KeyOrderObserver();
        this.mStartingQuickSwitchRotation = -1;
        this.mAutoHideUiElement = new AutoHideUiElement() {
            public void synchronizeState() {
                NavigationBarFragment.this.checkNavBarModes();
            }

            public boolean shouldHideOnTouch() {
                return !NavigationBarFragment.this.mNotificationRemoteInputManager.getController().isRemoteInputActive();
            }

            public boolean isVisible() {
                return NavigationBarFragment.this.isTransientShown();
            }

            public void hide() {
                NavigationBarFragment.this.clearTransient();
            }
        };
        this.mOverviewProxyListener = new OverviewProxyService.OverviewProxyListener() {
            public void onConnectionChanged(boolean z) {
                NavigationBarFragment.this.mNavigationBarView.updateStates();
                NavigationBarFragment.this.updateScreenPinningGestures();
                if (z) {
                    NavigationBarFragment navigationBarFragment = NavigationBarFragment.this;
                    navigationBarFragment.sendAssistantAvailability(navigationBarFragment.mAssistantAvailable);
                }
            }

            public void onQuickSwitchToNewTask(int i) {
                int unused = NavigationBarFragment.this.mStartingQuickSwitchRotation = i;
                if (i == -1) {
                    boolean unused2 = NavigationBarFragment.this.mShowOrientedHandleForImmersiveMode = false;
                }
                NavigationBarFragment.this.orientSecondaryHomeHandle();
            }

            public void startAssistant(Bundle bundle) {
                NavigationBarFragment.this.mAssistManager.startAssist(bundle);
            }

            /* JADX WARNING: Removed duplicated region for block: B:18:? A[RETURN, SYNTHETIC] */
            /* JADX WARNING: Removed duplicated region for block: B:9:0x0035 A[ADDED_TO_REGION] */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void onNavBarButtonAlphaChanged(float r4, boolean r5) {
                /*
                    r3 = this;
                    com.android.systemui.statusbar.phone.NavigationBarFragment r0 = com.android.systemui.statusbar.phone.NavigationBarFragment.this
                    int r0 = r0.mNavBarMode
                    boolean r0 = com.android.systemui.shared.system.QuickStepContract.isSwipeUpMode(r0)
                    r1 = 0
                    if (r0 == 0) goto L_0x0016
                    com.android.systemui.statusbar.phone.NavigationBarFragment r3 = com.android.systemui.statusbar.phone.NavigationBarFragment.this
                    com.android.systemui.statusbar.phone.NavigationBarView r3 = r3.mNavigationBarView
                    com.android.systemui.statusbar.phone.ButtonDispatcher r3 = r3.getBackButton()
                    goto L_0x0032
                L_0x0016:
                    com.android.systemui.statusbar.phone.NavigationBarFragment r0 = com.android.systemui.statusbar.phone.NavigationBarFragment.this
                    int r0 = r0.mNavBarMode
                    boolean r0 = com.android.systemui.shared.system.QuickStepContract.isGesturalMode(r0)
                    if (r0 == 0) goto L_0x0031
                    com.android.systemui.statusbar.phone.NavigationBarFragment r0 = com.android.systemui.statusbar.phone.NavigationBarFragment.this
                    boolean r0 = r0.mForceNavBarHandleOpaque
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

            public void onOverviewShown(boolean z) {
                NavigationBarFragment.this.mNavigationBarView.getRotationButtonController().setSkipOverrideUserLockPrefsOnce();
            }

            public void onToggleRecentApps() {
                NavigationBarFragment.this.mNavigationBarView.getRotationButtonController().setSkipOverrideUserLockPrefsOnce();
            }
        };
        this.mOrientationHandleIntensityListener = new NavigationBarTransitions.DarkIntensityListener() {
            public void onDarkIntensity(float f) {
                NavigationBarFragment.this.mOrientationHandle.setDarkIntensity(f);
            }
        };
        this.mRotationButtonListener = new ContextualButton.ContextButtonListener() {
            public final void onVisibilityChanged(ContextualButton contextualButton, boolean z) {
                NavigationBarFragment.this.lambda$new$0$NavigationBarFragment(contextualButton, z);
            }
        };
        this.mAutoDim = new Runnable() {
            public final void run() {
                NavigationBarFragment.this.lambda$new$1$NavigationBarFragment();
            }
        };
        this.mAssistContentObserver = new ContentObserver(new Handler(Looper.getMainLooper())) {
            public void onChange(boolean z, Uri uri) {
                boolean z2 = NavigationBarFragment.this.mAssistManager.getAssistInfoForUser(-2) != null;
                if (NavigationBarFragment.this.mAssistantAvailable != z2) {
                    NavigationBarFragment.this.sendAssistantAvailability(z2);
                    boolean unused = NavigationBarFragment.this.mAssistantAvailable = z2;
                }
            }
        };
        this.mOnPropertiesChangedListener = new DeviceConfig.OnPropertiesChangedListener() {
            public void onPropertiesChanged(DeviceConfig.Properties properties) {
                if (properties.getKeyset().contains("nav_bar_handle_force_opaque")) {
                    boolean unused = NavigationBarFragment.this.mForceNavBarHandleOpaque = properties.getBoolean("nav_bar_handle_force_opaque", true);
                }
            }
        };
        this.mAccessibilityListener = new AccessibilityManager.AccessibilityServicesStateChangeListener() {
            public final void onAccessibilityServicesStateChanged(AccessibilityManager accessibilityManager) {
                NavigationBarFragment.this.updateAccessibilityServicesState(accessibilityManager);
            }
        };
        this.mRotationWatcher = new Consumer() {
            public final void accept(Object obj) {
                NavigationBarFragment.this.lambda$new$5$NavigationBarFragment((Integer) obj);
            }
        };
        this.mBroadcastReceiver = new BroadcastReceiver() {
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
        this.mNavigationModeController = navigationModeController2;
        this.mNavBarMode = navigationModeController.addListener(this);
        this.mBroadcastDispatcher = broadcastDispatcher;
        this.mCommandQueue = commandQueue;
        this.mDivider = divider;
        this.mRecentsOptional = optional;
        this.mSystemActions = systemActions;
        this.mHandler = handler;
        this.mUiEventLogger = uiEventLogger;
    }

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
            public final /* synthetic */ Handler f$0;

            {
                this.f$0 = r1;
            }

            public final void execute(Runnable runnable) {
                this.f$0.post(runnable);
            }
        }, this.mOnPropertiesChangedListener);
    }

    public void onDestroy() {
        super.onDestroy();
        this.mNavigationModeController.removeListener(this);
        this.mAccessibilityManagerWrapper.removeCallback(this.mAccessibilityListener);
        this.mContentResolver.unregisterContentObserver(this.mAssistContentObserver);
        DeviceConfig.removeOnPropertiesChangedListener(this.mOnPropertiesChangedListener);
    }

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        return layoutInflater.inflate(C0014R$layout.navigation_bar, viewGroup, false);
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
            public final void onVerticalChanged(boolean z) {
                NavigationBarFragment.this.onVerticalChanged(z);
            }
        });
        this.mNavigationBarView.setOnTouchListener(new View.OnTouchListener() {
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
    /* JADX WARNING: Removed duplicated region for block: B:26:0x008d  */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x0090  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void orientSecondaryHomeHandle() {
        /*
            r7 = this;
            boolean r0 = r7.canShowSecondaryHandle()
            if (r0 != 0) goto L_0x0007
            return
        L_0x0007:
            int r0 = r7.mStartingQuickSwitchRotation
            r1 = -1
            if (r0 == r1) goto L_0x00b0
            com.android.systemui.stackdivider.Divider r0 = r7.mDivider
            boolean r0 = r0.isDividerVisible()
            if (r0 == 0) goto L_0x0016
            goto L_0x00b0
        L_0x0016:
            int r0 = r7.mCurrentRotation
            int r2 = r7.mStartingQuickSwitchRotation
            int r0 = r7.deltaRotation(r0, r2)
            int r2 = r7.mStartingQuickSwitchRotation
            if (r2 == r1) goto L_0x0024
            if (r0 != r1) goto L_0x004e
        L_0x0024:
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "secondary nav delta rotation: "
            r1.append(r2)
            r1.append(r0)
            java.lang.String r2 = " current: "
            r1.append(r2)
            int r2 = r7.mCurrentRotation
            r1.append(r2)
            java.lang.String r2 = " starting: "
            r1.append(r2)
            int r2 = r7.mStartingQuickSwitchRotation
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            java.lang.String r2 = "NavigationBar"
            android.util.Log.d(r2, r1)
        L_0x004e:
            android.view.WindowManager r1 = r7.mWindowManager
            android.view.WindowMetrics r1 = r1.getCurrentWindowMetrics()
            android.graphics.Rect r1 = r1.getBounds()
            com.android.systemui.statusbar.phone.QuickswitchOrientedNavHandle r2 = r7.mOrientationHandle
            r2.setDeltaRotation(r0)
            r2 = 3
            r3 = 1
            r4 = 0
            if (r0 == 0) goto L_0x0077
            if (r0 == r3) goto L_0x006c
            r5 = 2
            if (r0 == r5) goto L_0x0077
            if (r0 == r2) goto L_0x006c
            r1 = r4
            r5 = r1
            goto L_0x0089
        L_0x006c:
            int r1 = r1.height()
            com.android.systemui.statusbar.phone.NavigationBarView r5 = r7.mNavigationBarView
            int r5 = r5.getHeight()
            goto L_0x0089
        L_0x0077:
            boolean r5 = r7.mShowOrientedHandleForImmersiveMode
            if (r5 != 0) goto L_0x007f
            r7.resetSecondaryHandle()
            return
        L_0x007f:
            int r5 = r1.width()
            com.android.systemui.statusbar.phone.NavigationBarView r1 = r7.mNavigationBarView
            int r1 = r1.getHeight()
        L_0x0089:
            android.view.WindowManager$LayoutParams r6 = r7.mOrientationParams
            if (r0 != 0) goto L_0x0090
            r2 = 80
            goto L_0x0094
        L_0x0090:
            if (r0 != r3) goto L_0x0093
            goto L_0x0094
        L_0x0093:
            r2 = 5
        L_0x0094:
            r6.gravity = r2
            android.view.WindowManager$LayoutParams r0 = r7.mOrientationParams
            r0.height = r1
            r0.width = r5
            android.view.WindowManager r1 = r7.mWindowManager
            com.android.systemui.statusbar.phone.QuickswitchOrientedNavHandle r2 = r7.mOrientationHandle
            r1.updateViewLayout(r2, r0)
            com.android.systemui.statusbar.phone.NavigationBarView r0 = r7.mNavigationBarView
            r1 = 8
            r0.setVisibility(r1)
            com.android.systemui.statusbar.phone.QuickswitchOrientedNavHandle r7 = r7.mOrientationHandle
            r7.setVisibility(r4)
            goto L_0x00b3
        L_0x00b0:
            r7.resetSecondaryHandle()
        L_0x00b3:
            return
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
            this.mNavigationBarView.setOrientedHandleSamplingRegion((Rect) null);
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

    public void showTransient(int i, int[] iArr) {
        if (i == this.mDisplayId && InsetsState.containsType(iArr, 1) && !this.mTransientShown) {
            this.mTransientShown = true;
            handleTransientChanged();
        }
    }

    public void abortTransient(int i, int[] iArr) {
        if (i == this.mDisplayId && InsetsState.containsType(iArr, 1)) {
            clearTransient();
        }
    }

    /* access modifiers changed from: private */
    public void clearTransient() {
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
    public void updateScreenPinningGestures() {
        NavigationBarView navigationBarView = this.mNavigationBarView;
        if (navigationBarView != null) {
            boolean isRecentsButtonVisible = navigationBarView.isRecentsButtonVisible();
            ButtonDispatcher backButton = this.mNavigationBarView.getBackButton();
            if (isRecentsButtonVisible) {
                backButton.setOnLongClickListener(new View.OnLongClickListener() {
                    public final boolean onLongClick(View view) {
                        return NavigationBarFragment.this.onLongPressBackRecents(view);
                    }
                });
            } else {
                backButton.setOnLongClickListener(new View.OnLongClickListener() {
                    public final boolean onLongClick(View view) {
                        return NavigationBarFragment.this.onLongPressBackHome(view);
                    }
                });
            }
        }
    }

    /* access modifiers changed from: private */
    public void notifyNavigationBarScreenOn() {
        this.mNavigationBarView.updateNavButtonIcons();
    }

    private void prepareNavigationBarView() {
        this.mNavigationBarView.reorient();
        ButtonDispatcher recentsButton = this.mNavigationBarView.getRecentsButton();
        recentsButton.setOnClickListener(new View.OnClickListener() {
            public final void onClick(View view) {
                NavigationBarFragment.this.onRecentsClick(view);
            }
        });
        recentsButton.setOnTouchListener(new View.OnTouchListener() {
            public final boolean onTouch(View view, MotionEvent motionEvent) {
                return NavigationBarFragment.this.onRecentsTouch(view, motionEvent);
            }
        });
        recentsButton.setLongClickable(true);
        recentsButton.setOnLongClickListener(new View.OnLongClickListener() {
            public final boolean onLongClick(View view) {
                return NavigationBarFragment.this.onLongPressBackRecents(view);
            }
        });
        this.mNavigationBarView.getBackButton().setLongClickable(true);
        this.mNavigationBarView.getHomeButton().setOnTouchListener(new View.OnTouchListener() {
            public final boolean onTouch(View view, MotionEvent motionEvent) {
                return NavigationBarFragment.this.onHomeTouch(view, motionEvent);
            }
        });
        ButtonDispatcher accessibilityButton = this.mNavigationBarView.getAccessibilityButton();
        accessibilityButton.setOnClickListener(new View.OnClickListener() {
            public final void onClick(View view) {
                NavigationBarFragment.this.onAccessibilityClick(view);
            }
        });
        accessibilityButton.setOnLongClickListener(new View.OnLongClickListener() {
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
        return onLongPressNavigationButtons(view, C0012R$id.back, C0012R$id.home);
    }

    /* access modifiers changed from: private */
    public boolean onLongPressBackRecents(View view) {
        return onLongPressNavigationButtons(view, C0012R$id.back, C0012R$id.recent_apps);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:47:0x0096, code lost:
        r9 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x0097, code lost:
        android.util.Log.d("NavigationBar", "Unable to reach activity manager", r9);
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean onLongPressNavigationButtons(android.view.View r10, int r11, int r12) {
        /*
            r9 = this;
            r0 = 0
            android.app.IActivityTaskManager r1 = android.app.ActivityTaskManager.getService()     // Catch:{ RemoteException -> 0x0096 }
            android.view.accessibility.AccessibilityManager r2 = r9.mAccessibilityManager     // Catch:{ RemoteException -> 0x0096 }
            boolean r2 = r2.isTouchExplorationEnabled()     // Catch:{ RemoteException -> 0x0096 }
            boolean r3 = r1.isInLockTaskMode()     // Catch:{ RemoteException -> 0x0096 }
            r4 = 1
            if (r3 == 0) goto L_0x0052
            if (r2 != 0) goto L_0x0052
            long r2 = java.lang.System.currentTimeMillis()     // Catch:{ all -> 0x0094 }
            long r5 = r9.mLastLockToAppLongPress     // Catch:{ all -> 0x0094 }
            long r5 = r2 - r5
            r7 = 200(0xc8, double:9.9E-322)
            int r5 = (r5 > r7 ? 1 : (r5 == r7 ? 0 : -1))
            if (r5 >= 0) goto L_0x002b
            r1.stopSystemLockTaskMode()     // Catch:{ RemoteException -> 0x0096 }
            com.android.systemui.statusbar.phone.NavigationBarView r9 = r9.mNavigationBarView     // Catch:{ RemoteException -> 0x0096 }
            r9.updateNavButtonIcons()     // Catch:{ RemoteException -> 0x0096 }
            return r4
        L_0x002b:
            int r1 = r10.getId()     // Catch:{ all -> 0x0094 }
            if (r1 != r11) goto L_0x004e
            int r11 = com.android.systemui.C0012R$id.recent_apps     // Catch:{ all -> 0x0094 }
            if (r12 != r11) goto L_0x003c
            com.android.systemui.statusbar.phone.NavigationBarView r11 = r9.mNavigationBarView     // Catch:{ all -> 0x0094 }
            com.android.systemui.statusbar.phone.ButtonDispatcher r11 = r11.getRecentsButton()     // Catch:{ all -> 0x0094 }
            goto L_0x0042
        L_0x003c:
            com.android.systemui.statusbar.phone.NavigationBarView r11 = r9.mNavigationBarView     // Catch:{ all -> 0x0094 }
            com.android.systemui.statusbar.phone.ButtonDispatcher r11 = r11.getHomeButton()     // Catch:{ all -> 0x0094 }
        L_0x0042:
            android.view.View r11 = r11.getCurrentView()     // Catch:{ all -> 0x0094 }
            boolean r11 = r11.isPressed()     // Catch:{ all -> 0x0094 }
            if (r11 != 0) goto L_0x004e
            r11 = r4
            goto L_0x004f
        L_0x004e:
            r11 = r0
        L_0x004f:
            r9.mLastLockToAppLongPress = r2     // Catch:{ all -> 0x0094 }
            goto L_0x0086
        L_0x0052:
            int r5 = r10.getId()     // Catch:{ all -> 0x0094 }
            if (r5 != r11) goto L_0x005a
            r11 = r4
            goto L_0x0086
        L_0x005a:
            if (r2 == 0) goto L_0x0067
            if (r3 == 0) goto L_0x0067
            r1.stopSystemLockTaskMode()     // Catch:{ RemoteException -> 0x0096 }
            com.android.systemui.statusbar.phone.NavigationBarView r9 = r9.mNavigationBarView     // Catch:{ RemoteException -> 0x0096 }
            r9.updateNavButtonIcons()     // Catch:{ RemoteException -> 0x0096 }
            return r4
        L_0x0067:
            int r11 = r10.getId()     // Catch:{ all -> 0x0094 }
            if (r11 != r12) goto L_0x0085
            int r10 = com.android.systemui.C0012R$id.recent_apps     // Catch:{ all -> 0x0094 }
            if (r12 != r10) goto L_0x0076
            boolean r9 = r9.onLongPressRecents()     // Catch:{ all -> 0x0094 }
            goto L_0x0084
        L_0x0076:
            com.android.systemui.statusbar.phone.NavigationBarView r10 = r9.mNavigationBarView     // Catch:{ all -> 0x0094 }
            com.android.systemui.statusbar.phone.ButtonDispatcher r10 = r10.getHomeButton()     // Catch:{ all -> 0x0094 }
            android.view.View r10 = r10.getCurrentView()     // Catch:{ all -> 0x0094 }
            boolean r9 = r9.onHomeLongClick(r10)     // Catch:{ all -> 0x0094 }
        L_0x0084:
            return r9
        L_0x0085:
            r11 = r0
        L_0x0086:
            if (r11 == 0) goto L_0x009e
            com.android.systemui.statusbar.policy.KeyButtonView r10 = (com.android.systemui.statusbar.policy.KeyButtonView) r10     // Catch:{ RemoteException -> 0x0096 }
            r9 = 128(0x80, float:1.794E-43)
            r10.sendEvent(r0, r9)     // Catch:{ RemoteException -> 0x0096 }
            r9 = 2
            r10.sendAccessibilityEvent(r9)     // Catch:{ RemoteException -> 0x0096 }
            return r4
        L_0x0094:
            r9 = move-exception
            throw r9     // Catch:{ RemoteException -> 0x0096 }
        L_0x0096:
            r9 = move-exception
            java.lang.String r10 = "NavigationBar"
            java.lang.String r11 = "Unable to reach activity manager"
            android.util.Log.d(r10, r11, r9)
        L_0x009e:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.phone.NavigationBarFragment.onLongPressNavigationButtons(android.view.View, int, int):boolean");
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
            i = getA11yButtonState((boolean[]) null);
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
    public void sendAssistantAvailability(boolean z) {
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
    public boolean isTransientShown() {
        return this.mTransientShown;
    }

    public boolean isNavBarWindowVisible() {
        return this.mNavigationBarWindowState == 0;
    }

    public void checkNavBarModes() {
        this.mNavigationBarView.getBarTransitions().transitionTo(this.mNavigationBarMode, this.mStatusBarLazy.get().isDeviceInteractive() && this.mNavigationBarWindowState != 2);
    }

    public void onNavigationModeChanged(int i) {
        this.mNavBarMode = i;
        updateScreenPinningGestures();
        if (!canShowSecondaryHandle()) {
            resetSecondaryHandle();
        }
        if (ActivityManagerWrapper.getInstance().getCurrentUserId() != 0) {
            this.mHandler.post(new Runnable() {
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
        layoutParams.accessibilityTitle = context.getString(C0018R$string.nav_bar);
        layoutParams.windowAnimations = 0;
        layoutParams.privateFlags = layoutParams.privateFlags | 16777216;
        final View inflate = LayoutInflater.from(context).inflate(C0014R$layout.navigation_bar_window, (ViewGroup) null);
        if (inflate == null) {
            return null;
        }
        inflate.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            public void onViewAttachedToWindow(View view) {
                FragmentHostManager fragmentHostManager = FragmentHostManager.get(view);
                fragmentHostManager.getFragmentManager().beginTransaction().replace(C0012R$id.navigation_bar_frame, NavigationBarFragment.this, "NavigationBar").commit();
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
