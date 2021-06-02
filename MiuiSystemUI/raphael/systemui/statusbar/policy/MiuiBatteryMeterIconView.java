package com.android.systemui.statusbar.policy;

import android.animation.ArgbEvaluator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.ArrayMap;
import android.util.AttributeSet;
import android.util.Log;
import android.util.PathParser;
import android.widget.ImageView;
import com.android.systemui.C0010R$bool;
import com.android.systemui.C0011R$color;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0016R$integer;
import com.android.systemui.C0020R$raw;
import com.android.systemui.C0021R$string;
import com.android.systemui.plugins.DarkIconDispatcher;
import com.android.systemui.statusbar.phone.BatteryIcon;

public class MiuiBatteryMeterIconView extends ImageView {
    private int mBatteryChargingColor;
    private int mBatteryLowColor;
    private int mBatteryNormalDarkColor;
    private int mBatteryNormalDigitDarkColor;
    private int mBatteryNormalDigitLightColor;
    private int mBatteryNormalLightColor;
    private int mBatteryPowerSaveColor;
    private int mBgTintColor;
    private boolean mCharging;
    private Drawable mChargingMask;
    private Path mChargingMaskPath;
    private Paint mClearPaint;
    private boolean mDark;
    private Drawable mDarkBg;
    private int mDarkColor;
    private float mDarkIntensity;
    private int mIconId;
    private boolean mIsDigit;
    private LayerDrawable mLayerDrawable;
    private int mLevel;
    private int mLightColor;
    private boolean mLow;
    private boolean mMaskCharging;
    private boolean mMaskProgress;
    private float mMaskScale;
    private boolean mNeedRoundProgress;
    private boolean mPowerSave;
    private float mProgressCenterLeftOffset;
    private ClipDrawable mProgressClipDrawable;
    private ArrayMap<BatteryStatus, Drawable> mProgressDarkDrawables;
    private ArrayMap<BatteryStatus, Drawable> mProgressDrawables;
    private boolean mProgressGravityStart;
    private int mProgressHeight;
    private int mProgressMaskMax;
    private int mProgressMaskMin;
    private Path mProgressMaskPath;
    private boolean mProgressOrientationPortrait;
    private int mProgressWidth;
    private boolean mQuickCharging;
    private Drawable mQuickChargingMask;
    private Path mQuickChargingMaskPath;
    private Drawable mRoundProgress;
    private Rect mTintArea;
    private Drawable mTintBgDrawable;
    private boolean mUseLegacyDrawable;
    private boolean mUseTint;
    private Drawable mWhiteBg;

    /* access modifiers changed from: private */
    public enum BatteryStatus {
        UNKNOWN,
        LOW,
        LOW_DIGIT,
        NORMAL,
        NORMAL_DIGIT,
        CHARGING,
        CHARGING_DIGIT,
        POWER_SAVE,
        POWER_SAVE_DIGIT
    }

    public MiuiBatteryMeterIconView(Context context) {
        this(context, null);
    }

