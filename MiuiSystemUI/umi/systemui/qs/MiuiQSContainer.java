package com.android.systemui.qs;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
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
import com.android.systemui.C0011R$dimen;
import com.android.systemui.C0014R$id;
import com.android.systemui.C0016R$layout;
import com.android.systemui.C0021R$style;
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
import kotlin.TypeCastException;
import kotlin.collections.CollectionsKt__CollectionsKt;
import kotlin.jvm.internal.Intrinsics;
import kotlin.math.MathKt__MathJVMKt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressLint({"ViewConstructor"})
/* compiled from: MiuiQSContainer.kt */
public class MiuiQSContainer extends FrameLayout implements TunerService.Tunable, BrightnessMirrorController.BrightnessMirrorListener {
    private static final FloatPropertyCompat<MiuiQSContainer> BACKGROUND_BOTTOM = new MiuiQSContainer$Companion$BACKGROUND_BOTTOM$1("backgroundBottom");
    private static final PhysicsAnimator.SpringConfig BACKGROUND_SPRING = new PhysicsAnimator.SpringConfig(1500.0f, 0.75f);
    private boolean animateBottomOnNextLayout;
    private int backgroundBottom;
    private View backgroundGradient;
    private MiuiBrightnessController brightnessController;
    private BrightnessMirrorController brightnessMirrorController;
    private ToggleSliderView brightnessView;
    private final BroadcastDispatcher broadcastDispatcher;
    private ValueAnimator caretAnimator;
    private Interpolator caretInterpolator;
    private boolean contentAdded;
    private int contentPaddingEnd;
    private int contentPaddingStart;
    private QSFooterDataUsage dataUsage;
    private ImageView dragHandle;
    private View[] extraAnimatedViews;
    @Nullable
    private QSFooter footer;
    private ViewGroup footerBundle;
    @NotNull
    private MiuiNotificationShadeHeader header;
    private int heightOverride;
    private IndicatorDrawable indicatorDrawable;
    private float indicatorProgress;
    private final InjectionInflationController inflationController;
    private LayoutInflater layoutInflater;
    private View qsBackground;
    private QSContent qsContent;
    @Nullable
    private MiuiQSCustomizer qsCustomizer;
    private boolean qsDataUsageEnabled;
    @Nullable
    private MiuiQSDetail qsDetail;
    private boolean qsDisabled;
    private float qsExpansion;
    @Nullable
    private QSPanel qsPanel;
    @Nullable
    private View qsPanelScrollView;
    @Nullable
    private QuickQSPanel quickQSPanel;
    private boolean showQsPanel;
    private int sideMargins;
    private final Point sizePoint;
    private View statusBarBackground;
    private final TunerService tunerService;

    public boolean performClick() {
        return true;
    }

