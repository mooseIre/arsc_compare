package com.android.systemui.pip.phone;

import android.content.Context;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Region;
import android.hardware.input.InputManager;
import android.os.Handler;
import android.os.Looper;
import android.provider.DeviceConfig;
import android.view.BatchedInputEventReceiver;
import android.view.Choreographer;
import android.view.InputChannel;
import android.view.InputEvent;
import android.view.InputEventReceiver;
import android.view.InputMonitor;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import com.android.internal.policy.TaskResizingAlgorithm;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.model.SysUiState;
import com.android.systemui.pip.PipBoundsHandler;
import com.android.systemui.pip.PipTaskOrganizer;
import com.android.systemui.pip.PipUiEventLogger;
import com.android.systemui.util.DeviceConfigProxy;
import java.io.PrintWriter;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;

public class PipResizeGestureHandler {
    private boolean mAllowGesture;
    private final Context mContext;
    private int mCtrlType;
    private int mDelta;
    private final Rect mDisplayBounds = new Rect();
    private final int mDisplayId;
    private final PointF mDownPoint = new PointF();
    private final Rect mDragCornerSize = new Rect();
    private boolean mEnableUserResize;
    private InputEventReceiver mInputEventReceiver;
    private InputMonitor mInputMonitor;
    private boolean mIsAttached;
    private boolean mIsEnabled;
    private final Rect mLastDownBounds = new Rect();
    private final Rect mLastResizeBounds = new Rect();
    private final Executor mMainExecutor;
    private final Point mMaxSize = new Point();
    private final Point mMinSize = new Point();
    private final PipMotionHelper mMotionHelper;
    private final Function<Rect, Rect> mMovementBoundsSupplier;
    private final PipBoundsHandler mPipBoundsHandler;
    private PipTaskOrganizer mPipTaskOrganizer;
    private PipUiEventLogger mPipUiEventLogger;
    private final SysUiState mSysUiState;
    private boolean mThresholdCrossed;
    private final Rect mTmpBottomLeftCorner = new Rect();
    private final Rect mTmpBottomRightCorner = new Rect();
    private final Region mTmpRegion = new Region();
    private final Rect mTmpTopLeftCorner = new Rect();
    private final Rect mTmpTopRightCorner = new Rect();
    private float mTouchSlop;
    private final Runnable mUpdateMovementBoundsRunnable;

    public PipResizeGestureHandler(Context context, PipBoundsHandler pipBoundsHandler, PipMotionHelper pipMotionHelper, DeviceConfigProxy deviceConfigProxy, PipTaskOrganizer pipTaskOrganizer, Function<Rect, Rect> function, Runnable runnable, SysUiState sysUiState, PipUiEventLogger pipUiEventLogger) {
        this.mContext = context;
        this.mDisplayId = context.getDisplayId();
        this.mMainExecutor = context.getMainExecutor();
        this.mPipBoundsHandler = pipBoundsHandler;
        this.mMotionHelper = pipMotionHelper;
        this.mPipTaskOrganizer = pipTaskOrganizer;
        this.mMovementBoundsSupplier = function;
        this.mUpdateMovementBoundsRunnable = runnable;
        this.mSysUiState = sysUiState;
        this.mPipUiEventLogger = pipUiEventLogger;
        context.getDisplay().getRealSize(this.mMaxSize);
        reloadResources();
        this.mEnableUserResize = DeviceConfig.getBoolean("systemui", "pip_user_resize", true);
        deviceConfigProxy.addOnPropertiesChangedListener("systemui", this.mMainExecutor, new DeviceConfig.OnPropertiesChangedListener() {
            /* class com.android.systemui.pip.phone.PipResizeGestureHandler.AnonymousClass1 */

            public void onPropertiesChanged(DeviceConfig.Properties properties) {
                if (properties.getKeyset().contains("pip_user_resize")) {
                    PipResizeGestureHandler.this.mEnableUserResize = properties.getBoolean("pip_user_resize", true);
                }
            }
        });
    }

    public void onConfigurationChanged() {
        reloadResources();
    }

    private void reloadResources() {
        this.mDelta = this.mContext.getResources().getDimensionPixelSize(C0012R$dimen.pip_resize_edge_size);
        this.mTouchSlop = (float) ViewConfiguration.get(this.mContext).getScaledTouchSlop();
    }

    private void resetDragCorners() {
        Rect rect = this.mDragCornerSize;
        int i = this.mDelta;
        rect.set(0, 0, i, i);
        this.mTmpTopLeftCorner.set(this.mDragCornerSize);
        this.mTmpTopRightCorner.set(this.mDragCornerSize);
        this.mTmpBottomLeftCorner.set(this.mDragCornerSize);
        this.mTmpBottomRightCorner.set(this.mDragCornerSize);
    }

    private void disposeInputChannel() {
        InputEventReceiver inputEventReceiver = this.mInputEventReceiver;
        if (inputEventReceiver != null) {
            inputEventReceiver.dispose();
            this.mInputEventReceiver = null;
        }
        InputMonitor inputMonitor = this.mInputMonitor;
        if (inputMonitor != null) {
            inputMonitor.dispose();
            this.mInputMonitor = null;
        }
    }

