package com.android.systemui.statusbar.policy;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.hardware.input.InputManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.ImageView;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.R$styleable;
import com.android.systemui.plugins.R;

public class KeyButtonView extends ImageView {
    private AudioManager mAudioManager;
    private final Runnable mCheckLongPress;
    /* access modifiers changed from: private */
    public int mCode;
    private int mContentDescriptionRes;
    private long mDownTime;
    private boolean mGestureAborted;
    /* access modifiers changed from: private */
    public boolean mLongClicked;
    /* access modifiers changed from: private */
    public boolean mSupportsLongpress;
    private int mTouchSlop;

    public KeyButtonView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public KeyButtonView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet);
        this.mSupportsLongpress = true;
        this.mCheckLongPress = new Runnable() {
            public void run() {
                if (!KeyButtonView.this.isPressed()) {
                    return;
                }
                if (KeyButtonView.this.isLongClickable()) {
                    KeyButtonView.this.performLongClick();
                    boolean unused = KeyButtonView.this.mLongClicked = true;
                } else if (KeyButtonView.this.mSupportsLongpress) {
                    if (KeyButtonView.this.mCode != 0) {
                        KeyButtonView.this.sendEvent(0, 128);
                        KeyButtonView.this.sendAccessibilityEvent(2);
                    }
                    boolean unused2 = KeyButtonView.this.mLongClicked = true;
                }
            }
        };
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R$styleable.KeyButtonView, i, 0);
        this.mCode = obtainStyledAttributes.getInteger(2, 0);
        this.mSupportsLongpress = obtainStyledAttributes.getBoolean(3, true);
        TypedValue typedValue = new TypedValue();
        if (obtainStyledAttributes.getValue(0, typedValue)) {
            this.mContentDescriptionRes = typedValue.resourceId;
        }
        obtainStyledAttributes.recycle();
        setClickable(true);
        this.mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        this.mAudioManager = (AudioManager) context.getSystemService("audio");
        setBackground(new KeyButtonRipple(context, this));
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        if (this.mContentDescriptionRes != 0) {
            setContentDescription(this.mContext.getString(this.mContentDescriptionRes));
        }
    }

    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo accessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(accessibilityNodeInfo);
        if (this.mCode != 0) {
            accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(16, (CharSequence) null));
            if (this.mSupportsLongpress || isLongClickable()) {
                accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(32, (CharSequence) null));
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onWindowVisibilityChanged(int i) {
        super.onWindowVisibilityChanged(i);
        if (i != 0) {
            jumpDrawablesToCurrentState();
        }
    }

    public boolean performAccessibilityActionInternal(int i, Bundle bundle) {
        if (i == 16 && this.mCode != 0) {
            sendEvent(0, 0, SystemClock.uptimeMillis());
            sendEvent(1, 0);
            sendAccessibilityEvent(1);
            playSoundEffect(0);
            return true;
        } else if (i != 32 || this.mCode == 0) {
            return super.performAccessibilityActionInternal(i, bundle);
        } else {
            sendEvent(0, 128);
            sendEvent(1, 0);
            sendAccessibilityEvent(2);
            return true;
        }
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        int action = motionEvent.getAction();
        boolean z = false;
        if (action == 0) {
            this.mGestureAborted = false;
        }
        if (this.mGestureAborted) {
            return false;
        }
        if (action == 0) {
            this.mDownTime = SystemClock.uptimeMillis();
            this.mLongClicked = false;
            setPressed(true);
            if (this.mCode != 0) {
                sendEvent(0, 0, this.mDownTime);
            } else {
                performHapticFeedback(1);
            }
            removeCallbacks(this.mCheckLongPress);
            postDelayed(this.mCheckLongPress, (long) ViewConfiguration.getLongPressTimeout());
        } else if (action == 1) {
            boolean z2 = isPressed() && !this.mLongClicked;
            setPressed(false);
            if (this.mCode != 0) {
                if (z2) {
                    sendEvent(1, 0);
                    sendAccessibilityEvent(1);
                    playSoundEffect(0);
                } else {
                    sendEvent(1, 32);
                }
            } else if (z2) {
                performClick();
            }
            removeCallbacks(this.mCheckLongPress);
        } else if (action == 2) {
            int x = (int) motionEvent.getX();
            int y = (int) motionEvent.getY();
            if (x >= (-this.mTouchSlop)) {
                int width = getWidth();
                int i = this.mTouchSlop;
                if (x < width + i && y >= (-i) && y < getHeight() + this.mTouchSlop) {
                    z = true;
                }
            }
            setPressed(z);
        } else if (action == 3) {
            setPressed(false);
            if (this.mCode != 0) {
                sendEvent(1, 32);
            }
            removeCallbacks(this.mCheckLongPress);
        }
        return true;
    }

    public void playSoundEffect(int i) {
        this.mAudioManager.playSoundEffect(i, KeyguardUpdateMonitor.getCurrentUser());
    }

    public void sendEvent(int i, int i2) {
        sendEvent(i, i2, SystemClock.uptimeMillis());
    }

    /* access modifiers changed from: package-private */
    public void sendEvent(int i, int i2, long j) {
        int i3 = i2;
        InputManager.getInstance().injectInputEvent(new KeyEvent(this.mDownTime, j, i, this.mCode, (i3 & 128) != 0 ? 1 : 0, 0, -1, 0, i3 | 8 | 64, 257), 0);
    }

    public void setAlpha(float f) {
        super.setAlpha(f);
        if (getId() == R.id.back) {
            Log.d("KeyButtonView", "set back alpha:" + f);
        }
    }

    public void setPressed(boolean z) {
        super.setPressed(z);
        if (!isEnabled()) {
            getBackground().jumpToCurrentState();
        }
    }
}
