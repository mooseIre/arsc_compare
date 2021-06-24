package com.android.systemui.plugins;

import android.graphics.drawable.Drawable;
import com.android.systemui.plugins.VolumeDialogController;
import com.android.systemui.plugins.annotations.DependsOn;
import com.android.systemui.plugins.annotations.ProvidesInterface;

@ProvidesInterface(version = 1)
@DependsOn(target = VolumeDialogController.Callbacks.class)
public interface MediaDataManagerInterface {
    public static final int VERSION = 1;

    @ProvidesInterface(version = 1)
    public interface ArtListener {
        public static final int VERSION = 1;

        void onMediaDataLoaded(String str, Drawable drawable);
    }

    void addArtListener(ArtListener artListener);

    Drawable getMediaArtDrawable(String str);

    void removeArtListener(ArtListener artListener);
}
