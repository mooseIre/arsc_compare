package com.android.systemui.pip.tv;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ParceledListSlice;
import android.os.Bundle;
import com.android.systemui.pip.tv.PipManager;
import com.android.systemui.plugins.R;
import java.util.Collections;

public class PipMenuActivity extends Activity implements PipManager.Listener {
    private Animator mFadeInAnimation;
    private Animator mFadeOutAnimation;
    private PipControlsView mPipControlsView;
    private final PipManager mPipManager = PipManager.getInstance();
    private boolean mRestorePipSizeWhenClose;

    public void onPipEntered() {
    }

    public void onShowPipMenu() {
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.tv_pip_menu);
        this.mPipManager.addListener(this);
        this.mRestorePipSizeWhenClose = true;
        this.mPipControlsView = (PipControlsView) findViewById(R.id.pip_controls);
        Animator loadAnimator = AnimatorInflater.loadAnimator(this, R.anim.tv_pip_menu_fade_in_animation);
        this.mFadeInAnimation = loadAnimator;
        loadAnimator.setTarget(this.mPipControlsView);
        Animator loadAnimator2 = AnimatorInflater.loadAnimator(this, R.anim.tv_pip_menu_fade_out_animation);
        this.mFadeOutAnimation = loadAnimator2;
        loadAnimator2.setTarget(this.mPipControlsView);
        onPipMenuActionsChanged(getIntent().getParcelableExtra("custom_actions"));
    }

    /* access modifiers changed from: protected */
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        onPipMenuActionsChanged(getIntent().getParcelableExtra("custom_actions"));
    }

    private void restorePipAndFinish() {
        if (this.mRestorePipSizeWhenClose) {
            this.mPipManager.resizePinnedStack(1);
        }
        finish();
    }

    public void onResume() {
        super.onResume();
        this.mFadeInAnimation.start();
    }

    public void onPause() {
        super.onPause();
        this.mFadeOutAnimation.start();
        restorePipAndFinish();
    }

    /* access modifiers changed from: protected */
    public void onDestroy() {
        super.onDestroy();
        this.mPipManager.removeListener(this);
        this.mPipManager.resumePipResizing(1);
    }

    public void onBackPressed() {
        restorePipAndFinish();
    }

    public void onPipActivityClosed() {
        finish();
    }

    public void onPipMenuActionsChanged(ParceledListSlice parceledListSlice) {
        this.mPipControlsView.setActions(parceledListSlice != null && !parceledListSlice.getList().isEmpty() ? parceledListSlice.getList() : Collections.EMPTY_LIST);
    }

    public void onMoveToFullscreen() {
        this.mRestorePipSizeWhenClose = false;
        finish();
    }

    public void onPipResizeAboutToStart() {
        finish();
        this.mPipManager.suspendPipResizing(1);
    }
}
