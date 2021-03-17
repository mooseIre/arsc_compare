package com.android.systemui.qs;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.MiuiStatusBarManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.ViewTreeObserver;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0017R$layout;
import com.android.systemui.C0022R$style;
import com.android.systemui.Interpolators;
import com.android.systemui.controlcenter.phone.ControlPanelController;
import com.android.systemui.plugins.qs.QS;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.qs.customize.MiuiQSCustomizer;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.notification.stack.PanelAppearDisappearEvent;
import com.android.systemui.statusbar.phone.NotificationsQuickSettingsContainer;
import com.android.systemui.statusbar.policy.RemoteInputQuickSettingsDisabler;
import com.android.systemui.util.InjectionInflationController;
import com.android.systemui.util.LifecycleFragment;
import java.util.concurrent.Executor;

public class QSFragment extends LifecycleFragment implements QS, CommandQueue.Callbacks, StatusBarStateController.StateListener, ControlPanelController.UseControlPanelChangeListener {
    private final Animator.AnimatorListener mAnimateHeaderSlidingInListener = new AnimatorListenerAdapter() {
        /* class com.android.systemui.qs.QSFragment.AnonymousClass4 */

        public void onAnimationEnd(Animator animator) {
            QSFragment.this.mHeaderAnimating = false;
            QSFragment.this.updateQsState();
        }
    };
    private boolean mAppeared = true;
    private final Handler mBgHandler;
    private QSContainerImpl mContainer;
    private ControlPanelController mControlPanelController;
    private long mDelay;
    private QSFooter mFooter;
    protected MiuiNotificationShadeHeader mHeader;
    private boolean mHeaderAnimating;
    private final QSTileHost mHost;
    private final InjectionInflationController mInjectionInflater;
    private boolean mLastKeyguardAndExpanded;
    private float mLastQSExpansion = -1.0f;
    private int mLastViewHeight;
    private int mLayoutDirection;
    private boolean mListening;
    private QSAnimator mQSAnimator;
    private MiuiQSCustomizer mQSCustomizer;
    private MiuiQSDetail mQSDetail;
    protected QSPanel mQSPanel;
    protected NonInterceptingScrollView mQSPanelScrollView;
    private final Rect mQsBounds = new Rect();
    private boolean mQsDisabled;
    private boolean mQsExpanded;
    private final RemoteInputQuickSettingsDisabler mRemoteInputQuickSettingsDisabler;
    private final ContentResolver mResolver;
    private boolean mShowCollapsedOnKeyguard;
    private ContentObserver mShowDataUsageObserver;
    private boolean mStackScrollerOverscrolling;
    private final ViewTreeObserver.OnPreDrawListener mStartHeaderSlidingIn = new ViewTreeObserver.OnPreDrawListener() {
        /* class com.android.systemui.qs.QSFragment.AnonymousClass3 */

        public boolean onPreDraw() {
            QSFragment.this.getView().getViewTreeObserver().removeOnPreDrawListener(this);
            QSFragment.this.getView().animate().translationY(0.0f).setStartDelay(QSFragment.this.mDelay).setDuration(448).setInterpolator(Interpolators.FAST_OUT_SLOW_IN).setListener(QSFragment.this.mAnimateHeaderSlidingInListener).start();
            return true;
        }
    };
    private int mState;
    private final StatusBarStateController mStatusBarStateController;
    private final Executor mUIExecutor;

