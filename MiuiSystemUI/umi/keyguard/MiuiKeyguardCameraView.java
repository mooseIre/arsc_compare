package com.android.keyguard;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.app.ActivityManager;
import android.app.ActivityTaskManager;
import android.content.Context;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.graphics.Canvas;
import android.graphics.ImageDecoder;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.hardware.display.DisplayManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.Vibrator;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.WindowManager;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.android.keyguard.MiuiKeyguardCameraView;
import com.android.keyguard.analytics.AnalyticsHelper;
import com.android.keyguard.utils.ContentProviderUtils;
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
import java.io.IOException;
import java.util.List;

public class MiuiKeyguardCameraView extends FrameLayout implements IMiuiKeyguardWallpaperController.IWallpaperChangeCallback, ConfigurationController.ConfigurationListener {
    private float mActiveAnimPer;
    private AnimatorSet mAnimatorSet;
    /* access modifiers changed from: private */
    public float mBackAnimAspectRatio;
    /* access modifiers changed from: private */
    public ValueAnimator mBackgroundAnimator;
    /* access modifiers changed from: private */
    public View mBackgroundView;
    /* access modifiers changed from: private */
    public CallBack mCallBack;
    /* access modifiers changed from: private */
    public View mCameraScrimView;
    private Configuration mConfiguration = new Configuration();
    /* access modifiers changed from: private */
    public Context mContext = getContext();
    private boolean mDarkStyle;
    ContentObserver mFullScreenGestureObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean z) {
            super.onChange(z);
            MiuiKeyguardCameraView.this.updatePreViewBackground();
        }
    };
    /* access modifiers changed from: private */
    public Handler mHandler = new Handler(Looper.myLooper());
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
    /* access modifiers changed from: private */
    public boolean mIsBackAnimRunning;
    /* access modifiers changed from: private */
    public boolean mIsCameraShowing;
    /* access modifiers changed from: private */
    public boolean mIsCorrectOperation;
    /* access modifiers changed from: private */
    public boolean mIsPendingStartCamera;
    /* access modifiers changed from: private */
    public KeyguardUpdateMonitor mKeyguardUpdateMonitor;
    private MiuiKeyguardUpdateMonitorCallback mKeyguardUpdateMonitorCallback = new MiuiKeyguardUpdateMonitorCallback() {
        public void onStrongAuthStateChanged(int i) {
            KeyguardUpdateMonitor unused = MiuiKeyguardCameraView.this.mKeyguardUpdateMonitor;
            if (i == KeyguardUpdateMonitor.getCurrentUser() && !MiuiKeyguardCameraView.this.mUserAuthenticatedSinceBoot && MiuiKeyguardCameraView.this.mKeyguardUpdateMonitor.getStrongAuthTracker().hasUserAuthenticatedSinceBoot()) {
                boolean unused2 = MiuiKeyguardCameraView.this.mUserAuthenticatedSinceBoot = true;
                MiuiKeyguardCameraView.this.mHandler.postDelayed(new Runnable() {
                    public void run() {
                        MiuiKeyguardCameraView.this.updatePreViewBackground();
                    }
                }, 2000);
            }
        }

        public void onKeyguardShowingChanged(boolean z) {
            if (z) {
                MiuiKeyguardCameraView.this.addViewToWindow();
            } else {
                MiuiKeyguardCameraView.this.removeViewFromWindow();
            }
        }

        public void onKeyguardOccludedChanged(boolean z) {
            if (!z && MiuiKeyguardCameraView.this.mIsCameraShowing) {
                boolean unused = MiuiKeyguardCameraView.this.mIsCameraShowing = false;
                MiuiKeyguardCameraView.this.startBackAnim();
            }
        }

        public void onKeyguardVisibilityChanged(boolean z) {
            if (z) {
                return;
            }
            if (MiuiKeyguardCameraView.this.mIsPendingStartCamera) {
                boolean unused = MiuiKeyguardCameraView.this.mIsPendingStartCamera = false;
                boolean unused2 = MiuiKeyguardCameraView.this.mIsCameraShowing = true;
                MiuiKeyguardCameraView.this.setAlpha(0.0f);
                MiuiKeyguardCameraView.this.applyBlurRatio(0.0f);
                MiuiKeyguardCameraView.this.updateKeepScreenOnFlag(false);
                return;
            }
            MiuiKeyguardCameraView.this.reset();
        }
    };
    private boolean mLastIsActive;
    private WindowManager.LayoutParams mLayoutParams;
    private WindowManager.LayoutParams mLpChanged;
    private float mMoveActivePer;
    private float mMoveDistance;
    private float mMovePer;
    private float mMoveYPer;
    /* access modifiers changed from: private */
    public ImageView mPreView;
    private float mPreViewAlpha;
    private float mPreViewCenterX;
    private float mPreViewCenterY;
    /* access modifiers changed from: private */
    public LinearLayout mPreViewContainer;
    private FrameLayout.LayoutParams mPreViewContainerLayoutParams;
    private float mPreViewHeight;
    private float mPreViewInitRadius = 60.0f;
    private ViewOutlineProvider mPreViewOutlineProvider = new ViewOutlineProvider() {
        public void getOutline(View view, Outline outline) {
            outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), MiuiKeyguardCameraView.this.mPreViewRadius);
        }
    };
    /* access modifiers changed from: private */
    public float mPreViewRadius;
    private float mPreViewWidth;
    /* access modifiers changed from: private */
    public int mScreenHeight;
    private Point mScreenSizePoint;
    /* access modifiers changed from: private */
    public int mScreenWidth;
    private boolean mShowing = false;
    /* access modifiers changed from: private */
    public final TaskStackChangeListener mTaskStackListener = new TaskStackChangeListener() {
        public void onTaskMovedToFront(ActivityManager.RunningTaskInfo runningTaskInfo) {
            Log.d("KeyguardCameraView", "xxxxxxxxonTaskMovedToFront");
            if (MiuiKeyguardCameraView.this.mIsCameraShowing && MiuiKeyguardCameraView.this.isNotCameraActivity(runningTaskInfo)) {
                boolean unused = MiuiKeyguardCameraView.this.mIsCameraShowing = false;
                MiuiKeyguardCameraView.this.reset();
            }
        }

        public void onTaskStackChanged() {
            try {
                List tasks = ActivityTaskManager.getService().getTasks(1);
                if (MiuiKeyguardCameraView.this.mIsCameraShowing && !tasks.isEmpty() && MiuiKeyguardCameraView.this.isNotCameraActivity((ActivityManager.RunningTaskInfo) tasks.get(0))) {
                    boolean unused = MiuiKeyguardCameraView.this.mIsCameraShowing = false;
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
    /* access modifiers changed from: private */
    public boolean mUserAuthenticatedSinceBoot;
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
    }

    private float perFromVal(float f, float f2, float f3) {
        return (f - f2) / (f3 - f2);
    }

    private float valFromPer(float f, float f2, float f3) {
        return f2 + ((f3 - f2) * f);
    }

    /* access modifiers changed from: private */
    public boolean isNotCameraActivity(ActivityManager.RunningTaskInfo runningTaskInfo) {
        return !PackageUtils.PACKAGE_NAME_CAMERA.equals(runningTaskInfo.topActivity.getPackageName());
    }

    public MiuiKeyguardCameraView(Context context, CallBack callBack) {
        super(context);
        this.mCallBack = callBack;
        initViews();
        KeyguardUpdateMonitor keyguardUpdateMonitor = (KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class);
        this.mKeyguardUpdateMonitor = keyguardUpdateMonitor;
        keyguardUpdateMonitor.registerCallback(this.mKeyguardUpdateMonitorCallback);
        this.mUserAuthenticatedSinceBoot = this.mKeyguardUpdateMonitor.getStrongAuthTracker().hasUserAuthenticatedSinceBoot();
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
        if (MiuiKeyguardUtils.hasNavigationBar(this.mContext)) {
            this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("force_fsg_nav_bar"), false, this.mFullScreenGestureObserver);
            this.mFullScreenGestureObserver.onChange(false);
        }
        updatePreViewBackground();
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        ((IMiuiKeyguardWallpaperController) Dependency.get(IMiuiKeyguardWallpaperController.class)).registerWallpaperChangeCallback(this);
        ((ConfigurationController) Dependency.get(ConfigurationController.class)).addCallback(this);
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ((IMiuiKeyguardWallpaperController) Dependency.get(IMiuiKeyguardWallpaperController.class)).unregisterWallpaperChangeCallback(this);
        ((ConfigurationController) Dependency.get(ConfigurationController.class)).removeCallback(this);
    }

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
        Point point2 = this.mScreenSizePoint;
        this.mScreenHeight = Math.max(point2.y, point2.x);
        Point point3 = this.mScreenSizePoint;
        this.mScreenWidth = Math.min(point3.y, point3.x);
        View view = new View(getContext());
        this.mBackgroundView = view;
        view.setBackgroundColor(-16777216);
        this.mBackgroundView.setAlpha(0.0f);
        addView(this.mBackgroundView, new FrameLayout.LayoutParams(-1, -1, 17));
        View view2 = new View(getContext());
        this.mCameraScrimView = view2;
        view2.setBackgroundColor(-1728053248);
        this.mCameraScrimView.setAlpha(0.0f);
        addView(this.mCameraScrimView, new FrameLayout.LayoutParams(-1, -1, 17));
        LinearLayout linearLayout = new LinearLayout(getContext());
        this.mPreViewContainer = linearLayout;
        linearLayout.setOutlineProvider(this.mPreViewOutlineProvider);
        this.mPreViewContainer.setClipToOutline(true);
        this.mPreViewContainer.setBackgroundColor(-16777216);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(0, 0, 17);
        this.mPreViewContainerLayoutParams = layoutParams;
        addView(this.mPreViewContainer, layoutParams);
        ImageView imageView = new ImageView(getContext());
        this.mPreView = imageView;
        this.mPreViewContainer.addView(imageView, new FrameLayout.LayoutParams(-1, -1, 17));
        this.mIconView = new ImageView(getContext());
        this.mIconWidth = this.mContext.getResources().getDimensionPixelOffset(C0012R$dimen.keyguard_affordance_width);
        this.mIconHeight = this.mContext.getResources().getDimensionPixelOffset(C0012R$dimen.keyguard_affordance_height);
        this.mIconView.setScaleType(ImageView.ScaleType.CENTER);
        this.mIconView.setImageDrawable(this.mContext.getDrawable(C0013R$drawable.keyguard_bottom_camera_img));
        addView(this.mIconView, new FrameLayout.LayoutParams(this.mIconWidth, this.mIconHeight, 8388693));
        int i = this.mScreenWidth;
        this.mIconInitCenterX = (float) (i - (this.mIconWidth / 2));
        int i2 = this.mScreenHeight;
        this.mIconInitCenterY = (float) (i2 - (this.mIconHeight / 2));
        this.mIconActiveCenterX = ((float) i) * 0.55f;
        this.mIconActiveCenterY = ((float) i2) * 0.8f;
        this.mIconActiveWidth = ((float) i) * 0.74f;
        WindowManager.LayoutParams layoutParams2 = new WindowManager.LayoutParams(-1, -1, Build.VERSION.SDK_INT > 29 ? 2017 : 2014, 218171160, -2);
        this.mLayoutParams = layoutParams2;
        layoutParams2.x = 0;
        layoutParams2.y = 0;
        layoutParams2.setTitle("keyguard_camera");
        WindowManager.LayoutParams layoutParams3 = new WindowManager.LayoutParams();
        this.mLpChanged = layoutParams3;
        layoutParams3.copyFrom(this.mLayoutParams);
        setVisibility(8);
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        int updateFrom = this.mConfiguration.updateFrom(configuration);
        boolean z = true;
        boolean z2 = (updateFrom & 128) != 0;
        if ((updateFrom & 2048) == 0) {
            z = false;
        }
        if (z2 && MiuiKeyguardUtils.isPad()) {
            updatePreViewBackground();
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
            updatePreViewBackground();
            refreshView();
        }
    }

    private void updateSizeForScreenSizeChange() {
        Point point = this.mScreenSizePoint;
        this.mScreenHeight = Math.max(point.y, point.x);
        Point point2 = this.mScreenSizePoint;
        this.mScreenWidth = Math.min(point2.y, point2.x);
        this.mIconWidth = this.mContext.getResources().getDimensionPixelOffset(C0012R$dimen.keyguard_affordance_width);
        this.mIconHeight = this.mContext.getResources().getDimensionPixelOffset(C0012R$dimen.keyguard_affordance_height);
        this.mIconView.setImageDrawable(this.mContext.getDrawable(C0013R$drawable.keyguard_bottom_camera_img));
        updateLayoutParams();
        int i = this.mScreenWidth;
        this.mIconInitCenterX = (float) (i - (this.mIconWidth / 2));
        int i2 = this.mScreenHeight;
        this.mIconInitCenterY = (float) (i2 - (this.mIconHeight / 2));
        this.mIconActiveCenterX = ((float) i) * 0.55f;
        this.mIconActiveCenterY = ((float) i2) * 0.8f;
        this.mIconActiveWidth = ((float) i) * 0.74f;
    }

    private void updateLayoutParams() {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) this.mIconView.getLayoutParams();
        layoutParams.width = this.mIconWidth;
        layoutParams.height = this.mIconHeight;
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
        float f11 = f;
        if (!this.mDarkStyle) {
            float f12 = this.mPreViewRadius;
            canvas.drawRoundRect(f6 - 2.0f, f11 - 2.0f, f7 + 2.0f, f10 + 2.0f, f12, f12, this.mIconCircleStrokePaint);
        }
        this.mIconCirclePaint.setAlpha((int) (this.mIconCircleAlpha * 255.0f));
        float f13 = this.mPreViewRadius;
        canvas.drawRoundRect(f6, f11, f7, f10, f13, f13, this.mIconCirclePaint);
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
        this.mPreViewContainer.setX(this.mPreViewCenterX - (this.mPreViewWidth / 2.0f));
        this.mPreViewContainer.setY(this.mPreViewCenterY - (this.mPreViewHeight / 2.0f));
        FrameLayout.LayoutParams layoutParams = this.mPreViewContainerLayoutParams;
        layoutParams.width = (int) this.mPreViewWidth;
        layoutParams.height = (int) this.mPreViewHeight;
        this.mPreViewContainer.setLayoutParams(layoutParams);
        this.mPreViewContainer.setAlpha(this.mPreViewAlpha);
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
        ValueAnimator ofFloat = ValueAnimator.ofFloat(new float[]{f, f2});
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                MiuiKeyguardCameraView.this.lambda$startActiveAnim$0$MiuiKeyguardCameraView(valueAnimator);
            }
        });
        this.mAnimatorSet.playTogether(new Animator[]{ofFloat});
        if (f2 == 0.0f) {
            this.mAnimatorSet.setInterpolator(new PhysicBasedInterpolator(this, 0.9f, 0.8f));
            this.mAnimatorSet.setDuration(250);
        } else {
            this.mAnimatorSet.setInterpolator(new PhysicBasedInterpolator(this, 0.8f, 0.67f));
            this.mAnimatorSet.setDuration(450);
        }
        this.mAnimatorSet.setDuration(450);
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
        this.mAnimatorSet.setDuration(350);
        this.mAnimatorSet.addListener(new AnimatorListenerAdapter() {
            private boolean mCancelled;

            public void onAnimationCancel(Animator animator) {
                this.mCancelled = true;
            }

            public void onAnimationEnd(Animator animator) {
                super.onAnimationEnd(animator);
                if (MiuiKeyguardCameraView.this.mCallBack != null) {
                    MiuiKeyguardCameraView.this.mCallBack.onCompletedAnimationEnd();
                }
                if (MiuiKeyguardCameraView.this.mBackgroundAnimator != null) {
                    MiuiKeyguardCameraView.this.mBackgroundAnimator.cancel();
                    ValueAnimator unused = MiuiKeyguardCameraView.this.mBackgroundAnimator = null;
                }
                if (!this.mCancelled) {
                    ActivityManagerWrapper.getInstance().registerTaskStackListener(MiuiKeyguardCameraView.this.mTaskStackListener);
                    AnalyticsHelper.getInstance(MiuiKeyguardCameraView.this.mContext).recordKeyguardAction("action_enter_camera_view");
                    AnalyticsHelper.getInstance(MiuiKeyguardCameraView.this.mContext).trackPageStart("action_enter_camera_view");
                    MiuiKeyguardCameraView.this.mContext.startActivityAsUser(PackageUtils.getCameraIntent(), UserHandle.CURRENT);
                }
            }
        });
        this.mAnimatorSet.start();
        ValueAnimator ofFloat = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
        this.mBackgroundAnimator = ofFloat;
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                MiuiKeyguardCameraView.this.lambda$startFullScreenAnim$1$MiuiKeyguardCameraView(valueAnimator);
            }
        });
        this.mBackgroundAnimator.setInterpolator(new PhysicBasedInterpolator(this, 0.99f, 0.67f));
        this.mBackgroundAnimator.setDuration(450);
        this.mBackgroundAnimator.start();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$startFullScreenAnim$1 */
    public /* synthetic */ void lambda$startFullScreenAnim$1$MiuiKeyguardCameraView(ValueAnimator valueAnimator) {
        this.mBackgroundView.setAlpha(((Float) valueAnimator.getAnimatedValue()).floatValue());
    }

    private AnimatorSet getFullScreenAnim(float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, float f9, float f10, float f11, float f12, float f13, float f14, float f15, float f16) {
        AnimatorSet animatorSet = new AnimatorSet();
        ValueAnimator ofFloat = ValueAnimator.ofFloat(new float[]{f, f2});
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                MiuiKeyguardCameraView.this.lambda$getFullScreenAnim$2$MiuiKeyguardCameraView(valueAnimator);
            }
        });
        ValueAnimator ofFloat2 = ValueAnimator.ofFloat(new float[]{f3, f4});
        ofFloat2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                MiuiKeyguardCameraView.this.lambda$getFullScreenAnim$3$MiuiKeyguardCameraView(valueAnimator);
            }
        });
        ValueAnimator ofFloat3 = ValueAnimator.ofFloat(new float[]{f5, f6});
        ofFloat3.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                MiuiKeyguardCameraView.this.lambda$getFullScreenAnim$4$MiuiKeyguardCameraView(valueAnimator);
            }
        });
        ValueAnimator ofFloat4 = ValueAnimator.ofFloat(new float[]{f7, f8});
        ofFloat4.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                MiuiKeyguardCameraView.this.lambda$getFullScreenAnim$5$MiuiKeyguardCameraView(valueAnimator);
            }
        });
        ValueAnimator ofFloat5 = ValueAnimator.ofFloat(new float[]{f11, f12});
        ofFloat5.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                MiuiKeyguardCameraView.this.lambda$getFullScreenAnim$6$MiuiKeyguardCameraView(valueAnimator);
            }
        });
        ValueAnimator ofFloat6 = ValueAnimator.ofFloat(new float[]{f9, f10});
        ofFloat6.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                MiuiKeyguardCameraView.this.lambda$getFullScreenAnim$7$MiuiKeyguardCameraView(valueAnimator);
            }
        });
        ValueAnimator ofFloat7 = ValueAnimator.ofFloat(new float[]{f13, f14});
        ofFloat7.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                MiuiKeyguardCameraView.this.lambda$getFullScreenAnim$8$MiuiKeyguardCameraView(valueAnimator);
            }
        });
        ValueAnimator ofFloat8 = ValueAnimator.ofFloat(new float[]{f15, f16});
        ofFloat8.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                MiuiKeyguardCameraView.this.lambda$getFullScreenAnim$9$MiuiKeyguardCameraView(valueAnimator);
            }
        });
        animatorSet.playTogether(new Animator[]{ofFloat, ofFloat2, ofFloat3, ofFloat4, ofFloat5, ofFloat6, ofFloat7, ofFloat8});
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
        ValueAnimator ofFloat = ValueAnimator.ofFloat(new float[]{this.mActiveAnimPer, 0.0f});
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                MiuiKeyguardCameraView.this.lambda$startCancelAnim$10$MiuiKeyguardCameraView(valueAnimator);
            }
        });
        ofFloat.setInterpolator(new PhysicBasedInterpolator(this, 0.8f, 0.67f));
        ofFloat.setDuration(450);
        ValueAnimator ofFloat2 = ValueAnimator.ofFloat(new float[]{this.mMoveYPer, 0.0f});
        ofFloat2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                MiuiKeyguardCameraView.this.lambda$startCancelAnim$11$MiuiKeyguardCameraView(valueAnimator);
            }
        });
        ofFloat2.setInterpolator(new PhysicBasedInterpolator(this, 0.8f, 0.67f));
        ofFloat2.setDuration(450);
        ValueAnimator ofFloat3 = ValueAnimator.ofFloat(new float[]{this.mMoveDistance, 0.0f});
        ofFloat3.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                MiuiKeyguardCameraView.this.lambda$startCancelAnim$12$MiuiKeyguardCameraView(valueAnimator);
            }
        });
        ofFloat3.setInterpolator(new PhysicBasedInterpolator(this, 0.8f, 0.71f));
        ofFloat3.setDuration(700);
        this.mAnimatorSet.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animator) {
                MiuiKeyguardCameraView.this.dismiss();
                if (MiuiKeyguardCameraView.this.mCallBack != null) {
                    MiuiKeyguardCameraView.this.mCallBack.onCancelAnimationEnd(MiuiKeyguardCameraView.this.mIsCorrectOperation);
                }
            }
        });
        this.mAnimatorSet.playTogether(new Animator[]{ofFloat, ofFloat2, ofFloat3});
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
    public void startBackAnim() {
        this.mIsBackAnimRunning = true;
        cancelAnim();
        int i = this.mScreenWidth;
        AnimatorSet backIconAnim = getBackIconAnim((float) i, 0.0f, (float) (i / 2), this.mIconInitCenterX, (float) (this.mScreenHeight / 2), this.mIconInitCenterY, 1.5f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 0.0f);
        this.mAnimatorSet = backIconAnim;
        backIconAnim.setInterpolator(new PhysicBasedInterpolator(this, 0.8f, 0.71f));
        this.mAnimatorSet.setDuration(700);
        this.mAnimatorSet.addListener(new AnimatorListenerAdapter() {
            public void onAnimationStart(Animator animator) {
                ValueAnimator ofFloat = ValueAnimator.ofFloat(new float[]{((float) MiuiKeyguardCameraView.this.mScreenHeight) / ((float) MiuiKeyguardCameraView.this.mScreenWidth), 1.0f});
                ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                        MiuiKeyguardCameraView.AnonymousClass7.this.lambda$onAnimationStart$0$MiuiKeyguardCameraView$7(valueAnimator);
                    }
                });
                ofFloat.setInterpolator(new PhysicBasedInterpolator(MiuiKeyguardCameraView.this, 0.99f, 0.67f));
                ofFloat.setDuration(300);
                ofFloat.start();
                if (!DeviceConfig.isLowGpuDevice()) {
                    MiuiKeyguardCameraView.this.applyBlurRatio(1.0f);
                }
                MiuiKeyguardCameraView.this.mBackgroundView.setAlpha(1.0f);
                MiuiKeyguardCameraView.this.mPreViewContainer.setAlpha(0.0f);
                MiuiKeyguardCameraView.this.mCameraScrimView.setAlpha(0.0f);
                MiuiKeyguardCameraView.this.setAlpha(1.0f);
            }

            /* access modifiers changed from: private */
            /* renamed from: lambda$onAnimationStart$0 */
            public /* synthetic */ void lambda$onAnimationStart$0$MiuiKeyguardCameraView$7(ValueAnimator valueAnimator) {
                float unused = MiuiKeyguardCameraView.this.mBackAnimAspectRatio = ((Float) valueAnimator.getAnimatedValue()).floatValue();
            }

            public void onAnimationEnd(Animator animator) {
                boolean unused = MiuiKeyguardCameraView.this.mIsBackAnimRunning = false;
                MiuiKeyguardCameraView.this.reset();
                if (MiuiKeyguardCameraView.this.mCallBack != null) {
                    MiuiKeyguardCameraView.this.mCallBack.onBackAnimationEnd();
                }
            }
        });
        this.mAnimatorSet.start();
    }

    private AnimatorSet getBackIconAnim(float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, float f9, float f10, float f11, float f12, float f13, float f14) {
        AnimatorSet animatorSet = new AnimatorSet();
        ValueAnimator ofFloat = ValueAnimator.ofFloat(new float[]{f, f2});
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                MiuiKeyguardCameraView.this.lambda$getBackIconAnim$13$MiuiKeyguardCameraView(valueAnimator);
            }
        });
        ValueAnimator ofFloat2 = ValueAnimator.ofFloat(new float[]{f3, f4});
        ofFloat2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                MiuiKeyguardCameraView.this.lambda$getBackIconAnim$14$MiuiKeyguardCameraView(valueAnimator);
            }
        });
        ValueAnimator ofFloat3 = ValueAnimator.ofFloat(new float[]{f5, f6});
        ofFloat3.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                MiuiKeyguardCameraView.this.lambda$getBackIconAnim$15$MiuiKeyguardCameraView(valueAnimator);
            }
        });
        ValueAnimator ofFloat4 = ValueAnimator.ofFloat(new float[]{f11, f12});
        ofFloat4.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                MiuiKeyguardCameraView.this.lambda$getBackIconAnim$16$MiuiKeyguardCameraView(valueAnimator);
            }
        });
        ValueAnimator ofFloat5 = ValueAnimator.ofFloat(new float[]{f9, f10});
        ofFloat5.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                MiuiKeyguardCameraView.this.lambda$getBackIconAnim$17$MiuiKeyguardCameraView(valueAnimator);
            }
        });
        ValueAnimator ofFloat6 = ValueAnimator.ofFloat(new float[]{f7, f8});
        ofFloat6.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                MiuiKeyguardCameraView.this.lambda$getBackIconAnim$18$MiuiKeyguardCameraView(valueAnimator);
            }
        });
        ValueAnimator ofFloat7 = ValueAnimator.ofFloat(new float[]{f13, f14});
        ofFloat7.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                MiuiKeyguardCameraView.this.lambda$getBackIconAnim$19$MiuiKeyguardCameraView(valueAnimator);
            }
        });
        animatorSet.playTogether(new Animator[]{ofFloat, ofFloat2, ofFloat3, ofFloat4, ofFloat5, ofFloat6, ofFloat7});
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

    private void applyParams() {
        if (isAttachedToWindow() && this.mWindowManager != null && this.mLayoutParams.copyFrom(this.mLpChanged) != 0) {
            this.mWindowManager.updateViewLayout(this, this.mLayoutParams);
        }
    }

    /* access modifiers changed from: private */
    public void applyBlurRatio(float f) {
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
            this.mCallBack.onVisibilityChanged(true);
            updateKeepScreenOnFlag(true);
        }
    }

    public void dismiss() {
        Log.d("KeyguardCameraView", "dismiss " + this.mShowing);
        if (this.mShowing) {
            this.mShowing = false;
            setVisibility(8);
            this.mCallBack.onVisibilityChanged(false);
            updateKeepScreenOnFlag(false);
        }
    }

    /* access modifiers changed from: private */
    public void updateKeepScreenOnFlag(boolean z) {
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
    }

    /* access modifiers changed from: private */
    public void updatePreViewBackground() {
        if (this.mPreView != null) {
            new AsyncTask<Void, Void, Drawable>() {
                private boolean mIsProviderDrawable;

                /* access modifiers changed from: protected */
                public Drawable doInBackground(Void... voidArr) {
                    if (!MiuiKeyguardCameraView.this.mUserAuthenticatedSinceBoot) {
                        return null;
                    }
                    if (PackageUtils.IS_VELA_CAMERA) {
                        return PackageUtils.getDrawableFromPackage(MiuiKeyguardCameraView.this.mContext, PackageUtils.PACKAGE_NAME_CAMERA, MiuiKeyguardUtils.getCameraImageName(MiuiKeyguardCameraView.this.mContext, MiuiKeyguardUtils.isFullScreenGestureOpened()));
                    }
                    Drawable otherDrawable = getOtherDrawable();
                    if (otherDrawable != null) {
                        this.mIsProviderDrawable = true;
                    }
                    return otherDrawable;
                }

                private Drawable getOtherDrawable() {
                    Context access$1400 = MiuiKeyguardCameraView.this.mContext;
                    Drawable drawable = null;
                    Bundle resultFromProvider = ContentProviderUtils.getResultFromProvider(access$1400, "content://" + PackageUtils.PACKAGE_NAME_CAMERA + ".splashProvider", "getCameraSplash", (String) null, (Bundle) null);
                    if (resultFromProvider != null) {
                        String valueOf = String.valueOf(resultFromProvider.get("getCameraSplash"));
                        if (!TextUtils.isEmpty(valueOf)) {
                            try {
                                drawable = ImageDecoder.decodeDrawable(ImageDecoder.createSource(MiuiKeyguardCameraView.this.mContext.getContentResolver(), Uri.parse(valueOf), MiuiKeyguardCameraView.this.mContext.getResources()), $$Lambda$MiuiKeyguardCameraView$8$uCIKy0mDwr8roI9KpspXAkYl1Wg.INSTANCE);
                            } catch (IOException e) {
                                Log.e("KeyguardCameraView", "updatePreViewBackground " + e.getMessage());
                            }
                        }
                    }
                    return drawable == null ? PackageUtils.getDrawableFromPackage(MiuiKeyguardCameraView.this.mContext, PackageUtils.PACKAGE_NAME_CAMERA, MiuiKeyguardUtils.getCameraImageName(MiuiKeyguardCameraView.this.mContext, MiuiKeyguardUtils.isFullScreenGestureOpened())) : drawable;
                }

                /* access modifiers changed from: protected */
                public void onPostExecute(Drawable drawable) {
                    if (drawable != null) {
                        if (this.mIsProviderDrawable || PackageUtils.IS_VELA_CAMERA) {
                            MiuiKeyguardCameraView.this.mPreView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        } else {
                            MiuiKeyguardCameraView.this.mPreView.setScaleType(ImageView.ScaleType.FIT_END);
                        }
                        MiuiKeyguardCameraView.this.mPreView.setImageDrawable(drawable);
                    } else if (PackageUtils.IS_VELA_CAMERA) {
                        MiuiKeyguardCameraView.this.mPreView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        MiuiKeyguardCameraView.this.mPreView.setImageResource(C0013R$drawable.meitu_camera_preview);
                    } else {
                        MiuiKeyguardCameraView.this.mPreView.setScaleType(ImageView.ScaleType.FIT_END);
                        MiuiKeyguardCameraView.this.mPreView.setImageResource(C0013R$drawable.camera_preview);
                    }
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
        }
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
