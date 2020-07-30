package com.android.systemui.miui.volume;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.AudioSystem;
import android.media.AudioSystemCompat;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;
import android.util.Slog;
import android.util.SparseBooleanArray;
import android.view.AccessibilityManagerCompat;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBarCompat;
import android.widget.SeekBar;
import android.widget.Toast;
import androidx.preference.PreferenceManager;
import com.android.systemui.Dependency;
import com.android.systemui.Dumpable;
import com.android.systemui.Logger;
import com.android.systemui.Util;
import com.android.systemui.miui.volume.MiuiVolumeDialogMotion;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.VolumeDialog;
import com.android.systemui.plugins.VolumeDialogController;
import com.android.systemui.statistic.ScenarioConstants;
import com.android.systemui.statistic.ScenarioTrackUtil;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.statusbar.policy.DeviceProvisionedController;
import com.android.systemui.statusbar.policy.KeyguardMonitor;
import com.android.systemui.tuner.TunerService;
import com.android.systemui.volume.ConfigurableTexts;
import com.android.systemui.volume.Events;
import com.android.systemui.volume.SafetyWarningDialog;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import miui.app.AlertDialog;

public class MiuiVolumeDialogImpl implements VolumeDialog, TunerService.Tunable, Dumpable, ConfigurationController.ConfigurationListener {
    /* access modifiers changed from: private */
    public static final String TAG = Util.logTag(MiuiVolumeDialogImpl.class);
    private final Accessibility mAccessibility = new Accessibility();
    /* access modifiers changed from: private */
    public final AccessibilityManager mAccessibilityMgr;
    private int mActiveStream;
    private final AudioManager mAudioManager;
    private boolean mAutomute = true;
    private final View.OnClickListener mClickExpand = new View.OnClickListener() {
        public void onClick(View view) {
            ScenarioTrackUtil.beginScenario(ScenarioConstants.SCENARIO_EXPAND_VOLUME_DIALOG);
            if (!MiuiVolumeDialogImpl.this.mDialogView.isAnimating()) {
                boolean z = !MiuiVolumeDialogImpl.this.mExpanded;
                Events.writeEvent(MiuiVolumeDialogImpl.this.mContext, 3, Boolean.valueOf(z));
                MiuiVolumeDialogImpl.this.updateExpandedH(z, false);
            }
        }
    };
    /* access modifiers changed from: private */
    public final List<VolumeColumn> mColumns = new ArrayList();
    private ConfigurableTexts mConfigurableTexts;
    /* access modifiers changed from: private */
    public final Context mContext;
    /* access modifiers changed from: private */
    public VolumeDialogController mController;
    private final VolumeDialogController.Callbacks mControllerCallbackH = new VolumeDialogController.Callbacks() {
        public void onConfigurationChanged() {
        }

        public void onVolumeChanged(int i, boolean z) {
            MiuiVolumeDialogImpl.this.recordVolumeChanged(i, z);
        }

        private void trackVolumeShowEvent(boolean z, int i) {
            if (z && i != 0 && !MiuiVolumeDialogImpl.this.mShowing && !MiuiVolumeDialogImpl.this.mDialogView.isAnimating()) {
                VolumeEventTracker.trackVolumeShow(i > 0 ? "from_key_up" : "from_key_down");
            }
        }

        public void onShowRequested(int i, boolean z, int i2) {
            trackVolumeShowEvent(z, i2);
            MiuiVolumeDialogImpl.this.showH(i);
        }

        public void onDismissRequested(int i) {
            MiuiVolumeDialogImpl.this.dismissH(i);
        }

        public void onScreenOff() {
            MiuiVolumeDialogImpl.this.dismissH(4);
        }

        public void onStateChanged(VolumeDialogController.State state) {
            MiuiVolumeDialogImpl.this.onStateChangedH(state);
        }

        public void onLayoutDirectionChanged(int i) {
            MiuiVolumeDialogImpl.this.mDialogView.setLayoutDirection(i);
        }

        public void onShowVibrateHint() {
            if (MiuiVolumeDialogImpl.this.mSilentMode) {
                MiuiVolumeDialogImpl.this.mController.setRingerMode(0, false);
            }
        }

        public void onShowSilentHint() {
            if (MiuiVolumeDialogImpl.this.mSilentMode) {
                MiuiVolumeDialogImpl.this.mController.setRingerMode(2, false);
            }
        }

        public void onShowSafetyWarning(int i) {
            MiuiVolumeDialogImpl.this.showSafetyWarningH(i);
        }

        public void onAccessibilityModeChanged(Boolean bool) {
            boolean unused = MiuiVolumeDialogImpl.this.mShowA11yStream = bool == null ? false : bool.booleanValue();
            VolumeColumn access$4500 = MiuiVolumeDialogImpl.this.getActiveColumn();
            if (access$4500.stream == 10 && !MiuiVolumeDialogImpl.this.mShowA11yStream) {
                access$4500 = (VolumeColumn) MiuiVolumeDialogImpl.this.mColumns.get(0);
            }
            MiuiVolumeDialogImpl.this.updateColumnH(access$4500);
        }
    };
    /* access modifiers changed from: private */
    public CustomDialog mDialog;
    private VolumeColumns mDialogColumns;
    private ViewGroup mDialogContentView;
    /* access modifiers changed from: private */
    public MiuiVolumeDialogView mDialogView;
    private final SparseBooleanArray mDynamic = new SparseBooleanArray();
    private ImageView mExpandButton;
    /* access modifiers changed from: private */
    public boolean mExpanded;
    /* access modifiers changed from: private */
    public final H mHandler = new H();
    /* access modifiers changed from: private */
    public boolean mHovering = false;
    /* access modifiers changed from: private */
    public ColorStateList mIconTintDark;
    private final KeyguardManager mKeyguard;
    private final KeyguardMonitor mKeyguardMonitor;
    private final Configuration mLastConfiguration = new Configuration();
    private int mLastDensity;
    private int mLockRecordTypes = 0;
    private ColorStateList mMutedColorList;
    private boolean mNeedReInit;
    /* access modifiers changed from: private */
    public boolean mPendingRecheckAll;
    /* access modifiers changed from: private */
    public boolean mPendingStateChanged;
    private BroadcastReceiver mRingerModeChangedReceiver = new BroadcastReceiver() {
        private int mRingerMode = -1;

        public void onReceive(Context context, Intent intent) {
            if ("android.media.RINGER_MODE_CHANGED".equals(intent.getAction())) {
                int intExtra = intent.getIntExtra("android.media.EXTRA_RINGER_MODE", -1);
                int i = this.mRingerMode;
                if (i != intExtra) {
                    if (i != -1 && intExtra == 1) {
                        MiuiVolumeDialogImpl.this.mHandler.sendMessageDelayed(MiuiVolumeDialogImpl.this.mHandler.obtainMessage(8), 300);
                    }
                    this.mRingerMode = intExtra;
                }
                MiuiVolumeDialogImpl.this.mSilenceModeObserver.updateVolumeInfo(VolumeUtil.getZenMode(MiuiVolumeDialogImpl.this.mContext));
            }
        }
    };
    /* access modifiers changed from: private */
    public SafetyWarningDialog mSafetyWarning;
    /* access modifiers changed from: private */
    public final Object mSafetyWarningLock = new Object();
    /* access modifiers changed from: private */
    public boolean mShowA11yStream;
    /* access modifiers changed from: private */
    public boolean mShowing;
    /* access modifiers changed from: private */
    public SilenceModeObserver mSilenceModeObserver = new SilenceModeObserver();
    /* access modifiers changed from: private */
    public boolean mSilentMode = true;
    /* access modifiers changed from: private */
    public VolumeDialogController.State mState;
    private VolumeColumn mTempColumn;
    private FrameLayout mTempColumnContainer;
    private Window mWindow;
    private int mWindowType;

    public int getVersion() {
        return 0;
    }

    public void onCreate(Context context, Context context2) {
    }

    public void onDensityOrFontScaleChanged() {
    }

    public void onDestroy() {
    }

    public void onTuningChanged(String str, String str2) {
    }

