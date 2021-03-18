package com.android.systemui.qs;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.UiEventLogger;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0021R$string;
import com.android.systemui.Dependency;
import com.android.systemui.controlcenter.phone.widget.AnimatorListenerWrapper;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.qs.DetailAdapter;
import com.android.systemui.qs.MiuiQSDetail;
import com.android.systemui.qs.QSAnimation;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.phone.NotificationsQuickSettingsContainer;
import java.util.List;
import miui.widget.SlidingButton;

public class MiuiQSDetail extends LinearLayout {
    private Animator.AnimatorListener mAnimInListener;
    private Animator.AnimatorListener mAnimOutListener;
    private boolean mAnimatingOpen;
    private QSDetailClipper mClipper;
    private boolean mClosingDetail;
    private DetailAdapter mDetailAdapter;
    private View mDetailContainer;
    private ViewGroup mDetailContent;
    protected TextView mDetailDoneButton;
    protected TextView mDetailSettingsButton;
    private final SparseArray<View> mDetailViews = new SparseArray<>();
    private View mFooter;
    private boolean mFullyExpanded;
    private MiuiNotificationShadeHeader mHeader;
    private final AnimatorListenerAdapter mHideGridContentWhenDone = new AnimatorListenerAdapter() {
        /* class com.android.systemui.qs.MiuiQSDetail.AnonymousClass2 */

        public void onAnimationCancel(Animator animator) {
            animator.removeListener(this);
            MiuiQSDetail.this.mAnimatingOpen = false;
            MiuiQSDetail.this.checkPendingAnimations();
        }

        public void onAnimationEnd(Animator animator) {
            MiuiQSDetail.this.mAnimatingOpen = false;
            MiuiQSDetail.this.checkPendingAnimations();
        }
    };
    private float mInitY;
    private boolean mIsShowingDetail;
    private NotificationsQuickSettingsContainer mNotifQsContainer;
    private int mOpenX;
    private int mOpenY;
    protected View mQsDetailHeader;
    private SlidingButton mQsDetailHeaderSwitch;
    private ViewStub mQsDetailHeaderSwitchStub;
    protected TextView mQsDetailHeaderTitle;
    private QSPanel mQsPanel;
    protected Callback mQsPanelCallback = new Callback() {
        /* class com.android.systemui.qs.MiuiQSDetail.AnonymousClass1 */

        /* access modifiers changed from: private */
        /* renamed from: lambda$onToggleStateChanged$0 */
        public /* synthetic */ void lambda$onToggleStateChanged$0$MiuiQSDetail$1(boolean z) {
            MiuiQSDetail miuiQSDetail = MiuiQSDetail.this;
            miuiQSDetail.handleToggleStateChanged(z, miuiQSDetail.mDetailAdapter != null && MiuiQSDetail.this.mDetailAdapter.getToggleEnabled());
        }

        @Override // com.android.systemui.qs.MiuiQSDetail.Callback
        public void onToggleStateChanged(boolean z) {
            MiuiQSDetail.this.post(new Runnable(z) {
                /* class com.android.systemui.qs.$$Lambda$MiuiQSDetail$1$omLPU6ca8j87lMH45TyZrYZWl50 */
                public final /* synthetic */ boolean f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    MiuiQSDetail.AnonymousClass1.this.lambda$onToggleStateChanged$0$MiuiQSDetail$1(this.f$1);
                }
            });
        }

        @Override // com.android.systemui.qs.MiuiQSDetail.Callback
        public void onShowingDetail(DetailAdapter detailAdapter, int i, int i2) {
            MiuiQSDetail.this.post(new Runnable(detailAdapter, i, i2) {
                /* class com.android.systemui.qs.$$Lambda$MiuiQSDetail$1$_HWXuQHZgY6THwzAXMxeIaok */
                public final /* synthetic */ DetailAdapter f$1;
                public final /* synthetic */ int f$2;
                public final /* synthetic */ int f$3;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                }

                public final void run() {
                    MiuiQSDetail.AnonymousClass1.this.lambda$onShowingDetail$1$MiuiQSDetail$1(this.f$1, this.f$2, this.f$3);
                }
            });
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onShowingDetail$1 */
        public /* synthetic */ void lambda$onShowingDetail$1$MiuiQSDetail$1(DetailAdapter detailAdapter, int i, int i2) {
            if (MiuiQSDetail.this.isAttachedToWindow()) {
                MiuiQSDetail.this.handleShowingDetail(detailAdapter, i, i2, false);
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onScanStateChanged$2 */
        public /* synthetic */ void lambda$onScanStateChanged$2$MiuiQSDetail$1(boolean z) {
            MiuiQSDetail.this.handleScanStateChanged(z);
        }

        @Override // com.android.systemui.qs.MiuiQSDetail.Callback
        public void onScanStateChanged(boolean z) {
            MiuiQSDetail.this.post(new Runnable(z) {
                /* class com.android.systemui.qs.$$Lambda$MiuiQSDetail$1$3FsPnbIjHjXwyg5W3S_hQtzzuqQ */
                public final /* synthetic */ boolean f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    MiuiQSDetail.AnonymousClass1.this.lambda$onScanStateChanged$2$MiuiQSDetail$1(this.f$1);
                }
            });
        }
    };
    private QuickQSPanel mQuickQSPanel;
    private boolean mScanState;
    private boolean mSwitchState;
    private final AnimatorListenerAdapter mTeardownDetailWhenDone;
    private boolean mTriggeredExpand;
    private final UiEventLogger mUiEventLogger = QSEvents.INSTANCE.getQsUiEventsLogger();

