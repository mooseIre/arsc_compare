package com.android.systemui.miui.statusbar.phone;

import android.app.ActivityManager;
import android.app.ActivityManagerCompat;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.UserHandle;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.TextView;
import com.android.internal.telephony.Call;
import com.android.systemui.Dependency;
import com.android.systemui.Interpolators;
import com.android.systemui.miui.statusbar.InCallUtils;
import com.android.systemui.miui.statusbar.phone.MiuiStatusBarPromptController;
import com.android.systemui.miui.widget.LimitedSizeStyleSavedView;
import com.android.systemui.plugins.R;
import com.android.systemui.recents.misc.RecentsPushEventHelper;
import com.android.systemui.statusbar.Icons;
import com.android.systemui.statusbar.phone.KeyguardStatusBarView;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.policy.DarkIconDispatcherHelper;
import miui.view.MiuiHapticFeedbackConstants;

class MiuiStatusBarPromptImpl implements IMiuiStatusBarPrompt {
    private StatusBar mBar;
    private ImageView mCallStateIcon;
    private Chronometer mCallTimer;
    private int mClickActionType = -1;
    private Context mContext;
    private int mDisableFlags = 0;
    private View mDriveModeBg;
    private boolean mDriveModeMask;
    private TextView mDriveModeTextView;
    private boolean mIsSosTypeImage;
    private LimitedSizeStyleSavedView mMiniStateViews;
    private MiuiStatusBarPromptController mMiuiStatusBarPrompt;
    private ImageView mNotchRecorderImage;
    /* access modifiers changed from: private */
    public View mParentView;
    private Chronometer mRecordTimer;
    private LimitedSizeStyleSavedView mReturnToDriveModeView;
    private LimitedSizeStyleSavedView mReturnToInCallScreenButton;
    private TextView mReturnToMultiModeView;
    private LimitedSizeStyleSavedView mReturnToRecorderView;
    private LimitedSizeStyleSavedView mSafepayStatusBar;
    private TextView mSafepayStatusBarText;
    private View mSosStatusBar;
    private ViewGroup mStandardStateViews;
    private View mStateView;
    private boolean mTouchExpanded;

    private void clearReturnToInCallScreenButtonIcons() {
    }

    public void initReturnToInCallScreenButtonIcons() {
    }

    public MiuiStatusBarPromptImpl(StatusBar statusBar, View view, int i) {
        this.mBar = statusBar;
        this.mDisableFlags = i;
        this.mParentView = view;
        this.mContext = this.mParentView.getContext();
        this.mMiuiStatusBarPrompt = (MiuiStatusBarPromptController) Dependency.get(MiuiStatusBarPromptController.class);
        updateViewWidth(Integer.MAX_VALUE);
    }

    /* access modifiers changed from: package-private */
    public void updateViewWidth(int i) {
        this.mMiniStateViews = (LimitedSizeStyleSavedView) findViewById(R.id.miniStateViews);
        this.mMiniStateViews.setMaxWidth(i);
        this.mStandardStateViews = (ViewGroup) findViewById(R.id.standardStateViews);
        this.mReturnToInCallScreenButton = (LimitedSizeStyleSavedView) findViewById(R.id.notch_call);
        this.mReturnToInCallScreenButton.setMaxWidth(i);
        this.mCallStateIcon = (ImageView) this.mReturnToInCallScreenButton.findViewById(R.id.image);
        this.mCallTimer = (Chronometer) this.mReturnToInCallScreenButton.findViewById(R.id.timer);
        this.mReturnToDriveModeView = (LimitedSizeStyleSavedView) findViewById(R.id.notch_drivemode);
        this.mReturnToDriveModeView.setMaxWidth(i);
        this.mDriveModeTextView = (TextView) findViewById(R.id.driveModeTipText_notch);
        this.mReturnToMultiModeView = (TextView) findViewById(R.id.notch_multi);
        this.mDriveModeBg = findViewById(R.id.drivemodebg);
        this.mReturnToRecorderView = (LimitedSizeStyleSavedView) findViewById(R.id.notch_recorder);
        this.mReturnToRecorderView.setMaxWidth(i);
        this.mNotchRecorderImage = (ImageView) this.mReturnToRecorderView.findViewById(R.id.image);
        this.mRecordTimer = (Chronometer) this.mReturnToRecorderView.findViewById(R.id.timer);
        if (!isSafePayDisabled()) {
            this.mSafepayStatusBar = (LimitedSizeStyleSavedView) findViewById(R.id.notch_safe);
            this.mSafepayStatusBar.setMaxWidth(i);
            this.mSafepayStatusBarText = (TextView) this.mSafepayStatusBar.findViewById(R.id.title);
        }
        this.mSosStatusBar = (LimitedSizeStyleSavedView) findViewById(R.id.notch_sos);
        ((LimitedSizeStyleSavedView) this.mSosStatusBar).setMaxWidth(i);
    }