    public MiuiVolumeDialogImpl(Context context) {
        this.mContext = context;
        this.mController = (VolumeDialogController) Dependency.get(VolumeDialogController.class);
        this.mKeyguard = (KeyguardManager) this.mContext.getSystemService("keyguard");
        this.mKeyguardMonitor = (KeyguardMonitor) Dependency.get(KeyguardMonitor.class);
        this.mAudioManager = (AudioManager) this.mContext.getSystemService("audio");
        this.mAccessibilityMgr = (AccessibilityManager) this.mContext.getSystemService("accessibility");
    }

    public void init(int i, VolumeDialog.Callback callback) {
        this.mWindowType = i;
        initDialog();
        this.mAccessibility.init();
        this.mController.addCallback(this.mControllerCallbackH, this.mHandler);
        this.mController.getState();
        ((TunerService) Dependency.get(TunerService.class)).addTunable(this, "sysui_show_full_zen");
        this.mSilenceModeObserver.init();
        this.mSilenceModeObserver.register();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.media.RINGER_MODE_CHANGED");
        this.mContext.registerReceiverAsUser(this.mRingerModeChangedReceiver, UserHandle.ALL, intentFilter, (String) null, (Handler) null);
        ((ConfigurationController) Dependency.get(ConfigurationController.class)).addCallback(this);
    }

    public void destroy() {
        this.mAccessibility.destory();
        this.mController.removeCallback(this.mControllerCallbackH);
        ((TunerService) Dependency.get(TunerService.class)).removeTunable(this);
        this.mSilenceModeObserver.unregister();
        this.mContext.unregisterReceiver(this.mRingerModeChangedReceiver);
        ((ConfigurationController) Dependency.get(ConfigurationController.class)).removeCallback(this);
    }

    private void initDialog() {
        Logger.i(TAG, "initDialog");
        CustomDialog customDialog = this.mDialog;
        if (customDialog != null && customDialog.isShowing()) {
            this.mDialog.dismiss();
        }
        this.mDialog = new CustomDialog(this.mContext);
        this.mWindow = this.mDialog.getWindow();
        this.mWindow.requestFeature(1);
        this.mConfigurableTexts = new ConfigurableTexts(this.mContext);
        this.mHovering = false;
        this.mShowing = false;
        this.mPendingStateChanged = false;
        this.mDialog.setCanceledOnTouchOutside(true);
        this.mDialog.setContentView(R$layout.miui_volume_dialog);
        setupWindowAttributes();
        ((ViewGroup) this.mDialog.findViewById(16908290)).setClipChildren(false);
        this.mDialogView = (MiuiVolumeDialogView) this.mDialog.findViewById(R$id.volume_dialog);
        this.mDialogView.setOnHoverListener(new View.OnHoverListener() {
            public boolean onHover(View view, MotionEvent motionEvent) {
                int actionMasked = motionEvent.getActionMasked();
                boolean unused = MiuiVolumeDialogImpl.this.mHovering = actionMasked == 9 || actionMasked == 7;
                MiuiVolumeDialogImpl.this.rescheduleTimeoutH();
                return true;
            }
        });
        this.mDialogContentView = (ViewGroup) this.mDialogView.findViewById(R$id.volume_dialog_content);
        this.mDialogColumns = new VolumeColumns((ViewGroup) this.mDialogContentView.findViewById(R$id.volume_dialog_column_collapsed), (ViewGroup) this.mDialogContentView.findViewById(R$id.volume_dialog_columns));
        this.mTempColumnContainer = (FrameLayout) this.mDialogView.findViewById(R$id.volume_dialog_column_temp);
        this.mExpanded = false;
        this.mExpandButton = (ImageView) this.mDialogView.findViewById(R$id.volume_expand_button);
        this.mExpandButton.setOnClickListener(this.mClickExpand);
        this.mDialogView.setMotionCallback(new MiuiVolumeDialogMotion.Callback() {
            public void onAnimatingChanged(boolean z) {
                if (!z) {
                    if (MiuiVolumeDialogImpl.this.mPendingStateChanged) {
                        MiuiVolumeDialogImpl.this.mHandler.sendEmptyMessage(7);
                        boolean unused = MiuiVolumeDialogImpl.this.mPendingStateChanged = false;
                    }
                    if (MiuiVolumeDialogImpl.this.mPendingRecheckAll) {
                        MiuiVolumeDialogImpl.this.mHandler.sendEmptyMessage(4);
                        boolean unused2 = MiuiVolumeDialogImpl.this.mPendingRecheckAll = false;
                    }
                }
            }

            public void onShow() {
                String access$700 = MiuiVolumeDialogImpl.TAG;
                Logger.i(access$700, "onShow isShowing:" + MiuiVolumeDialogImpl.this.mDialog.isShowing());
                if (!MiuiVolumeDialogImpl.this.mDialog.isShowing()) {
                    MiuiVolumeDialogImpl.this.mDialog.show();
                }
            }

            public void onDismiss() {
                String access$700 = MiuiVolumeDialogImpl.TAG;
                Logger.i(access$700, "onDismiss isShowing:" + MiuiVolumeDialogImpl.this.mDialog.isShowing());
                if (MiuiVolumeDialogImpl.this.mDialog.isShowing()) {
                    MiuiVolumeDialogImpl.this.mDialog.dismiss();
                }
            }
        });
        if (this.mColumns.isEmpty()) {
            addColumn(3, R$drawable.ic_miui_volume_media, R$drawable.ic_miui_volume_media_mute, true);
            if (!AudioSystemCompat.isSingleVolume(this.mContext)) {
                addColumn(10, R$drawable.ic_miui_volume_accessibility, R$drawable.ic_miui_volume_accessibility_mute, true);
                addColumn(2, R$drawable.ic_miui_volume_ringer, R$drawable.ic_miui_volume_ringer_mute, true);
                addColumn(4, R$drawable.ic_miui_volume_alarm, R$drawable.ic_miui_volume_alarm_mute, false);
                addColumn(0, R$drawable.ic_miui_volume_voice, R$drawable.ic_miui_volume_voice_mute, false);
                addColumn(6, R$drawable.ic_miui_volume_voice, R$drawable.ic_miui_volume_voice_mute, false);
                if (this.mController.getVoiceAssistStreamType() > 0) {
                    addColumn(this.mController.getVoiceAssistStreamType(), R$drawable.ic_miui_volume_assist, R$drawable.ic_miui_volume_assist_mute, false);
                }
            }
        } else {
            addExistingColumns();
        }
        addTempColumn(3, R$drawable.ic_miui_volume_media, R$drawable.ic_miui_volume_media_mute, true);
        updateExpandedH(false, false, true);
        this.mMutedColorList = this.mContext.getResources().getColorStateList(R$color.miui_volume_disabled_color);
        this.mIconTintDark = this.mContext.getResources().getColorStateList(R$color.miui_volume_tint_dark);
    }

