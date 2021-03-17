package com.android.systemui.qs;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import androidx.dynamicanimation.animation.FloatPropertyCompat;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0017R$layout;
import com.android.systemui.C0022R$style;
import com.android.systemui.Interpolators;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.qs.customize.MiuiQSCustomizer;
import com.android.systemui.settings.ToggleSliderView;
import com.android.systemui.statusbar.policy.BrightnessMirrorController;
import com.android.systemui.statusbar.policy.MiuiBrightnessController;
import com.android.systemui.tuner.TunerService;
import com.android.systemui.util.InjectionInflationController;
import com.android.systemui.util.animation.PhysicsAnimator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class QSContainerImpl extends FrameLayout implements TunerService.Tunable, BrightnessMirrorController.BrightnessMirrorListener {
    private static final FloatPropertyCompat<QSContainerImpl> BACKGROUND_BOTTOM = new FloatPropertyCompat<QSContainerImpl>("backgroundBottom") {
        /* class com.android.systemui.qs.QSContainerImpl.AnonymousClass1 */

        public float getValue(QSContainerImpl qSContainerImpl) {
            return qSContainerImpl.getBackgroundBottom();
        }

        public void setValue(QSContainerImpl qSContainerImpl, float f) {
            qSContainerImpl.setBackgroundBottom((int) f);
        }
    };
    private static final PhysicsAnimator.SpringConfig BACKGROUND_SPRING = new PhysicsAnimator.SpringConfig(1500.0f, 0.75f);
    private boolean mAnimateBottomOnNextLayout;
    private View mBackground;
    private int mBackgroundBottom = -1;
    private View mBackgroundGradient;
    private MiuiBrightnessController mBrightnessController;
    private BrightnessMirrorController mBrightnessMirrorController;
    protected ToggleSliderView mBrightnessView;
    private final BroadcastDispatcher mBroadcastDispatcher;
    private ValueAnimator mCaretAnimator;
    private Interpolator mCaretInterpolator;
    private int mContentPaddingEnd = -1;
    private int mContentPaddingStart = -1;
    private QSFooterDataUsage mDataUsage;
    private ImageView mDragHandle;
    private View[] mExtraAnimatedViews;
    private MiuiNotificationShadeHeader mHeader;
    private int mHeightOverride = -1;
    private IndicatorDrawable mIndicatorDrawable;
    protected float mIndicatorProgress;
    private final InjectionInflationController mInjectionInflater;
    private QSContent mQSContent;
    private MiuiQSCustomizer mQSCustomizer;
    private MiuiQSDetail mQSDetail;
    private ViewGroup mQSFooterBundle;
    private QSPanel mQSPanel;
    private View mQSPanelContainer;
    private boolean mQsDisabled;
    private float mQsExpansion;
    private QuickQSPanel mQuickQSPanel;
    private boolean mShowQsPanel;
    private int mSideMargins;
    private final Point mSizePoint = new Point();
    private View mStatusBarBackground;
    private final TunerService mTunerService;

    public boolean performClick() {
        return true;
    }

    public QSContainerImpl(Context context, AttributeSet attributeSet, BroadcastDispatcher broadcastDispatcher, InjectionInflationController injectionInflationController, TunerService tunerService) {
        super(context, attributeSet);
        this.mBroadcastDispatcher = broadcastDispatcher;
        this.mInjectionInflater = injectionInflationController;
        this.mTunerService = tunerService;
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mQSPanel = (QSPanel) findViewById(C0015R$id.quick_settings_panel);
        this.mQuickQSPanel = (QuickQSPanel) findViewById(C0015R$id.quick_qs_panel);
        this.mQSContent = (QSContent) findViewById(C0015R$id.qs_content);
        this.mBrightnessView = (ToggleSliderView) findViewById(C0015R$id.brightness_slider);
        this.mBrightnessController = new MiuiBrightnessController(getContext(), this.mBrightnessView, this.mBroadcastDispatcher);
        this.mQSFooterBundle = (ViewGroup) findViewById(C0015R$id.qs_footer_bundle);
        this.mQSPanelContainer = findViewById(C0015R$id.expanded_qs_scroll_view);
        this.mQSDetail = (MiuiQSDetail) findViewById(C0015R$id.qs_detail);
        MiuiNotificationShadeHeader miuiNotificationShadeHeader = (MiuiNotificationShadeHeader) findViewById(C0015R$id.header);
        this.mHeader = miuiNotificationShadeHeader;
        miuiNotificationShadeHeader.setQSContainer(this);
        this.mQSCustomizer = (MiuiQSCustomizer) findViewById(C0015R$id.qs_customize);
        this.mDragHandle = (ImageView) findViewById(C0015R$id.qs_expand_indicator);
        IndicatorDrawable indicatorDrawable = new IndicatorDrawable(getContext());
        this.mIndicatorDrawable = indicatorDrawable;
        this.mDragHandle.setImageDrawable(indicatorDrawable);
        this.mCaretInterpolator = Interpolators.FAST_OUT_SLOW_IN;
        this.mBackground = findViewById(C0015R$id.quick_settings_background);
        this.mStatusBarBackground = findViewById(C0015R$id.quick_settings_status_bar_background);
        this.mBackgroundGradient = findViewById(C0015R$id.quick_settings_gradient_view);
        updateResources();
        this.mQuickQSPanel.setMediaVisibilityChangedListener(new Consumer() {
            /* class com.android.systemui.qs.$$Lambda$QSContainerImpl$SmgcCxPvK9MpCttxm75WvXCaB3s */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                QSContainerImpl.this.lambda$onFinishInflate$0$QSContainerImpl((Boolean) obj);
            }
        });
        this.mQSPanel.setMediaVisibilityChangedListener(new Consumer() {
            /* class com.android.systemui.qs.$$Lambda$QSContainerImpl$671EqL2XSP9H1_W3SpTMCiE58Y */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                QSContainerImpl.this.lambda$onFinishInflate$1$QSContainerImpl((Boolean) obj);
            }
        });
        setImportantForAccessibility(2);
        setupAnimatedViews();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onFinishInflate$0 */
    public /* synthetic */ void lambda$onFinishInflate$0$QSContainerImpl(Boolean bool) {
        if (this.mQuickQSPanel.isShown()) {
            this.mAnimateBottomOnNextLayout = true;
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onFinishInflate$1 */
    public /* synthetic */ void lambda$onFinishInflate$1$QSContainerImpl(Boolean bool) {
        if (this.mQSPanel.isShown()) {
            this.mAnimateBottomOnNextLayout = true;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setBackgroundBottom(int i) {
        this.mBackgroundBottom = i;
        this.mBackground.setBottom(i);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private float getBackgroundBottom() {
        int i = this.mBackgroundBottom;
        return i == -1 ? (float) this.mBackground.getBottom() : (float) i;
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        setBackgroundGradientVisibility(configuration);
        updateResources();
        this.mSizePoint.set(0, 0);
        updateBrightnessMirror();
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int i, int i2) {
        Configuration configuration = getResources().getConfiguration();
        boolean z = configuration.smallestScreenWidthDp >= 600 || configuration.orientation != 2;
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) this.mQSPanelContainer.getLayoutParams();
        int displayHeight = ((getDisplayHeight() - marginLayoutParams.topMargin) - marginLayoutParams.bottomMargin) - getPaddingBottom();
        if (z) {
            displayHeight -= getResources().getDimensionPixelSize(C0012R$dimen.navigation_bar_height);
        }
        int i3 = ((FrameLayout) this).mPaddingLeft + ((FrameLayout) this).mPaddingRight + marginLayoutParams.leftMargin + marginLayoutParams.rightMargin;
        this.mQSPanelContainer.measure(FrameLayout.getChildMeasureSpec(i, i3, marginLayoutParams.width), View.MeasureSpec.makeMeasureSpec(displayHeight, Integer.MIN_VALUE));
        super.onMeasure(View.MeasureSpec.makeMeasureSpec(this.mQSPanelContainer.getMeasuredWidth() + i3, 1073741824), View.MeasureSpec.makeMeasureSpec(marginLayoutParams.topMargin + marginLayoutParams.bottomMargin + this.mQSPanelContainer.getMeasuredHeight() + this.mQSFooterBundle.getMeasuredHeight() + getPaddingBottom(), 1073741824));
        this.mQSCustomizer.measure(i, View.MeasureSpec.makeMeasureSpec(getDisplayHeight(), 1073741824));
        this.mQSDetail.measure(i, View.MeasureSpec.makeMeasureSpec(getDisplayHeight(), 1073741824));
    }

    /* access modifiers changed from: protected */
    public void measureChildWithMargins(View view, int i, int i2, int i3, int i4) {
        if (view != this.mQSPanelContainer) {
            super.measureChildWithMargins(view, i, i2, i3, i4);
        }
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        updateExpansion(this.mAnimateBottomOnNextLayout);
        this.mAnimateBottomOnNextLayout = false;
    }

    public void disable(int i, int i2, boolean z) {
        boolean z2 = true;
        int i3 = 0;
        if ((i2 & 1) == 0) {
            z2 = false;
        }
        if (z2 != this.mQsDisabled) {
            this.mQsDisabled = z2;
            setBackgroundGradientVisibility(getResources().getConfiguration());
            View view = this.mBackground;
            if (this.mQsDisabled) {
                i3 = 8;
            }
            view.setVisibility(i3);
        }
    }

    public void updateResources() {
        int height = this.mHeader.getHeight();
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) this.mQSPanelContainer.getLayoutParams();
        layoutParams.topMargin = height;
        this.mQSPanelContainer.setLayoutParams(layoutParams);
        FrameLayout.LayoutParams layoutParams2 = (FrameLayout.LayoutParams) this.mQuickQSPanel.getLayoutParams();
        layoutParams2.topMargin = height;
        this.mQuickQSPanel.setLayoutParams(layoutParams2);
        this.mQSDetail.updateHeaderHeight(height);
        this.mQSCustomizer.updateResources(height);
        this.mSideMargins = getResources().getDimensionPixelSize(C0012R$dimen.notification_side_paddings);
        this.mContentPaddingStart = getResources().getDimensionPixelSize(17105356);
        int dimensionPixelSize = getResources().getDimensionPixelSize(17105355);
        boolean z = dimensionPixelSize != this.mContentPaddingEnd;
        this.mContentPaddingEnd = dimensionPixelSize;
        if (z) {
            updatePaddingsAndMargins();
        }
    }

    public void setHeightOverride(int i) {
        this.mHeightOverride = i;
        updateExpansion();
    }

    public void updateExpansion() {
        updateExpansion(false);
    }

    public void updateExpansion(boolean z) {
        int calculateContainerHeight = calculateContainerHeight();
        setBottom(getTop() + calculateContainerHeight);
        ViewGroup viewGroup = this.mQSFooterBundle;
        viewGroup.setTranslationY((float) (calculateContainerHeight - viewGroup.getHeight()));
        this.mBackground.setTop(this.mQSPanelContainer.getTop());
        updateBackgroundBottom((calculateContainerHeight - this.mQSFooterBundle.getHeight()) + getDataUsageHeight(), z);
    }

    private void updateBackgroundBottom(int i, boolean z) {
        FloatPropertyCompat<QSContainerImpl> floatPropertyCompat = BACKGROUND_BOTTOM;
        PhysicsAnimator instance = PhysicsAnimator.getInstance(this);
        if (instance.isPropertyAnimating(floatPropertyCompat) || z) {
            floatPropertyCompat.setValue(this, floatPropertyCompat.getValue(this));
            instance.spring(floatPropertyCompat, (float) i, BACKGROUND_SPRING);
            instance.start();
            return;
        }
        floatPropertyCompat.setValue(this, (float) i);
    }

    /* access modifiers changed from: protected */
    public int calculateContainerHeight() {
        int i = this.mHeightOverride;
        if (i == -1) {
            i = getHeight();
        }
        int minHeight = getMinHeight();
        return Math.round(this.mQsExpansion * ((float) (i - minHeight))) + minHeight;
    }

    private void setBackgroundGradientVisibility(Configuration configuration) {
        int i = 4;
        if (configuration.orientation == 2) {
            this.mBackgroundGradient.setVisibility(4);
            this.mStatusBarBackground.setVisibility(4);
            return;
        }
        View view = this.mBackgroundGradient;
        if (!this.mQsDisabled) {
            i = 0;
        }
        view.setVisibility(i);
        this.mStatusBarBackground.setVisibility(0);
    }

    public void setExpansion(float f) {
        float f2 = 0.0f;
        if (this.mQsExpansion - f > 0.002f && f != 0.0f) {
            f2 = -1.0f;
        } else if (f - this.mQsExpansion > 0.002f && f != 1.0f) {
            f2 = 1.0f;
        }
        updateIndicator(f2);
        this.mQSPanelContainer.setTranslationY(((float) (this.mQSPanelContainer.getBottom() - this.mQuickQSPanel.getBottom())) * (f - 1.0f));
        this.mQsExpansion = f;
        updateExpansion();
    }

    private void updatePaddingsAndMargins() {
        for (int i = 0; i < getChildCount(); i++) {
            View childAt = getChildAt(i);
            if (!(childAt == this.mStatusBarBackground || childAt == this.mBackgroundGradient || childAt == this.mHeader)) {
                MiuiQSCustomizer miuiQSCustomizer = this.mQSCustomizer;
                if (childAt == miuiQSCustomizer) {
                    int i2 = this.mSideMargins;
                    miuiQSCustomizer.setMargins(i2, i2);
                } else {
                    MiuiQSDetail miuiQSDetail = this.mQSDetail;
                    if (childAt == miuiQSDetail) {
                        int i3 = this.mSideMargins;
                        miuiQSDetail.setMargins(i3, i3);
                    } else {
                        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) childAt.getLayoutParams();
                        int i4 = this.mSideMargins;
                        layoutParams.rightMargin = i4;
                        layoutParams.leftMargin = i4;
                        if (childAt == this.mQSPanelContainer) {
                            this.mQSPanel.setContentMargins(this.mContentPaddingStart, this.mContentPaddingEnd);
                        } else if (!(childAt == this.mQSContent || childAt == this.mHeader)) {
                            childAt.setPaddingRelative(this.mContentPaddingStart, childAt.getPaddingTop(), this.mContentPaddingEnd, childAt.getPaddingBottom());
                        }
                    }
                }
            }
        }
    }

    private int getDisplayHeight() {
        if (this.mSizePoint.y == 0) {
            getDisplay().getRealSize(this.mSizePoint);
        }
        return this.mSizePoint.y;
    }

    public QuickQSPanel getQuickQSPanel() {
        return this.mQuickQSPanel;
    }

    @Override // com.android.systemui.tuner.TunerService.Tunable
    public void onTuningChanged(String str, String str2) {
        ToggleSliderView toggleSliderView;
        if ("qs_show_brightness".equals(str) && (toggleSliderView = this.mBrightnessView) != null) {
            updateViewVisibilityForTuningValue(toggleSliderView, str2);
        }
    }

    private void updateViewVisibilityForTuningValue(View view, String str) {
        view.setVisibility(TunerService.parseIntegerSwitch(str, true) ? 0 : 8);
    }

    public void setBrightnessListening(boolean z) {
        MiuiBrightnessController miuiBrightnessController = this.mBrightnessController;
        if (miuiBrightnessController != null) {
            if (z) {
                miuiBrightnessController.registerCallbacks();
            } else {
                miuiBrightnessController.unregisterCallbacks();
            }
        }
    }

    public int getMinHeight() {
        int height = this.mHeader.getHeight();
        return this.mShowQsPanel ? height + this.mQuickQSPanel.getHeight() + this.mQSFooterBundle.getHeight() : height;
    }

    public void updateQSDataUsage(boolean z) {
        if (!z) {
            QSFooterDataUsage qSFooterDataUsage = this.mDataUsage;
            if (qSFooterDataUsage != null) {
                this.mQSFooterBundle.removeView(qSFooterDataUsage);
                this.mDataUsage.setQSContainer(null);
                this.mDataUsage = null;
            }
        } else if (this.mDataUsage == null) {
            QSFooterDataUsage qSFooterDataUsage2 = (QSFooterDataUsage) this.mInjectionInflater.injectable(LayoutInflater.from(getContext()).cloneInContext(new ContextThemeWrapper(getContext(), C0022R$style.qs_theme))).inflate(C0017R$layout.qs_footer_data_usage, this.mQSFooterBundle, false);
            this.mDataUsage = qSFooterDataUsage2;
            this.mQSFooterBundle.addView(qSFooterDataUsage2, 0);
            this.mDataUsage.setElevation(50.0f);
            this.mDataUsage.setQSContainer(this);
        }
        updateExpansion(true);
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mTunerService.addTunable(this, "qs_show_brightness");
        BrightnessMirrorController brightnessMirrorController = this.mBrightnessMirrorController;
        if (brightnessMirrorController != null) {
            brightnessMirrorController.addCallback((BrightnessMirrorController.BrightnessMirrorListener) this);
        }
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mTunerService.removeTunable(this);
        BrightnessMirrorController brightnessMirrorController = this.mBrightnessMirrorController;
        if (brightnessMirrorController != null) {
            brightnessMirrorController.removeCallback((BrightnessMirrorController.BrightnessMirrorListener) this);
        }
    }

    @Override // com.android.systemui.statusbar.policy.BrightnessMirrorController.BrightnessMirrorListener
    public void onBrightnessMirrorReinflated(View view) {
        updateBrightnessMirror();
    }

    public void setBrightnessMirror(BrightnessMirrorController brightnessMirrorController) {
        BrightnessMirrorController brightnessMirrorController2 = this.mBrightnessMirrorController;
        if (brightnessMirrorController2 != null) {
            brightnessMirrorController2.removeCallback((BrightnessMirrorController.BrightnessMirrorListener) this);
        }
        this.mBrightnessMirrorController = brightnessMirrorController;
        if (brightnessMirrorController != null) {
            brightnessMirrorController.addCallback((BrightnessMirrorController.BrightnessMirrorListener) this);
        }
        updateBrightnessMirror();
    }

    public void updateBrightnessMirror() {
        if (this.mBrightnessMirrorController != null) {
            ToggleSliderView toggleSliderView = (ToggleSliderView) findViewById(C0015R$id.brightness_slider);
            toggleSliderView.setMirror((ToggleSliderView) this.mBrightnessMirrorController.getMirror().findViewById(C0015R$id.brightness_slider));
            toggleSliderView.setMirrorController(this.mBrightnessMirrorController);
        }
    }

    public void setShowQSPanel(boolean z) {
        this.mShowQsPanel = z;
        this.mQSContent.setVisibility(z ? 0 : 8);
    }

    public void setDetailAnimatedViews(View... viewArr) {
        this.mExtraAnimatedViews = viewArr;
        setupAnimatedViews();
    }

    private void setupAnimatedViews() {
        List<View> asList = Arrays.asList(this.mQSFooterBundle, this.mDragHandle, this.mQuickQSPanel, this.mQSPanel, this.mBackground);
        if (this.mExtraAnimatedViews != null) {
            ArrayList arrayList = new ArrayList(asList);
            arrayList.addAll(Arrays.asList(this.mExtraAnimatedViews));
            asList = arrayList;
        }
        ArrayList arrayList2 = new ArrayList(asList);
        arrayList2.add(this.mHeader);
        this.mQSDetail.setAnimatedViews(asList);
        this.mQSCustomizer.setAnimatedViews(arrayList2);
    }

    public void updateIndicator(float f) {
        if (this.mIndicatorProgress != f) {
            this.mIndicatorProgress = f;
            ValueAnimator valueAnimator = this.mCaretAnimator;
            if (valueAnimator != null && valueAnimator.isRunning()) {
                this.mCaretAnimator.cancel();
                this.mIndicatorDrawable.setCaretProgress(0.0f);
            }
            ValueAnimator ofFloat = ValueAnimator.ofFloat(0.0f, f);
            this.mCaretAnimator = ofFloat;
            ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                /* class com.android.systemui.qs.$$Lambda$QSContainerImpl$304nFTeTD2pE08Ua4zXfWxj8rG8 */

                public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                    QSContainerImpl.this.lambda$updateIndicator$2$QSContainerImpl(valueAnimator);
                }
            });
            this.mCaretAnimator.setDuration(200L);
            this.mCaretAnimator.setInterpolator(this.mCaretInterpolator);
            this.mCaretAnimator.start();
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$updateIndicator$2 */
    public /* synthetic */ void lambda$updateIndicator$2$QSContainerImpl(ValueAnimator valueAnimator) {
        this.mIndicatorDrawable.setCaretProgress(((Float) valueAnimator.getAnimatedValue()).floatValue());
    }

    public void updateDataUsageInfo() {
        QSFooterDataUsage qSFooterDataUsage = this.mDataUsage;
        if (qSFooterDataUsage != null) {
            qSFooterDataUsage.updateDataUsageInfo();
        }
    }

    private int getDataUsageHeight() {
        QSFooterDataUsage qSFooterDataUsage = this.mDataUsage;
        if (qSFooterDataUsage == null || !qSFooterDataUsage.isAvailable()) {
            return 0;
        }
        return this.mDataUsage.getHeight();
    }
}
