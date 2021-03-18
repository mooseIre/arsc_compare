package com.android.keyguard;

import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.PendingIntent;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Trace;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.animation.Animation;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.slice.Slice;
import androidx.slice.SliceItem;
import androidx.slice.SliceViewManager;
import androidx.slice.core.SliceQuery;
import androidx.slice.widget.ListContent;
import androidx.slice.widget.RowContent;
import androidx.slice.widget.SliceContent;
import androidx.slice.widget.SliceLiveData;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.graphics.ColorUtils;
import com.android.settingslib.Utils;
import com.android.systemui.C0009R$attr;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0022R$style;
import com.android.systemui.Dependency;
import com.android.systemui.Interpolators;
import com.android.systemui.keyguard.KeyguardSliceProvider;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.tuner.TunerService;
import com.android.systemui.util.wakelock.KeepAwakeAnimationListener;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

public class KeyguardSliceView extends LinearLayout implements View.OnClickListener, Observer<Slice>, TunerService.Tunable, ConfigurationController.ConfigurationListener {
    private final ActivityStarter mActivityStarter;
    private final HashMap<View, PendingIntent> mClickActions;
    private final ConfigurationController mConfigurationController;
    private Runnable mContentChangeListener;
    private float mDarkAmount = 0.0f;
    private int mDisplayId = -1;
    private boolean mHasHeader;
    private int mIconSize;
    private int mIconSizeWithHeader;
    private Uri mKeyguardSliceUri;
    private final LayoutTransition mLayoutTransition;
    private LiveData<Slice> mLiveData;
    private Row mRow;
    private final int mRowPadding;
    private float mRowTextSize;
    private final int mRowWithHeaderPadding;
    private float mRowWithHeaderTextSize;
    private Slice mSlice;
    private int mTextColor;
    @VisibleForTesting
    TextView mTitle;
    private final TunerService mTunerService;