    public interface Callback {
        void onScanStateChanged(boolean z);

        void onShowingDetail(DetailAdapter detailAdapter, int i, int i2);

        void onToggleStateChanged(boolean z);
    }

    public void setHost(QSTileHost qSTileHost) {
    }

    public MiuiQSDetail(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        AnonymousClass3 r1 = new AnimatorListenerAdapter() {
            /* class com.android.systemui.qs.MiuiQSDetail.AnonymousClass3 */

            public void onAnimationEnd(Animator animator) {
                MiuiQSDetail.this.mDetailContent.removeAllViews();
                MiuiQSDetail.this.setVisibility(4);
                MiuiQSDetail.this.mClosingDetail = false;
            }
        };
        this.mTeardownDetailWhenDone = r1;
        this.mAnimInListener = this.mHideGridContentWhenDone;
        this.mAnimOutListener = r1;
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        updateDetailLayout();
        for (int i = 0; i < this.mDetailViews.size(); i++) {
            this.mDetailViews.valueAt(i).dispatchConfigurationChanged(configuration);
        }
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mDetailContainer = findViewById(C0015R$id.qs_detail_container);
        this.mDetailContent = (ViewGroup) findViewById(16908290);
        this.mDetailSettingsButton = (TextView) findViewById(16908314);
        this.mDetailDoneButton = (TextView) findViewById(16908313);
        View findViewById = findViewById(C0015R$id.qs_detail_header);
        this.mQsDetailHeader = findViewById;
        this.mQsDetailHeaderTitle = (TextView) findViewById.findViewById(16908310);
        this.mQsDetailHeaderSwitchStub = (ViewStub) this.mQsDetailHeader.findViewById(C0015R$id.toggle_stub);
        ImageView imageView = (ImageView) findViewById(C0015R$id.qs_detail_header_progress);
        updateDetailText();
        this.mClipper = new QSDetailClipper(this);
        this.mDetailDoneButton.setOnClickListener(new View.OnClickListener() {
            /* class com.android.systemui.qs.$$Lambda$MiuiQSDetail$2fBnEkbvnzxQmaLsoeOX3XlfwcA */

            public final void onClick(View view) {
                MiuiQSDetail.this.lambda$onFinishInflate$0$MiuiQSDetail(view);
            }
        });
        updateDetailLayout();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onFinishInflate$0 */
    public /* synthetic */ void lambda$onFinishInflate$0$MiuiQSDetail(View view) {
        announceForAccessibility(((LinearLayout) this).mContext.getString(C0021R$string.accessibility_desc_quick_settings));
        this.mQsPanel.closeDetail();
    }

    public void setQsPanel(QSPanel qSPanel, MiuiNotificationShadeHeader miuiNotificationShadeHeader, QuickQSPanel quickQSPanel, View view) {
        this.mQsPanel = qSPanel;
        this.mHeader = miuiNotificationShadeHeader;
        this.mFooter = view;
        this.mQuickQSPanel = quickQSPanel;
        quickQSPanel.setCallback(this.mQsPanelCallback);
        this.mQsPanel.setCallback(this.mQsPanelCallback);
    }

    public boolean isShowingDetail() {
        return this.mDetailAdapter != null;
    }

    public void setFullyExpanded(boolean z) {
        this.mFullyExpanded = z;
    }

    public void setExpanded(boolean z) {
        if (!z) {
            this.mTriggeredExpand = false;
        }
    }

    private void updateDetailText() {
        this.mDetailDoneButton.setText(C0021R$string.quick_settings_done);
        this.mDetailSettingsButton.setText(C0021R$string.quick_settings_more_settings);
    }

    public void handleShowingDetail(DetailAdapter detailAdapter, int i, int i2, boolean z) {
        Animator.AnimatorListener animatorListener;
        boolean z2 = detailAdapter != null;
        this.mIsShowingDetail = z2;
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) this.mDetailContainer.getLayoutParams();
        int i3 = i + marginLayoutParams.leftMargin;
        int paddingTop = i2 + getPaddingTop();
        setClickable(z2);
        if (z2) {
            marginLayoutParams.height = detailAdapter.getContainerHeight();
            this.mDetailContainer.setLayoutParams(marginLayoutParams);
            setupDetailHeader(detailAdapter);
            if (!z || this.mFullyExpanded) {
                this.mTriggeredExpand = false;
            } else {
                this.mTriggeredExpand = true;
                ((CommandQueue) Dependency.get(CommandQueue.class)).animateExpandSettingsPanel(null);
            }
            this.mOpenX = i3;
            this.mOpenY = paddingTop;
        } else {
            i3 = this.mOpenX;
            paddingTop = this.mOpenY;
            if (z && this.mTriggeredExpand) {
                ((CommandQueue) Dependency.get(CommandQueue.class)).animateCollapsePanels();
                this.mTriggeredExpand = false;
            }
        }
        boolean z3 = (this.mDetailAdapter != null) != (detailAdapter != null);
        if (z3 || this.mDetailAdapter != detailAdapter) {
            if (detailAdapter != null) {
                int metricsCategory = detailAdapter.getMetricsCategory();
                View createDetailView = detailAdapter.createDetailView(((LinearLayout) this).mContext, this.mDetailViews.get(metricsCategory), this.mDetailContent);
                if (createDetailView != null) {
                    setupDetailFooter(detailAdapter);
                    this.mDetailContent.removeAllViews();
                    this.mDetailContent.addView(createDetailView);
                    this.mDetailViews.put(metricsCategory, createDetailView);
                    ((MetricsLogger) Dependency.get(MetricsLogger.class)).visible(detailAdapter.getMetricsCategory());
                    this.mUiEventLogger.log(detailAdapter.openDetailEvent());
                    announceForAccessibility(((LinearLayout) this).mContext.getString(C0021R$string.accessibility_quick_settings_detail, detailAdapter.getTitle()));
                    this.mDetailAdapter = detailAdapter;
                    animatorListener = this.mAnimInListener;
                    setVisibility(0);
                } else {
                    throw new IllegalStateException("Must return detail view");
                }
            } else {
                if (this.mDetailAdapter != null) {
                    ((MetricsLogger) Dependency.get(MetricsLogger.class)).hidden(this.mDetailAdapter.getMetricsCategory());
                    this.mUiEventLogger.log(this.mDetailAdapter.closeDetailEvent());
                }
                this.mDetailAdapter = null;
                animatorListener = this.mAnimOutListener;
                this.mHeader.setVisibility(0);
                this.mFooter.setVisibility(0);
                this.mQsPanel.setGridContentVisibility(true);
                this.mQsPanelCallback.onScanStateChanged(false);
            }
            sendAccessibilityEvent(32);
            animateDetailVisibleDiff(i3, paddingTop, z3, animatorListener);
            this.mNotifQsContainer.setCustomizerAnimating(z2);
            this.mNotifQsContainer.setCustomizerShowing(z2);
        }
    }

