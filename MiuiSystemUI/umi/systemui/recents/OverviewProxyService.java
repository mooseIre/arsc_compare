package com.android.systemui.recents;

import android.app.ActivityTaskManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Insets;
import android.graphics.Rect;
import android.graphics.Region;
import android.hardware.input.InputManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.accessibility.AccessibilityManager;
import com.android.internal.accessibility.dialog.AccessibilityButtonChooserActivity;
import com.android.internal.policy.ScreenDecorationsUtils;
import com.android.internal.util.ScreenshotHelper;
import com.android.systemui.Dependency;
import com.android.systemui.Dumpable;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.controlcenter.phone.ControlPanelWindowManager;
import com.android.systemui.model.SysUiState;
import com.android.systemui.pip.PipUI;
import com.android.systemui.recents.OverviewProxyService;
import com.android.systemui.settings.CurrentUserTracker;
import com.android.systemui.shared.recents.IMiuiSystemUiProxy;
import com.android.systemui.shared.recents.IOverviewProxy;
import com.android.systemui.shared.recents.IPinnedStackAnimationListener;
import com.android.systemui.shared.recents.ISystemUiProxy;
import com.android.systemui.shared.recents.model.Task$TaskKey;
import com.android.systemui.shared.system.ActivityManagerWrapper;
import com.android.systemui.shared.system.QuickStepContract;
import com.android.systemui.stackdivider.Divider;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.NavigationBarController;
import com.android.systemui.statusbar.phone.NavigationBarFragment;
import com.android.systemui.statusbar.phone.NavigationBarView;
import com.android.systemui.statusbar.phone.NavigationModeController;
import com.android.systemui.statusbar.phone.NotificationShadeWindowController;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.phone.StatusBarWindowCallback;
import com.android.systemui.statusbar.policy.CallbackController;
import dagger.Lazy;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class OverviewProxyService extends CurrentUserTracker implements CallbackController<OverviewProxyListener>, NavigationModeController.ModeChangedListener, Dumpable {
    private Region mActiveNavBarRegion;
    private boolean mBound;
    private int mConnectionBackoffAttempts;
    private final List<OverviewProxyListener> mConnectionCallbacks = new ArrayList();
    private final Runnable mConnectionRunnable = new Runnable() {
        /* class com.android.systemui.recents.$$Lambda$OverviewProxyService$2FrwSEVJnaHX9GGsAnD2I96htxU */

        public final void run() {
            OverviewProxyService.this.internalConnectToCurrentUser();
        }
    };
    private final Context mContext;
    private int mCurrentBoundedUserId = -1;
    private final Runnable mDeferredConnectionCallback = new Runnable() {
        /* class com.android.systemui.recents.$$Lambda$OverviewProxyService$53s1j2vSUNo_EjM7u2nSTJl32gM */

        public final void run() {
            OverviewProxyService.this.lambda$new$0$OverviewProxyService();
        }
    };
    private final Optional<Divider> mDividerOptional;
    private final Handler mHandler;
    private long mInputFocusTransferStartMillis;
    private float mInputFocusTransferStartY;
    private boolean mInputFocusTransferStarted;
    private boolean mIsEnabled;
    private final BroadcastReceiver mLauncherStateChangedReceiver = new BroadcastReceiver() {
        /* class com.android.systemui.recents.OverviewProxyService.AnonymousClass2 */

        public void onReceive(Context context, Intent intent) {
            OverviewProxyService.this.updateEnabledState();
            OverviewProxyService.this.startConnectionToCurrentUser();
        }
    };
    private IMiuiSystemUiProxy mMiuiSysUiProxy = new MiuiOverviewProxy(this);
    private float mNavBarButtonAlpha;
    private final NavigationBarController mNavBarController;
    private int mNavBarMode = 0;
    private IOverviewProxy mOverviewProxy;
    private final ServiceConnection mOverviewServiceConnection = new ServiceConnection() {
        /* class com.android.systemui.recents.OverviewProxyService.AnonymousClass3 */

        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            OverviewProxyService.this.mConnectionBackoffAttempts = 0;
            OverviewProxyService.this.mHandler.removeCallbacks(OverviewProxyService.this.mDeferredConnectionCallback);
            try {
                iBinder.linkToDeath(OverviewProxyService.this.mOverviewServiceDeathRcpt, 0);
                OverviewProxyService overviewProxyService = OverviewProxyService.this;
                overviewProxyService.mCurrentBoundedUserId = overviewProxyService.getCurrentUserId();
                OverviewProxyService.this.mOverviewProxy = IOverviewProxy.Stub.asInterface(iBinder);
                Bundle bundle = new Bundle();
                bundle.putBinder("extra_sysui_proxy", OverviewProxyService.this.mSysUiProxy.asBinder());
                bundle.putBinder("extra_miui_sysui_proxy", OverviewProxyService.this.mMiuiSysUiProxy.asBinder());
                bundle.putFloat("extra_window_corner_radius", OverviewProxyService.this.mWindowCornerRadius);
                bundle.putBoolean("extra_supports_window_corners", OverviewProxyService.this.mSupportsRoundedCornersOnWindows);
                try {
                    OverviewProxyService.this.mOverviewProxy.onInitialize(bundle);
                } catch (RemoteException e) {
                    OverviewProxyService.this.mCurrentBoundedUserId = -1;
                    Log.e("OverviewProxyService", "Failed to call onInitialize()", e);
                }
                OverviewProxyService.this.dispatchNavButtonBounds();
                OverviewProxyService.this.updateSystemUiStateFlags();
                OverviewProxyService overviewProxyService2 = OverviewProxyService.this;
                overviewProxyService2.notifySystemUiStateFlags(overviewProxyService2.mSysUiState.getFlags());
                OverviewProxyService.this.notifyConnectionChanged();
            } catch (RemoteException e2) {
                Log.e("OverviewProxyService", "Lost connection to launcher service", e2);
                OverviewProxyService.this.disconnectFromLauncherService();
                OverviewProxyService.this.retryConnectionWithBackoff();
            }
        }

        public void onNullBinding(ComponentName componentName) {
            Log.w("OverviewProxyService", "Null binding of '" + componentName + "', try reconnecting");
            OverviewProxyService.this.mCurrentBoundedUserId = -1;
            OverviewProxyService.this.retryConnectionWithBackoff();
        }

        public void onBindingDied(ComponentName componentName) {
            Log.w("OverviewProxyService", "Binding died of '" + componentName + "', try reconnecting");
            OverviewProxyService.this.mCurrentBoundedUserId = -1;
            OverviewProxyService.this.retryConnectionWithBackoff();
        }

        public void onServiceDisconnected(ComponentName componentName) {
            OverviewProxyService.this.mCurrentBoundedUserId = -1;
        }
    };
    private final IBinder.DeathRecipient mOverviewServiceDeathRcpt = new IBinder.DeathRecipient() {
        /* class com.android.systemui.recents.$$Lambda$FF1twVzMKp_FAsQO2IsbqUbCbs */

        public final void binderDied() {
            OverviewProxyService.this.cleanupAfterDeath();
        }
    };
    private final PipUI mPipUI;
    private final Intent mQuickStepIntent;
    private final ComponentName mRecentsComponentName;
    private final ScreenshotHelper mScreenshotHelper;
    private final Optional<Lazy<StatusBar>> mStatusBarOptionalLazy;
    private final NotificationShadeWindowController mStatusBarWinController;
    private final StatusBarWindowCallback mStatusBarWindowCallback = new StatusBarWindowCallback() {
        /* class com.android.systemui.recents.$$Lambda$OverviewProxyService$b7uhSpdl46tRQQQT8ZW7Bieyg6A */

        @Override // com.android.systemui.statusbar.phone.StatusBarWindowCallback
        public final void onStateChanged(boolean z, boolean z2, boolean z3) {
            OverviewProxyService.this.onStatusBarStateChanged(z, z2, z3);
        }
    };
    private boolean mSupportsRoundedCornersOnWindows;
    private ISystemUiProxy mSysUiProxy = new ISystemUiProxy.Stub() {
        /* class com.android.systemui.recents.OverviewProxyService.AnonymousClass1 */

        @Override // com.android.systemui.shared.recents.ISystemUiProxy
        public void handleImageAsScreenshot(Bitmap bitmap, Rect rect, Insets insets, int i) {
        }

        @Override // com.android.systemui.shared.recents.ISystemUiProxy
        public void startScreenPinning(int i) {
            if (verifyCaller("startScreenPinning")) {
                long clearCallingIdentity = Binder.clearCallingIdentity();
                try {
                    OverviewProxyService.this.mHandler.post(new Runnable(i) {
                        /* class com.android.systemui.recents.$$Lambda$OverviewProxyService$1$4SXWj0CMroT_CN5fJJLswjoG60 */
                        public final /* synthetic */ int f$1;

                        {
                            this.f$1 = r2;
                        }

                        public final void run() {
                            OverviewProxyService.AnonymousClass1.this.lambda$startScreenPinning$1$OverviewProxyService$1(this.f$1);
                        }
                    });
                } finally {
                    Binder.restoreCallingIdentity(clearCallingIdentity);
                }
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$startScreenPinning$1 */
        public /* synthetic */ void lambda$startScreenPinning$1$OverviewProxyService$1(int i) {
            OverviewProxyService.this.mStatusBarOptionalLazy.ifPresent(new Consumer(i) {
                /* class com.android.systemui.recents.$$Lambda$OverviewProxyService$1$L6GammKdHWnk5GdqkgMLveN3ScI */
                public final /* synthetic */ int f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    ((StatusBar) ((Lazy) obj).get()).showScreenPinningRequest(this.f$0, false);
                }
            });
        }

        @Override // com.android.systemui.shared.recents.ISystemUiProxy
        public void stopScreenPinning() {
            if (verifyCaller("stopScreenPinning")) {
                long clearCallingIdentity = Binder.clearCallingIdentity();
                try {
                    OverviewProxyService.this.mHandler.post($$Lambda$OverviewProxyService$1$9uERjvGI5cZ0Wh2SqRhoEXg8wYk.INSTANCE);
                } finally {
                    Binder.restoreCallingIdentity(clearCallingIdentity);
                }
            }
        }

        static /* synthetic */ void lambda$stopScreenPinning$2() {
            try {
                ActivityTaskManager.getService().stopSystemLockTaskMode();
            } catch (RemoteException unused) {
                Log.e("OverviewProxyService", "Failed to stop screen pinning");
            }
        }

        @Override // com.android.systemui.shared.recents.ISystemUiProxy
        public void onStatusBarMotionEvent(MotionEvent motionEvent) {
            if (verifyCaller("onStatusBarMotionEvent")) {
                long clearCallingIdentity = Binder.clearCallingIdentity();
                try {
                    OverviewProxyService.this.mStatusBarOptionalLazy.ifPresent(new Consumer(motionEvent) {
                        /* class com.android.systemui.recents.$$Lambda$OverviewProxyService$1$sWJd6osdRGJYQOQo8d7uwS1fPg */
                        public final /* synthetic */ MotionEvent f$1;

                        {
                            this.f$1 = r2;
                        }

                        @Override // java.util.function.Consumer
                        public final void accept(Object obj) {
                            OverviewProxyService.AnonymousClass1.this.lambda$onStatusBarMotionEvent$4$OverviewProxyService$1(this.f$1, (Lazy) obj);
                        }
                    });
                } finally {
                    Binder.restoreCallingIdentity(clearCallingIdentity);
                }
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onStatusBarMotionEvent$4 */
        public /* synthetic */ void lambda$onStatusBarMotionEvent$4$OverviewProxyService$1(MotionEvent motionEvent, Lazy lazy) {
            OverviewProxyService.this.mHandler.post(new Runnable(lazy, motionEvent) {
                /* class com.android.systemui.recents.$$Lambda$OverviewProxyService$1$BGnwKTIGrmtYHxNv2XOO0ioZPVE */
                public final /* synthetic */ Lazy f$1;
                public final /* synthetic */ MotionEvent f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                public final void run() {
                    OverviewProxyService.AnonymousClass1.this.lambda$onStatusBarMotionEvent$3$OverviewProxyService$1(this.f$1, this.f$2);
                }
            });
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onStatusBarMotionEvent$3 */
        public /* synthetic */ void lambda$onStatusBarMotionEvent$3$OverviewProxyService$1(Lazy lazy, MotionEvent motionEvent) {
            StatusBar statusBar = (StatusBar) lazy.get();
            int actionMasked = motionEvent.getActionMasked();
            if (!judgeInterceptByMiui(motionEvent, statusBar)) {
                boolean z = false;
                if (actionMasked == 0) {
                    OverviewProxyService.this.mInputFocusTransferStarted = true;
                    OverviewProxyService.this.mInputFocusTransferStartY = motionEvent.getY();
                    OverviewProxyService.this.mInputFocusTransferStartMillis = motionEvent.getEventTime();
                    statusBar.onInputFocusTransfer(OverviewProxyService.this.mInputFocusTransferStarted, false, 0.0f);
                }
                if (actionMasked == 1 || actionMasked == 3) {
                    OverviewProxyService.this.mInputFocusTransferStarted = false;
                    boolean z2 = OverviewProxyService.this.mInputFocusTransferStarted;
                    if (actionMasked == 3) {
                        z = true;
                    }
                    statusBar.onInputFocusTransfer(z2, z, (motionEvent.getY() - OverviewProxyService.this.mInputFocusTransferStartY) / ((float) (motionEvent.getEventTime() - OverviewProxyService.this.mInputFocusTransferStartMillis)));
                }
            }
            motionEvent.recycle();
        }

        public boolean judgeInterceptByMiui(MotionEvent motionEvent, StatusBar statusBar) {
            return (motionEvent.getActionMasked() == 3 || statusBar.getStatusBarWindow() == null || !((ControlPanelWindowManager) Dependency.get(ControlPanelWindowManager.class)).dispatchToControlPanel(motionEvent, (float) statusBar.getStatusBarWindow().getWidth())) ? false : true;
        }

        @Override // com.android.systemui.shared.recents.ISystemUiProxy
        public void onSplitScreenInvoked() {
            if (verifyCaller("onSplitScreenInvoked")) {
                long clearCallingIdentity = Binder.clearCallingIdentity();
                try {
                    OverviewProxyService.this.mDividerOptional.ifPresent($$Lambda$xuXEcdh0HmTmuN4e7qU9mBkM36M.INSTANCE);
                } finally {
                    Binder.restoreCallingIdentity(clearCallingIdentity);
                }
            }
        }

        @Override // com.android.systemui.shared.recents.ISystemUiProxy
        public void onOverviewShown(boolean z) {
            if (verifyCaller("onOverviewShown")) {
                long clearCallingIdentity = Binder.clearCallingIdentity();
                try {
                    OverviewProxyService.this.mHandler.post(new Runnable(z) {
                        /* class com.android.systemui.recents.$$Lambda$OverviewProxyService$1$4FVtgzFdKl6xTtmcCi3ZqBrQngY */
                        public final /* synthetic */ boolean f$1;

                        {
                            this.f$1 = r2;
                        }

                        public final void run() {
                            OverviewProxyService.AnonymousClass1.this.lambda$onOverviewShown$5$OverviewProxyService$1(this.f$1);
                        }
                    });
                } finally {
                    Binder.restoreCallingIdentity(clearCallingIdentity);
                }
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onOverviewShown$5 */
        public /* synthetic */ void lambda$onOverviewShown$5$OverviewProxyService$1(boolean z) {
            for (int size = OverviewProxyService.this.mConnectionCallbacks.size() - 1; size >= 0; size--) {
                ((OverviewProxyListener) OverviewProxyService.this.mConnectionCallbacks.get(size)).onOverviewShown(z);
            }
        }

        @Override // com.android.systemui.shared.recents.ISystemUiProxy
        public Rect getNonMinimizedSplitScreenSecondaryBounds() {
            if (!verifyCaller("getNonMinimizedSplitScreenSecondaryBounds")) {
                return null;
            }
            long clearCallingIdentity = Binder.clearCallingIdentity();
            try {
                return (Rect) OverviewProxyService.this.mDividerOptional.map($$Lambda$OverviewProxyService$1$jWyXSUssf3YIGp2Ozuegdbo3RQM.INSTANCE).orElse(null);
            } finally {
                Binder.restoreCallingIdentity(clearCallingIdentity);
            }
        }

        @Override // com.android.systemui.shared.recents.ISystemUiProxy
        public void setNavBarButtonAlpha(float f, boolean z) {
            if (verifyCaller("setNavBarButtonAlpha")) {
                long clearCallingIdentity = Binder.clearCallingIdentity();
                try {
                    OverviewProxyService.this.mNavBarButtonAlpha = f;
                    OverviewProxyService.this.mHandler.post(new Runnable(f, z) {
                        /* class com.android.systemui.recents.$$Lambda$OverviewProxyService$1$JeSA_8M36F8qXtmvuNJjrSs1GE */
                        public final /* synthetic */ float f$1;
                        public final /* synthetic */ boolean f$2;

                        {
                            this.f$1 = r2;
                            this.f$2 = r3;
                        }

                        public final void run() {
                            OverviewProxyService.AnonymousClass1.this.lambda$setNavBarButtonAlpha$7$OverviewProxyService$1(this.f$1, this.f$2);
                        }
                    });
                } finally {
                    Binder.restoreCallingIdentity(clearCallingIdentity);
                }
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$setNavBarButtonAlpha$7 */
        public /* synthetic */ void lambda$setNavBarButtonAlpha$7$OverviewProxyService$1(float f, boolean z) {
            OverviewProxyService.this.notifyNavBarButtonAlphaChanged(f, z);
        }

        @Override // com.android.systemui.shared.recents.ISystemUiProxy
        public void setBackButtonAlpha(float f, boolean z) {
            setNavBarButtonAlpha(f, z);
        }

        @Override // com.android.systemui.shared.recents.ISystemUiProxy
        public void onAssistantProgress(float f) {
            if (verifyCaller("onAssistantProgress")) {
                long clearCallingIdentity = Binder.clearCallingIdentity();
                try {
                    OverviewProxyService.this.mHandler.post(new Runnable(f) {
                        /* class com.android.systemui.recents.$$Lambda$OverviewProxyService$1$x61OGopTSUwfaMsTcGtlGuJLnog */
                        public final /* synthetic */ float f$1;

                        {
                            this.f$1 = r2;
                        }

                        public final void run() {
                            OverviewProxyService.AnonymousClass1.this.lambda$onAssistantProgress$8$OverviewProxyService$1(this.f$1);
                        }
                    });
                } finally {
                    Binder.restoreCallingIdentity(clearCallingIdentity);
                }
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onAssistantProgress$8 */
        public /* synthetic */ void lambda$onAssistantProgress$8$OverviewProxyService$1(float f) {
            OverviewProxyService.this.notifyAssistantProgress(f);
        }

        @Override // com.android.systemui.shared.recents.ISystemUiProxy
        public void onAssistantGestureCompletion(float f) {
            if (verifyCaller("onAssistantGestureCompletion")) {
                long clearCallingIdentity = Binder.clearCallingIdentity();
                try {
                    OverviewProxyService.this.mHandler.post(new Runnable(f) {
                        /* class com.android.systemui.recents.$$Lambda$OverviewProxyService$1$9Su7WjOgAjqw2JOgBqgqoRaUIY */
                        public final /* synthetic */ float f$1;

                        {
                            this.f$1 = r2;
                        }

                        public final void run() {
                            OverviewProxyService.AnonymousClass1.this.lambda$onAssistantGestureCompletion$9$OverviewProxyService$1(this.f$1);
                        }
                    });
                } finally {
                    Binder.restoreCallingIdentity(clearCallingIdentity);
                }
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onAssistantGestureCompletion$9 */
        public /* synthetic */ void lambda$onAssistantGestureCompletion$9$OverviewProxyService$1(float f) {
            OverviewProxyService.this.notifyAssistantGestureCompletion(f);
        }

        @Override // com.android.systemui.shared.recents.ISystemUiProxy
        public void startAssistant(Bundle bundle) {
            if (verifyCaller("startAssistant")) {
                long clearCallingIdentity = Binder.clearCallingIdentity();
                try {
                    OverviewProxyService.this.mHandler.post(new Runnable(bundle) {
                        /* class com.android.systemui.recents.$$Lambda$OverviewProxyService$1$daY2UqZd3NsPXvIP8cYjYZLB70 */
                        public final /* synthetic */ Bundle f$1;

                        {
                            this.f$1 = r2;
                        }

                        public final void run() {
                            OverviewProxyService.AnonymousClass1.this.lambda$startAssistant$10$OverviewProxyService$1(this.f$1);
                        }
                    });
                } finally {
                    Binder.restoreCallingIdentity(clearCallingIdentity);
                }
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$startAssistant$10 */
        public /* synthetic */ void lambda$startAssistant$10$OverviewProxyService$1(Bundle bundle) {
            OverviewProxyService.this.notifyStartAssistant(bundle);
        }

        @Override // com.android.systemui.shared.recents.ISystemUiProxy
        public Bundle monitorGestureInput(String str, int i) {
            if (!verifyCaller("monitorGestureInput")) {
                return null;
            }
            long clearCallingIdentity = Binder.clearCallingIdentity();
            try {
                Parcelable monitorGestureInput = InputManager.getInstance().monitorGestureInput(str, i);
                Bundle bundle = new Bundle();
                bundle.putParcelable("extra_input_monitor", monitorGestureInput);
                return bundle;
            } finally {
                Binder.restoreCallingIdentity(clearCallingIdentity);
            }
        }

        @Override // com.android.systemui.shared.recents.ISystemUiProxy
        public void notifyAccessibilityButtonClicked(int i) {
            if (verifyCaller("notifyAccessibilityButtonClicked")) {
                long clearCallingIdentity = Binder.clearCallingIdentity();
                try {
                    AccessibilityManager.getInstance(OverviewProxyService.this.mContext).notifyAccessibilityButtonClicked(i);
                } finally {
                    Binder.restoreCallingIdentity(clearCallingIdentity);
                }
            }
        }

        @Override // com.android.systemui.shared.recents.ISystemUiProxy
        public void notifyAccessibilityButtonLongClicked() {
            if (verifyCaller("notifyAccessibilityButtonLongClicked")) {
                long clearCallingIdentity = Binder.clearCallingIdentity();
                try {
                    Intent intent = new Intent("com.android.internal.intent.action.CHOOSE_ACCESSIBILITY_BUTTON");
                    intent.setClassName("android", AccessibilityButtonChooserActivity.class.getName());
                    intent.addFlags(268468224);
                    OverviewProxyService.this.mContext.startActivityAsUser(intent, UserHandle.CURRENT);
                } finally {
                    Binder.restoreCallingIdentity(clearCallingIdentity);
                }
            }
        }

        @Override // com.android.systemui.shared.recents.ISystemUiProxy
        public void setShelfHeight(boolean z, int i) {
            if (verifyCaller("setShelfHeight")) {
                long clearCallingIdentity = Binder.clearCallingIdentity();
                try {
                    OverviewProxyService.this.mPipUI.setShelfHeight(z, i);
                } finally {
                    Binder.restoreCallingIdentity(clearCallingIdentity);
                }
            }
        }

        @Override // com.android.systemui.shared.recents.ISystemUiProxy
        public void setSplitScreenMinimized(boolean z) {
            Divider divider = (Divider) OverviewProxyService.this.mDividerOptional.get();
            if (divider != null) {
                divider.setMinimized(z);
            }
        }

        @Override // com.android.systemui.shared.recents.ISystemUiProxy
        public void notifySwipeToHomeFinished() {
            if (verifyCaller("notifySwipeToHomeFinished")) {
                long clearCallingIdentity = Binder.clearCallingIdentity();
                try {
                    OverviewProxyService.this.mPipUI.setPinnedStackAnimationType(1);
                } finally {
                    Binder.restoreCallingIdentity(clearCallingIdentity);
                }
            }
        }

        @Override // com.android.systemui.shared.recents.ISystemUiProxy
        public void setPinnedStackAnimationListener(IPinnedStackAnimationListener iPinnedStackAnimationListener) {
            if (verifyCaller("setPinnedStackAnimationListener")) {
                long clearCallingIdentity = Binder.clearCallingIdentity();
                try {
                    OverviewProxyService.this.mPipUI.setPinnedStackAnimationListener(iPinnedStackAnimationListener);
                } finally {
                    Binder.restoreCallingIdentity(clearCallingIdentity);
                }
            }
        }

        @Override // com.android.systemui.shared.recents.ISystemUiProxy
        public void onQuickSwitchToNewTask(int i) {
            if (verifyCaller("onQuickSwitchToNewTask")) {
                long clearCallingIdentity = Binder.clearCallingIdentity();
                try {
                    OverviewProxyService.this.mHandler.post(new Runnable(i) {
                        /* class com.android.systemui.recents.$$Lambda$OverviewProxyService$1$tuK3db_PKF7Z0p3Tp2hFfoHgKRU */
                        public final /* synthetic */ int f$1;

                        {
                            this.f$1 = r2;
                        }

                        public final void run() {
                            OverviewProxyService.AnonymousClass1.this.lambda$onQuickSwitchToNewTask$11$OverviewProxyService$1(this.f$1);
                        }
                    });
                } finally {
                    Binder.restoreCallingIdentity(clearCallingIdentity);
                }
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onQuickSwitchToNewTask$11 */
        public /* synthetic */ void lambda$onQuickSwitchToNewTask$11$OverviewProxyService$1(int i) {
            OverviewProxyService.this.notifyQuickSwitchToNewTask(i);
        }

        @Override // com.android.systemui.shared.recents.ISystemUiProxy
        public void handleImageBundleAsScreenshot(Bundle bundle, Rect rect, Insets insets, Task$TaskKey task$TaskKey) {
            OverviewProxyService.this.mScreenshotHelper.provideScreenshot(bundle, rect, insets, task$TaskKey.id, task$TaskKey.userId, task$TaskKey.sourceComponent, 3, OverviewProxyService.this.mHandler, (Consumer) null);
        }

        private boolean verifyCaller(String str) {
            int identifier = Binder.getCallingUserHandle().getIdentifier();
            if (identifier == OverviewProxyService.this.mCurrentBoundedUserId) {
                return true;
            }
            Log.w("OverviewProxyService", "Launcher called sysui with invalid user: " + identifier + ", reason: " + str);
            return false;
        }
    };
    private SysUiState mSysUiState;
    private float mWindowCornerRadius;

    public interface OverviewProxyListener {
        default void onAssistantGestureCompletion(float f) {
        }

        default void onAssistantProgress(float f) {
        }

        default void onConnectionChanged(boolean z) {
        }

        default void onNavBarButtonAlphaChanged(float f, boolean z) {
        }

        default void onOverviewShown(boolean z) {
        }

        default void onQuickSwitchToNewTask(int i) {
        }

        default void onToggleRecentApps() {
        }

        default void startAssistant(Bundle bundle) {
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$0 */
    public /* synthetic */ void lambda$new$0$OverviewProxyService() {
        Log.w("OverviewProxyService", "Binder supposed established connection but actual connection to service timed out, trying again");
        retryConnectionWithBackoff();
    }

    public OverviewProxyService(Context context, CommandQueue commandQueue, NavigationBarController navigationBarController, NavigationModeController navigationModeController, NotificationShadeWindowController notificationShadeWindowController, SysUiState sysUiState, PipUI pipUI, Optional<Divider> optional, Optional<Lazy<StatusBar>> optional2, BroadcastDispatcher broadcastDispatcher) {
        super(broadcastDispatcher);
        this.mContext = context;
        this.mPipUI = pipUI;
        this.mStatusBarOptionalLazy = optional2;
        this.mHandler = new Handler();
        this.mNavBarController = navigationBarController;
        this.mStatusBarWinController = notificationShadeWindowController;
        this.mConnectionBackoffAttempts = 0;
        this.mDividerOptional = optional;
        this.mRecentsComponentName = ComponentName.unflattenFromString(context.getString(17039966));
        this.mQuickStepIntent = new Intent("android.intent.action.QUICKSTEP_SERVICE").setPackage(this.mRecentsComponentName.getPackageName());
        this.mWindowCornerRadius = ScreenDecorationsUtils.getWindowCornerRadius(this.mContext.getResources());
        this.mSupportsRoundedCornersOnWindows = ScreenDecorationsUtils.supportsRoundedCornersOnWindows(this.mContext.getResources());
        this.mSysUiState = sysUiState;
        sysUiState.addCallback(new SysUiState.SysUiStateCallback() {
            /* class com.android.systemui.recents.$$Lambda$OverviewProxyService$UsZDbsgQ2Qpz6L03F4TRLuFj_w */

            @Override // com.android.systemui.model.SysUiState.SysUiStateCallback
            public final void onSystemUiStateChanged(int i) {
                OverviewProxyService.this.notifySystemUiStateFlags(i);
            }
        });
        this.mNavBarButtonAlpha = 1.0f;
        this.mNavBarMode = navigationModeController.addListener(this);
        IntentFilter intentFilter = new IntentFilter("android.intent.action.PACKAGE_ADDED");
        intentFilter.addDataScheme("package");
        intentFilter.addDataSchemeSpecificPart(this.mRecentsComponentName.getPackageName(), 0);
        intentFilter.addAction("android.intent.action.PACKAGE_CHANGED");
        this.mContext.registerReceiver(this.mLauncherStateChangedReceiver, intentFilter);
        notificationShadeWindowController.registerCallback(this.mStatusBarWindowCallback);
        this.mScreenshotHelper = new ScreenshotHelper(context);
        commandQueue.addCallback((CommandQueue.Callbacks) new CommandQueue.Callbacks() {
            /* class com.android.systemui.recents.OverviewProxyService.AnonymousClass4 */

            @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
            public void onTracingStateChanged(boolean z) {
                SysUiState sysUiState = OverviewProxyService.this.mSysUiState;
                sysUiState.setFlag(4096, z);
                sysUiState.commitUpdate(OverviewProxyService.this.mContext.getDisplayId());
            }

            @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
            public void setWindowState(int i, int i2, int i3) {
                if (OverviewProxyService.this.mContext.getDisplayId() == i) {
                    MiuiRecentProxy.setWindowStateInject(OverviewProxyService.this.mSysUiState, i, i2, i3);
                }
            }
        });
        startTracking();
        updateEnabledState();
        startConnectionToCurrentUser();
    }

    @Override // com.android.systemui.settings.CurrentUserTracker
    public void onUserSwitched(int i) {
        this.mConnectionBackoffAttempts = 0;
        internalConnectToCurrentUser();
    }

    public void notifyBackAction(boolean z, int i, int i2, boolean z2, boolean z3) {
        try {
            if (this.mOverviewProxy != null) {
                this.mOverviewProxy.onBackAction(z, i, i2, z2, z3);
            }
        } catch (RemoteException e) {
            Log.e("OverviewProxyService", "Failed to notify back action", e);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateSystemUiStateFlags() {
        NavigationBarFragment defaultNavigationBarFragment = this.mNavBarController.getDefaultNavigationBarFragment();
        NavigationBarView navigationBarView = this.mNavBarController.getNavigationBarView(this.mContext.getDisplayId());
        if (defaultNavigationBarFragment != null) {
            defaultNavigationBarFragment.updateSystemUiStateFlags(-1);
        }
        if (navigationBarView != null) {
            navigationBarView.updatePanelSystemUiStateFlags();
            navigationBarView.updateDisabledSystemUiStateFlags();
        }
        NotificationShadeWindowController notificationShadeWindowController = this.mStatusBarWinController;
        if (notificationShadeWindowController != null) {
            notificationShadeWindowController.notifyStateChangedCallbacks();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    public void notifySystemUiStateFlags(int i) {
        try {
            if (this.mOverviewProxy != null) {
                this.mOverviewProxy.onSystemUiStateChanged(i);
            }
        } catch (RemoteException e) {
            Log.e("OverviewProxyService", "Failed to notify sysui state change", e);
        }
    }

    /* access modifiers changed from: private */
    public void onStatusBarStateChanged(boolean z, boolean z2, boolean z3) {
        SysUiState sysUiState = this.mSysUiState;
        boolean z4 = true;
        sysUiState.setFlag(64, z && !z2);
        if (!z || !z2) {
            z4 = false;
        }
        sysUiState.setFlag(512, z4);
        sysUiState.setFlag(8, z3);
        sysUiState.commitUpdate(this.mContext.getDisplayId());
    }

    public void onActiveNavBarRegionChanges(Region region) {
        this.mActiveNavBarRegion = region;
        dispatchNavButtonBounds();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dispatchNavButtonBounds() {
        Region region;
        IOverviewProxy iOverviewProxy = this.mOverviewProxy;
        if (iOverviewProxy != null && (region = this.mActiveNavBarRegion) != null) {
            try {
                iOverviewProxy.onActiveNavBarRegionChanges(region);
            } catch (RemoteException e) {
                Log.e("OverviewProxyService", "Failed to call onActiveNavBarRegionChanges()", e);
            }
        }
    }

    public void cleanupAfterDeath() {
        if (this.mInputFocusTransferStarted) {
            this.mHandler.post(new Runnable() {
                /* class com.android.systemui.recents.$$Lambda$OverviewProxyService$PSR8w04DgkmYl0QS7DaTBJbM_iU */

                public final void run() {
                    OverviewProxyService.this.lambda$cleanupAfterDeath$2$OverviewProxyService();
                }
            });
        }
        startConnectionToCurrentUser();
        Divider divider = this.mDividerOptional.get();
        if (divider != null) {
            divider.setMinimized(false);
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$cleanupAfterDeath$2 */
    public /* synthetic */ void lambda$cleanupAfterDeath$2$OverviewProxyService() {
        this.mStatusBarOptionalLazy.ifPresent(new Consumer() {
            /* class com.android.systemui.recents.$$Lambda$OverviewProxyService$r1ukwXYi8j1mxwBUvifNk9B4ue4 */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                OverviewProxyService.this.lambda$cleanupAfterDeath$1$OverviewProxyService((Lazy) obj);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$cleanupAfterDeath$1 */
    public /* synthetic */ void lambda$cleanupAfterDeath$1$OverviewProxyService(Lazy lazy) {
        this.mInputFocusTransferStarted = false;
        ((StatusBar) lazy.get()).onInputFocusTransfer(false, true, 0.0f);
    }

    public void startConnectionToCurrentUser() {
        if (this.mHandler.getLooper() != Looper.myLooper()) {
            this.mHandler.post(this.mConnectionRunnable);
        } else {
            internalConnectToCurrentUser();
        }
    }

    /* access modifiers changed from: private */
    public void internalConnectToCurrentUser() {
        disconnectFromLauncherService();
        if (!isEnabled()) {
            Log.v("OverviewProxyService", "Cannot attempt connection, is enabled " + isEnabled());
            return;
        }
        this.mHandler.removeCallbacks(this.mConnectionRunnable);
        try {
            this.mBound = this.mContext.bindServiceAsUser(new Intent("android.intent.action.QUICKSTEP_SERVICE").setPackage(this.mRecentsComponentName.getPackageName()), this.mOverviewServiceConnection, 33554433, UserHandle.of(getCurrentUserId()));
        } catch (SecurityException e) {
            Log.e("OverviewProxyService", "Unable to bind because of security error", e);
        }
        if (this.mBound) {
            this.mHandler.postDelayed(this.mDeferredConnectionCallback, 5000);
        } else {
            retryConnectionWithBackoff();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void retryConnectionWithBackoff() {
        if (!this.mHandler.hasCallbacks(this.mConnectionRunnable)) {
            long min = (long) Math.min(Math.scalb(1000.0f, this.mConnectionBackoffAttempts), 600000.0f);
            this.mHandler.postDelayed(this.mConnectionRunnable, min);
            this.mConnectionBackoffAttempts++;
            Log.w("OverviewProxyService", "Failed to connect on attempt " + this.mConnectionBackoffAttempts + " will try again in " + min + "ms");
        }
    }

    public void addCallback(OverviewProxyListener overviewProxyListener) {
        this.mConnectionCallbacks.add(overviewProxyListener);
        overviewProxyListener.onConnectionChanged(this.mOverviewProxy != null);
        overviewProxyListener.onNavBarButtonAlphaChanged(this.mNavBarButtonAlpha, false);
    }

    public void removeCallback(OverviewProxyListener overviewProxyListener) {
        this.mConnectionCallbacks.remove(overviewProxyListener);
    }

    public boolean shouldShowSwipeUpUI() {
        return isEnabled() && !QuickStepContract.isLegacyMode(this.mNavBarMode);
    }

    public boolean isEnabled() {
        return this.mIsEnabled;
    }

    public IOverviewProxy getProxy() {
        return this.mOverviewProxy;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void disconnectFromLauncherService() {
        if (this.mBound) {
            this.mContext.unbindService(this.mOverviewServiceConnection);
            this.mBound = false;
        }
        IOverviewProxy iOverviewProxy = this.mOverviewProxy;
        if (iOverviewProxy != null) {
            iOverviewProxy.asBinder().unlinkToDeath(this.mOverviewServiceDeathRcpt, 0);
            this.mOverviewProxy = null;
            notifyNavBarButtonAlphaChanged(1.0f, false);
            notifyConnectionChanged();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyNavBarButtonAlphaChanged(float f, boolean z) {
        for (int size = this.mConnectionCallbacks.size() - 1; size >= 0; size--) {
            this.mConnectionCallbacks.get(size).onNavBarButtonAlphaChanged(f, z);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyConnectionChanged() {
        for (int size = this.mConnectionCallbacks.size() - 1; size >= 0; size--) {
            this.mConnectionCallbacks.get(size).onConnectionChanged(this.mOverviewProxy != null);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyQuickSwitchToNewTask(int i) {
        for (int size = this.mConnectionCallbacks.size() - 1; size >= 0; size--) {
            this.mConnectionCallbacks.get(size).onQuickSwitchToNewTask(i);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyAssistantProgress(float f) {
        for (int size = this.mConnectionCallbacks.size() - 1; size >= 0; size--) {
            this.mConnectionCallbacks.get(size).onAssistantProgress(f);
        }
    }

    public void notifyAssistantGestureCompletion(float f) {
        for (int size = this.mConnectionCallbacks.size() - 1; size >= 0; size--) {
            this.mConnectionCallbacks.get(size).onAssistantGestureCompletion(f);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyStartAssistant(Bundle bundle) {
        for (int size = this.mConnectionCallbacks.size() - 1; size >= 0; size--) {
            this.mConnectionCallbacks.get(size).startAssistant(bundle);
        }
    }

    public void notifySplitScreenBoundsChanged(Rect rect, Rect rect2) {
        try {
            if (this.mOverviewProxy != null) {
                this.mOverviewProxy.onSplitScreenSecondaryBoundsChanged(rect, rect2);
            } else {
                Log.e("OverviewProxyService", "Failed to get overview proxy for split screen bounds.");
            }
        } catch (RemoteException e) {
            Log.e("OverviewProxyService", "Failed to call onSplitScreenSecondaryBoundsChanged()", e);
        }
    }

    /* access modifiers changed from: package-private */
    public void notifyToggleRecentApps() {
        for (int size = this.mConnectionCallbacks.size() - 1; size >= 0; size--) {
            this.mConnectionCallbacks.get(size).onToggleRecentApps();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateEnabledState() {
        this.mIsEnabled = this.mContext.getPackageManager().resolveServiceAsUser(this.mQuickStepIntent, 1048576, ActivityManagerWrapper.getInstance().getCurrentUserId()) != null;
    }

    @Override // com.android.systemui.statusbar.phone.NavigationModeController.ModeChangedListener
    public void onNavigationModeChanged(int i) {
        this.mNavBarMode = i;
    }

    @Override // com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("OverviewProxyService state:");
        printWriter.print("  recentsComponentName=");
        printWriter.println(this.mRecentsComponentName);
        printWriter.print("  isConnected=");
        printWriter.println(this.mOverviewProxy != null);
        printWriter.print("  connectionBackoffAttempts=");
        printWriter.println(this.mConnectionBackoffAttempts);
        printWriter.print("  quickStepIntent=");
        printWriter.println(this.mQuickStepIntent);
        printWriter.print("  quickStepIntentResolved=");
        printWriter.println(isEnabled());
        this.mSysUiState.dump(fileDescriptor, printWriter, strArr);
        printWriter.print(" mInputFocusTransferStarted=");
        printWriter.println(this.mInputFocusTransferStarted);
    }

    public Optional<Divider> getDividerOptional() {
        return this.mDividerOptional;
    }

    public Handler getHandler() {
        return this.mHandler;
    }

    public int getCurrentBoundedUserId() {
        return this.mCurrentBoundedUserId;
    }
}
