package com.android.systemui.qs;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.CustomizedUtils;
import com.android.systemui.Dependency;
import com.android.systemui.SystemUI;
import com.android.systemui.Util;
import com.android.systemui.miui.anim.AnimatorListenerWrapper;
import com.android.systemui.miui.statusbar.analytics.SystemUIStat;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.R;
import com.android.systemui.plugins.qs.DetailAdapter;
import com.android.systemui.plugins.qs.QS;
import com.android.systemui.qs.QSAnimation;
import com.android.systemui.statusbar.CommandQueue;
import java.util.List;
import miui.widget.SlidingButton;

public class QSDetail extends LinearLayout {
    private Animator.AnimatorListener mAnimInListener;
    private Animator.AnimatorListener mAnimOutListener;
    /* access modifiers changed from: private */
    public boolean mAnimatingOpen;
    private QSDetailClipper mClipper;
    /* access modifiers changed from: private */
    public boolean mClosingDetail;
    /* access modifiers changed from: private */
    public DetailAdapter mDetailAdapter;
    private View mDetailContainer;
    /* access modifiers changed from: private */
    public ViewGroup mDetailContent;
    protected TextView mDetailDoneButton;
    protected TextView mDetailSettingsButton;
    private final SparseArray<View> mDetailViews = new SparseArray<>();
    /* access modifiers changed from: private */
    public boolean mDonedClicked;
    private boolean mFullyExpanded;
    private final AnimatorListenerAdapter mHideGridContentWhenDone = new AnimatorListenerAdapter() {
        public void onAnimationCancel(Animator animator) {
            animator.removeListener(this);
            boolean unused = QSDetail.this.mAnimatingOpen = false;
            QSDetail.this.checkPendingAnimations();
        }

        public void onAnimationEnd(Animator animator) {
            boolean unused = QSDetail.this.mAnimatingOpen = false;
            QSDetail.this.checkPendingAnimations();
        }
    };
    private QS mQs;
    protected View mQsDetailHeader;
    protected SlidingButton mQsDetailHeaderSwitch;
    protected TextView mQsDetailHeaderTitle;
    /* access modifiers changed from: private */
    public QSPanel mQsPanel;
    protected QSPanelCallback mQsPanelCallback = new QSPanelCallback() {
        public void onToggleStateChanged(final boolean z) {
            QSDetail.this.post(new Runnable() {
                public void run() {
                    QSDetail qSDetail = QSDetail.this;
                    qSDetail.handleToggleStateChanged(z, qSDetail.mDetailAdapter != null && QSDetail.this.mDetailAdapter.getToggleEnabled());
                }
            });
        }

        public void onShowingDetail(final DetailAdapter detailAdapter, final int i, final int i2) {
            QSDetail.this.post(new Runnable() {
                public void run() {
                    QSDetail.this.handleShowingDetail(detailAdapter, i, i2, true);
                }
            });
        }

        public void onScanStateChanged(final boolean z) {
            QSDetail.this.post(new Runnable() {
                public void run() {
                    QSDetail.this.handleScanStateChanged(z);
                }
            });
        }
    };
    private boolean mScanState;
    /* access modifiers changed from: private */
    public boolean mSettingsClicked;
    /* access modifiers changed from: private */
    public boolean mSwitchClicked;
    private boolean mSwitchState;
    private final AnimatorListenerAdapter mTeardownDetailWhenDone;
    protected View mTopDivider;
    private boolean mTriggeredExpand;

    public interface QSPanelCallback {
        void onScanStateChanged(boolean z);

        void onShowingDetail(DetailAdapter detailAdapter, int i, int i2);

        void onToggleStateChanged(boolean z);
    }

    public void setHost(QSTileHost qSTileHost) {
    }

