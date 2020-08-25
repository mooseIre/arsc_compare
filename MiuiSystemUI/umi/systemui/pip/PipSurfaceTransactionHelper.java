package com.android.systemui.pip;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.SurfaceControl;
import com.android.systemui.plugins.R;

public class PipSurfaceTransactionHelper {
    public static PipSurfaceTransactionHelper sPipSurfaceTransactionHelper;
    private final int mCornerRadius;
    private final boolean mEnableCornerRadius;
    private final RectF mTmpDestinationRectF = new RectF();
    private final float[] mTmpFloat9 = new float[9];
    private final RectF mTmpSourceRectF = new RectF();
    private final Matrix mTmpTransform = new Matrix();

    interface SurfaceControlTransactionFactory {
        SurfaceControl.Transaction getTransaction();
    }

    public static PipSurfaceTransactionHelper getInstance(Context context) {
        if (sPipSurfaceTransactionHelper == null) {
            sPipSurfaceTransactionHelper = new PipSurfaceTransactionHelper(context);
        }
        return sPipSurfaceTransactionHelper;
    }

    private PipSurfaceTransactionHelper(Context context) {
        Resources resources = context.getResources();
        this.mEnableCornerRadius = resources.getBoolean(R.bool.config_pipEnableRoundCorner);
        this.mCornerRadius = resources.getDimensionPixelSize(R.dimen.pip_corner_radius);
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
        transaction.setMatrix(surfaceControl, this.mTmpTransform, this.mTmpFloat9).setPosition(surfaceControl, (float) rect2.left, (float) rect2.top);
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
