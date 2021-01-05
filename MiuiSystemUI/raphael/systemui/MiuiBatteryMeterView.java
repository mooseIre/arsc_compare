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
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.MiuiKeyguardUpdateMonitorCallback;
import com.android.keyguard.charge.MiuiBatteryStatus;
import com.android.systemui.plugins.DarkIconDispatcher;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.Icons;
import com.android.systemui.statusbar.phone.BatteryIcon;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.statusbar.policy.MiuiBatteryMeterIconView;
import com.android.systemui.util.Utils;
import miui.util.CustomizeUtil;

public class MiuiBatteryMeterView extends LinearLayout implements BatteryController.BatteryStateChangeCallback, DarkIconDispatcher.DarkReceiver, ConfigurationController.ConfigurationListener {
    private ImageView mBatteryChargingInView;
    private ImageView mBatteryChargingView;
    private BatteryController mBatteryController;
    private FrameLayout mBatteryDigitalView;
    /* access modifiers changed from: private */
    public MiuiBatteryMeterIconView mBatteryIconView;
    private BatteryMeterViewDelegate mBatteryMeterViewDelegate;
    private TextView mBatteryPercentMarkView;
    private TextView mBatteryPercentView;
    private int mBatteryStyle;
    private int[] mBatteryTextColors;
    private TextView mBatteryTextDigitView;
    /* access modifiers changed from: private */
    public boolean mCharging;
    /* access modifiers changed from: private */
    public Context mContext;
    private float mDarkIntensity;
    private boolean mDemoMode;
    private Typeface mDigitTypeface;
    /* access modifiers changed from: private */
    public boolean mExtremePowerSave;
    /* access modifiers changed from: private */
    public boolean mForceShowDigit;
    /* access modifiers changed from: private */
    public int mIconId;
    MiuiKeyguardUpdateMonitorCallback mKeyguardUpdateMonitorCallback;
    /* access modifiers changed from: private */
    public int mLevel;
    private Typeface mMarkTypeface;
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
    private boolean mUseTint;

    public interface BatteryMeterViewDelegate {
        void onNumberToIconChanged(boolean z);
    }

    static /* synthetic */ int access$972(MiuiBatteryMeterView miuiBatteryMeterView, int i) {
        int i2 = i & miuiBatteryMeterView.mQuickCharingStatus;
        miuiBatteryMeterView.mQuickCharingStatus = i2;
        return i2;
    }

    static /* synthetic */ int access$976(MiuiBatteryMeterView miuiBatteryMeterView, int i) {
        int i2 = i | miuiBatteryMeterView.mQuickCharingStatus;
        miuiBatteryMeterView.mQuickCharingStatus = i2;
        return i2;
    }

    public MiuiBatteryMeterView(Context context) {
        this(context, (AttributeSet) null);
    }

