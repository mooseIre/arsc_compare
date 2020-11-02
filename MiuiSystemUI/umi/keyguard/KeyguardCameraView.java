package com.android.keyguard;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
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
import android.os.UserHandle;
import android.os.Vibrator;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceControlCompat;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.WindowManager;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.android.keyguard.KeyguardCameraView;
import com.android.keyguard.analytics.AnalyticsHelper;
import com.android.keyguard.utils.ContentProviderUtils;
import com.android.keyguard.utils.PackageUtils;
import com.android.systemui.Dependency;
import com.android.systemui.HapticFeedBackImpl;
import com.android.systemui.miui.ActivityObserver;
import com.android.systemui.plugins.R;
import java.io.IOException;

public class KeyguardCameraView extends FrameLayout {
    private float mActiveAnimPer;
    /* access modifiers changed from: private */
    public ActivityObserver.ActivityObserverCallback mActivityStateObserver = new ActivityObserver.ActivityObserverCallback() {
        public void activityResumed(final Intent intent) {
            KeyguardCameraView.this.post(new Runnable() {
                public void run() {
                    Intent intent;
                    if (KeyguardCameraView.this.mIsCameraShowing && (intent = intent) != null && intent.getComponent() != null && !PackageUtils.PACKAGE_NAME_CAMERA.equals(intent.getComponent().getPackageName())) {
                        boolean unused = KeyguardCameraView.this.mIsCameraShowing = false;
                        KeyguardCameraView.this.reset();
                    }
                }
            });
        }
    };
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
    public Context mContext = getContext();
    private boolean mDarkMode;
    ContentObserver mFullScreenGestureObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean z) {
            super.onChange(z);
            KeyguardCameraView.this.updatePreViewBackground();
        }
    };
    /* access modifiers changed from: private */
    public Handler mHandler = new Handler();
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
    private KeyguardUpdateMonitorCallback mKeyguardUpdateMonitorCallback = new KeyguardUpdateMonitorCallback() {
        public void onStrongAuthStateChanged(int i) {
            KeyguardUpdateMonitor unused = KeyguardCameraView.this.mKeyguardUpdateMonitor;
            if (i == KeyguardUpdateMonitor.getCurrentUser() && !KeyguardCameraView.this.mUserAuthenticatedSinceBoot && KeyguardCameraView.this.mKeyguardUpdateMonitor.getStrongAuthTracker().hasUserAuthenticatedSinceBoot()) {
                boolean unused2 = KeyguardCameraView.this.mUserAuthenticatedSinceBoot = true;
                KeyguardCameraView.this.mHandler.postDelayed(new Runnable() {
                    public void run() {
                        KeyguardCameraView.this.updatePreViewBackground();
                    }
                }, 2000);
            }
        }

        public void onKeyguardShowingChanged(boolean z) {
            if (z) {
                KeyguardCameraView.this.addViewToWindow();
            } else {
                KeyguardCameraView.this.removeViewFromWindow();
            }
        }

        public void onKeyguardOccludedChanged(boolean z) {
            if (!z && KeyguardCameraView.this.mIsCameraShowing) {
                boolean unused = KeyguardCameraView.this.mIsCameraShowing = false;
                KeyguardCameraView.this.startBackAnim();
            }
        }

        public void onKeyguardVisibilityChanged(boolean z) {
            if (z) {
                return;
            }
            if (KeyguardCameraView.this.mIsPendingStartCamera) {
                boolean unused = KeyguardCameraView.this.mIsPendingStartCamera = false;
                boolean unused2 = KeyguardCameraView.this.mIsCameraShowing = true;
                KeyguardCameraView.this.setAlpha(0.0f);
                KeyguardCameraView.this.applyBlurRatio(0.0f);
                KeyguardCameraView.this.updateKeepScreenOnFlag(false);
                return;
            }
            KeyguardCameraView.this.reset();
        }
    };
    private boolean mLastIsActive;
    private WindowManager.LayoutParams mLayoutParams;
    private WindowManager.LayoutParams mLpChanged;
    private float mMoveActivePer;
    private float mMoveDistance;
    private float mMovePer;
    private float mMoveYPer;
    private int mOrientation = 1;
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
            outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), KeyguardCameraView.this.mPreViewRadius);
        }
    };
    /* access modifiers changed from: private */
    public float mPreViewRadius;
    private float mPreViewWidth;
    /* access modifiers changed from: private */
    public int mScreenHeight;
    /* access modifiers changed from: private */
    public int mScreenWidth;
    private boolean mShowing = false;
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

    public KeyguardCameraView(Context context, CallBack callBack) {
        super(context);
        this.mCallBack = callBack;
        initViews();
        KeyguardUpdateMonitor instance = KeyguardUpdateMonitor.getInstance(this.mContext);
        this.mKeyguardUpdateMonitor = instance;
        instance.registerCallback(this.mKeyguardUpdateMonitorCallback);
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

    private void initViews() {
        setSystemUiVisibility(4864);
        this.mWindowManager = (WindowManager) getContext().getSystemService("window");
        Display display = ((DisplayManager) getContext().getSystemService("display")).getDisplay(0);
        Point point = new Point();
        display.getRealSize(point);
        this.mScreenHeight = Math.max(point.y, point.x);
        this.mScreenWidth = Math.min(point.y, point.x);
        View view = new View(getContext());
        this.mBackgroundView = view;
        view.setBackgroundColor(-16777216);
        this.mBackgroundView.setAlpha(0.0f);
        addView(this.mBackgroundView, new FrameLayout.LayoutParams(-1, -1, 17));
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
        this.mIconWidth = this.mContext.getResources().getDimensionPixelOffset(R.dimen.keyguard_affordance_width);
        this.mIconHeight = this.mContext.getResources().getDimensionPixelOffset(R.dimen.keyguard_affordance_height);
        this.mIconView.setScaleType(ImageView.ScaleType.CENTER);
        this.mIconView.setImageDrawable(this.mContext.getDrawable(R.drawable.keyguard_bottom_camera_img));
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
        int i = configuration.orientation;
        if (i != this.mOrientation) {
            this.mOrientation = i;
            if (MiuiKeyguardUtils.isPad()) {
                updatePreViewBackground();
            }
        }
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
        if (!this.mDarkMode) {
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
            ((ActivityObserver) Dependency.get(ActivityObserver.class)).removeCallback(this.mActivityStateObserver);
        }
    }

    public void setDarkMode(boolean z) {
        if (this.mDarkMode != z) {
            this.mDarkMode = z;
            this.mIconView.setImageDrawable(this.mContext.getDrawable(z ? R.drawable.keyguard_bottom_camera_img_dark : R.drawable.keyguard_bottom_camera_img));
        }
    }

    private void handleMoveDistanceChanged() {
        this.mMovePer = perFromVal(this.mMoveDistance, 0.0f, (float) (this.mScreenWidth / 3));
        notifyAnimUpdate();
        updateBlurRatio();
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
                KeyguardCameraView.this.lambda$startActiveAnim$0$KeyguardCameraView(valueAnimator);
            }
        });
        this.mAnimatorSet.playTogether(new Animator[]{ofFloat});
        if (f2 == 0.0f) {
            this.mAnimatorSet.setInterpolator(new PhysicBasedInterpolator(0.9f, 0.8f));
            this.mAnimatorSet.setDuration(250);
        } else {
            this.mAnimatorSet.setInterpolator(new PhysicBasedInterpolator(0.8f, 0.67f));
            this.mAnimatorSet.setDuration(450);
        }
        this.mAnimatorSet.setDuration(450);
        this.mAnimatorSet.start();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$startActiveAnim$0 */
    public /* synthetic */ void lambda$startActiveAnim$0$KeyguardCameraView(ValueAnimator valueAnimator) {
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
        fullScreenAnim.setInterpolator(new PhysicBasedInterpolator(0.9f, 0.85f));
        this.mAnimatorSet.setDuration(350);
        this.mAnimatorSet.addListener(new AnimatorListenerAdapter() {
            private boolean mCancelled;

            public void onAnimationCancel(Animator animator) {
                this.mCancelled = true;
            }

            public void onAnimationEnd(Animator animator) {
                Class cls = ActivityObserver.class;
                super.onAnimationEnd(animator);
                if (KeyguardCameraView.this.mCallBack != null) {
                    KeyguardCameraView.this.mCallBack.onCompletedAnimationEnd();
                }
                if (KeyguardCameraView.this.mBackgroundAnimator != null) {
                    KeyguardCameraView.this.mBackgroundAnimator.cancel();
                    ValueAnimator unused = KeyguardCameraView.this.mBackgroundAnimator = null;
                }
                if (!this.mCancelled) {
                    ((ActivityObserver) Dependency.get(cls)).removeCallback(KeyguardCameraView.this.mActivityStateObserver);
                    ((ActivityObserver) Dependency.get(cls)).addCallback(KeyguardCameraView.this.mActivityStateObserver);
                    AnalyticsHelper.getInstance(KeyguardCameraView.this.mContext).recordKeyguardAction("action_enter_camera_view");
                    AnalyticsHelper.getInstance(KeyguardCameraView.this.mContext).trackPageStart("action_enter_camera_view");
                    KeyguardCameraView.this.mContext.startActivityAsUser(PackageUtils.getCameraIntent(), UserHandle.CURRENT);
                }
            }
        });
        this.mAnimatorSet.start();
        ValueAnimator ofFloat = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
        this.mBackgroundAnimator = ofFloat;
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                KeyguardCameraView.this.lambda$startFullScreenAnim$1$KeyguardCameraView(valueAnimator);
            }
        });
        this.mBackgroundAnimator.setInterpolator(new PhysicBasedInterpolator(0.99f, 0.67f));
        this.mBackgroundAnimator.setDuration(450);
        this.mBackgroundAnimator.start();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$startFullScreenAnim$1 */
    public /* synthetic */ void lambda$startFullScreenAnim$1$KeyguardCameraView(ValueAnimator valueAnimator) {
        this.mBackgroundView.setAlpha(((Float) valueAnimator.getAnimatedValue()).floatValue());
    }

    private AnimatorSet getFullScreenAnim(float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, float f9, float f10, float f11, float f12, float f13, float f14, float f15, float f16) {
        AnimatorSet animatorSet = new AnimatorSet();
        ValueAnimator ofFloat = ValueAnimator.ofFloat(new float[]{f, f2});
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                KeyguardCameraView.this.lambda$getFullScreenAnim$2$KeyguardCameraView(valueAnimator);
            }
        });
        ValueAnimator ofFloat2 = ValueAnimator.ofFloat(new float[]{f3, f4});
        ofFloat2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                KeyguardCameraView.this.lambda$getFullScreenAnim$3$KeyguardCameraView(valueAnimator);
            }
        });
        ValueAnimator ofFloat3 = ValueAnimator.ofFloat(new float[]{f5, f6});
        ofFloat3.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                KeyguardCameraView.this.lambda$getFullScreenAnim$4$KeyguardCameraView(valueAnimator);
            }
        });
        ValueAnimator ofFloat4 = ValueAnimator.ofFloat(new float[]{f7, f8});
        ofFloat4.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                KeyguardCameraView.this.lambda$getFullScreenAnim$5$KeyguardCameraView(valueAnimator);
            }
        });
        ValueAnimator ofFloat5 = ValueAnimator.ofFloat(new float[]{f11, f12});
        ofFloat5.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                KeyguardCameraView.this.lambda$getFullScreenAnim$6$KeyguardCameraView(valueAnimator);
            }
        });
        ValueAnimator ofFloat6 = ValueAnimator.ofFloat(new float[]{f9, f10});
        ofFloat6.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                KeyguardCameraView.this.lambda$getFullScreenAnim$7$KeyguardCameraView(valueAnimator);
            }
        });
        ValueAnimator ofFloat7 = ValueAnimator.ofFloat(new float[]{f13, f14});
        ofFloat7.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                KeyguardCameraView.this.lambda$getFullScreenAnim$8$KeyguardCameraView(valueAnimator);
            }
        });
        ValueAnimator ofFloat8 = ValueAnimator.ofFloat(new float[]{f15, f16});
        ofFloat8.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                KeyguardCameraView.this.lambda$getFullScreenAnim$9$KeyguardCameraView(valueAnimator);
            }
        });
        animatorSet.playTogether(new Animator[]{ofFloat, ofFloat2, ofFloat3, ofFloat4, ofFloat5, ofFloat6, ofFloat7, ofFloat8});
        return animatorSet;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$getFullScreenAnim$2 */
    public /* synthetic */ void lambda$getFullScreenAnim$2$KeyguardCameraView(ValueAnimator valueAnimator) {
        float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
        this.mPreViewWidth = floatValue;
        this.mIconCircleWidth = floatValue;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$getFullScreenAnim$3 */
    public /* synthetic */ void lambda$getFullScreenAnim$3$KeyguardCameraView(ValueAnimator valueAnimator) {
        float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
        this.mPreViewHeight = floatValue;
        this.mIconCircleHeight = floatValue;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$getFullScreenAnim$4 */
    public /* synthetic */ void lambda$getFullScreenAnim$4$KeyguardCameraView(ValueAnimator valueAnimator) {
        float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
        this.mPreViewCenterX = floatValue;
        this.mIconCircleCenterX = floatValue;
        this.mIconCenterX = floatValue;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$getFullScreenAnim$5 */
    public /* synthetic */ void lambda$getFullScreenAnim$5$KeyguardCameraView(ValueAnimator valueAnimator) {
        float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
        this.mPreViewCenterY = floatValue;
        this.mIconCircleCenterY = floatValue;
        this.mIconCenterY = (floatValue + (this.mIconCircleHeight / 2.0f)) - (this.mIconCircleWidth / 2.0f);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$getFullScreenAnim$6 */
    public /* synthetic */ void lambda$getFullScreenAnim$6$KeyguardCameraView(ValueAnimator valueAnimator) {
        this.mPreViewAlpha = ((Float) valueAnimator.getAnimatedValue()).floatValue();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$getFullScreenAnim$7 */
    public /* synthetic */ void lambda$getFullScreenAnim$7$KeyguardCameraView(ValueAnimator valueAnimator) {
        this.mPreViewRadius = ((Float) valueAnimator.getAnimatedValue()).floatValue();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$getFullScreenAnim$8 */
    public /* synthetic */ void lambda$getFullScreenAnim$8$KeyguardCameraView(ValueAnimator valueAnimator) {
        this.mIconAlpha = ((Float) valueAnimator.getAnimatedValue()).floatValue();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$getFullScreenAnim$9 */
    public /* synthetic */ void lambda$getFullScreenAnim$9$KeyguardCameraView(ValueAnimator valueAnimator) {
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
                KeyguardCameraView.this.lambda$startCancelAnim$10$KeyguardCameraView(valueAnimator);
            }
        });
        ofFloat.setInterpolator(new PhysicBasedInterpolator(0.8f, 0.67f));
        ofFloat.setDuration(450);
        ValueAnimator ofFloat2 = ValueAnimator.ofFloat(new float[]{this.mMoveYPer, 0.0f});
        ofFloat2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                KeyguardCameraView.this.lambda$startCancelAnim$11$KeyguardCameraView(valueAnimator);
            }
        });
        ofFloat2.setInterpolator(new PhysicBasedInterpolator(0.8f, 0.67f));
        ofFloat2.setDuration(450);
        ValueAnimator ofFloat3 = ValueAnimator.ofFloat(new float[]{this.mMoveDistance, 0.0f});
        ofFloat3.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                KeyguardCameraView.this.lambda$startCancelAnim$12$KeyguardCameraView(valueAnimator);
            }
        });
        ofFloat3.setInterpolator(new PhysicBasedInterpolator(0.8f, 0.71f));
        ofFloat3.setDuration(700);
        this.mAnimatorSet.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animator) {
                KeyguardCameraView.this.dismiss();
                if (KeyguardCameraView.this.mCallBack != null) {
                    KeyguardCameraView.this.mCallBack.onCancelAnimationEnd(KeyguardCameraView.this.mIsCorrectOperation);
                }
            }
        });
        this.mAnimatorSet.playTogether(new Animator[]{ofFloat, ofFloat2, ofFloat3});
        this.mAnimatorSet.start();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$startCancelAnim$10 */
    public /* synthetic */ void lambda$startCancelAnim$10$KeyguardCameraView(ValueAnimator valueAnimator) {
        float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
        this.mActiveAnimPer = floatValue;
        if (floatValue < 0.0f) {
            floatValue = 0.0f;
        }
        this.mActiveAnimPer = floatValue;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$startCancelAnim$11 */
    public /* synthetic */ void lambda$startCancelAnim$11$KeyguardCameraView(ValueAnimator valueAnimator) {
        this.mMoveYPer = ((Float) valueAnimator.getAnimatedValue()).floatValue();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$startCancelAnim$12 */
    public /* synthetic */ void lambda$startCancelAnim$12$KeyguardCameraView(ValueAnimator valueAnimator) {
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
        backIconAnim.setInterpolator(new PhysicBasedInterpolator(0.8f, 0.71f));
        this.mAnimatorSet.setDuration(700);
        this.mAnimatorSet.addListener(new AnimatorListenerAdapter() {
            public void onAnimationStart(Animator animator) {
                ValueAnimator ofFloat = ValueAnimator.ofFloat(new float[]{((float) KeyguardCameraView.this.mScreenHeight) / ((float) KeyguardCameraView.this.mScreenWidth), 1.0f});
                ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                        KeyguardCameraView.AnonymousClass7.this.lambda$onAnimationStart$0$KeyguardCameraView$7(valueAnimator);
                    }
                });
                ofFloat.setInterpolator(new PhysicBasedInterpolator(0.99f, 0.67f));
                ofFloat.setDuration(300);
                ofFloat.start();
                KeyguardCameraView.this.applyBlurRatio(1.0f);
                KeyguardCameraView.this.mBackgroundView.setAlpha(1.0f);
                KeyguardCameraView.this.mPreViewContainer.setAlpha(0.0f);
                KeyguardCameraView.this.setAlpha(1.0f);
            }

            /* access modifiers changed from: private */
            /* renamed from: lambda$onAnimationStart$0 */
            public /* synthetic */ void lambda$onAnimationStart$0$KeyguardCameraView$7(ValueAnimator valueAnimator) {
                float unused = KeyguardCameraView.this.mBackAnimAspectRatio = ((Float) valueAnimator.getAnimatedValue()).floatValue();
            }

            public void onAnimationEnd(Animator animator) {
                boolean unused = KeyguardCameraView.this.mIsBackAnimRunning = false;
                KeyguardCameraView.this.reset();
                if (KeyguardCameraView.this.mCallBack != null) {
                    KeyguardCameraView.this.mCallBack.onBackAnimationEnd();
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
                KeyguardCameraView.this.lambda$getBackIconAnim$13$KeyguardCameraView(valueAnimator);
            }
        });
        ValueAnimator ofFloat2 = ValueAnimator.ofFloat(new float[]{f3, f4});
        ofFloat2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                KeyguardCameraView.this.lambda$getBackIconAnim$14$KeyguardCameraView(valueAnimator);
            }
        });
        ValueAnimator ofFloat3 = ValueAnimator.ofFloat(new float[]{f5, f6});
        ofFloat3.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                KeyguardCameraView.this.lambda$getBackIconAnim$15$KeyguardCameraView(valueAnimator);
            }
        });
        ValueAnimator ofFloat4 = ValueAnimator.ofFloat(new float[]{f11, f12});
        ofFloat4.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                KeyguardCameraView.this.lambda$getBackIconAnim$16$KeyguardCameraView(valueAnimator);
            }
        });
        ValueAnimator ofFloat5 = ValueAnimator.ofFloat(new float[]{f9, f10});
        ofFloat5.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                KeyguardCameraView.this.lambda$getBackIconAnim$17$KeyguardCameraView(valueAnimator);
            }
        });
        ValueAnimator ofFloat6 = ValueAnimator.ofFloat(new float[]{f7, f8});
        ofFloat6.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                KeyguardCameraView.this.lambda$getBackIconAnim$18$KeyguardCameraView(valueAnimator);
            }
        });
        ValueAnimator ofFloat7 = ValueAnimator.ofFloat(new float[]{f13, f14});
        ofFloat7.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                KeyguardCameraView.this.lambda$getBackIconAnim$19$KeyguardCameraView(valueAnimator);
            }
        });
        animatorSet.playTogether(new Animator[]{ofFloat, ofFloat2, ofFloat3, ofFloat4, ofFloat5, ofFloat6, ofFloat7});
        return animatorSet;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$getBackIconAnim$13 */
    public /* synthetic */ void lambda$getBackIconAnim$13$KeyguardCameraView(ValueAnimator valueAnimator) {
        float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
        this.mIconCircleWidth = floatValue;
        this.mIconCircleHeight = floatValue * this.mBackAnimAspectRatio;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$getBackIconAnim$14 */
    public /* synthetic */ void lambda$getBackIconAnim$14$KeyguardCameraView(ValueAnimator valueAnimator) {
        float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
        this.mIconCircleCenterX = floatValue;
        this.mIconCenterX = floatValue;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$getBackIconAnim$15 */
    public /* synthetic */ void lambda$getBackIconAnim$15$KeyguardCameraView(ValueAnimator valueAnimator) {
        float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
        this.mIconCircleCenterY = floatValue;
        this.mIconCenterY = (floatValue + (this.mIconCircleHeight * 0.8f)) - (this.mIconCircleWidth / 2.0f);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$getBackIconAnim$16 */
    public /* synthetic */ void lambda$getBackIconAnim$16$KeyguardCameraView(ValueAnimator valueAnimator) {
        float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
        this.mIconCircleAlpha = floatValue;
        if (floatValue < 0.0f) {
            this.mIconCircleAlpha = 0.0f;
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$getBackIconAnim$17 */
    public /* synthetic */ void lambda$getBackIconAnim$17$KeyguardCameraView(ValueAnimator valueAnimator) {
        this.mIconAlpha = ((Float) valueAnimator.getAnimatedValue()).floatValue();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$getBackIconAnim$18 */
    public /* synthetic */ void lambda$getBackIconAnim$18$KeyguardCameraView(ValueAnimator valueAnimator) {
        this.mIconScale = ((Float) valueAnimator.getAnimatedValue()).floatValue();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$getBackIconAnim$19 */
    public /* synthetic */ void lambda$getBackIconAnim$19$KeyguardCameraView(ValueAnimator valueAnimator) {
        this.mBackgroundView.setAlpha(((Float) valueAnimator.getAnimatedValue()).floatValue());
        int i = this.mScreenHeight;
        int i2 = this.mScreenWidth;
        float f = ((float) i) / ((float) i2);
        float f2 = 1.0f;
        float f3 = ((((float) i) / ((float) i2)) - this.mBackAnimAspectRatio) / (f - 1.0f);
        float f4 = this.mPreViewInitRadius;
        this.mPreViewRadius = valFromPer(f3, f4, (this.mIconCircleWidth / 2.0f) + f4);
        float f5 = this.mIconInitCenterX;
        float f6 = this.mIconCenterX;
        if (f5 - f6 < 270.0f) {
            f2 = (f5 - f6) / 270.0f;
        }
        applyBlurRatio(f2);
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
        SurfaceControlCompat.setBlur(this.mLpChanged, getViewRootImpl(), f, 0);
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
                    Drawable drawable = null;
                    if (!KeyguardCameraView.this.mUserAuthenticatedSinceBoot) {
                        return null;
                    }
                    if (!PackageUtils.IS_VELA_CAMERA) {
                        Context access$1300 = KeyguardCameraView.this.mContext;
                        Bundle resultFromProvider = ContentProviderUtils.getResultFromProvider(access$1300, "content://" + PackageUtils.PACKAGE_NAME_CAMERA + ".splashProvider", "getCameraSplash", (String) null, (Bundle) null);
                        String valueOf = resultFromProvider != null ? String.valueOf(resultFromProvider.get("getCameraSplash")) : "";
                        if (!TextUtils.isEmpty(valueOf)) {
                            try {
                                drawable = ImageDecoder.decodeDrawable(ImageDecoder.createSource(KeyguardCameraView.this.mContext.getContentResolver(), Uri.parse(valueOf), KeyguardCameraView.this.mContext.getResources()), $$Lambda$KeyguardCameraView$8$wT9TMxtptpjuOCqjBWpG9gwqjpw.INSTANCE);
                            } catch (IOException e) {
                                Log.e("KeyguardCameraView", "updatePreViewBackground " + e.getMessage());
                            }
                        }
                    }
                    if (drawable == null) {
                        return PackageUtils.getDrawableFromPackage(KeyguardCameraView.this.mContext, PackageUtils.PACKAGE_NAME_CAMERA, MiuiKeyguardUtils.getCameraImageName(KeyguardCameraView.this.mContext, MiuiKeyguardUtils.isFullScreenGestureOpened(KeyguardCameraView.this.mContext)));
                    }
                    this.mIsProviderDrawable = true;
                    return drawable;
                }

                /* access modifiers changed from: protected */
                public void onPostExecute(Drawable drawable) {
                    if (drawable != null) {
                        if (this.mIsProviderDrawable || PackageUtils.IS_VELA_CAMERA) {
                            KeyguardCameraView.this.mPreView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        } else {
                            KeyguardCameraView.this.mPreView.setScaleType(ImageView.ScaleType.FIT_END);
                        }
                        KeyguardCameraView.this.mPreView.setImageDrawable(drawable);
                    } else if (PackageUtils.IS_VELA_CAMERA) {
                        KeyguardCameraView.this.mPreView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        KeyguardCameraView.this.mPreView.setImageResource(R.drawable.meitu_camera_preview);
                    } else {
                        KeyguardCameraView.this.mPreView.setScaleType(ImageView.ScaleType.FIT_END);
                        KeyguardCameraView.this.mPreView.setImageResource(R.drawable.camera_preview);
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

        public PhysicBasedInterpolator(float f, float f2) {
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