    public QSDetail(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        AnonymousClass6 r1 = new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animator) {
                QSDetail.this.mDetailContent.removeAllViews();
                QSDetail.this.setVisibility(4);
                boolean unused = QSDetail.this.mClosingDetail = false;
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
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        setClickable(false);
        this.mDetailContent = (ViewGroup) findViewById(16908290);
        this.mDetailSettingsButton = (TextView) findViewById(16908314);
        this.mDetailDoneButton = (TextView) findViewById(16908313);
        this.mDetailContainer = findViewById(R.id.qs_detail_container);
        this.mTopDivider = findViewById(R.id.top_divider);
        View findViewById = findViewById(R.id.qs_detail_header);
        this.mQsDetailHeader = findViewById;
        this.mQsDetailHeaderTitle = (TextView) findViewById.findViewById(16908310);
        this.mQsDetailHeaderSwitch = (SlidingButton) this.mQsDetailHeader.findViewById(16908311);
        updateDetailText();
        this.mClipper = new QSDetailClipper(this);
        this.mDetailDoneButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                QSDetail qSDetail = QSDetail.this;
                qSDetail.announceForAccessibility(qSDetail.mContext.getString(R.string.accessibility_desc_quick_settings));
                boolean unused = QSDetail.this.mDonedClicked = true;
                QSDetail.this.mQsPanel.closeDetail(false);
            }
        });
        updateDetailLayout();
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (motionEvent.getY() > ((float) getVisualBottom())) {
            return super.onTouchEvent(motionEvent);
        }
        return true;
    }

    public void setQsPanel(QSPanel qSPanel) {
        this.mQsPanel = qSPanel;
        qSPanel.setQSDetailCallback(this.mQsPanelCallback);
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
        this.mDetailDoneButton.setText(R.string.quick_settings_done);
        this.mDetailSettingsButton.setText(R.string.quick_settings_more_settings);
    }

    private void updateDetailLayout() {
        setPadding(getPaddingLeft(), CustomizedUtils.getNotchExpandedHeaderViewHeight(getContext(), getResources().getDimensionPixelSize(R.dimen.notch_expanded_header_height)), getPaddingRight(), getResources().getDimensionPixelOffset(R.dimen.qs_detail_margin_bottom));
        if (this.mDetailAdapter != null) {
            this.mDetailContainer.getLayoutParams().height = this.mDetailAdapter.getContainerHeight();
            this.mDetailContainer.requestLayout();
        }
    }

    public void handleShowingDetail(DetailAdapter detailAdapter, int i, int i2, boolean z) {
        Animator.AnimatorListener animatorListener;
        String str;
        boolean z2;
        String str2;
        boolean z3 = true;
        if (detailAdapter != null) {
            setupDetailHeader(detailAdapter);
            if (!z || this.mFullyExpanded) {
                this.mTriggeredExpand = false;
            } else {
                this.mTriggeredExpand = true;
                ((CommandQueue) SystemUI.getComponent(this.mContext, CommandQueue.class)).animateExpandSettingsPanel((String) null);
            }
        } else if (z && this.mTriggeredExpand) {
            ((CommandQueue) SystemUI.getComponent(this.mContext, CommandQueue.class)).animateCollapsePanels();
            this.mTriggeredExpand = false;
        }
        boolean z4 = (this.mDetailAdapter != null) != (detailAdapter != null);
        if (z4 || this.mDetailAdapter != detailAdapter) {
            if (detailAdapter != null) {
                this.mDetailContainer.getLayoutParams().height = detailAdapter.getContainerHeight();
                this.mDetailContainer.requestLayout();
                int metricsCategory = detailAdapter.getMetricsCategory();
                View createDetailView = detailAdapter.createDetailView(this.mContext, this.mDetailViews.get(metricsCategory), this.mDetailContent);
                if (createDetailView != null) {
                    setupDetailFooter(detailAdapter);
                    this.mDetailContent.removeAllViews();
                    this.mDetailContent.addView(createDetailView);
                    this.mDetailViews.put(metricsCategory, createDetailView);
                    MetricsLogger.visible(this.mContext, detailAdapter.getMetricsCategory());
                    announceForAccessibility(this.mContext.getString(R.string.accessibility_quick_settings_detail, new Object[]{detailAdapter.getTitle()}));
                    this.mDetailAdapter = detailAdapter;
                    animatorListener = this.mAnimInListener;
                    setVisibility(0);
                } else {
                    throw new IllegalStateException("Must return detail view");
                }
            } else {
                DetailAdapter detailAdapter2 = this.mDetailAdapter;
                if (detailAdapter2 != null) {
                    View view = this.mDetailViews.get(detailAdapter2.getMetricsCategory());
                    if (view == null || !(view instanceof QSDetailItems)) {
                        str = "";
                        z2 = false;
                    } else {
                        QSDetailItems qSDetailItems = (QSDetailItems) view;
                        z2 = qSDetailItems.isItemClicked();
                        qSDetailItems.setItemClicked(false);
                        str = qSDetailItems.getSuffix();
                    }
                    if (!TextUtils.isEmpty(str)) {
                        if (this.mDonedClicked) {
                            str2 = "done_btn";
                        } else if (this.mSettingsClicked) {
                            str2 = "settings_button";
                        } else {
                            str2 = (!"Wifi".equals(str) || !z2) ? "back_pressed" : "item_clicked";
                        }
                        ((SystemUIStat) Dependency.get(SystemUIStat.class)).handleQSDetailExitEvent(str, z2, this.mSwitchClicked, str2);
                    }
                    MetricsLogger.hidden(this.mContext, this.mDetailAdapter.getMetricsCategory());
                    resetDataTrackStates();
                }
                this.mDetailAdapter = null;
                animatorListener = this.mAnimOutListener;
                this.mQsPanelCallback.onScanStateChanged(false);
            }
            sendAccessibilityEvent(32);
            $$Lambda$QSDetail$Bz3GXMHJw3LIFP8claBtzVh6I r4 = new Runnable(i, i2, z4, animatorListener) {
                public final /* synthetic */ int f$1;
                public final /* synthetic */ int f$2;
                public final /* synthetic */ boolean f$3;
                public final /* synthetic */ Animator.AnimatorListener f$4;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                    this.f$4 = r5;
                }

                public final void run() {
                    QSDetail.this.lambda$handleShowingDetail$0$QSDetail(this.f$1, this.f$2, this.f$3, this.f$4);
                }
            };
            if (detailAdapter == null) {
                z3 = false;
            }
            Util.runAfterGlobalLayoutOrNot(this, r4, z3);
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$handleShowingDetail$0 */
    public /* synthetic */ void lambda$handleShowingDetail$0$QSDetail(int i, int i2, boolean z, Animator.AnimatorListener animatorListener) {
        animateDetailVisibleDiff(i, i2, z, animatorListener);
        this.mQs.notifyCustomizeChanged();
    }

    private void resetDataTrackStates() {
        this.mSwitchClicked = false;
        this.mSettingsClicked = false;
        this.mDonedClicked = false;
    }

    /* access modifiers changed from: protected */
    public void animateDetailVisibleDiff(int i, int i2, boolean z, Animator.AnimatorListener animatorListener) {
        if (z) {
            boolean z2 = true;
            boolean z3 = this.mDetailAdapter != null;
            this.mAnimatingOpen = z3;
            if (!this.mFullyExpanded && !z3) {
                z2 = false;
            }
            if (z2) {
                setAlpha(1.0f);
                this.mClipper.animateCircularClip(i, i2, this.mAnimatingOpen, animatorListener);
                return;
            }
            setAlpha(0.0f);
            animatorListener.onAnimationEnd((Animator) null);
        }
    }

    /* access modifiers changed from: protected */
    public void setupDetailFooter(final DetailAdapter detailAdapter) {
        final Intent settingsIntent = detailAdapter.getSettingsIntent();
        this.mDetailSettingsButton.setVisibility(settingsIntent != null ? 0 : 8);
        this.mDetailSettingsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                boolean unused = QSDetail.this.mSettingsClicked = true;
                MetricsLogger.action(QSDetail.this.mContext, 929, detailAdapter.getMetricsCategory());
                ((ActivityStarter) Dependency.get(ActivityStarter.class)).postStartActivityDismissingKeyguard(settingsIntent, 0);
            }
        });
    }

    /* access modifiers changed from: protected */
    public void setupDetailHeader(final DetailAdapter detailAdapter) {
        this.mQsDetailHeaderTitle.setText(detailAdapter.getTitle());
        Boolean toggleState = detailAdapter.getToggleState();
        if (toggleState == null) {
            this.mQsDetailHeaderSwitch.setVisibility(4);
            return;
        }
        this.mQsDetailHeaderSwitch.setVisibility(0);
        handleToggleStateChanged(toggleState.booleanValue(), detailAdapter.getToggleEnabled());
        this.mQsDetailHeaderSwitch.setOnPerformCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                boolean unused = QSDetail.this.mSwitchClicked = true;
                detailAdapter.setToggleState(z);
            }
        });
    }

    /* access modifiers changed from: private */
    public void handleToggleStateChanged(boolean z, boolean z2) {
        this.mSwitchState = z;
        if (!this.mAnimatingOpen) {
            this.mQsDetailHeaderSwitch.setChecked(z);
            this.mQsDetailHeaderSwitch.setEnabled(z2);
        }
    }

    /* access modifiers changed from: private */
    public void handleScanStateChanged(boolean z) {
        if (this.mScanState != z) {
            this.mScanState = z;
        }
    }

    /* access modifiers changed from: private */
    public void checkPendingAnimations() {
        boolean z = this.mSwitchState;
        DetailAdapter detailAdapter = this.mDetailAdapter;
        handleToggleStateChanged(z, detailAdapter != null && detailAdapter.getToggleEnabled());
    }

    public void setAnimatedViews(List<View> list) {
        if (list != null && list.size() != 0) {
            this.mAnimInListener = AnimatorListenerWrapper.of(this.mHideGridContentWhenDone, new QSAnimation.QsHideBeforeAnimatorListener((View[]) list.toArray(new View[0])));
            this.mAnimOutListener = AnimatorListenerWrapper.of(this.mTeardownDetailWhenDone, new QSAnimation.QsShowBeforeAnimatorListener((View[]) list.toArray(new View[0])));
        }
    }

    public void setQs(QS qs) {
        this.mQs = qs;
    }

    public int getVisualBottom() {
        return getTop() + getPaddingTop() + this.mDetailContainer.getMeasuredHeight();
    }
}