    public final void updateResources() {
        updateResources$default(this, false, 1, null);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public MiuiQSContainer(@Nullable Context context, @Nullable AttributeSet attributeSet, @NotNull BroadcastDispatcher broadcastDispatcher2, @NotNull InjectionInflationController injectionInflationController, @NotNull TunerService tunerService2) {
        super(context, attributeSet);
        Intrinsics.checkParameterIsNotNull(broadcastDispatcher2, "broadcastDispatcher");
        Intrinsics.checkParameterIsNotNull(injectionInflationController, "inflationController");
        Intrinsics.checkParameterIsNotNull(tunerService2, "tunerService");
        if (context != null) {
            this.broadcastDispatcher = broadcastDispatcher2;
            this.inflationController = injectionInflationController;
            this.tunerService = tunerService2;
            this.sizePoint = new Point();
            this.backgroundBottom = -1;
            this.heightOverride = -1;
            this.contentPaddingStart = -1;
            this.contentPaddingEnd = -1;
            return;
        }
        Intrinsics.throwNpe();
        throw null;
    }

    @Nullable
    public final QSPanel getQsPanel() {
        return this.qsPanel;
    }

    @Nullable
    public final QuickQSPanel getQuickQSPanel() {
        return this.quickQSPanel;
    }

    @Nullable
    public final MiuiQSDetail getQsDetail() {
        return this.qsDetail;
    }

    @Nullable
    public final MiuiQSCustomizer getQsCustomizer() {
        return this.qsCustomizer;
    }

    @Nullable
    public final View getQsPanelScrollView() {
        return this.qsPanelScrollView;
    }

    @Nullable
    public final QSFooter getFooter() {
        return this.footer;
    }

    @NotNull
    public final MiuiNotificationShadeHeader getHeader() {
        MiuiNotificationShadeHeader miuiNotificationShadeHeader = this.header;
        if (miuiNotificationShadeHeader != null) {
            return miuiNotificationShadeHeader;
        }
        Intrinsics.throwUninitializedPropertyAccessException("header");
        throw null;
    }

    private final float getQsExpansion() {
        if (Float.isNaN(this.qsExpansion)) {
            return 0.0f;
        }
        return this.qsExpansion;
    }

    private final LayoutInflater getLayoutInflater() {
        if (this.layoutInflater == null) {
            this.layoutInflater = this.inflationController.injectable(LayoutInflater.from(getContext()).cloneInContext(new ContextThemeWrapper(getContext(), C0021R$style.qs_theme)));
        }
        return this.layoutInflater;
    }

    private final int getDataUsageHeight() {
        QSFooterDataUsage qSFooterDataUsage = this.dataUsage;
        if (qSFooterDataUsage != null) {
            if (qSFooterDataUsage == null) {
                Intrinsics.throwNpe();
                throw null;
            } else if (qSFooterDataUsage.isAvailable()) {
                QSFooterDataUsage qSFooterDataUsage2 = this.dataUsage;
                if (qSFooterDataUsage2 != null) {
                    return qSFooterDataUsage2.getHeight();
                }
                Intrinsics.throwNpe();
                throw null;
            }
        }
        return 0;
    }

    public final boolean getContentAdded() {
        return this.contentAdded;
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        View findViewById = findViewById(C0014R$id.header);
        Intrinsics.checkExpressionValueIsNotNull(findViewById, "findViewById(R.id.header)");
        MiuiNotificationShadeHeader miuiNotificationShadeHeader = (MiuiNotificationShadeHeader) findViewById;
        this.header = miuiNotificationShadeHeader;
        if (miuiNotificationShadeHeader != null) {
            miuiNotificationShadeHeader.setQSContainer(this);
            this.caretInterpolator = Interpolators.FAST_OUT_SLOW_IN;
            View findViewById2 = findViewById(C0014R$id.quick_settings_status_bar_background);
            Intrinsics.checkExpressionValueIsNotNull(findViewById2, "findViewById(R.id.quick_…gs_status_bar_background)");
            this.statusBarBackground = findViewById2;
            View findViewById3 = findViewById(C0014R$id.quick_settings_gradient_view);
            Intrinsics.checkExpressionValueIsNotNull(findViewById3, "findViewById(R.id.quick_settings_gradient_view)");
            this.backgroundGradient = findViewById3;
            setImportantForAccessibility(2);
            return;
        }
        Intrinsics.throwUninitializedPropertyAccessException("header");
        throw null;
    }

    public final void addQSContent() {
        if (!this.contentAdded) {
            LayoutInflater layoutInflater2 = getLayoutInflater();
            if (layoutInflater2 != null) {
                layoutInflater2.inflate(C0016R$layout.qs_content, (ViewGroup) this, true);
                LayoutInflater layoutInflater3 = getLayoutInflater();
                if (layoutInflater3 != null) {
                    layoutInflater3.inflate(C0016R$layout.qs_detail, (ViewGroup) this, true);
                    LayoutInflater layoutInflater4 = getLayoutInflater();
                    if (layoutInflater4 != null) {
                        layoutInflater4.inflate(C0016R$layout.qs_customize_panel, (ViewGroup) this, true);
                        this.qsContent = (QSContent) findViewById(C0014R$id.qs_content);
                        this.qsDetail = (MiuiQSDetail) findViewById(C0014R$id.qs_detail);
                        this.qsCustomizer = (MiuiQSCustomizer) findViewById(C0014R$id.qs_customize);
                        this.qsPanel = (QSPanel) findViewById(C0014R$id.quick_settings_panel);
                        this.quickQSPanel = (QuickQSPanel) findViewById(C0014R$id.quick_qs_panel);
                        this.brightnessView = (ToggleSliderView) findViewById(C0014R$id.brightness_slider);
                        this.brightnessController = new MiuiBrightnessController(getContext(), this.brightnessView, this.broadcastDispatcher);
                        View findViewById = findViewById(C0014R$id.qs_footer);
                        Intrinsics.checkExpressionValueIsNotNull(findViewById, "findViewById(R.id.qs_footer)");
                        if (findViewById instanceof QSFooter) {
                            this.footer = (QSFooter) findViewById;
                        }
                        this.footerBundle = (ViewGroup) findViewById(C0014R$id.qs_footer_bundle);
                        this.qsPanelScrollView = findViewById(C0014R$id.expanded_qs_scroll_view);
                        this.qsDetail = (MiuiQSDetail) findViewById(C0014R$id.qs_detail);
                        this.qsCustomizer = (MiuiQSCustomizer) findViewById(C0014R$id.qs_customize);
                        this.dragHandle = (ImageView) findViewById(C0014R$id.qs_expand_indicator);
                        Context context = getContext();
                        Intrinsics.checkExpressionValueIsNotNull(context, "context");
                        IndicatorDrawable indicatorDrawable2 = new IndicatorDrawable(context);
                        this.indicatorDrawable = indicatorDrawable2;
                        ImageView imageView = this.dragHandle;
                        if (imageView != null) {
                            imageView.setImageDrawable(indicatorDrawable2);
                        }
                        View findViewById2 = findViewById(C0014R$id.quick_settings_background);
                        this.qsBackground = findViewById2;
                        int i = this.backgroundBottom;
                        if (i > 0 && findViewById2 != null) {
                            findViewById2.setBottom(i);
                        }
                        updateResources(true);
                        QuickQSPanel quickQSPanel2 = this.quickQSPanel;
                        if (quickQSPanel2 != null) {
                            quickQSPanel2.setMediaVisibilityChangedListener(new MiuiQSContainer$addQSContent$1(this));
                        }
                        QSPanel qSPanel = this.qsPanel;
                        if (qSPanel != null) {
                            qSPanel.setMediaVisibilityChangedListener(new MiuiQSContainer$addQSContent$2(this));
                        }
                        setupAnimatedViews();
                        setBrightnessMirror(this.brightnessMirrorController);
                        updateQSDataUsage(this.qsDataUsageEnabled);
                        this.contentAdded = true;
                        return;
                    }
                    Intrinsics.throwNpe();
                    throw null;
                }
                Intrinsics.throwNpe();
                throw null;
            }
            Intrinsics.throwNpe();
            throw null;
        }
    }

    public final void removeQSContent() {
        if (this.contentAdded) {
            removeView(this.qsContent);
            removeView(this.qsDetail);
            removeView(this.qsCustomizer);
            this.qsContent = null;
            this.qsDetail = null;
            this.qsCustomizer = null;
            QSPanel qSPanel = this.qsPanel;
            if (qSPanel != null) {
                qSPanel.setMediaVisibilityChangedListener(null);
            }
            this.qsPanel = null;
            QuickQSPanel quickQSPanel2 = this.quickQSPanel;
            if (quickQSPanel2 != null) {
                quickQSPanel2.setMediaVisibilityChangedListener(null);
            }
            this.quickQSPanel = null;
            this.brightnessView = null;
            this.brightnessController = null;
            this.footer = null;
            updateQSDataUsage(false);
            this.footerBundle = null;
            this.qsPanelScrollView = null;
            this.qsDetail = null;
            this.qsCustomizer = null;
            ImageView imageView = this.dragHandle;
            if (imageView != null) {
                imageView.setImageDrawable(null);
            }
            this.dragHandle = null;
            this.indicatorDrawable = null;
            this.qsBackground = null;
            updateResources$default(this, false, 1, null);
            this.contentAdded = false;
        }
    }

    /* access modifiers changed from: private */
    public final void setBackgroundBottom(int i) {
        this.backgroundBottom = i;
        View view = this.qsBackground;
        if (view != null) {
            view.setBottom(i);
        }
    }

    /* access modifiers changed from: private */
    public final float getBackgroundBottom() {
        int i = this.backgroundBottom;
        if (i != -1) {
            return (float) i;
        }
        View view = this.qsBackground;
        if (view == null) {
            return 0.0f;
        }
        if (view != null) {
            return (float) view.getBottom();
        }
        Intrinsics.throwNpe();
        throw null;
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(@NotNull Configuration configuration) {
        Intrinsics.checkParameterIsNotNull(configuration, "newConfig");
        super.onConfigurationChanged(configuration);
        setBackgroundGradientVisibility(configuration);
        updateResources$default(this, false, 1, null);
        this.sizePoint.set(0, 0);
        updateBrightnessMirror();
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int i, int i2) {
        if (this.contentAdded) {
            Resources resources = getResources();
            Intrinsics.checkExpressionValueIsNotNull(resources, "resources");
            Configuration configuration = resources.getConfiguration();
            boolean z = configuration.smallestScreenWidthDp >= 600 || configuration.orientation != 2;
            View view = this.qsPanelScrollView;
            if (view != null) {
                ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                if (layoutParams != null) {
                    ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) layoutParams;
                    int displayHeight = ((getDisplayHeight() - marginLayoutParams.topMargin) - marginLayoutParams.bottomMargin) - getPaddingBottom();
                    if (z) {
                        displayHeight -= getResources().getDimensionPixelSize(C0011R$dimen.navigation_bar_height);
                    }
                    int paddingLeft = getPaddingLeft() + getPaddingRight() + marginLayoutParams.leftMargin + marginLayoutParams.rightMargin;
                    int childMeasureSpec = ViewGroup.getChildMeasureSpec(i, paddingLeft, marginLayoutParams.width);
                    View view2 = this.qsPanelScrollView;
                    if (view2 != null) {
                        view2.measure(childMeasureSpec, View.MeasureSpec.makeMeasureSpec(displayHeight, Integer.MIN_VALUE));
                        View view3 = this.qsPanelScrollView;
                        if (view3 != null) {
                            int measuredWidth = view3.getMeasuredWidth() + paddingLeft;
                            int i3 = marginLayoutParams.topMargin + marginLayoutParams.bottomMargin;
                            View view4 = this.qsPanelScrollView;
                            if (view4 != null) {
                                int measuredHeight = i3 + view4.getMeasuredHeight();
                                ViewGroup viewGroup = this.footerBundle;
                                if (viewGroup != null) {
                                    super.onMeasure(View.MeasureSpec.makeMeasureSpec(measuredWidth, 1073741824), View.MeasureSpec.makeMeasureSpec(measuredHeight + viewGroup.getMeasuredHeight() + getPaddingBottom(), 1073741824));
                                    MiuiQSCustomizer miuiQSCustomizer = this.qsCustomizer;
                                    if (miuiQSCustomizer != null) {
                                        miuiQSCustomizer.measure(i, View.MeasureSpec.makeMeasureSpec(getDisplayHeight(), 1073741824));
                                    }
                                    MiuiQSDetail miuiQSDetail = this.qsDetail;
                                    if (miuiQSDetail != null) {
                                        miuiQSDetail.measure(i, View.MeasureSpec.makeMeasureSpec(getDisplayHeight(), 1073741824));
                                        return;
                                    }
                                    return;
                                }
                                Intrinsics.throwNpe();
                                throw null;
                            }
                            Intrinsics.throwNpe();
                            throw null;
                        }
                        Intrinsics.throwNpe();
                        throw null;
                    }
                    Intrinsics.throwNpe();
                    throw null;
                }
                throw new TypeCastException("null cannot be cast to non-null type android.view.ViewGroup.MarginLayoutParams");
            }
            Intrinsics.throwNpe();
            throw null;
        }
        super.onMeasure(i, i2);
    }

    /* access modifiers changed from: protected */
    public void measureChildWithMargins(@NotNull View view, int i, int i2, int i3, int i4) {
        Intrinsics.checkParameterIsNotNull(view, "child");
        if (view != this.qsPanelScrollView) {
            super.measureChildWithMargins(view, i, i2, i3, i4);
        }
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        updateExpansion(this.animateBottomOnNextLayout);
        this.animateBottomOnNextLayout = false;
    }

    public final void disable(int i, int i2, boolean z) {
        boolean z2 = true;
        int i3 = 0;
        if ((i2 & 1) == 0) {
            z2 = false;
        }
        if (z2 != this.qsDisabled) {
            this.qsDisabled = z2;
            Resources resources = getResources();
            Intrinsics.checkExpressionValueIsNotNull(resources, "resources");
            Configuration configuration = resources.getConfiguration();
            Intrinsics.checkExpressionValueIsNotNull(configuration, "resources.configuration");
            setBackgroundGradientVisibility(configuration);
            View view = this.qsBackground;
            if (view != null) {
                if (this.qsDisabled) {
                    i3 = 8;
                }
                view.setVisibility(i3);
            }
        }
    }

    public static /* synthetic */ void updateResources$default(MiuiQSContainer miuiQSContainer, boolean z, int i, Object obj) {
        if (obj == null) {
            if ((i & 1) != 0) {
                z = false;
            }
            miuiQSContainer.updateResources(z);
            return;
        }
        throw new UnsupportedOperationException("Super calls with default arguments not supported in this target, function: updateResources");
    }

    public final void updateResources(boolean z) {
        MiuiNotificationShadeHeader miuiNotificationShadeHeader = this.header;
        if (miuiNotificationShadeHeader != null) {
            int height = miuiNotificationShadeHeader.getHeight();
            View view = this.qsPanelScrollView;
            if (view != null) {
                ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                if (layoutParams != null) {
                    FrameLayout.LayoutParams layoutParams2 = (FrameLayout.LayoutParams) layoutParams;
                    layoutParams2.topMargin = height;
                    view.setLayoutParams(layoutParams2);
                } else {
                    throw new TypeCastException("null cannot be cast to non-null type android.widget.FrameLayout.LayoutParams");
                }
            }
            QuickQSPanel quickQSPanel2 = this.quickQSPanel;
            if (quickQSPanel2 != null) {
                ViewGroup.LayoutParams layoutParams3 = quickQSPanel2.getLayoutParams();
                if (layoutParams3 != null) {
                    FrameLayout.LayoutParams layoutParams4 = (FrameLayout.LayoutParams) layoutParams3;
                    layoutParams4.topMargin = height;
                    quickQSPanel2.setLayoutParams(layoutParams4);
                } else {
                    throw new TypeCastException("null cannot be cast to non-null type android.widget.FrameLayout.LayoutParams");
                }
            }
            MiuiQSDetail miuiQSDetail = this.qsDetail;
            if (miuiQSDetail != null) {
                miuiQSDetail.updateHeaderHeight(height);
            }
            MiuiQSCustomizer miuiQSCustomizer = this.qsCustomizer;
            if (miuiQSCustomizer != null) {
                miuiQSCustomizer.updateResources(height);
            }
            this.sideMargins = getResources().getDimensionPixelSize(C0011R$dimen.notification_side_paddings);
            this.contentPaddingStart = getResources().getDimensionPixelSize(17105356);
            int dimensionPixelSize = getResources().getDimensionPixelSize(17105355);
            boolean z2 = dimensionPixelSize != this.contentPaddingEnd;
            this.contentPaddingEnd = dimensionPixelSize;
            if (z2 || z) {
                updatePaddingAndMargins();
                return;
            }
            return;
        }
        Intrinsics.throwUninitializedPropertyAccessException("header");
        throw null;
    }

    public final void setHeightOverride(int i) {
        this.heightOverride = i;
        updateExpansion$default(this, false, 1, null);
    }

    public static /* synthetic */ void updateExpansion$default(MiuiQSContainer miuiQSContainer, boolean z, int i, Object obj) {
        if (obj == null) {
            if ((i & 1) != 0) {
                z = false;
            }
            miuiQSContainer.updateExpansion(z);
            return;
        }
        throw new UnsupportedOperationException("Super calls with default arguments not supported in this target, function: updateExpansion");
    }

    public final void updateExpansion(boolean z) {
        View view;
        int calculateContainerHeight = calculateContainerHeight();
        setBottom(getTop() + calculateContainerHeight);
        View view2 = this.qsPanelScrollView;
        if (!(view2 == null || (view = this.qsBackground) == null)) {
            view.setTop(view2.getTop());
        }
        ViewGroup viewGroup = this.footerBundle;
        if (viewGroup != null) {
            viewGroup.setTranslationY(((float) calculateContainerHeight) - ((float) viewGroup.getHeight()));
            updateBackgroundBottom((calculateContainerHeight - viewGroup.getHeight()) + getDataUsageHeight(), z);
        }
    }

    private final void updateBackgroundBottom(int i, boolean z) {
        FloatPropertyCompat<MiuiQSContainer> floatPropertyCompat = BACKGROUND_BOTTOM;
        PhysicsAnimator instance = PhysicsAnimator.Companion.getInstance(this);
        if (instance.isPropertyAnimating(floatPropertyCompat) || z) {
            floatPropertyCompat.setValue(this, floatPropertyCompat.getValue(this));
            instance.spring(floatPropertyCompat, (float) i, BACKGROUND_SPRING);
            instance.start();
            return;
        }
        floatPropertyCompat.setValue(this, (float) i);
    }

    private final int calculateContainerHeight() {
        int i = this.heightOverride;
        if (i == -1) {
            i = getHeight();
        }
        int minHeight = getMinHeight();
        return MathKt__MathJVMKt.roundToInt(getQsExpansion() * ((float) (i - minHeight))) + minHeight;
    }

    private final void setBackgroundGradientVisibility(Configuration configuration) {
        int i = 4;
        if (configuration.orientation == 2) {
            View view = this.backgroundGradient;
            if (view != null) {
                view.setVisibility(4);
                View view2 = this.statusBarBackground;
                if (view2 != null) {
                    view2.setVisibility(4);
                } else {
                    Intrinsics.throwUninitializedPropertyAccessException("statusBarBackground");
                    throw null;
                }
            } else {
                Intrinsics.throwUninitializedPropertyAccessException("backgroundGradient");
                throw null;
            }
        } else {
            View view3 = this.backgroundGradient;
            if (view3 != null) {
                if (!this.qsDisabled) {
                    i = 0;
                }
                view3.setVisibility(i);
                View view4 = this.statusBarBackground;
                if (view4 != null) {
                    view4.setVisibility(0);
                } else {
                    Intrinsics.throwUninitializedPropertyAccessException("statusBarBackground");
                    throw null;
                }
            } else {
                Intrinsics.throwUninitializedPropertyAccessException("backgroundGradient");
                throw null;
            }
        }
    }

    public final void setExpansion(float f) {
        float f2 = 1.0f;
        if (getQsExpansion() - f > 0.002f && f != 0.0f) {
            f2 = -1.0f;
        } else if (f - getQsExpansion() <= 0.002f || f == 1.0f) {
            f2 = 0.0f;
        }
        updateIndicator(f2);
        View view = this.qsPanelScrollView;
        if (!(view == null || this.qsPanel == null)) {
            if (view != null) {
                int bottom = view.getBottom();
                QuickQSPanel quickQSPanel2 = this.quickQSPanel;
                if (quickQSPanel2 != null) {
                    int bottom2 = bottom - quickQSPanel2.getBottom();
                    View view2 = this.qsPanelScrollView;
                    if (view2 != null) {
                        view2.setTranslationY(((float) bottom2) * (f - ((float) 1)));
                    } else {
                        Intrinsics.throwNpe();
                        throw null;
                    }
                } else {
                    Intrinsics.throwNpe();
                    throw null;
                }
            } else {
                Intrinsics.throwNpe();
                throw null;
            }
        }
        this.qsExpansion = f;
        updateExpansion$default(this, false, 1, null);
    }

    private final void updatePaddingAndMargins() {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = getChildAt(i);
            View view = this.statusBarBackground;
            if (view != null) {
                if (childAt != view) {
                    View view2 = this.backgroundGradient;
                    if (view2 == null) {
                        Intrinsics.throwUninitializedPropertyAccessException("backgroundGradient");
                        throw null;
                    } else if (childAt != view2) {
                        MiuiNotificationShadeHeader miuiNotificationShadeHeader = this.header;
                        if (miuiNotificationShadeHeader == null) {
                            Intrinsics.throwUninitializedPropertyAccessException("header");
                            throw null;
                        } else if (childAt != miuiNotificationShadeHeader) {
                            MiuiQSCustomizer miuiQSCustomizer = this.qsCustomizer;
                            if (childAt != miuiQSCustomizer) {
                                MiuiQSDetail miuiQSDetail = this.qsDetail;
                                if (childAt != miuiQSDetail) {
                                    Intrinsics.checkExpressionValueIsNotNull(childAt, "view");
                                    ViewGroup.LayoutParams layoutParams = childAt.getLayoutParams();
                                    if (layoutParams != null) {
                                        FrameLayout.LayoutParams layoutParams2 = (FrameLayout.LayoutParams) layoutParams;
                                        int i2 = this.sideMargins;
                                        layoutParams2.rightMargin = i2;
                                        layoutParams2.leftMargin = i2;
                                        if (childAt == this.qsPanelScrollView) {
                                            QSPanel qSPanel = this.qsPanel;
                                            if (qSPanel != null) {
                                                qSPanel.setContentMargins(this.contentPaddingStart, this.contentPaddingEnd);
                                            }
                                        } else if (childAt == this.qsContent) {
                                            continue;
                                        } else {
                                            MiuiNotificationShadeHeader miuiNotificationShadeHeader2 = this.header;
                                            if (miuiNotificationShadeHeader2 == null) {
                                                Intrinsics.throwUninitializedPropertyAccessException("header");
                                                throw null;
                                            } else if (childAt != miuiNotificationShadeHeader2) {
                                                childAt.setPaddingRelative(this.contentPaddingStart, childAt.getPaddingTop(), this.contentPaddingEnd, childAt.getPaddingBottom());
                                            }
                                        }
                                    } else {
                                        throw new TypeCastException("null cannot be cast to non-null type android.widget.FrameLayout.LayoutParams");
                                    }
                                } else if (miuiQSDetail != null) {
                                    int i3 = this.sideMargins;
                                    miuiQSDetail.setMargins(i3, i3);
                                }
                            } else if (miuiQSCustomizer != null) {
                                int i4 = this.sideMargins;
                                miuiQSCustomizer.setMargins(i4, i4);
                            }
                        } else {
                            continue;
                        }
                    } else {
                        continue;
                    }
                }
            } else {
                Intrinsics.throwUninitializedPropertyAccessException("statusBarBackground");
                throw null;
            }
        }
    }

