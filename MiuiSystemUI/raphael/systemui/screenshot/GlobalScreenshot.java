package com.android.systemui.screenshot;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Insets;
import android.graphics.Outline;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.LayerDrawable;
import android.media.MediaActionSound;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.MathUtils;
import android.util.Slog;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceControl;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewTreeObserver;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.android.internal.logging.UiEventLogger;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0017R$layout;
import com.android.systemui.C0021R$string;
import com.android.systemui.screenshot.GlobalScreenshot;
import com.android.systemui.shared.system.ActivityManagerWrapper;
import com.android.systemui.shared.system.QuickStepContract;
import com.android.systemui.statusbar.phone.StatusBar;
import dagger.Lazy;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

public class GlobalScreenshot implements ViewTreeObserver.OnComputeInternalInsetsListener {
    private final Interpolator mAccelerateInterpolator = new AccelerateInterpolator();
    private HorizontalScrollView mActionsContainer;
    private ImageView mActionsContainerBackground;
    private LinearLayout mActionsView;
    private ImageView mBackgroundProtection;
    private MediaActionSound mCameraSound;
    private final Context mContext;
    private float mCornerSizeX;
    private boolean mDirectionLTR;
    private Animator mDismissAnimation;
    private FrameLayout mDismissButton;
    private float mDismissDeltaY;
    private final Display mDisplay;
    private final DisplayMetrics mDisplayMetrics;
    private final Interpolator mFastOutSlowIn;
    private boolean mInDarkMode = false;
    private int mLeftInset;
    private int mNavMode;
    private final ScreenshotNotificationsController mNotificationsController;
    private Runnable mOnCompleteRunnable;
    private boolean mOrientationPortrait;
    private int mRightInset;
    private SaveImageInBackgroundTask mSaveInBgTask;
    private Bitmap mScreenBitmap;
    private ImageView mScreenshotAnimatedView;
    private Animator mScreenshotAnimation;
    private ImageView mScreenshotFlash;
    private final Handler mScreenshotHandler;
    private View mScreenshotLayout;
    private ImageView mScreenshotPreview;
    private ScreenshotSelectorView mScreenshotSelectorView;
    private final UiEventLogger mUiEventLogger;
    private final WindowManager.LayoutParams mWindowLayoutParams;
    private final WindowManager mWindowManager;

    /* access modifiers changed from: package-private */
    public static class SaveImageInBackgroundData {
        public int errorMsgResId;
        public Consumer<Uri> finisher;
        public Bitmap image;
        public ActionsReadyListener mActionsReadyListener;

        SaveImageInBackgroundData() {
        }

        /* access modifiers changed from: package-private */
        public void clearImage() {
            this.image = null;
        }
    }

    /* access modifiers changed from: package-private */
    public static class SavedImageData {
        public Notification.Action deleteAction;
        public Notification.Action editAction;
        public Notification.Action shareAction;
        public List<Notification.Action> smartActions;
        public Uri uri;

        SavedImageData() {
        }

        public void reset() {
            this.uri = null;
            this.shareAction = null;
            this.editAction = null;
            this.smartActions = null;
        }
    }

    /* access modifiers changed from: package-private */
    public static abstract class ActionsReadyListener {
        /* access modifiers changed from: package-private */
        public abstract void onActionsReady(SavedImageData savedImageData);

        ActionsReadyListener() {
        }
    }

    public GlobalScreenshot(Context context, Resources resources, ScreenshotNotificationsController screenshotNotificationsController, UiEventLogger uiEventLogger) {
        boolean z = true;
        this.mDirectionLTR = true;
        this.mOrientationPortrait = true;
        this.mScreenshotHandler = new Handler(Looper.getMainLooper()) {
            /* class com.android.systemui.screenshot.GlobalScreenshot.AnonymousClass1 */

            public void handleMessage(Message message) {
                if (message.what == 2) {
                    GlobalScreenshot.this.mUiEventLogger.log(ScreenshotEvent.SCREENSHOT_INTERACTION_TIMEOUT);
                    GlobalScreenshot.this.dismissScreenshot("timeout", false);
                    GlobalScreenshot.this.mOnCompleteRunnable.run();
                }
            }
        };
        this.mContext = context;
        this.mNotificationsController = screenshotNotificationsController;
        this.mUiEventLogger = uiEventLogger;
        reloadAssets();
        Configuration configuration = this.mContext.getResources().getConfiguration();
        this.mInDarkMode = configuration.isNightModeActive();
        this.mDirectionLTR = configuration.getLayoutDirection() == 0;
        this.mOrientationPortrait = configuration.orientation != 1 ? false : z;
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(-1, -1, 0, 0, 2036, 918816, -3);
        this.mWindowLayoutParams = layoutParams;
        layoutParams.setTitle("ScreenshotAnimation");
        WindowManager.LayoutParams layoutParams2 = this.mWindowLayoutParams;
        layoutParams2.layoutInDisplayCutoutMode = 3;
        layoutParams2.setFitInsetsTypes(0);
        WindowManager windowManager = (WindowManager) context.getSystemService("window");
        this.mWindowManager = windowManager;
        this.mDisplay = windowManager.getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.mDisplayMetrics = displayMetrics;
        this.mDisplay.getRealMetrics(displayMetrics);
        this.mCornerSizeX = (float) resources.getDimensionPixelSize(C0012R$dimen.global_screenshot_x_scale);
        this.mDismissDeltaY = (float) resources.getDimensionPixelSize(C0012R$dimen.screenshot_dismissal_height_delta);
        this.mFastOutSlowIn = AnimationUtils.loadInterpolator(this.mContext, 17563661);
        MediaActionSound mediaActionSound = new MediaActionSound();
        this.mCameraSound = mediaActionSound;
        mediaActionSound.load(0);
    }

    public void onComputeInternalInsets(ViewTreeObserver.InternalInsetsInfo internalInsetsInfo) {
        internalInsetsInfo.setTouchableInsets(3);
        Region region = new Region();
        Rect rect = new Rect();
        this.mScreenshotPreview.getBoundsOnScreen(rect);
        region.op(rect, Region.Op.UNION);
        Rect rect2 = new Rect();
        this.mActionsContainer.getBoundsOnScreen(rect2);
        region.op(rect2, Region.Op.UNION);
        Rect rect3 = new Rect();
        this.mDismissButton.getBoundsOnScreen(rect3);
        region.op(rect3, Region.Op.UNION);
        if (QuickStepContract.isGesturalMode(this.mNavMode)) {
            Rect rect4 = new Rect(0, 0, this.mLeftInset, this.mDisplayMetrics.heightPixels);
            region.op(rect4, Region.Op.UNION);
            DisplayMetrics displayMetrics = this.mDisplayMetrics;
            int i = displayMetrics.widthPixels;
            rect4.set(i - this.mRightInset, 0, i, displayMetrics.heightPixels);
            region.op(rect4, Region.Op.UNION);
        }
        internalInsetsInfo.touchableRegion.set(region);
    }

