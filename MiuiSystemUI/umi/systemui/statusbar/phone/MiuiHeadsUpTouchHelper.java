package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.view.MotionEvent;
import com.android.systemui.statusbar.notification.MiniWindowExpandParameters;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.policy.AppMiniWindowRowTouchCallback;
import com.android.systemui.statusbar.notification.policy.AppMiniWindowRowTouchHelper;
import com.android.systemui.statusbar.notification.row.ExpandableView;
import com.android.systemui.statusbar.notification.row.MiuiExpandableNotificationRow;
import com.android.systemui.statusbar.notification.stack.NotificationListContainer;
import com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout;
import com.android.systemui.statusbar.phone.HeadsUpTouchHelper;
import com.miui.systemui.EventTracker;
import com.miui.systemui.events.MiniWindowEventSource;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: MiuiHeadsUpTouchHelper.kt */
public final class MiuiHeadsUpTouchHelper extends HeadsUpTouchHelper implements AppMiniWindowRowTouchCallback {
    private final HeadsUpTouchHelper.Callback callback;
    private final NotificationListContainer mContainer;
    private final HeadsUpManagerPhone mHeadsUpManager;
    private final NotificationPanelViewController mPanel;
    private final NotificationStackScrollLayout mStackScrollLayout;
    private final AppMiniWindowRowTouchHelper mTouchHelper;
    private boolean mTrackingMiniWindowHeadsUp;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public MiuiHeadsUpTouchHelper(@NotNull HeadsUpManagerPhone headsUpManagerPhone, @NotNull HeadsUpTouchHelper.Callback callback2, @NotNull NotificationPanelViewController notificationPanelViewController, @NotNull NotificationListContainer notificationListContainer, @NotNull NotificationEntryManager notificationEntryManager, @NotNull NotificationStackScrollLayout notificationStackScrollLayout, @NotNull EventTracker eventTracker) {
        super(headsUpManagerPhone, callback2, notificationPanelViewController);
        Intrinsics.checkParameterIsNotNull(headsUpManagerPhone, "headsUpManager");
        Intrinsics.checkParameterIsNotNull(callback2, "callback");
        Intrinsics.checkParameterIsNotNull(notificationPanelViewController, "notificationPanelView");
        Intrinsics.checkParameterIsNotNull(notificationListContainer, "container");
        Intrinsics.checkParameterIsNotNull(notificationEntryManager, "notificationEntryManager");
        Intrinsics.checkParameterIsNotNull(notificationStackScrollLayout, "notificationStackScrollLayout");
        Intrinsics.checkParameterIsNotNull(eventTracker, "eventTracker");
        this.callback = callback2;
        this.mHeadsUpManager = headsUpManagerPhone;
        this.mPanel = notificationPanelViewController;
        this.mContainer = notificationListContainer;
        this.mStackScrollLayout = notificationStackScrollLayout;
        this.mTouchHelper = new AppMiniWindowRowTouchHelper(this, notificationEntryManager, eventTracker, MiniWindowEventSource.HEADS_UP);
    }

    @Override // com.android.systemui.Gefingerpoken, com.android.systemui.statusbar.phone.HeadsUpTouchHelper
    public boolean onInterceptTouchEvent(@NotNull MotionEvent motionEvent) {
        Intrinsics.checkParameterIsNotNull(motionEvent, "event");
        if (!this.mTouchHelper.onInterceptTouchEvent(motionEvent)) {
            return super.onInterceptTouchEvent(motionEvent);
        }
        return true;
    }

    @Override // com.android.systemui.Gefingerpoken, com.android.systemui.statusbar.phone.HeadsUpTouchHelper
    public boolean onTouchEvent(@NotNull MotionEvent motionEvent) {
        Intrinsics.checkParameterIsNotNull(motionEvent, "event");
        if (!this.mTouchHelper.onTouchEvent(motionEvent)) {
            return super.onTouchEvent(motionEvent);
        }
        return true;
    }

    @Override // com.android.systemui.statusbar.notification.policy.AppMiniWindowRowTouchCallback
    public void onMiniWindowTrackingStart() {
        this.mTrackingMiniWindowHeadsUp = true;
        setTrackingHeadsUp(true);
        this.mHeadsUpManager.extendHeadsUp();
        this.mPanel.clearNotificationEffects();
    }

    @Override // com.android.systemui.statusbar.notification.policy.AppMiniWindowRowTouchCallback
    public void onMiniWindowTrackingEnd() {
        this.mTrackingMiniWindowHeadsUp = false;
    }

    @Override // com.android.systemui.statusbar.notification.policy.AppMiniWindowRowTouchCallback
    public void onMiniWindowReset() {
        this.mContainer.applyExpandAnimationParams(null);
        setTrackingHeadsUp(false);
    }

    @Override // com.android.systemui.statusbar.notification.policy.AppMiniWindowRowTouchCallback
    public void onExpandedParamsUpdated(@NotNull MiniWindowExpandParameters miniWindowExpandParameters) {
        Intrinsics.checkParameterIsNotNull(miniWindowExpandParameters, "expandParams");
        this.mContainer.applyExpandAnimationParams(miniWindowExpandParameters);
    }

    @Override // com.android.systemui.statusbar.notification.policy.AppMiniWindowRowTouchCallback
    public void onMiniWindowAppLaunched() {
        this.mStackScrollLayout.setHeadsUpGoingAwayAnimationsAllowed(false);
        this.mHeadsUpManager.releaseAllImmediately();
        this.mStackScrollLayout.setHeadsUpGoingAwayAnimationsAllowed(true);
    }

    @Override // com.android.systemui.statusbar.notification.policy.AppMiniWindowRowTouchCallback
    @Nullable
    public ExpandableView getChildAtRawPosition(float f, float f2) {
        return this.callback.getChildAtRawPosition(f, f2);
    }

    @Override // com.android.systemui.statusbar.notification.policy.AppMiniWindowRowTouchCallback
    public boolean canChildBePicked(@NotNull ExpandableView expandableView) {
        Intrinsics.checkParameterIsNotNull(expandableView, "child");
        return !this.callback.isExpanded() && (expandableView instanceof MiuiExpandableNotificationRow) && ((MiuiExpandableNotificationRow) expandableView).isHeadsUp() && expandableView.isPinned();
    }

    @Override // com.android.systemui.statusbar.notification.policy.AppMiniWindowRowTouchCallback
    @NotNull
    public Context getContext() {
        Context context = this.callback.getContext();
        Intrinsics.checkExpressionValueIsNotNull(context, "callback.context");
        return context;
    }

    public final boolean isTrackingMiniWindowHeadsUp$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core() {
        return this.mTrackingMiniWindowHeadsUp;
    }
}
