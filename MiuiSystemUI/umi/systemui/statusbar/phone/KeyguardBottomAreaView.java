package com.android.systemui.statusbar.phone;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ActivityOptions;
import android.app.ActivityTaskManager;
import android.app.IApplicationThread;
import android.app.ProfilerInfo;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Property;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.EmergencyButton;
import com.android.keyguard.EmergencyCarrierArea;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.MiuiKeyguardUpdateMonitorCallback;
import com.android.keyguard.analytics.AnalyticsHelper;
import com.android.keyguard.injector.KeyguardBottomAreaInjector;
import com.android.keyguard.injector.KeyguardIndicationInjector;
import com.android.keyguard.injector.KeyguardNegative1PageInjector;
import com.android.keyguard.injector.KeyguardPanelViewInjector;
import com.android.keyguard.magazine.LockScreenMagazineController;
import com.android.keyguard.negative.MiuiKeyguardMoveLeftViewContainer;
import com.android.keyguard.utils.MiuiKeyguardUtils;
import com.android.keyguard.utils.PackageUtils;
import com.android.systemui.ActivityIntentHelper;
import com.android.systemui.C0010R$bool;
import com.android.systemui.C0011R$color;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0021R$string;
import com.android.systemui.Dependency;
import com.android.systemui.Interpolators;
import com.android.systemui.assist.AssistManager;
import com.android.systemui.doze.util.BurnInHelperKt;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.IntentButtonProvider;
import com.android.systemui.statusbar.KeyguardAffordanceView;
import com.android.systemui.statusbar.KeyguardIndicationController;
import com.android.systemui.statusbar.policy.AccessibilityController;
import com.android.systemui.statusbar.policy.ExtensionController;
import com.android.systemui.statusbar.policy.FlashlightController;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.android.systemui.tuner.LockscreenFragment;
import com.android.systemui.tuner.TunerService;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Supplier;
import miui.maml.animation.interpolater.SineEaseInOutInterpolater;
import miui.os.Build;

public class KeyguardBottomAreaView extends FrameLayout implements View.OnClickListener, View.OnLongClickListener, KeyguardStateController.Callback, AccessibilityController.AccessibilityStateChangedCallback {
    public static final Intent INSECURE_CAMERA_INTENT = new Intent("android.media.action.STILL_IMAGE_CAMERA");
    private static final Intent PHONE_INTENT = new Intent("android.intent.action.DIAL");
    private static final Intent SECURE_CAMERA_INTENT = new Intent("android.media.action.STILL_IMAGE_CAMERA_SECURE").addFlags(8388608);
    private AccessibilityController mAccessibilityController;
    private View.AccessibilityDelegate mAccessibilityDelegate;
    private ActivityIntentHelper mActivityIntentHelper;
    private ActivityStarter mActivityStarter;
    private int mBurnInXOffset;
    private int mBurnInYOffset;
    private View mCameraPreview;
    private Configuration mConfiguration;
    private float mDarkAmount;
    private boolean mDarkStyle;
    private int mDensityDpi;
    private final BroadcastReceiver mDevicePolicyReceiver;
    private boolean mDozing;
    private EmergencyButton mEmergencyButton;
    private EmergencyCarrierArea mEmergencyCarrierArea;
    private TextView mEnterpriseDisclosure;
    private float mFontScale;
    private ViewGroup mIndicationArea;
    private TextView mIndicationText;
    private boolean mIsSuperSavePowerMode;
    KeyguardIndicationController mKeyguardIndicationController;
    private KeyguardIndicationInjector mKeyguardIndicationInjector;
    private KeyguardStateController mKeyguardStateController;
    private KeyguardUpdateMonitor mKeyguardUpdateMonitor;
    private String mLanguage;
    private KeyguardAffordanceView mLeftAffordanceView;
    private LinearLayout mLeftAffordanceViewLayout;
    private TextView mLeftAffordanceViewTips;
    private IntentButtonProvider.IntentButton mLeftButton;
    private AnimatorSet mLeftButtonLayoutAnimatorSet;
    private ExtensionController.Extension<IntentButtonProvider.IntentButton> mLeftExtension;
    private boolean mLeftIsVoiceAssist;
    private LockPatternUtils mLockPatternUtils;
    private LockScreenMagazineController mLockScreenMagazineController;
    private LockscreenGestureLogger mLockscreenGestureLogger;
    private int mOrientation;
    private boolean mPrewarmBound;
    private final ServiceConnection mPrewarmConnection;
    private Messenger mPrewarmMessenger;
    private KeyguardAffordanceView mRightAffordanceView;
    private LinearLayout mRightAffordanceViewLayout;
    private TextView mRightAffordanceViewTips;
    private IntentButtonProvider.IntentButton mRightButton;
    private AnimatorSet mRightButtonLayoutAnimatorSet;
    private String mRightButtonStr;
    private ExtensionController.Extension<IntentButtonProvider.IntentButton> mRightExtension;
    private boolean mRightIntentAvailable;
    private boolean mShowCameraAffordance;
    private boolean mShowLeftAffordance;
    private StatusBar mStatusBar;
    private final MiuiKeyguardUpdateMonitorCallback mUpdateMonitorCallback;
    private boolean mUserSetupComplete;

    /* access modifiers changed from: private */
    public static boolean isSuccessfulLaunch(int i) {
        return i == 0 || i == 3 || i == 2;
    }

    public boolean hasOverlappingRendering() {
        return false;
    }

    @Override // com.android.systemui.statusbar.policy.AccessibilityController.AccessibilityStateChangedCallback
    public void onStateChanged(boolean z, boolean z2) {
    }

    public void setAffordanceHelper(KeyguardAffordanceHelper keyguardAffordanceHelper) {
    }

    public KeyguardBottomAreaView(Context context) {
        this(context, null);
    }

    public KeyguardBottomAreaView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public KeyguardBottomAreaView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public KeyguardBottomAreaView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mPrewarmConnection = new ServiceConnection() {
            /* class com.android.systemui.statusbar.phone.KeyguardBottomAreaView.AnonymousClass1 */

            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                KeyguardBottomAreaView.this.mPrewarmMessenger = new Messenger(iBinder);
            }

