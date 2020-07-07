package com.android.systemui.shared.system;

import android.graphics.HardwareRenderer;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.view.Surface;
import android.view.SurfaceControl;
import android.view.View;
import android.view.ViewRootImpl;
import com.android.systemui.shared.system.SyncRtSurfaceTransactionApplier;

public class SyncRtSurfaceTransactionApplier {
    private final Surface mTargetSurface;
    private final ViewRootImpl mTargetViewRootImpl;
    private final float[] mTmpFloat9 = new float[9];

    public SyncRtSurfaceTransactionApplier(View view) {
        Surface surface = null;
        ViewRootImpl viewRootImpl = view != null ? view.getViewRootImpl() : null;
        this.mTargetViewRootImpl = viewRootImpl;
        this.mTargetSurface = viewRootImpl != null ? viewRootImpl.mSurface : surface;
    }

    public void scheduleApply(SurfaceParams... surfaceParamsArr) {
        ViewRootImpl viewRootImpl = this.mTargetViewRootImpl;
        if (viewRootImpl != null) {
            viewRootImpl.registerRtFrameCallback(new HardwareRenderer.FrameDrawingCallback(surfaceParamsArr) {
                public final /* synthetic */ SyncRtSurfaceTransactionApplier.SurfaceParams[] f$1;

                {
                    this.f$1 = r2;
                }

                public final void onFrameDraw(long j) {
                    SyncRtSurfaceTransactionApplier.this.lambda$scheduleApply$0$SyncRtSurfaceTransactionApplier(this.f$1, j);
                }
            });
            this.mTargetViewRootImpl.getView().invalidate();
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$scheduleApply$0 */
    public /* synthetic */ void lambda$scheduleApply$0$SyncRtSurfaceTransactionApplier(SurfaceParams[] surfaceParamsArr, long j) {
        Surface surface = this.mTargetSurface;
        if (surface != null && surface.isValid()) {
            SurfaceControl.Transaction transaction = new SurfaceControl.Transaction();
            for (int length = surfaceParamsArr.length - 1; length >= 0; length--) {
                SurfaceParams surfaceParams = surfaceParamsArr[length];
                transaction.deferTransactionUntilSurface(surfaceParams.surface, this.mTargetSurface, j);
                applyParams(transaction, surfaceParams, this.mTmpFloat9);
            }
            transaction.setEarlyWakeup();
            transaction.apply();
        }
    }

    private static void applyParams(SurfaceControl.Transaction transaction, SurfaceParams surfaceParams, float[] fArr) {
        transaction.setMatrix(surfaceParams.surface, surfaceParams.matrix, fArr);
        transaction.setWindowCrop(surfaceParams.surface, surfaceParams.windowCrop);
        transaction.setAlpha(surfaceParams.surface, surfaceParams.alpha);
        setCornerRadius(transaction, surfaceParams);
        if (surfaceParams.visible) {
            transaction.show(surfaceParams.surface);
        } else {
            transaction.hide(surfaceParams.surface);
        }
        SurfaceControl surfaceControl = surfaceParams.layerAbove;
        if (surfaceControl == null) {
            transaction.setLayer(surfaceParams.surface, surfaceParams.layer);
        } else {
            transaction.setRelativeLayer(surfaceParams.surface, surfaceControl, -1);
        }
    }

    private static void setCornerRadius(SurfaceControl.Transaction transaction, SurfaceParams surfaceParams) {
        try {
            transaction.getClass().getDeclaredMethod("setCornerRadius", new Class[]{SurfaceControl.class, Float.TYPE}).invoke(transaction, new Object[]{surfaceParams.surface, Float.valueOf(surfaceParams.cornerRadius)});
        } catch (Exception unused) {
        }
    }

    public static class SurfaceParams {
        final float alpha;
        final float cornerRadius;
        final int layer;
        SurfaceControl layerAbove;
        final Matrix matrix;
        final SurfaceControl surface;
        public final boolean visible;
        final Rect windowCrop;

        public SurfaceParams(SurfaceControlCompat surfaceControlCompat, float f, Matrix matrix2, Rect rect, int i, float f2, boolean z) {
            this.surface = surfaceControlCompat.mSurfaceControl;
            this.alpha = f;
            this.matrix = new Matrix(matrix2);
            this.windowCrop = new Rect(rect);
            this.layer = i;
            this.cornerRadius = f2;
            this.visible = z;
        }

        public void setLayerAbove(SurfaceControlCompat surfaceControlCompat) {
            this.layerAbove = surfaceControlCompat.mSurfaceControl;
        }
    }
}
