package com.android.systemui.miui.statusbar.phone;

import android.content.Context;
import android.graphics.Rect;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RemoteViews;
import com.android.systemui.Dependency;
import com.android.systemui.miui.statusbar.analytics.SystemUIStat;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.phone.StatusBarTypeController;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;

public class MiuiStatusBarPromptController implements IMiuiStatusBarPrompt, StatusBarTypeController.StatusBarTypeChangeListener {
    private long mBaseTime;
    private WeakReference<Handler> mHandler = new WeakReference<>((Object) null);
    private IMiuiStatusBarPrompt mLastClickablePrompt;
    private boolean mMaskMode;
    private FrameLayout mPromptLayout;
    private View.OnLayoutChangeListener mPromptLayoutListener;
    private int mPromptMaxWidth;
    private int mPromptMaxWidthForNotch;
    private Map<String, OnPromptStateChangedListener> mPromptStateChangedListeners = new HashMap();
    /* access modifiers changed from: private */
    public int mPromptTouchWidth;
    private boolean mPromptVisibileChanged;
    private int mRecorderState;
    private long mRecordingPausedTime;
    private long mRecordingStartTime;
    /* access modifiers changed from: private */
    public Rect mRegion = new Rect();
    private String mReturnToCallState;
    private boolean mShowReturnToDrive;
    private int mSilentModeDefault = -1;
    private Map<String, MiuiStatusBarPromptImpl> mStatusBarPrompts = new HashMap();
    private LinkedHashMap<String, State> mStatusBarStatesMap = new LinkedHashMap<>();
    private LinkedHashMap<Integer, LinkedList<String>> mStatusBarStatesPriorityMap = new LinkedHashMap<>();
    private StatusBarTypeController mStatusBarTypeHelper;
    /* access modifiers changed from: private */
    public View mStatusBarView;
    private String mTopStatusBarModeState = "legacy_nromal";
    /* access modifiers changed from: private */
    public int mTouchRegionExpandValue;

    public interface OnPromptStateChangedListener {
        void onPromptStateChanged(boolean z, String str);
    }

    public void updateTouchRegion() {
    }

    public MiuiStatusBarPromptController() {
        this.mStatusBarStatesPriorityMap.put(3, new LinkedList());
        this.mStatusBarStatesPriorityMap.put(2, new LinkedList());
        this.mStatusBarStatesPriorityMap.put(1, new LinkedList());
        this.mStatusBarStatesPriorityMap.put(0, new LinkedList());
    }

    public void setHandler(Handler handler) {
        this.mHandler = new WeakReference<>(handler);
    }

    public void addStatusBarPrompt(String str, StatusBar statusBar, ViewGroup viewGroup, int i, OnPromptStateChangedListener onPromptStateChangedListener) {
        this.mStatusBarPrompts.put(str, new MiuiStatusBarPromptImpl(statusBar, viewGroup, i));
        if (viewGroup != null) {
            this.mStatusBarView = viewGroup;
        }
        if (getContext() != null) {
            this.mPromptMaxWidth = getContext().getResources().getDimensionPixelSize(R.dimen.prompt_max_width);
            this.mPromptMaxWidthForNotch = getContext().getResources().getDimensionPixelSize(R.dimen.statusbar_carrier_max_width);
            this.mTouchRegionExpandValue = getContext().getResources().getDimensionPixelOffset(R.dimen.prompt_touch_area_expand);
            updateTouchRegion();
        }
        addPromptStateChangedListener(str, onPromptStateChangedListener);
        if (this.mStatusBarTypeHelper == null) {
            this.mStatusBarTypeHelper = (StatusBarTypeController) Dependency.get(StatusBarTypeController.class);
            this.mStatusBarTypeHelper.addCallback(this);
        }
    }

