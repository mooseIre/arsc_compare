package com.android.systemui.recents.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Shader;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewDebug;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.R;
import com.android.systemui.proxy.ActivityManager$TaskThumbnailInfo;
import com.android.systemui.recents.BaseRecentsImpl;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.events.RecentsEventBus;
import com.android.systemui.recents.events.activity.TaskSnapshotChangedEvent;
import com.android.systemui.recents.misc.SpringAnimationImpl;
import com.android.systemui.recents.misc.SpringAnimationUtils;
import com.android.systemui.recents.misc.Utilities;
import com.android.systemui.recents.model.Task;
import com.android.systemui.statusbar.policy.ConfigurationController;

public class TaskViewThumbnail extends View {
    private static final ColorMatrix TMP_BRIGHTNESS_COLOR_MATRIX = new ColorMatrix();
    private static final ColorMatrix TMP_FILTER_COLOR_MATRIX = new ColorMatrix();
    private Paint mBgFillPaint;
    private BitmapShader mBitmapShader;
    private int mCornerRadius;
    private Paint mCoverPaint;
    @ViewDebug.ExportedProperty(category = "recents")
    private float mDimAlpha;
    @ViewDebug.ExportedProperty(category = "recents")
    private boolean mDisabledInSafeMode;
    private int mDisplayOrientation;
    private Rect mDisplayRect;
    private Paint mDrawPaint;
    private Paint mEdgePaint;
    private float mFullscreenThumbnailScale;
    @ViewDebug.ExportedProperty(category = "recents")
    private boolean mInvisible;
    private LightingColorFilter mLightingColorFilter;
    @ViewDebug.ExportedProperty(category = "recents")
    private float mRotateDegrees;
    private Matrix mScaleMatrix;
    public SpringAnimationImpl mSpringAnimationImpl;
    private Task mTask;
    private View mTaskBar;
    @ViewDebug.ExportedProperty(category = "recents")
    private Rect mTaskViewRect;
    private ActivityManager$TaskThumbnailInfo mThumbnailInfo;
    @ViewDebug.ExportedProperty(category = "recents")
    private Rect mThumbnailRect;
    @ViewDebug.ExportedProperty(category = "recents")
    private float mThumbnailScale;

    public TaskViewThumbnail(Context context) {
        this(context, (AttributeSet) null);
    }