    private final int getDisplayHeight() {
        if (this.sizePoint.y == 0) {
            getDisplay().getRealSize(this.sizePoint);
        }
        return this.sizePoint.y;
    }

    @Override // com.android.systemui.tuner.TunerService.Tunable
    public void onTuningChanged(@Nullable String str, @Nullable String str2) {
        ToggleSliderView toggleSliderView = this.brightnessView;
        if (toggleSliderView != null && Intrinsics.areEqual("qs_show_brightness", str)) {
            updateViewVisibilityForTuningValue(toggleSliderView, str2);
        }
    }

    private final void updateViewVisibilityForTuningValue(View view, String str) {
        if (view != null) {
            view.setVisibility(TunerService.parseIntegerSwitch(str, true) ? 0 : 8);
        }
    }

    public final void setBrightnessListening(boolean z) {
        MiuiBrightnessController miuiBrightnessController = this.brightnessController;
        if (miuiBrightnessController == null) {
            return;
        }
        if (z) {
            miuiBrightnessController.registerCallbacks();
        } else {
            miuiBrightnessController.unregisterCallbacks();
        }
    }

    public final int getMinHeight() {
        QuickQSPanel quickQSPanel2;
        MiuiNotificationShadeHeader miuiNotificationShadeHeader = this.header;
        if (miuiNotificationShadeHeader != null) {
            int height = miuiNotificationShadeHeader.getHeight();
            if (!this.showQsPanel || (quickQSPanel2 = this.quickQSPanel) == null || this.footerBundle == null) {
                return height;
            }
            if (quickQSPanel2 != null) {
                int height2 = height + quickQSPanel2.getHeight();
                ViewGroup viewGroup = this.footerBundle;
                if (viewGroup != null) {
                    return height2 + viewGroup.getHeight();
                }
                Intrinsics.throwNpe();
                throw null;
            }
            Intrinsics.throwNpe();
            throw null;
        }
        Intrinsics.throwUninitializedPropertyAccessException("header");
        throw null;
    }

