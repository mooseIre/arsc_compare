package com.android.systemui.statusbar.phone;

import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import com.android.systemui.Dependency;
import com.android.systemui.assist.AssistManager;
import com.android.systemui.bubbles.BubbleController;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.NotificationPresenter;
import com.android.systemui.statusbar.notification.modal.ModalController;
import dagger.Lazy;
import java.util.ArrayList;

public class ShadeControllerImpl implements ShadeController {
    private final Lazy<AssistManager> mAssistManagerLazy;
    private final Lazy<BubbleController> mBubbleControllerLazy;
    private final CommandQueue mCommandQueue;
    private final int mDisplayId;
    protected final NotificationShadeWindowController mNotificationShadeWindowController;
    private final ArrayList<Runnable> mPostCollapseRunnables = new ArrayList<>();
    private final StatusBarKeyguardViewManager mStatusBarKeyguardViewManager;
    protected final Lazy<StatusBar> mStatusBarLazy;
    private final StatusBarStateController mStatusBarStateController;

    public ShadeControllerImpl(CommandQueue commandQueue, StatusBarStateController statusBarStateController, NotificationShadeWindowController notificationShadeWindowController, StatusBarKeyguardViewManager statusBarKeyguardViewManager, WindowManager windowManager, Lazy<StatusBar> lazy, Lazy<AssistManager> lazy2, Lazy<BubbleController> lazy3) {
        this.mCommandQueue = commandQueue;
        this.mStatusBarStateController = statusBarStateController;
        this.mNotificationShadeWindowController = notificationShadeWindowController;
        this.mStatusBarKeyguardViewManager = statusBarKeyguardViewManager;
        this.mDisplayId = windowManager.getDefaultDisplay().getDisplayId();
        this.mStatusBarLazy = lazy;
        this.mAssistManagerLazy = lazy2;
        this.mBubbleControllerLazy = lazy3;
    }

    public void instantExpandNotificationsPanel() {
        getStatusBar().makeExpandedVisible(true);
        getNotificationPanelViewController().expand(false);
        this.mCommandQueue.recomputeDisableFlags(this.mDisplayId, false);
    }

    public void animateCollapsePanels() {
        animateCollapsePanels(0);
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
        if (z || this.mStatusBarStateController.getState() == 0) {
            if ((i & 2) == 0) {
                getStatusBar().postHideRecentApps();
            }
            Log.v("ShadeControllerImpl", "NotificationShadeWindow: " + getNotificationShadeWindowView() + " canPanelBeCollapsed(): " + getNotificationPanelViewController().canPanelBeCollapsed());
            if (getNotificationShadeWindowView() == null || !getNotificationPanelViewController().canPanelBeCollapsed()) {
                this.mBubbleControllerLazy.get().collapseStack();
                return;
            }
            this.mNotificationShadeWindowController.setNotificationShadeFocusable(false);
            getStatusBar().getNotificationShadeWindowViewController().cancelExpandHelper();
            getStatusBarView().collapsePanel(true, z2, f);
            return;
        }
        runPostCollapseRunnables();
    }

    public boolean closeShadeIfOpen() {
        if (!getNotificationPanelViewController().isFullyCollapsed()) {
            this.mCommandQueue.animateCollapsePanels(2, true);
            getStatusBar().visibilityChanged(false);
            this.mAssistManagerLazy.get().hideAssist();
            ((ModalController) Dependency.get(ModalController.class)).animExitModal();
        }
        return false;
    }

    public void postOnShadeExpanded(final Runnable runnable) {
        getNotificationPanelViewController().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                if (ShadeControllerImpl.this.getStatusBar().getNotificationShadeWindowView().isVisibleToUser()) {
                    ShadeControllerImpl.this.getNotificationPanelViewController().removeOnGlobalLayoutListener(this);
                    ShadeControllerImpl.this.getNotificationPanelViewController().getView().post(runnable);
                }
            }
        });
    }

    public void addPostCollapseAction(Runnable runnable) {
        this.mPostCollapseRunnables.add(runnable);
    }

    public void runPostCollapseRunnables() {
        ArrayList arrayList = new ArrayList(this.mPostCollapseRunnables);
        this.mPostCollapseRunnables.clear();
        int size = arrayList.size();
        for (int i = 0; i < size; i++) {
            ((Runnable) arrayList.get(i)).run();
        }
        this.mStatusBarKeyguardViewManager.readyForKeyguardDone();
    }

    public void goToLockedShade(View view) {
        getStatusBar().goToLockedShade(view);
    }

    public boolean collapsePanel() {
        if (getNotificationPanelViewController().isFullyCollapsed()) {
            return false;
        }
        animateCollapsePanels(2, true, true);
        getStatusBar().visibilityChanged(false);
        return true;
    }

    public void collapsePanel(boolean z) {
        if (z) {
            if (!collapsePanel()) {
                runPostCollapseRunnables();
            }
        } else if (!getPresenter().isPresenterFullyCollapsed()) {
            getStatusBar().instantCollapseNotificationPanel();
            getStatusBar().visibilityChanged(false);
        } else {
            runPostCollapseRunnables();
        }
    }

    /* access modifiers changed from: private */
    public StatusBar getStatusBar() {
        return this.mStatusBarLazy.get();
    }

    private NotificationPresenter getPresenter() {
        return getStatusBar().getPresenter();
    }

    /* access modifiers changed from: protected */
    public NotificationShadeWindowView getNotificationShadeWindowView() {
        return getStatusBar().getNotificationShadeWindowView();
    }

    /* access modifiers changed from: protected */
    public PhoneStatusBarView getStatusBarView() {
        return (PhoneStatusBarView) getStatusBar().getStatusBarView();
    }

    /* access modifiers changed from: private */
    public NotificationPanelViewController getNotificationPanelViewController() {
        return getStatusBar().getPanelController();
    }
}
