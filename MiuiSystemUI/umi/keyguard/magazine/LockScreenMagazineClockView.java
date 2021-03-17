package com.android.keyguard.magazine;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.analytics.AnalyticsHelper;
import com.android.keyguard.magazine.entity.LockScreenMagazineWallpaperInfo;
import com.android.keyguard.magazine.utils.LockScreenMagazineUtils;
import com.android.keyguard.utils.MiuiKeyguardUtils;
import com.android.keyguard.utils.PackageUtils;
import com.android.keyguard.wallpaper.WallpaperAuthorityUtils;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0015R$id;
import com.android.systemui.Dependency;
import com.miui.systemui.util.CommonExtensionsKt;
import java.util.Locale;
import miui.os.Build;
import miui.os.SystemProperties;
import miui.util.Log;

public class LockScreenMagazineClockView extends LinearLayout {
    private LinearLayout mContentsLayout;
    private boolean mDarkStyle;
    private boolean mHasTitleClick;
    private boolean mIsLeftTopClock;
    private LockScreenMagazineWallpaperInfo mMagazineWallpaperInfo;
    private TextView mProvider;
    private TextView mSource;
    private TextView mTitle;

    public LockScreenMagazineClockView(Context context) {
        this(context, null);
    }

    public LockScreenMagazineClockView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mTitle = (TextView) findViewById(C0015R$id.lock_screen_magazine_clock_title);
        initTitle();
        this.mContentsLayout = (LinearLayout) findViewById(C0015R$id.lock_screen_magazine_clock_contents_layout);
        this.mProvider = (TextView) findViewById(C0015R$id.lock_screen_magazine_clock_provider);
        this.mSource = (TextView) findViewById(C0015R$id.lock_screen_magazine_clock_source);
    }

    private void initTitle() {
        if (!Build.IS_INTERNATIONAL_BUILD) {
            this.mHasTitleClick = true;
            this.mTitle.setOnClickListener(new View.OnClickListener() {
                /* class com.android.keyguard.magazine.$$Lambda$LockScreenMagazineClockView$YmimdYIU4lBR3j3h5JaKPHLDoeo */

                public final void onClick(View view) {
                    LockScreenMagazineClockView.this.lambda$initTitle$0$LockScreenMagazineClockView(view);
                }
            });
            setLockScreenMagazineTitleTouchDelegate(30);
        }
        updateTitlePadding();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$initTitle$0 */
    public /* synthetic */ void lambda$initTitle$0$LockScreenMagazineClockView(View view) {
        if (CommonExtensionsKt.checkFastDoubleClick(view, 500)) {
            LockScreenMagazineUtils.gotoMagazine(((LinearLayout) this).mContext, "lockScreenInfo");
            AnalyticsHelper.getInstance(((LinearLayout) this).mContext).recordLockScreenMagazineEntryClickAction();
        }
    }

    public void updateInfo() {
        String str;
        String str2;
        this.mMagazineWallpaperInfo = ((LockScreenMagazineController) Dependency.get(LockScreenMagazineController.class)).getLockScreenMagazineWallpaperInfo();
        String str3 = null;
        if (!LockScreenMagazineUtils.isLockScreenMagazineAvailable() || (((!Build.IS_INTERNATIONAL_BUILD || !WallpaperAuthorityUtils.isLockScreenMagazineOpenedWallpaper() || !MiuiKeyguardUtils.isGxzwSensor()) && (Build.IS_INTERNATIONAL_BUILD || !WallpaperAuthorityUtils.isLockScreenMagazineWallpaper())) || !((LockScreenMagazineController) Dependency.get(LockScreenMagazineController.class)).isLockScreenMagazinePkgExist() || this.mMagazineWallpaperInfo == null)) {
            str2 = null;
            str = null;
        } else {
            str3 = getLockScreenMagazineInfoTitle();
            str2 = getLockScreenMagazineProvider();
            str = getLockScreenMagazineSource();
        }
        if (TextUtils.isEmpty(str3)) {
            this.mTitle.setVisibility(8);
            setLockScreenMagazineTitleTouchDelegate(0);
            this.mContentsLayout.setVisibility(8);
            return;
        }
        this.mTitle.setText(str3);
        this.mTitle.setVisibility(0);
        setLockScreenMagazineTitleTouchDelegate(30);
        updateTitle();
        if (!Build.IS_INTERNATIONAL_BUILD || (TextUtils.isEmpty(str2) && TextUtils.isEmpty(str))) {
            this.mContentsLayout.setVisibility(8);
            return;
        }
        this.mContentsLayout.setVisibility(0);
        if (!TextUtils.isEmpty(str2)) {
            this.mProvider.setText(str2);
            this.mProvider.setVisibility(0);
        } else {
            this.mProvider.setVisibility(8);
        }
        if (!TextUtils.isEmpty(str)) {
            this.mSource.setText(str);
            this.mSource.setVisibility(0);
            return;
        }
        this.mSource.setVisibility(8);
    }

    private void updateTitle() {
        if (Build.IS_INTERNATIONAL_BUILD) {
            if (!TextUtils.isEmpty(this.mMagazineWallpaperInfo.titleClickUri)) {
                new AsyncTask<Void, Void, Intent>() {
                    /* class com.android.keyguard.magazine.LockScreenMagazineClockView.AnonymousClass1 */

                    /* access modifiers changed from: protected */
                    public Intent doInBackground(Void... voidArr) {
                        Intent intent = new Intent();
                        Uri parse = Uri.parse(LockScreenMagazineClockView.this.mMagazineWallpaperInfo.carouselDeeplink);
                        LockScreenMagazineClockView lockScreenMagazineClockView = LockScreenMagazineClockView.this;
                        if (lockScreenMagazineClockView.needJump92(((LinearLayout) lockScreenMagazineClockView).mContext, parse)) {
                            intent.putExtra("deeplink92Uri", parse);
                        } else {
                            intent.setAction("android.intent.action.VIEW");
                            intent.addFlags(268435456);
                            intent.addFlags(67108864);
                        }
                        intent.setData(Uri.parse(LockScreenMagazineClockView.this.mMagazineWallpaperInfo.titleClickUri));
                        if (PackageUtils.resolveIntent(((LinearLayout) LockScreenMagazineClockView.this).mContext, intent) != null) {
                            return intent;
                        }
                        return null;
                    }

                    /* access modifiers changed from: protected */
                    public void onPostExecute(final Intent intent) {
                        boolean z;
                        if (intent == null) {
                            LockScreenMagazineClockView.this.mTitle.setOnClickListener(null);
                            z = false;
                        } else {
                            LockScreenMagazineClockView.this.mTitle.setOnClickListener(new View.OnClickListener() {
                                /* class com.android.keyguard.magazine.LockScreenMagazineClockView.AnonymousClass1.AnonymousClass1 */

                                public void onClick(View view) {
                                    if (CommonExtensionsKt.checkFastDoubleClick(view, 500)) {
                                        Uri uri = (Uri) intent.getParcelableExtra("deeplink92Uri");
                                        if (uri == null || !PackageUtils.isAppInstalledForUser(((LinearLayout) LockScreenMagazineClockView.this).mContext, "com.ziyou.haokan", KeyguardUpdateMonitor.getCurrentUser())) {
                                            ((LinearLayout) LockScreenMagazineClockView.this).mContext.startActivityAsUser(intent, UserHandle.CURRENT);
                                            return;
                                        }
                                        Intent intent = new Intent();
                                        intent.setData(uri);
                                        ((LinearLayout) LockScreenMagazineClockView.this).mContext.startActivityAsUser(intent, UserHandle.CURRENT);
                                        ((LinearLayout) LockScreenMagazineClockView.this).mContext.sendBroadcast(new Intent("xiaomi.intent.action.SHOW_SECURE_KEYGUARD"));
                                        Log.e("LockScreenMagazineClockView", "title onClick  start activity ! ");
                                    }
                                }
                            });
                            z = true;
                        }
                        LockScreenMagazineClockView.this.updateTitleClickLayout(z);
                    }
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
                return;
            }
            this.mTitle.setOnClickListener(null);
            updateTitleClickLayout(false);
        }
    }

    public boolean needJump92(Context context, Uri uri) {
        return uri != null && "MY".equalsIgnoreCase(SystemProperties.get("ro.miui.region", "")) && PackageUtils.isAppInstalledForUser(context, "com.ziyou.haokan", KeyguardUpdateMonitor.getCurrentUser());
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateTitleClickLayout(boolean z) {
        if (z != this.mHasTitleClick) {
            this.mHasTitleClick = z;
            updateTitlePadding();
            updateDrawableResources(this.mDarkStyle);
        }
    }

    private String getLockScreenMagazineInfoTitle() {
        if (((LockScreenMagazineController) Dependency.get(LockScreenMagazineController.class)).isSupportLockScreenMagazineLeft()) {
            if (!Build.IS_INTERNATIONAL_BUILD) {
                LockScreenMagazineWallpaperInfo lockScreenMagazineWallpaperInfo = this.mMagazineWallpaperInfo;
                if (!lockScreenMagazineWallpaperInfo.isTitleCustomized) {
                    if (!TextUtils.isEmpty(lockScreenMagazineWallpaperInfo.entryTitle) && Locale.CHINESE.getLanguage().equals(Locale.getDefault().getLanguage())) {
                        return this.mMagazineWallpaperInfo.entryTitle;
                    }
                }
            }
            return this.mMagazineWallpaperInfo.title;
        } else if (!TextUtils.isEmpty(this.mMagazineWallpaperInfo.title) && Locale.CHINESE.getLanguage().equals(Locale.getDefault().getLanguage())) {
            return this.mMagazineWallpaperInfo.title;
        }
        return null;
    }

    private String getLockScreenMagazineProvider() {
        if (TextUtils.isEmpty(this.mMagazineWallpaperInfo.provider)) {
            return null;
        }
        return this.mMagazineWallpaperInfo.provider;
    }

    private String getLockScreenMagazineSource() {
        if (TextUtils.isEmpty(this.mMagazineWallpaperInfo.source)) {
            return null;
        }
        return this.mMagazineWallpaperInfo.source;
    }

    public void setTextColor(int i) {
        this.mTitle.setTextColor(i);
        this.mProvider.setTextColor(i);
        this.mSource.setTextColor(i);
    }

    public void setTextSize() {
        int dimensionPixelSize = getResources().getDimensionPixelSize(C0012R$dimen.miui_clock_date_text_size);
        int dimensionPixelSize2 = getResources().getDimensionPixelSize(C0012R$dimen.lock_screen_magazine_clock_provider_source_text_size);
        this.mTitle.setTextSize(0, (float) dimensionPixelSize);
        float f = (float) dimensionPixelSize2;
        this.mProvider.setTextSize(0, f);
        this.mSource.setTextSize(0, f);
    }

    public void updateDrawableResources(boolean z) {
        int i;
        if (this.mDarkStyle != z) {
            this.mDarkStyle = z;
            if (!Build.IS_INTERNATIONAL_BUILD || this.mHasTitleClick) {
                Resources resources = getResources();
                if (z) {
                    i = C0013R$drawable.keyguard_bottom_guide_right_arrow_dark;
                } else {
                    i = C0013R$drawable.keyguard_bottom_guide_right_arrow;
                }
                this.mTitle.setCompoundDrawablesWithIntrinsicBounds((Drawable) null, (Drawable) null, resources.getDrawable(i), (Drawable) null);
                return;
            }
            this.mTitle.setCompoundDrawablesWithIntrinsicBounds((Drawable) null, (Drawable) null, (Drawable) null, (Drawable) null);
        }
    }

    public void updateTitlePadding() {
        this.mTitle.setPaddingRelative(!this.mIsLeftTopClock && this.mHasTitleClick ? ((LinearLayout) this).mContext.getResources().getDimensionPixelOffset(C0012R$dimen.lock_screen_magazine_clock_title_padding_start) : 0, 0, 0, 0);
    }

    private void updateContentsLayoutGravity() {
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) this.mContentsLayout.getLayoutParams();
        layoutParams.gravity = this.mIsLeftTopClock ? 8388611 : 17;
        this.mContentsLayout.setLayoutParams(layoutParams);
    }

    public void updateViewsForClockPosition(boolean z) {
        this.mIsLeftTopClock = z;
        updateTitlePadding();
        updateContentsLayoutGravity();
    }

    private void setLockScreenMagazineTitleTouchDelegate(int i) {
        if (!Build.IS_INTERNATIONAL_BUILD) {
            MiuiKeyguardUtils.setViewTouchDelegate(this.mTitle, i);
        }
    }
}
