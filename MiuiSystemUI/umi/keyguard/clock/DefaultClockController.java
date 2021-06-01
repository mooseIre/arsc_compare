package com.android.keyguard.clock;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.android.internal.colorextraction.ColorExtractor;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0017R$layout;
import com.android.systemui.C0021R$string;
import com.android.systemui.colorextraction.SysuiColorExtractor;
import com.android.systemui.plugins.ClockPlugin;
import java.util.TimeZone;

public class DefaultClockController implements ClockPlugin {
    private final SysuiColorExtractor mColorExtractor;
    private final LayoutInflater mLayoutInflater;
    private final ViewPreviewer mRenderer = new ViewPreviewer();
    private final Resources mResources;
    private TextView mTextDate;
    private TextView mTextTime;
    private View mView;

    @Override // com.android.systemui.plugins.ClockPlugin
    public String getName() {
        return "default";
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public View getView() {
        return null;
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public void onTimeTick() {
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public void onTimeZoneChanged(TimeZone timeZone) {
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public void setColorPalette(boolean z, int[] iArr) {
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public void setDarkAmount(float f) {
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public void setStyle(Paint.Style style) {
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public boolean shouldShowStatusArea() {
        return true;
    }

    public DefaultClockController(Resources resources, LayoutInflater layoutInflater, SysuiColorExtractor sysuiColorExtractor) {
        this.mResources = resources;
        this.mLayoutInflater = layoutInflater;
        this.mColorExtractor = sysuiColorExtractor;
    }

    private void createViews() {
        View inflate = this.mLayoutInflater.inflate(C0017R$layout.default_clock_preview, (ViewGroup) null);
        this.mView = inflate;
        this.mTextTime = (TextView) inflate.findViewById(C0015R$id.time);
        this.mTextDate = (TextView) this.mView.findViewById(C0015R$id.date);
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public void onDestroyView() {
        this.mView = null;
        this.mTextTime = null;
        this.mTextDate = null;
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public String getTitle() {
        return this.mResources.getString(C0021R$string.clock_title_default);
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public Bitmap getThumbnail() {
        return BitmapFactory.decodeResource(this.mResources, C0013R$drawable.default_thumbnail);
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public Bitmap getPreview(int i, int i2) {
        View bigClockView = getBigClockView();
        setDarkAmount(1.0f);
        setTextColor(-1);
        ColorExtractor.GradientColors colors = this.mColorExtractor.getColors(2);
        setColorPalette(colors.supportsDarkText(), colors.getColorPalette());
        onTimeTick();
        return this.mRenderer.createPreview(bigClockView, i, i2);
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public View getBigClockView() {
        if (this.mView == null) {
            createViews();
        }
        return this.mView;
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public int getPreferredY(int i) {
        return i / 2;
    }

    @Override // com.android.systemui.plugins.ClockPlugin
    public void setTextColor(int i) {
        this.mTextTime.setTextColor(i);
        this.mTextDate.setTextColor(i);
    }
}
