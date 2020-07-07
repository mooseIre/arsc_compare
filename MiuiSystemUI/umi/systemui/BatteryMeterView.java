package com.android.systemui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.keyguard.charge.BatteryStatus;
import com.android.systemui.miui.statusbar.BatteryMeterIconView;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.Icons;
import com.android.systemui.statusbar.phone.BatteryIcon;
import com.android.systemui.statusbar.phone.StatusBarTypeController;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.statusbar.policy.DarkIconDispatcher;
import com.android.systemui.statusbar.policy.DarkIconDispatcherHelper;
import com.android.systemui.util.DisableStateTracker;

public class BatteryMeterView extends LinearLayout implements BatteryController.BatteryStateChangeCallback, DarkIconDispatcher.DarkReceiver, ConfigurationController.ConfigurationListener, StatusBarTypeController.StatusBarTypeChangeListener {
    private ImageView mBatteryChargingInView;
    private final ImageView mBatteryChargingView;
    private BatteryController mBatteryController;
    private FrameLayout mBatteryDigitalView;
    /* access modifiers changed from: private */
    public final BatteryMeterIconView mBatteryIconView;
    private BatteryMeterViewDelegate mBatteryMeterViewDelegate;
    private TextView mBatteryPercentMarkView;
    private TextView mBatteryPercentView;
    private int mBatteryStyle;
    private int[] mBatteryTextColors;
    private TextView mBatteryTextDigitView;
    /* access modifiers changed from: private */
    public boolean mCharging;
    private StatusBarTypeController.CutoutType mCutoutType;
    private DarkIconDispatcher mDarkIconDispatcher;
    private float mDarkIntensity;
    private boolean mDemoMode;
    private Typeface mDigitTypeface;
    private boolean mDisabled;
    /* access modifiers changed from: private */
    public boolean mExtremePowerSave;
    /* access modifiers changed from: private */
    public boolean mForceShowDigit;
    /* access modifiers changed from: private */
    public int mIconId;
    private boolean mInHeader;
    KeyguardUpdateMonitorCallback mKeyguardUpdateMonitorCallback;
    /* access modifiers changed from: private */
    public int mLevel;
    private Typeface mMarkTypeface;
    private boolean mNortchEar;
    /* access modifiers changed from: private */
    public boolean mPowerSave;
    /* access modifiers changed from: private */
    public boolean mQuickCharging;
    /* access modifiers changed from: private */
    public int mQuickCharingStatus;
    private boolean mShowBatteryDigitFull;
    private boolean mShowPercent;
    private Rect mTintArea;
    private int mTintColor;
    private KeyguardUpdateMonitor mUpdateMonitor;
    private boolean mUseLegacyDrawable;

    public interface BatteryMeterViewDelegate {
        void onNumberToIconChanged(boolean z);
    }

    public boolean hasOverlappingRendering() {
        return false;
    }

    public void onBatteryLevelChanged(int i, boolean z, boolean z2) {
    }

    static /* synthetic */ int access$972(BatteryMeterView batteryMeterView, int i) {
        int i2 = i & batteryMeterView.mQuickCharingStatus;
        batteryMeterView.mQuickCharingStatus = i2;
        return i2;
    }

    static /* synthetic */ int access$976(BatteryMeterView batteryMeterView, int i) {
        int i2 = i | batteryMeterView.mQuickCharingStatus;
        batteryMeterView.mQuickCharingStatus = i2;
        return i2;
    }

    public BatteryMeterView(Context context) {
        this(context, (AttributeSet) null, 0);
    }