    /* access modifiers changed from: protected */
    public void animateDetailVisibleDiff(int i, int i2, boolean z, Animator.AnimatorListener animatorListener) {
        if (z) {
            boolean z2 = true;
            this.mAnimatingOpen = this.mDetailAdapter != null;
            if (this.mFullyExpanded || this.mDetailAdapter != null) {
                setAlpha(1.0f);
                QSDetailClipper qSDetailClipper = this.mClipper;
                if (this.mDetailAdapter == null) {
                    z2 = false;
                }
                qSDetailClipper.animateCircularClip(i, i2, z2, animatorListener);
                return;
            }
            animate().alpha(0.0f).setDuration(300).setListener(animatorListener).start();
        }
    }

    /* access modifiers changed from: protected */
    public void setupDetailFooter(DetailAdapter detailAdapter) {
        Intent settingsIntent = detailAdapter.getSettingsIntent();
        this.mDetailSettingsButton.setVisibility(settingsIntent != null ? 0 : 8);
        this.mDetailSettingsButton.setOnClickListener(new View.OnClickListener(detailAdapter, settingsIntent) {
            /* class com.android.systemui.qs.$$Lambda$MiuiQSDetail$xw5zXA9qdEPWxv2ak7yP3xz0T5E */
            public final /* synthetic */ DetailAdapter f$1;
            public final /* synthetic */ Intent f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void onClick(View view) {
                MiuiQSDetail.this.lambda$setupDetailFooter$1$MiuiQSDetail(this.f$1, this.f$2, view);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$setupDetailFooter$1 */
    public /* synthetic */ void lambda$setupDetailFooter$1$MiuiQSDetail(DetailAdapter detailAdapter, Intent intent, View view) {
        ((MetricsLogger) Dependency.get(MetricsLogger.class)).action(929, detailAdapter.getMetricsCategory());
        this.mUiEventLogger.log(detailAdapter.moreSettingsEvent());
        ((ActivityStarter) Dependency.get(ActivityStarter.class)).postStartActivityDismissingKeyguard(intent, 0);
    }

    /* access modifiers changed from: protected */
    public void setupDetailHeader(DetailAdapter detailAdapter) {
        this.mQsDetailHeaderTitle.setText(detailAdapter.getTitle());
        Boolean toggleState = detailAdapter.getToggleState();
        if (toggleState == null) {
            SlidingButton slidingButton = this.mQsDetailHeaderSwitch;
            if (slidingButton != null) {
                slidingButton.setVisibility(4);
                this.mQsDetailHeaderSwitch.setClickable(false);
                return;
            }
            return;
        }
        if (this.mQsDetailHeaderSwitch == null) {
            this.mQsDetailHeaderSwitch = this.mQsDetailHeaderSwitchStub.inflate();
        }
        this.mQsDetailHeaderSwitch.setVisibility(0);
        this.mQsDetailHeaderSwitch.setClickable(true);
        handleToggleStateChanged(toggleState.booleanValue(), detailAdapter.getToggleEnabled());
        this.mQsDetailHeaderSwitch.setOnPerformCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            /* class com.android.systemui.qs.$$Lambda$MiuiQSDetail$AdNHpUBZdRe21F6S_5YM97n9iFM */

            public final void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                MiuiQSDetail.lambda$setupDetailHeader$2(DetailAdapter.this, compoundButton, z);
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleToggleStateChanged(boolean z, boolean z2) {
        this.mSwitchState = z;
        if (!this.mAnimatingOpen) {
            SlidingButton slidingButton = this.mQsDetailHeaderSwitch;
            if (slidingButton != null) {
                slidingButton.setChecked(z);
            }
            SlidingButton slidingButton2 = this.mQsDetailHeaderSwitch;
            if (slidingButton2 != null) {
                slidingButton2.setEnabled(z2);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleScanStateChanged(boolean z) {
        if (this.mScanState != z) {
            this.mScanState = z;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void checkPendingAnimations() {
        boolean z = this.mSwitchState;
        DetailAdapter detailAdapter = this.mDetailAdapter;
        handleToggleStateChanged(z, detailAdapter != null && detailAdapter.getToggleEnabled());
    }

    public void setMargins(int i, int i2) {
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) this.mDetailContainer.getLayoutParams();
        marginLayoutParams.rightMargin = i;
        marginLayoutParams.leftMargin = i2;
        this.mDetailContainer.setLayoutParams(marginLayoutParams);
    }

    public void setAnimatedViews(List<View> list) {
        if (list != null && list.size() != 0) {
            this.mAnimInListener = AnimatorListenerWrapper.of(this.mHideGridContentWhenDone, new QSAnimation.QsHideBeforeAnimatorListener((View[]) list.toArray(new View[0])));
            this.mAnimOutListener = AnimatorListenerWrapper.of(this.mTeardownDetailWhenDone, new QSAnimation.QsShowBeforeAnimatorListener((View[]) list.toArray(new View[0])));
        }
    }

    public boolean isShowing() {
        return this.mIsShowingDetail;
    }

    public void setContainer(NotificationsQuickSettingsContainer notificationsQuickSettingsContainer) {
        this.mNotifQsContainer = notificationsQuickSettingsContainer;
    }

    public void updateHeaderHeight(int i) {
        setPadding(getPaddingLeft(), i, getPaddingRight(), getContext().getResources().getDimensionPixelOffset(C0012R$dimen.qs_detail_margin_bottom));
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (motionEvent.getActionMasked() == 0) {
            this.mInitY = motionEvent.getY();
        }
        if (this.mInitY < ((float) getPaddingTop())) {
            return false;
        }
        return super.onTouchEvent(motionEvent);
    }

    private void updateDetailLayout() {
        if (this.mDetailAdapter != null) {
            this.mDetailContainer.getLayoutParams().height = this.mDetailAdapter.getContainerHeight();
            this.mDetailContainer.requestLayout();
        }
    }
}
