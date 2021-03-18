package com.android.systemui.statusbar;

import android.app.WallpaperManager;
import android.os.SystemClock;
import android.util.MathUtils;
import android.view.Choreographer;
import android.view.View;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;
import com.android.internal.util.IndentingPrintWriter;
import com.android.systemui.Dumpable;
import com.android.systemui.dump.DumpManager;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.notification.ActivityLaunchAnimator;
import com.android.systemui.statusbar.phone.BiometricUnlockController;
import com.android.systemui.statusbar.phone.DozeParameters;
import com.android.systemui.statusbar.phone.NotificationShadeWindowController;
import com.android.systemui.statusbar.phone.PanelExpansionListener;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: NotificationShadeDepthController.kt */
public final class NotificationShadeDepthController implements PanelExpansionListener, Dumpable {
    private final BiometricUnlockController biometricUnlockController;
    private View blurRoot;
    private final BlurUtils blurUtils;
    @NotNull
    private DepthAnimation brightnessMirrorSpring = new DepthAnimation();
    private final Choreographer choreographer;
    private final DozeParameters dozeParameters;
    @NotNull
    private DepthAnimation globalActionsSpring = new DepthAnimation();
    private boolean ignoreShadeBlurUntilHidden;
    private boolean isBlurred;
    private boolean isClosed = true;
    private boolean isOpen;
    private final KeyguardStateController keyguardStateController;
    private List<DepthListener> listeners = new ArrayList();
    @Nullable
    private ActivityLaunchAnimator.ExpandAnimationParameters notificationLaunchAnimationParams;
    private final NotificationShadeWindowController notificationShadeWindowController;
    private int prevShadeDirection;
    private float prevShadeVelocity;
    private long prevTimestamp = -1;
    private boolean prevTracking;
    @NotNull
    public View root;
    private boolean scrimsVisible;
    @NotNull
    private DepthAnimation shadeAnimation = new DepthAnimation();
    private float shadeExpansion;
    @NotNull
    private DepthAnimation shadeSpring = new DepthAnimation();
    private boolean showingHomeControls;
    private final StatusBarStateController statusBarStateController;
    @NotNull
    private final Choreographer.FrameCallback updateBlurCallback = new NotificationShadeDepthController$updateBlurCallback$1(this);
    private boolean updateScheduled;
    private int wakeAndUnlockBlurRadius;
    private final WallpaperManager wallpaperManager;

    /* compiled from: NotificationShadeDepthController.kt */
    public interface DepthListener {
        void onWallpaperZoomOutChanged(float f);
    }

    public static /* synthetic */ void brightnessMirrorSpring$annotations() {
    }

    public static /* synthetic */ void globalActionsSpring$annotations() {
    }

    public static /* synthetic */ void shadeSpring$annotations() {
    }

    public static /* synthetic */ void updateBlurCallback$annotations() {
    }

    public NotificationShadeDepthController(@NotNull StatusBarStateController statusBarStateController2, @NotNull BlurUtils blurUtils2, @NotNull BiometricUnlockController biometricUnlockController2, @NotNull KeyguardStateController keyguardStateController2, @NotNull Choreographer choreographer2, @NotNull WallpaperManager wallpaperManager2, @NotNull NotificationShadeWindowController notificationShadeWindowController2, @NotNull DozeParameters dozeParameters2, @NotNull DumpManager dumpManager) {
        Intrinsics.checkParameterIsNotNull(statusBarStateController2, "statusBarStateController");
        Intrinsics.checkParameterIsNotNull(blurUtils2, "blurUtils");
        Intrinsics.checkParameterIsNotNull(biometricUnlockController2, "biometricUnlockController");
        Intrinsics.checkParameterIsNotNull(keyguardStateController2, "keyguardStateController");
        Intrinsics.checkParameterIsNotNull(choreographer2, "choreographer");
        Intrinsics.checkParameterIsNotNull(wallpaperManager2, "wallpaperManager");
        Intrinsics.checkParameterIsNotNull(notificationShadeWindowController2, "notificationShadeWindowController");
        Intrinsics.checkParameterIsNotNull(dozeParameters2, "dozeParameters");
        Intrinsics.checkParameterIsNotNull(dumpManager, "dumpManager");
        this.statusBarStateController = statusBarStateController2;
        this.blurUtils = blurUtils2;
        this.biometricUnlockController = biometricUnlockController2;
        this.keyguardStateController = keyguardStateController2;
        this.choreographer = choreographer2;
        this.wallpaperManager = wallpaperManager2;
        this.notificationShadeWindowController = notificationShadeWindowController2;
        this.dozeParameters = dozeParameters2;
        this.notificationShadeWindowController.setScrimsVisibilityListener(AnonymousClass1.INSTANCE);
        this.shadeAnimation.setStiffness(200.0f);
        this.shadeAnimation.setDampingRatio(1.0f);
    }

