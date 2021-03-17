package com.android.keyguard.fod;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.util.Slog;
import android.view.LayoutInflater;
import android.view.WindowManager;
import com.android.keyguard.utils.MiuiKeyguardUtils;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0017R$layout;

/* access modifiers changed from: package-private */
public class MiuiGxzwHightlightContainer extends GxzwNoRotateFrameLayout {
    private boolean mHealthFingerAuthen;
    private WindowManager.LayoutParams mLayoutParams;
    private MiuiGxzwHighlightView mMiuiGxzwHighlightView;
    private final boolean mSupportHalo = MiuiGxzwUtils.supportHalo(getContext());

    public MiuiGxzwHightlightContainer(Context context) {
        super(context);
        initView();
    }

    public void setTouchCenter(float f, float f2) {
        this.mMiuiGxzwHighlightView.setTouchCenter(f, f2);
    }

    public void setOvalInfo(float f, float f2, float f3) {
        this.mMiuiGxzwHighlightView.setOvalInfo(f, f2, f3);
    }

    public void setHightlightTransparen() {
        this.mLayoutParams.alpha = 0.0f;
        if (isAttachedToWindow()) {
            this.mWindowManager.updateViewLayout(this, this.mLayoutParams);
            Slog.i("MiuiGxzwHightlightContainer", "dismiss highlight view");
        }
    }

    public void setHightlightOpaque() {
        this.mLayoutParams.alpha = 1.0f;
        if (isAttachedToWindow()) {
            this.mWindowManager.updateViewLayout(this, this.mLayoutParams);
            Slog.i("MiuiGxzwHightlightContainer", "show highlight view");
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.fod.GxzwNoRotateFrameLayout
    public void show() {
        if (!this.mShowing) {
            super.show();
            addHighlightView();
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.fod.GxzwNoRotateFrameLayout
    public void dismiss() {
        if (this.mShowing) {
            super.dismiss();
            removeHighlightView();
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.fod.GxzwWindowFrameLayout
    public WindowManager.LayoutParams generateLayoutParams() {
        return this.mLayoutParams;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.fod.GxzwWindowFrameLayout
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mMiuiGxzwHighlightView.setInvertColorStatus(MiuiKeyguardUtils.isInvertColorsEnable(getContext()));
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.fod.GxzwNoRotateFrameLayout
    public Rect caculateRegion() {
        int i;
        int i2 = 0;
        if (this.mSupportHalo || this.mHealthFingerAuthen) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeResource(getResources(), MiuiGxzwUtils.getHaloRes(), options);
            float haloResCircleRadius = MiuiGxzwUtils.getHaloResCircleRadius(getContext());
            i2 = (((int) Math.ceil((double) ((((float) MiuiGxzwUtils.GXZW_ICON_WIDTH) / haloResCircleRadius) * ((float) options.outWidth)))) - MiuiGxzwUtils.GXZW_ICON_WIDTH) / 2;
            i = (((int) Math.ceil((double) ((((float) MiuiGxzwUtils.GXZW_ICON_HEIGHT) / haloResCircleRadius) * ((float) options.outHeight)))) - MiuiGxzwUtils.GXZW_ICON_HEIGHT) / 2;
        } else {
            i = 0;
        }
        return new Rect(MiuiGxzwUtils.GXZW_ICON_X - i2, MiuiGxzwUtils.GXZW_ICON_Y - i, MiuiGxzwUtils.GXZW_ICON_X + MiuiGxzwUtils.GXZW_ICON_WIDTH + i2, MiuiGxzwUtils.GXZW_ICON_Y + MiuiGxzwUtils.GXZW_ICON_HEIGHT + i);
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(C0017R$layout.miui_keyguard_gxzw_icon_view, this);
        this.mMiuiGxzwHighlightView = (MiuiGxzwHighlightView) findViewById(C0015R$id.gxzw_highlight);
        setSystemUiVisibility(4864);
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(this.mRegion.width(), this.mRegion.height(), 2018, 16778776, -2);
        this.mLayoutParams = layoutParams;
        layoutParams.layoutInDisplayCutoutMode = 1;
        layoutParams.privateFlags |= MiuiGxzwUtils.PRIVATE_FLAG_IS_HBM_OVERLAY;
        layoutParams.gravity = 51;
        layoutParams.alpha = 0.0f;
        layoutParams.setTitle("gxzw_icon");
    }

    private void addHighlightView() {
        setVisibility(0);
        addViewToWindow();
        if (isAttachedToWindow()) {
            this.mWindowManager.updateViewLayout(this, this.mLayoutParams);
        }
    }

    private void removeHighlightView() {
        setVisibility(8);
        removeViewFromWindow();
    }

    public void updateViewBackground() {
        this.mHealthFingerAuthen = MiuiGxzwManager.getInstance().getHealthAppAuthen();
        this.mMiuiGxzwHighlightView.setInvertColorStatus(MiuiKeyguardUtils.isInvertColorsEnable(getContext()));
    }
}
