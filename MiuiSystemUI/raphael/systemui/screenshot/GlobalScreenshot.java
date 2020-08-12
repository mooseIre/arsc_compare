package com.android.systemui.screenshot;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.ActivityOptions;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.app.StatusBarManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextCompat;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.UserManagerCompat;
import android.provider.MiuiSettings;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.IWindowManager;
import android.view.IWindowManagerCompat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.WindowManagerCompat;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import androidx.preference.PreferenceManager;
import com.android.systemui.Constants;
import com.android.systemui.SystemUICompat;
import com.android.systemui.Util;
import com.android.systemui.content.pm.PackageManagerCompat;
import com.android.systemui.miui.anim.PhysicBasedInterpolator;
import com.android.systemui.plugins.R;
import com.android.systemui.recents.misc.ForegroundThread;
import com.android.systemui.screenshot.IBitmapService;
import com.android.systemui.screenshot.IScreenShotCallback;
import com.android.systemui.util.CornerRadiusUtils;
import com.miui.enterprise.RestrictionsHelper;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import miui.os.Build;
import miui.util.HapticFeedbackUtil;
import miui.util.ScreenshotUtils;
import miui.view.animation.CubicEaseInOutInterpolator;
import miui.view.animation.CubicEaseOutInterpolator;
import miui.view.animation.SineEaseInOutInterpolator;

