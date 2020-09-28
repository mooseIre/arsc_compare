package com.android.systemui.recents.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.MiuiMultiWindowUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.android.systemui.Constants;
import com.android.systemui.Dependency;
import com.android.systemui.HapticFeedBackImpl;
import com.android.systemui.SystemUICompat;
import com.android.systemui.plugins.R;
import com.android.systemui.recents.BaseRecentsImpl;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.RecentsConfiguration;
import com.android.systemui.recents.events.RecentsEventBus;
import com.android.systemui.recents.events.activity.HideMemoryAndDockEvent;
import com.android.systemui.recents.events.activity.ShowMemoryAndDockEvent;
import com.android.systemui.recents.events.activity.ShowTaskMenuEvent;
import com.android.systemui.recents.events.activity.StartSmallWindowEvent;
import com.android.systemui.recents.events.component.ChangeTaskLockStateEvent;
import com.android.systemui.recents.events.ui.ShowApplicationInfoEvent;
import com.android.systemui.recents.events.ui.dragndrop.DragDropTargetChangedEvent;
import com.android.systemui.recents.events.ui.dragndrop.DragEndEvent;
import com.android.systemui.recents.misc.RecentsPushEventHelper;
import com.android.systemui.recents.misc.SpringAnimationUtils;
import com.android.systemui.recents.misc.Utilities;
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.model.TaskStack;
import com.android.systemui.stackdivider.events.StartedDragingEvent;
import com.android.systemui.util.ViewAnimUtils;
import java.util.ArrayList;
import java.util.Iterator;
import miui.os.Build;
import miui.util.ScreenshotUtils;
import miui.view.animation.BackEaseOutInterpolator;

public class RecentMenuView extends FrameLayout implements View.OnClickListener, View.OnLongClickListener {
    /* access modifiers changed from: private */
    public Bitmap mBlurBackground;
    private final int mFastBlurMaxRadius;
    /* access modifiers changed from: private */
    public boolean mIsShowing;
    private boolean mIsSupportLock;
    private boolean mIsSupportSmallWindow;
    private boolean mIsTaskViewLeft;
    boolean mIsTouchInTaskViewBound;
    Drawable mLockDrawable;
    private ColorDrawable mMaskBackground;
    private ImageView mMenuItemInfo;
    private FrameLayout mMenuItemInfoContainer;
    private ImageView mMenuItemLock;
    private FrameLayout mMenuItemLockContainer;
    private ImageView mMenuItemMultiWindow;
    private FrameLayout mMenuItemMultiWindowContainer;
    private ImageView mMenuItemSmallWindow;
    private FrameLayout mMenuItemSmallWindowContainer;
    private final boolean mNeedBlurMask;
    private TimeInterpolator mShowMenuItemAnimInterpolator;
    ValueAnimator mShowOrHideAnim;
    private ArrayList<View> mSupportViews;
    /* access modifiers changed from: private */
    public Task mTask;
    /* access modifiers changed from: private */
    public TaskStackView mTaskStackView;
    /* access modifiers changed from: private */
    public TaskView mTaskView;
    Rect mTaskViewBound;
    Drawable mUnlockDrawable;
    private int mVerticalMargin;

    public RecentMenuView(Context context) {
        this(context, (AttributeSet) null);
    }

