package com.android.systemui.plugins.qs;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.metrics.LogMaker;
import com.android.systemui.plugins.annotations.ProvidesInterface;
import java.util.Objects;

@ProvidesInterface(version = 1)
public interface QSTile {
    public static final int STATE_ACTIVE = 2;
    public static final int STATE_INACTIVE = 1;
    public static final int STATE_UNAVAILABLE = 0;
    public static final int TILE_ACTIVE_BG_AUTO_BRIGHTNESS = 2;
    public static final int TILE_ACTIVE_BG_BATTERY_RELATED = 1;
    public static final int TILE_ACTIVE_BG_NORMAL = 0;
    public static final int VERSION = 1;

    @ProvidesInterface(version = 1)
    public interface Callback {
        public static final int VERSION = 1;

        void onAnnouncementRequested(CharSequence charSequence);

        void onScanStateChanged(boolean z);

        void onShowDetail(boolean z);

        void onShowEdit(boolean z);

        void onStateChanged(State state);

        void onToggleStateChanged(boolean z);
    }

    void addCallback(Callback callback);

    void clearState();

    void click();

    void click(boolean z);

    QSIconView createControlCenterTileView(Context context);

    QSIconView createTileView(Context context);

    void destroy();

    DetailAdapter getDetailAdapter();

    int getIndex();

    int getMetricsCategory();

    State getState();

    CharSequence getTileLabel();

    String getTileSpec();

    boolean isAvailable();

    void longClick();

    LogMaker populate(LogMaker logMaker);

    void refreshState();

    void removeCallback(Callback callback);

    void removeCallbacks();

    void secondaryClick();

    void setDetailListening(boolean z);

    void setInControlCenter(boolean z);

    void setIndex(int i);

    void setListening(Object obj, boolean z);

    void setTileSpec(String str);

    void userSwitch(int i);

    @ProvidesInterface(version = 1)
    public static abstract class Icon {
        public static final int VERSION = 1;

        public abstract Drawable getDrawable(Context context);

        public int getPadding() {
            return 0;
        }

        public Drawable getInvisibleDrawable(Context context) {
            return getDrawable(context);
        }

        public int hashCode() {
            return Icon.class.hashCode();
        }
    }

    @ProvidesInterface(version = 1)
    public static class State {
        public static final int VERSION = 1;
        public int activeBgColor = 0;
        public CharSequence contentDescription;
        public boolean disabledByPolicy;
        public CharSequence dualLabelContentDescription;
        public boolean dualTarget = false;
        public String expandedAccessibilityClassName;
        public Icon icon;
        public boolean isTransient = false;
        public CharSequence label;
        public CharSequence secondaryLabel;
        public int state = 2;
        public boolean withAnimation;

        public boolean copyTo(State state2) {
            if (state2 == null) {
                throw new IllegalArgumentException();
            } else if (state2.getClass().equals(getClass())) {
                boolean z = !Objects.equals(state2.icon, this.icon) || !Objects.equals(state2.label, this.label) || !Objects.equals(state2.secondaryLabel, this.secondaryLabel) || !Objects.equals(state2.contentDescription, this.contentDescription) || !Objects.equals(state2.dualLabelContentDescription, this.dualLabelContentDescription) || !Objects.equals(state2.expandedAccessibilityClassName, this.expandedAccessibilityClassName) || !Objects.equals(Boolean.valueOf(state2.disabledByPolicy), Boolean.valueOf(this.disabledByPolicy)) || !Objects.equals(Integer.valueOf(state2.state), Integer.valueOf(this.state)) || !Objects.equals(Boolean.valueOf(state2.isTransient), Boolean.valueOf(this.isTransient)) || !Objects.equals(Boolean.valueOf(state2.dualTarget), Boolean.valueOf(this.dualTarget)) || !Objects.equals(Boolean.valueOf(state2.withAnimation), Boolean.valueOf(this.withAnimation));
                state2.icon = this.icon;
                state2.label = this.label;
                state2.secondaryLabel = this.secondaryLabel;
                state2.contentDescription = this.contentDescription;
                state2.dualLabelContentDescription = this.dualLabelContentDescription;
                state2.expandedAccessibilityClassName = this.expandedAccessibilityClassName;
                state2.disabledByPolicy = this.disabledByPolicy;
                state2.state = this.state;
                state2.dualTarget = this.dualTarget;
                state2.isTransient = this.isTransient;
                state2.withAnimation = this.withAnimation;
                state2.activeBgColor = this.activeBgColor;
                return z;
            } else {
                throw new IllegalArgumentException();
            }
        }

