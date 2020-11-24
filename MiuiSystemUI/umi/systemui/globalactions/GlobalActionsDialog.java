package com.android.systemui.globalactions;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.ActivityManager;
import android.app.Dialog;
import android.app.IActivityManager;
import android.app.IStopUserCallback;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.app.trust.TrustManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.UserInfo;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.Vibrator;
import android.provider.Settings;
import android.service.dreams.IDreamManager;
import android.sysprop.TelephonyProperties;
import android.telecom.TelecomManager;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.ArraySet;
import android.util.FeatureFlagUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.IWindowManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListPopupWindow;
import android.widget.TextView;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;
import androidx.lifecycle.Observer;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.colorextraction.ColorExtractor;
import com.android.internal.colorextraction.drawable.ScrimDrawable;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.UiEventLogger;
import com.android.internal.statusbar.IStatusBarService;
import com.android.internal.util.EmergencyAffordanceManager;
import com.android.internal.util.ScreenRecordHelper;
import com.android.internal.util.ScreenshotHelper;
import com.android.internal.view.RotationPolicy;
import com.android.internal.widget.LockPatternUtils;
import com.android.systemui.C0005R$array;
import com.android.systemui.C0008R$color;
import com.android.systemui.C0009R$dimen;
import com.android.systemui.C0010R$drawable;
import com.android.systemui.C0012R$id;
import com.android.systemui.C0013R$integer;
import com.android.systemui.C0014R$layout;
import com.android.systemui.C0019R$style;
import com.android.systemui.Interpolators;
import com.android.systemui.MultiListLayout;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.colorextraction.SysuiColorExtractor;
import com.android.systemui.controls.ControlsServiceInfo;
import com.android.systemui.controls.controller.ControlsController;
import com.android.systemui.controls.controller.SeedResponse;
import com.android.systemui.controls.dagger.ControlsComponent;
import com.android.systemui.controls.management.ControlsAnimations;
import com.android.systemui.controls.management.ControlsListingController;
import com.android.systemui.controls.ui.ControlsUiController;
import com.android.systemui.globalactions.GlobalActionsDialog;
import com.android.systemui.model.SysUiState;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.GlobalActions;
import com.android.systemui.plugins.GlobalActionsPanelPlugin;
import com.android.systemui.settings.CurrentUserContextTracker;
import com.android.systemui.statusbar.NotificationShadeDepthController;
import com.android.systemui.statusbar.phone.NotificationShadeWindowController;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.android.systemui.util.RingerModeTracker;
import com.android.systemui.util.leak.RotationUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

