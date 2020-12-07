package com.android.systemui.statusbar.notification.modal;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0017R$layout;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import com.android.systemui.statusbar.notification.row.NotificationBackgroundView;
import com.android.systemui.statusbar.phone.StatusBar;
import com.miui.systemui.DebugConfig;
import com.miui.systemui.util.HapticFeedBackImpl;
import java.util.ArrayList;
import kotlin.jvm.internal.Intrinsics;
import miuix.view.animation.CubicEaseInOutInterpolator;
import org.jetbrains.annotations.NotNull;

/* compiled from: ModalController.kt */
public final class ModalController {
    @NotNull
    private final Context context;
    private final long defaultDuration = 300;
    private long duration = 300;
    /* access modifiers changed from: private */
    public NotificationEntry entry;
    /* access modifiers changed from: private */
    public boolean isAnimating;
    private boolean isModal;
    private ExpandableNotificationRow modalRow;
    @NotNull
    public ModalRowInflater modalRowInflater;
    private ModalWindowManager modalWindowManager;
    private ModalWindowView modalWindowView;
    /* access modifiers changed from: private */
    public final ArrayList<OnModalChangeListener> onModalChangeListeners = new ArrayList<>();
    @NotNull
    private final StatusBar statusBar;

    /* compiled from: ModalController.kt */
    public interface OnModalChangeListener {
        void onChange(boolean z);
    }

    public ModalController(@NotNull Context context2, @NotNull StatusBar statusBar2) {
        Intrinsics.checkParameterIsNotNull(context2, "context");
        Intrinsics.checkParameterIsNotNull(statusBar2, "statusBar");
        this.context = context2;
        this.statusBar = statusBar2;
        this.modalWindowManager = new ModalWindowManager(context2);
        addModalWindow();
        ((NotificationEntryManager) Dependency.get(NotificationEntryManager.class)).addNotificationLifetimeExtender(new ModalLifetimeExtender(this));
    }

    @NotNull
    public final StatusBar getStatusBar() {
        return this.statusBar;
    }

    private final void addModalWindow() {
        View inflate = LayoutInflater.from(this.context).inflate(C0017R$layout.miui_modal_window, (ViewGroup) null);
        inflate.setOnClickListener(new ModalController$addModalWindow$1(this));
        this.modalWindowManager.addNotificationModalWindow(inflate);
        View findViewById = inflate.findViewById(C0015R$id.modal_window_view);
        Intrinsics.checkExpressionValueIsNotNull(findViewById, "view.findViewById(R.id.modal_window_view)");
        this.modalWindowView = (ModalWindowView) findViewById;
    }

    public final void tryAnimEnterModal(@NotNull ExpandableNotificationRow expandableNotificationRow) {
        Intrinsics.checkParameterIsNotNull(expandableNotificationRow, "row");
        if (canEnterModal(expandableNotificationRow)) {
            animEnterModal(expandableNotificationRow);
        }
    }

    private final void animEnterModal(ExpandableNotificationRow expandableNotificationRow) {
        StringBuilder sb = new StringBuilder();
        sb.append("enterModal ");
        NotificationEntry entry2 = expandableNotificationRow.getEntry();
        Intrinsics.checkExpressionValueIsNotNull(entry2, "row.entry");
        sb.append(entry2.getKey());
        Log.d("ModalController", sb.toString());
        if (!this.isModal && !this.isAnimating) {
            this.duration = this.defaultDuration;
            ModalRowInflater modalRowInflater2 = this.modalRowInflater;
            ExpandableNotificationRow expandableNotificationRow2 = null;
            if (modalRowInflater2 != null) {
                Context context2 = this.context;
                NotificationEntry entry3 = expandableNotificationRow.getEntry();
                Intrinsics.checkExpressionValueIsNotNull(entry3, "row.entry");
                ModalWindowView modalWindowView2 = this.modalWindowView;
                if (modalWindowView2 != null) {
                    modalRowInflater2.inflateModalRow(context2, entry3, modalWindowView2);
                    this.isModal = true;
                    this.isAnimating = true;
                    NotificationEntry entry4 = expandableNotificationRow.getEntry();
                    this.entry = entry4;
                    if (entry4 != null) {
                        expandableNotificationRow2 = entry4.getModalRow();
                    }
                    this.modalRow = expandableNotificationRow2;
                    enterModal();
                    startAnimator(new ModalController$animEnterModal$updateListener$1(this), new ModalController$animEnterModal$animatorListener$1(this, expandableNotificationRow));
                    return;
                }
                Intrinsics.throwUninitializedPropertyAccessException("modalWindowView");
                throw null;
            }
            Intrinsics.throwUninitializedPropertyAccessException("modalRowInflater");
            throw null;
        }
    }

