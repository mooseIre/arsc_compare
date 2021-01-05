package com.android.keyguard.injector;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.hardware.display.DisplayManager;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.util.Slog;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.keyguard.KeyguardMoveHelper;
import com.android.keyguard.KeyguardMoveLeftController;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardVerticalMoveHelper;
import com.android.keyguard.MiuiKeyguardUpdateMonitorCallback;
import com.android.keyguard.magazine.LockScreenMagazineController;
import com.android.keyguard.magazine.LockScreenMagazinePreView;
import com.android.keyguard.negative.MiuiKeyguardMoveLeftViewContainer;
import com.android.keyguard.utils.MiuiKeyguardUtils;
import com.android.keyguard.utils.PhoneUtils;
import com.android.keyguard.wallpaper.IMiuiKeyguardWallpaperController;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0021R$string;
import com.android.systemui.Dependency;
import com.android.systemui.keyguard.WakefulnessLifecycle;
import com.android.systemui.statusbar.KeyguardIndicationController;
import com.android.systemui.statusbar.phone.DoubleTapHelper;
import com.android.systemui.statusbar.phone.KeyguardBottomAreaView;
import com.android.systemui.statusbar.phone.KeyguardStatusBarView;
import com.android.systemui.statusbar.phone.MiuiNotificationPanelViewController;
import com.android.systemui.statusbar.phone.NotificationPanelView;
import com.android.systemui.statusbar.phone.PanelView;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.policy.UserSwitcherController;
import com.miui.systemui.SettingsObserver;
import com.miui.systemui.statusbar.phone.ForceBlackObserver;
import com.miui.systemui.util.MiuiTextUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.StringCompanionObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: KeyguardPanelViewInjector.kt */
public final class KeyguardPanelViewInjector extends MiuiKeyguardUpdateMonitorCallback implements KeyguardMoveHelper.Callback, SettingsObserver.Callback, ForceBlackObserver.Callback, IMiuiKeyguardWallpaperController.IWallpaperChangeCallback {
    private KeyguardBottomAreaView mBottomAreaView;
    @NotNull
    private final Context mContext;
    private DoubleTapHelper mDoubleTapHelper;
    private int mDoubleTapMinimumValidThresholdBottom = this.mContext.getResources().getDimensionPixelSize(C0012R$dimen.double_tap_sleep_valid_minimum_bottom);
    private int mDoubleTapMinimumValidThresholdTop = this.mContext.getResources().getDimensionPixelSize(C0012R$dimen.double_tap_sleep_valid_minimum_top);
    private int mDoubleTapMinimumWidthThreshold = this.mContext.getResources().getDimensionPixelSize(C0012R$dimen.double_tap_sleep_valid_minimum_width);
    /* access modifiers changed from: private */
    public boolean mForceBlack;
    private ForceBlackObserver mForceBlackObserver;
    private float mHorizontalMoveDistance;
    private float mHorizontalMovePer;
    private KeyguardIndicationController mIndicationController;
    private boolean mIsBottomButtonMoving;
    /* access modifiers changed from: private */
    public KeyguardMoveHelper mKeyguardMoveHelper;
    private KeyguardStatusBarView mKeyguardStatusBarView;
    private KeyguardUpdateMonitor mKeyguardUpdateMonitor;
    private KeyguardUpdateMonitorInjector mKeyguardUpdateMonitorInjector;
    private KeyguardVerticalMoveHelper mKeyguardVerticalMoveHelper;
    private int mLastDensityDpi = -1;
    private int mLastOrientation = -1;
    /* access modifiers changed from: private */
    public MiuiKeyguardMoveLeftViewContainer mLeftView;
    /* access modifiers changed from: private */
    public Drawable mLeftViewBackgroundImageDrawable;
    private ImageView mLeftViewBackgroundView;
    private LockScreenMagazineController mLockScreenMagazineController;
    private final List<View> mMobileKeyGuardViews = new ArrayList();
    private NotificationPanelView mPanelView;
    private MiuiNotificationPanelViewController mPanelViewController;
    private PowerManager mPowerManager;
    private int mScreenHeight;
    private int mScreenWidth;
    private SettingsObserver mSettingsObserver;
    /* access modifiers changed from: private */
    public AlertDialog mSimLockedTipsDialog;
    @NotNull
    private final StatusBar mStatusBar;
    private boolean mSupportGestureWakeup;
    private TextView mSwitchSystemUserEntrance;
    private UserSwitcherController mUserContextController;
    private final WakefulnessLifecycle.Observer mWakeObserver = new KeyguardPanelViewInjector$mWakeObserver$1(this);
    private IMiuiKeyguardWallpaperController mWallpaperController;
    private final WakefulnessLifecycle wakefulnessLifecycle;

    public boolean needsAntiFalsing() {
        return false;
    }

    public static final /* synthetic */ KeyguardIndicationController access$getMIndicationController$p(KeyguardPanelViewInjector keyguardPanelViewInjector) {
        KeyguardIndicationController keyguardIndicationController = keyguardPanelViewInjector.mIndicationController;
        if (keyguardIndicationController != null) {
            return keyguardIndicationController;
        }
        Intrinsics.throwUninitializedPropertyAccessException("mIndicationController");
        throw null;
    }

    public static final /* synthetic */ KeyguardStatusBarView access$getMKeyguardStatusBarView$p(KeyguardPanelViewInjector keyguardPanelViewInjector) {
        KeyguardStatusBarView keyguardStatusBarView = keyguardPanelViewInjector.mKeyguardStatusBarView;
        if (keyguardStatusBarView != null) {
            return keyguardStatusBarView;
        }
        Intrinsics.throwUninitializedPropertyAccessException("mKeyguardStatusBarView");
        throw null;
    }

    public static final /* synthetic */ KeyguardUpdateMonitor access$getMKeyguardUpdateMonitor$p(KeyguardPanelViewInjector keyguardPanelViewInjector) {
        KeyguardUpdateMonitor keyguardUpdateMonitor = keyguardPanelViewInjector.mKeyguardUpdateMonitor;
        if (keyguardUpdateMonitor != null) {
            return keyguardUpdateMonitor;
        }
        Intrinsics.throwUninitializedPropertyAccessException("mKeyguardUpdateMonitor");
        throw null;
    }

    public static final /* synthetic */ ImageView access$getMLeftViewBackgroundView$p(KeyguardPanelViewInjector keyguardPanelViewInjector) {
        ImageView imageView = keyguardPanelViewInjector.mLeftViewBackgroundView;
        if (imageView != null) {
            return imageView;
        }
        Intrinsics.throwUninitializedPropertyAccessException("mLeftViewBackgroundView");
        throw null;
    }

    public static final /* synthetic */ LockScreenMagazineController access$getMLockScreenMagazineController$p(KeyguardPanelViewInjector keyguardPanelViewInjector) {
        LockScreenMagazineController lockScreenMagazineController = keyguardPanelViewInjector.mLockScreenMagazineController;
        if (lockScreenMagazineController != null) {
            return lockScreenMagazineController;
        }
        Intrinsics.throwUninitializedPropertyAccessException("mLockScreenMagazineController");
        throw null;
    }

