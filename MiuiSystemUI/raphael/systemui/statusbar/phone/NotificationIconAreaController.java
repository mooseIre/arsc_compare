package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
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
import com.android.systemui.Interpolators;
import com.android.systemui.bubbles.BubbleController;
import com.android.systemui.plugins.DarkIconDispatcher;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.CrossFadeHelper;
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

public class NotificationIconAreaController implements DarkIconDispatcher.DarkReceiver, StatusBarStateController.StateListener, NotificationWakeUpCoordinator.WakeUpListener, NotificationIconObserver.Callback, NotificationSettings.StyleListener {
    private boolean mAnimationsEnabled;
    private int mAodIconAppearTranslation;
    private int mAodIconTint;
    private NotificationIconContainer mAodIcons;
    private boolean mAodIconsVisible;
    private final BubbleController mBubbleController;
    private final KeyguardBypassController mBypassController;
    private NotificationIconContainer mCenteredIcon;
    protected View mCenteredIconArea;
    private int mCenteredIconTint = -1;
    private StatusBarIconView mCenteredIconView;
    private Context mContext;
    private final ContrastColorUtil mContrastColorUtil;
    private final DozeParameters mDozeParameters;
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
    private NotificationIconContainer mShelfIcons;
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

    public NotificationIconAreaController(Context context, StatusBar statusBar, StatusBarStateController statusBarStateController, NotificationWakeUpCoordinator notificationWakeUpCoordinator, KeyguardBypassController keyguardBypassController, NotificationMediaManager notificationMediaManager, NotificationListener notificationListener, DozeParameters dozeParameters, BubbleController bubbleController) {
        this.mStatusBar = statusBar;
        this.mContrastColorUtil = ContrastColorUtil.getInstance(context);
        this.mContext = context;
        this.mStatusBarStateController = statusBarStateController;
        statusBarStateController.addCallback(this);
        this.mMediaManager = notificationMediaManager;
        this.mDozeParameters = dozeParameters;
        this.mWakeUpCoordinator = notificationWakeUpCoordinator;
        notificationWakeUpCoordinator.addListener(this);
        this.mBypassController = keyguardBypassController;
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
        LayoutInflater from = LayoutInflater.from(context);
        View inflateIconArea = inflateIconArea(from);
        this.mNotificationIconArea = inflateIconArea;
        this.mNotificationIcons = (NotificationIconContainer) inflateIconArea.findViewById(C0015R$id.notificationIcons);
        NotificationIconObserver notificationIconObserver = (NotificationIconObserver) Dependency.get(NotificationIconObserver.class);
        this.mNotificationIconObserver = notificationIconObserver;
        notificationIconObserver.addCallback(this);
        ((SettingsManager) Dependency.get(SettingsManager.class)).registerNotifStyleListener(this);
        this.mNotificationScrollLayout = this.mStatusBar.getNotificationScrollLayout();
        View inflate = from.inflate(C0017R$layout.center_icon_area, (ViewGroup) null);
        this.mCenteredIconArea = inflate;
        this.mCenteredIcon = (NotificationIconContainer) inflate.findViewById(C0015R$id.centeredIcon);
        initAodIcons();
    }

    public void initAodIcons() {
        boolean z = this.mAodIcons != null;
        if (z) {
            this.mAodIcons.setAnimationsEnabled(false);
            this.mAodIcons.removeAllViews();
        }
        NotificationIconContainer notificationIconContainer = (NotificationIconContainer) this.mStatusBar.getNotificationShadeWindowView().findViewById(C0015R$id.clock_notification_icon_container);
        this.mAodIcons = notificationIconContainer;
        notificationIconContainer.setOnLockScreen(true);
        updateAodIconsVisibility(false);
        updateAnimations();
        if (z) {
            updateAodNotificationIcons();
        }
    }