    public MiuiBatteryMeterIconView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public MiuiBatteryMeterIconView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public MiuiBatteryMeterIconView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mTintArea = new Rect(0, 0, 0, 0);
        this.mUseTint = false;
        this.mDark = false;
        this.mIconId = C0020R$raw.stat_sys_battery;
        this.mProgressDrawables = new ArrayMap<>();
        this.mProgressDarkDrawables = new ArrayMap<>();
        this.mClearPaint = new Paint(1);
        this.mClearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        setup();
    }

    public void setup() {
        updateResources();
        updateProgressDrawable();
        update(this.mLevel, this.mIconId, this.mIsDigit, this.mQuickCharging, this.mCharging, this.mPowerSave, true);
    }

    public void update(int i, int i2, boolean z, boolean z2, boolean z3, boolean z4, boolean z5) {
        if (this.mLevel != i || this.mIconId != i2 || this.mIsDigit != z || this.mQuickCharging != z2 || this.mCharging != z3 || this.mPowerSave != z4 || z5) {
            this.mLevel = i;
            this.mIconId = i2;
            this.mIsDigit = z;
            this.mCharging = z3;
            this.mQuickCharging = z2;
            this.mPowerSave = z4;
            this.mLow = i <= 19;
            updateRoundProgressFlag(this.mLevel);
            onDarkChanged(this.mTintArea, this.mDarkIntensity, this.mBgTintColor, this.mLightColor, this.mDarkColor, this.mUseTint, true);
        }
    }

    private void updateProgressDrawable() {
        int i;
        if (this.mUseLegacyDrawable) {
            this.mLayerDrawable = null;
            this.mProgressClipDrawable = null;
            this.mTintBgDrawable = null;
            setImageDrawable(null);
            return;
        }
        LayerDrawable layerDrawable = (LayerDrawable) getContext().getResources().getDrawable(C0013R$drawable.battery_meter);
        this.mLayerDrawable = layerDrawable;
        layerDrawable.mutate();
        if (this.mProgressOrientationPortrait) {
            i = 80;
        } else {
            i = this.mProgressGravityStart ? 8388611 : 8388613;
        }
        Drawable drawable = getResources().getDrawable(C0013R$drawable.battery_meter_progress_normal);
        drawable.mutate();
        ClipDrawable clipDrawable = new ClipDrawable(drawable, i, this.mProgressOrientationPortrait ? 2 : 1);
        this.mProgressClipDrawable = clipDrawable;
        clipDrawable.mutate();
        this.mLayerDrawable.setDrawableByLayerId(C0015R$id.progress, this.mProgressClipDrawable);
        LayerDrawable layerDrawable2 = this.mLayerDrawable;
        layerDrawable2.setLayerGravity(layerDrawable2.findIndexByLayerId(C0015R$id.progress), 17);
        LayerDrawable layerDrawable3 = this.mLayerDrawable;
        layerDrawable3.setLayerInset(layerDrawable3.findIndexByLayerId(C0015R$id.progress), 0, 0, (int) (this.mProgressCenterLeftOffset * 2.0f), 0);
        this.mProgressWidth = this.mProgressClipDrawable.getIntrinsicWidth();
        this.mProgressHeight = this.mProgressClipDrawable.getIntrinsicHeight();
        setImageDrawable(this.mLayerDrawable);
    }

    private void updateResources() {
        Resources resources = getContext().getResources();
        this.mMaskScale = resources.getDimension(C0012R$dimen.battery_meter_charging_mask_scale);
        this.mProgressCenterLeftOffset = resources.getDimension(C0012R$dimen.battery_meter_progress_center_left_offset);
        this.mProgressOrientationPortrait = resources.getBoolean(C0010R$bool.battery_meter_progress_oriention_portrait);
        this.mProgressGravityStart = resources.getBoolean(C0010R$bool.battery_meter_progress_gravity_start);
        this.mWhiteBg = resources.getDrawable(C0013R$drawable.battery_meter_bg);
        this.mDarkBg = resources.getDrawable(C0013R$drawable.battery_meter_bg_dark);
        Matrix matrix = new Matrix();
        float f = this.mMaskScale;
        matrix.setScale(f, f);
        this.mMaskCharging = resources.getBoolean(C0010R$bool.battery_meter_mask_charging);
        this.mMaskProgress = resources.getBoolean(C0010R$bool.battery_meter_mask_progress);
        if (this.mMaskCharging) {
            try {
                this.mChargingMaskPath = PathParser.createPathFromPathData(resources.getString(C0021R$string.battery_meter_charging_mask_path));
                this.mChargingMask = resources.getDrawable(C0013R$drawable.battery_meter_charging_mask);
                this.mChargingMaskPath.transform(matrix);
            } catch (Exception e) {
                this.mChargingMaskPath = null;
                Log.e("BatteryMeterIconView", "create ChargingMaskPath Exception=" + e);
            }
            try {
                this.mQuickChargingMaskPath = PathParser.createPathFromPathData(resources.getString(C0021R$string.battery_meter_quick_charging_mask_path));
                this.mQuickChargingMask = resources.getDrawable(C0013R$drawable.battery_meter_quick_charging_mask);
                this.mQuickChargingMaskPath.transform(matrix);
            } catch (Exception e2) {
                this.mQuickChargingMaskPath = null;
                Log.e("BatteryMeterIconView", "create ChargingMaskPath Exception=" + e2);
            }
        }
        if (this.mMaskProgress) {
            try {
                this.mProgressMaskPath = PathParser.createPathFromPathData(resources.getString(C0021R$string.battery_meter_progress_mask_path));
                this.mRoundProgress = resources.getDrawable(C0013R$drawable.battery_meter_progress_mask);
                this.mProgressMaskPath.transform(matrix);
            } catch (Exception e3) {
                this.mProgressMaskPath = null;
                Log.e("BatteryMeterIconView", "create ProgressMaskPath Exception=" + e3);
            }
        }
        this.mProgressMaskMin = resources.getInteger(C0016R$integer.battery_meter_progress_mask_min);
        this.mProgressMaskMax = resources.getInteger(C0016R$integer.battery_meter_progress_mask_max);
        this.mProgressDrawables.clear();
        this.mProgressDarkDrawables.clear();
        this.mBatteryLowColor = resources.getColor(C0011R$color.status_bar_battery_low);
        this.mBatteryNormalLightColor = resources.getColor(C0011R$color.status_bar_battery_normal_light);
        this.mBatteryNormalDarkColor = resources.getColor(C0011R$color.status_bar_battery_normal_dark);
        this.mBatteryNormalDigitLightColor = resources.getColor(C0011R$color.status_bar_battery_normal_digit_light);
        this.mBatteryNormalDigitDarkColor = resources.getColor(C0011R$color.status_bar_battery_normal_digit_dark);
        this.mBatteryChargingColor = resources.getColor(C0011R$color.status_bar_battery_charging);
        this.mBatteryPowerSaveColor = resources.getColor(C0011R$color.status_bar_battery_power_save);
        this.mProgressDrawables.put(BatteryStatus.LOW, resources.getDrawable(C0013R$drawable.battery_meter_progress_low));
        this.mProgressDarkDrawables.put(BatteryStatus.LOW, resources.getDrawable(C0013R$drawable.battery_meter_progress_low_dark));
        this.mProgressDrawables.put(BatteryStatus.LOW_DIGIT, resources.getDrawable(C0013R$drawable.battery_meter_progress_low_digit));
        this.mProgressDarkDrawables.put(BatteryStatus.LOW_DIGIT, resources.getDrawable(C0013R$drawable.battery_meter_progress_low_digit_dark));
        this.mProgressDrawables.put(BatteryStatus.NORMAL, resources.getDrawable(C0013R$drawable.battery_meter_progress_normal));
        this.mProgressDarkDrawables.put(BatteryStatus.NORMAL, resources.getDrawable(C0013R$drawable.battery_meter_progress_normal_dark));
        this.mProgressDrawables.put(BatteryStatus.NORMAL_DIGIT, resources.getDrawable(C0013R$drawable.battery_meter_progress_normal_digit));
        this.mProgressDarkDrawables.put(BatteryStatus.NORMAL_DIGIT, resources.getDrawable(C0013R$drawable.battery_meter_progress_normal_digit_dark));
        this.mProgressDrawables.put(BatteryStatus.CHARGING, resources.getDrawable(C0013R$drawable.battery_meter_progress_charging));
        this.mProgressDarkDrawables.put(BatteryStatus.CHARGING, resources.getDrawable(C0013R$drawable.battery_meter_progress_charging_dark));
        this.mProgressDrawables.put(BatteryStatus.CHARGING_DIGIT, resources.getDrawable(C0013R$drawable.battery_meter_progress_charging_digit));
        this.mProgressDarkDrawables.put(BatteryStatus.CHARGING_DIGIT, resources.getDrawable(C0013R$drawable.battery_meter_progress_charging_digit_dark));
        this.mProgressDrawables.put(BatteryStatus.POWER_SAVE, resources.getDrawable(C0013R$drawable.battery_meter_progress_power_save));
        this.mProgressDarkDrawables.put(BatteryStatus.POWER_SAVE, resources.getDrawable(C0013R$drawable.battery_meter_progress_power_save_dark));
        this.mProgressDrawables.put(BatteryStatus.POWER_SAVE_DIGIT, resources.getDrawable(C0013R$drawable.battery_meter_progress_power_save_digit));
        this.mProgressDarkDrawables.put(BatteryStatus.POWER_SAVE_DIGIT, resources.getDrawable(C0013R$drawable.battery_meter_progress_power_save_digit_dark));
    }

    public void onDarkChanged(Rect rect, float f, int i, int i2, int i3, boolean z) {
        onDarkChanged(rect, f, i, i2, i3, z, false);
    }

    public void onDarkChanged(Rect rect, float f, int i, int i2, int i3, boolean z, boolean z2) {
        Drawable drawable;
        this.mTintArea.set(rect);
        this.mDarkIntensity = f;
        this.mBgTintColor = i;
        this.mLightColor = i2;
        this.mDarkColor = i3;
        boolean z3 = false;
        boolean z4 = true;
        boolean z5 = DarkIconDispatcher.getDarkIntensity(rect, this, f) > 0.0f;
        if (this.mUseLegacyDrawable) {
            setImageDrawable(getLegacyDrawable(z5));
            setImageLevel(this.mLevel);
            return;
        }
        if (this.mUseTint != z) {
            this.mUseTint = z;
            if (!z) {
                this.mLayerDrawable.setTintList(null);
            }
            z3 = true;
        }
        if (z || this.mDark == z5) {
            z4 = z3;
        } else {
            this.mDark = z5;
        }
        if (z4 || z2) {
            if (z) {
                Drawable drawable2 = getContext().getDrawable(C0013R$drawable.battery_meter_bg_tint);
                this.mTintBgDrawable = drawable2;
                if (drawable2 != null) {
                    drawable2.mutate();
                    this.mLayerDrawable.setDrawableByLayerId(C0015R$id.background, this.mTintBgDrawable);
                }
                Drawable drawable3 = getContext().getDrawable(C0013R$drawable.battery_meter_progress_tint);
                if (drawable3 != null) {
                    drawable3.mutate();
                    this.mProgressClipDrawable.setDrawable(drawable3);
                }
            } else {
                this.mProgressClipDrawable.setTintList(null);
                this.mLayerDrawable.setDrawableByLayerId(C0015R$id.background, this.mDark ? this.mDarkBg : this.mWhiteBg);
                if (this.mDark) {
                    drawable = this.mProgressDarkDrawables.get(getStatus());
                } else {
                    drawable = this.mProgressDrawables.get(getStatus());
                }
                if (drawable != null) {
                    this.mProgressClipDrawable.setDrawable(drawable);
                }
            }
        }
        if (z) {
            this.mTintBgDrawable.setTintList(ColorStateList.valueOf(DarkIconDispatcher.getTint(rect, this, i)));
            this.mProgressClipDrawable.setTintList(ColorStateList.valueOf(getBatteryProgressColor(DarkIconDispatcher.isInArea(this.mTintArea, this))));
        }
        if (!this.mProgressClipDrawable.setLevel(this.mLevel * 100)) {
            this.mProgressClipDrawable.invalidateSelf();
        }
    }

    /* access modifiers changed from: package-private */
    /* renamed from: com.android.systemui.statusbar.policy.MiuiBatteryMeterIconView$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$android$systemui$statusbar$policy$MiuiBatteryMeterIconView$BatteryStatus;

        /* JADX WARNING: Can't wrap try/catch for region: R(18:0|1|2|3|4|5|6|7|8|9|10|11|12|13|14|15|16|(3:17|18|20)) */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:11:0x003e */
        /* JADX WARNING: Missing exception handler attribute for start block: B:13:0x0049 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:15:0x0054 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:17:0x0060 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0012 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:5:0x001d */
        /* JADX WARNING: Missing exception handler attribute for start block: B:7:0x0028 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:9:0x0033 */
        static {
            /*
            // Method dump skipped, instructions count: 109
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.policy.MiuiBatteryMeterIconView.AnonymousClass1.<clinit>():void");
        }
    }

    private int getBatteryProgressColor(boolean z) {
        switch (AnonymousClass1.$SwitchMap$com$android$systemui$statusbar$policy$MiuiBatteryMeterIconView$BatteryStatus[getStatus().ordinal()]) {
            case 1:
            case 2:
                return this.mBatteryLowColor;
            case 3:
                if (z) {
                    return ((Integer) ArgbEvaluator.getInstance().evaluate(this.mDarkIntensity, Integer.valueOf(this.mBatteryNormalDigitLightColor), Integer.valueOf(this.mBatteryNormalDigitDarkColor))).intValue();
                }
                return this.mBatteryNormalDigitLightColor;
            case 4:
            case 5:
                return this.mBatteryPowerSaveColor;
            case 6:
            case 7:
                return this.mBatteryChargingColor;
            default:
                if (z) {
                    return ((Integer) ArgbEvaluator.getInstance().evaluate(this.mDarkIntensity, Integer.valueOf(this.mBatteryNormalLightColor), Integer.valueOf(this.mBatteryNormalDarkColor))).intValue();
                }
                return this.mBatteryNormalLightColor;
        }
    }

    private BatteryStatus getStatus() {
        int i;
        BatteryStatus[] values = BatteryStatus.values();
        if (this.mCharging) {
            i = BatteryStatus.CHARGING.ordinal();
        } else if (this.mPowerSave) {
            i = BatteryStatus.POWER_SAVE.ordinal();
        } else if (this.mLow) {
            i = BatteryStatus.LOW.ordinal();
        } else {
            i = BatteryStatus.NORMAL.ordinal();
        }
        return values[i + (this.mIsDigit ? 1 : 0)];
    }

    private Drawable getLegacyDrawable(boolean z) {
        int i = this.mIconId;
        if (i == C0020R$raw.stat_sys_battery_power_save) {
            if (z) {
                return BatteryIcon.getInstance(getContext()).getGraphicPowerSaveIconDarkMode(this.mLevel);
            }
            return BatteryIcon.getInstance(getContext()).getGraphicPowerSaveIcon(this.mLevel);
        } else if (i == C0020R$raw.stat_sys_battery_power_save_digit) {
            if (z) {
                return BatteryIcon.getInstance(getContext()).getGraphicPowerSaveDigitIconDarkMode(this.mLevel);
            }
            return BatteryIcon.getInstance(getContext()).getGraphicPowerSaveDigitIcon(this.mLevel);
        } else if (i == C0020R$raw.stat_sys_battery_charge) {
            if (z) {
                return BatteryIcon.getInstance(getContext()).getGraphicChargeIconDarkMode(this.mLevel);
            }
            return BatteryIcon.getInstance(getContext()).getGraphicChargeIcon(this.mLevel);
        } else if (i == C0020R$raw.stat_sys_battery_charge_digit) {
            if (z) {
                return BatteryIcon.getInstance(getContext()).getGraphicChargeDigitIconDarkMode(this.mLevel);
            }
            return BatteryIcon.getInstance(getContext()).getGraphicChargeDigitIcon(this.mLevel);
        } else if (i == C0020R$raw.stat_sys_battery) {
            if (z) {
                return BatteryIcon.getInstance(getContext()).getGraphicIconDarkMode(this.mLevel);
            }
            return BatteryIcon.getInstance(getContext()).getGraphicIcon(this.mLevel);
        } else if (i != C0020R$raw.stat_sys_battery_digital) {
            return null;
        } else {
            if (z) {
                return BatteryIcon.getInstance(getContext()).getGraphicDigitalIconDarkMode(this.mLevel);
            }
            return BatteryIcon.getInstance(getContext()).getGraphicDigitalIcon(this.mLevel);
        }
    }

    private void updateRoundProgressFlag(int i) {
        this.mNeedRoundProgress = i >= this.mProgressMaskMin && i <= this.mProgressMaskMax;
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        float f;
        float f2;
        Path path;
        Path path2;
        Drawable drawable;
        if (this.mUseLegacyDrawable) {
            super.onDraw(canvas);
            return;
        }
        int saveLayer = canvas.saveLayer(0.0f, 0.0f, (float) canvas.getWidth(), (float) canvas.getHeight(), null, 31);
        super.onDraw(canvas);
        if (this.mMaskCharging && (path = this.mChargingMaskPath) != null && this.mCharging && !this.mIsDigit) {
            if (this.mQuickCharging) {
                drawable = this.mQuickChargingMask;
                path2 = this.mQuickChargingMaskPath;
            } else {
                path2 = path;
                drawable = this.mChargingMask;
            }
            canvas.save();
            canvas.translate(((float) ((canvas.getWidth() - drawable.getIntrinsicWidth()) / 2)) - this.mProgressCenterLeftOffset, (float) ((canvas.getHeight() - drawable.getIntrinsicHeight()) / 2));
            canvas.drawPath(path2, this.mClearPaint);
            canvas.restore();
        }
        if (this.mMaskProgress && this.mNeedRoundProgress && this.mRoundProgress != null) {
            if (this.mProgressOrientationPortrait) {
                f2 = (float) (((canvas.getHeight() - this.mProgressClipDrawable.getIntrinsicHeight()) / 2) + (((100 - this.mLevel) * this.mProgressHeight) / 100));
                f = (float) ((canvas.getWidth() - this.mRoundProgress.getIntrinsicWidth()) / 2);
            } else {
                f = ((((((float) canvas.getWidth()) - ((float) this.mProgressClipDrawable.getIntrinsicWidth())) / 2.0f) - this.mProgressCenterLeftOffset) + (((float) (this.mLevel * this.mProgressWidth)) / 100.0f)) - ((float) this.mRoundProgress.getIntrinsicWidth());
                f2 = (float) ((canvas.getHeight() - this.mRoundProgress.getIntrinsicHeight()) / 2);
            }
            canvas.save();
            canvas.translate(f, f2);
            canvas.drawPath(this.mProgressMaskPath, this.mClearPaint);
            canvas.restore();
        }
        canvas.restoreToCount(saveLayer);
    }

    public void setUseLegacyDrawable(boolean z) {
        this.mUseLegacyDrawable = z;
    }
}
