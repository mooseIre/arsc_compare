package com.android.systemui.miui.statusbar;

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
import com.android.systemui.Dependency;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.phone.BatteryIcon;
import com.android.systemui.statusbar.policy.DarkIconDispatcher;
import com.android.systemui.statusbar.policy.DarkIconDispatcherHelper;

public class BatteryMeterIconView extends ImageView {
    private int mBatteryChargingColor;
    private int mBatteryLowColor;
    private int mBatteryNormalDarkColor;
    private int mBatteryNormalDigitDarkColor;
    private int mBatteryNormalDigitLightColor;
    private int mBatteryNormalLightColor;
    private int mBatteryPowerSaveColor;
    private Drawable mBgDrawable;
    private int mBgTintColor;
    private boolean mCharging;
    private Drawable mChargingMask;
    private Path mChargingMaskPath;
    private Paint mClearPaint;
    private Drawable mDarkBg;
    private DarkIconDispatcher mDarkIconDispatcher;
    private float mDarkIntensity;
    private int mIconId;
    private boolean mIsDigit;
    private LayerDrawable mLayerDrawable;
    private int mLevel;
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
    private Rect mProgressRect;
    private int mProgressWidth;
    private boolean mQuickCharging;
    private Drawable mQuickChargingMask;
    private Path mQuickChargingMaskPath;
    private Drawable mRoundProgress;
    private Rect mTintArea;
    private boolean mUseLegacyDrawable;
    private Drawable mWhiteBg;

    private enum BatteryStatus {
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

    public BatteryMeterIconView(Context context) {
        this(context, (AttributeSet) null);
    }

    public BatteryMeterIconView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public BatteryMeterIconView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public BatteryMeterIconView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mTintArea = new Rect();
        this.mIconId = R.raw.stat_sys_battery;
        this.mProgressRect = new Rect();
        this.mProgressDrawables = new ArrayMap<>();
        this.mProgressDarkDrawables = new ArrayMap<>();
        this.mClearPaint = new Paint(1);
        this.mClearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        this.mDarkIconDispatcher = (DarkIconDispatcher) Dependency.get(DarkIconDispatcher.class);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        setup();
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
        int saveLayer = canvas.saveLayer(0.0f, 0.0f, (float) canvas.getWidth(), (float) canvas.getHeight(), (Paint) null, 31);
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

    private void updateProgressDrawable() {
        int i;
        if (this.mUseLegacyDrawable) {
            this.mLayerDrawable = null;
            this.mProgressClipDrawable = null;
            this.mBgDrawable = null;
            setImageDrawable((Drawable) null);
            return;
        }
        LayerDrawable layerDrawable = (LayerDrawable) getContext().getResources().getDrawable(R.drawable.battery_meter);
        this.mLayerDrawable = layerDrawable;
        layerDrawable.mutate();
        Drawable findDrawableByLayerId = this.mLayerDrawable.findDrawableByLayerId(R.id.background);
        this.mBgDrawable = findDrawableByLayerId;
        findDrawableByLayerId.mutate();
        if (this.mProgressOrientationPortrait) {
            i = 80;
        } else {
            i = this.mProgressGravityStart ? 8388611 : 8388613;
        }
        Drawable drawable = getResources().getDrawable(R.drawable.battery_meter_progress_normal);
        drawable.mutate();
        ClipDrawable clipDrawable = new ClipDrawable(drawable, i, this.mProgressOrientationPortrait ? 2 : 1);
        this.mProgressClipDrawable = clipDrawable;
        clipDrawable.mutate();
        this.mLayerDrawable.setDrawableByLayerId(R.id.progress, this.mProgressClipDrawable);
        LayerDrawable layerDrawable2 = this.mLayerDrawable;
        layerDrawable2.setLayerGravity(layerDrawable2.findIndexByLayerId(R.id.progress), 17);
        LayerDrawable layerDrawable3 = this.mLayerDrawable;
        layerDrawable3.setLayerInset(layerDrawable3.findIndexByLayerId(R.id.progress), 0, 0, (int) (this.mProgressCenterLeftOffset * 2.0f), 0);
        int intrinsicWidth = this.mLayerDrawable.getIntrinsicWidth();
        int intrinsicHeight = this.mLayerDrawable.getIntrinsicHeight();
        this.mProgressWidth = this.mProgressClipDrawable.getIntrinsicWidth();
        int intrinsicHeight2 = this.mProgressClipDrawable.getIntrinsicHeight();
        this.mProgressHeight = intrinsicHeight2;
        Rect rect = this.mProgressRect;
        int i2 = this.mProgressWidth;
        rect.left = (intrinsicWidth - i2) / 2;
        rect.right = intrinsicWidth - ((intrinsicWidth - i2) / 2);
        rect.top = (intrinsicHeight - intrinsicHeight2) / 2;
        rect.bottom = intrinsicHeight - ((intrinsicHeight - intrinsicHeight2) / 2);
        setImageDrawable(this.mLayerDrawable);
    }

