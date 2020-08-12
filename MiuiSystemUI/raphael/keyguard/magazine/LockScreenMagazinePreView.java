package com.android.keyguard.magazine;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.TextView;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.MiuiKeyguardUtils;
import com.android.keyguard.analytics.AnalyticsHelper;
import com.android.keyguard.magazine.mode.LockScreenMagazineWallpaperInfo;
import com.android.keyguard.utils.PackageUtils;
import com.android.keyguard.wallpaper.WallpaperAuthorityUtils;
import com.android.systemui.SystemUICompat;
import com.android.systemui.plugins.R;
import com.xiaomi.stat.d;
import java.util.Locale;
import miui.os.Build;
import miui.view.MiuiHapticFeedbackConstants;

public class LockScreenMagazinePreView extends RelativeLayout {
    private boolean mDarkMode;
    private int mDensityDpi;
    private float mFontScale;
    private LinearLayout mFullScreenBottomLayout;
    private TextView mFullScreenButton;
    private TextView mFullScreenContent;
    private LinearLayout mFullScreenLayout;
    private ImageView mFullScreenLinkButton;
    private TextView mFullScreenProvider;
    private RemoteViews mFullScreenRemoteView;
    private boolean mFullScreenRemoteViewApplyed;
    private ImageView mFullScreenSettingButton;
    private TextView mFullScreenSource;
    private TextView mFullScreenTitle;
    /* access modifiers changed from: private */
    public RelativeLayout mFullScreenTitleLayout;
    /* access modifiers changed from: private */
    public float mFullScreenTitleLayoutWidth;
    private Object mLocaleList;
    protected LockScreenMagazineWallpaperInfo mLockScreenMagazineWallpaperInfo;
    private LinearLayout mMainLayout;
    private TextView mMainProvider;
    private RemoteViews mMainRemoteView;
    private boolean mMainRemoteViewApplyed;
    private TextView mMainSource;
    private TextView mMainTitle;
    protected KeyguardUpdateMonitor mMonitor;
    /* access modifiers changed from: private */
    public OnPreViewClickListener mPreViewClickListener;
    private LinearLayout mRemoteFullScreenLayout;
    private View mRemoteFullScreenView;
    private LinearLayout mRemoteMainLayout;
    private View mRemoteMainView;
    private String mRemotePackageName;
    private Resources mResources;

    public interface OnPreViewClickListener {
        void onLinkButtonClick(View view);

        void onPreButtonClick(View view);

        void onSettingButtonClick(View view, Intent intent);
    }

    public boolean hasOverlappingRendering() {
        return false;
    }

    public LockScreenMagazinePreView(Context context) {
        super(context);
    }