    public final void updateQSDataUsage(boolean z) {
        this.qsDataUsageEnabled = true;
        if (!z) {
            QSFooterDataUsage qSFooterDataUsage = this.dataUsage;
            if (qSFooterDataUsage != null) {
                ViewGroup viewGroup = this.footerBundle;
                if (viewGroup != null) {
                    viewGroup.removeView(qSFooterDataUsage);
                }
                QSFooterDataUsage qSFooterDataUsage2 = this.dataUsage;
                if (qSFooterDataUsage2 != null) {
                    qSFooterDataUsage2.setQSContainer(null);
                }
                this.dataUsage = null;
            }
        } else if (this.dataUsage == null && this.footerBundle != null) {
            LayoutInflater layoutInflater2 = getLayoutInflater();
            if (layoutInflater2 != null) {
                View inflate = layoutInflater2.inflate(C0016R$layout.qs_footer_data_usage, this.footerBundle, false);
                if (inflate != null) {
                    QSFooterDataUsage qSFooterDataUsage3 = (QSFooterDataUsage) inflate;
                    this.dataUsage = qSFooterDataUsage3;
                    ViewGroup viewGroup2 = this.footerBundle;
                    if (viewGroup2 != null) {
                        viewGroup2.addView(qSFooterDataUsage3, 0);
                    }
                    QSFooterDataUsage qSFooterDataUsage4 = this.dataUsage;
                    if (qSFooterDataUsage4 != null) {
                        qSFooterDataUsage4.setElevation(50.0f);
                    }
                    QSFooterDataUsage qSFooterDataUsage5 = this.dataUsage;
                    if (qSFooterDataUsage5 != null) {
                        qSFooterDataUsage5.setQSContainer(this);
                    }
                } else {
                    throw new TypeCastException("null cannot be cast to non-null type com.android.systemui.qs.QSFooterDataUsage");
                }
            } else {
                Intrinsics.throwNpe();
                throw null;
            }
        }
        updateExpansion(true);
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.tunerService.addTunable(this, "qs_show_brightness");
        BrightnessMirrorController brightnessMirrorController2 = this.brightnessMirrorController;
        if (brightnessMirrorController2 != null) {
            brightnessMirrorController2.addCallback((BrightnessMirrorController.BrightnessMirrorListener) this);
        }
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.tunerService.removeTunable(this);
        BrightnessMirrorController brightnessMirrorController2 = this.brightnessMirrorController;
        if (brightnessMirrorController2 != null) {
            brightnessMirrorController2.removeCallback((BrightnessMirrorController.BrightnessMirrorListener) this);
        }
    }

