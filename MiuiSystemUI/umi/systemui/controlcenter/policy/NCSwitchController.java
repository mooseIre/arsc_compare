package com.android.systemui.controlcenter.policy;

import android.content.Context;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import com.android.systemui.Dependency;
import com.android.systemui.controlcenter.phone.ControlPanelController;
import com.android.systemui.controlcenter.phone.ControlPanelWindowManager;
import com.android.systemui.statusbar.SysuiStatusBarStateController;
import com.android.systemui.statusbar.notification.unimportant.FoldManager;
import com.android.systemui.statusbar.phone.HeadsUpManagerPhone;
import com.android.systemui.statusbar.phone.MiuiNotificationPanelViewController;
import com.android.systemui.statusbar.phone.NotificationPanelViewController;
import com.android.systemui.statusbar.phone.PanelViewLogger;
import com.android.systemui.statusbar.phone.ShadeController;
import com.android.systemui.statusbar.phone.StatusBar;
import com.miui.systemui.analytics.SystemUIStat;
import com.miui.systemui.events.NCSwitchEvent;
import kotlin.Lazy;
import kotlin.LazyKt__LazyJVMKt;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.PropertyReference1Impl;
import kotlin.jvm.internal.Reflection;
import kotlin.reflect.KProperty;
import org.jetbrains.annotations.NotNull;

/* compiled from: NCSwitchController.kt */
public final class NCSwitchController {
    static final /* synthetic */ KProperty[] $$delegatedProperties;
    private final NCSwitchEvent mCNSwitchStatEvent = new NCSwitchEvent(1);
    private final Context mContext;
    private final ControlPanelController mControlPanelController;
    private final Handler mHandler;
    private final HeadsUpManagerPhone mHeadsUpManager;
    private float mInitialTouchX;
    private float mInitialTouchY;
    private boolean mIsCNHandleTouch;
    private boolean mIsCNIntercepted;
    private boolean mIsNCIntercepted;
    private final NCSwitchEvent mNCSwitchStatEvent = new NCSwitchEvent(0);
    private final SysuiStatusBarStateController mStatusBarStateController;
    private final int mTouchSlop;
    private final PanelViewLogger panelViewLogger;
    private final Lazy resetSwitchingRunnable$delegate = LazyKt__LazyJVMKt.lazy(new NCSwitchController$resetSwitchingRunnable$2(this));
    private final ShadeController shadeColler;
    private final SystemUIStat systemUIStat;

    static {
        PropertyReference1Impl propertyReference1Impl = new PropertyReference1Impl(Reflection.getOrCreateKotlinClass(NCSwitchController.class), "resetSwitchingRunnable", "getResetSwitchingRunnable()Ljava/lang/Runnable;");
        Reflection.property1(propertyReference1Impl);
        $$delegatedProperties = new KProperty[]{propertyReference1Impl};
    }

    private final Runnable getResetSwitchingRunnable() {
        Lazy lazy = this.resetSwitchingRunnable$delegate;
        KProperty kProperty = $$delegatedProperties[0];
        return (Runnable) lazy.getValue();
    }

    public NCSwitchController(@NotNull Context context, @NotNull SysuiStatusBarStateController sysuiStatusBarStateController, @NotNull ControlPanelController controlPanelController, @NotNull ShadeController shadeController, @NotNull HeadsUpManagerPhone headsUpManagerPhone, @NotNull Handler handler, @NotNull PanelViewLogger panelViewLogger2, @NotNull SystemUIStat systemUIStat2) {
        Intrinsics.checkParameterIsNotNull(context, "mContext");
        Intrinsics.checkParameterIsNotNull(sysuiStatusBarStateController, "mStatusBarStateController");
        Intrinsics.checkParameterIsNotNull(controlPanelController, "mControlPanelController");
        Intrinsics.checkParameterIsNotNull(shadeController, "shadeColler");
        Intrinsics.checkParameterIsNotNull(headsUpManagerPhone, "mHeadsUpManager");
        Intrinsics.checkParameterIsNotNull(handler, "mHandler");
        Intrinsics.checkParameterIsNotNull(panelViewLogger2, "panelViewLogger");
        Intrinsics.checkParameterIsNotNull(systemUIStat2, "systemUIStat");
        this.mContext = context;
        this.mStatusBarStateController = sysuiStatusBarStateController;
        this.mControlPanelController = controlPanelController;
        this.shadeColler = shadeController;
        this.mHeadsUpManager = headsUpManagerPhone;
        this.mHandler = handler;
        this.panelViewLogger = panelViewLogger2;
        this.systemUIStat = systemUIStat2;
        ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
        Intrinsics.checkExpressionValueIsNotNull(viewConfiguration, "ViewConfiguration.get(mContext)");
        this.mTouchSlop = viewConfiguration.getScaledTouchSlop() * 4;
    }

    public final boolean onNCSwitchIntercept(@NotNull MotionEvent motionEvent, boolean z) {
        Intrinsics.checkParameterIsNotNull(motionEvent, "ev");
        if (this.mControlPanelController.isUseControlCenter() && this.mControlPanelController.isExpandable() && this.mStatusBarStateController.getState() == 0 && !skip()) {
            int actionMasked = motionEvent.getActionMasked();
            if (actionMasked == 0) {
                this.mInitialTouchX = motionEvent.getRawX();
                this.mInitialTouchY = motionEvent.getRawY();
            } else if (actionMasked != 1) {
                if (actionMasked == 2 && z && isLeftSlide(motionEvent)) {
                    this.mIsNCIntercepted = true;
                    return true;
                }
            } else if (z && isLeftSlide(motionEvent)) {
                this.mIsNCIntercepted = true;
                return true;
            }
        }
        return false;
    }

