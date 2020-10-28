package com.android.systemui.statusbar;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.systemui.Constants;
import com.android.systemui.DemoMode;
import com.android.systemui.Dependency;
import com.android.systemui.MCCUtils;
import com.android.systemui.Util;
import com.android.systemui.miui.statusbar.WifiLabelText;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.phone.SignalClusterViewController;
import com.android.systemui.statusbar.phone.StatusBarFactory;
import com.android.systemui.statusbar.phone.StatusBarIconControllerHelper;
import com.android.systemui.statusbar.phone.StatusBarTypeController;
import com.android.systemui.statusbar.policy.DarkIconDispatcher;
import com.android.systemui.statusbar.policy.DarkIconDispatcherHelper;
import com.android.systemui.statusbar.policy.DemoModeController;
import com.android.systemui.statusbar.policy.FiveGController;
import com.android.systemui.statusbar.policy.HotspotController;
import com.android.systemui.statusbar.policy.NetworkController;
import com.android.systemui.statusbar.policy.SecurityController;
import com.android.systemui.statusbar.policy.TelephonyIcons;
import com.android.systemui.tuner.TunerService;
import com.android.systemui.util.DisableStateTracker;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import miui.os.Build;
import miui.telephony.SubscriptionInfo;

public class SignalClusterView extends LinearLayout implements NetworkController.SignalCallback, SecurityController.SecurityControllerCallback, HotspotController.Callback, TunerService.Tunable, DarkIconDispatcher.DarkReceiver, DemoMode, StatusBarTypeController.StatusBarTypeChangeListener {
    static final boolean DEBUG = Log.isLoggable("SignalClusterView", 3);
    /* access modifiers changed from: private */
    public boolean isTypeChangeable;
    private boolean mActivityEnabled;
    ImageView mAirplane;
    private String mAirplaneContentDescription;
    private int mAirplaneIconId;
    private boolean mBlockAirplane;
    private boolean mBlockEthernet;
    private boolean mBlockMobile;
    private boolean mBlockWifi;
    /* access modifiers changed from: private */
    public SignalClusterViewController mController;
    private DarkIconDispatcher mDarkIconDispatcher;
    private float mDarkIntensity;
    boolean[] mDataConnectedStatus;
    private final DemoModeController.DemoModeCallback mDemoCallback;
    ImageView mDemoMobileSignal;
    private boolean mDemoMode;
    String[] mDualSignalDescription;
    private final int mEndPadding;
    private final int mEndPaddingNothingVisible;
    ImageView mEthernet;
    private boolean mEthernetAble;
    private String mEthernetDescription;
    private int mEthernetIconId;
    private boolean mEthernetVisible;
    private int mFilterColor;
    /* access modifiers changed from: private */
    public FiveGController mFiveGController;
    private boolean mForceBlockWifi;
    /* access modifiers changed from: private */
    public boolean mHideVolte;
    private boolean mHideVowifi;
    private final HotspotController mHotspot;
    private final float mIconScaleFactor;
    private int mIconTint;
    /* access modifiers changed from: private */
    public boolean mIsAirplaneMode;
    /* access modifiers changed from: private */
    public boolean mIsDripType;
    private int mLastAirplaneIconId;
    private int mLastEthernetIconId;
    private int mLastWifiActivityId;
    private int mLastWifiBadgeId;
    private int mLastWifiStrengthId;
    private final int mMobileDataIconStartPadding;
    LinearLayout[] mMobileSignalGroup;
    private final int mMobileSignalGroupEndPadding;
    /* access modifiers changed from: private */
    public final NetworkController mNetworkController;
    ImageView mNoSims;
    private int mNoSimsIcon;
    private boolean mNoSimsVisible;
    /* access modifiers changed from: private */
    public boolean mNotchEar;
    /* access modifiers changed from: private */
    public boolean mNotchEarDual;
    private boolean mNotchEarDualEnable;
    /* access modifiers changed from: private */
    public ArrayList<PhoneState> mPhoneStates;
    private boolean mReadIconsFromXML;
    private final int mSecondaryTelephonyPadding;
    private final SecurityController mSecurityController;
    /* access modifiers changed from: private */
    public boolean mShowHDIcon;
    private boolean mShowWifiGeneration;
    ViewGroup mSignalDualNotchGroup;
    ImageView mSignalDualNotchMobile;
    ImageView mSignalDualNotchMobile2;
    ImageView mSignalDualNotchMobileInout;
    TextView mSignalDualNotchMobileType;
    ImageView mSignalDualNotchMobileUpgrade;
    TextView mSignalDualNotchMobileVoice;
    private int mSimCnt;
    ImageView mSlaveWifi;
    private int mSlaveWifiStrengthId;
    private boolean mSlaveWifiVisible;
    private final Rect mTintArea;
    private boolean mVoWifiEnableInEar;
    ImageView[] mVowifi;
    ImageView mVpn;
    private boolean mVpnEnableInEar;
    /* access modifiers changed from: private */
    public boolean mVpnVisible;
    private final int mWideTypeIconStartPadding;
    ImageView mWifi;
    ImageView mWifiActivity;
    private boolean mWifiActivityEnabled;
    ImageView mWifiAp;
    ImageView mWifiApConnectMark;
    private int mWifiBadgeId;
    private String mWifiDescription;
    private int mWifiGenerationLevel;
    TextView mWifiGenerationView;
    ViewGroup mWifiGroup;
    private boolean mWifiIn;
    WifiLabelText mWifiLabel;
    private String mWifiName;
    /* access modifiers changed from: private */
    public boolean mWifiNoNetwork;
    private boolean mWifiNoNetworkEnableInEar;
    private boolean mWifiOut;
    View mWifiSignalSpacer;
    private int mWifiStrengthId;
    /* access modifiers changed from: private */
    public boolean mWifiVisible;

    public boolean hasOverlappingRendering() {
        return false;
    }

    public void setMobileDataEnabled(boolean z) {
    }

    public SignalClusterView(Context context) {
        this(context, (AttributeSet) null);
    }