    public void cancelState() {
        setViewVisibilty(this.mStandardStateViews, 8, false);
        setViewVisibilty(this.mMiniStateViews, 8, false);
    }

    public void updateTouchArea(final boolean z, final int i) {
        if (i != 0) {
            View view = this.mParentView;
            if (!(view instanceof KeyguardStatusBarView) && this.mTouchExpanded != z) {
                final View view2 = (View) view.getParent();
                this.mTouchExpanded = z;
                view2.post(new Runnable() {
                    public void run() {
                        Rect rect = new Rect();
                        MiuiStatusBarPromptImpl.this.mParentView.getHitRect(rect);
                        if (z) {
                            rect.bottom += i;
                        }
                        view2.setTouchDelegate(new TouchDelegate(rect, MiuiStatusBarPromptImpl.this.mParentView));
                    }
                });
            }
        }
    }

    public void updateStateViews(String str) {
        if (str != null) {
            boolean z = !str.contains("legacy_") && !str.equals("legacy_nromal");
            MiuiStatusBarPromptController.State state = this.mMiuiStatusBarPrompt.getState(str);
            if (!z) {
                cancelState();
            } else if (state != null) {
                RemoteViews remoteViews = state.miniState;
                final LimitedSizeStyleSavedView limitedSizeStyleSavedView = this.mMiniStateViews;
                setViewVisibilty(limitedSizeStyleSavedView, 0, false);
                final View apply = remoteViews.apply(this.mContext, limitedSizeStyleSavedView);
                limitedSizeStyleSavedView.removeAllViews();
                limitedSizeStyleSavedView.addView(apply);
                limitedSizeStyleSavedView.post(new Runnable() {
                    public void run() {
                        ViewGroup.LayoutParams layoutParams = apply.getLayoutParams();
                        layoutParams.width = limitedSizeStyleSavedView.getWidth();
                        apply.setLayoutParams(layoutParams);
                    }
                });
                this.mStateView = apply;
                apply.setClickable(false);
            }
        }
    }

    public void showReturnToInCallScreenButton(String str, long j) {
        Drawable background = this.mReturnToInCallScreenButton.getBackground();
        if (Call.State.HOLDING.toString().equals(str)) {
            background.setColorFilter(this.mContext.getResources().getColor(R.color.notch_call_color_yellow), PorterDuff.Mode.SRC_IN);
            this.mReturnToInCallScreenButton.setBackground(background);
            this.mCallStateIcon.setImageResource(R.drawable.status_bar_ic_return_to_incall_screen_pause);
        } else {
            background.setColorFilter(this.mContext.getResources().getColor(R.color.notch_call_color_green), PorterDuff.Mode.SRC_IN);
            this.mReturnToInCallScreenButton.setBackground(background);
            this.mCallStateIcon.setImageResource(R.drawable.status_bar_ic_return_to_incall_screen_normal);
        }
        if (!Call.State.ACTIVE.toString().equals(str)) {
            this.mCallTimer.stop();
            if (Call.State.HOLDING.toString().equals(str)) {
                this.mCallTimer.setBase(j);
            } else {
                this.mCallTimer.setBase(SystemClock.elapsedRealtime());
            }
        } else {
            this.mCallTimer.setBase(j);
            this.mCallTimer.start();
        }
        if (this.mReturnToInCallScreenButton.getVisibility() == 8) {
            initReturnToInCallScreenButtonIcons();
        }
        setViewVisibilty(this.mReturnToInCallScreenButton, 0, false);
    }

    public void hideReturnToInCallScreenButton() {
        this.mCallTimer.stop();
        this.mCallTimer.setBase(SystemClock.elapsedRealtime());
        setViewVisibilty(this.mReturnToInCallScreenButton, 8, false);
        clearReturnToInCallScreenButtonIcons();
    }

    public void makeReturnToInCallScreenButtonVisible() {
        setViewVisibilty(this.mReturnToInCallScreenButton, 0, false);
    }

    public void makeReturnToInCallScreenButtonGone() {
        setViewVisibilty(this.mReturnToInCallScreenButton, 8, false);
    }

    public void showReturnToMulti(boolean z) {
        if (!isMultiWindowDisabled()) {
            if (z) {
                Drawable background = this.mReturnToMultiModeView.getBackground();
                background.setColorFilter(this.mContext.getResources().getColor(R.color.notch_call_color_pink), PorterDuff.Mode.SRC_IN);
                this.mReturnToMultiModeView.setBackground(background);
                setViewVisibilty(this.mReturnToMultiModeView, 0, false);
                return;
            }
            setViewVisibilty(this.mReturnToMultiModeView, 8, false);
        }
    }