    @Override // com.android.systemui.statusbar.policy.BrightnessMirrorController.BrightnessMirrorListener
    public void onBrightnessMirrorReinflated(@NotNull View view) {
        Intrinsics.checkParameterIsNotNull(view, "brightnessMirror");
        updateBrightnessMirror();
    }

    public final void setBrightnessMirror(@Nullable BrightnessMirrorController brightnessMirrorController2) {
        BrightnessMirrorController brightnessMirrorController3 = this.brightnessMirrorController;
        if (brightnessMirrorController3 != null) {
            brightnessMirrorController3.removeCallback((BrightnessMirrorController.BrightnessMirrorListener) this);
        }
        this.brightnessMirrorController = brightnessMirrorController2;
        if (brightnessMirrorController2 != null) {
            brightnessMirrorController2.addCallback((BrightnessMirrorController.BrightnessMirrorListener) this);
        }
        updateBrightnessMirror();
    }

    private final void updateBrightnessMirror() {
        BrightnessMirrorController brightnessMirrorController2 = this.brightnessMirrorController;
        if (brightnessMirrorController2 != null) {
            ToggleSliderView toggleSliderView = (ToggleSliderView) findViewById(C0014R$id.brightness_slider);
            ToggleSliderView toggleSliderView2 = (ToggleSliderView) brightnessMirrorController2.getMirror().findViewById(C0014R$id.brightness_slider);
            if (toggleSliderView != null) {
                toggleSliderView.setMirror(toggleSliderView2);
            }
            if (toggleSliderView != null) {
                toggleSliderView.setMirrorController(brightnessMirrorController2);
            }
        }
    }

