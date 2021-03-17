package com.android.systemui.qs.tiles;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;
import androidx.appcompat.R$styleable;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import java.util.Arrays;
import java.util.Objects;

public class IntentTile extends QSTileImpl<QSTile.State> {
    private int mCurrentUserId;
    private String mIntentPackage;
    private Intent mLastIntent;
    private PendingIntent mOnClick;
    private String mOnClickUri;
    private PendingIntent mOnLongClick;
    private String mOnLongClickUri;
    private final BroadcastReceiver mReceiver;

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public Intent getLongClickIntent() {
        return null;
    }

    @Override // com.android.systemui.plugins.qs.QSTile, com.android.systemui.qs.tileimpl.QSTileImpl
    public int getMetricsCategory() {
        return R$styleable.AppCompatTheme_windowFixedHeightMinor;
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleSetListening(boolean z) {
    }

    private IntentTile(QSHost qSHost, String str) {
        super(qSHost);
        AnonymousClass1 r2 = new BroadcastReceiver() {
            /* class com.android.systemui.qs.tiles.IntentTile.AnonymousClass1 */

            public void onReceive(Context context, Intent intent) {
                IntentTile.this.refreshState(intent);
            }
        };
        this.mReceiver = r2;
        this.mContext.registerReceiver(r2, new IntentFilter(str));
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleDestroy() {
        super.handleDestroy();
        this.mContext.unregisterReceiver(this.mReceiver);
    }

    public static IntentTile create(QSHost qSHost, String str) {
        if (str == null || !str.startsWith("intent(") || !str.endsWith(")")) {
            throw new IllegalArgumentException("Bad intent tile spec: " + str);
        }
        String substring = str.substring(7, str.length() - 1);
        if (!substring.isEmpty()) {
            return new IntentTile(qSHost, substring);
        }
        throw new IllegalArgumentException("Empty intent tile spec action");
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public QSTile.State newTileState() {
        return new QSTile.State();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleUserSwitch(int i) {
        super.handleUserSwitch(i);
        this.mCurrentUserId = i;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleClick() {
        sendIntent("click", this.mOnClick, this.mOnClickUri);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleLongClick() {
        sendIntent("long-click", this.mOnLongClick, this.mOnLongClickUri);
    }

    private void sendIntent(String str, PendingIntent pendingIntent, String str2) {
        if (pendingIntent != null) {
            try {
                if (pendingIntent.isActivity()) {
                    ((ActivityStarter) Dependency.get(ActivityStarter.class)).postStartActivityDismissingKeyguard(pendingIntent);
                } else {
                    pendingIntent.send();
                }
            } catch (Throwable th) {
                String str3 = this.TAG;
                Log.w(str3, "Error sending " + str + " intent", th);
            }
        } else if (str2 != null) {
            this.mContext.sendBroadcastAsUser(Intent.parseUri(str2, 1), new UserHandle(this.mCurrentUserId));
        }
    }

    @Override // com.android.systemui.plugins.qs.QSTile
    public CharSequence getTileLabel() {
        return getState().label;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleUpdateState(QSTile.State state, Object obj) {
        Intent intent = (Intent) obj;
        if (intent != null || (intent = this.mLastIntent) != null) {
            this.mLastIntent = intent;
            state.contentDescription = intent.getStringExtra("contentDescription");
            state.label = intent.getStringExtra("label");
            state.icon = null;
            byte[] byteArrayExtra = intent.getByteArrayExtra("iconBitmap");
            if (byteArrayExtra != null) {
                try {
                    state.icon = new BytesIcon(byteArrayExtra);
                } catch (Throwable th) {
                    String str = this.TAG;
                    Log.w(str, "Error loading icon bitmap, length " + byteArrayExtra.length, th);
                }
            } else {
                int intExtra = intent.getIntExtra("iconId", 0);
                if (intExtra != 0) {
                    String stringExtra = intent.getStringExtra("iconPackage");
                    if (!TextUtils.isEmpty(stringExtra)) {
                        state.icon = new PackageDrawableIcon(stringExtra, intExtra);
                    } else {
                        state.icon = QSTileImpl.ResourceIcon.get(intExtra);
                    }
                }
            }
            this.mOnClick = (PendingIntent) intent.getParcelableExtra("onClick");
            this.mOnClickUri = intent.getStringExtra("onClickUri");
            this.mOnLongClick = (PendingIntent) intent.getParcelableExtra("onLongClick");
            this.mOnLongClickUri = intent.getStringExtra("onLongClickUri");
            String stringExtra2 = intent.getStringExtra("package");
            this.mIntentPackage = stringExtra2;
            if (stringExtra2 == null) {
                stringExtra2 = "";
            }
            this.mIntentPackage = stringExtra2;
        }
    }

    private static class BytesIcon extends QSTile.Icon {
        private final byte[] mBytes;

        public BytesIcon(byte[] bArr) {
            this.mBytes = bArr;
        }

        @Override // com.android.systemui.plugins.qs.QSTile.Icon
        public Drawable getDrawable(Context context) {
            byte[] bArr = this.mBytes;
            return new BitmapDrawable(context.getResources(), BitmapFactory.decodeByteArray(bArr, 0, bArr.length));
        }

        public boolean equals(Object obj) {
            return (obj instanceof BytesIcon) && Arrays.equals(((BytesIcon) obj).mBytes, this.mBytes);
        }

        @Override // com.android.systemui.plugins.qs.QSTile.Icon
        public String toString() {
            return String.format("BytesIcon[len=%s]", Integer.valueOf(this.mBytes.length));
        }
    }

    private class PackageDrawableIcon extends QSTile.Icon {
        private final String mPackage;
        private final int mResId;

        public PackageDrawableIcon(String str, int i) {
            this.mPackage = str;
            this.mResId = i;
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof PackageDrawableIcon)) {
                return false;
            }
            PackageDrawableIcon packageDrawableIcon = (PackageDrawableIcon) obj;
            if (!Objects.equals(packageDrawableIcon.mPackage, this.mPackage) || packageDrawableIcon.mResId != this.mResId) {
                return false;
            }
            return true;
        }

        @Override // com.android.systemui.plugins.qs.QSTile.Icon
        public Drawable getDrawable(Context context) {
            try {
                return context.createPackageContext(this.mPackage, 0).getDrawable(this.mResId);
            } catch (Throwable th) {
                String str = ((QSTileImpl) IntentTile.this).TAG;
                Log.w(str, "Error loading package drawable pkg=" + this.mPackage + " id=" + this.mResId, th);
                return null;
            }
        }

        @Override // com.android.systemui.plugins.qs.QSTile.Icon
        public String toString() {
            return String.format("PackageDrawableIcon[pkg=%s,id=0x%08x]", this.mPackage, Integer.valueOf(this.mResId));
        }
    }
}
