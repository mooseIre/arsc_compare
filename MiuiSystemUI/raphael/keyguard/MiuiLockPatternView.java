package com.android.keyguard;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.CanvasProperty;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.util.IntArray;
import android.util.Log;
import android.view.DisplayListCanvas;
import android.view.MotionEvent;
import android.view.RenderNodeAnimator;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.Interpolator;
import com.android.internal.widget.ExploreByTouchHelper;
import com.android.internal.widget.LockPatternView;
import com.android.keyguard.utils.MiuiKeyguardUtils;
import com.android.systemui.C0021R$string;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MiuiLockPatternView extends View {
    private long mAnimatingPeriodStart;
    private int mAspect;
    private final CellState[][] mCellStates;
    private final Path mCurrentPath;
    private final int mDotSize;
    private final int mDotSizeActivated;
    private boolean mEnableHapticFeedback;
    private int mErrorColor;
    private PatternExploreByTouchHelper mExploreByTouchHelper;
    private final Interpolator mFastOutSlowInInterpolator;
    private float mHitFactor;
    private float mInProgressX;
    private float mInProgressY;
    private boolean mInStealthMode;
    private boolean mInputEnabled;
    private final Rect mInvalidate;
    private final Interpolator mLinearOutSlowInInterpolator;
    private Drawable mNotSelectedDrawable;
    private OnPatternListener mOnPatternListener;
    private final Paint mPaint;
    private final Paint mPathPaint;
    private final int mPathWidth;
    private final ArrayList<LockPatternView.Cell> mPattern;
    private DisplayMode mPatternDisplayMode;
    private final boolean[][] mPatternDrawLookup;
    private boolean mPatternInProgress;
    private int mRegularColor;
    private Drawable mSelectedDrawable;
    private float mSquareHeight;
    private float mSquareWidth;
    private int mSuccessColor;
    private final Rect mTmpInvalidateRect;
    private boolean mUseLockPatternDrawable;

    public static class CellState {
        float alpha = 1.0f;
        int col;
        boolean hwAnimating;
        CanvasProperty<Float> hwCenterX;
        CanvasProperty<Float> hwCenterY;
        CanvasProperty<Paint> hwPaint;
        CanvasProperty<Float> hwRadius;
        public ValueAnimator lineAnimator;
        public float lineEndX = Float.MIN_VALUE;
        public float lineEndY = Float.MIN_VALUE;
        float radius;
        int row;
        float translationY;
    }

    public enum DisplayMode {
        CORRECT,
        ANIMATE,
        WRONG
    }

    public interface OnPatternListener {
        void onPatternCellAdded(List<LockPatternView.Cell> list);

        void onPatternCleared();

        void onPatternDetected(List<LockPatternView.Cell> list);

        void onPatternStart();
    }

    public MiuiLockPatternView(Context context) {
        this(context, null);
    }

    /* JADX DEBUG: Multi-variable search result rejected for r7v0, resolved type: com.android.keyguard.MiuiLockPatternView */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r8v2, types: [com.android.keyguard.MiuiLockPatternView$PatternExploreByTouchHelper, android.view.View$AccessibilityDelegate] */
    /* JADX WARNING: Unknown variable types count: 1 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public MiuiLockPatternView(android.content.Context r8, android.util.AttributeSet r9) {
        /*
        // Method dump skipped, instructions count: 312
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.keyguard.MiuiLockPatternView.<init>(android.content.Context, android.util.AttributeSet):void");
    }

    public CellState[][] getCellStates() {
        return this.mCellStates;
    }

    public void setInStealthMode(boolean z) {
        this.mInStealthMode = z;
    }

    public void setTactileFeedbackEnabled(boolean z) {
        this.mEnableHapticFeedback = z;
    }

    public void setOnPatternListener(OnPatternListener onPatternListener) {
        this.mOnPatternListener = onPatternListener;
    }

    public void setDisplayMode(DisplayMode displayMode) {
        this.mPatternDisplayMode = displayMode;
        if (displayMode == DisplayMode.ANIMATE) {
            if (this.mPattern.size() != 0) {
                this.mAnimatingPeriodStart = SystemClock.elapsedRealtime();
                LockPatternView.Cell cell = this.mPattern.get(0);
                this.mInProgressX = getCenterXForColumn(cell.getColumn());
                this.mInProgressY = getCenterYForRow(cell.getRow());
                clearPatternDrawLookup();
            } else {
                throw new IllegalStateException("you must have a pattern to animate if you want to set the display mode to animate");
            }
        }
        invalidate();
    }

    public void startCellStateAnimation(CellState cellState, float f, float f2, float f3, float f4, float f5, float f6, long j, long j2, Interpolator interpolator, Runnable runnable) {
        if (isHardwareAccelerated()) {
            startCellStateAnimationHw(cellState, f, f2, f3, f4, f5, f6, j, j2, interpolator, runnable);
        } else {
            startCellStateAnimationSw(cellState, f, f2, f3, f4, f5, f6, j, j2, interpolator, runnable);
        }
    }

    private void startCellStateAnimationSw(final CellState cellState, final float f, final float f2, final float f3, final float f4, final float f5, final float f6, long j, long j2, Interpolator interpolator, final Runnable runnable) {
        cellState.alpha = f;
        cellState.translationY = f3;
        cellState.radius = ((float) (this.mDotSize / 2)) * f5;
        ValueAnimator ofFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
        ofFloat.setDuration(j2);
        ofFloat.setStartDelay(j);
        ofFloat.setInterpolator(interpolator);
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.android.keyguard.MiuiLockPatternView.AnonymousClass1 */

            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float floatValue = Float.valueOf(valueAnimator.getAnimatedValue().toString()).floatValue();
                CellState cellState = cellState;
                float f = 1.0f - floatValue;
                cellState.alpha = (f * f) + (f2 * floatValue);
                cellState.translationY = (f3 * f) + (f4 * floatValue);
                cellState.radius = ((float) (MiuiLockPatternView.this.mDotSize / 2)) * ((f * f5) + (floatValue * f6));
                MiuiLockPatternView.this.invalidate();
            }
        });
        ofFloat.addListener(new AnimatorListenerAdapter(this) {
            /* class com.android.keyguard.MiuiLockPatternView.AnonymousClass2 */

            public void onAnimationEnd(Animator animator) {
                Runnable runnable = runnable;
                if (runnable != null) {
                    runnable.run();
                }
            }
        });
        ofFloat.start();
    }

    private void startCellStateAnimationHw(final CellState cellState, float f, float f2, float f3, float f4, float f5, float f6, long j, long j2, Interpolator interpolator, final Runnable runnable) {
        cellState.alpha = f2;
        cellState.translationY = f4;
        cellState.radius = ((float) (this.mDotSize / 2)) * f6;
        cellState.hwAnimating = true;
        cellState.hwCenterY = CanvasProperty.createFloat(getCenterYForRow(cellState.row) + f3);
        cellState.hwCenterX = CanvasProperty.createFloat(getCenterXForColumn(cellState.col));
        cellState.hwRadius = CanvasProperty.createFloat(((float) (this.mDotSize / 2)) * f5);
        this.mPaint.setColor(getCurrentColor(false));
        this.mPaint.setAlpha((int) (255.0f * f));
        cellState.hwPaint = CanvasProperty.createPaint(new Paint(this.mPaint));
        startRtFloatAnimation(cellState.hwCenterY, getCenterYForRow(cellState.row) + f4, j, j2, interpolator);
        startRtFloatAnimation(cellState.hwRadius, ((float) (this.mDotSize / 2)) * f6, j, j2, interpolator);
        startRtAlphaAnimation(cellState, f2, j, j2, interpolator, new AnimatorListenerAdapter(this) {
            /* class com.android.keyguard.MiuiLockPatternView.AnonymousClass3 */

            public void onAnimationEnd(Animator animator) {
                cellState.hwAnimating = false;
                Runnable runnable = runnable;
                if (runnable != null) {
                    runnable.run();
                }
            }
        });
        invalidate();
    }

    private void startRtAlphaAnimation(CellState cellState, float f, long j, long j2, Interpolator interpolator, Animator.AnimatorListener animatorListener) {
        RenderNodeAnimator renderNodeAnimator = new RenderNodeAnimator(cellState.hwPaint, 1, (float) ((int) (f * 255.0f)));
        renderNodeAnimator.setDuration(j2);
        renderNodeAnimator.setStartDelay(j);
        renderNodeAnimator.setInterpolator(interpolator);
        renderNodeAnimator.setTarget(this);
        renderNodeAnimator.addListener(animatorListener);
        renderNodeAnimator.start();
    }

    private void startRtFloatAnimation(CanvasProperty<Float> canvasProperty, float f, long j, long j2, Interpolator interpolator) {
        RenderNodeAnimator renderNodeAnimator = new RenderNodeAnimator(canvasProperty, f);
        renderNodeAnimator.setDuration(j2);
        renderNodeAnimator.setStartDelay(j);
        renderNodeAnimator.setInterpolator(interpolator);
        renderNodeAnimator.setTarget(this);
        renderNodeAnimator.start();
    }

    private void notifyCellAdded() {
        OnPatternListener onPatternListener = this.mOnPatternListener;
        if (onPatternListener != null) {
            onPatternListener.onPatternCellAdded(this.mPattern);
        }
        this.mExploreByTouchHelper.invalidateRoot();
    }

    private void notifyPatternStarted() {
        sendAccessEvent(C0021R$string.lockscreen_access_pattern_start);
        OnPatternListener onPatternListener = this.mOnPatternListener;
        if (onPatternListener != null) {
            onPatternListener.onPatternStart();
        }
    }

    private void notifyPatternDetected() {
        sendAccessEvent(C0021R$string.lockscreen_access_pattern_detected);
        OnPatternListener onPatternListener = this.mOnPatternListener;
        if (onPatternListener != null) {
            onPatternListener.onPatternDetected(this.mPattern);
        }
    }

    private void notifyPatternCleared() {
        sendAccessEvent(C0021R$string.lockscreen_access_pattern_cleared);
        OnPatternListener onPatternListener = this.mOnPatternListener;
        if (onPatternListener != null) {
            onPatternListener.onPatternCleared();
        }
    }

    public void clearPattern() {
        resetPattern();
    }

    /* access modifiers changed from: protected */
    public boolean dispatchHoverEvent(MotionEvent motionEvent) {
        return this.mExploreByTouchHelper.dispatchHoverEvent(motionEvent) | super.dispatchHoverEvent(motionEvent);
    }

    private void resetPattern() {
        this.mPattern.clear();
        clearPatternDrawLookup();
        this.mPatternDisplayMode = DisplayMode.CORRECT;
        invalidate();
    }

    private void clearPatternDrawLookup() {
        for (int i = 0; i < 3; i++) {
            for (int i2 = 0; i2 < 3; i2++) {
                this.mPatternDrawLookup[i][i2] = false;
            }
        }
    }

    public void disableInput() {
        this.mInputEnabled = false;
    }

    public void enableInput() {
        this.mInputEnabled = true;
    }

    /* access modifiers changed from: protected */
    public void onSizeChanged(int i, int i2, int i3, int i4) {
        int i5 = (i - ((View) this).mPaddingLeft) - ((View) this).mPaddingRight;
        this.mSquareWidth = ((float) i5) / 3.0f;
        int i6 = (i2 - ((View) this).mPaddingTop) - ((View) this).mPaddingBottom;
        this.mSquareHeight = ((float) i6) / 3.0f;
        this.mExploreByTouchHelper.invalidateRoot();
        if (this.mUseLockPatternDrawable) {
            this.mNotSelectedDrawable.setBounds(((View) this).mPaddingLeft, ((View) this).mPaddingTop, i5, i6);
            this.mSelectedDrawable.setBounds(((View) this).mPaddingLeft, ((View) this).mPaddingTop, i5, i6);
        }
    }

    private int resolveMeasured(int i, int i2) {
        int size = View.MeasureSpec.getSize(i);
        int mode = View.MeasureSpec.getMode(i);
        if (mode != Integer.MIN_VALUE) {
            return mode != 0 ? size : i2;
        }
        return Math.max(size, i2);
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int i, int i2) {
        int suggestedMinimumWidth = getSuggestedMinimumWidth();
        int suggestedMinimumHeight = getSuggestedMinimumHeight();
        int resolveMeasured = resolveMeasured(i, suggestedMinimumWidth);
        int resolveMeasured2 = resolveMeasured(i2, suggestedMinimumHeight);
        int i3 = this.mAspect;
        if (i3 == 0) {
            resolveMeasured = Math.min(resolveMeasured, resolveMeasured2);
            resolveMeasured2 = resolveMeasured;
        } else if (i3 == 1) {
            resolveMeasured2 = Math.min(resolveMeasured, resolveMeasured2);
        } else if (i3 == 2) {
            resolveMeasured = Math.min(resolveMeasured, resolveMeasured2);
        }
        Log.v("LockPatternView", "LockPatternView dimensions: " + resolveMeasured + "x" + resolveMeasured2);
        setMeasuredDimension(resolveMeasured, resolveMeasured2);
    }

    private LockPatternView.Cell detectAndAddHit(float f, float f2) {
        LockPatternView.Cell checkForNewHit = checkForNewHit(f, f2);
        LockPatternView.Cell cell = null;
        if (checkForNewHit == null) {
            return null;
        }
        ArrayList<LockPatternView.Cell> arrayList = this.mPattern;
        int i = 1;
        if (!arrayList.isEmpty()) {
            LockPatternView.Cell cell2 = arrayList.get(arrayList.size() - 1);
            int row = checkForNewHit.getRow() - cell2.getRow();
            int column = checkForNewHit.getColumn() - cell2.getColumn();
            int row2 = cell2.getRow();
            int column2 = cell2.getColumn();
            int i2 = -1;
            if (Math.abs(row) == 2 && Math.abs(column) != 1) {
                row2 = cell2.getRow() + (row > 0 ? 1 : -1);
            }
            if (Math.abs(column) == 2 && Math.abs(row) != 1) {
                int column3 = cell2.getColumn();
                if (column > 0) {
                    i2 = 1;
                }
                column2 = column3 + i2;
            }
            cell = LockPatternView.Cell.of(row2, column2);
        }
        if (cell != null && !this.mPatternDrawLookup[cell.getRow()][cell.getColumn()]) {
            addCellToPattern(cell);
        }
        addCellToPattern(checkForNewHit);
        if (this.mEnableHapticFeedback) {
            if (MiuiKeyguardUtils.SUPPORT_LINEAR_MOTOR_VIBRATE) {
                i = 268435461;
            }
            performHapticFeedback(i, 3);
        }
        return checkForNewHit;
    }

    private void addCellToPattern(LockPatternView.Cell cell) {
        this.mPatternDrawLookup[cell.getRow()][cell.getColumn()] = true;
        this.mPattern.add(cell);
        if (!this.mInStealthMode) {
            startCellActivatedAnimation(cell);
        }
        notifyCellAdded();
    }

    private void startCellActivatedAnimation(LockPatternView.Cell cell) {
        final CellState cellState = this.mCellStates[cell.getRow()][cell.getColumn()];
        startRadiusAnimation((float) (this.mDotSize / 2), (float) (this.mDotSizeActivated / 2), 96, this.mLinearOutSlowInInterpolator, cellState, new Runnable() {
            /* class com.android.keyguard.MiuiLockPatternView.AnonymousClass4 */

            public void run() {
                MiuiLockPatternView miuiLockPatternView = MiuiLockPatternView.this;
                miuiLockPatternView.startRadiusAnimation((float) (miuiLockPatternView.mDotSizeActivated / 2), (float) (MiuiLockPatternView.this.mDotSize / 2), 192, MiuiLockPatternView.this.mFastOutSlowInInterpolator, cellState, null);
            }
        });
        startLineEndAnimation(cellState, this.mInProgressX, this.mInProgressY, getCenterXForColumn(cell.getColumn()), getCenterYForRow(cell.getRow()));
    }

    private void startLineEndAnimation(final CellState cellState, final float f, final float f2, final float f3, final float f4) {
        ValueAnimator ofFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.android.keyguard.MiuiLockPatternView.AnonymousClass5 */

            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float floatValue = Float.valueOf(valueAnimator.getAnimatedValue().toString()).floatValue();
                CellState cellState = cellState;
                float f = 1.0f - floatValue;
                cellState.lineEndX = (f * f) + (f3 * floatValue);
                cellState.lineEndY = (f * f2) + (floatValue * f4);
                MiuiLockPatternView.this.invalidate();
            }
        });
        ofFloat.addListener(new AnimatorListenerAdapter(this) {
            /* class com.android.keyguard.MiuiLockPatternView.AnonymousClass6 */

            public void onAnimationEnd(Animator animator) {
                cellState.lineAnimator = null;
            }
        });
        ofFloat.setInterpolator(this.mFastOutSlowInInterpolator);
        ofFloat.setDuration(100L);
        ofFloat.start();
        cellState.lineAnimator = ofFloat;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startRadiusAnimation(float f, float f2, long j, Interpolator interpolator, final CellState cellState, final Runnable runnable) {
        ValueAnimator ofFloat = ValueAnimator.ofFloat(f, f2);
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.android.keyguard.MiuiLockPatternView.AnonymousClass7 */

            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                cellState.radius = Float.valueOf(valueAnimator.getAnimatedValue().toString()).floatValue();
                MiuiLockPatternView.this.invalidate();
            }
        });
        if (runnable != null) {
            ofFloat.addListener(new AnimatorListenerAdapter(this) {
                /* class com.android.keyguard.MiuiLockPatternView.AnonymousClass8 */

                public void onAnimationEnd(Animator animator) {
                    runnable.run();
                }
            });
        }
        ofFloat.setInterpolator(interpolator);
        ofFloat.setDuration(j);
        ofFloat.start();
    }

    private LockPatternView.Cell checkForNewHit(float f, float f2) {
        int columnHit;
        int rowHit = getRowHit(f2);
        if (rowHit >= 0 && (columnHit = getColumnHit(f)) >= 0 && !this.mPatternDrawLookup[rowHit][columnHit]) {
            return LockPatternView.Cell.of(rowHit, columnHit);
        }
        return null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getRowHit(float f) {
        float f2 = this.mSquareHeight;
        float f3 = this.mHitFactor * f2;
        float f4 = ((float) ((View) this).mPaddingTop) + ((f2 - f3) / 2.0f);
        for (int i = 0; i < 3; i++) {
            float f5 = (((float) i) * f2) + f4;
            if (f >= f5 && f <= f5 + f3) {
                return i;
            }
        }
        return -1;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getColumnHit(float f) {
        float f2 = this.mSquareWidth;
        float f3 = this.mHitFactor * f2;
        float f4 = ((float) ((View) this).mPaddingLeft) + ((f2 - f3) / 2.0f);
        for (int i = 0; i < 3; i++) {
            float f5 = (((float) i) * f2) + f4;
            if (f >= f5 && f <= f5 + f3) {
                return i;
            }
        }
        return -1;
    }

    public boolean onHoverEvent(MotionEvent motionEvent) {
        if (AccessibilityManager.getInstance(((View) this).mContext).isTouchExplorationEnabled()) {
            int action = motionEvent.getAction();
            if (action == 7) {
                motionEvent.setAction(2);
            } else if (action == 9) {
                motionEvent.setAction(0);
            } else if (action == 10) {
                motionEvent.setAction(1);
            }
            onTouchEvent(motionEvent);
            motionEvent.setAction(action);
        }
        return super.onHoverEvent(motionEvent);
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (!this.mInputEnabled || !isEnabled()) {
            return false;
        }
        int action = motionEvent.getAction();
        if (action == 0) {
            handleActionDown(motionEvent);
            return true;
        } else if (action == 1) {
            handleActionUp();
            return true;
        } else if (action == 2) {
            handleActionMove(motionEvent);
            return true;
        } else if (action != 3) {
            return false;
        } else {
            if (this.mPatternInProgress) {
                setPatternInProgress(false);
                resetPattern();
                notifyPatternCleared();
            }
            return true;
        }
    }

    private void setPatternInProgress(boolean z) {
        this.mPatternInProgress = z;
        this.mExploreByTouchHelper.invalidateRoot();
    }

    private void handleActionMove(MotionEvent motionEvent) {
        float f = (float) this.mPathWidth;
        int historySize = motionEvent.getHistorySize();
        this.mTmpInvalidateRect.setEmpty();
        int i = 0;
        boolean z = false;
        while (i < historySize + 1) {
            float historicalX = i < historySize ? motionEvent.getHistoricalX(i) : motionEvent.getX();
            float historicalY = i < historySize ? motionEvent.getHistoricalY(i) : motionEvent.getY();
            LockPatternView.Cell detectAndAddHit = detectAndAddHit(historicalX, historicalY);
            int size = this.mPattern.size();
            if (detectAndAddHit != null && size == 1) {
                setPatternInProgress(true);
                notifyPatternStarted();
            }
            float abs = Math.abs(historicalX - this.mInProgressX);
            float abs2 = Math.abs(historicalY - this.mInProgressY);
            if (abs > 0.0f || abs2 > 0.0f) {
                z = true;
            }
            if (this.mPatternInProgress && size > 0) {
                LockPatternView.Cell cell = this.mPattern.get(size - 1);
                float centerXForColumn = getCenterXForColumn(cell.getColumn());
                float centerYForRow = getCenterYForRow(cell.getRow());
                float min = Math.min(centerXForColumn, historicalX) - f;
                float max = Math.max(centerXForColumn, historicalX) + f;
                float min2 = Math.min(centerYForRow, historicalY) - f;
                float max2 = Math.max(centerYForRow, historicalY) + f;
                if (detectAndAddHit != null) {
                    float f2 = this.mSquareWidth * 0.5f;
                    float f3 = this.mSquareHeight * 0.5f;
                    float centerXForColumn2 = getCenterXForColumn(detectAndAddHit.getColumn());
                    float centerYForRow2 = getCenterYForRow(detectAndAddHit.getRow());
                    min = Math.min(centerXForColumn2 - f2, min);
                    max = Math.max(centerXForColumn2 + f2, max);
                    min2 = Math.min(centerYForRow2 - f3, min2);
                    max2 = Math.max(centerYForRow2 + f3, max2);
                }
                this.mTmpInvalidateRect.union(Math.round(min), Math.round(min2), Math.round(max), Math.round(max2));
            }
            i++;
        }
        this.mInProgressX = motionEvent.getX();
        this.mInProgressY = motionEvent.getY();
        if (z) {
            this.mInvalidate.union(this.mTmpInvalidateRect);
            invalidate(this.mInvalidate);
            this.mInvalidate.set(this.mTmpInvalidateRect);
        }
    }

    private void sendAccessEvent(int i) {
        announceForAccessibility(((View) this).mContext.getString(i));
    }

    private void handleActionUp() {
        if (!this.mPattern.isEmpty()) {
            setPatternInProgress(false);
            cancelLineAnimations();
            notifyPatternDetected();
            invalidate();
        }
    }

    private void cancelLineAnimations() {
        for (int i = 0; i < 3; i++) {
            for (int i2 = 0; i2 < 3; i2++) {
                CellState cellState = this.mCellStates[i][i2];
                ValueAnimator valueAnimator = cellState.lineAnimator;
                if (valueAnimator != null) {
                    valueAnimator.cancel();
                    cellState.lineEndX = Float.MIN_VALUE;
                    cellState.lineEndY = Float.MIN_VALUE;
                }
            }
        }
    }

    private void handleActionDown(MotionEvent motionEvent) {
        resetPattern();
        float x = motionEvent.getX();
        float y = motionEvent.getY();
        LockPatternView.Cell detectAndAddHit = detectAndAddHit(x, y);
        if (detectAndAddHit != null) {
            setPatternInProgress(true);
            this.mPatternDisplayMode = DisplayMode.CORRECT;
            notifyPatternStarted();
        } else if (this.mPatternInProgress) {
            setPatternInProgress(false);
            notifyPatternCleared();
        }
        if (detectAndAddHit != null) {
            float centerXForColumn = getCenterXForColumn(detectAndAddHit.getColumn());
            float centerYForRow = getCenterYForRow(detectAndAddHit.getRow());
            float f = this.mSquareWidth / 2.0f;
            float f2 = this.mSquareHeight / 2.0f;
            invalidate((int) (centerXForColumn - f), (int) (centerYForRow - f2), (int) (centerXForColumn + f), (int) (centerYForRow + f2));
        }
        this.mInProgressX = x;
        this.mInProgressY = y;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private float getCenterXForColumn(int i) {
        float f = this.mSquareWidth;
        return ((float) ((View) this).mPaddingLeft) + (((float) i) * f) + (f / 2.0f);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private float getCenterYForRow(int i) {
        float f = this.mSquareHeight;
        return ((float) ((View) this).mPaddingTop) + (((float) i) * f) + (f / 2.0f);
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        int i;
        int i2;
        float f;
        ArrayList<LockPatternView.Cell> arrayList = this.mPattern;
        int size = arrayList.size();
        boolean[][] zArr = this.mPatternDrawLookup;
        if (this.mPatternDisplayMode == DisplayMode.ANIMATE) {
            int elapsedRealtime = ((int) (SystemClock.elapsedRealtime() - this.mAnimatingPeriodStart)) % ((size + 1) * 700);
            int i3 = elapsedRealtime / 700;
            clearPatternDrawLookup();
            for (int i4 = 0; i4 < i3; i4++) {
                LockPatternView.Cell cell = arrayList.get(i4);
                zArr[cell.getRow()][cell.getColumn()] = true;
            }
            if (i3 > 0 && i3 < size) {
                float f2 = ((float) (elapsedRealtime % 700)) / 700.0f;
                LockPatternView.Cell cell2 = arrayList.get(i3 - 1);
                float centerXForColumn = getCenterXForColumn(cell2.getColumn());
                float centerYForRow = getCenterYForRow(cell2.getRow());
                LockPatternView.Cell cell3 = arrayList.get(i3);
                this.mInProgressX = centerXForColumn + ((getCenterXForColumn(cell3.getColumn()) - centerXForColumn) * f2);
                this.mInProgressY = centerYForRow + (f2 * (getCenterYForRow(cell3.getRow()) - centerYForRow));
            }
            invalidate();
        }
        Path path = this.mCurrentPath;
        path.rewind();
        int i5 = 0;
        while (true) {
            int i6 = 3;
            if (i5 >= 3) {
                break;
            }
            float centerYForRow2 = getCenterYForRow(i5);
            int i7 = 0;
            while (i7 < i6) {
                CellState cellState = this.mCellStates[i5][i7];
                float centerXForColumn2 = getCenterXForColumn(i7);
                float f3 = cellState.translationY;
                if (this.mUseLockPatternDrawable) {
                    i = i7;
                    f = centerYForRow2;
                    drawCellDrawable(canvas, i5, i7, cellState.radius, zArr[i5][i7]);
                } else {
                    i = i7;
                    f = centerYForRow2;
                    if (!isHardwareAccelerated() || !cellState.hwAnimating) {
                        i2 = i6;
                        drawCircle(canvas, (float) ((int) centerXForColumn2), ((float) ((int) f)) + f3, cellState.radius, zArr[i5][i], cellState.alpha);
                        i7 = i + 1;
                        centerYForRow2 = f;
                        i6 = i2;
                    } else {
                        ((DisplayListCanvas) canvas).drawCircle(cellState.hwCenterX, cellState.hwCenterY, cellState.hwRadius, cellState.hwPaint);
                    }
                }
                i2 = i6;
                i7 = i + 1;
                centerYForRow2 = f;
                i6 = i2;
            }
            i5++;
        }
        if (!this.mInStealthMode) {
            this.mPathPaint.setColor(getCurrentColor(true));
            float f4 = 0.0f;
            float f5 = 0.0f;
            int i8 = 0;
            boolean z = false;
            while (i8 < size) {
                LockPatternView.Cell cell4 = arrayList.get(i8);
                if (!zArr[cell4.getRow()][cell4.getColumn()]) {
                    break;
                }
                float centerXForColumn3 = getCenterXForColumn(cell4.getColumn());
                float centerYForRow3 = getCenterYForRow(cell4.getRow());
                if (i8 != 0) {
                    CellState cellState2 = this.mCellStates[cell4.getRow()][cell4.getColumn()];
                    path.rewind();
                    path.moveTo(f4, f5);
                    float f6 = cellState2.lineEndX;
                    if (f6 != Float.MIN_VALUE) {
                        float f7 = cellState2.lineEndY;
                        if (f7 != Float.MIN_VALUE) {
                            path.lineTo(f6, f7);
                            canvas.drawPath(path, this.mPathPaint);
                        }
                    }
                    path.lineTo(centerXForColumn3, centerYForRow3);
                    canvas.drawPath(path, this.mPathPaint);
                }
                i8++;
                f4 = centerXForColumn3;
                f5 = centerYForRow3;
                z = true;
            }
            if ((this.mPatternInProgress || this.mPatternDisplayMode == DisplayMode.ANIMATE) && z) {
                path.rewind();
                path.moveTo(f4, f5);
                path.lineTo(this.mInProgressX, this.mInProgressY);
                this.mPathPaint.setAlpha((int) (calculateLastSegmentAlpha(this.mInProgressX, this.mInProgressY, f4, f5) * 255.0f));
                canvas.drawPath(path, this.mPathPaint);
            }
        }
    }

    private float calculateLastSegmentAlpha(float f, float f2, float f3, float f4) {
        float f5 = f - f3;
        float f6 = f2 - f4;
        return Math.min(1.0f, Math.max(0.0f, ((((float) Math.sqrt((double) ((f5 * f5) + (f6 * f6)))) / this.mSquareWidth) - 0.3f) * 4.0f));
    }

    private int getCurrentColor(boolean z) {
        if (!z || this.mInStealthMode || this.mPatternInProgress) {
            return this.mRegularColor;
        }
        DisplayMode displayMode = this.mPatternDisplayMode;
        if (displayMode == DisplayMode.WRONG) {
            return this.mErrorColor;
        }
        if (displayMode == DisplayMode.CORRECT || displayMode == DisplayMode.ANIMATE) {
            return this.mSuccessColor;
        }
        throw new IllegalStateException("unknown display mode " + this.mPatternDisplayMode);
    }

    private void drawCircle(Canvas canvas, float f, float f2, float f3, boolean z, float f4) {
        this.mPaint.setColor(getCurrentColor(z));
        this.mPaint.setAlpha((int) (f4 * 255.0f));
        canvas.drawCircle(f, f2, f3, this.mPaint);
    }

    private void drawCellDrawable(Canvas canvas, int i, int i2, float f, boolean z) {
        int i3 = ((View) this).mPaddingLeft;
        float f2 = this.mSquareWidth;
        int i4 = ((View) this).mPaddingTop;
        float f3 = this.mSquareHeight;
        Rect rect = new Rect((int) (((float) i3) + (((float) i2) * f2)), (int) (((float) i4) + (((float) i) * f3)), (int) (((float) i3) + (((float) (i2 + 1)) * f2)), (int) (((float) i4) + (((float) (i + 1)) * f3)));
        float f4 = f / ((float) (this.mDotSize / 2));
        canvas.save();
        canvas.clipRect(rect);
        canvas.scale(f4, f4, (float) rect.centerX(), (float) rect.centerY());
        if (!z || f4 > 1.0f) {
            this.mNotSelectedDrawable.draw(canvas);
        } else {
            this.mSelectedDrawable.draw(canvas);
        }
        canvas.restore();
    }

    /* access modifiers changed from: protected */
    public Parcelable onSaveInstanceState() {
        return new SavedState(super.onSaveInstanceState(), null, this.mPatternDisplayMode.ordinal(), this.mInputEnabled, this.mInStealthMode, this.mEnableHapticFeedback);
    }

    /* access modifiers changed from: protected */
    public void onRestoreInstanceState(Parcelable parcelable) {
        SavedState savedState = (SavedState) parcelable;
        super.onRestoreInstanceState(savedState.getSuperState());
        this.mPatternDisplayMode = DisplayMode.values()[savedState.getDisplayMode()];
        this.mInputEnabled = savedState.isInputEnabled();
        this.mInStealthMode = savedState.isInStealthMode();
        this.mEnableHapticFeedback = savedState.isTactileFeedbackEnabled();
    }

    /* access modifiers changed from: private */
    public static class SavedState extends View.BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            /* class com.android.keyguard.MiuiLockPatternView.SavedState.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public SavedState createFromParcel(Parcel parcel) {
                return new SavedState(parcel);
            }

            @Override // android.os.Parcelable.Creator
            public SavedState[] newArray(int i) {
                return new SavedState[i];
            }
        };
        private final int mDisplayMode;
        private final boolean mInStealthMode;
        private final boolean mInputEnabled;
        private final String mSerializedPattern;
        private final boolean mTactileFeedbackEnabled;

        private SavedState(Parcelable parcelable, String str, int i, boolean z, boolean z2, boolean z3) {
            super(parcelable);
            this.mSerializedPattern = str;
            this.mDisplayMode = i;
            this.mInputEnabled = z;
            this.mInStealthMode = z2;
            this.mTactileFeedbackEnabled = z3;
        }

        private SavedState(Parcel parcel) {
            super(parcel);
            this.mSerializedPattern = parcel.readString();
            this.mDisplayMode = parcel.readInt();
            this.mInputEnabled = ((Boolean) parcel.readValue(null)).booleanValue();
            this.mInStealthMode = ((Boolean) parcel.readValue(null)).booleanValue();
            this.mTactileFeedbackEnabled = ((Boolean) parcel.readValue(null)).booleanValue();
        }

        public int getDisplayMode() {
            return this.mDisplayMode;
        }

        public boolean isInputEnabled() {
            return this.mInputEnabled;
        }

        public boolean isInStealthMode() {
            return this.mInStealthMode;
        }

        public boolean isTactileFeedbackEnabled() {
            return this.mTactileFeedbackEnabled;
        }

        public void writeToParcel(Parcel parcel, int i) {
            super.writeToParcel(parcel, i);
            parcel.writeString(this.mSerializedPattern);
            parcel.writeInt(this.mDisplayMode);
            parcel.writeValue(Boolean.valueOf(this.mInputEnabled));
            parcel.writeValue(Boolean.valueOf(this.mInStealthMode));
            parcel.writeValue(Boolean.valueOf(this.mTactileFeedbackEnabled));
        }
    }

    /* access modifiers changed from: private */
    public final class PatternExploreByTouchHelper extends ExploreByTouchHelper {
        private HashMap<Integer, VirtualViewContainer> mItems = new HashMap<>();
        private Rect mTempRect = new Rect();

        class VirtualViewContainer {
            CharSequence description;

            public VirtualViewContainer(PatternExploreByTouchHelper patternExploreByTouchHelper, CharSequence charSequence) {
                this.description = charSequence;
            }
        }

        public PatternExploreByTouchHelper(View view) {
            super(view);
        }

        /* access modifiers changed from: protected */
        public int getVirtualViewAt(float f, float f2) {
            return getVirtualViewIdForHit(f, f2);
        }

        /* access modifiers changed from: protected */
        public void getVisibleVirtualViews(IntArray intArray) {
            if (MiuiLockPatternView.this.mPatternInProgress) {
                for (int i = 1; i < 10; i++) {
                    if (!this.mItems.containsKey(Integer.valueOf(i))) {
                        this.mItems.put(Integer.valueOf(i), new VirtualViewContainer(this, getTextForVirtualView(i)));
                    }
                    intArray.add(i);
                }
            }
        }

        /* access modifiers changed from: protected */
        public void onPopulateEventForVirtualView(int i, AccessibilityEvent accessibilityEvent) {
            if (this.mItems.containsKey(Integer.valueOf(i))) {
                CharSequence charSequence = this.mItems.get(Integer.valueOf(i)).description;
                accessibilityEvent.getText().add(charSequence);
                accessibilityEvent.setContentDescription(charSequence);
                return;
            }
            accessibilityEvent.setContentDescription(((View) MiuiLockPatternView.this).mContext.getResources().getString(C0021R$string.input_pattern_hint_text));
        }

        public void onPopulateAccessibilityEvent(View view, AccessibilityEvent accessibilityEvent) {
            MiuiLockPatternView.super.onPopulateAccessibilityEvent(view, accessibilityEvent);
            if (!MiuiLockPatternView.this.mPatternInProgress) {
                accessibilityEvent.setContentDescription(MiuiLockPatternView.this.getContext().getText(17040504));
            }
        }

        /* access modifiers changed from: protected */
        public void onPopulateNodeForVirtualView(int i, AccessibilityNodeInfo accessibilityNodeInfo) {
            accessibilityNodeInfo.setText(getTextForVirtualView(i));
            accessibilityNodeInfo.setContentDescription(getTextForVirtualView(i));
            if (MiuiLockPatternView.this.mPatternInProgress) {
                accessibilityNodeInfo.setFocusable(true);
                if (isClickable(i)) {
                    accessibilityNodeInfo.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_CLICK);
                    accessibilityNodeInfo.setClickable(isClickable(i));
                }
            }
            accessibilityNodeInfo.setBoundsInParent(getBoundsForVirtualView(i));
        }

        private boolean isClickable(int i) {
            if (i == Integer.MIN_VALUE) {
                return false;
            }
            int i2 = i - 1;
            return !MiuiLockPatternView.this.mPatternDrawLookup[i2 / 3][i2 % 3];
        }

        /* access modifiers changed from: protected */
        public boolean onPerformActionForVirtualView(int i, int i2, Bundle bundle) {
            if (i2 != 16) {
                return false;
            }
            return onItemClicked(i);
        }

        /* access modifiers changed from: package-private */
        public boolean onItemClicked(int i) {
            invalidateVirtualView(i);
            sendEventForVirtualView(i, 1);
            return true;
        }

        private Rect getBoundsForVirtualView(int i) {
            int i2 = i - 1;
            Rect rect = this.mTempRect;
            int i3 = i2 / 3;
            int i4 = i2 % 3;
            CellState cellState = MiuiLockPatternView.this.mCellStates[i3][i4];
            float centerXForColumn = MiuiLockPatternView.this.getCenterXForColumn(i4);
            float centerYForRow = MiuiLockPatternView.this.getCenterYForRow(i3);
            float f = MiuiLockPatternView.this.mSquareHeight * MiuiLockPatternView.this.mHitFactor * 0.5f;
            float f2 = MiuiLockPatternView.this.mSquareWidth * MiuiLockPatternView.this.mHitFactor * 0.5f;
            rect.left = (int) (centerXForColumn - f2);
            rect.right = (int) (centerXForColumn + f2);
            rect.top = (int) (centerYForRow - f);
            rect.bottom = (int) (centerYForRow + f);
            return rect;
        }

        private CharSequence getTextForVirtualView(int i) {
            return MiuiLockPatternView.this.getResources().getString(C0021R$string.lockscreen_access_pattern_cell_added_verbose, Integer.valueOf(i));
        }

        private int getVirtualViewIdForHit(float f, float f2) {
            int columnHit;
            int rowHit = MiuiLockPatternView.this.getRowHit(f2);
            if (rowHit < 0 || (columnHit = MiuiLockPatternView.this.getColumnHit(f)) < 0) {
                return Integer.MIN_VALUE;
            }
            boolean z = MiuiLockPatternView.this.mPatternDrawLookup[rowHit][columnHit];
            int i = (rowHit * 3) + columnHit + 1;
            if (z) {
                return i;
            }
            return Integer.MIN_VALUE;
        }
    }
}
