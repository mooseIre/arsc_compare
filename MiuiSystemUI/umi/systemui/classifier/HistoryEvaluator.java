package com.android.systemui.classifier;

import android.os.SystemClock;
import java.util.ArrayList;

public class HistoryEvaluator {
    private final ArrayList<Data> mGestureWeights = new ArrayList<>();
    private long mLastUpdate = SystemClock.elapsedRealtime();
    private final ArrayList<Data> mStrokes = new ArrayList<>();

    private boolean isZero(float f) {
        return f <= 1.0E-5f && f >= -1.0E-5f;
    }

    public void addStroke(float f) {
        decayValue();
        this.mStrokes.add(new Data(f));
    }

    public void addGesture(float f) {
        decayValue();
        this.mGestureWeights.add(new Data(f));
    }

    public float getEvaluation() {
        return weightedAverage(this.mStrokes) + weightedAverage(this.mGestureWeights);
    }

    private float weightedAverage(ArrayList<Data> arrayList) {
        int size = arrayList.size();
        float f = 0.0f;
        float f2 = 0.0f;
        for (int i = 0; i < size; i++) {
            Data data = arrayList.get(i);
            float f3 = data.evaluation;
            float f4 = data.weight;
            f2 += f3 * f4;
            f += f4;
        }
        if (f == 0.0f) {
            return 0.0f;
        }
        return f2 / f;
    }

    private void decayValue() {
        long elapsedRealtime = SystemClock.elapsedRealtime();
        long j = this.mLastUpdate;
        if (elapsedRealtime > j) {
            float pow = (float) Math.pow(0.8999999761581421d, (double) (((float) (elapsedRealtime - j)) / 50.0f));
            decayValue(this.mStrokes, pow);
            decayValue(this.mGestureWeights, pow);
            this.mLastUpdate = elapsedRealtime;
        }
    }

    private void decayValue(ArrayList<Data> arrayList, float f) {
        int size = arrayList.size();
        for (int i = 0; i < size; i++) {
            arrayList.get(i).weight *= f;
        }
        while (!arrayList.isEmpty() && isZero(arrayList.get(0).weight)) {
            arrayList.remove(0);
        }
    }

    /* access modifiers changed from: private */
    public static class Data {
        public float evaluation;
        public float weight = 1.0f;

        public Data(float f) {
            this.evaluation = f;
        }
    }
}