    /* JADX WARNING: Removed duplicated region for block: B:12:0x001e A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x0028  */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x0033  */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x003e  */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x0048  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void onConfigChanged(android.content.res.Configuration r5) {
        /*
            r4 = this;
            boolean r0 = r5.isNightModeActive()
            r1 = 0
            r2 = 1
            if (r0 == 0) goto L_0x000f
            boolean r0 = r4.mInDarkMode
            if (r0 != 0) goto L_0x0017
            r4.mInDarkMode = r2
            goto L_0x0015
        L_0x000f:
            boolean r0 = r4.mInDarkMode
            if (r0 == 0) goto L_0x0017
            r4.mInDarkMode = r1
        L_0x0015:
            r0 = r2
            goto L_0x0018
        L_0x0017:
            r0 = r1
        L_0x0018:
            int r3 = r5.getLayoutDirection()
            if (r3 == 0) goto L_0x0028
            if (r3 == r2) goto L_0x0021
            goto L_0x002f
        L_0x0021:
            boolean r3 = r4.mDirectionLTR
            if (r3 == 0) goto L_0x002f
            r4.mDirectionLTR = r1
            goto L_0x002e
        L_0x0028:
            boolean r3 = r4.mDirectionLTR
            if (r3 != 0) goto L_0x002f
            r4.mDirectionLTR = r2
        L_0x002e:
            r0 = r2
        L_0x002f:
            int r5 = r5.orientation
            if (r5 == r2) goto L_0x003e
            r3 = 2
            if (r5 == r3) goto L_0x0037
            goto L_0x0045
        L_0x0037:
            boolean r5 = r4.mOrientationPortrait
            if (r5 == 0) goto L_0x0045
            r4.mOrientationPortrait = r1
            goto L_0x0046
        L_0x003e:
            boolean r5 = r4.mOrientationPortrait
            if (r5 != 0) goto L_0x0045
            r4.mOrientationPortrait = r2
            goto L_0x0046
        L_0x0045:
            r2 = r0
        L_0x0046:
            if (r2 == 0) goto L_0x004b
            r4.reloadAssets()
        L_0x004b:
            android.content.Context r5 = r4.mContext
            android.content.res.Resources r5 = r5.getResources()
            r0 = 17694853(0x10e0085, float:2.6081654E-38)
            int r5 = r5.getInteger(r0)
            r4.mNavMode = r5
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.screenshot.GlobalScreenshot.onConfigChanged(android.content.res.Configuration):void");
    }

    private void reloadAssets() {
        View view = this.mScreenshotLayout;
        boolean z = view != null && view.isAttachedToWindow();
        if (z) {
            this.mWindowManager.removeView(this.mScreenshotLayout);
        }
        View inflate = LayoutInflater.from(this.mContext).inflate(C0017R$layout.global_screenshot, (ViewGroup) null);
        this.mScreenshotLayout = inflate;
        inflate.setOnTouchListener(new View.OnTouchListener() {
            /* class com.android.systemui.screenshot.$$Lambda$GlobalScreenshot$cjbBbqRWya3kStc4feynRVu5_w */

            public final boolean onTouch(View view, MotionEvent motionEvent) {
                return GlobalScreenshot.this.lambda$reloadAssets$0$GlobalScreenshot(view, motionEvent);
            }
        });
        this.mScreenshotLayout.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            /* class com.android.systemui.screenshot.$$Lambda$GlobalScreenshot$6btUb3pURbXlvq3U7gZEq6_gft0 */

            public final WindowInsets onApplyWindowInsets(View view, WindowInsets windowInsets) {
                return GlobalScreenshot.this.lambda$reloadAssets$1$GlobalScreenshot(view, windowInsets);
            }
        });
        this.mScreenshotLayout.setOnKeyListener(new View.OnKeyListener() {
            /* class com.android.systemui.screenshot.GlobalScreenshot.AnonymousClass2 */

            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (i != 4) {
                    return false;
                }
                GlobalScreenshot.this.dismissScreenshot("back pressed", true);
                return true;
            }
        });
        this.mScreenshotLayout.setFocusableInTouchMode(true);
        this.mScreenshotLayout.requestFocus();
        ImageView imageView = (ImageView) this.mScreenshotLayout.findViewById(C0015R$id.global_screenshot_animated_view);
        this.mScreenshotAnimatedView = imageView;
        imageView.setClipToOutline(true);
        this.mScreenshotAnimatedView.setOutlineProvider(new ViewOutlineProvider(this) {
            /* class com.android.systemui.screenshot.GlobalScreenshot.AnonymousClass3 */

            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(new Rect(0, 0, view.getWidth(), view.getHeight()), ((float) view.getWidth()) * 0.05f);
            }
        });
        ImageView imageView2 = (ImageView) this.mScreenshotLayout.findViewById(C0015R$id.global_screenshot_preview);
        this.mScreenshotPreview = imageView2;
        imageView2.setClipToOutline(true);
        this.mScreenshotPreview.setOutlineProvider(new ViewOutlineProvider(this) {
            /* class com.android.systemui.screenshot.GlobalScreenshot.AnonymousClass4 */

            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(new Rect(0, 0, view.getWidth(), view.getHeight()), ((float) view.getWidth()) * 0.05f);
            }
        });
        this.mActionsContainerBackground = (ImageView) this.mScreenshotLayout.findViewById(C0015R$id.global_screenshot_actions_container_background);
        this.mActionsContainer = (HorizontalScrollView) this.mScreenshotLayout.findViewById(C0015R$id.global_screenshot_actions_container);
        this.mActionsView = (LinearLayout) this.mScreenshotLayout.findViewById(C0015R$id.global_screenshot_actions);
        this.mBackgroundProtection = (ImageView) this.mScreenshotLayout.findViewById(C0015R$id.global_screenshot_actions_background);
        FrameLayout frameLayout = (FrameLayout) this.mScreenshotLayout.findViewById(C0015R$id.global_screenshot_dismiss_button);
        this.mDismissButton = frameLayout;
        frameLayout.setOnClickListener(new View.OnClickListener() {
            /* class com.android.systemui.screenshot.$$Lambda$GlobalScreenshot$ivNcVUrtovF5MBU69iA0tYfbicU */

            public final void onClick(View view) {
                GlobalScreenshot.this.lambda$reloadAssets$2$GlobalScreenshot(view);
            }
        });
        this.mScreenshotFlash = (ImageView) this.mScreenshotLayout.findViewById(C0015R$id.global_screenshot_flash);
        this.mScreenshotSelectorView = (ScreenshotSelectorView) this.mScreenshotLayout.findViewById(C0015R$id.global_screenshot_selector);
        this.mScreenshotLayout.setFocusable(true);
        this.mScreenshotSelectorView.setFocusable(true);
        this.mScreenshotSelectorView.setFocusableInTouchMode(true);
        this.mScreenshotAnimatedView.setPivotX(0.0f);
        this.mScreenshotAnimatedView.setPivotY(0.0f);
        this.mActionsContainer.setScrollX(0);
        if (z) {
            this.mWindowManager.addView(this.mScreenshotLayout, this.mWindowLayoutParams);
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$reloadAssets$0 */
    public /* synthetic */ boolean lambda$reloadAssets$0$GlobalScreenshot(View view, MotionEvent motionEvent) {
        if (motionEvent.getActionMasked() == 4) {
            setWindowFocusable(false);
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$reloadAssets$1 */
    public /* synthetic */ WindowInsets lambda$reloadAssets$1$GlobalScreenshot(View view, WindowInsets windowInsets) {
        if (QuickStepContract.isGesturalMode(this.mNavMode)) {
            Insets insets = windowInsets.getInsets(WindowInsets.Type.systemGestures());
            this.mLeftInset = insets.left;
            this.mRightInset = insets.right;
        } else {
            this.mRightInset = 0;
            this.mLeftInset = 0;
        }
        return this.mScreenshotLayout.onApplyWindowInsets(windowInsets);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$reloadAssets$2 */
    public /* synthetic */ void lambda$reloadAssets$2$GlobalScreenshot(View view) {
        this.mUiEventLogger.log(ScreenshotEvent.SCREENSHOT_EXPLICIT_DISMISSAL);
        dismissScreenshot("dismiss_button", false);
        this.mOnCompleteRunnable.run();
    }

    private void setWindowFocusable(boolean z) {
        if (z) {
            this.mWindowLayoutParams.flags &= -9;
        } else {
            this.mWindowLayoutParams.flags |= 8;
        }
        if (this.mScreenshotLayout.isAttachedToWindow()) {
            this.mWindowManager.updateViewLayout(this.mScreenshotLayout, this.mWindowLayoutParams);
        }
    }

    private void saveScreenshotInWorkerThread(Consumer<Uri> consumer, ActionsReadyListener actionsReadyListener) {
        SaveImageInBackgroundData saveImageInBackgroundData = new SaveImageInBackgroundData();
        saveImageInBackgroundData.image = this.mScreenBitmap;
        saveImageInBackgroundData.finisher = consumer;
        saveImageInBackgroundData.mActionsReadyListener = actionsReadyListener;
        SaveImageInBackgroundTask saveImageInBackgroundTask = this.mSaveInBgTask;
        if (saveImageInBackgroundTask != null) {
            saveImageInBackgroundTask.setActionsReadyListener(new ActionsReadyListener() {
                /* class com.android.systemui.screenshot.GlobalScreenshot.AnonymousClass5 */

                /* access modifiers changed from: package-private */
                @Override // com.android.systemui.screenshot.GlobalScreenshot.ActionsReadyListener
                public void onActionsReady(SavedImageData savedImageData) {
                    GlobalScreenshot.this.logSuccessOnActionsReady(savedImageData);
                }
            });
        }
        SaveImageInBackgroundTask saveImageInBackgroundTask2 = new SaveImageInBackgroundTask(this.mContext, saveImageInBackgroundData);
        this.mSaveInBgTask = saveImageInBackgroundTask2;
        saveImageInBackgroundTask2.execute(new Void[0]);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void takeScreenshot(Consumer<Uri> consumer, Rect rect) {
        takeScreenshot(SurfaceControl.screenshot(rect, rect.width(), rect.height(), this.mDisplay.getRotation()), consumer, new Rect(rect), Insets.NONE, true);
    }

    private void takeScreenshot(Bitmap bitmap, Consumer<Uri> consumer, Rect rect, Insets insets, boolean z) {
        dismissScreenshot("new screenshot requested", true);
        this.mScreenBitmap = bitmap;
        if (bitmap == null) {
            this.mNotificationsController.notifyScreenshotError(C0021R$string.screenshot_failed_to_capture_text);
            consumer.accept(null);
            this.mOnCompleteRunnable.run();
        } else if (!isUserSetupComplete()) {
            saveScreenshotAndToast(consumer);
        } else {
            this.mScreenBitmap.setHasAlpha(false);
            this.mScreenBitmap.prepareToDraw();
            onConfigChanged(this.mContext.getResources().getConfiguration());
            Animator animator = this.mDismissAnimation;
            if (animator != null && animator.isRunning()) {
                this.mDismissAnimation.cancel();
            }
            setWindowFocusable(true);
            startAnimation(consumer, rect, insets, z);
        }
    }

    /* access modifiers changed from: package-private */
    public void takeScreenshot(Consumer<Uri> consumer, Runnable runnable) {
        this.mOnCompleteRunnable = runnable;
        this.mDisplay.getRealMetrics(this.mDisplayMetrics);
        DisplayMetrics displayMetrics = this.mDisplayMetrics;
        takeScreenshot(consumer, new Rect(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels));
    }

    /* access modifiers changed from: package-private */
    public void handleImageAsScreenshot(Bitmap bitmap, Rect rect, Insets insets, int i, int i2, ComponentName componentName, Consumer<Uri> consumer, Runnable runnable) {
        this.mOnCompleteRunnable = runnable;
        if (aspectRatiosMatch(bitmap, insets, rect)) {
            takeScreenshot(bitmap, consumer, rect, insets, false);
        } else {
            takeScreenshot(bitmap, consumer, new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()), Insets.NONE, true);
        }
    }

    /* access modifiers changed from: package-private */
    @SuppressLint({"ClickableViewAccessibility"})
    public void takeScreenshotPartial(final Consumer<Uri> consumer, Runnable runnable) {
        dismissScreenshot("new screenshot requested", true);
        this.mOnCompleteRunnable = runnable;
        this.mWindowManager.addView(this.mScreenshotLayout, this.mWindowLayoutParams);
        this.mScreenshotSelectorView.setOnTouchListener(new View.OnTouchListener() {
            /* class com.android.systemui.screenshot.GlobalScreenshot.AnonymousClass6 */

            public boolean onTouch(View view, MotionEvent motionEvent) {
                ScreenshotSelectorView screenshotSelectorView = (ScreenshotSelectorView) view;
                int action = motionEvent.getAction();
                if (action == 0) {
                    screenshotSelectorView.startSelection((int) motionEvent.getX(), (int) motionEvent.getY());
                    return true;
                } else if (action == 1) {
                    screenshotSelectorView.setVisibility(8);
                    GlobalScreenshot.this.mWindowManager.removeView(GlobalScreenshot.this.mScreenshotLayout);
                    Rect selectionRect = screenshotSelectorView.getSelectionRect();
                    if (!(selectionRect == null || selectionRect.width() == 0 || selectionRect.height() == 0)) {
                        GlobalScreenshot.this.mScreenshotLayout.post(new Runnable(consumer, selectionRect) {
                            /* class com.android.systemui.screenshot.$$Lambda$GlobalScreenshot$6$DV8eaPDbwMlxrm96cZXE9jcXpVY */
                            public final /* synthetic */ Consumer f$1;
                            public final /* synthetic */ Rect f$2;

                            {
                                this.f$1 = r2;
                                this.f$2 = r3;
                            }

                            public final void run() {
                                GlobalScreenshot.AnonymousClass6.this.lambda$onTouch$0$GlobalScreenshot$6(this.f$1, this.f$2);
                            }
                        });
                    }
                    screenshotSelectorView.stopSelection();
                    return true;
                } else if (action != 2) {
                    return false;
                } else {
                    screenshotSelectorView.updateSelection((int) motionEvent.getX(), (int) motionEvent.getY());
                    return true;
                }
            }

            /* access modifiers changed from: private */
            /* renamed from: lambda$onTouch$0 */
            public /* synthetic */ void lambda$onTouch$0$GlobalScreenshot$6(Consumer consumer, Rect rect) {
                GlobalScreenshot.this.takeScreenshot((GlobalScreenshot) consumer, (Consumer) rect);
            }
        });
        this.mScreenshotLayout.post(new Runnable() {
            /* class com.android.systemui.screenshot.$$Lambda$GlobalScreenshot$Tc_8QADSt7VB0ZmgXdNNGChxZmU */

            public final void run() {
                GlobalScreenshot.this.lambda$takeScreenshotPartial$3$GlobalScreenshot();
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$takeScreenshotPartial$3 */
    public /* synthetic */ void lambda$takeScreenshotPartial$3$GlobalScreenshot() {
        this.mScreenshotSelectorView.setVisibility(0);
        this.mScreenshotSelectorView.requestFocus();
    }

    /* access modifiers changed from: package-private */
    public void stopScreenshot() {
        if (this.mScreenshotSelectorView.getSelectionRect() != null) {
            this.mWindowManager.removeView(this.mScreenshotLayout);
            this.mScreenshotSelectorView.stopSelection();
        }
    }

    private void saveScreenshotAndToast(final Consumer<Uri> consumer) {
        this.mScreenshotHandler.post(new Runnable() {
            /* class com.android.systemui.screenshot.$$Lambda$GlobalScreenshot$NW_abdlllKq5tF4cCHzAFPVwopQ */

            public final void run() {
                GlobalScreenshot.this.lambda$saveScreenshotAndToast$4$GlobalScreenshot();
            }
        });
        saveScreenshotInWorkerThread(consumer, new ActionsReadyListener() {
            /* class com.android.systemui.screenshot.GlobalScreenshot.AnonymousClass7 */

            /* access modifiers changed from: package-private */
            @Override // com.android.systemui.screenshot.GlobalScreenshot.ActionsReadyListener
            public void onActionsReady(SavedImageData savedImageData) {
                consumer.accept(savedImageData.uri);
                if (savedImageData.uri == null) {
                    GlobalScreenshot.this.mUiEventLogger.log(ScreenshotEvent.SCREENSHOT_NOT_SAVED);
                    GlobalScreenshot.this.mNotificationsController.notifyScreenshotError(C0021R$string.screenshot_failed_to_capture_text);
                    return;
                }
                GlobalScreenshot.this.mUiEventLogger.log(ScreenshotEvent.SCREENSHOT_SAVED);
                GlobalScreenshot.this.mScreenshotHandler.post(new Runnable() {
                    /* class com.android.systemui.screenshot.$$Lambda$GlobalScreenshot$7$SPzMe6Aypg6ajtouKOBNZnbTV5E */

                    public final void run() {
                        GlobalScreenshot.AnonymousClass7.this.lambda$onActionsReady$0$GlobalScreenshot$7();
                    }
                });
            }

            /* access modifiers changed from: private */
            /* renamed from: lambda$onActionsReady$0 */
            public /* synthetic */ void lambda$onActionsReady$0$GlobalScreenshot$7() {
                Toast.makeText(GlobalScreenshot.this.mContext, C0021R$string.screenshot_saved_title, 0).show();
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$saveScreenshotAndToast$4 */
    public /* synthetic */ void lambda$saveScreenshotAndToast$4$GlobalScreenshot() {
        this.mCameraSound.play(0);
    }

    private boolean isUserSetupComplete() {
        return Settings.Secure.getInt(this.mContext.getContentResolver(), "user_setup_complete", 0) == 1;
    }

    /* access modifiers changed from: package-private */
    public void dismissScreenshot(String str, boolean z) {
        Log.v("GlobalScreenshot", "clearing screenshot: " + str);
        this.mScreenshotHandler.removeMessages(2);
        this.mScreenshotLayout.getViewTreeObserver().removeOnComputeInternalInsetsListener(this);
        if (!z) {
            AnimatorSet createScreenshotDismissAnimation = createScreenshotDismissAnimation();
            this.mDismissAnimation = createScreenshotDismissAnimation;
            createScreenshotDismissAnimation.addListener(new AnimatorListenerAdapter() {
                /* class com.android.systemui.screenshot.GlobalScreenshot.AnonymousClass8 */

                public void onAnimationEnd(Animator animator) {
                    super.onAnimationEnd(animator);
                    GlobalScreenshot.this.clearScreenshot();
                }
            });
            this.mDismissAnimation.start();
            return;
        }
        clearScreenshot();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void clearScreenshot() {
        if (this.mScreenshotLayout.isAttachedToWindow()) {
            this.mWindowManager.removeView(this.mScreenshotLayout);
        }
        this.mScreenshotPreview.setImageDrawable(null);
        this.mScreenshotAnimatedView.setImageDrawable(null);
        this.mScreenshotAnimatedView.setVisibility(8);
        this.mActionsContainerBackground.setVisibility(8);
        this.mActionsContainer.setVisibility(8);
        this.mBackgroundProtection.setAlpha(0.0f);
        this.mDismissButton.setVisibility(8);
        this.mScreenshotPreview.setVisibility(8);
        this.mScreenshotPreview.setLayerType(0, null);
        this.mScreenshotPreview.setContentDescription(this.mContext.getResources().getString(C0021R$string.screenshot_preview_description));
        this.mScreenshotLayout.setAlpha(1.0f);
        this.mDismissButton.setTranslationY(0.0f);
        this.mActionsContainer.setTranslationY(0.0f);
        this.mActionsContainerBackground.setTranslationY(0.0f);
        this.mScreenshotPreview.setTranslationY(0.0f);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void showUiOnActionsReady(SavedImageData savedImageData) {
        logSuccessOnActionsReady(savedImageData);
        this.mScreenshotHandler.removeMessages(2);
        Handler handler = this.mScreenshotHandler;
        handler.sendMessageDelayed(handler.obtainMessage(2), (long) ((AccessibilityManager) this.mContext.getSystemService("accessibility")).getRecommendedTimeoutMillis(6000, 4));
        if (savedImageData.uri != null) {
            this.mScreenshotHandler.post(new Runnable(savedImageData) {
                /* class com.android.systemui.screenshot.$$Lambda$GlobalScreenshot$4lRJxCebWv6lMPOxNapvb200hVc */
                public final /* synthetic */ GlobalScreenshot.SavedImageData f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    GlobalScreenshot.this.lambda$showUiOnActionsReady$5$GlobalScreenshot(this.f$1);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$showUiOnActionsReady$5 */
    public /* synthetic */ void lambda$showUiOnActionsReady$5$GlobalScreenshot(final SavedImageData savedImageData) {
        Animator animator = this.mScreenshotAnimation;
        if (animator == null || !animator.isRunning()) {
            createScreenshotActionsShadeAnimation(savedImageData).start();
        } else {
            this.mScreenshotAnimation.addListener(new AnimatorListenerAdapter() {
                /* class com.android.systemui.screenshot.GlobalScreenshot.AnonymousClass9 */

                public void onAnimationEnd(Animator animator) {
                    super.onAnimationEnd(animator);
                    GlobalScreenshot.this.createScreenshotActionsShadeAnimation(savedImageData).start();
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void logSuccessOnActionsReady(SavedImageData savedImageData) {
        if (savedImageData.uri == null) {
            this.mUiEventLogger.log(ScreenshotEvent.SCREENSHOT_NOT_SAVED);
            this.mNotificationsController.notifyScreenshotError(C0021R$string.screenshot_failed_to_capture_text);
            return;
        }
        this.mUiEventLogger.log(ScreenshotEvent.SCREENSHOT_SAVED);
    }

    private void startAnimation(Consumer<Uri> consumer, Rect rect, Insets insets, boolean z) {
        if (((PowerManager) this.mContext.getSystemService("power")).isPowerSaveMode()) {
            Toast.makeText(this.mContext, C0021R$string.screenshot_saved_title, 0).show();
        }
        this.mScreenshotHandler.post(new Runnable(insets, rect, z, consumer) {
            /* class com.android.systemui.screenshot.$$Lambda$GlobalScreenshot$wtfj5YdXpTdwTAi0ronJ8cHaMQ */
            public final /* synthetic */ Insets f$1;
            public final /* synthetic */ Rect f$2;
            public final /* synthetic */ boolean f$3;
            public final /* synthetic */ Consumer f$4;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
            }

            public final void run() {
                GlobalScreenshot.this.lambda$startAnimation$7$GlobalScreenshot(this.f$1, this.f$2, this.f$3, this.f$4);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$startAnimation$7 */
    public /* synthetic */ void lambda$startAnimation$7$GlobalScreenshot(Insets insets, Rect rect, boolean z, Consumer consumer) {
        if (!this.mScreenshotLayout.isAttachedToWindow()) {
            this.mWindowManager.addView(this.mScreenshotLayout, this.mWindowLayoutParams);
        }
        this.mScreenshotAnimatedView.setImageDrawable(createScreenDrawable(this.mScreenBitmap, insets));
        setAnimatedViewSize(rect.width(), rect.height());
        this.mScreenshotAnimatedView.setVisibility(8);
        this.mScreenshotPreview.setImageDrawable(createScreenDrawable(this.mScreenBitmap, insets));
        this.mScreenshotPreview.setVisibility(4);
        this.mScreenshotHandler.post(new Runnable(rect, z, consumer) {
            /* class com.android.systemui.screenshot.$$Lambda$GlobalScreenshot$jLPtsifwwAXetcpkY7zTdj5sE */
            public final /* synthetic */ Rect f$1;
            public final /* synthetic */ boolean f$2;
            public final /* synthetic */ Consumer f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            public final void run() {
                GlobalScreenshot.this.lambda$startAnimation$6$GlobalScreenshot(this.f$1, this.f$2, this.f$3);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$startAnimation$6 */
    public /* synthetic */ void lambda$startAnimation$6$GlobalScreenshot(Rect rect, boolean z, Consumer consumer) {
        this.mScreenshotLayout.getViewTreeObserver().addOnComputeInternalInsetsListener(this);
        this.mScreenshotAnimation = createScreenshotDropInAnimation(rect, z);
        saveScreenshotInWorkerThread(consumer, new ActionsReadyListener() {
            /* class com.android.systemui.screenshot.GlobalScreenshot.AnonymousClass10 */

            /* access modifiers changed from: package-private */
            @Override // com.android.systemui.screenshot.GlobalScreenshot.ActionsReadyListener
            public void onActionsReady(SavedImageData savedImageData) {
                GlobalScreenshot.this.showUiOnActionsReady(savedImageData);
            }
        });
        this.mCameraSound.play(0);
        this.mScreenshotPreview.setLayerType(2, null);
        this.mScreenshotPreview.buildLayer();
        this.mScreenshotAnimation.start();
    }

    private AnimatorSet createScreenshotDropInAnimation(final Rect rect, boolean z) {
        Rect rect2 = new Rect();
        this.mScreenshotPreview.getBoundsOnScreen(rect2);
        final float width = this.mCornerSizeX / ((float) (this.mOrientationPortrait ? rect.width() : rect.height()));
        this.mScreenshotAnimatedView.setScaleX(1.0f);
        this.mScreenshotAnimatedView.setScaleY(1.0f);
        this.mDismissButton.setAlpha(0.0f);
        this.mDismissButton.setVisibility(0);
        AnimatorSet animatorSet = new AnimatorSet();
        ValueAnimator ofFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
        ofFloat.setDuration(133L);
        ofFloat.setInterpolator(this.mFastOutSlowIn);
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.android.systemui.screenshot.$$Lambda$GlobalScreenshot$YZI2_eat7pqyqR5GQVbxZaUURE */

            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                GlobalScreenshot.this.lambda$createScreenshotDropInAnimation$8$GlobalScreenshot(valueAnimator);
            }
        });
        ValueAnimator ofFloat2 = ValueAnimator.ofFloat(1.0f, 0.0f);
        ofFloat2.setDuration(217L);
        ofFloat2.setInterpolator(this.mFastOutSlowIn);
        ofFloat2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.android.systemui.screenshot.$$Lambda$GlobalScreenshot$YQUkDp6FqmNgj235g5aM4pPh0E */

            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                GlobalScreenshot.this.lambda$createScreenshotDropInAnimation$9$GlobalScreenshot(valueAnimator);
            }
        });
        PointF pointF = new PointF((float) rect.centerX(), (float) rect.centerY());
        final PointF pointF2 = new PointF((float) rect2.centerX(), (float) rect2.centerY());
        ValueAnimator ofFloat3 = ValueAnimator.ofFloat(0.0f, 1.0f);
        ofFloat3.setDuration(500L);
        ofFloat3.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(0.468f, width, 0.468f, pointF, pointF2, rect, 0.4f) {
            /* class com.android.systemui.screenshot.$$Lambda$GlobalScreenshot$ZwaN03P50fmACzyWijOKLriVmWE */
            public final /* synthetic */ float f$1;
            public final /* synthetic */ float f$2;
            public final /* synthetic */ float f$3;
            public final /* synthetic */ PointF f$4;
            public final /* synthetic */ PointF f$5;
            public final /* synthetic */ Rect f$6;
            public final /* synthetic */ float f$7;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
                this.f$5 = r6;
                this.f$6 = r7;
                this.f$7 = r8;
            }

            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                GlobalScreenshot.this.lambda$createScreenshotDropInAnimation$10$GlobalScreenshot(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7, valueAnimator);
            }
        });
        ofFloat3.addListener(new AnimatorListenerAdapter() {
            /* class com.android.systemui.screenshot.GlobalScreenshot.AnonymousClass11 */

            public void onAnimationStart(Animator animator) {
                super.onAnimationStart(animator);
                GlobalScreenshot.this.mScreenshotAnimatedView.setVisibility(0);
            }
        });
        this.mScreenshotFlash.setAlpha(0.0f);
        this.mScreenshotFlash.setVisibility(0);
        if (z) {
            animatorSet.play(ofFloat2).after(ofFloat);
            animatorSet.play(ofFloat2).with(ofFloat3);
        } else {
            animatorSet.play(ofFloat3);
        }
        animatorSet.addListener(new AnimatorListenerAdapter() {
            /* class com.android.systemui.screenshot.GlobalScreenshot.AnonymousClass12 */

            public void onAnimationEnd(Animator animator) {
                float f;
                super.onAnimationEnd(animator);
                GlobalScreenshot.this.mDismissButton.setAlpha(1.0f);
                float width = ((float) GlobalScreenshot.this.mDismissButton.getWidth()) / 2.0f;
                if (GlobalScreenshot.this.mDirectionLTR) {
                    f = (pointF2.x - width) + ((((float) rect.width()) * width) / 2.0f);
                } else {
                    f = (pointF2.x - width) - ((((float) rect.width()) * width) / 2.0f);
                }
                GlobalScreenshot.this.mDismissButton.setX(f);
                GlobalScreenshot.this.mDismissButton.setY((pointF2.y - width) - ((((float) rect.height()) * width) / 2.0f));
                GlobalScreenshot.this.mScreenshotAnimatedView.setScaleX(1.0f);
                GlobalScreenshot.this.mScreenshotAnimatedView.setScaleY(1.0f);
                GlobalScreenshot.this.mScreenshotAnimatedView.setX(pointF2.x - ((((float) rect.width()) * width) / 2.0f));
                GlobalScreenshot.this.mScreenshotAnimatedView.setY(pointF2.y - ((((float) rect.height()) * width) / 2.0f));
                GlobalScreenshot.this.mScreenshotAnimatedView.setVisibility(8);
                GlobalScreenshot.this.mScreenshotPreview.setVisibility(0);
                GlobalScreenshot.this.mScreenshotLayout.forceLayout();
            }
        });
        return animatorSet;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$createScreenshotDropInAnimation$8 */
    public /* synthetic */ void lambda$createScreenshotDropInAnimation$8$GlobalScreenshot(ValueAnimator valueAnimator) {
        this.mScreenshotFlash.setAlpha(((Float) valueAnimator.getAnimatedValue()).floatValue());
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$createScreenshotDropInAnimation$9 */
    public /* synthetic */ void lambda$createScreenshotDropInAnimation$9$GlobalScreenshot(ValueAnimator valueAnimator) {
        this.mScreenshotFlash.setAlpha(((Float) valueAnimator.getAnimatedValue()).floatValue());
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$createScreenshotDropInAnimation$10 */
    public /* synthetic */ void lambda$createScreenshotDropInAnimation$10$GlobalScreenshot(float f, float f2, float f3, PointF pointF, PointF pointF2, Rect rect, float f4, ValueAnimator valueAnimator) {
        float animatedFraction = valueAnimator.getAnimatedFraction();
        if (animatedFraction < f) {
            float lerp = MathUtils.lerp(1.0f, f2, this.mFastOutSlowIn.getInterpolation(animatedFraction / f));
            this.mScreenshotAnimatedView.setScaleX(lerp);
            this.mScreenshotAnimatedView.setScaleY(lerp);
        } else {
            this.mScreenshotAnimatedView.setScaleX(f2);
            this.mScreenshotAnimatedView.setScaleY(f2);
        }
        float scaleX = this.mScreenshotAnimatedView.getScaleX();
        float scaleY = this.mScreenshotAnimatedView.getScaleY();
        if (animatedFraction < f3) {
            this.mScreenshotAnimatedView.setX(MathUtils.lerp(pointF.x, pointF2.x, this.mFastOutSlowIn.getInterpolation(animatedFraction / f3)) - ((((float) rect.width()) * scaleX) / 2.0f));
        } else {
            this.mScreenshotAnimatedView.setX(pointF2.x - ((((float) rect.width()) * scaleX) / 2.0f));
        }
        this.mScreenshotAnimatedView.setY(MathUtils.lerp(pointF.y, pointF2.y, this.mFastOutSlowIn.getInterpolation(animatedFraction)) - ((((float) rect.height()) * scaleY) / 2.0f));
        if (animatedFraction >= f4) {
            this.mDismissButton.setAlpha((animatedFraction - f4) / (1.0f - f4));
            float x = this.mScreenshotAnimatedView.getX();
            float y = this.mScreenshotAnimatedView.getY();
            FrameLayout frameLayout = this.mDismissButton;
            frameLayout.setY(y - (((float) frameLayout.getHeight()) / 2.0f));
            if (this.mDirectionLTR) {
                this.mDismissButton.setX((x + (((float) rect.width()) * scaleX)) - (((float) this.mDismissButton.getWidth()) / 2.0f));
                return;
            }
            FrameLayout frameLayout2 = this.mDismissButton;
            frameLayout2.setX(x - (((float) frameLayout2.getWidth()) / 2.0f));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private ValueAnimator createScreenshotActionsShadeAnimation(SavedImageData savedImageData) {
        LayoutInflater from = LayoutInflater.from(this.mContext);
        this.mActionsView.removeAllViews();
        this.mScreenshotLayout.invalidate();
        this.mScreenshotLayout.requestLayout();
        this.mScreenshotLayout.getViewTreeObserver().dispatchOnGlobalLayout();
        try {
            ActivityManager.getService().resumeAppSwitches();
        } catch (RemoteException unused) {
        }
        ArrayList arrayList = new ArrayList();
        for (Notification.Action action : savedImageData.smartActions) {
            ScreenshotActionChip screenshotActionChip = (ScreenshotActionChip) from.inflate(C0017R$layout.global_screenshot_action_chip, (ViewGroup) this.mActionsView, false);
            screenshotActionChip.setText(action.title);
            screenshotActionChip.setIcon(action.getIcon(), false);
            screenshotActionChip.setPendingIntent(action.actionIntent, new Runnable() {
                /* class com.android.systemui.screenshot.$$Lambda$GlobalScreenshot$zoiJ7VfPKwI7yIJvbi190gD1F0 */

                public final void run() {
                    GlobalScreenshot.this.lambda$createScreenshotActionsShadeAnimation$11$GlobalScreenshot();
                }
            });
            this.mActionsView.addView(screenshotActionChip);
            arrayList.add(screenshotActionChip);
        }
        ScreenshotActionChip screenshotActionChip2 = (ScreenshotActionChip) from.inflate(C0017R$layout.global_screenshot_action_chip, (ViewGroup) this.mActionsView, false);
        screenshotActionChip2.setText(savedImageData.shareAction.title);
        screenshotActionChip2.setIcon(savedImageData.shareAction.getIcon(), true);
        screenshotActionChip2.setPendingIntent(savedImageData.shareAction.actionIntent, new Runnable() {
            /* class com.android.systemui.screenshot.$$Lambda$GlobalScreenshot$MkAlXNeoR7_50KjiDre0R4wGids */

            public final void run() {
                GlobalScreenshot.this.lambda$createScreenshotActionsShadeAnimation$12$GlobalScreenshot();
            }
        });
        this.mActionsView.addView(screenshotActionChip2);
        arrayList.add(screenshotActionChip2);
        ScreenshotActionChip screenshotActionChip3 = (ScreenshotActionChip) from.inflate(C0017R$layout.global_screenshot_action_chip, (ViewGroup) this.mActionsView, false);
        screenshotActionChip3.setText(savedImageData.editAction.title);
        screenshotActionChip3.setIcon(savedImageData.editAction.getIcon(), true);
        screenshotActionChip3.setPendingIntent(savedImageData.editAction.actionIntent, new Runnable() {
            /* class com.android.systemui.screenshot.$$Lambda$GlobalScreenshot$XXIG7j6dPGZ6Zn1FXTR5gWo8g0I */

            public final void run() {
                GlobalScreenshot.this.lambda$createScreenshotActionsShadeAnimation$13$GlobalScreenshot();
            }
        });
        this.mActionsView.addView(screenshotActionChip3);
        arrayList.add(screenshotActionChip3);
        this.mScreenshotPreview.setOnClickListener(new View.OnClickListener(savedImageData) {
            /* class com.android.systemui.screenshot.$$Lambda$GlobalScreenshot$VvfHRCrWoGQwVLoHepVN1CIElwE */
            public final /* synthetic */ GlobalScreenshot.SavedImageData f$1;

            {
                this.f$1 = r2;
            }

            public final void onClick(View view) {
                GlobalScreenshot.this.lambda$createScreenshotActionsShadeAnimation$14$GlobalScreenshot(this.f$1, view);
            }
        });
        this.mScreenshotPreview.setContentDescription(savedImageData.editAction.title);
        LinearLayout linearLayout = this.mActionsView;
        ((LinearLayout.LayoutParams) linearLayout.getChildAt(linearLayout.getChildCount() - 1).getLayoutParams()).setMarginEnd(0);
        ValueAnimator ofFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
        ofFloat.setDuration(400L);
        this.mActionsContainer.setAlpha(0.0f);
        this.mActionsContainerBackground.setAlpha(0.0f);
        this.mActionsContainer.setVisibility(0);
        this.mActionsContainerBackground.setVisibility(0);
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(0.25f, arrayList) {
            /* class com.android.systemui.screenshot.$$Lambda$GlobalScreenshot$keIEq4fnGhzmcoMhgArWXJcCzY */
            public final /* synthetic */ float f$1;
            public final /* synthetic */ ArrayList f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                GlobalScreenshot.this.lambda$createScreenshotActionsShadeAnimation$15$GlobalScreenshot(this.f$1, this.f$2, valueAnimator);
            }
        });
        return ofFloat;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$createScreenshotActionsShadeAnimation$11 */
    public /* synthetic */ void lambda$createScreenshotActionsShadeAnimation$11$GlobalScreenshot() {
        this.mUiEventLogger.log(ScreenshotEvent.SCREENSHOT_SMART_ACTION_TAPPED);
        dismissScreenshot("chip tapped", false);
        this.mOnCompleteRunnable.run();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$createScreenshotActionsShadeAnimation$12 */
    public /* synthetic */ void lambda$createScreenshotActionsShadeAnimation$12$GlobalScreenshot() {
        this.mUiEventLogger.log(ScreenshotEvent.SCREENSHOT_SHARE_TAPPED);
        dismissScreenshot("chip tapped", false);
        this.mOnCompleteRunnable.run();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$createScreenshotActionsShadeAnimation$13 */
    public /* synthetic */ void lambda$createScreenshotActionsShadeAnimation$13$GlobalScreenshot() {
        this.mUiEventLogger.log(ScreenshotEvent.SCREENSHOT_EDIT_TAPPED);
        dismissScreenshot("chip tapped", false);
        this.mOnCompleteRunnable.run();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$createScreenshotActionsShadeAnimation$14 */
    public /* synthetic */ void lambda$createScreenshotActionsShadeAnimation$14$GlobalScreenshot(SavedImageData savedImageData, View view) {
        try {
            savedImageData.editAction.actionIntent.send();
            this.mUiEventLogger.log(ScreenshotEvent.SCREENSHOT_PREVIEW_TAPPED);
            dismissScreenshot("screenshot preview tapped", false);
            this.mOnCompleteRunnable.run();
        } catch (PendingIntent.CanceledException e) {
            Log.e("GlobalScreenshot", "Intent cancelled", e);
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$createScreenshotActionsShadeAnimation$15 */
    public /* synthetic */ void lambda$createScreenshotActionsShadeAnimation$15$GlobalScreenshot(float f, ArrayList arrayList, ValueAnimator valueAnimator) {
        float animatedFraction = valueAnimator.getAnimatedFraction();
        this.mBackgroundProtection.setAlpha(animatedFraction);
        float f2 = animatedFraction < f ? animatedFraction / f : 1.0f;
        this.mActionsContainer.setAlpha(f2);
        this.mActionsContainerBackground.setAlpha(f2);
        float f3 = (0.3f * animatedFraction) + 0.7f;
        this.mActionsContainer.setScaleX(f3);
        this.mActionsContainerBackground.setScaleX(f3);
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            ScreenshotActionChip screenshotActionChip = (ScreenshotActionChip) it.next();
            screenshotActionChip.setAlpha(animatedFraction);
            screenshotActionChip.setScaleX(1.0f / f3);
        }
        HorizontalScrollView horizontalScrollView = this.mActionsContainer;
        horizontalScrollView.setScrollX(this.mDirectionLTR ? 0 : horizontalScrollView.getWidth());
        HorizontalScrollView horizontalScrollView2 = this.mActionsContainer;
        float f4 = 0.0f;
        horizontalScrollView2.setPivotX(this.mDirectionLTR ? 0.0f : (float) horizontalScrollView2.getWidth());
        ImageView imageView = this.mActionsContainerBackground;
        if (!this.mDirectionLTR) {
            f4 = (float) imageView.getWidth();
        }
        imageView.setPivotX(f4);
    }

    private AnimatorSet createScreenshotDismissAnimation() {
        ValueAnimator ofFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
        ofFloat.setStartDelay(50);
        ofFloat.setDuration(183L);
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.android.systemui.screenshot.$$Lambda$GlobalScreenshot$lwSCWVmpTO3JMK1heDr17u172Q */

            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                GlobalScreenshot.this.lambda$createScreenshotDismissAnimation$16$GlobalScreenshot(valueAnimator);
            }
        });
        ValueAnimator ofFloat2 = ValueAnimator.ofFloat(0.0f, 1.0f);
        ofFloat2.setInterpolator(this.mAccelerateInterpolator);
        ofFloat2.setDuration(350L);
        ofFloat2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(this.mScreenshotPreview.getTranslationY(), this.mDismissButton.getTranslationY()) {
            /* class com.android.systemui.screenshot.$$Lambda$GlobalScreenshot$2_tLLQ8ajKLz2LczKwL5qBWTPFQ */
            public final /* synthetic */ float f$1;
            public final /* synthetic */ float f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                GlobalScreenshot.this.lambda$createScreenshotDismissAnimation$17$GlobalScreenshot(this.f$1, this.f$2, valueAnimator);
            }
        });
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(ofFloat2).with(ofFloat);
        return animatorSet;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$createScreenshotDismissAnimation$16 */
    public /* synthetic */ void lambda$createScreenshotDismissAnimation$16$GlobalScreenshot(ValueAnimator valueAnimator) {
        this.mScreenshotLayout.setAlpha(1.0f - valueAnimator.getAnimatedFraction());
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$createScreenshotDismissAnimation$17 */
    public /* synthetic */ void lambda$createScreenshotDismissAnimation$17$GlobalScreenshot(float f, float f2, ValueAnimator valueAnimator) {
        float lerp = MathUtils.lerp(0.0f, this.mDismissDeltaY, valueAnimator.getAnimatedFraction());
        this.mScreenshotPreview.setTranslationY(f + lerp);
        this.mDismissButton.setTranslationY(f2 + lerp);
        this.mActionsContainer.setTranslationY(lerp);
        this.mActionsContainerBackground.setTranslationY(lerp);
    }

    private void setAnimatedViewSize(int i, int i2) {
        ViewGroup.LayoutParams layoutParams = this.mScreenshotAnimatedView.getLayoutParams();
        layoutParams.width = i;
        layoutParams.height = i2;
        this.mScreenshotAnimatedView.setLayoutParams(layoutParams);
    }

    private boolean aspectRatiosMatch(Bitmap bitmap, Insets insets, Rect rect) {
        int width = (bitmap.getWidth() - insets.left) - insets.right;
        int height = (bitmap.getHeight() - insets.top) - insets.bottom;
        if (height == 0 || width == 0 || bitmap.getWidth() == 0 || bitmap.getHeight() == 0) {
            Log.e("GlobalScreenshot", String.format("Provided bitmap and insets create degenerate region: %dx%d %s", Integer.valueOf(bitmap.getWidth()), Integer.valueOf(bitmap.getHeight()), insets));
            return false;
        }
        float f = ((float) width) / ((float) height);
        float width2 = ((float) rect.width()) / ((float) rect.height());
        boolean z = Math.abs(f - width2) < 0.1f;
        if (!z) {
            Log.d("GlobalScreenshot", String.format("aspectRatiosMatch: don't match bitmap: %f, bounds: %f", Float.valueOf(f), Float.valueOf(width2)));
        }
        return z;
    }

    private Drawable createScreenDrawable(Bitmap bitmap, Insets insets) {
        int width = (bitmap.getWidth() - insets.left) - insets.right;
        int height = (bitmap.getHeight() - insets.top) - insets.bottom;
        BitmapDrawable bitmapDrawable = new BitmapDrawable(this.mContext.getResources(), bitmap);
        if (height == 0 || width == 0 || bitmap.getWidth() == 0 || bitmap.getHeight() == 0) {
            Log.e("GlobalScreenshot", String.format("Can't create insetted drawable, using 0 insets bitmap and insets create degenerate region: %dx%d %s", Integer.valueOf(bitmap.getWidth()), Integer.valueOf(bitmap.getHeight()), insets));
            return bitmapDrawable;
        }
        float f = (float) width;
        float f2 = (float) height;
        InsetDrawable insetDrawable = new InsetDrawable(bitmapDrawable, (((float) insets.left) * -1.0f) / f, (((float) insets.top) * -1.0f) / f2, (((float) insets.right) * -1.0f) / f, (((float) insets.bottom) * -1.0f) / f2);
        if (insets.left >= 0 && insets.top >= 0 && insets.right >= 0 && insets.bottom >= 0) {
            return insetDrawable;
        }
        return new LayerDrawable(new Drawable[]{new ColorDrawable(-16777216), insetDrawable});
    }

    public static class ActionProxyReceiver extends BroadcastReceiver {
        private final StatusBar mStatusBar;

        public ActionProxyReceiver(Optional<Lazy<StatusBar>> optional) {
            StatusBar statusBar = null;
            Lazy<StatusBar> orElse = optional.orElse(null);
            this.mStatusBar = orElse != null ? orElse.get() : statusBar;
        }

        public void onReceive(Context context, Intent intent) {
            $$Lambda$GlobalScreenshot$ActionProxyReceiver$tBhjeKzNYNKU1TanWTPaMXUfmOc r1 = new Runnable(intent, context) {
                /* class com.android.systemui.screenshot.$$Lambda$GlobalScreenshot$ActionProxyReceiver$tBhjeKzNYNKU1TanWTPaMXUfmOc */
                public final /* synthetic */ Intent f$0;
                public final /* synthetic */ Context f$1;

                {
                    this.f$0 = r1;
                    this.f$1 = r2;
                }

                public final void run() {
                    GlobalScreenshot.ActionProxyReceiver.lambda$onReceive$0(this.f$0, this.f$1);
                }
            };
            StatusBar statusBar = this.mStatusBar;
            if (statusBar != null) {
                statusBar.executeRunnableDismissingKeyguard(r1, null, true, true, true);
            } else {
                r1.run();
            }
            if (intent.getBooleanExtra("android:smart_actions_enabled", false)) {
                ScreenshotSmartActions.notifyScreenshotAction(context, intent.getStringExtra("android:screenshot_id"), "android.intent.action.EDIT".equals(intent.getAction()) ? "Edit" : "Share", false);
            }
        }

        static /* synthetic */ void lambda$onReceive$0(Intent intent, Context context) {
            try {
                ActivityManagerWrapper.getInstance().closeSystemWindows("screenshot").get(3000, TimeUnit.MILLISECONDS);
                PendingIntent pendingIntent = (PendingIntent) intent.getParcelableExtra("android:screenshot_action_intent");
                if (intent.getBooleanExtra("android:screenshot_cancel_notification", false)) {
                    ScreenshotNotificationsController.cancelScreenshotNotification(context);
                }
                ActivityOptions makeBasic = ActivityOptions.makeBasic();
                makeBasic.setDisallowEnterPictureInPictureWhileLaunching(intent.getBooleanExtra("android:screenshot_disallow_enter_pip", false));
                try {
                    pendingIntent.send(context, 0, null, null, null, null, makeBasic.toBundle());
                } catch (PendingIntent.CanceledException e) {
                    Log.e("GlobalScreenshot", "Pending intent canceled", e);
                }
            } catch (InterruptedException | ExecutionException | TimeoutException e2) {
                Slog.e("GlobalScreenshot", "Unable to share screenshot", e2);
            }
        }
    }

    public static class TargetChosenReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            ScreenshotNotificationsController.cancelScreenshotNotification(context);
        }
    }

    public static class DeleteScreenshotReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra("android:screenshot_uri_id")) {
                ScreenshotNotificationsController.cancelScreenshotNotification(context);
                Uri parse = Uri.parse(intent.getStringExtra("android:screenshot_uri_id"));
                new DeleteImageInBackgroundTask(context).execute(parse);
                if (intent.getBooleanExtra("android:smart_actions_enabled", false)) {
                    ScreenshotSmartActions.notifyScreenshotAction(context, intent.getStringExtra("android:screenshot_id"), "Delete", false);
                }
            }
        }
    }

    public static class SmartActionsReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            PendingIntent pendingIntent = (PendingIntent) intent.getParcelableExtra("android:screenshot_action_intent");
            String stringExtra = intent.getStringExtra("android:screenshot_action_type");
            Slog.d("GlobalScreenshot", "Executing smart action [" + stringExtra + "]:" + pendingIntent.getIntent());
            try {
                pendingIntent.send(context, 0, null, null, null, null, ActivityOptions.makeBasic().toBundle());
            } catch (PendingIntent.CanceledException e) {
                Log.e("GlobalScreenshot", "Pending intent canceled", e);
            }
            ScreenshotSmartActions.notifyScreenshotAction(context, intent.getStringExtra("android:screenshot_id"), stringExtra, true);
        }
    }
}
