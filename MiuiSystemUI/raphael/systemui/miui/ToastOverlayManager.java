package com.android.systemui.miui;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;
import java.lang.ref.WeakReference;
import java.util.HashMap;

public class ToastOverlayManager implements ViewTreeObserver.OnGlobalLayoutListener {
    private static final boolean ENABLED = (Build.VERSION.SDK_INT >= 28);
    private Context mContext;
    private Runnable mDispatchClearToastRunnable = new Runnable() {
        public void run() {
            for (ToastOverlayLayout toastOverlayLayout : ToastOverlayManager.this.mToastOverlayMap.values()) {
                toastOverlayLayout.setToast((Toast) null);
                toastOverlayLayout.invalidate();
            }
        }
    };
    private Runnable mDispatchHideToastRunnable = new Runnable() {
        public void run() {
            ToastOverlayManager.this.handleHideToast();
        }
    };
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private Toast mLastToast = null;
    /* access modifiers changed from: private */
    public HashMap<ViewGroup, ToastOverlayLayout> mToastOverlayMap = new HashMap<>();

    public void setup(Context context, ViewGroup viewGroup) {
        if (ENABLED && viewGroup != null) {
            this.mContext = context;
            ToastOverlayLayout toastOverlayLayout = new ToastOverlayLayout(context);
            this.mToastOverlayMap.put(viewGroup, toastOverlayLayout);
            viewGroup.addView(toastOverlayLayout, -1, -1);
        }
    }

    public void clear(Context context, ViewGroup viewGroup) {
        this.mToastOverlayMap.remove(viewGroup);
    }

    public void dispatchShowToast(final Toast toast) {
        if (ENABLED && this.mToastOverlayMap.size() != 0) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    ToastOverlayManager.this.handleShowToast(toast);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    public void handleShowToast(Toast toast) {
        this.mHandler.removeCallbacks(this.mDispatchHideToastRunnable);
        this.mHandler.removeCallbacks(this.mDispatchClearToastRunnable);
        Toast toast2 = this.mLastToast;
        if (toast2 != null) {
            toast2.cancel();
        }
        for (ToastOverlayLayout toast3 : this.mToastOverlayMap.values()) {
            toast3.setToast(toast);
        }
        this.mLastToast = toast;
        if (toast.getView() != null) {
            toast.getView().getViewTreeObserver().addOnGlobalLayoutListener(this);
        }
        startAnimIfExists("toast_enter");
        this.mHandler.postDelayed(this.mDispatchHideToastRunnable, toast.getDuration() == 1 ? 3500 : 2000);
    }

    /* access modifiers changed from: private */
    public void handleHideToast() {
        Animation startAnimIfExists = startAnimIfExists("toast_exit");
        this.mHandler.postDelayed(this.mDispatchClearToastRunnable, startAnimIfExists != null ? startAnimIfExists.getDuration() : 300);
        this.mLastToast = null;
    }

    private Animation startAnimIfExists(String str) {
        int identifier = this.mContext.getResources().getIdentifier(str, "anim", "android");
        if (identifier <= 0) {
            return null;
        }
        Animation loadAnimation = AnimationUtils.loadAnimation(this.mContext, identifier);
        for (ToastOverlayLayout next : this.mToastOverlayMap.values()) {
            if (next.getAnimation() != null) {
                next.getAnimation().cancel();
            }
            next.startAnimation(loadAnimation);
            next.invalidate();
        }
        return loadAnimation;
    }

    public void onGlobalLayout() {
        Toast toast = this.mLastToast;
        if (toast != null) {
            int[] iArr = new int[2];
            int[] iArr2 = new int[2];
            toast.getView().getLocationOnScreen(iArr);
            for (ToastOverlayLayout next : this.mToastOverlayMap.values()) {
                next.getLocationOnScreen(iArr2);
                next.setLocation(iArr[0] - iArr2[0], iArr[1] - iArr2[1]);
                next.invalidate();
            }
        }
    }

    private static class ToastOverlayLayout extends View {
        private WeakReference<Toast> mLastToastRef = new WeakReference<>((Object) null);
        private int mToastX;
        private int mToastY;

        public ToastOverlayLayout(Context context) {
            super(context);
        }

        public void setToast(Toast toast) {
            this.mLastToastRef = new WeakReference<>(toast);
        }

        public void setLocation(int i, int i2) {
            this.mToastX = i;
            this.mToastY = i2;
        }

        /* access modifiers changed from: protected */
        public void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (this.mToastX <= getWidth() && this.mToastY <= getHeight() && this.mLastToastRef.get() != null && ((Toast) this.mLastToastRef.get()).getView() != null) {
                canvas.save();
                canvas.translate((float) this.mToastX, (float) this.mToastY);
                ((Toast) this.mLastToastRef.get()).getView().draw(canvas);
                canvas.restore();
            }
        }
    }
}
