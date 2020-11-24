package com.android.systemui.statusbar.policy;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.FrameLayout;
import com.android.settingslib.animation.AppearAnimationUtils;
import com.android.settingslib.drawable.CircleFramedDrawable;
import com.android.systemui.C0008R$color;
import com.android.systemui.C0009R$dimen;
import com.android.systemui.C0010R$drawable;
import com.android.systemui.C0012R$id;
import com.android.systemui.C0014R$layout;
import com.android.systemui.Dependency;
import com.android.systemui.Interpolators;
import com.android.systemui.qs.tiles.UserDetailItemView;
import com.android.systemui.statusbar.phone.KeyguardStatusBarView;
import com.android.systemui.statusbar.phone.NotificationPanelViewController;
import com.android.systemui.statusbar.policy.UserSwitcherController;

public class KeyguardUserSwitcher {
    private final Adapter mAdapter;
    /* access modifiers changed from: private */
    public boolean mAnimating;
    private final AppearAnimationUtils mAppearAnimationUtils;
    private final KeyguardUserSwitcherScrim mBackground;
    /* access modifiers changed from: private */
    public ObjectAnimator mBgAnimator;
    public final DataSetObserver mDataSetObserver = new DataSetObserver() {
        public void onChanged() {
            KeyguardUserSwitcher.this.refresh();
        }
    };
    private final KeyguardStatusBarView mStatusBarView;
    /* access modifiers changed from: private */
    public ViewGroup mUserSwitcher;
    /* access modifiers changed from: private */
    public final Container mUserSwitcherContainer;
    private UserSwitcherController mUserSwitcherController;

    public KeyguardUserSwitcher(Context context, ViewStub viewStub, KeyguardStatusBarView keyguardStatusBarView, NotificationPanelViewController notificationPanelViewController) {
        boolean z = context.getResources().getBoolean(17891478);
        UserSwitcherController userSwitcherController = (UserSwitcherController) Dependency.get(UserSwitcherController.class);
        if (userSwitcherController == null || !z) {
            this.mUserSwitcherContainer = null;
            this.mStatusBarView = null;
            this.mAdapter = null;
            this.mAppearAnimationUtils = null;
            this.mBackground = null;
            return;
        }
        this.mUserSwitcherContainer = (Container) viewStub.inflate();
        this.mBackground = new KeyguardUserSwitcherScrim(context);
        reinflateViews();
        this.mStatusBarView = keyguardStatusBarView;
        keyguardStatusBarView.setKeyguardUserSwitcher(this);
        notificationPanelViewController.setKeyguardUserSwitcher(this);
        Adapter adapter = new Adapter(context, userSwitcherController, this);
        this.mAdapter = adapter;
        adapter.registerDataSetObserver(this.mDataSetObserver);
        this.mUserSwitcherController = userSwitcherController;
        this.mAppearAnimationUtils = new AppearAnimationUtils(context, 400, -0.5f, 0.5f, Interpolators.FAST_OUT_SLOW_IN);
        this.mUserSwitcherContainer.setKeyguardUserSwitcher(this);
    }

    private void reinflateViews() {
        ViewGroup viewGroup = this.mUserSwitcher;
        if (viewGroup != null) {
            viewGroup.setBackground((Drawable) null);
            this.mUserSwitcher.removeOnLayoutChangeListener(this.mBackground);
        }
        this.mUserSwitcherContainer.removeAllViews();
        LayoutInflater.from(this.mUserSwitcherContainer.getContext()).inflate(C0014R$layout.keyguard_user_switcher_inner, this.mUserSwitcherContainer);
        ViewGroup viewGroup2 = (ViewGroup) this.mUserSwitcherContainer.findViewById(C0012R$id.keyguard_user_switcher_inner);
        this.mUserSwitcher = viewGroup2;
        viewGroup2.addOnLayoutChangeListener(this.mBackground);
        this.mUserSwitcher.setBackground(this.mBackground);
    }

    public void setKeyguard(boolean z, boolean z2) {
        if (this.mUserSwitcher == null) {
            return;
        }
        if (!z || !shouldExpandByDefault()) {
            hide(z2);
        } else {
            show(z2);
        }
    }

    private boolean shouldExpandByDefault() {
        UserSwitcherController userSwitcherController = this.mUserSwitcherController;
        return userSwitcherController != null && userSwitcherController.isSimpleUserSwitcher();
    }