    public void updateResources() {
        Resources resources = getContext().getResources();
        this.mMaskScale = resources.getDimension(R.dimen.battery_meter_charging_mask_scale);
        this.mProgressCenterLeftOffset = (float) resources.getDimensionPixelSize(R.dimen.battery_meter_progress_center_left_offset);
        this.mProgressOrientationPortrait = resources.getBoolean(R.bool.battery_meter_progress_oriention_portrait);
        this.mProgressGravityStart = resources.getBoolean(R.bool.battery_meter_progress_gravity_start);
        this.mWhiteBg = resources.getDrawable(R.drawable.battery_meter_bg);
        this.mDarkBg = resources.getDrawable(R.drawable.battery_meter_bg_dark);
        Matrix matrix = new Matrix();
        float f = this.mMaskScale;
        matrix.setScale(f, f);
        this.mMaskCharging = resources.getBoolean(R.bool.battery_meter_mask_charging);
        this.mMaskProgress = resources.getBoolean(R.bool.battery_meter_mask_progress);
        if (this.mMaskCharging) {
            try {
                this.mChargingMaskPath = PathParser.createPathFromPathData(resources.getString(R.string.battery_meter_charging_mask_path));
                this.mChargingMask = resources.getDrawable(R.drawable.battery_meter_charging_mask);
                this.mChargingMaskPath.transform(matrix);
            } catch (Exception e) {
                this.mChargingMaskPath = null;
                Log.e("BatteryMeterIconView", "create ChargingMaskPath Exception=" + e);
            }
            try {
                this.mQuickChargingMaskPath = PathParser.createPathFromPathData(resources.getString(R.string.battery_meter_quick_charging_mask_path));
                this.mQuickChargingMask = resources.getDrawable(R.drawable.battery_meter_quick_charging_mask);
                this.mQuickChargingMaskPath.transform(matrix);
            } catch (Exception e2) {
                this.mQuickChargingMaskPath = null;
                Log.e("BatteryMeterIconView", "create ChargingMaskPath Exception=" + e2);
            }
        }
        if (this.mMaskProgress) {
            try {
                this.mProgressMaskPath = PathParser.createPathFromPathData(resources.getString(R.string.battery_meter_progress_mask_path));
                this.mRoundProgress = resources.getDrawable(R.drawable.battery_meter_progress_mask);
                this.mProgressMaskPath.transform(matrix);
            } catch (Exception e3) {
                this.mProgressMaskPath = null;
                Log.e("BatteryMeterIconView", "create ProgressMaskPath Exception=" + e3);
            }
        }
        this.mProgressMaskMin = resources.getInteger(R.integer.battery_meter_progress_mask_min);
        this.mProgressMaskMax = resources.getInteger(R.integer.battery_meter_progress_mask_max);
        if (this.mDarkIconDispatcher.useTint()) {
            this.mProgressDrawables.clear();
            this.mProgressDarkDrawables.clear();
            this.mBatteryLowColor = resources.getColor(R.color.status_bar_battery_low);
            this.mBatteryNormalLightColor = resources.getColor(R.color.status_bar_battery_normal_light);
            this.mBatteryNormalDarkColor = resources.getColor(R.color.status_bar_battery_normal_dark);
            this.mBatteryNormalDigitLightColor = resources.getColor(R.color.status_bar_battery_normal_digit_light);
            this.mBatteryNormalDigitDarkColor = resources.getColor(R.color.status_bar_battery_normal_digit_dark);
            this.mBatteryChargingColor = resources.getColor(R.color.status_bar_battery_charging);
            this.mBatteryPowerSaveColor = resources.getColor(R.color.status_bar_battery_power_save);
            return;
        }
        this.mProgressDrawables.put(BatteryStatus.LOW, resources.getDrawable(R.drawable.battery_meter_progress_low));
        this.mProgressDarkDrawables.put(BatteryStatus.LOW, resources.getDrawable(R.drawable.battery_meter_progress_low_dark));
        this.mProgressDrawables.put(BatteryStatus.LOW_DIGIT, resources.getDrawable(R.drawable.battery_meter_progress_low_digit));
        this.mProgressDarkDrawables.put(BatteryStatus.LOW_DIGIT, resources.getDrawable(R.drawable.battery_meter_progress_low_digit_dark));
        this.mProgressDrawables.put(BatteryStatus.NORMAL, resources.getDrawable(R.drawable.battery_meter_progress_normal));
        this.mProgressDarkDrawables.put(BatteryStatus.NORMAL, resources.getDrawable(R.drawable.battery_meter_progress_normal_dark));
        this.mProgressDrawables.put(BatteryStatus.NORMAL_DIGIT, resources.getDrawable(R.drawable.battery_meter_progress_normal_digit));
        this.mProgressDarkDrawables.put(BatteryStatus.NORMAL_DIGIT, resources.getDrawable(R.drawable.battery_meter_progress_normal_digit_dark));
        this.mProgressDrawables.put(BatteryStatus.CHARGING, resources.getDrawable(R.drawable.battery_meter_progress_charging));
        this.mProgressDarkDrawables.put(BatteryStatus.CHARGING, resources.getDrawable(R.drawable.battery_meter_progress_charging_dark));
        this.mProgressDrawables.put(BatteryStatus.CHARGING_DIGIT, resources.getDrawable(R.drawable.battery_meter_progress_charging_digit));
        this.mProgressDarkDrawables.put(BatteryStatus.CHARGING_DIGIT, resources.getDrawable(R.drawable.battery_meter_progress_charging_digit_dark));
        this.mProgressDrawables.put(BatteryStatus.POWER_SAVE, resources.getDrawable(R.drawable.battery_meter_progress_power_save));
        this.mProgressDarkDrawables.put(BatteryStatus.POWER_SAVE, resources.getDrawable(R.drawable.battery_meter_progress_power_save_dark));
        this.mProgressDrawables.put(BatteryStatus.POWER_SAVE_DIGIT, resources.getDrawable(R.drawable.battery_meter_progress_power_save_digit));
        this.mProgressDarkDrawables.put(BatteryStatus.POWER_SAVE_DIGIT, resources.getDrawable(R.drawable.battery_meter_progress_power_save_digit_dark));
    }

