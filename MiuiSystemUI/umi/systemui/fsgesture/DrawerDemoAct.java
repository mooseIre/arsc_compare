package com.android.systemui.fsgesture;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0017R$layout;
import miui.os.Build;

public class DrawerDemoAct extends FsGestureDemoBaseActiivy {
    /* access modifiers changed from: private */
    public static final boolean IS_DEBUG = Build.IS_ALPHA_BUILD;
    public static final String TAG = DrawerDemoAct.class.getSimpleName();
    /* access modifiers changed from: private */
    public ImageView drawerImg;
    /* access modifiers changed from: private */
    public FsGestureDemoSwipeView fsGestureDemoSwipeView;
    /* access modifiers changed from: private */
    public FsGestureDemoTitleView fsGestureDemoTitleView;
    Handler handler = new Handler();
    /* access modifiers changed from: private */
    public int initTranslateWidht;
    /* access modifiers changed from: private */
    public int maxTranslateWidth;
    /* access modifiers changed from: private */
    public View shelterView;

    /* access modifiers changed from: protected */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(C0017R$layout.drawer_demo_layout);
        this.drawerImg = (ImageView) findViewById(C0015R$id.drawer_img);
        View findViewById = findViewById(C0015R$id.shelter_view);
        this.shelterView = findViewById;
        findViewById.setOnTouchListener(new View.OnTouchListener() {
            /* JADX WARNING: Code restructure failed: missing block: B:5:0x0011, code lost:
                if (r6 != 3) goto L_0x00ec;
             */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public boolean onTouch(android.view.View r6, android.view.MotionEvent r7) {
                /*
                    r5 = this;
                    int r6 = r7.getAction()
                    float r7 = r7.getRawX()
                    r0 = 1
                    if (r6 == 0) goto L_0x00cd
                    r1 = 3
                    r2 = 2
                    if (r6 == r0) goto L_0x0044
                    if (r6 == r2) goto L_0x0015
                    if (r6 == r1) goto L_0x0044
                    goto L_0x00ec
                L_0x0015:
                    com.android.systemui.fsgesture.DrawerDemoAct r6 = com.android.systemui.fsgesture.DrawerDemoAct.this
                    android.widget.ImageView r6 = r6.drawerImg
                    com.android.systemui.fsgesture.DrawerDemoAct r1 = com.android.systemui.fsgesture.DrawerDemoAct.this
                    android.widget.ImageView r1 = r1.drawerImg
                    int r1 = r1.getWidth()
                    float r1 = (float) r1
                    int r1 = (r7 > r1 ? 1 : (r7 == r1 ? 0 : -1))
                    if (r1 < 0) goto L_0x0032
                    com.android.systemui.fsgesture.DrawerDemoAct r5 = com.android.systemui.fsgesture.DrawerDemoAct.this
                    int r5 = r5.maxTranslateWidth
                    float r5 = (float) r5
                    goto L_0x003f
                L_0x0032:
                    com.android.systemui.fsgesture.DrawerDemoAct r5 = com.android.systemui.fsgesture.DrawerDemoAct.this
                    android.widget.ImageView r5 = r5.drawerImg
                    int r5 = r5.getWidth()
                    float r5 = (float) r5
                    float r5 = r7 - r5
                L_0x003f:
                    r6.setTranslationX(r5)
                    goto L_0x00ec
                L_0x0044:
                    com.android.systemui.fsgesture.DrawerDemoAct r6 = com.android.systemui.fsgesture.DrawerDemoAct.this
                    android.view.View r6 = r6.shelterView
                    android.view.ViewGroup$LayoutParams r6 = r6.getLayoutParams()
                    com.android.systemui.fsgesture.DrawerDemoAct r3 = com.android.systemui.fsgesture.DrawerDemoAct.this
                    android.content.res.Resources r3 = r3.getResources()
                    int r4 = com.android.systemui.C0012R$dimen.fsgesture_shelter_width
                    int r3 = r3.getDimensionPixelSize(r4)
                    r6.width = r3
                    com.android.systemui.fsgesture.DrawerDemoAct r3 = com.android.systemui.fsgesture.DrawerDemoAct.this
                    android.view.View r3 = r3.shelterView
                    r3.setLayoutParams(r6)
                    com.android.systemui.fsgesture.DrawerDemoAct r6 = com.android.systemui.fsgesture.DrawerDemoAct.this
                    android.widget.ImageView r6 = r6.drawerImg
                    int r6 = r6.getWidth()
                    int r6 = r6 / r2
                    float r6 = (float) r6
                    int r6 = (r7 > r6 ? 1 : (r7 == r6 ? 0 : -1))
                    r2 = 200(0xc8, double:9.9E-322)
                    if (r6 < 0) goto L_0x00ab
                    com.android.systemui.fsgesture.DrawerDemoAct r6 = com.android.systemui.fsgesture.DrawerDemoAct.this
                    android.widget.ImageView r6 = r6.drawerImg
                    android.view.ViewPropertyAnimator r6 = r6.animate()
                    com.android.systemui.fsgesture.DrawerDemoAct r7 = com.android.systemui.fsgesture.DrawerDemoAct.this
                    int r7 = r7.maxTranslateWidth
                    float r7 = (float) r7
                    android.view.ViewPropertyAnimator r6 = r6.translationX(r7)
                    android.view.ViewPropertyAnimator r6 = r6.setDuration(r2)
                    r6.start()
                    com.android.systemui.fsgesture.DrawerDemoAct r6 = com.android.systemui.fsgesture.DrawerDemoAct.this
                    com.android.systemui.fsgesture.FsGestureDemoTitleView r6 = r6.fsGestureDemoTitleView
                    r6.notifyFinish()
                    com.android.systemui.fsgesture.DrawerDemoAct r6 = com.android.systemui.fsgesture.DrawerDemoAct.this
                    android.os.Handler r6 = r6.handler
                    com.android.systemui.fsgesture.DrawerDemoAct$1$1 r7 = new com.android.systemui.fsgesture.DrawerDemoAct$1$1
                    r7.<init>()
                    r1 = 1000(0x3e8, double:4.94E-321)
                    r6.postDelayed(r7, r1)
                    goto L_0x00ec
                L_0x00ab:
                    com.android.systemui.fsgesture.DrawerDemoAct r6 = com.android.systemui.fsgesture.DrawerDemoAct.this
                    r6.startSwipeViewAnimation(r1)
                    com.android.systemui.fsgesture.DrawerDemoAct r6 = com.android.systemui.fsgesture.DrawerDemoAct.this
                    android.widget.ImageView r6 = r6.drawerImg
                    android.view.ViewPropertyAnimator r6 = r6.animate()
                    com.android.systemui.fsgesture.DrawerDemoAct r5 = com.android.systemui.fsgesture.DrawerDemoAct.this
                    int r5 = r5.initTranslateWidht
                    float r5 = (float) r5
                    android.view.ViewPropertyAnimator r5 = r6.translationX(r5)
                    android.view.ViewPropertyAnimator r5 = r5.setDuration(r2)
                    r5.start()
                    goto L_0x00ec
                L_0x00cd:
                    com.android.systemui.fsgesture.DrawerDemoAct r6 = com.android.systemui.fsgesture.DrawerDemoAct.this
                    com.android.systemui.fsgesture.FsGestureDemoSwipeView r6 = r6.fsGestureDemoSwipeView
                    r6.cancelAnimation()
                    com.android.systemui.fsgesture.DrawerDemoAct r6 = com.android.systemui.fsgesture.DrawerDemoAct.this
                    android.view.View r6 = r6.shelterView
                    android.view.ViewGroup$LayoutParams r6 = r6.getLayoutParams()
                    r7 = -1
                    r6.width = r7
                    com.android.systemui.fsgesture.DrawerDemoAct r5 = com.android.systemui.fsgesture.DrawerDemoAct.this
                    android.view.View r5 = r5.shelterView
                    r5.setLayoutParams(r6)
                L_0x00ec:
                    return r0
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.fsgesture.DrawerDemoAct.AnonymousClass1.onTouch(android.view.View, android.view.MotionEvent):boolean");
            }
        });
        this.drawerImg.post(new Runnable() {
            public void run() {
                int width = DrawerDemoAct.this.drawerImg.getWidth();
                if (DrawerDemoAct.IS_DEBUG) {
                    String str = DrawerDemoAct.TAG;
                    Log.d(str, "====>>>> width:" + width);
                }
                int unused = DrawerDemoAct.this.initTranslateWidht = -width;
                int unused2 = DrawerDemoAct.this.maxTranslateWidth = 0;
                DrawerDemoAct.this.drawerImg.setTranslationX((float) DrawerDemoAct.this.initTranslateWidht);
            }
        });
        FsGestureDemoTitleView fsGestureDemoTitleView2 = (FsGestureDemoTitleView) findViewById(C0015R$id.fsgesture_title_view);
        this.fsGestureDemoTitleView = fsGestureDemoTitleView2;
        fsGestureDemoTitleView2.prepareTitleView(4);
        this.fsGestureDemoTitleView.registerSkipEvent(new View.OnClickListener() {
            public void onClick(View view) {
                DrawerDemoAct.this.finish();
            }
        });
        GestureTitleViewUtil.setMargin(this, this.fsGestureDemoTitleView);
        this.fsGestureDemoSwipeView = (FsGestureDemoSwipeView) findViewById(C0015R$id.fsgesture_swipe_view);
        startSwipeViewAnimation(3);
        this.mNavigationHandle = GestureLineUtils.createAndaddNavigationHandle((RelativeLayout) this.drawerImg.getParent());
    }

    /* access modifiers changed from: private */
    public void startSwipeViewAnimation(final int i) {
        this.handler.postDelayed(new Runnable() {
            public void run() {
                DrawerDemoAct.this.fsGestureDemoSwipeView.prepare(i);
                DrawerDemoAct.this.fsGestureDemoSwipeView.startAnimation(i);
            }
        }, 500);
    }
}
