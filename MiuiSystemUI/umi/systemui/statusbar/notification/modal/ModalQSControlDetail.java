package com.android.systemui.statusbar.notification.modal;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
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
import android.widget.FrameLayout;
import codeinjection.CodeInjection;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0021R$string;
import com.android.systemui.Dependency;
import com.android.systemui.controlcenter.phone.controls.MiPlayPluginManager;
import com.android.systemui.controlcenter.qs.tileview.QSBigTileView;
import com.android.systemui.controlcenter.utils.ControlCenterUtils;
import com.android.systemui.plugins.PluginListener;
import com.android.systemui.plugins.miui.controls.MiPlayPlugin;
import com.android.systemui.plugins.qs.DetailAdapter;
import com.android.systemui.qs.MiuiQSDetailItems;
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

public class ModalQSControlDetail extends FrameLayout {
    private float detailCornerRadius;
    protected IStateStyle mAnim;
    protected Runnable mAnimateHideRunnable = new Runnable() {
        /* class com.android.systemui.statusbar.notification.modal.ModalQSControlDetail.AnonymousClass4 */

        public void run() {
            if (DeviceConfig.isLowGpuDevice()) {
                ModalQSControlDetail.this.animateHideDetailAndTileOnLowEnd();
            } else {
                ModalQSControlDetail.this.animateHideDetailAndTile();
            }
        }
    };
    protected Runnable mAnimateShowRunnable = new Runnable() {
        /* class com.android.systemui.statusbar.notification.modal.ModalQSControlDetail.AnonymousClass3 */

        public void run() {
            if (DeviceConfig.isLowGpuDevice()) {
                Log.d("QSDetail", "showing on low end");
                ModalQSControlDetail.this.animateShowDetailAndTileOnLowEnd();
                return;
            }
            ModalQSControlDetail.this.animateShowDetailAndTile();
        }
    };
    private Context mContext;
    private int mCurrentDetailIndex;
    private DetailAdapter mDetailAdapter;
    private View mDetailContainer;
    private ViewGroup mDetailContent;
    private final SparseArray<View> mDetailViews = new SparseArray<>();
    protected View mFromView;
    protected int[] mFromViewFrame = new int[4];
    protected int[] mFromViewLocation = new int[4];
    protected boolean mIsAnimating;
    protected View mToView;
    protected int[] mToViewFrame = new int[4];
    protected int[] mToViewLocation = new int[4];
    protected View mTranslateView;
    private PluginListener<MiPlayPlugin> pluginListener = new PluginListener<MiPlayPlugin>() {
        /* class com.android.systemui.statusbar.notification.modal.ModalQSControlDetail.AnonymousClass1 */

        public void onPluginConnected(MiPlayPlugin miPlayPlugin, Context context) {
        }

        public void onPluginDisconnected(MiPlayPlugin miPlayPlugin) {
            ModalQSControlDetail.this.mDetailViews.remove(1168);
        }
    };

    public ModalQSControlDetail(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mContext = context;
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        updateDetailLayout();
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
        View findViewById = findViewById(C0015R$id.qs_detail_container);
        this.mDetailContainer = findViewById;
        findViewById.setClickable(true);
        updateBackground();
        this.detailCornerRadius = this.mContext.getResources().getDimension(C0012R$dimen.qs_control_corner_general_radius);
        this.mDetailContainer.setClipToOutline(true);
        this.mDetailContainer.setOutlineProvider(new ViewOutlineProvider() {
            /* class com.android.systemui.statusbar.notification.modal.ModalQSControlDetail.AnonymousClass2 */

            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), ModalQSControlDetail.this.detailCornerRadius);
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

    private void updateDetailLayout() {
        updateContainerHeight();
    }

    private void updateContainerHeight() {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) this.mDetailContainer.getLayoutParams();
        layoutParams.height = -2;
        this.mDetailContainer.setLayoutParams(layoutParams);
    }

