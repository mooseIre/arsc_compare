package com.android.systemui.partialscreenshot.shape;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;

public class RectScreenshot extends PartialScreenshotShape {
    private Bitmap bitmap;
    private int mLastX;
    private int mLastY;
    private final Paint mPaintBackground;
    private final Paint mPaintSelection;
    private Rect mSelectionRect;
    private Point mStartPoint;
    private TouchAreaEnum mTouchArea = TouchAreaEnum.OUT_OF_BOUNDS;
    private int mTouchInsideSize = 160;
    private View view;

    public RectScreenshot(View view2) {
        this.view = view2;
        this.mPaintBackground = new Paint();
        this.mPaintBackground.setColor(-16777216);
        this.mPaintBackground.setAlpha(165);
        this.mPaintSelection = new Paint(0);
        this.mPaintSelection.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        this.mPaintSelection.setFlags(1);
    }

    public void startSelection(int i, int i2) {
        this.mStartPoint = new Point(i, i2);
    }

    public void updateSelection(int i, int i2) {
        if (this.mSelectionRect != null) {
            if (Math.max(this.mStartPoint.x, i) - Math.min(this.mStartPoint.x, i) > this.mTouchInsideSize && i <= this.view.getWidth()) {
                this.mSelectionRect.left = Math.min(this.mStartPoint.x, i);
                this.mSelectionRect.right = Math.max(this.mStartPoint.x, i);
            }
            if (Math.max(this.mStartPoint.y, i2) - Math.min(this.mStartPoint.y, i2) > this.mTouchInsideSize && i2 <= this.view.getHeight()) {
                this.mSelectionRect.top = Math.min(this.mStartPoint.y, i2);
                this.mSelectionRect.bottom = Math.max(this.mStartPoint.y, i2);
            }
        } else if (Math.max(this.mStartPoint.x, i) - Math.min(this.mStartPoint.x, i) > this.mTouchInsideSize && Math.max(this.mStartPoint.y, i2) - Math.min(this.mStartPoint.y, i2) > this.mTouchInsideSize) {
            this.mSelectionRect = new Rect();
            this.mSelectionRect.left = Math.min(this.mStartPoint.x, i);
            this.mSelectionRect.right = Math.max(this.mStartPoint.x, i);
            this.mSelectionRect.top = Math.min(this.mStartPoint.y, i2);
            this.mSelectionRect.bottom = Math.max(this.mStartPoint.y, i2);
        }
    }

    public Rect getSelectionRect() {
        return this.mSelectionRect;
    }

    public void draw(Canvas canvas) {
        Rect rect = this.mSelectionRect;
        if (rect != null) {
            int i = rect.right;
            int i2 = rect.left;
            if (i - i2 > 50) {
                int i3 = rect.bottom;
                int i4 = rect.top;
                if (i3 - i4 > 50) {
                    this.bitmap = Bitmap.createBitmap(i - i2, i3 - i4, Bitmap.Config.ARGB_8888);
                    new Canvas(this.bitmap).drawRect(0.0f, 0.0f, (float) this.bitmap.getWidth(), (float) this.bitmap.getHeight(), this.mPaintSelection);
                    Bitmap bitmap2 = this.bitmap;
                    Rect rect2 = this.mSelectionRect;
                    canvas.drawBitmap(bitmap2, (float) rect2.left, (float) rect2.top, (Paint) null);
                    this.bitmap.recycle();
                    Rect rect3 = this.mSelectionRect;
                    canvas.drawRect(0.0f, (float) rect3.top, (float) rect3.left, (float) rect3.bottom, this.mPaintBackground);
                    canvas.drawRect(0.0f, 0.0f, (float) this.view.getWidth(), (float) this.mSelectionRect.top, this.mPaintBackground);
                    Rect rect4 = this.mSelectionRect;
                    canvas.drawRect((float) rect4.right, (float) rect4.top, (float) this.view.getWidth(), (float) this.mSelectionRect.bottom, this.mPaintBackground);
                    canvas.drawRect(0.0f, (float) this.mSelectionRect.bottom, (float) this.view.getWidth(), (float) this.view.getHeight(), this.mPaintBackground);
                    DrawShapeUtil.drawTrimmingFrame(canvas, this.mSelectionRect);
                    return;
                }
                return;
            }
            return;
        }
        canvas.drawRect(0.0f, 0.0f, (float) this.view.getWidth(), (float) this.view.getHeight(), this.mPaintBackground);
    }