    public static final /* synthetic */ PowerManager access$getMPowerManager$p(KeyguardPanelViewInjector keyguardPanelViewInjector) {
        PowerManager powerManager = keyguardPanelViewInjector.mPowerManager;
        if (powerManager != null) {
            return powerManager;
        }
        Intrinsics.throwUninitializedPropertyAccessException("mPowerManager");
        throw null;
    }

    public static final /* synthetic */ TextView access$getMSwitchSystemUserEntrance$p(KeyguardPanelViewInjector keyguardPanelViewInjector) {
        TextView textView = keyguardPanelViewInjector.mSwitchSystemUserEntrance;
        if (textView != null) {
            return textView;
        }
        Intrinsics.throwUninitializedPropertyAccessException("mSwitchSystemUserEntrance");
        throw null;
    }

    public static final /* synthetic */ UserSwitcherController access$getMUserContextController$p(KeyguardPanelViewInjector keyguardPanelViewInjector) {
        UserSwitcherController userSwitcherController = keyguardPanelViewInjector.mUserContextController;
        if (userSwitcherController != null) {
            return userSwitcherController;
        }
        Intrinsics.throwUninitializedPropertyAccessException("mUserContextController");
        throw null;
    }

    public static final /* synthetic */ IMiuiKeyguardWallpaperController access$getMWallpaperController$p(KeyguardPanelViewInjector keyguardPanelViewInjector) {
        IMiuiKeyguardWallpaperController iMiuiKeyguardWallpaperController = keyguardPanelViewInjector.mWallpaperController;
        if (iMiuiKeyguardWallpaperController != null) {
            return iMiuiKeyguardWallpaperController;
        }
        Intrinsics.throwUninitializedPropertyAccessException("mWallpaperController");
        throw null;
    }

    @NotNull
    public final Context getMContext() {
        return this.mContext;
    }

    public KeyguardPanelViewInjector(@NotNull Context context, @NotNull StatusBar statusBar, @NotNull WakefulnessLifecycle wakefulnessLifecycle2) {
        Intrinsics.checkParameterIsNotNull(context, "mContext");
        Intrinsics.checkParameterIsNotNull(statusBar, "mStatusBar");
        Intrinsics.checkParameterIsNotNull(wakefulnessLifecycle2, "wakefulnessLifecycle");
        this.mContext = context;
        this.mStatusBar = statusBar;
        this.wakefulnessLifecycle = wakefulnessLifecycle2;
    }

    public final void init(@NotNull MiuiNotificationPanelViewController miuiNotificationPanelViewController) {
        Class cls = KeyguardNegative1PageInjector.class;
        Intrinsics.checkParameterIsNotNull(miuiNotificationPanelViewController, "notificationPanelViewController");
        this.mPanelView = miuiNotificationPanelViewController.getPanelView();
        this.mPanelViewController = miuiNotificationPanelViewController;
        if (miuiNotificationPanelViewController != null) {
            KeyguardBottomAreaView keyguardBottomArea = miuiNotificationPanelViewController.getKeyguardBottomArea();
            if (keyguardBottomArea != null) {
                this.mBottomAreaView = keyguardBottomArea;
                MiuiNotificationPanelViewController miuiNotificationPanelViewController2 = this.mPanelViewController;
                if (miuiNotificationPanelViewController2 == null) {
                    Intrinsics.throwUninitializedPropertyAccessException("mPanelViewController");
                    throw null;
                } else if (miuiNotificationPanelViewController2.getKeyguardFaceUnlockView() != null) {
                    Resources resources = this.mContext.getResources();
                    Intrinsics.checkExpressionValueIsNotNull(resources, "mContext.resources");
                    this.mLastOrientation = resources.getConfiguration().orientation;
                    this.mKeyguardMoveHelper = new KeyguardMoveHelper(this, this.mContext, miuiNotificationPanelViewController);
                    this.mKeyguardVerticalMoveHelper = new KeyguardVerticalMoveHelper(miuiNotificationPanelViewController);
                    LockScreenMagazineController lockScreenMagazineController = (LockScreenMagazineController) Dependency.get(LockScreenMagazineController.class);
                    lockScreenMagazineController.initAndUpdateParams(miuiNotificationPanelViewController.getMNotificationStackScroller());
                    Intrinsics.checkExpressionValueIsNotNull(lockScreenMagazineController, "Dependency.get(LockScree…otificationStackScroller)");
                    this.mLockScreenMagazineController = lockScreenMagazineController;
                    Object obj = Dependency.get(KeyguardIndicationController.class);
                    Intrinsics.checkExpressionValueIsNotNull(obj, "Dependency.get(KeyguardI…onController::class.java)");
                    this.mIndicationController = (KeyguardIndicationController) obj;
                    Object obj2 = Dependency.get(KeyguardUpdateMonitorInjector.class);
                    Intrinsics.checkExpressionValueIsNotNull(obj2, "Dependency.get(KeyguardU…itorInjector::class.java)");
                    this.mKeyguardUpdateMonitorInjector = (KeyguardUpdateMonitorInjector) obj2;
                    Object obj3 = Dependency.get(KeyguardUpdateMonitor.class);
                    Intrinsics.checkExpressionValueIsNotNull(obj3, "Dependency.get(KeyguardUpdateMonitor::class.java)");
                    this.mKeyguardUpdateMonitor = (KeyguardUpdateMonitor) obj3;
                    Object obj4 = Dependency.get(SettingsObserver.class);
                    Intrinsics.checkExpressionValueIsNotNull(obj4, "Dependency.get(SettingsObserver::class.java)");
                    this.mSettingsObserver = (SettingsObserver) obj4;
                    Object obj5 = Dependency.get(ForceBlackObserver.class);
                    Intrinsics.checkExpressionValueIsNotNull(obj5, "Dependency.get(ForceBlackObserver::class.java)");
                    this.mForceBlackObserver = (ForceBlackObserver) obj5;
                    Object obj6 = Dependency.get(IMiuiKeyguardWallpaperController.class);
                    Intrinsics.checkExpressionValueIsNotNull(obj6, "Dependency.get(IMiuiKeyg…erController::class.java)");
                    this.mWallpaperController = (IMiuiKeyguardWallpaperController) obj6;
                    Object obj7 = Dependency.get(UserSwitcherController.class);
                    Intrinsics.checkExpressionValueIsNotNull(obj7, "Dependency.get(UserSwitcherController::class.java)");
                    this.mUserContextController = (UserSwitcherController) obj7;
                    this.mLeftView = ((KeyguardNegative1PageInjector) Dependency.get(cls)).getLeftView();
                    this.mLeftViewBackgroundView = ((KeyguardNegative1PageInjector) Dependency.get(cls)).getLeftBackgroundView();
                    initSplitUserSpace();
                    initKeyguardBackground();
                    initKeyguardViewCollection();
                    updateResource(false);
                    initScreenSize();
                    Object systemService = this.mContext.getSystemService("power");
                    if (systemService != null) {
                        this.mPowerManager = (PowerManager) systemService;
                        this.mDoubleTapHelper = new DoubleTapHelper(this.mPanelView, 200, KeyguardPanelViewInjector$init$1.INSTANCE, new KeyguardPanelViewInjector$init$2(this), (DoubleTapHelper.SlideBackListener) null, (DoubleTapHelper.DoubleTapLogListener) null);
                        this.mSupportGestureWakeup = MiuiKeyguardUtils.isSupportGestureWakeup();
                        return;
                    }
                    throw new TypeCastException("null cannot be cast to non-null type android.os.PowerManager");
                } else {
                    Intrinsics.throwNpe();
                    throw null;
                }
            } else {
                Intrinsics.throwNpe();
                throw null;
            }
        } else {
            Intrinsics.throwUninitializedPropertyAccessException("mPanelViewController");
            throw null;
        }
    }

