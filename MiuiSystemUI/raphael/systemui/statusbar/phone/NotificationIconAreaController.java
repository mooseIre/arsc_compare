package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.IconCompat;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.android.internal.statusbar.StatusBarIcon;
import com.android.systemui.Dependency;
import com.android.systemui.Util;
import com.android.systemui.miui.statusbar.notification.NotificationUtil;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.ExpandableNotificationRow;
import com.android.systemui.statusbar.Icons;
import com.android.systemui.statusbar.NotificationData;
import com.android.systemui.statusbar.NotificationShelf;
import com.android.systemui.statusbar.StatusBarIconView;
import com.android.systemui.statusbar.notification.CompatibilityColorUtil;
import com.android.systemui.statusbar.notification.NotificationUtils;
import com.android.systemui.statusbar.policy.DarkIconDispatcher;
import com.android.systemui.statusbar.policy.DarkIconDispatcherHelper;
import com.android.systemui.statusbar.stack.NotificationStackScrollLayout;
import java.util.ArrayList;
import java.util.function.Function;

public class NotificationIconAreaController implements DarkIconDispatcher.DarkReceiver {
    private static int sFilterColor;
    private int mClearableNotificationsCount;
    private final CompatibilityColorUtil mColorUtil;
    private Context mContext;
    private DarkIconDispatcher mDarkIconDispatcher;
    private float mDarkIntensity;
    private boolean mForceHideMoreIcon;
    private int mIconHPadding;
    private int mIconSize;
    private int mIconTint;
    private StatusBarIconView mMoreIcon;
    private boolean mNoIconsSetGone;
    protected View mNotificationIconArea;
    private NotificationIconContainer mNotificationIcons;
    private NotificationStackScrollLayout mNotificationScrollLayout;
    private NotificationIconContainer mShelfIcons;
    private boolean mShowNotificationIcons;
    private StatusBar mStatusBar;
    private final Rect mTintArea = new Rect();

    public void setupClockContainer(View view) {
    }

    public NotificationIconAreaController(Context context, StatusBar statusBar) {
        this.mContext = context;
        this.mStatusBar = statusBar;
        this.mColorUtil = new CompatibilityColorUtil(context);
        this.mNoIconsSetGone = context.getResources().getBoolean(R.bool.hide_notification_icons_if_empty);
        this.mDarkIconDispatcher = (DarkIconDispatcher) Dependency.get(DarkIconDispatcher.class);
        initializeNotificationAreaViews(context);
    }

    /* access modifiers changed from: protected */
    public View inflateIconArea(LayoutInflater layoutInflater) {
        return layoutInflater.inflate(R.layout.notification_icon_area, (ViewGroup) null);
    }

    /* access modifiers changed from: protected */
    public void initializeNotificationAreaViews(Context context) {
        reloadDimens(context);
        this.mNotificationIconArea = inflateIconArea(LayoutInflater.from(context));
        this.mNotificationIcons = (NotificationIconContainer) this.mNotificationIconArea.findViewById(R.id.notificationIcons);
        this.mNotificationScrollLayout = this.mStatusBar.getNotificationScrollLayout();
    }

    public void setupShelf(NotificationShelf notificationShelf) {
        this.mShelfIcons = notificationShelf.getShelfIcons();
        notificationShelf.setCollapsedIcons(this.mNotificationIcons);
    }

    public void setMoreIcon(StatusBarIconView statusBarIconView) {
        this.mMoreIcon = statusBarIconView;
    }

    public void setForceHideMoreIcon(boolean z) {
        this.mForceHideMoreIcon = z;
        setIconsVisibility();
    }

    public void onDensityOrFontScaleChanged(Context context) {
        reloadDimens(context);
        FrameLayout.LayoutParams generateIconLayoutParams = generateIconLayoutParams();
        for (int i = 0; i < this.mNotificationIcons.getChildCount(); i++) {
            this.mNotificationIcons.getChildAt(i).setLayoutParams(generateIconLayoutParams);
        }
        for (int i2 = 0; i2 < this.mShelfIcons.getChildCount(); i2++) {
            this.mShelfIcons.getChildAt(i2).setLayoutParams(generateIconLayoutParams);
        }
    }

    private FrameLayout.LayoutParams generateIconLayoutParams() {
        int i = this.mIconSize;
        return new FrameLayout.LayoutParams((this.mIconHPadding * 2) + i, i);
    }

    private void reloadDimens(Context context) {
        Resources resources = context.getResources();
        this.mIconSize = resources.getDimensionPixelSize(R.dimen.status_bar_icon_size);
        this.mIconHPadding = resources.getDimensionPixelSize(R.dimen.status_bar_notification_icon_padding);
    }

    public View getNotificationInnerAreaView() {
        return this.mNotificationIconArea;
    }