    public void setup() {
        updateResources();
        updateProgressDrawable();
        update(this.mLevel, this.mIconId, this.mIsDigit, this.mQuickCharging, this.mCharging, this.mPowerSave, true);
    }

    private void updateRoundProgressFlag(int i) {
        this.mNeedRoundProgress = i >= this.mProgressMaskMin && i <= this.mProgressMaskMax;
    }

    public void onDarkChanged(Rect rect, float f, int i) {
        this.mTintArea.set(rect);
        this.mDarkIntensity = f;
        this.mBgTintColor = i;
        if (this.mUseLegacyDrawable) {
            setImageDrawable(getLegacyDrawable(DarkIconDispatcherHelper.inDarkMode(rect, this, f)));
            setImageLevel(this.mLevel);
        } else if (this.mDarkIconDispatcher.useTint()) {
            this.mBgDrawable.setTintList(ColorStateList.valueOf(DarkIconDispatcherHelper.getTint(rect, this, i)));
            this.mProgressClipDrawable.setTintList(ColorStateList.valueOf(getBatteryProgressColor()));
            if (!this.mProgressClipDrawable.setLevel(this.mLevel * 100)) {
                this.mProgressClipDrawable.invalidateSelf();
            }
        } else {
            boolean inDarkMode = DarkIconDispatcherHelper.inDarkMode(rect, this, f);
            this.mLayerDrawable.setDrawableByLayerId(R.id.background, inDarkMode ? this.mDarkBg : this.mWhiteBg);
            Drawable drawable = (inDarkMode ? this.mProgressDarkDrawables : this.mProgressDrawables).get(getStatus());
            if (drawable != null) {
                this.mProgressClipDrawable.setDrawable(drawable);
            }
            if (!this.mProgressClipDrawable.setLevel(this.mLevel * 100)) {
                this.mProgressClipDrawable.invalidateSelf();
            }
        }
    }

