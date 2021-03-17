package com.android.keyguard;

import android.app.Presentation;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.media.MediaRouter;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.Display;
import android.view.DisplayInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.C0008R$array;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0017R$layout;
import com.android.systemui.C0022R$style;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.NavigationBarController;
import com.android.systemui.statusbar.phone.NavigationBarView;
import com.android.systemui.util.InjectionInflationController;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class KeyguardDisplayManager {
    private static boolean DEBUG = true;
    private final Context mContext;
    List<Integer> mDeviceHidePresentationIds;
    private final DisplayManager.DisplayListener mDisplayListener = new DisplayManager.DisplayListener() {
        /* class com.android.keyguard.KeyguardDisplayManager.AnonymousClass1 */

        public void onDisplayAdded(int i) {
            Display display = KeyguardDisplayManager.this.mDisplayService.getDisplay(i);
            if (KeyguardDisplayManager.this.mShowing) {
                KeyguardDisplayManager.this.updateNavigationBarVisibility(i, false);
                KeyguardDisplayManager.this.showPresentation(display);
            }
        }

        public void onDisplayChanged(int i) {
            if (i != 0 && ((Presentation) KeyguardDisplayManager.this.mPresentations.get(i)) != null && KeyguardDisplayManager.this.mShowing) {
                KeyguardDisplayManager.this.hidePresentation(i);
                Display display = KeyguardDisplayManager.this.mDisplayService.getDisplay(i);
                if (display != null) {
                    KeyguardDisplayManager.this.showPresentation(display);
                }
            }
        }

        public void onDisplayRemoved(int i) {
            KeyguardDisplayManager.this.hidePresentation(i);
        }
    };
    private final DisplayManager mDisplayService;
    private final InjectionInflationController mInjectableInflater;
    private final MediaRouter mMediaRouter;
    private final MediaRouter.SimpleCallback mMediaRouterCallback = new MediaRouter.SimpleCallback() {
        /* class com.android.keyguard.KeyguardDisplayManager.AnonymousClass2 */

        public void onRouteSelected(MediaRouter mediaRouter, int i, MediaRouter.RouteInfo routeInfo) {
            if (KeyguardDisplayManager.DEBUG) {
                Log.d("KeyguardDisplayManager", "onRouteSelected: type=" + i + ", info=" + routeInfo);
            }
            KeyguardDisplayManager keyguardDisplayManager = KeyguardDisplayManager.this;
            keyguardDisplayManager.updateDisplays(keyguardDisplayManager.mShowing);
        }

        public void onRouteUnselected(MediaRouter mediaRouter, int i, MediaRouter.RouteInfo routeInfo) {
            if (KeyguardDisplayManager.DEBUG) {
                Log.d("KeyguardDisplayManager", "onRouteUnselected: type=" + i + ", info=" + routeInfo);
            }
            KeyguardDisplayManager keyguardDisplayManager = KeyguardDisplayManager.this;
            keyguardDisplayManager.updateDisplays(keyguardDisplayManager.mShowing);
        }

        public void onRoutePresentationDisplayChanged(MediaRouter mediaRouter, MediaRouter.RouteInfo routeInfo) {
            if (KeyguardDisplayManager.DEBUG) {
                Log.d("KeyguardDisplayManager", "onRoutePresentationDisplayChanged: info=" + routeInfo);
            }
            KeyguardDisplayManager keyguardDisplayManager = KeyguardDisplayManager.this;
            keyguardDisplayManager.updateDisplays(keyguardDisplayManager.mShowing);
        }
    };
    private final NavigationBarController mNavBarController = ((NavigationBarController) Dependency.get(NavigationBarController.class));
    private final SparseArray<Presentation> mPresentations = new SparseArray<>();
    private boolean mShowing;
    private final DisplayInfo mTmpDisplayInfo = new DisplayInfo();

    public KeyguardDisplayManager(Context context, InjectionInflationController injectionInflationController) {
        this.mContext = context;
        this.mInjectableInflater = injectionInflationController;
        this.mMediaRouter = (MediaRouter) context.getSystemService(MediaRouter.class);
        DisplayManager displayManager = (DisplayManager) this.mContext.getSystemService(DisplayManager.class);
        this.mDisplayService = displayManager;
        displayManager.registerDisplayListener(this.mDisplayListener, null);
        this.mDeviceHidePresentationIds = (List) Arrays.stream(this.mContext.getResources().getIntArray(C0008R$array.miui_config_hideKeyguardPresentationDisplayIds)).boxed().collect(Collectors.toList());
    }

    private boolean isKeyguardShowable(Display display) {
        if (display == null) {
            if (DEBUG) {
                Log.i("KeyguardDisplayManager", "Cannot show Keyguard on null display");
            }
            return false;
        } else if (display.getDisplayId() == 0) {
            if (DEBUG) {
                Log.i("KeyguardDisplayManager", "Do not show KeyguardPresentation on the default display");
            }
            return false;
        } else {
            display.getDisplayInfo(this.mTmpDisplayInfo);
            if ((this.mTmpDisplayInfo.flags & 4) != 0) {
                if (DEBUG) {
                    Log.i("KeyguardDisplayManager", "Do not show KeyguardPresentation on a private display");
                }
                return false;
            } else if (!this.mDeviceHidePresentationIds.contains(Integer.valueOf(display.getDisplayId()))) {
                return true;
            } else {
                if (DEBUG) {
                    Log.i("KeyguardDisplayManager", "Do not show KeyguardPresentation on a specific display");
                }
                return false;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean showPresentation(Display display) {
        if (!isKeyguardShowable(display)) {
            return false;
        }
        if (DEBUG) {
            Log.i("KeyguardDisplayManager", "Keyguard enabled on display: " + display);
        }
        int displayId = display.getDisplayId();
        if (this.mPresentations.get(displayId) == null) {
            Context context = this.mContext;
            KeyguardPresentation keyguardPresentation = new KeyguardPresentation(context, display, this.mInjectableInflater.injectable(LayoutInflater.from(context)));
            keyguardPresentation.setOnDismissListener(new DialogInterface.OnDismissListener(keyguardPresentation, displayId) {
                /* class com.android.keyguard.$$Lambda$KeyguardDisplayManager$WcC7zwdycYHh9dpCnEiRCOObKEQ */
                public final /* synthetic */ Presentation f$1;
                public final /* synthetic */ int f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                public final void onDismiss(DialogInterface dialogInterface) {
                    KeyguardDisplayManager.this.lambda$showPresentation$0$KeyguardDisplayManager(this.f$1, this.f$2, dialogInterface);
                }
            });
            try {
                keyguardPresentation.show();
            } catch (WindowManager.InvalidDisplayException e) {
                Log.w("KeyguardDisplayManager", "Invalid display:", e);
                keyguardPresentation = null;
            }
            if (keyguardPresentation != null) {
                this.mPresentations.append(displayId, keyguardPresentation);
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$showPresentation$0 */
    public /* synthetic */ void lambda$showPresentation$0$KeyguardDisplayManager(Presentation presentation, int i, DialogInterface dialogInterface) {
        if (presentation.equals(this.mPresentations.get(i))) {
            this.mPresentations.remove(i);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void hidePresentation(int i) {
        Presentation presentation = this.mPresentations.get(i);
        if (presentation != null) {
            presentation.dismiss();
            this.mPresentations.remove(i);
        }
    }

    public void show() {
        if (!this.mShowing) {
            if (DEBUG) {
                Log.v("KeyguardDisplayManager", "show");
            }
            this.mMediaRouter.addCallback(4, this.mMediaRouterCallback, 8);
            updateDisplays(true);
        }
        this.mShowing = true;
    }

    public void hide() {
        if (this.mShowing) {
            if (DEBUG) {
                Log.v("KeyguardDisplayManager", "hide");
            }
            this.mMediaRouter.removeCallback(this.mMediaRouterCallback);
            updateDisplays(false);
        }
        this.mShowing = false;
    }

    /* access modifiers changed from: protected */
    public boolean updateDisplays(boolean z) {
        boolean z2 = false;
        if (z) {
            Display[] displays = this.mDisplayService.getDisplays();
            boolean z3 = false;
            for (Display display : displays) {
                updateNavigationBarVisibility(display.getDisplayId(), false);
                z3 |= showPresentation(display);
            }
            return z3;
        }
        if (this.mPresentations.size() > 0) {
            z2 = true;
        }
        for (int size = this.mPresentations.size() - 1; size >= 0; size--) {
            updateNavigationBarVisibility(this.mPresentations.keyAt(size), true);
            this.mPresentations.valueAt(size).dismiss();
        }
        this.mPresentations.clear();
        return z2;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateNavigationBarVisibility(int i, boolean z) {
        NavigationBarView navigationBarView;
        if (i != 0 && (navigationBarView = this.mNavBarController.getNavigationBarView(i)) != null) {
            if (z) {
                navigationBarView.getRootView().setVisibility(0);
            } else {
                navigationBarView.getRootView().setVisibility(8);
            }
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public static final class KeyguardPresentation extends Presentation {
        private View mClock;
        private final LayoutInflater mInjectableLayoutInflater;
        private int mMarginLeft;
        private int mMarginTop;
        Runnable mMoveTextRunnable = new Runnable() {
            /* class com.android.keyguard.KeyguardDisplayManager.KeyguardPresentation.AnonymousClass1 */

            public void run() {
                int random = KeyguardPresentation.this.mMarginLeft + ((int) (Math.random() * ((double) (KeyguardPresentation.this.mUsableWidth - KeyguardPresentation.this.mClock.getWidth()))));
                int random2 = KeyguardPresentation.this.mMarginTop + ((int) (Math.random() * ((double) (KeyguardPresentation.this.mUsableHeight - KeyguardPresentation.this.mClock.getHeight()))));
                KeyguardPresentation.this.mClock.setTranslationX((float) random);
                KeyguardPresentation.this.mClock.setTranslationY((float) random2);
                KeyguardPresentation.this.mClock.postDelayed(KeyguardPresentation.this.mMoveTextRunnable, 30000);
            }
        };
        private int mUsableHeight;
        private int mUsableWidth;

        public void cancel() {
        }

        KeyguardPresentation(Context context, Display display, LayoutInflater layoutInflater) {
            super(context, display, C0022R$style.Theme_SystemUI_KeyguardPresentation);
            this.mInjectableLayoutInflater = layoutInflater;
            getWindow().setType(2009);
            setCancelable(false);
        }

        public void onDetachedFromWindow() {
            this.mClock.removeCallbacks(this.mMoveTextRunnable);
        }

        /* access modifiers changed from: protected */
        public void onCreate(Bundle bundle) {
            super.onCreate(bundle);
            Point point = new Point();
            getDisplay().getSize(point);
            int i = point.x;
            this.mUsableWidth = (i * 80) / 100;
            int i2 = point.y;
            this.mUsableHeight = (i2 * 80) / 100;
            this.mMarginLeft = (i * 20) / 200;
            this.mMarginTop = (i2 * 20) / 200;
            setContentView(this.mInjectableLayoutInflater.inflate(C0017R$layout.keyguard_presentation, (ViewGroup) null));
            getWindow().getDecorView().setSystemUiVisibility(1792);
            getWindow().getAttributes().setFitInsetsTypes(0);
            getWindow().setNavigationBarContrastEnforced(false);
            getWindow().setNavigationBarColor(0);
            View findViewById = findViewById(C0015R$id.clock);
            this.mClock = findViewById;
            findViewById.post(this.mMoveTextRunnable);
        }
    }
}
