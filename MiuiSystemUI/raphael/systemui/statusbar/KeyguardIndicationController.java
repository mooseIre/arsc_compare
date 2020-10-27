package com.android.systemui.statusbar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.admin.DevicePolicyManager;
import android.app.admin.DevicePolicyManagerCompat;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.os.ServiceManager;
import android.os.UserHandleCompat;
import android.os.UserManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.app.IBatteryStats;
import com.android.keyguard.Ease$Cubic;
import com.android.keyguard.Ease$Quint;
import com.android.keyguard.EmergencyButton;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.keyguard.MiuiKeyguardFingerprintUtils$FingerprintIdentificationState;
import com.android.keyguard.MiuiKeyguardUtils;
import com.android.keyguard.charge.BatteryStatus;
import com.android.keyguard.charge.ChargeUtils;
import com.android.keyguard.charge.MiuiChargeController;
import com.android.keyguard.faceunlock.FaceUnlockCallback;
import com.android.keyguard.faceunlock.FaceUnlockManager;
import com.android.keyguard.faceunlock.MiuiFaceUnlockUtils;
import com.android.keyguard.utils.DeviceLevelUtils;
import com.android.keyguard.utils.PhoneUtils;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.phone.KeyguardBottomAreaView;
import com.android.systemui.statusbar.phone.KeyguardIndicationTextView;
import com.android.systemui.statusbar.phone.NotificationPanelView;
import com.android.systemui.statusbar.phone.StatusBarKeyguardViewManager;
import com.android.systemui.statusbar.policy.UserInfoController;
import com.android.systemui.util.wakelock.SettableWakeLock;
import com.android.systemui.util.wakelock.WakeLock;
import com.android.systemui.util.wakelock.WakeLockHelper;
import miui.util.FeatureParser;

public class KeyguardIndicationController {
    private final IBatteryStats mBatteryInfo;
    /* access modifiers changed from: private */
    public int mBatteryLevel;
    private ObjectAnimator mBottomButtonClickAnimator;
    /* access modifiers changed from: private */
    public AsyncTask<?, ?, ?> mChargeAsyncTask;
    /* access modifiers changed from: private */
    public int mChargeClickCount;
    /* access modifiers changed from: private */
    public long mChargeTextClickTime;
    /* access modifiers changed from: private */
    public boolean mChargeUIEntering;
    /* access modifiers changed from: private */
    public int mChargingSpeed;
    /* access modifiers changed from: private */
    public final Context mContext;
    private boolean mDarkMode;
    private final DevicePolicyManager mDevicePolicyManager;
    private final KeyguardIndicationTextView mDisclosure;
    /* access modifiers changed from: private */
    public boolean mDozing;
    private final FaceUnlockCallback mFaceUnlockCallback;
    private final int mFastThreshold;
    /* access modifiers changed from: private */
    public final Handler mHandler;
    private final ViewGroup mIndicationArea;
    /* access modifiers changed from: private */
    public boolean mIsOCTSingleClicking;
    private final KeyguardBottomAreaView mKeyguardBottomAreaView;
    /* access modifiers changed from: private */
    public boolean mLockScreenMagazinePreViewVisibility;
    /* access modifiers changed from: private */
    public String mMessageToShowOnScreenOn;
    /* access modifiers changed from: private */
    public MiuiChargeController mMiuiChargeController;
    private final NotificationPanelView mNotificationPanelView;
    /* access modifiers changed from: private */
    public boolean mPowerCharged;
    /* access modifiers changed from: private */
    public boolean mPowerPluggedIn;
    /* access modifiers changed from: private */
    public final Resources mResources;
    private String mRestingIndication;
    /* access modifiers changed from: private */
    public boolean mShowChargeAnimation;
    /* access modifiers changed from: private */
    public boolean mSignalAvailable;
    private final int mSlowThreshold;
    /* access modifiers changed from: private */
    public StatusBarKeyguardViewManager mStatusBarKeyguardViewManager;
    private KeyguardUpdateMonitor.StrongAuthTracker mStrongAuthTracker;
    /* access modifiers changed from: private */
    public final KeyguardIndicationTextView mTextView;
    private View.OnClickListener mTextViewOnClickListener;
    private final BroadcastReceiver mTickReceiver;
    private String mTransientIndication;
    private int mTransientTextColor;
    /* access modifiers changed from: private */
    public final ImageView mUpArrow;
    /* access modifiers changed from: private */
    public boolean mUpArrowEntering;
    /* access modifiers changed from: private */
    public String mUpArrowIndication;
    /* access modifiers changed from: private */
    public KeyguardUpdateMonitor mUpdateMonitor;
    private KeyguardUpdateMonitorCallback mUpdateMonitorCallback;
    private final UserManager mUserManager;
    private ViewConfiguration mViewConfiguration;
    /* access modifiers changed from: private */
    public boolean mVisible;
    private final SettableWakeLock mWakeLock;

    public void setUserInfoController(UserInfoController userInfoController) {
    }

    static /* synthetic */ int access$208(KeyguardIndicationController keyguardIndicationController) {
        int i = keyguardIndicationController.mChargeClickCount;
        keyguardIndicationController.mChargeClickCount = i + 1;
        return i;
    }

    public KeyguardIndicationController(Context context, NotificationPanelView notificationPanelView) {
        this(context, WakeLockHelper.createPartial(context, "Doze:KeyguardIndication"), notificationPanelView);
        registerCallbacks(this.mUpdateMonitor);
        FaceUnlockManager.getInstance().registerFaceUnlockCallback(this.mFaceUnlockCallback);
    }

