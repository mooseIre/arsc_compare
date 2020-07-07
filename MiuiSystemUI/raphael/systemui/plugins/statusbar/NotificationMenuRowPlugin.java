package com.android.systemui.plugins.statusbar;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import com.android.systemui.plugins.Plugin;
import com.android.systemui.plugins.annotations.ProvidesInterface;
import java.util.ArrayList;

@ProvidesInterface(action = "com.android.systemui.action.PLUGIN_NOTIFICATION_MENU_ROW", version = 2)
public interface NotificationMenuRowPlugin extends Plugin {
    public static final String ACTION = "com.android.systemui.action.PLUGIN_NOTIFICATION_MENU_ROW";
    public static final int VERSION = 2;

    @ProvidesInterface(version = 1)
    public interface MenuItem {
        public static final int VERSION = 1;

        String getContentDescription();

        View getGutsView();

        View getMenuView();

        void setAppName(String str);

        void setIcon(Context context, int i);
    }

    @ProvidesInterface(version = 1)
    public interface OnMenuEventListener {
        public static final int VERSION = 1;

        void onMenuClicked(View view, int i, int i2, MenuItem menuItem);

        void onMenuReset(View view);

        void onMenuShown(View view);
    }

    void createMenu(ViewGroup viewGroup);

    MenuItem getLongpressMenuItem(Context context);

    ArrayList<MenuItem> getMenuItems(Context context);

    View getMenuView();

    boolean isMenuVisible();

    void onConfigurationChanged();

    void onExpansionChanged();

    void onHeightUpdate();

    void onNotificationUpdated();

    boolean onTouchEvent(View view, MotionEvent motionEvent, float f);

    void onTranslationUpdate(float f);

    void resetMenu();

    void setAppName(String str);

    void setMenuClickListener(OnMenuEventListener onMenuEventListener);

    void setMenuItems(ArrayList<MenuItem> arrayList);

    void setSwipeActionHelper(NotificationSwipeActionHelper notificationSwipeActionHelper);

    boolean useDefaultMenuItems();
}
