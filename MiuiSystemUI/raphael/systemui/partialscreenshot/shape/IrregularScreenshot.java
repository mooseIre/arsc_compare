package com.android.systemui.partialscreenshot.shape;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;
import java.util.List;

public class IrregularScreenshot extends PartialScreenshotShape {
    private float behindNextX;
    private float behindNextY;
    private float behindX;
    private float behindY;
    private float crossoverBehindX;
    private float crossoverBehindY;
    private int crossoverPointCount = 0;
    private float crossoverPreX;
    private float crossoverPreY;
    private float endX;
    private float endY;
    private final float eps = 1.0E-6f;
    private boolean isBegin = false;
    private int isState = -1;
    private boolean isUp;
    private int mLastX;
    private int mLastY;
    private final Paint mPaintBackground;
    private final Paint mPaintSelection;
    private final Paint mPloyLine;
    private Rect mSelectionRect;
    private TouchAreaEnum mTouchArea = TouchAreaEnum.OUT_OF_BOUNDS;
    private int mTouchInsideSize = 200;
    private float pX;
    private float pY;
    private List<Float> pathX;
    private List<Float> pathY;
    private float preNextX;
    private float preNextY;
    private float preX;
    private float preY;
    private View view;

    public IrregularScreenshot(View view2) {
        this.view = view2;
        this.mPaintBackground = new Paint();
        this.mPaintBackground.setColor(-16777216);
        this.mPaintBackground.setAlpha(165);
        this.mPaintSelection = new Paint(0);
        this.mPaintSelection.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        this.mPaintSelection.setFlags(1);
        this.mPaintSelection.setStrokeJoin(Paint.Join.ROUND);
        this.mPaintSelection.setStrokeCap(Paint.Cap.ROUND);
        this.mPloyLine = new Paint();
        this.mPloyLine.setPathEffect(new DashPathEffect(new float[]{10.0f, 10.0f, 10.0f, 10.0f}, 2.0f));
        this.mPloyLine.setColor(-1);
        this.mPloyLine.setFlags(1);
        this.mPloyLine.setStrokeCap(Paint.Cap.ROUND);
        this.mPloyLine.setStrokeJoin(Paint.Join.ROUND);
        this.mPloyLine.setStrokeWidth(4.5f);
        this.mPloyLine.setStyle(Paint.Style.STROKE);
        this.pathX = new ArrayList();
        this.pathY = new ArrayList();
    }

    public Rect getmSelectionRect() {
        return this.mSelectionRect;
    }

    public void addPath(float f, float f2) {
        if (f > ((float) this.view.getWidth())) {
            f = (float) this.view.getWidth();
        }
        if (f2 > ((float) this.view.getHeight())) {
            f2 = (float) this.view.getHeight();
        }
        if (f < 0.0f) {
            f = 0.0f;
        }
        if (f2 < 0.0f) {
            f2 = 0.0f;
        }
        this.pathX.add(Float.valueOf(f));
        this.pathY.add(Float.valueOf(f2));
    }

    public void clear() {
        this.pathX.clear();
        this.pathY.clear();
    }

    public float getTop() {
        float floatValue = this.pathY.size() > 0 ? this.pathY.get(0).floatValue() : 0.0f;
        for (Float floatValue2 : this.pathY) {
            float floatValue3 = floatValue2.floatValue();
            if (floatValue3 < floatValue) {
                floatValue = floatValue3;
            }
        }
        if (floatValue < 0.0f) {
            return 0.0f;
        }
        return floatValue;
    }

    public float getLeft() {
        float floatValue = this.pathX.size() > 0 ? this.pathX.get(0).floatValue() : 0.0f;
        for (Float floatValue2 : this.pathX) {
            float floatValue3 = floatValue2.floatValue();
            if (floatValue3 < floatValue) {
                floatValue = floatValue3;
            }
        }
        if (floatValue < 0.0f) {
            return 0.0f;
        }
        return floatValue;
    }

    public float getBottom() {
        float floatValue = this.pathY.size() > 0 ? this.pathY.get(0).floatValue() : 0.0f;
        for (Float floatValue2 : this.pathY) {
            float floatValue3 = floatValue2.floatValue();
            if (floatValue3 > floatValue) {
                floatValue = floatValue3;
            }
        }
        if (floatValue < 0.0f) {
            return 0.0f;
        }
        return floatValue;
    }

