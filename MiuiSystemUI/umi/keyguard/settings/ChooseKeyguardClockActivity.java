package com.android.keyguard.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.MiuiKeyguardUtils;
import com.android.keyguard.clock.MiuiClockView;
import com.android.keyguard.wallpaper.KeyguardWallpaperUtils;
import com.android.keyguard.wallpaper.WallpaperAuthorityUtils;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtilsHelper;
import com.android.systemui.plugins.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import java.io.File;
import java.util.Locale;
import miui.R$drawable;
import miui.animation.Folme;
import miui.animation.base.AnimConfig;
import miui.widget.SlidingButton;
import miuix.recyclerview.widget.RecyclerView;

public class ChooseKeyguardClockActivity extends Activity {
    /* access modifiers changed from: private */
    public ImageView mBackImage;
    /* access modifiers changed from: private */
    public boolean mBackImageLight = true;
    /* access modifiers changed from: private */
    public BottomSheetBehavior mBottomSheetBehavior;
    /* access modifiers changed from: private */
    public boolean mCenterClockViewLight = true;
    private RecyclerView mClockList;
    protected MiuiClockView mClockView;
    private View mHeadBottomDiver;
    /* access modifiers changed from: private */
    public InputMethodManager mInputMethodManager;
    private boolean mIsMiWallpaper = false;
    /* access modifiers changed from: private */
    public boolean mIsNightMode = false;
    private boolean mIsSuperWallpaper = false;
    private boolean mIsThemeLiveWallpaper = false;
    private boolean mIsVideo24Wallpaper = false;
    /* access modifiers changed from: private */
    public boolean mLeftClockViewLight = true;
    /* access modifiers changed from: private */
    public MediaPlayer mLiveLockWallpaperPlayer;
    private TextureView mLiveLockWallpaperView;
    /* access modifiers changed from: private */
    public LockPatternUtils mLockPatternUtils;
    /* access modifiers changed from: private */
    public SlidingButton mLunarBtn;
    private TextView mLunarCalendar;
    private FrameLayout mLunarCalendarLayout;
    /* access modifiers changed from: private */
    public RestrictedLockUtils.EnforcedAdmin mOwnerAdmin;
    /* access modifiers changed from: private */
    public AlertDialog mOwnerInfoDialog = null;
    private FrameLayout mOwnerInfoLayout;
    private TextView mOwnerInfoTitle;
    private ImageView mPanelBarImage;
    private TextView mPanelTitle;
    private LinearLayout mPanelView;
    /* access modifiers changed from: private */
    public int mSelectedClockPosition = 0;
    /* access modifiers changed from: private */
    public int mUserId;
    /* access modifiers changed from: private */
    public ImageView mWallPaper;

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.mIsNightMode = MiuiKeyguardUtils.isNightMode(this);
        Window window = getWindow();
        WindowManager.LayoutParams attributes = getWindow().getAttributes();
        attributes.layoutInDisplayCutoutMode = 1;
        window.setAttributes(attributes);
        window.clearFlags(134217728);
        window.addFlags(Integer.MIN_VALUE);
        window.setNavigationBarColor(this.mIsNightMode ? -16777216 : -1);
        if (WallpaperAuthorityUtils.isThemeLockLiveWallpaper(this)) {
            window.addFlags(1048576);
            overridePendingTransition(0, 0);
        }
        setContentView(R.layout.choose_keyguard_clock);
        this.mInputMethodManager = (InputMethodManager) getSystemService("input_method");
        this.mLockPatternUtils = new LockPatternUtils(this);
        this.mUserId = getIntent().getIntExtra("extra_user_id", UserHandle.myUserId());
        this.mOwnerAdmin = RestrictedLockUtilsHelper.getDeviceOwner(this);
        this.mSelectedClockPosition = Settings.System.getIntForUser(getContentResolver(), "selected_keyguard_clock_position", MiuiKeyguardUtils.getDefaultKeyguardClockPosition(this), this.mUserId);
        initView();
        WallpaperInfo wallpaperInfo = ((WallpaperManager) getSystemService("wallpaper")).getWallpaperInfo();
        if (wallpaperInfo != null && wallpaperInfo.getServiceInfo() != null) {
            try {
                this.mIsSuperWallpaper = wallpaperInfo.getServiceInfo().metaData.getBoolean("is_super_wallpaper");
            } catch (Exception unused) {
            }
        }
    }

    public void initView() {
        ImageView imageView = (ImageView) findViewById(R.id.back_image);
        this.mBackImage = imageView;
        imageView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                ChooseKeyguardClockActivity.this.finish();
            }
        });
        this.mWallPaper = (ImageView) findViewById(R.id.wallpaper);
        this.mLiveLockWallpaperView = (TextureView) findViewById(R.id.wallpaper_textureView);
        File lockVideo = getLockVideo();
        if (lockVideo == null || !lockVideo.getPath().endsWith(".mp4")) {
            this.mLiveLockWallpaperView.setVisibility(8);
        } else {
            showMiLiveLockWallpaper(getLockVideo());
        }
        processWallpaperView();
        if (WallpaperAuthorityUtils.isThemeLockLiveWallpaper(this)) {
            this.mWallPaper.setVisibility(8);
            this.mLiveLockWallpaperView.setVisibility(8);
        }
        MiuiClockView miuiClockView = (MiuiClockView) findViewById(R.id.main_clock_view);
        this.mClockView = miuiClockView;
        miuiClockView.setClockStyle(this.mSelectedClockPosition);
        this.mClockView.setOwnerInfo(getOwnerInfo());
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.choose_clock_scroll_view);
        this.mPanelView = linearLayout;
        BottomSheetBehavior from = BottomSheetBehavior.from(linearLayout);
        this.mBottomSheetBehavior = from;
        from.setState(3);
        if (MiuiKeyguardUtils.isDefaultLockScreenTheme()) {
            ((FrameLayout) findViewById(R.id.third_theme_hint_layout)).setVisibility(8);
        } else {
            this.mPanelView.setVisibility(8);
        }
        this.mClockList = (RecyclerView) findViewById(R.id.clock_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(0);
        this.mClockList.setLayoutManager(linearLayoutManager);
        this.mClockList.setAdapter(new ClockAdapter(this));
        this.mPanelBarImage = (ImageView) findViewById(R.id.panel_bar_image);
        this.mPanelTitle = (TextView) findViewById(R.id.choose_clock_panel_title);
        this.mHeadBottomDiver = findViewById(R.id.head_bottom_divider);
        this.mLunarCalendar = (TextView) findViewById(R.id.lunar_calendar_title);
        this.mLunarCalendarLayout = (FrameLayout) findViewById(R.id.lunar_calendar_layout);
        this.mLunarBtn = (SlidingButton) findViewById(R.id.lunar_calendar_button);
        if (!Locale.CHINESE.getLanguage().equals(Locale.getDefault().getLanguage())) {
            this.mLunarCalendarLayout.setVisibility(8);
            this.mClockView.setShowLunarCalendar(0);
        } else {
            boolean z = true;
            if (Settings.System.getIntForUser(getContentResolver(), "show_lunar_calendar", 0, this.mUserId) != 1) {
                z = false;
            }
            this.mLunarBtn.setChecked(z);
            this.mClockView.setShowLunarCalendar(z ? 1 : 0);
        }
        this.mLunarCalendarLayout.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                ChooseKeyguardClockActivity.this.onLunarCalendarCheckedChanged(!ChooseKeyguardClockActivity.this.mLunarBtn.isChecked());
            }
        });
        this.mLunarBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                boolean z2 = false;
                if (Settings.System.getIntForUser(ChooseKeyguardClockActivity.this.getContentResolver(), "show_lunar_calendar", 0, ChooseKeyguardClockActivity.this.mUserId) == 1) {
                    z2 = true;
                }
                if (z != z2) {
                    ChooseKeyguardClockActivity.this.onLunarCalendarCheckedChanged(z);
                }
            }
        });
        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.owner_info_layout);
        this.mOwnerInfoLayout = frameLayout;
        frameLayout.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (ChooseKeyguardClockActivity.this.mOwnerAdmin != null) {
                    ChooseKeyguardClockActivity chooseKeyguardClockActivity = ChooseKeyguardClockActivity.this;
                    RestrictedLockUtils.sendShowAdminSupportDetailsIntent(chooseKeyguardClockActivity, chooseKeyguardClockActivity.mOwnerAdmin);
                    return;
                }
                ChooseKeyguardClockActivity chooseKeyguardClockActivity2 = ChooseKeyguardClockActivity.this;
                AlertDialog unused = chooseKeyguardClockActivity2.mOwnerInfoDialog = chooseKeyguardClockActivity2.showOwnerInfoDialog();
            }
        });
        this.mOwnerInfoTitle = (TextView) findViewById(R.id.owner_info_title);
        ImageView imageView2 = (ImageView) findViewById(R.id.owner_info_restricted_icon);
        ImageView imageView3 = (ImageView) findViewById(R.id.owner_info_arrow_right);
        if (this.mOwnerAdmin != null) {
            imageView2.setVisibility(0);
            imageView3.setVisibility(8);
        }
    }

    private void sendSuperWallpaperBroadcast(boolean z) {
        if (this.mIsSuperWallpaper) {
            Intent intent = new Intent("miui.miwallpaper.action.LOCK_SCREEN_PREVIEW");
            intent.putExtra("isPreview", z);
            sendBroadcast(intent);
        }
    }

    /* access modifiers changed from: protected */
    public void onResume() {
        super.onResume();
        sendSuperWallpaperBroadcast(true);
    }

    /* access modifiers changed from: private */
    public void onLunarCalendarCheckedChanged(boolean z) {
        this.mLunarBtn.setChecked(z);
        this.mClockView.setShowLunarCalendar(z ? 1 : 0);
        Settings.System.putIntForUser(getContentResolver(), "show_lunar_calendar", z, this.mUserId);
    }

    /* access modifiers changed from: protected */
    public void onPause() {
        super.onPause();
        AlertDialog alertDialog = this.mOwnerInfoDialog;
        if (alertDialog != null) {
            alertDialog.dismiss();
            this.mOwnerInfoDialog = null;
        }
        sendSuperWallpaperBroadcast(false);
    }

    /* access modifiers changed from: protected */
    public void onStop() {
        super.onStop();
        finish();
    }

    /* access modifiers changed from: protected */
    public void onDestroy() {
        super.onDestroy();
        releaseLiveWallpaper();
    }

    public void finish() {
        super.finish();
        if (WallpaperAuthorityUtils.isThemeLockLiveWallpaper(this)) {
            overridePendingTransition(0, 0);
        }
    }

    /* access modifiers changed from: private */
    public AlertDialog showOwnerInfoDialog() {
        this.mBottomSheetBehavior.setState(4);
        View inflate = LayoutInflater.from(this).inflate(R.layout.owner_info_dialog, (ViewGroup) null, false);
        final SlidingButton slidingButton = (SlidingButton) inflate.findViewById(R.id.owner_info_button);
        slidingButton.setChecked(this.mLockPatternUtils.isOwnerInfoEnabled(this.mUserId));
        final EditText editText = (EditText) inflate.findViewById(R.id.owner_info_edit_text);
        editText.setText(getOwnerInfo());
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setIconAttribute(16843605);
        builder.setTitle(R.string.lock_screen_signature_title);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                ChooseKeyguardClockActivity.this.mBottomSheetBehavior.setState(3);
                ChooseKeyguardClockActivity.this.mInputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                ChooseKeyguardClockActivity.this.mLockPatternUtils.setOwnerInfo(editText.getText().toString(), ChooseKeyguardClockActivity.this.mUserId);
                ChooseKeyguardClockActivity.this.mLockPatternUtils.setOwnerInfoEnabled(slidingButton.isChecked(), ChooseKeyguardClockActivity.this.mUserId);
                ChooseKeyguardClockActivity.this.sendBroadcast(new Intent("owner_info_changed"));
                ChooseKeyguardClockActivity chooseKeyguardClockActivity = ChooseKeyguardClockActivity.this;
                chooseKeyguardClockActivity.mClockView.setOwnerInfo(chooseKeyguardClockActivity.getOwnerInfo());
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                ChooseKeyguardClockActivity.this.mBottomSheetBehavior.setState(3);
                ChooseKeyguardClockActivity.this.mInputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                dialogInterface.dismiss();
            }
        });
        AlertDialog create = builder.create();
        create.setView(inflate);
        create.show();
        return create;
    }

    /* access modifiers changed from: private */
    public String getOwnerInfo() {
        if (this.mLockPatternUtils.isDeviceOwnerInfoEnabled()) {
            return this.mLockPatternUtils.getDeviceOwnerInfo();
        }
        if (this.mLockPatternUtils.isOwnerInfoEnabled(this.mUserId)) {
            return this.mLockPatternUtils.getOwnerInfo(this.mUserId);
        }
        return null;
    }

    private void processWallpaperView() {
        new AsyncTask<Void, Void, Drawable>() {
            /* access modifiers changed from: protected */
            public Drawable doInBackground(Void... voidArr) {
                Drawable lockWallpaperPreview = KeyguardWallpaperUtils.getLockWallpaperPreview(ChooseKeyguardClockActivity.this);
                try {
                    Bitmap bitmap = ((BitmapDrawable) lockWallpaperPreview).getBitmap();
                    boolean z = true;
                    Bitmap createScaledBitmap = Bitmap.createScaledBitmap(bitmap, 1080, (bitmap.getHeight() * 1080) / bitmap.getWidth(), true);
                    Bitmap createBitmap = Bitmap.createBitmap(createScaledBitmap, 80, 125, R.styleable.AppCompatTheme_textColorAlertDialogListItem, R.styleable.AppCompatTheme_textColorAlertDialogListItem);
                    if (createBitmap != null) {
                        boolean unused = ChooseKeyguardClockActivity.this.mBackImageLight = MiuiKeyguardUtils.getBitmapColorMode(createBitmap) != 0;
                        createBitmap.recycle();
                    }
                    Bitmap createBitmap2 = Bitmap.createBitmap(createScaledBitmap, 56, 150, 500, 500);
                    if (createBitmap2 != null) {
                        boolean unused2 = ChooseKeyguardClockActivity.this.mLeftClockViewLight = MiuiKeyguardUtils.getBitmapColorMode(createBitmap2) == 0;
                        createBitmap2.recycle();
                    }
                    Bitmap createBitmap3 = Bitmap.createBitmap(createScaledBitmap, 56, 150, 500, 500);
                    if (createBitmap3 != null) {
                        ChooseKeyguardClockActivity chooseKeyguardClockActivity = ChooseKeyguardClockActivity.this;
                        if (MiuiKeyguardUtils.getBitmapColorMode(createBitmap3) != 0) {
                            z = false;
                        }
                        boolean unused3 = chooseKeyguardClockActivity.mCenterClockViewLight = z;
                        createBitmap3.recycle();
                    }
                } catch (Exception e) {
                    Log.e("ChooseKeyguardClockActivity", "create bitmap exception: ", e);
                }
                return lockWallpaperPreview;
            }

            /* access modifiers changed from: protected */
            public void onPostExecute(Drawable drawable) {
                int i;
                ChooseKeyguardClockActivity.this.mWallPaper.setImageDrawable(drawable);
                ImageView access$1400 = ChooseKeyguardClockActivity.this.mBackImage;
                if (ChooseKeyguardClockActivity.this.mBackImageLight) {
                    i = R$drawable.action_bar_back_light;
                } else {
                    i = R$drawable.action_bar_back_dark;
                }
                access$1400.setImageResource(i);
                ChooseKeyguardClockActivity.this.setMainClockTextColor();
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
    }

    /* access modifiers changed from: private */
    public void setMainClockTextColor() {
        int i = this.mSelectedClockPosition;
        if (i == 2 || i == 4) {
            this.mClockView.setTextColorDark(!this.mLeftClockViewLight);
        } else {
            this.mClockView.setTextColorDark(!this.mCenterClockViewLight);
        }
    }

    private File getLockVideo() {
        if (WallpaperAuthorityUtils.isHomeDefaultWallpaper(this)) {
            return new File("/system/media/lockscreen/video/video_wallpaper.mp4");
        }
        if (WallpaperAuthorityUtils.isThemeLockVideoWallpaper(this) || (this.mIsThemeLiveWallpaper && !this.mIsVideo24Wallpaper && !this.mIsMiWallpaper)) {
            return new File("/data/system/theme_magic/video/video_wallpaper.mp4");
        }
        return null;
    }

    private void releaseLiveWallpaper() {
        final MediaPlayer mediaPlayer = this.mLiveLockWallpaperPlayer;
        if (mediaPlayer != null) {
            this.mLiveLockWallpaperPlayer = null;
            AsyncTask.execute(new Runnable() {
                public void run() {
                    mediaPlayer.release();
                }
            });
        }
    }

    private void showMiLiveLockWallpaper(File file) {
        this.mLiveLockWallpaperPlayer = MediaPlayer.create(this, Uri.fromFile(file));
        this.mLiveLockWallpaperView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                return false;
            }

            public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i2) {
            }

            public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
            }

            public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i2) {
                try {
                    ChooseKeyguardClockActivity.this.mLiveLockWallpaperPlayer.setSurface(new Surface(surfaceTexture));
                    ChooseKeyguardClockActivity.this.startLiveLockWallpaper();
                } catch (Exception e) {
                    Log.e("ChooseKeyguardClockActivity", "show live wallpaper fail:", e);
                }
            }
        });
    }

    /* access modifiers changed from: private */
    public void startLiveLockWallpaper() {
        MediaPlayer mediaPlayer = this.mLiveLockWallpaperPlayer;
        if (mediaPlayer != null) {
            try {
                mediaPlayer.start();
            } catch (Exception e) {
                Log.e("ChooseKeyguardClockActivity", e.getMessage(), e);
            }
        }
    }

    public class ClockAdapter extends RecyclerView.Adapter<MyViewHolder> {
        Context context;

        public int getItemCount() {
            return 4;
        }

        public ClockAdapter(Context context2) {
            this.context = context2;
        }

        public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View inflate = LayoutInflater.from(this.context).inflate(R.layout.choose_keyguard_clock_item, viewGroup, false);
            Folme.useAt(inflate).touch().handleTouchOf(inflate, new AnimConfig[0]);
            return new MyViewHolder(this, inflate);
        }

        public void onBindViewHolder(MyViewHolder myViewHolder, final int i) {
            myViewHolder.clockView.setClockStyle(i + 1);
            myViewHolder.itemRootView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    int unused = ChooseKeyguardClockActivity.this.mSelectedClockPosition = i + 1;
                    ChooseKeyguardClockActivity.this.mClockView.setClockStyle(i + 1);
                    Settings.System.putIntForUser(ClockAdapter.this.context.getContentResolver(), "selected_keyguard_clock_position", ChooseKeyguardClockActivity.this.mSelectedClockPosition, ChooseKeyguardClockActivity.this.mUserId);
                    ClockAdapter.this.notifyDataSetChanged();
                    ChooseKeyguardClockActivity.this.setMainClockTextColor();
                }
            });
            if (i == Math.max(ChooseKeyguardClockActivity.this.mSelectedClockPosition - 1, 0)) {
                myViewHolder.itemRootView.setSelected(true);
            } else {
                myViewHolder.itemRootView.setSelected(false);
            }
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            MiuiClockView clockView;
            FrameLayout itemRootView;

            public MyViewHolder(ClockAdapter clockAdapter, View view) {
                super(view);
                MiuiClockView miuiClockView = (MiuiClockView) view.findViewById(R.id.clock_item);
                this.clockView = miuiClockView;
                miuiClockView.setScaleRatio(0.26f);
                this.clockView.setTextColorDark(!ChooseKeyguardClockActivity.this.mIsNightMode);
                this.clockView.setHasTopMargin(false);
                this.clockView.setShowLunarCalendar(0);
                this.clockView.setAutoDualClock(false);
                this.itemRootView = (FrameLayout) view.findViewById(R.id.item_root_view);
            }
        }
    }
}
