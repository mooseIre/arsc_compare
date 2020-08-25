package com.android.systemui.statusbar;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.android.systemui.Constants;
import com.android.systemui.Dependency;
import com.android.systemui.HapticFeedBackImpl;
import com.android.systemui.miui.statusbar.ExpandedNotification;
import com.android.systemui.miui.statusbar.analytics.NotificationStat;
import com.android.systemui.plugins.R;
import com.android.systemui.plugins.statusbar.NotificationMenuRowPlugin;
import com.android.systemui.plugins.statusbar.NotificationSwipeActionHelper;
import com.android.systemui.statistic.ScenarioConstants;
import com.android.systemui.statistic.ScenarioTrackUtil;
import com.android.systemui.statusbar.ExpandableNotificationRow;
import com.android.systemui.statusbar.NotificationGuts;
import com.android.systemui.statusbar.stack.NotificationStackScrollLayout;
import com.android.systemui.util.ViewAnimUtils;
import java.util.ArrayList;
import java.util.function.Consumer;

public class NotificationMenuRow implements NotificationMenuRowPlugin, View.OnClickListener, ExpandableNotificationRow.LayoutListener {
    private CheckForDrag mCheckForDrag;
    private Context mContext;
    private boolean mDismissing;
    private Handler mHandler;
    private int[] mIconLocation = new int[2];
    private NotificationMenuRowPlugin.MenuItem mInfoItem;
    private NotificationMenuRowContainer mMenuContainer;
    private ArrayList<NotificationMenuRowPlugin.MenuItem> mMenuItems;
    private NotificationMenuRowPlugin.OnMenuEventListener mMenuListener;
    private boolean mMenuSnappedTo;
    /* access modifiers changed from: private */
    public ExpandableNotificationRow mParent;
    private int[] mParentLocation = new int[2];
    private float mPrevX;
    private boolean mShouldShowMenu;
    private boolean mSnapping;
    private NotificationSwipeActionHelper mSwipeHelper;
    /* access modifiers changed from: private */
    public float mTranslation;

    public int getVersion() {
        return -1;
    }

    public void onCreate(Context context, Context context2) {
    }

    public void onDestroy() {
    }

    public void setMenuItems(ArrayList<NotificationMenuRowPlugin.MenuItem> arrayList) {
    }

    public boolean useDefaultMenuItems() {
        return false;
    }

    public NotificationMenuRow(Context context) {
        this.mContext = context;
        this.mShouldShowMenu = context.getResources().getBoolean(R.bool.config_showNotificationGear);
        this.mHandler = new Handler(Looper.getMainLooper());
        this.mMenuItems = new ArrayList<>();
    }

    public ArrayList<NotificationMenuRowPlugin.MenuItem> getMenuItems(Context context) {
        return this.mMenuItems;
    }

    public NotificationMenuRowPlugin.MenuItem getLongpressMenuItem(Context context) {
        return this.mInfoItem;
    }

    public void setSwipeActionHelper(NotificationSwipeActionHelper notificationSwipeActionHelper) {
        this.mSwipeHelper = notificationSwipeActionHelper;
    }

    public void setMenuClickListener(NotificationMenuRowPlugin.OnMenuEventListener onMenuEventListener) {
        this.mMenuListener = onMenuEventListener;
    }

    public void createMenu(ViewGroup viewGroup) {
        this.mParent = (ExpandableNotificationRow) viewGroup;
        createMenuViews(true);
    }

    public boolean isMenuVisible() {
        NotificationMenuRowContainer notificationMenuRowContainer = this.mMenuContainer;
        if (notificationMenuRowContainer != null && notificationMenuRowContainer.getMenuAlpha() > 0.0f) {
            return true;
        }
        return false;
    }

    public View getMenuView() {
        return this.mMenuContainer;
    }

    public void resetMenu() {
        resetState(true);
    }

    public void onNotificationUpdated() {
        if (this.mMenuContainer != null) {
            createMenuViews(!isMenuVisible());
        }
    }

    public void onConfigurationChanged() {
        this.mParent.setLayoutListener(this);
    }

    public void onLayout() {
        setMenuLocation();
        this.mParent.removeListener();
    }