    public KeyguardSliceView(Context context, AttributeSet attributeSet, ActivityStarter activityStarter, ConfigurationController configurationController, TunerService tunerService, Resources resources) {
        super(context, attributeSet);
        this.mTunerService = tunerService;
        this.mClickActions = new HashMap<>();
        this.mRowPadding = resources.getDimensionPixelSize(C0012R$dimen.subtitle_clock_padding);
        this.mRowWithHeaderPadding = resources.getDimensionPixelSize(C0012R$dimen.header_subtitle_padding);
        this.mActivityStarter = activityStarter;
        this.mConfigurationController = configurationController;
        LayoutTransition layoutTransition = new LayoutTransition();
        this.mLayoutTransition = layoutTransition;
        layoutTransition.setStagger(0, 275);
        this.mLayoutTransition.setDuration(2, 550);
        this.mLayoutTransition.setDuration(3, 275);
        this.mLayoutTransition.disableTransitionType(0);
        this.mLayoutTransition.disableTransitionType(1);
        this.mLayoutTransition.setInterpolator(2, Interpolators.FAST_OUT_SLOW_IN);
        this.mLayoutTransition.setInterpolator(3, Interpolators.ALPHA_OUT);
        this.mLayoutTransition.setAnimateParentHierarchy(false);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mTitle = (TextView) findViewById(C0015R$id.title);
        this.mRow = (Row) findViewById(C0015R$id.row);
        this.mTextColor = Utils.getColorAttrDefaultColor(((LinearLayout) this).mContext, C0009R$attr.wallpaperTextColor);
        this.mIconSize = (int) ((LinearLayout) this).mContext.getResources().getDimension(C0012R$dimen.widget_icon_size);
        this.mIconSizeWithHeader = (int) ((LinearLayout) this).mContext.getResources().getDimension(C0012R$dimen.header_icon_size);
        this.mRowTextSize = (float) ((LinearLayout) this).mContext.getResources().getDimensionPixelSize(C0012R$dimen.widget_label_font_size);
        this.mRowWithHeaderTextSize = (float) ((LinearLayout) this).mContext.getResources().getDimensionPixelSize(C0012R$dimen.header_row_font_size);
        this.mTitle.setOnClickListener(this);
        this.mTitle.setBreakStrategy(2);
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Display display = getDisplay();
        if (display != null) {
            this.mDisplayId = display.getDisplayId();
        }
        this.mTunerService.addTunable(this, "keyguard_slice_uri");
        if (this.mDisplayId == 0) {
            this.mLiveData.observeForever(this);
        }
        this.mConfigurationController.addCallback(this);
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mDisplayId == 0) {
            this.mLiveData.removeObserver(this);
        }
        this.mTunerService.removeTunable(this);
        this.mConfigurationController.removeCallback(this);
    }

    public void onVisibilityAggregated(boolean z) {
        super.onVisibilityAggregated(z);
        setLayoutTransition(z ? this.mLayoutTransition : null);
    }

    public boolean hasHeader() {
        return this.mHasHeader;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r7v0, resolved type: boolean */
    /* JADX WARN: Multi-variable type inference failed */
    private void showSlice() {
        Drawable drawable;
        Trace.beginSection("KeyguardSliceView#showSlice");
        int i = 8;
        int i2 = 0;
        if (this.mSlice == null) {
            this.mTitle.setVisibility(8);
            this.mRow.setVisibility(8);
            this.mHasHeader = false;
            Runnable runnable = this.mContentChangeListener;
            if (runnable != null) {
                runnable.run();
            }
            Trace.endSection();
            return;
        }
        this.mClickActions.clear();
        ListContent listContent = new ListContent(getContext(), this.mSlice);
        RowContent header = listContent.getHeader();
        this.mHasHeader = header != null && !header.getSliceItem().hasHint("list_item");
        ArrayList arrayList = new ArrayList();
        for (int i3 = 0; i3 < listContent.getRowItems().size(); i3++) {
            SliceContent sliceContent = listContent.getRowItems().get(i3);
            if (!"content://com.android.systemui.keyguard/action".equals(sliceContent.getSliceItem().getSlice().getUri().toString())) {
                arrayList.add(sliceContent);
            }
        }
        if (!this.mHasHeader) {
            this.mTitle.setVisibility(8);
        } else {
            this.mTitle.setVisibility(0);
            RowContent header2 = listContent.getHeader();
            SliceItem titleItem = header2.getTitleItem();
            this.mTitle.setText(titleItem != null ? titleItem.getText() : null);
            if (!(header2.getPrimaryAction() == null || header2.getPrimaryAction().getAction() == null)) {
                this.mClickActions.put(this.mTitle, header2.getPrimaryAction().getAction());
            }
        }
        int size = arrayList.size();
        int textColor = getTextColor();
        boolean z = this.mHasHeader;
        Row row = this.mRow;
        if (size > 0) {
            i = 0;
        }
        row.setVisibility(i);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) this.mRow.getLayoutParams();
        layoutParams.topMargin = this.mHasHeader ? this.mRowWithHeaderPadding : this.mRowPadding;
        this.mRow.setLayoutParams(layoutParams);
        int i4 = z;
        while (i4 < size) {
            RowContent rowContent = (RowContent) arrayList.get(i4 == 1 ? 1 : 0);
            SliceItem sliceItem = rowContent.getSliceItem();
            Uri uri = sliceItem.getSlice().getUri();
            KeyguardSliceTextView keyguardSliceTextView = (KeyguardSliceTextView) this.mRow.findViewWithTag(uri);
            if (keyguardSliceTextView == null) {
                keyguardSliceTextView = new KeyguardSliceTextView(((LinearLayout) this).mContext);
                keyguardSliceTextView.setTextColor(textColor);
                keyguardSliceTextView.setTag(uri);
                this.mRow.addView(keyguardSliceTextView, i4 - (this.mHasHeader ? 1 : 0));
            }
            PendingIntent action = rowContent.getPrimaryAction() != null ? rowContent.getPrimaryAction().getAction() : null;
            this.mClickActions.put(keyguardSliceTextView, action);
            SliceItem titleItem2 = rowContent.getTitleItem();
            keyguardSliceTextView.setText(titleItem2 == null ? null : titleItem2.getText());
            keyguardSliceTextView.setContentDescription(rowContent.getContentDescription());
            keyguardSliceTextView.setTextSize(0, this.mHasHeader ? this.mRowWithHeaderTextSize : this.mRowTextSize);
            SliceItem find = SliceQuery.find(sliceItem.getSlice(), "image");
            if (find != null) {
                int i5 = this.mHasHeader ? this.mIconSizeWithHeader : this.mIconSize;
                drawable = find.getIcon().loadDrawable(((LinearLayout) this).mContext);
                if (drawable != null) {
                    drawable.setBounds(0, 0, Math.max((int) ((((float) drawable.getIntrinsicWidth()) / ((float) drawable.getIntrinsicHeight())) * ((float) i5)), 1), i5);
                }
            } else {
                drawable = null;
            }
            keyguardSliceTextView.setCompoundDrawables(drawable, null, null, null);
            keyguardSliceTextView.setOnClickListener(this);
            keyguardSliceTextView.setClickable(action != null);
            i4++;
        }
        while (i2 < this.mRow.getChildCount()) {
            View childAt = this.mRow.getChildAt(i2);
            if (!this.mClickActions.containsKey(childAt)) {
                this.mRow.removeView(childAt);
                i2--;
            }
            i2++;
        }
        Runnable runnable2 = this.mContentChangeListener;
        if (runnable2 != null) {
            runnable2.run();
        }
        Trace.endSection();
    }

    public void setDarkAmount(float f) {
        this.mDarkAmount = f;
        this.mRow.setDarkAmount(f);
        updateTextColors();
    }

    private void updateTextColors() {
        int textColor = getTextColor();
        this.mTitle.setTextColor(textColor);
        int childCount = this.mRow.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = this.mRow.getChildAt(i);
            if (childAt instanceof TextView) {
                ((TextView) childAt).setTextColor(textColor);
            }
        }
    }

    public void onClick(View view) {
        PendingIntent pendingIntent = this.mClickActions.get(view);
        if (pendingIntent != null) {
            this.mActivityStarter.startPendingIntentDismissingKeyguard(pendingIntent);
        }
    }

    public void setContentChangeListener(Runnable runnable) {
        this.mContentChangeListener = runnable;
    }

    public void onChanged(Slice slice) {
        this.mSlice = slice;
        showSlice();
    }

    @Override // com.android.systemui.tuner.TunerService.Tunable
    public void onTuningChanged(String str, String str2) {
        setupUri(str2);
    }

    public void setupUri(String str) {
        if (str == null) {
            str = "content://com.android.systemui.keyguard/main";
        }
        boolean z = false;
        LiveData<Slice> liveData = this.mLiveData;
        if (liveData != null && liveData.hasActiveObservers()) {
            z = true;
            this.mLiveData.removeObserver(this);
        }
        Uri parse = Uri.parse(str);
        this.mKeyguardSliceUri = parse;
        LiveData<Slice> fromUri = SliceLiveData.fromUri(((LinearLayout) this).mContext, parse);
        this.mLiveData = fromUri;
        if (z) {
            fromUri.observeForever(this);
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public int getTextColor() {
        return ColorUtils.blendARGB(this.mTextColor, -1, this.mDarkAmount);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void setTextColor(int i) {
        this.mTextColor = i;
        updateTextColors();
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onDensityOrFontScaleChanged() {
        this.mIconSize = ((LinearLayout) this).mContext.getResources().getDimensionPixelSize(C0012R$dimen.widget_icon_size);
        this.mIconSizeWithHeader = (int) ((LinearLayout) this).mContext.getResources().getDimension(C0012R$dimen.header_icon_size);
        this.mRowTextSize = (float) ((LinearLayout) this).mContext.getResources().getDimensionPixelSize(C0012R$dimen.widget_label_font_size);
        this.mRowWithHeaderTextSize = (float) ((LinearLayout) this).mContext.getResources().getDimensionPixelSize(C0012R$dimen.header_row_font_size);
    }

    public void refresh() {
        Slice slice;
        Trace.beginSection("KeyguardSliceView#refresh");
        if ("content://com.android.systemui.keyguard/main".equals(this.mKeyguardSliceUri.toString())) {
            KeyguardSliceProvider attachedInstance = KeyguardSliceProvider.getAttachedInstance();
            if (attachedInstance != null) {
                slice = attachedInstance.onBindSlice(this.mKeyguardSliceUri);
            } else {
                Log.w("KeyguardSliceView", "Keyguard slice not bound yet?");
                slice = null;
            }
        } else {
            slice = SliceViewManager.getInstance(getContext()).bindSlice(this.mKeyguardSliceUri);
        }
        onChanged(slice);
        Trace.endSection();
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        Object obj;
        printWriter.println("KeyguardSliceView:");
        printWriter.println("  mClickActions: " + this.mClickActions);
        StringBuilder sb = new StringBuilder();
        sb.append("  mTitle: ");
        TextView textView = this.mTitle;
        boolean z = true;
        Object obj2 = "null";
        if (textView == null) {
            obj = obj2;
        } else {
            obj = Boolean.valueOf(textView.getVisibility() == 0);
        }
        sb.append(obj);
        printWriter.println(sb.toString());
        StringBuilder sb2 = new StringBuilder();
        sb2.append("  mRow: ");
        Row row = this.mRow;
        if (row != null) {
            if (row.getVisibility() != 0) {
                z = false;
            }
            obj2 = Boolean.valueOf(z);
        }
        sb2.append(obj2);
        printWriter.println(sb2.toString());
        printWriter.println("  mTextColor: " + Integer.toHexString(this.mTextColor));
        printWriter.println("  mDarkAmount: " + this.mDarkAmount);
        printWriter.println("  mSlice: " + this.mSlice);
        printWriter.println("  mHasHeader: " + this.mHasHeader);
    }

    public static class Row extends LinearLayout {
        private float mDarkAmount;
        private final Animation.AnimationListener mKeepAwakeListener;
        private LayoutTransition mLayoutTransition;

        public boolean hasOverlappingRendering() {
            return false;
        }

        public Row(Context context) {
            this(context, null);
        }

        public Row(Context context, AttributeSet attributeSet) {
            this(context, attributeSet, 0);
        }

        public Row(Context context, AttributeSet attributeSet, int i) {
            this(context, attributeSet, i, 0);
        }

        public Row(Context context, AttributeSet attributeSet, int i, int i2) {
            super(context, attributeSet, i, i2);
            this.mKeepAwakeListener = new KeepAwakeAnimationListener(((LinearLayout) this).mContext);
        }

        /* access modifiers changed from: protected */
        public void onFinishInflate() {
            LayoutTransition layoutTransition = new LayoutTransition();
            this.mLayoutTransition = layoutTransition;
            layoutTransition.setDuration(550);
            ObjectAnimator ofPropertyValuesHolder = ObjectAnimator.ofPropertyValuesHolder(null, PropertyValuesHolder.ofInt("left", 0, 1), PropertyValuesHolder.ofInt("right", 0, 1));
            this.mLayoutTransition.setAnimator(0, ofPropertyValuesHolder);
            this.mLayoutTransition.setAnimator(1, ofPropertyValuesHolder);
            this.mLayoutTransition.setInterpolator(0, Interpolators.ACCELERATE_DECELERATE);
            this.mLayoutTransition.setInterpolator(1, Interpolators.ACCELERATE_DECELERATE);
            this.mLayoutTransition.setStartDelay(0, 550);
            this.mLayoutTransition.setStartDelay(1, 550);
            this.mLayoutTransition.setAnimator(2, ObjectAnimator.ofFloat((Object) null, "alpha", 0.0f, 1.0f));
            this.mLayoutTransition.setInterpolator(2, Interpolators.ALPHA_IN);
            ObjectAnimator ofFloat = ObjectAnimator.ofFloat((Object) null, "alpha", 1.0f, 0.0f);
            this.mLayoutTransition.setInterpolator(3, Interpolators.ALPHA_OUT);
            this.mLayoutTransition.setDuration(3, 137);
            this.mLayoutTransition.setAnimator(3, ofFloat);
            this.mLayoutTransition.setAnimateParentHierarchy(false);
        }

        public void onVisibilityAggregated(boolean z) {
            super.onVisibilityAggregated(z);
            setLayoutTransition(z ? this.mLayoutTransition : null);
        }

        /* access modifiers changed from: protected */
        public void onMeasure(int i, int i2) {
            int size = View.MeasureSpec.getSize(i);
            int childCount = getChildCount();
            for (int i3 = 0; i3 < childCount; i3++) {
                View childAt = getChildAt(i3);
                if (childAt instanceof KeyguardSliceTextView) {
                    ((KeyguardSliceTextView) childAt).setMaxWidth(size / 3);
                }
            }
            super.onMeasure(i, i2);
        }

        public void setDarkAmount(float f) {
            Animation.AnimationListener animationListener;
            boolean z = true;
            boolean z2 = f != 0.0f;
            if (this.mDarkAmount == 0.0f) {
                z = false;
            }
            if (z2 != z) {
                this.mDarkAmount = f;
                if (z2) {
                    animationListener = null;
                } else {
                    animationListener = this.mKeepAwakeListener;
                }
                setLayoutAnimationListener(animationListener);
            }
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public static class KeyguardSliceTextView extends TextView implements ConfigurationController.ConfigurationListener {
        private static int sStyleId = C0022R$style.TextAppearance_Keyguard_Secondary;

        KeyguardSliceTextView(Context context) {
            super(context, null, 0, sStyleId);
            onDensityOrFontScaleChanged();
            setEllipsize(TextUtils.TruncateAt.END);
        }

        /* access modifiers changed from: protected */
        public void onAttachedToWindow() {
            super.onAttachedToWindow();
            ((ConfigurationController) Dependency.get(ConfigurationController.class)).addCallback(this);
        }

        /* access modifiers changed from: protected */
        public void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            ((ConfigurationController) Dependency.get(ConfigurationController.class)).removeCallback(this);
        }

        @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
        public void onDensityOrFontScaleChanged() {
            updatePadding();
        }

        @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
        public void onOverlayChanged() {
            setTextAppearance(sStyleId);
        }

        @Override // android.widget.TextView
        public void setText(CharSequence charSequence, TextView.BufferType bufferType) {
            super.setText(charSequence, bufferType);
            updatePadding();
        }

        private void updatePadding() {
            int i = 1;
            boolean z = !TextUtils.isEmpty(getText());
            int dimension = ((int) getContext().getResources().getDimension(C0012R$dimen.widget_horizontal_padding)) / 2;
            if (!z) {
                i = -1;
            }
            setPadding(dimension, 0, i * dimension, 0);
            setCompoundDrawablePadding((int) ((TextView) this).mContext.getResources().getDimension(C0012R$dimen.widget_icon_padding));
        }

        @Override // android.widget.TextView
        public void setTextColor(int i) {
            super.setTextColor(i);
            updateDrawableColors();
        }

        public void setCompoundDrawables(Drawable drawable, Drawable drawable2, Drawable drawable3, Drawable drawable4) {
            super.setCompoundDrawables(drawable, drawable2, drawable3, drawable4);
            updateDrawableColors();
            updatePadding();
        }

        private void updateDrawableColors() {
            int currentTextColor = getCurrentTextColor();
            Drawable[] compoundDrawables = getCompoundDrawables();
            for (Drawable drawable : compoundDrawables) {
                if (drawable != null) {
                    drawable.setTint(currentTextColor);
                }
            }
        }
    }
}