    public void onCutoutTypeChanged() {
        showReturnToRecorderView(false);
        showReturnToSafeBar(false);
        showReturnToDriveMode(false);
        showReturnToInCall(false);
        showReturnToMulti(false);
        showReturnToSosBar(false);
        if (((StatusBarTypeController) Dependency.get(StatusBarTypeController.class)).getCutoutType() == StatusBarTypeController.CutoutType.NONE) {
            this.mPromptTouchWidth = this.mPromptMaxWidth;
        } else {
            this.mPromptTouchWidth = this.mPromptMaxWidthForNotch;
        }
        updateTouchRegion();
        for (MiuiStatusBarPromptImpl updateViewWidth : this.mStatusBarPrompts.values()) {
            updateViewWidth.updateViewWidth(this.mPromptTouchWidth);
        }
        if ("legacy_recorder".equals(getStatusBarModeState())) {
            forceRefreshRecorder();
        }
        if (!TextUtils.isEmpty(this.mReturnToCallState)) {
            showReturnToInCallScreenButton(this.mReturnToCallState, this.mBaseTime);
        }
        showReturnToDriveModeView(this.mShowReturnToDrive, this.mMaskMode);
        dispatchShowPrompt();
    }

    public void addPromptStateChangedListener(String str, OnPromptStateChangedListener onPromptStateChangedListener) {
        this.mPromptStateChangedListeners.put(str, onPromptStateChangedListener);
    }

    public void addAndUpdatePromptStateChangedListener(String str, OnPromptStateChangedListener onPromptStateChangedListener) {
        this.mPromptStateChangedListeners.put(str, onPromptStateChangedListener);
        onPromptStateChangedListener.onPromptStateChanged("legacy_nromal".equals(this.mTopStatusBarModeState), this.mTopStatusBarModeState);
    }

    public void removePromptStateChangedListener(String str) {
        this.mPromptStateChangedListeners.remove(str);
    }

    public void removePrompt(String str) {
        MiuiStatusBarPromptImpl miuiStatusBarPromptImpl = this.mStatusBarPrompts.get(str);
        miuiStatusBarPromptImpl.showReturnToRecorderView(false);
        miuiStatusBarPromptImpl.showReturnToSafeBar(false);
        miuiStatusBarPromptImpl.showReturnToDriveMode(false);
        miuiStatusBarPromptImpl.showReturnToInCall(false);
        miuiStatusBarPromptImpl.showReturnToMulti(false);
        miuiStatusBarPromptImpl.showReturnToSosBar(false);
        miuiStatusBarPromptImpl.updateStateViews("legacy_nromal");
        this.mStatusBarPrompts.remove(str);
        removePromptStateChangedListener(str);
    }

    public void setPromptSosTypeImage(String str) {
        this.mStatusBarPrompts.get(str).setSosTypeImage();
    }

    public String calculateTopTag() {
        this.mStatusBarStatesMap.keySet().toArray(new String[this.mStatusBarStatesMap.size()]);
        if (this.mStatusBarStatesMap.size() == 0) {
            return "legacy_nromal";
        }
        return getShowStateTag();
    }

    /* access modifiers changed from: private */
    public boolean needExpandTouchRegion() {
        return !"legacy_nromal".equals(calculateTopTag());
    }

    public Rect getTouchRegion() {
        return this.mRegion;
    }