    private void setupWindowAttributes() {
        this.mWindow.setBackgroundDrawable(new ColorDrawable(0));
        this.mWindow.addFlags(787496);
        this.mWindow.clearFlags(8388608);
        this.mWindow.addPrivateFlags(64);
        this.mContext.getResources();
        WindowManager.LayoutParams attributes = this.mWindow.getAttributes();
        attributes.type = this.mWindowType;
        attributes.format = -3;
        attributes.setTitle(MiuiVolumeDialogImpl.class.getSimpleName());
        attributes.windowAnimations = -1;
        attributes.gravity = 48;
        attributes.width = -1;
        attributes.height = -1;
        updateDialogWindowH(false);
        this.mWindow.setAttributes(attributes);
        this.mWindow.setSoftInputMode(48);
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

    private void addColumn(int i, int i2, int i3, boolean z) {
        addColumn(i, i2, i3, z, false);
    }

    private void addColumn(int i, int i2, int i3, boolean z, boolean z2) {
        int size;
        int childCount;
        VolumeColumn volumeColumn = new VolumeColumn();
        initColumn(volumeColumn, i, i2, i3, z);
        if (!this.mShowA11yStream || !z2 || (size = this.mColumns.size()) <= 1 || (childCount = this.mDialogColumns.getCurrentParent().getChildCount()) <= 1) {
            this.mDialogColumns.addView(volumeColumn.view);
            this.mColumns.add(volumeColumn);
            return;
        }
        this.mDialogColumns.addView(volumeColumn.view, childCount - 2);
        this.mColumns.add(size - 2, volumeColumn);
    }

    private void addTempColumn(int i, int i2, int i3, boolean z) {
        VolumeColumn volumeColumn = new VolumeColumn();
        initColumn(volumeColumn, i, i2, i3, z);
        if (this.mTempColumnContainer.getChildCount() != 0) {
            this.mTempColumnContainer.removeAllViews();
        }
        this.mTempColumnContainer.addView(volumeColumn.view);
        this.mTempColumn = volumeColumn;
    }

    private void addExistingColumns() {
        int size = this.mColumns.size();
        for (int i = 0; i < size; i++) {
            VolumeColumn volumeColumn = this.mColumns.get(i);
            initColumn(volumeColumn, volumeColumn.stream, volumeColumn.initIconRes, volumeColumn.initIconMuteRes, volumeColumn.important, true);
            this.mDialogColumns.addView(volumeColumn.view);
        }
    }

    /* access modifiers changed from: private */
    public VolumeColumn getActiveColumn() {
        for (VolumeColumn next : this.mColumns) {
            if (next.stream == this.mActiveStream) {
                return next;
            }
        }
        return this.mColumns.get(0);
    }

    private VolumeColumn findColumn(int i) {
        for (VolumeColumn next : this.mColumns) {
            if (next.stream == i) {
                return next;
            }
        }
        return null;
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println(MiuiVolumeDialogImpl.class.getSimpleName() + " state:");
        printWriter.print("  mShowing: ");
        printWriter.println(this.mShowing);
        printWriter.print("  mExpanded: ");
        printWriter.println(this.mExpanded);
        printWriter.print("  mActiveStream: ");
        printWriter.println(this.mActiveStream);
        printWriter.print("  mDynamic: ");
        printWriter.println(this.mDynamic);
        printWriter.print("  mAutomute: ");
        printWriter.println(this.mAutomute);
        printWriter.print("  mSilentMode: ");
        printWriter.println(this.mSilentMode);
        printWriter.print("  mAccessibility.mFeedbackEnabled: ");
        printWriter.println(this.mAccessibility.mFeedbackEnabled);
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
    private void initColumn(VolumeColumn volumeColumn, int i, int i2, int i3, boolean z) {
        initColumn(volumeColumn, i, i2, i3, z, false);
    }

    @SuppressLint({"InflateParams"})
    private void initColumn(final VolumeColumn volumeColumn, int i, int i2, int i3, boolean z, boolean z2) {
        int unused = volumeColumn.stream = i;
        int unused2 = volumeColumn.initIconRes = i2;
        int unused3 = volumeColumn.initIconMuteRes = i3;
        boolean unused4 = volumeColumn.important = z;
        View unused5 = volumeColumn.view = LayoutInflater.from(this.mContext).inflate(R$layout.miui_volume_dialog_column, this.mDialogColumns.getCurrentParent(), false);
        volumeColumn.view.setId(volumeColumn.stream);
        volumeColumn.view.setTag(volumeColumn);
        SeekBar unused6 = volumeColumn.slider = (SeekBar) volumeColumn.view.findViewById(R$id.volume_column_slider);
        volumeColumn.slider.setOnSeekBarChangeListener(new VolumeSeekBarChangeListener(volumeColumn));
        ObjectAnimator unused7 = volumeColumn.anim = null;
        volumeColumn.view.setOnTouchListener(new View.OnTouchListener() {
            private boolean mDragging;
            private final Rect mSliderHitRect = new Rect();

            @SuppressLint({"ClickableViewAccessibility"})
            public boolean onTouch(View view, MotionEvent motionEvent) {
                volumeColumn.slider.getHitRect(this.mSliderHitRect);
                if (!this.mDragging && motionEvent.getActionMasked() == 0 && motionEvent.getY() < ((float) this.mSliderHitRect.top)) {
                    this.mDragging = true;
                }
                if (!this.mDragging) {
                    return false;
                }
                Rect rect = this.mSliderHitRect;
                motionEvent.offsetLocation((float) (-rect.left), (float) (-rect.top));
                volumeColumn.slider.dispatchTouchEvent(motionEvent);
                if (motionEvent.getActionMasked() == 1 || motionEvent.getActionMasked() == 3) {
                    this.mDragging = false;
                }
                return true;
            }
        });
        ImageView unused8 = volumeColumn.icon = (ImageView) volumeColumn.view.findViewById(R$id.volume_column_icon);
        volumeColumn.icon.setImageResource(i2);
        if (volumeColumn.stream == 10) {
            volumeColumn.icon.setImportantForAccessibility(2);
        }
        volumeColumn.slider.setProgressTintList(volumeColumn.cachedSliderTint);
        if (z2) {
            int unused9 = volumeColumn.cachedIconRes = 0;
            ColorStateList unused10 = volumeColumn.cachedIconTint = null;
            ColorStateList unused11 = volumeColumn.cachedSliderTint = null;
        }
    }

    /* access modifiers changed from: private */
    public void showH(int i) {
        if (!this.mKeyguard.isKeyguardLocked() || !this.mKeyguardMonitor.needSkipVolumeDialog()) {
            ScenarioTrackUtil.beginScenario(ScenarioConstants.SCENARIO_VOLUME_DIALOG_SHOW);
            if (Util.DEBUG) {
                String str = TAG;
                Log.d(str, "showH r=" + Events.DISMISS_REASONS[i] + " mShowing:" + this.mShowing + " mNeedReInit:" + this.mNeedReInit);
            }
            this.mHandler.removeMessages(1);
            this.mHandler.removeMessages(2);
            rescheduleTimeoutH();
            if (!this.mShowing && !this.mDialogView.isAnimating()) {
                this.mHandler.removeMessages(10);
                if (this.mNeedReInit) {
                    this.mNeedReInit = false;
                    reInit();
                    this.mHandler.obtainMessage(10, i, 0).sendToTarget();
                    return;
                }
                showVolumeDialogH(i);
            }
        }
    }

    /* access modifiers changed from: private */
    public void showVolumeDialogH(int i) {
        this.mShowing = true;
        String str = TAG;
        Logger.i(str, "showVolumeDialogH reason:" + i + " mActiveStream:" + this.mActiveStream);
        sendAccessibilityEventIfNeed(this.mContext.getString(R$string.volume_dialog_accessibility_shown_message, new Object[]{getStreamLabelH(getActiveColumn().ss)}));
        Events.writeEvent(this.mContext, 0, Integer.valueOf(i), Boolean.valueOf(this.mKeyguard.isKeyguardLocked()));
        this.mController.notifyVisible(true);
        this.mDialogView.showH();
    }

    private void reInit() {
        String str = TAG;
        Logger.i(str, "reInit mActiveStream:" + this.mActiveStream);
        this.mAccessibility.destory();
        initDialog();
        this.mHandler.sendEmptyMessage(7);
        this.mAccessibility.init();
        reCheckAllH();
        MiuiVolumeDialogView miuiVolumeDialogView = this.mDialogView;
        int i = this.mActiveStream;
        miuiVolumeDialogView.updateFooterVisibility((i == 0 || i == 6) ? false : true);
    }

    /* access modifiers changed from: protected */
    public void rescheduleTimeoutH() {
        this.mHandler.removeMessages(2);
        int computeTimeoutH = computeTimeoutH();
        H h = this.mHandler;
        h.sendMessageDelayed(h.obtainMessage(2, 3, 0), (long) computeTimeoutH);
        String str = TAG;
        Logger.i(str, "rescheduleTimeout " + computeTimeoutH + " mActiveStream:" + this.mActiveStream);
        this.mController.userActivity();
    }

    private int computeTimeoutH() {
        if (this.mAccessibility.mFeedbackEnabled) {
            return 20000;
        }
        if (this.mHovering) {
            return 16000;
        }
        if (this.mSafetyWarning == null && !this.mExpanded) {
            return this.mActiveStream == 3 ? 1500 : 3000;
        }
        return 5000;
    }

    /* access modifiers changed from: private */
    public void vibrateH() {
        ((Vibrator) this.mContext.getSystemService("vibrator")).vibrate(300);
    }

    /* access modifiers changed from: protected */
    public void dismissH(int i) {
        String str = TAG;
        Logger.i(str, "dismissH mShowing:" + this.mShowing + " dialog showing:" + this.mDialog.isShowing() + " reason:" + i);
        if (!this.mDialogView.isAnimating() || i == 8) {
            this.mHandler.removeMessages(2);
            this.mHandler.removeMessages(1);
            this.mHandler.removeMessages(10);
            if (this.mShowing || this.mDialog.isShowing()) {
                dismissDialog(i);
                return;
            }
            return;
        }
        Logger.i(TAG, "Dialog is animating!");
    }

    private void dismissDialog(int i) {
        this.mShowing = false;
        String str = TAG;
        Logger.i(str, "Final dismiss! mActiveStream:" + this.mActiveStream + " reason:" + i);
        ScenarioTrackUtil.beginScenario(ScenarioConstants.SCENARIO_VOLUME_DIALOG_HIDE);
        sendAccessibilityEventIfNeed(this.mContext.getString(R$string.volume_dialog_accessibility_dismissed_message));
        Events.writeEvent(this.mContext, 1, Integer.valueOf(i));
        VolumeEventTracker.trackVolumeDismiss(Events.DISMISS_REASONS[i]);
        this.mController.notifyVisible(false);
        this.mDialogView.dismissH(new Runnable() {
            public void run() {
                MiuiVolumeDialogImpl.this.updateExpandedH(false, true);
            }
        });
        synchronized (this.mSafetyWarningLock) {
            if (this.mSafetyWarning != null) {
                if (Util.DEBUG) {
                    Log.d(TAG, "SafetyWarning dismissed");
                }
                this.mSafetyWarning.dismiss();
            }
        }
    }

    /* access modifiers changed from: private */
    public void updateExpandedH(boolean z, boolean z2) {
        updateExpandedH(z, z2, false);
    }

    private void updateExpandedH(boolean z, boolean z2, boolean z3) {
        if (this.mExpanded != z || z3) {
            this.mExpanded = z;
            if (Util.DEBUG) {
                String str = TAG;
                Log.d(str, "updateExpandedH " + z);
            }
            this.mDialogView.updateExpanded(z, !z2);
            this.mDialogColumns.updateExpandedH(this.mExpanded);
            updateColumnH(getActiveColumn());
            updateDialogWindowH(z2);
            rescheduleTimeoutH();
        }
    }

    private void updateDialogWindowH(boolean z) {
        float fraction = this.mContext.getResources().getFraction(R$fraction.miui_volume_dim_behind_collapsed, 1, 1);
        float fraction2 = this.mContext.getResources().getFraction(R$fraction.miui_volume_dim_behind_expanded, 1, 1);
        if (this.mExpanded || fraction > 0.0f) {
            this.mWindow.addFlags(2);
        } else {
            this.mWindow.clearFlags(2);
        }
        if (this.mExpanded) {
            this.mWindow.clearFlags(8);
        } else {
            this.mWindow.addFlags(8);
        }
        Window window = this.mWindow;
        if (this.mExpanded && !z) {
            fraction = fraction2;
        }
        window.setDimAmount(fraction);
    }

    private boolean shouldBeVisibleH(VolumeColumn volumeColumn, boolean z) {
        if (volumeColumn.stream == 10) {
            if (this.mExpanded || !this.mShowA11yStream) {
                return false;
            }
            return true;
        } else if (((this.mController.getVoiceAssistStreamType() > 0 && volumeColumn.stream == this.mController.getVoiceAssistStreamType()) || this.mDynamic.get(volumeColumn.stream)) && this.mExpanded) {
            return false;
        } else {
            if (this.mExpanded && volumeColumn.view.getVisibility() == 0) {
                return true;
            }
            if (this.mExpanded && (volumeColumn.important || z)) {
                return true;
            }
            if (this.mExpanded || !z) {
                return false;
            }
            return true;
        }
    }

    /* access modifiers changed from: private */
    public void updateColumnH(VolumeColumn volumeColumn) {
        if (Util.DEBUG) {
            Log.d(TAG, "updateColumnH");
        }
        if (!this.mShowing) {
            trimObsoleteH();
        }
        updateTempColumn();
        Iterator<VolumeColumn> it = this.mColumns.iterator();
        while (it.hasNext()) {
            VolumeColumn next = it.next();
            boolean shouldBeVisibleH = shouldBeVisibleH(next, next == volumeColumn);
            if (this.mExpanded || !next.important) {
                Util.setVisOrGone(next.view, shouldBeVisibleH);
            } else {
                Util.setVisOrInvis(next.view, shouldBeVisibleH);
            }
            updateColumnsSizeH(next.slider);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:22:0x0065  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void updateTempColumn() {
        /*
            r9 = this;
            android.media.AudioManager r0 = r9.mAudioManager
            boolean r0 = r0.isMusicActive()
            int r1 = com.android.systemui.miui.volume.R$drawable.ic_miui_volume_media
            int r2 = com.android.systemui.miui.volume.R$drawable.ic_miui_volume_media_mute
            int r3 = r9.mActiveStream
            r4 = 3
            r5 = 1
            r6 = 0
            if (r3 != 0) goto L_0x0016
            if (r0 == 0) goto L_0x0016
        L_0x0013:
            r3 = r5
            r7 = r3
            goto L_0x0041
        L_0x0016:
            int r3 = r9.mActiveStream
            if (r3 != r4) goto L_0x0030
            boolean r3 = android.media.AudioSystem.isStreamActive(r6, r6)
            if (r3 == 0) goto L_0x0030
            java.lang.String r1 = TAG
            java.lang.String r2 = "voice_call is active too"
            android.util.Log.d(r1, r2)
            int r1 = com.android.systemui.miui.volume.R$drawable.ic_miui_volume_voice
            int r2 = com.android.systemui.miui.volume.R$drawable.ic_miui_volume_voice_mute
            r7 = r5
            r3 = r6
            r4 = r3
            goto L_0x0041
        L_0x0030:
            int r3 = r9.mActiveStream
            com.android.systemui.plugins.VolumeDialogController r7 = r9.mController
            int r7 = r7.getVoiceAssistStreamType()
            if (r3 != r7) goto L_0x003f
            int r3 = r9.mActiveStream
            if (r3 <= 0) goto L_0x003f
            goto L_0x0013
        L_0x003f:
            r3 = r5
            r7 = r6
        L_0x0041:
            com.android.systemui.miui.volume.MiuiVolumeDialogImpl$VolumeColumn r8 = r9.mTempColumn
            int unused = r8.stream = r4
            com.android.systemui.miui.volume.MiuiVolumeDialogImpl$VolumeColumn r4 = r9.mTempColumn
            int unused = r4.initIconRes = r1
            com.android.systemui.miui.volume.MiuiVolumeDialogImpl$VolumeColumn r1 = r9.mTempColumn
            int unused = r1.initIconMuteRes = r2
            com.android.systemui.miui.volume.MiuiVolumeDialogImpl$VolumeColumn r1 = r9.mTempColumn
            boolean unused = r1.important = r3
            boolean r1 = r9.mExpanded
            if (r1 != 0) goto L_0x0060
            if (r7 != 0) goto L_0x0061
            boolean r1 = r9.mShowA11yStream
            if (r1 == 0) goto L_0x0060
            goto L_0x0061
        L_0x0060:
            r5 = r6
        L_0x0061:
            boolean r1 = com.android.systemui.miui.volume.Util.DEBUG
            if (r1 == 0) goto L_0x00a9
            java.lang.String r1 = TAG
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "shouldTempBeVisible mExpanded:"
            r2.append(r3)
            boolean r3 = r9.mExpanded
            r2.append(r3)
            java.lang.String r3 = " mActiveStream:"
            r2.append(r3)
            int r3 = r9.mActiveStream
            r2.append(r3)
            java.lang.String r3 = " mShowA11yStream:"
            r2.append(r3)
            boolean r3 = r9.mShowA11yStream
            r2.append(r3)
            java.lang.String r3 = " isMusicActive:"
            r2.append(r3)
            r2.append(r0)
            java.lang.String r0 = " shouldTempBeVisible:"
            r2.append(r0)
            r2.append(r5)
            java.lang.String r0 = " streamVisible:"
            r2.append(r0)
            r2.append(r7)
            java.lang.String r0 = r2.toString()
            android.util.Log.d(r1, r0)
        L_0x00a9:
            android.widget.FrameLayout r9 = r9.mTempColumnContainer
            com.android.systemui.miui.volume.Util.setVisOrGone(r9, r5)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.miui.volume.MiuiVolumeDialogImpl.updateTempColumn():void");
    }

    private void updateColumnsSizeH(View view) {
        int i;
        int i2;
        int i3;
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        Resources resources = this.mContext.getResources();
        if (this.mExpanded) {
            i = R$dimen.miui_volume_column_width_expanded;
        } else {
            i = R$dimen.miui_volume_column_width;
        }
        marginLayoutParams.width = resources.getDimensionPixelSize(i);
        Resources resources2 = this.mContext.getResources();
        if (this.mExpanded) {
            i2 = R$dimen.miui_volume_column_height_expanded;
        } else {
            i2 = R$dimen.miui_volume_column_height;
        }
        marginLayoutParams.height = resources2.getDimensionPixelSize(i2);
        Resources resources3 = this.mContext.getResources();
        if (!this.mExpanded) {
            i3 = R$dimen.miui_volume_column_margin_horizontal;
        } else if (this.mActiveStream == 0) {
            i3 = R$dimen.miui_volume_column_margin_horizontal_expanded_voice;
        } else {
            i3 = R$dimen.miui_volume_column_margin_horizontal_expanded;
        }
        int dimensionPixelSize = resources3.getDimensionPixelSize(i3);
        marginLayoutParams.rightMargin = dimensionPixelSize;
        marginLayoutParams.leftMargin = dimensionPixelSize;
    }

    private void trimObsoleteH() {
        if (Util.DEBUG) {
            Log.d(TAG, "trimObsoleteH");
        }
        for (int size = this.mColumns.size() - 1; size >= 0; size--) {
            VolumeColumn volumeColumn = this.mColumns.get(size);
            if (volumeColumn.ss != null && volumeColumn.ss.dynamic && !this.mDynamic.get(volumeColumn.stream)) {
                this.mColumns.remove(size);
                this.mDialogColumns.removeView(volumeColumn.view);
            }
        }
    }

    /* access modifiers changed from: private */
    public void onStateChangedH(VolumeDialogController.State state) {
        boolean isAnimating = this.mDialogView.isAnimating();
        if (Util.DEBUG) {
            Log.d(TAG, "onStateChangedH animating=" + isAnimating + " activeStream:" + state.activeStream);
        }
        this.mState = state;
        if (isAnimating) {
            this.mPendingStateChanged = true;
            return;
        }
        this.mDynamic.clear();
        boolean z = false;
        for (int i = 0; i < state.states.size(); i++) {
            int keyAt = state.states.keyAt(i);
            if (state.states.valueAt(i).dynamic) {
                this.mDynamic.put(keyAt, true);
                if (findColumn(keyAt) == null) {
                    addColumn(keyAt, R$drawable.ic_miui_volume_media, R$drawable.ic_miui_volume_media_mute, true, true);
                }
            }
        }
        if (Util.DEBUG) {
            Log.d(TAG, "onStateChangedH mActiveStream:" + this.mActiveStream + " state.activeStream:" + state.activeStream);
        }
        int i2 = this.mActiveStream;
        int i3 = state.activeStream;
        if (i2 != i3) {
            this.mActiveStream = i3;
            updateColumnH(getActiveColumn());
            rescheduleTimeoutH();
            MiuiVolumeDialogView miuiVolumeDialogView = this.mDialogView;
            int i4 = this.mActiveStream;
            if (!(i4 == 0 || i4 == 6)) {
                z = true;
            }
            miuiVolumeDialogView.updateFooterVisibility(z);
        }
        for (VolumeColumn updateVolumeColumnH : this.mColumns) {
            updateVolumeColumnH(updateVolumeColumnH);
        }
        updateVolumeColumnH(this.mTempColumn);
    }

    private void updateVolumeColumnH(VolumeColumn volumeColumn) {
        VolumeDialogController.StreamState streamState;
        int i;
        int i2;
        if (Util.DEBUG) {
            Log.d(TAG, "updateVolumeColumnH s=" + volumeColumn.stream);
        }
        VolumeDialogController.State state = this.mState;
        if (state != null && (streamState = state.states.get(volumeColumn.stream)) != null) {
            VolumeDialogController.StreamState unused = volumeColumn.ss = streamState;
            int i3 = streamState.level;
            if (i3 > 0) {
                int unused2 = volumeColumn.lastAudibleLevel = i3;
            }
            if (streamState.level == volumeColumn.requestedLevel) {
                int unused3 = volumeColumn.requestedLevel = -1;
            }
            boolean z = streamState.muted;
            int i4 = 1;
            if (volumeColumn.stream == 2 && !this.mDialogView.isOffMode()) {
                z = true;
            }
            int i5 = streamState.levelMax * 100;
            if (i5 != volumeColumn.slider.getMax()) {
                volumeColumn.slider.setMax(i5);
            }
            if (Util.DEBUG) {
                Log.d(TAG, "updateVolumeColumnH level:" + streamState.level + " levelMax:" + streamState.levelMax + " mAutomute:" + this.mAutomute + " streamMuted:" + z + " column.stream:" + volumeColumn.stream);
            }
            updateColumnIconH(volumeColumn);
            if (!this.mAutomute || streamState.level != 0) {
                i = z ? volumeColumn.iconMuteRes : volumeColumn.iconRes;
            } else {
                i = volumeColumn.iconMuteRes;
            }
            if (i != volumeColumn.cachedIconRes) {
                int unused4 = volumeColumn.cachedIconRes = i;
                volumeColumn.icon.setImageResource(i);
            }
            if (i == volumeColumn.iconMuteRes) {
                i4 = 2;
            } else if (i != volumeColumn.iconRes) {
                i4 = 0;
            }
            int unused5 = volumeColumn.iconState = i4;
            if (z) {
                i2 = this.mAudioManager.getLastAudibleStreamVolume(volumeColumn.stream);
            } else {
                i2 = volumeColumn.ss.level;
            }
            updateVolumeColumnSliderH(volumeColumn, z, i2);
        }
    }

    private void updateColumnIconH(VolumeColumn volumeColumn) {
        int unused = volumeColumn.iconRes = volumeColumn.initIconRes;
        int unused2 = volumeColumn.iconMuteRes = volumeColumn.initIconMuteRes;
        if (volumeColumn.stream == this.mActiveStream) {
            int devicesForStream = this.mAudioManager.getDevicesForStream(volumeColumn.stream);
            if (volumeColumn.stream == 0 && this.mAudioManager.isSpeakerphoneOn()) {
                int unused3 = volumeColumn.iconRes = R$drawable.ic_miui_volume_speaker;
                int unused4 = volumeColumn.iconMuteRes = R$drawable.ic_miui_volume_speaker_mute;
            }
            if ((devicesForStream & 4) != 0 || (devicesForStream & 8) != 0) {
                int unused5 = volumeColumn.iconRes = R$drawable.ic_miui_volume_headset;
                int unused6 = volumeColumn.iconMuteRes = R$drawable.ic_miui_volume_headset_mute;
            }
        }
    }

    private void updateVolumeColumnSliderH(VolumeColumn volumeColumn, boolean z, int i) {
        int i2;
        ColorStateList colorStateList = z ? this.mMutedColorList : null;
        if (Util.DEBUG) {
            String str = TAG;
            StringBuilder sb = new StringBuilder();
            sb.append("updateVolumeColumnSliderH column.stream:");
            sb.append(volumeColumn.stream);
            sb.append(" activeStream:");
            sb.append(this.mActiveStream);
            sb.append(" streamMute:");
            sb.append(z);
            sb.append(" vlevel:");
            sb.append(i);
            sb.append(" column.cachedSliderTint != stateList?:");
            sb.append(volumeColumn.cachedSliderTint != colorStateList);
            Log.d(str, sb.toString());
        }
        if (volumeColumn.cachedSliderTint != colorStateList) {
            ColorStateList unused = volumeColumn.cachedSliderTint = colorStateList;
            volumeColumn.slider.setProgressTintList(colorStateList);
        }
        volumeColumn.slider.setContentDescription(getStreamLabelH(volumeColumn.ss));
        if (!volumeColumn.tracking) {
            int progress = volumeColumn.slider.getProgress();
            int impliedLevel = getImpliedLevel(volumeColumn.slider, progress);
            boolean z2 = volumeColumn.view.getVisibility() == 0;
            boolean z3 = SystemClock.uptimeMillis() - volumeColumn.userAttempt < 1000;
            if (Util.DEBUG) {
                String str2 = TAG;
                Log.d(str2, "updateVolumeColumnSliderH column.stream:" + volumeColumn.stream + " activeStream:" + this.mActiveStream + " progress:" + progress + " level:" + impliedLevel + " columnVisible:" + z2 + " inGracePeriod:" + z3);
            }
            this.mHandler.removeMessages(3, volumeColumn);
            if (this.mShowing && z2 && z3) {
                if (Util.DEBUG) {
                    Log.d(TAG, "inGracePeriod");
                }
                H h = this.mHandler;
                h.sendMessageAtTime(h.obtainMessage(3, volumeColumn), volumeColumn.userAttempt + 1000);
            } else if ((i == impliedLevel && this.mShowing && z2) || progress == (i2 = i * 100)) {
            } else {
                if (!this.mShowing || !z2) {
                    if (volumeColumn.anim != null) {
                        volumeColumn.anim.cancel();
                    }
                    ProgressBarCompat.setProgress(volumeColumn.slider, i2, true);
                } else if (volumeColumn.anim == null || !volumeColumn.anim.isRunning() || volumeColumn.animTargetProgress != i2) {
                    if (volumeColumn.anim == null) {
                        ObjectAnimator unused2 = volumeColumn.anim = ObjectAnimator.ofInt(volumeColumn.slider, "progress", new int[]{progress, i2});
                        volumeColumn.anim.setInterpolator(new DecelerateInterpolator());
                    } else {
                        volumeColumn.anim.cancel();
                        volumeColumn.anim.setIntValues(new int[]{progress, i2});
                    }
                    int unused3 = volumeColumn.animTargetProgress = i2;
                    volumeColumn.anim.setDuration(80);
                    volumeColumn.anim.start();
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void reCheckAllH() {
        if (Util.DEBUG) {
            Log.d(TAG, "recheckH ALL");
        }
        trimObsoleteH();
        for (VolumeColumn updateVolumeColumnH : this.mColumns) {
            updateVolumeColumnH(updateVolumeColumnH);
        }
    }

    /* access modifiers changed from: private */
    public void recheckH(VolumeColumn volumeColumn) {
        if (Util.DEBUG) {
            String str = TAG;
            Log.d(str, "recheckH " + volumeColumn.stream);
        }
        updateVolumeColumnH(volumeColumn);
    }

    /* access modifiers changed from: private */
    public void setStreamImportantH(int i, boolean z) {
        for (VolumeColumn next : this.mColumns) {
            if (next.stream == i) {
                boolean unused = next.important = z;
                return;
            }
        }
    }

    /* access modifiers changed from: private */
    public void showSafetyWarningH(int i) {
        synchronized (this.mSafetyWarningLock) {
            if (this.mSafetyWarning == null) {
                this.mSafetyWarning = new SafetyWarningDialog(this.mContext, this.mController.getAudioManager()) {
                    /* access modifiers changed from: protected */
                    public void cleanUp() {
                        synchronized (MiuiVolumeDialogImpl.this.mSafetyWarningLock) {
                            SafetyWarningDialog unused = MiuiVolumeDialogImpl.this.mSafetyWarning = null;
                        }
                        MiuiVolumeDialogImpl.this.reCheckAllH();
                    }
                };
                this.mSafetyWarning.show();
                reCheckAllH();
                rescheduleTimeoutH();
            }
        }
    }

    private String getStreamLabelH(VolumeDialogController.StreamState streamState) {
        String str = streamState.remoteLabel;
        if (str != null) {
            return str;
        }
        try {
            return this.mContext.getString(streamState.nameRes);
        } catch (Resources.NotFoundException unused) {
            String str2 = TAG;
            Slog.e(str2, "Can't find translation for stream " + streamState);
            return "";
        }
    }

    private boolean applyNewConfig(Resources resources) {
        int updateFrom = this.mLastConfiguration.updateFrom(resources.getConfiguration());
        if (!(this.mLastDensity != resources.getDisplayMetrics().densityDpi) && (updateFrom & -1073741180) == 0) {
            return false;
        }
        this.mLastDensity = resources.getDisplayMetrics().densityDpi;
        return true;
    }

    public void onConfigChanged(Configuration configuration) {
        if (applyNewConfig(this.mContext.getResources())) {
            Log.i(TAG, "onConfigChanged sensitive config changed");
            dismissH(8);
            this.mNeedReInit = true;
            this.mActiveStream = -1;
            this.mConfigurableTexts.update();
            return;
        }
        Log.i(TAG, "onConfigChanged not sensitive.");
    }

    /* access modifiers changed from: private */
    public void recordVolumeChanged(int i, boolean z) {
        if (!z && this.mExpanded) {
            for (VolumeColumn next : this.mColumns) {
                if (next.stream == i && next.tracking) {
                    if (i == 2) {
                        recordCountIfNeed("stream_ring", 2);
                        return;
                    } else if (i == 3) {
                        recordCountIfNeed("stream_music", 1);
                        return;
                    } else if (i != 4) {
                        recordCountIfNeed("stream_other", 8);
                        return;
                    } else {
                        recordCountIfNeed("stream_alarm", 4);
                        return;
                    }
                }
            }
        }
    }

    private void recordCountIfNeed(String str, int i) {
        int i2 = this.mLockRecordTypes;
        if ((i2 & i) == 0) {
            this.mLockRecordTypes = i2 | i;
            Message obtain = Message.obtain(this.mHandler, 9);
            obtain.arg1 = i;
            this.mHandler.sendMessageDelayed(obtain, 2000);
            VolumeEventTracker.trackAdjustVolumeStream(str);
        }
    }

    /* access modifiers changed from: private */
    public void unlockRecordType(int i) {
        this.mLockRecordTypes = (~i) & this.mLockRecordTypes;
    }

    private final class H extends Handler {
        public H() {
            super(Looper.getMainLooper());
        }

        public void handleMessage(Message message) {
            switch (message.what) {
                case 1:
                    MiuiVolumeDialogImpl.this.showH(message.arg1);
                    return;
                case 2:
                    MiuiVolumeDialogImpl.this.dismissH(message.arg1);
                    return;
                case 3:
                    MiuiVolumeDialogImpl.this.recheckH((VolumeColumn) message.obj);
                    return;
                case 4:
                    MiuiVolumeDialogImpl.this.reCheckAllH();
                    return;
                case 5:
                    MiuiVolumeDialogImpl.this.setStreamImportantH(message.arg1, message.arg2 != 0);
                    return;
                case 6:
                    MiuiVolumeDialogImpl.this.rescheduleTimeoutH();
                    return;
                case 7:
                    MiuiVolumeDialogImpl miuiVolumeDialogImpl = MiuiVolumeDialogImpl.this;
                    miuiVolumeDialogImpl.onStateChangedH(miuiVolumeDialogImpl.mState);
                    return;
                case 8:
                    MiuiVolumeDialogImpl.this.vibrateH();
                    return;
                case 9:
                    MiuiVolumeDialogImpl.this.unlockRecordType(message.arg1);
                    return;
                case 10:
                    MiuiVolumeDialogImpl.this.showVolumeDialogH(message.arg1);
                    return;
                default:
                    return;
            }
        }
    }

    private final class CustomDialog extends Dialog {
        public CustomDialog(Context context) {
            super(context, R$style.Theme_MiuiVolumeDialog);
        }

        public boolean dispatchTouchEvent(MotionEvent motionEvent) {
            MiuiVolumeDialogImpl.this.rescheduleTimeoutH();
            return super.dispatchTouchEvent(motionEvent);
        }

        /* access modifiers changed from: protected */
        public void onStart() {
            super.onStart();
            VolumeEventTracker.trackVolumeShowTimeCost();
        }

        /* access modifiers changed from: protected */
        public void onStop() {
            super.onStop();
            boolean isAnimating = MiuiVolumeDialogImpl.this.mDialogView.isAnimating();
            if (Util.DEBUG) {
                String access$700 = MiuiVolumeDialogImpl.TAG;
                Log.d(access$700, "onStop animating=" + isAnimating);
            }
            if (isAnimating) {
                boolean unused = MiuiVolumeDialogImpl.this.mPendingRecheckAll = true;
            } else {
                MiuiVolumeDialogImpl.this.mHandler.sendEmptyMessage(4);
            }
        }

        public boolean onTouchEvent(MotionEvent motionEvent) {
            if (!isShowing()) {
                return false;
            }
            if (motionEvent.getAction() != 4 && motionEvent.getAction() != 0) {
                return false;
            }
            MiuiVolumeDialogImpl.this.dismissH(1);
            return true;
        }

        public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
            return super.dispatchPopulateAccessibilityEvent(accessibilityEvent);
        }

        public boolean dispatchKeyEvent(KeyEvent keyEvent) {
            if (keyEvent.getKeyCode() != 4) {
                return super.dispatchKeyEvent(keyEvent);
            }
            if (keyEvent.getAction() == 1) {
                if (MiuiVolumeDialogImpl.this.mExpanded) {
                    MiuiVolumeDialogImpl.this.updateExpandedH(false, false);
                } else {
                    MiuiVolumeDialogImpl.this.dismissH(7);
                }
            }
            return true;
        }
    }

    private final class VolumeSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {
        private final VolumeColumn mColumn;

        private VolumeSeekBarChangeListener(VolumeColumn volumeColumn) {
            this.mColumn = volumeColumn;
        }

        public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
            int i2;
            if (this.mColumn.ss != null) {
                if (Util.DEBUG) {
                    String access$700 = MiuiVolumeDialogImpl.TAG;
                    Log.d(access$700, AudioSystem.streamToString(this.mColumn.stream) + " onProgressChanged " + i + " fromUser=" + z);
                }
                ColorStateList access$5500 = ((double) (((float) i) / ((float) seekBar.getMax()))) < 0.1d ? MiuiVolumeDialogImpl.this.mIconTintDark : null;
                if (this.mColumn.cachedIconTint != access$5500) {
                    ColorStateList unused = this.mColumn.cachedIconTint = access$5500;
                    this.mColumn.icon.setImageTintList(access$5500);
                }
                if (z) {
                    if (this.mColumn.ss.levelMin > 0 && i < (i2 = this.mColumn.ss.levelMin * 100)) {
                        seekBar.setProgress(i2);
                        i = i2;
                    }
                    int access$5600 = MiuiVolumeDialogImpl.getImpliedLevel(seekBar, i);
                    if (this.mColumn.ss.level != access$5600 || (this.mColumn.ss.muted && access$5600 > 0)) {
                        long unused2 = this.mColumn.userAttempt = SystemClock.uptimeMillis();
                        if (this.mColumn.requestedLevel != access$5600) {
                            MiuiVolumeDialogImpl.this.mController.setStreamVolume(this.mColumn.stream, access$5600);
                            int unused3 = this.mColumn.requestedLevel = access$5600;
                            Events.writeEvent(MiuiVolumeDialogImpl.this.mContext, 9, Integer.valueOf(this.mColumn.stream), Integer.valueOf(access$5600));
                        }
                    }
                }
            }
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
            if (Util.DEBUG) {
                String access$700 = MiuiVolumeDialogImpl.TAG;
                Log.d(access$700, "onStartTrackingTouch " + this.mColumn.stream);
            }
            boolean unused = this.mColumn.tracking = true;
        }

        public void onStopTrackingTouch(SeekBar seekBar) {
            if (Util.DEBUG) {
                String access$700 = MiuiVolumeDialogImpl.TAG;
                Log.d(access$700, "onStopTrackingTouch " + this.mColumn.stream);
            }
            boolean unused = this.mColumn.tracking = false;
            long unused2 = this.mColumn.userAttempt = SystemClock.uptimeMillis();
            int access$5600 = MiuiVolumeDialogImpl.getImpliedLevel(seekBar, seekBar.getProgress());
            Events.writeEvent(MiuiVolumeDialogImpl.this.mContext, 16, Integer.valueOf(this.mColumn.stream), Integer.valueOf(access$5600));
            if (this.mColumn.ss.level != access$5600) {
                MiuiVolumeDialogImpl.this.mHandler.sendMessageDelayed(MiuiVolumeDialogImpl.this.mHandler.obtainMessage(3, this.mColumn), 1000);
            }
        }
    }

    private void sendAccessibilityEventIfNeed(String str) {
        AccessibilityManager accessibilityManager = this.mAccessibilityMgr;
        if (accessibilityManager != null && accessibilityManager.isEnabled()) {
            AccessibilityEvent obtain = AccessibilityEvent.obtain(32);
            obtain.setPackageName(this.mContext.getPackageName());
            obtain.setClassName(CustomDialog.class.getSuperclass().getName());
            obtain.getText().add(str);
            this.mAccessibilityMgr.sendAccessibilityEvent(obtain);
        }
    }

    private final class Accessibility extends View.AccessibilityDelegate implements AccessibilityManager.AccessibilityStateChangeListener {
        private final View.OnAttachStateChangeListener mAttachListener;
        /* access modifiers changed from: private */
        public boolean mFeedbackEnabled;

        private Accessibility() {
            this.mAttachListener = new View.OnAttachStateChangeListener() {
                public void onViewDetachedFromWindow(View view) {
                    if (Util.DEBUG) {
                        Log.d(MiuiVolumeDialogImpl.TAG, "onViewDetachedFromWindow");
                    }
                }

                public void onViewAttachedToWindow(View view) {
                    if (Util.DEBUG) {
                        Log.d(MiuiVolumeDialogImpl.TAG, "onViewAttachedToWindow");
                    }
                    Accessibility.this.updateFeedbackEnabled();
                }
            };
        }

        public void init() {
            Logger.i(MiuiVolumeDialogImpl.TAG, "Accessibility init");
            MiuiVolumeDialogImpl.this.mDialogView.addOnAttachStateChangeListener(this.mAttachListener);
            MiuiVolumeDialogImpl.this.mDialogView.setAccessibilityDelegate(this);
            MiuiVolumeDialogImpl.this.mAccessibilityMgr.addAccessibilityStateChangeListener(this);
            updateFeedbackEnabled();
            MiuiVolumeDialogImpl miuiVolumeDialogImpl = MiuiVolumeDialogImpl.this;
            boolean unused = miuiVolumeDialogImpl.mShowA11yStream = AccessibilityManagerCompat.isAccessibilityVolumeStreamActive(miuiVolumeDialogImpl.mAccessibilityMgr);
        }

        public void destory() {
            Logger.i(MiuiVolumeDialogImpl.TAG, "Accessibility destory");
            MiuiVolumeDialogImpl.this.mDialogView.removeOnAttachStateChangeListener(this.mAttachListener);
            MiuiVolumeDialogImpl.this.mDialogView.setAccessibilityDelegate((View.AccessibilityDelegate) null);
            MiuiVolumeDialogImpl.this.mAccessibilityMgr.removeAccessibilityStateChangeListener(this);
        }

        public boolean onRequestSendAccessibilityEvent(ViewGroup viewGroup, View view, AccessibilityEvent accessibilityEvent) {
            Logger.i(MiuiVolumeDialogImpl.TAG, "Accessibility onRequestSendAccessibilityEvent");
            return super.onRequestSendAccessibilityEvent(viewGroup, view, accessibilityEvent);
        }

        /* access modifiers changed from: private */
        public void updateFeedbackEnabled() {
            this.mFeedbackEnabled = computeFeedbackEnabled();
        }

        private boolean computeFeedbackEnabled() {
            for (AccessibilityServiceInfo accessibilityServiceInfo : MiuiVolumeDialogImpl.this.mAccessibilityMgr.getEnabledAccessibilityServiceList(-1)) {
                int i = accessibilityServiceInfo.feedbackType;
                if (i != 0 && i != 16) {
                    return true;
                }
            }
            return false;
        }

        public void onAccessibilityStateChanged(boolean z) {
            updateFeedbackEnabled();
        }
    }

    private static class VolumeColumn {
        /* access modifiers changed from: private */
        public ObjectAnimator anim;
        /* access modifiers changed from: private */
        public int animTargetProgress;
        /* access modifiers changed from: private */
        public int cachedIconRes;
        /* access modifiers changed from: private */
        public ColorStateList cachedIconTint;
        /* access modifiers changed from: private */
        public ColorStateList cachedSliderTint;
        /* access modifiers changed from: private */
        public ImageView icon;
        /* access modifiers changed from: private */
        public int iconMuteRes;
        /* access modifiers changed from: private */
        public int iconRes;
        /* access modifiers changed from: private */
        public int iconState;
        /* access modifiers changed from: private */
        public boolean important;
        /* access modifiers changed from: private */
        public int initIconMuteRes;
        /* access modifiers changed from: private */
        public int initIconRes;
        /* access modifiers changed from: private */
        public int lastAudibleLevel;
        /* access modifiers changed from: private */
        public int requestedLevel;
        /* access modifiers changed from: private */
        public SeekBar slider;
        /* access modifiers changed from: private */
        public VolumeDialogController.StreamState ss;
        /* access modifiers changed from: private */
        public int stream;
        /* access modifiers changed from: private */
        public boolean tracking;
        /* access modifiers changed from: private */
        public long userAttempt;
        /* access modifiers changed from: private */
        public View view;

        private VolumeColumn() {
            this.requestedLevel = -1;
            this.lastAudibleLevel = 1;
        }
    }

    private static class VolumeColumns {
        private ViewGroup mColumnsCollapsed;
        private ViewGroup mColumnsExpanded;
        private boolean mExpanded;

        VolumeColumns(ViewGroup viewGroup, ViewGroup viewGroup2) {
            this.mColumnsCollapsed = viewGroup;
            this.mColumnsExpanded = viewGroup2;
        }

        public void updateExpandedH(boolean z) {
            this.mExpanded = z;
            Util.reparentChildren(this.mExpanded ? this.mColumnsCollapsed : this.mColumnsExpanded, this.mExpanded ? this.mColumnsExpanded : this.mColumnsCollapsed);
        }

        public ViewGroup getCurrentParent() {
            return this.mExpanded ? this.mColumnsExpanded : this.mColumnsCollapsed;
        }

        public void addView(View view) {
            getCurrentParent().addView(view);
        }

        public void addView(View view, int i) {
            getCurrentParent().addView(view, i);
        }

        public void removeView(View view) {
            getCurrentParent().removeView(view);
        }
    }

    private final class SilenceModeObserver extends ContentObserver {
        private final Uri SILENCE_MODE = Settings.Global.getUriFor("zen_mode");
        private WeakReference<Toast> mLastToast = new WeakReference<>((Object) null);
        private SharedPreferences mSharedPreferences;
        private int mSilenceMode;

        SilenceModeObserver() {
            super(new Handler(Looper.getMainLooper()));
        }

        public void init() {
            this.mSilenceMode = VolumeUtil.getZenMode(MiuiVolumeDialogImpl.this.mContext);
            this.mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(MiuiVolumeDialogImpl.this.mContext);
        }

        public void register() {
            MiuiVolumeDialogImpl.this.mContext.getContentResolver().registerContentObserver(this.SILENCE_MODE, false, this, -1);
        }

        public void unregister() {
            MiuiVolumeDialogImpl.this.mContext.getContentResolver().unregisterContentObserver(this);
        }

        public void updateVolumeInfo(int i) {
            int i2;
            int i3 = this.mSilenceMode;
            this.mSilenceMode = i;
            MiuiVolumeDialogImpl.this.mDialogView.setSilenceMode(i, MiuiVolumeDialogImpl.this.mShowing);
            if (i3 != i) {
                boolean isDeviceProvisioned = ((DeviceProvisionedController) Dependency.get(DeviceProvisionedController.class)).isDeviceProvisioned();
                boolean z = 1 == Settings.Global.getInt(MiuiVolumeDialogImpl.this.mContext.getContentResolver(), "screentime_turn_off_ringer", 0);
                String access$700 = MiuiVolumeDialogImpl.TAG;
                Log.i(access$700, "onChange: screentime=" + z);
                if (!isDeviceProvisioned || z || i != 4 || this.mSharedPreferences.getBoolean("volume_guide_dialog_already_show", false)) {
                    boolean z2 = Settings.System.getIntForUser(MiuiVolumeDialogImpl.this.mContext.getContentResolver(), "mute_music_at_silent", 0, -3) == 0;
                    if (i == 0) {
                        if (z2) {
                            i2 = R$string.miui_toast_zen_standard_to_off;
                        } else {
                            i2 = R$string.miui_toast_zen_standard_to_off_when_shield_media;
                        }
                        if (i3 == 1) {
                            i2 = R$string.miui_toast_zen_dnd_to_off;
                        }
                    } else if (i == 1) {
                        i2 = R$string.miui_toast_zen_to_dnd;
                    } else if (z2) {
                        i2 = R$string.miui_toast_zen_to_standard;
                    } else {
                        i2 = R$string.miui_toast_zen_to_standard_when_shield_media;
                    }
                    if (this.mLastToast.get() != null) {
                        ((Toast) this.mLastToast.get()).cancel();
                    }
                    this.mLastToast = new WeakReference<>(Util.showSystemOverlayToast(MiuiVolumeDialogImpl.this.mContext, i2, 0));
                    return;
                }
                this.mSharedPreferences.edit().putBoolean("volume_guide_dialog_already_show", true).apply();
                showGuideDialog();
            }
        }

        public void onChange(boolean z, Uri uri) {
            super.onChange(z, uri);
            updateVolumeInfo(VolumeUtil.getZenMode(MiuiVolumeDialogImpl.this.mContext));
        }

        private void showGuideDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(MiuiVolumeDialogImpl.this.mContext, R$style.Theme_MiuiVolumeDialog_Alert);
            builder.setMessage((CharSequence) MiuiVolumeDialogImpl.this.mContext.getResources().getString(R$string.miui_guide_dialog_message));
            builder.setPositiveButton((CharSequence) MiuiVolumeDialogImpl.this.mContext.getResources().getString(R$string.miui_guide_dialog_button_positive_text), (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            builder.setNegativeButton((CharSequence) MiuiVolumeDialogImpl.this.mContext.getResources().getString(R$string.miui_guide_dialog_button_negative_text), (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    Log.d(MiuiVolumeDialogImpl.TAG, "showGuideDialog go to set.");
                    ComponentName unflattenFromString = ComponentName.unflattenFromString("com.android.settings/com.android.settings.Settings$MiuiSilentModeAcivity");
                    if (unflattenFromString != null) {
                        Intent intent = new Intent("android.intent.action.MAIN");
                        intent.setComponent(unflattenFromString);
                        intent.setFlags(335544320);
                        ((ActivityStarter) Dependency.get(ActivityStarter.class)).postStartActivityDismissingKeyguard(intent, 0);
                        dialogInterface.dismiss();
                    }
                }
            });
            AlertDialog create = builder.create();
            Window window = create.getWindow();
            window.addFlags(787456);
            window.clearFlags(8388608);
            window.setType(2020);
            create.setCanceledOnTouchOutside(false);
            create.show();
        }
    }
}
