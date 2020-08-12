package com.android.keyguard.fod;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.hardware.display.DisplayManager;
import android.net.Uri;
import android.os.Handler;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Slog;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.analytics.AnalyticsHelper;
import com.android.keyguard.fod.item.IQuickOpenItem;
import com.android.systemui.Dependency;
import com.android.systemui.UiOffloadThread;
import com.android.systemui.miui.statusbar.phone.applock.AppLockHelper;
import com.android.systemui.plugins.R;
import java.util.ArrayList;
import java.util.List;
import miui.security.SecurityManager;
import miui.view.MiuiHapticFeedbackConstants;

class MiuiGxzwQuickOpenView extends GxzwWindowFrameLayout {
    private float mCicleRadius;
    /* access modifiers changed from: private */
    public View mCloseView;
    /* access modifiers changed from: private */
    public IQuickOpenItem mCurrentSelectItem;
    private RectF mFastRect;
    private int mFingerID = 0;
    private RectF mFingerRect;
    private Handler mHandler = new Handler();
    /* access modifiers changed from: private */
    public Animator mItemExpandAnimator;
    private float mItemRadius;
    private float mItemScaleRadius;
    /* access modifiers changed from: private */
    public boolean mItemsExpanded = false;
    private WindowManager.LayoutParams mLayoutParams;
    /* access modifiers changed from: private */
    public boolean mLoading = false;
    /* access modifiers changed from: private */
    public ObjectAnimator mLoadingAnimator;
    private MiuiGxzwQuickTeachView mMiuiGxzwQuickTeachView;
    private Paint mPaint;
    private boolean mPendingUpdateLp;
    /* access modifiers changed from: private */
    public MiuiGxzwQuickLoadingView mQuickLoadingView;
    private List<IQuickOpenItem> mQuickOpenItemList = new ArrayList();
    /* access modifiers changed from: private */
    public QuickViewListener mQuickViewListener;
    private int mScreenHeight;
    private SecurityManager mSecurityManager;
    private float mSelectBackgroundRadius;
    private Runnable mShowRunnable = new Runnable() {
        public void run() {
            MiuiGxzwQuickOpenView.this.addViewToWindow();
            MiuiGxzwQuickOpenView.this.setVisibility(0);
            IQuickOpenItem unused = MiuiGxzwQuickOpenView.this.mCurrentSelectItem = null;
            MiuiGxzwQuickOpenView.this.updateTextSize();
            MiuiGxzwQuickOpenView.this.updatePixelSize();
            MiuiGxzwQuickOpenView.this.startLoadingAnimation();
        }
    };
    /* access modifiers changed from: private */
    public boolean mShowed = false;
    private TextView mSkipTeach;
    private TextView mSubTitleView;
    /* access modifiers changed from: private */
    public boolean mTeachMode;
    private boolean mTeachTouchDown;
    private FrameLayout.LayoutParams mTipLayoutParams;
    private int mTipPressMargin;
    private int mTipSlideMargin;
    private TextView mTipView;
    private LinearLayout mTitleContainer;
    private FrameLayout.LayoutParams mTitleLayoutParams;
    private int mTitleMargin;
    private TextView mTitleView;
    private final UiOffloadThread mUiOffloadThread = ((UiOffloadThread) Dependency.get(UiOffloadThread.class));

    public interface QuickViewListener {
        void onDismiss();

        void onShow();
    }

    public MiuiGxzwQuickOpenView(Context context) {
        super(context);
        initView();
    }

    /* access modifiers changed from: protected */
    public boolean drawChild(Canvas canvas, View view, long j) {
        IQuickOpenItem iQuickOpenItem = this.mCurrentSelectItem;
        if (iQuickOpenItem != null && iQuickOpenItem.getView() == view) {
            canvas.drawCircle(this.mCurrentSelectItem.getRect().centerX(), this.mCurrentSelectItem.getRect().centerY(), this.mSelectBackgroundRadius, this.mPaint);
        }
        return super.drawChild(canvas, view, j);
    }

    public void show(int i) {
        this.mFingerID = i;
        if (!this.mShowed) {
            this.mShowed = true;
            this.mHandler.postDelayed(this.mShowRunnable, 500);
        }
    }

    public void dismiss() {
        if (this.mShowed) {
            this.mHandler.removeCallbacks(this.mShowRunnable);
            this.mFingerID = 0;
            this.mShowed = false;
            if (this.mLoading || this.mCurrentSelectItem != null) {
                removeQuickView();
            } else {
                startDismissAnimation();
            }
            QuickViewListener quickViewListener = this.mQuickViewListener;
            if (quickViewListener != null) {
                quickViewListener.onDismiss();
            }
        }
    }

    public boolean isShow() {
        return this.mShowed;
    }

