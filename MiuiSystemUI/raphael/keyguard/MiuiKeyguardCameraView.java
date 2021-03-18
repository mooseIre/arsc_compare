package com.android.keyguard;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.app.ActivityManager;
import android.app.ActivityTaskManager;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.Vibrator;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.android.keyguard.analytics.AnalyticsHelper;
import com.android.keyguard.utils.MiuiKeyguardUtils;
import com.android.keyguard.utils.PackageUtils;
import com.android.keyguard.wallpaper.IMiuiKeyguardWallpaperController;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.Dependency;
import com.android.systemui.shared.system.ActivityManagerWrapper;
import com.android.systemui.shared.system.TaskStackChangeListener;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.miui.systemui.DeviceConfig;
import com.miui.systemui.util.BlurUtil;
import com.miui.systemui.util.HapticFeedBackImpl;
import java.util.List;

public class MiuiKeyguardCameraView extends FrameLayout implements IMiuiKeyguardWallpaperController.IWallpaperChangeCallback, ConfigurationController.ConfigurationListener {
    private float mActiveAnimPer;
    private AnimatorSet mAnimatorSet;
    private float mBackAnimAspectRatio;
    private ValueAnimator mBackgroundAnimator;
    private AnimatorSet mBackgroundAnimatorSet;
    private View mBackgroundView;
    private CallBack mCallBack;
    private View mCameraScrimView;
    private Configuration mConfiguration = new Configuration();
    private Context mContext = getContext();
    private boolean mDarkStyle;
    private float mIconActiveCenterX;
    private float mIconActiveCenterY;
    private float mIconActiveWidth;
    private float mIconAlpha;
    private float mIconCenterX;
    private float mIconCenterY;
    private float mIconCircleAlpha;
    private float mIconCircleCenterX;
    private float mIconCircleCenterY;
    private float mIconCircleHeight;
    private Paint mIconCirclePaint;
    private Paint mIconCircleStrokePaint;
    private float mIconCircleWidth;
    private int mIconHeight;
    private float mIconInitCenterX;
    private float mIconInitCenterY;
    private float mIconScale;
    private ImageView mIconView;
    private int mIconWidth;
    private float mInitialTouchX;
    private float mInitialTouchY;
    private boolean mIsActive;
    private boolean mIsBackAnimRunning;
    private boolean mIsCameraShowing;
    private boolean mIsCorrectOperation;
    private boolean mIsPendingStartCamera;
    private KeyguardUpdateMonitor mKeyguardUpdateMonitor;
    private MiuiKeyguardUpdateMonitorCallback mKeyguardUpdateMonitorCallback = new MiuiKeyguardUpdateMonitorCallback() {
        /* class com.android.keyguard.MiuiKeyguardCameraView.AnonymousClass1 */

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onKeyguardVisibilityChanged(boolean z) {
            if (!z) {
                if (MiuiKeyguardCameraView.this.mIsPendingStartCamera) {
                    MiuiKeyguardCameraView.this.mIsPendingStartCamera = false;
                    MiuiKeyguardCameraView.this.mIsCameraShowing = true;
                    if (MiuiKeyguardCameraView.this.mBackgroundAnimatorSet != null && !MiuiKeyguardCameraView.this.mBackgroundAnimatorSet.isRunning()) {
                        MiuiKeyguardCameraView.this.setAlpha(0.0f);
                        MiuiKeyguardCameraView.this.applyBlurRatio(0.0f);
                        MiuiKeyguardCameraView.this.updateKeepScreenOnFlag(false);
                        MiuiKeyguardCameraView.this.mBackgroundAnimatorSet = null;
                        return;
                    }
                    return;
                }
                MiuiKeyguardCameraView.this.reset();
            } else if (MiuiKeyguardCameraView.this.mIsCameraShowing) {
                MiuiKeyguardCameraView.this.mIsCameraShowing = false;
                MiuiKeyguardCameraView.this.startBackAnim();
            }
        }
    };
    private boolean mLastIsActive;
    private WindowManager.LayoutParams mLayoutParams;
    private WindowManager.LayoutParams mLpChanged;
    private float mMoveActivePer;
    private float mMoveDistance;
    private float mMovePer;
    private float mMoveYPer;
    private ImageView mPreView;
    private float mPreViewAlpha;
    private float mPreViewCenterX;
    private float mPreViewCenterY;
    private LinearLayout mPreViewContainer;
    private FrameLayout.LayoutParams mPreViewContainerLayoutParams;
    private float mPreViewHeight;
    private float mPreViewInitRadius = 60.0f;
    private ViewOutlineProvider mPreViewOutlineProvider = new ViewOutlineProvider() {
        /* class com.android.keyguard.MiuiKeyguardCameraView.AnonymousClass2 */

        public void getOutline(View view, Outline outline) {
            outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), MiuiKeyguardCameraView.this.mPreViewRadius);
        }
    };
    private float mPreViewRadius;
    private float mPreViewWidth;
    private int mScreenHeight;
    private Point mScreenSizePoint;
    private int mScreenWidth;
    private boolean mShowing = false;
    private final TaskStackChangeListener mTaskStackListener = new TaskStackChangeListener() {
        /* class com.android.keyguard.MiuiKeyguardCameraView.AnonymousClass3 */

        @Override // com.android.systemui.shared.system.TaskStackChangeListener
        public void onTaskMovedToFront(ActivityManager.RunningTaskInfo runningTaskInfo) {
            Log.d("KeyguardCameraView", "xxxxxxxxonTaskMovedToFront");
            if (MiuiKeyguardCameraView.this.mIsCameraShowing && MiuiKeyguardCameraView.this.isNotCameraActivity(runningTaskInfo)) {
                MiuiKeyguardCameraView.this.mIsCameraShowing = false;
                MiuiKeyguardCameraView.this.reset();
            }
        }

        @Override // com.android.systemui.shared.system.TaskStackChangeListener
        public void onTaskStackChanged() {
            try {
                List tasks = ActivityTaskManager.getService().getTasks(1);
                if (MiuiKeyguardCameraView.this.mIsCameraShowing && !tasks.isEmpty() && MiuiKeyguardCameraView.this.isNotCameraActivity((ActivityManager.RunningTaskInfo) tasks.get(0))) {
                    MiuiKeyguardCameraView.this.mIsCameraShowing = false;
                    MiuiKeyguardCameraView.this.reset();
                }
            } catch (RemoteException e) {
                Log.e("KeyguardCameraView", "am.getTasks fail " + e.getStackTrace());
                e.printStackTrace();
            }
        }
    };
    private boolean mTouchDownInitial;
    private float mTouchX;
    private float mTouchY;
    private float mVirHeight;
    private float mVirWidth;
    private float mVirX;
    private float mVirY;
    private WindowManager mWindowManager;

    public interface CallBack {
        void onAnimUpdate(float f);

        void onBackAnimationEnd();

        void onCancelAnimationEnd(boolean z);

        void onCompletedAnimationEnd();

        void onVisibilityChanged(boolean z);

        void updatePreViewBackground();
    }

    private float perFromVal(float f, float f2, float f3) {
        return (f - f2) / (f3 - f2);
    }

    private float valFromPer(float f, float f2, float f3) {
        return f2 + ((f3 - f2) * f);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isNotCameraActivity(ActivityManager.RunningTaskInfo runningTaskInfo) {
        return !PackageUtils.PACKAGE_NAME_CAMERA.equals(runningTaskInfo.topActivity.getPackageName());
    }

    public MiuiKeyguardCameraView(Context context, CallBack callBack) {
        super(context);
        this.mCallBack = callBack;
        initViews();
        this.mKeyguardUpdateMonitor = (KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class);
        Vibrator vibrator = (Vibrator) this.mContext.getSystemService("vibrator");
        Paint paint = new Paint();
        this.mIconCirclePaint = paint;
        paint.setColor(0);
        this.mIconCirclePaint.setStyle(Paint.Style.FILL);
        this.mIconCirclePaint.setAntiAlias(true);
        Paint paint2 = new Paint();
        this.mIconCircleStrokePaint = paint2;
        paint2.setColor(16777215);
        this.mIconCircleStrokePaint.setAlpha(51);
        this.mIconCircleStrokePaint.setStyle(Paint.Style.STROKE);
        this.mIconCircleStrokePaint.setStrokeWidth(2.0f);
        this.mIconCircleStrokePaint.setAntiAlias(true);
        setWillNotDraw(false);
        addViewToWindow();
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        ((IMiuiKeyguardWallpaperController) Dependency.get(IMiuiKeyguardWallpaperController.class)).registerWallpaperChangeCallback(this);
        ((ConfigurationController) Dependency.get(ConfigurationController.class)).addCallback(this);
        this.mKeyguardUpdateMonitor.registerCallback(this.mKeyguardUpdateMonitorCallback);
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ((IMiuiKeyguardWallpaperController) Dependency.get(IMiuiKeyguardWallpaperController.class)).unregisterWallpaperChangeCallback(this);
        ((ConfigurationController) Dependency.get(ConfigurationController.class)).removeCallback(this);
        this.mKeyguardUpdateMonitor.removeCallback(this.mKeyguardUpdateMonitorCallback);
    }

    @Override // com.android.keyguard.wallpaper.IMiuiKeyguardWallpaperController.IWallpaperChangeCallback
    public void onWallpaperChange(boolean z) {
        setDarkStyle(z);
    }

    private void initViews() {
        setSystemUiVisibility(4864);
        this.mWindowManager = (WindowManager) getContext().getSystemService("window");
        Display display = ((DisplayManager) getContext().getSystemService("display")).getDisplay(0);
        Point point = new Point();
        this.mScreenSizePoint = point;
        display.getRealSize(point);
        View view = new View(getContext());
        this.mBackgroundView = view;
        view.setAlpha(0.0f);
        addView(this.mBackgroundView, new FrameLayout.LayoutParams(-1, -1, 17));
        View view2 = new View(getContext());
        this.mCameraScrimView = view2;
        view2.setAlpha(0.0f);
        addView(this.mCameraScrimView, new FrameLayout.LayoutParams(-1, -1, 17));
        LinearLayout linearLayout = new LinearLayout(getContext());
        this.mPreViewContainer = linearLayout;
        linearLayout.setOutlineProvider(this.mPreViewOutlineProvider);
        this.mPreViewContainer.setClipToOutline(true);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(0, 0, 17);
        this.mPreViewContainerLayoutParams = layoutParams;
        addView(this.mPreViewContainer, layoutParams);
        ImageView imageView = new ImageView(getContext());
        this.mPreView = imageView;
        this.mPreViewContainer.addView(imageView, new FrameLayout.LayoutParams(-1, -1, 17));
        ImageView imageView2 = new ImageView(getContext());
        this.mIconView = imageView2;
        imageView2.setScaleType(ImageView.ScaleType.CENTER);
        addView(this.mIconView);
        WindowManager.LayoutParams layoutParams2 = new WindowManager.LayoutParams(-1, -1, Build.VERSION.SDK_INT > 29 ? 2017 : 2014, 218171160, -2);
        this.mLayoutParams = layoutParams2;
        layoutParams2.x = 0;
        layoutParams2.y = 0;
        layoutParams2.setTitle("keyguard_camera");
        WindowManager.LayoutParams layoutParams3 = new WindowManager.LayoutParams();
        this.mLpChanged = layoutParams3;
        layoutParams3.copyFrom(this.mLayoutParams);
        setVisibility(8);
        updateSizeForScreenSizeChange();
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        CallBack callBack;
        super.onConfigurationChanged(configuration);
        int updateFrom = this.mConfiguration.updateFrom(configuration);
        boolean z = true;
        boolean z2 = (updateFrom & 128) != 0;
        if ((updateFrom & 2048) == 0) {
            z = false;
        }
        if (z2 && MiuiKeyguardUtils.isPad() && (callBack = this.mCallBack) != null) {
            callBack.updatePreViewBackground();
        }
        if (z) {
            checkSize();
        }
    }

    private void checkSize() {
        Point point = new Point();
        this.mContext.getDisplay().getRealSize(point);
        if (!this.mScreenSizePoint.equals(point.x, point.y)) {
            this.mScreenSizePoint.set(point.x, point.y);
            updateSizeForScreenSizeChange();
            CallBack callBack = this.mCallBack;
            if (callBack != null) {
                callBack.updatePreViewBackground();
            }
            refreshView();
        }
    }

    private void updateSizeForScreenSizeChange() {
        Point point = this.mScreenSizePoint;
        this.mScreenHeight = Math.max(point.y, point.x);
        Point point2 = this.mScreenSizePoint;
        this.mScreenWidth = Math.min(point2.y, point2.x);
        this.mIconWidth = this.mContext.getResources().getDimensionPixelOffset(C0012R$dimen.keyguard_affordance_width);
        int dimensionPixelOffset = this.mContext.getResources().getDimensionPixelOffset(C0012R$dimen.keyguard_affordance_height);
        this.mIconHeight = dimensionPixelOffset;
        int i = this.mScreenWidth;
        this.mIconInitCenterX = (float) (i - (this.mIconWidth / 2));
        int i2 = this.mScreenHeight;
        this.mIconInitCenterY = (float) (i2 - (dimensionPixelOffset / 2));
        this.mIconActiveCenterX = ((float) i) * 0.55f;
        this.mIconActiveCenterY = ((float) i2) * 0.8f;
        this.mIconActiveWidth = ((float) i) * 0.74f;
        this.mIconView.setImageDrawable(this.mContext.getDrawable(C0013R$drawable.keyguard_bottom_camera_img));
        updateLayoutParams();
    }

    private void updateLayoutParams() {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) this.mIconView.getLayoutParams();
        if (layoutParams == null) {
            layoutParams = new FrameLayout.LayoutParams(this.mIconWidth, this.mIconHeight, 8388693);
        } else {
            layoutParams.width = this.mIconWidth;
            layoutParams.height = this.mIconHeight;
        }
        this.mIconView.setLayoutParams(layoutParams);
    }

    private void refreshView() {
        requestLayout();
        invalidate();
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        float f;
        float f2;
        float f3;
        super.onDraw(canvas);
        float f4 = this.mIconCircleCenterX;
        float f5 = this.mIconCircleWidth;
        float f6 = f4 - (f5 / 2.0f);
        float f7 = f4 + (f5 / 2.0f);
        if (this.mIsBackAnimRunning) {
            f3 = this.mIconCircleCenterY;
            float f8 = this.mIconCircleHeight;
            f = f3 - (0.2f * f8);
            f2 = f8 * 0.8f;
        } else {
            f3 = this.mIconCircleCenterY;
            float f9 = this.mIconCircleHeight;
            f = f3 - (f9 / 2.0f);
            f2 = f9 / 2.0f;
        }
        float f10 = f3 + f2;
        if (!this.mDarkStyle) {
            float f11 = f10 + 2.0f;
            float f12 = this.mPreViewRadius;
            canvas.drawRoundRect(f6 - 2.0f, f - 2.0f, f7 + 2.0f, f11, f12, f12, this.mIconCircleStrokePaint);
        }
        this.mIconCirclePaint.setAlpha((int) (this.mIconCircleAlpha * 255.0f));
        float f13 = this.mPreViewRadius;
        canvas.drawRoundRect(f6, f, f7, f10, f13, f13, this.mIconCirclePaint);
    }

    public void onTouchDown(float f, float f2, boolean z) {
        if (!this.mShowing || (z && !this.mIsPendingStartCamera)) {
            cancelAnim();
            this.mInitialTouchX = f;
            this.mInitialTouchY = f2;
            this.mMoveActivePer = z ? 0.6f : 0.3f;
            this.mTouchX = f;
            this.mTouchY = f2;
            this.mMoveYPer = 0.0f;
            this.mBackgroundView.setAlpha(0.0f);
            this.mCameraScrimView.setAlpha(0.0f);
            this.mIsActive = false;
            this.mLastIsActive = false;
            this.mActiveAnimPer = 0.0f;
            this.mIsCorrectOperation = true;
            this.mTouchDownInitial = true;
            this.mIsBackAnimRunning = false;
            if (z) {
                initBitmapResource();
                show();
            }
        }
    }

    public void onTouchMove(float f, float f2) {
        if (this.mTouchDownInitial) {
            this.mTouchX = f;
            this.mTouchY = f2;
            float f3 = this.mInitialTouchX;
            if (f > f3) {
                this.mTouchX = f3;
            }
            float f4 = this.mTouchY;
            float f5 = this.mInitialTouchY;
            if (f4 > f5) {
                this.mTouchY = f5;
            }
            if (this.mIsCorrectOperation) {
                this.mMoveDistance = (float) Math.sqrt(Math.pow((double) (this.mInitialTouchX - this.mTouchX), 2.0d) + Math.pow((double) (this.mInitialTouchY - this.mTouchY), 2.0d));
                handleMoveDistanceChanged();
            }
        }
    }

    public void onTouchUp(float f, float f2) {
        if (this.mTouchDownInitial) {
            this.mTouchDownInitial = false;
            if (this.mIsActive) {
                startFullScreenAnim();
            } else if (this.mIsCorrectOperation) {
                startCancelAnim();
            }
        }
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onDensityOrFontScaleChanged() {
        checkSize();
    }

    public void reset() {
        if (this.mShowing) {
            this.mIsPendingStartCamera = false;
            this.mTouchDownInitial = false;
            cancelAnim();
            dismiss();
            setAlpha(1.0f);
            this.mMoveDistance = 0.0f;
            this.mActiveAnimPer = 0.0f;
            this.mInitialTouchY = 0.0f;
            this.mInitialTouchX = 0.0f;
            this.mTouchY = 0.0f;
            this.mTouchX = 0.0f;
            handleMoveDistanceChanged();
            if (this.mTaskStackListener != null) {
                ActivityManagerWrapper.getInstance().unregisterTaskStackListener(this.mTaskStackListener);
            }
        }
    }

    public void setDarkStyle(boolean z) {
        int i;
        if (this.mDarkStyle != z) {
            this.mDarkStyle = z;
            ImageView imageView = this.mIconView;
            Context context = this.mContext;
            if (z) {
                i = C0013R$drawable.keyguard_bottom_camera_img_dark;
            } else {
                i = C0013R$drawable.keyguard_bottom_camera_img;
            }
            imageView.setImageDrawable(context.getDrawable(i));
        }
    }

    private void handleMoveDistanceChanged() {
        if (!this.mIsCameraShowing && !this.mIsPendingStartCamera) {
            this.mMovePer = perFromVal(this.mMoveDistance, 0.0f, (float) (this.mScreenWidth / 3));
            notifyAnimUpdate();
            if (DeviceConfig.isLowGpuDevice()) {
                updateScrimAlpha();
            } else {
                updateBlurRatio();
            }
            updateActiveAnim();
            if (this.mMoveActivePer == 0.6f) {
                updateViews();
            }
        }
    }

    private void updateActiveAnim() {
        this.mLastIsActive = this.mIsActive;
        float f = this.mMovePer;
        float f2 = this.mMoveActivePer;
        if (f > f2) {
            if (f2 == 0.3f) {
                handleMisOperation();
            } else {
                this.mIsActive = true;
            }
        } else if (f2 == 0.6f) {
            this.mIsActive = false;
        }
        if (!this.mIsActive && this.mLastIsActive) {
            startActiveAnim(this.mActiveAnimPer, 0.0f);
        } else if (this.mIsActive && !this.mLastIsActive) {
            startActiveAnim(this.mActiveAnimPer, 1.0f);
            ((HapticFeedBackImpl) Dependency.get(HapticFeedBackImpl.class)).hapticFeedback("mesh_heavy", false);
        }
    }

    private void handleMisOperation() {
        if (this.mIsCorrectOperation) {
            this.mIsCorrectOperation = false;
            this.mMoveDistance = this.mMoveActivePer * ((float) (this.mScreenWidth / 3));
            startCancelAnim();
            ((HapticFeedBackImpl) Dependency.get(HapticFeedBackImpl.class)).extLongHapticFeedback(165, true, 60);
        }
    }

    private void updateViews() {
        float f = (float) (((double) this.mMovePer) + (((double) this.mActiveAnimPer) * 0.4d));
        this.mMovePer = f;
        if (f < 0.0f) {
            f = 0.0f;
        }
        this.mMovePer = f;
        if (f > 1.0f) {
            f = ((f - 1.0f) / 20.0f) + 1.0f;
        }
        float f2 = this.mMovePer;
        if (f2 > 1.0f) {
            f2 = ((f2 - 1.0f) / 15.0f) + 1.0f;
        }
        float min = Math.min(0.0f, this.mTouchY - this.mInitialTouchY);
        int i = this.mScreenHeight;
        float f3 = min / ((float) i);
        this.mMoveYPer = f3;
        this.mIconActiveCenterY = ((float) i) * Math.max(0.8f, (f3 * 0.1f) + 0.85f);
        this.mVirX = valFromPer(f2, this.mIconInitCenterX, this.mIconActiveCenterX);
        this.mVirY = valFromPer((float) Math.pow((double) f2, 3.0d), this.mIconInitCenterY, this.mIconActiveCenterY) + (this.mMoveYPer * 100.0f);
        float valFromPer = valFromPer(f, 0.0f, this.mIconActiveWidth);
        this.mVirWidth = valFromPer;
        float min2 = valFromPer * (((this.mActiveAnimPer * Math.min(((float) this.mScreenHeight) / ((float) this.mScreenWidth), 2.0f)) / 2.0f) + 1.0f);
        this.mVirHeight = min2;
        this.mIconCenterX = this.mVirX;
        this.mIconCenterY = (this.mVirY + (min2 * 0.15f)) - (this.mVirWidth / 2.0f);
        this.mIconAlpha = valFromPer(this.mMovePer, 1.0f, 0.0f);
        this.mIconScale = valFromPer(this.mMovePer / this.mMoveActivePer, 1.0f, 1.5f);
        this.mIconCircleCenterX = this.mVirX;
        float f4 = this.mVirY;
        float f5 = this.mVirHeight;
        this.mIconCircleCenterY = f4 - (f5 * 0.35f);
        this.mIconCircleWidth = this.mVirWidth;
        this.mIconCircleHeight = f5;
        this.mIconCircleAlpha = valFromPer(this.mMovePer, 0.0f, 0.8f);
        this.mPreViewCenterX = this.mVirX;
        float f6 = this.mVirY;
        float f7 = this.mVirHeight;
        this.mPreViewCenterY = f6 - (0.35f * f7);
        float f8 = this.mVirWidth;
        this.mPreViewWidth = f8;
        this.mPreViewHeight = f7;
        this.mPreViewRadius = valFromPer(this.mActiveAnimPer, f8 / 2.0f, this.mPreViewInitRadius);
        this.mPreViewAlpha = this.mActiveAnimPer;
        invalidate();
        updateIconView();
        updatePreView();
    }

    private void updateIconView() {
        this.mIconView.setX(this.mIconCenterX - ((float) (this.mIconWidth / 2)));
        this.mIconView.setY(this.mIconCenterY - ((float) (this.mIconHeight / 2)));
        this.mIconView.setAlpha(this.mIconAlpha);
        this.mIconView.setScaleX(this.mIconScale);
        this.mIconView.setScaleY(this.mIconScale);
    }

    private void updatePreView() {
        if (!this.mIsBackAnimRunning) {
            this.mPreViewContainer.setX(this.mPreViewCenterX - (this.mPreViewWidth / 2.0f));
            this.mPreViewContainer.setY(this.mPreViewCenterY - (this.mPreViewHeight / 2.0f));
            FrameLayout.LayoutParams layoutParams = this.mPreViewContainerLayoutParams;
            layoutParams.width = (int) this.mPreViewWidth;
            layoutParams.height = (int) this.mPreViewHeight;
            this.mPreViewContainer.setLayoutParams(layoutParams);
            this.mPreViewContainer.setAlpha(this.mPreViewAlpha);
        }
    }

    private void updateBlurRatio() {
        float f = this.mMoveDistance;
        applyBlurRatio(f < 270.0f ? f / 270.0f : 1.0f);
    }

    private void updateScrimAlpha() {
        float f = this.mMoveDistance;
        this.mCameraScrimView.setAlpha(f < 270.0f ? f / 270.0f : 1.0f);
    }

    private void notifyAnimUpdate() {
        CallBack callBack = this.mCallBack;
        if (callBack != null) {
            callBack.onAnimUpdate(this.mMoveDistance);
        }
    }

    public void cancelAnim() {
        AnimatorSet animatorSet = this.mAnimatorSet;
        if (animatorSet != null) {
            animatorSet.cancel();
            this.mAnimatorSet = null;
        }
        ValueAnimator valueAnimator = this.mBackgroundAnimator;
        if (valueAnimator != null) {
            valueAnimator.cancel();
            this.mBackgroundAnimator = null;
        }
    }

    private void startActiveAnim(float f, float f2) {
        cancelAnim();
        this.mAnimatorSet = new AnimatorSet();
        ValueAnimator ofFloat = ValueAnimator.ofFloat(f, f2);
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.android.keyguard.$$Lambda$MiuiKeyguardCameraView$6G3eXA4who1pgF5AH_zyYs03b4 */

            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                MiuiKeyguardCameraView.this.lambda$startActiveAnim$0$MiuiKeyguardCameraView(valueAnimator);
            }
        });
        this.mAnimatorSet.playTogether(ofFloat);
        if (f2 == 0.0f) {
            this.mAnimatorSet.setInterpolator(new PhysicBasedInterpolator(this, 0.9f, 0.8f));
            this.mAnimatorSet.setDuration(250L);
        } else {
            this.mAnimatorSet.setInterpolator(new PhysicBasedInterpolator(this, 0.8f, 0.67f));
            this.mAnimatorSet.setDuration(450L);
        }
        this.mAnimatorSet.setDuration(450L);
        this.mAnimatorSet.start();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$startActiveAnim$0 */
    public /* synthetic */ void lambda$startActiveAnim$0$MiuiKeyguardCameraView(ValueAnimator valueAnimator) {
        float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
        this.mActiveAnimPer = floatValue;
        if (floatValue < 0.0f) {
            floatValue = 0.0f;
        }
        this.mActiveAnimPer = floatValue;
        handleMoveDistanceChanged();
    }

    private void startFullScreenAnim() {
        cancelAnim();
        this.mIsPendingStartCamera = true;
        float f = this.mPreViewWidth;
        int i = this.mScreenWidth;
        float f2 = this.mPreViewHeight;
        int i2 = this.mScreenHeight;
        AnimatorSet fullScreenAnim = getFullScreenAnim(f, (float) i, f2, (float) i2, this.mPreViewCenterX, (float) (i / 2), this.mPreViewCenterY, (float) (i2 / 2), this.mPreViewRadius, this.mPreViewInitRadius, this.mPreViewAlpha, 1.0f, this.mIconAlpha, 0.0f, this.mIconScale, 1.5f);
        this.mAnimatorSet = fullScreenAnim;
        fullScreenAnim.setInterpolator(new PhysicBasedInterpolator(this, 0.9f, 0.85f));
        this.mAnimatorSet.setDuration(350L);
        this.mAnimatorSet.addListener(new AnimatorListenerAdapter() {
            /* class com.android.keyguard.MiuiKeyguardCameraView.AnonymousClass4 */

            public void onAnimationCancel(Animator animator) {
            }

            public void onAnimationEnd(Animator animator) {
                super.onAnimationEnd(animator);
                if (MiuiKeyguardCameraView.this.mCallBack != null) {
                    MiuiKeyguardCameraView.this.mCallBack.onCompletedAnimationEnd();
                }
                if (MiuiKeyguardCameraView.this.mBackgroundAnimator != null) {
                    MiuiKeyguardCameraView.this.mBackgroundAnimator.cancel();
                    MiuiKeyguardCameraView.this.mBackgroundAnimator = null;
                }
                if (MiuiKeyguardCameraView.this.mBackgroundAnimatorSet != null && MiuiKeyguardCameraView.this.mIsCameraShowing) {
                    MiuiKeyguardCameraView.this.setAlpha(0.0f);
                    MiuiKeyguardCameraView.this.applyBlurRatio(0.0f);
                    MiuiKeyguardCameraView.this.updateKeepScreenOnFlag(false);
                    MiuiKeyguardCameraView.this.mBackgroundAnimatorSet = null;
                }
            }
        });
        this.mAnimatorSet.start();
        this.mBackgroundAnimatorSet = this.mAnimatorSet;
        ActivityManagerWrapper.getInstance().registerTaskStackListener(this.mTaskStackListener);
        AnalyticsHelper.getInstance(this.mContext).recordKeyguardAction("action_enter_camera_view");
        AnalyticsHelper.getInstance(this.mContext).trackPageStart("action_enter_camera_view");
        this.mContext.startActivityAsUser(PackageUtils.getCameraIntent(), UserHandle.CURRENT);
        ValueAnimator ofFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
        this.mBackgroundAnimator = ofFloat;
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.android.keyguard.$$Lambda$MiuiKeyguardCameraView$rB1kSAJJFjE75nb4TddKPG5sfdY */

            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                MiuiKeyguardCameraView.this.lambda$startFullScreenAnim$1$MiuiKeyguardCameraView(valueAnimator);
            }
        });
        this.mBackgroundAnimator.setInterpolator(new PhysicBasedInterpolator(this, 0.99f, 0.67f));
        this.mBackgroundAnimator.setDuration(450L);
        this.mBackgroundAnimator.start();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$startFullScreenAnim$1 */
    public /* synthetic */ void lambda$startFullScreenAnim$1$MiuiKeyguardCameraView(ValueAnimator valueAnimator) {
        this.mBackgroundView.setAlpha(((Float) valueAnimator.getAnimatedValue()).floatValue());
    }

    private AnimatorSet getFullScreenAnim(float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, float f9, float f10, float f11, float f12, float f13, float f14, float f15, float f16) {
        AnimatorSet animatorSet = new AnimatorSet();
        ValueAnimator ofFloat = ValueAnimator.ofFloat(f, f2);
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.android.keyguard.$$Lambda$MiuiKeyguardCameraView$51hzYHue6y3vtxtOIniRQTaXdiw */

            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                MiuiKeyguardCameraView.this.lambda$getFullScreenAnim$2$MiuiKeyguardCameraView(valueAnimator);
            }
        });
        ValueAnimator ofFloat2 = ValueAnimator.ofFloat(f3, f4);
        ofFloat2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.android.keyguard.$$Lambda$MiuiKeyguardCameraView$8LjmED2WhrC2qoFK0g_HUIj1ik */

            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                MiuiKeyguardCameraView.this.lambda$getFullScreenAnim$3$MiuiKeyguardCameraView(valueAnimator);
            }
        });
        ValueAnimator ofFloat3 = ValueAnimator.ofFloat(f5, f6);
        ofFloat3.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.android.keyguard.$$Lambda$MiuiKeyguardCameraView$5Td_lbfjWN8BzH0Z_pjLq3UO74s */

            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                MiuiKeyguardCameraView.this.lambda$getFullScreenAnim$4$MiuiKeyguardCameraView(valueAnimator);
            }
        });
        ValueAnimator ofFloat4 = ValueAnimator.ofFloat(f7, f8);
        ofFloat4.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.android.keyguard.$$Lambda$MiuiKeyguardCameraView$LU6pceS0n4SDv5uN5ul0p3rUssI */

            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                MiuiKeyguardCameraView.this.lambda$getFullScreenAnim$5$MiuiKeyguardCameraView(valueAnimator);
            }
        });
        ValueAnimator ofFloat5 = ValueAnimator.ofFloat(f11, f12);
        ofFloat5.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.android.keyguard.$$Lambda$MiuiKeyguardCameraView$D2WnIJcdZxsmYZKpokRGTQwLiwc */

            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                MiuiKeyguardCameraView.this.lambda$getFullScreenAnim$6$MiuiKeyguardCameraView(valueAnimator);
            }
        });
        ValueAnimator ofFloat6 = ValueAnimator.ofFloat(f9, f10);
        ofFloat6.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.android.keyguard.$$Lambda$MiuiKeyguardCameraView$AgpKqoMvOXstEWt3gkkdVxBmGK8 */

            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                MiuiKeyguardCameraView.this.lambda$getFullScreenAnim$7$MiuiKeyguardCameraView(valueAnimator);
            }
        });
        ValueAnimator ofFloat7 = ValueAnimator.ofFloat(f13, f14);
        ofFloat7.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.android.keyguard.$$Lambda$MiuiKeyguardCameraView$1vB_HhGuDkNM7Ni6teE9BVVVmw */

            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                MiuiKeyguardCameraView.this.lambda$getFullScreenAnim$8$MiuiKeyguardCameraView(valueAnimator);
            }
        });
        ValueAnimator ofFloat8 = ValueAnimator.ofFloat(f15, f16);
        ofFloat8.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.android.keyguard.$$Lambda$MiuiKeyguardCameraView$Zj4DoohRr5NAGksZzAseGYBAytY */

            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                MiuiKeyguardCameraView.this.lambda$getFullScreenAnim$9$MiuiKeyguardCameraView(valueAnimator);
            }
        });
        animatorSet.playTogether(ofFloat, ofFloat2, ofFloat3, ofFloat4, ofFloat5, ofFloat6, ofFloat7, ofFloat8);
        return animatorSet;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$getFullScreenAnim$2 */
    public /* synthetic */ void lambda$getFullScreenAnim$2$MiuiKeyguardCameraView(ValueAnimator valueAnimator) {
        float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
        this.mPreViewWidth = floatValue;
        this.mIconCircleWidth = floatValue;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$getFullScreenAnim$3 */
    public /* synthetic */ void lambda$getFullScreenAnim$3$MiuiKeyguardCameraView(ValueAnimator valueAnimator) {
        float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
        this.mPreViewHeight = floatValue;
        this.mIconCircleHeight = floatValue;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$getFullScreenAnim$4 */
    public /* synthetic */ void lambda$getFullScreenAnim$4$MiuiKeyguardCameraView(ValueAnimator valueAnimator) {
        float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
        this.mPreViewCenterX = floatValue;
        this.mIconCircleCenterX = floatValue;
        this.mIconCenterX = floatValue;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$getFullScreenAnim$5 */
    public /* synthetic */ void lambda$getFullScreenAnim$5$MiuiKeyguardCameraView(ValueAnimator valueAnimator) {
        float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
        this.mPreViewCenterY = floatValue;
        this.mIconCircleCenterY = floatValue;
        this.mIconCenterY = (floatValue + (this.mIconCircleHeight / 2.0f)) - (this.mIconCircleWidth / 2.0f);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$getFullScreenAnim$6 */
    public /* synthetic */ void lambda$getFullScreenAnim$6$MiuiKeyguardCameraView(ValueAnimator valueAnimator) {
        this.mPreViewAlpha = ((Float) valueAnimator.getAnimatedValue()).floatValue();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$getFullScreenAnim$7 */
    public /* synthetic */ void lambda$getFullScreenAnim$7$MiuiKeyguardCameraView(ValueAnimator valueAnimator) {
        this.mPreViewRadius = ((Float) valueAnimator.getAnimatedValue()).floatValue();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$getFullScreenAnim$8 */
    public /* synthetic */ void lambda$getFullScreenAnim$8$MiuiKeyguardCameraView(ValueAnimator valueAnimator) {
        this.mIconAlpha = ((Float) valueAnimator.getAnimatedValue()).floatValue();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$getFullScreenAnim$9 */
    public /* synthetic */ void lambda$getFullScreenAnim$9$MiuiKeyguardCameraView(ValueAnimator valueAnimator) {
        this.mIconScale = ((Float) valueAnimator.getAnimatedValue()).floatValue();
        invalidate();
        updatePreView();
        updateIconView();
    }

    public void startCancelAnim() {
        cancelAnim();
        this.mAnimatorSet = new AnimatorSet();
        ValueAnimator ofFloat = ValueAnimator.ofFloat(this.mActiveAnimPer, 0.0f);
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.android.keyguard.$$Lambda$MiuiKeyguardCameraView$2F3UNNHsCSOua2pF5p2OLHwgaoU */

            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                MiuiKeyguardCameraView.this.lambda$startCancelAnim$10$MiuiKeyguardCameraView(valueAnimator);
            }
        });
        ofFloat.setInterpolator(new PhysicBasedInterpolator(this, 0.8f, 0.67f));
        ofFloat.setDuration(450L);
        ValueAnimator ofFloat2 = ValueAnimator.ofFloat(this.mMoveYPer, 0.0f);
        ofFloat2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.android.keyguard.$$Lambda$MiuiKeyguardCameraView$j5H8zwtWSLxOxNPfo6eLss4vJJA */

            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                MiuiKeyguardCameraView.this.lambda$startCancelAnim$11$MiuiKeyguardCameraView(valueAnimator);
            }
        });
        ofFloat2.setInterpolator(new PhysicBasedInterpolator(this, 0.8f, 0.67f));
        ofFloat2.setDuration(450L);
        ValueAnimator ofFloat3 = ValueAnimator.ofFloat(this.mMoveDistance, 0.0f);
        ofFloat3.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.android.keyguard.$$Lambda$MiuiKeyguardCameraView$9ukSib3kqT_Pk9H9EhzhIEsdc7c */

            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                MiuiKeyguardCameraView.this.lambda$startCancelAnim$12$MiuiKeyguardCameraView(valueAnimator);
            }
        });
        ofFloat3.setInterpolator(new PhysicBasedInterpolator(this, 0.8f, 0.71f));
        ofFloat3.setDuration(700L);
        this.mAnimatorSet.addListener(new AnimatorListenerAdapter() {
            /* class com.android.keyguard.MiuiKeyguardCameraView.AnonymousClass5 */

            public void onAnimationEnd(Animator animator) {
                MiuiKeyguardCameraView.this.dismiss();
                if (MiuiKeyguardCameraView.this.mCallBack != null) {
                    MiuiKeyguardCameraView.this.mCallBack.onCancelAnimationEnd(MiuiKeyguardCameraView.this.mIsCorrectOperation);
                }
            }
        });
        this.mAnimatorSet.playTogether(ofFloat, ofFloat2, ofFloat3);
        this.mAnimatorSet.start();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$startCancelAnim$10 */
    public /* synthetic */ void lambda$startCancelAnim$10$MiuiKeyguardCameraView(ValueAnimator valueAnimator) {
        float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
        this.mActiveAnimPer = floatValue;
        if (floatValue < 0.0f) {
            floatValue = 0.0f;
        }
        this.mActiveAnimPer = floatValue;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$startCancelAnim$11 */
    public /* synthetic */ void lambda$startCancelAnim$11$MiuiKeyguardCameraView(ValueAnimator valueAnimator) {
        this.mMoveYPer = ((Float) valueAnimator.getAnimatedValue()).floatValue();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$startCancelAnim$12 */
    public /* synthetic */ void lambda$startCancelAnim$12$MiuiKeyguardCameraView(ValueAnimator valueAnimator) {
        this.mMoveDistance = ((Float) valueAnimator.getAnimatedValue()).floatValue();
        handleMoveDistanceChanged();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startBackAnim() {
        this.mIsBackAnimRunning = true;
        cancelAnim();
        int i = this.mScreenWidth;
        AnimatorSet backIconAnim = getBackIconAnim((float) i, 0.0f, (float) (i / 2), this.mIconInitCenterX, (float) (this.mScreenHeight / 2), this.mIconInitCenterY, 1.5f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 0.0f);
        this.mAnimatorSet = backIconAnim;
        backIconAnim.setInterpolator(new PhysicBasedInterpolator(this, 0.8f, 0.71f));
        this.mAnimatorSet.setDuration(1000L);
        this.mAnimatorSet.addListener(new AnimatorListenerAdapter() {
            /* class com.android.keyguard.MiuiKeyguardCameraView.AnonymousClass6 */

            public void onAnimationStart(Animator animator) {
                MiuiKeyguardCameraView.this.startUpdateAspectRatioAnimation();
            }

            public void onAnimationEnd(Animator animator) {
                MiuiKeyguardCameraView.this.mIsBackAnimRunning = false;
                MiuiKeyguardCameraView.this.reset();
                if (MiuiKeyguardCameraView.this.mCallBack != null) {
                    MiuiKeyguardCameraView.this.mCallBack.onBackAnimationEnd();
                }
                WindowManagerGlobal.getInstance().trimMemory(20);
            }
        });
        this.mAnimatorSet.start();
    }

    private AnimatorSet getBackIconAnim(float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, float f9, float f10, float f11, float f12, float f13, float f14) {
        AnimatorSet animatorSet = new AnimatorSet();
        ValueAnimator ofFloat = ValueAnimator.ofFloat(f, f2);
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.android.keyguard.$$Lambda$MiuiKeyguardCameraView$SBI3MYC2RvehJOnTEGhHvnJWGI */

            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                MiuiKeyguardCameraView.this.lambda$getBackIconAnim$13$MiuiKeyguardCameraView(valueAnimator);
            }
        });
        ValueAnimator ofFloat2 = ValueAnimator.ofFloat(f3, f4);
        ofFloat2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.android.keyguard.$$Lambda$MiuiKeyguardCameraView$oHoNk9rBntb4VxVc6fouGwEKXg */

            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                MiuiKeyguardCameraView.this.lambda$getBackIconAnim$14$MiuiKeyguardCameraView(valueAnimator);
            }
        });
        ValueAnimator ofFloat3 = ValueAnimator.ofFloat(f5, f6);
        ofFloat3.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.android.keyguard.$$Lambda$MiuiKeyguardCameraView$ecJA6brLsDERLkgSi3tEFhUdeaA */

            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                MiuiKeyguardCameraView.this.lambda$getBackIconAnim$15$MiuiKeyguardCameraView(valueAnimator);
            }
        });
        ValueAnimator ofFloat4 = ValueAnimator.ofFloat(f11, f12);
        ofFloat4.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.android.keyguard.$$Lambda$MiuiKeyguardCameraView$r2w63oweQMniVOog3hkT5iGxJHs */

            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                MiuiKeyguardCameraView.this.lambda$getBackIconAnim$16$MiuiKeyguardCameraView(valueAnimator);
            }
        });
        ValueAnimator ofFloat5 = ValueAnimator.ofFloat(f9, f10);
        ofFloat5.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.android.keyguard.$$Lambda$MiuiKeyguardCameraView$lmXwxD9kAYlYpvuHtESLO2rcnnE */

            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                MiuiKeyguardCameraView.this.lambda$getBackIconAnim$17$MiuiKeyguardCameraView(valueAnimator);
            }
        });
        ValueAnimator ofFloat6 = ValueAnimator.ofFloat(f7, f8);
        ofFloat6.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.android.keyguard.$$Lambda$MiuiKeyguardCameraView$sqXxYUg_4SE5U1ML8lh4BZadbU0 */

            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                MiuiKeyguardCameraView.this.lambda$getBackIconAnim$18$MiuiKeyguardCameraView(valueAnimator);
            }
        });
        ValueAnimator ofFloat7 = ValueAnimator.ofFloat(f13, f14);
        ofFloat7.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.android.keyguard.$$Lambda$MiuiKeyguardCameraView$SxqEydld7ogE7dam8P8iZtpUbU */

            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                MiuiKeyguardCameraView.this.lambda$getBackIconAnim$19$MiuiKeyguardCameraView(valueAnimator);
            }
        });
        animatorSet.playTogether(ofFloat, ofFloat2, ofFloat3, ofFloat4, ofFloat5, ofFloat6, ofFloat7);
        return animatorSet;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$getBackIconAnim$13 */
    public /* synthetic */ void lambda$getBackIconAnim$13$MiuiKeyguardCameraView(ValueAnimator valueAnimator) {
        float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
        this.mIconCircleWidth = floatValue;
        this.mIconCircleHeight = floatValue * this.mBackAnimAspectRatio;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$getBackIconAnim$14 */
    public /* synthetic */ void lambda$getBackIconAnim$14$MiuiKeyguardCameraView(ValueAnimator valueAnimator) {
        float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
        this.mIconCircleCenterX = floatValue;
        this.mIconCenterX = floatValue;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$getBackIconAnim$15 */
    public /* synthetic */ void lambda$getBackIconAnim$15$MiuiKeyguardCameraView(ValueAnimator valueAnimator) {
        float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
        this.mIconCircleCenterY = floatValue;
        this.mIconCenterY = (floatValue + (this.mIconCircleHeight * 0.8f)) - (this.mIconCircleWidth / 2.0f);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$getBackIconAnim$16 */
    public /* synthetic */ void lambda$getBackIconAnim$16$MiuiKeyguardCameraView(ValueAnimator valueAnimator) {
        float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
        this.mIconCircleAlpha = floatValue;
        if (floatValue < 0.0f) {
            this.mIconCircleAlpha = 0.0f;
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$getBackIconAnim$17 */
    public /* synthetic */ void lambda$getBackIconAnim$17$MiuiKeyguardCameraView(ValueAnimator valueAnimator) {
        this.mIconAlpha = ((Float) valueAnimator.getAnimatedValue()).floatValue();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$getBackIconAnim$18 */
    public /* synthetic */ void lambda$getBackIconAnim$18$MiuiKeyguardCameraView(ValueAnimator valueAnimator) {
        this.mIconScale = ((Float) valueAnimator.getAnimatedValue()).floatValue();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$getBackIconAnim$19 */
    public /* synthetic */ void lambda$getBackIconAnim$19$MiuiKeyguardCameraView(ValueAnimator valueAnimator) {
        this.mBackgroundView.setAlpha(((Float) valueAnimator.getAnimatedValue()).floatValue());
        int i = this.mScreenHeight;
        int i2 = this.mScreenWidth;
        float f = ((float) i) / ((float) i2);
        float f2 = 1.0f;
        float f3 = ((((float) i) / ((float) i2)) - this.mBackAnimAspectRatio) / (f - 1.0f);
        float f4 = this.mPreViewInitRadius;
        this.mPreViewRadius = valFromPer(f3, f4, (this.mIconCircleWidth / 2.0f) + f4);
        if (!DeviceConfig.isLowGpuDevice()) {
            float f5 = this.mIconInitCenterX;
            float f6 = this.mIconCenterX;
            if (f5 - f6 < 270.0f) {
                f2 = (f5 - f6) / 270.0f;
            }
            applyBlurRatio(f2);
        }
        invalidate();
        updateIconView();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startUpdateAspectRatioAnimation() {
        ValueAnimator ofFloat = ValueAnimator.ofFloat(((float) this.mScreenHeight) / ((float) this.mScreenWidth), 1.0f);
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.android.keyguard.$$Lambda$MiuiKeyguardCameraView$TL74oKbU2dukfkxCGWzQs1eEvA0 */

            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                MiuiKeyguardCameraView.this.lambda$startUpdateAspectRatioAnimation$20$MiuiKeyguardCameraView(valueAnimator);
            }
        });
        ofFloat.setInterpolator(new PhysicBasedInterpolator(this, 0.99f, 0.67f));
        ofFloat.setDuration(300L);
        ofFloat.start();
        if (!DeviceConfig.isLowGpuDevice()) {
            applyBlurRatio(1.0f);
        }
        this.mBackgroundView.setAlpha(1.0f);
        this.mPreViewContainer.setAlpha(0.0f);
        this.mCameraScrimView.setAlpha(0.0f);
        setAlpha(1.0f);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$startUpdateAspectRatioAnimation$20 */
    public /* synthetic */ void lambda$startUpdateAspectRatioAnimation$20$MiuiKeyguardCameraView(ValueAnimator valueAnimator) {
        this.mBackAnimAspectRatio = ((Float) valueAnimator.getAnimatedValue()).floatValue();
    }

    private void applyParams() {
        if (isAttachedToWindow() && this.mWindowManager != null && this.mLayoutParams.copyFrom(this.mLpChanged) != 0) {
            this.mWindowManager.updateViewLayout(this, this.mLayoutParams);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void applyBlurRatio(float f) {
        if (f > 1.0f) {
            f = 1.0f;
        } else if (f < 0.0f) {
            f = 0.0f;
        }
        BlurUtil.setBlur(getViewRootImpl(), f, 0);
    }

    public void show() {
        Log.d("KeyguardCameraView", "show " + this.mShowing);
        if (!this.mShowing) {
            this.mShowing = true;
            setVisibility(0);
            CallBack callBack = this.mCallBack;
            if (callBack != null) {
                callBack.onVisibilityChanged(true);
            }
            updateKeepScreenOnFlag(true);
        }
    }

    public void dismiss() {
        Log.d("KeyguardCameraView", "dismiss " + this.mShowing);
        if (this.mShowing) {
            this.mShowing = false;
            CallBack callBack = this.mCallBack;
            if (callBack != null) {
                callBack.onVisibilityChanged(false);
            }
            setVisibility(8);
            updateKeepScreenOnFlag(false);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateKeepScreenOnFlag(boolean z) {
        if (z) {
            this.mLpChanged.flags |= 128;
        } else {
            this.mLpChanged.flags &= -129;
        }
        applyParams();
    }

    public void addViewToWindow() {
        WindowManager windowManager;
        WindowManager.LayoutParams layoutParams;
        Log.d("KeyguardCameraView", "addViewToWindow " + isAttachedToWindow() + " " + getParent());
        if (!isAttachedToWindow() && getParent() == null && (windowManager = this.mWindowManager) != null && (layoutParams = this.mLayoutParams) != null) {
            windowManager.addView(this, layoutParams);
        }
    }

    public void removeViewFromWindow() {
        WindowManager windowManager;
        Log.d("KeyguardCameraView", "removeViewFromWindow " + isAttachedToWindow());
        if (isAttachedToWindow() && (windowManager = this.mWindowManager) != null) {
            windowManager.removeView(this);
        }
        releaseBitmapResource();
    }

    private void initBitmapResource() {
        if (PackageUtils.IS_VELA_CAMERA) {
            this.mPreView.setImageResource(C0013R$drawable.meitu_camera_preview);
        } else {
            this.mPreView.setScaleType(ImageView.ScaleType.FIT_END);
            this.mPreView.setImageResource(C0013R$drawable.camera_preview);
        }
        this.mBackgroundView.setBackgroundColor(-16777216);
        this.mPreViewContainer.setBackgroundColor(-16777216);
        this.mCameraScrimView.setBackgroundColor(-1728053248);
    }

    public void releaseBitmapResource() {
        Drawable drawable;
        Bitmap bitmap;
        ImageView imageView = this.mPreView;
        if (!(imageView == null || (drawable = imageView.getDrawable()) == null || !(drawable instanceof BitmapDrawable) || (bitmap = ((BitmapDrawable) drawable).getBitmap()) == null || bitmap.isRecycled())) {
            bitmap.recycle();
        }
        View view = this.mBackgroundView;
        if (view != null) {
            view.setBackgroundColor(0);
        }
        LinearLayout linearLayout = this.mPreViewContainer;
        if (linearLayout != null) {
            linearLayout.setBackgroundColor(0);
        }
        View view2 = this.mCameraScrimView;
        if (view2 != null) {
            view2.setBackgroundColor(0);
        }
    }

    public void setPreviewImageDrawable(Drawable drawable) {
        this.mPreView.setImageDrawable(drawable);
    }

    public class PhysicBasedInterpolator implements Interpolator {
        private float c;
        private float c1 = -1.0f;
        private float c2;
        private float k;
        private float m = 1.0f;
        private float mInitial = -1.0f;
        private float r;
        private float w;

        public PhysicBasedInterpolator(MiuiKeyguardCameraView miuiKeyguardCameraView, float f, float f2) {
            double d = (double) f2;
            double pow = Math.pow(6.283185307179586d / d, 2.0d);
            float f3 = this.m;
            float f4 = (float) (pow * ((double) f3));
            this.k = f4;
            float f5 = (float) (((((double) f) * 12.566370614359172d) * ((double) f3)) / d);
            this.c = f5;
            float f6 = this.m;
            float sqrt = ((float) Math.sqrt((double) (((f3 * 4.0f) * f4) - (f5 * f5)))) / (f6 * 2.0f);
            this.w = sqrt;
            float f7 = -((this.c / 2.0f) * f6);
            this.r = f7;
            this.c2 = (0.0f - (f7 * this.mInitial)) / sqrt;
        }

        public float getInterpolation(float f) {
            return (float) ((Math.pow(2.718281828459045d, (double) (this.r * f)) * ((((double) this.c1) * Math.cos((double) (this.w * f))) + (((double) this.c2) * Math.sin((double) (this.w * f))))) + 1.0d);
        }
    }
}
