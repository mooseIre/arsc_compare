package com.android.systemui.statusbar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Notification;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.FloatProperty;
import android.util.Log;
import android.util.Property;
import android.util.TypedValue;
import android.view.ViewDebug;
import android.view.accessibility.AccessibilityEvent;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import androidx.core.graphics.ColorUtils;
import com.android.internal.statusbar.StatusBarIcon;
import com.android.internal.util.ContrastColorUtil;
import com.android.systemui.C0010R$bool;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0021R$string;
import com.android.systemui.Interpolators;
import com.android.systemui.statusbar.notification.ExpandedNotification;
import com.android.systemui.statusbar.notification.NotificationIconDozeHelper;
import com.android.systemui.statusbar.notification.NotificationUtil;
import com.android.systemui.statusbar.notification.NotificationUtils;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.function.Consumer;

public class StatusBarIconView extends AnimatedImageView implements StatusIconDisplayable {
    private static final Property<StatusBarIconView, Float> DOT_APPEAR_AMOUNT = new FloatProperty<StatusBarIconView>("dot_appear_amount") {
        /* class com.android.systemui.statusbar.StatusBarIconView.AnonymousClass2 */

        public void setValue(StatusBarIconView statusBarIconView, float f) {
            statusBarIconView.setDotAppearAmount(f);
        }

        public Float get(StatusBarIconView statusBarIconView) {
            return Float.valueOf(statusBarIconView.getDotAppearAmount());
        }
    };
    private static final Property<StatusBarIconView, Float> ICON_APPEAR_AMOUNT = new FloatProperty<StatusBarIconView>("iconAppearAmount") {
        /* class com.android.systemui.statusbar.StatusBarIconView.AnonymousClass1 */

        public void setValue(StatusBarIconView statusBarIconView, float f) {
            statusBarIconView.setIconAppearAmount(f);
        }

        public Float get(StatusBarIconView statusBarIconView) {
            return Float.valueOf(statusBarIconView.getIconAppearAmount());
        }
    };
    private boolean mAlwaysScaleIcon;
    private int mAnimationStartColor;
    private final boolean mBlocked;
    private int mCachedContrastBackgroundColor;
    private ValueAnimator mColorAnimator;
    private final ValueAnimator.AnimatorUpdateListener mColorUpdater;
    private int mContrastedDrawableColor;
    private int mCurrentSetColor;
    private boolean mDark;
    private int mDecorColor;
    private int mDensity;
    private ObjectAnimator mDotAnimator;
    private float mDotAppearAmount;
    private final Paint mDotPaint;
    private float mDotRadius;
    private float mDozeAmount;
    private final NotificationIconDozeHelper mDozer;
    private int mDrawableColor;
    private StatusBarIcon mIcon;
    private float mIconAppearAmount;
    private ObjectAnimator mIconAppearAnimator;
    private int mIconColor;
    private float mIconScale;
    private boolean mIncreasedSize;
    private Runnable mLayoutRunnable;
    private float[] mMatrix;
    private ColorMatrixColorFilter mMatrixColorFilter;
    private boolean mNightMode;
    private ExpandedNotification mNotification;
    private Drawable mNumberBackground;
    private Paint mNumberPain;
    private String mNumberText;
    private int mNumberX;
    private int mNumberY;
    private Runnable mOnDismissListener;
    private OnVisibilityChangedListener mOnVisibilityChangedListener;
    private boolean mShowsConversation;
    @ViewDebug.ExportedProperty
    private String mSlot;
    private int mStaticDotRadius;
    private int mStatusBarIconDrawingSize;
    private int mStatusBarIconDrawingSizeIncreased;
    private int mStatusBarIconSize;
    private float mSystemIconDesiredHeight;
    private float mSystemIconIntrinsicHeight;
    private boolean mUseTint;
    private int mVisibleState;

    public interface OnVisibilityChangedListener {
        void onVisibilityChanged(int i);
    }

    private void updateIconScaleForSystemIcons() {
    }

    @Override // com.android.systemui.statusbar.AnimatedImageView
    public boolean hasOverlappingRendering() {
        return false;
    }

