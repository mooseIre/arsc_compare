package com.android.systemui.wm;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Slog;
import android.util.SparseArray;
import android.view.IDisplayWindowInsetsController;
import android.view.InsetsSource;
import android.view.InsetsSourceControl;
import android.view.InsetsState;
import android.view.SurfaceControl;
import android.view.WindowInsets;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import com.android.systemui.TransactionPool;
import com.android.systemui.wm.DisplayController;
import com.android.systemui.wm.DisplayImeController;
import com.miui.systemui.annotation.Inject;
import java.util.ArrayList;
import java.util.Iterator;

public class DisplayImeController implements DisplayController.OnDisplaysChangedListener {
    public static final Interpolator INTERPOLATOR = new PathInterpolator(0.4f, 0.0f, 0.2f, 1.0f);
    final Handler mHandler;
    final SparseArray<PerDisplay> mImePerDisplay = new SparseArray<>();
    final ArrayList<ImePositionProcessor> mPositionProcessors = new ArrayList<>();
    SystemWindows mSystemWindows;
    final TransactionPool mTransactionPool;

    public interface ImePositionProcessor {
        void onImeEndPositioning(int i, boolean z, SurfaceControl.Transaction transaction) {
        }

        void onImePositionChanged(int i, int i2, SurfaceControl.Transaction transaction) {
        }

        void onImeStartPositioning(int i, int i2, int i3, boolean z, SurfaceControl.Transaction transaction) {
        }
    }

    public DisplayImeController(@Inject SystemWindows systemWindows, @Inject DisplayController displayController, @Inject(tag = "main_handler") Handler handler, @Inject TransactionPool transactionPool) {
        this.mHandler = handler;
        this.mSystemWindows = systemWindows;
        this.mTransactionPool = transactionPool;
        displayController.addDisplayWindowListener(this);
    }

    public void onDisplayAdded(int i) {
        PerDisplay perDisplay = new PerDisplay(i, this.mSystemWindows.mDisplayController.getDisplayLayout(i).rotation());
        try {
            this.mSystemWindows.mWmService.setDisplayWindowInsetsController(i, perDisplay);
        } catch (RemoteException unused) {
            Slog.w("DisplayImeController", "Unable to set insets controller on display " + i);
        }
        this.mImePerDisplay.put(i, perDisplay);
    }

    public void onDisplayConfigurationChanged(int i, Configuration configuration) {
        PerDisplay perDisplay = this.mImePerDisplay.get(i);
        if (perDisplay != null && this.mSystemWindows.mDisplayController.getDisplayLayout(i).rotation() != perDisplay.mRotation && isImeShowing(i)) {
            perDisplay.startAnimation(true, false);
        }
    }

    public void onDisplayRemoved(int i) {
        try {
            this.mSystemWindows.mWmService.setDisplayWindowInsetsController(i, (IDisplayWindowInsetsController) null);
        } catch (RemoteException unused) {
            Slog.w("DisplayImeController", "Unable to remove insets controller on display " + i);
        }
        this.mImePerDisplay.remove(i);
    }