    public BatteryMeterView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public BatteryMeterView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mBatteryTextColors = new int[7];
        this.mUseLegacyDrawable = false;
        this.mIconId = R.raw.stat_sys_battery;
        this.mTintArea = new Rect();
        this.mQuickCharging = false;
        this.mForceShowDigit = false;
        this.mNortchEar = false;
        this.mInHeader = false;
        this.mBatteryStyle = 0;
        this.mKeyguardUpdateMonitorCallback = new KeyguardUpdateMonitorCallback() {
            public void onRefreshBatteryInfo(BatteryStatus batteryStatus) {
                super.onRefreshBatteryInfo(batteryStatus);
                boolean isCharging = batteryStatus.isCharging();
                boolean z = isCharging && (batteryStatus.isQuickCharge() || batteryStatus.isSuperQuickCharge());
                int level = batteryStatus.getLevel();
                if (isCharging != BatteryMeterView.this.mCharging) {
                    boolean unused = BatteryMeterView.this.mCharging = isCharging;
                    BatteryIcon.getInstance(BatteryMeterView.this.mContext).clear();
                    BatteryMeterView.this.mBatteryIconView.update(BatteryMeterView.this.mLevel, BatteryMeterView.this.mIconId, BatteryMeterView.this.mForceShowDigit, BatteryMeterView.this.mQuickCharging, BatteryMeterView.this.mCharging, BatteryMeterView.this.mPowerSave || BatteryMeterView.this.mExtremePowerSave, false);
                }
                if (!isCharging) {
                    boolean unused2 = BatteryMeterView.this.mQuickCharging = false;
                    int unused3 = BatteryMeterView.this.mQuickCharingStatus = 0;
                } else if (z) {
                    BatteryMeterView.access$976(BatteryMeterView.this, 1);
                } else {
                    BatteryMeterView.access$972(BatteryMeterView.this, -2);
                }
                if (BatteryMeterView.this.mIconId == BatteryMeterView.this.getIconId() && level == BatteryMeterView.this.mLevel) {
                    if (BatteryMeterView.this.mQuickCharging == (BatteryMeterView.this.mQuickCharingStatus > 0)) {
                        return;
                    }
                }
                BatteryMeterView batteryMeterView = BatteryMeterView.this;
                boolean unused4 = batteryMeterView.mQuickCharging = batteryMeterView.mQuickCharingStatus > 0;
                int unused5 = BatteryMeterView.this.mLevel = level;
                boolean unused6 = BatteryMeterView.this.mCharging = isCharging;
                BatteryMeterView batteryMeterView2 = BatteryMeterView.this;
                int unused7 = batteryMeterView2.mIconId = batteryMeterView2.getIconId();
                BatteryMeterView.this.mBatteryIconView.setContentDescription(BatteryMeterView.this.getContext().getString(isCharging ? R.string.accessibility_battery_level_charging : R.string.accessibility_battery_level, new Object[]{Integer.valueOf(level)}));
                BatteryMeterView.this.update();
            }
        };
        this.mUpdateMonitor = KeyguardUpdateMonitor.getInstance(this.mContext);
        this.mDigitTypeface = Typeface.create("sans-serif-medium", 0);
        this.mMarkTypeface = Typeface.create("mipro-bold", 1);
        setOrientation(0);
        setGravity(8388627);
        addOnAttachStateChangeListener(new DisableStateTracker(0, 2));
        this.mBatteryDigitalView = (FrameLayout) LayoutInflater.from(context).inflate(R.layout.battery_digital_view, (ViewGroup) null);
        this.mBatteryIconView = (BatteryMeterIconView) this.mBatteryDigitalView.findViewById(R.id.battery_image);
        this.mBatteryChargingInView = (ImageView) this.mBatteryDigitalView.findViewById(R.id.battery_charge_image);
        ViewGroup.LayoutParams layoutParams = this.mBatteryIconView.getLayoutParams();
        layoutParams.width = -2;
        this.mBatteryIconView.setLayoutParams(layoutParams);
        this.mBatteryTextDigitView = (TextView) this.mBatteryDigitalView.findViewById(R.id.battery_digit);
        this.mBatteryTextDigitView.setTypeface(this.mDigitTypeface);
        addView(this.mBatteryDigitalView);
        this.mBatteryChargingView = new ImageView(context);
        addView(this.mBatteryChargingView, new LinearLayout.LayoutParams(-2, -1));
        this.mDarkIconDispatcher = (DarkIconDispatcher) Dependency.get(DarkIconDispatcher.class);
        onDarkChanged(new Rect(), 0.0f, -1);
        updateResources();
    }

    private void updateResources() {
        Resources resources = this.mContext.getResources();
        this.mBatteryTextColors[0] = resources.getColor(R.color.status_bar_textColor);
        this.mBatteryTextColors[1] = resources.getColor(R.color.status_bar_textColor_darkmode);
        this.mBatteryTextColors[2] = resources.getColor(R.color.status_bar_battery_digit_textColor);
        this.mBatteryTextColors[3] = resources.getColor(R.color.status_bar_battery_digit_textColor_darkmode);
        this.mBatteryTextColors[4] = resources.getColor(R.color.status_bar_battery_power_save_digit_textColor);
        this.mBatteryTextColors[5] = resources.getColor(R.color.status_bar_battery_power_save_digit_textColor_darkmode);
        this.mBatteryTextColors[6] = resources.getColor(R.color.status_bar_icon_text_color_dark_mode_cts);
        this.mUseLegacyDrawable = resources.getBoolean(R.bool.battery_meter_use_legacy_drawable);
        this.mBatteryIconView.setUseLegacyDrawable(this.mUseLegacyDrawable);
        this.mBatteryIconView.setup();
        this.mShowBatteryDigitFull = resources.getBoolean(R.bool.show_battery_digit_full);
        TextView textView = this.mBatteryTextDigitView;
        if (textView != null) {
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) textView.getLayoutParams();
            layoutParams.rightMargin = resources.getDimensionPixelSize(R.dimen.battery_meter_progress_center_left_offset);
            this.mBatteryTextDigitView.setLayoutParams(layoutParams);
        }
        FrameLayout.LayoutParams layoutParams2 = (FrameLayout.LayoutParams) this.mBatteryChargingInView.getLayoutParams();
        layoutParams2.rightMargin = resources.getDimensionPixelSize(R.dimen.battery_meter_progress_center_left_offset);
        this.mBatteryChargingInView.setLayoutParams(layoutParams2);
    }

    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mBatteryController = (BatteryController) Dependency.get(BatteryController.class);
        this.mBatteryController.addCallback(this);
        this.mUpdateMonitor.registerCallback(this.mKeyguardUpdateMonitorCallback);
        ((StatusBarTypeController) Dependency.get(StatusBarTypeController.class)).addCallback(this);
        updateViews();
        ((ConfigurationController) Dependency.get(ConfigurationController.class)).addCallback(this);
    }

    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ((StatusBarTypeController) Dependency.get(StatusBarTypeController.class)).removeCallback(this);
        this.mBatteryController.removeCallback(this);
        this.mUpdateMonitor.removeCallback(this.mKeyguardUpdateMonitorCallback);
        ((ConfigurationController) Dependency.get(ConfigurationController.class)).removeCallback(this);
    }

    public void onBatteryStyleChanged(int i) {
        if (((StatusBarTypeController) Dependency.get(StatusBarTypeController.class)).hasCutout() && i == 2) {
            i = 0;
        }
        boolean z = true;
        this.mForceShowDigit = i == 1;
        if (i != 3 || (this.mNortchEar && !isWideNotchEar())) {
            z = false;
        }
        this.mShowPercent = z;
        this.mBatteryStyle = i;
        if (i == 2) {
            FrameLayout frameLayout = this.mBatteryDigitalView;
            if (frameLayout != null) {
                frameLayout.setVisibility(8);
            }
        } else {
            FrameLayout frameLayout2 = this.mBatteryDigitalView;
            if (frameLayout2 != null) {
                frameLayout2.setVisibility(0);
            }
            setDigitViewTextColor();
        }
        this.mIconId = getIconId();
        updateBatteryChargingIcon();
        update();
    }

    public void onPowerSaveChanged(boolean z) {
        this.mPowerSave = z;
        if (this.mIconId != getIconId()) {
            this.mIconId = getIconId();
            this.mBatteryIconView.setContentDescription(getContext().getString(this.mCharging ? R.string.accessibility_battery_level_charging : R.string.accessibility_battery_level, new Object[]{Integer.valueOf(this.mLevel)}));
            setDigitViewTextColor();
            update();
        }
    }

    public void onExtremePowerSaveChanged(boolean z) {
        this.mExtremePowerSave = z;
        if (this.mIconId != getIconId()) {
            this.mIconId = getIconId();
            this.mBatteryIconView.setContentDescription(getContext().getString(this.mCharging ? R.string.accessibility_battery_level_charging : R.string.accessibility_battery_level, new Object[]{Integer.valueOf(this.mLevel)}));
            setDigitViewTextColor();
            update();
        }
    }

    private TextView loadPercentView() {
        return (TextView) LayoutInflater.from(getContext()).inflate(R.layout.battery_percentage_view, (ViewGroup) null);
    }

    private TextView loadPercentMarkView() {
        return (TextView) LayoutInflater.from(getContext()).inflate(R.layout.battery_percentage_mark_view, (ViewGroup) null);
    }

    private void updatePercentText() {
        if (this.mBatteryPercentView != null) {
            TextView textView = this.mBatteryPercentMarkView;
            if (textView != null) {
                textView.setVisibility(0);
            }
            this.mBatteryPercentView.setText(String.valueOf(this.mLevel));
        }
    }

    private void updateShowPercent() {
        boolean z = this.mBatteryPercentView != null;
        if (this.mShowPercent) {
            if (!z) {
                this.mBatteryPercentView = loadPercentView();
                ViewGroup.MarginLayoutParams marginLayoutParams = new ViewGroup.MarginLayoutParams(-2, -1);
                marginLayoutParams.setMargins(getResources().getDimensionPixelOffset(R.dimen.statusbar_battery_percent_margin_start), 0, getResources().getDimensionPixelOffset(R.dimen.statusbar_battery_percent_margin_end), 0);
                addView(this.mBatteryPercentView, -1, marginLayoutParams);
                this.mBatteryPercentView.setTypeface(this.mDigitTypeface);
                this.mBatteryPercentView.setImportantForAccessibility(2);
                BatteryMeterViewDelegate batteryMeterViewDelegate = this.mBatteryMeterViewDelegate;
                if (batteryMeterViewDelegate != null) {
                    batteryMeterViewDelegate.onNumberToIconChanged(true);
                }
                this.mBatteryPercentMarkView = loadPercentMarkView();
                this.mBatteryPercentMarkView.setText(this.mContext.getResources().getString(R.string.battery_meter_percent_sign));
                addView(this.mBatteryPercentMarkView, -1, new LinearLayout.LayoutParams(this.mContext.getResources().getDimensionPixelSize(R.dimen.battery_percent_mark_view_width), -1));
                this.mBatteryPercentMarkView.setTypeface(this.mMarkTypeface);
                this.mBatteryPercentMarkView.setImportantForAccessibility(2);
                setPercentViewTextColor();
            }
            updatePercentText();
        } else if (z) {
            removeView(this.mBatteryPercentView);
            this.mBatteryPercentView = null;
            TextView textView = this.mBatteryPercentMarkView;
            if (textView != null) {
                removeView(textView);
                this.mBatteryPercentMarkView = null;
            }
            BatteryMeterViewDelegate batteryMeterViewDelegate2 = this.mBatteryMeterViewDelegate;
            if (batteryMeterViewDelegate2 != null) {
                batteryMeterViewDelegate2.onNumberToIconChanged(false);
            }
        }
    }

    private boolean isWideNotchEar() {
        StatusBarTypeController.CutoutType cutoutType = this.mCutoutType;
        if (cutoutType == null) {
            cutoutType = ((StatusBarTypeController) Dependency.get(StatusBarTypeController.class)).getCutoutType();
        }
        return cutoutType == StatusBarTypeController.CutoutType.DRIP || cutoutType == StatusBarTypeController.CutoutType.NARROW_NOTCH;
    }

    private void updateViews() {
        updateIconView(this.mBatteryStyle != 2);
    }

    private void updateIconView(boolean z) {
        if (z) {
            this.mBatteryDigitalView.setVisibility(0);
        } else {
            this.mBatteryDigitalView.setVisibility(8);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:7:0x0012, code lost:
        r3 = r5.mBatteryStyle;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void updateChargingIconView() {
        /*
            r5 = this;
            boolean r0 = r5.mDemoMode
            if (r0 == 0) goto L_0x0005
            return
        L_0x0005:
            boolean r0 = r5.mUseLegacyDrawable
            r1 = 0
            r2 = 8
            if (r0 != 0) goto L_0x001f
            android.widget.ImageView r0 = r5.mBatteryChargingInView
            boolean r3 = r5.mCharging
            if (r3 == 0) goto L_0x001b
            int r3 = r5.mBatteryStyle
            if (r3 == 0) goto L_0x0019
            r4 = 3
            if (r3 != r4) goto L_0x001b
        L_0x0019:
            r3 = r1
            goto L_0x001c
        L_0x001b:
            r3 = r2
        L_0x001c:
            r0.setVisibility(r3)
        L_0x001f:
            android.widget.ImageView r0 = r5.mBatteryChargingView
            boolean r3 = r5.mNortchEar
            if (r3 == 0) goto L_0x002b
            boolean r3 = r5.isWideNotchEar()
            if (r3 == 0) goto L_0x0034
        L_0x002b:
            int r5 = r5.mBatteryStyle
            r3 = 2
            if (r5 == r3) goto L_0x0035
            r3 = 1
            if (r5 != r3) goto L_0x0034
            goto L_0x0035
        L_0x0034:
            r1 = r2
        L_0x0035:
            r0.setVisibility(r1)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.BatteryMeterView.updateChargingIconView():void");
    }

    public void onConfigChanged(Configuration configuration) {
        updateResources();
        BatteryIcon.getInstance(this.mContext).clear();
        this.mIconId = getIconId();
        update();
    }

    public void onDensityOrFontScaleChanged() {
        scaleBatteryMeterViews();
    }

    private void scaleBatteryMeterViews() {
        Resources resources = getContext().getResources();
        TypedValue typedValue = new TypedValue();
        resources.getValue(R.dimen.status_bar_icon_scale_factor, typedValue, true);
        float f = typedValue.getFloat();
        int dimensionPixelSize = resources.getDimensionPixelSize(R.dimen.status_bar_battery_icon_height);
        int dimensionPixelSize2 = resources.getDimensionPixelSize(R.dimen.status_bar_battery_icon_width);
        new LinearLayout.LayoutParams((int) (((float) dimensionPixelSize) * f), (int) (((float) dimensionPixelSize2) * f)).setMargins(0, 0, 0, resources.getDimensionPixelSize(R.dimen.battery_margin_bottom));
    }

    public void onDarkChanged(Rect rect, float f, int i) {
        this.mTintArea.set(rect);
        this.mDarkIntensity = f;
        this.mTintColor = i;
        setDigitViewTextColor();
        setPercentViewTextColor();
        updateBatteryChargingIcon();
        this.mBatteryIconView.onDarkChanged(rect, f, i);
    }

    public void setPercentViewTextColor() {
        int i;
        int i2;
        int i3;
        int i4;
        if (this.mBatteryPercentView != null) {
            if (Util.showCtsSpecifiedColor()) {
                if (this.mShowPercent) {
                    boolean inDarkMode = DarkIconDispatcherHelper.inDarkMode(this.mTintArea, this.mBatteryPercentView, this.mDarkIntensity);
                    TextView textView = this.mBatteryPercentView;
                    if (inDarkMode) {
                        i3 = this.mBatteryTextColors[6];
                    } else {
                        i3 = this.mBatteryTextColors[0];
                    }
                    textView.setTextColor(i3);
                    TextView textView2 = this.mBatteryPercentMarkView;
                    if (textView2 != null) {
                        boolean inDarkMode2 = DarkIconDispatcherHelper.inDarkMode(this.mTintArea, textView2, this.mDarkIntensity);
                        TextView textView3 = this.mBatteryPercentMarkView;
                        if (inDarkMode2) {
                            i4 = this.mBatteryTextColors[6];
                        } else {
                            i4 = this.mBatteryTextColors[0];
                        }
                        textView3.setTextColor(i4);
                    }
                }
            } else if (!this.mShowPercent) {
            } else {
                if (this.mDarkIconDispatcher.useTint()) {
                    TextView textView4 = this.mBatteryPercentView;
                    textView4.setTextColor(DarkIconDispatcherHelper.getTint(this.mTintArea, textView4, this.mTintColor));
                    TextView textView5 = this.mBatteryPercentMarkView;
                    if (textView5 != null) {
                        textView5.setTextColor(DarkIconDispatcherHelper.getTint(this.mTintArea, this.mBatteryPercentView, this.mTintColor));
                        return;
                    }
                    return;
                }
                boolean inDarkMode3 = DarkIconDispatcherHelper.inDarkMode(this.mTintArea, this.mBatteryPercentView, this.mDarkIntensity);
                TextView textView6 = this.mBatteryPercentView;
                if (inDarkMode3) {
                    i = this.mBatteryTextColors[1];
                } else {
                    i = this.mBatteryTextColors[0];
                }
                textView6.setTextColor(i);
                TextView textView7 = this.mBatteryPercentMarkView;
                if (textView7 != null) {
                    boolean inDarkMode4 = DarkIconDispatcherHelper.inDarkMode(this.mTintArea, textView7, this.mDarkIntensity);
                    TextView textView8 = this.mBatteryPercentMarkView;
                    if (inDarkMode4) {
                        i2 = this.mBatteryTextColors[1];
                    } else {
                        i2 = this.mBatteryTextColors[0];
                    }
                    textView8.setTextColor(i2);
                }
            }
        }
    }

    public void setDigitViewTextColor() {
        int i;
        int i2;
        if (Util.showCtsSpecifiedColor()) {
            if (this.mForceShowDigit) {
                boolean inDarkMode = DarkIconDispatcherHelper.inDarkMode(this.mTintArea, this.mBatteryTextDigitView, this.mDarkIntensity);
                TextView textView = this.mBatteryTextDigitView;
                if (inDarkMode) {
                    i2 = this.mBatteryTextColors[6];
                } else if (this.mPowerSave || this.mExtremePowerSave) {
                    i2 = this.mBatteryTextColors[4];
                } else {
                    i2 = this.mBatteryTextColors[2];
                }
                textView.setTextColor(i2);
            }
        } else if (!this.mForceShowDigit) {
        } else {
            if (this.mDarkIconDispatcher.useTint()) {
                TextView textView2 = this.mBatteryTextDigitView;
                textView2.setTextColor(DarkIconDispatcherHelper.getTint(this.mTintArea, textView2, this.mTintColor));
                return;
            }
            boolean inDarkMode2 = DarkIconDispatcherHelper.inDarkMode(this.mTintArea, this.mBatteryTextDigitView, this.mDarkIntensity);
            TextView textView3 = this.mBatteryTextDigitView;
            if (inDarkMode2) {
                if (this.mPowerSave || this.mExtremePowerSave) {
                    i = this.mBatteryTextColors[5];
                } else {
                    i = this.mBatteryTextColors[3];
                }
            } else if (this.mPowerSave || this.mExtremePowerSave) {
                i = this.mBatteryTextColors[4];
            } else {
                i = this.mBatteryTextColors[2];
            }
            textView3.setTextColor(i);
        }
    }

    public void update() {
        int i = 0;
        if (this.mDemoMode) {
            this.mBatteryIconView.update(100, this.mIconId, false, false, false, false, false);
        } else {
            this.mBatteryIconView.update(this.mLevel, this.mIconId, this.mForceShowDigit, this.mQuickCharging, this.mCharging, this.mPowerSave || this.mExtremePowerSave, false);
        }
        this.mBatteryTextDigitView.setText(String.valueOf(this.mLevel));
        TextView textView = this.mBatteryTextDigitView;
        if (this.mDemoMode || !this.mForceShowDigit || (!this.mShowBatteryDigitFull && this.mLevel == 100)) {
            i = 8;
        }
        textView.setVisibility(i);
        updateShowPercent();
        updateBatteryChargingIcon();
        invalidate();
    }

    /* access modifiers changed from: private */
    public int getIconId() {
        return this.mCharging ? this.mForceShowDigit ? R.raw.stat_sys_battery_charge_digit : R.raw.stat_sys_battery_charge : (this.mPowerSave || this.mExtremePowerSave) ? this.mForceShowDigit ? R.raw.stat_sys_battery_power_save_digit : R.raw.stat_sys_battery_power_save : this.mForceShowDigit ? R.raw.stat_sys_battery_digital : R.raw.stat_sys_battery;
    }

    /* access modifiers changed from: protected */
    public void updateVisibility() {
        if (!this.mDemoMode) {
            setVisibility(!this.mDisabled ? 0 : 8);
        }
    }

    public void dispatchDemoCommand(String str, Bundle bundle) {
        if (!this.mDemoMode && str.equals("enter")) {
            this.mDemoMode = true;
            setVisibility(0);
            update();
        } else if (this.mDemoMode && str.equals("exit")) {
            this.mDemoMode = false;
            updateVisibility();
            update();
        }
    }

    private void updateBatteryChargingIcon() {
        if (this.mDemoMode) {
            this.mBatteryChargingView.setVisibility(8);
            this.mBatteryChargingInView.setVisibility(8);
        } else if (this.mQuickCharging || this.mCharging) {
            updateIcon(this.mBatteryChargingView, getChargingIconId());
            updateIcon(this.mBatteryChargingInView, getChargingIconId());
            updateChargingIconView();
        } else {
            this.mBatteryChargingView.setVisibility(8);
            this.mBatteryChargingInView.setVisibility(8);
        }
    }

    private int getChargingIconId() {
        return this.mUseLegacyDrawable ? this.mQuickCharging ? R.drawable.stat_sys_quick_charging : R.drawable.stat_sys_battery_charging : this.mQuickCharging ? R.drawable.battery_meter_quick_charging : R.drawable.battery_meter_charging;
    }

    private void updateIcon(ImageView imageView, int i) {
        imageView.setImageTintMode(PorterDuff.Mode.SRC_IN);
        if (Util.showCtsSpecifiedColor() || !this.mDarkIconDispatcher.useTint()) {
            boolean inDarkMode = DarkIconDispatcherHelper.inDarkMode(this.mTintArea, imageView, this.mDarkIntensity);
            imageView.setImageResource(Icons.get(Integer.valueOf(i), inDarkMode));
            if (!inDarkMode || !Util.showCtsSpecifiedColor()) {
                imageView.setImageTintList((ColorStateList) null);
            } else {
                imageView.setImageTintList(ColorStateList.valueOf(this.mBatteryTextColors[6]));
            }
        } else {
            imageView.setImageResource(i);
            imageView.setImageTintList(ColorStateList.valueOf(DarkIconDispatcherHelper.getTint(this.mTintArea, imageView, this.mTintColor)));
        }
    }

    public void setNotchEar(boolean z) {
        this.mNortchEar = z;
        onBatteryStyleChanged(this.mBatteryStyle);
    }

    public void setInHeader(boolean z) {
        this.mInHeader = z;
    }

    public void setCutoutType(StatusBarTypeController.CutoutType cutoutType) {
        this.mCutoutType = cutoutType;
        onBatteryStyleChanged(this.mBatteryStyle);
    }

    public void onCutoutTypeChanged() {
        if (this.mInHeader && this.mShowPercent) {
            updatePercentText();
        }
    }

    public void setBatteryMeterViewDelegate(BatteryMeterViewDelegate batteryMeterViewDelegate) {
        this.mBatteryMeterViewDelegate = batteryMeterViewDelegate;
        batteryMeterViewDelegate.onNumberToIconChanged(this.mShowPercent);
    }
}