    /* access modifiers changed from: package-private */
    public void onActivityPinned() {
        this.mIsAttached = true;
        updateIsEnabled();
    }

    /* access modifiers changed from: package-private */
    public void onActivityUnpinned() {
        this.mIsAttached = false;
        updateIsEnabled();
    }

    private void updateIsEnabled() {
        boolean z = this.mIsAttached && this.mEnableUserResize;
        if (z != this.mIsEnabled) {
            this.mIsEnabled = z;
            disposeInputChannel();
            if (this.mIsEnabled) {
                this.mInputMonitor = InputManager.getInstance().monitorGestureInput("pip-resize", this.mDisplayId);
                this.mInputEventReceiver = new SysUiInputEventReceiver(this.mInputMonitor.getInputChannel(), Looper.getMainLooper());
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onInputEvent(InputEvent inputEvent) {
        if (inputEvent instanceof MotionEvent) {
            onMotionEvent((MotionEvent) inputEvent);
        }
    }

    public boolean isWithinTouchRegion(int i, int i2) {
        Rect bounds = this.mMotionHelper.getBounds();
        if (bounds == null) {
            return false;
        }
        resetDragCorners();
        Rect rect = this.mTmpTopLeftCorner;
        int i3 = bounds.left;
        int i4 = this.mDelta;
        rect.offset(i3 - (i4 / 2), bounds.top - (i4 / 2));
        Rect rect2 = this.mTmpTopRightCorner;
        int i5 = bounds.right;
        int i6 = this.mDelta;
        rect2.offset(i5 - (i6 / 2), bounds.top - (i6 / 2));
        Rect rect3 = this.mTmpBottomLeftCorner;
        int i7 = bounds.left;
        int i8 = this.mDelta;
        rect3.offset(i7 - (i8 / 2), bounds.bottom - (i8 / 2));
        Rect rect4 = this.mTmpBottomRightCorner;
        int i9 = bounds.right;
        int i10 = this.mDelta;
        rect4.offset(i9 - (i10 / 2), bounds.bottom - (i10 / 2));
        this.mTmpRegion.setEmpty();
        this.mTmpRegion.op(this.mTmpTopLeftCorner, Region.Op.UNION);
        this.mTmpRegion.op(this.mTmpTopRightCorner, Region.Op.UNION);
        this.mTmpRegion.op(this.mTmpBottomLeftCorner, Region.Op.UNION);
        this.mTmpRegion.op(this.mTmpBottomRightCorner, Region.Op.UNION);
        return this.mTmpRegion.contains(i, i2);
    }

    public boolean willStartResizeGesture(MotionEvent motionEvent) {
        return this.mEnableUserResize && isInValidSysUiState() && isWithinTouchRegion((int) motionEvent.getRawX(), (int) motionEvent.getRawY());
    }

    private void setCtrlType(int i, int i2) {
        Rect bounds = this.mMotionHelper.getBounds();
        Rect apply = this.mMovementBoundsSupplier.apply(bounds);
        this.mDisplayBounds.set(apply.left, apply.top, apply.right + bounds.width(), apply.bottom + bounds.height());
        if (this.mTmpTopLeftCorner.contains(i, i2)) {
            int i3 = bounds.top;
            Rect rect = this.mDisplayBounds;
            if (!(i3 == rect.top || bounds.left == rect.left)) {
                int i4 = this.mCtrlType | 1;
                this.mCtrlType = i4;
                this.mCtrlType = i4 | 4;
            }
        }
        if (this.mTmpTopRightCorner.contains(i, i2)) {
            int i5 = bounds.top;
            Rect rect2 = this.mDisplayBounds;
            if (!(i5 == rect2.top || bounds.right == rect2.right)) {
                int i6 = this.mCtrlType | 2;
                this.mCtrlType = i6;
                this.mCtrlType = i6 | 4;
            }
        }
        if (this.mTmpBottomRightCorner.contains(i, i2)) {
            int i7 = bounds.bottom;
            Rect rect3 = this.mDisplayBounds;
            if (!(i7 == rect3.bottom || bounds.right == rect3.right)) {
                int i8 = this.mCtrlType | 2;
                this.mCtrlType = i8;
                this.mCtrlType = i8 | 8;
            }
        }
        if (this.mTmpBottomLeftCorner.contains(i, i2)) {
            int i9 = bounds.bottom;
            Rect rect4 = this.mDisplayBounds;
            if (i9 != rect4.bottom && bounds.left != rect4.left) {
                int i10 = this.mCtrlType | 1;
                this.mCtrlType = i10;
                this.mCtrlType = i10 | 8;
            }
        }
    }

    private boolean isInValidSysUiState() {
        return (this.mSysUiState.getFlags() & 51788) == 0;
    }

    private void onMotionEvent(MotionEvent motionEvent) {
        int actionMasked = motionEvent.getActionMasked();
        float x = motionEvent.getX();
        float y = motionEvent.getY();
        boolean z = false;
        if (actionMasked == 0) {
            this.mLastResizeBounds.setEmpty();
            if (isInValidSysUiState() && isWithinTouchRegion((int) x, (int) y)) {
                z = true;
            }
            this.mAllowGesture = z;
            if (z) {
                setCtrlType((int) x, (int) y);
                this.mDownPoint.set(x, y);
                this.mLastDownBounds.set(this.mMotionHelper.getBounds());
            }
        } else if (this.mAllowGesture) {
            if (actionMasked != 1) {
                if (actionMasked == 2) {
                    if (!this.mThresholdCrossed) {
                        PointF pointF = this.mDownPoint;
                        if (Math.hypot((double) (x - pointF.x), (double) (y - pointF.y)) > ((double) this.mTouchSlop)) {
                            this.mThresholdCrossed = true;
                            this.mDownPoint.set(x, y);
                            this.mInputMonitor.pilferPointers();
                        }
                    }
                    if (this.mThresholdCrossed) {
                        Rect bounds = this.mMotionHelper.getBounds();
                        Rect rect = this.mLastResizeBounds;
                        PointF pointF2 = this.mDownPoint;
                        float f = pointF2.x;
                        float f2 = pointF2.y;
                        int i = this.mCtrlType;
                        Point point = this.mMinSize;
                        int i2 = point.x;
                        int i3 = point.y;
                        Point point2 = this.mMaxSize;
                        if (this.mLastDownBounds.width() > this.mLastDownBounds.height()) {
                            z = true;
                        }
                        rect.set(TaskResizingAlgorithm.resizeDrag(x, y, f, f2, bounds, i, i2, i3, point2, true, z));
                        this.mPipBoundsHandler.transformBoundsToAspectRatio(this.mLastResizeBounds);
                        this.mPipTaskOrganizer.scheduleUserResizePip(this.mLastDownBounds, this.mLastResizeBounds, null);
                        return;
                    }
                    return;
                } else if (actionMasked != 3) {
                    if (actionMasked == 5) {
                        this.mAllowGesture = false;
                        return;
                    }
                    return;
                }
            }
            if (!this.mLastResizeBounds.isEmpty()) {
                this.mPipTaskOrganizer.scheduleFinishResizePip(this.mLastResizeBounds, new Consumer() {
                    /* class com.android.systemui.pip.phone.$$Lambda$PipResizeGestureHandler$RnFltK7aDIBKCAT3ErPSvbZbyg */

                    @Override // java.util.function.Consumer
                    public final void accept(Object obj) {
                        PipResizeGestureHandler.this.lambda$onMotionEvent$1$PipResizeGestureHandler((Rect) obj);
                    }
                });
                this.mPipUiEventLogger.log(PipUiEventLogger.PipUiEventEnum.PICTURE_IN_PICTURE_RESIZE);
                return;
            }
            resetState();
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onMotionEvent$1 */
    public /* synthetic */ void lambda$onMotionEvent$1$PipResizeGestureHandler(Rect rect) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            /* class com.android.systemui.pip.phone.$$Lambda$PipResizeGestureHandler$Z3nEFx0Z3KpDDBgJ9VHLzJ4HnEg */

            public final void run() {
                PipResizeGestureHandler.this.lambda$onMotionEvent$0$PipResizeGestureHandler();
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onMotionEvent$0 */
    public /* synthetic */ void lambda$onMotionEvent$0$PipResizeGestureHandler() {
        this.mMotionHelper.synchronizePinnedStackBounds();
        this.mUpdateMovementBoundsRunnable.run();
        resetState();
    }

    private void resetState() {
        this.mCtrlType = 0;
        this.mAllowGesture = false;
        this.mThresholdCrossed = false;
    }

    /* access modifiers changed from: package-private */
    public void updateMaxSize(int i, int i2) {
        this.mMaxSize.set(i, i2);
    }

    /* access modifiers changed from: package-private */
    public void updateMinSize(int i, int i2) {
        this.mMinSize.set(i, i2);
    }

    public void dump(PrintWriter printWriter, String str) {
        String str2 = str + "  ";
        printWriter.println(str + "PipResizeGestureHandler");
        printWriter.println(str2 + "mAllowGesture=" + this.mAllowGesture);
        printWriter.println(str2 + "mIsAttached=" + this.mIsAttached);
        printWriter.println(str2 + "mIsEnabled=" + this.mIsEnabled);
        printWriter.println(str2 + "mEnableUserResize=" + this.mEnableUserResize);
        printWriter.println(str2 + "mThresholdCrossed=" + this.mThresholdCrossed);
    }

    /* access modifiers changed from: package-private */
    public class SysUiInputEventReceiver extends BatchedInputEventReceiver {
        SysUiInputEventReceiver(InputChannel inputChannel, Looper looper) {
            super(inputChannel, looper, Choreographer.getSfInstance());
        }

        public void onInputEvent(InputEvent inputEvent) {
            PipResizeGestureHandler.this.onInputEvent(inputEvent);
            finishInputEvent(inputEvent, true);
        }
    }
}