    private boolean isImeShowing(int i) {
        InsetsSource source;
        PerDisplay perDisplay = this.mImePerDisplay.get(i);
        if (perDisplay == null || (source = perDisplay.mInsetsState.getSource(13)) == null || perDisplay.mImeSourceControl == null || !source.isVisible()) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    public void dispatchPositionChanged(int i, int i2, SurfaceControl.Transaction transaction) {
        synchronized (this.mPositionProcessors) {
            Iterator<ImePositionProcessor> it = this.mPositionProcessors.iterator();
            while (it.hasNext()) {
                it.next().onImePositionChanged(i, i2, transaction);
            }
        }
    }

    /* access modifiers changed from: private */
    public void dispatchStartPositioning(int i, int i2, int i3, boolean z, SurfaceControl.Transaction transaction) {
        synchronized (this.mPositionProcessors) {
            Iterator<ImePositionProcessor> it = this.mPositionProcessors.iterator();
            while (it.hasNext()) {
                it.next().onImeStartPositioning(i, i2, i3, z, transaction);
            }
        }
    }

    /* access modifiers changed from: private */
    public void dispatchEndPositioning(int i, boolean z, SurfaceControl.Transaction transaction) {
        synchronized (this.mPositionProcessors) {
            Iterator<ImePositionProcessor> it = this.mPositionProcessors.iterator();
            while (it.hasNext()) {
                it.next().onImeEndPositioning(i, z, transaction);
            }
        }
    }

    public void addPositionProcessor(ImePositionProcessor imePositionProcessor) {
        synchronized (this.mPositionProcessors) {
            if (!this.mPositionProcessors.contains(imePositionProcessor)) {
                this.mPositionProcessors.add(imePositionProcessor);
            }
        }
    }

    class PerDisplay extends IDisplayWindowInsetsController.Stub {
        ValueAnimator mAnimation = null;
        int mAnimationDirection = 0;
        final int mDisplayId;
        boolean mImeShowing = false;
        InsetsSourceControl mImeSourceControl = null;
        final InsetsState mInsetsState = new InsetsState();
        int mRotation = 0;

        PerDisplay(int i, int i2) {
            this.mDisplayId = i;
            this.mRotation = i2;
        }

        public void insetsChanged(InsetsState insetsState) {
            if (!this.mInsetsState.equals(insetsState)) {
                InsetsSource source = insetsState.getSource(13);
                Rect frame = source.getFrame();
                Rect frame2 = this.mInsetsState.getSource(13).getFrame();
                this.mInsetsState.set(insetsState, true);
                if (this.mImeShowing && !frame.equals(frame2) && source.isVisible()) {
                    startAnimation(this.mImeShowing, true);
                }
            }
        }

        public void insetsControlChanged(InsetsState insetsState, InsetsSourceControl[] insetsSourceControlArr) {
            insetsChanged(insetsState);
            if (insetsSourceControlArr != null) {
                for (InsetsSourceControl insetsSourceControl : insetsSourceControlArr) {
                    if (insetsSourceControl != null && insetsSourceControl.getType() == 13) {
                        DisplayImeController.this.mHandler.post(new Runnable(insetsSourceControl) {
                            public final /* synthetic */ InsetsSourceControl f$1;

                            {
                                this.f$1 = r2;
                            }

                            public final void run() {
                                DisplayImeController.PerDisplay.this.lambda$insetsControlChanged$0$DisplayImeController$PerDisplay(this.f$1);
                            }
                        });
                    }
                }
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$insetsControlChanged$0 */
        public /* synthetic */ void lambda$insetsControlChanged$0$DisplayImeController$PerDisplay(InsetsSourceControl insetsSourceControl) {
            InsetsSourceControl insetsSourceControl2 = this.mImeSourceControl;
            Point surfacePosition = insetsSourceControl2 != null ? insetsSourceControl2.getSurfacePosition() : null;
            this.mImeSourceControl = insetsSourceControl;
            if (!insetsSourceControl.getSurfacePosition().equals(surfacePosition) && this.mAnimation != null) {
                startAnimation(this.mImeShowing, true);
            }
        }

        public void showInsets(int i, boolean z) {
            if ((i & WindowInsets.Type.ime()) != 0) {
                startAnimation(true, false);
            }
        }

        public void hideInsets(int i, boolean z) {
            if ((i & WindowInsets.Type.ime()) != 0) {
                startAnimation(false, false);
            }
        }

        private void setVisibleDirectly(boolean z) {
            this.mInsetsState.getSource(13).setVisible(z);
            try {
                DisplayImeController.this.mSystemWindows.mWmService.modifyDisplayWindowInsets(this.mDisplayId, this.mInsetsState);
            } catch (RemoteException unused) {
            }
        }

        /* access modifiers changed from: private */
        public int imeTop(InsetsSource insetsSource, float f) {
            return insetsSource.getFrame().top + ((int) f);
        }

        /* access modifiers changed from: private */
        public void startAnimation(boolean z, boolean z2) {
            InsetsSource source = this.mInsetsState.getSource(13);
            if (source != null && this.mImeSourceControl != null) {
                DisplayImeController.this.mHandler.post(new Runnable(z, z2, source) {
                    public final /* synthetic */ boolean f$1;
                    public final /* synthetic */ boolean f$2;
                    public final /* synthetic */ InsetsSource f$3;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                        this.f$3 = r4;
                    }

                    public final void run() {
                        DisplayImeController.PerDisplay.this.lambda$startAnimation$2$DisplayImeController$PerDisplay(this.f$1, this.f$2, this.f$3);
                    }
                });
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$startAnimation$2 */
        public /* synthetic */ void lambda$startAnimation$2$DisplayImeController$PerDisplay(boolean z, boolean z2, InsetsSource insetsSource) {
            boolean z3;
            boolean z4 = z;
            if (!z2 && this.mAnimationDirection == 1 && z4) {
                return;
            }
            if (this.mAnimationDirection != 2 || z4) {
                float f = 0.0f;
                ValueAnimator valueAnimator = this.mAnimation;
                if (valueAnimator != null) {
                    if (valueAnimator.isRunning()) {
                        f = ((Float) this.mAnimation.getAnimatedValue()).floatValue();
                        z3 = true;
                    } else {
                        z3 = false;
                    }
                    this.mAnimation.cancel();
                } else {
                    z3 = false;
                }
                final float f2 = (float) this.mImeSourceControl.getSurfacePosition().y;
                float f3 = (float) this.mImeSourceControl.getSurfacePosition().x;
                final float height = f2 + ((float) insetsSource.getFrame().height());
                float f4 = z4 ? height : f2;
                final float f5 = z4 ? f2 : height;
                if (this.mAnimationDirection == 0 && this.mImeShowing && z4) {
                    f = f2;
                    z3 = true;
                }
                this.mAnimationDirection = z4 ? 1 : 2;
                this.mImeShowing = z4;
                ValueAnimator ofFloat = ValueAnimator.ofFloat(new float[]{f4, f5});
                this.mAnimation = ofFloat;
                ofFloat.setDuration(z4 ? 275 : 340);
                if (z3) {
                    this.mAnimation.setCurrentFraction((f - f4) / (f5 - f4));
                }
                this.mAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(f3, insetsSource) {
                    public final /* synthetic */ float f$1;
                    public final /* synthetic */ InsetsSource f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                        DisplayImeController.PerDisplay.this.lambda$startAnimation$1$DisplayImeController$PerDisplay(this.f$1, this.f$2, valueAnimator);
                    }
                });
                this.mAnimation.setInterpolator(DisplayImeController.INTERPOLATOR);
                final float f6 = f3;
                final float f7 = f4;
                final InsetsSource insetsSource2 = insetsSource;
                this.mAnimation.addListener(new AnimatorListenerAdapter() {
                    private boolean mCancelled = false;

                    public void onAnimationStart(Animator animator) {
                        SurfaceControl.Transaction acquire = DisplayImeController.this.mTransactionPool.acquire();
                        acquire.setPosition(PerDisplay.this.mImeSourceControl.getLeash(), f6, f7);
                        PerDisplay perDisplay = PerDisplay.this;
                        DisplayImeController.this.dispatchStartPositioning(perDisplay.mDisplayId, perDisplay.imeTop(insetsSource2, height), PerDisplay.this.imeTop(insetsSource2, f2), PerDisplay.this.mAnimationDirection == 1, acquire);
                        PerDisplay perDisplay2 = PerDisplay.this;
                        if (perDisplay2.mAnimationDirection == 1) {
                            acquire.show(perDisplay2.mImeSourceControl.getLeash());
                        }
                        acquire.apply();
                        DisplayImeController.this.mTransactionPool.release(acquire);
                    }

                    public void onAnimationCancel(Animator animator) {
                        this.mCancelled = true;
                    }

                    public void onAnimationEnd(Animator animator) {
                        SurfaceControl.Transaction acquire = DisplayImeController.this.mTransactionPool.acquire();
                        if (!this.mCancelled) {
                            acquire.setPosition(PerDisplay.this.mImeSourceControl.getLeash(), f6, f5);
                        }
                        PerDisplay perDisplay = PerDisplay.this;
                        DisplayImeController.this.dispatchEndPositioning(perDisplay.mDisplayId, this.mCancelled, acquire);
                        PerDisplay perDisplay2 = PerDisplay.this;
                        if (perDisplay2.mAnimationDirection == 2 && !this.mCancelled) {
                            acquire.hide(perDisplay2.mImeSourceControl.getLeash());
                        }
                        acquire.apply();
                        DisplayImeController.this.mTransactionPool.release(acquire);
                        PerDisplay perDisplay3 = PerDisplay.this;
                        perDisplay3.mAnimationDirection = 0;
                        perDisplay3.mAnimation = null;
                    }
                });
                if (!z4) {
                    setVisibleDirectly(false);
                }
                this.mAnimation.start();
                if (z4) {
                    setVisibleDirectly(true);
                }
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$startAnimation$1 */
        public /* synthetic */ void lambda$startAnimation$1$DisplayImeController$PerDisplay(float f, InsetsSource insetsSource, ValueAnimator valueAnimator) {
            SurfaceControl.Transaction acquire = DisplayImeController.this.mTransactionPool.acquire();
            float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
            acquire.setPosition(this.mImeSourceControl.getLeash(), f, floatValue);
            DisplayImeController.this.dispatchPositionChanged(this.mDisplayId, imeTop(insetsSource, floatValue), acquire);
            acquire.apply();
            DisplayImeController.this.mTransactionPool.release(acquire);
        }
    }
}
