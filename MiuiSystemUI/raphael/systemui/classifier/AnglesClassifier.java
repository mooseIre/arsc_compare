package com.android.systemui.classifier;

import android.os.Build;
import android.os.SystemProperties;
import android.view.MotionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AnglesClassifier extends StrokeClassifier {
    private static String TAG = "ANG";
    public static final boolean VERBOSE = SystemProperties.getBoolean("debug.falsing_log.ang", Build.IS_DEBUGGABLE);
    private HashMap<Stroke, Data> mStrokeMap = new HashMap<>();

    public AnglesClassifier(ClassifierData classifierData) {
        this.mClassifierData = classifierData;
    }

    @Override // com.android.systemui.classifier.Classifier
    public String getTag() {
        return TAG;
    }

    @Override // com.android.systemui.classifier.Classifier
    public void onTouchEvent(MotionEvent motionEvent) {
        if (motionEvent.getActionMasked() == 0) {
            this.mStrokeMap.clear();
        }
        for (int i = 0; i < motionEvent.getPointerCount(); i++) {
            Stroke stroke = this.mClassifierData.getStroke(motionEvent.getPointerId(i));
            if (this.mStrokeMap.get(stroke) == null) {
                this.mStrokeMap.put(stroke, new Data());
            }
            this.mStrokeMap.get(stroke).addPoint(stroke.getPoints().get(stroke.getPoints().size() - 1));
        }
    }

    @Override // com.android.systemui.classifier.StrokeClassifier
    public float getFalseTouchEvaluation(int i, Stroke stroke) {
        Data data = this.mStrokeMap.get(stroke);
        return AnglesVarianceEvaluator.evaluate(data.getAnglesVariance(), i) + AnglesPercentageEvaluator.evaluate(data.getAnglesPercentage(), i);
    }

    private static class Data {
        private float mAnglesCount = 0.0f;
        private float mBiggestAngle = 0.0f;
        private float mCount = 1.0f;
        private float mFirstAngleVariance = 0.0f;
        private float mFirstLength = 0.0f;
        private List<Point> mLastThreePoints = new ArrayList();
        private float mLeftAngles = 0.0f;
        private float mLength = 0.0f;
        private float mPreviousAngle = 3.1415927f;
        private float mRightAngles = 0.0f;
        private float mSecondCount = 1.0f;
        private float mSecondSum = 0.0f;
        private float mSecondSumSquares = 0.0f;
        private float mStraightAngles = 0.0f;
        private float mSum = 0.0f;
        private float mSumSquares = 0.0f;

        public float getAnglesVariance(float f, float f2, float f3) {
            float f4 = f2 / f3;
            return (f / f3) - (f4 * f4);
        }

        public void addPoint(Point point) {
            if (!this.mLastThreePoints.isEmpty()) {
                List<Point> list = this.mLastThreePoints;
                if (list.get(list.size() - 1).equals(point)) {
                    return;
                }
            }
            if (!this.mLastThreePoints.isEmpty()) {
                float f = this.mLength;
                List<Point> list2 = this.mLastThreePoints;
                this.mLength = f + list2.get(list2.size() - 1).dist(point);
            }
            this.mLastThreePoints.add(point);
            if (this.mLastThreePoints.size() == 4) {
                this.mLastThreePoints.remove(0);
                float angle = this.mLastThreePoints.get(1).getAngle(this.mLastThreePoints.get(0), this.mLastThreePoints.get(2));
                this.mAnglesCount += 1.0f;
                double d = (double) angle;
                if (d < 2.9845130165391645d) {
                    this.mLeftAngles += 1.0f;
                } else if (d <= 3.298672290640422d) {
                    this.mStraightAngles += 1.0f;
                } else {
                    this.mRightAngles += 1.0f;
                }
                float f2 = angle - this.mPreviousAngle;
                if (this.mBiggestAngle < angle) {
                    this.mBiggestAngle = angle;
                    this.mFirstLength = this.mLength;
                    this.mFirstAngleVariance = getAnglesVariance(this.mSumSquares, this.mSum, this.mCount);
                    this.mSecondSumSquares = 0.0f;
                    this.mSecondSum = 0.0f;
                    this.mSecondCount = 1.0f;
                } else {
                    this.mSecondSum += f2;
                    this.mSecondSumSquares += f2 * f2;
                    this.mSecondCount = (float) (((double) this.mSecondCount) + 1.0d);
                }
                this.mSum += f2;
                this.mSumSquares += f2 * f2;
                this.mCount = (float) (((double) this.mCount) + 1.0d);
                this.mPreviousAngle = angle;
            }
        }

        public float getAnglesVariance() {
            float anglesVariance = getAnglesVariance(this.mSumSquares, this.mSum, this.mCount);
            if (AnglesClassifier.VERBOSE) {
                String str = AnglesClassifier.TAG;
                FalsingLog.i(str, "getAnglesVariance: (first pass) " + anglesVariance);
                String str2 = AnglesClassifier.TAG;
                FalsingLog.i(str2, "   - mFirstLength=" + this.mFirstLength);
                String str3 = AnglesClassifier.TAG;
                FalsingLog.i(str3, "   - mLength=" + this.mLength);
            }
            if (this.mFirstLength < this.mLength / 2.0f) {
                anglesVariance = Math.min(anglesVariance, this.mFirstAngleVariance + getAnglesVariance(this.mSecondSumSquares, this.mSecondSum, this.mSecondCount));
                if (AnglesClassifier.VERBOSE) {
                    String str4 = AnglesClassifier.TAG;
                    FalsingLog.i(str4, "getAnglesVariance: (second pass) " + anglesVariance);
                }
            }
            return anglesVariance;
        }

        public float getAnglesPercentage() {
            if (this.mAnglesCount != 0.0f) {
                float max = (Math.max(this.mLeftAngles, this.mRightAngles) + this.mStraightAngles) / this.mAnglesCount;
                if (AnglesClassifier.VERBOSE) {
                    String str = AnglesClassifier.TAG;
                    FalsingLog.i(str, "getAnglesPercentage: left=" + this.mLeftAngles + " right=" + this.mRightAngles + " straight=" + this.mStraightAngles + " count=" + this.mAnglesCount + " result=" + max);
                }
                return max;
            } else if (!AnglesClassifier.VERBOSE) {
                return 1.0f;
            } else {
                FalsingLog.i(AnglesClassifier.TAG, "getAnglesPercentage: count==0, result=1");
                return 1.0f;
            }
        }
    }
}
