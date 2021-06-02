package com.android.systemui.qs.tileimpl;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Switch;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0021R$string;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.qs.QSIconView;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.plugins.qs.QSTileView;
import com.miui.systemui.util.HapticFeedBackImpl;
import miuix.animation.Folme;
import miuix.animation.ITouchStyle;
import miuix.animation.base.AnimConfig;

public class MiuiQSTileBaseView extends QSTileView {
    private String mAccessibilityClass;
    private boolean mClicked;
    private final H mHandler = new H(Looper.getMainLooper());
    protected QSIconView mIcon;
    private final FrameLayout mIconFrame;
    private ITouchStyle mIconMouseAnim;
    private boolean mTileState;

    /* access modifiers changed from: protected */
    public boolean animationsEnabled() {
        return true;
    }

    public boolean hasOverlappingRendering() {
        return false;
    }

    public MiuiQSTileBaseView(Context context, QSIconView qSIconView, boolean z) {
        super(context);
        FrameLayout frameLayout = new FrameLayout(context);
        this.mIconFrame = frameLayout;
        frameLayout.setForegroundGravity(17);
        int dimensionPixelSize = context.getResources().getDimensionPixelSize(C0012R$dimen.qs_tile_icon_bg_size);
        addView(this.mIconFrame, new LinearLayout.LayoutParams(dimensionPixelSize, dimensionPixelSize));
        this.mIcon = qSIconView;
        this.mIconFrame.addView(qSIconView);
        setImportantForAccessibility(2);
        setPadding(0, 0, 0, 0);
        setClipChildren(false);
        setClipToPadding(false);
        setFocusable(true);
    }

    @Override // com.android.systemui.plugins.qs.QSTileView
    public void init(QSTile qSTile) {
        init(new View.OnClickListener() {
            /* class com.android.systemui.qs.tileimpl.$$Lambda$MiuiQSTileBaseView$dC_s3RamS6twpy7OpNmEl3Qlro */

            public final void onClick(View view) {
                QSTile.this.click();
            }
        }, new View.OnClickListener() {
            /* class com.android.systemui.qs.tileimpl.$$Lambda$MiuiQSTileBaseView$_D0fJEIb006C0fXZMLrANeTwuq8 */

            public final void onClick(View view) {
                QSTile.this.secondaryClick();
            }
        }, new View.OnLongClickListener() {
            /* class com.android.systemui.qs.tileimpl.$$Lambda$MiuiQSTileBaseView$C1dZLf8m1_JnnZ1kzAtuKZZQBo0 */

            public final boolean onLongClick(View view) {
                return QSTile.this.longClick();
            }
        });
    }

    @SuppressLint({"ClickableViewAccessibility"})
    public void init(View.OnClickListener onClickListener, View.OnClickListener onClickListener2, View.OnLongClickListener onLongClickListener) {
        this.mIconFrame.setOnClickListener(onClickListener);
        this.mIconFrame.setOnLongClickListener(onLongClickListener);
        this.mIconFrame.setOnTouchListener(new View.OnTouchListener() {
            /* class com.android.systemui.qs.tileimpl.$$Lambda$MiuiQSTileBaseView$MJMAiwvnJnS6IdatEJ5btCTEEOw */

            public final boolean onTouch(View view, MotionEvent motionEvent) {
                return MiuiQSTileBaseView.this.lambda$init$3$MiuiQSTileBaseView(view, motionEvent);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$init$3 */
    public /* synthetic */ boolean lambda$init$3$MiuiQSTileBaseView(View view, MotionEvent motionEvent) {
        boolean z = true;
        if (this.mIconMouseAnim == null) {
            ITouchStyle iTouchStyle = Folme.useAt(view).touch();
            iTouchStyle.setTint(0.0f, 0.0f, 0.0f, 0.0f);
            this.mIconMouseAnim = iTouchStyle;
        }
        this.mIconMouseAnim.onMotionEventEx(view, motionEvent, new AnimConfig[0]);
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked == 0) {
            ((HapticFeedBackImpl) Dependency.get(HapticFeedBackImpl.class)).flick();
        } else if (actionMasked == 1) {
            if (motionEvent.getPointerId(0) != 0 || !pointInView(motionEvent.getX(), motionEvent.getY(), 0.0f) || !this.mIconFrame.isShown()) {
                z = false;
            }
            if (z) {
                ((HapticFeedBackImpl) Dependency.get(HapticFeedBackImpl.class)).flick();
            }
        }
        return false;
    }

    @Override // com.android.systemui.plugins.qs.QSTileView
    public View updateAccessibilityOrder(View view) {
        setAccessibilityTraversalAfter(view.getId());
        return this;
    }

    @Override // com.android.systemui.plugins.qs.QSTileView
    public void onStateChanged(QSTile.State state) {
        this.mHandler.obtainMessage(1, state).sendToTarget();
    }

    /* access modifiers changed from: protected */
    public void handleStateChanged(QSTile.State state) {
        boolean z;
        setClickable(state.state != 0);
        this.mIcon.setIcon(state, animationsEnabled());
        this.mIcon.setContentDescription(state.contentDescription);
        this.mAccessibilityClass = state.expandedAccessibilityClassName;
        if ((state instanceof QSTile.BooleanState) && this.mTileState != (z = ((QSTile.BooleanState) state).value)) {
            this.mClicked = false;
            this.mTileState = z;
        }
    }

    @Override // com.android.systemui.plugins.qs.QSTileView
    public int getDetailY() {
        return getTop() + (getHeight() / 2);
    }

    @Override // com.android.systemui.plugins.qs.QSTileView
    public QSIconView getIcon() {
        return this.mIcon;
    }

    @Override // com.android.systemui.plugins.qs.QSTileView
    public View getIconWithBackground() {
        return this.mIconFrame;
    }

    public boolean performClick() {
        this.mClicked = true;
        return super.performClick();
    }

    public void onInitializeAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        super.onInitializeAccessibilityEvent(accessibilityEvent);
        if (!TextUtils.isEmpty(this.mAccessibilityClass)) {
            accessibilityEvent.setClassName(this.mAccessibilityClass);
            if (Switch.class.getName().equals(this.mAccessibilityClass)) {
                boolean z = this.mClicked != this.mTileState;
                accessibilityEvent.setContentDescription(getResources().getString(z ? C0021R$string.switch_bar_on : C0021R$string.switch_bar_off));
                accessibilityEvent.setChecked(z);
            }
        }
    }

    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo accessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(accessibilityNodeInfo);
        if (!TextUtils.isEmpty(this.mAccessibilityClass)) {
            if (!this.mAccessibilityClass.equals(Button.class.getName())) {
                accessibilityNodeInfo.setClassName(this.mAccessibilityClass);
            }
            if (Switch.class.getName().equals(this.mAccessibilityClass)) {
                boolean z = this.mClicked != this.mTileState;
                accessibilityNodeInfo.setText(getResources().getString(z ? C0021R$string.switch_bar_on : C0021R$string.switch_bar_off));
                accessibilityNodeInfo.setChecked(z);
                accessibilityNodeInfo.setCheckable(true);
            }
        }
    }

    private class H extends Handler {
        private H(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message message) {
            if (message.what == 1) {
                MiuiQSTileBaseView.this.handleStateChanged((QSTile.State) message.obj);
            }
        }
    }
}
