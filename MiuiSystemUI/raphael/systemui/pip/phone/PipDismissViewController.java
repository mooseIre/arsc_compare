package com.android.systemui.pip.phone;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import com.android.systemui.Interpolators;
import com.android.systemui.plugins.R;
import com.android.systemui.recents.misc.SystemServicesProxy;

public class PipDismissViewController {
    private Context mContext;
    /* access modifiers changed from: private */
    public View mDismissView;
    /* access modifiers changed from: private */
    public WindowManager mWindowManager;

    public PipDismissViewController(Context context) {
        this.mContext = context;
        this.mWindowManager = (WindowManager) context.getSystemService("window");
    }

    public void createDismissTarget() {
        if (this.mDismissView == null) {
            Rect rect = new Rect();
            SystemServicesProxy.getInstance(this.mContext).getStableInsets(rect);
            Point point = new Point();
            this.mWindowManager.getDefaultDisplay().getRealSize(point);
            int dimensionPixelSize = this.mContext.getResources().getDimensionPixelSize(R.dimen.pip_dismiss_gradient_height);
            int dimensionPixelSize2 = this.mContext.getResources().getDimensionPixelSize(R.dimen.pip_dismiss_text_bottom_margin);
            this.mDismissView = LayoutInflater.from(this.mContext).inflate(R.layout.pip_dismiss_view, (ViewGroup) null);
            this.mDismissView.setSystemUiVisibility(256);
            this.mDismissView.forceHasOverlappingRendering(false);
            Drawable drawable = this.mContext.getResources().getDrawable(R.drawable.pip_dismiss_scrim);
            drawable.setAlpha(216);
            this.mDismissView.setBackground(drawable);
            ((FrameLayout.LayoutParams) this.mDismissView.findViewById(R.id.pip_dismiss_text).getLayoutParams()).bottomMargin = rect.bottom + dimensionPixelSize2;
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(-1, dimensionPixelSize, 0, point.y - dimensionPixelSize, 2024, 16777496, -3);
            layoutParams.setTitle("pip-dismiss-overlay");
            layoutParams.privateFlags |= 16;
            layoutParams.gravity = 49;
            this.mWindowManager.addView(this.mDismissView, layoutParams);
        }
        this.mDismissView.animate().cancel();
    }

    public void showDismissTarget() {
        this.mDismissView.animate().alpha(1.0f).setInterpolator(Interpolators.LINEAR).setStartDelay(100).setDuration(350).start();
    }

    public void destroyDismissTarget() {
        View view = this.mDismissView;
        if (view != null) {
            view.animate().alpha(0.0f).setInterpolator(Interpolators.LINEAR).setStartDelay(0).setDuration(225).withEndAction(new Runnable() {
                public void run() {
                    PipDismissViewController.this.mWindowManager.removeViewImmediate(PipDismissViewController.this.mDismissView);
                    View unused = PipDismissViewController.this.mDismissView = null;
                }
            }).start();
        }
    }
}
