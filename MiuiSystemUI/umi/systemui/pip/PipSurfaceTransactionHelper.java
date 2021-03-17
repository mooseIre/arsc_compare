package com.android.systemui.pip;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.SurfaceControl;
import com.android.systemui.C0010R$bool;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.statusbar.policy.ConfigurationController;

public class PipSurfaceTransactionHelper implements ConfigurationController.ConfigurationListener {
    private final Context mContext;
    private int mCornerRadius;
    private final boolean mEnableCornerRadius;
    private final Rect mTmpDestinationRect = new Rect();
    private final RectF mTmpDestinationRectF = new RectF();
    private final float[] mTmpFloat9 = new float[9];
    private final RectF mTmpSourceRectF = new RectF();
    private final Matrix mTmpTransform = new Matrix();

    interface SurfaceControlTransactionFactory {
        SurfaceControl.Transaction getTransaction();
    }

    public PipSurfaceTransactionHelper(Context context, ConfigurationController configurationController) {
        Resources resources = context.getResources();
        this.mContext = context;
        this.mEnableCornerRadius = resources.getBoolean(C0010R$bool.config_pipEnableRoundCorner);
        configurationController.addCallback(this);
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onDensityOrFontScaleChanged() {
        this.mCornerRadius = this.mContext.getResources().getDimensionPixelSize(C0012R$dimen.pip_corner_radius);
    }

    /* access modifiers changed from: package-private */
    public PipSurfaceTransactionHelper alpha(SurfaceControl.Transaction transaction, SurfaceControl surfaceControl, float f) {
        transaction.setAlpha(surfaceControl, f);
        return this;
    }

    /* access modifiers changed from: package-private */
    public PipSurfaceTransactionHelper crop(SurfaceControl.Transaction transaction, SurfaceControl surfaceControl, Rect rect) {
        transaction.setWindowCrop(surfaceControl, rect.width(), rect.height()).setPosition(surfaceControl, (float) rect.left, (float) rect.top);
        return this;
    }

    /* access modifiers changed from: package-private */
    public PipSurfaceTransactionHelper scale(SurfaceControl.Transaction transaction, SurfaceControl surfaceControl, Rect rect, Rect rect2) {
        this.mTmpSourceRectF.set(rect);
        this.mTmpDestinationRectF.set(rect2);
        this.mTmpTransform.setRectToRect(this.mTmpSourceRectF, this.mTmpDestinationRectF, Matrix.ScaleToFit.FILL);
        SurfaceControl.Transaction matrix = transaction.setMatrix(surfaceControl, this.mTmpTransform, this.mTmpFloat9);
        RectF rectF = this.mTmpDestinationRectF;
        matrix.setPosition(surfaceControl, rectF.left, rectF.top);
        return this;
    }

    /* access modifiers changed from: package-private */
    public PipSurfaceTransactionHelper scaleAndCrop(SurfaceControl.Transaction transaction, SurfaceControl surfaceControl, Rect rect, Rect rect2, Rect rect3) {
        int i;
        float f;
        this.mTmpSourceRectF.set(rect);
        this.mTmpDestinationRect.set(rect);
        this.mTmpDestinationRect.inset(rect3);
        if (rect.width() <= rect.height()) {
            f = (float) rect2.width();
            i = rect.width();
        } else {
            f = (float) rect2.height();
            i = rect.height();
        }
        float f2 = f / ((float) i);
        float f3 = ((float) rect2.top) - (((float) rect3.top) * f2);
        this.mTmpTransform.setScale(f2, f2);
        transaction.setMatrix(surfaceControl, this.mTmpTransform, this.mTmpFloat9).setWindowCrop(surfaceControl, this.mTmpDestinationRect).setPosition(surfaceControl, ((float) rect2.left) - (((float) rect3.left) * f2), f3);
        return this;
    }

    /* access modifiers changed from: package-private */
    public PipSurfaceTransactionHelper resetScale(SurfaceControl.Transaction transaction, SurfaceControl surfaceControl, Rect rect) {
        transaction.setMatrix(surfaceControl, Matrix.IDENTITY_MATRIX, this.mTmpFloat9).setPosition(surfaceControl, (float) rect.left, (float) rect.top);
        return this;
    }

    /* access modifiers changed from: package-private */
    public PipSurfaceTransactionHelper round(SurfaceControl.Transaction transaction, SurfaceControl surfaceControl, boolean z) {
        if (this.mEnableCornerRadius) {
            transaction.setCornerRadius(surfaceControl, z ? (float) this.mCornerRadius : 0.0f);
        }
        return this;
    }
}
