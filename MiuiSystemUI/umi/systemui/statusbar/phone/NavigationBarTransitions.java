package com.android.systemui.statusbar.phone;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateInterpolator;
import com.android.internal.statusbar.IStatusBarService;
import com.android.internal.statusbar.StatusBarServiceCompat;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.phone.LightBarTransitionsController;

public final class NavigationBarTransitions extends BarTransitions implements LightBarTransitionsController.DarkIntensityApplier {
    /* access modifiers changed from: private */
    public final IStatusBarService mBarService;
    private final LightBarTransitionsController mLightTransitionsController;
    private boolean mLightsOut;
    private final View.OnTouchListener mLightsOutListener = new View.OnTouchListener() {
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent.getAction() == 0) {
                NavigationBarTransitions.this.applyLightsOut(false, false, false);
                try {
                    StatusBarServiceCompat.setSystemUiVisibility(NavigationBarTransitions.this.mBarService, 0, 1, 0, "LightsOutListener");
                } catch (RemoteException unused) {
                }
            }
            return false;
        }
    };
    private final NavigationBarView mView;

    public NavigationBarTransitions(NavigationBarView navigationBarView) {
        super(navigationBarView, R.drawable.nav_background, R.color.system_nav_bar_background_opaque);
        this.mView = navigationBarView;
        this.mBarService = IStatusBarService.Stub.asInterface(ServiceManager.getService("statusbar"));
        this.mLightTransitionsController = new LightBarTransitionsController(navigationBarView.getContext(), this);
    }

    public void init() {
        applyModeBackground(-1, getMode(), false);
        applyMode(getMode(), false, true);
    }

    /* access modifiers changed from: protected */
    public void onTransition(int i, int i2, boolean z) {
        super.onTransition(i, i2, z);
        applyMode(i2, z, false);
    }

    private void applyMode(int i, boolean z, boolean z2) {
        applyLightsOut(isLightsOut(i), z, z2);
    }

    /* access modifiers changed from: private */
    public void applyLightsOut(boolean z, boolean z2, boolean z3) {
        AnonymousClass1 r8;
        if (z3 || z != this.mLightsOut) {
            this.mLightsOut = z;
            View findViewById = this.mView.getCurrentView().findViewById(R.id.nav_buttons);
            final View findViewById2 = this.mView.getCurrentView().findViewById(R.id.lights_out);
            findViewById.animate().cancel();
            findViewById2.animate().cancel();
            float f = 1.0f;
            float f2 = z ? 0.0f : 1.0f;
            if (!z) {
                f = 0.0f;
            }
            int i = 0;
            if (!z2) {
                findViewById.setAlpha(f2);
                findViewById2.setAlpha(f);
                if (!z) {
                    i = 8;
                }
                findViewById2.setVisibility(i);
                return;
            }
            long j = (long) (z ? 750 : 250);
            findViewById.animate().alpha(f2).setDuration(j).start();
            findViewById2.setOnTouchListener(this.mLightsOutListener);
            if (findViewById2.getVisibility() == 8) {
                findViewById2.setAlpha(0.0f);
                findViewById2.setVisibility(0);
            }
            ViewPropertyAnimator interpolator = findViewById2.animate().alpha(f).setDuration(j).setInterpolator(new AccelerateInterpolator(2.0f));
            if (z) {
                r8 = null;
            } else {
                r8 = new AnimatorListenerAdapter(this) {
                    public void onAnimationEnd(Animator animator) {
                        findViewById2.setVisibility(8);
                    }
                };
            }
            interpolator.setListener(r8).start();
        }
    }

    public LightBarTransitionsController getLightTransitionsController() {
        return this.mLightTransitionsController;
    }

    public void applyDarkIntensity(float f) {
        Log.d("NavigationBarTransitions", "applyDarkIntensity ".concat(String.valueOf(f)));
        this.mView.getNavigationHandle().setDarkIntensity(f);
    }
}
