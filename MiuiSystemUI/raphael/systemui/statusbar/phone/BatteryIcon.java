package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LevelListDrawable;
import android.util.SparseArray;
import android.util.TypedValue;
import com.android.systemui.C0011R$color;
import com.android.systemui.C0016R$integer;
import com.android.systemui.C0020R$raw;
import com.android.systemui.Dependency;
import com.miui.systemui.SettingsManager;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class BatteryIcon {
    private static BatteryIcon sBatteryIcon;
    private int mBatteryColumns;
    private int mChargeDarkLevel = -1;
    private int mChargeDigitDarkLevel = -1;
    private int mChargeDigitLevel = -1;
    private int mChargeLevel = -1;
    private Context mContext;
    private int mDarkLevel = -1;
    private int mDigitalDarkLevel = -1;
    private int mDigitalLevel = -1;
    private LevelListDrawable mGraphicChargeDigitIcon;
    private LevelListDrawable mGraphicChargeDigitIconDarkMode;
    private LevelListDrawable mGraphicChargeIcon;
    private LevelListDrawable mGraphicChargeIconDarkMode;
    private LevelListDrawable mGraphicDigitalIcon;
    private LevelListDrawable mGraphicDigitalIconDarkMode;
    private LevelListDrawable mGraphicIcon;
    private LevelListDrawable mGraphicIconDarkMode;
    private LevelListDrawable mGraphicPowerSaveDigitIcon;
    private LevelListDrawable mGraphicPowerSaveDigitIconDarkMode;
    private LevelListDrawable mGraphicPowerSaveIcon;
    private LevelListDrawable mGraphicPowerSaveIconDarkMode;
    private SparseArray<ArrayList<Drawable>> mGraphicRes2Drawables = new SparseArray<>();
    private int mLevel = -1;
    private int mPowerSaveDarkLevel = -1;
    private int mPowerSaveDigitDarkLevel = -1;
    private int mPowerSaveDigitLevel = -1;
    private int mPowerSaveLevel = -1;

    public static BatteryIcon getInstance(Context context) {
        if (sBatteryIcon == null) {
            sBatteryIcon = new BatteryIcon(context);
        }
        return sBatteryIcon;
    }

    private BatteryIcon(Context context) {
        this.mContext = context;
        this.mBatteryColumns = context.getResources().getInteger(C0016R$integer.battery_columns);
    }

    public LevelListDrawable getGraphicIcon(int i) {
        int i2 = this.mLevel;
        if (i2 == -1 || i2 - i > 10 || i2 - i < 0) {
            this.mGraphicIcon = generateIcon(C0020R$raw.stat_sys_battery, i, false);
            this.mLevel = i;
        }
        return this.mGraphicIcon;
    }

    public LevelListDrawable getGraphicIconDarkMode(int i) {
        int i2 = this.mDarkLevel;
        if (i2 == -1 || i2 - i > 10 || i2 - i < 0) {
            this.mGraphicIconDarkMode = generateIcon(C0020R$raw.stat_sys_battery_darkmode, i, false);
            this.mDarkLevel = i;
        }
        if (!((SettingsManager) Dependency.get(SettingsManager.class)).getMiuiOptimizationEnabled()) {
            this.mGraphicIconDarkMode.setColorFilter(this.mContext.getResources().getColor(C0011R$color.status_bar_icon_text_color_dark_mode_cts), PorterDuff.Mode.SRC_IN);
        } else {
            this.mGraphicIconDarkMode.setColorFilter(null);
        }
        return this.mGraphicIconDarkMode;
    }

    public LevelListDrawable getGraphicDigitalIcon(int i) {
        int i2 = this.mDigitalLevel;
        if (i2 == -1 || i2 - i > 10 || i2 - i < 0) {
            this.mGraphicDigitalIcon = generateIcon(C0020R$raw.stat_sys_battery_digital, i, false);
            this.mDigitalLevel = i;
        }
        return this.mGraphicDigitalIcon;
    }

    public LevelListDrawable getGraphicDigitalIconDarkMode(int i) {
        int i2 = this.mDigitalDarkLevel;
        if (i2 == -1 || i2 - i > 10 || i2 - i < 0) {
            this.mGraphicDigitalIconDarkMode = generateIcon(C0020R$raw.stat_sys_battery_digital_darkmode, i, false);
            this.mDigitalDarkLevel = i;
        }
        if (!((SettingsManager) Dependency.get(SettingsManager.class)).getMiuiOptimizationEnabled()) {
            this.mGraphicDigitalIconDarkMode.setColorFilter(this.mContext.getResources().getColor(C0011R$color.status_bar_icon_text_color_dark_mode_cts), PorterDuff.Mode.SRC_IN);
        } else {
            this.mGraphicDigitalIconDarkMode.setColorFilter(null);
        }
        return this.mGraphicDigitalIconDarkMode;
    }

    public LevelListDrawable getGraphicChargeIcon(int i) {
        int i2 = this.mChargeLevel;
        if (i2 == -1 || i - i2 > 10 || i - i2 < 0) {
            this.mGraphicChargeIcon = generateIcon(C0020R$raw.stat_sys_battery_charge, i, true);
            this.mChargeLevel = i;
        }
        return this.mGraphicChargeIcon;
    }

    public LevelListDrawable getGraphicChargeIconDarkMode(int i) {
        int i2 = this.mChargeDarkLevel;
        if (i2 == -1 || i - i2 > 10 || i - i2 < 0) {
            this.mGraphicChargeIconDarkMode = generateIcon(C0020R$raw.stat_sys_battery_charge_darkmode, i, true);
            this.mChargeDarkLevel = i;
        }
        if (!((SettingsManager) Dependency.get(SettingsManager.class)).getMiuiOptimizationEnabled()) {
            this.mGraphicChargeIconDarkMode.setColorFilter(this.mContext.getResources().getColor(C0011R$color.status_bar_icon_text_color_dark_mode_cts), PorterDuff.Mode.SRC_IN);
        } else {
            this.mGraphicChargeIconDarkMode.setColorFilter(null);
        }
        return this.mGraphicChargeIconDarkMode;
    }

    public LevelListDrawable getGraphicChargeDigitIcon(int i) {
        int i2 = this.mChargeDigitLevel;
        if (i2 == -1 || i - i2 > 10 || i - i2 < 0) {
            this.mGraphicChargeDigitIcon = generateIcon(C0020R$raw.stat_sys_battery_charge_digit, i, true);
            this.mChargeDigitLevel = i;
        }
        return this.mGraphicChargeDigitIcon;
    }

    public LevelListDrawable getGraphicChargeDigitIconDarkMode(int i) {
        int i2 = this.mChargeDigitDarkLevel;
        if (i2 == -1 || i - i2 > 10 || i - i2 < 0) {
            this.mGraphicChargeDigitIconDarkMode = generateIcon(C0020R$raw.stat_sys_battery_charge_digit_darkmode, i, true);
            this.mChargeDigitDarkLevel = i;
        }
        if (!((SettingsManager) Dependency.get(SettingsManager.class)).getMiuiOptimizationEnabled()) {
            this.mGraphicChargeDigitIconDarkMode.setColorFilter(this.mContext.getResources().getColor(C0011R$color.status_bar_icon_text_color_dark_mode_cts), PorterDuff.Mode.SRC_IN);
        } else {
            this.mGraphicChargeDigitIconDarkMode.setColorFilter(null);
        }
        return this.mGraphicChargeDigitIconDarkMode;
    }

    public LevelListDrawable getGraphicPowerSaveIcon(int i) {
        int i2 = this.mPowerSaveLevel;
        if (i2 == -1 || i - i2 > 10 || i - i2 < 0) {
            this.mGraphicPowerSaveIcon = generateIcon(C0020R$raw.stat_sys_battery_power_save, i, true);
            this.mPowerSaveLevel = i;
        }
        return this.mGraphicPowerSaveIcon;
    }

    public LevelListDrawable getGraphicPowerSaveIconDarkMode(int i) {
        int i2 = this.mPowerSaveDarkLevel;
        if (i2 == -1 || i - i2 > 10 || i - i2 < 0) {
            this.mGraphicPowerSaveIconDarkMode = generateIcon(C0020R$raw.stat_sys_battery_power_save_darkmode, i, true);
            this.mPowerSaveDarkLevel = i;
        }
        if (!((SettingsManager) Dependency.get(SettingsManager.class)).getMiuiOptimizationEnabled()) {
            this.mGraphicPowerSaveIconDarkMode.setColorFilter(this.mContext.getResources().getColor(C0011R$color.status_bar_icon_text_color_dark_mode_cts), PorterDuff.Mode.SRC_IN);
        } else {
            this.mGraphicPowerSaveIconDarkMode.setColorFilter(null);
        }
        return this.mGraphicPowerSaveIconDarkMode;
    }

    public LevelListDrawable getGraphicPowerSaveDigitIcon(int i) {
        int i2 = this.mPowerSaveDigitLevel;
        if (i2 == -1 || i - i2 > 10 || i - i2 < 0) {
            this.mGraphicPowerSaveDigitIcon = generateIcon(C0020R$raw.stat_sys_battery_power_save_digit, i, true);
            this.mPowerSaveDigitLevel = i;
        }
        return this.mGraphicPowerSaveDigitIcon;
    }

    public LevelListDrawable getGraphicPowerSaveDigitIconDarkMode(int i) {
        int i2 = this.mPowerSaveDigitDarkLevel;
        if (i2 == -1 || i - i2 > 10 || i - i2 < 0) {
            this.mGraphicPowerSaveDigitIconDarkMode = generateIcon(C0020R$raw.stat_sys_battery_power_save_digit_darkmode, i, true);
            this.mPowerSaveDigitDarkLevel = i;
        }
        if (!((SettingsManager) Dependency.get(SettingsManager.class)).getMiuiOptimizationEnabled()) {
            this.mGraphicPowerSaveDigitIconDarkMode.setColorFilter(this.mContext.getResources().getColor(C0011R$color.status_bar_icon_text_color_dark_mode_cts), PorterDuff.Mode.SRC_IN);
        } else {
            this.mGraphicPowerSaveDigitIconDarkMode.setColorFilter(null);
        }
        return this.mGraphicPowerSaveDigitIconDarkMode;
    }

    private LevelListDrawable generateIcon(int i, int i2, boolean z) {
        int i3;
        LevelListDrawable levelListDrawable = new LevelListDrawable();
        ArrayList<Drawable> extractDrawable = extractDrawable(i);
        int size = extractDrawable.size();
        if (size > 0) {
            float f = 0.4f;
            float f2 = 100.0f / ((float) size);
            if (z) {
                i3 = i2;
            } else {
                i3 = i2 - 10;
                if (i3 < 0) {
                    i3 = 0;
                }
            }
            if (z && (i2 = i2 + 10) > 100) {
                i2 = 100;
            }
            for (int i4 = 0; i4 < size; i4++) {
                int i5 = (int) f;
                f += f2;
                int i6 = (int) f;
                if (i6 < i3 || i5 > i2) {
                    levelListDrawable.addLevel(i5, i6, null);
                } else {
                    levelListDrawable.addLevel(i5, i6, extractDrawable.get(i4));
                }
            }
        }
        levelListDrawable.setAutoMirrored(true);
        return levelListDrawable;
    }

    private ArrayList<Drawable> extractDrawable(int i) {
        ArrayList<Drawable> arrayList = this.mGraphicRes2Drawables.get(i, null);
        if (arrayList != null) {
            return arrayList;
        }
        ArrayList<Drawable> doExtractDrawable = doExtractDrawable(i);
        this.mGraphicRes2Drawables.put(i, doExtractDrawable);
        return doExtractDrawable;
    }

    private ArrayList<Drawable> doExtractDrawable(int i) {
        ArrayList<Drawable> arrayList = new ArrayList<>();
        Resources resources = this.mContext.getResources();
        TypedValue typedValue = new TypedValue();
        InputStream openRawResource = resources.openRawResource(i, typedValue);
        Bitmap decodeStream = BitmapFactory.decodeStream(openRawResource);
        try {
            openRawResource.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (decodeStream == null) {
            return arrayList;
        }
        int max = Math.max(typedValue.density, 240);
        int i2 = max == 240 ? 38 : max == 320 ? 50 : max == 640 ? 72 : 60;
        int width = decodeStream.getWidth() / this.mBatteryColumns;
        int height = decodeStream.getHeight() / i2;
        int width2 = decodeStream.getWidth() / width;
        int[] iArr = new int[(i2 * width)];
        int i3 = 0;
        while (i3 < height) {
            int i4 = 0;
            while (i4 < width2) {
                decodeStream.getPixels(iArr, 0, width, i4 * width, i3 * i2, width, i2);
                Bitmap createBitmap = Bitmap.createBitmap(iArr, 0, width, width, i2, Bitmap.Config.ARGB_8888);
                createBitmap.setDensity(max);
                arrayList.add(new BitmapDrawable(resources, createBitmap));
                i4++;
                i3 = i3;
                iArr = iArr;
            }
            i3++;
        }
        decodeStream.recycle();
        return arrayList;
    }

    public void clear() {
        this.mGraphicIcon = null;
        this.mGraphicIconDarkMode = null;
        this.mGraphicDigitalIcon = null;
        this.mGraphicDigitalIconDarkMode = null;
        this.mGraphicChargeIcon = null;
        this.mGraphicChargeIconDarkMode = null;
        this.mGraphicChargeDigitIcon = null;
        this.mGraphicChargeDigitIconDarkMode = null;
        this.mGraphicPowerSaveIcon = null;
        this.mGraphicPowerSaveIconDarkMode = null;
        this.mGraphicPowerSaveDigitIcon = null;
        this.mGraphicPowerSaveDigitIconDarkMode = null;
        this.mGraphicRes2Drawables.clear();
        this.mLevel = -1;
        this.mDarkLevel = -1;
        this.mDigitalLevel = -1;
        this.mDigitalDarkLevel = -1;
        this.mChargeLevel = -1;
        this.mChargeDarkLevel = -1;
        this.mChargeDigitLevel = -1;
        this.mChargeDigitDarkLevel = -1;
        this.mPowerSaveLevel = -1;
        this.mPowerSaveDarkLevel = -1;
        this.mPowerSaveDigitLevel = -1;
        this.mPowerSaveDigitDarkLevel = -1;
    }
}
