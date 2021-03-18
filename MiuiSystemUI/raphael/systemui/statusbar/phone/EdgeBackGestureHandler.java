package com.android.systemui.statusbar.phone;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Region;
import android.hardware.display.DisplayManager;
import android.hardware.input.InputManager;
import android.os.Looper;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.DeviceConfig;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.ISystemGestureExclusionListener;
import android.view.InputChannel;
import android.view.InputEvent;
import android.view.InputEventReceiver;
import android.view.InputMonitor;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;
import com.android.internal.policy.GestureNavigationSettingsObserver;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0021R$string;
import com.android.systemui.Dependency;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.bubbles.BubbleController;
import com.android.systemui.model.SysUiState;
import com.android.systemui.plugins.NavigationEdgeBackPlugin;
import com.android.systemui.plugins.PluginListener;
import com.android.systemui.recents.OverviewProxyService;
import com.android.systemui.settings.CurrentUserTracker;
import com.android.systemui.shared.plugins.PluginManager;
import com.android.systemui.shared.system.ActivityManagerWrapper;
import com.android.systemui.shared.system.QuickStepContract;
import com.android.systemui.shared.system.SysUiStatsLog;
import com.android.systemui.shared.system.TaskStackChangeListener;
import com.android.systemui.shared.tracing.ProtoTraceable;
import com.android.systemui.statusbar.phone.EdgeBackGestureHandler;
import com.android.systemui.tracing.ProtoTracer;
import com.android.systemui.tracing.nano.EdgeBackGestureHandlerProto;
import com.android.systemui.tracing.nano.SystemUiTraceProto;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

