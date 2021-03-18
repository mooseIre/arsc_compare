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
    private static final boolean IS_DEBUG = Build.IS_ALPHA_BUILD;
    public static final String TAG = DrawerDemoAct.class.getSimpleName();
    private ImageView drawerImg;
    private FsGestureDemoSwipeView fsGestureDemoSwipeView;
    private FsGestureDemoTitleView fsGestureDemoTitleView;
    Handler handler = new Handler();
    private int initTranslateWidht;
    private int maxTranslateWidth;
    private View shelterView;

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.fsgesture.FsGestureDemoBaseActiivy
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(C0017R$layout.drawer_demo_layout);
        this.drawerImg = (ImageView) findViewById(C0015R$id.drawer_img);
        View findViewById = findViewById(C0015R$id.shelter_view);
        this.shelterView = findViewById;
        findViewById.setOnTouchListener(new View.OnTouchListener() {
            /* class com.android.systemui.fsgesture.DrawerDemoAct.AnonymousClass1 */

            /* JADX WARNING: Code restructure failed: missing block: B:5:0x0011, code lost:
                if (r6 != 3) goto L_0x00ec;
             */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public boolean onTouch(android.view.View r6, android.view.MotionEvent r7) {
                /*
                // Method dump skipped, instructions count: 237
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.fsgesture.DrawerDemoAct.AnonymousClass1.onTouch(android.view.View, android.view.MotionEvent):boolean");
            }
        });
        this.drawerImg.post(new Runnable() {
            /* class com.android.systemui.fsgesture.DrawerDemoAct.AnonymousClass2 */

            public void run() {
                int width = DrawerDemoAct.this.drawerImg.getWidth();
                if (DrawerDemoAct.IS_DEBUG) {
                    String str = DrawerDemoAct.TAG;
                    Log.d(str, "====>>>> width:" + width);
                }
                DrawerDemoAct.this.initTranslateWidht = -width;
                DrawerDemoAct.this.maxTranslateWidth = 0;
                DrawerDemoAct.this.drawerImg.setTranslationX((float) DrawerDemoAct.this.initTranslateWidht);
            }
        });
        FsGestureDemoTitleView fsGestureDemoTitleView2 = (FsGestureDemoTitleView) findViewById(C0015R$id.fsgesture_title_view);
        this.fsGestureDemoTitleView = fsGestureDemoTitleView2;
        fsGestureDemoTitleView2.prepareTitleView(4);
        this.fsGestureDemoTitleView.registerSkipEvent(new View.OnClickListener() {
            /* class com.android.systemui.fsgesture.DrawerDemoAct.AnonymousClass3 */

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
    /* access modifiers changed from: public */
    private void startSwipeViewAnimation(final int i) {
        this.handler.postDelayed(new Runnable() {
            /* class com.android.systemui.fsgesture.DrawerDemoAct.AnonymousClass4 */

            public void run() {
                DrawerDemoAct.this.fsGestureDemoSwipeView.prepare(i);
                DrawerDemoAct.this.fsGestureDemoSwipeView.startAnimation(i);
            }
        }, 500);
    }
}