    public TaskViewThumbnail(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public TaskViewThumbnail(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public TaskViewThumbnail(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mDisplayOrientation = 0;
        this.mDisplayRect = new Rect();
        this.mTaskViewRect = new Rect();
        this.mThumbnailRect = new Rect();
        this.mScaleMatrix = new Matrix();
        this.mDrawPaint = new Paint();
        this.mBgFillPaint = new Paint();
        this.mCoverPaint = new Paint();
        this.mLightingColorFilter = new LightingColorFilter(-1, 0);
        this.mEdgePaint = new Paint();
        this.mDrawPaint.setColorFilter(this.mLightingColorFilter);
        this.mDrawPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        if (Build.VERSION.SDK_INT >= 28) {
            this.mDrawPaint.setFilterBitmap(true);
            this.mDrawPaint.setAntiAlias(true);
        }
        this.mCornerRadius = getResources().getDimensionPixelSize(R.dimen.recents_task_view_rounded_corners_radius);
        this.mBgFillPaint.setColor(getResources().getColor(R.color.recent_task_bg_color));
        this.mBgFillPaint.setAntiAlias(true);
        this.mCoverPaint.setColor(getResources().getColor(R.color.recent_thumbnail_cover_color));
        this.mCoverPaint.setAntiAlias(true);
        this.mFullscreenThumbnailScale = 0.6f;
        this.mEdgePaint.setColor(getResources().getColor(R.color.recent_task_edge_color));
        this.mEdgePaint.setAntiAlias(true);
        this.mEdgePaint.setStyle(Paint.Style.STROKE);
        this.mEdgePaint.setStrokeWidth(2.0f);
        this.mSpringAnimationImpl = new SpringAnimationImpl(this);
    }

    public void onTaskViewSizeChanged(int i, int i2) {
        if (this.mTaskViewRect.width() != i || this.mTaskViewRect.bottom != i2) {
            this.mDisplayOrientation = Utilities.getAppConfiguration(getContext()).orientation;
            this.mDisplayRect = Recents.getSystemServices().getDisplayRect();
            this.mTaskViewRect.set(0, BaseRecentsImpl.mTaskBarHeight, i, i2);
            setLeftTopRightBottom(0, 0, i, i2);
            updateThumbnailScale();
        }
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        if (!this.mInvisible) {
            canvas.saveLayer(0.0f, 0.0f, (float) getWidth(), (float) getHeight(), (Paint) null);
            canvas.translate(0.0f, (float) BaseRecentsImpl.mTaskBarHeight);
            int width = this.mTaskViewRect.width();
            int height = this.mTaskViewRect.height();
            int min = Math.min(width, Math.round(((float) (this.mRotateDegrees == 0.0f ? this.mThumbnailRect.width() : this.mThumbnailRect.height())) * this.mThumbnailScale));
            int min2 = Math.min(height, Math.round(((float) (this.mRotateDegrees == 0.0f ? this.mThumbnailRect.height() : this.mThumbnailRect.width())) * this.mThumbnailScale));
            if (this.mBitmapShader == null || min <= 0 || min2 <= 0) {
                int i = this.mCornerRadius;
                canvas.drawRoundRect(0.0f, 0.0f, (float) width, (float) height, (float) i, (float) i, this.mBgFillPaint);
            } else {
                if (Math.abs(width - min) <= 2) {
                    width = min;
                }
                if (Math.abs(height - min2) <= 2) {
                    height = min2;
                }
                int i2 = this.mCornerRadius;
                Canvas canvas2 = canvas;
                canvas2.drawRoundRect(0.0f, 0.0f, (float) width, (float) height, (float) i2, (float) i2, this.mBgFillPaint);
                canvas2.drawRect(0.0f, 0.0f, (float) min, (float) min2, this.mDrawPaint);
            }
            Task task = this.mTask;
            if (task != null && task.isCoverThumbnail()) {
                int i3 = this.mCornerRadius;
                canvas.drawRoundRect(0.0f, 0.0f, (float) width, (float) height, (float) i3, (float) i3, this.mCoverPaint);
            }
            if (((ConfigurationController) Dependency.get(ConfigurationController.class)).isNightMode()) {
                int i4 = this.mCornerRadius;
                canvas.drawRoundRect(1.0f, 1.0f, (float) (width - 1), (float) (height - 1), (float) i4, (float) i4, this.mEdgePaint);
            }
            canvas.restore();
        }
    }

    /* access modifiers changed from: package-private */
    public void setThumbnail(Bitmap bitmap, ActivityManager$TaskThumbnailInfo activityManager$TaskThumbnailInfo) {
        if (bitmap != null) {
            Shader.TileMode tileMode = Shader.TileMode.CLAMP;
            BitmapShader bitmapShader = new BitmapShader(bitmap, tileMode, tileMode);
            this.mBitmapShader = bitmapShader;
            this.mDrawPaint.setShader(bitmapShader);
            this.mThumbnailInfo = activityManager$TaskThumbnailInfo;
            if (activityManager$TaskThumbnailInfo != null) {
                this.mFullscreenThumbnailScale = activityManager$TaskThumbnailInfo.scale;
            }
            ActivityManager$TaskThumbnailInfo activityManager$TaskThumbnailInfo2 = this.mThumbnailInfo;
            if (activityManager$TaskThumbnailInfo2 == null || activityManager$TaskThumbnailInfo2.insets == null) {
                this.mThumbnailRect.set(0, 0, bitmap.getWidth(), bitmap.getHeight());
            } else {
                Rect rect = this.mThumbnailRect;
                Rect rect2 = this.mThumbnailInfo.insets;
                Rect rect3 = this.mThumbnailInfo.insets;
                rect.set(0, 0, (int) (((float) bitmap.getWidth()) - (((float) (rect2.left + rect2.right)) * this.mFullscreenThumbnailScale)), (int) (((float) bitmap.getHeight()) - (((float) (rect3.top + rect3.bottom)) * this.mFullscreenThumbnailScale)));
            }
            updateThumbnailScale();
            return;
        }
        this.mBitmapShader = null;
        this.mDrawPaint.setShader((Shader) null);
        this.mThumbnailRect.setEmpty();
        this.mThumbnailInfo = null;
    }

    /* access modifiers changed from: package-private */
    public void updateThumbnailPaintFilter() {
        if (!this.mInvisible) {
            int i = (int) ((1.0f - this.mDimAlpha) * 255.0f);
            if (this.mBitmapShader == null) {
                this.mDrawPaint.setColorFilter((ColorFilter) null);
                this.mDrawPaint.setColor(Color.argb(255, i, i, i));
            } else if (this.mDisabledInSafeMode) {
                TMP_FILTER_COLOR_MATRIX.setSaturation(0.0f);
                float f = 1.0f - this.mDimAlpha;
                float[] array = TMP_BRIGHTNESS_COLOR_MATRIX.getArray();
                array[0] = f;
                array[6] = f;
                array[12] = f;
                float f2 = this.mDimAlpha;
                array[4] = f2 * 255.0f;
                array[9] = f2 * 255.0f;
                array[14] = f2 * 255.0f;
                TMP_FILTER_COLOR_MATRIX.preConcat(TMP_BRIGHTNESS_COLOR_MATRIX);
                ColorMatrixColorFilter colorMatrixColorFilter = new ColorMatrixColorFilter(TMP_FILTER_COLOR_MATRIX);
                this.mDrawPaint.setColorFilter(colorMatrixColorFilter);
                this.mBgFillPaint.setColorFilter(colorMatrixColorFilter);
            } else {
                this.mLightingColorFilter.setColorMultiply(Color.argb(255, i, i, i));
                this.mDrawPaint.setColorFilter(this.mLightingColorFilter);
                this.mDrawPaint.setColor(-1);
                this.mBgFillPaint.setColorFilter(this.mLightingColorFilter);
            }
            if (!this.mInvisible) {
                invalidate();
            }
        }
    }

    public void updateThumbnailScale() {
        float f;
        Rect rect;
        ActivityManager$TaskThumbnailInfo activityManager$TaskThumbnailInfo;
        int width;
        this.mDisplayOrientation = Utilities.getAppConfiguration(getContext()).orientation;
        float f2 = 0.0f;
        this.mRotateDegrees = 0.0f;
        this.mThumbnailScale = 1.0f;
        if (this.mBitmapShader != null) {
            boolean z = !this.mTask.isFreeformTask() || this.mTask.bounds == null;
            if (this.mTaskViewRect.isEmpty() || (activityManager$TaskThumbnailInfo = this.mThumbnailInfo) == null || activityManager$TaskThumbnailInfo.taskWidth == 0 || activityManager$TaskThumbnailInfo.taskHeight == 0) {
                this.mThumbnailScale = 0.0f;
            } else if (z) {
                if (this.mDisplayOrientation == 1) {
                    if (activityManager$TaskThumbnailInfo.screenOrientation == 1) {
                        this.mThumbnailScale = ((float) this.mTaskViewRect.width()) / ((float) this.mThumbnailRect.width());
                    } else {
                        this.mThumbnailScale = Math.max((((float) this.mTaskViewRect.width()) * 1.0f) / ((float) this.mThumbnailRect.height()), (((float) this.mTaskViewRect.height()) * 1.0f) / ((float) this.mThumbnailRect.width()));
                        this.mRotateDegrees = 90.0f;
                        f2 = (float) (this.mTaskViewRect.width() / 2);
                        width = this.mTaskViewRect.width() / 2;
                    }
                } else if (activityManager$TaskThumbnailInfo.screenOrientation == 2) {
                    this.mThumbnailScale = Math.max((((float) this.mTaskViewRect.width()) * 1.0f) / ((float) this.mThumbnailRect.width()), (((float) this.mTaskViewRect.height()) * 1.0f) / ((float) this.mThumbnailRect.height()));
                } else {
                    this.mThumbnailScale = Math.max((((float) this.mTaskViewRect.width()) * 1.0f) / ((float) this.mThumbnailRect.height()), (((float) this.mTaskViewRect.height()) * 1.0f) / ((float) this.mThumbnailRect.width()));
                    if (Recents.getSystemServices().getDisplayRotation() == 3) {
                        this.mRotateDegrees = 90.0f;
                        f2 = (float) (this.mTaskViewRect.width() / 2);
                        width = this.mTaskViewRect.width() / 2;
                    } else {
                        this.mRotateDegrees = -90.0f;
                        f2 = (((float) this.mThumbnailRect.width()) * this.mThumbnailScale) / 2.0f;
                        f = (((float) this.mThumbnailRect.width()) * this.mThumbnailScale) / 2.0f;
                        ActivityManager$TaskThumbnailInfo activityManager$TaskThumbnailInfo2 = this.mThumbnailInfo;
                        if (!(activityManager$TaskThumbnailInfo2 == null || (rect = activityManager$TaskThumbnailInfo2.insets) == null)) {
                            Matrix matrix = this.mScaleMatrix;
                            float f3 = this.mFullscreenThumbnailScale;
                            matrix.setTranslate(((float) (-rect.left)) * f3, ((float) (-rect.top)) * f3);
                        }
                        Matrix matrix2 = this.mScaleMatrix;
                        float f4 = this.mThumbnailScale;
                        matrix2.postScale(f4, f4);
                        this.mScaleMatrix.postRotate(this.mRotateDegrees, f2, f);
                        this.mBitmapShader.setLocalMatrix(this.mScaleMatrix);
                    }
                }
                f = (float) width;
                ActivityManager$TaskThumbnailInfo activityManager$TaskThumbnailInfo22 = this.mThumbnailInfo;
                Matrix matrix3 = this.mScaleMatrix;
                float f32 = this.mFullscreenThumbnailScale;
                matrix3.setTranslate(((float) (-rect.left)) * f32, ((float) (-rect.top)) * f32);
                Matrix matrix22 = this.mScaleMatrix;
                float f42 = this.mThumbnailScale;
                matrix22.postScale(f42, f42);
                this.mScaleMatrix.postRotate(this.mRotateDegrees, f2, f);
                this.mBitmapShader.setLocalMatrix(this.mScaleMatrix);
            } else {
                this.mThumbnailScale = Math.min(((float) this.mTaskViewRect.width()) / ((float) this.mThumbnailRect.width()), ((float) this.mTaskViewRect.height()) / ((float) this.mThumbnailRect.height()));
            }
            f = 0.0f;
            ActivityManager$TaskThumbnailInfo activityManager$TaskThumbnailInfo222 = this.mThumbnailInfo;
            Matrix matrix32 = this.mScaleMatrix;
            float f322 = this.mFullscreenThumbnailScale;
            matrix32.setTranslate(((float) (-rect.left)) * f322, ((float) (-rect.top)) * f322);
            Matrix matrix222 = this.mScaleMatrix;
            float f422 = this.mThumbnailScale;
            matrix222.postScale(f422, f422);
            this.mScaleMatrix.postRotate(this.mRotateDegrees, f2, f);
            this.mBitmapShader.setLocalMatrix(this.mScaleMatrix);
        }
        if (!this.mInvisible) {
            invalidate();
        }
    }

    /* access modifiers changed from: package-private */
    public void updateClipToTaskBar(View view) {
        this.mTaskBar = view;
        invalidate();
    }

    public void setDimAlpha(float f) {
        this.mDimAlpha = f;
        updateThumbnailPaintFilter();
    }

    /* access modifiers changed from: package-private */
    public void bindToTask(Task task, boolean z, int i, Rect rect) {
        this.mTask = task;
        this.mDisabledInSafeMode = z;
        this.mDisplayOrientation = i;
        this.mDisplayRect.set(rect);
        RecentsEventBus.getDefault().register(this);
    }

    /* access modifiers changed from: package-private */
    public void onTaskDataLoaded(ActivityManager$TaskThumbnailInfo activityManager$TaskThumbnailInfo) {
        Bitmap bitmap;
        Task task = this.mTask;
        if (task == null || (bitmap = task.thumbnail) == null) {
            setThumbnail((Bitmap) null, (ActivityManager$TaskThumbnailInfo) null);
        } else {
            setThumbnail(bitmap, activityManager$TaskThumbnailInfo);
        }
    }

    /* access modifiers changed from: package-private */
    public void unbindFromTask() {
        this.mTask = null;
        setThumbnail((Bitmap) null, (ActivityManager$TaskThumbnailInfo) null);
        RecentsEventBus.getDefault().unregister(this);
    }

    public final void onBusEvent(TaskSnapshotChangedEvent taskSnapshotChangedEvent) {
        Task task = this.mTask;
        if (task != null && taskSnapshotChangedEvent.taskId == task.key.id && taskSnapshotChangedEvent.snapshot != null && taskSnapshotChangedEvent.taskThumbnailInfo != null) {
            if (taskSnapshotChangedEvent.isDeterminedWhetherBlur || !task.isBlurThumbnail()) {
                setThumbnail(taskSnapshotChangedEvent.snapshot, taskSnapshotChangedEvent.taskThumbnailInfo);
            } else {
                Recents.getTaskLoader().updateBlurThumbnail(getContext(), this.mTask, taskSnapshotChangedEvent.snapshot, taskSnapshotChangedEvent.taskThumbnailInfo);
            }
        }
    }

    public void reset() {
        SpringAnimationUtils.getInstance().cancelAllSpringAnimation(this.mSpringAnimationImpl);
        setAlpha(1.0f);
        setScaleX(1.0f);
        setScaleY(1.0f);
        setTranslationX(0.0f);
        setTranslationY(0.0f);
    }

    public SpringAnimationImpl getSpringAnimationImpl() {
        return this.mSpringAnimationImpl;
    }
}
