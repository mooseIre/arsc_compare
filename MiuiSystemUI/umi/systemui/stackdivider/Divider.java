package com.android.systemui.stackdivider;

import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.RemoteException;
import android.util.Log;
import android.view.IDockedStackListener;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import com.android.systemui.SystemUI;
import com.android.systemui.plugins.R;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.events.RecentsEventBus;
import com.android.systemui.recents.events.activity.DividerExistChangeEvent;
import com.android.systemui.recents.events.activity.DividerMinimizedChangeEvent;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class Divider extends SystemUI {
    /* access modifiers changed from: private */
    public boolean mAdjustedForIme = false;
    private final DividerState mDividerState = new DividerState();
    private DockDividerVisibilityListener mDockDividerVisibilityListener;
    private DockedStackExistsChangedListener mDockedStackExistsChangedListener;
    private boolean mExists;
    /* access modifiers changed from: private */
    public ForcedResizableInfoActivityController mForcedResizableController;
    /* access modifiers changed from: private */
    public boolean mHomeStackResizable = false;
    /* access modifiers changed from: private */
    public boolean mMinimized = false;
    /* access modifiers changed from: private */
    public boolean mNeedUpdate;
    /* access modifiers changed from: private */
    public DividerView mView;
    /* access modifiers changed from: private */
    public boolean mVisible = false;
    private DividerWindowManager mWindowManager;

    public interface DockedStackExistsChangedListener {
        void onDockedStackMinimizedChanged(boolean z);
    }

    public void start() {
        this.mWindowManager = new DividerWindowManager(this.mContext);
        update(this.mContext.getResources().getConfiguration());
        putComponent(Divider.class, this);
        this.mDockDividerVisibilityListener = new DockDividerVisibilityListener();
        Recents.getSystemServices().registerDockedStackListener(this.mDockDividerVisibilityListener);
        this.mForcedResizableController = new ForcedResizableInfoActivityController(this.mContext);
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        if (this.mVisible) {
            update(configuration);
        } else {
            this.mNeedUpdate = true;
        }
    }

    public DividerView getView() {
        return this.mView;
    }

    public boolean isHomeStackResizable() {
        return this.mHomeStackResizable;
    }

    private void addDivider(Configuration configuration) {
        this.mView = (DividerView) LayoutInflater.from(this.mContext).inflate(R.layout.docked_stack_divider, (ViewGroup) null);
        this.mView.injectDependencies(this.mWindowManager, this.mDividerState);
        boolean z = false;
        this.mView.setVisibility(this.mVisible ? 0 : 4);
        this.mView.setMinimizedDockStack(this.mMinimized, this.mHomeStackResizable);
        int dimensionPixelSize = this.mContext.getResources().getDimensionPixelSize(R.dimen.docked_stack_divider_thickness);
        if (configuration.orientation == 2) {
            z = true;
        }
        int i = -1;
        int i2 = z ? dimensionPixelSize : -1;
        if (!z) {
            i = dimensionPixelSize;
        }
        this.mWindowManager.add(this.mView, i2, i);
    }

    private void removeDivider() {
        DividerView dividerView = this.mView;
        if (dividerView != null) {
            dividerView.onDividerRemoved();
        }
        this.mWindowManager.remove();
    }

    /* access modifiers changed from: private */
    public void update(Configuration configuration) {
        removeDivider();
        addDivider(configuration);
        if (this.mMinimized) {
            this.mView.setMinimizedDockStack(true, this.mHomeStackResizable);
            updateTouchable();
        }
        this.mNeedUpdate = false;
    }

    /* access modifiers changed from: private */
    public void updateVisibility(final boolean z) {
        this.mView.post(new Runnable() {
            public void run() {
                boolean access$000 = Divider.this.mVisible;
                boolean z = z;
                if (access$000 != z) {
                    boolean unused = Divider.this.mVisible = z;
                    if (Divider.this.mVisible && Divider.this.mNeedUpdate) {
                        Divider divider = Divider.this;
                        divider.update(divider.mContext.getResources().getConfiguration());
                    }
                    Divider.this.mView.setVisibility(z ? 0 : 4);
                    Divider.this.mView.setMinimizedDockStack(Divider.this.mMinimized, Divider.this.mHomeStackResizable);
                }
            }
        });
    }

    /* access modifiers changed from: private */
    public void updateMinimizedDockedStack(boolean z, long j, boolean z2) {
        final boolean z3 = z;
        final boolean z4 = z2;
        final long j2 = j;
        this.mView.post(new Runnable() {
            public void run() {
                if (Divider.this.mMinimized != z3) {
                    boolean unused = Divider.this.mHomeStackResizable = z4;
                    boolean unused2 = Divider.this.mMinimized = z3;
                    Divider.this.updateTouchable();
                    if (j2 > 0) {
                        Divider.this.mView.setMinimizedDockStack(z3, j2, z4);
                    } else {
                        Divider.this.mView.setMinimizedDockStack(z3, z4);
                    }
                    RecentsEventBus.getDefault().send(new DividerMinimizedChangeEvent(z3));
                }
            }
        });
        DockedStackExistsChangedListener dockedStackExistsChangedListener = this.mDockedStackExistsChangedListener;
        if (dockedStackExistsChangedListener != null) {
            dockedStackExistsChangedListener.onDockedStackMinimizedChanged(z);
        }
    }

    public void registerDockedStackExistsChangedListener(DockedStackExistsChangedListener dockedStackExistsChangedListener) {
        this.mDockedStackExistsChangedListener = dockedStackExistsChangedListener;
    }

    /* access modifiers changed from: private */
    public void notifyDockedStackExistsChanged(final boolean z) {
        this.mExists = z;
        this.mView.post(new Runnable() {
            public void run() {
                if (Divider.this.mForcedResizableController != null) {
                    Divider.this.mForcedResizableController.notifyDockedStackExistsChanged(z);
                }
                RecentsEventBus.getDefault().send(new DividerExistChangeEvent(z));
            }
        });
    }

    /* access modifiers changed from: private */
    public void updateTouchable() {
        this.mWindowManager.setTouchable((this.mHomeStackResizable || !this.mMinimized) && !this.mAdjustedForIme);
    }

    public void onRecentsActivityStarting() {
        DividerView dividerView = this.mView;
        if (dividerView != null) {
            dividerView.onRecentsActivityStarting();
        }
    }

    public void onDockedTopTask(int i, Rect rect) {
        DividerView dividerView = this.mView;
        if (dividerView != null) {
            dividerView.onDockedTopTask(i, rect);
        }
    }

    public void onRecentsDrawn() {
        DividerView dividerView = this.mView;
        if (dividerView != null) {
            dividerView.onRecentsDrawn();
        }
    }

    public void onUndockingTask(boolean z) {
        DividerView dividerView = this.mView;
        if (dividerView != null) {
            dividerView.onUndockingTask(z);
        }
    }

    public void onDockedFirstAnimationFrame() {
        DividerView dividerView = this.mView;
        if (dividerView != null) {
            dividerView.onDockedFirstAnimationFrame();
        }
    }

    public void onMultiWindowStateChanged(boolean z) {
        DividerView dividerView = this.mView;
        if (dividerView != null) {
            dividerView.onMultiWindowStateChanged(z);
        }
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.print("  mVisible=");
        printWriter.println(this.mVisible);
        printWriter.print("  mMinimized=");
        printWriter.println(this.mMinimized);
        printWriter.print("  mAdjustedForIme=");
        printWriter.println(this.mAdjustedForIme);
    }

    class DockDividerVisibilityListener extends IDockedStackListener.Stub {
        DockDividerVisibilityListener() {
        }

        public void onDividerVisibilityChanged(boolean z) throws RemoteException {
            Log.d("Divider", "onDividerVisibilityChanged visible=" + z);
            Divider.this.updateVisibility(z);
        }

        public void onDockedStackExistsChanged(boolean z) throws RemoteException {
            Log.d("Divider", "onDockedStackExistsChanged exists=" + z);
            Divider.this.notifyDockedStackExistsChanged(z);
        }

        public void onDockedStackMinimizedChanged(boolean z, long j, boolean z2) throws RemoteException {
            Log.d("Divider", "onDockedStackMinimizedChanged minimized=" + z + " animDuration=" + j);
            boolean unused = Divider.this.mHomeStackResizable = z2;
            Divider.this.updateMinimizedDockedStack(z, j, z2);
        }

        public void onAdjustedForImeChanged(final boolean z, final long j) throws RemoteException {
            Log.d("Divider", "onAdjustedForImeChanged adjustedForIme=" + z + " animDuration=" + j);
            Divider.this.mView.post(new Runnable() {
                public void run() {
                    boolean access$1100 = Divider.this.mAdjustedForIme;
                    boolean z = z;
                    if (access$1100 != z) {
                        boolean unused = Divider.this.mAdjustedForIme = z;
                        Divider.this.updateTouchable();
                        if (Divider.this.mMinimized) {
                            return;
                        }
                        if (j > 0) {
                            Divider.this.mView.setAdjustedForIme(z, j);
                        } else {
                            Divider.this.mView.setAdjustedForIme(z);
                        }
                    }
                }
            });
        }

        public void onDockSideChanged(final int i) throws RemoteException {
            Log.d("Divider", "onDockSideChanged newDockSide=" + i);
            Divider.this.mView.post(new Runnable() {
                public void run() {
                    Divider.this.mView.notifyDockSideChanged(i);
                }
            });
        }
    }

    public boolean isMinimized() {
        return this.mMinimized;
    }

    public boolean isExists() {
        return this.mExists;
    }
}
