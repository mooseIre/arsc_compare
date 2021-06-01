package com.android.systemui.qs.tiles;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.internal.util.ArrayUtils;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0017R$layout;
import com.android.systemui.FontSizeUtils;
import com.android.systemui.R$styleable;
import com.android.systemui.statusbar.phone.UserAvatarView;

public class UserDetailItemView extends LinearLayout {
    protected static int layoutResId = C0017R$layout.qs_user_detail_item;
    private int mActivatedStyle;
    private UserAvatarView mAvatar;
    private TextView mName;
    private int mRegularStyle;
    private View mRestrictedPadlock;

    public boolean hasOverlappingRendering() {
        return false;
    }

    public UserDetailItemView(Context context) {
        this(context, null);
    }

    public UserDetailItemView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public UserDetailItemView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public UserDetailItemView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R$styleable.UserDetailItemView, i, i2);
        int indexCount = obtainStyledAttributes.getIndexCount();
        for (int i3 = 0; i3 < indexCount; i3++) {
            int index = obtainStyledAttributes.getIndex(i3);
            if (index == R$styleable.UserDetailItemView_regularTextAppearance) {
                this.mRegularStyle = obtainStyledAttributes.getResourceId(index, 0);
            } else if (index == R$styleable.UserDetailItemView_activatedTextAppearance) {
                this.mActivatedStyle = obtainStyledAttributes.getResourceId(index, 0);
            }
        }
        obtainStyledAttributes.recycle();
    }

    public static UserDetailItemView convertOrInflate(Context context, View view, ViewGroup viewGroup) {
        if (!(view instanceof UserDetailItemView)) {
            view = LayoutInflater.from(context).inflate(layoutResId, viewGroup, false);
        }
        return (UserDetailItemView) view;
    }

    public void bind(String str, Drawable drawable, int i) {
        this.mName.setText(str);
        this.mAvatar.setDrawableWithBadge(drawable, i);
    }

    public void setDisabledByAdmin(boolean z) {
        View view = this.mRestrictedPadlock;
        if (view != null) {
            view.setVisibility(z ? 0 : 8);
        }
        setEnabled(!z);
    }

    public void setEnabled(boolean z) {
        super.setEnabled(z);
        this.mName.setEnabled(z);
        this.mAvatar.setEnabled(z);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        this.mAvatar = (UserAvatarView) findViewById(C0015R$id.user_picture);
        TextView textView = (TextView) findViewById(C0015R$id.user_name);
        this.mName = textView;
        if (this.mRegularStyle == 0) {
            this.mRegularStyle = textView.getExplicitStyle();
        }
        if (this.mActivatedStyle == 0) {
            this.mActivatedStyle = this.mName.getExplicitStyle();
        }
        updateTextStyle();
        this.mRestrictedPadlock = findViewById(C0015R$id.restricted_padlock);
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        FontSizeUtils.updateFontSize(this.mName, getFontSizeDimen());
    }

    /* access modifiers changed from: protected */
    public void drawableStateChanged() {
        super.drawableStateChanged();
        updateTextStyle();
    }

    private void updateTextStyle() {
        this.mName.setTextAppearance(ArrayUtils.contains(getDrawableState(), 16843518) ? this.mActivatedStyle : this.mRegularStyle);
    }

    /* access modifiers changed from: protected */
    public int getFontSizeDimen() {
        return C0012R$dimen.qs_detail_item_secondary_text_size;
    }
}
