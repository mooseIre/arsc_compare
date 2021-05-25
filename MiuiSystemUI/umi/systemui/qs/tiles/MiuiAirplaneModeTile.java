package com.android.systemui.qs.tiles;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;
import android.widget.Switch;
import androidx.constraintlayout.widget.R$styleable;
import com.android.systemui.C0012R$drawable;
import com.android.systemui.C0020R$string;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.statusbar.policy.NetworkController;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: MiuiAirplaneModeTile.kt */
public final class MiuiAirplaneModeTile extends QSTileImpl<QSTile.BooleanState> implements NetworkController.SignalCallback {
    private boolean mAirplane;
    private boolean mListening;
    private final NetworkController mNetworkController;
    private boolean mTargetAirplane;

    @Override // com.android.systemui.plugins.qs.QSTile, com.android.systemui.qs.tileimpl.QSTileImpl
    public int getMetricsCategory() {
        return R$styleable.Constraint_visibilityMode;
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public MiuiAirplaneModeTile(@Nullable QSHost qSHost, @NotNull NetworkController networkController) {
        super(qSHost);
        Intrinsics.checkParameterIsNotNull(networkController, "networkController");
        this.mNetworkController = networkController;
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    @NotNull
    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleClick() {
        boolean z = this.mAirplane;
        if (z != this.mTargetAirplane) {
            String str = this.TAG;
            Log.d(str, "handleClick: mTargetValue = " + this.mTargetAirplane + ", value = " + z);
            return;
        }
        setEnabled(!z);
        refreshState();
    }

    private final void setEnabled(boolean z) {
        Object systemService = this.mContext.getSystemService("connectivity");
        if (systemService != null) {
            this.mTargetAirplane = z;
            ((ConnectivityManager) systemService).setAirplaneMode(z);
            return;
        }
        throw new TypeCastException("null cannot be cast to non-null type android.net.ConnectivityManager");
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    @NotNull
    public Intent getLongClickIntent() {
        return new Intent("android.settings.AIRPLANE_MODE_SETTINGS");
    }

    @Override // com.android.systemui.plugins.qs.QSTile
    @NotNull
    public CharSequence getTileLabel() {
        String string = this.mContext.getString(C0020R$string.airplane_mode);
        Intrinsics.checkExpressionValueIsNotNull(string, "mContext.getString(R.string.airplane_mode)");
        return string;
    }

    /* access modifiers changed from: protected */
    public void handleUpdateState(@Nullable QSTile.BooleanState booleanState, @Nullable Object obj) {
        boolean z = this.mAirplane;
        if (booleanState != null) {
            booleanState.value = z;
            booleanState.label = this.mContext.getString(C0020R$string.airplane_mode);
            if (z) {
                booleanState.icon = QSTileImpl.ResourceIcon.get(C0012R$drawable.ic_signal_airplane_enable);
            } else {
                booleanState.icon = QSTileImpl.ResourceIcon.get(C0012R$drawable.ic_signal_airplane_disable);
            }
            booleanState.state = z ? 2 : 1;
            StringBuilder sb = new StringBuilder();
            sb.append(booleanState.label.toString());
            sb.append(",");
            sb.append(this.mContext.getString(booleanState.value ? C0020R$string.switch_bar_on : C0020R$string.switch_bar_off));
            booleanState.contentDescription = sb.toString();
            booleanState.expandedAccessibilityClassName = Switch.class.getName();
            return;
        }
        Intrinsics.throwNpe();
        throw null;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    @NotNull
    public String composeChangeAnnouncement() {
        TState tstate = this.mState;
        if (tstate == null) {
            Intrinsics.throwNpe();
            throw null;
        } else if (((QSTile.BooleanState) tstate).value) {
            String string = this.mContext.getString(C0020R$string.accessibility_quick_settings_airplane_changed_on);
            Intrinsics.checkExpressionValueIsNotNull(string, "mContext.getString(R.str…ings_airplane_changed_on)");
            return string;
        } else {
            String string2 = this.mContext.getString(C0020R$string.accessibility_quick_settings_airplane_changed_off);
            Intrinsics.checkExpressionValueIsNotNull(string2, "mContext.getString(R.str…ngs_airplane_changed_off)");
            return string2;
        }
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleSetListening(boolean z) {
        super.handleSetListening(z);
        if (this.mListening != z) {
            this.mListening = z;
            if (z) {
                this.mNetworkController.addCallback((NetworkController.SignalCallback) this);
            } else {
                this.mNetworkController.removeCallback((NetworkController.SignalCallback) this);
            }
        }
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController.SignalCallback
    public void setIsAirplaneMode(@Nullable NetworkController.IconState iconState) {
        super.setIsAirplaneMode(iconState);
        if (iconState != null) {
            boolean z = iconState.visible;
            this.mTargetAirplane = z;
            this.mAirplane = z;
            refreshState();
        }
    }
}
