package com.android.systemui.volume;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.core.view.accessibility.AccessibilityViewCommand;
import com.android.keyguard.AlphaOptimizedImageButton;
import com.android.systemui.C0009R$attr;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0021R$string;

public class CaptionsToggleImageButton extends AlphaOptimizedImageButton {
    private static final int[] OPTED_OUT_STATE = {C0009R$attr.optedOut};
    private boolean mCaptionsEnabled = false;
    private ConfirmedTapListener mConfirmedTapListener;
    private GestureDetector mGestureDetector;
    private GestureDetector.SimpleOnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener() {
        /* class com.android.systemui.volume.CaptionsToggleImageButton.AnonymousClass1 */

        public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
            return CaptionsToggleImageButton.this.tryToSendTapConfirmedEvent();
        }
    };
    private boolean mOptedOut = false;

    /* access modifiers changed from: package-private */
    public interface ConfirmedTapListener {
        void onConfirmedTap();
    }

    public CaptionsToggleImageButton(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setContentDescription(getContext().getString(C0021R$string.volume_odi_captions_content_description));
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        GestureDetector gestureDetector = this.mGestureDetector;
        if (gestureDetector != null) {
            gestureDetector.onTouchEvent(motionEvent);
        }
        return super.onTouchEvent(motionEvent);
    }

    public int[] onCreateDrawableState(int i) {
        int[] onCreateDrawableState = super.onCreateDrawableState(i + 1);
        if (this.mOptedOut) {
            ImageButton.mergeDrawableStates(onCreateDrawableState, OPTED_OUT_STATE);
        }
        return onCreateDrawableState;
    }

    /* access modifiers changed from: package-private */
    public Runnable setCaptionsEnabled(boolean z) {
        String str;
        int i;
        this.mCaptionsEnabled = z;
        AccessibilityNodeInfoCompat.AccessibilityActionCompat accessibilityActionCompat = AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_CLICK;
        if (z) {
            str = getContext().getString(C0021R$string.volume_odi_captions_hint_disable);
        } else {
            str = getContext().getString(C0021R$string.volume_odi_captions_hint_enable);
        }
        ViewCompat.replaceAccessibilityAction(this, accessibilityActionCompat, str, new AccessibilityViewCommand() {
            /* class com.android.systemui.volume.$$Lambda$CaptionsToggleImageButton$G1CrD3iT19JR_3drnIgC4b3Mg */

            @Override // androidx.core.view.accessibility.AccessibilityViewCommand
            public final boolean perform(View view, AccessibilityViewCommand.CommandArguments commandArguments) {
                return CaptionsToggleImageButton.this.lambda$setCaptionsEnabled$0$CaptionsToggleImageButton(view, commandArguments);
            }
        });
        if (this.mCaptionsEnabled) {
            i = C0013R$drawable.ic_volume_odi_captions;
        } else {
            i = C0013R$drawable.ic_volume_odi_captions_disabled;
        }
        return setImageResourceAsync(i);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$setCaptionsEnabled$0 */
    public /* synthetic */ boolean lambda$setCaptionsEnabled$0$CaptionsToggleImageButton(View view, AccessibilityViewCommand.CommandArguments commandArguments) {
        return tryToSendTapConfirmedEvent();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean tryToSendTapConfirmedEvent() {
        ConfirmedTapListener confirmedTapListener = this.mConfirmedTapListener;
        if (confirmedTapListener == null) {
            return false;
        }
        confirmedTapListener.onConfirmedTap();
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean getCaptionsEnabled() {
        return this.mCaptionsEnabled;
    }

    /* access modifiers changed from: package-private */
    public void setOptedOut(boolean z) {
        this.mOptedOut = z;
        refreshDrawableState();
    }

    /* access modifiers changed from: package-private */
    public boolean getOptedOut() {
        return this.mOptedOut;
    }

    /* access modifiers changed from: package-private */
    public void setOnConfirmedTapListener(ConfirmedTapListener confirmedTapListener, Handler handler) {
        this.mConfirmedTapListener = confirmedTapListener;
        if (this.mGestureDetector == null) {
            this.mGestureDetector = new GestureDetector(getContext(), this.mGestureListener, handler);
        }
    }
}
