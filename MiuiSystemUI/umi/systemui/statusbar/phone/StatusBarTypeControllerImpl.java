package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.hardware.display.DisplayManager;
import android.os.Handler;
import android.provider.MiuiSettings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.DisplayCutout;
import android.view.DisplayInfo;
import android.view.WindowManager;
import com.android.systemui.DisplayCutoutCompat;
import com.android.systemui.statusbar.phone.StatusBarTypeController;
import com.miui.systemui.annotation.Inject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;

public class StatusBarTypeControllerImpl implements StatusBarTypeController {
    private final ArrayList<StatusBarTypeController.StatusBarTypeChangeListener> mChangeListeners = new ArrayList<>();
    private final Context mContext;
    private StatusBarTypeController.CutoutType mCurrentType;
    /* access modifiers changed from: private */
    public Display mDisplay;
    /* access modifiers changed from: private */
    public DisplayInfo mInfo = new DisplayInfo();
    /* access modifiers changed from: private */
    public DisplayCutout mLastCutout;
    /* access modifiers changed from: private */
    public int mLastRotation;
    private StatusBarTypeController.CutoutType mPreType;
    private int mScreenWidth;

    public StatusBarTypeControllerImpl(@Inject Context context) {
        this.mContext = context;
        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.mDisplay = ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay();
        this.mDisplay.getRealMetrics(displayMetrics);
        this.mScreenWidth = displayMetrics.widthPixels;
        this.mDisplay.getDisplayInfo(this.mInfo);
        updateLastRotationAndCutout();
        if (MiuiSettings.Global.getBoolean(this.mContext.getContentResolver(), "force_black_v2")) {
            this.mCurrentType = StatusBarTypeController.CutoutType.NONE;
        } else {
            this.mCurrentType = getCurrentCutOutType();
        }
        ((DisplayManager) this.mContext.getSystemService("display")).registerDisplayListener(new DisplayManager.DisplayListener() {
            public void onDisplayAdded(int i) {
            }

            public void onDisplayRemoved(int i) {
            }

            public void onDisplayChanged(int i) {
                StatusBarTypeControllerImpl.this.mDisplay.getDisplayInfo(StatusBarTypeControllerImpl.this.mInfo);
                if (StatusBarTypeControllerImpl.this.mLastRotation != StatusBarTypeControllerImpl.this.mInfo.rotation || !Objects.equals(StatusBarTypeControllerImpl.this.mLastCutout, StatusBarTypeControllerImpl.this.mInfo.displayCutout)) {
                    StatusBarTypeControllerImpl.this.updateLastRotationAndCutout();
                    StatusBarTypeControllerImpl.this.switchStatusBarType(StatusBarTypeControllerImpl.this.getCurrentCutOutType());
                }
            }
        }, (Handler) null);
    }

    /* access modifiers changed from: private */
    public void updateLastRotationAndCutout() {
        DisplayInfo displayInfo = this.mInfo;
        this.mLastCutout = displayInfo.displayCutout;
        this.mLastRotation = displayInfo.rotation;
    }

    /* access modifiers changed from: private */
    public StatusBarTypeController.CutoutType getCurrentCutOutType() {
        if (isLandscape()) {
            return StatusBarTypeController.CutoutType.NONE;
        }
        if (!DisplayCutoutCompat.isCutoutSymmetrical(this.mInfo, this.mScreenWidth)) {
            return StatusBarTypeController.CutoutType.HOLE;
        }
        int cutoutWidth = DisplayCutoutCompat.getCutoutWidth(this.mInfo, this.mContext);
        if (cutoutWidth <= 0) {
            return StatusBarTypeController.CutoutType.NONE;
        }
        int i = this.mScreenWidth;
        float f = (float) cutoutWidth;
        if (((double) (((float) i) / f)) > 3.5d) {
            return StatusBarTypeController.CutoutType.DRIP;
        }
        if (((float) i) / f > 2.0f) {
            return StatusBarTypeController.CutoutType.NARROW_NOTCH;
        }
        return StatusBarTypeController.CutoutType.NOTCH;
    }

    private boolean isLandscape() {
        int i = this.mLastRotation;
        return i == 1 || i == 3;
    }

    /* access modifiers changed from: private */
    public void switchStatusBarType(StatusBarTypeController.CutoutType cutoutType) {
        if (this.mCurrentType != cutoutType) {
            String simpleName = StatusBarTypeControllerImpl.class.getSimpleName();
            StringBuilder sb = new StringBuilder();
            sb.append(" statusbar cutout type from ");
            StatusBarTypeController.CutoutType cutoutType2 = this.mCurrentType;
            String str = null;
            sb.append(cutoutType2 == null ? null : cutoutType2.name());
            sb.append(" to ");
            if (cutoutType != null) {
                str = cutoutType.name();
            }
            sb.append(str);
            Log.d(simpleName, sb.toString());
            this.mPreType = this.mCurrentType;
            this.mCurrentType = cutoutType;
            Iterator<StatusBarTypeController.StatusBarTypeChangeListener> it = this.mChangeListeners.iterator();
            while (it.hasNext()) {
                it.next().onCutoutTypeChanged();
            }
        }
    }

    public StatusBarTypeController.CutoutType getCutoutType() {
        return this.mCurrentType;
    }

    public boolean hasCutout() {
        return this.mCurrentType != StatusBarTypeController.CutoutType.NONE;
    }

    public void addCallback(StatusBarTypeController.StatusBarTypeChangeListener statusBarTypeChangeListener) {
        synchronized (this.mChangeListeners) {
            this.mChangeListeners.add(statusBarTypeChangeListener);
        }
    }

    public void removeCallback(StatusBarTypeController.StatusBarTypeChangeListener statusBarTypeChangeListener) {
        synchronized (this.mChangeListeners) {
            this.mChangeListeners.remove(statusBarTypeChangeListener);
        }
    }
}