    private void createMenuViews(boolean z) {
        this.mMenuItems.clear();
        NotificationMenuRowPlugin.MenuItem createInfoItem = createInfoItem(this.mContext);
        this.mInfoItem = createInfoItem;
        this.mMenuItems.add(createInfoItem);
        ExpandableNotificationRow expandableNotificationRow = this.mParent;
        ExpandedNotification statusBarNotification = expandableNotificationRow != null ? expandableNotificationRow.getStatusBarNotification() : null;
        if (Constants.IS_INTERNATIONAL) {
            if (statusBarNotification != null) {
                if (!((statusBarNotification.getNotification().flags & 64) != 0)) {
                    this.mMenuItems.add(createSnoozeItem(this.mContext));
                }
            }
        } else if (NotificationAggregate.canAggregate(this.mContext, statusBarNotification)) {
            this.mMenuItems.add(createAggregateItem(this.mContext));
        }
        NotificationMenuRowContainer notificationMenuRowContainer = this.mMenuContainer;
        if (notificationMenuRowContainer != null) {
            notificationMenuRowContainer.removeAllViews();
        } else {
            this.mMenuContainer = new NotificationMenuRowContainer(this.mContext);
        }
        for (int i = 0; i < this.mMenuItems.size(); i++) {
            View menuView = this.mMenuItems.get(i).getMenuView();
            this.mMenuContainer.addMenuView(menuView);
            menuView.setOnClickListener(this);
        }
        if (z) {
            resetState(false);
            return;
        }
        setMenuLocation();
        showMenu(this.mParent, -getSpaceForMenu(), 0.0f);
    }

    private void resetState(boolean z) {
        NotificationMenuRowContainer notificationMenuRowContainer = this.mMenuContainer;
        if (notificationMenuRowContainer != null) {
            notificationMenuRowContainer.resetState();
        }
        this.mSnapping = false;
        this.mDismissing = false;
        this.mMenuSnappedTo = false;
        setMenuLocation();
        NotificationMenuRowPlugin.OnMenuEventListener onMenuEventListener = this.mMenuListener;
        if (onMenuEventListener != null && z) {
            onMenuEventListener.onMenuReset(this.mParent);
        }
    }

