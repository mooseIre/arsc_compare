package com.android.systemui.media;

import android.content.Context;
import android.media.session.MediaController;
import android.media.session.MediaSession;

public class MediaControllerFactory {
    private final Context mContext;

    public MediaControllerFactory(Context context) {
        this.mContext = context;
    }

    public MediaController create(MediaSession.Token token) {
        return new MediaController(this.mContext, token);
    }
}
