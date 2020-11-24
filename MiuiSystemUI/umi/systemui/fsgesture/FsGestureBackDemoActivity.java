package com.android.systemui.fsgesture;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import com.android.systemui.C0004R$anim;
import com.android.systemui.C0012R$id;
import com.android.systemui.C0014R$layout;
import java.util.Locale;

public class FsGestureBackDemoActivity extends FsGestureDemoBaseActiivy {
    private static Handler sHandler = new Handler();
    /* access modifiers changed from: private */
    public String demoType;
    /* access modifiers changed from: private */
    public boolean isFromPro;
    /* access modifiers changed from: private */
    public GestureBackArrowView mBackArrowView;
    private View mBgView;
    private View.OnTouchListener mDemoActivityTouchListener = new View.OnTouchListener() {
        /* JADX WARNING: Code restructure failed: missing block: B:5:0x0015, code lost:
            if (r6 != 3) goto L_0x0144;
         */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean onTouch(android.view.View r6, android.view.MotionEvent r7) {
            /*
                r5 = this;
                int r6 = r7.getActionMasked()
                float r0 = r7.getX()
                float r1 = r7.getY()
                r2 = 2
                r3 = 1
                if (r6 == 0) goto L_0x00d9
                if (r6 == r3) goto L_0x0088
                if (r6 == r2) goto L_0x0019
                r1 = 3
                if (r6 == r1) goto L_0x0088
                goto L_0x0144
            L_0x0019:
                com.android.systemui.fsgesture.FsGestureBackDemoActivity r6 = com.android.systemui.fsgesture.FsGestureBackDemoActivity.this
                int r6 = r6.mGestureStatus
                if (r6 != 0) goto L_0x0023
                goto L_0x0144
            L_0x0023:
                com.android.systemui.fsgesture.FsGestureBackDemoActivity r6 = com.android.systemui.fsgesture.FsGestureBackDemoActivity.this
                int r4 = r6.mStatus
                if (r4 != r3) goto L_0x0033
                com.android.systemui.fsgesture.FsGestureBackDemoActivity r4 = com.android.systemui.fsgesture.FsGestureBackDemoActivity.this
                float r4 = r4.mDownX
                float r0 = r0 - r4
                goto L_0x003b
            L_0x0033:
                com.android.systemui.fsgesture.FsGestureBackDemoActivity r4 = com.android.systemui.fsgesture.FsGestureBackDemoActivity.this
                float r4 = r4.mDownX
                float r0 = r4 - r0
            L_0x003b:
                float unused = r6.mOffsetX = r0
                com.android.systemui.fsgesture.FsGestureBackDemoActivity r6 = com.android.systemui.fsgesture.FsGestureBackDemoActivity.this
                float r6 = r6.mDownY
                float r1 = r1 - r6
                java.lang.Math.abs(r1)
                r6 = 1101004800(0x41a00000, float:20.0)
                int r6 = (r0 > r6 ? 1 : (r0 == r6 ? 0 : -1))
                if (r6 < 0) goto L_0x0144
                com.android.systemui.fsgesture.FsGestureBackDemoActivity r6 = com.android.systemui.fsgesture.FsGestureBackDemoActivity.this
                int r6 = r6.mGestureStatus
                if (r6 != r3) goto L_0x005b
                com.android.systemui.fsgesture.FsGestureBackDemoActivity r6 = com.android.systemui.fsgesture.FsGestureBackDemoActivity.this
                int unused = r6.mGestureStatus = r2
            L_0x005b:
                com.android.systemui.fsgesture.FsGestureBackDemoActivity r6 = com.android.systemui.fsgesture.FsGestureBackDemoActivity.this
                com.android.systemui.fsgesture.GestureBackArrowView r6 = r6.mBackArrowView
                r6.onActionMove(r0)
                long r1 = r7.getEventTime()
                long r6 = r7.getDownTime()
                long r1 = r1 - r6
                int r6 = (int) r1
                float r6 = (float) r6
                float r6 = r0 / r6
                int r6 = (int) r6
                com.android.systemui.fsgesture.FsGestureBackDemoActivity r5 = com.android.systemui.fsgesture.FsGestureBackDemoActivity.this
                com.android.systemui.fsgesture.GestureBackArrowView r5 = r5.mBackArrowView
                boolean r6 = com.android.systemui.fsgesture.GesturesBackController.isFinished(r0, r6)
                if (r6 == 0) goto L_0x0081
                com.android.systemui.fsgesture.GestureBackArrowView$ReadyState r6 = com.android.systemui.fsgesture.GestureBackArrowView.ReadyState.READY_STATE_BACK
                goto L_0x0083
            L_0x0081:
                com.android.systemui.fsgesture.GestureBackArrowView$ReadyState r6 = com.android.systemui.fsgesture.GestureBackArrowView.ReadyState.READY_STATE_NONE
            L_0x0083:
                r5.setReadyFinish(r6)
                goto L_0x0144
            L_0x0088:
                com.android.systemui.fsgesture.FsGestureBackDemoActivity r6 = com.android.systemui.fsgesture.FsGestureBackDemoActivity.this
                int r6 = r6.mGestureStatus
                if (r6 != 0) goto L_0x0092
                goto L_0x0144
            L_0x0092:
                com.android.systemui.fsgesture.FsGestureBackDemoActivity r6 = com.android.systemui.fsgesture.FsGestureBackDemoActivity.this
                int r6 = r6.mStatus
                if (r6 != r3) goto L_0x00a2
                com.android.systemui.fsgesture.FsGestureBackDemoActivity r6 = com.android.systemui.fsgesture.FsGestureBackDemoActivity.this
                float r6 = r6.mDownX
                float r0 = r0 - r6
                goto L_0x00aa
            L_0x00a2:
                com.android.systemui.fsgesture.FsGestureBackDemoActivity r6 = com.android.systemui.fsgesture.FsGestureBackDemoActivity.this
                float r6 = r6.mDownX
                float r0 = r6 - r0
            L_0x00aa:
                com.android.systemui.fsgesture.FsGestureBackDemoActivity r6 = com.android.systemui.fsgesture.FsGestureBackDemoActivity.this
                com.android.systemui.fsgesture.GestureBackArrowView r6 = r6.mBackArrowView
                float r0 = com.android.systemui.fsgesture.GesturesBackController.convertOffset(r0)
                r1 = 0
                r6.onActionUp(r0, r1)
                long r0 = r7.getEventTime()
                long r6 = r7.getDownTime()
                long r0 = r0 - r6
                int r6 = (int) r0
                com.android.systemui.fsgesture.FsGestureBackDemoActivity r7 = com.android.systemui.fsgesture.FsGestureBackDemoActivity.this
                float r7 = r7.mOffsetX
                float r6 = (float) r6
                float r7 = r7 / r6
                int r6 = (int) r7
                com.android.systemui.fsgesture.FsGestureBackDemoActivity r5 = com.android.systemui.fsgesture.FsGestureBackDemoActivity.this
                float r7 = r5.mOffsetX
                boolean r6 = com.android.systemui.fsgesture.GesturesBackController.isFinished(r7, r6)
                r5.finishGesture(r6)
                goto L_0x0144
            L_0x00d9:
                com.android.systemui.fsgesture.FsGestureBackDemoActivity r6 = com.android.systemui.fsgesture.FsGestureBackDemoActivity.this
                int r6 = r6.mGestureStatus
                r7 = 0
                if (r6 != r2) goto L_0x00e3
                return r7
            L_0x00e3:
                com.android.systemui.fsgesture.FsGestureBackDemoActivity r6 = com.android.systemui.fsgesture.FsGestureBackDemoActivity.this
                float unused = r6.mDownX = r0
                com.android.systemui.fsgesture.FsGestureBackDemoActivity r6 = com.android.systemui.fsgesture.FsGestureBackDemoActivity.this
                float unused = r6.mDownY = r1
                com.android.systemui.fsgesture.FsGestureBackDemoActivity r6 = com.android.systemui.fsgesture.FsGestureBackDemoActivity.this
                int r6 = r6.mStatus
                if (r6 != r3) goto L_0x00fb
                r6 = 1116471296(0x428c0000, float:70.0)
                int r6 = (r0 > r6 ? 1 : (r0 == r6 ? 0 : -1))
                if (r6 < 0) goto L_0x0110
            L_0x00fb:
                com.android.systemui.fsgesture.FsGestureBackDemoActivity r6 = com.android.systemui.fsgesture.FsGestureBackDemoActivity.this
                int r6 = r6.mStatus
                if (r6 != r2) goto L_0x013f
                com.android.systemui.fsgesture.FsGestureBackDemoActivity r6 = com.android.systemui.fsgesture.FsGestureBackDemoActivity.this
                int r6 = r6.mDisplayWidth
                int r6 = r6 + -70
                float r6 = (float) r6
                int r6 = (r0 > r6 ? 1 : (r0 == r6 ? 0 : -1))
                if (r6 <= 0) goto L_0x013f
            L_0x0110:
                com.android.systemui.fsgesture.FsGestureBackDemoActivity r6 = com.android.systemui.fsgesture.FsGestureBackDemoActivity.this
                int r6 = r6.mDisplayHeight
                int r6 = r6 / 5
                int r6 = r6 * r2
                float r6 = (float) r6
                int r6 = (r1 > r6 ? 1 : (r1 == r6 ? 0 : -1))
                if (r6 <= 0) goto L_0x013f
                com.android.systemui.fsgesture.FsGestureBackDemoActivity r6 = com.android.systemui.fsgesture.FsGestureBackDemoActivity.this
                int unused = r6.mGestureStatus = r3
                com.android.systemui.fsgesture.FsGestureBackDemoActivity r6 = com.android.systemui.fsgesture.FsGestureBackDemoActivity.this
                com.android.systemui.fsgesture.GestureBackArrowView r6 = r6.mBackArrowView
                com.android.systemui.fsgesture.FsGestureBackDemoActivity r7 = com.android.systemui.fsgesture.FsGestureBackDemoActivity.this
                float r7 = r7.mDownY
                r0 = 0
                r1 = -1082130432(0xffffffffbf800000, float:-1.0)
                r6.onActionDown(r7, r0, r1)
                com.android.systemui.fsgesture.FsGestureBackDemoActivity r5 = com.android.systemui.fsgesture.FsGestureBackDemoActivity.this
                com.android.systemui.fsgesture.FsGestureDemoSwipeView r5 = r5.mFsGestureDemoSwipeView
                r5.cancelAnimation()
                goto L_0x0144
            L_0x013f:
                com.android.systemui.fsgesture.FsGestureBackDemoActivity r5 = com.android.systemui.fsgesture.FsGestureBackDemoActivity.this
                int unused = r5.mGestureStatus = r7
            L_0x0144:
                return r3
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.fsgesture.FsGestureBackDemoActivity.AnonymousClass1.onTouch(android.view.View, android.view.MotionEvent):boolean");
        }
    };
    /* access modifiers changed from: private */
    public View mDemoActivityView;
    /* access modifiers changed from: private */
    public Matrix mDemoActivityViewMatrix = new Matrix();
    /* access modifiers changed from: private */
    public int mDisplayHeight;
    /* access modifiers changed from: private */
    public int mDisplayWidth;
    /* access modifiers changed from: private */
    public float mDownX;
    /* access modifiers changed from: private */
    public float mDownY;
    /* access modifiers changed from: private */
    public FsGestureDemoSwipeView mFsGestureDemoSwipeView;
    private FsGestureDemoTitleView mFsGestureDemoTitleView;
    /* access modifiers changed from: private */
    public int mGestureStatus;
    /* access modifiers changed from: private */
    public float mOffsetX;
    /* access modifiers changed from: private */
    public int mStatus = 0;

    /* access modifiers changed from: protected */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(C0014R$layout.fs_gesture_back_demo);
        getWindow().addFlags(1024);
        FsgestureUtil.INSTANCE.hideSystemBars(getWindow().getDecorView());
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager) getSystemService("window")).getDefaultDisplay().getRealMetrics(displayMetrics);
        this.mDisplayWidth = displayMetrics.widthPixels;
        this.mDisplayHeight = displayMetrics.heightPixels;
        Intent intent = getIntent();
        String stringExtra = intent.getStringExtra("DEMO_TYPE");
        this.demoType = stringExtra;
        if (stringExtra == null) {
            stringExtra = "DEMO_FULLY_SHOW";
        }
        this.demoType = stringExtra;
        this.mStatus = intent.getIntExtra("DEMO_STEP", 1);
        this.isFromPro = intent.getBooleanExtra("IS_FROM_PROVISION", false);
        initView();
        this.mNavigationHandle = GestureLineUtils.createAndaddNavigationHandle((RelativeLayout) this.mDemoActivityView.getParent());
    }

    private void initView() {
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(C0012R$id.root_view);
        this.mBgView = findViewById(C0012R$id.bg_view);
        View findViewById = findViewById(C0012R$id.demo_activity);
        this.mDemoActivityView = findViewById;
        findViewById.setOnTouchListener(this.mDemoActivityTouchListener);
        this.mFsGestureDemoTitleView = (FsGestureDemoTitleView) findViewById(C0012R$id.fsgesture_title_view);
        if (TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) == 1) {
            this.mFsGestureDemoTitleView.setRTLParams();
        }
        int i = 0;
        if (this.mStatus == 1) {
            this.mFsGestureDemoTitleView.prepareTitleView(0);
        } else {
            this.mFsGestureDemoTitleView.prepareTitleView(1);
        }
        this.mFsGestureDemoTitleView.registerSkipEvent(new View.OnClickListener() {
            public void onClick(View view) {
                FsGestureBackDemoActivity.this.finish();
            }
        });
        GestureTitleViewUtil.setMargin(this, this.mFsGestureDemoTitleView);
        this.mFsGestureDemoSwipeView = (FsGestureDemoSwipeView) findViewById(C0012R$id.fsgesture_swipe_view);
        if (this.mStatus == 1) {
            startSwipeViewAnimation(0);
        } else {
            startSwipeViewAnimation(1);
        }
        if (this.mStatus != 1) {
            i = 1;
        }
        GestureBackArrowView gestureBackArrowView = new GestureBackArrowView(this, i);
        this.mBackArrowView = gestureBackArrowView;
        gestureBackArrowView.setDisplayWidth(this.mDisplayWidth);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(this.mDisplayWidth, this.mDisplayHeight);
        layoutParams.addRule(12);
        layoutParams.addRule(this.mStatus == 1 ? 9 : 11);
        relativeLayout.addView(this.mBackArrowView, layoutParams);
        relativeLayout.bringChildToFront(this.mBackArrowView);
    }

    /* access modifiers changed from: private */
    public void startSwipeViewAnimation(final int i) {
        sHandler.postDelayed(new Runnable() {
            public void run() {
                FsGestureBackDemoActivity.this.mFsGestureDemoSwipeView.prepare(i);
                FsGestureBackDemoActivity.this.mFsGestureDemoSwipeView.startAnimation(i);
            }
        }, 500);
    }

    /* access modifiers changed from: private */
    public void finishGesture(final boolean z) {
        if (this.mDemoActivityView != null) {
            ValueAnimator ofFloat = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
            ofFloat.setDuration(200);
            ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    float animatedFraction = valueAnimator.getAnimatedFraction();
                    float[] fArr = new float[9];
                    FsGestureBackDemoActivity.this.mDemoActivityViewMatrix.getValues(fArr);
                    float f = fArr[0];
                    float f2 = fArr[4];
                    float f3 = fArr[2];
                    float f4 = 1.0f - animatedFraction;
                    FsGestureBackDemoActivity.this.mDemoActivityViewMatrix.reset();
                    FsGestureBackDemoActivity.this.mDemoActivityViewMatrix.setScale(((1.0f - f) * animatedFraction) + f, ((1.0f - f2) * animatedFraction) + f2);
                    FsGestureBackDemoActivity.this.mDemoActivityViewMatrix.postTranslate(f3 * f4, fArr[5] * f4);
                    FsGestureBackDemoActivity.this.mDemoActivityView.setAnimationMatrix(FsGestureBackDemoActivity.this.mDemoActivityViewMatrix);
                }
            });
            ofFloat.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animator) {
                    if (z) {
                        FsGestureBackDemoActivity.this.showBackAnimation();
                        return;
                    }
                    if (FsGestureBackDemoActivity.this.mStatus == 1) {
                        FsGestureBackDemoActivity.this.startSwipeViewAnimation(0);
                    } else if (FsGestureBackDemoActivity.this.mStatus == 2) {
                        FsGestureBackDemoActivity.this.startSwipeViewAnimation(1);
                    }
                    int unused = FsGestureBackDemoActivity.this.mGestureStatus = 3;
                }
            });
            ofFloat.start();
        }
    }

    /* access modifiers changed from: private */
    public void showBackAnimation() {
        this.mBgView.setVisibility(0);
        Animation loadAnimation = AnimationUtils.loadAnimation(this, C0004R$anim.activity_close_enter);
        Animation loadAnimation2 = AnimationUtils.loadAnimation(this, C0004R$anim.activity_close_exit);
        loadAnimation.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationRepeat(Animation animation) {
            }

            public void onAnimationStart(Animation animation) {
            }

            public void onAnimationEnd(Animation animation) {
                FsGestureBackDemoActivity.this.onGestureFinish();
            }
        });
        this.mBgView.startAnimation(loadAnimation);
        this.mDemoActivityView.startAnimation(loadAnimation2);
    }

    /* access modifiers changed from: private */
    public void onGestureFinish() {
        this.mFsGestureDemoTitleView.notifyFinish();
        this.mDemoActivityView.setVisibility(8);
        finishGestureBack();
    }

    private void finishGestureBack() {
        sHandler.postDelayed(new Runnable() {
            public void run() {
                if (FsGestureBackDemoActivity.this.mStatus == 1) {
                    Intent intent = new Intent(FsGestureBackDemoActivity.this, FsGestureBackDemoActivity.class);
                    intent.putExtra("DEMO_TYPE", FsGestureBackDemoActivity.this.demoType);
                    intent.putExtra("DEMO_STEP", 2);
                    intent.putExtra("IS_FROM_PROVISION", FsGestureBackDemoActivity.this.isFromPro);
                    FsGestureBackDemoActivity.this.startActivity(intent);
                    FsGestureBackDemoActivity.this.overridePendingTransition(C0004R$anim.activity_start_enter, C0004R$anim.activity_start_exit);
                } else if ("DEMO_FULLY_SHOW".equals(FsGestureBackDemoActivity.this.demoType)) {
                    Intent intent2 = new Intent(FsGestureBackDemoActivity.this, DemoFinishAct.class);
                    intent2.putExtra("DEMO_TYPE", FsGestureBackDemoActivity.this.demoType);
                    intent2.putExtra("IS_FROM_PROVISION", FsGestureBackDemoActivity.this.isFromPro);
                    FsGestureBackDemoActivity.this.startActivity(intent2);
                    FsGestureBackDemoActivity.this.overridePendingTransition(C0004R$anim.activity_start_enter, C0004R$anim.activity_start_exit);
                }
                FsGestureBackDemoActivity.this.finish();
            }
        }, 500);
    }
}
