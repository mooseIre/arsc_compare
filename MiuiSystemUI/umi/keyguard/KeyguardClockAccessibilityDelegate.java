package com.android.keyguard;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.TextView;
import com.android.systemui.plugins.R;

class KeyguardClockAccessibilityDelegate extends View.AccessibilityDelegate {
    private final String mFancyColon;

    public KeyguardClockAccessibilityDelegate(Context context) {
        this.mFancyColon = context.getString(R.string.keyguard_fancy_colon);
    }

    public void onInitializeAccessibilityEvent(View view, AccessibilityEvent accessibilityEvent) {
        super.onInitializeAccessibilityEvent(view, accessibilityEvent);
        CharSequence contentDescription = accessibilityEvent.getContentDescription();
        if (!TextUtils.isEmpty(contentDescription)) {
            accessibilityEvent.setContentDescription(replaceFancyColon(contentDescription));
        }
    }

    public void onPopulateAccessibilityEvent(View view, AccessibilityEvent accessibilityEvent) {
        CharSequence charSequence;
        if (view instanceof TextView) {
            charSequence = ((TextView) view).getText();
        } else {
            charSequence = accessibilityEvent.getContentDescription();
        }
        if (!TextUtils.isEmpty(charSequence)) {
            accessibilityEvent.getText().add(replaceFancyColon(charSequence));
        }
    }

    public void onInitializeAccessibilityNodeInfo(View view, AccessibilityNodeInfo accessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(view, accessibilityNodeInfo);
        if (!TextUtils.isEmpty(accessibilityNodeInfo.getText())) {
            accessibilityNodeInfo.setText(replaceFancyColon(accessibilityNodeInfo.getText()));
        }
        if (!TextUtils.isEmpty(accessibilityNodeInfo.getContentDescription())) {
            accessibilityNodeInfo.setContentDescription(replaceFancyColon(accessibilityNodeInfo.getContentDescription()));
        }
    }

    private CharSequence replaceFancyColon(CharSequence charSequence) {
        return charSequence.toString().replace(this.mFancyColon, ":");
    }
}
