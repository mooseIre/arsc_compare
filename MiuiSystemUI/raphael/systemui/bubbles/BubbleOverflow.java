package com.android.systemui.bubbles;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.InsetDrawable;
import android.util.PathParser;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.android.systemui.C0011R$color;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0017R$layout;
import com.android.systemui.C0021R$string;

public class BubbleOverflow implements BubbleViewProvider {
    private int mBitmapSize;
    private Context mContext;
    private int mDotColor;
    private BubbleExpandedView mExpandedView;
    private Bitmap mIcon;
    private int mIconBitmapSize;
    private LayoutInflater mInflater;
    private BadgedImageView mOverflowBtn;
    private Path mPath;

    @Override // com.android.systemui.bubbles.BubbleViewProvider
    public String getKey() {
        return "Overflow";
    }

    @Override // com.android.systemui.bubbles.BubbleViewProvider
    public void logUIEvent(int i, int i2, float f, float f2, int i3) {
    }

    @Override // com.android.systemui.bubbles.BubbleViewProvider
    public boolean showDot() {
        return false;
    }

    public BubbleOverflow(Context context) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
    }

    /* access modifiers changed from: package-private */
    public void setUpOverflow(ViewGroup viewGroup, BubbleStackView bubbleStackView) {
        updateDimensions();
        BubbleExpandedView bubbleExpandedView = (BubbleExpandedView) this.mInflater.inflate(C0017R$layout.bubble_expanded_view, viewGroup, false);
        this.mExpandedView = bubbleExpandedView;
        bubbleExpandedView.setOverflow(true);
        this.mExpandedView.setStackView(bubbleStackView);
        this.mExpandedView.applyThemeAttrs();
        updateIcon(this.mContext, viewGroup);
    }

    /* access modifiers changed from: package-private */
    public void updateDimensions() {
        this.mBitmapSize = this.mContext.getResources().getDimensionPixelSize(C0012R$dimen.bubble_bitmap_size);
        this.mIconBitmapSize = this.mContext.getResources().getDimensionPixelSize(C0012R$dimen.bubble_overflow_icon_bitmap_size);
        BubbleExpandedView bubbleExpandedView = this.mExpandedView;
        if (bubbleExpandedView != null) {
            bubbleExpandedView.updateDimensions();
        }
    }

    /* access modifiers changed from: package-private */
    public void updateIcon(Context context, ViewGroup viewGroup) {
        this.mContext = context;
        LayoutInflater from = LayoutInflater.from(context);
        this.mInflater = from;
        BadgedImageView badgedImageView = (BadgedImageView) from.inflate(C0017R$layout.bubble_overflow_button, viewGroup, false);
        this.mOverflowBtn = badgedImageView;
        badgedImageView.setContentDescription(this.mContext.getResources().getString(C0021R$string.bubble_overflow_button_content_description));
        Resources resources = this.mContext.getResources();
        TypedValue typedValue = new TypedValue();
        this.mContext.getTheme().resolveAttribute(16843829, typedValue, true);
        int color = this.mContext.getColor(typedValue.resourceId);
        this.mOverflowBtn.getDrawable().setTint(color);
        this.mDotColor = color;
        ColorDrawable colorDrawable = new ColorDrawable(resources.getColor(C0011R$color.bubbles_light));
        if ((resources.getConfiguration().uiMode & 48) == 32) {
            colorDrawable = new ColorDrawable(resources.getColor(C0011R$color.bubbles_dark));
        }
        AdaptiveIconDrawable adaptiveIconDrawable = new AdaptiveIconDrawable(colorDrawable, new InsetDrawable(this.mOverflowBtn.getDrawable(), this.mBitmapSize - this.mIconBitmapSize));
        BubbleIconFactory bubbleIconFactory = new BubbleIconFactory(this.mContext);
        this.mIcon = bubbleIconFactory.createBadgedIconBitmap(adaptiveIconDrawable, null, true).icon;
        float scale = bubbleIconFactory.getNormalizer().getScale(this.mOverflowBtn.getDrawable(), null, null, null);
        this.mPath = PathParser.createPathFromPathData(this.mContext.getResources().getString(17039929));
        Matrix matrix = new Matrix();
        matrix.setScale(scale, scale, 50.0f, 50.0f);
        this.mPath.transform(matrix);
        this.mOverflowBtn.setRenderedBubble(this);
    }

    /* access modifiers changed from: package-private */
    public void setVisible(int i) {
        this.mOverflowBtn.setVisibility(i);
    }

    @Override // com.android.systemui.bubbles.BubbleViewProvider
    public BubbleExpandedView getExpandedView() {
        return this.mExpandedView;
    }

    @Override // com.android.systemui.bubbles.BubbleViewProvider
    public int getDotColor() {
        return this.mDotColor;
    }

    @Override // com.android.systemui.bubbles.BubbleViewProvider
    public Bitmap getBadgedImage() {
        return this.mIcon;
    }

    @Override // com.android.systemui.bubbles.BubbleViewProvider
    public Path getDotPath() {
        return this.mPath;
    }

    @Override // com.android.systemui.bubbles.BubbleViewProvider
    public void setContentVisibility(boolean z) {
        this.mExpandedView.setContentVisibility(z);
    }

    @Override // com.android.systemui.bubbles.BubbleViewProvider
    public View getIconView() {
        return this.mOverflowBtn;
    }

    @Override // com.android.systemui.bubbles.BubbleViewProvider
    public int getDisplayId() {
        BubbleExpandedView bubbleExpandedView = this.mExpandedView;
        if (bubbleExpandedView != null) {
            return bubbleExpandedView.getVirtualDisplayId();
        }
        return -1;
    }
}