    @NotNull
    public final View getRoot() {
        View view = this.root;
        if (view != null) {
            return view;
        }
        Intrinsics.throwUninitializedPropertyAccessException("root");
        throw null;
    }

    public final void setRoot(@NotNull View view) {
        Intrinsics.checkParameterIsNotNull(view, "<set-?>");
        this.root = view;
    }

    @NotNull
    public final DepthAnimation getShadeSpring() {
        return this.shadeSpring;
    }

    @NotNull
    public final DepthAnimation getShadeAnimation() {
        return this.shadeAnimation;
    }

    @NotNull
    public final DepthAnimation getGlobalActionsSpring() {
        return this.globalActionsSpring;
    }

    public final boolean getShowingHomeControls() {
        return this.showingHomeControls;
    }

    public final void setShowingHomeControls(boolean z) {
        this.showingHomeControls = z;
    }

    @NotNull
    public final DepthAnimation getBrightnessMirrorSpring() {
        return this.brightnessMirrorSpring;
    }

    public final void setBrightnessMirrorVisible(boolean z) {
        DepthAnimation.animateTo$default(this.brightnessMirrorSpring, z ? this.blurUtils.blurRadiusOfRatio(1.0f) : 0, null, 2, null);
    }

    @Nullable
    public final ActivityLaunchAnimator.ExpandAnimationParameters getNotificationLaunchAnimationParams() {
        return this.notificationLaunchAnimationParams;
    }

    public final void setNotificationLaunchAnimationParams(@Nullable ActivityLaunchAnimator.ExpandAnimationParameters expandAnimationParameters) {
        this.notificationLaunchAnimationParams = expandAnimationParameters;
        if (expandAnimationParameters != null) {
            scheduleUpdate$default(this, null, 1, null);
        } else if (this.shadeSpring.getRadius() != 0 || this.shadeAnimation.getRadius() != 0) {
            this.ignoreShadeBlurUntilHidden = true;
            DepthAnimation.animateTo$default(this.shadeSpring, 0, null, 2, null);
            this.shadeSpring.finishIfRunning();
            DepthAnimation.animateTo$default(this.shadeAnimation, 0, null, 2, null);
            this.shadeAnimation.finishIfRunning();
        }
    }

    public final void addListener(@NotNull DepthListener depthListener) {
        Intrinsics.checkParameterIsNotNull(depthListener, "listener");
        this.listeners.add(depthListener);
    }

    @Override // com.android.systemui.statusbar.phone.PanelExpansionListener
    public void onPanelExpansionChanged(float f, boolean z) {
        long elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos();
        if (this.shadeExpansion == f && this.prevTracking == z) {
            this.prevTimestamp = elapsedRealtimeNanos;
            return;
        }
        long j = this.prevTimestamp;
        float f2 = 1.0f;
        if (j < 0) {
            this.prevTimestamp = elapsedRealtimeNanos;
        } else {
            f2 = MathUtils.constrain((float) (((double) (elapsedRealtimeNanos - j)) / 1.0E9d), 1.0E-5f, 1.0f);
        }
        float f3 = f - this.shadeExpansion;
        int signum = (int) Math.signum(f3);
        float constrain = MathUtils.constrain((f3 * 100.0f) / f2, -3000.0f, 3000.0f);
        updateShadeAnimationBlur(f, z, constrain, signum);
        this.prevShadeDirection = signum;
        this.prevShadeVelocity = constrain;
        this.shadeExpansion = f;
        this.prevTracking = z;
        this.prevTimestamp = elapsedRealtimeNanos;
        updateShadeBlur();
    }

