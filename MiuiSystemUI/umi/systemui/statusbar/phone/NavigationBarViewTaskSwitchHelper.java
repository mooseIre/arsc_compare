package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.Intent;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import com.android.systemui.C0012R$dimen;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: NavigationBarViewTaskSwitchHelper.kt */
public final class NavigationBarViewTaskSwitchHelper extends GestureDetector.SimpleOnGestureListener {
    private Context context;
    private boolean mIsVertical;
    private final int mMinFlingVelocity;
    private final int mScrollTouchSlop;
    private final GestureDetector mTaskSwitcherDetector;
    private int mTouchDownX;
    private int mTouchDownY;
    private NavigationBarView navBar;

    public NavigationBarViewTaskSwitchHelper(@NotNull Context context2, @NotNull NavigationBarView navigationBarView) {
        Intrinsics.checkParameterIsNotNull(context2, "context");
        Intrinsics.checkParameterIsNotNull(navigationBarView, "navBar");
        this.mTaskSwitcherDetector = new GestureDetector(context2, this);
        this.mScrollTouchSlop = context2.getResources().getDimensionPixelSize(C0012R$dimen.navigation_bar_size);
        ViewConfiguration viewConfiguration = ViewConfiguration.get(context2);
        Intrinsics.checkExpressionValueIsNotNull(viewConfiguration, "ViewConfiguration.get(context)");
        this.mMinFlingVelocity = viewConfiguration.getScaledMinimumFlingVelocity();
        this.context = context2;
        this.navBar = navigationBarView;
    }

    public final void setBarState(boolean z, boolean z2) {
        this.mIsVertical = z;
    }

    public final boolean onInterceptTouchEvent(@NotNull MotionEvent motionEvent) {
        Intrinsics.checkParameterIsNotNull(motionEvent, "event");
        this.mTaskSwitcherDetector.onTouchEvent(motionEvent);
        int action = motionEvent.getAction();
        if (action == 0) {
            this.mTouchDownX = (int) motionEvent.getX();
            this.mTouchDownY = (int) motionEvent.getY();
        } else if (action == 2) {
            int abs = Math.abs(((int) motionEvent.getX()) - this.mTouchDownX);
            int abs2 = Math.abs(((int) motionEvent.getY()) - this.mTouchDownY);
            if (!this.mIsVertical ? !(abs <= this.mScrollTouchSlop || abs <= abs2) : !(abs2 <= this.mScrollTouchSlop || abs2 <= abs)) {
                return true;
            }
        }
        return false;
    }

    public final boolean onTouchEvent(@NotNull MotionEvent motionEvent) {
        Intrinsics.checkParameterIsNotNull(motionEvent, "event");
        return this.mTaskSwitcherDetector.onTouchEvent(motionEvent);
    }

    /* JADX WARNING: Removed duplicated region for block: B:109:0x01ea  */
    /* JADX WARNING: Removed duplicated region for block: B:75:0x0124  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean onFling(@org.jetbrains.annotations.NotNull android.view.MotionEvent r18, @org.jetbrains.annotations.NotNull android.view.MotionEvent r19, float r20, float r21) {
        /*
        // Method dump skipped, instructions count: 515
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.phone.NavigationBarViewTaskSwitchHelper.onFling(android.view.MotionEvent, android.view.MotionEvent, float, float):boolean");
    }

    private final void sendToHandyMode(int i) {
        Intent intent = new Intent("miui.action.handymode.changemode");
        intent.putExtra("mode", i);
        Context context2 = this.context;
        if (context2 != null) {
            context2.sendBroadcast(intent);
        }
    }
}
