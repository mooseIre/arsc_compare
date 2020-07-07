package com.android.systemui.partialscreenshot;

import android.app.ActivityOptions;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.app.StatusBarManager;
import android.app.WindowManagerGlobalCompat;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.UserManagerCompat;
import android.os.Vibrator;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManagerCompat;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import com.airbnb.lottie.LottieAnimationView;
import com.android.systemui.content.pm.PackageManagerCompat;
import com.android.systemui.partialscreenshot.factory.EllipseFactory;
import com.android.systemui.partialscreenshot.factory.IrregularFactory;
import com.android.systemui.partialscreenshot.factory.RectFactory;
import com.android.systemui.partialscreenshot.factory.ShapeFactory;
import com.android.systemui.screenshot.IBitmapService;
import com.android.systemui.screenshot.IScreenShotCallback;
import com.miui.blur.drawable.BlurDrawable;
import com.miui.enterprise.RestrictionsHelper;
import java.lang.Thread;
import java.util.Collection;
import java.util.List;
import miui.util.ScreenshotUtils;
import miuix.animation.Folme;
import miuix.animation.IFolme;
import miuix.animation.IVisibleStyle;
import miuix.animation.base.AnimConfig;
import miuix.animation.listener.TransitionListener;
import miuix.animation.listener.UpdateInfo;