    public void showReturnToInCall(boolean z) {
        if (z) {
            setViewVisibilty(this.mReturnToInCallScreenButton, 0, false);
        } else {
            setViewVisibilty(this.mReturnToInCallScreenButton, 8, false);
        }
    }

    public void showReturnToDriveMode(boolean z) {
        if (!isDriveModeDisabled()) {
            if (z) {
                setViewVisibilty(this.mReturnToDriveModeView, 0, false);
                this.mDriveModeBg.setVisibility(0);
                return;
            }
            setViewVisibilty(this.mReturnToDriveModeView, 8, false);
            this.mDriveModeBg.setVisibility(8);
        }
    }

    public void showReturnToDriveModeView(boolean z, boolean z2) {
        if (!isDriveModeDisabled()) {
            this.mDriveModeMask = z2;
            if (z2) {
                this.mDriveModeTextView.setText(R.string.drive_mode_tip_idle_notch);
            } else {
                this.mDriveModeTextView.setText(R.string.drive_mode_tip_notch);
            }
            int i = 8;
            setViewVisibilty(this.mReturnToDriveModeView, z ? 0 : 8, false);
            if (z) {
                View view = this.mDriveModeBg;
                if (z && z2) {
                    i = 0;
                }
                view.setVisibility(i);
            }
        }
    }

    public void showReturnToRecorderView(boolean z) {
        setViewVisibilty(this.mReturnToRecorderView, z ? 0 : 8, false);
    }

    public void hideReturnToRecorderView() {
        this.mNotchRecorderImage.setImageDrawable((Drawable) null);
        this.mRecordTimer.stop();
    }

    public void showReturnToRecorderView(String str, boolean z, long j) {
        Drawable background = this.mReturnToRecorderView.getBackground();
        background.setColorFilter(this.mContext.getColor(R.color.notch_recorder_color), PorterDuff.Mode.SRC_IN);
        this.mReturnToRecorderView.setBackground(background);
        this.mNotchRecorderImage.setImageResource(R.drawable.status_bar_recorder_icon);
        this.mRecordTimer.stop();
        this.mRecordTimer.setBase(SystemClock.elapsedRealtime() - j);
        if (z) {
            this.mRecordTimer.start();
        }
    }

    public void showReturnToSafeBar(boolean z) {
        if (!isSafePayDisabled()) {
            setViewVisibilty(this.mSafepayStatusBar, z ? 0 : 8, false);
        }
    }

    public void showSafePayStatusBar(int i, Bundle bundle) {
        int i2;
        if (!isSafePayDisabled()) {
            Drawable background = this.mSafepayStatusBar.getBackground();
            if (i == 2) {
                i2 = R.color.notch_safe_color;
                this.mSafepayStatusBarText.setText(R.string.prompt_safe);
            } else if (i == 3) {
                i2 = R.color.notch_danger_color;
                this.mSafepayStatusBarText.setText(R.string.prompt_danger);
            } else if (i != 4) {
                i2 = -1;
            } else {
                i2 = R.color.notch_failure;
                this.mSafepayStatusBarText.setText(R.string.prompt_unknown);
            }
            if (i2 != -1) {
                background.setColorFilter(this.mContext.getResources().getColor(i2), PorterDuff.Mode.SRC_IN);
                this.mSafepayStatusBar.setBackground(background);
            }
        }
    }

    public void hideSafePayStatusBar() {
        if (!isSafePayDisabled()) {
            setViewVisibilty(this.mSafepayStatusBar, 8, false);
        }
    }

    public void showSosStatusBar() {
        if (!isSosDisabled()) {
            if (!this.mIsSosTypeImage) {
                Drawable background = this.mSosStatusBar.getBackground();
                background.setColorFilter(this.mContext.getResources().getColor(R.color.notch_sos_status_bar_bg), PorterDuff.Mode.SRC_IN);
                this.mSosStatusBar.setBackground(background);
                return;
            }
            setViewVisibilty(this.mSosStatusBar, 0, false);
        }
    }

    public void hideSosStatusBar() {
        if (!isSosDisabled()) {
            setViewVisibilty(this.mSosStatusBar, 8, false);
        }
    }

    public void showReturnToSosBar(boolean z) {
        if (!isSosDisabled()) {
            setViewVisibilty(this.mSosStatusBar, z ? 0 : 8, false);
        }
    }

    public void setSosTypeImage() {
        this.mIsSosTypeImage = true;
        this.mSosStatusBar = findViewById(R.id.notch_sos_image);
    }

