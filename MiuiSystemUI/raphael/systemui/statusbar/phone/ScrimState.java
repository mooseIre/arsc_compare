package com.android.systemui.statusbar.phone;

import android.graphics.Color;
import android.os.Trace;
import com.android.systemui.dock.DockManager;
import com.android.systemui.statusbar.ScrimView;

public enum ScrimState {
    UNINITIALIZED,
    OFF {
        @Override // com.android.systemui.statusbar.phone.ScrimState
        public boolean isLowPowerState() {
            return true;
        }

        @Override // com.android.systemui.statusbar.phone.ScrimState
        public void prepare(ScrimState scrimState) {
            this.mFrontTint = -16777216;
            this.mBehindTint = -16777216;
            this.mBubbleTint = scrimState.mBubbleTint;
            this.mFrontAlpha = 1.0f;
            this.mBehindAlpha = 1.0f;
            this.mBubbleAlpha = scrimState.mBubbleAlpha;
            this.mAnimationDuration = 1000;
        }
    },
    KEYGUARD {
        @Override // com.android.systemui.statusbar.phone.ScrimState
        public void prepare(ScrimState scrimState) {
            this.mBlankScreen = false;
            if (scrimState == ScrimState.AOD) {
                this.mAnimationDuration = 500;
                if (this.mDisplayRequiresBlanking) {
                    this.mBlankScreen = true;
                }
            } else if (scrimState == ScrimState.KEYGUARD) {
                this.mAnimationDuration = 500;
            } else {
                this.mAnimationDuration = 220;
            }
            this.mFrontTint = -16777216;
            this.mBehindTint = -16777216;
            this.mBubbleTint = 0;
            this.mFrontAlpha = 0.0f;
            this.mBehindAlpha = 0.0f;
            this.mBubbleAlpha = 0.0f;
        }
    },
    BOUNCER {
        @Override // com.android.systemui.statusbar.phone.ScrimState
        public void prepare(ScrimState scrimState) {
            this.mBehindAlpha = 0.5f;
            this.mFrontAlpha = 0.0f;
            this.mBubbleAlpha = 0.0f;
        }
    },
    BOUNCER_SCRIMMED {
        @Override // com.android.systemui.statusbar.phone.ScrimState
        public void prepare(ScrimState scrimState) {
            this.mBehindAlpha = 0.5f;
            this.mBubbleAlpha = 0.0f;
            this.mFrontAlpha = 0.0f;
        }
    },
    BRIGHTNESS_MIRROR {
        @Override // com.android.systemui.statusbar.phone.ScrimState
        public void prepare(ScrimState scrimState) {
            this.mBehindAlpha = 0.0f;
            this.mFrontAlpha = 0.0f;
            this.mBubbleAlpha = 0.0f;
        }
    },
    AOD {
        @Override // com.android.systemui.statusbar.phone.ScrimState
        public boolean isLowPowerState() {
            return true;
        }

        @Override // com.android.systemui.statusbar.phone.ScrimState
        public void prepare(ScrimState scrimState) {
            float f;
            boolean alwaysOn = this.mDozeParameters.getAlwaysOn();
            boolean isDocked = this.mDockManager.isDocked();
            this.mBlankScreen = this.mDisplayRequiresBlanking;
            this.mFrontTint = -16777216;
            if (alwaysOn || isDocked) {
                f = this.mAodFrontScrimAlpha;
            } else {
                f = 1.0f;
            }
            this.mFrontAlpha = f;
            this.mBehindTint = -16777216;
            this.mBehindAlpha = 0.0f;
            this.mBubbleTint = 0;
            this.mBubbleAlpha = 0.0f;
            this.mAnimationDuration = 1000;
            this.mAnimateChange = this.mDozeParameters.shouldControlScreenOff();
        }

        @Override // com.android.systemui.statusbar.phone.ScrimState
        public float getBehindAlpha() {
            return (!this.mWallpaperSupportsAmbientMode || this.mHasBackdrop) ? 1.0f : 0.0f;
        }
    },
    PULSING {
        @Override // com.android.systemui.statusbar.phone.ScrimState
        public void prepare(ScrimState scrimState) {
            this.mFrontAlpha = this.mAodFrontScrimAlpha;
            this.mBubbleAlpha = 0.0f;
            this.mBehindTint = -16777216;
            this.mFrontTint = -16777216;
            this.mBlankScreen = this.mDisplayRequiresBlanking;
            this.mAnimationDuration = this.mWakeLockScreenSensorActive ? 1000 : 220;
            if (this.mWakeLockScreenSensorActive && scrimState == ScrimState.AOD) {
                updateScrimColor(this.mScrimBehind, 1.0f, -16777216);
            }
        }

        @Override // com.android.systemui.statusbar.phone.ScrimState
        public float getBehindAlpha() {
            if (this.mWakeLockScreenSensorActive) {
                return 0.6f;
            }
            return ScrimState.AOD.getBehindAlpha();
        }
    },
    UNLOCKED {
        @Override // com.android.systemui.statusbar.phone.ScrimState
        public void prepare(ScrimState scrimState) {
            this.mBehindAlpha = 0.0f;
            this.mFrontAlpha = 0.0f;
            this.mBubbleAlpha = 0.0f;
            this.mAnimationDuration = this.mKeyguardFadingAway ? this.mKeyguardFadingAwayDuration : 300;
            this.mAnimateChange = !this.mLaunchingAffordanceWithPreview;
            this.mFrontTint = 0;
            this.mBehindTint = 0;
            this.mBubbleTint = 0;
            this.mBlankScreen = false;
            if (scrimState == ScrimState.AOD) {
                updateScrimColor(this.mScrimInFront, 1.0f, -16777216);
                updateScrimColor(this.mScrimBehind, 1.0f, -16777216);
                updateScrimColor(this.mScrimForBubble, 1.0f, -16777216);
                this.mFrontTint = -16777216;
                this.mBehindTint = -16777216;
                this.mBubbleTint = -16777216;
                this.mBlankScreen = true;
            }
        }
    },
    BUBBLE_EXPANDED {
        @Override // com.android.systemui.statusbar.phone.ScrimState
        public void prepare(ScrimState scrimState) {
            this.mFrontTint = 0;
            this.mBehindTint = 0;
            this.mBubbleTint = 0;
            this.mFrontAlpha = 0.0f;
            this.mBehindAlpha = 0.0f;
            this.mBubbleAlpha = 0.6f;
            this.mAnimationDuration = 220;
            this.mBlankScreen = false;
        }
    };
    