    public float getRight() {
        float floatValue = this.pathX.size() > 0 ? this.pathX.get(0).floatValue() : 0.0f;
        for (Float floatValue2 : this.pathX) {
            float floatValue3 = floatValue2.floatValue();
            if (floatValue3 > floatValue) {
                floatValue = floatValue3;
            }
        }
        if (floatValue < 0.0f) {
            return 0.0f;
        }
        return floatValue;
    }

    public int size() {
        return this.pathY.size();
    }

    public void setUp(boolean z) {
        this.isUp = z;
    }

    public void draw(Canvas canvas) {
        if (size() <= 1 || getRight() - getLeft() <= 1.0f || getBottom() - getTop() <= 1.0f) {
            canvas.drawRect(0.0f, 0.0f, (float) this.view.getWidth(), (float) this.view.getHeight(), this.mPaintBackground);
            return;
        }
        Bitmap createBitmap = Bitmap.createBitmap(((int) getRight()) - ((int) getLeft()), ((int) getBottom()) - ((int) getTop()), Bitmap.Config.ARGB_8888);
        Canvas canvas2 = new Canvas(createBitmap);
        if (this.isUp) {
            canvas2.drawRect(0.0f, 0.0f, (float) (((int) getRight()) - ((int) getLeft())), (float) (((int) getBottom()) - ((int) getTop())), this.mPaintBackground);
            Path path = new Path();
            path.moveTo(this.pathX.get(0).floatValue() - getLeft(), this.pathY.get(0).floatValue() - getTop());
            pathLink(path);
            isCloseLineorQuad(path);
            canvas2.drawPath(path, this.mPaintSelection);
            path.reset();
            Rect rect = this.mSelectionRect;
            if (rect == null) {
                this.mSelectionRect = new Rect((int) getLeft(), (int) getTop(), (int) getRight(), (int) getBottom());
            } else {
                rect.top = (int) getTop();
                this.mSelectionRect.bottom = (int) getBottom();
                this.mSelectionRect.left = (int) getLeft();
                this.mSelectionRect.right = (int) getRight();
            }
        } else {
            canvas2.drawRect(0.0f, 0.0f, (float) (((int) getRight()) - ((int) getLeft())), (float) (((int) getBottom()) - ((int) getTop())), this.mPaintBackground);
            Path path2 = new Path();
            path2.moveTo(this.pathX.get(0).floatValue() - getLeft(), this.pathY.get(0).floatValue() - getTop());
            pathLink(path2);
            canvas2.drawPath(path2, this.mPloyLine);
            path2.reset();
        }
        canvas.drawBitmap(createBitmap, (float) ((int) getLeft()), (float) ((int) getTop()), (Paint) null);
        createBitmap.recycle();
        canvas.drawRect(0.0f, (float) ((int) getTop()), (float) ((int) getLeft()), (float) ((int) getBottom()), this.mPaintBackground);
        canvas.drawRect(0.0f, 0.0f, (float) this.view.getWidth(), (float) ((int) getTop()), this.mPaintBackground);
        canvas.drawRect((float) ((int) getRight()), (float) ((int) getTop()), (float) this.view.getWidth(), (float) ((int) getBottom()), this.mPaintBackground);
        canvas.drawRect(0.0f, (float) ((int) getBottom()), (float) this.view.getWidth(), (float) this.view.getHeight(), this.mPaintBackground);
        if (this.isUp) {
            DrawShapeUtil.drawTrimmingFrame(canvas, this.mSelectionRect);
        }
    }

