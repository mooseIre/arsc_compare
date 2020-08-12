package com.android.systemui.settings;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.SeekBar;
import com.android.settingslib.RestrictedLockUtils;
import com.android.systemui.Dependency;
import com.android.systemui.miui.widget.RelativeSeekBarInjector;
import com.android.systemui.plugins.ActivityStarter;

public class ToggleSeekBar extends SeekBar {
    private String mAccessibilityLabel;
    private RestrictedLockUtils.EnforcedAdmin mEnforcedAdmin;
    private RelativeSeekBarInjector mInjector;

    public ToggleSeekBar(Context context) {
        this(context, (AttributeSet) null);
    }

    public ToggleSeekBar(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public ToggleSeekBar(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mEnforcedAdmin = null;
        initInjector();
    }

    private void initInjector() {
        this.mInjector = new RelativeSeekBarInjector(this, false);
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (this.mEnforcedAdmin != null) {
            ((ActivityStarter) Dependency.get(ActivityStarter.class)).postStartActivityDismissingKeyguard(RestrictedLockUtils.getShowAdminSupportDetailsIntent(this.mContext, this.mEnforcedAdmin), 0);
            return true;
        }
        if (!isEnabled()) {
            setEnabled(true);
        }
        RelativeSeekBarInjector relativeSeekBarInjector = this.mInjector;
        if (relativeSeekBarInjector != null) {
            relativeSeekBarInjector.transformTouchEvent(motionEvent);
        }
        return super.onTouchEvent(motionEvent);
    }

    public void setAccessibilityLabel(String str) {
        this.mAccessibilityLabel = str;
    }

    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo accessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(accessibilityNodeInfo);
        String str = this.mAccessibilityLabel;
        if (str != null) {
            accessibilityNodeInfo.setText(str);
        }
    }
}
