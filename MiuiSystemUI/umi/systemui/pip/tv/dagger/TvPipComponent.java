package com.android.systemui.pip.tv.dagger;

import com.android.systemui.pip.tv.PipControlsView;
import com.android.systemui.pip.tv.PipControlsViewController;

public interface TvPipComponent {

    public interface Builder {
        TvPipComponent build();

        Builder pipControlsView(PipControlsView pipControlsView);
    }

    PipControlsViewController getPipControlsViewController();
}
