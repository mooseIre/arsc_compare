package com.android.keyguard.settings;

import android.app.Activity;
import android.app.AlertDialog;
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
import android.os.IBinder;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.appcompat.R$styleable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.clock.MiuiClockView;
import com.android.keyguard.settings.ChooseKeyguardClockActivity;
import com.android.keyguard.utils.MiuiKeyguardUtils;
import com.android.keyguard.wallpaper.IMiuiKeyguardWallpaperController;
import com.android.keyguard.wallpaper.KeyguardWallpaperUtils;
import com.android.keyguard.wallpaper.WallpaperAuthorityUtils;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtilsInternal;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0017R$layout;
import com.android.systemui.C0021R$string;
import com.android.systemui.Dependency;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import java.io.File;
import java.util.Locale;
import miui.R;
import miui.widget.SlidingButton;
import miuix.animation.Folme;
import miuix.animation.base.AnimConfig;
import miuix.recyclerview.widget.RecyclerView;

public class ChooseKeyguardClockActivity extends Activity {
    private ImageView mBackImage;
    private boolean mBackImageLight = true;
    private BottomSheetBehavior mBottomSheetBehavior;
    private boolean mCenterClockViewLight = true;
    private RecyclerView mClockList;
    protected MiuiClockView mClockView;
    private InputMethodManager mInputMethodManager;
    private boolean mIsMiWallpaper = false;
    private boolean mIsNightMode = false;
    private boolean mIsThemeLiveWallpaper = false;
    private boolean mLeftClockViewLight = true;
    private MediaPlayer mLiveLockWallpaperPlayer;
    private TextureView mLiveLockWallpaperView;
    private LockPatternUtils mLockPatternUtils;
    private SlidingButton mLunarBtn;
    private FrameLayout mLunarCalendarLayout;
    private RestrictedLockUtils.EnforcedAdmin mOwnerAdmin;
    private AlertDialog mOwnerInfoDialog = null;
    private FrameLayout mOwnerInfoLayout;
    private LinearLayout mPanelView;
    private View mRootView;
    private int mRootViewInitHeight;
    private int mSelectedClockStyle = 0;
    private int[] mStyles = {4, 1, 2, 3};
    private int mUserId;
    private ImageView mWallPaper;

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
        if (WallpaperAuthorityUtils.isThemeLockLiveWallpaper()) {
            window.addFlags(1048576);
            overridePendingTransition(0, 0);
        }
        setContentView(C0017R$layout.choose_keyguard_clock);
        this.mInputMethodManager = (InputMethodManager) getSystemService("input_method");
        this.mLockPatternUtils = new LockPatternUtils(this);
        this.mUserId = getIntent().getIntExtra("extra_user_id", UserHandle.myUserId());
        this.mOwnerAdmin = RestrictedLockUtilsInternal.getDeviceOwner(this);
        this.mSelectedClockStyle = Settings.System.getIntForUser(getContentResolver(), "selected_keyguard_clock_position", getClockStyleByConfiguration(MiuiKeyguardUtils.getDefaultKeyguardClockPosition(this)), this.mUserId);
        initView();
    }

    public void initView() {
        ImageView imageView = (ImageView) findViewById(C0015R$id.back_image);
        this.mBackImage = imageView;
        imageView.setOnClickListener(new View.OnClickListener() {
            /* class com.android.keyguard.settings.ChooseKeyguardClockActivity.AnonymousClass1 */

            public void onClick(View view) {
                ChooseKeyguardClockActivity.this.finish();
            }
        });
        this.mWallPaper = (ImageView) findViewById(C0015R$id.wallpaper);
        this.mLiveLockWallpaperView = (TextureView) findViewById(C0015R$id.wallpaper_textureView);
        File lockVideo = getLockVideo();
        if (lockVideo == null || !lockVideo.getPath().endsWith(".mp4")) {
            this.mLiveLockWallpaperView.setVisibility(8);
        } else {
            showMiLiveLockWallpaper(lockVideo);
        }
        processWallpaperView();
        if (WallpaperAuthorityUtils.isThemeLockLiveWallpaper()) {
            this.mWallPaper.setVisibility(8);
            this.mLiveLockWallpaperView.setVisibility(8);
        }
        MiuiClockView miuiClockView = (MiuiClockView) findViewById(C0015R$id.main_clock_view);
        this.mClockView = miuiClockView;
        miuiClockView.setClockStyle(this.mSelectedClockStyle);
        this.mClockView.setOwnerInfo(getOwnerInfo());
        this.mRootView = findViewById(C0015R$id.root_view);
        LinearLayout linearLayout = (LinearLayout) findViewById(C0015R$id.choose_clock_scroll_view);
        this.mPanelView = linearLayout;
        BottomSheetBehavior from = BottomSheetBehavior.from(linearLayout);
        this.mBottomSheetBehavior = from;
        from.setState(3);
        if (MiuiKeyguardUtils.isDefaultLockScreenTheme()) {
            ((FrameLayout) findViewById(C0015R$id.third_theme_hint_layout)).setVisibility(8);
        } else {
            this.mPanelView.setVisibility(8);
        }
        this.mClockList = (RecyclerView) findViewById(C0015R$id.clock_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(0);
        this.mClockList.setLayoutManager(linearLayoutManager);
        this.mClockList.setAdapter(new ClockAdapter(this));
        this.mLunarCalendarLayout = (FrameLayout) findViewById(C0015R$id.lunar_calendar_layout);
        this.mLunarBtn = findViewById(C0015R$id.lunar_calendar_button);
        if (!Locale.CHINESE.getLanguage().equals(Locale.getDefault().getLanguage())) {
            this.mLunarCalendarLayout.setVisibility(8);
            this.mClockView.setShowLunarCalendar(0);
        } else {
            boolean z = true;
            if (Settings.System.getIntForUser(getContentResolver(), "show_lunar_calendar", 0, this.mUserId) != 1) {
                z = false;
            }
            this.mLunarBtn.setChecked(z);
            MiuiClockView miuiClockView2 = this.mClockView;
            int i = z ? 1 : 0;
            int i2 = z ? 1 : 0;
            int i3 = z ? 1 : 0;
            miuiClockView2.setShowLunarCalendar(i);
        }
        this.mLunarCalendarLayout.setOnClickListener(new View.OnClickListener() {
            /* class com.android.keyguard.settings.ChooseKeyguardClockActivity.AnonymousClass2 */

            public void onClick(View view) {
                ChooseKeyguardClockActivity.this.onLunarCalendarCheckedChanged(!ChooseKeyguardClockActivity.this.mLunarBtn.isChecked());
            }
        });
        this.mLunarBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            /* class com.android.keyguard.settings.ChooseKeyguardClockActivity.AnonymousClass3 */

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
        FrameLayout frameLayout = (FrameLayout) findViewById(C0015R$id.owner_info_layout);
        this.mOwnerInfoLayout = frameLayout;
        frameLayout.setOnClickListener(new View.OnClickListener() {
            /* class com.android.keyguard.settings.ChooseKeyguardClockActivity.AnonymousClass4 */

            public void onClick(View view) {
                if (ChooseKeyguardClockActivity.this.mOwnerAdmin != null) {
                    ChooseKeyguardClockActivity chooseKeyguardClockActivity = ChooseKeyguardClockActivity.this;
                    RestrictedLockUtils.sendShowAdminSupportDetailsIntent(chooseKeyguardClockActivity, chooseKeyguardClockActivity.mOwnerAdmin);
                    return;
                }
                ChooseKeyguardClockActivity chooseKeyguardClockActivity2 = ChooseKeyguardClockActivity.this;
                chooseKeyguardClockActivity2.mOwnerInfoDialog = chooseKeyguardClockActivity2.showOwnerInfoDialog();
            }
        });
        ImageView imageView2 = (ImageView) findViewById(C0015R$id.owner_info_restricted_icon);
        ImageView imageView3 = (ImageView) findViewById(C0015R$id.owner_info_arrow_right);
        if (this.mOwnerAdmin != null) {
            imageView2.setVisibility(0);
            imageView3.setVisibility(8);
        }
    }

    private void sendSuperWallpaperBroadcast(boolean z) {
        if (((IMiuiKeyguardWallpaperController) Dependency.get(IMiuiKeyguardWallpaperController.class)).isSuperWallpaper()) {
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

    public void onWindowFocusChanged(boolean z) {
        if (this.mRootViewInitHeight == 0) {
            this.mRootViewInitHeight = this.mRootView.getMeasuredHeight();
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for r3v0, resolved type: boolean */
    /* JADX WARN: Multi-variable type inference failed */
    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onLunarCalendarCheckedChanged(boolean z) {
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
        if (WallpaperAuthorityUtils.isThemeLockLiveWallpaper()) {
            overridePendingTransition(0, 0);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private AlertDialog showOwnerInfoDialog() {
        this.mBottomSheetBehavior.setState(4);
        View inflate = LayoutInflater.from(this).inflate(C0017R$layout.owner_info_dialog, (ViewGroup) null, false);
        final SlidingButton findViewById = inflate.findViewById(C0015R$id.owner_info_button);
        findViewById.setChecked(this.mLockPatternUtils.isOwnerInfoEnabled(this.mUserId));
        final EditText editText = (EditText) inflate.findViewById(C0015R$id.owner_info_edit_text);
        editText.setText(getOwnerInfo());
        ((LinearLayout) inflate.findViewById(C0015R$id.owner_info_container)).setOnClickListener(new View.OnClickListener(this) {
            /* class com.android.keyguard.settings.ChooseKeyguardClockActivity.AnonymousClass5 */

            public void onClick(View view) {
                SlidingButton slidingButton = findViewById;
                slidingButton.setChecked(!slidingButton.isChecked());
            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle(C0021R$string.lock_screen_signature_title);
        builder.setPositiveButton(C0021R$string.ok, new DialogInterface.OnClickListener() {
            /* class com.android.keyguard.settings.ChooseKeyguardClockActivity.AnonymousClass6 */

            public void onClick(DialogInterface dialogInterface, int i) {
                ChooseKeyguardClockActivity.this.expandBottomSheet(editText.getWindowToken());
                ChooseKeyguardClockActivity.this.mLockPatternUtils.setOwnerInfo(editText.getText().toString(), ChooseKeyguardClockActivity.this.mUserId);
                ChooseKeyguardClockActivity.this.mLockPatternUtils.setOwnerInfoEnabled(findViewById.isChecked(), ChooseKeyguardClockActivity.this.mUserId);
                ChooseKeyguardClockActivity.this.sendBroadcast(new Intent("owner_info_changed"));
                ChooseKeyguardClockActivity chooseKeyguardClockActivity = ChooseKeyguardClockActivity.this;
                chooseKeyguardClockActivity.mClockView.setOwnerInfo(chooseKeyguardClockActivity.getOwnerInfo());
            }
        });
        builder.setNegativeButton(C0021R$string.cancel, new DialogInterface.OnClickListener() {
            /* class com.android.keyguard.settings.ChooseKeyguardClockActivity.AnonymousClass7 */

            public void onClick(DialogInterface dialogInterface, int i) {
                ChooseKeyguardClockActivity.this.expandBottomSheet(editText.getWindowToken());
                dialogInterface.dismiss();
            }
        });
        AlertDialog create = builder.create();
        create.setView(inflate);
        create.show();
        return create;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void expandBottomSheet(IBinder iBinder) {
        if (this.mRootView.getMeasuredHeight() < this.mRootViewInitHeight) {
            this.mInputMethodManager.hideSoftInputFromWindow(iBinder, 0);
            this.mRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                /* class com.android.keyguard.settings.ChooseKeyguardClockActivity.AnonymousClass8 */

                public void onGlobalLayout() {
                    if (ChooseKeyguardClockActivity.this.mRootViewInitHeight == ChooseKeyguardClockActivity.this.mRootView.getMeasuredHeight()) {
                        ChooseKeyguardClockActivity.this.mBottomSheetBehavior.setState(3);
                        ChooseKeyguardClockActivity.this.mRootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                }
            });
            return;
        }
        this.mBottomSheetBehavior.setState(3);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String getOwnerInfo() {
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
            /* class com.android.keyguard.settings.ChooseKeyguardClockActivity.AnonymousClass9 */

            /* access modifiers changed from: protected */
            public Drawable doInBackground(Void... voidArr) {
                Drawable lockWallpaperPreview = KeyguardWallpaperUtils.getLockWallpaperPreview(ChooseKeyguardClockActivity.this);
                try {
                    Bitmap bitmap = ((BitmapDrawable) lockWallpaperPreview).getBitmap();
                    boolean z = true;
                    Bitmap createScaledBitmap = Bitmap.createScaledBitmap(bitmap, 1080, (bitmap.getHeight() * 1080) / bitmap.getWidth(), true);
                    Bitmap createBitmap = Bitmap.createBitmap(createScaledBitmap, 80, (int) R$styleable.AppCompatTheme_windowMinWidthMinor, (int) androidx.constraintlayout.widget.R$styleable.Constraint_visibilityMode, (int) androidx.constraintlayout.widget.R$styleable.Constraint_visibilityMode);
                    if (createBitmap != null) {
                        ChooseKeyguardClockActivity.this.mBackImageLight = MiuiKeyguardUtils.getBitmapColorMode(createBitmap) != 0;
                        createBitmap.recycle();
                    }
                    Bitmap createBitmap2 = Bitmap.createBitmap(createScaledBitmap, 56, 150, 500, 500);
                    if (createBitmap2 != null) {
                        ChooseKeyguardClockActivity.this.mLeftClockViewLight = MiuiKeyguardUtils.getBitmapColorMode(createBitmap2) == 0;
                        createBitmap2.recycle();
                    }
                    Bitmap createBitmap3 = Bitmap.createBitmap(createScaledBitmap, 56, 150, 500, 500);
                    if (createBitmap3 != null) {
                        ChooseKeyguardClockActivity chooseKeyguardClockActivity = ChooseKeyguardClockActivity.this;
                        if (MiuiKeyguardUtils.getBitmapColorMode(createBitmap3) != 0) {
                            z = false;
                        }
                        chooseKeyguardClockActivity.mCenterClockViewLight = z;
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
                ImageView imageView = ChooseKeyguardClockActivity.this.mBackImage;
                if (ChooseKeyguardClockActivity.this.mBackImageLight) {
                    i = R.drawable.action_bar_back_light;
                } else {
                    i = R.drawable.action_bar_back_dark;
                }
                imageView.setImageResource(i);
                ChooseKeyguardClockActivity.this.setMainClockTextColor();
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setMainClockTextColor() {
        int i = this.mSelectedClockStyle;
        if (i == 2 || i == 4) {
            this.mClockView.setTextColorDark(!this.mLeftClockViewLight);
        } else {
            this.mClockView.setTextColorDark(!this.mCenterClockViewLight);
        }
    }

    private File getLockVideo() {
        if (WallpaperAuthorityUtils.isHomeDefaultWallpaper()) {
            return new File("/system/media/lockscreen/video/video_wallpaper.mp4");
        }
        if (WallpaperAuthorityUtils.isThemeLockVideoWallpaper() || (this.mIsThemeLiveWallpaper && !this.mIsMiWallpaper)) {
            return new File("/data/system/theme_magic/video/video_wallpaper.mp4");
        }
        return null;
    }

    private void releaseLiveWallpaper() {
        final MediaPlayer mediaPlayer = this.mLiveLockWallpaperPlayer;
        if (mediaPlayer != null) {
            this.mLiveLockWallpaperPlayer = null;
            AsyncTask.execute(new Runnable(this) {
                /* class com.android.keyguard.settings.ChooseKeyguardClockActivity.AnonymousClass10 */

                public void run() {
                    mediaPlayer.release();
                }
            });
        }
    }

    private void showMiLiveLockWallpaper(File file) {
        Uri fromFile = Uri.fromFile(file);
        Log.d("ChooseKeyguardClockActivity", "showMiLiveLockWallpaper wallpaper==" + fromFile);
        this.mLiveLockWallpaperPlayer = MediaPlayer.create(this, fromFile);
        this.mLiveLockWallpaperView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            /* class com.android.keyguard.settings.ChooseKeyguardClockActivity.AnonymousClass11 */

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
    /* access modifiers changed from: public */
    private void startLiveLockWallpaper() {
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

        public ClockAdapter(Context context2) {
            this.context = context2;
        }

        @Override // androidx.recyclerview.widget.RecyclerView.Adapter
        public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View inflate = LayoutInflater.from(this.context).inflate(C0017R$layout.choose_keyguard_clock_item, viewGroup, false);
            Folme.useAt(inflate).touch().handleTouchOf(inflate, new AnimConfig[0]);
            return new MyViewHolder(this, inflate);
        }

        public void onBindViewHolder(MyViewHolder myViewHolder, int i) {
            myViewHolder.clockView.setClockStyle(ChooseKeyguardClockActivity.this.mStyles[i]);
            myViewHolder.itemRootView.setOnClickListener(new View.OnClickListener(i) {
                /* class com.android.keyguard.settings.$$Lambda$ChooseKeyguardClockActivity$ClockAdapter$Dk9YcnfNZXXLHycm7_wh3QV3S3U */
                public final /* synthetic */ int f$1;

                {
                    this.f$1 = r2;
                }

                public final void onClick(View view) {
                    ChooseKeyguardClockActivity.ClockAdapter.this.lambda$onBindViewHolder$0$ChooseKeyguardClockActivity$ClockAdapter(this.f$1, view);
                }
            });
            if (ChooseKeyguardClockActivity.this.mSelectedClockStyle == ChooseKeyguardClockActivity.this.mStyles[i]) {
                myViewHolder.itemRootView.setSelected(true);
            } else {
                myViewHolder.itemRootView.setSelected(false);
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onBindViewHolder$0 */
        public /* synthetic */ void lambda$onBindViewHolder$0$ChooseKeyguardClockActivity$ClockAdapter(int i, View view) {
            ChooseKeyguardClockActivity chooseKeyguardClockActivity = ChooseKeyguardClockActivity.this;
            chooseKeyguardClockActivity.mSelectedClockStyle = chooseKeyguardClockActivity.mStyles[i];
            ChooseKeyguardClockActivity chooseKeyguardClockActivity2 = ChooseKeyguardClockActivity.this;
            chooseKeyguardClockActivity2.mClockView.setClockStyle(chooseKeyguardClockActivity2.mSelectedClockStyle);
            Settings.System.putIntForUser(this.context.getContentResolver(), "selected_keyguard_clock_position", ChooseKeyguardClockActivity.this.mSelectedClockStyle, ChooseKeyguardClockActivity.this.mUserId);
            notifyDataSetChanged();
            ChooseKeyguardClockActivity.this.setMainClockTextColor();
        }

        @Override // androidx.recyclerview.widget.RecyclerView.Adapter
        public int getItemCount() {
            return ChooseKeyguardClockActivity.this.mStyles.length;
        }

        /* access modifiers changed from: package-private */
        public class MyViewHolder extends RecyclerView.ViewHolder {
            MiuiClockView clockView;
            FrameLayout itemRootView;

            public MyViewHolder(ClockAdapter clockAdapter, View view) {
                super(view);
                MiuiClockView miuiClockView = (MiuiClockView) view.findViewById(C0015R$id.clock_item);
                this.clockView = miuiClockView;
                miuiClockView.setScaleRatio(0.26f);
                this.clockView.setTextColorDark(!ChooseKeyguardClockActivity.this.mIsNightMode);
                this.clockView.setHasTopMargin(false);
                this.clockView.setShowLunarCalendar(0);
                this.clockView.setAutoDualClock(false);
                this.itemRootView = (FrameLayout) view.findViewById(C0015R$id.item_root_view);
            }
        }
    }

    private int getClockStyleByConfiguration(int i) {
        if (i == 0) {
            return MiuiKeyguardUtils.isBlackGoldenTheme(this) ? 3 : 4;
        }
        return i;
    }
}