    private final void initKeyguardViewCollection() {
        this.mMobileKeyGuardViews.add(((KeyguardClockInjector) Dependency.get(KeyguardClockInjector.class)).getView());
        List<View> list = this.mMobileKeyGuardViews;
        MiuiNotificationPanelViewController miuiNotificationPanelViewController = this.mPanelViewController;
        if (miuiNotificationPanelViewController != null) {
            list.add(miuiNotificationPanelViewController.getNotificationContainerParent());
            List<View> list2 = this.mMobileKeyGuardViews;
            KeyguardBottomAreaView keyguardBottomAreaView = this.mBottomAreaView;
            if (keyguardBottomAreaView != null) {
                list2.add(keyguardBottomAreaView);
                List<View> list3 = this.mMobileKeyGuardViews;
                Object obj = Dependency.get(LockScreenMagazineController.class);
                Intrinsics.checkExpressionValueIsNotNull(obj, "Dependency.get(LockScree…neController::class.java)");
                LockScreenMagazinePreView view = ((LockScreenMagazineController) obj).getView();
                Intrinsics.checkExpressionValueIsNotNull(view, "Dependency.get(LockScree…troller::class.java).view");
                list3.add(view);
                List<View> list4 = this.mMobileKeyGuardViews;
                TextView textView = this.mSwitchSystemUserEntrance;
                if (textView != null) {
                    list4.add(textView);
                } else {
                    Intrinsics.throwUninitializedPropertyAccessException("mSwitchSystemUserEntrance");
                    throw null;
                }
            } else {
                Intrinsics.throwUninitializedPropertyAccessException("mBottomAreaView");
                throw null;
            }
        } else {
            Intrinsics.throwUninitializedPropertyAccessException("mPanelViewController");
            throw null;
        }
    }

    @NotNull
    public final TextView getSwitchSystemUserEntrance() {
        TextView textView = this.mSwitchSystemUserEntrance;
        if (textView != null) {
            return textView;
        }
        Intrinsics.throwUninitializedPropertyAccessException("mSwitchSystemUserEntrance");
        throw null;
    }

    private final void initSplitUserSpace() {
        NotificationPanelView notificationPanelView = this.mPanelView;
        if (notificationPanelView != null) {
            View findViewById = notificationPanelView.findViewById(C0015R$id.switch_to_system_user);
            Intrinsics.checkExpressionValueIsNotNull(findViewById, "mPanelView!!.findViewByI…id.switch_to_system_user)");
            TextView textView = (TextView) findViewById;
            this.mSwitchSystemUserEntrance = textView;
            if (textView != null) {
                textView.setOnClickListener(new KeyguardPanelViewInjector$initSplitUserSpace$1(this));
            } else {
                Intrinsics.throwUninitializedPropertyAccessException("mSwitchSystemUserEntrance");
                throw null;
            }
        } else {
            Intrinsics.throwNpe();
            throw null;
        }
    }

    public final void onViewAttachedToWindow(@NotNull NotificationPanelView notificationPanelView, @NotNull KeyguardStatusBarView keyguardStatusBarView) {
        Intrinsics.checkParameterIsNotNull(notificationPanelView, "panelView");
        Intrinsics.checkParameterIsNotNull(keyguardStatusBarView, "keyguardStatusBar");
        this.mPanelView = notificationPanelView;
        this.mKeyguardStatusBarView = keyguardStatusBarView;
        notificationPanelView.post(new KeyguardPanelViewInjector$onViewAttachedToWindow$1(this));
        ForceBlackObserver forceBlackObserver = this.mForceBlackObserver;
        if (forceBlackObserver != null) {
            forceBlackObserver.addCallback(this);
            SettingsObserver settingsObserver = this.mSettingsObserver;
            if (settingsObserver != null) {
                settingsObserver.addCallback(this, "status_bar_expandable_under_keyguard", "gesture_wakeup");
                IMiuiKeyguardWallpaperController iMiuiKeyguardWallpaperController = this.mWallpaperController;
                if (iMiuiKeyguardWallpaperController != null) {
                    iMiuiKeyguardWallpaperController.registerWallpaperChangeCallback(this);
                    KeyguardUpdateMonitor keyguardUpdateMonitor = this.mKeyguardUpdateMonitor;
                    if (keyguardUpdateMonitor != null) {
                        keyguardUpdateMonitor.registerCallback(this);
                        this.wakefulnessLifecycle.addObserver(this.mWakeObserver);
                        return;
                    }
                    Intrinsics.throwUninitializedPropertyAccessException("mKeyguardUpdateMonitor");
                    throw null;
                }
                Intrinsics.throwUninitializedPropertyAccessException("mWallpaperController");
                throw null;
            }
            Intrinsics.throwUninitializedPropertyAccessException("mSettingsObserver");
            throw null;
        }
        Intrinsics.throwUninitializedPropertyAccessException("mForceBlackObserver");
        throw null;
    }

    public final void onViewDetachedFromWindow(@NotNull NotificationPanelView notificationPanelView) {
        KeyguardMoveLeftController leftMovementController;
        Intrinsics.checkParameterIsNotNull(notificationPanelView, "panelView");
        this.mPanelView = notificationPanelView;
        ForceBlackObserver forceBlackObserver = this.mForceBlackObserver;
        if (forceBlackObserver != null) {
            forceBlackObserver.removeCallback(this);
            SettingsObserver settingsObserver = this.mSettingsObserver;
            if (settingsObserver != null) {
                settingsObserver.removeCallback(this);
                KeyguardMoveHelper keyguardMoveHelper = this.mKeyguardMoveHelper;
                if (!(keyguardMoveHelper == null || (leftMovementController = keyguardMoveHelper.getLeftMovementController()) == null)) {
                    leftMovementController.reset();
                }
                IMiuiKeyguardWallpaperController iMiuiKeyguardWallpaperController = this.mWallpaperController;
                if (iMiuiKeyguardWallpaperController != null) {
                    iMiuiKeyguardWallpaperController.unregisterWallpaperChangeCallback(this);
                    KeyguardUpdateMonitor keyguardUpdateMonitor = this.mKeyguardUpdateMonitor;
                    if (keyguardUpdateMonitor != null) {
                        keyguardUpdateMonitor.removeCallback(this);
                        this.wakefulnessLifecycle.removeObserver(this.mWakeObserver);
                        return;
                    }
                    Intrinsics.throwUninitializedPropertyAccessException("mKeyguardUpdateMonitor");
                    throw null;
                }
                Intrinsics.throwUninitializedPropertyAccessException("mWallpaperController");
                throw null;
            }
            Intrinsics.throwUninitializedPropertyAccessException("mSettingsObserver");
            throw null;
        }
        Intrinsics.throwUninitializedPropertyAccessException("mForceBlackObserver");
        throw null;
    }

