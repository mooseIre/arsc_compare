package com.android.systemui.wm;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.os.RemoteException;
import android.os.ServiceManager;
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
import com.android.internal.view.IInputMethodManager;
import com.android.systemui.TransactionPool;
import com.android.systemui.wm.DisplayController;
import com.android.systemui.wm.DisplayImeController;
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
        default void onImeEndPositioning(int i, boolean z, SurfaceControl.Transaction transaction) {
        }

        default void onImePositionChanged(int i, int i2, SurfaceControl.Transaction transaction) {
        }

        default int onImeStartPositioning(int i, int i2, int i3, boolean z, boolean z2, SurfaceControl.Transaction transaction) {
            return 0;
        }
    }

    public DisplayImeController(SystemWindows systemWindows, DisplayController displayController, Handler handler, TransactionPool transactionPool) {
        this.mHandler = handler;
        this.mSystemWindows = systemWindows;
        this.mTransactionPool = transactionPool;
        displayController.addDisplayWindowListener(this);
    }

    @Override // com.android.systemui.wm.DisplayController.OnDisplaysChangedListener
    public void onDisplayAdded(int i) {
        PerDisplay perDisplay = new PerDisplay(i, this.mSystemWindows.mDisplayController.getDisplayLayout(i).rotation());
        try {
            this.mSystemWindows.mWmService.setDisplayWindowInsetsController(i, perDisplay);
        } catch (RemoteException unused) {
            Slog.w("DisplayImeController", "Unable to set insets controller on display " + i);
        }
        this.mImePerDisplay.put(i, perDisplay);
    }

    @Override // com.android.systemui.wm.DisplayController.OnDisplaysChangedListener
    public void onDisplayConfigurationChanged(int i, Configuration configuration) {
        PerDisplay perDisplay = this.mImePerDisplay.get(i);
        if (perDisplay != null && this.mSystemWindows.mDisplayController.getDisplayLayout(i).rotation() != perDisplay.mRotation && isImeShowing(i)) {
            perDisplay.startAnimation(true, false);
        }
    }

    @Override // com.android.systemui.wm.DisplayController.OnDisplaysChangedListener
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
    /* access modifiers changed from: public */
    private void dispatchPositionChanged(int i, int i2, SurfaceControl.Transaction transaction) {
        synchronized (this.mPositionProcessors) {
            Iterator<ImePositionProcessor> it = this.mPositionProcessors.iterator();
            while (it.hasNext()) {
                it.next().onImePositionChanged(i, i2, transaction);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int dispatchStartPositioning(int i, int i2, int i3, boolean z, boolean z2, SurfaceControl.Transaction transaction) {
        int i4;
        synchronized (this.mPositionProcessors) {
            i4 = 0;
            Iterator<ImePositionProcessor> it = this.mPositionProcessors.iterator();
            while (it.hasNext()) {
                i4 |= it.next().onImeStartPositioning(i, i2, i3, z, z2, transaction);
            }
        }
        return i4;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dispatchEndPositioning(int i, boolean z, SurfaceControl.Transaction transaction) {
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

    /* access modifiers changed from: package-private */
    public class PerDisplay extends IDisplayWindowInsetsController.Stub {
        boolean mAnimateAlpha = true;
        ValueAnimator mAnimation = null;
        int mAnimationDirection = 0;
        final int mDisplayId;
        final Rect mImeFrame = new Rect();
        boolean mImeShowing = false;
        InsetsSourceControl mImeSourceControl = null;
        final InsetsState mInsetsState = new InsetsState();
        int mRotation = 0;

        PerDisplay(int i, int i2) {
            this.mDisplayId = i;
            this.mRotation = i2;
        }

        public void insetsChanged(InsetsState insetsState) {
            DisplayImeController.this.mHandler.post(new Runnable(insetsState) {
                /* class com.android.systemui.wm.$$Lambda$DisplayImeController$PerDisplay$RsnC99lStI9aBLStiHw26Ct1bkM */
                public final /* synthetic */ InsetsState f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    DisplayImeController.PerDisplay.this.lambda$insetsChanged$0$DisplayImeController$PerDisplay(this.f$1);
                }
            });
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$insetsChanged$0 */
        public /* synthetic */ void lambda$insetsChanged$0$DisplayImeController$PerDisplay(InsetsState insetsState) {
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
                            /* class com.android.systemui.wm.$$Lambda$DisplayImeController$PerDisplay$VKegz7htjpQCkeKeoZWkho3eGY */
                            public final /* synthetic */ InsetsSourceControl f$1;

                            {
                                this.f$1 = r2;
                            }

                            public final void run() {
                                DisplayImeController.PerDisplay.this.lambda$insetsControlChanged$1$DisplayImeController$PerDisplay(this.f$1);
                            }
                        });
                    }
                }
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$insetsControlChanged$1 */
        public /* synthetic */ void lambda$insetsControlChanged$1$DisplayImeController$PerDisplay(InsetsSourceControl insetsSourceControl) {
            InsetsSourceControl insetsSourceControl2 = this.mImeSourceControl;
            Point surfacePosition = insetsSourceControl2 != null ? insetsSourceControl2.getSurfacePosition() : null;
            this.mImeSourceControl = insetsSourceControl;
            if (!insetsSourceControl.getSurfacePosition().equals(surfacePosition) && this.mAnimation != null) {
                startAnimation(this.mImeShowing, true);
            } else if (!this.mImeShowing) {
                DisplayImeController.this.removeImeSurface();
            }
        }

        public void showInsets(int i, boolean z) {
            if ((i & WindowInsets.Type.ime()) != 0) {
                DisplayImeController.this.mHandler.post(new Runnable() {
                    /* class com.android.systemui.wm.$$Lambda$DisplayImeController$PerDisplay$FD_9ZTxZkOna83ACP1uBpyvywmw */

                    public final void run() {
                        DisplayImeController.PerDisplay.this.lambda$showInsets$2$DisplayImeController$PerDisplay();
                    }
                });
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$showInsets$2 */
        public /* synthetic */ void lambda$showInsets$2$DisplayImeController$PerDisplay() {
            startAnimation(true, false);
        }

        public void hideInsets(int i, boolean z) {
            if ((i & WindowInsets.Type.ime()) != 0) {
                DisplayImeController.this.mHandler.post(new Runnable() {
                    /* class com.android.systemui.wm.$$Lambda$DisplayImeController$PerDisplay$HI1i4VuaB2mt72uGGByWWGOYAoU */

                    public final void run() {
                        DisplayImeController.PerDisplay.this.lambda$hideInsets$3$DisplayImeController$PerDisplay();
                    }
                });
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$hideInsets$3 */
        public /* synthetic */ void lambda$hideInsets$3$DisplayImeController$PerDisplay() {
            startAnimation(false, false);
        }

        private void setVisibleDirectly(boolean z) {
            this.mInsetsState.getSource(13).setVisible(z);
            try {
                DisplayImeController.this.mSystemWindows.mWmService.modifyDisplayWindowInsets(this.mDisplayId, this.mInsetsState);
            } catch (RemoteException unused) {
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private int imeTop(float f) {
            return this.mImeFrame.top + ((int) f);
        }

        private boolean calcIsFloating(InsetsSource insetsSource) {
            Rect frame = insetsSource.getFrame();
            if (frame.height() != 0 && frame.height() > DisplayImeController.this.mSystemWindows.mDisplayController.getDisplayLayout(this.mDisplayId).navBarFrameHeight()) {
                return false;
            }
            return true;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void startAnimation(boolean z, boolean z2) {
            boolean z3;
            InsetsSource source = this.mInsetsState.getSource(13);
            if (source != null && this.mImeSourceControl != null) {
                Rect frame = source.getFrame();
                final boolean z4 = calcIsFloating(source) && z;
                if (z4) {
                    this.mImeFrame.set(frame);
                    this.mImeFrame.bottom -= (int) (DisplayImeController.this.mSystemWindows.mDisplayController.getDisplayLayout(this.mDisplayId).density() * -80.0f);
                } else if (frame.height() != 0) {
                    this.mImeFrame.set(frame);
                }
                if (!z2 && this.mAnimationDirection == 1 && z) {
                    return;
                }
                if (this.mAnimationDirection != 2 || z) {
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
                    final float f3 = (float) this.mImeSourceControl.getSurfacePosition().x;
                    final float height = f2 + ((float) this.mImeFrame.height());
                    final float f4 = z ? height : f2;
                    final float f5 = z ? f2 : height;
                    if (this.mAnimationDirection == 0 && this.mImeShowing && z) {
                        f = f2;
                        z3 = true;
                    }
                    this.mAnimationDirection = z ? 1 : 2;
                    this.mImeShowing = z;
                    ValueAnimator ofFloat = ValueAnimator.ofFloat(f4, f5);
                    this.mAnimation = ofFloat;
                    ofFloat.setDuration(z ? 275 : 340);
                    if (z3) {
                        this.mAnimation.setCurrentFraction((f - f4) / (f5 - f4));
                    }
                    this.mAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(f3, z4, height, f2) {
                        /* class com.android.systemui.wm.$$Lambda$DisplayImeController$PerDisplay$J4UVZpw7ZmqU_1hqrDGd2bjaNE */
                        public final /* synthetic */ float f$1;
                        public final /* synthetic */ boolean f$2;
                        public final /* synthetic */ float f$3;
                        public final /* synthetic */ float f$4;

                        {
                            this.f$1 = r2;
                            this.f$2 = r3;
                            this.f$3 = r4;
                            this.f$4 = r5;
                        }

                        public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                            DisplayImeController.PerDisplay.this.lambda$startAnimation$4$DisplayImeController$PerDisplay(this.f$1, this.f$2, this.f$3, this.f$4, valueAnimator);
                        }
                    });
                    this.mAnimation.setInterpolator(DisplayImeController.INTERPOLATOR);
                    this.mAnimation.addListener(new AnimatorListenerAdapter() {
                        /* class com.android.systemui.wm.DisplayImeController.PerDisplay.AnonymousClass1 */
                        private boolean mCancelled = false;

                        public void onAnimationStart(Animator animator) {
                            float f;
                            SurfaceControl.Transaction acquire = DisplayImeController.this.mTransactionPool.acquire();
                            acquire.setPosition(PerDisplay.this.mImeSourceControl.getLeash(), f3, f4);
                            PerDisplay perDisplay = PerDisplay.this;
                            boolean z = false;
                            int dispatchStartPositioning = DisplayImeController.this.dispatchStartPositioning(perDisplay.mDisplayId, perDisplay.imeTop(height), PerDisplay.this.imeTop(f2), PerDisplay.this.mAnimationDirection == 1, z4, acquire);
                            PerDisplay perDisplay2 = PerDisplay.this;
                            if ((dispatchStartPositioning & 1) == 0) {
                                z = true;
                            }
                            perDisplay2.mAnimateAlpha = z;
                            if (PerDisplay.this.mAnimateAlpha || z4) {
                                float f2 = f4;
                                float f3 = height;
                                f = (f2 - f3) / (f2 - f3);
                            } else {
                                f = 1.0f;
                            }
                            acquire.setAlpha(PerDisplay.this.mImeSourceControl.getLeash(), f);
                            PerDisplay perDisplay3 = PerDisplay.this;
                            if (perDisplay3.mAnimationDirection == 1) {
                                acquire.show(perDisplay3.mImeSourceControl.getLeash());
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
                                acquire.setPosition(PerDisplay.this.mImeSourceControl.getLeash(), f3, f5);
                                acquire.setAlpha(PerDisplay.this.mImeSourceControl.getLeash(), 1.0f);
                            }
                            PerDisplay perDisplay = PerDisplay.this;
                            DisplayImeController.this.dispatchEndPositioning(perDisplay.mDisplayId, this.mCancelled, acquire);
                            PerDisplay perDisplay2 = PerDisplay.this;
                            if (perDisplay2.mAnimationDirection == 2 && !this.mCancelled) {
                                acquire.hide(perDisplay2.mImeSourceControl.getLeash());
                                DisplayImeController.this.removeImeSurface();
                            }
                            acquire.apply();
                            DisplayImeController.this.mTransactionPool.release(acquire);
                            PerDisplay perDisplay3 = PerDisplay.this;
                            perDisplay3.mAnimationDirection = 0;
                            perDisplay3.mAnimation = null;
                        }
                    });
                    if (!z) {
                        setVisibleDirectly(false);
                    }
                    this.mAnimation.start();
                    if (z) {
                        setVisibleDirectly(true);
                    }
                }
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$startAnimation$4 */
        public /* synthetic */ void lambda$startAnimation$4$DisplayImeController$PerDisplay(float f, boolean z, float f2, float f3, ValueAnimator valueAnimator) {
            SurfaceControl.Transaction acquire = DisplayImeController.this.mTransactionPool.acquire();
            float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
            acquire.setPosition(this.mImeSourceControl.getLeash(), f, floatValue);
            acquire.setAlpha(this.mImeSourceControl.getLeash(), (this.mAnimateAlpha || z) ? (floatValue - f2) / (f3 - f2) : 1.0f);
            DisplayImeController.this.dispatchPositionChanged(this.mDisplayId, imeTop(floatValue), acquire);
            acquire.apply();
            DisplayImeController.this.mTransactionPool.release(acquire);
        }
    }

    /* access modifiers changed from: package-private */
    public void removeImeSurface() {
        IInputMethodManager imms = getImms();
        if (imms != null) {
            try {
                imms.removeImeSurface();
            } catch (RemoteException e) {
                Slog.e("DisplayImeController", "Failed to remove IME surface.", e);
            }
        }
    }

    public IInputMethodManager getImms() {
        return IInputMethodManager.Stub.asInterface(ServiceManager.getService("input_method"));
    }
}
