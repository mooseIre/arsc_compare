package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.ArraySet;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.android.internal.statusbar.StatusBarIcon;
import com.android.systemui.Dependency;
import com.android.systemui.Util;
import com.android.systemui.miui.statusbar.ExpandedNotification;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.Icons;
import com.android.systemui.statusbar.StatusBarIconView;
import com.android.systemui.statusbar.policy.DarkIconDispatcher;
import com.android.systemui.statusbar.policy.DarkIconDispatcherHelper;
import com.android.systemui.util.DisableStateTracker;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

public interface StatusBarIconController {
    void addIconGroup(IconManager iconManager);

    void dispatchDemoCommand(String str, Bundle bundle);

    void removeIcon(String str);

    void removeIconGroup(IconManager iconManager);

    void setExternalIcon(String str);

    void setIcon(String str, int i, CharSequence charSequence);

    void setIcon(String str, StatusBarIcon statusBarIcon);

    void setIconVisibility(String str, boolean z);

    public static class OrderedIconManager extends DarkIconManager {
        private HashSet<String> mCurrentSlots;
        private ArrayList<String> mSlots;

        public OrderedIconManager(LinearLayout linearLayout, ArrayList<String> arrayList) {
            this(linearLayout, arrayList, false);
        }

        public OrderedIconManager(LinearLayout linearLayout, ArrayList<String> arrayList, boolean z) {
            super(linearLayout, z);
            this.mSlots = arrayList;
            this.mCurrentSlots = new HashSet<>();
        }

        /* access modifiers changed from: protected */
        public void onIconAdded(int i, String str, boolean z, StatusBarIcon statusBarIcon) {
            if (this.mSlots.contains(str)) {
                this.mCurrentSlots.add(str);
                int realViewIndex = getRealViewIndex(str);
                super.onIconAdded(realViewIndex, str, z, statusBarIcon);
                if (realViewIndex == -1) {
                    applyDark(getRealViewIndex(str));
                }
            }
        }

        public void onSetIcon(int i, String str, StatusBarIcon statusBarIcon) {
            if (this.mCurrentSlots.contains(str)) {
                super.onSetIcon(getRealViewIndex(str), str, statusBarIcon);
            }
        }

        /* access modifiers changed from: protected */
        public void onRemoveIcon(int i, String str) {
            if (this.mCurrentSlots.contains(str)) {
                super.onRemoveIcon(getRealViewIndex(str), str);
                this.mCurrentSlots.remove(str);
            }
        }

        private int getRealViewIndex(String str) {
            Iterator<String> it = this.mSlots.iterator();
            int i = 0;
            while (it.hasNext()) {
                String next = it.next();
                if (this.mCurrentSlots.contains(next)) {
                    if (str.equals(next)) {
                        break;
                    }
                    i++;
                }
            }
            if (i >= this.mGroup.getChildCount()) {
                return -1;
            }
            return i;
        }
    }

    public static class DarkIconManager extends IconManager {
        /* access modifiers changed from: private */
        public static int sFilterColor;
        /* access modifiers changed from: private */
        public final DarkIconDispatcher mDarkIconDispatcher;
        /* access modifiers changed from: private */
        public float mDarkIntensity;
        private DarkIconDispatcher.DarkReceiver mDarkReceiver;
        private int mIconHPadding;
        /* access modifiers changed from: private */
        public Rect mTintArea;
        /* access modifiers changed from: private */
        public int mTintColor;

        public DarkIconManager(LinearLayout linearLayout) {
            this(linearLayout, false);
        }

