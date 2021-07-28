package com.android.systemui.controlcenter.phone.detail;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Outline;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewParent;
import android.view.WindowInsets;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.TextView;
import codeinjection.CodeInjection;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.C0011R$color;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0021R$string;
import com.android.systemui.C0022R$style;
import com.android.systemui.Dependency;
import com.android.systemui.controlcenter.phone.ControlCenterPanelView;
import com.android.systemui.controlcenter.phone.controls.MiPlayPluginManager;
import com.android.systemui.controlcenter.phone.detail.QSControlDetail;
import com.android.systemui.controlcenter.policy.ControlCenterActivityStarter;
import com.android.systemui.controlcenter.qs.tileview.QSBigTileView;
import com.android.systemui.controlcenter.utils.ControlCenterUtils;
import com.android.systemui.controlcenter.utils.CpuBoostUtil;
import com.android.systemui.plugins.PluginListener;
import com.android.systemui.plugins.miui.controls.MiPlayPlugin;
import com.android.systemui.plugins.qs.DetailAdapter;
import com.android.systemui.qs.MiuiQSDetailItems;
import com.android.systemui.qs.miplay.MiPlayDetailAdapter;
import com.miui.systemui.DeviceConfig;
import com.miui.systemui.analytics.SystemUIStat;
import com.miui.systemui.anim.PhysicBasedInterpolator;
import java.util.Collection;
import miuix.animation.Folme;
import miuix.animation.IStateStyle;
import miuix.animation.base.AnimConfig;
import miuix.animation.controller.AnimState;
import miuix.animation.listener.TransitionListener;
import miuix.animation.listener.UpdateInfo;
import miuix.animation.property.ViewProperty;
import miuix.animation.utils.EaseManager;
import miuix.slidingwidget.widget.SlidingButton;

