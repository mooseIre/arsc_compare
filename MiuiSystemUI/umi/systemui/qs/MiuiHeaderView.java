package com.android.systemui.qs;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.android.systemui.C0010R$bool;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0021R$string;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.statusbar.notification.unimportant.FoldListener;
import com.android.systemui.statusbar.notification.unimportant.FoldManager;
import com.android.systemui.statusbar.policy.MiuiClock;
import com.android.systemui.tuner.TunerService;
import miuix.animation.Folme;
import miuix.animation.IFolme;
import miuix.animation.IStateStyle;
import miuix.animation.base.AnimConfig;
import miuix.animation.controller.AnimState;
import miuix.animation.property.ViewProperty;

public abstract class MiuiHeaderView extends RelativeLayout implements View.OnClickListener, TunerService.Tunable, FoldListener {
    private boolean isShortcurVisible;
    private final ActivityStarter mActStarter;
    protected MiuiClock mClock;
    private IFolme mClockStyle;
    private IFolme mDateStyle;
    protected MiuiClock mDateView;
    protected AnimConfig mHeightConfig;
    protected AnimConfig mItemConfig;
    protected int mLastOrientation;
    private float mNormalHeight;
    protected ImageView mShortcut;
    protected int mShortcutDestination;
    private IFolme mShortcutStyle;
    protected View mSysIconArea;
    private IFolme mSysIconAreaStyle;
    private IStateStyle mTotalHeightStyle;
    private float mUnimportHeight;
    protected ImageView mUnimportantBack;
    private IFolme mUnimportantFolme;
    protected View mUnimportantHeader;
    protected ImageView mUnimportantIcon;

    public abstract void regionChanged();

    public abstract void themeChanged();

    public MiuiHeaderView(Context context) {
        this(context, null);
    }

