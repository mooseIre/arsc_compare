package com.android.systemui.statusbar.phone;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.UserManagerCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Property;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.EmergencyButton;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.keyguard.analytics.AnalyticsHelper;
import com.android.keyguard.magazine.LockScreenMagazineController;
import com.android.keyguard.utils.PackageUtils;
import com.android.systemui.Dependency;
import com.android.systemui.Interpolators;
import com.android.systemui.assist.AssistManager;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.IntentButtonProvider;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.KeyguardAffordanceView;
import com.android.systemui.statusbar.KeyguardIndicationController;
import com.android.systemui.statusbar.phone.UnlockMethodCache;
import com.android.systemui.statusbar.policy.AccessibilityController;
import com.android.systemui.statusbar.policy.ExtensionController;
import com.android.systemui.statusbar.policy.FlashlightController;
import java.util.function.Consumer;
import java.util.function.Supplier;
import miui.maml.animation.interpolater.SineEaseInOutInterpolater;
import miui.os.Build;
import miui.util.FeatureParser;

public class KeyguardBottomAreaView extends FrameLayout implements View.OnClickListener, UnlockMethodCache.OnUnlockMethodChangedListener, AccessibilityController.AccessibilityStateChangedCallback, View.OnLongClickListener {
    public static final Intent INSECURE_CAMERA_INTENT = new Intent("android.media.action.STILL_IMAGE_CAMERA");
    private AccessibilityController mAccessibilityController;
    private View.AccessibilityDelegate mAccessibilityDelegate;
    /* access modifiers changed from: private */
    public boolean mDarkMode;
    private int mDensityDpi;
    private final BroadcastReceiver mDevicePolicyReceiver;
    private boolean mDozing;
    private EmergencyButton mEmergencyButton;
    private TextView mEnterpriseDisclosure;
    private float mFontScale;
    private ViewGroup mIndicationArea;
    private KeyguardIndicationController mIndicationController;
    /* access modifiers changed from: private */
    public boolean mIsSuperSavePowerMode;
    /* access modifiers changed from: private */
    public KeyguardUpdateMonitor mKeyguardUpdateMonitor;
    private String mLanguage;
    /* access modifiers changed from: private */
    public KeyguardAffordanceView mLeftAffordanceView;
    private LinearLayout mLeftAffordanceViewLayout;
    private TextView mLeftAffordanceViewTips;
    private IntentButtonProvider.IntentButton mLeftButton;
    private AnimatorSet mLeftButtonLayoutAnimatorSet;
    private ExtensionController.Extension<IntentButtonProvider.IntentButton> mLeftExtension;
    /* access modifiers changed from: private */
    public boolean mLeftIntentAvailable;
    private LockPatternUtils mLockPatternUtils;
    /* access modifiers changed from: private */
    public LockScreenMagazineController mLockScreenMagazineController;
    private LockscreenGestureLogger mLockscreenGestureLogger;
    /* access modifiers changed from: private */
    public NotificationPanelView mNotificationPanelView;
    private int mOrientation;
    /* access modifiers changed from: private */
    public KeyguardAffordanceView mRightAffordanceView;
    private LinearLayout mRightAffordanceViewLayout;
    private TextView mRightAffordanceViewTips;
    private IntentButtonProvider.IntentButton mRightButton;
    private AnimatorSet mRightButtonLayoutAnimatorSet;
    private ExtensionController.Extension<IntentButtonProvider.IntentButton> mRightExtension;
    /* access modifiers changed from: private */
    public boolean mRightIntentAvailable;
    private StatusBar mStatusBar;
    private UnlockMethodCache mUnlockMethodCache;
    private final KeyguardUpdateMonitorCallback mUpdateMonitorCallback;
    /* access modifiers changed from: private */
    public boolean mUserSetupComplete;
    /* access modifiers changed from: private */
    public boolean mUserUnlocked;

    /* renamed from: com.android.systemui.statusbar.phone.KeyguardBottomAreaView$1  reason: invalid class name */
    class AnonymousClass1 implements ServiceConnection {
    }

    public boolean hasOverlappingRendering() {
        return false;
    }

    public void onStateChanged(boolean z, boolean z2) {
    }

    static {
        new Intent("android.media.action.STILL_IMAGE_CAMERA_SECURE").addFlags(8388608);
        new Intent("android.intent.action.DIAL");
    }

    public KeyguardBottomAreaView(Context context) {
        this(context, (AttributeSet) null);
    }

