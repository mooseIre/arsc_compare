package com.android.keyguard.fod.policy;

import com.android.keyguard.fod.MiuiGxzwManager;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.SysuiStatusBarStateController;
import com.android.systemui.statusbar.phone.NotificationShadeWindowView;
import com.android.systemui.statusbar.phone.StatusBar;
import com.miui.systemui.statusbar.NotificationPanelExpansionListener;
import com.miui.systemui.statusbar.PanelExpansionObserver;
import kotlin.Lazy;
import kotlin.LazyKt;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.PropertyReference1Impl;
import kotlin.jvm.internal.Reflection;
import kotlin.reflect.KProperty;
import org.jetbrains.annotations.NotNull;

/* compiled from: MiuiGxzwPolicy.kt */
public final class MiuiGxzwPolicy implements NotificationPanelExpansionListener, StatusBarStateController.StateListener {
    static final /* synthetic */ KProperty[] $$delegatedProperties;
    private final Lazy mStatusBar$delegate;
    private int mStatusBarState;
    private final SysuiStatusBarStateController mStatusBarStateController;
    private final PanelExpansionObserver panelExpansionObserver;
    private final StatusBarStateController statusBarStateController;

    static {
        PropertyReference1Impl propertyReference1Impl = new PropertyReference1Impl(Reflection.getOrCreateKotlinClass(MiuiGxzwPolicy.class), "mStatusBar", "getMStatusBar()Lcom/android/systemui/statusbar/phone/StatusBar;");
        Reflection.property1(propertyReference1Impl);
        $$delegatedProperties = new KProperty[]{propertyReference1Impl};
    }

    private final StatusBar getMStatusBar() {
        Lazy lazy = this.mStatusBar$delegate;
        KProperty kProperty = $$delegatedProperties[0];
        return (StatusBar) lazy.getValue();
    }

    public MiuiGxzwPolicy(@NotNull PanelExpansionObserver panelExpansionObserver2, @NotNull StatusBarStateController statusBarStateController2, @NotNull dagger.Lazy<StatusBar> lazy) {
        Intrinsics.checkParameterIsNotNull(panelExpansionObserver2, "panelExpansionObserver");
        Intrinsics.checkParameterIsNotNull(statusBarStateController2, "statusBarStateController");
        Intrinsics.checkParameterIsNotNull(lazy, "statusBarLazy");
        this.panelExpansionObserver = panelExpansionObserver2;
        this.statusBarStateController = statusBarStateController2;
        this.mStatusBar$delegate = LazyKt.lazy(new MiuiGxzwPolicy$mStatusBar$2(lazy));
        StatusBarStateController statusBarStateController3 = this.statusBarStateController;
        if (statusBarStateController3 != null) {
            this.mStatusBarStateController = (SysuiStatusBarStateController) statusBarStateController3;
            return;
        }
        throw new TypeCastException("null cannot be cast to non-null type com.android.systemui.statusbar.SysuiStatusBarStateController");
    }

    public final void start() {
        this.panelExpansionObserver.addCallback((NotificationPanelExpansionListener) this);
        this.statusBarStateController.addCallback(this);
        StatusBar mStatusBar = getMStatusBar();
        Intrinsics.checkExpressionValueIsNotNull(mStatusBar, "mStatusBar");
        NotificationShadeWindowView notificationShadeWindowView = mStatusBar.getNotificationShadeWindowView();
        Intrinsics.checkExpressionValueIsNotNull(notificationShadeWindowView, "mStatusBar.notificationShadeWindowView");
        notificationShadeWindowView.getViewTreeObserver().addOnWindowFocusChangeListener(MiuiGxzwPolicy$start$1.INSTANCE);
    }

    @Override // com.miui.systemui.statusbar.NotificationPanelExpansionListener
    public void onQsExpanded(boolean z) {
        MiuiGxzwManager.getInstance().updateQsExpandedStatus(z);
    }

    @Override // com.miui.systemui.statusbar.NotificationPanelExpansionListener
    public void onPanelExpanded(boolean z) {
        MiuiGxzwManager.getInstance().updatePanelExpandedStatus(z);
    }

    @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
    public void onStateChanged(int i) {
        int i2 = this.mStatusBarState;
        boolean goingToFullShade = this.mStatusBarStateController.goingToFullShade();
        this.mStatusBarState = i;
        if (i2 == 1 && (goingToFullShade || i == 2)) {
            MiuiGxzwManager.getInstance().updateGxzwState();
        } else if (i2 == 2 && i == 1) {
            MiuiGxzwManager.getInstance().updateGxzwState();
        }
    }
}