public class QSControlDetail extends FrameLayout {
    private float detailCornerRadius;
    protected IStateStyle mAnim;
    protected Runnable mAnimateHideRunnable = new Runnable() {
        /* class com.android.systemui.controlcenter.phone.detail.QSControlDetail.AnonymousClass5 */

        public void run() {
            if (DeviceConfig.isLowGpuDevice()) {
                QSControlDetail.this.animateHideDetailAndTileOnLowEnd();
            } else {
                QSControlDetail.this.animateHideDetailAndTile();
            }
        }
    };
    protected Runnable mAnimateShowRunnable = new Runnable() {
        /* class com.android.systemui.controlcenter.phone.detail.QSControlDetail.AnonymousClass4 */

        public void run() {
            if (DeviceConfig.isLowGpuDevice()) {
                Log.d("QSDetail", "showing on low end");
                QSControlDetail.this.animateShowDetailAndTileOnLowEnd();
                return;
            }
            QSControlDetail.this.animateShowDetailAndTile();
        }
    };
    private Context mContext;
    private int mCurrentDetailIndex;
    private DetailAdapter mDetailAdapter;
    private View mDetailContainer;
    private ViewGroup mDetailContent;
    protected TextView mDetailSettingsButton;
    private final SparseArray<View> mDetailViews = new SparseArray<>();
    private boolean mDonedClicked;
    protected View mFromView;
    protected int[] mFromViewFrame = new int[4];
    protected int[] mFromViewLocation = new int[4];
    protected boolean mIsAnimating;
    private int mOrientation;
    protected View mQsDetailHeader;
    protected SlidingButton mQsDetailHeaderSwitch;
    protected TextView mQsDetailHeaderTitle;
    private ControlCenterPanelView mQsPanel;
    protected QSPanelCallback mQsPanelCallback = new QSPanelCallback() {
        /* class com.android.systemui.controlcenter.phone.detail.QSControlDetail.AnonymousClass8 */

        @Override // com.android.systemui.controlcenter.phone.detail.QSControlDetail.QSPanelCallback
        public void onToggleStateChanged(final boolean z) {
            QSControlDetail.this.post(new Runnable() {
                /* class com.android.systemui.controlcenter.phone.detail.QSControlDetail.AnonymousClass8.AnonymousClass1 */

                public void run() {
                    QSControlDetail qSControlDetail = QSControlDetail.this;
                    qSControlDetail.handleToggleStateChanged(z, qSControlDetail.mDetailAdapter != null && QSControlDetail.this.mDetailAdapter.getToggleEnabled());
                }
            });
        }

        @Override // com.android.systemui.controlcenter.phone.detail.QSControlDetail.QSPanelCallback
        public void onShowingDetail(DetailAdapter detailAdapter, View view, View view2) {
            if (DeviceConfig.isMiddleGpuDevice()) {
                CpuBoostUtil.getInstance().boostCpuToMax(2000);
            }
            if (detailAdapter != null) {
                QSControlDetail.this.mShowingMiPlayDetail = detailAdapter instanceof MiPlayDetailAdapter;
            }
            QSControlDetail.this.handleShowingDetail(detailAdapter, view, view2);
            QSControlDetail qSControlDetail = QSControlDetail.this;
            if (qSControlDetail.mShowingMiPlayDetail) {
                View view3 = (View) qSControlDetail.mDetailViews.get(1168);
                boolean z = detailAdapter != null;
                MiPlayPluginManager miPlayPluginManager = (MiPlayPluginManager) Dependency.get(MiPlayPluginManager.class);
                if (z) {
                    miPlayPluginManager.showMiPlayDetailView(view3, MiPlayPlugin.REF_CONTROLCENTER);
                } else {
                    miPlayPluginManager.hideMiPlayDetailView(view3);
                }
            }
            if (detailAdapter == null) {
                QSControlDetail.this.post(new Runnable() {
                    /* class com.android.systemui.controlcenter.phone.detail.$$Lambda$QSControlDetail$8$mT3TXj0bjn0sWYsRuY9rdzTsXvM */

                    public final void run() {
                        QSControlDetail.AnonymousClass8.this.lambda$onShowingDetail$0$QSControlDetail$8();
                    }
                });
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onShowingDetail$0 */
        public /* synthetic */ void lambda$onShowingDetail$0$QSControlDetail$8() {
            QSControlDetail.this.mShowingMiPlayDetail = false;
        }

        @Override // com.android.systemui.controlcenter.phone.detail.QSControlDetail.QSPanelCallback
        public void onScanStateChanged(final boolean z) {
            QSControlDetail.this.post(new Runnable() {
                /* class com.android.systemui.controlcenter.phone.detail.QSControlDetail.AnonymousClass8.AnonymousClass2 */

                public void run() {
                    QSControlDetail.this.handleScanStateChanged(z);
                }
            });
        }
    };
    private boolean mScanState;
    private boolean mSettingsClicked;
    protected boolean mShowingMiPlayDetail;
    private String mSuffix;
    private boolean mSwitchClicked;
    private boolean mSwitchEnabled;
    private boolean mSwitchState;
    protected View mToView;
    protected int[] mToViewFrame = new int[4];
    protected int[] mToViewLocation = new int[4];
    protected View mTranslateView;
    private int mWifiBtDetailHeight;
    private PluginListener<MiPlayPlugin> pluginListener = new PluginListener<MiPlayPlugin>() {
        /* class com.android.systemui.controlcenter.phone.detail.QSControlDetail.AnonymousClass1 */

        public void onPluginConnected(MiPlayPlugin miPlayPlugin, Context context) {
        }

        public void onPluginDisconnected(MiPlayPlugin miPlayPlugin) {
            QSControlDetail.this.mDetailViews.remove(1168);
        }
    };

    public interface QSPanelCallback {
        void onScanStateChanged(boolean z);

        void onShowingDetail(DetailAdapter detailAdapter, View view, View view2);

        void onToggleStateChanged(boolean z);
    }

    public QSControlDetail(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mContext = context;
        this.mWifiBtDetailHeight = context.getResources().getDimensionPixelSize(C0012R$dimen.qs_control_detail_wifi_bt_height);
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        int i;
        int i2;
        super.onConfigurationChanged(configuration);
        int i3 = this.mOrientation;
        int i4 = configuration.orientation;
        if (i3 != i4) {
            this.mOrientation = i4;
            if (i4 == 1) {
                i = 0;
                i2 = 0;
            } else if (getLayoutDirection() == 0) {
                i = this.mContext.getResources().getDimensionPixelSize(C0012R$dimen.qs_control_width_land) + this.mContext.getResources().getDimensionPixelSize(C0012R$dimen.qs_control_land_tiles_margin_middle);
                i2 = 0;
            } else {
                i2 = this.mContext.getResources().getDimensionPixelSize(C0012R$dimen.qs_control_width_land) + this.mContext.getResources().getDimensionPixelSize(C0012R$dimen.qs_control_land_tiles_margin_middle);
                i = 0;
            }
            setPadding(i, 0, i2, 0);
        }
        this.mWifiBtDetailHeight = this.mContext.getResources().getDimensionPixelSize(C0012R$dimen.qs_control_detail_wifi_bt_height);
        updateDetailLayout();
    }

    public void updateResources() {
        setBackgroundColor(this.mContext.getColor(C0011R$color.qs_control_detail_layout_bg_color));
        updateBackground();
        this.mQsDetailHeaderTitle.setTextAppearance(C0022R$style.TextAppearance_QSControl_DetailHeader);
        this.mDetailSettingsButton.setTextAppearance(C0022R$style.TextAppearance_QSControl_DetailMoreButton);
        this.mDetailSettingsButton.setBackground(this.mContext.getDrawable(C0013R$drawable.qs_control_detail_more_button_bg));
        this.mDetailViews.clear();
    }

    private void updateBackground() {
        Drawable smoothRoundDrawable = ControlCenterUtils.getSmoothRoundDrawable(this.mContext, C0013R$drawable.qs_control_detail_bg);
        if (smoothRoundDrawable != null) {
            this.mDetailContainer.setBackground(smoothRoundDrawable);
        }
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        setClickable(false);
        this.mDetailContent = (ViewGroup) findViewById(16908290);
        TextView textView = (TextView) findViewById(C0015R$id.more_button);
        this.mDetailSettingsButton = textView;
        ControlCenterUtils.createButtonFolmeTouchStyle(textView);
        View findViewById = findViewById(C0015R$id.qs_detail_container);
        this.mDetailContainer = findViewById;
        findViewById.setClickable(true);
        updateBackground();
        View findViewById2 = findViewById(C0015R$id.qs_control_detail_header);
        this.mQsDetailHeader = findViewById2;
        this.mQsDetailHeaderTitle = (TextView) findViewById2.findViewById(16908310);
        SlidingButton slidingButton = (SlidingButton) this.mQsDetailHeader.findViewById(16908311);
        this.mQsDetailHeaderSwitch = slidingButton;
        slidingButton.setFocusable(true);
        this.mQsDetailHeaderSwitch.setFocusableInTouchMode(true);
        this.mQsDetailHeaderSwitch.setImportantForAccessibility(1);
        this.mQsDetailHeaderSwitch.setContentDescription(this.mContext.getResources().getString(C0021R$string.accessibility_detail_switch));
        this.detailCornerRadius = this.mContext.getResources().getDimension(C0012R$dimen.qs_control_corner_general_radius);
        this.mDetailContainer.setClipToOutline(true);
        this.mDetailContainer.setOutlineProvider(new ViewOutlineProvider() {
            /* class com.android.systemui.controlcenter.phone.detail.QSControlDetail.AnonymousClass2 */

            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), QSControlDetail.this.detailCornerRadius);
            }
        });
        updateDetailText();
        setOnClickListener(new View.OnClickListener() {
            /* class com.android.systemui.controlcenter.phone.detail.QSControlDetail.AnonymousClass3 */

            public void onClick(View view) {
                QSControlDetail qSControlDetail = QSControlDetail.this;
                qSControlDetail.announceForAccessibility(qSControlDetail.mContext.getString(C0021R$string.accessibility_desc_quick_settings));
                QSControlDetail.this.mDonedClicked = true;
                QSControlDetail.this.mQsPanel.closeDetail(false);
            }
        });
        updateDetailLayout();
        this.mAnim = Folme.useValue(this.mDetailContent);
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        ((MiPlayPluginManager) Dependency.get(MiPlayPluginManager.class)).addExtraListener(this.pluginListener);
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ((MiPlayPluginManager) Dependency.get(MiPlayPluginManager.class)).removeExtraListener(this.pluginListener);
    }

    public void setQsPanel(ControlCenterPanelView controlCenterPanelView) {
        this.mQsPanel = controlCenterPanelView;
        controlCenterPanelView.setQSDetailCallback(this.mQsPanelCallback);
    }

    public boolean isShowingDetail() {
        return this.mDetailAdapter != null;
    }

    private void updateDetailText() {
        this.mDetailSettingsButton.setText(C0021R$string.quick_settings_more_settings);
    }

    private void updateDetailLayout() {
        updateContainerHeight(this.mSuffix);
    }

    private void updateContainerHeight(String str) {
        this.mSuffix = str;
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) this.mDetailContainer.getLayoutParams();
        if (this.mDetailAdapter instanceof MiPlayDetailAdapter) {
            layoutParams.height = -2;
        } else if (this.mOrientation == 2 || "Wifi".equals(str) || "Bluetooth".equals(str)) {
            layoutParams.height = this.mWifiBtDetailHeight;
        } else {
            layoutParams.height = -2;
        }
        this.mDetailContainer.setLayoutParams(layoutParams);
    }

    public void requestLayout() {
        if (!isInLayout() && getParent() != null && getParent().isLayoutRequested()) {
            for (ViewParent parent = getParent(); parent != null; parent = parent.getParent()) {
                if (parent instanceof View) {
                    ((View) parent).mPrivateFlags &= -4097;
                }
            }
        }
        super.requestLayout();
    }

    public WindowInsets onApplyWindowInsets(WindowInsets windowInsets) {
        setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), windowInsets.getStableInsetBottom());
        return super.onApplyWindowInsets(windowInsets);
    }

    public void handleShowingDetail(DetailAdapter detailAdapter, View view, View view2) {
        boolean z;
        String str;
        String str2;
        boolean z2 = detailAdapter != null;
        boolean z3 = this.mDetailAdapter != null;
        if (z2 && z3 && this.mDetailAdapter == detailAdapter) {
            return;
        }
        if (z2 || z3) {
            if (z2) {
                this.mDetailAdapter = detailAdapter;
                setupDetailHeader(detailAdapter);
                setupDetailFooter(this.mDetailAdapter);
                int metricsCategory = this.mDetailAdapter.getMetricsCategory();
                View createDetailView = this.mDetailAdapter.createDetailView(this.mContext, this.mDetailViews.get(metricsCategory), this.mDetailContent);
                if (createDetailView instanceof MiuiQSDetailItems) {
                    MiuiQSDetailItems miuiQSDetailItems = (MiuiQSDetailItems) createDetailView;
                    String suffix = miuiQSDetailItems.getSuffix();
                    miuiQSDetailItems.setDetailShowing(true);
                    updateContainerHeight(suffix);
                } else {
                    this.mDetailContainer.getLayoutParams().height = this.mDetailAdapter.getContainerHeight();
                }
                if (createDetailView != null) {
                    this.mDetailContent.removeAllViews();
                    this.mDetailContent.addView(createDetailView);
                    this.mCurrentDetailIndex = metricsCategory;
                    this.mDetailViews.put(metricsCategory, createDetailView);
                    MetricsLogger.visible(this.mContext, this.mDetailAdapter.getMetricsCategory());
                    announceForAccessibility(this.mContext.getString(C0021R$string.accessibility_quick_settings_detail, this.mDetailAdapter.getTitle()));
                } else {
                    throw new IllegalStateException("Must return detail view");
                }
            } else {
                DetailAdapter detailAdapter2 = this.mDetailAdapter;
                if (detailAdapter2 != null) {
                    View view3 = this.mDetailViews.get(detailAdapter2.getMetricsCategory());
                    if (view3 instanceof MiuiQSDetailItems) {
                        MiuiQSDetailItems miuiQSDetailItems2 = (MiuiQSDetailItems) view3;
                        z = miuiQSDetailItems2.isItemClicked();
                        miuiQSDetailItems2.setItemClicked(false);
                        miuiQSDetailItems2.setDetailShowing(false);
                        str = miuiQSDetailItems2.getSuffix();
                    } else {
                        str = CodeInjection.MD5;
                        z = false;
                    }
                    if (!TextUtils.isEmpty(str)) {
                        updateContainerHeight(str);
                        if (this.mDonedClicked) {
                            str2 = "done_btn";
                        } else if (this.mSettingsClicked) {
                            str2 = "settings_button";
                        } else {
                            str2 = (!"Wifi".equals(str) || !z) ? "back_pressed" : "item_clicked";
                        }
                        ((SystemUIStat) Dependency.get(SystemUIStat.class)).handleQSDetailExitEvent(str, z, this.mSwitchClicked, str2);
                    }
                    MetricsLogger.hidden(this.mContext, this.mDetailAdapter.getMetricsCategory());
                    resetDataTrackStates();
                }
                this.mDetailAdapter = null;
                this.mQsPanelCallback.onScanStateChanged(false);
                view = null;
            }
            animateDetailVisibleDiff(z2, view, view2);
            sendAccessibilityEvent(32);
        }
    }

    private void resetDataTrackStates() {
        this.mSwitchClicked = false;
        this.mSettingsClicked = false;
        this.mDonedClicked = false;
    }

    /* access modifiers changed from: protected */
    public void animateDetailVisibleDiff(boolean z, View view, View view2) {
        Log.d("QSDetail", "animateDetailVisibleDiff: show = " + z + ", tileView = " + view);
        if (!z) {
            if (!DeviceConfig.isLowGpuDevice()) {
                animateDetailAlphaWithRotation(false, this.mFromView);
            }
            if (this.mFromView != null) {
                post(this.mAnimateHideRunnable);
            } else {
                setVisibility(8);
            }
        } else if (view == null) {
            this.mFromView = null;
            setVisibility(4);
        } else {
            setVisibility(4);
            this.mFromView = view;
            this.mToView = this.mDetailContainer;
            this.mTranslateView = view2;
            if (DeviceConfig.isLowGpuDevice()) {
                this.mDetailContainer.setAlpha(1.0f);
            } else {
                animateDetailAlphaWithRotation(true, this.mFromView);
            }
            post(this.mAnimateShowRunnable);
        }
    }

    /* access modifiers changed from: protected */
    public void setupDetailFooter(final DetailAdapter detailAdapter) {
        final Intent settingsIntent = detailAdapter.getSettingsIntent();
        this.mDetailSettingsButton.setVisibility(settingsIntent != null ? 0 : 8);
        this.mDetailSettingsButton.setOnClickListener(new View.OnClickListener() {
            /* class com.android.systemui.controlcenter.phone.detail.QSControlDetail.AnonymousClass6 */

            public void onClick(View view) {
                QSControlDetail.this.mSettingsClicked = true;
                MetricsLogger.action(QSControlDetail.this.mContext, 929, detailAdapter.getMetricsCategory());
                ((ControlCenterActivityStarter) Dependency.get(ControlCenterActivityStarter.class)).postStartActivityDismissingKeyguard(settingsIntent);
            }
        });
    }

    /* access modifiers changed from: protected */
    public void setupDetailHeader(final DetailAdapter detailAdapter) {
        if (!detailAdapter.hasHeader()) {
            this.mQsDetailHeader.setVisibility(8);
            return;
        }
        this.mQsDetailHeader.setVisibility(0);
        this.mQsDetailHeaderTitle.setText(detailAdapter.getTitle());
        Boolean toggleState = detailAdapter.getToggleState();
        if (toggleState == null) {
            this.mQsDetailHeaderSwitch.setVisibility(4);
            return;
        }
        this.mQsDetailHeaderSwitch.setVisibility(0);
        handleToggleStateChanged(toggleState.booleanValue(), detailAdapter.getToggleEnabled());
        this.mQsDetailHeaderSwitch.setOnPerformCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            /* class com.android.systemui.controlcenter.phone.detail.QSControlDetail.AnonymousClass7 */

            public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                QSControlDetail.this.mSwitchClicked = true;
                QSControlDetail.this.mSwitchState = z;
                detailAdapter.setToggleState(z);
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleToggleStateChanged(boolean z, boolean z2) {
        String str;
        if (this.mDetailAdapter == null) {
            return;
        }
        if (this.mSwitchState != z || this.mSwitchEnabled != z2) {
            this.mSwitchState = z;
            this.mSwitchEnabled = z2;
            this.mQsDetailHeaderSwitch.setEnabled(z2);
            if (z2) {
                this.mQsDetailHeaderSwitch.setChecked(z);
                SlidingButton slidingButton = this.mQsDetailHeaderSwitch;
                StringBuilder sb = new StringBuilder();
                sb.append((Object) this.mDetailAdapter.getTitle());
                sb.append(" ");
                if (z) {
                    str = this.mContext.getResources().getString(C0021R$string.accessibility_detail_switch_on);
                } else {
                    str = this.mContext.getResources().getString(C0021R$string.accessibility_detail_switch_off);
                }
                sb.append(str);
                slidingButton.announceForAccessibility(sb.toString());
                return;
            }
            SlidingButton slidingButton2 = this.mQsDetailHeaderSwitch;
            slidingButton2.announceForAccessibility(((Object) this.mDetailAdapter.getTitle()) + " " + this.mContext.getResources().getString(C0021R$string.accessibility_detail_switch_disable));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleScanStateChanged(boolean z) {
        if (this.mScanState != z) {
            this.mScanState = z;
        }
    }

    private void computeAnimationParams() {
        getLocationInWindowWithoutTransform(this.mFromView, this.mFromViewLocation);
        getLocationInWindowWithoutTransform(this.mToView, this.mToViewLocation);
        int width = this.mFromView.getWidth();
        int height = this.mFromView.getHeight();
        int width2 = this.mToView.getWidth();
        int height2 = this.mToView.getHeight();
        int[] iArr = this.mFromViewLocation;
        iArr[2] = iArr[0] + width;
        iArr[3] = iArr[1] + height;
        int[] iArr2 = this.mToViewLocation;
        iArr2[2] = iArr2[0] + width2;
        iArr2[3] = iArr2[1] + height2;
        this.mFromViewFrame[0] = this.mFromView.getLeft();
        this.mFromViewFrame[1] = this.mFromView.getTop();
        this.mFromViewFrame[2] = this.mFromView.getRight();
        this.mFromViewFrame[3] = this.mFromView.getBottom();
        this.mToViewFrame[0] = this.mToView.getLeft();
        this.mToViewFrame[1] = this.mToView.getTop();
        this.mToViewFrame[2] = this.mToView.getRight();
        this.mToViewFrame[3] = this.mToView.getBottom();
    }

    /* access modifiers changed from: protected */
    public void animateShowDetailAndTile() {
        EaseManager.EaseStyle easeStyle;
        computeAnimationParams();
        this.mAnim.cancel();
        IStateStyle to = this.mAnim.setTo("fromLeft", Integer.valueOf(this.mFromViewFrame[0]), "fromTop", Integer.valueOf(this.mFromViewFrame[1]), "fromRight", Integer.valueOf(this.mFromViewFrame[2]), "fromBottom", Integer.valueOf(this.mFromViewFrame[3]), "toLeft", Integer.valueOf((this.mToViewFrame[0] + this.mFromViewLocation[0]) - this.mToViewLocation[0]), "toTop", Integer.valueOf((this.mToViewFrame[1] + this.mFromViewLocation[1]) - this.mToViewLocation[1]), "toRight", Integer.valueOf((this.mToViewFrame[2] + this.mFromViewLocation[2]) - this.mToViewLocation[2]), "toBottom", Integer.valueOf((this.mToViewFrame[3] + this.mFromViewLocation[3]) - this.mToViewLocation[3]));
        Object[] objArr = new Object[17];
        objArr[0] = "fromLeft";
        objArr[1] = Integer.valueOf((this.mFromViewFrame[0] + this.mToViewLocation[0]) - this.mFromViewLocation[0]);
        objArr[2] = "fromTop";
        objArr[3] = Integer.valueOf((this.mFromViewFrame[1] + this.mToViewLocation[1]) - this.mFromViewLocation[1]);
        objArr[4] = "fromRight";
        objArr[5] = Integer.valueOf((this.mFromViewFrame[2] + this.mToViewLocation[2]) - this.mFromViewLocation[2]);
        objArr[6] = "fromBottom";
        objArr[7] = Integer.valueOf((this.mFromViewFrame[3] + this.mToViewLocation[3]) - this.mFromViewLocation[3]);
        objArr[8] = "toLeft";
        objArr[9] = Integer.valueOf(this.mToViewFrame[0]);
        objArr[10] = "toTop";
        objArr[11] = Integer.valueOf(this.mToViewFrame[1]);
        objArr[12] = "toRight";
        objArr[13] = Integer.valueOf(this.mToViewFrame[2]);
        objArr[14] = "toBottom";
        objArr[15] = Integer.valueOf(this.mToViewFrame[3]);
        AnimConfig animConfig = new AnimConfig();
        if (this.mShowingMiPlayDetail) {
            easeStyle = EaseManager.getStyle(-2, 0.7f, 0.3f);
        } else {
            easeStyle = EaseManager.getStyle(-2, 0.8f, 0.3f);
        }
        animConfig.setEase(easeStyle);
        animConfig.addListeners(new TransitionListener() {
            /* class com.android.systemui.controlcenter.phone.detail.QSControlDetail.AnonymousClass9 */
            final View from;
            int[] fromFrame = new int[4];
            final View to;
            int[] toFrame = new int[4];
            final View translate;

            {
                QSControlDetail qSControlDetail = QSControlDetail.this;
                this.from = qSControlDetail.mFromView;
                this.to = qSControlDetail.mToView;
                this.translate = qSControlDetail.mTranslateView;
            }

            @Override // miuix.animation.listener.TransitionListener
            public void onUpdate(Object obj, Collection<UpdateInfo> collection) {
                super.onUpdate(obj, collection);
                UpdateInfo findByName = UpdateInfo.findByName(collection, "fromLeft");
                UpdateInfo findByName2 = UpdateInfo.findByName(collection, "fromTop");
                UpdateInfo findByName3 = UpdateInfo.findByName(collection, "fromRight");
                UpdateInfo findByName4 = UpdateInfo.findByName(collection, "fromBottom");
                if (findByName != null) {
                    this.fromFrame[0] = (int) findByName.getFloatValue();
                }
                if (findByName2 != null) {
                    this.fromFrame[1] = (int) findByName2.getFloatValue();
                }
                if (findByName3 != null) {
                    this.fromFrame[2] = (int) findByName3.getFloatValue();
                }
                if (findByName4 != null) {
                    this.fromFrame[3] = (int) findByName4.getFloatValue();
                }
                View view = this.from;
                int[] iArr = this.fromFrame;
                view.setLeftTopRightBottom(iArr[0], iArr[1], iArr[2], iArr[3]);
                UpdateInfo findByName5 = UpdateInfo.findByName(collection, "toLeft");
                UpdateInfo findByName6 = UpdateInfo.findByName(collection, "toTop");
                UpdateInfo findByName7 = UpdateInfo.findByName(collection, "toRight");
                UpdateInfo findByName8 = UpdateInfo.findByName(collection, "toBottom");
                if (findByName5 != null) {
                    this.toFrame[0] = (int) findByName5.getFloatValue();
                }
                if (findByName6 != null) {
                    this.toFrame[1] = (int) findByName6.getFloatValue();
                }
                if (findByName7 != null) {
                    this.toFrame[2] = (int) findByName7.getFloatValue();
                }
                if (findByName8 != null) {
                    this.toFrame[3] = (int) findByName8.getFloatValue();
                }
                View view2 = this.to;
                int[] iArr2 = this.toFrame;
                view2.setLeftTopRightBottom(iArr2[0], iArr2[1], iArr2[2], iArr2[3]);
                View view3 = this.translate;
                if (view3 != null) {
                    int[] iArr3 = this.fromFrame;
                    int i = iArr3[2] - iArr3[0];
                    int[] iArr4 = QSControlDetail.this.mFromViewFrame;
                    view3.setTranslationX((float) (i - (iArr4[2] - iArr4[0])));
                    View view4 = this.translate;
                    int[] iArr5 = this.fromFrame;
                    int[] iArr6 = QSControlDetail.this.mFromViewFrame;
                    view4.setTranslationY((float) ((iArr5[3] - iArr5[1]) - (iArr6[3] - iArr6[1])));
                }
            }

            @Override // miuix.animation.listener.TransitionListener
            public void onBegin(Object obj) {
                super.onBegin(obj);
                QSControlDetail qSControlDetail = QSControlDetail.this;
                qSControlDetail.mIsAnimating = true;
                qSControlDetail.setVisibility(0);
                View view = QSControlDetail.this.mFromView;
                if (view != null && (view.getParent() instanceof ViewGroup)) {
                    ((ViewGroup) QSControlDetail.this.mFromView.getParent()).suppressLayout(true);
                }
                View view2 = QSControlDetail.this.mToView;
                if (view2 != null && (view2.getParent() instanceof ViewGroup)) {
                    ((ViewGroup) QSControlDetail.this.mToView.getParent()).suppressLayout(true);
                }
                View view3 = (View) QSControlDetail.this.mDetailViews.get(QSControlDetail.this.mCurrentDetailIndex);
                if (view3 instanceof ViewGroup) {
                    ((ViewGroup) view3).suppressLayout(true);
                }
            }

            @Override // miuix.animation.listener.TransitionListener
            public void onComplete(Object obj) {
                super.onComplete(obj);
                QSControlDetail qSControlDetail = QSControlDetail.this;
                qSControlDetail.mIsAnimating = false;
                View view = qSControlDetail.mFromView;
                int[] iArr = qSControlDetail.mFromViewFrame;
                view.setLeftTopRightBottom(iArr[0], iArr[1], iArr[2], iArr[3]);
                View view2 = QSControlDetail.this.mFromView;
                if (view2 != null && (view2.getParent() instanceof ViewGroup)) {
                    ((ViewGroup) QSControlDetail.this.mFromView.getParent()).suppressLayout(false);
                }
                View view3 = QSControlDetail.this.mToView;
                if (view3 != null && (view3.getParent() instanceof ViewGroup)) {
                    ((ViewGroup) QSControlDetail.this.mToView.getParent()).suppressLayout(false);
                }
                View view4 = (View) QSControlDetail.this.mDetailViews.get(QSControlDetail.this.mCurrentDetailIndex);
                if (view4 instanceof ViewGroup) {
                    ((ViewGroup) view4).suppressLayout(false);
                }
                if (view4 instanceof MiuiQSDetailItems) {
                    ((MiuiQSDetailItems) view4).notifyData();
                }
            }
        });
        objArr[16] = animConfig;
        to.to(objArr);
    }

    /* access modifiers changed from: protected */
    public void animateShowDetailAndTileOnLowEnd() {
        PhysicBasedInterpolator physicBasedInterpolator = new PhysicBasedInterpolator(0.9f, 0.35f);
        ValueAnimator duration = ValueAnimator.ofFloat(1.0f, 0.0f).setDuration(300L);
        duration.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.android.systemui.controlcenter.phone.detail.QSControlDetail.AnonymousClass10 */

            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                float f = 1.0f - (0.1f * floatValue);
                QSControlDetail.this.mToView.setScaleX(f);
                QSControlDetail.this.mToView.setScaleY(f);
                QSControlDetail.this.mToView.setAlpha(1.0f - floatValue);
            }
        });
        duration.addListener(new AnimatorListenerAdapter() {
            /* class com.android.systemui.controlcenter.phone.detail.QSControlDetail.AnonymousClass11 */

            public void onAnimationStart(Animator animator) {
                QSControlDetail.this.setVisibility(0);
                ((ViewGroup) QSControlDetail.this.mFromView.getParent()).suppressLayout(true);
                ((ViewGroup) QSControlDetail.this.mToView.getParent()).suppressLayout(true);
            }

            public void onAnimationEnd(Animator animator) {
                ViewGroup viewGroup = (ViewGroup) QSControlDetail.this.mFromView.getParent();
                if (viewGroup != null) {
                    viewGroup.suppressLayout(false);
                }
                ViewGroup viewGroup2 = (ViewGroup) QSControlDetail.this.mToView.getParent();
                if (viewGroup2 != null) {
                    viewGroup2.suppressLayout(false);
                }
                View view = (View) QSControlDetail.this.mDetailViews.get(QSControlDetail.this.mCurrentDetailIndex);
                if (view instanceof MiuiQSDetailItems) {
                    ((MiuiQSDetailItems) view).notifyData();
                }
            }
        });
        duration.setInterpolator(physicBasedInterpolator);
        duration.start();
    }

    /* access modifiers changed from: protected */
    public void animateHideDetailAndTile() {
        EaseManager.EaseStyle easeStyle;
        if (!this.mIsAnimating) {
            computeAnimationParams();
        }
        this.mAnim.cancel();
        IStateStyle to = this.mAnim.setTo("fromLeft", Integer.valueOf((this.mFromViewFrame[0] + this.mToViewLocation[0]) - this.mFromViewLocation[0]), "fromTop", Integer.valueOf((this.mFromViewFrame[1] + this.mToViewLocation[1]) - this.mFromViewLocation[1]), "fromRight", Integer.valueOf((this.mFromViewFrame[2] + this.mToViewLocation[2]) - this.mFromViewLocation[2]), "fromBottom", Integer.valueOf((this.mFromViewFrame[3] + this.mToViewLocation[3]) - this.mFromViewLocation[3]), "toLeft", Integer.valueOf(this.mToViewFrame[0]), "toTop", Integer.valueOf(this.mToViewFrame[1]), "toRight", Integer.valueOf(this.mToViewFrame[2]), "toBottom", Integer.valueOf(this.mToViewFrame[3]));
        Object[] objArr = new Object[17];
        objArr[0] = "fromLeft";
        objArr[1] = Integer.valueOf(this.mFromViewFrame[0]);
        objArr[2] = "fromTop";
        objArr[3] = Integer.valueOf(this.mFromViewFrame[1]);
        objArr[4] = "fromRight";
        objArr[5] = Integer.valueOf(this.mFromViewFrame[2]);
        objArr[6] = "fromBottom";
        objArr[7] = Integer.valueOf(this.mFromViewFrame[3]);
        objArr[8] = "toLeft";
        objArr[9] = Integer.valueOf((this.mToViewFrame[0] + this.mFromViewLocation[0]) - this.mToViewLocation[0]);
        objArr[10] = "toTop";
        objArr[11] = Integer.valueOf((this.mToViewFrame[1] + this.mFromViewLocation[1]) - this.mToViewLocation[1]);
        objArr[12] = "toRight";
        objArr[13] = Integer.valueOf((this.mToViewFrame[2] + this.mFromViewLocation[2]) - this.mToViewLocation[2]);
        objArr[14] = "toBottom";
        objArr[15] = Integer.valueOf((this.mToViewFrame[3] + this.mFromViewLocation[3]) - this.mToViewLocation[3]);
        AnimConfig animConfig = new AnimConfig();
        if (this.mShowingMiPlayDetail) {
            easeStyle = EaseManager.getStyle(-2, 0.75f, 0.3f);
        } else {
            easeStyle = EaseManager.getStyle(-2, 0.8f, 0.3f);
        }
        animConfig.setEase(easeStyle);
        animConfig.addListeners(new TransitionListener() {
            /* class com.android.systemui.controlcenter.phone.detail.QSControlDetail.AnonymousClass12 */
            final View from;
            int[] fromFrame = new int[4];
            final View to;
            int[] toFrame = new int[4];
            final View translate;

            {
                QSControlDetail qSControlDetail = QSControlDetail.this;
                this.translate = qSControlDetail.mTranslateView;
                this.from = qSControlDetail.mFromView;
                this.to = qSControlDetail.mToView;
            }

            @Override // miuix.animation.listener.TransitionListener
            public void onBegin(Object obj) {
                super.onBegin(obj);
                QSControlDetail qSControlDetail = QSControlDetail.this;
                qSControlDetail.mIsAnimating = true;
                View view = qSControlDetail.mFromView;
                if (view != null && (view.getParent() instanceof ViewGroup)) {
                    ((ViewGroup) QSControlDetail.this.mFromView.getParent()).suppressLayout(true);
                }
                View view2 = QSControlDetail.this.mToView;
                if (view2 != null && (view2.getParent() instanceof ViewGroup)) {
                    ((ViewGroup) QSControlDetail.this.mToView.getParent()).suppressLayout(true);
                }
                View view3 = (View) QSControlDetail.this.mDetailViews.get(QSControlDetail.this.mCurrentDetailIndex);
                if (view3 instanceof ViewGroup) {
                    ((ViewGroup) view3).suppressLayout(true);
                }
            }

            @Override // miuix.animation.listener.TransitionListener
            public void onUpdate(Object obj, Collection<UpdateInfo> collection) {
                super.onUpdate(obj, collection);
                UpdateInfo findByName = UpdateInfo.findByName(collection, "fromLeft");
                UpdateInfo findByName2 = UpdateInfo.findByName(collection, "fromTop");
                UpdateInfo findByName3 = UpdateInfo.findByName(collection, "fromRight");
                UpdateInfo findByName4 = UpdateInfo.findByName(collection, "fromBottom");
                if (findByName != null) {
                    this.fromFrame[0] = (int) findByName.getFloatValue();
                }
                if (findByName2 != null) {
                    this.fromFrame[1] = (int) findByName2.getFloatValue();
                }
                if (findByName3 != null) {
                    this.fromFrame[2] = (int) findByName3.getFloatValue();
                }
                if (findByName4 != null) {
                    this.fromFrame[3] = (int) findByName4.getFloatValue();
                }
                View view = this.from;
                int[] iArr = this.fromFrame;
                view.setLeftTopRightBottom(iArr[0], iArr[1], iArr[2], iArr[3]);
                UpdateInfo findByName5 = UpdateInfo.findByName(collection, "toLeft");
                UpdateInfo findByName6 = UpdateInfo.findByName(collection, "toTop");
                UpdateInfo findByName7 = UpdateInfo.findByName(collection, "toRight");
                UpdateInfo findByName8 = UpdateInfo.findByName(collection, "toBottom");
                if (findByName5 != null) {
                    this.toFrame[0] = (int) findByName5.getFloatValue();
                }
                if (findByName6 != null) {
                    this.toFrame[1] = (int) findByName6.getFloatValue();
                }
                if (findByName7 != null) {
                    this.toFrame[2] = (int) findByName7.getFloatValue();
                }
                if (findByName8 != null) {
                    this.toFrame[3] = (int) findByName8.getFloatValue();
                }
                View view2 = this.to;
                int[] iArr2 = this.toFrame;
                view2.setLeftTopRightBottom(iArr2[0], iArr2[1], iArr2[2], iArr2[3]);
                View view3 = this.translate;
                if (view3 != null) {
                    int[] iArr3 = this.fromFrame;
                    int i = iArr3[2] - iArr3[0];
                    int[] iArr4 = QSControlDetail.this.mFromViewFrame;
                    view3.setTranslationX((float) (i - (iArr4[2] - iArr4[0])));
                    View view4 = this.translate;
                    int[] iArr5 = this.fromFrame;
                    int[] iArr6 = QSControlDetail.this.mFromViewFrame;
                    view4.setTranslationY((float) ((iArr5[3] - iArr5[1]) - (iArr6[3] - iArr6[1])));
                }
            }

            @Override // miuix.animation.listener.TransitionListener
            public void onComplete(Object obj) {
                super.onComplete(obj);
                QSControlDetail qSControlDetail = QSControlDetail.this;
                qSControlDetail.mIsAnimating = false;
                View view = qSControlDetail.mFromView;
                if (view != null && (view.getParent() instanceof ViewGroup)) {
                    ((ViewGroup) QSControlDetail.this.mFromView.getParent()).suppressLayout(false);
                }
                View view2 = QSControlDetail.this.mToView;
                if (view2 != null && (view2.getParent() instanceof ViewGroup)) {
                    ((ViewGroup) QSControlDetail.this.mToView.getParent()).suppressLayout(false);
                }
                View view3 = (View) QSControlDetail.this.mDetailViews.get(QSControlDetail.this.mCurrentDetailIndex);
                if (view3 instanceof ViewGroup) {
                    ((ViewGroup) view3).suppressLayout(false);
                }
                QSControlDetail.this.setVisibility(8);
                View view4 = QSControlDetail.this.mFromView;
                if (view4 instanceof QSBigTileView) {
                    ((QSBigTileView) view4).updateIndicatorTouch();
                }
                if (DeviceConfig.isMiddleGpuDevice()) {
                    CpuBoostUtil.getInstance().cancelBoostCpu();
                }
            }
        });
        objArr[16] = animConfig;
        to.to(objArr);
    }

    /* access modifiers changed from: protected */
    public void animateHideDetailAndTileOnLowEnd() {
        PhysicBasedInterpolator physicBasedInterpolator = new PhysicBasedInterpolator(0.9f, 0.35f);
        ValueAnimator duration = ValueAnimator.ofFloat(1.0f, 0.0f).setDuration(300L);
        duration.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.android.systemui.controlcenter.phone.detail.QSControlDetail.AnonymousClass13 */

            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                float f = (0.1f * floatValue) + 0.9f;
                QSControlDetail.this.mToView.setScaleX(f);
                QSControlDetail.this.mToView.setScaleY(f);
                QSControlDetail.this.mToView.setAlpha(floatValue);
            }
        });
        duration.addListener(new AnimatorListenerAdapter() {
            /* class com.android.systemui.controlcenter.phone.detail.QSControlDetail.AnonymousClass14 */

            public void onAnimationStart(Animator animator) {
                View view = QSControlDetail.this.mToView;
                if (view != null && (view.getParent() instanceof ViewGroup)) {
                    ((ViewGroup) QSControlDetail.this.mToView.getParent()).suppressLayout(true);
                }
            }

            public void onAnimationEnd(Animator animator) {
                View view = QSControlDetail.this.mToView;
                if (view != null && (view.getParent() instanceof ViewGroup)) {
                    ((ViewGroup) QSControlDetail.this.mToView.getParent()).suppressLayout(false);
                }
                QSControlDetail.this.setVisibility(8);
                QSControlDetail.this.mDetailContainer.setAlpha(0.0f);
            }
        });
        duration.setInterpolator(physicBasedInterpolator);
        duration.start();
    }

    /* access modifiers changed from: protected */
    public void animateDetailAlphaWithRotation(boolean z, View view) {
        EaseManager.EaseStyle easeStyle;
        EaseManager.EaseStyle easeStyle2;
        if (z) {
            Folme.useAt(this.mDetailContainer).state().cancel();
            if (view != null) {
                this.mDetailContainer.setRotationX(view.getRotationX());
                this.mDetailContainer.setRotationY(view.getRotationY());
                this.mDetailContainer.setTranslationZ(view.getTranslationZ());
            }
            this.mDetailContainer.setAlpha(0.0f);
            IStateStyle state = Folme.useAt(this.mDetailContainer).state();
            AnimState animState = new AnimState("detail_container_alpha");
            animState.add(ViewProperty.ALPHA, 1.0d);
            animState.add(ViewProperty.ROTATION_X, 0.0d);
            animState.add(ViewProperty.ROTATION_Y, 0.0d);
            animState.add(ViewProperty.TRANSLATION_Z, 0.0d);
            AnimConfig[] animConfigArr = new AnimConfig[1];
            AnimConfig animConfig = new AnimConfig();
            if (this.mShowingMiPlayDetail) {
                easeStyle2 = EaseManager.getStyle(0, 300.0f, 0.8f, 0.6666f);
            } else {
                easeStyle2 = EaseManager.getStyle(0, 300.0f, 0.8f, 0.6666f);
            }
            animConfig.setEase(easeStyle2);
            animConfig.addListeners(new TransitionListener() {
                /* class com.android.systemui.controlcenter.phone.detail.QSControlDetail.AnonymousClass15 */

                @Override // miuix.animation.listener.TransitionListener
                public void onBegin(Object obj) {
                    super.onBegin(obj);
                    QSControlDetail.this.mDetailContainer.setLayerType(2, null);
                }

                @Override // miuix.animation.listener.TransitionListener
                public void onComplete(Object obj) {
                    super.onComplete(obj);
                    QSControlDetail.this.mDetailContainer.setLayerType(0, null);
                }
            });
            animConfigArr[0] = animConfig;
            state.to(animState, animConfigArr);
            return;
        }
        Folme.useAt(this.mDetailContainer).state().cancel();
        IStateStyle state2 = Folme.useAt(this.mDetailContainer).state();
        AnimState animState2 = new AnimState("detail_container_alpha");
        animState2.add(ViewProperty.ALPHA, 0.0d);
        animState2.add(ViewProperty.ROTATION_X, 0.0d);
        animState2.add(ViewProperty.ROTATION_Y, 0.0d);
        animState2.add(ViewProperty.TRANSLATION_Z, 0.0d);
        AnimConfig[] animConfigArr2 = new AnimConfig[1];
        AnimConfig animConfig2 = new AnimConfig();
        if (this.mShowingMiPlayDetail) {
            easeStyle = EaseManager.getStyle(0, 200.0f, 0.8f, 0.6666f);
        } else {
            easeStyle = EaseManager.getStyle(0, 300.0f, 0.8f, 0.6666f);
        }
        animConfig2.setEase(easeStyle);
        animConfig2.addListeners(new TransitionListener() {
            /* class com.android.systemui.controlcenter.phone.detail.QSControlDetail.AnonymousClass16 */

            @Override // miuix.animation.listener.TransitionListener
            public void onBegin(Object obj) {
                super.onBegin(obj);
                QSControlDetail.this.mDetailContainer.setLayerType(2, null);
            }

            @Override // miuix.animation.listener.TransitionListener
            public void onComplete(Object obj) {
                super.onComplete(obj);
                QSControlDetail.this.mDetailContainer.setLayerType(0, null);
            }
        });
        animConfigArr2[0] = animConfig2;
        state2.to(animState2, animConfigArr2);
    }

    /* access modifiers changed from: protected */
    public void getLocationInWindowWithoutTransform(View view, int[] iArr) {
        if (iArr == null || iArr.length < 2) {
            throw new IllegalArgumentException("inOutLocation must be an array of two integers");
        }
        iArr[1] = 0;
        iArr[0] = 0;
        iArr[0] = iArr[0] + view.getLeft();
        iArr[1] = iArr[1] + view.getTop();
        ViewParent parent = view.getParent();
        while (parent instanceof View) {
            View view2 = (View) parent;
            iArr[0] = iArr[0] - view2.getScrollX();
            iArr[1] = iArr[1] - view2.getScrollY();
            iArr[0] = iArr[0] + view2.getLeft();
            iArr[1] = iArr[1] + view2.getTop();
            parent = view2.getParent();
        }
        iArr[0] = Math.round((float) iArr[0]);
        iArr[1] = Math.round((float) iArr[1]);
    }
}
