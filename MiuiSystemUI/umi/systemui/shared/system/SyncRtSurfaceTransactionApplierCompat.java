package com.android.systemui.shared.system;

import android.graphics.HardwareRenderer;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.os.Trace;
import android.view.SurfaceControl;
import android.view.View;
import android.view.ViewRootImpl;

public class SyncRtSurfaceTransactionApplierCompat {
    private Runnable mAfterApplyCallback;
    private final Handler mApplyHandler;
    private final SurfaceControl mBarrierSurfaceControl;
    private int mPendingSequenceNumber = 0;
    private int mSequenceNumber = 0;
    private final ViewRootImpl mTargetViewRootImpl;

    public SyncRtSurfaceTransactionApplierCompat(View view) {
        SurfaceControl surfaceControl = null;
        ViewRootImpl viewRootImpl = view != null ? view.getViewRootImpl() : null;
        this.mTargetViewRootImpl = viewRootImpl;
        this.mBarrierSurfaceControl = viewRootImpl != null ? viewRootImpl.getRenderSurfaceControl() : surfaceControl;
        this.mApplyHandler = new Handler(new Handler.Callback() {
            /* class com.android.systemui.shared.system.SyncRtSurfaceTransactionApplierCompat.AnonymousClass1 */

            public boolean handleMessage(Message message) {
                if (message.what != 0) {
                    return false;
                }
                SyncRtSurfaceTransactionApplierCompat.this.onApplyMessage(message.arg1);
                return true;
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onApplyMessage(int i) {
        Runnable runnable;
        this.mSequenceNumber = i;
        if (i == this.mPendingSequenceNumber && (runnable = this.mAfterApplyCallback) != null) {
            this.mAfterApplyCallback = null;
            runnable.run();
        }
    }

    public void scheduleApply(final SurfaceParams... surfaceParamsArr) {
        ViewRootImpl viewRootImpl = this.mTargetViewRootImpl;
        if (viewRootImpl != null && viewRootImpl.getView() != null) {
            final int i = this.mPendingSequenceNumber + 1;
            this.mPendingSequenceNumber = i;
            this.mTargetViewRootImpl.registerRtFrameCallback(new HardwareRenderer.FrameDrawingCallback() {
                /* class com.android.systemui.shared.system.SyncRtSurfaceTransactionApplierCompat.AnonymousClass2 */

                public void onFrameDraw(long j) {
                    if (SyncRtSurfaceTransactionApplierCompat.this.mBarrierSurfaceControl == null || !SyncRtSurfaceTransactionApplierCompat.this.mBarrierSurfaceControl.isValid()) {
                        Message.obtain(SyncRtSurfaceTransactionApplierCompat.this.mApplyHandler, 0, i, 0).sendToTarget();
                        return;
                    }
                    Trace.traceBegin(8, "Sync transaction frameNumber=" + j);
                    SurfaceControl.Transaction transaction = new SurfaceControl.Transaction();
                    for (int length = surfaceParamsArr.length + -1; length >= 0; length--) {
                        SurfaceParams surfaceParams = surfaceParamsArr[length];
                        transaction.deferTransactionUntil(surfaceParams.surface, SyncRtSurfaceTransactionApplierCompat.this.mBarrierSurfaceControl, j);
                        surfaceParams.applyTo(transaction);
                    }
                    transaction.apply();
                    Trace.traceEnd(8);
                    Message.obtain(SyncRtSurfaceTransactionApplierCompat.this.mApplyHandler, 0, i, 0).sendToTarget();
                }
            });
            this.mTargetViewRootImpl.getView().invalidate();
        }
    }

    public static class SurfaceParams {
        public final float alpha;
        public final int backgroundBlurRadius;
        public final float cornerRadius;
        private final int flags;
        public final int layer;
        private final float[] mTmpValues;
        public final Matrix matrix;
        int relativeLayer;
        SurfaceControl relativeTo;
        public final SurfaceControl surface;
        public final boolean visible;
        public final Rect windowCrop;

        public static class Builder {
            float alpha;
            int backgroundBlurRadius;
            float cornerRadius;
            int flags;
            int layer;
            Matrix matrix;
            int relativeLayer;
            SurfaceControl relativeTo;
            final SurfaceControl surface;
            boolean visible;
            Rect windowCrop;

            public Builder(SurfaceControl surfaceControl) {
                this.surface = surfaceControl;
            }

            public Builder withAlpha(float f) {
                this.alpha = f;
                this.flags |= 1;
                return this;
            }

            public Builder withMatrix(Matrix matrix2) {
                this.matrix = matrix2;
                this.flags |= 2;
                return this;
            }

            public Builder withWindowCrop(Rect rect) {
                this.windowCrop = rect;
                this.flags |= 4;
                return this;
            }

            public Builder withLayer(int i) {
                this.layer = i;
                this.flags |= 8;
                return this;
            }

            public Builder withCornerRadius(float f) {
                this.cornerRadius = f;
                this.flags |= 16;
                return this;
            }

            public Builder withVisibility(boolean z) {
                this.visible = z;
                this.flags |= 64;
                return this;
            }

            public Builder withRelativeLayer(int i, SurfaceControl surfaceControl) {
                this.relativeLayer = i;
                this.relativeTo = surfaceControl;
                this.flags |= Integer.MIN_VALUE;
                return this;
            }

            public SurfaceParams build() {
                return new SurfaceParams(this.surface, this.flags, this.alpha, this.matrix, this.windowCrop, this.layer, this.cornerRadius, this.backgroundBlurRadius, this.visible, this.relativeLayer, this.relativeTo);
            }
        }

        private SurfaceParams(SurfaceControl surfaceControl, int i, float f, Matrix matrix2, Rect rect, int i2, float f2, int i3, boolean z, int i4, SurfaceControl surfaceControl2) {
            this.mTmpValues = new float[9];
            this.flags = i;
            this.surface = surfaceControl;
            this.alpha = f;
            this.matrix = new Matrix(matrix2);
            this.windowCrop = rect != null ? new Rect(rect) : null;
            this.layer = i2;
            this.cornerRadius = f2;
            this.backgroundBlurRadius = i3;
            this.visible = z;
            this.relativeLayer = i4;
            this.relativeTo = surfaceControl2;
        }

        public void applyTo(SurfaceControl.Transaction transaction) {
            SurfaceControl surfaceControl;
            if ((this.flags & 2) != 0) {
                transaction.setMatrix(this.surface, this.matrix, this.mTmpValues);
            }
            if ((this.flags & 4) != 0) {
                transaction.setWindowCrop(this.surface, this.windowCrop);
            }
            if ((this.flags & 1) != 0) {
                transaction.setAlpha(this.surface, this.alpha);
            }
            if ((this.flags & 8) != 0) {
                transaction.setLayer(this.surface, this.layer);
            }
            if (!((this.flags & Integer.MIN_VALUE) == 0 || (surfaceControl = this.relativeTo) == null)) {
                transaction.setRelativeLayer(this.surface, surfaceControl, this.relativeLayer);
            }
            if ((this.flags & 16) != 0) {
                transaction.setCornerRadius(this.surface, this.cornerRadius);
            }
            if ((this.flags & 32) != 0) {
                transaction.setBackgroundBlurRadius(this.surface, this.backgroundBlurRadius);
            }
            if ((this.flags & 64) == 0) {
                return;
            }
            if (this.visible) {
                transaction.show(this.surface);
            } else {
                transaction.hide(this.surface);
            }
        }
    }
}
