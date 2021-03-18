package com.android.systemui.statusbar;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.text.format.DateFormat;
import android.util.FloatProperty;
import android.util.Log;
import android.view.animation.Interpolator;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.logging.UiEventLogger;
import com.android.keyguard.wallpaper.MiuiWallpaperClient;
import com.android.systemui.DejankUtils;
import com.android.systemui.Dependency;
import com.android.systemui.Dumpable;
import com.android.systemui.Interpolators;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.SysuiStatusBarStateController;
import com.android.systemui.statusbar.policy.CallbackController;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.function.Predicate;

public class StatusBarStateControllerImpl implements SysuiStatusBarStateController, CallbackController<StatusBarStateController.StateListener>, Dumpable {
    private static final FloatProperty<StatusBarStateControllerImpl> SET_DARK_AMOUNT_PROPERTY = new FloatProperty<StatusBarStateControllerImpl>("mDozeAmount") {
        /* class com.android.systemui.statusbar.StatusBarStateControllerImpl.AnonymousClass1 */

        public void setValue(StatusBarStateControllerImpl statusBarStateControllerImpl, float f) {
            statusBarStateControllerImpl.setDozeAmountInternal(f);
        }

        public Float get(StatusBarStateControllerImpl statusBarStateControllerImpl) {
            return Float.valueOf(statusBarStateControllerImpl.mDozeAmount);
        }
    };
    private static final Comparator<SysuiStatusBarStateController.RankedListener> sComparator = Comparator.comparingInt($$Lambda$StatusBarStateControllerImpl$7y8VOe44iFeEd9HPscwVVB7kUfw.INSTANCE);
    private ValueAnimator mDarkAnimator;
    private float mDozeAmount;
    private float mDozeAmountTarget;
    private Interpolator mDozeInterpolator;
    private HistoricalState[] mHistoricalRecords;
    private int mHistoryIndex;
    private boolean mIsDozing;
    private boolean mIsFullscreen;
    private boolean mIsImmersive;
    private boolean mKeyguardRequested;
    private int mLastState;
    private boolean mLeaveOpenOnKeyguardHide;
    private final ArrayList<SysuiStatusBarStateController.RankedListener> mListeners = new ArrayList<>();
    private boolean mPulsing;
    private int mState;
    private final UiEventLogger mUiEventLogger;

    public StatusBarStateControllerImpl(UiEventLogger uiEventLogger) {
        this.mHistoryIndex = 0;
        this.mHistoricalRecords = new HistoricalState[32];
        this.mIsFullscreen = false;
        this.mIsImmersive = false;
        this.mDozeInterpolator = Interpolators.FAST_OUT_SLOW_IN;
        this.mUiEventLogger = uiEventLogger;
        for (int i = 0; i < 32; i++) {
            this.mHistoricalRecords[i] = new HistoricalState();
        }
    }

    @Override // com.android.systemui.plugins.statusbar.StatusBarStateController
    public int getState() {
        return this.mState;
    }

    @Override // com.android.systemui.statusbar.SysuiStatusBarStateController
    public boolean setState(int i) {
        if (i > 3 || i < 0) {
            throw new IllegalArgumentException("Invalid state " + i);
        }
        int i2 = this.mState;
        if (i == i2) {
            return false;
        }
        recordHistoricalState(i, i2);
        if (this.mState == 0 && i == 2) {
            Log.e("SbStateController", "Invalid state transition: SHADE -> SHADE_LOCKED", new Throwable());
        }
        synchronized (this.mListeners) {
            String str = getClass().getSimpleName() + "#setState(" + i + ")";
            DejankUtils.startDetectingBlockingIpcs(str);
            Iterator it = new ArrayList(this.mListeners).iterator();
            while (it.hasNext()) {
                ((SysuiStatusBarStateController.RankedListener) it.next()).mListener.onStatePreChange(this.mState, i);
            }
            this.mLastState = this.mState;
            this.mState = i;
            this.mUiEventLogger.log(StatusBarStateEvent.fromState(i));
            Iterator it2 = new ArrayList(this.mListeners).iterator();
            while (it2.hasNext()) {
                ((SysuiStatusBarStateController.RankedListener) it2.next()).mListener.onStateChanged(this.mState);
            }
            Iterator it3 = new ArrayList(this.mListeners).iterator();
            while (it3.hasNext()) {
                ((SysuiStatusBarStateController.RankedListener) it3.next()).mListener.onStatePostChange();
            }
            DejankUtils.stopDetectingBlockingIpcs(str);
        }
        return true;
    }

    @Override // com.android.systemui.plugins.statusbar.StatusBarStateController
    public boolean isDozing() {
        return this.mIsDozing;
    }

