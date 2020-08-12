package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.os.UserManager;
import android.provider.ContactsContract;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.FrameLayout;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.qs.DetailAdapter;
import com.android.systemui.qs.QSPanel;
import com.android.systemui.statusbar.policy.KeyguardUserSwitcher;
import com.android.systemui.statusbar.policy.UserSwitcherController;

public class MultiUserSwitch extends FrameLayout implements View.OnClickListener {
    private boolean mKeyguardMode;
    private KeyguardUserSwitcher mKeyguardUserSwitcher;
    protected QSPanel mQsPanel;
    private final int[] mTmpInt2 = new int[2];
    private UserSwitcherController.BaseUserAdapter mUserListener;
    final UserManager mUserManager = UserManager.get(getContext());
    protected UserSwitcherController mUserSwitcherController;

    public boolean hasOverlappingRendering() {
        return false;
    }

    public MultiUserSwitch(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        setOnClickListener(this);
        refreshContentDescription();
    }

    public void setQsPanel(QSPanel qSPanel) {
        this.mQsPanel = qSPanel;
        setUserSwitcherController((UserSwitcherController) Dependency.get(UserSwitcherController.class));
    }

    public boolean hasMultipleUsers() {
        UserSwitcherController.BaseUserAdapter baseUserAdapter = this.mUserListener;
        if (baseUserAdapter == null || baseUserAdapter.getUserCount() == 0) {
            return false;
        }
        return true;
    }

    public void setUserSwitcherController(UserSwitcherController userSwitcherController) {
        this.mUserSwitcherController = userSwitcherController;
        registerListener();
        refreshContentDescription();
    }

    private void registerListener() {
        UserSwitcherController userSwitcherController;
        if (this.mUserManager.isUserSwitcherEnabled() && this.mUserListener == null && (userSwitcherController = this.mUserSwitcherController) != null) {
            this.mUserListener = new UserSwitcherController.BaseUserAdapter(userSwitcherController) {
                public View getView(int i, View view, ViewGroup viewGroup) {
                    return null;
                }

                public void notifyDataSetChanged() {
                    MultiUserSwitch.this.refreshContentDescription();
                }
            };
            refreshContentDescription();
        }
    }

    public void onClick(View view) {
        if (this.mUserManager.isUserSwitcherEnabled()) {
            if (this.mKeyguardMode) {
                KeyguardUserSwitcher keyguardUserSwitcher = this.mKeyguardUserSwitcher;
                if (keyguardUserSwitcher != null) {
                    keyguardUserSwitcher.show(true);
                }
            } else if (this.mQsPanel != null && this.mUserSwitcherController != null) {
                View childAt = getChildCount() > 0 ? getChildAt(0) : this;
                childAt.getLocationInWindow(this.mTmpInt2);
                int[] iArr = this.mTmpInt2;
                iArr[0] = iArr[0] + (childAt.getWidth() / 2);
                int[] iArr2 = this.mTmpInt2;
                iArr2[1] = iArr2[1] + (childAt.getHeight() / 2);
                this.mQsPanel.showDetailAdapter(true, getUserDetailAdapter(), this.mTmpInt2);
            }
        } else if (this.mQsPanel != null) {
            ((ActivityStarter) Dependency.get(ActivityStarter.class)).postStartActivityDismissingKeyguard(ContactsContract.QuickContact.composeQuickContactsIntent(getContext(), view, ContactsContract.Profile.CONTENT_URI, 3, (String[]) null), 0);
        }
    }

    public void setClickable(boolean z) {
        super.setClickable(z);
        refreshContentDescription();
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:2:0x0009, code lost:
        r0 = r5.mUserSwitcherController;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void refreshContentDescription() {
        /*
            r5 = this;
            android.os.UserManager r0 = r5.mUserManager
            boolean r0 = r0.isUserSwitcherEnabled()
            r1 = 0
            if (r0 == 0) goto L_0x0014
            com.android.systemui.statusbar.policy.UserSwitcherController r0 = r5.mUserSwitcherController
            if (r0 == 0) goto L_0x0014
            android.content.Context r2 = r5.mContext
            java.lang.String r0 = r0.getCurrentUserName(r2)
            goto L_0x0015
        L_0x0014:
            r0 = r1
        L_0x0015:
            boolean r2 = android.text.TextUtils.isEmpty(r0)
            if (r2 != 0) goto L_0x002a
            android.content.Context r1 = r5.mContext
            r2 = 2131820762(0x7f1100da, float:1.9274248E38)
            r3 = 1
            java.lang.Object[] r3 = new java.lang.Object[r3]
            r4 = 0
            r3[r4] = r0
            java.lang.String r1 = r1.getString(r2, r3)
        L_0x002a:
            java.lang.CharSequence r0 = r5.getContentDescription()
            boolean r0 = android.text.TextUtils.equals(r0, r1)
            if (r0 != 0) goto L_0x0037
            r5.setContentDescription(r1)
        L_0x0037:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.phone.MultiUserSwitch.refreshContentDescription():void");
    }

    public void onInitializeAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        super.onInitializeAccessibilityEvent(accessibilityEvent);
        accessibilityEvent.setClassName(Button.class.getName());
    }

    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo accessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(accessibilityNodeInfo);
        accessibilityNodeInfo.setClassName(Button.class.getName());
    }

    /* access modifiers changed from: protected */
    public DetailAdapter getUserDetailAdapter() {
        return this.mUserSwitcherController.userDetailAdapter;
    }
}