public class GlobalActionsDialog implements DialogInterface.OnDismissListener, DialogInterface.OnShowListener, ConfigurationController.ConfigurationListener, GlobalActionsPanelPlugin.Callbacks, LifecycleOwner {
    @VisibleForTesting
    static final String GLOBAL_ACTION_KEY_POWER = "power";
    private final ActivityStarter mActivityStarter;
    /* access modifiers changed from: private */
    public MyAdapter mAdapter;
    private ContentObserver mAirplaneModeObserver = new ContentObserver(this.mMainHandler) {
        public void onChange(boolean z) {
            GlobalActionsDialog.this.onAirplaneModeChanged();
        }
    };
    /* access modifiers changed from: private */
    public ToggleAction mAirplaneModeOn;
    /* access modifiers changed from: private */
    public ToggleState mAirplaneState = ToggleState.Off;
    /* access modifiers changed from: private */
    public final AudioManager mAudioManager;
    /* access modifiers changed from: private */
    public final Executor mBackgroundExecutor;
    private final BroadcastDispatcher mBroadcastDispatcher;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.intent.action.CLOSE_SYSTEM_DIALOGS".equals(action) || "android.intent.action.SCREEN_OFF".equals(action)) {
                String stringExtra = intent.getStringExtra("reason");
                if (!"globalactions".equals(stringExtra)) {
                    GlobalActionsDialog.this.mHandler.sendMessage(GlobalActionsDialog.this.mHandler.obtainMessage(0, stringExtra));
                }
            } else if ("android.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED".equals(action) && !intent.getBooleanExtra("android.telephony.extra.PHONE_IN_ECM_STATE", false) && GlobalActionsDialog.this.mIsWaitingForEcmExit) {
                boolean unused = GlobalActionsDialog.this.mIsWaitingForEcmExit = false;
                GlobalActionsDialog.this.changeAirplaneModeSystemSetting(true);
            }
        }
    };
    private final ConfigurationController mConfigurationController;
    private final ContentResolver mContentResolver;
    /* access modifiers changed from: private */
    public final Context mContext;
    private Optional<ControlsController> mControlsControllerOptional;
    private List<ControlsServiceInfo> mControlsServiceInfos = new ArrayList();
    /* access modifiers changed from: private */
    public Optional<ControlsUiController> mControlsUiControllerOptional;
    private CurrentUserContextTracker mCurrentUserContextTracker;
    private final NotificationShadeDepthController mDepthController;
    private final DevicePolicyManager mDevicePolicyManager;
    /* access modifiers changed from: private */
    public boolean mDeviceProvisioned = false;
    @VisibleForTesting
    protected ActionsDialog mDialog;
    /* access modifiers changed from: private */
    public int mDialogPressDelay = 850;
    private final IDreamManager mDreamManager;
    /* access modifiers changed from: private */
    public final EmergencyAffordanceManager mEmergencyAffordanceManager;
    /* access modifiers changed from: private */
    public Handler mHandler = new Handler() {
        public void handleMessage(Message message) {
            int i = message.what;
            if (i != 0) {
                if (i == 1) {
                    GlobalActionsDialog.this.refreshSilentMode();
                    GlobalActionsDialog.this.mAdapter.notifyDataSetChanged();
                }
            } else if (GlobalActionsDialog.this.mDialog != null) {
                if ("dream".equals(message.obj)) {
                    GlobalActionsDialog.this.mDialog.completeDismiss();
                } else {
                    GlobalActionsDialog.this.mDialog.dismiss();
                }
                GlobalActionsDialog.this.mDialog = null;
            }
        }
    };
    /* access modifiers changed from: private */
    public boolean mHasTelephony;
    private boolean mHasVibrator;
    /* access modifiers changed from: private */
    public final IActivityManager mIActivityManager;
    /* access modifiers changed from: private */
    public final IWindowManager mIWindowManager;
    /* access modifiers changed from: private */
    public boolean mIsWaitingForEcmExit = false;
    @VisibleForTesting
    protected final ArrayList<Action> mItems = new ArrayList<>();
    /* access modifiers changed from: private */
    public boolean mKeyguardShowing = false;
    /* access modifiers changed from: private */
    public final KeyguardStateController mKeyguardStateController;
    private final LifecycleRegistry mLifecycle = new LifecycleRegistry(this);
    /* access modifiers changed from: private */
    public final LockPatternUtils mLockPatternUtils;
    private Handler mMainHandler;
    /* access modifiers changed from: private */
    public final MetricsLogger mMetricsLogger;
    private final NotificationShadeWindowController mNotificationShadeWindowController;
    /* access modifiers changed from: private */
    public MyOverflowAdapter mOverflowAdapter;
    @VisibleForTesting
    protected final ArrayList<Action> mOverflowItems = new ArrayList<>();
    PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        public void onServiceStateChanged(ServiceState serviceState) {
            if (GlobalActionsDialog.this.mHasTelephony) {
                ToggleState unused = GlobalActionsDialog.this.mAirplaneState = serviceState.getState() == 3 ? ToggleState.On : ToggleState.Off;
                GlobalActionsDialog.this.mAirplaneModeOn.updateState(GlobalActionsDialog.this.mAirplaneState);
                GlobalActionsDialog.this.mAdapter.notifyDataSetChanged();
                GlobalActionsDialog.this.mOverflowAdapter.notifyDataSetChanged();
                GlobalActionsDialog.this.mPowerAdapter.notifyDataSetChanged();
            }
        }
    };
    /* access modifiers changed from: private */
    public MyPowerOptionsAdapter mPowerAdapter;
    @VisibleForTesting
    protected final ArrayList<Action> mPowerItems = new ArrayList<>();
    private final Resources mResources;
    private final RingerModeTracker mRingerModeTracker;
    /* access modifiers changed from: private */
    public final ScreenRecordHelper mScreenRecordHelper;
    /* access modifiers changed from: private */
    public final ScreenshotHelper mScreenshotHelper;
    @VisibleForTesting
    boolean mShowLockScreenCardsAndControls = false;
    private final boolean mShowSilentToggle;
    private Action mSilentModeAction;
    private final IStatusBarService mStatusBarService;
    private final SysUiState mSysUiState;
    private final SysuiColorExtractor mSysuiColorExtractor;
    /* access modifiers changed from: private */
    public final TelecomManager mTelecomManager;
    private final TrustManager mTrustManager;
    /* access modifiers changed from: private */
    public final UiEventLogger mUiEventLogger;
    /* access modifiers changed from: private */
    public final UserManager mUserManager;
    private GlobalActionsPanelPlugin mWalletPlugin;
    /* access modifiers changed from: private */
    public final GlobalActions.GlobalActionsManager mWindowManagerFuncs;

    public interface Action {
        View create(Context context, View view, ViewGroup viewGroup, LayoutInflater layoutInflater);

        Drawable getIcon(Context context);

        CharSequence getMessage();

        int getMessageResId();

        boolean isEnabled();

        void onPress();

        boolean shouldBeSeparated() {
            return false;
        }

        boolean showBeforeProvisioning();

        boolean showDuringKeyguard();
    }

    private interface LongPressAction extends Action {
        boolean onLongPress();
    }

    @VisibleForTesting
    public enum GlobalActionsEvent implements UiEventLogger.UiEventEnum {
        GA_POWER_MENU_OPEN(337),
        GA_POWER_MENU_CLOSE(471),
        GA_BUGREPORT_PRESS(344),
        GA_BUGREPORT_LONG_PRESS(345),
        GA_EMERGENCY_DIALER_PRESS(346),
        GA_SCREENSHOT_PRESS(347),
        GA_SCREENSHOT_LONG_PRESS(348);
        
        private final int mId;

        private GlobalActionsEvent(int i) {
            this.mId = i;
        }

        public int getId() {
            return this.mId;
        }
    }

    public GlobalActionsDialog(Context context, GlobalActions.GlobalActionsManager globalActionsManager, AudioManager audioManager, IDreamManager iDreamManager, DevicePolicyManager devicePolicyManager, LockPatternUtils lockPatternUtils, BroadcastDispatcher broadcastDispatcher, ConnectivityManager connectivityManager, TelephonyManager telephonyManager, ContentResolver contentResolver, Vibrator vibrator, Resources resources, ConfigurationController configurationController, ActivityStarter activityStarter, KeyguardStateController keyguardStateController, UserManager userManager, TrustManager trustManager, IActivityManager iActivityManager, TelecomManager telecomManager, MetricsLogger metricsLogger, NotificationShadeDepthController notificationShadeDepthController, SysuiColorExtractor sysuiColorExtractor, IStatusBarService iStatusBarService, NotificationShadeWindowController notificationShadeWindowController, IWindowManager iWindowManager, Executor executor, UiEventLogger uiEventLogger, RingerModeTracker ringerModeTracker, SysUiState sysUiState, Handler handler, ControlsComponent controlsComponent, CurrentUserContextTracker currentUserContextTracker) {
        ContentResolver contentResolver2 = contentResolver;
        Resources resources2 = resources;
        KeyguardStateController keyguardStateController2 = keyguardStateController;
        this.mContext = context;
        this.mWindowManagerFuncs = globalActionsManager;
        this.mAudioManager = audioManager;
        this.mDreamManager = iDreamManager;
        this.mDevicePolicyManager = devicePolicyManager;
        this.mLockPatternUtils = lockPatternUtils;
        this.mKeyguardStateController = keyguardStateController2;
        this.mBroadcastDispatcher = broadcastDispatcher;
        this.mContentResolver = contentResolver2;
        this.mResources = resources2;
        this.mConfigurationController = configurationController;
        this.mUserManager = userManager;
        this.mTrustManager = trustManager;
        this.mIActivityManager = iActivityManager;
        this.mTelecomManager = telecomManager;
        this.mMetricsLogger = metricsLogger;
        this.mUiEventLogger = uiEventLogger;
        this.mDepthController = notificationShadeDepthController;
        this.mSysuiColorExtractor = sysuiColorExtractor;
        this.mStatusBarService = iStatusBarService;
        this.mNotificationShadeWindowController = notificationShadeWindowController;
        this.mControlsUiControllerOptional = controlsComponent.getControlsUiController();
        this.mIWindowManager = iWindowManager;
        this.mBackgroundExecutor = executor;
        this.mRingerModeTracker = ringerModeTracker;
        this.mControlsControllerOptional = controlsComponent.getControlsController();
        this.mSysUiState = sysUiState;
        this.mMainHandler = handler;
        this.mCurrentUserContextTracker = currentUserContextTracker;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.CLOSE_SYSTEM_DIALOGS");
        intentFilter.addAction("android.intent.action.SCREEN_OFF");
        intentFilter.addAction("android.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED");
        this.mBroadcastDispatcher.registerReceiver(this.mBroadcastReceiver, intentFilter);
        this.mHasTelephony = connectivityManager.isNetworkSupported(0);
        telephonyManager.listen(this.mPhoneStateListener, 1);
        contentResolver2.registerContentObserver(Settings.Global.getUriFor("airplane_mode_on"), true, this.mAirplaneModeObserver);
        this.mHasVibrator = vibrator != null && vibrator.hasVibrator();
        boolean z = !resources2.getBoolean(17891574);
        this.mShowSilentToggle = z;
        if (z) {
            this.mRingerModeTracker.getRingerMode().observe(this, new Observer() {
                public final void onChanged(Object obj) {
                    GlobalActionsDialog.this.lambda$new$0$GlobalActionsDialog((Integer) obj);
                }
            });
        }
        this.mEmergencyAffordanceManager = new EmergencyAffordanceManager(context);
        this.mScreenshotHelper = new ScreenshotHelper(context);
        this.mScreenRecordHelper = new ScreenRecordHelper(context);
        this.mConfigurationController.addCallback(this);
        this.mActivityStarter = activityStarter;
        keyguardStateController2.addCallback(new KeyguardStateController.Callback() {
            public void onUnlockedChanged() {
                GlobalActionsDialog globalActionsDialog = GlobalActionsDialog.this;
                if (globalActionsDialog.mDialog != null) {
                    boolean isUnlocked = globalActionsDialog.mKeyguardStateController.isUnlocked();
                    if (GlobalActionsDialog.this.mDialog.mWalletViewController != null) {
                        GlobalActionsDialog.this.mDialog.mWalletViewController.onDeviceLockStateChanged(!isUnlocked);
                    }
                    if (!GlobalActionsDialog.this.mDialog.isShowingControls() && GlobalActionsDialog.this.shouldShowControls()) {
                        GlobalActionsDialog globalActionsDialog2 = GlobalActionsDialog.this;
                        globalActionsDialog2.mDialog.showControls((ControlsUiController) globalActionsDialog2.mControlsUiControllerOptional.get());
                    }
                    if (isUnlocked) {
                        GlobalActionsDialog.this.mDialog.hideLockMessage();
                    }
                }
            }
        });
        if (controlsComponent.getControlsListingController().isPresent()) {
            controlsComponent.getControlsListingController().get().addCallback(new ControlsListingController.ControlsListingCallback() {
                public final void onServicesUpdated(List list) {
                    GlobalActionsDialog.this.lambda$new$1$GlobalActionsDialog(list);
                }
            });
        }
        onPowerMenuLockScreenSettingsChanged();
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("power_menu_locked_show_content"), false, new ContentObserver(this.mMainHandler) {
            public void onChange(boolean z) {
                GlobalActionsDialog.this.onPowerMenuLockScreenSettingsChanged();
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$0 */
    public /* synthetic */ void lambda$new$0$GlobalActionsDialog(Integer num) {
        this.mHandler.sendEmptyMessage(1);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$1 */
    public /* synthetic */ void lambda$new$1$GlobalActionsDialog(List list) {
        this.mControlsServiceInfos = list;
        ActionsDialog actionsDialog = this.mDialog;
        if (actionsDialog == null) {
            return;
        }
        if (!actionsDialog.isShowingControls() && shouldShowControls()) {
            this.mDialog.showControls(this.mControlsUiControllerOptional.get());
        } else if (shouldShowLockMessage()) {
            this.mDialog.showLockMessage();
        }
    }

    private void seedFavorites() {
        if (this.mControlsControllerOptional.isPresent() && !this.mControlsServiceInfos.isEmpty()) {
            String[] stringArray = this.mContext.getResources().getStringArray(C0005R$array.config_controlsPreferredPackages);
            SharedPreferences sharedPreferences = this.mCurrentUserContextTracker.getCurrentUserContext().getSharedPreferences("controls_prefs", 0);
            Set<String> stringSet = sharedPreferences.getStringSet("SeedingCompleted", Collections.emptySet());
            ArrayList arrayList = new ArrayList();
            for (int i = 0; i < Math.min(2, stringArray.length); i++) {
                String str = stringArray[i];
                Iterator<ControlsServiceInfo> it = this.mControlsServiceInfos.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    ControlsServiceInfo next = it.next();
                    if (str.equals(next.componentName.getPackageName())) {
                        if (!stringSet.contains(str)) {
                            if (this.mControlsControllerOptional.get().countFavoritesForComponent(next.componentName) > 0) {
                                addPackageToSeededSet(sharedPreferences, str);
                            } else {
                                arrayList.add(next.componentName);
                            }
                        }
                    }
                }
            }
            if (!arrayList.isEmpty()) {
                this.mControlsControllerOptional.get().seedFavoritesForComponents(arrayList, new Consumer(sharedPreferences) {
                    public final /* synthetic */ SharedPreferences f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void accept(Object obj) {
                        GlobalActionsDialog.this.lambda$seedFavorites$2$GlobalActionsDialog(this.f$1, (SeedResponse) obj);
                    }
                });
            }
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$seedFavorites$2 */
    public /* synthetic */ void lambda$seedFavorites$2$GlobalActionsDialog(SharedPreferences sharedPreferences, SeedResponse seedResponse) {
        Log.d("GlobalActionsDialog", "Controls seeded: " + seedResponse);
        if (seedResponse.getAccepted()) {
            addPackageToSeededSet(sharedPreferences, seedResponse.getPackageName());
        }
    }

    private void addPackageToSeededSet(SharedPreferences sharedPreferences, String str) {
        HashSet hashSet = new HashSet(sharedPreferences.getStringSet("SeedingCompleted", Collections.emptySet()));
        hashSet.add(str);
        sharedPreferences.edit().putStringSet("SeedingCompleted", hashSet).apply();
    }

    public void showOrHideDialog(boolean z, boolean z2, GlobalActionsPanelPlugin globalActionsPanelPlugin) {
        this.mKeyguardShowing = z;
        this.mDeviceProvisioned = z2;
        this.mWalletPlugin = globalActionsPanelPlugin;
        ActionsDialog actionsDialog = this.mDialog;
        if (actionsDialog == null || !actionsDialog.isShowing()) {
            handleShow();
            return;
        }
        this.mWindowManagerFuncs.onGlobalActionsShown();
        this.mDialog.dismiss();
        this.mDialog = null;
    }

    public void dismissDialog() {
        this.mHandler.removeMessages(0);
        this.mHandler.sendEmptyMessage(0);
    }

    private void awakenIfNecessary() {
        IDreamManager iDreamManager = this.mDreamManager;
        if (iDreamManager != null) {
            try {
                if (iDreamManager.isDreaming()) {
                    this.mDreamManager.awaken();
                }
            } catch (RemoteException unused) {
            }
        }
    }

    private void handleShow() {
        awakenIfNecessary();
        this.mDialog = createDialog();
        prepareDialog();
        seedFavorites();
        WindowManager.LayoutParams attributes = this.mDialog.getWindow().getAttributes();
        attributes.setTitle("ActionsDialog");
        attributes.layoutInDisplayCutoutMode = 3;
        this.mDialog.getWindow().setAttributes(attributes);
        this.mDialog.getWindow().setFlags(131072, 131072);
        this.mDialog.show();
        this.mWindowManagerFuncs.onGlobalActionsShown();
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public boolean shouldShowAction(Action action) {
        if (this.mKeyguardShowing && !action.showDuringKeyguard()) {
            return false;
        }
        if (this.mDeviceProvisioned || action.showBeforeProvisioning()) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public int getMaxShownPowerItems() {
        return this.mResources.getInteger(C0013R$integer.power_menu_max_columns);
    }

    private void addActionItem(Action action) {
        if (this.mItems.size() < getMaxShownPowerItems()) {
            this.mItems.add(action);
        } else {
            this.mOverflowItems.add(action);
        }
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public String[] getDefaultActions() {
        return this.mResources.getStringArray(17236042);
    }

    private void addIfShouldShowAction(List<Action> list, Action action) {
        if (shouldShowAction(action)) {
            list.add(action);
        }
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public void createActionItems() {
        if (!this.mHasVibrator) {
            this.mSilentModeAction = new SilentModeToggleAction();
        } else {
            this.mSilentModeAction = new SilentModeTriStateAction(this.mAudioManager, this.mHandler);
        }
        this.mAirplaneModeOn = new AirplaneModeAction();
        onAirplaneModeChanged();
        this.mItems.clear();
        this.mOverflowItems.clear();
        this.mPowerItems.clear();
        String[] defaultActions = getDefaultActions();
        ShutDownAction shutDownAction = new ShutDownAction();
        RestartAction restartAction = new RestartAction();
        ArraySet arraySet = new ArraySet();
        ArrayList<Action> arrayList = new ArrayList<>();
        CurrentUserProvider currentUserProvider = new CurrentUserProvider();
        if (this.mEmergencyAffordanceManager.needsEmergencyAffordance()) {
            addIfShouldShowAction(arrayList, new EmergencyAffordanceAction());
            arraySet.add("emergency");
        }
        for (String str : defaultActions) {
            if (!arraySet.contains(str)) {
                if (GLOBAL_ACTION_KEY_POWER.equals(str)) {
                    addIfShouldShowAction(arrayList, shutDownAction);
                } else if ("airplane".equals(str)) {
                    addIfShouldShowAction(arrayList, this.mAirplaneModeOn);
                } else if ("bugreport".equals(str)) {
                    if (shouldDisplayBugReport(currentUserProvider.get())) {
                        addIfShouldShowAction(arrayList, new BugReportAction());
                    }
                } else if ("silent".equals(str)) {
                    if (this.mShowSilentToggle) {
                        addIfShouldShowAction(arrayList, this.mSilentModeAction);
                    }
                } else if ("users".equals(str)) {
                    if (SystemProperties.getBoolean("fw.power_user_switcher", false)) {
                        addUserActions(arrayList, currentUserProvider.get());
                    }
                } else if ("settings".equals(str)) {
                    addIfShouldShowAction(arrayList, getSettingsAction());
                } else if ("lockdown".equals(str)) {
                    if (shouldDisplayLockdown(currentUserProvider.get())) {
                        addIfShouldShowAction(arrayList, new LockDownAction());
                    }
                } else if ("voiceassist".equals(str)) {
                    addIfShouldShowAction(arrayList, getVoiceAssistAction());
                } else if ("assist".equals(str)) {
                    addIfShouldShowAction(arrayList, getAssistAction());
                } else if ("restart".equals(str)) {
                    addIfShouldShowAction(arrayList, restartAction);
                } else if ("screenshot".equals(str)) {
                    addIfShouldShowAction(arrayList, new ScreenshotAction());
                } else if ("logout".equals(str)) {
                    if (!(!this.mDevicePolicyManager.isLogoutEnabled() || currentUserProvider.get() == null || currentUserProvider.get().id == 0)) {
                        addIfShouldShowAction(arrayList, new LogoutAction());
                    }
                } else if ("emergency".equals(str)) {
                    addIfShouldShowAction(arrayList, new EmergencyDialerAction());
                } else {
                    Log.e("GlobalActionsDialog", "Invalid global action key " + str);
                }
                arraySet.add(str);
            }
        }
        if (arrayList.contains(shutDownAction) && arrayList.contains(restartAction) && arrayList.size() > getMaxShownPowerItems()) {
            int min = Math.min(arrayList.indexOf(restartAction), arrayList.indexOf(shutDownAction));
            arrayList.remove(shutDownAction);
            arrayList.remove(restartAction);
            this.mPowerItems.add(shutDownAction);
            this.mPowerItems.add(restartAction);
            arrayList.add(min, new PowerOptionsAction());
        }
        for (Action addActionItem : arrayList) {
            addActionItem(addActionItem);
        }
    }

    /* access modifiers changed from: private */
    public void onRotate() {
        createActionItems();
    }

    private ActionsDialog createDialog() {
        createActionItems();
        this.mAdapter = new MyAdapter();
        this.mOverflowAdapter = new MyOverflowAdapter();
        this.mPowerAdapter = new MyPowerOptionsAdapter();
        this.mDepthController.setShowingHomeControls(true);
        ActionsDialog actionsDialog = new ActionsDialog(this.mContext, this.mAdapter, this.mOverflowAdapter, getWalletViewController(), this.mDepthController, this.mSysuiColorExtractor, this.mStatusBarService, this.mNotificationShadeWindowController, controlsAvailable(), (!this.mControlsUiControllerOptional.isPresent() || !shouldShowControls()) ? null : this.mControlsUiControllerOptional.get(), this.mSysUiState, new Runnable() {
            public final void run() {
                GlobalActionsDialog.this.onRotate();
            }
        }, this.mKeyguardShowing, this.mPowerAdapter);
        if (shouldShowLockMessage()) {
            actionsDialog.showLockMessage();
        }
        actionsDialog.setCanceledOnTouchOutside(false);
        actionsDialog.setOnDismissListener(this);
        actionsDialog.setOnShowListener(this);
        return actionsDialog;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public boolean shouldDisplayLockdown(UserInfo userInfo) {
        if (userInfo == null) {
            return false;
        }
        int i = userInfo.id;
        if (Settings.Secure.getIntForUser(this.mContentResolver, "lockdown_in_power_menu", 0, i) == 0 || !this.mKeyguardStateController.isMethodSecure()) {
            return false;
        }
        int strongAuthForUser = this.mLockPatternUtils.getStrongAuthForUser(i);
        if (strongAuthForUser == 0 || strongAuthForUser == 4) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public boolean shouldDisplayBugReport(UserInfo userInfo) {
        if (Settings.Global.getInt(this.mContentResolver, "bugreport_in_power_menu", 0) == 0) {
            return false;
        }
        if (userInfo == null || userInfo.isPrimary()) {
            return true;
        }
        return false;
    }

    public void onUiModeChanged() {
        this.mContext.getTheme().applyStyle(this.mContext.getThemeResId(), true);
        ActionsDialog actionsDialog = this.mDialog;
        if (actionsDialog != null && actionsDialog.isShowing()) {
            this.mDialog.refreshDialog();
        }
    }

    public void destroy() {
        this.mConfigurationController.removeCallback(this);
    }

    private GlobalActionsPanelPlugin.PanelViewController getWalletViewController() {
        GlobalActionsPanelPlugin globalActionsPanelPlugin = this.mWalletPlugin;
        if (globalActionsPanelPlugin == null) {
            return null;
        }
        return globalActionsPanelPlugin.onPanelShown(this, !this.mKeyguardStateController.isUnlocked());
    }

    public void dismissGlobalActionsMenu() {
        dismissDialog();
    }

    public void startPendingIntentDismissingKeyguard(PendingIntent pendingIntent) {
        this.mActivityStarter.startPendingIntentDismissingKeyguard(pendingIntent);
    }

    @VisibleForTesting
    protected final class PowerOptionsAction extends SinglePressAction {
        public boolean showBeforeProvisioning() {
            return true;
        }

        public boolean showDuringKeyguard() {
            return true;
        }

        private PowerOptionsAction() {
            super(GlobalActionsDialog.this, C0010R$drawable.ic_settings_power, 17040302);
        }

        public void onPress() {
            ActionsDialog actionsDialog = GlobalActionsDialog.this.mDialog;
            if (actionsDialog != null) {
                actionsDialog.showPowerOptionsMenu();
            }
        }
    }

    @VisibleForTesting
    final class ShutDownAction extends SinglePressAction implements LongPressAction {
        public boolean showBeforeProvisioning() {
            return true;
        }

        public boolean showDuringKeyguard() {
            return true;
        }

        private ShutDownAction() {
            super(GlobalActionsDialog.this, 17301552, 17040301);
        }

        public boolean onLongPress() {
            if (GlobalActionsDialog.this.mUserManager.hasUserRestriction("no_safe_boot")) {
                return false;
            }
            GlobalActionsDialog.this.mWindowManagerFuncs.reboot(true);
            return true;
        }

        public void onPress() {
            GlobalActionsDialog.this.mWindowManagerFuncs.shutdown();
        }
    }

    @VisibleForTesting
    protected abstract class EmergencyAction extends SinglePressAction {
        public boolean shouldBeSeparated() {
            return false;
        }

        public boolean showBeforeProvisioning() {
            return true;
        }

        public boolean showDuringKeyguard() {
            return true;
        }

        EmergencyAction(GlobalActionsDialog globalActionsDialog, int i, int i2) {
            super(globalActionsDialog, i, i2);
        }

        public View create(Context context, View view, ViewGroup viewGroup, LayoutInflater layoutInflater) {
            View create = super.create(context, view, viewGroup, layoutInflater);
            create.setBackgroundTintList(ColorStateList.valueOf(create.getResources().getColor(C0008R$color.global_actions_emergency_background)));
            int color = create.getResources().getColor(C0008R$color.global_actions_emergency_text);
            TextView textView = (TextView) create.findViewById(16908299);
            textView.setTextColor(color);
            textView.setSelected(true);
            ((ImageView) create.findViewById(16908294)).getDrawable().setTint(color);
            return create;
        }
    }

    private class EmergencyAffordanceAction extends EmergencyAction {
        EmergencyAffordanceAction() {
            super(GlobalActionsDialog.this, 17302220, 17040297);
        }

        public void onPress() {
            GlobalActionsDialog.this.mEmergencyAffordanceManager.performEmergencyCall();
        }
    }

    @VisibleForTesting
    class EmergencyDialerAction extends EmergencyAction {
        private EmergencyDialerAction() {
            super(GlobalActionsDialog.this, C0010R$drawable.ic_emergency_star, 17040297);
        }

        public void onPress() {
            GlobalActionsDialog.this.mMetricsLogger.action(1569);
            GlobalActionsDialog.this.mUiEventLogger.log(GlobalActionsEvent.GA_EMERGENCY_DIALER_PRESS);
            if (GlobalActionsDialog.this.mTelecomManager != null) {
                Intent createLaunchEmergencyDialerIntent = GlobalActionsDialog.this.mTelecomManager.createLaunchEmergencyDialerIntent((String) null);
                createLaunchEmergencyDialerIntent.addFlags(343932928);
                createLaunchEmergencyDialerIntent.putExtra("com.android.phone.EmergencyDialer.extra.ENTRY_TYPE", 2);
                GlobalActionsDialog.this.mContext.startActivityAsUser(createLaunchEmergencyDialerIntent, UserHandle.CURRENT);
            }
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public EmergencyDialerAction makeEmergencyDialerActionForTesting() {
        return new EmergencyDialerAction();
    }

    @VisibleForTesting
    final class RestartAction extends SinglePressAction implements LongPressAction {
        public boolean showBeforeProvisioning() {
            return true;
        }

        public boolean showDuringKeyguard() {
            return true;
        }

        private RestartAction() {
            super(GlobalActionsDialog.this, 17302828, 17040304);
        }

        public boolean onLongPress() {
            if (GlobalActionsDialog.this.mUserManager.hasUserRestriction("no_safe_boot")) {
                return false;
            }
            GlobalActionsDialog.this.mWindowManagerFuncs.reboot(true);
            return true;
        }

        public void onPress() {
            GlobalActionsDialog.this.mWindowManagerFuncs.reboot(false);
        }
    }

    @VisibleForTesting
    class ScreenshotAction extends SinglePressAction implements LongPressAction {
        public boolean showBeforeProvisioning() {
            return false;
        }

        public boolean showDuringKeyguard() {
            return true;
        }

        public ScreenshotAction() {
            super(GlobalActionsDialog.this, 17302830, 17040305);
        }

        public void onPress() {
            GlobalActionsDialog.this.mHandler.postDelayed(new Runnable() {
                public void run() {
                    GlobalActionsDialog.this.mScreenshotHelper.takeScreenshot(1, true, true, 0, GlobalActionsDialog.this.mHandler, (Consumer) null);
                    GlobalActionsDialog.this.mMetricsLogger.action(1282);
                    GlobalActionsDialog.this.mUiEventLogger.log(GlobalActionsEvent.GA_SCREENSHOT_PRESS);
                }
            }, (long) GlobalActionsDialog.this.mDialogPressDelay);
        }

        public boolean onLongPress() {
            if (FeatureFlagUtils.isEnabled(GlobalActionsDialog.this.mContext, "settings_screenrecord_long_press")) {
                GlobalActionsDialog.this.mUiEventLogger.log(GlobalActionsEvent.GA_SCREENSHOT_LONG_PRESS);
                GlobalActionsDialog.this.mScreenRecordHelper.launchRecordPrompt();
                return true;
            }
            onPress();
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public ScreenshotAction makeScreenshotActionForTesting() {
        return new ScreenshotAction();
    }

    @VisibleForTesting
    class BugReportAction extends SinglePressAction implements LongPressAction {
        public boolean showBeforeProvisioning() {
            return false;
        }

        public boolean showDuringKeyguard() {
            return true;
        }

        public BugReportAction() {
            super(GlobalActionsDialog.this, 17302484, 17039792);
        }

        public void onPress() {
            if (!ActivityManager.isUserAMonkey()) {
                GlobalActionsDialog.this.mHandler.postDelayed(new Runnable() {
                    public void run() {
                        try {
                            GlobalActionsDialog.this.mMetricsLogger.action(292);
                            GlobalActionsDialog.this.mUiEventLogger.log(GlobalActionsEvent.GA_BUGREPORT_PRESS);
                            if (!GlobalActionsDialog.this.mIActivityManager.launchBugReportHandlerApp()) {
                                Log.w("GlobalActionsDialog", "Bugreport handler could not be launched");
                                GlobalActionsDialog.this.mIActivityManager.requestInteractiveBugReport();
                            }
                        } catch (RemoteException unused) {
                        }
                    }
                }, (long) GlobalActionsDialog.this.mDialogPressDelay);
            }
        }

        public boolean onLongPress() {
            if (ActivityManager.isUserAMonkey()) {
                return false;
            }
            try {
                GlobalActionsDialog.this.mMetricsLogger.action(293);
                GlobalActionsDialog.this.mUiEventLogger.log(GlobalActionsEvent.GA_BUGREPORT_LONG_PRESS);
                GlobalActionsDialog.this.mIActivityManager.requestFullBugReport();
            } catch (RemoteException unused) {
            }
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public BugReportAction makeBugReportActionForTesting() {
        return new BugReportAction();
    }

    private final class LogoutAction extends SinglePressAction {
        public boolean showBeforeProvisioning() {
            return false;
        }

        public boolean showDuringKeyguard() {
            return true;
        }

        private LogoutAction() {
            super(GlobalActionsDialog.this, 17302535, 17040300);
        }

        public void onPress() {
            GlobalActionsDialog.this.mHandler.postDelayed(new Runnable() {
                public final void run() {
                    GlobalActionsDialog.LogoutAction.this.lambda$onPress$0$GlobalActionsDialog$LogoutAction();
                }
            }, (long) GlobalActionsDialog.this.mDialogPressDelay);
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onPress$0 */
        public /* synthetic */ void lambda$onPress$0$GlobalActionsDialog$LogoutAction() {
            try {
                int i = GlobalActionsDialog.this.getCurrentUser().id;
                GlobalActionsDialog.this.mIActivityManager.switchUser(0);
                GlobalActionsDialog.this.mIActivityManager.stopUser(i, true, (IStopUserCallback) null);
            } catch (RemoteException e) {
                Log.e("GlobalActionsDialog", "Couldn't logout user " + e);
            }
        }
    }

    private Action getSettingsAction() {
        return new SinglePressAction(17302836, 17040306) {
            public boolean showBeforeProvisioning() {
                return true;
            }

            public boolean showDuringKeyguard() {
                return true;
            }

            public void onPress() {
                Intent intent = new Intent("android.settings.SETTINGS");
                intent.addFlags(335544320);
                GlobalActionsDialog.this.mContext.startActivity(intent);
            }
        };
    }

    private Action getAssistAction() {
        return new SinglePressAction(17302307, 17040295) {
            public boolean showBeforeProvisioning() {
                return true;
            }

            public boolean showDuringKeyguard() {
                return true;
            }

            public void onPress() {
                Intent intent = new Intent("android.intent.action.ASSIST");
                intent.addFlags(335544320);
                GlobalActionsDialog.this.mContext.startActivity(intent);
            }
        };
    }

    private Action getVoiceAssistAction() {
        return new SinglePressAction(17302878, 17040310) {
            public boolean showBeforeProvisioning() {
                return true;
            }

            public boolean showDuringKeyguard() {
                return true;
            }

            public void onPress() {
                Intent intent = new Intent("android.intent.action.VOICE_ASSIST");
                intent.addFlags(335544320);
                GlobalActionsDialog.this.mContext.startActivity(intent);
            }
        };
    }

    @VisibleForTesting
    class LockDownAction extends SinglePressAction {
        public boolean showBeforeProvisioning() {
            return false;
        }

        public boolean showDuringKeyguard() {
            return true;
        }

        LockDownAction() {
            super(GlobalActionsDialog.this, 17302487, 17040299);
        }

        public void onPress() {
            GlobalActionsDialog.this.mLockPatternUtils.requireStrongAuth(32, -1);
            try {
                GlobalActionsDialog.this.mIWindowManager.lockNow((Bundle) null);
                GlobalActionsDialog.this.mBackgroundExecutor.execute(new Runnable() {
                    public final void run() {
                        GlobalActionsDialog.LockDownAction.this.lambda$onPress$0$GlobalActionsDialog$LockDownAction();
                    }
                });
            } catch (RemoteException e) {
                Log.e("GlobalActionsDialog", "Error while trying to lock device.", e);
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onPress$0 */
        public /* synthetic */ void lambda$onPress$0$GlobalActionsDialog$LockDownAction() {
            GlobalActionsDialog.this.lockProfiles();
        }
    }

    /* access modifiers changed from: private */
    public void lockProfiles() {
        int i = getCurrentUser().id;
        for (int i2 : this.mUserManager.getEnabledProfileIds(i)) {
            if (i2 != i) {
                this.mTrustManager.setDeviceLockedForUser(i2, true);
            }
        }
    }

    /* access modifiers changed from: private */
    public UserInfo getCurrentUser() {
        try {
            return this.mIActivityManager.getCurrentUser();
        } catch (RemoteException unused) {
            return null;
        }
    }

    private class CurrentUserProvider {
        private boolean mFetched;
        private UserInfo mUserInfo;

        private CurrentUserProvider() {
            this.mUserInfo = null;
            this.mFetched = false;
        }

        /* access modifiers changed from: package-private */
        public UserInfo get() {
            if (!this.mFetched) {
                this.mFetched = true;
                this.mUserInfo = GlobalActionsDialog.this.getCurrentUser();
            }
            return this.mUserInfo;
        }
    }

    private void addUserActions(List<Action> list, UserInfo userInfo) {
        if (this.mUserManager.isUserSwitcherEnabled()) {
            for (final UserInfo userInfo2 : this.mUserManager.getUsers()) {
                if (userInfo2.supportsSwitchToByUser()) {
                    boolean z = true;
                    if (userInfo != null ? userInfo.id != userInfo2.id : userInfo2.id != 0) {
                        z = false;
                    }
                    String str = userInfo2.iconPath;
                    Drawable createFromPath = str != null ? Drawable.createFromPath(str) : null;
                    StringBuilder sb = new StringBuilder();
                    String str2 = userInfo2.name;
                    if (str2 == null) {
                        str2 = "Primary";
                    }
                    sb.append(str2);
                    sb.append(z ? " ✔" : "");
                    addIfShouldShowAction(list, new SinglePressAction(17302706, createFromPath, sb.toString()) {
                        public boolean showBeforeProvisioning() {
                            return false;
                        }

                        public boolean showDuringKeyguard() {
                            return true;
                        }

                        public void onPress() {
                            try {
                                GlobalActionsDialog.this.mIActivityManager.switchUser(userInfo2.id);
                            } catch (RemoteException e) {
                                Log.e("GlobalActionsDialog", "Couldn't switch user " + e);
                            }
                        }
                    });
                }
            }
        }
    }

    private void prepareDialog() {
        refreshSilentMode();
        this.mAirplaneModeOn.updateState(this.mAirplaneState);
        this.mAdapter.notifyDataSetChanged();
        this.mLifecycle.setCurrentState(Lifecycle.State.RESUMED);
    }

    /* access modifiers changed from: private */
    public void refreshSilentMode() {
        if (!this.mHasVibrator) {
            Integer value = this.mRingerModeTracker.getRingerMode().getValue();
            ((ToggleAction) this.mSilentModeAction).updateState(value != null && value.intValue() != 2 ? ToggleState.On : ToggleState.Off);
        }
    }

    public void onDismiss(DialogInterface dialogInterface) {
        if (this.mDialog == dialogInterface) {
            this.mDialog = null;
        }
        this.mUiEventLogger.log(GlobalActionsEvent.GA_POWER_MENU_CLOSE);
        this.mWindowManagerFuncs.onGlobalActionsHidden();
        this.mLifecycle.setCurrentState(Lifecycle.State.CREATED);
    }

    public void onShow(DialogInterface dialogInterface) {
        this.mMetricsLogger.visible(1568);
        this.mUiEventLogger.log(GlobalActionsEvent.GA_POWER_MENU_OPEN);
    }

    public class MyAdapter extends MultiListLayout.MultiListAdapter {
        public boolean areAllItemsEnabled() {
            return false;
        }

        public long getItemId(int i) {
            return (long) i;
        }

        public MyAdapter() {
        }

        private int countItems(boolean z) {
            int i = 0;
            for (int i2 = 0; i2 < GlobalActionsDialog.this.mItems.size(); i2++) {
                if (GlobalActionsDialog.this.mItems.get(i2).shouldBeSeparated() == z) {
                    i++;
                }
            }
            return i;
        }

        public int countSeparatedItems() {
            return countItems(true);
        }

        public int countListItems() {
            return countItems(false);
        }

        public int getCount() {
            return countSeparatedItems() + countListItems();
        }

        public boolean isEnabled(int i) {
            return getItem(i).isEnabled();
        }

        public Action getItem(int i) {
            int i2 = 0;
            for (int i3 = 0; i3 < GlobalActionsDialog.this.mItems.size(); i3++) {
                Action action = GlobalActionsDialog.this.mItems.get(i3);
                if (GlobalActionsDialog.this.shouldShowAction(action)) {
                    if (i2 == i) {
                        return action;
                    }
                    i2++;
                }
            }
            throw new IllegalArgumentException("position " + i + " out of range of showable actions, filtered count=" + getCount() + ", keyguardshowing=" + GlobalActionsDialog.this.mKeyguardShowing + ", provisioned=" + GlobalActionsDialog.this.mDeviceProvisioned);
        }

        public View getView(int i, View view, ViewGroup viewGroup) {
            Action item = getItem(i);
            View create = item.create(GlobalActionsDialog.this.mContext, view, viewGroup, LayoutInflater.from(GlobalActionsDialog.this.mContext));
            create.setOnClickListener(new View.OnClickListener(i) {
                public final /* synthetic */ int f$1;

                {
                    this.f$1 = r2;
                }

                public final void onClick(View view) {
                    GlobalActionsDialog.MyAdapter.this.lambda$getView$0$GlobalActionsDialog$MyAdapter(this.f$1, view);
                }
            });
            if (item instanceof LongPressAction) {
                create.setOnLongClickListener(new View.OnLongClickListener(i) {
                    public final /* synthetic */ int f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final boolean onLongClick(View view) {
                        return GlobalActionsDialog.MyAdapter.this.lambda$getView$1$GlobalActionsDialog$MyAdapter(this.f$1, view);
                    }
                });
            }
            return create;
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$getView$0 */
        public /* synthetic */ void lambda$getView$0$GlobalActionsDialog$MyAdapter(int i, View view) {
            onClickItem(i);
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$getView$1 */
        public /* synthetic */ boolean lambda$getView$1$GlobalActionsDialog$MyAdapter(int i, View view) {
            return onLongClickItem(i);
        }

        public boolean onLongClickItem(int i) {
            Action item = GlobalActionsDialog.this.mAdapter.getItem(i);
            if (!(item instanceof LongPressAction)) {
                return false;
            }
            ActionsDialog actionsDialog = GlobalActionsDialog.this.mDialog;
            if (actionsDialog != null) {
                actionsDialog.dismiss();
            } else {
                Log.w("GlobalActionsDialog", "Action long-clicked while mDialog is null.");
            }
            return ((LongPressAction) item).onLongPress();
        }

        public void onClickItem(int i) {
            Action item = GlobalActionsDialog.this.mAdapter.getItem(i);
            if (!(item instanceof SilentModeTriStateAction)) {
                ActionsDialog actionsDialog = GlobalActionsDialog.this.mDialog;
                if (actionsDialog == null) {
                    Log.w("GlobalActionsDialog", "Action clicked while mDialog is null.");
                } else if (!(item instanceof PowerOptionsAction)) {
                    actionsDialog.dismiss();
                }
                item.onPress();
            }
        }

        public boolean shouldBeSeparated(int i) {
            return getItem(i).shouldBeSeparated();
        }
    }

    public class MyPowerOptionsAdapter extends BaseAdapter {
        public long getItemId(int i) {
            return (long) i;
        }

        public MyPowerOptionsAdapter() {
        }

        public int getCount() {
            return GlobalActionsDialog.this.mPowerItems.size();
        }

        public Action getItem(int i) {
            return GlobalActionsDialog.this.mPowerItems.get(i);
        }

        public View getView(int i, View view, ViewGroup viewGroup) {
            Action item = getItem(i);
            if (item == null) {
                Log.w("GlobalActionsDialog", "No power options action found at position: " + i);
                return null;
            }
            int i2 = C0014R$layout.global_actions_power_item;
            if (view == null) {
                view = LayoutInflater.from(GlobalActionsDialog.this.mContext).inflate(i2, viewGroup, false);
            }
            view.setOnClickListener(new View.OnClickListener(i) {
                public final /* synthetic */ int f$1;

                {
                    this.f$1 = r2;
                }

                public final void onClick(View view) {
                    GlobalActionsDialog.MyPowerOptionsAdapter.this.lambda$getView$0$GlobalActionsDialog$MyPowerOptionsAdapter(this.f$1, view);
                }
            });
            if (item instanceof LongPressAction) {
                view.setOnLongClickListener(new View.OnLongClickListener(i) {
                    public final /* synthetic */ int f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final boolean onLongClick(View view) {
                        return GlobalActionsDialog.MyPowerOptionsAdapter.this.lambda$getView$1$GlobalActionsDialog$MyPowerOptionsAdapter(this.f$1, view);
                    }
                });
            }
            ImageView imageView = (ImageView) view.findViewById(16908294);
            TextView textView = (TextView) view.findViewById(16908299);
            textView.setSelected(true);
            imageView.setImageDrawable(item.getIcon(GlobalActionsDialog.this.mContext));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            if (item.getMessage() != null) {
                textView.setText(item.getMessage());
            } else {
                textView.setText(item.getMessageResId());
            }
            return view;
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$getView$0 */
        public /* synthetic */ void lambda$getView$0$GlobalActionsDialog$MyPowerOptionsAdapter(int i, View view) {
            onClickItem(i);
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$getView$1 */
        public /* synthetic */ boolean lambda$getView$1$GlobalActionsDialog$MyPowerOptionsAdapter(int i, View view) {
            return onLongClickItem(i);
        }

        private boolean onLongClickItem(int i) {
            Action item = getItem(i);
            if (!(item instanceof LongPressAction)) {
                return false;
            }
            ActionsDialog actionsDialog = GlobalActionsDialog.this.mDialog;
            if (actionsDialog != null) {
                actionsDialog.dismiss();
            } else {
                Log.w("GlobalActionsDialog", "Action long-clicked while mDialog is null.");
            }
            return ((LongPressAction) item).onLongPress();
        }

        private void onClickItem(int i) {
            Action item = getItem(i);
            if (!(item instanceof SilentModeTriStateAction)) {
                ActionsDialog actionsDialog = GlobalActionsDialog.this.mDialog;
                if (actionsDialog != null) {
                    actionsDialog.dismiss();
                } else {
                    Log.w("GlobalActionsDialog", "Action clicked while mDialog is null.");
                }
                item.onPress();
            }
        }
    }

    public class MyOverflowAdapter extends BaseAdapter {
        public long getItemId(int i) {
            return (long) i;
        }

        public MyOverflowAdapter() {
        }

        public int getCount() {
            return GlobalActionsDialog.this.mOverflowItems.size();
        }

        public Action getItem(int i) {
            return GlobalActionsDialog.this.mOverflowItems.get(i);
        }

        public View getView(int i, View view, ViewGroup viewGroup) {
            Action item = getItem(i);
            if (item == null) {
                Log.w("GlobalActionsDialog", "No overflow action found at position: " + i);
                return null;
            }
            int i2 = C0014R$layout.controls_more_item;
            if (view == null) {
                view = LayoutInflater.from(GlobalActionsDialog.this.mContext).inflate(i2, viewGroup, false);
            }
            TextView textView = (TextView) view;
            if (item.getMessageResId() != 0) {
                textView.setText(item.getMessageResId());
            } else {
                textView.setText(item.getMessage());
            }
            return textView;
        }

        /* access modifiers changed from: private */
        public boolean onLongClickItem(int i) {
            Action item = getItem(i);
            if (!(item instanceof LongPressAction)) {
                return false;
            }
            ActionsDialog actionsDialog = GlobalActionsDialog.this.mDialog;
            if (actionsDialog != null) {
                actionsDialog.dismiss();
            } else {
                Log.w("GlobalActionsDialog", "Action long-clicked while mDialog is null.");
            }
            return ((LongPressAction) item).onLongPress();
        }

        /* access modifiers changed from: private */
        public void onClickItem(int i) {
            Action item = getItem(i);
            if (!(item instanceof SilentModeTriStateAction)) {
                ActionsDialog actionsDialog = GlobalActionsDialog.this.mDialog;
                if (actionsDialog != null) {
                    actionsDialog.dismiss();
                } else {
                    Log.w("GlobalActionsDialog", "Action clicked while mDialog is null.");
                }
                item.onPress();
            }
        }
    }

    private abstract class SinglePressAction implements Action {
        private final Drawable mIcon;
        private final int mIconResId;
        private final CharSequence mMessage;
        private final int mMessageResId;

        public boolean isEnabled() {
            return true;
        }

        protected SinglePressAction(GlobalActionsDialog globalActionsDialog, int i, int i2) {
            this.mIconResId = i;
            this.mMessageResId = i2;
            this.mMessage = null;
            this.mIcon = null;
        }

        protected SinglePressAction(GlobalActionsDialog globalActionsDialog, int i, Drawable drawable, CharSequence charSequence) {
            this.mIconResId = i;
            this.mMessageResId = 0;
            this.mMessage = charSequence;
            this.mIcon = drawable;
        }

        public int getMessageResId() {
            return this.mMessageResId;
        }

        public CharSequence getMessage() {
            return this.mMessage;
        }

        public Drawable getIcon(Context context) {
            Drawable drawable = this.mIcon;
            if (drawable != null) {
                return drawable;
            }
            return context.getDrawable(this.mIconResId);
        }

        public View create(Context context, View view, ViewGroup viewGroup, LayoutInflater layoutInflater) {
            View inflate = layoutInflater.inflate(C0014R$layout.global_actions_grid_item_v2, viewGroup, false);
            ImageView imageView = (ImageView) inflate.findViewById(16908294);
            TextView textView = (TextView) inflate.findViewById(16908299);
            textView.setSelected(true);
            imageView.setImageDrawable(getIcon(context));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            CharSequence charSequence = this.mMessage;
            if (charSequence != null) {
                textView.setText(charSequence);
            } else {
                textView.setText(this.mMessageResId);
            }
            return inflate;
        }
    }

    private enum ToggleState {
        Off(false),
        TurningOn(true),
        TurningOff(true),
        On(false);
        
        private final boolean mInTransition;

        private ToggleState(boolean z) {
            this.mInTransition = z;
        }

        public boolean inTransition() {
            return this.mInTransition;
        }
    }

    private abstract class ToggleAction implements Action {
        protected int mDisabledIconResid;
        protected int mDisabledStatusMessageResId;
        protected int mEnabledIconResId;
        protected int mEnabledStatusMessageResId;
        protected ToggleState mState = ToggleState.Off;

        public CharSequence getMessage() {
            return null;
        }

        /* access modifiers changed from: package-private */
        public abstract void onToggle(boolean z);

        /* access modifiers changed from: package-private */
        public void willCreate() {
        }

        public ToggleAction(GlobalActionsDialog globalActionsDialog, int i, int i2, int i3, int i4, int i5) {
            this.mEnabledIconResId = i;
            this.mDisabledIconResid = i2;
            this.mEnabledStatusMessageResId = i4;
            this.mDisabledStatusMessageResId = i5;
        }

        private boolean isOn() {
            ToggleState toggleState = this.mState;
            return toggleState == ToggleState.On || toggleState == ToggleState.TurningOn;
        }

        public int getMessageResId() {
            return isOn() ? this.mEnabledStatusMessageResId : this.mDisabledStatusMessageResId;
        }

        private int getIconResId() {
            return isOn() ? this.mEnabledIconResId : this.mDisabledIconResid;
        }

        public Drawable getIcon(Context context) {
            return context.getDrawable(getIconResId());
        }

        public View create(Context context, View view, ViewGroup viewGroup, LayoutInflater layoutInflater) {
            willCreate();
            View inflate = layoutInflater.inflate(C0014R$layout.global_actions_grid_item_v2, viewGroup, false);
            ImageView imageView = (ImageView) inflate.findViewById(16908294);
            TextView textView = (TextView) inflate.findViewById(16908299);
            boolean isEnabled = isEnabled();
            if (textView != null) {
                textView.setText(getMessageResId());
                textView.setEnabled(isEnabled);
                textView.setSelected(true);
            }
            if (imageView != null) {
                imageView.setImageDrawable(context.getDrawable(getIconResId()));
                imageView.setEnabled(isEnabled);
            }
            inflate.setEnabled(isEnabled);
            return inflate;
        }

        public final void onPress() {
            if (this.mState.inTransition()) {
                Log.w("GlobalActionsDialog", "shouldn't be able to toggle when in transition");
                return;
            }
            boolean z = this.mState != ToggleState.On;
            onToggle(z);
            changeStateFromPress(z);
        }

        public boolean isEnabled() {
            return !this.mState.inTransition();
        }

        /* access modifiers changed from: protected */
        public void changeStateFromPress(boolean z) {
            this.mState = z ? ToggleState.On : ToggleState.Off;
        }

        public void updateState(ToggleState toggleState) {
            this.mState = toggleState;
        }
    }

    private class AirplaneModeAction extends ToggleAction {
        public boolean showBeforeProvisioning() {
            return false;
        }

        public boolean showDuringKeyguard() {
            return true;
        }

        AirplaneModeAction() {
            super(GlobalActionsDialog.this, 17302480, 17302482, 17040314, 17040313, 17040312);
        }

        /* access modifiers changed from: package-private */
        public void onToggle(boolean z) {
            if (!GlobalActionsDialog.this.mHasTelephony || !((Boolean) TelephonyProperties.in_ecm_mode().orElse(Boolean.FALSE)).booleanValue()) {
                GlobalActionsDialog.this.changeAirplaneModeSystemSetting(z);
                return;
            }
            boolean unused = GlobalActionsDialog.this.mIsWaitingForEcmExit = true;
            Intent intent = new Intent("android.telephony.action.SHOW_NOTICE_ECM_BLOCK_OTHERS", (Uri) null);
            intent.addFlags(268435456);
            GlobalActionsDialog.this.mContext.startActivity(intent);
        }

        /* access modifiers changed from: protected */
        public void changeStateFromPress(boolean z) {
            if (GlobalActionsDialog.this.mHasTelephony && !((Boolean) TelephonyProperties.in_ecm_mode().orElse(Boolean.FALSE)).booleanValue()) {
                ToggleState toggleState = z ? ToggleState.TurningOn : ToggleState.TurningOff;
                this.mState = toggleState;
                ToggleState unused = GlobalActionsDialog.this.mAirplaneState = toggleState;
            }
        }
    }

    private class SilentModeToggleAction extends ToggleAction {
        public boolean showBeforeProvisioning() {
            return false;
        }

        public boolean showDuringKeyguard() {
            return true;
        }

        public SilentModeToggleAction() {
            super(GlobalActionsDialog.this, 17302328, 17302327, 17040309, 17040308, 17040307);
        }

        /* access modifiers changed from: package-private */
        public void onToggle(boolean z) {
            if (z) {
                GlobalActionsDialog.this.mAudioManager.setRingerMode(0);
            } else {
                GlobalActionsDialog.this.mAudioManager.setRingerMode(2);
            }
        }
    }

    private static class SilentModeTriStateAction implements Action, View.OnClickListener {
        private final int[] ITEM_IDS = {16909273, 16909274, 16909275};
        private final AudioManager mAudioManager;
        private final Handler mHandler;

        private int indexToRingerMode(int i) {
            return i;
        }

        private int ringerModeToIndex(int i) {
            return i;
        }

        public Drawable getIcon(Context context) {
            return null;
        }

        public CharSequence getMessage() {
            return null;
        }

        public int getMessageResId() {
            return 0;
        }

        public boolean isEnabled() {
            return true;
        }

        public void onPress() {
        }

        public boolean showBeforeProvisioning() {
            return false;
        }

        public boolean showDuringKeyguard() {
            return true;
        }

        SilentModeTriStateAction(AudioManager audioManager, Handler handler) {
            this.mAudioManager = audioManager;
            this.mHandler = handler;
        }

        public View create(Context context, View view, ViewGroup viewGroup, LayoutInflater layoutInflater) {
            View inflate = layoutInflater.inflate(17367169, viewGroup, false);
            int ringerMode = this.mAudioManager.getRingerMode();
            ringerModeToIndex(ringerMode);
            int i = 0;
            while (i < 3) {
                View findViewById = inflate.findViewById(this.ITEM_IDS[i]);
                findViewById.setSelected(ringerMode == i);
                findViewById.setTag(Integer.valueOf(i));
                findViewById.setOnClickListener(this);
                i++;
            }
            return inflate;
        }

        public void onClick(View view) {
            if (view.getTag() instanceof Integer) {
                int intValue = ((Integer) view.getTag()).intValue();
                AudioManager audioManager = this.mAudioManager;
                indexToRingerMode(intValue);
                audioManager.setRingerMode(intValue);
                this.mHandler.sendEmptyMessageDelayed(0, 300);
            }
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void setZeroDialogPressDelayForTesting() {
        this.mDialogPressDelay = 0;
    }

    /* access modifiers changed from: private */
    public void onAirplaneModeChanged() {
        if (!this.mHasTelephony) {
            boolean z = false;
            if (Settings.Global.getInt(this.mContentResolver, "airplane_mode_on", 0) == 1) {
                z = true;
            }
            ToggleState toggleState = z ? ToggleState.On : ToggleState.Off;
            this.mAirplaneState = toggleState;
            this.mAirplaneModeOn.updateState(toggleState);
        }
    }

    /* access modifiers changed from: private */
    public void changeAirplaneModeSystemSetting(boolean z) {
        Settings.Global.putInt(this.mContentResolver, "airplane_mode_on", z ? 1 : 0);
        Intent intent = new Intent("android.intent.action.AIRPLANE_MODE");
        intent.addFlags(536870912);
        intent.putExtra("state", z);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        if (!this.mHasTelephony) {
            this.mAirplaneState = z ? ToggleState.On : ToggleState.Off;
        }
    }

    public Lifecycle getLifecycle() {
        return this.mLifecycle;
    }

    @VisibleForTesting
    static final class ActionsDialog extends Dialog implements DialogInterface, ColorExtractor.OnColorsChangedListener {
        private final MyAdapter mAdapter;
        private Drawable mBackgroundDrawable;
        private final SysuiColorExtractor mColorExtractor;
        private ViewGroup mContainer;
        /* access modifiers changed from: private */
        public final Context mContext;
        private final boolean mControlsAvailable;
        private ControlsUiController mControlsUiController;
        private ViewGroup mControlsView;
        private final NotificationShadeDepthController mDepthController;
        private MultiListLayout mGlobalActionsLayout;
        private boolean mHadTopUi;
        private boolean mKeyguardShowing;
        private TextView mLockMessage;
        @VisibleForTesting
        ViewGroup mLockMessageContainer;
        private final NotificationShadeWindowController mNotificationShadeWindowController;
        private final Runnable mOnRotateCallback;
        private final MyOverflowAdapter mOverflowAdapter;
        private ListPopupWindow mOverflowPopup;
        private final MyPowerOptionsAdapter mPowerOptionsAdapter;
        private Dialog mPowerOptionsDialog;
        private ResetOrientationData mResetOrientationData;
        private float mScrimAlpha;
        private boolean mShowing;
        private final IStatusBarService mStatusBarService;
        private final SysUiState mSysUiState;
        private final IBinder mToken = new Binder();
        /* access modifiers changed from: private */
        public final GlobalActionsPanelPlugin.PanelViewController mWalletViewController;

        ActionsDialog(Context context, MyAdapter myAdapter, MyOverflowAdapter myOverflowAdapter, GlobalActionsPanelPlugin.PanelViewController panelViewController, NotificationShadeDepthController notificationShadeDepthController, SysuiColorExtractor sysuiColorExtractor, IStatusBarService iStatusBarService, NotificationShadeWindowController notificationShadeWindowController, boolean z, ControlsUiController controlsUiController, SysUiState sysUiState, Runnable runnable, boolean z2, MyPowerOptionsAdapter myPowerOptionsAdapter) {
            super(context, C0019R$style.Theme_SystemUI_Dialog_GlobalActions);
            this.mContext = context;
            this.mAdapter = myAdapter;
            this.mOverflowAdapter = myOverflowAdapter;
            this.mPowerOptionsAdapter = myPowerOptionsAdapter;
            this.mDepthController = notificationShadeDepthController;
            this.mColorExtractor = sysuiColorExtractor;
            this.mStatusBarService = iStatusBarService;
            this.mNotificationShadeWindowController = notificationShadeWindowController;
            this.mControlsAvailable = z;
            this.mControlsUiController = controlsUiController;
            this.mSysUiState = sysUiState;
            this.mOnRotateCallback = runnable;
            this.mKeyguardShowing = z2;
            Window window = getWindow();
            window.requestFeature(1);
            window.getDecorView();
            window.getAttributes().systemUiVisibility |= 1792;
            window.setLayout(-1, -1);
            window.clearFlags(2);
            window.addFlags(17629472);
            window.setType(2020);
            window.getAttributes().setFitInsetsTypes(0);
            setTitle(17040311);
            this.mWalletViewController = panelViewController;
            initializeLayout();
        }

        /* access modifiers changed from: private */
        public boolean isShowingControls() {
            return this.mControlsUiController != null;
        }

        /* access modifiers changed from: private */
        public void showControls(ControlsUiController controlsUiController) {
            this.mControlsUiController = controlsUiController;
            controlsUiController.show(this.mControlsView, new Runnable() {
                public final void run() {
                    GlobalActionsDialog.ActionsDialog.this.dismissForControlsActivity();
                }
            });
        }

        private void initializeWalletView() {
            GlobalActionsPanelPlugin.PanelViewController panelViewController = this.mWalletViewController;
            if (panelViewController != null && panelViewController.getPanelContent() != null) {
                int rotation = RotationUtils.getRotation(this.mContext);
                boolean isRotationLocked = RotationPolicy.isRotationLocked(this.mContext);
                if (rotation == 0) {
                    if (!isRotationLocked) {
                        if (this.mResetOrientationData == null) {
                            ResetOrientationData resetOrientationData = new ResetOrientationData();
                            this.mResetOrientationData = resetOrientationData;
                            resetOrientationData.locked = false;
                        }
                        this.mGlobalActionsLayout.post(new Runnable() {
                            public final void run() {
                                GlobalActionsDialog.ActionsDialog.this.lambda$initializeWalletView$1$GlobalActionsDialog$ActionsDialog();
                            }
                        });
                    }
                    setRotationSuggestionsEnabled(false);
                    FrameLayout frameLayout = (FrameLayout) findViewById(C0012R$id.global_actions_wallet);
                    FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(-1, -1);
                    if (!this.mControlsAvailable) {
                        layoutParams.topMargin = this.mContext.getResources().getDimensionPixelSize(C0009R$dimen.global_actions_wallet_top_margin);
                    }
                    View panelContent = this.mWalletViewController.getPanelContent();
                    frameLayout.addView(panelContent, layoutParams);
                    ViewGroup viewGroup = (ViewGroup) findViewById(C0012R$id.global_actions_grid_root);
                    if (viewGroup != null) {
                        panelContent.addOnLayoutChangeListener(new View.OnLayoutChangeListener(viewGroup) {
                            public final /* synthetic */ ViewGroup f$0;

                            {
                                this.f$0 = r1;
                            }

                            public final void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
                                GlobalActionsDialog.ActionsDialog.lambda$initializeWalletView$2(this.f$0, view, i, i2, i3, i4, i5, i6, i7, i8);
                            }
                        });
                    }
                } else if (isRotationLocked) {
                    if (this.mResetOrientationData == null) {
                        ResetOrientationData resetOrientationData2 = new ResetOrientationData();
                        this.mResetOrientationData = resetOrientationData2;
                        resetOrientationData2.locked = true;
                        resetOrientationData2.rotation = rotation;
                    }
                    this.mGlobalActionsLayout.post(new Runnable() {
                        public final void run() {
                            GlobalActionsDialog.ActionsDialog.this.lambda$initializeWalletView$0$GlobalActionsDialog$ActionsDialog();
                        }
                    });
                }
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$initializeWalletView$0 */
        public /* synthetic */ void lambda$initializeWalletView$0$GlobalActionsDialog$ActionsDialog() {
            RotationPolicy.setRotationLockAtAngle(this.mContext, false, 0);
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$initializeWalletView$1 */
        public /* synthetic */ void lambda$initializeWalletView$1$GlobalActionsDialog$ActionsDialog() {
            RotationPolicy.setRotationLockAtAngle(this.mContext, true, 0);
        }

        static /* synthetic */ void lambda$initializeWalletView$2(ViewGroup viewGroup, View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
            int i9 = i8 - i6;
            int i10 = i4 - i2;
            if (i9 > 0 && i9 != i10) {
                TransitionManager.beginDelayedTransition(viewGroup, new AutoTransition().setDuration(250).setOrdering(0));
            }
        }

        private ListPopupWindow createPowerOverflowPopup() {
            GlobalActionsPopupMenu globalActionsPopupMenu = new GlobalActionsPopupMenu(new ContextThemeWrapper(this.mContext, C0019R$style.Control_ListPopupWindow), false);
            globalActionsPopupMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public final void onItemClick(AdapterView adapterView, View view, int i, long j) {
                    GlobalActionsDialog.ActionsDialog.this.lambda$createPowerOverflowPopup$3$GlobalActionsDialog$ActionsDialog(adapterView, view, i, j);
                }
            });
            globalActionsPopupMenu.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                public final boolean onItemLongClick(AdapterView adapterView, View view, int i, long j) {
                    return GlobalActionsDialog.ActionsDialog.this.lambda$createPowerOverflowPopup$4$GlobalActionsDialog$ActionsDialog(adapterView, view, i, j);
                }
            });
            globalActionsPopupMenu.setAnchorView(findViewById(C0012R$id.global_actions_overflow_button));
            globalActionsPopupMenu.setAdapter(this.mOverflowAdapter);
            return globalActionsPopupMenu;
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$createPowerOverflowPopup$3 */
        public /* synthetic */ void lambda$createPowerOverflowPopup$3$GlobalActionsDialog$ActionsDialog(AdapterView adapterView, View view, int i, long j) {
            this.mOverflowAdapter.onClickItem(i);
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$createPowerOverflowPopup$4 */
        public /* synthetic */ boolean lambda$createPowerOverflowPopup$4$GlobalActionsDialog$ActionsDialog(AdapterView adapterView, View view, int i, long j) {
            return this.mOverflowAdapter.onLongClickItem(i);
        }

        public void showPowerOptionsMenu() {
            Dialog create = GlobalActionsPowerDialog.create(this.mContext, this.mPowerOptionsAdapter);
            this.mPowerOptionsDialog = create;
            create.show();
        }

        private void showPowerOverflowMenu() {
            ListPopupWindow createPowerOverflowPopup = createPowerOverflowPopup();
            this.mOverflowPopup = createPowerOverflowPopup;
            createPowerOverflowPopup.show();
        }

        private void initializeLayout() {
            setContentView(C0014R$layout.global_actions_grid_v2);
            fixNavBarClipping();
            this.mControlsView = (ViewGroup) findViewById(C0012R$id.global_actions_controls);
            MultiListLayout multiListLayout = (MultiListLayout) findViewById(C0012R$id.global_actions_view);
            this.mGlobalActionsLayout = multiListLayout;
            multiListLayout.setListViewAccessibilityDelegate(new View.AccessibilityDelegate() {
                public boolean dispatchPopulateAccessibilityEvent(View view, AccessibilityEvent accessibilityEvent) {
                    accessibilityEvent.getText().add(ActionsDialog.this.mContext.getString(17040311));
                    return true;
                }
            });
            this.mGlobalActionsLayout.setRotationListener(new MultiListLayout.RotationListener() {
                public final void onRotate(int i, int i2) {
                    GlobalActionsDialog.ActionsDialog.this.onRotate(i, i2);
                }
            });
            this.mGlobalActionsLayout.setAdapter(this.mAdapter);
            this.mContainer = (ViewGroup) findViewById(C0012R$id.global_actions_container);
            this.mLockMessageContainer = (ViewGroup) requireViewById(C0012R$id.global_actions_lock_message_container);
            this.mLockMessage = (TextView) requireViewById(C0012R$id.global_actions_lock_message);
            View findViewById = findViewById(C0012R$id.global_actions_overflow_button);
            if (findViewById != null) {
                if (this.mOverflowAdapter.getCount() > 0) {
                    findViewById.setOnClickListener(new View.OnClickListener() {
                        public final void onClick(View view) {
                            GlobalActionsDialog.ActionsDialog.this.lambda$initializeLayout$5$GlobalActionsDialog$ActionsDialog(view);
                        }
                    });
                    LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) this.mGlobalActionsLayout.getLayoutParams();
                    layoutParams.setMarginEnd(0);
                    this.mGlobalActionsLayout.setLayoutParams(layoutParams);
                } else {
                    findViewById.setVisibility(8);
                    LinearLayout.LayoutParams layoutParams2 = (LinearLayout.LayoutParams) this.mGlobalActionsLayout.getLayoutParams();
                    layoutParams2.setMarginEnd(this.mContext.getResources().getDimensionPixelSize(C0009R$dimen.global_actions_side_margin));
                    this.mGlobalActionsLayout.setLayoutParams(layoutParams2);
                }
            }
            initializeWalletView();
            if (this.mBackgroundDrawable == null) {
                this.mBackgroundDrawable = new ScrimDrawable();
                this.mScrimAlpha = 1.0f;
            }
            getWindow().setBackgroundDrawable(this.mBackgroundDrawable);
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$initializeLayout$5 */
        public /* synthetic */ void lambda$initializeLayout$5$GlobalActionsDialog$ActionsDialog(View view) {
            showPowerOverflowMenu();
        }

        private void fixNavBarClipping() {
            ViewGroup viewGroup = (ViewGroup) findViewById(16908290);
            viewGroup.setClipChildren(false);
            viewGroup.setClipToPadding(false);
            ViewGroup viewGroup2 = (ViewGroup) viewGroup.getParent();
            viewGroup2.setClipChildren(false);
            viewGroup2.setClipToPadding(false);
        }

        /* access modifiers changed from: protected */
        public void onStart() {
            super.setCanceledOnTouchOutside(true);
            super.onStart();
            this.mGlobalActionsLayout.updateList();
            if (this.mBackgroundDrawable instanceof ScrimDrawable) {
                this.mColorExtractor.addOnColorsChangedListener(this);
                updateColors(this.mColorExtractor.getNeutralColors(), false);
            }
        }

        private void updateColors(ColorExtractor.GradientColors gradientColors, boolean z) {
            ScrimDrawable scrimDrawable = this.mBackgroundDrawable;
            if (scrimDrawable instanceof ScrimDrawable) {
                scrimDrawable.setColor(-16777216, z);
                View decorView = getWindow().getDecorView();
                if (gradientColors.supportsDarkText()) {
                    decorView.setSystemUiVisibility(8208);
                } else {
                    decorView.setSystemUiVisibility(0);
                }
            }
        }

        /* access modifiers changed from: protected */
        public void onStop() {
            super.onStop();
            this.mColorExtractor.removeOnColorsChangedListener(this);
        }

        public void show() {
            super.show();
            this.mShowing = true;
            this.mHadTopUi = this.mNotificationShadeWindowController.getForceHasTopUi();
            this.mNotificationShadeWindowController.setForceHasTopUi(true);
            SysUiState sysUiState = this.mSysUiState;
            sysUiState.setFlag(32768, true);
            sysUiState.commitUpdate(this.mContext.getDisplayId());
            ViewGroup viewGroup = (ViewGroup) this.mGlobalActionsLayout.getRootView();
            viewGroup.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener(viewGroup) {
                public final /* synthetic */ ViewGroup f$0;

                {
                    this.f$0 = r1;
                }

                public final WindowInsets onApplyWindowInsets(View view, WindowInsets windowInsets) {
                    return this.f$0.setPadding(windowInsets.getStableInsetLeft(), windowInsets.getStableInsetTop(), windowInsets.getStableInsetRight(), windowInsets.getStableInsetBottom());
                }
            });
            ControlsUiController controlsUiController = this.mControlsUiController;
            if (controlsUiController != null) {
                controlsUiController.show(this.mControlsView, new Runnable() {
                    public final void run() {
                        GlobalActionsDialog.ActionsDialog.this.dismissForControlsActivity();
                    }
                });
            }
            this.mBackgroundDrawable.setAlpha(0);
            float animationOffsetX = this.mGlobalActionsLayout.getAnimationOffsetX();
            ObjectAnimator ofFloat = ObjectAnimator.ofFloat(this.mContainer, "alpha", new float[]{0.0f, 1.0f});
            ofFloat.setInterpolator(Interpolators.LINEAR_OUT_SLOW_IN);
            ofFloat.setDuration(183);
            ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                    GlobalActionsDialog.ActionsDialog.this.lambda$show$7$GlobalActionsDialog$ActionsDialog(valueAnimator);
                }
            });
            ObjectAnimator ofFloat2 = ObjectAnimator.ofFloat(this.mContainer, "translationX", new float[]{animationOffsetX, 0.0f});
            ofFloat2.setInterpolator(Interpolators.LINEAR_OUT_SLOW_IN);
            ofFloat2.setDuration(350);
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(new Animator[]{ofFloat, ofFloat2});
            animatorSet.start();
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$show$7 */
        public /* synthetic */ void lambda$show$7$GlobalActionsDialog$ActionsDialog(ValueAnimator valueAnimator) {
            float animatedFraction = valueAnimator.getAnimatedFraction();
            this.mBackgroundDrawable.setAlpha((int) (this.mScrimAlpha * animatedFraction * 255.0f));
            this.mDepthController.updateGlobalDialogVisibility(animatedFraction, this.mGlobalActionsLayout);
        }

        public void dismiss() {
            dismissWithAnimation(new Runnable() {
                public final void run() {
                    GlobalActionsDialog.ActionsDialog.this.lambda$dismiss$9$GlobalActionsDialog$ActionsDialog();
                }
            });
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$dismiss$9 */
        public /* synthetic */ void lambda$dismiss$9$GlobalActionsDialog$ActionsDialog() {
            this.mContainer.setTranslationX(0.0f);
            ObjectAnimator ofFloat = ObjectAnimator.ofFloat(this.mContainer, "alpha", new float[]{1.0f, 0.0f});
            ofFloat.setInterpolator(Interpolators.FAST_OUT_LINEAR_IN);
            ofFloat.setDuration(233);
            ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                    GlobalActionsDialog.ActionsDialog.this.lambda$dismiss$8$GlobalActionsDialog$ActionsDialog(valueAnimator);
                }
            });
            float animationOffsetX = this.mGlobalActionsLayout.getAnimationOffsetX();
            ObjectAnimator ofFloat2 = ObjectAnimator.ofFloat(this.mContainer, "translationX", new float[]{0.0f, animationOffsetX});
            ofFloat2.setInterpolator(Interpolators.FAST_OUT_LINEAR_IN);
            ofFloat2.setDuration(350);
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(new Animator[]{ofFloat, ofFloat2});
            animatorSet.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animator) {
                    ActionsDialog.this.completeDismiss();
                }
            });
            animatorSet.start();
            dismissOverflow(false);
            dismissPowerOptions(false);
            ControlsUiController controlsUiController = this.mControlsUiController;
            if (controlsUiController != null) {
                controlsUiController.closeDialogs(false);
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$dismiss$8 */
        public /* synthetic */ void lambda$dismiss$8$GlobalActionsDialog$ActionsDialog(ValueAnimator valueAnimator) {
            float animatedFraction = 1.0f - valueAnimator.getAnimatedFraction();
            this.mBackgroundDrawable.setAlpha((int) (this.mScrimAlpha * animatedFraction * 255.0f));
            this.mDepthController.updateGlobalDialogVisibility(animatedFraction, this.mGlobalActionsLayout);
        }

        /* access modifiers changed from: private */
        public void dismissForControlsActivity() {
            dismissWithAnimation(new Runnable() {
                public final void run() {
                    GlobalActionsDialog.ActionsDialog.this.lambda$dismissForControlsActivity$10$GlobalActionsDialog$ActionsDialog();
                }
            });
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$dismissForControlsActivity$10 */
        public /* synthetic */ void lambda$dismissForControlsActivity$10$GlobalActionsDialog$ActionsDialog() {
            ControlsAnimations.exitAnimation((ViewGroup) this.mGlobalActionsLayout.getParent(), new Runnable() {
                public final void run() {
                    GlobalActionsDialog.ActionsDialog.this.completeDismiss();
                }
            }).start();
        }

        /* access modifiers changed from: package-private */
        public void dismissWithAnimation(Runnable runnable) {
            if (this.mShowing) {
                this.mShowing = false;
                runnable.run();
            }
        }

        /* access modifiers changed from: private */
        public void completeDismiss() {
            this.mShowing = false;
            resetOrientation();
            dismissWallet();
            dismissOverflow(true);
            dismissPowerOptions(true);
            ControlsUiController controlsUiController = this.mControlsUiController;
            if (controlsUiController != null) {
                controlsUiController.hide();
            }
            this.mNotificationShadeWindowController.setForceHasTopUi(this.mHadTopUi);
            this.mDepthController.updateGlobalDialogVisibility(0.0f, (View) null);
            SysUiState sysUiState = this.mSysUiState;
            sysUiState.setFlag(32768, false);
            sysUiState.commitUpdate(this.mContext.getDisplayId());
            super.dismiss();
        }

        private void dismissWallet() {
            GlobalActionsPanelPlugin.PanelViewController panelViewController = this.mWalletViewController;
            if (panelViewController != null) {
                panelViewController.onDismissed();
            }
        }

        private void dismissOverflow(boolean z) {
            ListPopupWindow listPopupWindow = this.mOverflowPopup;
            if (listPopupWindow == null) {
                return;
            }
            if (z) {
                listPopupWindow.dismissImmediate();
            } else {
                listPopupWindow.dismiss();
            }
        }

        private void dismissPowerOptions(boolean z) {
            Dialog dialog = this.mPowerOptionsDialog;
            if (dialog == null) {
                return;
            }
            if (z) {
                dialog.dismiss();
            } else {
                dialog.dismiss();
            }
        }

        private void setRotationSuggestionsEnabled(boolean z) {
            try {
                this.mStatusBarService.disable2ForUser(z ? 0 : 16, this.mToken, this.mContext.getPackageName(), Binder.getCallingUserHandle().getIdentifier());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }

        private void resetOrientation() {
            ResetOrientationData resetOrientationData = this.mResetOrientationData;
            if (resetOrientationData != null) {
                RotationPolicy.setRotationLockAtAngle(this.mContext, resetOrientationData.locked, resetOrientationData.rotation);
            }
            setRotationSuggestionsEnabled(true);
        }

        public void onColorsChanged(ColorExtractor colorExtractor, int i) {
            if (this.mKeyguardShowing) {
                if ((i & 2) != 0) {
                    updateColors(colorExtractor.getColors(2), true);
                }
            } else if ((i & 1) != 0) {
                updateColors(colorExtractor.getColors(1), true);
            }
        }

        public void refreshDialog() {
            dismissWallet();
            dismissOverflow(true);
            dismissPowerOptions(true);
            ControlsUiController controlsUiController = this.mControlsUiController;
            if (controlsUiController != null) {
                controlsUiController.hide();
            }
            initializeLayout();
            this.mGlobalActionsLayout.updateList();
            ControlsUiController controlsUiController2 = this.mControlsUiController;
            if (controlsUiController2 != null) {
                controlsUiController2.show(this.mControlsView, new Runnable() {
                    public final void run() {
                        GlobalActionsDialog.ActionsDialog.this.dismissForControlsActivity();
                    }
                });
            }
        }

        public void onRotate(int i, int i2) {
            if (this.mShowing) {
                this.mOnRotateCallback.run();
                refreshDialog();
            }
        }

        /* access modifiers changed from: package-private */
        public void hideLockMessage() {
            if (this.mLockMessageContainer.getVisibility() == 0) {
                this.mLockMessageContainer.animate().alpha(0.0f).setDuration(150).setListener(new AnimatorListenerAdapter() {
                    public void onAnimationEnd(Animator animator) {
                        ActionsDialog.this.mLockMessageContainer.setVisibility(8);
                    }
                }).start();
            }
        }

        /* access modifiers changed from: package-private */
        public void showLockMessage() {
            Drawable drawable = this.mContext.getDrawable(17302479);
            drawable.setTint(this.mContext.getColor(C0008R$color.control_primary_text));
            this.mLockMessage.setCompoundDrawablesWithIntrinsicBounds((Drawable) null, drawable, (Drawable) null, (Drawable) null);
            this.mLockMessageContainer.setVisibility(0);
        }

        private static class ResetOrientationData {
            public boolean locked;
            public int rotation;

            private ResetOrientationData() {
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean shouldShowControls() {
        boolean z = this.mShowLockScreenCardsAndControls && this.mLockPatternUtils.getStrongAuthForUser(getCurrentUser().id) != 1;
        if (!controlsAvailable()) {
            return false;
        }
        if (this.mKeyguardStateController.isUnlocked() || z) {
            return true;
        }
        return false;
    }

    private boolean controlsAvailable() {
        return this.mDeviceProvisioned && this.mControlsUiControllerOptional.isPresent() && this.mControlsUiControllerOptional.get().getAvailable() && !this.mControlsServiceInfos.isEmpty();
    }

    private boolean walletViewAvailable() {
        GlobalActionsPanelPlugin.PanelViewController walletViewController = getWalletViewController();
        return (walletViewController == null || walletViewController.getPanelContent() == null) ? false : true;
    }

    private boolean shouldShowLockMessage() {
        boolean z = this.mLockPatternUtils.getStrongAuthForUser(getCurrentUser().id) == 1;
        if (this.mKeyguardStateController.isUnlocked()) {
            return false;
        }
        if (this.mShowLockScreenCardsAndControls && !z) {
            return false;
        }
        if (controlsAvailable() || walletViewAvailable()) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    public void onPowerMenuLockScreenSettingsChanged() {
        boolean z = false;
        if (Settings.Secure.getInt(this.mContentResolver, "power_menu_locked_show_content", 0) != 0) {
            z = true;
        }
        this.mShowLockScreenCardsAndControls = z;
    }
}
