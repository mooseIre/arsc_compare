package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.DisplayInfo;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import com.android.keyguard.CarrierText;
import com.android.systemui.BatteryMeterView;
import com.android.systemui.Dependency;
import com.android.systemui.DisplayCutoutCompat;
import com.android.systemui.miui.statusbar.phone.MiuiStatusBarPromptController;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.Icons;
import com.android.systemui.statusbar.NetworkSpeedView;
import com.android.systemui.statusbar.StatusBarIconView;
import com.android.systemui.statusbar.phone.StatusBarTypeController;
import com.android.systemui.statusbar.policy.DarkIconDispatcher;
import com.android.systemui.statusbar.policy.DarkIconDispatcherHelper;

public class KeyguardStatusBarView extends RelativeLayout implements MiuiStatusBarPromptController.OnPromptStateChangedListener, StatusBarTypeController.StatusBarTypeChangeListener {
    /* access modifiers changed from: private */
    public Rect mArea;
    private boolean mBlockClickActionToStatusBar;
    private LinearLayout mCarrierContainer;
    private CarrierText mCarrierLabel;
    private FrameLayout mCarrierSuperContainer;
    /* access modifiers changed from: private */
    public KeyguardStatusBarViewController mController;
    private StatusBarTypeController.CutoutType mCutoutType;
    private boolean mDark;
    private DarkIconDispatcher mDarkIconDispatcher = ((DarkIconDispatcher) Dependency.get(DarkIconDispatcher.class));
    /* access modifiers changed from: private */
    public float mDarkIntensity;
    private int mDarkModeIconColorSingleTone;
    private boolean mIconsDarkInExpanded;
    private int mLightModeIconColorSingleTone;
    private boolean mStatusBarExpanded;
    private MiuiStatusBarPromptController mStatusBarPrompt;
    public LinearLayout mStatusIcons;
    private LinearLayout mSystemIcons;
    private ViewGroup mSystemIconsSuperContainer;
    /* access modifiers changed from: private */
    public int mTint;

    public boolean hasOverlappingRendering() {
        return false;
    }

