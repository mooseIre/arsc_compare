package com.android.systemui.qs.tiles;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;
import android.widget.Switch;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0021R$string;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.GlobalSetting;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: MiuiAirplaneModeTile.kt */
public final class MiuiAirplaneModeTile extends QSTileImpl<QSTile.BooleanState> {
    private boolean mListening;
    private final GlobalSetting mSetting;
    private int mTargetValue;

    @Override // com.android.systemui.plugins.qs.QSTile, com.android.systemui.qs.tileimpl.QSTileImpl
    public int getMetricsCategory() {
        return 112;
    }

    public MiuiAirplaneModeTile(@Nullable QSHost qSHost) {
        super(qSHost);
        AnonymousClass1 r4 = new GlobalSetting(this, this.mContext, this.mHandler, "airplane_mode_on") {
            /* class com.android.systemui.qs.tiles.MiuiAirplaneModeTile.AnonymousClass1 */
            final /* synthetic */ MiuiAirplaneModeTile this$0;

            {
                this.this$0 = r1;
            }

            /* access modifiers changed from: protected */
            @Override // com.android.systemui.qs.GlobalSetting
            public void handleValueChanged(int i) {
                this.this$0.mTargetValue = i;
                this.this$0.handleRefreshState(Integer.valueOf(i));
            }
        };
        this.mSetting = r4;
        this.mTargetValue = r4.getValue();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    @NotNull
    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleClick() {
        int value = this.mSetting.getValue();
        if (value != this.mTargetValue) {
            String str = this.TAG;
            Log.d(str, "handleClick: mTargetValue = " + this.mTargetValue + ", value = " + value);
            return;
        }
        boolean z = true;
        if (value == 1) {
            z = false;
        }
        setEnabled(z);
        refreshState();
    }

    private final void setEnabled(boolean z) {
        Object systemService = this.mContext.getSystemService("connectivity");
        if (systemService != null) {
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
        String string = this.mContext.getString(C0021R$string.airplane_mode);
        Intrinsics.checkExpressionValueIsNotNull(string, "mContext.getString(R.string.airplane_mode)");
        return string;
    }

    /* access modifiers changed from: protected */
    public void handleUpdateState(@Nullable QSTile.BooleanState booleanState, @Nullable Object obj) {
        int i = 1;
        boolean z = (obj instanceof Integer ? ((Number) obj).intValue() : this.mSetting.getValue()) != 0;
        if (booleanState != null) {
            booleanState.value = z;
            booleanState.label = this.mContext.getString(C0021R$string.airplane_mode);
            if (z) {
                booleanState.icon = QSTileImpl.ResourceIcon.get(C0013R$drawable.ic_signal_airplane_enable);
            } else {
                booleanState.icon = QSTileImpl.ResourceIcon.get(C0013R$drawable.ic_signal_airplane_disable);
            }
            if (z) {
                i = 2;
            }
            booleanState.state = i;
            StringBuilder sb = new StringBuilder();
            sb.append(booleanState.label.toString());
            sb.append(",");
            sb.append(this.mContext.getString(booleanState.value ? C0021R$string.switch_bar_on : C0021R$string.switch_bar_off));
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
            String string = this.mContext.getString(C0021R$string.accessibility_quick_settings_airplane_changed_on);
            Intrinsics.checkExpressionValueIsNotNull(string, "mContext.getString(R.str…ings_airplane_changed_on)");
            return string;
        } else {
            String string2 = this.mContext.getString(C0021R$string.accessibility_quick_settings_airplane_changed_off);
            Intrinsics.checkExpressionValueIsNotNull(string2, "mContext.getString(R.str…ngs_airplane_changed_off)");
            return string2;
        }
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleSetListening(boolean z) {
        super.handleSetListening(z);
        if (this.mListening != z) {
            this.mListening = z;
            this.mTargetValue = this.mSetting.getValue();
            this.mSetting.setListening(z);
        }
    }
}