    public Bitmap getPartialBitmap(Bitmap bitmap) {
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
        if (rect3.left + rect3.width() > bitmap.getWidth()) {
            this.mSelectionRect.left = bitmap.getWidth() - this.mSelectionRect.width();
        }
        Rect rect4 = this.mSelectionRect;
        if (rect4.top + rect4.height() > bitmap.getHeight()) {
            this.mSelectionRect.top = bitmap.getHeight() - this.mSelectionRect.height();
        }
        Bitmap createBitmap = Bitmap.createBitmap(this.mSelectionRect.width(), this.mSelectionRect.height(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(createBitmap);
        Paint paint = new Paint(7);
        paint.setColor(-1);
        Path path = new Path();
        if (size() > 1) {
            path.moveTo(this.pathX.get(0).floatValue() - getLeft(), this.pathY.get(0).floatValue() - getTop());
            pathLink(path);
        }
        isCloseLineorQuad(path);
        canvas.drawPath(path, paint);
        return DrawShapeUtil.getResultBitmap(createBitmap.getWidth(), createBitmap.getHeight(), createBitmap, bitmap, this.mSelectionRect);
    }

    public boolean checkIsValid() {
        if (getRight() - getLeft() < 200.0f || getBottom() - getTop() < 200.0f) {
            clear();
            return false;
        }
        judgePath();
        if (this.crossoverPointCount <= 1 || Math.pow((double) (this.pathX.get(size() - 1).floatValue() - this.pathX.get(0).floatValue()), 2.0d) + Math.pow((double) (this.pathY.get(size() - 1).floatValue() - this.pathY.get(0).floatValue()), 2.0d) <= 250000.0d) {
            if (this.crossoverPointCount < 2) {
                cutpath();
                if (getRight() - getLeft() < 200.0f || getBottom() - getTop() < 200.0f) {
                    clear();
                    return false;
                }
            }
            return true;
        }
        clear();
        return false;
    }

    private void close(Path path) {
        if (((this.pathY.get(0).floatValue() - getTop()) + this.pathY.get(size() - 1).floatValue()) - getTop() > getBottom() - getTop()) {
            if (this.pathY.get(0).floatValue() > this.pathY.get(size() - 1).floatValue()) {
                path.quadTo(this.pathX.get(size() - 1).floatValue() - getLeft(), this.pathY.get(0).floatValue() - getTop(), this.pathX.get(0).floatValue() - getLeft(), this.pathY.get(0).floatValue() - getTop());
            } else {
                path.quadTo(this.pathX.get(0).floatValue() - getLeft(), this.pathY.get(size() - 1).floatValue() - getTop(), this.pathX.get(0).floatValue() - getLeft(), this.pathY.get(0).floatValue() - getTop());
            }
        } else if (this.pathY.get(0).floatValue() > this.pathY.get(size() - 1).floatValue()) {
            path.quadTo(this.pathX.get(0).floatValue() - getLeft(), this.pathY.get(size() - 1).floatValue() - getTop(), this.pathX.get(0).floatValue() - getLeft(), this.pathY.get(0).floatValue() - getTop());
        } else {
            path.quadTo(this.pathX.get(size() - 1).floatValue() - getLeft(), this.pathY.get(0).floatValue() - getTop(), this.pathX.get(0).floatValue() - getLeft(), this.pathY.get(0).floatValue() - getTop());
        }
    }

    private void pathLink(Path path) {
        path.moveTo(this.pathX.get(0).floatValue() - getLeft(), this.pathY.get(0).floatValue() - getTop());
        this.preX = this.pathX.get(0).floatValue() - getLeft();
        this.preY = this.pathY.get(0).floatValue() - getTop();
        for (int i = 1; i < size(); i++) {
            this.endX = ((this.preX + this.pathX.get(i).floatValue()) - getLeft()) / 2.0f;
            this.endY = ((this.preY + this.pathY.get(i).floatValue()) - getTop()) / 2.0f;
            path.quadTo(this.preX, this.preY, this.endX, this.endY);
            this.preX = this.pathX.get(i).floatValue() - getLeft();
            this.preY = this.pathY.get(i).floatValue() - getTop();
        }
    }

    private void isCloseLineorQuad(Path path) {
        if (this.isState != -1 || Math.pow((double) (this.pathX.get(size() - 1).floatValue() - this.pathX.get(0).floatValue()), 2.0d) + Math.pow((double) (this.pathY.get(size() - 1).floatValue() - this.pathY.get(0).floatValue()), 2.0d) >= 10000.0d) {
            int i = this.isState;
            if (i == -1) {
                close(path);
                this.isState = 1;
            } else if (i == 0) {
                path.close();
            } else {
                close(path);
            }
        } else {
            path.close();
            this.isState = 0;
        }
    }

    private void judgePath() {
        int i = 0;
        while (i < size() - 11) {
            this.pX = this.pathX.get(i).floatValue();
            this.pY = this.pathY.get(i).floatValue();
            int i2 = i + 1;
            this.preNextX = this.pathX.get(i2).floatValue();
            this.preNextY = this.pathY.get(i2).floatValue();
            int i3 = i + 10;
            while (i3 < size() - 1) {
                this.behindX = this.pathX.get(i3).floatValue();
                this.behindY = this.pathY.get(i3).floatValue();
                i3++;
                this.behindNextX = this.pathX.get(i3).floatValue();
                this.behindNextY = this.pathY.get(i3).floatValue();
                if (Math.min(this.pX, this.preNextX) < Math.max(this.behindNextX, this.behindX) && Math.min(this.pY, this.preNextY) < Math.max(this.behindNextY, this.behindY) && Math.min(this.behindNextX, this.behindX) < Math.max(this.pX, this.preNextX) && Math.min(this.behindNextY, this.behindY) < Math.max(this.pY, this.preNextY)) {
                    float f = this.preNextX;
                    float f2 = this.pX;
                    float f3 = this.behindY;
                    float f4 = this.pY;
                    float f5 = this.preNextY;
                    float f6 = this.behindX;
                    float f7 = ((f - f2) * (f3 - f4)) - ((f5 - f4) * (f6 - f2));
                    float f8 = this.behindNextY;
                    float f9 = this.behindNextX;
                    float f10 = ((f - f2) * (f8 - f4)) - ((f5 - f4) * (f9 - f2));
                    float f11 = ((f9 - f6) * (f4 - f3)) - ((f8 - f3) * (f2 - f6));
                    float f12 = ((f9 - f6) * (f5 - f3)) - ((f8 - f3) * (f - f6));
                    if (f7 * f10 <= 1.0E-6f && f11 * f12 <= 1.0E-6f) {
                        this.crossoverPointCount++;
                        if (this.crossoverPointCount == 1) {
                            this.crossoverPreX = f;
                            this.crossoverPreY = f5;
                            this.crossoverBehindX = f6;
                            this.crossoverBehindY = f3;
                        }
                    }
                }
            }
            i = i2;
        }
    }

    private double distance(float f, float f2, float f3, float f4) {
        return Math.sqrt(Math.pow((double) (f - f2), 2.0d) + Math.pow((double) (f3 - f4), 2.0d));
    }

    private void cutpath() {
        int i = 1;
        if (this.crossoverPointCount == 1 && (distance(this.pathX.get(size() - 1).floatValue(), this.pathX.get(0).floatValue(), this.pathY.get(size() - 1).floatValue(), this.pathY.get(0).floatValue()) > 200.0d || (distance(this.pathX.get(size() - 1).floatValue(), this.crossoverBehindX, this.pathY.get(size() - 1).floatValue(), this.crossoverBehindY) < 200.0d && distance(this.pathX.get(0).floatValue(), this.crossoverPreX, this.pathY.get(0).floatValue(), this.crossoverPreY) < 200.0d))) {
            int i2 = 0;
            int i3 = 0;
            for (int i4 = 0; i4 < size(); i4++) {
                if (this.pathX.get(i4).floatValue() == this.crossoverPreX && this.pathY.get(i4).floatValue() == this.crossoverPreY) {
                    i2 = i4;
                }
                if (this.pathX.get(i4).floatValue() == this.crossoverBehindX && this.pathY.get(i4).floatValue() == this.crossoverBehindY) {
                    i3 = i4;
                }
            }
            if (i2 > 0 && i3 > 0 && i3 - i2 > 5) {
                this.pathY = this.pathY.subList(i2, i3);
                this.pathX = this.pathX.subList(i2, i3);
                this.pathX.add(Float.valueOf(this.crossoverBehindX));
                this.pathY.add(Float.valueOf(this.crossoverBehindY));
                this.pathX.add(Float.valueOf(this.crossoverPreX));
                this.pathY.add(Float.valueOf(this.crossoverPreY));
            }
        }
        if (this.crossoverPointCount == 0) {
            float floatValue = this.pathX.get(0).floatValue();
            float floatValue2 = this.pathY.get(0).floatValue();
            float floatValue3 = this.pathX.get(size() - 1).floatValue();
            float floatValue4 = this.pathY.get(size() - 1).floatValue();
            int i5 = -1;
            float f = Float.MAX_VALUE;
            for (int i6 = 1; i6 < size() / 2; i6++) {
                if (Math.pow((double) (floatValue3 - this.pathX.get(i6).floatValue()), 2.0d) + Math.pow((double) (floatValue4 - this.pathY.get(i6).floatValue()), 2.0d) < ((double) f)) {
                    i = 1;
                    this.isBegin = true;
                    f = (float) (Math.pow((double) (floatValue3 - this.pathX.get(i6).floatValue()), 2.0d) + Math.pow((double) (floatValue4 - this.pathY.get(i6).floatValue()), 2.0d));
                    i5 = i6;
                } else {
                    i = 1;
                }
            }
            for (int size = size() / 2; size < size() - i; size++) {
                if (Math.pow((double) (floatValue - this.pathX.get(size).floatValue()), 2.0d) + Math.pow((double) (floatValue2 - this.pathY.get(size).floatValue()), 2.0d) < ((double) f)) {
                    this.isBegin = false;
                    i5 = size;
                    f = (float) (Math.pow((double) (floatValue - this.pathX.get(size).floatValue()), 2.0d) + Math.pow((double) (floatValue2 - this.pathY.get(size).floatValue()), 2.0d));
                }
            }
            if (i5 <= 0) {
                return;
            }
            if (this.isBegin && i5 + 7 < size()) {
                int i7 = i5 + 2;
                this.pathX = this.pathX.subList(i7, size());
                this.pathY = this.pathY.subList(i7, size());
            } else if (!this.isBegin && i5 > 8) {
                int i8 = i5 - 3;
                this.pathX = this.pathX.subList(0, i8);
                this.pathY = this.pathY.subList(0, i8);
            }
        }
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
        float x = motionEvent.getX();
        float y = motionEvent.getY();
        if (x >= 0.0f && x <= ((float) this.view.getWidth()) && y >= 0.0f && y <= ((float) this.view.getHeight())) {
            if (!DrawShapeUtil.isUseful((int) x, (int) y, this.mLastX, this.mLastY)) {
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

    /* renamed from: com.android.systemui.partialscreenshot.shape.IrregularScreenshot$1  reason: invalid class name */
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
            throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.partialscreenshot.shape.IrregularScreenshot.AnonymousClass1.<clinit>():void");
        }
    }

    private void moveRect(float f, float f2) {
        Rect rect = this.mSelectionRect;
        int i = this.mLastX;
        rect.left = (int) (((float) rect.left) + (f - ((float) i)));
        rect.right = (int) (((float) rect.right) + (f - ((float) i)));
        int i2 = this.mLastY;
        rect.top = (int) (((float) rect.top) + (f2 - ((float) i2)));
        rect.bottom = (int) (((float) rect.bottom) + (f2 - ((float) i2)));
        for (int i3 = 0; i3 < size(); i3++) {
            List<Float> list = this.pathX;
            list.set(i3, Float.valueOf((list.get(i3).floatValue() + f) - ((float) this.mLastX)));
            List<Float> list2 = this.pathY;
            list2.set(i3, Float.valueOf((list2.get(i3).floatValue() + f2) - ((float) this.mLastY)));
        }
        Rect rect2 = this.mSelectionRect;
        if (rect2.left < 0 || rect2.right > this.view.getWidth()) {
            Rect rect3 = this.mSelectionRect;
            int i4 = this.mLastX;
            rect3.left = (int) (((float) rect3.left) - (f - ((float) i4)));
            rect3.right = (int) (((float) rect3.right) - (f - ((float) i4)));
            for (int i5 = 0; i5 < size(); i5++) {
                List<Float> list3 = this.pathX;
                list3.set(i5, Float.valueOf((list3.get(i5).floatValue() - f) + ((float) this.mLastX)));
            }
        }
        Rect rect4 = this.mSelectionRect;
        if (rect4.top < 0 || rect4.bottom > this.view.getHeight()) {
            Rect rect5 = this.mSelectionRect;
            int i6 = this.mLastY;
            rect5.top = (int) (((float) rect5.top) - (f2 - ((float) i6)));
            rect5.bottom = (int) (((float) rect5.bottom) - (f2 - ((float) i6)));
            for (int i7 = 0; i7 < size(); i7++) {
                List<Float> list4 = this.pathY;
                list4.set(i7, Float.valueOf((list4.get(i7).floatValue() - f2) + ((float) this.mLastY)));
            }
        }
    }

    private void moveHandleCenterBottom(float f) {
        Rect rect = this.mSelectionRect;
        int i = rect.top;
        if (f - ((float) i) > ((float) this.mTouchInsideSize)) {
            float f2 = (float) (rect.bottom - i);
            for (int i2 = 0; i2 < size(); i2++) {
                int i3 = this.mSelectionRect.top;
                this.pathY.set(i2, Float.valueOf(((float) i3) + (((f - ((float) i3)) * (this.pathY.get(i2).floatValue() - ((float) this.mSelectionRect.top))) / f2)));
            }
            this.mSelectionRect.bottom = (int) f;
        }
    }

    private void moveHandleCenterRight(float f) {
        Rect rect = this.mSelectionRect;
        int i = rect.left;
        if (f - ((float) i) > ((float) this.mTouchInsideSize)) {
            float f2 = (float) (rect.right - i);
            for (int i2 = 0; i2 < size(); i2++) {
                float floatValue = this.pathX.get(i2).floatValue();
                int i3 = this.mSelectionRect.left;
                this.pathX.set(i2, Float.valueOf(((float) this.mSelectionRect.left) + (((floatValue - ((float) i3)) * (f - ((float) i3))) / f2)));
            }
            this.mSelectionRect.right = (int) f;
        }
    }

    private void moveHandleCenterTop(float f) {
        Rect rect = this.mSelectionRect;
        int i = rect.bottom;
        if (((float) i) - f > ((float) this.mTouchInsideSize)) {
            float f2 = (float) (i - rect.top);
            for (int i2 = 0; i2 < size(); i2++) {
                int i3 = this.mSelectionRect.bottom;
                this.pathY.set(i2, Float.valueOf(((float) i3) - (((((float) i3) - this.pathY.get(i2).floatValue()) * (((float) this.mSelectionRect.bottom) - f)) / f2)));
            }
            this.mSelectionRect.top = (int) f;
        }
    }

    private void moveHandleCenterLeft(float f) {
        Rect rect = this.mSelectionRect;
        int i = rect.right;
        if (((float) i) - f > ((float) this.mTouchInsideSize)) {
            float f2 = (float) (i - rect.left);
            for (int i2 = 0; i2 < size(); i2++) {
                int i3 = this.mSelectionRect.right;
                this.pathX.set(i2, Float.valueOf(((float) i3) - (((((float) i3) - this.pathX.get(i2).floatValue()) * (((float) this.mSelectionRect.right) - f)) / f2)));
            }
            this.mSelectionRect.left = (int) f;
        }
    }

    private void moveHandleRightBottom(float f, float f2) {
        Rect rect = this.mSelectionRect;
        int i = rect.left;
        if (f - ((float) i) > ((float) this.mTouchInsideSize)) {
            float f3 = (float) (rect.right - i);
            for (int i2 = 0; i2 < size(); i2++) {
                float floatValue = this.pathX.get(i2).floatValue();
                int i3 = this.mSelectionRect.left;
                this.pathX.set(i2, Float.valueOf(((float) this.mSelectionRect.left) + (((floatValue - ((float) i3)) * (f - ((float) i3))) / f3)));
            }
            this.mSelectionRect.right = (int) f;
        }
        Rect rect2 = this.mSelectionRect;
        int i4 = rect2.top;
        if (f2 - ((float) i4) > ((float) this.mTouchInsideSize)) {
            float f4 = (float) (rect2.bottom - i4);
            for (int i5 = 0; i5 < size(); i5++) {
                int i6 = this.mSelectionRect.top;
                this.pathY.set(i5, Float.valueOf(((float) i6) + (((f2 - ((float) i6)) * (this.pathY.get(i5).floatValue() - ((float) this.mSelectionRect.top))) / f4)));
            }
            this.mSelectionRect.bottom = (int) f2;
        }
    }

    private void moveHandleLeftBottom(float f, float f2) {
        Rect rect = this.mSelectionRect;
        int i = rect.right;
        if (((float) i) - f > ((float) this.mTouchInsideSize)) {
            float f3 = (float) (i - rect.left);
            for (int i2 = 0; i2 < size(); i2++) {
                int i3 = this.mSelectionRect.right;
                this.pathX.set(i2, Float.valueOf(((float) i3) - (((((float) i3) - this.pathX.get(i2).floatValue()) * (((float) this.mSelectionRect.right) - f)) / f3)));
            }
            this.mSelectionRect.left = (int) f;
        }
        Rect rect2 = this.mSelectionRect;
        int i4 = rect2.top;
        if (f2 - ((float) i4) > ((float) this.mTouchInsideSize)) {
            float f4 = (float) (rect2.bottom - i4);
            for (int i5 = 0; i5 < size(); i5++) {
                int i6 = this.mSelectionRect.top;
                this.pathY.set(i5, Float.valueOf(((float) i6) + (((f2 - ((float) i6)) * (this.pathY.get(i5).floatValue() - ((float) this.mSelectionRect.top))) / f4)));
            }
            this.mSelectionRect.bottom = (int) f2;
        }
    }

    private void moveHandleRightTop(float f, float f2) {
        Rect rect = this.mSelectionRect;
        int i = rect.left;
        if (f - ((float) i) > ((float) this.mTouchInsideSize)) {
            float f3 = (float) (rect.right - i);
            for (int i2 = 0; i2 < size(); i2++) {
                float floatValue = this.pathX.get(i2).floatValue();
                int i3 = this.mSelectionRect.left;
                this.pathX.set(i2, Float.valueOf(((float) this.mSelectionRect.left) + (((floatValue - ((float) i3)) * (f - ((float) i3))) / f3)));
            }
            this.mSelectionRect.right = (int) f;
        }
        Rect rect2 = this.mSelectionRect;
        int i4 = rect2.bottom;
        if (((float) i4) - f2 > ((float) this.mTouchInsideSize)) {
            float f4 = (float) (i4 - rect2.top);
            for (int i5 = 0; i5 < size(); i5++) {
                int i6 = this.mSelectionRect.bottom;
                this.pathY.set(i5, Float.valueOf(((float) i6) - (((((float) i6) - this.pathY.get(i5).floatValue()) * (((float) this.mSelectionRect.bottom) - f2)) / f4)));
            }
            this.mSelectionRect.top = (int) f2;
        }
    }

    private void moveHandleLeftTop(float f, float f2) {
        Rect rect = this.mSelectionRect;
        int i = rect.right;
        if (((float) i) - f > ((float) this.mTouchInsideSize)) {
            float f3 = (float) (i - rect.left);
            for (int i2 = 0; i2 < size(); i2++) {
                int i3 = this.mSelectionRect.right;
                this.pathX.set(i2, Float.valueOf(((float) i3) - (((((float) i3) - this.pathX.get(i2).floatValue()) * (((float) this.mSelectionRect.right) - f)) / f3)));
            }
            this.mSelectionRect.left = (int) f;
        }
        Rect rect2 = this.mSelectionRect;
        int i4 = rect2.bottom;
        if (((float) i4) - f2 > ((float) this.mTouchInsideSize)) {
            float f4 = (float) (i4 - rect2.top);
            for (int i5 = 0; i5 < size(); i5++) {
                int i6 = this.mSelectionRect.bottom;
                this.pathY.set(i5, Float.valueOf(((float) i6) - (((((float) i6) - this.pathY.get(i5).floatValue()) * (((float) this.mSelectionRect.bottom) - f2)) / f4)));
            }
            this.mSelectionRect.top = (int) f2;
        }
    }

    private void handleTouchArea(int i, int i2) {
        Rect rect = this.mSelectionRect;
        if (rect != null) {
            this.mTouchArea = DrawShapeUtil.handleArea(i, i2, rect);
        }
    }
}
