package com.android.keyguard;

import android.app.Presentation;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.media.MediaRouter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import android.view.Display;
import android.view.DisplayInfo;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManagerCompat;
import com.android.systemui.plugins.R;

public class KeyguardDisplayManager {
    /* access modifiers changed from: private */
    public static boolean DEBUG = true;
    private Context mContext;
    private final DisplayManager.DisplayListener mDisplayListener = new DisplayManager.DisplayListener() {
        public void onDisplayAdded(int i) {
            Display display = KeyguardDisplayManager.this.mDisplayService.getDisplay(i);
            if (KeyguardDisplayManager.this.mShowing) {
                boolean unused = KeyguardDisplayManager.this.showPresentation(display);
            }
        }

        public void onDisplayChanged(int i) {
            Display display;
            Presentation presentation;
            if (i != 0 && (display = KeyguardDisplayManager.this.mDisplayService.getDisplay(i)) != null && KeyguardDisplayManager.this.mShowing && (presentation = (Presentation) KeyguardDisplayManager.this.mPresentations.get(i)) != null && !presentation.getDisplay().equals(display)) {
                KeyguardDisplayManager.this.hidePresentation(i);
                boolean unused = KeyguardDisplayManager.this.showPresentation(display);
            }
        }

        public void onDisplayRemoved(int i) {
            KeyguardDisplayManager.this.hidePresentation(i);
        }
    };
    /* access modifiers changed from: private */
    public final DisplayManager mDisplayService;
    private MediaRouter mMediaRouter;
    private final MediaRouter.SimpleCallback mMediaRouterCallback = new MediaRouter.SimpleCallback() {
        public void onRouteSelected(MediaRouter mediaRouter, int i, MediaRouter.RouteInfo routeInfo) {
            if (KeyguardDisplayManager.DEBUG) {
                Slog.d("KeyguardDisplayManager", "onRouteSelected: type=" + i + ", info=" + routeInfo);
            }
            KeyguardDisplayManager keyguardDisplayManager = KeyguardDisplayManager.this;
            keyguardDisplayManager.updateDisplays(keyguardDisplayManager.mShowing);
        }

        public void onRouteUnselected(MediaRouter mediaRouter, int i, MediaRouter.RouteInfo routeInfo) {
            if (KeyguardDisplayManager.DEBUG) {
                Slog.d("KeyguardDisplayManager", "onRouteUnselected: type=" + i + ", info=" + routeInfo);
            }
            KeyguardDisplayManager keyguardDisplayManager = KeyguardDisplayManager.this;
            keyguardDisplayManager.updateDisplays(keyguardDisplayManager.mShowing);
        }

        public void onRoutePresentationDisplayChanged(MediaRouter mediaRouter, MediaRouter.RouteInfo routeInfo) {
            if (KeyguardDisplayManager.DEBUG) {
                Slog.d("KeyguardDisplayManager", "onRoutePresentationDisplayChanged: info=" + routeInfo);
            }
            KeyguardDisplayManager keyguardDisplayManager = KeyguardDisplayManager.this;
            keyguardDisplayManager.updateDisplays(keyguardDisplayManager.mShowing);
        }
    };
    /* access modifiers changed from: private */
    public final SparseArray<Presentation> mPresentations = new SparseArray<>();
    /* access modifiers changed from: private */
    public boolean mShowing;
    private final DisplayInfo mTmpDisplayInfo = new DisplayInfo();

