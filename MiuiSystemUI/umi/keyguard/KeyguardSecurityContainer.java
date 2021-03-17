package com.android.keyguard;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.metrics.LogMaker;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.util.AttributeSet;
import android.util.Log;
import android.util.MathUtils;
import android.util.Slog;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewRootImpl;
import android.view.WindowInsets;
import android.view.WindowInsetsAnimation;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.SpringAnimation;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.UiEventLogger;
import com.android.internal.logging.UiEventLoggerImpl;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.KeyguardSecurityModel;
import com.android.keyguard.fod.MiuiGxzwManager;
import com.android.settingslib.utils.ThreadUtils;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0017R$layout;
import com.android.systemui.C0019R$plurals;
import com.android.systemui.C0021R$string;
import com.android.systemui.DejankUtils;
import com.android.systemui.Dependency;
import com.android.systemui.SystemUIFactory;
import com.android.systemui.shared.system.SysUiStatsLog;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.android.systemui.util.InjectionInflationController;
import java.util.List;
import java.util.function.Supplier;
import miui.os.Build;

public class KeyguardSecurityContainer extends FrameLayout implements KeyguardSecurityView {
    private static final UiEventLogger sUiEventLogger = new UiEventLoggerImpl();
    private int mActivePointerId;
    private AlertDialog mAlertDialog;
    private KeyguardSecurityCallback mCallback;
    private KeyguardSecurityModel.SecurityMode mCurrentSecuritySelection;
    private KeyguardSecurityView mCurrentSecurityView;
    private boolean mDisappearAnimRunning;
    private View mFogetPasswordMethod;
    private View mFogetPasswordSuggestion;
    private TextView mForgetPasswordMethodBack;
    private TextView mForgetPasswordMethodNext;
    private InjectionInflationController mInjectionInflationController;
    private boolean mIsDragging;
    private final KeyguardStateController mKeyguardStateController;
    private LockPatternUtils mLockPatternUtils;
    private View mLockoutView;
    private final MetricsLogger mMetricsLogger;
    private KeyguardSecurityCallback mNullCallback;
    private AdminSecondaryLockScreenController mSecondaryLockScreenController;
    private SecurityCallback mSecurityCallback;
    private KeyguardSecurityModel mSecurityModel;
    KeyguardSecurityViewFlipper mSecurityViewFlipper;
    private float mStartTouchY;
    private boolean mSwipeUpToRetry;
    private final KeyguardUpdateMonitor mUpdateMonitor;
    private final VelocityTracker mVelocityTracker;
    private final ViewConfiguration mViewConfiguration;
    private final WindowInsetsAnimation.Callback mWindowInsetsAnimationCallback;

    public interface SecurityCallback {
        boolean dismiss(boolean z, int i, boolean z2);

        void finish(boolean z, int i);

        void onSecurityModeChanged(KeyguardSecurityModel.SecurityMode securityMode, boolean z);

        void reset();

        void userActivity();
    }

    public boolean shouldDelayChildPressedState() {
        return true;
    }

    public enum BouncerUiEvent implements UiEventLogger.UiEventEnum {
        UNKNOWN(0),
        BOUNCER_DISMISS_EXTENDED_ACCESS(413),
        BOUNCER_DISMISS_BIOMETRIC(414),
        BOUNCER_DISMISS_NONE_SECURITY(415),
        BOUNCER_DISMISS_PASSWORD(416),
        BOUNCER_DISMISS_SIM(417),
        BOUNCER_PASSWORD_SUCCESS(418),
        BOUNCER_PASSWORD_FAILURE(419);
        
        private final int mId;

        private BouncerUiEvent(int i) {
            this.mId = i;
        }

        public int getId() {
            return this.mId;
        }
    }