class GlobalScreenshot {
    private boolean isDeviceProvisioned;
    private BroadcastReceiver mBeforeScreenshotReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (!intent.getBooleanExtra("IsFinished", false)) {
                GlobalScreenshot.this.quitThumbnailWindow(false, true);
                StatHelper.recordCountEvent(GlobalScreenshot.this.mContext, "quit_thumbnail", "continue_screenshot");
            }
        }
    };
    /* access modifiers changed from: private */
    public IBitmapService mBitmapService;
    private float mBtnTranslationX;
    /* access modifiers changed from: private */
    public float mBtnTranslationY;
    private BroadcastReceiver mConfigurationReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            GlobalScreenshot.this.quitThumbnailWindow(false, true);
            StatHelper.recordCountEvent(GlobalScreenshot.this.mContext, "quit_thumbnail", "configuration_change");
        }
    };
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName componentName, final IBinder iBinder) {
            GlobalScreenshot.this.mGalleryHandler.post(new Runnable() {
                public void run() {
                    try {
                        IBitmapService unused = GlobalScreenshot.this.mBitmapService = IBitmapService.Stub.asInterface(iBinder);
                        GlobalScreenshot.this.mBitmapService.registerCallback(GlobalScreenshot.this.mScreenShotCallback);
                    } catch (RemoteException e) {
                        Log.e("GlobalScreenshot", "bitmap service register exception : " + e);
                    }
                }
            });
        }

        public void onServiceDisconnected(ComponentName componentName) {
            IBitmapService unused = GlobalScreenshot.this.mBitmapService = null;
        }
    };
    /* access modifiers changed from: private */
    public Context mContext;
    private Display mDisplay;
    private DisplayMetrics mDisplayMetrics;
    /* access modifiers changed from: private */
    public float mFirstTranslationX;
    /* access modifiers changed from: private */
    public float mFirstTranslationY;
    /* access modifiers changed from: private */
    public Handler mGalleryHandler;
    /* access modifiers changed from: private */
    public Handler mHandler = new Handler();
    private boolean mHasNavBar;
    private boolean mIsBeforeScreenshotReceiver;
    private boolean mIsConfigurationReceiver;
    /* access modifiers changed from: private */
    public boolean mIsInOutAnimating;
    /* access modifiers changed from: private */
    public boolean mIsQuited;
    /* access modifiers changed from: private */
    public boolean mIsSaved;
    /* access modifiers changed from: private */
    public boolean mIsShowLongScreenShotGuide;
    /* access modifiers changed from: private */
    public boolean mIsShowLongScreenShotGuideOverlay;
    private boolean mIsThumbnailMoving;
    /* access modifiers changed from: private */
    public LongScreenShotGuideLayout mLongScreenShotGuideLayout;
    /* access modifiers changed from: private */
    public View mLongScreenShotGuideLayoutOverlay;
    private RelativeLayout.LayoutParams mLongShotParams;
    private int mNotificationIconSize;
    private NotificationManager mNotificationManager;
    /* access modifiers changed from: private */
    public NotifyMediaStoreData mNotifyMediaStoreData;
    /* access modifiers changed from: private */
    public ViewTreeObserver.OnComputeInternalInsetsListener mOnComputeInternalInsetsListener = new ViewTreeObserver.OnComputeInternalInsetsListener() {
        public void onComputeInternalInsets(ViewTreeObserver.InternalInsetsInfo internalInsetsInfo) {
            internalInsetsInfo.setTouchableInsets(3);
            int access$900 = (int) GlobalScreenshot.this.mThumbnailMarginLeft;
            int access$1000 = GlobalScreenshot.this.mThumbnailTop;
            internalInsetsInfo.touchableRegion.set(new Rect(access$900, access$1000, GlobalScreenshot.this.mThumbnailTotalWidth + access$900, GlobalScreenshot.this.mThumbnailTotalHeight + access$1000));
        }
    };
    /* access modifiers changed from: private */
    public final boolean mOrientationLandscape;
    private float mPhoneTopRadius;
    /* access modifiers changed from: private */
    public Runnable mQuitThumbnailRunnable = new Runnable() {
        public void run() {
            GlobalScreenshot.this.quitThumbnailWindow(true, true);
            StatHelper.recordCountEvent(GlobalScreenshot.this.mContext, "quit_thumbnail", "timeout");
        }
    };
    private float mRadius;
    private Runnable mRemoveLongScreenShotGuideOverlayRunnable = new Runnable() {
        public void run() {
            if (GlobalScreenshot.this.mLongScreenShotGuideLayoutOverlay != null) {
                GlobalScreenshot.this.mWindowManager.removeView(GlobalScreenshot.this.mLongScreenShotGuideLayoutOverlay);
                boolean unused = GlobalScreenshot.this.mIsShowLongScreenShotGuideOverlay = false;
            }
        }
    };
    /* access modifiers changed from: private */
    public Ringtone mRingtone;
    /* access modifiers changed from: private */
    public float mScaleValue = 1.0f;
    /* access modifiers changed from: private */
    public Bitmap mScreenBitmap;
    /* access modifiers changed from: private */
    public int mScreenHeight;
    /* access modifiers changed from: private */
    public MarqueeTextView mScreenLongShotView;
    /* access modifiers changed from: private */
    public View mScreenLongShotViewGroup;
    private BroadcastReceiver mScreenOffReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            GlobalScreenshot.this.removeLongScreenShotGuideOverlay();
            GlobalScreenshot.this.removeLongScreenShotGuide(false);
            GlobalScreenshot.this.mHandler.postDelayed(GlobalScreenshot.this.mQuitThumbnailRunnable, 3600);
        }
    };
    /* access modifiers changed from: private */
    public View mScreenShareView;
    /* access modifiers changed from: private */
    public IScreenShotCallback.Stub mScreenShotCallback = new IScreenShotCallback.Stub() {
        public void quitThumnail() throws RemoteException {
            GlobalScreenshot.this.mHandler.post(new Runnable() {
                public void run() {
                    GlobalScreenshot.this.quitThumbnailWindow(false, false);
                }
            });
        }
    };
    private Uri mScreenShotUri;
    /* access modifiers changed from: private */
    public View mScreenWhiteBg;
    /* access modifiers changed from: private */
    public int mScreenWidth;
    /* access modifiers changed from: private */
    public ObjectAnimator mScreenshotAnimation;
    /* access modifiers changed from: private */
    public GlobalScreenshotDisplay mScreenshotDisplay;
    /* access modifiers changed from: private */
    public View mScreenshotLayout;
    /* access modifiers changed from: private */
    public View mScreenshotShadow;
    private float mScreenshotTranslationX;
    /* access modifiers changed from: private */
    public float mScreenshotTranslationY;
    /* access modifiers changed from: private */
    public ImageView mScreenshotView;
    private int mShadowMargin;
    private float mShadowScale;
    private float mShadowTranslationX;
    /* access modifiers changed from: private */
    public float mShadowTranslationY;
    private RelativeLayout.LayoutParams mShareParams;
    /* access modifiers changed from: private */
    public int mThumbnailHeight;
    private int mThumbnailLeft;
    /* access modifiers changed from: private */
    public float mThumbnailMarginLeft;
    /* access modifiers changed from: private */
    public int mThumbnailRight;
    /* access modifiers changed from: private */
    public ValueAnimator mThumbnailShakeAnimator;
    /* access modifiers changed from: private */
    public int mThumbnailTop;
    /* access modifiers changed from: private */
    public int mThumbnailTotalHeight;
    /* access modifiers changed from: private */
    public int mThumbnailTotalWidth;
    /* access modifiers changed from: private */
    public int mThumbnailWidth;
    private float mThumnailScale;
    private float mTouchDownX;
    private float mTouchDownY;
    private VelocityTracker mVTracker = VelocityTracker.obtain();
    private GradientDrawable mWhiteBgDrawable;
    private WindowManager.LayoutParams mWindowLayoutParams;
    /* access modifiers changed from: private */
    public WindowManager mWindowManager;

    public interface ScreenshotFinishCallback {
        void onFinish();
    }

    public static float calcPivot(float f, float f2, float f3, float f4) {
        return f3 + (((f3 - f) * f4) / (f2 - f4));
    }

    /* access modifiers changed from: private */
    public void updateRingtone() {
        Log.d("GlobalScreenshot", "updateRingtone() Build.getRegion()=" + Build.getRegion());
        if (Build.checkRegion(Locale.KOREA.getCountry())) {
            this.mScreenShotUri = Uri.fromFile(Constants.SOUND_SCREENSHOT_KR);
        } else {
            this.mScreenShotUri = Uri.fromFile(Constants.SOUND_SCREENSHOT);
        }
        this.mRingtone = RingtoneManager.getRingtone(this.mContext, this.mScreenShotUri);
        if (this.mRingtone == null) {
            return;
        }
        if (Build.checkRegion(Locale.KOREA.getCountry())) {
            this.mRingtone.setStreamType(7);
        } else {
            this.mRingtone.setStreamType(1);
        }
    }

    public GlobalScreenshot(final Context context, Handler handler) {
        this.mGalleryHandler = handler;
        Resources resources = context.getResources();
        this.mContext = context;
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService("layout_inflater");
        this.mScreenshotDisplay = new GlobalScreenshotDisplay(context);
        this.mThumbnailLeft = this.mContext.getResources().getDimensionPixelSize(R.dimen.screenshot_thumbnail_padding_left);
        this.mThumbnailTop = this.mContext.getResources().getDimensionPixelSize(R.dimen.screenshot_thumbnail_padding_top);
        this.mThumbnailRight = this.mContext.getResources().getDimensionPixelSize(R.dimen.screenshot_thumbnail_padding_right);
        int i = 0;
        this.mOrientationLandscape = this.mContext.getResources().getConfiguration().orientation == 2;
        this.mScreenshotLayout = layoutInflater.inflate(R.layout.global_screenshot, (ViewGroup) null);
        this.mScreenshotView = (ImageView) this.mScreenshotLayout.findViewById(R.id.global_screenshot);
        this.mScreenWhiteBg = this.mScreenshotLayout.findViewById(R.id.screen_white_bg);
        this.mScreenshotShadow = this.mScreenshotLayout.findViewById(R.id.screenshot_shadow);
        this.mScreenLongShotViewGroup = this.mScreenshotLayout.findViewById(R.id.btnLongShotViewGroup);
        this.mScreenShareView = this.mScreenshotLayout.findViewById(R.id.btnShare);
        this.mScreenLongShotView = (MarqueeTextView) this.mScreenshotLayout.findViewById(R.id.btnLongShot);
        this.mScreenshotLayout.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() != 4) {
                    return false;
                }
                if (GlobalScreenshot.this.mIsShowLongScreenShotGuide) {
                    GlobalScreenshot.this.removeLongScreenShotGuide(true);
                    GlobalScreenshot.this.mHandler.postDelayed(GlobalScreenshot.this.mQuitThumbnailRunnable, 3600);
                    return false;
                }
                GlobalScreenshot.this.quitThumbnailWindow(true, true);
                StatHelper.recordCountEvent(GlobalScreenshot.this.mContext, "quit_thumbnail", "touch_outside");
                return false;
            }
        });
        this.mScreenshotView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return GlobalScreenshot.this.onThumbnailViewTouch(motionEvent);
            }
        });
        this.isDeviceProvisioned = isDeviceProvisioned();
        if (this.isDeviceProvisioned) {
            this.mScreenLongShotView.setMarqueeEnable(true);
            this.mScreenLongShotView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    if (!GlobalScreenshot.this.mIsInOutAnimating) {
                        GlobalScreenshot.this.removeLongScreenShotGuide(true);
                        if (GlobalScreenshot.this.mOrientationLandscape) {
                            Toast.makeText(context, R.string.screenshot_failed_in_landscape_mode, 0).show();
                        } else if (GlobalScreenshot.this.mScreenLongShotView.isSelected()) {
                            GlobalScreenshot.this.enterLongScreenshot();
                            StatHelper.recordNewScreenshotEvent(GlobalScreenshot.this.mContext, "new_click_long_screenshot_button", (Map<String, String>) null);
                        }
                    }
                }
            });
            this.mScreenShareView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    GlobalScreenshot globalScreenshot = GlobalScreenshot.this;
                    globalScreenshot.jumpProcess(globalScreenshot.mContext, GlobalScreenshot.this.mNotifyMediaStoreData, "send");
                }
            });
        } else {
            this.mScreenLongShotViewGroup.setVisibility(4);
            this.mScreenShareView.setVisibility(4);
        }
        this.mWindowLayoutParams = new WindowManager.LayoutParams(-1, -1, 0, 0, 2024, 17565480, -3);
        WindowManagerCompat.setLayoutInDisplayCutoutMode(this.mWindowLayoutParams, 1);
        this.mWindowLayoutParams.setTitle("ScreenshotAnimation");
        this.mWindowManager = (WindowManager) context.getSystemService("window");
        this.mNotificationManager = (NotificationManager) context.getSystemService("notification");
        this.mDisplay = this.mWindowManager.getDefaultDisplay();
        this.mDisplayMetrics = new DisplayMetrics();
        this.mDisplay.getRealMetrics(this.mDisplayMetrics);
        DisplayMetrics displayMetrics = this.mDisplayMetrics;
        this.mScreenHeight = displayMetrics.heightPixels;
        this.mScreenWidth = displayMetrics.widthPixels;
        this.mShareParams = (RelativeLayout.LayoutParams) this.mScreenShareView.getLayoutParams();
        RelativeLayout.LayoutParams layoutParams = this.mShareParams;
        layoutParams.width = this.mScreenWidth;
        this.mScreenShareView.setLayoutParams(layoutParams);
        this.mLongShotParams = (RelativeLayout.LayoutParams) this.mScreenLongShotViewGroup.getLayoutParams();
        RelativeLayout.LayoutParams layoutParams2 = this.mLongShotParams;
        layoutParams2.width = this.mScreenWidth;
        this.mScreenLongShotViewGroup.setLayoutParams(layoutParams2);
        this.mPhoneTopRadius = (float) CornerRadiusUtils.getPhoneRadius(this.mContext);
        setWhiteBgCornerRadius(this.mPhoneTopRadius);
        if (!Build.IS_TABLET) {
            try {
                this.mHasNavBar = IWindowManagerCompat.hasNavigationBar(IWindowManager.Stub.asInterface(ServiceManager.getService("window")), ContextCompat.getDisplayId(context));
            } catch (RemoteException unused) {
            }
        }
        if (!Build.IS_TABLET && this.mHasNavBar && this.mOrientationLandscape && this.mDisplay.getRotation() == 3 && SystemProperties.getInt("ro.miui.notch", 0) == 1 && !MiuiSettings.Global.getBoolean(this.mContext.getContentResolver(), "force_black_v2")) {
            i = 0 + this.mContext.getResources().getDimensionPixelSize(R.dimen.notch_height);
        }
        RelativeLayout.LayoutParams layoutParams3 = (RelativeLayout.LayoutParams) this.mScreenshotShadow.getLayoutParams();
        layoutParams3.setMarginEnd(i);
        this.mScreenshotShadow.setLayoutParams(layoutParams3);
        this.mThumbnailShakeAnimator = ValueAnimator.ofInt(new int[]{0, 20});
        this.mThumbnailShakeAnimator.setDuration(1600);
        this.mThumbnailShakeAnimator.setInterpolator(new SineEaseInOutInterpolator());
        this.mThumbnailShakeAnimator.setRepeatCount(-1);
        this.mThumbnailShakeAnimator.setRepeatMode(2);
        this.mThumbnailShakeAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float intValue = (float) ((Integer) valueAnimator.getAnimatedValue()).intValue();
                GlobalScreenshot.this.mScreenShareView.setTranslationY(GlobalScreenshot.this.mBtnTranslationY + intValue);
                GlobalScreenshot.this.mScreenLongShotViewGroup.setTranslationY(GlobalScreenshot.this.mBtnTranslationY + intValue);
                GlobalScreenshot.this.mScreenshotView.setTranslationY(GlobalScreenshot.this.mScreenshotTranslationY + intValue);
                GlobalScreenshot.this.mScreenshotShadow.setTranslationY(GlobalScreenshot.this.mShadowTranslationY + intValue);
            }
        });
        this.mThumbnailShakeAnimator.addListener(new AnimatorListenerAdapter() {
            public void onAnimationCancel(Animator animator) {
                super.onAnimationCancel(animator);
                GlobalScreenshot.this.getTranslation();
            }

            public void onAnimationStart(Animator animator) {
                super.onAnimationStart(animator);
                GlobalScreenshot.this.getTranslation();
            }
        });
        this.mNotificationIconSize = resources.getDimensionPixelSize(R.dimen.notification_large_icon_height);
        this.mRadius = (float) this.mContext.getResources().getDimensionPixelSize(R.dimen.screenshot_thumnail_btn_radius);
    }

    /* access modifiers changed from: private */
    public void getTranslation() {
        getTranslationY();
        getTranslationX();
    }

    /* access modifiers changed from: private */
    public void getTranslationX() {
        this.mBtnTranslationX = this.mScreenShareView.getTranslationX();
        this.mScreenshotTranslationX = this.mScreenshotView.getTranslationX();
        this.mShadowTranslationX = this.mScreenshotShadow.getTranslationX();
    }

    /* access modifiers changed from: private */
    public void getTranslationY() {
        this.mBtnTranslationY = this.mScreenShareView.getTranslationY();
        this.mScreenshotTranslationY = this.mScreenshotView.getTranslationY();
        this.mShadowTranslationY = this.mScreenshotShadow.getTranslationY();
    }

    private void setWhiteBgCornerRadius(float f) {
        if (this.mWhiteBgDrawable == null) {
            this.mWhiteBgDrawable = new GradientDrawable();
            this.mWhiteBgDrawable.setColor(Color.parseColor("#ffffff"));
        }
        this.mWhiteBgDrawable.setCornerRadius(f);
        this.mScreenWhiteBg.setBackgroundDrawable(this.mWhiteBgDrawable);
    }

    public static void notifyMediaAndFinish(Context context, NotifyMediaStoreData notifyMediaStoreData) {
        notifyMediaAndFinish(context, notifyMediaStoreData, (ScreenshotFinishCallback) null);
    }

    public static void notifyMediaAndFinish(final Context context, final NotifyMediaStoreData notifyMediaStoreData, final ScreenshotFinishCallback screenshotFinishCallback) {
        if (notifyMediaStoreData != null && !notifyMediaStoreData.isRunned) {
            if (!notifyMediaStoreData.saveFinished) {
                notifyMediaStoreData.isPending = true;
                notifyMediaStoreData.finishCallback = screenshotFinishCallback;
                return;
            }
            new AsyncTask<Void, Void, Void>() {
                /* access modifiers changed from: protected */
                public Void doInBackground(Void[] voidArr) {
                    Intent intent = new Intent("com.miui.gallery.SAVE_TO_CLOUD");
                    intent.setPackage("com.miui.gallery");
                    List<ResolveInfo> queryBroadcastReceiversAsUser = PackageManagerCompat.queryBroadcastReceiversAsUser(context.getPackageManager(), intent, 0, -2);
                    if (queryBroadcastReceiversAsUser != null && queryBroadcastReceiversAsUser.size() > 0) {
                        intent.setComponent(new ComponentName("com.miui.gallery", queryBroadcastReceiversAsUser.get(0).activityInfo.name));
                    }
                    intent.putExtra("extra_file_path", notifyMediaStoreData.imageFilePath);
                    context.sendBroadcastAsUser(intent, UserHandle.CURRENT);
                    return null;
                }

                /* access modifiers changed from: protected */
                public void onPostExecute(Void voidR) {
                    super.onPostExecute(voidR);
                    Runnable runnable = notifyMediaStoreData.finisher;
                    if (runnable != null) {
                        runnable.run();
                    }
                    notifyMediaStoreData.isRunned = true;
                    ScreenshotFinishCallback screenshotFinishCallback = screenshotFinishCallback;
                    if (screenshotFinishCallback != null) {
                        screenshotFinishCallback.onFinish();
                    }
                    ScreenshotFinishCallback screenshotFinishCallback2 = notifyMediaStoreData.finishCallback;
                    if (screenshotFinishCallback2 != null) {
                        screenshotFinishCallback2.onFinish();
                    }
                }
            }.execute(new Void[0]);
        }
    }

    /* access modifiers changed from: package-private */
    public void enterLongScreenshot() {
        this.mHandler.post(new Runnable() {
            public void run() {
                GlobalScreenshot.this.quitThumbnailWindow(false, false);
                GlobalScreenshot.this.mScreenshotDisplay.show(GlobalScreenshot.this.mScreenBitmap, GlobalScreenshot.this.mNotifyMediaStoreData, (GlobalScreenshot.this.mScreenBitmap.getWidth() - GlobalScreenshot.this.mThumbnailRight) - GlobalScreenshot.this.mThumbnailWidth, GlobalScreenshot.this.mThumbnailTop, GlobalScreenshot.this.mThumbnailWidth, GlobalScreenshot.this.mThumbnailHeight, true);
                if (GlobalScreenshot.this.mIsSaved) {
                    GlobalScreenshot.this.mScreenshotDisplay.setIsScreenshotSaved();
                }
            }
        });
    }

    private void checkBindPhotoService() {
        if (this.mBitmapService == null) {
            Intent intent = new Intent();
            intent.setPackage("com.miui.gallery");
            intent.setAction("com.miui.gallery.action.SCREENSHOT");
            this.mContext.getApplicationContext().bindService(intent, this.mConnection, 1);
        }
    }

    private void unBindPhotoService() {
        IBitmapService iBitmapService = this.mBitmapService;
        if (iBitmapService != null) {
            try {
                iBitmapService.unregisterCallback(this.mScreenShotCallback);
            } catch (RemoteException e) {
                Log.e("GlobalScreenshot", "bitmap service register exception : " + e);
            }
            this.mContext.getApplicationContext().unbindService(this.mConnection);
            this.mBitmapService = null;
        }
    }

    private void dismissKeyguardIfNeed() {
        if (((KeyguardManager) this.mContext.getSystemService("keyguard")).isKeyguardLocked()) {
            SystemUICompat.dismissKeyguardOnNextActivity();
        }
    }

    /* access modifiers changed from: private */
    public void jumpProcess(final Context context, final NotifyMediaStoreData notifyMediaStoreData, final String str) {
        if (!this.mIsInOutAnimating) {
            StatusBarManager statusBarManager = (StatusBarManager) this.mContext.getSystemService("statusbar");
            if (statusBarManager != null) {
                statusBarManager.collapsePanels();
            }
            this.mHandler.removeCallbacks(this.mQuitThumbnailRunnable);
            this.mThumbnailShakeAnimator.cancel();
            dismissKeyguardIfNeed();
            notifyMediaAndFinish(context, notifyMediaStoreData, new ScreenshotFinishCallback() {
                public void onFinish() {
                    if (notifyMediaStoreData.outUri != null) {
                        Intent intent = new Intent();
                        intent.setPackage("com.miui.gallery");
                        intent.setData(notifyMediaStoreData.outUri);
                        int[] iArr = {(int) GlobalScreenshot.this.mThumbnailMarginLeft, GlobalScreenshot.this.mThumbnailTop, GlobalScreenshot.this.mThumbnailWidth, GlobalScreenshot.this.mThumbnailHeight};
                        if (TextUtils.equals(str, "send")) {
                            intent.addFlags(268468224);
                            intent.setAction(GlobalScreenshot.this.mScreenshotDisplay.checkShareAction(notifyMediaStoreData.outUri));
                            intent.putExtra("com.miui.gallery.extra.photo_enter_choice_mode", true);
                            intent.putExtra("com.miui.gallery.extra.sync_load_intent_data", true);
                            intent.putExtra("com.miui.gallery.extra.show_menu_after_choice_mode", true);
                            intent.putExtra("StartActivityWhenLocked", true);
                            intent.putExtra("ThumbnailRect", iArr);
                            intent.putExtra("is_from_send", true);
                            intent.putExtra("skip_interception", true);
                            context.startActivity(intent, GlobalScreenshot.this.createQuitAnimationBundle());
                            StatHelper.recordNewScreenshotEvent(GlobalScreenshot.this.mContext, "new_click_share_button", (Map<String, String>) null);
                        } else if (TextUtils.equals(str, "edit")) {
                            intent.addFlags(268468224);
                            intent.setAction(GlobalScreenshot.this.mScreenshotDisplay.checkEditAction(notifyMediaStoreData.outUri));
                            intent.putExtra("IsScreenshot", true);
                            intent.putExtra("IsLongScreenshot", false);
                            intent.putExtra("screenshot_filepath", notifyMediaStoreData.imageFilePath);
                            intent.putExtra("StartActivityWhenLocked", true);
                            intent.putExtra("skip_interception", true);
                            intent.putExtra("ThumbnailRect", iArr);
                            context.startActivity(intent, GlobalScreenshot.this.createQuitAnimationBundle());
                            StatHelper.recordNewScreenshotEvent(GlobalScreenshot.this.mContext, "new_click_thumbnail", (Map<String, String>) null);
                        }
                    } else {
                        Context context = context;
                        Toast.makeText(context, context.getResources().getString(R.string.screenshot_insert_failed), 0).show();
                    }
                }
            });
        }
    }

    /* access modifiers changed from: private */
    public Bundle createQuitAnimationBundle() {
        return ActivityOptions.makeCustomAnimation(this.mContext, 0, 0, this.mHandler, (ActivityOptions.OnAnimationStartedListener) null).toBundle();
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0023, code lost:
        if (r0 != 3) goto L_0x00ed;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean onThumbnailViewTouch(android.view.MotionEvent r8) {
        /*
            r7 = this;
            boolean r0 = r7.mIsInOutAnimating
            r1 = 0
            if (r0 == 0) goto L_0x0006
            return r1
        L_0x0006:
            float r0 = r8.getRawX()
            float r2 = r8.getRawY()
            r8.setLocation(r0, r2)
            android.view.VelocityTracker r0 = r7.mVTracker
            r0.addMovement(r8)
            int r0 = r8.getAction()
            r2 = 1
            if (r0 == 0) goto L_0x00d5
            if (r0 == r2) goto L_0x0072
            r3 = 2
            if (r0 == r3) goto L_0x0027
            r8 = 3
            if (r0 == r8) goto L_0x0072
            goto L_0x00ed
        L_0x0027:
            float r0 = r8.getRawY()
            float r1 = r7.mTouchDownY
            float r0 = r0 - r1
            int r0 = (int) r0
            float r8 = r8.getRawX()
            float r1 = r7.mTouchDownX
            float r8 = r8 - r1
            int r8 = (int) r8
            boolean r1 = r7.mIsThumbnailMoving
            if (r1 != 0) goto L_0x0051
            int r1 = java.lang.Math.abs(r0)
            android.content.Context r3 = r7.mContext
            android.view.ViewConfiguration r3 = android.view.ViewConfiguration.get(r3)
            int r3 = r3.getScaledTouchSlop()
            if (r1 <= r3) goto L_0x0051
            r7.mIsThumbnailMoving = r2
            r1 = 0
            r7.startLongScreenShotGuideAlphaAnim(r1)
        L_0x0051:
            android.animation.ValueAnimator r1 = r7.mThumbnailShakeAnimator
            java.lang.Object r1 = r1.getAnimatedValue()
            java.lang.Integer r1 = (java.lang.Integer) r1
            int r1 = r1.intValue()
            int r8 = -r8
            double r3 = (double) r8
            r5 = 4587366580439587226(0x3fa999999999999a, double:0.05)
            double r3 = r3 * r5
            int r8 = (int) r3
            int r8 = r8 + r1
            if (r0 <= 0) goto L_0x006c
            double r3 = (double) r0
            double r3 = r3 * r5
            int r0 = (int) r3
        L_0x006c:
            int r0 = r0 + r1
            r7.moveThumbnailWindow((int) r8, (int) r0)
            goto L_0x00ed
        L_0x0072:
            boolean r8 = r7.mIsThumbnailMoving
            if (r8 != 0) goto L_0x0089
            boolean r8 = r7.isDeviceProvisioned
            if (r8 == 0) goto L_0x0089
            android.animation.ValueAnimator r8 = r7.mThumbnailShakeAnimator
            r8.cancel()
            android.content.Context r8 = r7.mContext
            com.android.systemui.screenshot.NotifyMediaStoreData r0 = r7.mNotifyMediaStoreData
            java.lang.String r3 = "edit"
            r7.jumpProcess(r8, r0, r3)
            goto L_0x00cd
        L_0x0089:
            android.view.VelocityTracker r8 = r7.mVTracker
            r0 = 1000(0x3e8, float:1.401E-42)
            r8.computeCurrentVelocity(r0)
            android.content.Context r8 = r7.mContext
            android.content.res.Resources r8 = r8.getResources()
            android.util.DisplayMetrics r8 = r8.getDisplayMetrics()
            float r8 = r8.density
            r0 = -1020657664(0xffffffffc32a0000, float:-170.0)
            float r8 = r8 * r0
            int r8 = (int) r8
            android.view.VelocityTracker r0 = r7.mVTracker
            float r0 = r0.getYVelocity()
            float r8 = (float) r8
            int r8 = (r0 > r8 ? 1 : (r0 == r8 ? 0 : -1))
            if (r8 >= 0) goto L_0x00b9
            r7.quitThumbnailWindow(r2, r2)
            android.content.Context r8 = r7.mContext
            java.lang.String r0 = "quit_thumbnail"
            java.lang.String r3 = "slide_up"
            com.android.systemui.screenshot.StatHelper.recordCountEvent((android.content.Context) r8, (java.lang.String) r0, (java.lang.String) r3)
            goto L_0x00cd
        L_0x00b9:
            r7.getTranslation()
            r7.goInitialPosition()
            r8 = 1065353216(0x3f800000, float:1.0)
            r7.startLongScreenShotGuideAlphaAnim(r8)
            android.os.Handler r8 = r7.mHandler
            java.lang.Runnable r0 = r7.mQuitThumbnailRunnable
            r3 = 3600(0xe10, double:1.7786E-320)
            r8.postDelayed(r0, r3)
        L_0x00cd:
            r7.mIsThumbnailMoving = r1
            android.view.VelocityTracker r7 = r7.mVTracker
            r7.clear()
            goto L_0x00ed
        L_0x00d5:
            float r0 = r8.getRawY()
            r7.mTouchDownY = r0
            float r8 = r8.getRawX()
            r7.mTouchDownX = r8
            android.os.Handler r8 = r7.mHandler
            java.lang.Runnable r0 = r7.mQuitThumbnailRunnable
            r8.removeCallbacks(r0)
            android.animation.ValueAnimator r7 = r7.mThumbnailShakeAnimator
            r7.cancel()
        L_0x00ed:
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.screenshot.GlobalScreenshot.onThumbnailViewTouch(android.view.MotionEvent):boolean");
    }

    private void goInitialPosition() {
        ValueAnimator goInitialPosition = goInitialPosition((int) (this.mFirstTranslationX - this.mScreenshotTranslationX), true);
        ValueAnimator goInitialPosition2 = goInitialPosition((int) (this.mScreenshotTranslationY - this.mFirstTranslationY), false);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(new Animator[]{goInitialPosition, goInitialPosition2});
        animatorSet.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animator) {
                super.onAnimationEnd(animator);
                if (GlobalScreenshot.this.mScreenshotLayout.getWindowToken() != null) {
                    GlobalScreenshot.this.mThumbnailShakeAnimator.start();
                }
            }
        });
        animatorSet.start();
    }

    private ValueAnimator goInitialPosition(int i, final boolean z) {
        ValueAnimator ofInt = ValueAnimator.ofInt(new int[]{0, i});
        ofInt.setDuration(350);
        ofInt.setInterpolator(new PhysicBasedInterpolator(0.9f, 0.86f));
        ofInt.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                GlobalScreenshot.this.moveThumbnailWindow(((Integer) valueAnimator.getAnimatedValue()).intValue(), z);
            }
        });
        ofInt.setInterpolator(new AccelerateInterpolator());
        return ofInt;
    }

    private void moveThumbnailWindow(int i, int i2) {
        float f = (float) i;
        this.mScreenShareView.setTranslationX(this.mBtnTranslationX - f);
        this.mScreenLongShotViewGroup.setTranslationX(this.mBtnTranslationX - f);
        this.mScreenshotView.setTranslationX(this.mScreenshotTranslationX - f);
        this.mScreenshotShadow.setTranslationX(this.mShadowTranslationX - f);
        float f2 = (float) i2;
        this.mScreenShareView.setTranslationY(this.mBtnTranslationY + f2);
        this.mScreenLongShotViewGroup.setTranslationY(this.mBtnTranslationY + f2);
        this.mScreenshotView.setTranslationY(this.mScreenshotTranslationY + f2);
        this.mScreenshotShadow.setTranslationY(this.mShadowTranslationY + f2);
    }

    /* access modifiers changed from: private */
    public void moveThumbnailWindow(int i, boolean z) {
        if (z) {
            float f = (float) i;
            this.mScreenShareView.setTranslationX(this.mBtnTranslationX + f);
            this.mScreenLongShotViewGroup.setTranslationX(this.mBtnTranslationX + f);
            this.mScreenshotView.setTranslationX(this.mScreenshotTranslationX + f);
            this.mScreenshotShadow.setTranslationX(this.mShadowTranslationX + f);
            return;
        }
        float f2 = (float) i;
        this.mScreenShareView.setTranslationY(this.mBtnTranslationY - f2);
        this.mScreenLongShotViewGroup.setTranslationY(this.mBtnTranslationY - f2);
        this.mScreenshotView.setTranslationY(this.mScreenshotTranslationY - f2);
        this.mScreenshotShadow.setTranslationY(this.mShadowTranslationY - f2);
    }

    private void saveScreenshotInWorkerThread(Runnable runnable, Runnable runnable2) {
        SaveImageInBackgroundData saveImageInBackgroundData = new SaveImageInBackgroundData();
        saveImageInBackgroundData.screenshotDisplay = this.mScreenshotDisplay;
        saveImageInBackgroundData.screenLongShotView = this.mScreenLongShotView;
        Context context = this.mContext;
        saveImageInBackgroundData.context = context;
        saveImageInBackgroundData.image = this.mScreenBitmap;
        saveImageInBackgroundData.iconSize = this.mNotificationIconSize;
        saveImageInBackgroundData.finisher = runnable;
        saveImageInBackgroundData.orientationLandscape = this.mOrientationLandscape;
        SaveImageInBackgroundTask saveImageInBackgroundTask = new SaveImageInBackgroundTask(context, saveImageInBackgroundData, this.mNotificationManager);
        saveImageInBackgroundTask.execute(new SaveImageInBackgroundData[]{saveImageInBackgroundData});
        this.mNotifyMediaStoreData = saveImageInBackgroundTask.mNotifyMediaStoreData;
        this.mNotifyMediaStoreData.finisher = runnable2;
    }

    /* access modifiers changed from: package-private */
    public void takeScreenshot(Runnable runnable, Runnable runnable2, boolean z, boolean z2) {
        this.mIsSaved = false;
        if ("trigger_restart_min_framework".equals(SystemProperties.get("vold.decrypt")) || !UserManagerCompat.isUserUnlocked((UserManager) this.mContext.getSystemService(UserManager.class))) {
            Log.w("GlobalScreenshot", "Can not screenshot when decrypt state.");
            if (runnable != null) {
                runnable.run();
            }
            if (runnable2 != null) {
                runnable2.run();
            }
        } else if (RestrictionsHelper.hasRestriction(this.mContext, "disallow_screencapture", UserHandle.myUserId())) {
            Log.w("GlobalScreenshot", "Can not screenshot for enterprise forbidden.");
            if (runnable != null) {
                runnable.run();
            }
            if (runnable2 != null) {
                runnable2.run();
            }
        } else {
            this.mScreenBitmap = ScreenshotUtils.getScreenshot(this.mContext);
            afterTakeScreenshot(this.mContext);
            if (this.mScreenBitmap == null) {
                Log.e("GlobalScreenshot", "mScreenBitmap == null");
                notifyScreenshotError(this.mContext, this.mNotificationManager, R.string.screenshot_failed_to_capture_title);
                if (runnable != null) {
                    runnable.run();
                }
                if (runnable2 != null) {
                    runnable2.run();
                    return;
                }
                return;
            }
            if (Constants.IS_SUPPORT_LINEAR_MOTOR_VIBRATE) {
                new HapticFeedbackUtil(this.mContext, true).performExtHapticFeedback(85);
            }
            Bitmap bitmap = this.mScreenBitmap;
            this.mScreenBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, false);
            bitmap.recycle();
            this.mScreenBitmap.setHasAlpha(false);
            this.mScreenBitmap.prepareToDraw();
            saveScreenshotInWorkerThread(new Runnable() {
                public void run() {
                    boolean unused = GlobalScreenshot.this.mIsSaved = true;
                    GlobalScreenshot.this.mScreenshotDisplay.setIsScreenshotSaved();
                }
            }, runnable2);
            this.mDisplay.getRealMetrics(this.mDisplayMetrics);
            DisplayMetrics displayMetrics = this.mDisplayMetrics;
            startAnimation(runnable, displayMetrics.widthPixels, displayMetrics.heightPixels, z, z2);
            checkBindPhotoService();
        }
    }

    private void startAnimation(final Runnable runnable, int i, int i2, boolean z, boolean z2) {
        setRadius(this.mScreenshotView, this.mPhoneTopRadius);
        this.mScreenshotView.setImageBitmap(this.mScreenBitmap);
        this.mScreenshotLayout.requestFocus();
        this.mScreenshotView.setScaleX(1.0f);
        this.mScreenshotView.setScaleY(1.0f);
        ObjectAnimator objectAnimator = this.mScreenshotAnimation;
        if (objectAnimator != null) {
            objectAnimator.end();
        }
        measureThumbnail();
        if (this.isDeviceProvisioned) {
            showLongScreenshotGuideIfNeeded();
            if (!this.mIsShowLongScreenShotGuide) {
                this.mHandler.postDelayed(this.mQuitThumbnailRunnable, 3600);
            } else {
                showLongScreenshotGuideOverlayIfNeeded();
            }
        }
        this.mWindowManager.addView(this.mScreenshotLayout, this.mWindowLayoutParams);
        this.mScreenshotAnimation = createScreenshotAlphaAnimation();
        this.mScreenshotAnimation.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animator) {
                GlobalScreenshot.this.startGotoThumbnailAnimation(runnable);
            }
        });
        this.mScreenshotLayout.post(new Runnable() {
            public void run() {
                GlobalScreenshot globalScreenshot = GlobalScreenshot.this;
                if (globalScreenshot.hasScreenshotSoundEnabled(globalScreenshot.mContext)) {
                    ForegroundThread.getHandler().post(new Runnable() {
                        public void run() {
                            GlobalScreenshot.this.updateRingtone();
                            if (GlobalScreenshot.this.mRingtone != null) {
                                GlobalScreenshot.this.mRingtone.play();
                            }
                        }
                    });
                }
                GlobalScreenshot.this.mScreenshotView.setLayerType(2, (Paint) null);
                GlobalScreenshot.this.mScreenshotView.buildLayer();
                GlobalScreenshot.this.mScreenshotAnimation.start();
            }
        });
    }

    private void screenshotAnimator(final Runnable runnable) {
        ValueAnimator scaleAnimator = scaleAnimator();
        ValueAnimator scaleShadowAnimator = scaleShadowAnimator();
        ValueAnimator cornerRadiusScaleAnimator = cornerRadiusScaleAnimator();
        ValueAnimator translationXAnimator = translationXAnimator();
        ValueAnimator shadowTranslationXAnimator = shadowTranslationXAnimator();
        ValueAnimator translationYAnimator = translationYAnimator();
        ObjectAnimator alphaAnimator = alphaAnimator();
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(new Animator[]{scaleAnimator, scaleShadowAnimator, translationXAnimator, shadowTranslationXAnimator, cornerRadiusScaleAnimator, translationYAnimator, alphaAnimator});
        animatorSet.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animator) {
                super.onAnimationEnd(animator);
                runnable.run();
                boolean unused = GlobalScreenshot.this.mIsInOutAnimating = false;
                if (!GlobalScreenshot.this.mIsQuited) {
                    GlobalScreenshot.this.mThumbnailShakeAnimator.start();
                }
            }
        });
        animatorSet.start();
    }

    private ObjectAnimator alphaAnimator() {
        ObjectAnimator ofFloat = ObjectAnimator.ofFloat(this.mScreenWhiteBg, "alpha", new float[]{0.5f, 0.0f});
        ofFloat.setDuration(350);
        ofFloat.setInterpolator(new CubicEaseInOutInterpolator());
        ofFloat.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animator) {
                GlobalScreenshot.this.mScreenWhiteBg.setVisibility(8);
            }
        });
        return ofFloat;
    }

    private ValueAnimator cornerRadiusScaleAnimator() {
        ValueAnimator ofFloat = ValueAnimator.ofFloat(new float[]{this.mPhoneTopRadius, this.mRadius / this.mThumnailScale});
        ofFloat.setDuration(400);
        ofFloat.setInterpolator(new PhysicBasedInterpolator(0.95f, 0.75f));
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                GlobalScreenshot globalScreenshot = GlobalScreenshot.this;
                globalScreenshot.setRadius(globalScreenshot.mScreenshotView, floatValue);
                GlobalScreenshot globalScreenshot2 = GlobalScreenshot.this;
                globalScreenshot2.setRadius(globalScreenshot2.mScreenWhiteBg, floatValue);
            }
        });
        return ofFloat;
    }

    /* access modifiers changed from: private */
    public void setRadius(View view, final float f) {
        view.setOutlineProvider(new ViewOutlineProvider() {
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), f);
            }
        });
        view.setClipToOutline(true);
    }

    private ValueAnimator scaleAnimator() {
        ValueAnimator ofFloat = ValueAnimator.ofFloat(new float[]{1.0f, this.mThumnailScale});
        ofFloat.setDuration(400);
        ofFloat.setInterpolator(new PhysicBasedInterpolator(0.95f, 0.75f));
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                float unused = GlobalScreenshot.this.mScaleValue = floatValue;
                GlobalScreenshot.this.scaleScreenshot(floatValue);
                GlobalScreenshot.this.scaleBtnWidth(floatValue);
            }
        });
        return ofFloat;
    }

    private ValueAnimator scaleShadowAnimator() {
        ValueAnimator ofFloat = ValueAnimator.ofFloat(new float[]{this.mShadowScale, 1.0f});
        ofFloat.setDuration(400);
        ofFloat.setInterpolator(new PhysicBasedInterpolator(0.95f, 0.75f));
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                GlobalScreenshot.this.mScreenshotShadow.setScaleX(floatValue);
                GlobalScreenshot.this.mScreenshotShadow.setScaleY(floatValue);
            }
        });
        return ofFloat;
    }

    private ValueAnimator translationYAnimator() {
        ValueAnimator ofFloat = ValueAnimator.ofFloat(new float[]{0.0f, -(((((float) this.mScreenHeight) / 2.0f) - (((float) this.mThumbnailHeight) / 2.0f)) - ((float) this.mThumbnailTop))});
        ofFloat.setDuration(650);
        ofFloat.setInterpolator(new PhysicBasedInterpolator(0.9f, 0.77f));
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                GlobalScreenshot.this.mScreenshotView.setTranslationY(floatValue);
                GlobalScreenshot.this.mScreenWhiteBg.setTranslationY(floatValue);
                float access$4800 = (((floatValue + ((((float) GlobalScreenshot.this.mScreenHeight) * GlobalScreenshot.this.mScaleValue) / 2.0f)) + 0.5f) - (((float) GlobalScreenshot.this.mScreenHeight) / 2.0f)) + 0.5f;
                GlobalScreenshot.this.mScreenShareView.setTranslationY(access$4800);
                GlobalScreenshot.this.mScreenLongShotViewGroup.setTranslationY(access$4800);
            }
        });
        ofFloat.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animator) {
                super.onAnimationEnd(animator);
                GlobalScreenshot.this.getTranslationY();
                GlobalScreenshot globalScreenshot = GlobalScreenshot.this;
                float unused = globalScreenshot.mFirstTranslationY = globalScreenshot.mScreenshotView.getTranslationY();
            }
        });
        return ofFloat;
    }

    private ValueAnimator translationXAnimator() {
        ValueAnimator ofFloat = ValueAnimator.ofFloat(new float[]{0.0f, (this.mThumbnailMarginLeft - (((float) this.mScreenWidth) / 2.0f)) + (((float) this.mThumbnailWidth) / 2.0f)});
        ofFloat.setDuration(350);
        ofFloat.setInterpolator(new PhysicBasedInterpolator(0.9f, 0.86f));
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                GlobalScreenshot.this.mScreenshotView.setTranslationX(floatValue);
                GlobalScreenshot.this.mScreenWhiteBg.setTranslationX(floatValue);
                float access$5100 = ((((((float) GlobalScreenshot.this.mScreenWidth) / 2.0f) + 0.5f) + floatValue) - ((((float) GlobalScreenshot.this.mScreenWidth) * GlobalScreenshot.this.mScaleValue) / 2.0f)) + 0.5f;
                GlobalScreenshot.this.mScreenShareView.setTranslationX(access$5100);
                GlobalScreenshot.this.mScreenLongShotViewGroup.setTranslationX(access$5100);
            }
        });
        ofFloat.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animator) {
                super.onAnimationEnd(animator);
                GlobalScreenshot.this.getTranslationX();
                GlobalScreenshot globalScreenshot = GlobalScreenshot.this;
                float unused = globalScreenshot.mFirstTranslationX = globalScreenshot.mScreenshotView.getTranslationX();
            }
        });
        return ofFloat;
    }

    private ValueAnimator shadowTranslationXAnimator() {
        ValueAnimator ofFloat = ValueAnimator.ofFloat(new float[]{0.0f, (float) this.mShadowMargin});
        ofFloat.setDuration(350);
        ofFloat.setInterpolator(new PhysicBasedInterpolator(0.9f, 0.86f));
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                GlobalScreenshot.this.mScreenshotShadow.setTranslationX(((Float) valueAnimator.getAnimatedValue()).floatValue());
            }
        });
        ofFloat.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animator) {
                super.onAnimationEnd(animator);
                GlobalScreenshot.this.getTranslationX();
            }
        });
        return ofFloat;
    }

    private float getThumbnailMarginLeft() {
        float f = (float) ((this.mScreenWidth - this.mThumbnailTotalWidth) + this.mThumbnailLeft);
        return (Build.IS_TABLET || !this.mHasNavBar || this.mContext.getResources().getConfiguration().orientation != 2 || this.mDisplay.getRotation() != 3 || SystemProperties.getInt("ro.miui.notch", 0) != 1 || MiuiSettings.Global.getBoolean(this.mContext.getContentResolver(), "force_black_v2")) ? f : f - ((float) this.mContext.getResources().getDimensionPixelSize(R.dimen.notch_height));
    }

    /* access modifiers changed from: private */
    public void scaleBtnWidth(float f) {
        RelativeLayout.LayoutParams layoutParams = this.mShareParams;
        layoutParams.width = (int) ((((float) this.mScreenWidth) * f) + 0.5f);
        this.mScreenShareView.setLayoutParams(layoutParams);
        RelativeLayout.LayoutParams layoutParams2 = this.mLongShotParams;
        layoutParams2.width = (int) ((((float) this.mScreenWidth) * f) + 0.5f);
        this.mScreenLongShotViewGroup.setLayoutParams(layoutParams2);
    }

    /* access modifiers changed from: private */
    public void scaleScreenshot(float f) {
        this.mScreenshotView.setScaleX(f);
        this.mScreenshotView.setScaleY(f);
        this.mScreenWhiteBg.setScaleX(f);
        this.mScreenWhiteBg.setScaleY(f);
    }

    /* access modifiers changed from: private */
    public boolean hasScreenshotSoundEnabled(Context context) {
        if (!Build.checkRegion(Locale.KOREA.getCountry()) || !"monet".equals(android.os.Build.DEVICE) || !isCameraPreviewPage()) {
            return MiuiSettings.System.getBooleanForUser(context.getContentResolver(), "has_screenshot_sound", true, 0);
        }
        return true;
    }

    private boolean isCameraPreviewPage() {
        ComponentName topActivity = Util.getTopActivity(this.mContext);
        if (topActivity == null) {
            return false;
        }
        return "com.android.camera.Camera".equals(topActivity.getClassName());
    }

    private boolean isDeviceProvisioned() {
        return Settings.Global.getInt(this.mContext.getContentResolver(), "device_provisioned", 0) != 0;
    }

    /* access modifiers changed from: private */
    public void startGotoThumbnailAnimation(Runnable runnable) {
        this.mIsQuited = false;
        this.mScreenshotLayout.getViewTreeObserver().addOnComputeInternalInsetsListener(this.mOnComputeInternalInsetsListener);
        this.mContext.getApplicationContext().registerReceiver(this.mBeforeScreenshotReceiver, new IntentFilter("miui.intent.TAKE_SCREENSHOT"));
        this.mIsBeforeScreenshotReceiver = true;
        this.mContext.getApplicationContext().registerReceiver(this.mConfigurationReceiver, new IntentFilter("android.intent.action.CONFIGURATION_CHANGED"));
        this.mIsConfigurationReceiver = true;
        this.mIsInOutAnimating = true;
        showShadow();
        screenshotAnimator(runnable);
    }

    private void measureThumbnail() {
        this.mThumnailScale = (((float) this.mContext.getResources().getDimensionPixelSize(R.dimen.screenshot_thumnail_width)) * 1.0f) / ((float) Math.min(this.mScreenBitmap.getWidth(), this.mScreenBitmap.getHeight()));
        this.mThumbnailWidth = (int) ((((float) this.mScreenBitmap.getWidth()) * this.mThumnailScale) + 0.5f);
        this.mThumbnailHeight = (int) ((((float) this.mScreenBitmap.getHeight()) * this.mThumnailScale) + 0.5f);
        int dimensionPixelSize = this.mContext.getResources().getDimensionPixelSize(R.dimen.screenshot_thumnail_btn_margintop);
        this.mThumbnailTotalHeight = this.mThumbnailHeight + (this.isDeviceProvisioned ? (this.mContext.getResources().getDimensionPixelSize(R.dimen.screenshot_thumnail_btn_height) * 2) + (dimensionPixelSize * 2) : 0) + this.mThumbnailTop + this.mContext.getResources().getDimensionPixelSize(R.dimen.screenshot_thumbnail_padding_bottom);
        this.mThumbnailTotalWidth = this.mThumbnailWidth + this.mThumbnailLeft + this.mThumbnailRight;
        this.mThumbnailMarginLeft = getThumbnailMarginLeft();
    }

    private void showShadow() {
        this.mShadowMargin = this.mContext.getResources().getDimensionPixelSize(R.dimen.screenshot_shadow_margin_left);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) this.mScreenshotShadow.getLayoutParams();
        layoutParams.width = this.mThumbnailTotalWidth + this.mShadowMargin;
        layoutParams.height = this.mThumbnailTotalHeight;
        this.mScreenshotShadow.setLayoutParams(layoutParams);
        this.mShadowScale = 1.0f / this.mThumnailScale;
        this.mScreenshotShadow.setScaleX(this.mShadowScale);
        this.mScreenshotShadow.setScaleY(this.mShadowScale);
        this.mScreenshotShadow.setPivotX((float) this.mThumbnailWidth);
        this.mScreenshotShadow.setPivotY(0.0f);
        this.mScreenshotShadow.setVisibility(0);
    }

    private void showLongScreenshotGuideIfNeeded() {
        SharedPreferences sharedPreferences = this.mContext.getSharedPreferences("screenshot_shared_prefs", 0);
        boolean z = sharedPreferences.getBoolean("need_show_long_screenshot_guide", true);
        if (z) {
            SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.mContext.getApplicationContext());
            if (!defaultSharedPreferences.getBoolean("need_show_long_screenshot_guide", true)) {
                sharedPreferences.edit().putBoolean("need_show_long_screenshot_guide", false).apply();
                defaultSharedPreferences.edit().remove("need_show_long_screenshot_guide").apply();
                z = false;
            }
        }
        if (z) {
            this.mLongScreenShotGuideLayout = (LongScreenShotGuideLayout) ((LayoutInflater) this.mContext.getSystemService("layout_inflater")).inflate(R.layout.global_screenshot_guide, (ViewGroup) null);
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(2024, 134218792, -3);
            layoutParams.setTitle("LongScreenShotGuide");
            WindowManagerCompat.setLayoutInDisplayCutoutMode(layoutParams, 1);
            this.mWindowManager.addView(this.mLongScreenShotGuideLayout, layoutParams);
            this.mLongScreenShotGuideLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                public void onGlobalLayout() {
                    GlobalScreenshot.this.mLongScreenShotGuideLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    int dimensionPixelSize = GlobalScreenshot.this.mContext.getResources().getDimensionPixelSize(R.dimen.screenshot_thumnail_btn_margintop);
                    int dimensionPixelSize2 = GlobalScreenshot.this.mContext.getResources().getDimensionPixelSize(R.dimen.screenshot_thumnail_btn_height);
                    int access$900 = (int) GlobalScreenshot.this.mThumbnailMarginLeft;
                    int access$1000 = GlobalScreenshot.this.mThumbnailTop + GlobalScreenshot.this.mThumbnailHeight + dimensionPixelSize;
                    GlobalScreenshot.this.mLongScreenShotGuideLayout.setLongScreenShotButtonBound(new Rect(access$900, access$1000, GlobalScreenshot.this.mThumbnailWidth + access$900, dimensionPixelSize2 + access$1000), GlobalScreenshot.this.mScreenBitmap.getWidth());
                }
            });
            sharedPreferences.edit().putBoolean("need_show_long_screenshot_guide", false).apply();
            this.mIsShowLongScreenShotGuide = true;
            this.mContext.getApplicationContext().registerReceiver(this.mScreenOffReceiver, new IntentFilter("android.intent.action.SCREEN_OFF"));
        }
    }

    private void showLongScreenshotGuideOverlayIfNeeded() {
        if (this.mIsShowLongScreenShotGuide) {
            this.mLongScreenShotGuideLayoutOverlay = new View(this.mContext);
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(2024, 134218792, -3);
            layoutParams.setTitle("LongScreenshotGuideOverlay");
            WindowManagerCompat.setLayoutInDisplayCutoutMode(layoutParams, 1);
            this.mWindowManager.addView(this.mLongScreenShotGuideLayoutOverlay, layoutParams);
            this.mHandler.postDelayed(this.mRemoveLongScreenShotGuideOverlayRunnable, 1500);
            this.mIsShowLongScreenShotGuideOverlay = true;
        }
    }

    /* access modifiers changed from: private */
    public void removeLongScreenShotGuide(boolean z) {
        if (this.mIsShowLongScreenShotGuide) {
            if (z) {
                this.mLongScreenShotGuideLayout.animate().alpha(0.0f).setDuration(100).setListener(new AnimatorListenerAdapter() {
                    public void onAnimationEnd(Animator animator) {
                        GlobalScreenshot.this.mWindowManager.removeView(GlobalScreenshot.this.mLongScreenShotGuideLayout);
                    }
                }).start();
            } else {
                this.mWindowManager.removeView(this.mLongScreenShotGuideLayout);
            }
            this.mIsShowLongScreenShotGuide = false;
            this.mContext.getApplicationContext().unregisterReceiver(this.mScreenOffReceiver);
        }
    }

    /* access modifiers changed from: private */
    public void removeLongScreenShotGuideOverlay() {
        if (this.mIsShowLongScreenShotGuideOverlay) {
            this.mHandler.removeCallbacks(this.mRemoveLongScreenShotGuideOverlayRunnable);
            this.mRemoveLongScreenShotGuideOverlayRunnable.run();
        }
    }

    private void startLongScreenShotGuideAlphaAnim(float f) {
        if (this.mIsShowLongScreenShotGuide) {
            this.mLongScreenShotGuideLayout.startAlphaChangeAnim(f);
        }
    }

    /* access modifiers changed from: private */
    public void quitThumbnailWindow(boolean z, boolean z2) {
        if (this.mScreenshotLayout.getWindowToken() != null && !this.mIsQuited) {
            this.mIsQuited = true;
            this.mHandler.removeCallbacks(this.mQuitThumbnailRunnable);
            this.mThumbnailShakeAnimator.cancel();
            if (this.mIsBeforeScreenshotReceiver) {
                this.mContext.getApplicationContext().unregisterReceiver(this.mBeforeScreenshotReceiver);
                this.mIsBeforeScreenshotReceiver = false;
            }
            if (this.mIsConfigurationReceiver) {
                this.mContext.getApplicationContext().unregisterReceiver(this.mConfigurationReceiver);
                this.mIsConfigurationReceiver = false;
            }
            if (!z) {
                this.mWindowManager.removeView(this.mScreenshotLayout);
                this.mScreenshotLayout.getViewTreeObserver().removeOnComputeInternalInsetsListener(this.mOnComputeInternalInsetsListener);
            } else {
                this.mIsInOutAnimating = true;
                getTranslation();
                ValueAnimator ofInt = ValueAnimator.ofInt(new int[]{0, this.mThumbnailTotalHeight + this.mThumbnailTop});
                ofInt.setDuration(150);
                ofInt.setInterpolator(new CubicEaseInOutInterpolator());
                ofInt.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        GlobalScreenshot.this.moveThumbnailWindow(((Integer) valueAnimator.getAnimatedValue()).intValue(), false);
                    }
                });
                ofInt.addListener(new AnimatorListenerAdapter() {
                    public void onAnimationEnd(Animator animator) {
                        if (GlobalScreenshot.this.mScreenshotLayout.getWindowToken() != null) {
                            GlobalScreenshot.this.mWindowManager.removeView(GlobalScreenshot.this.mScreenshotLayout);
                            GlobalScreenshot.this.mScreenshotLayout.getViewTreeObserver().removeOnComputeInternalInsetsListener(GlobalScreenshot.this.mOnComputeInternalInsetsListener);
                            boolean unused = GlobalScreenshot.this.mIsInOutAnimating = false;
                        }
                    }
                });
                ofInt.start();
            }
            removeLongScreenShotGuide(z);
            if (z2) {
                notifyMediaAndFinish(this.mContext, this.mNotifyMediaStoreData);
            }
            unBindPhotoService();
        }
    }

    private ObjectAnimator createScreenshotAlphaAnimation() {
        ObjectAnimator ofFloat = ObjectAnimator.ofFloat(this.mScreenWhiteBg, "alpha", new float[]{0.0f, 0.5f});
        ofFloat.setInterpolator(new CubicEaseOutInterpolator());
        ofFloat.setDuration(150);
        ofFloat.addListener(new AnimatorListenerAdapter() {
            public void onAnimationStart(Animator animator) {
                GlobalScreenshot.this.mScreenWhiteBg.setVisibility(0);
            }
        });
        return ofFloat;
    }

    static void notifyScreenshotError(Context context, NotificationManager notificationManager, int i) {
        notificationManager.cancel(789);
        Toast.makeText(context, i, 0).show();
    }

    public static void beforeTakeScreenshot(Context context) {
        Intent intent = new Intent("miui.intent.TAKE_SCREENSHOT");
        intent.putExtra("IsFinished", false);
        context.sendBroadcast(intent);
    }

    public static void afterTakeScreenshot(Context context) {
        Intent intent = new Intent("miui.intent.TAKE_SCREENSHOT");
        intent.putExtra("IsFinished", true);
        context.sendBroadcast(intent);
    }
}
