package com.android.systemui;

import android.app.ActivityTaskManager;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.hardware.display.DisplayManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.util.SparseArray;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.SizeCompatModeActivityController;
import com.android.systemui.shared.system.ActivityManagerWrapper;
import com.android.systemui.shared.system.TaskStackChangeListener;
import com.android.systemui.statusbar.CommandQueue;
import java.lang.ref.WeakReference;

public class SizeCompatModeActivityController extends SystemUI implements CommandQueue.Callbacks {
    private final SparseArray<RestartActivityButton> mActiveButtons = new SparseArray<>(1);
    private final CommandQueue mCommandQueue;
    private final SparseArray<WeakReference<Context>> mDisplayContextCache = new SparseArray<>(0);
    private boolean mHasShownHint;

    @VisibleForTesting
    SizeCompatModeActivityController(Context context, ActivityManagerWrapper activityManagerWrapper, CommandQueue commandQueue) {
        super(context);
        this.mCommandQueue = commandQueue;
        activityManagerWrapper.registerTaskStackListener(new TaskStackChangeListener() {
            /* class com.android.systemui.SizeCompatModeActivityController.AnonymousClass1 */

            @Override // com.android.systemui.shared.system.TaskStackChangeListener
            public void onSizeCompatModeActivityChanged(int i, IBinder iBinder) {
                SizeCompatModeActivityController.this.updateRestartButton(i, iBinder);
            }
        });
    }

