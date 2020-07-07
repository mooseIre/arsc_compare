package com.android.systemui.qs.tiles;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.android.internal.logging.MetricsLogger;
import com.android.settingslib.RestrictedLockUtils;
import com.android.systemui.plugins.R;
import com.android.systemui.qs.PseudoGridView;
import com.android.systemui.statusbar.policy.UserSwitcherController;

public class UserDetailView extends PseudoGridView {
    protected Adapter mAdapter;

    public UserDetailView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public static UserDetailView inflate(Context context, ViewGroup viewGroup, boolean z) {
        return (UserDetailView) LayoutInflater.from(context).inflate(R.layout.qs_user_detail, viewGroup, z);
    }

    public void createAndSetAdapter(UserSwitcherController userSwitcherController) {
        Adapter adapter = new Adapter(this.mContext, userSwitcherController);
        this.mAdapter = adapter;
        PseudoGridView.ViewGroupAdapterBridge.link(this, adapter);
    }

    public void refreshAdapter() {
        this.mAdapter.refresh();
    }

    public static class Adapter extends UserSwitcherController.BaseUserAdapter implements View.OnClickListener {
        private final Context mContext;
        protected UserSwitcherController mController;

        public Adapter(Context context, UserSwitcherController userSwitcherController) {
            super(userSwitcherController);
            this.mContext = context;
            this.mController = userSwitcherController;
        }

        public View getView(int i, View view, ViewGroup viewGroup) {
            return createUserDetailItemView(view, viewGroup, getItem(i));
        }

        public UserDetailItemView createUserDetailItemView(View view, ViewGroup viewGroup, UserSwitcherController.UserRecord userRecord) {
            UserDetailItemView convertOrInflate = UserDetailItemView.convertOrInflate(this.mContext, view, viewGroup);
            if (convertOrInflate != view) {
                convertOrInflate.setOnClickListener(this);
            }
            String name = getName(this.mContext, userRecord);
            Bitmap bitmap = userRecord.picture;
            if (bitmap == null) {
                convertOrInflate.bind(name, getDrawable(this.mContext, userRecord), userRecord.resolveId());
            } else {
                convertOrInflate.bind(name, bitmap, userRecord.info.id);
            }
            convertOrInflate.setActivated(userRecord.isCurrent);
            convertOrInflate.setDisabledByAdmin(userRecord.isDisabledByAdmin);
            if (!userRecord.isSwitchToEnabled) {
                convertOrInflate.setEnabled(false);
            }
            convertOrInflate.setTag(userRecord);
            return convertOrInflate;
        }

        public void onClick(View view) {
            UserSwitcherController.UserRecord userRecord = (UserSwitcherController.UserRecord) view.getTag();
            if (userRecord.isDisabledByAdmin) {
                this.mController.startActivity(RestrictedLockUtils.getShowAdminSupportDetailsIntent(this.mContext, userRecord.enforcedAdmin));
            } else if (userRecord.isSwitchToEnabled) {
                MetricsLogger.action(this.mContext, 156);
                switchTo(userRecord);
            }
        }
    }
}