    public KeyguardDisplayManager(Context context) {
        this.mContext = context;
        this.mMediaRouter = (MediaRouter) context.getSystemService("media_router");
        DisplayManager displayManager = (DisplayManager) this.mContext.getSystemService(DisplayManager.class);
        this.mDisplayService = displayManager;
        displayManager.registerDisplayListener(this.mDisplayListener, (Handler) null);
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
            if ((this.mTmpDisplayInfo.flags & 4) == 0) {
                return true;
            }
            if (DEBUG) {
                Log.i("KeyguardDisplayManager", "Do not show KeyguardPresentation on a private display");
            }
            return false;
        }
    }

    /* access modifiers changed from: private */
    public boolean showPresentation(Display display) {
        if (!isKeyguardShowable(display)) {
            return false;
        }
        if (DEBUG) {
            Log.i("KeyguardDisplayManager", "Keyguard enabled on display: " + display);
        }
        int displayId = display.getDisplayId();
        if (this.mPresentations.get(displayId) == null) {
            KeyguardPresentation keyguardPresentation = new KeyguardPresentation(this.mContext, display, R.style.keyguard_presentation_theme);
            keyguardPresentation.setOnDismissListener(new DialogInterface.OnDismissListener(displayId) {
                public final /* synthetic */ int f$1;

                {
                    this.f$1 = r2;
                }

                public final void onDismiss(DialogInterface dialogInterface) {
                    KeyguardDisplayManager.this.lambda$showPresentation$0$KeyguardDisplayManager(this.f$1, dialogInterface);
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
    public /* synthetic */ void lambda$showPresentation$0$KeyguardDisplayManager(int i, DialogInterface dialogInterface) {
        if (this.mPresentations.get(i) != null) {
            this.mPresentations.remove(i);
        }
    }

    /* access modifiers changed from: private */
    public void hidePresentation(int i) {
        Presentation presentation = this.mPresentations.get(i);
        if (presentation != null) {
            presentation.dismiss();
            this.mPresentations.remove(i);
        }
    }

    public void show() {
        if (!this.mShowing) {
            if (DEBUG) {
                Slog.v("KeyguardDisplayManager", "show");
            }
            this.mMediaRouter.addCallback(4, this.mMediaRouterCallback, 8);
            updateDisplays(true);
        }
        this.mShowing = true;
    }

    public void hide() {
        if (this.mShowing) {
            if (DEBUG) {
                Slog.v("KeyguardDisplayManager", "hide");
            }
            this.mMediaRouter.removeCallback(this.mMediaRouterCallback);
            updateDisplays(false);
        }
        this.mShowing = false;
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v0, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v1, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v0, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v2, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v3, resolved type: int} */
    /* access modifiers changed from: protected */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean updateDisplays(boolean r5) {
        /*
            r4 = this;
            r0 = 0
            if (r5 == 0) goto L_0x0017
            android.hardware.display.DisplayManager r5 = r4.mDisplayService
            android.view.Display[] r5 = r5.getDisplays()
            int r1 = r5.length
            r2 = r0
        L_0x000b:
            if (r0 >= r1) goto L_0x003e
            r3 = r5[r0]
            boolean r3 = r4.showPresentation(r3)
            r2 = r2 | r3
            int r0 = r0 + 1
            goto L_0x000b
        L_0x0017:
            android.util.SparseArray<android.app.Presentation> r5 = r4.mPresentations
            int r5 = r5.size()
            r1 = 1
            if (r5 <= 0) goto L_0x0021
            r0 = r1
        L_0x0021:
            android.util.SparseArray<android.app.Presentation> r5 = r4.mPresentations
            int r5 = r5.size()
            int r5 = r5 - r1
        L_0x0028:
            if (r5 < 0) goto L_0x0038
            android.util.SparseArray<android.app.Presentation> r1 = r4.mPresentations
            java.lang.Object r1 = r1.valueAt(r5)
            android.app.Presentation r1 = (android.app.Presentation) r1
            r1.dismiss()
            int r5 = r5 + -1
            goto L_0x0028
        L_0x0038:
            android.util.SparseArray<android.app.Presentation> r4 = r4.mPresentations
            r4.clear()
            r2 = r0
        L_0x003e:
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.keyguard.KeyguardDisplayManager.updateDisplays(boolean):boolean");
    }

    private static final class KeyguardPresentation extends Presentation {
        /* access modifiers changed from: private */
        public View mClock;
        /* access modifiers changed from: private */
        public int mMarginLeft;
        /* access modifiers changed from: private */
        public int mMarginTop;
        Runnable mMoveTextRunnable = new Runnable() {
            public void run() {
                int access$600 = KeyguardPresentation.this.mMarginLeft + ((int) (Math.random() * ((double) (KeyguardPresentation.this.mUsableWidth - KeyguardPresentation.this.mClock.getWidth()))));
                int access$900 = KeyguardPresentation.this.mMarginTop + ((int) (Math.random() * ((double) (KeyguardPresentation.this.mUsableHeight - KeyguardPresentation.this.mClock.getHeight()))));
                KeyguardPresentation.this.mClock.setTranslationX((float) access$600);
                KeyguardPresentation.this.mClock.setTranslationY((float) access$900);
                KeyguardPresentation.this.mClock.postDelayed(KeyguardPresentation.this.mMoveTextRunnable, 10000);
            }
        };
        /* access modifiers changed from: private */
        public int mUsableHeight;
        /* access modifiers changed from: private */
        public int mUsableWidth;

        public KeyguardPresentation(Context context, Display display, int i) {
            super(context, display, i);
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
            setContentView(R.layout.keyguard_presentation);
            getWindow().getDecorView().setSystemUiVisibility(1792);
            View findViewById = findViewById(R.id.clock);
            this.mClock = findViewById;
            findViewById.post(this.mMoveTextRunnable);
            WindowManagerCompat.makeWindowFullScreen(getWindow());
        }
    }
}