    public void setIsInShelf(boolean z) {
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$0 */
    public /* synthetic */ void lambda$new$0$StatusBarIconView(ValueAnimator valueAnimator) {
        setColorInternal(NotificationUtils.interpolateColors(this.mAnimationStartColor, this.mIconColor, valueAnimator.getAnimatedFraction()));
    }

    public StatusBarIconView(Context context, String str, ExpandedNotification expandedNotification) {
        this(context, str, expandedNotification, false);
    }

    public StatusBarIconView(Context context, String str, ExpandedNotification expandedNotification, boolean z) {
        super(context);
        this.mSystemIconDesiredHeight = 15.0f;
        this.mSystemIconIntrinsicHeight = 17.0f;
        boolean z2 = true;
        this.mStatusBarIconDrawingSizeIncreased = 1;
        this.mStatusBarIconDrawingSize = 1;
        this.mStatusBarIconSize = 1;
        this.mIconScale = 1.0f;
        this.mDotPaint = new Paint(1);
        this.mVisibleState = 0;
        this.mIconAppearAmount = 1.0f;
        this.mCurrentSetColor = 0;
        this.mAnimationStartColor = 0;
        this.mColorUpdater = new ValueAnimator.AnimatorUpdateListener() {
            /* class com.android.systemui.statusbar.$$Lambda$StatusBarIconView$nRA4PFzSKIqshXSve3PBqKMX7Q */

            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                StatusBarIconView.this.lambda$new$0$StatusBarIconView(valueAnimator);
            }
        };
        this.mCachedContrastBackgroundColor = 0;
        this.mDozer = new NotificationIconDozeHelper(context);
        this.mBlocked = z;
        this.mSlot = str;
        Paint paint = new Paint();
        this.mNumberPain = paint;
        paint.setTextAlign(Paint.Align.CENTER);
        this.mNumberPain.setColor(context.getColor(C0013R$drawable.notification_number_text_color));
        this.mNumberPain.setAntiAlias(true);
        setNotification(expandedNotification);
        setScaleType(ImageView.ScaleType.CENTER);
        this.mDensity = context.getResources().getDisplayMetrics().densityDpi;
        this.mNightMode = (context.getResources().getConfiguration().uiMode & 48) != 32 ? false : z2;
        initializeDecorColor();
        reloadDimens();
        maybeUpdateIconScaleDimens();
    }

