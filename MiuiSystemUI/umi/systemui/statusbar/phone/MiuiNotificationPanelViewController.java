package com.android.systemui.statusbar.phone;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.PointF;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;
import android.util.Slog;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.ViewTreeObserver;
import android.view.accessibility.AccessibilityManager;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.util.LatencyTracker;
import com.android.keyguard.AwesomeLockScreen;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.MiuiFastUnlockController;
import com.android.keyguard.MiuiKeyguardUpdateMonitorCallback;
import com.android.keyguard.charge.ChargeUtils;
import com.android.keyguard.clock.KeyguardClockContainer;
import com.android.keyguard.faceunlock.MiuiKeyguardFaceUnlockView;
import com.android.keyguard.injector.KeyguardClockInjector;
import com.android.keyguard.injector.KeyguardPanelViewInjector;
import com.android.keyguard.injector.KeyguardUpdateMonitorInjector;
import com.android.keyguard.magazine.LockScreenMagazineController;
import com.android.keyguard.magazine.LockScreenMagazinePreView;
import com.android.keyguard.utils.MiuiKeyguardUtils;
import com.android.keyguard.wallpaper.KeyguardWallpaperUtils;
import com.android.keyguard.wallpaper.MiuiKeyguardWallpaperControllerImpl;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0015R$id;
import com.android.systemui.Dependency;
import com.android.systemui.Interpolators;
import com.android.systemui.controlcenter.phone.ControlPanelController;
import com.android.systemui.doze.DozeLog;
import com.android.systemui.keyguard.WakefulnessLifecycle;
import com.android.systemui.media.MediaHierarchyManager;
import com.android.systemui.plugins.FalsingManager;
import com.android.systemui.plugins.qs.QS;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.qs.MiuiQSFragment;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.FlingAnimationUtils;
import com.android.systemui.statusbar.NotificationLockscreenUserManager;
import com.android.systemui.statusbar.PulseExpansionHandler;
import com.android.systemui.statusbar.VibratorHelper;
import com.android.systemui.statusbar.notification.ConversationNotificationManager;
import com.android.systemui.statusbar.notification.DynamicPrivacyController;
import com.android.systemui.statusbar.notification.MiuiNotificationEntryManager;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.NotificationSettingsHelper;
import com.android.systemui.statusbar.notification.NotificationWakeUpCoordinator;
import com.android.systemui.statusbar.notification.analytics.NotificationStat;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout;
import com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayoutExtKt;
import com.android.systemui.statusbar.notification.stack.PanelAppearDisappearEvent;
import com.android.systemui.statusbar.notification.unimportant.FoldListener;
import com.android.systemui.statusbar.notification.unimportant.FoldManager;
import com.android.systemui.statusbar.phone.HeadsUpTouchHelper;
import com.android.systemui.statusbar.phone.NotificationPanelViewController;
import com.android.systemui.statusbar.phone.PanelViewController;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.android.systemui.statusbar.policy.ZenModeController;
import com.android.systemui.statusbar.views.DismissView;
import com.android.systemui.util.ConvenienceExtensionsKt;
import com.android.systemui.util.InjectionInflationController;
import com.miui.systemui.DeviceConfig;
import com.miui.systemui.EventTracker;
import com.miui.systemui.statusbar.PanelExpansionObserver;
import com.miui.systemui.util.AccessibilityUtils;
import com.miui.systemui.util.MiuiAnimationUtils;
import com.miui.systemui.util.MiuiThemeUtils;
import dagger.Lazy;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import kotlin.TypeCastException;
import kotlin.Unit;
import kotlin.collections.CollectionsKt;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Ref$ObjectRef;
import kotlin.ranges.RangesKt;
import kotlin.reflect.KFunction;
import kotlin.sequences.Sequence;
import kotlin.sequences.SequencesKt;
import miuix.animation.Folme;
import miuix.animation.IStateStyle;
import miuix.animation.listener.TransitionListener;
import miuix.animation.listener.UpdateInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: MiuiNotificationPanelViewController.kt */
public final class MiuiNotificationPanelViewController extends NotificationPanelViewController implements StatusBarStateController.StateListener, View.OnAttachStateChangeListener, WakefulnessLifecycle.Observer, FoldListener {
    private final Lazy<ControlPanelController> controlPanelController;
    private AwesomeLockScreen mAwesomeLockScreen;
    private FrameLayout mAwesomeLockScreenContainer;
    private int mBarState;
    private boolean mBlurAnimRunning;
    private float mBlurRatio;
    private boolean mBluring;
    private final float mBottomAreaCollapseHotZone;
    private ValueAnimator mBouncerFractionAnimator;
    private final MiuiNotificationPanelViewController$mChildPositionsChangedListener$1 mChildPositionsChangedListener;
    @NotNull
    private Configuration mConfiguration;
    private DismissView mDismissView;
    private final EventTracker mEventTracker;
    private boolean mExpandingFromHeadsUp;
    private boolean mExpectingSynthesizedDown;
    private boolean mHidePanelRequested;
    private final Runnable mHidePanelRunnable = new MiuiNotificationPanelViewController$mHidePanelRunnable$1(this);
    private boolean mIsDefaultTheme = true;
    private boolean mIsInteractive;
    private boolean mIsKeyguardOccluded;
    private float mKeyguardBouncerFraction;
    private boolean mKeyguardBouncerShowing;
    private final KeyguardClockInjector mKeyguardClockInjector;
    private final KeyguardPanelViewInjector mKeyguardPanelViewInjector;
    private final MiuiKeyguardUpdateMonitorCallback mKeyguardUpdateMonitorCallback;
    private boolean mNCSwitching;
    private final MiuiNotificationEntryManager mNotificationEntryManager;
    @NotNull
    private final NotificationStackScrollLayout mNotificationStackScroller;
    private boolean mNssCoveredQs;
    private boolean mNssCoveringQs;
    private boolean mPanelCollapsing;
    private long mPanelDisappearedTime;
    private boolean mPanelIntercepting;
    private boolean mPanelOpening;
    private float mQsTopPadding;
    private ValueAnimator mQsTopPaddingAnimator;
    private boolean mQuickFlingHeadsUpTriggered;
    private final KFunction<Unit> mSetExpandedHeight = new MiuiNotificationPanelViewController$mSetExpandedHeight$1(this);
    private boolean mShowDismissView;
    private float mSpringLength;
    private View mStickyGroupHeader;
    private int mStickyHeaderHeight;
    private boolean mStretchFromHeadsUpRequested;
    private float mStretchLength;
    private boolean mStretchingFromHeadsUp;
    private View mThemeBackgroundView;
    private final int mTouchSlop;
    private boolean mTrackingMiniWindowHeadsUp;
    private VelocityTracker mVelocityTracker;
    private final PanelViewLogger panelBlurLogger;
    @NotNull
    private final NotificationPanelView panelView;
    private final NotificationShadeWindowController shadeWindowController;
    private final StatusBarStateController statusBarStateController;
    private ValueAnimator valueAnimator;
    private boolean wakeUpFromDozing;
    private final WakefulnessLifecycle wakefulnessLifecycle;

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.NotificationPanelViewController
    public void setOverScrolling(boolean z) {
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.NotificationPanelViewController
    public void updateStatusBarIcons() {
    }

    @NotNull
    public final NotificationPanelView getPanelView() {
        return this.panelView;
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public MiuiNotificationPanelViewController(@NotNull NotificationPanelView notificationPanelView, @Nullable InjectionInflationController injectionInflationController, @Nullable NotificationWakeUpCoordinator notificationWakeUpCoordinator, @Nullable PulseExpansionHandler pulseExpansionHandler, @Nullable DynamicPrivacyController dynamicPrivacyController, @Nullable KeyguardBypassController keyguardBypassController, @Nullable FalsingManager falsingManager, @Nullable ShadeController shadeController, @Nullable NotificationLockscreenUserManager notificationLockscreenUserManager, @NotNull NotificationEntryManager notificationEntryManager, @Nullable KeyguardStateController keyguardStateController, @NotNull StatusBarStateController statusBarStateController2, @Nullable DozeLog dozeLog, @Nullable DozeParameters dozeParameters, @Nullable CommandQueue commandQueue, @Nullable VibratorHelper vibratorHelper, @Nullable LatencyTracker latencyTracker, @Nullable PowerManager powerManager, @Nullable AccessibilityManager accessibilityManager, int i, @Nullable KeyguardUpdateMonitor keyguardUpdateMonitor, @Nullable MetricsLogger metricsLogger, @Nullable ActivityManager activityManager, @Nullable ZenModeController zenModeController, @Nullable ConfigurationController configurationController, @Nullable FlingAnimationUtils.Builder builder, @Nullable StatusBarTouchableRegionManager statusBarTouchableRegionManager, @Nullable ConversationNotificationManager conversationNotificationManager, @Nullable MediaHierarchyManager mediaHierarchyManager, @Nullable BiometricUnlockController biometricUnlockController, @Nullable StatusBarKeyguardViewManager statusBarKeyguardViewManager, @NotNull Lazy<ControlPanelController> lazy, @NotNull EventTracker eventTracker, @NotNull WakefulnessLifecycle wakefulnessLifecycle2, @NotNull NotificationShadeWindowController notificationShadeWindowController, @NotNull PanelViewLogger panelViewLogger) {
        super(notificationPanelView, injectionInflationController, notificationWakeUpCoordinator, pulseExpansionHandler, dynamicPrivacyController, keyguardBypassController, falsingManager, shadeController, notificationLockscreenUserManager, notificationEntryManager, keyguardStateController, statusBarStateController2, dozeLog, dozeParameters, commandQueue, vibratorHelper, latencyTracker, powerManager, accessibilityManager, i, keyguardUpdateMonitor, metricsLogger, activityManager, zenModeController, configurationController, builder, statusBarTouchableRegionManager, conversationNotificationManager, mediaHierarchyManager, biometricUnlockController, statusBarKeyguardViewManager);
        Intrinsics.checkParameterIsNotNull(notificationPanelView, "panelView");
        Intrinsics.checkParameterIsNotNull(notificationEntryManager, "notificationEntryManager");
        Intrinsics.checkParameterIsNotNull(statusBarStateController2, "statusBarStateController");
        Intrinsics.checkParameterIsNotNull(lazy, "controlPanelController");
        Intrinsics.checkParameterIsNotNull(eventTracker, "eventTracker");
        Intrinsics.checkParameterIsNotNull(wakefulnessLifecycle2, "wakefulnessLifecycle");
        Intrinsics.checkParameterIsNotNull(notificationShadeWindowController, "shadeWindowController");
        Intrinsics.checkParameterIsNotNull(panelViewLogger, "panelBlurLogger");
        this.panelView = notificationPanelView;
        this.statusBarStateController = statusBarStateController2;
        this.controlPanelController = lazy;
        this.wakefulnessLifecycle = wakefulnessLifecycle2;
        this.shadeWindowController = notificationShadeWindowController;
        this.panelBlurLogger = panelViewLogger;
        this.mNotificationEntryManager = (MiuiNotificationEntryManager) notificationEntryManager;
        this.mEventTracker = eventTracker;
        ViewConfiguration viewConfiguration = ViewConfiguration.get(notificationPanelView.getContext());
        Intrinsics.checkExpressionValueIsNotNull(viewConfiguration, "ViewConfiguration.get(panelView.context)");
        this.mTouchSlop = viewConfiguration.getScaledTouchSlop();
        Context context = this.panelView.getContext();
        Intrinsics.checkExpressionValueIsNotNull(context, "panelView.context");
        this.mBottomAreaCollapseHotZone = context.getResources().getDimension(C0012R$dimen.miui_notification_swipe_area_height);
        View findViewById = this.panelView.findViewById(C0015R$id.notification_stack_scroller);
        Intrinsics.checkExpressionValueIsNotNull(findViewById, "panelView.findViewById(R…ification_stack_scroller)");
        this.mNotificationStackScroller = (NotificationStackScrollLayout) findViewById;
        Object obj = Dependency.get(KeyguardPanelViewInjector.class);
        Intrinsics.checkExpressionValueIsNotNull(obj, "Dependency.get(KeyguardP…ViewInjector::class.java)");
        this.mKeyguardPanelViewInjector = (KeyguardPanelViewInjector) obj;
        Object obj2 = Dependency.get(KeyguardClockInjector.class);
        Intrinsics.checkExpressionValueIsNotNull(obj2, "Dependency.get(KeyguardClockInjector::class.java)");
        this.mKeyguardClockInjector = (KeyguardClockInjector) obj2;
        this.mKeyguardUpdateMonitorCallback = new MiuiNotificationPanelViewController$mKeyguardUpdateMonitorCallback$1(this);
        this.mKeyguardPanelViewInjector.init(this);
        this.panelView.setClipChildren(false);
        this.panelView.addOnAttachStateChangeListener(this);
        if (this.panelView.isAttachedToWindow()) {
            onViewAttachedToWindow(this.panelView);
        }
        initializeFolmeAnimations();
        this.mIsDefaultTheme = MiuiKeyguardUtils.isDefaultLockScreenTheme();
        this.mUpdateMonitor.registerCallback(this.mKeyguardUpdateMonitorCallback);
        this.wakefulnessLifecycle.addObserver(this);
        this.mNotificationStackScroller.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener(this) {
            /* class com.android.systemui.statusbar.phone.MiuiNotificationPanelViewController.AnonymousClass1 */
            final /* synthetic */ MiuiNotificationPanelViewController this$0;

            {
                this.this$0 = r1;
            }

            public final void onGlobalLayout() {
                NotificationStackScrollLayoutExtKt.updateStackScrollLayoutHeight(this.this$0.getMNotificationStackScroller());
            }
        });
        NotificationPanelView notificationPanelView2 = ((NotificationPanelViewController) this).mView;
        Intrinsics.checkExpressionValueIsNotNull(notificationPanelView2, "mView");
        Context context2 = notificationPanelView2.getContext();
        Intrinsics.checkExpressionValueIsNotNull(context2, "mView.context");
        context2.getResources().getDimensionPixelSize(C0012R$dimen.notification_sticky_group_header_height);
        ((ControlPanelController) Dependency.get(ControlPanelController.class)).addCallback((ControlPanelController.UseControlPanelChangeListener) new ControlPanelController.UseControlPanelChangeListener(this) {
            /* class com.android.systemui.statusbar.phone.MiuiNotificationPanelViewController.AnonymousClass2 */
            final /* synthetic */ MiuiNotificationPanelViewController this$0;

            {
                this.this$0 = r1;
            }

            @Override // com.android.systemui.controlcenter.phone.ControlPanelController.UseControlPanelChangeListener
            public final void onUseControlPanelChange(boolean z) {
                MiuiNotificationPanelViewController miuiNotificationPanelViewController = this.this$0;
                miuiNotificationPanelViewController.mQsNotificationTopPadding = z ? miuiNotificationPanelViewController.mResources.getDimensionPixelSize(C0012R$dimen.qs_notification_padding) : 0;
            }
        });
        FoldManager.Companion.addTopListeners(this);
        this.mChildPositionsChangedListener = new MiuiNotificationPanelViewController$mChildPositionsChangedListener$1(this);
        this.mConfiguration = new Configuration();
    }

    @NotNull
    public final NotificationStackScrollLayout getMNotificationStackScroller() {
        return this.mNotificationStackScroller;
    }

    public final float getMBlurRatio() {
        return this.mBlurRatio;
    }

    public final void setMBlurRatio(float f) {
        float f2 = (float) 1000;
        int coerceIn = RangesKt.coerceIn((int) (this.mBlurRatio * f2), 0, 1000);
        int coerceIn2 = RangesKt.coerceIn((int) (f2 * f), 0, 1000);
        if (coerceIn != coerceIn2) {
            if (coerceIn2 >= 0 && 10 >= coerceIn2) {
                f = 0.0f;
            } else if (990 <= coerceIn2 && 1000 >= coerceIn2) {
                f = 1.0f;
            }
            if (this.mBlurRatio != f) {
                this.mBlurRatio = f;
                this.shadeWindowController.setBlurRatio(f);
                View view = this.mThemeBackgroundView;
                if (view != null) {
                    view.setAlpha(this.mBlurRatio);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private final void setMBlurAnimRunning(boolean z) {
        this.shadeWindowController.setBlurAnimRunning(z);
        this.mBlurAnimRunning = z;
    }

    /* access modifiers changed from: private */
    public final void setMStretchLength(float f) {
        if (this.mPanelOpening) {
            f = RangesKt.coerceAtLeast(f, 0.0f);
        } else if (this.mPanelCollapsing) {
            f = RangesKt.coerceAtMost(f, 0.0f);
        }
        this.mStretchLength = f;
        boolean z = false;
        if (this.mPanelOpening && f > ((float) 0)) {
            setExpandedHeightInternal((float) getMaxPanelHeight());
        } else if (!getMPanelAppeared() && !this.mPanelIntercepting && this.mStretchLength == 0.0f && !this.mExpandingFromHeadsUp) {
            setExpandedHeightInternal(0.0f);
        }
        if (this.mPanelOpening) {
            if (this.mStretchLength > (this.mStretchingFromHeadsUp ? 0.0f : 50.0f)) {
                z = true;
            }
            setMPanelAppeared(z);
            ((NotificationStat) Dependency.get(NotificationStat.class)).onPanelExpanded(isOnKeyguard(), true, this.mNotificationEntryManager.getActiveNotificationsCount(), this.mNotificationEntryManager.getImportantNotificationsCount());
        } else if (this.mPanelCollapsing) {
            if (Math.abs(this.mStretchLength) < 50.0f) {
                z = true;
            }
            setMPanelAppeared(z);
            ((NotificationStat) Dependency.get(NotificationStat.class)).onPanelCollapsed(true, this.mNotificationEntryManager.getActiveNotificationsCount());
        }
        float f2 = this.mStretchLength;
        if (f2 >= 50.0f) {
            setMSpringLength(afterFriction(Math.max(0.0f, f2 - 50.0f), getHeight()) * 0.5f);
        }
        if (this.mPanelOpening || this.mPanelCollapsing) {
            updateBlur();
        }
    }

    /* access modifiers changed from: private */
    public final void setMSpringLength(float f) {
        this.mSpringLength = f;
        updateQsExpansion();
        NotificationStackScrollLayoutExtKt.onSpringLengthUpdated(this.mNotificationStackScroller, f);
    }

    /* access modifiers changed from: private */
    public final boolean getMPanelStretching() {
        return NotificationStackScrollLayoutExtKt.isPanelStretching(this.mNotificationStackScroller);
    }

    /* access modifiers changed from: private */
    public final void setMPanelStretching(boolean z) {
        NotificationStackScrollLayoutExtKt.setPanelStretching(this.mNotificationStackScroller, z);
    }

    public final boolean getMPanelOpening() {
        return this.mPanelOpening;
    }

    public final void setMPanelOpening(boolean z) {
        if (z != this.mPanelOpening && z && isOnShade()) {
            Context context = this.panelView.getContext();
            Intrinsics.checkExpressionValueIsNotNull(context, "panelView.context");
            Resources resources = context.getResources();
            Intrinsics.checkExpressionValueIsNotNull(resources, "panelView.context.resources");
            if (resources.getConfiguration().orientation == 1) {
                this.controlPanelController.get().showDialog(true);
            }
        }
        this.mPanelOpening = z;
    }

    private final void setMStretchingFromHeadsUp(boolean z) {
        this.mStretchingFromHeadsUp = z;
        NotificationStackScrollLayoutExtKt.setPanelStretchingFromHeadsUp(this.mNotificationStackScroller, z);
    }

    public final boolean getMIsDefaultTheme() {
        return this.mIsDefaultTheme;
    }

    /* access modifiers changed from: private */
    public final boolean getMPanelAppeared() {
        return NotificationStackScrollLayoutExtKt.isPanelAppeared(this.mNotificationStackScroller);
    }

    private final void setMPanelAppeared(boolean z) {
        boolean mPanelAppeared = getMPanelAppeared();
        if (z != mPanelAppeared) {
            if (z) {
                getView().removeCallbacks(this.mHidePanelRunnable);
                this.mHidePanelRequested = false;
                FoldManager.Companion.notifyListeners(4);
            } else {
                FoldManager.Companion.notifyListenersDelayed(4, 150);
            }
            if (mPanelAppeared && !z) {
                this.mPanelDisappearedTime = SystemClock.uptimeMillis();
                AccessibilityUtils.hapticAccessibilityTransitionIfNeeded(((NotificationPanelViewController) this).mView.getContext(), 191);
            }
            if (mPanelAppeared != z) {
                refreshNssCoveringQs();
                QS qs = this.mQs;
                if (qs != null) {
                    qs.animateAppearDisappear(z);
                }
                resetStickHeader(z);
            }
            if (z) {
                AccessibilityUtils.hapticAccessibilityTransitionIfNeeded(((NotificationPanelViewController) this).mView.getContext(), 190);
            }
            NotificationStackScrollLayoutExtKt.setPanelAppeared(this.mNotificationStackScroller, z, isOnKeyguard());
            if (!z) {
                this.controlPanelController.get().showDialog(false);
            }
            updateDismissView();
        }
    }

    public final void requestNCSwitching(boolean z) {
        this.mNCSwitching = z;
        NotificationStackScrollLayoutExtKt.setNCSwitching(this.mNotificationStackScroller, z);
    }

    public final boolean isNCSwitching() {
        return this.mNCSwitching;
    }

    /* compiled from: MiuiNotificationPanelViewController.kt */
    public final class BlurTransitionListerner extends TransitionListener {
        /* JADX WARN: Incorrect args count in method signature: ()V */
        public BlurTransitionListerner() {
        }

        @Override // miuix.animation.listener.TransitionListener
        public void onBegin(@Nullable Object obj) {
            super.onBegin(obj);
            MiuiNotificationPanelViewController.this.panelBlurLogger.logBlurAnimStart(MiuiNotificationPanelViewController.this.getMBlurRatio());
            if (!PanelViewController.DEBUG || !MiuiNotificationPanelViewController.this.mBlurAnimRunning) {
                MiuiNotificationPanelViewController.this.setMBlurAnimRunning(true);
                return;
            }
            throw new IllegalStateException("SysuiBlur onBegin, but already running");
        }

        @Override // miuix.animation.listener.TransitionListener
        public void onUpdate(@Nullable Object obj, @Nullable Collection<UpdateInfo> collection) {
            UpdateInfo findByName = UpdateInfo.findByName(collection, "PanelBlurRatio");
            if (findByName != null) {
                float floatValue = findByName.getFloatValue();
                MiuiNotificationPanelViewController miuiNotificationPanelViewController = MiuiNotificationPanelViewController.this;
                float f = 0.0f;
                if (!Float.isNaN(floatValue)) {
                    f = RangesKt.coerceIn(floatValue, 0.0f, 1.0f);
                }
                miuiNotificationPanelViewController.setMBlurRatio(f);
                if (PanelViewController.DEBUG) {
                    MiuiNotificationPanelViewController.this.panelBlurLogger.logBlurAnimUpdate(MiuiNotificationPanelViewController.this.getMBlurRatio(), floatValue);
                }
            }
        }

        @Override // miuix.animation.listener.TransitionListener
        public void onComplete(@Nullable Object obj) {
            super.onComplete(obj);
            MiuiNotificationPanelViewController.this.panelBlurLogger.logBlurAnimEnd(false, MiuiNotificationPanelViewController.this.getMBlurRatio());
            if (!PanelViewController.DEBUG || MiuiNotificationPanelViewController.this.mBlurAnimRunning) {
                MiuiNotificationPanelViewController.this.setMBlurAnimRunning(false);
                MiuiNotificationPanelViewController.this.mBluring = false;
                return;
            }
            throw new IllegalStateException("SysuiBlur onComplete, but not running");
        }

        @Override // miuix.animation.listener.TransitionListener
        public void onCancel(@Nullable Object obj) {
            super.onCancel(obj);
            MiuiNotificationPanelViewController.this.panelBlurLogger.logBlurAnimEnd(true, MiuiNotificationPanelViewController.this.getMBlurRatio());
            if (!PanelViewController.DEBUG || MiuiNotificationPanelViewController.this.mBlurAnimRunning) {
                MiuiNotificationPanelViewController.this.setMBlurAnimRunning(false);
                MiuiNotificationPanelViewController.this.mBluring = false;
                return;
            }
            throw new IllegalStateException("SysuiBlur onCancel, but not running");
        }
    }

    private final void initializeFolmeAnimations() {
        IStateStyle upVar = Folme.useValue("PanelBlur").setup("PanelBlurSetup");
        Float valueOf = Float.valueOf(0.0f);
        upVar.setTo("PanelBlurRatio", valueOf);
        MiuiNotificationPanelViewControllerKt.access$getBLUR_ANIM_CONFIG$p().addListeners(new BlurTransitionListerner());
        Folme.useValue("PanelViewSpring").setTo("PanelSpringRatio", valueOf);
        MiuiNotificationPanelViewControllerKt.access$getSPRING_ANIM_CONFIG$p().addListeners(new MiuiNotificationPanelViewController$initializeFolmeAnimations$1(this));
        Folme.getValueTarget("PanelViewSpring").setMinVisibleChange(1.0f, "length");
    }

    @Override // com.android.systemui.statusbar.phone.PanelViewController
    public void setExpandedHeightInternal(float f) {
        if (this.mExpandingFromHeadsUp && !getMPanelStretching()) {
            super.setExpandedHeightInternal(f);
        } else if (this.mNCSwitching) {
            if (f > ((float) getMaxPanelHeight())) {
                f = (float) getMaxPanelHeight();
            }
            super.setExpandedHeightInternal(f);
        } else {
            float f2 = (float) 0;
            int i = (f > f2 ? 1 : (f == f2 ? 0 : -1));
            float f3 = 0.0f;
            boolean z = i > 0 && this.mExpandedHeight == 0.0f;
            if (i > 0) {
                f3 = (float) getMaxPanelHeight();
            }
            if (!isOnKeyguard()) {
                f = f3;
            }
            if (f <= f2) {
                setMPanelAppeared(false);
            }
            super.setExpandedHeightInternal(f);
            if (z) {
                maybeHandleQuickFling();
            }
        }
    }

    @Override // com.android.systemui.statusbar.phone.PanelViewController, com.android.systemui.statusbar.phone.NotificationPanelViewController
    public boolean flingExpands(float f, float f2, float f3, float f4) {
        boolean z = !this.mHidePanelRequested && super.flingExpands(f, f2, f3, f4);
        this.mHidePanelRequested = false;
        return z;
    }

    /* access modifiers changed from: private */
    public final void scheduleHidePanel() {
        long uptimeMillis = SystemClock.uptimeMillis() - this.mPanelDisappearedTime;
        this.mHidePanelRequested = true;
        this.panelView.postDelayed(this.mHidePanelRunnable, RangesKt.coerceAtLeast(((long) 450) - uptimeMillis, 0));
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.NotificationPanelViewController
    public void onQsTouch(@Nullable MotionEvent motionEvent) {
        if (motionEvent != null) {
            if (!(motionEvent.getActionMasked() == 2) || isStatusBarExpandable()) {
                super.onQsTouch(motionEvent);
                return;
            }
            return;
        }
        Intrinsics.throwNpe();
        throw null;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.PanelViewController, com.android.systemui.statusbar.phone.NotificationPanelViewController
    @NotNull
    public PanelViewController.TouchHandler createTouchHandler() {
        return new MiuiNotificationPanelViewController$createTouchHandler$1(this);
    }

    /* access modifiers changed from: private */
    public final void initVelocityTracker() {
        VelocityTracker velocityTracker = this.mVelocityTracker;
        if (velocityTracker != null) {
            velocityTracker.recycle();
        }
        this.mVelocityTracker = VelocityTracker.obtain();
    }

    /* access modifiers changed from: private */
    public final void trackMovement(MotionEvent motionEvent) {
        VelocityTracker velocityTracker = this.mVelocityTracker;
        if (velocityTracker != null) {
            velocityTracker.addMovement(motionEvent);
        }
    }

    /* access modifiers changed from: private */
    public final void recycleVelocityTracker() {
        VelocityTracker velocityTracker = this.mVelocityTracker;
        if (velocityTracker != null) {
            velocityTracker.recycle();
        }
        this.mVelocityTracker = null;
    }

    /* access modifiers changed from: private */
    public final float getCurrentQSVelocity() {
        VelocityTracker velocityTracker = this.mVelocityTracker;
        if (velocityTracker != null) {
            velocityTracker.computeCurrentVelocity(1000);
        }
        VelocityTracker velocityTracker2 = this.mVelocityTracker;
        if (velocityTracker2 != null) {
            return velocityTracker2.getYVelocity();
        }
        return 0.0f;
    }

    /* access modifiers changed from: private */
    public final void handleNssCoverQs(float f) {
        QS qs;
        float f2;
        if (!this.mQsExpanded && (qs = this.mQs) != null) {
            Intrinsics.checkExpressionValueIsNotNull(qs, "mQs");
            View header = qs.getHeader();
            Intrinsics.checkExpressionValueIsNotNull(header, "mQs.header");
            float height = (float) header.getHeight();
            float calculateQsTopPadding = super.calculateQsTopPadding();
            if (height <= calculateQsTopPadding) {
                f2 = RangesKt.coerceIn(this.mQsTopPadding + f, height, calculateQsTopPadding);
            } else {
                f2 = RangesKt.coerceIn(this.mQsTopPadding + f, calculateQsTopPadding, height);
            }
            updateScrollerTopPadding(f2);
        }
    }

    /* access modifiers changed from: private */
    public final void updateScrollerTopPadding(float f) {
        QS qs;
        ControlPanelController controlPanelController2 = this.controlPanelController.get();
        Intrinsics.checkExpressionValueIsNotNull(controlPanelController2, "controlPanelController.get()");
        if (!controlPanelController2.isUseControlCenter() && (qs = this.mQs) != null) {
            this.mQsTopPadding = f;
            Intrinsics.checkExpressionValueIsNotNull(qs, "mQs");
            View header = qs.getHeader();
            Intrinsics.checkExpressionValueIsNotNull(header, "mQs.header");
            this.mNssCoveredQs = f == ((float) header.getHeight());
            updateQsFraction(this.mQsTopPadding);
            requestScrollerTopPaddingUpdate(false);
        }
    }

    private final void updateQsFraction(float f) {
        QS qs = this.mQs;
        if (qs != null) {
            Intrinsics.checkExpressionValueIsNotNull(qs, "mQs");
            View header = qs.getHeader();
            Intrinsics.checkExpressionValueIsNotNull(header, "mQs.header");
            float calculateQsTopPadding = super.calculateQsTopPadding();
            float height = (calculateQsTopPadding - f) / (calculateQsTopPadding - ((float) header.getHeight()));
            QS qs2 = this.mQs;
            Intrinsics.checkExpressionValueIsNotNull(qs2, "mQs");
            View findViewById = qs2.getView().findViewById(C0015R$id.qs_content);
            if (findViewById != null && findViewById.isShown()) {
                findViewById.setPivotX(((float) findViewById.getWidth()) * 0.5f);
                QS qs3 = this.mQs;
                Intrinsics.checkExpressionValueIsNotNull(qs3, "mQs");
                View header2 = qs3.getHeader();
                Intrinsics.checkExpressionValueIsNotNull(header2, "mQs.header");
                findViewById.setPivotY((float) header2.getHeight());
                float f2 = 1.0f - (0.100000024f * height);
                float f3 = 1.0f - (height * 1.0f);
                if (!Float.isNaN(f2) && !Float.isNaN(f2) && !Float.isNaN(f3)) {
                    findViewById.setScaleX(f2);
                    findViewById.setScaleY(f2);
                    findViewById.setAlpha(f3);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public final void endNssCoveringQsMotion(float f) {
        QS qs = this.mQs;
        if (qs != null) {
            Intrinsics.checkExpressionValueIsNotNull(qs, "mQs");
            View header = qs.getHeader();
            Intrinsics.checkExpressionValueIsNotNull(header, "mQs.header");
            float height = (float) header.getHeight();
            float calculateQsTopPadding = super.calculateQsTopPadding();
            float f2 = this.mQsTopPadding;
            boolean z = f2 == height || f < ((float) 0);
            if (!z) {
                height = calculateQsTopPadding;
            }
            ValueAnimator ofFloat = ValueAnimator.ofFloat(f2, height);
            this.mQsTopPaddingAnimator = ofFloat;
            ((NotificationPanelViewController) this).mFlingAnimationUtils.apply(ofFloat, f2, height, f);
            ValueAnimator valueAnimator2 = this.mQsTopPaddingAnimator;
            if (valueAnimator2 != null) {
                valueAnimator2.addUpdateListener(new MiuiNotificationPanelViewController$endNssCoveringQsMotion$1(this));
            }
            ValueAnimator valueAnimator3 = this.mQsTopPaddingAnimator;
            if (valueAnimator3 != null) {
                valueAnimator3.addListener(new MiuiNotificationPanelViewController$endNssCoveringQsMotion$2(this, z));
            }
            ValueAnimator valueAnimator4 = this.mQsTopPaddingAnimator;
            if (valueAnimator4 != null) {
                valueAnimator4.start();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private final void refreshNssCoveringQs() {
        ValueAnimator valueAnimator2 = this.mQsTopPaddingAnimator;
        if (valueAnimator2 != null) {
            valueAnimator2.end();
        } else {
            new Handler(Looper.getMainLooper()).post(new MiuiNotificationPanelViewController$refreshNssCoveringQs$2(this));
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.NotificationPanelViewController
    public float calculateQsTopPadding() {
        boolean isShowingUnimportant = FoldManager.Companion.isShowingUnimportant();
        float calculateQsTopPadding = super.calculateQsTopPadding();
        ControlPanelController controlPanelController2 = this.controlPanelController.get();
        Intrinsics.checkExpressionValueIsNotNull(controlPanelController2, "controlPanelController.get()");
        if (controlPanelController2.isUseControlCenter()) {
            float unimportantTarget = FoldManager.Companion.getUnimportantTarget();
            return (!isShowingUnimportant || unimportantTarget <= 0.0f) ? calculateQsTopPadding : unimportantTarget + ((float) this.mQsNotificationTopPadding);
        } else if (isShowingUnimportant) {
            return this.mQsExpansionHeight;
        } else {
            if ((isOnShade() || isOnShadeLocked()) && !isQsExpanded() && this.mQsTopPadding < calculateQsTopPadding && (this.mNssCoveringQs || this.mNssCoveredQs)) {
                return this.mQsTopPadding + ((float) this.mStickyHeaderHeight);
            }
            return !isOnKeyguard() ? calculateQsTopPadding + ((float) this.mStickyHeaderHeight) : calculateQsTopPadding;
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.NotificationPanelViewController
    public int getKeyguardNotificationStaticPadding() {
        int keyguardNotificationStaticPadding = super.getKeyguardNotificationStaticPadding();
        NotificationStackScrollLayoutExtKt.setStaticTopPadding(this.mNotificationStackScroller, keyguardNotificationStaticPadding);
        return keyguardNotificationStaticPadding;
    }

    @Override // com.android.systemui.statusbar.phone.PanelViewController
    public void instantCollapse() {
        super.instantCollapse();
        setMPanelAppeared(false);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.PanelViewController
    public void notifyBarPanelExpansionChanged() {
        super.notifyBarPanelExpansionChanged();
        if (!getMPanelAppeared()) {
            updateScrollerTopPadding(super.calculateQsTopPadding());
        }
        updateBlur();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.PanelViewController
    public void fling(float f, boolean z, boolean z2) {
        if (isOnKeyguard() || this.mExpandingFromHeadsUp) {
            super.fling(f, z, z2);
        } else {
            flingSpring(f, z);
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.PanelViewController
    public void fling(float f, boolean z, float f2, boolean z2) {
        if (z && !isNCSwitching()) {
            setExpandedHeightInternal((float) getMaxPanelHeight());
        }
        super.fling(f, z, f2, z2);
        if (getMPanelAppeared() != z) {
            setMPanelAppeared(z);
            updateBlur();
        }
    }

    private final void flingSpring(float f, boolean z) {
        String str = PanelViewController.TAG;
        Log.d(str, "flingSpring mSpringLength=" + this.mSpringLength + ", expand=" + z + ", vel=" + f);
        if (!z || this.mSpringLength <= ((float) 0)) {
            setMSpringLength(0.0f);
            return;
        }
        Folme.useValue("PanelViewSpring").setTo("PanelSpringRatio", Float.valueOf(this.mSpringLength)).to("PanelSpringRatio", Float.valueOf(0.0f), MiuiNotificationPanelViewControllerKt.access$getSPRING_ANIM_CONFIG$p());
    }

    /* access modifiers changed from: private */
    public final void cancelFlingSpring() {
        Log.d(PanelViewController.TAG, "cancelFlingSpring");
        Folme.useValue("PanelViewSpring").cancel("PanelSpringRatio");
        setMSpringLength(0.0f);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.NotificationPanelViewController
    public float getHeaderTranslation() {
        if (this.mNCSwitching) {
            return 0.0f;
        }
        float f = this.mSpringLength;
        return f != 0.0f ? f : super.getHeaderTranslation();
    }

    private final float afterFriction(float f, int i) {
        float f2 = (float) i;
        float coerceAtMost = RangesKt.coerceAtMost(f / f2, 1.0f);
        float f3 = coerceAtMost * coerceAtMost;
        return ((((f3 * coerceAtMost) / ((float) 3)) - f3) + coerceAtMost) * f2;
    }

    public final boolean isOnKeyguard() {
        return this.statusBarStateController.getState() == 1;
    }

    public final boolean isOnShade() {
        return this.mBarState == 0;
    }

    public final boolean isOnShadeLocked() {
        return this.mBarState == 2;
    }

    public final boolean isDefaultTheme() {
        return this.mIsDefaultTheme;
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r1v1, types: [com.android.systemui.statusbar.phone.MiuiNotificationPanelViewControllerKt$sam$java_util_function_BiConsumer$0] */
    /* JADX WARNING: Unknown variable types count: 1 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onViewAttachedToWindow(@org.jetbrains.annotations.Nullable android.view.View r4) {
        /*
            r3 = this;
            com.android.systemui.plugins.statusbar.StatusBarStateController r4 = r3.statusBarStateController
            r4.addCallback(r3)
            com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout r4 = r3.mNotificationStackScroller
            kotlin.reflect.KFunction<kotlin.Unit> r0 = r3.mSetExpandedHeight
            kotlin.jvm.functions.Function2 r0 = (kotlin.jvm.functions.Function2) r0
            if (r0 == 0) goto L_0x0013
            com.android.systemui.statusbar.phone.MiuiNotificationPanelViewControllerKt$sam$java_util_function_BiConsumer$0 r1 = new com.android.systemui.statusbar.phone.MiuiNotificationPanelViewControllerKt$sam$java_util_function_BiConsumer$0
            r1.<init>(r0)
            r0 = r1
        L_0x0013:
            java.util.function.BiConsumer r0 = (java.util.function.BiConsumer) r0
            r4.addOnExpandedHeightChangedListener(r0)
            com.android.keyguard.injector.KeyguardPanelViewInjector r4 = r3.mKeyguardPanelViewInjector
            com.android.systemui.statusbar.phone.NotificationPanelView r0 = r3.panelView
            com.android.systemui.statusbar.phone.KeyguardStatusBarView r1 = r3.mKeyguardStatusBar
            java.lang.String r2 = "mKeyguardStatusBar"
            kotlin.jvm.internal.Intrinsics.checkExpressionValueIsNotNull(r1, r2)
            r4.onViewAttachedToWindow(r0, r1)
            boolean r4 = com.android.systemui.statusbar.notification.NotificationSettingsHelper.showMiuiStyle()
            if (r4 == 0) goto L_0x0033
            com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout r4 = r3.mNotificationStackScroller
            com.android.systemui.statusbar.phone.MiuiNotificationPanelViewController$mChildPositionsChangedListener$1 r0 = r3.mChildPositionsChangedListener
            r4.setOnChildLocationsChangedListener(r0)
        L_0x0033:
            r3.initDismissView()
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.phone.MiuiNotificationPanelViewController.onViewAttachedToWindow(android.view.View):void");
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r1v0, types: [com.android.systemui.statusbar.phone.MiuiNotificationPanelViewControllerKt$sam$java_util_function_BiConsumer$0] */
    /* JADX WARNING: Unknown variable types count: 1 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onViewDetachedFromWindow(@org.jetbrains.annotations.Nullable android.view.View r3) {
        /*
            r2 = this;
            com.android.systemui.plugins.statusbar.StatusBarStateController r3 = r2.statusBarStateController
            r3.removeCallback(r2)
            com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout r3 = r2.mNotificationStackScroller
            kotlin.reflect.KFunction<kotlin.Unit> r0 = r2.mSetExpandedHeight
            kotlin.jvm.functions.Function2 r0 = (kotlin.jvm.functions.Function2) r0
            if (r0 == 0) goto L_0x0013
            com.android.systemui.statusbar.phone.MiuiNotificationPanelViewControllerKt$sam$java_util_function_BiConsumer$0 r1 = new com.android.systemui.statusbar.phone.MiuiNotificationPanelViewControllerKt$sam$java_util_function_BiConsumer$0
            r1.<init>(r0)
            r0 = r1
        L_0x0013:
            java.util.function.BiConsumer r0 = (java.util.function.BiConsumer) r0
            r3.removeOnExpandedHeightChangedListener(r0)
            com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout r3 = r2.mNotificationStackScroller
            r0 = 0
            r3.setOnChildLocationsChangedListener(r0)
            com.android.keyguard.injector.KeyguardPanelViewInjector r3 = r2.mKeyguardPanelViewInjector
            com.android.systemui.statusbar.phone.NotificationPanelView r2 = r2.panelView
            r3.onViewDetachedFromWindow(r2)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.phone.MiuiNotificationPanelViewController.onViewDetachedFromWindow(android.view.View):void");
    }

    @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
    public void onStateChanged(int i) {
        this.mBarState = i;
        boolean z = true;
        if ((1 > i || 2 < i) && (i != 0 || this.mExpandedHeight <= 0.0f)) {
            z = false;
        }
        setMPanelAppeared(z);
        if (i != 0) {
            requestNCSwitching(false);
        }
        updateBlur();
        updateThemeBackground();
        updateNotificationStackScrollerVisibility();
        ((KeyguardPanelViewInjector) Dependency.get(KeyguardPanelViewInjector.class)).onStatusBarStateChanged(i);
        this.controlPanelController.get().showDialog(false);
    }

    @Override // com.android.systemui.statusbar.phone.NotificationPanelViewController
    public void setTrackedHeadsUp(@Nullable ExpandableNotificationRow expandableNotificationRow) {
        super.setTrackedHeadsUp(expandableNotificationRow);
        if (expandableNotificationRow != null && !isTrackingMiniWindowHeadsUp()) {
            this.mExpandingFromHeadsUp = true;
        }
    }

    private final void maybeHandleQuickFling() {
        if (this.mQuickFlingHeadsUpTriggered) {
            NotificationStackScrollLayoutExtKt.generateHeadsUpChildrenPositionAnimation(this.mNotificationStackScroller);
            this.mHeadsUpManager.unpinAll(true);
            this.mQuickFlingHeadsUpTriggered = false;
            setMPanelAppeared(true);
            updateBlur();
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.PanelViewController, com.android.systemui.statusbar.phone.NotificationPanelViewController
    public void onExpandingFinished() {
        super.onExpandingFinished();
        this.mExpandingFromHeadsUp = false;
        setMStretchingFromHeadsUp(false);
        setMPanelAppeared(this.mExpandedHeight > 0.0f);
        requestNCSwitching(false);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.PanelViewController, com.android.systemui.statusbar.phone.NotificationPanelViewController
    public void onClosingFinished() {
        super.onClosingFinished();
        setMStretchingFromHeadsUp(false);
        requestNCSwitching(false);
    }

    /* access modifiers changed from: private */
    public final void setAppearFraction(float f, float f2) {
        if (this.mExpandingFromHeadsUp && f2 >= 1.0f) {
            allowStretchFromHeadsUp();
            this.mHeadsUpManager.releaseAllImmediately();
            updateBlur();
        }
    }

    @Override // com.android.systemui.statusbar.phone.NotificationPanelViewController
    public void startWaitingForOpenPanelGesture() {
        super.startWaitingForOpenPanelGesture();
        this.mExpectingSynthesizedDown = true;
        if (isFullyCollapsed() && this.mHeadsUpManager.hasPinnedHeadsUp()) {
            this.mExpandingFromHeadsUp = true;
        }
    }

    @Override // com.android.systemui.statusbar.phone.NotificationPanelViewController
    public void stopWaitingForOpenPanelGesture(boolean z, float f) {
        super.stopWaitingForOpenPanelGesture(z, f);
        this.mExpectingSynthesizedDown = false;
        if (!z && f > 1.0f && this.mExpandingFromHeadsUp) {
            this.mExpandingFromHeadsUp = false;
            this.mQuickFlingHeadsUpTriggered = true;
        }
    }

    private final void allowStretchFromHeadsUp() {
        setMPanelAppeared(true);
        this.mExpandingFromHeadsUp = false;
        this.mStretchFromHeadsUpRequested = true;
        setMStretchingFromHeadsUp(true);
        HeadsUpTouchHelper headsUpTouchHelper = this.mHeadsUpTouchHelper;
        Intrinsics.checkExpressionValueIsNotNull(headsUpTouchHelper, "mHeadsUpTouchHelper");
        headsUpTouchHelper.setTrackingHeadsUp(false);
    }

    @Override // com.android.systemui.statusbar.phone.NotificationPanelViewController
    public void animateToFullShade(long j) {
        super.animateToFullShade(j);
    }

    @Override // com.android.systemui.statusbar.phone.NotificationPanelViewController
    public void setQsExpansionEnabled(boolean z) {
        super.setQsExpansionEnabled(z);
        NotificationStackScrollLayoutExtKt.setQsExpansionEnabled(this.mNotificationStackScroller, z);
    }

    @Override // com.android.systemui.statusbar.phone.PanelViewController, com.android.systemui.statusbar.phone.NotificationPanelViewController
    public void setHeadsUpManager(@NotNull HeadsUpManagerPhone headsUpManagerPhone) {
        Intrinsics.checkParameterIsNotNull(headsUpManagerPhone, "headsUpManager");
        super.setHeadsUpManager(headsUpManagerPhone);
        HeadsUpTouchHelper.Callback headsUpCallback = this.mNotificationStackScroller.getHeadsUpCallback();
        Intrinsics.checkExpressionValueIsNotNull(headsUpCallback, "mNotificationStackScroller.headsUpCallback");
        HeadsUpTouchCallbackWrapper headsUpTouchCallbackWrapper = new HeadsUpTouchCallbackWrapper(this, headsUpManagerPhone, headsUpCallback);
        NotificationStackScrollLayout notificationStackScrollLayout = this.mNotificationStackScroller;
        this.mHeadsUpTouchHelper = new MiuiHeadsUpTouchHelper(headsUpManagerPhone, headsUpTouchCallbackWrapper, this, notificationStackScrollLayout, this.mNotificationEntryManager, notificationStackScrollLayout, this.mEventTracker);
    }

    @Override // com.android.systemui.statusbar.phone.NotificationPanelViewController
    public void setDozing(boolean z, boolean z2, @Nullable PointF pointF) {
        super.setDozing(z, z2, pointF);
        if (z) {
            this.wakeUpFromDozing = true;
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.NotificationPanelViewController
    public void updateNotificationViews(@Nullable String str) {
        super.updateNotificationViews(str);
        if (isOnKeyguard() && !this.mDozing) {
            ((KeyguardClockInjector) Dependency.get(KeyguardClockInjector.class)).getView().updateClockView(this.mNotificationStackScroller.getVisibleNotificationCount() != 0);
        }
        updateDismissView();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.NotificationPanelViewController
    public void positionClockAndNotifications() {
        if (this.mBarState == 1) {
            KeyguardClockContainer view = ((KeyguardClockInjector) Dependency.get(KeyguardClockInjector.class)).getView();
            int notGoneChildCount = this.mNotificationStackScroller.getNotGoneChildCount();
            StatusBar statusBar = this.mStatusBar;
            Intrinsics.checkExpressionValueIsNotNull(statusBar, "mStatusBar");
            this.mClockPositionAlgorithm.setupMiuiClock(view.getClockHeight(), (int) view.getClockVisibleHeight(), notGoneChildCount, statusBar.getKeyguardNotifications());
        }
        super.positionClockAndNotifications();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.NotificationPanelViewController
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mThemeBackgroundView = ((NotificationPanelViewController) this).mView.findViewById(C0015R$id.theme_background);
        this.mAwesomeLockScreenContainer = (FrameLayout) ((NotificationPanelViewController) this).mView.findViewById(C0015R$id.awesome_lock_screen_container);
        this.mDismissView = (DismissView) ((NotificationPanelViewController) this).mView.findViewById(C0015R$id.dismiss_view);
        this.mStickyGroupHeader = ((NotificationPanelViewController) this).mView.findViewById(C0015R$id.group_header);
        updateThemeBackground();
        NotificationPanelView notificationPanelView = ((NotificationPanelViewController) this).mView;
        Intrinsics.checkExpressionValueIsNotNull(notificationPanelView, "mView");
        Context context = notificationPanelView.getContext();
        Intrinsics.checkExpressionValueIsNotNull(context, "mView.context");
        Resources resources = context.getResources();
        Intrinsics.checkExpressionValueIsNotNull(resources, "mView.context.resources");
        float f = resources.getDisplayMetrics().density * ((float) 5);
        NotificationsQuickSettingsContainer notificationsQuickSettingsContainer = this.mNotificationContainerParent;
        if (notificationsQuickSettingsContainer != null) {
            KeyguardBottomAreaView keyguardBottomAreaView = this.mKeyguardBottomArea;
            if (keyguardBottomAreaView != null) {
                f = keyguardBottomAreaView.getElevation();
            }
            notificationsQuickSettingsContainer.setElevation(f + ((float) 1));
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.PanelViewController, com.android.systemui.statusbar.phone.NotificationPanelViewController
    public void loadDimens() {
        super.loadDimens();
        Object obj = Dependency.get(ControlPanelController.class);
        Intrinsics.checkExpressionValueIsNotNull(obj, "Dependency.get(ControlPanelController::class.java)");
        this.mQsNotificationTopPadding = ((ControlPanelController) obj).isUseControlCenter() ? this.mResources.getDimensionPixelSize(C0012R$dimen.qs_notification_padding) : 0;
    }

    @Override // com.android.systemui.statusbar.phone.PanelViewController, com.android.systemui.statusbar.phone.NotificationPanelViewController
    public void expand(boolean z) {
        super.expand(z);
        setMPanelAppeared(true);
        updateBlur();
        updateAwePauseResumeStatus();
    }

    @Override // com.android.systemui.statusbar.phone.PanelViewController, com.android.systemui.statusbar.phone.NotificationPanelViewController
    public void collapse(boolean z, float f) {
        super.collapse(z, f);
        setMPanelAppeared(false);
        updateBlur();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.NotificationPanelViewController
    public void setKeyguardStatusViewVisibility(int i, boolean z, boolean z2) {
        Ref$ObjectRef ref$ObjectRef = new Ref$ObjectRef();
        ref$ObjectRef.element = (T) ((KeyguardClockInjector) Dependency.get(KeyguardClockInjector.class)).getView();
        MiuiNotificationPanelViewController$setKeyguardStatusViewVisibility$mAnimateKeyguardClockInvisibleEndRunnable$1 miuiNotificationPanelViewController$setKeyguardStatusViewVisibility$mAnimateKeyguardClockInvisibleEndRunnable$1 = new MiuiNotificationPanelViewController$setKeyguardStatusViewVisibility$mAnimateKeyguardClockInvisibleEndRunnable$1(this, ref$ObjectRef);
        if ((z || this.mBarState != 1 || i == 1) && !z2) {
            int i2 = 4;
            if (this.mBarState == 2 && i == 1) {
                addAwesomeLockScreenIfNeed();
                ref$ObjectRef.element.animate().cancel();
                T t = ref$ObjectRef.element;
                if (this.mIsDefaultTheme) {
                    i2 = 0;
                }
                t.setVisibility(i2);
                this.mKeyguardStatusViewAnimating = true;
                ref$ObjectRef.element.setAlpha(0.0f);
                ref$ObjectRef.element.animate().alpha(1.0f).setStartDelay(0).setDuration(320).setInterpolator(Interpolators.ALPHA_IN).withEndAction(this.mAnimateKeyguardStatusViewVisibleEndRunnable);
            } else if (i == 1) {
                addAwesomeLockScreenIfNeed();
                ref$ObjectRef.element.animate().cancel();
                this.mKeyguardStatusViewAnimating = false;
                T t2 = ref$ObjectRef.element;
                if (this.mIsDefaultTheme) {
                    i2 = 0;
                }
                t2.setVisibility(i2);
                ref$ObjectRef.element.setAlpha(1.0f);
            } else {
                removeAwesomeLockScreen();
                ref$ObjectRef.element.animate().cancel();
                this.mKeyguardStatusViewAnimating = false;
                ref$ObjectRef.element.setVisibility(4);
                ref$ObjectRef.element.setAlpha(1.0f);
            }
        } else {
            ref$ObjectRef.element.animate().cancel();
            this.mKeyguardStatusViewAnimating = true;
            ref$ObjectRef.element.animate().alpha(0.0f).setStartDelay(0).setDuration(160).setInterpolator(Interpolators.ALPHA_OUT).withEndAction(miuiNotificationPanelViewController$setKeyguardStatusViewVisibility$mAnimateKeyguardClockInvisibleEndRunnable$1);
            if (z) {
                ViewPropertyAnimator animate = ref$ObjectRef.element.animate();
                KeyguardStateController keyguardStateController = this.mKeyguardStateController;
                Intrinsics.checkExpressionValueIsNotNull(keyguardStateController, "mKeyguardStateController");
                ViewPropertyAnimator startDelay = animate.setStartDelay(keyguardStateController.getKeyguardFadingAwayDuration());
                KeyguardStateController keyguardStateController2 = this.mKeyguardStateController;
                Intrinsics.checkExpressionValueIsNotNull(keyguardStateController2, "mKeyguardStateController");
                startDelay.setDuration(keyguardStateController2.getKeyguardFadingAwayDuration() / ((long) 2)).start();
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.NotificationPanelViewController
    public void setKeyguardBottomAreaVisibility(int i, boolean z) {
        super.setKeyguardBottomAreaVisibility(i, z);
        Log.d(PanelViewController.TAG, "setKeyguardBottomAreaVisibility statusBarState=" + i + " goingToFullShade=" + z);
        int i2 = 0;
        if (z || i == 2) {
            MiuiKeyguardFaceUnlockView miuiKeyguardFaceUnlockView = this.mMiuiKeyguardFaceUnlockView;
            Intrinsics.checkExpressionValueIsNotNull(miuiKeyguardFaceUnlockView, "mMiuiKeyguardFaceUnlockView");
            if (!((KeyguardUpdateMonitorInjector) Dependency.get(KeyguardUpdateMonitorInjector.class)).isFaceUnlock()) {
                i2 = 4;
            }
            miuiKeyguardFaceUnlockView.setVisibility(i2);
        } else if (i == 1) {
            KeyguardBottomAreaView keyguardBottomAreaView = this.mKeyguardBottomArea;
            Intrinsics.checkExpressionValueIsNotNull(keyguardBottomAreaView, "mKeyguardBottomArea");
            if (!this.mIsDefaultTheme) {
                i2 = 4;
            }
            keyguardBottomAreaView.setVisibility(i2);
            this.mMiuiKeyguardFaceUnlockView.updateFaceUnlockIconStatus();
            MiuiKeyguardFaceUnlockView miuiKeyguardFaceUnlockView2 = this.mMiuiKeyguardFaceUnlockView;
            Intrinsics.checkExpressionValueIsNotNull(miuiKeyguardFaceUnlockView2, "mMiuiKeyguardFaceUnlockView");
            miuiKeyguardFaceUnlockView2.setAlpha(1.0f);
        } else {
            MiuiKeyguardFaceUnlockView miuiKeyguardFaceUnlockView3 = this.mMiuiKeyguardFaceUnlockView;
            Intrinsics.checkExpressionValueIsNotNull(miuiKeyguardFaceUnlockView3, "mMiuiKeyguardFaceUnlockView");
            miuiKeyguardFaceUnlockView3.setVisibility(8);
            MiuiKeyguardFaceUnlockView miuiKeyguardFaceUnlockView4 = this.mMiuiKeyguardFaceUnlockView;
            Intrinsics.checkExpressionValueIsNotNull(miuiKeyguardFaceUnlockView4, "mMiuiKeyguardFaceUnlockView");
            miuiKeyguardFaceUnlockView4.setAlpha(1.0f);
        }
    }

    @Override // com.android.systemui.statusbar.phone.NotificationPanelViewController
    public void launchCamera(boolean z, int i) {
        super.launchCamera(z, i);
        this.mKeyguardPanelViewInjector.launchCamera(z);
    }

    @Override // com.android.systemui.statusbar.phone.NotificationPanelViewController
    public void updateResources(boolean z) {
        super.updateResources(z);
        NotificationPanelView notificationPanelView = ((NotificationPanelViewController) this).mView;
        Intrinsics.checkExpressionValueIsNotNull(notificationPanelView, "mView");
        Context context = notificationPanelView.getContext();
        Intrinsics.checkExpressionValueIsNotNull(context, "mView.context");
        int dimensionPixelSize = context.getResources().getDimensionPixelSize(C0012R$dimen.notification_left_right_margin);
        FrameLayout frameLayout = this.mQsFrame;
        if (frameLayout != null) {
            ViewGroup.LayoutParams layoutParams = frameLayout.getLayoutParams();
            if (layoutParams != null) {
                FrameLayout.LayoutParams layoutParams2 = (FrameLayout.LayoutParams) layoutParams;
                if (layoutParams2.leftMargin != dimensionPixelSize) {
                    layoutParams2.leftMargin = dimensionPixelSize;
                    layoutParams2.rightMargin = dimensionPixelSize;
                    FrameLayout frameLayout2 = this.mQsFrame;
                    if (frameLayout2 != null) {
                        frameLayout2.setLayoutParams(layoutParams2);
                    } else {
                        Intrinsics.throwNpe();
                        throw null;
                    }
                }
                NotificationStackScrollLayout notificationStackScrollLayout = this.mNotificationStackScroller;
                if (notificationStackScrollLayout != null) {
                    ViewGroup.LayoutParams layoutParams3 = notificationStackScrollLayout.getLayoutParams();
                    if (layoutParams3 != null) {
                        FrameLayout.LayoutParams layoutParams4 = (FrameLayout.LayoutParams) layoutParams3;
                        if (layoutParams4.leftMargin != dimensionPixelSize) {
                            layoutParams4.leftMargin = dimensionPixelSize;
                            layoutParams4.rightMargin = dimensionPixelSize;
                            NotificationStackScrollLayout notificationStackScrollLayout2 = this.mNotificationStackScroller;
                            if (notificationStackScrollLayout2 != null) {
                                notificationStackScrollLayout2.setLayoutParams(layoutParams4);
                            } else {
                                Intrinsics.throwNpe();
                                throw null;
                            }
                        }
                        if (z) {
                            boolean isDefaultLockScreenTheme = MiuiKeyguardUtils.isDefaultLockScreenTheme();
                            if (isDefaultLockScreenTheme != this.mIsDefaultTheme) {
                                String str = PanelViewController.TAG;
                                Slog.i(str, "default theme change: mIsDefaultTheme = " + this.mIsDefaultTheme + ", isDefaultTheme = " + isDefaultLockScreenTheme);
                            }
                            this.mIsDefaultTheme = isDefaultLockScreenTheme;
                            ChargeUtils.disableChargeAnimation(false);
                            if (this.mKeyguardShowing) {
                                if (this.mIsDefaultTheme) {
                                    removeAwesomeLockScreen();
                                } else {
                                    addAwesomeLockScreenIfNeed(true);
                                    updateAwePauseResumeStatus();
                                }
                            }
                            updateThemeBackground();
                            updateNotificationStackScrollerVisibility();
                            return;
                        }
                        return;
                    }
                    throw new TypeCastException("null cannot be cast to non-null type android.widget.FrameLayout.LayoutParams");
                }
                Intrinsics.throwNpe();
                throw null;
            }
            throw new TypeCastException("null cannot be cast to non-null type android.widget.FrameLayout.LayoutParams");
        }
        Intrinsics.throwNpe();
        throw null;
    }

    private final void updateThemeBackground() {
        View view = this.mThemeBackgroundView;
        if (view != null) {
            NotificationPanelView notificationPanelView = ((NotificationPanelViewController) this).mView;
            Intrinsics.checkExpressionValueIsNotNull(notificationPanelView, "mView");
            view.setBackground(notificationPanelView.getContext().getDrawable(C0013R$drawable.notification_panel_window_bg));
            view.setVisibility((MiuiThemeUtils.isDefaultSysUiTheme() || !isOnShade()) ? 8 : 0);
        }
    }

    /* access modifiers changed from: protected */
    public void updateAweQsExpandHeight() {
        AwesomeLockScreen awesomeLockScreen;
        if (!this.mIsDefaultTheme && (awesomeLockScreen = this.mAwesomeLockScreen) != null && awesomeLockScreen != null) {
            awesomeLockScreen.updateQsExpandHeight((float) getHeight());
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.NotificationPanelViewController
    public void updateAwePauseResumeStatus() {
        AwesomeLockScreen awesomeLockScreen = this.mAwesomeLockScreen;
        if (awesomeLockScreen != null) {
            awesomeLockScreen.updatePauseResumeStatus();
        }
    }

    public final void addAwesomeLockScreenIfNeed() {
        addAwesomeLockScreenIfNeed(false);
    }

    public final void addAwesomeLockScreenIfNeed(boolean z) {
        FrameLayout frameLayout;
        if ((this.mAwesomeLockScreen == null && !this.mIsDefaultTheme) || z) {
            String str = PanelViewController.TAG;
            Log.d(str, "addAwesomeLockScreenIfNeed: " + z);
            this.mAwesomeLockScreen = new AwesomeLockScreen(this.panelView.getContext(), this.mStatusBar, this.statusBarStateController, this, this.mBar, this.mKeyguardStateController);
            FrameLayout frameLayout2 = this.mAwesomeLockScreenContainer;
            if (frameLayout2 != null) {
                frameLayout2.removeAllViews();
            }
            FrameLayout frameLayout3 = this.mAwesomeLockScreenContainer;
            if (frameLayout3 != null) {
                frameLayout3.addView(this.mAwesomeLockScreen);
            }
            AwesomeLockScreen awesomeLockScreen = this.mAwesomeLockScreen;
            if (awesomeLockScreen != null) {
                awesomeLockScreen.setIsInteractive(this.mIsInteractive);
            }
        }
        if (this.mAwesomeLockScreen != null && (frameLayout = this.mAwesomeLockScreenContainer) != null) {
            frameLayout.setVisibility(0);
        }
    }

    public final void removeAwesomeLockScreen() {
        if (this.mAwesomeLockScreen != null) {
            Log.d(PanelViewController.TAG, "removeAwesomeLockScreen");
            AwesomeLockScreen awesomeLockScreen = this.mAwesomeLockScreen;
            if (awesomeLockScreen != null) {
                awesomeLockScreen.setIsInteractive(false);
            }
            FrameLayout frameLayout = this.mAwesomeLockScreenContainer;
            if (frameLayout != null) {
                frameLayout.removeAllViews();
            }
            this.mAwesomeLockScreen = null;
            FrameLayout frameLayout2 = this.mAwesomeLockScreenContainer;
            if (frameLayout2 != null) {
                frameLayout2.setVisibility(8);
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.NotificationPanelViewController
    public void setStatusBar(@Nullable StatusBar statusBar) {
        super.setStatusBar(statusBar);
        AwesomeLockScreen awesomeLockScreen = this.mAwesomeLockScreen;
        if (awesomeLockScreen != null) {
            awesomeLockScreen.setStatusBar(statusBar);
        }
    }

    @Override // com.android.systemui.statusbar.phone.PanelViewController, com.android.systemui.statusbar.phone.NotificationPanelViewController
    public void resetViews(boolean z) {
        super.resetViews(z);
        this.mNotificationStackScroller.setOverScrollAmount(0.0f, false, z, !z);
        if (isOnKeyguard() || isOnShade()) {
            this.mKeyguardPanelViewInjector.resetLockScreenMagazine();
            this.mKeyguardPanelViewInjector.resetKeyguardVerticalMoveHelper();
            if (!this.mLaunchingAffordance) {
                this.mKeyguardPanelViewInjector.resetKeyguardMoveHelper();
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.PanelViewController, com.android.systemui.statusbar.phone.NotificationPanelViewController
    @NotNull
    public PanelViewController.OnConfigurationChangedListener createOnConfigurationChangedListener() {
        return new MiuiOnConfigurationChangedListener();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.PanelViewController
    public boolean isStatusBarExpandable() {
        return !isOnKeyguard() || MiuiKeyguardUtils.supportExpandableStatusbarUnderKeyguard();
    }

    @Override // com.android.systemui.statusbar.phone.PanelViewController
    public void goToLockedShade(@Nullable View view) {
        if (isStatusBarExpandable()) {
            this.mShadeController.goToLockedShade(view);
        }
    }

    @NotNull
    public final Configuration getMConfiguration() {
        return this.mConfiguration;
    }

    /* compiled from: MiuiNotificationPanelViewController.kt */
    private final class MiuiOnConfigurationChangedListener extends NotificationPanelViewController.OnConfigurationChangedListener {
        /* JADX WARN: Incorrect args count in method signature: ()V */
        public MiuiOnConfigurationChangedListener() {
            super();
        }

        @Override // com.android.systemui.statusbar.phone.PanelViewController.OnConfigurationChangedListener, com.android.systemui.statusbar.phone.NotificationPanelViewController.OnConfigurationChangedListener, com.android.systemui.statusbar.phone.PanelView.OnConfigurationChangedListener
        public void onConfigurationChanged(@Nullable Configuration configuration) {
            super.onConfigurationChanged(configuration);
            Configuration mConfiguration = MiuiNotificationPanelViewController.this.getMConfiguration();
            if (configuration != null) {
                int updateFrom = mConfiguration.updateFrom(configuration);
                boolean z = (updateFrom & 128) != 0;
                boolean z2 = (updateFrom & 2048) != 0;
                if (z || z2) {
                    MiuiNotificationPanelViewController.this.mKeyguardPanelViewInjector.resetLockScreenMagazine();
                    MiuiNotificationPanelViewController.this.mKeyguardPanelViewInjector.initScreenSize();
                    MiuiNotificationPanelViewController.this.mKeyguardPanelViewInjector.updateKeyguardMoveForScreenSizeChange();
                    MiuiNotificationPanelViewController.this.refreshNssCoveringQs();
                }
                if (configuration.orientation != 1) {
                    ((ControlPanelController) MiuiNotificationPanelViewController.this.controlPanelController.get()).showDialog(false);
                    return;
                }
                return;
            }
            Intrinsics.throwNpe();
            throw null;
        }
    }

    public final void updateNotificationStackScrollerVisibility() {
        this.mNotificationStackScroller.setVisibility((!isOnKeyguard() || this.mIsDefaultTheme || this.mQsTracking || this.mQsExpanded) ? 0 : 4);
    }

    @Nullable
    public final KeyguardBottomAreaView getKeyguardBottomArea() {
        return this.mKeyguardBottomArea;
    }

    @Nullable
    public final MiuiKeyguardFaceUnlockView getKeyguardFaceUnlockView() {
        return this.mMiuiKeyguardFaceUnlockView;
    }

    public final void setQsTracking(boolean z) {
        this.mQsTracking = z;
    }

    @NotNull
    public final NotificationsQuickSettingsContainer getNotificationContainerParent() {
        NotificationsQuickSettingsContainer notificationsQuickSettingsContainer = this.mNotificationContainerParent;
        Intrinsics.checkExpressionValueIsNotNull(notificationsQuickSettingsContainer, "mNotificationContainerParent");
        return notificationsQuickSettingsContainer;
    }

    @Override // com.android.systemui.keyguard.WakefulnessLifecycle.Observer
    public void onStartedWakingUp() {
        this.mIsInteractive = true;
        AwesomeLockScreen awesomeLockScreen = this.mAwesomeLockScreen;
        if (awesomeLockScreen != null) {
            awesomeLockScreen.setIsInteractive(true);
        }
        String wakeupReason = ((KeyguardUpdateMonitorInjector) Dependency.get(KeyguardUpdateMonitorInjector.class)).getWakeupReason();
        Object obj = Dependency.get(MiuiFastUnlockController.class);
        Intrinsics.checkExpressionValueIsNotNull(obj, "Dependency.get(MiuiFastU…ckController::class.java)");
        if (!((MiuiFastUnlockController) obj).isFastUnlock() && !Intrinsics.areEqual("InCallPresenter", wakeupReason)) {
            startWakeupAnimation();
        }
    }

    @Override // com.android.systemui.keyguard.WakefulnessLifecycle.Observer
    public void onStartedGoingToSleep() {
        this.mIsInteractive = false;
        AwesomeLockScreen awesomeLockScreen = this.mAwesomeLockScreen;
        if (awesomeLockScreen != null) {
            awesomeLockScreen.setIsInteractive(false);
        }
        if (((MiuiKeyguardWallpaperControllerImpl) Dependency.get(MiuiKeyguardWallpaperControllerImpl.class)).isAodUsingSuperWallpaper()) {
            ((NotificationPanelViewController) this).mView.animate().cancel();
            ((NotificationPanelViewController) this).mView.animate().setListener(new MiuiNotificationPanelViewController$onStartedGoingToSleep$1(this)).alpha(0.0f).setDuration(500).start();
            return;
        }
        this.mKeyguardPanelViewInjector.setVisibility(4);
    }

    private final void startWakeupAnimation() {
        if (this.mBarState == 1) {
            KeyguardClockContainer view = this.mKeyguardClockInjector.getView();
            if (this.mIsDefaultTheme) {
                Context context = this.panelView.getContext();
                Intrinsics.checkExpressionValueIsNotNull(context, "panelView.context");
                float dimension = context.getResources().getDimension(C0012R$dimen.keyguard_clock_tranlation_y);
                if (DeviceConfig.isLowEndDevice()) {
                    view.startAnimation(MiuiAnimationUtils.INSTANCE.generalWakeupAlphaAnimation());
                } else {
                    view.startAnimation(MiuiAnimationUtils.INSTANCE.generalWakeupTranslateAnimation(dimension));
                }
                if (this.wakeUpFromDozing) {
                    this.wakeUpFromDozing = false;
                    this.panelView.post(new MiuiNotificationPanelViewController$startWakeupAnimation$1(this, dimension));
                } else {
                    startNotificationWakeAnimation(dimension);
                }
                KeyguardBottomAreaView keyguardBottomAreaView = this.mKeyguardBottomArea;
                Intrinsics.checkExpressionValueIsNotNull(keyguardBottomAreaView, "mKeyguardBottomArea");
                keyguardBottomAreaView.getLeftView().startAnimation(MiuiAnimationUtils.INSTANCE.generalWakeupScaleAnimation());
                KeyguardBottomAreaView keyguardBottomAreaView2 = this.mKeyguardBottomArea;
                Intrinsics.checkExpressionValueIsNotNull(keyguardBottomAreaView2, "mKeyguardBottomArea");
                keyguardBottomAreaView2.getRightView().startAnimation(MiuiAnimationUtils.INSTANCE.generalWakeupScaleAnimation());
                LockScreenMagazinePreView view2 = ((LockScreenMagazineController) Dependency.get(LockScreenMagazineController.class)).getView();
                if (view2.getMainLayout().getVisibility() == 0) {
                    view2.getMainLayout().startAnimation(MiuiAnimationUtils.INSTANCE.generalWakeupAlphaAnimation());
                }
            }
            view.updateTime();
            this.mKeyguardPanelViewInjector.resetKeyguardMoveHelper();
        }
        if (((MiuiKeyguardWallpaperControllerImpl) Dependency.get(MiuiKeyguardWallpaperControllerImpl.class)).isAodUsingSuperWallpaper()) {
            ((NotificationPanelViewController) this).mView.animate().cancel();
            ((NotificationPanelViewController) this).mView.animate().setListener(new MiuiNotificationPanelViewController$startWakeupAnimation$2(this)).alpha(1.0f).setDuration(500).start();
            return;
        }
        this.mKeyguardPanelViewInjector.setVisibility(0);
        this.mKeyguardPanelViewInjector.setAlpha(1.0f);
    }

    /* access modifiers changed from: private */
    public final void startNotificationWakeAnimation(float f) {
        List<NotificationEntry> finalVisibleNotifications = this.mNotificationEntryManager.getFinalVisibleNotifications();
        ArrayList arrayList = new ArrayList(CollectionsKt.collectionSizeOrDefault(finalVisibleNotifications, 10));
        Iterator<T> it = finalVisibleNotifications.iterator();
        while (it.hasNext()) {
            arrayList.add(it.next().getRow());
        }
        ArrayList arrayList2 = new ArrayList();
        for (Object obj : arrayList) {
            ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) obj;
            Intrinsics.checkExpressionValueIsNotNull(expandableNotificationRow, "it");
            if (!expandableNotificationRow.isChildInGroup()) {
                arrayList2.add(obj);
            }
        }
        Sequence filter = SequencesKt.filter(SequencesKt.filter(ConvenienceExtensionsKt.getChildren(this.mNotificationStackScroller), new MiuiNotificationPanelViewController$startNotificationWakeAnimation$animateShadeViews$1(arrayList2)), MiuiNotificationPanelViewController$startNotificationWakeAnimation$animateShadeViews$2.INSTANCE);
        int i = 0;
        for (Object obj2 : filter) {
            int i2 = i + 1;
            if (i >= 0) {
                View view = (View) obj2;
                if (DeviceConfig.isLowEndDevice()) {
                    view.startAnimation(MiuiAnimationUtils.INSTANCE.generalWakeupAlphaAnimation());
                } else {
                    Animation generalWakeupTranslateAnimation = MiuiAnimationUtils.INSTANCE.generalWakeupTranslateAnimation(f);
                    generalWakeupTranslateAnimation.setStartOffset(50 * ((long) i));
                    view.startAnimation(generalWakeupTranslateAnimation);
                }
                i = i2;
            } else {
                CollectionsKt.throwIndexOverflow();
                throw null;
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.NotificationPanelViewController
    public void setQsExpansion(float f) {
        if (!this.mNssCoveredQs) {
            ControlPanelController controlPanelController2 = this.controlPanelController.get();
            Intrinsics.checkExpressionValueIsNotNull(controlPanelController2, "controlPanelController.get()");
            if (!controlPanelController2.isUseControlCenter()) {
                super.setQsExpansion(f);
            }
            int i = this.mBarState;
            if (i == 2 || i == 1) {
                updateAweQsExpandHeight();
            }
            if (this.mBarState == 1) {
                updateKeyguardElementAlpha();
            }
        }
    }

    public final void setAnimatingHeight(float f) {
        QS qs;
        ControlPanelController controlPanelController2 = this.controlPanelController.get();
        Intrinsics.checkExpressionValueIsNotNull(controlPanelController2, "controlPanelController.get()");
        if (!controlPanelController2.isUseControlCenter() && (qs = this.mQs) != null) {
            this.mQsTopPadding = f;
            Intrinsics.checkExpressionValueIsNotNull(qs, "mQs");
            View header = qs.getHeader();
            Intrinsics.checkExpressionValueIsNotNull(header, "mQs.header");
            this.mNssCoveredQs = f == ((float) header.getHeight());
            super.setQsExpansion(f);
        }
    }

    @Override // com.android.systemui.statusbar.notification.unimportant.FoldListener
    public void showUnimportantNotifications() {
        if (!FoldManager.Companion.isUsingControlPanel()) {
            QS qs = this.mQs;
            if (!(qs instanceof MiuiQSFragment)) {
                return;
            }
            if (qs != null) {
                int showQSPanelAnimate = ((MiuiQSFragment) qs).getQsContainer().setShowQSPanelAnimate(false);
                if (showQSPanelAnimate != 0) {
                    changeExpansion(showQSPanelAnimate);
                    return;
                }
                return;
            }
            throw new TypeCastException("null cannot be cast to non-null type com.android.systemui.qs.MiuiQSFragment");
        }
    }

    @Override // com.android.systemui.statusbar.notification.unimportant.FoldListener
    public void resetAll(boolean z) {
        if (!FoldManager.Companion.isUsingControlPanel()) {
            QS qs = this.mQs;
            if (!(qs instanceof MiuiQSFragment)) {
                return;
            }
            if (qs == null) {
                throw new TypeCastException("null cannot be cast to non-null type com.android.systemui.qs.MiuiQSFragment");
            } else if (((MiuiQSFragment) qs).getQsContainer().setShowQSPanelAnimate(true) != 0) {
                setAnimatingHeight(0.0f);
                if (z) {
                    refreshNssCoveringQs();
                }
            }
        }
    }

    private final void changeExpansion(int i) {
        if (this.mQs != null) {
            float f = (float) i;
            ValueAnimator valueAnimator2 = this.valueAnimator;
            if (valueAnimator2 != null) {
                valueAnimator2.cancel();
            }
            ValueAnimator ofFloat = ValueAnimator.ofFloat(this.mQsExpansionHeight, f);
            this.valueAnimator = ofFloat;
            if (ofFloat != null) {
                ofFloat.addUpdateListener(new MiuiNotificationPanelViewController$changeExpansion$1(this));
            }
            ValueAnimator valueAnimator3 = this.valueAnimator;
            if (valueAnimator3 != null) {
                valueAnimator3.addListener(new MiuiNotificationPanelViewController$changeExpansion$2(this, f));
            }
            ValueAnimator valueAnimator4 = this.valueAnimator;
            if (valueAnimator4 != null) {
                valueAnimator4.setStartDelay(100);
            }
            ValueAnimator valueAnimator5 = this.valueAnimator;
            if (valueAnimator5 != null) {
                valueAnimator5.setDuration(300L);
            }
            ValueAnimator valueAnimator6 = this.valueAnimator;
            if (valueAnimator6 != null) {
                valueAnimator6.start();
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.NotificationPanelViewController
    @Nullable
    public View[] getQsDetailAnimatedViews() {
        return new View[]{this.mDismissView, this.mNotificationStackScroller};
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.NotificationPanelViewController
    public void updateExpandedHeight(float f) {
        super.updateExpandedHeight(f);
        if (this.mBarState == 1) {
            updateKeyguardElementAlpha();
        }
    }

    private final void updateKeyguardElementAlpha() {
        Object obj = Dependency.get(LockScreenMagazineController.class);
        Intrinsics.checkExpressionValueIsNotNull(obj, "Dependency.get(LockScree…neController::class.java)");
        if (!((LockScreenMagazineController) obj).isPreViewVisible() && this.mIsDefaultTheme) {
            float min = Math.min(getKeyguardContentsAlpha(), ((float) 1) - getQsExpansionFraction());
            int i = min == 0.0f ? 4 : 0;
            KeyguardBottomAreaView keyguardBottomAreaView = this.mKeyguardBottomArea;
            Intrinsics.checkExpressionValueIsNotNull(keyguardBottomAreaView, "mKeyguardBottomArea");
            keyguardBottomAreaView.setImportantForAccessibility(i);
            KeyguardBottomAreaView keyguardBottomAreaView2 = this.mKeyguardBottomArea;
            Intrinsics.checkExpressionValueIsNotNull(keyguardBottomAreaView2, "mKeyguardBottomArea");
            keyguardBottomAreaView2.setAlpha(min);
            if (!this.mKeyguardStatusViewAnimating) {
                ((KeyguardClockInjector) Dependency.get(KeyguardClockInjector.class)).getView().updateClock(min, i);
            }
            MiuiKeyguardFaceUnlockView miuiKeyguardFaceUnlockView = this.mMiuiKeyguardFaceUnlockView;
            Intrinsics.checkExpressionValueIsNotNull(miuiKeyguardFaceUnlockView, "mMiuiKeyguardFaceUnlockView");
            miuiKeyguardFaceUnlockView.setAlpha(min);
            Object obj2 = Dependency.get(LockScreenMagazineController.class);
            Intrinsics.checkExpressionValueIsNotNull(obj2, "Dependency.get(LockScree…neController::class.java)");
            LockScreenMagazinePreView view = ((LockScreenMagazineController) obj2).getView();
            Intrinsics.checkExpressionValueIsNotNull(view, "Dependency.get(LockScree…troller::class.java).view");
            View mainLayout = view.getMainLayout();
            Intrinsics.checkExpressionValueIsNotNull(mainLayout, "Dependency.get(LockScree…ass.java).view.mainLayout");
            mainLayout.setAlpha(min);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:14:0x002d  */
    /* JADX WARNING: Removed duplicated region for block: B:18:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private final void updateDismissView() {
        /*
            r5 = this;
            boolean r0 = r5.mKeyguardShowing
            r1 = 1
            if (r0 != 0) goto L_0x0028
            com.android.systemui.statusbar.phone.HeadsUpManagerPhone r0 = r5.mHeadsUpManager
            boolean r0 = r0.hasPinnedHeadsUp()
            if (r0 != 0) goto L_0x0028
            com.android.systemui.statusbar.phone.HeadsUpManagerPhone r0 = r5.mHeadsUpManager
            java.lang.String r2 = "mHeadsUpManager"
            kotlin.jvm.internal.Intrinsics.checkExpressionValueIsNotNull(r0, r2)
            boolean r0 = r0.isHeadsUpGoingAway()
            if (r0 != 0) goto L_0x0028
            boolean r0 = r5.hasActiveClearableNotifications()
            if (r0 == 0) goto L_0x0028
            boolean r0 = r5.getMPanelAppeared()
            if (r0 == 0) goto L_0x0028
            r0 = r1
            goto L_0x0029
        L_0x0028:
            r0 = 0
        L_0x0029:
            boolean r2 = r5.mShowDismissView
            if (r0 == r2) goto L_0x0058
            r5.mShowDismissView = r0
            java.lang.String r2 = com.android.systemui.statusbar.phone.PanelViewController.TAG
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = " updateDismissView "
            r3.append(r4)
            r3.append(r0)
            java.lang.String r3 = r3.toString()
            android.util.Log.v(r2, r3)
            com.miui.systemui.animation.OnAnimatorEndsListener r2 = new com.miui.systemui.animation.OnAnimatorEndsListener
            com.android.systemui.statusbar.phone.MiuiNotificationPanelViewController$updateDismissView$listener$1 r3 = new com.android.systemui.statusbar.phone.MiuiNotificationPanelViewController$updateDismissView$listener$1
            r3.<init>(r5, r0)
            r2.<init>(r1, r3)
            com.android.systemui.statusbar.views.DismissView r5 = r5.mDismissView
            if (r5 == 0) goto L_0x0058
            com.android.systemui.statusbar.notification.stack.PanelAppearDisappearEvent$Companion r3 = com.android.systemui.statusbar.notification.stack.PanelAppearDisappearEvent.Companion
            r3.animateAppearDisappear$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core(r5, r0, r2, r1)
        L_0x0058:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.phone.MiuiNotificationPanelViewController.updateDismissView():void");
    }

    public final int getAdditionalInsetBottom() {
        DismissView dismissView = this.mDismissView;
        if (dismissView != null) {
            return dismissView.getPanelAdditionalInsetBottom();
        }
        return 0;
    }

    private final void initDismissView() {
        DismissView dismissView = this.mDismissView;
        if (dismissView != null) {
            dismissView.setDrawables(C0013R$drawable.notifications_clear_all, C0013R$drawable.btn_clear_all);
        }
        DismissView dismissView2 = this.mDismissView;
        if (dismissView2 != null) {
            dismissView2.setOnClickListener(new MiuiNotificationPanelViewController$initDismissView$1(this));
        }
        setDismissView(this.mDismissView);
        DismissView dismissView3 = this.mDismissView;
        if (dismissView3 != null) {
            dismissView3.setAccessibilityTraversalAfter(C0015R$id.notification_stack_scroller);
        }
        DismissView dismissView4 = this.mDismissView;
        if (dismissView4 != null) {
            PanelAppearDisappearEvent.Companion.animateAppearDisappear$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core(dismissView4, false);
        }
        this.mNotificationStackScroller.setAccessibilityTraversalBefore(C0015R$id.dismiss_view);
        this.mNotificationStackScroller.setImportantForAccessibility(1);
        updateDismissView();
    }

    private final void setDismissView(DismissView dismissView) {
        int i;
        DismissView dismissView2 = this.mDismissView;
        if (dismissView2 != null) {
            i = this.mNotificationContainerParent.indexOfChild(dismissView2);
            this.mNotificationContainerParent.removeView(this.mDismissView);
        } else {
            i = -1;
        }
        this.mDismissView = dismissView;
        this.mNotificationContainerParent.addView(dismissView, i);
    }

    public final boolean isExpectingSynthesizedDown$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core() {
        return this.mExpectingSynthesizedDown;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.NotificationPanelViewController
    public boolean canPanelCollapseOnQQS(float f, float f2) {
        boolean z;
        int i;
        QS qs;
        if (this.mCollapsedOnDown || (z = this.mKeyguardShowing) || this.mQsExpanded) {
            return false;
        }
        if (z || (qs = this.mQs) == null) {
            KeyguardStatusBarView keyguardStatusBarView = this.mKeyguardStatusBar;
            Intrinsics.checkExpressionValueIsNotNull(keyguardStatusBarView, "mKeyguardStatusBar");
            i = keyguardStatusBarView.getBottom();
        } else if (qs != null) {
            View header = qs.getHeader();
            Intrinsics.checkExpressionValueIsNotNull(header, "mQs!!.header");
            int top = header.getTop();
            QS qs2 = this.mQs;
            if (qs2 != null) {
                i = top + qs2.getQsMinExpansionHeight();
            } else {
                Intrinsics.throwNpe();
                throw null;
            }
        } else {
            Intrinsics.throwNpe();
            throw null;
        }
        FrameLayout frameLayout = this.mQsFrame;
        Intrinsics.checkExpressionValueIsNotNull(frameLayout, "mQsFrame");
        if (f < frameLayout.getX()) {
            return false;
        }
        FrameLayout frameLayout2 = this.mQsFrame;
        Intrinsics.checkExpressionValueIsNotNull(frameLayout2, "mQsFrame");
        float x = frameLayout2.getX();
        FrameLayout frameLayout3 = this.mQsFrame;
        Intrinsics.checkExpressionValueIsNotNull(frameLayout3, "mQsFrame");
        if (f > x + ((float) frameLayout3.getWidth()) || f2 > ((float) i)) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x0093  */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x00a2  */
    @Override // com.android.systemui.statusbar.phone.NotificationPanelViewController
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean shouldQuickSettingsIntercept(float r8, float r9, float r10) {
        /*
        // Method dump skipped, instructions count: 164
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.phone.MiuiNotificationPanelViewController.shouldQuickSettingsIntercept(float, float, float):boolean");
    }

    private final void resetStickHeader(boolean z) {
        this.mStickyHeaderHeight = 0;
        if (z) {
            View view = this.mStickyGroupHeader;
            if (view != null) {
                view.setVisibility(8);
            }
            View view2 = this.mStickyGroupHeader;
            if (view2 != null) {
                view2.setAlpha(1.0f);
            }
            View view3 = this.mStickyGroupHeader;
            if (view3 != null) {
                view3.setScaleX(1.0f);
            }
            View view4 = this.mStickyGroupHeader;
            if (view4 != null) {
                view4.setScaleY(1.0f);
                return;
            }
            return;
        }
        View view5 = this.mStickyGroupHeader;
        if (view5 != null) {
            PanelAppearDisappearEvent.Companion.animateAppearDisappear$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core(view5, false);
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.PanelViewController
    public void runPeekAnimation(long j, float f, boolean z) {
        if (NotificationSettingsHelper.shouldRunPeekAnimation()) {
            super.runPeekAnimation(j, f, z);
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x0093  */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x00ca  */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x00e6  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final void updateBlur() {
        /*
        // Method dump skipped, instructions count: 447
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.phone.MiuiNotificationPanelViewController.updateBlur():void");
    }

    private final void cancelPanelBlur() {
        Folme.useValue("PanelBlur").setup("PanelBlurSetup").cancel();
        Folme.useValue("PanelBlur").setup("PanelBlurSetup").setTo("PanelBlurRatio", Float.valueOf(this.mBlurRatio));
        this.mBluring = false;
    }

    private final void startPanelBlurIfNeed(float f) {
        if (this.mBluring || this.mBlurRatio != f) {
            this.mBluring = true;
            Folme.useValue("PanelBlur").setup("PanelBlurSetup").to("PanelBlurRatio", Float.valueOf(f), MiuiNotificationPanelViewControllerKt.access$getBLUR_ANIM_CONFIG$p());
        }
    }

    /* access modifiers changed from: private */
    public final void onBouncerShowingChanged(boolean z) {
        ValueAnimator valueAnimator2;
        this.mKeyguardBouncerShowing = z;
        if (z) {
            NotificationPanelView notificationPanelView = ((NotificationPanelViewController) this).mView;
            Intrinsics.checkExpressionValueIsNotNull(notificationPanelView, "mView");
            notificationPanelView.setTransitionAlpha(0.0f);
        } else {
            NotificationPanelView notificationPanelView2 = ((NotificationPanelViewController) this).mView;
            Intrinsics.checkExpressionValueIsNotNull(notificationPanelView2, "mView");
            notificationPanelView2.setTransitionAlpha(1.0f);
        }
        ValueAnimator valueAnimator3 = this.mBouncerFractionAnimator;
        if (valueAnimator3 != null) {
            valueAnimator3.cancel();
        }
        this.mBouncerFractionAnimator = ObjectAnimator.ofFloat(this.mKeyguardBouncerFraction, 0.0f);
        if (z && KeyguardWallpaperUtils.isWallpaperShouldBlur() && !DeviceConfig.isLowEndDevice() && (valueAnimator2 = this.mBouncerFractionAnimator) != null) {
            valueAnimator2.setFloatValues(this.mKeyguardBouncerFraction, 1.0f);
        }
        ValueAnimator valueAnimator4 = this.mBouncerFractionAnimator;
        if (valueAnimator4 != null) {
            valueAnimator4.setInterpolator(Interpolators.DECELERATE_QUINT);
        }
        ValueAnimator valueAnimator5 = this.mBouncerFractionAnimator;
        if (valueAnimator5 != null) {
            valueAnimator5.setDuration(300L);
        }
        ValueAnimator valueAnimator6 = this.mBouncerFractionAnimator;
        if (valueAnimator6 != null) {
            valueAnimator6.addUpdateListener(new MiuiNotificationPanelViewController$onBouncerShowingChanged$1(this));
        }
        ValueAnimator valueAnimator7 = this.mBouncerFractionAnimator;
        if (valueAnimator7 != null) {
            valueAnimator7.start();
        }
    }

    /* access modifiers changed from: private */
    public final void setBouncerShowingFraction(float f) {
        setAlpha(((float) 1) - f);
        this.mKeyguardBouncerFraction = f;
        updateBlur();
    }

    @Override // com.android.systemui.statusbar.phone.PanelViewController, com.android.systemui.statusbar.phone.NotificationPanelViewController
    public void dump(@Nullable FileDescriptor fileDescriptor, @Nullable PrintWriter printWriter, @Nullable String[] strArr) {
        int i;
        super.dump(fileDescriptor, printWriter, strArr);
        if (printWriter != null) {
            printWriter.println("bluring=" + this.mBluring + " blurRunning=" + this.mBlurAnimRunning + ' ' + "mBlurRatio=" + this.mBlurRatio + " mStretchLength=" + this.mStretchLength + " mSpringLength=" + this.mSpringLength + ' ' + "mIsDefaultTheme=" + this.mIsDefaultTheme + " mIsDefaultSysUiTheme=" + MiuiThemeUtils.isDefaultSysUiTheme());
            StringBuilder sb = new StringBuilder();
            sb.append(" mTopPadding is ");
            sb.append(super.calculateQsTopPadding());
            sb.append(" getKeyguardNotificationStaticPadding");
            sb.append(getKeyguardNotificationStaticPadding());
            sb.append(" mQsMaxExpansionHeight ");
            sb.append(this.mQsMaxExpansionHeight);
            sb.append(" mClockPositionResult.stackScrollerPadding =");
            sb.append(this.mClockPositionResult.stackScrollerPadding);
            sb.append(" bypassEnabled=");
            sb.append(this.mKeyguardBypassController.getBypassEnabled());
            sb.append(' ');
            sb.append(" mNotificationStackScroller.isPulseExpanding()=");
            sb.append(this.mNotificationStackScroller.isPulseExpanding());
            sb.append(' ');
            sb.append(" mQs.header.height=");
            QS qs = this.mQs;
            if (qs != null) {
                Intrinsics.checkExpressionValueIsNotNull(qs, "mQs");
                View header = qs.getHeader();
                Intrinsics.checkExpressionValueIsNotNull(header, "mQs.header");
                i = header.getHeight();
            } else {
                i = 0;
            }
            sb.append(i);
            printWriter.println(sb.toString());
            return;
        }
        Intrinsics.throwNpe();
        throw null;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.NotificationPanelViewController
    public void updatePanelExpanded() {
        boolean z = this.mPanelExpanded;
        super.updatePanelExpanded();
        if (this.mPanelExpanded != z) {
            ((PanelExpansionObserver) Dependency.get(PanelExpansionObserver.class)).dispatchPanelExpansionChanged(this.mPanelExpanded);
            updateDismissView();
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.NotificationPanelViewController
    public void onQsExpansionStarted(int i) {
        super.onQsExpansionStarted(i);
        updateNotificationStackScrollerVisibility();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.NotificationPanelViewController
    public void setQsExpanded(boolean z) {
        boolean z2 = this.mQsExpanded != z;
        super.setQsExpanded(z);
        if (z2) {
            ((PanelExpansionObserver) Dependency.get(PanelExpansionObserver.class)).dispatchQsExpansionChanged(this.mQsExpanded);
        }
        updateNotificationStackScrollerVisibility();
        if (z) {
            ((NotificationStat) Dependency.get(NotificationStat.class)).onOpenQSPanel();
            ((LockScreenMagazineController) Dependency.get(LockScreenMagazineController.class)).onOpenQSPanel();
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.NotificationPanelViewController
    public void updateVerticalPanelPosition(float f) {
        setHorizontalPanelTranslation(0.0f);
    }

    /* access modifiers changed from: private */
    public final boolean isTrackingMiniWindowHeadsUp() {
        HeadsUpTouchHelper headsUpTouchHelper = this.mHeadsUpTouchHelper;
        return (headsUpTouchHelper instanceof MiuiHeadsUpTouchHelper) && ((MiuiHeadsUpTouchHelper) headsUpTouchHelper).isTrackingMiniWindowHeadsUp$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core();
    }
}