    public void onTouchDown(float f, float f2) {
        MiuiGxzwQuickTeachView miuiGxzwQuickTeachView;
        if (this.mTeachMode && (miuiGxzwQuickTeachView = this.mMiuiGxzwQuickTeachView) != null) {
            miuiGxzwQuickTeachView.stopTeachAnim();
        }
    }

    public void onTouchUp(float f, float f2) {
        IQuickOpenItem caculateSelectQucikOpenItem = caculateSelectQucikOpenItem(f, f2);
        if (caculateSelectQucikOpenItem != null) {
            Slog.i("MiuiGxzwQuickOpenView", "open quick app: " + caculateSelectQucikOpenItem.getTag() + ", x = " + f + ", y = " + f2);
            handleQucikOpenItemTouchUp(caculateSelectQucikOpenItem);
            dismiss();
        } else if (this.mQuickOpenItemList.size() > 0 && (MiuiGxzwQuickOpenUtil.isShowQuickOpenTeach(getContext()) || this.mTeachMode)) {
            enterTeachMode();
        } else if (!AccessibilityManager.getInstance(getContext()).isTouchExplorationEnabled() || !this.mItemsExpanded) {
            dismiss();
        }
    }

    public void onTouchMove(float f, float f2) {
        if (this.mLoadingAnimator != null && this.mLoading && this.mShowed && !this.mFastRect.contains(f, f2)) {
            this.mLoadingAnimator.cancel();
            startShowQuickOpenItemAnimation();
        }
        IQuickOpenItem caculateSelectQucikOpenItem = caculateSelectQucikOpenItem(f, f2);
        IQuickOpenItem iQuickOpenItem = this.mCurrentSelectItem;
        if (!(iQuickOpenItem == null || iQuickOpenItem == caculateSelectQucikOpenItem)) {
            handleQucikOpenItemExit(iQuickOpenItem);
        }
        if (!(caculateSelectQucikOpenItem == null || this.mCurrentSelectItem == caculateSelectQucikOpenItem)) {
            handleQucikOpenItemEnter(caculateSelectQucikOpenItem);
        }
        this.mCurrentSelectItem = caculateSelectQucikOpenItem;
        invalidate();
    }

    public void setQuickViewListener(QuickViewListener quickViewListener) {
        this.mQuickViewListener = quickViewListener;
    }