    @Override // com.android.systemui.plugins.statusbar.StatusBarStateController
    public boolean isPulsing() {
        return this.mPulsing;
    }

    @Override // com.android.systemui.plugins.statusbar.StatusBarStateController
    public float getDozeAmount() {
        return this.mDozeAmount;
    }

    @Override // com.android.systemui.statusbar.SysuiStatusBarStateController
    public float getInterpolatedDozeAmount() {
        return this.mDozeInterpolator.getInterpolation(this.mDozeAmount);
    }

    @Override // com.android.systemui.statusbar.SysuiStatusBarStateController
    public boolean setIsDozing(boolean z) {
        if (this.mIsDozing == z) {
            return false;
        }
        this.mIsDozing = z;
        synchronized (this.mListeners) {
            String str = getClass().getSimpleName() + "#setIsDozing";
            DejankUtils.startDetectingBlockingIpcs(str);
            Iterator it = new ArrayList(this.mListeners).iterator();
            while (it.hasNext()) {
                ((SysuiStatusBarStateController.RankedListener) it.next()).mListener.onDozingChanged(z);
            }
            DejankUtils.stopDetectingBlockingIpcs(str);
            ((MiuiWallpaperClient) Dependency.get(MiuiWallpaperClient.class)).onDozingChanged(z);
        }
        return true;
    }

    @Override // com.android.systemui.statusbar.SysuiStatusBarStateController
    public void setDozeAmount(float f, boolean z) {
        ValueAnimator valueAnimator = this.mDarkAnimator;
        if (valueAnimator != null && valueAnimator.isRunning()) {
            if (!z || this.mDozeAmountTarget != f) {
                this.mDarkAnimator.cancel();
            } else {
                return;
            }
        }
        this.mDozeAmountTarget = f;
        if (z) {
            startDozeAnimation();
        } else {
            setDozeAmountInternal(f);
        }
    }

