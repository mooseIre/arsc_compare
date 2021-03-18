package com.android.systemui.qs;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import com.android.systemui.C0015R$id;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: QSHeaderInfoLayout.kt */
public final class QSHeaderInfoLayout extends FrameLayout {
    private View alarmContainer;
    private final Location location;
    private View ringerContainer;
    private View statusSeparator;

    public QSHeaderInfoLayout(@NotNull Context context) {
        this(context, null, 0, 0, 14, null);
    }

    public QSHeaderInfoLayout(@NotNull Context context, @Nullable AttributeSet attributeSet) {
        this(context, attributeSet, 0, 0, 12, null);
    }

    public QSHeaderInfoLayout(@NotNull Context context, @Nullable AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0, 8, null);
    }

    /* JADX INFO: this call moved to the top of the method (can break code semantics) */
    public /* synthetic */ QSHeaderInfoLayout(Context context, AttributeSet attributeSet, int i, int i2, int i3, DefaultConstructorMarker defaultConstructorMarker) {
        this(context, (i3 & 2) != 0 ? null : attributeSet, (i3 & 4) != 0 ? 0 : i, (i3 & 8) != 0 ? 0 : i2);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public QSHeaderInfoLayout(@NotNull Context context, @Nullable AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        Intrinsics.checkParameterIsNotNull(context, "context");
        this.location = new Location(0, 0);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        View findViewById = findViewById(C0015R$id.alarm_container);
        Intrinsics.checkExpressionValueIsNotNull(findViewById, "findViewById(R.id.alarm_container)");
        this.alarmContainer = findViewById;
        View findViewById2 = findViewById(C0015R$id.ringer_container);
        Intrinsics.checkExpressionValueIsNotNull(findViewById2, "findViewById(R.id.ringer_container)");
        this.ringerContainer = findViewById2;
        View findViewById3 = findViewById(C0015R$id.status_separator);
        Intrinsics.checkExpressionValueIsNotNull(findViewById3, "findViewById(R.id.status_separator)");
        this.statusSeparator = findViewById3;
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        View view = this.statusSeparator;
        if (view == null) {
            Intrinsics.throwUninitializedPropertyAccessException("statusSeparator");
            throw null;
        } else if (view.getVisibility() == 8) {
            super.onLayout(z, i, i2, i3, i4);
        } else {
            boolean isLayoutRtl = isLayoutRtl();
            int i5 = i3 - i;
            int i6 = i4 - i2;
            View view2 = this.alarmContainer;
            if (view2 != null) {
                int layoutView = 0 + layoutView(view2, i5, i6, 0, isLayoutRtl);
                View view3 = this.statusSeparator;
                if (view3 != null) {
                    int layoutView2 = layoutView(view3, i5, i6, layoutView, isLayoutRtl) + layoutView;
                    View view4 = this.ringerContainer;
                    if (view4 != null) {
                        layoutView(view4, i5, i6, layoutView2, isLayoutRtl);
                    } else {
                        Intrinsics.throwUninitializedPropertyAccessException("ringerContainer");
                        throw null;
                    }
                } else {
                    Intrinsics.throwUninitializedPropertyAccessException("statusSeparator");
                    throw null;
                }
            } else {
                Intrinsics.throwUninitializedPropertyAccessException("alarmContainer");
                throw null;
            }
        }
    }

    private final int layoutView(@NotNull View view, int i, int i2, int i3, boolean z) {
        this.location.setLocationFromOffset(i, i3, view.getMeasuredWidth(), z);
        view.layout(this.location.getLeft(), 0, this.location.getRight(), i2);
        return view.getMeasuredWidth();
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int i, int i2) {
        super.onMeasure(View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(i), Integer.MIN_VALUE), i2);
        int size = View.MeasureSpec.getSize(i);
        View view = this.statusSeparator;
        if (view != null) {
            if (view.getVisibility() != 8) {
                View view2 = this.alarmContainer;
                if (view2 != null) {
                    int measuredWidth = view2.getMeasuredWidth();
                    View view3 = this.statusSeparator;
                    if (view3 != null) {
                        int measuredWidth2 = view3.getMeasuredWidth();
                        View view4 = this.ringerContainer;
                        if (view4 != null) {
                            int measuredWidth3 = view4.getMeasuredWidth();
                            int size2 = View.MeasureSpec.getSize(size) - measuredWidth2;
                            int i3 = size2 / 2;
                            if (measuredWidth < i3) {
                                View view5 = this.ringerContainer;
                                if (view5 != null) {
                                    measureChild(view5, View.MeasureSpec.makeMeasureSpec(Math.min(measuredWidth3, size2 - measuredWidth), Integer.MIN_VALUE), i2);
                                } else {
                                    Intrinsics.throwUninitializedPropertyAccessException("ringerContainer");
                                    throw null;
                                }
                            } else if (measuredWidth3 < i3) {
                                View view6 = this.alarmContainer;
                                if (view6 != null) {
                                    measureChild(view6, View.MeasureSpec.makeMeasureSpec(Math.min(measuredWidth, size2 - measuredWidth3), Integer.MIN_VALUE), i2);
                                } else {
                                    Intrinsics.throwUninitializedPropertyAccessException("alarmContainer");
                                    throw null;
                                }
                            } else {
                                View view7 = this.alarmContainer;
                                if (view7 != null) {
                                    measureChild(view7, View.MeasureSpec.makeMeasureSpec(i3, Integer.MIN_VALUE), i2);
                                    View view8 = this.ringerContainer;
                                    if (view8 != null) {
                                        measureChild(view8, View.MeasureSpec.makeMeasureSpec(i3, Integer.MIN_VALUE), i2);
                                    } else {
                                        Intrinsics.throwUninitializedPropertyAccessException("ringerContainer");
                                        throw null;
                                    }
                                } else {
                                    Intrinsics.throwUninitializedPropertyAccessException("alarmContainer");
                                    throw null;
                                }
                            }
                        } else {
                            Intrinsics.throwUninitializedPropertyAccessException("ringerContainer");
                            throw null;
                        }
                    } else {
                        Intrinsics.throwUninitializedPropertyAccessException("statusSeparator");
                        throw null;
                    }
                } else {
                    Intrinsics.throwUninitializedPropertyAccessException("alarmContainer");
                    throw null;
                }
            }
            setMeasuredDimension(size, getMeasuredHeight());
            return;
        }
        Intrinsics.throwUninitializedPropertyAccessException("statusSeparator");
        throw null;
    }

    /* access modifiers changed from: private */
    /* compiled from: QSHeaderInfoLayout.kt */
    public static final class Location {
        private int left;
        private int right;

        public boolean equals(@Nullable Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Location)) {
                return false;
            }
            Location location = (Location) obj;
            return this.left == location.left && this.right == location.right;
        }

        public int hashCode() {
            return (Integer.hashCode(this.left) * 31) + Integer.hashCode(this.right);
        }

        @NotNull
        public String toString() {
            return "Location(left=" + this.left + ", right=" + this.right + ")";
        }

        public Location(int i, int i2) {
            this.left = i;
            this.right = i2;
        }

        public final int getLeft() {
            return this.left;
        }

        public final int getRight() {
            return this.right;
        }

        public final void setLocationFromOffset(int i, int i2, int i3, boolean z) {
            if (z) {
                int i4 = i - i2;
                this.left = i4 - i3;
                this.right = i4;
                return;
            }
            this.left = i2;
            this.right = i2 + i3;
        }
    }
}