    public KeyguardStatusBarView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mController = StatusBarFactory.getInstance().getKeyguardStatusBarViewController(context);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mSystemIconsSuperContainer = (ViewGroup) findViewById(R.id.system_icons_super_container);
        this.mCarrierSuperContainer = (FrameLayout) findViewById(R.id.keyguard_carrier_super_container);
        this.mCarrierContainer = (LinearLayout) findViewById(R.id.keyguard_carrier_container);
        this.mCarrierLabel = (CarrierText) findViewById(R.id.keyguard_carrier_text);
        this.mDarkModeIconColorSingleTone = this.mContext.getColor(R.color.dark_mode_icon_color_single_tone);
        this.mLightModeIconColorSingleTone = this.mContext.getColor(R.color.light_mode_icon_color_single_tone);
        Display defaultDisplay = ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay();
        DisplayInfo displayInfo = new DisplayInfo();
        defaultDisplay.getDisplayInfo(displayInfo);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        defaultDisplay.getRealMetrics(displayMetrics);
        if (DisplayCutoutCompat.isCutoutLeftTop(displayInfo, displayMetrics.widthPixels)) {
            this.mCarrierLabel.setShowStyle(-1);
        }
        this.mStatusBarPrompt = (MiuiStatusBarPromptController) Dependency.get(MiuiStatusBarPromptController.class);
        this.mStatusBarPrompt.addStatusBarPrompt("KeyguardStatusBarView", (StatusBar) null, this, 7, this);
        this.mStatusBarPrompt.setPromptSosTypeImage("KeyguardStatusBarView");
        updateCarrierSuperContainer();
        refreshViews();
    }

    /* access modifiers changed from: private */
    public void updateCarrierSuperContainer() {
        int dimensionPixelSize = this.mContext.getResources().getDimensionPixelSize(R.dimen.statusbar_carrier_max_width);
        int dimensionPixelSize2 = this.mContext.getResources().getDimensionPixelSize(R.dimen.statusbar_carrier_width_for_hide_norch);
        this.mCutoutType = ((StatusBarTypeController) Dependency.get(StatusBarTypeController.class)).getCutoutType();
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) this.mCarrierSuperContainer.getLayoutParams();
        StatusBarTypeController.CutoutType cutoutType = this.mCutoutType;
        if (cutoutType == StatusBarTypeController.CutoutType.NONE || cutoutType == StatusBarTypeController.CutoutType.HOLE) {
            this.mCarrierLabel.setMaxWidth(dimensionPixelSize2);
            layoutParams.width = -2;
        } else {
            this.mCarrierLabel.setMaxWidth(dimensionPixelSize);
            layoutParams.width = dimensionPixelSize;
        }
        this.mCarrierSuperContainer.setLayoutParams(layoutParams);
    }

    public void onCutoutTypeChanged() {
        post(new Runnable() {
            public void run() {
                if (KeyguardStatusBarView.this.isAttachedToWindow()) {
                    KeyguardStatusBarView.this.mController.hideStatusIcons();
                }
                KeyguardStatusBarView.this.mController.destroy();
                KeyguardStatusBarViewController unused = KeyguardStatusBarView.this.mController = StatusBarFactory.getInstance().getKeyguardStatusBarViewController(KeyguardStatusBarView.this.getContext());
                KeyguardStatusBarView.this.updateCarrierSuperContainer();
                KeyguardStatusBarView.this.refreshViews();
                if (KeyguardStatusBarView.this.isAttachedToWindow()) {
                    KeyguardStatusBarView.this.mController.showStatusIcons();
                }
                if (KeyguardStatusBarView.this.mArea != null) {
                    KeyguardStatusBarView.this.mController.setDarkMode(KeyguardStatusBarView.this.mArea, KeyguardStatusBarView.this.mDarkIntensity, KeyguardStatusBarView.this.mTint);
                }
            }
        });
    }

    /* access modifiers changed from: private */
    public void refreshViews() {
        this.mSystemIconsSuperContainer.removeAllViews();
        LayoutInflater.from(getContext()).inflate(this.mController.getLayoutId(), this.mSystemIconsSuperContainer, true);
        this.mSystemIcons = (LinearLayout) findViewById(R.id.system_icons);
        this.mStatusIcons = (LinearLayout) findViewById(R.id.statusIcons);
        this.mController.init(this);
        ((NetworkSpeedView) this.mSystemIcons.findViewById(R.id.network_speed_view)).setNotch(this.mController.isNotch());
        ((BatteryMeterView) findViewById(R.id.battery)).setNotchEar(this.mController.isNotch());
        updateNotchPromptViewLayout(this.mCarrierContainer);
        this.mController.updateNotchVisible();
        setDarkMode(this.mDark);
    }

    public void setVisibility(int i) {
        super.setVisibility(i);
        if (i != 0) {
            this.mSystemIconsSuperContainer.animate().cancel();
            this.mSystemIconsSuperContainer.setTranslationX(0.0f);
        }
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked == 0) {
            this.mBlockClickActionToStatusBar = this.mStatusBarPrompt.blockClickAction();
            if (this.mBlockClickActionToStatusBar) {
                return true;
            }
        } else if (actionMasked == 1 && this.mBlockClickActionToStatusBar && this.mStatusBarPrompt.getTouchRegion("KeyguardStatusBarView").contains((int) motionEvent.getRawX(), (int) motionEvent.getRawY())) {
            this.mStatusBarPrompt.handleClickAction();
            this.mBlockClickActionToStatusBar = false;
            return true;
        }
        return super.onTouchEvent(motionEvent);
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        ((StatusBarTypeController) Dependency.get(StatusBarTypeController.class)).addCallback(this);
        this.mController.showStatusIcons();
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ((StatusBarTypeController) Dependency.get(StatusBarTypeController.class)).removeCallback(this);
        this.mController.hideStatusIcons();
    }

    public void statusBarExpandChanged(boolean z, boolean z2) {
        if (this.mStatusBarExpanded != z || this.mIconsDarkInExpanded != z2) {
            this.mStatusBarExpanded = z;
            this.mIconsDarkInExpanded = z2;
            setDarkMode(this.mDark);
        }
    }

    public void setDarkMode(boolean z) {
        this.mDark = z;
        applyDarkMode();
    }

    private void applyDarkMode() {
        this.mDarkModeIconColorSingleTone = this.mContext.getColor(R.color.dark_mode_icon_color_single_tone);
        this.mLightModeIconColorSingleTone = this.mContext.getColor(R.color.light_mode_icon_color_single_tone);
        this.mArea = new Rect(0, 0, 0, 0);
        this.mDarkIntensity = ((this.mStatusBarExpanded || !this.mDark) && (!this.mStatusBarExpanded || !this.mIconsDarkInExpanded)) ? 0.0f : 1.0f;
        this.mTint = ((this.mStatusBarExpanded || !this.mDark) && (!this.mStatusBarExpanded || !this.mIconsDarkInExpanded)) ? this.mLightModeIconColorSingleTone : this.mDarkModeIconColorSingleTone;
        CarrierText carrierText = this.mCarrierLabel;
        carrierText.setTextColor(DarkIconDispatcherHelper.getTint(this.mArea, carrierText, this.mTint));
        this.mStatusBarPrompt.updateSosImageDark(this.mDark, this.mArea, this.mDarkIntensity);
        for (int i = 0; i < this.mSystemIcons.getChildCount(); i++) {
            View childAt = this.mSystemIcons.getChildAt(i);
            if (childAt instanceof DarkIconDispatcher.DarkReceiver) {
                ((DarkIconDispatcher.DarkReceiver) childAt).onDarkChanged(this.mArea, this.mDarkIntensity, this.mTint);
            }
        }
        setDarkMode(this.mStatusIcons, this.mArea, this.mDarkIntensity);
        this.mController.setDarkMode(this.mArea, this.mDarkIntensity, this.mTint);
    }

    public void setDarkMode(ViewGroup viewGroup, Rect rect, float f) {
        if (viewGroup != null) {
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                if (viewGroup.getChildAt(i) instanceof StatusBarIconView) {
                    StatusBarIconView statusBarIconView = (StatusBarIconView) viewGroup.getChildAt(i);
                    statusBarIconView.setImageTintMode(PorterDuff.Mode.SRC_IN);
                    if (this.mDarkIconDispatcher.useTint()) {
                        statusBarIconView.setImageResource(Icons.get(Integer.valueOf(statusBarIconView.getStatusBarIcon().icon.getResId()), false));
                        statusBarIconView.setImageTintList(ColorStateList.valueOf(DarkIconDispatcherHelper.getTint(this.mArea, statusBarIconView, this.mTint)));
                    } else {
                        statusBarIconView.setImageTintList((ColorStateList) null);
                        statusBarIconView.setImageResource(Icons.get(Integer.valueOf(statusBarIconView.getStatusBarIcon().icon.getResId()), DarkIconDispatcherHelper.inDarkMode(rect, statusBarIconView, f)));
                    }
                }
            }
        }
    }

    private void updateNotchPromptViewLayout(View view) {
        if (view != null) {
            boolean isPromptCenter = this.mController.isPromptCenter();
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) view.getLayoutParams();
            int i = 17;
            if ((layoutParams.gravity == 17) != isPromptCenter) {
                if (!isPromptCenter) {
                    i = 8388627;
                }
                layoutParams.gravity = i;
                view.setLayoutParams(layoutParams);
            }
        }
    }

    public void onPromptStateChanged(boolean z, String str) {
        this.mCarrierLabel.forceHide(!z && this.mCutoutType == StatusBarTypeController.CutoutType.NOTCH);
    }
}
