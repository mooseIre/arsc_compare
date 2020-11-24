package com.android.systemui.statusbar.phone;

import android.animation.ArgbEvaluator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.util.ArrayMap;
import android.widget.ImageView;
import androidx.appcompat.R$styleable;
import com.android.systemui.C0007R$bool;
import com.android.systemui.C0008R$color;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.DarkIconDispatcher;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.phone.LightBarTransitionsController;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.miui.systemui.NotificationSettings;
import com.miui.systemui.SettingsManager;
import com.miui.systemui.util.CommonUtil;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class DarkIconDispatcherImpl implements SysuiDarkIconDispatcher, LightBarTransitionsController.DarkIntensityApplier, ConfigurationController.ConfigurationListener {
    private Context mContext;
    private float mDarkIntensity;
    private int mDarkModeIconColorSingleTone;
    private int mIconTint = -1;
    private Configuration mLastConfiguration;
    private int mLightModeIconColorSingleTone;
    private final ArrayMap<Object, DarkIconDispatcher.DarkReceiver> mReceivers = new ArrayMap<>();
    private final Rect mTintArea = new Rect();
    private final LightBarTransitionsController mTransitionsController;
    private boolean mUseTint;

    public int getTintAnimationDuration() {
        return R$styleable.AppCompatTheme_windowFixedHeightMajor;
    }

    public DarkIconDispatcherImpl(Context context, CommandQueue commandQueue) {
        this.mDarkModeIconColorSingleTone = context.getColor(C0008R$color.dark_mode_icon_color_single_tone);
        this.mLightModeIconColorSingleTone = context.getColor(C0008R$color.light_mode_icon_color_single_tone);
        this.mTransitionsController = new LightBarTransitionsController(context, this, commandQueue);
        this.mContext = context;
        this.mUseTint = context.getResources().getBoolean(C0007R$bool.use_status_bar_tint);
        this.mLastConfiguration = new Configuration(context.getResources().getConfiguration());
        ((ConfigurationController) Dependency.get(ConfigurationController.class)).addCallback(this);
        ((SettingsManager) Dependency.get(SettingsManager.class)).registerNotifStyleListener(new NotificationSettings.StyleListener() {
            public final void onChanged(int i) {
                DarkIconDispatcherImpl.this.lambda$new$0$DarkIconDispatcherImpl(i);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$0 */
    public /* synthetic */ void lambda$new$0$DarkIconDispatcherImpl(int i) {
        applyIconTint();
    }

    public void onConfigChanged(Configuration configuration) {
        if (CommonUtil.isThemeResourcesChanged(this.mLastConfiguration.updateFrom(configuration), configuration.extraConfig.themeChangedFlags)) {
            updateResource();
        }
    }

    /* access modifiers changed from: protected */
    public void updateResource() {
        this.mLightModeIconColorSingleTone = this.mContext.getColor(C0008R$color.light_mode_icon_color_single_tone);
        this.mDarkModeIconColorSingleTone = this.mContext.getColor(C0008R$color.dark_mode_icon_color_single_tone);
        this.mUseTint = this.mContext.getResources().getBoolean(C0007R$bool.use_status_bar_tint);
        applyIconTint();
    }

    public boolean useTint() {
        return this.mUseTint;
    }

    public int getLightModeIconColorSingleTone() {
        return this.mLightModeIconColorSingleTone;
    }

    public int getDarkModeIconColorSingleTone() {
        return this.mDarkModeIconColorSingleTone;
    }

    public LightBarTransitionsController getTransitionsController() {
        return this.mTransitionsController;
    }

    public void addDarkReceiver(DarkIconDispatcher.DarkReceiver darkReceiver) {
        this.mReceivers.put(darkReceiver, darkReceiver);
        darkReceiver.onDarkChanged(this.mTintArea, this.mDarkIntensity, this.mIconTint, this.mLightModeIconColorSingleTone, this.mDarkModeIconColorSingleTone, this.mUseTint);
    }

    public void addDarkReceiver(ImageView imageView) {
        $$Lambda$DarkIconDispatcherImpl$_IbDO1rXkRypT7Wkg2xIt52ZKDk r0 = new DarkIconDispatcher.DarkReceiver(imageView) {
            public final /* synthetic */ ImageView f$1;

            {
                this.f$1 = r2;
            }

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

    public void removeDarkReceiver(DarkIconDispatcher.DarkReceiver darkReceiver) {
        this.mReceivers.remove(darkReceiver);
    }

    public void removeDarkReceiver(ImageView imageView) {
        this.mReceivers.remove(imageView);
    }

    public void applyDark(DarkIconDispatcher.DarkReceiver darkReceiver) {
        this.mReceivers.get(darkReceiver).onDarkChanged(this.mTintArea, this.mDarkIntensity, this.mIconTint, this.mLightModeIconColorSingleTone, this.mDarkModeIconColorSingleTone, this.mUseTint);
    }

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

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("DarkIconDispatcher: ");
        printWriter.println("  mIconTint: 0x" + Integer.toHexString(this.mIconTint));
        printWriter.println("  mDarkIntensity: " + this.mDarkIntensity + "f");
        StringBuilder sb = new StringBuilder();
        sb.append("  mTintArea: ");
        sb.append(this.mTintArea);
        printWriter.println(sb.toString());
    }
}
