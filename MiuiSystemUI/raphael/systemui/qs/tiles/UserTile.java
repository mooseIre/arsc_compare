package com.android.systemui.qs.tiles;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Pair;
import com.android.systemui.plugins.qs.DetailAdapter;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.statusbar.policy.UserInfoController;
import com.android.systemui.statusbar.policy.UserSwitcherController;

public class UserTile extends QSTileImpl<QSTile.State> implements UserInfoController.OnUserInfoChangedListener {
    private Pair<String, Drawable> mLastUpdate;
    private final UserInfoController mUserInfoController;
    private final UserSwitcherController mUserSwitcherController;

    @Override // com.android.systemui.plugins.qs.QSTile, com.android.systemui.qs.tileimpl.QSTileImpl
    public int getMetricsCategory() {
        return 260;
    }

    public UserTile(QSHost qSHost, UserSwitcherController userSwitcherController, UserInfoController userInfoController) {
        super(qSHost);
        this.mUserSwitcherController = userSwitcherController;
        this.mUserInfoController = userInfoController;
        userInfoController.observe(getLifecycle(), this);
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public QSTile.State newTileState() {
        return new QSTile.State();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public Intent getLongClickIntent() {
        return new Intent("android.settings.USER_SETTINGS");
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleClick() {
        showDetail(true);
    }

    @Override // com.android.systemui.plugins.qs.QSTile, com.android.systemui.qs.tileimpl.QSTileImpl
    public DetailAdapter getDetailAdapter() {
        return this.mUserSwitcherController.userDetailAdapter;
    }

    @Override // com.android.systemui.plugins.qs.QSTile
    public CharSequence getTileLabel() {
        return getState().label;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleUpdateState(QSTile.State state, Object obj) {
        final Pair<String, Drawable> pair = obj != null ? (Pair) obj : this.mLastUpdate;
        if (pair != null) {
            Object obj2 = pair.first;
            state.label = (CharSequence) obj2;
            state.contentDescription = (CharSequence) obj2;
            state.icon = new QSTile.Icon(this) {
                /* class com.android.systemui.qs.tiles.UserTile.AnonymousClass1 */

                @Override // com.android.systemui.plugins.qs.QSTile.Icon
                public Drawable getDrawable(Context context) {
                    return (Drawable) pair.second;
                }
            };
        }
    }

    @Override // com.android.systemui.statusbar.policy.UserInfoController.OnUserInfoChangedListener
    public void onUserInfoChanged(String str, Drawable drawable, String str2) {
        Pair<String, Drawable> pair = new Pair<>(str, drawable);
        this.mLastUpdate = pair;
        refreshState(pair);
    }
}