    public final void setShowQSPanel(boolean z) {
        this.showQsPanel = z;
        QSContent qSContent = this.qsContent;
        if (qSContent != null) {
            qSContent.setVisibility(z ? 0 : 8);
        }
    }

    public final void setDetailAnimatedViews(@NotNull View... viewArr) {
        Intrinsics.checkParameterIsNotNull(viewArr, "views");
        this.extraAnimatedViews = viewArr;
        setupAnimatedViews();
    }

    private final void setupAnimatedViews() {
        List<View> list = CollectionsKt__CollectionsKt.listOf((Object[]) new View[]{this.footerBundle, this.dragHandle, this.quickQSPanel, this.qsPanel, this.qsBackground});
        if (this.extraAnimatedViews != null) {
            ArrayList arrayList = new ArrayList(list);
            View[] viewArr = this.extraAnimatedViews;
            if (viewArr != null) {
                arrayList.addAll(CollectionsKt__CollectionsKt.listOf((Object[]) ((View[]) Arrays.copyOf(viewArr, viewArr.length))));
                list = arrayList;
            } else {
                Intrinsics.throwNpe();
                throw null;
            }
        }
        ArrayList arrayList2 = new ArrayList(list);
        MiuiNotificationShadeHeader miuiNotificationShadeHeader = this.header;
        if (miuiNotificationShadeHeader != null) {
            arrayList2.add(miuiNotificationShadeHeader);
            MiuiQSDetail miuiQSDetail = this.qsDetail;
            if (miuiQSDetail != null) {
                miuiQSDetail.setAnimatedViews(list);
            }
            MiuiQSCustomizer miuiQSCustomizer = this.qsCustomizer;
            if (miuiQSCustomizer != null) {
                miuiQSCustomizer.setAnimatedViews(arrayList2);
                return;
            }
            return;
        }
        Intrinsics.throwUninitializedPropertyAccessException("header");
        throw null;
    }