    public void resetFingerID() {
        if (this.mFingerID != 0 && MiuiGxzwQuickOpenUtil.isQuickOpenEnable(getContext())) {
            this.mFingerID = 0;
            this.mUiOffloadThread.submit(new Runnable() {
                public void run() {
                    MiuiGxzwQuickOpenUtil.setFodAuthFingerprint(MiuiGxzwQuickOpenView.this.getContext(), 0, KeyguardUpdateMonitor.getCurrentUser());
                }
            });
        }
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.mPendingUpdateLp && isAttachedToWindow()) {
            this.mWindowManager.updateViewLayout(this, this.mLayoutParams);
        }
        this.mPendingUpdateLp = false;
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mPendingUpdateLp = false;
    }

    public boolean dispatchKeyEvent(KeyEvent keyEvent) {
        if (keyEvent.getKeyCode() != 4) {
            return super.dispatchKeyEvent(keyEvent);
        }
        if (keyEvent.getAction() != 0) {
            return true;
        }
        dismiss();
        return true;
    }

    private void initView() {
        setSystemUiVisibility(4864);
        updatePixelSize();
        this.mLayoutParams = new WindowManager.LayoutParams(-1, -1, 2009, 84083968, -2);
        this.mLayoutParams.setTitle("gxzw_quick_open");
        this.mLayoutParams.screenOrientation = 1;
        this.mSecurityManager = (SecurityManager) getContext().getSystemService("security");
        MiuiGxzwUtils.caculateGxzwIconSize(getContext());
        int i = MiuiGxzwUtils.GXZW_ICON_X + (MiuiGxzwUtils.GXZW_ICON_WIDTH / 2);
        int i2 = MiuiGxzwUtils.GXZW_ICON_Y + (MiuiGxzwUtils.GXZW_ICON_HEIGHT / 2);
        float f = (float) i;
        float f2 = this.mItemRadius;
        float f3 = (float) i2;
        this.mFingerRect = new RectF(f - f2, f3 - f2, f + f2, f2 + f3);
        float dimension = getResources().getDimension(R.dimen.gxzw_quick_open_region_samll);
        this.mFastRect = new RectF(f - dimension, f3 - dimension, f + dimension, f3 + dimension);
        this.mPaint = new Paint();
        this.mPaint.setAntiAlias(true);
        this.mPaint.setStyle(Paint.Style.FILL);
        this.mPaint.setColor(872415231);
        Display display = ((DisplayManager) getContext().getSystemService("display")).getDisplay(0);
        Point point = new Point();
        display.getRealSize(point);
        this.mScreenHeight = point.y;
        setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (!MiuiGxzwQuickOpenView.this.mTeachMode && !AccessibilityManager.getInstance(MiuiGxzwQuickOpenView.this.getContext()).isTouchExplorationEnabled()) {
                    MiuiGxzwQuickOpenView.this.dismiss();
                }
            }
        });
        this.mTitleContainer = new LinearLayout(getContext());
        this.mTitleContainer.setOrientation(1);
        this.mTitleView = new TextView(getContext());
        this.mTitleView.setTextColor(-1);
        this.mTitleView.setGravity(17);
        this.mTitleContainer.addView(this.mTitleView, new LinearLayout.LayoutParams(-1, -2));
        this.mSubTitleView = new TextView(getContext());
        this.mSubTitleView.setTextColor(-1694498817);
        this.mSubTitleView.setGravity(17);
        this.mTitleContainer.addView(this.mSubTitleView, new LinearLayout.LayoutParams(-1, -2));
        this.mTitleLayoutParams = new FrameLayout.LayoutParams(-1, -2);
        FrameLayout.LayoutParams layoutParams = this.mTitleLayoutParams;
        layoutParams.gravity = 80;
        layoutParams.bottomMargin = (this.mScreenHeight - i2) + this.mTitleMargin;
        addView(this.mTitleContainer, layoutParams);
        this.mTipView = new TextView(getContext());
        this.mTipView.setTextColor(-16777216);
        this.mTipView.setGravity(17);
        this.mTipView.setVisibility(4);
        this.mTipView.setBackgroundResource(R.drawable.gxzw_quick_tip_background);
        this.mTipLayoutParams = new FrameLayout.LayoutParams(-2, -2);
        FrameLayout.LayoutParams layoutParams2 = this.mTipLayoutParams;
        layoutParams2.gravity = 81;
        layoutParams2.bottomMargin = (this.mScreenHeight - i2) + this.mTipPressMargin;
        addView(this.mTipView, layoutParams2);
        updateTextSize();
        MiuiGxzwQuickOpenUtil.loadSharedPreferencesValue(getContext());
    }

    /* access modifiers changed from: private */
    public void updateTextSize() {
        this.mTitleView.setTextSize(0, (float) getResources().getDimensionPixelSize(R.dimen.gxzw_quick_open_title_size));
        this.mSubTitleView.setTextSize(0, (float) getResources().getDimensionPixelSize(R.dimen.gxzw_quick_open_subtitle_size));
        this.mTipView.setTextSize(0, (float) getResources().getDimensionPixelSize(R.dimen.gxzw_quick_open_tip_size));
    }

    /* access modifiers changed from: private */
    public void updatePixelSize() {
        this.mItemRadius = getContext().getResources().getDimension(R.dimen.gxzw_quick_open_item_radius);
        this.mItemRadius += MiuiGxzwQuickOpenUtil.getLargeItemDetal(getContext());
        this.mItemScaleRadius = getContext().getResources().getDimension(R.dimen.gxzw_quick_open_item_scale_radius);
        this.mSelectBackgroundRadius = getContext().getResources().getDimension(R.dimen.gxzw_quick_open_item_background_radius);
        this.mCicleRadius = getContext().getResources().getDimension(R.dimen.gxzw_quick_open_circle_radius);
        this.mTitleMargin = (int) getContext().getResources().getDimension(R.dimen.gxzw_quick_open_title_margin);
        this.mTipPressMargin = (int) getContext().getResources().getDimension(R.dimen.gxzw_quick_open_tip_press_margin);
        this.mTipSlideMargin = (int) getContext().getResources().getDimension(R.dimen.gxzw_quick_open_tip_slide_margin);
        float f = (float) (MiuiGxzwUtils.GXZW_ICON_X + (MiuiGxzwUtils.GXZW_ICON_WIDTH / 2));
        float f2 = this.mItemRadius;
        float f3 = (float) (MiuiGxzwUtils.GXZW_ICON_Y + (MiuiGxzwUtils.GXZW_ICON_HEIGHT / 2));
        this.mFingerRect = new RectF(f - f2, f3 - f2, f + f2, f2 + f3);
        float dimension = getResources().getDimension(R.dimen.gxzw_quick_open_region_samll);
        this.mFastRect = new RectF(f - dimension, f3 - dimension, f + dimension, f3 + dimension);
    }

    private void initQuickOpenItemList() {
        cleanQuickOpenItemList();
        this.mQuickOpenItemList.addAll(MiuiGxzwQuickOpenUtil.generateQuickOpenItemList(getContext(), this.mItemRadius, this.mCicleRadius, isRTL()));
        for (IQuickOpenItem next : this.mQuickOpenItemList) {
            RectF rect = next.getRect();
            View view = next.getView();
            view.setVisibility(4);
            int width = (int) (rect.width() * 0.1f);
            if (AccessibilityManager.getInstance(getContext()).isTouchExplorationEnabled()) {
                float f = (float) width;
                RectF rectF = new RectF(rect.left - f, rect.top - f, rect.right + f, rect.bottom + f);
                view.setPadding(width, width, width, width);
                rect = rectF;
            }
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams((int) rect.width(), (int) rect.height());
            layoutParams.gravity = 51;
            layoutParams.leftMargin = (int) rect.left;
            layoutParams.topMargin = (int) rect.top;
            addView(view, layoutParams);
        }
    }

    private void cleanQuickOpenItemList() {
        for (IQuickOpenItem next : this.mQuickOpenItemList) {
            next.getView().setVisibility(4);
            if (next.getView().isAttachedToWindow()) {
                removeView(next.getView());
            }
        }
        this.mQuickOpenItemList.clear();
    }

    /* access modifiers changed from: protected */
    public WindowManager.LayoutParams generateLayoutParams() {
        return this.mLayoutParams;
    }

    /* access modifiers changed from: private */
    public void removeQuickView() {
        WindowManager.LayoutParams layoutParams = this.mLayoutParams;
        layoutParams.blurRatio = 0.0f;
        layoutParams.flags &= -5;
        removeViewFromWindow();
        setVisibility(8);
        cleanQuickOpenItemList();
        showTitle("", "");
        this.mTipView.setVisibility(4);
        this.mCurrentSelectItem = null;
        TextView textView = this.mSkipTeach;
        if (textView != null) {
            removeView(textView);
            this.mSkipTeach = null;
        }
        MiuiGxzwQuickTeachView miuiGxzwQuickTeachView = this.mMiuiGxzwQuickTeachView;
        if (miuiGxzwQuickTeachView != null) {
            miuiGxzwQuickTeachView.stopTeachAnim();
            removeView(this.mMiuiGxzwQuickTeachView);
            this.mMiuiGxzwQuickTeachView = null;
        }
        View view = this.mCloseView;
        if (view != null) {
            removeView(view);
            this.mCloseView = null;
        }
        this.mTeachMode = false;
        this.mTeachTouchDown = false;
        this.mItemsExpanded = false;
        ObjectAnimator objectAnimator = this.mLoadingAnimator;
        if (objectAnimator != null) {
            objectAnimator.cancel();
        }
        Animator animator = this.mItemExpandAnimator;
        if (animator != null) {
            animator.cancel();
            this.mItemExpandAnimator = null;
        }
    }

    private IQuickOpenItem caculateSelectQucikOpenItem(float f, float f2) {
        for (IQuickOpenItem next : this.mQuickOpenItemList) {
            if (isInItemArea(next, f, f2)) {
                return next;
            }
        }
        return null;
    }

    private void handleQucikOpenItemEnter(IQuickOpenItem iQuickOpenItem) {
        new ObjectAnimator();
        ObjectAnimator ofFloat = ObjectAnimator.ofFloat(iQuickOpenItem.getView(), View.SCALE_X, new float[]{iQuickOpenItem.getView().getScaleX(), this.mItemScaleRadius / this.mItemRadius});
        new ObjectAnimator();
        ObjectAnimator ofFloat2 = ObjectAnimator.ofFloat(iQuickOpenItem.getView(), View.SCALE_Y, new float[]{iQuickOpenItem.getView().getScaleY(), this.mItemScaleRadius / this.mItemRadius});
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(new Animator[]{ofFloat, ofFloat2});
        animatorSet.setDuration(100);
        animatorSet.start();
        MiuiGxzwUtils.vibrateLight(getContext());
        showTitle(iQuickOpenItem.getTitle(), iQuickOpenItem.getSubTitle());
        this.mTipView.setVisibility(4);
    }

    private void handleQucikOpenItemExit(IQuickOpenItem iQuickOpenItem) {
        new ObjectAnimator();
        ObjectAnimator ofFloat = ObjectAnimator.ofFloat(iQuickOpenItem.getView(), View.SCALE_X, new float[]{iQuickOpenItem.getView().getScaleX(), 1.0f});
        new ObjectAnimator();
        ObjectAnimator ofFloat2 = ObjectAnimator.ofFloat(iQuickOpenItem.getView(), View.SCALE_Y, new float[]{iQuickOpenItem.getView().getScaleY(), 1.0f});
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(new Animator[]{ofFloat, ofFloat2});
        animatorSet.setDuration(100);
        animatorSet.start();
        showTitle("", "");
    }

    /* access modifiers changed from: private */
    public void handleQucikOpenItemTouchUp(IQuickOpenItem iQuickOpenItem) {
        List list;
        Intent intent = iQuickOpenItem.getIntent();
        if (intent != null) {
            AnalyticsHelper.getInstance(getContext()).recordFodQuickOpenAppAction(iQuickOpenItem.getTag());
            String str = null;
            if (!TextUtils.isEmpty(intent.getPackage())) {
                str = intent.getPackage();
            } else if (intent.getComponent() != null && !TextUtils.isEmpty(intent.getComponent().getPackageName())) {
                str = intent.getComponent().getPackageName();
            }
            if (str != null) {
                boolean needStartProcess = iQuickOpenItem.needStartProcess();
                boolean startActionByService = iQuickOpenItem.startActionByService();
                if (startActionByService) {
                    list = getContext().getPackageManager().queryIntentServicesAsUser(intent, 65536, KeyguardUpdateMonitor.getCurrentUser());
                } else {
                    list = getContext().getPackageManager().queryIntentActivitiesAsUser(intent, 65536, KeyguardUpdateMonitor.getCurrentUser());
                }
                if (list == null || list.size() <= 0) {
                    Intent intent2 = new Intent("android.intent.action.VIEW", Uri.parse("market://details?id=" + str));
                    intent2.addFlags(MiuiHapticFeedbackConstants.FLAG_MIUI_HAPTIC_TAP_NORMAL);
                    startActivitySafely(intent2);
                } else {
                    boolean isAppLocked = AppLockHelper.isAppLocked(getContext(), this.mSecurityManager, str, KeyguardUpdateMonitor.getCurrentUser());
                    if (this.mFingerID != 0 && isAppLocked && (needStartProcess || !startActionByService)) {
                        intent.putExtra("fod_quick_open", true);
                        MiuiGxzwQuickOpenUtil.setFodAuthFingerprint(getContext(), this.mFingerID, KeyguardUpdateMonitor.getCurrentUser());
                    }
                    if (needStartProcess && !isAppLocked) {
                        startActivitySafely(getContext().getPackageManager().getLaunchIntentForPackage(str));
                    } else if (needStartProcess) {
                        intent.putExtra("quick_open_start_process", true);
                    }
                    if (startActionByService) {
                        startServiceSafely(intent);
                    } else {
                        startActivitySafely(intent);
                    }
                }
                MiuiGxzwQuickOpenUtil.disableShowQuickOpenTeach(getContext());
            }
        }
    }

    private void startActivitySafely(Intent intent) {
        try {
            getContext().startActivityAsUser(intent, new UserHandle(KeyguardUpdateMonitor.getCurrentUser()));
        } catch (Exception e) {
            Log.w("MiuiGxzwQuickOpenView", "start activity filed " + e);
        }
    }

    private void startServiceSafely(Intent intent) {
        try {
            getContext().startForegroundServiceAsUser(intent, new UserHandle(KeyguardUpdateMonitor.getCurrentUser()));
        } catch (Exception e) {
            Log.w("MiuiGxzwQuickOpenView", "start service filed " + e);
        }
    }

    /* access modifiers changed from: private */
    public void startLoadingAnimation() {
        MiuiGxzwQuickLoadingView miuiGxzwQuickLoadingView = this.mQuickLoadingView;
        if (miuiGxzwQuickLoadingView != null) {
            removeView(miuiGxzwQuickLoadingView);
            this.mQuickLoadingView = null;
        }
        ObjectAnimator objectAnimator = this.mLoadingAnimator;
        if (objectAnimator != null) {
            objectAnimator.cancel();
        }
        this.mQuickLoadingView = new MiuiGxzwQuickLoadingView(getContext(), this.mItemRadius);
        float loadingMaxRadius = this.mQuickLoadingView.getLoadingMaxRadius();
        MiuiGxzwUtils.caculateGxzwIconSize(getContext());
        int i = MiuiGxzwUtils.GXZW_ICON_X + (MiuiGxzwUtils.GXZW_ICON_WIDTH / 2);
        int i2 = MiuiGxzwUtils.GXZW_ICON_Y + (MiuiGxzwUtils.GXZW_ICON_HEIGHT / 2);
        int i3 = (int) (2.0f * loadingMaxRadius);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(i3, i3);
        layoutParams.gravity = 51;
        int i4 = (int) loadingMaxRadius;
        layoutParams.leftMargin = i - i4;
        layoutParams.topMargin = i2 - i4;
        addView(this.mQuickLoadingView, layoutParams);
        this.mQuickLoadingView.setLoading(true);
        new ObjectAnimator();
        MiuiGxzwQuickLoadingView miuiGxzwQuickLoadingView2 = this.mQuickLoadingView;
        this.mLoadingAnimator = ObjectAnimator.ofFloat(miuiGxzwQuickLoadingView2, "currentLoadingRadius", new float[]{miuiGxzwQuickLoadingView2.getLoadingOriginalRadius(), this.mQuickLoadingView.getLoadingMaxRadius()});
        this.mLoadingAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                MiuiGxzwQuickOpenView.this.mQuickLoadingView.setCurrentLoadingRadius(((Float) valueAnimator.getAnimatedValue()).floatValue());
            }
        });
        this.mLoadingAnimator.addListener(new Animator.AnimatorListener() {
            private boolean canceled = false;

            public void onAnimationRepeat(Animator animator) {
            }

            public void onAnimationStart(Animator animator) {
                MiuiGxzwQuickOpenView.this.showPressTipIfNeed();
            }

            public void onAnimationEnd(Animator animator) {
                ObjectAnimator unused = MiuiGxzwQuickOpenView.this.mLoadingAnimator = null;
                boolean unused2 = MiuiGxzwQuickOpenView.this.mLoading = false;
                MiuiGxzwQuickOpenView.this.mQuickLoadingView.setLoading(false);
                if (!this.canceled && MiuiGxzwQuickOpenView.this.mShowed) {
                    MiuiGxzwQuickOpenView.this.startShowQuickOpenItemAnimation();
                }
            }

            public void onAnimationCancel(Animator animator) {
                this.canceled = true;
            }
        });
        this.mLoadingAnimator.setDuration(600);
        this.mLoadingAnimator.start();
        this.mLoading = true;
    }

    /* access modifiers changed from: private */
    public void startShowQuickOpenItemAnimation() {
        WindowManager.LayoutParams layoutParams = this.mLayoutParams;
        layoutParams.blurRatio = 1.0f;
        layoutParams.flags |= 4;
        if (isAttachedToWindow()) {
            this.mWindowManager.updateViewLayout(this, this.mLayoutParams);
        } else {
            this.mPendingUpdateLp = true;
        }
        initQuickOpenItemList();
        MiuiGxzwUtils.caculateGxzwIconSize(getContext());
        int i = MiuiGxzwUtils.GXZW_ICON_X + (MiuiGxzwUtils.GXZW_ICON_WIDTH / 2);
        int i2 = MiuiGxzwUtils.GXZW_ICON_Y + (MiuiGxzwUtils.GXZW_ICON_HEIGHT / 2);
        Animator animator = this.mItemExpandAnimator;
        if (animator != null) {
            animator.cancel();
        }
        boolean z = false;
        for (IQuickOpenItem next : this.mQuickOpenItemList) {
            next.getView().setVisibility(0);
            new ObjectAnimator();
            ObjectAnimator ofFloat = ObjectAnimator.ofFloat(next.getView(), View.TRANSLATION_X, new float[]{((float) i) - next.getRect().centerX(), 0.0f});
            new ObjectAnimator();
            ObjectAnimator ofFloat2 = ObjectAnimator.ofFloat(next.getView(), View.TRANSLATION_Y, new float[]{((float) i2) - next.getRect().centerY(), 0.0f});
            AnimatorSet animatorSet = new AnimatorSet();
            if (!z) {
                this.mItemExpandAnimator = animatorSet;
                this.mItemExpandAnimator.addListener(new Animator.AnimatorListener() {
                    private boolean canceled = false;

                    public void onAnimationRepeat(Animator animator) {
                    }

                    public void onAnimationStart(Animator animator) {
                    }

                    public void onAnimationEnd(Animator animator) {
                        AnalyticsHelper.getInstance(MiuiGxzwQuickOpenView.this.getContext()).recordFodQuickOpenExpandResultAction(!this.canceled);
                        Animator unused = MiuiGxzwQuickOpenView.this.mItemExpandAnimator = null;
                        if (MiuiGxzwQuickOpenView.this.mQuickViewListener != null && !this.canceled) {
                            MiuiGxzwQuickOpenView.this.mQuickViewListener.onShow();
                        }
                        if (!this.canceled) {
                            boolean unused2 = MiuiGxzwQuickOpenView.this.mItemsExpanded = true;
                        }
                        if (!this.canceled && AccessibilityManager.getInstance(MiuiGxzwQuickOpenView.this.getContext()).isTouchExplorationEnabled()) {
                            MiuiGxzwQuickOpenView.this.initTalkbackInfo();
                        }
                    }

                    public void onAnimationCancel(Animator animator) {
                        this.canceled = true;
                    }
                });
                z = true;
            }
            animatorSet.playTogether(new Animator[]{ofFloat, ofFloat2});
            animatorSet.setDuration(150);
            animatorSet.start();
        }
        MiuiGxzwUtils.vibrateNormal(getContext());
        showSlideTipIfNeed();
    }

    /* access modifiers changed from: private */
    public void initTalkbackInfo() {
        announceForAccessibility(getResources().getString(R.string.gxzw_quick_open_title));
        for (final IQuickOpenItem next : this.mQuickOpenItemList) {
            String title = next.getTitle();
            String subTitle = next.getSubTitle();
            View view = next.getView();
            view.setContentDescription(subTitle + " " + title);
            next.getView().setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    MiuiGxzwQuickOpenView.this.handleQucikOpenItemTouchUp(next);
                    MiuiGxzwQuickOpenView.this.dismiss();
                }
            });
        }
        float width = this.mFingerRect.width() * 0.1f;
        RectF rectF = this.mFingerRect;
        RectF rectF2 = new RectF(rectF.left - width, rectF.top - width, rectF.right + width, rectF.bottom + width);
        this.mCloseView = new View(getContext());
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams((int) rectF2.width(), (int) rectF2.height());
        layoutParams.gravity = 51;
        layoutParams.leftMargin = (int) rectF2.left;
        layoutParams.topMargin = (int) rectF2.top;
        addView(this.mCloseView, layoutParams);
        this.mHandler.postDelayed(new Runnable() {
            public void run() {
                if (MiuiGxzwQuickOpenView.this.mCloseView != null) {
                    MiuiGxzwQuickOpenView.this.mCloseView.setContentDescription(MiuiGxzwQuickOpenView.this.getResources().getString(R.string.gxzw_quick_close));
                    MiuiGxzwQuickOpenView.this.mCloseView.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View view) {
                            MiuiGxzwQuickOpenView.this.dismiss();
                        }
                    });
                }
            }
        }, 1000);
    }

    private void startDismissAnimation() {
        MiuiGxzwUtils.caculateGxzwIconSize(getContext());
        int i = MiuiGxzwUtils.GXZW_ICON_X + (MiuiGxzwUtils.GXZW_ICON_WIDTH / 2);
        int i2 = MiuiGxzwUtils.GXZW_ICON_Y + (MiuiGxzwUtils.GXZW_ICON_HEIGHT / 2);
        for (IQuickOpenItem next : this.mQuickOpenItemList) {
            new ObjectAnimator();
            ObjectAnimator ofFloat = ObjectAnimator.ofFloat(next.getView(), View.TRANSLATION_X, new float[]{next.getView().getTranslationX(), ((float) i) - next.getRect().centerX()});
            new ObjectAnimator();
            ObjectAnimator ofFloat2 = ObjectAnimator.ofFloat(next.getView(), View.TRANSLATION_Y, new float[]{next.getView().getTranslationY(), ((float) i2) - next.getRect().centerY()});
            new ObjectAnimator();
            ObjectAnimator ofFloat3 = ObjectAnimator.ofFloat(next.getView(), View.ALPHA, new float[]{1.0f, 0.0f});
            new ObjectAnimator();
            ObjectAnimator ofFloat4 = ObjectAnimator.ofFloat(next.getView(), View.SCALE_X, new float[]{next.getView().getScaleX(), 0.0f});
            new ObjectAnimator();
            ObjectAnimator ofFloat5 = ObjectAnimator.ofFloat(next.getView(), View.SCALE_Y, new float[]{next.getView().getScaleY(), 0.0f});
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(new Animator[]{ofFloat, ofFloat2, ofFloat3, ofFloat4, ofFloat5});
            animatorSet.setDuration(150);
            animatorSet.start();
        }
        if (this.mQuickLoadingView != null) {
            new ObjectAnimator();
            ObjectAnimator ofFloat6 = ObjectAnimator.ofFloat(this.mQuickLoadingView, View.ALPHA, new float[]{1.0f, 0.0f});
            ofFloat6.setDuration(150);
            ofFloat6.addListener(new Animator.AnimatorListener() {
                public void onAnimationCancel(Animator animator) {
                }

                public void onAnimationRepeat(Animator animator) {
                }

                public void onAnimationStart(Animator animator) {
                }

                public void onAnimationEnd(Animator animator) {
                    MiuiGxzwQuickOpenView.this.removeQuickView();
                }
            });
            ofFloat6.start();
        } else {
            removeQuickView();
        }
        MiuiGxzwQuickTeachView miuiGxzwQuickTeachView = this.mMiuiGxzwQuickTeachView;
        if (miuiGxzwQuickTeachView != null) {
            miuiGxzwQuickTeachView.stopTeachAnim();
            removeView(this.mMiuiGxzwQuickTeachView);
            this.mMiuiGxzwQuickTeachView = null;
        }
        this.mTipView.setVisibility(4);
    }

    private boolean isInItemArea(IQuickOpenItem iQuickOpenItem, float f, float f2) {
        return iQuickOpenItem.getRegion().contains((int) f, (int) f2);
    }

    private void showTitle(String str, String str2) {
        int i = MiuiGxzwUtils.GXZW_ICON_Y + (MiuiGxzwUtils.GXZW_ICON_HEIGHT / 2);
        FrameLayout.LayoutParams layoutParams = this.mTitleLayoutParams;
        layoutParams.bottomMargin = (this.mScreenHeight - i) + this.mTitleMargin;
        updateViewLayout(this.mTitleContainer, layoutParams);
        this.mTitleView.setText(str);
        this.mSubTitleView.setText(str2);
    }

    /* access modifiers changed from: private */
    public void showPressTipIfNeed() {
        if (MiuiGxzwQuickOpenUtil.isShowQuickOpenPress(getContext())) {
            int i = MiuiGxzwUtils.GXZW_ICON_Y + (MiuiGxzwUtils.GXZW_ICON_HEIGHT / 2);
            FrameLayout.LayoutParams layoutParams = this.mTipLayoutParams;
            layoutParams.bottomMargin = (this.mScreenHeight - i) + this.mTipPressMargin;
            updateViewLayout(this.mTipView, layoutParams);
            this.mTipView.setTranslationY(0.0f);
            this.mTipView.setVisibility(0);
            this.mTipView.setText(R.string.gxzw_quick_tip_press);
            MiuiGxzwQuickOpenUtil.increaseShowQuickOpenPressCount(getContext());
        }
    }

    private void showSlideTipIfNeed() {
        if (MiuiGxzwQuickOpenUtil.isShowQuickOpenSlide(getContext())) {
            int i = MiuiGxzwUtils.GXZW_ICON_Y + (MiuiGxzwUtils.GXZW_ICON_HEIGHT / 2);
            FrameLayout.LayoutParams layoutParams = this.mTipLayoutParams;
            layoutParams.bottomMargin = (this.mScreenHeight - i) + this.mTipPressMargin;
            updateViewLayout(this.mTipView, layoutParams);
            this.mTipView.setTranslationY(0.0f);
            new ObjectAnimator();
            TextView textView = this.mTipView;
            ObjectAnimator ofFloat = ObjectAnimator.ofFloat(textView, View.TRANSLATION_Y, new float[]{textView.getTranslationY(), (float) (this.mTipPressMargin - this.mTipSlideMargin)});
            ofFloat.setDuration(100);
            ofFloat.start();
            this.mTipView.setVisibility(0);
            this.mTipView.setText(R.string.gxzw_quick_tip_slide);
            MiuiGxzwQuickOpenUtil.disableShowQuickOpenSlide(getContext());
        }
    }

    private void enterTeachMode() {
        this.mTeachMode = true;
        if (this.mSkipTeach == null) {
            this.mSkipTeach = new TextView(getContext());
            this.mSkipTeach.setTextColor(-1694498817);
            this.mSkipTeach.setTextSize(0, (float) getResources().getDimensionPixelSize(R.dimen.gxzw_quick_open_skip_teach));
            this.mSkipTeach.setText(R.string.gxzw_quick_open_skip_teach);
            this.mSkipTeach.setBackgroundResource(R.drawable.gxzw_quick_open_skip_teach_b);
            int dimension = (int) getResources().getDimension(R.dimen.gxzw_quick_open_skip_teach_padding_horizontal);
            int dimension2 = (int) getResources().getDimension(R.dimen.gxzw_quick_open_skip_teach_padding_vertical);
            this.mSkipTeach.setPadding(dimension, dimension2, dimension, dimension2);
            this.mSkipTeach.setGravity(17);
            this.mSkipTeach.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    MiuiGxzwQuickOpenView.this.dismiss();
                }
            });
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(-2, -2);
            layoutParams.gravity = 8388661;
            layoutParams.setMarginEnd((int) getResources().getDimension(R.dimen.gxzw_quick_open_skip_teach_margin_end));
            layoutParams.topMargin = (int) getResources().getDimension(R.dimen.gxzw_quick_open_skip_teach_margin_top);
            addView(this.mSkipTeach, layoutParams);
        }
        if (this.mMiuiGxzwQuickTeachView == null) {
            this.mMiuiGxzwQuickTeachView = new MiuiGxzwQuickTeachView(getContext(), this.mItemRadius);
            RectF rectF = this.mFingerRect;
            RectF rectF2 = new RectF(rectF.left, rectF.top - this.mCicleRadius, rectF.right, rectF.bottom);
            FrameLayout.LayoutParams layoutParams2 = new FrameLayout.LayoutParams((int) rectF2.width(), (int) rectF2.height());
            layoutParams2.gravity = 51;
            layoutParams2.leftMargin = (int) rectF2.left;
            layoutParams2.topMargin = (int) rectF2.top;
            addView(this.mMiuiGxzwQuickTeachView, layoutParams2);
            if (this.mQuickOpenItemList.size() > 0) {
                this.mMiuiGxzwQuickTeachView.setPivotX(rectF2.width() / 2.0f);
                this.mMiuiGxzwQuickTeachView.setPivotY(rectF2.height() - this.mItemRadius);
                this.mMiuiGxzwQuickTeachView.setRotation(MiuiGxzwQuickOpenUtil.getTeachViewRotation(this.mQuickOpenItemList.size()));
            }
        }
        this.mMiuiGxzwQuickTeachView.startTeachAnim();
        MiuiGxzwQuickOpenUtil.disableShowQuickOpenTeach(getContext());
    }

    private boolean isRTL() {
        return (getResources().getConfiguration().screenLayout & 192) == 128;
    }
}
