package com.android.systemui.statusbar.notification.modal;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.Property;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.policy.AppMiniWindowRowTouchCallback;
import com.android.systemui.statusbar.notification.policy.AppMiniWindowRowTouchHelper;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import com.android.systemui.statusbar.notification.row.ExpandableView;
import com.android.systemui.statusbar.notification.stack.AnimationFilter;
import com.android.systemui.statusbar.notification.stack.AnimationProperties;
import com.android.systemui.statusbar.notification.stack.ViewState;
import com.miui.systemui.EventTracker;
import com.miui.systemui.events.MiniWindowEventSource;
import java.util.function.Consumer;

public class ModalWindowView extends FrameLayout implements AppMiniWindowRowTouchCallback {
    /* access modifiers changed from: private */
    public final AnimationProperties animationProperties;
    /* access modifiers changed from: private */
    public boolean mChildrenUpdateRequested = false;
    private final ViewTreeObserver.OnPreDrawListener mChildrenUpdater;
    /* access modifiers changed from: private */
    public NotificationEntry mEntry;
    /* access modifiers changed from: private */
    public boolean mFirstAddUpdateRequested = false;
    private final ViewTreeObserver.OnPreDrawListener mFirstAddUpdater;
    private int mLayoutWidth;
    private int mMaxModalBottom;
    /* access modifiers changed from: private */
    public View mMenuView;
    /* access modifiers changed from: private */
    public final ViewState mMenuViewState = new ViewState();
    private int mModalMenuMarginTop;
    /* access modifiers changed from: private */
    public ExpandableNotificationRow mModalRow;
    private ExpandableView.OnHeightChangedListener mOnHeightChangedListener;
    private final int[] mTmpLoc = new int[2];
    private final AppMiniWindowRowTouchHelper mTouchHelper = new AppMiniWindowRowTouchHelper(this, (NotificationEntryManager) Dependency.get(NotificationEntryManager.class), (EventTracker) Dependency.get(EventTracker.class), MiniWindowEventSource.MODAL_NOTIFICATION);

    public boolean canChildBePicked(ExpandableView expandableView) {
        return true;
    }

