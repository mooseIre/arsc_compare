package com.android.systemui;

import android.app.Activity;
import android.content.Intent;
import android.service.dreams.Sandman;

public class Somnambulator extends Activity {
    public void onStart() {
        super.onStart();
        Intent intent = getIntent();
        if ("android.intent.action.CREATE_SHORTCUT".equals(intent.getAction())) {
            Intent intent2 = new Intent(this, Somnambulator.class);
            intent2.setFlags(276824064);
            Intent intent3 = new Intent();
            intent3.putExtra("android.intent.extra.shortcut.ICON_RESOURCE", Intent.ShortcutIconResource.fromContext(this, C0018R$mipmap.ic_launcher_dreams));
            intent3.putExtra("android.intent.extra.shortcut.INTENT", intent2);
            intent3.putExtra("android.intent.extra.shortcut.NAME", getString(C0021R$string.start_dreams));
            setResult(-1, intent3);
        } else if (intent.hasCategory("android.intent.category.DESK_DOCK")) {
            Sandman.startDreamWhenDockedIfAppropriate(this);
        } else {
            Sandman.startDreamByUserRequest(this);
        }
        finish();
    }
}