        public DarkIconManager(LinearLayout linearLayout, boolean z) {
            super(linearLayout);
            this.mTintArea = new Rect();
            this.mTintColor = this.mContext.getColor(R.color.light_mode_icon_color_single_tone);
            this.mIconHPadding = this.mContext.getResources().getDimensionPixelSize(R.dimen.status_bar_icon_padding);
            this.mDarkIconDispatcher = (DarkIconDispatcher) Dependency.get(DarkIconDispatcher.class);
            if (!z) {
                this.mDarkReceiver = new DarkIconDispatcher.DarkReceiver() {
                    public void onDarkChanged(Rect rect, float f, int i) {
                        float unused = DarkIconManager.this.mDarkIntensity = f;
                        int unused2 = DarkIconManager.this.mTintColor = i;
                        DarkIconManager.this.mTintArea.set(rect);
                        ViewGroup viewGroup = DarkIconManager.this.mGroup;
                        if (viewGroup != null && viewGroup.getChildCount() != 0) {
                            for (int i2 = 0; i2 < DarkIconManager.this.mGroup.getChildCount(); i2++) {
                                if (DarkIconManager.this.mGroup.getChildAt(i2) instanceof StatusBarIconView) {
                                    StatusBarIconView statusBarIconView = (StatusBarIconView) DarkIconManager.this.mGroup.getChildAt(i2);
                                    boolean z = "bluetooth_handsfree_battery".equals(statusBarIconView.getSlot()) && statusBarIconView.getStatusBarIcon().iconLevel <= 2;
                                    statusBarIconView.setImageTintMode(PorterDuff.Mode.SRC_IN);
                                    if (Util.showCtsSpecifiedColor() || !DarkIconManager.this.mDarkIconDispatcher.useTint() || z) {
                                        boolean inDarkMode = DarkIconDispatcherHelper.inDarkMode(DarkIconManager.this.mTintArea, statusBarIconView, DarkIconManager.this.mDarkIntensity);
                                        statusBarIconView.setImageResource(Icons.get(Integer.valueOf(statusBarIconView.getStatusBarIcon().icon.getResId()), inDarkMode));
                                        if (!inDarkMode || !Util.showCtsSpecifiedColor()) {
                                            statusBarIconView.setImageTintList((ColorStateList) null);
                                        } else {
                                            if (DarkIconManager.sFilterColor == 0) {
                                                int unused3 = DarkIconManager.sFilterColor = DarkIconManager.this.mContext.getResources().getColor(R.color.status_bar_icon_text_color_dark_mode_cts);
                                            }
                                            statusBarIconView.setImageTintList(ColorStateList.valueOf(DarkIconManager.sFilterColor));
                                        }
                                    } else {
                                        statusBarIconView.setImageResource(Icons.get(Integer.valueOf(statusBarIconView.getStatusBarIcon().icon.getResId()), false));
                                        statusBarIconView.setImageTintList(ColorStateList.valueOf(DarkIconDispatcherHelper.getTint(rect, statusBarIconView, i)));
                                    }
                                }
                            }
                        }
                    }
                };
                this.mDarkIconDispatcher.addDarkReceiver(this.mDarkReceiver);
            }
        }

        /* access modifiers changed from: protected */
        public void onIconAdded(int i, String str, boolean z, StatusBarIcon statusBarIcon) {
            addIcon(i, str, z, statusBarIcon);
            applyDark(i);
        }

        /* access modifiers changed from: protected */
        public LinearLayout.LayoutParams onCreateLayoutParams(int i) {
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(i, this.mIconSize);
            int i2 = this.mIconHPadding;
            layoutParams.setMargins(i2, 0, i2, 0);
            return layoutParams;
        }

        public void destroy() {
            this.mGroup.removeAllViews();
            this.mDarkIconDispatcher.removeDarkReceiver(this.mDarkReceiver);
        }

        public void onSetIcon(int i, String str, StatusBarIcon statusBarIcon) {
            super.onSetIcon(i, str, statusBarIcon);
            applyDark(i);
        }

        public void setDarkIntensity(Rect rect, float f, int i) {
            this.mDarkIntensity = f;
            this.mTintArea.set(rect);
            this.mTintColor = i;
            int childCount = this.mGroup.getChildCount();
            for (int i2 = 0; i2 < childCount; i2++) {
                applyDark(i2);
            }
        }

        /* access modifiers changed from: protected */
        public void applyDark(int i) {
            if (i < this.mGroup.getChildCount() && (this.mGroup.getChildAt(i) instanceof StatusBarIconView)) {
                StatusBarIconView statusBarIconView = (StatusBarIconView) this.mGroup.getChildAt(i);
                statusBarIconView.setImageTintMode(PorterDuff.Mode.SRC_IN);
                if (this.mDarkIconDispatcher.useTint()) {
                    statusBarIconView.setImageTintList(ColorStateList.valueOf(DarkIconDispatcherHelper.getTint(this.mTintArea, statusBarIconView, this.mTintColor)));
                    return;
                }
                statusBarIconView.setImageTintList((ColorStateList) null);
                statusBarIconView.setImageResource(Icons.get(Integer.valueOf(statusBarIconView.getStatusBarIcon().icon.getResId()), DarkIconDispatcherHelper.inDarkMode(this.mTintArea, statusBarIconView, this.mDarkIntensity)));
            }
        }
    }