    public void onDarkChanged(Rect rect, float f, int i) {
        if (rect == null) {
            this.mTintArea.setEmpty();
        } else {
            this.mTintArea.set(rect);
        }
        this.mIconTint = i;
        this.mDarkIntensity = f;
        applyIconsTint(this.mNotificationIcons, this.mIconTint);
        refreshMoreIcon();
    }

    /* access modifiers changed from: private */
    public void refreshMoreIcon() {
        StatusBarIconView statusBarIconView = this.mMoreIcon;
        if (statusBarIconView != null) {
            statusBarIconView.setImageTintMode(PorterDuff.Mode.SRC_IN);
            if (Util.showCtsSpecifiedColor() || !this.mDarkIconDispatcher.useTint()) {
                boolean inDarkMode = DarkIconDispatcherHelper.inDarkMode(this.mTintArea, this.mMoreIcon, this.mDarkIntensity);
                this.mMoreIcon.setImageResource(Icons.get(Integer.valueOf(R.drawable.stat_notify_more), inDarkMode));
                if (!inDarkMode || !Util.showCtsSpecifiedColor()) {
                    this.mMoreIcon.setImageTintList((ColorStateList) null);
                    return;
                }
                if (sFilterColor == 0) {
                    sFilterColor = this.mContext.getResources().getColor(R.color.status_bar_icon_text_color_dark_mode_cts);
                }
                this.mMoreIcon.setImageTintList(ColorStateList.valueOf(sFilterColor));
                return;
            }
            this.mMoreIcon.setImageResource(R.drawable.stat_notify_more);
            StatusBarIconView statusBarIconView2 = this.mMoreIcon;
            statusBarIconView2.setImageTintList(ColorStateList.valueOf(DarkIconDispatcherHelper.getTint(this.mTintArea, statusBarIconView2, this.mIconTint)));
        }
    }

    /* access modifiers changed from: protected */
    public boolean shouldShowNotificationIcon(NotificationData.Entry entry, NotificationData notificationData, boolean z) {
        if ((!notificationData.isAmbient(entry.key) || z) && StatusBar.isTopLevelChild(entry) && entry.row.getVisibility() != 8 && !entry.row.isDismissed() && !entry.row.isRemoved() && !entry.notification.isPersistent()) {
            return true;
        }
        return false;
    }

    public void release() {
        if (this.mNotificationIcons.getChildCount() != 0) {
            this.mNotificationIcons.removeAllViews();
        }
        NotificationIconContainer notificationIconContainer = this.mShelfIcons;
        if (notificationIconContainer != null) {
            notificationIconContainer.removeAllViews();
        }
    }

    public void updateNotificationIcons(NotificationData notificationData) {
        int i = 0;
        updateIconsForLayout(notificationData, $$Lambda$NotificationIconAreaController$UqZBoYLzFV9iQ2ZKXh5_vFY0A6w.INSTANCE, this.mNotificationIcons, false);
        updateIconsForLayout(notificationData, $$Lambda$NotificationIconAreaController$F5tpJPiPsFQj85OyZRaWS2ufQTM.INSTANCE, this.mShelfIcons, true);
        applyIconsTint(this.mNotificationIcons, this.mIconTint);
        NotificationIconContainer notificationIconContainer = this.mNotificationIcons;
        if (!this.mShowNotificationIcons || (isNoIconsSetGone() && this.mNotificationIcons.getChildCount() <= 0)) {
            i = 8;
        }
        notificationIconContainer.setVisibility(i);
        this.mClearableNotificationsCount = notificationData.getClearableNotifications().size();
        setIconsVisibility();
    }

    public int getNotificationIconsVisibility() {
        return this.mNotificationIcons.getVisibility();
    }

    private void setIconsVisibility() {
        if (this.mMoreIcon != null) {
            int i = (this.mForceHideMoreIcon || this.mShowNotificationIcons || this.mClearableNotificationsCount <= 0) ? 8 : 0;
            this.mMoreIcon.setVisibility(i);
            if (i == 0) {
                this.mMoreIcon.post(new Runnable() {
                    public void run() {
                        NotificationIconAreaController.this.refreshMoreIcon();
                    }
                });
            }
        }
    }

    private ArrayList<StatusBarIconView> getDisplayStatusBarIcons(NotificationData notificationData, Function<NotificationData.Entry, StatusBarIconView> function, boolean z) {
        ArrayList<StatusBarIconView> arrayList = new ArrayList<>(this.mNotificationScrollLayout.getChildCount());
        ArraySet arraySet = new ArraySet(arrayList.size());
        for (int i = 0; i < this.mNotificationScrollLayout.getChildCount(); i++) {
            View childAt = this.mNotificationScrollLayout.getChildAt(i);
            if (childAt instanceof ExpandableNotificationRow) {
                NotificationData.Entry entry = ((ExpandableNotificationRow) childAt).getEntry();
                String targetPackageName = entry.notification.getTargetPackageName();
                if (shouldShowNotificationIcon(entry, notificationData, z) && !arraySet.contains(targetPackageName)) {
                    arrayList.add(function.apply(entry));
                    arraySet.add(targetPackageName);
                }
            }
        }
        return arrayList;
    }

