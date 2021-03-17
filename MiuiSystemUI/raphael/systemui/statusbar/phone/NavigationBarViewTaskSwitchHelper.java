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
            r17 = this;
            r0 = r17
            java.lang.String r1 = "e1"
            r2 = r18
            kotlin.jvm.internal.Intrinsics.checkParameterIsNotNull(r2, r1)
            java.lang.String r1 = "e2"
            r3 = r19
            kotlin.jvm.internal.Intrinsics.checkParameterIsNotNull(r3, r1)
            float r1 = java.lang.Math.abs(r20)
            float r4 = r19.getX()
            float r5 = r18.getX()
            float r4 = r4 - r5
            float r4 = java.lang.Math.abs(r4)
            float r5 = r19.getY()
            float r6 = r18.getY()
            float r5 = r5 - r6
            float r5 = java.lang.Math.abs(r5)
            boolean r6 = r0.mIsVertical
            r7 = 0
            r8 = 1
            if (r6 != 0) goto L_0x0054
            r6 = 1073741824(0x40000000, float:2.0)
            float r5 = r5 * r6
            int r5 = (r4 > r5 ? 1 : (r4 == r5 ? 0 : -1))
            if (r5 <= 0) goto L_0x0054
            int r5 = r0.mScrollTouchSlop
            float r5 = (float) r5
            int r4 = (r4 > r5 ? 1 : (r4 == r5 ? 0 : -1))
            if (r4 <= 0) goto L_0x0054
            int r4 = r0.mMinFlingVelocity
            float r4 = (float) r4
            int r1 = (r1 > r4 ? 1 : (r1 == r4 ? 0 : -1))
            if (r1 <= 0) goto L_0x0054
            float r1 = r19.getY()
            float r4 = (float) r7
            int r1 = (r1 > r4 ? 1 : (r1 == r4 ? 0 : -1))
            if (r1 <= 0) goto L_0x0054
            r1 = r8
            goto L_0x0055
        L_0x0054:
            r1 = r7
        L_0x0055:
            if (r1 == 0) goto L_0x0202
            com.android.systemui.statusbar.phone.NavigationBarView r1 = r0.navBar
            r4 = 0
            if (r1 == 0) goto L_0x006d
            com.android.systemui.statusbar.phone.ButtonDispatcher r1 = r1.getHomeButton()
            if (r1 == 0) goto L_0x006d
            android.view.View r1 = r1.getCurrentView()
            if (r1 == 0) goto L_0x006d
            android.graphics.drawable.Drawable r1 = r1.getBackground()
            goto L_0x006e
        L_0x006d:
            r1 = r4
        L_0x006e:
            if (r1 == 0) goto L_0x01fa
            com.android.systemui.statusbar.phone.MiuiKeyButtonRipple r1 = (com.android.systemui.statusbar.phone.MiuiKeyButtonRipple) r1
            r5 = 2
            int[] r6 = new int[r5]
            com.android.systemui.statusbar.phone.NavigationBarView r9 = r0.navBar
            if (r9 == 0) goto L_0x0088
            com.android.systemui.statusbar.phone.ButtonDispatcher r9 = r9.getHomeButton()
            if (r9 == 0) goto L_0x0088
            android.view.View r9 = r9.getCurrentView()
            if (r9 == 0) goto L_0x0088
            r9.getLocationOnScreen(r6)
        L_0x0088:
            android.graphics.Rect r9 = new android.graphics.Rect
            r10 = r6[r7]
            r11 = r6[r8]
            r12 = r6[r7]
            com.android.systemui.statusbar.phone.NavigationBarView r13 = r0.navBar
            if (r13 == 0) goto L_0x00a9
            com.android.systemui.statusbar.phone.ButtonDispatcher r13 = r13.getHomeButton()
            if (r13 == 0) goto L_0x00a9
            android.view.View r13 = r13.getCurrentView()
            if (r13 == 0) goto L_0x00a9
            int r13 = r13.getWidth()
            java.lang.Integer r13 = java.lang.Integer.valueOf(r13)
            goto L_0x00aa
        L_0x00a9:
            r13 = r4
        L_0x00aa:
            if (r13 == 0) goto L_0x01f6
            int r13 = r13.intValue()
            int r12 = r12 + r13
            r13 = r6[r8]
            com.android.systemui.statusbar.phone.NavigationBarView r14 = r0.navBar
            if (r14 == 0) goto L_0x00cc
            com.android.systemui.statusbar.phone.ButtonDispatcher r14 = r14.getHomeButton()
            if (r14 == 0) goto L_0x00cc
            android.view.View r14 = r14.getCurrentView()
            if (r14 == 0) goto L_0x00cc
            int r14 = r14.getHeight()
            java.lang.Integer r14 = java.lang.Integer.valueOf(r14)
            goto L_0x00cd
        L_0x00cc:
            r14 = r4
        L_0x00cd:
            if (r14 == 0) goto L_0x01f2
            int r14 = r14.intValue()
            int r13 = r13 + r14
            r9.<init>(r10, r11, r12, r13)
            com.android.systemui.statusbar.phone.KeyOrderObserver$Companion r10 = com.android.systemui.statusbar.phone.KeyOrderObserver.Companion
            android.content.Context r11 = r0.context
            if (r11 == 0) goto L_0x01ee
            boolean r10 = r10.isReversed(r11)
            if (r10 == 0) goto L_0x0104
            com.android.systemui.statusbar.phone.NavigationBarView r10 = r0.navBar
            if (r10 == 0) goto L_0x00f2
            com.android.systemui.statusbar.phone.ButtonDispatcher r10 = r10.getBackButton()
            if (r10 == 0) goto L_0x00f2
            android.view.View r10 = r10.getCurrentView()
            goto L_0x00f3
        L_0x00f2:
            r10 = r4
        L_0x00f3:
            com.android.systemui.statusbar.phone.NavigationBarView r11 = r0.navBar
            if (r11 == 0) goto L_0x0102
            com.android.systemui.statusbar.phone.ButtonDispatcher r11 = r11.getRecentsButton()
            if (r11 == 0) goto L_0x0102
            android.view.View r11 = r11.getCurrentView()
            goto L_0x0122
        L_0x0102:
            r11 = r4
            goto L_0x0122
        L_0x0104:
            com.android.systemui.statusbar.phone.NavigationBarView r10 = r0.navBar
            if (r10 == 0) goto L_0x0113
            com.android.systemui.statusbar.phone.ButtonDispatcher r10 = r10.getRecentsButton()
            if (r10 == 0) goto L_0x0113
            android.view.View r10 = r10.getCurrentView()
            goto L_0x0114
        L_0x0113:
            r10 = r4
        L_0x0114:
            com.android.systemui.statusbar.phone.NavigationBarView r11 = r0.navBar
            if (r11 == 0) goto L_0x0102
            com.android.systemui.statusbar.phone.ButtonDispatcher r11 = r11.getBackButton()
            if (r11 == 0) goto L_0x0102
            android.view.View r11 = r11.getCurrentView()
        L_0x0122:
            if (r10 == 0) goto L_0x01ea
            r10.getLocationOnScreen(r6)
            android.graphics.Rect r12 = new android.graphics.Rect
            r13 = r6[r7]
            r14 = r6[r8]
            r15 = r6[r7]
            int r16 = r10.getWidth()
            int r15 = r15 + r16
            r16 = r6[r8]
            int r10 = r10.getHeight()
            int r10 = r16 + r10
            r12.<init>(r13, r14, r15, r10)
            if (r11 == 0) goto L_0x01e6
            r11.getLocationOnScreen(r6)
            android.graphics.Rect r10 = new android.graphics.Rect
            r13 = r6[r7]
            r14 = r6[r8]
            r15 = r6[r7]
            int r16 = r11.getWidth()
            int r15 = r15 + r16
            r6 = r6[r8]
            int r11 = r11.getHeight()
            int r6 = r6 + r11
            r10.<init>(r13, r14, r15, r6)
            float r6 = r18.getX()
            int r11 = r9.right
            float r11 = (float) r11
            int r6 = (r6 > r11 ? 1 : (r6 == r11 ? 0 : -1))
            if (r6 >= 0) goto L_0x017f
            float r6 = r19.getX()
            int r11 = r10.left
            float r11 = (float) r11
            int r6 = (r6 > r11 ? 1 : (r6 == r11 ? 0 : -1))
            if (r6 <= 0) goto L_0x017f
            r0.sendToHandyMode(r5)
            boolean r0 = r1 instanceof com.android.systemui.statusbar.phone.MiuiKeyButtonRipple
            if (r0 == 0) goto L_0x0202
            r1.gestureSlideEffect(r9, r10)
            goto L_0x0202
        L_0x017f:
            float r6 = r18.getX()
            int r10 = r9.left
            float r10 = (float) r10
            int r6 = (r6 > r10 ? 1 : (r6 == r10 ? 0 : -1))
            if (r6 <= 0) goto L_0x01a0
            float r6 = r19.getX()
            int r10 = r12.right
            float r10 = (float) r10
            int r6 = (r6 > r10 ? 1 : (r6 == r10 ? 0 : -1))
            if (r6 >= 0) goto L_0x01a0
            r0.sendToHandyMode(r8)
            boolean r0 = r1 instanceof com.android.systemui.statusbar.phone.MiuiKeyButtonRipple
            if (r0 == 0) goto L_0x0202
            r1.gestureSlideEffect(r9, r12)
            goto L_0x0202
        L_0x01a0:
            android.content.Context r1 = r0.context
            if (r1 == 0) goto L_0x01a8
            android.content.ContentResolver r4 = r1.getContentResolver()
        L_0x01a8:
            java.lang.String r1 = "handy_mode"
            int r1 = android.provider.Settings.Global.getInt(r4, r1, r7)
            if (r1 != r8) goto L_0x01ca
            float r4 = r18.getX()
            int r6 = r9.left
            float r6 = (float) r6
            int r4 = (r4 > r6 ? 1 : (r4 == r6 ? 0 : -1))
            if (r4 >= 0) goto L_0x01ca
            float r4 = r19.getX()
            int r6 = r9.left
            float r6 = (float) r6
            int r4 = (r4 > r6 ? 1 : (r4 == r6 ? 0 : -1))
            if (r4 <= 0) goto L_0x01ca
            r0.sendToHandyMode(r8)
            goto L_0x0202
        L_0x01ca:
            if (r1 != r5) goto L_0x0202
            float r1 = r18.getX()
            int r2 = r9.right
            float r2 = (float) r2
            int r1 = (r1 > r2 ? 1 : (r1 == r2 ? 0 : -1))
            if (r1 <= 0) goto L_0x0202
            float r1 = r19.getX()
            int r2 = r9.right
            float r2 = (float) r2
            int r1 = (r1 > r2 ? 1 : (r1 == r2 ? 0 : -1))
            if (r1 >= 0) goto L_0x0202
            r0.sendToHandyMode(r5)
            goto L_0x0202
        L_0x01e6:
            kotlin.jvm.internal.Intrinsics.throwNpe()
            throw r4
        L_0x01ea:
            kotlin.jvm.internal.Intrinsics.throwNpe()
            throw r4
        L_0x01ee:
            kotlin.jvm.internal.Intrinsics.throwNpe()
            throw r4
        L_0x01f2:
            kotlin.jvm.internal.Intrinsics.throwNpe()
            throw r4
        L_0x01f6:
            kotlin.jvm.internal.Intrinsics.throwNpe()
            throw r4
        L_0x01fa:
            kotlin.TypeCastException r0 = new kotlin.TypeCastException
            java.lang.String r1 = "null cannot be cast to non-null type com.android.systemui.statusbar.phone.MiuiKeyButtonRipple"
            r0.<init>(r1)
            throw r0
        L_0x0202:
            return r8
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