    public final boolean handleNCSwitchTouch(@NotNull MotionEvent motionEvent) {
        Intrinsics.checkParameterIsNotNull(motionEvent, "ev");
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked != 1) {
            if (actionMasked == 2 && this.mIsNCIntercepted && isLeftSlide(motionEvent)) {
                return true;
            }
        } else if (this.mIsNCIntercepted && isLeftSlide(motionEvent) && (this.mInitialTouchX - motionEvent.getRawX()) - Math.abs(this.mInitialTouchY - motionEvent.getRawY()) > ((float) this.mTouchSlop)) {
            this.panelViewLogger.logNcSwitch(true);
            handleNCSwitch();
            this.mIsNCIntercepted = false;
            this.systemUIStat.handleControlCenterEvent(this.mNCSwitchStatEvent);
            return true;
        }
        return false;
    }

    public final void handleNCSwitch() {
        prepareForNCSwitcher();
        this.shadeColler.collapsePanel(true);
        this.mControlPanelController.openPanelImmediately();
    }

    public final boolean onCNSwitchIntercept(@NotNull MotionEvent motionEvent) {
        Intrinsics.checkParameterIsNotNull(motionEvent, "ev");
        if (this.mStatusBarStateController.getState() != 0) {
            return false;
        }
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked == 0) {
            this.mInitialTouchX = motionEvent.getRawX();
            this.mInitialTouchY = motionEvent.getRawY();
            this.mIsCNIntercepted = false;
        } else if (actionMasked == 2) {
            if (this.mIsCNIntercepted) {
                return true;
            }
            if ((motionEvent.getRawX() - this.mInitialTouchX) - Math.abs(this.mInitialTouchY - motionEvent.getRawY()) > ((float) this.mTouchSlop)) {
                this.mIsCNIntercepted = true;
            }
        }
        return this.mIsCNIntercepted;
    }

    public final boolean handleCNSwitchTouch(@NotNull MotionEvent motionEvent, boolean z) {
        Intrinsics.checkParameterIsNotNull(motionEvent, "ev");
        if (this.mStatusBarStateController.getState() != 0) {
            return false;
        }
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked == 0) {
            this.mInitialTouchX = motionEvent.getRawX();
            this.mInitialTouchY = motionEvent.getRawY();
        } else if (actionMasked != 1) {
            if (actionMasked == 2) {
                if (!z || (motionEvent.getRawX() - this.mInitialTouchX) - Math.abs(this.mInitialTouchY - motionEvent.getRawY()) <= ((float) this.mTouchSlop)) {
                    this.mIsCNHandleTouch = false;
                    return false;
                }
                this.mIsCNHandleTouch = true;
                return true;
            }
        } else if (this.mIsCNHandleTouch) {
            this.panelViewLogger.logNcSwitch(false);
            handleCNSwitch();
            this.systemUIStat.handleControlCenterEvent(this.mCNSwitchStatEvent);
            this.mIsCNHandleTouch = false;
            return true;
        }
        return false;
    }

    public final void handleCNSwitch() {
        this.mHeadsUpManager.releaseAllImmediately();
        prepareForNCSwitcher();
        this.mControlPanelController.collapseControlCenter(true, true);
    }

    private final void prepareForNCSwitcher() {
        Object obj = Dependency.get(StatusBar.class);
        Intrinsics.checkExpressionValueIsNotNull(obj, "Dependency.get(StatusBar::class.java)");
        NotificationPanelViewController panelController = ((StatusBar) obj).getPanelController();
        if (panelController != null) {
            ((MiuiNotificationPanelViewController) panelController).requestNCSwitching(true);
            this.mControlPanelController.requestNCSwitching(true);
            if (this.mHandler.hasCallbacks(getResetSwitchingRunnable())) {
                this.mHandler.removeCallbacks(getResetSwitchingRunnable());
            }
            this.mHandler.postDelayed(getResetSwitchingRunnable(), 800);
            return;
        }
        throw new TypeCastException("null cannot be cast to non-null type com.android.systemui.statusbar.phone.MiuiNotificationPanelViewController");
    }

    private final boolean isLeftSlide(MotionEvent motionEvent) {
        return this.mInitialTouchX - motionEvent.getRawX() > Math.abs(this.mInitialTouchY - motionEvent.getRawY());
    }

    private final boolean skip() {
        return FoldManager.Companion.isShowingUnimportant();
    }

    public final void switchBlur(boolean z) {
        Object obj = Dependency.get(StatusBar.class);
        Intrinsics.checkExpressionValueIsNotNull(obj, "Dependency.get(StatusBar::class.java)");
        NotificationPanelViewController panelController = ((StatusBar) obj).getPanelController();
        if (panelController != null) {
            MiuiNotificationPanelViewController miuiNotificationPanelViewController = (MiuiNotificationPanelViewController) panelController;
            ControlPanelWindowManager controlPanelWindowManager = (ControlPanelWindowManager) Dependency.get(ControlPanelWindowManager.class);
            if (z) {
                Intrinsics.checkExpressionValueIsNotNull(controlPanelWindowManager, "controlPanelWindowManager");
                controlPanelWindowManager.setBlurRatio(1.0f);
                miuiNotificationPanelViewController.setMBlurRatio(0.0f);
                return;
            }
            miuiNotificationPanelViewController.setMBlurRatio(1.0f);
            Intrinsics.checkExpressionValueIsNotNull(controlPanelWindowManager, "controlPanelWindowManager");
            controlPanelWindowManager.setBlurRatio(0.0f);
            return;
        }
        throw new TypeCastException("null cannot be cast to non-null type com.android.systemui.statusbar.phone.MiuiNotificationPanelViewController");
    }
}
