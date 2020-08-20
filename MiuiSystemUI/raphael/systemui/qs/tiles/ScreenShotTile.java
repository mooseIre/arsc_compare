package com.android.systemui.qs.tiles;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.UserHandle;
import android.text.TextUtils;
import android.widget.Switch;
import com.android.systemui.Util;
import com.android.systemui.plugins.R;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.statusbar.Icons;
import java.io.File;
import java.io.FilenameFilter;

public class ScreenShotTile extends QSTileImpl<QSTile.BooleanState> {
    public static final Uri HTTPS_AUTHORITY_URI = Uri.parse("https://gallery.i.mi.com");

    public int getMetricsCategory() {
        return -1;
    }

    public void handleSetListening(boolean z) {
    }

    /* access modifiers changed from: protected */
    public void handleUserSwitch(int i) {
    }

    public boolean isAvailable() {
        return true;
    }

    public ScreenShotTile(QSHost qSHost) {
        super(qSHost);
    }

    /* access modifiers changed from: protected */
    public void handleDestroy() {
        super.handleDestroy();
    }

    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    public Intent getLongClickIntent() {
        Intent intent = new Intent("android.intent.action.VIEW", HTTPS_AUTHORITY_URI.buildUpon().appendPath("album").appendQueryParameter("local_path", "dcim/screenshots").build());
        intent.setPackage("com.miui.gallery");
        if (Util.isIntentActivityExist(this.mContext, intent)) {
            return intent;
        }
        return getLastScreenShotIntent();
    }

    /* access modifiers changed from: protected */
    public void handleClick() {
        if (this.mHost.getBarState() == 2) {
            captureScreen();
            return;
        }
        this.mHost.collapsePanels();
        this.mHandler.post(new Runnable() {
            public void run() {
                if (ScreenShotTile.this.mHost.isQSFullyCollapsed()) {
                    ScreenShotTile.this.mHandler.postDelayed(new Runnable() {
                        public void run() {
                            ScreenShotTile.this.captureScreen();
                        }
                    }, 300);
                } else {
                    ScreenShotTile.this.mHandler.postDelayed(this, 50);
                }
            }
        });
    }

    /* access modifiers changed from: private */
    public void captureScreen() {
        this.mContext.sendBroadcastAsUser(new Intent("android.intent.action.CAPTURE_SCREENSHOT"), UserHandle.CURRENT);
    }

    public CharSequence getTileLabel() {
        return this.mContext.getString(R.string.quick_settings_screenshot_label);
    }

    /* access modifiers changed from: protected */
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        booleanState.value = false;
        booleanState.state = 1;
        booleanState.icon = QSTileImpl.ResourceIcon.get(Icons.getQSIcons(Integer.valueOf(R.drawable.ic_qs_screenshot), this.mInControlCenter));
        booleanState.label = this.mHost.getContext().getString(R.string.quick_settings_screenshot_label);
        booleanState.contentDescription = this.mContext.getString(R.string.quick_settings_screenshot_label);
        booleanState.expandedAccessibilityClassName = Switch.class.getName();
    }

    private Intent getLastScreenShotIntent() {
        String str;
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Screenshots");
        if (!file.exists() || !file.isDirectory()) {
            str = null;
        } else {
            File[] listFiles = file.listFiles(new FilenameFilter() {
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