    private final void updateShadeAnimationBlur(float f, boolean z, float f2, int i) {
        if (!isOnKeyguardNotDismissing()) {
            animateBlur(false, 0.0f);
            this.isClosed = true;
            this.isOpen = false;
        } else if (f > 0.0f) {
            if (this.isClosed) {
                animateBlur(true, f2);
                this.isClosed = false;
            }
            if (z && !this.isBlurred) {
                animateBlur(true, 0.0f);
            }
            if (!z && i < 0 && this.isBlurred) {
                animateBlur(false, f2);
            }
            if (f != 1.0f) {
                this.isOpen = false;
            } else if (!this.isOpen) {
                this.isOpen = true;
                if (!this.isBlurred) {
                    animateBlur(true, f2);
                }
            }
        } else if (!this.isClosed) {
            this.isClosed = true;
            if (this.isBlurred) {
                animateBlur(false, f2);
            }
        }
    }

    private final void animateBlur(boolean z, float f) {
        this.isBlurred = z;
        float f2 = (!z || !isOnKeyguardNotDismissing()) ? 0.0f : 1.0f;
        this.shadeAnimation.setStartVelocity(f);
        DepthAnimation.animateTo$default(this.shadeAnimation, this.blurUtils.blurRadiusOfRatio(f2), null, 2, null);
    }

    private final void updateShadeBlur() {
        DepthAnimation.animateTo$default(this.shadeSpring, isOnKeyguardNotDismissing() ? this.blurUtils.blurRadiusOfRatio(this.shadeExpansion) : 0, null, 2, null);
    }

    static /* synthetic */ void scheduleUpdate$default(NotificationShadeDepthController notificationShadeDepthController, View view, int i, Object obj) {
        if ((i & 1) != 0) {
            view = null;
        }
        notificationShadeDepthController.scheduleUpdate(view);
    }

    /* access modifiers changed from: private */
    public final void scheduleUpdate(View view) {
        if (!this.updateScheduled) {
            this.updateScheduled = true;
            this.blurRoot = view;
            this.choreographer.postFrameCallback(this.updateBlurCallback);
        }
    }

    private final boolean isOnKeyguardNotDismissing() {
        int state = this.statusBarStateController.getState();
        return (state == 0 || state == 2) && !this.keyguardStateController.isKeyguardFadingAway();
    }

    public final void updateGlobalDialogVisibility(float f, @Nullable View view) {
        this.globalActionsSpring.animateTo(this.blurUtils.blurRadiusOfRatio(f), view);
    }

    @Override // com.android.systemui.Dumpable
    public void dump(@NotNull FileDescriptor fileDescriptor, @NotNull PrintWriter printWriter, @NotNull String[] strArr) {
        Intrinsics.checkParameterIsNotNull(fileDescriptor, "fd");
        Intrinsics.checkParameterIsNotNull(printWriter, "pw");
        Intrinsics.checkParameterIsNotNull(strArr, "args");
        IndentingPrintWriter indentingPrintWriter = new IndentingPrintWriter(printWriter, "  ");
        indentingPrintWriter.println("StatusBarWindowBlurController:");
        indentingPrintWriter.increaseIndent();
        indentingPrintWriter.println("shadeRadius: " + this.shadeSpring.getRadius());
        indentingPrintWriter.println("shadeAnimation: " + this.shadeAnimation.getRadius());
        indentingPrintWriter.println("globalActionsRadius: " + this.globalActionsSpring.getRadius());
        indentingPrintWriter.println("brightnessMirrorRadius: " + this.brightnessMirrorSpring.getRadius());
        indentingPrintWriter.println("wakeAndUnlockBlur: " + this.wakeAndUnlockBlurRadius);
        StringBuilder sb = new StringBuilder();
        sb.append("notificationLaunchAnimationProgress: ");
        ActivityLaunchAnimator.ExpandAnimationParameters expandAnimationParameters = this.notificationLaunchAnimationParams;
        sb.append(expandAnimationParameters != null ? Float.valueOf(expandAnimationParameters.linearProgress) : null);
        indentingPrintWriter.println(sb.toString());
        indentingPrintWriter.println("ignoreShadeBlurUntilHidden: " + this.ignoreShadeBlurUntilHidden);
    }

