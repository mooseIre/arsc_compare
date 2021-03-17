package com.android.systemui.screenshot;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.UserManager;
import android.util.Log;
import com.android.internal.logging.UiEventLogger;
import com.android.internal.util.ScreenshotHelper;
import com.android.systemui.screenshot.FallbackTakeScreenshotService;
import com.android.systemui.shared.recents.utilities.BitmapUtil;
import java.util.function.Consumer;

public class FallbackTakeScreenshotService extends Service {
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        /* class com.android.systemui.screenshot.FallbackTakeScreenshotService.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.CLOSE_SYSTEM_DIALOGS".equals(intent.getAction()) && FallbackTakeScreenshotService.this.mScreenshot != null) {
                FallbackTakeScreenshotService.this.mScreenshot.dismissScreenshot("close system dialogs", true);
            }
        }
    };
    private Handler mHandler = new Handler(Looper.myLooper()) {
        /* class com.android.systemui.screenshot.FallbackTakeScreenshotService.AnonymousClass2 */

        public void handleMessage(Message message) {
            Messenger messenger = message.replyTo;
            $$Lambda$FallbackTakeScreenshotService$2$BCZP2BsU_7az9K_C7RZADOY3iZo r8 = new Consumer(messenger) {
                /* class com.android.systemui.screenshot.$$Lambda$FallbackTakeScreenshotService$2$BCZP2BsU_7az9K_C7RZADOY3iZo */
                public final /* synthetic */ Messenger f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    FallbackTakeScreenshotService.AnonymousClass2.lambda$handleMessage$0(this.f$0, (Uri) obj);
                }
            };
            $$Lambda$FallbackTakeScreenshotService$2$PePM3eE2JSWT2tguvy8VbhRI0Tc r9 = new Runnable(messenger) {
                /* class com.android.systemui.screenshot.$$Lambda$FallbackTakeScreenshotService$2$PePM3eE2JSWT2tguvy8VbhRI0Tc */
                public final /* synthetic */ Messenger f$0;

                {
                    this.f$0 = r1;
                }

                public final void run() {
                    FallbackTakeScreenshotService.AnonymousClass2.lambda$handleMessage$1(this.f$0);
                }
            };
            if (!FallbackTakeScreenshotService.this.mUserManager.isUserUnlocked()) {
                Log.w("TakeScreenshotService", "Skipping screenshot because storage is locked!");
                post(new Runnable(r8) {
                    /* class com.android.systemui.screenshot.$$Lambda$FallbackTakeScreenshotService$2$Pn52_UjuPWms4LS9K_s9O3bSGFA */
                    public final /* synthetic */ Consumer f$0;

                    {
                        this.f$0 = r1;
                    }

                    public final void run() {
                        this.f$0.accept(null);
                    }
                });
                post(r9);
                return;
            }
            ScreenshotHelper.ScreenshotRequest screenshotRequest = (ScreenshotHelper.ScreenshotRequest) message.obj;
            FallbackTakeScreenshotService.this.mUiEventLogger.log(ScreenshotEvent.getScreenshotSource(screenshotRequest.getSource()));
            int i = message.what;
            if (i == 1) {
                FallbackTakeScreenshotService.this.mScreenshot.takeScreenshot(r8, r9);
            } else if (i == 2) {
                FallbackTakeScreenshotService.this.mScreenshot.takeScreenshotPartial(r8, r9);
            } else if (i != 3) {
                Log.d("TakeScreenshotService", "Invalid screenshot option: " + message.what);
            } else {
                FallbackTakeScreenshotService.this.mScreenshot.handleImageAsScreenshot(BitmapUtil.bundleToHardwareBitmap(screenshotRequest.getBitmapBundle()), screenshotRequest.getBoundsInScreen(), screenshotRequest.getInsets(), screenshotRequest.getTaskId(), screenshotRequest.getUserId(), screenshotRequest.getTopComponent(), r8, r9);
            }
        }

        static /* synthetic */ void lambda$handleMessage$0(Messenger messenger, Uri uri) {
            try {
                messenger.send(Message.obtain(null, 1, uri));
            } catch (RemoteException unused) {
            }
        }

        static /* synthetic */ void lambda$handleMessage$1(Messenger messenger) {
            try {
                messenger.send(Message.obtain((Handler) null, 2));
            } catch (RemoteException unused) {
            }
        }
    };
    private final GlobalScreenshot mScreenshot;
    private final UiEventLogger mUiEventLogger;
    private final UserManager mUserManager;

    public FallbackTakeScreenshotService(GlobalScreenshot globalScreenshot, UserManager userManager, UiEventLogger uiEventLogger) {
        this.mScreenshot = globalScreenshot;
        this.mUserManager = userManager;
        this.mUiEventLogger = uiEventLogger;
    }

    public IBinder onBind(Intent intent) {
        registerReceiver(this.mBroadcastReceiver, new IntentFilter("android.intent.action.CLOSE_SYSTEM_DIALOGS"));
        return new Messenger(this.mHandler).getBinder();
    }

    public boolean onUnbind(Intent intent) {
        GlobalScreenshot globalScreenshot = this.mScreenshot;
        if (globalScreenshot != null) {
            globalScreenshot.stopScreenshot();
        }
        unregisterReceiver(this.mBroadcastReceiver);
        return true;
    }
}
