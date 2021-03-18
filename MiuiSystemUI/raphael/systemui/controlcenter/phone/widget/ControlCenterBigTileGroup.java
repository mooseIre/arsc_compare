package com.android.systemui.controlcenter.phone.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0015R$id;
import com.android.systemui.controlcenter.phone.ControlCenterPanelView;
import com.android.systemui.controlcenter.phone.detail.QSControlExpandTileView;
import com.android.systemui.controlcenter.qs.tileview.QSBigTileView;
import com.android.systemui.controlcenter.utils.Constants;
import com.android.systemui.qs.QSTileHost;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Ref$ObjectRef;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: ControlCenterBigTileGroup.kt */
public final class ControlCenterBigTileGroup extends RelativeLayout {
    @Nullable
    private QSBigTileView bigTile0;
    @Nullable
    private QSBigTileView bigTile1;
    @Nullable
    private QSBigTileView bigTile2;
    @Nullable
    private QSBigTileView bigTile3;
    @Nullable
    private QSControlExpandTileView expandTileView;
    private boolean listening;
    @Nullable
    private View tileView0;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public ControlCenterBigTileGroup(@Nullable Context context, @NotNull AttributeSet attributeSet) {
        super(context, attributeSet);
        Intrinsics.checkParameterIsNotNull(attributeSet, "attrs");
    }

    @Nullable
    public final QSBigTileView getBigTile1() {
        return this.bigTile1;
    }

    @Nullable
    public final QSBigTileView getBigTile2() {
        return this.bigTile2;
    }

    @Nullable
    public final QSBigTileView getBigTile3() {
        return this.bigTile3;
    }

    @Nullable
    public final View getTileView0() {
        return this.tileView0;
    }

    public final void setListening(boolean z) {
        if (this.listening != z) {
            this.listening = z;
            QSBigTileView qSBigTileView = this.bigTile0;
            if (qSBigTileView != null) {
                qSBigTileView.handleSetListening(z);
            }
            QSBigTileView qSBigTileView2 = this.bigTile1;
            if (qSBigTileView2 != null) {
                qSBigTileView2.handleSetListening(z);
            }
            QSBigTileView qSBigTileView3 = this.bigTile2;
            if (qSBigTileView3 != null) {
                qSBigTileView3.handleSetListening(z);
            }
            QSBigTileView qSBigTileView4 = this.bigTile3;
            if (qSBigTileView4 != null) {
                qSBigTileView4.handleSetListening(z);
            }
        }
    }

    public final void init(@NotNull ControlCenterPanelView controlCenterPanelView) {
        Intrinsics.checkParameterIsNotNull(controlCenterPanelView, "panelView");
        QSControlExpandTileView qSControlExpandTileView = (QSControlExpandTileView) findViewById(C0015R$id.expand_tile);
        this.expandTileView = qSControlExpandTileView;
        if (Constants.IS_INTERNATIONAL) {
            if (qSControlExpandTileView != null) {
                qSControlExpandTileView.setVisibility(8);
            }
            QSBigTileView qSBigTileView = (QSBigTileView) findViewById(C0015R$id.big_tile_0);
            this.bigTile0 = qSBigTileView;
            if (qSBigTileView != null) {
                qSBigTileView.setVisibility(0);
            }
            QSBigTileView qSBigTileView2 = this.bigTile0;
            this.tileView0 = qSBigTileView2;
            if (qSBigTileView2 != null) {
                qSBigTileView2.init(controlCenterPanelView, "cell", 0);
            }
            QSBigTileView qSBigTileView3 = (QSBigTileView) findViewById(C0015R$id.big_tile_1);
            this.bigTile1 = qSBigTileView3;
            if (qSBigTileView3 != null) {
                qSBigTileView3.init(controlCenterPanelView, "wifi", 1);
            }
            QSBigTileView qSBigTileView4 = (QSBigTileView) findViewById(C0015R$id.big_tile_2);
            this.bigTile2 = qSBigTileView4;
            if (qSBigTileView4 != null) {
                qSBigTileView4.init(controlCenterPanelView, "bt", 2);
            }
            QSBigTileView qSBigTileView5 = (QSBigTileView) findViewById(C0015R$id.big_tile_3);
            this.bigTile3 = qSBigTileView5;
            if (qSBigTileView5 != null) {
                qSBigTileView5.init(controlCenterPanelView, "flashlight", 3);
                return;
            }
            return;
        }
        this.tileView0 = qSControlExpandTileView;
        QSBigTileView qSBigTileView6 = (QSBigTileView) findViewById(C0015R$id.big_tile_1);
        this.bigTile1 = qSBigTileView6;
        if (qSBigTileView6 != null) {
            qSBigTileView6.init(controlCenterPanelView, "bt", 1);
        }
        QSBigTileView qSBigTileView7 = (QSBigTileView) findViewById(C0015R$id.big_tile_2);
        this.bigTile2 = qSBigTileView7;
        if (qSBigTileView7 != null) {
            qSBigTileView7.init(controlCenterPanelView, "cell", 2);
        }
        QSBigTileView qSBigTileView8 = (QSBigTileView) findViewById(C0015R$id.big_tile_3);
        this.bigTile3 = qSBigTileView8;
        if (qSBigTileView8 != null) {
            qSBigTileView8.init(controlCenterPanelView, "wifi", 3);
        }
    }