    public MiuiHeaderView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public MiuiHeaderView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mLastOrientation = 1;
        this.mShortcutDestination = 0;
        this.isShortcurVisible = true;
        this.mActStarter = (ActivityStarter) Dependency.get(ActivityStarter.class);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        MiuiClock miuiClock = (MiuiClock) findViewById(C0015R$id.date_time);
        this.mDateView = miuiClock;
        miuiClock.setOnClickListener(this);
        MiuiClock miuiClock2 = (MiuiClock) findViewById(C0015R$id.big_time);
        this.mClock = miuiClock2;
        miuiClock2.setOnClickListener(this);
        try {
            if (!getContext().getResources().getBoolean(C0010R$bool.header_big_time_use_system_font)) {
                this.mClock.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/MiClock-Light.otf"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        ImageView imageView = (ImageView) findViewById(C0015R$id.notification_shade_shortcut);
        this.mShortcut = imageView;
        imageView.setOnClickListener(this);
        this.mShortcut.setContentDescription(getResources().getString(C0021R$string.accessibility_settings));
        this.mSysIconArea = findViewById(C0015R$id.system_icon_area);
        this.mUnimportantHeader = findViewById(C0015R$id.unimportant_header);
        ImageView imageView2 = (ImageView) findViewById(C0015R$id.unimportant_back);
        this.mUnimportantBack = imageView2;
        imageView2.setOnClickListener(this);
        ImageView imageView3 = (ImageView) findViewById(C0015R$id.unimportant_icon);
        this.mUnimportantIcon = imageView3;
        imageView3.setOnClickListener(this);
    }

    /* JADX WARNING: Removed duplicated region for block: B:30:0x0075  */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x0082  */
    /* JADX WARNING: Removed duplicated region for block: B:36:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onClick(android.view.View r7) {
        /*
        // Method dump skipped, instructions count: 146
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.qs.MiuiHeaderView.onClick(android.view.View):void");
    }

    /* access modifiers changed from: protected */
    public void updateShortCutVisibility(int i) {
        this.isShortcurVisible = i == 0;
    }

    private void showNormalNotificationsAnim() {
        FoldManager.Companion.notifyListeners(5);
    }

    private Intent jump2NotificationControlCenterSettings() {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setClassName("com.android.settings", "com.android.settings.SubSettings");
        intent.putExtra(":settings:show_fragment", "com.android.settings.NotificationControlCenterSettings");
        intent.putExtra(":settings:show_fragment_title", getResources().getString(C0021R$string.notification_control_center));
        return intent;
    }

    /* access modifiers changed from: protected */
    public void initFolme() {
        this.mTotalHeightStyle = Folme.useAt(this).state();
        AnimConfig animConfig = new AnimConfig();
        animConfig.setEase(-2, 0.95f, 0.25f);
        this.mHeightConfig = animConfig;
        AnimConfig animConfig2 = new AnimConfig();
        animConfig2.setEase(-2, 0.95f, 0.25f);
        this.mItemConfig = animConfig2;
        this.mUnimportantFolme = Folme.useAt(this.mUnimportantHeader);
        this.mSysIconAreaStyle = Folme.useAt(this.mSysIconArea);
        this.mShortcutStyle = Folme.useAt(this.mShortcut);
        this.mDateStyle = Folme.useAt(this.mDateView);
        this.mClockStyle = Folme.useAt(this.mClock);
        resetTranslationY();
    }

    private AnimState getAnimState(int i, boolean z) {
        String str;
        String str2;
        float f = 0.0f;
        float f2 = (z || this.mLastOrientation == 2) ? 0.0f : -150.0f;
        if (i == 1) {
            str2 = z ? "normal_sysicon_visible" : "normal_sysicon_invisible";
        } else if (i == 2) {
            str2 = z ? "normal_shortcut_visible" : "normal_shortcut_invisible";
        } else if (i == 3) {
            str2 = z ? "normal_date_visible" : "nromal_date_invisible";
        } else if (i != 4) {
            str = z ? "unimportant_visible" : "unimportant_invisible";
            if (!z && this.mLastOrientation != 2) {
                f = 150.0f;
            }
            f2 = f;
            AnimState animState = new AnimState(str);
            animState.add(ViewProperty.TRANSLATION_Y, (double) f2);
            return animState;
        } else {
            str2 = z ? "normal_clock_visible" : "normal_clock_invisible";
        }
        str = str2;
        AnimState animState2 = new AnimState(str);
        animState2.add(ViewProperty.TRANSLATION_Y, (double) f2);
        return animState2;
    }

    private AnimState getHeightAnimState(boolean z) {
        String str = z ? "normal_header_view" : "unimportant_header_view";
        float normalHeight = z ? getNormalHeight() : getUnimportantHeight();
        AnimState animState = new AnimState(str);
        animState.add(ViewProperty.HEIGHT, (double) normalHeight);
        return animState;
    }

    public void setNormalHeight(float f) {
        this.mNormalHeight = f;
    }

    public void setUnimportantHeight(float f) {
        this.mUnimportHeight = f;
    }

    public float getNormalHeight() {
        return this.mNormalHeight;
    }

    public float getUnimportantHeight() {
        return this.mUnimportHeight;
    }

    @Override // com.android.systemui.statusbar.notification.unimportant.FoldListener
    public void showUnimportantNotifications() {
        if (this.mLastOrientation == 2) {
            this.mItemConfig.setDelay(0);
            this.mHeightConfig.setDelay(0);
        } else {
            this.mItemConfig.setDelay(0);
            this.mHeightConfig.setDelay(250);
        }
        this.mTotalHeightStyle.to(getHeightAnimState(false), this.mHeightConfig);
        this.mSysIconAreaStyle.visible().hide(this.mItemConfig);
        this.mSysIconAreaStyle.state().to(getAnimState(1, false), this.mItemConfig);
        if (this.isShortcurVisible) {
            this.mShortcutStyle.visible().hide(this.mItemConfig);
            this.mShortcutStyle.state().to(getAnimState(2, false), this.mItemConfig);
        }
        this.mDateStyle.visible().hide(this.mItemConfig);
        this.mDateStyle.state().to(getAnimState(3, false), this.mItemConfig);
        this.mClockStyle.visible().hide(this.mItemConfig);
        this.mClockStyle.state().to(getAnimState(4, false), this.mItemConfig);
        this.mUnimportantHeader.setVisibility(0);
        this.mUnimportantFolme.visible().show(this.mItemConfig);
        if (this.mUnimportantHeader.getTranslationY() == 0.0f) {
            this.mUnimportantFolme.state().setTo(getAnimState(5, false), this.mItemConfig);
        }
        this.mUnimportantFolme.state().to(getAnimState(5, true), this.mItemConfig);
    }

    /* access modifiers changed from: protected */
    public void showNotificationsAnim() {
        if (this.mLastOrientation == 2) {
            this.mItemConfig.setDelay(0);
            this.mHeightConfig.setDelay(150);
        } else {
            this.mItemConfig.setDelay(150);
            this.mHeightConfig.setDelay(0);
        }
        this.mTotalHeightStyle.to(getHeightAnimState(true), this.mHeightConfig);
        this.mSysIconAreaStyle.visible().show(this.mItemConfig);
        this.mSysIconAreaStyle.state().to(getAnimState(1, true), this.mItemConfig);
        if (this.isShortcurVisible) {
            this.mShortcutStyle.visible().show(this.mItemConfig);
            this.mShortcutStyle.state().to(getAnimState(2, true), this.mItemConfig);
        }
        this.mDateStyle.visible().show(this.mItemConfig);
        this.mDateStyle.state().to(getAnimState(3, true), this.mItemConfig);
        this.mClockStyle.visible().show(this.mItemConfig);
        this.mClockStyle.state().to(getAnimState(4, true), this.mItemConfig);
        this.mUnimportantFolme.visible().hide(this.mItemConfig);
        this.mUnimportantFolme.state().to(getAnimState(5, false), this.mItemConfig);
    }

    /* access modifiers changed from: protected */
    public void showNotificationWithoutAnim() {
        cancelAllFolme();
        resetViewVisible(this.mDateView);
        resetViewVisible(this.mClock);
        resetViewVisible(this.mSysIconArea);
        if (this.isShortcurVisible) {
            resetViewVisible(this.mShortcut);
        }
        resetViewGone(this.mUnimportantHeader);
        resetTranslationY();
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        layoutParams.height = -2;
        setLayoutParams(layoutParams);
    }

    /* access modifiers changed from: protected */
    public void showUnimportantWithoutAnim() {
        cancelAllFolme();
        resetTranslationY();
        resetViewGone(this.mDateView);
        resetViewGone(this.mClock);
        resetViewGone(this.mSysIconArea);
        if (this.isShortcurVisible) {
            resetViewGone(this.mShortcut);
        }
        resetViewVisible(this.mUnimportantHeader);
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        layoutParams.height = (int) getUnimportantHeight();
        setLayoutParams(layoutParams);
    }

    private void resetViewVisible(View view) {
        if (view != null) {
            view.setVisibility(0);
            view.setAlpha(1.0f);
            view.setTransitionAlpha(1.0f);
        }
    }

    private void resetViewGone(View view) {
        if (view != null) {
            view.setVisibility(8);
            view.setAlpha(0.0f);
        }
    }

    /* access modifiers changed from: protected */
    public void resetTranslationY() {
        this.mDateView.setTranslationY(0.0f);
        this.mClock.setTranslationY(0.0f);
        this.mSysIconArea.setTranslationY(0.0f);
        this.mShortcut.setTranslationY(0.0f);
        this.mUnimportantHeader.setTranslationY(0.0f);
    }

    private void cancelAllFolme() {
        IStateStyle iStateStyle = this.mTotalHeightStyle;
        if (iStateStyle != null) {
            iStateStyle.cancel();
        }
        cancelFolmeCore(this.mSysIconAreaStyle);
        cancelFolmeCore(this.mShortcutStyle);
        cancelFolmeCore(this.mDateStyle);
        cancelFolmeCore(this.mClockStyle);
        cancelFolmeCore(this.mUnimportantFolme);
    }

    private void cancelFolmeCore(IFolme iFolme) {
        if (iFolme != null) {
            iFolme.state().cancel();
            iFolme.visible().cancel();
        }
    }

    @Override // com.android.systemui.statusbar.notification.unimportant.FoldListener
    public void resetAll(boolean z) {
        if (z) {
            showNotificationsAnim();
        } else {
            showNotificationWithoutAnim();
        }
    }
}
