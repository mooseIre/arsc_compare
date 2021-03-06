package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.util.ArraySet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.collection.ArrayMap;
import com.android.internal.statusbar.StatusBarIcon;
import com.android.internal.util.ContrastColorUtil;
import com.android.settingslib.Utils;
import com.android.systemui.C0009R$attr;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0017R$layout;
import com.android.systemui.Dependency;
import com.android.systemui.bubbles.BubbleController;
import com.android.systemui.plugins.DarkIconDispatcher;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.NotificationListener;
import com.android.systemui.statusbar.NotificationMediaManager;
import com.android.systemui.statusbar.NotificationShelf;
import com.android.systemui.statusbar.StatusBarIconView;
import com.android.systemui.statusbar.notification.NotificationUtils;
import com.android.systemui.statusbar.notification.NotificationWakeUpCoordinator;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import com.android.systemui.statusbar.phone.NotificationIconObserver;
import com.miui.systemui.NotificationSettings;
import com.miui.systemui.SettingsManager;
import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Function;
import miui.os.Build;

public class NotificationIconAreaController implements DarkIconDispatcher.DarkReceiver, StatusBarStateController.StateListener, NotificationWakeUpCoordinator.WakeUpListener, NotificationIconObserver.Callback, NotificationSettings.StyleListener {
    private boolean mAnimationsEnabled;
    private final BubbleController mBubbleController;
    protected View mCenteredIconArea;
    private StatusBarIconView mCenteredIconView;
    private Context mContext;
    private final ContrastColorUtil mContrastColorUtil;
    private int mIconHPadding;
    private int mIconSize;
    private int mIconTint = -1;
    private final NotificationMediaManager mMediaManager;
    protected View mNotificationIconArea;
    private NotificationIconObserver mNotificationIconObserver;
    private NotificationIconContainer mNotificationIcons;
    private ViewGroup mNotificationScrollLayout;
    final NotificationListener.NotificationSettingsListener mSettingsListener = new NotificationListener.NotificationSettingsListener() {
        /* class com.android.systemui.statusbar.phone.NotificationIconAreaController.AnonymousClass1 */

        @Override // com.android.systemui.statusbar.NotificationListener.NotificationSettingsListener
        public void onStatusBarIconsBehaviorChanged(boolean z) {
            NotificationIconAreaController.this.mShowLowPriority = !z;
            if (NotificationIconAreaController.this.mNotificationScrollLayout != null) {
                NotificationIconAreaController.this.updateStatusBarIcons();
            }
        }
    };
    private boolean mShowLowPriority = true;
    private StatusBar mStatusBar;
    private final StatusBarStateController mStatusBarStateController;
    private final Rect mTintArea = new Rect();
    private final Runnable mUpdateStatusBarIcons = new Runnable() {
        /* class com.android.systemui.statusbar.phone.$$Lambda$NWCrb8vzuopzf5kAygkNeXndtBo */

        public final void run() {
            NotificationIconAreaController.this.updateStatusBarIcons();
        }
    };
    private final NotificationWakeUpCoordinator mWakeUpCoordinator;
    private ArraySet<String> toShowPkg = new ArraySet<>(100);

    private void updateAodIconColors() {
    }

    private void updateAodIconsVisibility(boolean z) {
    }

    private void updateCenterIcon() {
    }

    private void updateShelfIcons() {
    }

    public void initAodIcons() {
    }

    @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
    public void onDozingChanged(boolean z) {
    }

    @Override // com.android.systemui.statusbar.notification.NotificationWakeUpCoordinator.WakeUpListener
    public void onFullyHiddenChanged(boolean z) {
    }

    public void updateAodNotificationIcons() {
    }

    public NotificationIconAreaController(Context context, StatusBar statusBar, StatusBarStateController statusBarStateController, NotificationWakeUpCoordinator notificationWakeUpCoordinator, KeyguardBypassController keyguardBypassController, NotificationMediaManager notificationMediaManager, NotificationListener notificationListener, DozeParameters dozeParameters, BubbleController bubbleController) {
        this.mStatusBar = statusBar;
        this.mContrastColorUtil = ContrastColorUtil.getInstance(context);
        this.mContext = context;
        this.mStatusBarStateController = statusBarStateController;
        statusBarStateController.addCallback(this);
        this.mMediaManager = notificationMediaManager;
        this.mWakeUpCoordinator = notificationWakeUpCoordinator;
        notificationWakeUpCoordinator.addListener(this);
        this.mBubbleController = bubbleController;
        notificationListener.addNotificationSettingsListener(this.mSettingsListener);
        initializeNotificationAreaViews(context);
        reloadAodColor();
    }