    public ModalWindowView(Context context) {
        super(context);
        AnonymousClass1 r0 = new AnimationProperties(this) {
            private final AnimationFilter filter;

            {
                AnimationFilter animationFilter = new AnimationFilter();
                animationFilter.animateHeight();
                animationFilter.animateY();
                animationFilter.animateAlpha();
                this.filter = animationFilter;
            }

            public AnimationFilter getAnimationFilter() {
                return this.filter;
            }
        };
        r0.setDuration(300);
        this.animationProperties = r0;
        this.mChildrenUpdater = new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                if (!(ModalWindowView.this.mModalRow == null || ModalWindowView.this.mModalRow.getViewState() == null)) {
                    ModalWindowView.this.mModalRow.getViewState().animateTo(ModalWindowView.this.mModalRow, ModalWindowView.this.animationProperties);
                }
                if (ModalWindowView.this.mMenuView != null) {
                    ModalWindowView.this.mMenuViewState.animateTo(ModalWindowView.this.mMenuView, ModalWindowView.this.animationProperties);
                }
                boolean unused = ModalWindowView.this.mChildrenUpdateRequested = false;
                ModalWindowView.this.getViewTreeObserver().removeOnPreDrawListener(this);
                return true;
            }
        };
        this.mFirstAddUpdater = new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                ModalWindowView modalWindowView = ModalWindowView.this;
                modalWindowView.enterModal(modalWindowView.mEntry);
                ModalWindowView.this.getViewTreeObserver().removeOnPreDrawListener(this);
                boolean unused = ModalWindowView.this.mFirstAddUpdateRequested = false;
                return true;
            }
        };
        this.mOnHeightChangedListener = new ExpandableView.OnHeightChangedListener() {
            public void onHeightChanged(ExpandableView expandableView, boolean z) {
                ModalWindowView.this.mModalRow.resetViewState();
                ViewState access$300 = ModalWindowView.this.mMenuViewState;
                ModalWindowView modalWindowView = ModalWindowView.this;
                access$300.yTranslation = modalWindowView.getMenuYInModal(modalWindowView.mModalRow, false);
                ModalWindowView.this.requestChildrenUpdate();
            }

            public void onReset(ExpandableView expandableView) {
                ModalWindowView.this.mModalRow.resetViewState();
            }
        };
        init(context);
    }

    public ModalWindowView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        AnonymousClass1 r5 = new AnimationProperties(this) {
            private final AnimationFilter filter;

            {
                AnimationFilter animationFilter = new AnimationFilter();
                animationFilter.animateHeight();
                animationFilter.animateY();
                animationFilter.animateAlpha();
                this.filter = animationFilter;
            }

            public AnimationFilter getAnimationFilter() {
                return this.filter;
            }
        };
        r5.setDuration(300);
        this.animationProperties = r5;
        this.mChildrenUpdater = new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                if (!(ModalWindowView.this.mModalRow == null || ModalWindowView.this.mModalRow.getViewState() == null)) {
                    ModalWindowView.this.mModalRow.getViewState().animateTo(ModalWindowView.this.mModalRow, ModalWindowView.this.animationProperties);
                }
                if (ModalWindowView.this.mMenuView != null) {
                    ModalWindowView.this.mMenuViewState.animateTo(ModalWindowView.this.mMenuView, ModalWindowView.this.animationProperties);
                }
                boolean unused = ModalWindowView.this.mChildrenUpdateRequested = false;
                ModalWindowView.this.getViewTreeObserver().removeOnPreDrawListener(this);
                return true;
            }
        };
        this.mFirstAddUpdater = new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                ModalWindowView modalWindowView = ModalWindowView.this;
                modalWindowView.enterModal(modalWindowView.mEntry);
                ModalWindowView.this.getViewTreeObserver().removeOnPreDrawListener(this);
                boolean unused = ModalWindowView.this.mFirstAddUpdateRequested = false;
                return true;
            }
        };
        this.mOnHeightChangedListener = new ExpandableView.OnHeightChangedListener() {
            public void onHeightChanged(ExpandableView expandableView, boolean z) {
                ModalWindowView.this.mModalRow.resetViewState();
                ViewState access$300 = ModalWindowView.this.mMenuViewState;
                ModalWindowView modalWindowView = ModalWindowView.this;
                access$300.yTranslation = modalWindowView.getMenuYInModal(modalWindowView.mModalRow, false);
                ModalWindowView.this.requestChildrenUpdate();
            }

            public void onReset(ExpandableView expandableView) {
                ModalWindowView.this.mModalRow.resetViewState();
            }
        };
        init(context);
    }

    public ModalWindowView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        AnonymousClass1 r4 = new AnimationProperties(this) {
            private final AnimationFilter filter;

            {
                AnimationFilter animationFilter = new AnimationFilter();
                animationFilter.animateHeight();
                animationFilter.animateY();
                animationFilter.animateAlpha();
                this.filter = animationFilter;
            }

            public AnimationFilter getAnimationFilter() {
                return this.filter;
            }
        };
        r4.setDuration(300);
        this.animationProperties = r4;
        this.mChildrenUpdater = new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                if (!(ModalWindowView.this.mModalRow == null || ModalWindowView.this.mModalRow.getViewState() == null)) {
                    ModalWindowView.this.mModalRow.getViewState().animateTo(ModalWindowView.this.mModalRow, ModalWindowView.this.animationProperties);
                }
                if (ModalWindowView.this.mMenuView != null) {
                    ModalWindowView.this.mMenuViewState.animateTo(ModalWindowView.this.mMenuView, ModalWindowView.this.animationProperties);
                }
                boolean unused = ModalWindowView.this.mChildrenUpdateRequested = false;
                ModalWindowView.this.getViewTreeObserver().removeOnPreDrawListener(this);
                return true;
            }
        };
        this.mFirstAddUpdater = new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                ModalWindowView modalWindowView = ModalWindowView.this;
                modalWindowView.enterModal(modalWindowView.mEntry);
                ModalWindowView.this.getViewTreeObserver().removeOnPreDrawListener(this);
                boolean unused = ModalWindowView.this.mFirstAddUpdateRequested = false;
                return true;
            }
        };
        this.mOnHeightChangedListener = new ExpandableView.OnHeightChangedListener() {
            public void onHeightChanged(ExpandableView expandableView, boolean z) {
                ModalWindowView.this.mModalRow.resetViewState();
                ViewState access$300 = ModalWindowView.this.mMenuViewState;
                ModalWindowView modalWindowView = ModalWindowView.this;
                access$300.yTranslation = modalWindowView.getMenuYInModal(modalWindowView.mModalRow, false);
                ModalWindowView.this.requestChildrenUpdate();
            }

            public void onReset(ExpandableView expandableView) {
                ModalWindowView.this.mModalRow.resetViewState();
            }
        };
        init(context);
    }

    public ModalWindowView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        AnonymousClass1 r3 = new AnimationProperties(this) {
            private final AnimationFilter filter;

            {
                AnimationFilter animationFilter = new AnimationFilter();
                animationFilter.animateHeight();
                animationFilter.animateY();
                animationFilter.animateAlpha();
                this.filter = animationFilter;
            }

            public AnimationFilter getAnimationFilter() {
                return this.filter;
            }
        };
        r3.setDuration(300);
        this.animationProperties = r3;
        this.mChildrenUpdater = new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                if (!(ModalWindowView.this.mModalRow == null || ModalWindowView.this.mModalRow.getViewState() == null)) {
                    ModalWindowView.this.mModalRow.getViewState().animateTo(ModalWindowView.this.mModalRow, ModalWindowView.this.animationProperties);
                }
                if (ModalWindowView.this.mMenuView != null) {
                    ModalWindowView.this.mMenuViewState.animateTo(ModalWindowView.this.mMenuView, ModalWindowView.this.animationProperties);
                }
                boolean unused = ModalWindowView.this.mChildrenUpdateRequested = false;
                ModalWindowView.this.getViewTreeObserver().removeOnPreDrawListener(this);
                return true;
            }
        };
        this.mFirstAddUpdater = new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                ModalWindowView modalWindowView = ModalWindowView.this;
                modalWindowView.enterModal(modalWindowView.mEntry);
                ModalWindowView.this.getViewTreeObserver().removeOnPreDrawListener(this);
                boolean unused = ModalWindowView.this.mFirstAddUpdateRequested = false;
                return true;
            }
        };
        this.mOnHeightChangedListener = new ExpandableView.OnHeightChangedListener() {
            public void onHeightChanged(ExpandableView expandableView, boolean z) {
                ModalWindowView.this.mModalRow.resetViewState();
                ViewState access$300 = ModalWindowView.this.mMenuViewState;
                ModalWindowView modalWindowView = ModalWindowView.this;
                access$300.yTranslation = modalWindowView.getMenuYInModal(modalWindowView.mModalRow, false);
                ModalWindowView.this.requestChildrenUpdate();
            }

            public void onReset(ExpandableView expandableView) {
                ModalWindowView.this.mModalRow.resetViewState();
            }
        };
        init(context);
    }

    public void init(Context context) {
        updateResource();
        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    public void updateResource() {
        Resources resources = getResources();
        this.mModalMenuMarginTop = resources.getDimensionPixelOffset(C0012R$dimen.notification_modal_menu_margin_top);
        this.mLayoutWidth = resources.getDimensionPixelOffset(C0012R$dimen.notification_panel_width);
        this.mMaxModalBottom = resources.getDisplayMetrics().heightPixels - resources.getDimensionPixelOffset(C0012R$dimen.notification_modal_menu_bottom_max);
    }

    /* access modifiers changed from: private */
    public void requestChildrenUpdate() {
        if (!this.mChildrenUpdateRequested) {
            getViewTreeObserver().addOnPreDrawListener(this.mChildrenUpdater);
            this.mChildrenUpdateRequested = true;
            invalidate();
        }
    }

    public void enterModal(NotificationEntry notificationEntry) {
        this.mEntry = notificationEntry;
        if (!(notificationEntry.getModalRow().getIntrinsicHeight() == 0 || notificationEntry.getModalRow().getActualHeight() == 0) || this.mFirstAddUpdateRequested) {
            addRow(notificationEntry);
            addMenu(notificationEntry);
            this.animationProperties.setAnimationEndAction((Consumer<Property>) null);
            requestChildrenUpdate();
            return;
        }
        this.mFirstAddUpdateRequested = true;
        addView(notificationEntry.getModalRow());
        getViewTreeObserver().addOnPreDrawListener(this.mFirstAddUpdater);
    }

    public void exitModal(NotificationEntry notificationEntry) {
        ExpandableNotificationRow row = notificationEntry.getRow();
        this.mModalRow.getViewState().yTranslation = getRowTranslationY(row);
        this.mMenuViewState.yTranslation = getMenuYInNss(row);
        this.mMenuViewState.alpha = 0.0f;
        if (((NotificationEntryManager) Dependency.get(NotificationEntryManager.class)).getAllNotifs().contains(notificationEntry) && row.isExpanded() != this.mModalRow.isExpanded()) {
            this.mModalRow.setUserExpanded(row.isExpanded());
            this.mModalRow.notifyHeightChanged(true);
        }
        requestChildrenUpdate();
        this.animationProperties.setAnimationEndAction(new Consumer(notificationEntry) {
            public final /* synthetic */ NotificationEntry f$1;

            {
                this.f$1 = r2;
            }

            public final void accept(Object obj) {
                ModalWindowView.this.lambda$exitModal$0$ModalWindowView(this.f$1, (Property) obj);
            }
        });
        this.mEntry = null;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$exitModal$0 */
    public /* synthetic */ void lambda$exitModal$0$ModalWindowView(NotificationEntry notificationEntry, Property property) {
        removeRow(notificationEntry);
        removeMenu(notificationEntry);
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        updateResource();
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        int i = layoutParams.width;
        int i2 = this.mLayoutWidth;
        if (i != i2) {
            layoutParams.width = i2;
            setLayoutParams(layoutParams);
        }
        reAddEntry(this.mEntry);
    }

    private void reAddEntry(NotificationEntry notificationEntry) {
        if (notificationEntry != null && !this.mFirstAddUpdateRequested) {
            removeRow(notificationEntry);
            removeMenu(notificationEntry);
            addRow(notificationEntry);
            addMenu(notificationEntry);
            requestChildrenUpdate();
        }
    }

    private void addRow(NotificationEntry notificationEntry) {
        ExpandableNotificationRow modalRow = notificationEntry.getModalRow();
        this.mModalRow = modalRow;
        modalRow.setOnHeightChangedListener(this.mOnHeightChangedListener);
        if (this.mModalRow.getParent() == null) {
            addView(this.mModalRow);
        }
        if (!this.mModalRow.isExpanded()) {
            this.mModalRow.setUserExpanded(true);
            this.mModalRow.notifyHeightChanged(false);
        }
        ExpandableNotificationRow row = notificationEntry.getRow();
        row.getViewState().applyToView(this.mModalRow);
        float rowTranslationY = getRowTranslationY(row);
        this.mModalRow.setTranslationY(rowTranslationY);
        this.mModalRow.getViewState().yTranslation = rowTranslationY - Math.max(0.0f, (((float) this.mModalRow.getIntrinsicHeight()) + rowTranslationY) - ((float) this.mMaxModalBottom));
    }

    public void removeRow(NotificationEntry notificationEntry) {
        ExpandableNotificationRow expandableNotificationRow = this.mModalRow;
        if (expandableNotificationRow != null) {
            removeView(expandableNotificationRow);
            this.mModalRow.setOnHeightChangedListener((ExpandableView.OnHeightChangedListener) null);
            this.mModalRow = null;
        }
    }

    public void addMenu(NotificationEntry notificationEntry) {
        View menuView = notificationEntry.getRow().getProvider().getMenuView();
        this.mMenuView = menuView;
        if (menuView.getParent() == null) {
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(-2, -2);
            layoutParams.gravity = 1;
            addView(this.mMenuView, layoutParams);
        }
        this.mMenuView.setTranslationY(getMenuYInNss(notificationEntry.getRow()));
        this.mMenuView.setAlpha(0.0f);
        this.mMenuView.setVisibility(0);
        this.mMenuViewState.initFrom(this.mMenuView);
        this.mMenuViewState.yTranslation = getMenuYInModal(this.mModalRow, false);
        this.mMenuViewState.alpha = 1.0f;
    }

    public void removeMenu(NotificationEntry notificationEntry) {
        View view = this.mMenuView;
        if (view != null) {
            removeView(view);
            this.mMenuView = null;
        }
    }

    public void onWindowFocusChanged(boolean z) {
        super.onWindowFocusChanged(z);
        if (z) {
            requestFocus();
        }
    }

    public boolean dispatchKeyEvent(KeyEvent keyEvent) {
        if (super.dispatchKeyEvent(keyEvent)) {
            return true;
        }
        if (keyEvent.getAction() != 1 || keyEvent.getKeyCode() != 4) {
            return false;
        }
        ((ModalController) Dependency.get(ModalController.class)).animExitModal();
        return true;
    }

    private float getRowTranslationY(ExpandableNotificationRow expandableNotificationRow) {
        if (expandableNotificationRow.isChildInGroup()) {
            return expandableNotificationRow.getNotificationParent().getTranslationY() + expandableNotificationRow.getTranslationY();
        }
        return expandableNotificationRow.getTranslationY();
    }

    private float getMenuYInNss(ExpandableNotificationRow expandableNotificationRow) {
        return getRowTranslationY(expandableNotificationRow) + ((float) expandableNotificationRow.getActualHeight()) + ((float) this.mModalMenuMarginTop);
    }

    /* access modifiers changed from: private */
    public float getMenuYInModal(ExpandableNotificationRow expandableNotificationRow, boolean z) {
        return expandableNotificationRow.getViewState().yTranslation + ((float) (z ? expandableNotificationRow.getActualHeight() : expandableNotificationRow.getIntrinsicHeight())) + ((float) this.mModalMenuMarginTop);
    }

    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        if (!this.mTouchHelper.onInterceptTouchEvent(motionEvent)) {
            return super.onInterceptTouchEvent(motionEvent);
        }
        return true;
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (!this.mTouchHelper.onTouchEvent(motionEvent)) {
            return super.onTouchEvent(motionEvent);
        }
        return true;
    }

    public void onMiniWindowTrackingUpdate(float f) {
        updateMenuLayoutVisibility(f == 0.0f);
    }

    public void onMiniWindowReset() {
        updateMenuLayoutVisibility(true);
    }

    private void updateMenuLayoutVisibility(boolean z) {
        float f = z ? 1.0f : 0.0f;
        boolean z2 = this.mMenuViewState.alpha != f;
        this.mMenuViewState.alpha = f;
        if (z2) {
            requestChildrenUpdate();
        }
    }

    public void onStartMiniWindowExpandAnimation() {
        ((ModalController) Dependency.get(ModalController.class)).animExitModal(500, false);
        ((CommandQueue) Dependency.get(CommandQueue.class)).animateCollapsePanels(0, false);
    }

    public void onMiniWindowAppLaunched() {
        ((ModalController) Dependency.get(ModalController.class)).exitModalImmediately();
    }

    public ExpandableView getChildAtRawPosition(float f, float f2) {
        ExpandableNotificationRow expandableNotificationRow = this.mModalRow;
        if (expandableNotificationRow == null || !expandableNotificationRow.isAttachedToWindow()) {
            return null;
        }
        expandableNotificationRow.getLocationInWindow(this.mTmpLoc);
        int[] iArr = this.mTmpLoc;
        if (f <= ((float) iArr[0]) || f >= ((float) (iArr[0] + expandableNotificationRow.getWidth()))) {
            return null;
        }
        int[] iArr2 = this.mTmpLoc;
        if (f2 <= ((float) iArr2[1]) || f2 >= ((float) (iArr2[1] + expandableNotificationRow.getHeight()))) {
            return null;
        }
        return expandableNotificationRow;
    }
}
