package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import com.android.settingslib.drawable.UserIconDrawable;
import com.android.systemui.R$styleable;

public class UserAvatarView extends View {
    private final UserIconDrawable mDrawable;

    public UserAvatarView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mDrawable = new UserIconDrawable();
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R$styleable.UserAvatarView, i, i2);
        int indexCount = obtainStyledAttributes.getIndexCount();
        for (int i3 = 0; i3 < indexCount; i3++) {
            int index = obtainStyledAttributes.getIndex(i3);
            if (index == R$styleable.UserAvatarView_avatarPadding) {
                setAvatarPadding(obtainStyledAttributes.getDimension(index, 0.0f));
            } else if (index == R$styleable.UserAvatarView_frameWidth) {
                setFrameWidth(obtainStyledAttributes.getDimension(index, 0.0f));
            } else if (index == R$styleable.UserAvatarView_framePadding) {
                setFramePadding(obtainStyledAttributes.getDimension(index, 0.0f));
            } else if (index == R$styleable.UserAvatarView_frameColor) {
                setFrameColor(obtainStyledAttributes.getColorStateList(index));
            } else if (index == R$styleable.UserAvatarView_badgeDiameter) {
                setBadgeDiameter(obtainStyledAttributes.getDimension(index, 0.0f));
            } else if (index == R$styleable.UserAvatarView_badgeMargin) {
                setBadgeMargin(obtainStyledAttributes.getDimension(index, 0.0f));
            }
        }
        obtainStyledAttributes.recycle();
        setBackground(this.mDrawable);
    }

    public UserAvatarView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public UserAvatarView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public UserAvatarView(Context context) {
        this(context, (AttributeSet) null);
    }

    public void setActivated(boolean z) {
        super.setActivated(z);
        this.mDrawable.invalidateSelf();
    }

    public void setFrameColor(ColorStateList colorStateList) {
        this.mDrawable.setFrameColor(colorStateList);
    }

    public void setFrameWidth(float f) {
        this.mDrawable.setFrameWidth(f);
    }

    public void setFramePadding(float f) {
        this.mDrawable.setFramePadding(f);
    }

    public void setAvatarPadding(float f) {
        this.mDrawable.setPadding(f);
    }

    public void setBadgeDiameter(float f) {
        this.mDrawable.setBadgeRadius(f * 0.5f);
    }

    public void setBadgeMargin(float f) {
        this.mDrawable.setBadgeMargin(f);
    }

    public void setDrawableWithBadge(Drawable drawable, int i) {
        if (!(drawable instanceof UserIconDrawable)) {
            this.mDrawable.setIconDrawable(drawable);
            this.mDrawable.setBadgeIfManagedUser(getContext(), i);
            return;
        }
        throw new RuntimeException("Recursively adding UserIconDrawable");
    }
}