    public SignalClusterView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public SignalClusterView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mNoSimsVisible = false;
        this.mVpnVisible = false;
        this.mEthernetVisible = false;
        this.mEthernetIconId = 0;
        this.mLastEthernetIconId = -1;
        this.mWifiBadgeId = -1;
        this.mWifiVisible = false;
        this.mWifiNoNetwork = false;
        this.mWifiStrengthId = 0;
        this.mLastWifiBadgeId = -1;
        this.mLastWifiStrengthId = -1;
        this.mLastWifiActivityId = -1;
        this.mIsAirplaneMode = false;
        this.mAirplaneIconId = 0;
        this.mLastAirplaneIconId = -1;
        this.mPhoneStates = new ArrayList<>();
        this.mTintArea = new Rect();
        this.mIconTint = this.mContext.getColor(R.color.light_mode_icon_color_single_tone);
        this.mFilterColor = 0;
        this.mSlaveWifiVisible = false;
        this.mSlaveWifiStrengthId = 0;
        this.mShowHDIcon = false;
        this.mVowifi = new ImageView[2];
        this.mDualSignalDescription = new String[2];
        this.mDataConnectedStatus = new boolean[2];
        this.mMobileSignalGroup = new LinearLayout[2];
        this.isTypeChangeable = true;
        this.mDemoCallback = new DemoModeController.DemoModeCallback() {
            public void onDemoModeChanged(String str, Bundle bundle) {
                SignalClusterView.this.dispatchDemoCommand(str, bundle);
            }
        };
        Resources resources = getResources();
        Resources resourcesForOperation = MCCUtils.getResourcesForOperation(context, "00000", false);
        this.mHideVowifi = resourcesForOperation.getBoolean(R.bool.status_bar_hide_vowifi);
        this.mHideVolte = resourcesForOperation.getBoolean(R.bool.status_bar_hide_volte);
        this.mShowHDIcon = resourcesForOperation.getBoolean(R.bool.status_bar_show_hd_icon);
        this.mMobileSignalGroupEndPadding = resources.getDimensionPixelSize(R.dimen.mobile_signal_group_end_padding);
        this.mMobileDataIconStartPadding = resources.getDimensionPixelSize(R.dimen.mobile_data_icon_start_padding);
        this.mWideTypeIconStartPadding = resources.getDimensionPixelSize(R.dimen.wide_type_icon_start_padding);
        this.mSecondaryTelephonyPadding = resources.getDimensionPixelSize(R.dimen.secondary_telephony_padding);
        this.mEndPadding = resources.getDimensionPixelSize(R.dimen.signal_cluster_battery_padding);
        this.mEndPaddingNothingVisible = resources.getDimensionPixelSize(R.dimen.no_signal_cluster_battery_padding);
        TypedValue typedValue = new TypedValue();
        resources.getValue(R.dimen.status_bar_icon_scale_factor, typedValue, true);
        this.mIconScaleFactor = typedValue.getFloat();
        this.mNetworkController = (NetworkController) Dependency.get(NetworkController.class);
        this.mSecurityController = (SecurityController) Dependency.get(SecurityController.class);
        this.mFiveGController = (FiveGController) Dependency.get(FiveGController.class);
        addOnAttachStateChangeListener(new DisableStateTracker(0, 2));
        updateActivityEnabled();
        this.mReadIconsFromXML = resources.getBoolean(R.bool.config_read_icons_from_xml);
        this.mHotspot = (HotspotController) Dependency.get(HotspotController.class);
        this.mController = StatusBarFactory.getInstance().getSignalClusterViewController(getContext());
        updateSwitches();
        this.mDarkIconDispatcher = (DarkIconDispatcher) Dependency.get(DarkIconDispatcher.class);
        this.mDarkIconDispatcher.applyDark(this);
    }

    public void onTuningChanged(String str, String str2) {
        if ("icon_blacklist".equals(str)) {
            ArraySet<String> iconBlacklist = StatusBarIconControllerHelper.getIconBlacklist(str2);
            boolean contains = iconBlacklist.contains("airplane");
            boolean contains2 = iconBlacklist.contains("mobile");
            boolean contains3 = iconBlacklist.contains("wifi");
            boolean contains4 = iconBlacklist.contains("ethernet");
            if (contains != this.mBlockAirplane || contains2 != this.mBlockMobile || contains4 != this.mBlockEthernet || contains3 != this.mBlockWifi) {
                this.mBlockAirplane = contains;
                this.mBlockMobile = contains2;
                this.mBlockEthernet = contains4;
                this.mBlockWifi = contains3 || this.mForceBlockWifi;
                this.mNetworkController.removeCallback(this);
                this.mNetworkController.addCallback(this);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mVpn = (ImageView) findViewById(R.id.vpn);
        this.mEthernet = (ImageView) findViewById(R.id.ethernet);
        this.mWifiGroup = (ViewGroup) findViewById(R.id.wifi_combo);
        this.mWifi = (ImageView) findViewById(R.id.wifi_signal);
        this.mSlaveWifi = (ImageView) findViewById(R.id.slave_wifi);
        this.mWifiActivity = (ImageView) findViewById(R.id.wifi_inout);
        this.mWifiGenerationView = (TextView) findViewById(R.id.wifi_generation);
        this.mWifiAp = (ImageView) findViewById(R.id.wifi_ap_on);
        this.mWifiLabel = (WifiLabelText) findViewById(R.id.wifi_label);
        this.mAirplane = (ImageView) findViewById(R.id.airplane);
        this.mVowifi[0] = (ImageView) findViewById(R.id.vowifi_0);
        this.mVowifi[1] = (ImageView) findViewById(R.id.vowifi_1);
        this.mNoSims = (ImageView) findViewById(R.id.no_sims);
        this.mDemoMobileSignal = (ImageView) findViewById(R.id.demo_mobile_signal);
        this.mWifiSignalSpacer = findViewById(R.id.wifi_signal_spacer);
        this.mMobileSignalGroup[0] = (LinearLayout) findViewById(R.id.mobile_signal_group_0);
        this.mMobileSignalGroup[1] = (LinearLayout) findViewById(R.id.mobile_signal_group_1);
        this.mWifiApConnectMark = (ImageView) findViewById(R.id.wifi_ap_connect_mark);
        this.mSignalDualNotchGroup = (ViewGroup) findViewById(R.id.mobile_signal_group_dual_notch);
        this.mSignalDualNotchMobile = (ImageView) this.mSignalDualNotchGroup.findViewById(R.id.notch_mobile_signal);
        this.mSignalDualNotchMobile2 = (ImageView) this.mSignalDualNotchGroup.findViewById(R.id.notch_mobile_signal2);
        this.mSignalDualNotchMobileVoice = (TextView) this.mSignalDualNotchGroup.findViewById(R.id.notch_carrier);
        this.mSignalDualNotchMobileType = (TextView) this.mSignalDualNotchGroup.findViewById(R.id.mobile_type);
        this.mSignalDualNotchMobileInout = (ImageView) this.mSignalDualNotchGroup.findViewById(R.id.mobile_inout);
        this.mSignalDualNotchMobileUpgrade = (ImageView) this.mSignalDualNotchGroup.findViewById(R.id.mobile_signal_upgrade);
        maybeScaleVpnAndNoSimsIcons();
        updateNotchEar();
        updateWifiGenerationView();
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
    }

    private void maybeScaleVpnAndNoSimsIcons() {
        if (this.mIconScaleFactor != 1.0f) {
            ImageView imageView = this.mVpn;
            imageView.setImageDrawable(new ScalingDrawableWrapper(imageView.getDrawable(), this.mIconScaleFactor));
            ImageView imageView2 = this.mNoSims;
            imageView2.setImageDrawable(new ScalingDrawableWrapper(imageView2.getDrawable(), this.mIconScaleFactor));
        }
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mVpnVisible = isVpnVisible();
        Iterator<PhoneState> it = this.mPhoneStates.iterator();
        while (it.hasNext()) {
            PhoneState next = it.next();
            if (next.mSlot < this.mMobileSignalGroup.length && next.mMobileGroup.getParent() == null) {
                this.mMobileSignalGroup[next.mSlot].addView(next.mMobileGroup);
            }
        }
        ((TunerService) Dependency.get(TunerService.class)).addTunable(this, "icon_blacklist");
        apply();
        applyIconTint();
        this.mNetworkController.addCallback(this);
        this.mSecurityController.addCallback(this);
        this.mHotspot.addCallback(this);
        ((DemoModeController) Dependency.get(DemoModeController.class)).addCallback(this.mDemoCallback);
        ((StatusBarTypeController) Dependency.get(StatusBarTypeController.class)).addCallback(this);
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        int i = 0;
        while (true) {
            LinearLayout[] linearLayoutArr = this.mMobileSignalGroup;
            if (i < linearLayoutArr.length) {
                linearLayoutArr[i].removeAllViews();
                i++;
            } else {
                ((TunerService) Dependency.get(TunerService.class)).removeTunable(this);
                this.mSecurityController.removeCallback(this);
                this.mNetworkController.removeCallback(this);
                this.mHotspot.removeCallback(this);
                ((DemoModeController) Dependency.get(DemoModeController.class)).removeCallback(this.mDemoCallback);
                ((StatusBarTypeController) Dependency.get(StatusBarTypeController.class)).removeCallback(this);
                super.onDetachedFromWindow();
                return;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        applyIconTint();
    }

    public void onStateChanged() {
        post(new Runnable() {
            public void run() {
                SignalClusterView signalClusterView = SignalClusterView.this;
                boolean unused = signalClusterView.mVpnVisible = signalClusterView.isVpnVisible();
                SignalClusterView.this.apply();
            }
        });
    }

    /* access modifiers changed from: private */
    public boolean isVpnVisible() {
        return (!this.mNotchEar || this.mVpnEnableInEar) && this.mSecurityController.isVpnEnabled() && !this.mSecurityController.isSilentVpnPackage();
    }

    public void onHotspotChanged(boolean z) {
        this.mWifiAp.setVisibility((!z || this.mNotchEar) ? 8 : 0);
    }

    private void updateActivityEnabled() {
        this.mActivityEnabled = this.mContext.getResources().getBoolean(R.bool.config_showActivity);
        this.mWifiActivityEnabled = this.mContext.getResources().getBoolean(R.bool.config_showWifiActivity);
    }

    public void updateWifiGeneration(boolean z, int i) {
        this.mShowWifiGeneration = z;
        this.mWifiGenerationLevel = i;
        updateWifiGenerationView();
    }

    public void setWifiIndicators(boolean z, NetworkController.IconState iconState, NetworkController.IconState iconState2, boolean z2, boolean z3, String str, boolean z4) {
        boolean z5 = true;
        this.mWifiVisible = iconState.visible && !this.mBlockWifi;
        this.mWifiStrengthId = iconState.icon;
        this.mWifiBadgeId = iconState.iconOverlay;
        this.mWifiDescription = iconState.contentDescription;
        this.mWifiName = getWifiName(str);
        this.mWifiIn = z2 && this.mActivityEnabled && this.mWifiVisible;
        this.mWifiOut = z3 && this.mActivityEnabled && this.mWifiVisible;
        if (this.mWifiStrengthId != R.drawable.stat_sys_wifi_signal_null) {
            z5 = false;
        }
        this.mWifiNoNetwork = z5;
        apply();
        updateDripVolte();
    }

    public void setSlaveWifiIndicators(boolean z, NetworkController.IconState iconState, NetworkController.IconState iconState2) {
        if (this.mSlaveWifi == null) {
            this.mSlaveWifiVisible = false;
            this.mSlaveWifiStrengthId = 0;
        } else if (this.mSlaveWifiVisible != iconState.visible || this.mSlaveWifiStrengthId != iconState.icon) {
            this.mSlaveWifiVisible = iconState.visible;
            this.mSlaveWifiStrengthId = iconState.icon;
            if (!this.mSlaveWifiVisible || this.mNotchEar) {
                this.mSlaveWifi.setVisibility(8);
                return;
            }
            this.mSlaveWifi.setVisibility(0);
            updateIcon(this.mSlaveWifi, this.mSlaveWifiStrengthId);
        }
    }

    public void updateNotchEar() {
        ImageView imageView;
        this.mNotchEar = this.mController.isNotch();
        this.mVpnVisible = isVpnVisible();
        ImageView imageView2 = this.mWifiAp;
        int i = 0;
        if (imageView2 != null) {
            imageView2.setVisibility((!this.mHotspot.isHotspotEnabled() || this.mNotchEar) ? 8 : 0);
        }
        if (this.mNotchEar) {
            if (!this.mVpnEnableInEar && (imageView = this.mVpn) != null) {
                imageView.setVisibility(8);
            }
            ImageView imageView3 = this.mSlaveWifi;
            if (imageView3 != null) {
                imageView3.setVisibility(8);
            }
            if (!this.mVoWifiEnableInEar && this.mVowifi != null) {
                while (true) {
                    ImageView[] imageViewArr = this.mVowifi;
                    if (i >= imageViewArr.length) {
                        break;
                    }
                    if (imageViewArr[i] != null) {
                        imageViewArr[i].setVisibility(8);
                    }
                    i++;
                }
            }
            updatePhoneStatesForNotchEar();
        }
    }

    private void updateWifiGenerationView() {
        TextView textView = this.mWifiGenerationView;
        if (textView != null) {
            textView.setVisibility(this.mShowWifiGeneration ? 0 : 8);
            int i = this.mWifiGenerationLevel;
            if (i > 0) {
                this.mWifiGenerationView.setText(String.valueOf(i));
            }
        }
        ImageView imageView = this.mWifiActivity;
        if (imageView != null) {
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) imageView.getLayoutParams();
            if (this.mShowWifiGeneration) {
                layoutParams.gravity = 83;
            } else {
                layoutParams.gravity = 85;
            }
            this.mWifiActivity.setLayoutParams(layoutParams);
        }
    }

    private void updatePhoneStatesForNotchEar() {
        if (this.mPhoneStates != null) {
            for (int i = 0; i < this.mPhoneStates.size(); i++) {
                if (this.mPhoneStates.get(i) != null) {
                    int i2 = this.mPhoneStates.get(i).mSlot;
                    if (couldHideVolte()) {
                        setIsImsRegisted(i2, false);
                    }
                    setVolteNoService(i2, false);
                    setSpeechHd(i2, false);
                }
            }
        }
    }

    public void setIsImsRegisted(int i, boolean z) {
        PhoneState state = getState(i);
        if (state != null) {
            state.mIsImsRegistered = z;
            state.setIsImsRegisted(z);
        }
    }

    public void setVolteNoService(int i, boolean z) {
        PhoneState state = getState(i);
        if (state != null) {
            state.setVolteNoService(z);
        }
    }

    public void setSpeechHd(int i, boolean z) {
        PhoneState state = getState(i);
        if (state != null) {
            state.setSpeechHd(z);
        }
    }

    public void setVowifi(int i, boolean z) {
        if (i < this.mVowifi.length) {
            int i2 = ((!this.mNotchEar || this.mVoWifiEnableInEar) && z && !this.mHideVowifi && !this.mNetworkController.hideVowifiForOperation(i)) ? 0 : 8;
            if (Constants.IS_INTERNATIONAL && i2 == 0) {
                updateIcon(this.mVowifi[i], this.mNetworkController.getVowifiDrawableId(i));
            }
            this.mVowifi[i].setVisibility(i2);
        }
    }

    public void setNetworkNameVoice(int i, String str) {
        PhoneState state = getState(i);
        if (state != null) {
            state.setNetworkNameVoice(str);
        }
    }

    public void setIsDefaultDataSim(int i, boolean z) {
        PhoneState state = getState(i);
        if (state != null) {
            state.setIsDefaultDataSim(z);
        }
    }

    public void setMobileDataIndicators(NetworkController.IconState iconState, NetworkController.IconState iconState2, int i, int i2, boolean z, boolean z2, int i3, int i4, int i5, String str, String str2, boolean z3, int i6, boolean z4) {
        PhoneState state = getState(i6);
        if (state != null) {
            boolean z5 = true;
            state.mMobileVisible = iconState.visible && !this.mBlockMobile;
            state.mMobileStrengthId = iconState.icon;
            state.mMobileTypeId = i;
            String unused = state.mMobileDescription = iconState.contentDescription;
            String unused2 = state.mMobileTypeDescription = str;
            boolean unused3 = state.mIsMobileTypeIconWide = i != 0 && z3;
            state.mRoaming = z4;
            state.mActivityIn = z && this.mActivityEnabled;
            if (!z2 || !this.mActivityEnabled) {
                z5 = false;
            }
            state.mActivityOut = z5;
            int unused4 = state.mDataActivityId = i3;
            int unused5 = state.mStackedDataId = i4;
            int unused6 = state.mStackedVoiceId = i5;
            apply();
        }
    }

    public void setEthernetIndicators(NetworkController.IconState iconState) {
        this.mEthernetVisible = iconState.visible && !this.mBlockEthernet;
        this.mEthernetIconId = iconState.icon;
        this.mEthernetDescription = iconState.contentDescription;
        apply();
    }

    public void setNoSims(boolean z) {
        this.mNoSimsVisible = z && !this.mBlockMobile;
        apply();
    }

    public void setSubs(List<SubscriptionInfo> list) {
        if (!hasCorrectSubs(list)) {
            this.mPhoneStates.clear();
            int i = 0;
            for (int i2 = 0; i2 < this.mVowifi.length; i2++) {
                setVowifi(i2, false);
            }
            int i3 = 0;
            while (true) {
                LinearLayout[] linearLayoutArr = this.mMobileSignalGroup;
                if (i3 >= linearLayoutArr.length) {
                    break;
                }
                if (linearLayoutArr[i3] != null) {
                    linearLayoutArr[i3].removeAllViews();
                }
                i3++;
            }
            int size = list.size();
            for (int i4 = 0; i4 < size; i4++) {
                int slotId = list.get(i4).getSlotId();
                inflatePhoneState(list.get(i4).getSlotId());
                int subscriptionId = list.get(i4).getSubscriptionId();
                NetworkController.SignalState signalState = ((NetworkController) Dependency.get(NetworkController.class)).getSignalState();
                if (signalState.imsMap.get(Integer.valueOf(subscriptionId)) != null) {
                    setIsImsRegisted(slotId, signalState.imsMap.get(Integer.valueOf(subscriptionId)).booleanValue());
                }
                if (signalState.vowifiMap.get(Integer.valueOf(subscriptionId)) != null) {
                    setVowifi(slotId, signalState.vowifiMap.get(Integer.valueOf(subscriptionId)).booleanValue());
                }
                if (signalState.speedHdMap.get(Integer.valueOf(subscriptionId)) != null) {
                    setSpeechHd(slotId, signalState.speedHdMap.get(Integer.valueOf(subscriptionId)).booleanValue());
                }
            }
            this.mSimCnt = size;
            if (this.mNotchEar) {
                this.mNotchEarDual = this.mSimCnt == 2 && notchEarDualEnable();
                ViewGroup viewGroup = this.mSignalDualNotchGroup;
                if (viewGroup != null) {
                    if (!this.mNotchEarDual) {
                        i = 8;
                    }
                    viewGroup.setVisibility(i);
                }
            }
            if (isAttachedToWindow()) {
                applyIconTint();
            }
        }
    }

    public void reApply() {
        apply();
    }

    private boolean hasCorrectSubs(List<SubscriptionInfo> list) {
        int size = list.size();
        if (size != this.mPhoneStates.size()) {
            return false;
        }
        for (int i = 0; i < size; i++) {
            if (this.mPhoneStates.get(i).mSlot != list.get(i).getSlotId()) {
                return false;
            }
        }
        return true;
    }

    private PhoneState getState(int i) {
        Iterator<PhoneState> it = this.mPhoneStates.iterator();
        while (it.hasNext()) {
            PhoneState next = it.next();
            if (next.mSlot == i) {
                return next;
            }
        }
        Log.e("SignalClusterView", "Unexpected subscription " + i);
        return null;
    }

    private int getNoSimIcon() {
        Resources resources = getContext().getResources();
        if (!resources.getBoolean(R.bool.config_read_icons_from_xml)) {
            return 0;
        }
        try {
            String[] stringArray = resources.getStringArray(R.array.multi_no_sim);
            if (stringArray == null) {
                return 0;
            }
            String str = stringArray[0];
            int identifier = resources.getIdentifier(str, (String) null, getContext().getPackageName());
            if (DEBUG) {
                Log.d("SignalClusterView", "getNoSimIcon resId = " + identifier + " resName = " + str);
            }
            return identifier;
        } catch (Resources.NotFoundException unused) {
            return 0;
        }
    }

    private PhoneState inflatePhoneState(int i) {
        PhoneState phoneState = new PhoneState(i, this.mContext);
        LinearLayout[] linearLayoutArr = this.mMobileSignalGroup;
        if (i <= linearLayoutArr.length && linearLayoutArr[i] != null) {
            linearLayoutArr[i].addView(phoneState.mMobileGroup);
        }
        this.mPhoneStates.add(phoneState);
        return phoneState;
    }

    public void setIsAirplaneMode(NetworkController.IconState iconState) {
        this.mIsAirplaneMode = iconState.visible && !this.mBlockAirplane;
        this.mAirplaneIconId = iconState.icon;
        this.mAirplaneContentDescription = iconState.contentDescription;
        apply();
    }

    public boolean dispatchPopulateAccessibilityEventInternal(AccessibilityEvent accessibilityEvent) {
        ViewGroup viewGroup;
        ImageView imageView;
        if (!(!this.mEthernetVisible || (imageView = this.mEthernet) == null || imageView.getContentDescription() == null)) {
            accessibilityEvent.getText().add(this.mEthernet.getContentDescription());
        }
        if (!(!this.mWifiVisible || (viewGroup = this.mWifiGroup) == null || viewGroup.getContentDescription() == null)) {
            accessibilityEvent.getText().add(this.mWifiGroup.getContentDescription());
        }
        Iterator<PhoneState> it = this.mPhoneStates.iterator();
        while (it.hasNext()) {
            it.next().populateAccessibilityEvent(accessibilityEvent);
        }
        return super.dispatchPopulateAccessibilityEventInternal(accessibilityEvent);
    }

    public void onRtlPropertiesChanged(int i) {
        super.onRtlPropertiesChanged(i);
        ImageView imageView = this.mEthernet;
        if (imageView != null) {
            imageView.setImageDrawable((Drawable) null);
            this.mLastEthernetIconId = -1;
        }
        ImageView imageView2 = this.mWifi;
        if (imageView2 != null) {
            imageView2.setImageDrawable((Drawable) null);
            this.mLastWifiStrengthId = -1;
            this.mLastWifiBadgeId = -1;
        }
        Iterator<PhoneState> it = this.mPhoneStates.iterator();
        while (it.hasNext()) {
            PhoneState next = it.next();
            if (next.mMobileType != null) {
                int unused = next.mLastMobileTypeId = -1;
            }
        }
        ImageView imageView3 = this.mAirplane;
        if (imageView3 != null) {
            imageView3.setImageDrawable((Drawable) null);
            this.mLastAirplaneIconId = -1;
        }
        apply();
    }

    /* access modifiers changed from: private */
    public void apply() {
        String str;
        if (this.mWifiGroup != null) {
            int i = 4;
            int i2 = 8;
            if (this.mDemoMode) {
                for (int i3 = 0; i3 < getChildCount(); i3++) {
                    getChildAt(i3).setVisibility(8);
                }
                this.mWifiGroup.setVisibility(0);
                this.mDemoMobileSignal.setVisibility(0);
                this.mWifiActivity.setVisibility(4);
                this.mWifiAp.setVisibility(8);
                this.mVowifi[0].setVisibility(8);
                this.mVowifi[1].setVisibility(8);
                this.mWifi.setImageResource(R.drawable.stat_sys_wifi_signal_4);
                this.mDemoMobileSignal.setImageResource(R.drawable.stat_sys_signal_5);
                return;
            }
            updateIcon(this.mVowifi[0], this.mNetworkController.getVowifiDrawableId(0));
            updateIcon(this.mVowifi[1], this.mNetworkController.getVowifiDrawableId(1));
            int i4 = 0;
            while (true) {
                LinearLayout[] linearLayoutArr = this.mMobileSignalGroup;
                if (i4 >= linearLayoutArr.length) {
                    break;
                }
                linearLayoutArr[i4].setVisibility(0);
                i4++;
            }
            this.mDemoMobileSignal.setVisibility(8);
            this.mVpn.setVisibility(this.mVpnVisible ? 0 : 8);
            String str2 = "VISIBLE";
            if (DEBUG) {
                Object[] objArr = new Object[1];
                objArr[0] = this.mVpnVisible ? str2 : "GONE";
                Log.d("SignalClusterView", String.format("vpn: %s", objArr));
            }
            if (!this.mEthernetVisible || !ethernetEnable()) {
                this.mEthernet.setVisibility(8);
            } else {
                int i5 = this.mLastEthernetIconId;
                int i6 = this.mEthernetIconId;
                if (i5 != i6) {
                    setIconForView(this.mEthernet, i6);
                    this.mLastEthernetIconId = this.mEthernetIconId;
                }
                this.mEthernet.setContentDescription(this.mEthernetDescription);
                this.mEthernet.setVisibility(0);
            }
            if (DEBUG) {
                Object[] objArr2 = new Object[1];
                if (this.mEthernetVisible) {
                    str = str2;
                } else {
                    str = "GONE";
                }
                objArr2[0] = str;
                Log.d("SignalClusterView", String.format("ethernet: %s", objArr2));
            }
            if (this.mWifiVisible && !this.mWifiNoNetwork) {
                if (!(this.mWifiStrengthId == this.mLastWifiStrengthId && this.mWifiBadgeId == this.mLastWifiBadgeId)) {
                    int i7 = this.mWifiBadgeId;
                    if (i7 == -1) {
                        setIconForView(this.mWifi, this.mWifiStrengthId);
                    } else {
                        setBadgedWifiIconForView(this.mWifi, this.mWifiStrengthId, i7);
                    }
                    this.mLastWifiStrengthId = this.mWifiStrengthId;
                    this.mLastWifiBadgeId = this.mWifiBadgeId;
                }
                updateIcon(this.mWifi, this.mWifiStrengthId);
                ImageView imageView = this.mWifiActivity;
                if (this.mWifiIn || this.mWifiOut) {
                    i = 0;
                }
                imageView.setVisibility(i);
                if (this.mWifiIn && this.mWifiOut) {
                    this.mLastWifiActivityId = R.drawable.stat_sys_wifi_inout;
                } else if (this.mWifiIn) {
                    this.mLastWifiActivityId = R.drawable.stat_sys_wifi_in;
                } else if (this.mWifiOut) {
                    this.mLastWifiActivityId = R.drawable.stat_sys_wifi_out;
                }
                int i8 = this.mLastWifiActivityId;
                if (i8 != -1) {
                    this.mWifiActivity.setImageResource(Icons.get(Integer.valueOf(i8), DarkIconDispatcherHelper.inDarkMode(this.mTintArea, this.mWifiActivity, this.mDarkIntensity)));
                }
                this.mWifiLabel.setWifiLabel(false, this.mWifiName);
                this.mWifiGroup.setContentDescription(this.mWifiDescription);
                this.mWifiGroup.setVisibility(0);
            } else if (!this.mWifiVisible || !this.mWifiNoNetwork || (this.mNotchEar && !this.mWifiNoNetworkEnableInEar)) {
                this.mWifiLabel.setWifiLabel(true, this.mWifiName);
                this.mWifiGroup.setVisibility(8);
            } else {
                updateIcon(this.mWifi, this.mWifiStrengthId);
                this.mWifiActivity.setVisibility(4);
                this.mWifiLabel.setWifiLabel(false, this.mWifiName);
                this.mWifiGroup.setContentDescription(this.mWifiDescription);
                this.mWifiGroup.setVisibility(0);
            }
            if (DEBUG) {
                Object[] objArr3 = new Object[2];
                if (!this.mWifiVisible) {
                    str2 = "GONE";
                }
                objArr3[0] = str2;
                objArr3[1] = Integer.valueOf(this.mWifiStrengthId);
                Log.d("SignalClusterView", String.format("wifi: %s sig=%d", objArr3));
            }
            Iterator<PhoneState> it = this.mPhoneStates.iterator();
            boolean z = false;
            int i9 = 0;
            while (it.hasNext()) {
                PhoneState next = it.next();
                if (next.apply(z) && !z) {
                    i9 = next.mMobileTypeId;
                    z = true;
                }
            }
            if (this.mIsAirplaneMode) {
                int i10 = this.mLastAirplaneIconId;
                int i11 = this.mAirplaneIconId;
                if (i10 != i11) {
                    setIconForView(this.mAirplane, i11);
                    this.mLastAirplaneIconId = this.mAirplaneIconId;
                }
                this.mAirplane.setContentDescription(this.mAirplaneContentDescription);
                this.mAirplane.setVisibility(0);
            } else {
                this.mAirplane.setVisibility(8);
            }
            if (((!z || i9 == 0) && !this.mNoSimsVisible) || !this.mWifiVisible) {
                this.mWifiSignalSpacer.setVisibility(8);
            } else {
                this.mWifiSignalSpacer.setVisibility(0);
            }
            if (this.mNoSims != null) {
                if (this.mNoSimsVisible) {
                    if (this.mNoSimsIcon == 0) {
                        this.mNoSimsIcon = getNoSimIcon();
                    }
                    int i12 = this.mNoSimsIcon;
                    if (i12 != 0) {
                        this.mNoSims.setImageResource(i12);
                    }
                }
                ImageView imageView2 = this.mNoSims;
                if (this.mNoSimsVisible && !this.mIsAirplaneMode) {
                    i2 = 0;
                }
                imageView2.setVisibility(i2);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void updateDripVolte() {
        if (this.mIsDripType && Constants.IS_INTERNATIONAL) {
            Iterator<PhoneState> it = this.mPhoneStates.iterator();
            while (it.hasNext()) {
                PhoneState next = it.next();
                next.setIsImsRegisted(next.mIsImsRegistered);
            }
        }
    }

    private void setIconForView(ImageView imageView, int i) {
        if (i > 0) {
            setScaledIcon(imageView, imageView.getContext().getDrawable(i));
        }
    }

    private void setScaledIcon(ImageView imageView, Drawable drawable) {
        float f = this.mIconScaleFactor;
        if (f == 1.0f) {
            imageView.setImageDrawable(drawable);
        } else {
            imageView.setImageDrawable(new ScalingDrawableWrapper(drawable, f));
        }
    }

    private void setBadgedWifiIconForView(ImageView imageView, int i, int i2) {
        LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{imageView.getContext().getDrawable(i), imageView.getContext().getDrawable(i2)});
        layerDrawable.mutate().setTint(getColorAttr(imageView.getContext(), R.attr.singleToneColor));
        setScaledIcon(imageView, layerDrawable);
    }

    private static int getColorAttr(Context context, int i) {
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(new int[]{i});
        int color = obtainStyledAttributes.getColor(0, -1);
        obtainStyledAttributes.recycle();
        return color;
    }

    public void onDarkChanged(Rect rect, float f, int i) {
        boolean z = (f == this.mDarkIntensity && i == this.mIconTint && this.mTintArea.equals(rect)) ? false : true;
        this.mDarkIntensity = f;
        this.mIconTint = i;
        this.mTintArea.set(rect);
        if (z && isAttachedToWindow()) {
            applyIconTint();
        }
    }

    private void applyIconTint() {
        updateIcon(this.mAirplane, R.drawable.stat_sys_signal_flightmode);
        updateIcon(this.mNoSims, R.drawable.stat_sys_no_sim);
        updateIcon(this.mWifi, this.mDemoMode ? R.drawable.stat_sys_wifi_signal_4 : this.mWifiStrengthId);
        updateIcon(this.mSlaveWifi, this.mSlaveWifiStrengthId);
        updateIcon(this.mWifiApConnectMark, R.drawable.stat_sys_wifi_ap);
        updateIcon(this.mWifiAp, R.drawable.stat_sys_wifi_ap_on);
        updateIcon(this.mVowifi[0], this.mNetworkController.getVowifiDrawableId(0));
        updateIcon(this.mVowifi[1], this.mNetworkController.getVowifiDrawableId(1));
        updateIcon(this.mVpn, R.drawable.stat_sys_vpn);
        setTextColor(this.mWifiLabel);
        setTextColor(this.mWifiGenerationView);
        int i = this.mLastWifiActivityId;
        if (i != -1) {
            updateIcon(this.mWifiActivity, i);
        }
        updateIcon(this.mEthernet, R.drawable.stat_sys_ethernet);
        updateIcon(this.mDemoMobileSignal, R.drawable.stat_sys_signal_5);
        for (int i2 = 0; i2 < this.mPhoneStates.size(); i2++) {
            this.mPhoneStates.get(i2).setIconTint(this.mIconTint, this.mDarkIntensity, this.mTintArea);
        }
    }

    private String getWifiName(String str) {
        if (str == null) {
            return this.mContext.getString(R.string.status_bar_settings_signal_meter_wifi_nossid);
        }
        return removeDoubleQuotes(str);
    }

    private String removeDoubleQuotes(String str) {
        if (str == null) {
            return null;
        }
        int length = str.length();
        if (length > 1 && str.charAt(0) == '\"') {
            int i = length - 1;
            if (str.charAt(i) == '\"') {
                return str.substring(1, i);
            }
        }
        return str;
    }

    /* access modifiers changed from: protected */
    public void updateIcon(ImageView imageView, int i) {
        if (imageView != null && i != 0) {
            if (Util.showCtsSpecifiedColor()) {
                boolean inDarkMode = DarkIconDispatcherHelper.inDarkMode(this.mTintArea, imageView, this.mDarkIntensity);
                imageView.setImageDrawable(this.mContext.getDrawable(Icons.get(Integer.valueOf(i), inDarkMode)));
                if (this.mFilterColor == 0) {
                    this.mFilterColor = this.mContext.getColor(R.color.status_bar_icon_text_color_dark_mode_cts);
                }
                if (inDarkMode) {
                    imageView.setImageTintList(ColorStateList.valueOf(this.mFilterColor));
                } else {
                    imageView.setImageTintList((ColorStateList) null);
                }
            } else if (this.mDarkIconDispatcher.useTint()) {
                imageView.setImageDrawable(this.mContext.getDrawable(i));
                imageView.setImageTintList(ColorStateList.valueOf(DarkIconDispatcherHelper.getTint(this.mTintArea, imageView, this.mIconTint)));
            } else {
                imageView.setImageDrawable(this.mContext.getDrawable(Icons.get(Integer.valueOf(i), DarkIconDispatcherHelper.inDarkMode(this.mTintArea, imageView, this.mDarkIntensity))));
            }
        }
    }

    public void setTextColor(TextView textView) {
        Resources resources = getResources();
        boolean showCtsSpecifiedColor = Util.showCtsSpecifiedColor();
        int i = R.color.status_bar_textColor;
        if (showCtsSpecifiedColor) {
            if (DarkIconDispatcherHelper.inDarkMode(this.mTintArea, textView, this.mDarkIntensity)) {
                i = R.color.status_bar_icon_text_color_dark_mode_cts;
            }
            textView.setTextColor(resources.getColor(i));
        } else if (this.mDarkIconDispatcher.useTint()) {
            textView.setTextColor(DarkIconDispatcherHelper.getTint(this.mTintArea, textView, this.mIconTint));
        } else {
            if (DarkIconDispatcherHelper.inDarkMode(this.mTintArea, textView, this.mDarkIntensity)) {
                i = R.color.status_bar_textColor_darkmode;
            }
            textView.setTextColor(resources.getColor(i));
        }
    }

    public void dispatchDemoCommand(String str, Bundle bundle) {
        if (str.equals("enter")) {
            this.mDemoMode = true;
            apply();
        } else if (str.equals("exit")) {
            this.mDemoMode = false;
            apply();
        }
    }

    public void onCutoutTypeChanged() {
        post(new Runnable() {
            public void run() {
                if (SignalClusterView.this.isTypeChangeable) {
                    SignalClusterViewController unused = SignalClusterView.this.mController = StatusBarFactory.getInstance().getSignalClusterViewController(SignalClusterView.this.getContext());
                    SignalClusterView.this.refreshByType();
                }
            }
        });
    }

    public void setForceNormalType() {
        this.isTypeChangeable = false;
        this.mController = StatusBarFactory.getInstance().getSignalClusterViewController(StatusBarTypeController.CutoutType.NONE);
        refreshByType();
    }

    public void setCutoutType(StatusBarTypeController.CutoutType cutoutType) {
        this.mController = StatusBarFactory.getInstance().getSignalClusterViewController(cutoutType);
        refreshByType();
    }

    /* access modifiers changed from: private */
    public void refreshByType() {
        updateSwitches();
        updateNotchEar();
        applyIconTint();
        apply();
    }

    private void updateSwitches() {
        this.mNotchEarDualEnable = this.mController.isNotchEarDualEnable();
        this.mEthernetAble = this.mController.isEthernetAble();
        this.mIsDripType = this.mController.isDrip();
        this.mWifiNoNetworkEnableInEar = this.mController.isWifiNoNetworkEnableInEar();
        this.mVpnEnableInEar = this.mController.isVpnEnableInEar();
        this.mVoWifiEnableInEar = this.mController.isVoWifiEnableInEar();
    }

    /* access modifiers changed from: protected */
    public boolean couldHideVolte() {
        return Constants.IS_INTERNATIONAL && !this.mIsDripType;
    }

    /* access modifiers changed from: private */
    public boolean notchEarDualEnable() {
        return this.mNotchEarDualEnable;
    }

    private boolean ethernetEnable() {
        return this.mEthernetAble;
    }

    /* access modifiers changed from: private */
    public boolean isBuildTest() {
        return Build.IS_CM_CUSTOMIZATION_TEST || Build.IS_CT_CUSTOMIZATION_TEST || Build.IS_CU_CUSTOMIZATION_TEST;
    }

    /* access modifiers changed from: private */
    public boolean isCUBuildTEST() {
        return Build.IS_CU_CUSTOMIZATION_TEST;
    }

    protected class PhoneState {
        public boolean mActivityIn;
        public boolean mActivityOut;
        /* access modifiers changed from: private */
        public int mDataActivityId = 0;
        private boolean mDataConnected;
        private boolean mIsDefaultDataSim;
        public boolean mIsImsRegistered;
        /* access modifiers changed from: private */
        public boolean mIsMobileTypeIconWide;
        private boolean mIsSmallMode = true;
        private int mLastMobileStrengthId = -1;
        /* access modifiers changed from: private */
        public int mLastMobileTypeId = -1;
        private String mLastShownType;
        private FrameLayout mMobileContainerLeft;
        /* access modifiers changed from: private */
        public String mMobileDescription;
        protected ViewGroup mMobileGroup;
        private ImageView mMobileInOut;
        private ImageView mMobileInOutBottom;
        private int mMobileInOutId = 0;
        private ImageView mMobileRoaming;
        private ImageView mMobileSignal;
        public int mMobileStrengthId = 0;
        /* access modifiers changed from: private */
        public TextView mMobileType;
        /* access modifiers changed from: private */
        public String mMobileTypeDescription;
        public int mMobileTypeId = 0;
        private ImageView mMobileTypeImage;
        public boolean mMobileVisible = false;
        private String mMobileVoiceLabel;
        private ImageView mNotchVolte;
        public boolean mRoaming;
        public final int mSlot;
        private ImageView mSmallRoam;
        private ImageView mSpeechHd;
        /* access modifiers changed from: private */
        public int mStackedDataId = 0;
        /* access modifiers changed from: private */
        public int mStackedVoiceId = 0;
        private ImageView mVolte;
        private ImageView mVolteNoService;
        private ImageView mWcdmaCardSlot;

        public PhoneState(int i, Context context) {
            setViews((ViewGroup) LayoutInflater.from(context).inflate(R.layout.mobile_signal_group, (ViewGroup) null));
            this.mSlot = i;
        }

        public void setViews(ViewGroup viewGroup) {
            this.mMobileGroup = viewGroup;
            this.mMobileSignal = (ImageView) viewGroup.findViewById(R.id.mobile_signal);
            this.mMobileContainerLeft = (FrameLayout) viewGroup.findViewById(R.id.mobile_container_left);
            this.mMobileType = (TextView) viewGroup.findViewById(R.id.mobile_type);
            this.mMobileTypeImage = (ImageView) viewGroup.findViewById(R.id.mobile_type_image);
            this.mVolte = (ImageView) viewGroup.findViewById(R.id.volte);
            this.mVolteNoService = (ImageView) viewGroup.findViewById(R.id.volte_no_service);
            this.mNotchVolte = (ImageView) viewGroup.findViewById(R.id.notch_volte);
            this.mSmallRoam = (ImageView) viewGroup.findViewById(R.id.small_roam);
            this.mSpeechHd = (ImageView) viewGroup.findViewById(R.id.speech_hd);
            this.mMobileRoaming = (ImageView) viewGroup.findViewById(R.id.mobile_roaming);
            this.mMobileInOut = (ImageView) viewGroup.findViewById(R.id.mobile_inout);
            this.mMobileInOutBottom = (ImageView) viewGroup.findViewById(R.id.mobile_inout_bottom);
            this.mWcdmaCardSlot = (ImageView) viewGroup.findViewById(R.id.card_slot);
            this.mMobileType.setTypeface(Typeface.create("sans-serif-semibold", 1));
            if (!Constants.IS_INTERNATIONAL || SignalClusterView.this.mShowHDIcon) {
                SignalClusterView.this.updateIcon(this.mVolte, R.drawable.stat_sys_signal_hd_big);
            } else {
                SignalClusterView signalClusterView = SignalClusterView.this;
                signalClusterView.updateIcon(this.mVolte, signalClusterView.mNetworkController.getVolteDrawableId(this.mSlot));
            }
            SignalClusterView signalClusterView2 = SignalClusterView.this;
            signalClusterView2.updateIcon(signalClusterView2.mVowifi[0], signalClusterView2.mNetworkController.getVowifiDrawableId(0));
            SignalClusterView signalClusterView3 = SignalClusterView.this;
            signalClusterView3.updateIcon(signalClusterView3.mVowifi[1], signalClusterView3.mNetworkController.getVowifiDrawableId(1));
        }

        public boolean apply(boolean z) {
            SignalClusterView signalClusterView = SignalClusterView.this;
            boolean unused = signalClusterView.mNotchEarDual = signalClusterView.mPhoneStates.size() == 2 && SignalClusterView.this.mNotchEar && SignalClusterView.this.notchEarDualEnable();
            int i = 8;
            if (!SignalClusterView.this.mNotchEarDual || !SignalClusterView.this.notchEarDualEnable()) {
                SignalClusterView.this.mSignalDualNotchGroup.setVisibility(8);
            } else {
                this.mMobileGroup.setVisibility(8);
                if (SignalClusterView.this.mSignalDualNotchGroup.getVisibility() != 0) {
                    SignalClusterView.this.mSignalDualNotchGroup.setVisibility(0);
                }
            }
            if (!this.mMobileVisible || SignalClusterView.this.mIsAirplaneMode) {
                SignalClusterView.this.mSignalDualNotchGroup.setVisibility(8);
                this.mMobileGroup.setVisibility(8);
            } else {
                String networkTypeName = TelephonyIcons.getNetworkTypeName(this.mMobileTypeId, this.mSlot);
                this.mDataConnected = this.mDataActivityId != 0;
                SignalClusterView signalClusterView2 = SignalClusterView.this;
                signalClusterView2.mDataConnectedStatus[this.mSlot] = this.mDataConnected;
                if (!signalClusterView2.mNotchEarDual) {
                    int i2 = this.mLastMobileStrengthId;
                    int i3 = this.mMobileStrengthId;
                    if (i2 != i3) {
                        SignalClusterView.this.updateIcon(this.mMobileSignal, i3);
                        this.mLastMobileStrengthId = this.mMobileStrengthId;
                    }
                    boolean z2 = ((SignalClusterView.this.mWifiVisible && !SignalClusterView.this.mWifiNoNetwork && !SignalClusterView.this.mNetworkController.isMobileTypeShownWhenWifiOn(this.mSlot)) || isHideMobile(this.mDataConnected)) && !SignalClusterView.this.isBuildTest();
                    boolean is4GLTE = is4GLTE(networkTypeName);
                    boolean z3 = getFiveGDrawable() > 0;
                    boolean z4 = this.mMobileTypeId == 0;
                    updateMobileTypeImage();
                    if (z2) {
                        this.mMobileContainerLeft.setVisibility(8);
                        this.mMobileInOut.setVisibility(8);
                        this.mMobileTypeImage.setVisibility(8);
                    } else if (is4GLTE || z3) {
                        this.mMobileInOut.setVisibility(0);
                        this.mMobileTypeImage.setVisibility(0);
                        this.mMobileContainerLeft.setVisibility(8);
                    } else {
                        this.mMobileInOut.setVisibility(8);
                        this.mMobileTypeImage.setVisibility(8);
                        if (!z4 || !TextUtils.isEmpty(this.mMobileVoiceLabel)) {
                            this.mMobileContainerLeft.setVisibility(0);
                            if (z4 && !TextUtils.isEmpty(this.mMobileVoiceLabel)) {
                                networkTypeName = this.mMobileVoiceLabel;
                            }
                            this.mMobileType.setText(networkTypeName);
                            updateMobileTypeLayout(networkTypeName);
                        } else {
                            this.mMobileContainerLeft.setVisibility(8);
                        }
                    }
                    if (this.mActivityIn && this.mActivityOut) {
                        this.mMobileInOutId = R.drawable.stat_sys_signal_inout_left;
                    } else if (!this.mActivityIn && this.mActivityOut) {
                        this.mMobileInOutId = R.drawable.stat_sys_signal_out_left;
                    } else if (!this.mActivityIn || this.mActivityOut) {
                        this.mMobileInOutId = R.drawable.stat_sys_signal_data_left;
                    } else {
                        this.mMobileInOutId = R.drawable.stat_sys_signal_in_left;
                    }
                    ImageView imageView = (is4GLTE || z3) ? this.mMobileInOut : this.mMobileInOutBottom;
                    SignalClusterView.this.updateIcon(imageView, this.mMobileInOutId);
                    imageView.setVisibility(((!SignalClusterView.this.mWifiVisible || SignalClusterView.this.mWifiNoNetwork) && this.mDataConnected) ? 0 : 8);
                    this.mIsSmallMode = z2 || is4GLTE || z3;
                    this.mSmallRoam.setVisibility((!this.mIsSmallMode || !this.mRoaming) ? 8 : 0);
                    ImageView imageView2 = this.mMobileRoaming;
                    if (!this.mIsSmallMode && this.mRoaming) {
                        i = 0;
                    }
                    imageView2.setVisibility(i);
                    setIsImsRegisted(this.mIsImsRegistered);
                    this.mMobileGroup.setVisibility(0);
                } else {
                    boolean updateMobileType = updateMobileType(networkTypeName);
                    int i4 = this.mSlot;
                    if (i4 == 0) {
                        SignalClusterView signalClusterView3 = SignalClusterView.this;
                        signalClusterView3.updateIcon(signalClusterView3.mSignalDualNotchMobile, Icons.getSignalHalfId(Integer.valueOf(this.mMobileStrengthId)));
                    } else if (i4 == 1) {
                        SignalClusterView signalClusterView4 = SignalClusterView.this;
                        signalClusterView4.updateIcon(signalClusterView4.mSignalDualNotchMobile2, Icons.getSignalHalfId(Integer.valueOf(this.mMobileStrengthId)));
                    }
                    SignalClusterView signalClusterView5 = SignalClusterView.this;
                    boolean[] zArr = signalClusterView5.mDataConnectedStatus;
                    if (!zArr[0] && !zArr[1]) {
                        signalClusterView5.mSignalDualNotchMobileInout.setVisibility(8);
                        if (this.mIsDefaultDataSim) {
                            SignalClusterView signalClusterView6 = SignalClusterView.this;
                            signalClusterView6.mSignalDualNotchMobileType.setVisibility(((!signalClusterView6.mWifiVisible || SignalClusterView.this.mWifiNoNetwork || SignalClusterView.this.isBuildTest()) && this.mMobileTypeId != 0 && !isHideMobile(false)) ? 0 : 8);
                            SignalClusterView.this.mSignalDualNotchMobileUpgrade.setVisibility(8);
                            if ((!SignalClusterView.this.mWifiVisible || SignalClusterView.this.isBuildTest()) && SignalClusterView.this.mSignalDualNotchMobileType.getVisibility() == 8 && !TextUtils.isEmpty(this.mMobileVoiceLabel) && !isHideMobile(false)) {
                                SignalClusterView.this.mSignalDualNotchMobileVoice.setText(this.mMobileVoiceLabel);
                                SignalClusterView.this.mSignalDualNotchMobileVoice.setVisibility(0);
                            } else {
                                SignalClusterView.this.mSignalDualNotchMobileVoice.setVisibility(8);
                            }
                        }
                    } else if (this.mDataConnected) {
                        if (this.mMobileTypeId == 0) {
                            SignalClusterView signalClusterView7 = SignalClusterView.this;
                            signalClusterView7.mSignalDualNotchMobileType.setVisibility(((!signalClusterView7.mWifiVisible || SignalClusterView.this.mWifiNoNetwork || SignalClusterView.this.isBuildTest()) && this.mMobileTypeId != 0) ? 0 : 8);
                            SignalClusterView.this.mSignalDualNotchMobileUpgrade.setVisibility(8);
                            if ((!SignalClusterView.this.mWifiVisible || SignalClusterView.this.isBuildTest()) && SignalClusterView.this.mSignalDualNotchMobileType.getVisibility() == 8 && !TextUtils.isEmpty(this.mMobileVoiceLabel)) {
                                SignalClusterView.this.mSignalDualNotchMobileVoice.setText(this.mMobileVoiceLabel);
                                SignalClusterView.this.mSignalDualNotchMobileVoice.setVisibility(0);
                            } else {
                                SignalClusterView.this.mSignalDualNotchMobileVoice.setVisibility(8);
                            }
                        } else {
                            SignalClusterView.this.mSignalDualNotchMobileVoice.setVisibility(8);
                            SignalClusterView signalClusterView8 = SignalClusterView.this;
                            signalClusterView8.mSignalDualNotchMobileType.setVisibility(((!signalClusterView8.mWifiVisible || SignalClusterView.this.mWifiNoNetwork || SignalClusterView.this.isBuildTest()) && this.mMobileTypeId != 0) ? 0 : 8);
                            SignalClusterView signalClusterView9 = SignalClusterView.this;
                            signalClusterView9.mSignalDualNotchMobileUpgrade.setVisibility(updateMobileType ? signalClusterView9.mSignalDualNotchMobileType.getVisibility() : 8);
                            SignalClusterView signalClusterView10 = SignalClusterView.this;
                            signalClusterView10.updateIcon(signalClusterView10.mSignalDualNotchMobileUpgrade, R.drawable.stat_sys_signal_upgrade);
                        }
                        if (this.mActivityIn && this.mActivityOut) {
                            this.mMobileInOutId = R.drawable.stat_sys_signal_dual_inout;
                        } else if (!this.mActivityIn && this.mActivityOut) {
                            this.mMobileInOutId = R.drawable.stat_sys_signal_dual_out;
                        } else if (!this.mActivityIn || this.mActivityOut) {
                            this.mMobileInOutId = R.drawable.stat_sys_signal_dual_data;
                        } else {
                            this.mMobileInOutId = R.drawable.stat_sys_signal_dual_in;
                        }
                        SignalClusterView signalClusterView11 = SignalClusterView.this;
                        signalClusterView11.updateIcon(signalClusterView11.mSignalDualNotchMobileInout, this.mMobileInOutId);
                        SignalClusterView signalClusterView12 = SignalClusterView.this;
                        ImageView imageView3 = signalClusterView12.mSignalDualNotchMobileInout;
                        if ((!signalClusterView12.mWifiVisible || SignalClusterView.this.mWifiNoNetwork) && this.mDataConnected) {
                            i = 0;
                        }
                        imageView3.setVisibility(i);
                    }
                }
                SignalClusterView.this.mDualSignalDescription[this.mSlot] = this.mMobileTypeDescription + " " + this.mMobileDescription;
                this.mMobileGroup.setContentDescription(SignalClusterView.this.mDualSignalDescription[this.mSlot]);
                SignalClusterView.this.mSignalDualNotchGroup.setContentDescription(SignalClusterView.this.mDualSignalDescription[0] + " " + SignalClusterView.this.mDualSignalDescription[1]);
            }
            if (SignalClusterView.DEBUG) {
                Object[] objArr = new Object[3];
                objArr[0] = this.mMobileVisible ? "VISIBLE" : "GONE";
                objArr[1] = Integer.valueOf(this.mMobileStrengthId);
                objArr[2] = Integer.valueOf(this.mMobileTypeId);
                Log.d("SignalClusterView", String.format("mobile: %s sig=%d typ=%d", objArr));
            }
            return this.mMobileVisible;
        }

        private void updateMobileTypeImage() {
            Log.d("SignalClusterView", "updateMobileTypeImage  " + getFiveGDrawable());
            if (getFiveGDrawable() > 0) {
                SignalClusterView.this.updateIcon(this.mMobileTypeImage, getFiveGDrawable());
            } else {
                SignalClusterView.this.updateIcon(this.mMobileTypeImage, R.drawable.stat_sys_signal_4g_lte);
            }
        }

        public void setIsImsRegisted(boolean z) {
            int i = 0;
            boolean z2 = !SignalClusterView.this.mHideVolte && !SignalClusterView.this.mNetworkController.hideVolteForOperation(this.mSlot) && z && !this.mRoaming;
            if (Constants.IS_INTERNATIONAL) {
                if (!z2 || (SignalClusterView.this.mNotchEar && (!SignalClusterView.this.mIsDripType || SignalClusterView.this.mWifiVisible))) {
                    i = 8;
                }
                if (i == 0 && !SignalClusterView.this.mShowHDIcon) {
                    SignalClusterView signalClusterView = SignalClusterView.this;
                    signalClusterView.updateIcon(this.mVolte, signalClusterView.mNetworkController.getVolteDrawableId(this.mSlot));
                }
                this.mVolte.setVisibility(i);
                return;
            }
            this.mNotchVolte.setVisibility((!this.mIsSmallMode || !z2) ? 8 : 0);
            ImageView imageView = this.mVolte;
            if (this.mIsSmallMode || !z2) {
                i = 8;
            }
            imageView.setVisibility(i);
        }

        private boolean isHideMobile(boolean z) {
            return Constants.IS_INTERNATIONAL && !z;
        }

        public void setVolteNoService(boolean z) {
            this.mVolteNoService.setVisibility(((!SignalClusterView.this.mNotchEar || (SignalClusterView.this.mIsDripType && Build.IS_CT_CUSTOMIZATION_TEST)) && z) ? 0 : 8);
        }

        public void setSpeechHd(boolean z) {
            this.mSpeechHd.setVisibility((!z || (SignalClusterView.this.mNotchEar && !SignalClusterView.this.isCUBuildTEST())) ? 8 : 0);
        }

        public void setNetworkNameVoice(String str) {
            this.mMobileVoiceLabel = str;
        }

        public void setIsDefaultDataSim(boolean z) {
            this.mIsDefaultDataSim = z;
        }

        public void populateAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
            ViewGroup viewGroup;
            if (this.mMobileVisible && (viewGroup = this.mMobileGroup) != null && viewGroup.getContentDescription() != null) {
                accessibilityEvent.getText().add(this.mMobileGroup.getContentDescription());
            }
        }

        public void setIconTint(int i, float f, Rect rect) {
            setTextColor();
            if (!SignalClusterView.this.mNotchEarDual) {
                SignalClusterView.this.updateIcon(this.mMobileRoaming, R.drawable.stat_sys_data_connected_roam);
                SignalClusterView.this.updateIcon(this.mMobileInOut, this.mMobileInOutId);
                SignalClusterView.this.updateIcon(this.mMobileInOutBottom, this.mMobileInOutId);
                SignalClusterView.this.updateIcon(this.mMobileSignal, this.mMobileStrengthId);
                SignalClusterView.this.updateIcon(this.mNotchVolte, R.drawable.stat_sys_signal_hd_notch);
                SignalClusterView.this.updateIcon(this.mSmallRoam, R.drawable.stat_sys_data_connected_roam_small);
                if (!Constants.IS_INTERNATIONAL || SignalClusterView.this.mShowHDIcon) {
                    SignalClusterView.this.updateIcon(this.mVolte, R.drawable.stat_sys_signal_hd_big);
                } else {
                    SignalClusterView signalClusterView = SignalClusterView.this;
                    signalClusterView.updateIcon(this.mVolte, signalClusterView.mNetworkController.getVolteDrawableId(this.mSlot));
                }
                SignalClusterView.this.updateIcon(this.mSpeechHd, R.drawable.stat_sys_speech_hd);
                SignalClusterView.this.updateIcon(this.mVolteNoService, R.drawable.stat_sys_volte_no_service);
                updateMobileTypeImage();
                return;
            }
            int i2 = this.mSlot;
            if (i2 == 0) {
                SignalClusterView signalClusterView2 = SignalClusterView.this;
                signalClusterView2.updateIcon(signalClusterView2.mSignalDualNotchMobile, Icons.getSignalHalfId(Integer.valueOf(this.mMobileStrengthId)));
            } else if (i2 == 1) {
                SignalClusterView signalClusterView3 = SignalClusterView.this;
                signalClusterView3.updateIcon(signalClusterView3.mSignalDualNotchMobile2, Icons.getSignalHalfId(Integer.valueOf(this.mMobileStrengthId)));
            }
            if (this.mDataConnected) {
                SignalClusterView signalClusterView4 = SignalClusterView.this;
                signalClusterView4.updateIcon(signalClusterView4.mSignalDualNotchMobileInout, this.mMobileInOutId);
                SignalClusterView signalClusterView5 = SignalClusterView.this;
                signalClusterView5.updateIcon(signalClusterView5.mSignalDualNotchMobileUpgrade, R.drawable.stat_sys_signal_upgrade);
            }
        }

        public void setTextColor() {
            if (!SignalClusterView.this.mNotchEarDual) {
                SignalClusterView.this.setTextColor(this.mMobileType);
                return;
            }
            SignalClusterView signalClusterView = SignalClusterView.this;
            signalClusterView.setTextColor(signalClusterView.mSignalDualNotchMobileType);
            if (!Util.showCtsSpecifiedColor()) {
                SignalClusterView signalClusterView2 = SignalClusterView.this;
                signalClusterView2.setTextColor(signalClusterView2.mSignalDualNotchMobileVoice);
            }
        }

        private boolean updateMobileType(String str) {
            boolean is4Gplus = is4Gplus(str);
            if (is4Gplus) {
                str = "4G";
            }
            if (SignalClusterView.this.mNotchEarDual) {
                return updateMobileTypeForNotchEarDual(is4Gplus, str);
            }
            updateMobileTypeForNormal(is4Gplus, str);
            return is4Gplus;
        }

        private boolean is4Gplus(String str) {
            return str.equals("4G+");
        }

        private boolean updateMobileTypeForNotchEarDual(boolean z, String str) {
            boolean z2;
            if (!this.mDataConnected) {
                boolean[] zArr = SignalClusterView.this.mDataConnectedStatus;
                if (zArr[0] || zArr[1] || !this.mIsDefaultDataSim) {
                    z2 = false;
                    boolean z3 = !str.equals(SignalClusterView.this.mSignalDualNotchMobileType.getText());
                    if (z2 && z3) {
                        this.mLastMobileTypeId = this.mMobileTypeId;
                        SignalClusterView.this.mSignalDualNotchMobileType.setText(str);
                    }
                    if (!z2 && z) {
                        return true;
                    }
                }
            }
            z2 = true;
            boolean z32 = !str.equals(SignalClusterView.this.mSignalDualNotchMobileType.getText());
            this.mLastMobileTypeId = this.mMobileTypeId;
            SignalClusterView.this.mSignalDualNotchMobileType.setText(str);
            return !z2 ? false : false;
        }

        private boolean updateMobileTypeForNormal(boolean z, String str) {
            if (this.mLastMobileTypeId != this.mMobileTypeId || (z && !str.equals(this.mMobileType.getText()))) {
                this.mLastMobileTypeId = this.mMobileTypeId;
                this.mMobileType.setText(str);
            }
            return z;
        }

        private boolean is4GLTE(String str) {
            return "4G LTE".equals(str);
        }

        private int getFiveGDrawable() {
            return SignalClusterView.this.mFiveGController.getFiveGDrawable(this.mSlot);
        }

        private void updateMobileTypeLayout(String str) {
            if (str != null && !str.equals(this.mLastShownType)) {
                this.mLastShownType = str;
                TextPaint paint = this.mMobileType.getPaint();
                Paint.FontMetrics fontMetrics = paint.getFontMetrics();
                int round = Math.round(fontMetrics.bottom - fontMetrics.top);
                int round2 = Math.round(paint.measureText(str));
                int dimensionPixelSize = SignalClusterView.this.mContext.getResources().getDimensionPixelSize(R.dimen.status_bar_mobile_type_margin);
                FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) this.mMobileType.getLayoutParams();
                layoutParams.topMargin = (round / 2) - dimensionPixelSize;
                this.mMobileType.setLayoutParams(layoutParams);
                LinearLayout.LayoutParams layoutParams2 = (LinearLayout.LayoutParams) this.mMobileContainerLeft.getLayoutParams();
                layoutParams2.rightMargin = ((-round2) / 2) - dimensionPixelSize;
                this.mMobileContainerLeft.setLayoutParams(layoutParams2);
                FrameLayout.LayoutParams layoutParams3 = (FrameLayout.LayoutParams) this.mMobileInOutBottom.getLayoutParams();
                layoutParams3.rightMargin = (round2 / 2) + dimensionPixelSize + SignalClusterView.this.mContext.getResources().getDimensionPixelSize(R.dimen.status_bar_signal_type_margin_right);
                this.mMobileInOutBottom.setLayoutParams(layoutParams3);
            }
        }
    }
}
