package com.android.systemui.wm;

import android.content.Context;
import android.content.res.Configuration;
import android.hardware.display.DisplayManager;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Slog;
import android.util.SparseArray;
import android.view.Display;
import android.view.IDisplayWindowListener;
import android.view.IWindowManager;
import com.android.systemui.wm.DisplayChangeController;
import com.android.systemui.wm.DisplayController;
import java.util.ArrayList;

public class DisplayController {
    private final DisplayChangeController mChangeController;
    private final Context mContext;
    private final ArrayList<OnDisplaysChangedListener> mDisplayChangedListeners = new ArrayList<>();
    private final IDisplayWindowListener mDisplayContainerListener = new IDisplayWindowListener.Stub() {
        /* class com.android.systemui.wm.DisplayController.AnonymousClass1 */

        public void onDisplayAdded(int i) {
            DisplayController.this.mHandler.post(new Runnable(i) {
                /* class com.android.systemui.wm.$$Lambda$DisplayController$1$zJ2mVywyLG45RsLGtw9ST7xxypY */
                public final /* synthetic */ int f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    DisplayController.AnonymousClass1.this.lambda$onDisplayAdded$0$DisplayController$1(this.f$1);
                }
            });
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onDisplayAdded$0 */
        public /* synthetic */ void lambda$onDisplayAdded$0$DisplayController$1(int i) {
            Context context;
            synchronized (DisplayController.this.mDisplays) {
                if (DisplayController.this.mDisplays.get(i) == null) {
                    Display display = DisplayController.this.getDisplay(i);
                    if (display != null) {
                        DisplayRecord displayRecord = new DisplayRecord();
                        if (i == 0) {
                            context = DisplayController.this.mContext;
                        } else {
                            context = DisplayController.this.mContext.createDisplayContext(display);
                        }
                        displayRecord.mContext = context;
                        displayRecord.mDisplayLayout = new DisplayLayout(context, display);
                        DisplayController.this.mDisplays.put(i, displayRecord);
                        for (int i2 = 0; i2 < DisplayController.this.mDisplayChangedListeners.size(); i2++) {
                            ((OnDisplaysChangedListener) DisplayController.this.mDisplayChangedListeners.get(i2)).onDisplayAdded(i);
                        }
                    }
                }
            }
        }

        public void onDisplayConfigurationChanged(int i, Configuration configuration) {
            DisplayController.this.mHandler.post(new Runnable(i, configuration) {
                /* class com.android.systemui.wm.$$Lambda$DisplayController$1$mO2SyOpDmJKrsjv09X0fk_FOg */
                public final /* synthetic */ int f$1;
                public final /* synthetic */ Configuration f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                public final void run() {
                    DisplayController.AnonymousClass1.this.lambda$onDisplayConfigurationChanged$1$DisplayController$1(this.f$1, this.f$2);
                }
            });
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onDisplayConfigurationChanged$1 */
        public /* synthetic */ void lambda$onDisplayConfigurationChanged$1$DisplayController$1(int i, Configuration configuration) {
            synchronized (DisplayController.this.mDisplays) {
                DisplayRecord displayRecord = (DisplayRecord) DisplayController.this.mDisplays.get(i);
                if (displayRecord == null) {
                    Slog.w("DisplayController", "Skipping Display Configuration change on non-added display.");
                    return;
                }
                Display display = DisplayController.this.getDisplay(i);
                if (display == null) {
                    Slog.w("DisplayController", "Skipping Display Configuration change on invalid display. It may have been removed.");
                    return;
                }
                Context context = DisplayController.this.mContext;
                if (i != 0) {
                    context = DisplayController.this.mContext.createDisplayContext(display);
                }
                Context createConfigurationContext = context.createConfigurationContext(configuration);
                displayRecord.mContext = createConfigurationContext;
                displayRecord.mDisplayLayout = new DisplayLayout(createConfigurationContext, display);
                for (int i2 = 0; i2 < DisplayController.this.mDisplayChangedListeners.size(); i2++) {
                    ((OnDisplaysChangedListener) DisplayController.this.mDisplayChangedListeners.get(i2)).onDisplayConfigurationChanged(i, configuration);
                }
            }
        }

        public void onDisplayRemoved(int i) {
            DisplayController.this.mHandler.post(new Runnable(i) {
                /* class com.android.systemui.wm.$$Lambda$DisplayController$1$sHTeIz3WbujoajhpVNRgzuLoi74 */
                public final /* synthetic */ int f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    DisplayController.AnonymousClass1.this.lambda$onDisplayRemoved$2$DisplayController$1(this.f$1);
                }
            });
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onDisplayRemoved$2 */
        public /* synthetic */ void lambda$onDisplayRemoved$2$DisplayController$1(int i) {
            synchronized (DisplayController.this.mDisplays) {
                if (DisplayController.this.mDisplays.get(i) != null) {
                    for (int size = DisplayController.this.mDisplayChangedListeners.size() - 1; size >= 0; size--) {
                        ((OnDisplaysChangedListener) DisplayController.this.mDisplayChangedListeners.get(size)).onDisplayRemoved(i);
                    }
                    DisplayController.this.mDisplays.remove(i);
                }
            }
        }

        public void onFixedRotationStarted(int i, int i2) {
            DisplayController.this.mHandler.post(new Runnable(i, i2) {
                /* class com.android.systemui.wm.$$Lambda$DisplayController$1$l7FiEOWmAq5RJbL_Wn1mlPPItcA */
                public final /* synthetic */ int f$1;
                public final /* synthetic */ int f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                public final void run() {
                    DisplayController.AnonymousClass1.this.lambda$onFixedRotationStarted$3$DisplayController$1(this.f$1, this.f$2);
                }
            });
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onFixedRotationStarted$3 */
        public /* synthetic */ void lambda$onFixedRotationStarted$3$DisplayController$1(int i, int i2) {
            synchronized (DisplayController.this.mDisplays) {
                if (DisplayController.this.mDisplays.get(i) != null) {
                    if (DisplayController.this.getDisplay(i) != null) {
                        for (int size = DisplayController.this.mDisplayChangedListeners.size() - 1; size >= 0; size--) {
                            ((OnDisplaysChangedListener) DisplayController.this.mDisplayChangedListeners.get(size)).onFixedRotationStarted(i, i2);
                        }
                        return;
                    }
                }
                Slog.w("DisplayController", "Skipping onFixedRotationStarted on unknown display, displayId=" + i);
            }
        }

        public void onFixedRotationFinished(int i) {
            DisplayController.this.mHandler.post(new Runnable(i) {
                /* class com.android.systemui.wm.$$Lambda$DisplayController$1$ZPKsrnPJwHyuDSJzU6cSEJs0 */
                public final /* synthetic */ int f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    DisplayController.AnonymousClass1.this.lambda$onFixedRotationFinished$4$DisplayController$1(this.f$1);
                }
            });
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onFixedRotationFinished$4 */
        public /* synthetic */ void lambda$onFixedRotationFinished$4$DisplayController$1(int i) {
            synchronized (DisplayController.this.mDisplays) {
                if (DisplayController.this.mDisplays.get(i) != null) {
                    if (DisplayController.this.getDisplay(i) != null) {
                        for (int size = DisplayController.this.mDisplayChangedListeners.size() - 1; size >= 0; size--) {
                            ((OnDisplaysChangedListener) DisplayController.this.mDisplayChangedListeners.get(size)).onFixedRotationFinished(i);
                        }
                        return;
                    }
                }
                Slog.w("DisplayController", "Skipping onFixedRotationFinished on unknown display, displayId=" + i);
            }
        }
    };
    private final SparseArray<DisplayRecord> mDisplays = new SparseArray<>();
    private final Handler mHandler;
    private final IWindowManager mWmService;

    public interface OnDisplaysChangedListener {
        default void onDisplayAdded(int i) {
        }

        default void onDisplayConfigurationChanged(int i, Configuration configuration) {
        }

        default void onDisplayRemoved(int i) {
        }

        default void onFixedRotationFinished(int i) {
        }

        default void onFixedRotationStarted(int i, int i2) {
        }
    }

    public Display getDisplay(int i) {
        return ((DisplayManager) this.mContext.getSystemService(DisplayManager.class)).getDisplay(i);
    }

    public DisplayController(Context context, Handler handler, IWindowManager iWindowManager) {
        this.mHandler = handler;
        this.mContext = context;
        this.mWmService = iWindowManager;
        this.mChangeController = new DisplayChangeController(handler, iWindowManager);
        try {
            this.mWmService.registerDisplayWindowListener(this.mDisplayContainerListener);
        } catch (RemoteException unused) {
            throw new RuntimeException("Unable to register hierarchy listener");
        }
    }

    public DisplayLayout getDisplayLayout(int i) {
        DisplayRecord displayRecord = this.mDisplays.get(i);
        if (displayRecord != null) {
            return displayRecord.mDisplayLayout;
        }
        return null;
    }

    public Context getDisplayContext(int i) {
        DisplayRecord displayRecord = this.mDisplays.get(i);
        if (displayRecord != null) {
            return displayRecord.mContext;
        }
        return null;
    }

    public void addDisplayWindowListener(OnDisplaysChangedListener onDisplaysChangedListener) {
        synchronized (this.mDisplays) {
            if (!this.mDisplayChangedListeners.contains(onDisplaysChangedListener)) {
                this.mDisplayChangedListeners.add(onDisplaysChangedListener);
                for (int i = 0; i < this.mDisplays.size(); i++) {
                    onDisplaysChangedListener.onDisplayAdded(this.mDisplays.keyAt(i));
                }
            }
        }
    }

    public void addDisplayChangingController(DisplayChangeController.OnDisplayChangingListener onDisplayChangingListener) {
        this.mChangeController.addRotationListener(onDisplayChangingListener);
    }

    /* access modifiers changed from: private */
    public static class DisplayRecord {
        Context mContext;
        DisplayLayout mDisplayLayout;

        private DisplayRecord() {
        }
    }
}