    public void updateSosImageDark(boolean z, Rect rect, float f) {
        if (this.mIsSosTypeImage) {
            View view = this.mSosStatusBar;
            if (view instanceof ImageView) {
                ImageView imageView = (ImageView) view;
                imageView.setImageResource(Icons.get(Integer.valueOf(R.drawable.stat_sys_sos), DarkIconDispatcherHelper.inDarkMode(rect, imageView, f)));
            }
        }
    }

    public boolean blockClickAction() {
        LimitedSizeStyleSavedView limitedSizeStyleSavedView;
        TextView textView;
        if (!this.mParentView.isShown()) {
            return false;
        }
        if (isMultiWindowDisabled() || (textView = this.mReturnToMultiModeView) == null || textView.getVisibility() != 0) {
            LimitedSizeStyleSavedView limitedSizeStyleSavedView2 = this.mReturnToInCallScreenButton;
            if (limitedSizeStyleSavedView2 != null && limitedSizeStyleSavedView2.getVisibility() == 0) {
                this.mClickActionType = 1;
                return true;
            } else if (isDriveModeDisabled() || (limitedSizeStyleSavedView = this.mReturnToDriveModeView) == null || limitedSizeStyleSavedView.getVisibility() != 0) {
                LimitedSizeStyleSavedView limitedSizeStyleSavedView3 = this.mReturnToRecorderView;
                if (limitedSizeStyleSavedView3 == null || limitedSizeStyleSavedView3.getVisibility() != 0) {
                    View view = this.mSosStatusBar;
                    if (view != null && view.getVisibility() == 0) {
                        this.mClickActionType = 4;
                        return true;
                    } else if (this.mStandardStateViews.getVisibility() == 0 || this.mMiniStateViews.getVisibility() == 0) {
                        this.mClickActionType = 5;
                        return true;
                    } else {
                        this.mClickActionType = -1;
                        return false;
                    }
                } else {
                    this.mClickActionType = 3;
                    return true;
                }
            } else {
                this.mClickActionType = 2;
                return true;
            }
        } else {
            this.mClickActionType = 0;
            return true;
        }
    }

    public void handleClickAction() {
        View view;
        int i = this.mClickActionType;
        if (i == 0) {
            try {
                ActivityManager.StackInfo stackInfo = ActivityManagerCompat.getStackInfo(3, 3, 0);
                if (stackInfo != null && this.mBar != null) {
                    this.mBar.showRecentApps(false, false);
                    RecentsPushEventHelper.sendClickStatusBarToReturnMultiWindowEvent(stackInfo.topActivity + "");
                }
            } catch (Exception unused) {
            }
        } else if (i == 1) {
            InCallUtils.goInCallScreen(this.mContext);
        } else {
            if (i == 3) {
                Intent intent = new Intent("android.intent.action.MAIN", (Uri) null);
                intent.setFlags(335806464);
                intent.setClassName("com.android.soundrecorder", "com.android.soundrecorder.SoundRecorder");
                this.mContext.startActivityAsUser(intent, UserHandle.CURRENT);
            }
            if (this.mClickActionType == 4) {
                Intent intent2 = new Intent("miui.intent.action.EXIT_SOS");
                intent2.setPackage("com.android.settings");
                intent2.addFlags(MiuiHapticFeedbackConstants.FLAG_MIUI_HAPTIC_TAP_NORMAL);
                this.mContext.startActivity(intent2);
            }
            if (this.mClickActionType == 5 && (view = this.mStateView) != null) {
                view.performClick();
            }
        }
    }

    public Context getContext() {
        return this.mContext;
    }

    private void setViewVisibilty(final View view, final int i, boolean z) {
        view.animate().cancel();
        if (i != 8) {
            view.setVisibility(0);
        }
        view.animate().alpha(i == 0 ? 1.0f : 0.0f).setDuration(z ? 320 : 0).setInterpolator(i == 0 ? Interpolators.ALPHA_IN : Interpolators.ALPHA_OUT).withEndAction(new Runnable() {
            public void run() {
                if (i == 8) {
                    view.setVisibility(8);
                }
            }
        });
    }

    private View findViewById(int i) {
        return this.mParentView.findViewById(i);
    }

    private boolean isDriveModeDisabled() {
        return (this.mDisableFlags & 1) != 0;
    }

    private boolean isSafePayDisabled() {
        return (this.mDisableFlags & 2) != 0;
    }

    private boolean isSosDisabled() {
        return (this.mDisableFlags & 8) != 0;
    }

    private boolean isMultiWindowDisabled() {
        return (this.mDisableFlags & 4) != 0;
    }
}
