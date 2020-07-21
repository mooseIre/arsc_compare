package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.ContextCompat;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.util.Log;
import android.view.CompositionSamplingListener;
import android.view.CompositionSamplingListenerCompat;
import android.view.View;
import com.android.systemui.plugins.R;

public class NavBarTintController implements View.OnAttachStateChangeListener, View.OnLayoutChangeListener {
    private float mCurrentMedianLuma;
    private final Handler mHandler = new Handler();
    private float mLastMedianLuma;
    private final LightBarTransitionsController mLightBarController;
    private final float mLuminanceChangeThreshold;
    private final float mLuminanceThreshold;
    private final int mNavBarHeight;
    private final int mNavColorSampleMargin;
    private final NavigationBarView mNavigationBarView;
    private final Rect mSamplingBounds = new Rect();
    private boolean mSamplingEnabled = false;
    private final CompositionSamplingListenerCompat mSamplingListener;
    private boolean mSamplingListenerRegistered = false;
    private boolean mUpdateOnNextDraw;
    private final Runnable mUpdateSamplingListener = new Runnable() {
        public final void run() {
            NavBarTintController.this.updateSamplingListener();
        }
    };

    public NavBarTintController(NavigationBarView navigationBarView, LightBarTransitionsController lightBarTransitionsController) {
        this.mSamplingListener = new CompositionSamplingListenerCompat(navigationBarView.getContext().getMainExecutor()) {
            public void onSampleCollected(float f) {
                Log.d("NavBarTintController", "onSampleCollected ".concat(String.valueOf(f)));
                NavBarTintController.this.updateTint(f);
            }
        };
        this.mNavigationBarView = navigationBarView;
        this.mNavigationBarView.addOnAttachStateChangeListener(this);
        this.mNavigationBarView.addOnLayoutChangeListener(this);
        this.mLightBarController = lightBarTransitionsController;
        Resources resources = navigationBarView.getResources();
        this.mNavBarHeight = resources.getDimensionPixelSize(17105296);
        this.mNavColorSampleMargin = resources.getDimensionPixelSize(R.dimen.navigation_handle_sample_horizontal_margin);
        this.mLuminanceThreshold = resources.getFloat(R.dimen.navigation_luminance_threshold);
        this.mLuminanceChangeThreshold = resources.getFloat(R.dimen.navigation_luminance_change_threshold);
    }

    /* access modifiers changed from: package-private */
    public void onDraw() {
        if (this.mUpdateOnNextDraw) {
            this.mUpdateOnNextDraw = false;
            Log.d("NavBarTintController", "onDraw");
            requestUpdateSamplingListener();
        }
    }

    /* access modifiers changed from: package-private */
    public void start() {
        if (!isEnabled(this.mNavigationBarView.getContext())) {
            Log.d("NavBarTintController", "not enabled");
            return;
        }
        Log.d("NavBarTintController", "start");
        this.mSamplingEnabled = true;
        requestUpdateSamplingListener();
    }

    /* access modifiers changed from: package-private */
    public void stop() {
        Log.d("NavBarTintController", "stop");
        this.mSamplingEnabled = false;
        requestUpdateSamplingListener();
    }

    public void onViewAttachedToWindow(View view) {
        requestUpdateSamplingListener();
    }

    public void onViewDetachedFromWindow(View view) {
        requestUpdateSamplingListener();
    }

    public void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        this.mSamplingBounds.setEmpty();
        NavigationHandle navigationHandle = this.mNavigationBarView.getNavigationHandle();
        if (navigationHandle != null) {
            int[] iArr = new int[2];
            navigationHandle.getLocationOnScreen(iArr);
            Point point = new Point();
            navigationHandle.getContext().getDisplay().getRealSize(point);
            Rect rect = new Rect(iArr[0] - this.mNavColorSampleMargin, point.y - this.mNavBarHeight, iArr[0] + navigationHandle.getWidth() + this.mNavColorSampleMargin, point.y);
            if (!rect.equals(this.mSamplingBounds)) {
                this.mSamplingBounds.set(rect);
                requestUpdateSamplingListener();
            }
        }
    }

    private void requestUpdateSamplingListener() {
        this.mHandler.removeCallbacks(this.mUpdateSamplingListener);
        this.mHandler.post(this.mUpdateSamplingListener);
    }

    /* access modifiers changed from: private */
    public void updateSamplingListener() {
        if (this.mSamplingListenerRegistered) {
            this.mSamplingListenerRegistered = false;
            CompositionSamplingListener.unregister(this.mSamplingListener);
        }
        if (this.mSamplingEnabled && !this.mSamplingBounds.isEmpty() && this.mNavigationBarView.isAttachedToWindow()) {
            Log.d("NavBarTintController", " updateSamplingListener true");
            if (!CompositionSamplingListenerCompat.isValid(this.mNavigationBarView)) {
                Log.d("NavBarTintController", " isValid not valid");
                this.mUpdateOnNextDraw = true;
                return;
            }
            this.mSamplingListenerRegistered = true;
            CompositionSamplingListenerCompat.register(this.mSamplingListener, 0, this.mNavigationBarView, this.mSamplingBounds);
        }
        Log.d("NavBarTintController", " updateSamplingListener false");
    }

    /* access modifiers changed from: private */
    public void updateTint(float f) {
        this.mLastMedianLuma = f;
        if (Math.abs(this.mCurrentMedianLuma - this.mLastMedianLuma) > this.mLuminanceChangeThreshold) {
            if (f > this.mLuminanceThreshold) {
                Log.d("NavBarTintController", " updateTint Black");
                this.mLightBarController.setIconsDark(true, true);
            } else {
                Log.d("NavBarTintController", " updateTint White");
                this.mLightBarController.setIconsDark(false, true);
            }
            this.mCurrentMedianLuma = f;
        }
    }

    public boolean isEnabled(Context context) {
        return ContextCompat.getDisplayId(context) == 0;
    }
}
