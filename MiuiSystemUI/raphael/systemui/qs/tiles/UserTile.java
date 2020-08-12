package com.android.systemui.qs.tiles;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Pair;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.qs.DetailAdapter;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.statusbar.policy.UserInfoController;
import com.android.systemui.statusbar.policy.UserSwitcherController;

public class UserTile extends QSTileImpl<QSTile.State> implements UserInfoController.OnUserInfoChangedListener {
    private Pair<String, Drawable> mLastUpdate;
    private final UserInfoController mUserInfoController = ((UserInfoController) Dependency.get(UserInfoController.class));
    private final UserSwitcherController mUserSwitcherController = ((UserSwitcherController) Dependency.get(UserSwitcherController.class));

    public int getMetricsCategory() {
        return 260;
    }

    public UserTile(QSHost qSHost) {
        super(qSHost);
    }

    public QSTile.State newTileState() {
        return new QSTile.State();
    }

    public Intent getLongClickIntent() {
        return new Intent("android.settings.USER_SETTINGS");
    }

    /* access modifiers changed from: protected */
    public void handleClick() {
        showDetail(true);
    }

    public DetailAdapter getDetailAdapter() {
        return this.mUserSwitcherController.userDetailAdapter;
    }

    public void handleSetListening(boolean z) {
        if (z) {
            this.mUserInfoController.addCallback(this);
        } else {
            this.mUserInfoController.removeCallback(this);
        }
    }

    public CharSequence getTileLabel() {
        return getState().label;
    }

    /* access modifiers changed from: protected */
    public void handleUpdateState(QSTile.State state, Object obj) {
        final Pair<String, Drawable> pair = obj != null ? (Pair) obj : this.mLastUpdate;
        if (pair != null) {
            Object obj2 = pair.first;
            state.label = (CharSequence) obj2;
            state.contentDescription = (CharSequence) obj2;
            state.icon = new QSTile.Icon() {
                public Drawable getDrawable(Context context) {
                    return (Drawable) pair.second;
                }
            };
        }
    }

    public void onUserInfoChanged(String str, Drawable drawable, String str2) {
        this.mLastUpdate = new Pair<>(str, drawable);
        refreshState(this.mLastUpdate);
    }
}