    private void updateIconsForLayout(NotificationData notificationData, Function<NotificationData.Entry, StatusBarIconView> function, NotificationIconContainer notificationIconContainer, boolean z) {
        if (notificationIconContainer != null) {
            ArrayList<StatusBarIconView> displayStatusBarIcons = getDisplayStatusBarIcons(notificationData, function, z);
            ArrayMap arrayMap = new ArrayMap();
            ArrayList arrayList = new ArrayList();
            for (int i = 0; i < notificationIconContainer.getChildCount(); i++) {
                View childAt = notificationIconContainer.getChildAt(i);
                if ((childAt instanceof StatusBarIconView) && !displayStatusBarIcons.contains(childAt)) {
                    StatusBarIconView statusBarIconView = (StatusBarIconView) childAt;
                    String groupKey = statusBarIconView.getNotification().getGroupKey();
                    int i2 = 0;
                    boolean z2 = false;
                    while (true) {
                        if (i2 >= displayStatusBarIcons.size()) {
                            break;
                        }
                        StatusBarIconView statusBarIconView2 = displayStatusBarIcons.get(i2);
                        if (IconCompat.sameAs(statusBarIconView2.getSourceIcon(), statusBarIconView.getSourceIcon()) && statusBarIconView2.getNotification().getGroupKey().equals(groupKey)) {
                            if (z2) {
                                z2 = false;
                                break;
                            }
                            z2 = true;
                        }
                        i2++;
                    }
                    if (z2) {
                        ArrayList arrayList2 = (ArrayList) arrayMap.get(groupKey);
                        if (arrayList2 == null) {
                            arrayList2 = new ArrayList();
                            arrayMap.put(groupKey, arrayList2);
                        }
                        arrayList2.add(statusBarIconView.getStatusBarIcon());
                    }
                    arrayList.add(statusBarIconView);
                }
            }
            ArrayList arrayList3 = new ArrayList();
            for (String str : arrayMap.keySet()) {
                if (((ArrayList) arrayMap.get(str)).size() != 1) {
                    arrayList3.add(str);
                }
            }
            arrayMap.removeAll(arrayList3);
            notificationIconContainer.setReplacingIcons(arrayMap);
            int size = arrayList.size();
            for (int i3 = 0; i3 < size; i3++) {
                notificationIconContainer.removeView((View) arrayList.get(i3));
            }
            FrameLayout.LayoutParams generateIconLayoutParams = generateIconLayoutParams();
            for (int i4 = 0; i4 < displayStatusBarIcons.size(); i4++) {
                StatusBarIconView statusBarIconView3 = displayStatusBarIcons.get(i4);
                notificationIconContainer.removeTransientView(statusBarIconView3);
                if (statusBarIconView3.getParent() == null) {
                    notificationIconContainer.addView(statusBarIconView3, i4, generateIconLayoutParams);
                }
            }
            notificationIconContainer.setChangingViewPositions(true);
            int childCount = notificationIconContainer.getChildCount();
            for (int i5 = 0; i5 < childCount; i5++) {
                View childAt2 = notificationIconContainer.getChildAt(i5);
                StatusBarIconView statusBarIconView4 = displayStatusBarIcons.get(i5);
                if (childAt2 != statusBarIconView4) {
                    notificationIconContainer.removeView(statusBarIconView4);
                    notificationIconContainer.addView(statusBarIconView4, i5);
                }
            }
            notificationIconContainer.setChangingViewPositions(false);
            notificationIconContainer.setReplacingIcons((ArrayMap<String, ArrayList<StatusBarIcon>>) null);
        }
    }

    private void applyIconsTint(NotificationIconContainer notificationIconContainer, int i) {
        for (int i2 = 0; i2 < notificationIconContainer.getChildCount(); i2++) {
            applySmallIconTint((StatusBarIconView) notificationIconContainer.getChildAt(i2), i);
        }
    }

    private void applySmallIconTint(StatusBarIconView statusBarIconView, int i) {
        applySmallIconTint(statusBarIconView, i, !NotificationUtil.shouldSubstituteSmallIcon(statusBarIconView.getNotification()) && NotificationUtils.isGrayscale(statusBarIconView, this.mColorUtil));
    }

    private void applySmallIconTint(StatusBarIconView statusBarIconView, int i, boolean z) {
        statusBarIconView.setStaticDrawableColor(z ? DarkIconDispatcherHelper.getTint(this.mTintArea, statusBarIconView, i) : 0);
        statusBarIconView.setDecorColor(i);
    }

    /* access modifiers changed from: package-private */
    public void setShowNotificationIcon(boolean z) {
        this.mShowNotificationIcons = z;
    }

    private boolean isNoIconsSetGone() {
        return this.mNoIconsSetGone && ((StatusBarTypeController) Dependency.get(StatusBarTypeController.class)).hasCutout();
    }
}
