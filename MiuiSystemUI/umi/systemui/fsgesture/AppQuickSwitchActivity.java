package com.android.systemui.fsgesture;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.RelativeLayout;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0017R$layout;

public class AppQuickSwitchActivity extends FsGestureDemoBaseActiivy {
    private FsGestureDemoTitleView fsGestureDemoTitleView;
    Handler handler = new Handler();
    private ScrollerLayout mCardContainer;
    private boolean mFinishGestureDetection;
    private FsGestureDemoSwipeView mFsGestureDemoSwipeView;
    private View mFsGestureView;
    private boolean mIsShowNavigationHandle;
    private boolean mIsStartGesture;
    private int mScreenHeight;
    private int mScreenWidth;
    Runnable mSwipeAnimationRunnable = new Runnable() {
        /* class com.android.systemui.fsgesture.AppQuickSwitchActivity.AnonymousClass4 */

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
    @Override // com.android.systemui.fsgesture.FsGestureDemoBaseActiivy
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(C0017R$layout.activity_app_quick);
        initView();
        initData();
    }

    private void initView() {
        this.fsGestureDemoTitleView = (FsGestureDemoTitleView) findViewById(C0015R$id.fsgesture_title_view);
        this.mCardContainer = (ScrollerLayout) findViewById(C0015R$id.card_container);
        this.mFsGestureView = findViewById(C0015R$id.fsg_nav_view);
        this.mFsGestureDemoSwipeView = (FsGestureDemoSwipeView) findViewById(C0015R$id.fsgesture_swipe_view);
        this.mNavigationHandle = GestureLineUtils.createAndaddNavigationHandle((RelativeLayout) this.fsGestureDemoTitleView.getParent());
        this.mScreenWidth = getScreenWidth(this);
        this.mScreenHeight = getScreenHeight(this);
        this.mCardContainer.setPivotX(((float) this.mScreenWidth) / 2.0f);
        this.mCardContainer.setPivotY(((float) this.mScreenHeight) / 2.0f);
        this.mVelocityThreshold = getResources().getDisplayMetrics().density * 350.0f;
    }

    private void initData() {
        this.fsGestureDemoTitleView.registerSkipEvent(new View.OnClickListener() {
            /* class com.android.systemui.fsgesture.AppQuickSwitchActivity.AnonymousClass1 */

            public void onClick(View view) {
                AppQuickSwitchActivity.this.finish();
            }
        });
        GestureTitleViewUtil.setMargin(this, this.fsGestureDemoTitleView);
        this.mFsGestureView.setOnTouchListener(new View.OnTouchListener() {
            /* class com.android.systemui.fsgesture.AppQuickSwitchActivity.AnonymousClass2 */

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
        int i = (int) (this.mXCur - this.mXDown);
        this.mTranslationX = i;
        this.mCardContainer.setTranslationX((float) i);
        this.mCardContainer.setTranslationY((float) ((int) ((this.mYCur - this.mYDown) * 0.18f)));
    }

    private void performHapticFeedbackIfNeeded(MotionEvent motionEvent) {
        if ((this.mXCur - this.mXDown > ((float) (-this.mScreenWidth)) / 3.5f && motionEvent.getRawX() - this.mXDown < ((float) (-this.mScreenWidth)) / 3.5f) || (this.mXCur - this.mXDown < ((float) this.mScreenWidth) / 3.5f && motionEvent.getRawX() - this.mXDown > ((float) this.mScreenWidth) / 3.5f)) {
            this.mCardContainer.performHapticFeedback(1);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x00ec  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void onNavigationHandle(android.view.MotionEvent r7) {
        /*
        // Method dump skipped, instructions count: 275
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.fsgesture.AppQuickSwitchActivity.onNavigationHandle(android.view.MotionEvent):void");
    }

    private void postFinishDelay() {
        this.handler.postDelayed(new Runnable() {
            /* class com.android.systemui.fsgesture.AppQuickSwitchActivity.AnonymousClass3 */

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
    @Override // com.android.systemui.fsgesture.FsGestureDemoBaseActiivy
    public void onResume() {
        super.onResume();
        FsgestureUtil.INSTANCE.wholeHideSystemBars(getWindow().getDecorView());
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
    @Override // com.android.systemui.fsgesture.FsGestureDemoBaseActiivy
    public void onStart() {
        super.onStart();
        startSwipeAnimationDelay();
    }

    /* access modifiers changed from: protected */
    public void onStop() {
        super.onStop();
        stopSwipeAnimation();
    }

    private static int getScreenWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    private static int getScreenHeight(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }
}