    private void startDozeAnimation() {
        Interpolator interpolator;
        float f = this.mDozeAmount;
        if (f == 0.0f || f == 1.0f) {
            if (this.mIsDozing) {
                interpolator = Interpolators.FAST_OUT_SLOW_IN;
            } else {
                interpolator = Interpolators.TOUCH_RESPONSE_REVERSE;
            }
            this.mDozeInterpolator = interpolator;
        }
        ObjectAnimator ofFloat = ObjectAnimator.ofFloat(this, SET_DARK_AMOUNT_PROPERTY, this.mDozeAmountTarget);
        this.mDarkAnimator = ofFloat;
        ofFloat.setInterpolator(Interpolators.LINEAR);
        this.mDarkAnimator.setDuration(500L);
        this.mDarkAnimator.start();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setDozeAmountInternal(float f) {
        this.mDozeAmount = f;
        float interpolation = this.mDozeInterpolator.getInterpolation(f);
        synchronized (this.mListeners) {
            String str = getClass().getSimpleName() + "#setDozeAmount";
            DejankUtils.startDetectingBlockingIpcs(str);
            Iterator it = new ArrayList(this.mListeners).iterator();
            while (it.hasNext()) {
                ((SysuiStatusBarStateController.RankedListener) it.next()).mListener.onDozeAmountChanged(this.mDozeAmount, interpolation);
            }
            DejankUtils.stopDetectingBlockingIpcs(str);
        }
    }

    @Override // com.android.systemui.statusbar.SysuiStatusBarStateController
    public boolean goingToFullShade() {
        return this.mState == 0 && this.mLeaveOpenOnKeyguardHide;
    }

    @Override // com.android.systemui.statusbar.SysuiStatusBarStateController
    public void setLeaveOpenOnKeyguardHide(boolean z) {
        this.mLeaveOpenOnKeyguardHide = z;
    }

    @Override // com.android.systemui.statusbar.SysuiStatusBarStateController
    public boolean leaveOpenOnKeyguardHide() {
        return this.mLeaveOpenOnKeyguardHide;
    }

    @Override // com.android.systemui.statusbar.SysuiStatusBarStateController
    public boolean fromShadeLocked() {
        return this.mLastState == 2;
    }

    @Override // com.android.systemui.plugins.statusbar.StatusBarStateController
    public void addCallback(StatusBarStateController.StateListener stateListener) {
        synchronized (this.mListeners) {
            addListenerInternalLocked(stateListener, Integer.MAX_VALUE);
        }
    }

    @Override // com.android.systemui.statusbar.SysuiStatusBarStateController
    @Deprecated
    public void addCallback(StatusBarStateController.StateListener stateListener, int i) {
        synchronized (this.mListeners) {
            addListenerInternalLocked(stateListener, i);
        }
    }

    @GuardedBy({"mListeners"})
    private void addListenerInternalLocked(StatusBarStateController.StateListener stateListener, int i) {
        Iterator<SysuiStatusBarStateController.RankedListener> it = this.mListeners.iterator();
        while (it.hasNext()) {
            if (it.next().mListener.equals(stateListener)) {
                return;
            }
        }
        this.mListeners.add(new SysuiStatusBarStateController.RankedListener(stateListener, i));
        this.mListeners.sort(sComparator);
    }

    @Override // com.android.systemui.plugins.statusbar.StatusBarStateController
    public void removeCallback(StatusBarStateController.StateListener stateListener) {
        synchronized (this.mListeners) {
            this.mListeners.removeIf(new Predicate() {
                /* class com.android.systemui.statusbar.$$Lambda$StatusBarStateControllerImpl$TAyHbKlLKq3j8NJBke8nEPo5OK4 */

                @Override // java.util.function.Predicate
                public final boolean test(Object obj) {
                    return ((SysuiStatusBarStateController.RankedListener) obj).mListener.equals(StatusBarStateController.StateListener.this);
                }
            });
        }
    }

    @Override // com.android.systemui.statusbar.SysuiStatusBarStateController
    public void setKeyguardRequested(boolean z) {
        this.mKeyguardRequested = z;
    }

    @Override // com.android.systemui.statusbar.SysuiStatusBarStateController
    public boolean isKeyguardRequested() {
        return this.mKeyguardRequested;
    }

    @Override // com.android.systemui.statusbar.SysuiStatusBarStateController
    public void setFullscreenState(boolean z, boolean z2) {
        if (this.mIsFullscreen != z || this.mIsImmersive != z2) {
            this.mIsFullscreen = z;
            this.mIsImmersive = z2;
            synchronized (this.mListeners) {
                Iterator it = new ArrayList(this.mListeners).iterator();
                while (it.hasNext()) {
                    ((SysuiStatusBarStateController.RankedListener) it.next()).mListener.onFullscreenStateChanged(z, z2);
                }
            }
        }
    }

    @Override // com.android.systemui.statusbar.SysuiStatusBarStateController
    public void setPulsing(boolean z) {
        if (this.mPulsing != z) {
            this.mPulsing = z;
            synchronized (this.mListeners) {
                Iterator it = new ArrayList(this.mListeners).iterator();
                while (it.hasNext()) {
                    ((SysuiStatusBarStateController.RankedListener) it.next()).mListener.onPulsingChanged(z);
                }
            }
        }
    }

    public static String describe(int i) {
        return StatusBarState.toShortString(i);
    }

    @Override // com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("StatusBarStateController: ");
        printWriter.println(" mState=" + this.mState + " (" + describe(this.mState) + ")");
        printWriter.println(" mLastState=" + this.mLastState + " (" + describe(this.mLastState) + ")");
        StringBuilder sb = new StringBuilder();
        sb.append(" mLeaveOpenOnKeyguardHide=");
        sb.append(this.mLeaveOpenOnKeyguardHide);
        printWriter.println(sb.toString());
        printWriter.println(" mKeyguardRequested=" + this.mKeyguardRequested);
        printWriter.println(" mIsDozing=" + this.mIsDozing);
        printWriter.println(" Historical states:");
        int i = 0;
        for (int i2 = 0; i2 < 32; i2++) {
            if (this.mHistoricalRecords[i2].mTimestamp != 0) {
                i++;
            }
        }
        for (int i3 = this.mHistoryIndex + 32; i3 >= ((this.mHistoryIndex + 32) - i) + 1; i3 += -1) {
            printWriter.println("  (" + (((this.mHistoryIndex + 32) - i3) + 1) + ")" + this.mHistoricalRecords[i3 & 31]);
        }
    }

    private void recordHistoricalState(int i, int i2) {
        int i3 = (this.mHistoryIndex + 1) % 32;
        this.mHistoryIndex = i3;
        HistoricalState historicalState = this.mHistoricalRecords[i3];
        historicalState.mState = i;
        historicalState.mLastState = i2;
        historicalState.mTimestamp = System.currentTimeMillis();
    }

    /* access modifiers changed from: private */
    public static class HistoricalState {
        int mLastState;
        int mState;
        long mTimestamp;

        private HistoricalState() {
        }

        public String toString() {
            if (this.mTimestamp != 0) {
                return "state=" + this.mState + " (" + StatusBarStateControllerImpl.describe(this.mState) + ")" + "lastState=" + this.mLastState + " (" + StatusBarStateControllerImpl.describe(this.mLastState) + ")" + "timestamp=" + DateFormat.format("MM-dd HH:mm:ss", this.mTimestamp);
            }
            return "Empty " + HistoricalState.class.getSimpleName();
        }
    }
}
