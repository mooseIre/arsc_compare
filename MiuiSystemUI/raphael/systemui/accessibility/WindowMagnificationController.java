package com.android.systemui.accessibility;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Binder;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceControl;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0016R$integer;
import com.android.systemui.C0017R$layout;
import com.android.systemui.C0021R$string;
import com.android.systemui.accessibility.MirrorWindowControl;
import com.android.systemui.shared.system.WindowManagerWrapper;

public class WindowMagnificationController implements View.OnTouchListener, SurfaceHolder.Callback, MirrorWindowControl.MirrorWindowDelegate {
    private final int mBorderSize;
    private View mBottomDrag;
    private final Context mContext;
    private final int mDisplayId;
    private final Point mDisplaySize = new Point();
    private View mDragView;
    private final PointF mLastDrag = new PointF();
    private View mLeftDrag;
    private final Rect mMagnificationFrame = new Rect();
    private final Rect mMagnificationFrameBoundary = new Rect();
    private SurfaceControl mMirrorSurface;
    private SurfaceView mMirrorSurfaceView;
    private View mMirrorView;
    private MirrorWindowControl mMirrorWindowControl;
    private View mOverlayView;
    private View mRightDrag;
    private float mScale;
    private final Rect mTmpRect = new Rect();
    private View mTopDrag;
    private final SurfaceControl.Transaction mTransaction = new SurfaceControl.Transaction();
    private final WindowManager mWm;

    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {
    }

    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
    }

    WindowMagnificationController(Context context, MirrorWindowControl mirrorWindowControl) {
        this.mContext = context;
        context.getDisplay().getRealSize(this.mDisplaySize);
        this.mDisplayId = this.mContext.getDisplayId();
        this.mWm = (WindowManager) context.getSystemService("window");
        Resources resources = context.getResources();
        this.mBorderSize = (int) resources.getDimension(C0012R$dimen.magnification_border_size);
        this.mScale = (float) resources.getInteger(C0016R$integer.magnification_default_scale);
        this.mMirrorWindowControl = mirrorWindowControl;
        if (mirrorWindowControl != null) {
            mirrorWindowControl.setWindowDelegate(this);
        }
    }

    /* access modifiers changed from: package-private */
    public void createWindowMagnification() {
        if (this.mMirrorView == null) {
            setInitialStartBounds();
            setMagnificationFrameBoundary();
            createOverlayWindow();
        }
    }

    private void createOverlayWindow() {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(-1, -1, 2039, 24, -2);
        layoutParams.gravity = 51;
        layoutParams.token = new Binder();
        layoutParams.setTitle(this.mContext.getString(C0021R$string.magnification_overlay_title));
        View view = new View(this.mContext);
        this.mOverlayView = view;
        view.getViewTreeObserver().addOnWindowAttachListener(new ViewTreeObserver.OnWindowAttachListener() {
            /* class com.android.systemui.accessibility.WindowMagnificationController.AnonymousClass1 */

            public void onWindowDetached() {
            }

            public void onWindowAttached() {
                WindowMagnificationController.this.mOverlayView.getViewTreeObserver().removeOnWindowAttachListener(this);
                WindowMagnificationController.this.createMirrorWindow();
                WindowMagnificationController.this.createControls();
            }
        });
        this.mOverlayView.setSystemUiVisibility(5894);
        this.mWm.addView(this.mOverlayView, layoutParams);
    }

    /* access modifiers changed from: package-private */
    public void deleteWindowMagnification() {
        SurfaceControl surfaceControl = this.mMirrorSurface;
        if (surfaceControl != null) {
            this.mTransaction.remove(surfaceControl).apply();
            this.mMirrorSurface = null;
        }
        View view = this.mOverlayView;
        if (view != null) {
            this.mWm.removeView(view);
            this.mOverlayView = null;
        }
        View view2 = this.mMirrorView;
        if (view2 != null) {
            this.mWm.removeView(view2);
            this.mMirrorView = null;
        }
        MirrorWindowControl mirrorWindowControl = this.mMirrorWindowControl;
        if (mirrorWindowControl != null) {
            mirrorWindowControl.destroyControl();
            throw null;
        }
    }

    /* access modifiers changed from: package-private */
    public void onConfigurationChanged(int i) {
        View view = this.mMirrorView;
        if (view != null) {
            this.mWm.removeView(view);
            createMirrorWindow();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void createMirrorWindow() {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(this.mMagnificationFrame.width() + (this.mBorderSize * 2), this.mMagnificationFrame.height() + ((int) this.mContext.getResources().getDimension(C0012R$dimen.magnification_drag_view_height)) + (this.mBorderSize * 2), 1000, 40, -2);
        layoutParams.gravity = 51;
        layoutParams.token = this.mOverlayView.getWindowToken();
        Rect rect = this.mMagnificationFrame;
        layoutParams.x = rect.left;
        layoutParams.y = rect.top;
        layoutParams.layoutInDisplayCutoutMode = 1;
        layoutParams.setTitle(this.mContext.getString(C0021R$string.magnification_window_title));
        View inflate = LayoutInflater.from(this.mContext).inflate(C0017R$layout.window_magnifier_view, (ViewGroup) null);
        this.mMirrorView = inflate;
        SurfaceView surfaceView = (SurfaceView) inflate.findViewById(C0015R$id.surface_view);
        this.mMirrorSurfaceView = surfaceView;
        surfaceView.setZOrderOnTop(true);
        this.mMirrorView.setSystemUiVisibility(5894);
        this.mWm.addView(this.mMirrorView, layoutParams);
        SurfaceHolder holder = this.mMirrorSurfaceView.getHolder();
        holder.addCallback(this);
        holder.setFormat(1);
        addDragTouchListeners();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void createControls() {
        MirrorWindowControl mirrorWindowControl = this.mMirrorWindowControl;
        if (mirrorWindowControl != null) {
            mirrorWindowControl.showControl(this.mOverlayView.getWindowToken());
            throw null;
        }
    }

    private void setInitialStartBounds() {
        Point point = this.mDisplaySize;
        int min = Math.min(point.x, point.y) / 2;
        Point point2 = this.mDisplaySize;
        int i = min / 2;
        int i2 = (point2.x / 2) - i;
        int i3 = (point2.y / 2) - i;
        this.mMagnificationFrame.set(i2, i3, i2 + min, min + i3);
    }

    private void createMirror() {
        SurfaceControl mirrorDisplay = WindowManagerWrapper.getInstance().mirrorDisplay(this.mDisplayId);
        this.mMirrorSurface = mirrorDisplay;
        if (mirrorDisplay.isValid()) {
            this.mTransaction.show(this.mMirrorSurface).reparent(this.mMirrorSurface, this.mMirrorSurfaceView.getSurfaceControl());
            modifyWindowMagnification(this.mTransaction);
            this.mTransaction.apply();
        }
    }

    private void addDragTouchListeners() {
        this.mDragView = this.mMirrorView.findViewById(C0015R$id.drag_handle);
        this.mLeftDrag = this.mMirrorView.findViewById(C0015R$id.left_handle);
        this.mTopDrag = this.mMirrorView.findViewById(C0015R$id.top_handle);
        this.mRightDrag = this.mMirrorView.findViewById(C0015R$id.right_handle);
        this.mBottomDrag = this.mMirrorView.findViewById(C0015R$id.bottom_handle);
        this.mDragView.setOnTouchListener(this);
        this.mLeftDrag.setOnTouchListener(this);
        this.mTopDrag.setOnTouchListener(this);
        this.mRightDrag.setOnTouchListener(this);
        this.mBottomDrag.setOnTouchListener(this);
    }

    private void modifyWindowMagnification(SurfaceControl.Transaction transaction) {
        Rect sourceBounds = getSourceBounds(this.mMagnificationFrame, this.mScale);
        this.mTmpRect.set(0, 0, this.mMagnificationFrame.width(), this.mMagnificationFrame.height());
        WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) this.mMirrorView.getLayoutParams();
        Rect rect = this.mMagnificationFrame;
        layoutParams.x = rect.left;
        layoutParams.y = rect.top;
        this.mWm.updateViewLayout(this.mMirrorView, layoutParams);
        transaction.setGeometry(this.mMirrorSurface, sourceBounds, this.mTmpRect, 0);
    }

    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (view == this.mDragView || view == this.mLeftDrag || view == this.mTopDrag || view == this.mRightDrag || view == this.mBottomDrag) {
            return handleDragTouchEvent(motionEvent);
        }
        return false;
    }

    private boolean handleDragTouchEvent(MotionEvent motionEvent) {
        int action = motionEvent.getAction();
        if (action == 0) {
            this.mLastDrag.set(motionEvent.getRawX(), motionEvent.getRawY());
            return true;
        } else if (action != 2) {
            return false;
        } else {
            moveMirrorWindow((int) (motionEvent.getRawX() - this.mLastDrag.x), (int) (motionEvent.getRawY() - this.mLastDrag.y));
            this.mLastDrag.set(motionEvent.getRawX(), motionEvent.getRawY());
            return true;
        }
    }

    private void moveMirrorWindow(int i, int i2) {
        if (updateMagnificationFramePosition(i, i2)) {
            modifyWindowMagnification(this.mTransaction);
            this.mTransaction.apply();
        }
    }

    private Rect getSourceBounds(Rect rect, float f) {
        int width = rect.width() / 2;
        int height = rect.height() / 2;
        int i = width - ((int) (((float) width) / f));
        int i2 = height - ((int) (((float) height) / f));
        return new Rect(rect.left + i, rect.top + i2, rect.right - i, rect.bottom - i2);
    }

    private void setMagnificationFrameBoundary() {
        int width = this.mMagnificationFrame.width() / 2;
        int height = this.mMagnificationFrame.height() / 2;
        float f = this.mScale;
        int i = width - ((int) (((float) width) / f));
        int i2 = height - ((int) (((float) height) / f));
        Point point = this.mDisplaySize;
        this.mMagnificationFrameBoundary.set(-i, -i2, point.x + i, point.y + i2);
    }

    private boolean updateMagnificationFramePosition(int i, int i2) {
        this.mTmpRect.set(this.mMagnificationFrame);
        this.mTmpRect.offset(i, i2);
        Rect rect = this.mTmpRect;
        int i3 = rect.left;
        Rect rect2 = this.mMagnificationFrameBoundary;
        int i4 = rect2.left;
        if (i3 < i4) {
            rect.offsetTo(i4, rect.top);
        } else {
            int i5 = rect.right;
            int i6 = rect2.right;
            if (i5 > i6) {
                int width = i6 - this.mMagnificationFrame.width();
                Rect rect3 = this.mTmpRect;
                rect3.offsetTo(width, rect3.top);
            }
        }
        Rect rect4 = this.mTmpRect;
        int i7 = rect4.top;
        Rect rect5 = this.mMagnificationFrameBoundary;
        int i8 = rect5.top;
        if (i7 < i8) {
            rect4.offsetTo(rect4.left, i8);
        } else {
            int i9 = rect4.bottom;
            int i10 = rect5.bottom;
            if (i9 > i10) {
                int height = i10 - this.mMagnificationFrame.height();
                Rect rect6 = this.mTmpRect;
                rect6.offsetTo(rect6.left, height);
            }
        }
        if (this.mTmpRect.equals(this.mMagnificationFrame)) {
            return false;
        }
        this.mMagnificationFrame.set(this.mTmpRect);
        return true;
    }

    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        createMirror();
    }
}