            public void onServiceDisconnected(ComponentName componentName) {
                KeyguardBottomAreaView.this.mPrewarmMessenger = null;
            }
        };
        this.mRightButton = new MiuiDefaultRightButton();
        this.mLeftButton = new MiuiDefaultLeftButton();
        this.mLockscreenGestureLogger = new LockscreenGestureLogger();
        this.mConfiguration = new Configuration();
        this.mAccessibilityDelegate = new View.AccessibilityDelegate() {
            /* class com.android.systemui.statusbar.phone.KeyguardBottomAreaView.AnonymousClass2 */

            public void onInitializeAccessibilityNodeInfo(View view, AccessibilityNodeInfo accessibilityNodeInfo) {
                String str;
                super.onInitializeAccessibilityNodeInfo(view, accessibilityNodeInfo);
                if (view == KeyguardBottomAreaView.this.mRightAffordanceView) {
                    str = KeyguardBottomAreaView.this.getResources().getString(C0021R$string.camera_label);
                } else if (view != KeyguardBottomAreaView.this.mLeftAffordanceView) {
                    str = null;
                } else if (KeyguardBottomAreaView.this.mLeftIsVoiceAssist) {
                    str = KeyguardBottomAreaView.this.getResources().getString(C0021R$string.voice_assist_label);
                } else {
                    str = ((LockScreenMagazineController) Dependency.get(LockScreenMagazineController.class)).isSupportLockScreenMagazineLeft() ? KeyguardBottomAreaView.this.getResources().getString(C0021R$string.lock_screen_magazine_label) : KeyguardBottomAreaView.this.getResources().getString(C0021R$string.phone_label);
                }
                accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(16, str));
            }

            public boolean performAccessibilityAction(View view, int i, Bundle bundle) {
                if (i == 16) {
                    if (view == KeyguardBottomAreaView.this.mRightAffordanceView) {
                        KeyguardBottomAreaView.this.launchCamera("lockscreen_affordance");
                        return true;
                    } else if (view == KeyguardBottomAreaView.this.mLeftAffordanceView) {
                        if (((LockScreenMagazineController) Dependency.get(LockScreenMagazineController.class)).isSupportLockScreenMagazineLeft()) {
                            KeyguardBottomAreaView.this.launchMagazineLeftActivity();
                        }
                        return true;
                    }
                }
                return super.performAccessibilityAction(view, i, bundle);
            }
        };
        this.mDevicePolicyReceiver = new BroadcastReceiver() {
            /* class com.android.systemui.statusbar.phone.KeyguardBottomAreaView.AnonymousClass9 */

            public void onReceive(Context context, Intent intent) {
                KeyguardBottomAreaView.this.post(new Runnable() {
                    /* class com.android.systemui.statusbar.phone.KeyguardBottomAreaView.AnonymousClass9.AnonymousClass1 */

                    public void run() {
                        KeyguardBottomAreaView.this.updateCameraVisibility();
                    }
                });
            }
        };
        this.mUpdateMonitorCallback = new MiuiKeyguardUpdateMonitorCallback() {
            /* class com.android.systemui.statusbar.phone.KeyguardBottomAreaView.AnonymousClass10 */

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onUserSwitchComplete(int i) {
                KeyguardBottomAreaView.this.updateCameraVisibility();
            }

            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onUserUnlocked() {
                KeyguardBottomAreaView.this.updateCameraVisibility();
                KeyguardBottomAreaView.this.updateLeftAffordance();
                KeyguardBottomAreaView.this.handleIntentAvailable();
            }

            @Override // com.android.keyguard.MiuiKeyguardUpdateMonitorCallback
            public void onSuperSavePowerChanged(boolean z) {
                KeyguardBottomAreaView.this.mIsSuperSavePowerMode = z;
                KeyguardBottomAreaView.this.updateLeftAffordance();
            }
        };
        this.mDarkStyle = false;
        init();
    }

    private void init() {
        this.mShowLeftAffordance = getResources().getBoolean(C0010R$bool.config_keyguardShowLeftAffordance);
        this.mShowCameraAffordance = getResources().getBoolean(C0010R$bool.config_keyguardShowCameraAffordance);
        this.mKeyguardIndicationInjector = (KeyguardIndicationInjector) Dependency.get(KeyguardIndicationInjector.class);
        this.mKeyguardIndicationController = (KeyguardIndicationController) Dependency.get(KeyguardIndicationController.class);
        KeyguardUpdateMonitor keyguardUpdateMonitor = (KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class);
        this.mKeyguardUpdateMonitor = keyguardUpdateMonitor;
        keyguardUpdateMonitor.registerCallback(this.mUpdateMonitorCallback);
    }

    public void initFrom(KeyguardBottomAreaView keyguardBottomAreaView) {
        setStatusBar(keyguardBottomAreaView.mStatusBar);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        new LockPatternUtils(((FrameLayout) this).mContext);
        this.mEmergencyCarrierArea = (EmergencyCarrierArea) findViewById(C0015R$id.keyguard_selector_fade_container);
        this.mRightAffordanceView = (KeyguardAffordanceView) findViewById(C0015R$id.right_button);
        this.mLeftAffordanceView = (KeyguardAffordanceView) findViewById(C0015R$id.left_button);
        this.mIndicationArea = (ViewGroup) findViewById(C0015R$id.keyguard_indication_area);
        this.mEnterpriseDisclosure = (TextView) findViewById(C0015R$id.keyguard_indication_enterprise_disclosure);
        this.mIndicationText = (TextView) findViewById(C0015R$id.keyguard_indication_text);
        this.mBurnInYOffset = getResources().getDimensionPixelSize(C0012R$dimen.default_burn_in_prevention_offset);
        updateCameraVisibility();
        KeyguardStateController keyguardStateController = (KeyguardStateController) Dependency.get(KeyguardStateController.class);
        this.mKeyguardStateController = keyguardStateController;
        keyguardStateController.addCallback(this);
        setClipChildren(false);
        setClipToPadding(false);
        this.mRightAffordanceView.setOnClickListener(this);
        this.mLeftAffordanceView.setOnClickListener(this);
        this.mIndicationArea.setOnClickListener(this);
        this.mLockPatternUtils = new LockPatternUtils(((FrameLayout) this).mContext);
        this.mEmergencyButton = (EmergencyButton) findViewById(C0015R$id.emergency_call_button);
        this.mRightAffordanceViewLayout = (LinearLayout) findViewById(C0015R$id.right_button_layout);
        this.mLeftAffordanceViewLayout = (LinearLayout) findViewById(C0015R$id.left_button_layout);
        this.mRightAffordanceViewTips = (TextView) findViewById(C0015R$id.right_button_tips);
        this.mLeftAffordanceViewTips = (TextView) findViewById(C0015R$id.left_button_tips);
        initTipsView(true);
        initTipsView(false);
        watchForCameraPolicyChanges();
        updateEmergencyButton();
        ((KeyguardBottomAreaInjector) Dependency.get(KeyguardBottomAreaInjector.class)).setView(this);
        ((KeyguardPanelViewInjector) Dependency.get(KeyguardPanelViewInjector.class)).updateBottomView(this);
        LockScreenMagazineController lockScreenMagazineController = (LockScreenMagazineController) Dependency.get(LockScreenMagazineController.class);
        this.mLockScreenMagazineController = lockScreenMagazineController;
        lockScreenMagazineController.setBottomAreaView(this);
        initAccessibility();
        this.mActivityStarter = (ActivityStarter) Dependency.get(ActivityStarter.class);
        FlashlightController flashlightController = (FlashlightController) Dependency.get(FlashlightController.class);
        this.mAccessibilityController = (AccessibilityController) Dependency.get(AccessibilityController.class);
        this.mActivityIntentHelper = new ActivityIntentHelper(getContext());
        updateLeftAffordance();
    }

    public void initTipsView(boolean z) {
        int i;
        String str;
        int i2;
        if (z) {
            boolean isSupportLockScreenMagazineLeft = ((LockScreenMagazineController) Dependency.get(LockScreenMagazineController.class)).isSupportLockScreenMagazineLeft();
            TextView textView = this.mLeftAffordanceViewTips;
            if (isSupportLockScreenMagazineLeft) {
                str = ((FrameLayout) this).mContext.getString(C0021R$string.open_lock_screen_magazine_hint_text);
            } else {
                str = ((FrameLayout) this).mContext.getString(C0021R$string.open_remote_center_hint_text);
            }
            textView.setText(str);
            Context context = ((FrameLayout) this).mContext;
            if (this.mDarkStyle) {
                i2 = C0013R$drawable.keyguard_bottom_guide_right_arrow_dark;
            } else {
                i2 = C0013R$drawable.keyguard_bottom_guide_right_arrow;
            }
            this.mLeftAffordanceViewTips.setCompoundDrawablesWithIntrinsicBounds((Drawable) null, (Drawable) null, context.getDrawable(i2), (Drawable) null);
            return;
        }
        this.mRightAffordanceViewTips.setText(((FrameLayout) this).mContext.getString(C0021R$string.open_camera_hint_text));
        Context context2 = ((FrameLayout) this).mContext;
        if (this.mDarkStyle) {
            i = C0013R$drawable.keyguard_bottom_guide_left_arrow_dark;
        } else {
            i = C0013R$drawable.keyguard_bottom_guide_left_arrow;
        }
        this.mRightAffordanceViewTips.setCompoundDrawablesWithIntrinsicBounds(context2.getDrawable(i), (Drawable) null, (Drawable) null, (Drawable) null);
    }

    private void watchForCameraPolicyChanges() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.app.action.DEVICE_POLICY_MANAGER_STATE_CHANGED");
        getContext().registerReceiverAsUser(this.mDevicePolicyReceiver, UserHandle.ALL, intentFilter, null, null);
    }

    private void updateEmergencyButton() {
        EmergencyButton emergencyButton = this.mEmergencyButton;
        if (emergencyButton != null) {
            emergencyButton.updateEmergencyCallButton();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private Drawable getLockScreenMagazineMainEntryIcon() {
        if (this.mDarkStyle) {
            Drawable preMainEntryResDarkIcon = this.mLockScreenMagazineController.getPreMainEntryResDarkIcon();
            if (preMainEntryResDarkIcon != null) {
                return preMainEntryResDarkIcon;
            }
            return ((FrameLayout) this).mContext.getDrawable(C0013R$drawable.keyguard_bottom_lock_screen_magazine_img_dark);
        }
        Drawable preMainEntryResLightIcon = this.mLockScreenMagazineController.getPreMainEntryResLightIcon();
        return preMainEntryResLightIcon != null ? preMainEntryResLightIcon : ((FrameLayout) this).mContext.getDrawable(C0013R$drawable.keyguard_bottom_lock_screen_magazine_img);
    }

    @Override // com.android.systemui.statusbar.policy.KeyguardStateController.Callback
    public void onKeyguardShowingChanged() {
        handleIntentAvailable();
    }

    public IntentButtonProvider.IntentButton.IconState getIconState(boolean z) {
        return (z ? this.mRightButton : this.mLeftButton).getIcon();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleIntentAvailable() {
        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
            /* class com.android.systemui.statusbar.phone.KeyguardBottomAreaView.AnonymousClass3 */

            public void run() {
                KeyguardBottomAreaView keyguardBottomAreaView = KeyguardBottomAreaView.this;
                keyguardBottomAreaView.mRightIntentAvailable = keyguardBottomAreaView.resolveCameraIntent() != null;
                KeyguardBottomAreaView.this.post(new Runnable() {
                    /* class com.android.systemui.statusbar.phone.KeyguardBottomAreaView.AnonymousClass3.AnonymousClass1 */

                    public void run() {
                        KeyguardBottomAreaView.this.updateLeftAffordance();
                        KeyguardBottomAreaView.this.updateRightAffordanceIcon();
                    }
                });
            }
        });
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mKeyguardStateController.addCallback(this);
        this.mAccessibilityController.addStateChangedCallback(this);
        ExtensionController.ExtensionBuilder newExtension = ((ExtensionController) Dependency.get(ExtensionController.class)).newExtension(IntentButtonProvider.IntentButton.class);
        newExtension.withPlugin(IntentButtonProvider.class, "com.android.systemui.action.PLUGIN_LOCKSCREEN_RIGHT_BUTTON", $$Lambda$KeyguardBottomAreaView$g4KaNPI9kzVsHrOlMYmA_f9J2Y.INSTANCE);
        newExtension.withTunerFactory(new LockscreenFragment.LockButtonFactory(((FrameLayout) this).mContext, "sysui_keyguard_right"));
        newExtension.withDefault(new Supplier() {
            /* class com.android.systemui.statusbar.phone.$$Lambda$KeyguardBottomAreaView$41MKD52m3LHIf9RRtKFf6LfUif0 */

            @Override // java.util.function.Supplier
            public final Object get() {
                return KeyguardBottomAreaView.this.lambda$onAttachedToWindow$1$KeyguardBottomAreaView();
            }
        });
        newExtension.withCallback(new Consumer() {
            /* class com.android.systemui.statusbar.phone.$$Lambda$KeyguardBottomAreaView$Z_R5g5wpXUcfPYLHCfZHekG4xK0 */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                KeyguardBottomAreaView.this.lambda$onAttachedToWindow$2$KeyguardBottomAreaView((IntentButtonProvider.IntentButton) obj);
            }
        });
        this.mRightExtension = newExtension.build();
        ExtensionController.ExtensionBuilder newExtension2 = ((ExtensionController) Dependency.get(ExtensionController.class)).newExtension(IntentButtonProvider.IntentButton.class);
        newExtension2.withPlugin(IntentButtonProvider.class, "com.android.systemui.action.PLUGIN_LOCKSCREEN_LEFT_BUTTON", $$Lambda$KeyguardBottomAreaView$Eh9_ou4HbbT4H4ZFilpDDtanY4k.INSTANCE);
        newExtension2.withTunerFactory(new LockscreenFragment.LockButtonFactory(((FrameLayout) this).mContext, "sysui_keyguard_left"));
        newExtension2.withDefault(new Supplier() {
            /* class com.android.systemui.statusbar.phone.$$Lambda$KeyguardBottomAreaView$WhTEBW5YZVW2MsKtz0LzBCynHY */

            @Override // java.util.function.Supplier
            public final Object get() {
                return KeyguardBottomAreaView.this.lambda$onAttachedToWindow$4$KeyguardBottomAreaView();
            }
        });
        newExtension2.withCallback(new Consumer() {
            /* class com.android.systemui.statusbar.phone.$$Lambda$KeyguardBottomAreaView$owXxFBBnubMOAUdfyf5a48bfZo */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                KeyguardBottomAreaView.this.lambda$onAttachedToWindow$5$KeyguardBottomAreaView((IntentButtonProvider.IntentButton) obj);
            }
        });
        this.mLeftExtension = newExtension2.build();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.app.action.DEVICE_POLICY_MANAGER_STATE_CHANGED");
        getContext().registerReceiverAsUser(this.mDevicePolicyReceiver, UserHandle.ALL, intentFilter, null, null);
        ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).registerCallback(this.mUpdateMonitorCallback);
        ((KeyguardBottomAreaInjector) Dependency.get(KeyguardBottomAreaInjector.class)).onAttachedToWindow();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onAttachedToWindow$1 */
    public /* synthetic */ IntentButtonProvider.IntentButton lambda$onAttachedToWindow$1$KeyguardBottomAreaView() {
        return new MiuiDefaultRightButton();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onAttachedToWindow$4 */
    public /* synthetic */ IntentButtonProvider.IntentButton lambda$onAttachedToWindow$4$KeyguardBottomAreaView() {
        return new MiuiDefaultLeftButton();
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mKeyguardStateController.removeCallback(this);
        this.mAccessibilityController.removeStateChangedCallback(this);
        this.mRightExtension.destroy();
        this.mLeftExtension.destroy();
        getContext().unregisterReceiver(this.mDevicePolicyReceiver);
        ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).removeCallback(this.mUpdateMonitorCallback);
        ((KeyguardBottomAreaInjector) Dependency.get(KeyguardBottomAreaInjector.class)).onDetachedFromWindow();
    }

    private void initAccessibility() {
        this.mLeftAffordanceView.setAccessibilityDelegate(this.mAccessibilityDelegate);
        this.mRightAffordanceView.setAccessibilityDelegate(this.mAccessibilityDelegate);
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        this.mBurnInYOffset = getResources().getDimensionPixelSize(C0012R$dimen.default_burn_in_prevention_offset);
        this.mEnterpriseDisclosure.setTextSize(0, (float) getResources().getDimensionPixelSize(17105518));
        this.mIndicationText.setTextSize(0, (float) getResources().getDimensionPixelSize(17105518));
        updateEmergencyButton();
        if ((this.mConfiguration.updateFrom(configuration) & 2048) != 0) {
            updateViewsLayoutParams();
        }
        int i = configuration.densityDpi;
        float f = configuration.fontScale;
        if (!(this.mFontScale == f && this.mDensityDpi == i)) {
            if (this.mFontScale != f) {
                this.mFontScale = f;
            }
            updateViewsTextSize();
        }
        if (i != this.mDensityDpi) {
            updateViewsLayoutParams();
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
        float dimensionPixelSize = (float) getResources().getDimensionPixelSize(C0012R$dimen.keyguard_bottom_button_tips_text_size);
        this.mLeftAffordanceViewTips.setTextSize(0, dimensionPixelSize);
        this.mRightAffordanceViewTips.setTextSize(0, dimensionPixelSize);
    }

    private void updateDrawableResource() {
        initTipsView(true);
        initTipsView(false);
    }

    private void updateViewsLayoutParams() {
        int dimensionPixelSize = getResources().getDimensionPixelSize(C0012R$dimen.keyguard_affordance_width);
        int dimensionPixelSize2 = getResources().getDimensionPixelSize(C0012R$dimen.keyguard_affordance_height);
        this.mLockScreenMagazineController.initPreMainEntryIcon();
        this.mLeftAffordanceViewLayout.setPaddingRelative(0, 0, dimensionPixelSize, 0);
        this.mRightAffordanceViewLayout.setPaddingRelative(dimensionPixelSize, 0, 0, 0);
        ViewGroup.LayoutParams layoutParams = this.mLeftAffordanceView.getLayoutParams();
        layoutParams.height = dimensionPixelSize2;
        layoutParams.width = dimensionPixelSize;
        this.mLeftAffordanceView.setLayoutParams(layoutParams);
        updateLeftAffordanceIcon();
        LinearLayout.LayoutParams layoutParams2 = (LinearLayout.LayoutParams) this.mLeftAffordanceViewTips.getLayoutParams();
        layoutParams2.setMarginStart(getResources().getDimensionPixelSize(C0012R$dimen.keyguard_bottom_left_button_tips_margin_start));
        this.mLeftAffordanceViewTips.setLayoutParams(layoutParams2);
        ViewGroup.LayoutParams layoutParams3 = this.mRightAffordanceView.getLayoutParams();
        layoutParams3.height = dimensionPixelSize2;
        layoutParams3.width = dimensionPixelSize;
        this.mRightAffordanceView.setLayoutParams(layoutParams3);
        updateRightAffordanceIcon();
        LinearLayout.LayoutParams layoutParams4 = (LinearLayout.LayoutParams) this.mRightAffordanceViewTips.getLayoutParams();
        layoutParams4.setMarginEnd(getResources().getDimensionPixelSize(C0012R$dimen.keyguard_bottom_right_button_tips_margin_end));
        this.mRightAffordanceViewTips.setLayoutParams(layoutParams4);
        FrameLayout.LayoutParams layoutParams5 = (FrameLayout.LayoutParams) this.mIndicationArea.getLayoutParams();
        layoutParams5.height = dimensionPixelSize2;
        layoutParams5.setMarginsRelative(dimensionPixelSize, 0, dimensionPixelSize, 0);
        this.mIndicationArea.setLayoutParams(layoutParams5);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateRightAffordanceIcon() {
        IntentButtonProvider.IntentButton.IconState icon = this.mRightButton.getIcon();
        this.mRightAffordanceView.setVisibility((this.mDozing || !icon.isVisible) ? 8 : 0);
        if (!(icon.drawable == this.mRightAffordanceView.getDrawable() && icon.tint == this.mRightAffordanceView.shouldTint())) {
            this.mRightAffordanceView.setImageDrawable(icon.drawable, icon.tint);
        }
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

    public ResolveInfo resolveCameraIntent() {
        return ((FrameLayout) this).mContext.getPackageManager().resolveActivityAsUser(getCameraIntent(), 65536, KeyguardUpdateMonitor.getCurrentUser());
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateCameraVisibility() {
        KeyguardAffordanceView keyguardAffordanceView = this.mRightAffordanceView;
        if (keyguardAffordanceView != null) {
            keyguardAffordanceView.setVisibility((this.mDozing || !this.mShowCameraAffordance || !this.mRightButton.getIcon().isVisible) ? 8 : 0);
        }
    }

    public void updateLeftAffordanceIcon() {
        int i = 8;
        if (!this.mShowLeftAffordance || this.mDozing) {
            this.mLeftAffordanceView.setVisibility(8);
            return;
        }
        IntentButtonProvider.IntentButton.IconState icon = this.mLeftButton.getIcon();
        KeyguardAffordanceView keyguardAffordanceView = this.mLeftAffordanceView;
        if (icon.isVisible) {
            i = 0;
        }
        keyguardAffordanceView.setVisibility(i);
        if (!(icon.drawable == this.mLeftAffordanceView.getDrawable() && icon.tint == this.mLeftAffordanceView.shouldTint())) {
            this.mLeftAffordanceView.setImageDrawable(icon.drawable, icon.tint);
        }
        this.mLeftAffordanceView.setContentDescription(icon.contentDescription);
        initTipsView(true);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isPhoneVisible() {
        PackageManager packageManager = ((FrameLayout) this).mContext.getPackageManager();
        if (!packageManager.hasSystemFeature("android.hardware.telephony") || packageManager.resolveActivity(PHONE_INTENT, 0) == null) {
            return false;
        }
        return true;
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
            if (z) {
                if (this.mLeftButton.getIcon().isVisible) {
                    this.mLeftButtonLayoutAnimatorSet.start();
                }
            } else if (this.mRightButton.getIcon().isVisible) {
                this.mRightButtonLayoutAnimatorSet.start();
            }
        }
    }

    private AnimatorSet getButtonLayoutAnimate(View view, final TextView textView, boolean z) {
        Property property = FrameLayout.TRANSLATION_X;
        AnimatorSet animatorSet = new AnimatorSet();
        float f = z ? -1.0f : 1.0f;
        float f2 = -50.0f * f;
        ObjectAnimator ofFloat = ObjectAnimator.ofFloat(view, property, 0.0f, f2);
        ofFloat.setDuration(150L);
        float f3 = 10.0f * f;
        ObjectAnimator ofFloat2 = ObjectAnimator.ofFloat(view, property, f2, f3);
        ofFloat2.setDuration(150L);
        float f4 = -8.0f * f;
        ObjectAnimator ofFloat3 = ObjectAnimator.ofFloat(view, property, f3, f4);
        ofFloat3.setDuration(100L);
        float f5 = f * 5.0f;
        ObjectAnimator ofFloat4 = ObjectAnimator.ofFloat(view, property, f4, f5);
        ofFloat4.setDuration(100L);
        ObjectAnimator ofFloat5 = ObjectAnimator.ofFloat(view, property, f5, 0.0f);
        ofFloat5.setDuration(100L);
        ObjectAnimator ofFloat6 = ObjectAnimator.ofFloat(textView, FrameLayout.ALPHA, 1.0f, 0.0f);
        ofFloat6.setDuration(500L);
        ofFloat6.setStartDelay(1000);
        animatorSet.play(ofFloat).before(ofFloat2);
        animatorSet.play(ofFloat2).before(ofFloat3);
        animatorSet.play(ofFloat3).before(ofFloat4);
        animatorSet.play(ofFloat4).before(ofFloat5);
        animatorSet.play(ofFloat5).before(ofFloat6);
        animatorSet.setInterpolator(new SineEaseInOutInterpolater());
        animatorSet.addListener(new AnimatorListenerAdapter() {
            /* class com.android.systemui.statusbar.phone.KeyguardBottomAreaView.AnonymousClass4 */

            public void onAnimationStart(Animator animator) {
                textView.setVisibility(0);
                textView.setTextColor(KeyguardBottomAreaView.this.getTextColor());
                textView.setAlpha(1.0f);
                KeyguardBottomAreaView.this.handleBottomButtonClickedAnimation(true);
            }

            public void onAnimationCancel(Animator animator) {
                KeyguardBottomAreaView.this.handleBottomButtonClickedAnimation(false);
            }

            public void onAnimationEnd(Animator animator) {
                textView.setVisibility(8);
                KeyguardBottomAreaView.this.handleBottomButtonClickedAnimation(false);
            }
        });
        return animatorSet;
    }

    public boolean onLongClick(View view) {
        handleTrustCircleClick();
        return true;
    }

    private void handleTrustCircleClick() {
        this.mLockscreenGestureLogger.write(191, 0, 0);
        KeyguardIndicationController keyguardIndicationController = this.mKeyguardIndicationController;
        if (keyguardIndicationController != null) {
            keyguardIndicationController.setDarkStyle(this.mDarkStyle);
            this.mKeyguardIndicationController.showTransientIndication(C0021R$string.keyguard_indication_trust_disabled);
        } else {
            Log.e("StatusBar/KeyguardBottomAreaView", "IndicationController == null");
        }
        this.mLockPatternUtils.requireCredentialEntry(KeyguardUpdateMonitor.getCurrentUser());
    }

    public void launchMagazineLeftActivity() {
        Intent intent = this.mLeftButton.getIntent();
        if (intent != null) {
            AnalyticsHelper.getInstance(getContext()).trackPageStart("action_enter_left_view");
            getContext().startActivityAsUser(intent, UserHandle.CURRENT);
        }
    }

    public void handleBottomButtonClickedAnimation(boolean z) {
        this.mKeyguardIndicationInjector.doIndicatorAnimation(z, this.mIndicationText);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getTextColor() {
        int i;
        Resources resources = ((FrameLayout) this).mContext.getResources();
        if (this.mDarkStyle) {
            i = C0011R$color.miui_common_unlock_screen_common_dark_text_color;
        } else {
            i = C0011R$color.miui_default_lock_screen_unlock_bottom_tips_text_color;
        }
        return resources.getColor(i);
    }

    public void unbindCameraPrewarmService(boolean z) {
        if (this.mPrewarmBound) {
            Messenger messenger = this.mPrewarmMessenger;
            if (messenger != null && z) {
                try {
                    messenger.send(Message.obtain((Handler) null, 1));
                } catch (RemoteException e) {
                    Log.w("StatusBar/KeyguardBottomAreaView", "Error sending camera fired message", e);
                }
            }
            ((FrameLayout) this).mContext.unbindService(this.mPrewarmConnection);
            this.mPrewarmBound = false;
        }
    }

    public void launchCamera(String str) {
        final Intent cameraIntent = getCameraIntent();
        cameraIntent.putExtra("com.android.systemui.camera_launch_source", str);
        boolean wouldLaunchResolverActivity = this.mActivityIntentHelper.wouldLaunchResolverActivity(cameraIntent, KeyguardUpdateMonitor.getCurrentUser());
        if (cameraIntent != SECURE_CAMERA_INTENT || wouldLaunchResolverActivity) {
            this.mActivityStarter.startActivity(cameraIntent, false, (ActivityStarter.Callback) new ActivityStarter.Callback() {
                /* class com.android.systemui.statusbar.phone.KeyguardBottomAreaView.AnonymousClass6 */

                @Override // com.android.systemui.plugins.ActivityStarter.Callback
                public void onActivityStarted(int i) {
                    KeyguardBottomAreaView.this.unbindCameraPrewarmService(KeyguardBottomAreaView.isSuccessfulLaunch(i));
                }
            });
        } else {
            AsyncTask.execute(new Runnable() {
                /* class com.android.systemui.statusbar.phone.KeyguardBottomAreaView.AnonymousClass5 */

                public void run() {
                    int i;
                    ActivityOptions makeBasic = ActivityOptions.makeBasic();
                    makeBasic.setDisallowEnterPictureInPictureWhileLaunching(true);
                    makeBasic.setRotationAnimationHint(3);
                    try {
                        i = ActivityTaskManager.getService().startActivityAsUser((IApplicationThread) null, KeyguardBottomAreaView.this.getContext().getBasePackageName(), KeyguardBottomAreaView.this.getContext().getAttributionTag(), cameraIntent, cameraIntent.resolveTypeIfNeeded(KeyguardBottomAreaView.this.getContext().getContentResolver()), (IBinder) null, (String) null, 0, 268435456, (ProfilerInfo) null, makeBasic.toBundle(), UserHandle.CURRENT.getIdentifier());
                    } catch (RemoteException e) {
                        Log.w("StatusBar/KeyguardBottomAreaView", "Unable to start camera activity", e);
                        i = -96;
                    }
                    final boolean isSuccessfulLaunch = KeyguardBottomAreaView.isSuccessfulLaunch(i);
                    KeyguardBottomAreaView.this.post(new Runnable() {
                        /* class com.android.systemui.statusbar.phone.KeyguardBottomAreaView.AnonymousClass5.AnonymousClass1 */

                        public void run() {
                            KeyguardBottomAreaView.this.unbindCameraPrewarmService(isSuccessfulLaunch);
                        }
                    });
                }
            });
        }
        AnalyticsHelper.getInstance(((FrameLayout) this).mContext).recordKeyguardAction("action_enter_camera_view");
        AnalyticsHelper.getInstance(((FrameLayout) this).mContext).trackPageStart("action_enter_camera_view");
    }

    public void setDarkAmount(float f) {
        if (f != this.mDarkAmount) {
            this.mDarkAmount = f;
            dozeTimeTick();
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void launchVoiceAssist() {
        AnonymousClass7 r1 = new Runnable(this) {
            /* class com.android.systemui.statusbar.phone.KeyguardBottomAreaView.AnonymousClass7 */

            public void run() {
                ((AssistManager) Dependency.get(AssistManager.class)).launchVoiceAssistFromKeyguard();
            }
        };
        if (!this.mKeyguardStateController.canDismissLockScreen()) {
            ((Executor) Dependency.get(Executor.class)).execute(r1);
        } else {
            this.mStatusBar.executeRunnableDismissingKeyguard(r1, null, !TextUtils.isEmpty(this.mRightButtonStr) && ((TunerService) Dependency.get(TunerService.class)).getValue("sysui_keyguard_right_unlock", 1) != 0, false, true);
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

    public View getRightPreview() {
        return this.mCameraPreview;
    }

    @Override // com.android.systemui.statusbar.policy.KeyguardStateController.Callback
    public void onUnlockedChanged() {
        updateCameraVisibility();
    }

    public void startFinishDozeAnimation() {
        long j = 0;
        if (this.mLeftAffordanceView.getVisibility() == 0) {
            startFinishDozeAnimationElement(this.mLeftAffordanceView, 0);
            j = 48;
        }
        if (this.mRightAffordanceView.getVisibility() == 0) {
            startFinishDozeAnimationElement(this.mRightAffordanceView, j + 48);
        }
        this.mIndicationArea.setAlpha(0.0f);
        this.mIndicationArea.animate().alpha(1.0f).setInterpolator(Interpolators.LINEAR_OUT_SLOW_IN).setDuration(700);
    }

    private void startFinishDozeAnimationElement(View view, long j) {
        view.setAlpha(0.0f);
        view.setTranslationY((float) (view.getHeight() / 2));
        view.animate().alpha(1.0f).translationY(0.0f).setInterpolator(Interpolators.LINEAR_OUT_SLOW_IN).setStartDelay(j).setDuration(250);
    }

    public void updateLeftAffordance() {
        updateLeftAffordanceIcon();
    }

    /* access modifiers changed from: private */
    /* renamed from: setRightButton */
    public void lambda$onAttachedToWindow$2(IntentButtonProvider.IntentButton intentButton) {
        this.mRightButton = intentButton;
        updateRightAffordanceIcon();
        updateCameraVisibility();
    }

    /* access modifiers changed from: private */
    /* renamed from: setLeftButton */
    public void lambda$onAttachedToWindow$5(IntentButtonProvider.IntentButton intentButton) {
        this.mLeftButton = intentButton;
        if (!(intentButton instanceof MiuiDefaultLeftButton)) {
            this.mLeftIsVoiceAssist = false;
        }
        updateLeftAffordance();
    }

    public void setDozing(boolean z, boolean z2) {
        if (this.mDozing != z) {
            this.mDozing = z;
            updateCameraVisibility();
            updateLeftAffordanceIcon();
            if (z) {
                this.mEmergencyCarrierArea.setVisibility(4);
            } else if (z2) {
                startFinishDozeAnimation();
            }
        }
    }

    public void dozeTimeTick() {
        this.mIndicationArea.setTranslationY(((float) (BurnInHelperKt.getBurnInOffset(this.mBurnInYOffset * 2, false) - this.mBurnInYOffset)) * this.mDarkAmount);
    }

    public void setAntiBurnInOffsetX(int i) {
        if (this.mBurnInXOffset != i) {
            this.mBurnInXOffset = i;
            this.mIndicationArea.setTranslationX((float) i);
        }
    }

    public void cancelAnimations() {
        AnimatorSet animatorSet = this.mLeftButtonLayoutAnimatorSet;
        if (animatorSet != null && animatorSet.isRunning()) {
            this.mLeftButtonLayoutAnimatorSet.cancel();
        }
        AnimatorSet animatorSet2 = this.mRightButtonLayoutAnimatorSet;
        if (animatorSet2 != null && animatorSet2.isRunning()) {
            this.mRightButtonLayoutAnimatorSet.cancel();
        }
    }

    /* access modifiers changed from: private */
    public class MiuiDefaultLeftButton implements IntentButtonProvider.IntentButton {
        private IntentButtonProvider.IntentButton.IconState mIconState;

        private MiuiDefaultLeftButton() {
            this.mIconState = new IntentButtonProvider.IntentButton.IconState();
        }

        @Override // com.android.systemui.plugins.IntentButtonProvider.IntentButton
        public IntentButtonProvider.IntentButton.IconState getIcon() {
            Drawable drawable;
            boolean isSupportLockScreenMagazineLeft = ((LockScreenMagazineController) Dependency.get(LockScreenMagazineController.class)).isSupportLockScreenMagazineLeft();
            MiuiKeyguardMoveLeftViewContainer leftView = ((KeyguardNegative1PageInjector) Dependency.get(KeyguardNegative1PageInjector.class)).getLeftView();
            boolean z = true;
            if (isSupportLockScreenMagazineLeft) {
                this.mIconState.drawable = KeyguardBottomAreaView.this.getLockScreenMagazineMainEntryIcon();
                this.mIconState.contentDescription = ((FrameLayout) KeyguardBottomAreaView.this).mContext.getString(C0021R$string.accessibility_left_lock_screen_magazine_button);
                IntentButtonProvider.IntentButton.IconState iconState = this.mIconState;
                if (!KeyguardBottomAreaView.this.mUserSetupComplete || !KeyguardBottomAreaView.this.mKeyguardUpdateMonitor.isUserUnlocked(KeyguardUpdateMonitor.getCurrentUser()) || MiuiKeyguardUtils.isPad() || leftView == null || !leftView.isSupportRightMove() || !KeyguardBottomAreaView.this.mShowLeftAffordance || !KeyguardBottomAreaView.this.isPhoneVisible() || KeyguardBottomAreaView.this.mIsSuperSavePowerMode) {
                    z = false;
                }
                iconState.isVisible = z;
            } else {
                IntentButtonProvider.IntentButton.IconState iconState2 = this.mIconState;
                if (KeyguardBottomAreaView.this.mDarkStyle) {
                    drawable = ((FrameLayout) KeyguardBottomAreaView.this).mContext.getDrawable(C0013R$drawable.keyguard_bottom_remote_center_img_dark);
                } else {
                    drawable = ((FrameLayout) KeyguardBottomAreaView.this).mContext.getDrawable(C0013R$drawable.keyguard_bottom_remote_center_img);
                }
                iconState2.drawable = drawable;
                this.mIconState.contentDescription = ((FrameLayout) KeyguardBottomAreaView.this).mContext.getString(C0021R$string.accessibility_left_control_center_button);
                IntentButtonProvider.IntentButton.IconState iconState3 = this.mIconState;
                if (!KeyguardBottomAreaView.this.mUserSetupComplete || !KeyguardBottomAreaView.this.mKeyguardUpdateMonitor.isUserUnlocked(KeyguardUpdateMonitor.getCurrentUser()) || MiuiKeyguardUtils.isPad() || leftView == null || !leftView.isSupportRightMove() || !KeyguardBottomAreaView.this.mShowLeftAffordance || KeyguardBottomAreaView.this.mIsSuperSavePowerMode) {
                    z = false;
                }
                iconState3.isVisible = z;
            }
            IntentButtonProvider.IntentButton.IconState iconState4 = this.mIconState;
            iconState4.tint = false;
            return iconState4;
        }

        @Override // com.android.systemui.plugins.IntentButtonProvider.IntentButton
        public Intent getIntent() {
            if (KeyguardBottomAreaView.this.mLockScreenMagazineController == null) {
                return null;
            }
            Intent preLeftScreenIntent = KeyguardBottomAreaView.this.mLockScreenMagazineController.getPreLeftScreenIntent();
            if (Build.IS_INTERNATIONAL_BUILD && preLeftScreenIntent != null) {
                preLeftScreenIntent.putExtra("entry_source", "swipe");
            }
            return preLeftScreenIntent;
        }
    }

    /* access modifiers changed from: private */
    public class MiuiDefaultRightButton implements IntentButtonProvider.IntentButton {
        private IntentButtonProvider.IntentButton.IconState mIconState;

        private MiuiDefaultRightButton() {
            this.mIconState = new IntentButtonProvider.IntentButton.IconState();
        }

        @Override // com.android.systemui.plugins.IntentButtonProvider.IntentButton
        public IntentButtonProvider.IntentButton.IconState getIcon() {
            Drawable drawable;
            this.mIconState.isVisible = !KeyguardBottomAreaView.this.isCameraDisabledByDpm() && KeyguardBottomAreaView.this.mShowCameraAffordance && KeyguardBottomAreaView.this.mUserSetupComplete && KeyguardBottomAreaView.this.resolveCameraIntent() != null && KeyguardBottomAreaView.this.mRightIntentAvailable && KeyguardBottomAreaView.this.mKeyguardUpdateMonitor.isUserUnlocked(KeyguardUpdateMonitor.getCurrentUser()) && KeyguardBottomAreaView.this.getResources().getConfiguration().orientation != 2;
            IntentButtonProvider.IntentButton.IconState iconState = this.mIconState;
            if (KeyguardBottomAreaView.this.mDarkStyle) {
                drawable = ((FrameLayout) KeyguardBottomAreaView.this).mContext.getDrawable(C0013R$drawable.keyguard_bottom_camera_img_dark);
            } else {
                drawable = ((FrameLayout) KeyguardBottomAreaView.this).mContext.getDrawable(C0013R$drawable.keyguard_bottom_camera_img);
            }
            iconState.drawable = drawable;
            IntentButtonProvider.IntentButton.IconState iconState2 = this.mIconState;
            iconState2.tint = false;
            iconState2.contentDescription = ((FrameLayout) KeyguardBottomAreaView.this).mContext.getString(C0021R$string.accessibility_camera_button);
            return this.mIconState;
        }

        @Override // com.android.systemui.plugins.IntentButtonProvider.IntentButton
        public Intent getIntent() {
            return (!KeyguardBottomAreaView.this.mKeyguardStateController.isMethodSecure() || KeyguardBottomAreaView.this.mKeyguardStateController.canDismissLockScreen()) ? PackageUtils.getCameraIntent() : KeyguardBottomAreaView.SECURE_CAMERA_INTENT;
        }
    }

    public WindowInsets onApplyWindowInsets(WindowInsets windowInsets) {
        int safeInsetBottom = windowInsets.getDisplayCutout() != null ? windowInsets.getDisplayCutout().getSafeInsetBottom() : 0;
        if (isPaddingRelative()) {
            setPaddingRelative(getPaddingStart(), getPaddingTop(), getPaddingEnd(), safeInsetBottom);
        } else {
            setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), safeInsetBottom);
        }
        return windowInsets;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isCameraDisabledByDpm() {
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) getContext().getSystemService("device_policy");
        if (devicePolicyManager == null || this.mStatusBar == null) {
            return false;
        }
        boolean z = (devicePolicyManager.getKeyguardDisabledFeatures(null, KeyguardUpdateMonitor.getCurrentUser()) & 2) != 0 && this.mStatusBar.isKeyguardSecure();
        if (devicePolicyManager.getCameraDisabled(null) || z) {
            return true;
        }
        return false;
    }

    public ViewGroup getRightViewLayout() {
        return this.mRightAffordanceViewLayout;
    }

    public void setDarkStyle(boolean z) {
        if (this.mDarkStyle != z) {
            this.mDarkStyle = z;
            updateLeftAffordanceIcon();
            updateRightAffordanceIcon();
            KeyguardIndicationController keyguardIndicationController = this.mKeyguardIndicationController;
            if (keyguardIndicationController != null) {
                keyguardIndicationController.setDarkStyle(z);
            } else {
                Log.e("StatusBar/KeyguardBottomAreaView", "IndicationController == null");
            }
        }
    }
}