    @Override // com.android.systemui.SystemUI
    public void start() {
        this.mCommandQueue.addCallback((CommandQueue.Callbacks) this);
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void setImeWindowStatus(int i, IBinder iBinder, int i2, int i3, boolean z) {
        RestartActivityButton restartActivityButton = this.mActiveButtons.get(i);
        if (restartActivityButton != null) {
            int i4 = 0;
            if ((i2 & 2) != 0) {
                i4 = 8;
            }
            if (restartActivityButton.getVisibility() != i4) {
                restartActivityButton.setVisibility(i4);
            }
        }
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void onDisplayRemoved(int i) {
        this.mDisplayContextCache.remove(i);
        removeRestartButton(i);
    }

    private void removeRestartButton(int i) {
        RestartActivityButton restartActivityButton = this.mActiveButtons.get(i);
        if (restartActivityButton != null) {
            restartActivityButton.remove();
            this.mActiveButtons.remove(i);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateRestartButton(int i, IBinder iBinder) {
        if (iBinder == null) {
            removeRestartButton(i);
            return;
        }
        RestartActivityButton restartActivityButton = this.mActiveButtons.get(i);
        if (restartActivityButton != null) {
            restartActivityButton.updateLastTargetActivity(iBinder);
            return;
        }
        Context orCreateDisplayContext = getOrCreateDisplayContext(i);
        if (orCreateDisplayContext == null) {
            Log.i("SizeCompatMode", "Cannot get context for display " + i);
            return;
        }
        RestartActivityButton createRestartButton = createRestartButton(orCreateDisplayContext);
        createRestartButton.updateLastTargetActivity(iBinder);
        if (createRestartButton.show()) {
            this.mActiveButtons.append(i, createRestartButton);
        } else {
            onDisplayRemoved(i);
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public RestartActivityButton createRestartButton(Context context) {
        RestartActivityButton restartActivityButton = new RestartActivityButton(context, this.mHasShownHint);
        this.mHasShownHint = true;
        return restartActivityButton;
    }

    private Context getOrCreateDisplayContext(int i) {
        Display display;
        if (i == 0) {
            return this.mContext;
        }
        Context context = null;
        WeakReference<Context> weakReference = this.mDisplayContextCache.get(i);
        if (weakReference != null) {
            context = weakReference.get();
        }
        if (context != null || (display = ((DisplayManager) this.mContext.getSystemService(DisplayManager.class)).getDisplay(i)) == null) {
            return context;
        }
        Context createDisplayContext = this.mContext.createDisplayContext(display);
        this.mDisplayContextCache.put(i, new WeakReference<>(createDisplayContext));
        return createDisplayContext;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public static class RestartActivityButton extends ImageButton implements View.OnClickListener, View.OnLongClickListener {
        IBinder mLastActivityToken;
        final int mPopupOffsetX;
        final int mPopupOffsetY;
        final boolean mShouldShowHint;
        PopupWindow mShowingHint;
        final WindowManager.LayoutParams mWinParams;

        private static int getGravity(int i) {
            return (i == 1 ? 8388611 : 8388613) | 80;
        }

        RestartActivityButton(Context context, boolean z) {
            super(context);
            this.mShouldShowHint = !z;
            Drawable drawable = context.getDrawable(C0013R$drawable.btn_restart);
            setImageDrawable(drawable);
            setContentDescription(context.getString(C0021R$string.restart_button_description));
            int intrinsicWidth = drawable.getIntrinsicWidth();
            int intrinsicHeight = drawable.getIntrinsicHeight();
            this.mPopupOffsetX = intrinsicWidth / 2;
            int i = intrinsicHeight * 2;
            this.mPopupOffsetY = i;
            ColorStateList valueOf = ColorStateList.valueOf(-3355444);
            GradientDrawable gradientDrawable = new GradientDrawable();
            gradientDrawable.setShape(1);
            gradientDrawable.setColor(valueOf);
            setBackground(new RippleDrawable(valueOf, null, gradientDrawable));
            setOnClickListener(this);
            setOnLongClickListener(this);
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            this.mWinParams = layoutParams;
            layoutParams.gravity = getGravity(getResources().getConfiguration().getLayoutDirection());
            WindowManager.LayoutParams layoutParams2 = this.mWinParams;
            layoutParams2.width = intrinsicWidth * 2;
            layoutParams2.height = i;
            layoutParams2.type = 2038;
            layoutParams2.flags = 40;
            layoutParams2.format = -3;
            layoutParams2.privateFlags |= 16;
            layoutParams2.setTitle(SizeCompatModeActivityController.class.getSimpleName() + context.getDisplayId());
        }

        /* access modifiers changed from: package-private */
        public void updateLastTargetActivity(IBinder iBinder) {
            this.mLastActivityToken = iBinder;
        }

        /* access modifiers changed from: package-private */
        public boolean show() {
            try {
                ((WindowManager) getContext().getSystemService(WindowManager.class)).addView(this, this.mWinParams);
                return true;
            } catch (WindowManager.InvalidDisplayException e) {
                Log.w("SizeCompatMode", "Cannot show on display " + getContext().getDisplayId(), e);
                return false;
            }
        }

        /* access modifiers changed from: package-private */
        public void remove() {
            PopupWindow popupWindow = this.mShowingHint;
            if (popupWindow != null) {
                popupWindow.dismiss();
            }
            ((WindowManager) getContext().getSystemService(WindowManager.class)).removeViewImmediate(this);
        }

        public void onClick(View view) {
            try {
                ActivityTaskManager.getService().restartActivityProcessIfVisible(this.mLastActivityToken);
            } catch (RemoteException e) {
                Log.w("SizeCompatMode", "Unable to restart activity", e);
            }
        }

        public boolean onLongClick(View view) {
            showHint();
            return true;
        }

        /* access modifiers changed from: protected */
        public void onAttachedToWindow() {
            super.onAttachedToWindow();
            if (this.mShouldShowHint) {
                showHint();
            }
        }

        public void setLayoutDirection(int i) {
            int gravity = getGravity(i);
            WindowManager.LayoutParams layoutParams = this.mWinParams;
            if (layoutParams.gravity != gravity) {
                layoutParams.gravity = gravity;
                PopupWindow popupWindow = this.mShowingHint;
                if (popupWindow != null) {
                    popupWindow.dismiss();
                    showHint();
                }
                ((WindowManager) getContext().getSystemService(WindowManager.class)).updateViewLayout(this, this.mWinParams);
            }
            super.setLayoutDirection(i);
        }

        /* access modifiers changed from: package-private */
        public void showHint() {
            if (this.mShowingHint == null) {
                View inflate = LayoutInflater.from(getContext()).inflate(C0017R$layout.size_compat_mode_hint, (ViewGroup) null);
                PopupWindow popupWindow = new PopupWindow(inflate, -2, -2);
                popupWindow.setWindowLayoutType(this.mWinParams.type);
                popupWindow.setElevation(getResources().getDimension(C0012R$dimen.bubble_elevation));
                popupWindow.setAnimationStyle(16973910);
                popupWindow.setClippingEnabled(false);
                popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    /* class com.android.systemui.$$Lambda$SizeCompatModeActivityController$RestartActivityButton$rxc8GUe9hnz5kAfzl4xmCIiwi3Y */

                    public final void onDismiss() {
                        SizeCompatModeActivityController.RestartActivityButton.this.lambda$showHint$0$SizeCompatModeActivityController$RestartActivityButton();
                    }
                });
                this.mShowingHint = popupWindow;
                Button button = (Button) inflate.findViewById(C0015R$id.got_it);
                button.setBackground(new RippleDrawable(ColorStateList.valueOf(-3355444), null, null));
                button.setOnClickListener(new View.OnClickListener(popupWindow) {
                    /* class com.android.systemui.$$Lambda$SizeCompatModeActivityController$RestartActivityButton$tZJkvUnAETgfbkQvNUGL2mQWd9s */
                    public final /* synthetic */ PopupWindow f$0;

                    {
                        this.f$0 = r1;
                    }

                    public final void onClick(View view) {
                        SizeCompatModeActivityController.RestartActivityButton.lambda$showHint$1(this.f$0, view);
                    }
                });
                popupWindow.showAtLocation(this, this.mWinParams.gravity, this.mPopupOffsetX, this.mPopupOffsetY);
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$showHint$0 */
        public /* synthetic */ void lambda$showHint$0$SizeCompatModeActivityController$RestartActivityButton() {
            this.mShowingHint = null;
        }
    }
}