    public static class IconManager {
        protected final Context mContext;
        protected final ViewGroup mGroup;
        protected final int mIconSize = this.mContext.getResources().getDimensionPixelSize(17105481);
        public ArraySet<String> mWhiteList;

        public IconManager(ViewGroup viewGroup) {
            this.mGroup = viewGroup;
            this.mContext = viewGroup.getContext();
            if (this.mGroup.getTag(R.id.tag_disable_state_tracker) == null) {
                DisableStateTracker disableStateTracker = new DisableStateTracker(0, 2);
                this.mGroup.addOnAttachStateChangeListener(disableStateTracker);
                this.mGroup.setTag(R.id.tag_disable_state_tracker, disableStateTracker);
                if (this.mGroup.isAttachedToWindow()) {
                    disableStateTracker.onViewAttachedToWindow(this.mGroup);
                }
            }
        }

        /* access modifiers changed from: protected */
        public void onIconAdded(int i, String str, boolean z, StatusBarIcon statusBarIcon) {
            addIcon(i, str, z, statusBarIcon);
        }

        /* access modifiers changed from: protected */
        public StatusBarIconView addIcon(int i, String str, boolean z, StatusBarIcon statusBarIcon) {
            ArraySet<String> arraySet = this.mWhiteList;
            if (arraySet != null) {
                z = !arraySet.contains(str);
            }
            StatusBarIconView onCreateStatusBarIconView = onCreateStatusBarIconView(str, z);
            onCreateStatusBarIconView.set(statusBarIcon);
            this.mGroup.addView(onCreateStatusBarIconView, i, onCreateLayoutParams(getDrawableWidth(onCreateStatusBarIconView)));
            return onCreateStatusBarIconView;
        }

        /* access modifiers changed from: protected */
        public StatusBarIconView onCreateStatusBarIconView(String str, boolean z) {
            return new StatusBarIconView(this.mContext, str, (ExpandedNotification) null, z);
        }

        /* access modifiers changed from: protected */
        public LinearLayout.LayoutParams onCreateLayoutParams(int i) {
            return new LinearLayout.LayoutParams(i, this.mIconSize);
        }

        /* access modifiers changed from: protected */
        public void destroy() {
            this.mGroup.removeAllViews();
        }

        /* access modifiers changed from: protected */
        public void onIconExternal(int i, int i2) {
            ImageView imageView = (ImageView) this.mGroup.getChildAt(i);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setAdjustViewBounds(true);
            setHeightAndCenter(imageView, i2);
        }

        private void setHeightAndCenter(ImageView imageView, int i) {
            ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
            layoutParams.height = i;
            if (layoutParams instanceof LinearLayout.LayoutParams) {
                ((LinearLayout.LayoutParams) layoutParams).gravity = 16;
            }
            imageView.setLayoutParams(layoutParams);
        }

        /* access modifiers changed from: protected */
        public void onRemoveIcon(int i, String str) {
            this.mGroup.removeViewAt(i);
        }

        public void onSetIcon(int i, String str, StatusBarIcon statusBarIcon) {
            ((StatusBarIconView) this.mGroup.getChildAt(i)).set(statusBarIcon);
        }

        public boolean hasView(String str) {
            for (int i = 0; i < this.mGroup.getChildCount(); i++) {
                if (((StatusBarIconView) this.mGroup.getChildAt(i)).getSlot().equals(str)) {
                    return true;
                }
            }
            return false;
        }

        public int getDrawableWidth(StatusBarIconView statusBarIconView) {
            if (statusBarIconView == null || statusBarIconView.getDrawable() == null) {
                return -2;
            }
            return statusBarIconView.getDrawable().getIntrinsicWidth();
        }
    }
}