    public final void setHost(@Nullable QSTileHost qSTileHost) {
        QSBigTileView qSBigTileView = this.bigTile0;
        if (qSBigTileView != null) {
            qSBigTileView.setHost(qSTileHost);
        }
        QSBigTileView qSBigTileView2 = this.bigTile1;
        if (qSBigTileView2 != null) {
            qSBigTileView2.setHost(qSTileHost);
        }
        QSBigTileView qSBigTileView3 = this.bigTile2;
        if (qSBigTileView3 != null) {
            qSBigTileView3.setHost(qSTileHost);
        }
        QSBigTileView qSBigTileView4 = this.bigTile3;
        if (qSBigTileView4 != null) {
            qSBigTileView4.setHost(qSTileHost);
        }
    }

    public final void onUserSwitched(int i) {
        QSBigTileView qSBigTileView = this.bigTile0;
        if (qSBigTileView != null) {
            qSBigTileView.onUserSwitched(i);
        }
        QSBigTileView qSBigTileView2 = this.bigTile1;
        if (qSBigTileView2 != null) {
            qSBigTileView2.onUserSwitched(i);
        }
        QSBigTileView qSBigTileView3 = this.bigTile2;
        if (qSBigTileView3 != null) {
            qSBigTileView3.onUserSwitched(i);
        }
        QSBigTileView qSBigTileView4 = this.bigTile3;
        if (qSBigTileView4 != null) {
            qSBigTileView4.onUserSwitched(i);
        }
    }

    public final void updateResources() {
        QSControlExpandTileView qSControlExpandTileView = this.expandTileView;
        if (qSControlExpandTileView != null) {
            qSControlExpandTileView.updateResources();
        }
        QSBigTileView qSBigTileView = this.bigTile0;
        if (qSBigTileView != null) {
            qSBigTileView.updateResources();
        }
        QSBigTileView qSBigTileView2 = this.bigTile1;
        if (qSBigTileView2 != null) {
            qSBigTileView2.updateResources();
        }
        QSBigTileView qSBigTileView3 = this.bigTile2;
        if (qSBigTileView3 != null) {
            qSBigTileView3.updateResources();
        }
        QSBigTileView qSBigTileView4 = this.bigTile3;
        if (qSBigTileView4 != null) {
            qSBigTileView4.updateResources();
        }
    }