    /* access modifiers changed from: protected */
    public View inflateIconArea(LayoutInflater layoutInflater) {
        return layoutInflater.inflate(C0017R$layout.notification_icon_area, (ViewGroup) null);
    }

    /* access modifiers changed from: protected */
    public void initializeNotificationAreaViews(Context context) {
        reloadDimens(context);
        View inflateIconArea = inflateIconArea(LayoutInflater.from(context));
        this.mNotificationIconArea = inflateIconArea;
        this.mNotificationIcons = (NotificationIconContainer) inflateIconArea.findViewById(C0015R$id.notificationIcons);
        NotificationIconObserver notificationIconObserver = (NotificationIconObserver) Dependency.get(NotificationIconObserver.class);
        this.mNotificationIconObserver = notificationIconObserver;
        notificationIconObserver.addCallback(this);
        ((SettingsManager) Dependency.get(SettingsManager.class)).registerNotifStyleListener(this);
        this.mNotificationScrollLayout = this.mStatusBar.getNotificationScrollLayout();
        initAodIcons();
    }

    public void setupShelf(NotificationShelf notificationShelf) {
        notificationShelf.setCollapsedIcons(this.mNotificationIcons);
    }

    public void onDensityOrFontScaleChanged(Context context) {
        reloadDimens(context);
        FrameLayout.LayoutParams generateIconLayoutParams = generateIconLayoutParams();
        for (int i = 0; i < this.mNotificationIcons.getChildCount(); i++) {
            this.mNotificationIcons.getChildAt(i).setLayoutParams(generateIconLayoutParams);
        }
    }

    private FrameLayout.LayoutParams generateIconLayoutParams() {
        int i = this.mIconSize;
        return new FrameLayout.LayoutParams((this.mIconHPadding * 2) + i, i);
    }

    private void reloadDimens(Context context) {
        Resources resources = context.getResources();
        this.mIconSize = resources.getDimensionPixelSize(C0012R$dimen.status_bar_icon_size);
        this.mIconHPadding = resources.getDimensionPixelSize(C0012R$dimen.status_bar_notification_icon_padding);
        resources.getDimensionPixelSize(C0012R$dimen.shelf_appear_translation);
    }

    public View getNotificationInnerAreaView() {
        return this.mNotificationIconArea;
    }

    public View getCenteredNotificationAreaView() {
        return this.mCenteredIconArea;
    }

    @Override // com.android.systemui.plugins.DarkIconDispatcher.DarkReceiver
    public void onDarkChanged(Rect rect, float f, int i, int i2, int i3, boolean z) {
        if (rect == null) {
            this.mTintArea.setEmpty();
        } else {
            this.mTintArea.set(rect);
        }
        View view = this.mNotificationIconArea;
        if (view == null) {
            this.mIconTint = i;
        } else if (DarkIconDispatcher.isInArea(rect, view)) {
            if (z) {
                this.mIconTint = i;
            } else {
                if (f != 0.0f) {
                    i2 = i3;
                }
                this.mIconTint = i2;
            }
        }
        View view2 = this.mCenteredIconArea;
        if (view2 != null && DarkIconDispatcher.isInArea(rect, view2) && !z) {
            int i4 = (f > 0.0f ? 1 : (f == 0.0f ? 0 : -1));
        }
        applyNotificationIconsTint();
    }

    /* access modifiers changed from: protected */
    public boolean shouldShowNotificationIcon(NotificationEntry notificationEntry, boolean z, boolean z2, boolean z3, boolean z4, boolean z5, boolean z6, boolean z7, boolean z8) {
        boolean z9 = (this.mCenteredIconView == null || notificationEntry.getIcons().getCenteredIcon() == null || !Objects.equals(notificationEntry.getIcons().getCenteredIcon(), this.mCenteredIconView)) ? false : true;
        if (z8) {
            return z9;
        }
        if (z6 && z9 && !notificationEntry.isRowHeadsUp()) {
            return false;
        }
        if (notificationEntry.getRanking().isAmbient() && !z) {
            return false;
        }
        if (z5 && notificationEntry.getKey().equals(this.mMediaManager.getMediaNotificationKey())) {
            return false;
        }
        if ((!z2 && notificationEntry.getImportance() < 3) || !notificationEntry.isTopLevelChild() || notificationEntry.getRow().getVisibility() == 8) {
            return false;
        }
        if (notificationEntry.isRowDismissed() && z3) {
            return false;
        }
        if (z4 && notificationEntry.isLastMessageFromReply()) {
            return false;
        }
        if (z || !notificationEntry.shouldSuppressStatusBar()) {
            return (!z7 || !notificationEntry.showingPulsing() || (this.mWakeUpCoordinator.getNotificationsFullyHidden() && notificationEntry.isPulseSuppressed())) && !this.mBubbleController.isBubbleExpanded(notificationEntry) && !notificationEntry.getSbn().isPersistent();
        }
        return false;
    }