    public boolean onTouchEvent(View view, MotionEvent motionEvent, float f) {
        CheckForDrag checkForDrag;
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked == 0) {
            this.mSnapping = false;
            this.mHandler.removeCallbacks(this.mCheckForDrag);
            this.mCheckForDrag = null;
            this.mPrevX = motionEvent.getRawX();
        } else if (actionMasked == 1) {
            return handleUpEvent(motionEvent, view, f);
        } else {
            if (actionMasked == 2) {
                this.mSnapping = false;
                motionEvent.getRawX();
                this.mPrevX = motionEvent.getRawX();
                if (this.mShouldShowMenu && this.mTranslation < 0.0f && !NotificationStackScrollLayout.isPinnedHeadsUp(view) && !this.mParent.isOnKeyguard() && !this.mParent.areGutsExposed() && !this.mParent.isDark() && ((checkForDrag = this.mCheckForDrag) == null || !this.mHandler.hasCallbacks(checkForDrag))) {
                    CheckForDrag checkForDrag2 = new CheckForDrag();
                    this.mCheckForDrag = checkForDrag2;
                    this.mHandler.postDelayed(checkForDrag2, 60);
                }
            }
        }
        return false;
    }

    private boolean handleUpEvent(MotionEvent motionEvent, View view, float f) {
        if (!this.mShouldShowMenu) {
            if (this.mTranslation <= 0.0f || !this.mSwipeHelper.isDismissGesture(motionEvent)) {
                snapBack(view, f);
            } else {
                dismiss(view, f);
            }
            return true;
        }
        boolean isTowardsMenu = isTowardsMenu(f);
        boolean z = false;
        boolean z2 = this.mSwipeHelper.getMinDismissVelocity() <= Math.abs(f);
        this.mSwipeHelper.swipedFarEnough(this.mTranslation, (float) this.mParent.getWidth());
        boolean z3 = !this.mParent.canViewBeDismissed() && ((double) (motionEvent.getEventTime() - motionEvent.getDownTime())) >= 200.0d;
        float f2 = -getSpaceForMenu();
        if (this.mTranslation > 0.0f || NotificationStackScrollLayout.isPinnedHeadsUp(view) || this.mParent.isOnKeyguard()) {
            if (this.mSwipeHelper.isDismissGesture(motionEvent)) {
                ((HapticFeedBackImpl) Dependency.get(HapticFeedBackImpl.class)).clearNotification();
                ScenarioTrackUtil.beginScenario(ScenarioConstants.SCENARIO_CLEAR_NOTI);
                dismiss(view, f);
            } else {
                snapBack(view, f);
            }
            return true;
        }
        if (this.mTranslation < (-(getSpaceForMenu() - (((float) getMenuIconSize()) * 0.2f)))) {
            z = true;
        }
        if (!this.mMenuSnappedTo || !isMenuVisible()) {
            if ((!this.mSwipeHelper.isFalseGesture(motionEvent) && swipedEnoughToShowMenu() && (!z2 || z3)) || isTowardsMenu) {
                showMenu(view, f2, f);
            } else if (!isTowardsMenu) {
                fadeInMenu((float) this.mParent.getWidth());
                showMenu(view, f2, f);
            } else {
                snapBack(view, f);
            }
        } else if (z) {
            showMenu(view, f2, f);
        } else {
            snapBack(view, f);
        }
        return true;
    }

    private void showMenu(View view, float f, float f2) {
        this.mMenuContainer.handleShowMenu();
        if (!this.mMenuSnappedTo && !this.mParent.areGutsExposed()) {
            ((HapticFeedBackImpl) Dependency.get(HapticFeedBackImpl.class)).meshNormal();
        }
        this.mMenuSnappedTo = true;
        this.mMenuListener.onMenuShown(view);
        this.mSwipeHelper.snap(view, f, f2);
        ((NotificationStat) Dependency.get(NotificationStat.class)).logNotificationSwipeLeft(this.mParent.getEntry().key);
    }

    private void snapBack(View view, float f) {
        if (this.mTranslation < 0.0f && this.mMenuSnappedTo) {
            ((HapticFeedBackImpl) Dependency.get(HapticFeedBackImpl.class)).meshNormal();
        }
        this.mHandler.removeCallbacks(this.mCheckForDrag);
        this.mMenuSnappedTo = false;
        this.mSnapping = true;
        this.mSwipeHelper.snap(view, 0.0f, f);
    }

    private void dismiss(View view, float f) {
        this.mHandler.removeCallbacks(this.mCheckForDrag);
        this.mMenuSnappedTo = false;
        this.mDismissing = true;
        this.mSwipeHelper.dismiss(view, f);
        ((NotificationStat) Dependency.get(NotificationStat.class)).logNotificationSwipeRight(this.mParent.getEntry().key);
    }

    private boolean swipedEnoughToShowMenu() {
        return !this.mSwipeHelper.swipedFarEnough(0.0f, 0.0f) && isMenuVisible() && this.mTranslation < (-(((float) getMenuIconSize()) * (this.mParent.canViewBeDismissed() ? 0.25f : 0.15f)));
    }

    private boolean isTowardsMenu(float f) {
        return isMenuVisible() && f >= 0.0f;
    }

    public void setAppName(String str) {
        this.mMenuItems.forEach(new Consumer(str) {
            public final /* synthetic */ String f$0;

            {
                this.f$0 = r1;
            }

            public final void accept(Object obj) {
                ((NotificationMenuRowPlugin.MenuItem) obj).setAppName(this.f$0);
            }
        });
    }

    public void onHeightUpdate() {
        if (this.mParent != null && this.mMenuItems.size() != 0 && this.mMenuContainer != null) {
            this.mMenuContainer.setTranslationY((float) ((this.mParent.getIntrinsicHeight() - getMenuIconSize()) / 2));
        }
    }

    public void onTranslationUpdate(float f) {
        this.mTranslation = f;
        this.mMenuContainer.onTranslationUpdate(f);
    }

    public void onExpansionChanged() {
        setMenuLocation();
    }

    public void onClick(View view) {
        if (this.mMenuListener != null) {
            view.getLocationOnScreen(this.mIconLocation);
            this.mParent.getLocationOnScreen(this.mParentLocation);
            int[] iArr = this.mIconLocation;
            int i = iArr[0];
            int[] iArr2 = this.mParentLocation;
            this.mMenuListener.onMenuClicked(this.mParent, (i - iArr2[0]) + (getMenuIconSize() / 2), (iArr[1] - iArr2[1]) + (view.getHeight() / 2), this.mMenuItems.get(this.mMenuContainer.indexOfChild(view)));
        }
    }

    private void setMenuLocation() {
        NotificationMenuRowContainer notificationMenuRowContainer;
        if (!this.mSnapping && (notificationMenuRowContainer = this.mMenuContainer) != null && notificationMenuRowContainer.isAttachedToWindow()) {
            this.mMenuContainer.resetMenuLocation();
        }
    }

    /* access modifiers changed from: private */
    public float getSpaceForMenu() {
        return (float) this.mMenuContainer.getActualWidth();
    }

    private int getMenuIconSize() {
        return this.mMenuContainer.getMenuIconSize();
    }

    private final class CheckForDrag implements Runnable {
        private CheckForDrag() {
        }

        public void run() {
            float abs = Math.abs(NotificationMenuRow.this.mTranslation);
            float access$200 = NotificationMenuRow.this.getSpaceForMenu();
            float width = ((float) NotificationMenuRow.this.mParent.getWidth()) * 0.4f;
            if (!NotificationMenuRow.this.isMenuVisible() && ((double) abs) >= ((double) access$200) * 0.4d && abs < width) {
                NotificationMenuRow.this.fadeInMenu(width);
            }
        }
    }

    /* access modifiers changed from: private */
    public void fadeInMenu(float f) {
        if (!this.mDismissing) {
            setMenuLocation();
            this.mMenuContainer.onTranslationUpdate(this.mTranslation);
        }
    }

    public static NotificationMenuRowPlugin.MenuItem createSnoozeItem(Context context) {
        return new NotificationMenuItem(context, context.getResources().getString(R.string.notification_menu_snooze_description), R.layout.notification_snooze, R.drawable.ic_snooze);
    }

    public static NotificationMenuRowPlugin.MenuItem createAggregateItem(Context context) {
        return new NotificationMenuItem(context, context.getResources().getString(R.string.notification_menu_aggregate_description), R.layout.notification_aggregate, R.drawable.ic_aggregate);
    }

    public static NotificationMenuRowPlugin.MenuItem createInfoItem(Context context) {
        return new NotificationMenuItem(context, context.getResources().getString(R.string.notification_menu_gear_description), R.layout.notification_info, R.drawable.ic_settings);
    }

    public static class NotificationMenuItem implements NotificationMenuRowPlugin.MenuItem {
        String mContentDescription;
        Context mContext;
        NotificationGuts.GutsContent mGutsContent;
        int mGutsResource;
        AlphaOptimizedImageView mMenuView;

        public NotificationMenuItem(Context context, String str, int i, int i2) {
            setIcon(context, i2);
            this.mContext = context;
            this.mContentDescription = str;
            this.mGutsResource = i;
        }

        public View getMenuView() {
            return this.mMenuView;
        }

        public View getGutsView() {
            if (this.mGutsContent == null) {
                this.mGutsContent = (NotificationGuts.GutsContent) LayoutInflater.from(this.mContext).inflate(this.mGutsResource, (ViewGroup) null, false);
            }
            return this.mGutsContent.getContentView();
        }

        public String getContentDescription() {
            return this.mContentDescription;
        }

        public void setIcon(Context context, int i) {
            if (this.mMenuView == null) {
                AlphaOptimizedImageView alphaOptimizedImageView = new AlphaOptimizedImageView(context);
                this.mMenuView = alphaOptimizedImageView;
                alphaOptimizedImageView.setScaleType(ImageView.ScaleType.CENTER);
                this.mMenuView.setAlpha(1.0f);
            }
            this.mMenuView.setImageResource(i);
            this.mMenuView.setBackgroundResource(R.drawable.notification_menu_bg);
            this.mMenuView.setTag(Integer.valueOf(i));
            ViewAnimUtils.mouse(this.mMenuView);
        }

        public void setAppName(String str) {
            if (!TextUtils.isEmpty(str)) {
                this.mMenuView.setContentDescription(String.format(this.mContext.getResources().getString(R.string.notification_menu_accessibility), new Object[]{str, this.mContentDescription}));
            }
        }
    }
}
