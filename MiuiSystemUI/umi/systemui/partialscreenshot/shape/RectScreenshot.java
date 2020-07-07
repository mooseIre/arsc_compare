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
        Paint paint = new Paint();
        this.mPaintBackground = paint;
        paint.setColor(-16777216);
        this.mPaintBackground.setAlpha(204);
        Paint paint2 = new Paint(0);
        this.mPaintSelection = paint2;
        paint2.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
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
            Rect rect = new Rect();
            this.mSelectionRect = rect;
            rect.left = Math.min(this.mStartPoint.x, i);
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
        int y = (int) motionEvent.getY();
        this.mLastY = y;
        handleTouchArea(this.mLastX, y);
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
        static final /* synthetic */ int[] $SwitchMap$com$android$systemui$partialscreenshot$shape$TouchAreaEnum;

        /* JADX WARNING: Can't wrap try/catch for region: R(20:0|1|2|3|4|5|6|7|8|9|10|11|12|13|14|15|16|17|18|(3:19|20|22)) */
        /* JADX WARNING: Can't wrap try/catch for region: R(22:0|1|2|3|4|5|6|7|8|9|10|11|12|13|14|15|16|17|18|19|20|22) */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:11:0x003e */
        /* JADX WARNING: Missing exception handler attribute for start block: B:13:0x0049 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:15:0x0054 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:17:0x0060 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:19:0x006c */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0012 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:5:0x001d */
        /* JADX WARNING: Missing exception handler attribute for start block: B:7:0x0028 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:9:0x0033 */
        static {
            /*
                com.android.systemui.partialscreenshot.shape.TouchAreaEnum[] r0 = com.android.systemui.partialscreenshot.shape.TouchAreaEnum.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$android$systemui$partialscreenshot$shape$TouchAreaEnum = r0
                com.android.systemui.partialscreenshot.shape.TouchAreaEnum r1 = com.android.systemui.partialscreenshot.shape.TouchAreaEnum.CENTER     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = $SwitchMap$com$android$systemui$partialscreenshot$shape$TouchAreaEnum     // Catch:{ NoSuchFieldError -> 0x001d }
                com.android.systemui.partialscreenshot.shape.TouchAreaEnum r1 = com.android.systemui.partialscreenshot.shape.TouchAreaEnum.LEFT_TOP     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                int[] r0 = $SwitchMap$com$android$systemui$partialscreenshot$shape$TouchAreaEnum     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.android.systemui.partialscreenshot.shape.TouchAreaEnum r1 = com.android.systemui.partialscreenshot.shape.TouchAreaEnum.RIGHT_TOP     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                int[] r0 = $SwitchMap$com$android$systemui$partialscreenshot$shape$TouchAreaEnum     // Catch:{ NoSuchFieldError -> 0x0033 }
                com.android.systemui.partialscreenshot.shape.TouchAreaEnum r1 = com.android.systemui.partialscreenshot.shape.TouchAreaEnum.LEFT_BOTTOM     // Catch:{ NoSuchFieldError -> 0x0033 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0033 }
                r2 = 4
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0033 }
            L_0x0033:
                int[] r0 = $SwitchMap$com$android$systemui$partialscreenshot$shape$TouchAreaEnum     // Catch:{ NoSuchFieldError -> 0x003e }
                com.android.systemui.partialscreenshot.shape.TouchAreaEnum r1 = com.android.systemui.partialscreenshot.shape.TouchAreaEnum.RIGHT_BOTTOM     // Catch:{ NoSuchFieldError -> 0x003e }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x003e }
                r2 = 5
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x003e }
            L_0x003e:
                int[] r0 = $SwitchMap$com$android$systemui$partialscreenshot$shape$TouchAreaEnum     // Catch:{ NoSuchFieldError -> 0x0049 }
                com.android.systemui.partialscreenshot.shape.TouchAreaEnum r1 = com.android.systemui.partialscreenshot.shape.TouchAreaEnum.CENTER_LEFT     // Catch:{ NoSuchFieldError -> 0x0049 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0049 }
                r2 = 6
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0049 }
            L_0x0049:
                int[] r0 = $SwitchMap$com$android$systemui$partialscreenshot$shape$TouchAreaEnum     // Catch:{ NoSuchFieldError -> 0x0054 }
                com.android.systemui.partialscreenshot.shape.TouchAreaEnum r1 = com.android.systemui.partialscreenshot.shape.TouchAreaEnum.CENTER_TOP     // Catch:{ NoSuchFieldError -> 0x0054 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0054 }
                r2 = 7
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0054 }
            L_0x0054:
                int[] r0 = $SwitchMap$com$android$systemui$partialscreenshot$shape$TouchAreaEnum     // Catch:{ NoSuchFieldError -> 0x0060 }
                com.android.systemui.partialscreenshot.shape.TouchAreaEnum r1 = com.android.systemui.partialscreenshot.shape.TouchAreaEnum.CENTER_RIGHT     // Catch:{ NoSuchFieldError -> 0x0060 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0060 }
                r2 = 8
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0060 }
            L_0x0060:
                int[] r0 = $SwitchMap$com$android$systemui$partialscreenshot$shape$TouchAreaEnum     // Catch:{ NoSuchFieldError -> 0x006c }
                com.android.systemui.partialscreenshot.shape.TouchAreaEnum r1 = com.android.systemui.partialscreenshot.shape.TouchAreaEnum.CENTER_BOTTOM     // Catch:{ NoSuchFieldError -> 0x006c }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x006c }
                r2 = 9
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x006c }
            L_0x006c:
                int[] r0 = $SwitchMap$com$android$systemui$partialscreenshot$shape$TouchAreaEnum     // Catch:{ NoSuchFieldError -> 0x0078 }
                com.android.systemui.partialscreenshot.shape.TouchAreaEnum r1 = com.android.systemui.partialscreenshot.shape.TouchAreaEnum.OUT_OF_BOUNDS     // Catch:{ NoSuchFieldError -> 0x0078 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0078 }
                r2 = 10
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0078 }
            L_0x0078:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.partialscreenshot.shape.RectScreenshot.AnonymousClass1.<clinit>():void");
        }
    }

    private void moveRect(int i, int i2) {
        Rect rect = this.mSelectionRect;
        int i3 = rect.left;
        int i4 = this.mLastX;
        int i5 = i3 + (i - i4);
        rect.left = i5;
        int i6 = rect.right + (i - i4);
        rect.right = i6;
        int i7 = rect.top;
        int i8 = this.mLastY;
        rect.top = i7 + (i2 - i8);
        rect.bottom += i2 - i8;
        if (i5 < 0 || i6 > this.view.getWidth()) {
            Rect rect2 = this.mSelectionRect;
            int i9 = rect2.left;
            int i10 = this.mLastX;
            rect2.left = i9 - (i - i10);
            rect2.right -= i - i10;
        }
        Rect rect3 = this.mSelectionRect;
        if (rect3.top < 0 || rect3.bottom > this.view.getHeight()) {
            Rect rect4 = this.mSelectionRect;
            int i11 = rect4.top;
            int i12 = this.mLastY;
            rect4.top = i11 - (i2 - i12);
            rect4.bottom -= i2 - i12;
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
