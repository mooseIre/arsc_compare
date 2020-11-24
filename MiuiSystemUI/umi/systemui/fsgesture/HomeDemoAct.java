package com.android.systemui.fsgesture;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import com.android.systemui.C0012R$id;
import com.android.systemui.C0014R$layout;

public class HomeDemoAct extends FsGestureDemoBaseActiivy {
    private View appBgView;
    private View appNoteImg;
    /* access modifiers changed from: private */
    public FsGestureDemoSwipeView fsGestureDemoSwipeView;
    private FsGestureDemoTitleView fsGestureDemoTitleView;
    /* access modifiers changed from: private */
    public NavStubDemoView fsgNavView;
    Handler handler = new Handler();
    private LinearLayout homeIconImg;
    /* access modifiers changed from: private */
    public View mAnimIcon;
    /* access modifiers changed from: private */
    public LinearLayout mRecentsCardContainer;
    private View mRecentsFirstCardIconView;
    private View navSubViewBgView;
    private View recentsBgView;

    static {
        Class<HomeDemoAct> cls = HomeDemoAct.class;
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        getWindow().addFlags(1024);
        setContentView(C0014R$layout.home_demo_layout);
        FsgestureUtil.INSTANCE.hideSystemBars(getWindow().getDecorView());
        Intent intent = getIntent();
        String stringExtra = intent.getStringExtra("DEMO_TYPE");
        int intExtra = intent.getIntExtra("FULLY_SHOW_STEP", 1);
        boolean booleanExtra = intent.getBooleanExtra("IS_FROM_PROVISION", false);
        this.homeIconImg = (LinearLayout) findViewById(C0012R$id.home_icon_img);
        ImageView imageView = (ImageView) findViewById(C0012R$id.anim_icon);
        this.mAnimIcon = imageView;
        imageView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                HomeDemoAct.this.mAnimIcon.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int[] locationOnScreen = HomeDemoAct.this.mAnimIcon.getLocationOnScreen();
                locationOnScreen[0] = locationOnScreen[0] + (HomeDemoAct.this.mAnimIcon.getWidth() / 2);
                locationOnScreen[1] = locationOnScreen[1] + (HomeDemoAct.this.mAnimIcon.getHeight() / 2);
                if (HomeDemoAct.this.fsgNavView != null) {
                    HomeDemoAct.this.fsgNavView.setDestPivot(locationOnScreen[0], locationOnScreen[1]);
                }
            }
        });
        this.recentsBgView = findViewById(C0012R$id.recents_bg_view);
        this.mRecentsCardContainer = (LinearLayout) findViewById(C0012R$id.recents_card_container);
        this.mRecentsFirstCardIconView = findViewById(C0012R$id.recents_first_card_icon);
        this.mRecentsCardContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                HomeDemoAct.this.mRecentsCardContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                Rect rect = new Rect();
                ((ImageView) HomeDemoAct.this.findViewById(C0012R$id.recents_first_card)).getBoundsOnScreen(rect);
                if (HomeDemoAct.this.fsgNavView != null) {
                    HomeDemoAct.this.fsgNavView.setRecentsFirstCardBound(rect);
                }
            }
        });
        this.mRecentsFirstCardIconView = findViewById(C0012R$id.recents_first_card_icon);
        this.appBgView = findViewById(C0012R$id.app_bg_view);
        this.appNoteImg = findViewById(C0012R$id.app_note_img);
        this.navSubViewBgView = findViewById(C0012R$id.navstubview_bg_view);
        this.fsGestureDemoTitleView = (FsGestureDemoTitleView) findViewById(C0012R$id.fsgesture_title_view);
        int i = (!"DEMO_FULLY_SHOW".equals(stringExtra) ? !"DEMO_TO_HOME".equals(stringExtra) : intExtra != 1) ? 3 : 2;
        this.fsGestureDemoTitleView.prepareTitleView(i);
        this.fsGestureDemoTitleView.registerSkipEvent(new View.OnClickListener() {
            public void onClick(View view) {
                HomeDemoAct.this.finish();
            }
        });
        GestureTitleViewUtil.setMargin(this, this.fsGestureDemoTitleView);
        this.fsGestureDemoSwipeView = (FsGestureDemoSwipeView) findViewById(C0012R$id.fsgesture_swipe_view);
        if (i == 3) {
            startSwipeViewAnimation(4);
        } else {
            startSwipeViewAnimation(2);
        }
        this.mNavigationHandle = GestureLineUtils.createAndaddNavigationHandle((RelativeLayout) this.fsGestureDemoTitleView.getParent());
        NavStubDemoView navStubDemoView = (NavStubDemoView) findViewById(C0012R$id.fsg_nav_view);
        this.fsgNavView = navStubDemoView;
        navStubDemoView.setCurActivity(this);
        this.fsgNavView.setDemoType(stringExtra);
        this.fsgNavView.setFullyShowStep(intExtra);
        this.fsgNavView.setIsFromPro(booleanExtra);
        this.fsgNavView.setHomeIconImg(this.homeIconImg);
        this.fsgNavView.setRecentsBgView(this.recentsBgView);
        this.fsgNavView.setRecentsCardContainer(this.mRecentsCardContainer);
        this.fsgNavView.setRecentsFirstCardIconView(this.mRecentsFirstCardIconView);
        this.fsgNavView.setAppBgView(this.appBgView);
        this.fsgNavView.setAppNoteImg(this.appNoteImg);
        this.fsgNavView.setDemoTitleView(this.fsGestureDemoTitleView);
        this.fsgNavView.setSwipeView(this.fsGestureDemoSwipeView);
        this.fsgNavView.setBgView(this.navSubViewBgView);
    }

    private void startSwipeViewAnimation(final int i) {
        this.handler.postDelayed(new Runnable() {
            public void run() {
                HomeDemoAct.this.fsGestureDemoSwipeView.prepare(i);
                HomeDemoAct.this.fsGestureDemoSwipeView.startAnimation(i);
            }
        }, 500);
    }

    /* access modifiers changed from: protected */
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }
}