    /* compiled from: NotificationShadeDepthController.kt */
    public final class DepthAnimation {
        private int pendingRadius = -1;
        private int radius;
        private SpringAnimation springAnimation;
        private View view;

        /* JADX WARN: Incorrect args count in method signature: ()V */
        public DepthAnimation() {
            SpringAnimation springAnimation2 = new SpringAnimation(this, new NotificationShadeDepthController$DepthAnimation$springAnimation$1(this, "blurRadius"));
            this.springAnimation = springAnimation2;
            springAnimation2.setSpring(new SpringForce(0.0f));
            SpringForce spring = this.springAnimation.getSpring();
            Intrinsics.checkExpressionValueIsNotNull(spring, "springAnimation.spring");
            spring.setDampingRatio(1.0f);
            SpringForce spring2 = this.springAnimation.getSpring();
            Intrinsics.checkExpressionValueIsNotNull(spring2, "springAnimation.spring");
            spring2.setStiffness(10000.0f);
            this.springAnimation.addEndListener(new DynamicAnimation.OnAnimationEndListener(this) {
                /* class com.android.systemui.statusbar.NotificationShadeDepthController.DepthAnimation.AnonymousClass1 */
                final /* synthetic */ DepthAnimation this$0;

                {
                    this.this$0 = r1;
                }

                @Override // androidx.dynamicanimation.animation.DynamicAnimation.OnAnimationEndListener
                public final void onAnimationEnd(DynamicAnimation<DynamicAnimation<?>> dynamicAnimation, boolean z, float f, float f2) {
                    this.this$0.pendingRadius = -1;
                }
            });
        }

        public final int getRadius() {
            return this.radius;
        }

        public final void setRadius(int i) {
            this.radius = i;
        }

        public final float getRatio() {
            return NotificationShadeDepthController.this.blurUtils.ratioOfBlurRadius(this.radius);
        }

        public static /* synthetic */ void animateTo$default(DepthAnimation depthAnimation, int i, View view2, int i2, Object obj) {
            if ((i2 & 2) != 0) {
                view2 = null;
            }
            depthAnimation.animateTo(i, view2);
        }

        public final void animateTo(int i, @Nullable View view2) {
            if (this.pendingRadius != i || !Intrinsics.areEqual(this.view, view2)) {
                this.view = view2;
                this.pendingRadius = i;
                this.springAnimation.animateToFinalPosition((float) i);
            }
        }

        public final void finishIfRunning() {
            if (this.springAnimation.isRunning()) {
                this.springAnimation.skipToEnd();
            }
        }

        public final void setStiffness(float f) {
            SpringForce spring = this.springAnimation.getSpring();
            Intrinsics.checkExpressionValueIsNotNull(spring, "springAnimation.spring");
            spring.setStiffness(f);
        }

        public final void setDampingRatio(float f) {
            SpringForce spring = this.springAnimation.getSpring();
            Intrinsics.checkExpressionValueIsNotNull(spring, "springAnimation.spring");
            spring.setDampingRatio(f);
        }

        public final void setStartVelocity(float f) {
            this.springAnimation.setStartVelocity(f);
        }
    }
}
