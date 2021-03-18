package com.android.systemui.statusbar.notification.modal;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0017R$layout;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.analytics.NotificationStat;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import com.android.systemui.statusbar.notification.row.MiuiExpandableNotificationRow;
import com.android.systemui.statusbar.notification.row.NotificationBackgroundView;
import com.android.systemui.statusbar.phone.StatusBar;
import com.miui.systemui.DebugConfig;
import com.miui.systemui.events.ModalExitMode;
import com.miui.systemui.util.HapticFeedBackImpl;
import java.util.ArrayList;
import java.util.Iterator;
import kotlin.jvm.internal.Intrinsics;
import miuix.view.animation.CubicEaseInOutInterpolator;
import org.jetbrains.annotations.NotNull;

/* compiled from: ModalController.kt */
public final class ModalController {
    @NotNull
    private final Context context;
    private final long defaultDuration = 300;
    private NotificationEntry entry;
    private boolean isAnimating;
    private boolean isModal;
    private boolean mDownEventInjected;
    @NotNull
    private final StatusBarStateController mStatusBarStateController;
    private ExpandableNotificationRow modalRow;
    @NotNull
    public ModalRowInflater modalRowInflater;
    private ModalWindowManager modalWindowManager;
    private ModalWindowView modalWindowView;
    private final ArrayList<OnModalChangeListener> onModalChangeListeners = new ArrayList<>();
    @NotNull
    private final StatusBar statusBar;

    /* compiled from: ModalController.kt */
    public interface OnModalChangeListener {
        void onChange(boolean z);
    }

    public ModalController(@NotNull Context context2, @NotNull StatusBar statusBar2, @NotNull StatusBarStateController statusBarStateController) {
        Intrinsics.checkParameterIsNotNull(context2, "context");
        Intrinsics.checkParameterIsNotNull(statusBar2, "statusBar");
        Intrinsics.checkParameterIsNotNull(statusBarStateController, "mStatusBarStateController");
        this.context = context2;
        this.statusBar = statusBar2;
        this.mStatusBarStateController = statusBarStateController;
        this.modalWindowManager = new ModalWindowManager(context2);
        addModalWindow();
        ((NotificationEntryManager) Dependency.get(NotificationEntryManager.class)).addNotificationLifetimeExtender(new ModalLifetimeExtender(this));
        this.mStatusBarStateController.addCallback(new StatusBarStateController.StateListener(this) {
            /* class com.android.systemui.statusbar.notification.modal.ModalController.AnonymousClass1 */
            final /* synthetic */ ModalController this$0;

            /* JADX WARN: Incorrect args count in method signature: ()V */
            {
                this.this$0 = r1;
            }

            @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
            public void onStateChanged(int i) {
                if (i == 1) {
                    ModalController.animExitModal$default(this.this$0, null, 1, null);
                }
            }
        });
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
                    startAnimator$default(this, new ModalController$animEnterModal$updateListener$1(this), new ModalController$animEnterModal$animatorListener$1(this, expandableNotificationRow), 0, 4, null);
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
        ExpandableNotificationRow expandableNotificationRow = this.modalRow;
        this.modalWindowManager.setBlurRatio(0.0f);
        ModalWindowView modalWindowView2 = this.modalWindowView;
        if (modalWindowView2 != null) {
            modalWindowView2.setOnClickListener(new ModalController$enterModal$1(this));
            if (expandableNotificationRow != null) {
                expandableNotificationRow.setOnClickListener(new ModalController$enterModal$2(this));
            }
            if (!(expandableNotificationRow == null || (notificationBackgroundView = (NotificationBackgroundView) expandableNotificationRow.findViewById(C0015R$id.backgroundNormal)) == null)) {
                notificationBackgroundView.disableBlur();
            }
            ModalWindowView modalWindowView3 = this.modalWindowView;
            if (modalWindowView3 != null) {
                modalWindowView3.enterModal(this.entry);
                this.modalWindowManager.show();
                ((HapticFeedBackImpl) Dependency.get(HapticFeedBackImpl.class)).longPress();
                if (expandableNotificationRow instanceof MiuiExpandableNotificationRow) {
                    ((MiuiExpandableNotificationRow) expandableNotificationRow).setIsInModal(true);
                    return;
                }
                return;
            }
            Intrinsics.throwUninitializedPropertyAccessException("modalWindowView");
            throw null;
        }
        Intrinsics.throwUninitializedPropertyAccessException("modalWindowView");
        throw null;
    }

    public final void animExitModelCollapsePanels() {
        animExitModal(ModalExitMode.MORE.name());
        this.statusBar.animateCollapsePanels(0, false);
    }

    public static /* synthetic */ void animExitModal$default(ModalController modalController, String str, int i, Object obj) {
        if ((i & 1) != 0) {
            str = ModalExitMode.OTHER.name();
        }
        modalController.animExitModal(str);
    }

