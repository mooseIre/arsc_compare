package com.android.keyguard;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityManager;

/* access modifiers changed from: package-private */
public class LiftToActivateListener implements View.OnHoverListener {
    private final AccessibilityManager mAccessibilityManager;
    private boolean mCachedClickableState;

    public LiftToActivateListener(Context context) {
        this.mAccessibilityManager = (AccessibilityManager) context.getSystemService("accessibility");
    }

    public boolean onHover(View view, MotionEvent motionEvent) {
        if (this.mAccessibilityManager.isEnabled() && this.mAccessibilityManager.isTouchExplorationEnabled()) {
            int actionMasked = motionEvent.getActionMasked();
            if (actionMasked == 9) {
                this.mCachedClickableState = view.isClickable();
                view.setClickable(false);
            } else if (actionMasked == 10) {
                int x = (int) motionEvent.getX();
                int y = (int) motionEvent.getY();
                if (x > view.getPaddingLeft() && y > view.getPaddingTop() && x < view.getWidth() - view.getPaddingRight() && y < view.getHeight() - view.getPaddingBottom()) {
                    view.performClick();
                }
                view.setClickable(this.mCachedClickableState);
            }
        }
        view.onHoverEvent(motionEvent);
        return true;
    }
}
