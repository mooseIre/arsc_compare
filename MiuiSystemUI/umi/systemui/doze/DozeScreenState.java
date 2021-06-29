package com.android.systemui.doze;

import android.os.Handler;
import android.util.Log;
import com.android.systemui.doze.DozeMachine;
import com.android.systemui.statusbar.phone.DozeParameters;
import com.android.systemui.util.wakelock.SettableWakeLock;
import com.android.systemui.util.wakelock.WakeLock;

public class DozeScreenState implements DozeMachine.Part {
    private static final boolean DEBUG = DozeService.DEBUG;
    private final Runnable mApplyPendingScreenState = new Runnable() {
        /* class com.android.systemui.doze.$$Lambda$DozeScreenState$eRrLSFQgxPfG2I_jJDfdCLwKzVE */

        public final void run() {
            DozeScreenState.this.applyPendingScreenState();
        }
    };
    private final DozeHost mDozeHost;
    private final DozeMachine.Service mDozeService;
    private final Handler mHandler;
    private final DozeParameters mParameters;
    private int mPendingScreenState = 0;
    private SettableWakeLock mWakeLock;

    public DozeScreenState(DozeMachine.Service service, Handler handler, DozeHost dozeHost, DozeParameters dozeParameters, WakeLock wakeLock) {
        this.mDozeService = service;
        this.mHandler = handler;
        this.mParameters = dozeParameters;
        this.mDozeHost = dozeHost;
        this.mWakeLock = new SettableWakeLock(wakeLock, "DozeScreenState");
    }

    @Override // com.android.systemui.doze.DozeMachine.Part
    public void transitionTo(DozeMachine.State state, DozeMachine.State state2) {
        int screenState = state2.screenState(this.mParameters);
        this.mDozeHost.cancelGentleSleep();
        boolean z = false;
        if (state2 == DozeMachine.State.FINISH) {
            this.mPendingScreenState = 0;
            this.mHandler.removeCallbacks(this.mApplyPendingScreenState);
            lambda$transitionTo$0(screenState);
            this.mWakeLock.setAcquired(false);
        } else if (screenState != 0) {
            boolean hasCallbacks = this.mHandler.hasCallbacks(this.mApplyPendingScreenState);
            int i = 1;
            boolean z2 = state == DozeMachine.State.DOZE_PULSE_DONE && state2.isAlwaysOn();
            boolean z3 = (state == DozeMachine.State.DOZE_AOD_PAUSED || state == DozeMachine.State.DOZE) && state2.isAlwaysOn();
            boolean z4 = (state.isAlwaysOn() && state2 == DozeMachine.State.DOZE) || (state == DozeMachine.State.DOZE_AOD_PAUSING && state2 == DozeMachine.State.DOZE_AOD_PAUSED);
            boolean z5 = state == DozeMachine.State.INITIALIZED;
            if (hasCallbacks || z5 || z2 || z3) {
                this.mPendingScreenState = screenState;
                if (state2 == DozeMachine.State.DOZE_AOD && this.mParameters.shouldControlScreenOff() && !z3) {
                    z = true;
                }
                if (z) {
                    this.mWakeLock.setAcquired(true);
                }
                if (!hasCallbacks) {
                    if (DEBUG) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("Display state changed to ");
                        sb.append(screenState);
                        sb.append(" delayed by ");
                        if (z) {
                            i = 4000;
                        }
                        sb.append(i);
                        Log.d("DozeScreenState", sb.toString());
                    }
                    if (z) {
                        this.mHandler.postDelayed(this.mApplyPendingScreenState, 4000);
                    } else {
                        this.mHandler.post(this.mApplyPendingScreenState);
                    }
                } else if (DEBUG) {
                    Log.d("DozeScreenState", "Pending display state change to " + screenState);
                }
            } else if (z4) {
                this.mDozeHost.prepareForGentleSleep(new Runnable(screenState) {
                    /* class com.android.systemui.doze.$$Lambda$DozeScreenState$Q7dH1ne3iIuPaAubBMflNieFfwI */
                    public final /* synthetic */ int f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void run() {
                        DozeScreenState.this.lambda$transitionTo$0$DozeScreenState(this.f$1);
                    }
                });
            } else {
                lambda$transitionTo$0(screenState);
            }
        }
    }

    /* access modifiers changed from: private */
    public void applyPendingScreenState() {
        lambda$transitionTo$0(this.mPendingScreenState);
        this.mPendingScreenState = 0;
    }

    /* access modifiers changed from: private */
    /* renamed from: applyScreenState */
    public void lambda$transitionTo$0(int i) {
        if (i != 0) {
            if (DEBUG) {
                Log.d("DozeScreenState", "setDozeScreenState(" + i + ")");
            }
            this.mDozeService.setDozeScreenState(i);
            this.mPendingScreenState = 0;
            this.mWakeLock.setAcquired(false);
        }
    }
}