    public void updateNotificationIcons() {
        updateStatusBarIcons();
        updateShelfIcons();
        updateCenterIcon();
        updateAodNotificationIcons();
        applyNotificationIconsTint();
    }

    public void updateStatusBarIcons() {
        updateIconsForLayout($$Lambda$NotificationIconAreaController$00FvnUlAy0_w9h_AZ6H93V6ggw.INSTANCE, this.mNotificationIcons, false, this.mShowLowPriority, true, true, false, true, false, false);
    }

    /* access modifiers changed from: package-private */
    public boolean shouldShouldLowPriorityIcons() {
        return this.mShowLowPriority;
    }

    private void updateIconsForLayout(Function<NotificationEntry, StatusBarIconView> function, NotificationIconContainer notificationIconContainer, boolean z, boolean z2, boolean z3, boolean z4, boolean z5, boolean z6, boolean z7, boolean z8) {
        int i;
        ArrayList arrayList = new ArrayList(this.mNotificationScrollLayout.getChildCount());
        boolean z9 = notificationIconContainer == this.mNotificationIcons;
        this.toShowPkg.clear();
        int i2 = 0;
        while (i2 < this.mNotificationScrollLayout.getChildCount()) {
            View childAt = this.mNotificationScrollLayout.getChildAt(i2);
            if (childAt instanceof ExpandableNotificationRow) {
                NotificationEntry entry = ((ExpandableNotificationRow) childAt).getEntry();
                i = i2;
                if (shouldShowNotificationIcon(entry, z, z2, z3, z4, z5, z6, z7, z8)) {
                    StatusBarIconView apply = function.apply(entry);
                    if (apply != null) {
                        if (Build.IS_INTERNATIONAL_BUILD) {
                            arrayList.add(apply);
                        } else if (!this.toShowPkg.contains(entry.getSbn().getTargetPackageName())) {
                            arrayList.add(apply);
                            if (z9) {
                                this.toShowPkg.add(entry.getSbn().getTargetPackageName());
                            }
                        }
                    }
                }
            } else {
                i = i2;
            }
            i2 = i + 1;
        }
        this.toShowPkg.clear();
        ArrayMap<String, ArrayList<StatusBarIcon>> arrayMap = new ArrayMap<>();
        ArrayList arrayList2 = new ArrayList();
        for (int i3 = 0; i3 < notificationIconContainer.getChildCount(); i3++) {
            View childAt2 = notificationIconContainer.getChildAt(i3);
            if ((childAt2 instanceof StatusBarIconView) && !arrayList.contains(childAt2)) {
                StatusBarIconView statusBarIconView = (StatusBarIconView) childAt2;
                String groupKey = statusBarIconView.getNotification().getGroupKey();
                int i4 = 0;
                boolean z10 = false;
                while (true) {
                    if (i4 >= arrayList.size()) {
                        break;
                    }
                    StatusBarIconView statusBarIconView2 = (StatusBarIconView) arrayList.get(i4);
                    if (statusBarIconView2.getSourceIcon().sameAs(statusBarIconView.getSourceIcon()) && statusBarIconView2.getNotification().getGroupKey().equals(groupKey)) {
                        if (z10) {
                            z10 = false;
                            break;
                        }
                        z10 = true;
                    }
                    i4++;
                }
                if (z10) {
                    ArrayList<StatusBarIcon> arrayList3 = arrayMap.get(groupKey);
                    if (arrayList3 == null) {
                        arrayList3 = new ArrayList<>();
                        arrayMap.put(groupKey, arrayList3);
                    }
                    arrayList3.add(statusBarIconView.getStatusBarIcon());
                }
                arrayList2.add(statusBarIconView);
            }
        }
        ArrayList arrayList4 = new ArrayList();
        for (String str : arrayMap.keySet()) {
            if (arrayMap.get(str).size() != 1) {
                arrayList4.add(str);
            }
        }
        arrayMap.removeAll(arrayList4);
        notificationIconContainer.setReplacingIcons(arrayMap);
        int size = arrayList2.size();
        for (int i5 = 0; i5 < size; i5++) {
            notificationIconContainer.removeView((View) arrayList2.get(i5));
        }
        FrameLayout.LayoutParams generateIconLayoutParams = generateIconLayoutParams();
        for (int i6 = 0; i6 < arrayList.size(); i6++) {
            StatusBarIconView statusBarIconView3 = (StatusBarIconView) arrayList.get(i6);
            notificationIconContainer.removeTransientView(statusBarIconView3);
            if (statusBarIconView3.getParent() == null) {
                if (z3) {
                    statusBarIconView3.setOnDismissListener(this.mUpdateStatusBarIcons);
                }
                notificationIconContainer.addView(statusBarIconView3, i6, generateIconLayoutParams);
            }
        }
        notificationIconContainer.setChangingViewPositions(true);
        int childCount = notificationIconContainer.getChildCount();
        for (int i7 = 0; i7 < childCount; i7++) {
            View childAt3 = notificationIconContainer.getChildAt(i7);
            StatusBarIconView statusBarIconView4 = (StatusBarIconView) arrayList.get(i7);
            if (childAt3 != statusBarIconView4) {
                notificationIconContainer.removeView(statusBarIconView4);
                notificationIconContainer.addView(statusBarIconView4, i7);
            }
        }
        notificationIconContainer.setChangingViewPositions(false);
        notificationIconContainer.setReplacingIcons(null);
    }