    public MiuiBatteryMeterView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public MiuiBatteryMeterView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mUseLegacyDrawable = false;
        this.mForceShowDigit = false;
        this.mQuickCharging = false;
        this.mIconId = C0020R$raw.stat_sys_battery;
        this.mBatteryStyle = 0;
        this.mTintArea = new Rect();
        this.mBatteryTextColors = new int[7];
        this.mKeyguardUpdateMonitorCallback = new MiuiKeyguardUpdateMonitorCallback() {
            public void onRefreshBatteryInfo(MiuiBatteryStatus miuiBatteryStatus) {
                int i;
                super.onRefreshBatteryInfo(miuiBatteryStatus);
                boolean isCharging = miuiBatteryStatus.isCharging();
                boolean z = isCharging && miuiBatteryStatus.isQuickCharge();
                int level = miuiBatteryStatus.getLevel();
                if (isCharging != MiuiBatteryMeterView.this.mCharging) {
                    boolean unused = MiuiBatteryMeterView.this.mCharging = isCharging;
                    BatteryIcon.getInstance(MiuiBatteryMeterView.this.mContext).clear();
                    MiuiBatteryMeterView.this.mBatteryIconView.update(MiuiBatteryMeterView.this.mLevel, MiuiBatteryMeterView.this.mIconId, MiuiBatteryMeterView.this.mForceShowDigit, MiuiBatteryMeterView.this.mQuickCharging, MiuiBatteryMeterView.this.mCharging, MiuiBatteryMeterView.this.mPowerSave || MiuiBatteryMeterView.this.mExtremePowerSave, false);
                }
                if (!isCharging) {
                    boolean unused2 = MiuiBatteryMeterView.this.mQuickCharging = false;
                    int unused3 = MiuiBatteryMeterView.this.mQuickCharingStatus = 0;
                } else if (z) {
                    MiuiBatteryMeterView.access$976(MiuiBatteryMeterView.this, 1);
                } else {
                    MiuiBatteryMeterView.access$972(MiuiBatteryMeterView.this, -2);
                }
                if (MiuiBatteryMeterView.this.mIconId == MiuiBatteryMeterView.this.getIconId() && level == MiuiBatteryMeterView.this.mLevel) {
                    if (MiuiBatteryMeterView.this.mQuickCharging == (MiuiBatteryMeterView.this.mQuickCharingStatus > 0)) {
                        return;
                    }
                }
                MiuiBatteryMeterView miuiBatteryMeterView = MiuiBatteryMeterView.this;
                boolean unused4 = miuiBatteryMeterView.mQuickCharging = miuiBatteryMeterView.mQuickCharingStatus > 0;
                int unused5 = MiuiBatteryMeterView.this.mLevel = level;
                boolean unused6 = MiuiBatteryMeterView.this.mCharging = isCharging;
                MiuiBatteryMeterView miuiBatteryMeterView2 = MiuiBatteryMeterView.this;
                int unused7 = miuiBatteryMeterView2.mIconId = miuiBatteryMeterView2.getIconId();
                MiuiBatteryMeterIconView access$800 = MiuiBatteryMeterView.this.mBatteryIconView;
                Context context = MiuiBatteryMeterView.this.getContext();
                if (isCharging) {
                    i = C0021R$string.accessibility_battery_level_charging;
                } else {
                    i = C0021R$string.accessibility_battery_level;
                }
                access$800.setContentDescription(context.getString(i, new Object[]{Integer.valueOf(level)}));
                MiuiBatteryMeterView.this.update();
            }
        };
        this.mContext = context;
        initMiuiView();
    }

    private void initMiuiView() {
        this.mDigitTypeface = Typeface.create("sans-serif-medium", 0);
        this.mMarkTypeface = Typeface.create("mipro-bold", 1);
        this.mUpdateMonitor = (KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class);
        setOrientation(0);
        setGravity(8388627);
        addOnAttachStateChangeListener(new Utils.DisableStateTracker(0, 2, (CommandQueue) Dependency.get(CommandQueue.class)));
        FrameLayout frameLayout = (FrameLayout) LayoutInflater.from(this.mContext).inflate(C0017R$layout.battery_digital_view, (ViewGroup) null);
        this.mBatteryDigitalView = frameLayout;
        this.mBatteryIconView = (MiuiBatteryMeterIconView) frameLayout.findViewById(C0015R$id.battery_image);
        this.mBatteryChargingInView = (ImageView) this.mBatteryDigitalView.findViewById(C0015R$id.battery_charge_image);
        ViewGroup.LayoutParams layoutParams = this.mBatteryIconView.getLayoutParams();
        layoutParams.width = -2;
        this.mBatteryIconView.setLayoutParams(layoutParams);
        TextView textView = (TextView) this.mBatteryDigitalView.findViewById(C0015R$id.battery_digit);
        this.mBatteryTextDigitView = textView;
        textView.setTypeface(this.mDigitTypeface);
        addView(this.mBatteryDigitalView);
        this.mBatteryChargingView = new ImageView(this.mContext);
        addView(this.mBatteryChargingView, new LinearLayout.LayoutParams(-2, -1));
        updateResources();
        onDarkChanged(new Rect(), 0.0f, -1, 0, 0, true);
    }

    public void onDarkChanged(Rect rect, float f, int i, int i2, int i3, boolean z) {
        this.mTintArea.set(rect);
        this.mDarkIntensity = f;
        this.mTintColor = i;
        this.mUseTint = z;
        setDigitViewTextColor();
        setPercentViewTextColor();
        updateBatteryChargingIcon();
        this.mBatteryIconView.onDarkChanged(rect, f, i, i2, i3, z);
    }

    private void updateResources() {
        Resources resources = this.mContext.getResources();
        this.mBatteryTextColors[0] = resources.getColor(C0011R$color.status_bar_textColor);
        this.mBatteryTextColors[1] = resources.getColor(C0011R$color.status_bar_textColor_darkmode);
        this.mBatteryTextColors[2] = resources.getColor(C0011R$color.status_bar_battery_digit_textColor);
        this.mBatteryTextColors[3] = resources.getColor(C0011R$color.status_bar_battery_digit_textColor_darkmode);
        this.mBatteryTextColors[4] = resources.getColor(C0011R$color.status_bar_battery_power_save_digit_textColor);
        this.mBatteryTextColors[5] = resources.getColor(C0011R$color.status_bar_battery_power_save_digit_textColor_darkmode);
        this.mBatteryTextColors[6] = resources.getColor(C0011R$color.status_bar_icon_text_color_dark_mode_cts);
        boolean z = resources.getBoolean(C0010R$bool.battery_meter_use_legacy_drawable);
        this.mUseLegacyDrawable = z;
        this.mBatteryIconView.setUseLegacyDrawable(z);
        this.mBatteryIconView.setup();
        this.mShowBatteryDigitFull = resources.getBoolean(C0010R$bool.show_battery_digit_full);
        TextView textView = this.mBatteryTextDigitView;
        if (textView != null) {
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) textView.getLayoutParams();
            layoutParams.rightMargin = resources.getDimensionPixelSize(C0012R$dimen.battery_meter_progress_center_left_offset);
            this.mBatteryTextDigitView.setLayoutParams(layoutParams);
        }
        FrameLayout.LayoutParams layoutParams2 = (FrameLayout.LayoutParams) this.mBatteryChargingInView.getLayoutParams();
        layoutParams2.rightMargin = resources.getDimensionPixelSize(C0012R$dimen.battery_meter_progress_center_left_offset);
        this.mBatteryChargingInView.setLayoutParams(layoutParams2);
    }

    private void updatePercentText() {
        if (this.mBatteryPercentView != null) {
            TextView textView = this.mBatteryPercentMarkView;
            if (textView != null) {
                textView.setVisibility(0);
            }
            this.mBatteryPercentView.setText(String.valueOf(this.mDemoMode ? 100 : this.mLevel));
        }
    }

    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        BatteryController batteryController = (BatteryController) Dependency.get(BatteryController.class);
        this.mBatteryController = batteryController;
        batteryController.addCallback(this);
        this.mUpdateMonitor.registerCallback(this.mKeyguardUpdateMonitorCallback);
        updateViews();
        ((ConfigurationController) Dependency.get(ConfigurationController.class)).addCallback(this);
    }

    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mBatteryController.removeCallback(this);
        this.mUpdateMonitor.removeCallback(this.mKeyguardUpdateMonitorCallback);
        ((ConfigurationController) Dependency.get(ConfigurationController.class)).removeCallback(this);
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

    public void onBatteryStyleChanged(int i) {
        Log.d("MiuiBatteryMeterView", "onBatteryStyleChanged: batteryStyle = " + i);
        if (CustomizeUtil.HAS_NOTCH && i == 2) {
            i = 0;
        }
        boolean z = true;
        this.mForceShowDigit = i == 1;
        if (i != 3) {
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

    private void setDigitViewTextColor() {
        int i;
        if (!this.mForceShowDigit) {
            return;
        }
        if (this.mUseTint) {
            TextView textView = this.mBatteryTextDigitView;
            textView.setTextColor(DarkIconDispatcher.getTint(this.mTintArea, textView, this.mTintColor));
            return;
        }
        boolean z = DarkIconDispatcher.getDarkIntensity(this.mTintArea, this.mBatteryTextDigitView, this.mDarkIntensity) > 0.0f;
        TextView textView2 = this.mBatteryTextDigitView;
        if (z) {
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
        textView2.setTextColor(i);
    }

    /* access modifiers changed from: private */
    public void update() {
        int i = 0;
        if (this.mDemoMode) {
            this.mBatteryIconView.update(100, this.mIconId, this.mForceShowDigit, this.mQuickCharging, this.mCharging, this.mPowerSave || this.mExtremePowerSave, false);
        } else {
            this.mBatteryIconView.update(this.mLevel, this.mIconId, this.mForceShowDigit, this.mQuickCharging, this.mCharging, this.mPowerSave || this.mExtremePowerSave, false);
        }
        this.mBatteryTextDigitView.setText(String.valueOf(this.mDemoMode ? 100 : this.mLevel));
        TextView textView = this.mBatteryTextDigitView;
        if (!this.mForceShowDigit || (!this.mShowBatteryDigitFull && this.mLevel == 100)) {
            i = 8;
        }
        textView.setVisibility(i);
        updateShowPercent();
        updateBatteryChargingIcon();
        invalidate();
    }

    private void updateBatteryChargingIcon() {
        if (this.mQuickCharging || this.mCharging) {
            updateIcon(this.mBatteryChargingView, getChargingIconId());
            updateIcon(this.mBatteryChargingInView, getChargingIconId());
            updateChargingIconView();
            return;
        }
        this.mBatteryChargingView.setVisibility(8);
        this.mBatteryChargingInView.setVisibility(8);
    }

    public void onConfigChanged(Configuration configuration) {
        updateResources();
        BatteryIcon.getInstance(this.mContext).clear();
        this.mIconId = getIconId();
        update();
    }

    public void onDensityOrFontScaleChanged() {
        scaleBatteryMeterViews();
        TextView textView = this.mBatteryTextDigitView;
        if (textView != null) {
            textView.setTextSize(0, (float) this.mContext.getResources().getDimensionPixelSize(C0012R$dimen.statusbar_battery_digit_size));
        }
        TextView textView2 = this.mBatteryPercentView;
        if (textView2 != null) {
            textView2.setTextSize(0, (float) this.mContext.getResources().getDimensionPixelSize(C0012R$dimen.statusbar_battery_size));
        }
        TextView textView3 = this.mBatteryPercentMarkView;
        if (textView3 != null) {
            textView3.setTextSize(0, (float) this.mContext.getResources().getDimensionPixelSize(C0012R$dimen.battery_percent_mark_view_text_size));
        }
        onConfigChanged(getContext().getResources().getConfiguration());
    }

    private void scaleBatteryMeterViews() {
        Resources resources = getContext().getResources();
        TypedValue typedValue = new TypedValue();
        resources.getValue(C0012R$dimen.status_bar_icon_scale_factor, typedValue, true);
        float f = typedValue.getFloat();
        int dimensionPixelSize = resources.getDimensionPixelSize(C0012R$dimen.status_bar_battery_icon_width);
        int dimensionPixelSize2 = resources.getDimensionPixelSize(C0012R$dimen.status_bar_battery_icon_height);
        new LinearLayout.LayoutParams((int) (((float) dimensionPixelSize2) * f), (int) (((float) dimensionPixelSize) * f)).setMargins(0, 0, 0, resources.getDimensionPixelSize(C0012R$dimen.battery_margin_bottom));
    }

    /* JADX WARNING: Code restructure failed: missing block: B:4:0x000d, code lost:
        r3 = r5.mBatteryStyle;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void updateChargingIconView() {
        /*
            r5 = this;
            boolean r0 = r5.mUseLegacyDrawable
            r1 = 8
            r2 = 0
            if (r0 != 0) goto L_0x001a
            android.widget.ImageView r0 = r5.mBatteryChargingInView
            boolean r3 = r5.mCharging
            if (r3 == 0) goto L_0x0016
            int r3 = r5.mBatteryStyle
            if (r3 == 0) goto L_0x0014
            r4 = 3
            if (r3 != r4) goto L_0x0016
        L_0x0014:
            r3 = r2
            goto L_0x0017
        L_0x0016:
            r3 = r1
        L_0x0017:
            r0.setVisibility(r3)
        L_0x001a:
            android.widget.ImageView r0 = r5.mBatteryChargingView
            int r5 = r5.mBatteryStyle
            r3 = 2
            if (r5 == r3) goto L_0x0024
            r3 = 1
            if (r5 != r3) goto L_0x0025
        L_0x0024:
            r1 = r2
        L_0x0025:
            r0.setVisibility(r1)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.MiuiBatteryMeterView.updateChargingIconView():void");
    }

    private int getChargingIconId() {
        return this.mQuickCharging ? C0013R$drawable.battery_meter_quick_charging : C0013R$drawable.battery_meter_charging;
    }

    private void updateIcon(ImageView imageView, int i) {
        int i2;
        imageView.setImageTintMode(PorterDuff.Mode.SRC_IN);
        if (!this.mUseTint) {
            if (DarkIconDispatcher.getDarkIntensity(this.mTintArea, imageView, this.mDarkIntensity) > 0.0f) {
                i2 = Icons.getDarkDrawableId(i);
            } else {
                i2 = Icons.getLightDrawableId(i);
            }
            imageView.setImageResource(i2);
            imageView.setImageTintList((ColorStateList) null);
            return;
        }
        imageView.setImageResource(Icons.getTintDrawableId(i));
        imageView.setImageTintList(ColorStateList.valueOf(DarkIconDispatcher.getTint(this.mTintArea, imageView, this.mTintColor)));
    }

    /* access modifiers changed from: private */
    public int getIconId() {
        return this.mCharging ? this.mForceShowDigit ? C0020R$raw.stat_sys_battery_charge_digit : C0020R$raw.stat_sys_battery_charge : (this.mPowerSave || this.mExtremePowerSave) ? this.mForceShowDigit ? C0020R$raw.stat_sys_battery_power_save_digit : C0020R$raw.stat_sys_battery_power_save : this.mForceShowDigit ? C0020R$raw.stat_sys_battery_digital : C0020R$raw.stat_sys_battery;
    }

    private void updateShowPercent() {
        boolean z = this.mBatteryPercentView != null;
        if (this.mShowPercent) {
            if (!z) {
                this.mBatteryPercentView = loadPercentView();
                ViewGroup.MarginLayoutParams marginLayoutParams = new ViewGroup.MarginLayoutParams(-2, -1);
                marginLayoutParams.setMargins(getResources().getDimensionPixelOffset(C0012R$dimen.statusbar_battery_percent_margin_start), 0, getResources().getDimensionPixelOffset(C0012R$dimen.statusbar_battery_percent_margin_end), 0);
                addView(this.mBatteryPercentView, -1, marginLayoutParams);
                this.mBatteryPercentView.setTypeface(this.mDigitTypeface);
                this.mBatteryPercentView.setImportantForAccessibility(2);
                BatteryMeterViewDelegate batteryMeterViewDelegate = this.mBatteryMeterViewDelegate;
                if (batteryMeterViewDelegate != null) {
                    batteryMeterViewDelegate.onNumberToIconChanged(true);
                }
                TextView loadPercentMarkView = loadPercentMarkView();
                this.mBatteryPercentMarkView = loadPercentMarkView;
                loadPercentMarkView.setText(this.mContext.getResources().getString(C0021R$string.battery_meter_percent_sign));
                addView(this.mBatteryPercentMarkView, -1, new LinearLayout.LayoutParams(-2, -1));
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

    private TextView loadPercentView() {
        return (TextView) LayoutInflater.from(getContext()).inflate(C0017R$layout.battery_percentage_view, (ViewGroup) null);
    }

    private void setPercentViewTextColor() {
        int i;
        int i2;
        TextView textView = this.mBatteryPercentView;
        if (textView == null || !this.mShowPercent) {
            return;
        }
        if (this.mUseTint) {
            textView.setTextColor(DarkIconDispatcher.getTint(this.mTintArea, textView, this.mTintColor));
            TextView textView2 = this.mBatteryPercentMarkView;
            if (textView2 != null) {
                textView2.setTextColor(DarkIconDispatcher.getTint(this.mTintArea, textView2, this.mTintColor));
                return;
            }
            return;
        }
        boolean z = DarkIconDispatcher.getDarkIntensity(this.mTintArea, textView, this.mDarkIntensity) > 0.0f;
        TextView textView3 = this.mBatteryPercentView;
        if (z) {
            i = this.mBatteryTextColors[1];
        } else {
            i = this.mBatteryTextColors[0];
        }
        textView3.setTextColor(i);
        TextView textView4 = this.mBatteryPercentMarkView;
        if (textView4 != null) {
            if (z) {
                i2 = this.mBatteryTextColors[1];
            } else {
                i2 = this.mBatteryTextColors[0];
            }
            textView4.setTextColor(i2);
        }
    }

    private TextView loadPercentMarkView() {
        return (TextView) LayoutInflater.from(getContext()).inflate(C0017R$layout.battery_percentage_mark_view, (ViewGroup) null);
    }

    public void onPowerSaveChanged(boolean z) {
        int i;
        this.mPowerSave = z;
        if (this.mIconId != getIconId()) {
            this.mIconId = getIconId();
            MiuiBatteryMeterIconView miuiBatteryMeterIconView = this.mBatteryIconView;
            Context context = getContext();
            if (this.mCharging) {
                i = C0021R$string.accessibility_battery_level_charging;
            } else {
                i = C0021R$string.accessibility_battery_level;
            }
            miuiBatteryMeterIconView.setContentDescription(context.getString(i, new Object[]{Integer.valueOf(this.mLevel)}));
            setDigitViewTextColor();
            update();
        }
    }

    public void onExtremePowerSaveChanged(boolean z) {
        int i;
        this.mExtremePowerSave = z;
        if (this.mIconId != getIconId()) {
            this.mIconId = getIconId();
            MiuiBatteryMeterIconView miuiBatteryMeterIconView = this.mBatteryIconView;
            Context context = getContext();
            if (this.mCharging) {
                i = C0021R$string.accessibility_battery_level_charging;
            } else {
                i = C0021R$string.accessibility_battery_level;
            }
            miuiBatteryMeterIconView.setContentDescription(context.getString(i, new Object[]{Integer.valueOf(this.mLevel)}));
            setDigitViewTextColor();
            update();
        }
    }

    public void dispatchDemoCommand(String str, Bundle bundle) {
        if (!this.mDemoMode && str.equals("enter")) {
            this.mDemoMode = true;
            update();
        } else if (this.mDemoMode && str.equals("exit")) {
            this.mDemoMode = false;
            update();
        }
    }
}
