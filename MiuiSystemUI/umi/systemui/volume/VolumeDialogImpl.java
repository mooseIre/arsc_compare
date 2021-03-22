package com.android.systemui.volume;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioSystem;
import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.provider.Settings;
import android.text.InputFilter;
import android.util.Log;
import android.util.Slog;
import android.util.SparseBooleanArray;
import android.view.ContextThemeWrapper;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.ViewStub;
import android.view.Window;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import com.android.settingslib.Utils;
import com.android.settingslib.volume.Util;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0016R$integer;
import com.android.systemui.C0017R$layout;
import com.android.systemui.C0021R$string;
import com.android.systemui.C0022R$style;
import com.android.systemui.Dependency;
import com.android.systemui.Prefs;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.VolumeDialog;
import com.android.systemui.plugins.VolumeDialogController;
import com.android.systemui.statusbar.policy.AccessibilityManagerWrapper;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.statusbar.policy.DeviceProvisionedController;
import com.android.systemui.volume.CaptionsToggleImageButton;
import com.android.systemui.volume.VolumeDialogImpl;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class VolumeDialogImpl implements VolumeDialog, ConfigurationController.ConfigurationListener {
    private static final String TAG = Util.logTag(VolumeDialogImpl.class);
    private final Accessibility mAccessibility = new Accessibility();
    private final AccessibilityManagerWrapper mAccessibilityMgr;
    private int mActiveStream;
    private final ActivityManager mActivityManager;
    private boolean mAutomute = true;
    private boolean mConfigChanged = false;
    private ConfigurableTexts mConfigurableTexts;
    private final Context mContext;
    private final VolumeDialogController mController;
    private final VolumeDialogController.Callbacks mControllerCallbackH = new VolumeDialogController.Callbacks() {
        /* class com.android.systemui.volume.VolumeDialogImpl.AnonymousClass3 */

        @Override // com.android.systemui.plugins.VolumeDialogController.Callbacks
        public void onShowRequested(int i) {
            VolumeDialogImpl.this.showH(i);
        }

        @Override // com.android.systemui.plugins.VolumeDialogController.Callbacks
        public void onDismissRequested(int i) {
            VolumeDialogImpl.this.dismissH(i);
        }

        @Override // com.android.systemui.plugins.VolumeDialogController.Callbacks
        public void onScreenOff() {
            VolumeDialogImpl.this.dismissH(4);
        }

        @Override // com.android.systemui.plugins.VolumeDialogController.Callbacks
        public void onStateChanged(VolumeDialogController.State state) {
            VolumeDialogImpl.this.onStateChangedH(state);
        }

        @Override // com.android.systemui.plugins.VolumeDialogController.Callbacks
        public void onLayoutDirectionChanged(int i) {
            VolumeDialogImpl.this.mDialogView.setLayoutDirection(i);
        }

        @Override // com.android.systemui.plugins.VolumeDialogController.Callbacks
        public void onConfigurationChanged() {
            VolumeDialogImpl.this.mDialog.dismiss();
            VolumeDialogImpl.this.mConfigChanged = true;
        }

        @Override // com.android.systemui.plugins.VolumeDialogController.Callbacks
        public void onShowVibrateHint() {
            if (VolumeDialogImpl.this.mSilentMode) {
                VolumeDialogImpl.this.mController.setRingerMode(0, false);
            }
        }

        @Override // com.android.systemui.plugins.VolumeDialogController.Callbacks
        public void onShowSilentHint() {
            if (VolumeDialogImpl.this.mSilentMode) {
                VolumeDialogImpl.this.mController.setRingerMode(2, false);
            }
        }

        @Override // com.android.systemui.plugins.VolumeDialogController.Callbacks
        public void onShowSafetyWarning(int i) {
            VolumeDialogImpl.this.showSafetyWarningH(i);
        }

        @Override // com.android.systemui.plugins.VolumeDialogController.Callbacks
        public void onAccessibilityModeChanged(Boolean bool) {
            VolumeDialogImpl.this.mShowA11yStream = bool == null ? false : bool.booleanValue();
            VolumeRow activeRow = VolumeDialogImpl.this.getActiveRow();
            if (VolumeDialogImpl.this.mShowA11yStream || 10 != activeRow.stream) {
                VolumeDialogImpl.this.updateRowsH(activeRow);
            } else {
                VolumeDialogImpl.this.dismissH(7);
            }
        }

        @Override // com.android.systemui.plugins.VolumeDialogController.Callbacks
        public void onCaptionComponentStateChanged(Boolean bool, Boolean bool2) {
            VolumeDialogImpl.this.updateODICaptionsH(bool.booleanValue(), bool2.booleanValue());
        }
    };
    private final DeviceProvisionedController mDeviceProvisionedController;
    private CustomDialog mDialog;
    private ViewGroup mDialogRowsView;
    private ViewGroup mDialogView;
    private final SparseBooleanArray mDynamic = new SparseBooleanArray();
    private final H mHandler = new H();
    private boolean mHasSeenODICaptionsTooltip;
    private boolean mHovering = false;
    private boolean mIsAnimatingDismiss = false;
    private final KeyguardManager mKeyguard;
    private CaptionsToggleImageButton mODICaptionsIcon;
    private View mODICaptionsTooltipView = null;
    private ViewStub mODICaptionsTooltipViewStub;
    private ViewGroup mODICaptionsView;
    private int mPrevActiveStream;
    private ViewGroup mRinger;
    private ImageButton mRingerIcon;
    private final List<VolumeRow> mRows = new ArrayList();
    private SafetyWarningDialog mSafetyWarning;
    private final Object mSafetyWarningLock = new Object();
    private ImageButton mSettingsIcon;
    private View mSettingsView;
    private boolean mShowA11yStream;
    private boolean mShowActiveStreamOnly;
    private boolean mShowing;
    private boolean mSilentMode = true;
    private VolumeDialogController.State mState;
    private Window mWindow;
    private FrameLayout mZenIcon;

    public VolumeDialogImpl(Context context) {
        this.mContext = new ContextThemeWrapper(context, C0022R$style.qs_theme);
        this.mController = (VolumeDialogController) Dependency.get(VolumeDialogController.class);
        this.mKeyguard = (KeyguardManager) this.mContext.getSystemService("keyguard");
        this.mActivityManager = (ActivityManager) this.mContext.getSystemService("activity");
        this.mAccessibilityMgr = (AccessibilityManagerWrapper) Dependency.get(AccessibilityManagerWrapper.class);
        this.mDeviceProvisionedController = (DeviceProvisionedController) Dependency.get(DeviceProvisionedController.class);
        this.mShowActiveStreamOnly = showActiveStreamOnly();
        this.mHasSeenODICaptionsTooltip = Prefs.getBoolean(context, "HasSeenODICaptionsTooltip", false);
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onUiModeChanged() {
        this.mContext.getTheme().applyStyle(this.mContext.getThemeResId(), true);
    }

    @Override // com.android.systemui.plugins.VolumeDialog
    public void init(int i, VolumeDialog.Callback callback) {
        initDialog();
        this.mAccessibility.init();
        this.mController.addCallback(this.mControllerCallbackH, this.mHandler);
        this.mController.getState();
        ((ConfigurationController) Dependency.get(ConfigurationController.class)).addCallback(this);
    }

    @Override // com.android.systemui.plugins.VolumeDialog
    public void destroy() {
        this.mController.removeCallback(this.mControllerCallbackH);
        this.mHandler.removeCallbacksAndMessages(null);
        ((ConfigurationController) Dependency.get(ConfigurationController.class)).removeCallback(this);
    }

    private void initDialog() {
        this.mDialog = new CustomDialog(this.mContext);
        this.mConfigurableTexts = new ConfigurableTexts(this.mContext);
        this.mHovering = false;
        this.mShowing = false;
        Window window = this.mDialog.getWindow();
        this.mWindow = window;
        window.requestFeature(1);
        this.mWindow.setBackgroundDrawable(new ColorDrawable(0));
        this.mWindow.clearFlags(65538);
        this.mWindow.addFlags(17563688);
        this.mWindow.setType(2020);
        this.mWindow.setWindowAnimations(16973828);
        WindowManager.LayoutParams attributes = this.mWindow.getAttributes();
        attributes.format = -3;
        attributes.setTitle(VolumeDialogImpl.class.getSimpleName());
        attributes.windowAnimations = -1;
        attributes.gravity = this.mContext.getResources().getInteger(C0016R$integer.volume_dialog_gravity);
        this.mWindow.setAttributes(attributes);
        this.mWindow.setLayout(-2, -2);
        this.mDialog.setContentView(C0017R$layout.volume_dialog);
        ViewGroup viewGroup = (ViewGroup) this.mDialog.findViewById(C0015R$id.volume_dialog);
        this.mDialogView = viewGroup;
        viewGroup.setAlpha(0.0f);
        this.mDialog.setCanceledOnTouchOutside(true);
        this.mDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            /* class com.android.systemui.volume.$$Lambda$VolumeDialogImpl$8BZhTIdOE2rPYfFa5HbcUDCtXeM */

            public final void onShow(DialogInterface dialogInterface) {
                VolumeDialogImpl.this.lambda$initDialog$1$VolumeDialogImpl(dialogInterface);
            }
        });
        this.mDialogView.setOnHoverListener(new View.OnHoverListener() {
            /* class com.android.systemui.volume.$$Lambda$VolumeDialogImpl$T52d0W13mYvykk6ORgbytqfZsps */

            public final boolean onHover(View view, MotionEvent motionEvent) {
                return VolumeDialogImpl.this.lambda$initDialog$2$VolumeDialogImpl(view, motionEvent);
            }
        });
        this.mDialogRowsView = (ViewGroup) this.mDialog.findViewById(C0015R$id.volume_dialog_rows);
        ViewGroup viewGroup2 = (ViewGroup) this.mDialog.findViewById(C0015R$id.ringer);
        this.mRinger = viewGroup2;
        if (viewGroup2 != null) {
            this.mRingerIcon = (ImageButton) viewGroup2.findViewById(C0015R$id.ringer_icon);
            this.mZenIcon = (FrameLayout) this.mRinger.findViewById(C0015R$id.dnd_icon);
        }
        ViewGroup viewGroup3 = (ViewGroup) this.mDialog.findViewById(C0015R$id.odi_captions);
        this.mODICaptionsView = viewGroup3;
        if (viewGroup3 != null) {
            this.mODICaptionsIcon = (CaptionsToggleImageButton) viewGroup3.findViewById(C0015R$id.odi_captions_icon);
        }
        ViewStub viewStub = (ViewStub) this.mDialog.findViewById(C0015R$id.odi_captions_tooltip_stub);
        this.mODICaptionsTooltipViewStub = viewStub;
        if (this.mHasSeenODICaptionsTooltip && viewStub != null) {
            this.mDialogView.removeView(viewStub);
            this.mODICaptionsTooltipViewStub = null;
        }
        this.mSettingsView = this.mDialog.findViewById(C0015R$id.settings_container);
        this.mSettingsIcon = (ImageButton) this.mDialog.findViewById(C0015R$id.settings);
        if (this.mRows.isEmpty()) {
            if (!AudioSystem.isSingleVolume(this.mContext)) {
                int i = C0013R$drawable.ic_volume_accessibility;
                addRow(10, i, i, true, false);
            }
            addRow(3, C0013R$drawable.ic_volume_media, C0013R$drawable.ic_volume_media_mute, true, true);
            if (!AudioSystem.isSingleVolume(this.mContext)) {
                addRow(2, C0013R$drawable.ic_volume_ringer, C0013R$drawable.ic_volume_ringer_mute, true, false);
                addRow(4, C0013R$drawable.ic_alarm, C0013R$drawable.ic_volume_alarm_mute, true, false);
                addRow(0, 17302808, 17302808, false, false);
                int i2 = C0013R$drawable.ic_volume_bt_sco;
                addRow(6, i2, i2, false, false);
                addRow(1, C0013R$drawable.ic_volume_system, C0013R$drawable.ic_volume_system_mute, false, false);
            }
        } else {
            addExistingRows();
        }
        updateRowsH(getActiveRow());
        initRingerH();
        initSettingsH();
        initODICaptionsH();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$initDialog$1 */
    public /* synthetic */ void lambda$initDialog$1$VolumeDialogImpl(DialogInterface dialogInterface) {
        if (!isLandscape()) {
            ViewGroup viewGroup = this.mDialogView;
            viewGroup.setTranslationX(((float) viewGroup.getWidth()) / 2.0f);
        }
        this.mDialogView.setAlpha(0.0f);
        this.mDialogView.animate().alpha(1.0f).translationX(0.0f).setDuration(300).setInterpolator(new SystemUIInterpolators$LogDecelerateInterpolator()).withEndAction(new Runnable() {
            /* class com.android.systemui.volume.$$Lambda$VolumeDialogImpl$vBH_Cy2LsLvfluWDg0W4IzJ1dm8 */

            public final void run() {
                VolumeDialogImpl.this.lambda$initDialog$0$VolumeDialogImpl();
            }
        }).start();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$initDialog$0 */
    public /* synthetic */ void lambda$initDialog$0$VolumeDialogImpl() {
        ImageButton imageButton;
        if (!Prefs.getBoolean(this.mContext, "TouchedRingerToggle", false) && (imageButton = this.mRingerIcon) != null) {
            imageButton.postOnAnimationDelayed(getSinglePressFor(imageButton), 1500);
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$initDialog$2 */
    public /* synthetic */ boolean lambda$initDialog$2$VolumeDialogImpl(View view, MotionEvent motionEvent) {
        int actionMasked = motionEvent.getActionMasked();
        this.mHovering = actionMasked == 9 || actionMasked == 7;
        rescheduleTimeoutH();
        return true;
    }

    private int getAlphaAttr(int i) {
        TypedArray obtainStyledAttributes = this.mContext.obtainStyledAttributes(new int[]{i});
        float f = obtainStyledAttributes.getFloat(0, 0.0f);
        obtainStyledAttributes.recycle();
        return (int) (f * 255.0f);
    }

    private boolean isLandscape() {
        return this.mContext.getResources().getConfiguration().orientation == 2;
    }

    public void setStreamImportant(int i, boolean z) {
        this.mHandler.obtainMessage(5, i, z ? 1 : 0).sendToTarget();
    }

    public void setAutomute(boolean z) {
        if (this.mAutomute != z) {
            this.mAutomute = z;
            this.mHandler.sendEmptyMessage(4);
        }
    }

    public void setSilentMode(boolean z) {
        if (this.mSilentMode != z) {
            this.mSilentMode = z;
            this.mHandler.sendEmptyMessage(4);
        }
    }

    private void addRow(int i, int i2, int i3, boolean z, boolean z2) {
        addRow(i, i2, i3, z, z2, false);
    }

    private void addRow(int i, int i2, int i3, boolean z, boolean z2, boolean z3) {
        if (D.BUG) {
            String str = TAG;
            Slog.d(str, "Adding row for stream " + i);
        }
        VolumeRow volumeRow = new VolumeRow();
        initRow(volumeRow, i, i2, i3, z, z2);
        this.mDialogRowsView.addView(volumeRow.view);
        this.mRows.add(volumeRow);
    }

    private void addExistingRows() {
        int size = this.mRows.size();
        for (int i = 0; i < size; i++) {
            VolumeRow volumeRow = this.mRows.get(i);
            initRow(volumeRow, volumeRow.stream, volumeRow.iconRes, volumeRow.iconMuteRes, volumeRow.important, volumeRow.defaultStream);
            this.mDialogRowsView.addView(volumeRow.view);
            updateVolumeRowH(volumeRow);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private VolumeRow getActiveRow() {
        for (VolumeRow volumeRow : this.mRows) {
            if (volumeRow.stream == this.mActiveStream) {
                return volumeRow;
            }
        }
        for (VolumeRow volumeRow2 : this.mRows) {
            if (volumeRow2.stream == 3) {
                return volumeRow2;
            }
        }
        return this.mRows.get(0);
    }

    private VolumeRow findRow(int i) {
        for (VolumeRow volumeRow : this.mRows) {
            if (volumeRow.stream == i) {
                return volumeRow;
            }
        }
        return null;
    }

    /* access modifiers changed from: private */
    public static int getImpliedLevel(SeekBar seekBar, int i) {
        int max = seekBar.getMax();
        int i2 = max / 100;
        int i3 = i2 - 1;
        if (i == 0) {
            return 0;
        }
        return i == max ? i2 : ((int) ((((float) i) / ((float) max)) * ((float) i3))) + 1;
    }

    @SuppressLint({"InflateParams"})
    private void initRow(VolumeRow volumeRow, int i, int i2, int i3, boolean z, boolean z2) {
        volumeRow.stream = i;
        volumeRow.iconRes = i2;
        volumeRow.iconMuteRes = i3;
        volumeRow.important = z;
        volumeRow.defaultStream = z2;
        volumeRow.view = this.mDialog.getLayoutInflater().inflate(C0017R$layout.volume_dialog_row, (ViewGroup) null);
        volumeRow.view.setId(volumeRow.stream);
        volumeRow.view.setTag(volumeRow);
        volumeRow.header = (TextView) volumeRow.view.findViewById(C0015R$id.volume_row_header);
        volumeRow.header.setId(volumeRow.stream * 20);
        if (i == 10) {
            volumeRow.header.setFilters(new InputFilter[]{new InputFilter.LengthFilter(13)});
        }
        volumeRow.dndIcon = (FrameLayout) volumeRow.view.findViewById(C0015R$id.dnd_icon);
        volumeRow.slider = (SeekBar) volumeRow.view.findViewById(C0015R$id.volume_row_slider);
        volumeRow.slider.setOnSeekBarChangeListener(new VolumeSeekBarChangeListener(volumeRow));
        volumeRow.anim = null;
        volumeRow.icon = (ImageButton) volumeRow.view.findViewById(C0015R$id.volume_row_icon);
        volumeRow.icon.setImageResource(i2);
        if (volumeRow.stream != 10) {
            volumeRow.icon.setOnClickListener(new View.OnClickListener(volumeRow, i) {
                /* class com.android.systemui.volume.$$Lambda$VolumeDialogImpl$I0sumSTzcnKtt5xn4YVlQQget8 */
                public final /* synthetic */ VolumeDialogImpl.VolumeRow f$1;
                public final /* synthetic */ int f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                public final void onClick(View view) {
                    VolumeDialogImpl.this.lambda$initRow$3$VolumeDialogImpl(this.f$1, this.f$2, view);
                }
            });
        } else {
            volumeRow.icon.setImportantForAccessibility(2);
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$initRow$3 */
    public /* synthetic */ void lambda$initRow$3$VolumeDialogImpl(VolumeRow volumeRow, int i, View view) {
        int i2 = 0;
        boolean z = true;
        Events.writeEvent(7, Integer.valueOf(volumeRow.stream), Integer.valueOf(volumeRow.iconState));
        this.mController.setActiveStream(volumeRow.stream);
        if (volumeRow.stream == 2) {
            boolean hasVibrator = this.mController.hasVibrator();
            if (this.mState.ringerModeInternal != 2) {
                this.mController.setRingerMode(2, false);
                if (volumeRow.ss.level == 0) {
                    this.mController.setStreamVolume(i, 1);
                }
            } else if (hasVibrator) {
                this.mController.setRingerMode(1, false);
            } else {
                if (volumeRow.ss.level != 0) {
                    z = false;
                }
                VolumeDialogController volumeDialogController = this.mController;
                if (z) {
                    i2 = volumeRow.lastAudibleLevel;
                }
                volumeDialogController.setStreamVolume(i, i2);
            }
        } else {
            if (volumeRow.ss.level == volumeRow.ss.levelMin) {
                i2 = 1;
            }
            this.mController.setStreamVolume(i, i2 != 0 ? volumeRow.lastAudibleLevel : volumeRow.ss.levelMin);
        }
        volumeRow.userAttempt = 0;
    }

    public void initSettingsH() {
        View view = this.mSettingsView;
        if (view != null) {
            view.setVisibility((!this.mDeviceProvisionedController.isCurrentUserSetup() || this.mActivityManager.getLockTaskModeState() != 0) ? 8 : 0);
        }
        ImageButton imageButton = this.mSettingsIcon;
        if (imageButton != null) {
            imageButton.setOnClickListener(new View.OnClickListener() {
                /* class com.android.systemui.volume.$$Lambda$VolumeDialogImpl$7RdQKc1FND8ZrjtxSEsHEKXSyeY */

                public final void onClick(View view) {
                    VolumeDialogImpl.this.lambda$initSettingsH$4$VolumeDialogImpl(view);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$initSettingsH$4 */
    public /* synthetic */ void lambda$initSettingsH$4$VolumeDialogImpl(View view) {
        Events.writeEvent(8, new Object[0]);
        Intent intent = new Intent("android.settings.panel.action.VOLUME");
        dismissH(5);
        ((ActivityStarter) Dependency.get(ActivityStarter.class)).startActivity(intent, true);
    }

    public void initRingerH() {
        ImageButton imageButton = this.mRingerIcon;
        if (imageButton != null) {
            imageButton.setAccessibilityLiveRegion(1);
            this.mRingerIcon.setOnClickListener(new View.OnClickListener() {
                /* class com.android.systemui.volume.$$Lambda$VolumeDialogImpl$leUR0c6hrY1TNx5XUGxhXI1EHk */

                public final void onClick(View view) {
                    VolumeDialogImpl.this.lambda$initRingerH$5$VolumeDialogImpl(view);
                }
            });
        }
        updateRingerH();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$initRingerH$5 */
    public /* synthetic */ void lambda$initRingerH$5$VolumeDialogImpl(View view) {
        Prefs.putBoolean(this.mContext, "TouchedRingerToggle", true);
        int i = 2;
        VolumeDialogController.StreamState streamState = this.mState.states.get(2);
        if (streamState != null) {
            boolean hasVibrator = this.mController.hasVibrator();
            int i2 = this.mState.ringerModeInternal;
            if (i2 == 2) {
                if (hasVibrator) {
                    i = 1;
                    Events.writeEvent(18, Integer.valueOf(i));
                    incrementManualToggleCount();
                    updateRingerH();
                    provideTouchFeedbackH(i);
                    this.mController.setRingerMode(i, false);
                    maybeShowToastH(i);
                }
            } else if (i2 != 1) {
                if (streamState.level == 0) {
                    this.mController.setStreamVolume(2, 1);
                }
                Events.writeEvent(18, Integer.valueOf(i));
                incrementManualToggleCount();
                updateRingerH();
                provideTouchFeedbackH(i);
                this.mController.setRingerMode(i, false);
                maybeShowToastH(i);
            }
            i = 0;
            Events.writeEvent(18, Integer.valueOf(i));
            incrementManualToggleCount();
            updateRingerH();
            provideTouchFeedbackH(i);
            this.mController.setRingerMode(i, false);
            maybeShowToastH(i);
        }
    }

    private void initODICaptionsH() {
        CaptionsToggleImageButton captionsToggleImageButton = this.mODICaptionsIcon;
        if (captionsToggleImageButton != null) {
            captionsToggleImageButton.setOnConfirmedTapListener(new CaptionsToggleImageButton.ConfirmedTapListener() {
                /* class com.android.systemui.volume.$$Lambda$VolumeDialogImpl$HIlX6MPuNck4Zm6cfzTdHTUxqn4 */

                @Override // com.android.systemui.volume.CaptionsToggleImageButton.ConfirmedTapListener
                public final void onConfirmedTap() {
                    VolumeDialogImpl.this.lambda$initODICaptionsH$6$VolumeDialogImpl();
                }
            }, this.mHandler);
        }
        this.mController.getCaptionsComponentState(false);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$initODICaptionsH$6 */
    public /* synthetic */ void lambda$initODICaptionsH$6$VolumeDialogImpl() {
        onCaptionIconClicked();
        Events.writeEvent(21, new Object[0]);
    }

    private void checkODICaptionsTooltip(boolean z) {
        if (!this.mHasSeenODICaptionsTooltip && !z && this.mODICaptionsTooltipViewStub != null) {
            this.mController.getCaptionsComponentState(true);
        } else if (this.mHasSeenODICaptionsTooltip && z && this.mODICaptionsTooltipView != null) {
            hideCaptionsTooltip();
        }
    }

    /* access modifiers changed from: protected */
    public void showCaptionsTooltip() {
        ViewStub viewStub;
        if (!this.mHasSeenODICaptionsTooltip && (viewStub = this.mODICaptionsTooltipViewStub) != null) {
            View inflate = viewStub.inflate();
            this.mODICaptionsTooltipView = inflate;
            inflate.findViewById(C0015R$id.dismiss).setOnClickListener(new View.OnClickListener() {
                /* class com.android.systemui.volume.$$Lambda$VolumeDialogImpl$TUvPGuqHQwDl_z3hgYr3GMVgOs */

                public final void onClick(View view) {
                    VolumeDialogImpl.this.lambda$showCaptionsTooltip$7$VolumeDialogImpl(view);
                }
            });
            this.mODICaptionsTooltipViewStub = null;
            rescheduleTimeoutH();
        }
        View view = this.mODICaptionsTooltipView;
        if (view != null) {
            view.setAlpha(0.0f);
            this.mODICaptionsTooltipView.animate().alpha(1.0f).setStartDelay(300).withEndAction(new Runnable() {
                /* class com.android.systemui.volume.$$Lambda$VolumeDialogImpl$j7bv45Q5uulCvMsn_IeT1Mv2PxI */

                public final void run() {
                    VolumeDialogImpl.this.lambda$showCaptionsTooltip$8$VolumeDialogImpl();
                }
            }).start();
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$showCaptionsTooltip$7 */
    public /* synthetic */ void lambda$showCaptionsTooltip$7$VolumeDialogImpl(View view) {
        hideCaptionsTooltip();
        Events.writeEvent(22, new Object[0]);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$showCaptionsTooltip$8 */
    public /* synthetic */ void lambda$showCaptionsTooltip$8$VolumeDialogImpl() {
        if (D.BUG) {
            Log.d(TAG, "tool:checkODICaptionsTooltip() putBoolean true");
        }
        Prefs.putBoolean(this.mContext, "HasSeenODICaptionsTooltip", true);
        this.mHasSeenODICaptionsTooltip = true;
        CaptionsToggleImageButton captionsToggleImageButton = this.mODICaptionsIcon;
        if (captionsToggleImageButton != null) {
            captionsToggleImageButton.postOnAnimation(getSinglePressFor(captionsToggleImageButton));
        }
    }

    private void hideCaptionsTooltip() {
        View view = this.mODICaptionsTooltipView;
        if (view != null && view.getVisibility() == 0) {
            this.mODICaptionsTooltipView.animate().cancel();
            this.mODICaptionsTooltipView.setAlpha(1.0f);
            this.mODICaptionsTooltipView.animate().alpha(0.0f).setStartDelay(0).setDuration(250).withEndAction(new Runnable() {
                /* class com.android.systemui.volume.$$Lambda$VolumeDialogImpl$eJIc7NaYfyZjv9kbw4RrRBwcYRI */

                public final void run() {
                    VolumeDialogImpl.this.lambda$hideCaptionsTooltip$9$VolumeDialogImpl();
                }
            }).start();
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$hideCaptionsTooltip$9 */
    public /* synthetic */ void lambda$hideCaptionsTooltip$9$VolumeDialogImpl() {
        this.mODICaptionsTooltipView.setVisibility(4);
    }

    /* access modifiers changed from: protected */
    public void tryToRemoveCaptionsTooltip() {
        if (this.mHasSeenODICaptionsTooltip && this.mODICaptionsTooltipView != null) {
            ((ViewGroup) this.mDialog.findViewById(C0015R$id.volume_dialog_container)).removeView(this.mODICaptionsTooltipView);
            this.mODICaptionsTooltipView = null;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateODICaptionsH(boolean z, boolean z2) {
        ViewGroup viewGroup = this.mODICaptionsView;
        if (viewGroup != null) {
            viewGroup.setVisibility(z ? 0 : 8);
        }
        if (z) {
            updateCaptionsIcon();
            if (z2) {
                showCaptionsTooltip();
            }
        }
    }

    private void updateCaptionsIcon() {
        boolean areCaptionsEnabled = this.mController.areCaptionsEnabled();
        if (this.mODICaptionsIcon.getCaptionsEnabled() != areCaptionsEnabled) {
            this.mHandler.post(this.mODICaptionsIcon.setCaptionsEnabled(areCaptionsEnabled));
        }
        boolean isCaptionStreamOptedOut = this.mController.isCaptionStreamOptedOut();
        if (this.mODICaptionsIcon.getOptedOut() != isCaptionStreamOptedOut) {
            this.mHandler.post(new Runnable(isCaptionStreamOptedOut) {
                /* class com.android.systemui.volume.$$Lambda$VolumeDialogImpl$lHJr2h1jrFiBPAxP01FnOgolTSg */
                public final /* synthetic */ boolean f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    VolumeDialogImpl.this.lambda$updateCaptionsIcon$10$VolumeDialogImpl(this.f$1);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$updateCaptionsIcon$10 */
    public /* synthetic */ void lambda$updateCaptionsIcon$10$VolumeDialogImpl(boolean z) {
        this.mODICaptionsIcon.setOptedOut(z);
    }

    private void onCaptionIconClicked() {
        this.mController.setCaptionsEnabled(!this.mController.areCaptionsEnabled());
        updateCaptionsIcon();
    }

    private void incrementManualToggleCount() {
        ContentResolver contentResolver = this.mContext.getContentResolver();
        Settings.Secure.putInt(contentResolver, "manual_ringer_toggle_count", Settings.Secure.getInt(contentResolver, "manual_ringer_toggle_count", 0) + 1);
    }

    private void provideTouchFeedbackH(int i) {
        VibrationEffect vibrationEffect;
        if (i == 0) {
            vibrationEffect = VibrationEffect.get(0);
        } else if (i != 2) {
            vibrationEffect = VibrationEffect.get(1);
        } else {
            this.mController.scheduleTouchFeedback();
            vibrationEffect = null;
        }
        if (vibrationEffect != null) {
            this.mController.vibrate(vibrationEffect);
        }
    }

    private void maybeShowToastH(int i) {
        int i2 = Prefs.getInt(this.mContext, "RingerGuidanceCount", 0);
        if (i2 <= 12) {
            String str = null;
            if (i == 0) {
                str = this.mContext.getString(17041536);
            } else if (i != 2) {
                str = this.mContext.getString(17041537);
            } else {
                VolumeDialogController.StreamState streamState = this.mState.states.get(2);
                if (streamState != null) {
                    str = this.mContext.getString(C0021R$string.volume_dialog_ringer_guidance_ring, Utils.formatPercentage((long) streamState.level, (long) streamState.levelMax));
                }
            }
            Toast.makeText(this.mContext, str, 0).show();
            Prefs.putInt(this.mContext, "RingerGuidanceCount", i2 + 1);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void showH(int i) {
        if (D.BUG) {
            String str = TAG;
            Log.d(str, "showH r=" + Events.SHOW_REASONS[i]);
        }
        this.mHandler.removeMessages(1);
        this.mHandler.removeMessages(2);
        rescheduleTimeoutH();
        if (this.mConfigChanged) {
            initDialog();
            this.mConfigurableTexts.update();
            this.mConfigChanged = false;
        }
        initSettingsH();
        this.mShowing = true;
        this.mIsAnimatingDismiss = false;
        this.mDialog.show();
        Events.writeEvent(0, Integer.valueOf(i), Boolean.valueOf(this.mKeyguard.isKeyguardLocked()));
        this.mController.notifyVisible(true);
        this.mController.getCaptionsComponentState(false);
        checkODICaptionsTooltip(false);
    }

    /* access modifiers changed from: protected */
    public void rescheduleTimeoutH() {
        this.mHandler.removeMessages(2);
        int computeTimeoutH = computeTimeoutH();
        H h = this.mHandler;
        h.sendMessageDelayed(h.obtainMessage(2, 3, 0), (long) computeTimeoutH);
        if (D.BUG) {
            String str = TAG;
            Log.d(str, "rescheduleTimeout " + computeTimeoutH + " " + Debug.getCaller());
        }
        this.mController.userActivity();
    }

    private int computeTimeoutH() {
        if (this.mHovering) {
            return this.mAccessibilityMgr.getRecommendedTimeoutMillis(16000, 4);
        }
        if (this.mSafetyWarning != null) {
            return this.mAccessibilityMgr.getRecommendedTimeoutMillis(5000, 6);
        }
        if (this.mHasSeenODICaptionsTooltip || this.mODICaptionsTooltipView == null) {
            return this.mAccessibilityMgr.getRecommendedTimeoutMillis(3000, 4);
        }
        return this.mAccessibilityMgr.getRecommendedTimeoutMillis(5000, 6);
    }

    /* access modifiers changed from: protected */
    public void dismissH(int i) {
        if (D.BUG) {
            String str = TAG;
            Log.d(str, "mDialog.dismiss() reason: " + Events.DISMISS_REASONS[i] + " from: " + Debug.getCaller());
        }
        this.mHandler.removeMessages(2);
        this.mHandler.removeMessages(1);
        if (!this.mIsAnimatingDismiss) {
            this.mIsAnimatingDismiss = true;
            this.mDialogView.animate().cancel();
            if (this.mShowing) {
                this.mShowing = false;
                Events.writeEvent(1, Integer.valueOf(i));
            }
            this.mDialogView.setTranslationX(0.0f);
            this.mDialogView.setAlpha(1.0f);
            ViewPropertyAnimator withEndAction = this.mDialogView.animate().alpha(0.0f).setDuration(250).setInterpolator(new SystemUIInterpolators$LogAccelerateInterpolator()).withEndAction(new Runnable() {
                /* class com.android.systemui.volume.$$Lambda$VolumeDialogImpl$DPdXKFGeK9VznmPgQ7xFJyJSxk */

                public final void run() {
                    VolumeDialogImpl.this.lambda$dismissH$12$VolumeDialogImpl();
                }
            });
            if (!isLandscape()) {
                withEndAction.translationX(((float) this.mDialogView.getWidth()) / 2.0f);
            }
            withEndAction.start();
            checkODICaptionsTooltip(true);
            this.mController.notifyVisible(false);
            synchronized (this.mSafetyWarningLock) {
                if (this.mSafetyWarning != null) {
                    if (D.BUG) {
                        Log.d(TAG, "SafetyWarning dismissed");
                    }
                    this.mSafetyWarning.dismiss();
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$dismissH$12 */
    public /* synthetic */ void lambda$dismissH$12$VolumeDialogImpl() {
        this.mHandler.postDelayed(new Runnable() {
            /* class com.android.systemui.volume.$$Lambda$VolumeDialogImpl$b6ITsqLv2inrGwKl329FqMV42GA */

            public final void run() {
                VolumeDialogImpl.this.lambda$dismissH$11$VolumeDialogImpl();
            }
        }, 50);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$dismissH$11 */
    public /* synthetic */ void lambda$dismissH$11$VolumeDialogImpl() {
        this.mDialog.dismiss();
        tryToRemoveCaptionsTooltip();
        this.mIsAnimatingDismiss = false;
    }

    private boolean showActiveStreamOnly() {
        return this.mContext.getPackageManager().hasSystemFeature("android.software.leanback") || this.mContext.getPackageManager().hasSystemFeature("android.hardware.type.television");
    }

    private boolean shouldBeVisibleH(VolumeRow volumeRow, VolumeRow volumeRow2) {
        if (volumeRow.stream == volumeRow2.stream) {
            return true;
        }
        if (this.mShowActiveStreamOnly) {
            return false;
        }
        if (volumeRow.stream == 10) {
            return this.mShowA11yStream;
        }
        if (volumeRow2.stream == 10 && volumeRow.stream == this.mPrevActiveStream) {
            return true;
        }
        if (volumeRow.defaultStream) {
            return volumeRow2.stream == 2 || volumeRow2.stream == 4 || volumeRow2.stream == 0 || volumeRow2.stream == 10 || this.mDynamic.get(volumeRow2.stream);
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateRowsH(VolumeRow volumeRow) {
        if (D.BUG) {
            Log.d(TAG, "updateRowsH");
        }
        if (!this.mShowing) {
            trimObsoleteH();
        }
        Iterator<VolumeRow> it = this.mRows.iterator();
        while (it.hasNext()) {
            VolumeRow next = it.next();
            boolean z = next == volumeRow;
            Util.setVisOrGone(next.view, shouldBeVisibleH(next, volumeRow));
            if (next.view.isShown()) {
                updateVolumeRowTintH(next, z);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void updateRingerH() {
        VolumeDialogController.State state;
        VolumeDialogController.StreamState streamState;
        if (this.mRinger != null && (state = this.mState) != null && (streamState = state.states.get(2)) != null) {
            VolumeDialogController.State state2 = this.mState;
            int i = state2.zenMode;
            boolean z = false;
            boolean z2 = i == 3 || i == 2 || (i == 1 && state2.disallowRinger);
            enableRingerViewsH(!z2);
            int i2 = this.mState.ringerModeInternal;
            if (i2 == 0) {
                this.mRingerIcon.setImageResource(C0013R$drawable.ic_volume_ringer_mute);
                this.mRingerIcon.setTag(2);
                addAccessibilityDescription(this.mRingerIcon, 0, this.mContext.getString(C0021R$string.volume_ringer_hint_unmute));
            } else if (i2 != 1) {
                if ((this.mAutomute && streamState.level == 0) || streamState.muted) {
                    z = true;
                }
                if (z2 || !z) {
                    this.mRingerIcon.setImageResource(C0013R$drawable.ic_volume_ringer);
                    if (this.mController.hasVibrator()) {
                        addAccessibilityDescription(this.mRingerIcon, 2, this.mContext.getString(C0021R$string.volume_ringer_hint_vibrate));
                    } else {
                        addAccessibilityDescription(this.mRingerIcon, 2, this.mContext.getString(C0021R$string.volume_ringer_hint_mute));
                    }
                    this.mRingerIcon.setTag(1);
                    return;
                }
                this.mRingerIcon.setImageResource(C0013R$drawable.ic_volume_ringer_mute);
                addAccessibilityDescription(this.mRingerIcon, 2, this.mContext.getString(C0021R$string.volume_ringer_hint_unmute));
                this.mRingerIcon.setTag(2);
            } else {
                this.mRingerIcon.setImageResource(C0013R$drawable.ic_volume_ringer_vibrate);
                addAccessibilityDescription(this.mRingerIcon, 1, this.mContext.getString(C0021R$string.volume_ringer_hint_mute));
                this.mRingerIcon.setTag(3);
            }
        }
    }

    private void addAccessibilityDescription(View view, int i, final String str) {
        int i2;
        if (i == 0) {
            i2 = C0021R$string.volume_ringer_status_silent;
        } else if (i != 1) {
            i2 = C0021R$string.volume_ringer_status_normal;
        } else {
            i2 = C0021R$string.volume_ringer_status_vibrate;
        }
        view.setContentDescription(this.mContext.getString(i2));
        view.setAccessibilityDelegate(new View.AccessibilityDelegate(this) {
            /* class com.android.systemui.volume.VolumeDialogImpl.AnonymousClass1 */

            public void onInitializeAccessibilityNodeInfo(View view, AccessibilityNodeInfo accessibilityNodeInfo) {
                super.onInitializeAccessibilityNodeInfo(view, accessibilityNodeInfo);
                accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(16, str));
            }
        });
    }

    private void enableVolumeRowViewsH(VolumeRow volumeRow, boolean z) {
        volumeRow.dndIcon.setVisibility(z ^ true ? 0 : 8);
    }

    private void enableRingerViewsH(boolean z) {
        ImageButton imageButton = this.mRingerIcon;
        if (imageButton != null) {
            imageButton.setEnabled(z);
        }
        FrameLayout frameLayout = this.mZenIcon;
        if (frameLayout != null) {
            frameLayout.setVisibility(z ? 8 : 0);
        }
    }

    private void trimObsoleteH() {
        if (D.BUG) {
            Log.d(TAG, "trimObsoleteH");
        }
        for (int size = this.mRows.size() - 1; size >= 0; size--) {
            VolumeRow volumeRow = this.mRows.get(size);
            if (volumeRow.ss != null && volumeRow.ss.dynamic && !this.mDynamic.get(volumeRow.stream)) {
                this.mRows.remove(size);
                this.mDialogRowsView.removeView(volumeRow.view);
                this.mConfigurableTexts.remove(volumeRow.header);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onStateChangedH(VolumeDialogController.State state) {
        int i;
        if (D.BUG) {
            String str = TAG;
            Log.d(str, "onStateChangedH() state: " + state.toString());
        }
        VolumeDialogController.State state2 = this.mState;
        if (!(state2 == null || state == null || state2.ringerModeInternal == (i = state.ringerModeInternal) || i != 1)) {
            this.mController.vibrate(VibrationEffect.get(5));
        }
        this.mState = state;
        this.mDynamic.clear();
        for (int i2 = 0; i2 < state.states.size(); i2++) {
            int keyAt = state.states.keyAt(i2);
            if (state.states.valueAt(i2).dynamic) {
                this.mDynamic.put(keyAt, true);
                if (findRow(keyAt) == null) {
                    addRow(keyAt, C0013R$drawable.ic_volume_remote, C0013R$drawable.ic_volume_remote_mute, true, false, true);
                }
            }
        }
        int i3 = this.mActiveStream;
        int i4 = state.activeStream;
        if (i3 != i4) {
            this.mPrevActiveStream = i3;
            this.mActiveStream = i4;
            updateRowsH(getActiveRow());
            if (this.mShowing) {
                rescheduleTimeoutH();
            }
        }
        for (VolumeRow volumeRow : this.mRows) {
            updateVolumeRowH(volumeRow);
        }
        updateRingerH();
        this.mWindow.setTitle(composeWindowTitle());
    }

    /* access modifiers changed from: package-private */
    public CharSequence composeWindowTitle() {
        return this.mContext.getString(C0021R$string.volume_dialog_title, getStreamLabelH(getActiveRow().ss));
    }

    private void updateVolumeRowH(VolumeRow volumeRow) {
        VolumeDialogController.StreamState streamState;
        boolean z;
        int i;
        int i2;
        int i3;
        int i4;
        if (D.BUG) {
            Log.i(TAG, "updateVolumeRowH s=" + volumeRow.stream);
        }
        VolumeDialogController.State state = this.mState;
        if (state != null && (streamState = state.states.get(volumeRow.stream)) != null) {
            volumeRow.ss = streamState;
            int i5 = streamState.level;
            if (i5 > 0) {
                volumeRow.lastAudibleLevel = i5;
            }
            if (streamState.level == volumeRow.requestedLevel) {
                volumeRow.requestedLevel = -1;
            }
            int i6 = 0;
            boolean z2 = volumeRow.stream == 10;
            int i7 = 2;
            boolean z3 = volumeRow.stream == 2;
            boolean z4 = volumeRow.stream == 1;
            boolean z5 = volumeRow.stream == 4;
            boolean z6 = volumeRow.stream == 3;
            boolean z7 = z3 && this.mState.ringerModeInternal == 1;
            boolean z8 = z3 && this.mState.ringerModeInternal == 0;
            boolean z9 = this.mState.zenMode == 1;
            boolean z10 = this.mState.zenMode == 3;
            boolean z11 = this.mState.zenMode == 2;
            if (!z10 ? !z11 ? !z9 || ((!z5 || !this.mState.disallowAlarms) && ((!z6 || !this.mState.disallowMedia) && ((!z3 || !this.mState.disallowRinger) && (!z4 || !this.mState.disallowSystem)))) : !z3 && !z4 && !z5 && !z6 : !z3 && !z4) {
                z = false;
            } else {
                z = true;
            }
            int i8 = streamState.levelMax * 100;
            if (i8 != volumeRow.slider.getMax()) {
                volumeRow.slider.setMax(i8);
            }
            int i9 = streamState.levelMin * 100;
            if (i9 != volumeRow.slider.getMin()) {
                volumeRow.slider.setMin(i9);
            }
            Util.setText(volumeRow.header, getStreamLabelH(streamState));
            volumeRow.slider.setContentDescription(volumeRow.header.getText());
            this.mConfigurableTexts.add(volumeRow.header, streamState.name);
            boolean z12 = (this.mAutomute || streamState.muteSupported) && !z;
            volumeRow.icon.setEnabled(z12);
            volumeRow.icon.setAlpha(z12 ? 1.0f : 0.5f);
            if (z7) {
                i = C0013R$drawable.ic_volume_ringer_vibrate;
            } else if (z8 || z) {
                i = volumeRow.iconMuteRes;
            } else if (streamState.routedToBluetooth) {
                i = isStreamMuted(streamState) ? C0013R$drawable.ic_volume_media_bt_mute : C0013R$drawable.ic_volume_media_bt;
            } else {
                i = isStreamMuted(streamState) ? volumeRow.iconMuteRes : volumeRow.iconRes;
            }
            volumeRow.icon.setImageResource(i);
            if (i == C0013R$drawable.ic_volume_ringer_vibrate) {
                i7 = 3;
            } else if (!(i == C0013R$drawable.ic_volume_media_bt_mute || i == volumeRow.iconMuteRes)) {
                i7 = (i == C0013R$drawable.ic_volume_media_bt || i == volumeRow.iconRes) ? 1 : 0;
            }
            volumeRow.iconState = i7;
            if (!z12) {
                volumeRow.icon.setContentDescription(getStreamLabelH(streamState));
            } else if (z3) {
                if (z7) {
                    volumeRow.icon.setContentDescription(this.mContext.getString(C0021R$string.volume_stream_content_description_unmute, getStreamLabelH(streamState)));
                } else if (this.mController.hasVibrator()) {
                    ImageButton imageButton = volumeRow.icon;
                    Context context = this.mContext;
                    if (this.mShowA11yStream) {
                        i4 = C0021R$string.volume_stream_content_description_vibrate_a11y;
                    } else {
                        i4 = C0021R$string.volume_stream_content_description_vibrate;
                    }
                    imageButton.setContentDescription(context.getString(i4, getStreamLabelH(streamState)));
                } else {
                    ImageButton imageButton2 = volumeRow.icon;
                    Context context2 = this.mContext;
                    if (this.mShowA11yStream) {
                        i3 = C0021R$string.volume_stream_content_description_mute_a11y;
                    } else {
                        i3 = C0021R$string.volume_stream_content_description_mute;
                    }
                    imageButton2.setContentDescription(context2.getString(i3, getStreamLabelH(streamState)));
                }
            } else if (z2) {
                volumeRow.icon.setContentDescription(getStreamLabelH(streamState));
            } else if (streamState.muted || (this.mAutomute && streamState.level == 0)) {
                volumeRow.icon.setContentDescription(this.mContext.getString(C0021R$string.volume_stream_content_description_unmute, getStreamLabelH(streamState)));
            } else {
                ImageButton imageButton3 = volumeRow.icon;
                Context context3 = this.mContext;
                if (this.mShowA11yStream) {
                    i2 = C0021R$string.volume_stream_content_description_mute_a11y;
                } else {
                    i2 = C0021R$string.volume_stream_content_description_mute;
                }
                imageButton3.setContentDescription(context3.getString(i2, getStreamLabelH(streamState)));
            }
            if (z) {
                volumeRow.tracking = false;
            }
            enableVolumeRowViewsH(volumeRow, !z);
            boolean z13 = !z;
            if (!volumeRow.ss.muted || z3 || z) {
                i6 = volumeRow.ss.level;
            }
            updateVolumeRowSliderH(volumeRow, z13, i6);
        }
    }

    private boolean isStreamMuted(VolumeDialogController.StreamState streamState) {
        return (this.mAutomute && streamState.level == 0) || streamState.muted;
    }

    private void updateVolumeRowTintH(VolumeRow volumeRow, boolean z) {
        ColorStateList colorStateList;
        int i;
        if (z) {
            volumeRow.slider.requestFocus();
        }
        boolean z2 = z && volumeRow.slider.isEnabled();
        if (z2) {
            colorStateList = Utils.getColorAccent(this.mContext);
        } else {
            colorStateList = Utils.getColorAttr(this.mContext, 16842800);
        }
        if (z2) {
            i = Color.alpha(colorStateList.getDefaultColor());
        } else {
            i = getAlphaAttr(16844115);
        }
        if (colorStateList != volumeRow.cachedTint) {
            volumeRow.slider.setProgressTintList(colorStateList);
            volumeRow.slider.setThumbTintList(colorStateList);
            volumeRow.slider.setProgressBackgroundTintList(colorStateList);
            volumeRow.slider.setAlpha(((float) i) / 255.0f);
            volumeRow.icon.setImageTintList(colorStateList);
            volumeRow.icon.setImageAlpha(i);
            volumeRow.cachedTint = colorStateList;
        }
    }

    private void updateVolumeRowSliderH(VolumeRow volumeRow, boolean z, int i) {
        int i2;
        volumeRow.slider.setEnabled(z);
        updateVolumeRowTintH(volumeRow, volumeRow.stream == this.mActiveStream);
        if (!volumeRow.tracking) {
            int progress = volumeRow.slider.getProgress();
            int impliedLevel = getImpliedLevel(volumeRow.slider, progress);
            boolean z2 = volumeRow.view.getVisibility() == 0;
            boolean z3 = SystemClock.uptimeMillis() - volumeRow.userAttempt < 1000;
            this.mHandler.removeMessages(3, volumeRow);
            if (this.mShowing && z2 && z3) {
                if (D.BUG) {
                    Log.d(TAG, "inGracePeriod");
                }
                H h = this.mHandler;
                h.sendMessageAtTime(h.obtainMessage(3, volumeRow), volumeRow.userAttempt + 1000);
            } else if ((i == impliedLevel && this.mShowing && z2) || progress == (i2 = i * 100)) {
            } else {
                if (!this.mShowing || !z2) {
                    if (volumeRow.anim != null) {
                        volumeRow.anim.cancel();
                    }
                    volumeRow.slider.setProgress(i2, true);
                } else if (volumeRow.anim == null || !volumeRow.anim.isRunning() || volumeRow.animTargetProgress != i2) {
                    if (volumeRow.anim == null) {
                        volumeRow.anim = ObjectAnimator.ofInt(volumeRow.slider, "progress", progress, i2);
                        volumeRow.anim.setInterpolator(new DecelerateInterpolator());
                    } else {
                        volumeRow.anim.cancel();
                        volumeRow.anim.setIntValues(progress, i2);
                    }
                    volumeRow.animTargetProgress = i2;
                    volumeRow.anim.setDuration(80L);
                    volumeRow.anim.start();
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void recheckH(VolumeRow volumeRow) {
        if (volumeRow == null) {
            if (D.BUG) {
                Log.d(TAG, "recheckH ALL");
            }
            trimObsoleteH();
            for (VolumeRow volumeRow2 : this.mRows) {
                updateVolumeRowH(volumeRow2);
            }
            return;
        }
        if (D.BUG) {
            String str = TAG;
            Log.d(str, "recheckH " + volumeRow.stream);
        }
        updateVolumeRowH(volumeRow);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setStreamImportantH(int i, boolean z) {
        for (VolumeRow volumeRow : this.mRows) {
            if (volumeRow.stream == i) {
                volumeRow.important = z;
                return;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0024, code lost:
        recheckH(null);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void showSafetyWarningH(int r4) {
        /*
            r3 = this;
            r4 = r4 & 1025(0x401, float:1.436E-42)
            if (r4 != 0) goto L_0x0008
            boolean r4 = r3.mShowing
            if (r4 == 0) goto L_0x0028
        L_0x0008:
            java.lang.Object r4 = r3.mSafetyWarningLock
            monitor-enter(r4)
            com.android.systemui.volume.SafetyWarningDialog r0 = r3.mSafetyWarning     // Catch:{ all -> 0x002c }
            if (r0 == 0) goto L_0x0011
            monitor-exit(r4)     // Catch:{ all -> 0x002c }
            return
        L_0x0011:
            com.android.systemui.volume.VolumeDialogImpl$2 r0 = new com.android.systemui.volume.VolumeDialogImpl$2     // Catch:{ all -> 0x002c }
            android.content.Context r1 = r3.mContext     // Catch:{ all -> 0x002c }
            com.android.systemui.plugins.VolumeDialogController r2 = r3.mController     // Catch:{ all -> 0x002c }
            android.media.AudioManager r2 = r2.getAudioManager()     // Catch:{ all -> 0x002c }
            r0.<init>(r1, r2)     // Catch:{ all -> 0x002c }
            r3.mSafetyWarning = r0     // Catch:{ all -> 0x002c }
            r0.show()     // Catch:{ all -> 0x002c }
            monitor-exit(r4)     // Catch:{ all -> 0x002c }
            r4 = 0
            r3.recheckH(r4)
        L_0x0028:
            r3.rescheduleTimeoutH()
            return
        L_0x002c:
            r3 = move-exception
            monitor-exit(r4)
            throw r3
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.volume.VolumeDialogImpl.showSafetyWarningH(int):void");
    }

    private String getStreamLabelH(VolumeDialogController.StreamState streamState) {
        if (streamState == null) {
            return "";
        }
        String str = streamState.remoteLabel;
        if (str != null) {
            return str;
        }
        try {
            return this.mContext.getResources().getString(streamState.name);
        } catch (Resources.NotFoundException unused) {
            String str2 = TAG;
            Slog.e(str2, "Can't find translation for stream " + streamState);
            return "";
        }
    }

    private Runnable getSinglePressFor(ImageButton imageButton) {
        return new Runnable(imageButton) {
            /* class com.android.systemui.volume.$$Lambda$VolumeDialogImpl$ELxLq17JBDlmCmJk3kWI8E8 */
            public final /* synthetic */ ImageButton f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                VolumeDialogImpl.this.lambda$getSinglePressFor$13$VolumeDialogImpl(this.f$1);
            }
        };
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$getSinglePressFor$13 */
    public /* synthetic */ void lambda$getSinglePressFor$13$VolumeDialogImpl(ImageButton imageButton) {
        if (imageButton != null) {
            imageButton.setPressed(true);
            imageButton.postOnAnimationDelayed(getSingleUnpressFor(imageButton), 200);
        }
    }

    private Runnable getSingleUnpressFor(ImageButton imageButton) {
        return new Runnable(imageButton) {
            /* class com.android.systemui.volume.$$Lambda$VolumeDialogImpl$A9JxlbuHI6pR_4OJL5e0cwBcPs */
            public final /* synthetic */ ImageButton f$0;

            {
                this.f$0 = r1;
            }

            public final void run() {
                VolumeDialogImpl.lambda$getSingleUnpressFor$14(this.f$0);
            }
        };
    }

    static /* synthetic */ void lambda$getSingleUnpressFor$14(ImageButton imageButton) {
        if (imageButton != null) {
            imageButton.setPressed(false);
        }
    }

    /* access modifiers changed from: private */
    public final class H extends Handler {
        public H() {
            super(Looper.getMainLooper());
        }

        public void handleMessage(Message message) {
            switch (message.what) {
                case 1:
                    VolumeDialogImpl.this.showH(message.arg1);
                    return;
                case 2:
                    VolumeDialogImpl.this.dismissH(message.arg1);
                    return;
                case 3:
                    VolumeDialogImpl.this.recheckH((VolumeRow) message.obj);
                    return;
                case 4:
                    VolumeDialogImpl.this.recheckH(null);
                    return;
                case 5:
                    VolumeDialogImpl.this.setStreamImportantH(message.arg1, message.arg2 != 0);
                    return;
                case 6:
                    VolumeDialogImpl.this.rescheduleTimeoutH();
                    return;
                case 7:
                    VolumeDialogImpl volumeDialogImpl = VolumeDialogImpl.this;
                    volumeDialogImpl.onStateChangedH(volumeDialogImpl.mState);
                    return;
                default:
                    return;
            }
        }
    }

    /* access modifiers changed from: private */
    public final class CustomDialog extends Dialog implements DialogInterface {
        public CustomDialog(Context context) {
            super(context, C0022R$style.qs_theme);
        }

        public boolean dispatchTouchEvent(MotionEvent motionEvent) {
            VolumeDialogImpl.this.rescheduleTimeoutH();
            return super.dispatchTouchEvent(motionEvent);
        }

        /* access modifiers changed from: protected */
        public void onStart() {
            super.setCanceledOnTouchOutside(true);
            super.onStart();
        }

        /* access modifiers changed from: protected */
        public void onStop() {
            super.onStop();
            VolumeDialogImpl.this.mHandler.sendEmptyMessage(4);
        }

        public boolean onTouchEvent(MotionEvent motionEvent) {
            if (!VolumeDialogImpl.this.mShowing || motionEvent.getAction() != 4) {
                return false;
            }
            VolumeDialogImpl.this.dismissH(1);
            return true;
        }
    }

    /* access modifiers changed from: private */
    public final class VolumeSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {
        private final VolumeRow mRow;

        private VolumeSeekBarChangeListener(VolumeRow volumeRow) {
            this.mRow = volumeRow;
        }

        public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
            int i2;
            if (this.mRow.ss != null) {
                if (D.BUG) {
                    String str = VolumeDialogImpl.TAG;
                    Log.d(str, AudioSystem.streamToString(this.mRow.stream) + " onProgressChanged " + i + " fromUser=" + z);
                }
                if (z) {
                    if (this.mRow.ss.levelMin > 0 && i < (i2 = this.mRow.ss.levelMin * 100)) {
                        seekBar.setProgress(i2);
                        i = i2;
                    }
                    int impliedLevel = VolumeDialogImpl.getImpliedLevel(seekBar, i);
                    if (this.mRow.ss.level != impliedLevel || (this.mRow.ss.muted && impliedLevel > 0)) {
                        this.mRow.userAttempt = SystemClock.uptimeMillis();
                        if (this.mRow.requestedLevel != impliedLevel) {
                            VolumeDialogImpl.this.mController.setActiveStream(this.mRow.stream);
                            VolumeDialogImpl.this.mController.setStreamVolume(this.mRow.stream, impliedLevel);
                            this.mRow.requestedLevel = impliedLevel;
                            Events.writeEvent(9, Integer.valueOf(this.mRow.stream), Integer.valueOf(impliedLevel));
                        }
                    }
                }
            }
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
            if (D.BUG) {
                String str = VolumeDialogImpl.TAG;
                Log.d(str, "onStartTrackingTouch " + this.mRow.stream);
            }
            VolumeDialogImpl.this.mController.setActiveStream(this.mRow.stream);
            this.mRow.tracking = true;
        }

        public void onStopTrackingTouch(SeekBar seekBar) {
            if (D.BUG) {
                String str = VolumeDialogImpl.TAG;
                Log.d(str, "onStopTrackingTouch " + this.mRow.stream);
            }
            this.mRow.tracking = false;
            this.mRow.userAttempt = SystemClock.uptimeMillis();
            int impliedLevel = VolumeDialogImpl.getImpliedLevel(seekBar, seekBar.getProgress());
            Events.writeEvent(16, Integer.valueOf(this.mRow.stream), Integer.valueOf(impliedLevel));
            if (this.mRow.ss.level != impliedLevel) {
                VolumeDialogImpl.this.mHandler.sendMessageDelayed(VolumeDialogImpl.this.mHandler.obtainMessage(3, this.mRow), 1000);
            }
        }
    }

    private final class Accessibility extends View.AccessibilityDelegate {
        private Accessibility() {
        }

        public void init() {
            VolumeDialogImpl.this.mDialogView.setAccessibilityDelegate(this);
        }

        public boolean dispatchPopulateAccessibilityEvent(View view, AccessibilityEvent accessibilityEvent) {
            accessibilityEvent.getText().add(VolumeDialogImpl.this.composeWindowTitle());
            return true;
        }

        public boolean onRequestSendAccessibilityEvent(ViewGroup viewGroup, View view, AccessibilityEvent accessibilityEvent) {
            VolumeDialogImpl.this.rescheduleTimeoutH();
            return super.onRequestSendAccessibilityEvent(viewGroup, view, accessibilityEvent);
        }
    }

    /* access modifiers changed from: private */
    public static class VolumeRow {
        private ObjectAnimator anim;
        private int animTargetProgress;
        private ColorStateList cachedTint;
        private boolean defaultStream;
        private FrameLayout dndIcon;
        private TextView header;
        private ImageButton icon;
        private int iconMuteRes;
        private int iconRes;
        private int iconState;
        private boolean important;
        private int lastAudibleLevel;
        private int requestedLevel;
        private SeekBar slider;
        private VolumeDialogController.StreamState ss;
        private int stream;
        private boolean tracking;
        private long userAttempt;
        private View view;

        private VolumeRow() {
            this.requestedLevel = -1;
            this.lastAudibleLevel = 1;
        }
    }
}
