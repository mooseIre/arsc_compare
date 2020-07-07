package com.android.keyguard.negative;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.AttributeSet;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.magazine.LockScreenMagazineController;
import com.android.keyguard.magazine.LockScreenMagazineUtils;
import com.android.keyguard.utils.PackageUtils;
import com.android.systemui.statusbar.phone.NotificationPanelView;

public class MiuiKeyguardMoveLeftLockScreenMagazineView extends MiuiKeyguardMoveLeftBaseView {
    /* access modifiers changed from: private */
    public Drawable mLeftViewBackgroundImageDrawable;
    /* access modifiers changed from: private */
    public Drawable mLeftViewPreImageDrawable;
    /* access modifiers changed from: private */
    public LockScreenMagazineController mLockScreenMagazineController;
    /* access modifiers changed from: private */
    public NotificationPanelView mPanel;
    /* access modifiers changed from: private */
    public KeyguardUpdateMonitor mUpdateMonitor;

    public void initLeftView() {
    }

    public void uploadData() {
    }

    public MiuiKeyguardMoveLeftLockScreenMagazineView(Context context) {
        this(context, (AttributeSet) null);
    }

    public MiuiKeyguardMoveLeftLockScreenMagazineView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mUpdateMonitor = KeyguardUpdateMonitor.getInstance(context);
        this.mLockScreenMagazineController = LockScreenMagazineController.getInstance(context);
    }

    public void setPanel(NotificationPanelView notificationPanelView) {
        this.mPanel = notificationPanelView;
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
    }

    public boolean isSupportRightMove() {
        return KeyguardUpdateMonitor.getInstance(this.mContext).isSupportLockScreenMagazineLeft() || KeyguardUpdateMonitor.getInstance(this.mContext).isLockScreenLeftOverlayAvailable();
    }

    public void setCustomBackground() {
        if (this.mUpdateMonitor.isSupportLockScreenMagazineLeft()) {
            new AsyncTask<Void, Void, Void>() {
                /* access modifiers changed from: protected */
                public Void doInBackground(Void... voidArr) {
                    if (!MiuiKeyguardMoveLeftLockScreenMagazineView.this.mUpdateMonitor.isUserUnlocked()) {
                        return null;
                    }
                    MiuiKeyguardMoveLeftLockScreenMagazineView miuiKeyguardMoveLeftLockScreenMagazineView = MiuiKeyguardMoveLeftLockScreenMagazineView.this;
                    Drawable unused = miuiKeyguardMoveLeftLockScreenMagazineView.mLeftViewPreImageDrawable = PackageUtils.getDrawableFromPackage(miuiKeyguardMoveLeftLockScreenMagazineView.mContext, LockScreenMagazineUtils.LOCK_SCREEN_MAGAZINE_PACKAGE_NAME, MiuiKeyguardMoveLeftLockScreenMagazineView.this.mLockScreenMagazineController.getPreLeftScreenDrawableResName());
                    MiuiKeyguardMoveLeftLockScreenMagazineView miuiKeyguardMoveLeftLockScreenMagazineView2 = MiuiKeyguardMoveLeftLockScreenMagazineView.this;
                    Drawable unused2 = miuiKeyguardMoveLeftLockScreenMagazineView2.mLeftViewBackgroundImageDrawable = PackageUtils.getDrawableFromPackage(miuiKeyguardMoveLeftLockScreenMagazineView2.mContext, LockScreenMagazineUtils.LOCK_SCREEN_MAGAZINE_PACKAGE_NAME, MiuiKeyguardMoveLeftLockScreenMagazineView.this.mLockScreenMagazineController.getPreTransToLeftScreenDrawableResName());
                    return null;
                }

                /* access modifiers changed from: protected */
                public void onPostExecute(Void voidR) {
                    MiuiKeyguardMoveLeftLockScreenMagazineView miuiKeyguardMoveLeftLockScreenMagazineView = MiuiKeyguardMoveLeftLockScreenMagazineView.this;
                    miuiKeyguardMoveLeftLockScreenMagazineView.setBackgroundDrawable(miuiKeyguardMoveLeftLockScreenMagazineView.mLeftViewPreImageDrawable != null ? MiuiKeyguardMoveLeftLockScreenMagazineView.this.mLeftViewPreImageDrawable : null);
                    if (MiuiKeyguardMoveLeftLockScreenMagazineView.this.mLeftViewBackgroundImageDrawable != null) {
                        MiuiKeyguardMoveLeftLockScreenMagazineView.this.mPanel.getLeftViewBg().setBackground(MiuiKeyguardMoveLeftLockScreenMagazineView.this.mLeftViewBackgroundImageDrawable);
                    } else {
                        MiuiKeyguardMoveLeftLockScreenMagazineView.this.mPanel.getLeftViewBg().setBackgroundColor(MiuiKeyguardMoveLeftLockScreenMagazineView.this.mUpdateMonitor.getWallpaperBlurColor());
                    }
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
            return;
        }
        setBackgroundDrawable((Drawable) null);
        this.mPanel.getLeftViewBg().setBackgroundColor(this.mUpdateMonitor.getWallpaperBlurColor());
        this.mLeftViewPreImageDrawable = null;
        this.mLeftViewBackgroundImageDrawable = null;
    }

    public boolean hasBackgroundImageDrawable() {
        return this.mLeftViewBackgroundImageDrawable != null;
    }
}