    private final void enterModal() {
        NotificationBackgroundView notificationBackgroundView;
        this.modalWindowManager.setBlurRatio(0.0f);
        ModalWindowView modalWindowView2 = this.modalWindowView;
        if (modalWindowView2 != null) {
            modalWindowView2.setOnClickListener(new ModalController$enterModal$1(this));
            ExpandableNotificationRow expandableNotificationRow = this.modalRow;
            if (expandableNotificationRow != null) {
                expandableNotificationRow.setOnClickListener(new ModalController$enterModal$2(this));
            }
            ExpandableNotificationRow expandableNotificationRow2 = this.modalRow;
            if (!(expandableNotificationRow2 == null || (notificationBackgroundView = (NotificationBackgroundView) expandableNotificationRow2.findViewById(C0015R$id.backgroundNormal)) == null)) {
                notificationBackgroundView.disableBlur();
            }
            ModalWindowView modalWindowView3 = this.modalWindowView;
            if (modalWindowView3 != null) {
                modalWindowView3.enterModal(this.entry);
                this.modalWindowManager.show();
                ((HapticFeedBackImpl) Dependency.get(HapticFeedBackImpl.class)).longPress();
                return;
            }
            Intrinsics.throwUninitializedPropertyAccessException("modalWindowView");
            throw null;
        }
        Intrinsics.throwUninitializedPropertyAccessException("modalWindowView");
        throw null;
    }

    public final void animExitModelCollapsePanels() {
        animExitModal();
        this.statusBar.animateCollapsePanels(0, false);
    }

    public final void animExitModal(long j) {
        this.duration = j;
        animExitModal();
    }

    public final void animExitModal() {
        if (this.isModal && !this.isAnimating) {
            this.isAnimating = true;
            ModalWindowView modalWindowView2 = this.modalWindowView;
            if (modalWindowView2 != null) {
                modalWindowView2.exitModal(this.entry);
                startAnimator(new ModalController$animExitModal$updateListener$1(this), new ModalController$animExitModal$animatorListener$1(this));
                return;
            }
            Intrinsics.throwUninitializedPropertyAccessException("modalWindowView");
            throw null;
        }
    }

    /* access modifiers changed from: private */
    public final void exitModal() {
        this.isModal = false;
        this.modalWindowManager.hide();
        this.modalRow = null;
        this.entry = null;
        for (OnModalChangeListener onChange : this.onModalChangeListeners) {
            onChange.onChange(false);
        }
    }

    private final void startAnimator(ValueAnimator.AnimatorUpdateListener animatorUpdateListener, Animator.AnimatorListener animatorListener) {
        ValueAnimator ofFloat = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
        Intrinsics.checkExpressionValueIsNotNull(ofFloat, "animator");
        ofFloat.setDuration(this.duration);
        ofFloat.setInterpolator(new CubicEaseInOutInterpolator());
        if (animatorUpdateListener != null) {
            ofFloat.addUpdateListener(animatorUpdateListener);
        }
        if (animatorListener != null) {
            ofFloat.addListener(animatorListener);
        }
        ofFloat.start();
    }

    /* access modifiers changed from: private */
    public final void updateExpandState(float f) {
        this.modalWindowManager.setBlurRatio(f);
    }

    private final boolean canEnterModal(ExpandableNotificationRow expandableNotificationRow) {
        return !expandableNotificationRow.isPinned();
    }

    static {
        boolean z = DebugConfig.DEBUG_NOTIFICATION;
    }

    public final void addOnModalChangeListener(@NotNull OnModalChangeListener onModalChangeListener) {
        Intrinsics.checkParameterIsNotNull(onModalChangeListener, "listener");
        this.onModalChangeListeners.add(onModalChangeListener);
    }

    public final void removeOnModalChangeListener(@NotNull OnModalChangeListener onModalChangeListener) {
        Intrinsics.checkParameterIsNotNull(onModalChangeListener, "listener");
        this.onModalChangeListeners.remove(onModalChangeListener);
    }

    public final boolean shouldExtendLifetime(@NotNull NotificationEntry notificationEntry) {
        Intrinsics.checkParameterIsNotNull(notificationEntry, "entry");
        return this.isModal && Intrinsics.areEqual((Object) this.entry, (Object) notificationEntry);
    }
}