public class PartialScreenshot implements Thread.UncaughtExceptionHandler {
    /* access modifiers changed from: private */
    public int checkID;
    /* access modifiers changed from: private */
    public LottieAnimationView lottieAnimationView;
    private AnimConfig mAnimConfig;
    /* access modifiers changed from: private */
    public IBitmapService mBitmapService;
    /* access modifiers changed from: private */
    public BlurDrawable mBlurDrawable;
    private IFolme mBottomTaskmc;
    private ImageButton mCancelButton;
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName componentName, final IBinder iBinder) {
            PartialScreenshot.this.mGalleryHandler.post(new Runnable() {
                public void run() {
                    try {
                        IBitmapService unused = PartialScreenshot.this.mBitmapService = IBitmapService.Stub.asInterface(iBinder);
                        PartialScreenshot.this.mBitmapService.registerCallback(PartialScreenshot.this.mScreenShotCallback);
                    } catch (RemoteException e) {
                        Log.e("PartialScreenshot", "bitmap service register exception : " + e);
                    }
                }
            });
        }

        public void onServiceDisconnected(ComponentName componentName) {
            IBitmapService unused = PartialScreenshot.this.mBitmapService = null;
        }
    };
    /* access modifiers changed from: private */
    public Context mContext;
    private float[] mCornerRadii;
    /* access modifiers changed from: private */
    public float mDimensionPixelOffset;
    private BlurImageView mEditButton;
    private View mEditText;
    private RadioButton mEllipseButton;
    private IFolme mFolmeButtonEdit;
    private IFolme mFolmeButtonRepaint;
    private IFolme mFolmeButtonSave;
    private IFolme mFolmeButtonShare;
    /* access modifiers changed from: private */
    public Handler mGalleryHandler;
    private ImageView mGreyBackgroundView;
    /* access modifiers changed from: private */
    public Handler mHandler = new Handler();
    private HandlerThread mHandlerThread = new HandlerThread("screen_gallery_thread", 10);
    private RadioButton mIrregularButton;
    /* access modifiers changed from: private */
    public long mLastClickTime = 0;
    private NotificationManager mNotificationManager;
    /* access modifiers changed from: private */
    public PartialNotifyMediaStoreData mPartialNotifyMediaStoreData;
    /* access modifiers changed from: private */
    public PartialScreenshotView mPartialScreenshotView;
    private BroadcastReceiver mQuitReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            PartialScreenshot.this.saveDataInQuitOrException("false");
            PartialScreenshot.this.quit();
        }
    };
    private RadioButton mRectButton;
    private BlurImageView mRepaintButton;
    private View mRepaintText;
    private BlurImageView mSaveButton;
    private View mSaveText;
    private Bitmap mScreenBitmap;
    /* access modifiers changed from: private */
    public IScreenShotCallback.Stub mScreenShotCallback = new IScreenShotCallback.Stub() {
        public void quitThumnail() throws RemoteException {
            PartialScreenshot.this.mHandler.post(new Runnable() {
                public void run() {
                    PartialScreenshot.this.saveDataInQuitOrException("false");
                    PartialScreenshot.this.unBindPhotoService();
                    PartialScreenshot.this.quit();
                }
            });
        }
    };
    /* access modifiers changed from: private */
    public View mScreenshotLayout;
    private RadioGroup mShapeGroup;
    private BlurImageView mShareButton;
    private View mShareText;
    /* access modifiers changed from: private */
    public IFolme mTopTaskmc;
    private Vibrator mVibrator;
    /* access modifiers changed from: private */
    public WindowManager.LayoutParams mWindowLayoutParams;
    /* access modifiers changed from: private */
    public WindowManager mWindowManager;
    /* access modifiers changed from: private */
    public ShapeFactory shapeFactory;
    private Runnable shotFinisher;
    /* access modifiers changed from: private */
    public Rect trimmingFramerect;

    public interface PartialScreenshotFinishCallback {
        void onFinish();
    }

    public PartialScreenshot(Context context) {
        Context context2 = context;
        this.mContext = context2;
        this.mHandlerThread.start();
        this.mGalleryHandler = new Handler(this.mHandlerThread.getLooper());
        this.mScreenshotLayout = ((LayoutInflater) context2.getSystemService("layout_inflater")).inflate(R$layout.partial_screenshot, (ViewGroup) null);
        this.mPartialScreenshotView = (PartialScreenshotView) this.mScreenshotLayout.findViewById(R$id.partial_screenshot_selector);
        this.mShapeGroup = (RadioGroup) this.mScreenshotLayout.findViewById(R$id.rg_shape);
        this.lottieAnimationView = (LottieAnimationView) this.mScreenshotLayout.findViewById(R$id.lottie_ellipse);
        this.mEllipseButton = (RadioButton) this.mScreenshotLayout.findViewById(R$id.rbtn_ellipse);
        this.mIrregularButton = (RadioButton) this.mScreenshotLayout.findViewById(R$id.rbtn_irregular);
        this.mRectButton = (RadioButton) this.mScreenshotLayout.findViewById(R$id.rbtn_rect);
        this.mCancelButton = (ImageButton) this.mScreenshotLayout.findViewById(R$id.btn_cancel);
        this.mRepaintButton = (BlurImageView) this.mScreenshotLayout.findViewById(R$id.btn_repaint);
        this.mShareButton = (BlurImageView) this.mScreenshotLayout.findViewById(R$id.btn_share);
        this.mEditButton = (BlurImageView) this.mScreenshotLayout.findViewById(R$id.btn_edit);
        this.mRepaintText = this.mScreenshotLayout.findViewById(R$id.text_repaint);
        this.mShareText = this.mScreenshotLayout.findViewById(R$id.text_share);
        this.mEditText = this.mScreenshotLayout.findViewById(R$id.text_edit);
        this.mSaveText = this.mScreenshotLayout.findViewById(R$id.text_save);
        this.mSaveButton = (BlurImageView) this.mScreenshotLayout.findViewById(R$id.btn_save);
        this.mGreyBackgroundView = (ImageView) this.mScreenshotLayout.findViewById(R$id.window_Background);
        this.mScreenshotLayout.requestFocus();
        this.mDimensionPixelOffset = context.getResources().getDimension(R$dimen.screenshot_ball_width_height) / 2.0f;
        this.mBlurDrawable = new BlurDrawable();
        float f = this.mDimensionPixelOffset;
        this.mCornerRadii = new float[]{f, f, f, f};
        this.mBlurDrawable.setBlurCornerRadii(this.mCornerRadii);
        this.mBlurDrawable.setBlurRatio(0.6f);
        this.mSaveButton.setBackground(this.mBlurDrawable);
        this.mEditButton.setBackground(this.mBlurDrawable);
        this.mShareButton.setBackground(this.mBlurDrawable);
        this.mRepaintButton.setBackground(this.mBlurDrawable);
        this.mWindowLayoutParams = new WindowManager.LayoutParams(-1, -1, 0, 0, 2024, 17040640, -3);
        WindowManager.LayoutParams layoutParams = this.mWindowLayoutParams;
        layoutParams.windowAnimations = R$style.PartialTheme;
        WindowManagerCompat.setLayoutInDisplayCutoutMode(layoutParams, 1);
        this.mWindowLayoutParams.setTitle("PartialScreenshot");
        this.mWindowManager = (WindowManager) context2.getSystemService("window");
        this.mNotificationManager = (NotificationManager) context2.getSystemService("notification");
        this.mWindowLayoutParams.screenOrientation = 14;
        this.mFolmeButtonRepaint = Folme.useAt(this.mRepaintButton);
        this.mTopTaskmc = Folme.useAt(this.mCancelButton, this.mRectButton, this.mIrregularButton, this.mEllipseButton);
        this.mBottomTaskmc = Folme.useAt(this.mRepaintText, this.mShareText, this.mEditText, this.mSaveText);
        this.mFolmeButtonShare = Folme.useAt(this.mShareButton);
        this.mFolmeButtonEdit = Folme.useAt(this.mEditButton);
        this.mFolmeButtonSave = Folme.useAt(this.mSaveButton);
        this.mFolmeButtonRepaint.touch().handleTouchOf(this.mRepaintButton, new AnimConfig[0]);
        this.mFolmeButtonShare.touch().handleTouchOf(this.mShareButton, new AnimConfig[0]);
        this.mFolmeButtonEdit.touch().handleTouchOf(this.mEditButton, new AnimConfig[0]);
        this.mFolmeButtonSave.touch().handleTouchOf(this.mSaveButton, new AnimConfig[0]);
        initButtonListener();
        setForceDarkEnable();
        this.mPartialScreenshotView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == 3) {
                    PartialScreenshot.this.mTopTaskmc.visible().show(new AnimConfig[0]);
                    if (PartialScreenshot.this.shapeFactory.getState() == 2) {
                        PartialScreenshot.this.showBottomAnimation();
                    } else {
                        PartialScreenshot.this.lottieAnimationView.playAnimation();
                        PartialScreenshot.this.lottieAnimationView.setVisibility(0);
                    }
                }
                boolean onTouch = PartialScreenshot.this.shapeFactory.onTouch(view, motionEvent);
                int action = motionEvent.getAction();
                if (action == 0) {
                    PartialScreenshot.this.lottieAnimationView.setVisibility(4);
                    PartialScreenshot.this.lottieAnimationView.cancelAnimation();
                    PartialScreenshot.this.mTopTaskmc.visible().hide(new AnimConfig[0]);
                    if (PartialScreenshot.this.shapeFactory.getState() == 2) {
                        PartialScreenshot.this.hideBottomTaskAnimation();
                    }
                } else if (action == 1 || action == 6) {
                    PartialScreenshot.this.mTopTaskmc.visible().show(new AnimConfig[0]);
                    if (PartialScreenshot.this.shapeFactory.getState() == 2) {
                        PartialScreenshot.this.showBottomAnimation();
                    } else if (motionEvent.getPointerCount() == 1) {
                        PartialScreenshot.this.lottieAnimationView.playAnimation();
                        PartialScreenshot.this.lottieAnimationView.setVisibility(0);
                    }
                }
                return onTouch;
            }
        });
        this.mScreenshotLayout.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (i != 4) {
                    return false;
                }
                PartialScreenshot.this.saveDataInQuitOrException("false");
                PartialScreenshot.this.quit();
                return true;
            }
        });
        initShapeFactory();
        this.mVibrator = (Vibrator) context2.getSystemService("vibrator");
        Thread.currentThread().setUncaughtExceptionHandler(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.CLOSE_SYSTEM_DIALOGS");
        intentFilter.addAction("android.intent.action.SCREEN_OFF");
        intentFilter.addAction("android.intent.action.PHONE_STATE");
        context.getApplicationContext().registerReceiver(this.mQuitReceiver, intentFilter);
    }

    private void setForceDarkEnable() {
        try {
            this.mScreenshotLayout.getClass().getMethod("setForceDarkAllowed", new Class[]{Boolean.TYPE}).invoke(this.mScreenshotLayout, new Object[]{false});
        } catch (Exception unused) {
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:12:0x0030  */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x0068  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void initShapeFactory() {
        /*
            r4 = this;
            java.lang.String r0 = "sys.miui.screentshot.partial.shape"
            java.lang.String r1 = "rect"
            java.lang.String r0 = android.os.SystemProperties.get(r0, r1)
            int r1 = r0.hashCode()
            r2 = -1656480802(0xffffffff9d441bde, float:-2.595479E-21)
            r3 = 1
            if (r1 == r2) goto L_0x0023
            r2 = 1394188883(0x5319a253, float:6.5985334E11)
            if (r1 == r2) goto L_0x0019
            goto L_0x002d
        L_0x0019:
            java.lang.String r1 = "irregular"
            boolean r0 = r0.equals(r1)
            if (r0 == 0) goto L_0x002d
            r0 = r3
            goto L_0x002e
        L_0x0023:
            java.lang.String r1 = "ellipse"
            boolean r0 = r0.equals(r1)
            if (r0 == 0) goto L_0x002d
            r0 = 0
            goto L_0x002e
        L_0x002d:
            r0 = -1
        L_0x002e:
            if (r0 == 0) goto L_0x0068
            if (r0 == r3) goto L_0x004d
            com.android.systemui.partialscreenshot.factory.RectFactory r0 = new com.android.systemui.partialscreenshot.factory.RectFactory
            r0.<init>()
            r4.shapeFactory = r0
            android.widget.RadioButton r0 = r4.mRectButton
            r0.setChecked(r3)
            com.airbnb.lottie.LottieAnimationView r0 = r4.lottieAnimationView
            java.lang.String r1 = "image_rect"
            r0.setImageAssetsFolder(r1)
            com.airbnb.lottie.LottieAnimationView r0 = r4.lottieAnimationView
            int r1 = com.android.systemui.partialscreenshot.R$raw.rect
            r0.setAnimation((int) r1)
            goto L_0x0082
        L_0x004d:
            com.android.systemui.partialscreenshot.factory.IrregularFactory r0 = new com.android.systemui.partialscreenshot.factory.IrregularFactory
            r0.<init>()
            r4.shapeFactory = r0
            android.widget.RadioButton r0 = r4.mIrregularButton
            r0.setChecked(r3)
            com.airbnb.lottie.LottieAnimationView r0 = r4.lottieAnimationView
            java.lang.String r1 = "image_irregular"
            r0.setImageAssetsFolder(r1)
            com.airbnb.lottie.LottieAnimationView r0 = r4.lottieAnimationView
            int r1 = com.android.systemui.partialscreenshot.R$raw.irregular
            r0.setAnimation((int) r1)
            goto L_0x0082
        L_0x0068:
            com.android.systemui.partialscreenshot.factory.EllipseFactory r0 = new com.android.systemui.partialscreenshot.factory.EllipseFactory
            r0.<init>()
            r4.shapeFactory = r0
            android.widget.RadioButton r0 = r4.mEllipseButton
            r0.setChecked(r3)
            com.airbnb.lottie.LottieAnimationView r0 = r4.lottieAnimationView
            java.lang.String r1 = "image_ellipse"
            r0.setImageAssetsFolder(r1)
            com.airbnb.lottie.LottieAnimationView r0 = r4.lottieAnimationView
            int r1 = com.android.systemui.partialscreenshot.R$raw.ellipse
            r0.setAnimation((int) r1)
        L_0x0082:
            com.airbnb.lottie.LottieAnimationView r4 = r4.lottieAnimationView
            r4.playAnimation()
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.partialscreenshot.PartialScreenshot.initShapeFactory():void");
    }

    public void takePartialScreenshot(Runnable runnable) {
        if (Build.VERSION.SDK_INT < 17 || (!"trigger_restart_min_framework".equals(SystemProperties.get("vold.decrypt")) && UserManagerCompat.isUserUnlocked((UserManager) this.mContext.getSystemService(UserManager.class)))) {
            if (!(Settings.Global.getInt(this.mContext.getContentResolver(), "device_provisioned", 0) != 0)) {
                Log.w("PartialScreenshot", "Can not screenshot when device not provisioned");
                if (runnable != null) {
                    runnable.run();
                }
            } else if (RestrictionsHelper.hasRestriction(this.mContext, "disallow_screencapture", UserHandle.myUserId())) {
                Log.w("PartialScreenshot", "Can not screenshot for enterprise forbidden.");
                if (runnable != null) {
                    runnable.run();
                }
            } else {
                this.mScreenBitmap = ScreenshotUtils.getScreenshot(this.mContext);
                Bitmap bitmap = this.mScreenBitmap;
                if (bitmap == null) {
                    Log.e("PartialScreenshot", "mScreenBitmap == null");
                    if (runnable != null) {
                        runnable.run();
                    }
                    notifyPartialScreenshotError(this.mContext, this.mNotificationManager, R$string.screenshot_failed_to_capture_title);
                    return;
                }
                this.shotFinisher = runnable;
                this.mScreenBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, false);
                bitmap.recycle();
                this.mGreyBackgroundView.setImageBitmap(this.mScreenBitmap);
                this.mGreyBackgroundView.setVisibility(0);
                this.mScreenBitmap.setHasAlpha(false);
                this.mScreenBitmap.prepareToDraw();
                saveDataInQuitOrException("true");
                this.mVibrator.vibrate(80);
                this.mWindowManager.addView(this.mScreenshotLayout, this.mWindowLayoutParams);
            }
        } else {
            Log.w("PartialScreenshot", "Can not screenshot when decrypt state.");
            if (runnable != null) {
                runnable.run();
            }
        }
    }

    /* access modifiers changed from: private */
    public void showBottomAnimation() {
        IVisibleStyle visible = this.mBottomTaskmc.visible();
        visible.setScale(0.6f, IVisibleStyle.VisibleType.HIDE);
        visible.setScale(1.0f, IVisibleStyle.VisibleType.SHOW);
        visible.setHide();
        visible.show(new AnimConfig[0]);
        IVisibleStyle visible2 = this.mFolmeButtonShare.visible();
        visible2.setScale(0.6f, IVisibleStyle.VisibleType.HIDE);
        visible2.setScale(1.0f, IVisibleStyle.VisibleType.SHOW);
        visible2.setHide();
        visible2.show(this.mAnimConfig);
        IVisibleStyle visible3 = this.mFolmeButtonRepaint.visible();
        visible3.setScale(0.6f, IVisibleStyle.VisibleType.HIDE);
        visible3.setScale(1.0f, IVisibleStyle.VisibleType.SHOW);
        visible3.setHide();
        visible3.show(this.mAnimConfig);
        IVisibleStyle visible4 = this.mFolmeButtonEdit.visible();
        visible4.setScale(0.6f, IVisibleStyle.VisibleType.HIDE);
        visible4.setScale(1.0f, IVisibleStyle.VisibleType.SHOW);
        visible4.setHide();
        visible4.show(this.mAnimConfig);
        IVisibleStyle visible5 = this.mFolmeButtonSave.visible();
        visible5.setScale(0.6f, IVisibleStyle.VisibleType.HIDE);
        visible5.setScale(1.0f, IVisibleStyle.VisibleType.SHOW);
        visible5.setHide();
        visible5.show(this.mAnimConfig);
    }

    /* access modifiers changed from: private */
    public void hideBottomTaskAnimation() {
        IVisibleStyle visible = this.mBottomTaskmc.visible();
        visible.setShow();
        visible.hide(new AnimConfig[0]);
        IVisibleStyle visible2 = this.mFolmeButtonRepaint.visible();
        visible2.setShow();
        visible2.hide(this.mAnimConfig);
        IVisibleStyle visible3 = this.mFolmeButtonShare.visible();
        visible3.setShow();
        visible3.hide(this.mAnimConfig);
        IVisibleStyle visible4 = this.mFolmeButtonEdit.visible();
        visible4.setShow();
        visible4.hide(this.mAnimConfig);
        IVisibleStyle visible5 = this.mFolmeButtonSave.visible();
        visible5.setShow();
        visible5.hide(this.mAnimConfig);
    }

    private void initButtonListener() {
        AnimConfig animConfig = new AnimConfig();
        animConfig.addListeners(new TransitionListener() {
            public void onUpdate(Object obj, Collection<UpdateInfo> collection) {
                for (UpdateInfo next : collection) {
                    if ("autoAlpha".equals(next.property.getName())) {
                        float floatValue = next.getFloatValue();
                        if (PartialScreenshot.this.mBlurDrawable != null) {
                            PartialScreenshot.this.mBlurDrawable.setAlpha((int) (floatValue * 255.0f));
                        }
                    }
                    if ("scaleY".equals(next.property.getName())) {
                        float floatValue2 = next.getFloatValue();
                        if (PartialScreenshot.this.mBlurDrawable != null) {
                            float access$1300 = floatValue2 * PartialScreenshot.this.mDimensionPixelOffset;
                            PartialScreenshot.this.mBlurDrawable.setBlurCornerRadii(new float[]{access$1300, access$1300, access$1300, access$1300});
                        }
                    }
                }
            }
        });
        this.mAnimConfig = animConfig;
        this.mCancelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                PartialScreenshot.this.saveDataInQuitOrException("false");
                PartialScreenshot.this.quit();
            }
        });
        this.mRepaintButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                PartialScreenshot.this.hideBottomTaskAnimation();
                PartialScreenshot.this.mPartialScreenshotView.clear();
                PartialScreenshot.this.lottieAnimationView.cancelAnimation();
                if ((PartialScreenshot.this.shapeFactory instanceof RectFactory) && PartialScreenshot.this.checkID != R$id.rbtn_irregular) {
                    ShapeFactory unused = PartialScreenshot.this.shapeFactory = new RectFactory();
                    PartialScreenshot.this.lottieAnimationView.setImageAssetsFolder("image_rect");
                    PartialScreenshot.this.lottieAnimationView.setAnimation(R$raw.rect);
                } else if (PartialScreenshot.this.shapeFactory instanceof EllipseFactory) {
                    ShapeFactory unused2 = PartialScreenshot.this.shapeFactory = new EllipseFactory();
                    PartialScreenshot.this.lottieAnimationView.setImageAssetsFolder("image_ellipse");
                    PartialScreenshot.this.lottieAnimationView.setAnimation(R$raw.ellipse);
                } else {
                    ShapeFactory unused3 = PartialScreenshot.this.shapeFactory = new IrregularFactory();
                    PartialScreenshot.this.lottieAnimationView.setImageAssetsFolder("image_irregular");
                    PartialScreenshot.this.lottieAnimationView.setAnimation(R$raw.irregular);
                }
                PartialScreenshot.this.lottieAnimationView.playAnimation();
                PartialScreenshot.this.lottieAnimationView.setVisibility(0);
            }
        });
        this.mShareButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                PartialScreenshot.this.mWindowLayoutParams.windowAnimations = R$style.QuitWindowTheme;
                long currentTimeMillis = System.currentTimeMillis();
                if (currentTimeMillis - PartialScreenshot.this.mLastClickTime > 60000) {
                    if (PartialScreenshot.this.shapeFactory.getTrimmingFrame() != null) {
                        PartialScreenshot partialScreenshot = PartialScreenshot.this;
                        Rect unused = partialScreenshot.trimmingFramerect = partialScreenshot.shapeFactory.getTrimmingFrame();
                        PartialScreenshot.this.checkBindPhotoService();
                        PartialScreenshot.this.saveScreenshotInWorkerThread(new Runnable() {
                            public void run() {
                                PartialScreenshot.this.saveDataInQuitOrException("false");
                                PartialScreenshot.this.mWindowLayoutParams.screenOrientation = -1;
                                PartialScreenshot.this.mWindowManager.updateViewLayout(PartialScreenshot.this.mScreenshotLayout, PartialScreenshot.this.mWindowLayoutParams);
                                PartialScreenshot partialScreenshot = PartialScreenshot.this;
                                partialScreenshot.jumpProcess(partialScreenshot.mContext, PartialScreenshot.this.mPartialNotifyMediaStoreData, "send");
                            }
                        });
                    }
                    long unused2 = PartialScreenshot.this.mLastClickTime = currentTimeMillis;
                }
            }
        });
        this.mEditButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                PartialScreenshot.this.mWindowLayoutParams.windowAnimations = R$style.QuitWindowTheme;
                long currentTimeMillis = System.currentTimeMillis();
                if (currentTimeMillis - PartialScreenshot.this.mLastClickTime > 60000) {
                    if (PartialScreenshot.this.shapeFactory.getTrimmingFrame() != null) {
                        PartialScreenshot partialScreenshot = PartialScreenshot.this;
                        Rect unused = partialScreenshot.trimmingFramerect = partialScreenshot.shapeFactory.getTrimmingFrame();
                        PartialScreenshot.this.checkBindPhotoService();
                        PartialScreenshot.this.saveScreenshotInWorkerThread(new Runnable() {
                            public void run() {
                                PartialScreenshot.this.saveDataInQuitOrException("false");
                                PartialScreenshot.this.mWindowLayoutParams.screenOrientation = -1;
                                PartialScreenshot.this.mWindowManager.updateViewLayout(PartialScreenshot.this.mScreenshotLayout, PartialScreenshot.this.mWindowLayoutParams);
                                PartialScreenshot partialScreenshot = PartialScreenshot.this;
                                partialScreenshot.jumpProcess(partialScreenshot.mContext, PartialScreenshot.this.mPartialNotifyMediaStoreData, "edit");
                            }
                        });
                    }
                    long unused2 = PartialScreenshot.this.mLastClickTime = currentTimeMillis;
                }
            }
        });
        this.mSaveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                long currentTimeMillis = System.currentTimeMillis();
                if (currentTimeMillis - PartialScreenshot.this.mLastClickTime > 60000) {
                    PartialScreenshot.this.saveScreenshotInWorkerThread(new Runnable() {
                        public void run() {
                            PartialScreenshot.this.saveDataInQuitOrException("false");
                            PartialScreenshot.notifyPartialMediaAndFinish(PartialScreenshot.this.mContext, PartialScreenshot.this.mPartialNotifyMediaStoreData);
                            Toast.makeText(PartialScreenshot.this.mContext, R$string.partial_screenshot_save_successful, 0).show();
                            PartialScreenshot.this.quit();
                        }
                    });
                    long unused = PartialScreenshot.this.mLastClickTime = currentTimeMillis;
                }
            }
        });
        this.mShapeGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                int unused = PartialScreenshot.this.checkID = i;
                PartialScreenshot.this.lottieAnimationView.cancelAnimation();
                if (i == R$id.rbtn_irregular) {
                    PartialScreenshot.this.saveShapeForNext("irregular");
                    if (PartialScreenshot.this.shapeFactory.getState() == 2) {
                        Rect trimmingFrame = PartialScreenshot.this.shapeFactory.getTrimmingFrame();
                        ShapeFactory unused2 = PartialScreenshot.this.shapeFactory = new RectFactory();
                        PartialScreenshot.this.shapeFactory.notifyShapeChanged(trimmingFrame, PartialScreenshot.this.mPartialScreenshotView);
                        return;
                    }
                    ShapeFactory unused3 = PartialScreenshot.this.shapeFactory = new IrregularFactory();
                    PartialScreenshot.this.lottieAnimationView.setImageAssetsFolder("image_irregular");
                    PartialScreenshot.this.lottieAnimationView.setAnimation(R$raw.irregular);
                    PartialScreenshot.this.lottieAnimationView.playAnimation();
                } else if (i == R$id.rbtn_rect) {
                    PartialScreenshot.this.saveShapeForNext("rect");
                    if (PartialScreenshot.this.shapeFactory.getState() == 2) {
                        Rect trimmingFrame2 = PartialScreenshot.this.shapeFactory.getTrimmingFrame();
                        ShapeFactory unused4 = PartialScreenshot.this.shapeFactory = new RectFactory();
                        PartialScreenshot.this.shapeFactory.notifyShapeChanged(trimmingFrame2, PartialScreenshot.this.mPartialScreenshotView);
                        return;
                    }
                    ShapeFactory unused5 = PartialScreenshot.this.shapeFactory = new RectFactory();
                    PartialScreenshot.this.lottieAnimationView.setImageAssetsFolder("image_rect");
                    PartialScreenshot.this.lottieAnimationView.setAnimation(R$raw.rect);
                    PartialScreenshot.this.lottieAnimationView.playAnimation();
                } else if (i == R$id.rbtn_ellipse) {
                    PartialScreenshot.this.saveShapeForNext("ellipse");
                    if (PartialScreenshot.this.shapeFactory.getState() == 2) {
                        Rect trimmingFrame3 = PartialScreenshot.this.shapeFactory.getTrimmingFrame();
                        ShapeFactory unused6 = PartialScreenshot.this.shapeFactory = new EllipseFactory();
                        PartialScreenshot.this.shapeFactory.notifyShapeChanged(trimmingFrame3, PartialScreenshot.this.mPartialScreenshotView);
                        return;
                    }
                    ShapeFactory unused7 = PartialScreenshot.this.shapeFactory = new EllipseFactory();
                    PartialScreenshot.this.lottieAnimationView.setImageAssetsFolder("image_ellipse");
                    PartialScreenshot.this.lottieAnimationView.setAnimation(R$raw.ellipse);
                    PartialScreenshot.this.lottieAnimationView.playAnimation();
                }
            }
        });
    }

    /* access modifiers changed from: private */
    public void saveDataInQuitOrException(String str) {
        try {
            SystemProperties.set("sys.miui.screenshot.partial", str);
        } catch (RuntimeException e) {
            Log.e("PartialScreenshot", "RuntimeException when setprop", e);
        }
    }

    /* access modifiers changed from: private */
    public void saveShapeForNext(String str) {
        try {
            SystemProperties.set("sys.miui.screentshot.partial.shape", str);
        } catch (RuntimeException e) {
            Log.e("PartialScreenshot", "RuntimeException when setprop", e);
        }
    }

    /* access modifiers changed from: private */
    public void quit() {
        Runnable runnable = this.shotFinisher;
        if (runnable != null) {
            runnable.run();
        }
        if (this.mQuitReceiver != null) {
            this.mContext.getApplicationContext().unregisterReceiver(this.mQuitReceiver);
            this.mQuitReceiver = null;
        }
        View view = this.mScreenshotLayout;
        if (!(view == null || view.getWindowToken() == null)) {
            this.mWindowManager.removeView(this.mScreenshotLayout);
        }
        Thread.currentThread().setUncaughtExceptionHandler((Thread.UncaughtExceptionHandler) null);
    }

    static void notifyPartialScreenshotError(Context context, NotificationManager notificationManager, int i) {
        notificationManager.cancel(789);
        Toast.makeText(context, i, 0).show();
    }

    /* access modifiers changed from: private */
    public void saveScreenshotInWorkerThread(Runnable runnable) {
        SavePartialImageInBackgroundData savePartialImageInBackgroundData = new SavePartialImageInBackgroundData();
        savePartialImageInBackgroundData.context = this.mContext;
        savePartialImageInBackgroundData.image = this.shapeFactory.getPartialBitmap(this.mScreenBitmap);
        savePartialImageInBackgroundData.finisher = runnable;
        SavePartialImageInBackgroundTask savePartialImageInBackgroundTask = new SavePartialImageInBackgroundTask(this.mContext, savePartialImageInBackgroundData, this.mNotificationManager);
        savePartialImageInBackgroundTask.execute(new SavePartialImageInBackgroundData[]{savePartialImageInBackgroundData});
        this.mPartialNotifyMediaStoreData = savePartialImageInBackgroundTask.mNotifyMediaStoreData;
    }

    public static void notifyPartialMediaAndFinish(Context context, PartialNotifyMediaStoreData partialNotifyMediaStoreData) {
        notifyPartialMediaAndFinish(context, partialNotifyMediaStoreData, (PartialScreenshotFinishCallback) null);
    }

    public static void notifyPartialMediaAndFinish(final Context context, final PartialNotifyMediaStoreData partialNotifyMediaStoreData, final PartialScreenshotFinishCallback partialScreenshotFinishCallback) {
        if (partialNotifyMediaStoreData != null && !partialNotifyMediaStoreData.isRunned) {
            if (!partialNotifyMediaStoreData.saveFinished) {
                partialNotifyMediaStoreData.isPending = true;
                partialNotifyMediaStoreData.finishCallback = partialScreenshotFinishCallback;
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
                    intent.putExtra("extra_file_path", partialNotifyMediaStoreData.imageFilePath);
                    context.sendBroadcastAsUser(intent, UserHandle.CURRENT);
                    return null;
                }

                /* access modifiers changed from: protected */
                public void onPostExecute(Void voidR) {
                    super.onPostExecute(voidR);
                    partialNotifyMediaStoreData.isRunned = true;
                    PartialScreenshotFinishCallback partialScreenshotFinishCallback = partialScreenshotFinishCallback;
                    if (partialScreenshotFinishCallback != null) {
                        partialScreenshotFinishCallback.onFinish();
                    }
                    PartialScreenshotFinishCallback partialScreenshotFinishCallback2 = partialNotifyMediaStoreData.finishCallback;
                    if (partialScreenshotFinishCallback2 != null) {
                        partialScreenshotFinishCallback2.onFinish();
                    }
                }
            }.execute(new Void[0]);
        }
    }

    /* access modifiers changed from: private */
    public void jumpProcess(final Context context, final PartialNotifyMediaStoreData partialNotifyMediaStoreData, final String str) {
        StatusBarManager statusBarManager = (StatusBarManager) this.mContext.getSystemService("statusbar");
        if (statusBarManager != null) {
            statusBarManager.collapsePanels();
        }
        dismissKeyguardIfNeed();
        notifyPartialMediaAndFinish(context, partialNotifyMediaStoreData, new PartialScreenshotFinishCallback() {
            public void onFinish() {
                if (partialNotifyMediaStoreData.outUri != null) {
                    Intent intent = new Intent();
                    intent.setPackage("com.miui.gallery");
                    intent.setData(partialNotifyMediaStoreData.outUri);
                    intent.addFlags(268468224);
                    intent.putExtra("StartActivityWhenLocked", true);
                    intent.putExtra("skip_interception", true);
                    intent.putExtra("from_partial_screenshot", true);
                    intent.putExtra("ThumbnailRect", new int[]{PartialScreenshot.this.trimmingFramerect.left, PartialScreenshot.this.trimmingFramerect.top, PartialScreenshot.this.trimmingFramerect.width(), PartialScreenshot.this.trimmingFramerect.height()});
                    if (TextUtils.equals(str, "send")) {
                        intent.setAction(PartialScreenshot.this.checkShareAction(partialNotifyMediaStoreData.outUri));
                        intent.putExtra("com.miui.gallery.extra.photo_enter_choice_mode", true);
                        intent.putExtra("com.miui.gallery.extra.sync_load_intent_data", true);
                        intent.putExtra("com.miui.gallery.extra.show_menu_after_choice_mode", true);
                        intent.putExtra("is_from_send", true);
                        context.startActivity(intent, PartialScreenshot.this.createQuitAnimationBundle());
                    } else if (TextUtils.equals(str, "edit")) {
                        intent.setAction(PartialScreenshot.this.checkEditAction(partialNotifyMediaStoreData.outUri));
                        intent.putExtra("IsScreenshot", true);
                        intent.putExtra("IsLongScreenshot", false);
                        intent.putExtra("screenshot_filepath", partialNotifyMediaStoreData.imageFilePath);
                        context.startActivity(intent, PartialScreenshot.this.createQuitAnimationBundle());
                    }
                } else {
                    Context context = context;
                    Toast.makeText(context, context.getResources().getString(R$string.screenshot_insert_failed), 0).show();
                }
            }
        });
    }

    /* access modifiers changed from: private */
    public Bundle createQuitAnimationBundle() {
        return ActivityOptions.makeCustomAnimation(this.mContext, 0, 0, this.mHandler, (ActivityOptions.OnAnimationStartedListener) null).toBundle();
    }

    private void dismissKeyguardIfNeed() {
        if (((KeyguardManager) this.mContext.getSystemService("keyguard")).isKeyguardLocked()) {
            WindowManagerGlobalCompat.dismissKeyguardOnNextActivity();
        }
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
    public void checkBindPhotoService() {
        if (this.mBitmapService == null) {
            Intent intent = new Intent();
            intent.setPackage("com.miui.gallery");
            intent.setAction("com.miui.gallery.action.SCREENSHOT");
            this.mContext.getApplicationContext().bindService(intent, this.mConnection, 1);
        }
    }

    /* access modifiers changed from: private */
    public void unBindPhotoService() {
        IBitmapService iBitmapService = this.mBitmapService;
        if (iBitmapService != null) {
            try {
                iBitmapService.unregisterCallback(this.mScreenShotCallback);
            } catch (RemoteException e) {
                SystemProperties.set("sys.miui.screenshot.partial", "false");
                Log.e("PartialScreenshot", "bitmap service register exception : " + e);
            }
            this.mContext.getApplicationContext().unbindService(this.mConnection);
            this.mBitmapService = null;
        }
    }

    public void uncaughtException(Thread thread, Throwable th) {
        Log.d("PartialScreenshot", "uncaughtException : thread = " + thread + " = " + thread.getName());
        StringBuilder sb = new StringBuilder();
        sb.append("uncaughtException : ");
        sb.append(th);
        Log.d("PartialScreenshot", sb.toString());
        saveDataInQuitOrException("false");
        Runnable runnable = this.shotFinisher;
        if (runnable != null) {
            runnable.run();
        }
        Thread.UncaughtExceptionHandler defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        if (defaultUncaughtExceptionHandler != null) {
            defaultUncaughtExceptionHandler.uncaughtException(thread, th);
        }
    }
}