    private final void updateIndicator(float f) {
        IndicatorDrawable indicatorDrawable2 = this.indicatorDrawable;
        if (indicatorDrawable2 != null && this.indicatorProgress != f) {
            this.indicatorProgress = f;
            ValueAnimator valueAnimator = this.caretAnimator;
            if (valueAnimator != null && valueAnimator.isRunning()) {
                valueAnimator.cancel();
                indicatorDrawable2.setCaretProgress(0.0f);
            }
            ValueAnimator ofFloat = ValueAnimator.ofFloat(0.0f, f);
            this.caretAnimator = ofFloat;
            if (ofFloat != null) {
                ofFloat.addUpdateListener(new MiuiQSContainer$updateIndicator$1$2(indicatorDrawable2));
            }
            ValueAnimator valueAnimator2 = this.caretAnimator;
            if (valueAnimator2 != null) {
                valueAnimator2.setDuration(200L);
            }
            ValueAnimator valueAnimator3 = this.caretAnimator;
            if (valueAnimator3 != null) {
                valueAnimator3.setInterpolator(this.caretInterpolator);
            }
            ValueAnimator valueAnimator4 = this.caretAnimator;
            if (valueAnimator4 != null) {
                valueAnimator4.start();
            }
        }
    }

    public final void updateDataUsageInfo() {
        QSFooterDataUsage qSFooterDataUsage = this.dataUsage;
        if (qSFooterDataUsage != null) {
            qSFooterDataUsage.updateDataUsageInfo();
        }
    }
}