    public final void animExitModal(@NotNull String str) {
        Intrinsics.checkParameterIsNotNull(str, "exitMode");
        animExitModal(this.defaultDuration, true, str);
    }

    public final void animExitModal(long j, boolean z, @NotNull String str) {
        Intrinsics.checkParameterIsNotNull(str, "exitMode");
        if (this.isModal && !this.isAnimating) {
            ModalController$animExitModal$animatorListener$1 modalController$animExitModal$animatorListener$1 = null;
            if (z) {
                ModalWindowView modalWindowView2 = this.modalWindowView;
                if (modalWindowView2 != null) {
                    modalWindowView2.exitModal(this.entry);
                } else {
                    Intrinsics.throwUninitializedPropertyAccessException("modalWindowView");
                    throw null;
                }
            }
            this.modalWindowManager.clearFocus();
            ModalController$animExitModal$updateListener$1 modalController$animExitModal$updateListener$1 = new ModalController$animExitModal$updateListener$1(this);
            ModalController$animExitModal$animatorListener$1 modalController$animExitModal$animatorListener$12 = new ModalController$animExitModal$animatorListener$1(this, str);
            if (z) {
                modalController$animExitModal$animatorListener$1 = modalController$animExitModal$animatorListener$12;
            }
            startAnimator(modalController$animExitModal$updateListener$1, modalController$animExitModal$animatorListener$1, j);
            this.isAnimating = true;
        }
    }

    public final void exitModalImmediately() {
        ModalWindowView modalWindowView2 = this.modalWindowView;
        if (modalWindowView2 != null) {
            modalWindowView2.exitModal(this.entry);
            exitModal(ModalExitMode.DOWNPULL.name());
            this.isAnimating = false;
            return;
        }
        Intrinsics.throwUninitializedPropertyAccessException("modalWindowView");
        throw null;
    }

    /* access modifiers changed from: private */
    public final void exitModal(String str) {
        ((NotificationStat) Dependency.get(NotificationStat.class)).onExitModal(this.entry, str);
        this.isModal = false;
        this.modalWindowManager.hide();
        this.modalRow = null;
        this.entry = null;
        Iterator<T> it = this.onModalChangeListeners.iterator();
        while (it.hasNext()) {
            it.next().onChange(false);
        }
    }

    static /* synthetic */ void startAnimator$default(ModalController modalController, ValueAnimator.AnimatorUpdateListener animatorUpdateListener, Animator.AnimatorListener animatorListener, long j, int i, Object obj) {
        if ((i & 4) != 0) {
            j = modalController.defaultDuration;
        }
        modalController.startAnimator(animatorUpdateListener, animatorListener, j);
    }

    private final void startAnimator(ValueAnimator.AnimatorUpdateListener animatorUpdateListener, Animator.AnimatorListener animatorListener, long j) {
        ValueAnimator ofFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
        Intrinsics.checkExpressionValueIsNotNull(ofFloat, "animator");
        ofFloat.setDuration(j);
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
        return !expandableNotificationRow.isPinned() && (Intrinsics.areEqual(expandableNotificationRow.getShowingLayout(), expandableNotificationRow.getPublicLayout()) ^ true);
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
        return this.isModal && Intrinsics.areEqual(this.entry, notificationEntry);
    }

    public final boolean maybeDispatchMotionEvent(@NotNull MotionEvent motionEvent) {
        boolean z;
        Intrinsics.checkParameterIsNotNull(motionEvent, "event");
        if (this.isModal) {
            if (!this.mDownEventInjected) {
                injectMotionEvent(motionEvent, 0);
                this.mDownEventInjected = true;
            }
            if (motionEvent.getActionMasked() != 1) {
                ModalWindowView modalWindowView2 = this.modalWindowView;
                if (modalWindowView2 != null) {
                    modalWindowView2.dispatchTouchEvent(motionEvent);
                } else {
                    Intrinsics.throwUninitializedPropertyAccessException("modalWindowView");
                    throw null;
                }
            } else {
                injectMotionEvent(motionEvent, 3);
            }
            z = true;
        } else {
            z = false;
        }
        if (motionEvent.getActionMasked() == 1 || motionEvent.getActionMasked() == 3) {
            this.mDownEventInjected = false;
        }
        return z;
    }

    private final void injectMotionEvent(MotionEvent motionEvent, int i) {
        MotionEvent obtain = MotionEvent.obtain(motionEvent);
        Intrinsics.checkExpressionValueIsNotNull(obtain, "downEvent");
        obtain.setAction(i);
        ModalWindowView modalWindowView2 = this.modalWindowView;
        if (modalWindowView2 != null) {
            modalWindowView2.dispatchTouchEvent(obtain);
            obtain.recycle();
            return;
        }
        Intrinsics.throwUninitializedPropertyAccessException("modalWindowView");
        throw null;
    }
}
