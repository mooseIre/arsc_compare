package com.android.systemui.screenshot;

import android.app.ActivityOptions;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextCompat;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.provider.MediaStore;
import android.provider.MiuiSettings;
import android.text.TextUtils;
import android.util.Log;
import android.view.IWindowManager;
import android.view.IWindowManagerCompat;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MiuiWindowManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.WindowManagerCompat;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.android.systemui.Constants;
import com.android.systemui.SystemUICompat;
import com.android.systemui.miui.ToastOverlayManager;
import com.android.systemui.plugins.R;
import com.android.systemui.screenshot.GlobalScreenshot;
import com.android.systemui.screenshot.ScreenshotScrollView;
import com.xiaomi.stat.MiStat;
import com.xiaomi.stat.c.b;
import java.io.File;
import java.io.IOException;
import java.lang.Thread;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import miui.R$style;
import miui.accounts.ExtraAccountManager;
import miui.graphics.BitmapFactory;
import miui.os.Build;
import miui.process.IMiuiApplicationThread;
import miui.process.ProcessManager;
import miui.util.ScreenshotUtils;

public class GlobalScreenshotDisplay implements ScreenshotScrollView.AnimatingCallback, Thread.UncaughtExceptionHandler {
    static SoftReference<int[]> sPixelsCache;
    private TextView mActionBarBack;
    /* access modifiers changed from: private */
    public Button mActionBarFeedback;
    /* access modifiers changed from: private */
    public View mActionBarLayout;
    /* access modifiers changed from: private */
    public View mBackgroundView;
    private View mBottomContainerDivider;
    /* access modifiers changed from: private */
    public ViewGroup mButtonContainer;
    /* access modifiers changed from: private */
    public Button mButtonStopLongScreenshot;
    /* access modifiers changed from: private */
    public Context mContext;
    private IMiuiApplicationThread mForeAppThread;
    private Handler mHandler = new Handler();
    private boolean mHasNavigationBar;
    private boolean mIsScreenshotSaved;
    private boolean mIsShow;
    /* access modifiers changed from: private */
    public boolean mIsShowingLongScreenshot;
    /* access modifiers changed from: private */
    public boolean mIsTakingLongScreenshot;
    /* access modifiers changed from: private */
    public Bitmap mLongScreenshotFirstPart;
    private BroadcastReceiver mLongScreenshotReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            GlobalScreenshotDisplay.this.onCallbackReceive(intent);
        }
    };
    /* access modifiers changed from: private */
    public int mNavigationBarHeight;
    /* access modifiers changed from: private */
    public NotifyMediaStoreData mNotifyMediaStoreData;
    /* access modifiers changed from: private */
    public boolean mPendingContinueSnap;
    private Runnable mPendingSavedRunnable;
    private BroadcastReceiver mQuitReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            GlobalScreenshotDisplay.this.backAll();
            Context access$200 = GlobalScreenshotDisplay.this.mContext;
            StatHelper.recordCountEvent(access$200, "quit_display", "receiver_" + intent.getAction());
        }
    };
    /* access modifiers changed from: private */
    public View mRootView;
    /* access modifiers changed from: private */
    public Bitmap mScreenshot;
    ArrayList<Bitmap> mScreenshotParts = new ArrayList<>();
    /* access modifiers changed from: private */
    public ScreenshotScrollView mScreenshotView;
    /* access modifiers changed from: private */
    public boolean mTakedTotalParts;
    private ToastOverlayManager mToastOverlayManager;
    /* access modifiers changed from: private */
    public View mTopMsgDivider;
    private ViewGroup mTopMsgLayout;
    /* access modifiers changed from: private */
    public TextView mTxtTopMsg;
    /* access modifiers changed from: private */
    public WindowManager.LayoutParams mWindowLayoutParams;
    /* access modifiers changed from: private */
    public WindowManager mWindowManager;

    /* access modifiers changed from: private */
    public void backAll() {
        if (!back()) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    GlobalScreenshotDisplay.this.backAll();
                }
            });
        }
    }

    /* access modifiers changed from: private */
    public boolean back() {
        if (this.mIsTakingLongScreenshot) {
            if (this.mButtonStopLongScreenshot.isEnabled()) {
                stopLongScreenshot(true);
                HashMap hashMap = new HashMap();
                hashMap.put("finish_ways", "cancel");
                StatHelper.recordNewScreenshotEvent(this.mContext, "new_finish_long_screenshot", hashMap);
            }
            return false;
        }
        quit(true, false);
        return true;
    }

    public GlobalScreenshotDisplay(Context context) {
        this.mContext = context;
        context.setTheme(R$style.Theme_DayNight);
        this.mWindowManager = (WindowManager) context.getSystemService("window");
        try {
            boolean hasNavigationBar = IWindowManagerCompat.hasNavigationBar(IWindowManager.Stub.asInterface(ServiceManager.getService("window")), ContextCompat.getDisplayId(context));
            this.mHasNavigationBar = hasNavigationBar;
            if (hasNavigationBar && MiuiSettings.Global.getBoolean(this.mContext.getContentResolver(), "force_fsg_nav_bar")) {
                this.mHasNavigationBar = false;
            }
            if (this.mHasNavigationBar) {
                this.mNavigationBarHeight = this.mContext.getResources().getDimensionPixelSize(R.dimen.navigation_bar_size);
            }
        } catch (RemoteException unused) {
        }
        View inflate = ((LayoutInflater) this.mContext.getSystemService("layout_inflater")).inflate(R.layout.global_screenshot_display, (ViewGroup) null);
        this.mRootView = inflate;
        inflate.setSystemUiVisibility(512);
        this.mScreenshotView = (ScreenshotScrollView) this.mRootView.findViewById(R.id.global_screenshot);
        this.mButtonContainer = (ViewGroup) this.mRootView.findViewById(R.id.button_container);
        this.mTopMsgLayout = (ViewGroup) this.mRootView.findViewById(R.id.top_titleormsg_layout);
        this.mBackgroundView = this.mRootView.findViewById(R.id.background);
        this.mActionBarLayout = this.mRootView.findViewById(R.id.screenshot_actionbar_layout);
        this.mActionBarFeedback = (Button) this.mRootView.findViewById(R.id.screenshot_feedback);
        if (isShowFeedback()) {
            this.mActionBarFeedback.setVisibility(0);
        }
        this.mActionBarFeedback.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                GlobalScreenshotDisplay.this.clickActionBtn("feedback");
            }
        });
        TextView textView = (TextView) this.mRootView.findViewById(R.id.screenshot_toalbum);
        this.mActionBarBack = textView;
        textView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                boolean unused = GlobalScreenshotDisplay.this.back();
            }
        });
        this.mTxtTopMsg = (TextView) this.mRootView.findViewById(R.id.txt_top_msg);
        this.mTopMsgDivider = this.mRootView.findViewById(R.id.top_msg_divider);
        this.mBottomContainerDivider = this.mRootView.findViewById(R.id.bottom_container_divider);
        if (this.mHasNavigationBar) {
            if (this.mContext.getResources().getConfiguration().orientation == 1 || Build.IS_TABLET) {
                ViewGroup viewGroup = this.mButtonContainer;
                viewGroup.setPadding(viewGroup.getPaddingLeft(), this.mButtonContainer.getPaddingTop(), this.mButtonContainer.getPaddingRight(), this.mNavigationBarHeight);
            } else {
                int paddingLeft = this.mActionBarLayout.getPaddingLeft();
                int paddingRight = this.mActionBarLayout.getPaddingRight();
                if (this.mWindowManager.getDefaultDisplay().getRotation() == 3) {
                    paddingLeft = this.mNavigationBarHeight + this.mActionBarLayout.getPaddingLeft();
                } else if (this.mWindowManager.getDefaultDisplay().getRotation() == 1) {
                    paddingRight = this.mActionBarLayout.getPaddingRight() + this.mNavigationBarHeight;
                }
                View view = this.mActionBarLayout;
                view.setPadding(paddingLeft, view.getPaddingTop(), paddingRight, this.mActionBarLayout.getPaddingBottom());
                ScreenshotScrollView screenshotScrollView = this.mScreenshotView;
                screenshotScrollView.setPadding(this.mNavigationBarHeight + screenshotScrollView.getPaddingLeft(), this.mScreenshotView.getPaddingTop(), this.mScreenshotView.getPaddingRight() + this.mNavigationBarHeight, this.mScreenshotView.getPaddingBottom());
            }
        }
        Configuration configuration = this.mContext.getResources().getConfiguration();
        if (Constants.IS_NOTCH && configuration.orientation == 1) {
            int dimensionPixelSize = this.mContext.getResources().getDimensionPixelSize(R.dimen.status_bar_height);
            ViewGroup.LayoutParams layoutParams = this.mActionBarLayout.getLayoutParams();
            layoutParams.height = context.getResources().getDimensionPixelSize(R.dimen.screenshot_actionbar_back_height) + dimensionPixelSize;
            this.mActionBarLayout.setLayoutParams(layoutParams);
            int dimensionPixelSize2 = context.getResources().getDimensionPixelSize(R.dimen.screenshot_topmsg_padding);
            this.mTxtTopMsg.setPadding(dimensionPixelSize2, dimensionPixelSize + dimensionPixelSize2, dimensionPixelSize2, dimensionPixelSize2);
        }
        this.mButtonStopLongScreenshot = (Button) this.mRootView.findViewById(R.id.button_stop_long_screenshot);
        this.mScreenshotView.setAnimatingCallback(this);
        this.mScreenshotView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (!GlobalScreenshotDisplay.this.mIsTakingLongScreenshot) {
                    return false;
                }
                GlobalScreenshotDisplay.this.mTxtTopMsg.setText(R.string.long_screenshot_top_msg_manual);
                return false;
            }
        });
        this.mRootView.setFocusableInTouchMode(true);
        this.mRootView.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (GlobalScreenshotDisplay.this.isPendingAction()) {
                    return true;
                }
                if (keyEvent.getAction() != 0 || i != 4) {
                    return false;
                }
                boolean unused = GlobalScreenshotDisplay.this.back();
                StatHelper.recordCountEvent(GlobalScreenshotDisplay.this.mContext, "quit_display", "key_back");
                return true;
            }
        });
        this.mButtonStopLongScreenshot.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                GlobalScreenshotDisplay.this.stopLongScreenshot(false);
                HashMap hashMap = new HashMap();
                hashMap.put("finish_ways", "click_to_finish");
                StatHelper.recordNewScreenshotEvent(GlobalScreenshotDisplay.this.mContext, "new_finish_long_screenshot", hashMap);
            }
        });
        WindowManager.LayoutParams layoutParams2 = new WindowManager.LayoutParams(-1, -1, 0, 0, 2024, 17368320, -3);
        this.mWindowLayoutParams = layoutParams2;
        layoutParams2.screenOrientation = 14;
        if (Build.VERSION.SDK_INT >= 28) {
            layoutParams2.extraFlags |= 8388608;
        }
        WindowManagerCompat.setLayoutInDisplayCutoutMode(this.mWindowLayoutParams, 1);
        this.mWindowLayoutParams.setTitle("GlobalScreenshotShow");
        ToastOverlayManager toastOverlayManager = new ToastOverlayManager();
        this.mToastOverlayManager = toastOverlayManager;
        toastOverlayManager.setup(this.mContext, (ViewGroup) this.mRootView);
    }

    private boolean isShowFeedback() {
        if (!miui.os.Build.IS_ALPHA_BUILD && !miui.os.Build.IS_DEVELOPMENT_VERSION) {
            return false;
        }
        ExtraAccountManager.getXiaomiAccount(this.mContext);
        return true;
    }

    public void show(Bitmap bitmap, NotifyMediaStoreData notifyMediaStoreData, int i, int i2, int i3, int i4, boolean z) {
        Thread.currentThread().setUncaughtExceptionHandler(this);
        this.mNotifyMediaStoreData = notifyMediaStoreData;
        this.mIsShow = true;
        this.mScreenshot = bitmap;
        this.mIsShowingLongScreenshot = false;
        this.mIsScreenshotSaved = false;
        this.mPendingSavedRunnable = null;
        this.mScreenshotView.setSingleBitmap(bitmap);
        this.mWindowManager.addView(this.mRootView, this.mWindowLayoutParams);
        this.mContext.sendBroadcast(new Intent("android.intent.action.CLOSE_SYSTEM_DIALOGS"));
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.CLOSE_SYSTEM_DIALOGS");
        intentFilter.addAction("android.intent.action.PHONE_STATE");
        intentFilter.addAction("android.intent.action.SCREEN_OFF");
        this.mContext.getApplicationContext().registerReceiver(this.mQuitReceiver, intentFilter);
        sendNavigationBarVisibilityChangeIfNeed(true);
        this.mRootView.measure(View.MeasureSpec.makeMeasureSpec(bitmap.getWidth(), 1073741824), View.MeasureSpec.makeMeasureSpec(bitmap.getHeight(), 1073741824));
        View view = this.mRootView;
        view.layout(0, 0, view.getMeasuredWidth(), this.mRootView.getMeasuredHeight());
        this.mRootView.requestFocus();
        Configuration configuration = this.mContext.getResources().getConfiguration();
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) this.mActionBarFeedback.getLayoutParams();
        if (configuration.orientation == 2) {
            this.mTopMsgDivider.setVisibility(8);
            this.mActionBarLayout.setBackgroundColor(0);
        } else {
            this.mTopMsgDivider.setVisibility(0);
        }
        this.mActionBarLayout.setVisibility(0);
        if (!isShowFeedback() || !this.mHasNavigationBar || this.mContext.getResources().getConfiguration().orientation != 2) {
            calScreenshotViewPaddingAndAnim(0, i, i2, i3, i4, z);
            return;
        }
        final int i5 = i;
        final int i6 = i2;
        final int i7 = i3;
        final int i8 = i4;
        final boolean z2 = z;
        this.mActionBarFeedback.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                int[] iArr = new int[2];
                GlobalScreenshotDisplay.this.mActionBarFeedback.getLocationOnScreen(iArr);
                GlobalScreenshotDisplay.this.calScreenshotViewPaddingAndAnim(((int) GlobalScreenshotDisplay.this.mContext.getResources().getDimension(R.dimen.screenshot_feedback_margin_right)) + ((GlobalScreenshotDisplay.this.mContext.getResources().getDisplayMetrics().widthPixels + GlobalScreenshotDisplay.this.mNavigationBarHeight) - iArr[0]), i5, i6, i7, i8, z2);
                GlobalScreenshotDisplay.this.mActionBarFeedback.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    /* access modifiers changed from: private */
    public void calScreenshotViewPaddingAndAnim(int i, int i2, int i3, int i4, int i5, final boolean z) {
        this.mScreenshotView.autoCalcPadding();
        if (i > 0) {
            ScreenshotScrollView screenshotScrollView = this.mScreenshotView;
            screenshotScrollView.setPadding(i, screenshotScrollView.getPaddingTop(), i, this.mScreenshotView.getPaddingBottom());
        }
        float calcPivot = GlobalScreenshot.calcPivot((float) this.mScreenshotView.getPaddingLeft(), (float) this.mScreenshotView.getWidthInner(), (float) i2, (float) i4) - ((float) this.mScreenshotView.getLeft());
        float f = (float) i5;
        float calcPivot2 = GlobalScreenshot.calcPivot((float) this.mScreenshotView.getPaddingTop(), (float) this.mScreenshotView.getHeightInner(), (float) i3, f) - ((float) this.mScreenshotView.getTop());
        float heightInner = f / ((float) this.mScreenshotView.getHeightInner());
        this.mScreenshotView.setPivotX(calcPivot);
        this.mScreenshotView.setPivotY(calcPivot2);
        this.mScreenshotView.setScaleX(heightInner);
        this.mScreenshotView.setScaleY(heightInner);
        this.mScreenshotView.setAlpha(1.0f);
        this.mScreenshotView.setTranslationY(0.0f);
        this.mBackgroundView.setVisibility(0);
        this.mBackgroundView.setAlpha(0.0f);
        ViewGroup viewGroup = this.mButtonContainer;
        viewGroup.setTranslationY((float) viewGroup.getHeight());
        this.mRootView.postDelayed(new Runnable() {
            public void run() {
                if (z) {
                    boolean unused = GlobalScreenshotDisplay.this.mIsTakingLongScreenshot = true;
                    boolean unused2 = GlobalScreenshotDisplay.this.mPendingContinueSnap = false;
                    boolean unused3 = GlobalScreenshotDisplay.this.mTakedTotalParts = false;
                    GlobalScreenshotDisplay.this.mScreenshotParts.clear();
                    GlobalScreenshot.beforeTakeScreenshot(GlobalScreenshotDisplay.this.mContext);
                    GlobalScreenshotDisplay.this.mWindowLayoutParams.flags |= 128;
                    GlobalScreenshotDisplay.this.mWindowManager.updateViewLayout(GlobalScreenshotDisplay.this.mRootView, GlobalScreenshotDisplay.this.mWindowLayoutParams);
                    GlobalScreenshotDisplay.this.mActionBarLayout.setVisibility(8);
                    GlobalScreenshotDisplay.this.mTopMsgDivider.setVisibility(0);
                    GlobalScreenshotDisplay.this.mTxtTopMsg.setText(R.string.long_screenshot_top_msg);
                    GlobalScreenshotDisplay.this.mTxtTopMsg.setVisibility(0);
                    GlobalScreenshotDisplay.this.mTxtTopMsg.setTranslationY((float) (-GlobalScreenshotDisplay.this.mTxtTopMsg.getHeight()));
                    GlobalScreenshotDisplay.this.mTxtTopMsg.animate().translationY(0.0f).start();
                    GlobalScreenshotDisplay.this.mButtonStopLongScreenshot.setText(R.string.long_screenshot_stop);
                    GlobalScreenshotDisplay.this.mButtonStopLongScreenshot.setEnabled(true);
                    GlobalScreenshotDisplay.this.mButtonStopLongScreenshot.setVisibility(0);
                    GlobalScreenshotDisplay.this.mScreenshotView.animate().setDuration(300).scaleX(1.0f).scaleY(1.0f).start();
                    GlobalScreenshotDisplay.this.mBackgroundView.animate().setDuration(300).alpha(1.0f).start();
                    GlobalScreenshotDisplay.this.mButtonContainer.animate().setDuration(300).translationY(0.0f).withEndAction(new Runnable() {
                        public void run() {
                            if (!GlobalScreenshotDisplay.this.startLongScreenshot()) {
                                GlobalScreenshotDisplay.this.exitTakingLongScreenshot(true);
                            }
                        }
                    }).start();
                    return;
                }
                GlobalScreenshotDisplay.this.mScreenshotView.animate().setDuration(300).scaleX(1.0f).scaleY(1.0f).start();
                GlobalScreenshotDisplay.this.mButtonContainer.animate().setDuration(300).translationY(0.0f).start();
                GlobalScreenshotDisplay.this.mBackgroundView.animate().setDuration(300).alpha(1.0f).start();
            }
        }, 20);
    }

    public void setIsScreenshotSaved() {
        this.mIsScreenshotSaved = true;
        Runnable runnable = this.mPendingSavedRunnable;
        if (runnable != null) {
            runnable.run();
            this.mPendingSavedRunnable = null;
        }
    }

    public boolean canLongScreenshot() {
        if (((KeyguardManager) this.mContext.getSystemService("keyguard")).isKeyguardLocked()) {
            return false;
        }
        IMiuiApplicationThread foregroundApplicationThread = getForegroundApplicationThread();
        if (foregroundApplicationThread == null) {
            Log.w("GlobalScreenshotDisplay", "getForegroundApplicationThread failed.");
            return false;
        }
        try {
            return foregroundApplicationThread.longScreenshot(1, this.mNavigationBarHeight);
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
    }

    private IMiuiApplicationThread getForegroundApplicationThread() {
        return ProcessManager.getForegroundApplicationThread();
    }

    /* access modifiers changed from: private */
    public void quit(boolean z, boolean z2) {
        if (this.mIsShow) {
            this.mIsShow = false;
            this.mScreenshotParts.clear();
            this.mContext.getApplicationContext().unregisterReceiver(this.mQuitReceiver);
            sendNavigationBarVisibilityChangeIfNeed(false);
            if (!z) {
                this.mWindowManager.removeView(this.mRootView);
                Thread.currentThread().setUncaughtExceptionHandler((Thread.UncaughtExceptionHandler) null);
            } else {
                if (!z2) {
                    this.mScreenshotView.animate().translationY((float) (-this.mRootView.getHeight()));
                } else {
                    ScreenshotScrollView screenshotScrollView = this.mScreenshotView;
                    screenshotScrollView.setPivotX((float) (screenshotScrollView.getWidth() / 2));
                    ScreenshotScrollView screenshotScrollView2 = this.mScreenshotView;
                    screenshotScrollView2.setPivotY((float) (screenshotScrollView2.getHeight() / 2));
                    this.mScreenshotView.animate().scaleX(0.7f).scaleY(0.7f).alpha(0.0f);
                }
                this.mScreenshotView.animate().setDuration(300).withEndAction(new Runnable() {
                    public void run() {
                        GlobalScreenshotDisplay.this.mWindowManager.removeView(GlobalScreenshotDisplay.this.mRootView);
                        Thread.currentThread().setUncaughtExceptionHandler((Thread.UncaughtExceptionHandler) null);
                    }
                }).start();
                this.mButtonContainer.animate().setDuration(300).translationY((float) this.mButtonContainer.getHeight()).start();
                this.mBackgroundView.animate().setDuration(300).alpha(0.0f).start();
            }
            GlobalScreenshot.notifyMediaAndFinish(this.mContext, this.mNotifyMediaStoreData);
        }
    }

    private void sendNavigationBarVisibilityChangeIfNeed(boolean z) {
        if (((KeyguardManager) this.mContext.getSystemService("keyguard")).isKeyguardLocked()) {
            Intent intent = new Intent("com.miui.lockscreen.navigation_bar_visibility");
            intent.putExtra("is_show", z);
            this.mContext.sendBroadcast(intent);
        }
    }

    /* access modifiers changed from: private */
    public boolean isPendingAction() {
        return this.mPendingSavedRunnable != null;
    }

    private void dismissKeyguardIfNeed() {
        if (((KeyguardManager) this.mContext.getSystemService("keyguard")).isKeyguardLocked()) {
            SystemUICompat.dismissKeyguardOnNextActivity();
        }
    }

    /* access modifiers changed from: private */
    public Bundle createQuitAnimationBundle() {
        return ActivityOptions.makeCustomAnimation(this.mContext, 0, 0, this.mHandler, new ActivityOptions.OnAnimationStartedListener() {
            public void onAnimationStarted() {
                GlobalScreenshotDisplay.this.mRootView.postDelayed(new Runnable() {
                    public void run() {
                        GlobalScreenshotDisplay.this.quit(false, false);
                    }
                }, 300);
            }
        }).toBundle();
    }

    /* access modifiers changed from: private */
    public void clickActionBtn(final String str) {
        if (!isPendingAction()) {
            if (this.mIsScreenshotSaved) {
                startPicActivity(str);
            } else {
                this.mPendingSavedRunnable = new Runnable() {
                    public void run() {
                        GlobalScreenshotDisplay.this.startPicActivity(str);
                    }
                };
            }
            if (TextUtils.equals(str, "send")) {
                HashMap hashMap = new HashMap();
                hashMap.put("interactive_event", MiStat.Event.SHARE);
                StatHelper.recordNewScreenshotEvent(this.mContext, "new_interactive_event_in_long_screenshot_page", hashMap);
            } else if (TextUtils.equals(str, "edit")) {
                HashMap hashMap2 = new HashMap();
                hashMap2.put("interactive_event", "edit");
                StatHelper.recordNewScreenshotEvent(this.mContext, "new_interactive_event_in_long_screenshot_page", hashMap2);
            }
        }
    }

    /* access modifiers changed from: private */
    public void startPicActivity(final String str) {
        dismissKeyguardIfNeed();
        GlobalScreenshot.notifyMediaAndFinish(this.mContext, this.mNotifyMediaStoreData, new GlobalScreenshot.ScreenshotFinishCallback() {
            public void onFinish() {
                if (!TextUtils.equals(str, "feedback")) {
                    if (TextUtils.equals(str, "edit")) {
                        GlobalScreenshotDisplay.this.mScreenshotView.resetToShortMode(true);
                    }
                    if (GlobalScreenshotDisplay.this.mNotifyMediaStoreData.outUri != null) {
                        Intent intent = new Intent();
                        intent.setPackage("com.miui.gallery");
                        intent.setData(GlobalScreenshotDisplay.this.mNotifyMediaStoreData.outUri);
                        if (TextUtils.equals(str, "edit")) {
                            intent.addFlags(268468224);
                            GlobalScreenshotDisplay globalScreenshotDisplay = GlobalScreenshotDisplay.this;
                            intent.setAction(globalScreenshotDisplay.checkEditAction(globalScreenshotDisplay.mNotifyMediaStoreData.outUri));
                            intent.putExtra("IsScreenshot", true);
                            intent.putExtra("IsLongScreenshot", GlobalScreenshotDisplay.this.mIsShowingLongScreenshot);
                            intent.putExtra("FromLongScreenshot", true);
                            intent.putExtra("screenshot_filepath", GlobalScreenshotDisplay.this.mNotifyMediaStoreData.imageFilePath);
                            intent.putExtra("StartActivityWhenLocked", true);
                            intent.putExtra("skip_interception", true);
                            Rect showRect = GlobalScreenshotDisplay.this.mScreenshotView.getShowRect();
                            intent.putExtra("ThumbnailRect", new int[]{showRect.left, showRect.top, showRect.width(), showRect.height()});
                            GlobalScreenshotDisplay.this.mContext.startActivity(intent, GlobalScreenshotDisplay.this.createQuitAnimationBundle());
                            return;
                        }
                        return;
                    }
                    GlobalScreenshotDisplay.this.quit(false, false);
                    Toast.makeText(GlobalScreenshotDisplay.this.mContext, GlobalScreenshotDisplay.this.mContext.getResources().getString(R.string.screenshot_insert_failed), 0).show();
                } else if (GlobalScreenshotDisplay.this.mNotifyMediaStoreData.outUri != null) {
                    Intent intent2 = new Intent("android.intent.action.SEND");
                    intent2.setClassName("com.miui.bugreport", "com.miui.bugreport.ui.FeedbackActivity");
                    intent2.putExtra("android.intent.extra.STREAM", GlobalScreenshotDisplay.this.mNotifyMediaStoreData.outUri);
                    intent2.setType("image/*");
                    intent2.addFlags(268468224);
                    GlobalScreenshotDisplay.this.mContext.startActivity(intent2, GlobalScreenshotDisplay.this.createQuitAnimationBundle());
                }
            }
        });
    }

    public String checkEditAction(Uri uri) {
        Intent intent = new Intent();
        intent.setPackage("com.miui.gallery");
        intent.setAction("com.miui.gallery.intent.action.SCREEN_EDIT");
        intent.setData(uri);
        if (this.mContext.getPackageManager().resolveActivity(intent, 65536) != null) {
            return "com.miui.gallery.intent.action.SCREEN_EDIT";
        }
        return "android.intent.action.EDIT";
    }

    public String checkShareAction(Uri uri) {
        Intent intent = new Intent();
        intent.setPackage("com.miui.gallery");
        intent.setAction("com.miui.gallery.intent.action.SCREEN_EDIT");
        intent.setData(uri);
        if (this.mContext.getPackageManager().resolveActivity(intent, 65536) != null) {
            return "com.miui.gallery.intent.action.SCREEN_EDIT";
        }
        return "android.intent.action.VIEW";
    }

    /* access modifiers changed from: private */
    public boolean startLongScreenshot() {
        this.mForeAppThread = getForegroundApplicationThread();
        Log.d("GlobalScreenshotDisplay", "startLongScreenshot:" + this.mForeAppThread);
        if (this.mForeAppThread == null) {
            return false;
        }
        this.mScreenshotView.resetToShortMode(true);
        Bitmap screenshotForLong = getScreenshotForLong(this.mContext, true);
        try {
            if (!this.mForeAppThread.longScreenshot(2, this.mNavigationBarHeight)) {
                return false;
            }
            this.mLongScreenshotFirstPart = screenshotForLong;
            this.mContext.getApplicationContext().registerReceiverAsUser(this.mLongScreenshotReceiver, UserHandle.ALL, new IntentFilter("com.miui.util.LongScreenshotUtils.LongScreenshot"), (String) null, (Handler) null);
            return true;
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
    }

    /* access modifiers changed from: private */
    public void stopLongScreenshot(boolean z) {
        IMiuiApplicationThread iMiuiApplicationThread = this.mForeAppThread;
        if (iMiuiApplicationThread != null) {
            try {
                iMiuiApplicationThread.longScreenshot(4, this.mNavigationBarHeight);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        finishTakingLongScreenshot(z);
    }

    private Bitmap getScreenshotForLong(Context context, boolean z) {
        int i;
        if (z) {
            i = MiuiWindowManager.getLayer(context, 2000);
        } else {
            i = MiuiWindowManager.getLayer(context, b.m) - 1;
        }
        return ScreenshotUtils.getScreenshot(context, 1.0f, 0, i, MiuiSettings.Global.getBoolean(context.getContentResolver(), "force_fsg_nav_bar"));
    }

    static Bitmap cropBitmap(Bitmap bitmap, int i, int i2) {
        int[] iArr = null;
        if (i < 0 || i2 <= 0 || bitmap == null || i + i2 > bitmap.getHeight()) {
            return null;
        }
        int width = bitmap.getWidth();
        SoftReference<int[]> softReference = sPixelsCache;
        if (softReference != null) {
            iArr = softReference.get();
        }
        if (iArr == null || iArr.length != i2 * width) {
            iArr = new int[(i2 * width)];
            sPixelsCache = new SoftReference<>(iArr);
        }
        bitmap.getPixels(iArr, 0, width, 0, i, width, i2);
        return Bitmap.createBitmap(iArr, width, i2, Bitmap.Config.ARGB_8888);
    }

    /* access modifiers changed from: private */
    public void onCallbackReceive(final Intent intent) {
        final boolean booleanExtra = intent.getBooleanExtra("IsEnd", false);
        new AsyncTask<Intent, Void, Bitmap[]>() {
            /* access modifiers changed from: protected */
            public Bitmap[] doInBackground(Intent... intentArr) {
                return GlobalScreenshotDisplay.this.snapForLongScreenshot(intentArr[0]);
            }

            /* access modifiers changed from: protected */
            public void onPostExecute(Bitmap[] bitmapArr) {
                if (GlobalScreenshotDisplay.this.mIsTakingLongScreenshot && bitmapArr != null) {
                    boolean z = GlobalScreenshotDisplay.this.mScreenshotParts.size() == 0;
                    if (z) {
                        GlobalScreenshotDisplay.this.mScreenshotParts.add(GlobalScreenshotDisplay.cropBitmap(GlobalScreenshotDisplay.this.mLongScreenshotFirstPart, 0, intent.getIntExtra("BottomLoc", 0)));
                    }
                    if (bitmapArr.length > 0 && bitmapArr[0] != null) {
                        GlobalScreenshotDisplay.this.mScreenshotParts.add(bitmapArr[0]);
                    }
                    if (bitmapArr.length > 1) {
                        GlobalScreenshotDisplay.this.mScreenshotView.setBottomPart(bitmapArr[1]);
                    }
                    if (z) {
                        GlobalScreenshotDisplay.this.mScreenshotView.setSingleBitmap((Bitmap) null);
                        GlobalScreenshotDisplay.this.mScreenshotView.setBitmaps(GlobalScreenshotDisplay.this.mScreenshotParts, true);
                        GlobalScreenshotDisplay.this.mScreenshotView.setIsTakingLongScreenshot(true);
                        GlobalScreenshotDisplay.this.mScreenshotView.startAnimating();
                    } else {
                        GlobalScreenshotDisplay.this.mScreenshotView.setBitmaps(GlobalScreenshotDisplay.this.mScreenshotParts, false);
                    }
                    if (!booleanExtra) {
                        boolean unused = GlobalScreenshotDisplay.this.mPendingContinueSnap = true;
                        GlobalScreenshotDisplay.this.tryToContinueOrFinish();
                        return;
                    }
                    boolean unused2 = GlobalScreenshotDisplay.this.mTakedTotalParts = true;
                }
            }
        }.execute(new Intent[]{intent});
    }

    /* access modifiers changed from: private */
    public Bitmap[] snapForLongScreenshot(Intent intent) {
        Bitmap screenshotForLong = getScreenshotForLong(this.mContext, false);
        if (screenshotForLong == null) {
            return null;
        }
        boolean booleanExtra = intent.getBooleanExtra("IsEnd", false);
        int intExtra = intent.getIntExtra("TopLoc", 0);
        int intExtra2 = intent.getIntExtra("BottomLoc", 0) - intExtra;
        if (booleanExtra) {
            intExtra2 = screenshotForLong.getHeight() - intExtra;
        }
        Bitmap[] bitmapArr = new Bitmap[2];
        if (intExtra2 > 0) {
            bitmapArr[0] = cropBitmap(screenshotForLong, intExtra, intExtra2);
        }
        int intExtra3 = intent.getIntExtra("ViewBottom", 0);
        if (intExtra3 < screenshotForLong.getHeight() - 1) {
            bitmapArr[1] = cropBitmap(screenshotForLong, intExtra3, screenshotForLong.getHeight() - intExtra3);
        } else {
            bitmapArr[1] = null;
        }
        screenshotForLong.recycle();
        return bitmapArr;
    }

    /* access modifiers changed from: private */
    public void tryToContinueOrFinish() {
        if (this.mScreenshotView.getShowedPageCount() < this.mScreenshotParts.size() - 1) {
            return;
        }
        if (this.mPendingContinueSnap && this.mIsTakingLongScreenshot) {
            try {
                this.mForeAppThread.longScreenshot(3, this.mNavigationBarHeight);
                this.mPendingContinueSnap = false;
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else if (this.mTakedTotalParts && this.mScreenshotView.getShowedPageCount() == this.mScreenshotParts.size() && !this.mScreenshotView.getIsManuTaking()) {
            finishTakingLongScreenshot(false);
            this.mScreenshotView.setBottomPart((Bitmap) null);
            HashMap hashMap = new HashMap();
            hashMap.put("finish_ways", "auto_finish");
            StatHelper.recordNewScreenshotEvent(this.mContext, "new_finish_long_screenshot", hashMap);
        }
    }

    /* access modifiers changed from: private */
    public void finishTakingLongScreenshot(final boolean z) {
        if (z || this.mIsScreenshotSaved) {
            Log.d("GlobalScreenshotDisplay", "finishTakingLongScreenshot:" + this.mForeAppThread);
            if (this.mForeAppThread != null) {
                this.mScreenshotView.stopAnimating();
                this.mContext.getApplicationContext().unregisterReceiver(this.mLongScreenshotReceiver);
            }
            this.mButtonStopLongScreenshot.setEnabled(false);
            if (z) {
                exitTakingLongScreenshot(true);
                return;
            }
            this.mButtonStopLongScreenshot.setText(R.string.long_screenshot_processing);
            new AsyncTask<Void, Void, Bitmap>() {
                /* access modifiers changed from: protected */
                public Bitmap doInBackground(Void[] voidArr) {
                    try {
                        Bitmap buildLongScreenshot = GlobalScreenshotDisplay.this.mScreenshotView.buildLongScreenshot();
                        if (buildLongScreenshot != null) {
                            try {
                                int lastIndexOf = GlobalScreenshotDisplay.this.mNotifyMediaStoreData.imageFilePath.lastIndexOf("jpg");
                                if (lastIndexOf >= 0) {
                                    new File(GlobalScreenshotDisplay.this.mNotifyMediaStoreData.imageFilePath).delete();
                                    String str = GlobalScreenshotDisplay.this.mNotifyMediaStoreData.imageFilePath.substring(0, lastIndexOf) + "jpg";
                                    GlobalScreenshotDisplay.this.mNotifyMediaStoreData.imageFilePath = str;
                                    GlobalScreenshotDisplay.this.mNotifyMediaStoreData.imageFileName = new File(str).getName();
                                }
                                BitmapFactory.saveToFile(buildLongScreenshot, GlobalScreenshotDisplay.this.mNotifyMediaStoreData.imageFilePath, true);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            GlobalScreenshotDisplay.this.mNotifyMediaStoreData.width = buildLongScreenshot.getWidth();
                            GlobalScreenshotDisplay.this.mNotifyMediaStoreData.height = buildLongScreenshot.getHeight();
                            if (GlobalScreenshotDisplay.this.mNotifyMediaStoreData.outUri != null) {
                                long parseId = ContentUris.parseId(GlobalScreenshotDisplay.this.mNotifyMediaStoreData.outUri);
                                if (parseId > 0) {
                                    ContentValues contentValues = new ContentValues();
                                    ContentResolver contentResolver = GlobalScreenshotDisplay.this.mContext.getContentResolver();
                                    contentValues.put("_data", GlobalScreenshotDisplay.this.mNotifyMediaStoreData.imageFilePath);
                                    contentValues.put("_display_name", GlobalScreenshotDisplay.this.mNotifyMediaStoreData.imageFileName);
                                    contentValues.put("width", Integer.valueOf(GlobalScreenshotDisplay.this.mNotifyMediaStoreData.width));
                                    contentValues.put("height", Integer.valueOf(GlobalScreenshotDisplay.this.mNotifyMediaStoreData.height));
                                    contentValues.put("mime_type", "image/png");
                                    contentValues.put("_size", Long.valueOf(new File(GlobalScreenshotDisplay.this.mNotifyMediaStoreData.imageFilePath).length()));
                                    int update = contentResolver.update(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues, "_id=?", new String[]{String.valueOf(parseId)});
                                    if (update != 1) {
                                        Log.d("GlobalScreenshot", "update uri from photo abnormal : " + update);
                                        GlobalScreenshotDisplay.this.mNotifyMediaStoreData.outUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                                    }
                                }
                            }
                            int height = GlobalScreenshotDisplay.this.mLongScreenshotFirstPart.getHeight();
                            Bitmap createScaledBitmap = Bitmap.createScaledBitmap(buildLongScreenshot, (buildLongScreenshot.getWidth() * height) / buildLongScreenshot.getHeight(), height, true);
                            StatHelper.recordNumericPropertyEvent(GlobalScreenshotDisplay.this.mContext, "longscreenshot_height", (long) buildLongScreenshot.getHeight());
                            return createScaledBitmap;
                        }
                        Toast makeText = Toast.makeText(GlobalScreenshotDisplay.this.mContext, R.string.long_screenshot_out_of_memory_error, 0);
                        makeText.setType(2006);
                        makeText.show();
                        StatHelper.recordCountEvent(GlobalScreenshotDisplay.this.mContext, "longscreenshot_fail_height");
                        return null;
                    } catch (Exception e2) {
                        Log.w("GlobalScreenshotDisplay", "", e2);
                        return null;
                    }
                }

                /* access modifiers changed from: protected */
                public void onPostExecute(Bitmap bitmap) {
                    if (bitmap != null) {
                        Bitmap unused = GlobalScreenshotDisplay.this.mScreenshot = bitmap;
                        GlobalScreenshotDisplay.this.exitTakingLongScreenshot(false);
                        return;
                    }
                    GlobalScreenshotDisplay.this.exitTakingLongScreenshot(true);
                }
            }.execute(new Void[0]);
            return;
        }
        this.mPendingSavedRunnable = new Runnable() {
            public void run() {
                GlobalScreenshotDisplay.this.finishTakingLongScreenshot(z);
            }
        };
    }

    /* access modifiers changed from: private */
    public void exitTakingLongScreenshot(boolean z) {
        Log.d("GlobalScreenshotDisplay", "exitTakingLongScreenshot:" + z);
        this.mIsTakingLongScreenshot = false;
        this.mForeAppThread = null;
        Bitmap bitmap = this.mLongScreenshotFirstPart;
        if (bitmap != null) {
            bitmap.recycle();
            this.mLongScreenshotFirstPart = null;
        }
        GlobalScreenshot.afterTakeScreenshot(this.mContext);
        this.mWindowLayoutParams.flags &= -129;
        if (this.mRootView.getWindowToken() != null) {
            this.mWindowManager.updateViewLayout(this.mRootView, this.mWindowLayoutParams);
        }
        if (!z) {
            this.mIsShowingLongScreenshot = true;
        }
        this.mScreenshotView.setIsTakingLongScreenshot(false);
        this.mBackgroundView.setVisibility(0);
        this.mRootView.setVisibility(0);
        clickActionBtn("edit");
    }

    public void onShowedPageCountChanged(int i) {
        tryToContinueOrFinish();
    }

    public void doubleClickEventReaction(boolean z) {
        if (this.mContext.getResources().getConfiguration().orientation != 2) {
            return;
        }
        if (z) {
            this.mActionBarLayout.animate().alpha(0.0f).setDuration(200).start();
        } else {
            this.mActionBarLayout.animate().alpha(1.0f).setDuration(200).start();
        }
    }

    public void uncaughtException(Thread thread, Throwable th) {
        View view = this.mRootView;
        if (!(view == null || view.getWindowToken() == null)) {
            this.mWindowManager.removeViewImmediate(this.mRootView);
        }
        Thread.UncaughtExceptionHandler defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        if (defaultUncaughtExceptionHandler != null) {
            defaultUncaughtExceptionHandler.uncaughtException(thread, th);
        }
    }
}
