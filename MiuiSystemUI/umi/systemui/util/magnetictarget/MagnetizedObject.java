package com.android.systemui.util.magnetictarget;

import android.content.Context;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import androidx.dynamicanimation.animation.FloatPropertyCompat;
import com.android.systemui.util.animation.PhysicsAnimator;
import java.util.ArrayList;
import java.util.Iterator;

public abstract class MagnetizedObject {
    PhysicsAnimator animator;
    ArrayList<MagneticTarget> associatedTargets = new ArrayList<>();
    boolean flingToTargetEnabled = true;
    float flingToTargetMinVelocity = 4000.0f;
    float flingToTargetWidthPercent = 3.0f;
    float flingUnstuckFromTargetMinVelocity = 1000.0f;
    PhysicsAnimator.SpringConfig flungIntoTargetSpringConfig;
    MagnetListener magnetListener;
    boolean movedBeyondSlop = false;
    int[] objectLocationOnScreen = new int[2];
    PhysicsAnimator.EndListener physicsAnimatorEndListener;
    PhysicsAnimator.UpdateListener physicsAnimatorUpdateListener;
    PhysicsAnimator.SpringConfig springConfig;
    float stickToTargetMaxVelocity = 2000.0f;
    /* access modifiers changed from: private */
    public MagneticTarget targetObjectIsStuckTo;
    PointF touchDown = new PointF();
    int touchSlop = 0;
    private Rect underlyingObject;
    VelocityTracker velocityTracker = VelocityTracker.obtain();
    private FloatPropertyCompat xProperty;
    private FloatPropertyCompat yProperty;

    public interface MagnetListener {
        void onReleasedInTarget(MagneticTarget magneticTarget);

        void onStuckToTarget(MagneticTarget magneticTarget);

        void onUnstuckFromTarget(MagneticTarget magneticTarget, float f, float f2, boolean z);
    }

    public abstract float getHeight(Rect rect);

    public abstract void getLocationOnScreen(Rect rect, int[] iArr);

    public abstract float getWidth(Rect rect);

    public MagnetizedObject(Context context, Rect rect, FloatPropertyCompat floatPropertyCompat, FloatPropertyCompat floatPropertyCompat2) {
        PhysicsAnimator.SpringConfig springConfig2 = new PhysicsAnimator.SpringConfig(1500.0f, 1.0f);
        this.springConfig = springConfig2;
        this.flungIntoTargetSpringConfig = springConfig2;
        this.underlyingObject = rect;
        this.xProperty = floatPropertyCompat;
        this.yProperty = floatPropertyCompat2;
        this.animator = PhysicsAnimator.getInstance(rect);
    }

    public class MagneticTarget {
        public PointF centerOnScreen = new PointF();
        public int magneticFieldRadiusPx;
        public View targetView;
        /* access modifiers changed from: private */
        public int[] tempLoc = new int[2];

        public MagneticTarget(MagnetizedObject magnetizedObject, View view, int i) {
            this.targetView = view;
            this.magneticFieldRadiusPx = i;
        }

        public void updateLocationOnScreen() {
            this.targetView.post(new Runnable() {
                public void run() {
                    MagneticTarget magneticTarget = MagneticTarget.this;
                    magneticTarget.targetView.getLocationOnScreen(magneticTarget.tempLoc);
                    MagneticTarget magneticTarget2 = MagneticTarget.this;
                    magneticTarget2.centerOnScreen.set((((float) magneticTarget2.tempLoc[0]) + (((float) MagneticTarget.this.targetView.getWidth()) / 2.0f)) - MagneticTarget.this.targetView.getTranslationX(), (((float) MagneticTarget.this.tempLoc[1]) + (((float) MagneticTarget.this.targetView.getHeight()) / 2.0f)) - MagneticTarget.this.targetView.getTranslationY());
                }
            });
        }