    public void onContentChanged(@Nullable String str, @Nullable String str2) {
        if (str != null) {
            int hashCode = str.hashCode();
            if (hashCode != -1283918561) {
                if (hashCode == 4625141 && str.equals("gesture_wakeup")) {
                    MiuiKeyguardUtils.setContentObserverForGestureWakeup(MiuiTextUtils.parseBoolean(str2, false));
                }
            } else if (str.equals("status_bar_expandable_under_keyguard")) {
                MiuiKeyguardUtils.setExpandableStatusbarUnderKeyguard(MiuiTextUtils.parseBoolean(str2, true));
            }
        }
    }

    public final boolean onInterceptTouchEvent(@NotNull MotionEvent motionEvent) {
        Intrinsics.checkParameterIsNotNull(motionEvent, "event");
        initDownStates(motionEvent);
        if (((KeyguardBottomAreaInjector) Dependency.get(KeyguardBottomAreaInjector.class)).disallowTouchEvent(motionEvent)) {
            return true;
        }
        KeyguardMoveHelper keyguardMoveHelper = this.mKeyguardMoveHelper;
        if (keyguardMoveHelper != null) {
            return keyguardMoveHelper.onInterceptTouchEvent(motionEvent);
        }
        Intrinsics.throwNpe();
        throw null;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:53:0x0096, code lost:
        if (r3.isQsExpanded() != false) goto L_0x00a1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x00b1, code lost:
        if (r3.isFullyCollapsed() == false) goto L_0x00b3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:65:0x00b3, code lost:
        r1 = r1.mKeyguardVerticalMoveHelper;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:66:0x00b5, code lost:
        if (r1 == null) goto L_0x00bb;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:0x00b7, code lost:
        r1.onTouchEvent(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x00bb, code lost:
        kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException("mKeyguardVerticalMoveHelper");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:69:0x00c0, code lost:
        throw null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final boolean onTouchEvent(@org.jetbrains.annotations.NotNull android.view.MotionEvent r2, int r3, float r4, float r5, boolean r6, boolean r7, boolean r8, boolean r9, boolean r10, boolean r11) {
        /*
            r1 = this;
            java.lang.String r0 = "event"
            kotlin.jvm.internal.Intrinsics.checkParameterIsNotNull(r2, r0)
            r1.initDownStates(r2)
            r0 = 0
            if (r6 == 0) goto L_0x000d
            if (r7 == 0) goto L_0x001e
        L_0x000d:
            if (r8 != 0) goto L_0x001e
            if (r9 != 0) goto L_0x001e
            com.android.keyguard.KeyguardMoveHelper r6 = r1.mKeyguardMoveHelper
            if (r6 == 0) goto L_0x001a
            boolean r6 = r6.onTouchEvent(r2)
            goto L_0x001f
        L_0x001a:
            kotlin.jvm.internal.Intrinsics.throwNpe()
            throw r0
        L_0x001e:
            r6 = 0
        L_0x001f:
            boolean r7 = r1.mIsBottomButtonMoving
            r8 = 1
            if (r7 != 0) goto L_0x00d8
            com.android.keyguard.KeyguardMoveHelper r7 = r1.mKeyguardMoveHelper
            if (r7 == 0) goto L_0x0031
            boolean r7 = r7.isInLeftView()
            java.lang.Boolean r7 = java.lang.Boolean.valueOf(r7)
            goto L_0x0032
        L_0x0031:
            r7 = r0
        L_0x0032:
            if (r7 == 0) goto L_0x00d4
            boolean r7 = r7.booleanValue()
            if (r7 == 0) goto L_0x003c
            goto L_0x00d8
        L_0x003c:
            boolean r7 = r1.mSupportGestureWakeup
            if (r7 == 0) goto L_0x005a
            boolean r7 = com.android.keyguard.utils.MiuiKeyguardUtils.supportDoubleTapSleep()
            if (r7 == 0) goto L_0x005a
            boolean r7 = r1.isDoubleTapBoundaryTouchEvent(r2)
            if (r7 == 0) goto L_0x005a
            com.android.systemui.statusbar.phone.DoubleTapHelper r7 = r1.mDoubleTapHelper
            if (r7 == 0) goto L_0x0054
            r7.onTouchEvent(r2)
            goto L_0x005a
        L_0x0054:
            java.lang.String r1 = "mDoubleTapHelper"
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException(r1)
            throw r0
        L_0x005a:
            com.android.keyguard.magazine.LockScreenMagazineController r7 = r1.mLockScreenMagazineController
            java.lang.String r9 = "mLockScreenMagazineController"
            if (r7 == 0) goto L_0x00d0
            boolean r7 = r7.onTouchEvent(r2)
            if (r7 == 0) goto L_0x0067
            return r8
        L_0x0067:
            com.android.systemui.statusbar.KeyguardIndicationController r7 = r1.mIndicationController
            if (r7 == 0) goto L_0x00ca
            r7.onTouchEvent(r2, r3, r4, r5)
            com.android.keyguard.magazine.LockScreenMagazineController r3 = r1.mLockScreenMagazineController
            if (r3 == 0) goto L_0x00c6
            boolean r3 = r3.isPreViewVisible()
            if (r3 != 0) goto L_0x00c5
            int r3 = r2.getActionMasked()
            java.lang.String r4 = "mPanelViewController"
            if (r3 != 0) goto L_0x00a1
            com.android.systemui.statusbar.phone.MiuiNotificationPanelViewController r3 = r1.mPanelViewController
            if (r3 == 0) goto L_0x009d
            boolean r3 = r3.isFullyExpanded()
            if (r3 == 0) goto L_0x00a1
            if (r10 != 0) goto L_0x00a1
            if (r11 != 0) goto L_0x00a1
            com.android.systemui.statusbar.phone.MiuiNotificationPanelViewController r3 = r1.mPanelViewController
            if (r3 == 0) goto L_0x0099
            boolean r3 = r3.isQsExpanded()
            if (r3 == 0) goto L_0x00b3
            goto L_0x00a1
        L_0x0099:
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException(r4)
            throw r0
        L_0x009d:
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException(r4)
            throw r0
        L_0x00a1:
            if (r6 != 0) goto L_0x00c5
            int r3 = r2.getActionMasked()
            if (r3 == 0) goto L_0x00c5
            com.android.systemui.statusbar.phone.MiuiNotificationPanelViewController r3 = r1.mPanelViewController
            if (r3 == 0) goto L_0x00c1
            boolean r3 = r3.isFullyCollapsed()
            if (r3 != 0) goto L_0x00c5
        L_0x00b3:
            com.android.keyguard.KeyguardVerticalMoveHelper r1 = r1.mKeyguardVerticalMoveHelper
            if (r1 == 0) goto L_0x00bb
            r1.onTouchEvent(r2)
            goto L_0x00c5
        L_0x00bb:
            java.lang.String r1 = "mKeyguardVerticalMoveHelper"
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException(r1)
            throw r0
        L_0x00c1:
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException(r4)
            throw r0
        L_0x00c5:
            return r6
        L_0x00c6:
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException(r9)
            throw r0
        L_0x00ca:
            java.lang.String r1 = "mIndicationController"
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException(r1)
            throw r0
        L_0x00d0:
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException(r9)
            throw r0
        L_0x00d4:
            kotlin.jvm.internal.Intrinsics.throwNpe()
            throw r0
        L_0x00d8:
            return r8
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.keyguard.injector.KeyguardPanelViewInjector.onTouchEvent(android.view.MotionEvent, int, float, float, boolean, boolean, boolean, boolean, boolean, boolean):boolean");
    }

    public final void updateResource(boolean z) {
        NotificationPanelView notificationPanelView = this.mPanelView;
        if (notificationPanelView != null) {
            Context context = notificationPanelView.getContext();
            Intrinsics.checkExpressionValueIsNotNull(context, "it.context");
            Resources resources = context.getResources();
            Intrinsics.checkExpressionValueIsNotNull(resources, "it.context.resources");
            Configuration configuration = resources.getConfiguration();
            int i = configuration.orientation;
            if (i != this.mLastOrientation) {
                this.mLastOrientation = i;
                MiuiNotificationPanelViewController miuiNotificationPanelViewController = this.mPanelViewController;
                if (miuiNotificationPanelViewController != null) {
                    miuiNotificationPanelViewController.resetVerticalPanelPosition();
                    KeyguardMoveHelper keyguardMoveHelper = this.mKeyguardMoveHelper;
                    if (keyguardMoveHelper != null) {
                        keyguardMoveHelper.reset(true);
                    }
                } else {
                    Intrinsics.throwUninitializedPropertyAccessException("mPanelViewController");
                    throw null;
                }
            }
            int i2 = configuration.densityDpi;
            if (i2 != this.mLastDensityDpi) {
                this.mLastDensityDpi = i2;
                MiuiKeyguardMoveLeftViewContainer miuiKeyguardMoveLeftViewContainer = this.mLeftView;
                if (miuiKeyguardMoveLeftViewContainer != null) {
                    miuiKeyguardMoveLeftViewContainer.inflateLeftView();
                }
                KeyguardMoveHelper keyguardMoveHelper2 = this.mKeyguardMoveHelper;
                if (keyguardMoveHelper2 != null) {
                    keyguardMoveHelper2.reset(false);
                }
            }
            KeyguardMoveHelper keyguardMoveHelper3 = this.mKeyguardMoveHelper;
            if (keyguardMoveHelper3 != null) {
                keyguardMoveHelper3.updateResource(z);
            }
            LockScreenMagazineController lockScreenMagazineController = this.mLockScreenMagazineController;
            if (lockScreenMagazineController != null) {
                lockScreenMagazineController.updateResources(z);
            } else {
                Intrinsics.throwUninitializedPropertyAccessException("mLockScreenMagazineController");
                throw null;
            }
        }
    }

    private final void initKeyguardBackground() {
        Object obj = Dependency.get(LockScreenMagazineController.class);
        Intrinsics.checkExpressionValueIsNotNull(obj, "Dependency.get(LockScree…neController::class.java)");
        if (((LockScreenMagazineController) obj).isSupportLockScreenMagazineLeft()) {
            setDrawableFromOtherApk();
            return;
        }
        MiuiKeyguardMoveLeftViewContainer miuiKeyguardMoveLeftViewContainer = this.mLeftView;
        if (miuiKeyguardMoveLeftViewContainer != null) {
            miuiKeyguardMoveLeftViewContainer.setCustomBackground();
        }
        ImageView imageView = this.mLeftViewBackgroundView;
        if (imageView != null) {
            IMiuiKeyguardWallpaperController iMiuiKeyguardWallpaperController = this.mWallpaperController;
            if (iMiuiKeyguardWallpaperController != null) {
                imageView.setBackgroundColor(iMiuiKeyguardWallpaperController.getWallpaperBlurColor());
            } else {
                Intrinsics.throwUninitializedPropertyAccessException("mWallpaperController");
                throw null;
            }
        } else {
            Intrinsics.throwUninitializedPropertyAccessException("mLeftViewBackgroundView");
            throw null;
        }
    }

    private final void setDrawableFromOtherApk() {
        new KeyguardPanelViewInjector$setDrawableFromOtherApk$1(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
    }

    private final void initDownStates(MotionEvent motionEvent) {
        if (motionEvent.getActionMasked() == 0) {
            this.mIsBottomButtonMoving = false;
        }
    }

    public final void resetLockScreenMagazine() {
        LockScreenMagazineController lockScreenMagazineController = this.mLockScreenMagazineController;
        if (lockScreenMagazineController != null) {
            lockScreenMagazineController.reset();
        } else {
            Intrinsics.throwUninitializedPropertyAccessException("mLockScreenMagazineController");
            throw null;
        }
    }

    public final void resetKeyguardMoveHelper() {
        KeyguardMoveHelper keyguardMoveHelper = this.mKeyguardMoveHelper;
        if (keyguardMoveHelper != null) {
            keyguardMoveHelper.resetImmediately();
        }
    }

    public final void resetKeyguardVerticalMoveHelper() {
        KeyguardVerticalMoveHelper keyguardVerticalMoveHelper = this.mKeyguardVerticalMoveHelper;
        if (keyguardVerticalMoveHelper != null) {
            keyguardVerticalMoveHelper.reset();
        } else {
            Intrinsics.throwUninitializedPropertyAccessException("mKeyguardVerticalMoveHelper");
            throw null;
        }
    }

    public void onAnimationToSideStarted(boolean z, float f, float f2) {
        NotificationPanelView notificationPanelView = this.mPanelView;
        if (notificationPanelView == null || notificationPanelView.getLayoutDirection() != 1) {
            z = !z;
        }
        MiuiNotificationPanelViewController miuiNotificationPanelViewController = this.mPanelViewController;
        if (miuiNotificationPanelViewController != null) {
            miuiNotificationPanelViewController.setLaunchTransitionRunning(true);
            MiuiNotificationPanelViewController miuiNotificationPanelViewController2 = this.mPanelViewController;
            if (miuiNotificationPanelViewController2 != null) {
                miuiNotificationPanelViewController2.setLaunchAnimationEndRunnable((Runnable) null);
                float displayDensity = this.mStatusBar.getDisplayDensity();
                int abs = Math.abs((int) (f / displayDensity));
                int abs2 = Math.abs((int) (f2 / displayDensity));
                if (z) {
                    MiuiNotificationPanelViewController miuiNotificationPanelViewController3 = this.mPanelViewController;
                    if (miuiNotificationPanelViewController3 != null) {
                        miuiNotificationPanelViewController3.getLockscreenGestureLogger().write(190, abs, abs2);
                        MiuiNotificationPanelViewController miuiNotificationPanelViewController4 = this.mPanelViewController;
                        if (miuiNotificationPanelViewController4 != null) {
                            miuiNotificationPanelViewController4.getFalsingManager().onLeftAffordanceOn();
                        } else {
                            Intrinsics.throwUninitializedPropertyAccessException("mPanelViewController");
                            throw null;
                        }
                    } else {
                        Intrinsics.throwUninitializedPropertyAccessException("mPanelViewController");
                        throw null;
                    }
                } else {
                    MiuiNotificationPanelViewController miuiNotificationPanelViewController5 = this.mPanelViewController;
                    if (miuiNotificationPanelViewController5 != null) {
                        if (Intrinsics.areEqual((Object) "lockscreen_affordance", (Object) miuiNotificationPanelViewController5.getLastCameraLaunchSource())) {
                            MiuiNotificationPanelViewController miuiNotificationPanelViewController6 = this.mPanelViewController;
                            if (miuiNotificationPanelViewController6 != null) {
                                miuiNotificationPanelViewController6.getLockscreenGestureLogger().write(189, abs, abs2);
                            } else {
                                Intrinsics.throwUninitializedPropertyAccessException("mPanelViewController");
                                throw null;
                            }
                        }
                        MiuiNotificationPanelViewController miuiNotificationPanelViewController7 = this.mPanelViewController;
                        if (miuiNotificationPanelViewController7 != null) {
                            miuiNotificationPanelViewController7.getFalsingManager().onCameraOn();
                            KeyguardBottomAreaView keyguardBottomAreaView = this.mBottomAreaView;
                            if (keyguardBottomAreaView != null) {
                                MiuiNotificationPanelViewController miuiNotificationPanelViewController8 = this.mPanelViewController;
                                if (miuiNotificationPanelViewController8 != null) {
                                    keyguardBottomAreaView.launchCamera(miuiNotificationPanelViewController8.getLastCameraLaunchSource());
                                } else {
                                    Intrinsics.throwUninitializedPropertyAccessException("mPanelViewController");
                                    throw null;
                                }
                            } else {
                                Intrinsics.throwUninitializedPropertyAccessException("mBottomAreaView");
                                throw null;
                            }
                        } else {
                            Intrinsics.throwUninitializedPropertyAccessException("mPanelViewController");
                            throw null;
                        }
                    } else {
                        Intrinsics.throwUninitializedPropertyAccessException("mPanelViewController");
                        throw null;
                    }
                }
                this.mStatusBar.startLaunchTransitionTimeout();
                MiuiNotificationPanelViewController miuiNotificationPanelViewController9 = this.mPanelViewController;
                if (miuiNotificationPanelViewController9 != null) {
                    miuiNotificationPanelViewController9.setBlockTouch(true);
                } else {
                    Intrinsics.throwUninitializedPropertyAccessException("mPanelViewController");
                    throw null;
                }
            } else {
                Intrinsics.throwUninitializedPropertyAccessException("mPanelViewController");
                throw null;
            }
        } else {
            Intrinsics.throwUninitializedPropertyAccessException("mPanelViewController");
            throw null;
        }
    }

    public void triggerAction(boolean z, float f, float f2) {
        if (z) {
            KeyguardBottomAreaView keyguardBottomAreaView = this.mBottomAreaView;
            if (keyguardBottomAreaView != null) {
                MiuiNotificationPanelViewController miuiNotificationPanelViewController = this.mPanelViewController;
                if (miuiNotificationPanelViewController != null) {
                    keyguardBottomAreaView.launchCamera(miuiNotificationPanelViewController.getLastCameraLaunchSource());
                } else {
                    Intrinsics.throwUninitializedPropertyAccessException("mPanelViewController");
                    throw null;
                }
            } else {
                Intrinsics.throwUninitializedPropertyAccessException("mBottomAreaView");
                throw null;
            }
        } else {
            Object obj = Dependency.get(LockScreenMagazineController.class);
            Intrinsics.checkExpressionValueIsNotNull(obj, "Dependency.get(LockScree…neController::class.java)");
            if (((LockScreenMagazineController) obj).isSupportLockScreenMagazineLeft()) {
                KeyguardBottomAreaView keyguardBottomAreaView2 = this.mBottomAreaView;
                if (keyguardBottomAreaView2 != null) {
                    keyguardBottomAreaView2.launchMagazineLeftActivity();
                    MiuiKeyguardMoveLeftViewContainer miuiKeyguardMoveLeftViewContainer = this.mLeftView;
                    if (miuiKeyguardMoveLeftViewContainer != null) {
                        miuiKeyguardMoveLeftViewContainer.removeLeftView();
                        return;
                    }
                    return;
                }
                Intrinsics.throwUninitializedPropertyAccessException("mBottomAreaView");
                throw null;
            }
        }
    }

    public void onKeyguardOccludedChanged(boolean z) {
        ((KeyguardSensorInjector) Dependency.get(KeyguardSensorInjector.class)).disableFullScreenGesture();
        if (!z) {
            MiuiNotificationPanelViewController miuiNotificationPanelViewController = this.mPanelViewController;
            if (miuiNotificationPanelViewController != null) {
                if (!miuiNotificationPanelViewController.isOnKeyguard()) {
                    MiuiNotificationPanelViewController miuiNotificationPanelViewController2 = this.mPanelViewController;
                    if (miuiNotificationPanelViewController2 == null) {
                        Intrinsics.throwUninitializedPropertyAccessException("mPanelViewController");
                        throw null;
                    } else if (!miuiNotificationPanelViewController2.isOnShade()) {
                        return;
                    }
                }
                MiuiNotificationPanelViewController miuiNotificationPanelViewController3 = this.mPanelViewController;
                if (miuiNotificationPanelViewController3 != null) {
                    miuiNotificationPanelViewController3.resetViews(z);
                } else {
                    Intrinsics.throwUninitializedPropertyAccessException("mPanelViewController");
                    throw null;
                }
            } else {
                Intrinsics.throwUninitializedPropertyAccessException("mPanelViewController");
                throw null;
            }
        }
    }

    public void onKeyguardShowingChanged(boolean z) {
        ((KeyguardSensorInjector) Dependency.get(KeyguardSensorInjector.class)).disableFullScreenGesture();
    }

    public void onAnimationToSideEnded() {
        MiuiNotificationPanelViewController miuiNotificationPanelViewController = this.mPanelViewController;
        if (miuiNotificationPanelViewController != null) {
            miuiNotificationPanelViewController.setLaunchTransitionRunning(false);
            MiuiNotificationPanelViewController miuiNotificationPanelViewController2 = this.mPanelViewController;
            if (miuiNotificationPanelViewController2 != null) {
                miuiNotificationPanelViewController2.setLaunchTransitionFinished(true);
                MiuiNotificationPanelViewController miuiNotificationPanelViewController3 = this.mPanelViewController;
                if (miuiNotificationPanelViewController3 != null) {
                    if (miuiNotificationPanelViewController3.getLaunchAnimationEndRunnable() != null) {
                        MiuiNotificationPanelViewController miuiNotificationPanelViewController4 = this.mPanelViewController;
                        if (miuiNotificationPanelViewController4 != null) {
                            miuiNotificationPanelViewController4.getLaunchAnimationEndRunnable().run();
                            MiuiNotificationPanelViewController miuiNotificationPanelViewController5 = this.mPanelViewController;
                            if (miuiNotificationPanelViewController5 != null) {
                                miuiNotificationPanelViewController5.setLaunchAnimationEndRunnable((Runnable) null);
                            } else {
                                Intrinsics.throwUninitializedPropertyAccessException("mPanelViewController");
                                throw null;
                            }
                        } else {
                            Intrinsics.throwUninitializedPropertyAccessException("mPanelViewController");
                            throw null;
                        }
                    }
                    this.mStatusBar.readyForKeyguardDone();
                    return;
                }
                Intrinsics.throwUninitializedPropertyAccessException("mPanelViewController");
                throw null;
            }
            Intrinsics.throwUninitializedPropertyAccessException("mPanelViewController");
            throw null;
        }
        Intrinsics.throwUninitializedPropertyAccessException("mPanelViewController");
        throw null;
    }

    public float getMaxTranslationDistance() {
        NotificationPanelView notificationPanelView = this.mPanelView;
        Float valueOf = notificationPanelView != null ? Float.valueOf(hypotCompute((double) notificationPanelView.getWidth(), (double) notificationPanelView.getHeight())) : null;
        if (valueOf != null) {
            return valueOf.floatValue();
        }
        Intrinsics.throwNpe();
        throw null;
    }

    private final float hypotCompute(double d, double d2) {
        return (float) Math.hypot(d, d2);
    }

    public void onSwipingStarted() {
        NotificationPanelView notificationPanelView = this.mPanelView;
        if (notificationPanelView != null) {
            notificationPanelView.requestDisallowInterceptTouchEvent(true);
        }
        MiuiNotificationPanelViewController miuiNotificationPanelViewController = this.mPanelViewController;
        if (miuiNotificationPanelViewController != null) {
            miuiNotificationPanelViewController.setQsTracking(false);
            this.mIsBottomButtonMoving = true;
            return;
        }
        Intrinsics.throwUninitializedPropertyAccessException("mPanelViewController");
        throw null;
    }

    public void onSwipingAborted() {
        MiuiNotificationPanelViewController miuiNotificationPanelViewController = this.mPanelViewController;
        if (miuiNotificationPanelViewController != null) {
            miuiNotificationPanelViewController.getFalsingManager().onAffordanceSwipingAborted();
        } else {
            Intrinsics.throwUninitializedPropertyAccessException("mPanelViewController");
            throw null;
        }
    }

    public boolean isKeyguardWallpaperCarouselSwitchAnimating() {
        LockScreenMagazineController lockScreenMagazineController = this.mLockScreenMagazineController;
        if (lockScreenMagazineController != null) {
            return lockScreenMagazineController.isSwitchAnimating();
        }
        Intrinsics.throwUninitializedPropertyAccessException("mLockScreenMagazineController");
        throw null;
    }

    public void onHorizontalMove(float f, boolean z) {
        MiuiNotificationPanelViewController miuiNotificationPanelViewController = this.mPanelViewController;
        if (miuiNotificationPanelViewController != null) {
            if (!miuiNotificationPanelViewController.isOnKeyguard()) {
                KeyguardUpdateMonitorInjector keyguardUpdateMonitorInjector = this.mKeyguardUpdateMonitorInjector;
                if (keyguardUpdateMonitorInjector == null) {
                    Intrinsics.throwUninitializedPropertyAccessException("mKeyguardUpdateMonitorInjector");
                    throw null;
                } else if (!keyguardUpdateMonitorInjector.isKeyguardOccluded()) {
                    return;
                }
            }
            if (this.mHorizontalMoveDistance != f) {
                this.mHorizontalMoveDistance = f;
                float coerceAtMost = RangesKt___RangesKt.coerceAtMost(Math.abs(f) / 270.0f, 1.0f);
                if (this.mHorizontalMovePer != coerceAtMost) {
                    this.mHorizontalMovePer = coerceAtMost;
                    if (coerceAtMost == 0.0f || coerceAtMost == 1.0f) {
                        String str = PanelView.TAG;
                        Slog.i(str, "onHorizontalMove per = " + coerceAtMost);
                    }
                    NotificationPanelView notificationPanelView = this.mPanelView;
                    if (notificationPanelView != null) {
                        if (z) {
                            notificationPanelView.setAlpha(((float) 1) - coerceAtMost);
                        }
                        float f2 = 1.0f - (coerceAtMost * 0.1f);
                        notificationPanelView.setScaleX(f2);
                        notificationPanelView.setScaleY(f2);
                        return;
                    }
                    return;
                }
                return;
            }
            return;
        }
        Intrinsics.throwUninitializedPropertyAccessException("mPanelViewController");
        throw null;
    }

    @Nullable
    public List<View> getMobileView() {
        return this.mMobileKeyGuardViews;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x002e, code lost:
        if (r3.getCurrentUserId() != com.android.systemui.statusbar.policy.UserSwitcherController.getMaintenanceModeId()) goto L_0x0039;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final void onStatusBarStateChanged(int r4) {
        /*
            r3 = this;
            r3.updateNotificationStackScrollerVisibility()
            r0 = 1
            r1 = 0
            if (r4 != r0) goto L_0x0008
            goto L_0x0009
        L_0x0008:
            r0 = r1
        L_0x0009:
            com.android.keyguard.negative.MiuiKeyguardMoveLeftViewContainer r4 = r3.mLeftView
            if (r4 == 0) goto L_0x0015
            if (r0 == 0) goto L_0x0011
            r2 = r1
            goto L_0x0012
        L_0x0011:
            r2 = 4
        L_0x0012:
            r4.setVisibility(r2)
        L_0x0015:
            android.widget.TextView r4 = r3.mSwitchSystemUserEntrance
            r2 = 0
            if (r4 == 0) goto L_0x003d
            if (r0 == 0) goto L_0x0037
            boolean r0 = r3.shouldShowSwitchSystemUser()
            if (r0 == 0) goto L_0x0037
            com.android.systemui.statusbar.policy.UserSwitcherController r3 = r3.mUserContextController
            if (r3 == 0) goto L_0x0031
            int r3 = r3.getCurrentUserId()
            int r0 = com.android.systemui.statusbar.policy.UserSwitcherController.getMaintenanceModeId()
            if (r3 == r0) goto L_0x0037
            goto L_0x0039
        L_0x0031:
            java.lang.String r3 = "mUserContextController"
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException(r3)
            throw r2
        L_0x0037:
            r1 = 8
        L_0x0039:
            r4.setVisibility(r1)
            return
        L_0x003d:
            java.lang.String r3 = "mSwitchSystemUserEntrance"
            kotlin.jvm.internal.Intrinsics.throwUninitializedPropertyAccessException(r3)
            throw r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.keyguard.injector.KeyguardPanelViewInjector.onStatusBarStateChanged(int):void");
    }

    private final boolean shouldShowSwitchSystemUser() {
        UserSwitcherController userSwitcherController = this.mUserContextController;
        if (userSwitcherController != null) {
            if (!userSwitcherController.isOwnerUser()) {
                UserSwitcherController userSwitcherController2 = this.mUserContextController;
                if (userSwitcherController2 == null) {
                    Intrinsics.throwUninitializedPropertyAccessException("mUserContextController");
                    throw null;
                } else if (userSwitcherController2.getCurrentUserId() != UserSwitcherController.getSecondUser()) {
                    UserSwitcherController userSwitcherController3 = this.mUserContextController;
                    if (userSwitcherController3 == null) {
                        Intrinsics.throwUninitializedPropertyAccessException("mUserContextController");
                        throw null;
                    } else if (userSwitcherController3.getCurrentUserId() != UserSwitcherController.getKidSpaceUser()) {
                        return true;
                    }
                }
            }
            return false;
        }
        Intrinsics.throwUninitializedPropertyAccessException("mUserContextController");
        throw null;
    }

    public final void updateNotificationStackScrollerVisibility() {
        MiuiNotificationPanelViewController miuiNotificationPanelViewController = this.mPanelViewController;
        if (miuiNotificationPanelViewController != null) {
            miuiNotificationPanelViewController.updateNotificationStackScrollerVisibility();
        } else {
            Intrinsics.throwUninitializedPropertyAccessException("mPanelViewController");
            throw null;
        }
    }

    private final boolean isDoubleTapBoundaryTouchEvent(MotionEvent motionEvent) {
        float rawX = motionEvent.getRawX();
        float rawY = motionEvent.getRawY();
        int i = this.mDoubleTapMinimumWidthThreshold;
        return rawX >= ((float) i) && rawX <= ((float) (this.mScreenWidth - i)) && rawY >= ((float) this.mDoubleTapMinimumValidThresholdTop) && rawY <= ((float) (this.mScreenHeight - this.mDoubleTapMinimumValidThresholdBottom));
    }

    public final void initScreenSize() {
        Object systemService = this.mContext.getSystemService("display");
        if (systemService != null) {
            Display display = ((DisplayManager) systemService).getDisplay(0);
            Point point = new Point();
            display.getRealSize(point);
            this.mScreenWidth = point.x;
            this.mScreenHeight = point.y;
            return;
        }
        throw new TypeCastException("null cannot be cast to non-null type android.hardware.display.DisplayManager");
    }

    public void onForceBlackChange(boolean z, boolean z2) {
        this.mForceBlack = z;
    }

    public final boolean isForceBlack() {
        return this.mForceBlack;
    }

    public final void resetVerticalTouchEvent() {
        KeyguardVerticalMoveHelper keyguardVerticalMoveHelper = this.mKeyguardVerticalMoveHelper;
        if (keyguardVerticalMoveHelper != null) {
            keyguardVerticalMoveHelper.reset();
        } else {
            Intrinsics.throwUninitializedPropertyAccessException("mKeyguardVerticalMoveHelper");
            throw null;
        }
    }

    public void onWallpaperChange(boolean z) {
        TextView switchSystemUserEntrance = getSwitchSystemUserEntrance();
        switchSystemUserEntrance.setTextColor(z ? -1308622848 : -1);
        switchSystemUserEntrance.setCompoundDrawablesWithIntrinsicBounds(this.mContext.getResources().getDrawable(z ? C0013R$drawable.logout_light : C0013R$drawable.logout_dark), (Drawable) null, (Drawable) null, (Drawable) null);
        KeyguardStatusBarView keyguardStatusBarView = this.mKeyguardStatusBarView;
        if (keyguardStatusBarView != null) {
            keyguardStatusBarView.setDarkStyle(!this.mForceBlack && z);
        } else {
            Intrinsics.throwUninitializedPropertyAccessException("mKeyguardStatusBarView");
            throw null;
        }
    }

    public void onKeyguardVisibilityChanged(boolean z) {
        KeyguardUpdateMonitorInjector keyguardUpdateMonitorInjector = this.mKeyguardUpdateMonitorInjector;
        if (keyguardUpdateMonitorInjector == null) {
            Intrinsics.throwUninitializedPropertyAccessException("mKeyguardUpdateMonitorInjector");
            throw null;
        } else if (keyguardUpdateMonitorInjector.isSimLocked() && z) {
            handleSimLockedTipsDialog(true);
        }
    }

    public void onSimLockedStateChanged(boolean z) {
        handleSimLockedTipsDialog(z);
    }

    private final void hideSimLockedTipsDialog() {
        AlertDialog alertDialog = this.mSimLockedTipsDialog;
        if (alertDialog != null) {
            if (alertDialog != null) {
                alertDialog.dismiss();
            }
            this.mSimLockedTipsDialog = null;
        }
    }

    private final void showSimLockedTipsDialog() {
        String str;
        Window window;
        View decorView;
        Window window2;
        if (this.mSimLockedTipsDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this.mContext);
            builder.setTitle(this.mContext.getString(C0021R$string.sim_state_locked_dialog_title));
            StringCompanionObject stringCompanionObject = StringCompanionObject.INSTANCE;
            String string = this.mContext.getString(C0021R$string.sim_state_locked_puk_dialog_message);
            Intrinsics.checkExpressionValueIsNotNull(string, "mContext.getString(R.str…ocked_puk_dialog_message)");
            String format = String.format(string, Arrays.copyOf(new Object[]{10}, 1));
            Intrinsics.checkExpressionValueIsNotNull(format, "java.lang.String.format(format, *args)");
            builder.setMessage(format);
            if (PhoneUtils.isInCall(this.mContext)) {
                str = this.mContext.getString(C0021R$string.return_to_incall_screen);
            } else {
                str = this.mContext.getString(C0021R$string.emergency_call_string);
            }
            builder.setNeutralButton(str, new KeyguardPanelViewInjector$showSimLockedTipsDialog$1(this));
            builder.setCancelable(false);
            AlertDialog create = builder.create();
            this.mSimLockedTipsDialog = create;
            if (create != null) {
                create.setCanceledOnTouchOutside(false);
            }
            AlertDialog alertDialog = this.mSimLockedTipsDialog;
            if (!(alertDialog == null || (window2 = alertDialog.getWindow()) == null)) {
                window2.setType(2020);
            }
            alertDialogDecorViewAddFlag();
            AlertDialog alertDialog2 = this.mSimLockedTipsDialog;
            if (!(alertDialog2 == null || (window = alertDialog2.getWindow()) == null || (decorView = window.getDecorView()) == null)) {
                decorView.setOnSystemUiVisibilityChangeListener(new KeyguardPanelViewInjector$showSimLockedTipsDialog$2(this));
            }
            AlertDialog alertDialog3 = this.mSimLockedTipsDialog;
            if (alertDialog3 != null) {
                alertDialog3.show();
            }
        }
    }

    /* access modifiers changed from: private */
    public final void alertDialogDecorViewAddFlag() {
        Window window;
        View decorView;
        AlertDialog alertDialog = this.mSimLockedTipsDialog;
        if (alertDialog != null && (window = alertDialog.getWindow()) != null && (decorView = window.getDecorView()) != null) {
            decorView.setSystemUiVisibility(5638);
        }
    }

    public final void handleSimLockedTipsDialog(boolean z) {
        if (z) {
            showSimLockedTipsDialog();
        } else {
            hideSimLockedTipsDialog();
        }
    }

    public final void launchCamera(boolean z) {
        KeyguardMoveHelper keyguardMoveHelper = this.mKeyguardMoveHelper;
        if (keyguardMoveHelper != null) {
            NotificationPanelView notificationPanelView = this.mPanelView;
            boolean z2 = true;
            if (notificationPanelView == null || notificationPanelView.getLayoutDirection() != 1) {
                z2 = false;
            }
            keyguardMoveHelper.launchAffordance(z, z2);
        }
    }

    public final void updateBottomView(@NotNull KeyguardBottomAreaView keyguardBottomAreaView) {
        Intrinsics.checkParameterIsNotNull(keyguardBottomAreaView, "newBottomArea");
        KeyguardMoveHelper keyguardMoveHelper = this.mKeyguardMoveHelper;
        if (keyguardMoveHelper != null) {
            keyguardMoveHelper.updateBottomIcons(keyguardBottomAreaView);
        }
        this.mBottomAreaView = keyguardBottomAreaView;
        if (this.mMobileKeyGuardViews.size() > 0) {
            this.mMobileKeyGuardViews.clear();
            initKeyguardViewCollection();
        }
    }
}