    private void applyNotificationIconsTint() {
        for (int i = 0; i < this.mNotificationIcons.getChildCount(); i++) {
            StatusBarIconView statusBarIconView = (StatusBarIconView) this.mNotificationIcons.getChildAt(i);
            if (statusBarIconView.getWidth() != 0) {
                updateTintForIcon(statusBarIconView, this.mIconTint);
            } else {
                statusBarIconView.executeOnLayout(new Runnable(statusBarIconView) {
                    /* class com.android.systemui.statusbar.phone.$$Lambda$NotificationIconAreaController$DK58FxtnSO50_ni0c1Dv2KyiKiw */
                    public final /* synthetic */ StatusBarIconView f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void run() {
                        NotificationIconAreaController.this.lambda$applyNotificationIconsTint$1$NotificationIconAreaController(this.f$1);
                    }
                });
            }
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$applyNotificationIconsTint$1 */
    public /* synthetic */ void lambda$applyNotificationIconsTint$1$NotificationIconAreaController(StatusBarIconView statusBarIconView) {
        updateTintForIcon(statusBarIconView, this.mIconTint);
    }

    private void updateTintForIcon(StatusBarIconView statusBarIconView, int i) {
        int i2 = 0;
        if (!Boolean.TRUE.equals(statusBarIconView.getTag(C0015R$id.icon_is_pre_L)) || NotificationUtils.isGrayscale(statusBarIconView, this.mContrastColorUtil)) {
            i2 = DarkIconDispatcher.getTint(this.mTintArea, statusBarIconView, i);
        }
        statusBarIconView.setStaticDrawableColor(i2);
        statusBarIconView.setDecorColor(i);
    }

    public void showIconIsolated(StatusBarIconView statusBarIconView, boolean z) {
        this.mNotificationIcons.showIconIsolated(statusBarIconView, z);
    }

    public void setIsolatedIconLocation(Rect rect, boolean z) {
        this.mNotificationIcons.setIsolatedIconLocation(rect, z);
    }

    public void setAnimationsEnabled(boolean z) {
        this.mAnimationsEnabled = z;
        updateAnimations();
    }

    @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
    public void onStateChanged(int i) {
        updateAodIconsVisibility(false);
        updateAnimations();
    }

    private void updateAnimations() {
        boolean z = true;
        boolean z2 = this.mStatusBarStateController.getState() == 0;
        NotificationIconContainer notificationIconContainer = this.mNotificationIcons;
        if (!this.mAnimationsEnabled || !z2) {
            z = false;
        }
        notificationIconContainer.setAnimationsEnabled(z);
    }

    public void onThemeChanged() {
        reloadAodColor();
        updateAodIconColors();
    }

    private void reloadAodColor() {
        Utils.getColorAttrDefaultColor(this.mContext, C0009R$attr.wallpaperTextColor);
    }

    @Override // com.android.systemui.statusbar.notification.NotificationWakeUpCoordinator.WakeUpListener
    public void onPulseExpansionChanged(boolean z) {
        if (z) {
            updateAodIconsVisibility(true);
        }
    }

    @Override // com.android.systemui.statusbar.phone.NotificationIconObserver.Callback
    public void onNotificationIconChanged(boolean z) {
        updateMaxIconsValue(z);
        this.mNotificationIcons.updateState();
    }

    private void updateMaxIconsValue(boolean z) {
        if (!z) {
            this.mNotificationIcons.setMaxStaticIcons(0);
            this.mNotificationIcons.setMaxVisibleIconsOnLock(0);
            this.mNotificationIcons.setMaxDots(0);
            return;
        }
        this.mNotificationIcons.setMaxVisibleIconsOnLock(3);
        this.mNotificationIcons.setMaxDots(3);
        this.mNotificationIcons.setMaxStaticIcons(3);
    }

    @Override // com.miui.systemui.NotificationSettings.StyleListener
    public void onChanged(int i) {
        updateNotificationIcons();
    }
}