    public void setPromptLayout(FrameLayout frameLayout) {
        FrameLayout frameLayout2 = this.mPromptLayout;
        if (frameLayout2 != null) {
            frameLayout2.removeOnLayoutChangeListener(this.mPromptLayoutListener);
        }
        this.mPromptLayout = frameLayout;
        this.mPromptLayoutListener = new View.OnLayoutChangeListener() {
            public void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
                int[] iArr = new int[2];
                view.getLocationOnScreen(iArr);
                MiuiStatusBarPromptController.this.mRegion.left = iArr[0];
                MiuiStatusBarPromptController.this.mRegion.right = (MiuiStatusBarPromptController.this.mRegion.left + i3) - i;
                MiuiStatusBarPromptController.this.mRegion.top = MiuiStatusBarPromptController.this.mStatusBarView.getTop();
                MiuiStatusBarPromptController.this.mRegion.bottom = MiuiStatusBarPromptController.this.mStatusBarView.getBottom();
                if (MiuiStatusBarPromptController.this.needExpandTouchRegion()) {
                    MiuiStatusBarPromptController.this.mRegion.bottom += MiuiStatusBarPromptController.this.mTouchRegionExpandValue;
                }
                if (MiuiStatusBarPromptController.this.mPromptTouchWidth != 0) {
                    if (TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) == 0) {
                        MiuiStatusBarPromptController.this.mRegion.right = MiuiStatusBarPromptController.this.mRegion.left + MiuiStatusBarPromptController.this.mPromptTouchWidth;
                        return;
                    }
                    MiuiStatusBarPromptController.this.mRegion.left = MiuiStatusBarPromptController.this.mRegion.right - MiuiStatusBarPromptController.this.mPromptTouchWidth;
                }
            }
        };
        this.mPromptLayout.addOnLayoutChangeListener(this.mPromptLayoutListener);
    }

    public void updateViews() {
        String calculateTopTag = calculateTopTag();
        this.mPromptVisibileChanged = "legacy_nromal".equals(this.mTopStatusBarModeState) ^ "legacy_nromal".equals(calculateTopTag);
        this.mTopStatusBarModeState = calculateTopTag;
        showReturnToRecorderView("legacy_recorder".equals(calculateTopTag));
        showReturnToSafeBar("legacy_safe".equals(calculateTopTag));
        showReturnToDriveMode("legacy_drive".equals(calculateTopTag));
        showReturnToInCall("legacy_call".equals(calculateTopTag));
        showReturnToMulti("legacy_multi".equals(calculateTopTag));
        showReturnToSosBar("legacy_sos".equals(calculateTopTag));
        updateStateViews(calculateTopTag);
        if (this.mPromptVisibileChanged) {
            updateTouchRegion();
            for (MiuiStatusBarPromptImpl updateTouchArea : this.mStatusBarPrompts.values()) {
                updateTouchArea.updateTouchArea(!"legacy_nromal".equals(calculateTopTag), this.mTouchRegionExpandValue);
            }
        }
    }

    public void showReturnToRecorderView(boolean z) {
        for (MiuiStatusBarPromptImpl showReturnToRecorderView : this.mStatusBarPrompts.values()) {
            showReturnToRecorderView.showReturnToRecorderView(z);
        }
    }

    public void hideReturnToRecorderView() {
        clearState("legacy_recorder");
        for (MiuiStatusBarPromptImpl hideReturnToRecorderView : this.mStatusBarPrompts.values()) {
            hideReturnToRecorderView.hideReturnToRecorderView();
        }
    }

    public void showReturnToRecorderView(String str, boolean z, long j) {
        setState("legacy_recorder", (State) null, 1);
        for (MiuiStatusBarPromptImpl showReturnToRecorderView : this.mStatusBarPrompts.values()) {
            showReturnToRecorderView.showReturnToRecorderView(str, z, j);
        }
    }

    public void showReturnToSafeBar(boolean z) {
        for (MiuiStatusBarPromptImpl showReturnToSafeBar : this.mStatusBarPrompts.values()) {
            showReturnToSafeBar.showReturnToSafeBar(z);
        }
    }

    public void showSafePayStatusBar(int i, Bundle bundle) {
        setState("legacy_safe", (State) null, 1);
        for (MiuiStatusBarPromptImpl showSafePayStatusBar : this.mStatusBarPrompts.values()) {
            showSafePayStatusBar.showSafePayStatusBar(i, bundle);
        }
    }

    public void hideSafePayStatusBar() {
        clearState("legacy_safe");
        for (MiuiStatusBarPromptImpl hideSafePayStatusBar : this.mStatusBarPrompts.values()) {
            hideSafePayStatusBar.hideSafePayStatusBar();
        }
    }

    public void showSosStatusBar() {
        setState("legacy_sos", (State) null, 2);
        for (MiuiStatusBarPromptImpl showSosStatusBar : this.mStatusBarPrompts.values()) {
            showSosStatusBar.showSosStatusBar();
        }
    }

    public void hideSosStatusBar() {
        clearState("legacy_sos");
        for (MiuiStatusBarPromptImpl hideSosStatusBar : this.mStatusBarPrompts.values()) {
            hideSosStatusBar.hideSosStatusBar();
        }
    }

    public void showReturnToSosBar(boolean z) {
        for (MiuiStatusBarPromptImpl showReturnToSosBar : this.mStatusBarPrompts.values()) {
            showReturnToSosBar.showReturnToSosBar(z);
        }
    }

    public void updateSosImageDark(boolean z, Rect rect, float f) {
        for (MiuiStatusBarPromptImpl updateSosImageDark : this.mStatusBarPrompts.values()) {
            updateSosImageDark.updateSosImageDark(z, rect, f);
        }
    }

    public void showReturnToDriveMode(boolean z) {
        for (MiuiStatusBarPromptImpl showReturnToDriveMode : this.mStatusBarPrompts.values()) {
            showReturnToDriveMode.showReturnToDriveMode(z);
        }
    }

    public void showReturnToDriveModeView(boolean z, boolean z2) {
        this.mShowReturnToDrive = z;
        this.mMaskMode = z2;
        if (z) {
            setState("legacy_drive", (State) null, 1);
        } else {
            clearState("legacy_drive");
        }
        for (MiuiStatusBarPromptImpl showReturnToDriveModeView : this.mStatusBarPrompts.values()) {
            showReturnToDriveModeView.showReturnToDriveModeView(z, z2);
        }
    }

    public void showReturnToInCall(boolean z) {
        for (MiuiStatusBarPromptImpl showReturnToInCall : this.mStatusBarPrompts.values()) {
            showReturnToInCall.showReturnToInCall(z);
        }
    }

    public void showReturnToInCallScreenButton(String str, long j) {
        this.mReturnToCallState = str;
        this.mBaseTime = j;
        setState("legacy_call", (State) null, 3);
        for (MiuiStatusBarPromptImpl showReturnToInCallScreenButton : this.mStatusBarPrompts.values()) {
            showReturnToInCallScreenButton.showReturnToInCallScreenButton(str, j);
        }
    }

    public void hideReturnToInCallScreenButton() {
        this.mReturnToCallState = "";
        this.mBaseTime = 0;
        clearState("legacy_call");
        for (MiuiStatusBarPromptImpl hideReturnToInCallScreenButton : this.mStatusBarPrompts.values()) {
            hideReturnToInCallScreenButton.hideReturnToInCallScreenButton();
        }
    }

    public void makeReturnToInCallScreenButtonVisible() {
        for (MiuiStatusBarPromptImpl makeReturnToInCallScreenButtonVisible : this.mStatusBarPrompts.values()) {
            makeReturnToInCallScreenButtonVisible.makeReturnToInCallScreenButtonVisible();
        }
    }

    public void makeReturnToInCallScreenButtonGone() {
        for (MiuiStatusBarPromptImpl makeReturnToInCallScreenButtonGone : this.mStatusBarPrompts.values()) {
            makeReturnToInCallScreenButtonGone.makeReturnToInCallScreenButtonGone();
        }
    }

    public void showReturnToMulti(boolean z) {
        for (MiuiStatusBarPromptImpl showReturnToMulti : this.mStatusBarPrompts.values()) {
            showReturnToMulti.showReturnToMulti(z);
        }
    }

    public void updateStateViews(String str) {
        for (MiuiStatusBarPromptImpl updateStateViews : this.mStatusBarPrompts.values()) {
            updateStateViews.updateStateViews(str);
        }
    }

    public boolean blockClickAction() {
        for (IMiuiStatusBarPrompt next : this.mStatusBarPrompts.values()) {
            if (next.blockClickAction()) {
                this.mLastClickablePrompt = next;
                return true;
            }
        }
        return false;
    }

    public void handleClickAction() {
        if (this.mLastClickablePrompt != null) {
            ((SystemUIStat) Dependency.get(SystemUIStat.class)).handleClickStatusBarPromptEvent(this.mTopStatusBarModeState);
            this.mLastClickablePrompt.handleClickAction();
            this.mLastClickablePrompt = null;
        }
    }

    public Context getContext() {
        for (MiuiStatusBarPromptImpl context : this.mStatusBarPrompts.values()) {
            Context context2 = context.getContext();
            if (context2 != null) {
                return context2;
            }
        }
        return null;
    }

    public State getState(String str) {
        return this.mStatusBarStatesMap.get(str);
    }

    private String formatStateTag(Bundle bundle) {
        return bundle.getString("key_status_bar_package_name") + "." + bundle.getString("key_status_bar_tag");
    }

    public void setStatus(int i, String str, Bundle bundle) {
        if (!TextUtils.isEmpty(str)) {
            if (str.contains("action_set_status_bar_state")) {
                if (i == 0) {
                    clearState(str.replace("action_set_status_bar_state.", ""));
                    return;
                }
                State state = new State();
                state.standardState = (RemoteViews) bundle.getParcelable("key_status_bar_standard_state");
                state.miniState = (RemoteViews) bundle.getParcelable("key_status_bar_mini_state");
                setState(formatStateTag(bundle), state, bundle.getInt("key_status_bar_priority"));
            } else if ("action_clear_status_bar_state".equals(str)) {
                clearState(formatStateTag(bundle));
            } else {
                int i2 = 2;
                if ("com.miui.app.ExtraStatusBarManager.action_status_recorder".equals(str)) {
                    if (i == 0) {
                        this.mRecorderState = 0;
                        hideReturnToRecorderView();
                    } else if (i == 1) {
                        long j = bundle.getLong("com.miui.app.ExtraStatusBarManager.extra_recorder_duration", 0);
                        this.mRecordingPausedTime = SystemClock.elapsedRealtime();
                        this.mRecordingStartTime = this.mRecordingPausedTime - j;
                        String string = bundle.getString("com.miui.app.ExtraStatusBarManager.extra_recorder_title");
                        boolean z = bundle.getBoolean("com.miui.app.ExtraStatusBarManager.extra_recorder_timer_on_off", false);
                        if (z) {
                            i2 = 1;
                        }
                        this.mRecorderState = i2;
                        showReturnToRecorderView(string, z, j);
                    } else if (i == 2) {
                        boolean z2 = bundle.getBoolean("com.miui.app.ExtraStatusBarManager.extra_recorder_silent_mode_changed_by_user");
                        boolean z3 = bundle.getBoolean("com.miui.app.ExtraStatusBarManager.extra_recorder_enter_silent_mode");
                        if (z2) {
                            this.mSilentModeDefault = -1;
                        } else {
                            setSilenceWhenRecording(z3);
                        }
                    } else if (i == 3) {
                        setSilenceWhenRecording(false);
                        this.mRecorderState = 0;
                        hideReturnToRecorderView();
                    }
                } else if ("com.miui.app.ExtraStatusBarManager.action_status_safepay".equals(str)) {
                    if (i == 0) {
                        hideSafePayStatusBar();
                    } else if (i == 2 || i == 3 || i == 4) {
                        showSafePayStatusBar(i, bundle);
                    }
                } else if (!"com.miui.app.ExtraStatusBarManager.action_status_sos".equals(str)) {
                } else {
                    if (i == 0) {
                        hideSosStatusBar();
                    } else if (i == 1) {
                        showSosStatusBar();
                    }
                }
            }
        }
    }

    private void setSilenceWhenRecording(boolean z) {
        if (getContext() != null) {
            AudioManager audioManager = (AudioManager) getContext().getSystemService("audio");
            int ringerMode = audioManager.getRingerMode();
            if (z) {
                boolean z2 = ringerMode != 2;
                this.mSilentModeDefault = ringerMode;
                if (z != z2) {
                    audioManager.setRingerMode(0);
                    return;
                }
                return;
            }
            int i = this.mSilentModeDefault;
            if (i != -1) {
                if (i != ringerMode) {
                    audioManager.setRingerMode(i);
                }
                this.mSilentModeDefault = -1;
            }
        }
    }

    public void dealWithRecordState() {
        int i;
        if (getContext() != null && (i = this.mRecorderState) != 0) {
            if (i == 1) {
                showReturnToRecorderView(getContext().getString(R.string.status_bar_recording_back), true, SystemClock.elapsedRealtime() - this.mRecordingStartTime);
            } else if (i == 2) {
                showReturnToRecorderView(getContext().getString(R.string.status_bar_recording_pause), false, this.mRecordingPausedTime - this.mRecordingStartTime);
            }
        }
    }

    /* access modifiers changed from: private */
    public void showPrompt() {
        updateViews();
        boolean equals = "legacy_nromal".equals(this.mTopStatusBarModeState);
        if (this.mPromptVisibileChanged) {
            for (OnPromptStateChangedListener onPromptStateChanged : this.mPromptStateChangedListeners.values()) {
                onPromptStateChanged.onPromptStateChanged(equals, this.mTopStatusBarModeState);
            }
        }
        if ("legacy_nromal".equals(this.mTopStatusBarModeState)) {
            ((SystemUIStat) Dependency.get(SystemUIStat.class)).handleShowStatusBarPromptEvent(this.mTopStatusBarModeState);
        }
    }

    private void dispatchShowPrompt() {
        if (this.mHandler.get() != null) {
            ((Handler) this.mHandler.get()).post(new Runnable() {
                public void run() {
                    MiuiStatusBarPromptController.this.showPrompt();
                }
            });
        } else if (Looper.myLooper() == Looper.getMainLooper()) {
            showPrompt();
        } else {
            Log.e("MiuiStatusBarPrompt", "dispatchShowPrompt abandoned, not in main thread");
        }
    }

    public void setState(String str, State state, int i) {
        if (setMiuiStatusState(str, state, i)) {
            dispatchShowPrompt();
        }
    }

    public void clearState(String str) {
        removeMiuiStatusState(str);
        dispatchShowPrompt();
    }

    private boolean setMiuiStatusState(String str, State state, int i) {
        LinkedList linkedList = this.mStatusBarStatesPriorityMap.get(Integer.valueOf(i));
        if (linkedList == null) {
            return false;
        }
        linkedList.remove(str);
        linkedList.add(str);
        this.mStatusBarStatesMap.remove(str);
        this.mStatusBarStatesMap.put(str, state);
        return true;
    }

    private void removeMiuiStatusState(String str) {
        for (Map.Entry<Integer, LinkedList<String>> value : this.mStatusBarStatesPriorityMap.entrySet()) {
            ((LinkedList) value.getValue()).remove(str);
        }
        this.mStatusBarStatesMap.remove(str);
    }

    private String getShowStateTag() {
        LinkedList linkedList;
        Iterator<Map.Entry<Integer, LinkedList<String>>> it = this.mStatusBarStatesPriorityMap.entrySet().iterator();
        while (true) {
            if (!it.hasNext()) {
                linkedList = null;
                break;
            }
            linkedList = (LinkedList) it.next().getValue();
            if (linkedList.size() > 0) {
                break;
            }
        }
        if (linkedList != null) {
            return (String) linkedList.get(linkedList.size() - 1);
        }
        return null;
    }

    public boolean isShowingState(String str) {
        return str.equals(this.mTopStatusBarModeState);
    }

    public boolean isStateNormal() {
        return this.mTopStatusBarModeState == "legacy_nromal";
    }

    public String getStatusBarModeState() {
        return this.mTopStatusBarModeState;
    }

    public void forceRefreshRecorder() {
        if (this.mHandler.get() != null) {
            ((Handler) this.mHandler.get()).post(new Runnable() {
                public void run() {
                    MiuiStatusBarPromptController.this.dealWithRecordState();
                }
            });
        }
    }

    public class State {
        RemoteViews miniState;
        RemoteViews standardState;

        public State() {
        }
    }
}
