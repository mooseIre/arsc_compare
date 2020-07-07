package com.android.systemui.qs;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Trace;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.R;
import com.android.systemui.qs.customize.QSCustomizer;
import com.android.systemui.settings.BrightnessController;
import com.android.systemui.settings.ToggleSliderView;
import com.android.systemui.statusbar.policy.BrightnessMirrorController;
import com.android.systemui.tuner.TunerService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class QSContainerImpl extends FrameLayout implements TunerService.Tunable {
    protected View mBackground;
    private BrightnessController mBrightnessController;
    private BrightnessMirrorController mBrightnessMirrorController;
    private ToggleSliderView mBrightnessView;
    private ValueAnimator mCaretAnimator;
    /* access modifiers changed from: private */
    public CaretDrawable mCaretDrawable;
    private Interpolator mCaretInterpolator;
    protected View mContent;
    private QSFooterDataUsage mDataUsageBar;
    private ImageView mExpandIndicator;
    private View[] mExtraAnimatedViews;
    private int mFooterChildCount = 0;
    private int mGutterHeight;
    protected QuickStatusBarHeader mHeader;
    private int mHeightOverride = -1;
    protected float mIndicarotProgress;
    private boolean mIsLandscape;
    private boolean mListening;
    private int mPanelWidth;
    protected View mQSContainer;
    private QSCustomizer mQSCustomizer;
    private QSDetail mQSDetail;
    private QSFooter mQSFooter;
    private View mQSFooterBundle;
    protected LinearLayout mQSFooterContainer;
    protected QSPanel mQSPanel;
    protected float mQsExpansion;
    protected QuickQSPanel mQuickQsPanel;
    private final Point mSizePoint = new Point();

    public boolean performClick() {
        return true;
    }

    public QSContainerImpl(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        ((TunerService) Dependency.get(TunerService.class)).addTunable(this, "qs_show_brightness");
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        Resources resources = getResources();
        this.mHeader = (QuickStatusBarHeader) findViewById(R.id.header);
        this.mContent = findViewById(R.id.qs_content);
        this.mQSContainer = findViewById(R.id.qs_container);
        this.mBackground = findViewById(R.id.qs_background);
        QuickQSPanel quickQSPanel = (QuickQSPanel) findViewById(R.id.quick_qs_panel);
        this.mQuickQsPanel = quickQSPanel;
        quickQSPanel.setVisibility(resources.getBoolean(R.bool.config_showQuickSettingsRow) ? 0 : 8);
        this.mQSPanel = (QSPanel) findViewById(R.id.quick_settings_panel);
        this.mQSFooterContainer = (LinearLayout) findViewById(R.id.qs_footer_container);
        this.mQSFooterBundle = findViewById(R.id.qs_footer_bundle);
        this.mBrightnessView = (ToggleSliderView) findViewById(R.id.qs_brightness);
        this.mBrightnessController = new BrightnessController(getContext(), this.mBrightnessView);
        this.mExpandIndicator = (ImageView) findViewById(R.id.qs_expand_indicator);
        CaretDrawable caretDrawable = new CaretDrawable(getContext());
        this.mCaretDrawable = caretDrawable;
        this.mExpandIndicator.setImageDrawable(caretDrawable);
        this.mCaretInterpolator = AnimationUtils.loadInterpolator(getContext(), 17563661);
        this.mQSDetail = (QSDetail) findViewById(R.id.qs_detail);
        this.mQSCustomizer = (QSCustomizer) findViewById(R.id.qs_customize);
        loadDimens();
        setClickable(false);
        setImportantForAccessibility(2);
        setupAnimatedViews();
    }

    private void loadDimens() {
        this.mGutterHeight = getResources().getDimensionPixelSize(R.dimen.qs_gutter_height);
        this.mPanelWidth = getResources().getDimensionPixelSize(R.dimen.notification_panel_width);
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int i, int i2) {
        int i3;
        Trace.traceBegin(8, "onMeasure QsContainer");
        if (this.mIsLandscape && (i3 = this.mPanelWidth) > 0) {
            i = View.MeasureSpec.makeMeasureSpec(i3, 1073741824);
        }
        super.onMeasure(i, i2);
        Trace.traceEnd(8);
        Trace.traceBegin(8, "onMeasure QsContainer children");
        int makeMeasureSpec = View.MeasureSpec.makeMeasureSpec(getDisplayHeight(), 1073741824);
        this.mQSCustomizer.measure(i, makeMeasureSpec);
        this.mQSDetail.measure(i, makeMeasureSpec);
        Trace.traceEnd(8);
    }

    public void updateQSDataUsage(boolean z) {
        if (z) {
            if (this.mDataUsageBar == null) {
                QSFooterDataUsage qSFooterDataUsage = (QSFooterDataUsage) LayoutInflater.from(getContext()).inflate(R.layout.qs_footer_data_usage, this.mQSFooterContainer, false);
                this.mDataUsageBar = qSFooterDataUsage;
                this.mQSFooterContainer.addView(qSFooterDataUsage);
                this.mDataUsageBar.setQSContainer(this);
            }
        } else if (this.mDataUsageBar != null) {
            if (this.mQSFooter == null) {
                this.mQSFooterContainer.setVisibility(8);
            }
            this.mQSFooterContainer.removeView(this.mDataUsageBar);
            this.mDataUsageBar.setQSContainer((QSContainerImpl) null);
            this.mDataUsageBar = null;
            updateFooter();
        }
    }

    public void updateFooter() {
        int childCount = this.mQSFooterContainer.getChildCount();
        QSFooterDataUsage qSFooterDataUsage = this.mDataUsageBar;
        if (qSFooterDataUsage != null && !qSFooterDataUsage.isAvailable()) {
            childCount--;
        }
        if (this.mFooterChildCount != childCount) {
            this.mFooterChildCount = childCount;
            if (childCount > 0) {
                this.mQSFooterContainer.setVisibility(0);
            } else {
                this.mQSFooterContainer.setVisibility(8);
            }
        }
    }

    public boolean isDataUsageAvailable() {
        return this.mDataUsageBar != null;
    }

    public void updateDataUsageInfo() {
        QSFooterDataUsage qSFooterDataUsage = this.mDataUsageBar;
        if (qSFooterDataUsage != null) {
            qSFooterDataUsage.updateDataUsageInfo();
        }
    }

    public QSFooter getQSFooter() {
        return this.mQSFooter;
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        this.mIsLandscape = configuration.orientation == 2;
        BrightnessMirrorController brightnessMirrorController = this.mBrightnessMirrorController;
        if (brightnessMirrorController != null) {
            setBrightnessMirror(brightnessMirrorController);
        }
        this.mSizePoint.set(0, 0);
        loadDimens();
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ((TunerService) Dependency.get(TunerService.class)).removeTunable(this);
    }

    public void onTuningChanged(String str, String str2) {
        if ("qs_show_brightness".equals(str)) {
            this.mBrightnessView.setVisibility((str2 == null || Integer.parseInt(str2) != 0) ? 0 : 8);
        }
    }

    public void onRtlPropertiesChanged(int i) {
        super.onRtlPropertiesChanged(i);
        this.mBrightnessView.setLayoutDirection(i);
    }

    public void setHeightOverride(int i) {
        if (this.mHeightOverride != i) {
            this.mHeightOverride = i;
            updateExpansion();
        }
    }

    public boolean isQSFullyCollapsed() {
        return this.mQsExpansion <= 0.0f;
    }

    public void setExpansion(float f) {
        float f2 = 1.0f;
        if (this.mQsExpansion - f > 0.002f && f != 0.0f) {
            f2 = -1.0f;
        } else if (f - this.mQsExpansion <= 0.002f || f == 1.0f) {
            f2 = 0.0f;
        }
        this.mQsExpansion = f;
        updateIndicator(f2);
        updateExpansion();
    }

    public void updateIndicator(float f) {
        if (this.mIndicarotProgress != f) {
            this.mIndicarotProgress = f;
            ValueAnimator valueAnimator = this.mCaretAnimator;
            if (valueAnimator != null && valueAnimator.isRunning()) {
                this.mCaretAnimator.cancel();
                this.mCaretDrawable.setCaretProgress(0.0f);
            }
            ValueAnimator ofFloat = ValueAnimator.ofFloat(new float[]{0.0f, f});
            this.mCaretAnimator = ofFloat;
            ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    QSContainerImpl.this.mCaretDrawable.setCaretProgress(((Float) valueAnimator.getAnimatedValue()).floatValue());
                }
            });
            this.mCaretAnimator.setDuration(200);
            this.mCaretAnimator.setInterpolator(this.mCaretInterpolator);
            this.mCaretAnimator.start();
        }
    }

    public void updateExpansion() {
        int calculateContainerHeight = calculateContainerHeight();
        setBottom(getTop() + calculateContainerHeight);
        View view = this.mContent;
        view.setBottom(view.getTop() + calculateContainerHeight);
        View view2 = this.mQSContainer;
        view2.setBottom(view2.getTop() + calculateContainerHeight);
        View view3 = this.mBackground;
        view3.setBottom(((view3.getTop() + calculateContainerHeight) - this.mHeader.getHeight()) - this.mQSFooterBundle.getHeight());
        float bottom = (float) ((getBottom() - this.mQSFooterBundle.getBottom()) - this.mHeader.getHeight());
        this.mQSFooterContainer.setTranslationY(bottom);
        this.mQSFooterBundle.setTranslationY(bottom);
    }

    /* access modifiers changed from: protected */
    public int calculateContainerHeight() {
        int i = this.mHeightOverride;
        if (i == -1) {
            i = this.mContent.getMeasuredHeight();
        }
        int qsMinExpansionHeight = getQsMinExpansionHeight();
        return Math.round((this.mQsExpansion * ((float) (i - qsMinExpansionHeight))) + ((float) qsMinExpansionHeight));
    }

    public int getQsMinExpansionHeight() {
        int height = this.mHeader.getHeight() + this.mQuickQsPanel.getHeight() + this.mQSFooterBundle.getHeight();
        return this.mQSFooterContainer.isShown() ? height + this.mQSFooterContainer.getHeight() : height;
    }

    public void setGutterEnabled(boolean z) {
        if (z != (this.mGutterHeight != 0)) {
            if (z) {
                this.mGutterHeight = getContext().getResources().getDimensionPixelSize(R.dimen.qs_gutter_height);
            } else {
                this.mGutterHeight = 0;
            }
            updateExpansion();
        }
    }

    public void setBrightnessMirror(BrightnessMirrorController brightnessMirrorController) {
        this.mBrightnessMirrorController = brightnessMirrorController;
        this.mBrightnessView.setMirror((ToggleSliderView) brightnessMirrorController.getMirror().findViewById(R.id.brightness_slider));
        this.mBrightnessView.setMirrorController(brightnessMirrorController);
    }

    public void setListening(boolean z) {
        if (this.mListening != z) {
            this.mListening = z;
            setBrightnessListening(z);
            QSFooter qSFooter = this.mQSFooter;
            if (qSFooter != null) {
                qSFooter.setListening(this.mListening);
            }
        }
    }

    public void setBrightnessListening(boolean z) {
        if (z) {
            this.mBrightnessController.registerCallbacks();
        } else {
            this.mBrightnessController.unregisterCallbacks();
        }
    }

    public View getBrightnessView() {
        return this.mBrightnessView;
    }

    public View getExpandIndicator() {
        return this.mExpandIndicator;
    }

    public void setDetailAnimatedViews(View... viewArr) {
        this.mExtraAnimatedViews = viewArr;
        setupAnimatedViews();
    }

    private int getDisplayHeight() {
        if (this.mSizePoint.y == 0) {
            getDisplay().getRealSize(this.mSizePoint);
        }
        return this.mSizePoint.y;
    }

    private void setupAnimatedViews() {
        List asList = Arrays.asList(new View[]{this.mQSFooterBundle, this.mQSFooterContainer, this.mQuickQsPanel, this.mQSPanel});
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
}
