package com.android.systemui.plugins.statusbar;

import android.content.Context;
import android.graphics.Point;
import android.service.notification.StatusBarNotification;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import com.android.systemui.plugins.Plugin;
import com.android.systemui.plugins.annotations.Dependencies;
import com.android.systemui.plugins.annotations.DependsOn;
import com.android.systemui.plugins.annotations.ProvidesInterface;
import com.android.systemui.plugins.statusbar.NotificationSwipeActionHelper;
import java.util.ArrayList;

@Dependencies({@DependsOn(target = OnMenuEventListener.class), @DependsOn(target = MenuItem.class), @DependsOn(target = NotificationSwipeActionHelper.class), @DependsOn(target = NotificationSwipeActionHelper.SnoozeOption.class)})
@ProvidesInterface(action = NotificationMenuRowPlugin.ACTION, version = 5)
public interface NotificationMenuRowPlugin extends Plugin {
    public static final String ACTION = "com.android.systemui.action.PLUGIN_NOTIFICATION_MENU_ROW";
    public static final int VERSION = 5;

    @ProvidesInterface(version = 1)
    public interface MenuItem {
        public static final int VERSION = 1;

        String getContentDescription();

        View getGutsView();

        View getMenuView();
    }

    @ProvidesInterface(version = 1)
    public interface OnMenuEventListener {
        public static final int VERSION = 1;

        void onMenuClicked(View view, int i, int i2, MenuItem menuItem);

        void onMenuReset(View view);

        void onMenuShown(View view);
    }

    boolean canBeDismissed();

    void createMenu(ViewGroup viewGroup, StatusBarNotification statusBarNotification);

    MenuItem getAppOpsMenuItem(Context context);

    MenuItem getLongpressMenuItem(Context context);

    ArrayList<MenuItem> getMenuItems(Context context);

    int getMenuSnapTarget();

    View getMenuView();

    MenuItem getSnoozeMenuItem(Context context);

    boolean isMenuVisible();

    boolean isSnappedAndOnSameSide();

    boolean isSwipedEnoughToShowMenu();

    boolean isTowardsMenu(float f);

    boolean isWithinSnapMenuThreshold();

    default MenuItem menuItemToExposeOnSnap() {
        return null;
    }

    default void onConfigurationChanged() {
    }

    void onDismiss();

    default boolean onInterceptTouchEvent(View view, MotionEvent motionEvent) {
        return false;
    }

    void onNotificationUpdated(StatusBarNotification statusBarNotification);

    void onParentHeightUpdate();

    void onParentTranslationUpdate(float f);

    void onSnapClosed();

    void onSnapOpen();

    void onTouchEnd();

    void onTouchMove(float f);

    void onTouchStart();

    void resetMenu();

    void setAppName(String str);

    default void setDismissRtl(boolean z) {
    }

    void setMenuClickListener(OnMenuEventListener onMenuEventListener);

    void setMenuItems(ArrayList<MenuItem> arrayList);

    default boolean shouldShowGutsOnSnapOpen() {
        return false;
    }

    boolean shouldShowMenu();

    boolean shouldSnapBack();

    default boolean shouldUseDefaultMenuItems() {
        return false;
    }

    default Point getRevealAnimationOrigin() {
        return new Point(0, 0);
    }
}