    public void setupShelf(NotificationShelf notificationShelf) {
        this.mShelfIcons = notificationShelf.getShelfIcons();
        notificationShelf.setCollapsedIcons(this.mNotificationIcons);
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
        for (int i3 = 0; i3 < this.mCenteredIcon.getChildCount(); i3++) {
            this.mCenteredIcon.getChildAt(i3).setLayoutParams(generateIconLayoutParams);
        }
        for (int i4 = 0; i4 < this.mAodIcons.getChildCount(); i4++) {
            this.mAodIcons.getChildAt(i4).setLayoutParams(generateIconLayoutParams);
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
        this.mAodIconAppearTranslation = resources.getDimensionPixelSize(C0012R$dimen.shelf_appear_translation);
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
                this.mIconTint = f == 0.0f ? i2 : i3;
            }
        }
        View view2 = this.mCenteredIconArea;
        if (view2 == null) {
            this.mCenteredIconTint = i;
        } else if (DarkIconDispatcher.isInArea(rect, view2)) {
            if (z) {
                this.mCenteredIconTint = i;
            } else {
                if (f != 0.0f) {
                    i2 = i3;
                }
                this.mCenteredIconTint = i2;
            }
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

    private void updateShelfIcons() {
        updateIconsForLayout($$Lambda$NotificationIconAreaController$afpYK1wAP1i0HTFHOa1jb1wzzAQ.INSTANCE, this.mShelfIcons, true, true, false, false, false, false, false, false);
    }

    public void updateStatusBarIcons() {
        updateIconsForLayout($$Lambda$NotificationIconAreaController$ujxUrqwlryo8PHBzga56kRshsA.INSTANCE, this.mNotificationIcons, false, this.mShowLowPriority, true, true, false, true, false, false);
    }

    private void updateCenterIcon() {
        updateIconsForLayout($$Lambda$NotificationIconAreaController$S6CJ2tXrA2ieNVmUpwBa8v9eeEY.INSTANCE, this.mCenteredIcon, false, true, false, false, false, false, false, true);
    }

    public void updateAodNotificationIcons() {
        updateIconsForLayout($$Lambda$NotificationIconAreaController$b7MkWJaTAeTosmR_aU3q7JZNLpI.INSTANCE, this.mAodIcons, false, true, true, true, true, true, this.mBypassController.getBypassEnabled(), false);
    }

    /* access modifiers changed from: package-private */
    public boolean shouldShouldLowPriorityIcons() {
        return this.mShowLowPriority;
    }

    private void updateIconsForLayout(Function<NotificationEntry, StatusBarIconView> function, NotificationIconContainer notificationIconContainer, boolean z, boolean z2, boolean z3, boolean z4, boolean z5, boolean z6, boolean z7, boolean z8) {
        ArrayList arrayList = new ArrayList(this.mNotificationScrollLayout.getChildCount());
        for (int i = 0; i < this.mNotificationScrollLayout.getChildCount(); i++) {
            View childAt = this.mNotificationScrollLayout.getChildAt(i);
            if (childAt instanceof ExpandableNotificationRow) {
                NotificationEntry entry = ((ExpandableNotificationRow) childAt).getEntry();
                if (shouldShowNotificationIcon(entry, z, z2, z3, z4, z5, z6, z7, z8)) {
                    StatusBarIconView apply = function.apply(entry);
                    if (apply != null) {
                        arrayList.add(apply);
                    }
                }
            }
        }
        ArrayMap<String, ArrayList<StatusBarIcon>> arrayMap = new ArrayMap<>();
        ArrayList arrayList2 = new ArrayList();
        for (int i2 = 0; i2 < notificationIconContainer.getChildCount(); i2++) {
            View childAt2 = notificationIconContainer.getChildAt(i2);
            if ((childAt2 instanceof StatusBarIconView) && !arrayList.contains(childAt2)) {
                StatusBarIconView statusBarIconView = (StatusBarIconView) childAt2;
                String groupKey = statusBarIconView.getNotification().getGroupKey();
                int i3 = 0;
                boolean z9 = false;
                while (true) {
                    if (i3 >= arrayList.size()) {
                        break;
                    }
                    StatusBarIconView statusBarIconView2 = (StatusBarIconView) arrayList.get(i3);
                    if (statusBarIconView2.getSourceIcon().sameAs(statusBarIconView.getSourceIcon()) && statusBarIconView2.getNotification().getGroupKey().equals(groupKey)) {
                        if (z9) {
                            z9 = false;
                            break;
                        }
                        z9 = true;
                    }
                    i3++;
                }
                if (z9) {
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
        for (int i4 = 0; i4 < size; i4++) {
            notificationIconContainer.removeView((View) arrayList2.get(i4));
        }
        ViewGroup.LayoutParams generateIconLayoutParams = generateIconLayoutParams();
        for (int i5 = 0; i5 < arrayList.size(); i5++) {
            StatusBarIconView statusBarIconView3 = (StatusBarIconView) arrayList.get(i5);
            notificationIconContainer.removeTransientView(statusBarIconView3);
            if (statusBarIconView3.getParent() == null) {
                if (z3) {
                    statusBarIconView3.setOnDismissListener(this.mUpdateStatusBarIcons);
                }
                notificationIconContainer.addView(statusBarIconView3, i5, generateIconLayoutParams);
            }
        }
        notificationIconContainer.setChangingViewPositions(true);
        int childCount = notificationIconContainer.getChildCount();
        for (int i6 = 0; i6 < childCount; i6++) {
            View childAt3 = notificationIconContainer.getChildAt(i6);
            View view = (StatusBarIconView) arrayList.get(i6);
            if (childAt3 != view) {
                notificationIconContainer.removeView(view);
                notificationIconContainer.addView(view, i6);
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
                    /* class com.android.systemui.statusbar.phone.$$Lambda$NotificationIconAreaController$kEHcYKNlJqRNuom7zI__dD3YiUQ */
                    public final /* synthetic */ StatusBarIconView f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void run() {
                        NotificationIconAreaController.this.lambda$applyNotificationIconsTint$4$NotificationIconAreaController(this.f$1);
                    }
                });
            }
        }
        for (int i2 = 0; i2 < this.mCenteredIcon.getChildCount(); i2++) {
            StatusBarIconView statusBarIconView2 = (StatusBarIconView) this.mCenteredIcon.getChildAt(i2);
            if (statusBarIconView2.getWidth() != 0) {
                updateTintForIcon(statusBarIconView2, this.mCenteredIconTint);
            } else {
                statusBarIconView2.executeOnLayout(new Runnable(statusBarIconView2) {
                    /* class com.android.systemui.statusbar.phone.$$Lambda$NotificationIconAreaController$DNX7QrLi_n7I734CPybT_ZrNpwI */
                    public final /* synthetic */ StatusBarIconView f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void run() {
                        NotificationIconAreaController.this.lambda$applyNotificationIconsTint$5$NotificationIconAreaController(this.f$1);
                    }
                });
            }
        }
        updateAodIconColors();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$applyNotificationIconsTint$4 */
    public /* synthetic */ void lambda$applyNotificationIconsTint$4$NotificationIconAreaController(StatusBarIconView statusBarIconView) {
        updateTintForIcon(statusBarIconView, this.mIconTint);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$applyNotificationIconsTint$5 */
    public /* synthetic */ void lambda$applyNotificationIconsTint$5$NotificationIconAreaController(StatusBarIconView statusBarIconView) {
        updateTintForIcon(statusBarIconView, this.mCenteredIconTint);
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

    @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
    public void onDozingChanged(boolean z) {
        this.mAodIcons.setDozing(z, this.mDozeParameters.getAlwaysOn() && !this.mDozeParameters.getDisplayNeedsBlanking(), 0);
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
        this.mAodIcons.setAnimationsEnabled(this.mAnimationsEnabled && !z2);
        this.mCenteredIcon.setAnimationsEnabled(this.mAnimationsEnabled && z2);
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

    public void appearAodIcons() {
        if (this.mDozeParameters.shouldControlScreenOff()) {
            this.mAodIcons.setTranslationY((float) (-this.mAodIconAppearTranslation));
            this.mAodIcons.setAlpha(0.0f);
            animateInAodIconTranslation();
            this.mAodIcons.animate().alpha(1.0f).setInterpolator(Interpolators.LINEAR).setDuration(200).start();
            return;
        }
        this.mAodIcons.setAlpha(1.0f);
        this.mAodIcons.setTranslationY(0.0f);
    }

    private void animateInAodIconTranslation() {
        this.mAodIcons.animate().setInterpolator(Interpolators.DECELERATE_QUINT).translationY(0.0f).setDuration(200).start();
    }

    private void reloadAodColor() {
        this.mAodIconTint = Utils.getColorAttrDefaultColor(this.mContext, C0009R$attr.wallpaperTextColor);
    }

    private void updateAodIconColors() {
        for (int i = 0; i < this.mAodIcons.getChildCount(); i++) {
            StatusBarIconView statusBarIconView = (StatusBarIconView) this.mAodIcons.getChildAt(i);
            if (statusBarIconView.getWidth() != 0) {
                updateTintForIcon(statusBarIconView, this.mAodIconTint);
            } else {
                statusBarIconView.executeOnLayout(new Runnable(statusBarIconView) {
                    /* class com.android.systemui.statusbar.phone.$$Lambda$NotificationIconAreaController$PUTDTipRCmrDLS4VQZByqHC4HFA */
                    public final /* synthetic */ StatusBarIconView f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void run() {
                        NotificationIconAreaController.this.lambda$updateAodIconColors$6$NotificationIconAreaController(this.f$1);
                    }
                });
            }
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$updateAodIconColors$6 */
    public /* synthetic */ void lambda$updateAodIconColors$6$NotificationIconAreaController(StatusBarIconView statusBarIconView) {
        updateTintForIcon(statusBarIconView, this.mAodIconTint);
    }

    @Override // com.android.systemui.statusbar.notification.NotificationWakeUpCoordinator.WakeUpListener
    public void onFullyHiddenChanged(boolean z) {
        boolean z2 = true;
        if (!this.mBypassController.getBypassEnabled()) {
            if (!this.mDozeParameters.getAlwaysOn() || this.mDozeParameters.getDisplayNeedsBlanking()) {
                z2 = false;
            }
            z2 &= z;
        }
        updateAodIconsVisibility(z2);
        updateAodNotificationIcons();
    }

    @Override // com.android.systemui.statusbar.notification.NotificationWakeUpCoordinator.WakeUpListener
    public void onPulseExpansionChanged(boolean z) {
        if (z) {
            updateAodIconsVisibility(true);
        }
    }

    private void updateAodIconsVisibility(boolean z) {
        boolean z2 = true;
        int i = 0;
        boolean z3 = this.mBypassController.getBypassEnabled() || this.mWakeUpCoordinator.getNotificationsFullyHidden();
        if (this.mStatusBarStateController.getState() != 1) {
            z3 = false;
        }
        if (z3 && this.mWakeUpCoordinator.isPulseExpanding()) {
            z3 = false;
        }
        if (this.mAodIconsVisible != z3) {
            this.mAodIconsVisible = z3;
            this.mAodIcons.animate().cancel();
            if (z) {
                if (this.mAodIcons.getVisibility() == 0) {
                    z2 = false;
                }
                if (!this.mAodIconsVisible) {
                    animateInAodIconTranslation();
                    CrossFadeHelper.fadeOut(this.mAodIcons);
                } else if (z2) {
                    this.mAodIcons.setVisibility(0);
                    this.mAodIcons.setAlpha(1.0f);
                    appearAodIcons();
                } else {
                    animateInAodIconTranslation();
                    CrossFadeHelper.fadeIn(this.mAodIcons);
                }
            } else {
                this.mAodIcons.setAlpha(1.0f);
                this.mAodIcons.setTranslationY(0.0f);
                NotificationIconContainer notificationIconContainer = this.mAodIcons;
                if (!z3) {
                    i = 4;
                }
                notificationIconContainer.setVisibility(i);
            }
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
