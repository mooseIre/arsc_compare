package com.android.systemui.statusbar.phone;

import android.animation.ArgbEvaluator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Rect;
import android.util.ArrayMap;
import android.widget.ImageView;
import androidx.appcompat.R$styleable;
import com.android.systemui.C0010R$bool;
import com.android.systemui.C0011R$color;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.DarkIconDispatcher;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.phone.LightBarTransitionsController;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.miui.systemui.DeveloperSettings;
import com.miui.systemui.SettingsManager;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class DarkIconDispatcherImpl implements SysuiDarkIconDispatcher, LightBarTransitionsController.DarkIntensityApplier, ConfigurationController.ConfigurationListener {
    private Context mContext;
    private float mDarkIntensity;
    private int mDarkModeIconColorSingleTone;
    private int mIconTint = -1;
    private int mLightModeIconColorSingleTone;
    private final ArrayMap<Object, DarkIconDispatcher.DarkReceiver> mReceivers = new ArrayMap<>();
    private final Rect mTintArea = new Rect();
    private final LightBarTransitionsController mTransitionsController;
    private boolean mUseTint;

    @Override // com.android.systemui.statusbar.phone.LightBarTransitionsController.DarkIntensityApplier
    public int getTintAnimationDuration() {
        return R$styleable.AppCompatTheme_windowFixedHeightMajor;
    }

    public DarkIconDispatcherImpl(Context context, CommandQueue commandQueue) {
        this.mDarkModeIconColorSingleTone = context.getColor(C0011R$color.dark_mode_icon_color_single_tone);
        this.mLightModeIconColorSingleTone = context.getColor(C0011R$color.light_mode_icon_color_single_tone);
        this.mTransitionsController = new LightBarTransitionsController(context, this, commandQueue);
        this.mContext = context;
        ((ConfigurationController) Dependency.get(ConfigurationController.class)).addCallback(this);
        ((SettingsManager) Dependency.get(SettingsManager.class)).registerMiuiOptimizationListener(new DeveloperSettings.MiuiOptimizationListener() {
            /* class com.android.systemui.statusbar.phone.$$Lambda$DarkIconDispatcherImpl$CkNTy0sYC2cDOG_6paOgVQFc7co */

            @Override // com.miui.systemui.DeveloperSettings.MiuiOptimizationListener
            public final void onChanged(boolean z) {
                DarkIconDispatcherImpl.this.lambda$new$0$DarkIconDispatcherImpl(z);
            }
        });
        updateResource();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$0 */
    public /* synthetic */ void lambda$new$0$DarkIconDispatcherImpl(boolean z) {
        updateResource();
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onMiuiThemeChanged(boolean z) {
        updateResource();
    }

    /* access modifiers changed from: protected */
    public void updateResource() {
        int i;
        this.mLightModeIconColorSingleTone = this.mContext.getColor(C0011R$color.light_mode_icon_color_single_tone);
        if (!((SettingsManager) Dependency.get(SettingsManager.class)).getMiuiOptimizationEnabled()) {
            i = -1728053248;
        } else {
            i = this.mContext.getColor(C0011R$color.dark_mode_icon_color_single_tone);
        }
        this.mDarkModeIconColorSingleTone = i;
        this.mUseTint = this.mContext.getResources().getBoolean(C0010R$bool.use_status_bar_tint);
        applyDarkIntensity(this.mDarkIntensity);
    }

    @Override // com.android.systemui.plugins.DarkIconDispatcher
    public boolean useTint() {
        return this.mUseTint;
    }

    @Override // com.android.systemui.plugins.DarkIconDispatcher
    public int getLightModeIconColorSingleTone() {
        return this.mLightModeIconColorSingleTone;
    }

    @Override // com.android.systemui.plugins.DarkIconDispatcher
    public int getDarkModeIconColorSingleTone() {
        return this.mDarkModeIconColorSingleTone;
    }

    @Override // com.android.systemui.plugins.DarkIconDispatcher
    public void reapply() {
        applyIconTint();
    }

    @Override // com.android.systemui.statusbar.phone.SysuiDarkIconDispatcher
    public LightBarTransitionsController getTransitionsController() {
        return this.mTransitionsController;
    }

    @Override // com.android.systemui.plugins.DarkIconDispatcher
    public void addDarkReceiver(DarkIconDispatcher.DarkReceiver darkReceiver) {
        this.mReceivers.put(darkReceiver, darkReceiver);
        darkReceiver.onDarkChanged(this.mTintArea, this.mDarkIntensity, this.mIconTint, this.mLightModeIconColorSingleTone, this.mDarkModeIconColorSingleTone, this.mUseTint);
    }

    @Override // com.android.systemui.plugins.DarkIconDispatcher
    public void addDarkReceiver(ImageView imageView) {
        $$Lambda$DarkIconDispatcherImpl$_IbDO1rXkRypT7Wkg2xIt52ZKDk r0 = new DarkIconDispatcher.DarkReceiver(imageView) {
            /* class com.android.systemui.statusbar.phone.$$Lambda$DarkIconDispatcherImpl$_IbDO1rXkRypT7Wkg2xIt52ZKDk */
            public final /* synthetic */ ImageView f$1;

            {
                this.f$1 = r2;
            }

            @Override // com.android.systemui.plugins.DarkIconDispatcher.DarkReceiver
            public final void onDarkChanged(Rect rect, float f, int i, int i2, int i3, boolean z) {
                DarkIconDispatcherImpl.this.lambda$addDarkReceiver$1$DarkIconDispatcherImpl(this.f$1, rect, f, i, i2, i3, z);
            }
        };
        this.mReceivers.put(imageView, r0);
        r0.onDarkChanged(this.mTintArea, this.mDarkIntensity, this.mIconTint, this.mLightModeIconColorSingleTone, this.mDarkModeIconColorSingleTone, this.mUseTint);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$addDarkReceiver$1 */
    public /* synthetic */ void lambda$addDarkReceiver$1$DarkIconDispatcherImpl(ImageView imageView, Rect rect, float f, int i, int i2, int i3, boolean z) {
        imageView.setImageTintList(ColorStateList.valueOf(DarkIconDispatcher.getTint(this.mTintArea, imageView, this.mIconTint)));
    }

    @Override // com.android.systemui.plugins.DarkIconDispatcher
    public void removeDarkReceiver(DarkIconDispatcher.DarkReceiver darkReceiver) {
        this.mReceivers.remove(darkReceiver);
    }

    @Override // com.android.systemui.plugins.DarkIconDispatcher
    public void removeDarkReceiver(ImageView imageView) {
        this.mReceivers.remove(imageView);
    }

    @Override // com.android.systemui.plugins.DarkIconDispatcher
    public void applyDark(DarkIconDispatcher.DarkReceiver darkReceiver) {
        this.mReceivers.get(darkReceiver).onDarkChanged(this.mTintArea, this.mDarkIntensity, this.mIconTint, this.mLightModeIconColorSingleTone, this.mDarkModeIconColorSingleTone, this.mUseTint);
    }

    @Override // com.android.systemui.plugins.DarkIconDispatcher
    public void setIconsDarkArea(Rect rect) {
        if (rect != null || !this.mTintArea.isEmpty()) {
            if (rect == null) {
                this.mTintArea.setEmpty();
            } else {
                this.mTintArea.set(rect);
            }
            applyIconTint();
        }
    }

    @Override // com.android.systemui.statusbar.phone.LightBarTransitionsController.DarkIntensityApplier
    public void applyDarkIntensity(float f) {
        this.mDarkIntensity = f;
        this.mIconTint = ((Integer) ArgbEvaluator.getInstance().evaluate(f, Integer.valueOf(this.mLightModeIconColorSingleTone), Integer.valueOf(this.mDarkModeIconColorSingleTone))).intValue();
        applyIconTint();
    }

    private void applyIconTint() {
        for (int i = 0; i < this.mReceivers.size(); i++) {
            this.mReceivers.valueAt(i).onDarkChanged(this.mTintArea, this.mDarkIntensity, this.mIconTint, this.mLightModeIconColorSingleTone, this.mDarkModeIconColorSingleTone, this.mUseTint);
        }
    }

    @Override // com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("DarkIconDispatcher: ");
        printWriter.println("  mIconTint: 0x" + Integer.toHexString(this.mIconTint));
        printWriter.println("  mDarkIntensity: " + this.mDarkIntensity + "f");
        StringBuilder sb = new StringBuilder();
        sb.append("  mTintArea: ");
        sb.append(this.mTintArea);
        printWriter.println(sb.toString());
        printWriter.println("  mLightModeIconColorSingleTone = " + this.mLightModeIconColorSingleTone);
        printWriter.println("  mDarkModeIconColorSingleTone = " + this.mDarkModeIconColorSingleTone);
    }
}