        public void setMagneticFieldRadiusPx(int i) {
            this.magneticFieldRadiusPx = i;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isObjectStuckToTarget() {
        return this.targetObjectIsStuckTo != null;
    }

    public void addTarget(MagneticTarget magneticTarget) {
        this.associatedTargets.add(magneticTarget);
        magneticTarget.updateLocationOnScreen();
    }

    public MagneticTarget addTarget(View view, int i) {
        MagneticTarget magneticTarget = new MagneticTarget(this, view, i);
        addTarget(magneticTarget);
        return magneticTarget;
    }

    public boolean maybeConsumeMotionEvent(MotionEvent motionEvent) {
        MagneticTarget magneticTarget;
        final MagneticTarget magneticTarget2;
        if (this.associatedTargets.size() == 0) {
            return false;
        }
        if (motionEvent.getAction() == 0) {
            updateTargetViews();
            this.velocityTracker.clear();
            this.targetObjectIsStuckTo = null;
            this.touchDown.set(motionEvent.getRawX(), motionEvent.getRawY());
            this.movedBeyondSlop = false;
        }
        addMovement(motionEvent);
        if (!this.movedBeyondSlop) {
            if (((float) Math.hypot((double) (motionEvent.getRawX() - this.touchDown.x), (double) (motionEvent.getRawY() - this.touchDown.y))) <= ((float) this.touchSlop)) {
                return false;
            }
            this.movedBeyondSlop = true;
        }
        Iterator<MagneticTarget> it = this.associatedTargets.iterator();
        while (true) {
            if (!it.hasNext()) {
                magneticTarget = null;
                break;
            }
            MagneticTarget next = it.next();
            if (((float) Math.hypot((double) (motionEvent.getRawX() - next.centerOnScreen.x), (double) (motionEvent.getRawY() - next.centerOnScreen.y))) < ((float) next.magneticFieldRadiusPx)) {
                magneticTarget = next;
                break;
            }
        }
        boolean z = !isObjectStuckToTarget() && magneticTarget != null;
        boolean z2 = (!isObjectStuckToTarget() || magneticTarget == null || this.targetObjectIsStuckTo == magneticTarget) ? false : true;
        Log.e("mag_test", "objectNewlyStuckToTarget=" + z + "   isObjectStuckToTarget=" + isObjectStuckToTarget() + "   targetObjectIsInMagneticFieldOf=" + magneticTarget + "   objectMovedIntoDifferentTarget=" + z2);
        if (z || z2) {
            this.velocityTracker.computeCurrentVelocity(1000);
            float xVelocity = this.velocityTracker.getXVelocity();
            float yVelocity = this.velocityTracker.getYVelocity();
            if (z && Math.hypot((double) xVelocity, (double) yVelocity) > ((double) this.stickToTargetMaxVelocity)) {
                return false;
            }
            this.targetObjectIsStuckTo = magneticTarget;
            cancelAnimations();
            this.magnetListener.onStuckToTarget(magneticTarget);
            animateStuckToTarget(magneticTarget, xVelocity, yVelocity, false, (Runnable) null);
        } else if (magneticTarget == null && isObjectStuckToTarget()) {
            this.velocityTracker.computeCurrentVelocity(1000);
            cancelAnimations();
            this.magnetListener.onUnstuckFromTarget(this.targetObjectIsStuckTo, this.velocityTracker.getXVelocity(), this.velocityTracker.getYVelocity(), false);
            this.targetObjectIsStuckTo = null;
        }
        if (motionEvent.getAction() != 1) {
            return isObjectStuckToTarget();
        }
        this.velocityTracker.computeCurrentVelocity(1000);
        float xVelocity2 = this.velocityTracker.getXVelocity();
        float yVelocity2 = this.velocityTracker.getYVelocity();
        cancelAnimations();
        if (isObjectStuckToTarget()) {
            if (Math.hypot((double) xVelocity2, (double) yVelocity2) > ((double) this.flingUnstuckFromTargetMinVelocity)) {
                this.magnetListener.onUnstuckFromTarget(this.targetObjectIsStuckTo, xVelocity2, yVelocity2, true);
            } else {
                this.magnetListener.onReleasedInTarget(this.targetObjectIsStuckTo);
            }
            this.targetObjectIsStuckTo = null;
            return true;
        }
        Iterator<MagneticTarget> it2 = this.associatedTargets.iterator();
        while (true) {
            if (!it2.hasNext()) {
                magneticTarget2 = null;
                break;
            }
            MagneticTarget next2 = it2.next();
            if (isForcefulFlingTowardsTarget(next2, motionEvent.getRawX(), motionEvent.getRawY(), xVelocity2, yVelocity2)) {
                magneticTarget2 = next2;
                break;
            }
        }
        if (magneticTarget2 == null) {
            return false;
        }
        this.magnetListener.onStuckToTarget(magneticTarget2);
        this.targetObjectIsStuckTo = magneticTarget2;
        animateStuckToTarget(magneticTarget2, xVelocity2, yVelocity2, true, new Runnable() {
            public void run() {
                MagneticTarget unused = MagnetizedObject.this.targetObjectIsStuckTo = null;
                MagnetizedObject.this.magnetListener.onReleasedInTarget(magneticTarget2);
            }
        });
        return true;
    }

    private void addMovement(MotionEvent motionEvent) {
        float rawX = motionEvent.getRawX() - motionEvent.getX();
        float rawY = motionEvent.getRawY() - motionEvent.getY();
        motionEvent.offsetLocation(rawX, rawY);
        this.velocityTracker.addMovement(motionEvent);
        motionEvent.offsetLocation(-rawX, -rawY);
    }

    private void animateStuckToTarget(MagneticTarget magneticTarget, float f, float f2, boolean z, Runnable runnable) {
        PhysicsAnimator.SpringConfig springConfig2;
        magneticTarget.updateLocationOnScreen();
        getLocationOnScreen(this.underlyingObject, this.objectLocationOnScreen);
        float width = (magneticTarget.centerOnScreen.x - (getWidth(this.underlyingObject) / 2.0f)) - ((float) this.objectLocationOnScreen[0]);
        float height = (magneticTarget.centerOnScreen.y - (getHeight(this.underlyingObject) / 2.0f)) - ((float) this.objectLocationOnScreen[1]);
        if (z) {
            springConfig2 = this.flungIntoTargetSpringConfig;
        } else {
            springConfig2 = this.springConfig;
        }
        cancelAnimations();
        PhysicsAnimator physicsAnimator = this.animator;
        FloatPropertyCompat floatPropertyCompat = this.xProperty;
        physicsAnimator.spring(floatPropertyCompat, floatPropertyCompat.getValue(this.underlyingObject) + width, f, springConfig2);
        FloatPropertyCompat floatPropertyCompat2 = this.yProperty;
        physicsAnimator.spring(floatPropertyCompat2, floatPropertyCompat2.getValue(this.underlyingObject) + height, f2, springConfig2);
        PhysicsAnimator.UpdateListener updateListener = this.physicsAnimatorUpdateListener;
        if (updateListener != null) {
            this.animator.addUpdateListener(updateListener);
        }
        PhysicsAnimator.EndListener endListener = this.physicsAnimatorEndListener;
        if (endListener != null) {
            this.animator.addEndListener(endListener);
        }
        if (runnable != null) {
            this.animator.withEndActions(runnable);
        }
        this.animator.start();
    }

    private boolean isForcefulFlingTowardsTarget(MagneticTarget magneticTarget, float f, float f2, float f3, float f4) {
        if (!this.flingToTargetEnabled) {
            return false;
        }
        if (!(f2 >= magneticTarget.centerOnScreen.y ? f4 < this.flingToTargetMinVelocity : f4 > this.flingToTargetMinVelocity)) {
            return false;
        }
        if (f3 != 0.0f) {
            float f5 = f4 / f3;
            f = (magneticTarget.centerOnScreen.y - (f2 - (f * f5))) / f5;
        }
        float width = ((float) magneticTarget.targetView.getWidth()) * this.flingToTargetWidthPercent;
        float f6 = magneticTarget.centerOnScreen.x;
        float f7 = width / 2.0f;
        if (f <= f6 - f7 || f >= f6 + f7) {
            return false;
        }
        return true;
    }

    private void cancelAnimations() {
        this.animator.cancel(this.xProperty);
        this.animator.cancel(this.yProperty);
    }

    private void updateTargetViews() {
        Iterator<MagneticTarget> it = this.associatedTargets.iterator();
        while (it.hasNext()) {
            it.next().updateLocationOnScreen();
        }
        if (this.associatedTargets.size() > 0) {
            this.touchSlop = ViewConfiguration.get(this.associatedTargets.get(0).targetView.getContext()).getScaledTouchSlop();
        }
    }

    public void setPhysicsAnimatorUpdateListener(PhysicsAnimator.UpdateListener updateListener) {
        this.physicsAnimatorUpdateListener = updateListener;
    }

    public void setMagnetListener(MagnetListener magnetListener2) {
        this.magnetListener = magnetListener2;
    }
}