    private void setEditLocation(View view) {
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void setHasNotifications(boolean z) {
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void setHeaderClickable(boolean z) {
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void setPanelView(QS.HeightListener heightListener) {
    }

    public QSFragment(RemoteInputQuickSettingsDisabler remoteInputQuickSettingsDisabler, InjectionInflationController injectionInflationController, QSTileHost qSTileHost, StatusBarStateController statusBarStateController, CommandQueue commandQueue, ControlPanelController controlPanelController, Context context, Looper looper, Executor executor) {
        this.mRemoteInputQuickSettingsDisabler = remoteInputQuickSettingsDisabler;
        this.mInjectionInflater = injectionInflationController;
        this.mControlPanelController = controlPanelController;
        commandQueue.observe(getLifecycle(), this);
        this.mHost = qSTileHost;
        this.mStatusBarStateController = statusBarStateController;
        this.mBgHandler = new Handler(looper);
        this.mResolver = context.getContentResolver();
        this.mUIExecutor = executor;
    }

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        return this.mInjectionInflater.injectable(layoutInflater.cloneInContext(new ContextThemeWrapper(getContext(), C0022R$style.qs_theme))).inflate(C0017R$layout.qs_panel, viewGroup, false);
    }

    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        this.mQSPanel = (QSPanel) view.findViewById(C0015R$id.quick_settings_panel);
        NonInterceptingScrollView nonInterceptingScrollView = (NonInterceptingScrollView) view.findViewById(C0015R$id.expanded_qs_scroll_view);
        this.mQSPanelScrollView = nonInterceptingScrollView;
        nonInterceptingScrollView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            /* class com.android.systemui.qs.$$Lambda$QSFragment$2XSLuGneMm7PezTcR5XlC3hGadQ */

            public final void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
                QSFragment.this.lambda$onViewCreated$0$QSFragment(view, i, i2, i3, i4, i5, i6, i7, i8);
            }
        });
        this.mQSPanelScrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            /* class com.android.systemui.qs.$$Lambda$QSFragment$D2SSstlg0NEgLdXPc7QqvH01NE */

            public final void onScrollChange(View view, int i, int i2, int i3, int i4) {
                QSFragment.this.lambda$onViewCreated$1$QSFragment(view, i, i2, i3, i4);
            }
        });
        this.mQSDetail = (MiuiQSDetail) view.findViewById(C0015R$id.qs_detail);
        this.mHeader = (MiuiNotificationShadeHeader) view.findViewById(C0015R$id.header);
        this.mQSPanel.setHeaderContainer((ViewGroup) view.findViewById(C0015R$id.header_text_container));
        this.mFooter = (QSFooter) view.findViewById(C0015R$id.qs_footer);
        QSContainerImpl qSContainerImpl = (QSContainerImpl) view.findViewById(C0015R$id.quick_settings_container);
        this.mContainer = qSContainerImpl;
        this.mQSDetail.setQsPanel(this.mQSPanel, this.mHeader, qSContainerImpl.getQuickQSPanel(), (View) this.mFooter);
        this.mQSAnimator = new QSAnimator(this, this.mContainer.getQuickQSPanel(), this.mQSPanel);
        MiuiQSCustomizer miuiQSCustomizer = (MiuiQSCustomizer) view.findViewById(C0015R$id.qs_customize);
        this.mQSCustomizer = miuiQSCustomizer;
        miuiQSCustomizer.setQs(this);
        if (bundle != null) {
            setExpanded(bundle.getBoolean("expanded"));
            setListening(bundle.getBoolean("listening"));
            setEditLocation(view);
            this.mQSCustomizer.restoreInstanceState(bundle);
            if (this.mQsExpanded) {
                this.mQSPanel.getTileLayout().restoreInstanceState(bundle);
            }
        }
        setHost(this.mHost);
        this.mStatusBarStateController.addCallback(this);
        this.mControlPanelController.addCallback((ControlPanelController.UseControlPanelChangeListener) this);
        onStateChanged(this.mStatusBarStateController.getState());
        view.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            /* class com.android.systemui.qs.$$Lambda$QSFragment$O2Q4y8liaaT1BCWBXINGcury9NY */

            public final void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
                QSFragment.this.lambda$onViewCreated$2$QSFragment(view, i, i2, i3, i4, i5, i6, i7, i8);
            }
        });
        this.mShowDataUsageObserver = new ContentObserver(this.mBgHandler) {
            /* class com.android.systemui.qs.QSFragment.AnonymousClass1 */

            public void onChange(boolean z) {
                QSFragment.this.updateQSDataUsage();
            }
        };
        updateQSDataUsage();
        this.mResolver.registerContentObserver(Settings.System.getUriFor("status_bar_show_network_assistant"), false, this.mShowDataUsageObserver, -1);
        this.mShowDataUsageObserver.onChange(false);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onViewCreated$0 */
    public /* synthetic */ void lambda$onViewCreated$0$QSFragment(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        updateQsBounds();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onViewCreated$1 */
    public /* synthetic */ void lambda$onViewCreated$1$QSFragment(View view, int i, int i2, int i3, int i4) {
        this.mQSAnimator.onQsScrollingChanged();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onViewCreated$2 */
    public /* synthetic */ void lambda$onViewCreated$2$QSFragment(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        if (i6 - i8 != i2 - i4) {
            float f = this.mLastQSExpansion;
            setQsExpansion(f, f);
        }
    }

    @Override // com.android.systemui.util.LifecycleFragment
    public void onDestroy() {
        super.onDestroy();
        this.mStatusBarStateController.removeCallback(this);
        this.mControlPanelController.removeCallback((ControlPanelController.UseControlPanelChangeListener) this);
        if (this.mListening) {
            setListening(false);
        }
        this.mQSCustomizer.setQs(null);
    }

    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putBoolean("expanded", this.mQsExpanded);
        bundle.putBoolean("listening", this.mListening);
        this.mQSCustomizer.saveInstanceState(bundle);
        if (this.mQsExpanded) {
            this.mQSPanel.getTileLayout().saveInstanceState(bundle);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isListening() {
        return this.mListening;
    }

    /* access modifiers changed from: package-private */
    public boolean isExpanded() {
        return this.mQsExpanded;
    }

    @Override // com.android.systemui.plugins.qs.QS
    public View getHeader() {
        return this.mHeader;
    }

    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        setEditLocation(getView());
        if (configuration.getLayoutDirection() != this.mLayoutDirection) {
            this.mLayoutDirection = configuration.getLayoutDirection();
            QSAnimator qSAnimator = this.mQSAnimator;
            if (qSAnimator != null) {
                qSAnimator.onRtlChanged();
            }
        }
        MiuiNotificationShadeHeader miuiNotificationShadeHeader = this.mHeader;
        if (miuiNotificationShadeHeader != null) {
            miuiNotificationShadeHeader.onConfigurationChanged(configuration);
        }
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void setContainer(ViewGroup viewGroup) {
        if (viewGroup instanceof NotificationsQuickSettingsContainer) {
            NotificationsQuickSettingsContainer notificationsQuickSettingsContainer = (NotificationsQuickSettingsContainer) viewGroup;
            this.mQSCustomizer.setContainer(notificationsQuickSettingsContainer);
            this.mQSDetail.setContainer(notificationsQuickSettingsContainer);
        }
    }

    @Override // com.android.systemui.plugins.qs.QS
    public boolean isCustomizing() {
        return this.mQSCustomizer.isCustomizing();
    }

    public void setHost(QSTileHost qSTileHost) {
        this.mQSPanel.setHost(qSTileHost, this.mQSCustomizer);
        this.mFooter.setQSPanel(this.mQSPanel);
        this.mQSDetail.setHost(qSTileHost);
        this.mContainer.getQuickQSPanel().setQSPanel(this.mQSPanel);
        this.mContainer.getQuickQSPanel().setHost(qSTileHost, null);
        QSAnimator qSAnimator = this.mQSAnimator;
        if (qSAnimator != null) {
            qSAnimator.setHost(qSTileHost);
        }
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void disable(int i, int i2, int i3, boolean z) {
        if (i == getContext().getDisplayId()) {
            int adjustDisableFlags = this.mRemoteInputQuickSettingsDisabler.adjustDisableFlags(i3);
            boolean z2 = (adjustDisableFlags & 1) != 0;
            if (z2 != this.mQsDisabled) {
                this.mQsDisabled = z2;
                this.mContainer.disable(i2, adjustDisableFlags, z);
                this.mFooter.disable(i2, adjustDisableFlags, z);
                this.mContainer.getQuickQSPanel().setDisabledByPolicy(z2);
                updateQsState();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateQsState() {
        boolean z = true;
        int i = 0;
        boolean z2 = this.mQsExpanded || this.mStackScrollerOverscrolling || this.mHeaderAnimating;
        this.mQSPanel.setExpanded(this.mQsExpanded);
        this.mQSDetail.setExpanded(this.mQsExpanded);
        boolean isKeyguardShowing = isKeyguardShowing();
        this.mHeader.setVisibility((this.mQsExpanded || !isKeyguardShowing || this.mHeaderAnimating || this.mShowCollapsedOnKeyguard) ? 0 : 4);
        this.mHeader.setExpanded((isKeyguardShowing && !this.mHeaderAnimating && !this.mShowCollapsedOnKeyguard) || (this.mQsExpanded && !this.mStackScrollerOverscrolling));
        this.mContainer.getQuickQSPanel().setExpanded((isKeyguardShowing && !this.mHeaderAnimating && !this.mShowCollapsedOnKeyguard) || (this.mQsExpanded && !this.mStackScrollerOverscrolling));
        this.mFooter.setVisibility((this.mQsDisabled || (!this.mQsExpanded && isKeyguardShowing && !this.mHeaderAnimating && !this.mShowCollapsedOnKeyguard)) ? 4 : 0);
        QSFooter qSFooter = this.mFooter;
        if ((!isKeyguardShowing || this.mHeaderAnimating || this.mShowCollapsedOnKeyguard) && (!this.mQsExpanded || this.mStackScrollerOverscrolling)) {
            z = false;
        }
        qSFooter.setExpanded(z);
        QSPanel qSPanel = this.mQSPanel;
        if (this.mQsDisabled || !z2) {
            i = 4;
        }
        qSPanel.setVisibility(i);
    }

    private boolean isKeyguardShowing() {
        return this.mStatusBarStateController.getState() == 1;
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void setShowCollapsedOnKeyguard(boolean z) {
        if (z != this.mShowCollapsedOnKeyguard) {
            this.mShowCollapsedOnKeyguard = z;
            updateQsState();
            QSAnimator qSAnimator = this.mQSAnimator;
            if (qSAnimator != null) {
                qSAnimator.setShowCollapsedOnKeyguard(z);
            }
            if (!z && isKeyguardShowing()) {
                setQsExpansion(this.mLastQSExpansion, 0.0f);
            }
        }
    }

    public QSPanel getQsPanel() {
        return this.mQSPanel;
    }

    @Override // com.android.systemui.plugins.qs.QS
    public boolean isShowingDetail() {
        return this.mQSPanel.isShowingCustomize() || this.mQSDetail.isShowingDetail();
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void setExpanded(boolean z) {
        this.mQsExpanded = z;
        this.mQSPanel.setListening(this.mListening, z);
        this.mContainer.setBrightnessListening(this.mListening);
        updateQsState();
    }

    private void setKeyguardShowing(boolean z) {
        this.mLastQSExpansion = -1.0f;
        QSAnimator qSAnimator = this.mQSAnimator;
        if (qSAnimator != null) {
            qSAnimator.setOnKeyguard(z);
        }
        this.mFooter.setKeyguardShowing(z);
        updateQsState();
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void setOverscrolling(boolean z) {
        this.mStackScrollerOverscrolling = z;
        updateQsState();
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void setListening(boolean z) {
        Log.d(QS.TAG, "setListening " + z);
        this.mListening = !this.mControlPanelController.isUseControlCenter() && z;
        this.mFooter.setListening(z);
        this.mQSPanel.setListening(this.mListening, this.mQsExpanded);
        this.mContainer.setBrightnessListening(z);
        this.mContainer.getQuickQSPanel().setListening(z);
        this.mContainer.getQuickQSPanel().switchTileLayout();
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void setHeaderListening(boolean z) {
        if (!this.mControlPanelController.isUseControlCenter()) {
            this.mFooter.setListening(z);
            this.mContainer.getQuickQSPanel().setListening(z);
            this.mContainer.updateDataUsageInfo();
        }
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void setQsExpansion(float f, float f2) {
        this.mContainer.setExpansion(f);
        float f3 = 1.0f;
        float f4 = f - 1.0f;
        boolean z = true;
        boolean z2 = isKeyguardShowing() && !this.mShowCollapsedOnKeyguard;
        if (!this.mHeaderAnimating && !headerWillBeAnimating()) {
            View view = getView();
            if (z2) {
                f2 = ((float) this.mContainer.getMinHeight()) * f4;
            }
            view.setTranslationY(f2);
        }
        int height = getView().getHeight();
        if (f != this.mLastQSExpansion || this.mLastKeyguardAndExpanded != z2 || this.mLastViewHeight != height) {
            this.mLastQSExpansion = f;
            this.mLastKeyguardAndExpanded = z2;
            this.mLastViewHeight = height;
            boolean z3 = f == 1.0f;
            if (f != 0.0f) {
                z = false;
            }
            this.mQSPanelScrollView.getBottom();
            this.mHeader.getBottom();
            this.mHeader.getPaddingBottom();
            this.mContainer.getQuickQSPanel().switchTileLayout();
            QSFooter qSFooter = this.mFooter;
            if (!z2) {
                f3 = f;
            }
            qSFooter.setExpansion(f3);
            this.mQSPanel.getQsTileRevealController().setExpansion(f);
            this.mQSPanel.getTileLayout().setExpansion(f);
            if (z) {
                this.mQSPanelScrollView.setScrollY(0);
            }
            this.mQSDetail.setFullyExpanded(z3);
            if (!z3) {
                this.mQsBounds.top = (int) (-this.mQSPanelScrollView.getTranslationY());
                this.mQsBounds.right = this.mQSPanelScrollView.getWidth();
                this.mQsBounds.bottom = this.mQSPanelScrollView.getHeight();
            }
            updateQsBounds();
            QSAnimator qSAnimator = this.mQSAnimator;
            if (qSAnimator != null) {
                qSAnimator.setPosition(f);
            }
        }
    }

    private void updateQsBounds() {
        if (this.mLastQSExpansion == 1.0f) {
            this.mQsBounds.set(0, 0, this.mQSPanelScrollView.getWidth(), this.mQSPanelScrollView.getHeight());
        }
        this.mQSPanelScrollView.setClipBounds(this.mQsBounds);
    }

    private boolean headerWillBeAnimating() {
        if (this.mState != 1 || !this.mShowCollapsedOnKeyguard || isKeyguardShowing()) {
            return false;
        }
        return true;
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void animateHeaderSlidingIn(long j) {
        if (!this.mQsExpanded && getView().getTranslationY() != 0.0f) {
            this.mHeaderAnimating = true;
            this.mDelay = j;
            getView().getViewTreeObserver().addOnPreDrawListener(this.mStartHeaderSlidingIn);
        }
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void animateHeaderSlidingOut() {
        if (getView().getY() != ((float) (-this.mContainer.getMinHeight()))) {
            this.mHeaderAnimating = true;
            getView().animate().y((float) (-this.mContainer.getMinHeight())).setStartDelay(0).setDuration(300).setInterpolator(Interpolators.FAST_OUT_SLOW_IN).setListener(new AnimatorListenerAdapter() {
                /* class com.android.systemui.qs.QSFragment.AnonymousClass2 */

                public void onAnimationEnd(Animator animator) {
                    if (QSFragment.this.getView() != null) {
                        QSFragment.this.getView().animate().setListener(null);
                    }
                    QSFragment.this.mHeaderAnimating = false;
                    QSFragment.this.updateQsState();
                }
            }).start();
        }
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void setExpandClickListener(View.OnClickListener onClickListener) {
        this.mFooter.setExpandClickListener(onClickListener);
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void closeDetail() {
        this.mQSPanel.closeDetail();
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void notifyCustomizeChanged() {
        int i = 0;
        this.mQSPanelScrollView.setVisibility(!this.mQSCustomizer.isCustomizing() ? 0 : 4);
        QSFooter qSFooter = this.mFooter;
        if (this.mQSCustomizer.isCustomizing()) {
            i = 4;
        }
        qSFooter.setVisibility(i);
    }

    @Override // com.android.systemui.plugins.qs.QS
    public int getDesiredHeight() {
        if (this.mQSCustomizer.isCustomizing()) {
            return this.mQSCustomizer.getHeight();
        }
        if (this.mQSDetail.isShowing()) {
            return this.mQSDetail.getHeight();
        }
        return getView().getMeasuredHeight();
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void setHeightOverride(int i) {
        this.mContainer.setHeightOverride(i);
    }

    @Override // com.android.systemui.plugins.qs.QS
    public int getQsMinExpansionHeight() {
        return this.mContainer.getMinHeight();
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void hideImmediately() {
        getView().animate().cancel();
        finishAppearAnimation();
        getView().setY((float) (-this.mContainer.getMinHeight()));
    }

    @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
    public void onStateChanged(int i) {
        this.mState = i;
        boolean z = true;
        if (i != 1) {
            z = false;
        }
        setKeyguardShowing(z);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateQSDataUsage() {
        this.mUIExecutor.execute(new Runnable() {
            /* class com.android.systemui.qs.$$Lambda$QSFragment$qXyO0cBd93lSFDfKmvWOD2WZrs */

            public final void run() {
                QSFragment.this.lambda$updateQSDataUsage$3$QSFragment();
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$updateQSDataUsage$3 */
    public /* synthetic */ void lambda$updateQSDataUsage$3$QSFragment() {
        Context context = getContext();
        if (context != null) {
            this.mContainer.updateQSDataUsage(MiuiStatusBarManager.isShowFlowInfoForUser(context, -2));
        }
    }

    public void onDestroyView() {
        this.mResolver.unregisterContentObserver(this.mShowDataUsageObserver);
        super.onDestroyView();
    }

    public QSContainerImpl getQSContainer() {
        return this.mContainer;
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void setDetailAnimatedViews(View... viewArr) {
        this.mContainer.setDetailAnimatedViews(viewArr);
    }

    @Override // com.android.systemui.controlcenter.phone.ControlPanelController.UseControlPanelChangeListener
    public void onUseControlPanelChange(boolean z) {
        this.mContainer.setShowQSPanel(!z);
    }

    @Override // com.android.systemui.plugins.qs.QS
    public void animateAppearDisappear(final boolean z) {
        this.mAppeared = z;
        if (z) {
            setListening(true);
        }
        float f = 1.0f;
        ViewPropertyAnimator scaleX = getView().animate().setInterpolator(PanelAppearDisappearEvent.Companion.getINTERPOLATOR()).setDuration(450).alpha(this.mAppeared ? 1.0f : 0.0f).scaleX(this.mAppeared ? 1.0f : 0.8f);
        if (!this.mAppeared) {
            f = 0.8f;
        }
        scaleX.scaleY(f).setListener(new AnimatorListenerAdapter() {
            /* class com.android.systemui.qs.QSFragment.AnonymousClass5 */

            public void onAnimationEnd(Animator animator) {
                if (!z) {
                    QSFragment.this.setListening(false);
                }
                QSFragment.this.mHeaderAnimating = false;
            }
        }).start();
    }

    private void finishAppearAnimation() {
        float f = 1.0f;
        getView().setAlpha(this.mAppeared ? 1.0f : 0.0f);
        getView().setScaleX(this.mAppeared ? 1.0f : 0.8f);
        View view = getView();
        if (!this.mAppeared) {
            f = 0.8f;
        }
        view.setScaleY(f);
        this.mHeaderAnimating = false;
    }
}