    private Drawable getLegacyDrawable(boolean z) {
        switch (this.mIconId) {
            case R.raw.stat_sys_battery /*2131755020*/:
                if (z) {
                    return BatteryIcon.getInstance(getContext()).getGraphicIconDarkMode(this.mLevel);
                }
                return BatteryIcon.getInstance(getContext()).getGraphicIcon(this.mLevel);
            case R.raw.stat_sys_battery_charge /*2131755021*/:
                if (z) {
                    return BatteryIcon.getInstance(getContext()).getGraphicChargeIconDarkMode(this.mLevel);
                }
                return BatteryIcon.getInstance(getContext()).getGraphicChargeIcon(this.mLevel);
            case R.raw.stat_sys_battery_charge_digit /*2131755023*/:
                if (z) {
                    return BatteryIcon.getInstance(getContext()).getGraphicChargeDigitIconDarkMode(this.mLevel);
                }
                return BatteryIcon.getInstance(getContext()).getGraphicChargeDigitIcon(this.mLevel);
            case R.raw.stat_sys_battery_digital /*2131755026*/:
                if (z) {
                    return BatteryIcon.getInstance(getContext()).getGraphicDigitalIconDarkMode(this.mLevel);
                }
                return BatteryIcon.getInstance(getContext()).getGraphicDigitalIcon(this.mLevel);
            case R.raw.stat_sys_battery_power_save /*2131755028*/:
                if (z) {
                    return BatteryIcon.getInstance(getContext()).getGraphicPowerSaveIconDarkMode(this.mLevel);
                }
                return BatteryIcon.getInstance(getContext()).getGraphicPowerSaveIcon(this.mLevel);
            case R.raw.stat_sys_battery_power_save_digit /*2131755030*/:
                if (z) {
                    return BatteryIcon.getInstance(getContext()).getGraphicPowerSaveDigitIconDarkMode(this.mLevel);
                }
                return BatteryIcon.getInstance(getContext()).getGraphicPowerSaveDigitIcon(this.mLevel);
            default:
                return null;
        }
    }