    public void show(boolean z) {
        if (this.mUserSwitcher != null && this.mUserSwitcherContainer.getVisibility() != 0) {
            cancelAnimations();
            this.mAdapter.refresh();
            this.mUserSwitcherContainer.setVisibility(0);
            this.mStatusBarView.setKeyguardUserSwitcherShowing(true, z);
            if (z) {
                startAppearAnimation();
            }
        }
    }

    private boolean hide(boolean z) {
        if (this.mUserSwitcher == null || this.mUserSwitcherContainer.getVisibility() != 0) {
            return false;
        }
        cancelAnimations();
        if (z) {
            startDisappearAnimation();
        } else {
            this.mUserSwitcherContainer.setVisibility(8);
        }
        this.mStatusBarView.setKeyguardUserSwitcherShowing(false, z);
        return true;
    }

    private void cancelAnimations() {
        int childCount = this.mUserSwitcher.getChildCount();
        for (int i = 0; i < childCount; i++) {
            this.mUserSwitcher.getChildAt(i).animate().cancel();
        }
        ObjectAnimator objectAnimator = this.mBgAnimator;
        if (objectAnimator != null) {
            objectAnimator.cancel();
        }
        this.mUserSwitcher.animate().cancel();
        this.mAnimating = false;
    }

    private void startAppearAnimation() {
        int childCount = this.mUserSwitcher.getChildCount();
        View[] viewArr = new View[childCount];
        for (int i = 0; i < childCount; i++) {
            viewArr[i] = this.mUserSwitcher.getChildAt(i);
        }
        this.mUserSwitcher.setClipChildren(false);
        this.mUserSwitcher.setClipToPadding(false);
        this.mAppearAnimationUtils.startAnimation(viewArr, new Runnable() {
            public void run() {
                KeyguardUserSwitcher.this.mUserSwitcher.setClipChildren(true);
                KeyguardUserSwitcher.this.mUserSwitcher.setClipToPadding(true);
            }
        });
        this.mAnimating = true;
        ObjectAnimator ofInt = ObjectAnimator.ofInt(this.mBackground, "alpha", new int[]{0, 255});
        this.mBgAnimator = ofInt;
        ofInt.setDuration(400);
        this.mBgAnimator.setInterpolator(Interpolators.ALPHA_IN);
        this.mBgAnimator.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animator) {
                ObjectAnimator unused = KeyguardUserSwitcher.this.mBgAnimator = null;
                boolean unused2 = KeyguardUserSwitcher.this.mAnimating = false;
            }
        });
        this.mBgAnimator.start();
    }

    private void startDisappearAnimation() {
        this.mAnimating = true;
        this.mUserSwitcher.animate().alpha(0.0f).setDuration(300).setInterpolator(Interpolators.ALPHA_OUT).withEndAction(new Runnable() {
            public void run() {
                KeyguardUserSwitcher.this.mUserSwitcherContainer.setVisibility(8);
                KeyguardUserSwitcher.this.mUserSwitcher.setAlpha(1.0f);
                boolean unused = KeyguardUserSwitcher.this.mAnimating = false;
            }
        });
    }

    /* access modifiers changed from: private */
    public void refresh() {
        int childCount = this.mUserSwitcher.getChildCount();
        int count = this.mAdapter.getCount();
        int max = Math.max(childCount, count);
        for (int i = 0; i < max; i++) {
            if (i < count) {
                View view = null;
                if (i < childCount) {
                    view = this.mUserSwitcher.getChildAt(i);
                }
                View view2 = this.mAdapter.getView(i, view, this.mUserSwitcher);
                if (view == null) {
                    this.mUserSwitcher.addView(view2);
                } else if (view != view2) {
                    this.mUserSwitcher.removeViewAt(i);
                    this.mUserSwitcher.addView(view2, i);
                }
            } else {
                this.mUserSwitcher.removeViewAt(this.mUserSwitcher.getChildCount() - 1);
            }
        }
    }

    public boolean hideIfNotSimple(boolean z) {
        if (this.mUserSwitcherContainer == null || this.mUserSwitcherController.isSimpleUserSwitcher()) {
            return false;
        }
        return hide(z);
    }

    /* access modifiers changed from: package-private */
    public boolean isAnimating() {
        return this.mAnimating;
    }

    public void onDensityOrFontScaleChanged() {
        if (this.mUserSwitcherContainer != null) {
            reinflateViews();
            refresh();
        }
    }

    public static class Adapter extends UserSwitcherController.BaseUserAdapter implements View.OnClickListener {
        private Context mContext;
        private View mCurrentUserView;
        private KeyguardUserSwitcher mKeyguardUserSwitcher;

        public Adapter(Context context, UserSwitcherController userSwitcherController, KeyguardUserSwitcher keyguardUserSwitcher) {
            super(userSwitcherController);
            this.mContext = context;
            this.mKeyguardUserSwitcher = keyguardUserSwitcher;
        }

        public View getView(int i, View view, ViewGroup viewGroup) {
            UserSwitcherController.UserRecord item = getItem(i);
            if (!(view instanceof UserDetailItemView) || !(view.getTag() instanceof UserSwitcherController.UserRecord)) {
                view = LayoutInflater.from(this.mContext).inflate(C0014R$layout.keyguard_user_switcher_item, viewGroup, false);
                view.setOnClickListener(this);
            }
            UserDetailItemView userDetailItemView = (UserDetailItemView) view;
            String name = getName(this.mContext, item);
            if (item.picture == null) {
                userDetailItemView.bind(name, getDrawable(this.mContext, item).mutate(), item.resolveId());
            } else {
                CircleFramedDrawable circleFramedDrawable = new CircleFramedDrawable(item.picture, (int) this.mContext.getResources().getDimension(C0009R$dimen.kg_framed_avatar_size));
                circleFramedDrawable.setColorFilter(item.isSwitchToEnabled ? null : UserSwitcherController.BaseUserAdapter.getDisabledUserAvatarColorFilter());
                userDetailItemView.bind(name, circleFramedDrawable, item.info.id);
            }
            userDetailItemView.setActivated(item.isCurrent);
            userDetailItemView.setDisabledByAdmin(item.isDisabledByAdmin);
            userDetailItemView.setEnabled(item.isSwitchToEnabled);
            userDetailItemView.setAlpha(userDetailItemView.isEnabled() ? 1.0f : 0.38f);
            if (item.isCurrent) {
                this.mCurrentUserView = userDetailItemView;
            }
            userDetailItemView.setTag(item);
            return userDetailItemView;
        }

        private static Drawable getDrawable(Context context, UserSwitcherController.UserRecord userRecord) {
            int i;
            Drawable iconDrawable = UserSwitcherController.BaseUserAdapter.getIconDrawable(context, userRecord);
            if (userRecord.isCurrent) {
                i = C0008R$color.kg_user_switcher_selected_avatar_icon_color;
            } else if (!userRecord.isSwitchToEnabled) {
                i = C0008R$color.GM2_grey_600;
            } else {
                i = C0008R$color.kg_user_switcher_avatar_icon_color;
            }
            iconDrawable.setTint(context.getResources().getColor(i, context.getTheme()));
            if (!userRecord.isCurrent) {
                return iconDrawable;
            }
            return new LayerDrawable(new Drawable[]{context.getDrawable(C0010R$drawable.bg_avatar_selected), iconDrawable});
        }

        public void onClick(View view) {
            UserSwitcherController.UserRecord userRecord = (UserSwitcherController.UserRecord) view.getTag();
            if (userRecord.isCurrent && !userRecord.isGuest) {
                this.mKeyguardUserSwitcher.hideIfNotSimple(true);
            } else if (userRecord.isSwitchToEnabled) {
                if (!userRecord.isAddUser && !userRecord.isRestricted && !userRecord.isDisabledByAdmin) {
                    View view2 = this.mCurrentUserView;
                    if (view2 != null) {
                        view2.setActivated(false);
                    }
                    view.setActivated(true);
                }
                switchTo(userRecord);
            }
        }
    }

    public static class Container extends FrameLayout {
        private KeyguardUserSwitcher mKeyguardUserSwitcher;

        public Container(Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
            setClipChildren(false);
        }

        public void setKeyguardUserSwitcher(KeyguardUserSwitcher keyguardUserSwitcher) {
            this.mKeyguardUserSwitcher = keyguardUserSwitcher;
        }

        public boolean onTouchEvent(MotionEvent motionEvent) {
            KeyguardUserSwitcher keyguardUserSwitcher = this.mKeyguardUserSwitcher;
            if (keyguardUserSwitcher == null || keyguardUserSwitcher.isAnimating()) {
                return false;
            }
            this.mKeyguardUserSwitcher.hideIfNotSimple(true);
            return false;
        }
    }
}
