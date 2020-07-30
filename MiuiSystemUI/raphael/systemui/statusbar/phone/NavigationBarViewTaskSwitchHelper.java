package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.provider.Settings;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.policy.KeyButtonRipple;

public class NavigationBarViewTaskSwitchHelper extends GestureDetector.SimpleOnGestureListener {
    private StatusBar mBar;
    private Context mContext;
    private boolean mIsRTL;
    private boolean mIsVertical;
    private final int mMinFlingVelocity;
    private final int mScrollTouchSlop;
    private final GestureDetector mTaskSwitcherDetector;
    private int mTouchDownX;
    private int mTouchDownY;

    public NavigationBarViewTaskSwitchHelper(Context context) {
        this.mContext = context;
        ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
        this.mScrollTouchSlop = context.getResources().getDimensionPixelSize(R.dimen.navigation_bar_size);
        this.mMinFlingVelocity = viewConfiguration.getScaledMinimumFlingVelocity();
        this.mTaskSwitcherDetector = new GestureDetector(context, this);
    }

    public void setBar(StatusBar statusBar) {
        this.mBar = statusBar;
    }

    public void setBarState(boolean z, boolean z2) {
        this.mIsVertical = z;
        this.mIsRTL = z2;
    }

    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        this.mTaskSwitcherDetector.onTouchEvent(motionEvent);
        int action = motionEvent.getAction() & 255;
        if (action == 0) {
            this.mTouchDownX = (int) motionEvent.getX();
            this.mTouchDownY = (int) motionEvent.getY();
        } else if (action == 2) {
            int abs = Math.abs(((int) motionEvent.getX()) - this.mTouchDownX);
            int abs2 = Math.abs(((int) motionEvent.getY()) - this.mTouchDownY);
            if (this.mIsVertical ? !(abs2 <= this.mScrollTouchSlop || abs2 <= abs) : !(abs <= this.mScrollTouchSlop || abs <= abs2)) {
                return true;
            }
        }
        return false;
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        return this.mTaskSwitcherDetector.onTouchEvent(motionEvent);
    }

    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent2, float f, float f2) {
        View view;
        View view2;
        float abs = Math.abs(f);
        Math.abs(f2);
        int abs2 = Math.abs((int) (motionEvent2.getX() - motionEvent.getX()));
        if (!this.mIsVertical && ((float) abs2) > ((float) Math.abs((int) (motionEvent2.getY() - motionEvent.getY()))) * 2.0f && abs2 > this.mScrollTouchSlop && abs > ((float) this.mMinFlingVelocity) && motionEvent2.getY() > 0.0f) {
            NavigationBarView navigationBarView = this.mBar.getNavigationBarView();
            KeyButtonRipple keyButtonRipple = (KeyButtonRipple) navigationBarView.getHomeButton().getBackground();
            int[] locationOnScreen = navigationBarView.getHomeButton().getLocationOnScreen();
            Rect rect = new Rect(locationOnScreen[0], locationOnScreen[1], locationOnScreen[0] + navigationBarView.getHomeButton().getWidth(), locationOnScreen[1] + navigationBarView.getHomeButton().getHeight());
            if (NavigationBarView.getScreenKeyOrder(this.mContext).get(0).intValue() == 3) {
                view2 = navigationBarView.getBackButton();
                view = navigationBarView.getRecentsButton();
            } else {
                view2 = navigationBarView.getRecentsButton();
                view = navigationBarView.getBackButton();
            }
            int[] locationOnScreen2 = view2.getLocationOnScreen();
            Rect rect2 = new Rect(locationOnScreen2[0], locationOnScreen2[1], locationOnScreen2[0] + view2.getWidth(), locationOnScreen2[1] + view2.getHeight());
            int[] locationOnScreen3 = view.getLocationOnScreen();
            Rect rect3 = new Rect(locationOnScreen3[0], locationOnScreen3[1], locationOnScreen3[0] + view.getWidth(), locationOnScreen3[1] + view.getHeight());
            if (motionEvent.getX() < ((float) rect.right) && motionEvent2.getX() > ((float) rect3.left)) {
                sendToHandyMode(2);
                keyButtonRipple.gestureSlideEffect(rect, rect3);
            } else if (motionEvent.getX() <= ((float) rect.left) || motionEvent2.getX() >= ((float) rect2.right)) {
                int i = Settings.Global.getInt(this.mContext.getContentResolver(), "handy_mode", 0);
                if (i == 1 && motionEvent.getX() < ((float) rect.left) && motionEvent2.getX() > ((float) rect.left)) {
                    sendToHandyMode(1);
                } else if (i == 2 && motionEvent.getX() > ((float) rect.right) && motionEvent2.getX() < ((float) rect.right)) {
                    sendToHandyMode(2);
                }
            } else {
                sendToHandyMode(1);
                keyButtonRipple.gestureSlideEffect(rect, rect2);
            }
        }
        return true;
    }

    private void sendToHandyMode(int i) {
        Intent intent = new Intent("miui.action.handymode.changemode");
        intent.putExtra("mode", i);
        this.mContext.sendBroadcast(intent);
    }
}