    public KeyguardBottomAreaView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public KeyguardBottomAreaView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public KeyguardBottomAreaView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mRightButton = new DefaultRightButton(this, (AnonymousClass1) null);
        this.mLeftButton = new DefaultLeftButton(this, (AnonymousClass1) null);
        this.mLockscreenGestureLogger = new LockscreenGestureLogger();
        this.mAccessibilityDelegate = new View.AccessibilityDelegate() {
            public void onInitializeAccessibilityNodeInfo(View view, AccessibilityNodeInfo accessibilityNodeInfo) {
                String str;
                super.onInitializeAccessibilityNodeInfo(view, accessibilityNodeInfo);
                if (view == KeyguardBottomAreaView.this.mRightAffordanceView) {
                    str = KeyguardBottomAreaView.this.getResources().getString(R.string.camera_label);
                } else {
                    str = (view != KeyguardBottomAreaView.this.mLeftAffordanceView || !KeyguardBottomAreaView.this.mKeyguardUpdateMonitor.isSupportLockScreenMagazineLeft()) ? null : KeyguardBottomAreaView.this.getResources().getString(R.string.lock_screen_magazine_label);
                }
                accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(16, str));
            }

            public boolean performAccessibilityAction(View view, int i, Bundle bundle) {
                if (i == 16) {
                    if (view == KeyguardBottomAreaView.this.mRightAffordanceView) {
                        KeyguardBottomAreaView.this.launchCamera("lockscreen_affordance");
                        return true;
                    } else if (view == KeyguardBottomAreaView.this.mLeftAffordanceView) {
                        if (KeyguardBottomAreaView.this.mKeyguardUpdateMonitor.isSupportLockScreenMagazineLeft()) {
                            KeyguardBottomAreaView.this.launchLockScreenMagazine();
                        }
                        return true;
                    }
                }
                return super.performAccessibilityAction(view, i, bundle);
            }
        };
        this.mDevicePolicyReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                KeyguardBottomAreaView.this.post(new Runnable() {
                    public void run() {
                        KeyguardBottomAreaView.this.updateCameraVisibility();
                    }
                });
            }
        };
        this.mUpdateMonitorCallback = new KeyguardUpdateMonitorCallback() {
            public void onStartedGoingToSleep(int i) {
            }

            public void onUserSwitchComplete(int i) {
                KeyguardBottomAreaView.this.updateCameraVisibility();
            }

            public void onUserUnlocked() {
                KeyguardBottomAreaView.this.mNotificationPanelView.getLeftView().initLeftView();
                KeyguardBottomAreaView.this.mNotificationPanelView.getLeftView().uploadData();
                boolean unused = KeyguardBottomAreaView.this.mUserUnlocked = true;
                KeyguardBottomAreaView.this.handleIntentAvailable();
            }

            public void onRegionChanged() {
                KeyguardBottomAreaView.this.mNotificationPanelView.getLeftView().initLeftView();
            }

            public void onSuperSavePowerChanged(boolean z) {
                boolean unused = KeyguardBottomAreaView.this.mIsSuperSavePowerMode = z;
                KeyguardBottomAreaView.this.updateLeftAffordance();
            }

            public void onLockScreenMagazineStatusChanged() {
                KeyguardBottomAreaView.this.handleIntentAvailable();
            }
        };
        this.mDarkMode = false;
        this.mKeyguardUpdateMonitor = KeyguardUpdateMonitor.getInstance(this.mContext);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mLockPatternUtils = new LockPatternUtils(this.mContext);
        this.mLockScreenMagazineController = LockScreenMagazineController.getInstance(this.mContext);
        this.mEmergencyButton = (EmergencyButton) findViewById(R.id.emergency_call_button);
        this.mRightAffordanceViewLayout = (LinearLayout) findViewById(R.id.right_button_layout);
        this.mLeftAffordanceViewLayout = (LinearLayout) findViewById(R.id.left_button_layout);
        this.mRightAffordanceView = (KeyguardAffordanceView) findViewById(R.id.right_button);
        this.mLeftAffordanceView = (KeyguardAffordanceView) findViewById(R.id.left_button);
        this.mRightAffordanceViewTips = (TextView) findViewById(R.id.right_button_tips);
        this.mLeftAffordanceViewTips = (TextView) findViewById(R.id.left_button_tips);
        initTipsView(true);
        initTipsView(false);
        this.mIndicationArea = (ViewGroup) findViewById(R.id.keyguard_indication_area);
        this.mEnterpriseDisclosure = (TextView) findViewById(R.id.keyguard_indication_enterprise_disclosure);
        watchForCameraPolicyChanges();
        updateCameraVisibility();
        UnlockMethodCache instance = UnlockMethodCache.getInstance(getContext());
        this.mUnlockMethodCache = instance;
        instance.addListener(this);
        updateEmergencyButton();
        setClipChildren(false);
        setClipToPadding(false);
        new LockPatternUtils(this.mContext);
        this.mRightAffordanceView.setOnClickListener(this);
        this.mLeftAffordanceView.setOnClickListener(this);
        initAccessibility();
        ActivityStarter activityStarter = (ActivityStarter) Dependency.get(ActivityStarter.class);
        FlashlightController flashlightController = (FlashlightController) Dependency.get(FlashlightController.class);
        this.mAccessibilityController = (AccessibilityController) Dependency.get(AccessibilityController.class);
        AssistManager assistManager = (AssistManager) Dependency.get(AssistManager.class);
        updateLeftAffordance();
        PowerManager powerManager = (PowerManager) this.mContext.getSystemService("power");
        this.mKeyguardUpdateMonitor.registerCallback(this.mUpdateMonitorCallback);
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        Class<IntentButtonProvider> cls = IntentButtonProvider.class;
        Class<IntentButtonProvider.IntentButton> cls2 = IntentButtonProvider.IntentButton.class;
        Class cls3 = ExtensionController.class;
        super.onAttachedToWindow();
        this.mAccessibilityController.addStateChangedCallback(this);
        ExtensionController.ExtensionBuilder<IntentButtonProvider.IntentButton> newExtension = ((ExtensionController) Dependency.get(cls3)).newExtension(cls2);
        newExtension.withPlugin(cls, "com.android.systemui.action.PLUGIN_LOCKSCREEN_RIGHT_BUTTON", new ExtensionController.PluginConverter<IntentButtonProvider.IntentButton, IntentButtonProvider>() {
            public IntentButtonProvider.IntentButton getInterfaceFromPlugin(IntentButtonProvider intentButtonProvider) {
                return intentButtonProvider.getIntentButton();
            }
        });
        newExtension.withDefault(new Supplier<IntentButtonProvider.IntentButton>() {
            public IntentButtonProvider.IntentButton get() {
                return new DefaultRightButton(KeyguardBottomAreaView.this, (AnonymousClass1) null);
            }
        });
        newExtension.withCallback(new Consumer<IntentButtonProvider.IntentButton>() {
            public void accept(IntentButtonProvider.IntentButton intentButton) {
                KeyguardBottomAreaView.this.setRightButton(intentButton);
            }
        });
        this.mRightExtension = newExtension.build();
        ExtensionController.ExtensionBuilder<IntentButtonProvider.IntentButton> newExtension2 = ((ExtensionController) Dependency.get(cls3)).newExtension(cls2);
        newExtension2.withPlugin(cls, "com.android.systemui.action.PLUGIN_LOCKSCREEN_LEFT_BUTTON", new ExtensionController.PluginConverter<IntentButtonProvider.IntentButton, IntentButtonProvider>() {
            public IntentButtonProvider.IntentButton getInterfaceFromPlugin(IntentButtonProvider intentButtonProvider) {
                return intentButtonProvider.getIntentButton();
            }
        });
        newExtension2.withDefault(new Supplier<IntentButtonProvider.IntentButton>() {
            public IntentButtonProvider.IntentButton get() {
                return new DefaultLeftButton(KeyguardBottomAreaView.this, (AnonymousClass1) null);
            }
        });
        newExtension2.withCallback(new Consumer<IntentButtonProvider.IntentButton>() {
            public void accept(IntentButtonProvider.IntentButton intentButton) {
                KeyguardBottomAreaView.this.setLeftButton(intentButton);
            }
        });
        this.mLeftExtension = newExtension2.build();
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mAccessibilityController.removeStateChangedCallback(this);
        this.mRightExtension.destroy();
        this.mLeftExtension.destroy();
    }

    private void initAccessibility() {
        this.mLeftAffordanceView.setAccessibilityDelegate(this.mAccessibilityDelegate);
        this.mRightAffordanceView.setAccessibilityDelegate(this.mAccessibilityDelegate);
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        updateEmergencyButton();
        float f = configuration.fontScale;
        if (this.mFontScale != f) {
            updateViewsTextSize();
            this.mFontScale = f;
        }
        int i = configuration.densityDpi;
        if (i != this.mDensityDpi) {
            updateViewsLayoutParams();
            updateViewsTextSize();
            updateDrawableResource();
            this.mDensityDpi = i;
        }
        String language = configuration.locale.getLanguage();
        if (!TextUtils.isEmpty(language) && !language.equals(this.mLanguage)) {
            initTipsView(false);
            this.mLanguage = language;
        }
        int i2 = configuration.orientation;
        if (i2 != this.mOrientation) {
            this.mOrientation = i2;
            updateCameraVisibility();
        }
    }

    private void updateViewsTextSize() {
        float dimensionPixelSize = (float) getResources().getDimensionPixelSize(R.dimen.keyguard_bottom_button_tips_text_size);
        this.mLeftAffordanceViewTips.setTextSize(0, dimensionPixelSize);
        this.mRightAffordanceViewTips.setTextSize(0, dimensionPixelSize);
    }

    private void updateDrawableResource() {
        initTipsView(true);
        initTipsView(false);
    }

    private void updateViewsLayoutParams() {
        int dimensionPixelOffset = getResources().getDimensionPixelOffset(R.dimen.keyguard_affordance_width);
        int dimensionPixelOffset2 = getResources().getDimensionPixelOffset(R.dimen.keyguard_affordance_height);
        this.mLockScreenMagazineController.initPreMainEntryIcon();
        this.mLeftAffordanceViewLayout.setPaddingRelative(0, 0, dimensionPixelOffset, 0);
        this.mRightAffordanceViewLayout.setPaddingRelative(dimensionPixelOffset, 0, 0, 0);
        IntentButtonProvider.IntentButton.IconState icon = this.mLeftButton.getIcon();
        this.mLeftAffordanceView.setImageDrawable(icon.drawable, icon.tint);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) this.mLeftAffordanceView.getLayoutParams();
        layoutParams.height = dimensionPixelOffset2;
        layoutParams.width = dimensionPixelOffset;
        this.mLeftAffordanceView.setLayoutParams(layoutParams);
        LinearLayout.LayoutParams layoutParams2 = (LinearLayout.LayoutParams) this.mLeftAffordanceViewTips.getLayoutParams();
        layoutParams2.setMarginStart(getResources().getDimensionPixelOffset(R.dimen.keyguard_bottom_left_button_tips_margin_start));
        this.mLeftAffordanceViewTips.setLayoutParams(layoutParams2);
        IntentButtonProvider.IntentButton.IconState icon2 = this.mRightButton.getIcon();
        this.mRightAffordanceView.setImageDrawable(icon2.drawable, icon2.tint);
        LinearLayout.LayoutParams layoutParams3 = (LinearLayout.LayoutParams) this.mRightAffordanceView.getLayoutParams();
        layoutParams3.height = dimensionPixelOffset2;
        layoutParams3.width = dimensionPixelOffset;
        this.mRightAffordanceView.setLayoutParams(layoutParams3);
        LinearLayout.LayoutParams layoutParams4 = (LinearLayout.LayoutParams) this.mRightAffordanceViewTips.getLayoutParams();
        layoutParams4.setMarginEnd(getResources().getDimensionPixelOffset(R.dimen.keyguard_bottom_right_button_tips_margin_end));
        this.mRightAffordanceViewTips.setLayoutParams(layoutParams4);
        FrameLayout.LayoutParams layoutParams5 = (FrameLayout.LayoutParams) this.mIndicationArea.getLayoutParams();
        layoutParams5.height = getResources().getDimensionPixelOffset(R.dimen.keyguard_affordance_height);
        layoutParams5.setMarginsRelative(dimensionPixelOffset, 0, dimensionPixelOffset, 0);
        this.mIndicationArea.setLayoutParams(layoutParams5);
    }

    /* access modifiers changed from: private */
    public void updateRightAffordanceIcon() {
        IntentButtonProvider.IntentButton.IconState icon = this.mRightButton.getIcon();
        this.mRightAffordanceView.setVisibility(icon.isVisible ? 0 : 8);
        this.mRightAffordanceView.setImageDrawable(icon.drawable, icon.tint);
        this.mRightAffordanceView.setContentDescription(icon.contentDescription);
        initTipsView(false);
    }

    public void setStatusBar(StatusBar statusBar) {
        this.mStatusBar = statusBar;
        updateCameraVisibility();
    }

    public void setUserSetupComplete(boolean z) {
        this.mUserSetupComplete = z;
        updateCameraVisibility();
        updateLeftAffordanceIcon();
    }

    private Intent getCameraIntent() {
        return this.mRightButton.getIntent();
    }

    private Intent getLockScreenMagazineIntent() {
        return this.mLeftButton.getIntent();
    }

    public ResolveInfo resolveCameraIntent() {
        return PackageUtils.resolveIntent(this.mContext, getCameraIntent(), 65536);
    }

    /* access modifiers changed from: private */
    public ResolveInfo resolveLockScreenMagazineIntent() {
        return PackageUtils.resolveIntent(this.mContext, getLockScreenMagazineIntent());
    }

    /* access modifiers changed from: private */
    public void updateCameraVisibility() {
        KeyguardAffordanceView keyguardAffordanceView = this.mRightAffordanceView;
        if (keyguardAffordanceView != null) {
            keyguardAffordanceView.setVisibility(this.mRightButton.getIcon().isVisible ? 0 : 8);
        }
    }

    private void updateLeftAffordanceIcon() {
        IntentButtonProvider.IntentButton.IconState icon = this.mLeftButton.getIcon();
        this.mLeftAffordanceView.setVisibility(icon.isVisible ? 0 : 8);
        this.mLeftAffordanceView.setImageDrawable(icon.drawable, icon.tint);
        this.mLeftAffordanceView.setContentDescription(icon.contentDescription);
        initTipsView(true);
    }

    /* access modifiers changed from: private */
    public boolean isCameraDisabledByDpm() {
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) getContext().getSystemService("device_policy");
        if (devicePolicyManager == null || this.mStatusBar == null) {
            return false;
        }
        boolean z = (devicePolicyManager.getKeyguardDisabledFeatures((ComponentName) null, KeyguardUpdateMonitor.getCurrentUser()) & 2) != 0 && this.mStatusBar.isKeyguardSecure();
        if (devicePolicyManager.getCameraDisabled((ComponentName) null) || z) {
            return true;
        }
        return false;
    }

    private void watchForCameraPolicyChanges() {
        this.mUserUnlocked = UserManagerCompat.isUserUnlocked(UserManager.get(this.mContext), KeyguardUpdateMonitor.getCurrentUser());
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.app.action.DEVICE_POLICY_MANAGER_STATE_CHANGED");
        getContext().registerReceiverAsUser(this.mDevicePolicyReceiver, UserHandle.ALL, intentFilter, (String) null, (Handler) null);
    }

    public void onClick(View view) {
        if (view == this.mRightAffordanceView) {
            startButtonLayoutAnimate(false);
        } else if (view == this.mLeftAffordanceView) {
            startButtonLayoutAnimate(true);
        }
    }

    public void startButtonLayoutAnimate(boolean z) {
        startButtonLayoutAnimate(z, false);
    }

    public void startButtonLayoutAnimate(boolean z, boolean z2) {
        if (this.mLeftButtonLayoutAnimatorSet == null) {
            this.mLeftButtonLayoutAnimatorSet = getButtonLayoutAnimate(this.mLeftAffordanceViewLayout, this.mLeftAffordanceViewTips, true);
        }
        if (this.mRightButtonLayoutAnimatorSet == null) {
            this.mRightButtonLayoutAnimatorSet = getButtonLayoutAnimate(this.mRightAffordanceViewLayout, this.mRightAffordanceViewTips, false);
        }
        if (z2 || (!this.mLeftButtonLayoutAnimatorSet.isRunning() && !this.mRightButtonLayoutAnimatorSet.isRunning())) {
            this.mLeftButtonLayoutAnimatorSet.cancel();
            this.mRightButtonLayoutAnimatorSet.cancel();
            if (z && this.mLeftButton.getIcon().isVisible) {
                this.mLeftButtonLayoutAnimatorSet.start();
            }
            if (!z && this.mRightButton.getIcon().isVisible) {
                this.mRightButtonLayoutAnimatorSet.start();
            }
        }
    }

    private AnimatorSet getButtonLayoutAnimate(View view, TextView textView, boolean z) {
        View view2 = view;
        final TextView textView2 = textView;
        Property property = FrameLayout.TRANSLATION_X;
        AnimatorSet animatorSet = new AnimatorSet();
        float f = z ? -1.0f : 1.0f;
        float f2 = -50.0f * f;
        ObjectAnimator ofFloat = ObjectAnimator.ofFloat(view2, property, new float[]{0.0f, f2});
        ofFloat.setDuration(150);
        float f3 = 10.0f * f;
        ObjectAnimator ofFloat2 = ObjectAnimator.ofFloat(view2, property, new float[]{f2, f3});
        ofFloat2.setDuration(150);
        float f4 = -8.0f * f;
        ObjectAnimator ofFloat3 = ObjectAnimator.ofFloat(view2, property, new float[]{f3, f4});
        ofFloat3.setDuration(100);
        float f5 = f * 5.0f;
        ObjectAnimator ofFloat4 = ObjectAnimator.ofFloat(view2, property, new float[]{f4, f5});
        ofFloat4.setDuration(100);
        ObjectAnimator ofFloat5 = ObjectAnimator.ofFloat(view2, property, new float[]{f5, 0.0f});
        ofFloat5.setDuration(100);
        ObjectAnimator ofFloat6 = ObjectAnimator.ofFloat(textView2, FrameLayout.ALPHA, new float[]{1.0f, 0.0f});
        ofFloat6.setDuration(500);
        ofFloat6.setStartDelay(1000);
        animatorSet.play(ofFloat).before(ofFloat2);
        animatorSet.play(ofFloat2).before(ofFloat3);
        animatorSet.play(ofFloat3).before(ofFloat4);
        animatorSet.play(ofFloat4).before(ofFloat5);
        animatorSet.play(ofFloat5).before(ofFloat6);
        animatorSet.setInterpolator(new SineEaseInOutInterpolater());
        animatorSet.addListener(new AnimatorListenerAdapter() {
            public void onAnimationStart(Animator animator) {
                textView2.setVisibility(0);
                textView2.setTextColor(KeyguardBottomAreaView.this.getTextColor());
                textView2.setAlpha(1.0f);
                KeyguardBottomAreaView.this.handleBottomButtonClicked(true);
            }

            public void onAnimationCancel(Animator animator) {
                KeyguardBottomAreaView.this.handleBottomButtonClicked(false);
            }

            public void onAnimationEnd(Animator animator) {
                textView2.setVisibility(8);
                KeyguardBottomAreaView.this.handleBottomButtonClicked(false);
            }
        });
        return animatorSet;
    }

    public void initTipsView(boolean z) {
        String str;
        if (z) {
            boolean isSupportLockScreenMagazineLeft = this.mKeyguardUpdateMonitor.isSupportLockScreenMagazineLeft();
            TextView textView = this.mLeftAffordanceViewTips;
            if (isSupportLockScreenMagazineLeft) {
                str = this.mContext.getString(R.string.open_lock_screen_magazine_hint_text);
            } else {
                str = this.mContext.getString(R.string.open_remote_center_hint_text);
            }
            textView.setText(str);
            this.mLeftAffordanceViewTips.setCompoundDrawablesWithIntrinsicBounds((Drawable) null, (Drawable) null, this.mContext.getDrawable(this.mDarkMode ? R.drawable.keyguard_bottom_guide_right_arrow_dark : R.drawable.keyguard_bottom_guide_right_arrow), (Drawable) null);
            return;
        }
        this.mRightAffordanceViewTips.setText(this.mContext.getString(R.string.open_camera_hint_text));
        this.mRightAffordanceViewTips.setCompoundDrawablesWithIntrinsicBounds(this.mContext.getDrawable(this.mDarkMode ? R.drawable.keyguard_bottom_guide_left_arrow_dark : R.drawable.keyguard_bottom_guide_left_arrow), (Drawable) null, (Drawable) null, (Drawable) null);
    }

    public void handleBottomButtonClicked(boolean z) {
        this.mKeyguardUpdateMonitor.handleBottomAreaButtonClicked(z);
    }

    /* access modifiers changed from: private */
    public int getTextColor() {
        return this.mContext.getResources().getColor(this.mDarkMode ? R.color.miui_common_unlock_screen_common_dark_text_color : R.color.miui_default_lock_screen_unlock_bottom_tips_text_color);
    }

    public boolean onLongClick(View view) {
        handleTrustCircleClick();
        return true;
    }

    private void handleTrustCircleClick() {
        this.mLockscreenGestureLogger.write(getContext(), 191, 0, 0);
        this.mIndicationController.showTransientIndication((int) R.string.keyguard_indication_trust_disabled);
        this.mLockPatternUtils.requireCredentialEntry(KeyguardUpdateMonitor.getCurrentUser());
    }

    public void launchCamera(String str) {
        AnalyticsHelper.getInstance(this.mContext).recordKeyguardAction("action_enter_camera_view");
        AnalyticsHelper.getInstance(this.mContext).trackPageStart("action_enter_camera_view");
        this.mContext.startActivityAsUser(getCameraIntent(), UserHandle.CURRENT);
    }

    public void launchLockScreenMagazine() {
        Intent lockScreenMagazineIntent = getLockScreenMagazineIntent();
        if (lockScreenMagazineIntent != null) {
            AnalyticsHelper.getInstance(this.mContext).trackPageStart("action_enter_left_view");
            this.mContext.startActivityAsUser(lockScreenMagazineIntent, UserHandle.CURRENT);
        }
    }

    /* access modifiers changed from: protected */
    public void onVisibilityChanged(View view, int i) {
        super.onVisibilityChanged(view, i);
        if (view == this && i == 0) {
            updateCameraVisibility();
        }
    }

    public KeyguardAffordanceView getLeftView() {
        return this.mLeftAffordanceView;
    }

    public KeyguardAffordanceView getRightView() {
        return this.mRightAffordanceView;
    }

    public ViewGroup getLeftViewLayout() {
        return this.mLeftAffordanceViewLayout;
    }

    public ViewGroup getRightViewLayout() {
        return this.mRightAffordanceViewLayout;
    }

    public void onUnlockMethodStateChanged() {
        updateCameraVisibility();
    }

    public void startFinishDozeAnimation() {
        long j = 0;
        if (this.mLeftAffordanceView.getVisibility() == 0) {
            startFinishDozeAnimationElement(this.mLeftAffordanceView, 0);
            j = 48;
        }
        long j2 = j + 48;
        if (this.mRightAffordanceView.getVisibility() == 0) {
            startFinishDozeAnimationElement(this.mRightAffordanceView, j2);
        }
        this.mIndicationArea.setAlpha(0.0f);
        this.mIndicationArea.animate().alpha(1.0f).setInterpolator(Interpolators.LINEAR_OUT_SLOW_IN).setDuration(700);
    }

    private void startFinishDozeAnimationElement(View view, long j) {
        view.setAlpha(0.0f);
        view.setTranslationY((float) (view.getHeight() / 2));
        view.animate().alpha(1.0f).translationY(0.0f).setInterpolator(Interpolators.LINEAR_OUT_SLOW_IN).setStartDelay(j).setDuration(250);
    }

    public void setKeyguardIndicationController(KeyguardIndicationController keyguardIndicationController) {
        this.mIndicationController = keyguardIndicationController;
        keyguardIndicationController.setDarkMode(this.mDarkMode);
    }

    public void setNotificationPanelView(NotificationPanelView notificationPanelView) {
        this.mNotificationPanelView = notificationPanelView;
    }

    public void updateLeftAffordance() {
        updateLeftAffordanceIcon();
    }

    public void onKeyguardShowingChanged() {
        handleIntentAvailable();
    }

    /* access modifiers changed from: private */
    public void handleIntentAvailable() {
        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
            public void run() {
                KeyguardBottomAreaView keyguardBottomAreaView = KeyguardBottomAreaView.this;
                boolean z = true;
                boolean unused = keyguardBottomAreaView.mRightIntentAvailable = keyguardBottomAreaView.resolveCameraIntent() != null;
                KeyguardBottomAreaView keyguardBottomAreaView2 = KeyguardBottomAreaView.this;
                if (keyguardBottomAreaView2.resolveLockScreenMagazineIntent() == null) {
                    z = false;
                }
                boolean unused2 = keyguardBottomAreaView2.mLeftIntentAvailable = z;
                KeyguardBottomAreaView.this.post(new Runnable() {
                    public void run() {
                        KeyguardBottomAreaView.this.updateLeftAffordance();
                        KeyguardBottomAreaView.this.updateRightAffordanceIcon();
                    }
                });
            }
        });
    }

    /* access modifiers changed from: private */
    public void setRightButton(IntentButtonProvider.IntentButton intentButton) {
        this.mRightButton = intentButton;
        updateRightAffordanceIcon();
        updateCameraVisibility();
    }

    /* access modifiers changed from: private */
    public void setLeftButton(IntentButtonProvider.IntentButton intentButton) {
        this.mLeftButton = intentButton;
        boolean z = intentButton instanceof DefaultLeftButton;
        updateLeftAffordance();
    }

    public void setDozing(boolean z, boolean z2) {
        if (this.mDozing != z) {
            this.mDozing = z;
            if (!z && z2) {
                startFinishDozeAnimation();
            }
        }
    }

    /* access modifiers changed from: private */
    public Drawable getLockScreenMagazineMainEntryIcon() {
        if (this.mDarkMode) {
            Drawable preMainEntryResDarkIcon = this.mLockScreenMagazineController.getPreMainEntryResDarkIcon();
            if (preMainEntryResDarkIcon != null) {
                return preMainEntryResDarkIcon;
            }
            return this.mContext.getDrawable(R.drawable.keyguard_bottom_lock_screen_magazine_img_dark);
        }
        Drawable preMainEntryResLightIcon = this.mLockScreenMagazineController.getPreMainEntryResLightIcon();
        return preMainEntryResLightIcon != null ? preMainEntryResLightIcon : this.mContext.getDrawable(R.drawable.keyguard_bottom_lock_screen_magazine_img);
    }

    private class DefaultLeftButton implements IntentButtonProvider.IntentButton {
        private IntentButtonProvider.IntentButton.IconState mIconState;

        private DefaultLeftButton() {
            this.mIconState = new IntentButtonProvider.IntentButton.IconState();
        }

        /* synthetic */ DefaultLeftButton(KeyguardBottomAreaView keyguardBottomAreaView, AnonymousClass1 r2) {
            this();
        }

        public IntentButtonProvider.IntentButton.IconState getIcon() {
            Drawable drawable;
            boolean z = true;
            if (KeyguardBottomAreaView.this.mKeyguardUpdateMonitor.isSupportLockScreenMagazineLeft()) {
                IntentButtonProvider.IntentButton.IconState iconState = this.mIconState;
                if (!KeyguardBottomAreaView.this.mUserSetupComplete || !KeyguardBottomAreaView.this.mUserUnlocked || !KeyguardBottomAreaView.this.mNotificationPanelView.getLeftView().isSupportRightMove() || FeatureParser.getBoolean("is_pad", false) || !KeyguardBottomAreaView.this.mLeftIntentAvailable || KeyguardBottomAreaView.this.mIsSuperSavePowerMode) {
                    z = false;
                }
                iconState.isVisible = z;
                this.mIconState.drawable = KeyguardBottomAreaView.this.getLockScreenMagazineMainEntryIcon();
                this.mIconState.contentDescription = KeyguardBottomAreaView.this.mContext.getString(R.string.accessibility_left_lock_screen_magazine_button);
            } else {
                IntentButtonProvider.IntentButton.IconState iconState2 = this.mIconState;
                if (!KeyguardBottomAreaView.this.mUserSetupComplete || !KeyguardBottomAreaView.this.mUserUnlocked || !KeyguardBottomAreaView.this.mNotificationPanelView.getLeftView().isSupportRightMove() || FeatureParser.getBoolean("is_pad", false) || KeyguardBottomAreaView.this.mIsSuperSavePowerMode) {
                    z = false;
                }
                iconState2.isVisible = z;
                IntentButtonProvider.IntentButton.IconState iconState3 = this.mIconState;
                if (KeyguardBottomAreaView.this.mDarkMode) {
                    drawable = KeyguardBottomAreaView.this.mContext.getDrawable(R.drawable.keyguard_bottom_remote_center_img_dark);
                } else {
                    drawable = KeyguardBottomAreaView.this.mContext.getDrawable(R.drawable.keyguard_bottom_remote_center_img);
                }
                iconState3.drawable = drawable;
                this.mIconState.contentDescription = KeyguardBottomAreaView.this.mContext.getString(R.string.accessibility_left_control_center_button);
            }
            IntentButtonProvider.IntentButton.IconState iconState4 = this.mIconState;
            iconState4.tint = false;
            return iconState4;
        }

        public Intent getIntent() {
            if (!KeyguardBottomAreaView.this.mKeyguardUpdateMonitor.isSupportLockScreenMagazineLeft()) {
                return null;
            }
            Intent preLeftScreenIntent = KeyguardBottomAreaView.this.mLockScreenMagazineController.getPreLeftScreenIntent();
            if (!Build.IS_INTERNATIONAL_BUILD || preLeftScreenIntent == null) {
                return preLeftScreenIntent;
            }
            preLeftScreenIntent.putExtra("entry_source", "swipe");
            return preLeftScreenIntent;
        }
    }

    private class DefaultRightButton implements IntentButtonProvider.IntentButton {
        private IntentButtonProvider.IntentButton.IconState mIconState;

        private DefaultRightButton() {
            this.mIconState = new IntentButtonProvider.IntentButton.IconState();
        }

        /* synthetic */ DefaultRightButton(KeyguardBottomAreaView keyguardBottomAreaView, AnonymousClass1 r2) {
            this();
        }

        public IntentButtonProvider.IntentButton.IconState getIcon() {
            Drawable drawable;
            this.mIconState.isVisible = !KeyguardBottomAreaView.this.isCameraDisabledByDpm() && KeyguardBottomAreaView.this.mRightIntentAvailable && KeyguardBottomAreaView.this.getResources().getBoolean(R.bool.config_keyguardShowCameraAffordance) && KeyguardBottomAreaView.this.mUserSetupComplete && KeyguardBottomAreaView.this.mUserUnlocked && KeyguardBottomAreaView.this.getResources().getConfiguration().orientation != 2;
            IntentButtonProvider.IntentButton.IconState iconState = this.mIconState;
            if (KeyguardBottomAreaView.this.mDarkMode) {
                drawable = KeyguardBottomAreaView.this.mContext.getDrawable(R.drawable.keyguard_bottom_camera_img_dark);
            } else {
                drawable = KeyguardBottomAreaView.this.mContext.getDrawable(R.drawable.keyguard_bottom_camera_img);
            }
            iconState.drawable = drawable;
            this.mIconState.contentDescription = KeyguardBottomAreaView.this.mContext.getString(R.string.accessibility_camera_button);
            IntentButtonProvider.IntentButton.IconState iconState2 = this.mIconState;
            iconState2.tint = false;
            return iconState2;
        }

        public Intent getIntent() {
            return PackageUtils.getCameraIntent();
        }
    }

    private void updateEmergencyButton() {
        EmergencyButton emergencyButton = this.mEmergencyButton;
        if (emergencyButton != null) {
            emergencyButton.updateEmergencyCallButton();
        }
    }

    public void setDarkMode(boolean z) {
        if (this.mDarkMode != z) {
            this.mDarkMode = z;
            updateLeftAffordanceIcon();
            updateRightAffordanceIcon();
            KeyguardIndicationController keyguardIndicationController = this.mIndicationController;
            if (keyguardIndicationController != null) {
                keyguardIndicationController.setDarkMode(this.mDarkMode);
            }
        }
    }

    public void setViewsAlpha(float f) {
        this.mLeftAffordanceViewLayout.setAlpha(f);
        this.mRightAffordanceViewLayout.setAlpha(f);
        this.mIndicationArea.setAlpha(f);
    }

    public IntentButtonProvider.IntentButton.IconState getIconState(boolean z) {
        return (z ? this.mRightButton : this.mLeftButton).getIcon();
    }
}