    public LockScreenMagazinePreView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mMonitor = KeyguardUpdateMonitor.getInstance(context);
        this.mLockScreenMagazineWallpaperInfo = this.mMonitor.getLockScreenMagazineWallpaperInfo();
        this.mResources = context.getResources();
    }

    public void onFinishInflate() {
        this.mFullScreenLayout = (LinearLayout) findViewById(R.id.lock_screen_magazine_preview_fullscreen_layout);
        this.mFullScreenSettingButton = (ImageView) findViewById(R.id.lock_screen_magazine_preview_fullscreen_setting);
        this.mFullScreenTitleLayout = (RelativeLayout) findViewById(R.id.lock_screen_magazine_preview_fullscreen_bottom_title_layout);
        this.mFullScreenTitle = (TextView) findViewById(R.id.lock_screen_magazine_preview_fullscreen_bottom_title_layout_title);
        this.mFullScreenLinkButton = (ImageView) findViewById(R.id.lock_screen_magazine_preview_fullscreen_bottom_title_layout_link);
        this.mFullScreenBottomLayout = (LinearLayout) findViewById(R.id.lock_screen_magazine_preview_fullscreen_bottom_layout);
        this.mFullScreenContent = (TextView) findViewById(R.id.lock_screen_magazine_preview_fullscreen_bottom_content);
        this.mFullScreenButton = (TextView) findViewById(R.id.lock_screen_magazine_preview_fullscreen_bottom_button);
        this.mFullScreenProvider = (TextView) findViewById(R.id.lock_screen_magazine_preview_fullscreen_bottom_provider);
        this.mFullScreenSource = (TextView) findViewById(R.id.lock_screen_magazine_preview_fullscreen_bottom_source);
        this.mMainLayout = (LinearLayout) findViewById(R.id.lock_screen_magazine_preview_main_layout);
        this.mMainTitle = (TextView) findViewById(R.id.lock_screen_magazine_preview_main_title);
        this.mMainProvider = (TextView) findViewById(R.id.lock_screen_magazine_preview_main_provider);
        this.mMainSource = (TextView) findViewById(R.id.lock_screen_magazine_preview_main_source);
        this.mRemoteMainLayout = (LinearLayout) findViewById(R.id.lock_screen_magazine_preview_remote_main_layout);
        this.mRemoteFullScreenLayout = (LinearLayout) findViewById(R.id.lock_screen_magazine_preview_remote_fullscreen_layout);
        initViews();
        updateFontScale();
    }

    private void initViews() {
        initLayoutVisibility();
        initSettingButton();
        initFullScreenTitleLayout();
        initFullScreenTitle();
        initLinkButton();
        initFullScreenBottomLayout();
        initFullScreenContent();
        initFullScreenButton();
    }

    public void initLayoutVisibility() {
        this.mMainLayout.setVisibility(8);
        this.mRemoteMainLayout.setVisibility(8);
        this.mFullScreenLayout.setVisibility(8);
        this.mRemoteFullScreenLayout.setVisibility(8);
        if (!Build.IS_INTERNATIONAL_BUILD || !this.mMonitor.isSupportLockScreenMagazineLeft() || MiuiKeyguardUtils.isGxzwSensor()) {
            getMainLayout().setVisibility(8);
        } else {
            getMainLayout().setVisibility(0);
        }
    }

    public void initSettingButton() {
        if (MiuiKeyguardUtils.isDeviceProvisionedInSettingsDb(this.mContext)) {
            new AsyncTask<Void, Void, Intent>() {
                /* access modifiers changed from: protected */
                public Intent doInBackground(Void... voidArr) {
                    Intent intent;
                    String lockScreenMagazineSettingsDeepLink = LockScreenMagazineUtils.getLockScreenMagazineSettingsDeepLink(LockScreenMagazinePreView.this.mContext);
                    if (!TextUtils.isEmpty(lockScreenMagazineSettingsDeepLink)) {
                        intent = new Intent("android.intent.action.VIEW");
                        intent.putExtra("from", "lks_preview");
                        intent.setData(Uri.parse(lockScreenMagazineSettingsDeepLink));
                        intent.addFlags(MiuiHapticFeedbackConstants.FLAG_MIUI_HAPTIC_TAP_NORMAL);
                        intent.addFlags(67108864);
                    } else {
                        intent = null;
                    }
                    if (PackageUtils.resolveIntent(LockScreenMagazinePreView.this.mContext, intent) != null) {
                        return intent;
                    }
                    return null;
                }

                /* access modifiers changed from: protected */
                public void onPostExecute(Intent intent) {
                    LockScreenMagazinePreView.this.updateSettingButton(intent);
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
        }
    }

    private void initFullScreenTitleLayout() {
        this.mFullScreenTitleLayout.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            public void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
                if (i3 - i != i7 - i5) {
                    LockScreenMagazinePreView lockScreenMagazinePreView = LockScreenMagazinePreView.this;
                    float unused = lockScreenMagazinePreView.mFullScreenTitleLayoutWidth = (float) lockScreenMagazinePreView.mFullScreenTitleLayout.getWidth();
                    LockScreenMagazinePreView.this.updateLinkButton();
                }
            }
        });
        setFullScreenTitleLayoutMargin();
    }

    private void initFullScreenTitle() {
        if (Build.IS_INTERNATIONAL_BUILD) {
            this.mFullScreenTitle.setFilters(new InputFilter[]{new InputFilter.LengthFilter(40)});
            this.mFullScreenTitle.setTypeface(Typeface.create("sans-serif-medium", 0));
            this.mFullScreenTitle.setLetterSpacing(0.01f);
            return;
        }
        this.mFullScreenTitle.setTypeface(Typeface.create("miui-bold", 0));
        this.mFullScreenTitle.setLetterSpacing(0.1f);
    }

    private void initLinkButton() {
        this.mFullScreenLinkButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (LockScreenMagazinePreView.this.mPreViewClickListener != null) {
                    LockScreenMagazinePreView.this.mPreViewClickListener.onLinkButtonClick(view);
                }
            }
        });
        this.mFullScreenLinkButton.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View view) {
                return true;
            }
        });
    }

    private void initFullScreenBottomLayout() {
        setFullScreenBottomLayoutPadding();
    }

    private void initFullScreenContent() {
        if (Build.IS_INTERNATIONAL_BUILD) {
            this.mFullScreenContent.setFilters(new InputFilter[]{new InputFilter.LengthFilter(150)});
            this.mFullScreenContent.setLetterSpacing(0.01f);
            this.mFullScreenContent.setTextColor(getResources().getColor(R.color.lock_screen_magazine_preview_fullscreen_global_bottom_content_text_color));
            this.mFullScreenContent.setLineSpacing((float) getResources().getDimensionPixelOffset(R.dimen.lock_screen_magazine_preview_fullscreen_bottom_global_content_line_spacing_extra), 1.0f);
        } else {
            this.mFullScreenContent.setTypeface(Typeface.create("miui-light", 0));
            this.mFullScreenContent.setLineSpacing((float) getResources().getDimensionPixelOffset(R.dimen.lock_screen_magazine_preview_fullscreen_bottom_content_line_spacing_extra), 1.0f);
        }
        setFullScreenContentMargin();
    }

    private void initFullScreenButton() {
        this.mFullScreenButton.setContentDescription(this.mResources.getText(R.string.accessibility_enter_lock_wallpaper));
        this.mFullScreenButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (LockScreenMagazinePreView.this.mPreViewClickListener != null) {
                    LockScreenMagazinePreView.this.mPreViewClickListener.onPreButtonClick(view);
                }
            }
        });
        this.mFullScreenButton.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View view) {
                return true;
            }
        });
        setFullScreenButtonPadding();
    }

    private void setFullScreenButtonPadding() {
        int i;
        int dimensionPixelOffset = this.mResources.getDimensionPixelOffset(R.dimen.lock_screen_magazine_preview_fullscreen_bottom_button_padding_top);
        if (Build.IS_INTERNATIONAL_BUILD) {
            i = this.mResources.getDimensionPixelOffset(R.dimen.lock_screen_magazine_preview_fullscreen_bottom_global_button_padding_start_end);
        } else {
            i = this.mResources.getDimensionPixelOffset(R.dimen.lock_screen_magazine_preview_fullscreen_bottom_button_padding_start_end);
        }
        this.mFullScreenButton.setPadding(i, dimensionPixelOffset, i, Build.IS_INTERNATIONAL_BUILD ? dimensionPixelOffset : this.mResources.getDimensionPixelOffset(R.dimen.lock_screen_magazine_preview_fullscreen_bottom_button_padding_bottom));
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        Object locales = SystemUICompat.getLocales(configuration);
        float f = configuration.fontScale;
        int i = configuration.densityDpi;
        if (this.mLocaleList != locales) {
            updateLanguage();
            this.mLocaleList = locales;
        }
        if (this.mFontScale != f) {
            updateFontScale();
            updateLinkButton();
            this.mFontScale = f;
        }
        if (this.mDensityDpi != i) {
            updateFontScale();
            updateViewsLayoutParams();
            updateDrawableResource();
            this.mDensityDpi = i;
        }
    }

    private void setFullScreenTitleLayoutMargin() {
        int i;
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) this.mFullScreenTitleLayout.getLayoutParams();
        if (Build.IS_INTERNATIONAL_BUILD) {
            i = this.mResources.getDimensionPixelOffset(R.dimen.lock_screen_magazine_preview_fullscreen_bottom_global_title_layout_margin_bottom);
        } else {
            i = this.mResources.getDimensionPixelOffset(R.dimen.lock_screen_magazine_preview_fullscreen_bottom_title_layout_margin_bottom);
        }
        layoutParams.setMargins(0, 0, 0, i);
        this.mFullScreenTitleLayout.setLayoutParams(layoutParams);
    }

    private void setFullScreenContentMargin() {
        int i;
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) this.mFullScreenContent.getLayoutParams();
        if (Build.IS_INTERNATIONAL_BUILD) {
            i = this.mResources.getDimensionPixelOffset(R.dimen.lock_screen_magazine_preview_fullscreen_bottom_global_content_margin_bottom);
        } else {
            i = this.mResources.getDimensionPixelOffset(R.dimen.lock_screen_magazine_preview_fullscreen_bottom_content_margin_bottom);
        }
        layoutParams.setMargins(0, 0, 0, i);
        this.mFullScreenContent.setLayoutParams(layoutParams);
    }

    private void setFullScreenBottomLayoutPadding() {
        int i;
        LinearLayout linearLayout = this.mFullScreenBottomLayout;
        int dimensionPixelOffset = this.mResources.getDimensionPixelOffset(R.dimen.lock_screen_magazine_preview_fullscreen_bottom_padding_start);
        int dimensionPixelOffset2 = this.mResources.getDimensionPixelOffset(R.dimen.lock_screen_magazine_preview_fullscreen_bottom_padding_top);
        if (Build.IS_INTERNATIONAL_BUILD) {
            i = this.mResources.getDimensionPixelOffset(R.dimen.lock_screen_magazine_preview_fullscreen_bottom_global_padding_end);
        } else {
            i = this.mResources.getDimensionPixelOffset(R.dimen.lock_screen_magazine_preview_fullscreen_bottom_padding_end);
        }
        linearLayout.setPaddingRelative(dimensionPixelOffset, dimensionPixelOffset2, i, this.mResources.getDimensionPixelOffset(R.dimen.lock_screen_magazine_preview_fullscreen_bottom_padding_bottom));
    }

    /* access modifiers changed from: private */
    public void updateSettingButton(final Intent intent) {
        if (intent != null) {
            this.mFullScreenSettingButton.setVisibility(0);
            this.mFullScreenSettingButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    if (LockScreenMagazinePreView.this.mPreViewClickListener != null) {
                        LockScreenMagazinePreView.this.mPreViewClickListener.onSettingButtonClick(view, intent);
                    }
                }
            });
            this.mFullScreenSettingButton.setOnLongClickListener(new View.OnLongClickListener() {
                public boolean onLongClick(View view) {
                    return true;
                }
            });
            MiuiKeyguardUtils.setViewTouchDelegate(this.mFullScreenSettingButton, 50);
            return;
        }
        this.mFullScreenSettingButton.setVisibility(8);
        MiuiKeyguardUtils.setViewTouchDelegate(this.mFullScreenSettingButton, 0);
    }

    private void updateLanguage() {
        updateViews();
    }

    private void updateFontScale() {
        if (!isDefaultMainLayout()) {
            updateRemoteTextView(this.mRemoteMainView, true);
        } else if (Build.IS_INTERNATIONAL_BUILD && this.mMonitor.isSupportLockScreenMagazineLeft()) {
            this.mMainTitle.setTextSize(0, (float) this.mResources.getDimensionPixelSize(R.dimen.lock_screen_magazine_preview_main_title_text_size));
            this.mMainProvider.setTextSize(0, (float) this.mResources.getDimensionPixelSize(R.dimen.lock_screen_magazine_preview_main_content_text_size));
            this.mMainSource.setTextSize(0, (float) this.mResources.getDimensionPixelSize(R.dimen.lock_screen_magazine_preview_main_content_text_size));
        }
        if (isDefaultFullScreenLayout()) {
            if (Build.IS_INTERNATIONAL_BUILD && this.mMonitor.isSupportLockScreenMagazineLeft()) {
                this.mFullScreenProvider.setTextSize(0, (float) this.mResources.getDimensionPixelSize(R.dimen.lock_screen_magazine_preview_fullscreen_bottom_provider_text_size));
                this.mFullScreenSource.setTextSize(0, (float) this.mResources.getDimensionPixelSize(R.dimen.lock_screen_magazine_preview_fullscreen_bottom_source_text_size));
            }
            this.mFullScreenTitle.setTextSize(0, (float) this.mResources.getDimensionPixelSize(Build.IS_INTERNATIONAL_BUILD ? R.dimen.lock_screen_magazine_preview_fullscreen_bottom_title_layout_global_title_text_size : R.dimen.lock_screen_magazine_preview_fullscreen_bottom_title_layout_title_text_size));
            this.mFullScreenContent.setTextSize(0, (float) this.mResources.getDimensionPixelSize(Build.IS_INTERNATIONAL_BUILD ? R.dimen.lock_screen_magazine_preview_fullscreen_bottom_global_content_text_size : R.dimen.lock_screen_magazine_preview_fullscreen_bottom_content_text_size));
            this.mFullScreenButton.setTextSize(0, (float) this.mResources.getDimensionPixelSize(R.dimen.lock_screen_magazine_preview_fullscreen_bottom_button_text_size));
            return;
        }
        updateRemoteTextView(this.mRemoteFullScreenView, false);
    }

    private void updateDrawableResource() {
        if (isDefaultFullScreenLayout()) {
            if (!Build.IS_INTERNATIONAL_BUILD && this.mMonitor.isSupportLockScreenMagazineLeft()) {
                updateSettingButtonDrawableResource();
                updateLinkButtonDrawableResource();
            }
            this.mFullScreenButton.setBackgroundResource(R.drawable.lock_screen_magazine_des_more_btn_bg);
            return;
        }
        updateRemoteTextView(this.mRemoteFullScreenLayout, false);
    }

    private void updateSettingButtonDrawableResource() {
        this.mFullScreenSettingButton.setImageResource(this.mDarkMode ? R.drawable.lock_screen_magazine_pre_settings_dark : R.drawable.lock_screen_magazine_pre_settings);
    }

    private void updateLinkButtonDrawableResource() {
        LockScreenMagazineWallpaperInfo lockScreenMagazineWallpaperInfo = this.mLockScreenMagazineWallpaperInfo;
        if (lockScreenMagazineWallpaperInfo != null) {
            int i = lockScreenMagazineWallpaperInfo.linkType;
            if (i == 1) {
                this.mFullScreenLinkButton.setImageResource(R.drawable.lock_screen_magazine_pre_link);
            } else if (i == 2) {
                this.mFullScreenLinkButton.setImageResource(R.drawable.wallpaper_play);
            }
        }
    }

    private void updateViewsLayoutParams() {
        if (Build.IS_INTERNATIONAL_BUILD && this.mMonitor.isSupportLockScreenMagazineLeft()) {
            if (isDefaultMainLayout()) {
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) this.mMainLayout.getLayoutParams();
                layoutParams.setMargins(0, 0, 0, this.mResources.getDimensionPixelOffset(R.dimen.keyguard_affordance_height));
                this.mMainLayout.setLayoutParams(layoutParams);
                LinearLayout.LayoutParams layoutParams2 = (LinearLayout.LayoutParams) this.mMainTitle.getLayoutParams();
                layoutParams2.setMargins(this.mResources.getDimensionPixelOffset(R.dimen.lock_screen_magazine_preview_main_title_margin_start_end), 0, this.mResources.getDimensionPixelOffset(R.dimen.lock_screen_magazine_preview_main_title_margin_start_end), this.mResources.getDimensionPixelOffset(R.dimen.lock_screen_magazine_preview_main_title_margin_bottom));
                this.mMainTitle.setLayoutParams(layoutParams2);
                LinearLayout.LayoutParams layoutParams3 = (LinearLayout.LayoutParams) this.mMainProvider.getLayoutParams();
                layoutParams3.setMargins(this.mResources.getDimensionPixelOffset(R.dimen.lock_screen_magazine_preview_main_provider_margin_start), 0, 0, 0);
                this.mMainProvider.setLayoutParams(layoutParams3);
            } else {
                updateRemoteTextView(this.mRemoteMainLayout, true);
            }
        }
        if (isDefaultFullScreenLayout()) {
            if (this.mMonitor.isSupportLockScreenMagazineLeft()) {
                LinearLayout.LayoutParams layoutParams4 = (LinearLayout.LayoutParams) this.mFullScreenSettingButton.getLayoutParams();
                layoutParams4.setMargins(0, this.mResources.getDimensionPixelOffset(R.dimen.lock_screen_magazine_preview_fullscreen_setting_margin_top), this.mResources.getDimensionPixelOffset(R.dimen.lock_screen_magazine_preview_fullscreen_setting_margin_end), 0);
                this.mFullScreenSettingButton.setLayoutParams(layoutParams4);
                RelativeLayout.LayoutParams layoutParams5 = (RelativeLayout.LayoutParams) this.mFullScreenLinkButton.getLayoutParams();
                layoutParams5.width = this.mResources.getDimensionPixelOffset(R.dimen.lock_screen_magazine_preview_fullscreen_bottom_title_layout_link_width_height);
                layoutParams5.height = this.mResources.getDimensionPixelOffset(R.dimen.lock_screen_magazine_preview_fullscreen_bottom_title_layout_link_width_height);
                this.mFullScreenLinkButton.setLayoutParams(layoutParams5);
            }
            setFullScreenBottomLayoutPadding();
            setFullScreenTitleLayoutMargin();
            setFullScreenContentMargin();
            LinearLayout.LayoutParams layoutParams6 = (LinearLayout.LayoutParams) this.mFullScreenButton.getLayoutParams();
            layoutParams6.setMargins(this.mResources.getDimensionPixelOffset(R.dimen.lock_screen_magazine_preview_fullscreen_bottom_button_margin_start), 0, 0, 0);
            this.mFullScreenButton.setLayoutParams(layoutParams6);
            setFullScreenButtonPadding();
            return;
        }
        updateRemoteTextView(this.mRemoteFullScreenLayout, false);
    }

    public void refreshWallpaperInfo(RemoteViews remoteViews, RemoteViews remoteViews2) {
        this.mLockScreenMagazineWallpaperInfo = this.mMonitor.getLockScreenMagazineWallpaperInfo();
        LockScreenMagazineWallpaperInfo lockScreenMagazineWallpaperInfo = this.mLockScreenMagazineWallpaperInfo;
        if (lockScreenMagazineWallpaperInfo != null) {
            if (!Build.IS_INTERNATIONAL_BUILD && !TextUtils.isEmpty(lockScreenMagazineWallpaperInfo.content)) {
                LockScreenMagazineWallpaperInfo lockScreenMagazineWallpaperInfo2 = this.mLockScreenMagazineWallpaperInfo;
                lockScreenMagazineWallpaperInfo2.content = lockScreenMagazineWallpaperInfo2.content.replaceAll("\\s*", "");
            }
            updateViews(remoteViews, remoteViews2);
        }
    }

    public void updateViews(RemoteViews remoteViews, RemoteViews remoteViews2) {
        checkResetRemoteView(remoteViews, remoteViews2);
        updateViews();
    }

    public void updateViews() {
        RemoteViews remoteViews = this.mMainRemoteView;
        if (remoteViews == null) {
            this.mRemotePackageName = null;
            updateMainView();
        } else {
            this.mRemotePackageName = remoteViews.getPackage();
            try {
                updateRemoteMainView();
            } catch (Exception e) {
                Log.e("LockScreenMagazinePreView", "updateRemoteMainView " + e.getMessage());
                updateMainView();
            }
        }
        RemoteViews remoteViews2 = this.mFullScreenRemoteView;
        if (remoteViews2 == null) {
            this.mRemotePackageName = null;
            updateFullScreenView();
        } else {
            this.mRemotePackageName = remoteViews2.getPackage();
            try {
                updateRemoteFullScreenView();
            } catch (Exception e2) {
                Log.e("LockScreenMagazinePreView", "updateRemoteFullScreenView " + e2.getMessage());
                updateFullScreenView();
            }
        }
        initLayoutVisibility();
    }

    private void updateMainView() {
        if (Build.IS_INTERNATIONAL_BUILD && this.mMonitor.isSupportLockScreenMagazineLeft()) {
            updateMainTitle();
            updateMainProviderText();
            updateMainSourceText();
        }
    }

    private void updateFullScreenView() {
        if (Build.IS_INTERNATIONAL_BUILD && this.mMonitor.isSupportLockScreenMagazineLeft()) {
            updateFullScreenProviderText();
            updateFullScreenSourceText();
        }
        updateFullScreenTitle();
        updateFullScreenContentText();
        updateFullScreenButtonText();
    }

    private void updateRemoteMainView() {
        if (!this.mMainRemoteViewApplyed) {
            this.mMainRemoteViewApplyed = true;
            applyMainRemoteView();
            return;
        }
        try {
            this.mMainRemoteView.reapply(getContext(), this.mRemoteMainView);
        } catch (Exception e) {
            Log.e("LockScreenMagazinePreView", "reapply RemoteMainView " + e.getMessage());
            applyMainRemoteView();
        }
    }

    private void applyMainRemoteView() {
        this.mRemoteMainLayout.removeView(this.mRemoteMainView);
        this.mRemoteMainView = this.mMainRemoteView.apply(getContext(), this.mRemoteMainLayout);
        updateRemoteTextView(this.mRemoteMainView, true);
        this.mRemoteMainLayout.addView(this.mRemoteMainView);
    }

    private void updateRemoteFullScreenView() {
        if (!this.mFullScreenRemoteViewApplyed) {
            this.mFullScreenRemoteViewApplyed = true;
            applyFullScreenRemoteView();
            return;
        }
        try {
            this.mFullScreenRemoteView.reapply(getContext(), this.mRemoteFullScreenView);
        } catch (Exception e) {
            Log.e("LockScreenMagazinePreView", "reapply RemoteFullScreenView " + e.getMessage());
            applyFullScreenRemoteView();
        }
    }

    private void applyFullScreenRemoteView() {
        this.mRemoteFullScreenLayout.removeView(this.mRemoteFullScreenView);
        this.mRemoteFullScreenView = this.mFullScreenRemoteView.apply(getContext(), this.mRemoteFullScreenLayout);
        updateRemoteTextView(this.mRemoteFullScreenView, false);
        this.mRemoteFullScreenLayout.addView(this.mRemoteFullScreenView);
    }

    private void updateRemoteTextView(View view, boolean z) {
        if (view != null) {
            String str = this.mRemotePackageName;
            if (!TextUtils.isEmpty(str)) {
                try {
                    Resources resourcesForApplication = getContext().getPackageManager().getResourcesForApplication(str);
                    int identifier = resourcesForApplication.getIdentifier("wallpaper_title", d.h, str);
                    int identifier2 = resourcesForApplication.getIdentifier("wallpaper_content", d.h, str);
                    int identifier3 = resourcesForApplication.getIdentifier("more_info", d.h, str);
                    View findViewById = view.findViewById(identifier);
                    View findViewById2 = view.findViewById(identifier2);
                    View findViewById3 = view.findViewById(identifier3);
                    int color = this.mDarkMode ? getContext().getResources().getColor(R.color.miui_common_unlock_screen_common_time_dark_text_color) : -1;
                    if (findViewById instanceof TextView) {
                        TextView textView = (TextView) findViewById;
                        textView.setTextSize(0, (float) this.mResources.getDimensionPixelSize(Build.IS_INTERNATIONAL_BUILD ? R.dimen.lock_screen_magazine_preview_fullscreen_bottom_title_layout_global_title_text_size : R.dimen.lock_screen_magazine_preview_fullscreen_bottom_title_layout_title_text_size));
                        textView.setTypeface(Typeface.create("sans-serif-medium", 0));
                        if (z) {
                            textView.setTextColor(color);
                        }
                    }
                    if (findViewById2 instanceof TextView) {
                        TextView textView2 = (TextView) findViewById2;
                        textView2.setTextSize(0, (float) this.mResources.getDimensionPixelSize(Build.IS_INTERNATIONAL_BUILD ? R.dimen.lock_screen_magazine_preview_fullscreen_bottom_global_content_text_size : R.dimen.lock_screen_magazine_preview_fullscreen_bottom_content_text_size));
                        textView2.setTypeface(Typeface.create("miui-light", 0));
                        if (z) {
                            textView2.setTextColor(color);
                        }
                    }
                    if (findViewById3 instanceof TextView) {
                        TextView textView3 = (TextView) findViewById3;
                        textView3.setTextSize(0, (float) this.mResources.getDimensionPixelSize(R.dimen.lock_screen_magazine_preview_fullscreen_bottom_button_text_size));
                        if (z) {
                            textView3.setTextColor(color);
                            textView3.setBackground(this.mResources.getDrawable(this.mDarkMode ? R.drawable.lock_screen_magazine_des_global_more_btn_bg_dark : R.drawable.lock_screen_magazine_des_global_more_btn_bg));
                        }
                    }
                } catch (Exception e) {
                    Log.e("LockScreenMagazinePreView", "updateRemoteTextView " + e.getMessage());
                }
            }
        }
    }

    private void checkResetRemoteView(RemoteViews remoteViews, RemoteViews remoteViews2) {
        if (remoteViews == null || this.mMainRemoteView == null || remoteViews.getLayoutId() != this.mMainRemoteView.getLayoutId()) {
            this.mRemoteMainLayout.removeView(this.mRemoteMainView);
            this.mRemoteMainView = null;
            this.mMainRemoteViewApplyed = false;
        }
        if (remoteViews2 == null || this.mFullScreenRemoteView == null || remoteViews2.getLayoutId() != this.mFullScreenRemoteView.getLayoutId()) {
            this.mRemoteFullScreenLayout.removeView(this.mRemoteFullScreenView);
            this.mRemoteFullScreenView = null;
            this.mFullScreenRemoteViewApplyed = false;
        }
        this.mMainRemoteView = remoteViews;
        this.mFullScreenRemoteView = remoteViews2;
    }

    private void updateFullScreenButtonText() {
        if (this.mLockScreenMagazineWallpaperInfo != null) {
            String string = this.mContext.getResources().getString(Build.IS_INTERNATIONAL_BUILD ? R.string.lock_screen_magazine_preview_fullscreen_bottom_button_global_text : R.string.lock_screen_magazine_preview_fullscreen_bottom_button_text);
            if (this.mMonitor.isLockScreenMagazinePkgExist()) {
                if (!WallpaperAuthorityUtils.isLockScreenMagazineOpenedWallpaper(this.mContext) && !TextUtils.isEmpty(this.mLockScreenMagazineWallpaperInfo.btnText)) {
                    string = this.mLockScreenMagazineWallpaperInfo.btnText;
                }
                if (Build.IS_INTERNATIONAL_BUILD && !TextUtils.isEmpty(this.mLockScreenMagazineWallpaperInfo.globalBtnText)) {
                    string = this.mLockScreenMagazineWallpaperInfo.globalBtnText;
                }
            } else {
                string = this.mContext.getResources().getString(R.string.download_lock_wallpaper);
            }
            this.mFullScreenButton.setText(string);
        }
    }

    private void updateFullScreenTitle() {
        String fullScreenTitleText = getFullScreenTitleText();
        if (!TextUtils.isEmpty(fullScreenTitleText)) {
            this.mFullScreenTitle.setVisibility(0);
            this.mFullScreenTitle.setText(fullScreenTitleText);
            updateLinkButton();
            return;
        }
        this.mFullScreenTitle.setVisibility(8);
    }

    /* access modifiers changed from: private */
    public void updateLinkButton() {
        if (isDefaultFullScreenLayout()) {
            LockScreenMagazineWallpaperInfo lockScreenMagazineWallpaperInfo = this.mLockScreenMagazineWallpaperInfo;
            if (lockScreenMagazineWallpaperInfo == null || TextUtils.isEmpty(lockScreenMagazineWallpaperInfo.landingPageUrl)) {
                updateLinkButtonLayoutParams((int) this.mFullScreenTitleLayoutWidth, 0, 0);
                this.mFullScreenLinkButton.setVisibility(8);
                return;
            }
            float dimensionPixelOffset = (float) this.mResources.getDimensionPixelOffset(R.dimen.lock_screen_magazine_preview_fullscreen_bottom_title_layout_link_width_height);
            float dimensionPixelOffset2 = (float) this.mResources.getDimensionPixelOffset(R.dimen.lock_screen_magazine_preview_fullscreen_bottom_link_margin_start);
            float f = dimensionPixelOffset + dimensionPixelOffset2;
            int titleTextWidth = getTitleTextWidth() + 20;
            float f2 = (float) titleTextWidth;
            float f3 = this.mFullScreenTitleLayoutWidth;
            if (f2 > f3 - f) {
                updateLinkButtonLayoutParams((int) (f3 - f), (int) f, (int) (f3 - dimensionPixelOffset));
            } else {
                updateLinkButtonLayoutParams(titleTextWidth, 0, (int) (f2 + dimensionPixelOffset2));
            }
            this.mFullScreenLinkButton.setVisibility(0);
            updateLinkButtonDrawableResource();
        }
    }

    private int getTitleTextWidth() {
        String charSequence = this.mFullScreenTitle.getText().toString();
        Rect rect = new Rect();
        this.mFullScreenTitle.getPaint().getTextBounds(charSequence, 0, charSequence.length(), rect);
        return rect.width();
    }

    private void updateLinkButtonLayoutParams(int i, int i2, int i3) {
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) this.mFullScreenTitle.getLayoutParams();
        layoutParams.width = i;
        layoutParams.rightMargin = i2;
        this.mFullScreenTitle.setLayoutParams(layoutParams);
        RelativeLayout.LayoutParams layoutParams2 = (RelativeLayout.LayoutParams) this.mFullScreenLinkButton.getLayoutParams();
        layoutParams2.leftMargin = i3;
        this.mFullScreenLinkButton.setLayoutParams(layoutParams2);
    }

    private void updateMainTitle() {
        String mainTitleText = getMainTitleText();
        if (!TextUtils.isEmpty(mainTitleText)) {
            this.mMainTitle.setVisibility(0);
            this.mMainTitle.setText(mainTitleText);
            return;
        }
        this.mMainTitle.setVisibility(8);
    }

    private String getFullScreenTitleText() {
        if (!WallpaperAuthorityUtils.isLockScreenMagazineWallpaper(this.mContext)) {
            return null;
        }
        String string = getResources().getString(R.string.lock_screen_magazine_default_title);
        LockScreenMagazineWallpaperInfo lockScreenMagazineWallpaperInfo = this.mLockScreenMagazineWallpaperInfo;
        if (lockScreenMagazineWallpaperInfo == null) {
            return string;
        }
        if (lockScreenMagazineWallpaperInfo.isTitleCustomized) {
            if (!TextUtils.isEmpty(lockScreenMagazineWallpaperInfo.title)) {
                return this.mLockScreenMagazineWallpaperInfo.title;
            }
            return string;
        } else if (!TextUtils.isEmpty(lockScreenMagazineWallpaperInfo.title)) {
            return (Build.IS_INTERNATIONAL_BUILD || Locale.CHINESE.getLanguage().equals(Locale.getDefault().getLanguage())) ? this.mLockScreenMagazineWallpaperInfo.title : string;
        } else {
            return string;
        }
    }

    private String getMainTitleText() {
        LockScreenMagazineWallpaperInfo lockScreenMagazineWallpaperInfo;
        if (!WallpaperAuthorityUtils.isLockScreenMagazineOpenedWallpaper(this.mContext) || (lockScreenMagazineWallpaperInfo = this.mLockScreenMagazineWallpaperInfo) == null || TextUtils.isEmpty(lockScreenMagazineWallpaperInfo.title)) {
            return null;
        }
        return this.mLockScreenMagazineWallpaperInfo.title;
    }

    private void updateFullScreenContentText() {
        String fullScreenContentText = getFullScreenContentText();
        if (!TextUtils.isEmpty(fullScreenContentText)) {
            this.mFullScreenContent.setVisibility(0);
            this.mFullScreenContent.setText(fullScreenContentText);
            return;
        }
        this.mFullScreenContent.setVisibility(8);
    }

    private void updateMainProviderText() {
        String providerText = getProviderText();
        if (TextUtils.isEmpty(providerText) || TextUtils.isEmpty(getMainTitleText())) {
            this.mMainProvider.setVisibility(8);
            return;
        }
        this.mMainProvider.setVisibility(0);
        this.mMainProvider.setText(providerText);
    }

    private void updateFullScreenProviderText() {
        String providerText = getProviderText();
        if (TextUtils.isEmpty(providerText) || !Build.IS_INTERNATIONAL_BUILD || !this.mMonitor.isSupportLockScreenMagazineLeft()) {
            this.mFullScreenProvider.setVisibility(8);
            return;
        }
        this.mFullScreenProvider.setVisibility(0);
        this.mFullScreenProvider.setText(providerText);
    }

    private void updateFullScreenSourceText() {
        String sourceText = getSourceText();
        if (TextUtils.isEmpty(sourceText) || !Build.IS_INTERNATIONAL_BUILD || !this.mMonitor.isSupportLockScreenMagazineLeft()) {
            this.mFullScreenSource.setVisibility(8);
            return;
        }
        this.mFullScreenSource.setVisibility(0);
        this.mFullScreenSource.setText(sourceText);
        if (!TextUtils.isEmpty(this.mLockScreenMagazineWallpaperInfo.sourceColor)) {
            this.mFullScreenSource.setTextColor(Color.parseColor(this.mLockScreenMagazineWallpaperInfo.sourceColor));
        }
    }

    private void updateMainSourceText() {
        String mainSourceText = getMainSourceText();
        if (TextUtils.isEmpty(mainSourceText) || TextUtils.isEmpty(getMainTitleText())) {
            this.mMainSource.setVisibility(8);
            return;
        }
        this.mMainSource.setVisibility(0);
        this.mMainSource.setText(mainSourceText);
        if (!TextUtils.isEmpty(this.mLockScreenMagazineWallpaperInfo.sourceColor)) {
            this.mMainSource.setTextColor(Color.parseColor(this.mLockScreenMagazineWallpaperInfo.sourceColor));
        }
    }

    private String getFullScreenContentText() {
        if (!WallpaperAuthorityUtils.isLockScreenMagazineWallpaper(this.mContext)) {
            return null;
        }
        String string = getResources().getString(R.string.lock_screen_magazine_default_content);
        LockScreenMagazineWallpaperInfo lockScreenMagazineWallpaperInfo = this.mLockScreenMagazineWallpaperInfo;
        if (lockScreenMagazineWallpaperInfo == null || TextUtils.isEmpty(lockScreenMagazineWallpaperInfo.content)) {
            return string;
        }
        return (Build.IS_INTERNATIONAL_BUILD || Locale.CHINESE.getLanguage().equals(Locale.getDefault().getLanguage())) ? this.mLockScreenMagazineWallpaperInfo.content : string;
    }

    private String getProviderText() {
        LockScreenMagazineWallpaperInfo lockScreenMagazineWallpaperInfo;
        if (WallpaperAuthorityUtils.isLockScreenMagazineOpenedWallpaper(this.mContext) && (lockScreenMagazineWallpaperInfo = this.mLockScreenMagazineWallpaperInfo) != null && !TextUtils.isEmpty(lockScreenMagazineWallpaperInfo.provider)) {
            return this.mLockScreenMagazineWallpaperInfo.provider;
        }
        return null;
    }

    private String getSourceText() {
        LockScreenMagazineWallpaperInfo lockScreenMagazineWallpaperInfo;
        if (WallpaperAuthorityUtils.isLockScreenMagazineOpenedWallpaper(this.mContext) && (lockScreenMagazineWallpaperInfo = this.mLockScreenMagazineWallpaperInfo) != null && !TextUtils.isEmpty(lockScreenMagazineWallpaperInfo.source)) {
            return this.mLockScreenMagazineWallpaperInfo.source;
        }
        return null;
    }

    private String getMainSourceText() {
        LockScreenMagazineWallpaperInfo lockScreenMagazineWallpaperInfo;
        if (WallpaperAuthorityUtils.isLockScreenMagazineOpenedWallpaper(this.mContext) && (lockScreenMagazineWallpaperInfo = this.mLockScreenMagazineWallpaperInfo) != null && !TextUtils.isEmpty(lockScreenMagazineWallpaperInfo.source)) {
            return this.mLockScreenMagazineWallpaperInfo.source;
        }
        return null;
    }

    public void setDarkMode(boolean z) {
        if (this.mDarkMode != z) {
            this.mDarkMode = z;
            updateSettingButtonDrawableResource();
            setMainLayoutDarkMode();
        }
    }

    public void setMainLayoutDarkMode() {
        if (isDefaultMainLayout()) {
            int color = this.mDarkMode ? getContext().getResources().getColor(R.color.miui_common_unlock_screen_common_time_dark_text_color) : -1;
            this.mMainTitle.setTextColor(color);
            this.mMainProvider.setTextColor(color);
            if (TextUtils.isEmpty(this.mLockScreenMagazineWallpaperInfo.sourceColor)) {
                this.mMainSource.setTextColor(color);
                return;
            }
            return;
        }
        updateRemoteTextView(this.mRemoteMainLayout, true);
    }

    public void setMainLayoutVisible(int i) {
        getMainLayout().setVisibility(i);
        AnalyticsHelper.getInstance(this.mContext).setLockScreenMagazineMainPreShow(i == 0);
    }

    public void setMainLayoutAlpha(float f) {
        getMainLayout().setAlpha(f);
    }

    public void setFullScreenLayoutVisible(int i) {
        getFullScreenLayout().setVisibility(i);
    }

    public void setFullScreenLayoutAlpha(float f) {
        getFullScreenLayout().setAlpha(f);
    }

    public View getMainLayout() {
        View view = this.mRemoteMainView;
        if (view == null || view.getParent() == null) {
            return this.mMainLayout;
        }
        return this.mRemoteMainLayout;
    }

    private boolean isDefaultMainLayout() {
        return getMainLayout() == this.mMainLayout;
    }

    private View getFullScreenLayout() {
        View view = this.mRemoteFullScreenView;
        if (view == null || view.getParent() == null) {
            return this.mFullScreenLayout;
        }
        return this.mRemoteFullScreenLayout;
    }

    private boolean isDefaultFullScreenLayout() {
        return getFullScreenLayout() == this.mFullScreenLayout;
    }

    public void setButtonClickListener(OnPreViewClickListener onPreViewClickListener) {
        this.mPreViewClickListener = onPreViewClickListener;
    }
}
