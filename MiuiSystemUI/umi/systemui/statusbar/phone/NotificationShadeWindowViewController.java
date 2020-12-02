package com.android.systemui.statusbar.phone;

import android.graphics.RectF;
import android.media.session.MediaSessionLegacyHelper;
import android.os.SystemClock;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.C0015R$id;
import com.android.systemui.Dependency;
import com.android.systemui.controlcenter.phone.ControlPanelWindowManager;
import com.android.systemui.dock.DockManager;
import com.android.systemui.doze.DozeLog;
import com.android.systemui.plugins.FalsingManager;
import com.android.systemui.shared.plugins.PluginManager;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.DragDownHelper;
import com.android.systemui.statusbar.NotificationLockscreenUserManager;
import com.android.systemui.statusbar.NotificationShadeDepthController;
import com.android.systemui.statusbar.PulseExpansionHandler;
import com.android.systemui.statusbar.SuperStatusBarViewFactory;
import com.android.systemui.statusbar.SysuiStatusBarStateController;
import com.android.systemui.statusbar.notification.DynamicPrivacyController;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.NotificationWakeUpCoordinator;
import com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout;
import com.android.systemui.statusbar.phone.NotificationShadeWindowView;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.android.systemui.tuner.TunerService;
import com.android.systemui.util.InjectionInflationController;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class NotificationShadeWindowViewController {
    private PhoneStatusBarTransitions mBarTransitions;
    /* access modifiers changed from: private */
    public View mBrightnessMirror;
    /* access modifiers changed from: private */
    public ControlPanelWindowManager mControlPanelWindowManager;
    private final NotificationShadeDepthController mDepthController;
    /* access modifiers changed from: private */
    public final DockManager mDockManager;
    /* access modifiers changed from: private */
    public boolean mDoubleTapEnabled;
    /* access modifiers changed from: private */
    public DragDownHelper mDragDownHelper;
    /* access modifiers changed from: private */
    public boolean mExpandAnimationPending;
    /* access modifiers changed from: private */
    public boolean mExpandAnimationRunning;
    /* access modifiers changed from: private */
    public boolean mExpandingBelowNotch;
    /* access modifiers changed from: private */
    public final FalsingManager mFalsingManager;
    /* access modifiers changed from: private */
    public GestureDetector mGestureDetector;
    /* access modifiers changed from: private */
    public boolean mIsTrackingBarGesture = false;
    /* access modifiers changed from: private */
    public final NotificationPanelViewController mNotificationPanelViewController;
    private NotificationShadeWindowController mNotificationShadeWindowController;
    /* access modifiers changed from: private */
    public StatusBar mService;
    /* access modifiers changed from: private */
    public boolean mSingleTapEnabled;
    /* access modifiers changed from: private */
    public NotificationStackScrollLayout mStackScrollLayout;
    /* access modifiers changed from: private */
    public final SysuiStatusBarStateController mStatusBarStateController;
    /* access modifiers changed from: private */
    public PhoneStatusBarView mStatusBarView;
    private final SuperStatusBarViewFactory mStatusBarViewFactory;
    private int[] mTempLocation = new int[2];
    private RectF mTempRect = new RectF();
    private boolean mTouchActive;
    /* access modifiers changed from: private */
    public boolean mTouchCancelled;
    private final TunerService mTunerService;
    /* access modifiers changed from: private */
    public final NotificationShadeWindowView mView;

    public NotificationShadeWindowViewController(InjectionInflationController injectionInflationController, NotificationWakeUpCoordinator notificationWakeUpCoordinator, PulseExpansionHandler pulseExpansionHandler, DynamicPrivacyController dynamicPrivacyController, KeyguardBypassController keyguardBypassController, FalsingManager falsingManager, PluginManager pluginManager, TunerService tunerService, NotificationLockscreenUserManager notificationLockscreenUserManager, NotificationEntryManager notificationEntryManager, KeyguardStateController keyguardStateController, SysuiStatusBarStateController sysuiStatusBarStateController, DozeLog dozeLog, DozeParameters dozeParameters, CommandQueue commandQueue, ShadeController shadeController, DockManager dockManager, NotificationShadeDepthController notificationShadeDepthController, NotificationShadeWindowView notificationShadeWindowView, MiuiNotificationPanelViewController miuiNotificationPanelViewController, SuperStatusBarViewFactory superStatusBarViewFactory, ControlPanelWindowManager controlPanelWindowManager) {
        NotificationShadeWindowView notificationShadeWindowView2 = notificationShadeWindowView;
        this.mFalsingManager = falsingManager;
        this.mTunerService = tunerService;
        this.mStatusBarStateController = sysuiStatusBarStateController;
        this.mView = notificationShadeWindowView2;
        this.mDockManager = dockManager;
        this.mNotificationPanelViewController = miuiNotificationPanelViewController;
        this.mDepthController = notificationShadeDepthController;
        this.mStatusBarViewFactory = superStatusBarViewFactory;
        this.mBrightnessMirror = notificationShadeWindowView2.findViewById(C0015R$id.brightness_mirror);
        this.mControlPanelWindowManager = controlPanelWindowManager;
    }

    public void setupExpandedStatusBar() {
        this.mStackScrollLayout = (NotificationStackScrollLayout) this.mView.findViewById(C0015R$id.notification_stack_scroller);
        this.mTunerService.addTunable(new TunerService.Tunable() {
            public final void onTuningChanged(String str, String str2) {
                NotificationShadeWindowViewController.this.lambda$setupExpandedStatusBar$0$NotificationShadeWindowViewController(str, str2);
            }
        }, "doze_pulse_on_double_tap", "doze_tap_gesture");
        this.mGestureDetector = new GestureDetector(this.mView.getContext(), new GestureDetector.SimpleOnGestureListener() {
            public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
                if (!NotificationShadeWindowViewController.this.mSingleTapEnabled || NotificationShadeWindowViewController.this.mDockManager.isDocked()) {
                    return false;
                }
                NotificationShadeWindowViewController.this.mService.wakeUpIfDozing(SystemClock.uptimeMillis(), NotificationShadeWindowViewController.this.mView, "SINGLE_TAP");
                return true;
            }

            public boolean onDoubleTap(MotionEvent motionEvent) {
                if (!NotificationShadeWindowViewController.this.mDoubleTapEnabled && !NotificationShadeWindowViewController.this.mSingleTapEnabled) {
                    return false;
                }
                NotificationShadeWindowViewController.this.mService.wakeUpIfDozing(SystemClock.uptimeMillis(), NotificationShadeWindowViewController.this.mView, "DOUBLE_TAP");
                return true;
            }
        });
        this.mView.setInteractionEventHandler(new NotificationShadeWindowView.InteractionEventHandler() {
            public Boolean handleDispatchTouchEvent(MotionEvent motionEvent) {
                Boolean bool = Boolean.FALSE;
                boolean z = motionEvent.getActionMasked() == 0;
                boolean z2 = motionEvent.getActionMasked() == 1;
                boolean z3 = motionEvent.getActionMasked() == 3;
                boolean access$500 = NotificationShadeWindowViewController.this.mExpandingBelowNotch;
                if (z2 || z3) {
                    boolean unused = NotificationShadeWindowViewController.this.mExpandingBelowNotch = false;
                }
                if (NotificationShadeWindowViewController.this.mControlPanelWindowManager.getTransToControlPanel()) {
                    ((ControlPanelWindowManager) Dependency.get(ControlPanelWindowManager.class)).dispatchToControlPanel(motionEvent);
                    if (z2 || z3) {
                        NotificationShadeWindowViewController.this.mControlPanelWindowManager.setTransToControlPanel(false);
                    }
                    return bool;
                } else if (!z3 && NotificationShadeWindowViewController.this.mService.shouldIgnoreTouch()) {
                    return bool;
                } else {
                    if (z && NotificationShadeWindowViewController.this.mNotificationPanelViewController.isFullyCollapsed()) {
                        NotificationShadeWindowViewController.this.mNotificationPanelViewController.startExpandLatencyTracking();
                    }
                    if (z) {
                        NotificationShadeWindowViewController.this.setTouchActive(true);
                        boolean unused2 = NotificationShadeWindowViewController.this.mTouchCancelled = false;
                    } else if (motionEvent.getActionMasked() == 1 || motionEvent.getActionMasked() == 3) {
                        NotificationShadeWindowViewController.this.setTouchActive(false);
                    }
                    if (NotificationShadeWindowViewController.this.mTouchCancelled || NotificationShadeWindowViewController.this.mExpandAnimationRunning || NotificationShadeWindowViewController.this.mExpandAnimationPending) {
                        return bool;
                    }
                    NotificationShadeWindowViewController.this.mFalsingManager.onTouchEvent(motionEvent, NotificationShadeWindowViewController.this.mView.getWidth(), NotificationShadeWindowViewController.this.mView.getHeight());
                    NotificationShadeWindowViewController.this.mGestureDetector.onTouchEvent(motionEvent);
                    if (NotificationShadeWindowViewController.this.mBrightnessMirror != null && NotificationShadeWindowViewController.this.mBrightnessMirror.getVisibility() == 0 && motionEvent.getActionMasked() == 5) {
                        return bool;
                    }
                    if (z) {
                        NotificationShadeWindowViewController.this.mStackScrollLayout.closeControlsIfOutsideTouch(motionEvent);
                    }
                    if (NotificationShadeWindowViewController.this.mStatusBarStateController.isDozing()) {
                        NotificationShadeWindowViewController.this.mService.mDozeScrimController.extendPulse();
                    }
                    if (z && motionEvent.getY() >= ((float) NotificationShadeWindowViewController.this.mView.getBottom())) {
                        boolean unused3 = NotificationShadeWindowViewController.this.mExpandingBelowNotch = true;
                        access$500 = true;
                    }
                    if (access$500) {
                        return Boolean.valueOf(NotificationShadeWindowViewController.this.mStatusBarView.dispatchTouchEvent(motionEvent));
                    }
                    if (!NotificationShadeWindowViewController.this.mIsTrackingBarGesture && z && NotificationShadeWindowViewController.this.mNotificationPanelViewController.isFullyCollapsed()) {
                        float rawX = motionEvent.getRawX();
                        float rawY = motionEvent.getRawY();
                        NotificationShadeWindowViewController notificationShadeWindowViewController = NotificationShadeWindowViewController.this;
                        if (!notificationShadeWindowViewController.isIntersecting(notificationShadeWindowViewController.mStatusBarView, rawX, rawY)) {
                            return null;
                        }
                        if (!NotificationShadeWindowViewController.this.mService.isSameStatusBarState(0)) {
                            return Boolean.TRUE;
                        }
                        boolean unused4 = NotificationShadeWindowViewController.this.mIsTrackingBarGesture = true;
                        return Boolean.valueOf(NotificationShadeWindowViewController.this.mStatusBarView.dispatchTouchEvent(motionEvent));
                    } else if (!NotificationShadeWindowViewController.this.mIsTrackingBarGesture) {
                        return null;
                    } else {
                        boolean dispatchTouchEvent = NotificationShadeWindowViewController.this.mStatusBarView.dispatchTouchEvent(motionEvent);
                        if (z2 || z3) {
                            boolean unused5 = NotificationShadeWindowViewController.this.mIsTrackingBarGesture = false;
                        }
                        return Boolean.valueOf(dispatchTouchEvent);
                    }
                }
            }

            public boolean shouldInterceptTouchEvent(MotionEvent motionEvent) {
                if (NotificationShadeWindowViewController.this.mStatusBarStateController.isDozing() && !NotificationShadeWindowViewController.this.mService.isPulsing() && !NotificationShadeWindowViewController.this.mDockManager.isDocked()) {
                    return true;
                }
                if (!NotificationShadeWindowViewController.this.mNotificationPanelViewController.isFullyExpanded() || !NotificationShadeWindowViewController.this.mDragDownHelper.isDragDownEnabled() || NotificationShadeWindowViewController.this.mService.isBouncerShowing() || NotificationShadeWindowViewController.this.mStatusBarStateController.isDozing()) {
                    return false;
                }
                return NotificationShadeWindowViewController.this.mDragDownHelper.onInterceptTouchEvent(motionEvent);
            }

            public void didIntercept(MotionEvent motionEvent) {
                MotionEvent obtain = MotionEvent.obtain(motionEvent);
                obtain.setAction(3);
                NotificationShadeWindowViewController.this.mStackScrollLayout.onInterceptTouchEvent(obtain);
                NotificationShadeWindowViewController.this.mNotificationPanelViewController.getView().onInterceptTouchEvent(obtain);
                obtain.recycle();
            }

            public boolean handleTouchEvent(MotionEvent motionEvent) {
                boolean z = NotificationShadeWindowViewController.this.mStatusBarStateController.isDozing() ? !NotificationShadeWindowViewController.this.mService.isPulsing() : false;
                return ((!NotificationShadeWindowViewController.this.mDragDownHelper.isDragDownEnabled() || z) && !NotificationShadeWindowViewController.this.mDragDownHelper.isDraggingDown()) ? z : NotificationShadeWindowViewController.this.mDragDownHelper.onTouchEvent(motionEvent);
            }

            public void didNotHandleTouchEvent(MotionEvent motionEvent) {
                int actionMasked = motionEvent.getActionMasked();
                if (actionMasked == 1 || actionMasked == 3) {
                    NotificationShadeWindowViewController.this.mService.setInteracting(1, false);
                }
            }

            public boolean interceptMediaKey(KeyEvent keyEvent) {
                return NotificationShadeWindowViewController.this.mService.interceptMediaKey(keyEvent);
            }

            public boolean dispatchKeyEvent(KeyEvent keyEvent) {
                boolean z = keyEvent.getAction() == 0;
                int keyCode = keyEvent.getKeyCode();
                if (keyCode != 4) {
                    if (keyCode != 62) {
                        if (keyCode != 82) {
                            if ((keyCode == 24 || keyCode == 25) && NotificationShadeWindowViewController.this.mStatusBarStateController.isDozing()) {
                                MediaSessionLegacyHelper.getHelper(NotificationShadeWindowViewController.this.mView.getContext()).sendVolumeKeyEvent(keyEvent, Integer.MIN_VALUE, true);
                                return true;
                            }
                        } else if (!z) {
                            return NotificationShadeWindowViewController.this.mService.onMenuPressed();
                        }
                    } else if (!z) {
                        return NotificationShadeWindowViewController.this.mService.onSpacePressed();
                    }
                    return false;
                }
                if (!z) {
                    NotificationShadeWindowViewController.this.mService.onBackPressed();
                }
                return true;
            }
        });
        this.mView.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
            public void onChildViewRemoved(View view, View view2) {
            }

            public void onChildViewAdded(View view, View view2) {
                if (view2.getId() == C0015R$id.brightness_mirror) {
                    View unused = NotificationShadeWindowViewController.this.mBrightnessMirror = view2;
                }
            }
        });
        setDragDownHelper(new DragDownHelper(this.mView.getContext(), this.mView, this.mStackScrollLayout.getExpandHelperCallback(), this.mStackScrollLayout.getDragDownCallback(), this.mFalsingManager));
        this.mDepthController.setRoot(this.mView);
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x0033  */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x003d  */
    /* renamed from: lambda$setupExpandedStatusBar$0 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public /* synthetic */ void lambda$setupExpandedStatusBar$0$NotificationShadeWindowViewController(java.lang.String r4, java.lang.String r5) {
        /*
            r3 = this;
            android.hardware.display.AmbientDisplayConfiguration r5 = new android.hardware.display.AmbientDisplayConfiguration
            com.android.systemui.statusbar.phone.NotificationShadeWindowView r0 = r3.mView
            android.content.Context r0 = r0.getContext()
            r5.<init>(r0)
            int r0 = r4.hashCode()
            r1 = 417936100(0x18e932e4, float:6.0280475E-24)
            r2 = 1
            if (r0 == r1) goto L_0x0025
            r1 = 1073289638(0x3ff919a6, float:1.9460952)
            if (r0 == r1) goto L_0x001b
            goto L_0x002f
        L_0x001b:
            java.lang.String r0 = "doze_pulse_on_double_tap"
            boolean r4 = r4.equals(r0)
            if (r4 == 0) goto L_0x002f
            r4 = 0
            goto L_0x0030
        L_0x0025:
            java.lang.String r0 = "doze_tap_gesture"
            boolean r4 = r4.equals(r0)
            if (r4 == 0) goto L_0x002f
            r4 = r2
            goto L_0x0030
        L_0x002f:
            r4 = -1
        L_0x0030:
            r0 = -2
            if (r4 == 0) goto L_0x003d
            if (r4 == r2) goto L_0x0036
            goto L_0x0043
        L_0x0036:
            boolean r4 = r5.tapGestureEnabled(r0)
            r3.mSingleTapEnabled = r4
            goto L_0x0043
        L_0x003d:
            boolean r4 = r5.doubleTapGestureEnabled(r0)
            r3.mDoubleTapEnabled = r4
        L_0x0043:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.phone.NotificationShadeWindowViewController.lambda$setupExpandedStatusBar$0$NotificationShadeWindowViewController(java.lang.String, java.lang.String):void");
    }

    public NotificationShadeWindowView getView() {
        return this.mView;
    }

    public void setTouchActive(boolean z) {
        this.mTouchActive = z;
    }

    public void cancelCurrentTouch() {
        if (this.mTouchActive) {
            long uptimeMillis = SystemClock.uptimeMillis();
            MotionEvent obtain = MotionEvent.obtain(uptimeMillis, uptimeMillis, 3, 0.0f, 0.0f, 0);
            obtain.setSource(4098);
            this.mView.dispatchTouchEvent(obtain);
            obtain.recycle();
            this.mTouchCancelled = true;
        }
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.print("  mExpandAnimationPending=");
        printWriter.println(this.mExpandAnimationPending);
        printWriter.print("  mExpandAnimationRunning=");
        printWriter.println(this.mExpandAnimationRunning);
        printWriter.print("  mTouchCancelled=");
        printWriter.println(this.mTouchCancelled);
        printWriter.print("  mTouchActive=");
        printWriter.println(this.mTouchActive);
    }

    public void setExpandAnimationPending(boolean z) {
        if (this.mExpandAnimationPending != z) {
            this.mExpandAnimationPending = z;
            this.mNotificationShadeWindowController.setLaunchingActivity(this.mExpandAnimationRunning | z);
        }
    }

    public void setExpandAnimationRunning(boolean z) {
        if (this.mExpandAnimationRunning != z) {
            this.mExpandAnimationRunning = z;
            this.mNotificationShadeWindowController.setLaunchingActivity(this.mExpandAnimationPending | z);
        }
    }

    public void cancelExpandHelper() {
        NotificationStackScrollLayout notificationStackScrollLayout = this.mStackScrollLayout;
        if (notificationStackScrollLayout != null) {
            notificationStackScrollLayout.cancelExpandHelper();
        }
    }

    public PhoneStatusBarTransitions getBarTransitions() {
        return this.mBarTransitions;
    }

    public void setStatusBarView(PhoneStatusBarView phoneStatusBarView) {
        SuperStatusBarViewFactory superStatusBarViewFactory;
        this.mStatusBarView = phoneStatusBarView;
        if (phoneStatusBarView != null && (superStatusBarViewFactory = this.mStatusBarViewFactory) != null) {
            this.mBarTransitions = new PhoneStatusBarTransitions(phoneStatusBarView, superStatusBarViewFactory.getStatusBarWindowView().findViewById(C0015R$id.status_bar_container));
        }
    }

    public void setService(StatusBar statusBar, NotificationShadeWindowController notificationShadeWindowController) {
        this.mService = statusBar;
        this.mNotificationShadeWindowController = notificationShadeWindowController;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void setDragDownHelper(DragDownHelper dragDownHelper) {
        this.mDragDownHelper = dragDownHelper;
    }

    /* access modifiers changed from: private */
    public boolean isIntersecting(View view, float f, float f2) {
        int[] locationOnScreen = view.getLocationOnScreen();
        this.mTempLocation = locationOnScreen;
        this.mTempRect.set((float) locationOnScreen[0], (float) locationOnScreen[1], (float) (locationOnScreen[0] + view.getWidth()), (float) (this.mTempLocation[1] + view.getHeight()));
        return this.mTempRect.contains(f, f2);
    }
}
