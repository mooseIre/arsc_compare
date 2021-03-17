package com.android.systemui.classifier.brightline;

import android.graphics.Point;
import android.view.MotionEvent;
import com.android.systemui.util.DeviceConfigProxy;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/* access modifiers changed from: package-private */
public class ZigZagClassifier extends FalsingClassifier {
    private float mLastDevianceX;
    private float mLastDevianceY;
    private float mLastMaxXDeviance;
    private float mLastMaxYDeviance;
    private final float mMaxXPrimaryDeviance;
    private final float mMaxXSecondaryDeviance;
    private final float mMaxYPrimaryDeviance;
    private final float mMaxYSecondaryDeviance;

    ZigZagClassifier(FalsingDataProvider falsingDataProvider, DeviceConfigProxy deviceConfigProxy) {
        super(falsingDataProvider);
        this.mMaxXPrimaryDeviance = deviceConfigProxy.getFloat("systemui", "brightline_falsing_zigzag_x_primary_deviance", 0.05f);
        this.mMaxYPrimaryDeviance = deviceConfigProxy.getFloat("systemui", "brightline_falsing_zigzag_y_primary_deviance", 0.15f);
        this.mMaxXSecondaryDeviance = deviceConfigProxy.getFloat("systemui", "brightline_falsing_zigzag_x_secondary_deviance", 0.4f);
        this.mMaxYSecondaryDeviance = deviceConfigProxy.getFloat("systemui", "brightline_falsing_zigzag_y_secondary_deviance", 0.3f);
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.systemui.classifier.brightline.FalsingClassifier
    public boolean isFalseTouch() {
        List<Point> list;
        float f;
        float f2;
        float f3;
        if (getRecentMotionEvents().size() < 3) {
            return false;
        }
        if (isHorizontal()) {
            list = rotateHorizontal();
        } else {
            list = rotateVertical();
        }
        float abs = (float) Math.abs(list.get(0).x - list.get(list.size() - 1).x);
        float abs2 = (float) Math.abs(list.get(0).y - list.get(list.size() - 1).y);
        FalsingClassifier.logDebug("Actual: (" + abs + "," + abs2 + ")");
        float f4 = 0.0f;
        boolean z = true;
        float f5 = 0.0f;
        float f6 = 0.0f;
        float f7 = 0.0f;
        for (Point point : list) {
            if (z) {
                f6 = (float) point.x;
                f7 = (float) point.y;
                z = false;
            } else {
                f4 += Math.abs(((float) point.x) - f6);
                f5 += Math.abs(((float) point.y) - f7);
                f6 = (float) point.x;
                f7 = (float) point.y;
                FalsingClassifier.logDebug("(x, y, runningAbsDx, runningAbsDy) - (" + f6 + ", " + f7 + ", " + f4 + ", " + f5 + ")");
            }
        }
        float f8 = f4 - abs;
        float f9 = f5 - abs2;
        float xdpi = abs / getXdpi();
        float ydpi = abs2 / getYdpi();
        float sqrt = (float) Math.sqrt((double) ((xdpi * xdpi) + (ydpi * ydpi)));
        if (abs > abs2) {
            f2 = this.mMaxXPrimaryDeviance * sqrt * getXdpi();
            f = this.mMaxYSecondaryDeviance * sqrt;
            f3 = getYdpi();
        } else {
            f2 = this.mMaxXSecondaryDeviance * sqrt * getXdpi();
            f = this.mMaxYPrimaryDeviance * sqrt;
            f3 = getYdpi();
        }
        float f10 = f * f3;
        this.mLastDevianceX = f8;
        this.mLastDevianceY = f9;
        this.mLastMaxXDeviance = f2;
        this.mLastMaxYDeviance = f10;
        FalsingClassifier.logDebug("Straightness Deviance: (" + f8 + "," + f9 + ") vs (" + f2 + "," + f10 + ")");
        if (f8 > f2 || f9 > f10) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.systemui.classifier.brightline.FalsingClassifier
    public String getReason() {
        return String.format(null, "{devianceX=%f, maxDevianceX=%s, devianceY=%s, maxDevianceY=%s}", Float.valueOf(this.mLastDevianceX), Float.valueOf(this.mLastMaxXDeviance), Float.valueOf(this.mLastDevianceY), Float.valueOf(this.mLastMaxYDeviance));
    }

    private float getAtan2LastPoint() {
        MotionEvent firstMotionEvent = getFirstMotionEvent();
        MotionEvent lastMotionEvent = getLastMotionEvent();
        float x = firstMotionEvent.getX();
        return (float) Math.atan2((double) (lastMotionEvent.getY() - firstMotionEvent.getY()), (double) (lastMotionEvent.getX() - x));
    }

    private List<Point> rotateVertical() {
        double atan2LastPoint = 1.5707963267948966d - ((double) getAtan2LastPoint());
        FalsingClassifier.logDebug("Rotating to vertical by: " + atan2LastPoint);
        return rotateMotionEvents(getRecentMotionEvents(), -atan2LastPoint);
    }

    private List<Point> rotateHorizontal() {
        double atan2LastPoint = (double) getAtan2LastPoint();
        FalsingClassifier.logDebug("Rotating to horizontal by: " + atan2LastPoint);
        return rotateMotionEvents(getRecentMotionEvents(), atan2LastPoint);
    }

    private List<Point> rotateMotionEvents(List<MotionEvent> list, double d) {
        ArrayList arrayList = new ArrayList();
        double cos = Math.cos(d);
        double sin = Math.sin(d);
        MotionEvent motionEvent = list.get(0);
        float x = motionEvent.getX();
        float y = motionEvent.getY();
        for (Iterator<MotionEvent> it = list.iterator(); it.hasNext(); it = it) {
            MotionEvent next = it.next();
            double x2 = (double) (next.getX() - x);
            double y2 = (double) (next.getY() - y);
            arrayList.add(new Point((int) ((cos * x2) + (sin * y2) + ((double) x)), (int) (((-sin) * x2) + (y2 * cos) + ((double) y))));
            motionEvent = motionEvent;
        }
        MotionEvent motionEvent2 = list.get(list.size() - 1);
        Point point = (Point) arrayList.get(0);
        Point point2 = (Point) arrayList.get(arrayList.size() - 1);
        FalsingClassifier.logDebug("Before: (" + motionEvent.getX() + "," + motionEvent.getY() + "), (" + motionEvent2.getX() + "," + motionEvent2.getY() + ")");
        FalsingClassifier.logDebug("After: (" + point.x + "," + point.y + "), (" + point2.x + "," + point2.y + ")");
        return arrayList;
    }
}