    /* renamed from: com.android.systemui.miui.statusbar.BatteryMeterIconView$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$android$systemui$miui$statusbar$BatteryMeterIconView$BatteryStatus;

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
                com.android.systemui.miui.statusbar.BatteryMeterIconView$BatteryStatus[] r0 = com.android.systemui.miui.statusbar.BatteryMeterIconView.BatteryStatus.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$android$systemui$miui$statusbar$BatteryMeterIconView$BatteryStatus = r0
                com.android.systemui.miui.statusbar.BatteryMeterIconView$BatteryStatus r1 = com.android.systemui.miui.statusbar.BatteryMeterIconView.BatteryStatus.LOW     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = $SwitchMap$com$android$systemui$miui$statusbar$BatteryMeterIconView$BatteryStatus     // Catch:{ NoSuchFieldError -> 0x001d }
                com.android.systemui.miui.statusbar.BatteryMeterIconView$BatteryStatus r1 = com.android.systemui.miui.statusbar.BatteryMeterIconView.BatteryStatus.LOW_DIGIT     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                int[] r0 = $SwitchMap$com$android$systemui$miui$statusbar$BatteryMeterIconView$BatteryStatus     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.android.systemui.miui.statusbar.BatteryMeterIconView$BatteryStatus r1 = com.android.systemui.miui.statusbar.BatteryMeterIconView.BatteryStatus.NORMAL_DIGIT     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                int[] r0 = $SwitchMap$com$android$systemui$miui$statusbar$BatteryMeterIconView$BatteryStatus     // Catch:{ NoSuchFieldError -> 0x0033 }
                com.android.systemui.miui.statusbar.BatteryMeterIconView$BatteryStatus r1 = com.android.systemui.miui.statusbar.BatteryMeterIconView.BatteryStatus.POWER_SAVE     // Catch:{ NoSuchFieldError -> 0x0033 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0033 }
                r2 = 4
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0033 }
            L_0x0033:
                int[] r0 = $SwitchMap$com$android$systemui$miui$statusbar$BatteryMeterIconView$BatteryStatus     // Catch:{ NoSuchFieldError -> 0x003e }
                com.android.systemui.miui.statusbar.BatteryMeterIconView$BatteryStatus r1 = com.android.systemui.miui.statusbar.BatteryMeterIconView.BatteryStatus.POWER_SAVE_DIGIT     // Catch:{ NoSuchFieldError -> 0x003e }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x003e }
                r2 = 5
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x003e }
            L_0x003e:
                int[] r0 = $SwitchMap$com$android$systemui$miui$statusbar$BatteryMeterIconView$BatteryStatus     // Catch:{ NoSuchFieldError -> 0x0049 }
                com.android.systemui.miui.statusbar.BatteryMeterIconView$BatteryStatus r1 = com.android.systemui.miui.statusbar.BatteryMeterIconView.BatteryStatus.CHARGING     // Catch:{ NoSuchFieldError -> 0x0049 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0049 }
                r2 = 6
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0049 }
            L_0x0049:
                int[] r0 = $SwitchMap$com$android$systemui$miui$statusbar$BatteryMeterIconView$BatteryStatus     // Catch:{ NoSuchFieldError -> 0x0054 }
                com.android.systemui.miui.statusbar.BatteryMeterIconView$BatteryStatus r1 = com.android.systemui.miui.statusbar.BatteryMeterIconView.BatteryStatus.CHARGING_DIGIT     // Catch:{ NoSuchFieldError -> 0x0054 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0054 }
                r2 = 7
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0054 }
            L_0x0054:
                int[] r0 = $SwitchMap$com$android$systemui$miui$statusbar$BatteryMeterIconView$BatteryStatus     // Catch:{ NoSuchFieldError -> 0x0060 }
                com.android.systemui.miui.statusbar.BatteryMeterIconView$BatteryStatus r1 = com.android.systemui.miui.statusbar.BatteryMeterIconView.BatteryStatus.NORMAL     // Catch:{ NoSuchFieldError -> 0x0060 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0060 }
                r2 = 8
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0060 }
            L_0x0060:
                int[] r0 = $SwitchMap$com$android$systemui$miui$statusbar$BatteryMeterIconView$BatteryStatus     // Catch:{ NoSuchFieldError -> 0x006c }
                com.android.systemui.miui.statusbar.BatteryMeterIconView$BatteryStatus r1 = com.android.systemui.miui.statusbar.BatteryMeterIconView.BatteryStatus.UNKNOWN     // Catch:{ NoSuchFieldError -> 0x006c }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x006c }
                r2 = 9
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x006c }
            L_0x006c:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.miui.statusbar.BatteryMeterIconView.AnonymousClass1.<clinit>():void");
        }
    }

    private int getBatteryProgressColor() {
        switch (AnonymousClass1.$SwitchMap$com$android$systemui$miui$statusbar$BatteryMeterIconView$BatteryStatus[getStatus().ordinal()]) {
            case 1:
            case 2:
                return this.mBatteryLowColor;
            case 3:
                return DarkIconDispatcherHelper.getTint(this.mTintArea, this, ((Integer) ArgbEvaluator.getInstance().evaluate(this.mDarkIntensity, Integer.valueOf(this.mBatteryNormalDigitLightColor), Integer.valueOf(this.mBatteryNormalDigitDarkColor))).intValue());
            case 4:
            case 5:
                return this.mBatteryPowerSaveColor;
            case 6:
            case 7:
                return this.mBatteryChargingColor;
            default:
                return DarkIconDispatcherHelper.getTint(this.mTintArea, this, ((Integer) ArgbEvaluator.getInstance().evaluate(this.mDarkIntensity, Integer.valueOf(this.mBatteryNormalLightColor), Integer.valueOf(this.mBatteryNormalDarkColor))).intValue());
        }
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
            onDarkChanged(this.mTintArea, this.mDarkIntensity, this.mBgTintColor);
        }
    }

    public BatteryStatus getStatus() {
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

    public void setUseLegacyDrawable(boolean z) {
        this.mUseLegacyDrawable = z;
    }
}