public class EdgeBackGestureHandler extends CurrentUserTracker implements DisplayManager.DisplayListener, PluginListener<NavigationEdgeBackPlugin>, ProtoTraceable<SystemUiTraceProto> {
    private static final int MAX_LONG_PRESS_TIMEOUT = SystemProperties.getInt("gestures.back_timeout", 250);
    private boolean mAllowGesture;
    private final NavigationEdgeBackPlugin.BackCallback mBackCallback;
    private float mBottomGestureHeight;
    private final Context mContext;
    private boolean mDisabledForQuickstep;
    private final int mDisplayId;
    private final Point mDisplaySize = new Point();
    private final PointF mDownPoint = new PointF();
    private NavigationEdgeBackPlugin mEdgeBackPlugin;
    private int mEdgeWidthLeft;
    private int mEdgeWidthRight;
    private final PointF mEndPoint = new PointF();
    private final Region mExcludeRegion = new Region();
    private final List<ComponentName> mGestureBlockingActivities = new ArrayList();
    private boolean mGestureBlockingActivityRunning;
    private ISystemGestureExclusionListener mGestureExclusionListener = new ISystemGestureExclusionListener.Stub() {
        /* class com.android.systemui.statusbar.phone.EdgeBackGestureHandler.AnonymousClass1 */

        public void onSystemGestureExclusionChanged(int i, Region region, Region region2) {
            if (i == EdgeBackGestureHandler.this.mDisplayId) {
                EdgeBackGestureHandler.this.mMainExecutor.execute(new Runnable(region, region2) {
                    /* class com.android.systemui.statusbar.phone.$$Lambda$EdgeBackGestureHandler$1$gxj4RNtkm_JZXkSr9gvVxA9V4Ew */
                    public final /* synthetic */ Region f$1;
                    public final /* synthetic */ Region f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    public final void run() {
                        EdgeBackGestureHandler.AnonymousClass1.this.lambda$onSystemGestureExclusionChanged$0$EdgeBackGestureHandler$1(this.f$1, this.f$2);
                    }
                });
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onSystemGestureExclusionChanged$0 */
        public /* synthetic */ void lambda$onSystemGestureExclusionChanged$0$EdgeBackGestureHandler$1(Region region, Region region2) {
            EdgeBackGestureHandler.this.mExcludeRegion.set(region);
            Region region3 = EdgeBackGestureHandler.this.mUnrestrictedExcludeRegion;
            if (region2 != null) {
                region = region2;
            }
            region3.set(region);
        }
    };
    private final GestureNavigationSettingsObserver mGestureNavigationSettingsObserver;
    private boolean mInRejectedExclusion;
    private InputEventReceiver mInputEventReceiver;
    private InputMonitor mInputMonitor;
    private boolean mIsAttached;
    private boolean mIsBackGestureAllowed;
    private boolean mIsEnabled;
    private boolean mIsNavBarShownTransiently;
    private boolean mIsOnLeftEdge;
    private int mLeftInset;
    private boolean mLogGesture;
    private final int mLongPressTimeout;
    private final Executor mMainExecutor;
    private final OverviewProxyService mOverviewProxyService;
    private final PluginManager mPluginManager;
    private OverviewProxyService.OverviewProxyListener mQuickSwitchListener = new OverviewProxyService.OverviewProxyListener() {
        /* class com.android.systemui.statusbar.phone.EdgeBackGestureHandler.AnonymousClass2 */

        @Override // com.android.systemui.recents.OverviewProxyService.OverviewProxyListener
        public void onQuickSwitchToNewTask(int i) {
            EdgeBackGestureHandler.this.mStartingQuickstepRotation = i;
            EdgeBackGestureHandler.this.updateDisabledForQuickstep();
        }
    };
    private int mRightInset;
    private int mStartingQuickstepRotation = -1;
    private SysUiState.SysUiStateCallback mStateCallback;
    private final Runnable mStateChangeCallback;
    private int mSysUiFlags;
    private TaskStackChangeListener mTaskStackListener = new TaskStackChangeListener() {
        /* class com.android.systemui.statusbar.phone.EdgeBackGestureHandler.AnonymousClass3 */

        @Override // com.android.systemui.shared.system.TaskStackChangeListener
        public void onTaskStackChanged() {
            EdgeBackGestureHandler edgeBackGestureHandler = EdgeBackGestureHandler.this;
            edgeBackGestureHandler.mGestureBlockingActivityRunning = edgeBackGestureHandler.isGestureBlockingActivityRunning();
        }
    };
    private boolean mThresholdCrossed;
    private float mTouchSlop;
    private final Region mUnrestrictedExcludeRegion = new Region();

    public void onDisplayAdded(int i) {
    }

    public void onDisplayRemoved(int i) {
    }

    public EdgeBackGestureHandler(Context context, OverviewProxyService overviewProxyService, SysUiState sysUiState, PluginManager pluginManager, Runnable runnable) {
        super((BroadcastDispatcher) Dependency.get(BroadcastDispatcher.class));
        this.mThresholdCrossed = false;
        this.mAllowGesture = false;
        this.mLogGesture = false;
        this.mInRejectedExclusion = false;
        this.mBackCallback = new NavigationEdgeBackPlugin.BackCallback() {
            /* class com.android.systemui.statusbar.phone.EdgeBackGestureHandler.AnonymousClass4 */

            @Override // com.android.systemui.plugins.NavigationEdgeBackPlugin.BackCallback
            public void triggerBack() {
                EdgeBackGestureHandler.this.sendEvent(0, 4);
                int i = 1;
                EdgeBackGestureHandler.this.sendEvent(1, 4);
                EdgeBackGestureHandler.this.mOverviewProxyService.notifyBackAction(true, (int) EdgeBackGestureHandler.this.mDownPoint.x, (int) EdgeBackGestureHandler.this.mDownPoint.y, false, !EdgeBackGestureHandler.this.mIsOnLeftEdge);
                EdgeBackGestureHandler edgeBackGestureHandler = EdgeBackGestureHandler.this;
                if (edgeBackGestureHandler.mInRejectedExclusion) {
                    i = 2;
                }
                edgeBackGestureHandler.logGesture(i);
            }

            @Override // com.android.systemui.plugins.NavigationEdgeBackPlugin.BackCallback
            public void cancelBack() {
                EdgeBackGestureHandler.this.logGesture(4);
                EdgeBackGestureHandler.this.mOverviewProxyService.notifyBackAction(false, (int) EdgeBackGestureHandler.this.mDownPoint.x, (int) EdgeBackGestureHandler.this.mDownPoint.y, false, !EdgeBackGestureHandler.this.mIsOnLeftEdge);
            }
        };
        this.mStateCallback = new SysUiState.SysUiStateCallback() {
            /* class com.android.systemui.statusbar.phone.EdgeBackGestureHandler.AnonymousClass5 */

            @Override // com.android.systemui.model.SysUiState.SysUiStateCallback
            public void onSystemUiStateChanged(int i) {
                EdgeBackGestureHandler.this.mSysUiFlags = i;
            }
        };
        this.mContext = context;
        this.mDisplayId = context.getDisplayId();
        this.mMainExecutor = context.getMainExecutor();
        this.mOverviewProxyService = overviewProxyService;
        this.mPluginManager = pluginManager;
        this.mStateChangeCallback = runnable;
        ComponentName unflattenFromString = ComponentName.unflattenFromString(context.getString(17039966));
        if (unflattenFromString != null) {
            String packageName = unflattenFromString.getPackageName();
            try {
                Resources resourcesForApplication = context.getPackageManager().getResourcesForApplication(packageName);
                int identifier = resourcesForApplication.getIdentifier("gesture_blocking_activities", "array", packageName);
                if (identifier == 0) {
                    Log.e("EdgeBackGestureHandler", "No resource found for gesture-blocking activities");
                } else {
                    for (String str : resourcesForApplication.getStringArray(identifier)) {
                        this.mGestureBlockingActivities.add(ComponentName.unflattenFromString(str));
                    }
                }
            } catch (PackageManager.NameNotFoundException e) {
                Log.e("EdgeBackGestureHandler", "Failed to add gesture blocking activities", e);
            }
        }
        ((ProtoTracer) Dependency.get(ProtoTracer.class)).add(this);
        this.mLongPressTimeout = Math.min(MAX_LONG_PRESS_TIMEOUT, ViewConfiguration.getLongPressTimeout());
        this.mGestureNavigationSettingsObserver = new GestureNavigationSettingsObserver(this.mContext.getMainThreadHandler(), this.mContext, new Runnable() {
            /* class com.android.systemui.statusbar.phone.$$Lambda$EdgeBackGestureHandler$_LuW15YEeoCQlkaEsBj7DgfSfSI */

            public final void run() {
                EdgeBackGestureHandler.lambda$_LuW15YEeoCQlkaEsBj7DgfSfSI(EdgeBackGestureHandler.this);
            }
        });
        updateCurrentUserResources();
        sysUiState.addCallback(this.mStateCallback);
    }

    public void updateCurrentUserResources() {
        Resources resources = ((NavigationModeController) Dependency.get(NavigationModeController.class)).getCurrentUserContext().getResources();
        this.mEdgeWidthLeft = this.mGestureNavigationSettingsObserver.getLeftSensitivity(resources);
        this.mEdgeWidthRight = this.mGestureNavigationSettingsObserver.getRightSensitivity(resources);
        this.mIsBackGestureAllowed = !this.mGestureNavigationSettingsObserver.areNavigationButtonForcedVisible();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        this.mBottomGestureHeight = TypedValue.applyDimension(1, DeviceConfig.getFloat("systemui", "back_gesture_bottom_height", resources.getDimension(17105335) / displayMetrics.density), displayMetrics);
        this.mTouchSlop = ((float) ViewConfiguration.get(this.mContext).getScaledTouchSlop()) * DeviceConfig.getFloat("systemui", "back_gesture_slop_multiplier", 0.75f);
    }

    /* access modifiers changed from: private */
    public void onNavigationSettingsChanged() {
        boolean isHandlingGestures = isHandlingGestures();
        updateCurrentUserResources();
        if (isHandlingGestures != isHandlingGestures()) {
            this.mStateChangeCallback.run();
        }
    }

    @Override // com.android.systemui.settings.CurrentUserTracker
    public void onUserSwitched(int i) {
        updateIsEnabled();
        updateCurrentUserResources();
    }

    public void onNavBarAttached() {
        this.mIsAttached = true;
        this.mOverviewProxyService.addCallback(this.mQuickSwitchListener);
        updateIsEnabled();
        startTracking();
    }

    public void onNavBarDetached() {
        this.mIsAttached = false;
        this.mOverviewProxyService.removeCallback(this.mQuickSwitchListener);
        updateIsEnabled();
        stopTracking();
    }

    public void onNavigationModeChanged(int i) {
        QuickStepContract.isGesturalMode(i);
        updateIsEnabled();
        updateCurrentUserResources();
    }

    public void onNavBarTransientStateChanged(boolean z) {
        this.mIsNavBarShownTransiently = z;
    }

    private void disposeInputChannel() {
        InputEventReceiver inputEventReceiver = this.mInputEventReceiver;
        if (inputEventReceiver != null) {
            inputEventReceiver.dispose();
            this.mInputEventReceiver = null;
        }
        InputMonitor inputMonitor = this.mInputMonitor;
        if (inputMonitor != null) {
            inputMonitor.dispose();
            this.mInputMonitor = null;
        }
    }

    private void updateIsEnabled() {
        if (this.mIsEnabled) {
            this.mIsEnabled = false;
            disposeInputChannel();
            NavigationEdgeBackPlugin navigationEdgeBackPlugin = this.mEdgeBackPlugin;
            if (navigationEdgeBackPlugin != null) {
                navigationEdgeBackPlugin.onDestroy();
                this.mEdgeBackPlugin = null;
            }
            if (!this.mIsEnabled) {
                this.mGestureNavigationSettingsObserver.unregister();
                ((DisplayManager) this.mContext.getSystemService(DisplayManager.class)).unregisterDisplayListener(this);
                this.mPluginManager.removePluginListener(this);
                ActivityManagerWrapper.getInstance().unregisterTaskStackListener(this.mTaskStackListener);
                try {
                    WindowManagerGlobal.getWindowManagerService().unregisterSystemGestureExclusionListener(this.mGestureExclusionListener, this.mDisplayId);
                } catch (RemoteException | IllegalArgumentException e) {
                    Log.e("EdgeBackGestureHandler", "Failed to unregister window manager callbacks", e);
                }
            } else {
                this.mGestureNavigationSettingsObserver.register();
                updateDisplaySize();
                ((DisplayManager) this.mContext.getSystemService(DisplayManager.class)).registerDisplayListener(this, this.mContext.getMainThreadHandler());
                ActivityManagerWrapper.getInstance().registerTaskStackListener(this.mTaskStackListener);
                try {
                    WindowManagerGlobal.getWindowManagerService().registerSystemGestureExclusionListener(this.mGestureExclusionListener, this.mDisplayId);
                } catch (RemoteException | IllegalArgumentException e2) {
                    Log.e("EdgeBackGestureHandler", "Failed to register window manager callbacks", e2);
                }
                this.mInputMonitor = InputManager.getInstance().monitorGestureInput("edge-swipe", this.mDisplayId);
                this.mInputEventReceiver = new SysUiInputEventReceiver(this.mInputMonitor.getInputChannel(), Looper.getMainLooper());
                setEdgeBackPlugin(new NavigationBarEdgePanel(this.mContext));
                this.mPluginManager.addPluginListener((PluginListener) this, NavigationEdgeBackPlugin.class, false);
            }
        }
    }

    public void onPluginConnected(NavigationEdgeBackPlugin navigationEdgeBackPlugin, Context context) {
        setEdgeBackPlugin(navigationEdgeBackPlugin);
    }

    public void onPluginDisconnected(NavigationEdgeBackPlugin navigationEdgeBackPlugin) {
        setEdgeBackPlugin(new NavigationBarEdgePanel(this.mContext));
    }

    private void setEdgeBackPlugin(NavigationEdgeBackPlugin navigationEdgeBackPlugin) {
        NavigationEdgeBackPlugin navigationEdgeBackPlugin2 = this.mEdgeBackPlugin;
        if (navigationEdgeBackPlugin2 != null) {
            navigationEdgeBackPlugin2.onDestroy();
        }
        this.mEdgeBackPlugin = navigationEdgeBackPlugin;
        navigationEdgeBackPlugin.setBackCallback(this.mBackCallback);
        this.mEdgeBackPlugin.setLayoutParams(createLayoutParams());
        updateDisplaySize();
    }

    public boolean isHandlingGestures() {
        return this.mIsEnabled && this.mIsBackGestureAllowed;
    }

    private WindowManager.LayoutParams createLayoutParams() {
        Resources resources = this.mContext.getResources();
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(resources.getDimensionPixelSize(C0012R$dimen.navigation_edge_panel_width), resources.getDimensionPixelSize(C0012R$dimen.navigation_edge_panel_height), 2024, 8388904, -3);
        layoutParams.privateFlags |= 16;
        layoutParams.setTitle("EdgeBackGestureHandler" + this.mContext.getDisplayId());
        layoutParams.accessibilityTitle = this.mContext.getString(C0021R$string.nav_bar_edge_panel);
        layoutParams.windowAnimations = 0;
        layoutParams.setFitInsetsTypes(0);
        return layoutParams;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onInputEvent(InputEvent inputEvent) {
        if (inputEvent instanceof MotionEvent) {
            onMotionEvent((MotionEvent) inputEvent);
        }
    }

    private boolean isWithinTouchRegion(int i, int i2) {
        Point point = this.mDisplaySize;
        if (((float) i2) >= ((float) point.y) - this.mBottomGestureHeight) {
            return false;
        }
        if (i > (this.mEdgeWidthLeft + this.mLeftInset) * 2 && i < point.x - ((this.mEdgeWidthRight + this.mRightInset) * 2)) {
            return false;
        }
        boolean z = i <= this.mEdgeWidthLeft + this.mLeftInset || i >= (this.mDisplaySize.x - this.mEdgeWidthRight) - this.mRightInset;
        if (this.mIsNavBarShownTransiently) {
            this.mLogGesture = true;
            return z;
        } else if (this.mExcludeRegion.contains(i, i2)) {
            if (z) {
                this.mOverviewProxyService.notifyBackAction(false, -1, -1, false, !this.mIsOnLeftEdge);
                PointF pointF = this.mEndPoint;
                pointF.x = -1.0f;
                pointF.y = -1.0f;
                this.mLogGesture = true;
                logGesture(3);
            }
            return false;
        } else {
            this.mInRejectedExclusion = this.mUnrestrictedExcludeRegion.contains(i, i2);
            this.mLogGesture = true;
            return z;
        }
    }

    private void cancelGesture(MotionEvent motionEvent) {
        this.mAllowGesture = false;
        this.mLogGesture = false;
        this.mInRejectedExclusion = false;
        MotionEvent obtain = MotionEvent.obtain(motionEvent);
        obtain.setAction(3);
        this.mEdgeBackPlugin.onMotionEvent(obtain);
        obtain.recycle();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void logGesture(int i) {
        if (this.mLogGesture) {
            this.mLogGesture = false;
            int i2 = (int) this.mDownPoint.y;
            int i3 = this.mIsOnLeftEdge ? 1 : 2;
            PointF pointF = this.mDownPoint;
            int i4 = (int) pointF.y;
            PointF pointF2 = this.mEndPoint;
            SysUiStatsLog.write(224, i, i2, i3, (int) pointF.x, i4, (int) pointF2.x, (int) pointF2.y, this.mEdgeWidthLeft + this.mLeftInset, this.mDisplaySize.x - (this.mEdgeWidthRight + this.mRightInset));
        }
    }

    private void onMotionEvent(MotionEvent motionEvent) {
        int actionMasked = motionEvent.getActionMasked();
        boolean z = true;
        if (actionMasked == 0) {
            this.mIsOnLeftEdge = motionEvent.getX() <= ((float) (this.mEdgeWidthLeft + this.mLeftInset));
            this.mLogGesture = false;
            this.mInRejectedExclusion = false;
            if (this.mDisabledForQuickstep || !this.mIsBackGestureAllowed || this.mGestureBlockingActivityRunning || QuickStepContract.isBackGestureDisabled(this.mSysUiFlags) || !isWithinTouchRegion((int) motionEvent.getX(), (int) motionEvent.getY())) {
                z = false;
            }
            this.mAllowGesture = z;
            if (z) {
                this.mEdgeBackPlugin.setIsLeftPanel(this.mIsOnLeftEdge);
                this.mEdgeBackPlugin.onMotionEvent(motionEvent);
            }
            if (this.mLogGesture) {
                this.mDownPoint.set(motionEvent.getX(), motionEvent.getY());
                this.mEndPoint.set(-1.0f, -1.0f);
                this.mThresholdCrossed = false;
            }
        } else if (this.mAllowGesture || this.mLogGesture) {
            if (!this.mThresholdCrossed) {
                this.mEndPoint.x = (float) ((int) motionEvent.getX());
                this.mEndPoint.y = (float) ((int) motionEvent.getY());
                if (actionMasked == 5) {
                    if (this.mAllowGesture) {
                        logGesture(6);
                        cancelGesture(motionEvent);
                    }
                    this.mLogGesture = false;
                    return;
                } else if (actionMasked == 2) {
                    if (motionEvent.getEventTime() - motionEvent.getDownTime() > ((long) this.mLongPressTimeout)) {
                        if (this.mAllowGesture) {
                            logGesture(7);
                            cancelGesture(motionEvent);
                        }
                        this.mLogGesture = false;
                        return;
                    }
                    float abs = Math.abs(motionEvent.getX() - this.mDownPoint.x);
                    float abs2 = Math.abs(motionEvent.getY() - this.mDownPoint.y);
                    if (abs2 > abs && abs2 > this.mTouchSlop) {
                        if (this.mAllowGesture) {
                            logGesture(8);
                            cancelGesture(motionEvent);
                        }
                        this.mLogGesture = false;
                        return;
                    } else if (abs > abs2 && abs > this.mTouchSlop) {
                        if (this.mAllowGesture) {
                            this.mThresholdCrossed = true;
                            this.mInputMonitor.pilferPointers();
                        } else {
                            logGesture(5);
                        }
                    }
                }
            }
            if (this.mAllowGesture) {
                this.mEdgeBackPlugin.onMotionEvent(motionEvent);
            }
        }
        ((ProtoTracer) Dependency.get(ProtoTracer.class)).update();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateDisabledForQuickstep() {
        int rotation = this.mContext.getResources().getConfiguration().windowConfiguration.getRotation();
        int i = this.mStartingQuickstepRotation;
        this.mDisabledForQuickstep = i > -1 && i != rotation;
    }

    public void onDisplayChanged(int i) {
        if (this.mStartingQuickstepRotation > -1) {
            updateDisabledForQuickstep();
        }
        if (i == this.mDisplayId) {
            updateDisplaySize();
        }
    }

    private void updateDisplaySize() {
        this.mContext.getDisplay().getRealSize(this.mDisplaySize);
        NavigationEdgeBackPlugin navigationEdgeBackPlugin = this.mEdgeBackPlugin;
        if (navigationEdgeBackPlugin != null) {
            navigationEdgeBackPlugin.setDisplaySize(this.mDisplaySize);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendEvent(int i, int i2) {
        long uptimeMillis = SystemClock.uptimeMillis();
        KeyEvent keyEvent = new KeyEvent(uptimeMillis, uptimeMillis, i, i2, 0, 0, -1, 0, 72, 257);
        int expandedDisplayId = ((BubbleController) Dependency.get(BubbleController.class)).getExpandedDisplayId(this.mContext);
        if (i2 == 4 && expandedDisplayId != -1) {
            keyEvent.setDisplayId(expandedDisplayId);
        }
        InputManager.getInstance().injectInputEvent(keyEvent, 0);
    }

    public void setInsets(int i, int i2) {
        this.mLeftInset = i;
        this.mRightInset = i2;
        NavigationEdgeBackPlugin navigationEdgeBackPlugin = this.mEdgeBackPlugin;
        if (navigationEdgeBackPlugin != null) {
            navigationEdgeBackPlugin.setInsets(i, i2);
        }
    }

    public void dump(PrintWriter printWriter) {
        printWriter.println("EdgeBackGestureHandler:");
        printWriter.println("  mIsEnabled=" + this.mIsEnabled);
        printWriter.println("  mIsBackGestureAllowed=" + this.mIsBackGestureAllowed);
        printWriter.println("  mAllowGesture=" + this.mAllowGesture);
        printWriter.println("  mDisabledForQuickstep=" + this.mDisabledForQuickstep);
        printWriter.println("  mStartingQuickstepRotation=" + this.mStartingQuickstepRotation);
        printWriter.println("  mInRejectedExclusion" + this.mInRejectedExclusion);
        printWriter.println("  mExcludeRegion=" + this.mExcludeRegion);
        printWriter.println("  mUnrestrictedExcludeRegion=" + this.mUnrestrictedExcludeRegion);
        printWriter.println("  mIsAttached=" + this.mIsAttached);
        printWriter.println("  mEdgeWidthLeft=" + this.mEdgeWidthLeft);
        printWriter.println("  mEdgeWidthRight=" + this.mEdgeWidthRight);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isGestureBlockingActivityRunning() {
        ComponentName componentName;
        ActivityManager.RunningTaskInfo runningTask = ActivityManagerWrapper.getInstance().getRunningTask();
        if (runningTask == null) {
            componentName = null;
        } else {
            componentName = runningTask.topActivity;
        }
        return componentName != null && this.mGestureBlockingActivities.contains(componentName);
    }

    public void writeToProto(SystemUiTraceProto systemUiTraceProto) {
        if (systemUiTraceProto.edgeBackGestureHandler == null) {
            systemUiTraceProto.edgeBackGestureHandler = new EdgeBackGestureHandlerProto();
        }
        systemUiTraceProto.edgeBackGestureHandler.allowGesture = this.mAllowGesture;
    }

    /* access modifiers changed from: package-private */
    public class SysUiInputEventReceiver extends InputEventReceiver {
        SysUiInputEventReceiver(InputChannel inputChannel, Looper looper) {
            super(inputChannel, looper);
        }

        public void onInputEvent(InputEvent inputEvent) {
            EdgeBackGestureHandler.this.onInputEvent(inputEvent);
            finishInputEvent(inputEvent, true);
        }
    }

    public void onDestroy() {
        ((ProtoTracer) Dependency.get(ProtoTracer.class)).remove(this);
        ((SysUiState) Dependency.get(SysUiState.class)).removeCallback(this.mStateCallback);
    }
}