    public StatusBarIconView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mSystemIconDesiredHeight = 15.0f;
        this.mSystemIconIntrinsicHeight = 17.0f;
        this.mStatusBarIconDrawingSizeIncreased = 1;
        this.mStatusBarIconDrawingSize = 1;
        this.mStatusBarIconSize = 1;
        this.mIconScale = 1.0f;
        this.mDotPaint = new Paint(1);
        this.mVisibleState = 0;
        this.mIconAppearAmount = 1.0f;
        this.mCurrentSetColor = 0;
        this.mAnimationStartColor = 0;
        this.mColorUpdater = new ValueAnimator.AnimatorUpdateListener() {
            /* class com.android.systemui.statusbar.$$Lambda$StatusBarIconView$nRA4PFzSKIqshXSve3PBqKMX7Q */

            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                StatusBarIconView.this.lambda$new$0$StatusBarIconView(valueAnimator);
            }
        };
        this.mCachedContrastBackgroundColor = 0;
        this.mDozer = new NotificationIconDozeHelper(context);
        this.mBlocked = false;
        this.mAlwaysScaleIcon = true;
        reloadDimens();
        maybeUpdateIconScaleDimens();
        this.mDensity = context.getResources().getDisplayMetrics().densityDpi;
    }

    private void maybeUpdateIconScaleDimens() {
        if (this.mNotification != null || this.mAlwaysScaleIcon) {
            updateIconScaleForNotifications();
        } else {
            updateIconScaleForSystemIcons();
        }
    }

    private void updateIconScaleForNotifications() {
        this.mIconScale = ((float) (this.mIncreasedSize ? this.mStatusBarIconDrawingSizeIncreased : this.mStatusBarIconDrawingSize)) / ((float) this.mStatusBarIconSize);
        updatePivot();
    }

    public float getIconScaleIncreased() {
        return ((float) this.mStatusBarIconDrawingSizeIncreased) / ((float) this.mStatusBarIconDrawingSize);
    }

    public float getIconScale() {
        return this.mIconScale;
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        int i = configuration.densityDpi;
        if (i != this.mDensity) {
            this.mDensity = i;
            reloadDimens();
            updateDrawable();
            maybeUpdateIconScaleDimens();
        }
        boolean z = (configuration.uiMode & 48) == 32;
        if (z != this.mNightMode) {
            this.mNightMode = z;
            initializeDecorColor();
        }
    }

    private void reloadDimens() {
        boolean z = this.mDotRadius == ((float) this.mStaticDotRadius);
        Resources resources = getResources();
        this.mStaticDotRadius = resources.getDimensionPixelSize(C0012R$dimen.overflow_dot_radius);
        this.mStatusBarIconSize = resources.getDimensionPixelSize(C0012R$dimen.status_bar_icon_size);
        this.mStatusBarIconDrawingSizeIncreased = resources.getDimensionPixelSize(C0012R$dimen.status_bar_icon_drawing_size_dark);
        this.mStatusBarIconDrawingSize = resources.getDimensionPixelSize(C0012R$dimen.status_bar_icon_drawing_size);
        if (z) {
            this.mDotRadius = (float) this.mStaticDotRadius;
        }
        this.mSystemIconDesiredHeight = resources.getDimension(17105494);
        this.mSystemIconIntrinsicHeight = resources.getDimension(17105493);
    }

    public void setNotification(ExpandedNotification expandedNotification) {
        this.mNotification = expandedNotification;
        if (expandedNotification != null) {
            setContentDescription(expandedNotification.getNotification());
        }
        maybeUpdateIconScaleDimens();
    }

    public boolean equalIcons(Icon icon, Icon icon2) {
        if (icon == icon2) {
            return true;
        }
        if (icon.getType() != icon2.getType()) {
            return false;
        }
        int type = icon.getType();
        if (type == 2) {
            return icon.getResPackage().equals(icon2.getResPackage()) && icon.getResId() == icon2.getResId();
        }
        if (type == 4 || type == 6) {
            return icon.getUriString().equals(icon2.getUriString());
        }
        return false;
    }

    public boolean set(StatusBarIcon statusBarIcon) {
        MiuiStatusBarIconViewHelper.transformIconIfNeed(getContext(), statusBarIcon, this.mNotification, this.mSlot, this.mUseTint, this.mDark);
        StatusBarIcon statusBarIcon2 = this.mIcon;
        int i = 0;
        boolean z = statusBarIcon2 != null && equalIcons(statusBarIcon2.icon, statusBarIcon.icon);
        boolean z2 = z && this.mIcon.iconLevel == statusBarIcon.iconLevel;
        StatusBarIcon statusBarIcon3 = this.mIcon;
        boolean z3 = statusBarIcon3 != null && statusBarIcon3.visible == statusBarIcon.visible;
        StatusBarIcon statusBarIcon4 = this.mIcon;
        boolean z4 = statusBarIcon4 != null && statusBarIcon4.number == statusBarIcon.number;
        this.mIcon = statusBarIcon.clone();
        setContentDescription(statusBarIcon.contentDescription);
        if (!z) {
            if (!updateDrawable(false)) {
                return false;
            }
            setTag(C0015R$id.icon_is_grayscale, null);
            maybeUpdateIconScaleDimens();
        }
        if (!z2) {
            setImageLevel(statusBarIcon.iconLevel);
        }
        if (!z4) {
            if (statusBarIcon.number <= 0 || !getContext().getResources().getBoolean(C0010R$bool.config_statusBarShowNumber)) {
                this.mNumberBackground = null;
                this.mNumberText = null;
            } else {
                if (this.mNumberBackground == null) {
                    this.mNumberBackground = getContext().getResources().getDrawable(C0013R$drawable.ic_notification_overlay);
                }
                placeNumber();
            }
            invalidate();
        }
        if (!z3) {
            if (!statusBarIcon.visible || this.mBlocked) {
                i = 8;
            }
            setVisibility(i);
        }
        return true;
    }

    public void updateDrawable() {
        updateDrawable(true);
    }

    private boolean updateDrawable(boolean z) {
        StatusBarIcon statusBarIcon = this.mIcon;
        if (statusBarIcon == null) {
            return false;
        }
        try {
            Drawable icon = getIcon(statusBarIcon);
            if (icon == null) {
                Log.w("StatusBarIconView", "No icon for slot " + this.mSlot + "; " + this.mIcon.icon);
                return false;
            }
            if (z) {
                setImageDrawable(null);
            }
            setImageDrawable(icon);
            return true;
        } catch (OutOfMemoryError unused) {
            Log.w("StatusBarIconView", "OOM while inflating " + this.mIcon.icon + " for slot " + this.mSlot);
            return false;
        }
    }

    public Icon getSourceIcon() {
        return this.mIcon.icon;
    }

    private Drawable getIcon(StatusBarIcon statusBarIcon) {
        return getIcon(getContext(), statusBarIcon);
    }

    public static Drawable getIcon(Context context, StatusBarIcon statusBarIcon) {
        int identifier = statusBarIcon.user.getIdentifier();
        if (identifier == -1) {
            identifier = 0;
        }
        Drawable loadDrawableAsUser = statusBarIcon.icon.loadDrawableAsUser(context, identifier);
        TypedValue typedValue = new TypedValue();
        context.getResources().getValue(C0012R$dimen.status_bar_icon_scale_factor, typedValue, true);
        float f = typedValue.getFloat();
        if (f == 1.0f) {
            return loadDrawableAsUser;
        }
        return new ScalingDrawableWrapper(loadDrawableAsUser, f);
    }

    public StatusBarIcon getStatusBarIcon() {
        return this.mIcon;
    }

    public void onInitializeAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        super.onInitializeAccessibilityEvent(accessibilityEvent);
        ExpandedNotification expandedNotification = this.mNotification;
        if (expandedNotification != null) {
            accessibilityEvent.setParcelableData(expandedNotification.getNotification());
        }
    }

    /* access modifiers changed from: protected */
    public void onSizeChanged(int i, int i2, int i3, int i4) {
        super.onSizeChanged(i, i2, i3, i4);
        if (this.mNumberBackground != null) {
            placeNumber();
        }
    }

    public void onRtlPropertiesChanged(int i) {
        super.onRtlPropertiesChanged(i);
        updateDrawable();
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        float f;
        if (this.mIconAppearAmount > 0.0f) {
            canvas.save();
            float f2 = this.mIconScale;
            float f3 = this.mIconAppearAmount;
            canvas.scale(f2 * f3, f2 * f3, (float) (getWidth() / 2), (float) (getHeight() / 2));
            super.onDraw(canvas);
            canvas.restore();
        }
        Drawable drawable = this.mNumberBackground;
        if (drawable != null) {
            drawable.draw(canvas);
            canvas.drawText(this.mNumberText, (float) this.mNumberX, (float) this.mNumberY, this.mNumberPain);
        }
        if (this.mDotAppearAmount != 0.0f) {
            float alpha = ((float) Color.alpha(this.mDecorColor)) / 255.0f;
            float f4 = this.mDotAppearAmount;
            if (f4 <= 1.0f) {
                f = this.mDotRadius * f4;
            } else {
                float f5 = f4 - 1.0f;
                alpha *= 1.0f - f5;
                f = NotificationUtils.interpolate(this.mDotRadius, (float) (getWidth() / 4), f5);
            }
            this.mDotPaint.setAlpha((int) (alpha * 255.0f));
            canvas.drawCircle((float) (getWidth() / 2), (float) (getHeight() / 2), f, this.mDotPaint);
        }
    }

    /* access modifiers changed from: protected */
    public void debug(int i) {
        super.debug(i);
        Log.d("View", ImageView.debugIndent(i) + "slot=" + this.mSlot);
        Log.d("View", ImageView.debugIndent(i) + "icon=" + this.mIcon);
    }

    /* access modifiers changed from: package-private */
    public void placeNumber() {
        String str;
        if (this.mIcon.number > getContext().getResources().getInteger(17694723)) {
            str = getContext().getResources().getString(17039383);
        } else {
            str = NumberFormat.getIntegerInstance().format((long) this.mIcon.number);
        }
        this.mNumberText = str;
        int width = getWidth();
        int height = getHeight();
        Rect rect = new Rect();
        this.mNumberPain.getTextBounds(str, 0, str.length(), rect);
        int i = rect.right - rect.left;
        int i2 = rect.bottom - rect.top;
        this.mNumberBackground.getPadding(rect);
        int i3 = rect.left + i + rect.right;
        if (i3 < this.mNumberBackground.getMinimumWidth()) {
            i3 = this.mNumberBackground.getMinimumWidth();
        }
        int i4 = rect.right;
        this.mNumberX = (width - i4) - (((i3 - i4) - rect.left) / 2);
        int i5 = rect.top + i2 + rect.bottom;
        if (i5 < this.mNumberBackground.getMinimumWidth()) {
            i5 = this.mNumberBackground.getMinimumWidth();
        }
        int i6 = rect.bottom;
        this.mNumberY = (height - i6) - ((((i5 - rect.top) - i2) - i6) / 2);
        this.mNumberBackground.setBounds(width - i3, height - i5, width, height);
    }

    private void setContentDescription(Notification notification) {
        if (notification != null) {
            String contentDescForNotification = contentDescForNotification(((ImageView) this).mContext, notification);
            if (!TextUtils.isEmpty(contentDescForNotification)) {
                setContentDescription(contentDescForNotification);
            }
        }
    }

    public String toString() {
        return "StatusBarIconView(slot=" + this.mSlot + " icon=" + this.mIcon + " notification=" + this.mNotification + ")";
    }

    public ExpandedNotification getNotification() {
        return this.mNotification;
    }

    @Override // com.android.systemui.statusbar.StatusIconDisplayable
    public String getSlot() {
        return this.mSlot;
    }

    public static String contentDescForNotification(Context context, Notification notification) {
        CharSequence charSequence;
        CharSequence charSequence2 = "";
        try {
            charSequence = Notification.Builder.recoverBuilder(context, notification).loadHeaderAppName();
        } catch (RuntimeException e) {
            Log.e("StatusBarIconView", "Unable to recover builder", e);
            Parcelable parcelable = notification.extras.getParcelable("android.appInfo");
            charSequence = parcelable instanceof ApplicationInfo ? String.valueOf(((ApplicationInfo) parcelable).loadLabel(context.getPackageManager())) : charSequence2;
        }
        CharSequence charSequence3 = notification.extras.getCharSequence("android.title");
        CharSequence charSequence4 = notification.extras.getCharSequence("android.text");
        CharSequence charSequence5 = notification.tickerText;
        if (TextUtils.equals(charSequence3, charSequence)) {
            charSequence3 = charSequence4;
        }
        if (!TextUtils.isEmpty(charSequence3)) {
            charSequence2 = charSequence3;
        } else if (!TextUtils.isEmpty(charSequence5)) {
            charSequence2 = charSequence5;
        }
        return context.getString(C0021R$string.accessibility_desc_notification_icon, charSequence, charSequence2);
    }

    public void setDecorColor(int i) {
        this.mDecorColor = i;
        updateDecorColor();
    }

    private void initializeDecorColor() {
        if (this.mNotification != null) {
            setDecorColor(getContext().getColor(this.mNightMode ? 17170889 : 17170890));
        }
    }

    private void updateDecorColor() {
        int interpolateColors = NotificationUtils.interpolateColors(this.mDecorColor, -1, this.mDozeAmount);
        if (this.mDotPaint.getColor() != interpolateColors) {
            this.mDotPaint.setColor(interpolateColors);
            if (this.mDotAppearAmount != 0.0f) {
                invalidate();
            }
        }
    }

    public void setStaticDrawableColor(int i) {
        this.mDrawableColor = i;
        setColorInternal(i);
        updateContrastedStaticColor();
        this.mIconColor = i;
        this.mDozer.setColor(i);
    }

    private void setColorInternal(int i) {
        this.mCurrentSetColor = i;
        updateIconColor();
    }

    private void updateIconColor() {
        if (this.mShowsConversation) {
            setColorFilter((ColorFilter) null);
        } else if (NotificationUtil.ignoreStatusBarIconColor(this.mNotification)) {
            setColorFilter((ColorFilter) null);
        } else if (this.mCurrentSetColor != 0) {
            if (this.mMatrixColorFilter == null) {
                this.mMatrix = new float[20];
                this.mMatrixColorFilter = new ColorMatrixColorFilter(this.mMatrix);
            }
            updateTintMatrix(this.mMatrix, NotificationUtils.interpolateColors(this.mCurrentSetColor, -1, this.mDozeAmount), this.mDozeAmount * 0.67f);
            this.mMatrixColorFilter.setColorMatrixArray(this.mMatrix);
            setColorFilter((ColorFilter) null);
            setColorFilter(this.mMatrixColorFilter);
        } else {
            this.mDozer.updateGrayscale(this, this.mDozeAmount);
        }
    }

    private static void updateTintMatrix(float[] fArr, int i, float f) {
        Arrays.fill(fArr, 0.0f);
        fArr[4] = (float) Color.red(i);
        fArr[9] = (float) Color.green(i);
        fArr[14] = (float) Color.blue(i);
        fArr[18] = (((float) Color.alpha(i)) / 255.0f) + f;
    }

    public void setIconColor(int i, boolean z) {
        if (this.mIconColor != i) {
            this.mIconColor = i;
            ValueAnimator valueAnimator = this.mColorAnimator;
            if (valueAnimator != null) {
                valueAnimator.cancel();
            }
            int i2 = this.mCurrentSetColor;
            if (i2 != i) {
                if (!z || i2 == 0) {
                    setColorInternal(i);
                    return;
                }
                this.mAnimationStartColor = i2;
                ValueAnimator ofFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
                this.mColorAnimator = ofFloat;
                ofFloat.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
                this.mColorAnimator.setDuration(100L);
                this.mColorAnimator.addUpdateListener(this.mColorUpdater);
                this.mColorAnimator.addListener(new AnimatorListenerAdapter() {
                    /* class com.android.systemui.statusbar.StatusBarIconView.AnonymousClass3 */

                    public void onAnimationEnd(Animator animator) {
                        StatusBarIconView.this.mColorAnimator = null;
                        StatusBarIconView.this.mAnimationStartColor = 0;
                    }
                });
                this.mColorAnimator.start();
            }
        }
    }

    public int getStaticDrawableColor() {
        return this.mDrawableColor;
    }

    /* access modifiers changed from: package-private */
    public int getContrastedStaticDrawableColor(int i) {
        if (this.mCachedContrastBackgroundColor != i) {
            this.mCachedContrastBackgroundColor = i;
            updateContrastedStaticColor();
        }
        return this.mContrastedDrawableColor;
    }

    private void updateContrastedStaticColor() {
        if (Color.alpha(this.mCachedContrastBackgroundColor) != 255) {
            this.mContrastedDrawableColor = this.mDrawableColor;
            return;
        }
        int i = this.mDrawableColor;
        if (!ContrastColorUtil.satisfiesTextContrast(this.mCachedContrastBackgroundColor, i)) {
            float[] fArr = new float[3];
            ColorUtils.colorToHSL(this.mDrawableColor, fArr);
            if (fArr[1] < 0.2f) {
                i = 0;
            }
            i = ContrastColorUtil.resolveContrastColor(((ImageView) this).mContext, i, this.mCachedContrastBackgroundColor, !ContrastColorUtil.isColorLight(this.mCachedContrastBackgroundColor));
        }
        this.mContrastedDrawableColor = i;
    }

    @Override // com.android.systemui.statusbar.StatusIconDisplayable
    public void setVisibleState(int i) {
        setVisibleState(i, true, null);
    }

    @Override // com.android.systemui.statusbar.StatusIconDisplayable
    public void setVisibleState(int i, boolean z) {
        setVisibleState(i, z, null);
    }

    public void setVisibleState(int i, boolean z, Runnable runnable) {
        setVisibleState(i, z, runnable, 0);
    }

    public void setVisibleState(int i, boolean z, final Runnable runnable, long j) {
        float f;
        boolean z2;
        boolean z3 = false;
        if (i != this.mVisibleState) {
            this.mVisibleState = i;
            ObjectAnimator objectAnimator = this.mIconAppearAnimator;
            if (objectAnimator != null) {
                objectAnimator.cancel();
            }
            ObjectAnimator objectAnimator2 = this.mDotAnimator;
            if (objectAnimator2 != null) {
                objectAnimator2.cancel();
            }
            if (z) {
                Interpolator interpolator = Interpolators.FAST_OUT_LINEAR_IN;
                if (i == 0) {
                    interpolator = Interpolators.LINEAR_OUT_SLOW_IN;
                    f = 1.0f;
                } else {
                    f = 0.0f;
                }
                float iconAppearAmount = getIconAppearAmount();
                long j2 = 100;
                if (f != iconAppearAmount) {
                    ObjectAnimator ofFloat = ObjectAnimator.ofFloat(this, ICON_APPEAR_AMOUNT, iconAppearAmount, f);
                    this.mIconAppearAnimator = ofFloat;
                    ofFloat.setInterpolator(interpolator);
                    this.mIconAppearAnimator.setDuration(j == 0 ? 100 : j);
                    this.mIconAppearAnimator.addListener(new AnimatorListenerAdapter() {
                        /* class com.android.systemui.statusbar.StatusBarIconView.AnonymousClass4 */

                        public void onAnimationEnd(Animator animator) {
                            StatusBarIconView.this.mIconAppearAnimator = null;
                            StatusBarIconView.this.runRunnable(runnable);
                        }
                    });
                    this.mIconAppearAnimator.start();
                    z2 = true;
                } else {
                    z2 = false;
                }
                float f2 = i == 0 ? 2.0f : 0.0f;
                Interpolator interpolator2 = Interpolators.FAST_OUT_LINEAR_IN;
                if (i == 1) {
                    interpolator2 = Interpolators.LINEAR_OUT_SLOW_IN;
                    f2 = 1.0f;
                }
                float dotAppearAmount = getDotAppearAmount();
                if (f2 != dotAppearAmount) {
                    ObjectAnimator ofFloat2 = ObjectAnimator.ofFloat(this, DOT_APPEAR_AMOUNT, dotAppearAmount, f2);
                    this.mDotAnimator = ofFloat2;
                    ofFloat2.setInterpolator(interpolator2);
                    ObjectAnimator objectAnimator3 = this.mDotAnimator;
                    if (j != 0) {
                        j2 = j;
                    }
                    objectAnimator3.setDuration(j2);
                    final boolean z4 = !z2;
                    this.mDotAnimator.addListener(new AnimatorListenerAdapter() {
                        /* class com.android.systemui.statusbar.StatusBarIconView.AnonymousClass5 */

                        public void onAnimationEnd(Animator animator) {
                            StatusBarIconView.this.mDotAnimator = null;
                            if (z4) {
                                StatusBarIconView.this.runRunnable(runnable);
                            }
                        }
                    });
                    this.mDotAnimator.start();
                    z3 = true;
                } else {
                    z3 = z2;
                }
            } else {
                setIconAppearAmount(i == 0 ? 1.0f : 0.0f);
                setDotAppearAmount(i == 1 ? 1.0f : i == 0 ? 2.0f : 0.0f);
            }
        }
        if (!z3) {
            runRunnable(runnable);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void runRunnable(Runnable runnable) {
        if (runnable != null) {
            runnable.run();
        }
    }

    public void setIconAppearAmount(float f) {
        if (this.mIconAppearAmount != f) {
            this.mIconAppearAmount = f;
            invalidate();
        }
    }

    public float getIconAppearAmount() {
        return this.mIconAppearAmount;
    }

    @Override // com.android.systemui.statusbar.StatusIconDisplayable
    public int getVisibleState() {
        return this.mVisibleState;
    }

    public void setDotAppearAmount(float f) {
        if (this.mDotAppearAmount != f) {
            this.mDotAppearAmount = f;
            invalidate();
        }
    }

    public void setVisibility(int i) {
        super.setVisibility(i);
        OnVisibilityChangedListener onVisibilityChangedListener = this.mOnVisibilityChangedListener;
        if (onVisibilityChangedListener != null) {
            onVisibilityChangedListener.onVisibilityChanged(i);
        }
    }

    public float getDotAppearAmount() {
        return this.mDotAppearAmount;
    }

    public void setOnVisibilityChangedListener(OnVisibilityChangedListener onVisibilityChangedListener) {
        this.mOnVisibilityChangedListener = onVisibilityChangedListener;
    }

    public void setDozing(boolean z, boolean z2, long j) {
        this.mDozer.setDozing(new Consumer() {
            /* class com.android.systemui.statusbar.$$Lambda$StatusBarIconView$x3AGEt5vRmEDqrCK9ien5Lp2M */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                StatusBarIconView.this.lambda$setDozing$1$StatusBarIconView((Float) obj);
            }
        }, z, z2, j, this);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$setDozing$1 */
    public /* synthetic */ void lambda$setDozing$1$StatusBarIconView(Float f) {
        this.mDozeAmount = f.floatValue();
        updateDecorColor();
        updateIconColor();
        updateAllowAnimation();
    }

    private void updateAllowAnimation() {
        float f = this.mDozeAmount;
        if (f == 0.0f || f == 1.0f) {
            setAllowAnimation(this.mDozeAmount == 0.0f);
        }
    }

    public void getDrawingRect(Rect rect) {
        super.getDrawingRect(rect);
        float translationX = getTranslationX();
        float translationY = getTranslationY();
        rect.left = (int) (((float) rect.left) + translationX);
        rect.right = (int) (((float) rect.right) + translationX);
        rect.top = (int) (((float) rect.top) + translationY);
        rect.bottom = (int) (((float) rect.bottom) + translationY);
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        Runnable runnable = this.mLayoutRunnable;
        if (runnable != null) {
            runnable.run();
            this.mLayoutRunnable = null;
        }
        updatePivot();
    }

    private void updatePivot() {
        if (isLayoutRtl()) {
            setPivotX(((this.mIconScale + 1.0f) / 2.0f) * ((float) getWidth()));
        } else {
            setPivotX(((1.0f - this.mIconScale) / 2.0f) * ((float) getWidth()));
        }
        setPivotY((((float) getHeight()) - (this.mIconScale * ((float) getWidth()))) / 2.0f);
    }

    public void executeOnLayout(Runnable runnable) {
        this.mLayoutRunnable = runnable;
    }

    public void setDismissed() {
        Runnable runnable = this.mOnDismissListener;
        if (runnable != null) {
            runnable.run();
        }
    }

    public void setOnDismissListener(Runnable runnable) {
        this.mOnDismissListener = runnable;
    }

    /* JADX WARNING: Removed duplicated region for block: B:22:0x0038  */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0045  */
    /* JADX WARNING: Removed duplicated region for block: B:27:? A[RETURN, SYNTHETIC] */
    @Override // com.android.systemui.plugins.DarkIconDispatcher.DarkReceiver
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onDarkChanged(android.graphics.Rect r2, float r3, int r4, int r5, int r6, boolean r7) {
        /*
            r1 = this;
            java.lang.String r5 = r1.mSlot
            com.android.internal.statusbar.StatusBarIcon r6 = r1.mIcon
            boolean r5 = com.android.systemui.statusbar.MiuiStatusBarIconViewHelper.canUseTint(r5, r6)
            r6 = 0
            r0 = 1
            if (r5 == 0) goto L_0x0010
            if (r7 == 0) goto L_0x0010
            r5 = r0
            goto L_0x0011
        L_0x0010:
            r5 = r6
        L_0x0011:
            boolean r7 = r1.mUseTint
            if (r7 == r5) goto L_0x001f
            r1.mUseTint = r5
            if (r5 != 0) goto L_0x001d
            r5 = 0
            r1.setImageTintList(r5)
        L_0x001d:
            r5 = r0
            goto L_0x0020
        L_0x001f:
            r5 = r6
        L_0x0020:
            boolean r7 = r1.mUseTint
            if (r7 != 0) goto L_0x0035
            float r3 = com.android.systemui.plugins.DarkIconDispatcher.getDarkIntensity(r2, r1, r3)
            r7 = 0
            int r3 = (r3 > r7 ? 1 : (r3 == r7 ? 0 : -1))
            if (r3 <= 0) goto L_0x002e
            r6 = r0
        L_0x002e:
            boolean r3 = r1.mDark
            if (r6 == r3) goto L_0x0035
            r1.mDark = r6
            goto L_0x0036
        L_0x0035:
            r0 = r5
        L_0x0036:
            if (r0 == 0) goto L_0x0041
            com.android.internal.statusbar.StatusBarIcon r3 = r1.mIcon
            com.android.internal.statusbar.StatusBarIcon r3 = r3.clone()
            r1.set(r3)
        L_0x0041:
            boolean r3 = r1.mUseTint
            if (r3 == 0) goto L_0x0053
            int r2 = com.android.systemui.plugins.DarkIconDispatcher.getTint(r2, r1, r4)
            android.content.res.ColorStateList r3 = android.content.res.ColorStateList.valueOf(r2)
            r1.setImageTintList(r3)
            r1.setDecorColor(r2)
        L_0x0053:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.StatusBarIconView.onDarkChanged(android.graphics.Rect, float, int, int, int, boolean):void");
    }

    @Override // com.android.systemui.statusbar.StatusIconDisplayable
    public boolean isIconVisible() {
        StatusBarIcon statusBarIcon = this.mIcon;
        return statusBarIcon != null && statusBarIcon.visible;
    }

    @Override // com.android.systemui.statusbar.StatusIconDisplayable
    public boolean isIconBlocked() {
        return this.mBlocked;
    }

    public void setIncreasedSize(boolean z) {
        this.mIncreasedSize = z;
        maybeUpdateIconScaleDimens();
    }

    public void setShowsConversation(boolean z) {
        if (this.mShowsConversation != z) {
            this.mShowsConversation = z;
            updateIconColor();
        }
    }

    public boolean showsConversation() {
        return this.mShowsConversation;
    }
}