    public Bitmap getPartialBitmap(Bitmap bitmap2) {
        Rect rect = this.mSelectionRect;
        if (rect.left < 0) {
            rect.left = 0;
        }
        if (this.mSelectionRect.right > this.view.getWidth()) {
            this.mSelectionRect.right = this.view.getWidth();
        }
        if (this.mSelectionRect.bottom > this.view.getHeight()) {
            this.mSelectionRect.bottom = this.view.getHeight();
        }
        Rect rect2 = this.mSelectionRect;
        if (rect2.top < 0) {
            rect2.top = 0;
        }
        Rect rect3 = this.mSelectionRect;
        if (rect3.left + rect3.width() > bitmap2.getWidth()) {
            this.mSelectionRect.left = bitmap2.getWidth() - this.mSelectionRect.width();
        }
        Rect rect4 = this.mSelectionRect;
        if (rect4.top + rect4.height() > bitmap2.getHeight()) {
            this.mSelectionRect.top = bitmap2.getHeight() - this.mSelectionRect.height();
        }
        Bitmap createBitmap = Bitmap.createBitmap(this.mSelectionRect.width(), this.mSelectionRect.height(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(createBitmap);
        Paint paint = new Paint(7);
        paint.setColor(-1);
        canvas.drawRect(0.0f, 0.0f, (float) this.bitmap.getWidth(), (float) this.bitmap.getHeight(), paint);
        return DrawShapeUtil.getResultBitmap(createBitmap.getWidth(), createBitmap.getHeight(), createBitmap, bitmap2, this.mSelectionRect);
    }

    public void onActionDown(MotionEvent motionEvent) {
        this.mLastX = (int) motionEvent.getX();
        this.mLastY = (int) motionEvent.getY();
        handleTouchArea(this.mLastX, this.mLastY);
    }

    public void onActionMove(MotionEvent motionEvent) {
        if (this.mTouchArea == TouchAreaEnum.OUT_OF_BOUNDS) {
            handleTouchArea(this.mLastX, this.mLastY);
        }
        int x = (int) motionEvent.getX();
        int y = (int) motionEvent.getY();
        if (x >= 0 && x <= this.view.getWidth() && y >= 0 && y <= this.view.getHeight()) {
            if (!DrawShapeUtil.isUseful(x, y, this.mLastX, this.mLastY)) {
                this.mTouchArea = TouchAreaEnum.OUT_OF_BOUNDS;
                return;
            }
            switch (AnonymousClass1.$SwitchMap$com$android$systemui$partialscreenshot$shape$TouchAreaEnum[this.mTouchArea.ordinal()]) {
                case 1:
                    moveRect(x, y);
                    break;
                case 2:
                    moveHandleLeftTop(x, y);
                    break;
                case 3:
                    moveHandleRightTop(x, y);
                    break;
                case 4:
                    moveHandleLeftBottom(x, y);
                    break;
                case 5:
                    moveHandleRightBottom(x, y);
                    break;
                case 6:
                    moveHandleCenterLeft(x);
                    break;
                case 7:
                    moveHandleCenterTop(y);
                    break;
                case 8:
                    moveHandleCenterRight(x);
                    break;
                case 9:
                    moveHandleCenterBottom(y);
                    break;
            }
            this.mLastX = (int) motionEvent.getX();
            this.mLastY = (int) motionEvent.getY();
        }
    }

    /* renamed from: com.android.systemui.partialscreenshot.shape.RectScreenshot$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$android$systemui$partialscreenshot$shape$TouchAreaEnum = new int[TouchAreaEnum.values().length];

        /* JADX WARNING: Can't wrap try/catch for region: R(20:0|1|2|3|4|5|6|7|8|9|10|11|12|13|14|15|16|17|18|(3:19|20|22)) */
        /* JADX WARNING: Can't wrap try/catch for region: R(22:0|1|2|3|4|5|6|7|8|9|10|11|12|13|14|15|16|17|18|19|20|22) */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:11:0x0040 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:13:0x004b */
        /* JADX WARNING: Missing exception handler attribute for start block: B:15:0x0056 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:17:0x0062 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:19:0x006e */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0014 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:5:0x001f */
        /* JADX WARNING: Missing exception handler attribute for start block: B:7:0x002a */
        /* JADX WARNING: Missing exception handler attribute for start block: B:9:0x0035 */
        static {
            /*
                com.android.systemui.partialscreenshot.shape.TouchAreaEnum[] r0 = com.android.systemui.partialscreenshot.shape.TouchAreaEnum.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$android$systemui$partialscreenshot$shape$TouchAreaEnum = r0
                int[] r0 = $SwitchMap$com$android$systemui$partialscreenshot$shape$TouchAreaEnum     // Catch:{ NoSuchFieldError -> 0x0014 }
                com.android.systemui.partialscreenshot.shape.TouchAreaEnum r1 = com.android.systemui.partialscreenshot.shape.TouchAreaEnum.CENTER     // Catch:{ NoSuchFieldError -> 0x0014 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0014 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0014 }
            L_0x0014:
                int[] r0 = $SwitchMap$com$android$systemui$partialscreenshot$shape$TouchAreaEnum     // Catch:{ NoSuchFieldError -> 0x001f }
                com.android.systemui.partialscreenshot.shape.TouchAreaEnum r1 = com.android.systemui.partialscreenshot.shape.TouchAreaEnum.LEFT_TOP     // Catch:{ NoSuchFieldError -> 0x001f }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001f }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001f }
            L_0x001f:
                int[] r0 = $SwitchMap$com$android$systemui$partialscreenshot$shape$TouchAreaEnum     // Catch:{ NoSuchFieldError -> 0x002a }
                com.android.systemui.partialscreenshot.shape.TouchAreaEnum r1 = com.android.systemui.partialscreenshot.shape.TouchAreaEnum.RIGHT_TOP     // Catch:{ NoSuchFieldError -> 0x002a }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x002a }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x002a }
            L_0x002a:
                int[] r0 = $SwitchMap$com$android$systemui$partialscreenshot$shape$TouchAreaEnum     // Catch:{ NoSuchFieldError -> 0x0035 }
                com.android.systemui.partialscreenshot.shape.TouchAreaEnum r1 = com.android.systemui.partialscreenshot.shape.TouchAreaEnum.LEFT_BOTTOM     // Catch:{ NoSuchFieldError -> 0x0035 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0035 }
                r2 = 4
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0035 }
            L_0x0035:
                int[] r0 = $SwitchMap$com$android$systemui$partialscreenshot$shape$TouchAreaEnum     // Catch:{ NoSuchFieldError -> 0x0040 }
                com.android.systemui.partialscreenshot.shape.TouchAreaEnum r1 = com.android.systemui.partialscreenshot.shape.TouchAreaEnum.RIGHT_BOTTOM     // Catch:{ NoSuchFieldError -> 0x0040 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0040 }
                r2 = 5
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0040 }
            L_0x0040:
                int[] r0 = $SwitchMap$com$android$systemui$partialscreenshot$shape$TouchAreaEnum     // Catch:{ NoSuchFieldError -> 0x004b }
                com.android.systemui.partialscreenshot.shape.TouchAreaEnum r1 = com.android.systemui.partialscreenshot.shape.TouchAreaEnum.CENTER_LEFT     // Catch:{ NoSuchFieldError -> 0x004b }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x004b }
                r2 = 6
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x004b }
            L_0x004b:
                int[] r0 = $SwitchMap$com$android$systemui$partialscreenshot$shape$TouchAreaEnum     // Catch:{ NoSuchFieldError -> 0x0056 }
                com.android.systemui.partialscreenshot.shape.TouchAreaEnum r1 = com.android.systemui.partialscreenshot.shape.TouchAreaEnum.CENTER_TOP     // Catch:{ NoSuchFieldError -> 0x0056 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0056 }
                r2 = 7
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0056 }
            L_0x0056:
                int[] r0 = $SwitchMap$com$android$systemui$partialscreenshot$shape$TouchAreaEnum     // Catch:{ NoSuchFieldError -> 0x0062 }
                com.android.systemui.partialscreenshot.shape.TouchAreaEnum r1 = com.android.systemui.partialscreenshot.shape.TouchAreaEnum.CENTER_RIGHT     // Catch:{ NoSuchFieldError -> 0x0062 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0062 }
                r2 = 8
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0062 }
            L_0x0062:
                int[] r0 = $SwitchMap$com$android$systemui$partialscreenshot$shape$TouchAreaEnum     // Catch:{ NoSuchFieldError -> 0x006e }
                com.android.systemui.partialscreenshot.shape.TouchAreaEnum r1 = com.android.systemui.partialscreenshot.shape.TouchAreaEnum.CENTER_BOTTOM     // Catch:{ NoSuchFieldError -> 0x006e }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x006e }
                r2 = 9
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x006e }
            L_0x006e:
                int[] r0 = $SwitchMap$com$android$systemui$partialscreenshot$shape$TouchAreaEnum     // Catch:{ NoSuchFieldError -> 0x007a }
                com.android.systemui.partialscreenshot.shape.TouchAreaEnum r1 = com.android.systemui.partialscreenshot.shape.TouchAreaEnum.OUT_OF_BOUNDS     // Catch:{ NoSuchFieldError -> 0x007a }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x007a }
                r2 = 10
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x007a }
            L_0x007a:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.partialscreenshot.shape.RectScreenshot.AnonymousClass1.<clinit>():void");
        }
    }

    private void moveRect(int i, int i2) {
        Rect rect = this.mSelectionRect;
        int i3 = rect.left;
        int i4 = this.mLastX;
        rect.left = i3 + (i - i4);
        rect.right += i - i4;
        int i5 = rect.top;
        int i6 = this.mLastY;
        rect.top = i5 + (i2 - i6);
        rect.bottom += i2 - i6;
        if (rect.left < 0 || rect.right > this.view.getWidth()) {
            Rect rect2 = this.mSelectionRect;
            int i7 = rect2.left;
            int i8 = this.mLastX;
            rect2.left = i7 - (i - i8);
            rect2.right -= i - i8;
        }
        Rect rect3 = this.mSelectionRect;
        if (rect3.top < 0 || rect3.bottom > this.view.getHeight()) {
            Rect rect4 = this.mSelectionRect;
            int i9 = rect4.top;
            int i10 = this.mLastY;
            rect4.top = i9 - (i2 - i10);
            rect4.bottom -= i2 - i10;
        }
    }

    private void moveHandleCenterBottom(int i) {
        Rect rect = this.mSelectionRect;
        if (i - rect.top > this.mTouchInsideSize) {
            rect.bottom = i;
        }
    }

    private void moveHandleCenterRight(int i) {
        Rect rect = this.mSelectionRect;
        if (i - rect.left > this.mTouchInsideSize) {
            rect.right = i;
        }
    }

    private void moveHandleCenterTop(int i) {
        Rect rect = this.mSelectionRect;
        if (rect.bottom - i > this.mTouchInsideSize) {
            rect.top = i;
        }
    }

    private void moveHandleCenterLeft(int i) {
        Rect rect = this.mSelectionRect;
        if (rect.right - i > this.mTouchInsideSize) {
            rect.left = i;
        }
    }

    private void moveHandleRightBottom(int i, int i2) {
        Rect rect = this.mSelectionRect;
        if (i - rect.left > this.mTouchInsideSize) {
            rect.right = i;
        }
        Rect rect2 = this.mSelectionRect;
        if (i2 - rect2.top > this.mTouchInsideSize) {
            rect2.bottom = i2;
        }
    }

    private void moveHandleLeftBottom(int i, int i2) {
        Rect rect = this.mSelectionRect;
        if (rect.right - i > this.mTouchInsideSize) {
            rect.left = i;
        }
        Rect rect2 = this.mSelectionRect;
        if (i2 - rect2.top > this.mTouchInsideSize) {
            rect2.bottom = i2;
        }
    }

    private void moveHandleRightTop(int i, int i2) {
        Rect rect = this.mSelectionRect;
        if (i - rect.left > this.mTouchInsideSize) {
            rect.right = i;
        }
        Rect rect2 = this.mSelectionRect;
        if (rect2.bottom - i2 > this.mTouchInsideSize) {
            rect2.top = i2;
        }
    }

    private void moveHandleLeftTop(int i, int i2) {
        Rect rect = this.mSelectionRect;
        if (rect.right - i > this.mTouchInsideSize) {
            rect.left = i;
        }
        Rect rect2 = this.mSelectionRect;
        if (rect2.bottom - i2 > this.mTouchInsideSize) {
            rect2.top = i2;
        }
    }

    private void handleTouchArea(int i, int i2) {
        Rect rect = this.mSelectionRect;
        if (rect != null) {
            this.mTouchArea = DrawShapeUtil.handleArea(i, i2, rect);
        }
    }
}
