package com.android.systemui.qs.tiles;

import android.content.Intent;
import android.net.Uri;
import android.os.UserHandle;
import android.text.TextUtils;
import android.widget.Switch;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0021R$string;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.qs.tiles.ScreenShotTile;
import java.io.File;
import java.io.FilenameFilter;
import miui.os.Environment;

public class ScreenShotTile extends QSTileImpl<QSTile.BooleanState> {
    public static final Uri HTTPS_AUTHORITY_URI = Uri.parse("https://gallery.i.mi.com");

    @Override // com.android.systemui.plugins.qs.QSTile, com.android.systemui.qs.tileimpl.QSTileImpl
    public int getMetricsCategory() {
        return -1;
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleSetListening(boolean z) {
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleUserSwitch(int i) {
    }

    @Override // com.android.systemui.plugins.qs.QSTile, com.android.systemui.qs.tileimpl.QSTileImpl
    public boolean isAvailable() {
        return true;
    }

    public ScreenShotTile(QSHost qSHost) {
        super(qSHost);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleDestroy() {
        super.handleDestroy();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public Intent getLongClickIntent() {
        Intent intent = new Intent("android.intent.action.VIEW", HTTPS_AUTHORITY_URI.buildUpon().appendPath("album").appendQueryParameter("local_path", "dcim/screenshots").build());
        boolean z = false;
        try {
            if (this.mContext.getPackageManager().queryIntentActivities(intent, 786432).size() > 0) {
                z = true;
            }
        } catch (Exception unused) {
        }
        if (z) {
            return intent;
        }
        return getLastScreenShotIntent();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleClick() {
        if (QSTileImpl.mInControlCenter || !(this.mHost.getBarState() == 2 || this.mHost.getBarState() == 1)) {
            this.mHost.collapsePanels();
            this.mHandler.post(new Runnable() {
                /* class com.android.systemui.qs.tiles.ScreenShotTile.AnonymousClass1 */

                public void run() {
                    if (((QSTileImpl) ScreenShotTile.this).mHost.isQSFullyCollapsed()) {
                        ((QSTileImpl) ScreenShotTile.this).mHandler.postDelayed(new Runnable() {
                            /* class com.android.systemui.qs.tiles.$$Lambda$ScreenShotTile$1$b6JRTqlIYtU55jJIIw6aPFB_o */

                            public final void run() {
                                ScreenShotTile.AnonymousClass1.this.lambda$run$0$ScreenShotTile$1();
                            }
                        }, 300);
                    } else {
                        ((QSTileImpl) ScreenShotTile.this).mHandler.postDelayed(this, 50);
                    }
                }

                /* access modifiers changed from: private */
                /* renamed from: lambda$run$0 */
                public /* synthetic */ void lambda$run$0$ScreenShotTile$1() {
                    ScreenShotTile.this.captureScreen();
                }
            });
            return;
        }
        captureScreen();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void captureScreen() {
        this.mContext.sendBroadcastAsUser(new Intent("android.intent.action.CAPTURE_SCREENSHOT"), UserHandle.CURRENT);
    }

    @Override // com.android.systemui.plugins.qs.QSTile
    public CharSequence getTileLabel() {
        return this.mContext.getString(C0021R$string.quick_settings_screenshot_label);
    }

    /* access modifiers changed from: protected */
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        booleanState.value = false;
        booleanState.state = 1;
        booleanState.icon = QSTileImpl.ResourceIcon.get(C0013R$drawable.ic_qs_screenshot);
        booleanState.label = this.mHost.getContext().getString(C0021R$string.quick_settings_screenshot_label);
        booleanState.contentDescription = this.mContext.getString(C0021R$string.quick_settings_screenshot_label);
        booleanState.expandedAccessibilityClassName = Switch.class.getName();
    }

    private Intent getLastScreenShotIntent() {
        String str;
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Screenshots");
        if (!file.exists() || !file.isDirectory()) {
            str = null;
        } else {
            File[] listFiles = file.listFiles(new FilenameFilter(this) {
                /* class com.android.systemui.qs.tiles.ScreenShotTile.AnonymousClass2 */

                public boolean accept(File file, String str) {
                    String lowerCase = str.toLowerCase();
                    return lowerCase.endsWith("png") || lowerCase.endsWith("jpg") || lowerCase.endsWith("jpeg");
                }
            });
            if (listFiles == null) {
                return null;
            }
            long j = 0;
            str = null;
            for (File file2 : listFiles) {
                if (file2.lastModified() > j) {
                    j = file2.lastModified();
                    str = file2.getAbsolutePath();
                }
            }
        }
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.setDataAndType(Uri.fromFile(new File(str)), "image/*");
        intent.setFlags(268435456);
        return intent;
    }
}
