package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.drawable.Animatable2;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Trace;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.ViewTreeObserver;
import com.android.internal.graphics.ColorUtils;
import com.android.systemui.C0007R$anim;
import com.android.systemui.C0021R$string;
import com.android.systemui.Interpolators;
import com.android.systemui.statusbar.KeyguardAffordanceView;

public class LockIcon extends KeyguardAffordanceView {
    private static final int[][] LOCK_ANIM_RES_IDS = {new int[]{C0007R$anim.lock_to_error, C0007R$anim.lock_unlock, C0007R$anim.lock_lock, C0007R$anim.lock_scanning}, new int[]{C0007R$anim.lock_to_error_circular, C0007R$anim.lock_unlock_circular, C0007R$anim.lock_lock_circular, C0007R$anim.lock_scanning_circular}, new int[]{C0007R$anim.lock_to_error_filled, C0007R$anim.lock_unlock_filled, C0007R$anim.lock_lock_filled, C0007R$anim.lock_scanning_filled}, new int[]{C0007R$anim.lock_to_error_rounded, C0007R$anim.lock_unlock_rounded, C0007R$anim.lock_lock_rounded, C0007R$anim.lock_scanning_rounded}};
    private float mDozeAmount;
    private boolean mDozing;
    private final SparseArray<Drawable> mDrawableCache = new SparseArray<>();
    private int mIconColor;
    private boolean mKeyguardJustShown;
    private int mOldState;
    private final ViewTreeObserver.OnPreDrawListener mOnPreDrawListener = new ViewTreeObserver.OnPreDrawListener() {
        /* class com.android.systemui.statusbar.phone.LockIcon.AnonymousClass1 */

        public boolean onPreDraw() {
            LockIcon.this.getViewTreeObserver().removeOnPreDrawListener(this);
            LockIcon.this.mPredrawRegistered = false;
            final int i = LockIcon.this.mState;
            Drawable icon = LockIcon.this.getIcon(i);
            LockIcon.this.setImageDrawable(icon, false);
            if (i == 2) {
                LockIcon lockIcon = LockIcon.this;
                lockIcon.announceForAccessibility(lockIcon.getResources().getString(C0021R$string.accessibility_scanning_face));
            }
            if (!(icon instanceof AnimatedVectorDrawable)) {
                return true;
            }
            final AnimatedVectorDrawable animatedVectorDrawable = (AnimatedVectorDrawable) icon;
            animatedVectorDrawable.forceAnimationOnUI();
            animatedVectorDrawable.clearAnimationCallbacks();
            animatedVectorDrawable.registerAnimationCallback(new Animatable2.AnimationCallback() {
                /* class com.android.systemui.statusbar.phone.LockIcon.AnonymousClass1.AnonymousClass1 */

                public void onAnimationEnd(Drawable drawable) {
                    if (LockIcon.this.getDrawable() == animatedVectorDrawable && i == LockIcon.this.mState && i == 2) {
                        animatedVectorDrawable.start();
                    } else {
                        Trace.endAsyncSection("LockIcon#Animation", i);
                    }
                }
            });
            Trace.beginAsyncSection("LockIcon#Animation", i);
            animatedVectorDrawable.start();
            return true;
        }
    };
    private boolean mPredrawRegistered;
    private boolean mPulsing;
    private int mState;

    private static int getAnimationIndexForTransition(int i, int i2, boolean z, boolean z2, boolean z3) {
        if (z2 && !z) {
            return -1;
        }
        if (i2 == 3) {
            return 0;
        }
        if (i != 1 && i2 == 1) {
            return 1;
        }
        if (i == 1 && i2 == 0 && !z3) {
            return 2;
        }
        return i2 == 2 ? 3 : -1;
    }

    public LockIcon(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        this.mDrawableCache.clear();
    }

    /* access modifiers changed from: package-private */
    public boolean updateIconVisibility(boolean z) {
        if (z == (getVisibility() == 0)) {
            return false;
        }
        setVisibility(8);
        animate().cancel();
        if (z) {
            setScaleX(0.0f);
            setScaleY(0.0f);
            animate().setInterpolator(Interpolators.LINEAR_OUT_SLOW_IN).scaleX(1.0f).scaleY(1.0f).withLayer().setDuration(233).start();
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public void update(int i, boolean z, boolean z2, boolean z3) {
        this.mOldState = this.mState;
        this.mState = i;
        this.mPulsing = z;
        this.mDozing = z2;
        this.mKeyguardJustShown = z3;
        if (!this.mPredrawRegistered) {
            this.mPredrawRegistered = true;
            getViewTreeObserver().addOnPreDrawListener(this.mOnPreDrawListener);
        }
    }

    /* access modifiers changed from: package-private */
    public void setDozeAmount(float f) {
        this.mDozeAmount = f;
        updateDarkTint();
    }

    /* access modifiers changed from: package-private */
    public void onThemeChange(int i) {
        this.mDrawableCache.clear();
        this.mIconColor = i;
        updateDarkTint();
    }

    private void updateDarkTint() {
        setImageTintList(ColorStateList.valueOf(ColorUtils.blendARGB(this.mIconColor, -1, this.mDozeAmount)));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private Drawable getIcon(int i) {
        int animationIndexForTransition = getAnimationIndexForTransition(this.mOldState, i, this.mPulsing, this.mDozing, this.mKeyguardJustShown);
        int themedAnimationResId = animationIndexForTransition != -1 ? getThemedAnimationResId(animationIndexForTransition) : getIconForState(i);
        if (!this.mDrawableCache.contains(themedAnimationResId)) {
            this.mDrawableCache.put(themedAnimationResId, getResources().getDrawable(themedAnimationResId));
        }
        return this.mDrawableCache.get(themedAnimationResId);
    }

    private static int getIconForState(int i) {
        if (i != 0) {
            if (i == 1) {
                return 17302488;
            }
            if (!(i == 2 || i == 3)) {
                throw new IllegalArgumentException();
            }
        }
        return 17302479;
    }

    private int getThemedAnimationResId(int i) {
        int[][] iArr = LOCK_ANIM_RES_IDS;
        String emptyIfNull = TextUtils.emptyIfNull(Settings.Secure.getString(getContext().getContentResolver(), "theme_customization_overlay_packages"));
        if (emptyIfNull.contains("com.android.theme.icon_pack.circular.android")) {
            return iArr[1][i];
        }
        if (emptyIfNull.contains("com.android.theme.icon_pack.filled.android")) {
            return iArr[2][i];
        }
        if (emptyIfNull.contains("com.android.theme.icon_pack.rounded.android")) {
            return iArr[3][i];
        }
        return iArr[0][i];
    }
}