    boolean mAnimateChange;
    long mAnimationDuration;
    float mAodFrontScrimAlpha;
    float mBehindAlpha;
    int mBehindTint;
    boolean mBlankScreen;
    float mBubbleAlpha;
    int mBubbleTint;
    float mDefaultScrimAlpha;
    boolean mDisplayRequiresBlanking;
    DockManager mDockManager;
    DozeParameters mDozeParameters;
    float mFrontAlpha;
    int mFrontTint;
    boolean mHasBackdrop;
    boolean mKeyguardFadingAway;
    long mKeyguardFadingAwayDuration;
    boolean mLaunchingAffordanceWithPreview;
    ScrimView mScrimBehind;
    float mScrimBehindAlphaKeyguard;
    ScrimView mScrimForBubble;
    ScrimView mScrimInFront;
    boolean mWakeLockScreenSensorActive;
    boolean mWallpaperSupportsAmbientMode;

    public boolean isLowPowerState() {
        return false;
    }

    public void prepare(ScrimState scrimState) {
    }

    private ScrimState() {
        this.mBlankScreen = false;
        this.mAnimationDuration = 220;
        this.mFrontTint = 0;
        this.mBehindTint = 0;
        this.mBubbleTint = 0;
        this.mAnimateChange = true;
    }

    public void init(ScrimView scrimView, ScrimView scrimView2, ScrimView scrimView3, DozeParameters dozeParameters, DockManager dockManager) {
        this.mScrimInFront = scrimView;
        this.mScrimBehind = scrimView2;
        this.mScrimForBubble = scrimView3;
        this.mDozeParameters = dozeParameters;
        this.mDockManager = dockManager;
        this.mDisplayRequiresBlanking = dozeParameters.getDisplayNeedsBlanking();
    }

    public float getFrontAlpha() {
        return this.mFrontAlpha;
    }

    public float getBehindAlpha() {
        return this.mBehindAlpha;
    }

    public float getBubbleAlpha() {
        return this.mBubbleAlpha;
    }

    public int getFrontTint() {
        return this.mFrontTint;
    }

    public int getBehindTint() {
        return this.mBehindTint;
    }

    public int getBubbleTint() {
        return this.mBubbleTint;
    }

    public long getAnimationDuration() {
        return this.mAnimationDuration;
    }

    public boolean getBlanksScreen() {
        return this.mBlankScreen;
    }

    public void updateScrimColor(ScrimView scrimView, float f, int i) {
        Trace.traceCounter(4096, scrimView == this.mScrimInFront ? "front_scrim_alpha" : "back_scrim_alpha", (int) (255.0f * f));
        Trace.traceCounter(4096, scrimView == this.mScrimInFront ? "front_scrim_tint" : "back_scrim_tint", Color.alpha(i));
        scrimView.setTint(i);
        scrimView.setViewAlpha(f);
    }

    public boolean getAnimateChange() {
        return this.mAnimateChange;
    }

    public void setAodFrontScrimAlpha(float f) {
        this.mAodFrontScrimAlpha = f;
    }

    public void setScrimBehindAlphaKeyguard(float f) {
        this.mScrimBehindAlphaKeyguard = f;
    }

    public void setDefaultScrimAlpha(float f) {
        this.mDefaultScrimAlpha = f;
    }

    public void setWallpaperSupportsAmbientMode(boolean z) {
        this.mWallpaperSupportsAmbientMode = z;
    }

    public void setLaunchingAffordanceWithPreview(boolean z) {
        this.mLaunchingAffordanceWithPreview = z;
    }

    public void setHasBackdrop(boolean z) {
        this.mHasBackdrop = z;
    }

    public void setWakeLockScreenSensorActive(boolean z) {
        this.mWakeLockScreenSensorActive = z;
    }

    public void setKeyguardFadingAway(boolean z, long j) {
        this.mKeyguardFadingAway = z;
        this.mKeyguardFadingAwayDuration = j;
    }
}