    public final void updateLayout() {
        Context context = getContext();
        Intrinsics.checkExpressionValueIsNotNull(context, "context");
        int dimensionPixelSize = context.getResources().getDimensionPixelSize(C0012R$dimen.qs_control_big_tile_height);
        Context context2 = getContext();
        Intrinsics.checkExpressionValueIsNotNull(context2, "context");
        int dimensionPixelSize2 = context2.getResources().getDimensionPixelSize(C0012R$dimen.qs_control_big_tiles_interval_vertical);
        Ref$ObjectRef ref$ObjectRef = new Ref$ObjectRef();
        QSBigTileView qSBigTileView = this.bigTile0;
        if (qSBigTileView == null) {
            QSControlExpandTileView qSControlExpandTileView = this.expandTileView;
            if (qSControlExpandTileView != null) {
                ViewGroup.LayoutParams layoutParams = qSControlExpandTileView.getLayoutParams();
                if (layoutParams != null) {
                    T t = (T) ((RelativeLayout.LayoutParams) layoutParams);
                    ref$ObjectRef.element = t;
                    ((RelativeLayout.LayoutParams) t).height = dimensionPixelSize;
                    qSControlExpandTileView.setLayoutParams(t);
                } else {
                    throw new TypeCastException("null cannot be cast to non-null type android.widget.RelativeLayout.LayoutParams");
                }
            }
        } else if (qSBigTileView != null) {
            ViewGroup.LayoutParams layoutParams2 = qSBigTileView.getLayoutParams();
            if (layoutParams2 != null) {
                T t2 = (T) ((RelativeLayout.LayoutParams) layoutParams2);
                ref$ObjectRef.element = t2;
                ((RelativeLayout.LayoutParams) t2).height = dimensionPixelSize;
                QSBigTileView qSBigTileView2 = this.bigTile0;
                if (qSBigTileView2 != null) {
                    qSBigTileView2.setLayoutParams(t2);
                } else {
                    Intrinsics.throwNpe();
                    throw null;
                }
            } else {
                throw new TypeCastException("null cannot be cast to non-null type android.widget.RelativeLayout.LayoutParams");
            }
        } else {
            Intrinsics.throwNpe();
            throw null;
        }
        QSBigTileView qSBigTileView3 = this.bigTile1;
        if (qSBigTileView3 != null) {
            ViewGroup.LayoutParams layoutParams3 = qSBigTileView3.getLayoutParams();
            if (layoutParams3 != null) {
                T t3 = (T) ((RelativeLayout.LayoutParams) layoutParams3);
                ref$ObjectRef.element = t3;
                ((RelativeLayout.LayoutParams) t3).height = dimensionPixelSize;
                qSBigTileView3.setLayoutParams(t3);
            } else {
                throw new TypeCastException("null cannot be cast to non-null type android.widget.RelativeLayout.LayoutParams");
            }
        }
        QSBigTileView qSBigTileView4 = this.bigTile2;
        if (qSBigTileView4 != null) {
            ViewGroup.LayoutParams layoutParams4 = qSBigTileView4.getLayoutParams();
            if (layoutParams4 != null) {
                T t4 = (T) ((RelativeLayout.LayoutParams) layoutParams4);
                ref$ObjectRef.element = t4;
                ((RelativeLayout.LayoutParams) t4).height = dimensionPixelSize;
                ((RelativeLayout.LayoutParams) t4).topMargin = dimensionPixelSize2;
                qSBigTileView4.setLayoutParams(t4);
            } else {
                throw new TypeCastException("null cannot be cast to non-null type android.widget.RelativeLayout.LayoutParams");
            }
        }
        QSBigTileView qSBigTileView5 = this.bigTile3;
        if (qSBigTileView5 != null) {
            ViewGroup.LayoutParams layoutParams5 = qSBigTileView5.getLayoutParams();
            if (layoutParams5 != null) {
                T t5 = (T) ((RelativeLayout.LayoutParams) layoutParams5);
                ref$ObjectRef.element = t5;
                ((RelativeLayout.LayoutParams) t5).height = dimensionPixelSize;
                ((RelativeLayout.LayoutParams) t5).topMargin = dimensionPixelSize2;
                qSBigTileView5.setLayoutParams(t5);
                return;
            }
            throw new TypeCastException("null cannot be cast to non-null type android.widget.RelativeLayout.LayoutParams");
        }
    }

    public final int calculateHeight() {
        Context context = getContext();
        Intrinsics.checkExpressionValueIsNotNull(context, "context");
        int dimensionPixelSize = context.getResources().getDimensionPixelSize(C0012R$dimen.qs_control_big_tile_height);
        Context context2 = getContext();
        Intrinsics.checkExpressionValueIsNotNull(context2, "context");
        int dimensionPixelSize2 = context2.getResources().getDimensionPixelSize(C0012R$dimen.qs_control_big_tiles_interval_vertical);
        Context context3 = getContext();
        Intrinsics.checkExpressionValueIsNotNull(context3, "context");
        return (dimensionPixelSize * 2) + dimensionPixelSize2 + context3.getResources().getDimensionPixelSize(C0012R$dimen.qs_control_big_tiles_padding_top);
    }
}