    @VisibleForTesting
    KeyguardIndicationController(Context context, WakeLock wakeLock, NotificationPanelView notificationPanelView) {
        this.mChargeClickCount = 0;
        this.mTextViewOnClickListener = new View.OnClickListener() {
            public void onClick(View view) {
                Log.i("KeyguardIndication", "onClick: mPowerPluggedIn " + KeyguardIndicationController.this.mPowerPluggedIn + ";mLockScreenMagazinePreViewVisibility=" + KeyguardIndicationController.this.mLockScreenMagazinePreViewVisibility);
                if (KeyguardIndicationController.this.mPowerPluggedIn && !KeyguardIndicationController.this.mLockScreenMagazinePreViewVisibility) {
                    if (KeyguardIndicationController.this.mChargeClickCount == 0) {
                        long unused = KeyguardIndicationController.this.mChargeTextClickTime = System.currentTimeMillis();
                    }
                    KeyguardIndicationController.access$208(KeyguardIndicationController.this);
                    Log.i("KeyguardIndication", "onClick: mChargeClickCount " + KeyguardIndicationController.this.mChargeClickCount + ";time=" + (System.currentTimeMillis() - KeyguardIndicationController.this.mChargeTextClickTime));
                    if (KeyguardIndicationController.this.mChargeClickCount >= 2) {
                        if (System.currentTimeMillis() - KeyguardIndicationController.this.mChargeTextClickTime > 150 && System.currentTimeMillis() - KeyguardIndicationController.this.mChargeTextClickTime < 500) {
                            int unused2 = KeyguardIndicationController.this.mChargeClickCount = 0;
                            long unused3 = KeyguardIndicationController.this.mChargeTextClickTime = System.currentTimeMillis();
                            KeyguardIndicationController.this.mMiuiChargeController.checkBatteryStatus(true);
                            KeyguardIndicationController.this.mTextView.setVisibility(4);
                        } else if (System.currentTimeMillis() - KeyguardIndicationController.this.mChargeTextClickTime > 500) {
                            int unused4 = KeyguardIndicationController.this.mChargeClickCount = 1;
                            long unused5 = KeyguardIndicationController.this.mChargeTextClickTime = System.currentTimeMillis();
                        } else {
                            int unused6 = KeyguardIndicationController.this.mChargeClickCount = 0;
                        }
                    }
                }
                if (MiuiKeyguardUtils.IS_OPERATOR_CUSTOMIZATION_TEST && KeyguardIndicationController.this.mSignalAvailable && !KeyguardIndicationController.this.mIsOCTSingleClicking && !KeyguardIndicationController.this.mPowerPluggedIn) {
                    KeyguardIndicationController.this.takeEmergencyCallAction();
                }
            }
        };
        this.mTickReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                KeyguardIndicationController.this.mHandler.sendEmptyMessage(3);
            }
        };
        this.mHandler = new Handler() {
            public void handleMessage(Message message) {
                switch (message.what) {
                    case 1:
                        KeyguardIndicationController.this.hideTransientIndication();
                        return;
                    case 2:
                        KeyguardIndicationController.this.hideTransientIndication();
                        return;
                    case 3:
                        KeyguardIndicationController.this.handleTickReceived();
                        return;
                    case 4:
                        KeyguardIndicationController.this.handleExitArrowAndTextAnimation();
                        return;
                    case 5:
                        KeyguardIndicationController.this.updateChargingInfoIndication();
                        return;
                    case 6:
                        KeyguardIndicationController.this.handleShowOCTEmergency();
                        return;
                    default:
                        return;
                }
            }
        };
        this.mFaceUnlockCallback = new FaceUnlockCallback() {
            public void onFaceAuthHelp(int i) {
                KeyguardIndicationController keyguardIndicationController = KeyguardIndicationController.this;
                keyguardIndicationController.handleFaceUnlockBouncerMessage(MiuiFaceUnlockUtils.getFaceHelpInfo(keyguardIndicationController.mContext, i));
            }

            public void onFaceAuthFailed() {
                KeyguardIndicationController.this.handleFaceUnlockBouncerMessage("");
            }

            public void onFaceAuthTimeOut(boolean z) {
                KeyguardIndicationController.this.handleFaceUnlockBouncerMessage("");
            }

            public void onFaceAuthLocked() {
                KeyguardIndicationController.this.handleFaceUnlockBouncerMessage("");
            }
        };
        this.mContext = context;
        this.mResources = this.mContext.getResources();
        this.mWakeLock = new SettableWakeLock(wakeLock);
        this.mNotificationPanelView = notificationPanelView;
        this.mKeyguardBottomAreaView = (KeyguardBottomAreaView) notificationPanelView.findViewById(R.id.keyguard_bottom_area);
        this.mIndicationArea = (LinearLayout) this.mKeyguardBottomAreaView.findViewById(R.id.keyguard_indication_area);
        this.mTextView = (KeyguardIndicationTextView) this.mKeyguardBottomAreaView.findViewById(R.id.keyguard_indication_text);
        this.mTextView.setOnClickListener(this.mTextViewOnClickListener);
        this.mDisclosure = (KeyguardIndicationTextView) this.mKeyguardBottomAreaView.findViewById(R.id.keyguard_indication_enterprise_disclosure);
        this.mUpArrow = (ImageView) this.mKeyguardBottomAreaView.findViewById(R.id.keyguard_up_arrow);
        this.mSlowThreshold = this.mResources.getInteger(R.integer.config_chargingSlowlyThreshold);
        this.mFastThreshold = this.mResources.getInteger(R.integer.config_chargingFastThreshold);
        this.mUserManager = (UserManager) context.getSystemService(UserManager.class);
        this.mBatteryInfo = IBatteryStats.Stub.asInterface(ServiceManager.getService("batterystats"));
        this.mDevicePolicyManager = (DevicePolicyManager) context.getSystemService("device_policy");
        this.mUpdateMonitor = KeyguardUpdateMonitor.getInstance(context);
        this.mStrongAuthTracker = this.mUpdateMonitor.getStrongAuthTracker();
        this.mViewConfiguration = ViewConfiguration.get(context);
        updateDisclosure();
        MiuiKeyguardUtils.setViewTouchDelegate(this.mTextView, 50);
    }

    /* access modifiers changed from: private */
    public void takeEmergencyCallAction() {
        PhoneUtils.takeEmergencyCallAction(this.mContext, (EmergencyButton.EmergencyButtonCallback) null);
    }

    private void registerCallbacks(KeyguardUpdateMonitor keyguardUpdateMonitor) {
        keyguardUpdateMonitor.registerCallback(getKeyguardCallback());
        this.mContext.registerReceiverAsUser(this.mTickReceiver, UserHandleCompat.SYSTEM, new IntentFilter("android.intent.action.TIME_TICK"), (String) null, (Handler) Dependency.get(Dependency.TIME_TICK_HANDLER));
    }

    /* access modifiers changed from: protected */
    public KeyguardUpdateMonitorCallback getKeyguardCallback() {
        if (this.mUpdateMonitorCallback == null) {
            this.mUpdateMonitorCallback = new BaseKeyguardCallback();
        }
        return this.mUpdateMonitorCallback;
    }

    /* access modifiers changed from: private */
    public void updateDisclosure() {
        DevicePolicyManager devicePolicyManager = this.mDevicePolicyManager;
        if (devicePolicyManager != null) {
            if (this.mDozing || !DevicePolicyManagerCompat.isDeviceManaged(devicePolicyManager)) {
                this.mDisclosure.setVisibility(8);
                return;
            }
            CharSequence deviceOwnerOrganizationName = DevicePolicyManagerCompat.getDeviceOwnerOrganizationName(this.mDevicePolicyManager);
            if (deviceOwnerOrganizationName != null) {
                this.mDisclosure.switchIndication((CharSequence) this.mResources.getString(R.string.do_disclosure_with_name, new Object[]{deviceOwnerOrganizationName}));
            } else {
                this.mDisclosure.switchIndication((int) R.string.do_disclosure_generic);
            }
            this.mDisclosure.setVisibility(0);
        }
    }

    public void setVisible(boolean z) {
        this.mVisible = z;
        this.mIndicationArea.setVisibility(z ? 0 : 8);
        if (z) {
            hideTransientIndication();
            updateIndication();
        }
    }

    public void hideTransientIndicationDelayed(long j) {
        Handler handler = this.mHandler;
        handler.sendMessageDelayed(handler.obtainMessage(1), j);
    }

    public void showTransientIndication(int i) {
        showTransientIndication(this.mResources.getString(i));
    }

    /* access modifiers changed from: private */
    public void showTransientIndication(String str) {
        showTransientIndication(str, getTextColor());
    }

    /* access modifiers changed from: private */
    public void showTransientIndication(String str, int i) {
        this.mTransientIndication = str;
        this.mTransientTextColor = i;
        this.mHandler.removeMessages(1);
        if (this.mDozing && !TextUtils.isEmpty(this.mTransientIndication)) {
            this.mWakeLock.setAcquired(true);
            hideTransientIndicationDelayed(5000);
        }
        updateIndication();
    }

    public void hideTransientIndication() {
        if (this.mTransientIndication != null) {
            this.mTransientIndication = null;
            this.mHandler.removeMessages(1);
            updateIndication();
        }
    }

    /* access modifiers changed from: private */
    public void updateIndication() {
        if (TextUtils.isEmpty(this.mTransientIndication)) {
            this.mWakeLock.setAcquired(false);
        }
        Log.i("KeyguardIndication", "updateIndication: mVisible " + this.mVisible + " mDozing " + this.mDozing + " mTransientIndication " + this.mTransientIndication + " mPowerPluggedIn " + this.mPowerPluggedIn + " mUpArrowIndication " + this.mUpArrowIndication);
        if (!this.mVisible) {
            return;
        }
        if (this.mDozing) {
            if (!TextUtils.isEmpty(this.mTransientIndication)) {
                this.mTextView.switchIndication((CharSequence) this.mTransientIndication);
                this.mTextView.setTextColor(this.mTransientTextColor);
                return;
            }
            this.mTextView.switchIndication((CharSequence) null);
        } else if (!TextUtils.isEmpty(this.mTransientIndication)) {
            this.mTextView.switchIndication((CharSequence) this.mTransientIndication);
            this.mTextView.setTextColor(this.mTransientTextColor);
        } else if (this.mPowerPluggedIn && !this.mChargeUIEntering) {
            this.mHandler.removeMessages(5);
            this.mHandler.sendEmptyMessageDelayed(5, 500);
        } else if (MiuiKeyguardUtils.IS_OPERATOR_CUSTOMIZATION_TEST && this.mSignalAvailable && !this.mIsOCTSingleClicking) {
            this.mTextView.switchIndication((int) R.string.emergency_call_string);
            this.mTextView.setTextColor(getTextColor());
        } else if (!TextUtils.isEmpty(this.mUpArrowIndication) && !this.mChargeUIEntering) {
            this.mTextView.switchIndication((CharSequence) this.mUpArrowEntering ? "" : this.mUpArrowIndication);
            this.mTextView.setTextColor(getTextColor());
            this.mUpArrow.setImageResource(this.mDarkMode ? R.drawable.miui_default_lock_screen_up_arrow_dark : R.drawable.miui_default_lock_screen_up_arrow);
        } else if (!this.mChargeUIEntering) {
            this.mTextView.switchIndication((CharSequence) this.mRestingIndication);
            this.mTextView.setTextColor(getTextColor());
        }
    }

    /* access modifiers changed from: private */
    public void updateChargingInfoIndication() {
        if (this.mChargeAsyncTask == null && this.mPowerPluggedIn) {
            this.mChargeAsyncTask = new AsyncTask<Void, Void, String>() {
                /* access modifiers changed from: protected */
                public String doInBackground(Void... voidArr) {
                    return ChargeUtils.getChargingHintText(KeyguardIndicationController.this.mContext, KeyguardIndicationController.this.mPowerPluggedIn, KeyguardIndicationController.this.mBatteryLevel);
                }

                /* access modifiers changed from: protected */
                public void onPostExecute(String str) {
                    super.onPostExecute(str);
                    if (KeyguardIndicationController.this.mPowerPluggedIn) {
                        if (KeyguardIndicationController.this.mShowChargeAnimation) {
                            KeyguardIndicationController.this.mTextView.setVisibility(4);
                        } else {
                            KeyguardIndicationController.this.mTextView.switchIndication((CharSequence) str);
                            KeyguardIndicationController.this.mTextView.setTextColor(KeyguardIndicationController.this.getTextColor());
                        }
                    }
                    AsyncTask unused = KeyguardIndicationController.this.mChargeAsyncTask = null;
                }

                /* access modifiers changed from: protected */
                public void onCancelled() {
                    super.onCancelled();
                    AsyncTask unused = KeyguardIndicationController.this.mChargeAsyncTask = null;
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
        }
    }

    public void setStatusBarKeyguardViewManager(StatusBarKeyguardViewManager statusBarKeyguardViewManager) {
        this.mStatusBarKeyguardViewManager = statusBarKeyguardViewManager;
    }

    public void onTouchEvent(MotionEvent motionEvent, int i, float f, float f2) {
        if (motionEvent.getAction() == 0) {
            clearUpArrowAnimation();
        } else if (motionEvent.getAction() == 1 && i == 1 && this.mNotificationPanelView.isQSFullyCollapsed()) {
            float scaledTouchSlop = (float) this.mViewConfiguration.getScaledTouchSlop();
            if (Math.abs(f - motionEvent.getRawX()) < scaledTouchSlop && Math.abs(f2 - motionEvent.getRawY()) < scaledTouchSlop) {
                handleSingleClickEvent();
            }
        }
    }

    private void handleSingleClickEvent() {
        if (MiuiKeyguardUtils.IS_OPERATOR_CUSTOMIZATION_TEST && !this.mPowerPluggedIn) {
            this.mIsOCTSingleClicking = true;
            updateIndication();
            this.mHandler.removeMessages(6);
            this.mHandler.sendEmptyMessageDelayed(6, 2000);
        }
    }

    /* access modifiers changed from: private */
    public void handleShowOCTEmergency() {
        this.mIsOCTSingleClicking = false;
        updateIndication();
    }

    /* access modifiers changed from: private */
    public void clearUpArrowAnimation() {
        this.mHandler.removeMessages(4);
        this.mUpArrow.clearAnimation();
        this.mTextView.clearAnimation();
        this.mUpArrow.setVisibility(4);
        updateIndication();
    }

    /* access modifiers changed from: private */
    public void handleTickReceived() {
        if (this.mVisible) {
            updateIndication();
        }
    }

    /* access modifiers changed from: private */
    public void handleExitArrowAndTextAnimation() {
        Animation loadAnimation = AnimationUtils.loadAnimation(this.mContext, 17432577);
        Animation loadAnimation2 = AnimationUtils.loadAnimation(this.mContext, 17432576);
        TranslateAnimation translateAnimation = new TranslateAnimation(1, 0.0f, 1, 0.0f, 1, 0.0f, 1, -2.0f);
        TranslateAnimation translateAnimation2 = new TranslateAnimation(1, 0.0f, 1, 0.0f, 1, 2.0f, 1, 0.0f);
        AnimationSet animationSet = new AnimationSet(true);
        AnimationSet animationSet2 = new AnimationSet(true);
        animationSet.addAnimation(loadAnimation);
        animationSet.addAnimation(translateAnimation);
        long j = (long) 500;
        animationSet.setDuration(j);
        animationSet2.addAnimation(loadAnimation2);
        animationSet2.addAnimation(translateAnimation2);
        animationSet2.setDuration(j);
        animationSet2.setStartOffset(100);
        animationSet.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationRepeat(Animation animation) {
            }

            public void onAnimationStart(Animation animation) {
            }

            public void onAnimationEnd(Animation animation) {
                KeyguardIndicationController.this.mUpArrow.setVisibility(4);
            }
        });
        animationSet2.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationRepeat(Animation animation) {
            }

            public void onAnimationStart(Animation animation) {
                boolean unused = KeyguardIndicationController.this.mUpArrowEntering = false;
                KeyguardIndicationController.this.updateIndication();
            }

            public void onAnimationEnd(Animation animation) {
                KeyguardIndicationController.this.updateIndication();
                KeyguardIndicationController.this.mTextView.setVisibility(0);
            }
        });
        this.mUpArrow.startAnimation(animationSet);
        this.mTextView.startAnimation(animationSet2);
    }

    public void setDozing(boolean z) {
        if (this.mDozing != z) {
            this.mDozing = z;
            updateIndication();
            updateDisclosure();
        }
    }

    /* access modifiers changed from: private */
    public void handleFaceUnlockBouncerMessage(String str) {
        String str2;
        if (this.mUpdateMonitor.isUnlockWithFingerprintPossible(KeyguardUpdateMonitor.getCurrentUser()) && this.mUpdateMonitor.shouldListenForFingerprint() && !this.mUpdateMonitor.isFingerprintTemporarilyLockout()) {
            str2 = this.mResources.getString(R.string.face_unlock_passwork_and_fingerprint);
        } else {
            str2 = this.mResources.getString(R.string.input_password_hint_text);
        }
        if (!FaceUnlockManager.getInstance().isFaceUnlockStarted()) {
            if (FaceUnlockManager.getInstance().shouldShowFaceUnlockRetryMessageInBouncer()) {
                str = this.mResources.getString(R.string.face_unlock_fail_retry_global);
            } else if (FaceUnlockManager.getInstance().isFaceLocked()) {
                str = this.mResources.getString(R.string.face_unlock_fail);
            }
        }
        this.mStatusBarKeyguardViewManager.showBouncerMessage(str2, str, this.mResources.getColor(R.color.secure_keyguard_bouncer_message_content_text_color));
    }

    protected class BaseKeyguardCallback extends KeyguardUpdateMonitorCallback {
        private final int FINGERPRINT_ERROR_LOCKOUT_PERMANENT_FOR_O = 9;
        private int mFingerprintAuthUserId;
        private int mFingerprintErrorMsgId;
        private MiuiKeyguardFingerprintUtils$FingerprintIdentificationState mFpiState;
        private MiuiKeyguardFingerprintUtils$FingerprintIdentificationState mLastFpiState;

        protected BaseKeyguardCallback() {
        }

        public void onRefreshBatteryInfo(BatteryStatus batteryStatus) {
            if (!FeatureParser.getBoolean("is_pad", false)) {
                boolean access$000 = KeyguardIndicationController.this.mPowerPluggedIn;
                boolean unused = KeyguardIndicationController.this.mPowerPluggedIn = batteryStatus.isPluggedIn() && batteryStatus.isChargingOrFull();
                boolean unused2 = KeyguardIndicationController.this.mPowerCharged = batteryStatus.isCharged();
                int unused3 = KeyguardIndicationController.this.mChargingSpeed = batteryStatus.getChargeSpeed();
                int unused4 = KeyguardIndicationController.this.mBatteryLevel = batteryStatus.getLevel();
                if (KeyguardIndicationController.this.mPowerPluggedIn && !access$000) {
                    KeyguardIndicationController.this.clearUpArrowAnimation();
                }
                if (!KeyguardIndicationController.this.mPowerPluggedIn && access$000) {
                    KeyguardIndicationController.this.mTextView.clearAnimation();
                    KeyguardIndicationController keyguardIndicationController = KeyguardIndicationController.this;
                    String unused5 = keyguardIndicationController.mUpArrowIndication = keyguardIndicationController.mResources.getString(R.string.default_lockscreen_unlock_hint_text);
                    boolean unused6 = KeyguardIndicationController.this.mChargeUIEntering = false;
                    boolean unused7 = KeyguardIndicationController.this.mUpArrowEntering = false;
                    KeyguardIndicationController.this.hideTransientIndication();
                }
                KeyguardIndicationController.this.updateIndication();
                KeyguardIndicationController.this.mTextView.setPowerPluggedIn(KeyguardIndicationController.this.mPowerPluggedIn);
                if (!KeyguardIndicationController.this.mDozing) {
                    return;
                }
                if (!access$000 && KeyguardIndicationController.this.mPowerPluggedIn) {
                    showChargingTransientIndication();
                } else if (access$000 && !KeyguardIndicationController.this.mPowerPluggedIn) {
                    KeyguardIndicationController.this.hideTransientIndication();
                }
            }
        }

        private void showChargingTransientIndication() {
            new AsyncTask<Void, Void, String>() {
                /* access modifiers changed from: protected */
                public String doInBackground(Void... voidArr) {
                    return ChargeUtils.getChargingHintText(KeyguardIndicationController.this.mContext, KeyguardIndicationController.this.mPowerPluggedIn, KeyguardIndicationController.this.mBatteryLevel);
                }

                /* access modifiers changed from: protected */
                public void onPostExecute(String str) {
                    super.onPostExecute(str);
                    KeyguardIndicationController.this.showTransientIndication(str);
                    KeyguardIndicationController.this.hideTransientIndicationDelayed(5000);
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
        }

        public void onKeyguardVisibilityChanged(boolean z) {
            if (z) {
                KeyguardIndicationController.this.updateDisclosure();
            } else {
                KeyguardIndicationController.this.clearUpArrowAnimation();
            }
        }

        public void onKeyguardBouncerChanged(boolean z) {
            if (z) {
                KeyguardIndicationController.this.clearUpArrowAnimation();
                KeyguardIndicationController.this.handleFaceUnlockBouncerMessage("");
            }
        }

        public void onFingerprintHelp(int i, String str) {
            if (KeyguardIndicationController.this.mUpdateMonitor.isUnlockingWithFingerprintAllowed()) {
                int access$1200 = KeyguardIndicationController.this.getTextColor();
                if (KeyguardIndicationController.this.mStatusBarKeyguardViewManager.isBouncerShowing()) {
                    if (!TextUtils.isEmpty(str) && !MiuiKeyguardUtils.isGxzwSensor()) {
                        KeyguardIndicationController.this.mStatusBarKeyguardViewManager.showBouncerMessage(str, KeyguardIndicationController.this.mResources.getColor(R.color.secure_keyguard_bouncer_message_content_text_color));
                    }
                } else if (KeyguardIndicationController.this.mUpdateMonitor.isDeviceInteractive() || (KeyguardIndicationController.this.mDozing && KeyguardIndicationController.this.mUpdateMonitor.isScreenOn())) {
                    KeyguardIndicationController.this.showTransientIndication(str, access$1200);
                    KeyguardIndicationController.this.mHandler.removeMessages(2);
                    KeyguardIndicationController.this.mHandler.sendMessageDelayed(KeyguardIndicationController.this.mHandler.obtainMessage(2), 1300);
                }
            }
        }

        public void onFingerprintError(int i, String str) {
            if (KeyguardIndicationController.this.mUpdateMonitor.isUnlockingWithFingerprintAllowed() && i != 5) {
                this.mFingerprintErrorMsgId = i;
                this.mFpiState = MiuiKeyguardFingerprintUtils$FingerprintIdentificationState.ERROR;
                handleFingerprintStateChanged();
            }
        }

        public void onStartedWakingUp() {
            if (KeyguardIndicationController.this.mMessageToShowOnScreenOn != null) {
                int access$1200 = KeyguardIndicationController.this.getTextColor();
                KeyguardIndicationController keyguardIndicationController = KeyguardIndicationController.this;
                keyguardIndicationController.showTransientIndication(keyguardIndicationController.mMessageToShowOnScreenOn, access$1200);
                KeyguardIndicationController.this.hideTransientIndicationDelayed(5000);
                String unused = KeyguardIndicationController.this.mMessageToShowOnScreenOn = null;
            }
            if (!KeyguardIndicationController.this.mVisible) {
                boolean unused2 = KeyguardIndicationController.this.mChargeUIEntering = false;
                boolean unused3 = KeyguardIndicationController.this.mUpArrowEntering = false;
                KeyguardIndicationController.this.updateIndication();
            } else if (KeyguardIndicationController.this.mPowerPluggedIn) {
                String unused4 = KeyguardIndicationController.this.mUpArrowIndication = null;
                KeyguardIndicationController.this.handleChargeTextAnimation(false);
            } else {
                KeyguardIndicationController keyguardIndicationController2 = KeyguardIndicationController.this;
                String unused5 = keyguardIndicationController2.mUpArrowIndication = keyguardIndicationController2.mResources.getString(R.string.default_lockscreen_unlock_hint_text);
                handleEnterArrowAnimation();
            }
        }

        private void handleEnterArrowAnimation() {
            boolean unused = KeyguardIndicationController.this.mUpArrowEntering = true;
            TranslateAnimation translateAnimation = new TranslateAnimation(1, 0.0f, 1, 0.0f, 1, 2.0f, 1, 0.0f);
            Animation loadAnimation = AnimationUtils.loadAnimation(KeyguardIndicationController.this.mContext, 17432576);
            AnimationSet animationSet = new AnimationSet(true);
            animationSet.addAnimation(translateAnimation);
            animationSet.addAnimation(loadAnimation);
            animationSet.setDuration((long) (DeviceLevelUtils.getAnimationDurationRatio() * 500.0f));
            animationSet.setStartOffset(30);
            KeyguardIndicationController.this.mUpArrow.setVisibility(0);
            KeyguardIndicationController.this.mUpArrow.startAnimation(animationSet);
            KeyguardIndicationController.this.mHandler.sendEmptyMessageDelayed(4, 100);
        }

        public void onStartedGoingToSleep(int i) {
            KeyguardIndicationController.this.clearUpArrowAnimation();
            if (KeyguardIndicationController.this.mUpArrowIndication != null) {
                String unused = KeyguardIndicationController.this.mUpArrowIndication = null;
                KeyguardIndicationController.this.updateIndication();
            }
            KeyguardIndicationController.this.hideTransientIndication();
        }

        public void onFingerprintRunningStateChanged(boolean z) {
            if (z) {
                String unused = KeyguardIndicationController.this.mMessageToShowOnScreenOn = null;
            }
        }

        public void onFingerprintAuthenticated(int i) {
            super.onFingerprintAuthenticated(i);
            this.mFpiState = MiuiKeyguardFingerprintUtils$FingerprintIdentificationState.SUCCEEDED;
            this.mFingerprintAuthUserId = i;
            handleFingerprintStateChanged();
        }

        public void onFingerprintAuthFailed() {
            super.onFingerprintAuthFailed();
            this.mFpiState = MiuiKeyguardFingerprintUtils$FingerprintIdentificationState.FAILED;
            handleFingerprintStateChanged();
        }

        public void onFingerprintLockoutReset() {
            super.onFingerprintLockoutReset();
            this.mFpiState = MiuiKeyguardFingerprintUtils$FingerprintIdentificationState.RESET;
            handleFingerprintStateChanged();
        }

        private void handleFingerprintStateChanged() {
            String str;
            String string;
            if (!KeyguardIndicationController.this.mUpdateMonitor.isUnlockingWithFingerprintAllowed(KeyguardUpdateMonitor.getCurrentUser()) || KeyguardIndicationController.this.mUpdateMonitor.shouldListenForFingerprintWhenUnlocked()) {
                this.mLastFpiState = this.mFpiState;
                return;
            }
            MiuiKeyguardFingerprintUtils$FingerprintIdentificationState miuiKeyguardFingerprintUtils$FingerprintIdentificationState = this.mFpiState;
            String str2 = "";
            if (miuiKeyguardFingerprintUtils$FingerprintIdentificationState == MiuiKeyguardFingerprintUtils$FingerprintIdentificationState.FAILED) {
                str2 = KeyguardIndicationController.this.mResources.getString(R.string.fingerprint_try_again_text);
                str = KeyguardIndicationController.this.mResources.getString(R.string.fingerprint_try_again_msg);
            } else if (miuiKeyguardFingerprintUtils$FingerprintIdentificationState == MiuiKeyguardFingerprintUtils$FingerprintIdentificationState.ERROR) {
                str2 = KeyguardIndicationController.this.mResources.getString(R.string.fingerprint_not_identified_title);
                str = KeyguardIndicationController.this.mResources.getString(R.string.fingerprint_not_identified_msg);
                if (this.mFingerprintErrorMsgId == 9) {
                    str = KeyguardIndicationController.this.mResources.getString(R.string.fingerprint_not_identified_msg_lock);
                }
            } else {
                if (miuiKeyguardFingerprintUtils$FingerprintIdentificationState == MiuiKeyguardFingerprintUtils$FingerprintIdentificationState.SUCCEEDED) {
                    if (this.mFingerprintAuthUserId != KeyguardUpdateMonitor.getCurrentUser()) {
                        if (MiuiKeyguardUtils.isGreenKidActive(KeyguardIndicationController.this.mContext)) {
                            string = KeyguardIndicationController.this.mResources.getString(R.string.input_password_after_boot_msg_can_not_switch_when_greenkid_active);
                        } else if (PhoneUtils.isInCall(KeyguardIndicationController.this.mContext)) {
                            string = KeyguardIndicationController.this.mResources.getString(R.string.input_password_after_boot_msg_can_not_switch_when_calling);
                        } else if (MiuiKeyguardUtils.isSuperPowerActive(KeyguardIndicationController.this.mContext)) {
                            string = KeyguardIndicationController.this.mResources.getString(R.string.input_password_after_boot_msg_can_not_switch_when_superpower_active);
                        } else if (!KeyguardIndicationController.this.mUpdateMonitor.getStrongAuthTracker().hasUserAuthenticatedSinceBoot(this.mFingerprintAuthUserId)) {
                            str2 = KeyguardIndicationController.this.mResources.getString(R.string.fingerprint_enter_second_psw_title);
                            str = KeyguardIndicationController.this.mResources.getString(R.string.fingerprint_enter_second_psw_msg);
                        }
                        String str3 = str2;
                        str2 = string;
                        str = str3;
                    }
                } else if (this.mLastFpiState == MiuiKeyguardFingerprintUtils$FingerprintIdentificationState.ERROR && miuiKeyguardFingerprintUtils$FingerprintIdentificationState == MiuiKeyguardFingerprintUtils$FingerprintIdentificationState.RESET) {
                    str2 = KeyguardIndicationController.this.mResources.getString(R.string.fingerprint_not_identified_title);
                    str = KeyguardIndicationController.this.mResources.getString(R.string.fingerprint_again_identified_msg);
                }
                str = str2;
            }
            int access$1200 = KeyguardIndicationController.this.getTextColor();
            KeyguardIndicationController.this.mStatusBarKeyguardViewManager.showBouncerMessage(str2, str, KeyguardIndicationController.this.mResources.getColor(R.color.secure_keyguard_bouncer_message_content_text_color));
            MiuiKeyguardFingerprintUtils$FingerprintIdentificationState miuiKeyguardFingerprintUtils$FingerprintIdentificationState2 = this.mFpiState;
            MiuiKeyguardFingerprintUtils$FingerprintIdentificationState miuiKeyguardFingerprintUtils$FingerprintIdentificationState3 = MiuiKeyguardFingerprintUtils$FingerprintIdentificationState.ERROR;
            if (miuiKeyguardFingerprintUtils$FingerprintIdentificationState2 == miuiKeyguardFingerprintUtils$FingerprintIdentificationState3 && this.mLastFpiState != miuiKeyguardFingerprintUtils$FingerprintIdentificationState3 && KeyguardIndicationController.this.mStatusBarKeyguardViewManager.isBouncerShowing()) {
                KeyguardIndicationController.this.mStatusBarKeyguardViewManager.applyHintAnimation(500);
            }
            if (this.mFpiState == MiuiKeyguardFingerprintUtils$FingerprintIdentificationState.FAILED && MiuiKeyguardUtils.isGxzwSensor() && KeyguardIndicationController.this.mStatusBarKeyguardViewManager.isBouncerShowing()) {
                KeyguardIndicationController.this.mStatusBarKeyguardViewManager.applyHintAnimation(500);
            }
            if (this.mFpiState == MiuiKeyguardFingerprintUtils$FingerprintIdentificationState.FAILED && KeyguardIndicationController.this.mUpdateMonitor.isDeviceInteractive()) {
                KeyguardIndicationController.this.mHandler.removeMessages(1);
                KeyguardIndicationController keyguardIndicationController = KeyguardIndicationController.this;
                keyguardIndicationController.showTransientIndication(keyguardIndicationController.mContext.getString(R.string.fingerprint_try_again_text), access$1200);
                KeyguardIndicationController.this.hideTransientIndicationDelayed(5000);
            }
            String unused = KeyguardIndicationController.this.mMessageToShowOnScreenOn = str2;
            this.mLastFpiState = this.mFpiState;
        }

        public void onUserUnlocked() {
            if (KeyguardIndicationController.this.mVisible) {
                KeyguardIndicationController.this.updateIndication();
            }
        }

        public void onBottomAreaButtonClicked(boolean z) {
            KeyguardIndicationController.this.handleBottomButtonClicked(z);
        }

        public void onPhoneSignalChanged(boolean z) {
            boolean unused = KeyguardIndicationController.this.mSignalAvailable = z;
            KeyguardIndicationController.this.updateIndication();
        }

        public void onLockScreenMagazinePreViewVisibilityChanged(boolean z) {
            boolean unused = KeyguardIndicationController.this.mLockScreenMagazinePreViewVisibility = z;
        }
    }

    /* access modifiers changed from: private */
    public void handleBottomButtonClicked(boolean z) {
        this.mTextView.setBottomAreaButtonClicked(z);
        startBottomButtonClickAnim(z);
    }

    private void startBottomButtonClickAnim(boolean z) {
        ObjectAnimator objectAnimator = this.mBottomButtonClickAnimator;
        if (objectAnimator != null && objectAnimator.isRunning()) {
            if (z) {
                this.mBottomButtonClickAnimator.cancel();
            } else {
                return;
            }
        }
        if (z) {
            this.mBottomButtonClickAnimator = ObjectAnimator.ofFloat(this.mTextView, View.ALPHA, new float[]{1.0f, 0.0f});
            this.mBottomButtonClickAnimator.setInterpolator(Ease$Quint.easeOut);
            this.mBottomButtonClickAnimator.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animator) {
                    KeyguardIndicationController.this.mTextView.setVisibility(8);
                }
            });
            this.mBottomButtonClickAnimator.setDuration(0);
        } else {
            this.mBottomButtonClickAnimator = ObjectAnimator.ofFloat(this.mTextView, View.ALPHA, new float[]{0.0f, 1.0f});
            this.mBottomButtonClickAnimator.setInterpolator(Ease$Cubic.easeInOut);
            this.mBottomButtonClickAnimator.addListener(new AnimatorListenerAdapter() {
                public void onAnimationStart(Animator animator) {
                    KeyguardIndicationController.this.mTextView.setVisibility(0);
                }

                public void onAnimationEnd(Animator animator) {
                    KeyguardIndicationController.this.mTextView.setVisibility(0);
                    KeyguardIndicationController.this.mTextView.setAlpha(1.0f);
                }
            });
            this.mBottomButtonClickAnimator.setDuration(800);
        }
        this.mBottomButtonClickAnimator.start();
    }

    public void setDarkMode(boolean z) {
        if (this.mDarkMode != z) {
            this.mDarkMode = z;
            updateIndication();
        }
    }

    /* access modifiers changed from: private */
    public int getTextColor() {
        int i;
        if (this.mDarkMode) {
            i = this.mPowerPluggedIn ? R.color.miui_common_unlock_screen_charge_dark_text_color : R.color.miui_common_unlock_screen_common_dark_text_color;
        } else {
            i = this.mPowerPluggedIn ? R.color.miui_charge_lock_screen_unlock_hint_text_color : R.color.miui_default_lock_screen_unlock_hint_text_color;
        }
        return this.mResources.getColor(i);
    }

    public void handleChargeTextAnimation(boolean z) {
        this.mShowChargeAnimation = z;
        this.mHandler.removeMessages(5);
        Log.i("KeyguardIndication", "handleChargeTextAnimation: " + z + ";mPowerPluggedIn=" + this.mPowerPluggedIn);
        this.mTextView.setVisibility(0);
        if (!this.mShowChargeAnimation && this.mPowerPluggedIn) {
            this.mChargeUIEntering = true;
            Animation loadAnimation = AnimationUtils.loadAnimation(this.mContext, 17432576);
            TranslateAnimation translateAnimation = new TranslateAnimation(1, 0.0f, 1, 0.0f, 1, 2.0f, 1, 0.0f);
            AnimationSet animationSet = new AnimationSet(true);
            animationSet.addAnimation(loadAnimation);
            animationSet.addAnimation(translateAnimation);
            animationSet.setDuration(500);
            animationSet.setAnimationListener(new Animation.AnimationListener() {
                public void onAnimationRepeat(Animation animation) {
                }

                public void onAnimationStart(Animation animation) {
                    Log.i("KeyguardIndication", "handleChargeTextAnimation: onAnimationStart");
                    KeyguardIndicationController.this.mTextView.setVisibility(0);
                    KeyguardIndicationController.this.updateChargingInfoIndication();
                }

                public void onAnimationEnd(Animation animation) {
                    Log.i("KeyguardIndication", "handleChargeTextAnimation: onAnimationEnd");
                    boolean unused = KeyguardIndicationController.this.mChargeUIEntering = false;
                    if (KeyguardIndicationController.this.mPowerPluggedIn) {
                        KeyguardIndicationController.this.updateChargingInfoIndication();
                    } else {
                        KeyguardIndicationController.this.updateIndication();
                    }
                    KeyguardIndicationController.this.mTextView.setVisibility(0);
                }
            });
            this.mTextView.startAnimation(animationSet);
        }
    }

    public void setChargeController(MiuiChargeController miuiChargeController) {
        this.mMiuiChargeController = miuiChargeController;
    }
}
