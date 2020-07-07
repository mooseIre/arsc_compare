package com.android.systemui.fsgesture;

import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.RelativeLayout;
import com.android.systemui.Util;
import com.android.systemui.plugins.R;
import com.android.systemui.util.ScreenUtils;

public class AppQuickSwitchActivity extends FsGestureDemoBaseActiivy {
    private FsGestureDemoTitleView fsGestureDemoTitleView;
    Handler handler = new Handler();
    private ScrollerLayout mCardContainer;
    private boolean mFinishGestureDetection;
    /* access modifiers changed from: private */
    public FsGestureDemoSwipeView mFsGestureDemoSwipeView;
    private View mFsGestureView;
    private boolean mIsShowNavigationHandle;
    private boolean mIsStartGesture;
    private int mScreenHeight;
    private int mScreenWidth;
    Runnable mSwipeAnimationRunnable = new Runnable() {
        public void run() {
            int i = GestureLineUtils.isShowNavigationHandle(AppQuickSwitchActivity.this) ? 5 : 6;
            AppQuickSwitchActivity.this.mFsGestureDemoSwipeView.prepare(i);
            AppQuickSwitchActivity.this.mFsGestureDemoSwipeView.startAnimation(i);
        }
    };
    private int mTranslationX;
    private float mVelocityThreshold;
    private VelocityTracker mVelocityTracker;
    private float mXCur;
    private float mXDown;
    private float mYCur;
    private float mYDown;