    public RecentMenuView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public RecentMenuView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public RecentMenuView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mIsTouchInTaskViewBound = false;
        this.mTaskViewBound = new Rect();
        this.mIsShowing = false;
        this.mBlurBackground = null;
        this.mShowOrHideAnim = new ValueAnimator();
        this.mShowMenuItemAnimInterpolator = new BackEaseOutInterpolator();
        this.mFastBlurMaxRadius = 36;
        this.mNeedBlurMask = false;
        this.mSupportViews = new ArrayList<>();
        this.mLockDrawable = context.getResources().getDrawable(R.drawable.ic_task_lock);
        this.mUnlockDrawable = context.getResources().getDrawable(R.drawable.ic_task_unlock);
        this.mMaskBackground = new ColorDrawable(getResources().getColor(R.color.recent_menu_mask_color));
        this.mVerticalMargin = context.getResources().getDimensionPixelSize(R.dimen.recents_task_menu_vertical_margin);
        setTranslationZ(10.0f);
        setVisibility(8);
        setClipChildren(false);
        this.mShowOrHideAnim = new ValueAnimator();
        this.mShowOrHideAnim.setDuration(180);
        this.mShowOrHideAnim.addListener(new AnimatorListenerAdapter() {
            public void onAnimationStart(Animator animator) {
                if (RecentMenuView.this.mIsShowing) {
                    RecentMenuView.this.setMaskBackground();
                    RecentMenuView.this.mTaskView.setTranslationZ(10.0f);
                    RecentMenuView.this.mTaskStackView.getMask().setAlpha(1.0f);
                    RecentMenuView.this.mTaskStackView.getMask().setTranslationZ(5.0f);
                    RecentsEventBus.getDefault().send(new HideMemoryAndDockEvent());
                    return;
                }
                RecentsEventBus.getDefault().send(new ShowMemoryAndDockEvent());
            }

            public void onAnimationEnd(Animator animator) {
                if (!RecentMenuView.this.mIsShowing) {
                    RecentMenuView.this.mTaskView.setTranslationZ(0.0f);
                    RecentMenuView.this.mTaskStackView.getMask().setAlpha(0.0f);
                    RecentMenuView.this.mTaskStackView.getMask().setTranslationZ(0.0f);
                    Bitmap unused = RecentMenuView.this.mBlurBackground = null;
                    RecentMenuView.this.mTaskView.getHeaderView().setAlpha(1.0f);
                    RecentMenuView.this.setVisibility(8);
                }
            }
        });
        this.mShowOrHideAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                ((Float) valueAnimator.getAnimatedValue()).floatValue();
            }
        });
        this.mIsSupportSmallWindow = checkIsSupportSmallWindow();
        this.mIsSupportLock = !checkIsNotSupportLock();
    }

    private boolean checkIsNotSupportLock() {
        return "lime".equals(Build.DEVICE) || "citrus".equals(Build.DEVICE) || "lemon".equals(Build.DEVICE) || isGlobalJ22();
    }

    private boolean isGlobalJ22() {
        return ("cannon".equals(Build.DEVICE) || "cannong".equals(Build.DEVICE)) && Build.IS_INTERNATIONAL_BUILD;
    }

    public static boolean checkIsSupportSmallWindow() {
        try {
            Class<?> cls = Class.forName("android.view.Display");
            return ((Boolean) cls.getDeclaredMethod("hasSmallFreeformFeature", (Class[]) null).invoke(cls, new Object[0])).booleanValue();
        } catch (Exception e) {
            Log.e("RecentMenuView", "isSupportSmallWindow: reflect error", e);
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        this.mMenuItemInfoContainer = (FrameLayout) findViewById(R.id.menu_item_info_container);
        this.mMenuItemLockContainer = (FrameLayout) findViewById(R.id.menu_item_lock_container);
        this.mMenuItemMultiWindowContainer = (FrameLayout) findViewById(R.id.menu_item_multi_window_container);
        this.mMenuItemSmallWindowContainer = (FrameLayout) findViewById(R.id.menu_item_small_window_container);
        this.mMenuItemInfo = (ImageView) findViewById(R.id.menu_item_info);
        this.mMenuItemLock = (ImageView) findViewById(R.id.menu_item_lock);
        this.mMenuItemMultiWindow = (ImageView) findViewById(R.id.menu_item_multi_window);
        this.mMenuItemSmallWindow = (ImageView) findViewById(R.id.menu_item_small_window);
        this.mMenuItemInfo.setImageResource(R.drawable.ic_task_setting);
        this.mMenuItemInfo.setContentDescription(this.mContext.getString(R.string.recent_menu_item_info));
        this.mMenuItemMultiWindow.setImageResource(R.drawable.ic_task_multi);
        this.mMenuItemSmallWindow.setImageResource(R.drawable.ic_task_small_window);
        this.mMenuItemInfo.setOnClickListener(this);
        this.mMenuItemLock.setOnClickListener(this);
        this.mMenuItemMultiWindow.setOnClickListener(this);
        this.mMenuItemSmallWindow.setOnClickListener(this);
        setOnClickListener(this);
        setOnLongClickListener(this);
        ViewAnimUtils.mouse(this.mMenuItemInfo);
        ViewAnimUtils.mouse(this.mMenuItemLock);
        ViewAnimUtils.mouse(this.mMenuItemMultiWindow);
        ViewAnimUtils.mouse(this.mMenuItemSmallWindow);
        if (this.mIsSupportLock) {
            this.mSupportViews.add(this.mMenuItemLockContainer);
        }
        this.mSupportViews.add(this.mMenuItemMultiWindowContainer);
        if (this.mIsSupportSmallWindow) {
            this.mSupportViews.add(this.mMenuItemSmallWindowContainer);
        }
        this.mSupportViews.add(this.mMenuItemInfoContainer);
    }

    public boolean onLongClick(View view) {
        if (!this.mIsTouchInTaskViewBound) {
            return false;
        }
        removeMenu(true);
        return false;
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (this.mTaskView == null || !this.mIsShowing) {
            return super.onTouchEvent(motionEvent);
        }
        int action = motionEvent.getAction();
        float rawX = motionEvent.getRawX();
        float rawY = motionEvent.getRawY();
        int i = action & 255;
        if (i == 0) {
            this.mTaskView.getHitRect(this.mTaskViewBound);
            this.mIsTouchInTaskViewBound = this.mTaskViewBound.contains((int) rawX, (int) rawY);
            if (this.mIsTouchInTaskViewBound) {
                SpringAnimationUtils.getInstance().startTaskViewScaleUpMenuModeAnim(this.mTaskView);
            }
        } else if (i == 2 && this.mIsTouchInTaskViewBound) {
            this.mIsTouchInTaskViewBound = this.mTaskViewBound.contains((int) rawX, (int) rawY);
            if (!this.mIsTouchInTaskViewBound) {
                SpringAnimationUtils.getInstance().startTaskViewScaleDownMenuModeAnim(this.mTaskView);
            }
        }
        return super.onTouchEvent(motionEvent);
    }

    public void onClick(View view) {
        String str;
        Task task = this.mTask;
        if (task != null) {
            Task.TaskKey taskKey = task.key;
            String packageName = (taskKey == null || taskKey.getComponent() == null) ? "" : this.mTask.key.getComponent().getPackageName();
            switch (view.getId()) {
                case R.id.menu_item_info:
                    RecentsEventBus.getDefault().send(new ShowApplicationInfoEvent(this.mTask));
                    RecentsPushEventHelper.sendShowAppInfoEvent(packageName);
                    break;
                case R.id.menu_item_lock:
                    Task task2 = this.mTask;
                    task2.isLocked = !task2.isLocked;
                    this.mTaskView.updateLockedFlagVisible(task2.isLocked, true, 200);
                    RecentsEventBus recentsEventBus = RecentsEventBus.getDefault();
                    Task task3 = this.mTask;
                    recentsEventBus.send(new ChangeTaskLockStateEvent(task3, task3.isLocked));
                    if (Constants.IS_SUPPORT_LINEAR_MOTOR_VIBRATE) {
                        if (this.mTask.isLocked) {
                            ((HapticFeedBackImpl) Dependency.get(HapticFeedBackImpl.class)).getHapticFeedbackUtil().performHapticFeedback("switch", false);
                        } else {
                            ((HapticFeedBackImpl) Dependency.get(HapticFeedBackImpl.class)).getHapticFeedbackUtil().performHapticFeedback("switch", false, 1);
                        }
                    }
                    if (this.mTask.isLocked) {
                        str = this.mContext.getString(R.string.accessibility_recent_task_locked_state);
                    } else {
                        str = this.mContext.getString(R.string.accessibility_recent_task_unlocked);
                    }
                    announceForAccessibility(str);
                    if (!this.mTask.isLocked) {
                        RecentsPushEventHelper.sendUnlockTaskEvent(packageName);
                        break;
                    } else {
                        RecentsPushEventHelper.sendLockTaskEvent(packageName);
                        break;
                    }
                case R.id.menu_item_multi_window:
                    if (!BaseRecentsImpl.toastForbidDockedWhenScreening(getContext())) {
                        final TaskStack.DockState[] dockStatesForCurrentOrientation = getDockStatesForCurrentOrientation();
                        if (dockStatesForCurrentOrientation[0] != null) {
                            this.mTaskStackView.postDelayed(new Runnable() {
                                public void run() {
                                    if (!Recents.getSystemServices().hasDockedTask()) {
                                        RecentMenuView.this.mTaskStackView.addIgnoreTask(RecentMenuView.this.mTask);
                                        RecentsEventBus.getDefault().send(new DragDropTargetChangedEvent(RecentMenuView.this.mTask, dockStatesForCurrentOrientation[0]));
                                        RecentsEventBus.getDefault().send(new DragEndEvent(RecentMenuView.this.mTask, RecentMenuView.this.mTaskView, dockStatesForCurrentOrientation[0]));
                                        RecentMenuView recentMenuView = RecentMenuView.this;
                                        recentMenuView.announceForAccessibility(recentMenuView.mContext.getString(R.string.accessibility_splite_screen_primary));
                                        return;
                                    }
                                    RecentMenuView.this.mTaskView.onClick(RecentMenuView.this.mTaskView);
                                    RecentMenuView recentMenuView2 = RecentMenuView.this;
                                    recentMenuView2.announceForAccessibility(recentMenuView2.mContext.getString(R.string.accessibility_splite_screen_secondary));
                                }
                            }, 250);
                            RecentsPushEventHelper.sendClickMultiWindowMenuEvent(packageName);
                            RecentsPushEventHelper.sendEnterMultiWindowEvent("clickMenu", packageName);
                            break;
                        }
                    } else {
                        return;
                    }
                    break;
                case R.id.menu_item_small_window:
                    if (SystemUICompat.startFreeformActivity(getContext(), this.mTask, packageName)) {
                        RecentsEventBus.getDefault().send(new StartSmallWindowEvent(packageName));
                        break;
                    }
                    break;
            }
        }
        removeMenu(true);
    }

    /* access modifiers changed from: private */
    public void setMaskBackground() {
        this.mBlurBackground = ScreenshotUtils.getScreenshot(getContext().getApplicationContext(), 0.25f, 0, 30000, true);
        if (Recents.getSystemServices().hasDockedTask() && this.mBlurBackground != null) {
            Rect rect = new Rect();
            this.mTaskStackView.getBoundsOnScreen(rect);
            rect.scale(0.25f);
            try {
                this.mBlurBackground = Bitmap.createBitmap(this.mBlurBackground, rect.left, rect.top, rect.width(), rect.height());
            } catch (IllegalArgumentException e) {
                Log.e("RecentMenuView", "Get blur menu background error: rect=" + rect + "   ScreenshotWidth=" + this.mBlurBackground.getWidth() + "   ScreenshotHeight=" + this.mBlurBackground.getHeight(), e);
                this.mBlurBackground = null;
            }
        }
    }

    public TaskStack.DockState[] getDockStatesForCurrentOrientation() {
        boolean z = getResources().getConfiguration().orientation == 2;
        RecentsConfiguration configuration = Recents.getConfiguration();
        return z ? configuration.isLargeScreen ? DockRegion.TABLET_LANDSCAPE : DockRegion.PHONE_LANDSCAPE : configuration.isLargeScreen ? DockRegion.TABLET_PORTRAIT : DockRegion.PHONE_PORTRAIT;
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        RecentsEventBus.getDefault().register(this, 3);
        super.onAttachedToWindow();
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        RecentsEventBus.getDefault().unregister(this);
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        int measuredWidth = this.mMenuItemLock.getMeasuredWidth();
        Rect rect = new Rect();
        this.mTaskView.getHitRect(rect);
        rect.top += this.mTaskView.getHeaderView().getHeight();
        rect.intersect(i, i2, i3, i4);
        if (this.mSupportViews.size() == 3) {
            layoutThreeItem(rect, measuredWidth, i3, i4);
        }
        if (this.mSupportViews.size() == 4) {
            layoutFourItem(rect, measuredWidth, i3, i4);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:22:0x015f  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void layoutThreeItem(android.graphics.Rect r19, int r20, int r21, int r22) {
        /*
            r18 = this;
            r7 = r18
            r0 = r19
            r8 = r20
            r9 = 3
            int[] r10 = new int[r9]
            int[] r11 = new int[r9]
            boolean r1 = r7.mIsTaskViewLeft
            r2 = 1053609165(0x3ecccccd, float:0.4)
            r3 = 1068708659(0x3fb33333, float:1.4)
            r4 = 0
            r5 = 1
            r6 = 2
            if (r1 == 0) goto L_0x0033
            int r1 = r0.right
            float r12 = (float) r1
            float r13 = (float) r8
            float r14 = r13 * r2
            float r12 = r12 + r14
            int r12 = (int) r12
            r10[r6] = r12
            r10[r4] = r12
            float r12 = (float) r1
            r14 = 1063675494(0x3f666666, float:0.9)
            float r13 = r13 * r14
            float r12 = r12 + r13
            int r12 = (int) r12
            r10[r5] = r12
            int r1 = r1 - r8
            int r12 = r19.centerY()
            goto L_0x004d
        L_0x0033:
            int r1 = r0.left
            float r12 = (float) r1
            float r13 = (float) r8
            float r14 = r13 * r3
            float r12 = r12 - r14
            int r12 = (int) r12
            r10[r6] = r12
            r10[r4] = r12
            float r12 = (float) r1
            r14 = 1072902963(0x3ff33333, float:1.9)
            float r13 = r13 * r14
            float r12 = r12 - r13
            int r12 = (int) r12
            r10[r5] = r12
            int r1 = r1 + r8
            int r12 = r19.centerY()
        L_0x004d:
            int r13 = r19.centerY()
            float r13 = (float) r13
            r14 = 1056964608(0x3f000000, float:0.5)
            float r15 = (float) r8
            float r14 = r14 * r15
            float r13 = r13 - r14
            int r13 = (int) r13
            r11[r5] = r13
            r13 = r11[r5]
            double r13 = (double) r13
            r16 = 4608083138725491507(0x3ff3333333333333, double:1.2)
            double r2 = (double) r8
            double r16 = r16 * r2
            double r13 = r13 - r16
            int r13 = (int) r13
            r11[r4] = r13
            r13 = r11[r5]
            double r13 = (double) r13
            double r13 = r13 + r16
            int r13 = (int) r13
            r11[r6] = r13
            r13 = r11[r4]
            int r14 = r7.mVerticalMargin
            r16 = 1058642330(0x3f19999a, float:0.6)
            r17 = 1070386381(0x3fcccccd, float:1.6)
            if (r13 >= r14) goto L_0x00d7
            boolean r1 = r7.mIsTaskViewLeft
            if (r1 == 0) goto L_0x00a0
            int r1 = r0.right
            float r12 = (float) r1
            float r16 = r16 * r15
            float r12 = r12 + r16
            int r12 = (int) r12
            r10[r4] = r12
            float r12 = (float) r1
            r13 = 1053609165(0x3ecccccd, float:0.4)
            float r14 = r15 * r13
            float r12 = r12 + r14
            int r12 = (int) r12
            r10[r5] = r12
            int r12 = r1 - r8
            r10[r6] = r12
            int r12 = r8 * 2
            int r1 = r1 - r12
            int r13 = r0.bottom
            goto L_0x00ba
        L_0x00a0:
            int r1 = r0.left
            float r12 = (float) r1
            float r17 = r17 * r15
            float r12 = r12 - r17
            int r12 = (int) r12
            r10[r4] = r12
            float r12 = (float) r1
            r13 = 1068708659(0x3fb33333, float:1.4)
            float r13 = r13 * r15
            float r12 = r12 - r13
            int r12 = (int) r12
            r10[r5] = r12
            r10[r6] = r1
            int r12 = r8 * 2
            int r1 = r1 + r12
            int r13 = r0.bottom
        L_0x00ba:
            int r13 = r13 - r12
            int r0 = r0.bottom
            int r12 = r0 - r8
            r11[r4] = r12
            float r12 = (float) r0
            r14 = 1053609165(0x3ecccccd, float:0.4)
            float r15 = r15 * r14
            float r12 = r12 + r15
            int r12 = (int) r12
            r11[r5] = r12
            double r14 = (double) r0
            r16 = 4603579539098121011(0x3fe3333333333333, double:0.6)
            double r2 = r2 * r16
            double r14 = r14 + r2
            int r0 = (int) r14
            r11[r6] = r0
            goto L_0x0133
        L_0x00d7:
            r2 = r11[r6]
            int r2 = r2 + r8
            int r3 = r22 - r14
            if (r2 <= r3) goto L_0x0132
            boolean r1 = r7.mIsTaskViewLeft
            if (r1 == 0) goto L_0x00ff
            int r1 = r0.right
            int r2 = r1 - r8
            r10[r4] = r2
            float r2 = (float) r1
            r3 = 1053609165(0x3ecccccd, float:0.4)
            float r3 = r3 * r15
            float r2 = r2 + r3
            int r2 = (int) r2
            r10[r5] = r2
            float r2 = (float) r1
            float r16 = r16 * r15
            float r2 = r2 + r16
            int r2 = (int) r2
            r10[r6] = r2
            int r2 = r8 * 2
            int r1 = r1 - r2
            int r3 = r0.top
            goto L_0x0119
        L_0x00ff:
            int r1 = r0.left
            r10[r4] = r1
            float r2 = (float) r1
            r3 = 1068708659(0x3fb33333, float:1.4)
            float r12 = r15 * r3
            float r2 = r2 - r12
            int r2 = (int) r2
            r10[r5] = r2
            float r2 = (float) r1
            float r3 = r15 * r17
            float r2 = r2 - r3
            int r2 = (int) r2
            r10[r6] = r2
            int r2 = r8 * 2
            int r1 = r1 + r2
            int r3 = r0.top
        L_0x0119:
            int r3 = r3 + r2
            int r0 = r0.top
            float r2 = (float) r0
            float r17 = r17 * r15
            float r2 = r2 - r17
            int r2 = (int) r2
            r11[r4] = r2
            float r2 = (float) r0
            r12 = 1068708659(0x3fb33333, float:1.4)
            float r15 = r15 * r12
            float r2 = r2 - r15
            int r2 = (int) r2
            r11[r5] = r2
            r11[r6] = r0
            r12 = r1
            r13 = r3
            goto L_0x0134
        L_0x0132:
            r13 = r12
        L_0x0133:
            r12 = r1
        L_0x0134:
            r0 = r10[r4]
            r1 = 10
            int r2 = r21 + -10
            int r0 = java.lang.Math.min(r0, r2)
            int r0 = java.lang.Math.max(r1, r0)
            r10[r4] = r0
            r0 = r10[r5]
            int r0 = java.lang.Math.min(r0, r2)
            int r0 = java.lang.Math.max(r1, r0)
            r10[r5] = r0
            r0 = r10[r6]
            int r0 = java.lang.Math.min(r0, r2)
            int r0 = java.lang.Math.max(r1, r0)
            r10[r6] = r0
            r14 = r4
        L_0x015d:
            if (r14 >= r9) goto L_0x017a
            java.util.ArrayList<android.view.View> r0 = r7.mSupportViews
            java.lang.Object r0 = r0.get(r14)
            r1 = r0
            android.view.View r1 = (android.view.View) r1
            if (r1 == 0) goto L_0x0177
            r2 = r10[r14]
            r3 = r11[r14]
            r0 = r18
            r4 = r12
            r5 = r13
            r6 = r20
            r0.layoutMenuItem(r1, r2, r3, r4, r5, r6)
        L_0x0177:
            int r14 = r14 + 1
            goto L_0x015d
        L_0x017a:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.recents.views.RecentMenuView.layoutThreeItem(android.graphics.Rect, int, int, int):void");
    }

    /* JADX WARNING: Removed duplicated region for block: B:22:0x01b6  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void layoutFourItem(android.graphics.Rect r21, int r22, int r23, int r24) {
        /*
            r20 = this;
            r7 = r20
            r0 = r21
            r8 = r22
            r9 = 4
            int[] r10 = new int[r9]
            int[] r11 = new int[r9]
            boolean r1 = r7.mIsTaskViewLeft
            r2 = 1061997773(0x3f4ccccd, float:0.8)
            r3 = 1069547520(0x3fc00000, float:1.5)
            r4 = 1056964608(0x3f000000, float:0.5)
            r5 = 3
            r6 = 0
            r12 = 1
            r13 = 2
            if (r1 == 0) goto L_0x0035
            int r1 = r0.right
            float r14 = (float) r1
            float r15 = (float) r8
            float r16 = r15 * r4
            float r14 = r14 + r16
            int r14 = (int) r14
            r10[r5] = r14
            r10[r6] = r14
            float r14 = (float) r1
            float r15 = r15 * r2
            float r14 = r14 + r15
            int r14 = (int) r14
            r10[r13] = r14
            r10[r12] = r14
            int r1 = r1 - r8
            int r14 = r21.centerY()
            goto L_0x0053
        L_0x0035:
            int r1 = r0.left
            float r14 = (float) r1
            float r15 = (float) r8
            float r16 = r15 * r3
            float r14 = r14 - r16
            int r14 = (int) r14
            r10[r5] = r14
            r10[r6] = r14
            float r14 = (float) r1
            r16 = 1072064102(0x3fe66666, float:1.8)
            float r15 = r15 * r16
            float r14 = r14 - r15
            int r14 = (int) r14
            r10[r13] = r14
            r10[r12] = r14
            int r1 = r1 + r8
            int r14 = r21.centerY()
        L_0x0053:
            int r15 = r21.centerY()
            float r15 = (float) r15
            r16 = 1036831949(0x3dcccccd, float:0.1)
            float r9 = (float) r8
            float r16 = r16 * r9
            float r15 = r15 + r16
            int r15 = (int) r15
            r11[r13] = r15
            r15 = r11[r13]
            float r15 = (float) r15
            r16 = 1067030938(0x3f99999a, float:1.2)
            float r16 = r16 * r9
            float r15 = r15 - r16
            int r15 = (int) r15
            r11[r12] = r15
            r15 = r11[r12]
            float r15 = (float) r15
            float r15 = r15 - r16
            int r15 = (int) r15
            r11[r6] = r15
            r15 = r11[r13]
            float r15 = (float) r15
            float r15 = r15 + r16
            int r15 = (int) r15
            r11[r5] = r15
            r15 = r11[r6]
            int r2 = r7.mVerticalMargin
            r17 = 1060320051(0x3f333333, float:0.7)
            r18 = 1053609165(0x3ecccccd, float:0.4)
            r19 = 1068708659(0x3fb33333, float:1.4)
            if (r15 >= r2) goto L_0x0105
            boolean r1 = r7.mIsTaskViewLeft
            if (r1 == 0) goto L_0x00b6
            int r1 = r0.right
            float r2 = (float) r1
            float r18 = r18 * r9
            float r2 = r2 + r18
            int r2 = (int) r2
            r10[r6] = r2
            float r2 = (float) r1
            float r4 = r4 * r9
            float r2 = r2 + r4
            int r2 = (int) r2
            r10[r12] = r2
            float r2 = (float) r1
            float r2 = r2 - r4
            int r2 = (int) r2
            r10[r13] = r2
            r2 = r10[r13]
            float r2 = (float) r2
            float r2 = r2 - r16
            int r2 = (int) r2
            r10[r5] = r2
            int r2 = r8 * 2
            int r1 = r1 - r2
            int r3 = r0.bottom
            goto L_0x00d9
        L_0x00b6:
            int r1 = r0.left
            float r2 = (float) r1
            float r19 = r19 * r9
            float r2 = r2 - r19
            int r2 = (int) r2
            r10[r6] = r2
            float r2 = (float) r1
            float r3 = r3 * r9
            float r2 = r2 - r3
            int r2 = (int) r2
            r10[r12] = r2
            float r2 = (float) r1
            float r4 = r4 * r9
            float r2 = r2 - r4
            int r2 = (int) r2
            r10[r13] = r2
            r2 = r10[r13]
            float r2 = (float) r2
            float r2 = r2 + r16
            int r2 = (int) r2
            r10[r5] = r2
            int r2 = r8 * 2
            int r1 = r1 + r2
            int r3 = r0.bottom
        L_0x00d9:
            int r3 = r3 - r2
            int r0 = r0.bottom
            float r0 = (float) r0
            r2 = 1050253722(0x3e99999a, float:0.3)
            float r2 = r2 * r9
            float r0 = r0 - r2
            int r0 = (int) r0
            r11[r12] = r0
            r0 = r11[r12]
            float r0 = (float) r0
            float r0 = r0 - r16
            int r0 = (int) r0
            r11[r6] = r0
            r0 = r11[r12]
            float r0 = (float) r0
            r2 = 1061997773(0x3f4ccccd, float:0.8)
            float r2 = r2 * r9
            float r0 = r0 + r2
            int r0 = (int) r0
            r11[r13] = r0
            r0 = r11[r12]
            float r0 = (float) r0
            float r9 = r9 * r17
            float r0 = r0 + r9
            int r0 = (int) r0
            r11[r5] = r0
        L_0x0101:
            r9 = r1
            r14 = r3
            goto L_0x017e
        L_0x0105:
            r15 = r11[r5]
            int r15 = r15 + r8
            int r2 = r24 - r2
            if (r15 <= r2) goto L_0x017d
            boolean r1 = r7.mIsTaskViewLeft
            if (r1 == 0) goto L_0x0133
            int r1 = r0.right
            float r2 = (float) r1
            float r4 = r4 * r9
            float r2 = r2 - r4
            int r2 = (int) r2
            r10[r12] = r2
            float r2 = (float) r1
            float r2 = r2 + r4
            int r2 = (int) r2
            r10[r13] = r2
            float r2 = (float) r1
            float r18 = r18 * r9
            float r2 = r2 + r18
            int r2 = (int) r2
            r10[r5] = r2
            r2 = r10[r12]
            float r2 = (float) r2
            float r2 = r2 - r16
            int r2 = (int) r2
            r10[r6] = r2
            int r2 = r8 * 2
            int r1 = r1 - r2
            int r3 = r0.top
            goto L_0x0156
        L_0x0133:
            int r1 = r0.left
            float r2 = (float) r1
            float r4 = r4 * r9
            float r2 = r2 - r4
            int r2 = (int) r2
            r10[r12] = r2
            float r2 = (float) r1
            float r3 = r3 * r9
            float r2 = r2 - r3
            int r2 = (int) r2
            r10[r13] = r2
            float r2 = (float) r1
            float r19 = r19 * r9
            float r2 = r2 - r19
            int r2 = (int) r2
            r10[r5] = r2
            r2 = r10[r12]
            float r2 = (float) r2
            float r2 = r2 + r16
            int r2 = (int) r2
            r10[r6] = r2
            int r2 = r8 * 2
            int r1 = r1 + r2
            int r3 = r0.top
        L_0x0156:
            int r3 = r3 + r2
            int r0 = r0.top
            float r0 = (float) r0
            float r17 = r17 * r9
            float r0 = r0 - r17
            int r0 = (int) r0
            r11[r13] = r0
            r0 = r11[r13]
            float r0 = (float) r0
            float r0 = r0 - r17
            int r0 = (int) r0
            r11[r6] = r0
            r0 = r11[r13]
            float r0 = (float) r0
            r2 = 1061997773(0x3f4ccccd, float:0.8)
            float r9 = r9 * r2
            float r0 = r0 - r9
            int r0 = (int) r0
            r11[r12] = r0
            r0 = r11[r13]
            float r0 = (float) r0
            float r0 = r0 + r16
            int r0 = (int) r0
            r11[r5] = r0
            goto L_0x0101
        L_0x017d:
            r9 = r1
        L_0x017e:
            r0 = r10[r6]
            r1 = 10
            int r2 = r23 + -10
            int r0 = java.lang.Math.min(r0, r2)
            int r0 = java.lang.Math.max(r1, r0)
            r10[r6] = r0
            r0 = r10[r12]
            int r0 = java.lang.Math.min(r0, r2)
            int r0 = java.lang.Math.max(r1, r0)
            r10[r12] = r0
            r0 = r10[r13]
            int r0 = java.lang.Math.min(r0, r2)
            int r0 = java.lang.Math.max(r1, r0)
            r10[r13] = r0
            r0 = r10[r5]
            int r0 = java.lang.Math.min(r0, r2)
            int r0 = java.lang.Math.max(r1, r0)
            r10[r5] = r0
            r12 = r6
            r13 = 4
        L_0x01b4:
            if (r12 >= r13) goto L_0x01d1
            java.util.ArrayList<android.view.View> r0 = r7.mSupportViews
            java.lang.Object r0 = r0.get(r12)
            r1 = r0
            android.view.View r1 = (android.view.View) r1
            if (r1 == 0) goto L_0x01ce
            r2 = r10[r12]
            r3 = r11[r12]
            r0 = r20
            r4 = r9
            r5 = r14
            r6 = r22
            r0.layoutMenuItem(r1, r2, r3, r4, r5, r6)
        L_0x01ce:
            int r12 = r12 + 1
            goto L_0x01b4
        L_0x01d1:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.recents.views.RecentMenuView.layoutFourItem(android.graphics.Rect, int, int, int):void");
    }

    private void layoutMenuItem(View view, int i, int i2, int i3, int i4, int i5) {
        view.setPivotX((float) (i3 - i));
        view.setPivotY((float) (i4 - i2));
        view.layout(i, i2, i + i5, i5 + i2);
    }

    private boolean isSupportSmallWindow() {
        Task.TaskKey taskKey;
        ArrayList freeformSuggestionList = MiuiMultiWindowUtils.getFreeformSuggestionList(this.mContext);
        Task task = this.mTask;
        if (task == null || (taskKey = task.key) == null || taskKey.getComponent() == null || freeformSuggestionList == null || !freeformSuggestionList.contains(this.mTask.key.getComponent().getPackageName()) || Recents.getSystemServices().hasDockedTask()) {
            return false;
        }
        return true;
    }

    public final void onBusEvent(ShowTaskMenuEvent showTaskMenuEvent) {
        String str;
        String str2;
        if (!this.mIsShowing) {
            this.mIsShowing = true;
            this.mTaskStackView.setIsShowingMenu(true);
            this.mTaskView = showTaskMenuEvent.taskView;
            this.mTask = this.mTaskView.getTask();
            this.mMenuItemMultiWindow.setEnabled(this.mTask.isDockable && Utilities.supportsMultiWindow() && !Utilities.isInSmallWindowMode(getContext()));
            if (this.mIsSupportLock) {
                this.mMenuItemLock.setImageDrawable(this.mTask.isLocked ? this.mUnlockDrawable : this.mLockDrawable);
                ImageView imageView = this.mMenuItemLock;
                if (this.mTask.isLocked) {
                    str2 = this.mContext.getString(R.string.recent_menu_item_unlock);
                } else {
                    str2 = this.mContext.getString(R.string.recent_menu_item_lock);
                }
                imageView.setContentDescription(str2);
            }
            ImageView imageView2 = this.mMenuItemMultiWindow;
            int i = 255;
            imageView2.setImageAlpha(imageView2.isEnabled() ? 255 : 80);
            ImageView imageView3 = this.mMenuItemMultiWindow;
            if (imageView3.isEnabled()) {
                str = this.mContext.getString(R.string.accessibility_menu_item_split_enable);
            } else {
                str = this.mContext.getString(R.string.accessibility_menu_item_split_disable);
            }
            imageView3.setContentDescription(str);
            if (this.mIsSupportSmallWindow) {
                this.mMenuItemSmallWindow.setEnabled(isSupportSmallWindow());
                ImageView imageView4 = this.mMenuItemSmallWindow;
                if (!imageView4.isEnabled()) {
                    i = 80;
                }
                imageView4.setImageAlpha(i);
            }
            this.mIsTaskViewLeft = this.mTaskStackView.getTaskViews().size() > 1 && this.mTaskView.getLeft() < this.mTaskStackView.getWidth() - this.mTaskView.getRight();
            setVisibility(0);
            setFocusable(true);
            int size = this.mSupportViews.size() > 1 ? 100 / (this.mSupportViews.size() - 1) : 0;
            for (int i2 = 0; i2 < this.mSupportViews.size(); i2++) {
                startShowItemAnim(this.mSupportViews.get(i2), 1.0f, (long) (i2 * size));
            }
            this.mShowOrHideAnim.setFloatValues(new float[]{0.0f, 1.0f});
            this.mShowOrHideAnim.start();
            SpringAnimationUtils.getInstance().startShowTaskMenuAnim(this.mTaskStackView, this.mTaskView);
            for (TaskView importantForAccessibility : this.mTaskStackView.getTaskViews()) {
                importantForAccessibility.setImportantForAccessibility(4);
            }
            Task.TaskKey taskKey = this.mTask.key;
            if (taskKey != null && taskKey.getComponent() != null && this.mTaskStackView.getStack() != null) {
                RecentsPushEventHelper.sendLongCLickTaskEvent(this.mTask.key.getComponent().getPackageName(), this.mTaskStackView.getStack().indexOfStackTask(this.mTask));
            }
        }
    }

    private void startShowItemAnim(View view, float f, long j) {
        view.setAlpha(0.0f);
        view.setScaleX(0.6f);
        view.setScaleY(0.6f);
        view.animate().alpha(f).scaleX(1.0f).scaleY(1.0f).setDuration(240).setStartDelay(j).setInterpolator(this.mShowMenuItemAnimInterpolator).start();
    }

    private void startHideItemAnim(View view) {
        view.animate().alpha(0.0f).scaleX(0.6f).scaleY(0.6f).setDuration(200).setStartDelay(0).start();
    }

    public final void onBusEvent(StartedDragingEvent startedDragingEvent) {
        removeMenu(false);
    }

    public boolean removeMenu(boolean z) {
        if (!this.mIsShowing) {
            return false;
        }
        this.mIsShowing = false;
        this.mTaskStackView.setIsShowingMenu(false);
        if (z) {
            Iterator<View> it = this.mSupportViews.iterator();
            while (it.hasNext()) {
                startHideItemAnim(it.next());
            }
        }
        this.mShowOrHideAnim.setFloatValues(new float[]{1.0f, 0.0f});
        this.mShowOrHideAnim.start();
        SpringAnimationUtils.getInstance().startRemoveTaskMenuAnim(this.mTaskStackView, this.mTaskView);
        for (TaskView importantForAccessibility : this.mTaskStackView.getTaskViews()) {
            importantForAccessibility.setImportantForAccessibility(0);
        }
        this.mTaskView.sendAccessibilityEvent(8);
        return true;
    }

    public void setTaskStackView(TaskStackView taskStackView) {
        this.mTaskStackView = taskStackView;
    }

    public boolean isShowing() {
        return this.mIsShowing;
    }

    public boolean isShowOrHideAnimRunning() {
        return this.mShowOrHideAnim.isRunning();
    }
}
