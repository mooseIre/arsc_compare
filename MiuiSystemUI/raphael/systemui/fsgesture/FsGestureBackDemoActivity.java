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
import com.android.systemui.C0007R$anim;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0017R$layout;
import java.util.Locale;

public class FsGestureBackDemoActivity extends FsGestureDemoBaseActiivy {
    private static Handler sHandler = new Handler();
    private String demoType;
    private boolean isFromPro;
    private GestureBackArrowView mBackArrowView;
    private View mBgView;
    private View.OnTouchListener mDemoActivityTouchListener = new View.OnTouchListener() {
        /* class com.android.systemui.fsgesture.FsGestureBackDemoActivity.AnonymousClass1 */

        /* JADX WARNING: Code restructure failed: missing block: B:5:0x0015, code lost:
            if (r6 != 3) goto L_0x0144;
         */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean onTouch(android.view.View r6, android.view.MotionEvent r7) {
            /*
            // Method dump skipped, instructions count: 325
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.fsgesture.FsGestureBackDemoActivity.AnonymousClass1.onTouch(android.view.View, android.view.MotionEvent):boolean");
        }
    };
    private View mDemoActivityView;
    private Matrix mDemoActivityViewMatrix = new Matrix();
    private int mDisplayHeight;
    private int mDisplayWidth;
    private float mDownX;
    private float mDownY;
    private FsGestureDemoSwipeView mFsGestureDemoSwipeView;
    private FsGestureDemoTitleView mFsGestureDemoTitleView;
    private int mGestureStatus;
    private float mOffsetX;
    private int mStatus = 0;

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.fsgesture.FsGestureDemoBaseActiivy
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(C0017R$layout.fs_gesture_back_demo);
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
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(C0015R$id.root_view);
        this.mBgView = findViewById(C0015R$id.bg_view);
        View findViewById = findViewById(C0015R$id.demo_activity);
        this.mDemoActivityView = findViewById;
        findViewById.setOnTouchListener(this.mDemoActivityTouchListener);
        this.mFsGestureDemoTitleView = (FsGestureDemoTitleView) findViewById(C0015R$id.fsgesture_title_view);
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
            /* class com.android.systemui.fsgesture.FsGestureBackDemoActivity.AnonymousClass2 */

            public void onClick(View view) {
                FsGestureBackDemoActivity.this.finish();
            }
        });
        GestureTitleViewUtil.setMargin(this, this.mFsGestureDemoTitleView);
        this.mFsGestureDemoSwipeView = (FsGestureDemoSwipeView) findViewById(C0015R$id.fsgesture_swipe_view);
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
    /* access modifiers changed from: public */
    private void startSwipeViewAnimation(final int i) {
        sHandler.postDelayed(new Runnable() {
            /* class com.android.systemui.fsgesture.FsGestureBackDemoActivity.AnonymousClass3 */

            public void run() {
                FsGestureBackDemoActivity.this.mFsGestureDemoSwipeView.prepare(i);
                FsGestureBackDemoActivity.this.mFsGestureDemoSwipeView.startAnimation(i);
            }
        }, 500);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void finishGesture(final boolean z) {
        if (this.mDemoActivityView != null) {
            ValueAnimator ofFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
            ofFloat.setDuration(200L);
            ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                /* class com.android.systemui.fsgesture.FsGestureBackDemoActivity.AnonymousClass4 */

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
                /* class com.android.systemui.fsgesture.FsGestureBackDemoActivity.AnonymousClass5 */

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
                    FsGestureBackDemoActivity.this.mGestureStatus = 3;
                }
            });
            ofFloat.start();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void showBackAnimation() {
        this.mBgView.setVisibility(0);
        Animation loadAnimation = AnimationUtils.loadAnimation(this, C0007R$anim.activity_close_enter);
        Animation loadAnimation2 = AnimationUtils.loadAnimation(this, C0007R$anim.activity_close_exit);
        loadAnimation.setAnimationListener(new Animation.AnimationListener() {
            /* class com.android.systemui.fsgesture.FsGestureBackDemoActivity.AnonymousClass6 */

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
    /* access modifiers changed from: public */
    private void onGestureFinish() {
        this.mFsGestureDemoTitleView.notifyFinish();
        this.mDemoActivityView.setVisibility(8);
        finishGestureBack();
    }

    private void finishGestureBack() {
        sHandler.postDelayed(new Runnable() {
            /* class com.android.systemui.fsgesture.FsGestureBackDemoActivity.AnonymousClass7 */

            public void run() {
                if (FsGestureBackDemoActivity.this.mStatus == 1) {
                    Intent intent = new Intent(FsGestureBackDemoActivity.this, FsGestureBackDemoActivity.class);
                    intent.putExtra("DEMO_TYPE", FsGestureBackDemoActivity.this.demoType);
                    intent.putExtra("DEMO_STEP", 2);
                    intent.putExtra("IS_FROM_PROVISION", FsGestureBackDemoActivity.this.isFromPro);
                    FsGestureBackDemoActivity.this.startActivity(intent);
                    FsGestureBackDemoActivity.this.overridePendingTransition(C0007R$anim.activity_start_enter, C0007R$anim.activity_start_exit);
                } else if ("DEMO_FULLY_SHOW".equals(FsGestureBackDemoActivity.this.demoType)) {
                    Intent intent2 = new Intent(FsGestureBackDemoActivity.this, DemoFinishAct.class);
                    intent2.putExtra("DEMO_TYPE", FsGestureBackDemoActivity.this.demoType);
                    intent2.putExtra("IS_FROM_PROVISION", FsGestureBackDemoActivity.this.isFromPro);
                    FsGestureBackDemoActivity.this.startActivity(intent2);
                    FsGestureBackDemoActivity.this.overridePendingTransition(C0007R$anim.activity_start_enter, C0007R$anim.activity_start_exit);
                }
                FsGestureBackDemoActivity.this.finish();
            }
        }, 500);
    }
}