    public KeyguardSecurityContainer(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public KeyguardSecurityContainer(Context context) {
        this(context, null, 0);
    }

    public KeyguardSecurityContainer(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mCurrentSecuritySelection = KeyguardSecurityModel.SecurityMode.Invalid;
        this.mVelocityTracker = VelocityTracker.obtain();
        this.mMetricsLogger = (MetricsLogger) Dependency.get(MetricsLogger.class);
        this.mActivePointerId = -1;
        this.mStartTouchY = -1.0f;
        this.mWindowInsetsAnimationCallback = new WindowInsetsAnimation.Callback(0) {
            /* class com.android.keyguard.KeyguardSecurityContainer.AnonymousClass1 */
            private final Rect mFinalBounds = new Rect();
            private final Rect mInitialBounds = new Rect();

            public void onPrepare(WindowInsetsAnimation windowInsetsAnimation) {
                KeyguardSecurityContainer.this.mSecurityViewFlipper.getBoundsOnScreen(this.mInitialBounds);
            }

            public WindowInsetsAnimation.Bounds onStart(WindowInsetsAnimation windowInsetsAnimation, WindowInsetsAnimation.Bounds bounds) {
                KeyguardSecurityContainer.this.mSecurityViewFlipper.getBoundsOnScreen(this.mFinalBounds);
                return bounds;
            }

            public WindowInsets onProgress(WindowInsets windowInsets, List<WindowInsetsAnimation> list) {
                if (KeyguardSecurityContainer.this.mDisappearAnimRunning) {
                    KeyguardSecurityContainer.this.mSecurityViewFlipper.setTranslationY((float) (this.mInitialBounds.bottom - this.mFinalBounds.bottom));
                } else {
                    int i = 0;
                    for (WindowInsetsAnimation windowInsetsAnimation : list) {
                        if ((windowInsetsAnimation.getTypeMask() & WindowInsets.Type.ime()) != 0) {
                            i += (int) MathUtils.lerp((float) (this.mInitialBounds.bottom - this.mFinalBounds.bottom), 0.0f, windowInsetsAnimation.getInterpolatedFraction());
                        }
                    }
                    KeyguardSecurityContainer.this.mSecurityViewFlipper.setTranslationY((float) i);
                }
                return windowInsets;
            }

            public void onEnd(WindowInsetsAnimation windowInsetsAnimation) {
                if (!KeyguardSecurityContainer.this.mDisappearAnimRunning) {
                    KeyguardSecurityContainer.this.mSecurityViewFlipper.setTranslationY(0.0f);
                }
            }
        };
        this.mCallback = new KeyguardSecurityCallback() {
            /* class com.android.keyguard.KeyguardSecurityContainer.AnonymousClass9 */

            @Override // com.android.keyguard.KeyguardSecurityCallback
            public void onUserInput() {
            }

            @Override // com.android.keyguard.KeyguardSecurityCallback
            public void userActivity() {
                if (KeyguardSecurityContainer.this.mSecurityCallback != null) {
                    KeyguardSecurityContainer.this.mSecurityCallback.userActivity();
                }
            }

            @Override // com.android.keyguard.KeyguardSecurityCallback
            public void dismiss(boolean z, int i) {
                dismiss(z, i, false);
            }

            @Override // com.android.keyguard.KeyguardSecurityCallback
            public void dismiss(boolean z, int i, boolean z2) {
                KeyguardSecurityContainer.this.mSecurityCallback.dismiss(z, i, z2);
            }

            @Override // com.android.keyguard.KeyguardSecurityCallback
            public void reportUnlockAttempt(int i, boolean z, int i2) {
                BouncerUiEvent bouncerUiEvent;
                if (z) {
                    SysUiStatsLog.write(64, 2);
                    KeyguardSecurityContainer.this.mLockPatternUtils.reportSuccessfulPasswordAttempt(i);
                    ThreadUtils.postOnBackgroundThread($$Lambda$KeyguardSecurityContainer$9$Vmk4hjsTAEO_VX6DVDEt0PiYSY.INSTANCE);
                } else {
                    SysUiStatsLog.write(64, 1);
                    KeyguardSecurityContainer.this.reportFailedUnlockAttempt(i, i2);
                }
                KeyguardSecurityContainer.this.mMetricsLogger.write(new LogMaker(197).setType(z ? 10 : 11));
                UiEventLogger uiEventLogger = KeyguardSecurityContainer.sUiEventLogger;
                if (z) {
                    bouncerUiEvent = BouncerUiEvent.BOUNCER_PASSWORD_SUCCESS;
                } else {
                    bouncerUiEvent = BouncerUiEvent.BOUNCER_PASSWORD_FAILURE;
                }
                uiEventLogger.log(bouncerUiEvent);
            }

            static /* synthetic */ void lambda$reportUnlockAttempt$0() {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException unused) {
                }
                Runtime.getRuntime().gc();
            }

            @Override // com.android.keyguard.KeyguardSecurityCallback
            public void reset() {
                KeyguardSecurityContainer.this.mSecurityCallback.reset();
            }

            @Override // com.android.keyguard.KeyguardSecurityCallback
            public void handleAttemptLockout(long j) {
                KeyguardSecurityContainer.this.showLockoutView(j);
            }
        };
        this.mNullCallback = new KeyguardSecurityCallback(this) {
            /* class com.android.keyguard.KeyguardSecurityContainer.AnonymousClass10 */

            @Override // com.android.keyguard.KeyguardSecurityCallback
            public void dismiss(boolean z, int i) {
            }

            @Override // com.android.keyguard.KeyguardSecurityCallback
            public void dismiss(boolean z, int i, boolean z2) {
            }

            @Override // com.android.keyguard.KeyguardSecurityCallback
            public void handleAttemptLockout(long j) {
            }

            @Override // com.android.keyguard.KeyguardSecurityCallback
            public void onUserInput() {
            }

            @Override // com.android.keyguard.KeyguardSecurityCallback
            public void reportUnlockAttempt(int i, boolean z, int i2) {
            }

            @Override // com.android.keyguard.KeyguardSecurityCallback
            public void reset() {
            }

            @Override // com.android.keyguard.KeyguardSecurityCallback
            public void userActivity() {
            }
        };
        this.mSecurityModel = (KeyguardSecurityModel) Dependency.get(KeyguardSecurityModel.class);
        this.mLockPatternUtils = new LockPatternUtils(context);
        this.mUpdateMonitor = (KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class);
        new SpringAnimation(this, DynamicAnimation.Y);
        this.mInjectionInflationController = new InjectionInflationController(SystemUIFactory.getInstance().getRootComponent());
        this.mViewConfiguration = ViewConfiguration.get(context);
        this.mKeyguardStateController = (KeyguardStateController) Dependency.get(KeyguardStateController.class);
        this.mSecondaryLockScreenController = new AdminSecondaryLockScreenController(context, this, this.mUpdateMonitor, this.mCallback, new Handler(Looper.myLooper()));
    }