    public void requestLayout() {
        if (!isInLayout() && getParent() != null) {
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

    public void handleShowingDetail(DetailAdapter detailAdapter, View view, View view2, String str) {
        boolean z;
        String str2;
        boolean z2 = detailAdapter != null;
        boolean z3 = this.mDetailAdapter != null;
        if (z2 && z3 && this.mDetailAdapter == detailAdapter) {
            return;
        }
        if (z2 || z3) {
            if (z2) {
                this.mDetailAdapter = detailAdapter;
                int metricsCategory = detailAdapter.getMetricsCategory();
                View createDetailView = this.mDetailAdapter.createDetailView(this.mContext, this.mDetailViews.get(metricsCategory), this.mDetailContent);
                if (createDetailView instanceof MiuiQSDetailItems) {
                    ((MiuiQSDetailItems) createDetailView).setDetailShowing(true);
                    updateContainerHeight();
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
                        MiuiQSDetailItems miuiQSDetailItems = (MiuiQSDetailItems) view3;
                        z = miuiQSDetailItems.isItemClicked();
                        miuiQSDetailItems.setItemClicked(false);
                        miuiQSDetailItems.setDetailShowing(false);
                        str2 = miuiQSDetailItems.getSuffix();
                    } else {
                        str2 = CodeInjection.MD5;
                        z = false;
                    }
                    if (!TextUtils.isEmpty(str2)) {
                        updateContainerHeight();
                        ((SystemUIStat) Dependency.get(SystemUIStat.class)).handleQSDetailExitEvent(str2, z, false, "back_pressed");
                    }
                    MetricsLogger.hidden(this.mContext, this.mDetailAdapter.getMetricsCategory());
                }
                this.mDetailAdapter = null;
                view = null;
            }
            animateDetailVisibleDiff(z2, view, view2);
            sendAccessibilityEvent(32);
            View view4 = this.mDetailViews.get(1168);
            MiPlayPluginManager miPlayPluginManager = (MiPlayPluginManager) Dependency.get(MiPlayPluginManager.class);
            if (z2) {
                miPlayPluginManager.showMiPlayDetailView(view4, str);
            } else {
                miPlayPluginManager.hideMiPlayDetailView(view4);
            }
        }
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

    private void computeAnimationParams() {
        getLocationInWindowWithoutTransform(this.mFromView, this.mFromViewLocation);
        int[] iArr = this.mFromViewLocation;
        iArr[1] = (int) (((float) iArr[1]) + this.mFromView.getTranslationY());
        int[] iArr2 = this.mToViewLocation;
        iArr2[1] = (int) (((float) iArr2[1]) + this.mToView.getTranslationY());
        getLocationInWindowWithoutTransform(this.mToView, this.mToViewLocation);
        int width = this.mFromView.getWidth();
        int[] iArr3 = this.mFromViewLocation;
        iArr3[0] = iArr3[0] + this.mContext.getResources().getDimensionPixelOffset(C0012R$dimen.panel_content_margin);
        int dimensionPixelOffset = width - (this.mContext.getResources().getDimensionPixelOffset(C0012R$dimen.panel_content_margin) * 2);
        int height = this.mFromView.getHeight();
        int width2 = this.mToView.getWidth();
        int height2 = this.mToView.getHeight();
        int[] iArr4 = this.mFromViewLocation;
        iArr4[2] = iArr4[0] + dimensionPixelOffset;
        iArr4[3] = iArr4[1] + height;
        int[] iArr5 = this.mToViewLocation;
        iArr5[2] = iArr5[0] + width2;
        iArr5[3] = iArr5[1] + height2;
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
        computeAnimationParams();
        this.mAnim.cancel();
        IStateStyle to = this.mAnim.setTo("fromLeft", Integer.valueOf(this.mFromViewFrame[0]), "fromTop", Integer.valueOf(this.mFromViewFrame[1]), "fromRight", Integer.valueOf(this.mFromViewFrame[2]), "fromBottom", Integer.valueOf(this.mFromViewFrame[3]), "toLeft", Integer.valueOf((this.mToViewFrame[0] + this.mFromViewLocation[0]) - this.mToViewLocation[0]), "toTop", Integer.valueOf((this.mToViewFrame[1] + this.mFromViewLocation[1]) - this.mToViewLocation[1]), "toRight", Integer.valueOf((this.mToViewFrame[2] + this.mFromViewLocation[2]) - this.mToViewLocation[2]), "toBottom", Integer.valueOf((this.mToViewFrame[3] + this.mFromViewLocation[3]) - this.mToViewLocation[3]));
        AnimConfig animConfig = new AnimConfig();
        animConfig.setEase(-2, 0.7f, 0.3f);
        animConfig.addListeners(new TransitionListener() {
            /* class com.android.systemui.statusbar.notification.modal.ModalQSControlDetail.AnonymousClass5 */
            int[] fromFrame = new int[4];
            final View to;
            int[] toFrame = new int[4];
            final View translate;

            {
                ModalQSControlDetail modalQSControlDetail = ModalQSControlDetail.this;
                View view = modalQSControlDetail.mFromView;
                this.to = modalQSControlDetail.mToView;
                this.translate = modalQSControlDetail.mTranslateView;
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
                View view = this.to;
                int[] iArr = this.toFrame;
                view.setLeftTopRightBottom(iArr[0], iArr[1], iArr[2], iArr[3]);
                int[] iArr2 = this.toFrame;
                ModalQSControlDetail.this.mDetailContent.setTranslationX((float) (((iArr2[2] - iArr2[0]) - ModalQSControlDetail.this.mDetailContent.getMeasuredWidth()) / 2));
                View view2 = this.translate;
                if (view2 != null) {
                    int[] iArr3 = this.fromFrame;
                    int i = iArr3[2] - iArr3[0];
                    int[] iArr4 = ModalQSControlDetail.this.mFromViewFrame;
                    view2.setTranslationX((float) (i - (iArr4[2] - iArr4[0])));
                    View view3 = this.translate;
                    int[] iArr5 = this.fromFrame;
                    int[] iArr6 = ModalQSControlDetail.this.mFromViewFrame;
                    view3.setTranslationY((float) ((iArr5[3] - iArr5[1]) - (iArr6[3] - iArr6[1])));
                }
            }

            @Override // miuix.animation.listener.TransitionListener
            public void onBegin(Object obj) {
                super.onBegin(obj);
                ModalQSControlDetail modalQSControlDetail = ModalQSControlDetail.this;
                modalQSControlDetail.mIsAnimating = true;
                modalQSControlDetail.setVisibility(0);
                ((ViewGroup) ModalQSControlDetail.this.mFromView.getParent()).suppressLayout(true);
                ((ViewGroup) ModalQSControlDetail.this.mToView.getParent()).suppressLayout(true);
                View view = (View) ModalQSControlDetail.this.mDetailViews.get(ModalQSControlDetail.this.mCurrentDetailIndex);
                if (view instanceof ViewGroup) {
                    ((ViewGroup) view).suppressLayout(true);
                }
            }

            @Override // miuix.animation.listener.TransitionListener
            public void onComplete(Object obj) {
                super.onComplete(obj);
                ModalQSControlDetail modalQSControlDetail = ModalQSControlDetail.this;
                modalQSControlDetail.mIsAnimating = false;
                View view = modalQSControlDetail.mFromView;
                int[] iArr = modalQSControlDetail.mFromViewFrame;
                view.setLeftTopRightBottom(iArr[0], iArr[1], iArr[2], iArr[3]);
                ((ViewGroup) ModalQSControlDetail.this.mFromView.getParent()).suppressLayout(false);
                ((ViewGroup) ModalQSControlDetail.this.mToView.getParent()).suppressLayout(false);
                View view2 = (View) ModalQSControlDetail.this.mDetailViews.get(ModalQSControlDetail.this.mCurrentDetailIndex);
                if (view2 instanceof ViewGroup) {
                    ((ViewGroup) view2).suppressLayout(false);
                }
                if (view2 instanceof MiuiQSDetailItems) {
                    ((MiuiQSDetailItems) view2).notifyData();
                }
            }
        });
        to.to("fromLeft", Integer.valueOf((this.mFromViewFrame[0] + this.mToViewLocation[0]) - this.mFromViewLocation[0]), "fromTop", Integer.valueOf((this.mFromViewFrame[1] + this.mToViewLocation[1]) - this.mFromViewLocation[1]), "fromRight", Integer.valueOf((this.mFromViewFrame[2] + this.mToViewLocation[2]) - this.mFromViewLocation[2]), "fromBottom", Integer.valueOf((this.mFromViewFrame[3] + this.mToViewLocation[3]) - this.mFromViewLocation[3]), "toLeft", Integer.valueOf(this.mToViewFrame[0]), "toTop", Integer.valueOf(this.mToViewFrame[1]), "toRight", Integer.valueOf(this.mToViewFrame[2]), "toBottom", Integer.valueOf(this.mToViewFrame[3]), animConfig);
    }

    /* access modifiers changed from: protected */
    public void animateShowDetailAndTileOnLowEnd() {
        PhysicBasedInterpolator physicBasedInterpolator = new PhysicBasedInterpolator(0.9f, 0.35f);
        ValueAnimator duration = ValueAnimator.ofFloat(1.0f, 0.0f).setDuration(300L);
        duration.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.android.systemui.statusbar.notification.modal.ModalQSControlDetail.AnonymousClass6 */

            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                float f = 1.0f - (0.1f * floatValue);
                ModalQSControlDetail.this.mToView.setScaleX(f);
                ModalQSControlDetail.this.mToView.setScaleY(f);
                ModalQSControlDetail.this.mToView.setAlpha(1.0f - floatValue);
            }
        });
        duration.addListener(new AnimatorListenerAdapter() {
            /* class com.android.systemui.statusbar.notification.modal.ModalQSControlDetail.AnonymousClass7 */

            public void onAnimationStart(Animator animator) {
                ModalQSControlDetail.this.setVisibility(0);
                ((ViewGroup) ModalQSControlDetail.this.mFromView.getParent()).suppressLayout(true);
                ((ViewGroup) ModalQSControlDetail.this.mToView.getParent()).suppressLayout(true);
            }

            public void onAnimationEnd(Animator animator) {
                ((ViewGroup) ModalQSControlDetail.this.mFromView.getParent()).suppressLayout(false);
                ((ViewGroup) ModalQSControlDetail.this.mToView.getParent()).suppressLayout(false);
            }
        });
        duration.setInterpolator(physicBasedInterpolator);
        duration.start();
    }

    /* access modifiers changed from: protected */
    public void animateHideDetailAndTile() {
        if (!this.mIsAnimating) {
            computeAnimationParams();
        }
        this.mAnim.cancel();
        IStateStyle to = this.mAnim.setTo("fromLeft", Integer.valueOf((this.mFromViewFrame[0] + this.mToViewLocation[0]) - this.mFromViewLocation[0]), "fromTop", Integer.valueOf((this.mFromViewFrame[1] + this.mToViewLocation[1]) - this.mFromViewLocation[1]), "fromRight", Integer.valueOf((this.mFromViewFrame[2] + this.mToViewLocation[2]) - this.mFromViewLocation[2]), "fromBottom", Integer.valueOf((this.mFromViewFrame[3] + this.mToViewLocation[3]) - this.mFromViewLocation[3]), "toLeft", Integer.valueOf(this.mToViewFrame[0]), "toTop", Integer.valueOf(this.mToViewFrame[1]), "toRight", Integer.valueOf(this.mToViewFrame[2]), "toBottom", Integer.valueOf(this.mToViewFrame[3]));
        AnimConfig animConfig = new AnimConfig();
        animConfig.setEase(-2, 0.7f, 0.3f);
        animConfig.addListeners(new TransitionListener() {
            /* class com.android.systemui.statusbar.notification.modal.ModalQSControlDetail.AnonymousClass8 */
            int[] fromFrame = new int[4];
            final View to;
            int[] toFrame = new int[4];
            final View translate;

            {
                ModalQSControlDetail modalQSControlDetail = ModalQSControlDetail.this;
                this.translate = modalQSControlDetail.mTranslateView;
                View view = modalQSControlDetail.mFromView;
                this.to = modalQSControlDetail.mToView;
            }

            @Override // miuix.animation.listener.TransitionListener
            public void onBegin(Object obj) {
                super.onBegin(obj);
                ModalQSControlDetail modalQSControlDetail = ModalQSControlDetail.this;
                modalQSControlDetail.mIsAnimating = true;
                ((ViewGroup) modalQSControlDetail.mFromView.getParent()).suppressLayout(true);
                ((ViewGroup) ModalQSControlDetail.this.mToView.getParent()).suppressLayout(true);
                View view = (View) ModalQSControlDetail.this.mDetailViews.get(ModalQSControlDetail.this.mCurrentDetailIndex);
                if (view instanceof ViewGroup) {
                    ((ViewGroup) view).suppressLayout(true);
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
                View view = this.to;
                int[] iArr = this.toFrame;
                view.setLeftTopRightBottom(iArr[0], iArr[1], iArr[2], iArr[3]);
                int[] iArr2 = this.toFrame;
                ModalQSControlDetail.this.mDetailContent.setTranslationX((float) (((iArr2[2] - iArr2[0]) - ModalQSControlDetail.this.mDetailContent.getMeasuredWidth()) / 2));
                View view2 = this.translate;
                if (view2 != null) {
                    int[] iArr3 = this.fromFrame;
                    int i = iArr3[2] - iArr3[0];
                    int[] iArr4 = ModalQSControlDetail.this.mFromViewFrame;
                    view2.setTranslationX((float) (i - (iArr4[2] - iArr4[0])));
                    View view3 = this.translate;
                    int[] iArr5 = this.fromFrame;
                    int[] iArr6 = ModalQSControlDetail.this.mFromViewFrame;
                    view3.setTranslationY((float) ((iArr5[3] - iArr5[1]) - (iArr6[3] - iArr6[1])));
                }
            }

            @Override // miuix.animation.listener.TransitionListener
            public void onComplete(Object obj) {
                super.onComplete(obj);
                ModalQSControlDetail modalQSControlDetail = ModalQSControlDetail.this;
                modalQSControlDetail.mIsAnimating = false;
                ((ViewGroup) modalQSControlDetail.mFromView.getParent()).suppressLayout(false);
                ((ViewGroup) ModalQSControlDetail.this.mToView.getParent()).suppressLayout(false);
                View view = (View) ModalQSControlDetail.this.mDetailViews.get(ModalQSControlDetail.this.mCurrentDetailIndex);
                if (view instanceof ViewGroup) {
                    ((ViewGroup) view).suppressLayout(false);
                }
                ModalQSControlDetail.this.setVisibility(8);
                View view2 = ModalQSControlDetail.this.mFromView;
                if (view2 instanceof QSBigTileView) {
                    ((QSBigTileView) view2).updateIndicatorTouch();
                }
            }
        });
        to.to("fromLeft", Integer.valueOf(this.mFromViewFrame[0]), "fromTop", Integer.valueOf(this.mFromViewFrame[1]), "fromRight", Integer.valueOf(this.mFromViewFrame[2]), "fromBottom", Integer.valueOf(this.mFromViewFrame[3]), "toLeft", Integer.valueOf((this.mToViewFrame[0] + this.mFromViewLocation[0]) - this.mToViewLocation[0]), "toTop", Integer.valueOf((this.mToViewFrame[1] + this.mFromViewLocation[1]) - this.mToViewLocation[1]), "toRight", Integer.valueOf((this.mToViewFrame[2] + this.mFromViewLocation[2]) - this.mToViewLocation[2]), "toBottom", Integer.valueOf((this.mToViewFrame[3] + this.mFromViewLocation[3]) - this.mToViewLocation[3]), animConfig);
    }

    /* access modifiers changed from: protected */
    public void animateHideDetailAndTileOnLowEnd() {
        PhysicBasedInterpolator physicBasedInterpolator = new PhysicBasedInterpolator(0.9f, 0.35f);
        ValueAnimator duration = ValueAnimator.ofFloat(1.0f, 0.0f).setDuration(300L);
        duration.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.android.systemui.statusbar.notification.modal.ModalQSControlDetail.AnonymousClass9 */

            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                float f = (0.1f * floatValue) + 0.9f;
                ModalQSControlDetail.this.mToView.setScaleX(f);
                ModalQSControlDetail.this.mToView.setScaleY(f);
                ModalQSControlDetail.this.mToView.setAlpha(floatValue);
            }
        });
        duration.addListener(new AnimatorListenerAdapter() {
            /* class com.android.systemui.statusbar.notification.modal.ModalQSControlDetail.AnonymousClass10 */

            public void onAnimationStart(Animator animator) {
                ((ViewGroup) ModalQSControlDetail.this.mToView.getParent()).suppressLayout(true);
            }

            public void onAnimationEnd(Animator animator) {
                ((ViewGroup) ModalQSControlDetail.this.mToView.getParent()).suppressLayout(false);
                ModalQSControlDetail.this.setVisibility(8);
                ModalQSControlDetail.this.mDetailContainer.setAlpha(0.0f);
            }
        });
        duration.setInterpolator(physicBasedInterpolator);
        duration.start();
    }

    /* access modifiers changed from: protected */
    public void animateDetailAlphaWithRotation(boolean z, View view) {
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
            AnimConfig animConfig = new AnimConfig();
            animConfig.setEase(-2, 0.8f, 0.3f);
            animConfig.addListeners(new TransitionListener() {
                /* class com.android.systemui.statusbar.notification.modal.ModalQSControlDetail.AnonymousClass11 */

                @Override // miuix.animation.listener.TransitionListener
                public void onBegin(Object obj) {
                    super.onBegin(obj);
                    ModalQSControlDetail.this.mDetailContainer.setLayerType(2, null);
                }

                @Override // miuix.animation.listener.TransitionListener
                public void onComplete(Object obj) {
                    super.onComplete(obj);
                    ModalQSControlDetail.this.mDetailContainer.setLayerType(0, null);
                }
            });
            state.to(animState, animConfig);
            return;
        }
        Folme.useAt(this.mDetailContainer).state().cancel();
        IStateStyle state2 = Folme.useAt(this.mDetailContainer).state();
        AnimState animState2 = new AnimState("detail_container_alpha");
        animState2.add(ViewProperty.ALPHA, 0.0d);
        animState2.add(ViewProperty.ROTATION_X, 0.0d);
        animState2.add(ViewProperty.ROTATION_Y, 0.0d);
        animState2.add(ViewProperty.TRANSLATION_Z, 0.0d);
        AnimConfig animConfig2 = new AnimConfig();
        animConfig2.setEase(-2, 0.8f, 0.3f);
        animConfig2.addListeners(new TransitionListener() {
            /* class com.android.systemui.statusbar.notification.modal.ModalQSControlDetail.AnonymousClass12 */

            @Override // miuix.animation.listener.TransitionListener
            public void onBegin(Object obj) {
                super.onBegin(obj);
                ModalQSControlDetail.this.mDetailContainer.setLayerType(2, null);
            }

            @Override // miuix.animation.listener.TransitionListener
            public void onComplete(Object obj) {
                super.onComplete(obj);
                ModalQSControlDetail.this.mDetailContainer.setLayerType(0, null);
            }
        });
        state2.to(animState2, animConfig2);
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
