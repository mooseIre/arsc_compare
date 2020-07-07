package com.android.systemui.statusbar.phone;

import android.graphics.Rect;
import android.os.Handler;
import android.util.Log;
import android.view.CompositionSamplingListener;
import android.view.CompositionSamplingListenerCompat;
import android.view.View;
import com.android.systemui.Dependency;

public class PhoneStatusBarTintController implements View.OnAttachStateChangeListener, View.OnLayoutChangeListener {
    private DarkModeCallback mDarkModeCallback = new DarkModeCallback() {
        public void onDarkModeChanged(boolean z) {
            boolean unused = PhoneStatusBarTintController.this.mSmartDark = z;
            PhoneStatusBarTintController.this.requestUpdateSamplingListener();
        }
    };
    private final Handler mHandler = new Handler();
    private LightBarController mLightBarController;
    private final Rect mSamplingBounds = new Rect();
    private CompositionSamplingListenerCompat mSamplingListener;
    private boolean mSamplingListenerRegistered = false;
    /* access modifiers changed from: private */
    public boolean mSmartDark;
    private final PhoneStatusBarView mStatusBarView;
    private boolean mUpdateOnNextDraw;
    private final Runnable mUpdateSamplingListener = new Runnable() {
        public final void run() {
            PhoneStatusBarTintController.this.updateSamplingListener();
        }
    };

    public PhoneStatusBarTintController(PhoneStatusBarView phoneStatusBarView) {
        LightBarController lightBarController = (LightBarController) Dependency.get(LightBarController.class);
        this.mLightBarController = lightBarController;
        this.mSamplingListener = lightBarController.getCompositionSamplingListener();
        this.mStatusBarView = phoneStatusBarView;
        phoneStatusBarView.addOnAttachStateChangeListener(this);
        this.mStatusBarView.addOnLayoutChangeListener(this);
    }

    /* access modifiers changed from: package-private */
    public void onDraw() {
        if (this.mUpdateOnNextDraw) {
            this.mUpdateOnNextDraw = false;
            requestUpdateSamplingListener();
        }
    }

    public void onViewAttachedToWindow(View view) {
        this.mLightBarController.registerCallback(this.mDarkModeCallback);
        requestUpdateSamplingListener();
    }

    public void onViewDetachedFromWindow(View view) {
        this.mStatusBarView.removeOnAttachStateChangeListener(this);
        this.mStatusBarView.removeOnLayoutChangeListener(this);
        this.mLightBarController.removeCallback(this.mDarkModeCallback);
        requestUpdateSamplingListener();
    }

    public void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        Rect rect = new Rect();
        this.mStatusBarView.getBoundsOnScreen(rect);
        if (!rect.equals(this.mSamplingBounds)) {
            this.mSamplingBounds.set(rect);
            if (this.mSmartDark) {
                requestUpdateSamplingListener();
            }
        }
    }

    /* access modifiers changed from: private */
    public void requestUpdateSamplingListener() {
        this.mHandler.removeCallbacks(this.mUpdateSamplingListener);
        this.mHandler.post(this.mUpdateSamplingListener);
    }

    /* access modifiers changed from: private */
    public void updateSamplingListener() {
        if (this.mSamplingListenerRegistered) {
            this.mSamplingListenerRegistered = false;
            CompositionSamplingListener.unregister(this.mSamplingListener);
            Log.d("PhoneStatusBarTintController", "updateSamplingListener unregister ");
        }
        if (this.mSmartDark && !this.mSamplingBounds.isEmpty() && this.mStatusBarView.isAttachedToWindow()) {
            if (!CompositionSamplingListenerCompat.isValid(this.mStatusBarView)) {
                this.mUpdateOnNextDraw = true;
                return;
            }
            this.mSamplingListenerRegistered = true;
            CompositionSamplingListenerCompat.register(this.mSamplingListener, 0, this.mStatusBarView, this.mSamplingBounds);
            Log.d("PhoneStatusBarTintController", "updateSamplingListener register");
        }
    }
}