    public void setSecurityCallback(SecurityCallback securityCallback) {
        this.mSecurityCallback = securityCallback;
    }

    public boolean onBackPressed() {
        View view = this.mFogetPasswordSuggestion;
        if (!(view == null || this.mFogetPasswordMethod == null)) {
            if (view.getVisibility() == 0) {
                this.mFogetPasswordSuggestion.setVisibility(4);
                this.mFogetPasswordMethod.setVisibility(0);
                return true;
            } else if (this.mFogetPasswordMethod.getVisibility() == 0) {
                this.mFogetPasswordMethod.setVisibility(4);
                setLockoutViewVisible(0);
                return true;
            }
        }
        return false;
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void onResume(int i) {
        KeyguardSecurityModel.SecurityMode securityMode = this.mCurrentSecuritySelection;
        if (securityMode != KeyguardSecurityModel.SecurityMode.None) {
            getSecurityView(securityMode).onResume(i);
        }
        this.mSecurityViewFlipper.setWindowInsetsAnimationCallback(this.mWindowInsetsAnimationCallback);
        updateBiometricRetry();
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void onPause() {
        AlertDialog alertDialog = this.mAlertDialog;
        if (alertDialog != null) {
            alertDialog.dismiss();
            this.mAlertDialog = null;
        }
        this.mSecondaryLockScreenController.hide();
        KeyguardSecurityModel.SecurityMode securityMode = this.mCurrentSecuritySelection;
        if (securityMode != KeyguardSecurityModel.SecurityMode.None) {
            getSecurityView(securityMode).onPause();
        }
        this.mSecurityViewFlipper.setWindowInsetsAnimationCallback(null);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:7:0x000e, code lost:
        if (r0 != 3) goto L_0x0061;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean onInterceptTouchEvent(android.view.MotionEvent r6) {
        /*
            r5 = this;
            int r0 = r6.getActionMasked()
            r1 = 0
            if (r0 == 0) goto L_0x004c
            r2 = 1
            if (r0 == r2) goto L_0x0049
            r3 = 2
            if (r0 == r3) goto L_0x0011
            r6 = 3
            if (r0 == r6) goto L_0x0049
            goto L_0x0061
        L_0x0011:
            boolean r0 = r5.mIsDragging
            if (r0 == 0) goto L_0x0016
            return r2
        L_0x0016:
            boolean r0 = r5.mSwipeUpToRetry
            if (r0 != 0) goto L_0x001b
            return r1
        L_0x001b:
            com.android.keyguard.KeyguardSecurityView r0 = r5.mCurrentSecurityView
            boolean r0 = r0.disallowInterceptTouch(r6)
            if (r0 == 0) goto L_0x0024
            return r1
        L_0x0024:
            int r0 = r5.mActivePointerId
            int r0 = r6.findPointerIndex(r0)
            android.view.ViewConfiguration r3 = r5.mViewConfiguration
            int r3 = r3.getScaledTouchSlop()
            float r3 = (float) r3
            r4 = 1082130432(0x40800000, float:4.0)
            float r3 = r3 * r4
            com.android.keyguard.KeyguardSecurityView r4 = r5.mCurrentSecurityView
            if (r4 == 0) goto L_0x0061
            r4 = -1
            if (r0 == r4) goto L_0x0061
            float r4 = r5.mStartTouchY
            float r6 = r6.getY(r0)
            float r4 = r4 - r6
            int r6 = (r4 > r3 ? 1 : (r4 == r3 ? 0 : -1))
            if (r6 <= 0) goto L_0x0061
            r5.mIsDragging = r2
            return r2
        L_0x0049:
            r5.mIsDragging = r1
            goto L_0x0061
        L_0x004c:
            int r0 = r6.getActionIndex()
            float r2 = r6.getY(r0)
            r5.mStartTouchY = r2
            int r6 = r6.getPointerId(r0)
            r5.mActivePointerId = r6
            android.view.VelocityTracker r5 = r5.mVelocityTracker
            r5.clear()
        L_0x0061:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.keyguard.KeyguardSecurityContainer.onInterceptTouchEvent(android.view.MotionEvent):boolean");
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void startAppearAnimation() {
        KeyguardSecurityModel.SecurityMode securityMode = this.mCurrentSecuritySelection;
        if (securityMode != KeyguardSecurityModel.SecurityMode.None) {
            getSecurityView(securityMode).startAppearAnimation();
        }
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public boolean startDisappearAnimation(Runnable runnable) {
        KeyguardSecurityModel.SecurityMode securityMode = this.mCurrentSecuritySelection;
        if (securityMode != KeyguardSecurityModel.SecurityMode.None) {
            return getSecurityView(securityMode).startDisappearAnimation(runnable);
        }
        return false;
    }

    private void updateBiometricRetry() {
        KeyguardSecurityModel.SecurityMode securityMode = getSecurityMode();
        this.mSwipeUpToRetry = (!this.mKeyguardStateController.isFaceAuthEnabled() || securityMode == KeyguardSecurityModel.SecurityMode.SimPin || securityMode == KeyguardSecurityModel.SecurityMode.SimPuk || securityMode == KeyguardSecurityModel.SecurityMode.None) ? false : true;
    }

    /* access modifiers changed from: protected */
    public KeyguardSecurityView getSecurityView(KeyguardSecurityModel.SecurityMode securityMode) {
        KeyguardSecurityView keyguardSecurityView;
        int securityViewIdForMode = getSecurityViewIdForMode(securityMode);
        int childCount = this.mSecurityViewFlipper.getChildCount();
        int i = 0;
        while (true) {
            if (i >= childCount) {
                keyguardSecurityView = null;
                break;
            } else if (this.mSecurityViewFlipper.getChildAt(i).getId() == securityViewIdForMode) {
                keyguardSecurityView = (KeyguardSecurityView) this.mSecurityViewFlipper.getChildAt(i);
                break;
            } else {
                i++;
            }
        }
        int layoutIdFor = getLayoutIdFor(securityMode);
        if (keyguardSecurityView != null || layoutIdFor == 0) {
            return keyguardSecurityView;
        }
        LayoutInflater from = LayoutInflater.from(((FrameLayout) this).mContext);
        Log.v("KeyguardSecurityView", "inflating id = " + layoutIdFor);
        View inflate = this.mInjectionInflationController.injectable(from).inflate(layoutIdFor, (ViewGroup) this.mSecurityViewFlipper, false);
        this.mSecurityViewFlipper.addView(inflate);
        updateSecurityView(inflate);
        KeyguardSecurityView keyguardSecurityView2 = (KeyguardSecurityView) inflate;
        keyguardSecurityView2.reset();
        return keyguardSecurityView2;
    }

    private void updateSecurityView(View view) {
        if (view instanceof KeyguardSecurityView) {
            KeyguardSecurityView keyguardSecurityView = (KeyguardSecurityView) view;
            keyguardSecurityView.setKeyguardCallback(this.mCallback);
            keyguardSecurityView.setLockPatternUtils(this.mLockPatternUtils);
            return;
        }
        Log.w("KeyguardSecurityView", "View " + view + " is not a KeyguardSecurityView");
    }

    public void onFinishInflate() {
        super.onFinishInflate();
        KeyguardSecurityViewFlipper keyguardSecurityViewFlipper = (KeyguardSecurityViewFlipper) findViewById(C0015R$id.view_flipper);
        this.mSecurityViewFlipper = keyguardSecurityViewFlipper;
        keyguardSecurityViewFlipper.setLockPatternUtils(this.mLockPatternUtils);
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void setLockPatternUtils(LockPatternUtils lockPatternUtils) {
        this.mLockPatternUtils = lockPatternUtils;
        this.mSecurityModel.setLockPatternUtils(lockPatternUtils);
        this.mSecurityViewFlipper.setLockPatternUtils(this.mLockPatternUtils);
    }

    public WindowInsets onApplyWindowInsets(WindowInsets windowInsets) {
        int i;
        if (ViewRootImpl.sNewInsetsMode == 2) {
            i = windowInsets.getInsets(WindowInsets.Type.ime()).bottom;
        } else {
            i = windowInsets.getSystemWindowInsetBottom();
        }
        setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), i);
        return windowInsets.inset(0, 0, 0, i);
    }

    private void showDialog(String str, String str2) {
        AlertDialog alertDialog = this.mAlertDialog;
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
        AlertDialog create = new AlertDialog.Builder(((FrameLayout) this).mContext).setTitle(str).setMessage(str2).setCancelable(false).setNeutralButton(C0021R$string.ok, (DialogInterface.OnClickListener) null).create();
        this.mAlertDialog = create;
        if (!(((FrameLayout) this).mContext instanceof Activity)) {
            create.getWindow().setType(2009);
        }
        this.mAlertDialog.show();
    }

    /* access modifiers changed from: package-private */
    /* renamed from: com.android.keyguard.KeyguardSecurityContainer$11  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass11 {
        static final /* synthetic */ int[] $SwitchMap$com$android$keyguard$KeyguardSecurityModel$SecurityMode;

        /* JADX WARNING: Can't wrap try/catch for region: R(14:0|1|2|3|4|5|6|7|8|9|10|11|12|(3:13|14|16)) */
        /* JADX WARNING: Can't wrap try/catch for region: R(16:0|1|2|3|4|5|6|7|8|9|10|11|12|13|14|16) */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:11:0x003e */
        /* JADX WARNING: Missing exception handler attribute for start block: B:13:0x0049 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0012 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:5:0x001d */
        /* JADX WARNING: Missing exception handler attribute for start block: B:7:0x0028 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:9:0x0033 */
        static {
            /*
                com.android.keyguard.KeyguardSecurityModel$SecurityMode[] r0 = com.android.keyguard.KeyguardSecurityModel.SecurityMode.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                com.android.keyguard.KeyguardSecurityContainer.AnonymousClass11.$SwitchMap$com$android$keyguard$KeyguardSecurityModel$SecurityMode = r0
                com.android.keyguard.KeyguardSecurityModel$SecurityMode r1 = com.android.keyguard.KeyguardSecurityModel.SecurityMode.Pattern     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = com.android.keyguard.KeyguardSecurityContainer.AnonymousClass11.$SwitchMap$com$android$keyguard$KeyguardSecurityModel$SecurityMode     // Catch:{ NoSuchFieldError -> 0x001d }
                com.android.keyguard.KeyguardSecurityModel$SecurityMode r1 = com.android.keyguard.KeyguardSecurityModel.SecurityMode.PIN     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                int[] r0 = com.android.keyguard.KeyguardSecurityContainer.AnonymousClass11.$SwitchMap$com$android$keyguard$KeyguardSecurityModel$SecurityMode     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.android.keyguard.KeyguardSecurityModel$SecurityMode r1 = com.android.keyguard.KeyguardSecurityModel.SecurityMode.Password     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                int[] r0 = com.android.keyguard.KeyguardSecurityContainer.AnonymousClass11.$SwitchMap$com$android$keyguard$KeyguardSecurityModel$SecurityMode     // Catch:{ NoSuchFieldError -> 0x0033 }
                com.android.keyguard.KeyguardSecurityModel$SecurityMode r1 = com.android.keyguard.KeyguardSecurityModel.SecurityMode.Invalid     // Catch:{ NoSuchFieldError -> 0x0033 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0033 }
                r2 = 4
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0033 }
            L_0x0033:
                int[] r0 = com.android.keyguard.KeyguardSecurityContainer.AnonymousClass11.$SwitchMap$com$android$keyguard$KeyguardSecurityModel$SecurityMode     // Catch:{ NoSuchFieldError -> 0x003e }
                com.android.keyguard.KeyguardSecurityModel$SecurityMode r1 = com.android.keyguard.KeyguardSecurityModel.SecurityMode.None     // Catch:{ NoSuchFieldError -> 0x003e }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x003e }
                r2 = 5
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x003e }
            L_0x003e:
                int[] r0 = com.android.keyguard.KeyguardSecurityContainer.AnonymousClass11.$SwitchMap$com$android$keyguard$KeyguardSecurityModel$SecurityMode     // Catch:{ NoSuchFieldError -> 0x0049 }
                com.android.keyguard.KeyguardSecurityModel$SecurityMode r1 = com.android.keyguard.KeyguardSecurityModel.SecurityMode.SimPin     // Catch:{ NoSuchFieldError -> 0x0049 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0049 }
                r2 = 6
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0049 }
            L_0x0049:
                int[] r0 = com.android.keyguard.KeyguardSecurityContainer.AnonymousClass11.$SwitchMap$com$android$keyguard$KeyguardSecurityModel$SecurityMode     // Catch:{ NoSuchFieldError -> 0x0054 }
                com.android.keyguard.KeyguardSecurityModel$SecurityMode r1 = com.android.keyguard.KeyguardSecurityModel.SecurityMode.SimPuk     // Catch:{ NoSuchFieldError -> 0x0054 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0054 }
                r2 = 7
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0054 }
            L_0x0054:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.keyguard.KeyguardSecurityContainer.AnonymousClass11.<clinit>():void");
        }
    }

    private void showAlmostAtWipeDialog(int i, int i2, int i3) {
        String str;
        if (i3 == 1) {
            str = ((FrameLayout) this).mContext.getString(C0021R$string.kg_failed_attempts_almost_at_wipe, Integer.valueOf(i), Integer.valueOf(i2));
        } else if (i3 == 2) {
            str = ((FrameLayout) this).mContext.getString(C0021R$string.kg_failed_attempts_almost_at_erase_profile, Integer.valueOf(i), Integer.valueOf(i2));
        } else if (i3 != 3) {
            str = null;
        } else {
            str = ((FrameLayout) this).mContext.getString(C0021R$string.kg_failed_attempts_almost_at_erase_user, Integer.valueOf(i), Integer.valueOf(i2));
        }
        showDialog(null, str);
    }

    private void showWipeDialog(int i, int i2) {
        String str;
        if (i2 == 1) {
            str = ((FrameLayout) this).mContext.getString(C0021R$string.kg_failed_attempts_now_wiping, Integer.valueOf(i));
        } else if (i2 == 2) {
            str = ((FrameLayout) this).mContext.getString(C0021R$string.kg_failed_attempts_now_erasing_profile, Integer.valueOf(i));
        } else if (i2 != 3) {
            str = null;
        } else {
            str = ((FrameLayout) this).mContext.getString(C0021R$string.kg_failed_attempts_now_erasing_user, Integer.valueOf(i));
        }
        showDialog(null, str);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void reportFailedUnlockAttempt(int i, int i2) {
        int i3 = 1;
        int currentFailedPasswordAttempts = this.mLockPatternUtils.getCurrentFailedPasswordAttempts(i) + 1;
        Log.d("KeyguardSecurityView", "reportFailedPatternAttempt: #" + currentFailedPasswordAttempts);
        DevicePolicyManager devicePolicyManager = this.mLockPatternUtils.getDevicePolicyManager();
        int maximumFailedPasswordsForWipe = devicePolicyManager.getMaximumFailedPasswordsForWipe(null, i);
        int i4 = maximumFailedPasswordsForWipe > 0 ? maximumFailedPasswordsForWipe - currentFailedPasswordAttempts : Integer.MAX_VALUE;
        if (i4 < 5) {
            int profileWithMinimumFailedPasswordsForWipe = devicePolicyManager.getProfileWithMinimumFailedPasswordsForWipe(i);
            if (profileWithMinimumFailedPasswordsForWipe == i) {
                if (profileWithMinimumFailedPasswordsForWipe != 0) {
                    i3 = 3;
                }
            } else if (profileWithMinimumFailedPasswordsForWipe != -10000) {
                i3 = 2;
            }
            if (i4 > 0) {
                showAlmostAtWipeDialog(currentFailedPasswordAttempts, i4, i3);
            } else {
                Slog.i("KeyguardSecurityView", "Too many unlock attempts; user " + profileWithMinimumFailedPasswordsForWipe + " will be wiped!");
                showWipeDialog(currentFailedPasswordAttempts, i3);
            }
        }
        this.mLockPatternUtils.reportFailedPasswordAttempt(i);
        if (i2 > 0) {
            this.mLockPatternUtils.reportPasswordLockout(i2, i);
            this.mLockPatternUtils.reportPasswordLockout(i2, i);
            showLockoutView((long) i2);
        }
    }

    /* access modifiers changed from: protected */
    public void showLockoutView(long j) {
        View view = this.mLockoutView;
        if (view == null) {
            loadLockoutView();
        } else if (view.getVisibility() == 0) {
            return;
        }
        MiuiGxzwManager.getInstance().setShowLockoutView(true);
        this.mLockoutView.setVisibility(0);
        final TextView textView = (TextView) this.mLockoutView.findViewById(C0015R$id.phone_locked_timeout_id);
        AlphaAnimation alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
        alphaAnimation.setDuration(500);
        this.mLockoutView.startAnimation(alphaAnimation);
        new CountDownTimer(((long) Math.ceil(((double) j) / 1000.0d)) * 1000, 1000) {
            /* class com.android.keyguard.KeyguardSecurityContainer.AnonymousClass2 */

            public void onTick(long j) {
                KeyguardSecurityContainer.this.updateCountDown(textView, (long) ((int) Math.round(((double) j) / 1000.0d)));
            }

            public void onFinish() {
                KeyguardSecurityContainer.this.hideLockoutView();
            }
        }.start();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateCountDown(TextView textView, long j) {
        if (j <= 60) {
            textView.setText(getResources().getQuantityString(C0019R$plurals.phone_locked_timeout_seconds_string, (int) j, Long.valueOf(j)));
            return;
        }
        textView.setText(getResources().getQuantityString(C0019R$plurals.phone_locked_timeout_minutes_string, ((int) j) / 60, Long.valueOf(j / 60)));
    }

    /* access modifiers changed from: protected */
    public void hideLockoutView() {
        hideLockoutView(true);
    }

    /* access modifiers changed from: protected */
    public void hideLockoutView(boolean z) {
        if (z) {
            Animation loadAnimation = AnimationUtils.loadAnimation(((FrameLayout) this).mContext, 17432577);
            loadAnimation.setAnimationListener(new Animation.AnimationListener() {
                /* class com.android.keyguard.KeyguardSecurityContainer.AnonymousClass3 */

                public void onAnimationRepeat(Animation animation) {
                }

                public void onAnimationStart(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {
                    KeyguardSecurityContainer.this.mLockoutView.setVisibility(4);
                }
            });
            this.mLockoutView.startAnimation(loadAnimation);
        } else {
            this.mLockoutView.clearAnimation();
            this.mLockoutView.setVisibility(4);
        }
        MiuiGxzwManager.getInstance().setShowLockoutView(false);
    }

    private void loadLockoutView() {
        View inflate = View.inflate(getContext(), C0017R$layout.miui_unlockscreen_lockout, null);
        ((ViewGroup) getParent()).addView(inflate);
        View findViewById = inflate.findViewById(C0015R$id.unlockscreen_lockout_id);
        this.mLockoutView = findViewById;
        Button button = (Button) findViewById.findViewById(C0015R$id.foget_password);
        this.mFogetPasswordMethod = this.mLockoutView.findViewById(C0015R$id.forget_password_hint_container);
        this.mFogetPasswordSuggestion = this.mLockoutView.findViewById(C0015R$id.forget_password_suggesstion);
        this.mForgetPasswordMethodBack = (TextView) this.mFogetPasswordMethod.findViewById(C0015R$id.forget_password_method_back);
        this.mForgetPasswordMethodNext = (TextView) this.mFogetPasswordMethod.findViewById(C0015R$id.forget_password_method_next);
        if (getLayoutDirection() == 1) {
            this.mForgetPasswordMethodBack.setBackgroundResource(C0013R$drawable.miui_keyguard_forget_password_suggestion_right);
            this.mForgetPasswordMethodNext.setBackgroundResource(C0013R$drawable.miui_keyguard_forget_password_suggestion_left);
        }
        button.setOnClickListener(new View.OnClickListener() {
            /* class com.android.keyguard.KeyguardSecurityContainer.AnonymousClass4 */

            public void onClick(View view) {
                KeyguardSecurityContainer.this.setLockoutViewVisible(4);
                KeyguardSecurityContainer.this.mFogetPasswordMethod.setVisibility(0);
                ((TextView) KeyguardSecurityContainer.this.mFogetPasswordMethod.findViewById(C0015R$id.forget_password_method_content)).setText(Html.fromHtml(KeyguardSecurityContainer.this.getResources().getString(C0021R$string.phone_locked_foget_password_method_content)));
            }
        });
        this.mLockoutView.setOnTouchListener(new View.OnTouchListener() {
            /* class com.android.keyguard.KeyguardSecurityContainer.AnonymousClass5 */

            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() != 0) {
                    return true;
                }
                KeyguardSecurityContainer.this.mCallback.userActivity();
                return true;
            }
        });
        this.mForgetPasswordMethodNext.setOnClickListener(new View.OnClickListener() {
            /* class com.android.keyguard.KeyguardSecurityContainer.AnonymousClass6 */

            public void onClick(View view) {
                KeyguardSecurityContainer.this.mFogetPasswordMethod.setVisibility(4);
                KeyguardSecurityContainer.this.setLockoutViewVisible(4);
                KeyguardSecurityContainer.this.mFogetPasswordSuggestion.setVisibility(0);
                ((TextView) KeyguardSecurityContainer.this.mFogetPasswordSuggestion.findViewById(C0015R$id.forget_password_suggesstion_one)).setText(Html.fromHtml(KeyguardSecurityContainer.this.getResources().getString(C0021R$string.phone_locked_forget_password_suggesstion_one_content), new Html.ImageGetter() {
                    /* class com.android.keyguard.KeyguardSecurityContainer.AnonymousClass6.AnonymousClass1 */

                    public Drawable getDrawable(String str) {
                        Drawable drawable;
                        if (str == null) {
                            return null;
                        }
                        if ("POCO".equals(Build.BRAND)) {
                            drawable = KeyguardSecurityContainer.this.getResources().getDrawable(C0013R$drawable.miui_keyguard_forget_password_poco);
                        } else {
                            drawable = KeyguardSecurityContainer.this.getResources().getDrawable(C0013R$drawable.miui_keyguard_forget_password_mi);
                        }
                        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                        return drawable;
                    }
                }, null));
                ((TextView) KeyguardSecurityContainer.this.mFogetPasswordSuggestion.findViewById(C0015R$id.forget_password_suggesstion_two)).setText(Html.fromHtml(KeyguardSecurityContainer.this.getResources().getString(C0021R$string.phone_locked_forget_password_suggesstion_two_content)));
            }
        });
        this.mForgetPasswordMethodBack.setOnClickListener(new View.OnClickListener() {
            /* class com.android.keyguard.KeyguardSecurityContainer.AnonymousClass7 */

            public void onClick(View view) {
                KeyguardSecurityContainer.this.mFogetPasswordMethod.setVisibility(4);
                KeyguardSecurityContainer.this.setLockoutViewVisible(0);
            }
        });
        this.mFogetPasswordSuggestion.findViewById(C0015R$id.forget_password_suggesstion_ok).setOnClickListener(new View.OnClickListener() {
            /* class com.android.keyguard.KeyguardSecurityContainer.AnonymousClass8 */

            public void onClick(View view) {
                KeyguardSecurityContainer.this.mFogetPasswordSuggestion.setVisibility(4);
                KeyguardSecurityContainer.this.mFogetPasswordMethod.setVisibility(4);
                KeyguardSecurityContainer.this.setLockoutViewVisible(0);
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setLockoutViewVisible(int i) {
        this.mLockoutView.findViewById(C0015R$id.phone_locked_textview).setVisibility(i);
        this.mLockoutView.findViewById(C0015R$id.phone_locked_timeout_id).setVisibility(i);
        this.mLockoutView.findViewById(C0015R$id.foget_password).setVisibility(i);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$showPrimarySecurityScreen$0 */
    public /* synthetic */ KeyguardSecurityModel.SecurityMode lambda$showPrimarySecurityScreen$0$KeyguardSecurityContainer() {
        return this.mSecurityModel.getSecurityMode(KeyguardUpdateMonitor.getCurrentUser());
    }

    /* access modifiers changed from: package-private */
    public void showPrimarySecurityScreen(boolean z) {
        Log.v("KeyguardSecurityView", "showPrimarySecurityScreen(turningOff=" + z + ")");
        showSecurityScreen((KeyguardSecurityModel.SecurityMode) DejankUtils.whitelistIpcs(new Supplier() {
            /* class com.android.keyguard.$$Lambda$KeyguardSecurityContainer$2pPkYsoLI01tKHny_UaXkNxVqo */

            @Override // java.util.function.Supplier
            public final Object get() {
                return KeyguardSecurityContainer.this.lambda$showPrimarySecurityScreen$0$KeyguardSecurityContainer();
            }
        }));
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x00a6 A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x00b8  */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x00d1  */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x00d8  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean showNextSecurityScreenOrFinish(boolean r9, int r10, boolean r11) {
        /*
        // Method dump skipped, instructions count: 222
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.keyguard.KeyguardSecurityContainer.showNextSecurityScreenOrFinish(boolean, int, boolean):boolean");
    }

    private void showSecurityScreen(KeyguardSecurityModel.SecurityMode securityMode) {
        Log.d("KeyguardSecurityView", "showSecurityScreen(" + securityMode + ")");
        KeyguardSecurityModel.SecurityMode securityMode2 = this.mCurrentSecuritySelection;
        if (securityMode != securityMode2) {
            KeyguardSecurityView securityView = getSecurityView(securityMode2);
            KeyguardSecurityView securityView2 = getSecurityView(securityMode);
            if (securityView != null) {
                securityView.onPause();
                securityView.setKeyguardCallback(this.mNullCallback);
            }
            if (securityMode != KeyguardSecurityModel.SecurityMode.None) {
                securityView2.onResume(2);
                securityView2.setKeyguardCallback(this.mCallback);
            }
            int childCount = this.mSecurityViewFlipper.getChildCount();
            int securityViewIdForMode = getSecurityViewIdForMode(securityMode);
            boolean z = false;
            int i = 0;
            while (true) {
                if (i >= childCount) {
                    break;
                } else if (this.mSecurityViewFlipper.getChildAt(i).getId() == securityViewIdForMode) {
                    this.mSecurityViewFlipper.setDisplayedChild(i);
                    break;
                } else {
                    i++;
                }
            }
            this.mCurrentSecuritySelection = securityMode;
            this.mCurrentSecurityView = securityView2;
            SecurityCallback securityCallback = this.mSecurityCallback;
            if (securityMode != KeyguardSecurityModel.SecurityMode.None && securityView2.needsInput()) {
                z = true;
            }
            securityCallback.onSecurityModeChanged(securityMode, z);
        }
    }

    private int getSecurityViewIdForMode(KeyguardSecurityModel.SecurityMode securityMode) {
        int i = AnonymousClass11.$SwitchMap$com$android$keyguard$KeyguardSecurityModel$SecurityMode[securityMode.ordinal()];
        if (i == 1) {
            return C0015R$id.keyguard_pattern_view;
        }
        if (i == 2) {
            return C0015R$id.keyguard_pin_view;
        }
        if (i == 3) {
            return C0015R$id.keyguard_password_view;
        }
        if (i == 6) {
            return C0015R$id.keyguard_sim_pin_view;
        }
        if (i != 7) {
            return 0;
        }
        return C0015R$id.keyguard_sim_puk_view;
    }

    public int getLayoutIdFor(KeyguardSecurityModel.SecurityMode securityMode) {
        int i = AnonymousClass11.$SwitchMap$com$android$keyguard$KeyguardSecurityModel$SecurityMode[securityMode.ordinal()];
        if (i == 1) {
            return C0017R$layout.keyguard_pattern_view;
        }
        if (i == 2) {
            return C0017R$layout.keyguard_pin_view;
        }
        if (i == 3) {
            return C0017R$layout.keyguard_password_view;
        }
        if (i == 6) {
            return C0017R$layout.keyguard_sim_pin_view;
        }
        if (i != 7) {
            return 0;
        }
        return C0017R$layout.keyguard_sim_puk_view;
    }

    public KeyguardSecurityModel.SecurityMode getSecurityMode() {
        return this.mSecurityModel.getSecurityMode(KeyguardUpdateMonitor.getCurrentUser());
    }

    public KeyguardSecurityModel.SecurityMode getCurrentSecurityMode() {
        return this.mCurrentSecuritySelection;
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public boolean needsInput() {
        return this.mSecurityViewFlipper.needsInput();
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void setKeyguardCallback(KeyguardSecurityCallback keyguardSecurityCallback) {
        this.mSecurityViewFlipper.setKeyguardCallback(keyguardSecurityCallback);
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void reset() {
        this.mSecurityViewFlipper.reset();
        this.mDisappearAnimRunning = false;
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void showPromptReason(int i) {
        if (this.mCurrentSecuritySelection != KeyguardSecurityModel.SecurityMode.None) {
            if (i != 0) {
                Log.i("KeyguardSecurityView", "Strong auth required, reason: " + i);
            }
            getSecurityView(this.mCurrentSecuritySelection).showPromptReason(i);
        }
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void showMessage(String str, String str2, int i) {
        KeyguardSecurityModel.SecurityMode securityMode = this.mCurrentSecuritySelection;
        if (securityMode != KeyguardSecurityModel.SecurityMode.None) {
            getSecurityView(securityMode).showMessage(str, str2, i);
        }
    }

    @Override // com.android.keyguard.KeyguardSecurityView
    public void applyHintAnimation(long j) {
        KeyguardSecurityModel.SecurityMode securityMode = this.mCurrentSecuritySelection;
        if (securityMode != KeyguardSecurityModel.SecurityMode.None) {
            getSecurityView(securityMode).applyHintAnimation(j);
        }
    }
}
