package com.android.keyguard.magazine;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.MiuiKeyguardUtils;
import com.android.keyguard.analytics.AnalyticsHelper;
import com.android.keyguard.magazine.mode.LockScreenMagazineWallpaperInfo;
import com.android.keyguard.utils.PackageUtils;
import com.android.keyguard.wallpaper.WallpaperAuthorityUtils;
import com.android.systemui.plugins.R;
import java.util.Locale;
import miui.os.Build;

public class LockScreenMagazineClockView extends LinearLayout {
    private LinearLayout mContentsLayout;
    private boolean mDarkMode;
    private boolean mHasTitleClick;
    private boolean mIsLeftTopClock;
    /* access modifiers changed from: private */
    public long mLastClickTime;
    /* access modifiers changed from: private */
    public LockScreenMagazineWallpaperInfo mMagazineWallpaperInfo;
    private TextView mProvider;
    private TextView mSource;
    /* access modifiers changed from: private */
    public TextView mTitle;

    public LockScreenMagazineClockView(Context context) {
        this(context, (AttributeSet) null);
    }

    public LockScreenMagazineClockView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mLastClickTime = 0;
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mTitle = (TextView) findViewById(R.id.lock_screen_magazine_clock_title);
        initTitle();
        this.mContentsLayout = (LinearLayout) findViewById(R.id.lock_screen_magazine_clock_contents_layout);
        this.mProvider = (TextView) findViewById(R.id.lock_screen_magazine_clock_provider);
        this.mSource = (TextView) findViewById(R.id.lock_screen_magazine_clock_source);
    }

    private void initTitle() {
        if (!Build.IS_INTERNATIONAL_BUILD) {
            this.mHasTitleClick = true;
            this.mTitle.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    if (SystemClock.elapsedRealtime() - LockScreenMagazineClockView.this.mLastClickTime > 500) {
                        LockScreenMagazineUtils.gotoLockScreenMagazine(LockScreenMagazineClockView.this.mContext, "lockScreenInfo");
                        AnalyticsHelper.getInstance(LockScreenMagazineClockView.this.mContext).recordLockScreenMagazineEntryClickAction();
                    }
                    long unused = LockScreenMagazineClockView.this.mLastClickTime = SystemClock.elapsedRealtime();
                }
            });
            setLockScreenMagazineTitleTouchDelegate(30);
        }
        updateTitlePadding();
    }

    public void updateInfo() {
        String str;
        String str2;
        LockScreenMagazineWallpaperInfo lockScreenMagazineWallpaperInfo = KeyguardUpdateMonitor.getInstance(this.mContext).getLockScreenMagazineWallpaperInfo();
        this.mMagazineWallpaperInfo = lockScreenMagazineWallpaperInfo;
        String str3 = null;
        if (!LockScreenMagazineUtils.isLockScreenMagazineAvailable(this.mContext) || (((!Build.IS_INTERNATIONAL_BUILD || !WallpaperAuthorityUtils.isLockScreenMagazineOpenedWallpaper(this.mContext) || !MiuiKeyguardUtils.isGxzwSensor()) && (Build.IS_INTERNATIONAL_BUILD || !WallpaperAuthorityUtils.isLockScreenMagazineWallpaper(this.mContext))) || !KeyguardUpdateMonitor.getInstance(this.mContext).isLockScreenMagazinePkgExist() || lockScreenMagazineWallpaperInfo == null)) {
            str2 = null;
            str = null;
        } else {
            str3 = getLockScreenMagazineInfoTitle(lockScreenMagazineWallpaperInfo);
            str = getLockScreenMagazineProvider(lockScreenMagazineWallpaperInfo);
            str2 = getLockScreenMagazineSource(lockScreenMagazineWallpaperInfo);
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
        if (!Build.IS_INTERNATIONAL_BUILD || (TextUtils.isEmpty(str) && TextUtils.isEmpty(str2))) {
            this.mContentsLayout.setVisibility(8);
            return;
        }
        this.mContentsLayout.setVisibility(0);
        if (!TextUtils.isEmpty(str)) {
            this.mProvider.setText(str);
            this.mProvider.setVisibility(0);
        } else {
            this.mProvider.setVisibility(8);
        }
        if (!TextUtils.isEmpty(str2)) {
            this.mSource.setText(str2);
            this.mSource.setVisibility(0);
            return;
        }
        this.mSource.setVisibility(8);
    }

    private void updateTitle() {
        if (Build.IS_INTERNATIONAL_BUILD) {
            if (!TextUtils.isEmpty(this.mMagazineWallpaperInfo.titleClickUri)) {
                new AsyncTask<Void, Void, Intent>() {
                    /* access modifiers changed from: protected */
                    public Intent doInBackground(Void... voidArr) {
                        Intent intent = new Intent("android.intent.action.VIEW");
                        intent.setData(Uri.parse(LockScreenMagazineClockView.this.mMagazineWallpaperInfo.titleClickUri));
                        intent.addFlags(268435456);
                        intent.addFlags(67108864);
                        if (PackageUtils.resolveIntent(LockScreenMagazineClockView.this.mContext, intent) != null) {
                            return intent;
                        }
                        return null;
                    }

                    /* access modifiers changed from: protected */
                    public void onPostExecute(final Intent intent) {
                        boolean z;
                        if (intent == null) {
                            LockScreenMagazineClockView.this.mTitle.setOnClickListener((View.OnClickListener) null);
                            z = false;
                        } else {
                            LockScreenMagazineClockView.this.mTitle.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View view) {
                                    if (SystemClock.elapsedRealtime() - LockScreenMagazineClockView.this.mLastClickTime > 500) {
                                        LockScreenMagazineClockView.this.mContext.startActivityAsUser(intent, UserHandle.CURRENT);
                                    }
                                    long unused = LockScreenMagazineClockView.this.mLastClickTime = SystemClock.elapsedRealtime();
                                }
                            });
                            z = true;
                        }
                        LockScreenMagazineClockView.this.updateTitleClickLayout(z);
                    }
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
                return;
            }
            this.mTitle.setOnClickListener((View.OnClickListener) null);
            updateTitleClickLayout(false);
        }
    }

    /* access modifiers changed from: private */
    public void updateTitleClickLayout(boolean z) {
        if (z != this.mHasTitleClick) {
            this.mHasTitleClick = z;
            updateTitlePadding();
            updateDrawableResources(this.mDarkMode);
        }
    }

    private String getLockScreenMagazineInfoTitle(LockScreenMagazineWallpaperInfo lockScreenMagazineWallpaperInfo) {
        if (KeyguardUpdateMonitor.getInstance(this.mContext).isSupportLockScreenMagazineLeft()) {
            if (Build.IS_INTERNATIONAL_BUILD || lockScreenMagazineWallpaperInfo.isTitleCustomized) {
                return lockScreenMagazineWallpaperInfo.title;
            }
            if (!TextUtils.isEmpty(lockScreenMagazineWallpaperInfo.entryTitle) && Locale.CHINESE.getLanguage().equals(Locale.getDefault().getLanguage())) {
                return lockScreenMagazineWallpaperInfo.entryTitle;
            }
        } else if (!TextUtils.isEmpty(lockScreenMagazineWallpaperInfo.title) && Locale.CHINESE.getLanguage().equals(Locale.getDefault().getLanguage())) {
            return lockScreenMagazineWallpaperInfo.title;
        }
        return null;
    }

    private String getLockScreenMagazineProvider(LockScreenMagazineWallpaperInfo lockScreenMagazineWallpaperInfo) {
        if (TextUtils.isEmpty(lockScreenMagazineWallpaperInfo.provider)) {
            return null;
        }
        return lockScreenMagazineWallpaperInfo.provider;
    }

    private String getLockScreenMagazineSource(LockScreenMagazineWallpaperInfo lockScreenMagazineWallpaperInfo) {
        if (TextUtils.isEmpty(lockScreenMagazineWallpaperInfo.source)) {
            return null;
        }
        return lockScreenMagazineWallpaperInfo.source;
    }

    public void setTextColor(int i) {
        this.mTitle.setTextColor(i);
        this.mProvider.setTextColor(i);
        this.mSource.setTextColor(i);
    }

    public void setTextSize() {
        int dimensionPixelSize = getResources().getDimensionPixelSize(R.dimen.miui_clock_date_text_size);
        int dimensionPixelSize2 = getResources().getDimensionPixelSize(R.dimen.lock_screen_magazine_clock_provider_source_text_size);
        this.mTitle.setTextSize(0, (float) dimensionPixelSize);
        float f = (float) dimensionPixelSize2;
        this.mProvider.setTextSize(0, f);
        this.mSource.setTextSize(0, f);
    }

    public void updateDrawableResources(boolean z) {
        this.mDarkMode = z;
        if (!Build.IS_INTERNATIONAL_BUILD || this.mHasTitleClick) {
            this.mTitle.setCompoundDrawablesWithIntrinsicBounds((Drawable) null, (Drawable) null, getResources().getDrawable(z ? R.drawable.keyguard_bottom_guide_right_arrow_dark : R.drawable.keyguard_bottom_guide_right_arrow), (Drawable) null);
        } else {
            this.mTitle.setCompoundDrawablesWithIntrinsicBounds((Drawable) null, (Drawable) null, (Drawable) null, (Drawable) null);
        }
    }

    public void updateTitlePadding() {
        this.mTitle.setPaddingRelative(!this.mIsLeftTopClock && this.mHasTitleClick ? this.mContext.getResources().getDimensionPixelOffset(R.dimen.lock_screen_magazine_clock_title_padding_start) : 0, 0, 0, 0);
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