    /* access modifiers changed from: protected */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        getWindow().addFlags(1024);
        setContentView(R.layout.activity_app_quick);
        initView();
        initData();
    }

    private void initView() {
        this.fsGestureDemoTitleView = (FsGestureDemoTitleView) findViewById(R.id.fsgesture_title_view);
        this.mCardContainer = (ScrollerLayout) findViewById(R.id.card_container);
        this.mFsGestureView = findViewById(R.id.fsg_nav_view);
        this.mFsGestureDemoSwipeView = (FsGestureDemoSwipeView) findViewById(R.id.fsgesture_swipe_view);
        this.mNavigationHandle = GestureLineUtils.createAndaddNavigationHandle((RelativeLayout) this.fsGestureDemoTitleView.getParent());
        this.mScreenWidth = ScreenUtils.getScreenWidth(this);
        this.mScreenHeight = ScreenUtils.getScreenHeight(this);
        this.mCardContainer.setPivotX(((float) this.mScreenWidth) / 2.0f);
        this.mCardContainer.setPivotY(((float) this.mScreenHeight) / 2.0f);
        this.mVelocityThreshold = getResources().getDisplayMetrics().density * 350.0f;
    }

    private void initData() {
        this.fsGestureDemoTitleView.registerSkipEvent(new View.OnClickListener() {
            public void onClick(View view) {
                AppQuickSwitchActivity.this.finish();
            }
        });
        GestureTitleViewUtil.setMargin(this, this.fsGestureDemoTitleView);
        this.mFsGestureView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View view, MotionEvent motionEvent) {
                AppQuickSwitchActivity.this.onNavigationHandle(motionEvent);
                return true;
            }
        });
    }

    private void initPosition() {
        this.mCardContainer.setScaleX(1.0f);
        this.mCardContainer.setScaleY(1.0f);
        this.mCardContainer.setTranslationX(0.0f);
        this.mCardContainer.setTranslationY(0.0f);
    }

    private void updatePosition() {
        float max = 1.0f - (Math.max(0.0f, Math.min((this.mYDown - this.mYCur) / ((float) this.mScreenHeight), 1.0f)) * 0.7f);
        this.mCardContainer.setScaleX(max);
        this.mCardContainer.setScaleY(max);
        this.mTranslationX = (int) (this.mXCur - this.mXDown);
        this.mCardContainer.setTranslationX((float) this.mTranslationX);
        this.mCardContainer.setTranslationY((float) ((int) ((this.mYCur - this.mYDown) * 0.18f)));
    }

    private void performHapticFeedbackIfNeeded(MotionEvent motionEvent) {
        if ((this.mXCur - this.mXDown > ((float) (-this.mScreenWidth)) / 3.5f && motionEvent.getRawX() - this.mXDown < ((float) (-this.mScreenWidth)) / 3.5f) || (this.mXCur - this.mXDown < ((float) this.mScreenWidth) / 3.5f && motionEvent.getRawX() - this.mXDown > ((float) this.mScreenWidth) / 3.5f)) {
            this.mCardContainer.performHapticFeedback(1);
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x00ee  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onNavigationHandle(android.view.MotionEvent r7) {
        /*
            r6 = this;
            r6.obtainVelocityTracker(r7)
            int r0 = r7.getAction()
            if (r0 == 0) goto L_0x00fa
            r1 = 1
            if (r0 == r1) goto L_0x006b
            r2 = 2
            if (r0 == r2) goto L_0x0014
            r7 = 3
            if (r0 == r7) goto L_0x006b
            goto L_0x0116
        L_0x0014:
            boolean r0 = r6.mIsStartGesture
            if (r0 == 0) goto L_0x001b
            r6.performHapticFeedbackIfNeeded(r7)
        L_0x001b:
            float r0 = r7.getRawX()
            r6.mXCur = r0
            float r7 = r7.getRawY()
            r6.mYCur = r7
            boolean r7 = r6.mIsShowNavigationHandle
            if (r7 != 0) goto L_0x0062
            boolean r7 = r6.mFinishGestureDetection
            if (r7 != 0) goto L_0x0062
            float r7 = r6.mXCur
            float r0 = r6.mXDown
            float r7 = r7 - r0
            double r2 = (double) r7
            float r7 = r6.mYCur
            float r0 = r6.mYDown
            float r7 = r7 - r0
            double r4 = (double) r7
            double r2 = java.lang.Math.hypot(r2, r4)
            r4 = 4622945017495814144(0x4028000000000000, double:12.0)
            int r7 = (r2 > r4 ? 1 : (r2 == r4 ? 0 : -1))
            if (r7 <= 0) goto L_0x0062
            r6.mFinishGestureDetection = r1
            float r7 = r6.mYCur
            float r0 = r6.mYDown
            float r7 = r7 - r0
            float r7 = java.lang.Math.abs(r7)
            r0 = 1073741824(0x40000000, float:2.0)
            float r2 = r6.mXCur
            float r3 = r6.mXDown
            float r2 = r2 - r3
            float r2 = java.lang.Math.abs(r2)
            float r2 = r2 * r0
            int r7 = (r7 > r2 ? 1 : (r7 == r2 ? 0 : -1))
            if (r7 <= 0) goto L_0x0062
            r6.mIsStartGesture = r1
        L_0x0062:
            boolean r7 = r6.mIsStartGesture
            if (r7 == 0) goto L_0x0116
            r6.updatePosition()
            goto L_0x0116
        L_0x006b:
            boolean r7 = r6.mIsStartGesture
            if (r7 == 0) goto L_0x00f6
            android.view.VelocityTracker r7 = r6.mVelocityTracker
            r0 = 1000(0x3e8, float:1.401E-42)
            android.view.ViewConfiguration r1 = android.view.ViewConfiguration.get(r6)
            int r1 = r1.getScaledMaximumFlingVelocity()
            float r1 = (float) r1
            r7.computeCurrentVelocity(r0, r1)
            android.view.VelocityTracker r7 = r6.mVelocityTracker
            float r7 = r7.getXVelocity()
            int r0 = r6.mTranslationX
            float r1 = (float) r0
            int r2 = r6.mScreenWidth
            int r2 = -r2
            float r2 = (float) r2
            r3 = 1080033280(0x40600000, float:3.5)
            float r2 = r2 / r3
            int r1 = (r1 > r2 ? 1 : (r1 == r2 ? 0 : -1))
            r2 = 0
            if (r1 < 0) goto L_0x00be
            if (r0 >= 0) goto L_0x009e
            float r0 = r6.mVelocityThreshold
            float r0 = -r0
            int r0 = (r7 > r0 ? 1 : (r7 == r0 ? 0 : -1))
            if (r0 >= 0) goto L_0x009e
            goto L_0x00be
        L_0x009e:
            int r0 = r6.mTranslationX
            float r1 = (float) r0
            int r4 = r6.mScreenWidth
            float r4 = (float) r4
            float r4 = r4 / r3
            int r1 = (r1 > r4 ? 1 : (r1 == r4 ? 0 : -1))
            if (r1 > 0) goto L_0x00b4
            if (r0 <= 0) goto L_0x00b2
            float r0 = r6.mVelocityThreshold
            int r7 = (r7 > r0 ? 1 : (r7 == r0 ? 0 : -1))
            if (r7 <= 0) goto L_0x00b2
            goto L_0x00b4
        L_0x00b2:
            r7 = r2
            goto L_0x00c9
        L_0x00b4:
            int r7 = r6.mScreenWidth
            com.android.systemui.fsgesture.ScrollerLayout r0 = r6.mCardContainer
            int r0 = r0.getHorizontalGap()
            int r7 = r7 + r0
            goto L_0x00c8
        L_0x00be:
            int r7 = r6.mScreenWidth
            com.android.systemui.fsgesture.ScrollerLayout r0 = r6.mCardContainer
            int r0 = r0.getHorizontalGap()
            int r7 = r7 + r0
            int r7 = -r7
        L_0x00c8:
            float r7 = (float) r7
        L_0x00c9:
            com.android.systemui.fsgesture.ScrollerLayout r0 = r6.mCardContainer
            android.view.ViewPropertyAnimator r0 = r0.animate()
            android.view.ViewPropertyAnimator r0 = r0.translationX(r7)
            android.view.ViewPropertyAnimator r0 = r0.translationY(r2)
            r1 = 1065353216(0x3f800000, float:1.0)
            android.view.ViewPropertyAnimator r0 = r0.scaleX(r1)
            android.view.ViewPropertyAnimator r0 = r0.scaleY(r1)
            r3 = 200(0xc8, double:9.9E-322)
            android.view.ViewPropertyAnimator r0 = r0.setDuration(r3)
            r0.start()
            int r7 = (r7 > r2 ? 1 : (r7 == r2 ? 0 : -1))
            if (r7 == 0) goto L_0x00f6
            com.android.systemui.fsgesture.FsGestureDemoTitleView r7 = r6.fsGestureDemoTitleView
            r7.notifyFinish()
            r6.postFinishDelay()
        L_0x00f6:
            r6.releaseVelocityTracker()
            goto L_0x0116
        L_0x00fa:
            float r0 = r7.getRawX()
            r6.mXDown = r0
            float r7 = r7.getRawY()
            r6.mYDown = r7
            boolean r7 = com.android.systemui.fsgesture.GestureLineUtils.isShowNavigationHandle(r6)
            r6.mIsShowNavigationHandle = r7
            r7 = 0
            r6.mFinishGestureDetection = r7
            boolean r7 = r6.mIsShowNavigationHandle
            r6.mIsStartGesture = r7
            r6.initPosition()
        L_0x0116:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.fsgesture.AppQuickSwitchActivity.onNavigationHandle(android.view.MotionEvent):void");
    }

    private void postFinishDelay() {
        this.handler.postDelayed(new Runnable() {
            public void run() {
                AppQuickSwitchActivity.this.finish();
            }
        }, 500);
    }

    private void releaseVelocityTracker() {
        VelocityTracker velocityTracker = this.mVelocityTracker;
        if (velocityTracker != null) {
            velocityTracker.clear();
            this.mVelocityTracker.recycle();
            this.mVelocityTracker = null;
        }
    }

    private void obtainVelocityTracker(MotionEvent motionEvent) {
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        }
        this.mVelocityTracker.addMovement(motionEvent);
    }

    /* access modifiers changed from: protected */
    public void onResume() {
        super.onResume();
        Util.wholeHideSystemBars(getWindow().getDecorView());
        updateTitle();
    }

    private void updateTitle() {
        this.fsGestureDemoTitleView.prepareTitleView(GestureLineUtils.isShowNavigationHandle(this) ? 5 : 6);
    }

    private void startSwipeAnimationDelay() {
        this.handler.postDelayed(this.mSwipeAnimationRunnable, 500);
    }

    private void stopSwipeAnimation() {
        this.handler.removeCallbacks(this.mSwipeAnimationRunnable);
    }

    /* access modifiers changed from: protected */
    public void onStart() {
        super.onStart();
        startSwipeAnimationDelay();
    }

    /* access modifiers changed from: protected */
    public void onStop() {
        super.onStop();
        stopSwipeAnimation();
    }
}