        public String toString() {
            return toStringBuilder().toString();
        }

        /* access modifiers changed from: protected */
        public StringBuilder toStringBuilder() {
            StringBuilder sb = new StringBuilder(getClass().getSimpleName());
            sb.append('[');
            sb.append(",icon=");
            sb.append(this.icon);
            sb.append(",label=");
            sb.append(this.label);
            sb.append(",secondaryLabel=");
            sb.append(this.secondaryLabel);
            sb.append(",contentDescription=");
            sb.append(this.contentDescription);
            sb.append(",dualLabelContentDescription=");
            sb.append(this.dualLabelContentDescription);
            sb.append(",expandedAccessibilityClassName=");
            sb.append(this.expandedAccessibilityClassName);
            sb.append(",disabledByPolicy=");
            sb.append(this.disabledByPolicy);
            sb.append(",dualTarget=");
            sb.append(this.dualTarget);
            sb.append(",isTransient=");
            sb.append(this.isTransient);
            sb.append(",state=");
            sb.append(this.state);
            sb.append(",animation=");
            sb.append(this.withAnimation);
            sb.append(",activeBgColor=");
            sb.append(this.activeBgColor);
            sb.append(']');
            return sb;
        }

        public State copy() {
            State state2 = new State();
            copyTo(state2);
            return state2;
        }
    }

    @ProvidesInterface(version = 1)
    public static class BooleanState extends State {
        public static final int VERSION = 1;
        public boolean value;

        public boolean copyTo(State state) {
            BooleanState booleanState = (BooleanState) state;
            boolean z = super.copyTo(state) || booleanState.value != this.value;
            booleanState.value = this.value;
            return z;
        }

        /* access modifiers changed from: protected */
        public StringBuilder toStringBuilder() {
            StringBuilder stringBuilder = super.toStringBuilder();
            stringBuilder.insert(stringBuilder.length() - 1, ",value=" + this.value);
            return stringBuilder;
        }

        public State copy() {
            BooleanState booleanState = new BooleanState();
            copyTo(booleanState);
            return booleanState;
        }
    }

    @ProvidesInterface(version = 1)
    public static final class SignalState extends BooleanState {
        public static final int VERSION = 1;
        public boolean activityIn;
        public boolean activityOut;
        public boolean isOverlayIconWide;
        public int overlayIconId;

        public boolean copyTo(State state) {
            SignalState signalState = (SignalState) state;
            boolean z = (signalState.activityIn == this.activityIn && signalState.activityOut == this.activityOut && signalState.isOverlayIconWide == this.isOverlayIconWide && signalState.overlayIconId == this.overlayIconId) ? false : true;
            signalState.activityIn = this.activityIn;
            signalState.activityOut = this.activityOut;
            signalState.isOverlayIconWide = this.isOverlayIconWide;
            signalState.overlayIconId = this.overlayIconId;
            if (super.copyTo(state) || z) {
                return true;
            }
            return false;
        }

        /* access modifiers changed from: protected */
        public StringBuilder toStringBuilder() {
            StringBuilder stringBuilder = super.toStringBuilder();
            stringBuilder.insert(stringBuilder.length() - 1, ",activityIn=" + this.activityIn);
            stringBuilder.insert(stringBuilder.length() - 1, ",activityOut=" + this.activityOut);
            return stringBuilder;
        }

        public State copy() {
            SignalState signalState = new SignalState();
            copyTo(signalState);
            return signalState;
        }
    }

    @ProvidesInterface(version = 1)
    public static class AirplaneBooleanState extends BooleanState {
        public static final int VERSION = 1;
        public boolean isAirplaneMode;

        public boolean copyTo(State state) {
            AirplaneBooleanState airplaneBooleanState = (AirplaneBooleanState) state;
            boolean z = super.copyTo(state) || airplaneBooleanState.isAirplaneMode != this.isAirplaneMode;
            airplaneBooleanState.isAirplaneMode = this.isAirplaneMode;
            return z;
        }

        public State copy() {
            AirplaneBooleanState airplaneBooleanState = new AirplaneBooleanState();
            copyTo(airplaneBooleanState);
            return airplaneBooleanState;
        }
    }
}
