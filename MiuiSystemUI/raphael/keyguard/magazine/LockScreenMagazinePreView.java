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
import com.android.keyguard.analytics.AnalyticsHelper;
import com.android.keyguard.magazine.entity.LockScreenMagazineWallpaperInfo;
import com.android.keyguard.magazine.utils.LockScreenMagazineUtils;
import com.android.keyguard.utils.MiuiKeyguardUtils;
import com.android.keyguard.utils.PackageUtils;
import com.android.keyguard.wallpaper.WallpaperAuthorityUtils;
import com.android.systemui.C0011R$color;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0021R$string;
import com.android.systemui.Dependency;
import com.miui.systemui.util.CommonUtil;
import java.util.Locale;
import miui.os.Build;

public class LockScreenMagazinePreView extends RelativeLayout {
    private boolean mDarkStyle;
    private int mDensityDpi;
    private TextView mEnterButton;
    private float mFontScale;
    private LinearLayout mFullScreenBottomLayout;
    private TextView mFullScreenContent;
    private LinearLayout mFullScreenLayout;
    private ImageView mFullScreenLinkButton;
    private TextView mFullScreenProvider;
    private RemoteViews mFullScreenRemoteView;
    private boolean mFullScreenRemoteViewApplyed;
    private ImageView mFullScreenSettingButton;
    private TextView mFullScreenSource;
    private TextView mFullScreenTitle;
    private RelativeLayout mFullScreenTitleLayout;
    private float mFullScreenTitleLayoutWidth;
    private Object mLocaleList;
    protected LockScreenMagazineWallpaperInfo mLockScreenMagazineWallpaperInfo;
    private LinearLayout mMainLayout;
    private TextView mMainProvider;
    private RemoteViews mMainRemoteView;
    private boolean mMainRemoteViewApplyed;
    private TextView mMainSource;
    private TextView mMainTitle;
    private OnPreViewClickListener mPreViewClickListener;
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
        this.mResources = context.getResources();
    }

    public void onFinishInflate() {
        super.onFinishInflate();
        this.mFullScreenLayout = (LinearLayout) findViewById(C0015R$id.lock_screen_magazine_preview_fullscreen_layout);
        this.mFullScreenSettingButton = (ImageView) findViewById(C0015R$id.lock_screen_magazine_preview_fullscreen_setting);
        this.mFullScreenTitleLayout = (RelativeLayout) findViewById(C0015R$id.lock_screen_magazine_preview_fullscreen_bottom_title_layout);
        this.mFullScreenTitle = (TextView) findViewById(C0015R$id.lock_screen_magazine_preview_fullscreen_bottom_title_layout_title);
        this.mFullScreenLinkButton = (ImageView) findViewById(C0015R$id.lock_screen_magazine_preview_fullscreen_bottom_title_layout_link);
        this.mFullScreenBottomLayout = (LinearLayout) findViewById(C0015R$id.lock_screen_magazine_preview_fullscreen_bottom_layout);
        this.mFullScreenContent = (TextView) findViewById(C0015R$id.lock_screen_magazine_preview_fullscreen_bottom_content);
        this.mEnterButton = (TextView) findViewById(C0015R$id.lock_screen_magazine_preview_fullscreen_bottom_button);
        this.mFullScreenProvider = (TextView) findViewById(C0015R$id.lock_screen_magazine_preview_fullscreen_bottom_provider);
        this.mFullScreenSource = (TextView) findViewById(C0015R$id.lock_screen_magazine_preview_fullscreen_bottom_source);
        this.mMainLayout = (LinearLayout) findViewById(C0015R$id.lock_screen_magazine_preview_main_layout);
        this.mMainTitle = (TextView) findViewById(C0015R$id.lock_screen_magazine_preview_main_title);
        this.mMainProvider = (TextView) findViewById(C0015R$id.lock_screen_magazine_preview_main_provider);
        this.mMainSource = (TextView) findViewById(C0015R$id.lock_screen_magazine_preview_main_source);
        this.mRemoteMainLayout = (LinearLayout) findViewById(C0015R$id.lock_screen_magazine_preview_remote_main_layout);
        this.mRemoteFullScreenLayout = (LinearLayout) findViewById(C0015R$id.lock_screen_magazine_preview_remote_fullscreen_layout);
        initViews();
        updateFontScale();
        initData();
    }

    private void initData() {
        this.mLockScreenMagazineWallpaperInfo = ((LockScreenMagazineController) Dependency.get(LockScreenMagazineController.class)).getLockScreenMagazineWallpaperInfo();
        ((LockScreenMagazineController) Dependency.get(LockScreenMagazineController.class)).setView(this);
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        ((LockScreenMagazineController) Dependency.get(LockScreenMagazineController.class)).onAttachedToWindow();
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ((LockScreenMagazineController) Dependency.get(LockScreenMagazineController.class)).onDetachedFromWindow();
    }

    private void initViews() {
        initLayoutVisibility();
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
        if (!Build.IS_INTERNATIONAL_BUILD || !((LockScreenMagazineController) Dependency.get(LockScreenMagazineController.class)).isSupportLockScreenMagazineLeft() || MiuiKeyguardUtils.isGxzwSensor()) {
            getMainLayout().setVisibility(8);
        } else {
            getMainLayout().setVisibility(0);
        }
    }

    public void initSettingButton() {
        if (MiuiKeyguardUtils.isDeviceProvisionedInSettingsDb(((RelativeLayout) this).mContext)) {
            new AsyncTask<Void, Void, Intent>() {
                /* class com.android.keyguard.magazine.LockScreenMagazinePreView.AnonymousClass1 */

                /* access modifiers changed from: protected */
                public Intent doInBackground(Void... voidArr) {
                    Intent intent;
                    String lockScreenMagazineSettingsDeepLink = LockScreenMagazineUtils.getLockScreenMagazineSettingsDeepLink(((RelativeLayout) LockScreenMagazinePreView.this).mContext);
                    if (!TextUtils.isEmpty(lockScreenMagazineSettingsDeepLink)) {
                        intent = new Intent("android.intent.action.VIEW");
                        intent.putExtra("from", "lks_preview");
                        intent.setData(Uri.parse(lockScreenMagazineSettingsDeepLink));
                        intent.addFlags(268435456);
                        intent.addFlags(67108864);
                    } else {
                        intent = null;
                    }
                    if (PackageUtils.resolveIntent(((RelativeLayout) LockScreenMagazinePreView.this).mContext, intent) != null) {
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
            /* class com.android.keyguard.magazine.LockScreenMagazinePreView.AnonymousClass2 */

            public void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
                if (i3 - i != i7 - i5) {
                    LockScreenMagazinePreView lockScreenMagazinePreView = LockScreenMagazinePreView.this;
                    lockScreenMagazinePreView.mFullScreenTitleLayoutWidth = (float) lockScreenMagazinePreView.mFullScreenTitleLayout.getWidth();
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
            /* class com.android.keyguard.magazine.LockScreenMagazinePreView.AnonymousClass3 */

            public void onClick(View view) {
                if (LockScreenMagazinePreView.this.mPreViewClickListener != null) {
                    LockScreenMagazinePreView.this.mPreViewClickListener.onLinkButtonClick(view);
                }
            }
        });
        this.mFullScreenLinkButton.setOnLongClickListener(new View.OnLongClickListener(this) {
            /* class com.android.keyguard.magazine.LockScreenMagazinePreView.AnonymousClass4 */

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
            this.mFullScreenContent.setTextColor(getResources().getColor(C0011R$color.lock_screen_magazine_preview_fullscreen_global_bottom_content_text_color));
            this.mFullScreenContent.setLineSpacing((float) getResources().getDimensionPixelOffset(C0012R$dimen.lock_screen_magazine_preview_fullscreen_bottom_global_content_line_spacing_extra), 1.0f);
        } else {
            this.mFullScreenContent.setTypeface(Typeface.create("miui-light", 0));
            this.mFullScreenContent.setLineSpacing((float) getResources().getDimensionPixelOffset(C0012R$dimen.lock_screen_magazine_preview_fullscreen_bottom_content_line_spacing_extra), 1.0f);
        }
        setFullScreenContentMargin();
    }

    private void initFullScreenButton() {
        this.mEnterButton.setContentDescription(this.mResources.getText(C0021R$string.accessibility_enter_lock_wallpaper));
        this.mEnterButton.setOnClickListener(new View.OnClickListener() {
            /* class com.android.keyguard.magazine.LockScreenMagazinePreView.AnonymousClass5 */

            public void onClick(View view) {
                if (LockScreenMagazinePreView.this.mPreViewClickListener != null) {
                    LockScreenMagazinePreView.this.mPreViewClickListener.onPreButtonClick(view);
                }
            }
        });
        this.mEnterButton.setOnLongClickListener(new View.OnLongClickListener(this) {
            /* class com.android.keyguard.magazine.LockScreenMagazinePreView.AnonymousClass6 */

            public boolean onLongClick(View view) {
                return true;
            }
        });
        setFullScreenButtonPadding();
    }

    private void setFullScreenButtonPadding() {
        int i;
        int dimensionPixelOffset = this.mResources.getDimensionPixelOffset(C0012R$dimen.lock_screen_magazine_preview_fullscreen_bottom_button_padding_top);
        if (Build.IS_INTERNATIONAL_BUILD) {
            i = this.mResources.getDimensionPixelOffset(C0012R$dimen.lock_screen_magazine_preview_fullscreen_bottom_global_button_padding_start_end);
        } else {
            i = this.mResources.getDimensionPixelOffset(C0012R$dimen.lock_screen_magazine_preview_fullscreen_bottom_button_padding_start_end);
        }
        this.mEnterButton.setPadding(i, dimensionPixelOffset, i, Build.IS_INTERNATIONAL_BUILD ? dimensionPixelOffset : this.mResources.getDimensionPixelOffset(C0012R$dimen.lock_screen_magazine_preview_fullscreen_bottom_button_padding_bottom));
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        Object locales = CommonUtil.getLocales(configuration);
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
            i = this.mResources.getDimensionPixelOffset(C0012R$dimen.lock_screen_magazine_preview_fullscreen_bottom_global_title_layout_margin_bottom);
        } else {
            i = this.mResources.getDimensionPixelOffset(C0012R$dimen.lock_screen_magazine_preview_fullscreen_bottom_title_layout_margin_bottom);
        }
        layoutParams.setMargins(0, 0, 0, i);
        this.mFullScreenTitleLayout.setLayoutParams(layoutParams);
    }

    private void setFullScreenContentMargin() {
        int i;
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) this.mFullScreenContent.getLayoutParams();
        if (Build.IS_INTERNATIONAL_BUILD) {
            i = this.mResources.getDimensionPixelOffset(C0012R$dimen.lock_screen_magazine_preview_fullscreen_bottom_global_content_margin_bottom);
        } else {
            i = this.mResources.getDimensionPixelOffset(C0012R$dimen.lock_screen_magazine_preview_fullscreen_bottom_content_margin_bottom);
        }
        layoutParams.setMargins(0, 0, 0, i);
        this.mFullScreenContent.setLayoutParams(layoutParams);
    }

    private void setFullScreenBottomLayoutPadding() {
        int i;
        LinearLayout linearLayout = this.mFullScreenBottomLayout;
        int dimensionPixelOffset = this.mResources.getDimensionPixelOffset(C0012R$dimen.lock_screen_magazine_preview_fullscreen_bottom_padding_start);
        int dimensionPixelOffset2 = this.mResources.getDimensionPixelOffset(C0012R$dimen.lock_screen_magazine_preview_fullscreen_bottom_padding_top);
        if (Build.IS_INTERNATIONAL_BUILD) {
            i = this.mResources.getDimensionPixelOffset(C0012R$dimen.lock_screen_magazine_preview_fullscreen_bottom_global_padding_end);
        } else {
            i = this.mResources.getDimensionPixelOffset(C0012R$dimen.lock_screen_magazine_preview_fullscreen_bottom_padding_end);
        }
        linearLayout.setPaddingRelative(dimensionPixelOffset, dimensionPixelOffset2, i, this.mResources.getDimensionPixelOffset(C0012R$dimen.lock_screen_magazine_preview_fullscreen_bottom_padding_bottom));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateSettingButton(final Intent intent) {
        if (intent != null) {
            this.mFullScreenSettingButton.setVisibility(0);
            this.mFullScreenSettingButton.setContentDescription(this.mResources.getText(C0021R$string.accessibility_enter_lock_setting));
            this.mFullScreenSettingButton.setOnClickListener(new View.OnClickListener() {
                /* class com.android.keyguard.magazine.LockScreenMagazinePreView.AnonymousClass7 */

                public void onClick(View view) {
                    if (LockScreenMagazinePreView.this.mPreViewClickListener != null) {
                        LockScreenMagazinePreView.this.mPreViewClickListener.onSettingButtonClick(view, intent);
                    }
                }
            });
            this.mFullScreenSettingButton.setOnLongClickListener(new View.OnLongClickListener(this) {
                /* class com.android.keyguard.magazine.LockScreenMagazinePreView.AnonymousClass8 */

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
        int i;
        int i2;
        if (!isDefaultMainLayout()) {
            updateRemoteTextView(this.mRemoteMainView, true);
        } else if (Build.IS_INTERNATIONAL_BUILD && ((LockScreenMagazineController) Dependency.get(LockScreenMagazineController.class)).isSupportLockScreenMagazineLeft()) {
            this.mMainTitle.setTextSize(0, (float) this.mResources.getDimensionPixelSize(C0012R$dimen.lock_screen_magazine_preview_main_title_text_size));
            this.mMainProvider.setTextSize(0, (float) this.mResources.getDimensionPixelSize(C0012R$dimen.lock_screen_magazine_preview_main_content_text_size));
            this.mMainSource.setTextSize(0, (float) this.mResources.getDimensionPixelSize(C0012R$dimen.lock_screen_magazine_preview_main_content_text_size));
        }
        if (isDefaultFullScreenLayout()) {
            if (Build.IS_INTERNATIONAL_BUILD && ((LockScreenMagazineController) Dependency.get(LockScreenMagazineController.class)).isSupportLockScreenMagazineLeft()) {
                this.mFullScreenProvider.setTextSize(0, (float) this.mResources.getDimensionPixelSize(C0012R$dimen.lock_screen_magazine_preview_fullscreen_bottom_provider_text_size));
                this.mFullScreenSource.setTextSize(0, (float) this.mResources.getDimensionPixelSize(C0012R$dimen.lock_screen_magazine_preview_fullscreen_bottom_source_text_size));
            }
            TextView textView = this.mFullScreenTitle;
            Resources resources = this.mResources;
            if (Build.IS_INTERNATIONAL_BUILD) {
                i = C0012R$dimen.lock_screen_magazine_preview_fullscreen_bottom_title_layout_global_title_text_size;
            } else {
                i = C0012R$dimen.lock_screen_magazine_preview_fullscreen_bottom_title_layout_title_text_size;
            }
            textView.setTextSize(0, (float) resources.getDimensionPixelSize(i));
            TextView textView2 = this.mFullScreenContent;
            Resources resources2 = this.mResources;
            if (Build.IS_INTERNATIONAL_BUILD) {
                i2 = C0012R$dimen.lock_screen_magazine_preview_fullscreen_bottom_global_content_text_size;
            } else {
                i2 = C0012R$dimen.lock_screen_magazine_preview_fullscreen_bottom_content_text_size;
            }
            textView2.setTextSize(0, (float) resources2.getDimensionPixelSize(i2));
            this.mEnterButton.setTextSize(0, (float) this.mResources.getDimensionPixelSize(C0012R$dimen.lock_screen_magazine_preview_fullscreen_bottom_button_text_size));
            return;
        }
        updateRemoteTextView(this.mRemoteFullScreenView, false);
    }

    private void updateDrawableResource() {
        if (isDefaultFullScreenLayout()) {
            if (!Build.IS_INTERNATIONAL_BUILD && ((LockScreenMagazineController) Dependency.get(LockScreenMagazineController.class)).isSupportLockScreenMagazineLeft()) {
                updateSettingButtonDrawableResource();
                updateLinkButtonDrawableResource();
            }
            this.mEnterButton.setBackgroundResource(C0013R$drawable.lock_screen_magazine_des_more_btn_bg);
            return;
        }
        updateRemoteTextView(this.mRemoteFullScreenLayout, false);
    }

    private void updateSettingButtonDrawableResource() {
        int i;
        ImageView imageView = this.mFullScreenSettingButton;
        if (this.mDarkStyle) {
            i = C0013R$drawable.lock_screen_magazine_pre_settings_dark;
        } else {
            i = C0013R$drawable.lock_screen_magazine_pre_settings;
        }
        imageView.setImageResource(i);
    }

    private void updateLinkButtonDrawableResource() {
        LockScreenMagazineWallpaperInfo lockScreenMagazineWallpaperInfo = this.mLockScreenMagazineWallpaperInfo;
        if (lockScreenMagazineWallpaperInfo != null) {
            int i = lockScreenMagazineWallpaperInfo.linkType;
            if (i == 1) {
                this.mFullScreenLinkButton.setImageResource(C0013R$drawable.lock_screen_magazine_pre_link);
            } else if (i == 2) {
                this.mFullScreenLinkButton.setImageResource(C0013R$drawable.wallpaper_play);
            }
        }
    }

    private void updateViewsLayoutParams() {
        if (Build.IS_INTERNATIONAL_BUILD && ((LockScreenMagazineController) Dependency.get(LockScreenMagazineController.class)).isSupportLockScreenMagazineLeft()) {
            if (isDefaultMainLayout()) {
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) this.mMainLayout.getLayoutParams();
                layoutParams.setMargins(0, 0, 0, this.mResources.getDimensionPixelOffset(C0012R$dimen.keyguard_affordance_height));
                this.mMainLayout.setLayoutParams(layoutParams);
                LinearLayout.LayoutParams layoutParams2 = (LinearLayout.LayoutParams) this.mMainTitle.getLayoutParams();
                layoutParams2.setMargins(this.mResources.getDimensionPixelOffset(C0012R$dimen.lock_screen_magazine_preview_main_title_margin_start_end), 0, this.mResources.getDimensionPixelOffset(C0012R$dimen.lock_screen_magazine_preview_main_title_margin_start_end), this.mResources.getDimensionPixelOffset(C0012R$dimen.lock_screen_magazine_preview_main_title_margin_bottom));
                this.mMainTitle.setLayoutParams(layoutParams2);
                LinearLayout.LayoutParams layoutParams3 = (LinearLayout.LayoutParams) this.mMainProvider.getLayoutParams();
                layoutParams3.setMargins(this.mResources.getDimensionPixelOffset(C0012R$dimen.lock_screen_magazine_preview_main_provider_margin_start), 0, 0, 0);
                this.mMainProvider.setLayoutParams(layoutParams3);
            } else {
                updateRemoteTextView(this.mRemoteMainLayout, true);
            }
        }
        if (isDefaultFullScreenLayout()) {
            if (((LockScreenMagazineController) Dependency.get(LockScreenMagazineController.class)).isSupportLockScreenMagazineLeft()) {
                LinearLayout.LayoutParams layoutParams4 = (LinearLayout.LayoutParams) this.mFullScreenSettingButton.getLayoutParams();
                layoutParams4.setMargins(0, this.mResources.getDimensionPixelOffset(C0012R$dimen.lock_screen_magazine_preview_fullscreen_setting_margin_top), this.mResources.getDimensionPixelOffset(C0012R$dimen.lock_screen_magazine_preview_fullscreen_setting_margin_end), 0);
                this.mFullScreenSettingButton.setLayoutParams(layoutParams4);
                RelativeLayout.LayoutParams layoutParams5 = (RelativeLayout.LayoutParams) this.mFullScreenLinkButton.getLayoutParams();
                layoutParams5.width = this.mResources.getDimensionPixelOffset(C0012R$dimen.lock_screen_magazine_preview_fullscreen_bottom_title_layout_link_width_height);
                layoutParams5.height = this.mResources.getDimensionPixelOffset(C0012R$dimen.lock_screen_magazine_preview_fullscreen_bottom_title_layout_link_width_height);
                this.mFullScreenLinkButton.setLayoutParams(layoutParams5);
            }
            setFullScreenBottomLayoutPadding();
            setFullScreenTitleLayoutMargin();
            setFullScreenContentMargin();
            LinearLayout.LayoutParams layoutParams6 = (LinearLayout.LayoutParams) this.mEnterButton.getLayoutParams();
            layoutParams6.setMargins(this.mResources.getDimensionPixelOffset(C0012R$dimen.lock_screen_magazine_preview_fullscreen_bottom_button_margin_start), 0, 0, 0);
            this.mEnterButton.setLayoutParams(layoutParams6);
            setFullScreenButtonPadding();
            return;
        }
        updateRemoteTextView(this.mRemoteFullScreenLayout, false);
    }

    public void refreshWallpaperInfo(RemoteViews remoteViews, RemoteViews remoteViews2) {
        LockScreenMagazineWallpaperInfo lockScreenMagazineWallpaperInfo = ((LockScreenMagazineController) Dependency.get(LockScreenMagazineController.class)).getLockScreenMagazineWallpaperInfo();
        this.mLockScreenMagazineWallpaperInfo = lockScreenMagazineWallpaperInfo;
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
        post(new Runnable() {
            /* class com.android.keyguard.magazine.LockScreenMagazinePreView.AnonymousClass9 */

            public void run() {
                if (LockScreenMagazinePreView.this.mMainRemoteView == null) {
                    LockScreenMagazinePreView.this.mRemotePackageName = null;
                    LockScreenMagazinePreView.this.updateMainView();
                } else {
                    LockScreenMagazinePreView lockScreenMagazinePreView = LockScreenMagazinePreView.this;
                    lockScreenMagazinePreView.mRemotePackageName = lockScreenMagazinePreView.mMainRemoteView.getPackage();
                    try {
                        LockScreenMagazinePreView.this.updateRemoteMainView();
                    } catch (Exception e) {
                        Log.e("LockScreenMagazinePreView", "updateRemoteMainView " + e.getMessage());
                        LockScreenMagazinePreView.this.updateMainView();
                    }
                }
                if (LockScreenMagazinePreView.this.mFullScreenRemoteView == null) {
                    LockScreenMagazinePreView.this.mRemotePackageName = null;
                    LockScreenMagazinePreView.this.updateFullScreenView();
                } else {
                    LockScreenMagazinePreView lockScreenMagazinePreView2 = LockScreenMagazinePreView.this;
                    lockScreenMagazinePreView2.mRemotePackageName = lockScreenMagazinePreView2.mFullScreenRemoteView.getPackage();
                    try {
                        LockScreenMagazinePreView.this.updateRemoteFullScreenView();
                    } catch (Exception e2) {
                        Log.e("LockScreenMagazinePreView", "updateRemoteFullScreenView " + e2.getMessage());
                        LockScreenMagazinePreView.this.updateFullScreenView();
                    }
                }
                LockScreenMagazinePreView.this.initLayoutVisibility();
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateMainView() {
        if (Build.IS_INTERNATIONAL_BUILD && ((LockScreenMagazineController) Dependency.get(LockScreenMagazineController.class)).isSupportLockScreenMagazineLeft()) {
            updateMainTitle();
            updateMainProviderText();
            updateMainSourceText();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateFullScreenView() {
        if (Build.IS_INTERNATIONAL_BUILD && ((LockScreenMagazineController) Dependency.get(LockScreenMagazineController.class)).isSupportLockScreenMagazineLeft()) {
            updateFullScreenProviderText();
            updateFullScreenSourceText();
        }
        updateFullScreenTitle();
        updateFullScreenContentText();
        updateFullScreenButtonText();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
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
        View apply = this.mMainRemoteView.apply(getContext(), this.mRemoteMainLayout);
        this.mRemoteMainView = apply;
        apply.setIsRootNamespace(true);
        updateRemoteTextView(this.mRemoteMainView, true);
        this.mRemoteMainLayout.addView(this.mRemoteMainView);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
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
        View apply = this.mFullScreenRemoteView.apply(getContext(), this.mRemoteFullScreenLayout);
        this.mRemoteFullScreenView = apply;
        apply.setIsRootNamespace(true);
        updateRemoteTextView(this.mRemoteFullScreenView, false);
        this.mRemoteFullScreenLayout.addView(this.mRemoteFullScreenView);
    }

    private void updateRemoteTextView(View view, boolean z) {
        int i;
        int i2;
        if (view != null) {
            String str = this.mRemotePackageName;
            if (!TextUtils.isEmpty(str)) {
                try {
                    Resources resourcesForApplication = getContext().getPackageManager().getResourcesForApplication(str);
                    int identifier = resourcesForApplication.getIdentifier("wallpaper_title", "id", str);
                    int identifier2 = resourcesForApplication.getIdentifier("wallpaper_content", "id", str);
                    int identifier3 = resourcesForApplication.getIdentifier("more_info", "id", str);
                    View findViewById = view.findViewById(identifier);
                    View findViewById2 = view.findViewById(identifier2);
                    View findViewById3 = view.findViewById(identifier3);
                    int color = this.mDarkStyle ? getContext().getResources().getColor(C0011R$color.miui_common_unlock_screen_common_time_dark_text_color) : -1;
                    if (findViewById instanceof TextView) {
                        TextView textView = (TextView) findViewById;
                        Resources resources = this.mResources;
                        if (Build.IS_INTERNATIONAL_BUILD) {
                            i2 = C0012R$dimen.lock_screen_magazine_preview_fullscreen_bottom_title_layout_global_title_text_size;
                        } else {
                            i2 = C0012R$dimen.lock_screen_magazine_preview_fullscreen_bottom_title_layout_title_text_size;
                        }
                        textView.setTextSize(0, (float) resources.getDimensionPixelSize(i2));
                        textView.setTypeface(Typeface.create("sans-serif-medium", 0));
                        if (z) {
                            textView.setTextColor(color);
                        }
                    }
                    if (findViewById2 instanceof TextView) {
                        TextView textView2 = (TextView) findViewById2;
                        Resources resources2 = this.mResources;
                        if (Build.IS_INTERNATIONAL_BUILD) {
                            i = C0012R$dimen.lock_screen_magazine_preview_fullscreen_bottom_global_content_text_size;
                        } else {
                            i = C0012R$dimen.lock_screen_magazine_preview_fullscreen_bottom_content_text_size;
                        }
                        textView2.setTextSize(0, (float) resources2.getDimensionPixelSize(i));
                        textView2.setTypeface(Typeface.create("miui-light", 0));
                        if (z) {
                            textView2.setTextColor(color);
                        }
                    }
                    if (findViewById3 instanceof TextView) {
                        TextView textView3 = (TextView) findViewById3;
                        textView3.setTextSize(0, (float) this.mResources.getDimensionPixelSize(C0012R$dimen.lock_screen_magazine_preview_fullscreen_bottom_button_text_size));
                        if (z) {
                            textView3.setTextColor(color);
                            textView3.setBackground(this.mResources.getDrawable(this.mDarkStyle ? C0013R$drawable.lock_screen_magazine_des_global_more_btn_bg_dark : C0013R$drawable.lock_screen_magazine_des_global_more_btn_bg));
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
        int i;
        if (this.mLockScreenMagazineWallpaperInfo != null) {
            Resources resources = ((RelativeLayout) this).mContext.getResources();
            if (Build.IS_INTERNATIONAL_BUILD) {
                i = C0021R$string.lock_screen_magazine_preview_fullscreen_bottom_button_global_text;
            } else {
                i = C0021R$string.lock_screen_magazine_preview_fullscreen_bottom_button_text;
            }
            String string = resources.getString(i);
            if (((LockScreenMagazineController) Dependency.get(LockScreenMagazineController.class)).isLockScreenMagazinePkgExist()) {
                if (!WallpaperAuthorityUtils.isLockScreenMagazineOpenedWallpaper() && !TextUtils.isEmpty(this.mLockScreenMagazineWallpaperInfo.btnText)) {
                    string = this.mLockScreenMagazineWallpaperInfo.btnText;
                }
                if (Build.IS_INTERNATIONAL_BUILD && !TextUtils.isEmpty(this.mLockScreenMagazineWallpaperInfo.globalBtnText)) {
                    string = this.mLockScreenMagazineWallpaperInfo.globalBtnText;
                }
            } else {
                string = ((RelativeLayout) this).mContext.getResources().getString(C0021R$string.download_lock_wallpaper);
            }
            this.mEnterButton.setText(string);
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
    /* access modifiers changed from: public */
    private void updateLinkButton() {
        if (isDefaultFullScreenLayout()) {
            LockScreenMagazineWallpaperInfo lockScreenMagazineWallpaperInfo = this.mLockScreenMagazineWallpaperInfo;
            if (lockScreenMagazineWallpaperInfo == null || TextUtils.isEmpty(lockScreenMagazineWallpaperInfo.landingPageUrl)) {
                updateLinkButtonLayoutParams((int) this.mFullScreenTitleLayoutWidth, 0, 0);
                this.mFullScreenLinkButton.setVisibility(8);
                return;
            }
            float dimensionPixelOffset = (float) this.mResources.getDimensionPixelOffset(C0012R$dimen.lock_screen_magazine_preview_fullscreen_bottom_title_layout_link_width_height);
            float dimensionPixelOffset2 = (float) this.mResources.getDimensionPixelOffset(C0012R$dimen.lock_screen_magazine_preview_fullscreen_bottom_link_margin_start);
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
        if (!WallpaperAuthorityUtils.isLockScreenMagazineWallpaper()) {
            return null;
        }
        String string = getResources().getString(C0021R$string.lock_screen_magazine_default_title);
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
        if (!WallpaperAuthorityUtils.isLockScreenMagazineOpenedWallpaper() || (lockScreenMagazineWallpaperInfo = this.mLockScreenMagazineWallpaperInfo) == null || TextUtils.isEmpty(lockScreenMagazineWallpaperInfo.title)) {
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
        if (TextUtils.isEmpty(providerText) || !Build.IS_INTERNATIONAL_BUILD || !((LockScreenMagazineController) Dependency.get(LockScreenMagazineController.class)).isSupportLockScreenMagazineLeft()) {
            this.mFullScreenProvider.setVisibility(8);
            return;
        }
        this.mFullScreenProvider.setVisibility(0);
        this.mFullScreenProvider.setText(providerText);
    }

    private void updateFullScreenSourceText() {
        String sourceText = getSourceText();
        if (TextUtils.isEmpty(sourceText) || !Build.IS_INTERNATIONAL_BUILD || !((LockScreenMagazineController) Dependency.get(LockScreenMagazineController.class)).isSupportLockScreenMagazineLeft()) {
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
        if (!WallpaperAuthorityUtils.isLockScreenMagazineWallpaper()) {
            return null;
        }
        String string = getResources().getString(C0021R$string.lock_screen_magazine_default_content);
        LockScreenMagazineWallpaperInfo lockScreenMagazineWallpaperInfo = this.mLockScreenMagazineWallpaperInfo;
        if (lockScreenMagazineWallpaperInfo == null || TextUtils.isEmpty(lockScreenMagazineWallpaperInfo.content)) {
            return string;
        }
        return (Build.IS_INTERNATIONAL_BUILD || Locale.CHINESE.getLanguage().equals(Locale.getDefault().getLanguage())) ? this.mLockScreenMagazineWallpaperInfo.content : string;
    }

    private String getProviderText() {
        LockScreenMagazineWallpaperInfo lockScreenMagazineWallpaperInfo;
        if (WallpaperAuthorityUtils.isLockScreenMagazineOpenedWallpaper() && (lockScreenMagazineWallpaperInfo = this.mLockScreenMagazineWallpaperInfo) != null && !TextUtils.isEmpty(lockScreenMagazineWallpaperInfo.provider)) {
            return this.mLockScreenMagazineWallpaperInfo.provider;
        }
        return null;
    }

    private String getSourceText() {
        LockScreenMagazineWallpaperInfo lockScreenMagazineWallpaperInfo;
        if (WallpaperAuthorityUtils.isLockScreenMagazineOpenedWallpaper() && (lockScreenMagazineWallpaperInfo = this.mLockScreenMagazineWallpaperInfo) != null && !TextUtils.isEmpty(lockScreenMagazineWallpaperInfo.source)) {
            return this.mLockScreenMagazineWallpaperInfo.source;
        }
        return null;
    }

    private String getMainSourceText() {
        LockScreenMagazineWallpaperInfo lockScreenMagazineWallpaperInfo;
        if (WallpaperAuthorityUtils.isLockScreenMagazineOpenedWallpaper() && (lockScreenMagazineWallpaperInfo = this.mLockScreenMagazineWallpaperInfo) != null && !TextUtils.isEmpty(lockScreenMagazineWallpaperInfo.source)) {
            return this.mLockScreenMagazineWallpaperInfo.source;
        }
        return null;
    }

    public void setMainLayoutVisible(int i) {
        getMainLayout().setVisibility(i);
        AnalyticsHelper.getInstance(((RelativeLayout) this).mContext).setLockScreenMagazineMainPreShow(i == 0);
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
        Log.e("LockScreenMagazinePreView", "getFullScreenLayout()  mRemoteFullScreenView:" + this.mRemoteFullScreenView);
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
